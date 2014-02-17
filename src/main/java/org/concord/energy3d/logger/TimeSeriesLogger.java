package org.concord.energy3d.logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.UndoableEdit;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.gui.MyPlainDocument;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.undo.AddHousePartCommand;
import org.concord.energy3d.undo.EditHousePartCommand;
import org.concord.energy3d.undo.RemoveHousePartCommand;
import org.concord.energy3d.undo.SaveCommand;
import org.concord.energy3d.undo.UndoManager;

import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;

/**
 * Code the activity stream using JSON format
 * 
 * @author Charles Xie
 * 
 */
public class TimeSeriesLogger implements PropertyChangeListener {

	private final static String separator = ",   ";
	private int logInterval = 2; // in seconds
	private int saveInterval = 1; // save every N valid actions
	private File file;
	private final SceneManager sceneManager;
	private UndoableEdit lastEdit;
	private final UndoManager undoManager;
	private HousePart actedHousePart;
	private String content = "";
	private int counter = 0;
	private String oldHeliodonTime = null;
	private String oldHeliodonLatitude = null;
	private String oldLine = null;
	private String oldCameraPosition = null;
	private String noteString = "";
	private boolean noteEditedFlag = false;
	private boolean sceneEditedFlag = false;
	private volatile boolean solarCalculationFinished = false;
	private ArrayList<Building> buildings = new ArrayList<Building>();

	public TimeSeriesLogger(final int logInterval, final int saveInterval, final SceneManager sceneManager) {
		this.logInterval = logInterval;
		this.saveInterval = saveInterval;
		this.sceneManager = sceneManager;
		undoManager = sceneManager.getUndoManager();
		lastEdit = undoManager.lastEdit();
		final Document noteAreaDoc = MainPanel.getInstance().getNoteTextArea().getDocument();
		noteAreaDoc.addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(final DocumentEvent e) {
				noteEditedFlag = true;
				if (noteAreaDoc instanceof MyPlainDocument) {
					String s = ((MyPlainDocument) noteAreaDoc).getRemovedString();
					if (s != null) {
						s = s.replace("\n", "-linebreak-");
						noteString += "D(" + e.getOffset() + "," + s + ")";
					}
				}
			}

			@Override
			public void insertUpdate(final DocumentEvent e) {
				noteEditedFlag = true;
				String s = null;
				try {
					s = noteAreaDoc.getText(e.getOffset(), e.getLength());
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				if (s != null) {
					s = s.replace("\n", "-linebreak-");
					s = s.replace("\\", "\\\\");
					s = s.replace("\"", "\\\"");
					noteString += "I(" + e.getOffset() + "," + s + ")";
				}
			}

			@Override
			public void changedUpdate(final DocumentEvent e) {
				noteEditedFlag = true;
			}
		});
	}

	private String getBuildingSolarEnergies() {
		String result = "";
		buildings.clear();
		List<HousePart> list = Scene.getInstance().getParts();
		synchronized (list) {
			for (HousePart p : list) {
				if (p instanceof Foundation) {
					Building b = new Building((int) p.getId());
					ArrayList<HousePart> children = p.getChildren();
					for (HousePart x : children) {
						if (x instanceof Wall)
							b.addWall((Wall) x);
					}
					if (b.isComplete() && !buildings.contains(b)) {
						buildings.add(b);
					}
				}
			}
		}
		if (!buildings.isEmpty()) {
			result = "[";
			for (Building b : buildings)
				result += "{\"#" + b.id + "\": " + b.getSolarEnergy() + "}, ";
			result = result.trim().substring(0, result.length() - 2);
			result += "]";
		}
		return result;
	}

	private void log() {

		final String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		final URL url = Scene.getURL();
		if (url == null) // no logging if not working on a saved file
			return;
		final String filename = url == null ? null : new File(url.getFile()).getName();
		String action = undoManager.getUndoPresentationName();
		if (action.startsWith("Undo")) {
			action = action.substring(4).trim();
			if (action.equals(""))
				action = null;
		}
		if (!(undoManager.lastEdit() instanceof SaveCommand) && undoManager.lastEdit() != lastEdit) {
			lastEdit = undoManager.lastEdit();
			if (lastEdit instanceof AddHousePartCommand) {
				actedHousePart = ((AddHousePartCommand) lastEdit).getHousePart();
			} else if (lastEdit instanceof EditHousePartCommand) {
				actedHousePart = ((EditHousePartCommand) lastEdit).getHousePart();
			} else if (lastEdit instanceof RemoveHousePartCommand) {
				actedHousePart = ((RemoveHousePartCommand) lastEdit).getHousePart();
			}
		} else {
			action = null;
		}
		boolean type2Action = false;
		if (action == null) {
			if (undoManager.getUndoFlag()) {
				action = "Undo";
				undoManager.setUndoFlag(false);
				type2Action = true;
			} else if (undoManager.getRedoFlag()) {
				action = "Redo";
				undoManager.setRedoFlag(false);
				type2Action = true;
			}
			if (undoManager.getSaveFlag()) {
				action = "Save";
				undoManager.setSaveFlag(false);
				type2Action = true;
			}
		}
		String line = "\"File\": \"" + filename + "\"";
		if (action != null) {
			line += separator + "\"" + action + "\": " + (type2Action ? "null" : LoggerUtil.getInfo(actedHousePart));
		}

		// toggle actions
		if (sceneManager.isHeliodonControlEnabled()) {
			line += separator + "\"Heliodon\": true";
		}
		if (sceneManager.isSolarColorMap()) {
			line += separator + "\"SolarMap\": true";
			if (solarCalculationFinished) {
				String result = getBuildingSolarEnergies();
				if (result.length() > 0) {
					line += separator + "\"SolarEnergy\": " + result;
				}
				solarCalculationFinished = false;
			}
		}
		if (sceneManager.isShadowEnabled()) {
			line += separator + "\"Shadow\": true";
		}
		if (Scene.getInstance().isAnnotationsVisible()) {
			line += separator + "\"Annotation\": true";
		}

		// continuous actions
		final String heliodonLatitude = "\"Latitude\": " + Math.round(180 * Heliodon.getInstance().getLatitude() / Math.PI);
		if (!heliodonLatitude.equals(oldHeliodonLatitude)) {
			line += separator + heliodonLatitude;
			oldHeliodonLatitude = heliodonLatitude;
		}
		if (sceneManager.isSunAnim()) {
			line += separator + "\"SunAnimation\": true";
		} else {
			final Calendar heliodonCalendar = Heliodon.getInstance().getCalander();
			final String heliodonTime = "\"Time\": \"" + (heliodonCalendar.get(Calendar.MONTH) + 1) + "/" + heliodonCalendar.get(Calendar.DAY_OF_MONTH) + ":" + heliodonCalendar.get(Calendar.HOUR_OF_DAY) + "\"";
			if (!heliodonTime.equals(oldHeliodonTime)) {
				if (!sceneManager.isSunAnim()) // don't log time if sun path is animated
					line += separator + heliodonTime;
				oldHeliodonTime = heliodonTime;
			}
		}
		if (sceneManager.isRotationAnimationOn()) {
			line += separator + "\"RotationAnimation\": true";
		} else {
			final Camera camera = SceneManager.getInstance().getCamera();
			if (camera != null) {
				final ReadOnlyVector3 location = camera.getLocation();
				final ReadOnlyVector3 direction = camera.getDirection();
				String cameraPosition = "\"Position\": {\"x\": " + LoggerUtil.FORMAT.format(location.getX());
				cameraPosition += ", \"y\": " + LoggerUtil.FORMAT.format(location.getY());
				cameraPosition += ", \"z\": " + LoggerUtil.FORMAT.format(location.getZ());
				cameraPosition += "}, \"Direction\": {\"x\": " + LoggerUtil.FORMAT.format(direction.getX());
				cameraPosition += ", \"y\": " + LoggerUtil.FORMAT.format(direction.getY());
				cameraPosition += ", \"z\": " + LoggerUtil.FORMAT.format(direction.getZ()) + "}";
				if (!cameraPosition.equals(oldCameraPosition)) {
					if (!sceneManager.isRotationAnimationOn()) // don't log camera if the view is being spun
						line += separator + "\"Camera\": {" + cameraPosition + "}";
					oldCameraPosition = cameraPosition;
				}
			}
		}

		if (noteEditedFlag) {
			if (noteString.length() > 0) {
				line += separator + "\"Note\": \"" + noteString + "\"";
				noteString = "";
			}
			noteEditedFlag = false;
		}

		if (!line.trim().endsWith(".ng3\"")) {
			if (action != null || !line.equals(oldLine)) {
				content += "{\"Timestamp\": \"" + timestamp + "\"" + separator + line + "},\n";
				if (counter % saveInterval == 0) {
					saveLog();
				}
				oldLine = line;
				counter++;
			}
		}
	}

	public void saveLog() {
		if (content == null || content.length() <= 0)
			return;
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(file);
			writer.print("{\n\"Activities\": [\n" + content.substring(0, content.length() - 2) + "\n]\n}");
		} catch (final Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "Error occured in logging: " + e.getMessage() + "\nPlease restart Energy3D.", "Logging Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		if (evt.getSource() == Scene.getInstance()) {
			if (evt.getPropertyName().equals("Edit")) {
				final Object newValue = evt.getNewValue();
				if (newValue.equals(Boolean.TRUE))
					sceneEditedFlag = true;
			}
		} else if (evt.getSource() == EnergyPanel.getInstance()) {
			solarCalculationFinished = true;
		}
	}

	public boolean isEdited() {
		return sceneEditedFlag || noteEditedFlag;
	}

	public void resetEditFlags() {
		sceneEditedFlag = false;
		noteEditedFlag = false;
	}

	public void start() {
		final String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime());
		file = new File(LoggerUtil.getLogFolder(), timestamp + ".json");
		final Thread t = new Thread("Time Series Logger") {
			@Override
			public void run() {
				while (true) {
					try {
						sleep(1000 * logInterval);
					} catch (final InterruptedException e) {
						e.printStackTrace();
					}
					try {
						log();
					} catch (Throwable t) {
						t.printStackTrace();
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Error occured in logging: " + t + "\nPlease restart Energy3D.", "Logging Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

}
