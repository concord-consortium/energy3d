package org.concord.energy3d.agents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.undo.AddPartCommand;
import org.concord.energy3d.undo.AdjustThermostatCommand;
import org.concord.energy3d.undo.ChangeBuildingUValueCommand;
import org.concord.energy3d.undo.ChangeCityCommand;
import org.concord.energy3d.undo.ChangeDateCommand;
import org.concord.energy3d.undo.ChangePartColorCommand;
import org.concord.energy3d.undo.ChangePartUValueCommand;
import org.concord.energy3d.undo.EditPartCommand;
import org.concord.energy3d.undo.MovePartCommand;
import org.concord.energy3d.undo.PastePartCommand;
import org.concord.energy3d.undo.RemovePartCommand;
import org.concord.energy3d.undo.RotateBuildingCommand;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 *
 */
public class EventMinerSheet2 implements Agent {

	final String name;
	String eventString;

	// clearly needed
	Map<String, Feedback> warnings;
	Map<String, Feedback> reminders;
	Map<String, FeedbackPool> shortcircuits;

	String conformanceRegex;
	FeedbackPool feedbackOnConformance;

	static List<Class<?>> observers = new ArrayList<Class<?>>();
	static {
		observers.add(AnalysisEvent.class);
		observers.add(DataCollectionEvent.class);
		observers.add(ChangePartUValueCommand.class);
		observers.add(ChangeBuildingUValueCommand.class);
		observers.add(ChangeDateCommand.class);
		observers.add(ChangeCityCommand.class);
		observers.add(ChangePartColorCommand.class);
		observers.add(AddPartCommand.class);
		observers.add(EditPartCommand.class);
		observers.add(PastePartCommand.class);
		observers.add(RemovePartCommand.class);
		observers.add(MovePartCommand.class);
		observers.add(RotateBuildingCommand.class);
		observers.add(AdjustThermostatCommand.class);
	}

	public EventMinerSheet2(final String name) {

		this.name = name;

		// single indicators
		shortcircuits = new LinkedHashMap<String, FeedbackPool>();

		FeedbackPool feedback = new FeedbackPool(1, 2);
		feedback.setItem(0, 0, "Try analyzing the energy use of the house using the menu<br>Analysis > Buildings > Dail Energy Analysis for Selected Building...");
		feedback.setItem(0, 1, "Did you forget to run daily energy analysis?");
		shortcircuits.put("A+?", feedback);

		feedback = new FeedbackPool(1, 1);
		feedback.setItem(0, 0, "Did you collect the U-value of the selected wall and the energy result<br>following the analysis and type it in the table?");
		shortcircuits.put("#{2,}", feedback);

		feedback = new FeedbackPool(1, 2);
		feedback.setItem(0, 0, "Have you selected a wall and changed its U-value?<br>Try right-clicking a wall and select \"Insulation...\" from the popup menu.");
		feedback.setItem(0, 1, "Your task is to investigate how changing U-value of a wall affects the energy use<br>of the house. Make sure that you adjust the U-value.");
		shortcircuits.put("A+?.*?W+?", feedback);

		feedbackOnConformance = new FeedbackPool(4, 1);
		feedbackOnConformance.setItem(0, 0, "You should run a daily energy analysis after changing the U-value.");
		feedbackOnConformance.setItem(1, 0, "You only analyzed U-value change once.<br>Is it sufficient to draw a conclusion?");
		feedbackOnConformance.setItem(2, 0, "You have run two correct analyses after changing U-value.<br>Did you compare the results to find the relationship<br>between the difference of energy use and the change<br>of the U-value?");
		feedbackOnConformance.setItem(3, 0, "You have run {COUNT_PATTERN} correct analyses after changing U-value.<br>What relationship between the energy use of the house<br>and the U-value of the wall did you find?");

		// warning upon the appearance of the specified events
		warnings = new LinkedHashMap<String, Feedback>();
		warnings.put("[C]+?", new Feedback(JOptionPane.WARNING_MESSAGE, false, "You changed the location. As each location has a different climate,<br>changing the location may affect the result of energy use."));
		warnings.put("[D]+?", new Feedback(JOptionPane.WARNING_MESSAGE, false, "You changed the date. As each date has different weather conditions,<br>changing the date may affect the result of energy use."));
		warnings.put("[L]+?", new Feedback(JOptionPane.WARNING_MESSAGE, false, "You changed the color of some part of the house. As the color of a house may affect its absorption of solar energy,<br>changing the color may affect the result of energy use."));
		warnings.put("[P]+?", new Feedback(JOptionPane.WARNING_MESSAGE, false, "Modification of the house (other than the U-value) is not recommended for this investigation<br>as it may interfere with the effect of the U-value on energy use."));
		warnings.put("[R]+?", new Feedback(JOptionPane.WARNING_MESSAGE, false, "You changed the U-value of an object that is not a wall.<br>In this investigation, you should select a wall and change only its U-value."));
		warnings.put("[T]+?", new Feedback(JOptionPane.WARNING_MESSAGE, false, "Adjusting the thermostat is not recommended for this investigation<br>as it may interfere with the effect of the U-value on energy use."));
		warnings.put("[U]+?", new Feedback(JOptionPane.WARNING_MESSAGE, false, "You changed the U-values of all walls.<br>In this investigation, you should select a wall and change only its U-value."));
		warnings.put("[Y]+?", new Feedback(JOptionPane.WARNING_MESSAGE, false, "You ran an annual energy analysis.<br>In this investigation, you should run only daily energy analyses."));
		warnings.put("[Z]+?", new Feedback(JOptionPane.WARNING_MESSAGE, false, "Rotation of the house is not recommended for this investigation<br>as it may interfere with the effect of the U-value on energy use."));

		// reminding upon the absence of the specified events
		reminders = new LinkedHashMap<String, Feedback>();
		reminders.put("#{2,}", new Feedback(JOptionPane.INFORMATION_MESSAGE, true, "Did you collect the U-value of the selected wall and the energy result<br>of the No. {ANALYSIS_NUMBER} analysis and type it in the table?"));

		// compounds
		String violations = "";
		for (final String x : warnings.keySet()) {
			violations += x.substring(x.indexOf('[') + 1, x.lastIndexOf(']'));
		}
		conformanceRegex = "(A([^A" + violations + "]*?W+?[^A" + violations + "]*?)(?=A))+?";

	}

	@Override
	public void sense(final MyEvent e) {
		eventString = EventUtil.eventsToString(observers, 10000, null);
		System.out.println(this + " Sensing:" + e.getName() + ">>> " + eventString);
	}

	@Override
	public void actuate() {
		System.out.println(this + " Actuating: " + eventString);
		String msg = checkSingleIndicators();
		int type = JOptionPane.INFORMATION_MESSAGE;
		if (msg.equals("")) {
			String s = "";

			// start with the latest segment since the last analysis and work back one segment by another, stopping at the last ask
			final String[] segments = eventString.split("A+?"); // if no A is found, the entire event string is the only segment returned
			if (segments != null && segments.length > 0) {

				// warnings are top priority, first scan the warnings
				outer1: for (int i = segments.length - 1; i > 0; i--) { // forgive the starter as modifying the state before analysis is fine as long as the condition is kept the same throughout
					String seg = segments[i];
					if ("".equals(seg)) {
						continue; // skip AA (A followed by A)
					}
					// System.out.println("substring: " + i + " = " + seg);
					if (i == segments.length - 1 && seg.endsWith("?")) { // skip the ask if and only if it is the last event in the whole event string (not the current segment)
						seg = seg.substring(0, seg.length() - 1);
					}
					seg = new StringBuilder(seg).reverse().toString(); // reverse the order so that the latest can be processed first (WARNING: This applies to only single-character indicators)
					for (final String regex : warnings.keySet()) {
						boolean find = Pattern.compile(regex).matcher(seg).find();
						final Feedback f = warnings.get(regex);
						if (f.getNegate()) {
							find = !find;
						}
						if (find) {
							s = f.getMessage();
							type = f.getType();
							break outer1;
						}
					}
					if (seg.lastIndexOf('?') != -1) { // stop at the last ask
						break outer1;
					}
				}

				if ("".equals(s)) { // if there is no warning, look for reminders
					outer2: for (int i = segments.length - 1; i > 0; i--) { // forgive the starter as no analysis has been run
						String seg = segments[i];
						if ("".equals(seg)) {
							continue; // skip AA (A followed by A)
						}
						if (i == segments.length - 1 && seg.endsWith("?")) { // skip the ask if and only if it is the last event in the whole event string (not the current segment)
							seg = seg.substring(0, seg.length() - 1);
						}
						seg = new StringBuilder(seg).reverse().toString(); // reverse the order so that the latest can be processed first (WARNING: This applies to only single-character indicators)
						for (final String regex : reminders.keySet()) {
							boolean find = Pattern.compile(regex).matcher(seg).find();
							final Feedback f = reminders.get(regex);
							if (f.getNegate()) {
								find = !find;
							}
							if (find) {
								s = f.getMessage();
								s = s.replaceAll("\\{ANALYSIS_NUMBER\\}", i + ""); // from the start of the string to the first A has the index 0, so the analysis index will start from 1
								type = f.getType();
								break outer2;
							}
						}
						if (seg.lastIndexOf('?') != -1) { // stop at the last ask
							break outer2;
						}
					}
				}

			}

			// if no warning or reminder is found, check conformity
			if ("".equals(s)) {
				int n = 0;
				if (segments != null && segments.length > 0) {
					final String seg = segments[0];
					if (seg.indexOf('W') != -1) {
						n = 1;
					}
				}
				final int c = countMatch(conformanceRegex) + n;
				s = feedbackOnConformance.getCurrentItem(c).replaceAll("\\{COUNT_PATTERN\\}", c + "");
			}

			msg += s;

		}

		JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>" + msg + "</html>", "Advice", type);

	}

	private int countMatch(final String regex) {
		return Util.countMatch(Pattern.compile(regex).matcher(eventString));
	}

	private String checkSingleIndicators() {
		String s = "";
		for (final String regex : shortcircuits.keySet()) {
			if (countMatch(regex) == 0) {
				final FeedbackPool f = shortcircuits.get(regex);
				s += f.getCurrentItem(0);
				f.forward(0);
				break;
			}
		}
		return s;
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
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

}
