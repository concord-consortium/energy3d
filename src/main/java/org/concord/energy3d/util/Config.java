package org.concord.energy3d.util;

import org.concord.energy3d.MainApplet;

public class Config {
	public static final String VERSION = "2.0";
	public static RenderMode RENDER_MODE = RenderMode.JOGL;
	private static MainApplet applet;
	private static boolean isWebstart;
	private static final boolean isMac = System.getProperty("os.name").toLowerCase().startsWith("mac");
//	private static long startTime;

	public static enum RenderMode {NEWT, JOGL, LWJGL};

//	static {
//		final Object renderer = JOptionPane.showInputDialog(null, "Which renderer?", null, JOptionPane.QUESTION_MESSAGE, null, new RenderMode[] {RenderMode.JOGL,  RenderMode.LWJGL, RenderMode.NEWT}, RenderMode.JOGL);
//		if (renderer != null)
//			RENDER_MODE = (RenderMode) renderer;
//		startTime = System.nanoTime();
//	}

//	public static void printTimeUntilFirstRender() {
//		if (startTime != 0) {
//			System.out.println("Time until first render = " + (float)((System.nanoTime() - startTime) / 1000000000.0) + "s");
//			startTime = 0;
//		}
//	}

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
		else
			return "true".equalsIgnoreCase(Config.getApplet().getParameter("heliodon"));
	}

	public static void setWebStart(final boolean webstart) {
		isWebstart = webstart;
	}

	public static boolean isWebStart() {
		return isWebstart;
	}

	public static boolean isMac() {
		return isMac;
	}

	// XIE: the following methods support logging and analysis

	public static boolean isClassroomMode() {
		return false;
	}

	public static boolean isAssessmentMode() {
		return false;
	}

	public static boolean replaying = true;
	public static boolean backward, forward;

}