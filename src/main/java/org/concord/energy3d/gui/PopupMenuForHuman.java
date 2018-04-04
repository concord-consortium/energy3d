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
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

class PopupMenuForHuman extends PopupMenuFactory {

	private static JPopupMenu popupMenuForHuman;

	static JPopupMenu getPopupMenu() {

		if (popupMenuForHuman == null) {
			popupMenuForHuman = createPopupMenu(true, true, null);
			popupMenuForHuman.addSeparator();

			final JMenu personMenu = new JMenu("Select Person");
			popupMenuForHuman.add(personMenu);

			final ButtonGroup personButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem rbmiJack = new JRadioButtonMenuItem(new ImageIcon(MainPanel.class.getResource("icons/jack.png")));
			rbmiJack.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (!(selectedPart instanceof Human)) {
							return;
						}
						final Human human = (Human) selectedPart;
						human.setHumanType(Human.JACK);
						human.draw();
					}
				}
			});
			personButtonGroup.add(rbmiJack);
			personMenu.add(rbmiJack);

			final JRadioButtonMenuItem rbmiJane = new JRadioButtonMenuItem(new ImageIcon(MainPanel.class.getResource("icons/jane.png")));
			rbmiJane.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (!(selectedPart instanceof Human)) {
							return;
						}
						final Human human = (Human) selectedPart;
						human.setHumanType(Human.JANE);
						human.draw();
					}
				}
			});
			personButtonGroup.add(rbmiJane);
			personMenu.add(rbmiJane);

			final JRadioButtonMenuItem rbmiJeni = new JRadioButtonMenuItem(new ImageIcon(MainPanel.class.getResource("icons/jenny.png")));
			rbmiJeni.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (!(selectedPart instanceof Human)) {
							return;
						}
						final Human human = (Human) selectedPart;
						human.setHumanType(Human.JENI);
						human.draw();
					}
				}
			});
			personButtonGroup.add(rbmiJeni);
			personMenu.add(rbmiJeni);

			final JRadioButtonMenuItem rbmiJill = new JRadioButtonMenuItem(new ImageIcon(MainPanel.class.getResource("icons/jill.png")));
			rbmiJill.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (!(selectedPart instanceof Human)) {
							return;
						}
						final Human human = (Human) selectedPart;
						human.setHumanType(Human.JILL);
						human.draw();
					}
				}
			});
			personButtonGroup.add(rbmiJill);
			personMenu.add(rbmiJill);

			final JRadioButtonMenuItem rbmiJohn = new JRadioButtonMenuItem(new ImageIcon(MainPanel.class.getResource("icons/john.png")));
			rbmiJohn.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (!(selectedPart instanceof Human)) {
							return;
						}
						final Human human = (Human) selectedPart;
						human.setHumanType(Human.JOHN);
						human.draw();
					}
				}
			});
			personButtonGroup.add(rbmiJohn);
			personMenu.add(rbmiJohn);

			final JRadioButtonMenuItem rbmiJose = new JRadioButtonMenuItem(new ImageIcon(MainPanel.class.getResource("icons/jose.png")));
			rbmiJose.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (!(selectedPart instanceof Human)) {
							return;
						}
						final Human human = (Human) selectedPart;
						human.setHumanType(Human.JOSE);
						human.draw();
					}
				}
			});
			personButtonGroup.add(rbmiJose);
			personMenu.add(rbmiJose);

			personMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(final MenuEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Human)) {
						return;
					}
					personButtonGroup.clearSelection();
					final Human human = (Human) selectedPart;
					switch (human.getHumanType()) {
					case Human.JACK:
						Util.selectSilently(rbmiJack, true);
						break;
					case Human.JANE:
						Util.selectSilently(rbmiJane, true);
						break;
					case Human.JENI:
						Util.selectSilently(rbmiJeni, true);
						break;
					case Human.JILL:
						Util.selectSilently(rbmiJill, true);
						break;
					case Human.JOHN:
						Util.selectSilently(rbmiJohn, true);
						break;
					case Human.JOSE:
						Util.selectSilently(rbmiJose, true);
						break;
					}
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
