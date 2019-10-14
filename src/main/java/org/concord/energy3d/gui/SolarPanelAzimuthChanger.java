package org.concord.energy3d.gui;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.undo.ChangeAzimuthCommand;
import org.concord.energy3d.undo.ChangeAzimuthForAllSolarPanelsCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarPanelAzimuthCommand;

import javax.swing.*;
import java.awt.*;

/**
 * @author Charles Xie
 */

public class SolarPanelAzimuthChanger {

    private final static SolarPanelAzimuthChanger instance = new SolarPanelAzimuthChanger();
    private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

    private SolarPanelAzimuthChanger() {
    }

    public static SolarPanelAzimuthChanger getInstance() {
        return instance;
    }

    public void change() {

        final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
        if (!(selectedPart instanceof SolarPanel)) {
            return;
        }
        final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
        final SolarPanel sp = (SolarPanel) selectedPart;
        final Foundation foundation = sp.getTopContainer();
        final String title = "<html>Azimuth Angle of " + partInfo + " (&deg;)</html>";
        final String footnote = "<html><hr><font size=2>The azimuth angle is measured clockwise from the true north.<hr></html>";
        final JPanel gui = new JPanel(new BorderLayout());
        final JPanel panel = new JPanel();
        gui.add(panel, BorderLayout.CENTER);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
        final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
        final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
        final JRadioButton rb3 = new JRadioButton("All Solar Panels");
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
        double a = sp.getRelativeAzimuth() + foundation.getAzimuth();
        if (a > 360) {
            a -= 360;
        }
        final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(a));
        gui.add(inputField, BorderLayout.SOUTH);

        final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
        final JOptionPane optionPane = new JOptionPane(new Object[]{title, footnote, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
        final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Azimuth");

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
                    a = val - foundation.getAzimuth();
                    if (a < 0) {
                        a += 360;
                    }
                    boolean changed = Math.abs(a - sp.getRelativeAzimuth()) > 0.000001;
                    final double azimuth = a;
                    if (rb1.isSelected()) {
                        if (changed) {
                            final ChangeAzimuthCommand c = new ChangeAzimuthCommand(sp);
                            SceneManager.getTaskManager().update(() -> {
                                sp.setRelativeAzimuth(azimuth);
                                sp.draw();
                                SceneManager.getInstance().refresh();
                                return null;
                            });
                            SceneManager.getInstance().getUndoManager().addEdit(c);
                        }
                        selectedScopeIndex = 0;
                    } else if (rb2.isSelected()) {
                        if (!changed) {
                            for (final SolarPanel x : foundation.getSolarPanels()) {
                                if (Math.abs(a - x.getRelativeAzimuth()) > 0.000001) {
                                    changed = true;
                                    break;
                                }
                            }
                        }
                        if (changed) {
                            final ChangeFoundationSolarPanelAzimuthCommand c = new ChangeFoundationSolarPanelAzimuthCommand(foundation);
                            SceneManager.getTaskManager().update(() -> {
                                foundation.setAzimuthForSolarPanels(azimuth);
                                return null;
                            });
                            SceneManager.getInstance().getUndoManager().addEdit(c);
                        }
                        selectedScopeIndex = 1;
                    } else if (rb3.isSelected()) {
                        if (!changed) {
                            for (final SolarPanel x : Scene.getInstance().getAllSolarPanels()) {
                                if (Math.abs(a - x.getRelativeAzimuth()) > 0.000001) {
                                    changed = true;
                                    break;
                                }
                            }
                        }
                        if (changed) {
                            final ChangeAzimuthForAllSolarPanelsCommand c = new ChangeAzimuthForAllSolarPanelsCommand();
                            SceneManager.getTaskManager().update(() -> {
                                Scene.getInstance().setAzimuthForAllSolarPanels(azimuth);
                                return null;
                            });
                            SceneManager.getInstance().getUndoManager().addEdit(c);
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