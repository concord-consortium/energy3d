package org.concord.energy3d;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.Config.RenderMode;

public class MainApplication {
	public static void main(final String[] args) {
		if (System.getProperty("os.name").startsWith("Mac")) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Energy3D");
		}
		Config.setWebStart(System.getProperty("javawebstart.version", null) != null);
		if (Config.isWebStart())
			System.out.println("Application is lauched by webstart.");
		else
			setupLibraryPath();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e) {
			e.printStackTrace();
		}
		final SceneManager scene = SceneManager.getInstance();
		final MainFrame mainFrame = MainFrame.getInstance();
		mainFrame.updateTitleBar();
		mainFrame.setVisible(true);
		Scene.getInstance();
		new Thread(scene, "Energy 3D Application").start();

		if (!Config.isMac() && args.length > 1 && !args[args.length - 1].startsWith("-"))
			mainFrame.open(args[args.length - 1]);

		if (Config.isClassroomMode()) {
			final File dir = new File("log");
			if (!dir.exists())
				dir.mkdir();
			logSnapshots(20, dir);
			new TimeSeriesLogger(1, dir, scene).start();
		}
	}

	private static boolean argsContain(final String command, final String[] args) {
		for (final String arg : args)
			if (arg.startsWith(command))
				return true;
		return false;
	}

	public static void setupLibraryPath() {
		System.out.println(System.getProperty("java.version") + ", " + System.getProperty("os.arch"));
		final String orgLibraryPath = System.getProperty("java.library.path");
		final String sep = System.getProperty("file.separator");
		final String rendererNativePath = "." + sep + "lib" + sep + (Config.RENDER_MODE == RenderMode.LWJGL ? "lwjgl" : "jogl") + sep + "native";
		final String OSPath;
		final String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("windows")) {
			final String sunArch = System.getProperty("sun.arch.data.model");
			if (sunArch != null && sunArch.startsWith("64"))
				OSPath = "windows-64";
			else
				OSPath = "windows-32";
		} else if (os.startsWith("mac")) {
			OSPath = "mac-universal";
		} else if (os.startsWith("linux")) {
			final String sunArch = System.getProperty("sun.arch.data.model");
			if (sunArch != null && sunArch.startsWith("64"))
				OSPath = "linux-64";
			else
				OSPath = "linux-32";
		} else
			throw new RuntimeException("Unknown OS: " + os);

		final String pathSep = System.getProperty("path.separator");
		final String newLibraryPath = "." + pathSep + rendererNativePath + sep + OSPath + pathSep + orgLibraryPath;
		System.setProperty("java.library.path", newLibraryPath);
		System.out.println("Path = " + System.getProperty("java.library.path"));
		// The following code is to empty the library path cache in order to force JVM to use the new library path above
		java.lang.reflect.Field fieldSysPath;
		try {
			fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static void logSnapshots(final int period, final File dir) {
		new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						sleep(1000 * period); // 20 seconds seem to be optimal
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
					if (Scene.getInstance().isEdited()) {
						final Date date = Calendar.getInstance().getTime();
						final String filename = dir + File.separator + new SimpleDateFormat("yyyy-MM-dd   HH-mm-ss   ").format(date) + SceneManager.getInstance().getUndoManager().getEditCounts() + ".ng3";
						try {
							Scene.save(new File(filename).toURI().toURL(), false);
						} catch (final Exception e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "Error occured in Auto-Save! Models will no longer be saved automatically. Please notify the teacher of this problem:\n" + e.getMessage(), "Auto-Save Error", JOptionPane.ERROR_MESSAGE);
							break;
						}
					}
				}
			}
		}.start();
	}

}
