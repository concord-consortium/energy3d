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
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.PickingHint;

public class Util {
	
	public static double findBoundLength(final BoundingVolume bounds) {
		double d;
		if (bounds instanceof BoundingSphere)
			d = ((BoundingSphere)bounds).getRadius() * 2;
		else {
			final BoundingBox boundingBox = (BoundingBox)bounds;
			d = 2 * Math.max(boundingBox.getXExtent(), Math.max(boundingBox.getYExtent(), boundingBox.getZExtent()));
		}
		return d;
	}
	
	public static double findExactHeight(final ArrayList<HousePart> parts) {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (final HousePart part : parts) {
			for (int i = 0; i < part.getPoints().size(); i++) {
				final Vector3 p = part.getAbsPoint(i);
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
	    double angle = Math.atan2(b.dot(n.cross(a, null)), b.dot(a));
//	    if (angle < 0)
//	    	angle = Math.PI * 2.0 + angle;
		return angle;
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
	
	public static void disablePickShadowLight(final Spatial spatial) {
		spatial.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		spatial.getSceneHints().setCastsShadows(false);
		spatial.getSceneHints().setLightCombineMode(LightCombineMode.Off);		
	}
	
	public static Vector3 getClosetPoint(Vector3 A, Vector3 B, Vector3 P, boolean segmentClamp)
	{
	    Vector3 AP = P.subtract(A, null);
	    Vector3 AB = B.subtract(A, null);
//	    double ab2 = AB.getX()*AB.getX() + AB.getY()*AB.getY() + AB.getZ()*AB.getZ();
	    double ab2 = AB.dot(AB);
//	    double ap_ab = AP.dot(vec).getX()*AB.x + AP.y*AB.y;
	    double ap_ab = AP.dot(AB);
	    double t = ap_ab / ab2;
	    if (segmentClamp)
	    {
	         if (t < 0.0f) t = 0.0f;
	         else if (t > 1.0f) t = 1.0f;
	    }
	    Vector3 Closest = AB.scaleAdd(t, A, null);
	    return Closest;
	}	
}
