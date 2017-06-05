package org.concord.energy3d.simulation;

import java.io.Serializable;

/**
 * @author Charles Xie
 *
 */
public class CspCustomPrice implements Serializable {

	private static final long serialVersionUID = 1L;

	private double mirrorUnitPrice = 100;
	private double heliostatPrice = 100;
	private double towerUnitPrice = 500;
	private int lifespan = 50;
	private double landUnitPrice = 10;

	public CspCustomPrice() {
		setDefaultValues();
	}

	public void setDefaultValues() {
		if (lifespan == 0) {
			lifespan = 50;
		}
		if (landUnitPrice == 0) {
			landUnitPrice = 10;
		}
		if (mirrorUnitPrice == 0) {
			mirrorUnitPrice = 100;
		}
		if (heliostatPrice == 0) {
			heliostatPrice = 1000;
		}
		if (towerUnitPrice == 0) {
			towerUnitPrice = 1000;
		}
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

	public void setTowerUnitPrice(final double towerUnitPrice) {
		this.towerUnitPrice = towerUnitPrice;
	}

	public double getTowerUnitPrice() {
		return towerUnitPrice;
	}

}
