package org.concord.energy3d.simulation;

import java.io.Serializable;

/**
 * @author Charles Xie
 *
 */
public final class Price implements Serializable {

	private static final long serialVersionUID = 1L;

	private double solarPanelPrice = 1000;

	public Price() {
		setDefaultValues();
	}

	public void setDefaultValues() {
		if (solarPanelPrice == 0) {
			solarPanelPrice = 1000;
		}
	}

	public void setSolarPanelPrice(final double solarPanelPrice) {
		this.solarPanelPrice = solarPanelPrice;
	}

	public double getSolarPanelPrice() {
		return solarPanelPrice;
	}

}
