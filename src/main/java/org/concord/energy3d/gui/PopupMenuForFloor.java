package org.concord.energy3d.gui;

import java.awt.Color;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.BuildingCost;
import org.concord.energy3d.util.Config;

class PopupMenuForFloor extends PopupMenuFactory {

	private static JPopupMenu popupMenuForFloor;

	static JPopupMenu getPopupMenu() {

		if (popupMenuForFloor == null) {

			final JMenuItem miInfo = new JMenuItem("Floor");
			miInfo.setEnabled(false);
			miInfo.setOpaque(true);
			miInfo.setBackground(Config.isMac() ? Color.BLACK : Color.GRAY);
			miInfo.setForeground(Color.WHITE);

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
					miInfo.setText(s.substring(0, s.indexOf(')') + 1) + " ($" + BuildingCost.getPartCost(selectedPart) + ")");
				}

				@Override
				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				}

				@Override
				public void popupMenuCanceled(final PopupMenuEvent e) {
				}

			});

			popupMenuForFloor.add(miInfo);
			// popupMenuForFloor.addSeparator();
			popupMenuForFloor.add(colorAction);

		}

		return popupMenuForFloor;

	}
}
