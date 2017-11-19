package org.concord.energy3d.simulation;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.util.Calendar;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.agents.OperationEvent;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.ClipImage;

/**
 * This graph shows the average monthly sunshine hours of the current location.
 * 
 * @author Charles Xie
 * 
 */

public class MonthlySunshineHours extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final Color COLOR_GOLD = new Color(255, 215, 0);

	private final int top = 25, right = 25, bottom = 45, left = 45;
	private final double xToday;
	private final String xAxisLabel = "Month";
	private final String yAxisLabel = "Sunshine Hours";
	private final BasicStroke thin = new BasicStroke(1);
	private final BasicStroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 2f }, 0.0f);
	private final String city;
	private final int[] sunshineHours;
	private final int max = 500;
	private double dx, dy;

	public MonthlySunshineHours() {
		super();
		setPreferredSize(new Dimension(600, 400));
		setBackground(Color.WHITE);
		city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
		sunshineHours = LocationData.getInstance().getSunshineHours().get(city);
		final Calendar today = (Calendar) Heliodon.getInstance().getCalendar().clone();
		xToday = today.get(Calendar.MONTH) + ((double) (today.get(Calendar.DAY_OF_MONTH) - 1) / (double) today.getActualMaximum(Calendar.DAY_OF_MONTH));
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
		g2.setColor(Color.GRAY);
		final int graphWidth = width - (left + right);
		final int graphHeight = height - (top + bottom);
		g2.drawRect(left, top, graphWidth, graphHeight);
		dx = (double) graphWidth / sunshineHours.length;
		dy = (double) graphHeight / max;

		g2.setColor(Color.BLACK);
		g2.setFont(new Font("Arial", Font.PLAIN, 8));
		float xTick;
		String s;
		for (int i = 0; i < sunshineHours.length; i++) {
			s = AnnualGraph.THREE_LETTER_MONTH[i];
			final int sWidth = g2.getFontMetrics().stringWidth(s);
			xTick = (int) (left + dx * (i + 0.5));
			g2.drawString(s, xTick - sWidth / 2, height - bottom + 16);
			// g2.drawLine((int) xTick, height - bottom, (int) xTick, height - bottom - 4);
		}
		g2.setFont(new Font("Arial", Font.PLAIN, 10));
		final int xAxisLabelWidth = g2.getFontMetrics().stringWidth(xAxisLabel);
		final int yAxisLabelWidth = g2.getFontMetrics().stringWidth(yAxisLabel);
		g2.drawString(xAxisLabel, (width - xAxisLabelWidth) / 2, height - 8);
		g2.rotate(-Math.PI / 2, 16, (height + yAxisLabelWidth) / 2);
		g2.drawString(yAxisLabel, 16, (height + yAxisLabelWidth) / 2);
		g2.rotate(Math.PI / 2, 16, (height + yAxisLabelWidth) / 2);

		// draw bars
		final Rectangle2D.Double rect = new Rectangle2D.Double();
		rect.width = dx * 0.9;
		for (int i = 0; i < sunshineHours.length; i++) {
			rect.x = left + dx * (i + 0.05);
			rect.height = sunshineHours[i] * dy;
			rect.y = height - bottom - rect.height;
			g2.setColor(COLOR_GOLD);
			g2.fill(rect);
			g2.setColor(Color.GRAY);
			g2.draw(rect);
		}

		// draw horizontal lines across the graph window
		double dataY;
		g2.setStroke(thin);
		for (int i = 0; i <= max / 100; i++) {
			dataY = getHeight() - bottom - i * 100 * dy;
			s = Integer.toString(i * 100);
			g2.setColor(Color.LIGHT_GRAY);
			g2.drawLine(left, (int) dataY, getWidth() - right, (int) dataY);
			final int yLabelWidth = g2.getFontMetrics().stringWidth(s);
			g2.setColor(Color.BLACK);
			g2.drawString(s, left - 5 - yLabelWidth, (int) (dataY + 4));
		}

		g2.setColor(Color.BLACK);
		g2.setFont(new Font("Arial", Font.BOLD, 14));
		final FontMetrics fm = g2.getFontMetrics();
		g2.drawString(city, (width - fm.stringWidth(city)) / 2, 20);

		g2.setColor(Color.LIGHT_GRAY);
		g2.setStroke(dashed);
		final int xTodayLine = (int) Math.round(left + dx * xToday);
		g2.drawLine(xTodayLine, top, xTodayLine, height - bottom);

	}

	public void showDialog() {

		final JDialog dialog = new JDialog(MainFrame.getInstance(), "Monthly Sunshine Hours", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		final JPanel contentPane = new JPanel(new BorderLayout());
		dialog.setContentPane(contentPane);

		final JMenuBar menuBar = new JMenuBar();
		dialog.setJMenuBar(menuBar);

		final JMenu menuAction = new JMenu("Action");
		menuBar.add(menuAction);

		final JMenuItem mi = new JMenuItem("Copy Image");
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				new ClipImage().copyImageToClipboard(MonthlySunshineHours.this);
			}
		});
		menuAction.add(mi);

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

		TimeSeriesLogger.getInstance().logAnalysis(this);
		final HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("Location", Scene.getInstance().getCity());
		MainApplication.addEvent(new OperationEvent(Scene.getURL(), System.currentTimeMillis(), getClass().getSimpleName(), attributes));

	}

	// TODO
	public String toJson() {
		final String s = "{}";
		return s;
	}

}
