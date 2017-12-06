package org.concord.energy3d.agents;

import java.util.List;
import java.util.TreeMap;
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

	TreeMap<String, FeedbackPool> feedbackMap;
	FeedbackPool feedbackOnDailyAnalysis;
	FeedbackPool feedbackOnUValueChange;
	FeedbackPool feedbackOnDataCollector;
	FeedbackPool feedbackOnTargetPattern;

	public EventMiner(final String name) {
		this.name = name;

		feedbackMap = new TreeMap<String, FeedbackPool>();

		feedbackOnDailyAnalysis = new FeedbackPool(1, 2);
		feedbackOnDailyAnalysis.setItem(0, 0, "Try analyzing the energy use of the house using the menu<br>Analysis > Buildings > Dail Energy Analysis for Selected Building...");
		feedbackOnDailyAnalysis.setItem(0, 1, "Did you forget to run daily energy analysis?");
		feedbackMap.put("A+?", feedbackOnDailyAnalysis);

		feedbackOnDataCollector = new FeedbackPool(1, 1);
		feedbackOnDataCollector.setItem(0, 0, "You haven't collected any data in the data tables yet.");
		feedbackMap.put("D+?", feedbackOnDataCollector);

		feedbackOnUValueChange = new FeedbackPool(1, 2);
		feedbackOnUValueChange.setItem(0, 0, "Your task is to investigate how changing U-value of a wall affects the energy use<br>of the house. But you haven't adjusted the U-value.");
		feedbackOnUValueChange.setItem(0, 1, "Have you selected a wall and changed its U-value?<br>Try right-clicking a wall and select \"Insulation...\" from the popup menu.");
		feedbackMap.put("U+?", feedbackOnUValueChange);

		feedbackOnTargetPattern = new FeedbackPool(4, 1);
		feedbackOnTargetPattern.setItem(0, 0, "You should run a daily energy analysis after changing U-value.");
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

	private int countMatch(final String regex) {
		return Util.countMatch(Pattern.compile(regex).matcher(eventString));
	}

	public String x() {
		String s = "";
		for (final String regex : feedbackMap.keySet()) {
			if (countMatch(regex) == 0) {
				final FeedbackPool f = feedbackMap.get(regex);
				s += f.getCurrentItem(0);
				f.forward(0);
				break;
			}
		}
		return s;
	}

	@Override
	public void actuate() {
		System.out.println(this + " Actuating: " + eventString);
		String msg = "<html>";
		int c = countMatch("A+?");
		if (c == 0) {
			msg += feedbackOnDailyAnalysis.getCurrentItem(0);
			feedbackOnDailyAnalysis.forward(0);
		} else {
			c = countMatch("D+?");
			if (c == 0) {
				msg += feedbackOnDataCollector.getCurrentItem(0);
				feedbackOnDataCollector.forward(0);
			} else {
				c = countMatch("U+?");
				if (c == 0) {
					msg += feedbackOnUValueChange.getCurrentItem(0);
					feedbackOnUValueChange.forward(0);
				} else {
					c = countMatch(targePattern);
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
