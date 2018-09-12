package org.concord.energy3d.model;

import java.io.IOException;
import java.nio.FloatBuffer;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.geom.BufferUtils;

/**
 * <code>Box</code> is an axis-aligned rectangular prism defined by a center point and x, y, and z extents from that center (essentially radii.)
 */
class MyBox extends Mesh {

	private double xExtent, yExtent, zExtent;
	private final Vector3 center = new Vector3();

	/**
	 * Constructs a new 1x1x1 <code>Box</code>.
	 */
	public MyBox() {
		this("unnamed Box");
	}

	/**
	 * Constructs a new 1x1x1 <code>Box</code> with the given name.
	 *
	 * @param name
	 *            the name to give this new box. This is required for identification and comparison purposes.
	 */
	public MyBox(final String name) {
		super(name);
		setData(Vector3.ZERO, 0.5, 0.5, 0.5);
	}

	/**
	 * Constructs a new <code>Box</code> object using the given two points as opposite corners of the box. These two points may be in any order.
	 *
	 * @param name
	 *            the name to give this new box. This is required for identification and comparison purposes.
	 * @param pntA
	 *            the first point
	 * @param pntB
	 *            the second point.
	 */
	public MyBox(final String name, final ReadOnlyVector3 pntA, final ReadOnlyVector3 pntB) {
		super(name);
		setData(pntA, pntB);
	}

	/**
	 * Constructs a new <code>Box</code> object using the given center and extents. Since the extents represent the distance from the center of the box to the edge, the full length of a side is actually 2 * extent.
	 *
	 * @param name
	 *            the name to give this new box. This is required for identification and comparison purposes.
	 * @param center
	 *            Center of the box.
	 * @param xExtent
	 *            x extent of the box
	 * @param yExtent
	 *            y extent of the box
	 * @param zExtent
	 *            z extent of the box
	 */
	public MyBox(final String name, final ReadOnlyVector3 center, final double xExtent, final double yExtent, final double zExtent) {
		super(name);
		setData(center, xExtent, yExtent, zExtent);
	}

	/**
	 * @return the current center of this box.
	 */
	public ReadOnlyVector3 getCenter() {
		return center;
	}

	/**
	 * @return the current X extent of this box.
	 */
	public double getXExtent() {
		return xExtent;
	}

	/**
	 * @return the current Y extent of this box.
	 */
	public double getYExtent() {
		return yExtent;
	}

	/**
	 * @return the current Z extent of this box.
	 */
	public double getZExtent() {
		return zExtent;
	}

	/**
	 * Updates the center point and extents of this box to match an axis-aligned box defined by the two given opposite corners.
	 *
	 * @param pntA
	 *            the first point
	 * @param pntB
	 *            the second point.
	 */
	public void setData(final ReadOnlyVector3 pntA, final ReadOnlyVector3 pntB) {
		center.set(pntB).addLocal(pntA).multiplyLocal(0.5);
		final double x = Math.abs(pntB.getX() - center.getX());
		final double y = Math.abs(pntB.getY() - center.getY());
		final double z = Math.abs(pntB.getZ() - center.getZ());
		setData(center, x, y, z);
	}

	/**
	 * Updates the center point and extents of this box using the defined values.
	 *
	 * @param center
	 *            The center of the box.
	 * @param xExtent
	 *            x extent of the box
	 * @param yExtent
	 *            y extent of the box
	 * @param zExtent
	 *            z extent of the box
	 */
	public void setData(final ReadOnlyVector3 center, final double xExtent, final double yExtent, final double zExtent) {
		if (center != null) {
			this.center.set(center);
		}
		this.xExtent = xExtent;
		this.yExtent = yExtent;
		this.zExtent = zExtent;
		setVertexData();
		setNormalData();
		setTextureData();
	}

	/**
	 * <code>setVertexData</code> sets the vertex positions that define the box using the center point and defined extents.
	 */
	protected void setVertexData() {
		if (_meshData.getVertexBuffer() == null) {
			_meshData.setVertexBuffer(BufferUtils.createVector3Buffer(36));
		}

		final Vector3 vert[] = new Vector3[8];
		vert[0] = center.add(-xExtent, -yExtent, -zExtent, null);
		vert[1] = center.add(xExtent, -yExtent, -zExtent, null);
		vert[2] = center.add(xExtent, yExtent, -zExtent, null);
		vert[3] = center.add(-xExtent, yExtent, -zExtent, null);
		vert[4] = center.add(-xExtent, -yExtent, zExtent, null);
		vert[5] = center.add(xExtent, -yExtent, zExtent, null);
		vert[6] = center.add(xExtent, yExtent, zExtent, null);
		vert[7] = center.add(-xExtent, yExtent, zExtent, null);

		// Back
		BufferUtils.setInBuffer(vert[0], _meshData.getVertexBuffer(), 0);
		BufferUtils.setInBuffer(vert[1], _meshData.getVertexBuffer(), 1);
		BufferUtils.setInBuffer(vert[2], _meshData.getVertexBuffer(), 2);
		BufferUtils.setInBuffer(vert[2], _meshData.getVertexBuffer(), 3);
		BufferUtils.setInBuffer(vert[3], _meshData.getVertexBuffer(), 4);
		BufferUtils.setInBuffer(vert[0], _meshData.getVertexBuffer(), 5);

		// Right
		BufferUtils.setInBuffer(vert[1], _meshData.getVertexBuffer(), 6);
		BufferUtils.setInBuffer(vert[2], _meshData.getVertexBuffer(), 7);
		BufferUtils.setInBuffer(vert[6], _meshData.getVertexBuffer(), 8);
		BufferUtils.setInBuffer(vert[6], _meshData.getVertexBuffer(), 9);
		BufferUtils.setInBuffer(vert[5], _meshData.getVertexBuffer(), 10);
		BufferUtils.setInBuffer(vert[1], _meshData.getVertexBuffer(), 11);

		// Front
		BufferUtils.setInBuffer(vert[4], _meshData.getVertexBuffer(), 12);
		BufferUtils.setInBuffer(vert[5], _meshData.getVertexBuffer(), 13);
		BufferUtils.setInBuffer(vert[6], _meshData.getVertexBuffer(), 14);
		BufferUtils.setInBuffer(vert[6], _meshData.getVertexBuffer(), 15);
		BufferUtils.setInBuffer(vert[7], _meshData.getVertexBuffer(), 16);
		BufferUtils.setInBuffer(vert[4], _meshData.getVertexBuffer(), 17);

		// Left
		BufferUtils.setInBuffer(vert[0], _meshData.getVertexBuffer(), 18);
		BufferUtils.setInBuffer(vert[3], _meshData.getVertexBuffer(), 19);
		BufferUtils.setInBuffer(vert[7], _meshData.getVertexBuffer(), 20);
		BufferUtils.setInBuffer(vert[7], _meshData.getVertexBuffer(), 21);
		BufferUtils.setInBuffer(vert[4], _meshData.getVertexBuffer(), 22);
		BufferUtils.setInBuffer(vert[0], _meshData.getVertexBuffer(), 23);

		// Top
		BufferUtils.setInBuffer(vert[3], _meshData.getVertexBuffer(), 24);
		BufferUtils.setInBuffer(vert[2], _meshData.getVertexBuffer(), 25);
		BufferUtils.setInBuffer(vert[6], _meshData.getVertexBuffer(), 26);
		BufferUtils.setInBuffer(vert[6], _meshData.getVertexBuffer(), 27);
		BufferUtils.setInBuffer(vert[7], _meshData.getVertexBuffer(), 28);
		BufferUtils.setInBuffer(vert[3], _meshData.getVertexBuffer(), 29);

		// Bottom
		BufferUtils.setInBuffer(vert[0], _meshData.getVertexBuffer(), 30);
		BufferUtils.setInBuffer(vert[1], _meshData.getVertexBuffer(), 31);
		BufferUtils.setInBuffer(vert[5], _meshData.getVertexBuffer(), 32);
		BufferUtils.setInBuffer(vert[5], _meshData.getVertexBuffer(), 33);
		BufferUtils.setInBuffer(vert[4], _meshData.getVertexBuffer(), 34);
		BufferUtils.setInBuffer(vert[0], _meshData.getVertexBuffer(), 35);

	}

	/**
	 * <code>setNormalData</code> sets the normals of each of the box's planes.
	 */
	private void setNormalData() {
		if (_meshData.getNormalBuffer() == null) {
			_meshData.setNormalBuffer(BufferUtils.createVector3Buffer(36));

			// back
			for (int i = 0; i < 6; i++) {
				_meshData.getNormalBuffer().put(0).put(0).put(-1);
			}

			// right
			for (int i = 0; i < 6; i++) {
				_meshData.getNormalBuffer().put(1).put(0).put(0);
			}

			// front
			for (int i = 0; i < 6; i++) {
				_meshData.getNormalBuffer().put(0).put(0).put(1);
			}

			// left
			for (int i = 0; i < 6; i++) {
				_meshData.getNormalBuffer().put(-1).put(0).put(0);
			}

			// top
			for (int i = 0; i < 6; i++) {
				_meshData.getNormalBuffer().put(0).put(1).put(0);
			}

			// bottom
			for (int i = 0; i < 6; i++) {
				_meshData.getNormalBuffer().put(0).put(-1).put(0);
			}
			_meshData.getNormalBuffer().rewind();
		}
	}

	/**
	 * <code>setTextureData</code> sets the points that define the texture of the box. It's a one-to-one ratio, where each plane of the box has its own copy of the texture. That is, the texture is repeated one time for each six faces.
	 */
	private void setTextureData() {
		if (_meshData.getTextureCoords(0) == null) {
			_meshData.setTextureBuffer(BufferUtils.createVector2Buffer(36), 0);
			final FloatBuffer tex = _meshData.getTextureBuffer(0);
			for (int i = 0; i < 9; i++) {
				tex.put(0).put(0);
				tex.put(0).put(1);
				tex.put(1).put(1);
				tex.put(1).put(0);
			}
			tex.rewind();
		}
	}

	@Override
	public void write(final OutputCapsule capsule) throws IOException {
		super.write(capsule);
		capsule.write(xExtent, "xExtent", 0);
		capsule.write(yExtent, "yExtent", 0);
		capsule.write(zExtent, "zExtent", 0);
		capsule.write(center, "center", new Vector3());
	}

	@Override
	public void read(final InputCapsule capsule) throws IOException {
		super.read(capsule);
		xExtent = capsule.readDouble("xExtent", 0);
		yExtent = capsule.readDouble("yExtent", 0);
		zExtent = capsule.readDouble("zExtent", 0);
		center.set((Vector3) capsule.readSavable("center", new Vector3()));
	}

}