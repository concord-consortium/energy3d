package org.concord.energy3d.util;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.MainFrame;

/**
 * This reminds the user who launches Energy3D from energy3d.jar that an update is available.
 * 
 * We must update both energy3d.jar and energy3d.zip at the same time. The same energy3d.jar must be used in the zip folder.
 * 
 * @author Charles Xie
 * 
 */
public final class UpdateAnnouncer {

	private final static String HOME = "http://energy.concord.org/energy3d/";

	public static void showMessage() {
		String s = getJarLocation();
		if (!s.endsWith("energy3d.jar"))
			return;
		if (new File(s).lastModified() >= checkTimeStamp(HOME + "energy3d.jar"))
			return;
		String msg = "An update (V" + Config.VERSION + ") is available. Do you want to download it now?";
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), msg, "Update Energy3D", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			Util.openBrowser(HOME + "update.html");
			System.exit(0);
		}
	}

	private static long checkTimeStamp(String s) {
		URLConnection connection = null;
		try {
			connection = new URL(s).openConnection();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(5000);
		if (!(connection instanceof HttpURLConnection))
			return -1;
		HttpURLConnection c = (HttpURLConnection) connection;
		try {
			c.setRequestMethod("HEAD");
		} catch (ProtocolException e) {
			e.printStackTrace();
			return -1;
		}
		long t = c.getLastModified();
		c.disconnect();
		return t;
	}

	private static String getJarLocation() {
		String jarLocation = System.getProperty("java.class.path");
		if (System.getProperty("os.name").startsWith("Mac"))
			jarLocation = validateJarLocationOnMacOSX(jarLocation);
		return jarLocation;
	}

	/*
	 * Mac OS X's Java 1.5.0_06 implementation returns things like : /Users/user/energy3d.jar:/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/.compatibility/14compatibility.jar
	 */
	private static String validateJarLocationOnMacOSX(String jarLocation) {
		int i = jarLocation.indexOf(".jar:/");
		if (i != -1)
			jarLocation = jarLocation.substring(0, i) + ".jar";
		return jarLocation;
	}

}