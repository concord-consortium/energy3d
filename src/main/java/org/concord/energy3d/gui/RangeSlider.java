package org.concord.energy3d.gui;

import java.awt.BasicStroke;
import java.awt.Color;
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
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

/**
 * A slider that can have as many handles as wanted to represent multiple ranges.
 * 
 * @author Charles Xie
 *
 */
class RangeSlider extends JPanel {

	private static final long serialVersionUID = 1L;

	private BasicStroke thickStroke = new BasicStroke(3);
	private Color color = new Color(223, 67, 0);
	private Map<Float, Integer> handles;
	private int selectedHandleIndex = -1;

	public RangeSlider(int numberOfSteps) {
		super();
		handles = Collections.synchronizedMap(new HashMap<Float, Integer>());
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
				selectedHandleIndex = -1;
				repaint();
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

	public void setHandle(float x, int y) {
		handles.put(x, y);
	}

	public void clearHandles() {
		handles.clear();
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

		synchronized (handles) {
			Object[] keys = handles.keySet().toArray();
			for (int i = 0; i < keys.length; i++) {
				float radius = 0.4f * height;
				int diameter = (int) (2 * radius);
				g2.setColor(color);
				float cx = (Float) keys[i] * width;
				float cy = height / 2;
				g2.fillOval((int) (cx - radius), (int) (cy - radius), diameter, diameter);
				if (selectedHandleIndex == i) {
					g2.setColor(Color.YELLOW);
					g2.setStroke(thickStroke);
					g2.drawOval((int) (cx - radius), (int) (cy - radius), diameter, diameter);
				}
				g2.setColor(Color.LIGHT_GRAY);
				String reading = handles.get(keys[i]) + "";
				g2.drawString(reading, cx - fm.stringWidth(reading) / 2, cy + (fm.getAscent() - fm.getDescent()) / 2);
			}
		}

	}

	private void processMouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int width = getWidth();
		int height = getHeight();
		selectedHandleIndex = -1;
		synchronized (handles) {
			Object[] keys = handles.keySet().toArray();
			for (int i = 0; i < keys.length; i++) {
				float hr = 0.4f * height;
				float hx = (Float) keys[i] * width;
				float hy = height / 2;
				if ((x - hx) * (x - hx) + (y - hy) * (y - hy) < hr * hr) {
					selectedHandleIndex = i;
					break;
				}
			}
		}
		repaint();
	}

	private void processMouseDragged(MouseEvent e) {

	}

}
