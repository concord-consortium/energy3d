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
	private boolean windowToFloorRatioEnabled = false;
	private boolean heightEnabled = false;
	private boolean areaEnabled = false;

	private int maximumBudget = 200000;
	private double minimumWindowToFloorRatio = 0.15;
	private double maximumWindowToFloorRatio = 0.25;
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
				EnergyPanel.getInstance().getConstructionCostGraph().updateBudget();
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
				EnergyPanel.getInstance().getConstructionCostGraph().updateBudget();
			}
		});
	}

	public int getMaximumBudget() {
		return maximumBudget;
	}

	public void setWindowToFloorRatioEnabled(boolean windowToFloorRatioEnabled) {
		this.windowToFloorRatioEnabled = windowToFloorRatioEnabled;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().getSpecsPanel().updateWindowToFloorRatio();
			}
		});
	}

	public boolean isWindowToFloorRatioEnabled() {
		return windowToFloorRatioEnabled;
	}

	public void setMinimumWindowToFloorRatio(double minimumWindowToFloorRatio) {
		this.minimumWindowToFloorRatio = minimumWindowToFloorRatio;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().getSpecsPanel().updateWindowToFloorRatio();
			}
		});
	}

	public double getMinimumWindowToFloorRatio() {
		return minimumWindowToFloorRatio;
	}

	public void setMaximumWindowToFloorRatio(double maximumWindowToFloorRatio) {
		this.maximumWindowToFloorRatio = maximumWindowToFloorRatio;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().getSpecsPanel().updateWindowToFloorRatio();
			}
		});
	}

	public double getMaximumWindowToFloorRatio() {
		return maximumWindowToFloorRatio;
	}

	public void setHeightEnabled(boolean heightEnabled) {
		this.heightEnabled = heightEnabled;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().getSpecsPanel().updateHeight();
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
				EnergyPanel.getInstance().getSpecsPanel().updateHeight();
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
				EnergyPanel.getInstance().getSpecsPanel().updateHeight();
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
				EnergyPanel.getInstance().getSpecsPanel().updateArea();
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
				EnergyPanel.getInstance().getSpecsPanel().updateArea();
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
				EnergyPanel.getInstance().getSpecsPanel().updateArea();
			}
		});
	}

	public double getMinimumArea() {
		return minimumArea;
	}

}