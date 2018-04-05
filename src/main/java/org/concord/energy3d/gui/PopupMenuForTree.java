package org.concord.energy3d.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.undo.ChangePlantCommand;
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

			final JMenu treeMenu = new JMenu("Select Tree");
			popupMenuForTree.add(treeMenu);

			final ButtonGroup treeButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem[] rbmi = new JRadioButtonMenuItem[Tree.PLANTS.length];
			for (int i = 0; i < rbmi.length; i++) {
				rbmi[i] = createMenuItem(i);
				treeMenu.add(rbmi[i]);
				treeButtonGroup.add(rbmi[i]);
			}

			treeMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(final MenuEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Tree)) {
						return;
					}
					treeButtonGroup.clearSelection();
					Util.selectSilently(rbmi[((Human) selectedPart).getHumanType()], true);
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
				}

				@Override
				public void menuCanceled(final MenuEvent e) {
				}

			});

		}

		return popupMenuForTree;

	}

	private static JRadioButtonMenuItem createMenuItem(final int plantType) {
		final JRadioButtonMenuItem rbmi = new JRadioButtonMenuItem(new ImageIcon(MainPanel.class.getResource("icons/" + Tree.getPlantName(plantType).toLowerCase() + ".png")));
		rbmi.setText(Tree.getPlantName(plantType));
		rbmi.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Tree)) {
						return;
					}
					final Tree plant = (Tree) selectedPart;
					final ChangePlantCommand c = new ChangePlantCommand(plant);
					plant.setPlantType(plantType);
					plant.draw();
					SceneManager.getInstance().getUndoManager().addEdit(c);
					Scene.getInstance().setEdited(true);
				}
			}
		});
		return rbmi;
	}

}
