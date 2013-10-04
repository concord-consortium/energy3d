package org.concord.energy3d.logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoableEdit;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.HousePart;
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

public class TimeSeriesLogger implements PropertyChangeListener {

	private final static DecimalFormat FORMAT = new DecimalFormat(".###");
	private final static String space = "   ";
	private int logInterval = 1; // in seconds
	private int saveInterval = 5; // save every N valid actions
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
	private boolean noteEditedFlag = false;
	private boolean sceneEditedFlag = false;

	public TimeSeriesLogger(final int logInterval, final int saveInterval, final SceneManager sceneManager) {
		this.logInterval = logInterval;
		this.saveInterval = saveInterval;
		this.sceneManager = sceneManager;
		undoManager = sceneManager.getUndoManager();
		lastEdit = undoManager.lastEdit();
		MainPanel.getInstance().getNoteTextArea().getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(final DocumentEvent e) {
				noteEditedFlag = true;
			}

			@Override
			public void insertUpdate(final DocumentEvent e) {
				noteEditedFlag = true;
			}

			@Override
			public void changedUpdate(final DocumentEvent e) {
				noteEditedFlag = true;
			}
		});
	}

	private void log() {
		final String timestamp = new SimpleDateFormat("yyyy-MM-dd" + space + "HH:mm:ss").format(Calendar.getInstance().getTime());
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
		String line = "\"" + filename + "\"";
		if (action != null) {
			line += space + "{" + action + "}";
			if (!type2Action) {
				line += space + "{" + LoggerUtil.getBuildingId(actedHousePart, true) + "}";
				line += space + "{" + LoggerUtil.getId(actedHousePart) + "}";
			}
		}
		final Calendar heliodonCalendar = Heliodon.getInstance().getCalander();
		final String heliodonTime = "[Time: " + (heliodonCalendar.get(Calendar.MONTH) + 1) + "/" + heliodonCalendar.get(Calendar.DAY_OF_MONTH) + ":" + heliodonCalendar.get(Calendar.HOUR_OF_DAY) + "]";
		if (!heliodonTime.equals(oldHeliodonTime)) {
			if (!sceneManager.isSunAnim()) // don't log time if sun path is animated
				line += space + heliodonTime;
			oldHeliodonTime = heliodonTime;
		}
		final String heliodonLatitude = "[Latitude: " + Math.round(180 * Heliodon.getInstance().getLatitude() / Math.PI) + "]";
		if (!heliodonLatitude.equals(oldHeliodonLatitude)) {
			line += space + heliodonLatitude;
			oldHeliodonLatitude = heliodonLatitude;
		}
		if (sceneManager.isHeliodonControlEnabled()) {
			line += space + "<Heliodon>";
		}
		if (sceneManager.isSolarColorMap()) {
			line += space + "<Solar Map>";
		}
		if (sceneManager.isSunAnim()) {
			line += space + "<Sun Animation>";
		}
		if (sceneManager.isShadowEnabled()) {
			line += space + "<Shadow>";
		}
		if (Scene.getInstance().isAnnotationsVisible()) {
			line += space + "<Annotation>";
		}
		final Camera camera = SceneManager.getInstance().getCamera();
		if (camera != null) {
			final ReadOnlyVector3 location = camera.getLocation();
			final ReadOnlyVector3 direction = camera.getDirection();
			String cameraPosition = "(" + FORMAT.format(location.getX());
			cameraPosition += ", " + FORMAT.format(location.getY());
			cameraPosition += ", " + FORMAT.format(location.getZ());
			cameraPosition += ")   (" + FORMAT.format(direction.getX());
			cameraPosition += ", " + FORMAT.format(direction.getY());
			cameraPosition += ", " + FORMAT.format(direction.getZ()) + ")";
			if (!cameraPosition.equals(oldCameraPosition)) {
				if (!sceneManager.isRotationAnimationOn()) // don't log camera if the view is being spun
					line += space + "[Camera: " + cameraPosition + "]";
				oldCameraPosition = cameraPosition;
			}
		}
		if (noteEditedFlag) {
			final String note = MainPanel.getInstance().getNoteTextArea().getText();
			line += space + "[Note: " + note.length() + "]";
			noteEditedFlag = false;
		}
		if (!line.trim().endsWith(".ng3\"")) {
			if (action != null || !line.equals(oldLine)) {
				// System.out.println("#" + counter + ": " + timestamp + space + line);
				content += timestamp + space + line + System.getProperty("line.separator");
				if (counter % saveInterval == 0) {
					saveLog();
				}
				oldLine = line;
				counter++;
			}
		}
	}

	public void saveLog() {
		try {
			final PrintWriter writer = new PrintWriter(file);
			writer.print(content);
			writer.close();
		} catch (final Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "Error occured in logging! Please notify the teacher of this problem:\n" + e.getMessage(), "Logging Error", JOptionPane.ERROR_MESSAGE);
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
		file = new File(LoggerUtil.getLogFolder(), timestamp + ".txt");
		final Thread t = new Thread("Time Series Logger") {
			@Override
			public void run() {
				while (true) {
					try {
						sleep(1000 * logInterval);
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
