package org.concord.energy3d.util;

import org.concord.energy3d.gui.MainApplet;

public class Config {
	private static MainApplet applet;

	public static MainApplet getApplet() {
		return applet;
	}

	public static void setApplet(MainApplet applet) {
		Config.applet = applet;
	}

	public static boolean isApplet() {
		return Config.applet != null;
	}

	public static boolean isHeliodonMode() {
		if (!isApplet())
			return false;
		else
			return "true".equalsIgnoreCase(Config.getApplet().getParameter("heliodon"));
	}
}
