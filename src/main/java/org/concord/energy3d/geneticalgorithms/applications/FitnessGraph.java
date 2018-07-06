package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.util.ClipImage;

/**
 * @author Charles Xie
 *
 */
class FitnessGraph extends JComponent {

	private static final long serialVersionUID = 1L;
	private final static int LEFT_MARGIN = 60;
	private final static int RIGHT_MARGIN = 20;
	private final static int TOP_MARGIN = 20;
	private final static int BOTTOM_MARGIN = 40;

	private final BasicStroke dashed = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 4f }, 0.0f);
	private final BasicStroke thin = new BasicStroke(1);
	private final Individual[] individuals;
	private double maxFitness = -Double.MAX_VALUE;
	private double minFitness = Double.MAX_VALUE;
	private int length;

	public FitnessGraph(final Individual[] individuals) {

		super();
		setPreferredSize(new Dimension(600, 400));
		setBackground(Color.DARK_GRAY);

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
			yTick = xAxisY - (individuals[i].getFitness() - minFitness) / (maxFitness - minFitness) * graphWindowHeight;
			final Ellipse2D circle = new Ellipse2D.Double(xTick - 4, yTick - 4, 8, 8);
			if (path == null) {
				path = new Path2D.Double();
				path.moveTo(xTick, yTick);
			} else {
				path.lineTo(xTick, yTick);
			}

			g2.setColor(Color.GRAY);
			g2.fill(circle);
			g2.setColor(Color.LIGHT_GRAY);
			g2.draw(circle);
			g2.drawLine((int) xTick, xAxisY + 2, (int) xTick, xAxisY);
			tickmarkLabel = i + "";
			tickmarkLength = g2.getFontMetrics().stringWidth(tickmarkLabel);
			g2.drawString(tickmarkLabel, (int) (xTick - tickmarkLength * 0.5), xAxisY + 16);
		}
		if (path != null) {
			g2.setColor(Color.WHITE);
			g2.setStroke(dashed);
			g2.draw(path);
		}

	}

	public void display(final String title) {

		final JDialog dialog = new JDialog(MainFrame.getInstance(), title, true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		final JPanel contentPane = new JPanel(new BorderLayout());
		dialog.setContentPane(contentPane);

		final JMenuBar menuBar = new JMenuBar();
		dialog.setJMenuBar(menuBar);

		final JMenu menu = new JMenu("Export");
		menuBar.add(menu);

		final JMenuItem mi = new JMenuItem("Copy Image");
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				new ClipImage().copyImageToClipboard(FitnessGraph.this);
			}
		});
		menu.add(mi);

		final JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		contentPane.add(panel, BorderLayout.CENTER);

		panel.add(this, BorderLayout.CENTER);

		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		final JButton button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				dialog.dispose();
			}
		});
		buttonPanel.add(button);

		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				dialog.dispose();
			}
		});

		dialog.pack();
		dialog.setLocationRelativeTo(MainFrame.getInstance());
		dialog.setVisible(true);

	}

}
