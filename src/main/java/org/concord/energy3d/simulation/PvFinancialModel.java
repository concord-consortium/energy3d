package org.concord.energy3d.simulation;

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
public class PvFinancialModel extends FinancialModel {

    private static final long serialVersionUID = 1L;

    // upfront costs
    private HashMap<String, Double> pvModelCosts;
    private double customSolarPanelCost = 1000;
    private double solarPanelRackBaseCost = 20;
    private double solarPanelRackHeightCost = 20;
    private double solarPanelHsatCost = 10;
    private double solarPanelVsatCost = 10;
    private double solarPanelAadatCost = 15;

    public PvFinancialModel() {
        super();
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

    double calculateROI(double landArea, double numberOfSolarPanels, double annualOutput) {
        double upfrontCost = PvProjectCost.getTotalUpFrontCost();
        double roi = annualOutput * lifespan * kWhSellingPrice;
        roi -= landRentalCost * lifespan * landArea;
        roi -= (cleaningCost + maintenanceCost) * lifespan * numberOfSolarPanels;
        roi -= loanInterestRate * lifespan * upfrontCost;
        roi /= upfrontCost;
        return roi * 100; // convert to percentage
    }

    private double getSolarPanelCost(SolarPanel s) {
        final String modelName = s.getModelName();
        if ("Custom".equals(modelName)) {
            return customSolarPanelCost;
        }
        final Double d = pvModelCosts.get(modelName);
        if (d != null) {
            return d;
        }
        return 0;
    }

    private double getTrackerCost(Trackable t) {
        double cost = 0;
        switch (t.getTracker()) {
            case Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER:
                cost = solarPanelHsatCost;
                break;
            case Trackable.VERTICAL_SINGLE_AXIS_TRACKER:
                cost = solarPanelVsatCost;
                break;
            case Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER:
                cost = solarPanelAadatCost;
                break;
        }
        return cost;
    }

    private boolean onFlatSurface(HousePart p) {
        boolean flat = true;
        final HousePart container = p.getContainer();
        if (container instanceof Roof) {
            final Roof roof = (Roof) container;
            flat = roof.getHeight() < 0.1;
        }
        return flat;
    }

    public double getCost(final Rack r) {
        double cost = getSolarPanelCost(r.getSolarPanel()) + solarPanelRackBaseCost;
        if (onFlatSurface(r)) {
            final double baseHeight = r.getPoleHeight() * Scene.getInstance().getScale();
            if (baseHeight > 1) {
                cost += solarPanelRackHeightCost * (baseHeight - 1);
            }
            cost += getTrackerCost(r);
        }
        return cost * r.getNumberOfSolarPanels();
    }

    public double getCost(final SolarPanel s) {
        double cost = getSolarPanelCost(s) + solarPanelRackBaseCost;
        if (onFlatSurface(s)) {
            final double baseHeight = s.getPoleHeight() * Scene.getInstance().getScale();
            if (baseHeight > 1) {
                cost += solarPanelRackHeightCost * (baseHeight - 1);
            }
            cost += getTrackerCost(s);
        }
        return cost;
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