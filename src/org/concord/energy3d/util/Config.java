package org.concord.energy3d.util;

import org.concord.energy3d.gui.MainApplet;

public class Config {
	public static final String VERSION = "0.5.4";
	private static MainApplet applet;
	private static boolean isWebstart;

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

	public static void setWebStart(final boolean webstart) {
		isWebstart = webstart;
	}

	public static boolean isWebStart() {
		return isWebstart;
	}
}