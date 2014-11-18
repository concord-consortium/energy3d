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
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.Operation;
import org.concord.energy3d.simulation.HeatLoad;
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
		return null;
	}

	private static JPopupMenu getPopupMenuForWindow() {

		if (popupMenuForWindow == null) {

			popupMenuForWindow = new JPopupMenu();
			popupMenuForWindow.setInvoker(MainPanel.getInstance().getCanvasPanel());

			final JMenuItem miInfo = new JMenuItem();
			miInfo.setEnabled(false);

			final JMenu styleMenu = new JMenu("Style");

			ButtonGroup styleButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem miSmallPanes = new JRadioButtonMenuItem("Small Panes");
			miSmallPanes.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						((Window) selectedPart).setStyle(Window.SMALL_PANES);
						Scene.getInstance().redrawAll();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			styleButtonGroup.add(miSmallPanes);
			styleMenu.add(miSmallPanes);

			final JRadioButtonMenuItem miMediumPanes = new JRadioButtonMenuItem("Medium Panes");
			miMediumPanes.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						((Window) selectedPart).setStyle(Window.MEDIUM_PANES);
						Scene.getInstance().redrawAll();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			styleButtonGroup.add(miMediumPanes);
			styleMenu.add(miMediumPanes);

			final JRadioButtonMenuItem miLargePanes = new JRadioButtonMenuItem("Large Panes");
			miLargePanes.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						((Window) selectedPart).setStyle(Window.LARGE_PANES);
						Scene.getInstance().redrawAll();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			styleButtonGroup.add(miLargePanes);
			styleMenu.add(miLargePanes);

			final JRadioButtonMenuItem miEmpty = new JRadioButtonMenuItem("Empty");
			miEmpty.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						((Window) selectedPart).setStyle(Window.EMPTY);
						Scene.getInstance().redrawAll();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			styleButtonGroup.add(miEmpty);
			styleMenu.add(miEmpty);

			styleMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(MenuEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						switch (((Window) selectedPart).getStyle()) {
						case Window.SMALL_PANES:
							miSmallPanes.setSelected(true);
							break;
						case Window.MEDIUM_PANES:
							miMediumPanes.setSelected(true);
							break;
						case Window.LARGE_PANES:
							miLargePanes.setSelected(true);
							break;
						case Window.EMPTY:
							miEmpty.setSelected(true);
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

			final JRadioButtonMenuItem miUFactor1 = new JRadioButtonMenuItem("1.20 (single pane)");
			miUFactor1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						selectedPart.setUFactor(1.2);
						Scene.getInstance().setEdited(true);
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
				}
			});
			uFactorButtonGroup.add(miUFactor1);
			uFactorMenu.add(miUFactor1);

			final JRadioButtonMenuItem miUFactor2 = new JRadioButtonMenuItem("0.55 (double pane)");
			miUFactor2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						selectedPart.setUFactor(0.55);
						Scene.getInstance().setEdited(true);
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
				}
			});
			uFactorButtonGroup.add(miUFactor2);
			uFactorMenu.add(miUFactor2);

			final JRadioButtonMenuItem miUFactor3 = new JRadioButtonMenuItem("0.35 (double pane, low-e)");
			miUFactor3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						selectedPart.setUFactor(0.35);
						Scene.getInstance().setEdited(true);
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
				}
			});
			uFactorButtonGroup.add(miUFactor3);
			uFactorMenu.add(miUFactor3);

			final JRadioButtonMenuItem miUFactor4 = new JRadioButtonMenuItem("0.15 (triple pane)");
			miUFactor4.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						selectedPart.setUFactor(0.15);
						Scene.getInstance().setEdited(true);
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
				}
			});
			uFactorButtonGroup.add(miUFactor4);
			uFactorMenu.add(miUFactor4);

			final JRadioButtonMenuItem miUFactor5 = new JRadioButtonMenuItem();
			uFactorButtonGroup.add(miUFactor5);

			uFactorMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(MenuEvent e) {
					boolean b = false;
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						if (Util.isZero(selectedPart.getUFactor() - 1.2)) {
							Util.selectSilently(miUFactor1, true);
							b = true;
						} else if (Util.isZero(selectedPart.getUFactor() - 0.55)) {
							Util.selectSilently(miUFactor2, true);
							b = true;
						} else if (Util.isZero(selectedPart.getUFactor() - 0.35)) {
							Util.selectSilently(miUFactor3, true);
							b = true;
						} else if (Util.isZero(selectedPart.getUFactor() - 0.15)) {
							Util.selectSilently(miUFactor4, true);
							b = true;
						} else {
							if (Util.isZero(selectedPart.getUFactor())) {
								double defaultWindowUFactor = HeatLoad.parseUFactor(EnergyPanel.getInstance().getWindowsComboBox());
								if (Util.isZero(defaultWindowUFactor - 1.2)) {
									Util.selectSilently(miUFactor1, true);
									b = true;
								} else if (Util.isZero(defaultWindowUFactor - 0.55)) {
									Util.selectSilently(miUFactor2, true);
									b = true;
								} else if (Util.isZero(defaultWindowUFactor - 0.35)) {
									Util.selectSilently(miUFactor3, true);
									b = true;
								} else if (Util.isZero(defaultWindowUFactor - 0.15)) {
									Util.selectSilently(miUFactor4, true);
									b = true;
								}
							}
						}
						if (!b)
							miUFactor5.setSelected(true);
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

			final JRadioButtonMenuItem miShgc1 = new JRadioButtonMenuItem("25%");
			miShgc1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						((Window) selectedPart).setSolarHeatGainCoefficient(25);
						Scene.getInstance().setEdited(true);
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
				}
			});
			shgcButtonGroup.add(miShgc1);
			shgcMenu.add(miShgc1);

			final JRadioButtonMenuItem miShgc2 = new JRadioButtonMenuItem("50%");
			miShgc2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						((Window) selectedPart).setSolarHeatGainCoefficient(50);
						Scene.getInstance().setEdited(true);
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
				}
			});
			shgcButtonGroup.add(miShgc2);
			shgcMenu.add(miShgc2);

			final JRadioButtonMenuItem miShgc3 = new JRadioButtonMenuItem("80%");
			miShgc3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						((Window) selectedPart).setSolarHeatGainCoefficient(80);
						Scene.getInstance().setEdited(true);
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
				}
			});
			shgcButtonGroup.add(miShgc3);
			shgcMenu.add(miShgc3);

			final JRadioButtonMenuItem miShgc4 = new JRadioButtonMenuItem();
			shgcButtonGroup.add(miShgc4);

			shgcMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(MenuEvent e) {
					boolean b = false;
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						Window window = (Window) selectedPart;
						if (Util.isZero(window.getSolarHeatGainCoefficientNotPercentage() - 0.25)) {
							Util.selectSilently(miShgc1, true);
							b = true;
						} else if (Util.isZero(window.getSolarHeatGainCoefficientNotPercentage() - 0.5)) {
							Util.selectSilently(miShgc2, true);
							b = true;
						} else if (Util.isZero(window.getSolarHeatGainCoefficientNotPercentage() - 0.8)) {
							Util.selectSilently(miShgc3, true);
							b = true;
						}
						if (!b)
							miShgc4.setSelected(true);
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
					styleMenu.setEnabled(selectedPart instanceof Window);
				}

				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				}

				@Override
				public void popupMenuCanceled(PopupMenuEvent e) {
				}

			});

			popupMenuForWindow.add(miInfo);
			popupMenuForWindow.add(styleMenu);
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
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						MainFrame.getInstance().showColorDialogForIndividualPart(Operation.DRAW_WALL);
					}
				}
			});

			final JMenu uFactorMenu = new JMenu("U-Factor");

			ButtonGroup uFactorButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem miUFactor1 = new JRadioButtonMenuItem("0.25 (masonry)");
			miUFactor1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						selectedPart.setUFactor(0.25);
						Scene.getInstance().setEdited(true);
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
				}
			});
			uFactorButtonGroup.add(miUFactor1);
			uFactorMenu.add(miUFactor1);

			final JRadioButtonMenuItem miUFactor2 = new JRadioButtonMenuItem("0.22 (wood frame)");
			miUFactor2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						selectedPart.setUFactor(0.22);
						Scene.getInstance().setEdited(true);
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
				}
			});
			uFactorButtonGroup.add(miUFactor2);
			uFactorMenu.add(miUFactor2);

			final JRadioButtonMenuItem miUFactor3 = new JRadioButtonMenuItem("0.08 (R13, 2x4 w/cellulose/fiberglass)");
			miUFactor3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						selectedPart.setUFactor(0.08);
						Scene.getInstance().setEdited(true);
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
				}
			});
			uFactorButtonGroup.add(miUFactor3);
			uFactorMenu.add(miUFactor3);

			final JRadioButtonMenuItem miUFactor4 = new JRadioButtonMenuItem("0.06 (R18, 2x4 w/cellulose/fiberglass & 1\" rigid foam exterior)");
			miUFactor4.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						selectedPart.setUFactor(0.06);
						Scene.getInstance().setEdited(true);
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
				}
			});
			uFactorButtonGroup.add(miUFactor4);
			uFactorMenu.add(miUFactor4);

			final JRadioButtonMenuItem miUFactor5 = new JRadioButtonMenuItem("0.05 (R20, 2x6 w/cellulose/fiberglass)");
			miUFactor5.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						selectedPart.setUFactor(0.05);
						Scene.getInstance().setEdited(true);
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
				}
			});
			uFactorButtonGroup.add(miUFactor5);
			uFactorMenu.add(miUFactor5);

			final JRadioButtonMenuItem miUFactor6 = new JRadioButtonMenuItem("0.04 (R25, 2x6 w/cellulose/fiberglass & 1\" rigid foam exterior)");
			miUFactor6.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						selectedPart.setUFactor(0.04);
						Scene.getInstance().setEdited(true);
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
				}
			});
			uFactorButtonGroup.add(miUFactor6);
			uFactorMenu.add(miUFactor6);

			final JRadioButtonMenuItem miUFactor7 = new JRadioButtonMenuItem();
			uFactorButtonGroup.add(miUFactor7);

			uFactorMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(MenuEvent e) {
					boolean b = false;
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						if (Util.isZero(selectedPart.getUFactor() - 0.25)) {
							Util.selectSilently(miUFactor1, true);
							b = true;
						} else if (Util.isZero(selectedPart.getUFactor() - 0.22)) {
							Util.selectSilently(miUFactor2, true);
							b = true;
						} else if (Util.isZero(selectedPart.getUFactor() - 0.08)) {
							Util.selectSilently(miUFactor3, true);
							b = true;
						} else if (Util.isZero(selectedPart.getUFactor() - 0.06)) {
							Util.selectSilently(miUFactor4, true);
							b = true;
						} else if (Util.isZero(selectedPart.getUFactor() - 0.05)) {
							Util.selectSilently(miUFactor5, true);
							b = true;
						} else if (Util.isZero(selectedPart.getUFactor() - 0.04)) {
							Util.selectSilently(miUFactor6, true);
							b = true;
						} else {
							if (Util.isZero(selectedPart.getUFactor())) {
								double defaultWallUFactor = HeatLoad.parseUFactor(EnergyPanel.getInstance().getWallsComboBox());
								if (Util.isZero(defaultWallUFactor - 0.25)) {
									Util.selectSilently(miUFactor1, true);
									b = true;
								} else if (Util.isZero(defaultWallUFactor - 0.22)) {
									Util.selectSilently(miUFactor2, true);
									b = true;
								} else if (Util.isZero(defaultWallUFactor - 0.08)) {
									Util.selectSilently(miUFactor3, true);
									b = true;
								} else if (Util.isZero(defaultWallUFactor - 0.06)) {
									Util.selectSilently(miUFactor4, true);
									b = true;
								} else if (Util.isZero(defaultWallUFactor - 0.05)) {
									Util.selectSilently(miUFactor5, true);
									b = true;
								} else if (Util.isZero(defaultWallUFactor - 0.04)) {
									Util.selectSilently(miUFactor6, true);
									b = true;
								}
							}
						}
						if (!b)
							miUFactor7.setSelected(true);
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
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Roof) {
						MainFrame.getInstance().showColorDialogForIndividualPart(Operation.DRAW_ROOF_PYRAMID);
					}
				}
			});

			final JMenu uFactorMenu = new JMenu("U-Factor");

			ButtonGroup uFactorButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem miUFactor1 = new JRadioButtonMenuItem("0.29 (old house)");
			miUFactor1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Roof) {
						selectedPart.setUFactor(0.29);
						Scene.getInstance().setEdited(true);
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
				}
			});
			uFactorButtonGroup.add(miUFactor1);
			uFactorMenu.add(miUFactor1);

			final JRadioButtonMenuItem miUFactor2 = new JRadioButtonMenuItem("0.05 (R22, cellulose/fiberglass)");
			miUFactor2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Roof) {
						selectedPart.setUFactor(0.05);
						Scene.getInstance().setEdited(true);
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
				}
			});
			uFactorButtonGroup.add(miUFactor2);
			uFactorMenu.add(miUFactor2);

			final JRadioButtonMenuItem miUFactor3 = new JRadioButtonMenuItem("0.03 (R38, cellulose/fiberglass)");
			miUFactor3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Roof) {
						selectedPart.setUFactor(0.03);
						Scene.getInstance().setEdited(true);
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
				}
			});
			uFactorButtonGroup.add(miUFactor3);
			uFactorMenu.add(miUFactor3);

			final JRadioButtonMenuItem miUFactor4 = new JRadioButtonMenuItem("0.02 (R50, cellulose/fiberglass)");
			miUFactor4.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Roof) {
						selectedPart.setUFactor(0.02);
						Scene.getInstance().setEdited(true);
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
				}
			});
			uFactorButtonGroup.add(miUFactor4);
			uFactorMenu.add(miUFactor4);

			final JRadioButtonMenuItem miUFactor5 = new JRadioButtonMenuItem();
			uFactorButtonGroup.add(miUFactor5);

			uFactorMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(MenuEvent e) {
					boolean b = false;
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Roof) {
						if (Util.isZero(selectedPart.getUFactor() - 0.29)) {
							Util.selectSilently(miUFactor1, true);
							b = true;
						} else if (Util.isZero(selectedPart.getUFactor() - 0.05)) {
							Util.selectSilently(miUFactor2, true);
							b = true;
						} else if (Util.isZero(selectedPart.getUFactor() - 0.03)) {
							Util.selectSilently(miUFactor3, true);
							b = true;
						} else if (Util.isZero(selectedPart.getUFactor() - 0.02)) {
							Util.selectSilently(miUFactor4, true);
							b = true;
						} else {
							if (Util.isZero(selectedPart.getUFactor())) {
								double defaultRoofUFactor = HeatLoad.parseUFactor(EnergyPanel.getInstance().getRoofsComboBox());
								if (Util.isZero(defaultRoofUFactor - 0.29)) {
									Util.selectSilently(miUFactor1, true);
									b = true;
								} else if (Util.isZero(defaultRoofUFactor - 0.05)) {
									Util.selectSilently(miUFactor2, true);
									b = true;
								} else if (Util.isZero(defaultRoofUFactor - 0.03)) {
									Util.selectSilently(miUFactor3, true);
									b = true;
								} else if (Util.isZero(defaultRoofUFactor - 0.02)) {
									Util.selectSilently(miUFactor4, true);
									b = true;
								}
							}
						}
						if (!b)
							miUFactor5.setSelected(true);
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
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Door) {
						MainFrame.getInstance().showColorDialogForIndividualPart(Operation.DRAW_DOOR);
					}
				}
			});

			final JMenu uFactorMenu = new JMenu("U-Factor");

			ButtonGroup uFactorButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem miUFactor1 = new JRadioButtonMenuItem("0.50 (wood)");
			miUFactor1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Door) {
						selectedPart.setUFactor(0.5);
						Scene.getInstance().setEdited(true);
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
				}
			});
			uFactorButtonGroup.add(miUFactor1);
			uFactorMenu.add(miUFactor1);

			final JRadioButtonMenuItem miUFactor2 = new JRadioButtonMenuItem("0.35 (insulated)");
			miUFactor2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Door) {
						selectedPart.setUFactor(0.35);
						Scene.getInstance().setEdited(true);
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
				}
			});
			uFactorButtonGroup.add(miUFactor2);
			uFactorMenu.add(miUFactor2);

			final JRadioButtonMenuItem miUFactor3 = new JRadioButtonMenuItem();
			uFactorButtonGroup.add(miUFactor3);

			uFactorMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(MenuEvent e) {
					boolean b = false;
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Door) {
						if (Util.isZero(selectedPart.getUFactor() - 0.5)) {
							Util.selectSilently(miUFactor1, true);
							b = true;
						} else if (Util.isZero(selectedPart.getUFactor() - 0.35)) {
							Util.selectSilently(miUFactor2, true);
							b = true;
						} else {
							if (Util.isZero(selectedPart.getUFactor())) {
								double defaultDoorUFactor = HeatLoad.parseUFactor(EnergyPanel.getInstance().getDoorsComboBox());
								if (Util.isZero(defaultDoorUFactor - 0.5)) {
									Util.selectSilently(miUFactor1, true);
									b = true;
								} else if (Util.isZero(defaultDoorUFactor - 0.35)) {
									Util.selectSilently(miUFactor2, true);
									b = true;
								}
							}
						}
						if (!b)
							miUFactor3.setSelected(true);
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
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						MainFrame.getInstance().showColorDialogForIndividualPart(Operation.DRAW_FOUNDATION);
					}
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

}
