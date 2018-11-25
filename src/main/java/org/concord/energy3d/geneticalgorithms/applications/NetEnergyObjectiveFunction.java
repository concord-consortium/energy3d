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
public class NetEnergyObjectiveFunction extends ObjectiveFunction {

	private final Foundation foundation;

	public NetEnergyObjectiveFunction(final int type, final Foundation foundation) {
		this.type = type;
		this.foundation = foundation;
	}

	@Override
	public double compute() {
		double result = 0;
		switch (type) {
		case ANNUAL:
			for (final int m : Analysis.MONTHS) {
				final Calendar c = Heliodon.getInstance().getCalendar();
				c.set(Calendar.MONTH, m);
				EnergyPanel.getInstance().computeNow();
				result += foundation.getTotalEnergyToday();
			}
			break;
		default:
			EnergyPanel.getInstance().computeNow();
			result = foundation.getTotalEnergyToday();
			break;
		}
		return -result; // energy use is positive but we want to minimize it, so we flip the sign here
	}

}
