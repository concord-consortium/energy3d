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
 * Sphere represents a 3D object with all points equi-distance from a center point.
 */

public class MySphere extends Mesh {

	private int _zSamples;
	private int _radialSamples;
	private double _radius;
	private final Vector3 _center = new Vector3();

	public MySphere() {
	}

	/**
	 * Constructs a sphere. By default the Sphere has not geometry data or center.
	 * 
	 * @param name
	 *            The name of the sphere.
	 */
	public MySphere(final String name) {
		super(name);
	}

	/**
	 * Constructs a sphere with center at the origin. For details, see the other constructor.
	 * 
	 * @param name
	 *            Name of sphere.
	 * @param zSamples
	 *            The samples along the Z.
	 * @param radialSamples
	 *            The samples along the radial.
	 * @param radius
	 *            Radius of the sphere.
	 * @see #Sphere(java.lang.String, com.ardor3d.math.Vector3, int, int, double)
	 */
	public MySphere(final String name, final int zSamples, final int radialSamples, final double radius) {
		this(name, new Vector3(), zSamples, radialSamples, radius);
	}

	/**
	 * Constructs a sphere. All geometry data buffers are updated automatically. Both zSamples and radialSamples increase the quality of the generated sphere.
	 * 
	 * @param name
	 *            Name of the sphere.
	 * @param center
	 *            Center of the sphere.
	 * @param zSamples
	 *            The number of samples along the Z.
	 * @param radialSamples
	 *            The number of samples along the radial.
	 * @param radius
	 *            The radius of the sphere.
	 */
	public MySphere(final String name, final ReadOnlyVector3 center, final int zSamples, final int radialSamples, final double radius) {
		super(name);
		setData(center, zSamples, radialSamples, radius);
	}

	/**
	 * Changes the information of the sphere into the given values.
	 * 
	 * @param center
	 *            The new center of the sphere.
	 * @param zSamples
	 *            The new number of zSamples of the sphere.
	 * @param radialSamples
	 *            The new number of radial samples of the sphere.
	 * @param radius
	 *            The new radius of the sphere.
	 */
	public void setData(final ReadOnlyVector3 center, final int zSamples, final int radialSamples, final double radius) {

		_center.set(center);
		_zSamples = zSamples;
		_radialSamples = radialSamples;
		_radius = radius;

		// allocate vertices
		final int verts = (_zSamples - 2) * (_radialSamples + 1) + 2;
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

		// Generate points on the unit circle to be used in computing the mesh points on a sphere slice.
		final double[] afSin = new double[(_radialSamples + 1)];
		final double[] afCos = new double[(_radialSamples + 1)];
		for (int i = 0; i < _radialSamples; i++) {
			final double fAngle = MathUtils.TWO_PI * fInvRS * i;
			afCos[i] = MathUtils.cos(fAngle);
			afSin[i] = MathUtils.sin(fAngle);
		}
		afSin[_radialSamples] = afSin[0];
		afCos[_radialSamples] = afCos[0];

		// generate the sphere itself
		int i = 0;
		final Vector3 tempVa = Vector3.fetchTempInstance();
		final Vector3 tempVb = Vector3.fetchTempInstance();
		final Vector3 tempVc = Vector3.fetchTempInstance();
		for (int iZ = 1; iZ < _zSamples - 1; iZ++) {
			final double fAFraction = MathUtils.HALF_PI * (-1.0f + fZFactor * iZ); // in (-pi/2, pi/2)
			final double fZFraction = MathUtils.sin(fAFraction); // in (-1,1)
			final double fZ = _radius * fZFraction;

			// compute center of slice
			final Vector3 kSliceCenter = tempVb.set(_center);
			kSliceCenter.setZ(kSliceCenter.getZ() + fZ);

			// compute radius of slice
			final double fSliceRadius = Math.sqrt(Math.abs(_radius * _radius - fZ * fZ));

			// compute slice vertices with duplication at end point
			Vector3 kNormal;
			final int iSave = i;
			for (int iR = 0; iR < _radialSamples; iR++) {
				final double fRadialFraction = iR * fInvRS; // in [0,1)
				final Vector3 kRadial = tempVc.set(afCos[iR], afSin[iR], 0);
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

		// south pole
		_meshData.getVertexBuffer().position(i * 3);
		_meshData.getVertexBuffer().put(_center.getXf()).put(_center.getYf()).put((float) (_center.getZ() - _radius));

		_meshData.getNormalBuffer().position(i * 3);
		_meshData.getNormalBuffer().put(0).put(0).put(-1);

		_meshData.getTextureCoords(0).getBuffer().position(i * 2);
		_meshData.getTextureCoords(0).getBuffer().put(0.5f).put(0.0f);

		i++;

		// north pole
		_meshData.getVertexBuffer().put(_center.getXf()).put(_center.getYf()).put((float) (_center.getZ() + _radius));
		_meshData.getNormalBuffer().put(0).put(0).put(1);
		_meshData.getTextureCoords(0).getBuffer().put(0.5f).put(1.0f);

		Vector3.releaseTempInstance(tempVa);
		Vector3.releaseTempInstance(tempVb);
		Vector3.releaseTempInstance(tempVc);
	}

	/**
	 * Returns the center of this sphere.
	 * 
	 * @return The sphere's center.
	 */
	public Vector3 getCenter() {
		return _center;
	}

	public double getRadius() {
		return _radius;
	}

	@Override
	public void write(final OutputCapsule capsule) throws IOException {
		super.write(capsule);
		capsule.write(_zSamples, "zSamples", 0);
		capsule.write(_radialSamples, "radialSamples", 0);
		capsule.write(_radius, "radius", 0);
		capsule.write(_center, "center", new Vector3());
	}

	@Override
	public void read(final InputCapsule capsule) throws IOException {
		super.read(capsule);
		_zSamples = capsule.readInt("zSamples", 0);
		_radialSamples = capsule.readInt("radialSamples", 0);
		_radius = capsule.readDouble("radius", 0);
		_center.set((Vector3) capsule.readSavable("center", new Vector3()));
	}

}