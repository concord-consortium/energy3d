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

/**
 * This models an elliptical paraboloid z = x^2 / a^2 + y^2 / b^2
 * 
 * @author Charles Xie
 * 
 */

public class Paraboloid extends Mesh {

	private double _a, _b, _apertureRadius;
	private int _zSamples;
	private int _radialSamples;
	private final Vector3 _center = new Vector3(); // the center of the paraboloid

	/**
	 * Constructs a paraboloid with center at the origin.
	 * 
	 * @param name
	 *            Name of paraboloid.
	 * @param apertureRadius
	 *            The radius of the aperture of the paraboloid.
	 * @param a
	 *            parameter a of the elliptical paraboloid.
	 * @param b
	 *            parameter b of the elliptical paraboloid.
	 * @param zSamples
	 *            The samples along the Z.
	 * @param radialSamples
	 *            The samples along the radial.
	 */
	public Paraboloid(final String name, final double apertureRadius, final double a, final double b, final int zSamples, final int radialSamples) {
		super(name);
		setData(new Vector3(), apertureRadius, a, b, zSamples, radialSamples);
	}

	/**
	 * Changes the information of the paraboloid into the given values.
	 * 
	 * @param center
	 *            The new center of the paraboloid.
	 * @param apertureRadius
	 *            The new radius of the paraboloid.
	 * @param a
	 *            parameter a of the elliptical paraboloid.
	 * @param b
	 *            parameter b of the elliptical paraboloid.
	 * @param zSamples
	 *            The new number of zSamples of the paraboloid.
	 * @param radialSamples
	 *            The new number of radial samples of the paraboloid.
	 */
	public void setData(final ReadOnlyVector3 center, final double apertureRadius, final double a, final double b, final int zSamples, final int radialSamples) {
		_center.set(center);
		_apertureRadius = apertureRadius;
		_a = a;
		_b = b;
		_zSamples = zSamples;
		_radialSamples = radialSamples;
		setGeometryData();
		setIndexData();
	}

	/**
	 * builds the vertices based on the aperture radius, a, b, center and radial and zSamples.
	 */
	private void setGeometryData() {
		// allocate vertices
		final int verts = (_zSamples - 1) * (_radialSamples + 1) + 1;
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

		// generate geometry
		final double fInvRS = 1.0 / _radialSamples;
		final double fZFactor = 2.0 / (_zSamples - 1);

		// Generate points on the unit circle to be used in computing the mesh points on a paraboloid slice.
		final double[] sin = new double[(_radialSamples + 1)];
		final double[] cos = new double[(_radialSamples + 1)];
		for (int iR = 0; iR < _radialSamples; iR++) {
			final double angle = MathUtils.TWO_PI * fInvRS * iR;
			cos[iR] = MathUtils.cos(angle);
			sin[iR] = MathUtils.sin(angle);
		}
		sin[_radialSamples] = sin[0];
		cos[_radialSamples] = cos[0];

		// generate the paraboloid itself
		int i = 0;
		final Vector3 tempVa = Vector3.fetchTempInstance();
		final Vector3 tempVb = Vector3.fetchTempInstance();
		final Vector3 tempVc = Vector3.fetchTempInstance();
		for (int iZ = 1; iZ < _zSamples; iZ++) {
			final double fAFraction = MathUtils.HALF_PI * (-1.0f + fZFactor * iZ); // in (-pi/2, pi/2)
			final double fZFraction = MathUtils.sin(fAFraction); // in (-1,1)
			final double fZ = _apertureRadius * fZFraction;

			// compute center of slice
			final Vector3 kSliceCenter = tempVb.set(_center);
			kSliceCenter.setZ(kSliceCenter.getZ() + fZ);

			// compute radius of slice
			final double fSliceRadius = Math.sqrt(Math.abs(_apertureRadius * _apertureRadius - fZ * fZ));

			// compute slice vertices with duplication at end point
			Vector3 kNormal;
			final int iSave = i;
			for (int iR = 0; iR < _radialSamples; iR++) {
				final double fRadialFraction = iR * fInvRS; // in [0,1)
				final Vector3 kRadial = tempVc.set(cos[iR], sin[iR], 0);
				kRadial.multiply(fSliceRadius, tempVa);
				_meshData.getVertexBuffer().put((float) (kSliceCenter.getX() + tempVa.getX())).put((float) (kSliceCenter.getY() + tempVa.getY())).put((float) (kSliceCenter.getZ() + tempVa.getZ()));

				BufferUtils.populateFromBuffer(tempVa, _meshData.getVertexBuffer(), i);
				kNormal = tempVa.subtractLocal(_center);
				kNormal.normalizeLocal();
				_meshData.getNormalBuffer().put(kNormal.getXf()).put(kNormal.getYf()).put(kNormal.getZf());

				_meshData.getTextureCoords(0).getBuffer().put((float) fRadialFraction).put((float) (0.5 * (fZFraction + 1.0)));

				i++;
			}

			BufferUtils.copyInternalVector3(_meshData.getVertexBuffer(), iSave, i);
			BufferUtils.copyInternalVector3(_meshData.getNormalBuffer(), iSave, i);

			_meshData.getTextureCoords(0).getBuffer().put(1.0f).put((float) (0.5 * (fZFraction + 1.0)));

			i++;
		}

		// the center is at the bottom of the paraboloid
		_meshData.getVertexBuffer().position(i * 3);
		_meshData.getVertexBuffer().put(_center.getXf()).put(_center.getYf()).put(_center.getZf());
		_meshData.getNormalBuffer().position(i * 3);
		_meshData.getNormalBuffer().put(0).put(0).put(-1);
		_meshData.getTextureCoords(0).getBuffer().position(i * 2);
		_meshData.getTextureCoords(0).getBuffer().put(0.5f).put(0.0f);

		Vector3.releaseTempInstance(tempVa);
		Vector3.releaseTempInstance(tempVb);
		Vector3.releaseTempInstance(tempVc);

	}

	/**
	 * sets the indices for rendering the paraboloid.
	 */
	private void setIndexData() {
		// allocate connectivity
		final int verts = (_zSamples - 2) * (_radialSamples + 1) + 2;
		final int tris = 2 * (_zSamples - 2) * _radialSamples;
		_meshData.setIndices(BufferUtils.createIndexBufferData(3 * tris, verts - 1));

		// generate connectivity
		for (int iZ = 0, iZStart = 0; iZ < (_zSamples - 3); iZ++) {
			int i0 = iZStart;
			int i1 = i0 + 1;
			iZStart += (_radialSamples + 1);
			int i2 = iZStart;
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

		// south pole triangles
		for (int i = 0; i < _radialSamples; i++) {
			_meshData.getIndices().put(i);
			_meshData.getIndices().put(_meshData.getVertexCount() - 2);
			_meshData.getIndices().put(i + 1);
		}

		// north pole triangles
		final int iOffset = (_zSamples - 3) * (_radialSamples + 1);
		for (int i = 0; i < _radialSamples; i++) {
			_meshData.getIndices().put(i + iOffset);
			_meshData.getIndices().put(i + 1 + iOffset);
			_meshData.getIndices().put(_meshData.getVertexCount() - 1);
		}
	}

	/**
	 * Returns the center of this paraboloid.
	 * 
	 * @return The paraboloid's center.
	 */
	public Vector3 getCenter() {
		return _center;
	}

	public double getApertureRadius() {
		return _apertureRadius;
	}

	public double getA() {
		return _a;
	}

	public double getB() {
		return _b;
	}

	@Override
	public void write(final OutputCapsule capsule) throws IOException {
		super.write(capsule);
		capsule.write(_a, "a", 0);
		capsule.write(_b, "b", 0);
		capsule.write(_apertureRadius, "apertureRadius", 0);
		capsule.write(_zSamples, "zSamples", 0);
		capsule.write(_radialSamples, "radialSamples", 0);
		capsule.write(_center, "center", new Vector3(Vector3.ZERO));
	}

	@Override
	public void read(final InputCapsule capsule) throws IOException {
		super.read(capsule);
		_a = capsule.readDouble("a", 0);
		_b = capsule.readDouble("b", 0);
		_apertureRadius = capsule.readDouble("apertureRadius", 0);
		_zSamples = capsule.readInt("zSamples", 0);
		_radialSamples = capsule.readInt("radialSamples", 0);
		_center.set((Vector3) capsule.readSavable("center", new Vector3(Vector3.ZERO)));
	}

}