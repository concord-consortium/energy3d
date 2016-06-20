package org.concord.energy3d.util;

import java.io.IOException;

import org.concord.energy3d.MainApplication;

import com.threerings.getdown.launcher.Getdown;
import com.threerings.getdown.launcher.GetdownApp;

public class Updater {

	public static void download() {
		System.out.println("Updater.download()");
		if (!Config.isWebStart() && !Config.isEclipse() && MainApplication.appDirectoryWritable)
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
					if (Config.isEclipse())
						System.setProperty("no_log_redir", "true");
					GetdownApp.main(new String[] { "." });
				};
			}.start();
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
