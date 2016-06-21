package org.concord.energy3d.model;

import java.nio.FloatBuffer;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Camera.ProjectionMode;
import com.ardor3d.renderer.state.OffsetState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.geom.BufferUtils;

public class SolarPanel extends HousePart {

	private static final long serialVersionUID = 1L;
	private transient ReadOnlyVector3 normal;
	private transient Mesh outlineMesh;
	private transient Box surround;
	private double efficiency = 0.15; // a number in (0, 1)
	private double inverterEfficiency = 0.95;
	private double panelWidth = 0.99; // 39"
	private double panelHeight = 1.65; // 65"
	private boolean rotated = false; // rotation around the normal usually takes only two angles: 0 or 90, so we use a boolean here
	private double relativeAzimuth;
	private double zenith = 90; // the zenith angle relative to the surface of the parent

	public SolarPanel(boolean rotated) {
		super(1, 1, 0);
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

	public void setPanelWidth(double panelWidth) {
		this.panelWidth = panelWidth;
	}

	public double getPanelWidth() {
		return panelWidth;
	}

	public void setPanelHeight(double panelHeight) {
		this.panelHeight = panelHeight;
	}

	public double getPanelHeight() {
		return panelHeight;
	}

	@Override
	protected void init() {
		super.init();

		if (Util.isZero(panelWidth))
			panelWidth = 0.99;
		if (Util.isZero(panelHeight))
			panelHeight = 1.65;
		if (Util.isZero(zenith))
			zenith = 90;
		if (Util.isZero(efficiency))
			efficiency = 0.15;
		if (Util.isZero(inverterEfficiency))
			inverterEfficiency = 0.95;

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
				if (!Util.isZero(zenith - 90)) {
					double h = (rotated ? panelWidth : panelHeight) / Scene.getInstance().getAnnotationScale();
					p.setZ(p.getZ() + 0.5 * h * Math.cos(Math.toRadians(zenith)));
				}
				getEditPointShape(i).setTranslation(p);
			}
		} finally {
			Vector3.releaseTempInstance(p);
		}
		/* remove remaining edit shapes */
		for (int i = points.size(); i < pointsRoot.getNumberOfChildren(); i++)
			pointsRoot.detachChildAt(points.size());
	}

	@Override
	protected void drawMesh() {
		if (container == null)
			return;

		normal = computeNormalAndKeepOnRoof();
		updateEditShapes();

		final double annotationScale = Scene.getInstance().getAnnotationScale();
		if (rotated) {
			surround.setData(new Vector3(0, 0, 0), panelHeight / 2.0 / annotationScale, panelWidth / 2.0 / annotationScale, 0.15);
		} else {
			surround.setData(new Vector3(0, 0, 0), panelWidth / 2.0 / annotationScale, panelHeight / 2.0 / annotationScale, 0.15);
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

		if (Util.isZero(zenith - 90)) {
			mesh.setTranslation(getAbsPoint(0));
			if (Util.isEqual(normal, Vector3.UNIT_Z)) {
				setNormal(Math.PI / 2 * 0.999999); // exactly 90 degrees will cause the solar panel to disappear
			}
		} else {
			double t = Math.toRadians(zenith);
			double h = (rotated ? panelWidth : panelHeight) / Scene.getInstance().getAnnotationScale();
			mesh.setTranslation(getAbsPoint(0).addLocal(0, 0, 0.5 * h * Math.cos(t)));
			setNormal(t, Math.toRadians(relativeAzimuth));
		}
		mesh.setRotation(new Matrix3().lookAt(normal, Vector3.UNIT_Z));

		surround.setTranslation(mesh.getTranslation());
		surround.setRotation(mesh.getRotation());

		outlineMesh.setTranslation(mesh.getTranslation());
		outlineMesh.setRotation(mesh.getRotation());

	}

	// ensure that a solar panel in special cases (on a flat roof or at a tilt angle) will have correct orientation
	private void setNormal(double zenith, double azimuth) {
		Foundation foundation = getTopContainer();
		Vector3 v = foundation.getAbsPoint(0);
		Vector3 vx = foundation.getAbsPoint(2).subtractLocal(v); // x direction
		Vector3 vy = foundation.getAbsPoint(1).subtractLocal(v); // y direction
		Matrix3 m = new Matrix3().applyRotationZ(-azimuth);
		Vector3 v1 = m.applyPost(vx, null);
		Vector3 v2 = m.applyPost(vy, null);
		v = new Matrix3().fromAngleAxis(zenith, v1).applyPost(v2, null);
		if (v.getZ() < 0)
			v.negateLocal();
		normal = v.normalizeLocal();
	}

	// ensure that a solar panel in special cases (on a flat roof or at a tilt angle) will have correct orientation
	private void setNormal(double zenith) {
		Foundation foundation = getTopContainer();
		Vector3 v = foundation.getAbsPoint(0);
		Vector3 vx = foundation.getAbsPoint(2).subtractLocal(v);
		Vector3 vy = foundation.getAbsPoint(1).subtractLocal(v);
		v = new Matrix3().fromAngleAxis(zenith, vx).applyPost(vy, null);
		if (v.getZ() < 0)
			v.negateLocal();
		normal = v.normalizeLocal();
	}

	@Override
	public boolean isDrawable() {
		if (container == null) // FIXME: There is a chance that a solar panel can be left without a container
			return true;
		if (mesh.getWorldBound() == null)
			return true;
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
		return "solarpanel.png";
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

	private boolean overlap() {
		double w1 = (rotated ? panelHeight : panelWidth) / Scene.getInstance().getAnnotationScale();
		final Vector3 center = getAbsCenter();
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p.getContainer() == container && p != this) {
				if (p instanceof SolarPanel) {
					SolarPanel s2 = (SolarPanel) p;
					double w2 = (s2.rotated ? s2.panelHeight : s2.panelWidth) / Scene.getInstance().getAnnotationScale();
					if (p.getAbsCenter().distance(center) < (w1 + w2) * 0.499)
						return true;
				}
			}
		}
		return false;
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
				final Vector3 d = normal.cross(Vector3.UNIT_Z, null);
				d.normalizeLocal();
				if (Util.isZero(d.length()))
					d.set(1, 0, 0);
				final Vector3 d0 = d.clone();
				d.multiplyLocal((rotated ? panelHeight : panelWidth) / Scene.getInstance().getAnnotationScale());
				d.addLocal(getContainerRelative().getPoints().get(0));
				final Vector3 v = toRelative(d);
				final Vector3 originalCenter = Scene.getInstance().getOriginalCopy().getAbsCenter();
				final double s = Math.signum(container.getAbsCenter().subtractLocal(originalCenter).dot(d0));
				c.points.get(0).setX(points.get(0).getX() + s * v.getX());
				c.points.get(0).setY(points.get(0).getY() + s * v.getY());
				c.points.get(0).setZ(points.get(0).getZ() + s * v.getZ());
				if (!((Roof) c.container).insideWallsPolygon(c.getAbsCenter())) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, you are not allowed to paste a solar panel outside a roof.", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
				if (c.overlap()) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, your new solar panel is too close to an existing one.", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			} else if (container instanceof Wall || container instanceof Foundation) {
				if (relativeAzimuth == 90) {
					final double s = Math.signum(toRelative(container.getAbsCenter()).subtractLocal(toRelative(Scene.getInstance().getOriginalCopy().getAbsCenter())).dot(Vector3.UNIT_Y));
					final double shift = (rotated ? panelHeight : panelWidth) / (container.getAbsPoint(0).distance(container.getAbsPoint(1)) * Scene.getInstance().getAnnotationScale());
					final double newY = points.get(0).getY() + s * shift;
					if (newY > 1 - shift / 2 || newY < shift / 2) // reject it if out of range
						return null;
					c.points.get(0).setY(newY);
				} else {
					final double s = Math.signum(toRelative(container.getAbsCenter()).subtractLocal(toRelative(Scene.getInstance().getOriginalCopy().getAbsCenter())).dot(Vector3.UNIT_X));
					final double shift = (rotated ? panelHeight : panelWidth) / (container.getAbsPoint(0).distance(container.getAbsPoint(2)) * Scene.getInstance().getAnnotationScale());
					final double newX = points.get(0).getX() + s * shift;
					if (newX > 1 - shift / 2 || newX < shift / 2) // reject it if out of range
						return null;
					c.points.get(0).setX(newX);
				}
				if (c.overlap()) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, your new solar panel is too close to an existing one.", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			}
		}
		return c;
	}

	public void setRotated(boolean b) {
		rotated = b;
	}

	public boolean isRotated() {
		return rotated;
	}

	public void setRelativeAzimuth(double relativeAzimuth) {
		this.relativeAzimuth = relativeAzimuth;
	}

	public double getRelativeAzimuth() {
		return relativeAzimuth;
	}

	public void setZenith(double zenith) {
		this.zenith = zenith;
	}

	public double getZenith() {
		return zenith;
	}

}
