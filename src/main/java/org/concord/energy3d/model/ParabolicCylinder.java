package org.concord.energy3d.model;

import java.io.IOException;

import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.geom.BufferUtils;

/**
 * @author Charles Xie
 *
 *         A parabolic cylinder is defined by its height, semilatus rectum, and depth. Its center is the origin and its axis is along the Y-axis.
 */
public class ParabolicCylinder extends Mesh {

	private int numberOfSamples;
	private double semilatusRectum;
	private double width;
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
	 * @param semilatusRectum
	 *            The semilatus rectum of the parabolic cylinder.
	 * @param width
	 *            The width of the open end of the parabolic cylinder.
	 * @param height
	 *            The height of the parabolic cylinder.
	 */
	public ParabolicCylinder(final String name, final int radialSamples, final double semilatusRectum, final double width, final double height) {
		super(name);
		this.numberOfSamples = radialSamples;
		this.width = width;
		this.height = height;
		setSemilatusRectum(semilatusRectum);
	}

	/**
	 * @return Returns the width of the open end at cutoff.
	 */
	public double getWidth() {
		return width;
	}

	/**
	 * @return Returns the height.
	 */
	public double getHeight() {
		return height;
	}

	/**
	 * @param width
	 *            The width of the open end at cutoff to set.
	 * @param height
	 *            The height to set.
	 */
	public void setSize(final double width, final double height) {
		this.width = width;
		this.height = height;
		allocateVertices();
	}

	/**
	 * @return Returns the semilatus rectum.
	 */
	public double getSemilatusRectum() {
		return semilatusRectum;
	}

	/**
	 * Change the semilatus rectum of this parabolic cylinder.
	 * 
	 * @param semilatusRectum
	 *            The semilatus rectum to set.
	 */
	public void setSemilatusRectum(final double semilatusRectum) {
		this.semilatusRectum = semilatusRectum;
		allocateVertices();
	}

	/**
	 * @return number of samples around parabolic cylinder
	 */
	public int getNumberOfSamples() {
		return numberOfSamples;
	}

	private void allocateVertices() {
		final int verts = 2 * (numberOfSamples + 1);
		_meshData.setVertexBuffer(BufferUtils.createVector3Buffer(_meshData.getVertexBuffer(), verts));
		_meshData.setNormalBuffer(BufferUtils.createVector3Buffer(_meshData.getNormalBuffer(), verts)); // allocate normals if requested
		_meshData.setTextureBuffer(BufferUtils.createVector2Buffer(verts), 0); // allocate texture coordinates
		final int count = 2 * numberOfSamples;
		if (_meshData.getIndices() == null || _meshData.getIndices().getBufferLimit() != 3 * count) {
			_meshData.setIndices(BufferUtils.createIndexBufferData(3 * count, verts - 1));
		}
		setGeometryData();
		setIndexData();
	}

	// generate geometry
	private void setGeometryData() {
		final double halfWidth = 0.5 * width;
		final double halfHeight = 0.5 * height;
		final double tmin = -halfWidth / semilatusRectum;
		final double delta = width / (numberOfSamples * semilatusRectum);

		// Generate points of the parabola under the limit to be used in computing the mesh points on a cylinder end.
		final double[] x = new double[numberOfSamples + 1];
		final double[] y = new double[numberOfSamples + 1];

		for (int i = 0; i < numberOfSamples + 1; i++) { // use the parametric equation to compute the points (no need to use Lissajous Curve)
			final double t = i * delta + tmin;
			x[i] = semilatusRectum * t;
			y[i] = semilatusRectum * t * t * 0.5;
		}

		// generate the cylinder itself
		final Vector3 point = new Vector3();
		final Vector3 pointNormal = new Vector3();
		final Vector3 faceCenter = new Vector3(0, -halfHeight, 0); // compute the center of the lower end

		// compute lower end vertices with duplication at end point
		final float inverseNumberOfSamples = 1.0f / numberOfSamples;
		for (int i = 0; i < numberOfSamples + 1; i++) {
			point.set(x[i], 0, y[i]);
			point.normalize(pointNormal);
			_meshData.getNormalBuffer().put(pointNormal.getXf()).put(pointNormal.getYf()).put(pointNormal.getZf());
			point.addLocal(faceCenter);
			_meshData.getVertexBuffer().put(point.getXf()).put(point.getYf()).put(point.getZf());
			_meshData.getTextureCoords(0).getBuffer().put(i * inverseNumberOfSamples).put(0);
		}

		// compute upper end vertices with duplication at end point
		faceCenter.setY(halfHeight);
		for (int i = 0; i < numberOfSamples + 1; i++) {
			point.set(x[i], 0, y[i]);
			point.normalize(pointNormal);
			_meshData.getNormalBuffer().put(pointNormal.getXf()).put(pointNormal.getYf()).put(pointNormal.getZf());
			point.addLocal(faceCenter);
			_meshData.getVertexBuffer().put(point.getXf()).put(point.getYf()).put(point.getZf());
			_meshData.getTextureCoords(0).getBuffer().put(i * inverseNumberOfSamples).put(1);
		}

	}

	private void setIndexData() {
		_meshData.getIndices().rewind();
		int i0 = 0;
		int i1 = 1;
		int i2 = numberOfSamples + 1;
		int i3 = i2 + 1;
		for (int i = 0; i < numberOfSamples; i++) {
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
		capsule.write(numberOfSamples, "numberOfSamples", 0);
		capsule.write(semilatusRectum, "semilatusRectum", 0);
		capsule.write(width, "width", 0);
		capsule.write(height, "height", 0);
	}

	@Override
	public void read(final InputCapsule capsule) throws IOException {
		super.read(capsule);
		numberOfSamples = capsule.readInt("numberOfSamples", 0);
		semilatusRectum = capsule.readDouble("semilatusRectum", 0);
		width = capsule.readDouble("width", 0);
		height = capsule.readDouble("height", 0);
	}

}
