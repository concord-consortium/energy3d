package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.concurrent.Callable;

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
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Trackable;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.PvAnnualAnalysis;
import org.concord.energy3d.simulation.PvDailyAnalysis;
import org.concord.energy3d.simulation.PvModuleSpecs;
import org.concord.energy3d.simulation.PvModulesData;
import org.concord.energy3d.undo.ChangeAzimuthCommand;
import org.concord.energy3d.undo.ChangeAzimuthForAllRacksCommand;
import org.concord.energy3d.undo.ChangeBaseHeightCommand;
import org.concord.energy3d.undo.ChangeBaseHeightForAllRacksCommand;
import org.concord.energy3d.undo.ChangeFoundationRackAzimuthCommand;
import org.concord.energy3d.undo.ChangeFoundationRackBaseHeightCommand;
import org.concord.energy3d.undo.ChangeFoundationRackTiltAngleCommand;
import org.concord.energy3d.undo.ChangePoleSettingsForAllRacksCommand;
import org.concord.energy3d.undo.ChangePoleSettingsForRacksOnFoundationCommand;
import org.concord.energy3d.undo.ChangeRackPoleSettingsCommand;
import org.concord.energy3d.undo.ChangeTiltAngleCommand;
import org.concord.energy3d.undo.ChangeTiltAngleForAllRacksCommand;
import org.concord.energy3d.undo.ChooseSolarPanelModelForRackCommand;
import org.concord.energy3d.undo.ChooseSolarPanelSizeForRackCommand;
import org.concord.energy3d.undo.RotateSolarPanelsForRacksOnFoundationCommand;
import org.concord.energy3d.undo.RotateSolarPanelsOnAllRacksCommand;
import org.concord.energy3d.undo.RotateSolarPanelsOnRackCommand;
import org.concord.energy3d.undo.SetCellTypeForSolarPanelsOnRackCommand;
import org.concord.energy3d.undo.SetColorForSolarPanelsOnRackCommand;
import org.concord.energy3d.undo.SetInverterEfficiencyForAllRacksCommand;
import org.concord.energy3d.undo.SetInverterEfficiencyForRackCommand;
import org.concord.energy3d.undo.SetInverterEfficiencyForRacksOnFoundationCommand;
import org.concord.energy3d.undo.SetNoctForAllRacksCommand;
import org.concord.energy3d.undo.SetNoctForRackCommand;
import org.concord.energy3d.undo.SetNoctForRacksOnFoundationCommand;
import org.concord.energy3d.undo.SetPartSizeCommand;
import org.concord.energy3d.undo.SetRackLabelCommand;
import org.concord.energy3d.undo.SetShadeToleranceForSolarPanelsOnRackCommand;
import org.concord.energy3d.undo.SetSizeForAllRacksCommand;
import org.concord.energy3d.undo.SetSizeForRacksOnFoundationCommand;
import org.concord.energy3d.undo.SetSolarCellEfficiencyForAllRacksCommand;
import org.concord.energy3d.undo.SetSolarCellEfficiencyForRackCommand;
import org.concord.energy3d.undo.SetSolarCellEfficiencyForRacksOnFoundationCommand;
import org.concord.energy3d.undo.SetSolarPanelArrayOnRackByModelCommand;
import org.concord.energy3d.undo.SetSolarPanelArrayOnRackCustomCommand;
import org.concord.energy3d.undo.SetSolarPanelCellTypeForAllRacksCommand;
import org.concord.energy3d.undo.SetSolarPanelCellTypeForRacksOnFoundationCommand;
import org.concord.energy3d.undo.SetSolarPanelColorForAllRacksCommand;
import org.concord.energy3d.undo.SetSolarPanelColorForRacksOnFoundationCommand;
import org.concord.energy3d.undo.SetSolarPanelModelForAllRacksCommand;
import org.concord.energy3d.undo.SetSolarPanelModelForRacksOnFoundationCommand;
import org.concord.energy3d.undo.SetSolarPanelShadeToleranceForAllRacksCommand;
import org.concord.energy3d.undo.SetSolarPanelShadeToleranceForRacksOnFoundationCommand;
import org.concord.energy3d.undo.SetSolarPanelSizeForAllRacksCommand;
import org.concord.energy3d.undo.SetSolarPanelSizeForRacksOnFoundationCommand;
import org.concord.energy3d.undo.SetSolarTrackerCommand;
import org.concord.energy3d.undo.SetSolarTrackersForAllCommand;
import org.concord.energy3d.undo.SetSolarTrackersOnFoundationCommand;
import org.concord.energy3d.undo.SetTemperatureCoefficientPmaxForAllRacksCommand;
import org.concord.energy3d.undo.SetTemperatureCoefficientPmaxForRackCommand;
import org.concord.energy3d.undo.SetTemperatureCoefficientPmaxForRacksOnFoundationCommand;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.SpringUtilities;
import org.concord.energy3d.util.Util;

class PopupMenuForRack extends PopupMenuFactory {

	private static JPopupMenu popupMenuForRack;

	static JPopupMenu getPopupMenu() {

		if (popupMenuForRack == null) {

			final JMenu trackerMenu = new JMenu("Tracker");

			final JMenuItem miPaste = new JMenuItem("Paste");
			miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			miPaste.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().pasteToPickedLocationOnRack();
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
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack rack = (Rack) selectedPart;
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							if (rack.isMonolithic()) {
								rack.setMonolithic(false);
								rack.draw();
							} else {
								Scene.getInstance().removeAllSolarPanels(null);
								EventQueue.invokeLater(new Runnable() {
									@Override
									public void run() {
										MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									}
								});
							}
							return null;
						}
					});
				}
			});

			final JMenuItem miTiltAngle = new JMenuItem("Tilt Angle...");
			miTiltAngle.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Rack rack = (Rack) selectedPart;
					final String title = "<html>Tilt Angle of " + partInfo + " (&deg;)</html>";
					final String footnote = "<html><hr><font size=2>The tilt angle of a rack is the angle between its surface and the ground surface.<br>The tilt angle must be between -90&deg; and 90&deg;.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on This Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					final JTextField inputField = new JTextField(rack.getTiltAngle() + "");
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Tilt Angle");

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
										final ChangeTiltAngleCommand c = new ChangeTiltAngleCommand(rack);
										rack.setTiltAngle(val);
										rack.draw();
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = rack.getTopContainer();
										final ChangeFoundationRackTiltAngleCommand c = new ChangeFoundationRackTiltAngleCommand(foundation);
										foundation.setTiltAngleForRacks(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final ChangeTiltAngleForAllRacksCommand c = new ChangeTiltAngleForAllRacksCommand();
										Scene.getInstance().setTiltAngleForAllRacks(val);
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
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Rack rack = (Rack) selectedPart;
					final Foundation foundation = rack.getTopContainer();
					final String title = "<html>Azimuth Angle of " + partInfo + " (&deg;)</html>";
					final String footnote = "<html><hr><font size=2>The azimuth angle is measured clockwise from the true north.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					double a = rack.getRelativeAzimuth() + foundation.getAzimuth();
					if (a > 360) {
						a -= 360;
					}
					final JTextField inputField = new JTextField(a + "");
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Azimuth Angle");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							try {
								a = Double.parseDouble(inputField.getText()) - foundation.getAzimuth();
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (a < 0) {
									a += 360;
								}
								if (rb1.isSelected()) {
									final ChangeAzimuthCommand c = new ChangeAzimuthCommand(rack);
									rack.setRelativeAzimuth(a);
									rack.draw();
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 0;
								} else if (rb2.isSelected()) {
									final ChangeFoundationRackAzimuthCommand c = new ChangeFoundationRackAzimuthCommand(foundation);
									foundation.setAzimuthForRacks(a);
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 1;
								} else if (rb3.isSelected()) {
									final ChangeAzimuthForAllRacksCommand c = new ChangeAzimuthForAllRacksCommand();
									Scene.getInstance().setAzimuthForAllRacks(a);
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

			final JMenuItem miRackSize = new JMenuItem("Size...");
			miRackSize.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack rack = (Rack) selectedPart;
					final Foundation foundation = rack.getTopContainer();
					final String partInfo = rack.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel inputPanel = new JPanel(new SpringLayout());
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					gui.add(inputPanel, BorderLayout.CENTER);
					JLabel label = new JLabel("Width (m): ", JLabel.TRAILING);
					inputPanel.add(label);
					final JTextField widthField = new JTextField(threeDecimalsFormat.format(rack.getRackWidth()));
					label.setLabelFor(widthField);
					inputPanel.add(widthField);
					label = new JLabel("Length (m): ", JLabel.TRAILING);
					inputPanel.add(label);
					final JTextField heightField = new JTextField(threeDecimalsFormat.format(rack.getRackHeight()));
					label.setLabelFor(heightField);
					inputPanel.add(heightField);
					SpringUtilities.makeCompactGrid(inputPanel, 2, 2, 6, 6, 6, 6);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Rack Size");

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
								if (w < 1 || w > 500) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Width must be between 1 and 500 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (h < 1 || h > 20) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Length must be between 1 and 20 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final SetPartSizeCommand c = new SetPartSizeCommand(rack);
										rack.setRackWidth(w);
										rack.setRackHeight(h);
										rack.ensureFullSolarPanels(false);
										rack.draw();
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final SetSizeForRacksOnFoundationCommand c = new SetSizeForRacksOnFoundationCommand(foundation);
										foundation.setSizeForRacks(w, h);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final SetSizeForAllRacksCommand c = new SetSizeForAllRacksCommand();
										Scene.getInstance().setSizeForAllRacks(w, h);
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
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Rack rack = (Rack) selectedPart;
					final Foundation foundation = rack.getTopContainer();
					final String title = "<html>Base Height (m) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2></html>";
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					final JPanel gui = new JPanel(new BorderLayout());
					gui.add(panel, BorderLayout.CENTER);
					final JTextField inputField = new JTextField(rack.getBaseHeight() * Scene.getInstance().getAnnotationScale() + "");
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Base Height");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							double val = 0;
							try {
								val = Double.parseDouble(inputField.getText()) / Scene.getInstance().getAnnotationScale();
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (rb1.isSelected()) {
									final ChangeBaseHeightCommand c = new ChangeBaseHeightCommand(rack);
									rack.setBaseHeight(val);
									rack.draw();
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 0;
								} else if (rb2.isSelected()) {
									final ChangeFoundationRackBaseHeightCommand c = new ChangeFoundationRackBaseHeightCommand(foundation);
									foundation.setBaseHeightForRacks(val);
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 1;
								} else if (rb3.isSelected()) {
									final ChangeBaseHeightForAllRacksCommand c = new ChangeBaseHeightForAllRacksCommand();
									Scene.getInstance().setBaseHeightForAllRacks(val);
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

			final JMenuItem miPoleSpacing = new JMenuItem("Pole Settings...");
			miPoleSpacing.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack rack = (Rack) selectedPart;
					final String partInfo = rack.toString().substring(0, rack.toString().indexOf(')') + 1);
					final String title = "<html>Pole Settings of " + partInfo + "</html>";

					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
					gui.add(inputPanel, BorderLayout.CENTER);
					inputPanel.add(new JLabel("Distance X (m): "));
					final JTextField dxField = new JTextField(threeDecimalsFormat.format(rack.getPoleDistanceX()));
					inputPanel.add(dxField);
					inputPanel.add(new JLabel("Distance Y (m): "));
					final JTextField dyField = new JTextField(threeDecimalsFormat.format(rack.getPoleDistanceY()));
					inputPanel.add(dyField);
					inputPanel.add(new JLabel("Visible: "));
					final JComboBox<String> visibleComboBox = new JComboBox<String>(new String[] { "Yes", "No" });
					visibleComboBox.setSelectedIndex(rack.isPoleVisible() ? 0 : 1);
					inputPanel.add(visibleComboBox);
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Pole Settings");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							double dx = 0, dy = 0;
							try {
								dx = Double.parseDouble(dxField.getText());
								dy = Double.parseDouble(dyField.getText());
							} catch (final NumberFormatException x) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (dx < 1 || dx > 50) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Dx must be between 1 and 50 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (dy < 1 || dy > 50) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Dy must be between 1 and 50 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									final boolean visible = visibleComboBox.getSelectedIndex() == 0;
									if (rb1.isSelected()) {
										final ChangeRackPoleSettingsCommand c = new ChangeRackPoleSettingsCommand(rack);
										rack.setPoleDistanceX(dx);
										rack.setPoleDistanceY(dy);
										rack.setPoleVisible(visible);
										rack.draw();
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = rack.getTopContainer();
										final ChangePoleSettingsForRacksOnFoundationCommand c = new ChangePoleSettingsForRacksOnFoundationCommand(foundation);
										foundation.setPoleSpacingForRacks(dx, dy, visible);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final ChangePoleSettingsForAllRacksCommand c = new ChangePoleSettingsForAllRacksCommand();
										Scene.getInstance().setPoleSpacingForAllRacks(dx, dy, visible);
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

			final JMenuItem miSolarPanels = new JMenuItem("Select Solar Panels...");
			miSolarPanels.addActionListener(new ActionListener() {

				private Rack rack;
				private JComboBox<String> modelComboBox;
				private JComboBox<String> sizeComboBox;
				private JComboBox<String> orientationComboBox;
				private JComboBox<String> cellTypeComboBox;
				private JComboBox<String> colorOptionComboBox;
				private JComboBox<String> shadeToleranceComboBox;
				private JTextField cellEfficiencyField;
				private JTextField noctField;
				private JTextField pmaxTcField;
				private double cellEfficiency;
				private double inverterEfficiency;
				private double pmax;
				private double noct;

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					rack = (Rack) selectedPart;
					final int n = rack.getChildren().size();
					if (n > 0 && JOptionPane.showConfirmDialog(MainFrame.getInstance(), "All existing " + n + " solar panels on this rack must be removed before\na new layout can be applied. Do you want to continue?", "Confirmation", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					final SolarPanel solarPanel = rack.getSolarPanel();
					final JPanel panel = new JPanel(new SpringLayout());

					panel.add(new JLabel("Model"));
					modelComboBox = new JComboBox<String>();
					modelComboBox.addItem("Custom");
					final Map<String, PvModuleSpecs> modules = PvModulesData.getInstance().getModules();
					for (final String key : modules.keySet()) {
						modelComboBox.addItem(key);
					}
					if (solarPanel.getModelName() != null) {
						modelComboBox.setSelectedItem(solarPanel.getModelName());
					}
					modelComboBox.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(final ItemEvent e) {
							final boolean isCustom = modelComboBox.getSelectedIndex() == 0;
							sizeComboBox.setEnabled(isCustom);
							cellTypeComboBox.setEnabled(isCustom);
							colorOptionComboBox.setEnabled(isCustom);
							shadeToleranceComboBox.setEnabled(isCustom);
							cellEfficiencyField.setEnabled(isCustom);
							noctField.setEnabled(isCustom);
							pmaxTcField.setEnabled(isCustom);
							if (!isCustom) {
								final PvModuleSpecs specs = modules.get(modelComboBox.getSelectedItem());
								cellTypeComboBox.setSelectedItem(specs.getCellType());
								cellEfficiencyField.setText(threeDecimalsFormat.format(specs.getCelLEfficiency() * 100));
								noctField.setText(threeDecimalsFormat.format(specs.getNoct()));
								pmaxTcField.setText(sixDecimalsFormat.format(specs.getPmaxTc()));
								final String s = threeDecimalsFormat.format(specs.getNominalWidth()) + "m \u00D7 " + threeDecimalsFormat.format(specs.getNominalLength()) + "m (" + specs.getLayout().width + " \u00D7 " + specs.getLayout().height + " cells)";
								sizeComboBox.setSelectedItem(s);
								if ("Blue".equals(specs.getColor())) {
									colorOptionComboBox.setSelectedIndex(0);
								} else if ("Black".equals(specs.getColor())) {
									colorOptionComboBox.setSelectedIndex(1);
								} else if ("Gray".equals(specs.getColor())) {
									colorOptionComboBox.setSelectedIndex(2);
								}
							}
						}
					});
					panel.add(modelComboBox);

					// the following properties should be disabled when the model is not custom
					panel.add(new JLabel("Panel Size:"));
					sizeComboBox = new JComboBox<String>(new String[] { "0.99m \u00D7 1.65m (6 \u00D7 10 cells)", "1.05m \u00D7 1.56m (8 \u00D7 12 cells)", "0.99m \u00D7 1.96m (6 \u00D7 12 cells)", "0.6m \u00D7 1.2m (10 \u00D7 20 cells)" });
					final PvModuleSpecs specs = solarPanel.getPvModuleSpecs();
					final boolean isCustom = "Custom".equals(specs.getModel());
					final double width = isCustom ? solarPanel.getPanelWidth() : specs.getNominalWidth();
					final double height = isCustom ? solarPanel.getPanelHeight() : specs.getNominalLength();
					if (Util.isZero(0.99 - width) && Util.isZero(1.65 - height)) {
						sizeComboBox.setSelectedIndex(0);
					} else if (Util.isZero(1.05 - width) && Util.isZero(1.56 - height)) {
						sizeComboBox.setSelectedIndex(1);
					} else if (Util.isZero(0.99 - width) && Util.isZero(1.96 - height)) {
						sizeComboBox.setSelectedIndex(2);
					} else if (Util.isZero(0.6 - width) && Util.isZero(1.2 - height)) {
						sizeComboBox.setSelectedIndex(3);
					}
					panel.add(sizeComboBox);
					panel.add(new JLabel("Cell Type:"));
					cellTypeComboBox = new JComboBox<String>(new String[] { "Polycrystalline", "Monocrystalline", "Thin Film" });
					cellTypeComboBox.setSelectedIndex(solarPanel.getCellType());
					panel.add(cellTypeComboBox);
					panel.add(new JLabel("Color:"));
					colorOptionComboBox = new JComboBox<String>(new String[] { "Blue", "Black", "Gray" });
					colorOptionComboBox.setSelectedIndex(solarPanel.getColorOption());
					panel.add(colorOptionComboBox);
					panel.add(new JLabel("Solar Cell Efficiency (%):"));
					cellEfficiencyField = new JTextField(threeDecimalsFormat.format(solarPanel.getCellEfficiency() * 100));
					panel.add(cellEfficiencyField);
					panel.add(new JLabel("<html>Nominal Operating Cell Temperature (&deg;C):"));
					noctField = new JTextField(threeDecimalsFormat.format(solarPanel.getNominalOperatingCellTemperature()));
					panel.add(noctField);
					panel.add(new JLabel("<html>Temperature Coefficient of Pmax (%/&deg;C):"));
					pmaxTcField = new JTextField(sixDecimalsFormat.format(solarPanel.getTemperatureCoefficientPmax() * 100));
					panel.add(pmaxTcField);
					panel.add(new JLabel("Shade Tolerance:"));
					shadeToleranceComboBox = new JComboBox<String>(new String[] { "Partial", "High", "None" });
					shadeToleranceComboBox.setSelectedIndex(solarPanel.getShadeTolerance());
					panel.add(shadeToleranceComboBox);

					if (modelComboBox.getSelectedIndex() != 0) {
						sizeComboBox.setEnabled(false);
						cellTypeComboBox.setEnabled(false);
						colorOptionComboBox.setEnabled(false);
						shadeToleranceComboBox.setEnabled(false);
						cellEfficiencyField.setEnabled(false);
						noctField.setEnabled(false);
						pmaxTcField.setEnabled(false);
					}

					// the following properties are not related to the model
					panel.add(new JLabel("Orientation:"));
					orientationComboBox = new JComboBox<String>(new String[] { "Portrait", "Landscape" });
					orientationComboBox.setSelectedIndex(solarPanel.isRotated() ? 1 : 0);
					panel.add(orientationComboBox);
					panel.add(new JLabel("Inverter Efficiency (%):"));
					final JTextField inverterEfficiencyField = new JTextField(threeDecimalsFormat.format(solarPanel.getInverterEfficiency() * 100));
					panel.add(inverterEfficiencyField);
					SpringUtilities.makeCompactGrid(panel, 10, 2, 6, 6, 6, 6);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panels on this Rack");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							if (modelComboBox.getSelectedIndex() == 0) {
								try {
									cellEfficiency = Double.parseDouble(cellEfficiencyField.getText());
									pmax = Double.parseDouble(pmaxTcField.getText());
									noct = Double.parseDouble(noctField.getText());
									inverterEfficiency = Double.parseDouble(inverterEfficiencyField.getText());
								} catch (final NumberFormatException ex) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
									ok = false;
								}
								if (ok) {
									if (cellEfficiency < SolarPanel.MIN_SOLAR_CELL_EFFICIENCY_PERCENTAGE || cellEfficiency > SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar cell efficiency must be between " + SolarPanel.MIN_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "% and " + SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "%.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (inverterEfficiency < SolarPanel.MIN_INVERTER_EFFICIENCY_PERCENTAGE || inverterEfficiency >= SolarPanel.MAX_INVERTER_EFFICIENCY_PERCENTAGE) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Inverter efficiency must be greater than " + SolarPanel.MIN_INVERTER_EFFICIENCY_PERCENTAGE + "% and less than " + SolarPanel.MAX_INVERTER_EFFICIENCY_PERCENTAGE + "%.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (pmax < -1 || pmax > 0) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Temperature coefficient of Pmax must be between -1% and 0% per Celsius degree.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (noct < 33 || noct > 58) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Nominal Cell Operating Temperature must be between 33 and 58 Celsius degrees.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else {
										setCustomSolarPanels();
										if (choice == options[0]) {
											break;
										}
									}
								}
							} else {
								try {
									inverterEfficiency = Double.parseDouble(inverterEfficiencyField.getText());
								} catch (final NumberFormatException ex) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
									ok = false;
								}
								if (ok) {
									if (inverterEfficiency < SolarPanel.MIN_INVERTER_EFFICIENCY_PERCENTAGE || inverterEfficiency >= SolarPanel.MAX_INVERTER_EFFICIENCY_PERCENTAGE) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Inverter efficiency must be greater than " + SolarPanel.MIN_INVERTER_EFFICIENCY_PERCENTAGE + "% and less than " + SolarPanel.MAX_INVERTER_EFFICIENCY_PERCENTAGE + "%.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else {
										setBrandSolarPanels((String) modelComboBox.getSelectedItem());
										if (choice == options[0]) {
											break;
										}
									}
								}
							}
						}
					}
				}

				private void setBrandSolarPanels(final String modelName) {
					final SolarPanel solarPanel = rack.getSolarPanel();
					final SetSolarPanelArrayOnRackByModelCommand command = rack.isMonolithic() ? new SetSolarPanelArrayOnRackByModelCommand(rack) : null;
					solarPanel.setRotated(orientationComboBox.getSelectedIndex() == 1);
					solarPanel.setInverterEfficiency(inverterEfficiency * 0.01);
					solarPanel.setPvModuleSpecs(PvModulesData.getInstance().getModuleSpecs(modelName));
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							rack.addSolarPanels();
							if (command != null) {
								SceneManager.getInstance().getUndoManager().addEdit(command);
							}
							return null;
						}
					});
					updateAfterEdit();
				}

				private void setCustomSolarPanels() {
					final SolarPanel solarPanel = rack.getSolarPanel();
					solarPanel.setModelName("Custom");
					solarPanel.setBrandName("Custom");
					final SetSolarPanelArrayOnRackCustomCommand command = rack.isMonolithic() ? new SetSolarPanelArrayOnRackCustomCommand(rack) : null;
					switch (sizeComboBox.getSelectedIndex()) {
					case 0:
						solarPanel.setPanelWidth(0.99);
						solarPanel.setPanelHeight(1.65);
						solarPanel.setNumberOfCellsInX(6);
						solarPanel.setNumberOfCellsInX(10);
						break;
					case 1:
						solarPanel.setPanelWidth(1.05);
						solarPanel.setPanelHeight(1.56);
						solarPanel.setNumberOfCellsInX(8);
						solarPanel.setNumberOfCellsInX(12);
						break;
					case 2:
						solarPanel.setPanelWidth(0.99);
						solarPanel.setPanelHeight(1.96);
						solarPanel.setNumberOfCellsInX(6);
						solarPanel.setNumberOfCellsInX(12);
						break;
					case 3:
						solarPanel.setPanelWidth(0.6);
						solarPanel.setPanelHeight(1.2);
						solarPanel.setNumberOfCellsInX(10);
						solarPanel.setNumberOfCellsInX(20);
						break;
					}
					solarPanel.setRotated(orientationComboBox.getSelectedIndex() == 1);
					solarPanel.setCellType(cellTypeComboBox.getSelectedIndex());
					solarPanel.setColorOption(colorOptionComboBox.getSelectedIndex());
					solarPanel.setCellEfficiency(cellEfficiency * 0.01);
					solarPanel.setInverterEfficiency(inverterEfficiency * 0.01);
					solarPanel.setTemperatureCoefficientPmax(pmax * 0.01);
					solarPanel.setNominalOperatingCellTemperature(noct);
					solarPanel.setShadeTolerance(shadeToleranceComboBox.getSelectedIndex());
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							rack.addSolarPanels();
							if (command != null) {
								SceneManager.getInstance().getUndoManager().addEdit(command);
							}
							return null;
						}
					});
					updateAfterEdit();
				}

			});

			final JMenu solarPanelMenu = new JMenu("Change Solar Panel Properties");

			final JMenuItem miSolarPanelModel = new JMenuItem("Model...");
			solarPanelMenu.add(miSolarPanelModel);
			miSolarPanelModel.addActionListener(new ActionListener() {

				private String modelName;
				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final SolarPanel s = r.getSolarPanel();
					final String partInfo = r.toString().substring(0, r.toString().indexOf(')') + 1);
					final Map<String, PvModuleSpecs> modules = PvModulesData.getInstance().getModules();
					final String[] models = new String[modules.size() + 1];
					int i = 0;
					models[i] = "Custom";
					for (final String key : modules.keySet()) {
						models[++i] = key;
					}
					final PvModuleSpecs specs = s.getPvModuleSpecs();
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					gui.setBorder(BorderFactory.createTitledBorder("Solar Panel Model for " + partInfo));
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
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final ChooseSolarPanelModelForRackCommand c = new ChooseSolarPanelModelForRackCommand(r);
								s.setPvModuleSpecs(PvModulesData.getInstance().getModuleSpecs(modelName));
								r.ensureFullSolarPanels(false);
								r.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final SetSolarPanelModelForRacksOnFoundationCommand c = new SetSolarPanelModelForRacksOnFoundationCommand(foundation);
								foundation.setSolarPanelModelForRacks(PvModulesData.getInstance().getModuleSpecs(modelName));
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetSolarPanelModelForAllRacksCommand c = new SetSolarPanelModelForAllRacksCommand();
								Scene.getInstance().setSolarPanelModelForAllRacks(PvModulesData.getInstance().getModuleSpecs(modelName));
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

			solarPanelMenu.addSeparator();

			final JMenuItem miSolarPanelSize = new JMenuItem("Size...");
			solarPanelMenu.add(miSolarPanelSize);
			miSolarPanelSize.addActionListener(new ActionListener() {

				private double w = 0.99;
				private double h = 1.65;
				private int numberOfCellsInX = 6;
				private int numberOfCellsInY = 10;
				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final SolarPanel s = r.getSolarPanel();
					w = s.getPanelWidth();
					h = s.getPanelHeight();
					final String partInfo = r.toString().substring(0, r.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					gui.setBorder(BorderFactory.createTitledBorder("Solar Panel Size for " + partInfo));
					final String[] sizes = new String[] { "0.99m \u00D7 1.65m (6 \u00D7 10 cells)", "1.05m \u00D7 1.56m (8 \u00D7 12 cells)", "0.99m \u00D7 1.96m (6 \u00D7 12 cells)", "0.6m \u00D7 1.2m (10 \u00D7 20 cells)" };
					final JComboBox<String> typeComboBox = new JComboBox<String>(sizes);
					final PvModuleSpecs specs = s.getPvModuleSpecs();
					final boolean isCustom = "Custom".equals(specs.getModel());
					final double width = isCustom ? s.getPanelWidth() : specs.getNominalWidth();
					final double height = isCustom ? s.getPanelHeight() : specs.getNominalLength();
					if (Util.isZero(height - 1.65) && Util.isZero(width - 0.99)) {
						typeComboBox.setSelectedIndex(0);
					} else if (Util.isZero(height - 1.56) && Util.isZero(width - 1.05)) {
						typeComboBox.setSelectedIndex(1);
					} else if (Util.isZero(height - 1.96) && Util.isZero(width - 0.99)) {
						typeComboBox.setSelectedIndex(2);
					} else if (Util.isZero(height - 1.2) && Util.isZero(width - 0.6)) {
						typeComboBox.setSelectedIndex(3);
					}
					typeComboBox.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(final ItemEvent e) {
							switch (typeComboBox.getSelectedIndex()) {
							case 0:
								w = 0.99;
								h = 1.65;
								numberOfCellsInX = 6;
								numberOfCellsInY = 10;
								break;
							case 1:
								w = 1.05;
								h = 1.56;
								numberOfCellsInX = 8;
								numberOfCellsInY = 12;
								break;
							case 2:
								w = 0.99;
								h = 1.96;
								numberOfCellsInX = 6;
								numberOfCellsInY = 12;
								break;
							case 3:
								w = 0.6;
								h = 1.2;
								numberOfCellsInX = 10;
								numberOfCellsInY = 20;
								break;
							}
						}
					});
					gui.add(typeComboBox, BorderLayout.NORTH);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Size");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final ChooseSolarPanelSizeForRackCommand c = new ChooseSolarPanelSizeForRackCommand(r);
								s.setPanelWidth(w);
								s.setPanelHeight(h);
								s.setNumberOfCellsInX(numberOfCellsInX);
								s.setNumberOfCellsInY(numberOfCellsInY);
								r.ensureFullSolarPanels(false);
								r.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final SetSolarPanelSizeForRacksOnFoundationCommand c = new SetSolarPanelSizeForRacksOnFoundationCommand(foundation);
								foundation.setSolarPanelSizeForRacks(w, h, numberOfCellsInX, numberOfCellsInY);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetSolarPanelSizeForAllRacksCommand c = new SetSolarPanelSizeForAllRacksCommand();
								Scene.getInstance().setSolarPanelSizeForAllRacks(w, h, numberOfCellsInX, numberOfCellsInY);
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

			final JMenuItem miSolarPanelColor = new JMenuItem("Color...");
			final JMenuItem miSolarPanelCellType = new JMenuItem("Cell Type...");
			solarPanelMenu.add(miSolarPanelCellType);
			miSolarPanelCellType.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final SolarPanel s = r.getSolarPanel();
					final String partInfo = r.toString().substring(0, r.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					gui.setBorder(BorderFactory.createTitledBorder("Choose Cell Type for " + partInfo));
					final JComboBox<String> cellTypeComboBox = new JComboBox<String>(new String[] { "Polycrystalline", "Monocrystalline", "Thin Film" });
					cellTypeComboBox.setSelectedIndex(s.getCellType());
					gui.add(cellTypeComboBox, BorderLayout.NORTH);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Cell Type");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final SetCellTypeForSolarPanelsOnRackCommand c = new SetCellTypeForSolarPanelsOnRackCommand(r);
								s.setCellType(cellTypeComboBox.getSelectedIndex());
								r.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final SetSolarPanelCellTypeForRacksOnFoundationCommand c = new SetSolarPanelCellTypeForRacksOnFoundationCommand(foundation);
								foundation.setSolarPanelCellTypeForRacks(cellTypeComboBox.getSelectedIndex());
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetSolarPanelCellTypeForAllRacksCommand c = new SetSolarPanelCellTypeForAllRacksCommand();
								Scene.getInstance().setSolarPanelCellTypeForAllRacks(cellTypeComboBox.getSelectedIndex());
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

			solarPanelMenu.add(miSolarPanelColor);
			miSolarPanelColor.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final SolarPanel s = r.getSolarPanel();
					final String partInfo = r.toString().substring(0, r.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					gui.setBorder(BorderFactory.createTitledBorder("Choose Color for " + partInfo));
					final JComboBox<String> colorComboBox = new JComboBox<String>(new String[] { "Blue", "Black", "Gray" });
					colorComboBox.setSelectedIndex(s.getColorOption());
					gui.add(colorComboBox, BorderLayout.NORTH);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Color");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final SetColorForSolarPanelsOnRackCommand c = new SetColorForSolarPanelsOnRackCommand(r);
								s.setColorOption(colorComboBox.getSelectedIndex());
								r.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final SetSolarPanelColorForRacksOnFoundationCommand c = new SetSolarPanelColorForRacksOnFoundationCommand(foundation);
								foundation.setSolarPanelColorForRacks(colorComboBox.getSelectedIndex());
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetSolarPanelColorForAllRacksCommand c = new SetSolarPanelColorForAllRacksCommand();
								Scene.getInstance().setSolarPanelColorForAllRacks(colorComboBox.getSelectedIndex());
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

			final JMenuItem miSolarPanelCellEfficiency = new JMenuItem("Solar Cell Efficiency...");
			solarPanelMenu.add(miSolarPanelCellEfficiency);
			miSolarPanelCellEfficiency.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final SolarPanel s = r.getSolarPanel();
					final String title = "Set Solar Cell Efficiency (%) for " + r.toString().substring(0, r.toString().indexOf(')') + 1);
					final String footnote = "<html><hr><font size=2>How efficient can a solar panel be for converting light into electricity?<br>The Shockley-Queisser limit is 34%.<br>The theoretical limit for multilayer cells is 86%.<br>As of 2017, the best solar panel in the market has an efficiency of 24%.<br>The highest efficiency you can choose is limited to " + SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "%.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					final JTextField inputField = new JTextField(threeDecimalsFormat.format(s.getCellEfficiency() * 100));
					gui.add(inputField, BorderLayout.NORTH);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Cell Efficiency");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							try {
								solarCellEfficiencyPercentage = Double.parseDouble(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (solarCellEfficiencyPercentage < SolarPanel.MIN_SOLAR_CELL_EFFICIENCY_PERCENTAGE || solarCellEfficiencyPercentage > SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar cell efficiency must be between " + SolarPanel.MIN_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "% and " + SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final SetSolarCellEfficiencyForRackCommand c = new SetSolarCellEfficiencyForRackCommand(r);
										s.setCellEfficiency(solarCellEfficiencyPercentage * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final SetSolarCellEfficiencyForRacksOnFoundationCommand c = new SetSolarCellEfficiencyForRacksOnFoundationCommand(foundation);
										foundation.setSolarCellEfficiencyForRacks(solarCellEfficiencyPercentage * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final SetSolarCellEfficiencyForAllRacksCommand c = new SetSolarCellEfficiencyForAllRacksCommand();
										Scene.getInstance().setSolarCellEfficiencyForAllRacks(solarCellEfficiencyPercentage * 0.01);
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

			final JMenuItem miSolarPanelNoct = new JMenuItem("Nominal Operating Cell Temperature...");
			solarPanelMenu.add(miSolarPanelNoct);
			miSolarPanelNoct.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final SolarPanel s = r.getSolarPanel();
					final String title = "<html>Nominal Operating Cell Temperature (&deg;C) for " + r.toString().substring(0, r.toString().indexOf(')') + 1);
					final String footnote = "<html><hr><font size=2>Increased temperature reduces solar cell efficiency.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					final JTextField inputField = new JTextField(threeDecimalsFormat.format(s.getNominalOperatingCellTemperature()));
					gui.add(inputField, BorderLayout.NORTH);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Nominal Operating Cell Temperature");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							try {
								solarPanelNominalOperatingCellTemperature = Double.parseDouble(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (solarPanelNominalOperatingCellTemperature < 33 || solarPanelNominalOperatingCellTemperature > 58) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Nominal Operating Cell Temperature must be between 33 and 58 degrees.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final SetNoctForRackCommand c = new SetNoctForRackCommand(r);
										s.setNominalOperatingCellTemperature(solarPanelNominalOperatingCellTemperature);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final SetNoctForRacksOnFoundationCommand c = new SetNoctForRacksOnFoundationCommand(foundation);
										foundation.setNominalOperatingCellTemperatureForRacks(solarPanelNominalOperatingCellTemperature);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final SetNoctForAllRacksCommand c = new SetNoctForAllRacksCommand();
										Scene.getInstance().setNominalOperatingCellTemperatureForAllRacks(solarPanelNominalOperatingCellTemperature);
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

			final JMenuItem miSolarPanelPmaxTc = new JMenuItem("Temperature Coefficient of Pmax...");
			solarPanelMenu.add(miSolarPanelPmaxTc);
			miSolarPanelPmaxTc.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final SolarPanel s = r.getSolarPanel();
					final String title = "<html>Temperature Coefficienct of Pmax (%/&deg;C) for " + r.toString().substring(0, r.toString().indexOf(')') + 1);
					final String footnote = "<html><hr><font size=2>Increased temperature reduces solar cell efficiency.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					final JTextField inputField = new JTextField(threeDecimalsFormat.format(s.getTemperatureCoefficientPmax() * 100));
					gui.add(inputField, BorderLayout.NORTH);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Temperature Coefficient of Pmax");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							try {
								solarPanelTemperatureCoefficientPmaxPercentage = Double.parseDouble(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (solarPanelTemperatureCoefficientPmaxPercentage < -1 || solarPanelTemperatureCoefficientPmaxPercentage > 0) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Temperature coefficient of Pmax must be between -1 and 0", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final SetTemperatureCoefficientPmaxForRackCommand c = new SetTemperatureCoefficientPmaxForRackCommand(r);
										s.setTemperatureCoefficientPmax(solarPanelTemperatureCoefficientPmaxPercentage * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final SetTemperatureCoefficientPmaxForRacksOnFoundationCommand c = new SetTemperatureCoefficientPmaxForRacksOnFoundationCommand(foundation);
										foundation.setTemperatureCoefficientPmaxForRacks(solarPanelTemperatureCoefficientPmaxPercentage * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final SetTemperatureCoefficientPmaxForAllRacksCommand c = new SetTemperatureCoefficientPmaxForAllRacksCommand();
										Scene.getInstance().setTemperatureCoefficientPmaxForAllRacks(solarPanelTemperatureCoefficientPmaxPercentage * 0.01);
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

			final JMenuItem miSolarPanelShadeTolerance = new JMenuItem("Shade Tolerance...");
			solarPanelMenu.add(miSolarPanelShadeTolerance);
			miSolarPanelShadeTolerance.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final SolarPanel s = r.getSolarPanel();
					final String title = "Set Solar Panel Shade Tolerance for " + r.toString().substring(0, r.toString().indexOf(')') + 1);
					final String footnote = "<html><hr><font size=2>Use bypass diodes to direct current under shading conditions.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					final JComboBox<String> toleranceComboBox = new JComboBox<String>(new String[] { "Partial", "High", "None" });
					toleranceComboBox.setSelectedIndex(s.getShadeTolerance());
					gui.add(toleranceComboBox, BorderLayout.NORTH);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Shade Tolerance");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final SetShadeToleranceForSolarPanelsOnRackCommand c = new SetShadeToleranceForSolarPanelsOnRackCommand(r);
								s.setShadeTolerance(toleranceComboBox.getSelectedIndex());
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final SetSolarPanelShadeToleranceForRacksOnFoundationCommand c = new SetSolarPanelShadeToleranceForRacksOnFoundationCommand(foundation);
								foundation.setSolarPanelShadeToleranceForRacks(toleranceComboBox.getSelectedIndex());
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetSolarPanelShadeToleranceForAllRacksCommand c = new SetSolarPanelShadeToleranceForAllRacksCommand();
								Scene.getInstance().setSolarPanelShadeToleranceForAllRacks(toleranceComboBox.getSelectedIndex());
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

			solarPanelMenu.addSeparator();

			final JMenuItem miSolarPanelOrientation = new JMenuItem("Orientation...");
			solarPanelMenu.add(miSolarPanelOrientation);
			miSolarPanelOrientation.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final SolarPanel s = r.getSolarPanel();
					final String partInfo = r.toString().substring(0, r.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					gui.setBorder(BorderFactory.createTitledBorder("Solar Panel Orientation for " + partInfo));
					final JComboBox<String> orientationComboBox = new JComboBox<String>(new String[] { "Portrait", "Landscape" });
					orientationComboBox.setSelectedIndex(s.isRotated() ? 1 : 0);
					gui.add(orientationComboBox, BorderLayout.NORTH);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Orientation");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final RotateSolarPanelsOnRackCommand c = new RotateSolarPanelsOnRackCommand(r);
								s.setRotated(orientationComboBox.getSelectedIndex() == 1);
								r.ensureFullSolarPanels(false);
								r.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final RotateSolarPanelsForRacksOnFoundationCommand c = new RotateSolarPanelsForRacksOnFoundationCommand(foundation);
								foundation.rotateSolarPanelsOnRacks(orientationComboBox.getSelectedIndex() == 1);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final RotateSolarPanelsOnAllRacksCommand c = new RotateSolarPanelsOnAllRacksCommand();
								Scene.getInstance().rotateSolarPanelsOnAllRacks(orientationComboBox.getSelectedIndex() == 1);
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

			final JMenuItem miInverterEfficiency = new JMenuItem("Inverter Efficiency...");
			solarPanelMenu.add(miInverterEfficiency);
			miInverterEfficiency.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final SolarPanel s = r.getSolarPanel();
					final String title = "Set Inverter Efficiency (%) for " + r.toString().substring(0, r.toString().indexOf(')') + 1);
					final String footnote = "<html><hr><font size=2>The efficiency of a micro inverter for converting electricity<br>from DC to AC is typically 95%.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					final JTextField inputField = new JTextField(threeDecimalsFormat.format(s.getInverterEfficiency() * 100));
					gui.add(inputField, BorderLayout.NORTH);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Inverter Efficiency");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							try {
								inverterEfficiencyPercentage = Double.parseDouble(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (inverterEfficiencyPercentage < SolarPanel.MIN_INVERTER_EFFICIENCY_PERCENTAGE || inverterEfficiencyPercentage > SolarPanel.MAX_INVERTER_EFFICIENCY_PERCENTAGE) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Inverter efficiency must be between " + SolarPanel.MIN_INVERTER_EFFICIENCY_PERCENTAGE + "% and " + SolarPanel.MAX_INVERTER_EFFICIENCY_PERCENTAGE + "%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final SetInverterEfficiencyForRackCommand c = new SetInverterEfficiencyForRackCommand(r);
										s.setInverterEfficiency(inverterEfficiencyPercentage * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final SetInverterEfficiencyForRacksOnFoundationCommand c = new SetInverterEfficiencyForRacksOnFoundationCommand(foundation);
										foundation.setInverterEfficiencyForRacks(inverterEfficiencyPercentage * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final SetInverterEfficiencyForAllRacksCommand c = new SetInverterEfficiencyForAllRacksCommand();
										Scene.getInstance().setInverterEfficiencyForAllRacks(inverterEfficiencyPercentage * 0.01);
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

			final ButtonGroup trackerButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem miNoTracker = new JRadioButtonMenuItem("No Tracker...", true);
			trackerButtonGroup.add(miNoTracker);
			miNoTracker.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack rack = (Rack) selectedPart;
					final String partInfo = rack.toString().substring(0, rack.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					final String title = "<html>Disable tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>No tracker will be used.<hr></html>";
					final Object[] params = { title, footnote, panel };
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), params, "Disable solar tracker", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					if (rb1.isSelected()) {
						final SetSolarTrackerCommand c = new SetSolarTrackerCommand(rack);
						rack.setTracker(Trackable.NO_TRACKER);
						rack.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 0;
					} else if (rb2.isSelected()) {
						final Foundation foundation = rack.getTopContainer();
						final SetSolarTrackersOnFoundationCommand c = new SetSolarTrackersOnFoundationCommand(foundation, rack);
						foundation.setTrackerForRacks(Trackable.NO_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 1;
					} else if (rb3.isSelected()) {
						final SetSolarTrackersForAllCommand c = new SetSolarTrackersForAllCommand(rack);
						Scene.getInstance().setTrackerForAllRacks(Trackable.NO_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 2;
					}
					updateAfterEdit();
				}
			});

			final JRadioButtonMenuItem miHorizontalSingleAxisTracker = new JRadioButtonMenuItem("Horizontal Single-Axis Tracker...");
			trackerButtonGroup.add(miHorizontalSingleAxisTracker);
			miHorizontalSingleAxisTracker.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack rack = (Rack) selectedPart;
					final String partInfo = rack.toString().substring(0, rack.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					final String title = "<html>Enable horizontal single-axis tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2><hr></html>";
					final Object[] params = { title, footnote, panel };
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), params, "Enable horizontal single-axis solar tracker", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					if (rb1.isSelected()) {
						final SetSolarTrackerCommand c = new SetSolarTrackerCommand(rack);
						rack.setTracker(Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER);
						rack.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 0;
					} else if (rb2.isSelected()) {
						final Foundation foundation = rack.getTopContainer();
						final SetSolarTrackersOnFoundationCommand c = new SetSolarTrackersOnFoundationCommand(foundation, rack);
						foundation.setTrackerForRacks(Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 1;
					} else if (rb3.isSelected()) {
						final SetSolarTrackersForAllCommand c = new SetSolarTrackersForAllCommand(rack);
						Scene.getInstance().setTrackerForAllRacks(Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 2;
					}
					updateAfterEdit();
				}
			});

			final JRadioButtonMenuItem miVerticalSingleAxisTracker = new JRadioButtonMenuItem("Vertical Single-Axis Tracker...");
			trackerButtonGroup.add(miVerticalSingleAxisTracker);
			miVerticalSingleAxisTracker.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack rack = (Rack) selectedPart;
					final String partInfo = rack.toString().substring(0, rack.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					final String title = "<html>Enable vertical single-axis tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2><hr></html>";
					final Object[] params = { title, footnote, panel };
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), params, "Enable vertical single-axis solar tracker", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					if (rb1.isSelected()) {
						final SetSolarTrackerCommand c = new SetSolarTrackerCommand(rack);
						rack.setTracker(Trackable.VERTICAL_SINGLE_AXIS_TRACKER);
						rack.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 0;
					} else if (rb2.isSelected()) {
						final Foundation foundation = rack.getTopContainer();
						final SetSolarTrackersOnFoundationCommand c = new SetSolarTrackersOnFoundationCommand(foundation, rack);
						foundation.setTrackerForRacks(Trackable.VERTICAL_SINGLE_AXIS_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 1;
					} else if (rb3.isSelected()) {
						final SetSolarTrackersForAllCommand c = new SetSolarTrackersForAllCommand(rack);
						Scene.getInstance().setTrackerForAllRacks(Trackable.VERTICAL_SINGLE_AXIS_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 2;
					}
					updateAfterEdit();
				}
			});

			final JRadioButtonMenuItem miAltazimuthDualAxisTracker = new JRadioButtonMenuItem("Altazimuth Dual-Axis Tracker...");
			trackerButtonGroup.add(miAltazimuthDualAxisTracker);
			miAltazimuthDualAxisTracker.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack rack = (Rack) selectedPart;
					final String partInfo = rack.toString().substring(0, rack.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					final String title = "<html>Enable altitude-azimuth dual-axis tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The Alt/Az dual-axis solar tracker will rotate the solar panel to face the sun exactly.<hr></html>";
					final Object[] params = { title, footnote, panel };
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), params, "Enable altitude-azimuth dual-axis solar tracker", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					if (rb1.isSelected()) {
						final SetSolarTrackerCommand c = new SetSolarTrackerCommand(rack);
						rack.setTracker(Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER);
						rack.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 0;
					} else if (rb2.isSelected()) {
						final Foundation foundation = rack.getTopContainer();
						final SetSolarTrackersOnFoundationCommand c = new SetSolarTrackersOnFoundationCommand(foundation, rack);
						foundation.setTrackerForRacks(Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 1;
					} else if (rb3.isSelected()) {
						final SetSolarTrackersForAllCommand c = new SetSolarTrackersForAllCommand(rack);
						Scene.getInstance().setTrackerForAllRacks(Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 2;
					}
					updateAfterEdit();
				}
			});

			trackerMenu.add(miNoTracker);
			trackerMenu.add(miHorizontalSingleAxisTracker);
			trackerMenu.add(miVerticalSingleAxisTracker);
			trackerMenu.add(miAltazimuthDualAxisTracker);

			final JCheckBoxMenuItem cbmiDrawSunBeam = new JCheckBoxMenuItem("Draw Sun Beam");
			cbmiDrawSunBeam.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack rack = (Rack) selectedPart;
					rack.setSunBeamVisible(cbmiDrawSunBeam.isSelected());
					rack.drawSunBeam();
					rack.draw();
					Scene.getInstance().setEdited(true);
				}
			});

			final JMenu labelMenu = new JMenu("Label");

			final JCheckBoxMenuItem miLabelNone = new JCheckBoxMenuItem("None", true);
			miLabelNone.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (miLabelNone.isSelected()) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Rack) {
							final Rack r = (Rack) selectedPart;
							final SetRackLabelCommand c = new SetRackLabelCommand(r);
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
					if (selectedPart instanceof Rack) {
						final Rack r = (Rack) selectedPart;
						final SetRackLabelCommand c = new SetRackLabelCommand(r);
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
					if (selectedPart instanceof Rack) {
						final Rack r = (Rack) selectedPart;
						final SetRackLabelCommand c = new SetRackLabelCommand(r);
						r.setLabelId(miLabelId.isSelected());
						r.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelId);

			final JCheckBoxMenuItem miLabelCellEfficiency = new JCheckBoxMenuItem("Cell Efficiency");
			miLabelCellEfficiency.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Rack) {
						final Rack r = (Rack) selectedPart;
						final SetRackLabelCommand c = new SetRackLabelCommand(r);
						r.setLabelCellEfficiency(miLabelCellEfficiency.isSelected());
						r.draw();
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
					if (selectedPart instanceof Rack) {
						final Rack r = (Rack) selectedPart;
						final SetRackLabelCommand c = new SetRackLabelCommand(r);
						r.setLabelTiltAngle(miLabelTiltAngle.isSelected());
						r.draw();
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
					if (selectedPart instanceof Rack) {
						final Rack r = (Rack) selectedPart;
						final SetRackLabelCommand c = new SetRackLabelCommand(r);
						r.setLabelTracker(miLabelTracker.isSelected());
						r.draw();
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
					if (selectedPart instanceof Rack) {
						final Rack r = (Rack) selectedPart;
						final SetRackLabelCommand c = new SetRackLabelCommand(r);
						r.setLabelEnergyOutput(miLabelEnergyOutput.isSelected());
						r.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelEnergyOutput);

			popupMenuForRack = createPopupMenu(true, true, new Runnable() {

				@Override
				public void run() {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
					miPaste.setEnabled(copyBuffer instanceof SolarPanel);
					final Rack rack = (Rack) selectedPart;
					switch (rack.getTracker()) {
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
					if (rack.getContainer() instanceof Roof) {
						final Roof roof = (Roof) rack.getContainer();
						final boolean flat = Util.isZero(roof.getHeight());
						miAltazimuthDualAxisTracker.setEnabled(flat);
						miHorizontalSingleAxisTracker.setEnabled(flat);
						miVerticalSingleAxisTracker.setEnabled(flat);
					}
					if (rack.getTracker() != Trackable.NO_TRACKER) {
						miTiltAngle.setEnabled(rack.getTracker() == Trackable.VERTICAL_SINGLE_AXIS_TRACKER);
						miAzimuth.setEnabled(false);
					} else {
						miTiltAngle.setEnabled(true);
						miAzimuth.setEnabled(true);
						miBaseHeight.setEnabled(true);
						miPoleSpacing.setEnabled(true);
						if (rack.getContainer() instanceof Roof) {
							final Roof roof = (Roof) rack.getContainer();
							if (roof.getHeight() > 0) {
								miTiltAngle.setEnabled(false);
								miAzimuth.setEnabled(false);
								miBaseHeight.setEnabled(false);
								miPoleSpacing.setEnabled(false);
							}
						}
					}
					Util.selectSilently(cbmiDrawSunBeam, rack.isDrawSunBeamVisible());
					Util.selectSilently(miLabelNone, !rack.isLabelVisible());
					Util.selectSilently(miLabelCustom, rack.getLabelCustom());
					Util.selectSilently(miLabelId, rack.getLabelId());
					Util.selectSilently(miLabelCellEfficiency, rack.getLabelCellEfficiency());
					Util.selectSilently(miLabelTiltAngle, rack.getLabelTiltAngle());
					Util.selectSilently(miLabelTracker, rack.getLabelTracker());
					Util.selectSilently(miLabelEnergyOutput, rack.getLabelEnergyOutput());
					final boolean isCustom = "Custom".equals(rack.getSolarPanel().getModelName());
					miSolarPanelCellEfficiency.setEnabled(isCustom);
					miSolarPanelCellType.setEnabled(isCustom);
					miSolarPanelColor.setEnabled(isCustom);
					miSolarPanelSize.setEnabled(isCustom);
					miSolarPanelShadeTolerance.setEnabled(isCustom);
					miSolarPanelNoct.setEnabled(isCustom);
					miSolarPanelPmaxTc.setEnabled(isCustom);
				}

			});

			popupMenuForRack.add(miPaste);
			popupMenuForRack.add(miClear);
			popupMenuForRack.addSeparator();
			popupMenuForRack.add(miSolarPanels);
			popupMenuForRack.add(solarPanelMenu);
			popupMenuForRack.addSeparator();
			popupMenuForRack.add(miTiltAngle);
			popupMenuForRack.add(miAzimuth);
			popupMenuForRack.add(miRackSize);
			popupMenuForRack.add(miBaseHeight);
			popupMenuForRack.add(miPoleSpacing);
			popupMenuForRack.add(trackerMenu);
			popupMenuForRack.addSeparator();
			popupMenuForRack.add(cbmiDrawSunBeam);
			popupMenuForRack.add(labelMenu);
			popupMenuForRack.addSeparator();

			JMenuItem mi = new JMenuItem("Daily Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Rack) {
						new PvDailyAnalysis().show();
					}
				}
			});
			popupMenuForRack.add(mi);

			mi = new JMenuItem("Annual Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Rack) {
						new PvAnnualAnalysis().show();
					}
				}
			});
			popupMenuForRack.add(mi);

		}

		return popupMenuForRack;

	}

}
