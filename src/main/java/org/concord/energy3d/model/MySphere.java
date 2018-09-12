package org.concord.energy3d.model;

import java.io.IOException;

import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.geom.BufferUtils;

public class MySphere extends Mesh {

	private int zSamples;
	private int radialSamples;
	private double radius;
	private final Vector3 center = new Vector3();

	public MySphere() {
	}

	public MySphere(final String name) {
		super(name);
	}

	public MySphere(final String name, final int zSamples, final int radialSamples, final double radius) {
		this(name, new Vector3(), zSamples, radialSamples, radius);
	}

	public MySphere(final String name, final ReadOnlyVector3 center, final int zSamples, final int radialSamples, final double radius) {
		super(name);
		setData(center, zSamples, radialSamples, radius);
	}

	public void setData(final ReadOnlyVector3 centerPoint, final int zSteps, final int rSteps, final double rad) {

		center.set(centerPoint);
		zSamples = zSteps;
		radialSamples = rSteps;
		radius = rad;

		// allocate vertices
		final int verts = 6 * (zSamples - 1) * (radialSamples + 1) + 2;
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

		final double inverseRadialSamples = 1.0 / radialSamples;
		final double zFactor = 2.0 / (zSamples - 1);
		final double theta = MathUtils.TWO_PI * inverseRadialSamples;
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
		final double[] sinz = new double[zSamples - 1];
		final double[] dz = new double[zSamples - 1];
		final double[] dr = new double[zSamples - 1];
		for (int i = 0; i < zSamples - 1; i++) {
			sinz[i] = Math.sin(MathUtils.HALF_PI * (-1.0 + zFactor * i)); // angle in -pi/2 and pi/2
			dz[i] = radius * sinz[i];
			dr[i] = Math.sqrt(Math.abs(radius * radius - dz[i] * dz[i]));
		}

		for (int i = 0; i < zSamples - 2; i++) {

			dx1 = dr[i];
			dy1 = 0;
			dz1 = dz[i];
			dx2 = dr[i + 1];
			dy2 = 0;
			dz2 = dz[i + 1];
			dz3 = dz2;
			dz4 = dz1;

			for (int j = 0; j <= radialSamples; j++) {

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
				_meshData.getTextureCoords(0).getBuffer().put((float) (j * inverseRadialSamples)).put((float) (0.5 * (sinz[i] + 1.0)));

				_meshData.getVertexBuffer().put((float) (cx + dx2)).put((float) (cy + dy2)).put((float) (cz + dz2));
				_meshData.getNormalBuffer().put((float) xn).put((float) yn).put((float) zn);
				_meshData.getTextureCoords(0).getBuffer().put((float) (j * inverseRadialSamples)).put((float) (0.5 * (sinz[i + 1] + 1.0)));

				_meshData.getVertexBuffer().put((float) (cx + dx3)).put((float) (cy + dy3)).put((float) (cz + dz3));
				_meshData.getNormalBuffer().put((float) xn).put((float) yn).put((float) zn);
				_meshData.getTextureCoords(0).getBuffer().put((float) (j * inverseRadialSamples)).put((float) (0.5 * (sinz[i + 1] + 1.0)));

				_meshData.getVertexBuffer().put((float) (cx + dx3)).put((float) (cy + dy3)).put((float) (cz + dz3));
				_meshData.getNormalBuffer().put((float) xn).put((float) yn).put((float) zn);
				_meshData.getTextureCoords(0).getBuffer().put((float) (j * inverseRadialSamples)).put((float) (0.5 * (sinz[i + 1] + 1.0)));

				_meshData.getVertexBuffer().put((float) (cx + dx4)).put((float) (cy + dy4)).put((float) (cz + dz4));
				_meshData.getNormalBuffer().put((float) xn).put((float) yn).put((float) zn);
				_meshData.getTextureCoords(0).getBuffer().put((float) (j * inverseRadialSamples)).put((float) (0.5 * (sinz[i] + 1.0)));

				_meshData.getVertexBuffer().put((float) (cx + dx1)).put((float) (cy + dy1)).put((float) (cz + dz1));
				_meshData.getNormalBuffer().put((float) xn).put((float) yn).put((float) zn);
				_meshData.getTextureCoords(0).getBuffer().put((float) (j * inverseRadialSamples)).put((float) (0.5 * (sinz[i] + 1.0)));

				// next strip
				dx1 = dx4;
				dy1 = dy4;
				dx2 = dx3;
				dy2 = dy3;

			}

			_meshData.getTextureCoords(0).getBuffer().put(1.0f).put((float) (0.5 * (sinz[i] + 1.0)));

		}

		// TODO: Handle the poles

		final int i = verts - 2;
		// south pole
		_meshData.getVertexBuffer().position(i * 3);
		_meshData.getVertexBuffer().put(center.getXf()).put(center.getYf()).put((float) (center.getZ() - radius));
		_meshData.getNormalBuffer().position(i * 3);
		_meshData.getNormalBuffer().put(0).put(0).put(-1);
		_meshData.getTextureCoords(0).getBuffer().position(i * 2);
		_meshData.getTextureCoords(0).getBuffer().put(0.5f).put(0.0f);

		// north pole
		_meshData.getVertexBuffer().put(center.getXf()).put(center.getYf()).put((float) (center.getZ() + radius));
		_meshData.getNormalBuffer().put(0).put(0).put(1);
		_meshData.getTextureCoords(0).getBuffer().put(0.5f).put(1.0f);

	}

	/**
	 * Returns the center of this sphere.
	 * 
	 * @return The sphere's center.
	 */
	public Vector3 getCenter() {
		return center;
	}

	public double getRadius() {
		return radius;
	}

	@Override
	public void write(final OutputCapsule capsule) throws IOException {
		super.write(capsule);
		capsule.write(zSamples, "zSamples", 0);
		capsule.write(radialSamples, "radialSamples", 0);
		capsule.write(radius, "radius", 0);
		capsule.write(center, "center", new Vector3());
	}

	@Override
	public void read(final InputCapsule capsule) throws IOException {
		super.read(capsule);
		zSamples = capsule.readInt("zSamples", 0);
		radialSamples = capsule.readInt("radialSamples", 0);
		radius = capsule.readDouble("radius", 0);
		center.set((Vector3) capsule.readSavable("center", new Vector3()));
	}

}