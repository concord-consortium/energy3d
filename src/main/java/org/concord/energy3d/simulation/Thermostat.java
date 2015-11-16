package org.concord.energy3d.simulation;

import java.io.Serializable;

/**
 * @author Charles Xie
 *
 */

public class Thermostat implements Serializable {

	private static final long serialVersionUID = 1L;

	private int temperature = 20;

	public void setTemperature(int temperature) {
		this.temperature = temperature;
	}

	public int getTemperature() {
		return temperature;
	}

}
