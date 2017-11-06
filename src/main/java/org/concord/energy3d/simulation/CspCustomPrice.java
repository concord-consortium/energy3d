package org.concord.energy3d.simulation;

import java.io.Serializable;

/**
 * @author Charles Xie
 *
 */
public class CspCustomPrice implements Serializable {

	private static final long serialVersionUID = 1L;

	private double heliostatUnitPrice = 100;
	private double towerUnitPrice = 500;
	private double parabolicTroughUnitPrice = 100;
	private double parabolicDishUnitPrice = 100;
	private double fresnelReflectorUnitPrice = 50;
	private int lifespan = 50;
	private double landUnitPrice;

	public CspCustomPrice() {
		setDefaultValues();
	}

	public void setDefaultValues() {
		if (lifespan == 0) {
			lifespan = 50;
		}
		if (heliostatUnitPrice == 0) {
			heliostatUnitPrice = 100;
		}
		if (towerUnitPrice == 0) {
			towerUnitPrice = 1000;
		}
		if (parabolicTroughUnitPrice == 0) {
			parabolicTroughUnitPrice = 100;
		}
		if (parabolicDishUnitPrice == 0) {
			parabolicDishUnitPrice = 100;
		}
		if (fresnelReflectorUnitPrice == 0) {
			fresnelReflectorUnitPrice = 50;
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

	public void setParabolicTroughUnitPrice(final double parabolicTroughUnitPrice) {
		this.parabolicTroughUnitPrice = parabolicTroughUnitPrice;
	}

	public double getParabolicTroughUnitPrice() {
		return parabolicTroughUnitPrice;
	}

	public void setParabolicDishUnitPrice(final double parabolicDishUnitPrice) {
		this.parabolicDishUnitPrice = parabolicDishUnitPrice;
	}

	public double getParabolicDishUnitPrice() {
		return parabolicDishUnitPrice;
	}

	public void setFresnelReflectorUnitPrice(final double fresnelReflectorUnitPrice) {
		this.fresnelReflectorUnitPrice = fresnelReflectorUnitPrice;
	}

	public double getFresnelReflectorUnitPrice() {
		return fresnelReflectorUnitPrice;
	}

	public void setHeliostatUnitPrice(final double heliostatUnitPrice) {
		this.heliostatUnitPrice = heliostatUnitPrice;
	}

	public double getHeliostatUnitPrice() {
		return heliostatUnitPrice;
	}

	public void setTowerUnitPrice(final double towerUnitPrice) {
		this.towerUnitPrice = towerUnitPrice;
	}

	public double getTowerUnitPrice() {
		return towerUnitPrice;
	}

}
