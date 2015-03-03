package org.concord.energy3d.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

			popupMenuForWindow = new JPopupMenu();
			popupMenuForWindow.setInvoker(MainPanel.getInstance().getCanvasPanel());

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

			final JMenu uFactorMenu = new JMenu("U-Factor");

			ButtonGroup uFactorButtonGroup = new ButtonGroup();

			final int nUFactor = EnergyPanel.U_FACTOR_CHOICES_WINDOW.length;
			final JRadioButtonMenuItem[] miUFactor = new JRadioButtonMenuItem[nUFactor + 1];
			miUFactor[nUFactor] = new JRadioButtonMenuItem();
			uFactorButtonGroup.add(miUFactor[nUFactor]);

			for (int i = 0; i < nUFactor; i++) {
				miUFactor[i] = new JRadioButtonMenuItem(EnergyPanel.U_FACTOR_CHOICES_WINDOW[i]);
				final int i2 = i;
				miUFactor[i].addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Window) {
							selectedPart.setUFactor(Scene.parsePropertyString(miUFactor[i2].getText()));
							Scene.getInstance().setEdited(true);
							EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
						}
					}
				});
				uFactorButtonGroup.add(miUFactor[i]);
				uFactorMenu.add(miUFactor[i]);
			}

			uFactorMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(MenuEvent e) {
					boolean b = false;
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						for (int i = 0; i < nUFactor; i++) {
							if (Util.isZero(selectedPart.getUFactor() - Scene.parsePropertyString(miUFactor[i].getText()))) {
								Util.selectSilently(miUFactor[i], true);
								b = true;
								break;
							}
						}
						if (!b) {
							if (Util.isZero(selectedPart.getUFactor())) {
								double defaultWindowUFactor = HeatLoad.parseValue(EnergyPanel.getInstance().getWindowsComboBox());
								for (int i = 0; i < nUFactor; i++) {
									if (Util.isZero(defaultWindowUFactor - Scene.parsePropertyString(miUFactor[i].getText()))) {
										Util.selectSilently(miUFactor[i], true);
										b = true;
										break;
									}
								}
							}
							if (!b)
								miUFactor[nUFactor].setSelected(true);
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
							((Window) selectedPart).setSolarHeatGainCoefficient(shgcValues[i2]);
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
							if (Util.isZero(window.getSolarHeatGainCoefficientNotPercentage() - shgcValues[i] * 0.01)) {
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

			popupMenuForWindow.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null)
						return;
					String s = selectedPart.toString();
					miInfo.setText(s.substring(0, s.indexOf(')') + 1));
					muntinMenu.setEnabled(selectedPart instanceof Window);
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				}

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
				}

			});

			popupMenuForWindow.add(miInfo);
			popupMenuForWindow.add(muntinMenu);
			popupMenuForWindow.add(uFactorMenu);
			popupMenuForWindow.add(shgcMenu);

		}

		return popupMenuForWindow;

	}

	private static JPopupMenu getPopupMenuForWall() {

		if (popupMenuForWall == null) {

			popupMenuForWall = new JPopupMenu();
			popupMenuForWall.setInvoker(MainPanel.getInstance().getCanvasPanel());

			final JMenuItem miInfo = new JMenuItem();
			miInfo.setEnabled(false);
			final JMenuItem miColor = new JMenuItem("Color");
			miColor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					MainFrame.getInstance().showColorDialogForIndividualPart();
				}
			});

			final JMenu uFactorMenu = new JMenu("U-Factor");

			ButtonGroup uFactorButtonGroup = new ButtonGroup();

			final int nUFactor = EnergyPanel.U_FACTOR_CHOICES_WALL.length;
			final JRadioButtonMenuItem[] miUFactor = new JRadioButtonMenuItem[nUFactor + 1];
			miUFactor[nUFactor] = new JRadioButtonMenuItem();
			uFactorButtonGroup.add(miUFactor[nUFactor]);

			for (int i = 0; i < nUFactor; i++) {
				miUFactor[i] = new JRadioButtonMenuItem(EnergyPanel.U_FACTOR_CHOICES_WALL[i]);
				final int i2 = i;
				miUFactor[i].addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Wall) {
							selectedPart.setUFactor(Scene.parsePropertyString(miUFactor[i2].getText()));
							SceneManager.getInstance().getUndoManager().addEdit(new ChangePartUFactorCommand(selectedPart));
							Scene.getInstance().setEdited(true);
							EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
						}
					}
				});
				uFactorButtonGroup.add(miUFactor[i]);
				uFactorMenu.add(miUFactor[i]);
			}

			uFactorMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(MenuEvent e) {
					boolean b = false;
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						for (int i = 0; i < nUFactor; i++) {
							if (Util.isZero(selectedPart.getUFactor() - Scene.parsePropertyString(miUFactor[i].getText()))) {
								Util.selectSilently(miUFactor[i], true);
								b = true;
								break;
							}
						}
						if (!b) {
							if (Util.isZero(selectedPart.getUFactor())) {
								double defaultWallUFactor = HeatLoad.parseValue(EnergyPanel.getInstance().getWallsComboBox());
								for (int i = 0; i < nUFactor; i++) {
									if (Util.isZero(defaultWallUFactor - Scene.parsePropertyString(miUFactor[i].getText()))) {
										Util.selectSilently(miUFactor[i], true);
										b = true;
										break;
									}
								}
							}
							if (!b)
								miUFactor[nUFactor].setSelected(true);
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

			popupMenuForWall.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null)
						return;
					String s = selectedPart.toString();
					miInfo.setText(s.substring(0, s.indexOf(')') + 1));
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				}

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
				}

			});

			popupMenuForWall.add(miInfo);
			popupMenuForWall.add(miColor);
			popupMenuForWall.add(uFactorMenu);

		}

		return popupMenuForWall;

	}

	private static JPopupMenu getPopupMenuForRoof() {

		if (popupMenuForRoof == null) {

			popupMenuForRoof = new JPopupMenu();
			popupMenuForRoof.setInvoker(MainPanel.getInstance().getCanvasPanel());

			final JMenuItem miInfo = new JMenuItem();
			miInfo.setEnabled(false);
			final JMenuItem miColor = new JMenuItem("Color");
			miColor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					MainFrame.getInstance().showColorDialogForIndividualPart();
				}
			});

			final JMenu uFactorMenu = new JMenu("U-Factor");

			ButtonGroup uFactorButtonGroup = new ButtonGroup();

			final int nUFactor = EnergyPanel.U_FACTOR_CHOICES_ROOF.length;
			final JRadioButtonMenuItem[] miUFactor = new JRadioButtonMenuItem[nUFactor + 1];
			miUFactor[nUFactor] = new JRadioButtonMenuItem();
			uFactorButtonGroup.add(miUFactor[nUFactor]);

			for (int i = 0; i < nUFactor; i++) {
				miUFactor[i] = new JRadioButtonMenuItem(EnergyPanel.U_FACTOR_CHOICES_ROOF[i]);
				final int i2 = i;
				miUFactor[i].addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Roof) {
							selectedPart.setUFactor(Scene.parsePropertyString(miUFactor[i2].getText()));
							Scene.getInstance().setEdited(true);
							EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
						}
					}
				});
				uFactorButtonGroup.add(miUFactor[i]);
				uFactorMenu.add(miUFactor[i]);
			}

			uFactorMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(MenuEvent e) {
					boolean b = false;
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Roof) {
						for (int i = 0; i < nUFactor; i++) {
							if (Util.isZero(selectedPart.getUFactor() - Scene.parsePropertyString(miUFactor[i].getText()))) {
								Util.selectSilently(miUFactor[i], true);
								b = true;
								break;
							}
						}
						if (!b) {
							if (Util.isZero(selectedPart.getUFactor())) {
								double defaultRoofUFactor = HeatLoad.parseValue(EnergyPanel.getInstance().getRoofsComboBox());
								for (int i = 0; i < nUFactor; i++) {
									if (Util.isZero(defaultRoofUFactor - Scene.parsePropertyString(miUFactor[i].getText()))) {
										Util.selectSilently(miUFactor[i], true);
										b = true;
										break;
									}
								}
							}
							if (!b)
								miUFactor[nUFactor].setSelected(true);
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

			popupMenuForRoof.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null)
						return;
					String s = selectedPart.toString();
					miInfo.setText(s.substring(0, s.indexOf(')') + 1));
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				}

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
				}

			});

			popupMenuForRoof.add(miInfo);
			popupMenuForRoof.add(miColor);
			popupMenuForRoof.add(uFactorMenu);

		}

		return popupMenuForRoof;

	}

	private static JPopupMenu getPopupMenuForDoor() {

		if (popupMenuForDoor == null) {

			popupMenuForDoor = new JPopupMenu();
			popupMenuForDoor.setInvoker(MainPanel.getInstance().getCanvasPanel());

			final JMenuItem miInfo = new JMenuItem();
			miInfo.setEnabled(false);
			final JMenuItem miColor = new JMenuItem("Color");
			miColor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					MainFrame.getInstance().showColorDialogForIndividualPart();
				}
			});

			final JMenu uFactorMenu = new JMenu("U-Factor");

			ButtonGroup uFactorButtonGroup = new ButtonGroup();

			final int nUFactor = EnergyPanel.U_FACTOR_CHOICES_DOOR.length;
			final JRadioButtonMenuItem[] miUFactor = new JRadioButtonMenuItem[nUFactor + 1];
			miUFactor[nUFactor] = new JRadioButtonMenuItem();
			uFactorButtonGroup.add(miUFactor[nUFactor]);

			for (int i = 0; i < nUFactor; i++) {
				miUFactor[i] = new JRadioButtonMenuItem(EnergyPanel.U_FACTOR_CHOICES_DOOR[i]);
				final int i2 = i;
				miUFactor[i].addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Door) {
							selectedPart.setUFactor(Scene.parsePropertyString(miUFactor[i2].getText()));
							Scene.getInstance().setEdited(true);
							EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
						}
					}
				});
				uFactorButtonGroup.add(miUFactor[i]);
				uFactorMenu.add(miUFactor[i]);
			}

			uFactorMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(MenuEvent e) {
					boolean b = false;
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Door) {
						for (int i = 0; i < nUFactor; i++) {
							if (Util.isZero(selectedPart.getUFactor() - Scene.parsePropertyString(miUFactor[i].getText()))) {
								Util.selectSilently(miUFactor[i], true);
								b = true;
								break;
							}
						}
						if (!b) {
							if (Util.isZero(selectedPart.getUFactor())) {
								double defaultDoorUFactor = HeatLoad.parseValue(EnergyPanel.getInstance().getDoorsComboBox());
								for (int i = 0; i < nUFactor; i++) {
									if (Util.isZero(defaultDoorUFactor - Scene.parsePropertyString(miUFactor[i].getText()))) {
										Util.selectSilently(miUFactor[i], true);
										b = true;
										break;
									}
								}
							}
							if (!b)
								miUFactor[nUFactor].setSelected(true);
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

			popupMenuForDoor.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null)
						return;
					String s = selectedPart.toString();
					miInfo.setText(s.substring(0, s.indexOf(')') + 1));
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				}

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
				}

			});

			popupMenuForDoor.add(miInfo);
			popupMenuForDoor.add(miColor);
			popupMenuForDoor.add(uFactorMenu);

		}

		return popupMenuForDoor;

	}

	private static JPopupMenu getPopupMenuForFoundation() {

		if (popupMenuForFoundation == null) {

			popupMenuForFoundation = new JPopupMenu();
			popupMenuForFoundation.setInvoker(MainPanel.getInstance().getCanvasPanel());

			final JMenuItem miInfo = new JMenuItem();
			miInfo.setEnabled(false);
			final JMenuItem miColor = new JMenuItem("Color");
			miColor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					MainFrame.getInstance().showColorDialogForIndividualPart();
				}
			});

			popupMenuForFoundation.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null)
						return;
					String s = selectedPart.toString();
					miInfo.setText(s.substring(0, s.indexOf(')') + 1));
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				}

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
				}

			});

			popupMenuForFoundation.add(miInfo);
			popupMenuForFoundation.add(miColor);

		}

		return popupMenuForFoundation;

	}

	private static JPopupMenu getPopupMenuForSolarPanel() {

		if (popupMenuForSolarPanel == null) {

			popupMenuForSolarPanel = new JPopupMenu();
			popupMenuForSolarPanel.setInvoker(MainPanel.getInstance().getCanvasPanel());

			final JMenuItem miInfo = new JMenuItem();
			miInfo.setEnabled(false);

			popupMenuForSolarPanel.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null)
						return;
					String s = selectedPart.toString();
					miInfo.setText(s.substring(0, s.indexOf(')') + 1));
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				}

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
				}

			});

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
							((SolarPanel) selectedPart).setEfficiency(efficiencyValues[i2]);
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

			popupMenuForSolarPanel.add(miInfo);
			popupMenuForSolarPanel.add(efficiencyMenu);

		}

		return popupMenuForSolarPanel;

	}

}
