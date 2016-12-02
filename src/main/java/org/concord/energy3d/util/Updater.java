package org.concord.energy3d.util;

import java.io.IOException;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.MainFrame;

import com.threerings.getdown.launcher.Getdown;
import com.threerings.getdown.launcher.GetdownApp;

public class Updater {
	private static boolean firstTime = true;
	private static boolean restartRequested = false;

	public static void download() {		
		System.out.println("Updater.download()");
		if (!Config.isWebStart() && !Config.isEclipse()) {
			new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
					System.setProperty("direct", "true");
					System.setProperty("no_install", "true");
					System.setProperty("silent", "true");
					GetdownApp.main(new String[] { "." });
					for (int i = 0; i < 60; i++) {
						if (!firstTime) {
							break;
						}
						try {
							Thread.sleep(1000);
						} catch (final InterruptedException e) {
							e.printStackTrace();
						}
						if (Getdown.isUpdateAvailable()) {
							if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(MainFrame.getInstance(), "<html>A new update is available. Please exit the program and then restart it.<br>Is it good time to do it now?</html>", "Update", JOptionPane.YES_NO_OPTION)) {
								restartRequested = true;
								MainFrame.getInstance().exit();
							} else {
								firstTime = false;
							}
						}
					}
				};
			}.start();
		}
	}

	public static void install() {
		System.out.println("Updater.install()");
		try {
			Getdown.install();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static boolean isRestartRequested() {
		return restartRequested;
	}
}
