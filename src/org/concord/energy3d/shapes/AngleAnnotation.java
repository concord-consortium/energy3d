package org.concord.energy3d.shapes;

import org.concord.energy3d.model.HousePart;

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

	public void setRange(final ReadOnlyVector3 p1, final ReadOnlyVector3 p2, final ReadOnlyVector3 p3) {
		final Vector3 a = new Vector3().set(p2).subtractLocal(p1).normalizeLocal();
		final Vector3 b = new Vector3().set(p3).subtractLocal(p1).normalizeLocal();		
		final Vector3 n = new Vector3().set(a).crossLocal(b).normalizeLocal();
		final Vector3 v = new Vector3();
		final Matrix3 m1 = new Matrix3();
		
		v.set(n).crossLocal(a).normalizeLocal();

		double start = 0;
		double end = a.smallestAngleBetween(b);
		
		v.set(n).crossLocal(Vector3.UNIT_Z).normalizeLocal();
		double angleOffset = a.smallestAngleBetween(Vector3.UNIT_X);
		if (a.dot(Vector3.UNIT_Z) < 0)
			angleOffset = -angleOffset;
		start += angleOffset;
		end += angleOffset;

		Arc arc = (Arc)mesh;
		arc.set(10, 0.2, start, end);
		
		m1.fromAngleAxis(-n.smallestAngleBetween(Vector3.UNIT_Z), v);
		mesh.setRotation(m1);
		this.setTranslation(p1.getX(), p1.getY()-0.01, p1.getZ());
		
		v.set(a).addLocal(b).normalizeLocal();
		final Align align = getPreferedAlignment(v);
		label.setAlign(align);
		v.multiplyLocal(0.22);		
		mesh.setDefaultColor(ColorRGBA.WHITE);
		
		label.setTranslation(v);
		label.setTextColor(ColorRGBA.WHITE);
		label.setText(""+Math.round(a.smallestAngleBetween(b)*180/Math.PI)+"\u00B0");
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
