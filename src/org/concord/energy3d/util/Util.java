package org.concord.energy3d.util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.bounding.BoundingVolume;

public class Util {
	public static final boolean DEBUG = true;
	
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
}
