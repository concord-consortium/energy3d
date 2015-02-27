package org.concord.energy3d.util;

import org.concord.energy3d.gui.PropertiesPanel;

/**
 * This class defines the design specifications.
 * 
 * @author Charles Xie
 * 
 */
public class Specifications {

	private static Specifications instance = new Specifications();

	private boolean budgetEnabled = true;
	private boolean heightEnabled = false;
	private boolean areaEnabled = false;

	private int maximumBudget = 100000;
	private double minimumHeight = 8;
	private double maximumHeight = 10;
	private double minimumArea = 100;
	private double maximumArea = 150;

	private Specifications() {
	}

	public static Specifications getInstance() {
		return instance;
	}

	public void setBudgetEnabled(boolean budgetEnabled) {
		this.budgetEnabled = budgetEnabled;
		PropertiesPanel.getInstance().updateBudgetBar();
	}

	public boolean isBudgetEnabled() {
		return budgetEnabled;
	}

	public void setMaximumBudget(int maximumBudget) {
		this.maximumBudget = maximumBudget;
		PropertiesPanel.getInstance().updateBudgetBar();
	}

	public int getMaximumBudget() {
		return maximumBudget;
	}

	public void setHeightEnabled(boolean heightEnabled) {
		this.heightEnabled = heightEnabled;
		PropertiesPanel.getInstance().updateHeightBar();
	}

	public boolean isHeightEnabled() {
		return heightEnabled;
	}

	public void setMaximumHeight(double maximumHeight) {
		this.maximumHeight = maximumHeight;
		PropertiesPanel.getInstance().updateHeightBar();
	}

	public double getMaximumHeight() {
		return maximumHeight;
	}

	public void setMinimumHeight(double minimumHeight) {
		this.minimumHeight = minimumHeight;
		PropertiesPanel.getInstance().updateHeightBar();
	}

	public double getMinimumHeight() {
		return minimumHeight;
	}

	public void setAreaEnabled(boolean areaEnabled) {
		this.areaEnabled = areaEnabled;
		PropertiesPanel.getInstance().updateAreaBar();
	}

	public boolean isAreaEnabled() {
		return areaEnabled;
	}

	public void setMaximumArea(double maximumArea) {
		this.maximumArea = maximumArea;
		PropertiesPanel.getInstance().updateAreaBar();
	}

	public double getMaximumArea() {
		return maximumArea;
	}

	public void setMinimumArea(double minimumArea) {
		this.minimumArea = minimumArea;
		PropertiesPanel.getInstance().updateAreaBar();
	}

	public double getMinimumArea() {
		return minimumArea;
	}

}
