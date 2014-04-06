package org.concord.energy3d.simulation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

/**
 * Graph for time and seasonal analysis.
 * 
 * @author Charles Xie
 * 
 */
class Graph extends JPanel {

	private static final long serialVersionUID = 1L;

	private int top = 50, right = 50, bottom = 80, left = 50;
	private BasicStroke dashed = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 8f }, 0.0f);
	private BasicStroke thin = new BasicStroke(1);
	private BasicStroke thick = new BasicStroke(2);
	private Map<String, List<Double>> data;
	private float xmin = 0;
	private float xmax = 11;
	private int numberOfTicks = 12;
	private final DecimalFormat twoDecimals;

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

	private double getSum(String name) {
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
		List<Double> list = data.get("Net");
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
		String xAxisLabel = "Month";
		String yAxisLabel = "Energy (kWh)";
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

			double ymax = 1, ymin = 0;
			for (String key : data.keySet()) {
				List<Double> list = data.get(key);
				if (!list.isEmpty()) {
					double max = Collections.max(list);
					double min = Collections.min(list);
					if (max > ymax)
						ymax = max;
					if (min < ymin)
						ymin = min;
				}
			}
			float dx = (float) (width - left - right) / (float) (xmax - xmin);
			float dy = (float) (height - top - bottom) / (float) (ymax - ymin);

			for (String key : data.keySet()) {

				List<Double> list = data.get(key);

				if (!list.isEmpty()) {

					if (Collections.max(list) == Collections.min(list))
						continue;

					g2.setColor(Color.BLACK);
					float dataX, dataY;
					Path2D.Float path = new Path2D.Float();
					for (int i = 0; i < list.size(); i++) {
						dataX = left + dx * i;
						dataY = (float) (height - top - (list.get(i) - ymin) * dy);
						if (i == 0)
							path.moveTo(dataX, dataY);
						else
							path.lineTo(dataX, dataY);
					}
					g2.setStroke(dashed);
					g2.draw(path);

					g2.setStroke(thin);
					for (int i = 0; i < list.size(); i++) {
						dataX = left + dx * i;
						dataY = (float) (height - top - (list.get(i) - ymin) * dy);
						if ("Windows".equals(key)) {
							drawSquare(g2, Math.round(dataX - 5), Math.round(dataY - 5), 10, Color.YELLOW);
						} else if ("Solar Panels".equals(key)) {
							drawSquare(g2, Math.round(dataX - 5), Math.round(dataY - 5), 10, Color.ORANGE);
						} else if ("Heater".equals(key)) {
							drawSquare(g2, Math.round(dataX - 5), Math.round(dataY - 5), 10, Color.RED);
						} else if ("AC".equals(key)) {
							drawSquare(g2, Math.round(dataX - 5), Math.round(dataY - 5), 10, Color.BLUE);
						} else if ("Net".equals(key)) {
							drawCircle(g2, Math.round(dataX - 5), Math.round(dataY - 5), 10, Color.GREEN);
						}
					}

				}

			}

		}

		g2.setFont(new Font("Arial", Font.PLAIN, 10));
		g2.setStroke(thin);
		int x0 = width - 80 - right;
		int y0 = top - 10;
		drawSquare(g2, x0, y0, 8, Color.YELLOW);
		String s = "Windows";
		g2.drawString(s + " (" + twoDecimals.format(getSum(s)) + ")", x0 + 14, y0 + 8);
		s = "Solar Panels";
		y0 += 12;
		drawSquare(g2, x0, y0, 8, Color.ORANGE);
		g2.drawString(s + " (" + twoDecimals.format(getSum(s)) + ")", x0 + 14, y0 + 8);
		s = "Heater";
		y0 += 12;
		drawSquare(g2, x0, y0, 8, Color.RED);
		g2.drawString(s + " (" + twoDecimals.format(getSum(s)) + ")", x0 + 14, y0 + 8);
		s = "AC";
		y0 += 12;
		drawSquare(g2, x0, y0, 8, Color.BLUE);
		g2.drawString(s + " (" + twoDecimals.format(getSum(s)) + ")", x0 + 14, y0 + 8);
		s = "Net";
		y0 += 12;
		drawCircle(g2, x0, y0, 8, Color.GREEN);
		g2.drawString(s + " (" + twoDecimals.format(getSum(s)) + ")", x0 + 14, y0 + 8);

	}

	private void drawCircle(Graphics g, int x, int y, int d, Color c) {
		g.setColor(c);
		g.fillOval(x, y, d, d);
		g.setColor(Color.BLACK);
		g.drawOval(x, y, d, d);

	}

	private void drawSquare(Graphics g, int x, int y, int a, Color c) {
		g.setColor(c);
		g.fillRect(x, y, a, a);
		g.setColor(Color.BLACK);
		g.drawRect(x, y, a, a);
	}

}
