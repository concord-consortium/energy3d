package org.concord.energy3d.generative;

import java.util.Calendar;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 *
 */
public class HeliostatObjectiveFunction extends ObjectiveFunction {

	public HeliostatObjectiveFunction() {

	}

	@Override
	public double compute() {
		Scene.getInstance().updateTrackables();
		EnergyPanel.getInstance().computeNow();
		final int month = Heliodon.getInstance().getCalendar().get(Calendar.MONTH);
		return Util.sum(Scene.getInstance().getSolarResults()[month]);
	}

}
