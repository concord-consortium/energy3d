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
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.util.ClipImage;

/**
 * @author Charles Xie
 *
 */
class FitnessEvolutionGraph extends JComponent {

	private static final long serialVersionUID = 1L;
	private final static int LEFT_MARGIN = 60;
	private final static int RIGHT_MARGIN = 20;
	private final static int TOP_MARGIN = 20;
	private final static int BOTTOM_MARGIN = 40;

	// private final BasicStroke dashed = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 8f }, 0.0f);
	private final BasicStroke thin = new BasicStroke(1);
	private final Individual[] records;
	private double maxFitness = -Double.MAX_VALUE;
	private double minFitness = Double.MAX_VALUE;

	public FitnessEvolutionGraph(final Individual[] records) {

		super();
		setPreferredSize(new Dimension(800, 500));
		setBackground(Color.DARK_GRAY);

		this.records = records;
		for (final Individual i : records) {
			if (i != null) {
				final double f = i.getFitness();
				if (f > maxFitness) {
					maxFitness = f;
				}
				if (f < minFitness) {
					minFitness = f;
				}
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

		String tickmark;
		final int x0 = LEFT_MARGIN + 1;
		final int ny = 10;
		g2.setFont(new Font("Arial", Font.PLAIN, 10));
		double yTick;
		for (int i = 0; i < ny; i++) {
			yTick = height - BOTTOM_MARGIN - (double) (i * graphWindowHeight) / (double) ny;
			g2.drawLine(x0, (int) yTick, x0 - 5, (int) yTick);
			tickmark = "" + i;
			g2.drawString(tickmark, x0 - 10 - g2.getFontMetrics().stringWidth(tickmark), (int) (yTick + 4));
		}
		final String yLabel = "Fitness";
		g2.setFont(new Font("Arial", Font.BOLD, 12));
		final int yLabelX = x0 - 30;
		final int yLabelY = height / 2 + g2.getFontMetrics().stringWidth(yLabel) / 2 - 8;
		g2.rotate(-Math.PI * 0.5, yLabelX, yLabelY);
		g2.drawString(yLabel, yLabelX, yLabelY);
		g2.rotate(Math.PI * 0.5, yLabelX, yLabelY);

		g2.setFont(new Font("Arial", Font.PLAIN, 10));
		final double dx = graphWindowWidth / records.length;
		double xTick;
		int tickmarkLength;
		Path2D.Double path = null;
		for (int i = 0; i < records.length; i++) {
			if (records[i] == null) {
				break;
			}
			xTick = x0 + i * dx;
			final double f = (records[i].getFitness() - minFitness) / (maxFitness - minFitness);
			final Shape s = new Rectangle2D.Double(xTick, xAxisY - f * graphWindowHeight, 8, 8);
			if (path == null) {
				path = new Path2D.Double();
				path.moveTo(xTick, xAxisY - f * graphWindowHeight);
			} else {
				path.lineTo(xTick, xAxisY - f * graphWindowHeight);
			}

			g2.setColor(Color.GRAY);
			g2.fill(s);
			g2.setColor(Color.LIGHT_GRAY);
			g2.draw(s);
			g2.drawLine((int) xTick, xAxisY + 2, (int) xTick, xAxisY);
			if (i == records.length - 1) {
				tickmark = (i + 1) + "";
				tickmarkLength = g2.getFontMetrics().stringWidth(tickmark);
				g2.drawString(tickmark, (int) (xTick + dx - tickmarkLength * 0.5), xAxisY + 16);
			}
		}
		if (path != null) {
			g2.draw(path);
		}

	}

	public void showGui() {

		final JDialog dialog = new JDialog(MainFrame.getInstance(), "Fitness Evolution", true);
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
				new ClipImage().copyImageToClipboard(FitnessEvolutionGraph.this);
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
