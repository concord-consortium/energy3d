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

	private boolean budgetEnabled;
	private int maximumBudget = 200000;

	private boolean numberOfSolarPanelsEnabled;
	private int minimumNumberOfSolarPanels = 0;
	private int maximumNumberOfSolarPanels = 50;

	private boolean numberOfWindowsEnabled;
	private int minimumNumberOfWindows = 4;
	private int maximumNumberOfWindows = 20;

	private boolean numberOfWallsEnabled;
	private int minimumNumberOfWalls = 4;
	private int maximumNumberOfWalls = 10;

	private boolean windowToFloorRatioEnabled;
	private double minimumWindowToFloorRatio = 0.15;
	private double maximumWindowToFloorRatio = 0.25;

	private boolean heightEnabled;
	private double minimumHeight = 8;
	private double maximumHeight = 10;

	private boolean areaEnabled;
	private double minimumArea = 100;
	private double maximumArea = 150;

	public DesignSpecs() {
	}

	// budget

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

	// number of windows

	public void setNumberOfWindowsEnabled(boolean windowEnabled) {
		this.numberOfWindowsEnabled = windowEnabled;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().getBasicsPanel().updateWindow();
			}
		});
	}

	public boolean isNumberOfWindowsEnabled() {
		return numberOfWindowsEnabled;
	}

	public void setMinimumNumberOfWindows(int minimumNumberOfWindows) {
		this.minimumNumberOfWindows = minimumNumberOfWindows;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().getBasicsPanel().updateWindow();
			}
		});
	}

	public int getMinimumNumberOfWindows() {
		return minimumNumberOfWindows;
	}

	public void setMaximumNumberOfWindows(int maximumNumberOfWindows) {
		this.maximumNumberOfWindows = maximumNumberOfWindows;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().getBasicsPanel().updateWindow();
			}
		});
	}

	public int getMaximumNumberOfWindows() {
		return maximumNumberOfWindows;
	}

	// number of solar panels

	public void setNumberOfSolarPanelsEnabled(boolean solarPanelEnabled) {
		this.numberOfSolarPanelsEnabled = solarPanelEnabled;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().getBasicsPanel().updateSolarPanel();
			}
		});
	}

	public boolean isNumberOfSolarPanelsEnabled() {
		return numberOfSolarPanelsEnabled;
	}

	public void setMinimumNumberOfSolarPanels(int minimumNumberOfSolarPanels) {
		this.minimumNumberOfSolarPanels = minimumNumberOfSolarPanels;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().getBasicsPanel().updateSolarPanel();
			}
		});
	}

	public int getMinimumNumberOfSolarPanels() {
		return minimumNumberOfSolarPanels;
	}

	public void setMaximumNumberOfSolarPanels(int maximumNumberOfSolarPanels) {
		this.maximumNumberOfSolarPanels = maximumNumberOfSolarPanels;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().getBasicsPanel().updateSolarPanel();
			}
		});
	}

	public int getMaximumNumberOfSolarPanels() {
		return maximumNumberOfSolarPanels;
	}

	// window-to-floor ratio

	public void setWindowToFloorRatioEnabled(boolean windowToFloorRatioEnabled) {
		this.windowToFloorRatioEnabled = windowToFloorRatioEnabled;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().getBasicsPanel().updateWindowToFloorRatio();
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
				EnergyPanel.getInstance().getBasicsPanel().updateWindowToFloorRatio();
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
				EnergyPanel.getInstance().getBasicsPanel().updateWindowToFloorRatio();
			}
		});
	}

	public double getMaximumWindowToFloorRatio() {
		return maximumWindowToFloorRatio;
	}

	// height

	public void setHeightEnabled(boolean heightEnabled) {
		this.heightEnabled = heightEnabled;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().getBasicsPanel().updateHeight();
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
				EnergyPanel.getInstance().getBasicsPanel().updateHeight();
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
				EnergyPanel.getInstance().getBasicsPanel().updateHeight();
			}
		});
	}

	public double getMinimumHeight() {
		return minimumHeight;
	}

	// area

	public void setAreaEnabled(boolean areaEnabled) {
		this.areaEnabled = areaEnabled;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().getBasicsPanel().updateArea();
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
				EnergyPanel.getInstance().getBasicsPanel().updateArea();
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
				EnergyPanel.getInstance().getBasicsPanel().updateArea();
			}
		});
	}

	public double getMinimumArea() {
		return minimumArea;
	}

	// wall

	public void setNumberOfWallsEnabled(boolean wallEnabled) {
		this.numberOfWallsEnabled = wallEnabled;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().getBasicsPanel().updateWall();
			}
		});
	}

	public boolean isNumberOfWallsEnabled() {
		return numberOfWallsEnabled;
	}

	public void setMaximumNumberOfWalls(int maximumNumberOfWalls) {
		this.maximumNumberOfWalls = maximumNumberOfWalls;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().getBasicsPanel().updateWall();
			}
		});
	}

	public int getMaximumNumberOfWalls() {
		return maximumNumberOfWalls;
	}

	public void setMinimumNumberOfWalls(int minimumNumberOfWalls) {
		this.minimumNumberOfWalls = minimumNumberOfWalls;
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().getBasicsPanel().updateWall();
			}
		});
	}

	public int getMinimumNumberOfWalls() {
		return minimumNumberOfWalls;
	}

}