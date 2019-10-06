package org.concord.energy3d.simulation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Trackable;
import org.concord.energy3d.scene.Scene;

/**
 * @author Charles Xie
 */
public class PvFinancialModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private int lifespan = 20;
    private double kWhSellingPrice = 0.1;

    private double customSolarPanelCost = 1000;
    private double solarPanelRackBaseCost = 20;
    private double solarPanelRackHeightCost = 20;
    private double solarPanelHsatCost = 10;
    private double solarPanelVsatCost = 10;
    private double solarPanelAadatCost = 15;

    private double landRentalCost;
    private double cleaningCost = 2;
    private double maintenanceCost = 1;
    private double loanInterestRate = 0.05; // not percentage

    private HashMap<String, Double> pvModelCosts;

    public PvFinancialModel() {
        setDefaultValues();
    }

    public void setDefaultValues() {
        if (kWhSellingPrice == 0) {
            kWhSellingPrice = 0.1;
        }
        if (lifespan == 0) {
            lifespan = 20;
        }
        if (loanInterestRate == 0) {
            loanInterestRate = 0.05;
        }
        if (customSolarPanelCost == 0) {
            customSolarPanelCost = 1000;
        }
        if (cleaningCost == 0) {
            cleaningCost = 2;
        }
        if (maintenanceCost == 0) {
            maintenanceCost = 1;
        }
        if (solarPanelRackBaseCost == 0) {
            solarPanelRackBaseCost = 20;
        }
        if (solarPanelRackHeightCost == 0) {
            solarPanelRackHeightCost = 20;
        }
        if (solarPanelHsatCost == 0) {
            solarPanelHsatCost = 10;
        }
        if (solarPanelVsatCost == 0) {
            solarPanelVsatCost = 10;
        }
        if (solarPanelAadatCost == 0) {
            solarPanelAadatCost = 15;
        }
        if (pvModelCosts == null) {
            pvModelCosts = new HashMap<>();
            final Map<String, PvModuleSpecs> modules = PvModulesData.getInstance().getModules();
            for (final String key : modules.keySet()) {
                pvModelCosts.put(key, modules.get(key).getPrice());
            }
        }
    }

    public double calculateROI(double landArea, double numberOfSolarPanels, double annualOutput) {
        double upfrontCost = PvProjectCost.getTotalSolarPanelCost();
        double roi = annualOutput * lifespan * kWhSellingPrice;
        roi -= landRentalCost * lifespan * landArea;
        roi -= (cleaningCost + maintenanceCost) * lifespan * numberOfSolarPanels;
        roi -= loanInterestRate * lifespan * upfrontCost;
        roi /= upfrontCost;
        return roi * 100; // convert to percentage
    }

    public double getCost(final Rack r) {
        final String modelName = r.getSolarPanel().getModelName();
        double cost = 0;
        if ("Custom".equals(modelName)) {
            cost = customSolarPanelCost;
        } else {
            final Double d = pvModelCosts.get(modelName);
            if (d != null) {
                cost = d;
            }
        }
        cost += solarPanelRackBaseCost;
        boolean flat;
        final HousePart container = r.getContainer();
        if (container instanceof Roof) {
            final Roof roof = (Roof) container;
            flat = roof.getHeight() < 0.1;
        } else {
            flat = true;
        }
        if (flat) {
            final double baseHeight = r.getPoleHeight() * Scene.getInstance().getScale();
            if (baseHeight > 1) {
                cost += solarPanelRackHeightCost * (baseHeight - 1);
            }
            switch (r.getTracker()) {
                case Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER:
                    cost += solarPanelHsatCost;
                    break;
                case Trackable.VERTICAL_SINGLE_AXIS_TRACKER:
                    cost += solarPanelVsatCost;
                    break;
                case Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER:
                    cost += solarPanelAadatCost;
                    break;
            }
        }
        return cost * r.getNumberOfSolarPanels();
    }

    public double getCost(final SolarPanel s) {
        final String modelName = s.getModelName();
        double cost = 0;
        if ("Custom".equals(modelName)) {
            cost = customSolarPanelCost;
        } else {
            final Double d = pvModelCosts.get(modelName);
            if (d != null) {
                cost = d;
            }
        }
        cost += solarPanelRackBaseCost;
        final double baseHeight = s.getPoleHeight() * Scene.getInstance().getScale();
        if (baseHeight > 1) {
            cost += solarPanelRackHeightCost * (baseHeight - 1);
        }
        switch (s.getTracker()) {
            case Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER:
                cost += solarPanelHsatCost;
                break;
            case Trackable.VERTICAL_SINGLE_AXIS_TRACKER:
                cost += solarPanelVsatCost;
                break;
            case Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER:
                cost += solarPanelAadatCost;
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

    public void setkWhSellingPrice(final double kWhSellPrice) {
        this.kWhSellingPrice = kWhSellPrice;
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

    public void setCustomSolarPanelCost(final double customSolarPanelCost) {
        this.customSolarPanelCost = customSolarPanelCost;
    }

    public double getCustomSolarPanelCost() {
        return customSolarPanelCost;
    }

    public void setSolarPanelHsatCost(final double solarPanelHsatCost) {
        this.solarPanelHsatCost = solarPanelHsatCost;
    }

    public double getSolarPanelHsatCost() {
        return solarPanelHsatCost;
    }

    public void setSolarPanelVsatCost(final double solarPanelVsatCost) {
        this.solarPanelVsatCost = solarPanelVsatCost;
    }

    public double getSolarPanelVsatCost() {
        return solarPanelVsatCost;
    }

    public void setSolarPanelAadatCost(final double solarPanelAadatCost) {
        this.solarPanelAadatCost = solarPanelAadatCost;
    }

    public double getSolarPanelAadatCost() {
        return solarPanelAadatCost;
    }

    public void setSolarPanelRackBaseCost(final double solarPanelRackBaseCost) {
        this.solarPanelRackBaseCost = solarPanelRackBaseCost;
    }

    public double getSolarPanelRackBaseCost() {
        return solarPanelRackBaseCost;
    }

    public void setSolarPanelRackHeightCost(final double solarPanelRackHeightCost) {
        this.solarPanelRackHeightCost = solarPanelRackHeightCost;
    }

    public double getSolarPanelRackHeightCost() {
        return solarPanelRackHeightCost;
    }

    public void setPvModelCost(final String model, final double cost) {
        pvModelCosts.put(model, cost);
    }

    public double getPvModelCost(final String model) {
        final Double a = pvModelCosts.get(model);
        if (a != null) {
            return a;
        }
        // model is new, not present in the stored prices, so we must add it here
        final double x = PvModulesData.getInstance().getModules().get(model).getPrice();
        pvModelCosts.put(model, x);
        return x;
    }

}