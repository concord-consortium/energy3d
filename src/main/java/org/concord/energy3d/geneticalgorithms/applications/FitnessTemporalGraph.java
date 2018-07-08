package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 *
 */
class FitnessTemporalGraph extends AbstractGraph {

	private static final long serialVersionUID = 1L;
	private final Individual[] individuals;
	private double maxFitness = -Double.MAX_VALUE;
	private double minFitness = Double.MAX_VALUE;
	private int length;

	public FitnessTemporalGraph(final Individual[] individuals) {

		super();

		this.individuals = individuals;
		for (final Individual i : individuals) {
			if (i != null && !Double.isNaN(i.getFitness())) {
				final double f = i.getFitness();
				if (f > maxFitness) {
					maxFitness = f;
				}
				if (f < minFitness) {
					minFitness = f;
				}
				length++;
			}
		}

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

		final boolean dark = getBackground().equals(Color.DARK_GRAY);

		g2.setColor(dark ? Color.LIGHT_GRAY : Color.DARK_GRAY);
		final int xAxisY = height - BOTTOM_MARGIN;
		final int graphWindowHeight = height - TOP_MARGIN - BOTTOM_MARGIN;
		final int graphWindowWidth = width - LEFT_MARGIN - RIGHT_MARGIN;
		g2.drawRect(LEFT_MARGIN, TOP_MARGIN, graphWindowWidth, graphWindowHeight);
		g2.setStroke(thin);

		// draw x axis

		g2.setColor(dark ? Color.WHITE : Color.BLACK);
		g2.setFont(new Font("Arial", Font.BOLD, 12));
		final String xLabel = "Generation";
		g2.drawString(xLabel, width / 2 - g2.getFontMetrics().stringWidth(xLabel) / 2, xAxisY + 30);

		// draw y axis

		String tickmarkLabel;
		final int x0 = LEFT_MARGIN + 1;
		final int ny = 10;
		g2.setFont(new Font("Arial", Font.PLAIN, 10));
		double yTick;
		for (int i = 0; i <= ny; i++) {
			yTick = height - BOTTOM_MARGIN - (double) (i * graphWindowHeight) / (double) ny;
			g2.drawLine(x0, (int) yTick, x0 - 5, (int) yTick);
			if (i == 0 || i == ny) {
				tickmarkLabel = EnergyPanel.TWO_DECIMALS.format(minFitness + i * (maxFitness - minFitness) / ny);
				g2.drawString(tickmarkLabel, x0 - 10 - g2.getFontMetrics().stringWidth(tickmarkLabel), (int) (yTick + 4));
			}
		}
		final String yLabel = "Fitness";
		g2.setFont(new Font("Arial", Font.BOLD, 12));
		final int yLabelX = x0 - 30;
		final int yLabelY = height / 2 + g2.getFontMetrics().stringWidth(yLabel) / 2 - 8;
		g2.rotate(-Math.PI * 0.5, yLabelX, yLabelY);
		g2.drawString(yLabel, yLabelX, yLabelY);
		g2.rotate(Math.PI * 0.5, yLabelX, yLabelY);

		g2.setFont(new Font("Arial", Font.PLAIN, 10));
		final double dx = graphWindowWidth / (length - 1.0);
		double xTick;
		int tickmarkLength;
		Path2D.Double path = null;
		for (int i = 0; i < individuals.length; i++) {
			if (individuals[i] == null || Double.isNaN(individuals[i].getFitness())) {
				continue;
			}
			xTick = x0 + i * dx;
			if (Util.isEqual(minFitness, maxFitness, 0.000001)) {
				yTick = xAxisY - graphWindowHeight * 0.5;
			} else {
				yTick = xAxisY - (individuals[i].getFitness() - minFitness) / (maxFitness - minFitness) * graphWindowHeight;
			}
			if (path == null) {
				path = new Path2D.Double();
				path.moveTo(xTick, yTick);
			} else {
				path.lineTo(xTick, yTick);
			}
			g2.drawLine((int) xTick, xAxisY + 2, (int) xTick, xAxisY);
			tickmarkLabel = i + "";
			tickmarkLength = g2.getFontMetrics().stringWidth(tickmarkLabel);
			g2.drawString(tickmarkLabel, (int) (xTick - tickmarkLength * 0.5), xAxisY + 16);
		}
		if (path != null) {
			g2.setColor(dark ? Color.WHITE : Color.BLACK);
			g2.setStroke(dashed);
			g2.draw(path);
		}
		g2.setStroke(thin);
		for (int i = 0; i < individuals.length; i++) {
			if (individuals[i] == null || Double.isNaN(individuals[i].getFitness())) {
				continue;
			}
			xTick = x0 + i * dx;
			if (Util.isEqual(minFitness, maxFitness, 0.000001)) {
				yTick = xAxisY - graphWindowHeight * 0.5;
			} else {
				yTick = xAxisY - (individuals[i].getFitness() - minFitness) / (maxFitness - minFitness) * graphWindowHeight;
			}
			final Ellipse2D circle = new Ellipse2D.Double(xTick - 4, yTick - 4, 8, 8);
			g2.setColor(dark ? Color.GRAY : Color.WHITE);
			g2.fill(circle);
			g2.setColor(dark ? Color.LIGHT_GRAY : Color.GRAY);
			g2.draw(circle);
		}

	}

}
