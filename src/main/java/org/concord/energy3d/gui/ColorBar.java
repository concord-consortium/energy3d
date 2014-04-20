package org.concord.energy3d.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * A color bar used to indicate how far the current design is from a goal or a limit.
 * 
 * @author Charles Xie
 * 
 */
public class ColorBar extends JPanel {

	private static final long serialVersionUID = 1L;

	private float value = 0.0f;
	private float maximum = 100000.0f;

	public ColorBar(Color background, Color foreground) {
		super();
		setBackground(background);
		setForeground(foreground);
	}

	public void setValue(float value) {
		this.value = value;
	}

	public void setMaximum(float maximum) {
		this.maximum = maximum;
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
		g2.fillRect(1, 1, width - 3, height - 3);
		g2.setColor(getBackground().darker());
		g2.drawRect(0, 0, width - 1, height - 1);

		g2.setColor(getForeground());
		g2.fillRect(0, 0, Math.round(value * width / maximum), height);

		if (value / maximum > 0.001) {
			g2.setFont(new Font("Arial", Font.PLAIN, 10));
			g2.setColor(getForeground().darker());
			String s = "$" + value;
			FontMetrics fm = g2.getFontMetrics();
			g2.drawString(s, (width - fm.stringWidth(s)) / 2, (fm.getAscent() + (height - (fm.getAscent() + fm.getDescent())) / 2));
		}

	}

}
