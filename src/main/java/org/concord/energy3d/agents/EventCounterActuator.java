package org.concord.energy3d.agents;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.MainFrame;

/**
 * @author Charles Xie
 *
 */
public class EventCounterActuator implements Actuator {

	private final EventCounter counter;

	public EventCounterActuator(final EventCounter counter) {
		this.counter = counter;
	}

	@Override
	public String getName() {
		return counter.getName();
	}

	@Override
	public void actuate() {
		final int count = counter.getCount();
		String msg = "<html>";
		switch (count) {
		case 0:
			msg += "You have not changed the U-value.";
			break;
		case 1:
			msg += "You have changed the U-value only one time.<br>Is the information enough to understand the meaning of U-value?";
			break;
		case 2:
			msg += "You have changed the U-value twice.<br>What did you find?";
			break;
		case 3:
			msg += "You have changed the U-value three times.<br>Have you tried to look at the differences in energy use?";
			break;
		}
		JOptionPane.showMessageDialog(MainFrame.getInstance(), msg + "</html>", "Advice", JOptionPane.INFORMATION_MESSAGE);
	}

}
