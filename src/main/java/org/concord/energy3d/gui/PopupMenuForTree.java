package org.concord.energy3d.gui;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.undo.ChangePlantCommand;
import org.concord.energy3d.undo.LockEditPointsCommand;
import org.concord.energy3d.util.Util;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.event.ItemEvent;

class PopupMenuForTree extends PopupMenuFactory {

    private static JPopupMenu popupMenuForTree;

    static JPopupMenu getPopupMenu() {

        if (popupMenuForTree == null) {

            final JCheckBoxMenuItem miPolygon = new JCheckBoxMenuItem("Show Polygon");
            miPolygon.addItemListener(e -> {
                final HousePart p = SceneManager.getInstance().getSelectedPart();
                if (p instanceof Tree) {
                    ((Tree) p).setShowPolygons(miPolygon.isSelected());
                }
                Scene.getInstance().setEdited(true);
            });

            final JCheckBoxMenuItem miLock = new JCheckBoxMenuItem("Disable Edit Point");
            miLock.addItemListener(e -> {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (selectedPart instanceof Tree) {
                    final Tree tree = (Tree) selectedPart;
                    SceneManager.getInstance().getUndoManager().addEdit(new LockEditPointsCommand(tree));
                    final boolean lock = miLock.isSelected();
                    SceneManager.getTaskManager().update(() -> {
                        tree.setLockEdit(lock);
                        if (lock) {
                            SceneManager.getInstance().hideAllEditPoints();
                        }
                        tree.draw();
                        return null;
                    });
                    Scene.getInstance().setEdited(true);
                }
            });

            popupMenuForTree = createPopupMenu(true, true, () -> {
                final HousePart p = SceneManager.getInstance().getSelectedPart();
                if (p instanceof Tree) {
                    Util.selectSilently(miPolygon, ((Tree) p).getShowPolygons());
                    Util.selectSilently(miLock, p.getLockEdit());
                }
            });

            popupMenuForTree.addSeparator();
            popupMenuForTree.add(miLock);
            popupMenuForTree.add(miPolygon);
            popupMenuForTree.addSeparator();

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
                    Util.selectSilently(rbmi[((Tree) selectedPart).getPlantType()], true);
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
        rbmi.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                if (!(selectedPart instanceof Tree)) {
                    return;
                }
                final Tree plant = (Tree) selectedPart;
                final ChangePlantCommand c = new ChangePlantCommand(plant);
                SceneManager.getTaskManager().update(() -> {
                    plant.setPlantType(plantType);
                    plant.draw();
                    return null;
                });
                SceneManager.getInstance().getUndoManager().addEdit(c);
                Scene.getInstance().setEdited(true);
                if (MainPanel.getInstance().getEnergyButton().isSelected()) {
                    MainPanel.getInstance().getEnergyButton().setSelected(false);
                }
            }
        });
        return rbmi;
    }

}