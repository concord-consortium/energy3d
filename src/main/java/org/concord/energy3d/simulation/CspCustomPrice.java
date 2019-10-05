package org.concord.energy3d.simulation;

import java.io.Serializable;

/**
 * @author Charles Xie
 */
public class CspCustomPrice implements Serializable {

    private static final long serialVersionUID = 1L;

    private int lifespan = 50;
    private double kWhSellingPrice = 0.15;

    private double heliostatUnitCost = 100;
    private double towerUnitCost = 5000;
    private double parabolicTroughUnitCost = 100;
    private double parabolicDishUnitCost = 100;
    private double fresnelReflectorUnitCost = 50;

    private double landRentalCost;
    private double cleaningCost = 10;
    private double maintenanceCost = 1;

    public CspCustomPrice() {
        setDefaultValues();
    }

    public void setDefaultValues() {
        if (kWhSellingPrice == 0) {
            kWhSellingPrice = 0.15;
        }
        if (lifespan == 0) {
            lifespan = 50;
        }
        if (cleaningCost == 0) {
            cleaningCost = 10;
        }
        if (maintenanceCost == 0) {
            maintenanceCost = 1;
        }
        if (heliostatUnitCost == 0) {
            heliostatUnitCost = 100;
        }
        if (towerUnitCost == 0) {
            towerUnitCost = 5000;
        }
        if (parabolicTroughUnitCost == 0) {
            parabolicTroughUnitCost = 100;
        }
        if (parabolicDishUnitCost == 0) {
            parabolicDishUnitCost = 100;
        }
        if (fresnelReflectorUnitCost == 0) {
            fresnelReflectorUnitCost = 50;
        }
    }

    public void setLifespan(final int lifespan) {
        this.lifespan = lifespan;
    }

    public int getLifespan() {
        return lifespan;
    }

    public void setkWhSellingPrice(final double kWhSellingPrice) {
        this.kWhSellingPrice = kWhSellingPrice;
    }

    public double getkWhSellingPrice() {
        return kWhSellingPrice;
    }

    public void setLandRentalCost(final double landRentalCost) {
        this.landRentalCost = landRentalCost;
    }

    public double getLandRentalCost() {
        return landRentalCost;
    }

    public void setCleaningCost(final double cleaningCost) {
        this.cleaningCost = cleaningCost;
    }

    public double getCleaningCost() {
        return cleaningCost;
    }

    public void setMaintenanceCost(final double maintenanceCost) {
        this.maintenanceCost = maintenanceCost;
    }

    public double getMaintenanceCost() {
        return maintenanceCost;
    }

    public void setParabolicTroughUnitCost(final double parabolicTroughUnitCost) {
        this.parabolicTroughUnitCost = parabolicTroughUnitCost;
    }

    public double getParabolicTroughUnitCost() {
        return parabolicTroughUnitCost;
    }

    public void setParabolicDishUnitCost(final double parabolicDishUnitCost) {
        this.parabolicDishUnitCost = parabolicDishUnitCost;
    }

    public double getParabolicDishUnitCost() {
        return parabolicDishUnitCost;
    }

    public void setFresnelReflectorUnitCost(final double fresnelReflectorUnitCost) {
        this.fresnelReflectorUnitCost = fresnelReflectorUnitCost;
    }

    public double getFresnelReflectorUnitCost() {
        return fresnelReflectorUnitCost;
    }

    public void setHeliostatUnitCost(final double heliostatUnitCost) {
        this.heliostatUnitCost = heliostatUnitCost;
    }

    public double getHeliostatUnitCost() {
        return heliostatUnitCost;
    }

    public void setTowerUnitCost(final double towerUnitCost) {
        this.towerUnitCost = towerUnitCost;
    }

    public double getTowerUnitCost() {
        return towerUnitCost;
    }

}