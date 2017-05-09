package org.concord.energy3d.simulation;

import java.io.Serializable;

/**
 * @author Charles Xie
 *
 */
public final class CustomPrice implements Serializable {

	private static final long serialVersionUID = 1L;

	private double commercialSolarPanelPrice = 1000;
	private double residentialSolarPanelPrice = 1500;

	public CustomPrice() {
		setDefaultValues();
	}

	public void setDefaultValues() {
		if (commercialSolarPanelPrice == 0) {
			commercialSolarPanelPrice = 1000;
		}
		if (residentialSolarPanelPrice == 0) {
			residentialSolarPanelPrice = 1500;
		}
	}

	public void setCommercialSolarPanelPrice(final double commercialSolarPanelPrice) {
		this.commercialSolarPanelPrice = commercialSolarPanelPrice;
	}

	public double getCommercialSolarPanelPrice() {
		return commercialSolarPanelPrice;
	}

	public void setResidentialSolarPanelPrice(final double residentialSolarPanelPrice) {
		this.residentialSolarPanelPrice = residentialSolarPanelPrice;
	}

	public double getResidentialSolarPanelPrice() {
		return residentialSolarPanelPrice;
	}

}
