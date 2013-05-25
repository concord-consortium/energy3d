package org.concord.energy3d.logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.undo.UndoableEdit;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.undo.AddHousePartCommand;
import org.concord.energy3d.undo.EditHousePartCommand;
import org.concord.energy3d.undo.UndoManager;

public class TimeSeriesLogger {

	private int period = 1; // in seconds
	private File dir;
	private SceneManager sceneManager;
	private UndoableEdit lastEdit;
	private UndoManager undoManager;
	private HousePart actedHousePart;

	public TimeSeriesLogger(int period, File dir, SceneManager sceneManager) {
		this.period = period;
		this.dir = dir;
		this.sceneManager = sceneManager;
		undoManager = sceneManager.getUndoManager();
		lastEdit = undoManager.lastEdit();
	}

	private void log() {
		String timestamp = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss  ").format(Calendar.getInstance().getTime());
		String undoAction = undoManager.getUndoPresentationName();
		if (undoAction.startsWith("Undo")) {
			undoAction = undoAction.substring(4).trim();
			if (undoAction.equals(""))
				undoAction = "None";
		}
		if (undoManager.lastEdit() != lastEdit) {
			lastEdit = undoManager.lastEdit();
			if (lastEdit instanceof AddHousePartCommand) {
				actedHousePart = ((AddHousePartCommand) lastEdit).getHousePart();
			} else if (lastEdit instanceof EditHousePartCommand) {
				actedHousePart = ((EditHousePartCommand) lastEdit).getHousePart();
			}
		} else {
			undoAction = "None";
		}
		if (undoAction.equals("None")) {
			System.out.println(timestamp + ": None, None, None");
		} else {
			System.out.println(timestamp + ": " + undoAction + ", " + getId(actedHousePart) + ", " + getId(getTopContainer(actedHousePart)));
		}
	}

	private String getId(final HousePart p) {
		return p.getClass().getSimpleName() + p.getId();
	}

	private HousePart getTopContainer(final HousePart p) {
		if (p == null)
			return null;
		HousePart c = p.getContainer();
		if (c == null)
			return p;
		HousePart x = null;
		while (c != null) {
			x = c;
			c = c.getContainer();
		}
		return x;
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
