package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.FresnelReflectorAnnualAnalysis;
import org.concord.energy3d.simulation.FresnelReflectorDailyAnalysis;
import org.concord.energy3d.undo.ChangeAbsorberForAllFresnelReflectorsCommand;
import org.concord.energy3d.undo.ChangeAzimuthCommand;
import org.concord.energy3d.undo.ChangeAzimuthForAllFresnelReflectorsCommand;
import org.concord.energy3d.undo.ChangeFoundationFresnelReflectorAbsorberCommand;
import org.concord.energy3d.undo.ChangeFoundationFresnelReflectorAzimuthCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarCollectorPoleHeightCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarReflectorOpticalEfficiencyCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarReflectorReflectanceCommand;
import org.concord.energy3d.undo.ChangeFresnelReflectorAbsorberCommand;
import org.concord.energy3d.undo.ChangeOpticalEfficiencyForAllSolarReflectorsCommand;
import org.concord.energy3d.undo.ChangePoleHeightCommand;
import org.concord.energy3d.undo.ChangePoleHeightForAllSolarCollectorsCommand;
import org.concord.energy3d.undo.ChangeReflectanceForAllSolarReflectorsCommand;
import org.concord.energy3d.undo.ChangeSolarReceiverEfficiencyCommand;
import org.concord.energy3d.undo.ChangeSolarReceiverEfficiencyForAllReflectorsCommand;
import org.concord.energy3d.undo.ChangeSolarReflectorOpticalEfficiencyCommand;
import org.concord.energy3d.undo.ChangeSolarReflectorReflectanceCommand;
import org.concord.energy3d.undo.LockEditPointsCommand;
import org.concord.energy3d.undo.LockEditPointsForClassCommand;
import org.concord.energy3d.undo.LockEditPointsOnFoundationCommand;
import org.concord.energy3d.undo.SetFresnelReflectorLabelCommand;
import org.concord.energy3d.undo.SetPartSizeCommand;
import org.concord.energy3d.undo.SetSizeForAllFresnelReflectorsCommand;
import org.concord.energy3d.undo.SetSizeForFresnelReflectorsOnFoundationCommand;
import org.concord.energy3d.undo.ShowSunBeamCommand;
import org.concord.energy3d.util.Util;

class PopupMenuForFresnelReflector extends PopupMenuFactory {

    private static JPopupMenu popupMenuForFresnelReflector;

    static JPopupMenu getPopupMenu() {

        if (popupMenuForFresnelReflector == null) {

            final JMenuItem miMesh = new JMenuItem("Mesh...");
            miMesh.addActionListener(new ActionListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof FresnelReflector)) {
                        return;
                    }
                    final FresnelReflector r = (FresnelReflector) selectedPart;
                    final Foundation foundation = r.getTopContainer();
                    final String partInfo = r.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                    final JPanel gui = new JPanel(new BorderLayout());
                    final JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
                    gui.add(inputPanel, BorderLayout.CENTER);
                    inputPanel.add(new JLabel("Length direction: "));
                    final JTextField nLengthField = new JTextField("" + r.getNSectionLength());
                    inputPanel.add(nLengthField);
                    inputPanel.add(new JLabel("Width direction: "));
                    final JTextField nWidthField = new JTextField("" + r.getNSectionWidth());
                    inputPanel.add(nWidthField);
                    inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                    final JPanel scopePanel = new JPanel();
                    scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
                    scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                    final JRadioButton rb1 = new JRadioButton("Only this Fresnel Reflector", true);
                    final JRadioButton rb2 = new JRadioButton("All Fresnel Reflectors on this Foundation");
                    final JRadioButton rb3 = new JRadioButton("All Fresnel Reflectors");
                    scopePanel.add(rb1);
                    scopePanel.add(rb2);
                    scopePanel.add(rb3);
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
                    gui.add(scopePanel, BorderLayout.NORTH);

                    final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
                    final JOptionPane optionPane = new JOptionPane(new Object[]{"Set mesh for " + partInfo, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                    final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Fresnel Reflector Mesh");

                    while (true) {
                        dialog.setVisible(true);
                        final Object choice = optionPane.getValue();
                        if (choice == options[1] || choice == null) {
                            break;
                        } else {
                            int nSectionLength = 0, nSectionWidth = 0;
                            boolean ok = true;
                            try {
                                nSectionLength = Integer.parseInt(nLengthField.getText());
                                nSectionWidth = Integer.parseInt(nWidthField.getText());
                            } catch (final NumberFormatException nfe) {
                                JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
                                ok = false;
                            }
                            if (ok) {
                                if (nSectionLength < 4) {
                                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sections in the direction of length must be at least 4.", "Range Error", JOptionPane.ERROR_MESSAGE);
                                } else if (nSectionWidth < 4) {
                                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sections in the direction of width must be at least 4.", "Range Error", JOptionPane.ERROR_MESSAGE);
                                } else if (!Util.isPowerOfTwo(nSectionLength) || !Util.isPowerOfTwo(nSectionWidth)) {
                                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "Numbers of mesh sections must be power of two.", "Range Error", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    if (rb1.isSelected()) {
                                        r.setNSectionLength(nSectionLength);
                                        r.setNSectionWidth(nSectionWidth);
                                        SceneManager.getTaskManager().update(() -> {
                                            r.draw();
                                            return null;
                                        });
                                        SceneManager.getInstance().refresh();
                                        selectedScopeIndex = 0;
                                    } else if (rb2.isSelected()) {
                                        foundation.setSectionsForFresnelReflectors(nSectionLength, nSectionWidth); // call draw in Task Manager thread
                                        selectedScopeIndex = 1;
                                    } else if (rb3.isSelected()) {
                                        Scene.getInstance().setSectionsForAllFresnelReflectors(nSectionLength, nSectionWidth); // call draw in Task Manager thread
                                        selectedScopeIndex = 2;
                                    }
                                    updateAfterEdit();
                                    if (choice == options[0]) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            });

            final JCheckBoxMenuItem cbmiDisableEditPoints = new JCheckBoxMenuItem("Disable Edit Points");
            cbmiDisableEditPoints.addItemListener(new ItemListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

                @Override
                public void itemStateChanged(final ItemEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof FresnelReflector)) {
                        return;
                    }
                    final boolean disabled = cbmiDisableEditPoints.isSelected();
                    final FresnelReflector r = (FresnelReflector) selectedPart;
                    final String partInfo = r.toString().substring(0, r.toString().indexOf(')') + 1);
                    final JPanel gui = new JPanel(new BorderLayout(0, 20));
                    final JPanel panel = new JPanel();
                    gui.add(panel, BorderLayout.SOUTH);
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                    final JRadioButton rb1 = new JRadioButton("Only this Fresnel Reflector", true);
                    final JRadioButton rb2 = new JRadioButton("All Fresnel Reflectors on this Foundation");
                    final JRadioButton rb3 = new JRadioButton("All Fresnel Reflectors");
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

                    final String title = "<html>" + (disabled ? "Disable" : "Enable") + " edit points for " + partInfo + "</html>";
                    final String footnote = "<html><hr><font size=2>Disable the edit points of a Fresnel reflector prevents it<br>from being unintentionally moved.<hr></html>";
                    final Object[] options = new Object[]{"OK", "Cancel"};
                    final JOptionPane optionPane = new JOptionPane(new Object[]{title, footnote, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[0]);
                    final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), (disabled ? "Disable" : "Enable") + " Edit Points");
                    dialog.setVisible(true);
                    if (optionPane.getValue() == options[0]) {
                        if (rb1.isSelected()) {
                            final LockEditPointsCommand c = new LockEditPointsCommand(r);
                            r.setLockEdit(disabled);
                            SceneManager.getInstance().getUndoManager().addEdit(c);
                            selectedScopeIndex = 0;
                        } else if (rb2.isSelected()) {
                            final Foundation foundation = r.getTopContainer();
                            final LockEditPointsOnFoundationCommand c = new LockEditPointsOnFoundationCommand(foundation, r.getClass());
                            foundation.setLockEditForClass(disabled, r.getClass());
                            SceneManager.getInstance().getUndoManager().addEdit(c);
                            selectedScopeIndex = 1;
                        } else if (rb3.isSelected()) {
                            final LockEditPointsForClassCommand c = new LockEditPointsForClassCommand(r);
                            Scene.getInstance().setLockEditForClass(disabled, r.getClass());
                            SceneManager.getInstance().getUndoManager().addEdit(c);
                            selectedScopeIndex = 2;
                        }
                        SceneManager.getInstance().refresh();
                        Scene.getInstance().setEdited(true);
                    }
                }

            });

            final JCheckBoxMenuItem cbmiDrawBeam = new JCheckBoxMenuItem("Draw Sun Beam");
            cbmiDrawBeam.addItemListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (!(selectedPart instanceof FresnelReflector)) {
                    return;
                }
                final FresnelReflector r = (FresnelReflector) selectedPart;
                final ShowSunBeamCommand c = new ShowSunBeamCommand(r);
                r.setSunBeamVisible(cbmiDrawBeam.isSelected());
                SceneManager.getTaskManager().update(() -> {
                    r.drawSunBeam();
                    r.draw();
                    SceneManager.getInstance().refresh();
                    return null;
                });
                SceneManager.getInstance().getUndoManager().addEdit(c);
                Scene.getInstance().setEdited(true);
            });

            final JMenuItem miSetAbsorber = new JMenuItem("Set Absorber...");
            miSetAbsorber.addActionListener(new ActionListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof FresnelReflector)) {
                        return;
                    }
                    final FresnelReflector r = (FresnelReflector) selectedPart;
                    final String partInfo = r.toString().substring(0, r.toString().indexOf(')') + 1);
                    final JPanel gui = new JPanel(new BorderLayout(0, 20));
                    final JPanel panel = new JPanel();
                    gui.add(panel, BorderLayout.SOUTH);
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                    final JRadioButton rb1 = new JRadioButton("Only this Fresnel Reflector", true);
                    final JRadioButton rb2 = new JRadioButton("All Fresnel Reflectors on this Foundation");
                    final JRadioButton rb3 = new JRadioButton("All Fresnel Reflectors");
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

                    final List<Foundation> foundations = Scene.getInstance().getAllFoundations();
                    final JComboBox<String> comboBox = new JComboBox<>();
                    comboBox.addItemListener(event -> {
                        // TODO
                    });
                    comboBox.addItem("None");
                    for (final Foundation x : foundations) {
                        if (!x.getChildren().isEmpty()) {
                            comboBox.addItem(x.getId() + "");
                        }
                    }
                    if (r.getReceiver() != null) {
                        comboBox.setSelectedItem(r.getReceiver().getId() + "");
                    }
                    gui.add(comboBox, BorderLayout.CENTER);

                    final String title = "<html>Select the ID of the absorber<br>foundation for " + partInfo + "</html>";
                    final String footnote = "<html><hr><font size=2>The sunlight reflected by this Fresnel reflector will<br>focus on the top of the target, where the absorber<br>tube is located.<hr></html>";
                    final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
                    final JOptionPane optionPane = new JOptionPane(new Object[]{title, footnote, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                    final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Absorber");

                    while (true) {
                        dialog.setVisible(true);
                        final Object choice = optionPane.getValue();
                        if (choice == options[1] || choice == null) {
                            break;
                        } else {
                            Foundation absorber = null;
                            if (comboBox.getSelectedIndex() > 0) {
                                int id = -1;
                                boolean ok = true;
                                try {
                                    id = Integer.parseInt((String) comboBox.getSelectedItem());
                                } catch (final NumberFormatException exception) {
                                    JOptionPane.showMessageDialog(MainFrame.getInstance(), comboBox.getSelectedItem() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
                                    ok = false;
                                }
                                if (ok) {
                                    final HousePart p = Scene.getInstance().getPart(id);
                                    if (p instanceof Foundation) {
                                        absorber = (Foundation) p;
                                    } else {
                                        JOptionPane.showMessageDialog(MainFrame.getInstance(), "ID must be that of a foundation.", "ID Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            }
                            boolean changed = absorber != r.getReceiver();
                            final Foundation absorber2 = absorber;
                            if (rb1.isSelected()) {
                                if (changed) {
                                    final Foundation oldTarget = r.getReceiver();
                                    final ChangeFresnelReflectorAbsorberCommand c = new ChangeFresnelReflectorAbsorberCommand(r);
                                    r.setReceiver(absorber);
                                    SceneManager.getTaskManager().update(() -> {
                                        r.draw();
                                        if (oldTarget != null) {
                                            oldTarget.drawSolarReceiver();
                                        }
                                        SceneManager.getInstance().refresh();
                                        return null;
                                    });
                                    SceneManager.getInstance().getUndoManager().addEdit(c);
                                }
                                selectedScopeIndex = 0;
                            } else if (rb2.isSelected()) {
                                final Foundation foundation = r.getTopContainer();
                                if (!changed) {
                                    for (final FresnelReflector x : foundation.getFresnelReflectors()) {
                                        if (x.getReceiver() != absorber) {
                                            changed = true;
                                            break;
                                        }
                                    }
                                }
                                if (changed) {
                                    final ChangeFoundationFresnelReflectorAbsorberCommand c = new ChangeFoundationFresnelReflectorAbsorberCommand(foundation);
                                    SceneManager.getTaskManager().update(() -> {
                                        foundation.setAbsorberForFresnelReflectors(absorber2);
                                        return null;
                                    });
                                    SceneManager.getInstance().getUndoManager().addEdit(c);
                                }
                                selectedScopeIndex = 1;
                            } else if (rb3.isSelected()) {
                                if (!changed) {
                                    for (final FresnelReflector x : Scene.getInstance().getAllFresnelReflectors()) {
                                        if (x.getReceiver() != absorber) {
                                            changed = true;
                                            break;
                                        }
                                    }
                                }
                                if (changed) {
                                    final ChangeAbsorberForAllFresnelReflectorsCommand c = new ChangeAbsorberForAllFresnelReflectorsCommand();
                                    SceneManager.getTaskManager().update(() -> {
                                        Scene.getInstance().setAbsorberForAllFresnelReflectors(absorber2);
                                        return null;
                                    });
                                    SceneManager.getInstance().getUndoManager().addEdit(c);
                                }
                                selectedScopeIndex = 2;
                            }
                            if (changed) {
                                if (absorber2 != null) {
                                    SceneManager.getTaskManager().update(() -> {
                                        absorber2.drawSolarReceiver();
                                        return null;
                                    });
                                }
                                updateAfterEdit();
                            }
                            if (choice == options[0]) {
                                break;
                            }
                        }
                    }
                }
            });

            final JMenuItem miLength = new JMenuItem("Length...");
            miLength.addActionListener(new ActionListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof FresnelReflector)) {
                        return;
                    }
                    final FresnelReflector r = (FresnelReflector) selectedPart;
                    final Foundation foundation = r.getTopContainer();
                    final String partInfo = r.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                    final JPanel gui = new JPanel(new BorderLayout());
                    final JPanel inputPanel = new JPanel(new GridLayout(1, 2, 5, 5));
                    gui.add(inputPanel, BorderLayout.CENTER);
                    inputPanel.add(new JLabel("Length: "));
                    final JTextField lengthField = new JTextField(threeDecimalsFormat.format(r.getLength()));
                    inputPanel.add(lengthField);
                    inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                    final JPanel scopePanel = new JPanel();
                    scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
                    scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                    final JRadioButton rb1 = new JRadioButton("Only this Fresnel Reflector", true);
                    final JRadioButton rb2 = new JRadioButton("All Fresnel Reflectors on this Foundation");
                    final JRadioButton rb3 = new JRadioButton("All Fresnel Reflectors");
                    scopePanel.add(rb1);
                    scopePanel.add(rb2);
                    scopePanel.add(rb3);
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
                    gui.add(scopePanel, BorderLayout.NORTH);

                    final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
                    final JOptionPane optionPane = new JOptionPane(new Object[]{"Set length for " + partInfo, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                    final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Fresnel Reflector Length");

                    while (true) {
                        dialog.setVisible(true);
                        final Object choice = optionPane.getValue();
                        if (choice == options[1] || choice == null) {
                            break;
                        } else {
                            double length = 0;
                            boolean ok = true;
                            try {
                                length = Double.parseDouble(lengthField.getText());
                            } catch (final NumberFormatException x) {
                                JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
                                ok = false;
                            }
                            if (ok) {
                                if (length < 1 || length > 1000) {
                                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "Length must be between 1 and 1000 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    boolean changed = length != r.getLength();
                                    final double length2 = length;
                                    if (rb1.isSelected()) {
                                        if (changed) {
                                            final SetPartSizeCommand c = new SetPartSizeCommand(r);
                                            SceneManager.getTaskManager().update(() -> {
                                                r.setLength(length2);
                                                r.ensureFullModules(false);
                                                r.draw();
                                                SceneManager.getInstance().refresh();
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 0;
                                    } else if (rb2.isSelected()) {
                                        if (!changed) {
                                            for (final FresnelReflector x : foundation.getFresnelReflectors()) {
                                                if (x.getLength() != length) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final SetSizeForFresnelReflectorsOnFoundationCommand c = new SetSizeForFresnelReflectorsOnFoundationCommand(foundation);
                                            SceneManager.getTaskManager().update(() -> {
                                                foundation.setLengthForFresnelReflectors(length2);
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 1;
                                    } else if (rb3.isSelected()) {
                                        if (!changed) {
                                            for (final FresnelReflector x : Scene.getInstance().getAllFresnelReflectors()) {
                                                if (x.getLength() != length) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final SetSizeForAllFresnelReflectorsCommand c = new SetSizeForAllFresnelReflectorsCommand();
                                            SceneManager.getTaskManager().update(() -> {
                                                Scene.getInstance().setLengthForAllFresnelReflectors(length2);
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 2;
                                    }
                                    if (changed) {
                                        updateAfterEdit();
                                    }
                                    if (choice == options[0]) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            });

            final JMenuItem miModuleWidth = new JMenuItem("Module Width...");
            miModuleWidth.addActionListener(new ActionListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof FresnelReflector)) {
                        return;
                    }
                    final FresnelReflector r = (FresnelReflector) selectedPart;
                    final Foundation foundation = r.getTopContainer();
                    final String partInfo = r.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                    final JPanel gui = new JPanel(new BorderLayout());
                    final JPanel inputPanel = new JPanel(new GridLayout(1, 2, 5, 5));
                    gui.add(inputPanel, BorderLayout.CENTER);
                    inputPanel.add(new JLabel("Module Width: "));
                    final JTextField moduleWidthField = new JTextField(threeDecimalsFormat.format(r.getModuleWidth()));
                    inputPanel.add(moduleWidthField);
                    inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                    final JPanel scopePanel = new JPanel();
                    scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
                    scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                    final JRadioButton rb1 = new JRadioButton("Only this Fresnel Reflector", true);
                    final JRadioButton rb2 = new JRadioButton("All Fresnel Reflectors on this Foundation");
                    final JRadioButton rb3 = new JRadioButton("All Fresnel Reflectors");
                    scopePanel.add(rb1);
                    scopePanel.add(rb2);
                    scopePanel.add(rb3);
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
                    gui.add(scopePanel, BorderLayout.NORTH);

                    final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
                    final JOptionPane optionPane = new JOptionPane(new Object[]{"Set module width for " + partInfo, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                    final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Fresnel Reflector Module Width");

                    while (true) {
                        dialog.setVisible(true);
                        final Object choice = optionPane.getValue();
                        if (choice == options[1] || choice == null) {
                            break;
                        } else {
                            double moduleWidth = 0;
                            boolean ok = true;
                            try {
                                moduleWidth = Double.parseDouble(moduleWidthField.getText());
                            } catch (final NumberFormatException x) {
                                JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
                                ok = false;
                            }
                            if (ok) {
                                if (moduleWidth < 0.1 || moduleWidth > 20) {
                                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "Module width must be between 0.1 and 20 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    boolean changed = moduleWidth != r.getModuleWidth();
                                    final double moduleWidth2 = moduleWidth;
                                    if (rb1.isSelected()) {
                                        if (changed) {
                                            final SetPartSizeCommand c = new SetPartSizeCommand(r);
                                            SceneManager.getTaskManager().update(() -> {
                                                r.setModuleWidth(moduleWidth2);
                                                r.ensureFullModules(false);
                                                r.draw();
                                                SceneManager.getInstance().refresh();
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 0;
                                    } else if (rb2.isSelected()) {
                                        if (!changed) {
                                            for (final FresnelReflector x : foundation.getFresnelReflectors()) {
                                                if (x.getModuleWidth() != moduleWidth) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final SetSizeForFresnelReflectorsOnFoundationCommand c = new SetSizeForFresnelReflectorsOnFoundationCommand(foundation);
                                            SceneManager.getTaskManager().update(() -> {
                                                foundation.setModuleWidthForFresnelReflectors(moduleWidth2);
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 1;
                                    } else if (rb3.isSelected()) {
                                        if (!changed) {
                                            for (final FresnelReflector x : Scene.getInstance().getAllFresnelReflectors()) {
                                                if (x.getModuleWidth() != moduleWidth) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final SetSizeForAllFresnelReflectorsCommand c = new SetSizeForAllFresnelReflectorsCommand();
                                            SceneManager.getTaskManager().update(() -> {
                                                Scene.getInstance().setModuleWidthForAllFresnelReflectors(moduleWidth2);
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 2;
                                    }
                                    if (changed) {
                                        updateAfterEdit();
                                    }
                                    if (choice == options[0]) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            });

            final JMenuItem miModuleLength = new JMenuItem("Module Length...");
            miModuleLength.addActionListener(new ActionListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof FresnelReflector)) {
                        return;
                    }
                    final FresnelReflector r = (FresnelReflector) selectedPart;
                    final Foundation foundation = r.getTopContainer();
                    final String partInfo = r.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                    final JPanel gui = new JPanel(new BorderLayout());
                    final JPanel inputPanel = new JPanel(new GridLayout(1, 2, 5, 5));
                    gui.add(inputPanel, BorderLayout.CENTER);
                    inputPanel.add(new JLabel("Module Length: "));
                    final JTextField moduleLengthField = new JTextField(threeDecimalsFormat.format(r.getModuleLength()));
                    inputPanel.add(moduleLengthField);
                    inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                    final JPanel scopePanel = new JPanel();
                    scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
                    scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                    final JRadioButton rb1 = new JRadioButton("Only this Fresnel Reflector", true);
                    final JRadioButton rb2 = new JRadioButton("All Fresnel Reflectors on this Foundation");
                    final JRadioButton rb3 = new JRadioButton("All Fresnel Reflectors");
                    scopePanel.add(rb1);
                    scopePanel.add(rb2);
                    scopePanel.add(rb3);
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
                    gui.add(scopePanel, BorderLayout.NORTH);

                    final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
                    final JOptionPane optionPane = new JOptionPane(new Object[]{"Set module length for " + partInfo, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                    final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Fresnel Reflector Module Length");

                    while (true) {
                        dialog.setVisible(true);
                        final Object choice = optionPane.getValue();
                        if (choice == options[1] || choice == null) {
                            break;
                        } else {
                            double moduleLength = 0;
                            boolean ok = true;
                            try {
                                moduleLength = Double.parseDouble(moduleLengthField.getText());
                            } catch (final NumberFormatException x) {
                                JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
                                ok = false;
                            }
                            if (ok) {
                                if (moduleLength < 1 || moduleLength > 100) {
                                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "Module length must be between 1 and 100 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    boolean changed = moduleLength != r.getModuleLength();
                                    final double moduleLength2 = moduleLength;
                                    if (rb1.isSelected()) {
                                        if (changed) {
                                            final SetPartSizeCommand c = new SetPartSizeCommand(r);
                                            SceneManager.getTaskManager().update(() -> {
                                                r.setModuleLength(moduleLength2);
                                                r.ensureFullModules(false);
                                                r.draw();
                                                SceneManager.getInstance().refresh();
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 0;
                                    } else if (rb2.isSelected()) {
                                        if (!changed) {
                                            for (final FresnelReflector x : foundation.getFresnelReflectors()) {
                                                if (x.getModuleLength() != moduleLength) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final SetSizeForFresnelReflectorsOnFoundationCommand c = new SetSizeForFresnelReflectorsOnFoundationCommand(foundation);
                                            SceneManager.getTaskManager().update(() -> {
                                                foundation.setModuleLengthForFresnelReflectors(moduleLength2);
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 1;
                                    } else if (rb3.isSelected()) {
                                        if (!changed) {
                                            for (final FresnelReflector x : Scene.getInstance().getAllFresnelReflectors()) {
                                                if (x.getModuleLength() != moduleLength) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final SetSizeForAllFresnelReflectorsCommand c = new SetSizeForAllFresnelReflectorsCommand();
                                            SceneManager.getTaskManager().update(() -> {
                                                Scene.getInstance().setModuleLengthForAllFresnelReflectors(moduleLength2);
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 2;
                                    }
                                    if (changed) {
                                        updateAfterEdit();
                                    }
                                    if (choice == options[0]) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            });

            final JMenuItem miPoleHeight = new JMenuItem("Pole Height...");
            miPoleHeight.addActionListener(new ActionListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof FresnelReflector)) {
                        return;
                    }
                    final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                    final FresnelReflector r = (FresnelReflector) selectedPart;
                    final Foundation foundation = r.getTopContainer();
                    final String title = "<html>Pole Height (m) of " + partInfo + "</html>";
                    final String footnote = "<html><hr><font size=2></html>";
                    final JPanel gui = new JPanel(new BorderLayout());
                    final JPanel panel = new JPanel();
                    gui.add(panel, BorderLayout.CENTER);
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                    final JRadioButton rb1 = new JRadioButton("Only this Fresnel Reflector", true);
                    final JRadioButton rb2 = new JRadioButton("All Fresnel Reflectors on this Foundation");
                    final JRadioButton rb3 = new JRadioButton("All Fresnel Reflectors");
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
                    final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(r.getPoleHeight() * Scene.getInstance().getScale()));
                    gui.add(inputField, BorderLayout.SOUTH);

                    final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
                    final JOptionPane optionPane = new JOptionPane(new Object[]{title, footnote, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                    final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Fresnel Reflector Pole Height");

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
                                val = Double.parseDouble(inputField.getText()) / Scene.getInstance().getScale();
                            } catch (final NumberFormatException exception) {
                                JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
                                ok = false;
                            }
                            if (ok) {
                                if (val < 0 || val * Scene.getInstance().getScale() > 10) {
                                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "The pole height must be between 0 and 10 meters.", "Range Error", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    boolean changed = val != r.getPoleHeight();
                                    final double height = val;
                                    if (rb1.isSelected()) {
                                        if (changed) {
                                            final ChangePoleHeightCommand c = new ChangePoleHeightCommand(r);
                                            SceneManager.getTaskManager().update(() -> {
                                                r.setPoleHeight(height);
                                                r.draw();
                                                SceneManager.getInstance().refresh();
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 0;
                                    } else if (rb2.isSelected()) {
                                        if (!changed) {
                                            for (final FresnelReflector x : foundation.getFresnelReflectors()) {
                                                if (x.getPoleHeight() != val) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final ChangeFoundationSolarCollectorPoleHeightCommand c = new ChangeFoundationSolarCollectorPoleHeightCommand(foundation, r.getClass());
                                            SceneManager.getTaskManager().update(() -> {
                                                foundation.setPoleHeightForFresnelReflectors(height);
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 1;
                                    } else if (rb3.isSelected()) {
                                        if (!changed) {
                                            for (final FresnelReflector x : Scene.getInstance().getAllFresnelReflectors()) {
                                                if (x.getPoleHeight() != val) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final ChangePoleHeightForAllSolarCollectorsCommand c = new ChangePoleHeightForAllSolarCollectorsCommand(r.getClass());
                                            SceneManager.getTaskManager().update(() -> {
                                                Scene.getInstance().setPoleHeightForAllFresnelReflectors(height);
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 2;
                                    }
                                    if (changed) {
                                        updateAfterEdit();
                                    }
                                    if (choice == options[0]) {
                                        break;
                                    }
                                }
                            }
                        }
                    }

                }
            });

            final JMenuItem miAzimuth = new JMenuItem("Azimuth...");
            miAzimuth.addActionListener(new ActionListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof FresnelReflector)) {
                        return;
                    }
                    final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                    final FresnelReflector fresnel = (FresnelReflector) selectedPart;
                    final Foundation foundation = fresnel.getTopContainer();
                    final String title = "<html>Azimuth Angle (&deg;) of " + partInfo + "</html>";
                    final String footnote = "<html><hr><font size=2>The azimuth angle is measured clockwise from the true north.<hr></html>";
                    final JPanel gui = new JPanel(new BorderLayout());
                    final JPanel panel = new JPanel();
                    gui.add(panel, BorderLayout.CENTER);
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                    final JRadioButton rb1 = new JRadioButton("Only this Fresnel Reflector", true);
                    final JRadioButton rb2 = new JRadioButton("All Fresnel Reflectors on this Foundation");
                    final JRadioButton rb3 = new JRadioButton("All Fresnel Reflectors");
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
                    double a = fresnel.getRelativeAzimuth() + foundation.getAzimuth();
                    if (a > 360) {
                        a -= 360;
                    }
                    final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(a));
                    gui.add(inputField, BorderLayout.SOUTH);

                    final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
                    final JOptionPane optionPane = new JOptionPane(new Object[]{title, footnote, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                    final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Fresnel Reflector Azimuth");

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
                                boolean changed = Math.abs(a - fresnel.getRelativeAzimuth()) > 0.000001;
                                final double azimuth = a;
                                if (rb1.isSelected()) {
                                    if (changed) {
                                        final ChangeAzimuthCommand c = new ChangeAzimuthCommand(fresnel);
                                        SceneManager.getTaskManager().update(() -> {
                                            fresnel.setRelativeAzimuth(azimuth);
                                            fresnel.draw();
                                            SceneManager.getInstance().refresh();
                                            return null;
                                        });
                                        SceneManager.getInstance().getUndoManager().addEdit(c);
                                    }
                                    selectedScopeIndex = 0;
                                } else if (rb2.isSelected()) {
                                    if (!changed) {
                                        for (final FresnelReflector x : foundation.getFresnelReflectors()) {
                                            if (Math.abs(a - x.getRelativeAzimuth()) > 0.000001) {
                                                changed = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (changed) {
                                        final ChangeFoundationFresnelReflectorAzimuthCommand c = new ChangeFoundationFresnelReflectorAzimuthCommand(foundation);
                                        SceneManager.getTaskManager().update(() -> {
                                            foundation.setAzimuthForParabolicFresnelReflectors(azimuth);
                                            return null;
                                        });
                                        SceneManager.getInstance().getUndoManager().addEdit(c);
                                    }
                                    selectedScopeIndex = 1;
                                } else if (rb3.isSelected()) {
                                    if (!changed) {
                                        for (final ParabolicTrough x : Scene.getInstance().getAllParabolicTroughs()) {
                                            if (Math.abs(a - x.getRelativeAzimuth()) > 0.000001) {
                                                changed = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (changed) {
                                        final ChangeAzimuthForAllFresnelReflectorsCommand c = new ChangeAzimuthForAllFresnelReflectorsCommand();
                                        SceneManager.getTaskManager().update(() -> {
                                            Scene.getInstance().setAzimuthForAllFresnelReflectors(azimuth);
                                            return null;
                                        });
                                        SceneManager.getInstance().getUndoManager().addEdit(c);
                                    }
                                    selectedScopeIndex = 2;
                                }
                                if (changed) {
                                    updateAfterEdit();
                                }
                                if (choice == options[0]) {
                                    break;
                                }
                            }
                        }
                    }
                }
            });

            final JMenu labelMenu = new JMenu("Label");

            final JCheckBoxMenuItem miLabelNone = new JCheckBoxMenuItem("None", true);
            miLabelNone.addActionListener(e -> {
                if (miLabelNone.isSelected()) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof FresnelReflector) {
                        final FresnelReflector r = (FresnelReflector) selectedPart;
                        final SetFresnelReflectorLabelCommand c = new SetFresnelReflectorLabelCommand(r);
                        SceneManager.getTaskManager().update(() -> {
                            r.clearLabels();
                            r.draw();
                            SceneManager.getInstance().refresh();
                            return null;
                        });
                        SceneManager.getInstance().getUndoManager().addEdit(c);
                        Scene.getInstance().setEdited(true);
                    }
                }
            });
            labelMenu.add(miLabelNone);

            final JCheckBoxMenuItem miLabelCustom = new JCheckBoxMenuItem("Custom");
            miLabelCustom.addActionListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof FresnelReflector) {
                    final FresnelReflector r = (FresnelReflector) selectedPart;
                    final SetFresnelReflectorLabelCommand c = new SetFresnelReflectorLabelCommand(r);
                    r.setLabelCustom(miLabelCustom.isSelected());
                    if (r.getLabelCustom()) {
                        r.setLabelCustomText(JOptionPane.showInputDialog(MainFrame.getInstance(), "Custom Text", r.getLabelCustomText()));
                    }
                    SceneManager.getTaskManager().update(() -> {
                        r.draw();
                        SceneManager.getInstance().refresh();
                        return null;
                    });
                    SceneManager.getInstance().getUndoManager().addEdit(c);
                    Scene.getInstance().setEdited(true);
                }
            });
            labelMenu.add(miLabelCustom);

            final JCheckBoxMenuItem miLabelId = new JCheckBoxMenuItem("ID");
            miLabelId.addActionListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof FresnelReflector) {
                    final FresnelReflector r = (FresnelReflector) selectedPart;
                    final SetFresnelReflectorLabelCommand c = new SetFresnelReflectorLabelCommand(r);
                    r.setLabelId(miLabelId.isSelected());
                    SceneManager.getTaskManager().update(() -> {
                        r.draw();
                        SceneManager.getInstance().refresh();
                        return null;
                    });
                    SceneManager.getInstance().getUndoManager().addEdit(c);
                    Scene.getInstance().setEdited(true);
                }
            });
            labelMenu.add(miLabelId);

            final JCheckBoxMenuItem miLabelEnergyOutput = new JCheckBoxMenuItem("Energy Output");
            miLabelEnergyOutput.addActionListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof FresnelReflector) {
                    final FresnelReflector r = (FresnelReflector) selectedPart;
                    final SetFresnelReflectorLabelCommand c = new SetFresnelReflectorLabelCommand(r);
                    r.setLabelEnergyOutput(miLabelEnergyOutput.isSelected());
                    SceneManager.getTaskManager().update(() -> {
                        r.draw();
                        SceneManager.getInstance().refresh();
                        return null;
                    });
                    SceneManager.getInstance().getUndoManager().addEdit(c);
                    Scene.getInstance().setEdited(true);
                }
            });
            labelMenu.add(miLabelEnergyOutput);

            popupMenuForFresnelReflector = createPopupMenu(true, true, () -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (!(selectedPart instanceof FresnelReflector)) {
                    return;
                }
                final FresnelReflector r = (FresnelReflector) selectedPart;
                Util.selectSilently(cbmiDisableEditPoints, r.getLockEdit());
                Util.selectSilently(cbmiDrawBeam, r.isSunBeamVisible());
                Util.selectSilently(miLabelNone, !r.isLabelVisible());
                Util.selectSilently(miLabelCustom, r.getLabelCustom());
                Util.selectSilently(miLabelId, r.getLabelId());
                Util.selectSilently(miLabelEnergyOutput, r.getLabelEnergyOutput());
            });

            final JMenuItem miReflectance = new JMenuItem("Reflectance...");
            miReflectance.addActionListener(new ActionListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof FresnelReflector)) {
                        return;
                    }
                    final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                    final FresnelReflector r = (FresnelReflector) selectedPart;
                    final String title = "<html>Reflectance (%) of " + partInfo + "</html>";
                    final String footnote = "<html><hr><font size=2>Reflectance can be affected by pollen and dust.<hr></html>";
                    final JPanel gui = new JPanel(new BorderLayout());
                    final JPanel panel = new JPanel();
                    gui.add(panel, BorderLayout.CENTER);
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                    final JRadioButton rb1 = new JRadioButton("Only this Fresnel Reflector", true);
                    final JRadioButton rb2 = new JRadioButton("All Fresnel Reflectors on this Foundation");
                    final JRadioButton rb3 = new JRadioButton("All Fresnel Reflectors");
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
                    final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(r.getReflectance() * 100));
                    gui.add(inputField, BorderLayout.SOUTH);

                    final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
                    final JOptionPane optionPane = new JOptionPane(new Object[]{title, footnote, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                    final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Fresnel Reflector Reflectance");

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
                                if (val < 50 || val > 99) {
                                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "Fresnel reflector reflectance must be between 50% and 99%.", "Range Error", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    boolean changed = Math.abs(val * 0.01 - r.getReflectance()) > 0.000001;
                                    if (rb1.isSelected()) {
                                        if (changed) {
                                            final ChangeSolarReflectorReflectanceCommand c = new ChangeSolarReflectorReflectanceCommand(r);
                                            r.setReflectance(val * 0.01);
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 0;
                                    } else if (rb2.isSelected()) {
                                        final Foundation foundation = r.getTopContainer();
                                        if (!changed) {
                                            for (final FresnelReflector x : foundation.getFresnelReflectors()) {
                                                if (Math.abs(x.getReflectance() - val * 0.01) > 0.000001) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final ChangeFoundationSolarReflectorReflectanceCommand c = new ChangeFoundationSolarReflectorReflectanceCommand(foundation, r.getClass());
                                            foundation.setReflectanceForSolarReflectors(val * 0.01, r.getClass());
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 1;
                                    } else if (rb3.isSelected()) {
                                        if (!changed) {
                                            for (final FresnelReflector x : Scene.getInstance().getAllFresnelReflectors()) {
                                                if (Math.abs(x.getReflectance() - val * 0.01) > 0.000001) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final ChangeReflectanceForAllSolarReflectorsCommand c = new ChangeReflectanceForAllSolarReflectorsCommand(r.getClass());
                                            Scene.getInstance().setReflectanceForAllSolarReflectors(val * 0.01, r.getClass());
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 2;
                                    }
                                    if (changed) {
                                        updateAfterEdit();
                                    }
                                    if (choice == options[0]) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            });

            final JMenuItem miApertureRatio = new JMenuItem("Aperture Ratio...");
            miApertureRatio.addActionListener(new ActionListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof FresnelReflector)) {
                        return;
                    }
                    final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                    final FresnelReflector r = (FresnelReflector) selectedPart;
                    final String title = "<html>Aperture percentage of " + partInfo + "</html>";
                    final String footnote = "<html><hr><font size=2>The percentage of the effective area for reflection<br>after deducting the area of gaps, frames, etc.<hr></html>";
                    final JPanel gui = new JPanel(new BorderLayout());
                    final JPanel panel = new JPanel();
                    gui.add(panel, BorderLayout.CENTER);
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                    final JRadioButton rb1 = new JRadioButton("Only this Fresnel Reflector", true);
                    final JRadioButton rb2 = new JRadioButton("All Fresnel Reflectors on this Foundation");
                    final JRadioButton rb3 = new JRadioButton("All Fresnel Reflectors");
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
                    final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(r.getOpticalEfficiency() * 100));
                    gui.add(inputField, BorderLayout.SOUTH);

                    final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
                    final JOptionPane optionPane = new JOptionPane(new Object[]{title, footnote, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                    final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Aperture Percentage of Fresnel Reflector Surface");

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
                                if (val < 70 || val > 100) {
                                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "Fresnel reflector aperature percentage must be between 70% and 100%.", "Range Error", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    boolean changed = Math.abs(val * 0.01 - r.getOpticalEfficiency()) > 0.000001;
                                    if (rb1.isSelected()) {
                                        if (changed) {
                                            final ChangeSolarReflectorOpticalEfficiencyCommand c = new ChangeSolarReflectorOpticalEfficiencyCommand(r);
                                            r.setOpticalEfficiency(val * 0.01);
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 0;
                                    } else if (rb2.isSelected()) {
                                        final Foundation foundation = r.getTopContainer();
                                        if (!changed) {
                                            for (final FresnelReflector x : foundation.getFresnelReflectors()) {
                                                if (Math.abs(val * 0.01 - x.getOpticalEfficiency()) > 0.000001) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final ChangeFoundationSolarReflectorOpticalEfficiencyCommand c = new ChangeFoundationSolarReflectorOpticalEfficiencyCommand(foundation, r.getClass());
                                            foundation.setOpticalEfficiencyForSolarReflectors(val * 0.01, r.getClass());
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 1;
                                    } else if (rb3.isSelected()) {
                                        if (!changed) {
                                            for (final FresnelReflector x : Scene.getInstance().getAllFresnelReflectors()) {
                                                if (Math.abs(val * 0.01 - x.getOpticalEfficiency()) > 0.000001) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final ChangeOpticalEfficiencyForAllSolarReflectorsCommand c = new ChangeOpticalEfficiencyForAllSolarReflectorsCommand(r.getClass());
                                            Scene.getInstance().setOpticalEfficiencyForAllSolarReflectors(val * 0.01, r.getClass());
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 2;
                                    }
                                    if (changed) {
                                        updateAfterEdit();
                                    }
                                    if (choice == options[0]) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            });

            final JMenuItem miConversionEfficiency = new JMenuItem("Absorber Conversion Efficiency...");
            miConversionEfficiency.addActionListener(new ActionListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof FresnelReflector)) {
                        return;
                    }
                    final FresnelReflector r = (FresnelReflector) selectedPart;
                    final Foundation absorber = r.getReceiver();
                    if (absorber == null) {
                        JOptionPane.showMessageDialog(MainFrame.getInstance(), "This reflector does not link to an absorber.", "No Absorber", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                    final String title = "<html>Light-electricity conversion efficiency (%) of " + partInfo + "'s absorber</html>";
                    final String footnote = "<html><hr><font size=2><hr></html>";
                    final JPanel gui = new JPanel(new BorderLayout());
                    final JPanel panel = new JPanel();
                    gui.add(panel, BorderLayout.CENTER);
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                    final JRadioButton rb1 = new JRadioButton("Only this Fresnel Reflector's Absorber", true);
                    final JRadioButton rb2 = new JRadioButton("All Fresnel Reflector Absorbers");
                    panel.add(rb1);
                    panel.add(rb2);
                    final ButtonGroup bg = new ButtonGroup();
                    bg.add(rb1);
                    bg.add(rb2);
                    switch (selectedScopeIndex) {
                        case 0:
                            rb1.setSelected(true);
                            break;
                        case 1:
                            rb2.setSelected(true);
                            break;
                    }
                    final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(absorber.getSolarReceiverEfficiency() * 100));
                    gui.add(inputField, BorderLayout.SOUTH);

                    final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
                    final JOptionPane optionPane = new JOptionPane(new Object[]{title, footnote, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                    final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Receiver Conversion Efficiency");

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
                                if (val < 5 || val > 50) {
                                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "Light-electricity conversion efficiency must be between 5% and 50%.", "Range Error", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    boolean changed = Math.abs(val * 0.01 - absorber.getSolarReceiverEfficiency()) > 0.000001;
                                    if (rb1.isSelected()) {
                                        if (changed) {
                                            final ChangeSolarReceiverEfficiencyCommand c = new ChangeSolarReceiverEfficiencyCommand(absorber);
                                            absorber.setSolarReceiverEfficiency(val * 0.01);
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 0;
                                    } else if (rb2.isSelected()) {
                                        if (!changed) {
                                            for (final FresnelReflector x : Scene.getInstance().getAllFresnelReflectors()) {
                                                if (Math.abs(val * 0.01 - x.getReceiver().getSolarReceiverEfficiency()) > 0.000001) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final ChangeSolarReceiverEfficiencyForAllReflectorsCommand c = new ChangeSolarReceiverEfficiencyForAllReflectorsCommand(r.getClass());
                                            Scene.getInstance().setSolarReceiverEfficiencyForAllSolarReflectors(val * 0.01, r.getClass());
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 1;
                                    }
                                    if (changed) {
                                        updateAfterEdit();
                                    }
                                    if (choice == options[0]) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            });

            popupMenuForFresnelReflector.addSeparator();
            popupMenuForFresnelReflector.add(miSetAbsorber);
            popupMenuForFresnelReflector.addSeparator();
            popupMenuForFresnelReflector.add(cbmiDisableEditPoints);
            popupMenuForFresnelReflector.add(cbmiDrawBeam);
            popupMenuForFresnelReflector.add(labelMenu);
            popupMenuForFresnelReflector.addSeparator();
            popupMenuForFresnelReflector.add(miLength);
            popupMenuForFresnelReflector.add(miModuleWidth);
            popupMenuForFresnelReflector.add(miModuleLength);
            popupMenuForFresnelReflector.add(miPoleHeight);
            popupMenuForFresnelReflector.add(miAzimuth);
            popupMenuForFresnelReflector.addSeparator();
            popupMenuForFresnelReflector.add(miReflectance);
            popupMenuForFresnelReflector.add(miApertureRatio);
            popupMenuForFresnelReflector.add(miConversionEfficiency);
            popupMenuForFresnelReflector.addSeparator();
            popupMenuForFresnelReflector.add(miMesh);
            popupMenuForFresnelReflector.addSeparator();

            JMenuItem mi = new JMenuItem("Daily Yield Analysis...");
            mi.addActionListener(e -> {
                if (EnergyPanel.getInstance().adjustCellSize()) {
                    return;
                }
                if (SceneManager.getInstance().getSelectedPart() instanceof FresnelReflector) {
                    new FresnelReflectorDailyAnalysis().show();
                }
            });
            popupMenuForFresnelReflector.add(mi);

            mi = new JMenuItem("Annual Yield Analysis...");
            mi.addActionListener(e -> {
                if (EnergyPanel.getInstance().adjustCellSize()) {
                    return;
                }
                if (SceneManager.getInstance().getSelectedPart() instanceof FresnelReflector) {
                    new FresnelReflectorAnnualAnalysis().show();
                }
            });
            popupMenuForFresnelReflector.add(mi);

        }

        return popupMenuForFresnelReflector;

    }

}