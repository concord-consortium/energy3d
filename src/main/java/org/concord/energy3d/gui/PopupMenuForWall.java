package org.concord.energy3d.gui;

import org.concord.energy3d.model.Window;
import org.concord.energy3d.model.*;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.EnergyAnnualAnalysis;
import org.concord.energy3d.simulation.EnergyDailyAnalysis;
import org.concord.energy3d.undo.*;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.Util;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

class PopupMenuForWall extends PopupMenuFactory {

    private static JPopupMenu popupMenuForWall;

    static JPopupMenu getPopupMenuForWall() {

        if (popupMenuForWall == null) {

            final JMenuItem miPaste = new JMenuItem("Paste");
            miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
            miPaste.addActionListener(e -> {
                SceneManager.getTaskManager().update(() -> {
                    Scene.getInstance().pasteToPickedLocationOnWall();
                    return null;
                });
                Scene.getInstance().setEdited(true);
            });

            final JMenuItem miClear = new JMenuItem("Clear");
            miClear.addActionListener(e -> {
                SceneManager.getTaskManager().update(() -> {
                    Scene.getInstance().removeAllChildren(SceneManager.getInstance().getSelectedPart());
                    return null;
                });
                MainPanel.getInstance().getEnergyButton().setSelected(false);
                Scene.getInstance().setEdited(true);
            });

            final JMenuItem miDeleteAllConnectedWalls = new JMenuItem("Delete All Connected Walls");
            miDeleteAllConnectedWalls.addActionListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Wall) {
                    SceneManager.getTaskManager().update(() -> {
                        Scene.getInstance().deleteAllConnectedWalls((Wall) selectedPart);
                        return null;
                    });
                }
            });

            final JMenuItem miThickness = new JMenuItem("Thickness...");
            miThickness.addActionListener(new ActionListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof Wall)) {
                        return;
                    }
                    final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                    final Wall w = (Wall) selectedPart;
                    final String title = "<html>Thickness of " + partInfo + "</html>";
                    final String footnote = "<html><hr><font size=2>Thickness of wall is in meters.<hr></html>";
                    final JPanel gui = new JPanel(new BorderLayout());
                    final JPanel panel = new JPanel();
                    gui.add(panel, BorderLayout.CENTER);
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                    final JRadioButton rb1 = new JRadioButton("Only this Wall", true);
                    final JRadioButton rb2 = new JRadioButton("All Walls on This Foundation");
                    final JRadioButton rb3 = new JRadioButton("All Walls");
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
                    final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(w.getThickness() * Scene.getInstance().getScale()));
                    gui.add(inputField, BorderLayout.SOUTH);

                    final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
                    final JOptionPane optionPane = new JOptionPane(new Object[]{title, footnote, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                    final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Wall Thickness");

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
                                if (val < 0.1 || val > 10) {
                                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "The thickness of a wall must be between 0.1 and 10 meters.", "Range Error", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    val /= Scene.getInstance().getScale();
                                    Wall.setDefaultThickess(val);
                                    boolean changed = Math.abs(val - w.getThickness()) > 0.000001;
                                    final double val2 = val;
                                    if (rb1.isSelected()) {
                                        if (changed) {
                                            final ChangeWallThicknessCommand c = new ChangeWallThicknessCommand(w);
                                            SceneManager.getTaskManager().update(() -> {
                                                w.setThickness(val2);
                                                w.draw();
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 0;
                                    } else if (rb2.isSelected()) {
                                        final Foundation foundation = w.getTopContainer();
                                        if (!changed) {
                                            for (final Wall x : foundation.getWalls()) {
                                                if (Math.abs(val - x.getThickness()) > 0.000001) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final ChangeFoundationWallThicknessCommand c = new ChangeFoundationWallThicknessCommand(foundation);
                                            SceneManager.getTaskManager().update(() -> {
                                                foundation.setThicknessOfWalls(val2);
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 1;
                                    } else if (rb3.isSelected()) {
                                        if (!changed) {
                                            for (final Wall x : Scene.getInstance().getAllWalls()) {
                                                if (Math.abs(val - x.getThickness()) > 0.000001) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final ChangeThicknessForAllWallsCommand c = new ChangeThicknessForAllWallsCommand(w);
                                            SceneManager.getTaskManager().update(() -> {
                                                Scene.getInstance().setThicknessForAllWalls(val2);
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

            final JMenuItem miHeight = new JMenuItem("Height...");
            miHeight.addActionListener(new ActionListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope
                private boolean changed;
                private double val;

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof Wall)) {
                        return;
                    }
                    final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                    final Wall w = (Wall) selectedPart;
                    final String title = "<html>Height of " + partInfo + "</html>";
                    final String footnote = "<html><hr><font size=2>Height of wall is in meters.<hr></html>";
                    final JPanel gui = new JPanel(new BorderLayout());
                    final JPanel panel = new JPanel();
                    gui.add(panel, BorderLayout.CENTER);
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                    final JRadioButton rb1 = new JRadioButton("Only this Wall", true);
                    final JRadioButton rb2 = new JRadioButton("All Walls Connected to This One (Direct and Indirect)");
                    final JRadioButton rb3 = new JRadioButton("All Walls on This Foundation");
                    final JRadioButton rb4 = new JRadioButton("All Walls");
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
                    final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(w.getHeight() * Scene.getInstance().getScale()));
                    gui.add(inputField, BorderLayout.SOUTH);

                    final Object[] options = new Object[]{"OK", "Cancel", "Apply"};
                    final JOptionPane optionPane = new JOptionPane(new Object[]{title, footnote, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                    final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Wall Height");

                    while (true) {
                        inputField.selectAll();
                        inputField.requestFocusInWindow();
                        dialog.setVisible(true);
                        final Object choice = optionPane.getValue();
                        if (choice == options[1] || choice == null) {
                            break;
                        } else {
                            boolean ok = true;
                            try {
                                val = Double.parseDouble(inputField.getText());
                            } catch (final NumberFormatException exception) {
                                JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
                                ok = false;
                            }
                            if (ok) {
                                if (val < 1 || val > 1000) {
                                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "The height of a wall must be between 1 and 1000 meters.", "Range Error", JOptionPane.ERROR_MESSAGE);
                                } else {
                                    val /= Scene.getInstance().getScale();
                                    changed = Math.abs(val - w.getHeight()) > 0.000001;
                                    if (rb1.isSelected()) {
                                        if (changed) {
                                            final ChangeWallHeightCommand c = new ChangeWallHeightCommand(w);
                                            SceneManager.getTaskManager().update(() -> {
                                                w.setHeight(val, true);
                                                Scene.getInstance().redrawAllWallsNow();
                                                final Foundation foundation = w.getTopContainer();
                                                if (foundation.hasSolarReceiver()) {
                                                    foundation.drawSolarReceiver();
                                                    for (final HousePart x : Scene.getInstance().getParts()) {
                                                        if (x instanceof FresnelReflector) {
                                                            final FresnelReflector reflector = (FresnelReflector) x;
                                                            if (foundation == reflector.getReceiver() && reflector.isSunBeamVisible()) {
                                                                reflector.drawSunBeam();
                                                            }
                                                        } else if (x instanceof Mirror) {
                                                            final Mirror heliostat = (Mirror) x;
                                                            if (foundation == heliostat.getReceiver() && heliostat.isSunBeamVisible()) {
                                                                heliostat.setNormal();
                                                                heliostat.drawSunBeam();
                                                            }
                                                        }
                                                    }
                                                    SceneManager.getInstance().refresh();
                                                }
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 0;
                                    } else if (rb2.isSelected()) {
                                        if (!changed) {
                                            w.visitNeighbors((currentWall, prev, next) -> {
                                                if (Math.abs(val - currentWall.getHeight()) > 0.000001) {
                                                    changed = true;
                                                }
                                            });
                                        }
                                        if (changed) {
                                            final ChangeHeightForConnectedWallsCommand c = new ChangeHeightForConnectedWallsCommand(w);
                                            SceneManager.getTaskManager().update(() -> {
                                                Scene.getInstance().setHeightOfConnectedWalls(w, val);
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 1;
                                    } else if (rb3.isSelected()) {
                                        final Foundation foundation = w.getTopContainer();
                                        if (!changed) {
                                            for (final Wall x : foundation.getWalls()) {
                                                if (Math.abs(val - x.getHeight()) > 0.000001) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final ChangeFoundationWallHeightCommand c = new ChangeFoundationWallHeightCommand(foundation);
                                            SceneManager.getTaskManager().update(() -> {
                                                foundation.setHeightOfWalls(val);
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 2;
                                    } else if (rb4.isSelected()) {
                                        if (!changed) {
                                            for (final Wall x : Scene.getInstance().getAllWalls()) {
                                                if (Math.abs(val - x.getHeight()) > 0.000001) {
                                                    changed = true;
                                                    break;
                                                }
                                            }
                                        }
                                        if (changed) {
                                            final ChangeHeightForAllWallsCommand c = new ChangeHeightForAllWallsCommand(w);
                                            SceneManager.getTaskManager().update(() -> {
                                                Scene.getInstance().setHeightForAllWalls(val);
                                                return null;
                                            });
                                            SceneManager.getInstance().getUndoManager().addEdit(c);
                                        }
                                        selectedScopeIndex = 3;
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

            final JCheckBoxMenuItem miOutline = new JCheckBoxMenuItem("Outline...", true);
            miOutline.addActionListener(new ActionListener() {

                private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof Wall)) {
                        return;
                    }
                    final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                    final Wall w = (Wall) selectedPart;
                    final String title = "<html>Outline of " + partInfo + "</html>";
                    final String footnote = "<html>Hiding outline may create a continuous effect of a polygon<br>formed by many walls.</html>";
                    final JPanel gui = new JPanel(new BorderLayout());
                    final JPanel panel = new JPanel();
                    gui.add(panel, BorderLayout.CENTER);
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                    panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                    final JRadioButton rb1 = new JRadioButton("Only this Wall", true);
                    final JRadioButton rb2 = new JRadioButton("All Walls Connected to This One (Direct and Indirect)");
                    final JRadioButton rb3 = new JRadioButton("All Walls on This Foundation");
                    final JRadioButton rb4 = new JRadioButton("All Walls");
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
                    final JOptionPane optionPane = new JOptionPane(new Object[]{title, footnote, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                    final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Wall Outline");

                    while (true) {
                        dialog.setVisible(true);
                        final Object choice = optionPane.getValue();
                        if (choice == options[1] || choice == null) {
                            break;
                        } else {
                            if (rb1.isSelected()) {
                                // final ChangeWallHeightCommand c = new ChangeWallHeightCommand(w);
                                w.showOutline(miOutline.isSelected());
                                SceneManager.getTaskManager().update(() -> {
                                    w.draw();
                                    return null;
                                });
                                // SceneManager.getInstance().getUndoManager().addEdit(c);
                                selectedScopeIndex = 0;
                            } else if (rb2.isSelected()) {
                                // final ChangeHeightForConnectedWallsCommand c = new ChangeHeightForConnectedWallsCommand(w);
                                SceneManager.getTaskManager().update(() -> {
                                    Scene.getInstance().showOutlineOfConnectedWalls(w, miOutline.isSelected());
                                    return null;
                                });
                                // SceneManager.getInstance().getUndoManager().addEdit(c);
                                selectedScopeIndex = 1;
                            } else if (rb3.isSelected()) {
                                final Foundation foundation = w.getTopContainer();
                                // final ChangeFoundationWallHeightCommand c = new ChangeFoundationWallHeightCommand(foundation);
                                SceneManager.getTaskManager().update(() -> {
                                    foundation.showOutlineOfWalls(miOutline.isSelected());
                                    return null;
                                });
                                // SceneManager.getInstance().getUndoManager().addEdit(c);
                                selectedScopeIndex = 2;
                            } else if (rb4.isSelected()) {
                                // final ChangeHeightForAllWallsCommand c = new ChangeHeightForAllWallsCommand(w);
                                SceneManager.getTaskManager().update(() -> {
                                    Scene.getInstance().showOutlineForAllWalls(miOutline.isSelected());
                                    return null;
                                });
                                // SceneManager.getInstance().getUndoManager().addEdit(c);
                                selectedScopeIndex = 3;
                            }
                            updateAfterEdit();
                            if (choice == options[0]) {
                                break;
                            }
                        }
                    }

                }
            });

            popupMenuForWall = createPopupMenu(false, false, () -> {
                final HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
                miPaste.setEnabled(copyBuffer instanceof Window || copyBuffer instanceof SolarPanel || copyBuffer instanceof Rack);
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Wall) {
                    final Wall w = (Wall) selectedPart;
                    Util.selectSilently(miOutline, w.outlineShown());
                }
            });

            popupMenuForWall.add(miPaste);
            popupMenuForWall.add(miDeleteAllConnectedWalls);
            popupMenuForWall.add(miClear);
            popupMenuForWall.addSeparator();
            popupMenuForWall.add(colorAction);
            popupMenuForWall.add(miOutline);
            popupMenuForWall.add(miThickness);
            popupMenuForWall.add(miHeight);
            popupMenuForWall.add(createInsulationMenuItem(false));
            popupMenuForWall.add(createVolumetricHeatCapacityMenuItem());
            popupMenuForWall.addSeparator();

            final JMenu textureMenu = new JMenu("Texture");
            popupMenuForWall.add(textureMenu);
            final ButtonGroup textureButtonGroup = new ButtonGroup();
            final JRadioButtonMenuItem rbmiTextureNone = createTextureMenuItem(Wall.TEXTURE_NONE, null);
            final JRadioButtonMenuItem rbmiTextureEdge = createTextureMenuItem(Wall.TEXTURE_EDGE, null);
            final JRadioButtonMenuItem rbmiTexture01 = createTextureMenuItem(Wall.TEXTURE_01, "icons/wall_01.png");
            final JRadioButtonMenuItem rbmiTexture02 = createTextureMenuItem(Wall.TEXTURE_02, "icons/wall_02.png");
            final JRadioButtonMenuItem rbmiTexture03 = createTextureMenuItem(Wall.TEXTURE_03, "icons/wall_03.png");
            final JRadioButtonMenuItem rbmiTexture04 = createTextureMenuItem(Wall.TEXTURE_04, "icons/wall_04.png");
            final JRadioButtonMenuItem rbmiTexture05 = createTextureMenuItem(Wall.TEXTURE_05, "icons/wall_05.png");
            final JRadioButtonMenuItem rbmiTexture06 = createTextureMenuItem(Wall.TEXTURE_06, "icons/wall_06.png");
            final JRadioButtonMenuItem rbmiTexture07 = createTextureMenuItem(Wall.TEXTURE_07, "icons/wall_07.png");
            textureButtonGroup.add(rbmiTextureNone);
            textureButtonGroup.add(rbmiTextureEdge);
            textureButtonGroup.add(rbmiTexture01);
            textureButtonGroup.add(rbmiTexture02);
            textureButtonGroup.add(rbmiTexture03);
            textureButtonGroup.add(rbmiTexture04);
            textureButtonGroup.add(rbmiTexture05);
            textureButtonGroup.add(rbmiTexture06);
            textureButtonGroup.add(rbmiTexture07);
            textureMenu.add(rbmiTextureNone);
            textureMenu.add(rbmiTextureEdge);
            textureMenu.addSeparator();
            textureMenu.add(rbmiTexture01);
            textureMenu.add(rbmiTexture02);
            textureMenu.add(rbmiTexture03);
            textureMenu.add(rbmiTexture04);
            textureMenu.add(rbmiTexture05);
            textureMenu.add(rbmiTexture06);
            textureMenu.add(rbmiTexture07);

            textureMenu.addMenuListener(new MenuListener() {

                @Override
                public void menuSelected(final MenuEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof Wall)) {
                        return;
                    }
                    final Wall wall = (Wall) selectedPart;
                    switch (wall.getTextureType()) {
                        case Wall.TEXTURE_NONE:
                            Util.selectSilently(rbmiTextureNone, true);
                            break;
                        case Wall.TEXTURE_EDGE:
                            Util.selectSilently(rbmiTextureEdge, true);
                            break;
                        case Wall.TEXTURE_01:
                            Util.selectSilently(rbmiTexture01, true);
                            break;
                        case Wall.TEXTURE_02:
                            Util.selectSilently(rbmiTexture02, true);
                            break;
                        case Wall.TEXTURE_03:
                            Util.selectSilently(rbmiTexture03, true);
                            break;
                        case Wall.TEXTURE_04:
                            Util.selectSilently(rbmiTexture04, true);
                            break;
                        case Wall.TEXTURE_05:
                            Util.selectSilently(rbmiTexture05, true);
                            break;
                        case Wall.TEXTURE_06:
                            Util.selectSilently(rbmiTexture06, true);
                            break;
                        case Wall.TEXTURE_07:
                            Util.selectSilently(rbmiTexture07, true);
                            break;
                        default:
                            textureButtonGroup.clearSelection();
                    }
                }

                @Override
                public void menuDeselected(final MenuEvent e) {
                    textureMenu.setEnabled(true);
                }

                @Override
                public void menuCanceled(final MenuEvent e) {
                    textureMenu.setEnabled(true);
                }

            });

            final JMenu typeMenu = new JMenu("Type");
            popupMenuForWall.add(typeMenu);
            popupMenuForWall.addSeparator();
            final ButtonGroup typeGroup = new ButtonGroup();

            final JRadioButtonMenuItem rbmiSolidWall = new JRadioButtonMenuItem("Solid Wall");
            rbmiSolidWall.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof Wall) {
                        final Wall wall = (Wall) selectedPart;
                        final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
                        SceneManager.getTaskManager().update(() -> {
                            wall.setType(Wall.SOLID_WALL);
                            wall.draw();
                            return null;
                        });
                        Scene.getInstance().setEdited(true);
                        SceneManager.getInstance().getUndoManager().addEdit(c);
                    }
                }
            });
            typeMenu.add(rbmiSolidWall);
            typeGroup.add(rbmiSolidWall);

            final JRadioButtonMenuItem rbmiEmpty = new JRadioButtonMenuItem("Empty");
            rbmiEmpty.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof Wall) {
                        final Wall wall = (Wall) selectedPart;
                        final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
                        SceneManager.getTaskManager().update(() -> {
                            wall.setType(Wall.EMPTY);
                            wall.draw();
                            return null;
                        });
                        Scene.getInstance().setEdited(true);
                        SceneManager.getInstance().getUndoManager().addEdit(c);
                    }
                }
            });
            typeMenu.add(rbmiEmpty);
            typeGroup.add(rbmiEmpty);

            final JRadioButtonMenuItem rbmiEdges = new JRadioButtonMenuItem("Vertical Edges");
            rbmiEdges.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof Wall) {
                        final Wall wall = (Wall) selectedPart;
                        final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
                        SceneManager.getTaskManager().update(() -> {
                            wall.setType(Wall.VERTICAL_EDGES_ONLY);
                            wall.draw();
                            return null;
                        });
                        Scene.getInstance().setEdited(true);
                        SceneManager.getInstance().getUndoManager().addEdit(c);
                    }
                }
            });
            typeMenu.add(rbmiEdges);
            typeGroup.add(rbmiEdges);

            final JRadioButtonMenuItem rbmiColumns = new JRadioButtonMenuItem("Columns");
            rbmiColumns.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof Wall) {
                        final Wall wall = (Wall) selectedPart;
                        final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
                        SceneManager.getTaskManager().update(() -> {
                            wall.setType(Wall.COLUMNS_ONLY);
                            wall.draw();
                            return null;
                        });
                        Scene.getInstance().setEdited(true);
                        SceneManager.getInstance().getUndoManager().addEdit(c);
                    }
                }
            });
            typeMenu.add(rbmiColumns);
            typeGroup.add(rbmiColumns);

            final JRadioButtonMenuItem rbmiRails = new JRadioButtonMenuItem("Rails");
            rbmiRails.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof Wall) {
                        final Wall wall = (Wall) selectedPart;
                        final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
                        SceneManager.getTaskManager().update(() -> {
                            wall.setType(Wall.RAILS_ONLY);
                            wall.draw();
                            return null;
                        });
                        Scene.getInstance().setEdited(true);
                        SceneManager.getInstance().getUndoManager().addEdit(c);
                    }
                }
            });
            typeMenu.add(rbmiRails);
            typeGroup.add(rbmiRails);

            final JRadioButtonMenuItem rbmiColumnsAndRailings = new JRadioButtonMenuItem("Columns & Railings");
            rbmiColumnsAndRailings.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof Wall) {
                        final Wall wall = (Wall) selectedPart;
                        final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
                        SceneManager.getTaskManager().update(() -> {
                            wall.setType(Wall.COLUMNS_RAILS);
                            wall.draw();
                            return null;
                        });
                        Scene.getInstance().setEdited(true);
                        SceneManager.getInstance().getUndoManager().addEdit(c);
                    }
                }
            });
            typeMenu.add(rbmiColumnsAndRailings);
            typeGroup.add(rbmiColumnsAndRailings);

            final JRadioButtonMenuItem rbmiFence = new JRadioButtonMenuItem("Fence");
            rbmiFence.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof Wall) {
                        final Wall wall = (Wall) selectedPart;
                        final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
                        SceneManager.getTaskManager().update(() -> {
                            wall.setType(Wall.FENCE);
                            wall.draw();
                            return null;
                        });
                        Scene.getInstance().setEdited(true);
                        SceneManager.getInstance().getUndoManager().addEdit(c);
                    }
                }
            });
            typeMenu.add(rbmiFence);
            typeGroup.add(rbmiFence);

            final JRadioButtonMenuItem rbmiSteelFrame = new JRadioButtonMenuItem("Steel Frame");
            rbmiSteelFrame.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof Wall) {
                        final Wall wall = (Wall) selectedPart;
                        final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
                        SceneManager.getTaskManager().update(() -> {
                            wall.setType(Wall.STEEL_FRAME);
                            wall.draw();
                            return null;
                        });
                        Scene.getInstance().setEdited(true);
                        SceneManager.getInstance().getUndoManager().addEdit(c);
                    }
                }
            });
            typeMenu.add(rbmiSteelFrame);
            typeGroup.add(rbmiSteelFrame);

            typeMenu.addMenuListener(new MenuListener() {

                @Override
                public void menuSelected(final MenuEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof Wall) {
                        final Wall wall = (Wall) selectedPart;
                        switch (wall.getType()) {
                            case Wall.SOLID_WALL:
                                Util.selectSilently(rbmiSolidWall, true);
                                break;
                            case Wall.EMPTY:
                                Util.selectSilently(rbmiEmpty, true);
                                break;
                            case Wall.VERTICAL_EDGES_ONLY:
                                Util.selectSilently(rbmiEdges, true);
                                break;
                            case Wall.COLUMNS_ONLY:
                                Util.selectSilently(rbmiColumns, true);
                                break;
                            case Wall.RAILS_ONLY:
                                Util.selectSilently(rbmiRails, true);
                                break;
                            case Wall.COLUMNS_RAILS:
                                Util.selectSilently(rbmiColumnsAndRailings, true);
                                break;
                            case Wall.STEEL_FRAME:
                                Util.selectSilently(rbmiSteelFrame, true);
                                break;
                        }
                    }
                }

                @Override
                public void menuDeselected(final MenuEvent e) {
                    typeMenu.setEnabled(true);
                }

                @Override
                public void menuCanceled(final MenuEvent e) {
                    typeMenu.setEnabled(true);
                }

            });

            JMenuItem mi = new JMenuItem("Daily Energy Analysis...");
            mi.addActionListener(e -> {
                if (EnergyPanel.getInstance().adjustCellSize()) {
                    return;
                }
                if (SceneManager.getInstance().getSelectedPart() instanceof Wall) {
                    new EnergyDailyAnalysis().show("Daily Energy for Wall");
                }
            });
            popupMenuForWall.add(mi);

            mi = new JMenuItem("Annual Energy Analysis...");
            mi.addActionListener(e -> {
                if (EnergyPanel.getInstance().adjustCellSize()) {
                    return;
                }
                if (SceneManager.getInstance().getSelectedPart() instanceof Wall) {
                    new EnergyAnnualAnalysis().show("Annual Energy for Wall");
                }
            });
            popupMenuForWall.add(mi);

        }

        return popupMenuForWall;

    }

    private static JRadioButtonMenuItem createTextureMenuItem(final int type, final String imageFile) {

        final JRadioButtonMenuItem m;
        if (type == HousePart.TEXTURE_NONE) {
            m = new JRadioButtonMenuItem("No Texture");
        } else if (type == HousePart.TEXTURE_EDGE) {
            m = new JRadioButtonMenuItem("Edge Texture");
        } else {
            m = new JRadioButtonMenuItem(new ImageIcon(MainPanel.class.getResource(imageFile)));
            m.setText("Texture #" + type);
        }

        m.addItemListener(new ItemListener() {

            private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

            @Override
            public void itemStateChanged(final ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (!(selectedPart instanceof Wall)) {
                        return;
                    }
                    final Wall wall = (Wall) selectedPart;
                    final Foundation foundation = wall.getTopContainer();
                    final String partInfo = wall.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                    final JPanel gui = new JPanel(new BorderLayout());
                    final JPanel scopePanel = new JPanel();
                    scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
                    scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
                    final JRadioButton rb1 = new JRadioButton("Only this Wall", true);
                    final JRadioButton rb2 = new JRadioButton("All Walls on this Foundation");
                    final JRadioButton rb3 = new JRadioButton("All Walls");
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
                    final JOptionPane optionPane = new JOptionPane(new Object[]{"Set Texture for " + partInfo, gui}, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
                    final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Wall Texture");

                    while (true) {
                        dialog.setVisible(true);
                        final Object choice = optionPane.getValue();
                        if (choice == options[1] || choice == null) {
                            break;
                        } else {
                            if (rb1.isSelected()) {
                                final ChangeTextureCommand c = new ChangeTextureCommand(wall);
                                SceneManager.getTaskManager().update(() -> {
                                    wall.setTextureType(type);
                                    wall.draw();
                                    return null;
                                });
                                SceneManager.getInstance().getUndoManager().addEdit(c);
                                selectedScopeIndex = 0;
                            } else if (rb2.isSelected()) {
                                final SetTextureForWallsOnFoundationCommand c = new SetTextureForWallsOnFoundationCommand(foundation);
                                SceneManager.getTaskManager().update(() -> {
                                    foundation.setTextureForWalls(type);
                                    return null;
                                });
                                SceneManager.getInstance().getUndoManager().addEdit(c);
                                selectedScopeIndex = 1;
                            } else if (rb3.isSelected()) {
                                final List<HousePart> walls = Scene.getInstance().getAllPartsOfSameType(wall);
                                final SetTextureForPartsCommand c = new SetTextureForPartsCommand(walls);
                                SceneManager.getTaskManager().update(() -> {
                                    for (final HousePart p : walls) {
                                        p.setTextureType(type);
                                        p.draw();
                                    }
                                    return null;
                                });
                                SceneManager.getInstance().getUndoManager().addEdit(c);
                                selectedScopeIndex = 2;
                            }
                            updateAfterEdit();
                            if (MainPanel.getInstance().getEnergyButton().isSelected()) {
                                MainPanel.getInstance().getEnergyButton().setSelected(false);
                            }
                            SceneManager.getInstance().refresh();
                            if (choice == options[0]) {
                                break;
                            }
                        }
                    }

                }
            }
        });

        return m;

    }

}