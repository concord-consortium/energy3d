package org.concord.energy3d.simulation;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Charles Xie
 *
 */

public class Thermostat implements Serializable {

	private static final long serialVersionUID = 1L;

	private int[][][] temperatures;

	public Thermostat() {
		init();
	}

	private void init() {
		temperatures = new int[12][7][24];
		for (int i = 0; i < 7; i++) {
			Arrays.fill(temperatures[0][i], 20);
			Arrays.fill(temperatures[1][i], 20);
			Arrays.fill(temperatures[2][i], 20);
			Arrays.fill(temperatures[3][i], 21);
			Arrays.fill(temperatures[4][i], 21);
			Arrays.fill(temperatures[5][i], 22);
			Arrays.fill(temperatures[6][i], 22);
			Arrays.fill(temperatures[7][i], 22);
			Arrays.fill(temperatures[8][i], 21);
			Arrays.fill(temperatures[9][i], 21);
			Arrays.fill(temperatures[10][i], 20);
			Arrays.fill(temperatures[11][i], 20);
		}
	}

	/** monthOfYear, dayOfWeek, and hourOfDay all starts from zero. */
	public void setTemperature(int monthOfYear, int dayOfWeek, int hourOfDay, int temperature) {
		temperatures[monthOfYear][dayOfWeek][hourOfDay] = temperature;
	}

	/** monthOfYear, dayOfWeek, and hourOfDay all starts from zero. */
	public int getTemperature(int monthOfYear, int dayOfWeek, int hourOfDay) {
		if (temperatures == null) // backward compatibility with object serialization
			init();
		return temperatures[monthOfYear][dayOfWeek][hourOfDay];
	}

}
