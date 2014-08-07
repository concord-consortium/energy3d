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

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.Operation;

/**
 * @author Charles Xie
 * 
 */

public class PopupMenuFactory {

	private static JPopupMenu popupMenuForWindow;
	private static JPopupMenu popupMenuForWall;

	private PopupMenuFactory() {
	}

	public static JPopupMenu getPopupMenu() {
		HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart instanceof Window)
			return getPopupMenuForWindow();
		if (selectedPart instanceof Wall)
			return getPopupMenuForWall();
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

			styleMenu.add(miEmpty);

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

}
