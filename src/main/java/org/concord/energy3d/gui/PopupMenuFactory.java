package org.concord.energy3d.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.concurrent.Callable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Thermal;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.ProjectCost;
import org.concord.energy3d.undo.ChangeBuildingUValueCommand;
import org.concord.energy3d.undo.ChangePartUValueCommand;
import org.concord.energy3d.undo.ChangeVolumetricHeatCapacityCommand;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.Util;

/**
 * Pop-up menus for customizing individual elements.
 *
 * @author Charles Xie
 *
 */

public abstract class PopupMenuFactory {

	static DecimalFormat integerFormat = new DecimalFormat();
	static DecimalFormat threeDecimalsFormat = new DecimalFormat();
	static DecimalFormat sixDecimalsFormat = new DecimalFormat();

	static {
		integerFormat.setMaximumFractionDigits(0);
		threeDecimalsFormat.setMaximumFractionDigits(3);
		sixDecimalsFormat.setMaximumFractionDigits(6);
	}

	static SolarPanelNominalSize solarPanelNominalSize = new SolarPanelNominalSize();

	// cached values
	static JColorChooser colorChooser = new JColorChooser();
	static double solarPanelWidth = 0.99;
	static double solarPanelHeight = 1.96;
	static int solarPanelRowsPerRack = 3;
	static double solarCellEfficiencyPercentage = 15;
	static double inverterEfficiencyPercentage = 95;
	static double solarPanelTemperatureCoefficientPmaxPercentage = -0.5;
	static double solarPanelNominalOperatingCellTemperature = 48;
	static Action colorAction = new AbstractAction("Color...") {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			MainFrame.getInstance().showColorDialogForParts();
		}
	};

	PopupMenuFactory() {
	}

	public static JPopupMenu getPopupMenu(final boolean onLand) {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart instanceof Window) {
			return PopupMenuForWindow.getPopupMenu();
		}
		if (selectedPart instanceof Wall) {
			return PopupMenuForWall.getPopupMenuForWall();
		}
		if (selectedPart instanceof Roof) {
			return PopupMenuForRoof.getPopupMenu();
		}
		if (selectedPart instanceof Door) {
			return PopupMenuForDoor.getPopupMenu();
		}
		if (selectedPart instanceof Floor) {
			return PopupMenuForFloor.getPopupMenu();
		}
		if (selectedPart instanceof Foundation) {
			final Foundation f = (Foundation) selectedPart;
			if (f.getSelectedMesh() != null) {
				return PopupMenuForMesh.getPopupMenu();
			}
			return PopupMenuForFoundation.getPopupMenu();
		}
		if (selectedPart instanceof SolarPanel) {
			return PopupMenuForSolarPanel.getPopupMenu();
		}
		if (selectedPart instanceof Rack) {
			return PopupMenuForRack.getPopupMenu();
		}
		if (selectedPart instanceof Mirror) {
			return PopupMenuForMirror.getPopupMenu();
		}
		if (selectedPart instanceof ParabolicTrough) {
			return PopupMenuForParabolicTrough.getPopupMenu();
		}
		if (selectedPart instanceof ParabolicDish) {
			return PopupMenuForParabolicDish.getPopupMenu();
		}
		if (selectedPart instanceof FresnelReflector) {
			return PopupMenuForFresnelReflector.getPopupMenu();
		}
		if (selectedPart instanceof Sensor) {
			return PopupMenuForSensor.getPopupMenu();
		}
		if (selectedPart instanceof Tree) {
			return PopupMenuForTree.getPopupMenu();
		}
		if (selectedPart instanceof Human) {
			return PopupMenuForHuman.getPopupMenu();
		}
		return onLand ? PopupMenuForLand.getPopupMenu() : PopupMenuForSky.getPopupMenu();
	}

	static void addPrefabMenuItem(final String type, final String url, final JMenu menu) {
		final JMenuItem mi = new JMenuItem(type);
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				SceneManager.getTaskManager().update(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						try {
							Scene.getInstance().importFile(MainApplication.class.getResource(url));
						} catch (final Throwable err) {
							Util.reportError(err);
						}
						return null;
					}
				});
			}
		});
		menu.add(mi);
	}

	static JMenuItem createInsulationMenuItem(final boolean useUValue) {
		final JMenuItem mi = new JMenuItem("Insulation...");
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (!(selectedPart instanceof Thermal)) {
					return;
				}
				final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
				final Thermal t = (Thermal) selectedPart;

				final JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				String s;
				if (useUValue) {
					if (selectedPart instanceof Door) {
						s = "<html>U-Value of " + partInfo + "<hr><font size=2>Examples:<br>US 1.20 (uninsulated metal), US 0.60 (insulated metal), US 0.50 (wood)</html>";
					} else {
						s = "<html>U-Value of " + partInfo + "<hr><font size=2>Examples:<br>US 1.30 (single glass), US 0.48 (double glass), US 0.25 (triple glass)</html>";
					}
				} else {
					s = "<html>Insulation Value of " + partInfo + "<hr><font size=2>Examples:<br>US R13 (cellulose, 3.5\"), US R16 (mineral wool, 5.25\"), US R31 (fiberglass, 10\")</html>";
				}
				final JLabel label = new JLabel(s);
				label.setAlignmentX(Component.LEFT_ALIGNMENT);
				panel.add(label);
				panel.add(Box.createVerticalStrut(15));

				final String partName = selectedPart.getClass().getSimpleName();
				final JRadioButton rb1 = new JRadioButton("Only this " + partName, true);
				final JRadioButton rb2 = new JRadioButton("All " + partName + "s of this Building");
				if (selectedPart instanceof Wall || selectedPart instanceof Window) {
					final JPanel scopePanel = new JPanel();
					scopePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					scopePanel.add(rb1);
					scopePanel.add(rb2);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					panel.add(scopePanel);
					panel.add(Box.createVerticalStrut(5));
				}

				final JPanel unitPanel = new JPanel(new GridLayout(2, 3, 5, 5));
				unitPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
				panel.add(unitPanel);

				unitPanel.add(new JLabel("U-Value in SI Unit:"));
				final JTextField siField = new JTextField("" + t.getUValue(), 10);
				unitPanel.add(siField);
				unitPanel.add(new JLabel("<html>W/(m<sup>2</sup>&middot;&deg;C)</html>"));

				if (useUValue) {

					unitPanel.add(new JLabel("U-Value in US Unit:"));
					final JTextField uValueField = new JTextField(threeDecimalsFormat.format(Util.toUsUValue(t.getUValue())), 10);
					uValueField.setAlignmentX(Component.LEFT_ALIGNMENT);
					unitPanel.add(uValueField);
					unitPanel.add(new JLabel("<html>Btu/(h&middot;ft<sup>2</sup>&middot;&deg;F)</html>"));

					siField.getDocument().addDocumentListener(new DocumentListener() {

						private void update() {
							if (!siField.hasFocus()) {
								return;
							}
							final String newValue = siField.getText();
							if ("".equals(newValue)) {
								return;
							}
							try {
								uValueField.setText(threeDecimalsFormat.format(Util.toUsUValue(Double.parseDouble(newValue))));
							} catch (final Exception exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}

						@Override
						public void removeUpdate(final DocumentEvent e) {
							update();
						}

						@Override
						public void insertUpdate(final DocumentEvent e) {
							update();
						}

						@Override
						public void changedUpdate(final DocumentEvent e) {
							update();
						}
					});
					uValueField.getDocument().addDocumentListener(new DocumentListener() {

						private void update() {
							if (!uValueField.hasFocus()) {
								return;
							}
							final String newValue = uValueField.getText();
							if ("".equals(newValue)) {
								return;
							}
							try {
								siField.setText(threeDecimalsFormat.format(1.0 / (Util.toSiRValue(1.0 / Double.parseDouble(newValue)))));
							} catch (final Exception exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}

						@Override
						public void removeUpdate(final DocumentEvent e) {
							update();
						}

						@Override
						public void insertUpdate(final DocumentEvent e) {
							update();
						}

						@Override
						public void changedUpdate(final DocumentEvent e) {
							update();
						}
					});

				} else {

					unitPanel.add(new JLabel("R-Value in US Unit:"));
					final JTextField rValueField = new JTextField(integerFormat.format(Util.toUsRValue(t.getUValue())), 10);
					rValueField.setAlignmentX(Component.LEFT_ALIGNMENT);
					unitPanel.add(rValueField);
					unitPanel.add(new JLabel("<html>h&middot;ft<sup>2</sup>&middot;&deg;F/Btu</html>"));

					siField.getDocument().addDocumentListener(new DocumentListener() {

						private void update() {
							if (!siField.hasFocus()) {
								return;
							}
							final String newValue = siField.getText();
							if ("".equals(newValue)) {
								return;
							}
							try {
								rValueField.setText(integerFormat.format(Util.toUsRValue(Double.parseDouble(newValue))));
							} catch (final Exception exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}

						@Override
						public void removeUpdate(final DocumentEvent e) {
							update();
						}

						@Override
						public void insertUpdate(final DocumentEvent e) {
							update();
						}

						@Override
						public void changedUpdate(final DocumentEvent e) {
							update();
						}
					});
					rValueField.getDocument().addDocumentListener(new DocumentListener() {

						private void update() {
							if (!rValueField.hasFocus()) {
								return;
							}
							final String newValue = rValueField.getText();
							if ("".equals(newValue)) {
								return;
							}
							try {
								siField.setText(threeDecimalsFormat.format(1.0 / Util.toSiRValue(Double.parseDouble(newValue))));
							} catch (final Exception exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}

						@Override
						public void removeUpdate(final DocumentEvent e) {
							update();
						}

						@Override
						public void insertUpdate(final DocumentEvent e) {
							update();
						}

						@Override
						public void changedUpdate(final DocumentEvent e) {
							update();
						}
					});

				}

				while (true) {
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), panel, "Input: " + partInfo, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
						final String newValue = siField.getText();
						try {
							final double val = Double.parseDouble(newValue);
							if (val <= 0) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "U-value must be positive.", "Range Error", JOptionPane.ERROR_MESSAGE);
							} else {
								boolean changed = val != t.getUValue();
								if (rb1.isSelected()) {
									if (changed) {
										final ChangePartUValueCommand c = new ChangePartUValueCommand(selectedPart);
										t.setUValue(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
								} else {
									if (!changed) {
										for (final HousePart x : Scene.getInstance().getPartsOfSameTypeInBuilding(selectedPart)) {
											if (val != ((Thermal) x).getUValue()) {
												changed = true;
												break;
											}
										}
									}
									if (changed) {
										final ChangeBuildingUValueCommand c = new ChangeBuildingUValueCommand(selectedPart);
										Scene.getInstance().setUValuesOfSameTypeInBuilding(selectedPart, val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
								}
								if (changed) {
									updateAfterEdit();
									EnergyPanel.getInstance().getBuildingCostGraph().updateBudget();
								}
								break;
							}
						} catch (final NumberFormatException exception) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
						}
					} else {
						break;
					}
				}

			}
		});
		return mi;

	}

	static JMenuItem createVolumetricHeatCapacityMenuItem() {
		final JMenuItem mi = new JMenuItem("Volumeric Heat Capacity...");
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (!(selectedPart instanceof Thermal)) {
					return;
				}
				final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
				final Thermal t = (Thermal) selectedPart;
				final String title = "<html>Volumeric Heat Capacity of " + partInfo + " [kWh/(m<sup>3</sup>&middot;&deg;C)]<hr><font size=2>Examples:<br>0.03 (fiberglass), 0.18 (asphalt), 0.25(oak wood), 0.33 (concrete), 0.37 (brick), 0.58 (stone)</html>";
				while (true) {
					// final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), title, t.getVolumetricHeatCapacity());
					final String newValue = (String) JOptionPane.showInputDialog(MainFrame.getInstance(), title, "Input: " + partInfo, JOptionPane.QUESTION_MESSAGE, null, null, t.getVolumetricHeatCapacity());
					if (newValue == null) {
						break;
					} else {
						try {
							final double val = Double.parseDouble(newValue);
							if (val <= 0) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Volumeric heat capacity must be positive.", "Range Error", JOptionPane.ERROR_MESSAGE);
							} else {
								if (val != t.getVolumetricHeatCapacity()) {
									final ChangeVolumetricHeatCapacityCommand c = new ChangeVolumetricHeatCapacityCommand(selectedPart);
									t.setVolumetricHeatCapacity(val);
									updateAfterEdit();
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								break;
							}
						} catch (final NumberFormatException exception) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		});
		return mi;
	}

	static JPopupMenu createPopupMenu(final boolean hasCopyMenu, final boolean pastable, final Runnable runWhenBecomingVisible) {

		final JMenuItem miInfo = new JMenuItem();
		miInfo.setEnabled(false);
		miInfo.setOpaque(true);
		miInfo.setBackground(Config.isMac() ? Color.BLACK : Color.GRAY);
		miInfo.setForeground(Color.WHITE);

		final JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.setInvoker(MainPanel.getInstance().getCanvasPanel());
		popupMenu.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (selectedPart == null) {
					return;
				}
				final String s = selectedPart.toString();
				if (selectedPart instanceof SolarPanel) {
					final SolarPanel sp = (SolarPanel) selectedPart;
					miInfo.setText(s.substring(0, s.indexOf(')') + 1) + ": " + sp.getModelName() + " ($" + (int) ProjectCost.getCost(selectedPart) + ")");
				} else if (selectedPart instanceof Rack) {
					final SolarPanel sp = ((Rack) selectedPart).getSolarPanel();
					miInfo.setText(s.substring(0, s.indexOf(')') + 1) + ": " + sp.getModelName() + " ($" + (int) ProjectCost.getCost(selectedPart) + ")");
				} else {
					miInfo.setText(s.substring(0, s.indexOf(')') + 1) + " ($" + (int) ProjectCost.getCost(selectedPart) + ")");
				}
				if (runWhenBecomingVisible != null) {
					runWhenBecomingVisible.run();
				}
			}

			@Override
			public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(final PopupMenuEvent e) {
			}

		});

		final JMenuItem miCut = new JMenuItem(pastable ? "Cut" : "Delete");
		miCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
		miCut.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (selectedPart != null) {
					Scene.getInstance().setCopyBuffer(selectedPart);
					SceneManager.getInstance().deleteCurrentSelection();
				}
			}
		});

		popupMenu.add(miInfo);
		// popupMenu.addSeparator();
		popupMenu.add(miCut);

		if (hasCopyMenu) {
			final JMenuItem miCopy = new JMenuItem("Copy");
			miCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			miCopy.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart != null) {
						Scene.getInstance().setCopyBuffer(selectedPart.copy(false));
					}
				}
			});
			popupMenu.add(miCopy);
		}

		return popupMenu;

	}

	static void updateAfterEdit() {
		EnergyPanel.getInstance().updateProperties();
		EnergyPanel.getInstance().updateRadiationHeatMap();
		Scene.getInstance().setEdited(true);
	}

}
