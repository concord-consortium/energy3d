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
import org.concord.energy3d.simulation.Cost;
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
		final JPanel panel = new JPanel(new GridLayout(4, 3, 8, 8));
		panel.setBorder(new EmptyBorder(15, 15, 15, 15));
		getContentPane().add(panel, BorderLayout.CENTER);

		// set the budget limit
		panel.add(new JLabel("Budget: "));
		final JTextField budgetTextField = new JTextField(FORMAT1.format(Cost.getInstance().getBudget()));
		panel.add(budgetTextField);
		budgetTextField.setColumns(6);
		panel.add(new JLabel("Dollars"));

		// set the grid size ("solar step")
		panel.add(new JLabel("Irradiation Grid Cell Size: "));
		final JTextField cellSizeTextField = new JTextField(FORMAT1.format(SolarIrradiation.getInstance().getSolarStep()));
		panel.add(cellSizeTextField);
		cellSizeTextField.setColumns(6);
		panel.add(new JLabel("Internal unit"));

		// set the time step
		panel.add(new JLabel("Time Step: "));
		final JTextField timeStepTextField = new JTextField(FORMAT2.format(SolarIrradiation.getInstance().getTimeStep()));
		panel.add(timeStepTextField);
		timeStepTextField.setColumns(6);
		panel.add(new JLabel("Minutes"));

		// choose air mass
		panel.add(new JLabel("Air Mass: "));
		final JComboBox<String> airMassComboBox = new JComboBox<String>(new String[] { "None", "Kasten-Young", "Sphere Model" });
		airMassComboBox.setSelectedIndex(SolarIrradiation.getInstance().getAirMassSelection() + 1);
		panel.add(airMassComboBox);
		panel.add(new JLabel("Dimensionless"));

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				double cellSize;
				int timeStep;
				int budget;
				try {
					cellSize = Double.parseDouble(cellSizeTextField.getText());
					timeStep = (int) Double.parseDouble(timeStepTextField.getText());
					budget = (int) Double.parseDouble(budgetTextField.getText());
				} catch (final NumberFormatException err) {
					err.printStackTrace();
					JOptionPane.showMessageDialog(SimulationSettingsDialog.this, "Invalid input: " + err.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				// range check
				if (budget <= 1000) {
					JOptionPane.showMessageDialog(SimulationSettingsDialog.this, "Your budget is too low to construct a building.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (cellSize < 0.1 || cellSize > 4) {
					JOptionPane.showMessageDialog(SimulationSettingsDialog.this, "Cell size must be in 0.1-4.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (timeStep < 5 || timeStep > 30) {
					JOptionPane.showMessageDialog(SimulationSettingsDialog.this, "Time step must be in 5-30.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				Cost.getInstance().setBudget(budget);
				SolarIrradiation.getInstance().setSolarStep(cellSize);
				SolarIrradiation.getInstance().setTimeStep(timeStep);
				SolarIrradiation.getInstance().setAirMassSelection(airMassComboBox.getSelectedIndex() - 1);
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