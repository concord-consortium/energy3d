package org.concord.energy3d.simulation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.text.NumberFormat;

import javax.swing.JComponent;

/**
 * @author Charles Xie
 * 
 */

class PieChart extends JComponent {

	private static final long serialVersionUID = 1L;
	private NumberFormat format;
	private float[] percents;
	private Color[] colors;
	private String[] legends;
	private Arc2D[] arcs;
	private Rectangle bound;

	public PieChart(float[] p, Color[] c, String[] s) {
		setPreferredSize(new Dimension(400, 300));
		format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(1);
		bound = new Rectangle(20, 20, 80, 80);
		percents = p;
		colors = c;
		legends = s;
		arcs = new Arc2D.Float[p.length];
		for (int i = 0; i < arcs.length; i++)
			arcs[i] = new Arc2D.Float();
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

		bound.setRect(width / 20, height / 10, width / 2, width / 2);
		int r = bound.x + bound.width + 20;
		int s = bound.y + 10;

		int legendX1 = width / 10;
		int legendX2 = width / 6;

		float t = 0.0f;
		int n = percents.length;
		FontMetrics fm = g.getFontMetrics();
		for (String l : legends) {
			int len = fm.stringWidth(l);
			if (legendX2 < len)
				legendX2 = len;
		}
		legendX2 += legendX1 + 8;

		for (int i = 0; i < n; i++) {
			g2.setColor(colors[i]);
			arcs[i].setArc(bound, t, percents[i] * 360.0f, Arc2D.PIE);
			g2.fill(arcs[i]);
			g2.fillRect(r, s + i * 20, 20, 10);
			g2.setColor(Color.black);
			g2.draw(arcs[i]);
			g2.drawRect(r, s + i * 20, 20, 10);
			g2.drawString(legends[i], r + legendX1, s + 10 + i * 20);
			g2.drawString(format.format(percents[i] * 100.0) + "%", r + legendX2, s + 10 + i * 20);
			t += percents[i] * 360.0f;
		}

	}

}
