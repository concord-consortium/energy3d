package org.concord.energy3d.agents;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.undo.ChangeDateCommand;
import org.concord.energy3d.undo.ChangePartUValueCommand;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 *
 */
public class EventMiner implements Agent {

	private final String name;
	private String eventString;

	public EventMiner(final String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void sense(final MyEvent e) {
		eventString = EventUtil.eventsToString(new Class[] { AnalysisEvent.class, ChangePartUValueCommand.class, ChangeDateCommand.class }, 10000);
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
				p = Pattern.compile("(A[_\\*]*U)+?");
				m = p.matcher(eventString);
				c = Util.countMatch(m);
				switch (c) {
				case 0:
					msg += "" + c;
					break;
				case 1:
					msg += "" + c;
					break;
				case 2:
					msg += "" + c;
					break;
				default: // greater than 2
					msg += "" + c;
					break;
				}
			}
		}
		JOptionPane.showMessageDialog(MainFrame.getInstance(), msg + "</html>", "Advice", JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public String toString() {
		return "Simple Reflex Agent";
	}

}
