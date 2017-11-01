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
import javax.swing.JCheckBoxMenuItem;
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
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.MirrorAnnualAnalysis;
import org.concord.energy3d.simulation.MirrorDailyAnalysis;
import org.concord.energy3d.undo.ChangeAzimuthCommand;
import org.concord.energy3d.undo.ChangeAzimuthForAllMirrorsCommand;
import org.concord.energy3d.undo.ChangeBaseHeightCommand;
import org.concord.energy3d.undo.ChangeBaseHeightForAllMirrorsCommand;
import org.concord.energy3d.undo.ChangeFoundationMirrorAzimuthCommand;
import org.concord.energy3d.undo.ChangeFoundationMirrorBaseHeightCommand;
import org.concord.energy3d.undo.ChangeFoundationMirrorReflectanceCommand;
import org.concord.energy3d.undo.ChangeFoundationMirrorTargetCommand;
import org.concord.energy3d.undo.ChangeFoundationMirrorTiltAngleCommand;
import org.concord.energy3d.undo.ChangeMirrorReflectanceCommand;
import org.concord.energy3d.undo.ChangeMirrorTargetCommand;
import org.concord.energy3d.undo.ChangeReflectanceForAllMirrorsCommand;
import org.concord.energy3d.undo.ChangeTargetForAllMirrorsCommand;
import org.concord.energy3d.undo.ChangeTiltAngleCommand;
import org.concord.energy3d.undo.ChangeTiltAngleForAllMirrorsCommand;
import org.concord.energy3d.undo.SetMirrorLabelCommand;
import org.concord.energy3d.undo.SetPartSizeCommand;
import org.concord.energy3d.undo.SetSizeForAllMirrorsCommand;
import org.concord.energy3d.undo.SetSizeForMirrorsOnFoundationCommand;
import org.concord.energy3d.util.Util;

class PopupMenuForMirror extends PopupMenuFactory {

	private static JPopupMenu popupMenuForMirror;

	static JPopupMenu getPopupMenu() {

		if (popupMenuForMirror == null) {

			final JCheckBoxMenuItem cbmiDrawSunBeam = new JCheckBoxMenuItem("Draw Sun Beam");
			cbmiDrawSunBeam.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final Mirror m = (Mirror) selectedPart;
					m.setDrawSunBeam(cbmiDrawSunBeam.isSelected());
					m.draw();
					Scene.getInstance().setEdited(true);
				}
			});

			final JMenuItem miSetHeliostat = new JMenuItem("Set Heliostat...");
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
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Mirror", true);
					final JRadioButton rb2 = new JRadioButton("All Mirrors on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Mirrors");
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
					final JTextField inputField = new JTextField(m.getHeliostatTarget() != null ? m.getHeliostatTarget().getId() + "" : "");
					gui.add(inputField, BorderLayout.SOUTH);

					final String title = "<html>Set the ID of the foundation of the target tower for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The sunlight reflected by this mirror will focus on the top of the target foundation.<hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Heliostat Target");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							int id = 0;
							boolean ok = true;
							try {
								id = Integer.parseInt(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								final HousePart p = Scene.getInstance().getPart(id);
								if (id < 0) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "ID cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (!(p instanceof Foundation)) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "ID must be that of a foundation.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (p instanceof Foundation) {
										final Foundation target = (Foundation) p;
										if (rb1.isSelected()) {
											final Foundation oldTarget = m.getHeliostatTarget();
											final ChangeMirrorTargetCommand c = new ChangeMirrorTargetCommand(m);
											m.setHeliostatTarget(target);
											m.draw();
											if (oldTarget != null) {
												oldTarget.drawSolarReceiver();
											}
											SceneManager.getInstance().getUndoManager().addEdit(c);
											selectedScopeIndex = 0;
										} else if (rb2.isSelected()) {
											final Foundation foundation = m.getTopContainer();
											final ChangeFoundationMirrorTargetCommand c = new ChangeFoundationMirrorTargetCommand(foundation);
											foundation.setTargetForMirrors(target);
											SceneManager.getInstance().getUndoManager().addEdit(c);
											selectedScopeIndex = 1;
										} else if (rb3.isSelected()) {
											final ChangeTargetForAllMirrorsCommand c = new ChangeTargetForAllMirrorsCommand();
											Scene.getInstance().setTargetForAllMirrors(target);
											SceneManager.getInstance().getUndoManager().addEdit(c);
											selectedScopeIndex = 2;
										}
										target.drawSolarReceiver();
										updateAfterEdit();
										if (choice == options[0]) {
											break;
										}
									}
								}
							}
						}
					}
				}
			});

			final JMenuItem miDisableHeliostat = new JMenuItem("Disable Heliostat...");
			miDisableHeliostat.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final Mirror m = (Mirror) selectedPart;
					final String partInfo = m.toString().substring(0, m.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Mirror", true);
					final JRadioButton rb2 = new JRadioButton("All Mirrors on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Mirrors");
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
					final String title = "<html>Disable heliostat for " + partInfo + "</html>";
					final String footnote = "<html><hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, panel }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Disable Heliostat");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final ChangeMirrorTargetCommand c = new ChangeMirrorTargetCommand(m);
								if (m.getHeliostatTarget() != null) {
									m.getHeliostatTarget().drawSolarReceiver();
								}
								m.setHeliostatTarget(null);
								m.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final Foundation foundation = m.getTopContainer();
								final ChangeFoundationMirrorTargetCommand c = new ChangeFoundationMirrorTargetCommand(foundation);
								foundation.setTargetForMirrors(null);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final ChangeTargetForAllMirrorsCommand c = new ChangeTargetForAllMirrorsCommand();
								Scene.getInstance().setTargetForAllMirrors(null);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 2;
							}
							updateAfterEdit();
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
					final String footnote = "<html><hr><font size=2>The tilt angle of a mirror is the angle between its surface and the base surface.<br>The tilt angle must be between -90&deg; and 90&deg;.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Mirror", true);
					final JRadioButton rb2 = new JRadioButton("All Mirrors on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Mirrors");
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
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Mirror Tilt Angle");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
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
									if (rb1.isSelected()) {
										final ChangeTiltAngleCommand c = new ChangeTiltAngleCommand(m);
										m.setTiltAngle(val);
										m.draw();
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = m.getTopContainer();
										final ChangeFoundationMirrorTiltAngleCommand c = new ChangeFoundationMirrorTiltAngleCommand(foundation);
										foundation.setZenithAngleForMirrors(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final ChangeTiltAngleForAllMirrorsCommand c = new ChangeTiltAngleForAllMirrorsCommand();
										Scene.getInstance().setTiltAngleForAllMirrors(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
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
					final JRadioButton rb1 = new JRadioButton("Only this Mirror", true);
					final JRadioButton rb2 = new JRadioButton("All Mirrors on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Mirrors");
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
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Mirror Azimuth");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
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
								if (rb1.isSelected()) {
									final ChangeAzimuthCommand c = new ChangeAzimuthCommand(mirror);
									mirror.setRelativeAzimuth(a);
									mirror.draw();
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 0;
								} else if (rb2.isSelected()) {
									final ChangeFoundationMirrorAzimuthCommand c = new ChangeFoundationMirrorAzimuthCommand(foundation);
									foundation.setAzimuthForMirrors(a);
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 1;
								} else if (rb3.isSelected()) {
									final ChangeAzimuthForAllMirrorsCommand c = new ChangeAzimuthForAllMirrorsCommand();
									Scene.getInstance().setAzimuthForAllMirrors(a);
									SceneManager.getInstance().getUndoManager().addEdit(c);
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
					final JRadioButton rb1 = new JRadioButton("Only this Mirror", true);
					final JRadioButton rb2 = new JRadioButton("All Mirrors on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Mirrors");
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
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Mirror Size");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
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
									if (rb1.isSelected()) {
										final SetPartSizeCommand c = new SetPartSizeCommand(m);
										m.setMirrorWidth(w);
										m.setMirrorHeight(h);
										m.draw();
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final SetSizeForMirrorsOnFoundationCommand c = new SetSizeForMirrorsOnFoundationCommand(foundation);
										foundation.setSizeForMirrors(w, h);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final SetSizeForAllMirrorsCommand c = new SetSizeForAllMirrorsCommand();
										Scene.getInstance().setSizeForAllMirrors(w, h);
										SceneManager.getInstance().getUndoManager().addEdit(c);
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
					final JRadioButton rb1 = new JRadioButton("Only this Mirror", true);
					final JRadioButton rb2 = new JRadioButton("All Mirrors on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Mirrors");
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
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Mirror Base Height");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
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
								if (rb1.isSelected()) {
									final ChangeBaseHeightCommand c = new ChangeBaseHeightCommand(m);
									m.setBaseHeight(val);
									m.draw();
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 0;
								} else if (rb2.isSelected()) {
									final ChangeFoundationMirrorBaseHeightCommand c = new ChangeFoundationMirrorBaseHeightCommand(foundation);
									foundation.setBaseHeightForMirrors(val);
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 1;
								} else if (rb3.isSelected()) {
									final ChangeBaseHeightForAllMirrorsCommand c = new ChangeBaseHeightForAllMirrorsCommand();
									Scene.getInstance().setBaseHeightForAllMirrors(val);
									SceneManager.getInstance().getUndoManager().addEdit(c);
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
							final SetMirrorLabelCommand c = new SetMirrorLabelCommand(m);
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
						final SetMirrorLabelCommand c = new SetMirrorLabelCommand(m);
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
						final SetMirrorLabelCommand c = new SetMirrorLabelCommand(m);
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
						final SetMirrorLabelCommand c = new SetMirrorLabelCommand(m);
						m.setLabelEnergyOutput(miLabelEnergyOutput.isSelected());
						m.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelEnergyOutput);

			popupMenuForMirror = createPopupMenu(true, true, new Runnable() {
				@Override
				public void run() {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final Mirror m = (Mirror) selectedPart;
					if (m.getHeliostatTarget() == null) {
						miZenith.setEnabled(true);
						miAzimuth.setEnabled(true);
						miDisableHeliostat.setEnabled(false);
					} else {
						miZenith.setEnabled(false);
						miAzimuth.setEnabled(false);
						miDisableHeliostat.setEnabled(true);
					}
					Util.selectSilently(cbmiDrawSunBeam, m.getDrawSunBeam());
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
					final JRadioButton rb1 = new JRadioButton("Only this Mirror", true);
					final JRadioButton rb2 = new JRadioButton("All Mirrors on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Mirrors");
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
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Mirror Reflectance");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
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
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Mirror reflectance must be between 50% and 99%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final ChangeMirrorReflectanceCommand c = new ChangeMirrorReflectanceCommand(m);
										m.setReflectance(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = m.getTopContainer();
										final ChangeFoundationMirrorReflectanceCommand c = new ChangeFoundationMirrorReflectanceCommand(foundation);
										foundation.setReflectanceForMirrors(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final ChangeReflectanceForAllMirrorsCommand c = new ChangeReflectanceForAllMirrorsCommand();
										Scene.getInstance().setReflectanceForAllMirrors(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
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

			popupMenuForMirror.addSeparator();
			popupMenuForMirror.add(miSetHeliostat);
			popupMenuForMirror.add(miDisableHeliostat);
			popupMenuForMirror.addSeparator();
			popupMenuForMirror.add(miZenith);
			popupMenuForMirror.add(miAzimuth);
			popupMenuForMirror.addSeparator();
			popupMenuForMirror.add(cbmiDrawSunBeam);
			popupMenuForMirror.add(labelMenu);
			popupMenuForMirror.addSeparator();
			popupMenuForMirror.add(miSize);
			popupMenuForMirror.add(miBaseHeight);
			popupMenuForMirror.add(miReflectance);
			popupMenuForMirror.addSeparator();

			JMenuItem mi = new JMenuItem("Daily Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Mirror) {
						new MirrorDailyAnalysis().show();
					}
				}
			});
			popupMenuForMirror.add(mi);

			mi = new JMenuItem("Annual Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Mirror) {
						new MirrorAnnualAnalysis().show();
					}
				}
			});
			popupMenuForMirror.add(mi);

		}

		return popupMenuForMirror;

	}

}
