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
		final JPanel panel = new JPanel(new GridLayout(6, 3, 8, 8));
		panel.setBorder(new EmptyBorder(15, 15, 15, 15));
		getContentPane().add(panel, BorderLayout.CENTER);

		final Scene s = Scene.getInstance();
		final JTextField plateNxTextField = new JTextField(s.getPlateNx() + "");
		final JTextField plateNyTextField = new JTextField(s.getPlateNy() + "");
		final JTextField cellSizeTextField = new JTextField(FORMAT1.format(Scene.getInstance().getSolarStep()));
		final JTextField heatVectorLengthTextField = new JTextField(FORMAT1.format(Scene.getInstance().getHeatVectorLength()));
		final JTextField timeStepTextField = new JTextField(FORMAT2.format(Scene.getInstance().getTimeStep()));
		final JComboBox<String> airMassComboBox = new JComboBox<String>(new String[] { "None", "Kasten-Young", "Sphere Model" });

		ActionListener okListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				double cellSize;
				int timeStep;
				double heatVectorLength;
				int plateNx;
				int plateNy;
				try {
					cellSize = Double.parseDouble(cellSizeTextField.getText());
					heatVectorLength = Double.parseDouble(heatVectorLengthTextField.getText());
					timeStep = (int) Double.parseDouble(timeStepTextField.getText());
					plateNx = Integer.parseInt(plateNxTextField.getText());
					plateNy = Integer.parseInt(plateNyTextField.getText());
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
				if (timeStep < 5 || timeStep > 60) {
					JOptionPane.showMessageDialog(SimulationSettingsDialog.this, "Time step must be in 5-60 seconds.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (plateNx < 2 || plateNy < 2) {
					JOptionPane.showMessageDialog(SimulationSettingsDialog.this, "Number of grid cells in x or y direction must be at least two.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if ((plateNx & (plateNx - 1)) != 0 || (plateNy & (plateNy - 1)) != 0) {
					JOptionPane.showMessageDialog(SimulationSettingsDialog.this, "Number of grid cells in x or y direction must be power of two.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				s.setSolarStep(cellSize);
				s.setTimeStep(timeStep);
				s.setPlateNx(plateNx);
				s.setPlateNy(plateNy);
				s.setHeatVectorLength(heatVectorLength);
				s.setEdited(true);
				SolarRadiation.getInstance().setAirMassSelection(airMassComboBox.getSelectedIndex() - 1);
				EnergyPanel.getInstance().clearRadiationHeatMap();
				SimulationSettingsDialog.this.dispose();
			}
		};

		// set number of grid cells for a plate
		panel.add(new JLabel("# Grid Cells in X-Direction: "));
		panel.add(plateNxTextField);
		plateNxTextField.setColumns(6);
		panel.add(new JLabel("For panels (must be power of 2)"));

		// set number of grid cells for a plate
		panel.add(new JLabel("# Grid Cells in Y-Direction: "));
		panel.add(plateNyTextField);
		panel.add(new JLabel("For panels (must be power of 2)"));

		// set the grid size ("solar step")
		panel.add(new JLabel("Radiation Grid Cell Size: "));
		panel.add(cellSizeTextField);
		panel.add(new JLabel("For non-panels (internal unit)"));

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