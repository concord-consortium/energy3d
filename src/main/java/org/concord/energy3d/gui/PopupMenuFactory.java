package org.concord.energy3d.gui;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.concord.energy3d.gui.EnergyPanel.UpdateRadiation;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Thermalizable;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.undo.ChangeBackgroundAlbedoCommand;
import org.concord.energy3d.undo.ChangeBuildingSolarPanelEfficiencyCommand;
import org.concord.energy3d.undo.ChangeBuildingWindowShgcCommand;
import org.concord.energy3d.undo.ChangeGroundThermalDiffusivityCommand;
import org.concord.energy3d.undo.ChangePartUValueCommand;
import org.concord.energy3d.undo.ChangeVolumetricHeatCapacityCommand;
import org.concord.energy3d.undo.ChangeWallWindowShgcCommand;
import org.concord.energy3d.undo.ChangeRoofOverhangCommand;
import org.concord.energy3d.undo.ChangeSolarPanelEfficiencyCommand;
import org.concord.energy3d.undo.ChangeWindowShgcCommand;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.Util;

/**
 * Pop-up menus for customizing individual elements.
 * 
 * @author Charles Xie
 * 
 */

public class PopupMenuFactory {

	private static DecimalFormat twoDecimals = new DecimalFormat();

	static {
		twoDecimals.setMaximumFractionDigits(2);
	}

	private static JPopupMenu popupMenuForWindow;
	private static JPopupMenu popupMenuForWall;
	private static JPopupMenu popupMenuForRoof;
	private static JPopupMenu popupMenuForDoor;
	private static JPopupMenu popupMenuForTree;
	private static JPopupMenu popupMenuForHuman;
	private static JPopupMenu popupMenuForFoundation;
	private static JPopupMenu popupMenuForSolarPanel;
	private static JPopupMenu popupMenuForSensor;
	private static JPopupMenu popupMenuForLand;
	private static JPopupMenu popupMenuForSky;

	private static Action colorAction = new AbstractAction("Color...") {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			MainFrame.getInstance().showColorDialogForIndividualPart();
		}
	};

	private PopupMenuFactory() {
	}

	public static JPopupMenu getPopupMenu(boolean onLand) {
		HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart instanceof Window)
			return getPopupMenuForWindow();
		if (selectedPart instanceof Wall)
			return getPopupMenuForWall();
		if (selectedPart instanceof Roof)
			return getPopupMenuForRoof();
		if (selectedPart instanceof Door)
			return getPopupMenuForDoor();
		if (selectedPart instanceof Foundation)
			return getPopupMenuForFoundation();
		if (selectedPart instanceof SolarPanel)
			return getPopupMenuForSolarPanel();
		if (selectedPart instanceof Sensor)
			return getPopupMenuForSensor();
		if (selectedPart instanceof Tree)
			return getPopupMenuForTree();
		if (selectedPart instanceof Human)
			return getPopupMenuForHuman();
		return onLand ? getPopupMenuForLand() : getPopupMenuForSky();
	}

	private static JPopupMenu getPopupMenuForLand() {

		if (popupMenuForLand == null) {

			final JMenuItem miInfo = new JMenuItem("Land");
			miInfo.setEnabled(false);

			final JMenuItem miPaste = new JMenuItem("Paste");
			miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			miPaste.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Scene.getInstance().pasteToPickedLocationOnLand();
				}
			});

			final JMenuItem miAlbedo = new JMenuItem("Albedo...");
			miAlbedo.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String title = "<html>Background Albedo (dimensionless [0, 1])<hr><font size=2>Some reference values:<br>0.17 (soil), 0.25 (grass), 0.40 (sand), 0.55 (concrete), snow (0.9)</html>";
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), title, Scene.getInstance().getBackgroundAlbedo());
						if (newValue == null)
							break;
						else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val < 0 || val > 1) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Albedo value must be in 0-1.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									SceneManager.getInstance().getUndoManager().addEdit(new ChangeBackgroundAlbedoCommand());
									Scene.getInstance().setBackgroundAlbedo(val);
									Scene.getInstance().setEdited(true);
									break;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "" + newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});

			final JMenuItem miThermalDiffusivity = new JMenuItem("Ground Thermal Diffusivity...");
			miThermalDiffusivity.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String title = "<html>Ground Thermal Diffusivity (m<sup>2</sup>/s)<hr><font size=2>Some reference values:<br>0.039 (sand), 0.046 (clay), 0.05 (silt)</html>";
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), title, Scene.getInstance().getGroundThermalDiffusivity());
						if (newValue == null)
							break;
						else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val <= 0) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Ground thermal diffusivity must be positive.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									SceneManager.getInstance().getUndoManager().addEdit(new ChangeGroundThermalDiffusivityCommand());
									Scene.getInstance().setGroundThermalDiffusivity(val);
									Scene.getInstance().setEdited(true);
									break;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "" + newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});

			popupMenuForLand = new JPopupMenu();
			popupMenuForLand.setInvoker(MainPanel.getInstance().getCanvasPanel());
			popupMenuForLand.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
					miPaste.setEnabled(copyBuffer instanceof Tree || copyBuffer instanceof Human || copyBuffer instanceof Foundation);
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				}

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
				}

			});

			popupMenuForLand.add(miInfo);
			popupMenuForLand.addSeparator();
			popupMenuForLand.add(miPaste);
			popupMenuForLand.addSeparator();
			popupMenuForLand.add(miAlbedo);
			popupMenuForLand.add(miThermalDiffusivity);

		}

		return popupMenuForLand;

	}

	private static JPopupMenu getPopupMenuForSky() {

		if (popupMenuForSky == null) {

			final JMenuItem miInfo = new JMenuItem("Sky");
			miInfo.setEnabled(false);

			popupMenuForSky = new JPopupMenu();
			popupMenuForSky.setInvoker(MainPanel.getInstance().getCanvasPanel());
			popupMenuForSky.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				}

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
				}

			});

			popupMenuForSky.add(miInfo);

		}

		return popupMenuForSky;

	}

	private static JPopupMenu getPopupMenuForWindow() {

		if (popupMenuForWindow == null) {

			popupMenuForWindow = createPopupMenu(true, null);

			final JMenu muntinMenu = new JMenu("Muntins");

			ButtonGroup muntinButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem miMoreBars = new JRadioButtonMenuItem("More Bars");
			miMoreBars.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						((Window) selectedPart).setStyle(Window.MORE_MUNTIN_BARS);
						Scene.getInstance().redrawAll();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			muntinButtonGroup.add(miMoreBars);
			muntinMenu.add(miMoreBars);

			final JRadioButtonMenuItem miMediumBars = new JRadioButtonMenuItem("Medium Bars");
			miMediumBars.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						((Window) selectedPart).setStyle(Window.MEDIUM_MUNTIN_BARS);
						Scene.getInstance().redrawAll();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			muntinButtonGroup.add(miMediumBars);
			muntinMenu.add(miMediumBars);

			final JRadioButtonMenuItem miLessBars = new JRadioButtonMenuItem("Less Bars");
			miLessBars.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						((Window) selectedPart).setStyle(Window.LESS_MUNTIN_BARS);
						Scene.getInstance().redrawAll();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			muntinButtonGroup.add(miLessBars);
			muntinMenu.add(miLessBars);

			final JRadioButtonMenuItem miNoBar = new JRadioButtonMenuItem("No Bar");
			miNoBar.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						((Window) selectedPart).setStyle(Window.NO_MUNTIN_BAR);
						Scene.getInstance().redrawAll();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			muntinButtonGroup.add(miNoBar);
			muntinMenu.add(miNoBar);

			muntinMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(MenuEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						switch (((Window) selectedPart).getStyle()) {
						case Window.MORE_MUNTIN_BARS:
							miMoreBars.setSelected(true);
							break;
						case Window.MEDIUM_MUNTIN_BARS:
							miMediumBars.setSelected(true);
							break;
						case Window.LESS_MUNTIN_BARS:
							miLessBars.setSelected(true);
							break;
						case Window.NO_MUNTIN_BAR:
							miNoBar.setSelected(true);
							break;
						}
					}
				}

				@Override
				public void menuDeselected(MenuEvent e) {
				}

				@Override
				public void menuCanceled(MenuEvent e) {
				}

			});

			final JMenuItem miShgc = new JMenuItem("Solar Heat Gain Coefficient...");
			miShgc.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Window))
						return;
					final Window window = (Window) selectedPart;
					final String title = "<html>Solar Heat Gain Coefficient (%)</html>";
					final String footnote = "<html><hr><font size=2>Examples:<br><table><tr><td><font size=2>Single glass (clear)</td><td><font size=2>66%</td></tr><tr><td><font size=2>Single glass (green tint)</td><td><font size=2>55%</td></tr><tr><td><font size=2>Triple glass (air spaces)</td><td><font size=2>39%</td></tr></table><hr></html>";
					JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this window", true);
					final JRadioButton rb2 = new JRadioButton("All windows on this wall");
					final JRadioButton rb3 = new JRadioButton("All windows of this building");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					Object[] params = { title, footnote, panel };
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), params, window.getSolarHeatGainCoefficient());
						if (newValue == null)
							break;
						else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val < 10 || val > 90) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar heat gain coefficient must be between 10% and 90%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										SceneManager.getInstance().getUndoManager().addEdit(new ChangeWindowShgcCommand(window));
										window.setSolarHeatGainCoefficient(val);
									} else if (rb2.isSelected()) {
										Wall wall = (Wall) window.getContainer();
										SceneManager.getInstance().getUndoManager().addEdit(new ChangeWallWindowShgcCommand(wall));
										Scene.getInstance().setWindowShgcOnWall(wall, val);
									} else if (rb3.isSelected()) {
										Foundation foundation = window.getTopContainer();
										SceneManager.getInstance().getUndoManager().addEdit(new ChangeBuildingWindowShgcCommand(foundation));
										Scene.getInstance().setWindowShgcOfBuilding(foundation, val);
									}
									Scene.getInstance().setEdited(true);
									EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
									break;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "" + newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});

			popupMenuForWindow.addSeparator();
			popupMenuForWindow.add(miShgc);
			popupMenuForWindow.add(createUValueMenuItem(true));
			popupMenuForWindow.add(muntinMenu);

		}

		return popupMenuForWindow;

	}

	private static JPopupMenu getPopupMenuForWall() {

		if (popupMenuForWall == null) {

			final JMenuItem miPaste = new JMenuItem("Paste");
			miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			miPaste.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Scene.getInstance().pasteToPickedLocationOnWall();
				}
			});

			popupMenuForWall = createPopupMenu(false, new Runnable() {
				@Override
				public void run() {
					HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
					miPaste.setEnabled(copyBuffer instanceof Window || copyBuffer instanceof SolarPanel);
				}
			});

			popupMenuForWall.add(miPaste);
			popupMenuForWall.addSeparator();
			popupMenuForWall.add(colorAction);
			popupMenuForWall.add(createUValueMenuItem(false));
			popupMenuForWall.add(createVolumetricHeatCapacityMenuItem());

		}

		return popupMenuForWall;

	}

	private static JPopupMenu getPopupMenuForRoof() {

		if (popupMenuForRoof == null) {

			final JMenuItem miPaste = new JMenuItem("Paste");
			miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			miPaste.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Scene.getInstance().pasteToPickedLocationOnRoof();
				}
			});

			final JMenuItem miOverhang = new JMenuItem("Overhang Length...");
			miOverhang.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Roof))
						return;
					final Roof roof = (Roof) selectedPart;
					while (true) {
						SceneManager.getInstance().refresh(1);
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), "Overhang Length (m)", roof.getOverhangLength() * Scene.getInstance().getAnnotationScale());
						if (newValue == null)
							break;
						else {
							try {
								final double val = Double.parseDouble(newValue);
								double min = Roof.OVERHANG_MIN * Scene.getInstance().getAnnotationScale();
								if (val < min || val > 5) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Overhang value must be between " + min + " and 5.", "Error", JOptionPane.ERROR_MESSAGE);
								} else {
									SceneManager.getInstance().getUndoManager().addEdit(new ChangeRoofOverhangCommand(roof));
									roof.setOverhangLength(val / Scene.getInstance().getAnnotationScale());
									Scene.getInstance().redrawAll();
									Scene.getInstance().setEdited(true);
									EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
									break;
								}
							} catch (final NumberFormatException exception) {
								exception.printStackTrace();
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "" + newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});

			popupMenuForRoof = createPopupMenu(false, new Runnable() {
				@Override
				public void run() {
					HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
					miPaste.setEnabled(copyBuffer instanceof SolarPanel);
				}
			});

			popupMenuForRoof.add(miPaste);
			popupMenuForRoof.addSeparator();
			popupMenuForRoof.add(miOverhang);
			popupMenuForRoof.add(colorAction);
			popupMenuForRoof.add(createUValueMenuItem(false));
			popupMenuForRoof.add(createVolumetricHeatCapacityMenuItem());
		}

		return popupMenuForRoof;

	}

	private static JPopupMenu getPopupMenuForDoor() {

		if (popupMenuForDoor == null) {
			popupMenuForDoor = createPopupMenu(false, null);
			popupMenuForDoor.addSeparator();
			popupMenuForDoor.add(colorAction);
			popupMenuForDoor.add(createUValueMenuItem(true));
			popupMenuForDoor.add(createVolumetricHeatCapacityMenuItem());
		}

		return popupMenuForDoor;

	}

	private static JPopupMenu getPopupMenuForFoundation() {

		if (popupMenuForFoundation == null) {
			popupMenuForFoundation = createPopupMenu(false, null);
			final JMenuItem miCopyBuilding = new JMenuItem("Copy Building");
			miCopyBuilding.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						Scene.getInstance().setCopyBuffer(selectedPart.copy(false));
					}
				}
			});
			popupMenuForFoundation.add(miCopyBuilding);
			popupMenuForFoundation.addSeparator();
			popupMenuForFoundation.add(colorAction);
			// floor insulation only for the first floor, so this U-value is associated with the Foundation class, not the Floor class
			popupMenuForFoundation.add(createUValueMenuItem(false));
			popupMenuForFoundation.add(createVolumetricHeatCapacityMenuItem());
		}

		return popupMenuForFoundation;

	}

	private static JPopupMenu getPopupMenuForSensor() {
		if (popupMenuForSensor == null) {
			popupMenuForSensor = createPopupMenu(false, null);
		}
		return popupMenuForSensor;
	}

	private static JPopupMenu getPopupMenuForSolarPanel() {

		if (popupMenuForSolarPanel == null) {

			popupMenuForSolarPanel = createPopupMenu(true, null);

			final JMenuItem miEff = new JMenuItem("Energy Conversion Efficiency...");
			miEff.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel))
						return;
					final SolarPanel solarPanel = (SolarPanel) selectedPart;
					final String title = "<html>Energy Conversion Efficiency (%)</html>";
					final String footnote = "<html><hr><font size=2>How efficient can a solar panel be?<br>The Shockley–Queisser limit is 34%, but the theoretical limit for multilayer cells is 86%.<hr></html>";
					JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this solar panel", true);
					final JRadioButton rb2 = new JRadioButton("All solar panels of this building");
					panel.add(rb1);
					panel.add(rb2);
					ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					Object[] params = { title, footnote, panel };
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), params, solarPanel.getEfficiency());
						if (newValue == null)
							break;
						else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val < 10 || val > 86) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Conversion efficiency must be between 10% and 86%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										SceneManager.getInstance().getUndoManager().addEdit(new ChangeSolarPanelEfficiencyCommand(solarPanel));
										solarPanel.setEfficiency(val);
									} else if (rb2.isSelected()) {
										Foundation foundation = solarPanel.getTopContainer();
										SceneManager.getInstance().getUndoManager().addEdit(new ChangeBuildingSolarPanelEfficiencyCommand(foundation));
										Scene.getInstance().setSolarPanelEfficiencyOfBuilding(foundation, val);
									}
									Scene.getInstance().setEdited(true);
									EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
									break;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "" + newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});

			popupMenuForSolarPanel.addSeparator();
			popupMenuForSolarPanel.add(miEff);

		}

		return popupMenuForSolarPanel;

	}

	private static JPopupMenu getPopupMenuForTree() {

		if (popupMenuForTree == null) {

			final JCheckBoxMenuItem miPolygon = new JCheckBoxMenuItem("Show Polygon");
			miPolygon.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					HousePart p = SceneManager.getInstance().getSelectedPart();
					if (p instanceof Tree)
						((Tree) p).setShowPolygons(miPolygon.isSelected());
				}
			});

			popupMenuForTree = createPopupMenu(true, new Runnable() {
				@Override
				public void run() {
					HousePart p = SceneManager.getInstance().getSelectedPart();
					if (p instanceof Tree)
						Util.selectSilently(miPolygon, ((Tree) p).getShowPolygons());
				}
			});

			popupMenuForTree.add(miPolygon);

		}

		return popupMenuForTree;

	}

	private static JPopupMenu getPopupMenuForHuman() {

		if (popupMenuForHuman == null) {
			popupMenuForHuman = createPopupMenu(true, null);
		}
		return popupMenuForHuman;

	}

	private static JMenuItem createUValueMenuItem(final boolean useUValue) {
		final JMenuItem mi = new JMenuItem("U-Value...");
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (!(selectedPart instanceof Thermalizable))
					return;
				final Thermalizable t = (Thermalizable) selectedPart;

				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				JLabel label = new JLabel("<html>Insulation Property<hr><font size=2>Examples:<br></html>");
				label.setAlignmentX(Component.LEFT_ALIGNMENT);
				panel.add(label);
				panel.add(Box.createVerticalStrut(15));
				JPanel unitPanel = new JPanel(new GridLayout(2, 3, 5, 5));
				unitPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
				panel.add(unitPanel);

				unitPanel.add(new JLabel("U-Value in SI Unit:"));
				final JTextField siField = new JTextField("" + t.getUValue(), 10);
				unitPanel.add(siField);
				unitPanel.add(new JLabel("<html>W/(m<sup>2</sup>&middot;&deg;C)</html>"));

				if (useUValue) {

					unitPanel.add(new JLabel("U-Value in US Unit:"));
					final JTextField uValueField = new JTextField(twoDecimals.format(Util.toUsUValue(t.getUValue())), 10);
					uValueField.setAlignmentX(Component.LEFT_ALIGNMENT);
					unitPanel.add(uValueField);
					unitPanel.add(new JLabel("<html>Btu/(h&middot;ft<sup>2</sup>&middot;&deg;F)</html>"));

					siField.getDocument().addDocumentListener(new DocumentListener() {

						private void update() {
							if (!siField.hasFocus())
								return;
							String newValue = siField.getText();
							if ("".equals(newValue))
								return;
							try {
								uValueField.setText(twoDecimals.format(Util.toUsUValue(Double.parseDouble(newValue))));
							} catch (Exception exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "" + newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							update();
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							update();
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
							update();
						}
					});
					uValueField.getDocument().addDocumentListener(new DocumentListener() {

						private void update() {
							if (!uValueField.hasFocus())
								return;
							String newValue = uValueField.getText();
							if ("".equals(newValue))
								return;
							try {
								siField.setText(twoDecimals.format(1.0 / (Util.toSiRValue(1.0 / Double.parseDouble(newValue)))));
							} catch (Exception exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "" + newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							update();
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							update();
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
							update();
						}
					});

				} else {

					unitPanel.add(new JLabel("R-Value in US Unit:"));
					final JTextField rValueField = new JTextField(twoDecimals.format(Util.toUsRValue(t.getUValue())), 10);
					rValueField.setAlignmentX(Component.LEFT_ALIGNMENT);
					unitPanel.add(rValueField);
					unitPanel.add(new JLabel("<html>h&middot;ft<sup>2</sup>&middot;&deg;F/Btu</html>"));

					siField.getDocument().addDocumentListener(new DocumentListener() {

						private void update() {
							if (!siField.hasFocus())
								return;
							String newValue = siField.getText();
							if ("".equals(newValue))
								return;
							try {
								rValueField.setText(twoDecimals.format(Util.toUsRValue(Double.parseDouble(newValue))));
							} catch (Exception exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "" + newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							update();
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							update();
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
							update();
						}
					});
					rValueField.getDocument().addDocumentListener(new DocumentListener() {

						private void update() {
							if (!rValueField.hasFocus())
								return;
							String newValue = rValueField.getText();
							if ("".equals(newValue))
								return;
							try {
								siField.setText(twoDecimals.format(1.0 / Util.toSiRValue(Double.parseDouble(newValue))));
							} catch (Exception exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "" + newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}

						@Override
						public void removeUpdate(DocumentEvent e) {
							update();
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							update();
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
							update();
						}
					});

				}

				while (true) {
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), panel, "Input", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
						String newValue = siField.getText();
						try {
							final double val = Double.parseDouble(newValue);
							if (val <= 0) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "U-value must be positive.", "Range Error", JOptionPane.ERROR_MESSAGE);
							} else {
								SceneManager.getInstance().getUndoManager().addEdit(new ChangePartUValueCommand(selectedPart));
								t.setUValue(val);
								Scene.getInstance().setEdited(true);
								EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
								break;
							}
						} catch (final NumberFormatException exception) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "" + newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
						}
					} else {
						break;
					}
				}

			}
		});
		return mi;
	}

	private static JMenuItem createVolumetricHeatCapacityMenuItem() {
		final JMenuItem mi = new JMenuItem("Volumeric Heat Capacity...");
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (!(selectedPart instanceof Thermalizable))
					return;
				final Thermalizable t = (Thermalizable) selectedPart;
				final String title = "<html>Volumeric Heat Capacity [kWh/(m<sup>3</sup>&middot;&deg;C)]<hr><font size=2>Some reference values:<br>0.03 (fiberglass), 0.18 (asphalt), 0.25(oak wood), 0.33 (concrete), 0.37 (brick), 0.58 (stone)</html>";
				while (true) {
					final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), title, t.getVolumetricHeatCapacity());
					if (newValue == null)
						break;
					else {
						try {
							final double val = Double.parseDouble(newValue);
							if (val <= 0) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Volumeric heat capacity must be positive.", "Range Error", JOptionPane.ERROR_MESSAGE);
							} else {
								SceneManager.getInstance().getUndoManager().addEdit(new ChangeVolumetricHeatCapacityCommand(selectedPart));
								t.setVolumetricHeatCapacity(val);
								Scene.getInstance().setEdited(true);
								EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
								break;
							}
						} catch (final NumberFormatException exception) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "" + newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		});
		return mi;
	}

	private static JPopupMenu createPopupMenu(boolean hasCopyMenu, final Runnable runWhenBecomingVisible) {

		final JMenuItem miInfo = new JMenuItem();
		miInfo.setEnabled(false);

		final JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.setInvoker(MainPanel.getInstance().getCanvasPanel());
		popupMenu.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (selectedPart == null)
					return;
				String s = selectedPart.toString();
				miInfo.setText(s.substring(0, s.indexOf(')') + 1));
				if (runWhenBecomingVisible != null)
					runWhenBecomingVisible.run();
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

		});

		final JMenuItem miCut = new JMenuItem("Cut");
		miCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
		miCut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (selectedPart != null) {
					Scene.getInstance().setCopyBuffer(selectedPart);
					SceneManager.getInstance().deleteCurrentHousePart();
				}
			}
		});

		popupMenu.add(miInfo);
		popupMenu.addSeparator();
		popupMenu.add(miCut);

		if (hasCopyMenu) {
			final JMenuItem miCopy = new JMenuItem("Copy");
			miCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			miCopy.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart != null) {
						Scene.getInstance().setCopyBuffer(selectedPart.copy(false));
					}
				}
			});
			popupMenu.add(miCopy);
		}

		return popupMenu;

	}

}
