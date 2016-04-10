package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.concord.energy3d.scene.Scene;

/**
 * @author Charles Xie
 * 
 */
class PropertiesDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public PropertiesDialog() {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Properties");

		getContentPane().setLayout(new BorderLayout());
		final JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
		panel.setBorder(new EmptyBorder(15, 15, 15, 15));
		getContentPane().add(panel, BorderLayout.CENTER);

		final JComboBox<String> unitSystemComboBox = new JComboBox<String>(new String[] { "International System of Units", "United States Customary Units" });
		if (Scene.getInstance().getUnit() == Scene.Unit.USCustomaryUnits)
			unitSystemComboBox.setSelectedIndex(1);
		final JComboBox<String> studentModeComboBox = new JComboBox<String>(new String[] { "No", "Yes" });
		if (Scene.getInstance().isStudentMode())
			studentModeComboBox.setSelectedIndex(1);
		final JTextField projectNameField = new JTextField(Scene.getInstance().getProjectName());

		ActionListener okListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				switch (unitSystemComboBox.getSelectedIndex()) {
				case 0:
					Scene.getInstance().setUnit(Scene.Unit.InternationalSystemOfUnits);
					break;
				case 1:
					Scene.getInstance().setUnit(Scene.Unit.USCustomaryUnits);
					break;
				}
				EnergyPanel.getInstance().updateWeatherData();
				EnergyPanel.getInstance().update();
				Scene.getInstance().setProjectName(projectNameField.getText());
				Scene.getInstance().setStudentMode(studentModeComboBox.getSelectedIndex() == 1);
				Scene.getInstance().setEdited(true);
				PropertiesDialog.this.dispose();
			}
		};

		// set project name
		panel.add(new JLabel("Project Name: "));
		panel.add(projectNameField);

		// set project name
		panel.add(new JLabel("Student Mode: "));
		panel.add(studentModeComboBox);

		// choose unit system
		panel.add(new JLabel("Unit System: "));
		panel.add(unitSystemComboBox);

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
				PropertiesDialog.this.dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPanel.add(cancelButton);

		pack();
		setLocationRelativeTo(MainFrame.getInstance());

	}

}