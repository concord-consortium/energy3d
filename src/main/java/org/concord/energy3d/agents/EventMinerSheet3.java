package org.concord.energy3d.agents;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.MainFrame;

/**
 * @author Charles Xie
 *
 */
public class EventMinerSheet3 extends EventMinerSheet2 {

	public EventMinerSheet3(final String name) {
		super(name);
	}

	@Override
	public void actuate() {
		System.out.println(this + " Actuating: " + eventString);
		final int i = eventString.lastIndexOf('D');
		if (i == -1) {
			final String msg = "This investigation requires change to a different season.";
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>" + msg + "</html>", "Advice", JOptionPane.WARNING_MESSAGE);
		} else {
			super.actuate();
		}
	}

}
