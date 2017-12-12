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

	String eventString;

	private final String name;

	private final Map<String, Feedback> warnings;
	private final Map<String, Feedback> reminders;
	private final Map<String, FeedbackPool> shortcircuits;
	private final String progressRegex;
	private final FeedbackPool progressFeedback;

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

		// cases that we can immediately decide the feedback
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

		// warnings upon the occurrence of the specified events
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

		// reminders upon the absence of the specified events
		reminders = new LinkedHashMap<String, Feedback>();
		reminders.put("#{2,}", new Feedback(JOptionPane.INFORMATION_MESSAGE, true, "Did you collect the U-value of the selected wall and the energy result<br>of the No. {ANALYSIS_NUMBER} analysis and type it in the table?"));

		// compound regex
		String violations = "";
		for (final String x : warnings.keySet()) {
			violations += x.substring(x.indexOf('[') + 1, x.lastIndexOf(']'));
		}
		progressRegex = "(A([^A" + violations + "]*?W+?[^A" + violations + "]*?)(?=A))+?";

		// instruction for progress
		progressFeedback = new FeedbackPool(4, 1);
		progressFeedback.setItem(0, 0, "You should run a daily energy analysis after changing only the U-value.<br>Your previous analyses might have involved other changes than U-value.");
		progressFeedback.setItem(1, 0, "Good work! You correctly analyzed the U-value change once.<br>Is it sufficient to draw a conclusion?");
		progressFeedback.setItem(2, 0, "You have run two correct analyses after changing U-value.<br>Did you compare the results to find the relationship<br>between the difference of energy use and the change<br>of the U-value?");
		progressFeedback.setItem(3, 0, "You have run {COUNT_PATTERN} correct analyses after changing U-value.<br>What relationship between the energy use of the house<br>and the U-value of the wall did you find?");

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
		if ("".equals(msg)) {
			final String[] segments = eventString.split("A+?"); // if no A is found, the entire event string is the only segment returned
			if (segments != null && segments.length > 0) {
				Feedback f = getFeedback(segments, warnings); // prioritize warnings
				if (f != null) {
					msg = f.getCustomMessage();
					type = f.getType();
				} else {
					f = getFeedback(segments, reminders); // if there is no warning, check reminders
					if (f != null) {
						msg = f.getCustomMessage();
						type = f.getType();
					}
				}
			}
			if ("".equals(msg)) { // if no warning or reminder is found, check progress
				int n = 0;
				if (segments != null && segments.length > 0) {
					final String seg = segments[0]; // special treatment for the starter zone
					if (seg.indexOf('W') != -1) { // if the wall U-value has been adjusted before the first analysis, hop to the next instruction
						n = 1;
					}
				}
				final int c = countMatch(progressRegex) + n;
				msg = progressFeedback.getCurrentItem(c).replaceAll("\\{COUNT_PATTERN\\}", c + "");
			}
		}
		JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>" + msg + "</html>", "Advice", type);
	}

	private Feedback getFeedback(final String[] segments, final Map<String, Feedback> map) {
		// Forgive the starter (meaning don't use i >= 0 in the for loop) because:
		// 1) no analysis has been run and 2) modifying the state before analysis is fine as long as the condition is kept the same later.
		for (int i = segments.length - 1; i > 0; i--) {
			String seg = segments[i];
			if ("".equals(seg)) {
				continue; // skip AA (A immediately followed by A)
			}
			if (i == segments.length - 1 && seg.endsWith("?")) { // skip the ask if it is the last event in the whole event string (not the current segment)
				seg = seg.substring(0, seg.length() - 1);
			}
			// reverse the order so that the latest can be processed first (TODO: This applies to only single-character indicators!)
			seg = new StringBuilder(seg).reverse().toString();
			for (final String regex : map.keySet()) {
				boolean find = Pattern.compile(regex).matcher(seg).find();
				final Feedback f = map.get(regex);
				if (f.getNegate()) {
					find = !find;
				}
				if (find) {
					// from the start of the string to the first A has the index 0, so the analysis index will start from 1
					f.setCustomMessage(f.getMessage().replaceAll("\\{ANALYSIS_NUMBER\\}", i + ""));
					return f;
				}
			}
			if (seg.lastIndexOf('?') != -1) { // go no further than the last ask
				break;
			}
		}
		return null;
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
