package org.concord.energy3d.math;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;

public class EnergyComputer {
	private static final double deltaT = 10;
	private static final double wallUFactor = 0.41;
	private static final double windowUFactor = 1.22;
	private static final double doorUFactor = 0.64;
	private static final double roofUFactor = 0.48;

	public static void computeAreaAndEnergy() {
		double wallsArea = 0;
		double windowsArea = 0;
		double doorsArea = 0;
		double roofsArea = 0;
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Wall)
				wallsArea += part.computeArea();
			else if (part instanceof Window)
				windowsArea += part.computeArea();
			else if (part instanceof Door)
				doorsArea += part.computeArea();
			else if (part instanceof Roof)
				roofsArea += part.computeArea();
		}
		System.out.println(wallsArea);
		EnergyPanel.getInstance().updateArea(wallsArea, windowsArea, doorsArea, roofsArea);

		final double wallsEnergyLoss = wallsArea * wallUFactor * deltaT;
		final double windowsEnergyLoss = windowsArea * windowUFactor * deltaT;
		final double doorsEnergyLoss = doorsArea * doorUFactor * deltaT;
		final double roofsEnergyLoss = roofsArea * roofUFactor * deltaT;

		EnergyPanel.getInstance().updateEnergyLoss(wallsEnergyLoss, windowsEnergyLoss, doorsEnergyLoss, roofsEnergyLoss);
	}
}
