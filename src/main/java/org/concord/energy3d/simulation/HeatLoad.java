package org.concord.energy3d.simulation;

import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JComboBox;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Building;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.Thermalizable;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.Util;

import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;

public class HeatLoad {

	public final static double LOWEST_TEMPERATURE_OF_WARM_DAY = 15;
	private final static HeatLoad instance = new HeatLoad();

	public static HeatLoad getInstance() {
		return instance;
	}

	private double getUValue(final HousePart part) {
		if (part instanceof Foundation)
			return ((Foundation) part).getUValue();
		if (part instanceof Wall)
			return ((Wall) part).getUValue();
		if (part instanceof Door)
			return ((Door) part).getUValue();
		if (part instanceof Roof)
			return ((Roof) part).getUValue();
		if (part instanceof Window)
			return ((Window) part).getUValue();
		if (part instanceof Sensor) {
			final HousePart container = part.getContainer();
			if (container instanceof Wall) {
				final HousePart x = insideChild(part.getPoints().get(0), container);
				if (x instanceof Window)
					return ((Window) x).getUValue();
				if (x instanceof Door)
					return ((Door) x).getUValue();
				return ((Wall) container).getUValue();
			}
			if (container instanceof Roof)
				return ((Roof) container).getUValue();
			if (container instanceof Foundation)
				return ((Foundation) container).getUValue();
		}
		return 0;
	}

	public void computeEnergyToday(final Calendar today) {
		
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
			final double outsideTemperature = Weather.getInstance().getOutsideTemperatureAtMinute(outsideTemperatureRange[1], outsideTemperatureRange[0], minute);
			for (final HousePart part : Scene.getInstance().getParts()) {
				if (part instanceof Human || part instanceof Tree || part instanceof Floor) // these should not be in the heat load calculations
					continue;
				final float absorption = part instanceof Window ? 0 : 1 - part.getAlbedo();
				if (part instanceof Roof) { // need to compute piece by piece for a roof because sun affects outside temperature of roof part
					final Roof roof = (Roof) part;
					for (final Spatial child : roof.getRoofPartsRoot().getChildren()) {
						if (child.getSceneHints().getCullHint() != CullHint.Always) {
							final Mesh mesh = (Mesh) ((Node) child).getChild(6);
							final double[] solarPotential = SolarRadiation.getInstance().getSolarPotential(mesh);
							final double solarHeat = solarPotential != null ? solarPotential[iMinute] * absorption / roof.getVolumetricHeatCapacity() : 0;
							final double insideTemperature = part.getTopContainer().getThermostat().getTemperature(today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY, minute / 60);
							final double deltaT = insideTemperature - (outsideTemperature + solarHeat);
							if (part.isDrawCompleted()) {
								final double uValue = getUValue(part);
								if (Util.isZero(uValue))
									continue;
								double heatloss = roof.getAreaWithoutOverhang(mesh) * uValue * deltaT / 1000.0 / 60 * timeStep;
								// if the lowest outside temperature is high enough, there is no need to turn on the heater hence no heat loss
								if (heatloss > 0 && outsideTemperatureRange[0] >= LOWEST_TEMPERATURE_OF_WARM_DAY)
									heatloss = 0;
								roof.getHeatLoss()[iMinute] += heatloss;
								final double[] heatLossArray = SolarRadiation.getInstance().getHeatLoss(mesh);
								if (heatLossArray != null)
									heatLossArray[iMinute] += heatloss;
							}
						}
					}
				} else if (part instanceof Foundation) {
					final double groundTemperature = Scene.getInstance().getGround().getTemperatureMinuteOfDay(today.get(Calendar.DAY_OF_YEAR), minute, 0.5 * (outsideTemperatureRange[1] - outsideTemperatureRange[0]));
					final Foundation foundation = (Foundation) part;
					final double insideTemperature = foundation.getThermostat().getTemperature(today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY, minute / 60);
					final double deltaT = insideTemperature - groundTemperature;
					if (foundation.isDrawCompleted()) {
						final double uValue = getUValue(part);
						if (Util.isZero(uValue))
							continue;
						Building building = new Building(foundation);
						double area;
						if (building.isWallComplete()) {
							building.calculate();
							area = building.getArea();
						} else {
							area = foundation.getArea();
						}
						final double heatloss = area * uValue * deltaT / 1000.0 / 60 * timeStep;
						// if (iMinute % 4 == 0) System.out.println((int) (iMinute / 4) + "=" + outsideTemperature + ", " + groundTemperature + ", " + deltaT + ", " + heatloss);
						foundation.getHeatLoss()[iMinute] += heatloss;
					}
				} else {
					double solarHeat = part.getSolarPotential()[iMinute] * absorption;
					if (part instanceof Thermalizable)
						solarHeat /= ((Thermalizable) part).getVolumetricHeatCapacity();
					final double insideTemperature = part.getTopContainer().getThermostat().getTemperature(today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY, minute / 60);
					final double deltaT = insideTemperature - (outsideTemperature + solarHeat);
					if (part.isDrawCompleted()) {
						final double uValue = getUValue(part);
						if (Util.isZero(uValue))
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
