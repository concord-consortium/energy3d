package org.concord.energy3d;

import java.awt.EventQueue;
import java.awt.Frame;
import java.io.File;
import java.io.FileWriter;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;

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
import org.concord.energy3d.util.Updater;
import org.concord.energy3d.util.Util;

public class MainApplication {
	public static final String VERSION = "7.5.2";
	private static Thread sceneManagerThread;
	public static boolean appDirectoryWritable = true;
	public static boolean isMacOpeningFile;
	private static volatile boolean initializing;
	private static ArrayList<Runnable> shutdownHooks;

	public static void main(final String[] args) {
		System.out.println("Initiating...");
		final long t = System.nanoTime();
		checkSingleInstance(MainApplication.class, args);
		startDeadlockDetectionThread();

		final File testFile = new File(System.getProperty("user.dir"), "test.txt");
		// can't use File.canWrite() to check if we can write a file to this folder. So we have to walk extra miles as follows.
		try {
			testFile.createNewFile();
			testFile.delete();
		} catch (final Throwable e) {
			appDirectoryWritable = false;
		}

		System.setProperty("jogl.gljpanel.noglsl", "true");

		if (System.getProperty("os.name").startsWith("Mac")) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Energy3D");
		}
		Config.setWebStart(System.getProperty("javawebstart.version", null) != null);
		if (!Config.isWebStart()) {
			System.setProperty("jogamp.gluegen.UseTempJarCache", "false");
		}
		final boolean isJarOrEclipse = !Config.isWebStart() && !System.getProperty("java.library.path").contains("jogl");
		if (isJarOrEclipse) {
			setupLibraryPath();
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e) {
			e.printStackTrace();
		}

		initializing = true;
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final SceneManager sceneManager = SceneManager.getInstance(); // this calls Swing GUI
				Scene.getInstance(); // this calls Swing GUI
				sceneManagerThread = new Thread(sceneManager, "Energy3D Main Application");
				sceneManagerThread.start();

				final MainFrame mainFrame = MainFrame.getInstance();
				mainFrame.updateTitleBar();
				mainFrame.setVisible(true);
				new Thread("Energy3D Open File") {
					@Override
					public void run() {
						try {
							if (Config.isMac()) {
								Thread.sleep(200);
							}
							if (isMacOpeningFile) {
								return;
							}
							if (Config.isWebStart()) {
								if (args.length > 1 && !args[args.length - 1].startsWith("-")) {
									mainFrame.open(args[args.length - 1]);
								} else {
									Scene.newFile();
								}
							} else {
								if (args.length > 0) {
									mainFrame.open(args[0]);
								} else {
									Scene.newFile();
								}
							}
						} catch (final Exception e) {
							e.printStackTrace();
						} finally {
							initializing = false;
						}
					}
				}.start();

				Updater.download();

			}
		});

		/* initialize data logging */
		addShutdownHook(new Runnable() {
			@Override
			public void run() {
				TimeSeriesLogger.getInstance().close();
			}
		});
		TimeSeriesLogger.getInstance().start();
		SnapshotLogger.getInstance().start(20);

		System.out.println("Initiatialization phase 2 done.");
		System.out.println("Time = " + (System.nanoTime() - t) / 1000000000.0);

	}

	public static void addShutdownHook(final Runnable r) {
		if (shutdownHooks == null) {
			shutdownHooks = new ArrayList<Runnable>();
		}
		if (!shutdownHooks.contains(r)) {
			shutdownHooks.add(r);
		}
	}

	public static void exit() {
		if (Scene.isSaving()) {
			return;
		}
		if (shutdownHooks != null) { // e.g., save the log file before exit to ensure that the last segment is saved
			for (final Runnable r : shutdownHooks) {
				r.run();
			}
		}
		if (!Config.isWebStart()) {
			Updater.install();
			if (Updater.isRestartRequested()) {
				restartApplication();
			}
		}
		System.out.println("exit.");
		System.exit(0);
	}

	public static void restartApplication() {
		try {
			System.out.println("Restarting...");
			final String userDir = System.getProperty("user.dir");
			if (Config.isWindows()) {
				final String exeFile = userDir + File.separator + ".." + File.separator + "Energy3D.exe";
				if (new File(exeFile).exists()) {
					System.out.println(exeFile);
					Runtime.getRuntime().exec(exeFile);
					return;
				}
			} else if (Config.isMac()) {
				final int indexOfApp = userDir.indexOf(".app");
				if (indexOfApp != -1) {
					final String appFile = userDir.substring(0, indexOfApp + 4);
					if (new File(appFile).exists()) {
						final File scriptFile = File.createTempFile("gc3_tmp_", ".sh");
						final FileWriter writer = new FileWriter(scriptFile);
						writer.write("sleep 1\n");
						writer.write("open " + appFile + "\n");
						writer.flush();
						writer.close();
						System.out.println("open " + appFile);
						System.out.println("sh " + scriptFile.getAbsolutePath());
						Runtime.getRuntime().exec("sh " + scriptFile.getAbsolutePath());
						return;
					}
				}
			}

			final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
			final File currentJar = new File(MainApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI());

			/* is it a jar file? */
			if (!currentJar.getName().endsWith(".jar")) {
				return;
			}

			/* Build command: java -jar application.jar */
			final ArrayList<String> command = new ArrayList<String>();
			command.add(javaBin);
			command.add("-jar");
			command.add(currentJar.getPath());

			System.out.println(javaBin + " -jar " + currentJar.getPath());

			final ProcessBuilder builder = new ProcessBuilder(command);
			builder.start();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static void setupLibraryPath() {
		System.out.println(System.getProperty("java.version") + ", " + System.getProperty("os.arch"));
		final String OSPath;
		final String os = System.getProperty("os.name").toLowerCase();
		if (os.startsWith("windows")) {
			final String sunArch = System.getProperty("sun.arch.data.model");
			if (sunArch != null && sunArch.startsWith("64")) {
				OSPath = "windows-64";
			} else {
				OSPath = "windows-32";
			}
		} else if (os.startsWith("mac")) {
			OSPath = "mac-universal";
		} else if (os.startsWith("linux")) {
			final String sunArch = System.getProperty("sun.arch.data.model");
			if (sunArch != null && sunArch.startsWith("64")) {
				OSPath = "linux-64";
			} else {
				OSPath = "linux-32";
			}
		} else {
			throw new RuntimeException("Unknown OS: " + os);
		}

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
				if (!initializing) { // prevent user to invoke newActivation while the app is being initialized (e.g., user impatiently clicks the app multiple times)
					newActivation(args);
				}
				return false;
			}
		});

		if (!oneInstance.register(mainClass, args)) {
			System.out.println("Already running...exit");
			System.exit(0);
		}
	}

	private static void newActivation(final String[] args) {
		System.out.println("newActivation(): " + Arrays.asList(args));
		if (args.length > 0) {
			try {
				MainFrame.getInstance().open(args[0]);
				MainFrame.getInstance().updateTitleBar();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		showAndBringToFront();
	}

	private static void showAndBringToFront() {
		System.out.println("showAndBringToFront");
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (!MainFrame.getInstance().isVisible()) {
					MainFrame.getInstance().setVisible(true);
				}
				if (MainFrame.getInstance().getState() == Frame.ICONIFIED) {
					MainFrame.getInstance().setState(Frame.NORMAL);
				} else if (Config.isMac()) {
					Mac.bringToFront();
				} else {
					MainFrame.getInstance().toFront();
				}
			}
		});
	}

	public static void startDeadlockDetectionThread() {
		new Thread("Energy3D Deadlock Detection") {
			@Override
			public void run() {
				try {
					sleep(10000);
				} catch (final InterruptedException e) {
				}
				while (true) {
					final boolean[] isInvoked = { false };
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							isInvoked[0] = true;
						}
					});

					try {
						sleep(10000);
					} catch (final InterruptedException e) {
					}

					if (!isInvoked[0]) {
						final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
						final ThreadInfo[] infos = threadBean.dumpAllThreads(true, true);
						String msg = "Number of threads: " + infos.length + "\n";
						for (final ThreadInfo ti : infos) {
							msg += getThreadInfoString(ti) + "\n";
						}
						try {
							System.err.println(msg);
							Util.sendError(msg, null);
						} catch (final Exception e) {
							e.printStackTrace();
						}
						break;
					}
				}
			}
		}.start();
	}

	public static String getThreadInfoString(final ThreadInfo infos) {
		final StringBuilder sb = new StringBuilder("\"" + infos.getThreadName() + "\"" + " Id=" + infos.getThreadId() + " " + infos.getThreadState());
		if (infos.getLockName() != null) {
			sb.append(" on " + infos.getLockName());
		}
		if (infos.getLockOwnerName() != null) {
			sb.append(" owned by \"" + infos.getLockOwnerName() + "\" Id=" + infos.getLockOwnerId());
		}
		if (infos.isSuspended()) {
			sb.append(" (suspended)");
		}
		if (infos.isInNative()) {
			sb.append(" (in native)");
		}
		sb.append('\n');
		int i = 0;
		final int MAX_LINES = 100;
		for (; i < infos.getStackTrace().length && i < MAX_LINES; i++) {
			final StackTraceElement ste = infos.getStackTrace()[i];
			sb.append("\tat " + ste.toString());
			sb.append('\n');
			if (i == 0 && infos.getLockInfo() != null) {
				final Thread.State ts = infos.getThreadState();
				switch (ts) {
				case BLOCKED:
					sb.append("\t-  blocked on " + infos.getLockInfo());
					sb.append('\n');
					break;
				case WAITING:
					sb.append("\t-  waiting on " + infos.getLockInfo());
					sb.append('\n');
					break;
				case TIMED_WAITING:
					sb.append("\t-  waiting on " + infos.getLockInfo());
					sb.append('\n');
					break;
				default:
				}
			}

			for (final MonitorInfo mi : infos.getLockedMonitors()) {
				if (mi.getLockedStackDepth() == i) {
					sb.append("\t-  locked " + mi);
					sb.append('\n');
				}
			}
		}
		if (i < infos.getStackTrace().length) {
			sb.append("\t...");
			sb.append('\n');
		}

		final LockInfo[] locks = infos.getLockedSynchronizers();
		if (locks.length > 0) {
			sb.append("\n\tNumber of locked synchronizers = " + locks.length);
			sb.append('\n');
			for (final LockInfo li : locks) {
				sb.append("\t- " + li);
				sb.append('\n');
			}
		}
		sb.append('\n');
		return sb.toString();
	}

	public static boolean isSceneManagerThread() {
		return Thread.currentThread() == sceneManagerThread;
	}

	public static boolean isSceneManagerThreadAlive() {
		return sceneManagerThread.isAlive();
	}

}
