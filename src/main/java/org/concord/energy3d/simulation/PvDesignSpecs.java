package org.concord.energy3d.simulation;

import java.awt.EventQueue;
import java.io.Serializable;

import org.concord.energy3d.gui.EnergyPanel;

/**
 * This class defines the design specifications for a PV array.
 *
 * @author Charles Xie
 */
public class PvDesignSpecs implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean budgetEnabled;
    private int maximumBudget = 1000000;

    private boolean numberOfSolarPanelsEnabled;
    private int maximumNumberOfSolarPanels = 100000;

    public PvDesignSpecs() {
    }

    // fix the serialization problem (that sets all unset values to zero)

    public void setDefaultValues() {

        if (maximumBudget == 0) {
            maximumBudget = 1000000;
        }

        if (maximumNumberOfSolarPanels == 0) {
            maximumNumberOfSolarPanels = 100000;
        }

    }

    // budget

    public void setBudgetEnabled(final boolean budgetEnabled) {
        this.budgetEnabled = budgetEnabled;
        EventQueue.invokeLater(() -> {
            EnergyPanel.getInstance().getPvProjectCostGraphForZone().updateBudget();
            EnergyPanel.getInstance().getPvProjectInfoForZone().updateBudgetMaximum();
        });
    }

    public boolean isBudgetEnabled() {
        return budgetEnabled;
    }

    public void setMaximumBudget(final int maximumBudget) {
        this.maximumBudget = maximumBudget;
        EventQueue.invokeLater(() -> {
            EnergyPanel.getInstance().getPvProjectCostGraphForZone().updateBudget();
            EnergyPanel.getInstance().getPvProjectInfoForZone().updateBudgetMaximum();
        });
    }

    public int getMaximumBudget() {
        return maximumBudget;
    }

    public void setNumberOfSolarPanelsEnabled(final boolean numberOfSolarPanelsEnabled) {
        this.numberOfSolarPanelsEnabled = numberOfSolarPanelsEnabled;
        EventQueue.invokeLater(() -> EnergyPanel.getInstance().getPvProjectInfoForZone().updateSolarPanelNumberMaximum());
    }

    public boolean isNumberOfSolarPanelsEnabled() {
        return numberOfSolarPanelsEnabled;
    }

    public void setMaximumNumberOfSolarPanels(final int maximumNumberOfSolarPanels) {
        this.maximumNumberOfSolarPanels = maximumNumberOfSolarPanels;
        EventQueue.invokeLater(() -> EnergyPanel.getInstance().getPvProjectInfoForZone().updateSolarPanelNumberMaximum());
    }

    public int getMaximumNumberOfSolarPanels() {
        return maximumNumberOfSolarPanels;
    }

}