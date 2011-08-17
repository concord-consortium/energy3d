package org.concord.energy3d.shapes;

import java.nio.FloatBuffer;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.util.Util;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

public class AngleAnnotation extends Annotation {

	public AngleAnnotation() {
		super(new Arc("Angle annotation arc", 10));
		mesh.setDefaultColor(ColorRGBA.WHITE);		
		label.setTextColor(ColorRGBA.WHITE);
	}

	public void setRange(final ReadOnlyVector3 mainPoint, final ReadOnlyVector3 p2, final ReadOnlyVector3 p3, final ReadOnlyVector3 n) {
		final ReadOnlyVector3 a = new Vector3().set(p2).subtractLocal(mainPoint).normalizeLocal();
		final ReadOnlyVector3 b = new Vector3().set(p3).subtractLocal(mainPoint).normalizeLocal();		
		
		final ReadOnlyVector3 axis = n.cross(Vector3.UNIT_Z, null).normalizeLocal();
		final Matrix3 toFlat = new Matrix3().fromAngleAxis(Util.angleBetween(n, Vector3.UNIT_Z, n.cross(Vector3.UNIT_Z, null).normalizeLocal()), axis);
		final ReadOnlyVector3 aFlat = toFlat.applyPost(a, null);
		final ReadOnlyVector3 bFlat = toFlat.applyPost(b, null);
		
		final double start, end;
		if (Util.angleBetween(aFlat, bFlat, Vector3.UNIT_Z) >= 0) {
			start = Util.angleBetween(Vector3.UNIT_X, aFlat, Vector3.UNIT_Z);
			end = start + Util.angleBetween(aFlat, bFlat, Vector3.UNIT_Z);
		} else {
			start = Util.angleBetween(Vector3.UNIT_X, bFlat, Vector3.UNIT_Z);
			end = start + Util.angleBetween(bFlat, aFlat, Vector3.UNIT_Z);
		}
		final long angle = Math.round((end - start) * 180.0 / Math.PI);

		final double radius = end == start ? 0.0 : 0.3 / Math.sqrt(end - start);
		if (angle == 90) {
			final ReadOnlyVector3[] p = new ReadOnlyVector3[3];
			p[0] = a.normalize(null).multiplyLocal(0.2);
			p[1] = a.normalize(null).addLocal(b.normalize(null)).multiplyLocal(0.2);
			p[2] = b.normalize(null).multiplyLocal(0.2);
			final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
			buf.rewind();
			buf.limit(9);
			buf.rewind();
			for (final ReadOnlyVector3 v : p)
				buf.put(v.getXf()).put(v.getYf()).put(v.getZf());
			mesh.setRotation(new Matrix3());
			this.detachChild(label);
		} else {
			((Arc)mesh).set(radius, start, end);
			mesh.setRotation(toFlat.invertLocal());

			label.setText("" + angle + "\u00B0");
			label.updateModelBound();
			label.setTranslation(a.add(b, null).normalizeLocal().multiplyLocal(radius / 2.0));
			final ReadOnlyVector3 ab = a.add(b, null).normalizeLocal();
			label.setRotation(new Matrix3().fromAxes(n.cross(ab, null).normalizeLocal().negateLocal(), n, ab));
			this.attachChild(label);
		}
		mesh.updateModelBound();
		
		this.setTranslation(mainPoint);
	}	
	
}
