package org.concord.energy3d.logger;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.BugReporter;

/**
 * @author Charles Xie
 * 
 */
public class SnapshotLogger {

	// must maintain an independent set of "edited" flags for logging snapshots
	private volatile boolean sceneEdited = false;
	private volatile boolean noteEdited = false;

	private static SnapshotLogger instance = new SnapshotLogger();

	private SnapshotLogger() {
	}

	public static SnapshotLogger getInstance() {
		return instance;
	}

	public void setNoteEdited(final boolean b) {
		noteEdited = b;
	}

	public void setSceneEdited(final boolean b) {
		sceneEdited = b;
	}

	private void reset() {
		sceneEdited = false;
		noteEdited = false;
	}

	public static File getLogFolder() {
		return LoggerUtil.getLogFolder();
	}

	public void start(final int period) {
		final Thread t = new Thread("Energy3D Snapshot Logger") {
			@Override
			public void run() {
				while (true) {
					try {
						sleep(1000 * period); // 20 seconds seem optimal
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
					if (noteEdited || sceneEdited) {
						try {
							saveSnapshot();
						} catch (final Exception e) {
							e.printStackTrace();
							BugReporter.report(e);
							break;
						}
						reset();
					}
				}
			}
		};
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	private void saveSnapshot() throws Exception {
		final Date date = Calendar.getInstance().getTime();
		final String filename = LoggerUtil.getLogFolder() + File.separator + new SimpleDateFormat("yyyy-MM-dd  HH-mm-ss").format(date) + ".ng3";
		Scene.save(new File(filename).toURI().toURL(), false, false, true);
	}

	public File saveSnapshot(final String name) throws Exception {
		final File file = File.createTempFile(name, ".ng3");
		Scene.saveOutsideTaskManager(file.toURI().toURL());
		return file;
	}

	public File getLatestSnapshot() {
		final File[] files = getLogFolder().listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				return name.endsWith(".ng3");
			}
		});
		final int n = files.length;
		if (n > 0) {
			Arrays.sort(files);
			for (int i = n - 1; i >= 0; i--) {
				if (files[i].length() > 0) {
					return files[i];
				}
			}
		}
		return null;
	}

}
