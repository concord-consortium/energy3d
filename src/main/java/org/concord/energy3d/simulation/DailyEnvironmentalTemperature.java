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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Path2D;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.Util;

/**
 * This graph shows the daily variation of air and ground temperatures for the current day and location.
 * 
 * @author Charles Xie
 * 
 */

public class DailyEnvironmentalTemperature extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int CIRCLE = 0;
	private static final int DIAMOND = 1;
	private static final int SQUARE = 2;
	private static final int TRIANGLEUP = 3;
	private static final int TRIANGLEDOWN = 4;

	private int top = 50, right = 50, bottom = 80, left = 90;
	private double xmin = 0;
	private double xmax = 23;
	private double ymin = 1000;
	private double ymax = -1000;
	private double dx;
	private double dy;
	private double xNow;
	private int symbolSize = 8;
	private int numberOfTicks = 24;
	private String xAxisLabel = "Hour";
	private String yAxisLabel = "Temperature (\u00b0C)";
	private BasicStroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[] { 2f }, 0.0f);
	private BasicStroke thin = new BasicStroke(1);
	private BasicStroke thick = new BasicStroke(2);
	private String city;
	private Calendar today;

	private double lowestAirTemperature;
	private double highestAirTemperature;

	private double[] depth;
	private double[][] groundTemperature;

	private int[] symbol;
	private BasicStroke[] stroke;
	private Map<double[], Boolean> hideData;

	public DailyEnvironmentalTemperature() {

		super();
		setPreferredSize(new Dimension(600, 400));
		setBackground(Color.WHITE);

		hideData = new HashMap<double[], Boolean>();

		depth = new double[] { 0, 0.5, 1, 2, 6 };
		symbol = new int[] { -1, CIRCLE, DIAMOND, SQUARE, -1 };
		stroke = new BasicStroke[] { thick, thick, thick, thick, dashed };
		int m = depth.length;
		groundTemperature = new double[m][24];

		for (int i = 0; i < m; i++)
			hideData.put(groundTemperature[i], false);

		city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
		today = Heliodon.getInstance().getCalender();
		xNow = today.get(Calendar.HOUR_OF_DAY) + (double) today.get(Calendar.MINUTE) / 60.0;
		int day = today.get(Calendar.DAY_OF_YEAR);
		double[] r = Weather.computeOutsideTemperature(today, city);
		lowestAirTemperature = r[0];
		highestAirTemperature = r[1];
		double amp = 0.5 * (highestAirTemperature - lowestAirTemperature);
		for (int h = 0; h < 24; h++) {
			for (int i = 0; i < m; i++) {
				groundTemperature[i][h] = i == 0 ? Weather.getInstance().getOutsideTemperatureAtMinute(highestAirTemperature, lowestAirTemperature, h * 60) : Scene.getInstance().getGround().getTemperatureMinuteOfDay(day, h * 60, amp, depth[i]);
			}
		}

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
		g2.drawRect(left / 2, top / 2, width - (left + right) / 2, height - (top + bottom) / 2);

		g2.setColor(Color.BLACK);
		g2.setFont(new Font("Arial", Font.PLAIN, 8));
		float tickWidth = (float) (width - left - right) / (float) (numberOfTicks - 1);
		float xTick;
		for (int i = 0; i < numberOfTicks; i++) {
			String s = i + "";
			int sWidth = g2.getFontMetrics().stringWidth(s);
			xTick = left + tickWidth * i;
			g2.drawString(s, xTick - sWidth / 2, height - bottom / 2 + 16);
			g2.drawLine((int) xTick, height - bottom / 2, (int) xTick, height - bottom / 2 - 4);
		}
		g2.setFont(new Font("Arial", Font.PLAIN, 10));
		int xAxisLabelWidth = g2.getFontMetrics().stringWidth(xAxisLabel);
		int yAxisLabelWidth = g2.getFontMetrics().stringWidth(yAxisLabel);
		g2.drawString(xAxisLabel, (width - xAxisLabelWidth) / 2, height - 8);
		g2.rotate(-Math.PI / 2, 16, (height + yAxisLabelWidth) / 2);
		g2.drawString(yAxisLabel, 16, (height + yAxisLabelWidth) / 2);
		g2.rotate(Math.PI / 2, 16, (height + yAxisLabelWidth) / 2);

		calculateBounds();
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

		for (int i = 0; i < depth.length; i++) {
			if (!hideData.get(groundTemperature[i])) {
				drawCurve(g2, groundTemperature[i], Color.GRAY, symbol[i], stroke[i]);
			}
		}

		drawLegends(g2);

		g2.setFont(new Font("Arial", Font.BOLD, 14));
		FontMetrics fm = g2.getFontMetrics();
		String cityAndDate = city + " - " + (today.get(Calendar.MONTH) + 1) + "/" + today.get(Calendar.DAY_OF_MONTH);
		g2.drawString(cityAndDate, (width - fm.stringWidth(cityAndDate)) / 2, 20);

		g2.setColor(Color.LIGHT_GRAY);
		g2.setStroke(thin);
		int xTodayLine = (int) Math.round(left + dx * xNow);
		g2.drawLine(xTodayLine, top / 2, xTodayLine, height - bottom / 2);

	}

	void drawLegends(Graphics2D g2) {

		g2.setFont(new Font("Arial", Font.PLAIN, 10));
		g2.setStroke(thin);
		int x0 = left / 2 + 20;
		int y0 = top - 10;

		for (int i = 0; i < depth.length - 1; i++) {
			if (!hideData.get(groundTemperature[i])) {
				g2.setColor(Color.GRAY);
				g2.setStroke(stroke[i]);
				g2.drawLine(x0 - 7, y0 + 4, x0 + 13, y0 + 4);
				g2.setStroke(thin);
				drawSymbol(g2, symbol[i], x0, y0);
				g2.drawString(i == 0 ? "Air" : "Ground (" + depth[i] + "m deep)", x0 + 20, y0 + 8);
				y0 += 14;
			}
		}

		if (!hideData.get(groundTemperature[depth.length - 1])) {
			g2.setColor(Color.GRAY);
			g2.setStroke(dashed);
			g2.drawLine(x0 - 7, y0 + 4, x0 + 13, y0 + 4);
			g2.setColor(Color.BLACK);
			g2.drawString("Ground (" + depth[depth.length - 1] + "m deep)", x0 + 20, y0 + 8);
		}

	}

	private void drawSymbol(Graphics g2, int sym, int x0, int y0) {
		switch (sym) {
		case CIRCLE:
			Graph.drawCircle(g2, x0, y0, 6, Color.WHITE);
			break;
		case DIAMOND:
			Graph.drawDiamond(g2, x0 + 3, y0 + 4, 4, Color.WHITE);
			break;
		case SQUARE:
			Graph.drawSquare(g2, x0, y0, 6, Color.WHITE);
			break;
		case TRIANGLEUP:
			Graph.drawTriangleUp(g2, x0, y0, 6, Color.WHITE);
			break;
		case TRIANGLEDOWN:
			Graph.drawTriangleDown(g2, x0, y0, 6, Color.WHITE);
			break;
		}
	}

	private void drawCurve(Graphics2D g2, double[] data, Color color, int sym, BasicStroke stk) {

		double dataX, dataY;
		Path2D.Float path = new Path2D.Float();
		for (int i = 0; i < data.length; i++) {
			dataX = left + dx * i;
			dataY = getHeight() - top - (data[i] - ymin) * dy;
			if (i == 0) {
				path.moveTo(dataX, dataY);
			} else {
				path.lineTo(dataX, dataY);
			}
		}
		g2.setColor(color);
		g2.setStroke(stk);
		g2.draw(path);

		if (sym != -1) {
			g2.setStroke(thin);
			for (int i = 0; i < data.length; i++) {
				dataX = left + dx * i;
				dataY = getHeight() - top - (data[i] - ymin) * dy;
				switch (sym) {
				case CIRCLE:
					Graph.drawCircle(g2, (int) Math.round(dataX - symbolSize / 2 + 1), (int) Math.round(dataY - symbolSize / 2 + 1), symbolSize - 2, Color.WHITE);
					break;
				case SQUARE:
					Graph.drawSquare(g2, (int) Math.round(dataX - symbolSize / 2 + 1), (int) Math.round(dataY - symbolSize / 2 + 1), symbolSize - 2, Color.WHITE);
					break;
				case DIAMOND:
					Graph.drawDiamond(g2, (int) dataX, (int) dataY, symbolSize / 2, Color.WHITE);
					break;
				case TRIANGLEUP:
					Graph.drawTriangleUp(g2, (int) Math.round(dataX - symbolSize / 2 + 1), (int) Math.round(dataY - symbolSize / 2 + 1), symbolSize - 2, Color.WHITE);
					break;
				case TRIANGLEDOWN:
					Graph.drawTriangleDown(g2, (int) Math.round(dataX - symbolSize / 2 + 1), (int) Math.round(dataY - symbolSize / 2 + 1), symbolSize - 2, Color.WHITE);
					break;
				}
			}
		}

	}

	private void drawHorizontalLine(Graphics2D g2, int yValue, String yLabel) {
		g2.setStroke(thin);
		g2.setColor(Color.LIGHT_GRAY);
		g2.drawLine(left / 2, yValue, getWidth() - right / 2, yValue);
		g2.setColor(Color.BLACK);
		int yLabelWidth = g2.getFontMetrics().stringWidth(yLabel);
		g2.drawString(yLabel, left / 2 - 5 - yLabelWidth, yValue + 4);
	}

	private void calculateBounds() {
		if (ymin > lowestAirTemperature)
			ymin = lowestAirTemperature;
		if (ymax < highestAirTemperature)
			ymax = highestAirTemperature;
		double t;
		for (int i = 0; i < groundTemperature.length; i++) {
			for (int j = 0; j < groundTemperature[i].length; j++) {
				t = groundTemperature[i][j];
				if (t < ymin)
					ymin = t;
				if (t > ymax)
					ymax = t;
			}
		}
		dx = (double) (getWidth() - left - right) / (xmax - xmin);
		dy = (double) (getHeight() - top - bottom) / (ymax - ymin);
	}

	public void showDialog() {

		EnergyPanel.getInstance().requestDisableActions(this);

		final JDialog dialog = new JDialog(MainFrame.getInstance(), "Daily Environmental Temperature", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		final JPanel contentPane = new JPanel(new BorderLayout());
		dialog.setContentPane(contentPane);

		final JMenuBar menuBar = new JMenuBar();
		dialog.setJMenuBar(menuBar);

		final JCheckBoxMenuItem cbmiAirTemperature = new JCheckBoxMenuItem("Air Temperature");
		final JCheckBoxMenuItem[] cbmiGroundTemperature = new JCheckBoxMenuItem[depth.length];
		for (int i = 0; i < depth.length; i++) {
			cbmiGroundTemperature[i] = new JCheckBoxMenuItem("Ground Temperature (" + depth[i] + "m)");
			Util.selectSilently(cbmiGroundTemperature[i], !hideData.get(groundTemperature[i]));
		}

		final JMenu menuView = new JMenu("View");
		menuBar.add(menuView);
		menuView.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				for (int i = 0; i < depth.length; i++)
					Util.selectSilently(cbmiGroundTemperature[i], !hideData.get(groundTemperature[i]));
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});

		menuView.add(cbmiAirTemperature);

		for (int i = 0; i < depth.length; i++) {
			final int i2 = i;
			cbmiGroundTemperature[i].addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					JCheckBoxMenuItem source = (JCheckBoxMenuItem) e.getSource();
					hideData.put(groundTemperature[i2], !source.isSelected());
					DailyEnvironmentalTemperature.this.repaint();
				}
			});
			menuView.add(cbmiGroundTemperature[i]);
		}

		final JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		contentPane.add(panel, BorderLayout.CENTER);

		panel.add(this, BorderLayout.CENTER);

		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		JButton button = new JButton("Close");
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

		EnergyPanel.getInstance().requestDisableActions(null);

	}

}
