package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;

import org.concord.energy3d.Designer;
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
		panel.setBorder(new EmptyBorder(8, 8, 8, 8));
		getContentPane().add(panel, BorderLayout.CENTER);

		final JComboBox<String> onlySolarAnalysisComboBox = new JComboBox<String>(new String[] { "No", "Yes" });
		final JTextField designerNameField = new JTextField(Scene.getInstance().getDesigner() == null ? "User" : Scene.getInstance().getDesigner().getName());
		final JTextField designerEmailField = new JTextField(Scene.getInstance().getDesigner() == null ? "" : Scene.getInstance().getDesigner().getEmail());
		final JTextField designerOrganizationField = new JTextField(Scene.getInstance().getDesigner() == null ? "" : Scene.getInstance().getDesigner().getOrganization());
		final JComboBox<String> projectTypeComboBox = new JComboBox<String>(new String[] { "Building", "Photovoltaic Solar Power System", "Concentrated Solar Power System" });
		projectTypeComboBox.setSelectedIndex(Scene.getInstance().getProjectType() - 1);
		projectTypeComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if (projectTypeComboBox.getSelectedIndex() == 0) {
						onlySolarAnalysisComboBox.setSelectedIndex(0);
					} else {
						onlySolarAnalysisComboBox.setSelectedIndex(1);
					}
				}
			}
		});
		final JComboBox<String> unitSystemComboBox = new JComboBox<String>(new String[] { "International System of Units", "United States Customary Units" });
		if (Scene.getInstance().getUnit() == Scene.Unit.USCustomaryUnits) {
			unitSystemComboBox.setSelectedIndex(1);
		}
		final JComboBox<String> studentModeComboBox = new JComboBox<String>(new String[] { "No", "Yes" });
		if (Scene.getInstance().isStudentMode()) {
			studentModeComboBox.setSelectedIndex(1);
		}
		final JTextField projectNameField = new JTextField(Scene.getInstance().getProjectName());
		final JTextField projectDescriptionField = new JTextField(Scene.getInstance().getProjectDescription());
		projectDescriptionField.setColumns(10);
		final JComboBox<String> foundationOverlapComboBox = new JComboBox<String>(new String[] { "Disallowed", "Allowed" });
		if (!Scene.getInstance().getDisallowFoundationOverlap()) {
			foundationOverlapComboBox.setSelectedIndex(1);
		}
		if (Scene.getInstance().getOnlySolarAnalysis()) {
			onlySolarAnalysisComboBox.setSelectedIndex(1);
		}
		final JComboBox<String> snapshotLoggingComboBox = new JComboBox<String>(new String[] { "Yes", "No" });
		if (Scene.getInstance().getNoSnaphshotLogging()) {
			snapshotLoggingComboBox.setSelectedIndex(1);
		}
		final JComboBox<String> groundImageColorationComboBox = new JComboBox<String>(new String[] { "Dark Colored", "Light Colored" });
		groundImageColorationComboBox.setSelectedIndex(Scene.getInstance().isGroundImageLightColored() ? 1 : 0);
		final JComboBox<String> instructionTabHeaderComboBox = new JComboBox<String>(new String[] { "Show", "Hide" });
		instructionTabHeaderComboBox.setSelectedIndex(Scene.getInstance().isInstructionTabHeaderVisible() ? 0 : 1);

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
				final String designerName = designerNameField.getText();
				final String designerEmail = designerEmailField.getText();
				final String designerOrganization = designerOrganizationField.getText();
				if (designerName != null && !designerName.trim().equals("")) {
					Designer designer = Scene.getInstance().getDesigner();
					if (designer == null) {
						designer = new Designer();
						Scene.getInstance().setDesigner(designer);
					}
					designer.setName(designerName);
				} else {
					Designer designer = Scene.getInstance().getDesigner();
					if (designer == null) {
						designer = new Designer();
						Scene.getInstance().setDesigner(designer);
					}
					designer.setName("User");
				}
				if (designerEmail != null && !designerEmail.trim().equals("")) {
					Designer designer = Scene.getInstance().getDesigner();
					if (designer == null) {
						designer = new Designer();
						Scene.getInstance().setDesigner(designer);
					}
					designer.setEmail(designerEmail);
				} else {
					Designer designer = Scene.getInstance().getDesigner();
					if (designer == null) {
						designer = new Designer();
						Scene.getInstance().setDesigner(designer);
					}
					designer.setEmail(null);
				}
				if (designerOrganization != null && !designerOrganization.trim().equals("")) {
					Designer designer = Scene.getInstance().getDesigner();
					if (designer == null) {
						designer = new Designer();
						Scene.getInstance().setDesigner(designer);
					}
					designer.setOrganization(designerOrganization);
				} else {
					Designer designer = Scene.getInstance().getDesigner();
					if (designer == null) {
						designer = new Designer();
						Scene.getInstance().setDesigner(designer);
					}
					designer.setOrganization(null);
				}
				Scene.getInstance().setProjectType(projectTypeComboBox.getSelectedIndex() + 1);
				Scene.getInstance().setProjectName(projectNameField.getText());
				Scene.getInstance().setProjectDescription(projectDescriptionField.getText());
				Scene.getInstance().setStudentMode(studentModeComboBox.getSelectedIndex() == 1);
				Scene.getInstance().setDisallowFoundationOverlap(foundationOverlapComboBox.getSelectedIndex() == 0);
				Scene.getInstance().setOnlySolarAnalysis(onlySolarAnalysisComboBox.getSelectedIndex() == 1);
				Scene.getInstance().setNoSnapshotLogging(snapshotLoggingComboBox.getSelectedIndex() == 1);
				Scene.getInstance().setGroundImageLightColored(groundImageColorationComboBox.getSelectedIndex() == 1);
				Scene.getInstance().setInstructionTabHeaderVisible(instructionTabHeaderComboBox.getSelectedIndex() == 0);
				Scene.getInstance().setEdited(true);
				EnergyPanel.getInstance().updateWeatherData();
				EnergyPanel.getInstance().update();
				PropertiesDialog.this.dispose();
			}
		};

		// set designer name
		panel.add(new JLabel("Designer Name: "));
		panel.add(designerNameField);

		// set designer email
		panel.add(new JLabel("Designer Email: "));
		panel.add(designerEmailField);

		// set designer organization
		panel.add(new JLabel("Designer Organization: "));
		panel.add(designerOrganizationField);

		// set project type
		panel.add(new JLabel("Project Type: "));
		panel.add(projectTypeComboBox);

		// set project name
		panel.add(new JLabel("Project Name: "));
		panel.add(projectNameField);

		// set project description
		panel.add(new JLabel("Project Description: "));
		panel.add(projectDescriptionField);

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

		// enable or disable snapshot logging for UX
		panel.add(new JLabel("Snapshot Logging: "));
		panel.add(snapshotLoggingComboBox);

		// ground image color
		panel.add(new JLabel("Ground Image Coloration: "));
		panel.add(groundImageColorationComboBox);

		// instruction tab
		panel.add(new JLabel("Instruction Tab Header: "));
		panel.add(instructionTabHeaderComboBox);

		SpringUtilities.makeCompactGrid(panel, 13, 2, 8, 8, 8, 8);

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
		projectNameField.requestFocusInWindow();
		projectNameField.selectAll();

	}

}