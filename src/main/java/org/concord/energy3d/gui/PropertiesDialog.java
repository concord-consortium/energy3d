package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.SpringUtilities;

/**
 * @author Charles Xie
 * 
 */
class PropertiesDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public PropertiesDialog() {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Properties - " + Scene.getInstance().getParts().size() + " parts");

		getContentPane().setLayout(new BorderLayout());
		final JPanel panel = new JPanel(new SpringLayout());
		panel.setBorder(new EmptyBorder(15, 15, 15, 15));
		getContentPane().add(panel, BorderLayout.CENTER);

		final JComboBox<String> projectTypeComboBox = new JComboBox<String>(new String[] { "Building", "Photovoltaic Solar Power System", "Concentrated Solar Power System" });
		projectTypeComboBox.setSelectedIndex(Scene.getInstance().getProjectType() - 1);
		final JComboBox<String> unitSystemComboBox = new JComboBox<String>(new String[] { "International System of Units", "United States Customary Units" });
		if (Scene.getInstance().getUnit() == Scene.Unit.USCustomaryUnits) {
			unitSystemComboBox.setSelectedIndex(1);
		}
		final JComboBox<String> studentModeComboBox = new JComboBox<String>(new String[] { "No", "Yes" });
		if (Scene.getInstance().isStudentMode()) {
			studentModeComboBox.setSelectedIndex(1);
		}
		final JTextField projectNameField = new JTextField(Scene.getInstance().getProjectName());
		final JComboBox<String> foundationOverlapComboBox = new JComboBox<String>(new String[] { "Disallowed", "Allowed" });
		if (!Scene.getInstance().getDisallowFoundationOverlap()) {
			foundationOverlapComboBox.setSelectedIndex(1);
		}
		final JComboBox<String> onlySolarAnalysisComboBox = new JComboBox<String>(new String[] { "No", "Yes" });
		if (Scene.getInstance().getOnlySolarAnalysis()) {
			onlySolarAnalysisComboBox.setSelectedIndex(1);
		}
		final JComboBox<String> groundImageColorationComboBox = new JComboBox<String>(new String[] { "Dark Colored", "Light Colored" });
		groundImageColorationComboBox.setSelectedIndex(Scene.getInstance().isGroundImageLightColored() ? 1 : 0);

		final ActionListener okListener = new ActionListener() {
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
				Scene.getInstance().setProjectName(projectNameField.getText());
				Scene.getInstance().setProjectType(projectTypeComboBox.getSelectedIndex() + 1);
				Scene.getInstance().setStudentMode(studentModeComboBox.getSelectedIndex() == 1);
				Scene.getInstance().setDisallowFoundationOverlap(foundationOverlapComboBox.getSelectedIndex() == 0);
				Scene.getInstance().setOnlySolarAnalysis(onlySolarAnalysisComboBox.getSelectedIndex() == 1);
				Scene.getInstance().setGroundImageLightColored(groundImageColorationComboBox.getSelectedIndex() == 1);
				Scene.getInstance().setEdited(true);
				EnergyPanel.getInstance().updateWeatherData();
				EnergyPanel.getInstance().update();
				PropertiesDialog.this.dispose();
			}
		};

		// set project name
		panel.add(new JLabel("Project Name: "));
		panel.add(projectNameField);

		// set project type
		panel.add(new JLabel("Project Type: "));
		panel.add(projectTypeComboBox);

		// set student mode
		panel.add(new JLabel("Student Mode: "));
		panel.add(studentModeComboBox);

		// choose unit system
		panel.add(new JLabel("Unit System: "));
		panel.add(unitSystemComboBox);

		// allow building overlap
		panel.add(new JLabel("Foundation Overlap: "));
		panel.add(foundationOverlapComboBox);

		// restrict to only solar analysis
		panel.add(new JLabel("Only Solar Analysis: "));
		panel.add(onlySolarAnalysisComboBox);

		// ground image color
		panel.add(new JLabel("Ground Image Coloration: "));
		panel.add(groundImageColorationComboBox);

		SpringUtilities.makeCompactGrid(panel, 7, 2, 8, 8, 8, 8);

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