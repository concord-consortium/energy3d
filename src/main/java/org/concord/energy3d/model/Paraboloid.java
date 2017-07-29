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
 * @author Charles Xie
 * 
 */

public class Paraboloid extends Mesh {

	private enum TextureMode {
		Linear, Polar;
	}

	private double _a, _apertureRadius;
	private int _zSamples;
	private int _rSamples;
	private final Vector3 _center = new Vector3(); // the center of the paraboloid
	private TextureMode _textureMode = TextureMode.Polar; // use polar to eleminate the seam line in linear mode

	public Paraboloid() {
	}

	/**
	 * Constructs a paraboloid with center at the origin.
	 * 
	 * @param name
	 *            Name of paraboloid.
	 * @param apertureRadius
	 *            The radius of the aperture of the paraboloid.
	 * @param a
	 *            parameter a of the round paraboloid.
	 * @param zSamples
	 *            The samples along the Z.
	 * @param radialSamples
	 *            The samples along the radial.
	 */
	public Paraboloid(final String name, final double apertureRadius, final double a, final int zSamples, final int radialSamples) {
		super(name);
		setData(apertureRadius, a, zSamples, radialSamples);
	}

	/**
	 * Changes the information of the paraboloid into the given values.
	 * 
	 * @param apertureRadius
	 *            The new radius of the paraboloid.
	 * @param a
	 *            parameter a of the elliptical paraboloid.
	 * @param zSamples
	 *            The new number of zSamples of the paraboloid.
	 * @param radialSamples
	 *            The new number of radial samples of the paraboloid.
	 */
	public void setData(final double apertureRadius, final double a, final int zSamples, final int radialSamples) {
		_apertureRadius = apertureRadius;
		_a = a;
		_zSamples = zSamples;
		_rSamples = radialSamples;
		setGeometryData();
		setIndexData();
	}

	public void setApertureRadius(final double apertureRadius) {
		_apertureRadius = apertureRadius;
		setGeometryData();
		setIndexData();
	}

	/**
	 * builds the vertices based on the aperture radius, a, b, center and radial and zSamples.
	 */
	private void setGeometryData() {
		// allocate vertices
		final int verts = (_zSamples - 1) * (_rSamples + 1);
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
		double zmax = _apertureRadius / _a;
		zmax *= zmax;
		final double inverseRSamples = 1.0 / _rSamples;
		final double zSteplength = zmax / (_zSamples - 1);

		// Generate points on the unit circle to be used in computing the mesh points on a paraboloid slice.
		final double[] sin = new double[(_rSamples + 1)];
		final double[] cos = new double[(_rSamples + 1)];
		for (int iR = 0; iR < _rSamples; iR++) {
			final double angle = MathUtils.TWO_PI * inverseRSamples * iR;
			cos[iR] = MathUtils.cos(angle);
			sin[iR] = MathUtils.sin(angle);
		}
		sin[_rSamples] = sin[0];
		cos[_rSamples] = cos[0];

		// generate the paraboloid itself
		int i = 0;
		final Vector3 tempVa = Vector3.fetchTempInstance();
		final Vector3 tempVb = Vector3.fetchTempInstance();
		final Vector3 tempVc = Vector3.fetchTempInstance();
		for (int iZ = 1; iZ < _zSamples - 1; iZ++) {
			final float zFraction = iZ / (_zSamples - 1.0f);
			final double z = zSteplength * iZ;
			final Vector3 sliceCenter = tempVb.set(_center);
			sliceCenter.setZ(sliceCenter.getZ() + z);
			final double sliceRadius = Math.sqrt(z) * _a;

			// compute slice vertices with duplication at end point
			Vector3 kNormal;
			final int iSave = i;
			for (int iR = 0; iR < _rSamples; iR++) {
				final double radialFraction = iR * inverseRSamples; // in [0,1)
				tempVc.set(cos[iR], sin[iR], 0).multiply(sliceRadius, tempVa);
				_meshData.getVertexBuffer().put((float) (sliceCenter.getX() + tempVa.getX())).put((float) (sliceCenter.getY() + tempVa.getY())).put((float) (sliceCenter.getZ() + tempVa.getZ()));

				BufferUtils.populateFromBuffer(tempVa, _meshData.getVertexBuffer(), i);
				kNormal = tempVa.subtractLocal(_center);
				kNormal.normalizeLocal();
				_meshData.getNormalBuffer().put(kNormal.getXf()).put(kNormal.getYf()).put(kNormal.getZf());

				if (_textureMode == TextureMode.Linear) {
					_meshData.getTextureCoords(0).getBuffer().put((float) radialFraction).put(zFraction);
				} else if (_textureMode == TextureMode.Polar) {
					final double r = (1.0 - Math.abs(zFraction)) * 0.5;
					final double u = r * cos[iR] + 0.5;
					final double v = r * sin[iR] + 0.5;
					_meshData.getTextureCoords(0).getBuffer().put((float) u).put((float) v);
				}

			}
			i += _rSamples;

			// close the circle by looping back to the beginning point
			BufferUtils.copyInternalVector3(_meshData.getVertexBuffer(), iSave, i);
			BufferUtils.copyInternalVector3(_meshData.getNormalBuffer(), iSave, i);
			if (_textureMode == TextureMode.Linear) {
				_meshData.getTextureCoords(0).getBuffer().put(1f).put(zFraction);
			} else if (_textureMode == TextureMode.Polar) {
				final float r = (float) ((MathUtils.HALF_PI - Math.abs(zFraction)) / MathUtils.PI);
				_meshData.getTextureCoords(0).getBuffer().put(r + 0.5f).put(0.5f);
			}
			i++;

		}

		Vector3.releaseTempInstance(tempVa);
		Vector3.releaseTempInstance(tempVb);
		Vector3.releaseTempInstance(tempVc);

	}

	/**
	 * sets the indices for rendering the paraboloid.
	 */
	private void setIndexData() {
		// allocate connectivity
		final int verts = (_zSamples - 2) * (_rSamples + 1) + 2;
		final int tris = 2 * (_zSamples - 2) * _rSamples;
		_meshData.setIndices(BufferUtils.createIndexBufferData(3 * tris, verts - 1));

		// generate connectivity
		for (int iZ = 0, iZStart = 0; iZ < (_zSamples - 3); iZ++) {
			int i0 = iZStart;
			int i1 = i0 + 1;
			iZStart += (_rSamples + 1);
			int i2 = iZStart;
			int i3 = i2 + 1;
			for (int i = 0; i < _rSamples; i++) {
				_meshData.getIndices().put(i0++);
				_meshData.getIndices().put(i1);
				_meshData.getIndices().put(i2);
				_meshData.getIndices().put(i1++);
				_meshData.getIndices().put(i3++);
				_meshData.getIndices().put(i2++);
			}
		}

		// south pole triangles
		for (int i = 0; i < _rSamples; i++) {
			_meshData.getIndices().put(i);
			_meshData.getIndices().put(_meshData.getVertexCount() - 2);
			_meshData.getIndices().put(i + 1);
		}

		// north pole triangles
		final int iOffset = (_zSamples - 3) * (_rSamples + 1);
		for (int i = 0; i < _rSamples; i++) {
			_meshData.getIndices().put(i + iOffset);
			_meshData.getIndices().put(i + 1 + iOffset);
			_meshData.getIndices().put(_meshData.getVertexCount() - 1);
		}
	}

	public double getApertureRadius() {
		return _apertureRadius;
	}

	public double getCurvatureParameter() {
		return _a;
	}

	public int getZSamples() {
		return _zSamples;
	}

	public int getRSamples() {
		return _rSamples;
	}

	@Override
	public void write(final OutputCapsule capsule) throws IOException {
		super.write(capsule);
		capsule.write(_a, "a", 0);
		capsule.write(_apertureRadius, "apertureRadius", 0);
		capsule.write(_zSamples, "zSamples", 0);
		capsule.write(_rSamples, "rSamples", 0);
		capsule.write(_center, "center", new Vector3(Vector3.ZERO));
		capsule.write(_textureMode, "textureMode", TextureMode.Linear);
	}

	@Override
	public void read(final InputCapsule capsule) throws IOException {
		super.read(capsule);
		_a = capsule.readDouble("a", 0);
		_apertureRadius = capsule.readDouble("apertureRadius", 0);
		_zSamples = capsule.readInt("zSamples", 0);
		_rSamples = capsule.readInt("rSamples", 0);
		_center.set((Vector3) capsule.readSavable("center", new Vector3(Vector3.ZERO)));
		_textureMode = capsule.readEnum("textureMode", TextureMode.class, TextureMode.Linear);
	}

}