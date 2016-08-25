package org.concord.energy3d.model;

import java.nio.FloatBuffer;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.Atmosphere;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.extension.effect.bloom.BloomRenderPass;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Camera.ProjectionMode;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.OffsetState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.geom.BufferUtils;

public class SolarPanel extends HousePart {

	private static final long serialVersionUID = 1L;

	public static final int NO_TRACKER = 0;
	public static final int HORIZONTAL_SINGLE_AXIS_TRACKER = 1;
	public static final int ALTAZIMUTH_DUAL_AXIS_TRACKER = 2;
	public static final int VERTICAL_SINGLE_AXIS_TRACKER = 3;

	public static final int PARTIAL_SHADE_TOLERANCE = 0;
	public static final int HIGH_SHADE_TOLERANCE = 1;
	public static final int NO_SHADE_TOLERANCE = 2;

	private transient ReadOnlyVector3 normal;
	private transient Mesh outlineMesh;
	private transient Box surround;
	private transient Mesh supportFrame;
	private transient Line sunBeam;
	private double efficiency = 0.15; // a number in (0, 1)
	private double inverterEfficiency = 0.95;
	private double panelWidth = 0.99; // 39"
	private double panelHeight = 1.65; // 65"
	private boolean rotated = false; // rotation around the normal usually takes only two angles: 0 or 90, so we use a boolean here
	private double relativeAzimuth;
	private double tiltAngle;
	private int trackerType = NO_TRACKER;
	private double baseHeight = 6;
	private boolean drawSunBeam;
	private int rotationAxis;
	private int shadeTolerance = HIGH_SHADE_TOLERANCE;
	private int numberOfCellsInX = 6;
	private int numberOfCellsInY = 10;
	private transient double layoutGap = 0.01;
	private static transient BloomRenderPass bloomRenderPass;

	public SolarPanel(final boolean rotated) {
		super(1, 1, 0);
	}

	@Override
	protected void init() {
		super.init();

		if (Util.isZero(panelWidth)) {
			panelWidth = 0.99;
		}
		if (Util.isZero(panelHeight)) {
			panelHeight = 1.65;
		}
		if (Util.isZero(efficiency)) {
			efficiency = 0.15;
		}
		if (Util.isZero(inverterEfficiency)) {
			inverterEfficiency = 0.95;
		}
		if (Util.isZero(baseHeight)) {
			baseHeight = 6;
		}
		if (Util.isZero(numberOfCellsInX)) {
			numberOfCellsInX = 6;
		}
		if (Util.isZero(numberOfCellsInY)) {
			numberOfCellsInY = 10;
		}

		mesh = new Mesh("SolarPanel");
		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(6));
		mesh.getMeshData().setTextureBuffer(BufferUtils.createVector2Buffer(6), 0);
		mesh.setModelBound(new OrientedBoundingBox());
		mesh.setUserData(new UserData(this));
		root.attachChild(mesh);

		surround = new Box("SolarPanel (Surround)");
		surround.setModelBound(new OrientedBoundingBox());
		final OffsetState offset = new OffsetState();
		offset.setFactor(1);
		offset.setUnits(1);
		surround.setRenderState(offset);
		root.attachChild(surround);

		outlineMesh = new Line("SolarPanel (Outline)");
		outlineMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(8));
		outlineMesh.setDefaultColor(ColorRGBA.BLACK);
		outlineMesh.setModelBound(new OrientedBoundingBox());
		root.attachChild(outlineMesh);

		supportFrame = new Mesh("Supporting Frame");
		supportFrame.getMeshData().setIndexMode(IndexMode.Quads);
		supportFrame.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(12));
		supportFrame.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(12));
		supportFrame.setRenderState(offsetState);
		supportFrame.setModelBound(new BoundingBox());
		root.attachChild(supportFrame);

		sunBeam = new Line("Sun Beam");
		sunBeam.setLineWidth(0.01f);
		sunBeam.setStipplePattern((short) 0xffff);
		sunBeam.setModelBound(null);
		Util.disablePickShadowLight(sunBeam);
		sunBeam.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		sunBeam.setDefaultColor(new ColorRGBA(1f, 1f, 1f, 1f));
		root.attachChild(sunBeam);

		updateTextureAndColor();

	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		final PickedHousePart picked = pickContainer(x, y, new Class<?>[] { Roof.class, Wall.class, Foundation.class });
		if (picked != null) {
			final Vector3 p = picked.getPoint().clone();
			snapToGrid(p, getAbsPoint(0), getGridSize(), container instanceof Wall);
			points.get(0).set(toRelative(p));
		}
		if (container != null) {
			draw();
			setEditPointsVisible(true);
			setHighlight(!isDrawable());
		}
	}

	@Override
	public void updateEditShapes() {
		final Vector3 p = Vector3.fetchTempInstance();
		try {
			for (int i = 0; i < points.size(); i++) {
				getAbsPoint(i, p);
				final Camera camera = SceneManager.getInstance().getCamera();
				if (camera != null && camera.getProjectionMode() != ProjectionMode.Parallel) {
					final double distance = camera.getLocation().distance(p);
					getEditPointShape(i).setScale(distance > 0.1 ? distance / 10 : 0.01);
				} else {
					getEditPointShape(i).setScale(camera.getFrustumTop() / 4);
				}
				if (onFlatSurface()) {
					p.setZ(p.getZ() + baseHeight);
				}
				getEditPointShape(i).setTranslation(p);
			}
		} finally {
			Vector3.releaseTempInstance(p);
		}
		/* remove remaining edit shapes */
		for (int i = points.size(); i < pointsRoot.getNumberOfChildren(); i++) {
			pointsRoot.detachChildAt(points.size());
		}
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
	protected void drawMesh() {

		if (container == null) {
			return;
		}

		normal = computeNormalAndKeepOnRoof();
		updateEditShapes();

		final double annotationScaleFactor = 0.5 / Scene.getInstance().getAnnotationScale();
		if (rotated) {
			surround.setData(new Vector3(), panelHeight * annotationScaleFactor, panelWidth * annotationScaleFactor, 0.15);
		} else {
			surround.setData(new Vector3(), panelWidth * annotationScaleFactor, panelHeight * annotationScaleFactor, 0.15);
		}
		surround.updateModelBound();

		final FloatBuffer boxVertexBuffer = surround.getMeshData().getVertexBuffer();
		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		final FloatBuffer textureBuffer = mesh.getMeshData().getTextureBuffer(0);
		final FloatBuffer outlineBuffer = outlineMesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		outlineBuffer.rewind();
		textureBuffer.rewind();
		int i = 8 * 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(1).put(0);
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		i += 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(0).put(0);
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		i += 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(0).put(1);
		textureBuffer.put(0).put(1);
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		i += 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(1).put(1);
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		i = 8 * 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(1).put(0);
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));

		mesh.updateModelBound();
		outlineMesh.updateModelBound();

		final boolean onFlatSurface = onFlatSurface();
		switch (trackerType) {
		case ALTAZIMUTH_DUAL_AXIS_TRACKER:
			normal = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).normalize(null);
			break;
		case HORIZONTAL_SINGLE_AXIS_TRACKER:
			int xRotationAxis = 1;
			int yRotationAxis = 0;
			switch (rotationAxis) {
			case 1:
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
				setNormal(Util.isZero(tiltAngle) ? Math.PI / 2 * 0.9999 : Math.toRadians(90 - tiltAngle), Math.toRadians(relativeAzimuth)); // exactly 90 degrees will cause the solar panel to disappear
			}
		}
		if (Util.isEqual(normal, Vector3.UNIT_Z)) {
			normal = new Vector3(-0.001, 0, 1).normalizeLocal();
		}
		mesh.setRotation(new Matrix3().lookAt(normal, normal.getX() > 0 ? Vector3.UNIT_Z : Vector3.NEG_UNIT_Z));
		mesh.setTranslation(onFlatSurface ? getAbsPoint(0).addLocal(0, 0, baseHeight) : getAbsPoint(0));

		surround.setTranslation(mesh.getTranslation());
		surround.setRotation(mesh.getRotation());

		outlineMesh.setTranslation(mesh.getTranslation());
		outlineMesh.setRotation(mesh.getRotation());

		if (onFlatSurface) {
			supportFrame.getSceneHints().setCullHint(CullHint.Inherit);
			drawSupporFrame();
		} else {
			supportFrame.getSceneHints().setCullHint(CullHint.Always);
		}

		if (drawSunBeam) {
			drawSunBeam();
		}

	}

	// ensure that a solar panel in special cases (on a flat roof or at a tilt angle) will have correct orientation
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

	private void drawSupporFrame() {
		supportFrame.setDefaultColor(getColor());
		final FloatBuffer vertexBuffer = supportFrame.getMeshData().getVertexBuffer();
		final FloatBuffer normalBuffer = supportFrame.getMeshData().getNormalBuffer();
		vertexBuffer.rewind();
		normalBuffer.rewind();
		vertexBuffer.limit(vertexBuffer.capacity());
		normalBuffer.limit(normalBuffer.capacity());
		final ReadOnlyVector3 o = getAbsPoint(0);
		Vector3 dir;
		Vector3 p;
		if (trackerType == NO_TRACKER && Util.isZero(tiltAngle)) {
			dir = new Vector3(0.5, 0, 0);
			p = o.add(0, 0, baseHeight, null);
		} else {
			dir = Util.isEqual(normal, Vector3.UNIT_Z, 0.001) ? new Vector3(0, 1, 0) : normal.cross(Vector3.UNIT_Z, null); // special case when normal is z-axis
			dir = dir.multiplyLocal(0.5);
			p = o.add(0, 0, baseHeight, null);
		}
		Util.addPointToQuad(normal, o, p, dir, vertexBuffer, normalBuffer);
		final double w = (rotated ? panelHeight : panelWidth) / Scene.getInstance().getAnnotationScale();
		dir.normalizeLocal().multiplyLocal(w * 0.5);
		final Vector3 v1 = p.add(dir, null);
		dir.negateLocal();
		final Vector3 v2 = p.add(dir, null);
		dir = new Vector3(normal).multiplyLocal(0.2);
		Util.addPointToQuad(normal, v1, v2, dir, vertexBuffer, normalBuffer);

		vertexBuffer.limit(vertexBuffer.position());
		normalBuffer.limit(normalBuffer.position());
		supportFrame.getMeshData().updateVertexCount();
		supportFrame.updateModelBound();
	}

	public void drawSunBeam() {
		if (Heliodon.getInstance().isNightTime() || !drawSunBeam) {
			sunBeam.setVisible(false);
			return;
		}
		final Vector3 o = getAbsPoint(0).addLocal(0, 0, baseHeight);
		final Vector3 sunLocation = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).normalize(null);
		final FloatBuffer beamsVertices = sunBeam.getMeshData().getVertexBuffer();
		beamsVertices.rewind();
		final Vector3 r = new Vector3(o);
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
	}

	@Override
	public boolean isDrawable() {
		if (container == null) {
			return true;
		}
		if (mesh.getWorldBound() == null) {
			return true;
		}
		final OrientedBoundingBox bound = (OrientedBoundingBox) mesh.getWorldBound().clone(null);
		bound.setExtent(bound.getExtent().divide(1.1, null).addLocal(0, 0, 1));
		for (final HousePart child : container.getChildren()) {
			if (child != this && child instanceof SolarPanel && bound.intersects(child.mesh.getWorldBound())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void updateTextureAndColor() {
		updateTextureAndColor(mesh, null, TextureMode.Full);
	}

	@Override
	protected String getTextureFileName() {
		return rotated ? "solarpanel-rotated.png" : "solarpanel.png";
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
		return panelWidth / Scene.getInstance().getAnnotationScale() / (SceneManager.getInstance().isFineGrid() ? 25.0 : 5.0);
	}

	@Override
	protected void computeArea() {
		area = panelWidth * panelHeight;
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

	private double overlap() {
		final double w1 = (rotated ? panelHeight : panelWidth) / Scene.getInstance().getAnnotationScale();
		final Vector3 center = getAbsCenter();
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p.getContainer() == container && p != this) {
				if (p instanceof SolarPanel) {
					final SolarPanel s2 = (SolarPanel) p;
					final double w2 = (s2.rotated ? s2.panelHeight : s2.panelWidth) / Scene.getInstance().getAnnotationScale();
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
		final SolarPanel c = (SolarPanel) super.copy(false);
		if (check) {
			normal = c.computeNormalAndKeepOnRoof();
			if (container instanceof Roof) {
				if (normal == null) {
					// don't remove this error message just in case this happens again
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Normal of solar panel [" + c + "] is null. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
				if (Util.isEqual(normal, Vector3.UNIT_Z)) { // flat roof
					final Foundation foundation = getTopContainer();
					final Vector3 p0 = foundation.getAbsPoint(0);
					final Vector3 p1 = foundation.getAbsPoint(1);
					final Vector3 p2 = foundation.getAbsPoint(2);
					final double a = -Math.toRadians(relativeAzimuth) * Math.signum(p2.subtract(p0, null).getX() * p1.subtract(p0, null).getY());
					final Vector3 v = new Vector3(Math.cos(a), Math.sin(a), 0);
					final double length = (1 + layoutGap) * (rotated ? panelHeight : panelWidth) / Scene.getInstance().getAnnotationScale();
					final double s = Math.signum(container.getAbsCenter().subtractLocal(Scene.getInstance().getOriginalCopy().getAbsCenter()).dot(v));
					final double tx = length / p0.distance(p2);
					final double ty = length / p0.distance(p1);
					final double lx = s * v.getX() * tx;
					final double ly = s * v.getY() * ty;
					c.points.get(0).setX(points.get(0).getX() + lx);
					c.points.get(0).setY(points.get(0).getY() + ly);
				} else {
					final Vector3 d = normal.cross(Vector3.UNIT_Z, null);
					d.normalizeLocal();
					if (Util.isZero(d.length())) {
						d.set(1, 0, 0);
					}
					final double s = Math.signum(container.getAbsCenter().subtractLocal(Scene.getInstance().getOriginalCopy().getAbsCenter()).dot(d));
					d.multiplyLocal((1 + layoutGap) * (rotated ? panelHeight : panelWidth) / Scene.getInstance().getAnnotationScale());
					d.addLocal(getContainerRelative().getPoints().get(0));
					final Vector3 v = toRelative(d);
					c.points.get(0).setX(points.get(0).getX() + s * v.getX());
					c.points.get(0).setY(points.get(0).getY() + s * v.getY());
					c.points.get(0).setZ(points.get(0).getZ() + s * v.getZ());
				}
				if (!((Roof) c.container).insideWallsPolygon(c.getAbsCenter())) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, you are not allowed to paste a solar panel outside a roof.", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
				final double o = c.overlap();
				if (o >= 0) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, your new solar panel is too close to an existing one (" + o + ").", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			} else if (container instanceof Foundation) {
				final Vector3 p0 = container.getAbsPoint(0);
				final Vector3 p1 = container.getAbsPoint(1);
				final Vector3 p2 = container.getAbsPoint(2);
				final double a = -Math.toRadians(relativeAzimuth) * Math.signum(p2.subtract(p0, null).getX() * p1.subtract(p0, null).getY());
				final Vector3 v = new Vector3(Math.cos(a), Math.sin(a), 0);
				final double length = (1 + layoutGap) * (rotated ? panelHeight : panelWidth) / Scene.getInstance().getAnnotationScale();
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
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, your new solar panel is too close to an existing one (" + o + ").", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			} else if (container instanceof Wall) {
				final double s = Math.signum(toRelative(container.getAbsCenter()).subtractLocal(toRelative(Scene.getInstance().getOriginalCopy().getAbsCenter())).dot(Vector3.UNIT_X));
				final double shift = (1 + layoutGap) * (rotated ? panelHeight : panelWidth) / (container.getAbsPoint(0).distance(container.getAbsPoint(2)) * Scene.getInstance().getAnnotationScale());
				final double newX = points.get(0).getX() + s * shift;
				if (newX > 1 - shift / 2 || newX < shift / 2) {
					return null;
				}
				c.points.get(0).setX(newX);
				if (c.overlap() >= 0) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, your new solar panel is too close to an existing one.", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			}
		}
		return c;
	}

	/** a number between 0 and 1 */
	public void setCellEfficiency(final double efficiency) {
		this.efficiency = efficiency;
	}

	/** a number between 0 and 1 */
	public double getCellEfficiency() {
		return efficiency;
	}

	/** a number between 0 and 1, typically 0.95 */
	public void setInverterEfficiency(final double inverterEfficiency) {
		this.inverterEfficiency = inverterEfficiency;
	}

	/** a number between 0 and 1, typically 0.95 */
	public double getInverterEfficiency() {
		return inverterEfficiency;
	}

	public void setPanelWidth(final double panelWidth) {
		this.panelWidth = panelWidth;
	}

	public double getPanelWidth() {
		return panelWidth;
	}

	public void setPanelHeight(final double panelHeight) {
		this.panelHeight = panelHeight;
	}

	public double getPanelHeight() {
		return panelHeight;
	}

	public void setBaseHeight(final double baseHeight) {
		this.baseHeight = baseHeight;
	}

	public double getBaseHeight() {
		return baseHeight;
	}

	public void setRotated(final boolean b) {
		rotated = b;
	}

	public boolean isRotated() {
		return rotated;
	}

	public void setRelativeAzimuth(double relativeAzimuth) {
		if (relativeAzimuth < 0) {
			relativeAzimuth += 360;
		} else if (relativeAzimuth > 360) {
			relativeAzimuth -= 360;
		}
		this.relativeAzimuth = relativeAzimuth;
	}

	public double getRelativeAzimuth() {
		return relativeAzimuth;
	}

	public void setTiltAngle(final double tiltAngle) {
		this.tiltAngle = tiltAngle;
	}

	public double getTiltAngle() {
		return tiltAngle;
	}

	public void setTracker(final int tracker) {
		this.trackerType = tracker;
	}

	public int getTracker() {
		return trackerType;
	}

	public void setRotationAxis(final int rotationAxis) {
		this.rotationAxis = rotationAxis;
	}

	public int getRotationAxis() {
		return rotationAxis;
	}

	public void setNumberOfCellsInX(final int numberOfCellsInX) {
		this.numberOfCellsInX = numberOfCellsInX;
	}

	public int getNumberOfCellsInX() {
		return numberOfCellsInX;
	}

	public void setNumberOfCellsInY(final int numberOfCellsInY) {
		this.numberOfCellsInY = numberOfCellsInY;
	}

	public int getNumberOfCellsInY() {
		return numberOfCellsInY;
	}

	public void setShadeTolerance(final int shadeTolerance) {
		this.shadeTolerance = shadeTolerance;
	}

	public int getShadeTolerance() {
		return shadeTolerance;
	}

	public void setSunBeamVisible(final boolean drawSunBeam) {
		this.drawSunBeam = drawSunBeam;
	}

	public boolean isDrawSunBeamVisible() {
		return drawSunBeam;
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

	public void move(final Vector3 v, final double steplength) {
		v.normalizeLocal();
		v.multiplyLocal(steplength);
		final Vector3 p = getAbsPoint(0).addLocal(v);
		points.get(0).set(toRelative(p));
	}

	public double getSystemEfficiency() {
		double e = efficiency * inverterEfficiency;
		final Atmosphere atm = Scene.getInstance().getAtmosphere();
		if (atm != null) {
			e *= 1 - atm.getDustLoss();
		}
		return e;
	}

}
