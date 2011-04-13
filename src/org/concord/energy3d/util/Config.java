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
}
