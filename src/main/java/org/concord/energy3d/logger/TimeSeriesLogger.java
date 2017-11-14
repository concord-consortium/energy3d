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

import org.concord.energy3d.gui.BuildingDailyEnergyGraph;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Building;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarCollector;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Thermal;
import org.concord.energy3d.model.Trackable;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.ViewMode;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.AnnualEnvironmentalTemperature;
import org.concord.energy3d.simulation.AnnualSensorData;
import org.concord.energy3d.simulation.DailyEnvironmentalTemperature;
import org.concord.energy3d.simulation.DailySensorData;
import org.concord.energy3d.simulation.EnergyAngularAnalysis;
import org.concord.energy3d.simulation.EnergyAnnualAnalysis;
import org.concord.energy3d.simulation.EnergyDailyAnalysis;
import org.concord.energy3d.simulation.GroupAnnualAnalysis;
import org.concord.energy3d.simulation.GroupDailyAnalysis;
import org.concord.energy3d.simulation.ProjectCost;
import org.concord.energy3d.simulation.PvAnnualAnalysis;
import org.concord.energy3d.simulation.PvDailyAnalysis;
import org.concord.energy3d.undo.*;
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
		if (url == null) {
			return;
		}

		/* write the header */

		String filename = url == null ? null : new File(url.getFile()).getName();
		if (Scene.isInternalFile()) {
			filename = "@" + filename;
		}
		final Date time = Calendar.getInstance().getTime();
		final String timestamp = TIME_FORMAT.format(time);
		String line = "";
		if (Scene.getInstance().getProjectName() != null && !Scene.getInstance().getProjectName().trim().equals("")) {
			line += "\"Project\": \"" + Scene.getInstance().getProjectName() + "\", ";
		}
		line += "\"File\": \"" + filename + "\"";

		/* actions registered with the Undo Manager */

		if (action != null) {

			if ((lastTime != null && Math.abs(time.getTime() - lastTime.getTime()) < MINIMUM_INTERVAL) && action.equals(lastAction)) {
				return; // don't log too frequently for the same type of action
			}

			HousePart actedPart = null;
			String stateValue = null;

			// special treatments
			if (action.equals("Undo")) {
				String s = SceneManager.getInstance().getUndoManager().getRedoPresentationName();
				if (s.length() >= 4) {
					s = s.substring(4, s.length()).trim();
				}
				stateValue = "\"" + s + "\"";
			} else if (action.equals("Redo")) {
				String s = SceneManager.getInstance().getUndoManager().getUndoPresentationName();
				if (s.length() >= 4) {
					s = s.substring(4, s.length()).trim();
				}
				stateValue = "\"" + s + "\"";
			} else if (action.equals("Save")) {
				stateValue = "\"" + Scene.getURL().toString() + "*\""; // append * at the end so that the ng3 suffix is not interpreted as a delimiter
			} else if (action.equals("Note")) {
				final String s = MainPanel.getInstance().getNoteString();
				if (s.length() > 0) {
					stateValue = "\"" + s + "\"";
					MainPanel.getInstance().setNoteString("");
				}
			} else if (action.equals("Camera")) {
				stateValue = "{" + cameraPosition + ", \"Mode\": \"" + cameraMode + "\"}";
			} else if (action.equals("Graph Tab")) {
				stateValue = "\"" + graphTabName + "\"";
			} else if (action.equals("Adjust Thermostat")) {
				final HousePart p = SceneManager.getInstance().getSelectedPart();
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

				/* everything else */

				final UndoableEdit lastEdit = SceneManager.getInstance().getUndoManager().lastEdit();

				/* add, edit, or remove parts */

				if (lastEdit instanceof AddPartCommand) {
					actedPart = ((AddPartCommand) lastEdit).getPart();
				} else if (lastEdit instanceof AddMultiplePartsCommand) {
					final AddMultiplePartsCommand c = (AddMultiplePartsCommand) lastEdit;
					if (c.getURL() != null) {
						stateValue = "{\"Import\": \"" + c.getURL() + "\"}";
					}
				} else if (lastEdit instanceof AddArrayCommand) {
					final AddArrayCommand c = (AddArrayCommand) lastEdit;
					final HousePart p = c.getParent();
					if (p instanceof Foundation) {
						final Foundation f = (Foundation) p;
						stateValue = "{\"Foundation\": " + f.getId() + ", \"Old Array Size\": " + c.getOldArray().size() + ", \"New Array Size\": " + f.countParts(c.getTypes()) + "}";
					} else if (p instanceof Rack) {
						final Foundation f = p.getTopContainer();
						stateValue = "{\"Foundation\": " + f.getId() + ", \"Rack\": " + p.getId() + ", \"Old Array Size\": " + c.getOldArray().size() + ", \"New Array Size\": " + p.getChildren().size() + "}";
					}
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
				} else if (lastEdit instanceof RemoveMultiplePartsCommand) {
					final Foundation f = ((RemoveMultiplePartsCommand) lastEdit).getFoundation();
					if (f != null) {
						stateValue = "{\"Building\": " + f.getId() + "}";
					}
				} else if (lastEdit instanceof RotateBuildingCommand) {
					final RotateBuildingCommand c = (RotateBuildingCommand) lastEdit;
					final Foundation f = c.getFoundation();
					if (f != null) {
						stateValue = "{\"Building\": " + f.getId() + ", \"Angle\": " + Math.toDegrees(c.getRotationAngle()) + "}";
					} else {
						stateValue = "{\"Angle\": " + Math.toDegrees(c.getRotationAngle()) + "}";
					}
				} else if (lastEdit instanceof RescaleBuildingCommand) {
					final RescaleBuildingCommand c = (RescaleBuildingCommand) lastEdit;
					final Foundation f = c.getFoundation();
					if (f != null) {
						stateValue = "{\"Building\": " + f.getId();
						stateValue += ", \"Old X Length\": " + c.getOldXLength() + ", \"New X Length\": " + c.getNewXLength();
						stateValue += ", \"Old Y Length\": " + c.getOldYLength() + ", \"New Y Length\": " + c.getNewYLength();
						stateValue += ", \"Old Z Length\": " + c.getOldZLength() + ", \"New Z Length\": " + c.getNewZLength();
						stateValue += "}";
					}
				} else if (lastEdit instanceof MovePartCommand) {
					final MovePartCommand c = (MovePartCommand) lastEdit;
					final HousePart p = c.getPart();
					final Vector3 d = c.getDisplacement();
					final String s = "\"(" + d.getX() + ", " + d.getY() + ")\"";
					if (p != null) {
						stateValue = "{\"Part\": " + p.getId() + ", \"Displacement\": " + s + "}";
					} else {
						stateValue = "{\"Displacement\": " + s + "}";
					}
				} else if (lastEdit instanceof DeleteUtilityBillCommand) {
					final DeleteUtilityBillCommand c = (DeleteUtilityBillCommand) lastEdit;
					final Foundation f = c.getFoundation();
					stateValue = "{\"Building\": " + f.getId() + "}";
				}

				/* boolean switches */

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
				} else if (lastEdit instanceof ZoomCommand) {
					stateValue = "" + ((ZoomCommand) lastEdit).getValue();
				}

				/* value changes */

				else if (lastEdit instanceof RescaleCommand) {
					final RescaleCommand c = (RescaleCommand) lastEdit;
					stateValue = "{\"Old Value\": " + c.getOldValue() + ", \"New Value\": " + Scene.getInstance().getAnnotationScale() + "}";
				} else if (lastEdit instanceof ChangeBackgroundAlbedoCommand) {
					final ChangeBackgroundAlbedoCommand c = (ChangeBackgroundAlbedoCommand) lastEdit;
					stateValue = "{\"Old Value\": " + c.getOldValue() + ", \"New Value\": " + Scene.getInstance().getGround().getAlbedo() + "}";
				} else if (lastEdit instanceof ChangeGroundThermalDiffusivityCommand) {
					final ChangeGroundThermalDiffusivityCommand c = (ChangeGroundThermalDiffusivityCommand) lastEdit;
					stateValue = "{\"Old Value\": " + c.getOldValue() + ", \"New Value\": " + Scene.getInstance().getGround().getThermalDiffusivity() + "}";
				} else if (lastEdit instanceof ChangeAtmosphericDustLossCommand) {
					final ChangeAtmosphericDustLossCommand c = (ChangeAtmosphericDustLossCommand) lastEdit;
					final double[] oldValues = c.getOldValue();
					String oldValueString = "\"";
					for (final double x : oldValues) {
						oldValueString += x + " ";
					}
					oldValueString.trim();
					oldValueString += "\"";
					String newValueString = "\"";
					for (int i = 0; i < 12; i++) {
						newValueString += Scene.getInstance().getAtmosphere().getDustLoss(i) + " ";
					}
					newValueString.trim();
					newValueString += "\"";
					stateValue = "{\"Old Values\": " + oldValueString + ", \"New Values\": " + newValueString + "}";
				} else if (lastEdit instanceof ChangeSnowReflectionFactorCommand) {
					final ChangeSnowReflectionFactorCommand c = (ChangeSnowReflectionFactorCommand) lastEdit;
					final double[] oldValues = c.getOldValue();
					String oldValueString = "\"";
					for (final double x : oldValues) {
						oldValueString += x + " ";
					}
					oldValueString.trim();
					oldValueString += "\"";
					String newValueString = "\"";
					for (int i = 0; i < 12; i++) {
						newValueString += Scene.getInstance().getGround().getSnowReflectionFactor(i) + " ";
					}
					newValueString.trim();
					newValueString += "\"";
					stateValue = "{\"Old Values\": " + oldValueString + ", \"New Values\": " + newValueString + "}";
				} else if (lastEdit instanceof ChangeSolarHeatMapColorContrastCommand) {
					final ChangeSolarHeatMapColorContrastCommand c = (ChangeSolarHeatMapColorContrastCommand) lastEdit;
					stateValue = "{\"Old Value\": " + c.getOldValue() + ", \"New Value\": " + Scene.getInstance().getSolarHeatMapColorContrast() + "}";
				} else if (lastEdit instanceof ChangeLatitudeCommand) {
					final ChangeLatitudeCommand c = (ChangeLatitudeCommand) lastEdit;
					stateValue = "{\"Old Value\": " + Math.round(Math.toDegrees(c.getOldValue())) + ", \"New Value\": " + Math.round(Math.toDegrees(Heliodon.getInstance().getLatitude())) + "}";
				} else if (lastEdit instanceof ChangeCityCommand) {
					final ChangeCityCommand c = (ChangeCityCommand) lastEdit;
					stateValue = "{\"Old City\": \"" + c.getOldValue() + "\", \"New City\": \"" + Scene.getInstance().getCity() + "\"}";
				} else if (lastEdit instanceof ChangeDateCommand) {
					final ChangeDateCommand c = (ChangeDateCommand) lastEdit;
					final Calendar calendar = new GregorianCalendar();
					calendar.setTime(c.getOldDate());
					stateValue = "{\"Old Date\": \"" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "\"";
					calendar.setTime(Scene.getInstance().getDate());
					stateValue += ", \"New Date\": \"" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.DAY_OF_MONTH) + "\"}";
				} else if (lastEdit instanceof ChangeTimeCommand) {
					final ChangeTimeCommand c = (ChangeTimeCommand) lastEdit;
					final Calendar cal0 = new GregorianCalendar();
					cal0.setTime(c.getOldTime());
					stateValue = "{\"Old Time\": \"" + (cal0.get(Calendar.HOUR_OF_DAY)) + ":" + cal0.get(Calendar.MINUTE) + "\"";
					final Calendar cal1 = Heliodon.getInstance().getCalendar();
					stateValue += ", \"New Time\": \"" + (cal1.get(Calendar.HOUR_OF_DAY)) + ":" + cal1.get(Calendar.MINUTE) + "\"}";
				} else if (lastEdit instanceof ChangeTimeAndDateWithHeliodonCommand) {
					final ChangeTimeAndDateWithHeliodonCommand c = (ChangeTimeAndDateWithHeliodonCommand) lastEdit;
					final Calendar oldCal = new GregorianCalendar();
					oldCal.setTime(c.getOldTime());
					final Calendar newCal = Heliodon.getInstance().getCalendar();
					stateValue = "{\"Old Time\": \"" + (oldCal.get(Calendar.HOUR_OF_DAY)) + ":" + oldCal.get(Calendar.MINUTE) + "\"";
					stateValue += ", \"New Time\": \"" + (newCal.get(Calendar.HOUR_OF_DAY)) + ":" + newCal.get(Calendar.MINUTE) + "\"";
					stateValue += ", \"Old Date\": \"" + (oldCal.get(Calendar.MONTH) + 1) + "/" + oldCal.get(Calendar.DAY_OF_MONTH) + "\"";
					stateValue += ", \"New Date\": \"" + (newCal.get(Calendar.MONTH) + 1) + "/" + newCal.get(Calendar.DAY_OF_MONTH) + "\"}";
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
				} else if (lastEdit instanceof ChangeThemeCommand) {
					stateValue = "{\"Old Value\": " + ((ChangeThemeCommand) lastEdit).getOldValue() + ", \"New Value\": " + Scene.getInstance().getTheme() + "}";
				}

				/* building properties */

				else if (lastEdit instanceof ChangeRoofOverhangCommand) {
					final ChangeRoofOverhangCommand c = (ChangeRoofOverhangCommand) lastEdit;
					final Roof r = c.getRoof();
					stateValue = "{\"Building\": " + r.getTopContainer().getId() + ", \"ID\": " + r.getId();
					stateValue += ", \"Old Value\": " + c.getOldValue() * Scene.getInstance().getAnnotationScale();
					stateValue += ", \"New Value\": " + r.getOverhangLength() * Scene.getInstance().getAnnotationScale() + "}";
				} else if (lastEdit instanceof ChangeFoundationSizeCommand) {
					final ChangeFoundationSizeCommand c = (ChangeFoundationSizeCommand) lastEdit;
					final Foundation f = c.getFoundation();
					stateValue = "{\"Foundation\": " + f.getId();
					stateValue += ", \"Old Length\": " + c.getOldLength();
					stateValue += ", \"New Length\": " + c.getNewLength();
					stateValue += ", \"Old Width\": " + c.getOldWidth();
					stateValue += ", \"New Width\": " + c.getNewWidth();
					stateValue += ", \"Old Height\": " + c.getOldHeight();
					stateValue += ", \"New Height\": " + c.getNewHeight() + "}";
				} else if (lastEdit instanceof AdjustThermostatCommand) {
					final Foundation f = ((AdjustThermostatCommand) lastEdit).getFoundation();
					stateValue = "{\"Building\":" + f.getId() + "}";
				}

				/* colors */

				else if (lastEdit instanceof ChangePartColorCommand) {
					final ChangePartColorCommand c = (ChangePartColorCommand) lastEdit;
					final HousePart p = c.getPart();
					final Foundation f = p instanceof Foundation ? (Foundation) p : p.getTopContainer();
					stateValue = "{\"Building\": " + f.getId() + ", \"ID\": " + p.getId();
					stateValue += ", \"Type\": \"" + p.getClass().getSimpleName() + "\"";
					stateValue += ", \"Old Color\": \"" + Util.toString(c.getOldColor()) + "\", \"New Color\": \"" + Util.toString(p.getColor()) + "\"}";
				} else if (lastEdit instanceof ChangeContainerWindowColorCommand) {
					final ChangeContainerWindowColorCommand cmd = (ChangeContainerWindowColorCommand) lastEdit;
					final HousePart container = cmd.getContainer();
					final List<Window> windows = Scene.getInstance().getWindowsOnContainer(container);
					final String containerType = container instanceof Wall ? "Wall" : "Roof";
					stateValue = "{\"" + containerType + "\":" + container.getId() + ", \"New Color\": \"" + Util.toString(windows.get(0).getColor()) + "\"}";
				} else if (lastEdit instanceof ChangeBuildingColorCommand) {
					final ChangeBuildingColorCommand c = (ChangeBuildingColorCommand) lastEdit;
					final HousePart p = c.getPart();
					String s = "{\"Building\":" + c.getFoundation().getId();
					s += ", \"Type\": \"" + p.getClass().getSimpleName() + "\"";
					s += ", \"New Color\": \"" + Util.toString(p.getColor()) + "\"}";
					stateValue = s;
				} else if (lastEdit instanceof ChangeLandColorCommand) {
					final ChangeLandColorCommand c = (ChangeLandColorCommand) lastEdit;
					stateValue = "{\"Old Color\": \"" + Util.toString(c.getOldColor()) + "\", \"New Color\": \"" + Util.toString(Scene.getInstance().getLandColor()) + "\"}";
				}

				/* u-values and thermal masses */

				else if (lastEdit instanceof ChangePartUValueCommand) {
					final ChangePartUValueCommand c = (ChangePartUValueCommand) lastEdit;
					final HousePart p = c.getPart();
					if (p instanceof Thermal) {
						final Foundation f = p instanceof Foundation ? (Foundation) p : p.getTopContainer();
						stateValue = "{\"Building\":" + f.getId() + ", \"ID\":" + p.getId();
						stateValue += ", \"Type\": \"" + p.getClass().getSimpleName() + "\"";
						stateValue += ", \"Old Value\": " + c.getOldValue();
						stateValue += ", \"New Value\": " + ((Thermal) p).getUValue() + "}";
					}
				} else if (lastEdit instanceof ChangeBuildingUValueCommand) {
					final ChangeBuildingUValueCommand c = (ChangeBuildingUValueCommand) lastEdit;
					final HousePart p = c.getPart();
					if (p instanceof Thermal) {
						final Foundation f = p instanceof Foundation ? (Foundation) p : p.getTopContainer();
						stateValue = "{\"Building\":" + f.getId();
						stateValue += ", \"Type\": \"" + p.getClass().getSimpleName() + "\"";
						stateValue += ", \"New Value\": " + ((Thermal) p).getUValue() + "}";
					}
				} else if (lastEdit instanceof ChangeVolumetricHeatCapacityCommand) {
					final ChangeVolumetricHeatCapacityCommand c = (ChangeVolumetricHeatCapacityCommand) lastEdit;
					final HousePart p = c.getPart();
					if (p instanceof Thermal) {
						final Foundation f = p instanceof Foundation ? (Foundation) p : p.getTopContainer();
						stateValue = "{\"Building\":" + f.getId() + ", \"ID\":" + p.getId();
						stateValue += ", \"Type\": \"" + p.getClass().getSimpleName() + "\"";
						stateValue += ", \"Old Value\": " + c.getOldValue();
						stateValue += ", \"New Value\": " + ((Thermal) p).getVolumetricHeatCapacity() + "}";
					}
				}

				/* common actions for tilt, azimuth, and base */

				else if (lastEdit instanceof ChangeTiltAngleCommand) {
					final ChangeTiltAngleCommand c = (ChangeTiltAngleCommand) lastEdit;
					final HousePart p = c.getPart();
					stateValue = "{\"Foundation\": " + p.getTopContainer().getId() + ", \"ID\": " + p.getId() + ", \"Old Value\": " + c.getOldValue() + ", \"New Value\": " + c.getNewValue() + "}";
				} else if (lastEdit instanceof ChangeAzimuthCommand) {
					final ChangeAzimuthCommand c = (ChangeAzimuthCommand) lastEdit;
					final HousePart p = c.getPart();
					stateValue = "{\"Foundation\": " + (p instanceof Foundation ? (Foundation) p : p.getTopContainer()).getId() + ", \"ID\": " + p.getId() + ", \"Old Value\": " + c.getOldValue() + ", \"New Value\": " + c.getNewValue() + "}";
				} else if (lastEdit instanceof ChangeBaseHeightCommand) {
					final ChangeBaseHeightCommand c = (ChangeBaseHeightCommand) lastEdit;
					final SolarCollector s = c.getPart();
					if (s instanceof HousePart) {
						final HousePart p = (HousePart) s;
						stateValue = "{\"Foundation\": " + p.getTopContainer().getId() + ", \"ID\": " + p.getId() + ", \"Old Value\": " + c.getOldValue() + ", \"New Value\": " + c.getNewValue() + "}";
					}
				} else if (lastEdit instanceof SetPartSizeCommand) {
					final SetPartSizeCommand c = (SetPartSizeCommand) lastEdit;
					if (c.getPart() instanceof Mirror) {
						final Mirror m = (Mirror) c.getPart();
						stateValue = "{\"Foundation\": " + m.getTopContainer().getId() + ", \"ID\": " + m.getId();
						stateValue += ", \"Old Width\": " + c.getOldWidth() + ", \"New Width\": " + m.getMirrorWidth();
						stateValue += ", \"Old Height\": " + c.getOldHeight() + ", \"New Height\": " + m.getMirrorHeight() + "}";
					} else if (c.getPart() instanceof ParabolicDish) {
						final ParabolicDish d = (ParabolicDish) c.getPart();
						stateValue = "{\"Foundation\": " + d.getTopContainer().getId() + ", \"ID\": " + d.getId();
						stateValue += ", \"Old Rim Radius\": " + c.getOldWidth() + ", \"New Rim Radius\": " + d.getRimRadius() + "}";
					} else if (c.getPart() instanceof ParabolicTrough) {
						final ParabolicTrough t = (ParabolicTrough) c.getPart();
						stateValue = "{\"Foundation\": " + t.getTopContainer().getId() + ", \"ID\": " + t.getId();
						stateValue += ", \"Old Aperture\": " + c.getOldWidth() + ", \"New Aperture\": " + t.getApertureWidth();
						stateValue += ", \"Old Length\": " + c.getOldHeight() + ", \"New Length\": " + t.getTroughLength();
						stateValue += ", \"Old Module Length\": " + c.getOldModuleLength() + ", \"New Module Length\": " + t.getModuleLength() + "}";
					} else if (c.getPart() instanceof FresnelReflector) {
						final FresnelReflector t = (FresnelReflector) c.getPart();
						stateValue = "{\"Foundation\": " + t.getTopContainer().getId() + ", \"ID\": " + t.getId();
						stateValue += ", \"Old Module Width\": " + c.getOldWidth() + ", \"New Module Width\": " + t.getModuleWidth();
						stateValue += ", \"Old Length\": " + c.getOldHeight() + ", \"New Length\": " + t.getLength();
						stateValue += ", \"Old Module Length\": " + c.getOldModuleLength() + ", \"New Module Length\": " + t.getModuleLength() + "}";
					} else if (c.getPart() instanceof Rack) {
						final Rack r = (Rack) c.getPart();
						stateValue = "{\"Foundation\": " + r.getTopContainer().getId() + ", \"ID\": " + r.getId();
						stateValue += ", \"Old Width\": " + c.getOldWidth() + ", \"New Width\": " + r.getRackWidth();
						stateValue += ", \"Old Height\": " + c.getOldHeight() + ", \"New Height\": " + r.getRackHeight() + "}";
					} else if (c.getPart() instanceof Window) {
						final Window r = (Window) c.getPart();
						stateValue = "{\"Foundation\": " + r.getTopContainer().getId() + ", \"ID\": " + r.getId();
						stateValue += ", \"Old Width\": " + c.getOldWidth() + ", \"New Width\": " + r.getWindowWidth();
						stateValue += ", \"Old Height\": " + c.getOldHeight() + ", \"New Height\": " + r.getWindowHeight() + "}";
					}
				}

				/* solar panel properties */

				else if (lastEdit instanceof ChangeSolarPanelModelCommand) {
					final ChangeSolarPanelModelCommand c = (ChangeSolarPanelModelCommand) lastEdit;
					final SolarPanel sp = c.getSolarPanel();
					stateValue = "{\"Foundation\": " + sp.getTopContainer().getId() + ", \"ID\": " + sp.getId();
					stateValue += ", \"Old Model\": \"" + c.getOldModel().getModel() + "\", \"New Model\": \"" + sp.getPvModuleSpecs().getModel() + "\"}";
				} else if (lastEdit instanceof ChooseSolarPanelSizeCommand) {
					final ChooseSolarPanelSizeCommand c = (ChooseSolarPanelSizeCommand) lastEdit;
					final SolarPanel sp = c.getSolarPanel();
					stateValue = "{\"Foundation\": " + sp.getTopContainer().getId() + ", \"ID\": " + sp.getId();
					stateValue += ", \"Old Width\": " + c.getOldWidth() + ", \"New Width\": " + sp.getPanelWidth();
					stateValue += ", \"Old Height\": " + c.getOldHeight() + ", \"New Height\": " + sp.getPanelHeight() + "}";
				} else if (lastEdit instanceof RotateSolarPanelCommand) {
					final RotateSolarPanelCommand c = (RotateSolarPanelCommand) lastEdit;
					final SolarPanel sp = c.getSolarPanel();
					stateValue = "{\"Foundation\": " + sp.getTopContainer().getId() + ", \"ID\": " + sp.getId() + ", \"New Value\": " + sp.isRotated() + "}";
				}

				else if (lastEdit instanceof ChangeSolarCellPropertiesCommand) {
					final ChangeSolarCellPropertiesCommand c = (ChangeSolarCellPropertiesCommand) lastEdit;
					final SolarPanel sp = c.getSolarPanel();
					stateValue = "{\"Foundation\": " + sp.getTopContainer().getId() + ", \"ID\": " + sp.getId();
					stateValue += ", \"Old Efficiency\": " + c.getOldEfficiency() + ", \"New Efficiency\": " + sp.getCellEfficiency();
					stateValue += ", \"Old Type\": " + c.getOldType() + ", \"New Type\": " + sp.getCellType();
					stateValue += ", \"Old Color\": " + c.getOldColor() + ", \"New Color\": " + sp.getColorOption();
					stateValue += "}";
				} else if (lastEdit instanceof ChangeFoundationSolarCellPropertiesCommand) {
					final Foundation f = ((ChangeFoundationSolarCellPropertiesCommand) lastEdit).getFoundation();
					final List<SolarPanel> solarPanels = f.getSolarPanels();
					if (solarPanels.isEmpty()) {
						stateValue = "{\"Foundation\": " + f.getId() + "}";
					} else {
						final SolarPanel p = solarPanels.get(0);
						stateValue = "{\"Foundation\": " + f.getId() + ", \"New Efficiency\": " + p.getCellEfficiency();
						stateValue += ", \"New Type\": " + p.getCellType();
						stateValue += ", \"New Color\": " + p.getColorOption() + "}";
					}
				} else if (lastEdit instanceof ChangeSolarCellPropertiesForAllCommand) {
					final List<SolarPanel> solarPanels = Scene.getInstance().getAllSolarPanels();
					if (solarPanels.isEmpty()) {
						stateValue = "{}";
					} else {
						final SolarPanel p = solarPanels.get(0);
						stateValue = "{\"New Efficiency\": " + p.getCellEfficiency();
						stateValue += ", \"New Type\": " + p.getCellType();
						stateValue += ", \"New Color\": " + p.getColorOption() + "}";
					}
				}

				else if (lastEdit instanceof SetTemperatureEffectsCommand) {
					final SetTemperatureEffectsCommand c = (SetTemperatureEffectsCommand) lastEdit;
					final SolarPanel sp = c.getSolarPanel();
					stateValue = "{\"Foundation\": " + sp.getTopContainer().getId() + ", \"ID\": " + sp.getId() + ", \"Old Noct\": " + c.getOldNoct() + ", \"New Noct\": " + sp.getNominalOperatingCellTemperature() + ", \"Old Pmax\": " + c.getOldPmax() + ", \"New Pmax\": " + sp.getTemperatureCoefficientPmax() + "}";
				} else if (lastEdit instanceof SetFoundationTemperatureEffectsCommand) {
					final Foundation f = ((SetFoundationTemperatureEffectsCommand) lastEdit).getFoundation();
					final List<SolarPanel> solarPanels = f.getSolarPanels();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Pmax\": " + (solarPanels.isEmpty() ? -1 : solarPanels.get(0).getTemperatureCoefficientPmax()) + "}";
				} else if (lastEdit instanceof SetTemperatrureEffectsForAllCommand) {
					final List<SolarPanel> solarPanels = Scene.getInstance().getAllSolarPanels();
					stateValue = "{\"New Pmax\": " + (solarPanels.isEmpty() ? -1 : solarPanels.get(0).getTemperatureCoefficientPmax()) + "}";
				}

				else if (lastEdit instanceof ChangeInverterEfficiencyCommand) {
					final ChangeInverterEfficiencyCommand c = (ChangeInverterEfficiencyCommand) lastEdit;
					final SolarPanel sp = c.getSolarPanel();
					stateValue = "{\"Foundation\": " + sp.getTopContainer().getId() + ", \"ID\": " + sp.getId() + ", \"Old Value\": " + c.getOldValue() + ", \"New Value\": " + sp.getInverterEfficiency() + "}";
				} else if (lastEdit instanceof ChangeFoundationInverterEfficiencyCommand) {
					final Foundation f = ((ChangeFoundationInverterEfficiencyCommand) lastEdit).getFoundation();
					final List<SolarPanel> solarPanels = f.getSolarPanels();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (solarPanels.isEmpty() ? -1 : solarPanels.get(0).getInverterEfficiency()) + "}";
				} else if (lastEdit instanceof ChangeInverterEfficiencyForAllCommand) {
					final List<SolarPanel> solarPanels = Scene.getInstance().getAllSolarPanels();
					stateValue = "{\"New Value\": " + (solarPanels.isEmpty() ? -1 : solarPanels.get(0).getInverterEfficiency()) + "}";
				}

				else if (lastEdit instanceof SetShadeToleranceCommand) {
					final SetShadeToleranceCommand c = (SetShadeToleranceCommand) lastEdit;
					final SolarPanel sp = c.getSolarPanel();
					stateValue = "{\"Foundation\": " + sp.getTopContainer().getId() + ", \"ID\": " + sp.getId() + ", \"Old Value\": " + c.getOldValue() + ", \"New Value\": " + sp.getShadeTolerance() + "}";
				} else if (lastEdit instanceof SetShadeToleranceForSolarPanelsOnFoundationCommand) {
					final SetShadeToleranceForSolarPanelsOnFoundationCommand c = (SetShadeToleranceForSolarPanelsOnFoundationCommand) lastEdit;
					final Foundation f = c.getFoundation();
					final List<SolarPanel> solarPanels = f.getSolarPanels();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (solarPanels.isEmpty() ? -1 : solarPanels.get(0).getShadeTolerance()) + "}";
				} else if (lastEdit instanceof SetShadeToleranceForAllSolarPanelsCommand) {
					final List<SolarPanel> solarPanels = Scene.getInstance().getAllSolarPanels();
					stateValue = "{\"New Value\": " + (solarPanels.isEmpty() ? -1 : solarPanels.get(0).getShadeTolerance()) + "}";
				}

				else if (lastEdit instanceof ChangeFoundationSolarPanelTiltAngleCommand) {
					final Foundation f = ((ChangeFoundationSolarPanelTiltAngleCommand) lastEdit).getFoundation();
					final List<SolarPanel> solarPanels = f.getSolarPanels();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (solarPanels.isEmpty() ? -1 : solarPanels.get(0).getTiltAngle()) + "}";
				} else if (lastEdit instanceof ChangeTiltAngleForAllSolarPanelsCommand) {
					final List<SolarPanel> solarPanels = Scene.getInstance().getAllSolarPanels();
					stateValue = "{\"New Value\": " + (solarPanels.isEmpty() ? -1 : solarPanels.get(0).getTiltAngle()) + "}";
				}

				else if (lastEdit instanceof ChangeFoundationSolarPanelAzimuthCommand) {
					final Foundation f = ((ChangeFoundationSolarPanelAzimuthCommand) lastEdit).getFoundation();
					final List<SolarPanel> solarPanels = f.getSolarPanels();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (solarPanels.isEmpty() ? -1 : solarPanels.get(0).getRelativeAzimuth()) + "}";
				} else if (lastEdit instanceof ChangeAzimuthForAllSolarPanelsCommand) {
					final List<SolarPanel> solarPanels = Scene.getInstance().getAllSolarPanels();
					stateValue = "{\"New Value\": " + (solarPanels.isEmpty() ? -1 : solarPanels.get(0).getRelativeAzimuth()) + "}";
				}

				else if (lastEdit instanceof ChangeFoundationSolarPanelBaseHeightCommand) {
					final Foundation f = ((ChangeFoundationSolarPanelBaseHeightCommand) lastEdit).getFoundation();
					final List<SolarPanel> solarPanels = f.getSolarPanels();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (solarPanels.isEmpty() ? -1 : solarPanels.get(0).getBaseHeight()) + "}";
				} else if (lastEdit instanceof ChangeBaseHeightForAllSolarPanelsCommand) {
					final List<SolarPanel> solarPanels = Scene.getInstance().getAllSolarPanels();
					stateValue = "{\"New Value\": " + (solarPanels.isEmpty() ? -1 : solarPanels.get(0).getBaseHeight()) + "}";
				}

				else if (lastEdit instanceof ChangeFoundationSolarPanelModelCommand) {
					final Foundation f = ((ChangeFoundationSolarPanelModelCommand) lastEdit).getFoundation();
					final List<SolarPanel> solarPanels = f.getSolarPanels();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Model\": " + (solarPanels.isEmpty() ? null : "\"" + solarPanels.get(0).getPvModuleSpecs().getModel() + "\"") + "}";
				} else if (lastEdit instanceof ChangeModelForAllSolarPanelsCommand) {
					final List<SolarPanel> solarPanels = Scene.getInstance().getAllSolarPanels();
					stateValue = "{\"New Model\": " + (solarPanels.isEmpty() ? null : "\"" + solarPanels.get(0).getPvModuleSpecs().getModel() + "\"") + "}";
				}

				/* rack properties */

				else if (lastEdit instanceof ChangeSolarPanelModelForRackCommand) {
					final ChangeSolarPanelModelForRackCommand c = (ChangeSolarPanelModelForRackCommand) lastEdit;
					final Rack rack = c.getRack();
					stateValue = "{\"Foundation\": " + rack.getTopContainer().getId() + ", \"ID\": " + rack.getId();
					stateValue += ", \"Old Model\": \"" + c.getOldModel().getModel() + "\", \"New Model\": \"" + rack.getSolarPanel().getPvModuleSpecs().getModel() + "\"}";
				} else if (lastEdit instanceof ChangeSolarPanelModelForRacksOnFoundationCommand) {
					final Foundation f = ((ChangeSolarPanelModelForRacksOnFoundationCommand) lastEdit).getFoundation();
					final List<Rack> racks = f.getRacks();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Model\": " + (racks.isEmpty() ? null : "\"" + racks.get(0).getSolarPanel().getPvModuleSpecs().getModel() + "\"") + "}";
				} else if (lastEdit instanceof ChangeSolarPanelModelForAllRacksCommand) {
					final List<Rack> racks = Scene.getInstance().getAllRacks();
					stateValue = "{\"New Model\": " + (racks.isEmpty() ? null : "\"" + racks.get(0).getSolarPanel().getPvModuleSpecs().getModel() + "\"") + "}";
				}

				else if (lastEdit instanceof SetSolarCellEfficiencyForRackCommand) {
					final SetSolarCellEfficiencyForRackCommand c = (SetSolarCellEfficiencyForRackCommand) lastEdit;
					final Rack rack = c.getRack();
					stateValue = "{\"Foundation\": " + rack.getTopContainer().getId() + ", \"ID\": " + rack.getId();
					stateValue += ", \"Old Value\": \"" + c.getOldValue() + "\", \"New Value\": \"" + rack.getSolarPanel().getCellEfficiency() + "\"}";
				} else if (lastEdit instanceof SetSolarCellEfficiencyForRacksOnFoundationCommand) {
					final SetSolarCellEfficiencyForRacksOnFoundationCommand c = (SetSolarCellEfficiencyForRacksOnFoundationCommand) lastEdit;
					final Foundation f = c.getFoundation();
					final List<Rack> racks = f.getRacks();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (racks.isEmpty() ? null : "\"" + racks.get(0).getSolarPanel().getCellEfficiency() + "\"") + "}";
				} else if (lastEdit instanceof SetSolarCellEfficiencyForAllRacksCommand) {
					final List<Rack> racks = Scene.getInstance().getAllRacks();
					stateValue = "{\"New Value\": " + (racks.isEmpty() ? null : "\"" + racks.get(0).getSolarPanel().getCellEfficiency() + "\"") + "}";
				}

				else if (lastEdit instanceof SetNoctForRackCommand) {
					final SetNoctForRackCommand c = (SetNoctForRackCommand) lastEdit;
					final Rack rack = c.getRack();
					stateValue = "{\"Foundation\": " + rack.getTopContainer().getId() + ", \"ID\": " + rack.getId();
					stateValue += ", \"Old Value\": \"" + c.getOldValue() + "\", \"New Value\": \"" + rack.getSolarPanel().getNominalOperatingCellTemperature() + "\"}";
				} else if (lastEdit instanceof SetNoctForRacksOnFoundationCommand) {
					final SetNoctForRacksOnFoundationCommand c = (SetNoctForRacksOnFoundationCommand) lastEdit;
					final Foundation f = c.getFoundation();
					final List<Rack> racks = f.getRacks();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (racks.isEmpty() ? null : "\"" + racks.get(0).getSolarPanel().getNominalOperatingCellTemperature() + "\"") + "}";
				} else if (lastEdit instanceof SetNoctForAllRacksCommand) {
					final List<Rack> racks = Scene.getInstance().getAllRacks();
					stateValue = "{\"New Value\": " + (racks.isEmpty() ? null : "\"" + racks.get(0).getSolarPanel().getNominalOperatingCellTemperature() + "\"") + "}";
				}

				else if (lastEdit instanceof SetTemperatureCoefficientPmaxForRackCommand) {
					final SetTemperatureCoefficientPmaxForRackCommand c = (SetTemperatureCoefficientPmaxForRackCommand) lastEdit;
					final Rack rack = c.getRack();
					stateValue = "{\"Foundation\": " + rack.getTopContainer().getId() + ", \"ID\": " + rack.getId();
					stateValue += ", \"Old Value\": \"" + c.getOldValue() + "\", \"New Value\": \"" + rack.getSolarPanel().getTemperatureCoefficientPmax() + "\"}";
				} else if (lastEdit instanceof SetTemperatureCoefficientPmaxForRacksOnFoundationCommand) {
					final SetTemperatureCoefficientPmaxForRacksOnFoundationCommand c = (SetTemperatureCoefficientPmaxForRacksOnFoundationCommand) lastEdit;
					final Foundation f = c.getFoundation();
					final List<Rack> racks = f.getRacks();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (racks.isEmpty() ? null : "\"" + racks.get(0).getSolarPanel().getTemperatureCoefficientPmax() + "\"") + "}";
				} else if (lastEdit instanceof SetTemperatureCoefficientPmaxForAllRacksCommand) {
					final List<Rack> racks = Scene.getInstance().getAllRacks();
					stateValue = "{\"New Value\": " + (racks.isEmpty() ? null : "\"" + racks.get(0).getSolarPanel().getTemperatureCoefficientPmax() + "\"") + "}";
				}

				else if (lastEdit instanceof SetSolarPanelCellTypeForRackCommand) {
					final SetSolarPanelCellTypeForRackCommand c = (SetSolarPanelCellTypeForRackCommand) lastEdit;
					final Rack rack = c.getRack();
					stateValue = "{\"Foundation\": " + rack.getTopContainer().getId() + ", \"ID\": " + rack.getId();
					stateValue += ", \"Old Type\": \"" + c.getOldValue() + "\", \"New Type\": \"" + rack.getSolarPanel().getCellType() + "\"}";
				} else if (lastEdit instanceof SetSolarPanelCellTypeForRacksOnFoundationCommand) {
					final SetSolarPanelCellTypeForRacksOnFoundationCommand c = (SetSolarPanelCellTypeForRacksOnFoundationCommand) lastEdit;
					final Foundation f = c.getFoundation();
					final List<Rack> racks = f.getRacks();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Type\": " + (racks.isEmpty() ? null : "\"" + racks.get(0).getSolarPanel().getCellType() + "\"") + "}";
				} else if (lastEdit instanceof SetSolarPanelCellTypeForAllRacksCommand) {
					final List<Rack> racks = Scene.getInstance().getAllRacks();
					stateValue = "{\"New Type\": " + (racks.isEmpty() ? null : "\"" + racks.get(0).getSolarPanel().getCellType() + "\"") + "}";
				}

				else if (lastEdit instanceof SetSolarPanelShadeToleranceForRackCommand) {
					final SetSolarPanelShadeToleranceForRackCommand c = (SetSolarPanelShadeToleranceForRackCommand) lastEdit;
					final Rack rack = c.getRack();
					stateValue = "{\"Foundation\": " + rack.getTopContainer().getId() + ", \"ID\": " + rack.getId();
					stateValue += ", \"Old Value\": \"" + c.getOldValue() + "\", \"New Value\": \"" + rack.getSolarPanel().getShadeTolerance() + "\"}";
				} else if (lastEdit instanceof SetSolarPanelShadeToleranceForRacksOnFoundationCommand) {
					final SetSolarPanelShadeToleranceForRacksOnFoundationCommand c = (SetSolarPanelShadeToleranceForRacksOnFoundationCommand) lastEdit;
					final Foundation f = c.getFoundation();
					final List<Rack> racks = f.getRacks();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (racks.isEmpty() ? null : "\"" + racks.get(0).getSolarPanel().getShadeTolerance() + "\"") + "}";
				} else if (lastEdit instanceof SetSolarPanelShadeToleranceForAllRacksCommand) {
					final List<Rack> racks = Scene.getInstance().getAllRacks();
					stateValue = "{\"New Value\": " + (racks.isEmpty() ? null : "\"" + racks.get(0).getSolarPanel().getShadeTolerance() + "\"") + "}";
				}

				else if (lastEdit instanceof SetSolarPanelColorForRackCommand) {
					final SetSolarPanelColorForRackCommand c = (SetSolarPanelColorForRackCommand) lastEdit;
					final Rack rack = c.getRack();
					stateValue = "{\"Foundation\": " + rack.getTopContainer().getId() + ", \"ID\": " + rack.getId();
					stateValue += ", \"Old Color\": \"" + c.getOldValue() + "\", \"New Color\": \"" + rack.getSolarPanel().getColorOption() + "\"}";
				} else if (lastEdit instanceof SetSolarPanelColorForRacksOnFoundationCommand) {
					final SetSolarPanelColorForRacksOnFoundationCommand c = (SetSolarPanelColorForRacksOnFoundationCommand) lastEdit;
					final Foundation f = c.getFoundation();
					final List<Rack> racks = f.getRacks();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Color\": " + (racks.isEmpty() ? null : "\"" + racks.get(0).getSolarPanel().getColorOption() + "\"") + "}";
				} else if (lastEdit instanceof SetSolarPanelColorForAllRacksCommand) {
					final List<Rack> racks = Scene.getInstance().getAllRacks();
					stateValue = "{\"New Color\": " + (racks.isEmpty() ? null : "\"" + racks.get(0).getSolarPanel().getColorOption() + "\"") + "}";
				}

				else if (lastEdit instanceof ChooseSolarPanelSizeForRackCommand) {
					final ChooseSolarPanelSizeForRackCommand c = (ChooseSolarPanelSizeForRackCommand) lastEdit;
					final Rack rack = c.getRack();
					stateValue = "{\"Foundation\": " + rack.getTopContainer().getId() + ", \"ID\": " + rack.getId();
					stateValue += ", \"Old Width\": " + c.getOldWidth() + ", \"New Width\": " + rack.getSolarPanel().getPanelWidth();
					stateValue += ", \"Old Height\": " + c.getOldHeight() + ", \"New Height\": " + rack.getSolarPanel().getPanelHeight() + "}";
				} else if (lastEdit instanceof SetSizeForRacksOnFoundationCommand) {
					final Foundation f = ((SetSizeForRacksOnFoundationCommand) lastEdit).getFoundation();
					final List<Rack> racks = f.getRacks();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Width\": " + (racks.isEmpty() ? -1 : racks.get(0).getRackWidth()) + ", \"New Height\": " + (racks.isEmpty() ? -1 : racks.get(0).getRackHeight()) + "}";
				} else if (lastEdit instanceof SetSizeForAllRacksCommand) {
					final List<Rack> racks = Scene.getInstance().getAllRacks();
					stateValue = "{\"New Width\": " + (racks.isEmpty() ? -1 : racks.get(0).getRackWidth()) + ", \"New Height\": " + (racks.isEmpty() ? -1 : racks.get(0).getRackHeight()) + "}";
				}

				else if (lastEdit instanceof RotateSolarPanelsForRackCommand) {
					final RotateSolarPanelsForRackCommand c = (RotateSolarPanelsForRackCommand) lastEdit;
					final Rack rack = c.getRack();
					stateValue = "{\"Foundation\": " + rack.getTopContainer().getId() + ", \"ID\": " + rack.getId();
					stateValue += ", \"Old Value\": \"" + c.getOldValue() + "\", \"New Value\": \"" + rack.getSolarPanel().isRotated() + "\"}";
				} else if (lastEdit instanceof RotateSolarPanelsForRacksOnFoundationCommand) {
					final RotateSolarPanelsForRacksOnFoundationCommand c = (RotateSolarPanelsForRacksOnFoundationCommand) lastEdit;
					final Foundation f = c.getFoundation();
					final List<Rack> racks = f.getRacks();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (racks.isEmpty() ? null : "\"" + racks.get(0).getSolarPanel().isRotated() + "\"") + "}";
				} else if (lastEdit instanceof RotateSolarPanelsForAllRacksCommand) {
					final List<Rack> racks = Scene.getInstance().getAllRacks();
					stateValue = "{\"New Value\": " + (racks.isEmpty() ? null : "\"" + racks.get(0).getSolarPanel().isRotated() + "\"") + "}";
				}

				else if (lastEdit instanceof SetInverterEfficiencyForRackCommand) {
					final SetInverterEfficiencyForRackCommand c = (SetInverterEfficiencyForRackCommand) lastEdit;
					final Rack rack = c.getRack();
					stateValue = "{\"Foundation\": " + rack.getTopContainer().getId() + ", \"ID\": " + rack.getId();
					stateValue += ", \"Old Value\": \"" + c.getOldValue() + "\", \"New Value\": \"" + rack.getSolarPanel().getInverterEfficiency() + "\"}";
				} else if (lastEdit instanceof SetInverterEfficiencyForRacksOnFoundationCommand) {
					final SetInverterEfficiencyForRacksOnFoundationCommand c = (SetInverterEfficiencyForRacksOnFoundationCommand) lastEdit;
					final Foundation f = c.getFoundation();
					final List<Rack> racks = f.getRacks();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (racks.isEmpty() ? null : "\"" + racks.get(0).getSolarPanel().getInverterEfficiency() + "\"") + "}";
				} else if (lastEdit instanceof SetInverterEfficiencyForAllRacksCommand) {
					final List<Rack> racks = Scene.getInstance().getAllRacks();
					stateValue = "{\"New Value\": " + (racks.isEmpty() ? null : "\"" + racks.get(0).getSolarPanel().getInverterEfficiency() + "\"") + "}";
				}

				else if (lastEdit instanceof ChangeFoundationRackTiltAngleCommand) {
					final Foundation f = ((ChangeFoundationRackTiltAngleCommand) lastEdit).getFoundation();
					final List<Rack> racks = f.getRacks();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (racks.isEmpty() ? -1 : racks.get(0).getTiltAngle()) + "}";
				} else if (lastEdit instanceof ChangeTiltAngleForAllRacksCommand) {
					final List<Rack> racks = Scene.getInstance().getAllRacks();
					stateValue = "{\"New Value\": " + (racks.isEmpty() ? -1 : racks.get(0).getTiltAngle()) + "}";
				}

				else if (lastEdit instanceof ChangeFoundationRackAzimuthCommand) {
					final Foundation f = ((ChangeFoundationRackAzimuthCommand) lastEdit).getFoundation();
					final List<Rack> racks = f.getRacks();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (racks.isEmpty() ? -1 : racks.get(0).getRelativeAzimuth()) + "}";
				} else if (lastEdit instanceof ChangeAzimuthForAllRacksCommand) {
					final List<Rack> racks = Scene.getInstance().getAllRacks();
					stateValue = "{\"New Value\": " + (racks.isEmpty() ? -1 : racks.get(0).getRelativeAzimuth()) + "}";
				}

				else if (lastEdit instanceof ChangeFoundationRackBaseHeightCommand) {
					final Foundation f = ((ChangeFoundationRackBaseHeightCommand) lastEdit).getFoundation();
					final List<Rack> racks = f.getRacks();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (racks.isEmpty() ? -1 : racks.get(0).getBaseHeight()) + "}";
				} else if (lastEdit instanceof ChangeBaseHeightForAllRacksCommand) {
					final List<Rack> racks = Scene.getInstance().getAllRacks();
					stateValue = "{\"New Value\": " + (racks.isEmpty() ? -1 : racks.get(0).getBaseHeight()) + "}";
				}

				else if (lastEdit instanceof SetSolarPanelSizeForRacksOnFoundationCommand) {
					final Foundation f = ((SetSolarPanelSizeForRacksOnFoundationCommand) lastEdit).getFoundation();
					final List<Rack> racks = f.getRacks();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Width\": " + (racks.isEmpty() ? -1 : racks.get(0).getSolarPanel().getPanelWidth()) + ", \"New Height\": " + (racks.isEmpty() ? -1 : racks.get(0).getSolarPanel().getPanelHeight()) + "}";
				} else if (lastEdit instanceof SetSolarPanelSizeForAllRacksCommand) {
					final List<Rack> racks = Scene.getInstance().getAllRacks();
					stateValue = "{\"New Width\": " + (racks.isEmpty() ? -1 : racks.get(0).getSolarPanel().getPanelWidth()) + ", \"New Height\": " + (racks.isEmpty() ? -1 : racks.get(0).getSolarPanel().getPanelHeight()) + "}";
				}

				/* tracker properties */

				else if (lastEdit instanceof SetSolarTrackerCommand) {
					final SetSolarTrackerCommand c = (SetSolarTrackerCommand) lastEdit;
					final Trackable tracker = c.getTracker();
					long bid = -1;
					long cid = -1;
					if (tracker instanceof HousePart) {
						bid = ((HousePart) tracker).getTopContainer().getId();
						cid = ((HousePart) tracker).getId();
					}
					stateValue = "{\"Foundation\": " + bid + ", \"ID\": " + cid + ", \"Old Value\": " + c.getOldValue() + ", \"New Value\": " + tracker.getTracker() + "}";
				} else if (lastEdit instanceof SetSolarTrackersOnFoundationCommand) {
					final SetSolarTrackersOnFoundationCommand c = (SetSolarTrackersOnFoundationCommand) lastEdit;
					final Foundation f = c.getFoundation();
					final Trackable tracker = c.getTracker();
					if (tracker instanceof SolarPanel) {
						final List<SolarPanel> solarPanels = f.getSolarPanels();
						stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (solarPanels.isEmpty() ? -1 : solarPanels.get(0).getTracker()) + "}";
					} else if (tracker instanceof Rack) {
						final List<Rack> racks = f.getRacks();
						stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (racks.isEmpty() ? -1 : racks.get(0).getTracker()) + "}";
					}
				} else if (lastEdit instanceof SetSolarTrackersForAllCommand) {
					final SetSolarTrackersForAllCommand c = (SetSolarTrackersForAllCommand) lastEdit;
					final Trackable tracker = c.getTracker();
					if (tracker instanceof SolarPanel) {
						final List<SolarPanel> solarPanels = Scene.getInstance().getAllSolarPanels();
						stateValue = "{\"New Value\": " + (solarPanels.isEmpty() ? -1 : solarPanels.get(0).getTracker()) + "}";
					} else if (tracker instanceof Rack) {
						final List<Rack> racks = Scene.getInstance().getAllRacks();
						stateValue = "{\"New Value\": " + (racks.isEmpty() ? -1 : racks.get(0).getTracker()) + "}";
					}
				}

				/* mirror properties */

				else if (lastEdit instanceof SetSizeForMirrorsOnFoundationCommand) {
					final Foundation f = ((SetSizeForMirrorsOnFoundationCommand) lastEdit).getFoundation();
					final List<Mirror> mirrors = f.getMirrors();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Width\": " + (mirrors.isEmpty() ? -1 : mirrors.get(0).getMirrorWidth()) + ", \"New Height\": " + (mirrors.isEmpty() ? -1 : mirrors.get(0).getMirrorHeight()) + "}";
				} else if (lastEdit instanceof SetSizeForAllMirrorsCommand) {
					final List<Mirror> mirrors = Scene.getInstance().getAllMirrors();
					stateValue = "{\"New Width\": " + (mirrors.isEmpty() ? -1 : mirrors.get(0).getMirrorWidth()) + ", \"New Height\": " + (mirrors.isEmpty() ? -1 : mirrors.get(0).getMirrorHeight()) + "}";
				}

				else if (lastEdit instanceof ChangeMirrorReflectanceCommand) {
					final ChangeMirrorReflectanceCommand c = (ChangeMirrorReflectanceCommand) lastEdit;
					final Mirror m = c.getMirror();
					stateValue = "{\"Foundation\": " + m.getTopContainer().getId() + ", \"ID\": " + m.getId() + ", \"Old Value\": " + c.getOldValue() + ", \"New Value\": " + m.getReflectance() + "}";
				} else if (lastEdit instanceof ChangeFoundationMirrorReflectanceCommand) {
					final Foundation f = ((ChangeFoundationMirrorReflectanceCommand) lastEdit).getFoundation();
					final List<Mirror> mirrors = f.getMirrors();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (mirrors.isEmpty() ? -1 : mirrors.get(0).getReflectance()) + "}";
				} else if (lastEdit instanceof ChangeReflectanceForAllMirrorsCommand) {
					final List<Mirror> mirrors = Scene.getInstance().getAllMirrors();
					stateValue = "{\"New Value\": " + (mirrors.isEmpty() ? -1 : mirrors.get(0).getReflectance()) + "}";
				}

				else if (lastEdit instanceof ChangeMirrorTargetCommand) {
					final ChangeMirrorTargetCommand c = (ChangeMirrorTargetCommand) lastEdit;
					final Mirror m = c.getMirror();
					stateValue = "{\"Foundation\": " + m.getTopContainer().getId() + ", \"ID\": " + m.getId();
					stateValue += ", \"Old Value\": " + (c.getOldValue() == null ? -1 : c.getOldValue().getId()) + ", \"New Value\": " + (c.getNewValue() == null ? -1 : c.getNewValue().getId()) + "}";
				} else if (lastEdit instanceof ChangeFoundationMirrorTargetCommand) {
					final Foundation f = ((ChangeFoundationMirrorTargetCommand) lastEdit).getFoundation();
					final List<Mirror> mirrors = f.getMirrors();
					long newValue = -1;
					if (!mirrors.isEmpty()) {
						final Foundation t = mirrors.get(0).getHeliostatTarget();
						if (t != null) {
							newValue = t.getId();
						}
					}
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + newValue + "}";
				} else if (lastEdit instanceof ChangeTargetForAllMirrorsCommand) {
					final List<Mirror> mirrors = Scene.getInstance().getAllMirrors();
					long newValue = -1;
					if (!mirrors.isEmpty()) {
						final Foundation t = mirrors.get(0).getHeliostatTarget();
						if (t != null) {
							newValue = t.getId();
						}
					}
					stateValue = "{\"New Value\": " + newValue + "}";
				}

				else if (lastEdit instanceof ChangeFoundationMirrorTiltAngleCommand) {
					final Foundation f = ((ChangeFoundationMirrorTiltAngleCommand) lastEdit).getFoundation();
					final List<Mirror> mirrors = f.getMirrors();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (mirrors.isEmpty() ? -1 : mirrors.get(0).getTiltAngle()) + "}";
				} else if (lastEdit instanceof ChangeTiltAngleForAllMirrorsCommand) {
					final List<Mirror> mirrors = Scene.getInstance().getAllMirrors();
					stateValue = "{\"New Value\": " + (mirrors.isEmpty() ? -1 : mirrors.get(0).getTiltAngle()) + "}";
				}

				else if (lastEdit instanceof ChangeFoundationMirrorAzimuthCommand) {
					final Foundation f = ((ChangeFoundationMirrorAzimuthCommand) lastEdit).getFoundation();
					final List<Mirror> mirrors = f.getMirrors();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (mirrors.isEmpty() ? -1 : mirrors.get(0).getRelativeAzimuth()) + "}";
				} else if (lastEdit instanceof ChangeAzimuthForAllMirrorsCommand) {
					final List<Mirror> mirrors = Scene.getInstance().getAllMirrors();
					stateValue = "{\"New Value\": " + (mirrors.isEmpty() ? -1 : mirrors.get(0).getRelativeAzimuth()) + "}";
				}

				else if (lastEdit instanceof ChangeFoundationMirrorBaseHeightCommand) {
					final Foundation f = ((ChangeFoundationMirrorBaseHeightCommand) lastEdit).getFoundation();
					final List<Mirror> mirrors = f.getMirrors();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + (mirrors.isEmpty() ? -1 : mirrors.get(0).getBaseHeight()) + "}";
				} else if (lastEdit instanceof ChangeBaseHeightForAllMirrorsCommand) {
					final List<Mirror> mirrors = Scene.getInstance().getAllMirrors();
					stateValue = "{\"New Value\": " + (mirrors.isEmpty() ? -1 : mirrors.get(0).getBaseHeight()) + "}";
				}

				/* parabolic trough properties */

				else if (lastEdit instanceof SetShapeForParabolicTroughsOnFoundationCommand) {
					final Foundation f = ((SetShapeForParabolicTroughsOnFoundationCommand) lastEdit).getFoundation();
					final List<ParabolicTrough> troughs = f.getParabolicTroughs();
					final ParabolicTrough t = troughs.isEmpty() ? null : troughs.get(0);
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Aperture Width\": " + (t == null ? -1 : t.getApertureWidth()) + ", \"New Length\": " + (t == null ? -1 : t.getTroughLength()) + ", \"New Module Length\": " + (t == null ? -1 : t.getModuleLength()) + ", \"New Semilatus Rectum\": " + (t == null ? -1 : t.getSemilatusRectum()) + "}";
				} else if (lastEdit instanceof SetShapeForAllParabolicTroughsCommand) {
					final List<ParabolicTrough> troughs = Scene.getInstance().getAllParabolicTroughs();
					final ParabolicTrough t = troughs.isEmpty() ? null : troughs.get(0);
					stateValue = "{\"New Aperture Width\": " + (t == null ? -1 : t.getApertureWidth()) + ", \"New Length\": " + (t == null ? -1 : t.getTroughLength()) + ", \"New Module Length\": " + (t == null ? -1 : t.getModuleLength()) + ", \"New Semilatus Rectum\": " + (t == null ? -1 : t.getSemilatusRectum()) + "}";
				}

				/* parabolic dish properties */

				else if (lastEdit instanceof SetRimRadiusForParabolicDishesOnFoundationCommand) {
					final Foundation f = ((SetRimRadiusForParabolicDishesOnFoundationCommand) lastEdit).getFoundation();
					final List<ParabolicDish> dishes = f.getParabolicDishes();
					final ParabolicDish d = dishes.isEmpty() ? null : dishes.get(0);
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Rim Radius\": " + (d == null ? -1 : d.getRimRadius()) + "}";
				} else if (lastEdit instanceof SetRimRadiusForAllParabolicDishesCommand) {
					final List<ParabolicDish> dishes = Scene.getInstance().getAllParabolicDishes();
					final ParabolicDish d = dishes.isEmpty() ? null : dishes.get(0);
					stateValue = "{\"New Rim Radius\": " + (d == null ? -1 : d.getRimRadius()) + "}";
				}

				/* Fresnel reflector properties */

				else if (lastEdit instanceof SetSizeForFresnelReflectorsOnFoundationCommand) {
					final Foundation f = ((SetSizeForFresnelReflectorsOnFoundationCommand) lastEdit).getFoundation();
					final List<FresnelReflector> reflectors = f.getFresnelReflectors();
					final FresnelReflector r = reflectors.isEmpty() ? null : reflectors.get(0);
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Length\": " + (r == null ? -1 : r.getLength()) + ", \"New Module Length\": " + (r == null ? -1 : r.getModuleLength()) + ", \"New Module Width\": " + (r == null ? -1 : r.getModuleWidth()) + "}";
				} else if (lastEdit instanceof SetSizeForAllFresnelReflectorsCommand) {
					final List<FresnelReflector> reflectors = Scene.getInstance().getAllFresnelReflectors();
					final FresnelReflector r = reflectors.isEmpty() ? null : reflectors.get(0);
					stateValue = "{\"New Length\": " + (r == null ? -1 : r.getLength()) + ", \"New Module Length\": " + (r == null ? -1 : r.getModuleLength()) + ", \"New Module Width\": " + (r == null ? -1 : r.getModuleWidth()) + "}";
				}

				/* wall properties */

				else if (lastEdit instanceof ChangeWallTypeCommand) {
					final ChangeWallTypeCommand c = (ChangeWallTypeCommand) lastEdit;
					final Wall w = c.getWall();
					stateValue = "{\"Building\": " + w.getContainer().getId() + ", \"ID\": " + w.getId();
					stateValue += ", \"Old Value\": " + c.getOldValue() + ", \"New Value\": " + w.getType() + "}";
				}

				else if (lastEdit instanceof ChangeWallThicknessCommand) {
					final ChangeWallThicknessCommand c = (ChangeWallThicknessCommand) lastEdit;
					final Wall w = c.getWall();
					stateValue = "{\"Building\": " + w.getContainer().getId() + ", \"ID\": " + w.getId();
					stateValue += ", \"Old Value\": " + c.getOldValue() + ", \"New Value\": " + w.getThickness() + "}";
				} else if (lastEdit instanceof ChangeFoundationWallThicknessCommand) {
					final ChangeFoundationWallThicknessCommand c = (ChangeFoundationWallThicknessCommand) lastEdit;
					final Foundation f = c.getFoundation();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + c.getWalls().get(0).getThickness() + "}";
				} else if (lastEdit instanceof ChangeThicknessForAllWallsCommand) {
					final ChangeThicknessForAllWallsCommand c = (ChangeThicknessForAllWallsCommand) lastEdit;
					stateValue = "{\"New Value\": " + (c.getWalls().isEmpty() ? -1 : ((Wall) c.getWalls().get(0)).getThickness()) + "}";
				}

				else if (lastEdit instanceof ChangeWallHeightCommand) {
					final ChangeWallHeightCommand c = (ChangeWallHeightCommand) lastEdit;
					final Wall w = c.getWall();
					stateValue = "{\"Building\": " + w.getContainer().getId() + ", \"ID\": " + w.getId();
					stateValue += ", \"Old Value\": " + c.getOldValue() + ", \"New Value\": " + w.getHeight() + "}";
				} else if (lastEdit instanceof ChangeFoundationWallHeightCommand) {
					final ChangeFoundationWallHeightCommand c = (ChangeFoundationWallHeightCommand) lastEdit;
					final Foundation f = c.getFoundation();
					stateValue = "{\"Foundation\": " + f.getId() + ", \"New Value\": " + c.getWalls().get(0).getHeight() + "}";
				} else if (lastEdit instanceof ChangeHeightForAllWallsCommand) {
					final ChangeHeightForAllWallsCommand c = (ChangeHeightForAllWallsCommand) lastEdit;
					stateValue = "{\"New Value\": " + (c.getWalls().isEmpty() ? -1 : ((Wall) c.getWalls().get(0)).getHeight()) + "}";
				} else if (lastEdit instanceof ChangeHeightForConnectedWallsCommand) {
					final ChangeHeightForConnectedWallsCommand c = (ChangeHeightForConnectedWallsCommand) lastEdit;
					stateValue = "{\"New Value\": " + (c.getWalls().isEmpty() ? -1 : c.getWalls().get(0).getHeight()) + "}";
				}

				/* window properties */

				else if (lastEdit instanceof ChangeWindowShgcCommand) {
					final ChangeWindowShgcCommand c = (ChangeWindowShgcCommand) lastEdit;
					final Window w = c.getWindow();
					stateValue = "{\"Building\": " + w.getTopContainer().getId() + ", \"ID\": " + w.getId() + ", \"Old Value\": " + c.getOldValue() + ", \"New Value\": " + w.getSolarHeatGainCoefficient() + "}";
				} else if (lastEdit instanceof ChangeContainerWindowShgcCommand) {
					final ChangeContainerWindowShgcCommand c = (ChangeContainerWindowShgcCommand) lastEdit;
					final HousePart container = c.getContainer();
					final List<Window> windows = Scene.getInstance().getWindowsOnContainer(container);
					final String containerType = container instanceof Wall ? "Wall" : "Roof";
					stateValue = "{\"" + containerType + "\": " + container.getId() + ", \"New Value\": " + (windows.isEmpty() ? -1 : windows.get(0).getSolarHeatGainCoefficient()) + "}";
				} else if (lastEdit instanceof ChangeBuildingWindowShgcCommand) {
					final ChangeBuildingWindowShgcCommand c = (ChangeBuildingWindowShgcCommand) lastEdit;
					final Foundation foundation = c.getFoundation();
					final List<Window> windows = Scene.getInstance().getWindowsOfBuilding(foundation);
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
			final HousePart analyzedPart = SceneManager.getInstance().getSelectedPart();
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
					} else if (analysisRequester instanceof BuildingDailyEnergyGraph) {
						line += ((BuildingDailyEnergyGraph) analysisRequester).toJson();
						final String result = Building.getBuildingSolarPotentials();
						if (result != null) {
							line += ", \"Solar Potential\": " + result;
						}
					} else if (analysisRequester instanceof EnergyAnnualAnalysis) {
						line += ((EnergyAnnualAnalysis) analysisRequester).toJson();
					} else if (analysisRequester instanceof EnergyAngularAnalysis) {
						line += ((EnergyAngularAnalysis) analysisRequester).toJson();
					} else if (analysisRequester instanceof ProjectCost) {
						line += ((ProjectCost) analysisRequester).toJson();
					}
				} else {
					if (analysisRequester instanceof ProjectCost) {
						line += ((ProjectCost) analysisRequester).toJson();
					} else if (analysisRequester instanceof BuildingDailyEnergyGraph) {
						line += ((BuildingDailyEnergyGraph) analysisRequester).toJson();
						final String result = Building.getBuildingSolarPotentials();
						if (result != null) {
							line += ", \"Solar Potential\": " + result;
						}
					}
				}
				if (analysisRequester instanceof PvDailyAnalysis) {
					line += ((PvDailyAnalysis) analysisRequester).toJson();
				} else if (analysisRequester instanceof PvAnnualAnalysis) {
					line += ((PvAnnualAnalysis) analysisRequester).toJson();
				}
				if (analysisRequester instanceof GroupDailyAnalysis) {
					line += ((GroupDailyAnalysis) analysisRequester).toJson();
				} else if (analysisRequester instanceof GroupAnnualAnalysis) {
					line += ((GroupAnnualAnalysis) analysisRequester).toJson();
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
		} catch (final Throwable t) {
			Util.reportError(t);
		} finally {
			action = null;
		}
	}

	public void logAnalysis(final Object x) {
		analysisRequester = x;
		try {
			record();
		} catch (final Throwable t) {
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

	public void logCamera(final String mode) {
		if (!isCameraChanged()) {
			return;
		}
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
		if (cameraPosition.equals(lastCameraPosition)) {
			return false;
		}
		lastCameraPosition = cameraPosition;
		return true;
	}

	public void logGraphTab(final String graphTabName) {
		action = "Graph Tab";
		this.graphTabName = graphTabName;
		record();
		action = null;
	}

	public void logClearGraphData(final String graphName) {
		action = "Clear Graph Data";
		this.graphName = graphName;
		record();
		action = null;
	}

	public void logShowCurve(final String graphName, final String curveName, final boolean curveShown) {
		action = "Show Curve";
		this.graphName = graphName;
		this.curveName = curveName;
		this.curveShown = curveShown;
		record();
		action = null;
	}

	public void logShowRun(final String graphName, final String runID, final boolean runShown) {
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
		} catch (final Exception e) {
			Util.reportError(e);
		}
		writer.write("{\n\"Activities\": [\n");
	}

}
