package org.concord.energy3d.gui;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.undo.*;
import org.concord.energy3d.util.Util;

import javax.swing.*;
import java.awt.*;

/**
 * @author Charles Xie
 */

public class RackFixedTiltAngleChanger {

    private final static RackFixedTiltAngleChanger instance = new RackFixedTiltAngleChanger();
    private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

    private RackFixedTiltAngleChanger() {
    }

    public static RackFixedTiltAngleChanger getInstance() {
        return instance;
    }

    public void change() {

        final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
        if (!(selectedPart instanceof Rack)) {
            return;
        }
        final Rack rack = (Rack) selectedPart;
        if (rack.areMonthlyTiltAnglesSet()) {
            if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "<html>Choosing the fixed tilt angle will remove existing settings for seasonal tilt angles.<br>Are you sure?", "Confirm", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
        final String title = "<html>Tilt Angle of " + partInfo + " (&deg;)</html>";
        final String footnote = "<html><hr><font size=2>The tilt angle of a rack is the angle between its surface and the ground surface.<br>The tilt angle must be between -90&deg; and 90&deg;.<hr></html>";
        final JPanel gui = new JPanel(new BorderLayout());
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
        final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
        final JRadioButton rb2 = new JRadioButton("All Racks on This Foundation");
        final JRadioButton rb3 = new JRadioButton("All Racks");
        panel.add(rb1);
        panel.add(rb2);
        panel.add(rb3);
        final ButtonGroup bg = new ButtonGroup();
        bg.add(rb1);
        bg.add(rb2);
        bg.add(rb3);
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
        }
        gui.add(panel, BorderLayout.CENTER);
        final JTextField inputField = new JTextField(rack.getTiltAngle() + "");
        gui.add(inputField, BorderLayout.SOUTH);

        final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
        final JOptionPane optionPane = new JOptionPane(new Object[]{title, footnote, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
        final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Tilt Angle");

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
                        boolean changed = val != rack.getTiltAngle();
                        final double tiltAngle = val;
                        if (rb1.isSelected()) {
                            if (changed) {
                                final ChangeTiltAngleCommand c = new ChangeTiltAngleCommand(rack);
                                SceneManager.getTaskManager().update(() -> {
                                    rack.setTiltAngle(tiltAngle);
                                    rack.draw();
                                    if (rack.checkContainerIntersection()) {
                                        EventQueue.invokeLater(() -> {
                                            JOptionPane.showMessageDialog(MainFrame.getInstance(), "The rack cannot be tilted at such an angle as it would cut into the underlying surface.",
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
                            final Foundation foundation = rack.getTopContainer();
                            if (!changed) {
                                for (final Rack x : foundation.getRacks()) {
                                    if (x.getTiltAngle() != val) {
                                        changed = true;
                                        break;
                                    }
                                }
                            }
                            if (changed) {
                                final ChangeFoundationRackTiltAngleCommand c = new ChangeFoundationRackTiltAngleCommand(foundation);
                                SceneManager.getTaskManager().update(() -> {
                                    foundation.setTiltAngleForRacks(tiltAngle);
                                    if (foundation.checkContainerIntersectionForRacks()) {
                                        EventQueue.invokeLater(() -> {
                                            JOptionPane.showMessageDialog(MainFrame.getInstance(), "Racks cannot be tilted at such an angle as one or more would cut into the underlying surface.", "Illegal Tilt Angle", JOptionPane.ERROR_MESSAGE);
                                            c.undo();
                                        });
                                    } else {
                                        EventQueue.invokeLater(() -> SceneManager.getInstance().getUndoManager().addEdit(c));
                                    }
                                    return null;
                                });
                            }
                            selectedScopeIndex = 1;
                        } else if (rb3.isSelected()) {
                            if (!changed) {
                                for (final Rack x : Scene.getInstance().getAllRacks()) {
                                    if (x.getTiltAngle() != val) {
                                        changed = true;
                                        break;
                                    }
                                }
                            }
                            if (changed) {
                                final ChangeTiltAngleForAllRacksCommand c = new ChangeTiltAngleForAllRacksCommand();
                                SceneManager.getTaskManager().update(() -> {
                                    Scene.getInstance().setTiltAngleForAllRacks(tiltAngle);
                                    if (Scene.getInstance().checkContainerIntersectionForAllRacks()) {
                                        EventQueue.invokeLater(() -> {
                                            JOptionPane.showMessageDialog(MainFrame.getInstance(), "Racks cannot be tilted at such an angle as one or more would cut into the underlying surface.", "Illegal Tilt Angle", JOptionPane.ERROR_MESSAGE);
                                            c.undo();
                                        });
                                    } else {
                                        EventQueue.invokeLater(() -> SceneManager.getInstance().getUndoManager().addEdit(c));
                                    }
                                    return null;
                                });
                            }
                            selectedScopeIndex = 2;
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