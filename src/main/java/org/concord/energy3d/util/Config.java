package org.concord.energy3d.util;

import org.concord.energy3d.MainApplet;

public class Config {

	public static final RenderMode RENDER_MODE = RenderMode.JOGL;
	private static MainApplet applet;
	private static boolean isWebstart;
	private static final boolean isMac = System.getProperty("os.name").toLowerCase().startsWith("mac");
	private static final boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("win");

	public static enum RenderMode {
		NEWT, JOGL, LWJGL
	};

	public static MainApplet getApplet() {
		return applet;
	}

	public static void setApplet(final MainApplet applet) {
		Config.applet = applet;
	}

	public static boolean isApplet() {
		return Config.applet != null;
	}

	public static boolean isHeliodonMode() {
		if (!isApplet())
			return false;
		return "true".equalsIgnoreCase(Config.getApplet().getParameter("heliodon"));
	}

	public static void setWebStart(final boolean webstart) {
		isWebstart = webstart;
	}

	public static boolean isWebStart() {
		return isWebstart;
	}

	public static boolean isEclipse() {
		return "true".equalsIgnoreCase(System.getProperty("runInEclipse"));
	}

	public static boolean isMac() {
		return isMac;
	}

	public static boolean isWindows() {
		return isWindows;
	}

}