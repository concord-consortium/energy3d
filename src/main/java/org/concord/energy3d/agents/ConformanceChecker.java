package org.concord.energy3d.agents;

import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.undo.ChangeDateCommand;
import org.concord.energy3d.undo.ChangePartUValueCommand;
import org.concord.energy3d.util.Util;

/**
 * Conformance checking (https://en.wikipedia.org/wiki/Conformance_checking) is a process mining technique to compare a process model with an event log of the same process.
 * 
 * @author Charles Xie
 *
 */
public class ConformanceChecker implements Agent {

	private final String name;
	private String eventString;

	public ConformanceChecker(final String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void sense(final MyEvent e) {
		final Class<?>[] c = new Class[] { AnalysisEvent.class, ChangePartUValueCommand.class, ChangeDateCommand.class, QuestionnaireEvent.class, OperationEvent.class, DataCollectionEvent.class };
		eventString = EventUtil.eventsToString(c, 10000, null);
		System.out.println(this + " Sensing:" + e.getName() + ">>> " + eventString);
	}

	MyEvent idChangeEvent() {
		final List<MyEvent> u = EventUtil.getEvents(ChangePartUValueCommand.class);
		if (u.size() < 2) {
			return null;
		}
		long oldId = -1;
		long newId = -1;
		for (final MyEvent x : u) {
			if (x instanceof ChangePartUValueCommand) {
				final ChangePartUValueCommand command = (ChangePartUValueCommand) x;
				newId = command.getPart().getId();
				if (oldId == -1) { // first
					oldId = newId;
				} else {
					if (newId != oldId) {
						return x;
					}
				}
			}
		}
		return null;
	}

	@Override
	public void actuate() {
		System.out.println(this + " Actuating: " + eventString);
		String msg = "<html>";
		final int countA = Util.countMatch(Pattern.compile("A+?").matcher(eventString));
		final int countC = Util.countMatch(Pattern.compile("C+?").matcher(eventString));
		final int countD = Util.countMatch(Pattern.compile("D+?").matcher(eventString));
		final int countQ = Util.countMatch(Pattern.compile("Q+?").matcher(eventString));
		final int countU = Util.countMatch(Pattern.compile("U+?").matcher(eventString));
		if (countQ < 2) {
			msg += "Did you forget to answer the pre/post-test questions?";
		} else if (countA == 0) {
			msg += "You have never run a daily energy analysis.";
		} else if (countU == 0) {
			msg += "You have never changed the U-value.";
		} else if (countD == 0) {
			msg += "You have never collected any data.";
		} else if (countC == 0) {
			msg += "Did you forget to investigate the effect of the U-value in a different season?";
		} else {
			final MyEvent startEvent = idChangeEvent();
			if (startEvent == null) {
				msg += "Did you forget to investigate the effect of the U-value of a different wall?";
			} else {
				msg += "Thank you for completing this task!";
				EnergyPanel.getInstance().showInstructionTabHeaders(true);
			}
		}
		JOptionPane.showMessageDialog(MainFrame.getInstance(), msg + "</html>", "Advice", JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public String toString() {
		return name;
	}

}
