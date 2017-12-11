package org.concord.energy3d.agents;

import java.util.ArrayList;
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
	private final List<Class<?>> checkList;

	public ConformanceChecker(final String name) {
		this.name = name;
		checkList = new ArrayList<Class<?>>();
		checkList.add(AnalysisEvent.class);
		checkList.add(ChangePartUValueCommand.class);
		checkList.add(ChangeDateCommand.class);
		checkList.add(QuestionnaireEvent.class);
		checkList.add(OperationEvent.class);
		checkList.add(DataCollectionEvent.class);
	}

	@Override
	public void sense(final MyEvent e) {
		eventString = EventUtil.eventsToString(checkList, 10000, null);
		System.out.println(this + " Sensing:" + e.getName() + ">>> " + eventString);
	}

	@Override
	public void actuate() {
		System.out.println(this + " Actuating: " + eventString);
		String msg = "<html>";
		final int countA = Util.countMatch(Pattern.compile("A+?").matcher(eventString));
		final int countD = Util.countMatch(Pattern.compile("D+?").matcher(eventString));
		final int countI = Util.countMatch(Pattern.compile("#+?").matcher(eventString));
		final int countQ = Util.countMatch(Pattern.compile("Q+?").matcher(eventString));
		final int countW = Util.countMatch(Pattern.compile("W+?").matcher(eventString));
		if (countQ < 2) {
			msg += "Did you forget to answer the pre/post-test questions?";
		} else if (countA == 0) {
			msg += "You have never run a daily energy analysis.";
		} else if (countW == 0) {
			msg += "You have never changed the U-value of a wall.";
		} else if (countI == 0) {
			msg += "You have never collected any data.";
		} else if (countD == 0) {
			msg += "Did you forget to investigate the effect of the U-value in a different season?";
		} else {
			msg += "Thank you for completing this task!";
			EnergyPanel.getInstance().showInstructionTabHeaders(true);
		}
		JOptionPane.showMessageDialog(MainFrame.getInstance(), msg + "</html>", "Advice", JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

}
