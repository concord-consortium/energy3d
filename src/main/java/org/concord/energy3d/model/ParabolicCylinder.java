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

	private int _axisSamples;
	private int _radialSamples;
	private double _radius;
	private double _depth;
	private double _height;

	public ParabolicCylinder() {
	}

	/**
	 * Creates a new ParabolicCylinder. By default its center is the origin. Usually, a higher sample number creates a better looking parabolic cylinder, but at the cost of more vertex information.
	 * 
	 * @param name
	 *            The name of this ParabolicCylinder.
	 * @param axisSamples
	 *            Number of triangle samples along the axis.
	 * @param radialSamples
	 *            Number of triangle samples along the radial.
	 * @param radius
	 *            The radius of the parabolic cylinder.
	 * @param height
	 *            The height of the parabolic cylinder.
	 * @param depth
	 *            The depth of the parabolic cylinder.
	 */
	public ParabolicCylinder(final String name, final int axisSamples, final int radialSamples, final double radius, final double depth, final double height) {
		super(name);
		_axisSamples = axisSamples;
		_radialSamples = radialSamples;
		setRadius(radius);
		_height = height;
		_depth = depth;
		allocateVertices();
	}

	/**
	 * @return Returns the height.
	 */
	public double getHeight() {
		return _height;
	}

	/**
	 * @param height
	 *            The height to set.
	 */
	public void setHeight(final double height) {
		_height = height;
		allocateVertices();
	}

	/**
	 * @return Returns the radius.
	 */
	public double getRadius() {
		return _radius;
	}

	/**
	 * Change the radius of this parabolic cylinder.
	 * 
	 * @param radius
	 *            The radius to set.
	 */
	public void setRadius(final double radius) {
		_radius = radius;
		allocateVertices();
	}

	/**
	 * @return Returns the depth.
	 */
	public double getDepth() {
		return _depth;
	}

	/**
	 * Change the depth of this parabolic cylinder.
	 * 
	 * @param depth
	 *            The depth to set.
	 */
	public void setDepth(final double depth) {
		_depth = depth;
		allocateVertices();
	}

	/**
	 * @return the number of samples along the parabolic cylinder axis
	 */
	public int getAxisSamples() {
		return _axisSamples;
	}

	/**
	 * @return number of samples around parabolic cylinder
	 */
	public int getRadialSamples() {
		return _radialSamples;
	}

	private void allocateVertices() {
		final int verts = _axisSamples * (_radialSamples + 1);
		_meshData.setVertexBuffer(BufferUtils.createVector3Buffer(_meshData.getVertexBuffer(), verts));
		_meshData.setNormalBuffer(BufferUtils.createVector3Buffer(_meshData.getNormalBuffer(), verts)); // allocate normals if requested
		_meshData.setTextureBuffer(BufferUtils.createVector2Buffer(verts), 0); // allocate texture coordinates
		final int count = 2 * (_axisSamples - 1) * _radialSamples;
		if (_meshData.getIndices() == null || _meshData.getIndices().getBufferLimit() != 3 * count) {
			_meshData.setIndices(BufferUtils.createIndexBufferData(3 * count, verts - 1));
		}
		setGeometryData();
		setIndexData();
	}

	// generate geometry
	private void setGeometryData() {
		final double inverseRadial = 1.0 / _radialSamples;
		final double inverseAxisLess = 1.0 / (_axisSamples - 1);
		final double halfHeight = 0.5 * _height;

		// Generate points on the unit circle to be used in computing the mesh points on a cylinder slice.
		final double[] sin = new double[_radialSamples + 1];
		final double[] cos = new double[_radialSamples + 1];

		for (int radialCount = 0; radialCount < _radialSamples; radialCount++) {
			final double angle = MathUtils.TWO_PI * inverseRadial * radialCount;
			cos[radialCount] = MathUtils.cos(angle);
			sin[radialCount] = MathUtils.sin(angle);
		}
		sin[_radialSamples] = sin[0];
		cos[_radialSamples] = cos[0];

		// generate the cylinder itself
		final Vector3 tempNormal = new Vector3();
		for (int axisCount = 0, i = 0; axisCount < _axisSamples; axisCount++) {
			double axisFraction;
			double axisFractionTexture;
			axisFraction = axisCount * inverseAxisLess; // in [0,1]
			axisFractionTexture = axisFraction;
			final double z = -halfHeight + _height * axisFraction;

			final Vector3 sliceCenter = new Vector3(0, 0, z); // compute center of slice

			// compute slice vertices with duplication at end point
			final int save = i;
			for (int radialCount = 0; radialCount < _radialSamples; radialCount++) {
				final double radialFraction = radialCount * inverseRadial; // in [0,1)
				tempNormal.set(cos[radialCount], sin[radialCount], 0);
				_meshData.getNormalBuffer().put(tempNormal.getXf()).put(tempNormal.getYf()).put(tempNormal.getZf());
				tempNormal.multiplyLocal(_radius).addLocal(sliceCenter);
				_meshData.getVertexBuffer().put(tempNormal.getXf()).put(tempNormal.getYf()).put(tempNormal.getZf());
				_meshData.getTextureCoords(0).getBuffer().put((float) radialFraction).put((float) axisFractionTexture);
				i++;
			}

			BufferUtils.copyInternalVector3(_meshData.getVertexBuffer(), save, i);
			BufferUtils.copyInternalVector3(_meshData.getNormalBuffer(), save, i);

			_meshData.getTextureCoords(0).getBuffer().put(1.0f).put((float) axisFractionTexture);

			i++;
		}

	}

	private void setIndexData() {
		_meshData.getIndices().rewind();
		for (int axisCount = 0, axisStart = 0; axisCount < _axisSamples - 1; axisCount++) { // generate connectivity
			int i0 = axisStart;
			int i1 = i0 + 1;
			axisStart += _radialSamples + 1;
			int i2 = axisStart;
			int i3 = i2 + 1;
			for (int i = 0; i < _radialSamples; i++) {
				_meshData.getIndices().put(i0++);
				_meshData.getIndices().put(i1);
				_meshData.getIndices().put(i2);
				_meshData.getIndices().put(i1++);
				_meshData.getIndices().put(i3++);
				_meshData.getIndices().put(i2++);
			}
		}
	}

	@Override
	public void write(final OutputCapsule capsule) throws IOException {
		super.write(capsule);
		capsule.write(_axisSamples, "axisSamples", 0);
		capsule.write(_radialSamples, "radialSamples", 0);
		capsule.write(_radius, "radius", 0);
		capsule.write(_depth, "depth", 0);
		capsule.write(_height, "height", 0);
	}

	@Override
	public void read(final InputCapsule capsule) throws IOException {
		super.read(capsule);
		_axisSamples = capsule.readInt("axisSamples", 0);
		_radialSamples = capsule.readInt("radialSamples", 0);
		_radius = capsule.readDouble("radius", 0);
		_depth = capsule.readDouble("depth", 0);
		_height = capsule.readDouble("height", 0);
	}

}
