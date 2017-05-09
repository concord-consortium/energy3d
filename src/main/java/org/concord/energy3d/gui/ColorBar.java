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
	private final DecimalFormat decimalFormat = new DecimalFormat();

	public ColorBar(final Color background, final Color foreground) {
		super();
		setBackground(background);
		setForeground(foreground);
		setDecimalDigits(0);
	}

	public void setDecimalDigits(final int n) {
		decimalFormat.setMaximumFractionDigits(n);
	}

	public void setUnitPrefix(final boolean b) {
		unitPrefix = b;
	}

	public void setVerticalLineRepresentation(final boolean b) {
		verticalLineRepresentation = b;
	}

	public void setUnit(final String unit) {
		this.unit = unit;
	}

	public void setValue(final float value) {
		this.value = value;
	}

	public void setMinimum(final double minimum) {
		this.minimum = minimum;
	}

	public double getMinimum() {
		return minimum;
	}

	public void setMaximum(final double maximum) {
		this.maximum = maximum;
	}

	public double getMaximum() {
		return maximum;
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		update(g);
	}

	@Override
	public void update(final Graphics g) {

		final Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		final Dimension dim = getSize();
		final int width = dim.width;
		final int height = dim.height;
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
			final double max = maximum + (Double.isNaN(minimum) ? 0 : (minimum == 0 ? 0.1 * maximum : minimum));
			g2.fillRect(1, 1, (int) Math.round(value * width / max), height);
			if (verticalLineRepresentation) {
				g2.setColor(Color.RED);
				g2.fillRect((int) Math.round(maximum * width / max), 1, 2, height - 2);
				if (!Double.isNaN(minimum)) {
					g2.fillRect((int) Math.round(minimum * width / max), 1, 2, height - 2);
				}
			} else {
				if (!Double.isNaN(minimum)) {
					g2.setColor(new Color(0xCD5C5C));
					int x1 = 0;
					x1 = (int) Math.round(minimum * width / max);
					g2.fillRect(0, height - 4, x1, 4);
					final int x2 = (int) Math.round(maximum * width / max);
					g2.fillRect(x2, height - 4, width - x2, 4);
					g2.setColor(new Color(0x32CD32));
					g2.fillRect(x1, height - 4, x2 - x1, 4);
				}
			}
		}

		if (value / maximum > 0.000001) {
			g2.setFont(new Font(null, Font.PLAIN, 10));
			g2.setColor(Color.BLACK);
			final String s = unitPrefix ? unit + decimalFormat.format(value) : decimalFormat.format(value) + unit;
			final FontMetrics fm = g2.getFontMetrics();
			g2.drawString(s, (width - fm.stringWidth(s)) / 2, (fm.getAscent() + (height - (fm.getAscent() + fm.getDescent())) / 2));
		}

	}

}
