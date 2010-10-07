package org.concord.energy3d.util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

public class Util {
	public static final boolean DEBUG = false;
	
	public static double findBoundLength(final BoundingVolume bounds) {
		double d;
		if (bounds instanceof BoundingSphere)
			d = ((BoundingSphere)bounds).getRadius() * 2;
		else {
			final BoundingBox boundingBox = (BoundingBox)bounds;
			d = Math.max(boundingBox.getXExtent(), Math.max(boundingBox.getYExtent(), boundingBox.getZExtent()));
		}
		return d;
	}
	
	/** require that a and b are normalized **/
	public static double angleBetween(ReadOnlyVector3 a, ReadOnlyVector3 b, ReadOnlyVector3 n) {
	    double angle;
	    Vector3 v = new Vector3(b);

	    // a and b parallel?
	    if( a.equals(v) ) {
	        return 0;
	    }
	    if( a.equals( v.negateLocal() )) {
	        return Math.PI;
	    }

	    a.cross(b,v);
	    if (v.dot(n) < 0)
	    	v.negateLocal();
	    v.cross(a,v).normalizeLocal();
//	    v.set(Math.abs(v.getX()), Math.abs(v.getY()), Math.abs(v.getZ()));

	    angle = Math.atan2(b.dot(v), b.dot(a));
	    if( angle < 0 ) {
	        angle += 2*Math.PI;
	    }
	    return angle;
	}
}
