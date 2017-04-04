package org.concord.energy3d.util;

import java.awt.EventQueue;
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
			new Thread("Energy3D Update Downloader") {
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
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(MainFrame.getInstance(), "A new update is available. Would you like to install updates and restart now?", "Update", JOptionPane.YES_NO_OPTION)) {
										restartRequested = true;
										MainFrame.getInstance().exit();
									} else {
										firstTime = false;
									}
								}
							});
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
