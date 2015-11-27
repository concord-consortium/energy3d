package org.concord.energy3d.gui;

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
import java.text.DateFormatSymbols;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.Thermostat;

import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;

/**
 * This implements a GUI that looks like the Nest Thermostat app for changing temperature.
 * 
 * @author Charles Xie
 * 
 */
class ThermostatDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private ThermostatView[] sliders;

	public ThermostatDialog() {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		Calendar calendar = Heliodon.getInstance().getCalender();
		int month = calendar.get(Calendar.MONTH);
		setTitle("Thermostat Schedule: " + new DateFormatSymbols().getMonths()[month]);
		getContentPane().setLayout(new BorderLayout());

		Color bgColor = new Color(225, 225, 225);

		JLabel hourLabel = new JLabel();
		HourPanel hourPanel = new HourPanel();
		hourPanel.setPreferredSize(new Dimension(720, 20));
		hourPanel.setBackground(bgColor);
		hourPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));

		sliders = new ThermostatView[7];
		int numberOfSteps = 25;
		Thermostat t = Scene.getInstance().getThermostat();
		for (int i = 0; i < sliders.length; i++) {
			sliders[i] = new ThermostatView(month, i);
			sliders[i].setBackground(bgColor);
			sliders[i].setPreferredSize(new Dimension(720, 30));
			sliders[i].setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
			for (int j = 0; j < 24; j++)
				sliders[i].setHandle((float) (j + 1) / numberOfSteps, t.getTemperature(month, i, j));
		}

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
		for (int i = 0; i < sliders.length; i++) {
			pg = pg.addComponent(sliders[i]);
		}
		pg = pg.addComponent(hourPanel);
		hGroup.addGroup(pg);
		layout.setHorizontalGroup(hGroup);

		// Create a sequential group for the vertical axis.
		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();

		// The sequential group contains two parallel groups that align the contents along the baseline. The first parallel group contains the first label and text field, and the
		// second parallel group contains the second label and text field. By using a sequential group the labels and text fields are positioned vertically after one another.
		for (int i = 0; i < labels.length; i++) {
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(labels[i]).addComponent(sliders[i]));
		}
		vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(hourLabel).addComponent(hourPanel));
		layout.setVerticalGroup(vGroup);

		JPanel buttonPanel = new JPanel(new BorderLayout());
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		final JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.add(actionPanel, BorderLayout.WEST);

		JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < sliders.length; i++) {
					sliders[i].removeSelectedHour();
				}
			}
		});
		actionPanel.add(removeButton);

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

			float delta = width / 25.0f;
			g2.setColor(Color.DARK_GRAY);
			String hourString;
			FontMetrics fm = g2.getFontMetrics();
			for (int i = 0; i < 24; i++) {
				hourString = i + "";
				g2.drawString(hourString, (int) (delta * (i + 1) - fm.stringWidth(hourString) / 2), height - 5);
			}

		}

	}

}