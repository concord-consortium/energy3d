package org.concord.energy3d.simulation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

/**
 * Time graph (e.g., 24 hours or 12 months)
 * 
 * @author Charles Xie
 * 
 */
abstract class Graph extends JPanel {

	private static final long serialVersionUID = 1L;

	int top = 50, right = 50, bottom = 80, left = 50;
	BasicStroke dashed = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 2f }, 0.0f);
	BasicStroke thin = new BasicStroke(1);
	BasicStroke thick = new BasicStroke(2);
	Map<String, List<Double>> data;
	float xmin = 0;
	float xmax = 11;
	int numberOfTicks = 12;
	String xAxisLabel = "Month";
	String yAxisLabel = "Energy (kWh)";
	DecimalFormat twoDecimals;

	Graph() {
		super();
		data = new HashMap<String, List<Double>>();
		twoDecimals = new DecimalFormat();
		twoDecimals.setMaximumFractionDigits(2);
	}

	void setMinimum(float xmin) {
		this.xmin = xmin;
	}

	void setMaximum(float xmax) {
		this.xmax = xmax;
	}

	void addData(String name, double d) {
		List<Double> list = data.get(name);
		if (list == null) {
			list = new ArrayList<Double>();
			data.put(name, list);
		}
		list.add(d);
	}

	List<Double> getData(String name) {
		return data.get(name);
	}

	double getSum(String name) {
		List<Double> x = getData(name);
		if (x == null || x.isEmpty())
			return 0;
		double sum = 0;
		for (double a : x)
			sum += a;
		return sum;
	}

	int getChannel() {
		return data.size();
	}

	int getLength() {
		if (data.isEmpty())
			return 0;
		List<Double> list = null;
		for (String name : data.keySet()) {
			list = data.get(name);
			break;
		}
		return list.size();
	}

	void clearData() {
		data.clear();
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
		g2.setStroke(thick);
		g2.drawRect(left / 2, top / 2, width - (left + right) / 2, height - (top + bottom) / 2);

		g2.setColor(Color.BLACK);
		float tickWidth = (float) (width - left - right) / (float) (numberOfTicks - 1);
		for (int i = 0; i < numberOfTicks; i++) {
			String s = "" + (i + 1);
			int sWidth = g2.getFontMetrics().stringWidth(s);
			g2.drawString(s, left + tickWidth * i - sWidth / 2, height - bottom / 2 + 16);
		}
		int xAxisLabelWidth = g2.getFontMetrics().stringWidth(xAxisLabel);
		int yAxisLabelWidth = g2.getFontMetrics().stringWidth(yAxisLabel);
		g2.drawString(xAxisLabel, (width - xAxisLabelWidth) / 2, height - 10);
		g2.rotate(-Math.PI / 2, 16, (height + yAxisLabelWidth) / 2);
		g2.drawString(yAxisLabel, 16, (height + yAxisLabelWidth) / 2);
		g2.rotate(Math.PI / 2, 16, (height + yAxisLabelWidth) / 2);

		if (data.isEmpty()) {
			g2.setColor(Color.LIGHT_GRAY);
			g2.setFont(new Font("Arial", Font.PLAIN, 20));
			String info = "No data";
			int infoWidth = g2.getFontMetrics().stringWidth(info);
			g2.drawString(info, (width - infoWidth) / 2, height / 2);
		} else {
			drawCurves(g2);
		}

		drawLegends(g2);

	}

	abstract void drawLegends(Graphics2D g2);

	abstract void drawCurves(Graphics2D g2);

	static void drawCircle(Graphics g, int x, int y, int d, Color c) {
		g.setColor(c);
		g.fillOval(x, y, d, d);
		g.setColor(Color.BLACK);
		g.drawOval(x, y, d, d);

	}

	static void drawSquare(Graphics g, int x, int y, int a, Color c) {
		g.setColor(c);
		g.fillRect(x, y, a, a);
		g.setColor(Color.BLACK);
		g.drawRect(x, y, a, a);
	}

}
