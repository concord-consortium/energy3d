package org.concord.energy3d.model;

import org.concord.energy3d.util.Arc;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Mesh;

public class AngleAnnotation extends Annotation {

	protected Mesh getMesh() {
		if (mesh != null)
			return mesh;
		else {
			final Arc arc = new Arc("Angle annotation arc"); //, 10, 1, 0, Math.PI/2);
//			arc.setDefaultColor(ColorRGBA.BLACK);
			return arc;
			
		}
	}
	
	public void setRange(final ReadOnlyVector3 p1, final ReadOnlyVector3 p2, final ReadOnlyVector3 p3) {
		Vector3 a = Vector3.fetchTempInstance().set(p2).subtractLocal(p1).normalizeLocal();
		Vector3 b = Vector3.fetchTempInstance().set(p3).subtractLocal(p1).normalizeLocal();		
		Vector3 n = Vector3.fetchTempInstance().set(a).crossLocal(b).normalizeLocal();
		Vector3 v = Vector3.fetchTempInstance();
		Matrix3 m1 = Matrix3.fetchTempInstance();
		Matrix3 m2 = Matrix3.fetchTempInstance();
		
		v.set(a).crossLocal(n).normalizeLocal();
		double start = a.smallestAngleBetween(Vector3.UNIT_X);
		if (a.dot(Vector3.UNIT_Z) < 0)
			start = -start;		
		double end = b.smallestAngleBetween(Vector3.UNIT_X);
		if (b.dot(Vector3.UNIT_Z) < 0)
			end = -end;
		
		
		System.out.println(p1);
		System.out.println(p2);
		System.out.println(p3);
		System.out.println(a);
		System.out.println(b);
		System.out.println("start = " + start* 180 / Math.PI);
		System.out.println("end = " + end * 180 / Math.PI);
		
		double angle = a.smallestAngleBetween(b);
//		double aAngle = v.set(a).crossLocal(n).smallestAngleBetween(a);
		double aAngle = v.smallestAngleBetween(a);
//		double aAngle = 0.5;
		m2.fromAngleAxis(aAngle, v);
		
		double rot = a.crossLocal(b).smallestAngleBetween(Vector3.UNIT_Z);		
		a.crossLocal(Vector3.UNIT_Z);
		m1.fromAngleAxis(rot, a);
		
//		m2.multiplyLocal(m1);
		this.setRotation(m1);
		this.setTranslation(p1.getX(), -0.1, p1.getZ());
		
		
		Arc arc = (Arc)mesh;
		arc.set(10, 1, start, end);
		
		
		Vector3.releaseTempInstance(a);
		Vector3.releaseTempInstance(b);
		Vector3.releaseTempInstance(n);
		Vector3.releaseTempInstance(v);
		Matrix3.releaseTempInstance(m1);
		Matrix3.releaseTempInstance(m2);
		
	}
}
