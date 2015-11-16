package org.concord.energy3d.simulation;

import java.awt.EventQueue;
import java.io.Serializable;

import org.concord.energy3d.gui.EnergyPanel;

/**
 * This class defines the design specifications.
 * 
 * @author Charles Xie
 * 
 */
public class DesignSpecs implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean budgetEnabled = true;
	private boolean heightEnabled = false;
	private boolean areaEnabled = false;

	private int maximumBudget = 200000;
	private double minimumHeight = 8;
	private double maximumHeight = 10;
	private double minimumArea = 100;
	private double maximumArea = 150;

	public DesignSpecs() {
	}

	public void setBudgetEnabled(boolean budgetEnabled) {
		this.budgetEnabled = budgetEnabled;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().updateBudgetBar();
			}
		});
	}

	public boolean isBudgetEnabled() {
		return budgetEnabled;
	}

	public void setMaximumBudget(int maximumBudget) {
		this.maximumBudget = maximumBudget;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().updateBudgetBar();
			}
		});
	}

	public int getMaximumBudget() {
		return maximumBudget;
	}

	public void setHeightEnabled(boolean heightEnabled) {
		this.heightEnabled = heightEnabled;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().updateHeightBar();
			}
		});
	}

	public boolean isHeightEnabled() {
		return heightEnabled;
	}

	public void setMaximumHeight(double maximumHeight) {
		this.maximumHeight = maximumHeight;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().updateHeightBar();
			}
		});
	}

	public double getMaximumHeight() {
		return maximumHeight;
	}

	public void setMinimumHeight(double minimumHeight) {
		this.minimumHeight = minimumHeight;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().updateHeightBar();
			}
		});
	}

	public double getMinimumHeight() {
		return minimumHeight;
	}

	public void setAreaEnabled(boolean areaEnabled) {
		this.areaEnabled = areaEnabled;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().updateAreaBar();
			}
		});
	}

	public boolean isAreaEnabled() {
		return areaEnabled;
	}

	public void setMaximumArea(double maximumArea) {
		this.maximumArea = maximumArea;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().updateAreaBar();
			}
		});
	}

	public double getMaximumArea() {
		return maximumArea;
	}

	public void setMinimumArea(double minimumArea) {
		this.minimumArea = minimumArea;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().updateAreaBar();
			}
		});
	}

	public double getMinimumArea() {
		return minimumArea;
	}

}