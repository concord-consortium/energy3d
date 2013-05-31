package org.concord.energy3d.logger;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JOptionPane;
import javax.swing.undo.UndoableEdit;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.undo.AddHousePartCommand;
import org.concord.energy3d.undo.EditHousePartCommand;
import org.concord.energy3d.undo.RemoveHousePartCommand;
import org.concord.energy3d.undo.UndoManager;

import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;

public class TimeSeriesLogger {

	private final static DecimalFormat FORMAT = new DecimalFormat(".###");
	private final static String space = "   ";
	private int logPeriod = 1; // in seconds
	private int savePeriod = 20; // save less frequently
	private File dir;
	private File file;
	private SceneManager sceneManager;
	private UndoableEdit lastEdit;
	private UndoManager undoManager;
	private HousePart actedHousePart;
	private String content = "";
	private int counter = 0;
	private String oldHeliodonTime = null;
	private String oldHeliodonLatitude = null;
	private String oldLine = null;
	private String oldCameraPosition = null;

	public TimeSeriesLogger(int logPeriod, int savePeriod, File dir, SceneManager sceneManager) {
		this.logPeriod = logPeriod;
		this.savePeriod = savePeriod;
		this.dir = dir;
		this.sceneManager = sceneManager;
		undoManager = sceneManager.getUndoManager();
		lastEdit = undoManager.lastEdit();
	}

	private void log() {
		String timestamp = new SimpleDateFormat("yyyy-MM-dd" + space + "HH:mm:ss").format(Calendar.getInstance().getTime());
		URL url = Scene.getURL();
		if (url == null) // no logging if not using a template
			return;
		String filename = url == null ? null : new File(url.getFile()).getName();
		String undoAction = undoManager.getUndoPresentationName();
		String redoAction = undoManager.getRedoPresentationName();
		if (undoAction.startsWith("Undo")) {
			undoAction = undoAction.substring(4).trim();
			if (undoAction.equals(""))
				undoAction = null;
		}
		if (redoAction.startsWith("Redo")) {
			redoAction = redoAction.substring(4).trim();
			if (redoAction.equals(""))
				redoAction = null;
		}
		if (undoManager.lastEdit() != lastEdit) {
			lastEdit = undoManager.lastEdit();
			if (lastEdit instanceof AddHousePartCommand) {
				actedHousePart = ((AddHousePartCommand) lastEdit).getHousePart();
			} else if (lastEdit instanceof EditHousePartCommand) {
				actedHousePart = ((EditHousePartCommand) lastEdit).getHousePart();
			} else if (lastEdit instanceof RemoveHousePartCommand) {
				actedHousePart = ((RemoveHousePartCommand) lastEdit).getHousePart();
			}
		} else {
			undoAction = null;
		}
		String line = "[" + filename + "]";
		String action = redoAction == null ? undoAction : "Undo";
		if (action != null) {
			line += space + "[" + action + "]";
			line += space + "[" + getBuildingId(actedHousePart) + "]";
			line += space + "[" + getId(actedHousePart) + "]";
		}
		Calendar heliodonCalendar = Heliodon.getInstance().getCalander();
		String heliodonTime = "[Time: " + (heliodonCalendar.get(Calendar.MONTH) + 1) + "/" + heliodonCalendar.get(Calendar.DAY_OF_MONTH) + ":" + heliodonCalendar.get(Calendar.HOUR_OF_DAY) + "]";
		if (!heliodonTime.equals(oldHeliodonTime)) {
			line += space + heliodonTime;
			oldHeliodonTime = heliodonTime;
		}
		String heliodonLatitude = "[Latitude: " + Math.round(180 * Heliodon.getInstance().getLatitude() / Math.PI) + "]";
		if (!heliodonLatitude.equals(oldHeliodonLatitude)) {
			line += space + heliodonLatitude;
			oldHeliodonLatitude = heliodonLatitude;
		}
		if (sceneManager.isHeliodonControlEnabled()) {
			line += space + "[Heliodon]";
		}
		if (sceneManager.isSolarColorMap()) {
			line += space + "[Solar Map]";
		}
		if (sceneManager.isSunAnim()) {
			line += space + "[Sun Animation]";
		}
		if (sceneManager.isShadowEnabled()) {
			line += space + "[Shadow]";
		}
		if (Scene.getInstance().isAnnotationsVisible()) {
			line += space + "[Annotation]";
		}
		Camera camera = SceneManager.getInstance().getCamera();
		if (camera != null) {
			ReadOnlyVector3 location = camera.getLocation();
			ReadOnlyVector3 direction = camera.getDirection();
			String cameraPosition = FORMAT.format(location.getX());
			cameraPosition += ", " + FORMAT.format(location.getY());
			cameraPosition += ", " + FORMAT.format(location.getZ());
			cameraPosition += ", " + FORMAT.format(direction.getX());
			cameraPosition += ", " + FORMAT.format(direction.getY());
			cameraPosition += ", " + FORMAT.format(direction.getZ());
			if (!cameraPosition.equals(oldCameraPosition)) {
				line += space + "[Camera: " + cameraPosition + "]";
				oldCameraPosition = cameraPosition;
			}
		}
		if (!line.trim().endsWith(".ng3]")) {
			if (action != null || !line.equals(oldLine)) {
				System.out.println(timestamp + space + line);
				content += timestamp + space + line + System.getProperty("line.separator");
				if (counter % savePeriod == 0) {
					saveLog();
				}
				oldLine = line;
			}
		}
		counter++;
	}

	public void saveLog() {
		try {
			PrintWriter writer = new PrintWriter(file);
			writer.print(content);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "Error occured in logging! Please notify the teacher of this problem:\n" + e.getMessage(), "Logging Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private String getId(final HousePart p) {
		if (p == null)
			return null;
		return p.getClass().getSimpleName() + " #" + p.getId();
	}

	private String getBuildingId(final HousePart p) {
		if (p == null)
			return null;
		HousePart x = getTopContainer(actedHousePart);
		if (x == null)
			return null;
		return "Building #" + x.getId();
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
		String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime());
		file = new File(dir, timestamp + ".txt");
		Thread t = new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						sleep(1000 * logPeriod);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
					log();
				}
			}
		};
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

}
