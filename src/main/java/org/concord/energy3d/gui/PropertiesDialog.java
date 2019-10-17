package org.concord.energy3d.gui;

import org.concord.energy3d.Designer;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.SpringUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

/**
 * @author Charles Xie
 */
class PropertiesDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    PropertiesDialog() {

        super(MainFrame.getInstance(), true);
        final Scene s = Scene.getInstance();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle("Properties - " + s.getParts().size() + " parts");


        getContentPane().setLayout(new BorderLayout());
        final JPanel panel = new JPanel(new SpringLayout());
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        getContentPane().add(panel, BorderLayout.CENTER);

        final JComboBox<String> onlySolarAnalysisComboBox = new JComboBox<>(new String[]{"No", "Yes"});
        final JTextField designerNameField = new JTextField(s.getDesigner() == null ? "User" : s.getDesigner().getName());
        final JTextField designerEmailField = new JTextField(s.getDesigner() == null ? "" : s.getDesigner().getEmail());
        final JTextField designerOrganizationField = new JTextField(s.getDesigner() == null ? "" : s.getDesigner().getOrganization());
        final JComboBox<String> projectTypeComboBox = new JComboBox<>(new String[]{"Building", "Photovoltaic Solar Power System", "Concentrated Solar Power System"});
        projectTypeComboBox.setSelectedIndex(s.getProjectType() - 1);
        projectTypeComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (projectTypeComboBox.getSelectedIndex() == 0) {
                    onlySolarAnalysisComboBox.setSelectedIndex(0);
                } else {
                    onlySolarAnalysisComboBox.setSelectedIndex(1);
                }
            }
        });
        final JComboBox<String> unitSystemComboBox = new JComboBox<>(new String[]{"International System of Units", "United States Customary Units"});
        if (s.getUnit() == Scene.Unit.USCustomaryUnits) {
            unitSystemComboBox.setSelectedIndex(1);
        }
        final JComboBox<String> studentModeComboBox = new JComboBox<>(new String[]{"No", "Yes"});
        if (s.isStudentMode()) {
            studentModeComboBox.setSelectedIndex(1);
        }
        final JTextField projectNameField = new JTextField(s.getProjectName());
        final JTextField projectDescriptionField = new JTextField(s.getProjectDescription());
        projectDescriptionField.setColumns(10);
        final JComboBox<String> foundationOverlapComboBox = new JComboBox<>(new String[]{"Disallowed", "Allowed"});
        if (!s.getDisallowFoundationOverlap()) {
            foundationOverlapComboBox.setSelectedIndex(1);
        }
        if (s.getOnlySolarAnalysis()) {
            onlySolarAnalysisComboBox.setSelectedIndex(1);
        }
        final JComboBox<String> snapshotLoggingComboBox = new JComboBox<>(new String[]{"Yes", "No"});
        if (s.getNoSnaphshotLogging()) {
            snapshotLoggingComboBox.setSelectedIndex(1);
        }
        final JComboBox<String> groundImageColorationComboBox = new JComboBox<>(new String[]{"Dark Colored", "Light Colored"});
        groundImageColorationComboBox.setSelectedIndex(s.isGroundImageLightColored() ? 1 : 0);
        final JComboBox<String> instructionTabHeaderComboBox = new JComboBox<>(new String[]{"Show", "Hide"});
        instructionTabHeaderComboBox.setSelectedIndex(s.isInstructionTabHeaderVisible() ? 0 : 1);
        final JComboBox<String> dateFixedComboBox = new JComboBox<>(new String[]{"No", "Yes"});
        dateFixedComboBox.setSelectedIndex(s.isDateFixed() ? 1 : 0);
        final JComboBox<String> locationFixedComboBox = new JComboBox<>(new String[]{"No", "Yes"});
        locationFixedComboBox.setSelectedIndex(s.isLocationFixed() ? 1 : 0);

        final ActionListener okListener = e -> {
            switch (unitSystemComboBox.getSelectedIndex()) {
                case 0:
                    s.setUnit(Scene.Unit.InternationalSystemOfUnits);
                    break;
                case 1:
                    s.setUnit(Scene.Unit.USCustomaryUnits);
                    break;
            }
            final String designerName = designerNameField.getText();
            final String designerEmail = designerEmailField.getText();
            final String designerOrganization = designerOrganizationField.getText();
            if (designerName != null && !designerName.trim().equals("")) {
                Designer designer = s.getDesigner();
                if (designer == null) {
                    designer = new Designer();
                    s.setDesigner(designer);
                }
                designer.setName(designerName);
            } else {
                Designer designer = s.getDesigner();
                if (designer == null) {
                    designer = new Designer();
                    s.setDesigner(designer);
                }
                designer.setName("User");
            }
            if (designerEmail != null && !designerEmail.trim().equals("")) {
                Designer designer = s.getDesigner();
                if (designer == null) {
                    designer = new Designer();
                    s.setDesigner(designer);
                }
                designer.setEmail(designerEmail);
            } else {
                Designer designer = s.getDesigner();
                if (designer == null) {
                    designer = new Designer();
                    s.setDesigner(designer);
                }
                designer.setEmail(null);
            }
            if (designerOrganization != null && !designerOrganization.trim().equals("")) {
                Designer designer = s.getDesigner();
                if (designer == null) {
                    designer = new Designer();
                    s.setDesigner(designer);
                }
                designer.setOrganization(designerOrganization);
            } else {
                Designer designer = s.getDesigner();
                if (designer == null) {
                    designer = new Designer();
                    s.setDesigner(designer);
                }
                designer.setOrganization(null);
            }
            int oldProjectType = s.getProjectType();
            s.setProjectType(projectTypeComboBox.getSelectedIndex() + 1);
            if (s.getProjectType() != oldProjectType) { // if project type changes, adjust the cell size for others
                if (s.getProjectType() == Foundation.TYPE_PV_PROJECT || s.getProjectType() == Foundation.TYPE_CSP_PROJECT) {
                    if (s.getSolarStep() * s.getScale() < 1) {
                        s.setSolarStep(10 / s.getScale());
                    }
                } else {
                    if (s.getSolarStep() * s.getScale() > 2) {
                        s.setSolarStep(0.4 / s.getScale());
                    }
                }
            }
            s.setProjectName(projectNameField.getText());
            s.setProjectDescription(projectDescriptionField.getText());
            s.setStudentMode(studentModeComboBox.getSelectedIndex() == 1);
            s.setDisallowFoundationOverlap(foundationOverlapComboBox.getSelectedIndex() == 0);
            s.setOnlySolarAnalysis(onlySolarAnalysisComboBox.getSelectedIndex() == 1);
            s.setNoSnapshotLogging(snapshotLoggingComboBox.getSelectedIndex() == 1);
            s.setGroundImageLightColored(groundImageColorationComboBox.getSelectedIndex() == 1);
            s.setInstructionTabHeaderVisible(instructionTabHeaderComboBox.getSelectedIndex() == 0);
            s.setDateFixed(dateFixedComboBox.getSelectedIndex() == 1);
            s.setLocationFixed(locationFixedComboBox.getSelectedIndex() == 1);
            s.setEdited(true);
            EnergyPanel.getInstance().updateWeatherData();
            EnergyPanel.getInstance().update();
            PropertiesDialog.this.dispose();
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

        // fixed date
        panel.add(new JLabel("Fixed Date: "));
        panel.add(dateFixedComboBox);

        // fixed location
        panel.add(new JLabel("Fixed Location: "));
        panel.add(locationFixedComboBox);

        SpringUtilities.makeCompactGrid(panel, 15, 2, 8, 8, 8, 8);

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        final JButton okButton = new JButton("OK");
        okButton.addActionListener(okListener);
        okButton.setActionCommand("OK");
        buttonPanel.add(okButton);
        getRootPane().setDefaultButton(okButton);

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> PropertiesDialog.this.dispose());
        cancelButton.setActionCommand("Cancel");
        buttonPanel.add(cancelButton);

        pack();
        setLocationRelativeTo(MainFrame.getInstance());
        projectNameField.requestFocusInWindow();
        projectNameField.selectAll();

    }

}