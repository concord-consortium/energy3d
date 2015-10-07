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

	private static final double OMEGA = Math.PI / 365;
	private double thermalDiffusivity = 0.1; // the larger the thermal diffusivity is, the more the ground temperature is affected by air temperature
	private int yearlyLagInDays = 30;
	private double defaultDepth = 1;
	private int dailyLagInMinutes = 120;

	public static Ground getInstance() {
		return instance;
	}

	private Ground() {
	}

	public int getYearlyLagInDays() {
		return yearlyLagInDays;
	}

	public int getDailyLagInMinutes() {
		return dailyLagInMinutes;
	}

	public double getFloorTemperature(int day) {
		return getFloorTemperature(day, defaultDepth);
	}

	/** calculate the average floor temperature of a given day using the Kusuda formula: http://soilphysics.okstate.edu/software/SoilTemperature/document.pdf */
	public double getFloorTemperature(int day, double depth) {
		String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
		int[] loTemp = LocationData.getInstance().getLowTemperatures().get(city);
		int[] hiTemp = LocationData.getInstance().getHighTemperatures().get(city);
		int n = loTemp.length;
		double ave = 0;
		double amp = 0;
		double hiMax = -1000, hiMin = 1000, loMax = -1000, loMin = 1000;
		for (int i = 0; i < n; i++) {
			ave += hiTemp[i] + loTemp[i];
			if (hiMax < hiTemp[i])
				hiMax = hiTemp[i];
			if (loMax < loTemp[i])
				loMax = loTemp[i];
			if (hiMin > hiTemp[i])
				hiMin = hiTemp[i];
			if (loMin > loTemp[i])
				loMin = loTemp[i];
		}
		ave /= 2 * n;
		amp = 0.25 * (hiMax - hiMin + loMax - loMin);
		if (LocationData.getInstance().getLatitutes().get(city) > 0)
			return ave - amp * Math.exp(-depth * Math.sqrt(OMEGA / thermalDiffusivity)) * Math.cos(2 * OMEGA * (day - yearlyLagInDays - 0.5 * depth / Math.sqrt(OMEGA * thermalDiffusivity)));
		return ave - amp * Math.exp(-depth * Math.sqrt(OMEGA / thermalDiffusivity)) * Math.cos(Math.PI + 2 * OMEGA * (day - yearlyLagInDays - 0.5 * depth / Math.sqrt(OMEGA * thermalDiffusivity)));
	}

	public double getFloorTemperature(int day, int minute, double airTemperatrureFluctuationAmplitude) {
		return getFloorTemperature(day) + Math.exp(-defaultDepth * Math.sqrt(OMEGA / thermalDiffusivity)) * airTemperatrureFluctuationAmplitude * Math.cos((1 - (minute - dailyLagInMinutes) / 720.0) * Math.PI);
	}

	public double getFloorTemperature(int day, int minute, double airTemperatrureFluctuationAmplitude, double depth) {
		return getFloorTemperature(day, depth) + Math.exp(-depth * Math.sqrt(OMEGA / thermalDiffusivity)) * airTemperatrureFluctuationAmplitude * Math.cos((1 - (minute - dailyLagInMinutes) / 720.0) * Math.PI);
	}

}