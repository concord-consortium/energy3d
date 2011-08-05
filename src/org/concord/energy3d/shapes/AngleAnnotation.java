package org.concord.energy3d.shapes;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.util.Util;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;

public class AngleAnnotation extends Annotation {
	protected final BMText label = makeNewLabel();

	public AngleAnnotation(final HousePart housePart) {
		super(new Arc("Angle annotation arc"));
		this.attachChild(label);
	}

	public void setRange(final ReadOnlyVector3 mainPoint, final ReadOnlyVector3 p2, final ReadOnlyVector3 p3, final ReadOnlyVector3 n) {
		final ReadOnlyVector3 a = new Vector3().set(p2).subtractLocal(mainPoint).normalizeLocal();
		final ReadOnlyVector3 b = new Vector3().set(p3).subtractLocal(mainPoint).normalizeLocal();		
//		final ReadOnlyVector3 n = new Vector3().set(a).crossLocal(b).normalizeLocal();
		final Vector3 v = new Vector3();
//		final Matrix3 m1 = new Matrix3();
		
		final Matrix3 toFlat = new Matrix3().fromAngleAxis(Util.angleBetween(n, Vector3.UNIT_Z, n.cross(Vector3.UNIT_Z, null)), n.cross(Vector3.UNIT_Z, null));
		final ReadOnlyVector3 aFlat = toFlat.applyPost(a, null);
		final ReadOnlyVector3 bFlat = toFlat.applyPost(b, null);
		
//		if (b.dot(Vector3.UNIT_X) > a.dot(Vector3.UNIT_X)) {
//			final Vector3 tmp = a;
//			a = b;
//			b = tmp;
//		}
		
//		v.set(n).crossLocal(a).normalizeLocal();
		
		double start, end;
		if (Util.angleBetween(aFlat, bFlat, Vector3.UNIT_Z) >= 0) {
			start = Util.angleBetween(Vector3.UNIT_X, aFlat, Vector3.UNIT_Z);
			end = start + Util.angleBetween(aFlat, bFlat, Vector3.UNIT_Z);
		} else {
			start = Util.angleBetween(Vector3.UNIT_X, bFlat, Vector3.UNIT_Z);
			end = start + Util.angleBetween(bFlat, aFlat, Vector3.UNIT_Z);
		}

//		double start = 0;
//		double end = a.smallestAngleBetween(b);
//		if (a.cross(b, null).dot(Vector3.UNIT_Z) < 0)
//			end = -end;
		
		v.set(n).crossLocal(Vector3.UNIT_Z).normalizeLocal();
//		double angleOffset = a.smallestAngleBetween(Vector3.UNIT_X);
//		if (a.dot(Vector3.UNIT_Z) < 0)
//			angleOffset = -angleOffset;
//		start += angleOffset;
//		end += angleOffset;
//
//		Arc arc = (Arc)mesh;
//		arc.set(10, 0.2, start, end);
//		
//		mesh.updateModelBound();
		
//		m1.fromAngleAxis(-n.smallestAngleBetween(Vector3.UNIT_Z), v);
//		m1.fromAngleAxis(n.smallestAngleBetween(Vector3.UNIT_Z), v);
//		mesh.setRotation(m1);
		
		mesh.setRotation(toFlat.invertLocal());
//		this.setRotation(toFlat.invertLocal());
		
//		mesh.setRotation(new Matrix3());
//		mesh.setRotation(new Matrix3().fromAngleAxis(a.smallestAngleBetween(Vector3.UNIT_X), a.cross(Vector3.UNIT_X, null)));
//		this.setTranslation(mainPoint.getX(), mainPoint.getY()-0.01, mainPoint.getZ());
		this.setTranslation(n.multiply(0.01, null).addLocal(mainPoint));
		
//		start += angleOffset; // - n.multiply(new Vector3(1, 1, 0), null).normalizeLocal().smallestAngleBetween(Vector3.NEG_UNIT_Y);
//		end += angleOffset; // - n.multiply(new Vector3(1, 1, 0), null).normalizeLocal().smallestAngleBetween(Vector3.NEG_UNIT_Y);
		
//		start -= Math.PI / 2.0;
//		end -= Math.PI / 2.0;
		
		System.out.println(Util.toString(a) + "\t" + Util.toString(b) + "\t" + start + "\t" + end);

		Arc arc = (Arc)mesh;
		arc.set(10, 0.2, start, end);
		
		mesh.updateModelBound();
		
		
		v.set(a).addLocal(b).normalizeLocal();
//		final Align align = getPreferedAlignment(v);
		final Align align; // = getPreferedAlignment(a);
//		double avgAngle = (start + end) / 2;
//		if (avgAngle < 0)
//			avgAngle = Math.PI * 2 + avgAngle;
//		final double avgAngle = Util.angleBetween(dir, b, n);
//		final int angleSector = (int)Math.round(avgAngle / Math.PI / 2 * 8.0);
//		final Align[] aligns = {Align.West, Align.SouthWest, Align.South, Align.SouthEast, Align.East, Align.NorthEast, Align.North, Align.NorthWest, Align.West};
		
//		label.setAlign(aligns[angleSector]);
//		label.setAlign(getPreferedAlignment(v));
		
		v.multiplyLocal(0.22);		
		mesh.setDefaultColor(ColorRGBA.WHITE);
		
//		label.setTranslation(v);
		if (Double.isNaN(n.smallestAngleBetween(Vector3.UNIT_Y)))
				System.out.println(n.smallestAngleBetween(Vector3.UNIT_Y));
//		label.setRotation(new Matrix3().fromAngleAxis(Math.PI-n.smallestAngleBetween(Vector3.UNIT_Y), n.cross(Vector3.UNIT_Y, null)));
//		label.setRotation(new Matrix3().fromAngleAxis(Math.PI - Util.angleBetween(n, Vector3.UNIT_Y, n.cross(Vector3.UNIT_Y, null)), n.cross(Vector3.UNIT_Y, null)));
//		label.setRotation(new Matrix3().fromAngles(-Math.PI / 2.0, 0, 0));
		
//        new Matrix3().fromAxes(_left, _look, cam.getUp());
		label.setRotation(new Matrix3().fromAxes(a, n, b));
		
		label.setTextColor(ColorRGBA.WHITE);
		label.setText(""+Math.round(a.smallestAngleBetween(b)*180/Math.PI)+"\u00B0");
		
		label.updateModelBound();
	}	
	
	private Align getPreferedAlignment(Vector3 dir) {
		int h, v;
		double dot = dir.dot(Vector3.UNIT_X);
		if (dot > 0.5)
			h = 1;
		else if (dot < -0.5)
			h = 2;
		else
			h = 0;

		dot = dir.dot(Vector3.UNIT_Z);
		if (dot > 0.5)
			v = 2;
		else if (dot < -0.5)
			v = 0;
		else
			v = 1;
		return Align.values()[v*3+h];
	}	
}
