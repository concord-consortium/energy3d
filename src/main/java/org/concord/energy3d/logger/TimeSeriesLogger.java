package org.concord.energy3d.logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.UndoableEdit;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.gui.MyPlainDocument;
import org.concord.energy3d.model.Building;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Thermalizable;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.Operation;
import org.concord.energy3d.scene.SceneManager.ViewMode;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.AnnualSensorData;
import org.concord.energy3d.simulation.AnnualEnvironmentalTemperature;
import org.concord.energy3d.simulation.Cost;
import org.concord.energy3d.simulation.DailyEnvironmentalTemperature;
import org.concord.energy3d.simulation.DailySensorData;
import org.concord.energy3d.simulation.EnergyAngularAnalysis;
import org.concord.energy3d.simulation.EnergyAnnualAnalysis;
import org.concord.energy3d.simulation.EnergyDailyAnalysis;
import org.concord.energy3d.undo.AddPartCommand;
import org.concord.energy3d.undo.AdjustThermostatCommand;
import org.concord.energy3d.undo.AnimateSunCommand;
import org.concord.energy3d.undo.ChangeBackgroundAlbedoCommand;
import org.concord.energy3d.undo.ChangeBuildingColorCommand;
import org.concord.energy3d.undo.ChangeBuildingSolarPanelEfficiencyCommand;
import org.concord.energy3d.undo.ChangeBuildingWindowShgcCommand;
import org.concord.energy3d.undo.ChangeBuildingUValueCommand;
import org.concord.energy3d.undo.ChangeCityCommand;
import org.concord.energy3d.undo.ChangeContainerWindowColorCommand;
import org.concord.energy3d.undo.ChangeDateCommand;
import org.concord.energy3d.undo.ChangeGroundThermalDiffusivityCommand;
import org.concord.energy3d.undo.ChangeInsideTemperatureCommand;
import org.concord.energy3d.undo.ChangeLatitudeCommand;
import org.concord.energy3d.undo.ChangePartColorCommand;
import org.concord.energy3d.undo.ChangePartUValueCommand;
import org.concord.energy3d.undo.ChangeVolumetricHeatCapacityCommand;
import org.concord.energy3d.undo.ChangeContainerWindowShgcCommand;
import org.concord.energy3d.undo.ChangeRoofOverhangCommand;
import org.concord.energy3d.undo.ChangeSolarHeatMapColorContrastCommand;
import org.concord.energy3d.undo.ChangeSolarPanelEfficiencyCommand;
import org.concord.energy3d.undo.ChangeTextureCommand;
import org.concord.energy3d.undo.ChangeTimeCommand;
import org.concord.energy3d.undo.ChangeWindowShgcCommand;
import org.concord.energy3d.undo.EditPartCommand;
import org.concord.energy3d.undo.RemoveMultiplePartsOfSameTypeCommand;
import org.concord.energy3d.undo.RemovePartCommand;
import org.concord.energy3d.undo.RotateBuildingCommand;
import org.concord.energy3d.undo.SaveCommand;
import org.concord.energy3d.undo.ShowAnnotationCommand;
import org.concord.energy3d.undo.ShowAxesCommand;
import org.concord.energy3d.undo.ShowHeliodonCommand;
import org.concord.energy3d.undo.ShowShadowCommand;
import org.concord.energy3d.undo.SpinViewCommand;
import org.concord.energy3d.undo.TopViewCommand;
import org.concord.energy3d.undo.UndoManager;
import org.concord.energy3d.util.Util;

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
	private HousePart actedPart;
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
	private final static DecimalFormat ENERGY_FORMAT = new DecimalFormat("######.##");

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
					Building b = new Building((Foundation) p);
					if (b.isWallComplete() && !buildings.contains(b)) {
						buildings.add(b);
					}
				}
			}
		}
		if (!buildings.isEmpty()) {
			result = "[";
			for (Building b : buildings)
				result += "{\"Building\": " + b.getFoundation().getId() + ", \"Daily\": " + ENERGY_FORMAT.format(b.getFoundation().getSolarPotentialToday()) + "}, ";
			result = result.trim().substring(0, result.length() - 2);
			result += "]";
		}
		return result;
	}

	private void log() {

		actedPart = null;
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
			if (lastEdit instanceof AddPartCommand) {
				actedPart = ((AddPartCommand) lastEdit).getHousePart();
			} else if (lastEdit instanceof EditPartCommand) {
				actedPart = ((EditPartCommand) lastEdit).getHousePart();
			} else if (lastEdit instanceof RemovePartCommand) {
				actedPart = ((RemovePartCommand) lastEdit).getHousePart();
			} else if (lastEdit instanceof RemoveMultiplePartsOfSameTypeCommand) {
				Foundation foundation = ((RemoveMultiplePartsOfSameTypeCommand) lastEdit).getFoundation();
				if (foundation != null)
					stateValue = "{\"Building\":" + foundation.getId() + "}";
			} else if (lastEdit instanceof RotateBuildingCommand) {
				RotateBuildingCommand c = (RotateBuildingCommand) lastEdit;
				stateValue = "{\"Building\":" + c.getFoundation().getId() + ", \"Angle\": " + Math.toDegrees(c.getRotationAngle()) + "}";
			} else if (lastEdit instanceof ChangeSolarPanelEfficiencyCommand) {
				SolarPanel sp = ((ChangeSolarPanelEfficiencyCommand) lastEdit).getSolarPanel();
				stateValue = "{\"Building\":" + sp.getTopContainer().getId() + ", \"ID\":" + sp.getId() + ", \"Value\": " + sp.getEfficiency() + "}";
			} else if (lastEdit instanceof ChangeRoofOverhangCommand) {
				Roof r = ((ChangeRoofOverhangCommand) lastEdit).getRoof();
				stateValue = "{\"Building\":" + r.getTopContainer().getId() + ", \"ID\":" + r.getId() + ", \"Value\": " + r.getOverhangLength() * Scene.getInstance().getAnnotationScale() + "}";
			} else if (lastEdit instanceof ChangeBuildingSolarPanelEfficiencyCommand) {
				Foundation foundation = ((ChangeBuildingSolarPanelEfficiencyCommand) lastEdit).getFoundation();
				List<SolarPanel> solarPanels = Scene.getInstance().getSolarPanelsOfBuilding(foundation);
				stateValue = "{\"Building\":" + foundation.getId() + ", \"Value\": " + (solarPanels.isEmpty() ? -1 : solarPanels.get(0).getEfficiency()) + "}";
			} else if (lastEdit instanceof ChangeWindowShgcCommand) {
				Window w = ((ChangeWindowShgcCommand) lastEdit).getWindow();
				stateValue = "{\"Building\":" + w.getTopContainer().getId() + ", \"ID\":" + w.getId() + ", \"Value\": " + w.getSolarHeatGainCoefficient() + "}";
			} else if (lastEdit instanceof ChangeContainerWindowShgcCommand) {
				ChangeContainerWindowShgcCommand c = (ChangeContainerWindowShgcCommand) lastEdit;
				HousePart container = c.getContainer();
				List<Window> windows = Scene.getInstance().getWindowsOnContainer(container);
				String containerType = container instanceof Wall ? "Wall" : "Roof";
				stateValue = "{\"" + containerType + "\":" + container.getId() + ", \"Value\": " + (windows.isEmpty() ? -1 : windows.get(0).getSolarHeatGainCoefficient()) + "}";
			} else if (lastEdit instanceof ChangeContainerWindowColorCommand) {
				ChangeContainerWindowColorCommand cmd = (ChangeContainerWindowColorCommand) lastEdit;
				HousePart container = cmd.getContainer();
				List<Window> windows = Scene.getInstance().getWindowsOnContainer(container);
				String containerType = container instanceof Wall ? "Wall" : "Roof";
				ReadOnlyColorRGBA c = windows.get(0).getColor();
				stateValue = "{\"" + containerType + "\":" + container.getId() + ", \"Color\": \"" + String.format("#%02x%02x%02x", (int) Math.round(c.getRed() * 255), (int) Math.round(c.getGreen() * 255), (int) Math.round(c.getBlue() * 255)) + "\"}";
			} else if (lastEdit instanceof ChangeBuildingWindowShgcCommand) {
				Foundation foundation = ((ChangeBuildingWindowShgcCommand) lastEdit).getFoundation();
				List<Window> windows = Scene.getInstance().getWindowsOfBuilding(foundation);
				stateValue = "{\"Building\":" + foundation.getId() + ", \"Value\": " + (windows.isEmpty() ? -1 : windows.get(0).getSolarHeatGainCoefficient()) + "}";
			} else if (lastEdit instanceof AdjustThermostatCommand) {
				Foundation foundation = ((AdjustThermostatCommand) lastEdit).getFoundation();
				stateValue = "{\"Building\":" + foundation.getId() + "}";
			} else if (lastEdit instanceof ChangePartUValueCommand) {
				HousePart p = ((ChangePartUValueCommand) lastEdit).getHousePart();
				if (p instanceof Thermalizable) {
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
					} else if (p instanceof Foundation) {
						s += ", \"Type\": \"Floor\"";
					}
					s += ", \"Value\": " + ((Thermalizable) p).getUValue() + "}";
					stateValue = s;
				}
			} else if (lastEdit instanceof ChangeVolumetricHeatCapacityCommand) {
				HousePart p = ((ChangeVolumetricHeatCapacityCommand) lastEdit).getHousePart();
				if (p instanceof Thermalizable) {
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
					} else if (p instanceof Foundation) {
						s += ", \"Type\": \"Floor\"";
					}
					s += ", \"Value\": " + ((Thermalizable) p).getVolumetricHeatCapacity() + "}";
					stateValue = s;
				}
			} else if (lastEdit instanceof ChangeBuildingUValueCommand) {
				HousePart p = ((ChangeBuildingUValueCommand) lastEdit).getHousePart();
				if (p instanceof Thermalizable) {
					Foundation foundation = p instanceof Foundation ? (Foundation) p : p.getTopContainer();
					String s = "{\"Building\":" + foundation.getId();
					if (p instanceof Wall) {
						s += ", \"Type\": \"Wall\"";
					} else if (p instanceof Door) {
						s += ", \"Type\": \"Door\"";
					} else if (p instanceof Foundation) {
						s += ", \"Type\": \"Floor\"";
					} else if (p instanceof Window) {
						s += ", \"Type\": \"Window\"";
					} else if (p instanceof Roof) {
						s += ", \"Type\": \"Roof\"";
					}
					s += ", \"Value\": " + ((Thermalizable) p).getUValue() + "}";
					stateValue = s;
				}
			} else if (lastEdit instanceof ChangePartColorCommand) {
				HousePart p = ((ChangePartColorCommand) lastEdit).getHousePart();
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
				ReadOnlyColorRGBA color = Scene.getInstance().getPartColorOfBuilding(c.getFoundation(), o);
				if (color != null)
					s += ", \"Color\": \"" + String.format("#%02x%02x%02x", (int) Math.round(color.getRed() * 255), (int) Math.round(color.getGreen() * 255), (int) Math.round(color.getBlue() * 255)) + "\"";
				s += "}";
				stateValue = s;
			} else if (lastEdit instanceof AnimateSunCommand) {
				stateValue = SceneManager.getInstance().isSunAnimation();
			} else if (lastEdit instanceof SpinViewCommand) {
				stateValue = SceneManager.getInstance().getSpinView();
			} else if (lastEdit instanceof ChangeTextureCommand) {
				TextureMode textureMode = Scene.getInstance().getTextureMode();
				if (textureMode == TextureMode.Full) {
					stateValue = "\"Full\"";
				} else if (textureMode == TextureMode.Simple) {
					stateValue = "\"Simple\"";
				} else if (textureMode == TextureMode.None) {
					stateValue = "\"None\"";
				}
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
			} else if (lastEdit instanceof ChangeBackgroundAlbedoCommand) {
				stateValue = Scene.getInstance().getGround().getAlbedo();
			} else if (lastEdit instanceof ChangeGroundThermalDiffusivityCommand) {
				stateValue = Scene.getInstance().getGround().getThermalDiffusivity();
			} else if (lastEdit instanceof ChangeInsideTemperatureCommand) {
				ChangeInsideTemperatureCommand c = (ChangeInsideTemperatureCommand) lastEdit;
				stateValue = c.getBuilding().getThermostat().getTemperature(c.getMonthOfYear(), c.getDayOfWeek(), c.getHourOfDay());
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
		String type2Action = null;
		if (action == null) {
			if (undoManager.getUndoFlag()) {
				action = "Undo";
				undoManager.setUndoFlag(false);
				type2Action = undoManager.getPresentationName();
			} else if (undoManager.getRedoFlag()) {
				action = "Redo";
				undoManager.setRedoFlag(false);
				type2Action = undoManager.getPresentationName();
			}
			if (undoManager.getSaveFlag()) {
				action = "Save";
				undoManager.setSaveFlag(false);
				type2Action = Scene.getURL().toString() + "*";
			}
		}

		String line = "\"File\": \"" + filename + "\"";
		if (Scene.getInstance().getProjectName() != null && !Scene.getInstance().getProjectName().trim().equals("")) {
			line += separator + "\"Project\": \"" + Scene.getInstance().getProjectName() + "\"";
		}

		analysisRequester = SceneManager.getInstance().getAnalysisRequester();
		if (analysisRequester != null) {

			analyzedPart = SceneManager.getInstance().getSelectedPart();
			analysisRequesterCopy = analysisRequester;

		} else {

			if (analysisRequesterCopy != null) { // this analysis is completed, now record some results
				line += separator + "\"" + analysisRequesterCopy.getClass().getSimpleName() + "\": ";
				if (analysisRequesterCopy instanceof AnnualSensorData) {
					AnnualSensorData asd = (AnnualSensorData) analysisRequesterCopy;
					line += "{\"Months\": " + asd.getNumberOfDataPoints() + "}";
				} else if (analysisRequesterCopy instanceof DailySensorData) {
					DailySensorData asd = (DailySensorData) analysisRequesterCopy;
					line += "{\"Hours\": " + asd.getNumberOfDataPoints() + "}";
				} else {
					if (analyzedPart != null && !(analyzedPart instanceof Tree) && !(analyzedPart instanceof Human)) { // if something analyzable is selected
						line += "{";
						String part = analyzedPart.toString().substring(0, analyzedPart.toString().indexOf(')') + 1);
						if (analysisRequesterCopy instanceof EnergyDailyAnalysis) {
							EnergyDailyAnalysis eda = (EnergyDailyAnalysis) analysisRequesterCopy;
							if (analyzedPart instanceof Foundation) {
								line += "\"Building\": " + analyzedPart.getId();
								String name = "Net";
								line += ", \"" + name + "\": " + ENERGY_FORMAT.format(eda.getResult(name));
								name = "AC";
								line += ", \"" + name + "\": " + ENERGY_FORMAT.format(eda.getResult(name));
								name = "Heater";
								line += ", \"" + name + "\": " + ENERGY_FORMAT.format(eda.getResult(name));
								name = "Windows";
								line += ", \"" + name + "\": " + ENERGY_FORMAT.format(eda.getResult(name));
								name = "Solar Panels";
								line += ", \"" + name + "\": " + ENERGY_FORMAT.format(eda.getResult(name));
							} else {
								line += "\"Part\": \"" + part + "\"";
								String name = "Solar";
								line += ", \"" + name + "\": " + ENERGY_FORMAT.format(eda.getResult(name));
								name = "Heat Gain";
								line += ", \"" + name + "\": " + ENERGY_FORMAT.format(eda.getResult(name));
							}
						} else if (analysisRequesterCopy instanceof EnergyAnnualAnalysis) {
							EnergyAnnualAnalysis eaa = (EnergyAnnualAnalysis) analysisRequesterCopy;
							line += "\"Months\": " + eaa.getNumberOfDataPoints();
							if (analyzedPart instanceof Foundation) {
								line += ", \"Building\": " + analyzedPart.getId();
								String name = "Net";
								line += ", \"" + name + "\": " + ENERGY_FORMAT.format(eaa.getResult(name));
								name = "AC";
								line += ", \"" + name + "\": " + ENERGY_FORMAT.format(eaa.getResult(name));
								name = "Heater";
								line += ", \"" + name + "\": " + ENERGY_FORMAT.format(eaa.getResult(name));
								name = "Windows";
								line += ", \"" + name + "\": " + ENERGY_FORMAT.format(eaa.getResult(name));
								name = "Solar Panels";
								line += ", \"" + name + "\": " + ENERGY_FORMAT.format(eaa.getResult(name));
							} else {
								line += ", \"Part\": \"" + part + "\"";
								String name = "Solar";
								line += ", \"" + name + "\": " + ENERGY_FORMAT.format(eaa.getResult(name));
								name = "Heat Gain";
								line += ", \"" + name + "\": " + ENERGY_FORMAT.format(eaa.getResult(name));
							}
						} else if (analysisRequesterCopy instanceof EnergyAngularAnalysis) {
							line += "\"Building\": " + LoggerUtil.getBuildingId(analyzedPart);
							EnergyAngularAnalysis eaa = (EnergyAngularAnalysis) analysisRequesterCopy;
							line += ", \"Angles\": " + eaa.getNumberOfDataPoints();
						} else if (analysisRequesterCopy instanceof Cost) {
							Cost cost = (Cost) analysisRequesterCopy;
							line += "\"Building\": " + LoggerUtil.getBuildingId(analyzedPart);
							line += ", \"Amount\": " + cost.getBuildingCost(analyzedPart instanceof Foundation ? (Foundation) analyzedPart : analyzedPart.getTopContainer());
						}
						analyzedPart = null;
						line += "}";
					} else {
						if (analysisRequesterCopy instanceof Cost) {
							line += "[";
							Cost cost = (Cost) analysisRequesterCopy;
							int count = 0;
							for (HousePart p : Scene.getInstance().getParts()) {
								if (p instanceof Foundation) {
									count++;
									line += "{\"Building\": " + LoggerUtil.getBuildingId(p) + ", \"Amount\": " + cost.getBuildingCost((Foundation) p) + "}, ";
								}
							}
							if (count > 0)
								line = line.substring(0, line.length() - 2);
							line += "]";
						} else if (analysisRequesterCopy instanceof AnnualEnvironmentalTemperature || analysisRequesterCopy instanceof DailyEnvironmentalTemperature) {
							line += "{}";
						}
					}
				}
				analysisRequesterCopy = null;
			}

			if (action != null) {
				line += separator + "\"" + action + "\": ";
				if (type2Action != null) {
					line += "\"" + type2Action + "\"";
				} else {
					if (actedPart != null) {
						line += LoggerUtil.getInfo(actedPart);
					} else if (stateValue != null) {
						line += stateValue;
					} else {
						line += "null";
					}
				}
			}

			// record the daily solar radiation results
			if (SceneManager.getInstance().getSolarHeatMap() && SceneManager.getInstance().areBuildingLabelsVisible()) {
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
			Util.reportError(e);
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
						Util.reportError(t);
					}
				}
			}
		};
		t.setPriority(Thread.MIN_PRIORITY);
		t.start();
	}

}
