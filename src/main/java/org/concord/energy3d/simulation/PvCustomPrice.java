package org.concord.energy3d.simulation;

import java.io.Serializable;

import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Trackable;
import org.concord.energy3d.scene.Scene;

/**
 * @author Charles Xie
 *
 */
public class PvCustomPrice implements Serializable {

	private static final long serialVersionUID = 1L;

	private double solarPanelPrice = 1000;
	private double solarPanelRackBasePrice = 50;
	private double solarPanelRackHeightPrice = 100;
	private double solarPanelHsatPrice = 100;
	private double solarPanelVsatPrice = 100;
	private double solarPanelAadatPrice = 100;
	private int lifespan = 20;
	private double landUnitPrice;

	public PvCustomPrice() {
		setDefaultValues();
	}

	public void setDefaultValues() {
		if (lifespan == 0) {
			lifespan = 20;
		}
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
	}

	public double getTotalCost(final Rack r) {
		return getTotalCost(r.getSolarPanel()) * r.getNumberOfSolarPanels();
	}

	public double getTotalCost(final SolarPanel s) {
		double cost = solarPanelPrice;
		cost += solarPanelRackBasePrice;
		final double baseHeight = s.getBaseHeight() * Scene.getInstance().getAnnotationScale();
		if (baseHeight > 1) {
			cost += solarPanelRackHeightPrice * (baseHeight - 1);
		}
		switch (s.getTracker()) {
		case Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER:
			cost += solarPanelHsatPrice;
			break;
		case Trackable.VERTICAL_SINGLE_AXIS_TRACKER:
			cost += solarPanelVsatPrice;
			break;
		case Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER:
			cost += solarPanelAadatPrice;
			break;
		}
		return cost;
	}

	public void setLifespan(final int lifespan) {
		this.lifespan = lifespan;
	}

	public int getLifespan() {
		return lifespan;
	}

	public void setLandUnitPrice(final double landUnitPrice) {
		this.landUnitPrice = landUnitPrice;
	}

	public double getLandUnitPrice() {
		return landUnitPrice;
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
