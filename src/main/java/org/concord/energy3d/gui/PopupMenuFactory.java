package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.concurrent.Callable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
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

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Thermalizable;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.Cost;
import org.concord.energy3d.simulation.EnergyAnnualAnalysis;
import org.concord.energy3d.simulation.EnergyDailyAnalysis;
import org.concord.energy3d.simulation.MirrorAnnualAnalysis;
import org.concord.energy3d.simulation.MirrorDailyAnalysis;
import org.concord.energy3d.simulation.PvAnnualAnalysis;
import org.concord.energy3d.simulation.PvDailyAnalysis;
import org.concord.energy3d.simulation.UtilityBill;
import org.concord.energy3d.undo.ChangeAzimuthCommand;
import org.concord.energy3d.undo.ChangeAzimuthForAllMirrorsCommand;
import org.concord.energy3d.undo.ChangeAzimuthForAllSolarPanelsCommand;
import org.concord.energy3d.undo.ChangeBackgroundAlbedoCommand;
import org.concord.energy3d.undo.ChangeBaseHeightCommand;
import org.concord.energy3d.undo.ChangeBaseHeightForAllMirrorsCommand;
import org.concord.energy3d.undo.ChangeBaseHeightForAllSolarPanelsCommand;
import org.concord.energy3d.undo.ChangeBuildingColorCommand;
import org.concord.energy3d.undo.ChangeBuildingShutterColorCommand;
import org.concord.energy3d.undo.ChangeBuildingUValueCommand;
import org.concord.energy3d.undo.ChangeBuildingWindowShgcCommand;
import org.concord.energy3d.undo.ChangeContainerShutterColorCommand;
import org.concord.energy3d.undo.ChangeContainerWindowColorCommand;
import org.concord.energy3d.undo.ChangeContainerWindowShgcCommand;
import org.concord.energy3d.undo.ChangeFoundationHeightCommand;
import org.concord.energy3d.undo.ChangeFoundationMicroInverterEfficiencyCommand;
import org.concord.energy3d.undo.ChangeFoundationMirrorAzimuthCommand;
import org.concord.energy3d.undo.ChangeFoundationMirrorBaseHeightCommand;
import org.concord.energy3d.undo.ChangeFoundationMirrorReflectivityCommand;
import org.concord.energy3d.undo.ChangeFoundationMirrorTargetCommand;
import org.concord.energy3d.undo.ChangeFoundationMirrorTiltAngleCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarCellEfficiencyCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarPanelAzimuthCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarPanelBaseHeightCommand;
import org.concord.energy3d.undo.ChangeFoundationSolarPanelTiltAngleCommand;
import org.concord.energy3d.undo.ChangeGroundThermalDiffusivityCommand;
import org.concord.energy3d.undo.ChangeMicroInverterEfficiencyCommand;
import org.concord.energy3d.undo.ChangeMicroInverterEfficiencyForAllCommand;
import org.concord.energy3d.undo.ChangeMirrorReflectivityCommand;
import org.concord.energy3d.undo.ChangeMirrorTargetCommand;
import org.concord.energy3d.undo.ChangePartColorCommand;
import org.concord.energy3d.undo.ChangePartUValueCommand;
import org.concord.energy3d.undo.ChangeReflectivityForAllMirrorsCommand;
import org.concord.energy3d.undo.ChangeRoofOverhangCommand;
import org.concord.energy3d.undo.ChangeShutterColorCommand;
import org.concord.energy3d.undo.ChangeShutterLengthCommand;
import org.concord.energy3d.undo.ChangeSolarCellEfficiencyCommand;
import org.concord.energy3d.undo.ChangeSolarCellEfficiencyForAllCommand;
import org.concord.energy3d.undo.ChangeTargetForAllMirrorsCommand;
import org.concord.energy3d.undo.ChangeThemeCommand;
import org.concord.energy3d.undo.ChangeTiltAngleCommand;
import org.concord.energy3d.undo.ChangeTiltAngleForAllMirrorsCommand;
import org.concord.energy3d.undo.ChangeTiltAngleForAllSolarPanelsCommand;
import org.concord.energy3d.undo.ChangeVolumetricHeatCapacityCommand;
import org.concord.energy3d.undo.ChangeWallTypeCommand;
import org.concord.energy3d.undo.ChangeWindowShgcCommand;
import org.concord.energy3d.undo.ChangeWindowShuttersCommand;
import org.concord.energy3d.undo.ChooseSolarPanelSizeCommand;
import org.concord.energy3d.undo.DeleteUtilityBillCommand;
import org.concord.energy3d.undo.LockPartCommand;
import org.concord.energy3d.undo.RotateSolarPanelCommand;
import org.concord.energy3d.undo.SetFoundationShadeToleranceCommand;
import org.concord.energy3d.undo.SetFoundationSolarTrackerCommand;
import org.concord.energy3d.undo.SetMirrorSizeCommand;
import org.concord.energy3d.undo.SetShadeToleranceCommand;
import org.concord.energy3d.undo.SetShadeToleranceForAllSolarPanelsCommand;
import org.concord.energy3d.undo.SetSolarTrackerCommand;
import org.concord.energy3d.undo.SetTrackerForAllSolarPanelsCommand;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.Util;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;

/**
 * Pop-up menus for customizing individual elements.
 *
 * @author Charles Xie
 *
 */

public class PopupMenuFactory {

	private static DecimalFormat integerFormat = new DecimalFormat();
	private static DecimalFormat twoDecimalsFormat = new DecimalFormat();

	static {
		integerFormat.setMaximumFractionDigits(0);
		twoDecimalsFormat.setMaximumFractionDigits(3);
	}

	private static JPopupMenu popupMenuForWindow;
	private static JPopupMenu popupMenuForWall;
	private static JPopupMenu popupMenuForRoof;
	private static JPopupMenu popupMenuForDoor;
	private static JPopupMenu popupMenuForFloor;
	private static JPopupMenu popupMenuForTree;
	private static JPopupMenu popupMenuForHuman;
	private static JPopupMenu popupMenuForFoundation;
	private static JPopupMenu popupMenuForSolarPanel;
	private static JPopupMenu popupMenuForMirror;
	private static JPopupMenu popupMenuForSensor;
	private static JPopupMenu popupMenuForLand;
	private static JPopupMenu popupMenuForSky;

	// cached values
	private static double pvArrayRowSpacing = 1;
	private static double pvArrayColSpacing = 0.5;
	private static int pvArrayRowAxis = 0;
	private static double solarPanelWidth = 0.99;
	private static double solarPanelHeight = 1.96;
	private static double mirrorWidth = 2;
	private static double mirrorHeight = 3;
	private static double mirrorArrayRadialSpacing = 1;
	private static double mirrorArrayAzimuthalSpacing = 1;
	private static int mirrorArrayLayout = 0;

	private static Action colorAction = new AbstractAction("Color...") {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			MainFrame.getInstance().showColorDialogForParts();
		}
	};

	private PopupMenuFactory() {
	}

	public static JPopupMenu getPopupMenu(final boolean onLand) {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart instanceof Window) {
			return getPopupMenuForWindow();
		}
		if (selectedPart instanceof Wall) {
			return getPopupMenuForWall();
		}
		if (selectedPart instanceof Roof) {
			return getPopupMenuForRoof();
		}
		if (selectedPart instanceof Door) {
			return getPopupMenuForDoor();
		}
		if (selectedPart instanceof Floor) {
			return getPopupMenuForFloor();
		}
		if (selectedPart instanceof Foundation) {
			return getPopupMenuForFoundation();
		}
		if (selectedPart instanceof SolarPanel) {
			return getPopupMenuForSolarPanel();
		}
		if (selectedPart instanceof Mirror) {
			return getPopupMenuForMirror();
		}
		if (selectedPart instanceof Sensor) {
			return getPopupMenuForSensor();
		}
		if (selectedPart instanceof Tree) {
			return getPopupMenuForTree();
		}
		if (selectedPart instanceof Human) {
			return getPopupMenuForHuman();
		}
		return onLand ? getPopupMenuForLand() : getPopupMenuForSky();
	}

	private static void addPrefabMenuItem(final String type, final String url, final JMenu menu) {
		final JMenuItem mi = new JMenuItem(type);
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				SceneManager.getTaskManager().update(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						try {
							Scene.importFile(MainApplication.class.getResource(url));
						} catch (final Throwable err) {
							Util.reportError(err);
						}
						return null;
					}
				});
			}
		});
		menu.add(mi);
	}

	private static JPopupMenu getPopupMenuForLand() {

		if (popupMenuForLand == null) {

			final JMenuItem miInfo = new JMenuItem("Land");
			miInfo.setEnabled(false);

			final JMenuItem miPaste = new JMenuItem("Paste");
			miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			miPaste.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().pasteToPickedLocationOnLand();
							return null;
						}
					});
				}
			});

			final JMenuItem miRemoveAllTrees = new JMenuItem("Remove All Trees");
			miRemoveAllTrees.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().removeAllTrees();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});

			final JMenuItem miRemoveAllHumans = new JMenuItem("Remove All Humans");
			miRemoveAllHumans.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().removeAllHumans();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});

			final JMenuItem miRemoveAllBuildings = new JMenuItem("Remove All Buildings");
			miRemoveAllBuildings.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().removeAllFoundations();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});

			final JMenuItem miImport = new JMenuItem("Import...");
			miImport.setToolTipText("Import the content in an existing file into the clicked location on the land as the center");
			miImport.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					MainFrame.getInstance().importFile();
					Scene.getInstance().setEdited(true);
				}
			});

			final JMenu miImportPrefabMenu = new JMenu("Import a Prefab");
			addPrefabMenuItem("Back Hip Roof Porch", "prefabs/back-hip-roof-porch.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Balcony", "prefabs/balcony1.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Bell Tower", "prefabs/bell-tower.ng3", miImportPrefabMenu);
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
			addPrefabMenuItem("Shed Dormer", "prefabs/shed-dormer.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Solarium", "prefabs/solarium1.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Square Tower", "prefabs/square-tower.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Stair", "prefabs/stair1.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Tall Front Door Overhang", "prefabs/tall-front-door-overhang.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Temple Front", "prefabs/temple-front.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Waterfront Deck", "prefabs/waterfront-deck.ng3", miImportPrefabMenu);

			final JMenuItem miAlbedo = new JMenuItem("Albedo...");
			miAlbedo.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
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
									final ChangeBackgroundAlbedoCommand c = new ChangeBackgroundAlbedoCommand();
									Scene.getInstance().getGround().setAlbedo(val);
									updateAfterEdit();
									SceneManager.getInstance().getUndoManager().addEdit(c);
									break;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});

			final JMenuItem miThermalDiffusivity = new JMenuItem("Ground Thermal Diffusivity...");
			miThermalDiffusivity.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
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
									final ChangeGroundThermalDiffusivityCommand c = new ChangeGroundThermalDiffusivityCommand();
									Scene.getInstance().getGround().setThermalDiffusivity(val);
									updateAfterEdit();
									SceneManager.getInstance().getUndoManager().addEdit(c);
									break;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});

			final JMenu geoLocationMenu = new JMenu("Geo-Location");

			final JMenuItem miSetLocation = new JMenuItem("Set Location");
			miSetLocation.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					MapDialog.showDialog();
				}
			});
			geoLocationMenu.add(miSetLocation);

			final JMenuItem miClearLocation = new JMenuItem("Clear Location");
			miClearLocation.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().setMap(null, 1);
					Scene.getInstance().setEdited(true);
				}
			});
			geoLocationMenu.add(miClearLocation);

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
			popupMenuForLand.addSeparator();
			popupMenuForLand.add(miPaste);
			popupMenuForLand.add(miRemoveAllTrees);
			popupMenuForLand.add(miRemoveAllHumans);
			popupMenuForLand.add(miRemoveAllBuildings);
			popupMenuForLand.addSeparator();
			popupMenuForLand.add(miImport);
			popupMenuForLand.add(miImportPrefabMenu);
			popupMenuForLand.addSeparator();
			popupMenuForLand.add(geoLocationMenu);
			popupMenuForLand.add(colorAction);
			popupMenuForLand.add(miAlbedo);
			popupMenuForLand.add(miThermalDiffusivity);

		}

		return popupMenuForLand;

	}

	private static JPopupMenu getPopupMenuForSky() {

		if (popupMenuForSky == null) {

			final JMenuItem miInfo = new JMenuItem("Sky");
			miInfo.setEnabled(false);

			final JCheckBoxMenuItem miHeliodon = new JCheckBoxMenuItem("Heliodon");
			miHeliodon.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					MainPanel.getInstance().getHeliodonButton().doClick();
				}
			});

			final JMenu themeMenu = new JMenu("Theme");
			final ButtonGroup themeButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem miCloudySky = new JRadioButtonMenuItem("Cloudy Sky");
			miCloudySky.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeThemeCommand c = new ChangeThemeCommand();
					Scene.getInstance().setTheme(Scene.CLOUDY_SKY_THEME);
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			themeButtonGroup.add(miCloudySky);
			themeMenu.add(miCloudySky);

			final JRadioButtonMenuItem miDesert = new JRadioButtonMenuItem("Desert");
			miDesert.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeThemeCommand c = new ChangeThemeCommand();
					Scene.getInstance().setTheme(Scene.DESERT_THEME);
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			themeButtonGroup.add(miDesert);
			themeMenu.add(miDesert);

			final JRadioButtonMenuItem miGrassland = new JRadioButtonMenuItem("Grassland");
			miGrassland.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeThemeCommand c = new ChangeThemeCommand();
					Scene.getInstance().setTheme(Scene.GRASSLAND_THEME);
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			themeButtonGroup.add(miGrassland);
			themeMenu.add(miGrassland);

			final JRadioButtonMenuItem miForest = new JRadioButtonMenuItem("Forest");
			miForest.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeThemeCommand c = new ChangeThemeCommand();
					Scene.getInstance().setTheme(Scene.FOREST_THEME);
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			themeButtonGroup.add(miForest);
			themeMenu.add(miForest);

			themeMenu.addMenuListener(new MenuListener() {
				@Override
				public void menuCanceled(final MenuEvent e) {
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					SceneManager.getInstance().refresh();
				}

				@Override
				public void menuSelected(final MenuEvent e) {
					Util.selectSilently(miCloudySky, Scene.getInstance().getTheme() == Scene.CLOUDY_SKY_THEME);
					Util.selectSilently(miDesert, Scene.getInstance().getTheme() == Scene.DESERT_THEME);
					Util.selectSilently(miGrassland, Scene.getInstance().getTheme() == Scene.GRASSLAND_THEME);
					Util.selectSilently(miForest, Scene.getInstance().getTheme() == Scene.FOREST_THEME);
				}
			});

			popupMenuForSky = new JPopupMenu();
			popupMenuForSky.setInvoker(MainPanel.getInstance().getCanvasPanel());
			popupMenuForSky.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
					Util.selectSilently(miHeliodon, MainPanel.getInstance().getHeliodonButton().isSelected());
				}

				@Override
				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				}

				@Override
				public void popupMenuCanceled(final PopupMenuEvent e) {
				}

			});

			popupMenuForSky.add(miInfo);
			popupMenuForSky.addSeparator();
			popupMenuForSky.add(miHeliodon);
			popupMenuForSky.add(themeMenu);

		}

		return popupMenuForSky;

	}

	private static JPopupMenu getPopupMenuForFloor() {

		if (popupMenuForFloor == null) {

			final JMenuItem miInfo = new JMenuItem("Floor");
			miInfo.setEnabled(false);

			popupMenuForFloor = new JPopupMenu();
			popupMenuForFloor.setInvoker(MainPanel.getInstance().getCanvasPanel());
			popupMenuForFloor.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null) {
						return;
					}
					final String s = selectedPart.toString();
					miInfo.setText(s.substring(0, s.indexOf(')') + 1) + " ($" + Cost.getInstance().getPartCost(selectedPart) + ")");
				}

				@Override
				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				}

				@Override
				public void popupMenuCanceled(final PopupMenuEvent e) {
				}

			});

			popupMenuForFloor.add(miInfo);
			popupMenuForFloor.addSeparator();
			popupMenuForFloor.add(colorAction);

		}

		return popupMenuForFloor;

	}

	private static JPopupMenu getPopupMenuForWindow() {

		if (popupMenuForWindow == null) {

			final JMenu shutterMenu = new JMenu("Shutters");

			popupMenuForWindow = createPopupMenu(true, true, new Runnable() {
				@Override
				public void run() {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						shutterMenu.setEnabled(selectedPart.getContainer() instanceof Wall);
					}
				}
			});

			final JMenu muntinMenu = new JMenu("Muntins");

			final JCheckBoxMenuItem cbmiHorizontalBars = new JCheckBoxMenuItem("Horizontal Bars");
			cbmiHorizontalBars.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window w = (Window) selectedPart;
						w.setHorizontalBars(cbmiHorizontalBars.isSelected());
						w.draw();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			muntinMenu.add(cbmiHorizontalBars);

			final JCheckBoxMenuItem cbmiVerticalBars = new JCheckBoxMenuItem("Vertical Bars");
			cbmiVerticalBars.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window w = (Window) selectedPart;
						w.setVerticalBars(cbmiVerticalBars.isSelected());
						w.draw();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			muntinMenu.add(cbmiVerticalBars);
			muntinMenu.addSeparator();

			final ButtonGroup muntinButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem miMoreBars = new JRadioButtonMenuItem("More Bars");
			miMoreBars.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window w = (Window) selectedPart;
						w.setStyle(Window.MORE_MUNTIN_BARS);
						w.draw();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			muntinButtonGroup.add(miMoreBars);
			muntinMenu.add(miMoreBars);

			final JRadioButtonMenuItem miMediumBars = new JRadioButtonMenuItem("Medium Bars");
			miMediumBars.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window w = (Window) selectedPart;
						w.setStyle(Window.MEDIUM_MUNTIN_BARS);
						w.draw();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			muntinButtonGroup.add(miMediumBars);
			muntinMenu.add(miMediumBars);

			final JRadioButtonMenuItem miLessBars = new JRadioButtonMenuItem("Less Bars");
			miLessBars.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window w = (Window) selectedPart;
						w.setStyle(Window.LESS_MUNTIN_BARS);
						w.draw();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			muntinButtonGroup.add(miLessBars);
			muntinMenu.add(miLessBars);

			muntinMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(final MenuEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window window = (Window) selectedPart;
						switch (window.getStyle()) {
						case Window.MORE_MUNTIN_BARS:
							Util.selectSilently(miMoreBars, true);
							break;
						case Window.MEDIUM_MUNTIN_BARS:
							Util.selectSilently(miMediumBars, true);
							break;
						case Window.LESS_MUNTIN_BARS:
							Util.selectSilently(miLessBars, true);
							break;
						}
						// NO_MUNTIN_BAR backward compatibility
						Util.selectSilently(cbmiHorizontalBars, window.getStyle() != Window.NO_MUNTIN_BAR && window.getHorizontalBars());
						Util.selectSilently(cbmiVerticalBars, window.getStyle() != Window.NO_MUNTIN_BAR && window.getVerticalBars());
					}
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					muntinMenu.setEnabled(true);
				}

				@Override
				public void menuCanceled(final MenuEvent e) {
					muntinMenu.setEnabled(true);
				}

			});

			final JCheckBoxMenuItem cbmiBothShutters = new JCheckBoxMenuItem("Both Shutters");
			final JCheckBoxMenuItem cbmiLeftShutter = new JCheckBoxMenuItem("Left Shutter");
			final JCheckBoxMenuItem cbmiRightShutter = new JCheckBoxMenuItem("Right Shutter");

			cbmiLeftShutter.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window w = (Window) selectedPart;
						final ChangeWindowShuttersCommand c = new ChangeWindowShuttersCommand(w);
						w.setLeftShutter(cbmiLeftShutter.isSelected());
						w.draw();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			shutterMenu.add(cbmiLeftShutter);

			cbmiRightShutter.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window w = (Window) selectedPart;
						final ChangeWindowShuttersCommand c = new ChangeWindowShuttersCommand(w);
						w.setRightShutter(cbmiRightShutter.isSelected());
						w.draw();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			shutterMenu.add(cbmiRightShutter);

			cbmiBothShutters.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window w = (Window) selectedPart;
						final ChangeWindowShuttersCommand c = new ChangeWindowShuttersCommand(w);
						w.setLeftShutter(cbmiBothShutters.isSelected());
						w.setRightShutter(cbmiBothShutters.isSelected());
						w.draw();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			shutterMenu.add(cbmiBothShutters);
			shutterMenu.addSeparator();

			final JMenuItem miShutterColor = new JMenuItem("Color...");
			miShutterColor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Window)) {
						return;
					}
					final Window window = (Window) selectedPart;
					final JColorChooser colorChooser = MainFrame.getInstance().getColorChooser();
					final ReadOnlyColorRGBA color = window.getShutterColor();
					if (color != null) {
						colorChooser.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
					}
					final ActionListener actionListener = new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							final Color c = colorChooser.getColor();
							final float[] newColor = c.getComponents(null);
							final ColorRGBA color = new ColorRGBA(newColor[0], newColor[1], newColor[2], 1);
							final JPanel panel = new JPanel();
							panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
							panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
							final JRadioButton rb1 = new JRadioButton("Only this Window", true);
							final JRadioButton rb2 = new JRadioButton("All Windows on this " + (window.getContainer() instanceof Wall ? "Wall" : "Roof"));
							final JRadioButton rb3 = new JRadioButton("All Windows of this Building");
							panel.add(rb1);
							panel.add(rb2);
							panel.add(rb3);
							final ButtonGroup bg = new ButtonGroup();
							bg.add(rb1);
							bg.add(rb2);
							bg.add(rb3);
							if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), panel, "Scope", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
								return;
							}
							if (rb1.isSelected()) { // apply to only this window
								final ChangeShutterColorCommand cmd = new ChangeShutterColorCommand(window);
								window.setShutterColor(color);
								window.draw();
								SceneManager.getInstance().getUndoManager().addEdit(cmd);
							} else if (rb2.isSelected()) {
								final ChangeContainerShutterColorCommand cmd = new ChangeContainerShutterColorCommand(window.getContainer());
								Scene.getInstance().setWindowColorInContainer(window.getContainer(), color, true);
								SceneManager.getInstance().getUndoManager().addEdit(cmd);
							} else {
								final ChangeBuildingShutterColorCommand cmd = new ChangeBuildingShutterColorCommand(window);
								Scene.getInstance().setShutterColorOfBuilding(window, color);
								SceneManager.getInstance().getUndoManager().addEdit(cmd);
							}
							Scene.getInstance().setEdited(true);
						}
					};
					JColorChooser.createDialog(MainFrame.getInstance(), "Select Shutter Color", true, colorChooser, actionListener, null).setVisible(true);
				}
			});
			shutterMenu.add(miShutterColor);

			final JMenuItem miShutterLength = new JMenuItem("Relative Length...");
			miShutterLength.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Window)) {
						return;
					}
					final Window window = (Window) selectedPart;
					final String partInfo = window.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final String title = "<html>Relative Length of Shutter for " + partInfo + "</html>";
					final String footnote = "<html><hr width=400></html>";
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Window Shutter", true);
					final JRadioButton rb2 = new JRadioButton("All Window Shutters on this Wall");
					final JRadioButton rb3 = new JRadioButton("All Window Shutters of this Building");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					final Object[] params = { title, footnote, panel };
					while (true) {
						final String newValue = (String) JOptionPane.showInputDialog(MainFrame.getInstance(), params, "Input: " + partInfo, JOptionPane.QUESTION_MESSAGE, null, null, window.getShutterLength());
						if (newValue == null) {
							break;
						} else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val <= 0 || val > 1) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Relative shutter length must be within (0, 1).", "Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final ChangeShutterLengthCommand c = new ChangeShutterLengthCommand(window);
										window.setShutterLength(val);
										window.draw();
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb2.isSelected()) {
										Scene.getInstance().setShutterLengthInContainer(window.getContainer(), val);
									} else if (rb3.isSelected()) {
										Scene.getInstance().setShutterLengthOfBuilding(window, val);
									}
									Scene.getInstance().setEdited(true);
									break;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});
			shutterMenu.add(miShutterLength);

			shutterMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(final MenuEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window window = (Window) selectedPart;
						Util.selectSilently(cbmiLeftShutter, window.getLeftShutter());
						Util.selectSilently(cbmiRightShutter, window.getRightShutter());
						Util.selectSilently(cbmiBothShutters, window.getLeftShutter() && window.getRightShutter());
					}
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					shutterMenu.setEnabled(true);
				}

				@Override
				public void menuCanceled(final MenuEvent e) {
					shutterMenu.setEnabled(true);
				}

			});

			final JMenuItem miShgc = new JMenuItem("Solar Heat Gain Coefficient...");
			miShgc.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Window)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Window window = (Window) selectedPart;
					final String title = "<html>Solar Heat Gain Coefficient of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>Examples:<br><table><tr><td><font size=2>Single glass (clear)</td><td><font size=2>0.66</td></tr><tr><td><font size=2>Single glass (green tint)</td><td><font size=2>0.55</td></tr><tr><td><font size=2>Triple glass (air spaces)</td><td><font size=2>0.39</td></tr></table><hr></html>";
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Window", true);
					final JRadioButton rb2 = new JRadioButton("All Windows on this " + (selectedPart.getContainer() instanceof Wall ? "Wall" : "Roof"));
					final JRadioButton rb3 = new JRadioButton("All Windows of this Building");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					final Object[] params = { title, footnote, panel };
					while (true) {
						final String newValue = (String) JOptionPane.showInputDialog(MainFrame.getInstance(), params, "Input: " + partInfo, JOptionPane.QUESTION_MESSAGE, null, null, window.getSolarHeatGainCoefficient());
						if (newValue == null) {
							break;
						} else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val < 0 || val > 1) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar heat gain coefficient must be between 0 and 1.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final ChangeWindowShgcCommand c = new ChangeWindowShgcCommand(window);
										window.setSolarHeatGainCoefficient(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb2.isSelected()) {
										final ChangeContainerWindowShgcCommand c = new ChangeContainerWindowShgcCommand(window.getContainer());
										Scene.getInstance().setWindowShgcInContainer(window.getContainer(), val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb3.isSelected()) {
										final Foundation foundation = window.getTopContainer();
										final ChangeBuildingWindowShgcCommand c = new ChangeBuildingWindowShgcCommand(foundation);
										Scene.getInstance().setWindowShgcOfBuilding(foundation, val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									updateAfterEdit();
									break;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});

			final JMenuItem miTint = new JMenuItem("Tint...");
			miTint.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Window)) {
						return;
					}
					final Window window = (Window) selectedPart;
					final JColorChooser colorChooser = MainFrame.getInstance().getColorChooser();
					final ReadOnlyColorRGBA color = window.getColor();
					if (color != null) {
						colorChooser.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
					}
					final ActionListener actionListener = new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							final Color c = colorChooser.getColor();
							final float[] newColor = c.getComponents(null);
							final ColorRGBA color = new ColorRGBA(newColor[0], newColor[1], newColor[2], (float) (1.0 - window.getSolarHeatGainCoefficient()));
							final JPanel panel = new JPanel();
							panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
							panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
							final JRadioButton rb1 = new JRadioButton("Only this Window", true);
							final JRadioButton rb2 = new JRadioButton("All Windows on this " + (window.getContainer() instanceof Wall ? "Wall" : "Roof"));
							final JRadioButton rb3 = new JRadioButton("All Windows of this Building");
							panel.add(rb1);
							panel.add(rb2);
							panel.add(rb3);
							final ButtonGroup bg = new ButtonGroup();
							bg.add(rb1);
							bg.add(rb2);
							bg.add(rb3);
							if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), panel, "Scope", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
								return;
							}
							if (rb1.isSelected()) { // apply to only this window
								final ChangePartColorCommand cmd = new ChangePartColorCommand(window);
								window.setColor(color);
								SceneManager.getInstance().getUndoManager().addEdit(cmd);
							} else if (rb2.isSelected()) {
								final ChangeContainerWindowColorCommand cmd = new ChangeContainerWindowColorCommand(window.getContainer());
								Scene.getInstance().setWindowColorInContainer(window.getContainer(), color, false);
								SceneManager.getInstance().getUndoManager().addEdit(cmd);
							} else {
								final ChangeBuildingColorCommand cmd = new ChangeBuildingColorCommand(window);
								Scene.getInstance().setPartColorOfBuilding(window, color);
								SceneManager.getInstance().getUndoManager().addEdit(cmd);
							}
							updateAfterEdit();
						}
					};
					JColorChooser.createDialog(MainFrame.getInstance(), "Select Tint", true, colorChooser, actionListener, null).setVisible(true);
				}
			});

			popupMenuForWindow.addSeparator();
			popupMenuForWindow.add(miTint);
			popupMenuForWindow.add(createInsulationMenuItem(true));
			popupMenuForWindow.add(miShgc);
			popupMenuForWindow.add(muntinMenu);
			popupMenuForWindow.add(shutterMenu);
			popupMenuForWindow.addSeparator();

			JMenuItem mi = new JMenuItem("Daily Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Window) {
						new EnergyDailyAnalysis().show("Daily Energy for Window");
					}
				}
			});
			popupMenuForWindow.add(mi);

			mi = new JMenuItem("Annual Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Window) {
						new EnergyAnnualAnalysis().show("Annual Energy for Window");
					}
				}
			});
			popupMenuForWindow.add(mi);

		}

		return popupMenuForWindow;

	}

	private static JPopupMenu getPopupMenuForWall() {

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
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});

			popupMenuForWall = createPopupMenu(false, false, new Runnable() {
				@Override
				public void run() {
					final HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
					miPaste.setEnabled(copyBuffer instanceof Window || copyBuffer instanceof SolarPanel);
				}
			});

			popupMenuForWall.add(miPaste);
			popupMenuForWall.add(miClear);
			popupMenuForWall.addSeparator();
			popupMenuForWall.add(colorAction);
			popupMenuForWall.add(createInsulationMenuItem(false));
			popupMenuForWall.add(createVolumetricHeatCapacityMenuItem());
			popupMenuForWall.addSeparator();

			final JMenu typeMenu = new JMenu("Type");
			popupMenuForWall.add(typeMenu);
			popupMenuForWall.addSeparator();
			final ButtonGroup typeGroup = new ButtonGroup();

			final JRadioButtonMenuItem rbmiSolidWall = new JRadioButtonMenuItem("Solid Wall");
			rbmiSolidWall.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
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
			});
			typeMenu.add(rbmiSolidWall);
			typeGroup.add(rbmiSolidWall);

			final JRadioButtonMenuItem rbmiEmpty = new JRadioButtonMenuItem("Empty");
			rbmiEmpty.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
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
			});
			typeMenu.add(rbmiEmpty);
			typeGroup.add(rbmiEmpty);

			final JRadioButtonMenuItem rbmiEdges = new JRadioButtonMenuItem("Vertical Edges");
			rbmiEdges.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
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
			});
			typeMenu.add(rbmiEdges);
			typeGroup.add(rbmiEdges);

			final JRadioButtonMenuItem rbmiColumns = new JRadioButtonMenuItem("Columns");
			rbmiColumns.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
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
			});
			typeMenu.add(rbmiColumns);
			typeGroup.add(rbmiColumns);

			final JRadioButtonMenuItem rbmiRails = new JRadioButtonMenuItem("Rails");
			rbmiRails.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
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
			});
			typeMenu.add(rbmiRails);
			typeGroup.add(rbmiRails);

			final JRadioButtonMenuItem rbmiColumnsAndRailings = new JRadioButtonMenuItem("Columns & Railings");
			rbmiColumnsAndRailings.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
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
			});
			typeMenu.add(rbmiColumnsAndRailings);
			typeGroup.add(rbmiColumnsAndRailings);

			final JRadioButtonMenuItem rbmiFence = new JRadioButtonMenuItem("Fence");
			rbmiFence.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
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
			});
			typeMenu.add(rbmiFence);
			typeGroup.add(rbmiFence);

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
					if (SceneManager.getInstance().getSelectedPart() instanceof Wall) {
						new EnergyAnnualAnalysis().show("Annual Energy for Wall");
					}
				}
			});
			popupMenuForWall.add(mi);

		}

		return popupMenuForWall;

	}

	private static JPopupMenu getPopupMenuForRoof() {

		if (popupMenuForRoof == null) {

			final JMenuItem miPaste = new JMenuItem("Paste");
			miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			miPaste.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().pasteToPickedLocationOnRoof();
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
							Scene.getInstance().setEdited(true);
							return null;
						}
					});
				}
			});

			final JMenuItem miOverhang = new JMenuItem("Overhang Length...");
			miOverhang.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Roof)) {
						return;
					}
					final Roof roof = (Roof) selectedPart;
					while (true) {
						SceneManager.getInstance().refresh(1);
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), "Overhang Length (m)", roof.getOverhangLength() * Scene.getInstance().getAnnotationScale());
						if (newValue == null) {
							break;
						} else {
							try {
								double val = Double.parseDouble(newValue);
								final double min = Roof.OVERHANG_MIN * Scene.getInstance().getAnnotationScale() * 10;
								if (val < min && val >= 0) {
									val = min;
								}
								if (val < 0 || val > 10) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Overhang value must be between " + min + " and 10.", "Error", JOptionPane.ERROR_MESSAGE);
								} else {
									final ChangeRoofOverhangCommand c = new ChangeRoofOverhangCommand(roof);
									roof.setOverhangLength(val / Scene.getInstance().getAnnotationScale());
									roof.draw();
									final Foundation f = roof.getTopContainer();
									f.drawChildren();
									SceneManager.getInstance().refresh();
									updateAfterEdit();
									SceneManager.getInstance().getUndoManager().addEdit(c);
									break;
								}
							} catch (final NumberFormatException exception) {
								exception.printStackTrace();
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});

			popupMenuForRoof = createPopupMenu(false, false, new Runnable() {
				@Override
				public void run() {
					final HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
					miPaste.setEnabled(copyBuffer instanceof SolarPanel || copyBuffer instanceof Window);
				}
			});

			popupMenuForRoof.add(miPaste);
			popupMenuForRoof.add(miClear);
			popupMenuForRoof.addSeparator();
			popupMenuForRoof.add(miOverhang);
			popupMenuForRoof.add(colorAction);
			popupMenuForRoof.add(createInsulationMenuItem(false));
			popupMenuForRoof.add(createVolumetricHeatCapacityMenuItem());
			popupMenuForRoof.addSeparator();

			JMenuItem mi = new JMenuItem("Daily Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Roof) {
						new EnergyDailyAnalysis().show("Daily Energy for Roof");
					}
				}
			});
			popupMenuForRoof.add(mi);

			mi = new JMenuItem("Annual Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Roof) {
						new EnergyAnnualAnalysis().show("Annual Energy for Roof");
					}
				}
			});
			popupMenuForRoof.add(mi);

		}

		return popupMenuForRoof;

	}

	private static JPopupMenu getPopupMenuForDoor() {

		if (popupMenuForDoor == null) {
			popupMenuForDoor = createPopupMenu(false, false, null);
			popupMenuForDoor.addSeparator();
			popupMenuForDoor.add(colorAction);
			popupMenuForDoor.add(createInsulationMenuItem(true));
			popupMenuForDoor.add(createVolumetricHeatCapacityMenuItem());
			popupMenuForDoor.addSeparator();

			JMenuItem mi = new JMenuItem("Daily Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
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
					if (SceneManager.getInstance().getSelectedPart() instanceof Door) {
						new EnergyAnnualAnalysis().show("Annual Energy for Door");
					}
				}
			});
			popupMenuForDoor.add(mi);

		}

		return popupMenuForDoor;

	}

	private static JPopupMenu getPopupMenuForFoundation() {

		if (popupMenuForFoundation == null) {

			final JMenuItem miPaste = new JMenuItem("Paste");
			miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			miPaste.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().pasteToPickedLocationOnFoundation();
							Scene.getInstance().setEdited(true);
							return null;
						}
					});
				}
			});

			final JMenuItem miCopyBuilding = new JMenuItem("Copy Building");
			miCopyBuilding.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						Scene.getInstance().setCopyBuffer(selectedPart);
					}
				}
			});

			final JMenuItem miRescale = new JMenuItem("Rescale...");
			miRescale.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Foundation)) {
						return;
					}
					new RescaleBuildingDialog((Foundation) selectedPart).setVisible(true);
					Scene.getInstance().setEdited(true);
				}
			});

			final JMenu rotateMenu = new JMenu("Rotate");

			final JMenuItem mi180 = new JMenuItem("180\u00B0");
			mi180.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().rotate(Math.PI);
					Scene.getInstance().setEdited(true);
				}
			});
			rotateMenu.add(mi180);

			final JMenuItem mi90CW = new JMenuItem("90\u00B0 Clockwise");
			mi90CW.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().rotate(-Math.PI / 2);
					Scene.getInstance().setEdited(true);
				}
			});
			rotateMenu.add(mi90CW);

			final JMenuItem mi90CCW = new JMenuItem("90\u00B0 Counter Clockwise");
			mi90CCW.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().rotate(Math.PI / 2);
					Scene.getInstance().setEdited(true);
				}
			});
			rotateMenu.add(mi90CCW);

			final JMenu clearMenu = new JMenu("Clear");

			final JMenuItem miRemoveAllWindows = new JMenuItem("Remove All Windows");
			miRemoveAllWindows.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllWindows();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});
			clearMenu.add(miRemoveAllWindows);

			final JMenuItem miRemoveAllWindowShutters = new JMenuItem("Remove All Window Shutters");
			miRemoveAllWindowShutters.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllWindowShutters();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});
			clearMenu.add(miRemoveAllWindowShutters);

			final JMenuItem miRemoveAllSolarPanels = new JMenuItem("Remove All Solar Panels");
			miRemoveAllSolarPanels.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllSolarPanels();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});
			clearMenu.add(miRemoveAllSolarPanels);

			final JMenuItem miRemoveAllMirrors = new JMenuItem("Remove All Mirrors");
			miRemoveAllMirrors.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllMirrors();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});
			clearMenu.add(miRemoveAllMirrors);

			final JMenuItem removeAllFloorsMenuItem = new JMenuItem("Remove All Floors");
			removeAllFloorsMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllFloors();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});
			clearMenu.add(removeAllFloorsMenuItem);

			final JMenu layoutMenu = new JMenu("Layout");

			final JMenuItem miSolarPanelArrays = new JMenuItem("Solar Panel Arrays...");
			layoutMenu.add(miSolarPanelArrays);
			miSolarPanelArrays.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final int n = f.countParts(SolarPanel.class);
						if (n > 0 && JOptionPane.showConfirmDialog(MainFrame.getInstance(), "All existing " + n + " solar panels on this platform must be removed before\na new layout can be applied. Do you want to continue?", "Confirmation", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
							return;
						}
						final JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
						panel.add(new JLabel("Panel Size:"));
						final JComboBox<String> sizeComboBox = new JComboBox<String>(new String[] { "0.99m \u00D7 1.65m", "1.04m \u00D7 1.55m", "0.99m \u00D7 1.96m" });
						if (Util.isZero(0.99 - solarPanelWidth) && Util.isZero(1.65 - solarPanelHeight)) {
							sizeComboBox.setSelectedIndex(0);
						} else if (Util.isZero(1.04 - solarPanelWidth) && Util.isZero(1.55 - solarPanelHeight)) {
							sizeComboBox.setSelectedIndex(1);
						} else {
							sizeComboBox.setSelectedIndex(2);
						}
						panel.add(sizeComboBox);
						panel.add(new JLabel("Row Axis:"));
						final JComboBox<String> rowAxisComboBox = new JComboBox<String>(new String[] { "North-South", "East-West" });
						rowAxisComboBox.setSelectedIndex(pvArrayRowAxis);
						panel.add(rowAxisComboBox);
						panel.add(new JLabel("Row Spacing:"));
						final JTextField rowSpacingField = new JTextField(twoDecimalsFormat.format(pvArrayRowSpacing));
						panel.add(rowSpacingField);
						panel.add(new JLabel("Column Spacing:"));
						final JTextField colSpacingField = new JTextField(twoDecimalsFormat.format(pvArrayColSpacing));
						panel.add(colSpacingField);
						boolean ok = false;
						while (true) {
							if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), panel, "Solar Panel Array Options", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
								final String rowValue = rowSpacingField.getText();
								final String colValue = colSpacingField.getText();
								try {
									pvArrayRowSpacing = Double.parseDouble(rowValue);
									pvArrayColSpacing = Double.parseDouble(colValue);
									if (pvArrayRowSpacing < 0 || pvArrayColSpacing < 0) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Spacing cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else {
										ok = true;
										break;
									}
								} catch (final NumberFormatException exception) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								}
							} else {
								break;
							}
						}
						if (ok) {
							switch (sizeComboBox.getSelectedIndex()) {
							case 0:
								solarPanelWidth = 0.99;
								solarPanelHeight = 1.65;
								break;
							case 1:
								solarPanelWidth = 1.04;
								solarPanelHeight = 1.55;
								break;
							default:
								solarPanelWidth = 0.99;
								solarPanelHeight = 1.96;
								break;
							}
							pvArrayRowAxis = rowAxisComboBox.getSelectedIndex();
							SceneManager.getTaskManager().update(new Callable<Object>() {
								@Override
								public Object call() {
									f.addSolarPanelArrays(solarPanelWidth, solarPanelHeight, pvArrayRowSpacing, pvArrayColSpacing, pvArrayRowAxis);
									return null;
								}
							});
							updateAfterEdit();
						}
					}
				}
			});

			layoutMenu.addSeparator();

			final JMenuItem miMirrorCircularArrays = new JMenuItem("Circular Mirror Arrays");
			layoutMenu.add(miMirrorCircularArrays);
			miMirrorCircularArrays.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final int n = f.countParts(Mirror.class);
						if (n > 0 && JOptionPane.showConfirmDialog(MainFrame.getInstance(), "All existing " + n + " mirrors on this platform must be removed before\na new layout can be applied. Do you want to continue?", "Confirmation", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
							return;
						}
						final JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
						final JLabel widthLabel = new JLabel("Mirror Width: ");
						panel.add(widthLabel);
						final JTextField widthField = new JTextField(twoDecimalsFormat.format(mirrorWidth));
						panel.add(widthField);
						final JLabel heightLabel = new JLabel("Mirror Height: ");
						panel.add(heightLabel);
						final JTextField heightField = new JTextField(twoDecimalsFormat.format(mirrorHeight));
						panel.add(heightField);
						panel.add(new JLabel("Layout:"));
						final JComboBox<String> layoutComboBox = new JComboBox<String>(new String[] { "Circular (surrounding tower)", "Semicircular (north to tower)", "Semicircular (south to tower)" });
						layoutComboBox.setSelectedIndex(mirrorArrayLayout);
						panel.add(layoutComboBox);
						panel.add(new JLabel("Radial Spacing:"));
						final JTextField rowSpacingField = new JTextField(twoDecimalsFormat.format(mirrorArrayRadialSpacing));
						panel.add(rowSpacingField);
						panel.add(new JLabel("Azimuthal Spacing:"));
						final JTextField colSpacingField = new JTextField(twoDecimalsFormat.format(mirrorArrayAzimuthalSpacing));
						panel.add(colSpacingField);
						boolean ok = false;
						while (true) {
							if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), panel, "Circular Mirror Array Options", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
								final String rowValue = rowSpacingField.getText();
								final String colValue = colSpacingField.getText();
								try {
									mirrorArrayRadialSpacing = Double.parseDouble(rowValue);
									mirrorArrayAzimuthalSpacing = Double.parseDouble(colValue);
									mirrorWidth = Double.parseDouble(widthField.getText());
									mirrorHeight = Double.parseDouble(heightField.getText());
									if (mirrorArrayRadialSpacing < 0 || mirrorArrayAzimuthalSpacing < 0) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Mirror spacing cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorWidth < 1 || mirrorWidth > 6 || mirrorHeight < 1 || mirrorHeight > 6) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Mirror width and height must be between 1 and 6 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else {
										ok = true;
										break;
									}
								} catch (final NumberFormatException exception) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								}
							} else {
								break;
							}
						}
						if (ok) {
							mirrorArrayLayout = layoutComboBox.getSelectedIndex();
							SceneManager.getTaskManager().update(new Callable<Object>() {
								@Override
								public Object call() {
									f.addCircularMirrorArrays(mirrorWidth, mirrorHeight, mirrorArrayRadialSpacing, mirrorArrayAzimuthalSpacing, mirrorArrayLayout);
									return null;
								}
							});
							updateAfterEdit();
						}
					}
				}
			});

			final JMenuItem miMirrorRadialStaggerArrays = new JMenuItem("Radial Stagger Mirror Arrays");
			// layoutMenu.add(miMirrorRadialStaggerArrays);
			miMirrorRadialStaggerArrays.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final int n = f.countParts(Mirror.class);
						if (n > 0 && JOptionPane.showConfirmDialog(MainFrame.getInstance(), "All existing " + n + " mirrors on this platform must be removed before\na new layout can be applied. Do you want to continue?", "Confirmation", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
							return;
						}
						SceneManager.getTaskManager().update(new Callable<Object>() {
							@Override
							public Object call() {
								f.addRadialStaggerMirrorArrays();
								return null;
							}
						});
						updateAfterEdit();
					}
				}
			});

			final JMenuItem miAddUtilityBill = new JMenuItem("Add Utility Bill");
			miAddUtilityBill.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						UtilityBill b = f.getUtilityBill();
						if (b == null) {
							if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "No utility bill is found for this building. Create one?", "Utility Bill for Building #" + f.getId(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
								return;
							}
							b = new UtilityBill();
							f.setUtilityBill(b);
						}
						new UtilityBillDialog(b).setVisible(true);
						Scene.getInstance().setEdited(true);
					}
				}
			});

			final JMenuItem miDeleteUtilityBill = new JMenuItem("Delete Utility Bill");
			miDeleteUtilityBill.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						if (f.getUtilityBill() == null) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no utilitiy bill associated with this building.", "No Utility Bill", JOptionPane.INFORMATION_MESSAGE);
						} else {
							if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove the utility bill associated with this building?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
								final DeleteUtilityBillCommand c = new DeleteUtilityBillCommand(f);
								f.setUtilityBill(null);
								Scene.getInstance().setEdited(true);
								SceneManager.getInstance().getUndoManager().addEdit(c);
							}
						}
					}
				}
			});

			final JCheckBoxMenuItem miLock = new JCheckBoxMenuItem("Lock");
			miLock.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation foundation = (Foundation) selectedPart;
						SceneManager.getInstance().getUndoManager().addEdit(new LockPartCommand(foundation));
						final boolean lock = miLock.isSelected();
						foundation.setFreeze(lock);
						for (final HousePart p : Scene.getInstance().getParts()) {
							if (p.getTopContainer() == foundation) {
								p.setFreeze(lock);
							}
						}
						if (lock) {
							SceneManager.getInstance().hideAllEditPoints();
						}
						foundation.draw();
						foundation.drawChildren();
						Scene.getInstance().setEdited(true);
					}
				}
			});

			final JCheckBoxMenuItem miDisableEdits = new JCheckBoxMenuItem("Disable Edits");
			miDisableEdits.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						((Foundation) selectedPart).setLockEdit(miDisableEdits.isSelected());
						Scene.getInstance().setEdited(true);
					}
				}
			});

			final JMenuItem miThermostat = new JMenuItem("Thermostat...");
			miThermostat.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation foundation = (Foundation) selectedPart;
						MainPanel.getInstance().getEnergyViewButton().setSelected(false);
						new ThermostatDialog(foundation).setVisible(true);
						TimeSeriesLogger.getInstance().logAdjustThermostatButton();
						Scene.getInstance().setEdited(true);
					}
				}
			});

			final JMenuItem miHeight = new JMenuItem("Height...");
			miHeight.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Foundation)) {
						return;
					}
					final Foundation f = (Foundation) selectedPart;
					while (true) {
						SceneManager.getInstance().refresh(1);
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), "Height (m)", f.getHeight() * Scene.getInstance().getAnnotationScale());
						if (newValue == null) {
							break;
						} else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val < 0 || val > 10) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Height must be between 0 and 10 m.", "Error", JOptionPane.ERROR_MESSAGE);
								} else {
									final ChangeFoundationHeightCommand c = new ChangeFoundationHeightCommand(f);
									f.setHeight(val / Scene.getInstance().getAnnotationScale());
									f.draw();
									f.drawChildren();
									SceneManager.getInstance().refresh();
									updateAfterEdit();
									SceneManager.getInstance().getUndoManager().addEdit(c);
									break;
								}
							} catch (final NumberFormatException exception) {
								exception.printStackTrace();
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});

			popupMenuForFoundation = createPopupMenu(false, true, new Runnable() {
				@Override
				public void run() {
					final HousePart p = SceneManager.getInstance().getSelectedPart();
					if (p instanceof Foundation) {
						final Foundation f = (Foundation) p;
						if (Scene.getInstance().isStudentMode()) {
							miLock.setEnabled(false);
							miDisableEdits.setEnabled(false);
							miThermostat.setEnabled(false);
						} else {
							miLock.setEnabled(true);
							miDisableEdits.setEnabled(true);
							miThermostat.setEnabled(true);
						}
						miDeleteUtilityBill.setEnabled(f.getUtilityBill() != null);
						Util.selectSilently(miLock, f.isFrozen());
						Util.selectSilently(miDisableEdits, f.getLockEdit());
					}
				}
			});

			popupMenuForFoundation.add(miPaste);
			popupMenuForFoundation.add(miCopyBuilding);
			popupMenuForFoundation.addSeparator();
			popupMenuForFoundation.add(miHeight);
			popupMenuForFoundation.add(miRescale);
			popupMenuForFoundation.add(rotateMenu);
			popupMenuForFoundation.add(clearMenu);
			popupMenuForFoundation.add(layoutMenu);
			popupMenuForFoundation.addSeparator();
			popupMenuForFoundation.add(miLock);
			popupMenuForFoundation.add(miDisableEdits);
			popupMenuForFoundation.addSeparator();
			popupMenuForFoundation.add(colorAction);
			// floor insulation only for the first floor, so this U-value is associated with the Foundation class, not the Floor class
			popupMenuForFoundation.add(createInsulationMenuItem(false));
			popupMenuForFoundation.add(createVolumetricHeatCapacityMenuItem());
			popupMenuForFoundation.add(miThermostat);
			popupMenuForFoundation.addSeparator();
			popupMenuForFoundation.add(miAddUtilityBill);
			popupMenuForFoundation.add(miDeleteUtilityBill);
			popupMenuForFoundation.addSeparator();

			final JMenu analysisMenu = new JMenu("Analysis");
			popupMenuForFoundation.add(analysisMenu);

			JMenuItem mi = new JMenuItem("Daily Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Foundation) {
						final EnergyDailyAnalysis analysis = new EnergyDailyAnalysis();
						if (SceneManager.getInstance().getSolarHeatMap()) {
							analysis.updateGraph();
						}
						analysis.show("Daily Energy");
					}
				}
			});
			analysisMenu.add(mi);

			mi = new JMenuItem("Annual Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Foundation) {
						new EnergyAnnualAnalysis().show("Annual Energy");
					}
				}
			});
			analysisMenu.add(mi);
			analysisMenu.addSeparator();

			mi = new JMenuItem("Daily Solar Panel Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Foundation) {
						final Foundation f = (Foundation) SceneManager.getInstance().getSelectedPart();
						if (f.countParts(SolarPanel.class) <= 0) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no solar panel on this building to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						final PvDailyAnalysis a = new PvDailyAnalysis();
						if (SceneManager.getInstance().getSolarHeatMap()) {
							a.updateGraph();
						}
						a.show();
					}
				}
			});
			analysisMenu.add(mi);

			mi = new JMenuItem("Annual Solar Panel Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						if (f.countParts(SolarPanel.class) <= 0) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no solar panel on this building to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						final PvAnnualAnalysis a = new PvAnnualAnalysis();
						if (f.getUtilityBill() != null) {
							a.setUtilityBill(f.getUtilityBill());
						}
						a.show();
					}
				}
			});
			analysisMenu.add(mi);
			analysisMenu.addSeparator();

			mi = new JMenuItem("Daily Mirror Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Foundation) {
						final Foundation f = (Foundation) SceneManager.getInstance().getSelectedPart();
						if (f.countParts(Mirror.class) <= 0) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no mirror on this building to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						final MirrorDailyAnalysis a = new MirrorDailyAnalysis();
						if (SceneManager.getInstance().getSolarHeatMap()) {
							a.updateGraph();
						}
						a.show();
					}
				}
			});
			analysisMenu.add(mi);

			mi = new JMenuItem("Annual Mirror Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						if (f.countParts(Mirror.class) <= 0) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no mirror on this building to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						new MirrorAnnualAnalysis().show();
					}
				}
			});
			analysisMenu.add(mi);

		}

		return popupMenuForFoundation;

	}

	private static JPopupMenu getPopupMenuForSensor() {

		if (popupMenuForSensor == null) {

			final JCheckBoxMenuItem miLight = new JCheckBoxMenuItem("Light", true);
			miLight.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Sensor)) {
						return;
					}
					final Sensor s = (Sensor) selectedPart;
					s.setLightOff(!miLight.isSelected());
					Scene.getInstance().setEdited(true);
				}
			});

			final JCheckBoxMenuItem miHeatFlux = new JCheckBoxMenuItem("Heat Flux", true);
			miHeatFlux.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Sensor)) {
						return;
					}
					final Sensor s = (Sensor) selectedPart;
					s.setHeatFluxOff(!miHeatFlux.isSelected());
					Scene.getInstance().setEdited(true);
				}
			});

			popupMenuForSensor = createPopupMenu(false, false, new Runnable() {
				@Override
				public void run() {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Sensor)) {
						return;
					}
					final Sensor s = (Sensor) selectedPart;
					Util.selectSilently(miLight, !s.isLightOff());
					Util.selectSilently(miHeatFlux, !s.isHeatFluxOff());
				}
			});

			popupMenuForSensor.addSeparator();
			popupMenuForSensor.add(miLight);
			popupMenuForSensor.add(miHeatFlux);

		}

		return popupMenuForSensor;

	}

	private static JPopupMenu getPopupMenuForSolarPanel() {

		if (popupMenuForSolarPanel == null) {

			final JMenu trackerMenu = new JMenu("Tracker");
			final JMenu shadeToleranceMenu = new JMenu("Shade Tolerance");

			final ButtonGroup shadeToleranceButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem miHighTolerance = new JRadioButtonMenuItem("High Tolerance...");
			shadeToleranceButtonGroup.add(miHighTolerance);
			miHighTolerance.addActionListener(new ActionListener() {
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
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Platform");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					final String title = "<html>Choose shade tolerance level for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The energy generated by this panel comes from each cell proportionally (ideal case).<hr></html>";
					final Object[] params = { title, footnote, panel };
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), params, "Shade tolerance", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					if (rb1.isSelected()) {
						final SetShadeToleranceCommand c = new SetShadeToleranceCommand(sp);
						sp.setShadeTolerance(SolarPanel.HIGH_SHADE_TOLERANCE);
						sp.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
					} else if (rb2.isSelected()) {
						final Foundation foundation = sp.getTopContainer();
						final SetFoundationShadeToleranceCommand c = new SetFoundationShadeToleranceCommand(foundation);
						Scene.getInstance().setShadeToleranceForSolarPanelsOnFoundation(foundation, SolarPanel.HIGH_SHADE_TOLERANCE);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					} else if (rb3.isSelected()) {
						final SetShadeToleranceForAllSolarPanelsCommand c = new SetShadeToleranceForAllSolarPanelsCommand();
						Scene.getInstance().setShadeToleranceForAllSolarPanels(SolarPanel.HIGH_SHADE_TOLERANCE);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
					updateAfterEdit();
				}
			});

			final JRadioButtonMenuItem miPartialTolerance = new JRadioButtonMenuItem("Partial Tolerance...", true);
			shadeToleranceButtonGroup.add(miPartialTolerance);
			miPartialTolerance.addActionListener(new ActionListener() {
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
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Platform");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					final String title = "<html>Choose shade tolerance level for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>Use bypass diodes to direct current under shading conditions.<hr></html>";
					final Object[] params = { title, footnote, panel };
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), params, "Shade tolerance", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					if (rb1.isSelected()) {
						final SetShadeToleranceCommand c = new SetShadeToleranceCommand(sp);
						sp.setShadeTolerance(SolarPanel.PARTIAL_SHADE_TOLERANCE);
						sp.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
					} else if (rb2.isSelected()) {
						final Foundation foundation = sp.getTopContainer();
						final SetFoundationShadeToleranceCommand c = new SetFoundationShadeToleranceCommand(foundation);
						Scene.getInstance().setShadeToleranceForSolarPanelsOnFoundation(foundation, SolarPanel.PARTIAL_SHADE_TOLERANCE);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					} else if (rb3.isSelected()) {
						final SetShadeToleranceForAllSolarPanelsCommand c = new SetShadeToleranceForAllSolarPanelsCommand();
						Scene.getInstance().setShadeToleranceForAllSolarPanels(SolarPanel.PARTIAL_SHADE_TOLERANCE);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
					updateAfterEdit();
				}
			});

			final JRadioButtonMenuItem miNoTolerance = new JRadioButtonMenuItem("No Tolerance...");
			shadeToleranceButtonGroup.add(miNoTolerance);
			miNoTolerance.addActionListener(new ActionListener() {
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
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Platform");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					final String title = "<html>Choose shade tolerance level for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>Shading greatly reduces the output of the entire panel.<hr></html>";
					final Object[] params = { title, footnote, panel };
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), params, "Shade tolerance", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					if (rb1.isSelected()) {
						final SetShadeToleranceCommand c = new SetShadeToleranceCommand(sp);
						sp.setShadeTolerance(SolarPanel.NO_SHADE_TOLERANCE);
						sp.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
					} else if (rb2.isSelected()) {
						final Foundation foundation = sp.getTopContainer();
						final SetFoundationShadeToleranceCommand c = new SetFoundationShadeToleranceCommand(foundation);
						Scene.getInstance().setShadeToleranceForSolarPanelsOnFoundation(foundation, SolarPanel.NO_SHADE_TOLERANCE);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					} else if (rb3.isSelected()) {
						final SetShadeToleranceForAllSolarPanelsCommand c = new SetShadeToleranceForAllSolarPanelsCommand();
						Scene.getInstance().setShadeToleranceForAllSolarPanels(SolarPanel.NO_SHADE_TOLERANCE);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
					updateAfterEdit();
				}
			});

			final ButtonGroup trackerButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem miNoTracker = new JRadioButtonMenuItem("No Tracker...", true);
			trackerButtonGroup.add(miNoTracker);
			miNoTracker.addActionListener(new ActionListener() {
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
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Platform");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					final String title = "<html>Disable tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>No tracker will be used.<hr></html>";
					final Object[] params = { title, footnote, panel };
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), params, "Disable solar tracker", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					if (rb1.isSelected()) {
						final SetSolarTrackerCommand c = new SetSolarTrackerCommand(sp);
						sp.setTracker(SolarPanel.NO_TRACKER);
						sp.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
					} else if (rb2.isSelected()) {
						final Foundation foundation = sp.getTopContainer();
						final SetFoundationSolarTrackerCommand c = new SetFoundationSolarTrackerCommand(foundation);
						Scene.getInstance().setTrackerForSolarPanelsOnFoundation(foundation, SolarPanel.NO_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					} else if (rb3.isSelected()) {
						final SetTrackerForAllSolarPanelsCommand c = new SetTrackerForAllSolarPanelsCommand();
						Scene.getInstance().setTrackerForAllSolarPanels(SolarPanel.NO_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
					updateAfterEdit();
				}
			});

			final JRadioButtonMenuItem miHorizontalSingleAxisTracker = new JRadioButtonMenuItem("Horizontal Single-Axis Tracker...");
			trackerButtonGroup.add(miHorizontalSingleAxisTracker);
			miHorizontalSingleAxisTracker.addActionListener(new ActionListener() {
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
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Platform");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					final String title = "<html>Enable horizontal single-axis tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2><hr></html>";
					final Object[] params = { title, footnote, panel };
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), params, "Enable horizontal single-axis solar tracker", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					if (rb1.isSelected()) {
						final SetSolarTrackerCommand c = new SetSolarTrackerCommand(sp);
						sp.setTracker(SolarPanel.HORIZONTAL_SINGLE_AXIS_TRACKER);
						sp.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
					} else if (rb2.isSelected()) {
						final Foundation foundation = sp.getTopContainer();
						final SetFoundationSolarTrackerCommand c = new SetFoundationSolarTrackerCommand(foundation);
						Scene.getInstance().setTrackerForSolarPanelsOnFoundation(foundation, SolarPanel.HORIZONTAL_SINGLE_AXIS_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					} else if (rb3.isSelected()) {
						final SetTrackerForAllSolarPanelsCommand c = new SetTrackerForAllSolarPanelsCommand();
						Scene.getInstance().setTrackerForAllSolarPanels(SolarPanel.HORIZONTAL_SINGLE_AXIS_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
					updateAfterEdit();
				}
			});

			final JRadioButtonMenuItem miVerticalSingleAxisTracker = new JRadioButtonMenuItem("Vertical Single-Axis Tracker...");
			trackerButtonGroup.add(miVerticalSingleAxisTracker);
			miVerticalSingleAxisTracker.addActionListener(new ActionListener() {
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
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Platform");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					final String title = "<html>Enable vertical single-axis tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2><hr></html>";
					final Object[] params = { title, footnote, panel };
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), params, "Enable vertical single-axis solar tracker", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					if (rb1.isSelected()) {
						final SetSolarTrackerCommand c = new SetSolarTrackerCommand(sp);
						sp.setTracker(SolarPanel.VERTICAL_SINGLE_AXIS_TRACKER);
						sp.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
					} else if (rb2.isSelected()) {
						final Foundation foundation = sp.getTopContainer();
						final SetFoundationSolarTrackerCommand c = new SetFoundationSolarTrackerCommand(foundation);
						Scene.getInstance().setTrackerForSolarPanelsOnFoundation(foundation, SolarPanel.VERTICAL_SINGLE_AXIS_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					} else if (rb3.isSelected()) {
						final SetTrackerForAllSolarPanelsCommand c = new SetTrackerForAllSolarPanelsCommand();
						Scene.getInstance().setTrackerForAllSolarPanels(SolarPanel.VERTICAL_SINGLE_AXIS_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
					updateAfterEdit();
				}
			});

			final JRadioButtonMenuItem miAltazimuthDualAxisTracker = new JRadioButtonMenuItem("Altazimuth Dual-Axis Tracker...");
			trackerButtonGroup.add(miAltazimuthDualAxisTracker);
			miAltazimuthDualAxisTracker.addActionListener(new ActionListener() {
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
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Platform");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					final String title = "<html>Enable altitude-azimuth dual-axis tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The Alt/Az dual-axis solar tracker will rotate the solar panel to face the sun exactly.<hr></html>";
					final Object[] params = { title, footnote, panel };
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), params, "Enable altitude-azimuth dual-axis solar tracker", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					if (rb1.isSelected()) {
						final SetSolarTrackerCommand c = new SetSolarTrackerCommand(sp);
						sp.setTracker(SolarPanel.ALTAZIMUTH_DUAL_AXIS_TRACKER);
						sp.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
					} else if (rb2.isSelected()) {
						final Foundation foundation = sp.getTopContainer();
						final SetFoundationSolarTrackerCommand c = new SetFoundationSolarTrackerCommand(foundation);
						Scene.getInstance().setTrackerForSolarPanelsOnFoundation(foundation, SolarPanel.ALTAZIMUTH_DUAL_AXIS_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					} else if (rb3.isSelected()) {
						final SetTrackerForAllSolarPanelsCommand c = new SetTrackerForAllSolarPanelsCommand();
						Scene.getInstance().setTrackerForAllSolarPanels(SolarPanel.ALTAZIMUTH_DUAL_AXIS_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
					updateAfterEdit();
				}
			});

			final JCheckBoxMenuItem miRotateAroundNormal = new JCheckBoxMenuItem("Rotate 90\u00B0");
			miRotateAroundNormal.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel s = (SolarPanel) selectedPart;
					final RotateSolarPanelCommand c = new RotateSolarPanelCommand(s);
					s.setRotated(miRotateAroundNormal.isSelected());
					SceneManager.getInstance().getUndoManager().addEdit(c);
					s.draw();
					updateAfterEdit();
				}
			});

			final JMenuItem miTiltAngle = new JMenuItem("Tilt Angle...");
			miTiltAngle.addActionListener(new ActionListener() {
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
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on This Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					final Object[] params = { title, footnote, panel };
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), params, sp.getTiltAngle());
						if (newValue == null) {
							break;
						} else {
							try {
								double val = Double.parseDouble(newValue);
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
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb2.isSelected()) {
										final Foundation foundation = sp.getTopContainer();
										final ChangeFoundationSolarPanelTiltAngleCommand c = new ChangeFoundationSolarPanelTiltAngleCommand(foundation);
										Scene.getInstance().setTiltAngleForSolarPanelsOnFoundation(foundation, val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb3.isSelected()) {
										final ChangeTiltAngleForAllSolarPanelsCommand c = new ChangeTiltAngleForAllSolarPanelsCommand();
										Scene.getInstance().setTiltAngleForAllSolarPanels(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									sp.draw();
									updateAfterEdit();
									break;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});

			final JMenuItem miAzimuth = new JMenuItem("Azimuth...");
			miAzimuth.addActionListener(new ActionListener() {
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
					final Object[] params = { title, footnote, panel };
					while (true) {
						double a = sp.getRelativeAzimuth() + foundation.getAzimuth();
						if (a > 360) {
							a -= 360;
						}
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), params, a);
						if (newValue == null) {
							break;
						} else {
							try {
								final double val = Double.parseDouble(newValue);
								a = val - foundation.getAzimuth();
								if (a < 0) {
									a += 360;
								}
								if (rb1.isSelected()) {
									final ChangeAzimuthCommand c = new ChangeAzimuthCommand(sp);
									sp.setRelativeAzimuth(a);
									sp.draw();
									SceneManager.getInstance().getUndoManager().addEdit(c);
								} else if (rb2.isSelected()) {
									final ChangeFoundationSolarPanelAzimuthCommand c = new ChangeFoundationSolarPanelAzimuthCommand(foundation);
									Scene.getInstance().setAzimuthForSolarPanelsOnFoundation(foundation, a);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								} else if (rb3.isSelected()) {
									final ChangeAzimuthForAllSolarPanelsCommand c = new ChangeAzimuthForAllSolarPanelsCommand();
									Scene.getInstance().setAzimuthForAllSolarPanels(a);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								sp.draw();
								updateAfterEdit();
								break;
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});

			final JMenuItem miSize = new JMenuItem("Size...");
			miSize.addActionListener(new ActionListener() {

				private double w = 0.99;
				private double h = 1.65;

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
					final JComboBox<String> typeComboBox = new JComboBox<String>(new String[] { "0.99m \u00D7 1.65m", "1.04m \u00D7 1.55m", "0.99m \u00D7 1.96m" });
					if (Util.isZero(s.getPanelHeight() - 1.65)) {
						typeComboBox.setSelectedIndex(0);
					} else if (Util.isZero(s.getPanelHeight() - 1.55)) {
						typeComboBox.setSelectedIndex(1);
					} else if (Util.isZero(s.getPanelHeight() - 1.96)) {
						typeComboBox.setSelectedIndex(2);
					}
					typeComboBox.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(final ItemEvent e) {
							switch (typeComboBox.getSelectedIndex()) {
							case 0:
								w = 0.99;
								h = 1.65;
								break;
							case 1:
								w = 1.04;
								h = 1.55;
								break;
							case 2:
								w = 0.99;
								h = 1.96;
								break;
							}
						}
					});
					gui.add(typeComboBox, BorderLayout.NORTH);
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), gui, "Set Size", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					final ChooseSolarPanelSizeCommand c = new ChooseSolarPanelSizeCommand(s);
					s.setPanelWidth(w);
					s.setPanelHeight(h);
					s.draw();
					SceneManager.getInstance().getUndoManager().addEdit(c);
					updateAfterEdit();
				}
			});

			final JMenuItem miBaseHeight = new JMenuItem("Base Height...");
			miBaseHeight.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final SolarPanel sp = (SolarPanel) selectedPart;
					final Foundation foundation = sp.getTopContainer();
					final String title = "<html>Base Height of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2></html>";
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
					final Object[] params = { title, footnote, panel };
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), params, sp.getBaseHeight() * Scene.getInstance().getAnnotationScale());
						if (newValue == null) {
							break;
						} else {
							try {
								final double val = Double.parseDouble(newValue) / Scene.getInstance().getAnnotationScale();
								if (rb1.isSelected()) {
									final ChangeBaseHeightCommand c = new ChangeBaseHeightCommand(sp);
									sp.setBaseHeight(val);
									sp.draw();
									SceneManager.getInstance().getUndoManager().addEdit(c);
								} else if (rb2.isSelected()) {
									final ChangeFoundationSolarPanelBaseHeightCommand c = new ChangeFoundationSolarPanelBaseHeightCommand(foundation);
									Scene.getInstance().setBaseHeightForSolarPanelsOnFoundation(foundation, val);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								} else if (rb3.isSelected()) {
									final ChangeBaseHeightForAllSolarPanelsCommand c = new ChangeBaseHeightForAllSolarPanelsCommand();
									Scene.getInstance().setBaseHeightForAllSolarPanels(val);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								sp.draw();
								updateAfterEdit();
								break;
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
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
					sp.setSunBeamVisible(cbmiDrawSunBeam.isSelected());
					sp.drawSunBeam();
					sp.draw();
					Scene.getInstance().setEdited(true);
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
					Util.selectSilently(cbmiDrawSunBeam, sp.isDrawSunBeamVisible());
					Util.selectSilently(miRotateAroundNormal, sp.isRotated());
					switch (sp.getTracker()) {
					case SolarPanel.ALTAZIMUTH_DUAL_AXIS_TRACKER:
						Util.selectSilently(miAltazimuthDualAxisTracker, true);
						break;
					case SolarPanel.HORIZONTAL_SINGLE_AXIS_TRACKER:
						Util.selectSilently(miHorizontalSingleAxisTracker, true);
						break;
					case SolarPanel.VERTICAL_SINGLE_AXIS_TRACKER:
						Util.selectSilently(miVerticalSingleAxisTracker, true);
						break;
					case SolarPanel.NO_TRACKER:
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
					} else if (sp.getContainer() instanceof Wall) {
						miAltazimuthDualAxisTracker.setEnabled(false);
						miHorizontalSingleAxisTracker.setEnabled(false);
						miVerticalSingleAxisTracker.setEnabled(false);
					}
					if (sp.getTracker() != SolarPanel.NO_TRACKER) {
						miTiltAngle.setEnabled(sp.getTracker() == SolarPanel.VERTICAL_SINGLE_AXIS_TRACKER);
						miAzimuth.setEnabled(false);
					} else {
						miTiltAngle.setEnabled(true);
						miAzimuth.setEnabled(true);
						if (sp.getContainer() instanceof Roof) {
							final Roof roof = (Roof) sp.getContainer();
							if (roof.getHeight() > 0) {
								miTiltAngle.setEnabled(false);
								miAzimuth.setEnabled(false);
							}
						} else if (sp.getContainer() instanceof Wall) {
							miTiltAngle.setEnabled(false);
							miAzimuth.setEnabled(false);
						}
					}
				}
			});

			final JMenuItem miEff = new JMenuItem("Solar Cell Efficiency...");
			miEff.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final SolarPanel solarPanel = (SolarPanel) selectedPart;
					final String title = "<html>Solar Cell Efficiency (%) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>How efficient can a solar panel be?<br>The Shockley-Queisser limit is 34% and the theoretical limit for multilayer cells is 86%.<br>As of 2016, the best solar panel in the market has an efficiency of 24%.<br>So the highest efficiency you can choose is limited to 30%.<hr></html>";
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels of this Building");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					final Object[] params = { title, footnote, panel };
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), params, solarPanel.getCellEfficiency() * 100);
						if (newValue == null) {
							break;
						} else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val < 10 || val > 30) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar cell efficiency must be between 10% and 30%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final ChangeSolarCellEfficiencyCommand c = new ChangeSolarCellEfficiencyCommand(solarPanel);
										solarPanel.setCellEfficiency(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb2.isSelected()) {
										final Foundation foundation = solarPanel.getTopContainer();
										final ChangeFoundationSolarCellEfficiencyCommand c = new ChangeFoundationSolarCellEfficiencyCommand(foundation);
										Scene.getInstance().setSolarCellEfficiencyOnFoundation(foundation, val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb3.isSelected()) {
										final ChangeSolarCellEfficiencyForAllCommand c = new ChangeSolarCellEfficiencyForAllCommand();
										Scene.getInstance().setSolarCellEfficiencyForAll(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									updateAfterEdit();
									break;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});

			final JMenuItem miInverterEff = new JMenuItem("Micro Inverter Efficiency...");
			miInverterEff.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final SolarPanel solarPanel = (SolarPanel) selectedPart;
					final String title = "<html>Micro Inverter Efficiency (%) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The efficiency of a micro inverter for converting electricity from DC to AC is typically 95%.<hr></html>";
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels of this Building");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					final Object[] params = { title, footnote, panel };
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), params, solarPanel.getInverterEfficiency() * 100);
						if (newValue == null) {
							break;
						} else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val < 80 || val >= 100) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Micro inverter efficiency must be greater than 80% and less than 100%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final ChangeMicroInverterEfficiencyCommand c = new ChangeMicroInverterEfficiencyCommand(solarPanel);
										solarPanel.setInverterEfficiency(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb2.isSelected()) {
										final Foundation foundation = solarPanel.getTopContainer();
										final ChangeFoundationMicroInverterEfficiencyCommand c = new ChangeFoundationMicroInverterEfficiencyCommand(foundation);
										Scene.getInstance().setSolarPanelInverterEfficiencyOnFoundation(foundation, val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb3.isSelected()) {
										final ChangeMicroInverterEfficiencyForAllCommand c = new ChangeMicroInverterEfficiencyForAllCommand();
										Scene.getInstance().setSolarPanelInverterEfficiencyForAll(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									updateAfterEdit();
									break;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
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

			popupMenuForSolarPanel.addSeparator();
			popupMenuForSolarPanel.add(trackerMenu);
			popupMenuForSolarPanel.addSeparator();
			popupMenuForSolarPanel.add(miRotateAroundNormal);
			popupMenuForSolarPanel.add(miTiltAngle);
			popupMenuForSolarPanel.add(miAzimuth);
			popupMenuForSolarPanel.add(miSize);
			popupMenuForSolarPanel.add(miBaseHeight);
			popupMenuForSolarPanel.addSeparator();
			popupMenuForSolarPanel.add(cbmiDrawSunBeam);
			popupMenuForSolarPanel.addSeparator();
			popupMenuForSolarPanel.add(miEff);
			popupMenuForSolarPanel.add(miInverterEff);
			popupMenuForSolarPanel.add(shadeToleranceMenu);
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

	private static JPopupMenu getPopupMenuForMirror() {

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
					final JRadioButton rb2 = new JRadioButton("All Mirrors on this Platform");
					final JRadioButton rb3 = new JRadioButton("All Mirrors");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					final String title = "<html>Set the ID of the platform of the target tower for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The sunlight reflected by this mirror will focus on the top of the target platform.<hr></html>";
					final Object[] params = { title, footnote, panel };
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), params, m.getHeliostatTarget() == null ? "" : m.getHeliostatTarget().getId());
						if (newValue == null) {
							break;
						} else {
							try {
								final int id = Integer.parseInt(newValue);
								if (id < 0) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "ID cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									final HousePart p = Scene.getInstance().getPart(id);
									if (p instanceof Foundation) {
										final Foundation f = (Foundation) p;
										if (rb1.isSelected()) {
											final ChangeMirrorTargetCommand c = new ChangeMirrorTargetCommand(m);
											m.setHeliostatTarget(f);
											m.draw();
											SceneManager.getInstance().getUndoManager().addEdit(c);
										} else if (rb2.isSelected()) {
											final Foundation foundation = m.getTopContainer();
											final ChangeFoundationMirrorTargetCommand c = new ChangeFoundationMirrorTargetCommand(foundation);
											Scene.getInstance().setTargetForMirrorsOnFoundation(foundation, f);
											SceneManager.getInstance().getUndoManager().addEdit(c);
										} else if (rb3.isSelected()) {
											final ChangeTargetForAllMirrorsCommand c = new ChangeTargetForAllMirrorsCommand();
											Scene.getInstance().setTargetForAllMirrors(f);
											SceneManager.getInstance().getUndoManager().addEdit(c);
										}
										updateAfterEdit();
									} else {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "ID must be that of a foundation.", "Range Error", JOptionPane.ERROR_MESSAGE);
									}
									break;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});

			final JMenuItem miDisableHeliostat = new JMenuItem("Disable Heliostat...");
			miDisableHeliostat.addActionListener(new ActionListener() {
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
					final JRadioButton rb2 = new JRadioButton("All Mirrors on this Platform");
					final JRadioButton rb3 = new JRadioButton("All Mirrors");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					final String title = "<html>Disable heliostat for " + partInfo + "</html>";
					final String footnote = "<html><hr></html>";
					final Object[] params = { title, footnote, panel };
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), params, "Disable heliostat", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					if (rb1.isSelected()) {
						final ChangeMirrorTargetCommand c = new ChangeMirrorTargetCommand(m);
						m.setHeliostatTarget(null);
						m.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
					} else if (rb2.isSelected()) {
						final Foundation foundation = m.getTopContainer();
						final ChangeFoundationMirrorTargetCommand c = new ChangeFoundationMirrorTargetCommand(foundation);
						Scene.getInstance().setTargetForMirrorsOnFoundation(foundation, null);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					} else if (rb3.isSelected()) {
						final ChangeTargetForAllMirrorsCommand c = new ChangeTargetForAllMirrorsCommand();
						Scene.getInstance().setTargetForAllMirrors(null);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
					updateAfterEdit();
				}
			});

			final JMenuItem miZenith = new JMenuItem("Tilt Angle...");
			miZenith.addActionListener(new ActionListener() {
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
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Mirror", true);
					final JRadioButton rb2 = new JRadioButton("All Mirrors on this Platform");
					final JRadioButton rb3 = new JRadioButton("All Mirrors");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					final Object[] params = { title, footnote, panel };
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), params, m.getTiltAngle());
						if (newValue == null) {
							break;
						} else {
							try {
								double val = Double.parseDouble(newValue);
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
									} else if (rb2.isSelected()) {
										final Foundation foundation = m.getTopContainer();
										final ChangeFoundationMirrorTiltAngleCommand c = new ChangeFoundationMirrorTiltAngleCommand(foundation);
										Scene.getInstance().setZenithAngleForMirrorsOfFoundation(foundation, val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb3.isSelected()) {
										final ChangeTiltAngleForAllMirrorsCommand c = new ChangeTiltAngleForAllMirrorsCommand();
										Scene.getInstance().setTiltAngleForAllMirrors(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									m.draw();
									updateAfterEdit();
									break;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});

			final JMenuItem miAzimuth = new JMenuItem("Azimuth...");
			miAzimuth.addActionListener(new ActionListener() {
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
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Mirror", true);
					final JRadioButton rb2 = new JRadioButton("All Mirrors on this Platform");
					final JRadioButton rb3 = new JRadioButton("All Mirrors");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					final Object[] params = { title, footnote, panel };
					while (true) {
						double a = mirror.getRelativeAzimuth() + foundation.getAzimuth();
						if (a > 360) {
							a -= 360;
						}
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), params, a);
						if (newValue == null) {
							break;
						} else {
							try {
								final double val = Double.parseDouble(newValue);
								a = val - foundation.getAzimuth();
								if (a < 0) {
									a += 360;
								}
								if (rb1.isSelected()) {
									final ChangeAzimuthCommand c = new ChangeAzimuthCommand(mirror);
									mirror.setRelativeAzimuth(a);
									mirror.draw();
									SceneManager.getInstance().getUndoManager().addEdit(c);
								} else if (rb2.isSelected()) {
									final ChangeFoundationMirrorAzimuthCommand c = new ChangeFoundationMirrorAzimuthCommand(foundation);
									Scene.getInstance().setAzimuthForMirrorsOnFoundation(foundation, a);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								} else if (rb3.isSelected()) {
									final ChangeAzimuthForAllMirrorsCommand c = new ChangeAzimuthForAllMirrorsCommand();
									Scene.getInstance().setAzimuthForAllMirrors(a);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								mirror.draw();
								updateAfterEdit();
								break;
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
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
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final Mirror m = (Mirror) selectedPart;
					final String partInfo = m.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new GridLayout(2, 2, 5, 5));
					final JLabel widthLabel = new JLabel("Width: ");
					gui.add(widthLabel);
					final JTextField widthField = new JTextField(twoDecimalsFormat.format(m.getMirrorWidth()));
					gui.add(widthField);
					final JLabel heightLabel = new JLabel("Height: ");
					gui.add(heightLabel);
					final JTextField heightField = new JTextField(twoDecimalsFormat.format(m.getMirrorHeight()));
					gui.add(heightField);
					gui.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), gui, "Set Size for " + partInfo, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					final SetMirrorSizeCommand c = new SetMirrorSizeCommand(m);
					try {
						final double w = Double.parseDouble(widthField.getText());
						if (w < 1 || w > 6) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "Width must be between 1 and 6 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
						} else {
							m.setMirrorWidth(w);
						}
					} catch (final NumberFormatException x) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), widthField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
					}
					try {
						final double h = Double.parseDouble(heightField.getText());
						if (h < 1 || h > 6) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "Height must be between 1 and 6 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
						} else {
							m.setMirrorHeight(h);
						}
					} catch (final NumberFormatException x) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), heightField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
					}
					m.draw();
					updateAfterEdit();
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});

			final JMenuItem miBaseHeight = new JMenuItem("Base Height...");
			miBaseHeight.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Mirror m = (Mirror) selectedPart;
					final Foundation foundation = m.getTopContainer();
					final String title = "<html>Base Height of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2></html>";
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
					final Object[] params = { title, footnote, panel };
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), params, m.getBaseHeight() * Scene.getInstance().getAnnotationScale());
						if (newValue == null) {
							break;
						} else {
							try {
								final double val = Double.parseDouble(newValue) / Scene.getInstance().getAnnotationScale();
								if (rb1.isSelected()) {
									final ChangeBaseHeightCommand c = new ChangeBaseHeightCommand(m);
									m.setBaseHeight(val);
									m.draw();
									SceneManager.getInstance().getUndoManager().addEdit(c);
								} else if (rb2.isSelected()) {
									final ChangeFoundationMirrorBaseHeightCommand c = new ChangeFoundationMirrorBaseHeightCommand(foundation);
									Scene.getInstance().setBaseHeightForMirrorsOnFoundation(foundation, val);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								} else if (rb3.isSelected()) {
									final ChangeBaseHeightForAllMirrorsCommand c = new ChangeBaseHeightForAllMirrorsCommand();
									Scene.getInstance().setBaseHeightForAllMirrors(val);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								m.draw();
								updateAfterEdit();
								break;
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});

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
					} else {
						miZenith.setEnabled(false);
						miAzimuth.setEnabled(false);
					}
					Util.selectSilently(cbmiDrawSunBeam, m.getDrawSunBeam());
				}
			});

			final JMenuItem miReflectivity = new JMenuItem("Reflectivity...");
			miReflectivity.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Mirror m = (Mirror) selectedPart;
					final String title = "<html>Reflectivity (%) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2><hr></html>";
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Mirror", true);
					final JRadioButton rb2 = new JRadioButton("All Mirrors on this Platform");
					final JRadioButton rb3 = new JRadioButton("All Mirrors");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					final Object[] params = { title, footnote, panel };
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), params, m.getReflectivity() * 100);
						if (newValue == null) {
							break;
						} else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val < 50 || val > 99) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Mirror reflectivity must be between 50% and 99%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final ChangeMirrorReflectivityCommand c = new ChangeMirrorReflectivityCommand(m);
										m.setReflectivity(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb2.isSelected()) {
										final Foundation foundation = m.getTopContainer();
										final ChangeFoundationMirrorReflectivityCommand c = new ChangeFoundationMirrorReflectivityCommand(foundation);
										Scene.getInstance().setReflectivityForMirrorsOnFoundation(foundation, val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb3.isSelected()) {
										final ChangeReflectivityForAllMirrorsCommand c = new ChangeReflectivityForAllMirrorsCommand();
										Scene.getInstance().setReflectivityForAllMirrors(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									updateAfterEdit();
									break;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
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
			popupMenuForMirror.addSeparator();
			popupMenuForMirror.add(miSize);
			popupMenuForMirror.add(miBaseHeight);
			popupMenuForMirror.add(miReflectivity);
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

	private static JPopupMenu getPopupMenuForTree() {

		if (popupMenuForTree == null) {

			final JCheckBoxMenuItem miPolygon = new JCheckBoxMenuItem("Show Polygon");
			miPolygon.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final HousePart p = SceneManager.getInstance().getSelectedPart();
					if (p instanceof Tree) {
						((Tree) p).setShowPolygons(miPolygon.isSelected());
					}
					Scene.getInstance().setEdited(true);
				}
			});

			final JCheckBoxMenuItem miLock = new JCheckBoxMenuItem("Lock");
			miLock.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Tree) {
						final Tree tree = (Tree) selectedPart;
						SceneManager.getInstance().getUndoManager().addEdit(new LockPartCommand(tree));
						final boolean lock = miLock.isSelected();
						tree.setFreeze(lock);
						if (lock) {
							SceneManager.getInstance().hideAllEditPoints();
						}
						tree.draw();
						Scene.getInstance().setEdited(true);
					}
				}
			});

			popupMenuForTree = createPopupMenu(true, true, new Runnable() {
				@Override
				public void run() {
					final HousePart p = SceneManager.getInstance().getSelectedPart();
					if (p instanceof Tree) {
						Util.selectSilently(miPolygon, ((Tree) p).getShowPolygons());
						Util.selectSilently(miLock, p.isFrozen());
					}
				}
			});

			popupMenuForTree.addSeparator();
			popupMenuForTree.add(miLock);
			popupMenuForTree.add(miPolygon);

		}

		return popupMenuForTree;

	}

	private static JPopupMenu getPopupMenuForHuman() {

		if (popupMenuForHuman == null) {
			popupMenuForHuman = createPopupMenu(true, true, null);
		}
		return popupMenuForHuman;

	}

	private static JMenuItem createInsulationMenuItem(final boolean useUValue) {
		final JMenuItem mi = new JMenuItem("Insulation...");
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (!(selectedPart instanceof Thermalizable)) {
					return;
				}
				final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
				final Thermalizable t = (Thermalizable) selectedPart;

				final JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				String s;
				if (useUValue) {
					if (selectedPart instanceof Door) {
						s = "<html>U-Value of " + partInfo + "<hr><font size=2>Examples:<br>US 1.20 (uninsulated metal), US 0.60 (insulated metal), US 0.50 (wood)</html>";
					} else {
						s = "<html>U-Value of " + partInfo + "<hr><font size=2>Examples:<br>US 1.30 (single glass), US 0.81 (double glass), US 0.53 (triple glass)</html>";
					}
				} else {
					s = "<html>Insulation Value of " + partInfo + "<hr><font size=2>Examples:<br>US R13 (cellulose, 3.5\"), US R16 (mineral wool, 5.25\"), US R31 (fiberglass, 10\")</html>";
				}
				final JLabel label = new JLabel(s);
				label.setAlignmentX(Component.LEFT_ALIGNMENT);
				panel.add(label);
				panel.add(Box.createVerticalStrut(15));

				final String partName = selectedPart.getClass().getSimpleName();
				final JRadioButton rb1 = new JRadioButton("Only this " + partName, true);
				final JRadioButton rb2 = new JRadioButton("All " + partName + "s of this Building");
				if (selectedPart instanceof Wall || selectedPart instanceof Window) {
					final JPanel scopePanel = new JPanel();
					scopePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					scopePanel.add(rb1);
					scopePanel.add(rb2);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					panel.add(scopePanel);
					panel.add(Box.createVerticalStrut(5));
				}

				final JPanel unitPanel = new JPanel(new GridLayout(2, 3, 5, 5));
				unitPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
				panel.add(unitPanel);

				unitPanel.add(new JLabel("U-Value in SI Unit:"));
				final JTextField siField = new JTextField("" + t.getUValue(), 10);
				unitPanel.add(siField);
				unitPanel.add(new JLabel("<html>W/(m<sup>2</sup>&middot;&deg;C)</html>"));

				if (useUValue) {

					unitPanel.add(new JLabel("U-Value in US Unit:"));
					final JTextField uValueField = new JTextField(twoDecimalsFormat.format(Util.toUsUValue(t.getUValue())), 10);
					uValueField.setAlignmentX(Component.LEFT_ALIGNMENT);
					unitPanel.add(uValueField);
					unitPanel.add(new JLabel("<html>Btu/(h&middot;ft<sup>2</sup>&middot;&deg;F)</html>"));

					siField.getDocument().addDocumentListener(new DocumentListener() {

						private void update() {
							if (!siField.hasFocus()) {
								return;
							}
							final String newValue = siField.getText();
							if ("".equals(newValue)) {
								return;
							}
							try {
								uValueField.setText(twoDecimalsFormat.format(Util.toUsUValue(Double.parseDouble(newValue))));
							} catch (final Exception exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}

						@Override
						public void removeUpdate(final DocumentEvent e) {
							update();
						}

						@Override
						public void insertUpdate(final DocumentEvent e) {
							update();
						}

						@Override
						public void changedUpdate(final DocumentEvent e) {
							update();
						}
					});
					uValueField.getDocument().addDocumentListener(new DocumentListener() {

						private void update() {
							if (!uValueField.hasFocus()) {
								return;
							}
							final String newValue = uValueField.getText();
							if ("".equals(newValue)) {
								return;
							}
							try {
								siField.setText(twoDecimalsFormat.format(1.0 / (Util.toSiRValue(1.0 / Double.parseDouble(newValue)))));
							} catch (final Exception exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}

						@Override
						public void removeUpdate(final DocumentEvent e) {
							update();
						}

						@Override
						public void insertUpdate(final DocumentEvent e) {
							update();
						}

						@Override
						public void changedUpdate(final DocumentEvent e) {
							update();
						}
					});

				} else {

					unitPanel.add(new JLabel("R-Value in US Unit:"));
					final JTextField rValueField = new JTextField(integerFormat.format(Util.toUsRValue(t.getUValue())), 10);
					rValueField.setAlignmentX(Component.LEFT_ALIGNMENT);
					unitPanel.add(rValueField);
					unitPanel.add(new JLabel("<html>h&middot;ft<sup>2</sup>&middot;&deg;F/Btu</html>"));

					siField.getDocument().addDocumentListener(new DocumentListener() {

						private void update() {
							if (!siField.hasFocus()) {
								return;
							}
							final String newValue = siField.getText();
							if ("".equals(newValue)) {
								return;
							}
							try {
								rValueField.setText(integerFormat.format(Util.toUsRValue(Double.parseDouble(newValue))));
							} catch (final Exception exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}

						@Override
						public void removeUpdate(final DocumentEvent e) {
							update();
						}

						@Override
						public void insertUpdate(final DocumentEvent e) {
							update();
						}

						@Override
						public void changedUpdate(final DocumentEvent e) {
							update();
						}
					});
					rValueField.getDocument().addDocumentListener(new DocumentListener() {

						private void update() {
							if (!rValueField.hasFocus()) {
								return;
							}
							final String newValue = rValueField.getText();
							if ("".equals(newValue)) {
								return;
							}
							try {
								siField.setText(twoDecimalsFormat.format(1.0 / Util.toSiRValue(Double.parseDouble(newValue))));
							} catch (final Exception exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}

						@Override
						public void removeUpdate(final DocumentEvent e) {
							update();
						}

						@Override
						public void insertUpdate(final DocumentEvent e) {
							update();
						}

						@Override
						public void changedUpdate(final DocumentEvent e) {
							update();
						}
					});

				}

				while (true) {
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), panel, "Input: " + partInfo, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
						final String newValue = siField.getText();
						try {
							final double val = Double.parseDouble(newValue);
							if (val <= 0) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "U-value must be positive.", "Range Error", JOptionPane.ERROR_MESSAGE);
							} else {
								if (rb1.isSelected()) {
									final ChangePartUValueCommand c = new ChangePartUValueCommand(selectedPart);
									t.setUValue(val);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								} else {
									final ChangeBuildingUValueCommand c = new ChangeBuildingUValueCommand(selectedPart);
									Scene.getInstance().setUValuesOfSameTypeInBuilding(selectedPart, val);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								updateAfterEdit();
								EnergyPanel.getInstance().getConstructionCostGraph().updateBudget();
								break;
							}
						} catch (final NumberFormatException exception) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
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
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (!(selectedPart instanceof Thermalizable)) {
					return;
				}
				final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
				final Thermalizable t = (Thermalizable) selectedPart;
				final String title = "<html>Volumeric Heat Capacity of " + partInfo + " [kWh/(m<sup>3</sup>&middot;&deg;C)]<hr><font size=2>Examples:<br>0.03 (fiberglass), 0.18 (asphalt), 0.25(oak wood), 0.33 (concrete), 0.37 (brick), 0.58 (stone)</html>";
				while (true) {
					// final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), title, t.getVolumetricHeatCapacity());
					final String newValue = (String) JOptionPane.showInputDialog(MainFrame.getInstance(), title, "Input: " + partInfo, JOptionPane.QUESTION_MESSAGE, null, null, t.getVolumetricHeatCapacity());
					if (newValue == null) {
						break;
					} else {
						try {
							final double val = Double.parseDouble(newValue);
							if (val <= 0) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Volumeric heat capacity must be positive.", "Range Error", JOptionPane.ERROR_MESSAGE);
							} else {
								final ChangeVolumetricHeatCapacityCommand c = new ChangeVolumetricHeatCapacityCommand(selectedPart);
								t.setVolumetricHeatCapacity(val);
								updateAfterEdit();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								break;
							}
						} catch (final NumberFormatException exception) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		});
		return mi;
	}

	private static JPopupMenu createPopupMenu(final boolean hasCopyMenu, final boolean pastable, final Runnable runWhenBecomingVisible) {

		final JMenuItem miInfo = new JMenuItem();
		miInfo.setEnabled(false);

		final JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.setInvoker(MainPanel.getInstance().getCanvasPanel());
		popupMenu.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (selectedPart == null) {
					return;
				}
				final String s = selectedPart.toString();
				miInfo.setText(s.substring(0, s.indexOf(')') + 1) + " ($" + Cost.getInstance().getPartCost(selectedPart) + ")");
				if (runWhenBecomingVisible != null) {
					runWhenBecomingVisible.run();
				}
			}

			@Override
			public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(final PopupMenuEvent e) {
			}

		});

		final JMenuItem miCut = new JMenuItem(pastable ? "Cut" : "Delete");
		miCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
		miCut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
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
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart != null) {
						Scene.getInstance().setCopyBuffer(selectedPart.copy(false));
					}
				}
			});
			popupMenu.add(miCopy);
		}

		return popupMenu;

	}

	private static void updateAfterEdit() {
		EnergyPanel.getInstance().updateProperties();
		EnergyPanel.getInstance().clearRadiationHeatMap();
		Scene.getInstance().setEdited(true);
	}

}
