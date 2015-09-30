package org.concord.energy3d.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.concord.energy3d.gui.EnergyPanel.UpdateRadiation;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.HeatLoad;
import org.concord.energy3d.undo.ChangePartUFactorCommand;
import org.concord.energy3d.undo.ChangeSolarPanelEfficiencyCommand;
import org.concord.energy3d.undo.ChangeWindowShgcCommand;
import org.concord.energy3d.util.Util;

/**
 * Pop-up menus for customizing individual elements.
 * 
 * @author Charles Xie
 * 
 */

public class PopupMenuFactory {

	private static JPopupMenu popupMenuForWindow;
	private static JPopupMenu popupMenuForWall;
	private static JPopupMenu popupMenuForRoof;
	private static JPopupMenu popupMenuForDoor;
	private static JPopupMenu popupMenuForFoundation;
	private static JPopupMenu popupMenuForSolarPanel;
	private static Action colorAction = new AbstractAction("Color") {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			MainFrame.getInstance().showColorDialogForIndividualPart();
		}
	};

	private PopupMenuFactory() {
	}

	public static JPopupMenu getPopupMenu() {
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
		return null;
	}

	private static JPopupMenu getPopupMenuForWindow() {

		if (popupMenuForWindow == null) {

			popupMenuForWindow = createPopupMenu();

			final JMenuItem miInfo = new JMenuItem();
			miInfo.setEnabled(false);

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

			final JMenu shgcMenu = new JMenu("Solar Heat Gain Coefficient");

			ButtonGroup shgcButtonGroup = new ButtonGroup();

			final int nShgc = EnergyPanel.WINDOW_SHGC_CHOICES.length;
			final int[] shgcValues = new int[nShgc];
			for (int i = 0; i < nShgc; i++)
				shgcValues[i] = Integer.parseInt(EnergyPanel.WINDOW_SHGC_CHOICES[i]);
			final JRadioButtonMenuItem[] miShgc = new JRadioButtonMenuItem[nShgc + 1];

			for (int i = 0; i < nShgc; i++) {
				miShgc[i] = new JRadioButtonMenuItem(shgcValues[i] + "%");
				final int i2 = i;
				miShgc[i].addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Window) {
							Window window = (Window) selectedPart;
							SceneManager.getInstance().getUndoManager().addEdit(new ChangeWindowShgcCommand(window));
							window.setSolarHeatGainCoefficient(shgcValues[i2]);
							Scene.getInstance().setEdited(true);
							EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
						}
					}
				});
				shgcButtonGroup.add(miShgc[i]);
				shgcMenu.add(miShgc[i]);
			}
			miShgc[nShgc] = new JRadioButtonMenuItem();
			shgcButtonGroup.add(miShgc[nShgc]);

			shgcMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(MenuEvent e) {
					boolean b = false;
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						Window window = (Window) selectedPart;
						for (int i = 0; i < nShgc; i++) {
							if (Util.isZero(window.getSolarHeatGainCoefficient() - shgcValues[i])) {
								Util.selectSilently(miShgc[i], true);
								b = true;
								break;
							}
						}
						if (!b)
							miShgc[nShgc].setSelected(true);
					}
				}

				@Override
				public void menuDeselected(MenuEvent e) {
				}

				@Override
				public void menuCanceled(MenuEvent e) {
				}

			});

			popupMenuForWindow.add(miInfo);
			popupMenuForWindow.add(muntinMenu);
			popupMenuForWindow.add(createPropertyMenu("U-Factor", EnergyPanel.U_FACTOR_CHOICES_WINDOW, 0));
			popupMenuForWindow.add(shgcMenu);

		}

		return popupMenuForWindow;

	}

	private static JPopupMenu getPopupMenuForWall() {

		if (popupMenuForWall == null) {
			popupMenuForWall = createPopupMenu();
			final JMenuItem miInfo = new JMenuItem();
			miInfo.setEnabled(false);
			popupMenuForWall.add(miInfo);
			popupMenuForWall.add(colorAction);
			popupMenuForWall.add(createPropertyMenu("U-Factor", EnergyPanel.U_FACTOR_CHOICES_WALL, 0));
			popupMenuForWall.add(createPropertyMenu("Volumetric Heat Capacity", EnergyPanel.VOLUMETRIC_HEAT_CAPACITY_CHOICES_WALL, 1));
		}

		return popupMenuForWall;

	}

	private static JPopupMenu getPopupMenuForRoof() {

		if (popupMenuForRoof == null) {
			popupMenuForRoof = createPopupMenu();
			final JMenuItem miInfo = new JMenuItem();
			miInfo.setEnabled(false);
			popupMenuForRoof.add(miInfo);
			popupMenuForRoof.add(colorAction);
			popupMenuForRoof.add(createPropertyMenu("U-Factor", EnergyPanel.U_FACTOR_CHOICES_ROOF, 0));
			popupMenuForRoof.add(createPropertyMenu("Volumetric Heat Capacity", EnergyPanel.VOLUMETRIC_HEAT_CAPACITY_CHOICES_ROOF, 1));
		}

		return popupMenuForRoof;

	}

	private static JPopupMenu getPopupMenuForDoor() {

		if (popupMenuForDoor == null) {
			popupMenuForDoor = createPopupMenu();
			final JMenuItem miInfo = new JMenuItem();
			miInfo.setEnabled(false);
			popupMenuForDoor.add(miInfo);
			popupMenuForDoor.add(colorAction);
			popupMenuForDoor.add(createPropertyMenu("U-Factor", EnergyPanel.U_FACTOR_CHOICES_DOOR, 0));
		}

		return popupMenuForDoor;

	}

	private static JPopupMenu getPopupMenuForFoundation() {

		if (popupMenuForFoundation == null) {
			popupMenuForFoundation = createPopupMenu();
			final JMenuItem miInfo = new JMenuItem();
			miInfo.setEnabled(false);
			popupMenuForFoundation.add(miInfo);
			popupMenuForFoundation.add(colorAction);
			// floor insulation only for the first floor, so this U-value is associated with the Foundation class, not the Floor class
			popupMenuForFoundation.add(createPropertyMenu("Floor U-Factor", EnergyPanel.U_FACTOR_CHOICES_FLOOR, 0));
		}

		return popupMenuForFoundation;

	}

	private static JPopupMenu getPopupMenuForSolarPanel() {

		if (popupMenuForSolarPanel == null) {

			popupMenuForSolarPanel = createPopupMenu();

			final JMenu efficiencyMenu = new JMenu("Energy Conversion Efficiency");

			ButtonGroup efficiencyButtonGroup = new ButtonGroup();

			final int nEfficiency = EnergyPanel.SOLAR_PANEL_CONVERSION_EFFICIENCY_CHOICES.length;
			final int[] efficiencyValues = new int[nEfficiency];
			for (int i = 0; i < nEfficiency; i++)
				efficiencyValues[i] = Integer.parseInt(EnergyPanel.SOLAR_PANEL_CONVERSION_EFFICIENCY_CHOICES[i]);
			final JRadioButtonMenuItem[] miEfficiency = new JRadioButtonMenuItem[nEfficiency + 1];

			for (int i = 0; i < nEfficiency; i++) {
				miEfficiency[i] = new JRadioButtonMenuItem(efficiencyValues[i] + "%");
				final int i2 = i;
				miEfficiency[i].addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof SolarPanel) {
							SolarPanel sp = (SolarPanel) selectedPart;
							SceneManager.getInstance().getUndoManager().addEdit(new ChangeSolarPanelEfficiencyCommand(sp));
							sp.setEfficiency(efficiencyValues[i2]);
							Scene.getInstance().setEdited(true);
							EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
						}
					}
				});
				efficiencyButtonGroup.add(miEfficiency[i]);
				efficiencyMenu.add(miEfficiency[i]);
			}
			miEfficiency[nEfficiency] = new JRadioButtonMenuItem();
			efficiencyButtonGroup.add(miEfficiency[nEfficiency]);

			efficiencyMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(MenuEvent e) {
					boolean b = false;
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof SolarPanel) {
						SolarPanel sp = (SolarPanel) selectedPart;
						for (int i = 0; i < nEfficiency; i++) {
							if (Util.isZero(sp.getEfficiency() - efficiencyValues[i])) {
								Util.selectSilently(miEfficiency[i], true);
								b = true;
								break;
							}
						}
						if (!b)
							miEfficiency[nEfficiency].setSelected(true);
					}
				}

				@Override
				public void menuDeselected(MenuEvent e) {
				}

				@Override
				public void menuCanceled(MenuEvent e) {
				}

			});

			final JMenuItem miInfo = new JMenuItem();
			miInfo.setEnabled(false);
			popupMenuForSolarPanel.add(miInfo);
			popupMenuForSolarPanel.add(efficiencyMenu);

		}

		return popupMenuForSolarPanel;

	}

	private static JPopupMenu createPopupMenu() {

		final JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.setInvoker(MainPanel.getInstance().getCanvasPanel());
		popupMenu.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (selectedPart == null)
					return;
				String s = selectedPart.toString();
				((JMenuItem) popupMenu.getComponent(0)).setText(s.substring(0, s.indexOf(')') + 1));
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

		});

		return popupMenu;

	}

	private static JMenu createPropertyMenu(final String title, final String[] items, final int type) {

		final JMenu menu = new JMenu(title);

		ButtonGroup buttonGroup = new ButtonGroup();

		final int itemCount = items.length;
		final JRadioButtonMenuItem[] mi = new JRadioButtonMenuItem[itemCount + 1];
		mi[itemCount] = new JRadioButtonMenuItem();
		buttonGroup.add(mi[itemCount]);

		for (int i = 0; i < itemCount; i++) {
			mi[i] = new JRadioButtonMenuItem(items[i]);
			final int i2 = i;
			mi[i].addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					switch (type) {
					case 0:
						SceneManager.getInstance().getUndoManager().addEdit(new ChangePartUFactorCommand(selectedPart));
						selectedPart.setUFactor(Scene.parsePropertyString(mi[i2].getText()));
						break;
					case 1:
						// SceneManager.getInstance().getUndoManager().addEdit(new ChangePartVolumetricHeatCapacityCommand(selectedPart));
						selectedPart.setVolumetricHeatCapacity(Scene.parsePropertyString(mi[i2].getText()));
						break;
					}
					Scene.getInstance().setEdited(true);
					EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
				}
			});
			buttonGroup.add(mi[i]);
			menu.add(mi[i]);
		}

		menu.addMenuListener(new MenuListener() {

			@Override
			public void menuSelected(MenuEvent e) {
				boolean b = false;
				HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				switch (type) {
				case 0:
					for (int i = 0; i < itemCount; i++) {
						if (Util.isZero(selectedPart.getUFactor() - Scene.parsePropertyString(mi[i].getText()))) {
							Util.selectSilently(mi[i], true);
							b = true;
							break;
						}
					}
					if (!b) {
						if (Util.isZero(selectedPart.getUFactor())) {
							double defaultWallUFactor = HeatLoad.parseValue(EnergyPanel.getInstance().getWallsComboBox());
							for (int i = 0; i < itemCount; i++) {
								if (Util.isZero(defaultWallUFactor - Scene.parsePropertyString(mi[i].getText()))) {
									Util.selectSilently(mi[i], true);
									b = true;
									break;
								}
							}
						}
						if (!b)
							mi[itemCount].setSelected(true);
					}
					break;
				case 1:
					for (int i = 0; i < itemCount; i++) {
						if (Util.isZero(selectedPart.getVolumetricHeatCapacity() - Scene.parsePropertyString(mi[i].getText()))) {
							Util.selectSilently(mi[i], true);
							b = true;
							break;
						}
					}
					if (!b) {
						if (Util.isZero(selectedPart.getVolumetricHeatCapacity())) {
							double defaultWallVolumetricHeatCapacity = 0.25;
							for (int i = 0; i < itemCount; i++) {
								if (Util.isZero(defaultWallVolumetricHeatCapacity - Scene.parsePropertyString(mi[i].getText()))) {
									Util.selectSilently(mi[i], true);
									b = true;
									break;
								}
							}
						}
						if (!b)
							mi[itemCount].setSelected(true);
					}
					break;
				}
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}

		});

		return menu;

	}

}
