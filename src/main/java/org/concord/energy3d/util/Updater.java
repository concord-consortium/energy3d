package org.concord.energy3d.util;

import java.io.IOException;

import com.threerings.getdown.launcher.Getdown;
import com.threerings.getdown.launcher.GetdownApp;

public class Updater {

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
					// EventQueue.invokeLater(new Runnable() {
					// @Override
					// public void run() {
					// JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html><h2>A new update is available.</h2>You may restart the program now or later to get it.</html>", "Update Notice", JOptionPane.INFORMATION_MESSAGE);
					// }
					// });
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

}
