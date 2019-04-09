package org.concord.energy3d.model;

import java.io.IOException;

import com.ardor3d.math.MathUtils;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.geom.BufferUtils;

/**
 * <code>Cylinder</code> provides an extension of <code>Mesh</code>. A <code>Cylinder</code> is defined by a height and radius. The center of the Cylinder is the origin.
 */
public class MyCylinder extends Mesh {

	private int _axisSamples;
	private int _radialSamples;
	private double _radius;
	private double _height;
	private boolean _closed;

	public MyCylinder() {
	}

	/**
	 * Creates a new Cylinder. By default its center is the origin. Usually, a higher sample number creates a better looking cylinder, but at the cost of more vertex information.
	 * 
	 * @param name
	 *            The name of this Cylinder.
	 * @param axisSamples
	 *            Number of triangle samples along the axis.
	 * @param radialSamples
	 *            Number of triangle samples along the radial.
	 * @param radius
	 *            The radius of the cylinder.
	 * @param height
	 *            The cylinder's height.
	 */
	public MyCylinder(final String name, final int axisSamples, final int radialSamples, final double radius, final double height) {
		this(name, axisSamples, radialSamples, radius, height, false);
	}

	/**
	 * Creates a new Cylinder. By default its center is the origin. Usually, a higher sample number creates a better looking cylinder, but at the cost of more vertex information. <br>
	 * If the cylinder is closed the texture is split into axisSamples parts: top most and bottom most part is used for top and bottom of the cylinder, rest of the texture for the cylinder wall. The middle of the top is mapped to texture coordinates (0.5, 1), bottom to (0.5, 0). Thus you need a suited distorted texture.
	 * 
	 * @param name
	 *            The name of this Cylinder.
	 * @param axisSamples
	 *            Number of triangle samples along the axis.
	 * @param radialSamples
	 *            Number of triangle samples along the radial.
	 * @param radius
	 *            The radius of the cylinder.
	 * @param height
	 *            The cylinder's height.
	 * @param closed
	 *            true to create a cylinder with top and bottom surface
	 */
	public MyCylinder(final String name, final int axisSamples, final int radialSamples, final double radius, final double height, final boolean closed) {
		super(name);
		_axisSamples = axisSamples + (closed ? 2 : 0);
		_radialSamples = radialSamples;
		_radius = radius;
		_height = height;
		_closed = closed;
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
	 * Change the radius of this cylinder. This resets any second radius.
	 * 
	 * @param radius
	 *            The radius to set.
	 */
	public void setRadius(final double radius) {
		_radius = radius;
		allocateVertices();
	}

	/**
	 * @return true if end caps are used.
	 */
	public boolean isClosed() {
		return _closed;
	}

	/**
	 * @return number of samples around cylinder
	 */
	public int getRadialSamples() {
		return _radialSamples;
	}

	private void allocateVertices() {
		// allocate vertices
		final int verts = 6 * _axisSamples * _radialSamples + (_closed ? 2 : 0);
		_meshData.setVertexBuffer(BufferUtils.createVector3Buffer(_meshData.getVertexBuffer(), verts));

		// allocate normals if requested
		_meshData.setNormalBuffer(BufferUtils.createVector3Buffer(_meshData.getNormalBuffer(), verts));

		// allocate texture coordinates
		_meshData.setTextureBuffer(BufferUtils.createVector2Buffer(verts), 0);

		// generate geometry
		final float halfHeight = (float) (0.5 * _height);
		final double angle = MathUtils.TWO_PI / _radialSamples;
		final double cos = MathUtils.cos(angle);
		final double sin = MathUtils.sin(angle);

		// generate the cylinder itself
		double x1 = _radius, y1 = 0, x2, y2;
		for (int i = 0; i < _radialSamples; i++) {
			x2 = cos * x1 - sin * y1;
			y2 = sin * x1 + cos * y1;
			// normal vector
			double xn = (x1 + x2) * 0.5;
			double yn = (y1 + y2) * 0.5;
			final double nf = 1.0 / Math.sqrt(xn * xn + yn * yn);
			xn = xn * nf;
			yn = yn * nf;
			// texture coordinates
			final float tx = (float) (i + 0.5) / _radialSamples;
			_meshData.getNormalBuffer().put((float) xn).put((float) yn).put(0);
			_meshData.getTextureCoords(0).getBuffer().put(tx).put(0);
			_meshData.getVertexBuffer().put((float) x1).put((float) y1).put(-halfHeight);
			_meshData.getNormalBuffer().put((float) xn).put((float) yn).put(0);
			_meshData.getTextureCoords(0).getBuffer().put(tx).put(1);
			_meshData.getVertexBuffer().put((float) x1).put((float) y1).put(halfHeight);
			_meshData.getNormalBuffer().put((float) xn).put((float) yn).put(0);
			_meshData.getTextureCoords(0).getBuffer().put(tx).put(1);
			_meshData.getVertexBuffer().put((float) x2).put((float) y2).put(halfHeight);

			_meshData.getNormalBuffer().put((float) xn).put((float) yn).put(0);
			_meshData.getTextureCoords(0).getBuffer().put(tx).put(1);
			_meshData.getVertexBuffer().put((float) x2).put((float) y2).put(halfHeight);
			_meshData.getNormalBuffer().put((float) xn).put((float) yn).put(0);
			_meshData.getTextureCoords(0).getBuffer().put(tx).put(0);
			_meshData.getVertexBuffer().put((float) x2).put((float) y2).put(-halfHeight);
			_meshData.getNormalBuffer().put((float) xn).put((float) yn).put(0);
			_meshData.getTextureCoords(0).getBuffer().put(tx).put(0);
			_meshData.getVertexBuffer().put((float) x1).put((float) y1).put(-halfHeight);
			// next strip
			x1 = x2;
			y1 = y2;
		}

		if (_closed) {
			_meshData.getVertexBuffer().put(0).put(0).put(-halfHeight); // bottom center
			_meshData.getNormalBuffer().put(0).put(0).put(-1);
			_meshData.getTextureCoords(0).getBuffer().put(0.5f).put(0);
			_meshData.getVertexBuffer().put(0).put(0).put(halfHeight); // top center
			_meshData.getNormalBuffer().put(0).put(0).put(1);
			_meshData.getTextureCoords(0).getBuffer().put(0.5f).put(1);
		}

	}

	@Override
	public void write(final OutputCapsule capsule) throws IOException {
		super.write(capsule);
		capsule.write(_axisSamples, "axisSamples", 0);
		capsule.write(_radialSamples, "radialSamples", 0);
		capsule.write(_radius, "radius", 0);
		capsule.write(_height, "height", 0);
		capsule.write(_closed, "closed", false);
	}

	@Override
	public void read(final InputCapsule capsule) throws IOException {
		super.read(capsule);
		_axisSamples = capsule.readInt("axisSamples", 0);
		_radialSamples = capsule.readInt("radialSamples", 0);
		_radius = capsule.readDouble("radius", 0);
		_height = capsule.readDouble("height", 0);
		_closed = capsule.readBoolean("closed", false);
	}

}
