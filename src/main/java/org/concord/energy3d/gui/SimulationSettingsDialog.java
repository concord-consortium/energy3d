package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.concord.energy3d.gui.EnergyPanel.UpdateRadiation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.simulation.SolarIrradiation;

class SimulationSettingsDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final static DecimalFormat FORMAT1 = new DecimalFormat("#0.##");
	private final static DecimalFormat FORMAT2 = new DecimalFormat("##");

	public SimulationSettingsDialog() {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Simulation Settings");

		getContentPane().setLayout(new BorderLayout());
		final JPanel contentPanel = new JPanel(new GridLayout(2, 2));
		contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		// set the grid size ("solar step")
		contentPanel.add(new JLabel("Irradiation Grid Cell Size: "));

		final JTextField cellSizeTextField = new JTextField(FORMAT1.format(SolarIrradiation.getInstance().getSolarStep()));
		contentPanel.add(cellSizeTextField);
		cellSizeTextField.setColumns(10);

		// set the time step
		contentPanel.add(new JLabel("Time Step: "));

		final JTextField timeStepTextField = new JTextField(FORMAT2.format(SolarIrradiation.getInstance().getTimeStep()));
		contentPanel.add(timeStepTextField);
		timeStepTextField.setColumns(10);

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				double cellSize;
				int timeStep;
				try {
					cellSize = Double.parseDouble(cellSizeTextField.getText());
					timeStep = (int) Double.parseDouble(timeStepTextField.getText());
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
				if (timeStep < 5 || timeStep > 30) {
					JOptionPane.showMessageDialog(SimulationSettingsDialog.this, "Time step must be in 5-30.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				SolarIrradiation.getInstance().setSolarStep(cellSize);
				SolarIrradiation.getInstance().setTimeStep(timeStep);
				Scene.getInstance().setEdited(true);
				EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
				SimulationSettingsDialog.this.dispose();
			}
		});
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