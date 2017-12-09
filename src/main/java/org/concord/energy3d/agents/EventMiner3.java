package org.concord.energy3d.agents;

import java.util.ArrayList;
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
public class EventMiner3 extends EventMiner {

	public EventMiner3(final String name) {
		super(name);
	}

	@Override
	public void sense(final MyEvent e) {
		eventString = EventUtil.eventsToString(observers, 10000, null);
		System.out.println(this + " Sensing:" + e.getName() + ">>> " + eventString);
	}

	@Override
	public void actuate() {
		System.out.println(this + " Actuating: " + eventString);
		String msg = "<html>";
		final MyEvent startEvent = idChangeEvent();
		if (startEvent == null) {
			msg += "This investigation requires choosing a different wall.";
		} else {
			final List<Class<?>> clazz = new ArrayList<Class<?>>();
			clazz.add(AnalysisEvent.class);
			clazz.add(ChangePartUValueCommand.class);
			final String s = EventUtil.eventsToString(clazz, 10000, startEvent);
			final Pattern p = Pattern.compile(segmentSeparatorRegex);
			final Matcher m = p.matcher(s);
			final int c = Util.countMatch(m);
			switch (c) {
			case 0:
				msg += "You have selected a different wall. You should change<br>its U-value and then run analysis.";
				break;
			case 1:
				msg += "You ran only one analysis after changing U-value.<br>Is it sufficient to draw a conclusion?";
				break;
			case 2:
				msg += "You have run two analyses after changing U-value.<br>Did you compare the results to find the relationship<br>between the difference of energy use and the change<br>of the U-value?";
				break;
			default:
				msg += "You have run " + c + " analyses after changing U-value.<br>What relationship between the energy use of the house<br>and the U-value of the wall did you find?";
				break;
			}
		}
		JOptionPane.showMessageDialog(MainFrame.getInstance(), msg + "</html>", "Advice", JOptionPane.INFORMATION_MESSAGE);
	}

}
