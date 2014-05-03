package org.concord.energy3d.logger;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.concord.energy3d.model.Building;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.util.Config;

import com.ardor3d.math.Vector3;

/**
 * @author Charles Xie
 * 
 */
class LoggerUtil {

	final static DecimalFormat FORMAT = new DecimalFormat("0.###");

	private static File folder = null;

	static File getLogFolder() {
		if (folder != null)
			return folder;
		final String logPath;
		if (Config.isWebStart()) {
			final String rootPathRelative = File.separator + "Energy3D";
			final String rootPathAbsolute;
			if (Config.isWindows()) {
				final float winVersion = Float.parseFloat(System.getProperty("os.version"));
				System.out.println("Windows " + winVersion);
				if (winVersion < 6)
					rootPathAbsolute = System.getProperty("user.home") + "\\Local Settings\\Application Data" + rootPathRelative;
				else
					rootPathAbsolute = System.getenv("LOCALAPPDATA") + rootPathRelative;
			} else if (Config.isMac())
				rootPathAbsolute = System.getProperty("user.home") + "/Library/Logs/Energy3D";
			else
				rootPathAbsolute = System.getProperty("user.home") + rootPathRelative;
			if (Config.isMac())
				logPath = rootPathAbsolute;
			else {
				final File dir = new File(rootPathAbsolute);
				if (!dir.exists())
					dir.mkdir();
				logPath = rootPathAbsolute + File.separator + "log";
			}
		} else
			logPath = "log";
		final File folder = new File(logPath);
		System.out.println("Log folder: " + folder.toString());
		if (!folder.exists())
			folder.mkdir();
		return folder;
	}

	static Object getBuildingId(final HousePart p) {
		if (p == null)
			return null;
		final HousePart x = p.getTopContainer();
		return x == null ? null : x.getId();
	}

	static Object getInfo(final HousePart p) {
		if (p == null)
			return null;
		Object bid = getBuildingId(p);
		String s = "{\"Building\": " + bid + ", \"ID\": " + p.getId();
		int n = p.getPoints().size();
		if (n > 0) {
			s += ", \"Coordinates\": [";
			for (int i = 0; i < n; i++) {
				Vector3 v = p.getAbsPoint(i);
				s += "{\"x\": " + FORMAT.format(v.getX()) + ", \"y\": " + FORMAT.format(v.getY()) + ", \"z\": " + FORMAT.format(v.getZ()) + "}";
				if (i < n - 1)
					s += ", ";
			}
			s += "]";
		}
		if (bid != null && ((Long) bid).longValue() == p.getId()) {
			if (p instanceof Foundation) {
				Building b = new Building((int) p.getId());
				ArrayList<HousePart> children = p.getChildren();
				for (HousePart x : children) {
					if (x instanceof Wall)
						b.addWall((Wall) x);
				}
				if (b.isWallComplete()) {
					s += ", " + b.getGeometryJson();
				}
			}
		}
		s += "}";
		return s;
	}
}
