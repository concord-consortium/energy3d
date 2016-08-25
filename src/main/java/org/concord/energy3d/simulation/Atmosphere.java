package org.concord.energy3d.simulation;

import java.io.Serializable;

/**
 * @author Charles Xie
 *
 */
public class Atmosphere implements Serializable {

	private static final long serialVersionUID = 1L;

	private double dustLoss = 0.05;

	public void setDustLoss(final double dustLoss) {
		this.dustLoss = dustLoss;
	}

	public double getDustLoss() {
		return dustLoss;
	}

	// Vittitoe-Biggs formula for atmospheric transmittance of light (s must be in km)
	public static double getTransmittance(final double s, final boolean haze) {
		if (haze) {
			return 0.98707 - 0.2748 * s + 0.03394 * s * s;
		}
		return 0.99326 - 0.1046 * s + 0.017 * s * s - 0.002845 * s * s * s;
	}

}
