package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.concord.energy3d.gui.EnergyPanel.UpdateRadiation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.simulation.SolarRadiation;

/**
 * @author Charles Xie
 * 
 */
class SimulationSettingsDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final static DecimalFormat FORMAT1 = new DecimalFormat("#0.##");
	private final static DecimalFormat FORMAT2 = new DecimalFormat("##");

	public SimulationSettingsDialog() {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Simulation Settings");

		getContentPane().setLayout(new BorderLayout());
		final JPanel panel = new JPanel(new GridLayout(5, 3, 8, 8));
		panel.setBorder(new EmptyBorder(15, 15, 15, 15));
		getContentPane().add(panel, BorderLayout.CENTER);

		final JTextField cellSizeTextField = new JTextField(FORMAT1.format(SolarRadiation.getInstance().getSolarStep()));
		final JTextField heatVectorLengthTextField = new JTextField(FORMAT1.format(Scene.getInstance().getHeatVectorLength()));
		final JTextField timeStepTextField = new JTextField(FORMAT2.format(SolarRadiation.getInstance().getTimeStep()));
		final JTextField volumetricHeatCapacityTextField = new JTextField(FORMAT1.format(Scene.getInstance().getVolumetricHeatCapacity()));
		final JComboBox<String> airMassComboBox = new JComboBox<String>(new String[] { "None", "Kasten-Young", "Sphere Model" });

		ActionListener okListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				double cellSize;
				int timeStep;
				double volumetricHeatCapacity;
				double heatVectorLength;
				try {
					cellSize = Double.parseDouble(cellSizeTextField.getText());
					heatVectorLength = Double.parseDouble(heatVectorLengthTextField.getText());
					timeStep = (int) Double.parseDouble(timeStepTextField.getText());
					volumetricHeatCapacity = Double.parseDouble(volumetricHeatCapacityTextField.getText());
				} catch (final NumberFormatException err) {
					err.printStackTrace();
					JOptionPane.showMessageDialog(SimulationSettingsDialog.this, "Invalid input: " + err.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				// range check
				if (cellSize < 0.1 || cellSize > 4) {
					JOptionPane.showMessageDialog(SimulationSettingsDialog.this, "Cell size must be in 0.1-4.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (heatVectorLength < 1000) {
					JOptionPane.showMessageDialog(SimulationSettingsDialog.this, "Heat arrow length must be greater than 1000.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (timeStep < 5 || timeStep > 30) {
					JOptionPane.showMessageDialog(SimulationSettingsDialog.this, "Time step must be in 5-30.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (volumetricHeatCapacity <= 0) {
					JOptionPane.showMessageDialog(SimulationSettingsDialog.this, "Volumetric heat capacity cannot be zero or negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				SolarRadiation.getInstance().setSolarStep(cellSize);
				SolarRadiation.getInstance().setTimeStep(timeStep);
				SolarRadiation.getInstance().setAirMassSelection(airMassComboBox.getSelectedIndex() - 1);
				Scene.getInstance().setHeatVectorLength(heatVectorLength);
				Scene.getInstance().setVolumetricHeatCapacity(volumetricHeatCapacity);
				Scene.getInstance().setEdited(true);
				EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
				SimulationSettingsDialog.this.dispose();
			}
		};

		// set the grid size ("solar step")
		panel.add(new JLabel("Radiation Grid Cell Size: "));
		panel.add(cellSizeTextField);
		cellSizeTextField.setColumns(6);
		panel.add(new JLabel("Internal unit"));

		// set the heat arrow length
		panel.add(new JLabel("Heat Arrow Length: "));
		panel.add(heatVectorLengthTextField);
		panel.add(new JLabel("Internal unit"));

		// set the time step
		panel.add(new JLabel("Time Step: "));
		panel.add(timeStepTextField);
		timeStepTextField.setColumns(6);
		panel.add(new JLabel("Minutes"));

		// choose air mass
		panel.add(new JLabel("Air Mass: "));
		airMassComboBox.setSelectedIndex(SolarRadiation.getInstance().getAirMassSelection() + 1);
		panel.add(airMassComboBox);
		panel.add(new JLabel("Dimensionless"));

		// set the default volumetric heat capacity
		panel.add(new JLabel("Volumetric Heat Capacity: "));
		panel.add(volumetricHeatCapacityTextField);
		panel.add(new JLabel("<html>kWh/(m<sup>3</sup>&times;C)</html>"));

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