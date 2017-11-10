package org.concord.energy3d.simulation;

import java.awt.EventQueue;
import java.io.Serializable;

import org.concord.energy3d.gui.EnergyPanel;

/**
 * This class defines the design specifications for a CSP power tower plant.
 * 
 * @author Charles Xie
 * 
 */
public class CspDesignSpecs implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean budgetEnabled;
	private int maximumBudget = 10000000;

	private boolean numberOfMirrorsEnabled;
	private int maximumNumberOfMirrors = 100000;

	private boolean numberOfParabolicTroughsEnabled;
	private int maximumNumberOfParabolicTroughs = 10000;

	public CspDesignSpecs() {
	}

	// fix the serialization problem (that sets all unset values to zero)

	public void setDefaultValues() {
		if (maximumBudget == 0) {
			maximumBudget = 10000000;
		}
		if (maximumNumberOfMirrors == 0) {
			maximumNumberOfMirrors = 100000;
		}
		if (maximumNumberOfParabolicTroughs == 0) {
			maximumNumberOfParabolicTroughs = 10000;
		}
	}

	// budget

	public void setBudgetEnabled(final boolean budgetEnabled) {
		this.budgetEnabled = budgetEnabled;
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				EnergyPanel.getInstance().getCspProjectCostGraph().updateBudget();
				EnergyPanel.getInstance().getCspStationInfoPanel().updateBudgetMaximum();
			}
		});
	}

	public boolean isBudgetEnabled() {
		return budgetEnabled;
	}

	public void setMaximumBudget(final int maximumBudget) {
		this.maximumBudget = maximumBudget;
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				EnergyPanel.getInstance().getCspProjectCostGraph().updateBudget();
				EnergyPanel.getInstance().getCspStationInfoPanel().updateBudgetMaximum();
			}
		});
	}

	public int getMaximumBudget() {
		return maximumBudget;
	}

	public void setNumberOfMirrorsEnabled(final boolean numberOfMirrorsEnabled) {
		this.numberOfMirrorsEnabled = numberOfMirrorsEnabled;
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				EnergyPanel.getInstance().getCspStationInfoPanel().updateMirrorNumberMaximum();
			}
		});
	}

	public boolean isNumberOfMirrorsEnabled() {
		return numberOfMirrorsEnabled;
	}

	public void setMaximumNumberOfMirrors(final int maximumNumberOfMirrors) {
		this.maximumNumberOfMirrors = maximumNumberOfMirrors;
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				EnergyPanel.getInstance().getCspStationInfoPanel().updateMirrorNumberMaximum();
			}
		});
	}

	public int getMaximumNumberOfMirrors() {
		return maximumNumberOfMirrors;
	}

	public void setNumberOfParabolicTroughsEnabled(final boolean numberOfParabolicTroughsEnabled) {
		this.numberOfParabolicTroughsEnabled = numberOfParabolicTroughsEnabled;
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				EnergyPanel.getInstance().getCspStationInfoPanel().updateParabolicTroughNumberMaximum();
			}
		});
	}

	public boolean isNumberOfParabolicTroughsEnabled() {
		return numberOfParabolicTroughsEnabled;
	}

	public void setMaximumNumberOfParabolicTroughs(final int maximumNumberOfParabolicTroughs) {
		this.maximumNumberOfParabolicTroughs = maximumNumberOfParabolicTroughs;
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				EnergyPanel.getInstance().getCspStationInfoPanel().updateParabolicTroughNumberMaximum();
			}
		});
	}

	public int getMaximumNumberOfParabolicTroughs() {
		return maximumNumberOfParabolicTroughs;
	}

}