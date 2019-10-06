package org.concord.energy3d.simulation;

/**
 * @author Charles Xie
 */
public class CspFinancialModel extends FinancialModel {

    private static final long serialVersionUID = 1L;

    // upfront costs
    private double heliostatUnitCost = 100;
    private double towerUnitCost = 5000;
    private double parabolicTroughUnitCost = 100;
    private double parabolicDishUnitCost = 100;
    private double fresnelReflectorUnitCost = 50;

    public CspFinancialModel() {
        super();
    }

    public void setDefaultValues() {
        if (kWhSellingPrice == 0) {
            kWhSellingPrice = 0.15;
        }
        if (lifespan == 0) {
            lifespan = 50;
        }
        if (loanInterestRate == 0) {
            loanInterestRate = 0.05;
        }
        if (cleaningCost == 0) {
            cleaningCost = 5;
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