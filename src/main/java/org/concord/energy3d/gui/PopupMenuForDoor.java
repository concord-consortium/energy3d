package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.EnergyAnnualAnalysis;
import org.concord.energy3d.simulation.EnergyDailyAnalysis;
import org.concord.energy3d.undo.ChangeDoorSizeOnWallCommand;
import org.concord.energy3d.undo.ChangeDoorTextureOnWallCommand;
import org.concord.energy3d.undo.ChangeTextureCommand;
import org.concord.energy3d.undo.SetPartSizeCommand;
import org.concord.energy3d.undo.SetSizeForDoorsOnFoundationCommand;
import org.concord.energy3d.undo.SetTextureForDoorsOnFoundationCommand;
import org.concord.energy3d.util.Util;

class PopupMenuForDoor extends PopupMenuFactory {

	private static JPopupMenu popupMenuForDoor;

	static JPopupMenu getPopupMenu() {

		if (popupMenuForDoor == null) {

			final JMenuItem miSize = new JMenuItem("Size...");
			miSize.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Door)) {
						return;
					}
					final Door door = (Door) selectedPart;
					final HousePart container = door.getContainer();
					final Foundation foundation = door.getTopContainer();
					final String partInfo = door.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
					gui.add(inputPanel, BorderLayout.CENTER);
					inputPanel.add(new JLabel("Width (m): "));
					final JTextField widthField = new JTextField(threeDecimalsFormat.format(door.getDoorWidth()));
					inputPanel.add(widthField);
					inputPanel.add(new JLabel("Height (m): "));
					final JTextField heightField = new JTextField(threeDecimalsFormat.format(door.getDoorHeight()));
					inputPanel.add(heightField);
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Door", true);
					final JRadioButton rb2 = new JRadioButton("All Doors on this Wall");
					final JRadioButton rb3 = new JRadioButton("All Doors of this Building");
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

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { "Set Size for " + partInfo, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Door Size");

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
									boolean changed = Math.abs(w - door.getDoorWidth()) > 0.000001 || Math.abs(h - door.getDoorHeight()) > 0.000001;
									if (rb1.isSelected()) {
										if (changed) {
											final SetPartSizeCommand c = new SetPartSizeCommand(door);
											door.setDoorWidth(w);
											door.setDoorHeight(h);
											door.draw();
											door.getContainer().draw();
											SceneManager.getInstance().refresh();
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										if (!changed) {
											if (door.getContainer() instanceof Wall) {
												final Wall wall = (Wall) door.getContainer();
												for (final Door x : wall.getDoors()) {
													if (Math.abs(w - x.getDoorWidth()) > 0.000001 || Math.abs(h - x.getDoorHeight()) > 0.000001) {
														changed = true;
														break;
													}
												}
											}
										}
										if (changed) {
											if (door.getContainer() instanceof Wall) {
												final Wall wall = (Wall) door.getContainer();
												final ChangeDoorSizeOnWallCommand c = new ChangeDoorSizeOnWallCommand(wall);
												wall.setDoorSize(w, h);
												SceneManager.getInstance().getUndoManager().addEdit(c);
											}
										}
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										if (!changed) {
											for (final Door x : foundation.getDoors()) {
												if (Math.abs(w - x.getDoorWidth()) > 0.000001 || Math.abs(h - x.getDoorHeight()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final SetSizeForDoorsOnFoundationCommand c = new SetSizeForDoorsOnFoundationCommand(foundation);
											foundation.setSizeForDoors(w, h);
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

			final JMenu textureMenu = new JMenu("Texture");
			final ButtonGroup textureGroup = new ButtonGroup();
			final JRadioButtonMenuItem rbmiTextureNone = createTextureMenuItem(Door.TEXTURE_NONE, null);
			final JRadioButtonMenuItem rbmiTextureEdge = createTextureMenuItem(Door.TEXTURE_EDGE, null);
			final JRadioButtonMenuItem rbmiTexture01 = createTextureMenuItem(Door.TEXTURE_01, "icons/door_01.png");
			final JRadioButtonMenuItem rbmiTexture02 = createTextureMenuItem(Door.TEXTURE_02, "icons/door_02.png");
			final JRadioButtonMenuItem rbmiTexture03 = createTextureMenuItem(Door.TEXTURE_03, "icons/door_03.png");
			final JRadioButtonMenuItem rbmiTexture04 = createTextureMenuItem(Door.TEXTURE_04, "icons/door_04.png");
			final JRadioButtonMenuItem rbmiTexture05 = createTextureMenuItem(Door.TEXTURE_05, "icons/door_05.png");
			final JRadioButtonMenuItem rbmiTexture06 = createTextureMenuItem(Door.TEXTURE_06, "icons/door_06.png");
			final JRadioButtonMenuItem rbmiTexture07 = createTextureMenuItem(Door.TEXTURE_07, "icons/door_07.png");
			final JRadioButtonMenuItem rbmiTexture08 = createTextureMenuItem(Door.TEXTURE_08, "icons/door_08.png");
			textureGroup.add(rbmiTextureNone);
			textureGroup.add(rbmiTextureEdge);
			textureGroup.add(rbmiTexture01);
			textureGroup.add(rbmiTexture02);
			textureGroup.add(rbmiTexture03);
			textureGroup.add(rbmiTexture04);
			textureGroup.add(rbmiTexture05);
			textureGroup.add(rbmiTexture06);
			textureGroup.add(rbmiTexture07);
			textureGroup.add(rbmiTexture08);
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
			textureMenu.add(rbmiTexture08);

			textureMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(final MenuEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Door)) {
						return;
					}
					final Door door = (Door) selectedPart;
					switch (door.getTextureType()) {
					case Door.TEXTURE_EDGE:
						Util.selectSilently(rbmiTextureEdge, true);
						break;
					case Door.TEXTURE_NONE:
						Util.selectSilently(rbmiTextureNone, true);
						break;
					case Door.TEXTURE_02:
						Util.selectSilently(rbmiTexture02, true);
						break;
					case Door.TEXTURE_03:
						Util.selectSilently(rbmiTexture03, true);
						break;
					case Door.TEXTURE_04:
						Util.selectSilently(rbmiTexture04, true);
						break;
					case Door.TEXTURE_05:
						Util.selectSilently(rbmiTexture05, true);
						break;
					case Door.TEXTURE_06:
						Util.selectSilently(rbmiTexture06, true);
						break;
					case Door.TEXTURE_07:
						Util.selectSilently(rbmiTexture07, true);
						break;
					case Door.TEXTURE_08:
						Util.selectSilently(rbmiTexture08, true);
						break;
					default:
						textureGroup.clearSelection();
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

			popupMenuForDoor = createPopupMenu(false, false, null);
			popupMenuForDoor.addSeparator();
			popupMenuForDoor.add(miSize);
			popupMenuForDoor.add(colorAction);
			popupMenuForDoor.add(textureMenu);
			popupMenuForDoor.add(createInsulationMenuItem(true));
			popupMenuForDoor.add(createVolumetricHeatCapacityMenuItem());
			popupMenuForDoor.addSeparator();

			JMenuItem mi = new JMenuItem("Daily Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (EnergyPanel.getInstance().adjustCellSize()) {
						return;
					}
					if (SceneManager.getInstance().getSelectedPart() instanceof Door) {
						new EnergyDailyAnalysis().show("Daily Energy for Door");
					}
				}
			});
			popupMenuForDoor.add(mi);

			mi = new JMenuItem("Annual Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (EnergyPanel.getInstance().adjustCellSize()) {
						return;
					}
					if (SceneManager.getInstance().getSelectedPart() instanceof Door) {
						new EnergyAnnualAnalysis().show("Annual Energy for Door");
					}
				}
			});
			popupMenuForDoor.add(mi);

		}

		return popupMenuForDoor;

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
					if (!(selectedPart instanceof Door)) {
						return;
					}
					final Door door = (Door) selectedPart;
					final HousePart container = door.getContainer();
					final Foundation foundation = door.getTopContainer();
					final String partInfo = door.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Door", true);
					final JRadioButton rb2 = new JRadioButton("All Doors on this Wall");
					final JRadioButton rb3 = new JRadioButton("All Doors on this Foundation");
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

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { "Set Texture for " + partInfo, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Door Texture");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							if (rb1.isSelected()) {
								final ChangeTextureCommand c = new ChangeTextureCommand(door);
								door.setTextureType(type);
								door.draw();
								SceneManager.getInstance().refresh();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								if (container instanceof Wall) {
									final Wall wall = (Wall) container;
									final ChangeDoorTextureOnWallCommand c = new ChangeDoorTextureOnWallCommand(wall);
									wall.setDoorTexture(type);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetTextureForDoorsOnFoundationCommand c = new SetTextureForDoorsOnFoundationCommand(foundation);
								foundation.setTextureForDoors(type);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 2;
							}
							updateAfterEdit();
							if (MainPanel.getInstance().getEnergyButton().isSelected()) {
								MainPanel.getInstance().getEnergyButton().setSelected(false);
							}
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
