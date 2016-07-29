package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.Calendar;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.SolarRadiation;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.extension.effect.bloom.BloomRenderPass;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.Camera.ProjectionMode;
import com.ardor3d.renderer.state.OffsetState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.geom.BufferUtils;

public class SolarPanel extends HousePart {

	private static final long serialVersionUID = 1L;
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
	private boolean heliostat;
	private double baseHeight = 6;
	private boolean drawSunBeam;
	private transient double layoutGap = 0.01;
	private static transient BloomRenderPass bloomRenderPass;

	public SolarPanel(boolean rotated) {
		super(1, 1, 0);
	}

	@Override
	protected void init() {
		super.init();

		if (Util.isZero(panelWidth))
			panelWidth = 0.99;
		if (Util.isZero(panelHeight))
			panelHeight = 1.65;
		if (Util.isZero(efficiency))
			efficiency = 0.15;
		if (Util.isZero(inverterEfficiency))
			inverterEfficiency = 0.95;
		if (Util.isZero(baseHeight))
			baseHeight = 6;

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
				if (onFlatSurface())
					p.setZ(p.getZ() + baseHeight);
				getEditPointShape(i).setTranslation(p);
			}
		} finally {
			Vector3.releaseTempInstance(p);
		}
		/* remove remaining edit shapes */
		for (int i = points.size(); i < pointsRoot.getNumberOfChildren(); i++)
			pointsRoot.detachChildAt(points.size());
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
		if (container == null)
			return;

		normal = computeNormalAndKeepOnRoof();
		updateEditShapes();

		final double annotationScaleFactor = 0.5 / Scene.getInstance().getAnnotationScale();
		if (rotated) {
			surround.setData(new Vector3(0, 0, 0), panelHeight * annotationScaleFactor, panelWidth * annotationScaleFactor, 0.15);
		} else {
			surround.setData(new Vector3(0, 0, 0), panelWidth * annotationScaleFactor, panelHeight * annotationScaleFactor, 0.15);
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

		boolean onFlatSurface = onFlatSurface();
		if (heliostat) {
			normal = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalender()).normalize(null);
		} else {
			if (onFlatSurface)
				setNormal(Util.isZero(tiltAngle) ? Math.PI / 2 * 0.9999 : Math.toRadians(90 - tiltAngle), Math.toRadians(relativeAzimuth)); // exactly 90 degrees will cause the solar panel to disappear
		}
		mesh.setRotation(new Matrix3().lookAt(normal, Vector3.UNIT_Z));
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

		if (drawSunBeam)
			drawSunBeam();

	}

	public void setNormalAtTime(int minute) {
		Calendar calendar = (Calendar) Heliodon.getInstance().getCalender().clone();
		calendar.set(Calendar.HOUR_OF_DAY, (int) ((double) minute / (double) SolarRadiation.MINUTES_OF_DAY * 24.0));
		calendar.set(Calendar.MINUTE, minute % 60);
		normal = Heliodon.getInstance().computeSunLocation(calendar).normalize(null);
	}

	// ensure that a solar panel in special cases (on a flat roof or at a tilt angle) will have correct orientation
	private void setNormal(double angle, double azimuth) {
		Foundation foundation = getTopContainer();
		Vector3 v = foundation.getAbsPoint(0);
		Vector3 vx = foundation.getAbsPoint(2).subtractLocal(v); // x direction
		Vector3 vy = foundation.getAbsPoint(1).subtractLocal(v); // y direction
		Matrix3 m = new Matrix3().applyRotationZ(-azimuth);
		Vector3 v1 = m.applyPost(vx, null);
		Vector3 v2 = m.applyPost(vy, null);
		v = new Matrix3().fromAngleAxis(angle, v1).applyPost(v2, null);
		if (v.getZ() < 0)
			v.negateLocal();
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
		if (!heliostat && Util.isZero(tiltAngle)) {
			dir = new Vector3(0.5, 0, 0);
			p = o.add(0, 0, baseHeight, null);
		} else {
			dir = normal.cross(Vector3.UNIT_Z, null).multiplyLocal(0.5);
			p = o.add(0, 0, baseHeight, null);
		}
		Util.addPointToQuad(normal, o, p, dir, vertexBuffer, normalBuffer);
		double w = (rotated ? panelHeight : panelWidth) / Scene.getInstance().getAnnotationScale();
		dir.normalizeLocal().multiplyLocal(w * 0.5);
		Vector3 v1 = p.add(dir, null);
		dir.negateLocal();
		Vector3 v2 = p.add(dir, null);
		dir = new Vector3(normal).multiplyLocal(0.2);
		Util.addPointToQuad(normal, v1, v2, dir, vertexBuffer, normalBuffer);

		vertexBuffer.limit(vertexBuffer.position());
		normalBuffer.limit(normalBuffer.position());
		supportFrame.getMeshData().updateVertexCount();
		supportFrame.updateModelBound();
	}

	public void drawSunBeam() {
		if (Heliodon.getInstance().isNightTime()) {
			sunBeam.setVisible(false);
			return;
		}
		final Vector3 o = getAbsPoint(0).addLocal(0, 0, baseHeight);
		final Vector3 sunLocation = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalender()).normalize(null);
		FloatBuffer beamsVertices = sunBeam.getMeshData().getVertexBuffer();
		beamsVertices.rewind();
		Vector3 r = new Vector3(o);
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

	private double overlap() {
		double w1 = (rotated ? panelHeight : panelWidth) / Scene.getInstance().getAnnotationScale();
		final Vector3 center = getAbsCenter();
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p.getContainer() == container && p != this) {
				if (p instanceof SolarPanel) {
					SolarPanel s2 = (SolarPanel) p;
					double w2 = (s2.rotated ? s2.panelHeight : s2.panelWidth) / Scene.getInstance().getAnnotationScale();
					double distance = p.getAbsCenter().distance(center);
					if (distance < (w1 + w2) * 0.499)
						return distance;
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
					Foundation foundation = getTopContainer();
					Vector3 p0 = foundation.getAbsPoint(0);
					Vector3 p1 = foundation.getAbsPoint(1);
					Vector3 p2 = foundation.getAbsPoint(2);
					double a = -Math.toRadians(relativeAzimuth) * Math.signum(p2.subtract(p0, null).getX() * p1.subtract(p0, null).getY());
					Vector3 v = new Vector3(Math.cos(a), Math.sin(a), 0);
					final double length = (1 + layoutGap) * (rotated ? panelHeight : panelWidth) / Scene.getInstance().getAnnotationScale();
					final double s = Math.signum(container.getAbsCenter().subtractLocal(Scene.getInstance().getOriginalCopy().getAbsCenter()).dot(v));
					double tx = length / p0.distance(p2);
					double ty = length / p0.distance(p1);
					double lx = s * v.getX() * tx;
					double ly = s * v.getY() * ty;
					c.points.get(0).setX(points.get(0).getX() + lx);
					c.points.get(0).setY(points.get(0).getY() + ly);
				} else {
					final Vector3 d = normal.cross(Vector3.UNIT_Z, null);
					d.normalizeLocal();
					if (Util.isZero(d.length()))
						d.set(1, 0, 0);
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
				double o = c.overlap();
				if (o >= 0) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, your new solar panel is too close to an existing one (" + o + ").", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			} else if (container instanceof Foundation) {
				Vector3 p0 = container.getAbsPoint(0);
				Vector3 p1 = container.getAbsPoint(1);
				Vector3 p2 = container.getAbsPoint(2);
				double a = -Math.toRadians(relativeAzimuth) * Math.signum(p2.subtract(p0, null).getX() * p1.subtract(p0, null).getY());
				Vector3 v = new Vector3(Math.cos(a), Math.sin(a), 0);
				final double length = (1 + layoutGap) * (rotated ? panelHeight : panelWidth) / Scene.getInstance().getAnnotationScale();
				final double s = Math.signum(container.getAbsCenter().subtractLocal(Scene.getInstance().getOriginalCopy().getAbsCenter()).dot(v));
				double tx = length / p0.distance(p2);
				double ty = length / p0.distance(p1);
				double lx = s * v.getX() * tx;
				double ly = s * v.getY() * ty;
				final double newX = points.get(0).getX() + lx;
				if (newX > 1 - tx || newX < tx) // reject it if out of range
					return null;
				final double newY = points.get(0).getY() + ly;
				if (newY > 1 - ty || newY < ty) // reject it if out of range
					return null;
				c.points.get(0).setX(newX);
				c.points.get(0).setY(newY);
				double o = c.overlap();
				if (o >= 0) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, your new solar panel is too close to an existing one (" + o + ").", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			} else if (container instanceof Wall) {
				final double s = Math.signum(toRelative(container.getAbsCenter()).subtractLocal(toRelative(Scene.getInstance().getOriginalCopy().getAbsCenter())).dot(Vector3.UNIT_X));
				final double shift = (1 + layoutGap) * (rotated ? panelHeight : panelWidth) / (container.getAbsPoint(0).distance(container.getAbsPoint(2)) * Scene.getInstance().getAnnotationScale());
				final double newX = points.get(0).getX() + s * shift;
				if (newX > 1 - shift / 2 || newX < shift / 2) // reject it if out of range
					return null;
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

	public void setBaseHeight(double baseHeight) {
		this.baseHeight = baseHeight;
	}

	public double getBaseHeight() {
		return baseHeight;
	}

	public void setRotated(boolean b) {
		rotated = b;
	}

	public boolean isRotated() {
		return rotated;
	}

	public void setRelativeAzimuth(double relativeAzimuth) {
		if (relativeAzimuth < 0)
			relativeAzimuth += 360;
		else if (relativeAzimuth > 360)
			relativeAzimuth -= 360;
		this.relativeAzimuth = relativeAzimuth;
	}

	public double getRelativeAzimuth() {
		return relativeAzimuth;
	}

	public void setTiltAngle(double tiltAngle) {
		this.tiltAngle = tiltAngle;
	}

	public double getTiltAngle() {
		return tiltAngle;
	}

	public void setHeliostat(boolean heliostat) {
		this.heliostat = heliostat;
	}

	public boolean getHeliostat() {
		return heliostat;
	}

	public void setDrawSunBeam(boolean drawSunBeam) {
		this.drawSunBeam = drawSunBeam;
	}

	public boolean getDrawSunBeam() {
		return drawSunBeam;
	}

	@Override
	public void delete() {
		super.delete();
		if (bloomRenderPass != null) {
			if (bloomRenderPass.contains(sunBeam))
				bloomRenderPass.remove(sunBeam);
		}
	}

}
