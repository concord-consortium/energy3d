package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Snap;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.EnergyAnnualAnalysis;
import org.concord.energy3d.simulation.EnergyDailyAnalysis;
import org.concord.energy3d.undo.ChangeBuildingTextureCommand;
import org.concord.energy3d.undo.ChangeFoundationWallHeightCommand;
import org.concord.energy3d.undo.ChangeFoundationWallThicknessCommand;
import org.concord.energy3d.undo.ChangeHeightForAllWallsCommand;
import org.concord.energy3d.undo.ChangeHeightForConnectedWallsCommand;
import org.concord.energy3d.undo.ChangeThicknessForAllWallsCommand;
import org.concord.energy3d.undo.ChangeWallHeightCommand;
import org.concord.energy3d.undo.ChangeWallThicknessCommand;
import org.concord.energy3d.undo.ChangeWallTypeCommand;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.Util;
import org.concord.energy3d.util.WallVisitor;

class PopupMenuForWall extends PopupMenuFactory {

	private static JPopupMenu popupMenuForWall;

	static JPopupMenu getPopupMenuForWall() {

		if (popupMenuForWall == null) {

			final JMenuItem miPaste = new JMenuItem("Paste");
			miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			miPaste.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().pasteToPickedLocationOnWall();
							Scene.getInstance().setEdited(true);
							return null;
						}
					});
				}
			});

			final JMenuItem miClear = new JMenuItem("Clear");
			miClear.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().removeAllChildren(SceneManager.getInstance().getSelectedPart());
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});

			final JMenuItem miDeleteAllConnectedWalls = new JMenuItem("Delete All Connected Walls");
			miDeleteAllConnectedWalls.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						Scene.getInstance().deleteAllConnectedWalls((Wall) selectedPart);
					}
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
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(w.getThickness() * Scene.getInstance().getAnnotationScale()));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
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
									val /= Scene.getInstance().getAnnotationScale();
									Wall.setDefaultThickess(val);
									boolean changed = Math.abs(val - w.getThickness()) > 0.000001;
									if (rb1.isSelected()) {
										if (changed) {
											final ChangeWallThicknessCommand c = new ChangeWallThicknessCommand(w);
											w.setThickness(val);
											w.draw();
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
											foundation.setThicknessOfWalls(val);
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
											Scene.getInstance().setThicknessForAllWalls(val);
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
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(w.getHeight() * Scene.getInstance().getAnnotationScale()));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
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
									val /= Scene.getInstance().getAnnotationScale();
									changed = Math.abs(val - w.getHeight()) > 0.000001;
									if (rb1.isSelected()) {
										if (changed) {
											final ChangeWallHeightCommand c = new ChangeWallHeightCommand(w);
											w.setHeight(val, true);
											Scene.getInstance().redrawAllWallsNow();
											SceneManager.getInstance().getUndoManager().addEdit(c);
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
															heliostat.drawSunBeam();
														}
													}
												}
											}
										}
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										if (!changed) {
											w.visitNeighbors(new WallVisitor() {
												@Override
												public void visit(final Wall currentWall, final Snap prev, final Snap next) {
													if (Math.abs(val - currentWall.getHeight()) > 0.000001) {
														changed = true;
													}
												}
											});
										}
										if (changed) {
											final ChangeHeightForConnectedWallsCommand c = new ChangeHeightForConnectedWallsCommand(w);
											Scene.getInstance().setHeightOfConnectedWalls(w, val);
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
											foundation.setHeightOfWalls(val);
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
											Scene.getInstance().setHeightForAllWalls(val);
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

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
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
								w.draw();
								// SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								// final ChangeHeightForConnectedWallsCommand c = new ChangeHeightForConnectedWallsCommand(w);
								Scene.getInstance().showOutlineOfConnectedWalls(w, miOutline.isSelected());
								// SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final Foundation foundation = w.getTopContainer();
								// final ChangeFoundationWallHeightCommand c = new ChangeFoundationWallHeightCommand(foundation);
								foundation.showOutlineOfWalls(miOutline.isSelected());
								// SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 2;
							} else if (rb4.isSelected()) {
								// final ChangeHeightForAllWallsCommand c = new ChangeHeightForAllWallsCommand(w);
								Scene.getInstance().showOutlineForAllWalls(miOutline.isSelected());
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

			popupMenuForWall = createPopupMenu(false, false, new Runnable() {
				@Override
				public void run() {
					final HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
					miPaste.setEnabled(copyBuffer instanceof Window || copyBuffer instanceof SolarPanel || copyBuffer instanceof Rack);
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						final Wall w = (Wall) selectedPart;
						Util.selectSilently(miOutline, w.outlineShown());
					}
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
			final ButtonGroup textureGroup = new ButtonGroup();

			final JRadioButtonMenuItem rbmiTextureNone = new JRadioButtonMenuItem("No Texture");
			rbmiTextureNone.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final ChangeBuildingTextureCommand c = new ChangeBuildingTextureCommand();
						Scene.getInstance().setTextureMode(TextureMode.None);
						Scene.getInstance().setEdited(true);
						if (MainPanel.getInstance().getEnergyButton().isSelected()) {
							MainPanel.getInstance().getEnergyButton().setSelected(false);
						}
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			textureGroup.add(rbmiTextureNone);
			textureMenu.add(rbmiTextureNone);

			final JRadioButtonMenuItem rbmiTextureOutline = new JRadioButtonMenuItem("Outline Texture");
			rbmiTextureOutline.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final ChangeBuildingTextureCommand c = new ChangeBuildingTextureCommand();
						Scene.getInstance().setTextureMode(TextureMode.Simple);
						Scene.getInstance().setEdited(true);
						if (MainPanel.getInstance().getEnergyButton().isSelected()) {
							MainPanel.getInstance().getEnergyButton().setSelected(false);
						}
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			textureGroup.add(rbmiTextureOutline);
			textureMenu.add(rbmiTextureOutline);
			textureMenu.addSeparator();

			final JRadioButtonMenuItem rbmiTexture01 = MainFrame.getInstance().createWallTextureMenuItem(Wall.TEXTURE_01, "icons/wall_01.png");
			final JRadioButtonMenuItem rbmiTexture02 = MainFrame.getInstance().createWallTextureMenuItem(Wall.TEXTURE_02, "icons/wall_02.png");
			final JRadioButtonMenuItem rbmiTexture03 = MainFrame.getInstance().createWallTextureMenuItem(Wall.TEXTURE_03, "icons/wall_03.png");
			final JRadioButtonMenuItem rbmiTexture04 = MainFrame.getInstance().createWallTextureMenuItem(Wall.TEXTURE_04, "icons/wall_04.png");
			final JRadioButtonMenuItem rbmiTexture05 = MainFrame.getInstance().createWallTextureMenuItem(Wall.TEXTURE_05, "icons/wall_05.png");
			final JRadioButtonMenuItem rbmiTexture06 = MainFrame.getInstance().createWallTextureMenuItem(Wall.TEXTURE_06, "icons/wall_06.png");
			final JRadioButtonMenuItem rbmiTexture07 = MainFrame.getInstance().createWallTextureMenuItem(Wall.TEXTURE_07, "icons/wall_07.png");
			textureGroup.add(rbmiTexture01);
			textureGroup.add(rbmiTexture02);
			textureGroup.add(rbmiTexture03);
			textureGroup.add(rbmiTexture04);
			textureGroup.add(rbmiTexture05);
			textureGroup.add(rbmiTexture06);
			textureGroup.add(rbmiTexture07);
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
					if (Scene.getInstance().getTextureMode() == TextureMode.None) {
						Util.selectSilently(rbmiTextureNone, true);
						return;
					}
					if (Scene.getInstance().getTextureMode() == TextureMode.Simple) {
						Util.selectSilently(rbmiTextureOutline, true);
						return;
					}
					switch (Scene.getInstance().getWallTextureType()) {
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
			rbmiSolidWall.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Wall) {
							final Wall wall = (Wall) selectedPart;
							final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
							wall.setType(Wall.SOLID_WALL);
							wall.draw();
							Scene.getInstance().setEdited(true);
							SceneManager.getInstance().getUndoManager().addEdit(c);
						}
					}
				}
			});
			typeMenu.add(rbmiSolidWall);
			typeGroup.add(rbmiSolidWall);

			final JRadioButtonMenuItem rbmiEmpty = new JRadioButtonMenuItem("Empty");
			rbmiEmpty.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Wall) {
							final Wall wall = (Wall) selectedPart;
							final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
							wall.setType(Wall.EMPTY);
							wall.draw();
							Scene.getInstance().setEdited(true);
							SceneManager.getInstance().getUndoManager().addEdit(c);
						}
					}
				}
			});
			typeMenu.add(rbmiEmpty);
			typeGroup.add(rbmiEmpty);

			final JRadioButtonMenuItem rbmiEdges = new JRadioButtonMenuItem("Vertical Edges");
			rbmiEdges.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Wall) {
							final Wall wall = (Wall) selectedPart;
							final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
							wall.setType(Wall.VERTICAL_EDGES_ONLY);
							wall.draw();
							Scene.getInstance().setEdited(true);
							SceneManager.getInstance().getUndoManager().addEdit(c);
						}
					}
				}
			});
			typeMenu.add(rbmiEdges);
			typeGroup.add(rbmiEdges);

			final JRadioButtonMenuItem rbmiColumns = new JRadioButtonMenuItem("Columns");
			rbmiColumns.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Wall) {
							final Wall wall = (Wall) selectedPart;
							final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
							wall.setType(Wall.COLUMNS_ONLY);
							wall.draw();
							Scene.getInstance().setEdited(true);
							SceneManager.getInstance().getUndoManager().addEdit(c);
						}
					}
				}
			});
			typeMenu.add(rbmiColumns);
			typeGroup.add(rbmiColumns);

			final JRadioButtonMenuItem rbmiRails = new JRadioButtonMenuItem("Rails");
			rbmiRails.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Wall) {
							final Wall wall = (Wall) selectedPart;
							final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
							wall.setType(Wall.RAILS_ONLY);
							wall.draw();
							Scene.getInstance().setEdited(true);
							SceneManager.getInstance().getUndoManager().addEdit(c);
						}
					}
				}
			});
			typeMenu.add(rbmiRails);
			typeGroup.add(rbmiRails);

			final JRadioButtonMenuItem rbmiColumnsAndRailings = new JRadioButtonMenuItem("Columns & Railings");
			rbmiColumnsAndRailings.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Wall) {
							final Wall wall = (Wall) selectedPart;
							final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
							wall.setType(Wall.COLUMNS_RAILS);
							wall.draw();
							Scene.getInstance().setEdited(true);
							SceneManager.getInstance().getUndoManager().addEdit(c);
						}
					}
				}
			});
			typeMenu.add(rbmiColumnsAndRailings);
			typeGroup.add(rbmiColumnsAndRailings);

			final JRadioButtonMenuItem rbmiFence = new JRadioButtonMenuItem("Fence");
			rbmiFence.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Wall) {
							final Wall wall = (Wall) selectedPart;
							final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
							wall.setType(Wall.FENCE);
							wall.draw();
							Scene.getInstance().setEdited(true);
							SceneManager.getInstance().getUndoManager().addEdit(c);
						}
					}
				}
			});
			typeMenu.add(rbmiFence);
			typeGroup.add(rbmiFence);

			final JRadioButtonMenuItem rbmiSteelFrame = new JRadioButtonMenuItem("Steel Frame");
			rbmiSteelFrame.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Wall) {
							final Wall wall = (Wall) selectedPart;
							final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
							wall.setType(Wall.STEEL_FRAME);
							wall.draw();
							Scene.getInstance().setEdited(true);
							SceneManager.getInstance().getUndoManager().addEdit(c);
						}
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
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (EnergyPanel.getInstance().adjustCellSize()) {
						return;
					}
					if (SceneManager.getInstance().getSelectedPart() instanceof Wall) {
						new EnergyDailyAnalysis().show("Daily Energy for Wall");
					}
				}
			});
			popupMenuForWall.add(mi);

			mi = new JMenuItem("Annual Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (EnergyPanel.getInstance().adjustCellSize()) {
						return;
					}
					if (SceneManager.getInstance().getSelectedPart() instanceof Wall) {
						new EnergyAnnualAnalysis().show("Annual Energy for Wall");
					}
				}
			});
			popupMenuForWall.add(mi);

		}

		return popupMenuForWall;

	}

}
