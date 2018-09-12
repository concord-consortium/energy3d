package org.concord.energy3d.model;

import java.io.IOException;

import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.geom.BufferUtils;

/**
 * This models a paraboloid z = x^2 / a^2 + y^2 / a^2
 * 
 * The focal length f = a^2 / 4. So a = 2 * sqrt(f)
 * 
 * @author Charles Xie
 * 
 */

public class Paraboloid extends Mesh {

	private double a, rimRadius;
	private int zSamples;
	private int rSamples;
	private final Vector3 center = new Vector3();

	public Paraboloid() {
	}

	public Paraboloid(final String name, final double rimRadius, final double a, final int zSamples, final int rSamples) {
		super(name);
		setData(rimRadius, a, zSamples, rSamples);
	}

	public void setData(final double rimRadius, final double a, final int zSamples, final int rSamples) {
		this.rimRadius = rimRadius;
		this.a = a;
		this.zSamples = zSamples;
		this.rSamples = rSamples;
		allocateVertices();
	}

	/**
	 * builds the vertices based on the rim radius, a, center, radial, and axial samples.
	 */
	private void allocateVertices() {

		// allocate vertices
		final int verts = 6 * (zSamples - 1) * (rSamples + 1);
		final FloatBufferData vertsData = _meshData.getVertexCoords();
		if (vertsData == null) {
			_meshData.setVertexBuffer(BufferUtils.createVector3Buffer(verts));
		} else {
			vertsData.setBuffer(BufferUtils.createVector3Buffer(vertsData.getBuffer(), verts));
		}

		// allocate normals if requested
		final FloatBufferData normsData = _meshData.getNormalCoords();
		if (normsData == null) {
			_meshData.setNormalBuffer(BufferUtils.createVector3Buffer(verts));
		} else {
			normsData.setBuffer(BufferUtils.createVector3Buffer(normsData.getBuffer(), verts));
		}

		// allocate texture coordinates
		final FloatBufferData texData = _meshData.getTextureCoords(0);
		if (texData == null) {
			_meshData.setTextureBuffer(BufferUtils.createVector2Buffer(verts), 0);
		} else {
			texData.setBuffer(BufferUtils.createVector2Buffer(texData.getBuffer(), verts));
		}

		// generate geometry: Depth = R^2/a^2
		double depth = rimRadius / a;
		depth *= depth;
		final double inverseRSamples = 1.0 / rSamples;
		final double zSteplength = depth / (zSamples - 1);
		final double theta = MathUtils.TWO_PI * inverseRSamples;
		final double cosr = Math.cos(theta);
		final double sinr = Math.sin(theta);
		double dx1, dy1, dz1;
		double dx2, dy2, dz2;
		double dx3, dy3, dz3;
		double dx4, dy4, dz4;
		double xn, yn, zn, nf;
		final double cx = center.getX();
		final double cy = center.getY();
		final double cz = center.getZ();
		final double[] dz = new double[zSamples - 1];
		final double[] dr = new double[zSamples - 1];
		for (int i = 1; i < zSamples - 1; i++) {
			dz[i] = zSteplength * i;
			dr[i] = Math.sqrt(dz[i]) * a;
		}

		for (int i = 1; i < zSamples - 2; i++) {

			final float zFraction = (float) i / (float) (zSamples - 1);
			dx1 = dr[i];
			dy1 = 0;
			dz1 = dz[i];
			dx2 = dr[i + 1];
			dy2 = 0;
			dz2 = dz[i + 1];
			dz3 = dz2;
			dz4 = dz1;

			for (int j = 0; j <= rSamples; j++) {

				dx3 = cosr * dx2 - sinr * dy2;
				dy3 = sinr * dx2 + cosr * dy2;
				dx4 = cosr * dx1 - sinr * dy1;
				dy4 = sinr * dx1 + cosr * dy1;
				// normal vector
				xn = (dx1 + dx2) * 0.5;
				yn = (dy1 + dy2) * 0.5;
				zn = (dz1 + dz2) * 0.5;
				nf = 1.0 / Math.sqrt(xn * xn + yn * yn + zn * zn);
				xn = xn * nf;
				yn = yn * nf;
				zn = zn * nf;

				_meshData.getVertexBuffer().put((float) (cx + dx1)).put((float) (cy + dy1)).put((float) (cz + dz1));
				_meshData.getNormalBuffer().put((float) xn).put((float) yn).put((float) zn);
				_meshData.getTextureCoords(0).getBuffer().put((float) (j * inverseRSamples)).put(zFraction);

				_meshData.getVertexBuffer().put((float) (cx + dx2)).put((float) (cy + dy2)).put((float) (cz + dz2));
				_meshData.getNormalBuffer().put((float) xn).put((float) yn).put((float) zn);
				_meshData.getTextureCoords(0).getBuffer().put((float) (j * inverseRSamples)).put(zFraction);

				_meshData.getVertexBuffer().put((float) (cx + dx3)).put((float) (cy + dy3)).put((float) (cz + dz3));
				_meshData.getNormalBuffer().put((float) xn).put((float) yn).put((float) zn);
				_meshData.getTextureCoords(0).getBuffer().put((float) (j * inverseRSamples)).put(zFraction);

				_meshData.getVertexBuffer().put((float) (cx + dx3)).put((float) (cy + dy3)).put((float) (cz + dz3));
				_meshData.getNormalBuffer().put((float) xn).put((float) yn).put((float) zn);
				_meshData.getTextureCoords(0).getBuffer().put((float) (j * inverseRSamples)).put(zFraction);

				_meshData.getVertexBuffer().put((float) (cx + dx4)).put((float) (cy + dy4)).put((float) (cz + dz4));
				_meshData.getNormalBuffer().put((float) xn).put((float) yn).put((float) zn);
				_meshData.getTextureCoords(0).getBuffer().put((float) (j * inverseRSamples)).put(zFraction);

				_meshData.getVertexBuffer().put((float) (cx + dx1)).put((float) (cy + dy1)).put((float) (cz + dz1));
				_meshData.getNormalBuffer().put((float) xn).put((float) yn).put((float) zn);
				_meshData.getTextureCoords(0).getBuffer().put((float) (j * inverseRSamples)).put(zFraction);

				// next strip
				dx1 = dx4;
				dy1 = dy4;
				dx2 = dx3;
				dy2 = dy3;

			}

			// close the circle by looping back to the beginning point
			// final float r = (float) ((MathUtils.HALF_PI - Math.abs(zFraction)) / MathUtils.PI);
			// _meshData.getTextureCoords(0).getBuffer().put(r + 0.5f).put(0.5f);

		}

	}

	public double getRimRadius() {
		return rimRadius;
	}

	public void setRimRadius(final double rimRadius) {
		this.rimRadius = rimRadius;
		allocateVertices();
	}

	public double getCurvatureParameter() {
		return a;
	}

	public void setCurvatureParameter(final double a) {
		this.a = a;
		allocateVertices();
	}

	public void setZSamples(final int zSamples) {
		this.zSamples = zSamples;
		allocateVertices();
	}

	public int getZSamples() {
		return zSamples;
	}

	public void setRSamples(final int rSamples) {
		this.rSamples = rSamples;
		allocateVertices();
	}

	public int getRSamples() {
		return rSamples;
	}

	@Override
	public void write(final OutputCapsule capsule) throws IOException {
		super.write(capsule);
		capsule.write(a, "a", 0);
		capsule.write(rimRadius, "rimRadius", 0);
		capsule.write(zSamples, "zSamples", 0);
		capsule.write(rSamples, "rSamples", 0);
		capsule.write(center, "center", new Vector3());
	}

	@Override
	public void read(final InputCapsule capsule) throws IOException {
		super.read(capsule);
		a = capsule.readDouble("a", 0);
		rimRadius = capsule.readDouble("rimRadius", 0);
		zSamples = capsule.readInt("zSamples", 0);
		rSamples = capsule.readInt("rSamples", 0);
		center.set((Vector3) capsule.readSavable("center", new Vector3()));
	}

}