package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.gui.EnergyPanel;

/**
 * @author Charles Xie
 *
 */
class GeneGraph extends JComponent {

	private static final long serialVersionUID = 1L;
	private final static int LEFT_MARGIN = 60;
	private final static int RIGHT_MARGIN = 20;
	private final static int TOP_MARGIN = 20;
	private final static int BOTTOM_MARGIN = 40;

	// private final BasicStroke dashed = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 8f }, 0.0f);
	private final BasicStroke thin = new BasicStroke(1);
	private final String geneName;
	private final int nbin = 100; // number of bins
	private int ymax = 0; // maximum count per bin
	private final int[] count;

	GeneGraph(final Individual[] individuals, final int geneIndex, final String geneName) {

		super();
		setBackground(Color.DARK_GRAY);
		this.geneName = geneName;

		final double delta = 1.0 / nbin;
		count = new int[nbin];
		for (final Individual i : individuals) {
			if (i != null) {
				count[(int) (i.getGene(geneIndex) / delta)]++;
			}
		}
		for (int i = 0; i < count.length; i++) {
			if (count[i] > ymax) {
				ymax = count[i];
			}
		}

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

		g2.setColor(Color.LIGHT_GRAY);
		final int xAxisY = height - BOTTOM_MARGIN;
		final int graphWindowHeight = height - TOP_MARGIN - BOTTOM_MARGIN;
		final int graphWindowWidth = width - LEFT_MARGIN - RIGHT_MARGIN;
		g2.drawRect(LEFT_MARGIN, TOP_MARGIN, graphWindowWidth, graphWindowHeight);
		g2.setStroke(thin);

		// draw x axis

		g2.setColor(Color.WHITE);
		g2.setFont(new Font("Arial", Font.BOLD, 12));
		final String xLabel = geneName + " (normalized)";
		g2.drawString(xLabel, width / 2 - g2.getFontMetrics().stringWidth(xLabel) / 2, xAxisY + 30);

		// draw y axis

		String tickmarkLabel;
		final int x0 = LEFT_MARGIN + 1;
		final int ny = ymax + 1;
		final double dy = (double) graphWindowHeight / (double) ny;
		g2.setFont(new Font("Arial", Font.PLAIN, 10));
		double yTick;
		if (ny < 20) {
			for (int i = 0; i < ny; i++) {
				yTick = height - BOTTOM_MARGIN - i * dy;
				g2.drawLine(x0, (int) yTick, x0 - 5, (int) yTick);
				tickmarkLabel = "" + i;
				g2.drawString(tickmarkLabel, x0 - 10 - g2.getFontMetrics().stringWidth(tickmarkLabel), (int) (yTick + 4));
			}
		} else {
			for (int i = 0; i < ny; i++) {
				if (i % 5 == 0) {
					yTick = height - BOTTOM_MARGIN - i * dy;
					g2.drawLine(x0, (int) yTick, x0 - 5, (int) yTick);
					tickmarkLabel = "" + i;
					g2.drawString(tickmarkLabel, x0 - 10 - g2.getFontMetrics().stringWidth(tickmarkLabel), (int) (yTick + 4));
				}
			}
		}
		final String yLabel = "Individual Count";
		g2.setFont(new Font("Arial", Font.BOLD, 12));
		final int yLabelX = x0 - 30;
		final int yLabelY = height / 2 + g2.getFontMetrics().stringWidth(yLabel) / 2 - 8;
		g2.rotate(-Math.PI * 0.5, yLabelX, yLabelY);
		g2.drawString(yLabel, yLabelX, yLabelY);
		g2.rotate(Math.PI * 0.5, yLabelX, yLabelY);

		if (count != null) { // draw Manhattan plot
			final Area area = new Area();
			g2.setFont(new Font("Arial", Font.PLAIN, 10));
			final double dx = (double) graphWindowWidth / (double) count.length;
			double xTick;
			int tickmarkLabelLength;
			for (int i = 0; i < count.length; i++) {
				xTick = x0 + i * dx;
				area.add(new Area(new Rectangle2D.Double(xTick - dx / 2, xAxisY - count[i] * dy, dx, count[i] * dy)));
				if (i % 2 == 0) {
					g2.drawLine((int) xTick, xAxisY + 2, (int) xTick, xAxisY);
					if (i % 20 == 0) {
						tickmarkLabel = EnergyPanel.ONE_DECIMAL.format((double) i / (double) count.length);
						tickmarkLabelLength = g2.getFontMetrics().stringWidth(tickmarkLabel);
						g2.drawString(tickmarkLabel, (int) (xTick - tickmarkLabelLength * 0.5), xAxisY + 16);
					}
				}
			}
			tickmarkLabel = "1.0";
			tickmarkLabelLength = g2.getFontMetrics().stringWidth(tickmarkLabel);
			g2.drawString(tickmarkLabel, (int) (x0 + count.length * dx - tickmarkLabelLength * 0.5), xAxisY + 16);
			g2.setColor(Color.GRAY);
			g2.fill(area);
			g2.setColor(Color.LIGHT_GRAY);
			g2.draw(area);
		}

	}

}
