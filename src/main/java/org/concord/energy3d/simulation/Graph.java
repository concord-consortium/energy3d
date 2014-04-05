package org.concord.energy3d.simulation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

/**
 * Graph for time and seasonal analyses.
 * 
 * @author Charles Xie
 * 
 */
class Graph extends JPanel {

	private static final long serialVersionUID = 1L;

	private int top = 50, right = 20, bottom = 50, left = 20;
	private BasicStroke dashed = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 8f }, 0.0f);
	private BasicStroke thin = new BasicStroke(1);
	private BasicStroke thick = new BasicStroke(2);
	List<Double> data;
	private float xmin = 0;
	private float xmax = 12;

	Graph() {
		super();
		data = new ArrayList<Double>();
	}

	void setMinimum(float xmin) {
		this.xmin = xmin;
	}

	void setMaximum(float xmax) {
		this.xmax = xmax;
	}

	double getData(int i) {
		if (data.isEmpty())
			return Double.NaN;
		return data.get(i);
	}

	void addData(double d) {
		data.add(d);
	}

	void clearData() {
		data.clear();
	}

	static Color getOppositeColor(Color c) {
		return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue());
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
		g2.setColor(Color.GRAY);
		g2.drawRect(left / 2, top / 2, width - (left + right) / 2, height - (top + bottom) / 2);

		if (data.isEmpty()) {
			g2.setColor(Color.LIGHT_GRAY);
			g2.drawString("No data", width / 2, height / 2);
		} else {
			g2.setColor(Color.BLACK);
			int dx = Math.round((width - left - right) / (xmax - xmin));
			double ymax = Collections.max(data);
			double ymin = Collections.min(data);
			int dy = Math.round((height - top - bottom) / (float) (ymax - ymin));

			float dataX, dataY;
			Path2D.Float path = new Path2D.Float();
			for (int i = 0; i < data.size(); i++) {
				dataX = left + dx * i;
				dataY = (float) (height - top - (data.get(i) - ymin) * dy);
				if (i == 0)
					path.moveTo(dataX, dataY);
				else
					path.lineTo(dataX, dataY);
			}
			g2.setStroke(thick);
			g2.draw(path);

			g2.setColor(Color.GRAY);
			for (int i = 0; i < data.size(); i++) {
				dataX = left + dx * i;
				dataY = (float) (height - top - (data.get(i) - ymin) * dy);
				g2.fillOval(Math.round(dataX - 5), Math.round(dataY - 5), 10, 10);
			}

		}
	}
}
