package org.concord.energy3d.simulation;

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

class PieChart extends JComponent {

	private static final long serialVersionUID = 1L;
	private NumberFormat format;
	private float[] data;
	private Color[] colors;
	private String[] legends;
	private float sum;
	private float[] percents;
	private Arc2D.Float[] arcs;
	private String unit;
	private String info;
	private String details;

	public PieChart(float[] data, Color[] colors, String[] legends, String unit, String info, String details) {
		setPreferredSize(new Dimension(450, 300));
		format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(2);
		this.data = data;
		this.colors = colors;
		this.legends = legends;
		this.unit = unit;
		this.info = info;
		this.details = details;
		for (float x : data)
			sum += x;
		percents = new float[data.length];
		arcs = new Arc2D.Float[data.length];
		for (int i = 0; i < percents.length; i++) {
			percents[i] = data[i] / sum;
			arcs[i] = new Arc2D.Float();
		}
		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				for (int i = 0; i < arcs.length; i++) {
					if (arcs[i].contains(e.getX(), e.getY())) {
						setToolTipText(PieChart.this.legends[i] + ": " + PieChart.this.unit + Math.round(PieChart.this.data[i]));
						return;
					}
				}
				setToolTipText("Hover mouse over the pie chart to view the numbers");
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

		Rectangle bound = new Rectangle(width / 20, height / 10, width / 2, width / 2);
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

		g2.setFont(new Font("Arial", Font.PLAIN | Font.BOLD, 11));
		String total = info + ", Total: " + unit + (int) sum;
		g2.drawString(total, (width - fm.stringWidth(total)) / 2, height - 30);
		if (details != null) {
			g2.setFont(new Font("Arial", Font.PLAIN, 11));
			g2.drawString(details, (width - fm.stringWidth(details)) / 2, height - 15);
		}

	}

}
