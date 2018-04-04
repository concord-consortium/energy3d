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
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.HeliostatAnnualAnalysis;
import org.concord.energy3d.simulation.HeliostatDailyAnalysis;
import org.concord.energy3d.undo.ChangeAzimuthCommand;
import org.concord.energy3d.undo.ChangeAzimuthForAllHeliostatsCommand;
import org.concord.energy3d.undo.ChangeBaseHeightCommand;
import org.concord.energy3d.undo.ChangeBaseHeightForAllSolarCollectorsCommand;
import org.concord.energy3d.undo.ChangeFoundationHeliostatAzimuthCommand;
import org.concord.energy3d.undo.ChangeFoundationHeliostatTargetCommand;
import org.concord.energy3d.undo.ChangeFoundationHeliostatTiltAngleCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarCollectorBaseHeightCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarReflectorOpticalEfficiencyCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarReflectorReflectanceCommand;
import org.concord.energy3d.undo.ChangeHeliostatTargetCommand;
import org.concord.energy3d.undo.ChangeHeliostatTextureCommand;
import org.concord.energy3d.undo.ChangeOpticalEfficiencyForAllSolarReflectorsCommand;
import org.concord.energy3d.undo.ChangeReflectanceForAllSolarReflectorsCommand;
import org.concord.energy3d.undo.ChangeSolarReceiverEfficiencyCommand;
import org.concord.energy3d.undo.ChangeSolarReflectorOpticalEfficiencyCommand;
import org.concord.energy3d.undo.ChangeSolarReflectorReflectanceCommand;
import org.concord.energy3d.undo.ChangeTargetForAllHeliostatsCommand;
import org.concord.energy3d.undo.ChangeTiltAngleCommand;
import org.concord.energy3d.undo.ChangeTiltAngleForAllHeliostatsCommand;
import org.concord.energy3d.undo.LockEditPointsCommand;
import org.concord.energy3d.undo.LockEditPointsForClassCommand;
import org.concord.energy3d.undo.LockEditPointsOnFoundationCommand;
import org.concord.energy3d.undo.SetHeliostatLabelCommand;
import org.concord.energy3d.undo.SetPartSizeCommand;
import org.concord.energy3d.undo.SetSizeForAllHeliostatsCommand;
import org.concord.energy3d.undo.SetSizeForHeliostatsOnFoundationCommand;
import org.concord.energy3d.undo.ShowSunBeamCommand;
import org.concord.energy3d.util.Util;

class PopupMenuForHeliostat extends PopupMenuFactory {

	private static JPopupMenu popupMenuForHeliostat;

	static JPopupMenu getPopupMenu() {

		if (popupMenuForHeliostat == null) {

			final JCheckBoxMenuItem cbmiDisableEditPoint = new JCheckBoxMenuItem("Disable Edit Point");
			cbmiDisableEditPoint.addItemListener(new ItemListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void itemStateChanged(final ItemEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final boolean disabled = cbmiDisableEditPoint.isSelected();
					final Mirror m = (Mirror) selectedPart;
					final String partInfo = m.toString().substring(0, m.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout(0, 20));
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.SOUTH);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Heliostat", true);
					final JRadioButton rb2 = new JRadioButton("All Heliostats on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Heliostats");
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

					final String title = "<html>" + (disabled ? "Disable" : "Enable") + " edit point for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>Disable the edit point of a heliostat prevents it from<br>being unintentionally moved.<hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[0]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), (disabled ? "Disable" : "Enable") + " Edit Point");
					dialog.setVisible(true);
					if (optionPane.getValue() == options[0]) {
						if (rb1.isSelected()) {
							final LockEditPointsCommand c = new LockEditPointsCommand(m);
							m.setLockEdit(disabled);
							SceneManager.getInstance().getUndoManager().addEdit(c);
							selectedScopeIndex = 0;
						} else if (rb2.isSelected()) {
							final Foundation foundation = m.getTopContainer();
							final LockEditPointsOnFoundationCommand c = new LockEditPointsOnFoundationCommand(foundation, m.getClass());
							foundation.setLockEditForClass(disabled, m.getClass());
							SceneManager.getInstance().getUndoManager().addEdit(c);
							selectedScopeIndex = 1;
						} else if (rb3.isSelected()) {
							final LockEditPointsForClassCommand c = new LockEditPointsForClassCommand(m);
							Scene.getInstance().setLockEditForClass(disabled, m.getClass());
							SceneManager.getInstance().getUndoManager().addEdit(c);
							selectedScopeIndex = 2;
						}
						SceneManager.getInstance().refresh();
						Scene.getInstance().setEdited(true);
					}
				}

			});

			final JCheckBoxMenuItem cbmiDrawSunBeam = new JCheckBoxMenuItem("Draw Sun Beam");
			cbmiDrawSunBeam.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final Mirror m = (Mirror) selectedPart;
					final ShowSunBeamCommand c = new ShowSunBeamCommand(m);
					m.setSunBeamVisible(cbmiDrawSunBeam.isSelected());
					m.draw();
					SceneManager.getInstance().refresh();
					SceneManager.getInstance().getUndoManager().addEdit(c);
					Scene.getInstance().setEdited(true);
				}
			});

			final JMenuItem miSetHeliostat = new JMenuItem("Set Target Tower...");
			miSetHeliostat.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final Mirror m = (Mirror) selectedPart;
					final String partInfo = m.toString().substring(0, m.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout(0, 20));
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.SOUTH);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Heliostat", true);
					final JRadioButton rb2 = new JRadioButton("All Heliostats on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Heliostats");
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
					final JComboBox<String> comboBox = new JComboBox<String>();
					comboBox.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(final ItemEvent e) {

						}
					});
					comboBox.addItem("None");
					for (final Foundation x : foundations) {
						if (!x.getChildren().isEmpty()) {
							comboBox.addItem(x.getId() + "");
						}
					}
					if (m.getReceiver() != null) {
						comboBox.setSelectedItem(m.getReceiver().getId() + "");
					}
					gui.add(comboBox, BorderLayout.CENTER);

					final String title = "<html>Select the ID of the foundation<br>of the target tower for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The sunlight reflected by the mirror(s) of this heliostat will<br>focus on the top of the target tower.<hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Heliostat Target");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							Foundation target = null;
							if (comboBox.getSelectedIndex() > 0) {
								boolean ok = true;
								int id = -1;
								try {
									id = Integer.parseInt((String) comboBox.getSelectedItem());
								} catch (final NumberFormatException exception) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), comboBox.getSelectedItem() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
									ok = false;
								}
								if (ok) {
									final HousePart p = Scene.getInstance().getPart(id);
									if (p instanceof Foundation) {
										target = (Foundation) p;
									} else {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "ID must be that of a foundation.", "ID Error", JOptionPane.ERROR_MESSAGE);
									}
								}
							}
							boolean changed = target != m.getReceiver();
							if (rb1.isSelected()) {
								if (changed) {
									final Foundation oldTarget = m.getReceiver();
									final ChangeHeliostatTargetCommand c = new ChangeHeliostatTargetCommand(m);
									m.setReceiver(target);
									m.draw();
									if (oldTarget != null) {
										oldTarget.drawSolarReceiver();
									}
									SceneManager.getInstance().refresh();
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final Foundation foundation = m.getTopContainer();
								if (!changed) {
									for (final Mirror x : foundation.getHeliostats()) {
										if (target != x.getReceiver()) {
											changed = true;
											break;
										}
									}
								}
								if (changed) {
									final ChangeFoundationHeliostatTargetCommand c = new ChangeFoundationHeliostatTargetCommand(foundation);
									foundation.setTargetForHeliostats(target);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								if (!changed) {
									for (final Mirror x : Scene.getInstance().getAllHeliostats()) {
										if (target != x.getReceiver()) {
											changed = true;
											break;
										}
									}
								}
								if (changed) {
									final ChangeTargetForAllHeliostatsCommand c = new ChangeTargetForAllHeliostatsCommand();
									Scene.getInstance().setTargetForAllHeliostats(target);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								selectedScopeIndex = 2;
							}
							if (changed) {
								if (target != null) {
									target.drawSolarReceiver();
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

			final JMenuItem miZenith = new JMenuItem("Tilt Angle...");
			miZenith.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final Mirror m = (Mirror) selectedPart;
					final String partInfo = m.toString().substring(0, m.toString().indexOf(')') + 1);
					final String title = "<html>Tilt Angle of " + partInfo + " (&deg;)</html>";
					final String footnote = "<html><hr><font size=2>The tilt angle of the mirror of a heliostat is the angle between its surface and the base surface.<br>The tilt angle must be between -90&deg; and 90&deg;.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Heliostat", true);
					final JRadioButton rb2 = new JRadioButton("All Heliostats on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Heliostats");
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
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(m.getTiltAngle()));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Heliostat Mirror Tilt Angle");

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
									boolean changed = Math.abs(val - m.getTiltAngle()) > 0.000001;
									if (rb1.isSelected()) {
										if (changed) {
											final ChangeTiltAngleCommand c = new ChangeTiltAngleCommand(m);
											m.setTiltAngle(val);
											m.draw();
											SceneManager.getInstance().refresh();
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = m.getTopContainer();
										if (!changed) {
											for (final Mirror x : foundation.getHeliostats()) {
												if (Math.abs(val - x.getTiltAngle()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final ChangeFoundationHeliostatTiltAngleCommand c = new ChangeFoundationHeliostatTiltAngleCommand(foundation);
											foundation.setTiltAngleForHeliostats(val);
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										if (!changed) {
											for (final Mirror x : Scene.getInstance().getAllHeliostats()) {
												if (Math.abs(val - x.getTiltAngle()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final ChangeTiltAngleForAllHeliostatsCommand c = new ChangeTiltAngleForAllHeliostatsCommand();
											Scene.getInstance().setTiltAngleForAllHeliostats(val);
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
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Mirror mirror = (Mirror) selectedPart;
					final Foundation foundation = mirror.getTopContainer();
					final String title = "<html>Azimuth Angle (&deg;) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The azimuth angle is measured clockwise from the true north.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Heliostat", true);
					final JRadioButton rb2 = new JRadioButton("All Heliostats on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Heliostats");
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
					double a = mirror.getRelativeAzimuth() + foundation.getAzimuth();
					if (a > 360) {
						a -= 360;
					}
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(a));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Heliostat Azimuth");

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
								boolean changed = Math.abs(a - mirror.getRelativeAzimuth()) > 0.000001;
								if (rb1.isSelected()) {
									if (changed) {
										final ChangeAzimuthCommand c = new ChangeAzimuthCommand(mirror);
										mirror.setRelativeAzimuth(a);
										mirror.draw();
										SceneManager.getInstance().refresh();
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									selectedScopeIndex = 0;
								} else if (rb2.isSelected()) {
									if (!changed) {
										for (final Mirror x : foundation.getHeliostats()) {
											if (Math.abs(a - x.getRelativeAzimuth()) > 0.000001) {
												changed = true;
												break;
											}
										}
									}
									if (changed) {
										final ChangeFoundationHeliostatAzimuthCommand c = new ChangeFoundationHeliostatAzimuthCommand(foundation);
										foundation.setAzimuthForHeliostats(a);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									selectedScopeIndex = 1;
								} else if (rb3.isSelected()) {
									if (!changed) {
										for (final Mirror x : Scene.getInstance().getAllHeliostats()) {
											if (Math.abs(a - x.getRelativeAzimuth()) > 0.000001) {
												changed = true;
												break;
											}
										}
									}
									if (changed) {
										final ChangeAzimuthForAllHeliostatsCommand c = new ChangeAzimuthForAllHeliostatsCommand();
										Scene.getInstance().setAzimuthForAllHeliostats(a);
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

			final JMenuItem miSize = new JMenuItem("Size...");
			miSize.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final Mirror m = (Mirror) selectedPart;
					final Foundation foundation = m.getTopContainer();
					final String partInfo = m.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
					gui.add(inputPanel, BorderLayout.CENTER);
					inputPanel.add(new JLabel("Width: "));
					final JTextField widthField = new JTextField(threeDecimalsFormat.format(m.getMirrorWidth()));
					inputPanel.add(widthField);
					inputPanel.add(new JLabel("Length: "));
					final JTextField heightField = new JTextField(threeDecimalsFormat.format(m.getMirrorHeight()));
					inputPanel.add(heightField);
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Heliostat", true);
					final JRadioButton rb2 = new JRadioButton("All Heliostats on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Heliostats");
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
					final JOptionPane optionPane = new JOptionPane(new Object[] { "Set size for " + partInfo, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Heliostat Size");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							double w = 0, h = 0;
							boolean ok = true;
							try {
								w = Double.parseDouble(widthField.getText());
								h = Double.parseDouble(heightField.getText());
							} catch (final NumberFormatException x) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (w < 1 || w > 50) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Width must be between 1 and 50 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (h < 1 || h > 50) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Height must be between 1 and 50 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									boolean changed = Math.abs(w - m.getMirrorWidth()) > 0.000001 || Math.abs(h - m.getMirrorHeight()) > 0.000001;
									if (rb1.isSelected()) {
										if (changed) {
											final SetPartSizeCommand c = new SetPartSizeCommand(m);
											m.setMirrorWidth(w);
											m.setMirrorHeight(h);
											m.draw();
											SceneManager.getInstance().refresh();
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										if (!changed) {
											for (final Mirror x : foundation.getHeliostats()) {
												if (Math.abs(w - x.getMirrorWidth()) > 0.000001 || Math.abs(h - x.getMirrorHeight()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final SetSizeForHeliostatsOnFoundationCommand c = new SetSizeForHeliostatsOnFoundationCommand(foundation);
											foundation.setSizeForHeliostats(w, h);
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										if (!changed) {
											for (final Mirror x : Scene.getInstance().getAllHeliostats()) {
												if (Math.abs(w - x.getMirrorWidth()) > 0.000001 || Math.abs(h - x.getMirrorHeight()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final SetSizeForAllHeliostatsCommand c = new SetSizeForAllHeliostatsCommand();
											Scene.getInstance().setSizeForAllHeliostats(w, h);
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

			final JMenuItem miBaseHeight = new JMenuItem("Base Height...");
			miBaseHeight.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Mirror m = (Mirror) selectedPart;
					final Foundation foundation = m.getTopContainer();
					final String title = "<html>Base Height (m) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Heliostat", true);
					final JRadioButton rb2 = new JRadioButton("All Heliostats on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Heliostats");
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
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(m.getBaseHeight() * Scene.getInstance().getAnnotationScale()));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Heliostat Base Height");

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
								val = Double.parseDouble(inputField.getText()) / Scene.getInstance().getAnnotationScale();
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								boolean changed = Math.abs(val - m.getBaseHeight()) > 0.000001;
								if (rb1.isSelected()) {
									if (changed) {
										final ChangeBaseHeightCommand c = new ChangeBaseHeightCommand(m);
										m.setBaseHeight(val);
										m.draw();
										SceneManager.getInstance().refresh();
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									selectedScopeIndex = 0;
								} else if (rb2.isSelected()) {
									if (!changed) {
										for (final Mirror x : foundation.getHeliostats()) {
											if (Math.abs(val - x.getBaseHeight()) > 0.000001) {
												changed = true;
												break;
											}
										}
									}
									if (changed) {
										final ChangeFoundationSolarCollectorBaseHeightCommand c = new ChangeFoundationSolarCollectorBaseHeightCommand(foundation, m.getClass());
										foundation.setBaseHeightForHeliostats(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									selectedScopeIndex = 1;
								} else if (rb3.isSelected()) {
									if (!changed) {
										for (final Mirror x : Scene.getInstance().getAllHeliostats()) {
											if (Math.abs(val - x.getBaseHeight()) > 0.000001) {
												changed = true;
												break;
											}
										}
									}
									if (changed) {
										final ChangeBaseHeightForAllSolarCollectorsCommand c = new ChangeBaseHeightForAllSolarCollectorsCommand(m.getClass());
										Scene.getInstance().setBaseHeightForAllHeliostats(val);
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
			miLabelNone.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (miLabelNone.isSelected()) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Mirror) {
							final Mirror m = (Mirror) selectedPart;
							final SetHeliostatLabelCommand c = new SetHeliostatLabelCommand(m);
							m.clearLabels();
							m.draw();
							SceneManager.getInstance().getUndoManager().addEdit(c);
							Scene.getInstance().setEdited(true);
							SceneManager.getInstance().refresh();
						}
					}
				}
			});
			labelMenu.add(miLabelNone);

			final JCheckBoxMenuItem miLabelCustom = new JCheckBoxMenuItem("Custom");
			miLabelCustom.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Mirror) {
						final Mirror m = (Mirror) selectedPart;
						final SetHeliostatLabelCommand c = new SetHeliostatLabelCommand(m);
						m.setLabelCustom(miLabelCustom.isSelected());
						if (m.getLabelCustom()) {
							m.setLabelCustomText(JOptionPane.showInputDialog(MainFrame.getInstance(), "Custom Text", m.getLabelCustomText()));
						}
						m.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelCustom);

			final JCheckBoxMenuItem miLabelId = new JCheckBoxMenuItem("ID");
			miLabelId.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Mirror) {
						final Mirror m = (Mirror) selectedPart;
						final SetHeliostatLabelCommand c = new SetHeliostatLabelCommand(m);
						m.setLabelId(miLabelId.isSelected());
						m.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelId);

			final JCheckBoxMenuItem miLabelEnergyOutput = new JCheckBoxMenuItem("Energy Output");
			miLabelEnergyOutput.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Mirror) {
						final Mirror m = (Mirror) selectedPart;
						final SetHeliostatLabelCommand c = new SetHeliostatLabelCommand(m);
						m.setLabelEnergyOutput(miLabelEnergyOutput.isSelected());
						m.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelEnergyOutput);

			final JMenu textureMenu = new JMenu("Texture");

			final ButtonGroup textureButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem texture1MenuItem = new JRadioButtonMenuItem("Whole Mirror");
			texture1MenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final ChangeHeliostatTextureCommand c = new ChangeHeliostatTextureCommand();
						Scene.getInstance().setHeliostatTextureType(Mirror.TEXTURE_ONE_MIRROR);
						Scene.getInstance().setEdited(true);
						if (MainPanel.getInstance().getEnergyButton().isSelected()) {
							MainPanel.getInstance().getEnergyButton().setSelected(false);
						}
						Scene.getInstance().redrawAll();
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			textureButtonGroup.add(texture1MenuItem);
			textureMenu.add(texture1MenuItem);

			final JRadioButtonMenuItem texture2MenuItem = new JRadioButtonMenuItem("2 \u00D7 1 Mirrors");
			texture2MenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final ChangeHeliostatTextureCommand c = new ChangeHeliostatTextureCommand();
						Scene.getInstance().setHeliostatTextureType(Mirror.TEXTURE_2X1_MIRRORS);
						Scene.getInstance().setEdited(true);
						if (MainPanel.getInstance().getEnergyButton().isSelected()) {
							MainPanel.getInstance().getEnergyButton().setSelected(false);
						}
						Scene.getInstance().redrawAll();
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			textureButtonGroup.add(texture2MenuItem);
			textureMenu.add(texture2MenuItem);

			final JRadioButtonMenuItem texture3MenuItem = new JRadioButtonMenuItem("1 \u00D7 2 Mirrors");
			texture3MenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final ChangeHeliostatTextureCommand c = new ChangeHeliostatTextureCommand();
						Scene.getInstance().setHeliostatTextureType(Mirror.TEXTURE_1X2_MIRRORS);
						Scene.getInstance().setEdited(true);
						if (MainPanel.getInstance().getEnergyButton().isSelected()) {
							MainPanel.getInstance().getEnergyButton().setSelected(false);
						}
						Scene.getInstance().redrawAll();
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			textureButtonGroup.add(texture3MenuItem);
			textureMenu.add(texture3MenuItem);

			final JRadioButtonMenuItem texture4MenuItem = new JRadioButtonMenuItem("7 \u00D7 5 Mirrors");
			texture4MenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final ChangeHeliostatTextureCommand c = new ChangeHeliostatTextureCommand();
						Scene.getInstance().setHeliostatTextureType(Mirror.TEXTURE_7X5_MIRRORS);
						Scene.getInstance().setEdited(true);
						if (MainPanel.getInstance().getEnergyButton().isSelected()) {
							MainPanel.getInstance().getEnergyButton().setSelected(false);
						}
						Scene.getInstance().redrawAll();
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			textureButtonGroup.add(texture4MenuItem);
			textureMenu.add(texture4MenuItem);

			textureMenu.addMenuListener(new MenuListener() {
				@Override
				public void menuCanceled(final MenuEvent e) {
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					SceneManager.getInstance().refresh();
				}

				@Override
				public void menuSelected(final MenuEvent e) {
					textureButtonGroup.clearSelection();
					switch (Scene.getInstance().getHeliostatTextureType()) {
					default:
						Util.selectSilently(texture1MenuItem, true);
						break;
					case Mirror.TEXTURE_2X1_MIRRORS:
						Util.selectSilently(texture2MenuItem, true);
						break;
					case Mirror.TEXTURE_1X2_MIRRORS:
						Util.selectSilently(texture3MenuItem, true);
						break;
					case Mirror.TEXTURE_7X5_MIRRORS:
						Util.selectSilently(texture4MenuItem, true);
						break;
					}
				}
			});

			popupMenuForHeliostat = createPopupMenu(true, true, new Runnable() {
				@Override
				public void run() {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final Mirror m = (Mirror) selectedPart;
					if (m.getReceiver() == null) {
						miZenith.setEnabled(true);
						miAzimuth.setEnabled(true);
					} else {
						miZenith.setEnabled(false);
						miAzimuth.setEnabled(false);
					}
					Util.selectSilently(cbmiDisableEditPoint, m.getLockEdit());
					Util.selectSilently(cbmiDrawSunBeam, m.isSunBeamVisible());
					Util.selectSilently(miLabelNone, !m.isLabelVisible());
					Util.selectSilently(miLabelCustom, m.getLabelCustom());
					Util.selectSilently(miLabelId, m.getLabelId());
					Util.selectSilently(miLabelEnergyOutput, m.getLabelEnergyOutput());
				}
			});

			final JMenuItem miReflectance = new JMenuItem("Reflectance...");
			miReflectance.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Mirror m = (Mirror) selectedPart;
					final String title = "<html>Reflectance (%) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>Reflectance can be affected by pollen and dust.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Heliostat", true);
					final JRadioButton rb2 = new JRadioButton("All Heliostats on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Heliostats");
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
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(m.getReflectance() * 100));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Heliostat Mirror Reflectance");

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
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Heliostat mirror reflectance must be between 50% and 99%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									boolean changed = Math.abs(val * 0.01 - m.getReflectance()) > 0.000001;
									if (rb1.isSelected()) {
										if (changed) {
											final ChangeSolarReflectorReflectanceCommand c = new ChangeSolarReflectorReflectanceCommand(m);
											m.setReflectance(val * 0.01);
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = m.getTopContainer();
										if (!changed) {
											for (final Mirror x : foundation.getHeliostats()) {
												if (Math.abs(val * 0.01 - x.getReflectance()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final ChangeFoundationSolarReflectorReflectanceCommand c = new ChangeFoundationSolarReflectorReflectanceCommand(foundation, m.getClass());
											foundation.setReflectanceForHeliostatMirrors(val * 0.01);
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										if (!changed) {
											for (final Mirror x : Scene.getInstance().getAllHeliostats()) {
												if (Math.abs(val * 0.01 - x.getReflectance()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final ChangeReflectanceForAllSolarReflectorsCommand c = new ChangeReflectanceForAllSolarReflectorsCommand(m.getClass());
											Scene.getInstance().setReflectanceForAllSolarReflectors(val * 0.01, m.getClass());
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
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Mirror m = (Mirror) selectedPart;
					final String title = "<html>Aperture percentage of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The percentage of the effective area for reflection<br>after deducting the area of gaps, frames, etc.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Heliostat", true);
					final JRadioButton rb2 = new JRadioButton("All Heliostats on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Heliostats");
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
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(m.getOpticalEfficiency() * 100));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Aperture Percentage of Heliostat Surface");

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
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Heliostat aperature percentage must be between 70% and 100%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									boolean changed = Math.abs(val * 0.01 - m.getOpticalEfficiency()) > 0.000001;
									if (rb1.isSelected()) {
										if (changed) {
											final ChangeSolarReflectorOpticalEfficiencyCommand c = new ChangeSolarReflectorOpticalEfficiencyCommand(m);
											m.setOpticalEfficiency(val * 0.01);
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = m.getTopContainer();
										if (!changed) {
											for (final Mirror x : foundation.getHeliostats()) {
												if (Math.abs(val * 0.01 - x.getOpticalEfficiency()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final ChangeFoundationSolarReflectorOpticalEfficiencyCommand c = new ChangeFoundationSolarReflectorOpticalEfficiencyCommand(foundation, m.getClass());
											foundation.setOpticalEfficiencyForSolarReflectors(val * 0.01, m.getClass());
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										if (!changed) {
											for (final Mirror x : Scene.getInstance().getAllHeliostats()) {
												if (Math.abs(val * 0.01 - x.getOpticalEfficiency()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final ChangeOpticalEfficiencyForAllSolarReflectorsCommand c = new ChangeOpticalEfficiencyForAllSolarReflectorsCommand(m.getClass());
											Scene.getInstance().setOpticalEfficiencyForAllSolarReflectors(val * 0.01, m.getClass());
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

			final JMenuItem miConversionEfficiency = new JMenuItem("Central Receiver Conversion Efficiency...");
			miConversionEfficiency.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final Mirror m = (Mirror) selectedPart;
					final Foundation receiver = m.getReceiver();
					if (receiver == null) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "This heliostat does not link to a receiver.", "No Receiver", JOptionPane.ERROR_MESSAGE);
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final String title = "<html>Light-electricity conversion efficiency (%) of " + partInfo + "'s central receiver</html>";
					final String footnote = "<html><hr><font size=2><hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(receiver.getSolarReceiverEfficiency() * 100));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
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
									final boolean changed = Math.abs(val * 0.01 - receiver.getSolarReceiverEfficiency()) > 0.000001;
									if (changed) {
										final ChangeSolarReceiverEfficiencyCommand c = new ChangeSolarReceiverEfficiencyCommand(receiver);
										receiver.setSolarReceiverEfficiency(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
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

			popupMenuForHeliostat.addSeparator();
			popupMenuForHeliostat.add(miSetHeliostat);
			popupMenuForHeliostat.add(miZenith);
			popupMenuForHeliostat.add(miAzimuth);
			popupMenuForHeliostat.add(miSize);
			popupMenuForHeliostat.add(miBaseHeight);
			popupMenuForHeliostat.addSeparator();
			popupMenuForHeliostat.add(miReflectance);
			popupMenuForHeliostat.add(miApertureRatio);
			popupMenuForHeliostat.add(miConversionEfficiency);
			popupMenuForHeliostat.addSeparator();
			popupMenuForHeliostat.add(cbmiDisableEditPoint);
			popupMenuForHeliostat.add(cbmiDrawSunBeam);
			popupMenuForHeliostat.add(labelMenu);
			popupMenuForHeliostat.add(textureMenu);
			popupMenuForHeliostat.addSeparator();

			JMenuItem mi = new JMenuItem("Daily Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (EnergyPanel.getInstance().adjustCellSize()) {
						return;
					}
					if (SceneManager.getInstance().getSelectedPart() instanceof Mirror) {
						new HeliostatDailyAnalysis().show();
					}
				}
			});
			popupMenuForHeliostat.add(mi);

			mi = new JMenuItem("Annual Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (EnergyPanel.getInstance().adjustCellSize()) {
						return;
					}
					if (SceneManager.getInstance().getSelectedPart() instanceof Mirror) {
						new HeliostatAnnualAnalysis().show();
					}
				}
			});
			popupMenuForHeliostat.add(mi);

		}

		return popupMenuForHeliostat;

	}

}
