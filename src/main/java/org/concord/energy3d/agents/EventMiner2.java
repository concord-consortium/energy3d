package org.concord.energy3d.agents;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 *
 */
public class EventMiner2 extends EventMiner {

	public EventMiner2(final String name) {
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
		final int i = eventString.lastIndexOf('C');
		if (i == -1) {
			msg += "This investigation requires change to a different season.";
		} else {
			final String s = eventString.substring(i + 1);
			final Pattern p = Pattern.compile(segmentSeparatorRegex);
			final Matcher m = p.matcher(s);
			final int c = Util.countMatch(m);
			switch (c) {
			case 0:
				msg += "You should change U-value and then run analysis.";
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
