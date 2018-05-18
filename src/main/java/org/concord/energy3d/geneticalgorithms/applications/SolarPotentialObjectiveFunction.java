package org.concord.energy3d.geneticalgorithms.applications;

import java.util.Calendar;

import org.concord.energy3d.geneticalgorithms.ObjectiveFunction;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.Analysis;

/**
 * @author Charles Xie
 *
 */
public class SolarPotentialObjectiveFunction extends ObjectiveFunction {

	private final Foundation foundation;
	private final boolean maximize;

	public SolarPotentialObjectiveFunction(final int type, final Foundation foundation, final boolean maximize) {
		this.type = type;
		this.foundation = foundation;
		this.maximize = maximize;
	}

	@Override
	public double compute() {
		double result = 0;
		switch (type) {
		case ANNUAl:
			for (final int m : Analysis.MONTHS) {
				final Calendar c = Heliodon.getInstance().getCalendar();
				c.set(Calendar.MONTH, m);
				EnergyPanel.getInstance().computeNow();
				result += (maximize ? 1 : -1) * foundation.getSolarPotentialToday();
			}
			break;
		case RANDOM:
			result = Math.random();
			break;
		default:
			EnergyPanel.getInstance().computeNow();
			result = (maximize ? 1 : -1) * foundation.getSolarPotentialToday();
			break;
		}
		return result;
	}

}
