package org.concord.energy3d;

import java.awt.EventQueue;
import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.concord.energy3d.etc.oneinstance.OneInstance;
import org.concord.energy3d.etc.oneinstance.OneInstanceListener;
import org.concord.energy3d.gui.Mac;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.logger.SnapshotLogger;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.UpdateStub;

import com.threerings.getdown.launcher.GetdownApp;

public class MainApplication {

	public static final String VERSION = "5.4.4";

	public static boolean appDirectoryWritable = true;
	public static boolean isMacOpeningFile;
	private static ArrayList<Runnable> shutdownHooks;

	public static void main(final String[] args) {
		System.out.println("Initiating...");
		checkSingleInstance(MainApplication.class, args);

		final File testFile = new File(System.getProperty("user.dir"), "test.txt");
		// can't use File.canWrite() to check if we can write a file to this folder. So we have to walk extra miles as follows.
		try {
			testFile.createNewFile();
			testFile.delete();
		} catch (final Throwable e) {
			appDirectoryWritable = false;
		}

		final String version = System.getProperty("java.version");
		if (version.compareTo("1.6") < 0) {
			JOptionPane.showMessageDialog(null, "Your current Java version is " + version + ". Version 1.6 or higher is required.");
			System.exit(0);
		}

		System.setProperty("jogl.gljpanel.noglsl", "true");
		System.setProperty("direct", "true");

		if (System.getProperty("os.name").startsWith("Mac")) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Energy3D");
		}
		Config.setWebStart(System.getProperty("javawebstart.version", null) != null);
		if (!Config.isWebStart())
			System.setProperty("jogamp.gluegen.UseTempJarCache", "false");
		final boolean isJarOrEclipse = !Config.isWebStart() && !System.getProperty("java.library.path").contains("jogl");
		if (isJarOrEclipse)
			setupLibraryPath();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e) {
			e.printStackTrace();
		}
		final SceneManager sceneManager = SceneManager.getInstance();
		final MainFrame mainFrame = MainFrame.getInstance();
		mainFrame.updateTitleBar();
		mainFrame.setVisible(true);
		Scene.getInstance();
		new Thread(sceneManager, "Energy3D Application").start();

		/* initialize data logging */
		addShutdownHook(new Runnable() {
			@Override
			public void run() {
				TimeSeriesLogger.getInstance().close();
			}
		});
		TimeSeriesLogger.getInstance().start();
		SnapshotLogger.getInstance().start(20);

		try {
			new Thread() {
				@Override
				public void run() {
					try {
						if (isMacOpeningFile)
							return;
						if (Config.isWebStart()) {
							if (args.length > 1 && !args[args.length - 1].startsWith("-"))
								mainFrame.open(args[args.length - 1]);
							else
								Scene.newFile();
						} else {
							if (args.length > 0)
								mainFrame.open(args[0]);
							else
								Scene.newFile();
						}
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			}.start();

			System.out.println("Initiatialization phase 2 done.");
		} catch (final Throwable e) {
			e.printStackTrace();
		}
	}

	public static void addShutdownHook(final Runnable r) {
		if (shutdownHooks == null)
			shutdownHooks = new ArrayList<Runnable>();
		if (!shutdownHooks.contains(r))
			shutdownHooks.add(r);
	}

	public static void exit() {
		if (shutdownHooks != null) { // e.g., save the log file before exit to ensure that the last segment is saved
			for (final Runnable r : shutdownHooks)
				r.run();
		}
		System.out.println("exit.");
		try {
			System.out.println(new File(".").getCanonicalPath());
		} catch (final IOException e) {
			e.printStackTrace();
		}
		if (Config.isWebStart() || Config.isEclipse())
			System.exit(0);
		else {
			if (appDirectoryWritable) {
				MainFrame.getInstance().setVisible(false);
				new Thread() {
					@Override
					public void run() {
						GetdownApp.main(new String[] { "." });
						while (!UpdateStub.receivedCall)
							Thread.yield();
						UpdateStub.receivedCall = false;
						System.exit(0);
					};
				}.start();
			} else {
				System.exit(0);
			}
		}
	}

	public static void setupLibraryPath() {
		System.out.println(System.getProperty("java.version") + ", " + System.getProperty("os.arch"));
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

		final String newLibraryPath = "./lib/jogl/native/" + OSPath;
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

	private static void checkSingleInstance(final Class<?> mainClass, final String[] args) {
		System.out.println("Checking single instance...");
		final OneInstance oneInstance = OneInstance.getInstance();

		// Install listener which processes the start of secondary instances
		oneInstance.addListener(new OneInstanceListener() {
			@Override
			public boolean newInstanceCreated(final File workingDir, final String[] args) {
				System.out.println("SingleInstance.open()");
				newActivation(args);
				return false;
			}
		});

		if (!oneInstance.register(mainClass, args)) {
			System.out.println("Already running...exit");
			System.exit(0);
		}
	}

	public static void newActivation(final String[] args) {
		System.out.println("newActivation()");

		for (final String s : args)
			System.out.println(s);

		if (args.length > 0)
			try {
				MainFrame.getInstance().open(args[0]);
				MainFrame.getInstance().updateTitleBar();
			} catch (final Exception e) {
				e.printStackTrace();
			}

		showAndBringToFront();
	}

	public static void showAndBringToFront() {
		System.out.println("showAndBringToFront");
		if (!MainFrame.getInstance().isVisible())
			MainFrame.getInstance().setVisible(true);
		if (MainFrame.getInstance().getState() == Frame.ICONIFIED)
			MainFrame.getInstance().setState(Frame.NORMAL);

		else if (Config.isMac())
			Mac.bringToFront();
		else
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					MainFrame.getInstance().toFront();
				}
			});
	}

}
