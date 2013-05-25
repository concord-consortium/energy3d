package org.concord.energy3d.logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.concord.energy3d.scene.SceneManager;

public class TimeSeriesLogger {

	private int period = 1; // in seconds
	private File dir;
	private SceneManager sceneManager;

	public TimeSeriesLogger(int period, File dir, SceneManager sceneManager) {
		this.period = period;
		this.dir = dir;
		this.sceneManager = sceneManager;
	}

	private void log() {
		String timestamp = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss  ").format(Calendar.getInstance().getTime());
		String undoAction = sceneManager.getUndoManager().getUndoPresentationName();
		if (undoAction.startsWith("Undo")) {
			undoAction = undoAction.substring(4).trim();
			if (undoAction.equals(""))
				undoAction = "None";
		}
		System.out.println(timestamp + ": " + undoAction);
	}

	public void start() {
		new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						sleep(1000 * period);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
					log();
				}
			}
		}.start();
	}

}
