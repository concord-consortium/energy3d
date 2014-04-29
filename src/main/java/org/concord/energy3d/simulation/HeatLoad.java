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
import org.concord.energy3d.util.Util;

import com.ardor3d.math.Vector3;

public class HeatLoad {

	private final static HeatLoad instance = new HeatLoad();

	public static HeatLoad getInstance() {
		return instance;
	}

	public void computeEnergyToday(final Calendar today, final double insideTemperature) {
		final double wallUFactor, doorUFactor, windowUFactor, roofUFactor;
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

		final double[] outsideTemperatureRange;

		final int timeStep = SolarIrradiation.getInstance().getTimeStep();
		synchronized (Scene.getInstance().getParts()) {
			for (final HousePart part : Scene.getInstance().getParts())
				part.setHeatLoss(new double[1440 / timeStep]);
		}

		if (EnergyPanel.getInstance().getCityComboBox().getSelectedItem().equals(""))
			return;
		else
			outsideTemperatureRange = CityData.getInstance().computeOutsideTemperature(today, (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem());

		synchronized (Scene.getInstance().getParts()) { // without this, it sometimes throws ConcurrentModificationException and causes refresh to fail
			for (int minute = 0; minute < 1440; minute += timeStep) {
				for (final HousePart part : Scene.getInstance().getParts()) {
					final double outsideTemperature = CityData.getInstance().computeOutsideTemperatureRange(outsideTemperatureRange, minute);
					final double deltaT = insideTemperature - outsideTemperature;
					if (part.isDrawCompleted()) {
						final double uFactor;
						if (part instanceof Wall)
							uFactor = wallUFactor;
						else if (part instanceof Door)
							uFactor = doorUFactor;
						else if (part instanceof Window)
							uFactor = windowUFactor;
						else if (part instanceof Roof)
							uFactor = roofUFactor;
						else if (part instanceof Sensor) {
							if (part.getContainer() instanceof Wall) {
								HousePart x = insideChild(part.getPoints().get(0), part.getContainer());
								if (x instanceof Window)
									uFactor = windowUFactor;
								else if (x instanceof Door)
									uFactor = doorUFactor;
								else
									uFactor = wallUFactor;
							} else if (part.getContainer() instanceof Roof)
								uFactor = roofUFactor;
							else
								continue;
						} else
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

	private static HousePart insideChild(Vector3 point, HousePart parent) {
		for (HousePart x : parent.getChildren())
			if (insideRectangle(point, x.getPoints()))
				return x;
		return null;
	}

	// Note: this assumes y = 0 in all Vector3
	private static boolean insideRectangle(Vector3 point, ArrayList<Vector3> rect) {
		double x = point.getX();
		double y = point.getZ();
		double xmin = Double.MAX_VALUE;
		double xmax = -Double.MAX_VALUE;
		double ymin = Double.MAX_VALUE;
		double ymax = -Double.MAX_VALUE;
		for (Vector3 v : rect) {
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
