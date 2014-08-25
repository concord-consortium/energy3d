package org.concord.energy3d.simulation;

import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Door;
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
	private double wallUFactor, doorUFactor, windowUFactor, roofUFactor;

	public static HeatLoad getInstance() {
		return instance;
	}

	private double getUFactor(HousePart part) {
		if (part instanceof Wall)
			return part.getUFactor() < 0.000001 ? wallUFactor : part.getUFactor();
		if (part instanceof Door)
			return part.getUFactor() < 0.000001 ? doorUFactor : part.getUFactor();
		if (part instanceof Roof)
			return part.getUFactor() < 0.000001 ? roofUFactor : part.getUFactor();
		if (part instanceof Window)
			return part.getUFactor() < 0.000001 ? windowUFactor : part.getUFactor();
		if (part instanceof Sensor) {
			HousePart container = part.getContainer();
			if (container instanceof Wall) {
				final HousePart x = insideChild(part.getPoints().get(0), container);
				if (x instanceof Window)
					return x.getUFactor() < 0.000001 ? windowUFactor : x.getUFactor();
				if (x instanceof Door)
					return x.getUFactor() < 0.000001 ? doorUFactor : x.getUFactor();
				return container.getUFactor() < 0.000001 ? wallUFactor : container.getUFactor();
			} else if (container instanceof Roof) {
				return container.getUFactor() < 0.000001 ? roofUFactor : container.getUFactor();
			}
		}
		return part.getUFactor();
	}

	public void computeEnergyToday(final Calendar today, final double insideTemperature) {
		try {
			wallUFactor = parseUFactor(EnergyPanel.getInstance().getWallsComboBox());
			doorUFactor = parseUFactor(EnergyPanel.getInstance().getDoorsComboBox());
			windowUFactor = parseUFactor(EnergyPanel.getInstance().getWindowsComboBox());
			roofUFactor = parseUFactor(EnergyPanel.getInstance().getRoofsComboBox());
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

		final int timeStep = SolarIrradiation.getInstance().getTimeStep();
		final double[] outsideTemperatureRange = CityData.getInstance().computeOutsideTemperature(today, (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem());
		for (int minute = 0; minute < SolarIrradiation.MINUTES_OF_DAY; minute += timeStep) {
			for (final HousePart part : Scene.getInstance().getParts()) {
				final double outsideTemperature = CityData.getInstance().computeOutsideTemperatureRange(outsideTemperatureRange, minute);
				float absorption = part instanceof Window ? 0 : 1 - part.getAlbedo();
				if (part instanceof Roof) { // need to compute piece by piece for a roof
					Roof roof = (Roof) part;
					for (final Spatial child : roof.getRoofPartsRoot().getChildren()) {
						Mesh mesh = (Mesh) ((Node) child).getChild(0);
						double[] solarPotential = SolarIrradiation.getInstance().getSolarPotential(mesh);
						double solarHeat = solarPotential != null ? solarPotential[minute / timeStep] * absorption / part.getVolumetricHeatCapacity() : 0;
						double deltaT = insideTemperature - (outsideTemperature + solarHeat);
						if (part.isDrawCompleted()) {
							double uFactor = getUFactor(part);
							if (uFactor < 0.000001)
								continue;
							double heatloss = roof.computeArea(mesh) * uFactor * deltaT / 1000.0 / 60 * timeStep;
							if (heatloss > 0 && outsideTemperatureRange[0] >= 15)
								heatloss = 0;
							part.getHeatLoss()[minute / timeStep] += heatloss;
							double[] heatLossArray = SolarIrradiation.getInstance().getHeatLoss(mesh);
							if (heatLossArray != null)
								heatLossArray[minute / timeStep] += heatloss;
						}
					}
				} else {
					double solarHeat = part.getSolarPotential()[minute / timeStep] * absorption / part.getVolumetricHeatCapacity();
					double deltaT = insideTemperature - (outsideTemperature + solarHeat);
					if (part.isDrawCompleted()) {
						double uFactor = getUFactor(part);
						if (uFactor < 0.000001)
							continue;
						double heatloss = part.computeArea() * uFactor * deltaT / 1000.0 / 60 * timeStep;
						if (heatloss > 0 && outsideTemperatureRange[0] >= 15)
							heatloss = 0;
						part.getHeatLoss()[minute / timeStep] += heatloss;
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

	public static double parseUFactor(final JComboBox<String> comboBox) {
		final String valueStr = comboBox.getSelectedItem().toString();
		final int indexOfSpace = valueStr.indexOf(' ');
		return Double.parseDouble(valueStr.substring(0, indexOfSpace != -1 ? indexOfSpace : valueStr.length()));
	}

}
