package org.concord.energy3d.gui;

import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import org.concord.energy3d.model.*;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.SpringUtilities;
import org.concord.energy3d.util.Util;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

class PopupMenuForMesh extends PopupMenuFactory {

    private static JPopupMenu popupMenuForMesh;

    static JPopupMenu getPopupMenu() {

        if (popupMenuForMesh == null) {

            final JMenuItem miInfo = new JMenuItem("Mesh");
            miInfo.setEnabled(false);
            miInfo.setOpaque(true);
            miInfo.setBackground(Config.isMac() ? Color.BLACK : Color.GRAY);
            miInfo.setForeground(Color.WHITE);

            final JMenuItem miMessThickness = new JMenuItem("Thickness...");
            miMessThickness.addActionListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Foundation) {
                    final Foundation f = (Foundation) selectedPart;
                    final Mesh m = f.getSelectedMesh();
                    if (m != null) {
                        final Node n = m.getParent();
                        final String title = "<html>Adjust the distance between two mesh faces to create some thickness<br>A larger thickness also mitigates the z-fighting effect.</html>";
                        while (true) {
                            final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), title, f.getMeshThickness(n) * Scene.getInstance().getScale());
                            if (newValue == null) {
                                break;
                            } else {
                                try {
                                    final double val = Double.parseDouble(newValue);
                                    if (val < 0 || val > 1) {
                                        JOptionPane.showMessageDialog(MainFrame.getInstance(), "Thickness must be between 0 and 1 meter.", "Range Error", JOptionPane.ERROR_MESSAGE);
                                    } else {
                                        SceneManager.getTaskManager().update(() -> {
                                            f.setMeshThickness(n, val / Scene.getInstance().getScale());
                                            f.draw();
                                            return null;
                                        });
                                        break;
                                    }
                                } catch (final NumberFormatException exception) {
                                    JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    }
                }
            });

            final JMenuItem miReverseNormalVector = new JMenuItem("Reverse Mesh Normal Vector");
            miReverseNormalVector.addActionListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Foundation) {
                    final Foundation f = (Foundation) selectedPart;
                    SceneManager.getTaskManager().update(() -> {
                        final Mesh m = f.getSelectedMesh();
                        if (m != null) {
                            Util.reverseFace(m);
                            f.getNodeState(m.getParent()).reverseNormalOfMesh(((UserData) m.getUserData()).getMeshIndex());
                            f.draw();
                            updateAfterEdit();
                        }
                        return null;
                    });
                }
            });

            final JMenuItem miAlignBottom = new JMenuItem("Align Node Bottom with Ground Level");
            miAlignBottom.addActionListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Foundation) {
                    final Foundation f = (Foundation) selectedPart;
                    final Mesh m = f.getSelectedMesh();
                    if (m != null) {
                        SceneManager.getTaskManager().update(() -> {
                            final Node n = m.getParent();
                            if (n != null) {
                                final OrientedBoundingBox boundingBox = Util.getOrientedBoundingBox(n);
                                final double zBottom = boundingBox.getCenter().getZ() - boundingBox.getZAxis().getZ() * boundingBox.getExtent().getZ() - f.getHeight();
                                f.translateImportedNode(n, 0, 0, -zBottom);
                                f.draw();
                                updateAfterEdit();
                            }
                            return null;
                        });
                    }
                }
            });

            final JMenuItem miAlignCenter = new JMenuItem("Align Node Center with Foundation Center");
            miAlignCenter.addActionListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Foundation) {
                    final Foundation f = (Foundation) selectedPart;
                    final Mesh m = f.getSelectedMesh();
                    if (m != null) {
                        SceneManager.getTaskManager().update(() -> {
                            final Node n = m.getParent();
                            if (n != null) {
                                final OrientedBoundingBox boundingBox = Util.getOrientedBoundingBox(n);
                                final ReadOnlyVector3 shift = boundingBox.getCenter().subtract(f.getAbsCenter(), null);
                                f.translateImportedNode(n, shift.getX(), shift.getY(), 0);
                                f.setMeshSelectionVisible(false);
                                f.draw();
                                updateAfterEdit();
                            }
                            return null;
                        });
                    }
                }
            });

            final JMenuItem miCopyNode = new JMenuItem("Copy Node");
            miCopyNode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
            miCopyNode.addActionListener(e -> SceneManager.getTaskManager().update(() -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Foundation) {
                    final Foundation f = (Foundation) selectedPart;
                    final Mesh m = f.getSelectedMesh();
                    if (m != null) {
                        final Node n = m.getParent();
                        Scene.getInstance().setCopyNode(n, f.getNodeState(n));
                    }
                }
                return null;
            }));

            final JMenuItem miPaste = new JMenuItem("Paste");
            miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
            miPaste.addActionListener(e -> SceneManager.getTaskManager().update(() -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Foundation) {
                    final Foundation f = (Foundation) selectedPart;
                    final Mesh m = f.getSelectedMesh();
                    if (m != null) {
                        Scene.getInstance().pasteToPickedLocationOnMesh(m);
                        Scene.getInstance().setEdited(true);
                        updateAfterEdit();
                    }
                }
                return null;
            }));

            popupMenuForMesh = new JPopupMenu();
            popupMenuForMesh.setInvoker(MainPanel.getInstance().getCanvasPanel());
            popupMenuForMesh.addPopupMenuListener(new PopupMenuListener() {

                @Override
                public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof Foundation) {
                        final Foundation f = (Foundation) selectedPart;
                        final Mesh m = f.getSelectedMesh();
                        if (m != null) {
                            String name = f.getNodeState(m.getParent()).getName();
                            if (name == null) {
                                name = "Undefined";
                            }
                            miInfo.setText(m.getName() + " (" + name + ")");
                            final OrientedBoundingBox boundingBox = Util.getOrientedBoundingBox(m.getParent());
                            final ReadOnlyVector3 center = boundingBox.getCenter();
                            final double zBottom = center.getZ() - boundingBox.getZAxis().getZ() * boundingBox.getExtent().getZ();
                            miAlignBottom.setEnabled(!Util.isZero(zBottom - f.getHeight()));
                            final Vector3 foundationCenter = f.getAbsCenter();
                            miAlignCenter.setEnabled(!Util.isEqual(new Vector2(foundationCenter.getX(), foundationCenter.getY()), new Vector2(center.getX(), center.getY())));
                            final HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
                            miPaste.setEnabled(copyBuffer instanceof SolarPanel || copyBuffer instanceof Rack);
                        }
                    }
                }

                @Override
                public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
                    miAlignBottom.setEnabled(true);
                    miAlignCenter.setEnabled(true);
                }

                @Override
                public void popupMenuCanceled(final PopupMenuEvent e) {
                    miAlignBottom.setEnabled(true);
                    miAlignCenter.setEnabled(true);
                }

            });

            final JMenuItem miDeleteMesh = new JMenuItem("Delete Mesh");
            miDeleteMesh.addActionListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Foundation) {
                    final Foundation f = (Foundation) selectedPart;
                    final Mesh m = f.getSelectedMesh();
                    if (m != null) {
                        SceneManager.getTaskManager().update(() -> {
                            f.deleteMesh(m);
                            updateAfterEdit();
                            return null;
                        });
                    }
                }
            });

            final JMenuItem miRestoreDeletedMeshes = new JMenuItem("Restore Deleted Meshes (Reload Required)");
            miRestoreDeletedMeshes.addActionListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Foundation) {
                    final Foundation f = (Foundation) selectedPart;
                    final Mesh m = f.getSelectedMesh();
                    if (m != null) {
                        SceneManager.getTaskManager().update(() -> {
                            f.restoreDeletedMeshes(m.getParent());
                            updateAfterEdit();
                            return null;
                        });
                    }
                }
            });

            final JMenuItem miCutNode = new JMenuItem("Cut Node");
            miCutNode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
            miCutNode.addActionListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Foundation) {
                    final Foundation f = (Foundation) selectedPart;
                    final Mesh m = f.getSelectedMesh();
                    if (m != null) {
                        SceneManager.getTaskManager().update(() -> {
                            final Node n = m.getParent();
                            Scene.getInstance().setCopyNode(n, f.getNodeState(n));
                            f.deleteNode(n);
                            updateAfterEdit();
                            return null;
                        });
                    }
                }
            });

            final JMenuItem miMeshProperties = new JMenuItem("Mesh Properties...");
            miMeshProperties.addActionListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Foundation) {
                    final Foundation f = (Foundation) selectedPart;
                    final Mesh m = f.getSelectedMesh();
                    if (m != null) {
                        final UserData ud = (UserData) m.getUserData();

                        final JPanel gui = new JPanel(new BorderLayout());
                        final String title = "<html>A mesh is a basic unit (e.g., a triangle or a line) of geometry of a structure.</html>";
                        gui.add(new JLabel(title), BorderLayout.NORTH);
                        final JPanel propertiesPanel = new JPanel(new SpringLayout());
                        propertiesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                        gui.add(propertiesPanel, BorderLayout.CENTER);

                        // index mode
                        JLabel label = new JLabel("Index Mode: ", JLabel.TRAILING);
                        propertiesPanel.add(label);
                        JTextField textField = new JTextField(m.getMeshData().getIndexMode(0) + "", 5);
                        textField.setEditable(false);
                        label.setLabelFor(textField);
                        propertiesPanel.add(textField);

                        // vertex count
                        label = new JLabel("Vertex Count: ", JLabel.TRAILING);
                        propertiesPanel.add(label);
                        textField = new JTextField(m.getMeshData().getVertexCount() + "", 5);
                        textField.setEditable(false);
                        label.setLabelFor(textField);
                        propertiesPanel.add(textField);

                        // normal
                        label = new JLabel("Normal Vector: ", JLabel.TRAILING);
                        propertiesPanel.add(label);
                        final ReadOnlyVector3 normal = ((UserData) m.getUserData()).getNormal();
                        textField = new JTextField("(" + threeDecimalsFormat.format(normal.getX()) + ", " + threeDecimalsFormat.format(normal.getY()) + ", " + threeDecimalsFormat.format(normal.getZ()) + "), relative", 5);
                        textField.setEditable(false);
                        label.setLabelFor(textField);
                        propertiesPanel.add(textField);

                        // color
                        label = new JLabel("Color: ", JLabel.TRAILING);
                        propertiesPanel.add(label);
                        final ReadOnlyColorRGBA rgb = m.getDefaultColor();
                        colorChooser.setColor(new Color(Math.round(rgb.getRed() * 255), Math.round(rgb.getGreen() * 255), Math.round(rgb.getBlue() * 255)));
                        label.setLabelFor(colorChooser);
                        propertiesPanel.add(colorChooser);

                        SpringUtilities.makeCompactGrid(propertiesPanel, 4, 2, 6, 6, 6, 6);
                        if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), gui, "Mesh Properties: " + miInfo.getText(), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                            final Color color = colorChooser.getColor();
                            m.clearRenderState(StateType.Texture);
                            m.setDefaultColor(new ColorRGBA(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1));
                            final NodeState ns = f.getNodeState(m.getParent());
                            ns.setMeshColor(ud.getMeshIndex(), m.getDefaultColor());
                            SceneManager.getTaskManager().update(() -> {
                                f.draw();
                                return null;
                            });
                        }
                    }
                }
            });

            final JMenuItem miNodeProperties = new JMenuItem("Node Properties...");
            miNodeProperties.addActionListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Foundation) {
                    final Foundation f = (Foundation) selectedPart;
                    final Mesh m = f.getSelectedMesh();
                    if (m != null) {
                        final Node n = m.getParent();
                        if (n != null) {
                            final NodeState ns = f.getNodeState(n);
                            final JPanel gui = new JPanel(new BorderLayout());
                            final String title = "<html>A node contains a set of meshes that represent<br>the geometry of the structure.</html>";
                            gui.add(new JLabel(title), BorderLayout.NORTH);
                            final JPanel propertiesPanel = new JPanel(new SpringLayout());
                            propertiesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                            gui.add(propertiesPanel, BorderLayout.CENTER);

                            // name
                            JLabel label = new JLabel("Name: ", JLabel.TRAILING);
                            propertiesPanel.add(label);
                            final JTextField nameField = new JTextField(ns.getName(), 5);
                            label.setLabelFor(nameField);
                            propertiesPanel.add(nameField);

                            // name
                            label = new JLabel("File: ", JLabel.TRAILING);
                            propertiesPanel.add(label);
                            final JTextField fileField = new JTextField(Util.getFileName(ns.getSourceURL().getPath()), 5);
                            label.setLabelFor(fileField);
                            propertiesPanel.add(fileField);

                            // children count
                            label = new JLabel("Children: ", JLabel.TRAILING);
                            propertiesPanel.add(label);
                            final JTextField textField = new JTextField(n.getNumberOfChildren() + "", 5);
                            textField.setEditable(false);
                            label.setLabelFor(textField);
                            propertiesPanel.add(textField);

                            SpringUtilities.makeCompactGrid(propertiesPanel, 3, 2, 6, 6, 6, 6);

                            if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), gui, "Node Properties", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                                final String nodeName = nameField.getText();
                                if (nodeName != null && !nodeName.trim().equals("")) {
                                    n.setName(nodeName);
                                    f.getNodeState(n).setName(nodeName);
                                } else {
                                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "Node must have a name!", "Name Error", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    }
                }
            });

            popupMenuForMesh.add(miInfo);
            popupMenuForMesh.add(miCutNode);
            popupMenuForMesh.add(miPaste);
            popupMenuForMesh.add(miCopyNode);
            popupMenuForMesh.addSeparator();
            popupMenuForMesh.add(miAlignBottom);
            popupMenuForMesh.add(miAlignCenter);
            popupMenuForMesh.add(miMessThickness);
            popupMenuForMesh.add(miNodeProperties);
            popupMenuForMesh.addSeparator();
            popupMenuForMesh.add(miDeleteMesh);
            popupMenuForMesh.add(miReverseNormalVector);
            popupMenuForMesh.add(miRestoreDeletedMeshes);
            popupMenuForMesh.add(miMeshProperties);

        }

        return popupMenuForMesh;

    }

}