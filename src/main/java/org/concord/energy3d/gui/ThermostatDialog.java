package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
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

	private RangeSlider[] sliders;

	public ThermostatDialog() {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Thermostat Settings");

		sliders = new RangeSlider[7];
		for (int i = 0; i < sliders.length; i++) {
			sliders[i] = new RangeSlider();
			sliders[i].setPreferredSize(new Dimension(300, 30));
			sliders[i].setBorder(BorderFactory.createEtchedBorder());
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
			// labels[i].setBorder(BorderFactory.createEtchedBorder());
			labels[i].setMinimumSize(new Dimension(60, 30));
			labels[i].setAlignmentX(CENTER_ALIGNMENT);
		}

		getContentPane().setLayout(new BorderLayout());
		final JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		getContentPane().add(panel, BorderLayout.CENTER);

		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		// Turn on automatically adding gaps between components
		layout.setAutoCreateGaps(true);

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
		hGroup.addGroup(pg);
		pg = layout.createParallelGroup();
		for (int i = 0; i < sliders.length; i++) {
			pg = pg.addComponent(sliders[i]);
		}
		hGroup.addGroup(pg);
		layout.setHorizontalGroup(hGroup);

		// Create a sequential group for the vertical axis.
		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();

		// The sequential group contains two parallel groups that align the contents along the baseline. The first parallel group contains the first label and text field, and the
		// second parallel group contains the second label and text field. By using a sequential group the labels and text fields are positioned vertically after one another.
		for (int i = 0; i < labels.length; i++) {
			vGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(labels[i]).addComponent(sliders[i]));
		}
		layout.setVerticalGroup(vGroup);

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				ThermostatDialog.this.dispose();
			}
		});
		okButton.setActionCommand("OK");
		buttonPanel.add(okButton);
		getRootPane().setDefaultButton(okButton);

		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				ThermostatDialog.this.dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPanel.add(cancelButton);

		pack();
		setLocationRelativeTo(MainFrame.getInstance());

	}

}