package org.concord.energy3d.shapes;

import java.nio.FloatBuffer;

import org.concord.energy3d.util.Util;

import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

public class AngleAnnotation extends Annotation {
	private ReadOnlyVector3 mainPoint;
	private ReadOnlyVector3 p2;
	private ReadOnlyVector3 p3;
	private ReadOnlyVector3 n;

	public AngleAnnotation() {
		super(new Arc("Angle annotation arc", 10));
	}

	public void setRange(final ReadOnlyVector3 mainPoint, final ReadOnlyVector3 p2, final ReadOnlyVector3 p3, final ReadOnlyVector3 n) {
		this.mainPoint = mainPoint;
		this.p2 = p2;
		this.p3 = p3;
		this.n = n;
		draw();
	}

	@Override
	public void draw() {
		final ReadOnlyVector3 a = new Vector3().set(p2).subtractLocal(mainPoint).normalizeLocal();
		final ReadOnlyVector3 b = new Vector3().set(p3).subtractLocal(mainPoint).normalizeLocal();

		final ReadOnlyVector3 axis = n.cross(Vector3.UNIT_Z, null).normalizeLocal();
		final Matrix3 toFlat = new Matrix3().fromAngleAxis(Util.angleBetween(n, Vector3.UNIT_Z, n.cross(Vector3.UNIT_Z, null).normalizeLocal()), axis);
		final ReadOnlyVector3 aFlat = toFlat.applyPost(a, null);
		final ReadOnlyVector3 bFlat = toFlat.applyPost(b, null);

		final double start, angle;
		if (Util.angleBetween(aFlat, bFlat, Vector3.UNIT_Z) >= 0) {
			start = Util.angleBetween(Vector3.UNIT_X, aFlat, Vector3.UNIT_Z);
			angle = Util.angleBetween(aFlat, bFlat, Vector3.UNIT_Z);
		} else {
			start = Util.angleBetween(Vector3.UNIT_X, bFlat, Vector3.UNIT_Z);
			angle = Util.angleBetween(bFlat, aFlat, Vector3.UNIT_Z);
		}
		final double end = start + angle;
		final long angleDegrees = Math.round((end - start) * 180.0 / Math.PI);

		final double radius = end == start ? 0.0 : 3.0 / Math.sqrt(end - start);
		if (angleDegrees == 90) {
			final ReadOnlyVector3[] p = new ReadOnlyVector3[3];
			p[0] = a.normalize(null).multiplyLocal(2.0);
			p[1] = a.normalize(null).addLocal(b.normalize(null)).multiplyLocal(2.0);
			p[2] = b.normalize(null).multiplyLocal(2.0);
			final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
			buf.rewind();
			buf.limit(9);
			mesh.getMeshData().updateVertexCount();
			buf.rewind();
			for (final ReadOnlyVector3 v : p)
				buf.put(v.getXf()).put(v.getYf()).put(v.getZf());
			mesh.setRotation(new Matrix3());
			detachChild(label);
		} else {
			((Arc)mesh).set(radius, start, end);
			mesh.setRotation(toFlat.invertLocal());
			label.setText("" + angleDegrees + "\u00B0");
			final double start360 = start < 0 ? MathUtils.TWO_PI + start : start;
			final double angle360 = angle < 0 ? MathUtils.TWO_PI + angle : angle;
			final double end360 = start360 + angle360;
			final Matrix3 rotMatrix = toFlat.multiplyLocal(new Matrix3().fromAngles(-Math.PI / 2.0, 0, -Math.PI / 2.0 + (start360 + end360) / 2.0));
			label.setRotation(rotMatrix);
			final Vector3 trans = new Vector3(0, 0, radius / 1.7);
			label.setTranslation(rotMatrix.applyPost(trans, trans));
			attachChild(label);
		}
		mesh.updateModelBound();
//		this.setTranslation(mainPoint.add(n.multiply(0.02, null), null));
//		this.setTranslation(mainPoint.add(n.multiply(0.05, null), null));
		this.setTranslation(mainPoint);
	}
}
