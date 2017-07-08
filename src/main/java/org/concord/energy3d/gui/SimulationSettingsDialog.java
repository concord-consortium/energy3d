package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.simulation.SolarRadiation;
import org.concord.energy3d.util.SpringUtilities;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 * 
 */
class SimulationSettingsDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final static DecimalFormat FORMAT2 = new DecimalFormat("##");

	public SimulationSettingsDialog() {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Simulation Settings");

		getContentPane().setLayout(new BorderLayout());
		final JPanel panel = new JPanel(new SpringLayout());
		panel.setBorder(new EmptyBorder(15, 15, 15, 15));
		getContentPane().add(panel, BorderLayout.CENTER);

		final Scene s = Scene.getInstance();
		final JTextField mirrorNxTextField = new JTextField(s.getMirrorNx() + "", 6);
		final JTextField mirrorNyTextField = new JTextField(s.getMirrorNy() + "", 6);
		final JTextField rackCellSizeTextField = new JTextField(FORMAT2.format(Scene.getInstance().getRackCellSize()));
		final JLabel rackCellSizeLabelLeft = new JLabel("Rack grid cell: ");
		final JLabel rackCellSizeLabelRight = new JLabel("Meters");
		final JLabel rackModelLabelRight = new JLabel("Fast but inaccurate");
		final JTextField timeStepTextField = new JTextField(FORMAT2.format(Scene.getInstance().getTimeStep()));

		final JComboBox<String> airMassComboBox = new JComboBox<String>(new String[] { "None", "Kasten-Young", "Sphere Model" });
		airMassComboBox.setSelectedIndex(SolarRadiation.getInstance().getAirMassSelection() + 1);

		final JComboBox<String> rackModelComboBox = new JComboBox<String>(new String[] { "Approximate", "Exact" });
		rackModelComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				final boolean approximate = rackModelComboBox.getSelectedIndex() == 0;
				rackCellSizeTextField.setEnabled(approximate);
				rackCellSizeLabelLeft.setEnabled(approximate);
				rackCellSizeLabelRight.setEnabled(approximate);
				rackModelLabelRight.setText(approximate ? "Fast but inaccurate" : "Slow but accurate");
			}
		});
		rackModelComboBox.setSelectedIndex(s.isRackModelExact() ? 1 : 0);

		final ActionListener okListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				int mirrorNx;
				int mirrorNy;
				double rackCellSize;
				int timeStep;
				try {
					mirrorNx = Integer.parseInt(mirrorNxTextField.getText());
					mirrorNy = Integer.parseInt(mirrorNyTextField.getText());
					rackCellSize = Double.parseDouble(rackCellSizeTextField.getText());
					timeStep = (int) Double.parseDouble(timeStepTextField.getText());
				} catch (final NumberFormatException err) {
					err.printStackTrace();
					JOptionPane.showMessageDialog(SimulationSettingsDialog.this, "Invalid input: " + err.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				// power of two check
				if (!Util.isPowerOfTwo(mirrorNx) || !Util.isPowerOfTwo(mirrorNy)) {
					JOptionPane.showMessageDialog(SimulationSettingsDialog.this, "Number of mirror grid cells in x or y direction must be power of two.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				// range check
				if (timeStep < 5 || timeStep > 60) {
					JOptionPane.showMessageDialog(SimulationSettingsDialog.this, "Time step must be in 5-60 seconds.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (rackCellSize < 0.5 || rackCellSize > 50) {
					JOptionPane.showMessageDialog(SimulationSettingsDialog.this, "The grid cell size of a rack must be in 0.5-50 meters.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (mirrorNx < 2 || mirrorNy < 2) {
					JOptionPane.showMessageDialog(SimulationSettingsDialog.this, "Number of mirror grid cells in x or y direction must be at least two.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				s.setMirrorNx(mirrorNx);
				s.setMirrorNy(mirrorNy);
				s.setRackModelExact(rackModelComboBox.getSelectedIndex() == 1);
				s.setRackCellSize(rackCellSize);
				s.setTimeStep(timeStep);
				s.setEdited(true);
				SolarRadiation.getInstance().setAirMassSelection(airMassComboBox.getSelectedIndex() - 1);
				EnergyPanel.getInstance().clearRadiationHeatMap();
				SimulationSettingsDialog.this.dispose();
			}
		};

		// set number of grid points for a mirror, used in both heat map generation and energy calculation
		panel.add(new JLabel("Mirror mesh: "));
		final JPanel p1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		p1.add(mirrorNxTextField);
		p1.add(new JLabel("  \u00D7  "));
		p1.add(mirrorNyTextField);
		panel.add(p1);
		panel.add(new JLabel("Must be power of 2"));

		// select the model for racks
		panel.add(new JLabel("Rack model:"));
		panel.add(rackModelComboBox);
		panel.add(rackModelLabelRight);

		// set number of grid points for a solar rack, used in both heat map generation and energy calculation
		panel.add(rackCellSizeLabelLeft);
		panel.add(rackCellSizeTextField);
		panel.add(rackCellSizeLabelRight);

		// set the time step
		panel.add(new JLabel("Time step: "));
		panel.add(timeStepTextField);
		timeStepTextField.setColumns(6);
		panel.add(new JLabel("Minutes"));

		// choose air mass
		panel.add(new JLabel("Air mass: "));
		panel.add(airMassComboBox);
		panel.add(new JLabel("Dimensionless"));

		SpringUtilities.makeCompactGrid(panel, 5, 3, 8, 8, 8, 8);

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		final JButton okButton = new JButton("OK");
		okButton.addActionListener(okListener);
		okButton.setActionCommand("OK");
		buttonPanel.add(okButton);
		getRootPane().setDefaultButton(okButton);

		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				SimulationSettingsDialog.this.dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPanel.add(cancelButton);

		pack();
		setLocationRelativeTo(MainFrame.getInstance());

	}

}