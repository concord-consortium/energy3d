package org.concord.energy3d.gui;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.model.*;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.EnergyAnnualAnalysis;
import org.concord.energy3d.simulation.EnergyDailyAnalysis;
import org.concord.energy3d.undo.*;
import org.concord.energy3d.util.Util;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;

class PopupMenuForWindow extends PopupMenuFactory {

    private static JPopupMenu popupMenuForWindow;

    static JPopupMenu getPopupMenu() {

        if (popupMenuForWindow == null) {

            final JMenu muntinMenu = new JMenu("Muntins");
            final JMenu shutterMenu = new JMenu("Shutters");

            popupMenuForWindow = createPopupMenu(true, true, () -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Window) {
                    shutterMenu.setEnabled(selectedPart.getContainer() instanceof Wall);
                }
            });

            final JCheckBoxMenuItem cbmiHorizontalBars = new JCheckBoxMenuItem("Horizontal Bars");
            cbmiHorizontalBars.addItemListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Window) {
                    final Window w = (Window) selectedPart;
                    w.setHorizontalBars(cbmiHorizontalBars.isSelected());
                    SceneManager.getTaskManager().update(() -> {
                        w.draw();
                        SceneManager.getInstance().refresh();
                        return null;
                    });
                    Scene.getInstance().setEdited(true);
                }
            });
            muntinMenu.add(cbmiHorizontalBars);

            final JCheckBoxMenuItem cbmiVerticalBars = new JCheckBoxMenuItem("Vertical Bars");
            cbmiVerticalBars.addItemListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Window) {
                    final Window w = (Window) selectedPart;
                    w.setVerticalBars(cbmiVerticalBars.isSelected());
                    SceneManager.getTaskManager().update(() -> {
                        w.draw();
                        SceneManager.getInstance().refresh();
                        return null;
                    });
                    Scene.getInstance().setEdited(true);
                }
            });
            muntinMenu.add(cbmiVerticalBars);
            muntinMenu.addSeparator();

            final ButtonGroup muntinButtonGroup = new ButtonGroup();

            final JRadioButtonMenuItem miMoreBars = new JRadioButtonMenuItem("More Bars");
            miMoreBars.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof Window) {
                        final Window w = (Window) selectedPart;
                        SceneManager.getTaskManager().update(() -> {
                            w.setStyle(Window.MORE_MUNTIN_BARS);
                            w.draw();
                            SceneManager.getInstance().refresh();
                            return null;
                        });
                        Scene.getInstance().setEdited(true);
                    }
                }
            });
            muntinButtonGroup.add(miMoreBars);
            muntinMenu.add(miMoreBars);

            final JRadioButtonMenuItem miMediumBars = new JRadioButtonMenuItem("Medium Bars");
            miMediumBars.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof Window) {
                        final Window w = (Window) selectedPart;
                        SceneManager.getTaskManager().update(() -> {
                            w.setStyle(Window.MEDIUM_MUNTIN_BARS);
                            w.draw();
                            SceneManager.getInstance().refresh();
                            return null;
                        });
                        Scene.getInstance().setEdited(true);
                    }
                }
            });
            muntinButtonGroup.add(miMediumBars);
            muntinMenu.add(miMediumBars);

            final JRadioButtonMenuItem miLessBars = new JRadioButtonMenuItem("Less Bars");
            miLessBars.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof Window) {
                        final Window w = (Window) selectedPart;
                        SceneManager.getTaskManager().update(() -> {
                            w.setStyle(Window.LESS_MUNTIN_BARS);
                            w.draw();
                            SceneManager.getInstance().refresh();
                            return null;
                        });
                        Scene.getInstance().setEdited(true);
                    }
                }
            });
            muntinButtonGroup.add(miLessBars);
            muntinMenu.add(miLessBars);
            muntinMenu.addSeparator();

            final JMenuItem miMuntinColor = new JMenuItem("Color...");
            miMuntinColor.addActionListener(new ActionListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof Window)) {
                        return;
                    }
                    final Window window = (Window) selectedPart;
                    final JColorChooser colorChooser = MainFrame.getInstance().getColorChooser();
                    final ReadOnlyColorRGBA color = window.getMuntinColor();
                    if (color != null) {
                        colorChooser.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
                    }
                    final ActionListener actionListener = e12 -> {
                        final Color c = colorChooser.getColor();
                        final float[] newColor = c.getComponents(null);
                        final ColorRGBA color12 = new ColorRGBA(newColor[0], newColor[1], newColor[2], 1);
                        final JPanel panel = new JPanel();
                        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                        panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                        final JRadioButton rb1 = new JRadioButton("Only this Window", true);
                        final JRadioButton rb2 = new JRadioButton("All Windows on this " + (window.getContainer() instanceof Wall ? "Wall" : "Roof"));
                        final JRadioButton rb3 = new JRadioButton("All Windows of this Building");
                        final JRadioButton rb4 = new JRadioButton("All Windows");
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

                        final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
                        final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                        final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Muntin Color");

                        while (true) {
                            dialog.setVisible(true);
                            final Object choice = optionPane.getValue();
                            if (choice == options[1] || choice == null) {
                                break;
                            } else {
                                boolean changed = !color12.equals(window.getMuntinColor());
                                if (rb1.isSelected()) { // apply to only this window
                                    if (changed) {
                                        final ChangeMuntinColorCommand cmd = new ChangeMuntinColorCommand(window);
                                        SceneManager.getTaskManager().update(() -> {
                                            window.setMuntinColor(color12);
                                            window.draw();
                                            return null;
                                        });
                                        SceneManager.getInstance().getUndoManager().addEdit(cmd);
                                    }
                                    selectedScopeIndex = 0;
                                } else if (rb2.isSelected()) {
                                    if (!changed) {
                                        if (window.getContainer() instanceof Wall) {
                                            final Wall wall = (Wall) window.getContainer();
                                            for (final Window x : wall.getWindows()) {
                                                if (!color12.equals(x.getMuntinColor())) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        } else if (window.getContainer() instanceof Roof) {
                                            final Roof roof = (Roof) window.getContainer();
                                            for (final Window x : roof.getWindows()) {
                                                if (!color12.equals(x.getMuntinColor())) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (changed) {
                                        final ChangeContainerMuntinColorCommand cmd = new ChangeContainerMuntinColorCommand(window.getContainer());
                                        SceneManager.getTaskManager().update(() -> {
                                            Scene.getInstance().setWindowColorInContainer(window.getContainer(), color12, "muntin");
                                            return null;
                                        });
                                        SceneManager.getInstance().getUndoManager().addEdit(cmd);
                                    }
                                    selectedScopeIndex = 1;
                                } else if (rb3.isSelected()) {
                                    final Foundation foundation = window.getTopContainer();
                                    if (!changed) {
                                        for (final Window x : foundation.getWindows()) {
                                            if (!color12.equals(x.getMuntinColor())) {
                                                changed = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (changed) {
                                        final ChangeBuildingMuntinColorCommand cmd = new ChangeBuildingMuntinColorCommand(window);
                                        SceneManager.getTaskManager().update(() -> {
                                            Scene.getInstance().setMuntinColorOfBuilding(window, color12);
                                            return null;
                                        });
                                        SceneManager.getInstance().getUndoManager().addEdit(cmd);
                                    }
                                    selectedScopeIndex = 2;
                                } else {
                                    if (!changed) {
                                        for (final Window x : Scene.getInstance().getAllWindows()) {
                                            if (!color12.equals(x.getMuntinColor())) {
                                                changed = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (changed) {
                                        final ChangeMuntinColorForAllWindowsCommand cmd = new ChangeMuntinColorForAllWindowsCommand();
                                        SceneManager.getTaskManager().update(() -> {
                                            Scene.getInstance().setMuntinColorForAllWindows(color12);
                                            return null;
                                        });
                                        SceneManager.getInstance().getUndoManager().addEdit(cmd);
                                    }
                                    selectedScopeIndex = 3;
                                }
                                if (changed) {
                                    Scene.getInstance().setEdited(true);
                                    SceneManager.getInstance().refresh();
                                }
                                if (choice == options[0]) {
                                    break;
                                }
                            }
                        }
                    };
                    JColorChooser.createDialog(MainFrame.getInstance(), "Select Muntin Color", true, colorChooser, actionListener, null).setVisible(true);
                }
            });
            muntinMenu.add(miMuntinColor);

            muntinMenu.addMenuListener(new MenuListener() {

                @Override
                public void menuSelected(final MenuEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof Window) {
                        final Window window = (Window) selectedPart;
                        switch (window.getStyle()) {
                            case Window.MORE_MUNTIN_BARS:
                                Util.selectSilently(miMoreBars, true);
                                break;
                            case Window.MEDIUM_MUNTIN_BARS:
                                Util.selectSilently(miMediumBars, true);
                                break;
                            case Window.LESS_MUNTIN_BARS:
                                Util.selectSilently(miLessBars, true);
                                break;
                        }
                        // NO_MUNTIN_BAR backward compatibility
                        Util.selectSilently(cbmiHorizontalBars, window.getStyle() != Window.NO_MUNTIN_BAR && window.getHorizontalBars());
                        Util.selectSilently(cbmiVerticalBars, window.getStyle() != Window.NO_MUNTIN_BAR && window.getVerticalBars());
                    }
                }

                @Override
                public void menuDeselected(final MenuEvent e) {
                    muntinMenu.setEnabled(true);
                }

                @Override
                public void menuCanceled(final MenuEvent e) {
                    muntinMenu.setEnabled(true);
                }

            });

            final JCheckBoxMenuItem cbmiBothShutters = new JCheckBoxMenuItem("Both Shutters");
            final JCheckBoxMenuItem cbmiLeftShutter = new JCheckBoxMenuItem("Left Shutter");
            final JCheckBoxMenuItem cbmiRightShutter = new JCheckBoxMenuItem("Right Shutter");

            cbmiLeftShutter.addItemListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Window) {
                    final Window w = (Window) selectedPart;
                    final ChangeWindowShuttersCommand c = new ChangeWindowShuttersCommand(w);
                    SceneManager.getTaskManager().update(() -> {
                        w.setLeftShutter(cbmiLeftShutter.isSelected());
                        w.draw();
                        return null;
                    });
                    Scene.getInstance().setEdited(true);
                    SceneManager.getInstance().getUndoManager().addEdit(c);
                }
            });
            shutterMenu.add(cbmiLeftShutter);

            cbmiRightShutter.addItemListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Window) {
                    final Window w = (Window) selectedPart;
                    final ChangeWindowShuttersCommand c = new ChangeWindowShuttersCommand(w);
                    SceneManager.getTaskManager().update(() -> {
                        w.setRightShutter(cbmiRightShutter.isSelected());
                        w.draw();
                        return null;
                    });
                    Scene.getInstance().setEdited(true);
                    SceneManager.getInstance().getUndoManager().addEdit(c);
                }
            });
            shutterMenu.add(cbmiRightShutter);

            cbmiBothShutters.addItemListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Window) {
                    final Window w = (Window) selectedPart;
                    final ChangeWindowShuttersCommand c = new ChangeWindowShuttersCommand(w);
                    SceneManager.getTaskManager().update(() -> {
                        w.setLeftShutter(cbmiBothShutters.isSelected());
                        w.setRightShutter(cbmiBothShutters.isSelected());
                        w.draw();
                        return null;
                    });
                    Scene.getInstance().setEdited(true);
                    SceneManager.getInstance().getUndoManager().addEdit(c);
                }
            });
            shutterMenu.add(cbmiBothShutters);
            shutterMenu.addSeparator();

            final JMenuItem miShutterColor = new JMenuItem("Color...");
            miShutterColor.addActionListener(new ActionListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof Window)) {
                        return;
                    }
                    final Window window = (Window) selectedPart;
                    final JColorChooser colorChooser = MainFrame.getInstance().getColorChooser();
                    final ReadOnlyColorRGBA color = window.getShutterColor();
                    if (color != null) {
                        colorChooser.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
                    }
                    final ActionListener actionListener = e13 -> {
                        final Color c = colorChooser.getColor();
                        final float[] newColor = c.getComponents(null);
                        final ColorRGBA color13 = new ColorRGBA(newColor[0], newColor[1], newColor[2], 1);
                        final JPanel panel = new JPanel();
                        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                        panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                        final JRadioButton rb1 = new JRadioButton("Only this Window", true);
                        final JRadioButton rb2 = new JRadioButton("All Windows on this " + (window.getContainer() instanceof Wall ? "Wall" : "Roof"));
                        final JRadioButton rb3 = new JRadioButton("All Windows of this Building");
                        final JRadioButton rb4 = new JRadioButton("All Windows");
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

                        final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
                        final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                        final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Shutter Color");

                        while (true) {
                            dialog.setVisible(true);
                            final Object choice = optionPane.getValue();
                            if (choice == options[1] || choice == null) {
                                break;
                            } else {
                                boolean changed = !color13.equals(window.getShutterColor());
                                if (rb1.isSelected()) { // apply to only this window
                                    if (changed) {
                                        final ChangeShutterColorCommand cmd = new ChangeShutterColorCommand(window);
                                        SceneManager.getTaskManager().update(() -> {
                                            window.setShutterColor(color13);
                                            window.draw();
                                            return null;
                                        });
                                        SceneManager.getInstance().getUndoManager().addEdit(cmd);
                                    }
                                    selectedScopeIndex = 0;
                                } else if (rb2.isSelected()) {
                                    if (!changed) {
                                        if (window.getContainer() instanceof Wall) {
                                            final Wall wall = (Wall) window.getContainer();
                                            for (final Window x : wall.getWindows()) {
                                                if (!color13.equals(x.getShutterColor())) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        } else if (window.getContainer() instanceof Roof) {
                                            final Roof roof = (Roof) window.getContainer();
                                            for (final Window x : roof.getWindows()) {
                                                if (!color13.equals(x.getShutterColor())) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (changed) {
                                        final ChangeContainerShutterColorCommand cmd = new ChangeContainerShutterColorCommand(window.getContainer());
                                        SceneManager.getTaskManager().update(() -> {
                                            Scene.getInstance().setWindowColorInContainer(window.getContainer(), color13, "shutter");
                                            return null;
                                        });
                                        SceneManager.getInstance().getUndoManager().addEdit(cmd);
                                    }
                                    selectedScopeIndex = 1;
                                } else if (rb3.isSelected()) {
                                    final Foundation foundation = window.getTopContainer();
                                    if (!changed) {
                                        for (final Window x : foundation.getWindows()) {
                                            if (!color13.equals(x.getShutterColor())) {
                                                changed = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (changed) {
                                        final ChangeBuildingShutterColorCommand cmd = new ChangeBuildingShutterColorCommand(window);
                                        SceneManager.getTaskManager().update(() -> {
                                            Scene.getInstance().setShutterColorOfBuilding(window, color13);
                                            return null;
                                        });
                                        SceneManager.getInstance().getUndoManager().addEdit(cmd);
                                    }
                                    selectedScopeIndex = 2;
                                } else {
                                    if (!changed) {
                                        for (final Window x : Scene.getInstance().getAllWindows()) {
                                            if (!color13.equals(x.getShutterColor())) {
                                                changed = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (changed) {
                                        final ChangeShutterColorForAllWindowsCommand cmd = new ChangeShutterColorForAllWindowsCommand();
                                        SceneManager.getTaskManager().update(() -> {
                                            Scene.getInstance().setShutterColorForAllWindows(color13);
                                            return null;
                                        });
                                        SceneManager.getInstance().getUndoManager().addEdit(cmd);
                                    }
                                    selectedScopeIndex = 3;
                                }
                                if (changed) {
                                    Scene.getInstance().setEdited(true);
                                    SceneManager.getInstance().refresh();
                                }
                                if (choice == options[0]) {
                                    break;
                                }
                            }
                        }
                    };
                    JColorChooser.createDialog(MainFrame.getInstance(), "Select Shutter Color", true, colorChooser, actionListener, null).setVisible(true);
                }
            });
            shutterMenu.add(miShutterColor);

            final JMenuItem miShutterLength = new JMenuItem("Relative Length...");
            miShutterLength.addActionListener(new ActionListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof Window)) {
                        return;
                    }
                    final Window window = (Window) selectedPart;
                    final String partInfo = window.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                    final String title = "<html>Relative Length of Shutter for " + partInfo + "</html>";
                    final String footnote = "<html><hr width=400></html>";
                    final JPanel gui = new JPanel(new BorderLayout());
                    final JPanel panel = new JPanel();
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                    final JRadioButton rb1 = new JRadioButton("Only this Window Shutter", true);
                    final JRadioButton rb2 = new JRadioButton("All Window Shutters on this Wall");
                    final JRadioButton rb3 = new JRadioButton("All Window Shutters of this Building");
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
                    final JTextField inputField = new JTextField(window.getShutterLength() + "");
                    gui.add(inputField, BorderLayout.SOUTH);

                    final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
                    final JOptionPane optionPane = new JOptionPane(new Object[]{title, footnote, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                    final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Shutter Length (Relative)");

                    while (true) {
                        dialog.setVisible(true);
                        inputField.selectAll();
                        inputField.requestFocusInWindow();
                        final Object choice = optionPane.getValue();
                        if (choice == options[1] || choice == null) {
                            break;
                        } else {
                            boolean ok = true;
                            double val = 0;
                            try {
                                val = Double.parseDouble(inputField.getText());
                            } catch (final NumberFormatException exception) {
                                JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
                                ok = false;
                            }
                            if (ok) {
                                if (val <= 0 || val > 1) {
                                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "Relative shutter length must be within (0, 1).", "Error", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    boolean changed = Math.abs(val - window.getShutterLength()) > 0.000001;
                                    final double val2 = val;
                                    if (rb1.isSelected()) {
                                        if (changed) {
                                            final ChangeShutterLengthCommand c = new ChangeShutterLengthCommand(window);
                                            SceneManager.getTaskManager().update(() -> {
                                                window.setShutterLength(val2);
                                                window.draw();
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 0;
                                    } else if (rb2.isSelected()) {
                                        if (!changed) {
                                            if (window.getContainer() instanceof Wall) {
                                                final Wall wall = (Wall) window.getContainer();
                                                for (final Window x : wall.getWindows()) {
                                                    if (Math.abs(val - x.getShutterLength()) > 0.000001) {
                                                        changed = true;
                                                        break;
                                                    }
                                                }
                                            } else if (window.getContainer() instanceof Roof) {
                                                final Roof roof = (Roof) window.getContainer();
                                                for (final Window x : roof.getWindows()) {
                                                    if (Math.abs(val - x.getShutterLength()) > 0.000001) {
                                                        changed = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        if (changed) {
                                            SceneManager.getTaskManager().update(() -> {
                                                Scene.getInstance().setShutterLengthInContainer(window.getContainer(), val2);
                                                return null;
                                            });
                                        }
                                        selectedScopeIndex = 1;
                                    } else if (rb3.isSelected()) {
                                        final Foundation foundation = window.getTopContainer();
                                        if (!changed) {
                                            for (final Window x : foundation.getWindows()) {
                                                if (Math.abs(val - x.getShutterLength()) > 0.000001) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            SceneManager.getTaskManager().update(() -> {
                                                Scene.getInstance().setShutterLengthOfBuilding(window, val2);
                                                return null;
                                            });
                                        }
                                        selectedScopeIndex = 2;
                                    }
                                    if (changed) {
                                        SceneManager.getInstance().refresh();
                                        Scene.getInstance().setEdited(true);
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
            shutterMenu.add(miShutterLength);

            shutterMenu.addMenuListener(new MenuListener() {

                @Override
                public void menuSelected(final MenuEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof Window) {
                        final Window window = (Window) selectedPart;
                        Util.selectSilently(cbmiLeftShutter, window.getLeftShutter());
                        Util.selectSilently(cbmiRightShutter, window.getRightShutter());
                        Util.selectSilently(cbmiBothShutters, window.getLeftShutter() && window.getRightShutter());
                    }
                }

                @Override
                public void menuDeselected(final MenuEvent e) {
                    shutterMenu.setEnabled(true);
                }

                @Override
                public void menuCanceled(final MenuEvent e) {
                    shutterMenu.setEnabled(true);
                }

            });

            final JMenuItem miShgc = new JMenuItem("Solar Heat Gain Coefficient...");
            miShgc.addActionListener(new ActionListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof Window)) {
                        return;
                    }
                    final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                    final Window window = (Window) selectedPart;
                    final String title = "<html>Solar Heat Gain Coefficient of " + partInfo + "</html>";
                    final String footnote = "<html><hr><font size=2>Examples:<br><table><tr><td><font size=2>Single glass (clear)</td><td><font size=2>0.66</td></tr><tr><td><font size=2>Single glass (green tint)</td><td><font size=2>0.55</td></tr><tr><td><font size=2>Triple glass (air spaces)</td><td><font size=2>0.39</td></tr></table><hr></html>";
                    final JPanel gui = new JPanel(new BorderLayout());
                    final JPanel panel = new JPanel();
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                    final JRadioButton rb1 = new JRadioButton("Only this Window", true);
                    final JRadioButton rb2 = new JRadioButton("All Windows on this " + (selectedPart.getContainer() instanceof Wall ? "Wall" : "Roof"));
                    final JRadioButton rb3 = new JRadioButton("All Windows of this Building");
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
                    final JTextField inputField = new JTextField(window.getSolarHeatGainCoefficient() + "");
                    gui.add(inputField, BorderLayout.SOUTH);

                    final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
                    final JOptionPane optionPane = new JOptionPane(new Object[]{title, footnote, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                    final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Heat Gain Coefficient (SHGC)");

                    while (true) {
                        dialog.setVisible(true);
                        inputField.selectAll();
                        inputField.requestFocusInWindow();
                        final Object choice = optionPane.getValue();
                        if (choice == options[1] || choice == null) {
                            break;
                        } else {
                            boolean ok = true;
                            double val = 0;
                            try {
                                val = Double.parseDouble(inputField.getText());
                            } catch (final NumberFormatException exception) {
                                JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
                                ok = false;
                            }
                            if (ok) {
                                if (val < 0 || val > 1) {
                                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar heat gain coefficient must be between 0 and 1.", "Range Error", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    boolean changed = Math.abs(val - window.getSolarHeatGainCoefficient()) > 0.000001;
                                    if (rb1.isSelected()) {
                                        if (changed) {
                                            final ChangeWindowShgcCommand c = new ChangeWindowShgcCommand(window);
                                            window.setSolarHeatGainCoefficient(val);
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 0;
                                    } else if (rb2.isSelected()) {
                                        if (!changed) {
                                            if (window.getContainer() instanceof Wall) {
                                                final Wall wall = (Wall) window.getContainer();
                                                for (final Window x : wall.getWindows()) {
                                                    if (Math.abs(val - x.getSolarHeatGainCoefficient()) > 0.000001) {
                                                        changed = true;
                                                        break;
                                                    }
                                                }
                                            } else if (window.getContainer() instanceof Roof) {
                                                final Roof roof = (Roof) window.getContainer();
                                                for (final Window x : roof.getWindows()) {
                                                    if (Math.abs(val - x.getSolarHeatGainCoefficient()) > 0.000001) {
                                                        changed = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final ChangeContainerWindowShgcCommand c = new ChangeContainerWindowShgcCommand(window.getContainer());
                                            Scene.getInstance().setWindowShgcInContainer(window.getContainer(), val);
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 1;
                                    } else if (rb3.isSelected()) {
                                        final Foundation foundation = window.getTopContainer();
                                        if (!changed) {
                                            for (final Window x : foundation.getWindows()) {
                                                if (Math.abs(val - x.getSolarHeatGainCoefficient()) > 0.000001) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final ChangeBuildingWindowShgcCommand c = new ChangeBuildingWindowShgcCommand(foundation);
                                            Scene.getInstance().setWindowShgcOfBuilding(foundation, val);
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

            final JMenuItem miTint = new JMenuItem("Tint...");
            miTint.addActionListener(new ActionListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof Window)) {
                        return;
                    }
                    final Window window = (Window) selectedPart;
                    final JColorChooser colorChooser = MainFrame.getInstance().getColorChooser();
                    final ReadOnlyColorRGBA color = window.getColor();
                    if (color != null) {
                        colorChooser.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
                    }
                    final ActionListener actionListener = e1 -> {
                        final Color c = colorChooser.getColor();
                        final float[] newColor = c.getComponents(null);
                        final ColorRGBA color1 = new ColorRGBA(newColor[0], newColor[1], newColor[2], (float) (1.0 - window.getSolarHeatGainCoefficient()));
                        final JPanel panel = new JPanel();
                        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                        panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                        final JRadioButton rb1 = new JRadioButton("Only this Window", true);
                        final JRadioButton rb2 = new JRadioButton("All Windows on this " + (window.getContainer() instanceof Wall ? "Wall" : "Roof"));
                        final JRadioButton rb3 = new JRadioButton("All Windows of this Building");
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

                        final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
                        final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                        final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Window Tint");

                        while (true) {
                            dialog.setVisible(true);
                            final Object choice = optionPane.getValue();
                            if (choice == options[1] || choice == null) {
                                break;
                            } else {
                                boolean changed = !Util.isRGBEqual(color1, window.getColor());
                                if (rb1.isSelected()) { // apply to only this window
                                    if (changed) {
                                        final ChangePartColorCommand cmd = new ChangePartColorCommand(window);
                                        window.setColor(color1);
                                        SceneManager.getInstance().getUndoManager().addEdit(cmd);
                                    }
                                    selectedScopeIndex = 0;
                                } else if (rb2.isSelected()) {
                                    if (!changed) {
                                        if (window.getContainer() instanceof Wall) {
                                            final Wall wall = (Wall) window.getContainer();
                                            for (final Window x : wall.getWindows()) {
                                                if (!Util.isRGBEqual(color1, x.getColor())) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        } else if (window.getContainer() instanceof Roof) {
                                            final Roof roof = (Roof) window.getContainer();
                                            for (final Window x : roof.getWindows()) {
                                                if (!Util.isRGBEqual(color1, x.getColor())) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (changed) {
                                        final ChangeContainerWindowColorCommand cmd = new ChangeContainerWindowColorCommand(window.getContainer());
                                        Scene.getInstance().setWindowColorInContainer(window.getContainer(), color1, "tint");
                                        SceneManager.getInstance().getUndoManager().addEdit(cmd);
                                    }
                                    selectedScopeIndex = 1;
                                } else if (rb3.isSelected()) {
                                    final Foundation foundation = window.getTopContainer();
                                    if (!changed) {
                                        for (final Window x : foundation.getWindows()) {
                                            if (!Util.isRGBEqual(color1, x.getColor())) {
                                                changed = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (changed) {
                                        final ChangeBuildingColorCommand cmd = new ChangeBuildingColorCommand(window);
                                        Scene.getInstance().setPartColorOfBuilding(window, color1);
                                        SceneManager.getInstance().getUndoManager().addEdit(cmd);
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
                    };
                    JColorChooser.createDialog(MainFrame.getInstance(), "Select Tint", true, colorChooser, actionListener, null).setVisible(true);
                }
            });

            final JMenuItem miSize = new JMenuItem("Size...");
            miSize.addActionListener(new ActionListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof Window)) {
                        return;
                    }
                    final Window window = (Window) selectedPart;
                    final HousePart container = window.getContainer();
                    final Foundation foundation = window.getTopContainer();
                    final String partInfo = window.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                    final JPanel gui = new JPanel(new BorderLayout());
                    final JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
                    gui.add(inputPanel, BorderLayout.CENTER);
                    inputPanel.add(new JLabel("Width (m): "));
                    final JTextField widthField = new JTextField(threeDecimalsFormat.format(window.getWindowWidth()));
                    inputPanel.add(widthField);
                    inputPanel.add(new JLabel("Height (m): "));
                    final JTextField heightField = new JTextField(threeDecimalsFormat.format(window.getWindowHeight()));
                    inputPanel.add(heightField);
                    inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                    final JPanel scopePanel = new JPanel();
                    scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
                    scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                    final JRadioButton rb1 = new JRadioButton("Only this Window", true);
                    final JRadioButton rb2 = new JRadioButton("All Windows on this " + (window.getContainer() instanceof Wall ? "Wall" : "Roof"));
                    final JRadioButton rb3 = new JRadioButton("All Windows of this Building");
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
                    final JOptionPane optionPane = new JOptionPane(new Object[]{"Set Size for " + partInfo, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                    final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Window Size");

                    while (true) {
                        dialog.setVisible(true);
                        final Object choice = optionPane.getValue();
                        if (choice == options[1] || choice == null) {
                            break;
                        } else {
                            boolean ok = true;
                            double w = 0, h = 0;
                            try {
                                w = Double.parseDouble(widthField.getText());
                                h = Double.parseDouble(heightField.getText());
                            } catch (final NumberFormatException x) {
                                JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
                                ok = false;
                            }
                            if (ok) {
                                double wmax = 10;
                                if (container instanceof Wall) {
                                    wmax = ((Wall) container).getWallWidth() * 0.99;
                                }
                                double hmax = 10;
                                if (container instanceof Wall) {
                                    hmax = ((Wall) container).getWallHeight() * 0.99;
                                }
                                if (w < 0.1 || w > wmax) {
                                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "Width must be between 0.1 and " + (int) wmax + " m.", "Range Error", JOptionPane.ERROR_MESSAGE);
                                } else if (h < 0.1 || h > hmax) {
                                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "Height must be between 0.1 and " + (int) hmax + " m.", "Range Error", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    boolean changed = Math.abs(w - window.getWindowWidth()) > 0.000001 || Math.abs(h - window.getWindowHeight()) > 0.000001;
                                    final double w2 = w;
                                    final double h2 = h;
                                    if (rb1.isSelected()) {
                                        if (changed) {
                                            final SetPartSizeCommand c = new SetPartSizeCommand(window);
                                            SceneManager.getTaskManager().update(() -> {
                                                window.setWindowWidth(w2);
                                                window.setWindowHeight(h2);
                                                window.draw();
                                                window.getContainer().draw();
                                                SceneManager.getInstance().refresh();
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 0;
                                    } else if (rb2.isSelected()) {
                                        if (!changed) {
                                            if (window.getContainer() instanceof Wall) {
                                                final Wall wall = (Wall) window.getContainer();
                                                for (final Window x : wall.getWindows()) {
                                                    if (Math.abs(w - x.getWindowWidth()) > 0.000001 || Math.abs(h - x.getWindowHeight()) > 0.000001) {
                                                        changed = true;
                                                        break;
                                                    }
                                                }
                                            } else if (window.getContainer() instanceof Roof) {
                                                final Roof roof = (Roof) window.getContainer();
                                                for (final Window x : roof.getWindows()) {
                                                    if (Math.abs(w - x.getWindowWidth()) > 0.000001 || Math.abs(h - x.getWindowHeight()) > 0.000001) {
                                                        changed = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final ChangeContainerWindowSizeCommand c = new ChangeContainerWindowSizeCommand(window.getContainer());
                                            SceneManager.getTaskManager().update(() -> {
                                                Scene.getInstance().setWindowSizeInContainer(window.getContainer(), w2, h2);
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 1;
                                    } else if (rb3.isSelected()) {
                                        if (!changed) {
                                            for (final Window x : foundation.getWindows()) {
                                                if (Math.abs(w - x.getWindowWidth()) > 0.000001 || Math.abs(h - x.getWindowHeight()) > 0.000001) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final SetSizeForWindowsOnFoundationCommand c = new SetSizeForWindowsOnFoundationCommand(foundation);
                                            SceneManager.getTaskManager().update(() -> {
                                                foundation.setSizeForWindows(w2, h2);
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

            popupMenuForWindow.addSeparator();
            popupMenuForWindow.add(miSize);
            popupMenuForWindow.add(miTint);
            popupMenuForWindow.add(createInsulationMenuItem(true));
            popupMenuForWindow.add(miShgc);
            popupMenuForWindow.add(muntinMenu);
            popupMenuForWindow.add(shutterMenu);
            popupMenuForWindow.addSeparator();

            JMenuItem mi = new JMenuItem("Daily Energy Analysis...");
            mi.addActionListener(e -> {
                if (EnergyPanel.getInstance().adjustCellSize()) {
                    return;
                }
                if (SceneManager.getInstance().getSelectedPart() instanceof Window) {
                    new EnergyDailyAnalysis().show("Daily Energy for Window");
                }
            });
            popupMenuForWindow.add(mi);

            mi = new JMenuItem("Annual Energy Analysis...");
            mi.addActionListener(e -> {
                if (EnergyPanel.getInstance().adjustCellSize()) {
                    return;
                }
                if (SceneManager.getInstance().getSelectedPart() instanceof Window) {
                    new EnergyAnnualAnalysis().show("Annual Energy for Window");
                }
            });
            popupMenuForWindow.add(mi);

        }

        return popupMenuForWindow;

    }

}