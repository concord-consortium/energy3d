package org.concord.energy3d.util;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.concord.energy3d.model.HousePart;
import org.poly2tri.geometry.polygon.PolygonPoint;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

public class Util {
//	public static final boolean DEBUG = false;
	
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
	
	public static double findExactHeight(final ArrayList<HousePart> parts) {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (final HousePart part : parts) {
			for (final Vector3 p : part.getAbsPoints()) {
				final double z = p.getZ();
				if (z > max)
					max = z;
				if (z < min)
					min = z;
			}				
		}
		return max - min;
	}	

	/** require that a and b are normalized **/
	public static double angleBetween(ReadOnlyVector3 a, ReadOnlyVector3 b, ReadOnlyVector3 n) {
//	    double angle;
//	    Vector3 v = new Vector3(b);
//
//	    // a and b parallel?
//	    if( a.equals(v) ) {
//	        return 0;
//	    }
//	    if( a.equals( v.negateLocal() )) {
//	        return Math.PI;
//	    }
//
//	    a.cross(b,v);
//	    if (v.dot(n) < 0)
//	    	v.negateLocal();
//	    v.cross(a,v).normalizeLocal();
////	    v.set(Math.abs(v.getX()), Math.abs(v.getY()), Math.abs(v.getZ()));
//
//	    angle = Math.atan2(b.dot(v), b.dot(a));
//	    if( angle < 0 ) {
//	        angle += 2*Math.PI;
//	    }
	    return Math.atan2(b.dot(n.cross(a, null)), b.dot(a));
	}
	
	public static long degree(final double radian) {
		return Math.round(radian * 180.0 / Math.PI);
	}
	
	public static String toString(final ReadOnlyVector3 v) {
		return "(" + Math.round(v.getX()*100) / 100.0 + ", " + Math.round(v.getY()*100) / 100.0 + ", " + Math.round(v.getZ()*100) / 100.0 + ")";
	}

	public static String toString(final double v) {
		return "" + Math.round(v*100) / 100.0;
	}

	public static Vector3 get(final Vector3 p, final FloatBuffer buf) {		
		return p.set(buf.get(), buf.get(), buf.get());
	}

	public static void put(final Vector3 p, final FloatBuffer buf) {		
		buf.put(p.getXf()).put(p.getYf()).put(p.getZf());		
	}

	public static PolygonPoint toPolygonPoint(Vector3 p) {
		return new PolygonPoint(p.getX(), p.getY(), p.getZ());
	}
	
}
