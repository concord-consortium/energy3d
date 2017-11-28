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
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.FresnelReflectorAnnualAnalysis;
import org.concord.energy3d.simulation.FresnelReflectorDailyAnalysis;
import org.concord.energy3d.undo.ChangeAbsorberForAllFresnelReflectorsCommand;
import org.concord.energy3d.undo.ChangeAbsorptanceForAllFresnelReflectorsCommand;
import org.concord.energy3d.undo.ChangeBaseHeightCommand;
import org.concord.energy3d.undo.ChangeBaseHeightForAllFresnelReflectorsCommand;
import org.concord.energy3d.undo.ChangeFoundationFresnelReflectorAbsorberCommand;
import org.concord.energy3d.undo.ChangeFoundationFresnelReflectorAbsorptanceCommand;
import org.concord.energy3d.undo.ChangeFoundationFresnelReflectorBaseHeightCommand;
import org.concord.energy3d.undo.ChangeFoundationFresnelReflectorReflectanceCommand;
import org.concord.energy3d.undo.ChangeFresnelReflectorAbsorberCommand;
import org.concord.energy3d.undo.ChangeFresnelReflectorAbsorptanceCommand;
import org.concord.energy3d.undo.ChangeFresnelReflectorReflectanceCommand;
import org.concord.energy3d.undo.ChangeReflectanceForAllFresnelReflectorsCommand;
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

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { "Set mesh for " + partInfo, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
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
										r.draw();
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										foundation.setSectionsForFresnelReflectors(nSectionLength, nSectionWidth);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										Scene.getInstance().setSectionsForAllFresnelReflectors(nSectionLength, nSectionWidth);
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

			final JCheckBoxMenuItem cbmiDrawBeam = new JCheckBoxMenuItem("Draw Sun Beam");
			cbmiDrawBeam.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof FresnelReflector)) {
						return;
					}
					final FresnelReflector r = (FresnelReflector) selectedPart;
					final ShowSunBeamCommand c = new ShowSunBeamCommand(r);
					r.setSunBeamVisible(cbmiDrawBeam.isSelected());
					r.drawSunBeam();
					r.draw();
					SceneManager.getInstance().refresh();
					SceneManager.getInstance().getUndoManager().addEdit(c);
					Scene.getInstance().setEdited(true);
				}
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
					final JTextField inputField = new JTextField(r.getAbsorber() != null ? r.getAbsorber().getId() + "" : "");
					gui.add(inputField, BorderLayout.SOUTH);

					final String title = "<html>Set the ID of the absorber foundation for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The sunlight reflected by this Fresnel reflector will focus on the top of<br>the target foundation, where the absorber tube is located.<hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Absorber");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
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
										boolean changed = target != r.getAbsorber();
										if (rb1.isSelected()) {
											if (changed) {
												final Foundation oldTarget = r.getAbsorber();
												final ChangeFresnelReflectorAbsorberCommand c = new ChangeFresnelReflectorAbsorberCommand(r);
												r.setAbsorber(target);
												r.draw();
												if (oldTarget != null) {
													oldTarget.drawSolarReceiver();
												}
												SceneManager.getInstance().getUndoManager().addEdit(c);
											}
											selectedScopeIndex = 0;
										} else if (rb2.isSelected()) {
											final Foundation foundation = r.getTopContainer();
											if (!changed) {
												for (final FresnelReflector x : foundation.getFresnelReflectors()) {
													if (x.getAbsorber() != target) {
														changed = true;
														break;
													}
												}
											}
											if (changed) {
												final ChangeFoundationFresnelReflectorAbsorberCommand c = new ChangeFoundationFresnelReflectorAbsorberCommand(foundation);
												foundation.setAbsorberForFresnelReflectors(target);
												SceneManager.getInstance().getUndoManager().addEdit(c);
											}
											selectedScopeIndex = 1;
										} else if (rb3.isSelected()) {
											if (!changed) {
												for (final FresnelReflector x : Scene.getInstance().getAllFresnelReflectors()) {
													if (x.getAbsorber() != target) {
														changed = true;
														break;
													}
												}
											}
											if (changed) {
												final ChangeAbsorberForAllFresnelReflectorsCommand c = new ChangeAbsorberForAllFresnelReflectorsCommand();
												Scene.getInstance().setAbsorberForAllFresnelReflectors(target);
												SceneManager.getInstance().getUndoManager().addEdit(c);
											}
											selectedScopeIndex = 2;
										}
										if (changed) {
											target.drawSolarReceiver();
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

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { "Set length for " + partInfo, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
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
									if (rb1.isSelected()) {
										if (changed) {
											final SetPartSizeCommand c = new SetPartSizeCommand(r);
											r.setLength(length);
											r.ensureFullModules(false);
											r.draw();
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
											foundation.setSizeForFresnelReflectors(length, r.getModuleWidth(), r.getModuleLength());
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
											Scene.getInstance().setSizeForAllFresnelReflectors(length, r.getModuleWidth(), r.getModuleLength());
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

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { "Set module width for " + partInfo, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
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
								if (moduleWidth < 1 || moduleWidth > 20) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Module width must be between 1 and 20 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									boolean changed = moduleWidth != r.getModuleWidth();
									if (rb1.isSelected()) {
										if (changed) {
											final SetPartSizeCommand c = new SetPartSizeCommand(r);
											r.setModuleWidth(moduleWidth);
											r.ensureFullModules(false);
											r.draw();
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
											foundation.setSizeForFresnelReflectors(r.getLength(), moduleWidth, r.getModuleLength());
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
											Scene.getInstance().setSizeForAllFresnelReflectors(r.getLength(), moduleWidth, r.getModuleLength());
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

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { "Set module length for " + partInfo, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
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
								if (moduleLength < 1 || moduleLength > 50) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Module length must be between 1 and 50 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									boolean changed = moduleLength != r.getModuleLength();
									if (rb1.isSelected()) {
										if (changed) {
											final SetPartSizeCommand c = new SetPartSizeCommand(r);
											r.setModuleLength(moduleLength);
											r.ensureFullModules(false);
											r.draw();
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
											foundation.setSizeForFresnelReflectors(r.getLength(), r.getModuleWidth(), moduleLength);
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
											Scene.getInstance().setSizeForAllFresnelReflectors(r.getLength(), r.getModuleWidth(), moduleLength);
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
					if (!(selectedPart instanceof FresnelReflector)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final FresnelReflector r = (FresnelReflector) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final String title = "<html>Base Height (m) of " + partInfo + "</html>";
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
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(r.getBaseHeight() * Scene.getInstance().getAnnotationScale()));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Fresnel Reflector Base Height");

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
								boolean changed = val != r.getBaseHeight();
								if (rb1.isSelected()) {
									if (changed) {
										final ChangeBaseHeightCommand c = new ChangeBaseHeightCommand(r);
										r.setBaseHeight(val);
										r.draw();
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									selectedScopeIndex = 0;
								} else if (rb2.isSelected()) {
									if (!changed) {
										for (final FresnelReflector x : foundation.getFresnelReflectors()) {
											if (x.getBaseHeight() != val) {
												changed = true;
												break;
											}
										}
									}
									if (changed) {
										final ChangeFoundationFresnelReflectorBaseHeightCommand c = new ChangeFoundationFresnelReflectorBaseHeightCommand(foundation);
										foundation.setBaseHeightForFresnelReflectors(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									selectedScopeIndex = 1;
								} else if (rb3.isSelected()) {
									if (!changed) {
										for (final FresnelReflector x : Scene.getInstance().getAllFresnelReflectors()) {
											if (x.getBaseHeight() != val) {
												changed = true;
												break;
											}
										}
									}
									if (changed) {
										final ChangeBaseHeightForAllFresnelReflectorsCommand c = new ChangeBaseHeightForAllFresnelReflectorsCommand();
										Scene.getInstance().setBaseHeightForAllFresnelReflectors(val);
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
						if (selectedPart instanceof FresnelReflector) {
							final FresnelReflector r = (FresnelReflector) selectedPart;
							final SetFresnelReflectorLabelCommand c = new SetFresnelReflectorLabelCommand(r);
							r.clearLabels();
							r.draw();
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
					if (selectedPart instanceof FresnelReflector) {
						final FresnelReflector r = (FresnelReflector) selectedPart;
						final SetFresnelReflectorLabelCommand c = new SetFresnelReflectorLabelCommand(r);
						r.setLabelCustom(miLabelCustom.isSelected());
						if (r.getLabelCustom()) {
							r.setLabelCustomText(JOptionPane.showInputDialog(MainFrame.getInstance(), "Custom Text", r.getLabelCustomText()));
						}
						r.draw();
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
					if (selectedPart instanceof FresnelReflector) {
						final FresnelReflector r = (FresnelReflector) selectedPart;
						final SetFresnelReflectorLabelCommand c = new SetFresnelReflectorLabelCommand(r);
						r.setLabelId(miLabelId.isSelected());
						r.draw();
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
					if (selectedPart instanceof FresnelReflector) {
						final FresnelReflector r = (FresnelReflector) selectedPart;
						final SetFresnelReflectorLabelCommand c = new SetFresnelReflectorLabelCommand(r);
						r.setLabelEnergyOutput(miLabelEnergyOutput.isSelected());
						r.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelEnergyOutput);

			popupMenuForFresnelReflector = createPopupMenu(true, true, new Runnable() {
				@Override
				public void run() {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof FresnelReflector)) {
						return;
					}
					final FresnelReflector r = (FresnelReflector) selectedPart;
					Util.selectSilently(cbmiDrawBeam, r.isSunBeamVisible());
					Util.selectSilently(miLabelNone, !r.isLabelVisible());
					Util.selectSilently(miLabelCustom, r.getLabelCustom());
					Util.selectSilently(miLabelId, r.getLabelId());
					Util.selectSilently(miLabelEnergyOutput, r.getLabelEnergyOutput());
				}
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

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
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
											final ChangeFresnelReflectorReflectanceCommand c = new ChangeFresnelReflectorReflectanceCommand(r);
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
											final ChangeFoundationFresnelReflectorReflectanceCommand c = new ChangeFoundationFresnelReflectorReflectanceCommand(foundation);
											foundation.setReflectanceForFresnelReflectors(val * 0.01);
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
											final ChangeReflectanceForAllFresnelReflectorsCommand c = new ChangeReflectanceForAllFresnelReflectorsCommand();
											Scene.getInstance().setReflectanceForAllFresnelReflectors(val * 0.01);
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

			final JMenuItem miAbsorptance = new JMenuItem("Absorptance...");
			miAbsorptance.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof FresnelReflector)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final FresnelReflector r = (FresnelReflector) selectedPart;
					final String title = "<html>Absorptance (%) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2><hr></html>";
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
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(r.getAbsorptance() * 100));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Fresnel Reflector Absorber Tube Absorptance");

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
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Fresnel reflector absorptance must be between 50% and 99%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									boolean changed = Math.abs(val * 0.01 - r.getAbsorptance()) > 0.000001;
									if (rb1.isSelected()) {
										if (changed) {
											final ChangeFresnelReflectorAbsorptanceCommand c = new ChangeFresnelReflectorAbsorptanceCommand(r);
											r.setAbsorptance(val * 0.01);
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = r.getTopContainer();
										if (!changed) {
											for (final FresnelReflector x : foundation.getFresnelReflectors()) {
												if (Math.abs(x.getAbsorptance() - val * 0.01) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final ChangeFoundationFresnelReflectorAbsorptanceCommand c = new ChangeFoundationFresnelReflectorAbsorptanceCommand(foundation);
											foundation.setAbsorptanceForFresnelReflectors(val * 0.01);
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										if (!changed) {
											for (final FresnelReflector x : Scene.getInstance().getAllFresnelReflectors()) {
												if (Math.abs(x.getAbsorptance() - val * 0.01) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final ChangeAbsorptanceForAllFresnelReflectorsCommand c = new ChangeAbsorptanceForAllFresnelReflectorsCommand();
											Scene.getInstance().setAbsorptanceForAllFresnelReflectors(val * 0.01);
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

			popupMenuForFresnelReflector.addSeparator();
			popupMenuForFresnelReflector.add(miSetAbsorber);
			popupMenuForFresnelReflector.addSeparator();
			popupMenuForFresnelReflector.add(cbmiDrawBeam);
			popupMenuForFresnelReflector.add(labelMenu);
			popupMenuForFresnelReflector.addSeparator();
			popupMenuForFresnelReflector.add(miLength);
			popupMenuForFresnelReflector.add(miModuleWidth);
			popupMenuForFresnelReflector.add(miModuleLength);
			popupMenuForFresnelReflector.add(miBaseHeight);
			popupMenuForFresnelReflector.addSeparator();
			popupMenuForFresnelReflector.add(miReflectance);
			popupMenuForFresnelReflector.add(miAbsorptance);
			popupMenuForFresnelReflector.addSeparator();
			popupMenuForFresnelReflector.add(miMesh);
			popupMenuForFresnelReflector.addSeparator();

			JMenuItem mi = new JMenuItem("Daily Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof FresnelReflector) {
						new FresnelReflectorDailyAnalysis().show();
					}
				}
			});
			popupMenuForFresnelReflector.add(mi);

			mi = new JMenuItem("Annual Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof FresnelReflector) {
						new FresnelReflectorAnnualAnalysis().show();
					}
				}
			});
			popupMenuForFresnelReflector.add(mi);

		}

		return popupMenuForFresnelReflector;

	}

}
