package org.concord.energy3d.agents;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.undo.ChangePartUValueCommand;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 *
 */
public class EventMiner implements Agent {

	final String name;
	String eventString;
	String targePattern = "(U[_\\*\\?D]*A)+?";

	FeedbackPool feedbackOnAnalysisCheck;
	FeedbackPool feedbackOnUValueCheck;
	FeedbackPool feedbackOnDataCollectorCheck;
	FeedbackPool feedbackOnTargetPattern;

	public EventMiner(final String name) {
		this.name = name;
		feedbackOnAnalysisCheck = new FeedbackPool(1, 2);
		feedbackOnAnalysisCheck.setItem(0, 0, "Try analyzing the energy use of the house using the menu<br>Analysis > Buildings > Dail Energy Analysis for Selected Building...");
		feedbackOnAnalysisCheck.setItem(0, 1, "Did you forget to run analysis?");
		feedbackOnUValueCheck = new FeedbackPool(1, 2);
		feedbackOnUValueCheck.setItem(0, 0, "Your task is to investigate how changing U-value of a wall affects the energy use<br>of the house. But you haven't adjusted the U-value.");
		feedbackOnUValueCheck.setItem(0, 1, "Have you selected a wall and changed its U-value?<br>Try right-clicking a wall and select \"Insulation...\" from the popup menu.");
		feedbackOnDataCollectorCheck = new FeedbackPool(1, 1);
		feedbackOnDataCollectorCheck.setItem(0, 0, "You haven't collected any data in the data tables yet.");
		feedbackOnTargetPattern = new FeedbackPool(4, 1);
		feedbackOnTargetPattern.setItem(0, 0, "You should run analysis after changing U-value.");
		feedbackOnTargetPattern.setItem(1, 0, "You ran only one analysis after changing U-value.<br>Is it sufficient to draw a conclusion?");
		feedbackOnTargetPattern.setItem(2, 0, "You have run two analyses after changing U-value.<br>Did you compare the results to find the relationship<br>between the difference of energy use and the change<br>of the U-value?");
		feedbackOnTargetPattern.setItem(3, 0, "You have run at least three analyses after changing U-value.<br>What relationship between the energy use of the house<br>and the U-value of the wall did you find?");
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void sense(final MyEvent e) {
		eventString = EventUtil.eventsToString(new Class[] { AnalysisEvent.class, DataCollectionEvent.class, ChangePartUValueCommand.class }, 10000, null);
		System.out.println(this + " Sensing:" + e.getName() + ">>> " + eventString);
	}

	@Override
	public void actuate() {
		System.out.println(this + " Actuating: " + eventString);
		String msg = "<html>";
		Pattern p = Pattern.compile("A+?");
		Matcher m = p.matcher(eventString);
		int c = Util.countMatch(m);
		if (c == 0) {
			msg += feedbackOnAnalysisCheck.getCurrentItem(0);
			feedbackOnAnalysisCheck.forward(0);
		} else {
			p = Pattern.compile("D+?");
			m = p.matcher(eventString);
			c = Util.countMatch(m);
			if (c == 0) {
				msg += feedbackOnDataCollectorCheck.getCurrentItem(0);
				feedbackOnDataCollectorCheck.forward(0);
			} else {
				p = Pattern.compile("U+?");
				m = p.matcher(eventString);
				c = Util.countMatch(m);
				if (c == 0) {
					msg += feedbackOnUValueCheck.getCurrentItem(0);
					feedbackOnUValueCheck.forward(0);
				} else {
					p = Pattern.compile(targePattern);
					m = p.matcher(eventString);
					c = Util.countMatch(m);
					if (c >= feedbackOnTargetPattern.getNumberOfCases()) {
						c = feedbackOnTargetPattern.getNumberOfCases() - 1;
					}
					msg += feedbackOnTargetPattern.getCurrentItem(c);
				}
			}
		}
		JOptionPane.showMessageDialog(MainFrame.getInstance(), msg + "</html>", "Advice", JOptionPane.INFORMATION_MESSAGE);
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
	public String toString() {
		return name;
	}

}
