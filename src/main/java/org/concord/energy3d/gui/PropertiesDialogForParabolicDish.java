package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;

import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.util.SpringUtilities;

/**
 * @author Charles Xie
 *
 */
class PropertiesDialogForParabolicDish extends PropertiesDialogFactory {

	static JDialog getDialog(final ParabolicDish trough) {

		final JDialog dialog = new JDialog(MainFrame.getInstance(), "Parabolic Dish", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		final String info = trough.toString().substring(0, trough.toString().indexOf(')') + 1);
		dialog.setTitle("Properties - " + info);

		dialog.getContentPane().setLayout(new BorderLayout());
		final JPanel panel = new JPanel(new SpringLayout());
		panel.setBorder(new EmptyBorder(8, 8, 8, 8));
		final JScrollPane scroller = new JScrollPane(panel);
		scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setPreferredSize(new Dimension(320, 240));
		dialog.getContentPane().add(scroller, BorderLayout.CENTER);

		final double focalLength = trough.getFocalLength();
		final double rimRadius = trough.getRimRadius();

		int i = 0;

		panel.add(new JLabel("Focal Length: "));
		final JTextField focalLengthField = new JTextField(PopupMenuFactory.threeDecimalsFormat.format(focalLength) + " m");
		focalLengthField.setEditable(false);
		panel.add(focalLengthField);
		i++;

		panel.add(new JLabel("Rim Radius: "));
		final JTextField rimRadiusField = new JTextField(PopupMenuFactory.threeDecimalsFormat.format(rimRadius) + " m");
		rimRadiusField.setEditable(false);
		panel.add(rimRadiusField);
		i++;

		panel.add(new JLabel("Parabola Height: "));
		final JTextField parabolaHeightField = new JTextField(PopupMenuFactory.threeDecimalsFormat.format(rimRadius * rimRadius / (4 * focalLength)) + " m");
		parabolaHeightField.setEditable(false);
		panel.add(parabolaHeightField);
		i++;

		// http://www.powerfromthesun.net/Book/chapter08/chapter08.html
		double r = rimRadius / (2 * focalLength);
		r = r * r + 1;
		final double s = 8.0 * Math.PI / 3.0 * focalLength * focalLength * (r * Math.sqrt(r) - 1);
		panel.add(new JLabel("Total Surface Area: "));
		final JTextField totalSurfaceAreaField = new JTextField(PopupMenuFactory.threeDecimalsFormat.format(s) + " m\u00B2");
		totalSurfaceAreaField.setEditable(false);
		panel.add(totalSurfaceAreaField);
		i++;

		panel.add(new JLabel("Total Aperture Area: "));
		final JTextField totalApertureAreaField = new JTextField(PopupMenuFactory.threeDecimalsFormat.format(Math.PI * rimRadius * rimRadius) + " m\u00B2");
		totalApertureAreaField.setEditable(false);
		panel.add(totalApertureAreaField);
		i++;

		panel.add(new JLabel("Mirror Reflectance: "));
		final JTextField reflectanceField = new JTextField(PopupMenuFactory.threeDecimalsFormat.format(trough.getReflectance() * 100) + "%");
		reflectanceField.setEditable(false);
		panel.add(reflectanceField);
		i++;

		panel.add(new JLabel("Receiver Absorptance: "));
		final JTextField absorptanceField = new JTextField(PopupMenuFactory.threeDecimalsFormat.format(trough.getAbsorptance() * 100) + "%");
		absorptanceField.setEditable(false);
		panel.add(absorptanceField);
		i++;

		panel.add(new JLabel("Optical Efficiency: "));
		final JTextField opticalEfficiencyField = new JTextField(PopupMenuFactory.threeDecimalsFormat.format(trough.getOpticalEfficiency() * 100) + "%");
		opticalEfficiencyField.setEditable(false);
		panel.add(opticalEfficiencyField);
		i++;

		panel.add(new JLabel("Thermal Efficiency: "));
		final JTextField thermalEfficiencyField = new JTextField(PopupMenuFactory.threeDecimalsFormat.format(trough.getThermalEfficiency() * 100) + "%");
		thermalEfficiencyField.setEditable(false);
		panel.add(thermalEfficiencyField);
		i++;

		SpringUtilities.makeCompactGrid(panel, i, 2, 4, 4, 4, 4);

		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		final JButton button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				dialog.dispose();
			}
		});
		buttonPanel.add(button);

		dialog.pack();

		return dialog;

	}

}
