package org.concord.energy3d.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.DecimalFormat;

import javax.swing.JPanel;

/**
 * A color bar used to indicate how far the current design is from a goal or a limit.
 * 
 * @author Charles Xie
 * 
 */
class ColorBar extends JPanel {

	private static final long serialVersionUID = 1L;

	private double value = 0.0;
	private double minimum = Double.NaN;
	private double maximum = 100000.0;
	private String unit = "$";
	private boolean unitPrefix = true;
	private boolean verticalLineRepresentation = true;
	private DecimalFormat decimalFormat = new DecimalFormat();

	public ColorBar(Color background, Color foreground) {
		super();
		setBackground(background);
		setForeground(foreground);
		setDecimalDigits(0);
	}

	public void setDecimalDigits(int n) {
		decimalFormat.setMaximumFractionDigits(n);
	}

	public void setUnitPrefix(boolean b) {
		unitPrefix = b;
	}

	public void setVerticalLineRepresentation(boolean b) {
		verticalLineRepresentation = b;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public void setMinimum(double minimum) {
		this.minimum = minimum;
	}

	public void setMaximum(double maximum) {
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

		if (isEnabled()) {
			if (value > maximum) {
				g2.setColor(Color.YELLOW);
			} else if (!Double.isNaN(minimum) && value < minimum) {
				g2.setColor(Color.YELLOW);
			} else {
				g2.setColor(getForeground());
			}
			double max = maximum + (Double.isNaN(minimum) ? 0 : minimum);
			g2.fillRect(1, 1, (int) Math.round(value * width / max), height);
			if (verticalLineRepresentation) {
				g2.setColor(Color.RED);
				g2.fillRect((int) Math.round(maximum * width / max), 1, 2, height - 2);
				if (!Double.isNaN(minimum))
					g2.fillRect((int) Math.round(minimum * width / max), 1, 2, height - 2);
			} else {
				g2.setColor(new Color(0xCD5C5C));
				int x1 = 0;
				if (!Double.isNaN(minimum)) {
					x1 = (int) Math.round(minimum * width / max);
					g2.fillRect(0, height - 4, x1, 4);
				}
				int x2 = (int) Math.round(maximum * width / max);
				g2.fillRect(x2, height - 4, width - x2, 4);
				g2.setColor(new Color(0x32CD32));
				g2.fillRect(x1, height - 4, x2 - x1, 4);
			}
		}

		if (value / maximum > 0.0001) {
			g2.setFont(new Font(null, Font.PLAIN, 10));
			g2.setColor(Color.BLACK);
			String s = unitPrefix ? unit + decimalFormat.format(value) : decimalFormat.format(value) + unit;
			FontMetrics fm = g2.getFontMetrics();
			g2.drawString(s, (width - fm.stringWidth(s)) / 2, (fm.getAscent() + (height - (fm.getAscent() + fm.getDescent())) / 2));
		}

	}

}
