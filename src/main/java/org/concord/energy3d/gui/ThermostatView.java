package org.concord.energy3d.gui;

import java.awt.BasicStroke;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 *
 */
class ThermostatView extends JPanel {

	private static final long serialVersionUID = 1L;

	private BasicStroke thickStroke = new BasicStroke(3);
	private Color color = new Color(223, 67, 0);
	private Map<Float, Integer> hourlyTemperatures;
	private int selectedHour = -1;
	private int monthOfYear = -1;
	private int dayOfWeek = -1;
	private Foundation foundation;

	public ThermostatView(Foundation foundation, int monthOfYear, int dayOfWeek) {
		super();
		this.foundation = foundation;
		this.monthOfYear = monthOfYear;
		this.dayOfWeek = dayOfWeek;
		hourlyTemperatures = Collections.synchronizedMap(new LinkedHashMap<Float, Integer>());
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				processMouseClicked(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
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

	public void removeSelectedHour() {
		if (selectedHour > 0) {
			Object key1 = hourlyTemperatures.keySet().toArray()[selectedHour - 1];
			Object key2 = hourlyTemperatures.keySet().toArray()[selectedHour];
			int earlyTemperature = hourlyTemperatures.get(key1);
			hourlyTemperatures.put((Float) key2, earlyTemperature);
			foundation.getThermostat().setTemperature(monthOfYear, dayOfWeek, selectedHour, earlyTemperature);
			selectedHour = -1;
		}
		repaint();
	}

	public void setHandle(float x, int y) {
		hourlyTemperatures.put(x, y);
	}

	public void clearHandles() {
		hourlyTemperatures.clear();
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
		g2.setFont(new Font("Arial", Font.BOLD, 10));
		FontMetrics fm = g2.getFontMetrics();

		boolean drawHandle = true;
		synchronized (hourlyTemperatures) {
			Object[] keys = hourlyTemperatures.keySet().toArray();
			for (int i = 0; i < keys.length; i++) {
				if (i > 0) {
					drawHandle = !Util.isZero(hourlyTemperatures.get(keys[i]) - hourlyTemperatures.get(keys[i - 1]));
				}
				if (drawHandle) {
					float radius = 0.4f * height;
					int diameter = (int) (2 * radius);
					g2.setColor(color);
					float cx = (Float) keys[i] * width;
					float cy = height / 2;
					g2.fillOval((int) (cx - radius), (int) (cy - radius), diameter, diameter);
					if (selectedHour == i) {
						g2.setColor(Color.YELLOW);
						g2.setStroke(thickStroke);
						g2.drawOval((int) (cx - radius), (int) (cy - radius), diameter, diameter);
					}
					g2.setColor(Color.LIGHT_GRAY);
					String reading = hourlyTemperatures.get(keys[i]) + "";
					g2.drawString(reading, cx - fm.stringWidth(reading) / 2, cy + (fm.getAscent() - fm.getDescent()) / 2);
				}
			}
		}

	}

	private void processMouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int width = getWidth();
		int height = getHeight();
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		synchronized (hourlyTemperatures) {
			Object[] keys = hourlyTemperatures.keySet().toArray();
			for (int i = 0; i < keys.length; i++) {
				float hr = 0.4f * height;
				float hx = (Float) keys[i] * width;
				float hy = height / 2;
				if ((x - hx) * (x - hx) + (y - hy) * (y - hy) < hr * hr) {
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					break;
				}
			}
		}
		repaint();
	}

	private void processMouseDragged(MouseEvent e) {

	}

	private void processMouseClicked(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int width = getWidth();
		int height = getHeight();
		selectedHour = -1;
		synchronized (hourlyTemperatures) {
			Object[] keys = hourlyTemperatures.keySet().toArray();
			for (int i = 0; i < keys.length; i++) {
				float hr = 0.4f * height;
				float hx = (Float) keys[i] * width;
				float hy = height / 2;
				if ((x - hx) * (x - hx) + (y - hy) * (y - hy) < hr * hr) {
					selectedHour = i;
					break;
				}
			}
		}
		if (e.getClickCount() >= 2) {
			if (selectedHour >= 0) {
				Object key = hourlyTemperatures.keySet().toArray()[selectedHour];
				int newTemperature = e.isShiftDown() ? hourlyTemperatures.get(key) - 1 : hourlyTemperatures.get(key) + 1;
				hourlyTemperatures.put((Float) key, newTemperature);
				foundation.getThermostat().setTemperature(monthOfYear, dayOfWeek, selectedHour, newTemperature);
			}
		}
		repaint();
	}

}
