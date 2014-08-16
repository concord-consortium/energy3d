package org.concord.energy3d.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

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
			miSmallPanes.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
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
			miMediumPanes.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
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
			miLargePanes.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
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
			miEmpty.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
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
			miUFactor1.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						selectedPart.setUFactor(1.2);
						Scene.getInstance().setEdited(true);
					}
				}
			});
			uFactorButtonGroup.add(miUFactor1);
			uFactorMenu.add(miUFactor1);

			final JRadioButtonMenuItem miUFactor2 = new JRadioButtonMenuItem("0.55 (double pane)");
			miUFactor2.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						selectedPart.setUFactor(0.55);
						Scene.getInstance().setEdited(true);
					}
				}
			});
			uFactorButtonGroup.add(miUFactor2);
			uFactorMenu.add(miUFactor2);

			final JRadioButtonMenuItem miUFactor3 = new JRadioButtonMenuItem("0.35 (double pane, low-e)");
			miUFactor3.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						selectedPart.setUFactor(0.35);
						Scene.getInstance().setEdited(true);
					}
				}
			});
			uFactorButtonGroup.add(miUFactor3);
			uFactorMenu.add(miUFactor3);

			final JRadioButtonMenuItem miUFactor4 = new JRadioButtonMenuItem("0.15 (triple pane)");
			miUFactor4.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						selectedPart.setUFactor(0.15);
						Scene.getInstance().setEdited(true);
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
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						double windowUFactor = HeatLoad.parseUFactor(EnergyPanel.getInstance().getWindowsComboBox());
						if (Util.isZero(selectedPart.getUFactor() - 1.2) || Util.isZero(windowUFactor - 1.2))
							miUFactor1.setSelected(true);
						else if (Util.isZero(selectedPart.getUFactor() - 0.55) || Util.isZero(windowUFactor - 0.55))
							miUFactor2.setSelected(true);
						else if (Util.isZero(selectedPart.getUFactor() - 0.35) || Util.isZero(windowUFactor - 0.35))
							miUFactor3.setSelected(true);
						else if (Util.isZero(selectedPart.getUFactor() - 0.15) || Util.isZero(windowUFactor - 0.15))
							miUFactor4.setSelected(true);
						else
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
						MainFrame.getInstance().showColorDialogForHousePart(Operation.DRAW_WALL);
					}
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
						MainFrame.getInstance().showColorDialogForHousePart(Operation.DRAW_ROOF_PYRAMID);
					}
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
						MainFrame.getInstance().showColorDialogForHousePart(Operation.DRAW_DOOR);
					}
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
						MainFrame.getInstance().showColorDialogForHousePart(Operation.DRAW_FOUNDATION);
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
