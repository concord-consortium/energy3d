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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.UndoableEdit;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.gui.MyPlainDocument;
import org.concord.energy3d.model.Building;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.Operation;
import org.concord.energy3d.scene.SceneManager.ViewMode;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.AnnualSensorData;
import org.concord.energy3d.simulation.Cost;
import org.concord.energy3d.simulation.EnergyAngularAnalysis;
import org.concord.energy3d.simulation.EnergyAnnualAnalysis;
import org.concord.energy3d.undo.AddHousePartCommand;
import org.concord.energy3d.undo.AnimateSunCommand;
import org.concord.energy3d.undo.ChangeBuildingColorCommand;
import org.concord.energy3d.undo.ChangeBuildingSolarPanelEfficiencyCommand;
import org.concord.energy3d.undo.ChangeBuildingWindowShgcCommand;
import org.concord.energy3d.undo.ChangeBuildingUFactorCommand;
import org.concord.energy3d.undo.ChangeCityCommand;
import org.concord.energy3d.undo.ChangeDateCommand;
import org.concord.energy3d.undo.ChangeInsideTemperatureCommand;
import org.concord.energy3d.undo.ChangeLatitudeCommand;
import org.concord.energy3d.undo.ChangePartColorCommand;
import org.concord.energy3d.undo.ChangePartUFactorCommand;
import org.concord.energy3d.undo.ChangeSolarHeatMapColorContrastCommand;
import org.concord.energy3d.undo.ChangeSolarPanelEfficiencyCommand;
import org.concord.energy3d.undo.ChangeTimeCommand;
import org.concord.energy3d.undo.ChangeWindowShgcCommand;
import org.concord.energy3d.undo.ComputeEnergyCommand;
import org.concord.energy3d.undo.EditHousePartCommand;
import org.concord.energy3d.undo.RemoveHousePartCommand;
import org.concord.energy3d.undo.SaveCommand;
import org.concord.energy3d.undo.ShowAnnotationCommand;
import org.concord.energy3d.undo.ShowAxesCommand;
import org.concord.energy3d.undo.ShowHeliodonCommand;
import org.concord.energy3d.undo.ShowShadowCommand;
import org.concord.energy3d.undo.SpinViewCommand;
import org.concord.energy3d.undo.TopViewCommand;
import org.concord.energy3d.undo.UndoManager;

import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;

/**
 * Encode activity stream in JSON format
 * 
 * @author Charles Xie
 * 
 */
public class TimeSeriesLogger implements PropertyChangeListener {

	private final static String separator = ",   ";
	private int logInterval = 2; // in seconds
	private File file;
	private UndoableEdit lastEdit;
	private final UndoManager undoManager;
	private HousePart actedHousePart;
	private Object stateValue;
	private String oldLine = null;
	private String oldCameraPosition = null;
	private String noteString = "";
	private boolean noteEditedFlag = false;
	private boolean sceneEditedFlag = false;
	private volatile boolean solarCalculationFinished = false;
	private ArrayList<Building> buildings = new ArrayList<Building>();
	private Object analysisRequester;
	private Object analysisRequesterCopy;
	private HousePart analyzedPart;
	private PrintWriter writer;
	private boolean firstLine = true;

	public TimeSeriesLogger(final int logInterval) {
		this.logInterval = logInterval;
		undoManager = SceneManager.getInstance().getUndoManager();
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
						s = s.replace("\t", "-tab-");
						s = s.replace("\\", "\\\\");
						s = s.replace("\"", "\\\"");
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
					s = s.replace("\t", "-tab-");
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
					if (b.isWallComplete() && !buildings.contains(b)) {
						buildings.add(b);
					}
				}
			}
		}
		if (!buildings.isEmpty()) {
			result = "[";
			for (Building b : buildings)
				result += "{\"#" + b.getID() + "\": " + b.getSolarEnergy() + "}, ";
			result = result.trim().substring(0, result.length() - 2);
			result += "]";
		}
		return result;
	}

	private void log() {

		actedHousePart = null;
		stateValue = null;

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
			} else if (lastEdit instanceof ChangeSolarPanelEfficiencyCommand) {
				actedHousePart = ((ChangeSolarPanelEfficiencyCommand) lastEdit).getSolarPanel();
			} else if (lastEdit instanceof ChangeBuildingSolarPanelEfficiencyCommand) {
				actedHousePart = ((ChangeBuildingSolarPanelEfficiencyCommand) lastEdit).getFoundation();
			} else if (lastEdit instanceof ChangeWindowShgcCommand) {
				actedHousePart = ((ChangeWindowShgcCommand) lastEdit).getWindow();
			} else if (lastEdit instanceof ChangeBuildingWindowShgcCommand) {
				actedHousePart = ((ChangeBuildingWindowShgcCommand) lastEdit).getFoundation();
			} else if (lastEdit instanceof ChangePartUFactorCommand) {
				ChangePartUFactorCommand c = (ChangePartUFactorCommand) lastEdit;
				HousePart p = c.getHousePart();
				Foundation foundation = p instanceof Foundation ? (Foundation) p : p.getTopContainer();
				String s = "{\"Building\":" + foundation.getId() + ", \"ID\":" + p.getId();
				if (p instanceof Wall) {
					s += ", \"Type\": \"Wall\"";
				} else if (p instanceof Door) {
					s += ", \"Type\": \"Door\"";
				} else if (p instanceof Window) {
					s += ", \"Type\": \"Window\"";
				} else if (p instanceof Roof) {
					s += ", \"Type\": \"Roof\"";
				}
				s += ", \"Value\": " + p.getUFactor() + "}";
				stateValue = s;
			} else if (lastEdit instanceof ChangeBuildingUFactorCommand) {
				ChangeBuildingUFactorCommand c = (ChangeBuildingUFactorCommand) lastEdit;
				String s = "{\"Building\":" + c.getFoundation().getId();
				Operation o = c.getOperation();
				if (o == Operation.DRAW_WALL) {
					s += ", \"Type\": \"Wall\"";
				} else if (o == Operation.DRAW_DOOR) {
					s += ", \"Type\": \"Door\"";
				} else if (o == Operation.DRAW_WINDOW) {
					s += ", \"Type\": \"Window\"";
				} else if (o == Operation.DRAW_ROOF_PYRAMID) {
					s += ", \"Type\": \"Roof\"";
				}
				s += ", \"Value\": " + Scene.getInstance().getPartUFactorForWholeBuilding(c.getFoundation(), o) + "}";
				stateValue = s;
			} else if (lastEdit instanceof ChangePartColorCommand) {
				ChangePartColorCommand c = (ChangePartColorCommand) lastEdit;
				HousePart p = c.getHousePart();
				Foundation foundation = p instanceof Foundation ? (Foundation) p : p.getTopContainer();
				String s = "{\"Building\":" + foundation.getId() + ", \"ID\":" + p.getId();
				if (p instanceof Foundation) {
					s += ", \"Type\": \"Foundation\"";
				} else if (p instanceof Wall) {
					s += ", \"Type\": \"Wall\"";
				} else if (p instanceof Door) {
					s += ", \"Type\": \"Door\"";
				} else if (p instanceof Floor) {
					s += ", \"Type\": \"Floor\"";
				} else if (p instanceof Roof) {
					s += ", \"Type\": \"Roof\"";
				}
				ReadOnlyColorRGBA color = p.getColor();
				if (color != null)
					s += ", \"Color\": \"" + String.format("#%02x%02x%02x", (int) Math.round(color.getRed() * 255), (int) Math.round(color.getGreen() * 255), (int) Math.round(color.getBlue() * 255)) + "\"";
				s += "}";
				stateValue = s;
			} else if (lastEdit instanceof ChangeBuildingColorCommand) {
				ChangeBuildingColorCommand c = (ChangeBuildingColorCommand) lastEdit;
				String s = "{\"Building\":" + c.getFoundation().getId();
				Operation o = c.getOperation();
				if (o == Operation.DRAW_FOUNDATION) {
					s += ", \"Type\": \"Foundation\"";
				} else if (o == Operation.DRAW_WALL) {
					s += ", \"Type\": \"Wall\"";
				} else if (o == Operation.DRAW_DOOR) {
					s += ", \"Type\": \"Door\"";
				} else if (o == Operation.DRAW_FLOOR) {
					s += ", \"Type\": \"Floor\"";
				} else if (o == Operation.DRAW_ROOF_PYRAMID) {
					s += ", \"Type\": \"Roof\"";
				}
				ReadOnlyColorRGBA color = Scene.getInstance().getPartColorForWholeBuilding(c.getFoundation(), o);
				if (color != null)
					s += ", \"Color\": \"" + String.format("#%02x%02x%02x", (int) Math.round(color.getRed() * 255), (int) Math.round(color.getGreen() * 255), (int) Math.round(color.getBlue() * 255)) + "\"";
				s += "}";
				stateValue = s;
			} else if (lastEdit instanceof AnimateSunCommand) {
				stateValue = SceneManager.getInstance().isSunAnimation();
			} else if (lastEdit instanceof SpinViewCommand) {
				stateValue = SceneManager.getInstance().getSpinView();
			} else if (lastEdit instanceof ShowAxesCommand) {
				stateValue = SceneManager.getInstance().areAxesVisible();
			} else if (lastEdit instanceof TopViewCommand) {
				stateValue = SceneManager.getInstance().getViewMode() == ViewMode.TOP_VIEW;
			} else if (lastEdit instanceof ShowShadowCommand) {
				stateValue = SceneManager.getInstance().isShadowEnabled();
			} else if (lastEdit instanceof ShowAnnotationCommand) {
				stateValue = Scene.getInstance().areAnnotationsVisible();
			} else if (lastEdit instanceof ShowHeliodonCommand) {
				stateValue = SceneManager.getInstance().isHeliodonVisible();
			} else if (lastEdit instanceof ComputeEnergyCommand) {
				stateValue = SceneManager.getInstance().getSolarHeatMap();
			} else if (lastEdit instanceof ChangeInsideTemperatureCommand) {
				stateValue = Scene.getInstance().getInsideTemperature();
			} else if (lastEdit instanceof ChangeSolarHeatMapColorContrastCommand) {
				stateValue = Scene.getInstance().getSolarHeatMapColorContrast();
			} else if (lastEdit instanceof ChangeLatitudeCommand) {
				stateValue = Math.round(180 * Heliodon.getInstance().getLatitude() / Math.PI);
			} else if (lastEdit instanceof ChangeCityCommand) {
				stateValue = "\"" + Scene.getInstance().getCity() + "\"";
			} else if (lastEdit instanceof ChangeDateCommand) {
				Calendar calendar = Heliodon.getInstance().getCalender();
				stateValue = "\"" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "\"";
			} else if (lastEdit instanceof ChangeTimeCommand) {
				Calendar calendar = Heliodon.getInstance().getCalender();
				stateValue = "\"" + (calendar.get(Calendar.HOUR_OF_DAY)) + ":" + calendar.get(Calendar.MINUTE) + "\"";
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
		analysisRequester = EnergyPanel.getInstance().getDisableActionsRequester();
		if (analysisRequester != null) {

			analysisRequesterCopy = analysisRequester;
			analyzedPart = SceneManager.getInstance().getSelectedPart();
			if (analyzedPart == null) // this analysis requires no selection
				line += separator + "\"" + analysisRequester.getClass().getSimpleName() + "\": {}";

		} else {

			if (analysisRequesterCopy != null && analyzedPart != null) { // this analysis is completed, now record some results
				String part = analyzedPart.toString().substring(0, analyzedPart.toString().indexOf(')') + 1);
				line += separator + "\"" + analysisRequesterCopy.getClass().getSimpleName() + "\": {";
				line += "\"Object\": \"" + part + "\"";
				if (analysisRequesterCopy instanceof EnergyAnnualAnalysis) {
					EnergyAnnualAnalysis eaa = (EnergyAnnualAnalysis) analysisRequesterCopy;
					line += ", \"Calculated Months\": " + eaa.getNumberOfDataPoints();
					if (analyzedPart instanceof Foundation) {
						String name = "Net";
						line += ", \"" + name + "\": " + eaa.getResult(name);
						name = "AC";
						line += ", \"" + name + "\": " + eaa.getResult(name);
						name = "Heater";
						line += ", \"" + name + "\": " + eaa.getResult(name);
						name = "Windows";
						line += ", \"" + name + "\": " + eaa.getResult(name);
						name = "Solar Panels";
						line += ", \"" + name + "\": " + eaa.getResult(name);
					} else {
						String name = "Solar";
						line += ", \"" + name + "\": " + eaa.getResult(name);
						name = "Heat Gain";
						line += ", \"" + name + "\": " + eaa.getResult(name);
					}
				} else if (analysisRequesterCopy instanceof EnergyAngularAnalysis) {
					EnergyAngularAnalysis eaa = (EnergyAngularAnalysis) analysisRequesterCopy;
					line += ", \"Calculated Angles\": " + eaa.getNumberOfDataPoints();
				} else if (analysisRequesterCopy instanceof AnnualSensorData) {
					AnnualSensorData asd = (AnnualSensorData) analysisRequesterCopy;
					line += ", \"Calculated Months\": " + asd.getNumberOfDataPoints();
				} else if (analysisRequesterCopy instanceof Cost) {
					Cost cost = (Cost) analysisRequesterCopy;
					line += ", \"Cost\": " + cost.getTotalCost();
				}
				line += "}";
				analyzedPart = null;
				analysisRequesterCopy = null;
			}

			if (action != null) {
				line += separator + "\"" + action + "\": ";
				if (type2Action) {
					line += "null";
				} else {
					if (actedHousePart != null) {
						line += LoggerUtil.getInfo(actedHousePart);
					} else if (stateValue != null) {
						line += stateValue;
					} else {
						line += "null";
					}
				}
			}

			// record the daily solar radiation results
			if (SceneManager.getInstance().getSolarHeatMap()) {
				if (solarCalculationFinished) {
					String result = getBuildingSolarEnergies();
					if (result.length() > 0) {
						line += separator + "\"SolarEnergy\": " + result;
					}
					solarCalculationFinished = false;
				}
			}

			if (!SceneManager.getInstance().getSpinView()) {
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
						if (!SceneManager.getInstance().getSpinView()) // don't log camera if the view is being spun
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

		}

		if (!line.trim().endsWith(".ng3\"")) {
			if (action != null || !line.equals(oldLine)) {
				if (firstLine) {
					firstLine = false;
				} else {
					writer.write(",\n");
				}
				writer.write("{\"Timestamp\": \"" + timestamp + "\"" + separator + line + "}");
				writer.flush();
				oldLine = line;
			}
		}
	}

	public void closeLog() {
		if (writer != null) {
			writer.write("]\n}");
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
		try {
			writer = new PrintWriter(file);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(MainFrame.getInstance(), e.getMessage(), "Logger Error", JOptionPane.WARNING_MESSAGE);
		}
		writer.write("{\n\"Activities\": [\n");
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
