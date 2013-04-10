package org.concord.energy3d.model;

import java.nio.FloatBuffer;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.geom.BufferUtils;

public class Door extends HousePart {
	private static final long serialVersionUID = 1L;
//	private static double defaultDoorHeight = 15;
	private static double defaultDoorHeight = 10;

	public Door() {
		super(2, 4, defaultDoorHeight);
	}

	@Override
	protected void init() {
		super.init();
		mesh = new Mesh("Door");
		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		mesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(4));
		mesh.getMeshData().setTextureBuffer(BufferUtils.createVector2Buffer(4), 0);

		updateTextureAndColor();

		mesh.setModelBound(new BoundingBox());
		mesh.setUserData(new UserData(this));
		root.attachChild(mesh);
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		if (editPointIndex == -1 || editPointIndex == 0 || editPointIndex == 2) {
			final PickedHousePart picked = pickContainer(x, y, Wall.class);
			if (picked != null) {
				Vector3 p = picked.getPoint();
				final Vector3 wallFirstPoint = container.getAbsPoint(0);
				final Vector3 wallx = container.getAbsPoint(2).subtract(wallFirstPoint, null);
				p = Util.closestPoint(wallFirstPoint, wallx, x, y);
				snapToGrid(p, getAbsPoint(editPointIndex == -1 ? points.size() - 2 : editPointIndex), getGridSize());

				final int index = (editPointIndex == -1) ? points.size() - 2 : editPointIndex;
				points.set(index, toRelative(p));
				if (editPointIndex != -1)
					defaultDoorHeight = height = getAbsPoint(editPointIndex == 0 ? 2 : 0).subtract(getAbsPoint(editPointIndex == 0 ? 3 : 1), null).length();
				p.setZ(p.getZ() + height);
				points.set(index + 1, toRelative(p));
			}
		} else if (editPointIndex == 1 || editPointIndex == 3) {
			final int lower = (editPointIndex == 1) ? 0 : 2;
			final Vector3 base = points.get(lower);
			final Vector3 absoluteBase = toAbsolute(base);
			final Vector3 p = Util.closestPoint(absoluteBase, Vector3.UNIT_Z, x, y);
			snapToGrid(p, getAbsPoint(editPointIndex), getGridSize());
			defaultDoorHeight = height = Math.max(0, p.getZ() - absoluteBase.getZ());

			final double rel_z = toRelative(absoluteBase.addLocal(0, 0, height)).getZ();
			points.get(1).setZ(rel_z);
			points.get(3).setZ(rel_z);
		}
		if (container != null) {
			draw();
			setEditPointsVisible(true);
		}
	}

//	@Override
//	public boolean isDrawable() {
//		return points.size() >= 4 && getAbsPoint(2).distance(getAbsPoint(0)) > MathUtils.ZERO_TOLERANCE && getAbsPoint(1).distance(getAbsPoint(0)) > MathUtils.ZERO_TOLERANCE;
//	}

	@Override
	protected void drawMesh() {
		if (points.size() < 4)
			return;

		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		for (int i = 0; i < points.size(); i++) {
			final ReadOnlyVector3 p = getAbsPoint(i);
			vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
		}

		final ReadOnlyVector3 normal = getAbsPoint(2).subtract(getAbsPoint(0), null).crossLocal(getAbsPoint(1).subtract(getAbsPoint(0), null)).normalizeLocal().negateLocal();
		final FloatBuffer normalBuffer = mesh.getMeshData().getNormalBuffer();
		normalBuffer.rewind();
		for (int i = 0; i < points.size(); i++)
			normalBuffer.put(normal.getXf()).put(normal.getYf()).put(normal.getZf());

		final FloatBuffer textureBuffer = mesh.getMeshData().getTextureBuffer(0);
		textureBuffer.rewind();
		textureBuffer.put(0).put(0);
		textureBuffer.put(0).put(1);
		textureBuffer.put(1).put(0);
		textureBuffer.put(1).put(1);

		mesh.updateModelBound();
	}

	@Override
	public boolean isPrintable() {
		return false;
	}

	@Override
	public void updateTextureAndColor() {
		updateTextureAndColor(mesh, Scene.getInstance().getDoorColor(), Scene.getInstance().getTextureMode() == TextureMode.None ? TextureMode.Simple : Scene.getInstance().getTextureMode());
	}

	@Override
	protected String getTextureFileName() {
		return Scene.getInstance().getTextureMode() == TextureMode.Full ? "door.jpg" : "door.png";
	}

	@Override
	public Vector3 getAbsPoint(final int index) {
		if (container != null)
			return container.getRoot().getTransform().applyForward(super.getAbsPoint(index));
		else
			return super.getAbsPoint(index);
	}
}