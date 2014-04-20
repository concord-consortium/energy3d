package org.concord.energy3d.simulation;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;

/**
 * Analyze the cost.
 * 
 * @author Charles Xie
 * 
 */
public class Cost {

	private int budget = 100000;
	private static Cost instance = new Cost();

	private Cost() {
	}

	public static Cost getInstance() {
		return instance;
	}

	public void setBudget(int budget) {
		this.budget = budget;
		EnergyPanel.getInstance().setBudget(budget);
	}

	public int getBudget() {
		return budget;
	}

	public int getTotalCost() {
		int sum = 0;
		for (HousePart p : Scene.getInstance().getParts()) {
			if (p.isFrozen())
				continue;
			sum += getPartCost(p);
		}
		return sum;
	}

	public int getBuildingCost(Foundation foundation) {
		int sum = 0;
		for (HousePart p : Scene.getInstance().getParts()) {
			if (p.getTopContainer() == foundation) {
				sum += getPartCost(p);
			}
		}
		return sum;
	}

	private int getPartCost(HousePart part) {
		if (part instanceof Tree) {
			return 1000;
		}
		if (part instanceof Wall) {
			String uValue = (String) EnergyPanel.getInstance().getWallsComboBox().getSelectedItem();
			int i = uValue.indexOf(" ");
			if (i != -1)
				uValue = uValue.substring(0, i);
			return (int) (part.computeArea() * 100);
		}
		if (part instanceof Window) {
			return (int) (part.computeArea() * 100);
		}
		return 0;
	}

}
