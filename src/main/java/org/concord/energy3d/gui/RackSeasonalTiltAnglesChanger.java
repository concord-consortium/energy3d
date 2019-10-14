package org.concord.energy3d.gui;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.AnnualGraph;
import org.concord.energy3d.undo.*;
import org.concord.energy3d.util.SpringUtilities;
import org.concord.energy3d.util.Util;

import javax.swing.*;
import java.awt.*;

/**
 * @author Charles Xie
 */

public class RackSeasonalTiltAnglesChanger {

    private final static RackSeasonalTiltAnglesChanger instance = new RackSeasonalTiltAnglesChanger();
    private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

    private RackSeasonalTiltAnglesChanger() {
    }

    public static RackSeasonalTiltAnglesChanger getInstance() {
        return instance;
    }

    public void change() {

        final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
        if (!(selectedPart instanceof Rack)) {
            return;
        }
        final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
        final Rack rack = (Rack) selectedPart;
        final String title = "<html>Seasonally Adjusted Tilt Angles of " + partInfo + " (&deg;)</html>";
        final String footnote = "<html><hr><font size=2>The tilt angle of a rack is the angle between its surface and the ground surface.<br>The tilt angle must be between -90&deg; and 90&deg; and may be adjusted seasonally.<hr></html>";
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

        final JPanel inputPanel = new JPanel(new SpringLayout());
        final JTextField[] fields = new JTextField[12];
        for (int i = 0; i < 12; i++) {
            final JLabel l = new JLabel(AnnualGraph.THREE_LETTER_MONTH[i] + ": ", JLabel.LEFT);
            inputPanel.add(l);
            fields[i] = new JTextField(rack.getTiltAngleOfMonth(i) + "", 5);
            l.setLabelFor(fields[i]);
            inputPanel.add(fields[i]);
        }
        SpringUtilities.makeCompactGrid(inputPanel, 4, 6, 6, 6, 6, 6);
        gui.add(inputPanel, BorderLayout.SOUTH);

        final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
        final JOptionPane optionPane = new JOptionPane(new Object[]{title, footnote, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
        final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Seasonally Adjusted Tilt Angles");

        while (true) {
            fields[0].selectAll();
            fields[0].requestFocusInWindow();
            dialog.setVisible(true);
            final Object choice = optionPane.getValue();
            if (choice == options[1] || choice == null) {
                break;
            } else {
                final double[] val = new double[12];
                boolean ok = true;
                for (int i = 0; i < 12; i++) {
                    try {
                        val[i] = Double.parseDouble(fields[i].getText());
                    } catch (final NumberFormatException exception) {
                        JOptionPane.showMessageDialog(MainFrame.getInstance(), AnnualGraph.THREE_LETTER_MONTH[i] + ": " + fields[i].getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
                        ok = false;
                    }
                    if (ok) {
                        if (val[i] < -90 || val[i] > 90) {
                            JOptionPane.showMessageDialog(MainFrame.getInstance(), AnnualGraph.THREE_LETTER_MONTH[i] + ": The tilt angle must be between -90 and 90 degrees.", "Range Error", JOptionPane.ERROR_MESSAGE);
                            ok = false;
                        }
                    }
                }
                if (ok) {
                    boolean changed = false;
                    for (int i = 0; i < 12; i++) {
                        if (Util.isZero(val[i] - 90)) {
                            val[i] = 89.999;
                        } else if (Util.isZero(val[i] + 90)) {
                            val[i] = -89.999;
                        }
                        if (!changed) {
                            changed = val[i] != rack.getTiltAngleOfMonth(i);
                        }
                    }
                    if (rb1.isSelected()) {
                        if (changed) {
                            final ChangeMonthlyTiltAnglesCommand c = new ChangeMonthlyTiltAnglesCommand(rack);
                            SceneManager.getTaskManager().update(() -> {
                                rack.setMonthlyTiltAngles(val);
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
                                for (int i = 0; i < 12; i++) {
                                    if (x.getTiltAngleOfMonth(i) != val[i]) {
                                        changed = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (changed) {
                            final ChangeFoundationRackMonthlyTiltAnglesCommand c = new ChangeFoundationRackMonthlyTiltAnglesCommand(foundation);
                            SceneManager.getTaskManager().update(() -> {
                                foundation.setMonthlyTiltAnglesForRacks(val);
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
                                for (int i = 0; i < 12; i++) {
                                    if (x.getTiltAngleOfMonth(i) != val[i]) {
                                        changed = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (changed) {
                            final ChangeMonthlyTiltAnglesForAllRacksCommand c = new ChangeMonthlyTiltAnglesForAllRacksCommand();
                            SceneManager.getTaskManager().update(() -> {
                                Scene.getInstance().setMonthlyTiltAnglesForAllRacks(val);
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