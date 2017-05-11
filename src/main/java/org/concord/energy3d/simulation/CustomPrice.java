package org.concord.energy3d.simulation;

import java.io.Serializable;

/**
 * @author Charles Xie
 *
 */
public class CustomPrice implements Serializable {

	private static final long serialVersionUID = 1L;

	private double solarPanelPrice = 1000;
	private double solarPanelRackBasePrice = 50;
	private double solarPanelRackHeightPrice = 100;
	private double solarPanelHsatPrice = 100;
	private double solarPanelVsatPrice = 100;
	private double solarPanelAadatPrice = 100;
	private double mirrorUnitPrice = 100;
	private double heliostatPrice = 1000;

	public CustomPrice() {
		setDefaultValues();
	}

	public void setDefaultValues() {
		if (solarPanelPrice == 0) {
			solarPanelPrice = 1000;
		}
		if (solarPanelRackBasePrice == 0) {
			solarPanelRackBasePrice = 50;
		}
		if (solarPanelRackHeightPrice == 0) {
			solarPanelRackHeightPrice = 100;
		}
		if (solarPanelHsatPrice == 0) {
			solarPanelHsatPrice = 1000;
		}
		if (solarPanelVsatPrice == 0) {
			solarPanelVsatPrice = 1000;
		}
		if (solarPanelAadatPrice == 0) {
			solarPanelAadatPrice = 1000;
		}
		if (mirrorUnitPrice == 0) {
			mirrorUnitPrice = 100;
		}
		if (heliostatPrice == 0) {
			heliostatPrice = 1000;
		}
	}

	public void setMirrorUnitPrice(final double mirrorUnitPrice) {
		this.mirrorUnitPrice = mirrorUnitPrice;
	}

	public double getMirrorUnitPrice() {
		return mirrorUnitPrice;
	}

	public void setHeliostatPrice(final double heliostatPrice) {
		this.heliostatPrice = heliostatPrice;
	}

	public double getHeliostatPrice() {
		return heliostatPrice;
	}

	public void setSolarPanelPrice(final double solarPanelPrice) {
		this.solarPanelPrice = solarPanelPrice;
	}

	public double getSolarPanelPrice() {
		return solarPanelPrice;
	}

	public void setSolarPanelHsatPrice(final double solarPanelHsatPrice) {
		this.solarPanelHsatPrice = solarPanelHsatPrice;
	}

	public double getSolarPanelHsatPrice() {
		return solarPanelHsatPrice;
	}

	public void setSolarPanelVsatPrice(final double solarPanelVsatPrice) {
		this.solarPanelVsatPrice = solarPanelVsatPrice;
	}

	public double getSolarPanelVsatPrice() {
		return solarPanelVsatPrice;
	}

	public void setSolarPanelAadatPrice(final double solarPanelAadatPrice) {
		this.solarPanelAadatPrice = solarPanelAadatPrice;
	}

	public double getSolarPanelAadatPrice() {
		return solarPanelAadatPrice;
	}

	public void setSolarPanelRackBasePrice(final double solarPanelRackBasePrice) {
		this.solarPanelRackBasePrice = solarPanelRackBasePrice;
	}

	public double getSolarPanelRackBasePrice() {
		return solarPanelRackBasePrice;
	}

	public void setSolarPanelRackHeightPrice(final double solarPanelRackHeightPrice) {
		this.solarPanelRackHeightPrice = solarPanelRackHeightPrice;
	}

	public double getSolarPanelRackHeightPrice() {
		return solarPanelRackHeightPrice;
	}

}
