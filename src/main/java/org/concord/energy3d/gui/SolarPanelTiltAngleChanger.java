package org.concord.energy3d.gui;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.undo.ChangeFoundationSolarPanelTiltAngleCommand;
import org.concord.energy3d.undo.ChangeTiltAngleCommand;
import org.concord.energy3d.undo.ChangeTiltAngleForAllSolarPanelsCommand;
import org.concord.energy3d.undo.ChangeTiltAngleForSolarPanelRowCommand;
import org.concord.energy3d.util.Util;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author Charles Xie
 */

public class SolarPanelTiltAngleChanger {

    private final static SolarPanelTiltAngleChanger instance = new SolarPanelTiltAngleChanger();
    private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

    private SolarPanelTiltAngleChanger() {
    }

    public static SolarPanelTiltAngleChanger getInstance() {
        return instance;
    }

    public void change() {

        final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
        if (!(selectedPart instanceof SolarPanel)) {
            return;
        }
        final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
        final SolarPanel sp = (SolarPanel) selectedPart;
        final String title = "<html>Tilt Angle of " + partInfo + " (&deg;)</html>";
        final String footnote = "<html><hr><font size=2>The tilt angle of a solar panel is the angle between its surface and the base surface.<br>The tilt angle must be between -90&deg; and 90&deg;.<hr></html>";
        final JPanel gui = new JPanel(new BorderLayout());
        final JPanel panel = new JPanel();
        gui.add(panel, BorderLayout.CENTER);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
        final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
        final JRadioButton rb2 = new JRadioButton("Only this Row", true);
        final JRadioButton rb3 = new JRadioButton("All Solar Panels on This Foundation");
        final JRadioButton rb4 = new JRadioButton("All Solar Panels");
        panel.add(rb1);
        panel.add(rb2);
        panel.add(rb3);
        panel.add(rb4);
        final ButtonGroup bg = new ButtonGroup();
        bg.add(rb1);
        bg.add(rb2);
        bg.add(rb3);
        bg.add(rb4);
        switch (selectedScopeIndex) {
            case 0:
                rb1.setSelected(true);
                break;
            case 1:
                rb2.setSelected(true);
                break;
            case 2:
                rb3.setSelected(true);
                break;
            case 3:
                rb4.setSelected(true);
                break;
        }
        gui.add(panel, BorderLayout.CENTER);
        final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(sp.getTiltAngle()));
        gui.add(inputField, BorderLayout.SOUTH);

        final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
        final JOptionPane optionPane = new JOptionPane(new Object[]{title, footnote, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
        final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Tilt Angle");

        while (true) {
            inputField.selectAll();
            inputField.requestFocusInWindow();
            dialog.setVisible(true);
            final Object choice = optionPane.getValue();
            if (choice == options[1] || choice == null) {
                break;
            } else {
                double val = 0;
                boolean ok = true;
                try {
                    val = Double.parseDouble(inputField.getText());
                } catch (final NumberFormatException exception) {
                    JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
                    ok = false;
                }
                if (ok) {
                    if (val < -90 || val > 90) {
                        JOptionPane.showMessageDialog(MainFrame.getInstance(), "The tilt angle must be between -90 and 90 degrees.", "Range Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        if (Util.isZero(val - 90)) {
                            val = 89.999;
                        } else if (Util.isZero(val + 90)) {
                            val = -89.999;
                        }
                        boolean changed = Math.abs(val - sp.getTiltAngle()) > 0.000001;
                        final double tiltAngle = val;
                        if (rb1.isSelected()) {
                            if (changed) {
                                final ChangeTiltAngleCommand c = new ChangeTiltAngleCommand(sp);
                                SceneManager.getTaskManager().update(() -> {
                                    sp.setTiltAngle(tiltAngle);
                                    sp.draw();
                                    if (sp.checkContainerIntersection()) {
                                        EventQueue.invokeLater(() -> {
                                            JOptionPane.showMessageDialog(MainFrame.getInstance(),
                                                    "This tilt angle cannot be set as the solar panel would cut into the underlying surface.",
                                                    "Illegal Tilt Angle", JOptionPane.ERROR_MESSAGE);
                                            c.undo();
                                        });
                                    } else {
                                        SceneManager.getInstance().refresh();
                                        EventQueue.invokeLater(() -> SceneManager.getInstance().getUndoManager().addEdit(c));
                                    }
                                    return null;
                                });
                            }
                            selectedScopeIndex = 0;
                        } else if (rb2.isSelected()) {
                            final List<SolarPanel> row = sp.getRow();
                            if (!changed) {
                                for (final SolarPanel x : row) {
                                    if (Math.abs(val - x.getTiltAngle()) > 0.000001) {
                                        changed = true;
                                        break;
                                    }
                                }
                            }
                            if (changed) {
                                final ChangeTiltAngleForSolarPanelRowCommand c = new ChangeTiltAngleForSolarPanelRowCommand(row);
                                SceneManager.getTaskManager().update(() -> {
                                    boolean intersected = false;
                                    for (final SolarPanel x : row) {
                                        x.setTiltAngle(tiltAngle);
                                        x.draw();
                                        if (x.checkContainerIntersection()) {
                                            intersected = true;
                                            break;
                                        }
                                    }
                                    if (intersected) {
                                        EventQueue.invokeLater(() -> {
                                            JOptionPane.showMessageDialog(MainFrame.getInstance(),
                                                    "This tilt angle cannot be set as one or more solar panels would cut into the underlying surface.",
                                                    "Illegal Tilt Angle", JOptionPane.ERROR_MESSAGE);
                                            c.undo();
                                        });
                                    } else {
                                        SceneManager.getInstance().refresh();
                                        EventQueue.invokeLater(() -> SceneManager.getInstance().getUndoManager().addEdit(c));
                                    }
                                    return null;
                                });
                            }
                            selectedScopeIndex = 1;
                        } else if (rb3.isSelected()) {
                            final Foundation foundation = sp.getTopContainer();
                            if (!changed) {
                                for (final SolarPanel x : foundation.getSolarPanels()) {
                                    if (Math.abs(val - x.getTiltAngle()) > 0.000001) {
                                        changed = true;
                                        break;
                                    }
                                }
                            }
                            if (changed) {
                                final ChangeFoundationSolarPanelTiltAngleCommand c = new ChangeFoundationSolarPanelTiltAngleCommand(foundation);
                                SceneManager.getTaskManager().update(() -> {
                                    foundation.setTiltAngleForSolarPanels(tiltAngle);
                                    if (foundation.checkContainerIntersectionForSolarPanels()) {
                                        EventQueue.invokeLater(() -> {
                                            JOptionPane.showMessageDialog(MainFrame.getInstance(),
                                                    "This tilt angle cannot be set as one or more solar panels would cut into the underlying surface.",
                                                    "Illegal Tilt Angle", JOptionPane.ERROR_MESSAGE);
                                            c.undo();
                                        });
                                    } else {
                                        EventQueue.invokeLater(() -> SceneManager.getInstance().getUndoManager().addEdit(c));
                                    }
                                    return null;
                                });
                            }
                            selectedScopeIndex = 2;
                        } else if (rb4.isSelected()) {
                            if (!changed) {
                                for (final SolarPanel x : Scene.getInstance().getAllSolarPanels()) {
                                    if (Math.abs(val - x.getTiltAngle()) > 0.000001) {
                                        changed = true;
                                        break;
                                    }
                                }
                            }
                            if (changed) {
                                final ChangeTiltAngleForAllSolarPanelsCommand c = new ChangeTiltAngleForAllSolarPanelsCommand();
                                SceneManager.getTaskManager().update(() -> {
                                    Scene.getInstance().setTiltAngleForAllSolarPanels(tiltAngle);
                                    if (Scene.getInstance().checkContainerIntersectionForAllSolarPanels()) {
                                        EventQueue.invokeLater(() -> {
                                            JOptionPane.showMessageDialog(MainFrame.getInstance(),
                                                    "This tilt angle cannot be set as one or more solar panels would cut into the underlying surface.",
                                                    "Illegal Tilt Angle", JOptionPane.ERROR_MESSAGE);
                                            c.undo();
                                        });
                                    } else {
                                        EventQueue.invokeLater(() -> SceneManager.getInstance().getUndoManager().addEdit(c));
                                    }
                                    return null;
                                });
                            }
                            selectedScopeIndex = 3;
                        }
                        if (changed) {
                            PopupMenuFactory.updateAfterEdit();
                        }
                        if (choice == options[0]) {
                            break;
                        }
                    }
                }
            }
        }
    }

}