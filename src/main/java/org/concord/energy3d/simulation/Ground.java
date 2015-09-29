package org.concord.energy3d.simulation;

import org.concord.energy3d.gui.EnergyPanel;

/**
 * The science of ground
 * 
 * @author Charles Xie
 * 
 */

public class Ground {

	private static final Ground instance = new Ground();

	private double thermalDiffusivity = 1;
	private int yearlyLagInDays = 30;
	private double depth = 1;
	private int dailyLagInMinutes = 120;

	public static Ground getInstance() {
		return instance;
	}

	private Ground() {
	}

	/** calculate the average floor temperature of a given day using the Kasuda formula: http://soilphysics.okstate.edu/software/SoilTemperature/document.pdf */
	public double getFloorTemperature(int day) {
		String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
		int[] loTemp = LocationData.getInstance().getLowTemperatures().get(city);
		int[] hiTemp = LocationData.getInstance().getHighTemperatures().get(city);
		double lo = 0;
		for (int x : loTemp) {
			lo += x;
		}
		lo /= loTemp.length;
		double hi = 0;
		for (int x : hiTemp) {
			hi += x;
		}
		hi /= hiTemp.length;
		double ave = 0.5 * (hi + lo);
		double amp = 0.5 * (hi - lo);
		double omg = Math.PI / 365;
		return ave - amp * Math.exp(-depth * Math.sqrt(omg / thermalDiffusivity)) * Math.cos(2 * omg * (day - yearlyLagInDays - 0.5 * depth / Math.sqrt(omg * thermalDiffusivity)));
	}

	public double getFloorTemperature(int day, int minute, double airTemperatrureFluctuationAmplitude) {
		return getFloorTemperature(day) + Math.exp(-depth / thermalDiffusivity) * airTemperatrureFluctuationAmplitude * Math.cos((1 - (minute - dailyLagInMinutes) / 720.0) * Math.PI);
	}

}
