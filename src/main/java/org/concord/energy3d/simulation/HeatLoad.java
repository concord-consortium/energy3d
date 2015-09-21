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
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;

import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;

public class HeatLoad {

	private final static HeatLoad instance = new HeatLoad();
	private double wallUFactor, doorUFactor, windowUFactor, roofUFactor, floorUFactor;

	public static HeatLoad getInstance() {
		return instance;
	}

	private static boolean isZero(final double x) {
		return Math.abs(x) < 0.000001;
	}

	private double getUFactor(final HousePart part) {
		if (part instanceof Foundation)
			return isZero(part.getUFactor()) ? floorUFactor : part.getUFactor();
		if (part instanceof Wall)
			return isZero(part.getUFactor()) ? wallUFactor : part.getUFactor();
		if (part instanceof Door)
			return isZero(part.getUFactor()) ? doorUFactor : part.getUFactor();
		if (part instanceof Roof)
			return isZero(part.getUFactor()) ? roofUFactor : part.getUFactor();
		if (part instanceof Window)
			return isZero(part.getUFactor()) ? windowUFactor : part.getUFactor();
		if (part instanceof Sensor) {
			final HousePart container = part.getContainer();
			if (container instanceof Wall) {
				final HousePart x = insideChild(part.getPoints().get(0), container);
				if (x instanceof Window)
					return isZero(x.getUFactor()) ? windowUFactor : x.getUFactor();
				if (x instanceof Door)
					return isZero(x.getUFactor()) ? doorUFactor : x.getUFactor();
				return isZero(container.getUFactor()) ? wallUFactor : container.getUFactor();
			}
			if (container instanceof Roof)
				return isZero(container.getUFactor()) ? roofUFactor : container.getUFactor();
			if (container instanceof Foundation)
				return isZero(container.getUFactor()) ? floorUFactor : container.getUFactor();
		}
		return part.getUFactor();
	}

	public void computeEnergyToday(final Calendar today, final double insideTemperature) {
		try {
			wallUFactor = parseValue(EnergyPanel.getInstance().getWallsComboBox());
			doorUFactor = parseValue(EnergyPanel.getInstance().getDoorsComboBox());
			windowUFactor = parseValue(EnergyPanel.getInstance().getWindowsComboBox());
			roofUFactor = parseValue(EnergyPanel.getInstance().getRoofsComboBox());
			floorUFactor = parseValue(EnergyPanel.getInstance().getFloorsComboBox());
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
		final double[] outsideTemperatureRange = CityData.getInstance().computeOutsideTemperature(today, (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem());
		int iMinute = 0;
		for (int minute = 0; minute < SolarRadiation.MINUTES_OF_DAY; minute += timeStep) {
			iMinute = minute / timeStep;
			for (final HousePart part : Scene.getInstance().getParts()) {
				final double outsideTemperature = CityData.getInstance().computeOutsideTemperatureRange(outsideTemperatureRange, minute);
				final float absorption = part instanceof Window ? 0 : 1 - part.getAlbedo();
				if (part instanceof Roof) { // need to compute piece by piece for a roof because sun affects outside temperature of roof part
					final Roof roof = (Roof) part;
					for (final Spatial child : roof.getRoofPartsRoot().getChildren()) {
						final Mesh mesh = (Mesh) ((Node) child).getChild(0);
						final double[] solarPotential = SolarRadiation.getInstance().getSolarPotential(mesh);
						final double solarHeat = solarPotential != null ? solarPotential[iMinute] * absorption / part.getVolumetricHeatCapacity() : 0;
						final double deltaT = insideTemperature - (outsideTemperature + solarHeat);
						if (part.isDrawCompleted()) {
							final double uFactor = getUFactor(part);
							if (isZero(uFactor))
								continue;
							double heatloss = roof.getArea(mesh) * uFactor * deltaT / 1000.0 / 60 * timeStep;
							if (heatloss > 0 && outsideTemperatureRange[0] >= 15)
								heatloss = 0;
							roof.getHeatLoss()[iMinute] += heatloss;
							final double[] heatLossArray = SolarRadiation.getInstance().getHeatLoss(mesh);
							if (heatLossArray != null)
								heatLossArray[iMinute] += heatloss;
						}
					}
				} else {
					final double solarHeat = part.getSolarPotential()[iMinute] * absorption / part.getVolumetricHeatCapacity();
					final double deltaT = insideTemperature - (outsideTemperature + solarHeat);
					if (part.isDrawCompleted()) {
						final double uFactor = getUFactor(part);
						if (isZero(uFactor))
							continue;
						double area = 0;
						if (part instanceof Foundation) {
							double[] buildingGeometry = ((Foundation) part).getBuildingGeometry();
							area = buildingGeometry != null ? buildingGeometry[1] : part.getArea();
						} else {
							area = part.getArea();
						}
						double heatloss = area * uFactor * deltaT / 1000.0 / 60 * timeStep;
						if (heatloss > 0 && outsideTemperatureRange[0] >= 15)
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
