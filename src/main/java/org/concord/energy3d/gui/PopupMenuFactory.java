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
import org.concord.energy3d.gui.EnergyPanel.UpdateRadiation;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
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
import org.concord.energy3d.simulation.Cost;
import org.concord.energy3d.simulation.EnergyAnnualAnalysis;
import org.concord.energy3d.simulation.EnergyDailyAnalysis;
import org.concord.energy3d.simulation.SolarAnnualAnalysis;
import org.concord.energy3d.simulation.SolarDailyAnalysis;
import org.concord.energy3d.simulation.UtilityBill;
import org.concord.energy3d.undo.ChangeBackgroundAlbedoCommand;
import org.concord.energy3d.undo.ChangeBuildingColorCommand;
import org.concord.energy3d.undo.ChangeBuildingMicroInverterEfficiencyCommand;
import org.concord.energy3d.undo.ChangeBuildingShutterColorCommand;
import org.concord.energy3d.undo.ChangeBuildingSolarCellEfficiencyCommand;
import org.concord.energy3d.undo.ChangeBuildingUValueCommand;
import org.concord.energy3d.undo.ChangeBuildingWindowShgcCommand;
import org.concord.energy3d.undo.ChangeContainerShutterColorCommand;
import org.concord.energy3d.undo.ChangeContainerWindowColorCommand;
import org.concord.energy3d.undo.ChangeGroundThermalDiffusivityCommand;
import org.concord.energy3d.undo.ChangeMicroInverterEfficiencyForAllCommand;
import org.concord.energy3d.undo.ChangeMicroInverterEfficiencyCommand;
import org.concord.energy3d.undo.ChangePartColorCommand;
import org.concord.energy3d.undo.ChangePartUValueCommand;
import org.concord.energy3d.undo.ChangeVolumetricHeatCapacityCommand;
import org.concord.energy3d.undo.ChangeWallTypeCommand;
import org.concord.energy3d.undo.ChangeContainerWindowShgcCommand;
import org.concord.energy3d.undo.ChangeRoofOverhangCommand;
import org.concord.energy3d.undo.ChangeShutterColorCommand;
import org.concord.energy3d.undo.ChangeShutterLengthCommand;
import org.concord.energy3d.undo.ChangeSolarCellEfficiencyCommand;
import org.concord.energy3d.undo.ChangeSolarCellEfficiencyForAllCommand;
import org.concord.energy3d.undo.ChangeWindowShgcCommand;
import org.concord.energy3d.undo.ChangeWindowShuttersCommand;
import org.concord.energy3d.undo.ChooseSolarPanelSizeCommand;
import org.concord.energy3d.undo.DeleteUtilityBillCommand;
import org.concord.energy3d.undo.LockPartCommand;
import org.concord.energy3d.undo.RotateSolarPanelCommand;
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
	private static JPopupMenu popupMenuForSensor;
	private static JPopupMenu popupMenuForLand;
	private static JPopupMenu popupMenuForSky;

	private static Action colorAction = new AbstractAction("Color...") {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			MainFrame.getInstance().showColorDialogForParts();
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
		if (selectedPart instanceof Floor)
			return getPopupMenuForFloor();
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

	private static void addPrefabMenuItem(final String type, final String url, final JMenu menu) {
		JMenuItem mi = new JMenuItem(type);
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
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
				public void actionPerformed(ActionEvent e) {
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
				public void actionPerformed(ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().removeAllTrees();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
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
				public void actionPerformed(ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().removeAllHumans();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
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
				public void actionPerformed(ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().removeAllFoundations();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
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
				public void actionPerformed(ActionEvent e) {
					MainFrame.getInstance().importFile();
				}
			});

			final JMenu miImportPrefabMenu = new JMenu("Import a Prefab");
			addPrefabMenuItem("Back Hip Roof Porch", "prefabs/back-hip-roof-porch.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Balcony", "prefabs/balcony1.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Bell Tower", "prefabs/bell-tower.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Chimney", "prefabs/chimney.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Connecting Porch", "prefabs/connecting-porch.ng3", miImportPrefabMenu);
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
						if (newValue == null)
							break;
						else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val < 0 || val > 1) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Albedo value must be in 0-1.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									ChangeBackgroundAlbedoCommand c = new ChangeBackgroundAlbedoCommand();
									Scene.getInstance().getGround().setAlbedo(val);
									Scene.getInstance().setEdited(true);
									EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
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
						if (newValue == null)
							break;
						else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val <= 0) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Ground thermal diffusivity must be positive.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									ChangeGroundThermalDiffusivityCommand c = new ChangeGroundThermalDiffusivityCommand();
									Scene.getInstance().getGround().setThermalDiffusivity(val);
									Scene.getInstance().setEdited(true);
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
			popupMenuForLand.add(miRemoveAllTrees);
			popupMenuForLand.add(miRemoveAllHumans);
			popupMenuForLand.add(miRemoveAllBuildings);
			popupMenuForLand.addSeparator();
			popupMenuForLand.add(miImport);
			popupMenuForLand.add(miImportPrefabMenu);
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

			final JCheckBoxMenuItem miHeliodon = new JCheckBoxMenuItem("Heliodon");
			miHeliodon.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					MainPanel.getInstance().getHeliodonButton().doClick();
				}
			});

			popupMenuForSky = new JPopupMenu();
			popupMenuForSky.setInvoker(MainPanel.getInstance().getCanvasPanel());
			popupMenuForSky.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					Util.selectSilently(miHeliodon, MainPanel.getInstance().getHeliodonButton().isSelected());
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				}

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
				}

			});

			popupMenuForSky.add(miInfo);
			popupMenuForSky.addSeparator();
			popupMenuForSky.add(miHeliodon);

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
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null)
						return;
					String s = selectedPart.toString();
					miInfo.setText(s.substring(0, s.indexOf(')') + 1) + " ($" + Cost.getInstance().getPartCost(selectedPart) + ")");
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				}

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
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
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						shutterMenu.setEnabled(selectedPart.getContainer() instanceof Wall);
					}
				}
			});

			final JMenu muntinMenu = new JMenu("Muntins");

			final JCheckBoxMenuItem cbmiHorizontalBars = new JCheckBoxMenuItem("Horizontal Bars");
			cbmiHorizontalBars.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						((Window) selectedPart).setHorizontalBars(cbmiHorizontalBars.isSelected());
						Scene.getInstance().redrawAll();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			muntinMenu.add(cbmiHorizontalBars);

			final JCheckBoxMenuItem cbmiVerticalBars = new JCheckBoxMenuItem("Vertical Bars");
			cbmiVerticalBars.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						((Window) selectedPart).setVerticalBars(cbmiVerticalBars.isSelected());
						Scene.getInstance().redrawAll();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			muntinMenu.add(cbmiVerticalBars);
			muntinMenu.addSeparator();

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

			muntinMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(MenuEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						Window window = (Window) selectedPart;
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
				public void menuDeselected(MenuEvent e) {
					muntinMenu.setEnabled(true);
				}

				@Override
				public void menuCanceled(MenuEvent e) {
					muntinMenu.setEnabled(true);
				}

			});

			final JCheckBoxMenuItem cbmiBothShutters = new JCheckBoxMenuItem("Both Shutters");
			final JCheckBoxMenuItem cbmiLeftShutter = new JCheckBoxMenuItem("Left Shutter");
			final JCheckBoxMenuItem cbmiRightShutter = new JCheckBoxMenuItem("Right Shutter");

			cbmiLeftShutter.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						ChangeWindowShuttersCommand c = new ChangeWindowShuttersCommand((Window) selectedPart);
						((Window) selectedPart).setLeftShutter(cbmiLeftShutter.isSelected());
						Scene.getInstance().redrawAll();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			shutterMenu.add(cbmiLeftShutter);

			cbmiRightShutter.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						ChangeWindowShuttersCommand c = new ChangeWindowShuttersCommand((Window) selectedPart);
						((Window) selectedPart).setRightShutter(cbmiRightShutter.isSelected());
						Scene.getInstance().redrawAll();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			shutterMenu.add(cbmiRightShutter);

			cbmiBothShutters.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						Window window = (Window) selectedPart;
						ChangeWindowShuttersCommand c = new ChangeWindowShuttersCommand(window);
						window.setLeftShutter(cbmiBothShutters.isSelected());
						window.setRightShutter(cbmiBothShutters.isSelected());
						Scene.getInstance().redrawAll();
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
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Window))
						return;
					final Window window = (Window) selectedPart;
					final JColorChooser colorChooser = MainFrame.getInstance().getColorChooser();
					ReadOnlyColorRGBA color = window.getShutterColor();
					if (color != null)
						colorChooser.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
					final ActionListener actionListener = new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							final Color c = colorChooser.getColor();
							final float[] newColor = c.getComponents(null);
							final ColorRGBA color = new ColorRGBA(newColor[0], newColor[1], newColor[2], 1);
							JPanel panel = new JPanel();
							panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
							panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
							final JRadioButton rb1 = new JRadioButton("Only this Window", true);
							final JRadioButton rb2 = new JRadioButton("All Windows on this " + (window.getContainer() instanceof Wall ? "Wall" : "Roof"));
							final JRadioButton rb3 = new JRadioButton("All Windows of this Building");
							panel.add(rb1);
							panel.add(rb2);
							panel.add(rb3);
							ButtonGroup bg = new ButtonGroup();
							bg.add(rb1);
							bg.add(rb2);
							bg.add(rb3);
							if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), panel, "Scope", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION)
								return;
							if (rb1.isSelected()) { // apply to only this window
								ChangeShutterColorCommand cmd = new ChangeShutterColorCommand(window);
								window.setShutterColor(color);
								Scene.getInstance().redrawAll();
								SceneManager.getInstance().getUndoManager().addEdit(cmd);
							} else if (rb2.isSelected()) {
								ChangeContainerShutterColorCommand cmd = new ChangeContainerShutterColorCommand(window.getContainer());
								Scene.getInstance().setWindowColorInContainer(window.getContainer(), color, true);
								SceneManager.getInstance().getUndoManager().addEdit(cmd);
							} else {
								ChangeBuildingShutterColorCommand cmd = new ChangeBuildingShutterColorCommand(window);
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
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Window))
						return;
					final Window window = (Window) selectedPart;
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), "Shutter Length (Relative to Window Width)", window.getShutterLength());
						if (newValue == null)
							break;
						else {
							try {
								double val = Double.parseDouble(newValue);
								if (val <= 0 || val > 1) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Relative shutter length must be within (0, 1).", "Error", JOptionPane.ERROR_MESSAGE);
								} else {
									ChangeShutterLengthCommand c = new ChangeShutterLengthCommand(window);
									window.setShutterLength(val);
									Scene.getInstance().redrawAll();
									Scene.getInstance().setEdited(true);
									EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
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
			shutterMenu.add(miShutterLength);

			shutterMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(MenuEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						Window window = (Window) selectedPart;
						Util.selectSilently(cbmiLeftShutter, window.getLeftShutter());
						Util.selectSilently(cbmiRightShutter, window.getRightShutter());
						Util.selectSilently(cbmiBothShutters, window.getLeftShutter() && window.getRightShutter());
					}
				}

				@Override
				public void menuDeselected(MenuEvent e) {
					shutterMenu.setEnabled(true);
				}

				@Override
				public void menuCanceled(MenuEvent e) {
					shutterMenu.setEnabled(true);
				}

			});

			final JMenuItem miShgc = new JMenuItem("Solar Heat Gain Coefficient...");
			miShgc.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Window))
						return;
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Window window = (Window) selectedPart;
					final String title = "<html>Solar Heat Gain Coefficient of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>Examples:<br><table><tr><td><font size=2>Single glass (clear)</td><td><font size=2>0.66</td></tr><tr><td><font size=2>Single glass (green tint)</td><td><font size=2>0.55</td></tr><tr><td><font size=2>Triple glass (air spaces)</td><td><font size=2>0.39</td></tr></table><hr></html>";
					JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Window", true);
					final JRadioButton rb2 = new JRadioButton("All Windows on this " + (selectedPart.getContainer() instanceof Wall ? "Wall" : "Roof"));
					final JRadioButton rb3 = new JRadioButton("All Windows of this Building");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					Object[] params = { title, footnote, panel };
					while (true) {
						final String newValue = (String) JOptionPane.showInputDialog(MainFrame.getInstance(), params, "Input: " + partInfo, JOptionPane.QUESTION_MESSAGE, null, null, window.getSolarHeatGainCoefficient());
						if (newValue == null)
							break;
						else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val < 0 || val > 1) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar heat gain coefficient must be between 0 and 1.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										ChangeWindowShgcCommand c = new ChangeWindowShgcCommand(window);
										window.setSolarHeatGainCoefficient(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb2.isSelected()) {
										ChangeContainerWindowShgcCommand c = new ChangeContainerWindowShgcCommand(window.getContainer());
										Scene.getInstance().setWindowShgcInContainer(window.getContainer(), val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb3.isSelected()) {
										Foundation foundation = window.getTopContainer();
										ChangeBuildingWindowShgcCommand c = new ChangeBuildingWindowShgcCommand(foundation);
										Scene.getInstance().setWindowShgcOfBuilding(foundation, val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									Scene.getInstance().setEdited(true);
									EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
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
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Window))
						return;
					final Window window = (Window) selectedPart;
					final JColorChooser colorChooser = MainFrame.getInstance().getColorChooser();
					ReadOnlyColorRGBA color = window.getColor();
					if (color != null)
						colorChooser.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
					final ActionListener actionListener = new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							final Color c = colorChooser.getColor();
							final float[] newColor = c.getComponents(null);
							final ColorRGBA color = new ColorRGBA(newColor[0], newColor[1], newColor[2], (float) (1.0 - window.getSolarHeatGainCoefficient()));
							JPanel panel = new JPanel();
							panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
							panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
							final JRadioButton rb1 = new JRadioButton("Only this Window", true);
							final JRadioButton rb2 = new JRadioButton("All Windows on this " + (window.getContainer() instanceof Wall ? "Wall" : "Roof"));
							final JRadioButton rb3 = new JRadioButton("All Windows of this Building");
							panel.add(rb1);
							panel.add(rb2);
							panel.add(rb3);
							ButtonGroup bg = new ButtonGroup();
							bg.add(rb1);
							bg.add(rb2);
							bg.add(rb3);
							if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), panel, "Scope", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION)
								return;
							if (rb1.isSelected()) { // apply to only this window
								ChangePartColorCommand cmd = new ChangePartColorCommand(window);
								window.setColor(color);
								SceneManager.getInstance().getUndoManager().addEdit(cmd);
							} else if (rb2.isSelected()) {
								ChangeContainerWindowColorCommand cmd = new ChangeContainerWindowColorCommand(window.getContainer());
								Scene.getInstance().setWindowColorInContainer(window.getContainer(), color, false);
								SceneManager.getInstance().getUndoManager().addEdit(cmd);
							} else {
								ChangeBuildingColorCommand cmd = new ChangeBuildingColorCommand(window);
								Scene.getInstance().setPartColorOfBuilding(window, color);
								SceneManager.getInstance().getUndoManager().addEdit(cmd);
							}
							Scene.getInstance().setEdited(true);
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
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Window))
						return;
					new EnergyDailyAnalysis().show("Daily Energy for Window");
				}
			});
			popupMenuForWindow.add(mi);

			mi = new JMenuItem("Annual Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Window))
						return;
					new EnergyAnnualAnalysis().show("Annual Energy for Window");
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
				public void actionPerformed(ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().pasteToPickedLocationOnWall();
							return null;
						}
					});
				}
			});

			final JMenuItem miClear = new JMenuItem("Clear");
			miClear.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().removeAllChildren(SceneManager.getInstance().getSelectedPart());
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
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
					HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
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
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						Wall wall = (Wall) selectedPart;
						ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
						wall.setType(Wall.SOLID_WALL);
						Scene.getInstance().redrawAll();
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
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						Wall wall = (Wall) selectedPart;
						ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
						wall.setType(Wall.EMPTY);
						Scene.getInstance().redrawAll();
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
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						Wall wall = (Wall) selectedPart;
						ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
						wall.setType(Wall.VERTICAL_EDGES_ONLY);
						Scene.getInstance().redrawAll();
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
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						Wall wall = (Wall) selectedPart;
						ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
						wall.setType(Wall.COLUMNS_ONLY);
						Scene.getInstance().redrawAll();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			typeMenu.add(rbmiColumns);
			typeGroup.add(rbmiColumns);

			final JRadioButtonMenuItem rbmiRailings = new JRadioButtonMenuItem("Railings");
			rbmiRailings.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						Wall wall = (Wall) selectedPart;
						ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
						wall.setType(Wall.RAILINGS_ONLY);
						Scene.getInstance().redrawAll();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			typeMenu.add(rbmiRailings);
			typeGroup.add(rbmiRailings);

			final JRadioButtonMenuItem rbmiBoard = new JRadioButtonMenuItem("Board");
			rbmiBoard.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						Wall wall = (Wall) selectedPart;
						ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
						wall.setType(Wall.BOARD_ONLY);
						Scene.getInstance().redrawAll();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			// typeMenu.add(rbmiBoard);
			// typeGroup.add(rbmiBoard);

			final JRadioButtonMenuItem rbmiColumnsAndRailings = new JRadioButtonMenuItem("Columns & Railings");
			rbmiColumnsAndRailings.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						Wall wall = (Wall) selectedPart;
						ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
						wall.setType(Wall.COLUMNS_RAILINGS);
						Scene.getInstance().redrawAll();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			typeMenu.add(rbmiColumnsAndRailings);
			typeGroup.add(rbmiColumnsAndRailings);

			typeMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(MenuEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						Wall wall = (Wall) selectedPart;
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
						case Wall.RAILINGS_ONLY:
							Util.selectSilently(rbmiRailings, true);
							break;
						case Wall.COLUMNS_RAILINGS:
							Util.selectSilently(rbmiColumnsAndRailings, true);
							break;
						}
					}
				}

				@Override
				public void menuDeselected(MenuEvent e) {
					typeMenu.setEnabled(true);
				}

				@Override
				public void menuCanceled(MenuEvent e) {
					typeMenu.setEnabled(true);
				}

			});

			JMenuItem mi = new JMenuItem("Daily Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Wall))
						return;
					new EnergyDailyAnalysis().show("Daily Energy for Wall");
				}
			});
			popupMenuForWall.add(mi);

			mi = new JMenuItem("Annual Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Wall))
						return;
					new EnergyAnnualAnalysis().show("Annual Energy for Wall");
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
				public void actionPerformed(ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().pasteToPickedLocationOnRoof();
							return null;
						}
					});
				}
			});

			final JMenuItem miClear = new JMenuItem("Clear");
			miClear.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().removeAllChildren(SceneManager.getInstance().getSelectedPart());
							return null;
						}
					});
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
								double val = Double.parseDouble(newValue);
								double min = Roof.OVERHANG_MIN * Scene.getInstance().getAnnotationScale() * 10;
								if (val < min && val >= 0)
									val = min;
								if (val < 0 || val > 10) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Overhang value must be between " + min + " and 10.", "Error", JOptionPane.ERROR_MESSAGE);
								} else {
									ChangeRoofOverhangCommand c = new ChangeRoofOverhangCommand(roof);
									roof.setOverhangLength(val / Scene.getInstance().getAnnotationScale());
									Scene.getInstance().redrawAll();
									Scene.getInstance().setEdited(true);
									EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
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
					HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
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
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Roof))
						return;
					new EnergyDailyAnalysis().show("Daily Energy for Roof");
				}
			});
			popupMenuForRoof.add(mi);

			mi = new JMenuItem("Annual Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Roof))
						return;
					new EnergyAnnualAnalysis().show("Annual Energy for Roof");
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
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Door))
						return;
					new EnergyDailyAnalysis().show("Daily Energy for Door");
				}
			});
			popupMenuForDoor.add(mi);

			mi = new JMenuItem("Annual Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Door))
						return;
					new EnergyAnnualAnalysis().show("Annual Energy for Door");
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
				public void actionPerformed(ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().pasteToPickedLocationOnFoundation();
							return null;
						}
					});
				}
			});

			final JMenuItem miCopyBuilding = new JMenuItem("Copy Building");
			miCopyBuilding.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						Scene.getInstance().setCopyBuffer(selectedPart);
					}
				}
			});

			final JMenuItem miRescale = new JMenuItem("Rescale...");
			miRescale.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Foundation))
						return;
					new RescaleBuildingDialog((Foundation) selectedPart).setVisible(true);
				}
			});

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
								}
							});
							return null;
						}
					});
				}
			});
			clearMenu.add(miRemoveAllSolarPanels);

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
								}
							});
							return null;
						}
					});
				}
			});
			clearMenu.add(removeAllFloorsMenuItem);

			final JMenuItem miAddUtilityBill = new JMenuItem("Add Utility Bill");
			miAddUtilityBill.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						Foundation f = (Foundation) selectedPart;
						UtilityBill b = f.getUtilityBill();
						if (b == null) {
							if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "No utility bill is found for this building. Create one?", "Utility Bill for Building #" + f.getId(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION)
								return;
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
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						Foundation f = (Foundation) selectedPart;
						if (f.getUtilityBill() == null) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no utilitiy bill associated with this building.", "No Utility Bill", JOptionPane.INFORMATION_MESSAGE);
						} else {
							if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove the utility bill associated with this building?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
								DeleteUtilityBillCommand c = new DeleteUtilityBillCommand(f);
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
				public void itemStateChanged(ItemEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						Foundation foundation = (Foundation) selectedPart;
						SceneManager.getInstance().getUndoManager().addEdit(new LockPartCommand(foundation));
						boolean lock = miLock.isSelected();
						foundation.setFreeze(lock);
						for (final HousePart p : Scene.getInstance().getParts()) {
							if (p.getTopContainer() == foundation)
								p.setFreeze(lock);
						}
						if (lock)
							SceneManager.getInstance().hideAllEditPoints();
						Scene.getInstance().redrawAll();
					}
				}
			});

			final JCheckBoxMenuItem miDisableEdits = new JCheckBoxMenuItem("Disable Edits");
			miDisableEdits.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						((Foundation) selectedPart).setLockEdit(miDisableEdits.isSelected());
						Scene.getInstance().setEdited(true);
					}
				}
			});

			final JMenuItem miThermostat = new JMenuItem("Thermostat...");
			miThermostat.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						Foundation foundation = (Foundation) selectedPart;
						MainPanel.getInstance().getEnergyViewButton().setSelected(false);
						new ThermostatDialog(foundation).setVisible(true);
						TimeSeriesLogger.getInstance().logAdjustThermostatButton();
					}
				}
			});

			popupMenuForFoundation = createPopupMenu(false, true, new Runnable() {
				@Override
				public void run() {
					HousePart p = SceneManager.getInstance().getSelectedPart();
					if (p instanceof Foundation) {
						Foundation f = (Foundation) p;
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
			popupMenuForFoundation.add(miRescale);
			popupMenuForFoundation.add(clearMenu);
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

			JMenuItem mi = new JMenuItem("Daily Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Foundation))
						return;
					final EnergyDailyAnalysis analysis = new EnergyDailyAnalysis();
					if (SceneManager.getInstance().getSolarHeatMap())
						analysis.updateGraph();
					analysis.show("Daily Energy");
				}
			});
			popupMenuForFoundation.add(mi);

			mi = new JMenuItem("Annual Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Foundation))
						return;
					new EnergyAnnualAnalysis().show("Annual Energy");
				}
			});
			popupMenuForFoundation.add(mi);

			mi = new JMenuItem("Daily Solar Panel Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Foundation))
						return;
					Foundation foundation = (Foundation) selectedPart;
					if (foundation.countParts(SolarPanel.class) <= 0) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no solar panel on this building to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					final SolarDailyAnalysis a = new SolarDailyAnalysis();
					if (SceneManager.getInstance().getSolarHeatMap())
						a.updateGraph();
					a.show();
				}
			});
			popupMenuForFoundation.add(mi);

			mi = new JMenuItem("Annual Solar Panel Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Foundation))
						return;
					Foundation foundation = (Foundation) selectedPart;
					if (foundation.countParts(SolarPanel.class) <= 0) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no solar panel on this building to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					SolarAnnualAnalysis a = new SolarAnnualAnalysis();
					if (foundation.getUtilityBill() != null)
						a.setUtilityBill(foundation.getUtilityBill());
					a.show();
				}
			});
			popupMenuForFoundation.add(mi);

		}

		return popupMenuForFoundation;

	}

	private static JPopupMenu getPopupMenuForSensor() {
		if (popupMenuForSensor == null) {
			popupMenuForSensor = createPopupMenu(false, false, null);
		}
		return popupMenuForSensor;
	}

	private static JPopupMenu getPopupMenuForSolarPanel() {

		if (popupMenuForSolarPanel == null) {

			final JCheckBoxMenuItem miRotate = new JCheckBoxMenuItem("Rotate 90\u00B0");
			miRotate.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel))
						return;
					SolarPanel s = (SolarPanel) selectedPart;
					RotateSolarPanelCommand c = new RotateSolarPanelCommand(s);
					s.setRotated(miRotate.isSelected());
					SceneManager.getInstance().getUndoManager().addEdit(c);
					Scene.getInstance().setEdited(true);
					Scene.getInstance().redrawAll();
				}
			});

			final JMenuItem miTilt = new JMenuItem("Tilt Angle...");
			miTilt.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel))
						return;
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final SolarPanel solarPanel = (SolarPanel) selectedPart;
					final String title = "<html>Tilt Angle (&deg;) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>Optimal titl angle captures most sun.<hr></html>";
					JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels of this Building");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					Object[] params = { title, footnote, panel };
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), params, solarPanel.getTiltAngle());
						if (newValue == null)
							break;
						else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val < -90 || val > 90) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar panel tilt angle must be between 0 and 90 degrees.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										// ChangeSolarCellEfficiencyCommand c = new ChangeSolarCellEfficiencyCommand(solarPanel);
										solarPanel.setTiltAngle(val);
										// SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb2.isSelected()) {
										// Foundation foundation = solarPanel.getTopContainer();
										// ChangeBuildingSolarCellEfficiencyCommand c = new ChangeBuildingSolarCellEfficiencyCommand(foundation);
										// Scene.getInstance().setSolarCellEfficiencyOfBuilding(foundation, val * 0.01);
										// SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb3.isSelected()) {
										// ChangeSolarCellEfficiencyForAllCommand c = new ChangeSolarCellEfficiencyForAllCommand();
										// Scene.getInstance().setSolarCellEfficiencyForAll(val * 0.01);
										// SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
									Scene.getInstance().setEdited(true);
									Scene.getInstance().redrawAll();
									break;
								}
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
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel))
						return;
					final SolarPanel s = (SolarPanel) selectedPart;
					final String partInfo = s.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					JPanel gui = new JPanel(new BorderLayout(5, 5));
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
						public void itemStateChanged(ItemEvent e) {
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
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), gui, "Set Size", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.CANCEL_OPTION)
						return;
					ChooseSolarPanelSizeCommand c = new ChooseSolarPanelSizeCommand(s);
					s.setPanelWidth(w);
					s.setPanelHeight(h);
					Scene.getInstance().setEdited(true);
					EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					Scene.getInstance().redrawAll();
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});

			popupMenuForSolarPanel = createPopupMenu(true, true, new Runnable() {
				@Override
				public void run() {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel))
						return;
					Util.selectSilently(miRotate, ((SolarPanel) selectedPart).isRotated());
				}
			});

			final JMenuItem miEff = new JMenuItem("Solar Cell Efficiency...");
			miEff.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel))
						return;
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final SolarPanel solarPanel = (SolarPanel) selectedPart;
					final String title = "<html>Solar Cell Efficiency (%) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>How efficient can a solar panel be?<br>The Shockley-Queisser limit is 34% and the theoretical limit for multilayer cells is 86%.<br>As of 2016, the best solar panel in the market has an efficiency of 22%.<br>So the highest efficiency you can choose is limited to 25%.<hr></html>";
					JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels of this Building");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					Object[] params = { title, footnote, panel };
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), params, solarPanel.getCellEfficiency() * 100);
						if (newValue == null)
							break;
						else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val < 10 || val > 25) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar cell efficiency must be between 10% and 25%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										ChangeSolarCellEfficiencyCommand c = new ChangeSolarCellEfficiencyCommand(solarPanel);
										solarPanel.setCellEfficiency(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb2.isSelected()) {
										Foundation foundation = solarPanel.getTopContainer();
										ChangeBuildingSolarCellEfficiencyCommand c = new ChangeBuildingSolarCellEfficiencyCommand(foundation);
										Scene.getInstance().setSolarCellEfficiencyOfBuilding(foundation, val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb3.isSelected()) {
										ChangeSolarCellEfficiencyForAllCommand c = new ChangeSolarCellEfficiencyForAllCommand();
										Scene.getInstance().setSolarCellEfficiencyForAll(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									Scene.getInstance().setEdited(true);
									EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
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
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel))
						return;
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final SolarPanel solarPanel = (SolarPanel) selectedPart;
					final String title = "<html>Micro Inverter Efficiency (%) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The efficiency of a micro inverter for converting electricity from DC to AC is typically 95%.<hr></html>";
					JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels of this Building");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					Object[] params = { title, footnote, panel };
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), params, solarPanel.getInverterEfficiency() * 100);
						if (newValue == null)
							break;
						else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val < 80 || val >= 100) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Micro inverter efficiency must be greater than 80% and less than 100%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										ChangeMicroInverterEfficiencyCommand c = new ChangeMicroInverterEfficiencyCommand(solarPanel);
										solarPanel.setInverterEfficiency(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb2.isSelected()) {
										Foundation foundation = solarPanel.getTopContainer();
										ChangeBuildingMicroInverterEfficiencyCommand c = new ChangeBuildingMicroInverterEfficiencyCommand(foundation);
										Scene.getInstance().setSolarPanelInverterEfficiencyOfBuilding(foundation, val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb3.isSelected()) {
										ChangeMicroInverterEfficiencyForAllCommand c = new ChangeMicroInverterEfficiencyForAllCommand();
										Scene.getInstance().setSolarPanelInverterEfficiencyForAll(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									Scene.getInstance().setEdited(true);
									EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
									break;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});

			popupMenuForSolarPanel.addSeparator();
			popupMenuForSolarPanel.add(miRotate);
			popupMenuForSolarPanel.add(miTilt);
			popupMenuForSolarPanel.add(miSize);
			popupMenuForSolarPanel.add(miEff);
			popupMenuForSolarPanel.add(miInverterEff);
			popupMenuForSolarPanel.addSeparator();

			JMenuItem mi = new JMenuItem("Daily Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel))
						return;
					new SolarDailyAnalysis().show();
				}
			});
			popupMenuForSolarPanel.add(mi);

			mi = new JMenuItem("Annual Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel))
						return;
					new SolarAnnualAnalysis().show();
				}
			});
			popupMenuForSolarPanel.add(mi);

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

			final JCheckBoxMenuItem miLock = new JCheckBoxMenuItem("Lock");
			miLock.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Tree) {
						Tree tree = (Tree) selectedPart;
						SceneManager.getInstance().getUndoManager().addEdit(new LockPartCommand(tree));
						boolean lock = miLock.isSelected();
						tree.setFreeze(lock);
						if (lock)
							SceneManager.getInstance().hideAllEditPoints();
						Scene.getInstance().redrawAll();
					}
				}
			});

			popupMenuForTree = createPopupMenu(true, true, new Runnable() {
				@Override
				public void run() {
					HousePart p = SceneManager.getInstance().getSelectedPart();
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
				HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (!(selectedPart instanceof Thermalizable))
					return;
				final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
				final Thermalizable t = (Thermalizable) selectedPart;

				JPanel panel = new JPanel();
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
				JLabel label = new JLabel(s);
				label.setAlignmentX(Component.LEFT_ALIGNMENT);
				panel.add(label);
				panel.add(Box.createVerticalStrut(15));

				final String partName = selectedPart.getClass().getSimpleName();
				final JRadioButton rb1 = new JRadioButton("Only this " + partName, true);
				final JRadioButton rb2 = new JRadioButton("All " + partName + "s of this Building");
				if (selectedPart instanceof Wall || selectedPart instanceof Window) {
					JPanel scopePanel = new JPanel();
					scopePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					scopePanel.add(rb1);
					scopePanel.add(rb2);
					ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					panel.add(scopePanel);
					panel.add(Box.createVerticalStrut(5));
				}

				JPanel unitPanel = new JPanel(new GridLayout(2, 3, 5, 5));
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
							if (!siField.hasFocus())
								return;
							String newValue = siField.getText();
							if ("".equals(newValue))
								return;
							try {
								uValueField.setText(twoDecimalsFormat.format(Util.toUsUValue(Double.parseDouble(newValue))));
							} catch (Exception exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
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
								siField.setText(twoDecimalsFormat.format(1.0 / (Util.toSiRValue(1.0 / Double.parseDouble(newValue)))));
							} catch (Exception exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
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
					final JTextField rValueField = new JTextField(integerFormat.format(Util.toUsRValue(t.getUValue())), 10);
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
								rValueField.setText(integerFormat.format(Util.toUsRValue(Double.parseDouble(newValue))));
							} catch (Exception exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
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
								siField.setText(twoDecimalsFormat.format(1.0 / Util.toSiRValue(Double.parseDouble(newValue))));
							} catch (Exception exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
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

				while (true)

				{
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), panel, "Input: " + partInfo, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
						String newValue = siField.getText();
						try {
							final double val = Double.parseDouble(newValue);
							if (val <= 0) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "U-value must be positive.", "Range Error", JOptionPane.ERROR_MESSAGE);
							} else {
								if (rb1.isSelected()) {
									ChangePartUValueCommand c = new ChangePartUValueCommand(selectedPart);
									t.setUValue(val);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								} else {
									ChangeBuildingUValueCommand c = new ChangeBuildingUValueCommand(selectedPart);
									Scene.getInstance().setUValuesOfSameTypeInBuilding(selectedPart, val);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								Scene.getInstance().setEdited(true);
								EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
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
				HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (!(selectedPart instanceof Thermalizable))
					return;
				final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
				final Thermalizable t = (Thermalizable) selectedPart;
				final String title = "<html>Volumeric Heat Capacity of " + partInfo + " [kWh/(m<sup>3</sup>&middot;&deg;C)]<hr><font size=2>Examples:<br>0.03 (fiberglass), 0.18 (asphalt), 0.25(oak wood), 0.33 (concrete), 0.37 (brick), 0.58 (stone)</html>";
				while (true) {
					// final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), title, t.getVolumetricHeatCapacity());
					final String newValue = (String) JOptionPane.showInputDialog(MainFrame.getInstance(), title, "Input: " + partInfo, JOptionPane.QUESTION_MESSAGE, null, null, t.getVolumetricHeatCapacity());
					if (newValue == null)
						break;
					else {
						try {
							final double val = Double.parseDouble(newValue);
							if (val <= 0) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Volumeric heat capacity must be positive.", "Range Error", JOptionPane.ERROR_MESSAGE);
							} else {
								ChangeVolumetricHeatCapacityCommand c = new ChangeVolumetricHeatCapacityCommand(selectedPart);
								t.setVolumetricHeatCapacity(val);
								Scene.getInstance().setEdited(true);
								EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
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

	private static JPopupMenu createPopupMenu(boolean hasCopyMenu, boolean pastable, final Runnable runWhenBecomingVisible) {

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
				miInfo.setText(s.substring(0, s.indexOf(')') + 1) + " ($" + Cost.getInstance().getPartCost(selectedPart) + ")");
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

		final JMenuItem miCut = new JMenuItem(pastable ? "Cut" : "Delete");
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
