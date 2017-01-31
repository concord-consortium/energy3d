package org.concord.energy3d.simulation;

import java.io.Serializable;
import java.util.Arrays;

import org.concord.energy3d.gui.EnergyPanel;

/**
 * The science of ground
 * 
 * @author Charles Xie
 * 
 */

public class Ground implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final double OMEGA_YEAR = Math.PI / 182.5; // the annual cycle is 365 days
	private static final double OMEGA_DAY = Math.PI / 720.0; // the daily cycle is 1440 minutes

	private double albedo = 0.3;
	private double thermalDiffusivity = 0.05; // the larger the thermal diffusivity is, the more the ground temperature is affected by air temperature, unit: m^2/s
	private double foundationDepth = 1;

	private final int yearlyLagInDays = 30;
	private final int dailyLagInMinutes = 120;

	private double[] snowReflectionFactors = new double[12]; // snow on the ground can enhance the diffusion solar radiation, a factor between 0 and 0.2 (20% of increase of ground albedo)

	public Ground() {
		Arrays.fill(snowReflectionFactors, 0);
	}

	public void setSnowReflectionFactor(final double factor, final int i) {
		if (snowReflectionFactors == null) {
			snowReflectionFactors = new double[12];
		}
		snowReflectionFactors[i] = factor;
	}

	public double getSnowReflectionFactor(final int i) {
		if (snowReflectionFactors == null) {
			snowReflectionFactors = new double[12];
			Arrays.fill(snowReflectionFactors, 0);
		}
		return snowReflectionFactors[i];
	}

	public void setAlbedo(final double albedo) {
		this.albedo = albedo;
	}

	public double getAlbedo() {
		return albedo;
	}

	public double getAdjustedAlbedo(final int month) {
		if (snowReflectionFactors == null) {
			snowReflectionFactors = new double[12];
			Arrays.fill(snowReflectionFactors, 0);
			return albedo;
		}
		double a = albedo * (1 + snowReflectionFactors[month]);
		if (a > 1) {
			a = 1;
		}
		return a;
	}

	public void setThermalDiffusivity(final double thermalDiffusivity) {
		this.thermalDiffusivity = thermalDiffusivity;
	}

	public double getThermalDiffusivity() {
		return thermalDiffusivity;
	}

	public void setFoundationDepth(final double foundationDepth) {
		this.foundationDepth = foundationDepth;
	}

	public double getFoundationDepth() {
		return foundationDepth;
	}

	public int getYearlyLagInDays() {
		return yearlyLagInDays;
	}

	public int getDailyLagInMinutes() {
		return dailyLagInMinutes;
	}

	public double getTemperatureOnDay(final int day) {
		return getTemperatureOnDay(day, foundationDepth);
	}

	/** calculate the average floor temperature of a given day using the Kusuda formula: http://soilphysics.okstate.edu/software/SoilTemperature/document.pdf */
	public double getTemperatureOnDay(final int day, final double depth) {
		final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
		final int[] loTemp = LocationData.getInstance().getLowTemperatures().get(city);
		final int[] hiTemp = LocationData.getInstance().getHighTemperatures().get(city);
		final int n = loTemp.length;
		double ave = 0;
		double amp = 0;
		double hiMax = -1000, hiMin = 1000, loMax = -1000, loMin = 1000;
		for (int i = 0; i < n; i++) {
			ave += hiTemp[i] + loTemp[i];
			if (hiMax < hiTemp[i]) {
				hiMax = hiTemp[i];
			}
			if (loMax < loTemp[i]) {
				loMax = loTemp[i];
			}
			if (hiMin > hiTemp[i]) {
				hiMin = hiTemp[i];
			}
			if (loMin > loTemp[i]) {
				loMin = loTemp[i];
			}
		}
		ave /= 2 * n;
		amp = 0.25 * (hiMax - hiMin + loMax - loMin);
		final double d2 = depth * Math.sqrt(OMEGA_YEAR / (2.0 * thermalDiffusivity));
		if (LocationData.getInstance().getLatitutes().get(city) > 0) {
			return ave - amp * Math.exp(-d2) * Math.cos(OMEGA_YEAR * (day - yearlyLagInDays) - d2);
		}
		return ave - amp * Math.exp(-d2) * Math.cos(Math.PI + OMEGA_YEAR * (day - yearlyLagInDays) - d2);
	}

	public double getTemperatureMinuteOfDay(final int day, final int minute, final double airTemperatrureFluctuationAmplitudeOfDay) {
		return getTemperatureOnDay(day) - Math.exp(-foundationDepth * Math.sqrt(OMEGA_DAY / (2.0 * thermalDiffusivity))) * airTemperatrureFluctuationAmplitudeOfDay * Math.cos(OMEGA_DAY * (minute - dailyLagInMinutes));
	}

	public double getTemperatureMinuteOfDay(final int day, final int minute, final double airTemperatrureFluctuationAmplitudeOfDay, final double depth) {
		return getTemperatureOnDay(day, depth) - Math.exp(-depth * Math.sqrt(OMEGA_DAY / (2.0 * thermalDiffusivity))) * airTemperatrureFluctuationAmplitudeOfDay * Math.cos(OMEGA_DAY * (minute - dailyLagInMinutes));
	}

}