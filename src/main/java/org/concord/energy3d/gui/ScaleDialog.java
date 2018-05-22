package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.undo.RescaleCommand;

class ScaleDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField currentTextField;
	private JTextField newTextField;
	private JTextField scaleTextField;
	private final ButtonGroup buttonGroup = new ButtonGroup();

	private JLabel currentLegthLabel;
	private JLabel newLegthLabel;
	private JLabel scaleLabel;
	private JRadioButton scaleByMeasurementRadioButton;

	public ScaleDialog() {
		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Rescale Scene");
		setBounds(100, 100, 310, 215);
		super.setLocationRelativeTo(MainFrame.getInstance());
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		final GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 150, 133, 0, 0 };
		gbl_contentPanel.rowHeights = new int[] { 23, 0, 0, 0, 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);
		{
			scaleByMeasurementRadioButton = new JRadioButton("Scale by Measurement");
			scaleByMeasurementRadioButton.setSelected(true);
			scaleByMeasurementRadioButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					currentLegthLabel.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
					currentTextField.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
					newLegthLabel.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
					newTextField.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
				}
			});
			buttonGroup.add(scaleByMeasurementRadioButton);
			final GridBagConstraints gbc_scaleByMeasurementRadioButton = new GridBagConstraints();
			gbc_scaleByMeasurementRadioButton.insets = new Insets(0, 0, 5, 5);
			gbc_scaleByMeasurementRadioButton.anchor = GridBagConstraints.NORTHWEST;
			gbc_scaleByMeasurementRadioButton.gridx = 0;
			gbc_scaleByMeasurementRadioButton.gridy = 0;
			contentPanel.add(scaleByMeasurementRadioButton, gbc_scaleByMeasurementRadioButton);
		}
		{
			currentLegthLabel = new JLabel("Current Length: ");
			final GridBagConstraints gbc_currentLegthLabel = new GridBagConstraints();
			gbc_currentLegthLabel.insets = new Insets(0, 0, 5, 5);
			gbc_currentLegthLabel.anchor = GridBagConstraints.EAST;
			gbc_currentLegthLabel.gridx = 0;
			gbc_currentLegthLabel.gridy = 1;
			contentPanel.add(currentLegthLabel, gbc_currentLegthLabel);
		}
		{
			currentTextField = new JTextField();
			final GridBagConstraints gbc_currentTextField = new GridBagConstraints();
			gbc_currentTextField.insets = new Insets(0, 0, 5, 5);
			gbc_currentTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_currentTextField.gridx = 1;
			gbc_currentTextField.gridy = 1;
			contentPanel.add(currentTextField, gbc_currentTextField);
			currentTextField.setColumns(10);
		}
		{
			newLegthLabel = new JLabel("New Length: ");
			final GridBagConstraints gbc_newLegthLabel = new GridBagConstraints();
			gbc_newLegthLabel.anchor = GridBagConstraints.EAST;
			gbc_newLegthLabel.insets = new Insets(0, 0, 5, 5);
			gbc_newLegthLabel.gridx = 0;
			gbc_newLegthLabel.gridy = 2;
			contentPanel.add(newLegthLabel, gbc_newLegthLabel);
		}
		{
			newTextField = new JTextField();
			final GridBagConstraints gbc_newTextField = new GridBagConstraints();
			gbc_newTextField.insets = new Insets(0, 0, 5, 5);
			gbc_newTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_newTextField.gridx = 1;
			gbc_newTextField.gridy = 2;
			contentPanel.add(newTextField, gbc_newTextField);
			newTextField.setColumns(10);
		}
		{
			final JRadioButton scaleManuallyRadioButton = new JRadioButton("Scale by Factor");
			scaleManuallyRadioButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					scaleLabel.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
					scaleTextField.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
				}
			});
			buttonGroup.add(scaleManuallyRadioButton);
			final GridBagConstraints gbc_scaleManuallyRadioButton = new GridBagConstraints();
			gbc_scaleManuallyRadioButton.anchor = GridBagConstraints.WEST;
			gbc_scaleManuallyRadioButton.insets = new Insets(0, 0, 5, 5);
			gbc_scaleManuallyRadioButton.gridx = 0;
			gbc_scaleManuallyRadioButton.gridy = 3;
			contentPanel.add(scaleManuallyRadioButton, gbc_scaleManuallyRadioButton);
		}
		{
			scaleLabel = new JLabel("Scale Factor: ");
			scaleLabel.setEnabled(false);
			final GridBagConstraints gbc_scaleLabel = new GridBagConstraints();
			gbc_scaleLabel.anchor = GridBagConstraints.EAST;
			gbc_scaleLabel.insets = new Insets(0, 0, 0, 5);
			gbc_scaleLabel.gridx = 0;
			gbc_scaleLabel.gridy = 4;
			contentPanel.add(scaleLabel, gbc_scaleLabel);
		}
		{
			scaleTextField = new JTextField();
			scaleTextField.setEnabled(false);
			final GridBagConstraints gbc_scaleTextField = new GridBagConstraints();
			gbc_scaleTextField.insets = new Insets(0, 0, 0, 5);
			gbc_scaleTextField.fill = GridBagConstraints.HORIZONTAL;
			gbc_scaleTextField.gridx = 1;
			gbc_scaleTextField.gridy = 4;
			contentPanel.add(scaleTextField, gbc_scaleTextField);
			scaleTextField.setColumns(10);
		}
		{
			final JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				final JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						try {
							final double scale;
							if (scaleByMeasurementRadioButton.isSelected()) {
								scale = Double.parseDouble(newTextField.getText()) / Double.parseDouble(currentTextField.getText());
							} else {
								scale = Double.parseDouble(scaleTextField.getText());
							}
							final RescaleCommand c = new RescaleCommand();
							Scene.getInstance().setScale(Scene.getInstance().getScale() * scale);
							Scene.getInstance().setEdited(true);
							SceneManager.getInstance().getUndoManager().addEdit(c);
							ScaleDialog.this.dispose();
							Scene.getInstance().redrawAll();
							EnergyPanel.getInstance().updateRadiationHeatMap();
						} catch (final NumberFormatException err) {
							err.printStackTrace();
							JOptionPane.showMessageDialog(ScaleDialog.this, "Invalid input: " + err.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
						}
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				final JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						ScaleDialog.this.dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

}