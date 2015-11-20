package org.concord.energy3d.simulation;

import java.io.Serializable;
import java.util.Calendar;

/**
 * @author Charles Xie
 *
 */

public class Thermostat implements Serializable {

	private static final long serialVersionUID = 1L;

	private int[] monthlyTemperatures;

	public Thermostat() {
		monthlyTemperatures = new int[12];
		monthlyTemperatures[0] = 20;
		monthlyTemperatures[1] = 20;
		monthlyTemperatures[2] = 20;
		monthlyTemperatures[3] = 20;
		monthlyTemperatures[4] = 21;
		monthlyTemperatures[5] = 22;
		monthlyTemperatures[6] = 22;
		monthlyTemperatures[7] = 22;
		monthlyTemperatures[8] = 22;
		monthlyTemperatures[9] = 22;
		monthlyTemperatures[10] = 21;
		monthlyTemperatures[11] = 20;
	}

	public void setTemperature(int month, int temperature) {
		if (month < Calendar.JANUARY || month > Calendar.DECEMBER)
			return;
		monthlyTemperatures[month] = temperature;
	}

	public int getTemperature(int month) {
		if (month < Calendar.JANUARY || month > Calendar.DECEMBER)
			return 20;
		return monthlyTemperatures[month];
	}

	public void setMonthlyTemperature(int[] monthlyTemperature) {
		this.monthlyTemperatures = monthlyTemperature;
	}

	public int[] getMonthlyTemperature() {
		return monthlyTemperatures;
	}

}
