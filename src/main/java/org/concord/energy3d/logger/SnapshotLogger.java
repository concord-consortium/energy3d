package org.concord.energy3d.logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.Util;

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

	public void setNoteEdited(boolean b) {
		noteEdited = b;
	}

	public void setSceneEdited(boolean b) {
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
		final Thread t = new Thread("Snapshots Logger") {
			public void run() {
				while (true) {
					try {
						sleep(1000 * period); // 20 seconds seem optimal
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
					if (Scene.getURL() != null) { // log only when student starts with a template
						if (noteEdited || sceneEdited) {
							try {
								saveSnapshot(LoggerUtil.getLogFolder());
							} catch (final Exception e) {
								e.printStackTrace();
								Util.reportError(e);
								break;
							}
							reset();
						}
					}
				}
			}
		};
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

	private static void saveSnapshot(final File dir) throws Exception {
		final Date date = Calendar.getInstance().getTime();
		final String filename = dir + File.separator + new SimpleDateFormat("yyyy-MM-dd  HH-mm-ss").format(date) + ".ng3";
		Scene.save(new File(filename).toURI().toURL(), false, false);
	}

}
