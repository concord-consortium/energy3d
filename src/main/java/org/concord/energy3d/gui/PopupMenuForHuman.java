package org.concord.energy3d.gui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.undo.ChangeFigureCommand;
import org.concord.energy3d.util.Util;

class PopupMenuForHuman extends PopupMenuFactory {

	private static JPopupMenu popupMenuForHuman;

	private static JRadioButtonMenuItem createMenuItem(final int humanType) {
		final JRadioButtonMenuItem rbmi = new JRadioButtonMenuItem(new ImageIcon(MainPanel.class.getResource("icons/" + Human.getHumanName(humanType).toLowerCase() + ".png")));
		rbmi.setText(Human.getHumanName(humanType));
		rbmi.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Human)) {
						return;
					}
					final Human human = (Human) selectedPart;
					final ChangeFigureCommand c = new ChangeFigureCommand(human);
					human.setHumanType(humanType);
					human.draw();
					SceneManager.getInstance().getUndoManager().addEdit(c);
					Scene.getInstance().setEdited(true);
				}
			}
		});
		return rbmi;
	}

	static JPopupMenu getPopupMenu() {

		if (popupMenuForHuman == null) {
			popupMenuForHuman = createPopupMenu(true, true, null);
			popupMenuForHuman.addSeparator();

			final JMenu personMenu = new JMenu("Select Person");
			popupMenuForHuman.add(personMenu);

			final ButtonGroup personButtonGroup = new ButtonGroup();

			final JMenu menMenu = new JMenu("Men");
			personMenu.add(menMenu);
			final JMenu womenMenu = new JMenu("Women");
			personMenu.add(womenMenu);

			final JRadioButtonMenuItem[] rbmi = new JRadioButtonMenuItem[Human.FIGURES.length];
			for (int i = 0; i < rbmi.length; i++) {
				rbmi[i] = createMenuItem(i);
				(Human.FIGURES[i].isMale() ? menMenu : womenMenu).add(rbmi[i]);
				personButtonGroup.add(rbmi[i]);
			}

			personMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(final MenuEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Human)) {
						return;
					}
					personButtonGroup.clearSelection();
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

		return popupMenuForHuman;

	}

}
