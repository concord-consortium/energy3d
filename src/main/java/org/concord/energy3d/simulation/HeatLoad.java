package org.concord.energy3d.simulation;

import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.Thermalizable;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;

import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;

public class HeatLoad {

	public final static double LOWEST_TEMPERATURE_OF_WARM_DAY = 15;
	private final static HeatLoad instance = new HeatLoad();
	private double wallUValue, doorUValue, windowUValue, roofUValue, floorUValue;

	public static HeatLoad getInstance() {
		return instance;
	}

	private static boolean isZero(final double x) {
		return Math.abs(x) < 0.000001;
	}

	private double getUValue(final HousePart part) {
		if (part instanceof Foundation)
			return isZero(((Foundation) part).getUValue()) ? floorUValue : ((Foundation) part).getUValue();
		if (part instanceof Wall)
			return isZero(((Wall) part).getUValue()) ? wallUValue : ((Wall) part).getUValue();
		if (part instanceof Door)
			return isZero(((Door) part).getUValue()) ? doorUValue : ((Door) part).getUValue();
		if (part instanceof Roof)
			return isZero(((Roof) part).getUValue()) ? roofUValue : ((Roof) part).getUValue();
		if (part instanceof Window)
			return isZero(((Window) part).getUValue()) ? windowUValue : ((Window) part).getUValue();
		if (part instanceof Sensor) {
			final HousePart container = part.getContainer();
			if (container instanceof Wall) {
				final HousePart x = insideChild(part.getPoints().get(0), container);
				if (x instanceof Window)
					return isZero(((Window) x).getUValue()) ? windowUValue : ((Window) x).getUValue();
				if (x instanceof Door)
					return isZero(((Door) x).getUValue()) ? doorUValue : ((Door) x).getUValue();
				return isZero(((Wall) container).getUValue()) ? wallUValue : ((Wall) container).getUValue();
			}
			if (container instanceof Roof)
				return isZero(((Roof) container).getUValue()) ? roofUValue : ((Roof) container).getUValue();
			if (container instanceof Foundation)
				return isZero(((Foundation) container).getUValue()) ? floorUValue : ((Foundation) container).getUValue();
		}
		return 0;
	}

	public void computeEnergyToday(final Calendar today, final double insideTemperature) {
		try {
			wallUValue = parseValue(EnergyPanel.getInstance().getWallsComboBox());
			doorUValue = parseValue(EnergyPanel.getInstance().getDoorsComboBox());
			windowUValue = parseValue(EnergyPanel.getInstance().getWindowsComboBox());
			roofUValue = parseValue(EnergyPanel.getInstance().getRoofsComboBox());
			floorUValue = parseValue(EnergyPanel.getInstance().getFloorsComboBox());
		} catch (final Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(MainPanel.getInstance(), "Invalid U-Factor value: " + e.getMessage(), "Invalid U-Factor", JOptionPane.WARNING_MESSAGE);
			return;
		}

		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.HOUR_OF_DAY, 0);

		if (EnergyPanel.getInstance().getCityComboBox().getSelectedItem().equals(""))
			return;

		final int timeStep = SolarRadiation.getInstance().getTimeStep();
		final double[] outsideTemperatureRange = Weather.computeOutsideTemperature(today, (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem());
		// System.out.println(today.get(Calendar.DAY_OF_YEAR) + ", " + outsideTemperatureRange[0] + ", " + outsideTemperatureRange[1]);
		int iMinute = 0;
		for (int minute = 0; minute < SolarRadiation.MINUTES_OF_DAY; minute += timeStep) {
			iMinute = minute / timeStep;
			for (final HousePart part : Scene.getInstance().getParts()) {
				final double outsideTemperature = Weather.getInstance().getOutsideTemperatureAtMinute(outsideTemperatureRange[1], outsideTemperatureRange[0], minute);
				final float absorption = part instanceof Window ? 0 : 1 - part.getAlbedo();
				if (part instanceof Roof) { // need to compute piece by piece for a roof because sun affects outside temperature of roof part
					final Roof roof = (Roof) part;
					for (final Spatial child : roof.getRoofPartsRoot().getChildren()) {
						final Mesh mesh = (Mesh) ((Node) child).getChild(0);
						final double[] solarPotential = SolarRadiation.getInstance().getSolarPotential(mesh);
						final double solarHeat = solarPotential != null ? solarPotential[iMinute] * absorption / roof.getVolumetricHeatCapacity() : 0;
						final double deltaT = insideTemperature - (outsideTemperature + solarHeat);
						if (part.isDrawCompleted()) {
							final double uValue = getUValue(part);
							if (isZero(uValue))
								continue;
							double heatloss = roof.getArea(mesh) * uValue * deltaT / 1000.0 / 60 * timeStep;
							// if the lowest outside temperature is high enough, there is no need to turn on the heater hence no heat loss
							if (heatloss > 0 && outsideTemperatureRange[0] >= LOWEST_TEMPERATURE_OF_WARM_DAY)
								heatloss = 0;
							roof.getHeatLoss()[iMinute] += heatloss;
							final double[] heatLossArray = SolarRadiation.getInstance().getHeatLoss(mesh);
							if (heatLossArray != null)
								heatLossArray[iMinute] += heatloss;
						}
					}
				} else if (part instanceof Foundation) {
					double groundTemperature = Ground.getInstance().getTemperatureMinuteOfDay(today.get(Calendar.DAY_OF_YEAR), minute, 0.5 * (outsideTemperatureRange[1] - outsideTemperatureRange[0]));
					double deltaT = insideTemperature - groundTemperature;
					Foundation foundation = (Foundation) part;
					if (foundation.isDrawCompleted()) {
						final double uValue = getUValue(foundation);
						if (isZero(uValue))
							continue;
						double[] buildingGeometry = foundation.getBuildingGeometry();
						double area = buildingGeometry != null ? buildingGeometry[1] : foundation.getArea();
						double heatloss = area * uValue * deltaT / 1000.0 / 60 * timeStep;
						// if (iMinute % 4 == 0) System.out.println((int) (iMinute / 4) + "=" + outsideTemperature + ", " + groundTemperature + ", " + deltaT + ", " + heatloss);
						foundation.getHeatLoss()[iMinute] += heatloss;
					}
				} else {
					double solarHeat = part.getSolarPotential()[iMinute] * absorption;
					if (part instanceof Thermalizable)
						solarHeat /= ((Thermalizable) part).getVolumetricHeatCapacity();
					final double deltaT = insideTemperature - (outsideTemperature + solarHeat);
					if (part.isDrawCompleted()) {
						final double uValue = getUValue(part);
						if (isZero(uValue))
							continue;
						double heatloss = part.getArea() * uValue * deltaT / 1000.0 / 60 * timeStep;
						// if outside is warm enough, there is no need to turn on the heater hence no heat loss
						if (heatloss > 0 && outsideTemperatureRange[0] >= LOWEST_TEMPERATURE_OF_WARM_DAY)
							heatloss = 0;
						part.getHeatLoss()[iMinute] += heatloss;
					}
				}
			}
		}

	}

	private static HousePart insideChild(final Vector3 point, final HousePart parent) {
		for (final HousePart x : parent.getChildren())
			if (insideRectangle(point, x.getPoints()))
				return x;
		return null;
	}

	// Note: this assumes y = 0 in all Vector3
	private static boolean insideRectangle(final Vector3 point, final ArrayList<Vector3> rect) {
		final double x = point.getX();
		final double y = point.getZ();
		double xmin = Double.MAX_VALUE;
		double xmax = -Double.MAX_VALUE;
		double ymin = Double.MAX_VALUE;
		double ymax = -Double.MAX_VALUE;
		for (final Vector3 v : rect) {
			if (xmin > v.getX())
				xmin = v.getX();
			if (xmax < v.getX())
				xmax = v.getX();
			if (ymin > v.getZ())
				ymin = v.getZ();
			if (ymax < v.getZ())
				ymax = v.getZ();
		}
		return x > xmin && x < xmax && y > ymin && y < ymax;
	}

	public static double parseValue(final JComboBox<String> comboBox) {
		final String valueStr = comboBox.getSelectedItem().toString();
		final int indexOfSpace = valueStr.indexOf(' ');
		return Double.parseDouble(valueStr.substring(0, indexOfSpace != -1 ? indexOfSpace : valueStr.length()));
	}

}
