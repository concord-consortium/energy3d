package org.concord.energy3d.agents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.undo.ChangeBuildingUValueCommand;
import org.concord.energy3d.undo.ChangeCityCommand;
import org.concord.energy3d.undo.ChangeDateCommand;
import org.concord.energy3d.undo.ChangePartColorCommand;
import org.concord.energy3d.undo.ChangePartUValueCommand;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 *
 */
public class EventMiner implements Agent {

	final String name;
	String eventString;
	String segmentRegex = "(A.*?(?=A))+?";
	String violationsRegex;
	String strictConformanceRegex;

	Map<String, FeedbackPool> singleViolatorFeedbackMap;
	Map<String, FeedbackPool> singleIndicatorFeedbackMap;
	FeedbackPool feedbackOnTargetBehavior;
	FeedbackPool feedbackOnTargetViolation;

	static List<Class<?>> observers = new ArrayList<Class<?>>();
	static {
		observers.add(AnalysisEvent.class);
		observers.add(DataCollectionEvent.class);
		observers.add(ChangePartUValueCommand.class);
		observers.add(ChangeBuildingUValueCommand.class);
		observers.add(ChangeDateCommand.class);
		observers.add(ChangeCityCommand.class);
		observers.add(ChangePartColorCommand.class);
	}

	public EventMiner(final String name) {

		this.name = name;

		// single indicators
		singleIndicatorFeedbackMap = new LinkedHashMap<String, FeedbackPool>();

		FeedbackPool feedback = new FeedbackPool(1, 2);
		feedback.setItem(0, 0, "Try analyzing the energy use of the house using the menu<br>Analysis > Buildings > Dail Energy Analysis for Selected Building...");
		feedback.setItem(0, 1, "Did you forget to run daily energy analysis?");
		singleIndicatorFeedbackMap.put("A+?", feedback);

		feedback = new FeedbackPool(1, 1);
		feedback.setItem(0, 0, "Have you input enough data in the table?");
		singleIndicatorFeedbackMap.put("#{2,}?", feedback);

		feedback = new FeedbackPool(1, 2);
		feedback.setItem(0, 0, "Have you selected a wall and changed its U-value?<br>Try right-clicking a wall and select \"Insulation...\" from the popup menu.");
		feedback.setItem(0, 1, "Your task is to investigate how changing U-value of a wall affects the energy use<br>of the house. But you haven't adjusted the U-value.");
		singleIndicatorFeedbackMap.put("W+?", feedback);

		feedbackOnTargetBehavior = new FeedbackPool(4, 1);
		feedbackOnTargetBehavior.setItem(0, 0, "You should run a daily energy analysis after changing U-value.");
		feedbackOnTargetBehavior.setItem(1, 0, "You only analyzed U-value change once.<br>Is it sufficient to draw a conclusion?");
		feedbackOnTargetBehavior.setItem(2, 0, "You have run two analyses after changing U-value.<br>Did you compare the results to find the relationship<br>between the difference of energy use and the change<br>of the U-value?");
		feedbackOnTargetBehavior.setItem(3, 0, "You have run {COUNT_PATTERN} analyses after changing U-value.<br>What relationship between the energy use of the house<br>and the U-value of the wall did you find?");

		feedbackOnTargetViolation = new FeedbackPool(1, 1);
		feedbackOnTargetViolation.setItem(0, 0, "You changed multiple variables besides U-value, which may invalidate<br>your result. You can discard the previous data and collec more new data<br>under the new condition. Make sure to change only the U-value between<br>analyses.");

		// single violators
		singleViolatorFeedbackMap = new LinkedHashMap<String, FeedbackPool>();

		FeedbackPool warning = new FeedbackPool(1, 1);
		warning.setItem(0, 0, "You changed the location. As each location has a different climate,<br>changing the location may affect the result of energy use.");
		singleViolatorFeedbackMap.put("C", warning);

		warning = new FeedbackPool(1, 1);
		warning.setItem(0, 0, "You changed the date. As each date has different weather conditions,<br>changing the date may affect the result of energy use.");
		singleViolatorFeedbackMap.put("D", warning);

		warning = new FeedbackPool(1, 1);
		warning.setItem(0, 0, "You changed the color of some part of the house. As the color of a house may affect its absorption of solar energy,<br>changing the color may affect the result of energy use.");
		singleViolatorFeedbackMap.put("L", warning);

		warning = new FeedbackPool(1, 1);
		warning.setItem(0, 0, "You changed the U-value of an object that is not a wall.<br>In this investigation, you should select a wall and change only its U-value.");
		singleViolatorFeedbackMap.put("R", warning);

		warning = new FeedbackPool(1, 1);
		warning.setItem(0, 0, "You changed the U-values of all walls.<br>In this investigation, you should select a wall and change only its U-value.");
		singleViolatorFeedbackMap.put("U", warning);

		warning = new FeedbackPool(1, 1);
		warning.setItem(0, 0, "You ran an annual energy analysis.<br>In this investigation, you should run only daily energy analyses.");
		singleViolatorFeedbackMap.put("Y", warning);

		String violations = "";
		for (final String x : singleViolatorFeedbackMap.keySet()) {
			violations += x;
		}

		violationsRegex = "[" + violations + "]+?";
		strictConformanceRegex = "(A([^A" + violations + "]*?W+?[^A" + violations + "]*?)(?=A))+?";

	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void sense(final MyEvent e) {
		eventString = EventUtil.eventsToString(observers, 10000, null);
		System.out.println(this + " Sensing:" + e.getName() + ">>> " + eventString);
	}

	private int countMatch(final String regex) {
		return Util.countMatch(Pattern.compile(regex).matcher(eventString));
	}

	public String checkSingleIndicators() {
		String s = "";
		for (final String regex : singleIndicatorFeedbackMap.keySet()) {
			if (countMatch(regex) == 0) {
				final FeedbackPool f = singleIndicatorFeedbackMap.get(regex);
				s += f.getCurrentItem(0);
				f.forward(0);
				break;
			}
		}
		return s;
	}

	private String lastMatch(final String regex, final String string) {
		final Matcher matcher = Pattern.compile(regex).matcher(string);
		String lastMatch = null;
		while (matcher.find()) {
			lastMatch = matcher.group();
		}
		return lastMatch;
	}

	@Override
	public void actuate() {
		System.out.println(this + " Actuating: " + eventString);
		String msg = checkSingleIndicators();
		boolean violated = false;
		if (msg.equals("")) {
			String s = "";
			final String v = lastMatch(segmentRegex, eventString);
			if (v != null) {
				System.out.println("~~~~~" + v);
				for (final String x : singleViolatorFeedbackMap.keySet()) {
					if (Pattern.compile("[" + x + "]+?").matcher(v).find()) {
						s = singleViolatorFeedbackMap.get(x).getCurrentItem(0);
						violated = true;
						break;
					}
				}
			}
			if (violated) {
				// s = feedbackOnTargetViolation.getCurrentItem(0);
			} else {
				final int c = countMatch(strictConformanceRegex);
				s = feedbackOnTargetBehavior.getCurrentItem(c).replaceAll("\\{COUNT_PATTERN\\}", c + "");
			}
			msg += s;
		}
		if (violated) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>" + msg + "</html>", "Warning", JOptionPane.WARNING_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>" + msg + "</html>", "Advice", JOptionPane.INFORMATION_MESSAGE);
		}
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
