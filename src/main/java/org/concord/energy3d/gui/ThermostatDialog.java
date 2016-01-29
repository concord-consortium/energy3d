package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormatSymbols;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.Thermostat;

import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;

/**
 * This implements a GUI for changing hourly temperature according to the week of the month and the month of the year.
 * 
 * @author Charles Xie
 * 
 */
class ThermostatDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private ThermostatView[] temperatureButtons;
	private int numberOfSteps = 25;
	private Color bgColor = new Color(225, 225, 225);

	public ThermostatDialog(Foundation foundation) {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		Calendar calendar = Heliodon.getInstance().getCalender();
		int month = calendar.get(Calendar.MONTH);
		setTitle("Thermostat Schedule: " + new DateFormatSymbols().getMonths()[month]);
		getContentPane().setLayout(new BorderLayout());

		temperatureButtons = new ThermostatView[7];
		Thermostat t = foundation.getThermostat();
		for (int i = 0; i < temperatureButtons.length; i++) {
			temperatureButtons[i] = new ThermostatView(foundation, month, i);
			temperatureButtons[i].setBackground(bgColor);
			temperatureButtons[i].setPreferredSize(new Dimension(720, 30));
			temperatureButtons[i].setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
			for (int j = 0; j < numberOfSteps; j++)
				temperatureButtons[i].setButton((float) (j + 1) / numberOfSteps, t.getTemperature(month, i, j));
		}

		final JLabel hourLabel = new JLabel(new Icon() {
			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				int w = getIconWidth();
				int h = getIconHeight();
				g.setColor(Color.WHITE);
				g.fillOval(x, y, w, h);
				g.setColor(Color.GRAY);
				g.drawOval(x, y, w, h);
				g.drawLine(x + w / 2, y + h / 2, x + w / 2, y + 2);
				g.drawLine(x + w / 2, y + h / 2, x + w - 2, y + h / 2);
			}

			@Override
			public int getIconWidth() {
				return 16;
			}

			@Override
			public int getIconHeight() {
				return 16;
			}
		});
		hourLabel.setHorizontalAlignment(JLabel.LEFT);
		hourLabel.setVerticalAlignment(JLabel.CENTER);
		hourLabel.setMinimumSize(new Dimension(40, 20));
		HourPanel hourPanel = new HourPanel(temperatureButtons[0]);
		hourPanel.setPreferredSize(new Dimension(temperatureButtons[0].getPreferredSize().width, 20));
		hourPanel.setBackground(bgColor);
		hourPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

		JLabel[] labels = new JLabel[7];
		labels[0] = new JLabel("Sunday");
		labels[1] = new JLabel("Monday");
		labels[2] = new JLabel("Tuesday");
		labels[3] = new JLabel("Wednesday");
		labels[4] = new JLabel("Thursday");
		labels[5] = new JLabel("Friday");
		labels[6] = new JLabel("Saturday");
		for (int i = 0; i < labels.length; i++) {
			labels[i].setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
			labels[i].setMinimumSize(new Dimension(60, 30));
			labels[i].setAlignmentX(CENTER_ALIGNMENT);
		}

		final JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);

		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		// layout.setAutoCreateGaps(true); // Turn on automatically adding gaps between components

		// Turn on automatically creating gaps between components that touch the edge of the container and the container.
		layout.setAutoCreateContainerGaps(true);

		// Create a sequential group for the horizontal axis.
		GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();

		// The sequential group in turn contains two parallel groups. One parallel group contains the labels, the other the sliders.
		// Putting the labels in a parallel group along the horizontal axis positions them at the same x location. Variable indentation is used to reinforce the level of grouping.
		ParallelGroup pg = layout.createParallelGroup();
		for (int i = 0; i < labels.length; i++) {
			pg = pg.addComponent(labels[i]);
		}
		pg = pg.addComponent(hourLabel);
		hGroup.addGroup(pg);
		pg = layout.createParallelGroup();
		for (int i = 0; i < temperatureButtons.length; i++) {
			pg = pg.addComponent(temperatureButtons[i]);
		}
		pg = pg.addComponent(hourPanel);
		hGroup.addGroup(pg);
		layout.setHorizontalGroup(hGroup);

		// Create a sequential group for the vertical axis.
		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();

		// The sequential group contains two parallel groups that align the contents along the baseline. The first parallel group contains the first label and text field, and the
		// second parallel group contains the second label and text field. By using a sequential group the labels and text fields are positioned vertically after one another.
		for (int i = 0; i < labels.length; i++) {
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(labels[i]).addComponent(temperatureButtons[i]));
		}
		vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(hourLabel).addComponent(hourPanel));
		layout.setVerticalGroup(vGroup);

		JPanel buttonPanel = new JPanel(new BorderLayout());
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		final JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(actionPanel, BorderLayout.WEST);

		actionPanel.add(new JLabel("  Drag a number up or down to raise or lower temperature"));

		final JPanel okPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(okPanel, BorderLayout.EAST);

		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				ThermostatDialog.this.dispose();
			}
		});
		okButton.setActionCommand("OK");
		okPanel.add(okButton);
		getRootPane().setDefaultButton(okButton);

		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				ThermostatDialog.this.dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		okPanel.add(cancelButton);

		pack();
		setLocationRelativeTo(MainFrame.getInstance());

	}

	@SuppressWarnings("serial")
	class HourPanel extends JPanel {

		private ThermostatView thermostatView;

		public HourPanel(ThermostatView thermostatView) {
			this.thermostatView = thermostatView;
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
			g2.setFont(new Font("Arial", Font.PLAIN, 10));

			float delta = (width - 0.8f * thermostatView.getHeight()) / 25.0f;
			g2.setColor(Color.DARK_GRAY);
			String hourString;
			FontMetrics fm = g2.getFontMetrics();
			for (int i = 0; i < 25; i++) {
				hourString = i == 24 ? "All" : i + "";
				g2.drawString(hourString, (int) (delta * (i + 1) - fm.stringWidth(hourString) / 2), height - 5);
			}

		}

	}

}