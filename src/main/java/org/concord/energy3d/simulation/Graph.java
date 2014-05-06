package org.concord.energy3d.simulation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

/**
 * @author Charles Xie
 * 
 */
abstract class Graph extends JPanel {

	private static final long serialVersionUID = 1L;

	static final byte DEFAULT = 0;
	static final byte SENSOR = 1;

	int top = 50, right = 50, bottom = 80, left = 90;
	BasicStroke dashed = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 2f }, 0.0f);
	BasicStroke thin = new BasicStroke(1);
	BasicStroke thick = new BasicStroke(2);
	Map<String, List<Double>> data;
	Map<String, Boolean> hideData;
	Map<Integer, Boolean> hideRuns;
	double xmin = 0;
	double xmax = 11;
	double ymin = 0;
	double ymax = 1;
	double dx;
	double dy;
	int symbolSize = 8;
	int numberOfTicks = 12;
	String xAxisLabel = "Month";
	String yAxisLabel = "Energy (kWh)";
	DecimalFormat twoDecimals;
	byte type = DEFAULT;
	static Map<String, Color> colors;

	static {
		colors = new HashMap<String, Color>();
		colors.put("Solar", Color.ORANGE);
		colors.put("Heat Gain", Color.GRAY);
		colors.put("Windows", colors.get("Solar"));
		colors.put("Solar Panels", Color.GREEN.darker());
		colors.put("Heater", Color.RED);
		colors.put("AC", Color.BLUE);
		colors.put("Net", Color.MAGENTA);
	}

	Graph() {
		super();
		data = new HashMap<String, List<Double>>();
		hideData = new HashMap<String, Boolean>();
		hideRuns = new HashMap<Integer, Boolean>();
		twoDecimals = new DecimalFormat();
		twoDecimals.setMaximumFractionDigits(2);
	}

	/* keep the records by their class types */
	private List<Results> getRecords() {
		if (this instanceof DailyGraph)
			return DailyGraph.records;
		if (this instanceof AngularGraph)
			return AngularGraph.records;
		return AnnualGraph.records;
	}

	void keepResults() {
		if (data.isEmpty())
			return;
		getRecords().add(new Results(data));
	}

	boolean hasRecords() {
		return !getRecords().isEmpty();
	}

	private boolean areRecordsShown() {
		for (Results r : getRecords()) {
			Boolean x = hideRuns.get(r.getID());
			if (x == null || x == Boolean.FALSE)
				return true;
		}
		return false;
	}

	void clearRecords() {
		getRecords().clear();
	}

	void setMinimum(float xmin) {
		this.xmin = xmin;
	}

	void setMaximum(float xmax) {
		this.xmax = xmax;
	}

	Set<String> getDataNames() {
		return data.keySet();
	}

	void addData(String name, double d) {
		List<Double> list = data.get(name);
		if (list == null) {
			list = new ArrayList<Double>();
			data.put(name, list);
		}
		list.add(d);
	}

	void hideData(String name, boolean hidden) {
		hideData.put(name, hidden);
	}

	boolean isDataHidden(String name) {
		Boolean b = hideData.get(name);
		return b != null ? b : false;
	}

	void hideRun(int id, boolean hidden) {
		hideRuns.put(id, hidden);
	}

	boolean isRunHidden(int id) {
		Boolean b = hideRuns.get(id);
		return b != null ? b : false;
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

	boolean hasData() {
		return !data.isEmpty();
	}

	void clearData() {
		data.clear();
	}

	abstract double getXAxisLabelScalingFactor();

	abstract String getXAxisUnit();

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
			String s = Math.round((i + 1) * getXAxisLabelScalingFactor()) + getXAxisUnit();
			int sWidth = g2.getFontMetrics().stringWidth(s);
			g2.drawString(s, left + tickWidth * i - sWidth / 2, height - bottom / 2 + 16);
		}
		int xAxisLabelWidth = g2.getFontMetrics().stringWidth(xAxisLabel);
		int yAxisLabelWidth = g2.getFontMetrics().stringWidth(yAxisLabel);
		g2.drawString(xAxisLabel, (width - xAxisLabelWidth) / 2, height - 10);
		g2.rotate(-Math.PI / 2, 16, (height + yAxisLabelWidth) / 2);
		g2.drawString(yAxisLabel, 16, (height + yAxisLabelWidth) / 2);
		g2.rotate(Math.PI / 2, 16, (height + yAxisLabelWidth) / 2);

		boolean showRecords = hasRecords() && areRecordsShown();
		if (showRecords) {
			recalculateBounds();
			drawRecords(g2);
		} else {
			ymin = 0;
			ymax = 1;
		}

		if (data.isEmpty()) {
			g2.setColor(Color.LIGHT_GRAY);
			g2.setFont(new Font("Arial", Font.PLAIN, 20));
			String info = "No new data";
			int infoWidth = g2.getFontMetrics().stringWidth(info);
			g2.drawString(info, (width - infoWidth) / 2, height / 2);
		} else {
			if (!showRecords)
				recalculateBounds();
			int digits = String.valueOf(Math.round(ymax - ymin)).length() - 1;
			digits = (int) Math.pow(10, digits);
			int i1 = (int) Math.round(ymin / digits) - 2;
			int i2 = (int) Math.round(ymax / digits) + 2;
			int hVal, hPos;
			for (int i = i1; i <= i2; i++) {
				hVal = i * digits;
				hPos = (int) (getHeight() - top - (hVal - ymin) * dy);
				if (hPos > top / 2 && hPos < getHeight() - bottom / 2)
					drawHorizontalLine(g2, hPos, Integer.toString(hVal));
			}
			drawCurves(g2);
		}

		drawLegends(g2);

	}

	private void recalculateBounds() {
		if (!getRecords().isEmpty()) {
			for (Results r : getRecords()) {
				if (isRunHidden(r.getID()))
					continue;
				double[] bound = r.getBound();
				if (bound[0] < ymin)
					ymin = bound[0];
				if (bound[1] > ymax)
					ymax = bound[1];
			}
		}
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
		dx = (float) (getWidth() - left - right) / (float) (xmax - xmin);
		dy = (float) (getHeight() - top - bottom) / (float) (ymax - ymin);
	}

	void drawRecords(Graphics2D g2) {

		double dataX, dataY;
		Path2D.Float path = new Path2D.Float();
		for (Results r : getRecords()) {

			if (isRunHidden(r.getID()))
				continue;

			Map<String, List<Double>> x = r.getData();
			for (String key : x.keySet()) {
				if (isDataHidden(key))
					continue;
				List<Double> list = x.get(key);
				path.reset();
				for (int i = 0; i < list.size(); i++) {
					dataX = left + dx * i;
					dataY = (float) (getHeight() - top - (list.get(i) - ymin) * dy);
					if (i == 0) {
						path.moveTo(dataX, dataY);
						g2.setColor(Color.GRAY);
						g2.drawString(Integer.toString(r.getID()), (int) dataX - 8, (int) dataY + 5);
					} else {
						path.lineTo(dataX, dataY);
					}
				}
				g2.setColor(colors.get(key));
				g2.setStroke(thin);
				g2.draw(path);
			}

		}

	}

	abstract void drawLegends(Graphics2D g2);

	abstract void drawCurves(Graphics2D g2);

	void drawBuildingCurves(Graphics2D g2) {

		for (String key : data.keySet()) {

			if (isDataHidden(key))
				continue;

			List<Double> list = data.get(key);

			if (!list.isEmpty()) {

				if (Collections.max(list) == Collections.min(list))
					continue;

				g2.setColor(Color.BLACK);
				double dataX, dataY;
				Path2D.Float path = new Path2D.Float();
				for (int i = 0; i < list.size(); i++) {
					dataX = left + dx * i;
					dataY = (float) (getHeight() - top - (list.get(i) - ymin) * dy);
					if (i == 0)
						path.moveTo(dataX, dataY);
					else
						path.lineTo(dataX, dataY);
				}
				g2.setStroke("Net".equals(key) ? thick : dashed);
				g2.draw(path);

				g2.setStroke(thin);
				Color c = colors.get(key);
				for (int i = 0; i < list.size(); i++) {
					dataX = left + dx * i;
					dataY = (float) (getHeight() - top - (list.get(i) - ymin) * dy);
					if ("Windows".equals(key)) {
						drawDiamond(g2, (int) Math.round(dataX), (int) Math.round(dataY), 2 * symbolSize / 3, c);
					} else if ("Solar Panels".equals(key)) {
						drawSquare(g2, (int) Math.round(dataX - symbolSize / 2), (int) Math.round(dataY - symbolSize / 2), symbolSize, c);
					} else if ("Heater".equals(key)) {
						drawSquare(g2, (int) Math.round(dataX - symbolSize / 2), (int) Math.round(dataY - symbolSize / 2), symbolSize, c);
					} else if ("AC".equals(key)) {
						drawSquare(g2, (int) Math.round(dataX - symbolSize / 2), (int) Math.round(dataY - symbolSize / 2), symbolSize, c);
					} else if ("Net".equals(key)) {
						drawCircle(g2, (int) Math.round(dataX - symbolSize / 2 + 1), (int) Math.round(dataY - symbolSize / 2 + 1), symbolSize - 2, c);
					}
				}

			}

		}

	}

	void drawBuildingLegends(Graphics2D g2) {

		g2.setFont(new Font("Arial", Font.PLAIN, 10));
		g2.setStroke(thin);
		int x0 = getWidth() - 100 - right;

		String s = "Windows";
		int y0 = top - 10;
		if (!isDataHidden(s)) {
			drawDiamond(g2, x0 + 4, y0 + 3, 5, colors.get(s));
			g2.drawString(s + " (" + twoDecimals.format(getSum(s)) + ")", x0 + 14, y0 + 8);
		}

		s = "Solar Panels";
		y0 += 12;
		if (!isDataHidden(s)) {
			drawSquare(g2, x0, y0, 8, colors.get(s));
			g2.drawString(s + " (" + twoDecimals.format(getSum(s)) + ")", x0 + 14, y0 + 8);
		}

		s = "Heater";
		y0 += 12;
		if (!isDataHidden(s)) {
			drawSquare(g2, x0, y0, 8, colors.get(s));
			g2.drawString(s + " (" + twoDecimals.format(getSum(s)) + ")", x0 + 14, y0 + 8);
		}

		s = "AC";
		y0 += 12;
		if (!isDataHidden(s)) {
			drawSquare(g2, x0, y0, 8, colors.get(s));
			g2.drawString(s + " (" + twoDecimals.format(getSum(s)) + ")", x0 + 14, y0 + 8);
		}

		s = "Net";
		y0 += 12;
		if (!isDataHidden(s)) {
			drawCircle(g2, x0, y0, 8, colors.get(s));
			g2.drawString(s + " (" + twoDecimals.format(getSum(s)) + ")", x0 + 14, y0 + 8);
		}

	}

	void drawPartCurves(Graphics2D g2) {

		Path2D.Float path = new Path2D.Float();

		for (String key : data.keySet()) {

			if (isDataHidden(key))
				continue;

			List<Double> list = data.get(key);

			if (!list.isEmpty()) {

				if (Collections.max(list) == Collections.min(list))
					continue;

				g2.setColor(Color.BLACK);
				path.reset();
				double dataX, dataY;
				for (int i = 0; i < list.size(); i++) {
					dataX = left + dx * i;
					dataY = (float) (getHeight() - top - (list.get(i) - ymin) * dy);
					if (i == 0)
						path.moveTo(dataX, dataY);
					else
						path.lineTo(dataX, dataY);
				}
				g2.setStroke(thin);
				g2.draw(path);

				g2.setStroke(thin);
				switch (type) {
				case SENSOR:
					for (int i = 0; i < list.size(); i++) {
						dataX = left + dx * i;
						dataY = (float) (getHeight() - top - (list.get(i) - ymin) * dy);
						if (i == 0) {
							int pound = key.indexOf("#");
							String s = key.substring(pound);
							g2.drawString(s, (int) dataX - 28, (int) dataY + 5);
						}
						if (key.startsWith("Light"))
							drawDiamond(g2, (int) Math.round(dataX), (int) Math.round(dataY), 2 * symbolSize / 3, colors.get("Solar"));
						else if (key.startsWith("Heat Flux"))
							drawSquare(g2, (int) Math.round(dataX - symbolSize / 2), (int) Math.round(dataY - symbolSize / 2), symbolSize, colors.get("Heat Gain"));
					}
					break;
				default:
					Color c = colors.get(key);
					for (int i = 0; i < list.size(); i++) {
						dataX = left + dx * i;
						dataY = (float) (getHeight() - top - (list.get(i) - ymin) * dy);
						if ("Solar".equals(key))
							drawDiamond(g2, (int) Math.round(dataX), (int) Math.round(dataY), 2 * symbolSize / 3, c);
						else if ("Heat Gain".equals(key))
							drawSquare(g2, (int) Math.round(dataX - symbolSize / 2), (int) Math.round(dataY - symbolSize / 2), symbolSize, c);
					}
				}

			}

		}

	}

	void drawPartLegends(Graphics2D g2) {

		g2.setFont(new Font("Arial", Font.PLAIN, 10));
		g2.setStroke(thin);
		int x0 = getWidth() - right;
		int y0 = top - 10;

		switch (type) {
		case SENSOR:
			x0 -= 50;
			drawDiamond(g2, x0 + 4, y0 + 4, 5, colors.get("Solar"));
			g2.drawString("Light", x0 + 14, y0 + 8);
			y0 += 14;
			drawSquare(g2, x0, y0, 8, colors.get("Heat Gain"));
			g2.drawString("Heat Flux", x0 + 14, y0 + 8);
			break;
		default:
			x0 -= 100;
			String s = "Solar";
			if (data.containsKey(s) && !isDataHidden(s)) {
				drawDiamond(g2, x0 + 4, y0 + 4, 5, colors.get(s));
				g2.drawString(s + " (" + twoDecimals.format(getSum(s)) + ")", x0 + 14, y0 + 8);
			}
			s = "Heat Gain";
			if (data.containsKey(s) && !isDataHidden(s)) {
				y0 += 14;
				drawSquare(g2, x0, y0, 8, colors.get(s));
				g2.drawString(s + " (" + twoDecimals.format(getSum(s)) + ")", x0 + 14, y0 + 8);
			}
		}

	}

	void drawHorizontalLine(Graphics2D g2, int yValue, String yLabel) {
		g2.setStroke(thin);
		g2.setColor(Color.LIGHT_GRAY);
		g2.drawLine(left / 2, yValue, getWidth() - right / 2, yValue);
		g2.setColor(Color.BLACK);
		int yLabelWidth = g2.getFontMetrics().stringWidth(yLabel);
		g2.drawString(yLabel, left / 2 - 5 - yLabelWidth, yValue + 4);
	}

	static void drawCircle(Graphics g, int upperLeftX, int upperLeftY, int d, Color c) {
		g.setColor(c);
		g.fillOval(upperLeftX, upperLeftY, d, d);
		g.setColor(Color.BLACK);
		g.drawOval(upperLeftX, upperLeftY, d, d);

	}

	static void drawSquare(Graphics g, int upperLeftX, int upperLeftY, int a, Color c) {
		g.setColor(c);
		g.fillRect(upperLeftX, upperLeftY, a, a);
		g.setColor(Color.BLACK);
		g.drawRect(upperLeftX, upperLeftY, a, a);
	}

	static void drawDiamond(Graphics g, int x, int y, int a, Color c) {
		g.setColor(c);
		Polygon p = new Polygon();
		p.addPoint(x, y - a);
		p.addPoint(x + a, y);
		p.addPoint(x, y + a);
		p.addPoint(x - a, y);
		g.fillPolygon(p);
		g.setColor(Color.BLACK);
		g.drawPolygon(p);
	}

}
