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
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
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
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.TextureKey;
import com.ardor3d.util.geom.BufferUtils;

public class SolarIrradiation {
	public static final int MINUTE_STEP = 15;
	private static SolarIrradiation instance = new SolarIrradiation();
	
	private final Map<Mesh, double[][]> onMesh = new HashMap<Mesh, double[][]>();
	private final Map<Mesh, Boolean> textureCoordsAlreadyComputed = new HashMap<Mesh, Boolean>();
	private final List<Spatial> collidables = new ArrayList<Spatial>();
	private double[][] onLand;
	private long maxValue;
	private double step = 2.0;
	
	public static SolarIrradiation getInstance() {
		return instance;
	}

	public void compute() {
		System.out.println("computeIrradiation()");
		initCollidables();
		onMesh.clear();
		textureCoordsAlreadyComputed.clear();
		for (final HousePart part : Scene.getInstance().getParts())
			part.setSolarPotential(new double[1440 / SolarIrradiation.MINUTE_STEP]);
		onLand = null;
		maxValue = 1;
		computeToday((Calendar) Heliodon.getInstance().getCalander().clone());
		updateValueOnAllHouses();
	}
	
	private void initCollidables() {
		collidables.clear();
//		collidables.add(Scene.getRoot());
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Foundation)
				collidables.add(part.getMesh());
			else if (part instanceof Wall)
				collidables.add(((Wall) part).getInvisibleMesh());
			else if (part instanceof SolarPanel)
				collidables.add(((SolarPanel) part).getSurroundMesh());			
			else if (part instanceof Roof)
				for (final Spatial roofPart : ((Roof) part).getRoofPartsRoot().getChildren())
					collidables.add(((Node) roofPart).getChild(0));
		}
	}	

	private void computeToday(final Calendar today) {
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.HOUR_OF_DAY, 0);
		for (int minute = 0; minute < 1440; minute += MINUTE_STEP) {
//			final ReadOnlyVector3 sunLocation = Heliodon.getInstance().computeSunLocation(today).normalize(null);
			 final ReadOnlyVector3 sunLocation = Heliodon.getInstance().getSunLocation();
			if (sunLocation.getZ() > 0) {
				final ReadOnlyVector3 directionTowardSun = sunLocation.normalize(null);
				for (final HousePart part : Scene.getInstance().getParts()) {
					if (part.isDrawCompleted())
						if (part instanceof Wall || part instanceof Window || part instanceof SolarPanel) {
							final ReadOnlyVector3 faceDirection = part.getFaceDirection();
							if (faceDirection.dot(directionTowardSun) > 0)
								computeOnMesh(minute, directionTowardSun, part, part.getMesh(), part instanceof Wall ? ((Wall) part).getInvisibleMesh() : part.getMesh(), faceDirection, true);
						} else if (part instanceof Roof) {
							for (final Spatial roofPart : ((Roof) part).getRoofPartsRoot().getChildren()) {
								final ReadOnlyVector3 faceDirection = (ReadOnlyVector3) roofPart.getUserData();
								if (faceDirection.dot(directionTowardSun) > 0) {
									final Mesh mesh = (Mesh) ((Node) roofPart).getChild(0);
									computeOnMesh(minute, directionTowardSun, part, mesh, mesh, faceDirection, false);
								}
							}
						}
				}
				computeOnLand(directionTowardSun);
			}
			maxValue++;
			today.add(Calendar.MINUTE, MINUTE_STEP);
			EnergyPanel.getInstance().progress();
		}
		maxValue *= (100 - EnergyPanel.getInstance().getColorMapSlider().getValue()) / 100.0;
	}

	private void computeOnMesh(final int minute, final ReadOnlyVector3 directionTowardSun, final HousePart housePart, final Mesh drawMesh, final Mesh collisionMesh, final ReadOnlyVector3 normal, final boolean addToTotal) {
		/* needed in order to prevent picking collision with neighboring wall at wall edge */
		final double OFFSET = 0.1;
		final ReadOnlyVector3 offset = directionTowardSun.multiply(OFFSET, null);
		final double zenithAngle = directionTowardSun.smallestAngleBetween(Vector3.UNIT_Z);
		final double airMass = 1 / (Math.cos(zenithAngle) + 0.50572 * Math.pow(96.07995 - zenithAngle / Math.PI * 180.0, -1.6364));

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
		final ReadOnlyVector3 origin = new Vector3(tmp.getX(), tmp.getY(), tmp.getZ());

		tmp.set(minX, maxY, z);
		fromXY.transform(tmp);
		final ReadOnlyVector3 p1 = new Vector3(tmp.getX(), tmp.getY(), tmp.getZ());

		tmp.set(maxX, minY, z);
		fromXY.transform(tmp);
		final ReadOnlyVector3 p2 = new Vector3(tmp.getX(), tmp.getY(), tmp.getZ());

		final double height = p1.subtract(origin, null).length();
		final int rows = (int) Math.ceil(height / step);
		final int cols = (int) Math.ceil(p2.subtract(origin, null).length() / step);
		double[][] solar = onMesh.get(drawMesh);
		if (solar == null) {
			solar = new double[roundToPowerOfTwo(rows)][roundToPowerOfTwo(cols)];
			onMesh.put(drawMesh, solar);
		}

		if (textureCoordsAlreadyComputed.get(drawMesh) == null) {
			final ReadOnlyVector2 originXY = new Vector2(minX, minY);
			final ReadOnlyVector2 uXY = new Vector2(maxX - minX, 0).normalizeLocal();
			final ReadOnlyVector2 vXY = new Vector2(0, maxY - minY).normalizeLocal();
			for (int row = 0; row < solar.length; row++)
				for (int col = 0; col < solar[0].length; col++) {
					if (row >= rows || col >= cols)
						solar[row][col] = -1;
					else {
						final ReadOnlyVector2 p = originXY.add(uXY.multiply(col * step, null), null).add(vXY.multiply(row * step, null), null);
						boolean isInside = false;
						for (int i = 0; i < points.size(); i += 3) {
							if (Util.isPointInsideTriangle(p, points.get(i), points.get(i + 1), points.get(i + 2))) {
								isInside = true;
								break;
							}
						}
						if (!isInside)
							solar[row][col] = -1;
					}
				}
		}

		final ReadOnlyVector3 u = p2.subtract(origin, null).normalizeLocal();
		final ReadOnlyVector3 v = p1.subtract(origin, null).normalizeLocal();
		final double dot = normal.dot(directionTowardSun);
		for (int col = 0; col < cols; col++) {
			final ReadOnlyVector3 pU = u.multiply(step / 2.0 + col * step, null).addLocal(origin);
			final double w = (col == cols - 1) ? p2.distance(pU) : step;
			for (int row = 0; row < rows; row++) {
				if (EnergyPanel.getInstance().isComputeRequest())
					throw new CancellationException();
				if (solar[row][col] == -1)
					continue;
				final ReadOnlyVector3 p = v.multiply(step / 2.0 + row * step, null).addLocal(pU);
				final double h;
				if (row == rows - 1)
					h = height - (row * step);
				else
					h = step;
				final Ray3 pickRay = new Ray3(p.add(offset, null), directionTowardSun);
				final PickResults pickResults = new PrimitivePickResults();
//				final PickResults pickResults = new BoundingPickResults();
				boolean collision = false;
				for (final Spatial spatial : collidables)
					if (spatial != collisionMesh) {
						PickingUtil.findPick(spatial, pickRay, pickResults, false);						
						if (pickResults.getNumber() != 0) {
							collision = true;
							break;
						}
					}
				if (!collision)
					for (final HousePart part : Scene.getInstance().getParts())
						if (part instanceof Tree)
							if (((Tree) part).intersects(pickRay)) {
								collision = true;
								break;
							}
					
				if (!collision) {
					solar[row][col] += dot / airMass;
					final double annotationScale = Scene.getInstance().getAnnotationScale();
					housePart.getSolarPotential()[minute / MINUTE_STEP] += dot / airMass * w * h * annotationScale * annotationScale / 60 * MINUTE_STEP;
				}
			}
		}

		if (textureCoordsAlreadyComputed.get(drawMesh) == null && !(housePart instanceof Window)) {
			updateTextureCoords(drawMesh, origin, u, v, rows, cols);
			textureCoordsAlreadyComputed.put(drawMesh, Boolean.TRUE);
		}
	}

	private void updateTextureCoords(final Mesh drawMesh, final ReadOnlyVector3 origin, final ReadOnlyVector3 uDir, final ReadOnlyVector3 vDir, final int rows, final int cols) {
		final ReadOnlyVector3 o = origin;
		final ReadOnlyVector3 u = uDir.multiply(roundToPowerOfTwo(cols) * getStep(), null);
		final ReadOnlyVector3 v = vDir.multiply(roundToPowerOfTwo(rows) * getStep(), null);
		final FloatBuffer vertexBuffer = drawMesh.getMeshData().getVertexBuffer();
		final FloatBuffer textureBuffer = drawMesh.getMeshData().getTextureBuffer(0);
		vertexBuffer.rewind();
		textureBuffer.rewind();
		while (vertexBuffer.hasRemaining()) {
			final ReadOnlyVector3 p = drawMesh.localToWorld(new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get()), null);
			final Vector3 uP = Util.closestPoint(o, u, p, v.negate(null));
			final float uScale = (float) (uP.distance(o) / u.length());
			final Vector3 vP = Util.closestPoint(o, v, p, u.negate(null));
			final float vScale = (float) (vP.distance(o) / v.length());
			textureBuffer.put(uScale).put(vScale);
		}
	}

	private void computeOnLand(final ReadOnlyVector3 directionTowardSun) {
		final double step = this.step * 4;
		final int rows = (int) (256 / step);
		final int cols = rows;
		if (onLand == null)
			onLand = new double[rows][cols];
		final Vector3 p = new Vector3();
		for (int col = 0; col < cols; col++) {
			p.setX((col - cols / 2) * step + step / 2.0);
			for (int row = 0; row < rows; row++) {
				if (EnergyPanel.getInstance().isComputeRequest())
					throw new CancellationException();
				p.setY((row - rows / 2) * step + step / 2.0);
				final Ray3 pickRay = new Ray3(p, directionTowardSun);
				final PickResults pickResults = new PrimitivePickResults();
				for (final Spatial spatial : collidables)
					PickingUtil.findPick(spatial, pickRay, pickResults, false);
				if (pickResults.getNumber() == 0)
					onLand[row][col] += directionTowardSun.dot(Vector3.UNIT_Z);
			}
		}
	}

	private void updateValueOnAllHouses() {
		applyTexture(SceneManager.getInstance().getSolarLand(), onLand, maxValue);
		for (final HousePart part : Scene.getInstance().getParts())
			if (part instanceof Wall || part instanceof Window || part instanceof SolarPanel)
				applyTexture(part.getMesh(), onMesh.get(part.getMesh()), maxValue);
			else if (part instanceof Roof)
				for (final Spatial roofPart : ((Roof) part).getRoofPartsRoot().getChildren()) {
					final Mesh mesh = (Mesh) ((Node) roofPart).getChild(0);
					applyTexture(mesh, onMesh.get(mesh), maxValue);
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
							if (houseChild.isWall() || houseChild.isDoor() || houseChild.isWindow() || houseChild.isRoof())								
								heatLoss[i] += houseChild.getHeatLoss()[i];								
							if (houseChild.isWindow())
								passiveSolar[i] += houseChild.getSolarPotential()[i];
							else if (houseChild.isSolarPanel())
								photovoltaic[i] += houseChild.getSolarPotential()[i];
						}
					}
				
				double heatingTotal = 0.0;
				double coolingTotal = 0.0;
				double passiveSolarTotal = 0.0;
				double photovoltaicTotal = 0.0;
				double photovoltaicLeftover = 0.0;
				for (int i = 0; i < heatLoss.length; i++) {
//					if (Heliodon.getInstance().isVisible()) {
						heatLoss[i] -= passiveSolar[i];
						if (Math.abs(heatLoss[i]) > photovoltaic[i])
							heatLoss[i] = Math.signum(heatLoss[i]) * (Math.abs(heatLoss[i]) - photovoltaic[i]);
						else {
							photovoltaicLeftover += photovoltaic[i] - Math.abs(heatLoss[i]);
							heatLoss[i] = 0.0;
						}
//					}
					if (heatLoss[i] > 0)
						heatingTotal += heatLoss[i];
					else
						coolingTotal += -heatLoss[i];
					passiveSolarTotal += passiveSolar[i];
					photovoltaicTotal += photovoltaic[i];
				}
				
				foundation.setSolarPotentialToday(solarPotentialTotal);
				foundation.setSolarLabelValue(solarPotentialTotal);
				foundation.setPassiveSolarToday(passiveSolarTotal);
				foundation.setPhotovoltaicToday(photovoltaicTotal);
				foundation.setHeatingToday(heatingTotal);
				foundation.setCoolingToday(coolingTotal);
				foundation.setTotalEnergyToday(heatingTotal + coolingTotal - photovoltaicLeftover);
			}
		}

		SceneManager.getInstance().refresh();
	}
	
	private void applyTexture(final Mesh mesh, final double[][] solarData, final long maxValue) {
		if (solarData != null)
			fillBlanksWithNeighboringValues(solarData);

		final int rows;
		final int cols;
		if (solarData == null) {
			rows = cols = 1;
		} else {
			rows = solarData.length;
			cols = solarData[0].length;
		}

		final ByteBuffer data = BufferUtils.createByteBuffer(cols * rows * 3);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				final double value = solarData == null ? 0 : solarData[row][col];
				final ColorRGBA color = computeColor(value, maxValue);
				data.put((byte) (color.getRed() * 255)).put((byte) (color.getGreen() * 255)).put((byte) (color.getBlue() * 255));
			}
		}

		final Image image = new Image(ImageDataFormat.RGB, PixelDataType.UnsignedByte, cols, rows, data, null);
		final Texture2D texture = new Texture2D();
		texture.setTextureKey(TextureKey.getRTTKey(MinificationFilter.NearestNeighborNoMipMaps));
		texture.setImage(image);
		final TextureState textureState = new TextureState();
		textureState.setTexture(texture);
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

	public void setStep(final double solarStep) {
		this.step = solarStep;
	}

	public double getStep() {
		return step;
	}	

}
