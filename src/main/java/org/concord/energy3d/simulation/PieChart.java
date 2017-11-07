package org.concord.energy3d.simulation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Arc2D;
import java.text.NumberFormat;

import javax.swing.JComponent;

/**
 * @author Charles Xie
 * 
 */

public class PieChart extends JComponent {

	private static final long serialVersionUID = 1L;
	private NumberFormat format;
	private Color[] colors;
	private String[] legends;
	private double sum;
	private double[] percents;
	private Arc2D.Double[] arcs;
	private String unit;
	private String info;
	private String details;
	private boolean popup;
	private final BasicStroke thinStroke = new BasicStroke(1);
	private final BasicStroke thickStroke = new BasicStroke(2);
	private final Color normalColor = Color.BLACK;
	private final Color highlightColor = Color.YELLOW;
	private int selectedIndex = -1;

	public PieChart(final double[] data, final Color[] colors, final String[] legends, final String unit, final String info, final String details, final boolean popup) {
		if (popup) {
			setPreferredSize(new Dimension(450, 300));
		}
		this.popup = popup;
		format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(2);
		this.colors = colors;
		this.legends = legends;
		this.unit = unit;
		this.info = info;
		this.details = details;
		for (final double x : data) {
			sum += x;
		}
		percents = new double[data.length];
		arcs = new Arc2D.Double[data.length];
		for (int i = 0; i < percents.length; i++) {
			percents[i] = data[i] / sum;
			arcs[i] = new Arc2D.Double();
		}
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(final MouseEvent e) {
				selectedIndex = -1;
				for (int i = 0; i < arcs.length; i++) {
					if (arcs[i].contains(e.getX(), e.getY())) {
						selectedIndex = i;
						repaint();
						setToolTipText(legends[i] + ": " + unit + Math.round(data[i]));
						return;
					}
				}
				String toolTipText = "<html><h4>Data:</h4><hr>";
				for (int i = 0; i < data.length; i++) {
					toolTipText += legends[i] + ": " + unit + Math.round(data[i]) + "<br>";
				}
				toolTipText += "<hr>Hover mouse over the pie chart to view the numbers.";
				setToolTipText(toolTipText + (popup ? "" : "<br>Double-click to enlarge this chart.") + "</html>");
				repaint();
			}
		});
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
		g2.fillRect(0, 0, width, height);
		g2.setFont(new Font("Arial", Font.PLAIN, popup ? 10 : 8));

		final int legendX1 = width / 20;
		int legendX2 = width / 6;
		final Rectangle bound = new Rectangle(width / 20, height / 10, width / 2, width / 2);
		final int r = bound.x + bound.width + legendX1;
		final int s = bound.y + (popup ? 10 : 0);

		float t = 0.0f;
		final int n = percents.length;
		final FontMetrics fm = g.getFontMetrics();
		for (final String l : legends) {
			final int len = fm.stringWidth(l);
			if (legendX2 < len) {
				legendX2 = len;
			}
		}
		legendX2 += legendX1 + 8;

		for (int i = 0; i < n; i++) {
			g2.setColor(colors[i]);
			arcs[i].setArc(bound, t, percents[i] * 360.0f, Arc2D.PIE);
			g2.fill(arcs[i]);
			g2.fillRect(r, s + i * 20, 10, 10);
			if (i == selectedIndex) {
				g2.setColor(highlightColor);
				g2.setStroke(thickStroke);
			} else {
				g2.setColor(normalColor);
				g2.setStroke(thinStroke);
			}
			g2.draw(arcs[i]);
			g2.setColor(Color.BLACK);
			g2.drawRect(r, s + i * 20, 10, 10);
			g2.drawString(legends[i], r + legendX1, s + 10 + i * 20);
			g2.drawString(format.format(percents[i] * 100.0) + "%", r + legendX2, s + 10 + i * 20);
			t += percents[i] * 360.0f;
		}

		g2.setFont(new Font("Arial", Font.PLAIN | Font.BOLD, 11));
		if (info != null) {
			final String total = info + ", Total: " + unit + (int) sum;
			g2.drawString(total, (width - fm.stringWidth(total)) / 2, height - 30);
		}
		if (details != null) {
			g2.setFont(new Font("Arial", Font.PLAIN, 11));
			g2.drawString(details, (width - fm.stringWidth(details)) / 2, height - 15);
		}

	}

}
