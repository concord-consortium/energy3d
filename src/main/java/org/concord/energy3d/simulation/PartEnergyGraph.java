package org.concord.energy3d.simulation;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.Collections;
import java.util.List;

/**
 * Graph for seasonal analysis of energy related to a house part.
 * 
 * @author Charles Xie
 * 
 */
class PartEnergyGraph extends Graph {

	private static final long serialVersionUID = 1L;

	PartEnergyGraph() {
		super();
	}

	@Override
	void drawCurves(Graphics2D g2) {

		for (String key : data.keySet()) {

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
				g2.setStroke(thin);
				g2.draw(path);

				g2.setStroke(thin);
				for (int i = 0; i < list.size(); i++) {
					dataX = left + dx * i;
					dataY = (float) (getHeight() - top - (list.get(i) - ymin) * dy);
					if ("Solar".equals(key)) {
						drawCircle(g2, (int) Math.round(dataX - 5), (int) Math.round(dataY - 5), 10, Color.YELLOW);
					} else if ("Heat Transfer".equals(key)) {
						drawSquare(g2, (int) Math.round(dataX - 5), (int) Math.round(dataY - 5), 10, Color.GRAY);
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
		int y0 = top - 10;
		drawCircle(g2, x0, y0, 8, Color.YELLOW);
		String s = "Solar";
		g2.drawString(s + " (" + twoDecimals.format(getSum(s)) + ")", x0 + 14, y0 + 8);

		s = "Heat Transfer";
		if (data.containsKey(s)) {
			y0 += 14;
			drawSquare(g2, x0, y0, 8, Color.GRAY);
			g2.drawString(s + " (" + twoDecimals.format(getSum(s)) + ")", x0 + 14, y0 + 8);
		}

	}

}
