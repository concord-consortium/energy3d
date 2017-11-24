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

	public EventMiner(final String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void sense(final MyEvent e) {
		eventString = EventUtil.eventsToString(new Class[] { AnalysisEvent.class, ChangePartUValueCommand.class }, 10000, null);
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
			msg += "Did you forget to run analysis?";
		} else {
			p = Pattern.compile("U+?");
			m = p.matcher(eventString);
			c = Util.countMatch(m);
			if (c == 0) {
				msg += "You haven't changed the U-value.";
			} else {
				p = Pattern.compile("(U[_\\*]*A)+?");
				m = p.matcher(eventString);
				c = Util.countMatch(m);
				switch (c) {
				case 0:
					msg += "You should run analysis after changing U-value.";
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
