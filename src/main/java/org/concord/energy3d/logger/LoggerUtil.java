package org.concord.energy3d.logger;

import java.io.File;
import java.text.DecimalFormat;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.model.Building;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Tree;
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
		if (Config.isWebStart() || !MainApplication.appDirectoryWritable) {
			final String logPath;
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
			folder = new File(logPath);
		} else {
			folder = new File("log");
		}
		if (!folder.exists())
			folder.mkdir();
		System.out.println("Log folder: " + folder);
		return folder;
	}

	static long getBuildingId(final HousePart p) {
		if (p == null)
			return -1;
		if (p instanceof Foundation)
			return p.getId();
		final HousePart x = p.getTopContainer();
		return x == null ? -1 : x.getId();
	}

	static Foundation getBuildingFoundation(final HousePart p) {
		if (p == null)
			return null;
		if (p instanceof Foundation)
			return (Foundation) p;
		return p.getTopContainer();
	}

	static Object getInfo(final HousePart p) {
		if (p == null)
			return null;
		long bid = getBuildingId(p);
		String s;
		if (p instanceof Human) {
			s = "{\"Name\": \"" + ((Human) p).getHumanName() + "\", ";
		} else if (p instanceof Tree) {
			s = "{\"Species\": \"" + ((Tree) p).getTreeName() + "\", ";
		} else {
			s = "{\"Type\": \"" + p.getClass().getSimpleName() + "\", ";
		}
		if (bid != -1)
			s += "\"Building\": " + bid + ", ";
		s += "\"ID\": " + p.getId();
		int n = p.getPoints().size();
		if (n > 0) {
			s += ", \"Coordinates\": [";
			boolean exist;
			for (int i = 0; i < n; i++) {
				exist = false;
				for (int j = 0; j < i; j++) {
					if (p.getPoints().get(j).equals(p.getPoints().get(i))) {
						exist = true;
						break;
					}
				}
				if (!exist) {
					Vector3 v = p.getAbsPoint(i);
					s += "{\"x\": " + FORMAT.format(v.getX()) + ", \"y\": " + FORMAT.format(v.getY()) + ", \"z\": " + FORMAT.format(v.getZ()) + "}";
					if (i < n - 1)
						s += ", ";
				}
			}
			s = s.trim();
			if (s.endsWith(",")) {
				s = s.substring(0, s.length() - 1);
			}
			s += "]";
		}
		if (bid != -1 && bid == p.getId()) {
			if (p instanceof Foundation) {
				Building b = new Building((Foundation) p);
				if (b.isWallComplete()) {
					s += ", " + b.getGeometryJson();
				}
			}
		}
		s += "}";
		return s;
	}

}
