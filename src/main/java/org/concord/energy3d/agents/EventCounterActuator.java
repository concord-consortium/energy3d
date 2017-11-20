package org.concord.energy3d.agents;

import java.util.List;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.undo.ChangePartUValueCommand;

/**
 * @author Charles Xie
 *
 */
public class EventCounterActuator implements Actuator {

	private final List<EventCounter> counters;

	public EventCounterActuator(final List<EventCounter> counters) {
		this.counters = counters;
	}

	private EventCounter getCounterByClass(final Class<?> c) {
		for (final EventCounter e : counters) {
			if (e.getClazz().equals(c)) {
				return e;
			}
		}
		return null;
	}

	@Override
	public String getName() {
		return "Event Counter Actuator";
	}

	@Override
	public void actuate() {
		String msg = "<html>";
		final int analysisCount = getCounterByClass(AnalysisEvent.class).getCount();
		if (analysisCount == 0) {
			msg += "Did you forget to run analysis?";
		} else {
			final int uValueCount = getCounterByClass(ChangePartUValueCommand.class).getCount();
			switch (uValueCount) {
			case 0:
				msg += "You haven't changed the U-value.";
				break;
			case 1:
				if (analysisCount == 1) {
					msg += "You changed the U-value one time but ran only one analysis.";
				} else {
					msg += "You changed the U-value only one time.";
				}
				msg += "<br>The information isn't enough to understand the meaning of U-value.";
				break;
			case 2:
				if (analysisCount <= 2) {
					msg += "You changed the U-value twice but you didn't run enough analysis.";
				} else {
					msg += "You changed the U-value twice.<br>What did you find?";
				}
				break;
			default:
				msg += "You have changed the U-value more than three times.<br>Have you tried to look at the differences in energy use after each change?";
				break;
			}
		}
		JOptionPane.showMessageDialog(MainFrame.getInstance(), msg + "</html>", "Advice", JOptionPane.INFORMATION_MESSAGE);
	}

}
