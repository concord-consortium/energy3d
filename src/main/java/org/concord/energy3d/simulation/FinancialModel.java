package org.concord.energy3d.simulation;

import java.io.Serializable;

/**
 * @author Charles Xie
 */
public abstract class FinancialModel implements Serializable {

    private static final long serialVersionUID = 1L;

    // revenue goals
    int lifespan;
    double kWhSellingPrice;

    // operational costs
    double landRentalCost;
    double cleaningCost;
    double maintenanceCost;
    double loanInterestRate; // not percentage

    public FinancialModel() {
        setDefaultValues();
    }

    public abstract void setDefaultValues();

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

    public void setLoanInterestRate(double loanInterestRate) {
        this.loanInterestRate = loanInterestRate;
    }

    public double getLoanInterestRate() {
        return loanInterestRate;
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

}