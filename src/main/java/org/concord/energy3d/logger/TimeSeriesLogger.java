package org.concord.energy3d.logger;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.undo.UndoableEdit;

import org.concord.energy3d.gui.DailyEnergyGraph;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Building;
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
import org.concord.energy3d.simulation.SolarAnnualAnalysis;
import org.concord.energy3d.simulation.SolarDailyAnalysis;
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
import org.concord.energy3d.undo.MoveBuildingCommand;
import org.concord.energy3d.undo.PastePartCommand;
import org.concord.energy3d.undo.RemoveMultiplePartsOfSameTypeCommand;
import org.concord.energy3d.undo.RemovePartCommand;
import org.concord.energy3d.undo.RotateBuildingCommand;
import org.concord.energy3d.undo.ShowAnnotationCommand;
import org.concord.energy3d.undo.ShowAxesCommand;
import org.concord.energy3d.undo.ShowHeatFluxCommand;
import org.concord.energy3d.undo.ShowHeliodonCommand;
import org.concord.energy3d.undo.ShowShadowCommand;
import org.concord.energy3d.undo.SpinViewCommand;
import org.concord.energy3d.undo.TopViewCommand;
import org.concord.energy3d.util.Util;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;

/**
 * Encode activity stream in JSON format
 * 
 * @author Charles Xie
 * 
 */
public class TimeSeriesLogger {

	private final static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final static int MINIMUM_INTERVAL = 200; // in milliseconds
	private File file;
	private Object analysisRequester;
	private String action;
	private String cameraPosition, cameraMode;
	private PrintWriter writer;
	private boolean firstRecord = true;
	private String lastAction;
	private Date lastTime;
	private String lastCameraPosition;

	// Ugly logging
	private String graphTabName;
	private String graphName, curveName, runID;
	private boolean curveShown, runShown;

	private static final TimeSeriesLogger instance = new TimeSeriesLogger();

	private TimeSeriesLogger() {
	}

	public static TimeSeriesLogger getInstance() {
		return instance;
	}

	private void record() {

		final URL url = Scene.getURL();
		if (url == null) // no logging if not working on a saved file
			return;

		/* write the header */

		final String filename = url == null ? null : new File(url.getFile()).getName();
		final Date time = Calendar.getInstance().getTime();
		final String timestamp = TIME_FORMAT.format(time);
		String line = "";
		if (Scene.getInstance().getProjectName() != null && !Scene.getInstance().getProjectName().trim().equals("")) {
			line += "\"Project\": \"" + Scene.getInstance().getProjectName() + "\", ";
		}
		line += "\"File\": \"" + filename + "\"";

		/* actions registered with the Undo Manager */

		if (action != null) {

			if ((lastTime != null && Math.abs(time.getTime() - lastTime.getTime()) < MINIMUM_INTERVAL) && action.equals(lastAction))
				return; // don't log too frequently for the same type of action

			HousePart actedPart = null;
			String stateValue = null;

			// special treatments
			if (action.equals("Undo")) {
				String s = SceneManager.getInstance().getUndoManager().getRedoPresentationName();
				if (s.length() >= 4)
					s = s.substring(4, s.length()).trim();
				stateValue = "\"" + s + "\"";
			} else if (action.equals("Redo")) {
				String s = SceneManager.getInstance().getUndoManager().getUndoPresentationName();
				if (s.length() >= 4)
					s = s.substring(4, s.length()).trim();
				stateValue = "\"" + s + "\"";
			} else if (action.equals("Save")) {
				stateValue = "\"" + Scene.getURL().toString() + "*\""; // append * at the end so that the ng3 suffix is not interpreted as a delimiter
			} else if (action.equals("Note")) {
				String s = MainPanel.getInstance().getNoteString();
				if (s.length() > 0) {
					stateValue = "\"" + s + "\"";
					MainPanel.getInstance().setNoteString("");
				}
			} else if (action.equals("Camera")) {
				stateValue = "{" + cameraPosition + ", \"Mode\": \"" + cameraMode + "\"}";
			} else if (action.equals("Graph Tab")) {
				stateValue = "\"" + graphTabName + "\"";
			} else if (action.equals("Adjust Thermostat")) {
				HousePart p = SceneManager.getInstance().getSelectedPart();
				if (p instanceof Foundation) {
					stateValue = "{\"Building\": " + p.getId() + "}";
				}
			} else if (action.equals("Show Curve")) {
				stateValue = "{\"Graph\": \"" + graphName + "\", \"Name\": \"" + curveName + "\", \"Shown\": " + curveShown + "}";
			} else if (action.equals("Show Run")) {
				stateValue = "{\"Graph\": \"" + graphName + "\", \"ID\": \"" + runID + "\", \"Shown\": " + runShown + "}";
			} else if (action.equals("Clear Graph Data")) {
				stateValue = "\"" + graphName + "\"";
			} else {

				// everything else
				UndoableEdit lastEdit = SceneManager.getInstance().getUndoManager().lastEdit();

				// add, edit, or remove parts
				if (lastEdit instanceof AddPartCommand) {
					actedPart = ((AddPartCommand) lastEdit).getPart();
				} else if (lastEdit instanceof PastePartCommand) {
					actedPart = ((PastePartCommand) lastEdit).getPart();
					if (actedPart instanceof Foundation) {
						stateValue = "{\"Building\": " + actedPart.getId() + "}";
						actedPart = null; // FIXME: work around a bug related to pasting a building, fix later
					}
				} else if (lastEdit instanceof EditPartCommand) {
					actedPart = ((EditPartCommand) lastEdit).getPart();
				} else if (lastEdit instanceof RemovePartCommand) {
					actedPart = ((RemovePartCommand) lastEdit).getPart();
				} else if (lastEdit instanceof RemoveMultiplePartsOfSameTypeCommand) {
					Foundation foundation = ((RemoveMultiplePartsOfSameTypeCommand) lastEdit).getFoundation();
					if (foundation != null)
						stateValue = "{\"Building\": " + foundation.getId() + "}";
				} else if (lastEdit instanceof RotateBuildingCommand) {
					RotateBuildingCommand c = (RotateBuildingCommand) lastEdit;
					Foundation f = c.getFoundation();
					if (f != null) {
						stateValue = "{\"Building\": " + f.getId() + ", \"Angle\": " + Math.toDegrees(c.getRotationAngle()) + "}";
					} else {
						stateValue = "{\"Angle\": " + Math.toDegrees(c.getRotationAngle()) + "}";
					}
				} else if (lastEdit instanceof MoveBuildingCommand) {
					MoveBuildingCommand c = (MoveBuildingCommand) lastEdit;
					Foundation f = c.getFoundation();
					Vector3 d = c.getDisplacement();
					String s = "\"(" + d.getX() + ", " + d.getY() + ")\"";
					if (f != null) {
						stateValue = "{\"Building\": " + f.getId() + ", \"Displacement\": " + s + "}";
					} else {
						stateValue = "{\"Displacement\": " + s + "}";
					}
				}

				// boolean switches
				else if (lastEdit instanceof AnimateSunCommand) {
					stateValue = "" + ((AnimateSunCommand) lastEdit).getNewValue();
				} else if (lastEdit instanceof ShowShadowCommand) {
					stateValue = "" + ((ShowShadowCommand) lastEdit).getNewValue();
				} else if (lastEdit instanceof ShowHeatFluxCommand) {
					stateValue = "" + ((ShowHeatFluxCommand) lastEdit).getNewValue();
				} else if (lastEdit instanceof SpinViewCommand) {
					stateValue = "" + ((SpinViewCommand) lastEdit).getNewValue();
				} else if (lastEdit instanceof ShowAxesCommand) {
					stateValue = "" + ((ShowAxesCommand) lastEdit).getNewValue();
				} else if (lastEdit instanceof ShowAnnotationCommand) {
					stateValue = "" + ((ShowAnnotationCommand) lastEdit).getNewValue();
				} else if (lastEdit instanceof ShowHeliodonCommand) {
					stateValue = "" + ((ShowHeliodonCommand) lastEdit).getNewValue();
				} else if (lastEdit instanceof TopViewCommand) {
					stateValue = "" + (SceneManager.getInstance().getViewMode() == ViewMode.TOP_VIEW);
				}

				// value changes
				else if (lastEdit instanceof ChangeBackgroundAlbedoCommand) {
					ChangeBackgroundAlbedoCommand c = (ChangeBackgroundAlbedoCommand) lastEdit;
					stateValue = "{\"Old Value\": " + c.getOldValue() + ", \"New Value\": " + Scene.getInstance().getGround().getAlbedo() + "}";
				} else if (lastEdit instanceof ChangeGroundThermalDiffusivityCommand) {
					ChangeGroundThermalDiffusivityCommand c = (ChangeGroundThermalDiffusivityCommand) lastEdit;
					stateValue = "{\"Old Value\": " + c.getOldValue() + ", \"New Value\": " + Scene.getInstance().getGround().getThermalDiffusivity() + "}";
				} else if (lastEdit instanceof ChangeSolarHeatMapColorContrastCommand) {
					ChangeSolarHeatMapColorContrastCommand c = (ChangeSolarHeatMapColorContrastCommand) lastEdit;
					stateValue = "{\"Old Value\": " + c.getOldValue() + ", \"New Value\": " + Scene.getInstance().getSolarHeatMapColorContrast() + "}";
				} else if (lastEdit instanceof ChangeLatitudeCommand) {
					ChangeLatitudeCommand c = (ChangeLatitudeCommand) lastEdit;
					stateValue = "{\"Old Value\": " + Math.round(Math.toDegrees(c.getOldValue())) + ", \"New Value\": " + Math.round(Math.toDegrees(Heliodon.getInstance().getLatitude())) + "}";
				} else if (lastEdit instanceof ChangeCityCommand) {
					ChangeCityCommand c = (ChangeCityCommand) lastEdit;
					stateValue = "{\"Old City\": \"" + c.getOldValue() + "\", \"New City\": \"" + Scene.getInstance().getCity() + "\"}";
				} else if (lastEdit instanceof ChangeDateCommand) {
					ChangeDateCommand c = (ChangeDateCommand) lastEdit;
					Calendar calendar = new GregorianCalendar();
					calendar.setTime(c.getOldDate());
					stateValue = "{\"Old Date\": \"" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "\"";
					calendar.setTime(Scene.getInstance().getDate());
					stateValue += ", \"New Date\": \"" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "\"}";
				} else if (lastEdit instanceof ChangeTimeCommand) {
					ChangeTimeCommand c = (ChangeTimeCommand) lastEdit;
					Calendar cal0 = new GregorianCalendar();
					cal0.setTime(c.getOldTime());
					stateValue = "{\"Old Time\": \"" + (cal0.get(Calendar.HOUR_OF_DAY)) + ":" + cal0.get(Calendar.MINUTE) + "\"";
					Calendar cal1 = Heliodon.getInstance().getCalender();
					stateValue += ", \"New Time\": \"" + (cal1.get(Calendar.HOUR_OF_DAY)) + ":" + cal1.get(Calendar.MINUTE) + "\"}";
				} else if (lastEdit instanceof ChangeTextureCommand) {
					stateValue = "{\"Old Value\": ";
					TextureMode textureMode = ((ChangeTextureCommand) lastEdit).getOldValue();
					if (textureMode == TextureMode.Full) {
						stateValue += "\"Full\"";
					} else if (textureMode == TextureMode.Simple) {
						stateValue += "\"Simple\"";
					} else if (textureMode == TextureMode.None) {
						stateValue += "\"None\"";
					}
					stateValue += ", \"New Value\": ";
					textureMode = Scene.getInstance().getTextureMode();
					if (textureMode == TextureMode.Full) {
						stateValue += "\"Full\"";
					} else if (textureMode == TextureMode.Simple) {
						stateValue += "\"Simple\"";
					} else if (textureMode == TextureMode.None) {
						stateValue += "\"None\"";
					}
					stateValue += "}";
				}

				// building properties
				else if (lastEdit instanceof ChangeRoofOverhangCommand) {
					ChangeRoofOverhangCommand c = (ChangeRoofOverhangCommand) lastEdit;
					Roof r = c.getRoof();
					stateValue = "{\"Building\": " + r.getTopContainer().getId() + ", \"ID\": " + r.getId();
					stateValue += ", \"Old Value\": " + c.getOldValue() * Scene.getInstance().getAnnotationScale();
					stateValue += ", \"New Value\": " + r.getOverhangLength() * Scene.getInstance().getAnnotationScale() + "}";
				} else if (lastEdit instanceof AdjustThermostatCommand) {
					Foundation foundation = ((AdjustThermostatCommand) lastEdit).getFoundation();
					stateValue = "{\"Building\":" + foundation.getId() + "}";
				}

				// colors
				else if (lastEdit instanceof ChangePartColorCommand) {
					ChangePartColorCommand c = (ChangePartColorCommand) lastEdit;
					HousePart p = c.getPart();
					Foundation foundation = p instanceof Foundation ? (Foundation) p : p.getTopContainer();
					stateValue = "{\"Building\": " + foundation.getId() + ", \"ID\": " + p.getId();
					stateValue += ", \"Type\": \"" + p.getClass().getSimpleName() + "\"";
					stateValue += ", \"Old Color\": \"" + Util.toString(c.getOldColor()) + "\", \"New Color\": \"" + Util.toString(p.getColor()) + "\"}";
				} else if (lastEdit instanceof ChangeContainerWindowColorCommand) {
					ChangeContainerWindowColorCommand cmd = (ChangeContainerWindowColorCommand) lastEdit;
					HousePart container = cmd.getContainer();
					List<Window> windows = Scene.getInstance().getWindowsOnContainer(container);
					String containerType = container instanceof Wall ? "Wall" : "Roof";
					stateValue = "{\"" + containerType + "\":" + container.getId() + ", \"New Color\": \"" + Util.toString(windows.get(0).getColor()) + "\"}";
				} else if (lastEdit instanceof ChangeBuildingColorCommand) {
					ChangeBuildingColorCommand c = (ChangeBuildingColorCommand) lastEdit;
					HousePart p = c.getPart();
					String s = "{\"Building\":" + c.getFoundation().getId();
					s += ", \"Type\": \"" + p.getClass().getSimpleName() + "\"";
					s += ", \"New Color\": \"" + Util.toString(p.getColor()) + "\"}";
					stateValue = s;
				}

				// u-values and thermal masses
				else if (lastEdit instanceof ChangePartUValueCommand) {
					ChangePartUValueCommand c = (ChangePartUValueCommand) lastEdit;
					HousePart p = c.getPart();
					if (p instanceof Thermalizable) {
						Foundation foundation = p instanceof Foundation ? (Foundation) p : p.getTopContainer();
						stateValue = "{\"Building\":" + foundation.getId() + ", \"ID\":" + p.getId();
						stateValue += ", \"Type\": \"" + p.getClass().getSimpleName() + "\"";
						stateValue += ", \"Old Value\": " + c.getOldValue();
						stateValue += ", \"New Value\": " + ((Thermalizable) p).getUValue() + "}";
					}
				} else if (lastEdit instanceof ChangeBuildingUValueCommand) {
					ChangeBuildingUValueCommand c = (ChangeBuildingUValueCommand) lastEdit;
					HousePart p = c.getPart();
					if (p instanceof Thermalizable) {
						Foundation foundation = p instanceof Foundation ? (Foundation) p : p.getTopContainer();
						stateValue = "{\"Building\":" + foundation.getId();
						stateValue += ", \"Type\": \"" + p.getClass().getSimpleName() + "\"";
						stateValue += ", \"New Value\": " + ((Thermalizable) p).getUValue() + "}";
					}
				} else if (lastEdit instanceof ChangeVolumetricHeatCapacityCommand) {
					ChangeVolumetricHeatCapacityCommand c = (ChangeVolumetricHeatCapacityCommand) lastEdit;
					HousePart p = c.getPart();
					if (p instanceof Thermalizable) {
						Foundation foundation = p instanceof Foundation ? (Foundation) p : p.getTopContainer();
						stateValue = "{\"Building\":" + foundation.getId() + ", \"ID\":" + p.getId();
						stateValue += ", \"Type\": \"" + p.getClass().getSimpleName() + "\"";
						stateValue += ", \"Old Value\": " + c.getOldValue();
						stateValue += ", \"New Value\": " + ((Thermalizable) p).getVolumetricHeatCapacity() + "}";
					}
				}

				// solar panel properties
				else if (lastEdit instanceof ChangeSolarPanelEfficiencyCommand) {
					ChangeSolarPanelEfficiencyCommand c = (ChangeSolarPanelEfficiencyCommand) lastEdit;
					SolarPanel sp = c.getSolarPanel();
					stateValue = "{\"Building\": " + sp.getTopContainer().getId() + ", \"ID\": " + sp.getId();
					stateValue += ", \"Old Value\": " + c.getOldValue() + ", \"New Value\": " + sp.getEfficiency() + "}";
				} else if (lastEdit instanceof ChangeBuildingSolarPanelEfficiencyCommand) {
					Foundation foundation = ((ChangeBuildingSolarPanelEfficiencyCommand) lastEdit).getFoundation();
					List<SolarPanel> solarPanels = Scene.getInstance().getSolarPanelsOfBuilding(foundation);
					stateValue = "{\"Building\": " + foundation.getId() + ", \"New Value\": " + (solarPanels.isEmpty() ? -1 : solarPanels.get(0).getEfficiency()) + "}";
				}

				// window properties
				else if (lastEdit instanceof ChangeWindowShgcCommand) {
					ChangeWindowShgcCommand c = (ChangeWindowShgcCommand) lastEdit;
					Window w = c.getWindow();
					stateValue = "{\"Building\": " + w.getTopContainer().getId() + ", \"ID\": " + w.getId();
					stateValue += ", \"Old Value\": " + c.getOldValue() + ", \"New Value\": " + w.getSolarHeatGainCoefficient() + "}";
				} else if (lastEdit instanceof ChangeContainerWindowShgcCommand) {
					ChangeContainerWindowShgcCommand c = (ChangeContainerWindowShgcCommand) lastEdit;
					HousePart container = c.getContainer();
					List<Window> windows = Scene.getInstance().getWindowsOnContainer(container);
					String containerType = container instanceof Wall ? "Wall" : "Roof";
					stateValue = "{\"" + containerType + "\": " + container.getId() + ", \"New Value\": " + (windows.isEmpty() ? -1 : windows.get(0).getSolarHeatGainCoefficient()) + "}";
				} else if (lastEdit instanceof ChangeBuildingWindowShgcCommand) {
					ChangeBuildingWindowShgcCommand c = (ChangeBuildingWindowShgcCommand) lastEdit;
					Foundation foundation = c.getFoundation();
					List<Window> windows = Scene.getInstance().getWindowsOfBuilding(foundation);
					stateValue = "{\"Building\": " + foundation.getId() + ", \"New Value\": " + (windows.isEmpty() ? -1 : windows.get(0).getSolarHeatGainCoefficient()) + "}";
				}

			}

			line += ", \"" + action + "\": ";
			if (actedPart != null) {
				line += LoggerUtil.getInfo(actedPart);
			} else if (stateValue != null) {
				line += stateValue;
			} else {
				line += "null";
			}

			lastAction = action;

		}

		// analysis requesters

		if (analysisRequester != null) {
			HousePart analyzedPart = SceneManager.getInstance().getSelectedPart();
			line += ", \"" + analysisRequester.getClass().getSimpleName() + "\": ";
			if (analysisRequester instanceof AnnualSensorData) {
				line += ((AnnualSensorData) analysisRequester).toJson();
			} else if (analysisRequester instanceof DailySensorData) {
				line += ((DailySensorData) analysisRequester).toJson();
			} else if (analysisRequester instanceof DailyEnvironmentalTemperature) {
				line += ((DailyEnvironmentalTemperature) analysisRequester).toJson();
			} else if (analysisRequester instanceof AnnualEnvironmentalTemperature) {
				line += ((AnnualEnvironmentalTemperature) analysisRequester).toJson();
			} else {
				if (analyzedPart != null && !(analyzedPart instanceof Tree) && !(analyzedPart instanceof Human)) { // if something analyzable is selected
					if (analysisRequester instanceof EnergyDailyAnalysis) {
						line += ((EnergyDailyAnalysis) analysisRequester).toJson();
					} else if (analysisRequester instanceof DailyEnergyGraph) {
						line += ((DailyEnergyGraph) analysisRequester).toJson();
						if (SceneManager.getInstance().areBuildingLabelsVisible()) {
							String result = Building.getBuildingSolarPotentials();
							if (result != null) {
								line += ", \"Solar Potential\": " + result;
							}
						}
					} else if (analysisRequester instanceof EnergyAnnualAnalysis) {
						line += ((EnergyAnnualAnalysis) analysisRequester).toJson();
					} else if (analysisRequester instanceof EnergyAngularAnalysis) {
						line += ((EnergyAngularAnalysis) analysisRequester).toJson();
					} else if (analysisRequester instanceof Cost) {
						line += ((Cost) analysisRequester).toJson();
					}
				} else {
					if (analysisRequester instanceof Cost) {
						line += ((Cost) analysisRequester).toJson();
					} else if (analysisRequester instanceof DailyEnergyGraph) {
						line += ((DailyEnergyGraph) analysisRequester).toJson();
						if (SceneManager.getInstance().areBuildingLabelsVisible()) {
							String result = Building.getBuildingSolarPotentials();
							if (result != null) {
								line += ", \"Solar Potential\": " + result;
							}
						}
					}
				}
				if (analysisRequester instanceof SolarDailyAnalysis) {
					line += ((SolarDailyAnalysis) analysisRequester).toJson();
				} else if (analysisRequester instanceof SolarAnnualAnalysis) {
					line += ((SolarAnnualAnalysis) analysisRequester).toJson();
				}
			}
		}

		if (firstRecord) {
			firstRecord = false;
		} else {
			writer.write(",\n");
		}
		writer.write("{\"Timestamp\": \"" + timestamp + "\", " + line + "}");
		writer.flush();

		lastTime = time;

	}

	public void close() {
		if (writer != null) {
			writer.write("]\n}");
			writer.close();
		}
	}

	public void logAction() {
		action = SceneManager.getInstance().getUndoManager().getPresentationName();
		try {
			record();
		} catch (Throwable t) {
			Util.reportError(t);
		} finally {
			action = null;
		}
	}

	public void logAnalysis(Object x) {
		analysisRequester = x;
		try {
			record();
		} catch (Throwable t) {
			Util.reportError(t);
		} finally {
			analysisRequester = null;
		}
	}

	public void logUndo() {
		action = "Undo";
		record();
		action = null;
	}

	public void logRedo() {
		action = "Redo";
		record();
		action = null;
	}

	public void logSave() {
		action = "Save";
		record();
		action = null;
	}

	public void logNote() {
		action = "Note";
		record();
		action = null;
	}

	public void logCamera(String mode) {
		if (!isCameraChanged())
			return;
		action = "Camera";
		cameraMode = mode;
		record();
		action = null;
	}

	private boolean isCameraChanged() {
		final Camera camera = SceneManager.getInstance().getCamera();
		final ReadOnlyVector3 location = camera.getLocation();
		final ReadOnlyVector3 direction = camera.getDirection();
		cameraPosition = "\"Position\": {\"x\": " + LoggerUtil.FORMAT.format(location.getX());
		cameraPosition += ", \"y\": " + LoggerUtil.FORMAT.format(location.getY());
		cameraPosition += ", \"z\": " + LoggerUtil.FORMAT.format(location.getZ());
		cameraPosition += "}, \"Direction\": {\"x\": " + LoggerUtil.FORMAT.format(direction.getX());
		cameraPosition += ", \"y\": " + LoggerUtil.FORMAT.format(direction.getY());
		cameraPosition += ", \"z\": " + LoggerUtil.FORMAT.format(direction.getZ()) + "}";
		if (cameraPosition.equals(lastCameraPosition))
			return false;
		lastCameraPosition = cameraPosition;
		return true;
	}

	public void logGraphTab(String graphTabName) {
		action = "Graph Tab";
		this.graphTabName = graphTabName;
		record();
		action = null;
	}

	public void logClearGraphData(String graphName) {
		action = "Clear Graph Data";
		this.graphName = graphName;
		record();
		action = null;
	}

	public void logShowCurve(String graphName, String curveName, boolean curveShown) {
		action = "Show Curve";
		this.graphName = graphName;
		this.curveName = curveName;
		this.curveShown = curveShown;
		record();
		action = null;
	}

	public void logShowRun(String graphName, String runID, boolean runShown) {
		action = "Show Run";
		this.graphName = graphName;
		this.runID = runID;
		this.runShown = runShown;
		record();
		action = null;
	}

	public void logAdjustThermostatButton() {
		action = "Adjust Thermostat";
		record();
		action = null;
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
	}

}
