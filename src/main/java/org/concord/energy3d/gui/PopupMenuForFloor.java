package org.concord.energy3d.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.concurrent.Callable;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.undo.ChangeBuildingTextureCommand;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.Util;

class PopupMenuForFloor extends PopupMenuFactory {

	private static JPopupMenu popupMenuForFloor;

	static JPopupMenu getPopupMenu(final MouseEvent e) {

		if (e.isShiftDown()) {
			SceneManager.getTaskManager().update(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					Scene.getInstance().pasteToPickedLocationOnFloor();
					Scene.getInstance().setEdited(true);
					return null;
				}
			});
			return null;
		}

		if (popupMenuForFloor == null) {

			final JMenuItem miInfo = new JMenuItem("Floor");
			miInfo.setEnabled(false);
			miInfo.setOpaque(true);
			miInfo.setBackground(Config.isMac() ? Color.BLACK : Color.GRAY);
			miInfo.setForeground(Color.WHITE);

			final JMenuItem miPaste = new JMenuItem("Paste");
			miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			miPaste.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().pasteToPickedLocationOnFloor();
							Scene.getInstance().setEdited(true);
							return null;
						}
					});
				}
			});

			final JMenuItem miClear = new JMenuItem("Clear");
			miClear.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().removeAllChildren(SceneManager.getInstance().getSelectedPart());
							Scene.getInstance().setEdited(true);
							return null;
						}
					});
				}
			});

			final JMenu typeMenu = new JMenu("Type");
			final ButtonGroup typeGroup = new ButtonGroup();

			final JRadioButtonMenuItem rbmiSolid = new JRadioButtonMenuItem("Solid");
			rbmiSolid.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Floor) {
							final Floor floor = (Floor) selectedPart;
							// final ChangeRoofTypeCommand c = new ChangeRoofTypeCommand(roof);
							floor.setType(Floor.SOLID);
							floor.draw();
							SceneManager.getInstance().refresh();
							Scene.getInstance().setEdited(true);
							// SceneManager.getInstance().getUndoManager().addEdit(c);
						}
					}
				}
			});
			typeMenu.add(rbmiSolid);
			typeGroup.add(rbmiSolid);

			final JRadioButtonMenuItem rbmiTransparent = new JRadioButtonMenuItem("Transparent");
			rbmiTransparent.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Floor) {
							final Floor floor = (Floor) selectedPart;
							// final ChangeRoofTypeCommand c = new ChangeRoofTypeCommand(roof);
							floor.setType(Floor.TRANSPARENT);
							floor.draw();
							SceneManager.getInstance().refresh();
							Scene.getInstance().setEdited(true);
							// SceneManager.getInstance().getUndoManager().addEdit(c);
						}
					}
				}
			});
			typeMenu.add(rbmiTransparent);
			typeGroup.add(rbmiTransparent);

			typeMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(final MenuEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Floor) {
						final Floor floor = (Floor) selectedPart;
						switch (floor.getType()) {
						case Floor.SOLID:
							Util.selectSilently(rbmiSolid, true);
							break;
						case Floor.TRANSPARENT:
							Util.selectSilently(rbmiTransparent, true);
							break;
						}
					}
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					typeMenu.setEnabled(true);
				}

				@Override
				public void menuCanceled(final MenuEvent e) {
					typeMenu.setEnabled(true);
				}

			});

			final JMenu textureMenu = new JMenu("Texture");
			final ButtonGroup textureGroup = new ButtonGroup();

			final JRadioButtonMenuItem rbmiTextureNone = new JRadioButtonMenuItem("No Texture");
			rbmiTextureNone.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final ChangeBuildingTextureCommand c = new ChangeBuildingTextureCommand();
						Scene.getInstance().setTextureMode(TextureMode.None);
						Scene.getInstance().setEdited(true);
						if (MainPanel.getInstance().getEnergyButton().isSelected()) {
							MainPanel.getInstance().getEnergyButton().setSelected(false);
						}
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			textureGroup.add(rbmiTextureNone);
			textureMenu.add(rbmiTextureNone);

			final JRadioButtonMenuItem rbmiTextureOutline = new JRadioButtonMenuItem("Outline Texture");
			rbmiTextureOutline.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final ChangeBuildingTextureCommand c = new ChangeBuildingTextureCommand();
						Scene.getInstance().setTextureMode(TextureMode.Simple);
						Scene.getInstance().setEdited(true);
						if (MainPanel.getInstance().getEnergyButton().isSelected()) {
							MainPanel.getInstance().getEnergyButton().setSelected(false);
						}
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			textureGroup.add(rbmiTextureOutline);
			textureMenu.add(rbmiTextureOutline);
			textureMenu.addSeparator();

			final JRadioButtonMenuItem rbmiTexture01 = MainFrame.getInstance().createFloorTextureMenuItem(Floor.TEXTURE_01, "icons/floor_01.png");
			textureGroup.add(rbmiTexture01);
			textureMenu.add(rbmiTexture01);

			textureMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(final MenuEvent e) {
					if (Scene.getInstance().getTextureMode() == TextureMode.None) {
						Util.selectSilently(rbmiTextureNone, true);
						return;
					}
					if (Scene.getInstance().getTextureMode() == TextureMode.Simple) {
						Util.selectSilently(rbmiTextureOutline, true);
						return;
					}
					switch (Scene.getInstance().getWallTextureType()) {
					case Floor.TEXTURE_01:
						Util.selectSilently(rbmiTexture01, true);
						break;
					}
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					textureMenu.setEnabled(true);
				}

				@Override
				public void menuCanceled(final MenuEvent e) {
					textureMenu.setEnabled(true);
				}

			});

			popupMenuForFloor = createPopupMenu(false, false, new Runnable() {
				@Override
				public void run() {
					final HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
					miPaste.setEnabled(copyBuffer instanceof SolarPanel || copyBuffer instanceof Rack || copyBuffer instanceof Human);
				}
			});

			popupMenuForFloor.add(miPaste);
			popupMenuForFloor.add(miClear);
			popupMenuForFloor.addSeparator();
			popupMenuForFloor.add(typeMenu);
			popupMenuForFloor.add(textureMenu);
			popupMenuForFloor.add(colorAction);

		}

		return popupMenuForFloor;

	}
}
