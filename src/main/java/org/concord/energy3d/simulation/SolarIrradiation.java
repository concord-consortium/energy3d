package org.concord.energy3d.simulation;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.UserData;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.Util;
import org.poly2tri.geometry.primitives.Point;
import org.poly2tri.transform.coordinate.AnyToXYTransform;
import org.poly2tri.transform.coordinate.XYToAnyTransform;
import org.poly2tri.triangulation.point.TPoint;

import com.ardor3d.image.Image;
import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.image.PixelDataType;
import com.ardor3d.image.Texture.MinificationFilter;
import com.ardor3d.image.Texture2D;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector2;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.TextureKey;
import com.ardor3d.util.geom.BufferUtils;

public class SolarIrradiation {

	public final static double SOLAR_CONSTANT = 1.361; // http://en.wikipedia.org/wiki/Solar_constant

	public final static int AIR_MASS_NONE = -1;
	public final static int AIR_MASS_KASTEN_YOUNG = 0;
	public final static int AIR_MASS_SPHERE_MODEL = 1;

	private static SolarIrradiation instance = new SolarIrradiation();
	private final Map<Mesh, TextureData> onMesh = new HashMap<Mesh, TextureData>();
	private final List<Spatial> collidables = new ArrayList<Spatial>();
	private int timeStep = 15;
	private double solarStep = 2.0;
	private long maxValue;
	private int airMassSelection = AIR_MASS_SPHERE_MODEL;

	private class TextureData {
		public Vector3 p0;
		public Vector3 p1;
		public Vector3 p2;
		public Vector3 u;
		public Vector3 v;
		public int rows;
		public int cols;
		public double[][] solar;
	}

	public static SolarIrradiation getInstance() {
		return instance;
	}

	public void compute() {
		System.out.println("computeIrradiation()");
		initCollidables();
		onMesh.clear();
		synchronized (Scene.getInstance().getParts()) {
			for (final HousePart part : Scene.getInstance().getParts())
				part.setSolarPotential(new double[1440 / timeStep]);
		}
		maxValue = 1;
		computeToday((Calendar) Heliodon.getInstance().getCalender().clone());
		updateValueOnAllHouses();
	}

	private void initCollidables() {
		collidables.clear();
		synchronized (Scene.getInstance().getParts()) {
			for (final HousePart part : Scene.getInstance().getParts()) {
				if (part instanceof Foundation || part instanceof Wall || part instanceof SolarPanel || part instanceof Tree || part instanceof Sensor)
					collidables.add(part.getIrradiationCollisionSpatial());
				else if (part instanceof Roof)
					for (final Spatial roofPart : ((Roof) part).getRoofPartsRoot().getChildren())
						collidables.add(((Node) roofPart).getChild(0));
			}
		}
	}

	private void computeToday(final Calendar today) {
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.HOUR_OF_DAY, 0);
		final ReadOnlyVector3[] sunLocations = new ReadOnlyVector3[1440 / timeStep];
		int totalSteps = 0;
		for (int minute = 0; minute < 1440; minute += timeStep) {
			final ReadOnlyVector3 sunLocation = Heliodon.getInstance().computeSunLocation(today).normalize(null);
			// final ReadOnlyVector3 sunLocation = Heliodon.getInstance().getSunLocation();
			// timeStep = 1440;
			sunLocations[minute / timeStep] = sunLocation;
			if (sunLocation.getZ() > 0)
				totalSteps++;
			today.add(Calendar.MINUTE, timeStep);
		}
		totalSteps -= 2;
		int step = 1;
		for (int minute = 0; minute < 1440; minute += timeStep) {
			final ReadOnlyVector3 sunLocation = sunLocations[minute / timeStep];
			if (sunLocation.getZ() > 0) {
				final ReadOnlyVector3 directionTowardSun = sunLocation.normalize(null);
				synchronized (Scene.getInstance().getParts()) {
					for (final HousePart part : Scene.getInstance().getParts()) {
						if (part.isDrawCompleted())
							if (part instanceof Foundation || part instanceof Wall || part instanceof Window || part instanceof Sensor)
								// if (part instanceof Foundation || part instanceof Wall || part instanceof Window || part instanceof Sensor || part instanceof SolarPanel)
								computeOnMesh(minute, directionTowardSun, part, part.getIrradiationMesh(), (Mesh) part.getIrradiationCollisionSpatial(), part.getFaceDirection(), true);
							else if (part instanceof SolarPanel)
								computeOnMeshSolarPanel(minute, directionTowardSun, part, part.getIrradiationMesh(), (Mesh) part.getIrradiationCollisionSpatial(), part.getFaceDirection(), true);
							else if (part instanceof Roof)
								for (final Spatial roofPart : ((Roof) part).getRoofPartsRoot().getChildren()) {
									final ReadOnlyVector3 faceDirection = (ReadOnlyVector3) roofPart.getUserData();
									final Mesh mesh = (Mesh) ((Node) roofPart).getChild(0);
									computeOnMesh(minute, directionTowardSun, part, mesh, mesh, faceDirection, false);
								}
					}
				}
				computeOnLand(directionTowardSun);
				EnergyPanel.getInstance().progress(100 * step / totalSteps);
				step++;
			}
			maxValue++;
		}
		maxValue *= (100 - EnergyPanel.getInstance().getColorMapSlider().getValue()) / 100.0;
	}

	// Formula from http://en.wikipedia.org/wiki/Air_mass_(solar_energy)#Solar_intensity
	private void computeOnMesh(final int minute, final ReadOnlyVector3 directionTowardSun, final HousePart housePart, final Mesh drawMesh, final Mesh collisionMesh, final ReadOnlyVector3 normal, final boolean addToTotal) {
		if (normal.dot(directionTowardSun) <= 0)
			return;

		TextureData data = onMesh.get(drawMesh);
		if (data == null)
			data = initMeshTextureData(drawMesh, collisionMesh, normal, !(housePart instanceof Window));

		/* needed in order to prevent picking collision with neighboring wall at wall edge */
		final double OFFSET = 0.1;
		final ReadOnlyVector3 offset = directionTowardSun.multiply(OFFSET, null);
		final double airMass = computeAirMass(directionTowardSun);
		final double dot = 1.1 * normal.dot(directionTowardSun) * SOLAR_CONSTANT * Math.pow(0.7, Math.pow(airMass, 0.678));
		final double annotationScale = Scene.getInstance().getAnnotationScale();
		final double scaleFactor = annotationScale * annotationScale / 60 * timeStep;

		for (int col = 0; col < data.cols; col++) {
			final ReadOnlyVector3 pU = data.u.multiply(solarStep / 2.0 + col * solarStep, null).addLocal(data.p0);
			final double w = (col == data.cols - 1) ? data.p2.distance(pU) : solarStep;
			for (int row = 0; row < data.rows; row++) {
				if (EnergyPanel.getInstance().isCancelled())
					throw new CancellationException();
				if (data.solar[row][col] == -1)
					continue;
				final ReadOnlyVector3 p = data.v.multiply(solarStep / 2.0 + row * solarStep, null).addLocal(pU).add(offset, null);
				final double h;
				if (row == data.rows - 1)
					h = data.p1.subtract(data.p0, null).length() - (row * solarStep);
				else
					h = solarStep;
				final Ray3 pickRay = new Ray3(p, directionTowardSun);
				final PickResults pickResults = new PrimitivePickResults();
				// final PickResults pickResults = new BoundingPickResults();
				boolean collision = false;
				for (final Spatial spatial : collidables)
					if (spatial != collisionMesh) {
						PickingUtil.findPick(spatial, pickRay, pickResults, false);
						if (pickResults.getNumber() != 0) {
							collision = true;
							break;
						}
					}

				if (!collision) {
					data.solar[row][col] += dot;
					housePart.getSolarPotential()[minute / timeStep] += dot * w * h * scaleFactor;
				}
			}
		}

	}

	private void computeOnMeshSolarPanel(final int minute, final ReadOnlyVector3 directionTowardSun, final HousePart housePart, final Mesh drawMesh, final Mesh collisionMesh, final ReadOnlyVector3 normal, final boolean addToTotal) {
		if (normal.dot(directionTowardSun) <= 0)
			return;

		TextureData data = onMesh.get(drawMesh);
		if (data == null)
			data = initMeshTextureDataSolarPanel(drawMesh, collisionMesh, normal);

		final double OFFSET = 3;
		final ReadOnlyVector3 offset = directionTowardSun.multiply(OFFSET, null);
		final double airMass = computeAirMass(directionTowardSun);
		final double dot = 1.1 * normal.dot(directionTowardSun) * SOLAR_CONSTANT * Math.pow(0.7, Math.pow(airMass, 0.678));
		final double annotationScale = Scene.getInstance().getAnnotationScale();
		// final double scaleFactor = annotationScale * annotationScale / 60 * timeStep;
		final double scaleFactor = 1.0 / 60 * timeStep;
		final FloatBuffer vertexBuffer = drawMesh.getMeshData().getVertexBuffer();

		for (int col = 0; col < data.cols; col++) {
			for (int row = 0; row < data.rows; row++) {
				if (EnergyPanel.getInstance().isCancelled())
					throw new CancellationException();
				final int index;
				if (row == 0 && col == 0)
					index = 1 * 3;
				else if (row == 0 && col == 1)
					index = 0 * 3;
				else if (row == 1 && col == 0)
					index = 2 * 3;
				else
					index = 4 * 3;
				final Vector3 point = new Vector3(vertexBuffer.get(index), vertexBuffer.get(index + 1), vertexBuffer.get(index + 2));
				// System.out.println(point);
				final ReadOnlyVector3 p = drawMesh.getWorldTransform().applyForward(point).addLocal(offset);
				final Ray3 pickRay = new Ray3(p, directionTowardSun);
				final PickResults pickResults = new PrimitivePickResults();
				// final PickResults pickResults = new BoundingPickResults();
				boolean collision = false;
				for (final Spatial spatial : collidables)
					if (spatial != collisionMesh) {
						PickingUtil.findPick(spatial, pickRay, pickResults, false);
						if (pickResults.getNumber() != 0) {
							collision = true;
							break;
						}
					}

				if (!collision) {
					data.solar[row][col] += dot;
					housePart.getSolarPotential()[minute / timeStep] += dot * SolarPanel.WIDTH * SolarPanel.HEIGHT / 4.0 * scaleFactor;
				}
			}
		}
	}

	public void initMeshTextureData(final Mesh drawMesh, final Mesh collisionMesh, final ReadOnlyVector3 normal) {
		if (onMesh.get(drawMesh) == null)
			drawMesh.setDefaultColor(ColorRGBA.BLUE);

		initMeshTextureData(drawMesh, collisionMesh, normal, true);
	}

	private TextureData initMeshTextureData(final Mesh drawMesh, final Mesh collisionMesh, final ReadOnlyVector3 normal, final boolean updateTexture) {
		final TextureData data = new TextureData();

		final AnyToXYTransform toXY = new AnyToXYTransform(normal.getX(), normal.getY(), normal.getZ());
		final XYToAnyTransform fromXY = new XYToAnyTransform(normal.getX(), normal.getY(), normal.getZ());

		final FloatBuffer vertexBuffer = collisionMesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		double minX, minY, maxX, maxY;
		minX = minY = Double.POSITIVE_INFINITY;
		maxX = maxY = Double.NEGATIVE_INFINITY;
		double z = Double.NaN;
		final List<ReadOnlyVector2> points = new ArrayList<ReadOnlyVector2>(vertexBuffer.limit() / 3);
		while (vertexBuffer.hasRemaining()) {
			final Vector3 pWorld = drawMesh.localToWorld(new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get()), null);
			final Point p = new TPoint(pWorld.getX(), pWorld.getY(), pWorld.getZ());
			toXY.transform(p);
			if (p.getX() < minX)
				minX = p.getX();
			if (p.getX() > maxX)
				maxX = p.getX();
			if (p.getY() < minY)
				minY = p.getY();
			if (p.getY() > maxY)
				maxY = p.getY();
			if (Double.isNaN(z))
				z = p.getZ();
			points.add(new Vector2(p.getX(), p.getY()));
		}

		final Point tmp = new TPoint(minX, minY, z);
		fromXY.transform(tmp);
		data.p0 = new Vector3(tmp.getX(), tmp.getY(), tmp.getZ());

		tmp.set(minX, maxY, z);
		fromXY.transform(tmp);
		data.p1 = new Vector3(tmp.getX(), tmp.getY(), tmp.getZ());

		tmp.set(maxX, minY, z);
		fromXY.transform(tmp);
		data.p2 = new Vector3(tmp.getX(), tmp.getY(), tmp.getZ());

		data.rows = (int) Math.ceil(data.p1.subtract(data.p0, null).length() / solarStep);
		data.cols = (int) Math.ceil(data.p2.subtract(data.p0, null).length() / solarStep);
		if (data.solar == null)
			data.solar = new double[roundToPowerOfTwo(data.rows)][roundToPowerOfTwo(data.cols)];

		if (onMesh.get(drawMesh) == null) {
			final ReadOnlyVector2 originXY = new Vector2(minX, minY);
			final ReadOnlyVector2 uXY = new Vector2(maxX - minX, 0).normalizeLocal();
			final ReadOnlyVector2 vXY = new Vector2(0, maxY - minY).normalizeLocal();
			for (int row = 0; row < data.solar.length; row++)
				for (int col = 0; col < data.solar[0].length; col++) {
					if (row >= data.rows || col >= data.cols)
						data.solar[row][col] = -1;
					else {
						final ReadOnlyVector2 p = originXY.add(uXY.multiply(col * solarStep, null), null).add(vXY.multiply(row * solarStep, null), null);
						boolean isInside = false;
						for (int i = 0; i < points.size(); i += 3) {
							if (Util.isPointInsideTriangle(p, points.get(i), points.get(i + 1), points.get(i + 2))) {
								isInside = true;
								break;
							}
						}
						if (!isInside)
							data.solar[row][col] = -1;
					}
				}
		}

		data.u = data.p2.subtract(data.p0, null).normalizeLocal();
		data.v = data.p1.subtract(data.p0, null).normalizeLocal();

		onMesh.put(drawMesh, data);

		if (updateTexture)
			updateTextureCoords(drawMesh);
		return data;
	}

	private TextureData initMeshTextureDataSolarPanel(final Mesh drawMesh, final Mesh collisionMesh, final ReadOnlyVector3 normal) {
		final TextureData data = new TextureData();

		data.rows = 4;
		data.cols = 4;
		if (data.solar == null)
			data.solar = new double[roundToPowerOfTwo(data.rows)][roundToPowerOfTwo(data.cols)];

		onMesh.put(drawMesh, data);
		return data;
	}

	// air mass calculation from http://en.wikipedia.org/wiki/Air_mass_(solar_energy)#At_higher_altitudes
	public double computeAirMass(final ReadOnlyVector3 directionTowardSun) {
		switch (airMassSelection) {
		case AIR_MASS_NONE:
			return 1;
		case AIR_MASS_KASTEN_YOUNG:
			double zenithAngle = directionTowardSun.smallestAngleBetween(Vector3.UNIT_Z);
			return 1 / (Math.cos(zenithAngle) + 0.50572 * Math.pow(96.07995 - zenithAngle / Math.PI * 180.0, -1.6364));
		default:
			zenithAngle = directionTowardSun.smallestAngleBetween(Vector3.UNIT_Z);
			final double cos = Math.cos(zenithAngle);
			final double r = 708;
			final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
			if (!"".equals(city)) {
				final double c = CityData.getInstance().getCityAltitudes().get(city) / 9000.0;
				return Math.sqrt((r + c) * (r + c) * cos * cos + (2 * r + 1 + c) * (1 - c)) - (r + c) * cos;
			} else {
				return Math.sqrt(r * r * cos * cos + 2 * r + 1) - r * cos;
			}
		}
	}

	private void updateTextureCoords(final Mesh drawMesh) {
		final TextureData data = onMesh.get(drawMesh);
		final ReadOnlyVector3 o = data.p0;
		final ReadOnlyVector3 u = data.u.multiply(roundToPowerOfTwo(data.cols) * getSolarStep(), null);
		final ReadOnlyVector3 v = data.v.multiply(roundToPowerOfTwo(data.rows) * getSolarStep(), null);
		final FloatBuffer vertexBuffer = drawMesh.getMeshData().getVertexBuffer();
		final FloatBuffer textureBuffer = drawMesh.getMeshData().getTextureBuffer(0);
		vertexBuffer.rewind();
		textureBuffer.rewind();
		while (vertexBuffer.hasRemaining()) {
			final ReadOnlyVector3 p = drawMesh.localToWorld(new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get()), null);
			final Vector3 uP = Util.closestPoint(o, u, p, v.negate(null));
			final Vector3 vP = Util.closestPoint(o, v, p, u.negate(null));
			if (uP != null && vP != null) {
				final float uScale = (float) (uP.distance(o) / u.length());
				final float vScale = (float) (vP.distance(o) / v.length());
				textureBuffer.put(uScale).put(vScale);
			}
		}
	}

	private void computeOnLand(final ReadOnlyVector3 directionTowardSun) {
		final double dot = SOLAR_CONSTANT * directionTowardSun.dot(Vector3.UNIT_Z) / computeAirMass(directionTowardSun);
		final double step = this.solarStep * 4;
		final int rows = (int) (256 / step);
		final int cols = rows;
		TextureData data = onMesh.get(SceneManager.getInstance().getSolarLand());
		if (data == null) {
			data = new TextureData();
			data.solar = new double[rows][cols];
			onMesh.put(SceneManager.getInstance().getSolarLand(), data);
		}
		final Vector3 p = new Vector3();
		for (int col = 0; col < cols; col++) {
			p.setX((col - cols / 2) * step + step / 2.0);
			for (int row = 0; row < rows; row++) {
				if (EnergyPanel.getInstance().isCancelled())
					throw new CancellationException();
				p.setY((row - rows / 2) * step + step / 2.0);
				final Ray3 pickRay = new Ray3(p, directionTowardSun);
				final PickResults pickResults = new PrimitivePickResults();
				for (final Spatial spatial : collidables)
					PickingUtil.findPick(spatial, pickRay, pickResults, false);
				if (pickResults.getNumber() == 0)
					data.solar[row][col] += dot;
			}
		}
	}

	private void updateValueOnAllHouses() {
		applyTexture(SceneManager.getInstance().getSolarLand());
		synchronized (Scene.getInstance().getParts()) {
			for (final HousePart part : Scene.getInstance().getParts())
				if (part instanceof Foundation || part instanceof Wall || part instanceof Window || part instanceof SolarPanel || part instanceof Sensor)
					applyTexture(part.getIrradiationMesh());
				else if (part instanceof Roof)
					for (final Spatial roofPart : ((Roof) part).getRoofPartsRoot().getChildren()) {
						final Mesh mesh = (Mesh) ((Node) roofPart).getChild(0);
						applyTexture(mesh);
					}

			/* subtract window potential from wall potential */
			for (final HousePart wall : Scene.getInstance().getParts()) {
				if (wall instanceof Wall) {
					final double[] wallSolarPotentials = wall.getSolarPotential();
					for (final HousePart window : wall.getChildren()) {
						if (window instanceof Window) {
							final double[] windowSolarPotentials = window.getSolarPotential();
							for (int i = 0; i < wallSolarPotentials.length; i++)
								wallSolarPotentials[i] -= windowSolarPotentials[i];
						}
					}
				}
			}

			for (final HousePart part : Scene.getInstance().getParts()) {
				if (part instanceof Foundation) {
					final Foundation foundation = (Foundation) part;
					final double[] heatLoss = foundation.getHeatLoss();
					final double[] passiveSolar = new double[heatLoss.length];
					final double[] photovoltaic = new double[heatLoss.length];
					double solarPotentialTotal = 0.0;
					for (final HousePart houseChild : Scene.getInstance().getParts())
						if (houseChild.getTopContainer() == foundation) {
							houseChild.setSolarPotentialToday(0.0);
							for (int i = 0; i < houseChild.getSolarPotential().length; i++) {
								solarPotentialTotal += houseChild.getSolarPotential()[i];
								houseChild.setSolarPotentialToday(houseChild.getSolarPotentialToday() + houseChild.getSolarPotential()[i]);
								if (houseChild instanceof Wall || houseChild instanceof Door || houseChild instanceof Window || houseChild instanceof Roof)
									heatLoss[i] += houseChild.getHeatLoss()[i];
								if (houseChild instanceof Window)
									passiveSolar[i] += houseChild.getSolarPotential()[i] * Scene.getInstance().getWindowSolarHeatGainCoefficientNotPercentage();
								else if (houseChild instanceof SolarPanel)
									photovoltaic[i] += houseChild.getSolarPotential()[i] * Scene.getInstance().getSolarPanelEfficiencyNotPercentage();
							}
						}

					double heatingTotal = 0.0;
					double coolingTotal = 0.0;
					double passiveSolarTotal = 0.0;
					double photovoltaicTotal = 0.0;
					for (int i = 0; i < heatLoss.length; i++) {
						if (heatLoss[i] < 0)
							heatLoss[i] -= passiveSolar[i];
						else
							heatLoss[i] = Math.max(0, heatLoss[i] - passiveSolar[i]);
						if (heatLoss[i] > 0)
							heatingTotal += heatLoss[i];
						else
							coolingTotal -= heatLoss[i];
						passiveSolarTotal += passiveSolar[i];
						photovoltaicTotal += photovoltaic[i];
					}

					foundation.setSolarPotentialToday(solarPotentialTotal);
					foundation.setSolarLabelValue(solarPotentialTotal);
					foundation.setPassiveSolarToday(passiveSolarTotal);
					foundation.setPhotovoltaicToday(photovoltaicTotal);
					foundation.setHeatingToday(heatingTotal);
					foundation.setCoolingToday(coolingTotal);
					foundation.setTotalEnergyToday(heatingTotal + coolingTotal - photovoltaicTotal);
				}
			}
		}

		// SceneManager.getInstance().refresh();
	}

	private void applyTexture(final Mesh mesh) {
		if (onMesh.get(mesh) == null) {
			mesh.setDefaultColor(ColorRGBA.BLUE);
			mesh.clearRenderState(StateType.Texture);
			return;
		}

		final double[][] solarData = onMesh.get(mesh).solar;
		if (mesh.getUserData() != null && ((UserData) mesh.getUserData()).getHousePart() instanceof SolarPanel) {
			solarData[3][0] = solarData[2][0] = solarData[1][0];
			solarData[3][1] = solarData[2][1] = solarData[1][0];
			solarData[3][2] = solarData[2][2] = solarData[1][1];
			solarData[3][3] = solarData[2][3] = solarData[1][1];

			solarData[0][2] = solarData[1][2] = solarData[0][1];
			solarData[0][3] = solarData[1][3] = solarData[0][1];
			solarData[0][0] = solarData[1][0] = solarData[0][0];
			solarData[0][1] = solarData[1][1] = solarData[0][0];
		}

		fillBlanksWithNeighboringValues(solarData);

		final int rows = solarData.length;
		final int cols = solarData[0].length;
		final ByteBuffer data = BufferUtils.createByteBuffer(cols * rows * 3);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				final ColorRGBA color = computeColor(solarData[row][col], maxValue);
				data.put((byte) (color.getRed() * 255)).put((byte) (color.getGreen() * 255)).put((byte) (color.getBlue() * 255));
			}
		}

		final Image image = new Image(ImageDataFormat.RGB, PixelDataType.UnsignedByte, cols, rows, data, null);
		final Texture2D texture = new Texture2D();
		texture.setTextureKey(TextureKey.getRTTKey(MinificationFilter.NearestNeighborNoMipMaps));
		texture.setImage(image);
		// texture.setWrap(WrapMode.Clamp);
		final TextureState textureState = new TextureState();
		textureState.setTexture(texture);
		mesh.setDefaultColor(Scene.GRAY);
		mesh.setRenderState(textureState);
	}

	private void fillBlanksWithNeighboringValues(final double[][] solarData) {
		final int rows = solarData.length;
		final int cols = solarData[0].length;
		for (int repeat = 0; repeat < 2; repeat++)
			for (int row = 0; row < rows; row++)
				for (int col = 0; col < cols; col++)
					if (solarData[row][col] == -1)
						if (solarData[row][(col + 1) % cols] != -1)
							solarData[row][col] = solarData[row][(col + 1) % cols];
						else if (col != 0 && solarData[row][col - 1] != -1)
							solarData[row][col] = solarData[row][col - 1];
						else if (col == 0 && solarData[row][cols - 1] != -1)
							solarData[row][col] = solarData[row][cols - 1];
						else if (solarData[(row + 1) % rows][col] != -1)
							solarData[row][col] = solarData[(row + 1) % rows][col];
						else if (row != 0 && solarData[row - 1][col] != -1)
							solarData[row][col] = solarData[row - 1][col];
						else if (row == 0 && solarData[rows - 1][col] != -1)
							solarData[row][col] = solarData[rows - 1][col];
	}

	public ColorRGBA computeColor(final double value, final long maxValue) {
		final ReadOnlyColorRGBA[] colors = EnergyPanel.solarColors;
		long valuePerColorRange = maxValue / (colors.length - 1);
		final int colorIndex;
		if (valuePerColorRange == 0) {
			valuePerColorRange = 1;
			colorIndex = 0;
		} else
			colorIndex = (int) Math.min(value / valuePerColorRange, colors.length - 2);
		final float scalar = Math.min(1.0f, (float) (value - valuePerColorRange * colorIndex) / valuePerColorRange);
		final ColorRGBA color = new ColorRGBA().lerpLocal(colors[colorIndex], colors[colorIndex + 1], scalar);
		return color;
	}

	private int roundToPowerOfTwo(final int n) {
		return (int) Math.pow(2.0, Math.ceil(Math.log(n) / Math.log(2)));
	}

	public void setSolarStep(final double solarStep) {
		this.solarStep = solarStep;
	}

	public double getSolarStep() {
		return solarStep;
	}

	public void setTimeStep(final int timeStep) {
		this.timeStep = timeStep;
	}

	public int getTimeStep() {
		return timeStep;
	}

	public void setAirMassSelection(final int selection) {
		airMassSelection = selection;
	}

	public int getAirMassSelection() {
		return airMassSelection;
	}

}
