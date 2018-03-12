package org.concord.energy3d.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.undo.LockEditPointsCommand;
import org.concord.energy3d.util.Util;

class PopupMenuForTree extends PopupMenuFactory {

	private static JPopupMenu popupMenuForTree;

	static JPopupMenu getPopupMenu() {

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
						SceneManager.getInstance().getUndoManager().addEdit(new LockEditPointsCommand(tree));
						final boolean lock = miLock.isSelected();
						tree.setLockEdit(lock);
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
						Util.selectSilently(miLock, p.getLockEdit());
					}
				}
			});

			popupMenuForTree.addSeparator();
			popupMenuForTree.add(miLock);
			popupMenuForTree.add(miPolygon);

		}

		return popupMenuForTree;

	}

}
