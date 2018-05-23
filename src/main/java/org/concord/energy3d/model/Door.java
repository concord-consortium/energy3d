package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.List;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.util.geom.BufferUtils;

public class Door extends HousePart implements Thermal {

	public static final int TEXTURE_01 = 1;
	public static final int TEXTURE_02 = 2;
	public static final int TEXTURE_03 = 3;
	public static final int TEXTURE_04 = 4;
	public static final int TEXTURE_05 = 5;
	public static final int TEXTURE_06 = 6;
	public static final int TEXTURE_07 = 7;
	public static final int TEXTURE_08 = 8;
	public static final int TEXTURE_09 = 9;
	public static final int TEXTURE_10 = 10;
	public static final int TEXTURE_11 = 11;
	public static final int TEXTURE_12 = 12;
	private static final long serialVersionUID = 1L;
	private static final double DEFAULT_DOOR_HEIGHT = 10;
	private double volumetricHeatCapacity = 0.5; // unit: kWh/m^3/C (1 kWh = 3.6 MJ)
	private double uValue = 2; // default is IECC code for Massachusetts (https://energycode.pnl.gov/EnergyCodeReqs/index.jsp?state=Massachusetts)
	private transient Line outlineMesh;

	public Door() {
		super(2, 4, DEFAULT_DOOR_HEIGHT);
	}

	@Override
	protected void init() {
		super.init();

		if (Util.isZero(uValue)) {
			uValue = 2;
		}
		if (Util.isZero(volumetricHeatCapacity)) {
			volumetricHeatCapacity = 0.5;
		}

		mesh = new Mesh("Door");
		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		mesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(4));
		mesh.getMeshData().setTextureBuffer(BufferUtils.createVector2Buffer(4), 0);

		updateTextureAndColor();

		mesh.setModelBound(new BoundingBox());
		mesh.setUserData(new UserData(this));
		root.attachChild(mesh);

		outlineMesh = new Line("Door (Outline)");
		outlineMesh.setLineWidth(1);
		outlineMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(6 + 2 * 4));
		outlineMesh.setDefaultColor(ColorRGBA.BLACK);
		outlineMesh.setModelBound(new BoundingBox());
		Util.disablePickShadowLight(outlineMesh);
		root.attachChild(outlineMesh);

	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		final PickedHousePart picked = pickContainer(x, y, Wall.class); // pick container even for disabled foundation
		final Foundation foundation = getTopContainer();
		if (foundation != null && foundation.getLockEdit()) {
			return;
		}
		if (editPointIndex == -1 || editPointIndex == 0 || editPointIndex == 2) {
			if (picked != null) {
				Vector3 p = picked.getPoint().clone();
				final Vector3 wallFirstPoint = container.getAbsPoint(0);
				final Vector3 wallx = container.getAbsPoint(2).subtract(wallFirstPoint, null);
				p = Util.closestPoint(wallFirstPoint, wallx, x, y);
				if (p == null) {
					return;
				}
				snapToGrid(p, getAbsPoint(editPointIndex == -1 ? points.size() - 2 : editPointIndex), getGridSize(), false);

				final int index = (editPointIndex == -1) ? points.size() - 2 : editPointIndex;
				points.set(index, toRelative(p));
				if (editPointIndex != -1) {
					height = getAbsPoint(editPointIndex == 0 ? 2 : 0).subtract(getAbsPoint(editPointIndex == 0 ? 3 : 1), null).length();
				}
				p.setZ(p.getZ() + height);
				points.set(index + 1, toRelative(p));
			}
		} else if (editPointIndex == 1 || editPointIndex == 3) {
			final int lower = (editPointIndex == 1) ? 0 : 2;
			final Vector3 base = points.get(lower);
			final Vector3 absoluteBase = toAbsolute(base);
			final Vector3 p = Util.closestPoint(absoluteBase, Vector3.UNIT_Z, x, y);
			if (p == null) {
				return;
			}
			snapToGrid(p, getAbsPoint(editPointIndex), getGridSize());
			height = Math.max(getGridSize(), p.getZ() - absoluteBase.getZ());

			final double rel_z = toRelative(absoluteBase.addLocal(0, 0, height)).getZ();
			points.get(1).setZ(rel_z);
			points.get(3).setZ(rel_z);
		}
		if (container != null) {
			draw();
			setEditPointsVisible(true);
		}
	}

	@Override
	public double getGridSize() {
		return SceneManager.getInstance().isFineGrid() ? 1.0 : 2.0;
	}

	@Override
	public ReadOnlyVector3 getNormal() {
		return container.getNormal();
	}

	@Override
	protected void drawMesh() {
		if (points.size() < 4) {
			return;
		}

		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		for (int i = 0; i < points.size(); i++) {
			final ReadOnlyVector3 p = getAbsPoint(i);
			vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
		}

		final ReadOnlyVector3 normal = getAbsPoint(2).subtract(getAbsPoint(0), null).crossLocal(getAbsPoint(1).subtract(getAbsPoint(0), null)).normalizeLocal().negateLocal();
		final FloatBuffer normalBuffer = mesh.getMeshData().getNormalBuffer();
		normalBuffer.rewind();
		for (int i = 0; i < points.size(); i++) {
			normalBuffer.put(normal.getXf()).put(normal.getYf()).put(normal.getZf());
		}

		final FloatBuffer textureBuffer = mesh.getMeshData().getTextureBuffer(0);
		textureBuffer.rewind();
		textureBuffer.put(0).put(0);
		textureBuffer.put(0).put(1);
		textureBuffer.put(1).put(0);
		textureBuffer.put(1).put(1);

		mesh.updateModelBound();

		outlineMesh.getSceneHints().setCullHint(CullHint.Inherit);
		final FloatBuffer outlineBuffer = outlineMesh.getMeshData().getVertexBuffer();
		outlineBuffer.rewind();
		final Vector3 p0 = getAbsPoint(0);
		final Vector3 p1 = getAbsPoint(1);
		final Vector3 p2 = getAbsPoint(2);
		final Vector3 p3 = getAbsPoint(3);
		outlineBuffer.put(p0.getXf()).put(p0.getYf()).put(p0.getZf());
		outlineBuffer.put(p1.getXf()).put(p1.getYf()).put(p1.getZf());
		outlineBuffer.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
		outlineBuffer.put(p3.getXf()).put(p3.getYf()).put(p3.getZf());
		outlineBuffer.put(p1.getXf()).put(p1.getYf()).put(p1.getZf());
		outlineBuffer.put(p3.getXf()).put(p3.getYf()).put(p3.getZf());
		if (textureType == TEXTURE_NONE) {
			final double dmin = 0.1 * Math.min(p0.distance(p1), p0.distance(p2));
			final Vector3 d10 = p1.subtract(p0, null).normalizeLocal().multiplyLocal(dmin);
			final Vector3 d20 = p2.subtract(p0, null).normalizeLocal().multiplyLocal(dmin);
			final Vector3 v0 = p0.add(d10, null).addLocal(d20);
			final Vector3 v1 = p1.subtract(d10, null).addLocal(d20);
			final Vector3 v2 = p2.subtract(d20, null).addLocal(d10);
			final Vector3 v3 = p3.subtract(d20, null).subtractLocal(d10);
			outlineBuffer.put(v0.getXf()).put(v0.getYf()).put(v0.getZf());
			outlineBuffer.put(v1.getXf()).put(v1.getYf()).put(v1.getZf());
			outlineBuffer.put(v2.getXf()).put(v2.getYf()).put(v2.getZf());
			outlineBuffer.put(v3.getXf()).put(v3.getYf()).put(v3.getZf());
			outlineBuffer.put(v0.getXf()).put(v0.getYf()).put(v0.getZf());
			outlineBuffer.put(v2.getXf()).put(v2.getYf()).put(v2.getZf());
			outlineBuffer.put(v1.getXf()).put(v1.getYf()).put(v1.getZf());
			outlineBuffer.put(v3.getXf()).put(v3.getYf()).put(v3.getZf());
		}
		outlineMesh.updateModelBound();

	}

	@Override
	public boolean isPrintable() {
		return false;
	}

	@Override
	public void updateTextureAndColor() {
		updateTextureAndColor(mesh, getColor() == null ? Scene.getInstance().getDefaultDoorColor() : getColor());
	}

	@Override
	protected String getTextureFileName() {
		switch (textureType) {
		case TEXTURE_EDGE:
			return "door_edge.png";
		case TEXTURE_01:
			return "door_01.png";
		case TEXTURE_02:
			return "door_02.png";
		case TEXTURE_03:
			return "door_03.png";
		case TEXTURE_04:
			return "door_04.png";
		case TEXTURE_05:
			return "door_05.png";
		case TEXTURE_06:
			return "door_06.png";
		case TEXTURE_07:
			return "door_07.png";
		case TEXTURE_08:
			return "door_08.png";
		case TEXTURE_09:
			return "door_09.png";
		case TEXTURE_10:
			return "door_10.png";
		case TEXTURE_11:
			return "door_11.png";
		case TEXTURE_12:
			return "door_12.png";
		}
		return null;
	}

	@Override
	public Vector3 getAbsPoint(final int index) {
		return container != null ? container.getRoot().getTransform().applyForward(super.getAbsPoint(index)) : super.getAbsPoint(index);
	}

	@Override
	protected void computeArea() {
		if (isDrawCompleted()) {
			final Vector3 p0 = getAbsPoint(0);
			final Vector3 p1 = getAbsPoint(1);
			final Vector3 p2 = getAbsPoint(2);
			final double C = 100.0;
			final double sceneScale = Scene.getInstance().getScale();
			area = Math.round(Math.round(p2.subtract(p0, null).length() * sceneScale * C) / C * Math.round(p1.subtract(p0, null).length() * sceneScale * C) / C * C) / C;
		} else {
			area = 0.0;
		}
	}

	@Override
	public boolean isCopyable() {
		return false;
	}

	@Override
	public void setUValue(final double uValue) {
		this.uValue = uValue;
	}

	@Override
	public double getUValue() {
		return uValue;
	}

	@Override
	public void setVolumetricHeatCapacity(final double volumetricHeatCapacity) {
		this.volumetricHeatCapacity = volumetricHeatCapacity;
	}

	@Override
	public double getVolumetricHeatCapacity() {
		return volumetricHeatCapacity;
	}

	@Override
	public boolean isValid() {
		if (!super.isValid()) {
			return false;
		}
		return super.isDrawable();
	}

	public void setDoorWidth(final double width) {
		final Vector3 a = toRelativeVector(getAbsPoint(2).subtract(getAbsPoint(0), null).normalizeLocal().multiplyLocal(0.5 * (width - getDoorWidth()) / Scene.getInstance().getScale()));
		points.get(0).subtractLocal(a);
		points.get(1).subtractLocal(a);
		points.get(2).addLocal(a);
		points.get(3).addLocal(a);
	}

	public double getDoorWidth() {
		return getAbsPoint(0).distance(getAbsPoint(2)) * Scene.getInstance().getScale();
	}

	public void setDoorHeight(final double height) {
		final Vector3 a = toRelativeVector(getAbsPoint(1).subtract(getAbsPoint(0), null).normalizeLocal().multiplyLocal(height / Scene.getInstance().getScale()));
		points.get(1).set(points.get(0).add(a, null));
		points.get(3).set(points.get(2).add(a, null));
	}

	public double getDoorHeight() {
		return getAbsPoint(0).distance(getAbsPoint(1)) * Scene.getInstance().getScale();
	}

	private boolean overlap() {
		final double w1 = getAbsPoint(0).distance(getAbsPoint(2));
		final Vector3 center = getAbsCenter();
		for (final HousePart p : container.getChildren()) {
			if (p != this && (p instanceof Door || p instanceof Window)) {
				final double w2 = p.getAbsPoint(0).distance(p.getAbsPoint(2));
				if (p.getAbsCenter().distance(center) < (w1 + w2) * 0.55) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public HousePart copy(final boolean check) {
		final Door c = (Door) super.copy(false);
		if (check) {
			if (container instanceof Wall) {
				final double s = Math.signum(toRelative(container.getAbsCenter()).subtractLocal(toRelative(Scene.getInstance().getOriginalCopy().getAbsCenter())).dot(Vector3.UNIT_X));
				final double shift = s * (points.get(0).distance(points.get(2)) * 2); // place the next door one width away
				final int n = c.getPoints().size();
				for (int i = 0; i < n; i++) {
					final double newX = points.get(i).getX() + shift;
					if (newX > 1 - shift / 20 || newX < shift / 20) {
						return null;
					}
				}
				for (int i = 0; i < n; i++) {
					c.points.get(i).setX(points.get(i).getX() + shift);
				}
				if (c.overlap()) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, your new door is too close to an existing door or window.", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			}
		}
		return c;
	}

	@Override
	public void addPrintMeshes(final List<Mesh> list) {
		addPrintMesh(list, mesh);
	}

}
