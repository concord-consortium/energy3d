package org.concord.energy3d.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JPanel;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.undo.AdjustThermostatCommand;

/**
 * This allows users to change the temperature of a given hour of all days in the week.
 * 
 * @author Charles Xie
 * 
 *
 */
class HourPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private ThermostatView[] thermostatButtons;
	private int selectedHour = -1;
	private int previousY;
	private boolean increaseTemperature = false;
	private boolean decreaseTemperature = false;
	private Foundation foundation;

	public HourPanel(Foundation foundation, ThermostatView[] thermostatButtons) {
		this.foundation = foundation;
		this.thermostatButtons = thermostatButtons;
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				processMousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				processMouseReleased(e);
			}
		});
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				processMouseMoved(e);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				processMouseDragged(e);
			}
		});
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		update(g);
	}

	public void update(Graphics g) {

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		Dimension dim = getSize();
		int width = dim.width;
		int height = dim.height;
		g2.setColor(getBackground());
		g2.fillRect(0, 0, width, height);
		g2.setFont(new Font("Arial", Font.PLAIN, 10));

		float delta = (width - 0.8f * thermostatButtons[0].getHeight()) / 25.0f;
		g2.setColor(Color.DARK_GRAY);
		String hourString;
		FontMetrics fm = g2.getFontMetrics();
		for (int i = 0; i < 25; i++) {
			hourString = i == 24 ? "All" : i + "";
			g2.drawString(hourString, (int) (delta * (i + 1) - fm.stringWidth(hourString) / 2), height - 5);
		}

	}

	private void processMouseMoved(MouseEvent e) {
		int x = e.getX();
		int width = getWidth();
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		float delta = (width - 0.8f * thermostatButtons[0].getHeight()) / 25.0f;
		for (int i = 0; i < 25; i++) {
			if (Math.abs(x - delta * (i + 1)) < 0.25 * delta) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				break;
			}
		}
		repaint();
	}

	private void processMousePressed(MouseEvent e) {
		int x = e.getX();
		int width = getWidth();
		previousY = e.getY();
		selectedHour = -1;
		float delta = (width - 0.8f * thermostatButtons[0].getHeight()) / 25.0f;
		for (int i = 0; i < 25; i++) {
			if (Math.abs(x - delta * (i + 1)) < 0.25 * delta) {
				selectedHour = i;
				break;
			}
		}
		repaint();
	}

	private void processMouseDragged(MouseEvent e) {
		int y = e.getY();
		increaseTemperature = y < previousY;
		decreaseTemperature = y > previousY;
		previousY = y;
		repaint();
	}

	private void processMouseReleased(MouseEvent e) {
		if (!increaseTemperature && !decreaseTemperature)
			return;
		if (selectedHour >= 0) {
			SceneManager.getInstance().getUndoManager().addEdit(new AdjustThermostatCommand(foundation));
			for (int i = 0; i < thermostatButtons.length; i++) {
				Object[] keys = thermostatButtons[i].hourlyTemperatures.keySet().toArray();
				Object selectedKey = keys[selectedHour];
				int newTemperature = thermostatButtons[i].hourlyTemperatures.get(selectedKey);
				if (increaseTemperature)
					newTemperature++;
				if (decreaseTemperature)
					newTemperature--;
				thermostatButtons[i].hourlyTemperatures.put((Float) selectedKey, newTemperature);
				foundation.getThermostat().setTemperature(thermostatButtons[i].monthOfYear, thermostatButtons[i].dayOfWeek, selectedHour, newTemperature);
				thermostatButtons[i].repaint();
			}
			if (selectedHour == 24) {
				for (int j = 0; j < thermostatButtons.length; j++) {
					Object[] keys = thermostatButtons[j].hourlyTemperatures.keySet().toArray();
					for (int h = 0; h < 24; h++) {
						int t2 = thermostatButtons[j].hourlyTemperatures.get(keys[h]);
						if (increaseTemperature)
							t2++;
						if (decreaseTemperature)
							t2--;
						thermostatButtons[j].hourlyTemperatures.put((Float) keys[h], t2);
						foundation.getThermostat().setTemperature(thermostatButtons[j].monthOfYear, thermostatButtons[j].dayOfWeek, h, t2);
					}
					thermostatButtons[j].repaint();
				}
			}
			selectedHour = -1;
		}
		repaint();
	}

}