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
import javax.swing.ImageIcon;
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
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.undo.ChangeTextureCommand;
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
			final JRadioButtonMenuItem rbmiTextureNone = createTextureMenuItem(Floor.TEXTURE_NONE, null);
			final JRadioButtonMenuItem rbmiTextureEdge = createTextureMenuItem(Floor.TEXTURE_EDGE, null);
			final JRadioButtonMenuItem rbmiTexture01 = createTextureMenuItem(Floor.TEXTURE_01, "icons/floor_01.png");
			textureGroup.add(rbmiTextureNone);
			textureGroup.add(rbmiTextureEdge);
			textureGroup.add(rbmiTexture01);
			textureMenu.add(rbmiTextureNone);
			textureMenu.add(rbmiTextureEdge);
			textureMenu.addSeparator();
			textureMenu.add(rbmiTexture01);

			textureMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(final MenuEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Floor)) {
						return;
					}
					final Floor floor = (Floor) selectedPart;
					switch (floor.getTextureType()) {
					case Floor.TEXTURE_EDGE:
						Util.selectSilently(rbmiTextureEdge, true);
						break;
					case Floor.TEXTURE_NONE:
						Util.selectSilently(rbmiTextureNone, true);
						break;
					case Floor.TEXTURE_01:
						Util.selectSilently(rbmiTexture01, true);
						break;
					default:
						textureGroup.clearSelection();
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

	private static JRadioButtonMenuItem createTextureMenuItem(final int type, final String imageFile) {

		final JRadioButtonMenuItem m;
		if (type == HousePart.TEXTURE_NONE) {
			m = new JRadioButtonMenuItem("No Texture");
		} else if (type == HousePart.TEXTURE_EDGE) {
			m = new JRadioButtonMenuItem("Edge Texture");
		} else {
			m = new JRadioButtonMenuItem(new ImageIcon(MainPanel.class.getResource(imageFile)));
			m.setText("Texture #" + type);
		}

		m.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Floor)) {
						return;
					}
					final Floor floor = (Floor) selectedPart;
					final ChangeTextureCommand c = new ChangeTextureCommand(floor);
					floor.setTextureType(type);
					floor.draw();
					SceneManager.getInstance().refresh();
					Scene.getInstance().setEdited(true);
					if (MainPanel.getInstance().getEnergyButton().isSelected()) {
						MainPanel.getInstance().getEnergyButton().setSelected(false);
					}
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			}
		});

		return m;

	}

}
