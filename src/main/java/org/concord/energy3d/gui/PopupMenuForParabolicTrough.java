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
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.ParabolicTroughAnnualAnalysis;
import org.concord.energy3d.simulation.ParabolicTroughDailyAnalysis;
import org.concord.energy3d.undo.ChangeAbsorptanceForAllSolarReflectorsCommand;
import org.concord.energy3d.undo.ChangeAzimuthCommand;
import org.concord.energy3d.undo.ChangeAzimuthForAllParabolicTroughsCommand;
import org.concord.energy3d.undo.ChangeBaseHeightCommand;
import org.concord.energy3d.undo.ChangeBaseHeightForAllSolarCollectorsCommand;
import org.concord.energy3d.undo.ChangeFoundationParabolicTroughAzimuthCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarCollectorBaseHeightCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarReflectorAbsorptanceCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarReflectorOpticalEfficiencyCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarReflectorReflectanceCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarReflectorThermalEfficiencyCommand;
import org.concord.energy3d.undo.ChangeOpticalEfficiencyForAllSolarReflectorsCommand;
import org.concord.energy3d.undo.ChangeReflectanceForAllSolarReflectorsCommand;
import org.concord.energy3d.undo.ChangeSolarReflectorAbsorptanceCommand;
import org.concord.energy3d.undo.ChangeSolarReflectorOpticalEfficiencyCommand;
import org.concord.energy3d.undo.ChangeSolarReflectorReflectanceCommand;
import org.concord.energy3d.undo.ChangeSolarReflectorThermalEfficiencyCommand;
import org.concord.energy3d.undo.ChangeThermalEfficiencyForAllSolarReflectorsCommand;
import org.concord.energy3d.undo.LockEditPointsCommand;
import org.concord.energy3d.undo.LockEditPointsForClassCommand;
import org.concord.energy3d.undo.LockEditPointsOnFoundationCommand;
import org.concord.energy3d.undo.SetParabolicTroughLabelCommand;
import org.concord.energy3d.undo.SetParabolicTroughSemilatusRectumCommand;
import org.concord.energy3d.undo.SetPartSizeCommand;
import org.concord.energy3d.undo.SetShapeForAllParabolicTroughsCommand;
import org.concord.energy3d.undo.SetShapeForParabolicTroughsOnFoundationCommand;
import org.concord.energy3d.undo.ShowSunBeamCommand;
import org.concord.energy3d.util.Util;

class PopupMenuForParabolicTrough extends PopupMenuFactory {

	private static JPopupMenu popupMenuForParabolicTrough;

	static JPopupMenu getPopupMenu() {

		if (popupMenuForParabolicTrough == null) {

			final JMenuItem miMesh = new JMenuItem("Mesh...");
			miMesh.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof ParabolicTrough)) {
						return;
					}
					final ParabolicTrough t = (ParabolicTrough) selectedPart;
					final Foundation foundation = t.getTopContainer();
					final String partInfo = t.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
					gui.add(inputPanel, BorderLayout.CENTER);
					inputPanel.add(new JLabel("Parabolic cross-section: "));
					final JTextField nParabolaField = new JTextField("" + t.getNSectionParabola());
					inputPanel.add(nParabolaField);
					inputPanel.add(new JLabel("Axial direction: "));
					final JTextField nAxisField = new JTextField("" + t.getNSectionAxis());
					inputPanel.add(nAxisField);
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Parabolic Trough", true);
					final JRadioButton rb2 = new JRadioButton("All Parabolic Troughs on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Parabolic Troughs");
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
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Parabolic Trough Mesh");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							int nSectionParabola = 0, nSectionAxis = 0;
							boolean ok = true;
							try {
								nSectionParabola = Integer.parseInt(nParabolaField.getText());
								nSectionAxis = Integer.parseInt(nAxisField.getText());
							} catch (final NumberFormatException nfe) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (nSectionParabola < 4) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Parabolic sections must be at least 4.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (nSectionAxis < 4) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Axis mesh must be at least 4.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (!Util.isPowerOfTwo(nSectionParabola) || !Util.isPowerOfTwo(nSectionAxis)) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Number of parabolic trough mesh sections in x or y direction must be power of two.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										// final SetPartSizeCommand c = new SetPartSizeCommand(t);
										t.setNSectionParabola(nSectionParabola);
										t.setNSectionAxis(nSectionAxis);
										t.draw();
										SceneManager.getInstance().refresh();
										// SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										// final SetShapeForParabolicTroughsOnFoundationCommand c = new SetShapeForParabolicTroughsOnFoundationCommand(foundation);
										foundation.setSectionsForParabolicTroughs(nSectionParabola, nSectionAxis);
										// SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										// final SetShapeForAllParabolicTroughsCommand c = new SetShapeForAllParabolicTroughsCommand();
										Scene.getInstance().setSectionsForAllParabolicTroughs(nSectionParabola, nSectionAxis);
										// SceneManager.getInstance().getUndoManager().addEdit(c);
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
					if (!(selectedPart instanceof ParabolicTrough)) {
						return;
					}
					final boolean disabled = cbmiDisableEditPoints.isSelected();
					final ParabolicTrough trough = (ParabolicTrough) selectedPart;
					final String partInfo = trough.toString().substring(0, trough.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout(0, 20));
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.SOUTH);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Parabolic Trough", true);
					final JRadioButton rb2 = new JRadioButton("All Parabolic Troughs on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Parabolic Troughs");
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
					final String footnote = "<html><hr><font size=2>Disable the edit points of a parabolic trough prevents it<br>from being unintentionally moved.<hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[0]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), (disabled ? "Disable" : "Enable") + " Edit Points");
					dialog.setVisible(true);
					if (optionPane.getValue() == options[0]) {
						if (rb1.isSelected()) {
							final LockEditPointsCommand c = new LockEditPointsCommand(trough);
							trough.setLockEdit(disabled);
							SceneManager.getInstance().getUndoManager().addEdit(c);
							selectedScopeIndex = 0;
						} else if (rb2.isSelected()) {
							final Foundation foundation = trough.getTopContainer();
							final LockEditPointsOnFoundationCommand c = new LockEditPointsOnFoundationCommand(foundation, trough.getClass());
							foundation.setLockEditForClass(disabled, trough.getClass());
							SceneManager.getInstance().getUndoManager().addEdit(c);
							selectedScopeIndex = 1;
						} else if (rb3.isSelected()) {
							final LockEditPointsForClassCommand c = new LockEditPointsForClassCommand(trough);
							Scene.getInstance().setLockEditForClass(disabled, trough.getClass());
							SceneManager.getInstance().getUndoManager().addEdit(c);
							selectedScopeIndex = 2;
						}
						SceneManager.getInstance().refresh();
						Scene.getInstance().setEdited(true);
					}
				}

			});

			final JCheckBoxMenuItem cbmiDrawSunBeams = new JCheckBoxMenuItem("Draw Sun Beams");
			cbmiDrawSunBeams.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof ParabolicTrough)) {
						return;
					}
					final ParabolicTrough t = (ParabolicTrough) selectedPart;
					final ShowSunBeamCommand c = new ShowSunBeamCommand(t);
					t.setSunBeamVisible(cbmiDrawSunBeams.isSelected());
					t.drawSunBeam();
					t.draw();
					SceneManager.getInstance().refresh();
					SceneManager.getInstance().getUndoManager().addEdit(c);
					Scene.getInstance().setEdited(true);
				}
			});

			final JMenuItem miTroughLength = new JMenuItem("Trough Length...");
			miTroughLength.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof ParabolicTrough)) {
						return;
					}
					final ParabolicTrough t = (ParabolicTrough) selectedPart;
					final Foundation foundation = t.getTopContainer();
					final String partInfo = t.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel inputPanel = new JPanel(new GridLayout(1, 2, 5, 5));
					gui.add(inputPanel, BorderLayout.CENTER);
					inputPanel.add(new JLabel("Trough Length (m): "));
					final JTextField lengthField = new JTextField(threeDecimalsFormat.format(t.getTroughLength()));
					inputPanel.add(lengthField);
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Parabolic Trough", true);
					final JRadioButton rb2 = new JRadioButton("All Parabolic Troughs on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Parabolic Troughs");
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
					final JOptionPane optionPane = new JOptionPane(new Object[] { "Set assembly length for " + partInfo, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Parabolic Trough Assembly Length");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							double l = 0;
							boolean ok = true;
							try {
								l = Double.parseDouble(lengthField.getText());
							} catch (final NumberFormatException x) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (l < 1 || l > 1000) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Parabolic trough length must be between 1 and 1000 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									boolean changed = Math.abs(l - t.getTroughLength()) > 0.000001;
									if (rb1.isSelected()) {
										if (changed) {
											final SetPartSizeCommand c = new SetPartSizeCommand(t);
											t.setTroughLength(l);
											t.ensureFullModules(false);
											t.draw();
											SceneManager.getInstance().refresh();
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										if (!changed) {
											for (final ParabolicTrough x : foundation.getParabolicTroughs()) {
												if (Math.abs(l - x.getTroughLength()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final SetShapeForParabolicTroughsOnFoundationCommand c = new SetShapeForParabolicTroughsOnFoundationCommand(foundation);
											foundation.setLengthForParabolicTroughs(l);
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										if (!changed) {
											for (final ParabolicTrough x : Scene.getInstance().getAllParabolicTroughs()) {
												if (Math.abs(l - x.getTroughLength()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final SetShapeForAllParabolicTroughsCommand c = new SetShapeForAllParabolicTroughsCommand();
											Scene.getInstance().setLengthForAllParabolicTroughs(l);
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

			final JMenuItem miApertureWidth = new JMenuItem("Aperture Width...");
			miApertureWidth.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof ParabolicTrough)) {
						return;
					}
					final ParabolicTrough t = (ParabolicTrough) selectedPart;
					final Foundation foundation = t.getTopContainer();
					final String partInfo = t.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel inputPanel = new JPanel(new GridLayout(1, 2, 5, 5));
					gui.add(inputPanel, BorderLayout.CENTER);
					inputPanel.add(new JLabel("Aperture Width (m): "));
					final JTextField widthField = new JTextField(threeDecimalsFormat.format(t.getApertureWidth()));
					inputPanel.add(widthField);
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Parabolic Trough", true);
					final JRadioButton rb2 = new JRadioButton("All Parabolic Troughs on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Parabolic Troughs");
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
					final JOptionPane optionPane = new JOptionPane(new Object[] { "Set aperture width for " + partInfo, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Parabolic Trough Aperture Width");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							double w = 0;
							boolean ok = true;
							try {
								w = Double.parseDouble(widthField.getText());
							} catch (final NumberFormatException x) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (w < 1 || w > 20) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Parabolic trough aperture width must be between 1 and 20 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									boolean changed = Math.abs(w - t.getApertureWidth()) > 0.000001;
									if (rb1.isSelected()) {
										if (changed) {
											final SetPartSizeCommand c = new SetPartSizeCommand(t);
											t.setApertureWidth(w);
											t.ensureFullModules(false);
											t.draw();
											SceneManager.getInstance().refresh();
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										if (!changed) {
											for (final ParabolicTrough x : foundation.getParabolicTroughs()) {
												if (Math.abs(w - x.getApertureWidth()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final SetShapeForParabolicTroughsOnFoundationCommand c = new SetShapeForParabolicTroughsOnFoundationCommand(foundation);
											foundation.setApertureForParabolicTroughs(w);
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										if (!changed) {
											for (final ParabolicTrough x : Scene.getInstance().getAllParabolicTroughs()) {
												if (Math.abs(w - x.getApertureWidth()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final SetShapeForAllParabolicTroughsCommand c = new SetShapeForAllParabolicTroughsCommand();
											Scene.getInstance().setApertureForAllParabolicTroughs(w);
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
					if (!(selectedPart instanceof ParabolicTrough)) {
						return;
					}
					final ParabolicTrough t = (ParabolicTrough) selectedPart;
					final Foundation foundation = t.getTopContainer();
					final String partInfo = t.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel inputPanel = new JPanel(new GridLayout(1, 2, 5, 5));
					gui.add(inputPanel, BorderLayout.CENTER);
					inputPanel.add(new JLabel("Module Length (m): "));
					final JTextField moduleLengthField = new JTextField(threeDecimalsFormat.format(t.getModuleLength()));
					inputPanel.add(moduleLengthField);
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Parabolic Trough", true);
					final JRadioButton rb2 = new JRadioButton("All Parabolic Troughs on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Parabolic Troughs");
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
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Parabolic Trough Module Length");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							double u = 0;
							boolean ok = true;
							try {
								u = Double.parseDouble(moduleLengthField.getText());
							} catch (final NumberFormatException x) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (u < 1 || u > 100) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar collector module length must be between 1 and 100 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									boolean changed = Math.abs(u - t.getModuleLength()) > 0.000001;
									if (rb1.isSelected()) {
										if (changed) {
											final SetPartSizeCommand c = new SetPartSizeCommand(t);
											t.setModuleLength(u);
											t.ensureFullModules(false);
											t.draw();
											SceneManager.getInstance().refresh();
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										if (!changed) {
											for (final ParabolicTrough x : foundation.getParabolicTroughs()) {
												if (Math.abs(u - x.getModuleLength()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final SetShapeForParabolicTroughsOnFoundationCommand c = new SetShapeForParabolicTroughsOnFoundationCommand(foundation);
											foundation.setModuleLengthForParabolicTroughs(u);
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										if (!changed) {
											for (final ParabolicTrough x : Scene.getInstance().getAllParabolicTroughs()) {
												if (Math.abs(u - x.getModuleLength()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final SetShapeForAllParabolicTroughsCommand c = new SetShapeForAllParabolicTroughsCommand();
											Scene.getInstance().setModuleLengthForAllParabolicTroughs(u);
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

			final JMenuItem miFocalLength = new JMenuItem("Focal Length...");
			miFocalLength.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof ParabolicTrough)) {
						return;
					}
					final ParabolicTrough t = (ParabolicTrough) selectedPart;
					final Foundation foundation = t.getTopContainer();
					final String partInfo = t.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel inputPanel = new JPanel(new GridLayout(1, 2, 5, 5));
					gui.add(inputPanel, BorderLayout.CENTER);
					inputPanel.add(new JLabel("Focal Length (m): "));
					final JTextField focalLengthField = new JTextField(threeDecimalsFormat.format(t.getSemilatusRectum() / 2));
					inputPanel.add(focalLengthField);
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Parabolic Trough", true);
					final JRadioButton rb2 = new JRadioButton("All Parabolic Troughs on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Parabolic Troughs");
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
					final JOptionPane optionPane = new JOptionPane(new Object[] { "Set shape for " + partInfo, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Parabola Shape");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							double f = 0;
							boolean ok = true;
							try {
								f = Double.parseDouble(focalLengthField.getText());
							} catch (final NumberFormatException nfe) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (f < 0.5 || f > 5) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Focal length must be between 0.5 and 5 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									boolean changed = Math.abs(2 * f - t.getSemilatusRectum()) > 0.000001;
									if (rb1.isSelected()) {
										if (changed) {
											final SetParabolicTroughSemilatusRectumCommand c = new SetParabolicTroughSemilatusRectumCommand(t);
											t.setSemilatusRectum(2 * f); // semilatus rectum p = 2f
											t.draw();
											SceneManager.getInstance().refresh();
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										if (!changed) {
											for (final ParabolicTrough x : foundation.getParabolicTroughs()) {
												if (Math.abs(2 * f - x.getSemilatusRectum()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final SetShapeForParabolicTroughsOnFoundationCommand c = new SetShapeForParabolicTroughsOnFoundationCommand(foundation);
											foundation.setSemilatusRectumForParabolicTroughs(2 * f);
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										if (!changed) {
											for (final ParabolicTrough x : Scene.getInstance().getAllParabolicTroughs()) {
												if (Math.abs(2 * f - x.getSemilatusRectum()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final SetShapeForAllParabolicTroughsCommand c = new SetShapeForAllParabolicTroughsCommand();
											Scene.getInstance().setSemilatusRectumForAllParabolicTroughs(2 * f);
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
					if (!(selectedPart instanceof ParabolicTrough)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final ParabolicTrough t = (ParabolicTrough) selectedPart;
					final Foundation foundation = t.getTopContainer();
					final String title = "<html>Base Height (m) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Parabolic Trough", true);
					final JRadioButton rb2 = new JRadioButton("All Parabolic Troughs on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Parabolic Troughs");
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
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(t.getBaseHeight() * Scene.getInstance().getAnnotationScale()));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Parabolic Trough Base Height");

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
								if (val < 0 || val * Scene.getInstance().getAnnotationScale() > 10) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "The base height must be between 0 and 10 meters.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									boolean changed = Math.abs(val - t.getBaseHeight()) > 0.000001;
									if (rb1.isSelected()) {
										if (changed) {
											final ChangeBaseHeightCommand c = new ChangeBaseHeightCommand(t);
											t.setBaseHeight(val);
											t.draw();
											SceneManager.getInstance().refresh();
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										if (!changed) {
											for (final ParabolicTrough x : foundation.getParabolicTroughs()) {
												if (Math.abs(val - x.getBaseHeight()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final ChangeFoundationSolarCollectorBaseHeightCommand c = new ChangeFoundationSolarCollectorBaseHeightCommand(foundation, t.getClass());
											foundation.setBaseHeightForSolarCollectors(val, t.getClass());
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										if (!changed) {
											for (final ParabolicTrough x : Scene.getInstance().getAllParabolicTroughs()) {
												if (Math.abs(val - x.getBaseHeight()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final ChangeBaseHeightForAllSolarCollectorsCommand c = new ChangeBaseHeightForAllSolarCollectorsCommand(t.getClass());
											Scene.getInstance().setBaseHeightForAllParabolicTroughs(val);
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
					if (!(selectedPart instanceof ParabolicTrough)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final ParabolicTrough trough = (ParabolicTrough) selectedPart;
					final Foundation foundation = trough.getTopContainer();
					final String title = "<html>Azimuth Angle (&deg;) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The azimuth angle is measured clockwise from the true north.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Parabolic Trough", true);
					final JRadioButton rb2 = new JRadioButton("All Parabolic Troughs on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Parabolic Troughs");
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
					double a = trough.getRelativeAzimuth() + foundation.getAzimuth();
					if (a > 360) {
						a -= 360;
					}
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(a));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Parabolic Trough Azimuth");

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
								boolean changed = Math.abs(a - trough.getRelativeAzimuth()) > 0.000001;
								if (rb1.isSelected()) {
									if (changed) {
										final ChangeAzimuthCommand c = new ChangeAzimuthCommand(trough);
										trough.setRelativeAzimuth(a);
										trough.draw();
										SceneManager.getInstance().refresh();
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									selectedScopeIndex = 0;
								} else if (rb2.isSelected()) {
									if (!changed) {
										for (final ParabolicTrough x : foundation.getParabolicTroughs()) {
											if (Math.abs(a - x.getRelativeAzimuth()) > 0.000001) {
												changed = true;
												break;
											}
										}
									}
									if (changed) {
										final ChangeFoundationParabolicTroughAzimuthCommand c = new ChangeFoundationParabolicTroughAzimuthCommand(foundation);
										foundation.setAzimuthForParabolicTroughs(a);
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
										final ChangeAzimuthForAllParabolicTroughsCommand c = new ChangeAzimuthForAllParabolicTroughsCommand();
										Scene.getInstance().setAzimuthForAllParabolicTroughs(a);
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
						if (selectedPart instanceof ParabolicTrough) {
							final ParabolicTrough t = (ParabolicTrough) selectedPart;
							final SetParabolicTroughLabelCommand c = new SetParabolicTroughLabelCommand(t);
							t.clearLabels();
							t.draw();
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
					if (selectedPart instanceof ParabolicTrough) {
						final ParabolicTrough t = (ParabolicTrough) selectedPart;
						final SetParabolicTroughLabelCommand c = new SetParabolicTroughLabelCommand(t);
						t.setLabelCustom(miLabelCustom.isSelected());
						if (t.getLabelCustom()) {
							t.setLabelCustomText(JOptionPane.showInputDialog(MainFrame.getInstance(), "Custom Text", t.getLabelCustomText()));
						}
						t.draw();
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
					if (selectedPart instanceof ParabolicTrough) {
						final ParabolicTrough t = (ParabolicTrough) selectedPart;
						final SetParabolicTroughLabelCommand c = new SetParabolicTroughLabelCommand(t);
						t.setLabelId(miLabelId.isSelected());
						t.draw();
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
					if (selectedPart instanceof ParabolicTrough) {
						final ParabolicTrough t = (ParabolicTrough) selectedPart;
						final SetParabolicTroughLabelCommand c = new SetParabolicTroughLabelCommand(t);
						t.setLabelEnergyOutput(miLabelEnergyOutput.isSelected());
						t.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelEnergyOutput);

			popupMenuForParabolicTrough = createPopupMenu(true, true, new Runnable() {
				@Override
				public void run() {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof ParabolicTrough)) {
						return;
					}
					final ParabolicTrough t = (ParabolicTrough) selectedPart;
					Util.selectSilently(miLabelNone, !t.isLabelVisible());
					Util.selectSilently(miLabelCustom, t.getLabelCustom());
					Util.selectSilently(miLabelId, t.getLabelId());
					Util.selectSilently(miLabelEnergyOutput, t.getLabelEnergyOutput());
					Util.selectSilently(cbmiDrawSunBeams, t.isSunBeamVisible());
					Util.selectSilently(cbmiDisableEditPoints, t.getLockEdit());
				}
			});

			final JMenuItem miReflectance = new JMenuItem("Mirror Reflectance...");
			miReflectance.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof ParabolicTrough)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final ParabolicTrough t = (ParabolicTrough) selectedPart;
					final String title = "<html>Reflectance (%) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>Reflectance can be affected by pollen and dust.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Parabolic Trough", true);
					final JRadioButton rb2 = new JRadioButton("All Parabolic Troughs on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Parabolic Troughs");
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
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(t.getReflectance() * 100));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Parabolic Trough Mirror Reflectance");

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
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Parabolic trough reflectance must be between 50% and 99%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									boolean changed = Math.abs(val * 0.01 - t.getReflectance()) > 0.000001;
									if (rb1.isSelected()) {
										if (changed) {
											final ChangeSolarReflectorReflectanceCommand c = new ChangeSolarReflectorReflectanceCommand(t);
											t.setReflectance(val * 0.01);
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = t.getTopContainer();
										if (!changed) {
											for (final ParabolicTrough x : foundation.getParabolicTroughs()) {
												if (Math.abs(val * 0.01 - x.getReflectance()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final ChangeFoundationSolarReflectorReflectanceCommand c = new ChangeFoundationSolarReflectorReflectanceCommand(foundation, t.getClass());
											foundation.setReflectanceForSolarReflectors(val * 0.01, t.getClass());
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										if (!changed) {
											for (final ParabolicTrough x : Scene.getInstance().getAllParabolicTroughs()) {
												if (Math.abs(val * 0.01 - x.getReflectance()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final ChangeReflectanceForAllSolarReflectorsCommand c = new ChangeReflectanceForAllSolarReflectorsCommand(t.getClass());
											Scene.getInstance().setReflectanceForAllSolarReflectors(val * 0.01, t.getClass());
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

			final JMenuItem miAbsorptance = new JMenuItem("Receiver Absorptance...");
			miAbsorptance.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof ParabolicTrough)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final ParabolicTrough t = (ParabolicTrough) selectedPart;
					final String title = "<html>Absorptance (%) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2><hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Parabolic Trough", true);
					final JRadioButton rb2 = new JRadioButton("All Parabolic Troughs on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Parabolic Troughs");
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
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(t.getAbsorptance() * 100));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Parabolic Trough Receiver Absorptance");

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
								if (val < 20 || val > 100) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Parabolic trough absorptance must be between 20% and 100%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									boolean changed = Math.abs(val * 0.01 - t.getAbsorptance()) > 0.000001;
									if (rb1.isSelected()) {
										if (changed) {
											final ChangeSolarReflectorAbsorptanceCommand c = new ChangeSolarReflectorAbsorptanceCommand(t);
											t.setAbsorptance(val * 0.01);
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = t.getTopContainer();
										if (!changed) {
											for (final ParabolicTrough x : foundation.getParabolicTroughs()) {
												if (Math.abs(val * 0.01 - x.getAbsorptance()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final ChangeFoundationSolarReflectorAbsorptanceCommand c = new ChangeFoundationSolarReflectorAbsorptanceCommand(foundation, t.getClass());
											foundation.setAbsorptanceForSolarReflectors(val * 0.01, t.getClass());
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										if (!changed) {
											for (final ParabolicTrough x : Scene.getInstance().getAllParabolicTroughs()) {
												if (Math.abs(val * 0.01 - x.getAbsorptance()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final ChangeAbsorptanceForAllSolarReflectorsCommand c = new ChangeAbsorptanceForAllSolarReflectorsCommand(t.getClass());
											Scene.getInstance().setAbsorptanceForAllSolarReflectors(val * 0.01, t.getClass());
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

			final JMenuItem miOpticalEfficiency = new JMenuItem("Optical Efficiency...");
			miOpticalEfficiency.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof ParabolicTrough)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final ParabolicTrough t = (ParabolicTrough) selectedPart;
					final String title = "<html>Opitical efficiency (%) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>For example, the percentage of the effective area for reflection after deducting<br>the area of facet gaps, module frames, absorber shadow, etc.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Parabolic Trough", true);
					final JRadioButton rb2 = new JRadioButton("All Parabolic Troughs on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Parabolic Troughs");
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
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(t.getOpticalEfficiency() * 100));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Parabolic Trough Optical Efficiency");

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
								if (val < 50 || val > 100) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Parabolic trough optical efficiency must be between 50% and 100%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									boolean changed = Math.abs(val * 0.01 - t.getOpticalEfficiency()) > 0.000001;
									if (rb1.isSelected()) {
										if (changed) {
											final ChangeSolarReflectorOpticalEfficiencyCommand c = new ChangeSolarReflectorOpticalEfficiencyCommand(t);
											t.setOpticalEfficiency(val * 0.01);
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = t.getTopContainer();
										if (!changed) {
											for (final ParabolicTrough x : foundation.getParabolicTroughs()) {
												if (Math.abs(val * 0.01 - x.getOpticalEfficiency()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final ChangeFoundationSolarReflectorOpticalEfficiencyCommand c = new ChangeFoundationSolarReflectorOpticalEfficiencyCommand(foundation, t.getClass());
											foundation.setOpticalEfficiencyForSolarReflectors(val * 0.01, t.getClass());
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										if (!changed) {
											for (final ParabolicTrough x : Scene.getInstance().getAllParabolicTroughs()) {
												if (Math.abs(val * 0.01 - x.getOpticalEfficiency()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final ChangeOpticalEfficiencyForAllSolarReflectorsCommand c = new ChangeOpticalEfficiencyForAllSolarReflectorsCommand(t.getClass());
											Scene.getInstance().setOpticalEfficiencyForAllSolarReflectors(val * 0.01, t.getClass());
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

			final JMenuItem miThermalEfficiency = new JMenuItem("Thermal Efficiency...");
			miThermalEfficiency.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof ParabolicTrough)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final ParabolicTrough t = (ParabolicTrough) selectedPart;
					final String title = "<html>Thermal efficiency (%) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2><hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Parabolic Trough", true);
					final JRadioButton rb2 = new JRadioButton("All Parabolic Troughs on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Parabolic Troughs");
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
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(t.getThermalEfficiency() * 100));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Parabolic Trough Thermal Efficiency");

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
								if (val < 5 || val > 80) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Parabolic trough thermal efficiency must be between 5% and 80%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									boolean changed = Math.abs(val * 0.01 - t.getThermalEfficiency()) > 0.000001;
									if (rb1.isSelected()) {
										if (changed) {
											final ChangeSolarReflectorThermalEfficiencyCommand c = new ChangeSolarReflectorThermalEfficiencyCommand(t);
											t.setThermalEfficiency(val * 0.01);
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = t.getTopContainer();
										if (!changed) {
											for (final ParabolicTrough x : foundation.getParabolicTroughs()) {
												if (Math.abs(val * 0.01 - x.getThermalEfficiency()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final ChangeFoundationSolarReflectorThermalEfficiencyCommand c = new ChangeFoundationSolarReflectorThermalEfficiencyCommand(foundation, t.getClass());
											foundation.setThermalEfficiencyForSolarReflectors(val * 0.01, t.getClass());
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										if (!changed) {
											for (final ParabolicTrough x : Scene.getInstance().getAllParabolicTroughs()) {
												if (Math.abs(val * 0.01 - x.getThermalEfficiency()) > 0.000001) {
													changed = true;
													break;
												}
											}
										}
										if (changed) {
											final ChangeThermalEfficiencyForAllSolarReflectorsCommand c = new ChangeThermalEfficiencyForAllSolarReflectorsCommand(t.getClass());
											Scene.getInstance().setThermalEfficiencyForAllSolarReflectors(val * 0.01, t.getClass());
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

			popupMenuForParabolicTrough.addSeparator();
			popupMenuForParabolicTrough.add(cbmiDisableEditPoints);
			popupMenuForParabolicTrough.add(cbmiDrawSunBeams);
			popupMenuForParabolicTrough.add(labelMenu);
			popupMenuForParabolicTrough.addSeparator();
			popupMenuForParabolicTrough.add(miTroughLength);
			popupMenuForParabolicTrough.add(miApertureWidth);
			popupMenuForParabolicTrough.add(miFocalLength);
			popupMenuForParabolicTrough.add(miModuleLength);
			popupMenuForParabolicTrough.add(miBaseHeight);
			popupMenuForParabolicTrough.add(miAzimuth);
			popupMenuForParabolicTrough.addSeparator();
			popupMenuForParabolicTrough.add(miReflectance);
			popupMenuForParabolicTrough.add(miAbsorptance);
			popupMenuForParabolicTrough.add(miOpticalEfficiency);
			popupMenuForParabolicTrough.add(miThermalEfficiency);
			popupMenuForParabolicTrough.addSeparator();
			popupMenuForParabolicTrough.add(miMesh);
			popupMenuForParabolicTrough.addSeparator();

			JMenuItem mi = new JMenuItem("Daily Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (EnergyPanel.getInstance().adjustCellSize()) {
						return;
					}
					if (SceneManager.getInstance().getSelectedPart() instanceof ParabolicTrough) {
						new ParabolicTroughDailyAnalysis().show();
					}
				}
			});
			popupMenuForParabolicTrough.add(mi);

			mi = new JMenuItem("Annual Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (EnergyPanel.getInstance().adjustCellSize()) {
						return;
					}
					if (SceneManager.getInstance().getSelectedPart() instanceof ParabolicTrough) {
						new ParabolicTroughAnnualAnalysis().show();
					}
				}
			});
			popupMenuForParabolicTrough.add(mi);

		}

		return popupMenuForParabolicTrough;

	}

}
