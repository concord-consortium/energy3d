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

	private double result = 0;

	public HeliostatObjectiveFunction() {

	}

	public double getResult() {
		return result;
	}

	public void compute() {
		EnergyPanel.getInstance().computeNow();
		final int month = Heliodon.getInstance().getCalendar().get(Calendar.MONTH);
		result = Util.sum(Scene.getInstance().getSolarResults()[month]);
	}

}
