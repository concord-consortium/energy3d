package org.concord.energy3d.logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.Scene;

/**
 * @author Charles Xie
 * 
 */
public class SnapshotLogger {

	private SnapshotLogger() {
	}

	public static void start(final int period, final TimeSeriesLogger logger) {
		final Thread t = new Thread("Snapshots Logger") {
			public void run() {
				while (true) {
					try {
						sleep(1000 * period); // 20 seconds seem optimal
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
					if (Scene.getURL() != null) { // log only when student starts with a template
						if (logger.isEdited()) {
							try {
								saveSnapshot(LoggerUtil.getLogFolder());
							} catch (final Exception e) {
								e.printStackTrace();
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Error occured in logging! Please notify the teacher of this problem:\n" + e.getMessage(), "Logging Error", JOptionPane.ERROR_MESSAGE);
								break;
							}
							logger.resetEditFlags();
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
