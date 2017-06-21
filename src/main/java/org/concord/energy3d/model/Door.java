package org.concord.energy3d.model;

import java.nio.FloatBuffer;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.geom.BufferUtils;

public class Door extends HousePart implements Thermalizable {

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
		outlineMesh.setLineWidth(2);
		outlineMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		outlineMesh.setDefaultColor(ColorRGBA.BLACK);
		outlineMesh.setModelBound(new BoundingBox());
		Util.disablePickShadowLight(outlineMesh);
		root.attachChild(outlineMesh);

	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		final Foundation foundation = getTopContainer();
		if (foundation != null && foundation.getLockEdit()) {
			return;
		}
		if (editPointIndex == -1 || editPointIndex == 0 || editPointIndex == 2) {
			final PickedHousePart picked = pickContainer(x, y, Wall.class);
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
		outlineMesh.updateModelBound();

	}

	@Override
	public boolean isPrintable() {
		return false;
	}

	@Override
	public void updateTextureAndColor() {
		updateTextureAndColor(mesh, getColor() == null ? Scene.getInstance().getDoorColor() : getColor(), Scene.getInstance().getTextureMode() == TextureMode.None ? TextureMode.Simple : Scene.getInstance().getTextureMode());
		// updateTextureAndColor(mesh, getColor() == null ? Scene.getInstance().getDoorColor() : getColor(), Scene.getInstance().getTextureMode());
	}

	@Override
	protected String getTextureFileName() {
		return Scene.getInstance().getTextureMode() == TextureMode.Full ? "door.jpg" : "door.png";
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
			final double annotationScale = Scene.getInstance().getAnnotationScale();
			area = Math.round(Math.round(p2.subtract(p0, null).length() * annotationScale * C) / C * Math.round(p1.subtract(p0, null).length() * annotationScale * C) / C * C) / C;
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

}
