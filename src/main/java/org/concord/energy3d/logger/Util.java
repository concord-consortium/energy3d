package org.concord.energy3d.logger;

import org.concord.energy3d.model.HousePart;

/**
 * @author Charles
 * 
 */
class Util {

	static String getBuildingId(final HousePart p) {
		if (p == null)
			return null;
		final HousePart x = getTopContainer(p);
		if (x == null)
			return null;
		return "Building #" + x.getId();
	}

	private static HousePart getTopContainer(final HousePart p) {
		if (p == null)
			return null;
		HousePart c = p.getContainer();
		if (c == null)
			return p;
		HousePart x = null;
		while (c != null) {
			x = c;
			c = c.getContainer();
		}
		return x;
	}

	static String getId(final HousePart p) {
		if (p == null)
			return null;
		return p.getClass().getSimpleName() + " #" + p.getId();
	}

}
