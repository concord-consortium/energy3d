package org.concord.energy3d.simulation;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.Collections;
import java.util.List;

/**
 * Graph for seasonal analysis of building energy.
 * 
 * @author Charles Xie
 * 
 */
class BuildingEnergyGraph extends Graph {

	private static final long serialVersionUID = 1L;

	BuildingEnergyGraph() {
		super();
	}

	@Override
	void drawCurves(Graphics2D g2) {

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

	@Override
	void drawLegends(Graphics2D g2) {

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

}
