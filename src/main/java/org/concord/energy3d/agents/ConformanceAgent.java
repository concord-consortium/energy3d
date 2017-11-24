package org.concord.energy3d.agents;

import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.undo.ChangeDateCommand;
import org.concord.energy3d.undo.ChangePartUValueCommand;
import org.concord.energy3d.util.Util;

/**
 * Conformance analysis
 * 
 * @author Charles Xie
 *
 */
public class ConformanceAgent implements Agent {

	private final String name;
	private String eventString;

	public ConformanceAgent(final String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void sense(final MyEvent e) {
		final Class<?>[] c = new Class[] { AnalysisEvent.class, ChangePartUValueCommand.class, ChangeDateCommand.class, QuestionnaireEvent.class, OperationEvent.class };
		eventString = EventUtil.eventsToString(c, 10000, null);
		System.out.println(this + " Sensing:" + e.getName() + ">>> " + eventString);
	}

	@Override
	public void actuate() {
		System.out.println(this + " Actuating: " + eventString);
		String msg = "<html>";
		final int countA = Util.countMatch(Pattern.compile("A+?").matcher(eventString));
		final int countC = Util.countMatch(Pattern.compile("C+?").matcher(eventString));
		final int countQ = Util.countMatch(Pattern.compile("Q+?").matcher(eventString));
		final int countU = Util.countMatch(Pattern.compile("U+?").matcher(eventString));
		if (countQ < 2) {
			msg += "Did you forget to answer all the questions?";
		} else if (countA == 0) {
			msg += "You have never run analysis.";
		} else if (countU == 0) {
			msg += "You have never changed U-value";
		} else if (countC == 0) {
			msg += "Did you forget to investigate the relationship between U-value and season?";
		} else {
			msg += "Thank you for taking this task!";
		}
		JOptionPane.showMessageDialog(MainFrame.getInstance(), msg + "</html>", "Advice", JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public String toString() {
		return name;
	}

}
