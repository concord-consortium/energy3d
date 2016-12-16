package org.concord.energy3d.model;

import java.awt.EventQueue;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.undo.AddArrayCommand;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.extension.effect.bloom.BloomRenderPass;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyTransform;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.OffsetState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.scenegraph.shape.Cylinder;
import com.ardor3d.util.geom.BufferUtils;

public class Rack extends HousePart implements Trackable {
	private static final long serialVersionUID = 1L;
	private transient ArrayList<Vector3> solarOrgPoints;
	private transient ReadOnlyVector3 normal;
	private transient Mesh outlineMesh;
	private transient Box surround;
	private transient Node polesRoot;
	private transient Vector3 moveStartPoint;
	private transient double layoutGap = 0.01;
	private transient boolean allowAzimuthLargeRotation;
	private ReadOnlyVector3 previousNormal;
	private double rackWidth = 15;
	private double rackHeight = 3;
	private double relativeAzimuth = 0;
	private double tiltAngle = -25;
	private double baseHeight = 15;
	private double poleDistanceX = 4;
	private double poleDistanceY = 2;
	private int trackerType = NO_TRACKER;
	private int rotationAxis;
	private boolean monolithic; // true if the whole rack is covered by solar panels
	private SolarPanel sampleSolarPanel;
	private transient Vector3 oldRackCenter;
	private transient double oldRackWidth, oldRackHeight;
	private boolean drawSunBeam;
	private transient Line sunBeam;
	private transient Line normalVector;
	private static transient BloomRenderPass bloomRenderPass;

	public Rack() {
		super(1, 1, 0);
	}

	@Override
	protected void init() {
		super.init();
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
		offset.setFactor(1);
		offset.setUnits(1);
		surround.setRenderState(offset);
		root.attachChild(surround);

		outlineMesh = new Line("Rack (Outline)");
		outlineMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(8));
		outlineMesh.setDefaultColor(ColorRGBA.BLACK);
		outlineMesh.setModelBound(new OrientedBoundingBox());
		root.attachChild(outlineMesh);

		sunBeam = new Line("Sun Beam");
		sunBeam.setLineWidth(0.01f);
		sunBeam.setStipplePattern((short) 0xffff);
		sunBeam.setModelBound(null);
		Util.disablePickShadowLight(sunBeam);
		sunBeam.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		sunBeam.setDefaultColor(new ColorRGBA(1f, 1f, 1f, 1f));
		root.attachChild(sunBeam);

		normalVector = new Line("Normal Vector");
		normalVector.setLineWidth(0.01f);
		normalVector.setStipplePattern((short) 0xffff);
		normalVector.setModelBound(null);
		Util.disablePickShadowLight(normalVector);
		normalVector.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		normalVector.setDefaultColor(new ColorRGBA(1f, 1f, 0f, 1f));
		root.attachChild(normalVector);

		polesRoot = new Node("Poles Root");
		root.attachChild(polesRoot);
		updateTextureAndColor();

		if (sampleSolarPanel == null) {
			sampleSolarPanel = new SolarPanel();
		}

		oldRackCenter = points.get(0).clone();
		oldRackWidth = rackWidth;
		oldRackHeight = rackHeight;
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		if (moveStartPoint == null) {
			initSolarPanelsForMove();
		}
		if (editPointIndex <= 0) {
			final PickedHousePart picked = pickContainer(x, y, new Class<?>[] { Foundation.class, Roof.class });
			if (picked != null) {
				final Vector3 p = picked.getPoint().clone();
				snapToGrid(p, getAbsPoint(0), getGridSize(), false);
				points.get(0).set(toRelative(p));
			}
			if (outOfBound()) {
				if (oldRackCenter != null) {
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
				final double rw = p.distance(p1) * Scene.getInstance().getAnnotationScale();
				final double pw = sampleSolarPanel.isRotated() ? sampleSolarPanel.getPanelHeight() : sampleSolarPanel.getPanelWidth();
				if (rw > pw) {
					final Vector3 newCenter = toRelative(p.add(p1, null).multiplyLocal(0.5));
					getEditPointShape(editPointIndex).setTranslation(p);
					points.get(0).set(newCenter);
					setRackWidth(Math.max(rw, pw));
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
			} else {
				final ReadOnlyVector3 p1 = getEditPointShape(editPointIndex == 1 ? 3 : 1).getTranslation();
				p = Util.closestPoint(pEdit, pEdit.subtract(p1, null).normalizeLocal(), x, y);
				final double rh = p.distance(p1) * Scene.getInstance().getAnnotationScale();
				final double ph = sampleSolarPanel.isRotated() ? sampleSolarPanel.getPanelWidth() : sampleSolarPanel.getPanelHeight();
				if (rh > ph) {
					final Vector3 newCenter = toRelative(p.add(p1, null).multiplyLocal(0.5));
					getEditPointShape(editPointIndex).setTranslation(p);
					points.get(0).set(newCenter);
					setRackHeight(Math.max(rh, ph));
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
		}
		return false;
	}

	@Override
	protected void drawMesh() {
		if (container == null) {
			return;
		}

		final boolean onFlatSurface = onFlatSurface();
		getEditPointShape(0).setDefaultColor(ColorRGBA.ORANGE);
		normal = computeNormalAndKeepOnRoof();

		final double dotE = 0.9999;
		switch (trackerType) {
		case ALTAZIMUTH_DUAL_AXIS_TRACKER:
			normal = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).normalize(null);
			break;
		case HORIZONTAL_SINGLE_AXIS_TRACKER:
			int xRotationAxis = 1;
			int yRotationAxis = 0;
			switch (rotationAxis) {
			case EAST_WEST_AXIS:
				xRotationAxis = 0;
				yRotationAxis = 1;
				break;
			}
			normal = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).multiply(xRotationAxis, yRotationAxis, 1, null).normalize(null);
			break;
		case VERTICAL_SINGLE_AXIS_TRACKER:
			final Vector3 a = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).multiply(1, 1, 0, null).normalizeLocal();
			final Vector3 b = Vector3.UNIT_Z.cross(a, null);
			final Matrix3 m = new Matrix3().applyRotation(Math.toRadians(90 - tiltAngle), b.getX(), b.getY(), b.getZ());
			normal = m.applyPost(a, null);
			if (normal.getZ() < 0) {
				normal = normal.negate(null);
			}
			break;
		default:
			if (onFlatSurface) {
				setNormal(Util.isZero(tiltAngle) ? Math.PI / 2 * dotE : Math.toRadians(90 - tiltAngle), Math.toRadians(relativeAzimuth)); // exactly 90 degrees will cause the solar panel to disappear
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

		final double baseZ;
		if (this.container instanceof Foundation) {
			baseZ = this.container.getHeight();
		} else {
			baseZ = this.container.getPoints().get(0).getZ();
		}

		if (onFlatSurface) {
			points.get(0).setZ(baseZ + baseHeight);
		}

		final double annotationScale = Scene.getInstance().getAnnotationScale();
		surround.setData(new Vector3(0, 0, 0), rackWidth / 2.0 / annotationScale, rackHeight / 2.0 / annotationScale, 0.15);
		surround.updateModelBound();

		final FloatBuffer boxVertexBuffer = surround.getMeshData().getVertexBuffer();
		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		final FloatBuffer textureBuffer = mesh.getMeshData().getTextureBuffer(0);
		final FloatBuffer outlineBuffer = outlineMesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		outlineBuffer.rewind();
		textureBuffer.rewind();
		final float spw = monolithic ? (float) (rackWidth / (sampleSolarPanel.isRotated() ? sampleSolarPanel.getPanelHeight() : sampleSolarPanel.getPanelWidth())) : 1;
		final float sph = monolithic ? (float) (rackHeight / (sampleSolarPanel.isRotated() ? sampleSolarPanel.getPanelWidth() : sampleSolarPanel.getPanelHeight())) : 1;
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
		mesh.setTranslation(getAbsPoint(0));

		surround.setTranslation(mesh.getTranslation());
		surround.setRotation(mesh.getRotation());
		outlineMesh.setTranslation(mesh.getTranslation());
		outlineMesh.setRotation(mesh.getRotation());

		polesRoot.detachAllChildren();
		final Vector3 center = getAbsPoint(0);
		final Matrix3 matrix = new Matrix3().fromAngles(0, 0, -Math.toRadians(relativeAzimuth));
		switch (trackerType) {
		case Trackable.NO_TRACKER:
			final HousePart container = getContainerRelative();
			final Vector3 uDir = container.getPoints().get(2).subtract(container.getPoints().get(0), null).normalizeLocal();
			final Vector3 vDir = container.getPoints().get(1).subtract(container.getPoints().get(0), null).normalizeLocal();
			matrix.applyPost(uDir, uDir);
			matrix.applyPost(vDir, vDir);
			if (vDir.dot(normal) < 0) {
				vDir.negateLocal();
			}
			final double tanTiltAngle = Math.abs(Math.tan(Math.toRadians(tiltAngle)));
			for (double u = poleDistanceX; u < rackWidth; u += poleDistanceX) {
				for (double v = poleDistanceY; v < rackHeight; v += poleDistanceY) {
					final double vFactor = (v - rackHeight / 2) / annotationScale;
					final Vector3 position = uDir.multiply((u - rackWidth / 2) / annotationScale, null).addLocal(vDir.multiply(vFactor, null)).addLocal(center);
					final double dz = tanTiltAngle * vFactor;

					final Cylinder pole = new Cylinder("Pole Cylinder", 10, 10, 10, 0);
					pole.setRadius(1);
					pole.setRenderState(offsetState);
					pole.setHeight(baseHeight - dz - 0.1);
					pole.setModelBound(new BoundingBox());
					pole.updateModelBound();
					position.setZ(baseZ + pole.getHeight() / 2);

					pole.setTranslation(position);
					polesRoot.attachChild(pole);
				}
			}
			break;
		case Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER:
			final Vector3 p0 = new Vector3(vertexBuffer.get(3), vertexBuffer.get(4), vertexBuffer.get(5)); // (0, 0)
			final Vector3 p1 = new Vector3(vertexBuffer.get(6), vertexBuffer.get(7), vertexBuffer.get(8)); // (1, 0)
			// final Vector3 p2 = new Vector3(vertexBuffer.get(0), vertexBuffer.get(1), vertexBuffer.get(2)); // (0, 1)
			final Vector3 p10 = p1.subtract(p0, null).normalizeLocal();
			matrix.applyPost(p10, p10);
			for (double u = poleDistanceX; u < rackWidth; u += poleDistanceX) {
				final Vector3 position = p10.multiply((u - rackWidth / 2) / annotationScale, null).addLocal(center);
				final Cylinder pole = new Cylinder("Pole Cylinder", 10, 10, 10, 0);
				pole.setRadius(1);
				pole.setRenderState(offsetState);
				pole.setHeight(baseHeight - 0.1);
				pole.setModelBound(new BoundingBox());
				pole.updateModelBound();
				position.setZ(baseZ + pole.getHeight() / 2);
				pole.setTranslation(position);
				polesRoot.attachChild(pole);
			}
			break;
		case Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER:
		case Trackable.VERTICAL_SINGLE_AXIS_TRACKER:
			final Cylinder pole = new Cylinder("Pole Cylinder", 10, 10, 10, 0);
			pole.setRadius(1);
			pole.setRenderState(offsetState);
			pole.setHeight(baseHeight - 0.1);
			pole.setModelBound(new BoundingBox());
			pole.updateModelBound();
			pole.setHeight(baseHeight - 0.5 * pole.getRadius());
			pole.setTranslation(getAbsPoint(0).addLocal(0, 0, pole.getHeight() / 2 - baseHeight));
			polesRoot.attachChild(pole);
			break;
		}
		polesRoot.getSceneHints().setCullHint(onFlatSurface ? CullHint.Inherit : CullHint.Always);

		if (drawSunBeam) {
			drawSunBeam();
		}

		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
		CollisionTreeManager.INSTANCE.removeCollisionTree(surround);
		root.updateGeometricState(0);
		drawChildren();
	}

	// ensure that a mirror in special cases (on a flat roof or at a tilt angle) will have correct orientation
	private void setNormal(final double angle, final double azimuth) {
		final Foundation foundation = getTopContainer();
		Vector3 v = foundation.getAbsPoint(0);
		final Vector3 vx = foundation.getAbsPoint(2).subtractLocal(v); // x direction
		final Vector3 vy = foundation.getAbsPoint(1).subtractLocal(v); // y direction
		final Matrix3 m = new Matrix3().applyRotationZ(-azimuth);
		final Vector3 v1 = m.applyPost(vx, null);
		final Vector3 v2 = m.applyPost(vy, null);
		v = new Matrix3().fromAngleAxis(angle, v1).applyPost(v2, null);
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
		updateTextureAndColor(mesh, ColorRGBA.LIGHT_GRAY, monolithic ? TextureMode.Full : TextureMode.None);
	}

	@Override
	protected String getTextureFileName() {
		if (monolithic) {
			return sampleSolarPanel.isRotated() ? "solarpanel-rotated.png" : "solarpanel.png";
		}
		return "";
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
		return Math.min(rackWidth, rackHeight) / Scene.getInstance().getAnnotationScale() / (SceneManager.getInstance().isFineGrid() ? 25.0 : 5.0);
	}

	@Override
	protected void computeArea() {
		area = rackWidth * rackHeight;
	}

	@Override
	protected HousePart getContainerRelative() {
		return getTopContainer();
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

	private double overlap() {
		final double w1 = rackWidth / Scene.getInstance().getAnnotationScale();
		final Vector3 center = getAbsCenter();
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p.container == container && p != this) {
				if (p instanceof Rack) {
					final Rack s2 = (Rack) p;
					final double w2 = s2.rackWidth / Scene.getInstance().getAnnotationScale();
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
		if (check) {
			normal = container.getNormal();
			if (container instanceof Foundation) {
				final Vector3 p0 = container.getAbsPoint(0);
				final Vector3 p1 = container.getAbsPoint(1);
				final Vector3 p2 = container.getAbsPoint(2);
				final double a = -Math.toRadians(relativeAzimuth) * Math.signum(p2.subtract(p0, null).getX() * p1.subtract(p0, null).getY());
				final Vector3 v = new Vector3(Math.cos(a), Math.sin(a), 0);
				final double length = (1 + layoutGap) * rackWidth / Scene.getInstance().getAnnotationScale();
				final double s = Math.signum(container.getAbsCenter().subtractLocal(Scene.getInstance().getOriginalCopy().getAbsCenter()).dot(v));
				final double tx = length / p0.distance(p2);
				final double ty = length / p0.distance(p1);
				final double lx = s * v.getX() * tx;
				final double ly = s * v.getY() * ty;
				final double newX = points.get(0).getX() + lx;
				if (newX > 1 - tx || newX < tx) {
					return null;
				}
				final double newY = points.get(0).getY() + ly;
				if (newY > 1 - ty || newY < ty) {
					return null;
				}
				c.points.get(0).setX(newX);
				c.points.get(0).setY(newY);
				final double o = c.overlap();
				if (o >= 0) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, your new rack is too close to an existing one (" + o + ").", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			}
		}
		return c;
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

	public void setBaseHeight(final double baseHeight) {
		this.baseHeight = baseHeight;
	}

	public double getBaseHeight() {
		return baseHeight;
	}

	private transient double oldRelativeAzimuth;

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

	private void rotateSolarPanelsAzimuth(final double angle) {
		final Vector3 center = getAbsPoint(0);
		final Matrix3 matrix = new Matrix3().fromAngles(0, 0, angle);
		for (final HousePart child : children) {
			final Vector3 v = child.getAbsPoint(0).subtractLocal(center);
			matrix.applyPost(v, v);
			v.addLocal(center);
			child.getPoints().get(0).set(child.toRelative(v));
		}
	}

	public double getRelativeAzimuth() {
		return relativeAzimuth;
	}

	public void setTiltAngle(final double tiltAngle) {
		this.tiltAngle = tiltAngle;
	}

	private void rotateSolarPanels(final Matrix3 matrix) {
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

	public double getTiltAngle() {
		return tiltAngle;
	}

	public void move(final Vector3 v, final double steplength) {
		v.normalizeLocal();
		v.multiplyLocal(steplength);
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
		int i = 0;
		for (final HousePart child : children) {
			if (orgPoints == null) {
				child.getPoints().get(0).addLocal(d);
			} else {
				child.getPoints().get(0).set(orgPoints.get(i++).add(d, null));
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
		EnergyPanel.getInstance().clearRadiationHeatMap();
		final AddArrayCommand command = new AddArrayCommand(removeAllChildren(), this, SolarPanel.class);
		final ArrayList<HousePart> c0 = new ArrayList<HousePart>(children);
		for (final HousePart c : c0) { // make a copy to avoid concurrent modification
			Scene.getInstance().remove(c, false);
		}
		if (monolithic) {
			ensureFullSolarPanels();
			draw();
		} else {
			final Foundation foundation = getTopContainer();
			final double azFoundation = Math.toRadians(foundation.getAzimuth());
			if (!Util.isZero(azFoundation)) {
				foundation.rotate(azFoundation, null);
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
			final double w = a / Scene.getInstance().getAnnotationScale();
			final double h = b / Scene.getInstance().getAnnotationScale();
			final double costilt = Math.cos(Math.toRadians(tiltAngle));
			final double x0 = p0.getX() - 0.5 * (rackWidth - remainderX) / Scene.getInstance().getAnnotationScale();
			final double y0 = p0.getY() - 0.5 * (rackHeight - remainderY) / Scene.getInstance().getAnnotationScale() * costilt;
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
				foundation.rotate(-azFoundation, null);
			}
			setRelativeAzimuth(azRack);
			Scene.getInstance().redrawAll();
		}
		SceneManager.getInstance().getUndoManager().addEdit(command);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				EnergyPanel.getInstance().updateProperties();
			}
		});
	}

	private boolean onFlatSurface() {
		if (container instanceof Roof) {
			if (Util.isZero(container.getHeight())) {
				return true;
			}
		} else if (container instanceof Foundation) {
			return true;
		}
		return false;
	}

	@Override
	public void setTracker(final int tracker) {
		this.trackerType = tracker;
	}

	@Override
	public int getTracker() {
		return trackerType;
	}

	@Override
	public void setRotationAxis(final int rotationAxis) {
		this.rotationAxis = rotationAxis;
	}

	@Override
	public int getRotationAxis() {
		return rotationAxis;
	}

	@Override
	public void updateEditShapes() {
		final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
		final ReadOnlyTransform trans = mesh.getWorldTransform();
		final Vector3 v1 = new Vector3();
		final Vector3 v2 = new Vector3();
		int i = 1;
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
		getEditPointShape(i++).setTranslation(p1);
		getEditPointShape(i++).setTranslation(p2);
		getEditPointShape(i++).setTranslation(p3);
		getEditPointShape(i++).setTranslation(p4);
		super.updateEditShapes();
		getEditPointShape(0).setTranslation(getAbsPoint(0).addLocal(0, 0, 1));
	}

	private Vector3 getVertex(final int i) {
		final Vector3 v = new Vector3();
		BufferUtils.populateFromBuffer(v, mesh.getMeshData().getVertexBuffer(), i);
		return mesh.getWorldTransform().applyForward(v);
	}

	public void setMonolithic(final boolean monolithic) {
		this.monolithic = monolithic;
	}

	public boolean isMonolithic() {
		return monolithic;
	}

	public SolarPanel getSolarPanel() {
		return sampleSolarPanel;
	}

	public void ensureFullSolarPanels() {
		if (monolithic) {
			if (editPointIndex > 0) { // the rack has been resized
				final boolean portrait = !sampleSolarPanel.isRotated();
				final double a = portrait ? sampleSolarPanel.getPanelWidth() : sampleSolarPanel.getPanelHeight();
				final double b = portrait ? sampleSolarPanel.getPanelHeight() : sampleSolarPanel.getPanelWidth();
				final int nw = (int) Math.floor(rackWidth / a);
				final int nh = (int) Math.floor(rackHeight / b);
				setRackWidth(nw * a);
				setRackHeight(nh * b);
				drawMesh();
			}
		}
	}

	public int getSolarPanelCount() {
		final boolean portrait = !sampleSolarPanel.isRotated();
		final double a = portrait ? sampleSolarPanel.getPanelWidth() : sampleSolarPanel.getPanelHeight();
		final double b = portrait ? sampleSolarPanel.getPanelHeight() : sampleSolarPanel.getPanelWidth();
		final int nw = (int) Math.floor(rackWidth / a);
		final int nh = (int) Math.floor(rackHeight / b);
		return nw * nh;
	}

	public void drawSunBeam() {
		if (Heliodon.getInstance().isNightTime() || !drawSunBeam) {
			sunBeam.setVisible(false);
			normalVector.setVisible(false);
			return;
		}
		final Vector3 o = getAbsPoint(0).addLocal(0, 0, baseHeight);
		final Vector3 sunLocation = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).normalize(null);
		final FloatBuffer beamsVertices = sunBeam.getMeshData().getVertexBuffer();
		beamsVertices.rewind();
		Vector3 r = o.clone(); // draw sun vector
		r.addLocal(sunLocation.multiply(5000, null));
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
		r.addLocal(normal.multiply(5, null));
		normalVertices.put(o.getXf()).put(o.getYf()).put(o.getZf());
		normalVertices.put(r.getXf()).put(r.getYf()).put(r.getZf());
		// TODO final Vector3 s = new Vector3(r);
		// normalVertices.put(r.getXf()).put(r.getYf()).put(r.getZf());
		// normalVertices.put(s.getXf()).put(s.getYf()).put(s.getZf());
		normalVector.updateModelBound();
		normalVector.setVisible(true);
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

	public void setSunBeamVisible(final boolean drawSunBeam) {
		this.drawSunBeam = drawSunBeam;
	}

	public boolean isDrawSunBeamVisible() {
		return drawSunBeam;
	}

}
