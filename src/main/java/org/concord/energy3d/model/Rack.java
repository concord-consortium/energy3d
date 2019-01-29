package org.concord.energy3d.model;

import java.awt.EventQueue;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.AngleAnnotation;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.PvModuleSpecs;
import org.concord.energy3d.undo.AddArrayCommand;
import org.concord.energy3d.util.FontManager;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.extension.effect.bloom.BloomRenderPass;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Transform;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyTransform;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.OffsetState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.ui.text.BMText.Justify;
import com.ardor3d.util.geom.BufferUtils;

public class Rack extends HousePart implements Trackable, Meshable, Labelable {

	public static final int MAXIMUM_SOLAR_PANEL_ROWS = 6;
	private static final long serialVersionUID = 1L;
	private transient ArrayList<Vector3> solarOrgPoints;
	private transient ReadOnlyVector3 normal;
	private transient Mesh outlineMesh;
	private transient Box surround;
	private transient Node polesRoot;
	private transient Node angles;
	private transient AngleAnnotation sunAngle;
	private transient Line sunBeam;
	private transient Line normalVector;
	private transient Line solarPanelOutlines;
	private transient BMText label;
	private transient Vector3 moveStartPoint;
	private transient double copyLayoutGap = 1;
	private transient boolean allowAzimuthLargeRotation;
	private transient double yieldNow; // solar output at current hour
	private transient double yieldToday;
	private ReadOnlyVector3 previousNormal;
	private double rackWidth = 4.95; // 5x1 0.99m x 1.65m solar panels by default (use only one row so that it can fit a small roof)
	private double rackHeight = 1.65;
	private double relativeAzimuth;
	private transient double oldRelativeAzimuth;
	private double tiltAngle; // fixed tilt angle
	private double[] monthlyTiltAngles; // seasonally adjusted tilt angles
	private double baseHeight = 15;
	private double poleDistanceX = 4;
	private double poleDistanceY = 2;
	private boolean poleInvisible;
	private int trackerType = NO_TRACKER;
	private boolean monolithic = true; // true if the whole rack is covered by solar panels
	private boolean drawSunBeam;
	private SolarPanel sampleSolarPanel;
	private boolean labelCellEfficiency;
	private boolean labelTiltAngle;
	private boolean labelTracker;
	private boolean labelEnergyOutput;
	private transient Vector3 oldRackCenter;
	private transient double oldRackWidth, oldRackHeight;
	private static double normalVectorLength = 5;
	private static transient BloomRenderPass bloomRenderPass;
	private transient double baseZ;
	private MeshLocator meshLocator; // if the mesh that this rack rests on is a vertical surface of unknown type (e.g., an imported mesh), store its info for finding it later

	public Rack() {
		super(1, 1, 0);
	}

	@Override
	protected void init() {
		super.init();

		if (Util.isZero(copyLayoutGap)) { // FIXME: Why is a transient member evaluated to zero?
			copyLayoutGap = 1;
		}
		if (Util.isZero(rackWidth)) {
			rackWidth = 4.95;
		}
		if (Util.isZero(rackHeight)) {
			rackHeight = 1.65;
		}
		if (monthlyTiltAngles == null) {
			monthlyTiltAngles = new double[12];
			Arrays.fill(monthlyTiltAngles, tiltAngle);
		}

		mesh = new Mesh("Rack");
		mesh.setDefaultColor(ColorRGBA.LIGHT_GRAY);
		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(6));
		mesh.getMeshData().setTextureBuffer(BufferUtils.createVector2Buffer(6), 0);
		mesh.setModelBound(new OrientedBoundingBox());
		mesh.setUserData(new UserData(this));
		root.attachChild(mesh);

		surround = new Box("Rack (Surround)");
		surround.setDefaultColor(ColorRGBA.LIGHT_GRAY);
		surround.setModelBound(new OrientedBoundingBox());
		final OffsetState offset = new OffsetState();
		offset.setFactor(0.2f); // set a smaller value than solar panel so that the texture doesn't show up on the underside
		offset.setUnits(1);
		surround.setRenderState(offset);
		root.attachChild(surround);

		outlineMesh = new Line("Rack (Outline)");
		outlineMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(8));
		outlineMesh.setDefaultColor(ColorRGBA.BLACK);
		outlineMesh.setModelBound(new OrientedBoundingBox());
		root.attachChild(outlineMesh);

		sunBeam = new Line("Sun Beam");
		sunBeam.setLineWidth(1f);
		sunBeam.setStipplePattern((short) 0xffff);
		sunBeam.setModelBound(null);
		Util.disablePickShadowLight(sunBeam);
		sunBeam.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		sunBeam.setDefaultColor(new ColorRGBA(1f, 1f, 1f, 1f));
		root.attachChild(sunBeam);

		normalVector = new Line("Normal Vector");
		normalVector.setLineWidth(1f);
		normalVector.setStipplePattern((short) 0xffff);
		normalVector.setModelBound(null);
		Util.disablePickShadowLight(normalVector);
		normalVector.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(6));
		normalVector.setDefaultColor(new ColorRGBA(1f, 1f, 0f, 1f));
		root.attachChild(normalVector);

		angles = new Node("Angles");
		angles.getSceneHints().setAllPickingHints(false);
		Util.disablePickShadowLight(angles);
		root.attachChild(angles);

		sunAngle = new AngleAnnotation(); // the angle between the sun beam and the normal vector
		sunAngle.setColor(ColorRGBA.WHITE);
		sunAngle.setLineWidth(1);
		sunAngle.setFontSize(1);
		sunAngle.setCustomRadius(normalVectorLength * 0.8);
		angles.attachChild(sunAngle);

		solarPanelOutlines = new Line("Solar Panel Outlines");
		solarPanelOutlines.setLineWidth(1f);
		solarPanelOutlines.setStipplePattern((short) 0xffff);
		solarPanelOutlines.setModelBound(null);
		Util.disablePickShadowLight(solarPanelOutlines);
		solarPanelOutlines.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(1));
		solarPanelOutlines.setDefaultColor(new ColorRGBA(0f, 0f, 0f, 1f));
		root.attachChild(solarPanelOutlines);

		label = new BMText("Label", "#" + id, FontManager.getInstance().getPartNumberFont(), Align.Center, Justify.Center);
		Util.initHousePartLabel(label);
		label.setFontScale(0.5);
		label.setVisible(false);
		root.attachChild(label);

		polesRoot = new Node("Poles Root");
		root.attachChild(polesRoot);
		updateTextureAndColor();

		if (sampleSolarPanel == null) {
			sampleSolarPanel = new SolarPanel();
		}
		// ugly fixes for zero initial values when sampleSolarPanel is previously serialized without new variables
		if (sampleSolarPanel.getPvModuleSpecs() == null) {
			sampleSolarPanel.setPvModuleSpecs(new PvModuleSpecs());
		}
		if (Util.isZero(sampleSolarPanel.getNominalOperatingCellTemperature())) {
			sampleSolarPanel.setNominalOperatingCellTemperature(48);
		}

		if (!points.isEmpty()) {
			oldRackCenter = points.get(0).clone();
		}
		oldRackWidth = rackWidth;
		oldRackHeight = rackHeight;
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		if (lockEdit) {
			return;
		}
		if (moveStartPoint == null) {
			initSolarPanelsForMove();
		}
		if (editPointIndex <= 0) {
			// isBaseZ = true;
			final PickedHousePart picked = pickContainer(x, y, new Class<?>[] { Foundation.class, Roof.class, Wall.class, Floor.class });
			if (picked != null && picked.getUserData() != null) { // when the user data is null, it picks the land
				final Vector3 p = picked.getPoint().clone();
				// isBaseZ = Util.isEqual(p.getZ(), baseZ);
				final UserData ud = picked.getUserData();
				if (ud.getHousePart() instanceof Foundation && ud.isImported() && ud.getNodeIndex() >= 0 && ud.getMeshIndex() >= 0) {
					// if this rack rests on an imported mesh, store its info and don't snap to grid (as imported meshes do not sit on grid)
					meshLocator = new MeshLocator((Foundation) ud.getHousePart(), ud.getNodeIndex(), ud.getMeshIndex());
				} else {
					snapToGrid(p, getAbsPoint(0), getGridSize(), container instanceof Wall);
					meshLocator = null;
				}
				points.get(0).set(toRelative(p));
				pickedNormal = ud.getRotatedNormal() == null ? ud.getNormal() : ud.getRotatedNormal();
			} else {
				pickedNormal = null;
			}
			if (outOfBound()) {
				if (oldRackCenter != null && !oldRackCenter.equals(new Vector3())) { // TODO: Need to find a better way to do this
					points.get(0).set(oldRackCenter);
				}
			} else {
				oldRackCenter = points.get(0).clone();
			}
			if (container != null) {
				moveSolarPanels(getPoints().get(0).clone().subtractLocal(moveStartPoint), solarOrgPoints);
			}
		} else {
			final ReadOnlyVector3 pEdit = getEditPointShape(editPointIndex).getTranslation();
			final Vector3 p;
			if (editPointIndex % 2 == 0) {
				final ReadOnlyVector3 p1 = getEditPointShape(editPointIndex == 2 ? 4 : 2).getTranslation();
				p = Util.closestPoint(pEdit, pEdit.subtract(p1, null).normalizeLocal(), x, y);
				if (p != null) {
					final double rw = p.distance(p1) * Scene.getInstance().getScale();
					final double pw = sampleSolarPanel.isRotated() ? sampleSolarPanel.getPanelHeight() : sampleSolarPanel.getPanelWidth();
					if (rw > pw) {
						final Vector3 delta = toRelativeVector(p.subtract(pEdit, null)).multiplyLocal(0.5);
						points.get(0).addLocal(delta);
						getEditPointShape(editPointIndex).setTranslation(p);
						setRackWidth(rw);
						if (outOfBound()) {
							if (oldRackCenter != null) {
								points.get(0).set(oldRackCenter);
							}
							setRackWidth(oldRackWidth);
						} else {
							oldRackCenter = points.get(0).clone();
							oldRackWidth = rackWidth;
						}
					}
				}
			} else {
				final ReadOnlyVector3 p1 = getEditPointShape(editPointIndex == 1 ? 3 : 1).getTranslation();
				p = Util.closestPoint(pEdit, pEdit.subtract(p1, null).normalizeLocal(), x, y);
				if (p != null) {
					final double rh = p.distance(p1) * Scene.getInstance().getScale();
					final double ph = sampleSolarPanel.isRotated() ? sampleSolarPanel.getPanelWidth() : sampleSolarPanel.getPanelHeight();
					if (rh > ph) {
						final Vector3 delta = toRelativeVector(p.subtract(pEdit, null)).multiplyLocal(0.5);
						points.get(0).addLocal(delta);
						getEditPointShape(editPointIndex).setTranslation(p);
						setRackHeight(rh);
						if (outOfBound()) {
							if (oldRackCenter != null) {
								points.get(0).set(oldRackCenter);
							}
							setRackHeight(oldRackHeight);
						} else {
							oldRackCenter = points.get(0).clone();
							oldRackHeight = rackHeight;
						}
					}
				}
			}
		}
		if (container != null) {
			draw();
			drawChildren();
			setEditPointsVisible(true);
			setHighlight(!isDrawable());
		}
	}

	private boolean outOfBound() {
		drawMesh();
		if (container instanceof Foundation) {
			final Foundation foundation = (Foundation) container;
			final int n = Math.round(mesh.getMeshData().getVertexBuffer().limit() / 3);
			for (int i = 0; i < n; i++) {
				final Vector3 a = getVertex(i);
				if (a.getZ() < foundation.getHeight() * 1.1) { // left a 10% margin above the foundation
					return true;
				}
				if (!foundation.containsPoint(a.getX(), a.getY())) {
					return true;
				}
			}
		} else if (container instanceof Roof) {
			final Roof roof = (Roof) container;
			final int n = Math.round(mesh.getMeshData().getVertexBuffer().limit() / 3);
			boolean init = true;
			for (int i = 0; i < n; i++) {
				final Vector3 a = getVertex(i);
				if (!roof.insideWalls(a.getX(), a.getY(), init)) {
					return true;
				}
				if (init) {
					init = false;
				}
			}

		}
		return false;
	}

	@Override
	protected void drawMesh() {
		if (container == null) {
			return;
		}

		final double az = Math.toRadians(relativeAzimuth);
		final double currentTilt = monthlyTiltAngles[Heliodon.getInstance().getCalendar().get(Calendar.MONTH)];
		boolean onFlatSurface = onFlatSurface();
		getEditPointShape(0).setDefaultColor(ColorRGBA.ORANGE);
		final Mesh host = meshLocator == null ? null : meshLocator.find(); // if this rack rests on an imported mesh or not?
		if (host == null) {
			normal = pickedNormal != null ? pickedNormal : computeNormalAndKeepOnSurface();
		} else {
			final UserData ud = (UserData) host.getUserData();
			normal = ud.getRotatedNormal() == null ? ud.getNormal() : ud.getRotatedNormal();
			onFlatSurface = Util.isEqual(normal, Vector3.UNIT_Z);
		}

		final double dotE = 0.9999;
		switch (trackerType) {
		case ALTAZIMUTH_DUAL_AXIS_TRACKER:
			normal = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).normalizeLocal();
			break;
		case HORIZONTAL_SINGLE_AXIS_TRACKER:
			final Vector3 sunDirection = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).normalizeLocal();
			final Vector3 rotationAxis = Util.isZero(az) ? new Vector3(1, 0, 0) : new Vector3(MathUtils.cos(az), MathUtils.sin(az), 0); // by default, the rotation axis is in the east-west direction, so az = 0 maps to (1, 0, 0)
			final double axisSunDot = sunDirection.dot(rotationAxis);
			rotationAxis.multiplyLocal(Util.isZero(axisSunDot) ? 0.001 : axisSunDot); // avoid singularity when the direction of the sun is perpendicular to the rotation axis
			normal = sunDirection.subtractLocal(rotationAxis).normalizeLocal();
			break;
		case VERTICAL_SINGLE_AXIS_TRACKER:
			final Vector3 a = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).multiplyLocal(1, 1, 0).normalizeLocal();
			final Vector3 b = Vector3.UNIT_Z.cross(a, null);
			Matrix3 m = new Matrix3().applyRotation(Math.toRadians(90 - currentTilt), b.getX(), b.getY(), b.getZ());
			normal = m.applyPost(a, null);
			if (normal.getZ() < 0) {
				normal = normal.negate(null);
			}
			break;
		case TILTED_SINGLE_AXIS_TRACKER: // TODO: The following does not work
			final double sunAngleX = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).normalizeLocal().dot(Vector3.UNIT_X);
			System.out.println("*** sun cosx = " + sunAngleX + ", " + Math.toDegrees(Math.asin(sunAngleX)));
			// rotate the normal according to the tilt angle, at this point, the axis is still north-south
			setNormal(Util.isZero(currentTilt) ? Math.PI / 2 * dotE : Math.toRadians(90 - currentTilt), az); // exactly 90 degrees will cause the solar panel to disappear
			System.out.println("*** tilt normal = " + normal);
			// the following vector should be the rack axis
			final Vector3 rackAxis = Vector3.UNIT_X.cross(normal, null);
			System.out.println("*** rack axis = " + rackAxis);
			m = new Matrix3().fromAngleNormalAxis(Math.asin(sunAngleX), rackAxis);
			normal = m.applyPost(normal, null);
			System.out.println("*** final normal = " + normal);
			if (normal.getZ() < 0) {
				normal = normal.negate(null);
			}
			break;
		default:
			if (onFlatSurface) {
				setNormal(Util.isZero(currentTilt) ? Math.PI / 2 * dotE : Math.toRadians(90 - currentTilt), az); // exactly 90 degrees will cause the solar panel to disappear
			}
		}
		if (Util.isEqual(normal, Vector3.UNIT_Z)) {
			normal = new Vector3(-0.001, 0, 1).normalizeLocal();
		}

		if (previousNormal == null) {
			previousNormal = normal;
		}
		if (previousNormal != null && normal.dot(previousNormal) < dotE) {
			// azimuth rotation
			Matrix3 matrix = null;
			if (allowAzimuthLargeRotation && Util.isEqual(normal.multiply(1, 1, 0, null).normalizeLocal(), previousNormal.multiply(1, 1, 0, null).negateLocal().normalizeLocal())) {
				matrix = new Matrix3().fromAngleAxis(Math.PI, Vector3.UNIT_Z);
			} else if (normal.multiply(1, 1, 0, null).normalizeLocal().dot(previousNormal.multiply(1, 1, 0, null).normalizeLocal()) > -dotE) {
				matrix = findRotationMatrix(previousNormal.multiply(1, 1, 0, null).normalizeLocal(), normal.multiply(1, 1, 0, null).normalizeLocal());
			}
			if (matrix != null) {
				rotateSolarPanels(matrix);
				previousNormal = matrix.applyPost(previousNormal, null);
			}
			// tilt rotation
			rotateSolarPanels(findRotationMatrix(previousNormal, normal));
			initSolarPanelsForMove();
			previousNormal = normal;
		}
		allowAzimuthLargeRotation = false;

		if (container instanceof Foundation) {
			if (host != null && Util.isEqualFaster(normal, Vector3.UNIT_Z, 0.001)) { // compute the height of the underlying mesh
				final FloatBuffer buff = host.getMeshData().getVertexBuffer();
				Vector3 v0 = new Vector3(buff.get(0), buff.get(1), buff.get(2));
				v0 = host.getWorldTransform().applyForward(v0, null);
				baseZ = v0.getZ();
			} else {
				baseZ = container.getHeight();
			}
		} else {
			baseZ = container.getPoints().get(0).getZ();
		}
		// if (onFlatSurface && Util.isEqual(points.get(0).getZ(), baseZ)) {
		if (onFlatSurface && host == null) {
			points.get(0).setZ(baseZ + baseHeight);
		}

		final double sceneScale = Scene.getInstance().getScale();
		surround.setData(new Vector3(0, 0, 0), 0.5 * rackWidth / sceneScale, 0.5 * rackHeight / sceneScale, 0.15);
		surround.updateModelBound();

		final boolean heatMap = SceneManager.getInstance().getSolarHeatMap();
		final FloatBuffer boxVertexBuffer = surround.getMeshData().getVertexBuffer();
		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		final FloatBuffer textureBuffer = mesh.getMeshData().getTextureBuffer(0);
		final FloatBuffer outlineBuffer = outlineMesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		outlineBuffer.rewind();
		textureBuffer.rewind();
		// when the heat map is on, use a single texture from the radiation calculation, don't repeat
		final double sampleSolarPanelX = sampleSolarPanel.isRotated() ? sampleSolarPanel.getPanelHeight() : sampleSolarPanel.getPanelWidth();
		final double sampleSolarPanelY = sampleSolarPanel.isRotated() ? sampleSolarPanel.getPanelWidth() : sampleSolarPanel.getPanelHeight();
		final float spw = heatMap ? 1 : (monolithic ? (float) (rackWidth / sampleSolarPanelX) : 1);
		final float sph = heatMap ? 1 : (monolithic ? (float) (rackHeight / sampleSolarPanelY) : 1);
		int i = 8 * 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(spw).put(0);
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		i += 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(0).put(0);
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		i += 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(0).put(sph);
		textureBuffer.put(0).put(sph);
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		i += 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(spw).put(sph);
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		i = 8 * 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(spw).put(0);
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));

		mesh.updateModelBound();
		outlineMesh.updateModelBound();

		mesh.setRotation(new Matrix3().lookAt(normal, normal.getX() > 0 ? Vector3.UNIT_Z : Vector3.NEG_UNIT_Z));
		mesh.setTranslation(onFlatSurface && host != null ? getAbsPoint(0).addLocal(0, 0, baseHeight) : getAbsPoint(0));

		surround.setTranslation(mesh.getTranslation());
		surround.setRotation(mesh.getRotation());
		outlineMesh.setTranslation(mesh.getTranslation());
		outlineMesh.setRotation(mesh.getRotation());

		polesRoot.detachAllChildren();
		if (!poleInvisible) {
			final Vector3 center = getAbsPoint(0);
			final double halfRackWidth = rackWidth * 0.5;
			switch (trackerType) {
			case Trackable.NO_TRACKER:
				HousePart container = getContainerRelative();
				final Vector3 uDir = container.getPoints().get(2).subtract(container.getPoints().get(0), null).normalizeLocal();
				final Vector3 vDir = container.getPoints().get(1).subtract(container.getPoints().get(0), null).normalizeLocal();
				final Matrix3 matrix = new Matrix3().fromAngles(0, 0, az);
				matrix.applyPost(uDir, uDir);
				matrix.applyPost(vDir, vDir);
				if (vDir.dot(normal) < 0) {
					vDir.negateLocal();
				}
				final double tanTiltAngle = Math.abs(Math.tan(Math.toRadians(currentTilt)));
				if (tanTiltAngle < 100) {
					final double cosTiltAngle = Math.cos(Math.toRadians(currentTilt));
					final double poleDistanceYHorizontal = poleDistanceY * cosTiltAngle; // project to the horizontal direction
					final double rackHeightHorizontal = rackHeight * cosTiltAngle;
					final double halfRackHeightHorizontal = 0.5 * rackHeightHorizontal;
					for (double u = halfRackWidth; u < rackWidth; u += poleDistanceX) {
						for (double v = halfRackHeightHorizontal; v < rackHeightHorizontal; v += poleDistanceYHorizontal) {
							final double vFactor = (v - halfRackHeightHorizontal) / sceneScale;
							final Vector3 position = uDir.multiply((u - halfRackWidth) / sceneScale, null).addLocal(vDir.multiply(vFactor, null)).addLocal(center);
							final double dz = tanTiltAngle * vFactor;
							if (baseHeight > dz) {
								addPole(position, baseHeight - dz, baseZ);
							}
						}
						for (double v = halfRackHeightHorizontal - poleDistanceYHorizontal; v > 0; v -= poleDistanceYHorizontal) {
							final double vFactor = (v - halfRackHeightHorizontal) / sceneScale;
							final Vector3 position = uDir.multiply((u - halfRackWidth) / sceneScale, null).addLocal(vDir.multiply(vFactor, null)).addLocal(center);
							final double dz = tanTiltAngle * vFactor;
							if (baseHeight > dz) {
								addPole(position, baseHeight - dz, baseZ);
							}
						}
					}
					for (double u = halfRackWidth - poleDistanceX; u > 0; u -= poleDistanceX) {
						for (double v = halfRackHeightHorizontal; v < rackHeightHorizontal; v += poleDistanceYHorizontal) {
							final double vFactor = (v - halfRackHeightHorizontal) / sceneScale;
							final Vector3 position = uDir.multiply((u - halfRackWidth) / sceneScale, null).addLocal(vDir.multiply(vFactor, null)).addLocal(center);
							final double dz = tanTiltAngle * vFactor;
							if (baseHeight > dz) {
								addPole(position, baseHeight - dz, baseZ);
							}
						}
						for (double v = halfRackHeightHorizontal - poleDistanceYHorizontal; v > 0; v -= poleDistanceYHorizontal) {
							final double vFactor = (v - halfRackHeightHorizontal) / sceneScale;
							final Vector3 position = uDir.multiply((u - halfRackWidth) / sceneScale, null).addLocal(vDir.multiply(vFactor, null)).addLocal(center);
							final double dz = tanTiltAngle * vFactor;
							if (baseHeight > dz) {
								addPole(position, baseHeight - dz, baseZ);
							}
						}
					}
				}
				break;
			case Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER:
				polesRoot.detachAllChildren();
				container = getContainerRelative();
				final Vector3 p0 = new Vector3(vertexBuffer.get(3), vertexBuffer.get(4), vertexBuffer.get(5)); // (0, 0)
				final Vector3 p1 = new Vector3(vertexBuffer.get(0), vertexBuffer.get(1), vertexBuffer.get(2)); // (1, 0)
				final Vector3 pd = p0.subtract(p1, null).normalizeLocal();
				final int nPoles = Math.max(1, (int) (rackWidth / poleDistanceX));
				if (nPoles > 1) {
					final double halfLength = rackWidth * 0.5;
					final Vector3 qd = new Matrix3().applyRotationZ(az).applyPost(pd, null);
					for (double u = halfLength; u < rackWidth; u += poleDistanceX) {
						addPole(qd.multiply((u - halfLength) / sceneScale, null).addLocal(center), baseHeight, baseZ);
					}
					for (double u = halfLength - poleDistanceX; u > 0; u -= poleDistanceX) {
						addPole(qd.multiply((u - halfLength) / sceneScale, null).addLocal(center), baseHeight, baseZ);
					}
				} else {
					addPole(center, baseHeight, baseZ);
				}
				polesRoot.getSceneHints().setCullHint(CullHint.Inherit);
				break;
			case Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER:
			case Trackable.VERTICAL_SINGLE_AXIS_TRACKER:
				addPole(getAbsPoint(0), baseHeight, baseZ);
				break;
			}
		}
		polesRoot.getSceneHints().setCullHint(onFlatSurface ? CullHint.Inherit : CullHint.Always);

		if (drawSunBeam) {
			drawSunBeam();
		}

		drawFloatingLabel(onFlatSurface);

		if (heatMap && (rackWidth > sampleSolarPanelX || rackHeight > sampleSolarPanelY)) {
			drawSolarPanelOutlines();
		} else {
			solarPanelOutlines.setVisible(false);
		}

		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
		CollisionTreeManager.INSTANCE.removeCollisionTree(surround);
		root.updateGeometricState(0);
		drawChildren();
	}

	@Override
	public void updateLabel() {
		drawFloatingLabel(onFlatSurface());
	}

	private void drawFloatingLabel(final boolean onFlatSurface) {
		String text = "";
		if (labelCustom && labelCustomText != null) {
			text += labelCustomText;
		}
		if (labelId) {
			text += (text.equals("") ? "" : "\n") + "#" + id;
		}
		if (labelCellEfficiency) {
			text += (text.equals("") ? "" : "\n") + EnergyPanel.TWO_DECIMALS.format(100 * sampleSolarPanel.getCellEfficiency()) + "%";
		}
		if (labelTiltAngle) {
			text += (text.equals("") ? "" : "\n") + EnergyPanel.ONE_DECIMAL.format(onFlatSurface ? monthlyTiltAngles[Heliodon.getInstance().getCalendar().get(Calendar.MONTH)] : Math.toDegrees(Math.asin(normal.getY()))) + " \u00B0";
		}
		if (labelTracker) {
			final String name = getTrackerName();
			if (name != null) {
				text += (text.equals("") ? "" : "\n") + name;
			}
		}
		if (labelEnergyOutput) {
			text += (text.equals("") ? "" : "\n") + (Util.isZero(solarPotentialToday) ? "Output" : EnergyPanel.TWO_DECIMALS.format(solarPotentialToday) + " kWh");
		}
		if (!text.equals("")) {
			label.setText(text);
			final double shift = (sampleSolarPanel.isRotated() ? sampleSolarPanel.getPanelHeight() : sampleSolarPanel.getPanelWidth()) / Scene.getInstance().getScale();
			label.setTranslation((getAbsCenter()).addLocal(normal.multiply(shift, null)));
			label.setVisible(true);
		} else {
			label.setVisible(false);
		}
	}

	public String getTrackerName() {
		String name = null;
		switch (trackerType) {
		case HORIZONTAL_SINGLE_AXIS_TRACKER:
			name = "HSAT";
			break;
		case TILTED_SINGLE_AXIS_TRACKER:
			name = "TSAT";
			break;
		case VERTICAL_SINGLE_AXIS_TRACKER:
			name = "VSAT";
			break;
		case ALTAZIMUTH_DUAL_AXIS_TRACKER:
			name = "AADAT";
			break;
		}
		return name;
	}

	private void addPole(final Vector3 position, final double poleHeight, final double baseZ) {
		final MyCylinder pole = new MyCylinder("Pole Cylinder", 10, 10, 10, 0);
		pole.setRadius(0.6);
		pole.setRenderState(offsetState);
		pole.setHeight(poleHeight - 0.5 * pole.getRadius()); // slightly shorter so that the pole won't penetrate the surface of the rack
		pole.setModelBound(new BoundingBox());
		pole.updateModelBound();
		position.setZ(baseZ + pole.getHeight() / 2);
		pole.setTranslation(position);
		polesRoot.attachChild(pole);
	}

	// ensure that a rack in special cases (on a flat roof or at a tilt angle) will have correct orientation. here angle = 90 - tilt (the angle to the z axis)
	private void setNormal(final double angle, final double azimuth) {
		final Foundation foundation = getTopContainer();
		Vector3 v = foundation.getAbsPoint(0);
		final Vector3 vx = foundation.getAbsPoint(2).subtractLocal(v); // (1, 0) x direction
		final Vector3 vy = foundation.getAbsPoint(1).subtractLocal(v); // (2, 0) y direction
		if (Util.isZero(azimuth)) {
			v = new Matrix3().fromAngleAxis(angle, vx).applyPost(vy, null);
		} else {
			final Matrix3 m = new Matrix3().applyRotationZ(azimuth);
			final Vector3 v1 = m.applyPost(vx, null);
			final Vector3 v2 = m.applyPost(vy, null);
			v = new Matrix3().fromAngleAxis(angle, v1).applyPost(v2, null);
		}
		if (v.getZ() < 0) {
			v.negateLocal();
		}
		normal = v.normalizeLocal();
	}

	@Override
	public boolean isDrawable() {
		if (container == null) {
			return true;
		}
		if (mesh.getWorldBound() == null) {
			return true;
		}
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart == null || selectedPart.isDrawCompleted()) { // if nothing is really selected, skip overlap check
			return true;
		}
		final OrientedBoundingBox bound = (OrientedBoundingBox) mesh.getWorldBound().clone(null);
		bound.setExtent(bound.getExtent().divide(1.1, null).addLocal(0, 0, 1));
		for (final HousePart child : container.getChildren()) {
			if (child != this && child instanceof Rack && bound.intersects(child.mesh.getWorldBound())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void updateTextureAndColor() {
		updateTextureAndColor(mesh, ColorRGBA.LIGHT_GRAY);
	}

	@Override
	protected String getTextureFileName() {
		return monolithic ? sampleSolarPanel.getTextureFileName() : "";
	}

	@Override
	public ReadOnlyVector3 getNormal() {
		return normal;
	}

	@Override
	public boolean isPrintable() {
		return false;
	}

	@Override
	public double getGridSize() {
		return Math.min(rackWidth, rackHeight) / Scene.getInstance().getScale() / (SceneManager.getInstance().isFineGrid() ? 100.0 : 20.0);
	}

	@Override
	protected void computeArea() {
		area = rackWidth * rackHeight;
	}

	@Override
	protected HousePart getContainerRelative() {
		return container instanceof Wall ? container : getTopContainer();
	}

	@Override
	public void drawHeatFlux() {
		// this method is left empty on purpose -- don't draw heat flux
	}

	public void moveTo(final HousePart target) {
		setContainer(target);
	}

	@Override
	public boolean isCopyable() {
		return true;
	}

	private double copyOverlap() { // assume that we copy in the direction of shorter side
		final double w1 = Math.min(rackWidth, rackHeight) / Scene.getInstance().getScale();
		final Vector3 center = getAbsCenter();
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p.container == container && p != this) {
				if (p instanceof Rack) {
					final Rack s2 = (Rack) p;
					final double w2 = Math.min(s2.rackWidth, s2.rackHeight) / Scene.getInstance().getScale();
					final double distance = p.getAbsCenter().distance(center);
					if (distance < (w1 + w2) * 0.499) {
						return distance;
					}
				}
			}
		}
		return -1;
	}

	@Override
	public HousePart copy(final boolean check) {
		final Rack c = (Rack) super.copy(false);
		c.meshLocator = meshLocator; // deepy copy creates a copy of the foundation, we don't want that
		if (check) {
			normal = container.getNormal();
			if (container instanceof Foundation) {
				if (!isPositionLegal(c, (Foundation) container)) {
					return null;
				}
			} else if (container instanceof Roof) {
				if (!isPositionLegal(c, (Roof) container, !Util.isZero(container.getHeight()))) {
					return null;
				}
			} else if (container instanceof Floor) {
				if (!isPositionLegal(c, (Floor) container)) {
					return null;
				}
			}
		}
		return c;
	}

	private boolean isPositionLegal(final Rack rack, final Foundation foundation) {
		final Vector3 p0 = foundation.getAbsPoint(0);
		final Vector3 p1 = foundation.getAbsPoint(1);
		final Vector3 p2 = foundation.getAbsPoint(2);
		final double a = -Math.toRadians(relativeAzimuth) * Math.signum(p2.subtract(p0, null).getX() * p1.subtract(p0, null).getY());
		final Vector3 v = new Vector3(Math.cos(Math.PI / 2 + a), Math.sin(Math.PI / 2 + a), 0);
		double length;
		double s;
		boolean inHeight = true;
		final Rack nearest = foundation.getNearestRack(this);
		if (nearest != null) { // use the nearest rack as the reference to infer next position
			final Vector3 d = getAbsCenter().subtractLocal(nearest.getAbsCenter());
			length = d.length();
			if (rackHeight > length * Scene.getInstance().getScale()) {
				inHeight = false;
			}
			if (length > Math.min(rackWidth, rackHeight) * 5 / Scene.getInstance().getScale()) {
				length = (1 + copyLayoutGap) * rackHeight / Scene.getInstance().getScale();
				s = Math.signum(foundation.getAbsCenter().subtractLocal(Scene.getInstance().getOriginalCopy().getAbsCenter()).dot(v));
			} else {
				final double vx = v.getX();
				final double vy = v.getY();
				if (Math.abs(d.getX()) > Math.abs(d.getY())) {
					if (Math.abs(vx) < Math.abs(vy)) {
						v.setX(vy);
						v.setY(vx);
					}
				} else {
					if (Math.abs(vx) > Math.abs(vy)) {
						v.setX(vy);
						v.setY(vx);
					}
				}
				s = Math.signum(d.dot(v));
			}
		} else {
			length = (1 + copyLayoutGap) * rackHeight / Scene.getInstance().getScale();
			s = Math.signum(foundation.getAbsCenter().subtractLocal(Scene.getInstance().getOriginalCopy().getAbsCenter()).dot(v));
		}
		final double tx = length / p0.distance(p2);
		final double ty = length / p0.distance(p1);
		final double lx = s * v.getX() * tx;
		final double ly = s * v.getY() * ty;
		final double newX = points.get(0).getX() + lx;
		if (newX > 1 - 0.5 * tx || newX < 0.5 * tx) {
			return false;
		}
		final double newY = points.get(0).getY() + ly;
		if (newY > 1 - 0.5 * ty || newY < 0.5 * ty) {
			return false;
		}
		rack.points.get(0).setX(newX);
		rack.points.get(0).setY(newY);
		final double o = rack.checkCopyOverlap(inHeight);
		if (o >= 0) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, your new rack is too close to an existing one (" + o + ").", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private double checkCopyOverlap(final boolean inHeight) { // copy only in the direction of rack height
		final double w1 = (inHeight ? rackHeight : rackWidth) / Scene.getInstance().getScale();
		final Vector3 center = getAbsCenter();
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p.container == container && p != this) {
				if (p instanceof Rack) {
					final Rack s2 = (Rack) p;
					final double w2 = (inHeight ? s2.rackHeight : s2.rackWidth) / Scene.getInstance().getScale();
					final double distance = p.getAbsCenter().distance(center);
					if (distance < (w1 + w2) * 0.499) {
						return distance;
					}
				}
			}
		}
		return -1;
	}

	private boolean isPositionLegal(final Rack rack, final Roof roof, final boolean nonFlatRoof) {
		if (!nonFlatRoof) { // flat roof
			return isPositionLegal(rack, getTopContainer());
		}
		final Vector3 d = normal.cross(Vector3.UNIT_Z, null);
		d.normalizeLocal();
		if (Util.isZero(d.length())) {
			d.set(1, 0, 0);
		}
		final double s = Math.signum(roof.getAbsCenter().subtractLocal(Scene.getInstance().getOriginalCopy().getAbsCenter()).dot(d));
		d.multiplyLocal((1 + (nonFlatRoof ? 0 : copyLayoutGap)) * rackWidth / Scene.getInstance().getScale());
		d.addLocal(getContainerRelative().getPoints().get(0));
		final Vector3 v = toRelative(d);
		rack.points.get(0).setX(points.get(0).getX() + s * v.getX());
		rack.points.get(0).setY(points.get(0).getY() + s * v.getY());
		rack.points.get(0).setZ(points.get(0).getZ() + s * v.getZ());
		final boolean isOutside = !roof.insideWallsPolygon(rack.getAbsCenter());
		if (isOutside) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, you are not allowed to paste a solar panel rack outside a roof.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		final double o = rack.copyOverlap(); // TODO
		if (o >= 0) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, your new rack is too close to an existing one (" + o + ").", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	private boolean isPositionLegal(final Rack rack, final Floor floor) {
		final Vector3 d = normal.cross(Vector3.UNIT_Z, null);
		d.normalizeLocal();
		if (Util.isZero(d.length())) {
			d.set(1, 0, 0);
		}
		final double s = Math.signum(floor.getAbsCenter().subtractLocal(Scene.getInstance().getOriginalCopy().getAbsCenter()).dot(d));
		d.multiplyLocal((1 + copyLayoutGap) * rackHeight / Scene.getInstance().getScale());
		d.addLocal(getContainerRelative().getPoints().get(0));
		final Vector3 v = toRelative(d);
		rack.points.get(0).setX(points.get(0).getX() + s * v.getX());
		rack.points.get(0).setY(points.get(0).getY() + s * v.getY());
		rack.points.get(0).setZ(points.get(0).getZ() + s * v.getZ());
		final boolean isOutside = !floor.insideWallsPolygon(rack.getAbsCenter());
		if (isOutside) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, you are not allowed to paste a solar panel rack outside a floor.", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		final double o = rack.copyOverlap(); // TODO
		if (o >= 0) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, your new rack is too close to an existing one (" + o + ").", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	public void setRackWidth(final double rackWidth) {
		this.rackWidth = rackWidth;
	}

	public double getRackWidth() {
		return rackWidth;
	}

	public void setRackHeight(final double rackHeight) {
		this.rackHeight = rackHeight;
	}

	public double getRackHeight() {
		return rackHeight;
	}

	@Override
	public void setPoleHeight(final double poleHeight) {
		baseHeight = poleHeight;
	}

	@Override
	public double getPoleHeight() {
		return baseHeight;
	}

	public void setRelativeAzimuth(double relativeAzimuth) {
		if (relativeAzimuth < 0) {
			relativeAzimuth += 360;
		} else if (relativeAzimuth > 360) {
			relativeAzimuth -= 360;
		}
		this.relativeAzimuth = relativeAzimuth;
		if (outOfBound()) { // undo the rotation
			this.relativeAzimuth = oldRelativeAzimuth;
		} else {
			oldRelativeAzimuth = this.relativeAzimuth;
		}
		allowAzimuthLargeRotation = true;
	}

	public double getRelativeAzimuth() {
		return relativeAzimuth;
	}

	private void rotateSolarPanels(final Matrix3 matrix) {
		if (children.isEmpty()) {
			return;
		}
		final Vector3 center = getAbsPoint(0);
		for (final HousePart child : children) {
			final Vector3 v = child.getAbsPoint(0).subtractLocal(center);
			matrix.applyPost(v, v);
			v.addLocal(center);
			child.getPoints().get(0).set(child.toRelative(v));
		}
	}

	private Matrix3 findRotationMatrix(final ReadOnlyVector3 v1, final ReadOnlyVector3 v2) {
		final double angle = v1.smallestAngleBetween(v2);
		final Vector3 axis = v1.cross(v2, null).normalizeLocal();
		final Matrix3 matrix = new Matrix3().fromAngleAxis(angle, axis);
		return matrix;
	}

	public void setTiltAngle(final double tiltAngle) {
		this.tiltAngle = tiltAngle;
		Arrays.fill(monthlyTiltAngles, tiltAngle);
	}

	public double getTiltAngle() {
		return tiltAngle;
	}

	public void setMonthlyTiltAngles(final double[] a) {
		for (int i = 0; i < 12; i++) {
			monthlyTiltAngles[i] = a[i];
		}
	}

	public boolean areMonthlyTiltAnglesSet() {
		for (int i = 1; i < 12; i++) {
			if (!Util.isEqual(monthlyTiltAngles[0], monthlyTiltAngles[i])) {
				return true;
			}
		}
		return false;
	}

	public double[] getMonthlyTiltAngles() {
		return monthlyTiltAngles;
	}

	public double getTiltAngleOfMonth(final int month) {
		return monthlyTiltAngles[month];
	}

	@Override
	public void move(final Vector3 v, final double steplength) {
		if (lockEdit) {
			return;
		}
		v.normalizeLocal().multiplyLocal(steplength);
		final Vector3 v_rel = toRelativeVector(v);
		points.get(0).addLocal(v_rel);
		moveSolarPanels(v_rel);
		draw();
		if (outOfBound()) {
			if (oldRackCenter != null) {
				points.get(0).set(oldRackCenter);
			}
		} else {
			oldRackCenter = points.get(0).clone();
		}
	}

	public void set(final Vector3 center, final double width, final double height) {
		points.get(0).set(toRelative(center));
		setRackWidth(width);
		setRackHeight(height);
		draw();
	}

	public double getPoleDistanceX() {
		return poleDistanceX;
	}

	public void setPoleDistanceX(final double poleDistanceX) {
		this.poleDistanceX = poleDistanceX;
	}

	public double getPoleDistanceY() {
		return poleDistanceY;
	}

	public void setPoleDistanceY(final double poleDistanceY) {
		this.poleDistanceY = poleDistanceY;
	}

	public void setPoleVisible(final boolean b) {
		poleInvisible = !b;
	}

	public boolean isPoleVisible() {
		return !poleInvisible;
	}

	private void initSolarPanelsForMove() {
		moveStartPoint = getPoints().get(0).clone();
		solarOrgPoints = new ArrayList<Vector3>(children.size());
		for (final HousePart child : children) {
			solarOrgPoints.add(child.getPoints().get(0).clone());
		}
	}

	public void moveSolarPanels(final Vector3 d) {
		moveSolarPanels(d, null);
	}

	private void moveSolarPanels(final Vector3 d, final ArrayList<Vector3> orgPoints) {
		if (children.size() > 0) {
			int i = 0;
			for (final HousePart child : children) {
				if (orgPoints == null) {
					child.getPoints().get(0).addLocal(d);
				} else {
					child.getPoints().get(0).set(orgPoints.get(i++).add(d, null));
				}
			}
		}
	}

	@Override
	public void complete() {
		super.complete();
		moveStartPoint = null;
		solarOrgPoints = null;
	}

	private List<HousePart> removeAllChildren() {
		final List<HousePart> removed = new ArrayList<HousePart>();
		for (final HousePart c : children) {
			removed.add(c);
		}
		for (final HousePart x : removed) {
			Scene.getInstance().remove(x, false);
		}
		return removed;
	}

	public void addSolarPanels() {
		EnergyPanel.getInstance().updateRadiationHeatMap();
		final AddArrayCommand command = new AddArrayCommand(removeAllChildren(), this, SolarPanel.class);
		if (monolithic) {
			ensureFullSolarPanels(false);
			draw();
		} else {
			final Foundation foundation = getTopContainer();
			final double azFoundation = Math.toRadians(foundation.getAzimuth());
			if (!Util.isZero(azFoundation)) {
				foundation.rotate(azFoundation, null, false);
			}
			final double azRack = relativeAzimuth;
			setRelativeAzimuth(0);
			final boolean portrait = !sampleSolarPanel.isRotated();
			final double a = portrait ? sampleSolarPanel.getPanelWidth() : sampleSolarPanel.getPanelHeight();
			final double b = portrait ? sampleSolarPanel.getPanelHeight() : sampleSolarPanel.getPanelWidth();
			final int rows = (int) Math.floor(rackWidth / a);
			final int cols = (int) Math.floor(rackHeight / b);
			final double remainderX = rackWidth - rows * a;
			final double remainderY = rackHeight - cols * b;
			final Vector3 p0 = getAbsPoint(0);
			final double w = a / Scene.getInstance().getScale();
			final double h = b / Scene.getInstance().getScale();
			final double costilt = Math.cos(Math.toRadians(monthlyTiltAngles[Heliodon.getInstance().getCalendar().get(Calendar.MONTH)]));
			final double x0 = p0.getX() - 0.5 * (rackWidth - remainderX) / Scene.getInstance().getScale();
			final double y0 = p0.getY() - 0.5 * (rackHeight - remainderY) / Scene.getInstance().getScale() * costilt;
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					final double x = x0 + w * (r + 0.5);
					final double y = y0 + h * (c + 0.5) * costilt;
					final SolarPanel sp = new SolarPanel();
					sp.setContainer(this);
					final Vector3 v = sp.toRelative(new Vector3(x, y, 0));
					sp.points.get(0).setX(v.getX());
					sp.points.get(0).setY(v.getY());
					sp.setPanelWidth(sampleSolarPanel.getPanelWidth());
					sp.setPanelHeight(sampleSolarPanel.getPanelHeight());
					sp.setRotated(!portrait);
					Scene.getInstance().add(sp, false);
					sp.complete();
					sp.draw();
				}
			}
			if (!Util.isZero(azFoundation)) {
				foundation.rotate(-azFoundation, null, false);
			}
			setRelativeAzimuth(azRack);
			Scene.getInstance().redrawAll();
		}
		if (!command.getOldArray().isEmpty()) {
			SceneManager.getInstance().getUndoManager().addEdit(command);
		}
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				EnergyPanel.getInstance().updateProperties();
			}
		});
	}

	private boolean onFlatSurface() {
		if (meshLocator != null) { // if this rack rests on an imported mesh, treat it differently (WHY? I don't remember)
			return false;
		}
		if (container instanceof Roof) {
			if (Util.isZero(container.getHeight())) {
				return true;
			}
		} else if (container instanceof Foundation) {
			if (pickedNormal != null) {
				return Util.isEqualFaster(pickedNormal, Vector3.UNIT_Z);
			}
			return true;
		} else if (container instanceof Floor) {
			return true;
		}
		return false;
	}

	@Override
	public void setTracker(final int tracker) {
		this.trackerType = tracker;
		sampleSolarPanel.setTracker(tracker);
	}

	@Override
	public int getTracker() {
		return trackerType;
	}

	@Override
	public void updateEditShapes() {
		final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
		final ReadOnlyTransform trans = mesh.getWorldTransform();
		final Vector3 v1 = new Vector3();
		final Vector3 v2 = new Vector3();
		BufferUtils.populateFromBuffer(v1, buf, 0);
		BufferUtils.populateFromBuffer(v2, buf, 1);
		final Vector3 p1 = trans.applyForward(v1).add(trans.applyForward(v2), null).multiplyLocal(0.5);
		BufferUtils.populateFromBuffer(v1, buf, 1);
		BufferUtils.populateFromBuffer(v2, buf, 2);
		final Vector3 p2 = trans.applyForward(v1).add(trans.applyForward(v2), null).multiplyLocal(0.5);
		BufferUtils.populateFromBuffer(v1, buf, 2);
		BufferUtils.populateFromBuffer(v2, buf, 4);
		final Vector3 p3 = trans.applyForward(v1).add(trans.applyForward(v2), null).multiplyLocal(0.5);
		BufferUtils.populateFromBuffer(v1, buf, 4);
		BufferUtils.populateFromBuffer(v2, buf, 0);
		final Vector3 p4 = trans.applyForward(v1).add(trans.applyForward(v2), null).multiplyLocal(0.5);
		if (!monolithic) { // the rack may be filled with individual solar panels whose center may overlap with the handles when zooming out, so shift them out
			final Vector3 d31 = p3.subtract(p1, null).normalizeLocal();
			final Vector3 d42 = p4.subtract(p2, null).normalizeLocal();
			p1.subtractLocal(d31.multiply(2.5, null));
			p3.addLocal(d31.multiply(2.5, null));
			p2.subtractLocal(d42.multiply(2.5, null));
			p4.addLocal(d42.multiply(2.5, null));
		}
		int i = 1;
		getEditPointShape(i++).setTranslation(p1);
		getEditPointShape(i++).setTranslation(p2);
		getEditPointShape(i++).setTranslation(p3);
		getEditPointShape(i++).setTranslation(p4);
		final ReadOnlyColorRGBA c = Scene.getInstance().isGroundImageLightColored() ? ColorRGBA.DARK_GRAY : (lockEdit ? disabledColor : ColorRGBA.WHITE);
		for (i = 1; i < 5; i++) {
			getEditPointShape(i).setDefaultColor(c);
		}
		super.updateEditShapes();
		getEditPointShape(0).setTranslation(p1.addLocal(p3).multiplyLocal(0.5).addLocal(0, 0, (monolithic ? 0.15 : 1)));
	}

	private Vector3 getVertex(final int i) {
		final Vector3 v = new Vector3();
		BufferUtils.populateFromBuffer(v, mesh.getMeshData().getVertexBuffer(), i);
		return mesh.getWorldTransform().applyForward(v);
	}

	public boolean checkContainerIntersection() {
		final double z0 = (container instanceof Floor ? container.getAbsCenter().getZ() : container.getAbsCenter().getZ() + container.height) + surround.getZExtent() * 2;
		final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
		final ReadOnlyTransform trans = mesh.getWorldTransform();
		final Vector3 v1 = new Vector3();
		final Vector3 v2 = new Vector3();
		BufferUtils.populateFromBuffer(v1, buf, 0);
		BufferUtils.populateFromBuffer(v2, buf, 1);
		final Vector3 p1 = trans.applyForward(v1).add(trans.applyForward(v2), null).multiplyLocal(0.5);
		if (p1.getZ() < z0) {
			return true;
		}
		BufferUtils.populateFromBuffer(v1, buf, 1);
		BufferUtils.populateFromBuffer(v2, buf, 2);
		final Vector3 p2 = trans.applyForward(v1).add(trans.applyForward(v2), null).multiplyLocal(0.5);
		if (p2.getZ() < z0) {
			return true;
		}
		BufferUtils.populateFromBuffer(v1, buf, 2);
		BufferUtils.populateFromBuffer(v2, buf, 4);
		final Vector3 p3 = trans.applyForward(v1).add(trans.applyForward(v2), null).multiplyLocal(0.5);
		if (p3.getZ() < z0) {
			return true;
		}
		BufferUtils.populateFromBuffer(v1, buf, 4);
		BufferUtils.populateFromBuffer(v2, buf, 0);
		final Vector3 p4 = trans.applyForward(v1).add(trans.applyForward(v2), null).multiplyLocal(0.5);
		if (p4.getZ() < z0) {
			return true;
		}
		return false;
	}

	public void setMonolithic(final boolean monolithic) {
		this.monolithic = monolithic;
	}

	public boolean isMonolithic() {
		return monolithic;
	}

	public void setSolarPanel(final SolarPanel sp) {
		sampleSolarPanel = sp;
	}

	public SolarPanel getSolarPanel() {
		return sampleSolarPanel;
	}

	public void ensureFullSolarPanels(final boolean dragged) {
		if (monolithic) {
			boolean ok = false;
			if (dragged) {
				if (editPointIndex > 0) { // the rack has been resized
					ok = true;
				}
			} else {
				ok = true;
			}
			if (ok) {
				final boolean portrait = !sampleSolarPanel.isRotated();
				final double a = portrait ? sampleSolarPanel.getPanelWidth() : sampleSolarPanel.getPanelHeight();
				final double b = portrait ? sampleSolarPanel.getPanelHeight() : sampleSolarPanel.getPanelWidth();
				int nw = (int) Math.round(rackWidth / a);
				int nh = (int) Math.round(rackHeight / b);
				if (nw <= 0) {
					nw = 1;
				}
				if (nh <= 0) {
					nh = 1;
				}
				setRackWidth(nw * a);
				setRackHeight(nh * b);
				drawMesh();
				updateEditShapes();
			}
		}
	}

	void roundUpRackWidth() {
		final boolean portrait = !sampleSolarPanel.isRotated();
		final double a = portrait ? sampleSolarPanel.getPanelWidth() : sampleSolarPanel.getPanelHeight();
		int nw = (int) Math.round(rackWidth / a);
		if (nw <= 0) {
			nw = 1;
		}
		setRackWidth(nw * a);
	}

	public int[] getSolarPanelRowAndColumnNumbers() {
		final boolean portrait = !sampleSolarPanel.isRotated();
		final double a = portrait ? sampleSolarPanel.getPanelWidth() : sampleSolarPanel.getPanelHeight();
		final double b = portrait ? sampleSolarPanel.getPanelHeight() : sampleSolarPanel.getPanelWidth();
		final int nw = (int) Math.round(rackWidth / a);
		final int nh = (int) Math.round(rackHeight / b);
		return new int[] { nw, nh };
	}

	public int getNumberOfSolarPanels() {
		if (monolithic) {
			final int[] n = getSolarPanelRowAndColumnNumbers();
			return n[0] * n[1];
		}
		return 0;
	}

	@Override
	public void drawSunBeam() {
		if (Heliodon.getInstance().isNightTime() || !drawSunBeam) {
			sunBeam.setVisible(false);
			normalVector.setVisible(false);
			sunAngle.setVisible(false);
			return;
		}
		final Vector3 o = getAbsPoint(0);
		final Vector3 sunLocation = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).normalizeLocal();
		final FloatBuffer beamsVertices = sunBeam.getMeshData().getVertexBuffer();
		beamsVertices.rewind();
		Vector3 r = o.clone(); // draw sun vector
		r.addLocal(sunLocation.multiply(10000, null));
		beamsVertices.put(o.getXf()).put(o.getYf()).put(o.getZf());
		beamsVertices.put(r.getXf()).put(r.getYf()).put(r.getZf());
		sunBeam.updateModelBound();
		sunBeam.setVisible(true);
		if (bloomRenderPass == null) {
			bloomRenderPass = new BloomRenderPass(SceneManager.getInstance().getCamera(), 10);
			bloomRenderPass.setBlurIntensityMultiplier(0.5f);
			bloomRenderPass.setNrBlurPasses(2);
			SceneManager.getInstance().getPassManager().add(bloomRenderPass);
		}
		if (!bloomRenderPass.contains(sunBeam)) {
			bloomRenderPass.add(sunBeam);
		}
		final FloatBuffer normalVertices = normalVector.getMeshData().getVertexBuffer();
		normalVertices.rewind();
		r = o.clone(); // draw normal vector
		r.addLocal(normal.multiply(normalVectorLength, null));
		normalVertices.put(o.getXf()).put(o.getYf()).put(o.getZf());
		normalVertices.put(r.getXf()).put(r.getYf()).put(r.getZf());

		// draw arrows of the normal vector
		final double arrowLength = 0.75;
		final double arrowAngle = Math.toRadians(20);
		final Matrix3 matrix = new Matrix3();
		final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
		final ReadOnlyTransform trans = mesh.getWorldTransform();
		final Vector3 v1 = new Vector3();
		final Vector3 v2 = new Vector3();
		BufferUtils.populateFromBuffer(v1, buf, 1);
		BufferUtils.populateFromBuffer(v2, buf, 2);
		Vector3 a = trans.applyForward(v1).subtract(trans.applyForward(v2), null).normalizeLocal();
		a = a.crossLocal(normal);
		Vector3 s = normal.clone();
		s = matrix.fromAngleNormalAxis(arrowAngle, a).applyPost(s, null).multiplyLocal(arrowLength);
		s = r.subtract(s, null);
		normalVertices.put(r.getXf()).put(r.getYf()).put(r.getZf());
		normalVertices.put(s.getXf()).put(s.getYf()).put(s.getZf());
		s = normal.clone();
		s = matrix.fromAngleNormalAxis(-arrowAngle, a).applyPost(s, null).multiplyLocal(arrowLength);
		s = r.subtract(s, null);
		normalVertices.put(r.getXf()).put(r.getYf()).put(r.getZf());
		normalVertices.put(s.getXf()).put(s.getYf()).put(s.getZf());

		// draw the angle between the sun beam and the normal vector
		normal.cross(sunLocation, a);
		sunAngle.setRange(o, o.add(sunLocation, null), o.add(normal, null), a);
		sunAngle.setVisible(true);

		normalVector.updateModelBound();
		normalVector.setVisible(true);
	}

	// solar panels would vanish in the single heat map texture without drawing these lines
	private void drawSolarPanelOutlines() {
		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		final ReadOnlyTransform trans = mesh.getTransform(); // do not use WorldTransform
		final Vector3 p0 = trans.applyForward(new Vector3(vertexBuffer.get(3), vertexBuffer.get(4), vertexBuffer.get(5))); // (0, 0)
		final Vector3 p1 = trans.applyForward(new Vector3(vertexBuffer.get(6), vertexBuffer.get(7), vertexBuffer.get(8))); // (1, 0)
		final Vector3 p2 = trans.applyForward(new Vector3(vertexBuffer.get(0), vertexBuffer.get(1), vertexBuffer.get(2))); // (0, 1)
		final boolean portrait = !sampleSolarPanel.isRotated();
		double a = portrait ? sampleSolarPanel.getPanelWidth() : sampleSolarPanel.getPanelHeight();
		double b = portrait ? sampleSolarPanel.getPanelHeight() : sampleSolarPanel.getPanelWidth();
		final int nw = (int) Math.round(rackWidth / a);
		final int nh = (int) Math.round(rackHeight / b);
		a /= Scene.getInstance().getScale();
		b /= Scene.getInstance().getScale();
		final int bufferSize = (nw + nh - 2) * 6;
		FloatBuffer vertices = solarPanelOutlines.getMeshData().getVertexBuffer();
		if (vertices.capacity() != bufferSize) {
			vertices = BufferUtils.createFloatBuffer(bufferSize);
			solarPanelOutlines.getMeshData().setVertexBuffer(vertices);
		} else {
			vertices.rewind();
			vertices.limit(vertices.capacity());
		}
		final Vector3 u = p1.subtract(p0, null).normalizeLocal().multiplyLocal(b);
		final Vector3 v = p2.subtract(p0, null).normalizeLocal().multiplyLocal(a);
		Vector3 p, q;
		for (int i = 1; i < nw; i++) {
			q = v.multiply(i, null);
			p = p0.add(q, null);
			vertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
			p = p1.add(q, null);
			vertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
		}
		for (int i = 1; i < nh; i++) {
			q = u.multiply(i, null);
			p = p0.add(q, null);
			vertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
			p = p2.add(q, null);
			vertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
		}
		solarPanelOutlines.updateModelBound();
		solarPanelOutlines.setVisible(true);
	}

	@Override
	public void delete() {
		super.delete();
		if (bloomRenderPass != null) {
			if (bloomRenderPass.contains(sunBeam)) {
				bloomRenderPass.remove(sunBeam);
			}
		}
	}

	@Override
	public void setSunBeamVisible(final boolean drawSunBeam) {
		this.drawSunBeam = drawSunBeam;
	}

	@Override
	public boolean isSunBeamVisible() {
		return drawSunBeam;
	}

	@Override
	public double getYieldNow() {
		return yieldNow;
	}

	@Override
	public void setYieldNow(final double yieldNow) {
		this.yieldNow = yieldNow;
	}

	@Override
	public double getYieldToday() {
		return yieldToday;
	}

	@Override
	public void setYieldToday(final double yieldToday) {
		this.yieldToday = yieldToday;
	}

	@Override
	public MeshLocator getMeshLocator() {
		return meshLocator;
	}

	@Override
	public void setMeshLocator(final MeshLocator meshLocator) {
		this.meshLocator = meshLocator;
	}

	@Override
	public void clearLabels() {
		super.clearLabels();
		labelCellEfficiency = false;
		labelTiltAngle = false;
		labelTracker = false;
		labelEnergyOutput = false;
	}

	public boolean isLabelVisible() {
		return label.isVisible();
	}

	public void setLabelTracker(final boolean labelTracker) {
		this.labelTracker = labelTracker;
	}

	public boolean getLabelTracker() {
		return labelTracker;
	}

	public void setLabelCellEfficiency(final boolean labelCellEfficiency) {
		this.labelCellEfficiency = labelCellEfficiency;
	}

	public boolean getLabelCellEfficiency() {
		return labelCellEfficiency;
	}

	public void setLabelTiltAngle(final boolean labelTiltAngle) {
		this.labelTiltAngle = labelTiltAngle;
	}

	public boolean getLabelTiltAngle() {
		return labelTiltAngle;
	}

	public void setLabelEnergyOutput(final boolean labelEnergyOutput) {
		this.labelEnergyOutput = labelEnergyOutput;
	}

	public boolean getLabelEnergyOutput() {
		return labelEnergyOutput;
	}

	@Override
	protected void addPrintMesh(final List<Mesh> list, final Mesh mesh) {
		if (mesh.getSceneHints().getCullHint() != CullHint.Always) {
			final Mesh newMesh = mesh.makeCopy(false);
			final MaterialState material = new MaterialState();
			switch (sampleSolarPanel.getColorOption()) {
			case SolarPanel.COLOR_OPTION_BLACK:
				material.setDiffuse(ColorRGBA.BLACK);
				break;
			case SolarPanel.COLOR_OPTION_BLUE:
				material.setDiffuse(ColorRGBA.BLUE);
				break;
			case SolarPanel.COLOR_OPTION_GRAY:
				material.setDiffuse(ColorRGBA.GRAY);
				break;
			default:
				material.setDiffuse(mesh.getDefaultColor());
			}
			newMesh.setRenderState(material);
			newMesh.getMeshData().transformVertices((Transform) mesh.getWorldTransform());
			newMesh.getMeshData().transformNormals((Transform) mesh.getWorldTransform(), true);
			list.add(newMesh);
		}
	}

	@Override
	public void addPrintMeshes(final List<Mesh> list) {
		addPrintMesh(list, surround);
		for (final Spatial pole : this.polesRoot.getChildren()) {
			addPrintMesh(list, (Mesh) pole);
		}
	}

}