package org.concord.energy3d.util;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.concord.energy3d.model.HousePart;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;

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
	
//	public static double findExactHeight(final Spatial obj) {
//		final Vector2 maxMin = new Vector2(Double.MAX_VALUE, Double.MIN_VALUE);
//		findExactHeight(obj, maxMin);
//		return maxMin.getX() - maxMin.getY();
//	}
	
	
	
//	private static void findExactHeight(Spatial obj, Vector2 maxMin) {
//		if (obj instanceof Node) {
//			for (Spatial child : ((Node)obj).getChildren())
//				findExactHeight(child, maxMin);
//		} else if (obj instanceof Mesh) {
//			System.out.println(obj);
//			final FloatBuffer vertexBuffer = ((Mesh)obj).getMeshData().getVertexBuffer();
//			while (vertexBuffer.hasRemaining()) {
//				vertexBuffer.get();
//				if (!vertexBuffer.hasRemaining())
//					break;
//				vertexBuffer.get();
//				if (!vertexBuffer.hasRemaining())
//					break;				
//				final double z = vertexBuffer.get();
//				if (z > maxMin.getX())
//					maxMin.setX(z);
//				if (z < maxMin.getY())
//					maxMin.setY(z);
//			}
//		}
//	}
	
	public static double findExactHeight(final ArrayList<HousePart> parts) {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (final HousePart part : parts) {
			for (final Vector3 p : part.getPoints()) {
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
