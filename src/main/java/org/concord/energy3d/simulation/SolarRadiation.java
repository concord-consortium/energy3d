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
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.Solar;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Trackable;
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
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.util.TextureKey;
import com.ardor3d.util.geom.BufferUtils;

public class SolarRadiation {

	public final static double SOLAR_CONSTANT = 1.361; // in kW, see http://en.wikipedia.org/wiki/Solar_constant
	public final static int MINUTES_OF_DAY = 1440;

	// public final static double[] ASHRAE_C = new double[] { 0.058, 0.060, 0.071, 0.097, 0.121, 0.134, 0.136, 0.122, 0.092, 0.073, 0.063, 0.057 }; // http://www.physics.arizona.edu/~cronin/Solar/References/Irradiance%20Models%20and%20Data/WOC01.pdf
	public final static double[] ASHRAE_C = new double[] { 0.103, 0.104, 0.109, 0.120, 0.130, 0.137, 0.138, 0.134, 0.121, 0.111, 0.106, 0.103 }; // revised C coefficients found from Iqbal's book

	public final static int AIR_MASS_NONE = -1;
	public final static int AIR_MASS_KASTEN_YOUNG = 0;
	public final static int AIR_MASS_SPHERE_MODEL = 1;

	private static SolarRadiation instance = new SolarRadiation();
	private final Map<Mesh, MeshDataStore> onMesh = new HashMap<Mesh, MeshDataStore>();
	private final List<Spatial> collidables = new ArrayList<Spatial>();
	private final Map<Spatial, HousePart> collidablesToParts = new HashMap<Spatial, HousePart>();
	private long maxValue;
	private int airMassSelection = AIR_MASS_SPHERE_MODEL;
	private double peakRadiation;
	private double[] dailyAirTemperatures; // daily air temperature high and low
	private double[][] cellOutputs; // cache the intermediate calculated solar radiation on the solar cells of a solar panel or rack

	private class MeshDataStore { // renamed this to avoid name conflict with MeshData
		public Vector3 p0;
		public Vector3 p1;
		public Vector3 p2;
		public Vector3 u;
		public Vector3 v;
		public int rows;
		public int cols;
		public double[][] dailySolarIntensity; // store the solar energy intensity distribution on this mesh
		public double[] solarPotential; // store the total solar energy that falls onto this mesh -- this applies to only roof meshes and imported meshes (other parts that have a single mesh uses its own array)
		public double[] heatLoss;
	}

	public static SolarRadiation getInstance() {
		return instance;
	}

	public void compute() {
		System.out.println("Compute solar radiation...");
		initCollidables();
		onMesh.clear();
		final int n = Math.round(MINUTES_OF_DAY / (float) Scene.getInstance().getTimeStep());
		for (final HousePart part : Scene.getInstance().getParts()) {
			part.setSolarPotential(new double[n]);
		}
		computeToday();
		if (Scene.getInstance().getAlwaysComputeHeatFluxVectors()) {
			for (final HousePart part : Scene.getInstance().getParts()) {
				if (part.isDrawCompleted()) {
					part.drawHeatFlux();
				}
			}
		}
	}

	public double[] getSolarPotential(final Mesh mesh) {
		final MeshDataStore md = onMesh.get(mesh);
		return md == null ? null : md.solarPotential;
	}

	public double[] getHeatLoss(final Mesh mesh) {
		final MeshDataStore md = onMesh.get(mesh);
		return md == null ? null : md.heatLoss;
	}

	private void initCollidables() {
		collidables.clear();
		collidablesToParts.clear();
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof SolarPanel || part instanceof Mirror || part instanceof Tree || part instanceof Sensor || part instanceof Window) {
				final Spatial s = part.getRadiationCollisionSpatial();
				collidables.add(s);
				collidablesToParts.put(s, part);
			} else if (part instanceof ParabolicTrough) {
				final Spatial s = part.getRadiationCollisionSpatial();
				collidables.add(s);
				collidablesToParts.put(s, part);
			} else if (part instanceof Rack) {
				final Rack rack = (Rack) part;
				if (rack.isMonolithic()) {
					final Spatial s = part.getRadiationCollisionSpatial();
					collidables.add(s);
					collidablesToParts.put(s, rack);
				}
			} else if (part instanceof Foundation) {
				final Foundation foundation = (Foundation) part;
				for (int i = 0; i < 4; i++) {
					final Spatial s = foundation.getRadiationCollisionSpatial(i);
					collidables.add(s);
					collidablesToParts.put(s, foundation);
				}
				final List<Node> importedNodes = foundation.getImportedNodes();
				if (importedNodes != null) {
					for (final Node node : importedNodes) {
						for (final Spatial s : node.getChildren()) {
							collidables.add(s);
							collidablesToParts.put(s, foundation);
						}
					}
				}
			} else if (part instanceof Wall) {
				final Wall wall = (Wall) part;
				if (wall.getType() == Wall.SOLID_WALL) {
					final Spatial s = part.getRadiationCollisionSpatial();
					collidables.add(s);
					collidablesToParts.put(s, wall);
				}
			} else if (part instanceof Roof) {
				final Roof roof = (Roof) part;
				for (final Spatial roofPart : roof.getRoofPartsRoot().getChildren()) {
					if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
						final Spatial s = ((Node) roofPart).getChild(6);
						collidables.add(s);
						collidablesToParts.put(s, roof);
					}
				}
			}
		}
	}

	private void computeToday() {

		// save current calendar for restoring at the end of this calculation
		final Calendar today = (Calendar) Heliodon.getInstance().getCalendar().clone();
		final int hourOfDay = today.get(Calendar.HOUR_OF_DAY);
		final int minuteOfHour = today.get(Calendar.MINUTE);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.HOUR_OF_DAY, 0);
		final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
		dailyAirTemperatures = Weather.computeOutsideTemperature(today, city);

		final int timeStep = Scene.getInstance().getTimeStep();
		final ReadOnlyVector3[] sunLocations = new ReadOnlyVector3[SolarRadiation.MINUTES_OF_DAY / timeStep];
		int totalSteps = 0;
		for (int minute = 0; minute < SolarRadiation.MINUTES_OF_DAY; minute += timeStep) {
			final ReadOnlyVector3 sunLocation = Heliodon.getInstance().computeSunLocation(today).normalize(null);
			sunLocations[minute / timeStep] = sunLocation;
			if (sunLocation.getZ() > 0) {
				totalSteps++;
			}
			today.add(Calendar.MINUTE, timeStep);
		}
		totalSteps -= 2;
		final double dayLength = totalSteps * timeStep / 60.0;
		int step = 1;
		setupImportedMeshes();
		// for (int minute = MINUTES_OF_DAY / 2; minute < MINUTES_OF_DAY / 2 + timeStep; minute += timeStep) { // test for 12 pm for comparison with shadow
		for (int minute = 0; minute < MINUTES_OF_DAY; minute += timeStep) {
			final ReadOnlyVector3 sunLocation = sunLocations[minute / timeStep];
			if (sunLocation.getZ() > 0) {
				final ReadOnlyVector3 directionTowardSun = sunLocation.normalize(null);
				calculatePeakRadiation(directionTowardSun, dayLength);
				for (final HousePart part : Scene.getInstance().getParts()) {
					if (part.isDrawCompleted()) {
						if (part instanceof Window) {
							computeOnMesh(minute, directionTowardSun, part, part.getRadiationMesh(), (Mesh) part.getRadiationCollisionSpatial(), part.getNormal());
						} else if (part instanceof Wall) {
							if (((Wall) part).getType() == Wall.SOLID_WALL) {
								computeOnMesh(minute, directionTowardSun, part, part.getRadiationMesh(), (Mesh) part.getRadiationCollisionSpatial(), part.getNormal());
							}
						} else if (part instanceof Foundation) {
							final Foundation foundation = (Foundation) part;
							for (int i = 0; i < 5; i++) {
								final Mesh radiationMesh = foundation.getRadiationMesh(i);
								final ReadOnlyVector3 normal = i == 0 ? part.getNormal() : ((UserData) radiationMesh.getUserData()).getNormal();
								computeOnMesh(minute, directionTowardSun, part, radiationMesh, foundation.getRadiationCollisionSpatial(i), normal);
							}
							if (!Scene.getInstance().getOnlySolarComponentsInSolarMap()) {
								final List<Node> importedNodes = foundation.getImportedNodes();
								if (importedNodes != null) {
									for (final Node node : importedNodes) {
										for (final Spatial s : node.getChildren()) {
											final Mesh m = (Mesh) s;
											computeOnImportedMesh(minute, directionTowardSun, foundation, m);
										}
									}
								}
							}
						} else if (part instanceof Roof) {
							for (final Spatial roofPart : ((Roof) part).getRoofPartsRoot().getChildren()) {
								if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
									final ReadOnlyVector3 faceDirection = (ReadOnlyVector3) roofPart.getUserData();
									final Mesh mesh = (Mesh) ((Node) roofPart).getChild(6);
									computeOnMesh(minute, directionTowardSun, part, mesh, mesh, faceDirection);
								}
							}
						} else if (part instanceof SolarPanel) {
							computeOnSolarPanel(minute, directionTowardSun, (SolarPanel) part);
						} else if (part instanceof Rack) {
							computeOnRack(minute, directionTowardSun, (Rack) part);
						} else if (part instanceof Mirror) {
							computeOnMirror(minute, directionTowardSun, (Mirror) part);
						} else if (part instanceof ParabolicTrough) {
							computeOnParabolicTrough(minute, directionTowardSun, (ParabolicTrough) part);
						} else if (part instanceof Sensor) {
							computeOnSensor(minute, directionTowardSun, (Sensor) part);
						}
					}
				}
				computeOnLand(directionTowardSun);
				EnergyPanel.getInstance().progress((int) Math.round(100.0 * step / totalSteps));
				step++;
			}
		}
		maxValue = Math.round((MINUTES_OF_DAY / timeStep + 1.0) * (1 - 0.01 * Scene.getInstance().getSolarHeatMapColorContrast()));

		// If driven by heliostat or solar tracker, the heliodon's calendar has been changed. Restore the time now.
		Heliodon.getInstance().getCalendar().set(Calendar.HOUR_OF_DAY, hourOfDay);
		Heliodon.getInstance().getCalendar().set(Calendar.MINUTE, minuteOfHour);
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Mirror) {
				final Mirror m = (Mirror) part;
				if (m.getHeliostatTarget() != null) {
					m.draw();
				}
			} else if (part instanceof ParabolicTrough) {
				final ParabolicTrough pt = (ParabolicTrough) part;
				pt.draw();
			} else if (part instanceof SolarPanel) {
				final SolarPanel sp = (SolarPanel) part;
				if (sp.getTracker() != Trackable.NO_TRACKER) {
					sp.draw();
				}
			} else if (part instanceof Rack) {
				final Rack rack = (Rack) part;
				if (rack.getTracker() != Trackable.NO_TRACKER) {
					rack.draw();
				}
			}
		}

	}

	private void computeOnLand(final ReadOnlyVector3 directionTowardSun) {
		final double indirectRadiation = calculateDiffuseAndReflectedRadiation(directionTowardSun, Vector3.UNIT_Z);
		final double totalRadiation = calculateDirectRadiation(directionTowardSun, Vector3.UNIT_Z) + indirectRadiation;
		final double step = Scene.getInstance().getSolarStep() * 4;
		final int rows = (int) (256 / step);
		final int cols = rows;
		MeshDataStore data = onMesh.get(SceneManager.getInstance().getSolarLand());
		if (data == null) {
			data = new MeshDataStore();
			data.dailySolarIntensity = new double[rows][cols];
			onMesh.put(SceneManager.getInstance().getSolarLand(), data);
		}
		final Vector3 p = new Vector3();
		final double absorption = 1 - Scene.getInstance().getGround().getAdjustedAlbedo(Heliodon.getInstance().getCalendar().get(Calendar.MONTH));
		for (int col = 0; col < cols; col++) {
			p.setX((col - cols / 2) * step + step / 2.0);
			for (int row = 0; row < rows; row++) {
				if (EnergyPanel.getInstance().isCancelled()) {
					throw new CancellationException();
				}
				p.setY((row - rows / 2) * step + step / 2.0);
				final Ray3 pickRay = new Ray3(p, directionTowardSun);
				final PickResults pickResults = new PrimitivePickResults();
				for (final Spatial spatial : collidables) {
					PickingUtil.findPick(spatial, pickRay, pickResults, false);
					if (pickResults.getNumber() != 0) {
						break;
					}
				}
				if (pickResults.getNumber() == 0) {
					data.dailySolarIntensity[row][col] += Scene.getInstance().getOnlyAbsorptionInSolarMap() ? totalRadiation * absorption : totalRadiation;
				} else { // if shaded, it still receives indirect radiation
					data.dailySolarIntensity[row][col] += Scene.getInstance().getOnlyAbsorptionInSolarMap() ? indirectRadiation * absorption : indirectRadiation;
				}
			}
		}
	}

	// Formula from http://en.wikipedia.org/wiki/Air_mass_(solar_energy)#Solar_intensity
	private void computeOnMesh(final int minute, final ReadOnlyVector3 directionTowardSun, final HousePart housePart, final Mesh drawMesh, final Mesh collisionMesh, final ReadOnlyVector3 normal) {

		if (Scene.getInstance().getOnlySolarComponentsInSolarMap()) {
			return;
		}

		MeshDataStore data = onMesh.get(drawMesh);
		if (data == null) {
			data = initMeshTextureData(drawMesh, collisionMesh, normal, !(housePart instanceof Window));
		}

		/* needed in order to prevent picking collision with neighboring wall at wall edge (seem 0.1 is too small, 0.5 is about right) */
		final ReadOnlyVector3 offset = directionTowardSun.multiply(0.5, null);

		final double dot = normal.dot(directionTowardSun);
		final double directRadiation = dot > 0 ? calculateDirectRadiation(directionTowardSun, normal) : 0;
		final double indirectRadiation = calculateDiffuseAndReflectedRadiation(directionTowardSun, normal);

		final int timeStep = Scene.getInstance().getTimeStep();
		final double solarStep = Scene.getInstance().getSolarStep();
		final double annotationScale = Scene.getInstance().getAnnotationScale();
		final double scaleFactor = annotationScale * annotationScale / 60 * timeStep;
		final float absorption = housePart instanceof Window ? 1 : 1 - housePart.getAlbedo(); // a window itself doesn't really absorb solar energy, but it passes the energy into the house to be absorbed

		if (housePart instanceof Roof) { // for now, only store this for roofs that have different meshes
			if (data.solarPotential == null) {
				data.solarPotential = new double[MINUTES_OF_DAY / timeStep];
			}
			if (data.heatLoss == null) {
				data.heatLoss = new double[MINUTES_OF_DAY / timeStep];
			}
		}

		for (int col = 0; col < data.cols; col++) {
			final double w = col == data.cols - 1 ? data.p2.distance(data.u.multiply(col * solarStep, null).addLocal(data.p0)) : solarStep;
			final ReadOnlyVector3 pU = data.u.multiply(col * solarStep + 0.5 * w, null).addLocal(data.p0);
			for (int row = 0; row < data.rows; row++) {
				if (EnergyPanel.getInstance().isCancelled()) {
					throw new CancellationException();
				}
				if (data.dailySolarIntensity[row][col] == -1) {
					continue;
				}
				final double h = row == data.rows - 1 ? data.p1.distance(data.p0) - row * solarStep : solarStep;
				final ReadOnlyVector3 p = data.v.multiply(row * solarStep + 0.5 * h, null).addLocal(pU).add(offset, null);
				final Ray3 pickRay = new Ray3(p, directionTowardSun);
				final PickResults pickResults = new PrimitivePickResults();
				double radiation = indirectRadiation; // assuming that indirect (ambient or diffuse) radiation can always reach a grid point
				final double scaledArea = w * h * scaleFactor;
				if (dot > 0) {
					for (final Spatial spatial : collidables) {
						if (EnergyPanel.getInstance().isCancelled()) {
							throw new CancellationException();
						}
						if (spatial != collisionMesh) {
							PickingUtil.findPick(spatial, pickRay, pickResults, false);
							if (pickResults.getNumber() != 0) {
								if (housePart instanceof Foundation) { // at this point, we only show radiation heat map on the first floor
									final HousePart collidableOwner = collidablesToParts.get(spatial);
									if (collidableOwner instanceof Window) {
										radiation += directRadiation * ((Window) collidableOwner).getSolarHeatGainCoefficient();
									}
								}
								break;
							}
						}
					}
					if (pickResults.getNumber() == 0) {
						radiation += directRadiation;
					}
				}
				data.dailySolarIntensity[row][col] += Scene.getInstance().getOnlyAbsorptionInSolarMap() ? absorption * radiation : radiation;
				if (data.solarPotential != null) {
					data.solarPotential[minute / timeStep] += radiation * scaledArea;
				}
				if (!(housePart instanceof Foundation)) { // exclude radiation on foundation
					housePart.getSolarPotential()[minute / timeStep] += radiation * scaledArea;
				}
			}
		}

	}

	private void setupImportedMeshes() {
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Foundation) {
				final Foundation foundation = (Foundation) part;
				final boolean nonZeroAz = !Util.isZero(foundation.getAzimuth());
				final List<Node> importedNodes = foundation.getImportedNodes();
				if (importedNodes != null) {
					for (final Node node : importedNodes) {
						for (final Spatial s : node.getChildren()) {
							final Mesh m = (Mesh) s;
							final UserData ud = (UserData) m.getUserData();
							ReadOnlyVector3 normal = ud.getNormal();
							if (nonZeroAz) { // if the foundation is rotated, rotate the imported meshes, too, but this doesn't alter their original normals
								ud.setRotatedNormal(node.getRotation().applyPost(normal, null)); // this must be recalculated in case the foundation has been rotated after loading
								normal = ud.getRotatedNormal();
							}
							MeshDataStore data = onMesh.get(m);
							if (data == null) { // initialize mesh solar data and texture
								data = initMeshTextureData(m, m, normal, true);
								data.solarPotential = new double[MINUTES_OF_DAY / Scene.getInstance().getTimeStep()];
							}
						}
					}
				}
			}
		}
	}

	private void computeOnImportedMesh(final int minute, final ReadOnlyVector3 directionTowardSun, final Foundation foundation, final Mesh mesh) {

		final UserData userData = (UserData) mesh.getUserData();
		if (!userData.isReachable()) {
			return;
		}

		final ReadOnlyVector3 normal = userData.getRotatedNormal() == null ? userData.getNormal() : userData.getRotatedNormal();
		final MeshDataStore data = onMesh.get(mesh);
		final int timeStep = Scene.getInstance().getTimeStep();
		final int iMinute = minute / timeStep;

		final double dot = normal.dot(directionTowardSun);
		final double directRadiation = dot > 0 ? calculateDirectRadiation(directionTowardSun, normal) : 0;
		final double indirectRadiation = calculateDiffuseAndReflectedRadiation(directionTowardSun, normal);

		final double solarStep = Scene.getInstance().getSolarStep();
		final double annotationScale = Scene.getInstance().getAnnotationScale();
		final double scaleFactor = annotationScale * annotationScale / 60 * timeStep;
		final float absorption = 1 - foundation.getAlbedo();

		for (int col = 0; col < data.cols; col++) {
			// final double w = col == data.cols - 1 ? data.p2.distance(data.u.multiply(col * solarStep, null).addLocal(data.p0)) : solarStep;
			final double w = col == data.cols - 1 ? data.p2.distance(data.p0) - col * solarStep : solarStep;
			final ReadOnlyVector3 pU = data.u.multiply(col * solarStep + 0.5 * w, null).addLocal(data.p0);
			for (int row = 0; row < data.rows; row++) {
				if (EnergyPanel.getInstance().isCancelled()) {
					throw new CancellationException();
				}
				if (data.dailySolarIntensity[row][col] == -1) {
					continue;
				}
				final double h = row == data.rows - 1 ? data.p1.distance(data.p0) - row * solarStep : solarStep;
				final ReadOnlyVector3 p = data.v.multiply(row * solarStep + 0.5 * h, null).addLocal(pU); // cannot do offset as in computeOnMesh
				final Ray3 pickRay = new Ray3(p, directionTowardSun);
				final PickResults pickResults = new PrimitivePickResults();
				double radiation = indirectRadiation; // assuming that indirect (ambient or diffuse) radiation can always reach a grid point
				final double scaledArea = w * h * scaleFactor;
				if (dot > 0) {
					for (final Spatial spatial : collidables) {
						if (EnergyPanel.getInstance().isCancelled()) {
							throw new CancellationException();
						}
						if (spatial != mesh) {
							PickingUtil.findPick(spatial, pickRay, pickResults, false);
							if (pickResults.getNumber() != 0) {
								break;
							}
						}
					}
					if (pickResults.getNumber() == 0) {
						radiation += directRadiation;
					}
				}
				data.dailySolarIntensity[row][col] += Scene.getInstance().getOnlyAbsorptionInSolarMap() ? absorption * radiation : radiation;
				if (data.solarPotential != null) {
					data.solarPotential[iMinute] += radiation * scaledArea;
				}
				foundation.getSolarPotential()[iMinute] += radiation * scaledArea; // sum all the solar energy up over all meshes and store in the foundation's solar potential array
			}
		}

	}

	// the mesh is a parabolic surface
	private void computeOnParabolicTrough(final int minute, final ReadOnlyVector3 directionTowardSun, final ParabolicTrough trough) {

		final int nAxis = trough.getNSectionAxis();
		final int nPara = trough.getNSectionParabola();
		final Calendar calendar = Heliodon.getInstance().getCalendar();
		calendar.set(Calendar.HOUR_OF_DAY, (int) ((double) minute / (double) SolarRadiation.MINUTES_OF_DAY * 24.0));
		calendar.set(Calendar.MINUTE, minute % 60);
		trough.draw();
		final ReadOnlyVector3 normal = trough.getNormal();
		if (normal == null) {
			throw new RuntimeException("Normal is null");
		}
		// nx*ny*60: nx*ny is to get the unit cell area of the nx*ny grid; 60 is to convert the unit of timeStep from minute to kWh
		final double a = trough.getTroughWidth() * trough.getTroughLength() * Scene.getInstance().getTimeStep() / (nAxis * nPara * 60.0);
		final Mesh mesh = trough.getRadiationMesh();
		MeshDataStore data = onMesh.get(mesh);
		if (data == null) {
			data = initMeshTextureDataOnRectangle(mesh, nAxis, nPara); // axis is row and parabola is column
		}

		final double dot = normal.dot(directionTowardSun);
		double directRadiation = 0;
		if (dot > 0) {
			directRadiation += calculateDirectRadiation(directionTowardSun, normal);
		}

		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		final int j = vertexBuffer.limit() / 2; // number of vertex coordinates on each end
		final Vector3 p0 = new Vector3(vertexBuffer.get(0), vertexBuffer.get(1), vertexBuffer.get(2)); // (0, 0)
		final Vector3 p1 = new Vector3(vertexBuffer.get(j - 3), vertexBuffer.get(j - 2), vertexBuffer.get(j - 1)); // (1, 0)
		final Vector3 p2 = new Vector3(vertexBuffer.get(j), vertexBuffer.get(j + 1), vertexBuffer.get(j + 2)); // (0, 1)
		// final Vector3 q0 = mesh.localToWorld(p0, null);
		// final Vector3 q1 = mesh.localToWorld(p1, null);
		// final Vector3 q2 = mesh.localToWorld(p2, null);
		// System.out.println("***" + q0.distance(q1) * Scene.getInstance().getAnnotationScale() + "," + q0.distance(q2) * Scene.getInstance().getAnnotationScale());
		final Vector3 u = p1.subtract(p0, null).normalizeLocal(); // this is perpendicular to the direction of the cylinder axis (nPara)
		final Vector3 v = p2.subtract(p0, null).normalizeLocal(); // this is parallel to the direction of the cylinder axis (nAxis)
		final double xSpacing = p1.distance(p0) / nPara;
		final double ySpacing = p2.distance(p0) / nAxis;

		final int iMinute = minute / Scene.getInstance().getTimeStep();
		for (int x = 0; x < nPara; x++) {
			for (int y = 0; y < nAxis; y++) {
				if (EnergyPanel.getInstance().isCancelled()) {
					throw new CancellationException();
				}
				final Vector3 u2 = u.multiply(xSpacing * (x + 0.5), null);
				final Vector3 v2 = v.multiply(ySpacing * (y + 0.5), null);
				final Vector3 q = p0.add(v2, null).addLocal(u2); // on the plane of the mouth of the parabolic trough
				final ReadOnlyVector3 p = mesh.localToWorld(q, null);
				final Ray3 pickRay = new Ray3(p, directionTowardSun);
				if (dot > 0) {
					final PickResults pickResults = new PrimitivePickResults();
					for (final Spatial spatial : collidables) {
						if (spatial != mesh) {
							PickingUtil.findPick(spatial, pickRay, pickResults, false);
							if (pickResults.getNumber() != 0) {
								break;
							}
						}
					}
					if (pickResults.getNumber() == 0) {
						// for heat map generation
						data.dailySolarIntensity[y][x] += directRadiation;
						trough.getSolarPotential()[iMinute] += directRadiation * a; // sum all the solar energy up over all meshes and store in the foundation's solar potential array
					}
				}
			}
		}

	}

	// unlike PV solar panels, no indirect (ambient or diffuse) radiation should be included in mirror reflection calculation
	private void computeOnMirror(final int minute, final ReadOnlyVector3 directionTowardSun, final Mirror mirror) {

		final int nx = Scene.getInstance().getMirrorNx();
		final int ny = Scene.getInstance().getMirrorNy();
		final Foundation target = mirror.getHeliostatTarget();
		if (target != null) {
			final Calendar calendar = Heliodon.getInstance().getCalendar();
			calendar.set(Calendar.HOUR_OF_DAY, (int) ((double) minute / (double) SolarRadiation.MINUTES_OF_DAY * 24.0));
			calendar.set(Calendar.MINUTE, minute % 60);
			mirror.draw();
		}
		// nx*ny*60: nx*ny is to get the unit cell area of the nx*ny grid; 60 is to convert the unit of timeStep from minute to kWh
		final double a = mirror.getMirrorWidth() * mirror.getMirrorHeight() * Scene.getInstance().getTimeStep() / (nx * ny * 60.0);

		final ReadOnlyVector3 normal = mirror.getNormal();
		if (normal == null) {
			throw new RuntimeException("Normal is null");
		}

		final Mesh mesh = mirror.getRadiationMesh();
		MeshDataStore data = onMesh.get(mesh);
		if (data == null) {
			data = initMeshTextureDataOnRectangle(mesh, nx, ny);
		}

		final ReadOnlyVector3 offset = directionTowardSun.multiply(1, null);

		final double dot = normal.dot(directionTowardSun);
		double directRadiation = 0;
		if (dot > 0) {
			directRadiation += calculateDirectRadiation(directionTowardSun, normal);
		}

		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();

		final Vector3 p0 = new Vector3(vertexBuffer.get(3), vertexBuffer.get(4), vertexBuffer.get(5)); // (0, 0)
		final Vector3 p1 = new Vector3(vertexBuffer.get(6), vertexBuffer.get(7), vertexBuffer.get(8)); // (1, 0)
		final Vector3 p2 = new Vector3(vertexBuffer.get(0), vertexBuffer.get(1), vertexBuffer.get(2)); // (0, 1)
		// final Vector3 q0 = drawMesh.localToWorld(p0, null);
		// final Vector3 q1 = drawMesh.localToWorld(p1, null);
		// final Vector3 q2 = drawMesh.localToWorld(p2, null);
		// System.out.println("***" + q0.distance(q1) * Scene.getInstance().getAnnotationScale() + "," + q0.distance(q2) * Scene.getInstance().getAnnotationScale());
		final Vector3 u = p1.subtract(p0, null).normalizeLocal(); // this is the longer side (supposed to be y)
		final Vector3 v = p2.subtract(p0, null).normalizeLocal(); // this is the shorter side (supposed to be x)
		final double xSpacing = p1.distance(p0) / nx; // x and y must be swapped to have correct heat map texture, because nx represents rows and ny columns as we call initMeshTextureDataOnRectangle(mesh, nx, ny)
		final double ySpacing = p2.distance(p0) / ny;

		final Vector3 receiver = target != null ? target.getSolarReceiverCenter() : null;
		List<Mesh> towerCollisionMeshes = null;
		if (target != null) {
			towerCollisionMeshes = new ArrayList<Mesh>();
			for (final HousePart child : target.getChildren()) {
				towerCollisionMeshes.add((Mesh) child.getRadiationCollisionSpatial());
			}
			final List<Roof> roofs = target.getRoofs();
			if (!roofs.isEmpty()) {
				for (final Roof r : roofs) {
					for (final Spatial roofPart : r.getRoofPartsRoot().getChildren()) {
						towerCollisionMeshes.add((Mesh) ((Node) roofPart).getChild(6));
					}
				}
			}
		}
		final int iMinute = minute / Scene.getInstance().getTimeStep();
		final boolean reflectionMapOnly = Scene.getInstance().getOnlyReflectedEnergyInMirrorSolarMap();
		for (int x = 0; x < nx; x++) {
			for (int y = 0; y < ny; y++) {
				if (EnergyPanel.getInstance().isCancelled()) {
					throw new CancellationException();
				}
				final Vector3 u2 = u.multiply(xSpacing * (x + 0.5), null);
				final Vector3 v2 = v.multiply(ySpacing * (y + 0.5), null);
				final ReadOnlyVector3 p = mesh.getWorldTransform().applyForward(p0.add(v2, null).addLocal(u2)).addLocal(offset);
				final Ray3 pickRay = new Ray3(p, directionTowardSun);
				if (dot > 0) {
					final PickResults pickResults = new PrimitivePickResults();
					for (final Spatial spatial : collidables) {
						if (spatial != mesh) {
							PickingUtil.findPick(spatial, pickRay, pickResults, false);
							if (pickResults.getNumber() != 0) {
								break;
							}
						}
					}
					if (pickResults.getNumber() == 0) {

						// for heat map generation
						if (!reflectionMapOnly) {
							data.dailySolarIntensity[x][y] += directRadiation;
						}

						if (receiver != null) {
							// for concentrated energy calculation
							final Vector3 toReceiver = receiver.subtract(p, null);
							final Ray3 rayToReceiver = new Ray3(p, toReceiver.normalize(null));
							final PickResults pickResultsToReceiver = new PrimitivePickResults();
							for (final Spatial spatial : collidables) {
								if (spatial != mesh) {
									if (towerCollisionMeshes == null || (towerCollisionMeshes != null && !towerCollisionMeshes.contains(spatial))) {
										PickingUtil.findPick(spatial, rayToReceiver, pickResultsToReceiver, false);
										if (pickResultsToReceiver.getNumber() != 0) {
											break;
										}
									}
								}
							}
							if (pickResultsToReceiver.getNumber() == 0) {
								final double r = directRadiation * Atmosphere.getTransmittance(toReceiver.length() * Scene.getInstance().getAnnotationScale() * 0.001, false);
								mirror.getSolarPotential()[iMinute] += r * a;
								if (reflectionMapOnly) {
									data.dailySolarIntensity[x][y] += r;
								}
							}
						}

					}
				}
			}
		}

	}

	private void computeOnSensor(final int minute, final ReadOnlyVector3 directionTowardSun, final Sensor sensor) {

		final int nx = 2, ny = 2;
		// nx*ny*60: nx*ny is to get the unit cell area of the nx*ny grid; 60 is to convert the unit of timeStep from minute to kWh
		final double a = Sensor.WIDTH * Sensor.HEIGHT * Scene.getInstance().getTimeStep() / (nx * ny * 60.0);

		final ReadOnlyVector3 normal = sensor.getNormal();
		if (normal == null) {
			throw new RuntimeException("Normal is null");
		}

		final Mesh drawMesh = sensor.getRadiationMesh();
		final Mesh collisionMesh = (Mesh) sensor.getRadiationCollisionSpatial();
		MeshDataStore data = onMesh.get(drawMesh);
		if (data == null) {
			data = initMeshTextureDataOnRectangle(drawMesh, nx, ny);
		}

		final ReadOnlyVector3 offset = directionTowardSun.multiply(1, null);

		final double dot = normal.dot(directionTowardSun);
		double directRadiation = 0;
		if (dot > 0) {
			directRadiation += calculateDirectRadiation(directionTowardSun, normal);
		}
		final double indirectRadiation = calculateDiffuseAndReflectedRadiation(directionTowardSun, normal);

		final FloatBuffer vertexBuffer = drawMesh.getMeshData().getVertexBuffer();

		final Vector3 p0 = new Vector3(vertexBuffer.get(3), vertexBuffer.get(4), vertexBuffer.get(5)); // (0, 0)
		final Vector3 p1 = new Vector3(vertexBuffer.get(6), vertexBuffer.get(7), vertexBuffer.get(8)); // (1, 0)
		final Vector3 p2 = new Vector3(vertexBuffer.get(0), vertexBuffer.get(1), vertexBuffer.get(2)); // (0, 1)
		final Vector3 u = p1.subtract(p0, null).normalizeLocal(); // this is the longer side (supposed to be y)
		final Vector3 v = p2.subtract(p0, null).normalizeLocal(); // this is the shorter side (supposed to be x)
		final double xSpacing = p1.distance(p0) / nx; // x and y must be swapped to have correct heat map texture, because nx represents rows and ny columns as we call initMeshTextureDataOnRectangle(mesh, nx, ny)
		final double ySpacing = p2.distance(p0) / ny;

		final int iMinute = minute / Scene.getInstance().getTimeStep();
		for (int x = 0; x < nx; x++) {
			for (int y = 0; y < ny; y++) {
				if (EnergyPanel.getInstance().isCancelled()) {
					throw new CancellationException();
				}
				final Vector3 u2 = u.multiply(xSpacing * (x + 0.5), null);
				final Vector3 v2 = v.multiply(ySpacing * (y + 0.5), null);
				final ReadOnlyVector3 p = drawMesh.getWorldTransform().applyForward(p0.add(v2, null).addLocal(u2)).addLocal(offset);
				final Ray3 pickRay = new Ray3(p, directionTowardSun);
				double radiation = indirectRadiation; // assuming that indirect (ambient or diffuse) radiation can always reach a grid point
				if (dot > 0) {
					final PickResults pickResults = new PrimitivePickResults();
					for (final Spatial spatial : collidables) {
						if (spatial != collisionMesh) {
							PickingUtil.findPick(spatial, pickRay, pickResults, false);
							if (pickResults.getNumber() != 0) {
								break;
							}
						}
					}
					if (pickResults.getNumber() == 0) {
						radiation += directRadiation;
					}
				}
				data.dailySolarIntensity[x][y] += radiation;
				sensor.getSolarPotential()[iMinute] += radiation * a;
			}
		}

	}

	// a solar panel typically has 6x10 cells, 6 and 10 are not power of 2 for texture. so we need some special handling here
	private void computeOnSolarPanel(final int minute, final ReadOnlyVector3 directionTowardSun, final SolarPanel panel) {

		if (panel.getTracker() != SolarPanel.NO_TRACKER) {
			final Calendar calendar = Heliodon.getInstance().getCalendar();
			calendar.set(Calendar.HOUR_OF_DAY, (int) ((double) minute / (double) SolarRadiation.MINUTES_OF_DAY * 24.0));
			calendar.set(Calendar.MINUTE, minute % 60);
			panel.draw();
		}

		final ReadOnlyVector3 normal = panel.getNormal();
		if (normal == null) {
			throw new RuntimeException("Normal is null");
		}

		int nx = Scene.getInstance().getSolarPanelNx();
		int ny = Scene.getInstance().getSolarPanelNy();

		final Mesh drawMesh = panel.getRadiationMesh();
		final Mesh collisionMesh = (Mesh) panel.getRadiationCollisionSpatial();
		MeshDataStore data = onMesh.get(drawMesh);
		if (data == null) {
			data = initMeshTextureDataOnRectangle(drawMesh, nx, ny);
		}

		final ReadOnlyVector3 offset = directionTowardSun.multiply(1, null);

		final double dot = normal.dot(directionTowardSun);
		double directRadiation = 0;
		if (dot > 0) {
			directRadiation += calculateDirectRadiation(directionTowardSun, normal);
		}
		final double indirectRadiation = calculateDiffuseAndReflectedRadiation(directionTowardSun, normal);

		final FloatBuffer vertexBuffer = drawMesh.getMeshData().getVertexBuffer();

		final Vector3 p0 = new Vector3(vertexBuffer.get(3), vertexBuffer.get(4), vertexBuffer.get(5)); // (0, 0)
		final Vector3 p1 = new Vector3(vertexBuffer.get(6), vertexBuffer.get(7), vertexBuffer.get(8)); // (1, 0)
		final Vector3 p2 = new Vector3(vertexBuffer.get(0), vertexBuffer.get(1), vertexBuffer.get(2)); // (0, 1)
		final double d10 = p1.distance(p0); // this is the longer side (supposed to be y)
		final double d20 = p2.distance(p0); // this is the shorter side (supposed to be x)
		final Vector3 p10 = p1.subtract(p0, null).normalizeLocal();
		final Vector3 p20 = p2.subtract(p0, null).normalizeLocal();

		// generate the heat map first. this doesn't affect the energy calculation, it just shows the distribution of solar radiation on the panel.

		double xSpacing = d10 / nx; // x and y must be swapped to have correct heat map texture, because nx represents rows and ny columns as we call initMeshTextureDataOnRectangle(mesh, nx, ny)
		double ySpacing = d20 / ny;
		Vector3 u = p10;
		Vector3 v = p20;

		final int iMinute = minute / Scene.getInstance().getTimeStep();
		for (int x = 0; x < nx; x++) {
			for (int y = 0; y < ny; y++) {
				if (EnergyPanel.getInstance().isCancelled()) {
					throw new CancellationException();
				}
				final Vector3 u2 = u.multiply(xSpacing * (x + 0.5), null);
				final Vector3 v2 = v.multiply(ySpacing * (y + 0.5), null);
				final ReadOnlyVector3 p = drawMesh.getWorldTransform().applyForward(p0.add(v2, null).addLocal(u2)).addLocal(offset);
				final Ray3 pickRay = new Ray3(p, directionTowardSun);
				double radiation = indirectRadiation; // assuming that indirect (ambient or diffuse) radiation can always reach a grid point
				if (dot > 0) {
					final PickResults pickResults = new PrimitivePickResults();
					for (final Spatial spatial : collidables) {
						if (spatial != collisionMesh) {
							PickingUtil.findPick(spatial, pickRay, pickResults, false);
							if (pickResults.getNumber() != 0) {
								break;
							}
						}
					}
					if (pickResults.getNumber() == 0) {
						radiation += directRadiation;
					}
				}
				data.dailySolarIntensity[x][y] += radiation;
			}
		}

		// now do the calculation to get the total energy generated by the cells (nx and ny must be the real cell numbers on the panel)

		if (panel.isRotated()) { // landscape
			nx = panel.getNumberOfCellsInY();
			ny = panel.getNumberOfCellsInX();
		} else { // portrait
			nx = panel.getNumberOfCellsInX();
			ny = panel.getNumberOfCellsInY();
		}
		// nx*ny*60: nx*ny is to get the unit cell area of the nx*ny grid; 60 is to convert the unit of timeStep from minute to kWh
		final double a = panel.getPanelWidth() * panel.getPanelHeight() * Scene.getInstance().getTimeStep() / (nx * ny * 60.0);
		xSpacing = d20 / nx; // swap the x and y back to correct order
		ySpacing = d10 / ny;
		u = p20;
		v = p10;
		if (cellOutputs == null || cellOutputs.length != nx || cellOutputs[0].length != ny) {
			cellOutputs = new double[nx][ny];
		}

		// calculate the solar radiation first without worrying about the underlying cell wiring and distributed efficiency
		for (int x = 0; x < nx; x++) {
			for (int y = 0; y < ny; y++) {
				if (EnergyPanel.getInstance().isCancelled()) {
					throw new CancellationException();
				}
				final Vector3 u2 = u.multiply(xSpacing * (x + 0.5), null);
				final Vector3 v2 = v.multiply(ySpacing * (y + 0.5), null);
				final ReadOnlyVector3 p = drawMesh.getWorldTransform().applyForward(p0.add(v2, null).addLocal(u2)).addLocal(offset);
				final Ray3 pickRay = new Ray3(p, directionTowardSun);
				double radiation = indirectRadiation; // assuming that indirect (ambient or diffuse) radiation can always reach a grid point
				if (dot > 0) {
					final PickResults pickResults = new PrimitivePickResults();
					for (final Spatial spatial : collidables) {
						if (spatial != collisionMesh) {
							PickingUtil.findPick(spatial, pickRay, pickResults, false);
							if (pickResults.getNumber() != 0) {
								break;
							}
						}
					}
					if (pickResults.getNumber() == 0) {
						radiation += directRadiation;
					}
				}
				cellOutputs[x][y] = radiation * a;
			}
		}

		final double airTemperature = Weather.getInstance().getOutsideTemperatureAtMinute(dailyAirTemperatures[1], dailyAirTemperatures[0], minute);
		double syseff;
		double output;
		double tcell; // cell temperature
		final double noctFactor = (panel.getNominalOperatingCellTemperature() - 20.0) * 100.0 / (a * 80.0); // Tcell = Tair + (NOCT - 20) / 80 * R, where the unit of R is mW/cm^2

		// now consider cell wiring and distributed efficiency (Nice demo at: https://www.youtube.com/watch?v=UNPJapaZlCU)
		switch (panel.getShadeTolerance()) {
		case SolarPanel.HIGH_SHADE_TOLERANCE: // the most ideal assumption that probably doesn't exist in reality (just keep it here in case someone has a breakthrough in the future)
			for (int x = 0; x < nx; x++) {
				for (int y = 0; y < ny; y++) {
					output = cellOutputs[x][y];
					tcell = airTemperature + output * noctFactor;
					syseff = panel.getSystemEfficiency(tcell);
					panel.getSolarPotential()[iMinute] += output * syseff;
				}
			}
			break;
		case SolarPanel.NO_SHADE_TOLERANCE: // all the cells are connected in a single series, so the total output is (easily) determined by the minimum
			double min = Double.MAX_VALUE;
			for (int x = 0; x < nx; x++) {
				for (int y = 0; y < ny; y++) {
					output = cellOutputs[x][y];
					tcell = airTemperature + output * noctFactor;
					syseff = panel.getSystemEfficiency(tcell);
					output *= syseff;
					if (output < min) {
						min = output;
					}
				}
			}
			panel.getSolarPotential()[iMinute] += min * ny * nx;
			break;
		case SolarPanel.PARTIAL_SHADE_TOLERANCE: // assuming each panel uses a diode bypass to connect two columns of cells
			min = Double.MAX_VALUE;
			if (panel.isRotated()) { // landscape: nx = 10, ny = 6
				for (int y = 0; y < ny; y++) {
					if (y % 2 == 0) { // reset min every two columns of cells
						min = Double.MAX_VALUE;
					}
					for (int x = 0; x < nx; x++) {
						output = cellOutputs[x][y];
						tcell = airTemperature + output * noctFactor;
						syseff = panel.getSystemEfficiency(tcell);
						output *= syseff;
						if (output < min) {
							min = output;
						}
					}
					if (y % 2 == 1) {
						panel.getSolarPotential()[iMinute] += min * nx * 2;
					}
				}
			} else { // portrait: nx = 6, ny = 10
				for (int x = 0; x < nx; x++) {
					if (x % 2 == 0) { // reset min every two columns of cells
						min = Double.MAX_VALUE;
					}
					for (int y = 0; y < ny; y++) {
						output = cellOutputs[x][y];
						tcell = airTemperature + output * noctFactor;
						syseff = panel.getSystemEfficiency(tcell);
						output *= syseff;
						if (output < min) {
							min = output;
						}
					}
					if (x % 2 == 1) {
						panel.getSolarPotential()[iMinute] += min * ny * 2;
					}
				}
			}
			break;
		}

	}

	// TODO: we probably should handle the radiation heat map visualization on the rack using a coarse grid and the energy calculation using a fine grid
	private void computeOnRack(final int minute, final ReadOnlyVector3 directionTowardSun, final Rack rack) {

		if (rack.getTracker() != SolarPanel.NO_TRACKER) {
			final Calendar calendar = Heliodon.getInstance().getCalendar();
			calendar.set(Calendar.HOUR_OF_DAY, (int) ((double) minute / (double) SolarRadiation.MINUTES_OF_DAY * 24.0));
			calendar.set(Calendar.MINUTE, minute % 60);
			rack.draw();
		}

		if (!rack.isMonolithic()) {
			return;
		}

		final ReadOnlyVector3 normal = rack.getNormal();
		if (normal == null) {
			throw new RuntimeException("Normal is null");
		}

		int nx = Scene.getInstance().getRackNx();
		int ny = Scene.getInstance().getRackNy();

		final Mesh drawMesh = rack.getRadiationMesh();
		final Mesh collisionMesh = (Mesh) rack.getRadiationCollisionSpatial();
		MeshDataStore data = onMesh.get(drawMesh);
		if (data == null) {
			data = initMeshTextureDataOnRectangle(drawMesh, nx, ny);
		}

		final ReadOnlyVector3 offset = directionTowardSun.multiply(1, null);

		final double dot = normal.dot(directionTowardSun);
		double directRadiation = 0;
		if (dot > 0) {
			directRadiation += calculateDirectRadiation(directionTowardSun, normal);
		}
		final double indirectRadiation = calculateDiffuseAndReflectedRadiation(directionTowardSun, normal);

		final FloatBuffer vertexBuffer = drawMesh.getMeshData().getVertexBuffer();

		final Vector3 p0 = new Vector3(vertexBuffer.get(3), vertexBuffer.get(4), vertexBuffer.get(5)); // (0, 0)
		final Vector3 p1 = new Vector3(vertexBuffer.get(6), vertexBuffer.get(7), vertexBuffer.get(8)); // (1, 0)
		final Vector3 p2 = new Vector3(vertexBuffer.get(0), vertexBuffer.get(1), vertexBuffer.get(2)); // (0, 1)
		final double d10 = p1.distance(p0); // this is the longer side (supposed to be y)
		final double d20 = p2.distance(p0); // this is the shorter side (supposed to be x)
		final Vector3 p10 = p1.subtract(p0, null).normalizeLocal();
		final Vector3 p20 = p2.subtract(p0, null).normalizeLocal();

		// generate the heat map first. this doesn't affect the energy calculation, it just shows the distribution of solar radiation on the rack.

		double xSpacing = d10 / nx; // x and y must be swapped to have correct heat map texture, because nx represents rows and ny columns as we call initMeshTextureDataOnRectangle(mesh, nx, ny)
		double ySpacing = d20 / ny;
		Vector3 u = p10;
		Vector3 v = p20;

		final int iMinute = minute / Scene.getInstance().getTimeStep();
		for (int x = 0; x < nx; x++) {
			for (int y = 0; y < ny; y++) {
				if (EnergyPanel.getInstance().isCancelled()) {
					throw new CancellationException();
				}
				final Vector3 u2 = u.multiply(xSpacing * (x + 0.5), null);
				final Vector3 v2 = v.multiply(ySpacing * (y + 0.5), null);
				final ReadOnlyVector3 p = drawMesh.getWorldTransform().applyForward(p0.add(v2, null).addLocal(u2)).addLocal(offset);
				final Ray3 pickRay = new Ray3(p, directionTowardSun);
				double radiation = indirectRadiation; // assuming that indirect (ambient or diffuse) radiation can always reach a grid point
				if (dot > 0) {
					final PickResults pickResults = new PrimitivePickResults();
					for (final Spatial spatial : collidables) {
						if (spatial != collisionMesh) {
							PickingUtil.findPick(spatial, pickRay, pickResults, false);
							if (pickResults.getNumber() != 0) {
								break;
							}
						}
					}
					if (pickResults.getNumber() == 0) {
						radiation += directRadiation;
					}
				}
				data.dailySolarIntensity[x][y] += radiation;
			}
		}

		// now do the calculation to get the total energy generated by the cells

		final double airTemperature = Weather.getInstance().getOutsideTemperatureAtMinute(dailyAirTemperatures[1], dailyAirTemperatures[0], minute);
		double syseff; // system efficiency
		double output; // output at a cell center
		double tcell; // cell temperature
		final SolarPanel panel = rack.getSolarPanel();

		if (Scene.getInstance().isRackModelExact()) { // exactly model each solar cell on each solar panel

			final int[] rc = rack.getSolarPanelRowAndColumnNumbers();
			final int nxPanels = rc[0]; // numbers of solar panels in x and y directions
			final int nyPanels = rc[1];
			int nxCells, nyCells; // numbers of solar cells on each panel in x and y directions
			if (panel.isRotated()) {
				nxCells = panel.getNumberOfCellsInY();
				nyCells = panel.getNumberOfCellsInX();
			} else {
				nxCells = panel.getNumberOfCellsInX();
				nyCells = panel.getNumberOfCellsInY();
			}
			nx = nxCells * rc[0];
			ny = nyCells * rc[1];
			// get the area of a solar cell. 60 converts the unit of timeStep from minute to kWh
			final double a = panel.getPanelWidth() * panel.getPanelHeight() * Scene.getInstance().getTimeStep() / (panel.getNumberOfCellsInX() * panel.getNumberOfCellsInY() * 60.0);
			xSpacing = d20 / nx; // swap the x and y back to correct order
			ySpacing = d10 / ny;
			u = p20;
			v = p10;
			if (cellOutputs == null || cellOutputs.length != nx || cellOutputs[0].length != ny) {
				cellOutputs = new double[nx][ny];
			}

			// calculate the solar radiation first without worrying about the underlying cell wiring and distributed efficiency
			for (int x = 0; x < nx; x++) {
				for (int y = 0; y < ny; y++) {
					if (EnergyPanel.getInstance().isCancelled()) {
						throw new CancellationException();
					}
					final Vector3 u2 = u.multiply(xSpacing * (x + 0.5), null);
					final Vector3 v2 = v.multiply(ySpacing * (y + 0.5), null);
					final ReadOnlyVector3 p = drawMesh.getWorldTransform().applyForward(p0.add(v2, null).addLocal(u2)).addLocal(offset);
					final Ray3 pickRay = new Ray3(p, directionTowardSun);
					double radiation = indirectRadiation; // assuming that indirect (ambient or diffuse) radiation can always reach a grid point
					if (dot > 0) {
						final PickResults pickResults = new PrimitivePickResults();
						for (final Spatial spatial : collidables) {
							if (spatial != collisionMesh) {
								PickingUtil.findPick(spatial, pickRay, pickResults, false);
								if (pickResults.getNumber() != 0) {
									break;
								}
							}
						}
						if (pickResults.getNumber() == 0) {
							radiation += directRadiation;
						}
					}
					cellOutputs[x][y] = radiation * a;
				}
			}

			final double noctFactor = (panel.getNominalOperatingCellTemperature() - 20.0) * 100.0 / (a * 80.0); // Tcell = Tair + (NOCT - 20) / 80 * R, where the unit of R is mW/cm^2

			// now consider cell wiring and distributed efficiency. TODO: This is very inaccurate. The output depends on both cell wiring and panel wiring.
			switch (panel.getShadeTolerance()) { // the ideal case that probably doesn't exist in reality
			case SolarPanel.HIGH_SHADE_TOLERANCE:
				for (int x = 0; x < nx; x++) {
					for (int y = 0; y < ny; y++) {
						output = cellOutputs[x][y];
						tcell = airTemperature + output * noctFactor;
						syseff = panel.getSystemEfficiency(tcell);
						rack.getSolarPotential()[iMinute] += output * syseff;
					}
				}
				break;
			case SolarPanel.NO_SHADE_TOLERANCE: // assuming that all the cells on a panel are connected in series and all panels are connected in parallel
				double min = Double.MAX_VALUE;
				for (int ix = 0; ix < nxPanels; ix++) { // panel by panel
					for (int iy = 0; iy < nyPanels; iy++) {
						min = Double.MAX_VALUE;
						for (int jx = 0; jx < nxCells; jx++) { // cell by cell on each panel
							for (int jy = 0; jy < nyCells; jy++) {
								output = cellOutputs[ix * nxCells + jx][iy * nyCells + jy];
								tcell = airTemperature + output * noctFactor;
								syseff = panel.getSystemEfficiency(tcell);
								output *= syseff;
								if (output < min) {
									min = output;
								}
							}
						}
						rack.getSolarPotential()[iMinute] += min * nxCells * nyCells;
					}
				}
				break;
			case SolarPanel.PARTIAL_SHADE_TOLERANCE: // assuming each panel uses a diode bypass to connect two columns of cells
				for (int ix = 0; ix < nxPanels; ix++) { // panel by panel
					for (int iy = 0; iy < nyPanels; iy++) {
						min = Double.MAX_VALUE;
						if (panel.isRotated()) { // landscape: nxCells = 10, nyCells = 6
							for (int jy = 0; jy < nyCells; jy++) { // cell by cell on each panel
								if (jy % 2 == 0) {// reset min every two columns of cells
									min = Double.MAX_VALUE;
								}
								for (int jx = 0; jx < nxCells; jx++) {
									output = cellOutputs[ix * nxCells + jx][iy * nyCells + jy];
									tcell = airTemperature + output * noctFactor;
									syseff = panel.getSystemEfficiency(tcell);
									output *= syseff;
									if (output < min) {
										min = output;
									}
								}
								if (jy % 2 == 1) {
									rack.getSolarPotential()[iMinute] += min * 2 * nxCells;
								}
							}
						} else { // portrait: nxCells = 6, nyCells = 10
							for (int jx = 0; jx < nxCells; jx++) { // cell by cell on each panel
								if (jx % 2 == 0) {// reset min every two columns of cells
									min = Double.MAX_VALUE;
								}
								for (int jy = 0; jy < nyCells; jy++) {
									output = cellOutputs[ix * nxCells + jx][iy * nyCells + jy];
									tcell = airTemperature + output * noctFactor;
									syseff = panel.getSystemEfficiency(tcell);
									output *= syseff;
									if (output < min) {
										min = output;
									}
								}
								if (jx % 2 == 1) {
									rack.getSolarPotential()[iMinute] += min * 2 * nyCells;
								}
							}
						}
					}
				}
				break;
			}

		} else { // for simulation speed, approximate rack model doesn't compute panel by panel and cell by cell

			ySpacing = xSpacing = Scene.getInstance().getRackCellSize() / Scene.getInstance().getAnnotationScale();
			nx = Math.max(2, (int) (d20 / xSpacing)); // swap the x and y back to correct order
			ny = Math.max(2, (int) (d10 / ySpacing));
			// nx*ny*60: dividing the total rack area by nx*ny gets the unit cell area of the nx*ny grid; 60 converts the unit of timeStep from minute to kWh
			final double a = rack.getRackWidth() * rack.getRackHeight() * Scene.getInstance().getTimeStep() / (nx * ny * 60.0);
			u = p20;
			v = p10;
			if (cellOutputs == null || cellOutputs.length != nx || cellOutputs[0].length != ny) {
				cellOutputs = new double[nx][ny];
			}

			// calculate the solar radiation first without worrying about the underlying cell wiring and distributed efficiency
			for (int x = 0; x < nx; x++) {
				for (int y = 0; y < ny; y++) {
					if (EnergyPanel.getInstance().isCancelled()) {
						throw new CancellationException();
					}
					final Vector3 u2 = u.multiply(xSpacing * (x + 0.5), null);
					final Vector3 v2 = v.multiply(ySpacing * (y + 0.5), null);
					final ReadOnlyVector3 p = drawMesh.getWorldTransform().applyForward(p0.add(v2, null).addLocal(u2)).addLocal(offset);
					final Ray3 pickRay = new Ray3(p, directionTowardSun);
					double radiation = indirectRadiation; // assuming that indirect (ambient or diffuse) radiation can always reach a grid point
					if (dot > 0) {
						final PickResults pickResults = new PrimitivePickResults();
						for (final Spatial spatial : collidables) {
							if (spatial != collisionMesh) {
								PickingUtil.findPick(spatial, pickRay, pickResults, false);
								if (pickResults.getNumber() != 0) {
									break;
								}
							}
						}
						if (pickResults.getNumber() == 0) {
							radiation += directRadiation;
						}
					}
					cellOutputs[x][y] = radiation * a;
				}
			}

			final double noctFactor = (panel.getNominalOperatingCellTemperature() - 20.0) * 100.0 / (a * 80.0); // Tcell = Tair + (NOCT - 20) / 80 * R, where the unit of R is mW/cm^2

			// now consider cell wiring and distributed efficiency. TODO: This is very inaccurate. The output depends on both cell wiring and panel wiring.
			switch (panel.getShadeTolerance()) {
			case SolarPanel.HIGH_SHADE_TOLERANCE: // the ideal case that probably doesn't exist in reality
				for (int x = 0; x < nx; x++) {
					for (int y = 0; y < ny; y++) {
						output = cellOutputs[x][y];
						tcell = airTemperature + output * noctFactor;
						syseff = panel.getSystemEfficiency(tcell);
						rack.getSolarPotential()[iMinute] += output * syseff;
					}
				}
				break;
			case SolarPanel.NO_SHADE_TOLERANCE:
				double min = Double.MAX_VALUE;
				for (int x = 0; x < nx; x++) {
					for (int y = 0; y < ny; y++) {
						output = cellOutputs[x][y];
						tcell = airTemperature + output * noctFactor;
						syseff = panel.getSystemEfficiency(tcell);
						output *= syseff;
						if (output < min) {
							min = output;
						}
					}
				}
				rack.getSolarPotential()[iMinute] += min * ny * nx;
				break;
			case SolarPanel.PARTIAL_SHADE_TOLERANCE:
				for (int x = 0; x < nx; x++) {
					min = Double.MAX_VALUE;
					for (int y = 0; y < ny; y++) {
						output = cellOutputs[x][y];
						tcell = airTemperature + output * noctFactor;
						syseff = panel.getSystemEfficiency(tcell);
						output *= syseff;
						if (output < min) {
							min = output;
						}
					}
					rack.getSolarPotential()[iMinute] += min * ny;
				}
				break;
			}

		}

	}

	public void initMeshTextureData(final Mesh drawMesh, final Mesh collisionMesh, final ReadOnlyVector3 normal) {
		if (onMesh.get(drawMesh) == null) {
			drawMesh.setDefaultColor(ColorRGBA.BLACK);
			initMeshTextureData(drawMesh, collisionMesh, normal, true);
		}
	}

	private MeshDataStore initMeshTextureData(final Mesh drawMesh, final Mesh collisionMesh, final ReadOnlyVector3 normal, final boolean updateTexture) {
		final MeshDataStore data = new MeshDataStore();
		if (normal != null) {

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
				if (p.getX() < minX) {
					minX = p.getX();
				}
				if (p.getX() > maxX) {
					maxX = p.getX();
				}
				if (p.getY() < minY) {
					minY = p.getY();
				}
				if (p.getY() > maxY) {
					maxY = p.getY();
				}
				if (Double.isNaN(z)) {
					z = p.getZ();
				}
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

			final double solarStep = Scene.getInstance().getSolarStep();
			data.rows = Math.max(1, (int) Math.ceil(data.p1.subtract(data.p0, null).length() / solarStep));
			data.cols = Math.max(1, (int) Math.ceil(data.p2.subtract(data.p0, null).length() / solarStep));
			data.dailySolarIntensity = new double[Util.roundToPowerOfTwo(data.rows)][Util.roundToPowerOfTwo(data.cols)];

			final ReadOnlyVector2 originXY = new Vector2(minX, minY);
			final ReadOnlyVector2 uXY = new Vector2(maxX - minX, 0).normalizeLocal();
			final ReadOnlyVector2 vXY = new Vector2(0, maxY - minY).normalizeLocal();
			final int nrow = data.dailySolarIntensity.length;
			final int ncol = data.dailySolarIntensity[0].length;
			for (int row = 0; row < nrow; row++) {
				for (int col = 0; col < ncol; col++) {
					if (row >= data.rows || col >= data.cols) { // overshot cells
						data.dailySolarIntensity[row][col] = -1;
					} else {
						final ReadOnlyVector2 p = originXY.add(uXY.multiply(col * solarStep, null), null).add(vXY.multiply(row * solarStep, null), null);
						boolean isInside = false;
						if (points.size() >= 3) { // FIXME: sometimes we can end up with less than three points
							for (int i = 0; i < points.size(); i += 3) {
								if (Util.isPointInsideTriangle(p, points.get(i), points.get(i + 1), points.get(i + 2))) {
									isInside = true;
									break;
								}
							}
						}
						if (!isInside && col > 0 && row > 0) { // must at least include one column or row
							data.dailySolarIntensity[row][col] = -1;
						}
					}
				}
			}

			data.u = data.p2.subtract(data.p0, null).normalizeLocal();
			data.v = data.p1.subtract(data.p0, null).normalizeLocal();

		}

		// TODO: Temporarily allow the program to move forward behind this point even if normal is null

		onMesh.put(drawMesh, data);

		if (updateTexture) {
			updateTextureCoords(drawMesh);
		}
		return data;
	}

	private MeshDataStore initMeshTextureDataOnRectangle(final Mesh mesh, final int rows, final int cols) {
		final MeshDataStore data = new MeshDataStore();
		data.rows = rows;
		data.cols = cols;
		data.dailySolarIntensity = new double[Util.roundToPowerOfTwo(data.rows)][Util.roundToPowerOfTwo(data.cols)];
		onMesh.put(mesh, data);
		return data;
	}

	private void updateTextureCoords(final Mesh drawMesh) {
		final MeshDataStore data = onMesh.get(drawMesh);
		final ReadOnlyVector3 o = data.p0;
		final ReadOnlyVector3 u = data.u.multiply(Util.roundToPowerOfTwo(data.cols) * Scene.getInstance().getSolarStep(), null);
		final ReadOnlyVector3 v = data.v.multiply(Util.roundToPowerOfTwo(data.rows) * Scene.getInstance().getSolarStep(), null);
		final FloatBuffer vertexBuffer = drawMesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		final FloatBuffer textureBuffer = drawMesh.getMeshData().getTextureBuffer(0);
		if (textureBuffer != null) {
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
	}

	// air mass calculation from http://en.wikipedia.org/wiki/Air_mass_(solar_energy)#At_higher_altitudes
	private double computeAirMass(final ReadOnlyVector3 directionTowardSun) {
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
				final double c = LocationData.getInstance().getAltitudes().get(city) / 9000.0;
				return Math.sqrt((r + c) * (r + c) * cos * cos + (2 * r + 1 + c) * (1 - c)) - (r + c) * cos;
			} else {
				return Math.sqrt(r * r * cos * cos + 2 * r + 1) - r * cos;
			}
		}
	}

	// Solar radiation incident outside the earth's atmosphere is called extraterrestrial radiation.
	// https://pvpmc.sandia.gov/modeling-steps/1-weather-design-inputs/irradiance-and-insolation-2/extraterrestrial-radiation/
	private static double getExtraterrestrialRadiation() {
		final double b = Math.PI * 2.0 * Heliodon.getInstance().getCalendar().get(Calendar.DAY_OF_YEAR) / 365.0;
		final double er = 1.00011 + 0.034221 * Math.cos(b) + 0.00128 * Math.sin(b) + 0.000719 * Math.cos(2 * b) + 0.000077 * Math.sin(2 * b);
		return SOLAR_CONSTANT * er;
	}

	// Reused peak solar radiation value. Must be called once and only once before calling calculateDirectRadiation and calculateDiffusionAndReflection
	private void calculatePeakRadiation(final ReadOnlyVector3 directionTowardSun, final double dayLength) {
		double sunshinePercentage = 1.0;
		final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
		if (!city.equals("")) {
			final int[] sunshineHours = LocationData.getInstance().getSunshineHours().get(city);
			if (sunshineHours != null) {
				sunshinePercentage = sunshineHours[Heliodon.getInstance().getCalendar().get(Calendar.MONTH)] / (dayLength * 30);
			}
		}
		// don't use the 1.1 prefactor as we consider diffuse radiation in the ASHRAE model
		peakRadiation = getExtraterrestrialRadiation() * Math.pow(0.7, Math.pow(computeAirMass(directionTowardSun), 0.678)) * sunshinePercentage;
	}

	private double calculateDirectRadiation(final ReadOnlyVector3 directionTowardSun, final ReadOnlyVector3 normal) {
		final double result = directionTowardSun.dot(normal) * peakRadiation;
		return result < 0 ? 0 : result;
	}

	// see: http://www.physics.arizona.edu/~cronin/Solar/References/Irradiance%20Models%20and%20Data/WOC01.pdf
	private double calculateDiffuseAndReflectedRadiation(final ReadOnlyVector3 directionTowardSun, final ReadOnlyVector3 normal) {
		final int month = Heliodon.getInstance().getCalendar().get(Calendar.MONTH);
		double result = 0;
		final double cos = normal.dot(Vector3.UNIT_Z);
		final double viewFactorWithSky = 0.5 * (1 + cos);
		if (viewFactorWithSky > 0) { // diffuse irradiance from the sky
			result += ASHRAE_C[month] * viewFactorWithSky * peakRadiation;
		}
		final double viewFactorWithGround = 0.5 * Math.abs(1 - cos); // if a surface faces down, it should receive ground reflection as well
		if (viewFactorWithGround != 0) { // short-wave reflection from the ground
			result += Scene.getInstance().getGround().getAdjustedAlbedo(month) * viewFactorWithGround * peakRadiation;
		}
		return result;
	}

	public void computeEnergyOfToday() {
		updateTextures();
		if (Scene.getInstance().getAlwaysComputeHeatFluxVectors()) {
			for (final HousePart part : Scene.getInstance().getParts()) {
				part.drawHeatFlux();
			}
		}

		final Calendar today = Heliodon.getInstance().getCalendar();
		final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
		final double[] outsideTemperatureRange = Weather.computeOutsideTemperature(today, city);

		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Foundation) {
				final Foundation foundation = (Foundation) part;
				final int n = foundation.getHeatLoss().length;
				final double[] heatLoss = new double[n];
				final double[] passiveSolar = new double[n];
				final double[] photovoltaic = new double[n];
				final double[] csp = new double[n];
				for (int i = 0; i < n; i++) {
					final double groundHeatLoss = foundation.getHeatLoss()[i];
					// In most cases, the inside temperature is always higher than the ground temperature. In this winter, this adds to heating load, but in the summer, this reduces cooling load.
					// In other words, geothermal energy is good in hot conditions. This is similar to passive solar energy, which is good in the winter but bad in the summer.
					if (groundHeatLoss > 0) {
						final double outsideTemperature = Weather.getInstance().getOutsideTemperatureAtMinute(outsideTemperatureRange[1], outsideTemperatureRange[0], i * Scene.getInstance().getTimeStep());
						if (outsideTemperature >= foundation.getThermostat().getTemperature(today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY, today.get(Calendar.HOUR_OF_DAY))) {
							heatLoss[i] -= groundHeatLoss;
						}
					} else {
						heatLoss[i] += groundHeatLoss;
					}
				}
				double solarPotentialTotal = 0;
				for (final HousePart child : Scene.getInstance().getParts()) {
					if (child.getTopContainer() == foundation) {
						child.setSolarPotentialToday(0);
						if (child instanceof SolarPanel) {
							((SolarPanel) child).setYieldToday(0);
						} else if (child instanceof Rack) {
							((Rack) child).setYieldToday(0);
						} else if (child instanceof Mirror) {
							((Mirror) child).setYieldToday(0);
						}
						for (int i = 0; i < n; i++) {
							solarPotentialTotal += child.getSolarPotential()[i];
							child.setSolarPotentialToday(child.getSolarPotentialToday() + child.getSolarPotential()[i]);
							if (child instanceof Wall || child instanceof Door || child instanceof Window || child instanceof Roof) {
								heatLoss[i] += child.getHeatLoss()[i];
							}
							if (child instanceof Window) {
								final Window window = (Window) child;
								passiveSolar[i] += child.getSolarPotential()[i] * window.getSolarHeatGainCoefficient();
							} else if (child instanceof SolarPanel) {
								final SolarPanel sp = (SolarPanel) child;
								final double yield = sp.getSolarPotential()[i]; // distributed efficiency must be handled for each individual cell
								sp.setYieldToday(sp.getYieldToday() + yield);
								photovoltaic[i] += yield;
							} else if (child instanceof Rack) {
								final Rack rack = (Rack) child;
								final double yield = rack.getSolarPotential()[i]; // distributed efficiency must be handled for each individual cell
								rack.setYieldToday(rack.getYieldToday() + yield);
								photovoltaic[i] += yield;
							} else if (child instanceof Mirror) {
								final Mirror mirror = (Mirror) child;
								final double yield = mirror.getSolarPotential()[i] * mirror.getSystemEfficiency();
								mirror.setYieldToday(mirror.getYieldToday() + yield);
								csp[i] += yield;
							}
						}
					}
				}
				if (foundation.getImportedNodes() != null) {
					for (int i = 0; i < n; i++) {
						solarPotentialTotal += foundation.getSolarPotential()[i];
						foundation.setSolarPotentialToday(foundation.getSolarPotentialToday() + foundation.getSolarPotential()[i]);
					}
				}

				double heatingTotal = 0.0;
				double coolingTotal = 0.0;
				double passiveSolarTotal = 0.0;
				double photovoltaicTotal = 0.0;
				double cspTotal = 0.0;
				for (int i = 0; i < n; i++) {
					if (heatLoss[i] < 0) {
						heatLoss[i] -= passiveSolar[i];
					} else {
						heatLoss[i] = Math.max(0, heatLoss[i] - passiveSolar[i]);
					}
					if (heatLoss[i] > 0) {
						heatingTotal += heatLoss[i];
					} else {
						coolingTotal -= heatLoss[i];
					}
					passiveSolarTotal += passiveSolar[i];
					photovoltaicTotal += photovoltaic[i];
					cspTotal += csp[i];
				}

				foundation.setSolarPotentialToday(solarPotentialTotal);
				foundation.setPassiveSolarToday(passiveSolarTotal);
				foundation.setPhotovoltaicToday(photovoltaicTotal);
				foundation.setCspToday(cspTotal);
				foundation.setHeatingToday(heatingTotal);
				foundation.setCoolingToday(coolingTotal);
				foundation.setTotalEnergyToday(heatingTotal + coolingTotal - photovoltaicTotal);

			}

		}

	}

	public void computeEnergyAtHour(final int hour) {
		final Calendar today = Heliodon.getInstance().getCalendar();
		final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
		final double[] outsideAirTemperatureRange = Weather.computeOutsideTemperature(today, city);
		final double outsideAirTemperature = Weather.getInstance().getOutsideTemperatureAtMinute(outsideAirTemperatureRange[1], outsideAirTemperatureRange[0], hour * 60);

		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Foundation) {
				final Foundation foundation = (Foundation) part;
				if (foundation.getHeatLoss() == null) {
					continue;
				}
				final int n = (int) Math.round(60.0 / Scene.getInstance().getTimeStep());
				final double[] heatLoss = new double[n];
				final double[] passiveSolar = new double[n];
				final double[] photovoltaic = new double[n];
				final double[] csp = new double[n];
				final int t0 = n * hour;
				for (int i = 0; i < n; i++) {
					final double groundHeatLoss = foundation.getHeatLoss()[t0 + i];
					if (groundHeatLoss > 0) {
						final double thermostat = foundation.getThermostat().getTemperature(today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY, today.get(Calendar.HOUR_OF_DAY));
						if (outsideAirTemperature >= thermostat) {
							heatLoss[i] -= groundHeatLoss;
						}
					} else {
						heatLoss[i] += groundHeatLoss;
					}
				}
				double solarPotentialTotal = 0.0;
				for (final HousePart child : Scene.getInstance().getParts()) {
					if (child.getTopContainer() == foundation) {
						child.setSolarPotentialNow(0);
						if (child instanceof SolarPanel) {
							((SolarPanel) child).setYieldNow(0);
						} else if (child instanceof Rack) {
							((Rack) child).setYieldNow(0);
						}
						for (int i = 0; i < n; i++) {
							solarPotentialTotal += child.getSolarPotential()[t0 + i];
							child.setSolarPotentialNow(child.getSolarPotentialNow() + child.getSolarPotential()[t0 + i]);
							if (child instanceof Wall || child instanceof Door || child instanceof Window || child instanceof Roof) {
								heatLoss[i] += child.getHeatLoss()[t0 + i];
							}
							if (child instanceof Window) {
								final Window window = (Window) child;
								passiveSolar[i] += window.getSolarPotential()[t0 + i] * window.getSolarHeatGainCoefficient();
							} else if (child instanceof Mirror) {
								final Mirror mirror = (Mirror) child;
								final double yield = mirror.getSolarPotential()[t0 + i] * mirror.getSystemEfficiency();
								csp[i] += yield;
								mirror.setYieldNow(mirror.getYieldNow() + yield);
							} else if (child instanceof SolarPanel) {
								final SolarPanel sp = (SolarPanel) child;
								final double yield = sp.getSolarPotential()[t0 + i]; // distributed efficiency must be handled for each individual cell
								photovoltaic[i] += yield;
								sp.setYieldNow(sp.getYieldNow() + yield);
							} else if (child instanceof Rack) {
								final Rack rack = (Rack) child;
								if (rack.isMonolithic()) {
									final double yield = rack.getSolarPotential()[t0 + i]; // distributed efficiency must be handled for each individual cell
									photovoltaic[i] += yield;
									rack.setYieldNow(rack.getYieldNow() + yield);
								}
							}
						}
					}
				}

				double heatingTotal = 0.0;
				double coolingTotal = 0.0;
				double passiveSolarTotal = 0.0;
				double photovoltaicTotal = 0.0;
				double cspTotal = 0.0;
				for (int i = 0; i < n; i++) {
					if (heatLoss[i] < 0) {
						heatLoss[i] -= passiveSolar[i];
					} else {
						heatLoss[i] = Math.max(0, heatLoss[i] - passiveSolar[i]);
					}
					if (heatLoss[i] > 0) {
						heatingTotal += heatLoss[i];
					} else {
						coolingTotal -= heatLoss[i];
					}
					passiveSolarTotal += passiveSolar[i];
					photovoltaicTotal += photovoltaic[i];
					cspTotal += csp[i];
				}

				foundation.setSolarPotentialNow(solarPotentialTotal);
				foundation.setPassiveSolarNow(passiveSolarTotal);
				foundation.setPhotovoltaicNow(photovoltaicTotal);
				foundation.setCspNow(cspTotal);
				foundation.setHeatingNow(heatingTotal);
				foundation.setCoolingNow(coolingTotal);
				foundation.setTotalEnergyNow(heatingTotal + coolingTotal - photovoltaicTotal);

			}

		}

	}

	public void updateTextures() {
		EnergyPanel.getInstance().setComputingStartMillis(System.currentTimeMillis());
		maxValue = Math.round((MINUTES_OF_DAY / Scene.getInstance().getTimeStep() + 1.0) * (1 - 0.01 * Scene.getInstance().getSolarHeatMapColorContrast()));
		applyTexture(SceneManager.getInstance().getSolarLand());
		final int totalMeshes = Scene.getInstance().getParts().size() + Scene.getInstance().countMeshes();
		int countMesh = 0;
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Solar) {
				applyTexture(part.getRadiationMesh());
			} else {
				if (!Scene.getInstance().getOnlySolarComponentsInSolarMap()) {
					if (part instanceof Wall) {
						applyTexture(part.getRadiationMesh());
					} else if (part instanceof Foundation) {
						final Foundation foundation = (Foundation) part;
						for (int i = 0; i < 5; i++) {
							applyTexture(foundation.getRadiationMesh(i));
						}
						final List<Node> importedNodes = foundation.getImportedNodes();
						if (importedNodes != null) {
							for (final Node node : importedNodes) {
								for (final Spatial s : node.getChildren()) {
									applyTexture((Mesh) s);
									EnergyPanel.getInstance().progress((int) Math.round(100.0 * (countMesh++) / totalMeshes));
								}
							}
						}
					} else if (part instanceof Roof) {
						for (final Spatial roofPart : ((Roof) part).getRoofPartsRoot().getChildren()) {
							if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
								final Mesh mesh = (Mesh) ((Node) roofPart).getChild(6);
								applyTexture(mesh);
							}
						}
					}
				}
			}
			countMesh++;
		}
		EnergyPanel.getInstance().progress(0);
	}

	private void applyTexture(final Mesh mesh) {
		if (onMesh.get(mesh) == null) {
			mesh.setDefaultColor(ColorRGBA.BLACK);
			mesh.clearRenderState(StateType.Texture);
			return;
		}
		final double[][] solarData = onMesh.get(mesh).dailySolarIntensity;
		final int rows = solarData.length;
		if (rows == 0) {
			return;
		}
		final int cols = solarData[0].length;
		if (cols == 0) {
			return;
		}
		fillBlanksWithNeighboringValues(solarData);
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
		mesh.setDefaultColor(Scene.WHITE);
		mesh.setRenderState(textureState);
	}

	private void fillBlanksWithNeighboringValues(final double[][] solarData) {
		final int rows = solarData.length;
		final int cols = solarData[0].length;
		for (int repeat = 0; repeat < 2; repeat++) {
			for (int row = 0; row < rows; row++) {
				for (int col = 0; col < cols; col++) {
					if (solarData[row][col] == -1) {
						if (solarData[row][(col + 1) % cols] != -1) {
							solarData[row][col] = solarData[row][(col + 1) % cols];
						} else if (col != 0 && solarData[row][col - 1] != -1) {
							solarData[row][col] = solarData[row][col - 1];
						} else if (col == 0 && solarData[row][cols - 1] != -1) {
							solarData[row][col] = solarData[row][cols - 1];
						} else if (solarData[(row + 1) % rows][col] != -1) {
							solarData[row][col] = solarData[(row + 1) % rows][col];
						} else if (row != 0 && solarData[row - 1][col] != -1) {
							solarData[row][col] = solarData[row - 1][col];
						} else if (row == 0 && solarData[rows - 1][col] != -1) {
							solarData[row][col] = solarData[rows - 1][col];
						}
					}
				}
			}
		}
	}

	public static ColorRGBA computeColor(final double value, final long maxValue) {
		final ReadOnlyColorRGBA[] colors = EnergyPanel.solarColors;
		long valuePerColorRange = maxValue / (colors.length - 1);
		int colorIndex;
		if (valuePerColorRange == 0) {
			valuePerColorRange = 1;
			colorIndex = 0;
		} else {
			colorIndex = (int) Math.min(value / valuePerColorRange, colors.length - 2);
			if (colorIndex < 0) {
				colorIndex = 0;
			}
		}
		final float scalar = Math.min(1.0f, (float) (value - valuePerColorRange * colorIndex) / valuePerColorRange);
		return new ColorRGBA().lerpLocal(colors[colorIndex], colors[colorIndex + 1], scalar);
	}

	public void setAirMassSelection(final int selection) {
		airMassSelection = selection;
	}

	public int getAirMassSelection() {
		return airMassSelection;
	}

}
