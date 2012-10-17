package org.concord.energy3d.math;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;

public class EnergyComputer {
	public static void computeArea() {
		for (final HousePart part : Scene.getInstance().getParts())
			System.out.println(part.computeArea());
	}
}
