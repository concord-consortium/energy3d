package org.concord.energy3d.simulation;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This class models the atmosphere.
 * 
 * @author Charles Xie
 *
 */
public class Atmosphere implements Serializable {

	private static final long serialVersionUID = 1L;

	private double[] dustLosses = new double[12];

	public Atmosphere() {
		Arrays.fill(dustLosses, 0.05);
	}

	// Reference: http://solarprofessional.com/articles/operations-maintenance/impacts-of-soiling-on-utility-scale-pv-system-performance
	public void setDustLoss(final double loss, final int i) {
		if (dustLosses == null) {
			dustLosses = new double[12];
		}
		dustLosses[i] = loss;
	}

	public double getDustLoss(final int i) {
		if (dustLosses == null) {
			dustLosses = new double[12];
			Arrays.fill(dustLosses, 0.05);
		}
		return dustLosses[i];
	}

	/** Vittitoe-Biggs formula for atmospheric transmittance of light (s must be in km) */
	public static double getTransmittance(final double s, final boolean haze) {
		if (haze) {
			return 0.98707 - 0.2748 * s + 0.03394 * s * s;
		}
		return 0.99326 - 0.1046 * s + 0.017 * s * s - 0.002845 * s * s * s;
	}

}
