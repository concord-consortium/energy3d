package org.concord.energy3d.math;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;

public class EnergyComputer {
	public static void computeArea() {
		double walls = 0;
		double windows = 0;
		double doors = 0;
		double roofs = 0;
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Wall)
				walls += part.computeArea();
			else if (part instanceof Window)
				windows += part.computeArea();
			else if (part instanceof Door)
				doors += part.computeArea();
			else if (part instanceof Roof)
				roofs += part.computeArea();
		}
		System.out.println(walls);
		EnergyPanel.getInstance().updateArea(walls, windows, doors, roofs);
	}
}
