package org.concord.energy3d.model;

import java.io.IOException;

import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.geom.BufferUtils;

/**
 * @author Charles Xie
 *
 *         A parabolic cylinder is defined by its height, semilatus rectum, and depth. Its center is the origin.
 */
public class ParabolicCylinder extends Mesh {

	private int radialSamples;
	private double radius;
	private double depth;
	private double height;

	public ParabolicCylinder() {
	}

	/**
	 * Creates a new ParabolicCylinder. By default its center is the origin. Usually, a higher sample number creates a better looking parabolic cylinder, but at the cost of more vertex information.
	 * 
	 * @param name
	 *            The name of this parabolic cylinder.
	 * @param radialSamples
	 *            Number of triangle samples along the radial.
	 * @param radius
	 *            The radius of the parabolic cylinder.
	 * @param height
	 *            The height of the parabolic cylinder.
	 * @param depth
	 *            The depth of the parabolic cylinder.
	 */
	public ParabolicCylinder(final String name, final int radialSamples, final double radius, final double depth, final double height) {
		super(name);
		this.radialSamples = radialSamples;
		this.height = height;
		this.depth = depth;
		setRadius(radius);
		allocateVertices();
	}

	/**
	 * @return Returns the height.
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * @param height
	 *            The height to set.
	 */
	public void setHeight(final double height) {
		this.height = height;
		allocateVertices();
	}

	/**
	 * @return Returns the radius.
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Change the radius of this parabolic cylinder.
	 * 
	 * @param radius
	 *            The radius to set.
	 */
	public void setRadius(final double radius) {
		this.radius = radius;
		allocateVertices();
	}

	/**
	 * @return Returns the depth.
	 */
	public double getDepth() {
		return depth;
	}

	/**
	 * Change the depth of this parabolic cylinder.
	 * 
	 * @param depth
	 *            The depth to set.
	 */
	public void setDepth(final double depth) {
		this.depth = depth;
		allocateVertices();
	}

	/**
	 * @return number of samples around parabolic cylinder
	 */
	public int getRadialSamples() {
		return radialSamples;
	}

	private void allocateVertices() {
		final int verts = 2 * (radialSamples + 1);
		_meshData.setVertexBuffer(BufferUtils.createVector3Buffer(_meshData.getVertexBuffer(), verts));
		_meshData.setNormalBuffer(BufferUtils.createVector3Buffer(_meshData.getNormalBuffer(), verts)); // allocate normals if requested
		_meshData.setTextureBuffer(BufferUtils.createVector2Buffer(verts), 0); // allocate texture coordinates
		final int count = 2 * radialSamples;
		if (_meshData.getIndices() == null || _meshData.getIndices().getBufferLimit() != 3 * count) {
			_meshData.setIndices(BufferUtils.createIndexBufferData(3 * count, verts - 1));
		}
		setGeometryData();
		setIndexData();
	}

	// generate geometry
	private void setGeometryData() {
		final double inverseRadial = 1.0 / radialSamples;
		final double halfHeight = 0.5 * height;

		// Generate points on the unit circle to be used in computing the mesh points on a cylinder end.
		final double[] sin = new double[radialSamples + 1];
		final double[] cos = new double[radialSamples + 1];

		for (int radialCount = 0; radialCount < radialSamples; radialCount++) {
			final double angle = MathUtils.TWO_PI * inverseRadial * radialCount;
			cos[radialCount] = MathUtils.cos(angle);
			sin[radialCount] = MathUtils.sin(angle);
		}
		sin[radialSamples] = sin[0];
		cos[radialSamples] = cos[0];

		// generate the cylinder itself
		final Vector3 tempNormal = new Vector3();
		final Vector3 faceCenter = new Vector3(0, 0, -halfHeight); // compute the center of the lower end

		// compute lower end vertices with duplication at end point
		for (int radialCount = 0; radialCount < radialSamples; radialCount++) {
			final double radialFraction = radialCount * inverseRadial; // in [0,1)
			tempNormal.set(cos[radialCount], sin[radialCount], 0);
			_meshData.getNormalBuffer().put(tempNormal.getXf()).put(tempNormal.getYf()).put(tempNormal.getZf());
			tempNormal.multiplyLocal(radius).addLocal(faceCenter);
			_meshData.getVertexBuffer().put(tempNormal.getXf()).put(tempNormal.getYf()).put(tempNormal.getZf());
			_meshData.getTextureCoords(0).getBuffer().put((float) radialFraction).put(0);
		}

		BufferUtils.copyInternalVector3(_meshData.getVertexBuffer(), 0, radialSamples);
		BufferUtils.copyInternalVector3(_meshData.getNormalBuffer(), 0, radialSamples);

		_meshData.getTextureCoords(0).getBuffer().put(1).put(0);

		// draw the upper end of the cylinder
		faceCenter.setZ(halfHeight);

		// compute upper end vertices with duplication at end point
		for (int radialCount = 0; radialCount < radialSamples; radialCount++) {
			final double radialFraction = radialCount * inverseRadial; // in [0,1)
			tempNormal.set(cos[radialCount], sin[radialCount], 0);
			_meshData.getNormalBuffer().put(tempNormal.getXf()).put(tempNormal.getYf()).put(tempNormal.getZf());
			tempNormal.multiplyLocal(radius).addLocal(faceCenter);
			_meshData.getVertexBuffer().put(tempNormal.getXf()).put(tempNormal.getYf()).put(tempNormal.getZf());
			_meshData.getTextureCoords(0).getBuffer().put((float) radialFraction).put(1);
		}

		BufferUtils.copyInternalVector3(_meshData.getVertexBuffer(), radialSamples + 1, 2 * radialSamples + 1);
		BufferUtils.copyInternalVector3(_meshData.getNormalBuffer(), radialSamples + 1, 2 * radialSamples + 1);

		_meshData.getTextureCoords(0).getBuffer().put(1f).put(1);

	}

	private void setIndexData() {
		_meshData.getIndices().rewind();
		int i0 = 0;
		int i1 = 1;
		int i2 = radialSamples + 1;
		int i3 = i2 + 1;
		for (int i = 0; i < radialSamples; i++) {
			_meshData.getIndices().put(i0++);
			_meshData.getIndices().put(i1);
			_meshData.getIndices().put(i2);
			_meshData.getIndices().put(i1++);
			_meshData.getIndices().put(i3++);
			_meshData.getIndices().put(i2++);
		}
	}

	@Override
	public void write(final OutputCapsule capsule) throws IOException {
		super.write(capsule);
		capsule.write(radialSamples, "radialSamples", 0);
		capsule.write(radius, "radius", 0);
		capsule.write(depth, "depth", 0);
		capsule.write(height, "height", 0);
	}

	@Override
	public void read(final InputCapsule capsule) throws IOException {
		super.read(capsule);
		radialSamples = capsule.readInt("radialSamples", 0);
		radius = capsule.readDouble("radius", 0);
		depth = capsule.readDouble("depth", 0);
		height = capsule.readDouble("height", 0);
	}

}
