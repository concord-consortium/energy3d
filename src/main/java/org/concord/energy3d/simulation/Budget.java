package org.concord.energy3d.simulation;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;

/**
 * Calculate the cost.
 * 
 * @author Charles Xie
 * 
 */
public class Budget {

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

	public int getPartCost(HousePart part) {
		if (part instanceof Tree) {
			return 1000;
		}
		if (part instanceof Wall) {
			return (int) (part.computeArea() * 100);
		}
		if (part instanceof Window) {
			return (int) (part.computeArea() * 100);
		}
		return 0;
	}

}
