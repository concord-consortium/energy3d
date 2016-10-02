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
import org.concord.energy3d.undo.AddArrayCommand;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.OffsetState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.scenegraph.shape.Cylinder;
import com.ardor3d.util.geom.BufferUtils;

public class Rack extends HousePart {
	private static final long serialVersionUID = 1L;
	private transient ReadOnlyVector3 normal;
	private transient Mesh outlineMesh;
	private transient Box surround;
	private transient Node polesRoot;
	private transient ArrayList<Vector3> solarOrgPoints;
	private transient Vector3 moveStartPoint;
	private transient double layoutGap = 0.01;
	private double rackWidth = 15;
	private double rackHeight = 3;
	private double relativeAzimuth = 0;
	private double tiltAngle = -25;
	private double baseHeight = 15;
	private double poleDistanceX = 3;
	private double poleDistanceY = 1;

	public Rack() {
		super(1, 1, 0);
	}

	@Override
	protected void init() {
		super.init();
		mesh = new Mesh("Reflecting Rack");
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

		polesRoot = new Node("Poles Root");
		root.attachChild(polesRoot);
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		if (moveStartPoint == null) {
			initSolarPanelsForMove();
		}
		final PickedHousePart picked = pickContainer(x, y, new Class<?>[] { Foundation.class });
		if (picked != null) {
			final Vector3 p = picked.getPoint().clone();
			snapToGrid(p, getAbsPoint(0), getGridSize(), false);
			points.get(0).set(toRelative(p));
		}
		if (container != null) {
			draw();
			moveSolarPanels(getPoints().get(0).clone().subtractLocal(moveStartPoint), solarOrgPoints);
			drawChildren();
			setEditPointsVisible(true);
			setHighlight(!isDrawable());
		}
	}

	@Override
	protected void drawMesh() {
		if (container == null) {
			return;
		}

		normal = computeNormalAndKeepOnRoof();
		points.get(0).setZ(getTopContainer().getHeight() + baseHeight);

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

		final Vector3 a = getAbsPoint(0);
		setNormal(Util.isZero(tiltAngle) ? Math.PI / 2 * 0.9999 : Math.toRadians(90 - tiltAngle), Math.toRadians(relativeAzimuth)); // exactly 90 degrees will cause the mirror to disappear
		mesh.setTranslation(a);
		mesh.setRotation(new Matrix3().lookAt(normal, Vector3.UNIT_Z));

		surround.setTranslation(mesh.getTranslation());
		surround.setRotation(mesh.getRotation());
		outlineMesh.setTranslation(mesh.getTranslation());
		outlineMesh.setRotation(mesh.getRotation());

		final Vector3 center = getAbsPoint(0);
		polesRoot.detachAllChildren();
		final HousePart container = getContainerRelative();
		final Vector3 uDir = container.getPoints().get(2).subtract(container.getPoints().get(0), null).normalizeLocal();
		final Vector3 vDir = container.getPoints().get(1).subtract(container.getPoints().get(0), null).normalizeLocal();
		final Matrix3 matrix = new Matrix3().fromAngles(0, 0, -Math.toRadians(relativeAzimuth));
		matrix.applyPost(uDir, uDir);
		matrix.applyPost(vDir, vDir);
		if (vDir.dot(normal) < 0) {
			vDir.negateLocal();
		}
		final double tanTiltAngle = Math.abs(Math.tan(Math.toRadians(tiltAngle)));
		for (double u = poleDistanceX; u < rackWidth / 1; u += poleDistanceX) {
			for (double v = poleDistanceY; v < rackHeight / 1; v += poleDistanceY) {
				final double vFactor = (v - rackHeight / 2) / annotationScale;
				final Vector3 position = uDir.multiply((u - rackWidth / 2) / annotationScale, null).addLocal(vDir.multiply(vFactor, null)).addLocal(center);
				final double dz = tanTiltAngle * vFactor;

				final Cylinder pole = new Cylinder("Pole Cylinder", 10, 10, 10, 0);
				pole.setRadius(0.6);
				pole.setRenderState(offsetState);
				pole.setHeight(baseHeight - dz - 0.1);
				pole.setModelBound(new BoundingBox());
				pole.updateModelBound();
				position.setZ(container.getHeight() + pole.getHeight() / 2);
				pole.setTranslation(position);
				polesRoot.attachChild(pole);
			}
		}
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
		updateTextureAndColor(mesh, ColorRGBA.LIGHT_GRAY, TextureMode.None);
	}

	@Override
	protected String getTextureFileName() {
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
			if (p.getContainer() == container && p != this) {
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

	public void setRelativeAzimuth(double relativeAzimuth) {
		if (relativeAzimuth < 0) {
			relativeAzimuth += 360;
		} else if (relativeAzimuth > 360) {
			relativeAzimuth -= 360;
		}
		// rotate all solar panels
		final Vector3 center = getAbsPoint(0);
		final Matrix3 matrix = new Matrix3().fromAngles(0, 0, Math.toRadians(this.relativeAzimuth - relativeAzimuth));
		for (final HousePart child : children) {
			final Vector3 v = child.getAbsPoint(0).subtractLocal(center);
			matrix.applyPost(v, v);
			v.addLocal(center);
			child.getPoints().get(0).set(child.toRelative(v));
		}
		this.relativeAzimuth = relativeAzimuth;
	}

	public double getRelativeAzimuth() {
		return relativeAzimuth;
	}

	public void setTiltAngle(final double tiltAngle) {
		// TODO: rotate all solar panels
		for (final HousePart child : children) {
		}
		this.tiltAngle = tiltAngle;
	}

	public double getTiltAngle() {
		return tiltAngle;
	}

	public void move(final Vector3 v, final double steplength) {
		v.normalizeLocal();
		v.multiplyLocal(steplength);
		final Vector3 p = getAbsPoint(0).addLocal(v);
		points.get(0).set(toRelative(p));
		// TODO: Move solar panels as well, the following doesn't work
		// moveSolarPanels(toRelative(v));
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

	public void addSolarPanels(final double panelWidth, final double panelHeight, final boolean portrait) {
		EnergyPanel.getInstance().clearRadiationHeatMap();
		final AddArrayCommand command = new AddArrayCommand(removeAllChildren(), this, SolarPanel.class);
		final ArrayList<HousePart> c0 = new ArrayList<HousePart>(children);
		for (final HousePart c : c0) { // make a copy to avoid concurrent modification
			Scene.getInstance().remove(c, false);
		}
		final Foundation foundation = getTopContainer();
		final double azFoundation = Math.toRadians(foundation.getAzimuth());
		if (!Util.isZero(azFoundation)) {
			foundation.rotate(azFoundation, null);
		}
		final double azRack = relativeAzimuth;
		setRelativeAzimuth(0);
		final double a = portrait ? panelWidth : panelHeight;
		final double b = portrait ? panelHeight : panelWidth;
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
				final SolarPanel sp = new SolarPanel(false);
				sp.setContainer(this);
				final Vector3 v = sp.toRelative(new Vector3(x, y, 0));
				sp.points.get(0).setX(v.getX());
				sp.points.get(0).setY(v.getY());
				sp.setPanelWidth(panelWidth);
				sp.setPanelHeight(panelHeight);
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
		SceneManager.getInstance().getUndoManager().addEdit(command);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				EnergyPanel.getInstance().updateProperties();
			}
		});
	}

}
