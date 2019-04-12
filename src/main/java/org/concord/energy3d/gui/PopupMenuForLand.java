package org.concord.energy3d.gui;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.AnnualGraph;
import org.concord.energy3d.undo.ChangeBackgroundAlbedoCommand;
import org.concord.energy3d.undo.ChangeGroundThermalDiffusivityCommand;
import org.concord.energy3d.undo.ChangeSnowReflectionFactorCommand;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.FileChooser;
import org.concord.energy3d.util.SpringUtilities;
import org.concord.energy3d.util.Util;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;

class PopupMenuForLand extends PopupMenuFactory {

	private static JPopupMenu popupMenuForLand;

	static JPopupMenu getPopupMenu(final MouseEvent e) {

		if (e.isShiftDown()) {
			SceneManager.getTaskManager().update(() -> {
				Scene.getInstance().pasteToPickedLocationOnLand();
				Scene.getInstance().setEdited(true);
				return null;
			});
			return null;
		}

		if (popupMenuForLand == null) {

			final JMenuItem miInfo = new JMenuItem("Land");
			miInfo.setEnabled(false);
			miInfo.setOpaque(true);
			miInfo.setBackground(Config.isMac() ? Color.DARK_GRAY : Color.GRAY);
			miInfo.setForeground(Color.WHITE);

			final JMenuItem miPaste = new JMenuItem("Paste");
			miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			miPaste.addActionListener(event -> SceneManager.getTaskManager().update(() -> {
				Scene.getInstance().pasteToPickedLocationOnLand();
				return null;
			}));

			final JMenuItem miRemoveAllTrees = new JMenuItem("Remove All Trees");
			miRemoveAllTrees.addActionListener(event -> SceneManager.getTaskManager().update(() -> {
				Scene.getInstance().removeAllTrees();
				EventQueue.invokeLater(() -> {
					MainPanel.getInstance().getEnergyButton().setSelected(false);
					Scene.getInstance().setEdited(true);
				});
				return null;
			}));

			final JMenuItem miRemoveAllHumans = new JMenuItem("Remove All Humans");
			miRemoveAllHumans.addActionListener(event -> SceneManager.getTaskManager().update(() -> {
				Scene.getInstance().removeAllHumans();
				EventQueue.invokeLater(() -> {
					MainPanel.getInstance().getEnergyButton().setSelected(false);
					Scene.getInstance().setEdited(true);
				});
				return null;
			}));

			final JMenuItem miRemoveAllBuildings = new JMenuItem("Remove All Foundations");
			miRemoveAllBuildings.addActionListener(event -> SceneManager.getTaskManager().update(() -> {
				Scene.getInstance().removeAllFoundations();
				EventQueue.invokeLater(() -> {
					MainPanel.getInstance().getEnergyButton().setSelected(false);
					Scene.getInstance().setEdited(true);
				});
				return null;
			}));

			final JMenuItem miImportEnergy3D = new JMenuItem("Import...");
			miImportEnergy3D.setToolTipText("Import the content in an existing Energy3D file into the clicked location on the land as the center");
			miImportEnergy3D.addActionListener(e15 -> MainFrame.getInstance().importFile());

			final JMenuItem miImportCollada = new JMenuItem("Import Collada...");
			miImportCollada.setToolTipText("Import the content in an existing Collada file into the clicked location on the land as the center");
			miImportCollada.addActionListener(e16 -> MainFrame.getInstance().importColladaFile());

			final JMenu miImportPrefabMenu = new JMenu("Import a Prefab");
			addPrefabMenuItem("Back Hip Roof Porch", "prefabs/back-hip-roof-porch.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Balcony", "prefabs/balcony1.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Bell Tower", "prefabs/bell-tower.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Box", "prefabs/box.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Chimney", "prefabs/chimney.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Connecting Porch", "prefabs/connecting-porch.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Cylinder Tower", "prefabs/cylinder-tower.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Fence", "prefabs/fence1.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Flat-Top Porch", "prefabs/flat-top-porch.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Fountain", "prefabs/fountain.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Front Door Overhang", "prefabs/front-door-overhang.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Gable Dormer", "prefabs/gable-dormer.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Hexagonal Gazebo", "prefabs/hexagonal-gazebo.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Hexagonal Tower", "prefabs/hexagonal-tower.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Lighthouse", "prefabs/lighthouse.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Octagonal Tower", "prefabs/octagonal-tower.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Round Tower", "prefabs/round-tower.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Shed Dormer", "prefabs/shed-dormer.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Solarium", "prefabs/solarium1.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Square Tower", "prefabs/square-tower.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Stair", "prefabs/stair1.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Tall Front Door Overhang", "prefabs/tall-front-door-overhang.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Temple Front", "prefabs/temple-front.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Waterfront Deck", "prefabs/waterfront-deck.ng3", miImportPrefabMenu);

			final JMenuItem miAlbedo = new JMenuItem("Albedo...");
			miAlbedo.addActionListener(event -> {
				final String title = "<html>Background Albedo (dimensionless [0, 1])<hr><font size=2>Examples:<br>0.17 (soil), 0.25 (grass), 0.40 (sand), 0.55 (concrete), snow (0.9)</html>";
				while (true) {
					final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), title, Scene.getInstance().getGround().getAlbedo());
					if (newValue == null) {
						break;
					} else {
						try {
							final double val = Double.parseDouble(newValue);
							if (val < 0 || val > 1) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Albedo value must be in 0-1.", "Range Error", JOptionPane.ERROR_MESSAGE);
							} else {
								if (val != Scene.getInstance().getGround().getAlbedo()) {
									final ChangeBackgroundAlbedoCommand c = new ChangeBackgroundAlbedoCommand();
									Scene.getInstance().getGround().setAlbedo(val);
									updateAfterEdit();
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								break;
							}
						} catch (final NumberFormatException exception) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});

			final JMenuItem miSnowReflection = new JMenuItem("Snow Reflection...");
			miSnowReflection.addActionListener(event -> {
				final JPanel gui = new JPanel(new BorderLayout());
				final String title = "<html>Increase of indirect solar radiation due to snow reflection<br>(a dimensionless parameter within [0, 0.2])</html>";
				gui.add(new JLabel(title), BorderLayout.NORTH);
				final JPanel inputPanel = new JPanel(new SpringLayout());
				inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
				gui.add(inputPanel, BorderLayout.CENTER);
				final JTextField[] fields = new JTextField[12];
				for (int i = 0; i < 12; i++) {
					final JLabel l = new JLabel(AnnualGraph.THREE_LETTER_MONTH[i] + ": ", JLabel.TRAILING);
					inputPanel.add(l);
					fields[i] = new JTextField(threeDecimalsFormat.format(Scene.getInstance().getGround().getSnowReflectionFactor(i)), 5);
					l.setLabelFor(fields[i]);
					inputPanel.add(fields[i]);
				}
				SpringUtilities.makeCompactGrid(inputPanel, 12, 2, 6, 6, 6, 6);
				while (true) {
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), gui, "Snow reflection factor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.CANCEL_OPTION) {
						break;
					}
					boolean pass = true;
					final double[] val = new double[12];
					for (int i = 0; i < 12; i++) {
						try {
							val[i] = Double.parseDouble(fields[i].getText());
							if (val[i] < 0 || val[i] > 0.2) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Snow reflection factor must be in 0-0.2.", "Range Error", JOptionPane.ERROR_MESSAGE);
								pass = false;
							}
						} catch (final NumberFormatException exception) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), fields[i].getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							pass = false;
						}
					}
					if (pass) {
						final ChangeSnowReflectionFactorCommand c = new ChangeSnowReflectionFactorCommand();
						for (int i = 0; i < 12; i++) {
							Scene.getInstance().getGround().setSnowReflectionFactor(val[i], i);
						}
						updateAfterEdit();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						break;
					}
				}
			});

			final JMenuItem miThermalDiffusivity = new JMenuItem("Ground Thermal Diffusivity...");
			miThermalDiffusivity.addActionListener(event -> {
				final String title = "<html>Ground Thermal Diffusivity (m<sup>2</sup>/s)<hr><font size=2>Examples:<br>0.039 (sand), 0.046 (clay), 0.05 (silt)</html>";
				while (true) {
					final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), title, Scene.getInstance().getGround().getThermalDiffusivity());
					if (newValue == null) {
						break;
					} else {
						try {
							final double val = Double.parseDouble(newValue);
							if (val <= 0) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Ground thermal diffusivity must be positive.", "Range Error", JOptionPane.ERROR_MESSAGE);
							} else {
								if (val != Scene.getInstance().getGround().getThermalDiffusivity()) {
									final ChangeGroundThermalDiffusivityCommand c = new ChangeGroundThermalDiffusivityCommand();
									Scene.getInstance().getGround().setThermalDiffusivity(val);
									updateAfterEdit();
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								break;
							}
						} catch (final NumberFormatException exception) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});

			final JMenuItem miClearImage = new JMenuItem("Clear Image");
			final JMenuItem miRescaleImage = new JMenuItem("Rescale Image...");
			final JCheckBoxMenuItem miShowImage = new JCheckBoxMenuItem("Show Image");

			final JMenu groundImageMenu = new JMenu("Ground Image");
			groundImageMenu.addMenuListener(new MenuListener() {
				@Override
				public void menuCanceled(final MenuEvent e) {
					miShowImage.setEnabled(true);
					miClearImage.setEnabled(true);
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					miShowImage.setEnabled(true);
					miClearImage.setEnabled(true);
				}

				@Override
				public void menuSelected(final MenuEvent e) {
					final boolean hasGroundImage = Scene.getInstance().isGroundImageEnabled();
					miShowImage.setEnabled(hasGroundImage);
					miClearImage.setEnabled(hasGroundImage);
					Util.selectSilently(miShowImage, SceneManager.getInstance().getGroundImageLand().isVisible());
				}
			});

			final JMenuItem miUseEarthView = new JMenuItem("Use Image from Earth View...");
			miUseEarthView.addActionListener(event -> new MapDialog(MainFrame.getInstance()).setVisible(true));
			groundImageMenu.add(miUseEarthView);

			final JMenuItem miUseImageFile = new JMenuItem("Use Image from File...");
			miUseImageFile.addActionListener(event -> {
				final File file = FileChooser.getInstance().showDialog(".png", FileChooser.pngFilter, false);
				if (file == null) {
					return;
				}
				try {
					Scene.getInstance().setGroundImage(ImageIO.read(file), 1);
					Scene.getInstance().setGroundImageEarthView(false);
				} catch (final Throwable t) {
					t.printStackTrace();
					JOptionPane.showMessageDialog(MainFrame.getInstance(), t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
				Scene.getInstance().setEdited(true);
			});
			groundImageMenu.add(miUseImageFile);
			groundImageMenu.addSeparator();

			miRescaleImage.addActionListener(event -> {
				final String title = "Scale the ground image";
				while (true) {
					final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), title, Scene.getInstance().getGroundImageScale());
					if (newValue == null) {
						break;
					} else {
						try {
							final double val = Double.parseDouble(newValue);
							if (val <= 0) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "The scaling factor must be positive.", "Range Error", JOptionPane.ERROR_MESSAGE);
							} else {
								// final ChangeGroundThermalDiffusivityCommand c = new ChangeGroundThermalDiffusivityCommand();
								Scene.getInstance().setGroundImageScale(val);
								// SceneManager.getInstance().getUndoManager().addEdit(c);
								break;
							}
						} catch (final NumberFormatException exception) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				Scene.getInstance().setEdited(true);
			});
			groundImageMenu.add(miRescaleImage);

			miClearImage.addActionListener(event -> {
				Scene.getInstance().setGroundImage(null, 1);
				Scene.getInstance().setEdited(true);
			});
			groundImageMenu.add(miClearImage);

			miShowImage.addItemListener(event -> {
				final boolean b = miShowImage.isSelected();
				SceneManager.getInstance().getGroundImageLand().setVisible(b);
				Scene.getInstance().setShowGroundImage(b);
				Scene.getInstance().setEdited(true);
				SceneManager.getInstance().refresh();
			});
			groundImageMenu.add(miShowImage);

			popupMenuForLand = new JPopupMenu();
			popupMenuForLand.setInvoker(MainPanel.getInstance().getCanvasPanel());
			popupMenuForLand.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
					final HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
					miPaste.setEnabled(copyBuffer instanceof Tree || copyBuffer instanceof Human || copyBuffer instanceof Foundation);
				}

				@Override
				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				}

				@Override
				public void popupMenuCanceled(final PopupMenuEvent e) {
				}

			});

			popupMenuForLand.add(miInfo);
			// popupMenuForLand.addSeparator();
			popupMenuForLand.add(miPaste);
			popupMenuForLand.add(miRemoveAllTrees);
			popupMenuForLand.add(miRemoveAllHumans);
			popupMenuForLand.add(miRemoveAllBuildings);
			popupMenuForLand.addSeparator();
			popupMenuForLand.add(miImportEnergy3D);
			popupMenuForLand.add(miImportCollada);
			popupMenuForLand.add(miImportPrefabMenu);
			popupMenuForLand.addSeparator();
			popupMenuForLand.add(groundImageMenu);
			popupMenuForLand.add(colorAction);
			popupMenuForLand.add(miAlbedo);
			popupMenuForLand.add(miSnowReflection);
			popupMenuForLand.add(miThermalDiffusivity);

		}

		return popupMenuForLand;

	}

}