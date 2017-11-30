package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
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
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Trackable;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.PvAnnualAnalysis;
import org.concord.energy3d.simulation.PvDailyAnalysis;
import org.concord.energy3d.simulation.PvModuleSpecs;
import org.concord.energy3d.simulation.PvModulesData;
import org.concord.energy3d.undo.ChangeAzimuthCommand;
import org.concord.energy3d.undo.ChangeAzimuthForAllSolarPanelsCommand;
import org.concord.energy3d.undo.ChangeBaseHeightCommand;
import org.concord.energy3d.undo.ChangeBaseHeightForAllSolarPanelsCommand;
import org.concord.energy3d.undo.ChangeBaseHeightForSolarPanelRowCommand;
import org.concord.energy3d.undo.ChangeCellNumbersCommand;
import org.concord.energy3d.undo.ChangeCellNumbersForAllSolarPanelsCommand;
import org.concord.energy3d.undo.ChangeFoundationInverterEfficiencyCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarCellPropertiesCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarPanelAzimuthCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarPanelBaseHeightCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarPanelCellNumbersCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarPanelModelCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarPanelTiltAngleCommand;
import org.concord.energy3d.undo.ChangeInverterEfficiencyCommand;
import org.concord.energy3d.undo.ChangeInverterEfficiencyForAllCommand;
import org.concord.energy3d.undo.ChangeModelForAllSolarPanelsCommand;
import org.concord.energy3d.undo.ChangeSolarCellPropertiesCommand;
import org.concord.energy3d.undo.ChangeSolarCellPropertiesForAllCommand;
import org.concord.energy3d.undo.ChangeSolarPanelModelCommand;
import org.concord.energy3d.undo.ChangeTiltAngleCommand;
import org.concord.energy3d.undo.ChangeTiltAngleForAllSolarPanelsCommand;
import org.concord.energy3d.undo.ChangeTiltAngleForSolarPanelRowCommand;
import org.concord.energy3d.undo.ChooseSolarPanelSizeCommand;
import org.concord.energy3d.undo.RotateSolarPanelCommand;
import org.concord.energy3d.undo.SetFoundationTemperatureEffectsCommand;
import org.concord.energy3d.undo.SetShadeToleranceCommand;
import org.concord.energy3d.undo.SetShadeToleranceForAllSolarPanelsCommand;
import org.concord.energy3d.undo.SetShadeToleranceForSolarPanelsOnFoundationCommand;
import org.concord.energy3d.undo.SetSolarPanelLabelCommand;
import org.concord.energy3d.undo.SetSolarTrackerCommand;
import org.concord.energy3d.undo.SetSolarTrackersForAllCommand;
import org.concord.energy3d.undo.SetSolarTrackersOnFoundationCommand;
import org.concord.energy3d.undo.SetTemperatrureEffectsForAllCommand;
import org.concord.energy3d.undo.SetTemperatureEffectsCommand;
import org.concord.energy3d.undo.ShowSunBeamCommand;
import org.concord.energy3d.util.SpringUtilities;
import org.concord.energy3d.util.Util;

class PopupMenuForSolarPanel extends PopupMenuFactory {

	private static JPopupMenu popupMenuForSolarPanel;

	static JPopupMenu getPopupMenu() {

		if (popupMenuForSolarPanel == null) {

			final JMenu trackerMenu = new JMenu("Tracker");
			final JMenu shadeToleranceMenu = new JMenu("Shade Tolerance");

			final ButtonGroup shadeToleranceButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem miHighTolerance = new JRadioButtonMenuItem("High Tolerance...");
			shadeToleranceButtonGroup.add(miHighTolerance);
			miHighTolerance.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel sp = (SolarPanel) selectedPart;
					final String partInfo = sp.toString().substring(0, sp.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
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
					final String title = "<html>Choose shade tolerance level for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The energy generated by this panel comes from each cell proportionally (ideal case).<hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, panel }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "High Shade Tolerance");
					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							if (rb1.isSelected()) {
								final SetShadeToleranceCommand c = new SetShadeToleranceCommand(sp);
								sp.setShadeTolerance(SolarPanel.HIGH_SHADE_TOLERANCE);
								sp.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final Foundation foundation = sp.getTopContainer();
								final SetShadeToleranceForSolarPanelsOnFoundationCommand c = new SetShadeToleranceForSolarPanelsOnFoundationCommand(foundation);
								foundation.setShadeToleranceForSolarPanels(SolarPanel.HIGH_SHADE_TOLERANCE);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetShadeToleranceForAllSolarPanelsCommand c = new SetShadeToleranceForAllSolarPanelsCommand();
								Scene.getInstance().setShadeToleranceForAllSolarPanels(SolarPanel.HIGH_SHADE_TOLERANCE);
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

			final JRadioButtonMenuItem miPartialTolerance = new JRadioButtonMenuItem("Partial Tolerance...", true);
			shadeToleranceButtonGroup.add(miPartialTolerance);
			miPartialTolerance.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel sp = (SolarPanel) selectedPart;
					final String partInfo = sp.toString().substring(0, sp.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
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
					final String title = "<html>Choose shade tolerance level for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>Use bypass diodes to direct current under shading conditions.<hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, panel }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Partial Shade Tolerance");
					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							if (rb1.isSelected()) {
								final SetShadeToleranceCommand c = new SetShadeToleranceCommand(sp);
								sp.setShadeTolerance(SolarPanel.PARTIAL_SHADE_TOLERANCE);
								sp.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final Foundation foundation = sp.getTopContainer();
								final SetShadeToleranceForSolarPanelsOnFoundationCommand c = new SetShadeToleranceForSolarPanelsOnFoundationCommand(foundation);
								foundation.setShadeToleranceForSolarPanels(SolarPanel.PARTIAL_SHADE_TOLERANCE);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetShadeToleranceForAllSolarPanelsCommand c = new SetShadeToleranceForAllSolarPanelsCommand();
								Scene.getInstance().setShadeToleranceForAllSolarPanels(SolarPanel.PARTIAL_SHADE_TOLERANCE);
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

			final JRadioButtonMenuItem miNoTolerance = new JRadioButtonMenuItem("No Tolerance...");
			shadeToleranceButtonGroup.add(miNoTolerance);
			miNoTolerance.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel sp = (SolarPanel) selectedPart;
					final String partInfo = sp.toString().substring(0, sp.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
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
					final String title = "<html>Choose shade tolerance level for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>Shading greatly reduces the output of the entire panel.<hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, panel }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "No Shade Tolerance");
					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							if (rb1.isSelected()) {
								final SetShadeToleranceCommand c = new SetShadeToleranceCommand(sp);
								sp.setShadeTolerance(SolarPanel.NO_SHADE_TOLERANCE);
								sp.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final Foundation foundation = sp.getTopContainer();
								final SetShadeToleranceForSolarPanelsOnFoundationCommand c = new SetShadeToleranceForSolarPanelsOnFoundationCommand(foundation);
								foundation.setShadeToleranceForSolarPanels(SolarPanel.NO_SHADE_TOLERANCE);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetShadeToleranceForAllSolarPanelsCommand c = new SetShadeToleranceForAllSolarPanelsCommand();
								Scene.getInstance().setShadeToleranceForAllSolarPanels(SolarPanel.NO_SHADE_TOLERANCE);
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

			final ButtonGroup trackerButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem miNoTracker = new JRadioButtonMenuItem("No Tracker...", true);
			trackerButtonGroup.add(miNoTracker);
			miNoTracker.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel sp = (SolarPanel) selectedPart;
					final String partInfo = sp.toString().substring(0, sp.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
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
					final String title = "<html>Remove tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>No tracker will be used.<hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, panel }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "No Tracker");
					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							boolean changed = sp.getTracker() != Trackable.NO_TRACKER;
							if (rb1.isSelected()) {
								if (changed) {
									final SetSolarTrackerCommand c = new SetSolarTrackerCommand(sp, "No Tracker");
									sp.setTracker(Trackable.NO_TRACKER);
									sp.draw();
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final Foundation foundation = sp.getTopContainer();
								if (!changed) {
									for (final SolarPanel x : foundation.getSolarPanels()) {
										if (x.getTracker() != Trackable.NO_TRACKER) {
											changed = true;
											break;
										}
									}
								}
								if (changed) {
									final SetSolarTrackersOnFoundationCommand c = new SetSolarTrackersOnFoundationCommand(foundation, sp, "No Tracker for All Solar Panels on Selected Foundation");
									foundation.setTrackerForSolarPanels(Trackable.NO_TRACKER);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								if (!changed) {
									for (final SolarPanel x : Scene.getInstance().getAllSolarPanels()) {
										if (x.getTracker() != Trackable.NO_TRACKER) {
											changed = true;
											break;
										}
									}
								}
								if (changed) {
									final SetSolarTrackersForAllCommand c = new SetSolarTrackersForAllCommand(sp, "No Tracker for All Solar Panels");
									Scene.getInstance().setTrackerForAllSolarPanels(Trackable.NO_TRACKER);
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
			});

			final JRadioButtonMenuItem miHorizontalSingleAxisTracker = new JRadioButtonMenuItem("Horizontal Single-Axis Tracker...");
			trackerButtonGroup.add(miHorizontalSingleAxisTracker);
			miHorizontalSingleAxisTracker.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel sp = (SolarPanel) selectedPart;
					final String partInfo = sp.toString().substring(0, sp.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
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
					final String title = "<html>Set horizontal single-axis tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>A horizontal single-axis tracker (HSAT) rotates about the north-south axis<br>to follow the sun from east to west during the day.<hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, panel }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Horizontal Single-Axis Tracker");
					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							boolean changed = sp.getTracker() != Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER;
							if (rb1.isSelected()) {
								if (changed) {
									final SetSolarTrackerCommand c = new SetSolarTrackerCommand(sp, "Horizontal Single-Axis Tracker");
									sp.setTracker(SolarPanel.HORIZONTAL_SINGLE_AXIS_TRACKER);
									sp.draw();
									SceneManager.getInstance().refresh();
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final Foundation foundation = sp.getTopContainer();
								if (!changed) {
									for (final SolarPanel x : foundation.getSolarPanels()) {
										if (x.getTracker() != Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER) {
											changed = true;
											break;
										}
									}
								}
								if (changed) {
									final SetSolarTrackersOnFoundationCommand c = new SetSolarTrackersOnFoundationCommand(foundation, sp, "Horizontal Single-Axis Tracker for All Solar Panels on Selected Foundation");
									foundation.setTrackerForSolarPanels(SolarPanel.HORIZONTAL_SINGLE_AXIS_TRACKER);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								if (!changed) {
									for (final SolarPanel x : Scene.getInstance().getAllSolarPanels()) {
										if (x.getTracker() != Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER) {
											changed = true;
											break;
										}
									}
								}
								if (changed) {
									final SetSolarTrackersForAllCommand c = new SetSolarTrackersForAllCommand(sp, "Horizontal Single-Axis Tracker for All Solar Panels");
									Scene.getInstance().setTrackerForAllSolarPanels(SolarPanel.HORIZONTAL_SINGLE_AXIS_TRACKER);
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
			});

			final JRadioButtonMenuItem miVerticalSingleAxisTracker = new JRadioButtonMenuItem("Vertical Single-Axis Tracker...");
			trackerButtonGroup.add(miVerticalSingleAxisTracker);
			miVerticalSingleAxisTracker.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel sp = (SolarPanel) selectedPart;
					final String partInfo = sp.toString().substring(0, sp.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
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
					final String title = "<html>Set vertical single-axis tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>A vertical single-axis tracker (VSAT) rotates about an axis perpendicular to the ground<br>and follow the sun from east to west during the day.<hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, panel }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Vertical Single-Axis Tracker");
					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							boolean changed = sp.getTracker() != Trackable.VERTICAL_SINGLE_AXIS_TRACKER;
							if (rb1.isSelected()) {
								if (changed) {
									final SetSolarTrackerCommand c = new SetSolarTrackerCommand(sp, "Vertical Single-Axis Tracker");
									sp.setTracker(SolarPanel.VERTICAL_SINGLE_AXIS_TRACKER);
									sp.draw();
									SceneManager.getInstance().refresh();
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final Foundation foundation = sp.getTopContainer();
								if (!changed) {
									for (final SolarPanel x : foundation.getSolarPanels()) {
										if (x.getTracker() != Trackable.VERTICAL_SINGLE_AXIS_TRACKER) {
											changed = true;
											break;
										}
									}
								}
								if (changed) {
									final SetSolarTrackersOnFoundationCommand c = new SetSolarTrackersOnFoundationCommand(foundation, sp, "Vertical Single-Axis Tracker for All Solar Panels on Selected Foundation");
									foundation.setTrackerForSolarPanels(SolarPanel.VERTICAL_SINGLE_AXIS_TRACKER);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								if (!changed) {
									for (final SolarPanel x : Scene.getInstance().getAllSolarPanels()) {
										if (x.getTracker() != Trackable.VERTICAL_SINGLE_AXIS_TRACKER) {
											changed = true;
											break;
										}
									}
								}
								if (changed) {
									final SetSolarTrackersForAllCommand c = new SetSolarTrackersForAllCommand(sp, "Vertical Single-Axis Tracker for All Solar Panels");
									Scene.getInstance().setTrackerForAllSolarPanels(SolarPanel.VERTICAL_SINGLE_AXIS_TRACKER);
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
			});

			final JRadioButtonMenuItem miAltazimuthDualAxisTracker = new JRadioButtonMenuItem("Altazimuth Dual-Axis Tracker...");
			trackerButtonGroup.add(miAltazimuthDualAxisTracker);
			miAltazimuthDualAxisTracker.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel sp = (SolarPanel) selectedPart;
					final String partInfo = sp.toString().substring(0, sp.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
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
					final String title = "<html>Set altitude-azimuth dual-axis tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The Alt/Az dual-axis solar tracker will rotate the solar panel to face the sun<br>all the time during the day.<hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, panel }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Altitude-Azimuth Dual-Axis Tracker");
					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							boolean changed = sp.getTracker() != Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER;
							if (rb1.isSelected()) {
								if (changed) {
									final SetSolarTrackerCommand c = new SetSolarTrackerCommand(sp, "Dual-Axis Tracker");
									sp.setTracker(SolarPanel.ALTAZIMUTH_DUAL_AXIS_TRACKER);
									sp.draw();
									SceneManager.getInstance().refresh();
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final Foundation foundation = sp.getTopContainer();
								if (!changed) {
									for (final SolarPanel x : foundation.getSolarPanels()) {
										if (x.getTracker() != Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER) {
											changed = true;
											break;
										}
									}
								}
								if (changed) {
									final SetSolarTrackersOnFoundationCommand c = new SetSolarTrackersOnFoundationCommand(foundation, sp, "Dual-Axis Tracker for All Solar Panels on Selected Foundation");
									foundation.setTrackerForSolarPanels(SolarPanel.ALTAZIMUTH_DUAL_AXIS_TRACKER);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								if (!changed) {
									for (final SolarPanel x : Scene.getInstance().getAllSolarPanels()) {
										if (x.getTracker() != Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER) {
											changed = true;
											break;
										}
									}
								}
								if (changed) {
									final SetSolarTrackersForAllCommand c = new SetSolarTrackersForAllCommand(sp, "Dual-Axis Tracker for All Solar Panels");
									Scene.getInstance().setTrackerForAllSolarPanels(SolarPanel.ALTAZIMUTH_DUAL_AXIS_TRACKER);
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
			});

			final JMenu orientationMenu = new JMenu("Orientation");
			final ButtonGroup orientationGroup = new ButtonGroup();

			final JRadioButtonMenuItem rbmiLandscape = new JRadioButtonMenuItem("Landscape");
			rbmiLandscape.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (rbmiLandscape.isSelected()) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (!(selectedPart instanceof SolarPanel)) {
							return;
						}
						final SolarPanel s = (SolarPanel) selectedPart;
						final RotateSolarPanelCommand c = new RotateSolarPanelCommand(s);
						s.setRotated(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						s.draw();
						updateAfterEdit();
					}
				}
			});
			orientationMenu.add(rbmiLandscape);
			orientationGroup.add(rbmiLandscape);

			final JRadioButtonMenuItem rbmiPortrait = new JRadioButtonMenuItem("Portrait", true);
			rbmiPortrait.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (rbmiPortrait.isSelected()) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (!(selectedPart instanceof SolarPanel)) {
							return;
						}
						final SolarPanel s = (SolarPanel) selectedPart;
						final RotateSolarPanelCommand c = new RotateSolarPanelCommand(s);
						s.setRotated(false);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						s.draw();
						updateAfterEdit();
					}
				}
			});
			orientationMenu.add(rbmiPortrait);
			orientationGroup.add(rbmiPortrait);

			final JMenu labelMenu = new JMenu("Label");

			final JCheckBoxMenuItem miLabelNone = new JCheckBoxMenuItem("None", true);
			miLabelNone.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (miLabelNone.isSelected()) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof SolarPanel) {
							final SolarPanel s = (SolarPanel) selectedPart;
							final SetSolarPanelLabelCommand c = new SetSolarPanelLabelCommand(s);
							s.clearLabels();
							s.draw();
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
					if (selectedPart instanceof SolarPanel) {
						final SolarPanel s = (SolarPanel) selectedPart;
						final SetSolarPanelLabelCommand c = new SetSolarPanelLabelCommand(s);
						s.setLabelCustom(miLabelCustom.isSelected());
						if (s.getLabelCustom()) {
							s.setLabelCustomText(JOptionPane.showInputDialog(MainFrame.getInstance(), "Custom Text", s.getLabelCustomText()));
						}
						s.draw();
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
					if (selectedPart instanceof SolarPanel) {
						final SolarPanel s = (SolarPanel) selectedPart;
						final SetSolarPanelLabelCommand c = new SetSolarPanelLabelCommand(s);
						s.setLabelId(miLabelId.isSelected());
						s.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelId);

			final JCheckBoxMenuItem miLabelModelName = new JCheckBoxMenuItem("Model");
			miLabelModelName.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof SolarPanel) {
						final SolarPanel s = (SolarPanel) selectedPart;
						final SetSolarPanelLabelCommand c = new SetSolarPanelLabelCommand(s);
						s.setLabelModelName(miLabelModelName.isSelected());
						s.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelModelName);

			final JCheckBoxMenuItem miLabelCellEfficiency = new JCheckBoxMenuItem("Cell Efficiency");
			miLabelCellEfficiency.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof SolarPanel) {
						final SolarPanel s = (SolarPanel) selectedPart;
						final SetSolarPanelLabelCommand c = new SetSolarPanelLabelCommand(s);
						s.setLabelCellEfficiency(miLabelCellEfficiency.isSelected());
						s.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelCellEfficiency);

			final JCheckBoxMenuItem miLabelTiltAngle = new JCheckBoxMenuItem("Tilt Angle");
			miLabelTiltAngle.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof SolarPanel) {
						final SolarPanel s = (SolarPanel) selectedPart;
						final SetSolarPanelLabelCommand c = new SetSolarPanelLabelCommand(s);
						s.setLabelTiltAngle(miLabelTiltAngle.isSelected());
						s.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelTiltAngle);

			final JCheckBoxMenuItem miLabelTracker = new JCheckBoxMenuItem("Tracker");
			miLabelTracker.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof SolarPanel) {
						final SolarPanel s = (SolarPanel) selectedPart;
						final SetSolarPanelLabelCommand c = new SetSolarPanelLabelCommand(s);
						s.setLabelTracker(miLabelTracker.isSelected());
						s.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelTracker);

			final JCheckBoxMenuItem miLabelEnergyOutput = new JCheckBoxMenuItem("Energy Output");
			miLabelEnergyOutput.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof SolarPanel) {
						final SolarPanel s = (SolarPanel) selectedPart;
						final SetSolarPanelLabelCommand c = new SetSolarPanelLabelCommand(s);
						s.setLabelEnergyOutput(miLabelEnergyOutput.isSelected());
						s.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelEnergyOutput);

			final JMenuItem miTiltAngle = new JMenuItem("Tilt Angle...");
			miTiltAngle.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final SolarPanel sp = (SolarPanel) selectedPart;
					final String title = "<html>Tilt Angle of " + partInfo + " (&deg;)</html>";
					final String footnote = "<html><hr><font size=2>The tilt angle of a solar panel is the angle between its surface and the base surface.<br>The tilt angle must be between -90&deg; and 90&deg;.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("Only this Row", true);
					final JRadioButton rb3 = new JRadioButton("All Solar Panels on This Foundation");
					final JRadioButton rb4 = new JRadioButton("All Solar Panels");
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
					gui.add(panel, BorderLayout.CENTER);
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(sp.getTiltAngle()));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Tilt Angle");

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
									if (rb1.isSelected()) {
										final ChangeTiltAngleCommand c = new ChangeTiltAngleCommand(sp);
										sp.setTiltAngle(val);
										sp.draw();
										SceneManager.getInstance().refresh();
										if (sp.checkContainerIntersection()) {
											JOptionPane.showMessageDialog(MainFrame.getInstance(), "This tilt angle cannot be set as the solar panel would cut into the underlying surface.", "Illegal Tilt Angle", JOptionPane.ERROR_MESSAGE);
											c.undo();
										} else {
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final List<SolarPanel> row = sp.getRow();
										final ChangeTiltAngleForSolarPanelRowCommand c = new ChangeTiltAngleForSolarPanelRowCommand(row);
										boolean intersected = false;
										for (final SolarPanel x : row) {
											x.setTiltAngle(val);
											x.draw();
											if (x.checkContainerIntersection()) {
												intersected = true;
												break;
											}
										}
										SceneManager.getInstance().refresh();
										if (intersected) {
											JOptionPane.showMessageDialog(MainFrame.getInstance(), "This tilt angle cannot be set as one or more solar panels would cut into the underlying surface.", "Illegal Tilt Angle", JOptionPane.ERROR_MESSAGE);
											c.undo();
										} else {
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final Foundation foundation = sp.getTopContainer();
										final ChangeFoundationSolarPanelTiltAngleCommand c = new ChangeFoundationSolarPanelTiltAngleCommand(foundation);
										foundation.setTiltAngleForSolarPanels(val);
										if (foundation.checkContainerIntersectionForSolarPanels()) {
											JOptionPane.showMessageDialog(MainFrame.getInstance(), "This tilt angle cannot be set as one or more solar panels would cut into the underlying surface.", "Illegal Tilt Angle", JOptionPane.ERROR_MESSAGE);
											c.undo();
										} else {
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 2;
									} else if (rb4.isSelected()) {
										final ChangeTiltAngleForAllSolarPanelsCommand c = new ChangeTiltAngleForAllSolarPanelsCommand();
										Scene.getInstance().setTiltAngleForAllSolarPanels(val);
										if (Scene.getInstance().checkContainerIntersectionForAllSolarPanels()) {
											JOptionPane.showMessageDialog(MainFrame.getInstance(), "This tilt angle cannot be set as one or more solar panels would cut into the underlying surface.", "Illegal Tilt Angle", JOptionPane.ERROR_MESSAGE);
											c.undo();
										} else {
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										selectedScopeIndex = 3;
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
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final SolarPanel sp = (SolarPanel) selectedPart;
					final Foundation foundation = sp.getTopContainer();
					final String title = "<html>Azimuth Angle of " + partInfo + " (&deg;)</html>";
					final String footnote = "<html><hr><font size=2>The azimuth angle is measured clockwise from the true north.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
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
					double a = sp.getRelativeAzimuth() + foundation.getAzimuth();
					if (a > 360) {
						a -= 360;
					}
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(a));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Azimuth");

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
								if (rb1.isSelected()) {
									final ChangeAzimuthCommand c = new ChangeAzimuthCommand(sp);
									sp.setRelativeAzimuth(a);
									sp.draw();
									SceneManager.getInstance().refresh();
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 0;
								} else if (rb2.isSelected()) {
									final ChangeFoundationSolarPanelAzimuthCommand c = new ChangeFoundationSolarPanelAzimuthCommand(foundation);
									foundation.setAzimuthForSolarPanels(a);
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 1;
								} else if (rb3.isSelected()) {
									final ChangeAzimuthForAllSolarPanelsCommand c = new ChangeAzimuthForAllSolarPanelsCommand();
									Scene.getInstance().setAzimuthForAllSolarPanels(a);
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

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel s = (SolarPanel) selectedPart;
					final String partInfo = s.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					gui.setBorder(BorderFactory.createTitledBorder("Choose Size for " + partInfo));
					final JComboBox<String> sizeComboBox = new JComboBox<String>(solarPanelNominalSize.getStrings());
					final int nItems = sizeComboBox.getItemCount();
					for (int i = 0; i < nItems; i++) {
						if (Util.isZero(s.getPanelHeight() - solarPanelNominalSize.getNominalHeights()[i]) && Util.isZero(s.getPanelWidth() - solarPanelNominalSize.getNominalWidths()[i])) {
							sizeComboBox.setSelectedIndex(i);
						}
					}
					gui.add(sizeComboBox, BorderLayout.NORTH);
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), gui, "Set Size", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					final ChooseSolarPanelSizeCommand c = new ChooseSolarPanelSizeCommand(s);
					final int i = sizeComboBox.getSelectedIndex();
					s.setPanelWidth(solarPanelNominalSize.getNominalWidths()[i]);
					s.setPanelHeight(solarPanelNominalSize.getNominalHeights()[i]);
					s.setNumberOfCellsInX(solarPanelNominalSize.getCellNx()[i]);
					s.setNumberOfCellsInY(solarPanelNominalSize.getCellNy()[i]);
					s.draw();
					SceneManager.getInstance().getUndoManager().addEdit(c);
					updateAfterEdit();
				}
			});

			// @deprecated: module structure is related to size and may not be set independently
			final JMenuItem miModuleStructure = new JMenuItem("Module Structure...");
			miModuleStructure.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel s = (SolarPanel) selectedPart;
					final Foundation foundation = s.getTopContainer();
					int nx = s.getNumberOfCellsInX();
					int ny = s.getNumberOfCellsInY();
					final String partInfo = s.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final JPanel inputFields = new JPanel();
					inputFields.setBorder(BorderFactory.createTitledBorder("Cell Numbers for " + partInfo));
					final JSpinner nxSpinner = new JSpinner(new SpinnerNumberModel(nx, 1, 20, 1));
					inputFields.add(nxSpinner);
					inputFields.add(new JLabel("  \u00D7  "));
					final JSpinner nySpinner = new JSpinner(new SpinnerNumberModel(ny, 1, 20, 1));
					inputFields.add(nySpinner);
					nxSpinner.setEnabled(false);
					nySpinner.setEnabled(false);
					final JPanel scopeFields = new JPanel();
					scopeFields.setLayout(new BoxLayout(scopeFields, BoxLayout.Y_AXIS));
					scopeFields.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					scopeFields.add(rb1);
					scopeFields.add(rb2);
					scopeFields.add(rb3);
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
					final JPanel panel = new JPanel(new BorderLayout(0, 8));
					panel.add(inputFields, BorderLayout.NORTH);
					panel.add(new JLabel(new ImageIcon(PopupMenuFactory.class.getResource("icons/solarcells.png"))));
					panel.add(scopeFields, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Cells of Module");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							nx = (Integer) nxSpinner.getValue();
							ny = (Integer) nySpinner.getValue();
							if (rb1.isSelected()) {
								final ChangeCellNumbersCommand c = new ChangeCellNumbersCommand(s);
								s.setNumberOfCellsInX(nx);
								s.setNumberOfCellsInY(ny);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final ChangeFoundationSolarPanelCellNumbersCommand c = new ChangeFoundationSolarPanelCellNumbersCommand(foundation);
								foundation.setCellNumbersForSolarPanels(nx, ny);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final ChangeCellNumbersForAllSolarPanelsCommand c = new ChangeCellNumbersForAllSolarPanelsCommand();
								Scene.getInstance().setCellNumbersForAllSolarPanels(nx, ny);
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

			final JMenuItem miBaseHeight = new JMenuItem("Base Height...");
			miBaseHeight.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final SolarPanel sp = (SolarPanel) selectedPart;
					final Foundation foundation = sp.getTopContainer();
					final String title = "<html>Base Height (m) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("Only this Row");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb4 = new JRadioButton("All Solar Panels");
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
					gui.add(panel, BorderLayout.CENTER);
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(sp.getBaseHeight() * Scene.getInstance().getAnnotationScale()));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Base Height");

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
								if (rb1.isSelected()) {
									final ChangeBaseHeightCommand c = new ChangeBaseHeightCommand(sp);
									sp.setBaseHeight(val);
									sp.draw();
									SceneManager.getInstance().refresh();
									if (sp.checkContainerIntersection()) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "This base height cannot be set as the solar panel would cut into the underlying surface.", "Illegal Base Height", JOptionPane.ERROR_MESSAGE);
										c.undo();
									} else {
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									selectedScopeIndex = 0;
								} else if (rb2.isSelected()) {
									final List<SolarPanel> row = sp.getRow();
									final ChangeBaseHeightForSolarPanelRowCommand c = new ChangeBaseHeightForSolarPanelRowCommand(row);
									boolean intersected = false;
									for (final SolarPanel x : row) {
										x.setBaseHeight(val);
										x.draw();
										if (x.checkContainerIntersection()) {
											intersected = true;
											break;
										}
									}
									SceneManager.getInstance().refresh();
									if (intersected) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "This base height cannot be set as one or more solar panels in the row would cut into the underlying surface.", "Illegal Base Height", JOptionPane.ERROR_MESSAGE);
										c.undo();
									} else {
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									selectedScopeIndex = 1;
								} else if (rb3.isSelected()) {
									final ChangeFoundationSolarPanelBaseHeightCommand c = new ChangeFoundationSolarPanelBaseHeightCommand(foundation);
									foundation.setBaseHeightForSolarPanels(val);
									if (foundation.checkContainerIntersectionForSolarPanels()) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "This base height cannot be set as one or more solar panels would cut into the underlying surface.", "Illegal Base Height", JOptionPane.ERROR_MESSAGE);
										c.undo();
									} else {
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									selectedScopeIndex = 2;
								} else if (rb4.isSelected()) {
									final ChangeBaseHeightForAllSolarPanelsCommand c = new ChangeBaseHeightForAllSolarPanelsCommand();
									Scene.getInstance().setBaseHeightForAllSolarPanels(val);
									if (Scene.getInstance().checkContainerIntersectionForAllSolarPanels()) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "This base height cannot be set as one or more solar panels would cut into the underlying surface.", "Illegal Base Height", JOptionPane.ERROR_MESSAGE);
										c.undo();
									} else {
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									selectedScopeIndex = 3;
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

			final JCheckBoxMenuItem cbmiDrawSunBeam = new JCheckBoxMenuItem("Draw Sun Beam");
			cbmiDrawSunBeam.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel sp = (SolarPanel) selectedPart;
					final ShowSunBeamCommand c = new ShowSunBeamCommand(sp);
					sp.setSunBeamVisible(cbmiDrawSunBeam.isSelected());
					sp.drawSunBeam();
					sp.draw();
					SceneManager.getInstance().refresh();
					SceneManager.getInstance().getUndoManager().addEdit(c);
					Scene.getInstance().setEdited(true);
				}
			});

			final JMenuItem miCells = new JMenuItem("Solar Cells...");
			miCells.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel solarPanel = (SolarPanel) selectedPart;
					final String title = "<html>Solar Cell Properties of " + selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1) + "</html>";
					String footnote = "<html><hr><font size=2><b>How efficiently can a solar cell convert light into electricity?</b><br>The Shockley-Queisser limit is 34%. The theoretical limit for multilayer cells<br>is 86%. As of 2017, the best solar panel in the market has an efficiency of 24%.<br>The highest efficiency you can choose is limited to " + SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "%.<hr>";
					footnote += "<font size=2>Solar cells made of monocrystalline silicon are usually round or semi-round.<br>Hence, there is a small fraction of area on a solar panel not covered by cells.<br>In other words, a monocrystalline solar panel has a smaller packing density.";
					footnote += "<br><font size=2>Solar cells made of polycrystalline silicon are usually square. Compared with a<br>monocrystalline solar panel, a polycrystalline one has a higher packing density.<br>Color has no relationship with efficiency.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.SOUTH);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
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

					final JPanel inputPanel = new JPanel(new SpringLayout());
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					gui.add(inputPanel, BorderLayout.CENTER);
					JLabel label = new JLabel("Type: ", JLabel.LEFT);
					inputPanel.add(label);
					final JComboBox<String> typeComboBox = new JComboBox<String>(new String[] { "Polycrystalline", "Monocrystalline", "Thin Film" });
					typeComboBox.setSelectedIndex(solarPanel.getCellType());
					label.setLabelFor(typeComboBox);
					inputPanel.add(typeComboBox);
					label = new JLabel("Color: ", JLabel.LEFT);
					inputPanel.add(label);
					final JComboBox<String> colorComboBox = new JComboBox<String>(new String[] { "Blue", "Black", "Gray" });
					colorComboBox.setSelectedIndex(solarPanel.getColorOption());
					label.setLabelFor(colorComboBox);
					inputPanel.add(colorComboBox);
					label = new JLabel("Efficiency (%): ", JLabel.LEFT);
					inputPanel.add(label);
					final JTextField efficiencyField = new JTextField(EnergyPanel.TWO_DECIMALS.format(solarPanel.getCellEfficiency() * 100));
					label.setLabelFor(efficiencyField);
					inputPanel.add(efficiencyField);
					SpringUtilities.makeCompactGrid(inputPanel, 3, 2, 6, 6, 6, 6);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Cell Properties");

					while (true) {
						efficiencyField.selectAll();
						efficiencyField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							double val = 0;
							boolean ok = true;
							try {
								val = Double.parseDouble(efficiencyField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), efficiencyField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (val < SolarPanel.MIN_SOLAR_CELL_EFFICIENCY_PERCENTAGE || val > SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar cell efficiency must be between " + SolarPanel.MIN_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "% and " + SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									final int cellType = typeComboBox.getSelectedIndex();
									final int colorOption = colorComboBox.getSelectedIndex();
									if (rb1.isSelected()) {
										final ChangeSolarCellPropertiesCommand c = new ChangeSolarCellPropertiesCommand(solarPanel);
										solarPanel.setCellEfficiency(val * 0.01);
										solarPanel.setCellType(cellType);
										solarPanel.setColorOption(colorOption);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = solarPanel.getTopContainer();
										final ChangeFoundationSolarCellPropertiesCommand c = new ChangeFoundationSolarCellPropertiesCommand(foundation);
										foundation.setSolarCellEfficiency(val * 0.01);
										foundation.setCellTypeForSolarPanels(cellType);
										foundation.setColorForSolarPanels(colorOption);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final ChangeSolarCellPropertiesForAllCommand c = new ChangeSolarCellPropertiesForAllCommand();
										Scene.getInstance().setSolarCellEfficiencyForAll(val * 0.01);
										Scene.getInstance().setCellTypeForAllSolarPanels(cellType);
										Scene.getInstance().setColorForAllSolarPanels(colorOption);
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

			final JMenuItem miTemperatureEffects = new JMenuItem("Temperature Effects...");
			miTemperatureEffects.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final SolarPanel solarPanel = (SolarPanel) selectedPart;
					final String title = "<html>Temperature Effects of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>Increased temperature reduces solar cell efficiency. To determine this temperature effect,<br>it is important to know the expected operating temperature: the Nominal Operating Cell<br>Temperature (NOCT). NOCT ranges from 33&deg;C to 58&deg;C.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
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
					gui.add(panel, BorderLayout.SOUTH);

					final JPanel inputPanel = new JPanel(new SpringLayout());
					gui.add(inputPanel, BorderLayout.CENTER);
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

					JLabel label = new JLabel("<html>Nominal Operating Cell Temperature (&deg;C): ", JLabel.LEFT);
					inputPanel.add(label);
					final JTextField noctField = new JTextField(EnergyPanel.TWO_DECIMALS.format(solarPanel.getNominalOperatingCellTemperature()));
					label.setLabelFor(noctField);
					inputPanel.add(noctField);
					label = new JLabel("<html>Temperature Coefficient of Pmax (%/&deg;C): ", JLabel.LEFT);
					inputPanel.add(label);
					final JTextField pmaxField = new JTextField(EnergyPanel.TWO_DECIMALS.format(solarPanel.getTemperatureCoefficientPmax() * 100));
					label.setLabelFor(pmaxField);
					inputPanel.add(pmaxField);
					SpringUtilities.makeCompactGrid(inputPanel, 2, 2, 6, 6, 6, 6);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Temperature Effects");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							double noct = 0;
							double pmax = 0;
							boolean ok = true;
							try {
								noct = Double.parseDouble(noctField.getText());
								pmax = Double.parseDouble(pmaxField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (noct < 33 || noct > 58) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Nominal Operating Cell Temperature must be between 33 and 58 Celsius degrees.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (pmax < -1 || pmax > 0) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Temperature coefficient of Pmax must be between -1% and 0% per Celsius degree.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final SetTemperatureEffectsCommand c = new SetTemperatureEffectsCommand(solarPanel);
										solarPanel.setTemperatureCoefficientPmax(pmax * 0.01);
										solarPanel.setNominalOperatingCellTemperature(noct);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = solarPanel.getTopContainer();
										final SetFoundationTemperatureEffectsCommand c = new SetFoundationTemperatureEffectsCommand(foundation);
										foundation.setTemperatureCoefficientPmax(pmax * 0.01);
										foundation.setNominalOperatingCellTemperature(noct);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final SetTemperatrureEffectsForAllCommand c = new SetTemperatrureEffectsForAllCommand();
										Scene.getInstance().setTemperatureCoefficientPmaxForAll(pmax * 0.01);
										Scene.getInstance().setNominalOperatingCellTemperatureForAll(noct);
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

			final JMenuItem miInverterEff = new JMenuItem("Inverter Efficiency...");
			miInverterEff.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final SolarPanel solarPanel = (SolarPanel) selectedPart;
					final String title = "<html>Inverter Efficiency (%) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The efficiency of a micro inverter for converting electricity from DC to AC is typically 95%.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
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
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(solarPanel.getInverterEfficiency() * 100));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Inverter Efficiency");

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
								if (val < SolarPanel.MIN_INVERTER_EFFICIENCY_PERCENTAGE || val >= SolarPanel.MAX_INVERTER_EFFICIENCY_PERCENTAGE) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Inverter efficiency must be greater than " + SolarPanel.MIN_INVERTER_EFFICIENCY_PERCENTAGE + "% and less than " + SolarPanel.MAX_INVERTER_EFFICIENCY_PERCENTAGE + "%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final ChangeInverterEfficiencyCommand c = new ChangeInverterEfficiencyCommand(solarPanel);
										solarPanel.setInverterEfficiency(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = solarPanel.getTopContainer();
										final ChangeFoundationInverterEfficiencyCommand c = new ChangeFoundationInverterEfficiencyCommand(foundation);
										foundation.setSolarPanelInverterEfficiency(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final ChangeInverterEfficiencyForAllCommand c = new ChangeInverterEfficiencyForAllCommand();
										Scene.getInstance().setSolarPanelInverterEfficiencyForAll(val * 0.01);
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

			trackerMenu.add(miNoTracker);
			trackerMenu.add(miHorizontalSingleAxisTracker);
			trackerMenu.add(miVerticalSingleAxisTracker);
			trackerMenu.add(miAltazimuthDualAxisTracker);

			shadeToleranceMenu.add(miNoTolerance);
			shadeToleranceMenu.add(miPartialTolerance);
			shadeToleranceMenu.add(miHighTolerance);

			final JMenuItem miModel = new JMenuItem("Model...");
			miModel.addActionListener(new ActionListener() {

				private String modelName;
				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel s = (SolarPanel) selectedPart;
					final Foundation foundation = s.getTopContainer();
					final String partInfo = s.toString().substring(0, s.toString().indexOf(')') + 1);
					final Map<String, PvModuleSpecs> modules = PvModulesData.getInstance().getModules();
					final String[] models = new String[modules.size() + 1];
					int i = 0;
					models[i] = "Custom";
					for (final String key : modules.keySet()) {
						models[++i] = key;
					}
					final PvModuleSpecs specs = s.getPvModuleSpecs();
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					gui.setBorder(BorderFactory.createTitledBorder("Model for " + partInfo));
					final JComboBox<String> typeComboBox = new JComboBox<String>(models);
					typeComboBox.setSelectedItem(specs.getModel());
					typeComboBox.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(final ItemEvent e) {
							if (e.getStateChange() == ItemEvent.SELECTED) {
								modelName = (String) typeComboBox.getSelectedItem();
							}
						}
					});
					gui.add(typeComboBox, BorderLayout.NORTH);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
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
					gui.add(scopePanel, BorderLayout.CENTER);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(gui, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Model");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							if (rb1.isSelected()) {
								final ChangeSolarPanelModelCommand c = new ChangeSolarPanelModelCommand(s);
								s.setPvModuleSpecs(PvModulesData.getInstance().getModuleSpecs(modelName));
								s.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final ChangeFoundationSolarPanelModelCommand c = new ChangeFoundationSolarPanelModelCommand(foundation);
								foundation.setModelForSolarPanels(PvModulesData.getInstance().getModuleSpecs(modelName));
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final ChangeModelForAllSolarPanelsCommand c = new ChangeModelForAllSolarPanelsCommand();
								Scene.getInstance().setModelForAllSolarPanels(PvModulesData.getInstance().getModuleSpecs(modelName));
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

			popupMenuForSolarPanel = createPopupMenu(true, true, new Runnable() {
				@Override
				public void run() {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel sp = (SolarPanel) selectedPart;
					switch (sp.getShadeTolerance()) {
					case SolarPanel.HIGH_SHADE_TOLERANCE:
						Util.selectSilently(miHighTolerance, true);
						break;
					case SolarPanel.PARTIAL_SHADE_TOLERANCE:
						Util.selectSilently(miPartialTolerance, true);
						break;
					case SolarPanel.NO_SHADE_TOLERANCE:
						Util.selectSilently(miNoTolerance, true);
						break;
					}
					Util.selectSilently(cbmiDrawSunBeam, sp.isSunBeamVisible());
					Util.selectSilently(rbmiLandscape, sp.isRotated());
					Util.selectSilently(rbmiPortrait, !sp.isRotated());
					Util.selectSilently(miLabelNone, !sp.isLabelVisible());
					Util.selectSilently(miLabelCustom, sp.getLabelCustom());
					Util.selectSilently(miLabelId, sp.getLabelId());
					Util.selectSilently(miLabelModelName, sp.getLabelModelName());
					Util.selectSilently(miLabelCellEfficiency, sp.getLabelCellEfficiency());
					Util.selectSilently(miLabelTiltAngle, sp.getLabelTiltAngle());
					Util.selectSilently(miLabelTracker, sp.getLabelTracker());
					Util.selectSilently(miLabelEnergyOutput, sp.getLabelEnergyOutput());

					final PvModuleSpecs pms = sp.getPvModuleSpecs();
					final boolean isCustom = "Custom".equals(pms.getModel());
					miCells.setEnabled(isCustom);
					miSize.setEnabled(isCustom);
					miTemperatureEffects.setEnabled(isCustom);
					shadeToleranceMenu.setEnabled(isCustom);

					switch (sp.getTracker()) {
					case Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER:
						Util.selectSilently(miAltazimuthDualAxisTracker, true);
						break;
					case Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER:
						Util.selectSilently(miHorizontalSingleAxisTracker, true);
						break;
					case Trackable.VERTICAL_SINGLE_AXIS_TRACKER:
						Util.selectSilently(miVerticalSingleAxisTracker, true);
						break;
					case Trackable.NO_TRACKER:
						Util.selectSilently(miNoTracker, true);
						break;
					}
					miAltazimuthDualAxisTracker.setEnabled(true);
					miHorizontalSingleAxisTracker.setEnabled(true);
					miVerticalSingleAxisTracker.setEnabled(true);
					if (sp.getContainer() instanceof Roof) {
						final Roof roof = (Roof) sp.getContainer();
						final boolean flat = Util.isZero(roof.getHeight());
						miAltazimuthDualAxisTracker.setEnabled(flat);
						miHorizontalSingleAxisTracker.setEnabled(flat);
						miVerticalSingleAxisTracker.setEnabled(flat);
					} else if (sp.getContainer() instanceof Wall || sp.getContainer() instanceof Rack) {
						miAltazimuthDualAxisTracker.setEnabled(false);
						miHorizontalSingleAxisTracker.setEnabled(false);
						miVerticalSingleAxisTracker.setEnabled(false);
					}
					if (sp.getTracker() != Trackable.NO_TRACKER) {
						miTiltAngle.setEnabled(sp.getTracker() == Trackable.VERTICAL_SINGLE_AXIS_TRACKER);
						miAzimuth.setEnabled(false);
					} else {
						miTiltAngle.setEnabled(true);
						miAzimuth.setEnabled(true);
						miBaseHeight.setEnabled(true);
						if (sp.getContainer() instanceof Roof) {
							final Roof roof = (Roof) sp.getContainer();
							if (roof.getHeight() > 0) {
								miTiltAngle.setEnabled(false);
								miAzimuth.setEnabled(false);
								miBaseHeight.setEnabled(false);
							}
						} else if (sp.getContainer() instanceof Wall || sp.getContainer() instanceof Rack) {
							miTiltAngle.setEnabled(false);
							miAzimuth.setEnabled(false);
							miBaseHeight.setEnabled(false);
						}
					}
				}
			});

			final JMenuItem miDeleteRow = new JMenuItem("Delete Row");
			miDeleteRow.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllSolarPanels(((SolarPanel) selectedPart).getRow());
							return null;
						}
					});
				}
			});
			popupMenuForSolarPanel.add(miDeleteRow);
			popupMenuForSolarPanel.addSeparator();
			popupMenuForSolarPanel.add(miModel);
			popupMenuForSolarPanel.add(miCells);
			popupMenuForSolarPanel.add(miSize);
			popupMenuForSolarPanel.add(miTemperatureEffects);
			popupMenuForSolarPanel.add(shadeToleranceMenu);
			popupMenuForSolarPanel.addSeparator();
			popupMenuForSolarPanel.add(miTiltAngle);
			popupMenuForSolarPanel.add(miAzimuth);
			popupMenuForSolarPanel.add(miBaseHeight);
			popupMenuForSolarPanel.add(miInverterEff);
			popupMenuForSolarPanel.add(orientationMenu);
			popupMenuForSolarPanel.add(trackerMenu);
			popupMenuForSolarPanel.addSeparator();
			popupMenuForSolarPanel.add(cbmiDrawSunBeam);
			popupMenuForSolarPanel.add(labelMenu);
			popupMenuForSolarPanel.addSeparator();

			JMenuItem mi = new JMenuItem("Daily Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof SolarPanel) {
						new PvDailyAnalysis().show();
					}
				}
			});
			popupMenuForSolarPanel.add(mi);

			mi = new JMenuItem("Annual Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof SolarPanel) {
						new PvAnnualAnalysis().show();
					}
				}
			});
			popupMenuForSolarPanel.add(mi);

		}

		return popupMenuForSolarPanel;

	}

}
