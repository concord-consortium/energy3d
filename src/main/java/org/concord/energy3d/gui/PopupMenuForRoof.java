package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.EnergyAnnualAnalysis;
import org.concord.energy3d.simulation.EnergyDailyAnalysis;
import org.concord.energy3d.undo.ChangeRoofOverhangCommand;
import org.concord.energy3d.undo.ChangeRoofTypeCommand;
import org.concord.energy3d.undo.ChangeTextureCommand;
import org.concord.energy3d.undo.SetTextureForPartsCommand;
import org.concord.energy3d.undo.SetTextureForRoofsOnFoundationCommand;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.Util;

class PopupMenuForRoof extends PopupMenuFactory {

	private static JPopupMenu popupMenuForRoof;

	static JPopupMenu getPopupMenu(final MouseEvent e) {

		if (e.isShiftDown()) {
			SceneManager.getTaskManager().update(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					Scene.getInstance().pasteToPickedLocationOnRoof();
					Scene.getInstance().setEdited(true);
					return null;
				}
			});
			return null;
		}

		if (popupMenuForRoof == null) {

			final JMenuItem miPaste = new JMenuItem("Paste");
			miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			miPaste.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().pasteToPickedLocationOnRoof();
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

			final JMenuItem miOverhang = new JMenuItem("Overhang Length...");
			miOverhang.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Roof)) {
						return;
					}
					final Roof roof = (Roof) selectedPart;
					while (true) {
						SceneManager.getInstance().refresh(1);
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), "Overhang Length (m)", roof.getOverhangLength() * Scene.getInstance().getAnnotationScale());
						if (newValue == null) {
							break;
						} else {
							try {
								double val = Double.parseDouble(newValue);
								final double min = Roof.OVERHANG_MIN * Scene.getInstance().getAnnotationScale() * 10;
								if (val < min && val >= 0) {
									val = min;
								}
								if (val < 0 || val > 10) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Overhang value must be between " + min + " and 10.", "Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (Math.abs(val - roof.getOverhangLength() * Scene.getInstance().getAnnotationScale()) > 0.000001) {
										final ChangeRoofOverhangCommand c = new ChangeRoofOverhangCommand(roof);
										roof.setOverhangLength(val / Scene.getInstance().getAnnotationScale());
										roof.draw();
										final Foundation f = roof.getTopContainer();
										f.drawChildren();
										SceneManager.getInstance().refresh();
										updateAfterEdit();
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									break;
								}
							} catch (final NumberFormatException exception) {
								exception.printStackTrace();
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
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
						if (selectedPart instanceof Roof) {
							final Roof roof = (Roof) selectedPart;
							final ChangeRoofTypeCommand c = new ChangeRoofTypeCommand(roof);
							roof.setType(Roof.SOLID);
							roof.draw();
							SceneManager.getInstance().refresh();
							Scene.getInstance().setEdited(true);
							SceneManager.getInstance().getUndoManager().addEdit(c);
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
						if (selectedPart instanceof Roof) {
							final Roof roof = (Roof) selectedPart;
							final ChangeRoofTypeCommand c = new ChangeRoofTypeCommand(roof);
							roof.setType(Roof.TRANSPARENT);
							roof.draw();
							SceneManager.getInstance().refresh();
							Scene.getInstance().setEdited(true);
							SceneManager.getInstance().getUndoManager().addEdit(c);
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
					if (selectedPart instanceof Roof) {
						final Roof roof = (Roof) selectedPart;
						switch (roof.getType()) {
						case Roof.SOLID:
							Util.selectSilently(rbmiSolid, true);
							break;
						case Roof.TRANSPARENT:
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
			final ButtonGroup textureButtonGroup = new ButtonGroup();
			final JRadioButtonMenuItem rbmiTextureNone = createTextureMenuItem(Roof.TEXTURE_NONE, null);
			final JRadioButtonMenuItem rbmiTextureEdge = createTextureMenuItem(Roof.TEXTURE_EDGE, null);
			final JRadioButtonMenuItem rbmiTexture01 = createTextureMenuItem(Roof.TEXTURE_01, "icons/roof_01.png");
			final JRadioButtonMenuItem rbmiTexture02 = createTextureMenuItem(Roof.TEXTURE_02, "icons/roof_02.png");
			final JRadioButtonMenuItem rbmiTexture03 = createTextureMenuItem(Roof.TEXTURE_03, "icons/roof_03.png");
			final JRadioButtonMenuItem rbmiTexture04 = createTextureMenuItem(Roof.TEXTURE_04, "icons/roof_04.png");
			final JRadioButtonMenuItem rbmiTexture05 = createTextureMenuItem(Roof.TEXTURE_05, "icons/roof_05.png");
			final JRadioButtonMenuItem rbmiTexture06 = createTextureMenuItem(Roof.TEXTURE_06, "icons/roof_06.png");
			final JRadioButtonMenuItem rbmiTexture07 = createTextureMenuItem(Roof.TEXTURE_07, "icons/roof_07.png");
			textureButtonGroup.add(rbmiTextureNone);
			textureButtonGroup.add(rbmiTextureEdge);
			textureButtonGroup.add(rbmiTexture01);
			textureButtonGroup.add(rbmiTexture02);
			textureButtonGroup.add(rbmiTexture03);
			textureButtonGroup.add(rbmiTexture04);
			textureButtonGroup.add(rbmiTexture05);
			textureButtonGroup.add(rbmiTexture06);
			textureButtonGroup.add(rbmiTexture07);
			textureMenu.add(rbmiTextureNone);
			textureMenu.add(rbmiTextureEdge);
			textureMenu.addSeparator();
			textureMenu.add(rbmiTexture01);
			textureMenu.add(rbmiTexture02);
			textureMenu.add(rbmiTexture03);
			textureMenu.add(rbmiTexture04);
			textureMenu.add(rbmiTexture05);
			textureMenu.add(rbmiTexture06);
			textureMenu.add(rbmiTexture07);

			textureMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(final MenuEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Roof)) {
						return;
					}
					final Roof roof = (Roof) selectedPart;
					switch (roof.getTextureType()) {
					case Roof.TEXTURE_NONE:
						Util.selectSilently(rbmiTextureNone, true);
						break;
					case Roof.TEXTURE_EDGE:
						Util.selectSilently(rbmiTextureEdge, true);
						break;
					case Roof.TEXTURE_01:
						Util.selectSilently(rbmiTexture01, true);
						break;
					case Roof.TEXTURE_02:
						Util.selectSilently(rbmiTexture02, true);
						break;
					case Roof.TEXTURE_03:
						Util.selectSilently(rbmiTexture03, true);
						break;
					case Roof.TEXTURE_04:
						Util.selectSilently(rbmiTexture04, true);
						break;
					case Roof.TEXTURE_05:
						Util.selectSilently(rbmiTexture05, true);
						break;
					case Roof.TEXTURE_06:
						Util.selectSilently(rbmiTexture06, true);
						break;
					case Roof.TEXTURE_07:
						Util.selectSilently(rbmiTexture07, true);
						break;
					default:
						textureButtonGroup.clearSelection();
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

			popupMenuForRoof = createPopupMenu(false, false, new Runnable() {
				@Override
				public void run() {
					final HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
					miPaste.setEnabled(copyBuffer instanceof SolarPanel || copyBuffer instanceof Window || copyBuffer instanceof Rack || copyBuffer instanceof Human);
				}
			});

			popupMenuForRoof.add(miPaste);
			popupMenuForRoof.add(miClear);
			popupMenuForRoof.addSeparator();
			popupMenuForRoof.add(miOverhang);
			popupMenuForRoof.add(colorAction);
			popupMenuForRoof.add(createInsulationMenuItem(false));
			popupMenuForRoof.add(createVolumetricHeatCapacityMenuItem());
			popupMenuForRoof.addSeparator();
			popupMenuForRoof.add(typeMenu);
			popupMenuForRoof.add(textureMenu);
			popupMenuForRoof.addSeparator();

			JMenuItem mi = new JMenuItem("Daily Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (EnergyPanel.getInstance().adjustCellSize()) {
						return;
					}
					if (SceneManager.getInstance().getSelectedPart() instanceof Roof) {
						new EnergyDailyAnalysis().show("Daily Energy for Roof");
					}
				}
			});
			popupMenuForRoof.add(mi);

			mi = new JMenuItem("Annual Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (EnergyPanel.getInstance().adjustCellSize()) {
						return;
					}
					if (SceneManager.getInstance().getSelectedPart() instanceof Roof) {
						new EnergyAnnualAnalysis().show("Annual Energy for Roof");
					}
				}
			});
			popupMenuForRoof.add(mi);

		}

		return popupMenuForRoof;

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

			private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

			@Override
			public void itemStateChanged(final ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Roof)) {
						return;
					}
					final Roof roof = (Roof) selectedPart;
					final Foundation foundation = roof.getTopContainer();
					final String partInfo = roof.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Roof", true);
					final JRadioButton rb2 = new JRadioButton("All Roofs on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Roofs");
					scopePanel.add(rb1);
					scopePanel.add(rb2);
					scopePanel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					switch (selectedScopeIndex) {
					case 0:
						rb1.setSelected(true);
						break;
					case 1:
						rb2.setSelected(true);
						break;
					case 2:
						rb3.setSelected(true);
						break;
					}
					gui.add(scopePanel, BorderLayout.NORTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { "Set Texture for " + partInfo, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Roof Texture");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							if (rb1.isSelected()) {
								final ChangeTextureCommand c = new ChangeTextureCommand(roof);
								roof.setTextureType(type);
								roof.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final SetTextureForRoofsOnFoundationCommand c = new SetTextureForRoofsOnFoundationCommand(foundation);
								foundation.setTextureForRoofs(type);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final List<HousePart> roofs = Scene.getInstance().getAllPartsOfSameType(roof);
								final SetTextureForPartsCommand c = new SetTextureForPartsCommand(roofs);
								for (final HousePart p : roofs) {
									p.setTextureType(type);
									p.draw();
								}
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 2;
							}
							SceneManager.getInstance().refresh();
							updateAfterEdit();
							if (MainPanel.getInstance().getEnergyButton().isSelected()) {
								MainPanel.getInstance().getEnergyButton().setSelected(false);
							}
							if (choice == options[0]) {
								break;
							}
						}
					}

				}
			}
		});

		return m;

	}

}
