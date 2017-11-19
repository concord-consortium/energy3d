package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;

import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.MirrorCircularFieldLayout;
import org.concord.energy3d.model.MirrorRectangularFieldLayout;
import org.concord.energy3d.model.MirrorSpiralFieldLayout;
import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarCollector;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.EnergyAnnualAnalysis;
import org.concord.energy3d.simulation.EnergyDailyAnalysis;
import org.concord.energy3d.simulation.FresnelReflectorAnnualAnalysis;
import org.concord.energy3d.simulation.FresnelReflectorDailyAnalysis;
import org.concord.energy3d.simulation.MirrorAnnualAnalysis;
import org.concord.energy3d.simulation.MirrorDailyAnalysis;
import org.concord.energy3d.simulation.ParabolicDishAnnualAnalysis;
import org.concord.energy3d.simulation.ParabolicDishDailyAnalysis;
import org.concord.energy3d.simulation.ParabolicTroughAnnualAnalysis;
import org.concord.energy3d.simulation.ParabolicTroughDailyAnalysis;
import org.concord.energy3d.simulation.PvAnnualAnalysis;
import org.concord.energy3d.simulation.PvDailyAnalysis;
import org.concord.energy3d.simulation.PvModuleSpecs;
import org.concord.energy3d.simulation.PvModulesData;
import org.concord.energy3d.simulation.UtilityBill;
import org.concord.energy3d.undo.AddNodeCommand;
import org.concord.energy3d.undo.ChangeFoundationSizeCommand;
import org.concord.energy3d.undo.DeleteUtilityBillCommand;
import org.concord.energy3d.undo.SetFoundationLabelCommand;
import org.concord.energy3d.undo.SetGroupMasterCommand;
import org.concord.energy3d.undo.ShowBorderLineCommand;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.FileChooser;
import org.concord.energy3d.util.SpringUtilities;
import org.concord.energy3d.util.Util;

import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Node;

class PopupMenuForFoundation extends PopupMenuFactory {

	private static JPopupMenu popupMenuForFoundation;
	private static double solarPanelArrayBaseHeight = 1;
	private static int solarPanelArrayRowAxis = 0;
	private static double solarPanelArrayRowSpacing = solarPanelHeight + 1;
	private static double solarPanelArrayColSpacing = solarPanelWidth + 1;
	private static int solarPanelOrientation = 0;
	private static String solarPanelModel = "Custom";
	private static int solarPanelColorOption = SolarPanel.COLOR_OPTION_BLUE;
	private static int solarPanelCellType = SolarPanel.MONOCRYSTALLINE;
	private static double solarPanelRackArrayInterRowSpacing = solarPanelHeight * solarPanelRowsPerRack + 1;
	private static double solarPanelRackPoleSpacingX = 4;
	private static double solarPanelRackPoleSpacingY = 2;
	private static double solarPanelRackBaseHeight = 3;
	private static double solarPanelTiltAngle = 0;
	private static int solarPanelShadeTolerance = SolarPanel.PARTIAL_SHADE_TOLERANCE;
	private static MirrorRectangularFieldLayout mirrorRectangularFieldLayout = new MirrorRectangularFieldLayout();
	private static MirrorCircularFieldLayout mirrorCircularFieldLayout = new MirrorCircularFieldLayout();
	private static MirrorSpiralFieldLayout mirrorSpiralFieldLayout = new MirrorSpiralFieldLayout();

	static JPopupMenu getPopupMenu() {

		if (popupMenuForFoundation == null) {

			final JMenuItem miImportCollada = new JMenuItem("Import Collada...");
			miImportCollada.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final File file = FileChooser.getInstance().showDialog(".dae", MainFrame.daeFilter, false);
						if (file != null) {
							EnergyPanel.getInstance().updateRadiationHeatMap();
							SceneManager.getTaskManager().update(new Callable<Object>() {
								@Override
								public Object call() throws Exception {
									boolean success = true;
									final Vector3 position = SceneManager.getInstance().getPickedLocationOnFoundation();
									try {
										((Foundation) selectedPart).importCollada(file.toURI().toURL(), position);
									} catch (final Throwable t) {
										Util.reportError(t);
										success = false;
									}
									if (success) {
										SceneManager.getInstance().getUndoManager().addEdit(new AddNodeCommand((Foundation) selectedPart));
									}
									return null;
								}
							});
						}
					}
				}
			});

			final JMenuItem miPaste = new JMenuItem("Paste");
			miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			miPaste.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().pasteToPickedLocationOnFoundation();
							Scene.getInstance().setEdited(true);
							return null;
						}
					});
				}
			});

			final JMenuItem miCopy = new JMenuItem("Copy");
			miCopy.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						Scene.getInstance().setCopyBuffer(selectedPart);
					}
				}
			});

			final JMenuItem miRescale = new JMenuItem("Rescale...");
			miRescale.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Foundation)) {
						return;
					}
					new RescaleBuildingDialog((Foundation) selectedPart).setVisible(true);
					Scene.getInstance().setEdited(true);
				}
			});

			final JMenu rotateMenu = new JMenu("Rotate");

			final JMenuItem mi180 = new JMenuItem("180\u00B0");
			mi180.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().rotate(Math.PI);
					Scene.getInstance().setEdited(true);
				}
			});
			rotateMenu.add(mi180);

			final JMenuItem mi90CW = new JMenuItem("90\u00B0 Clockwise");
			mi90CW.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().rotate(-Math.PI / 2);
					Scene.getInstance().setEdited(true);
				}
			});
			rotateMenu.add(mi90CW);

			final JMenuItem mi90CCW = new JMenuItem("90\u00B0 Counter Clockwise");
			mi90CCW.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().rotate(Math.PI / 2);
					Scene.getInstance().setEdited(true);
				}
			});
			rotateMenu.add(mi90CCW);

			final JMenu clearMenu = new JMenu("Clear");

			final JMenuItem miRemoveAllWalls = new JMenuItem("Remove All Walls");
			miRemoveAllWalls.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllWalls();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});
			clearMenu.add(miRemoveAllWalls);

			final JMenuItem miRemoveAllWindows = new JMenuItem("Remove All Windows");
			miRemoveAllWindows.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllWindows();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});
			clearMenu.add(miRemoveAllWindows);

			final JMenuItem miRemoveAllWindowShutters = new JMenuItem("Remove All Window Shutters");
			miRemoveAllWindowShutters.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllWindowShutters();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});
			clearMenu.add(miRemoveAllWindowShutters);

			final JMenuItem miRemoveAllSolarPanels = new JMenuItem("Remove All Solar Panels");
			miRemoveAllSolarPanels.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllSolarPanels(null);
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});
			clearMenu.add(miRemoveAllSolarPanels);

			final JMenuItem miRemoveAllRacks = new JMenuItem("Remove All Solar Panel Racks");
			miRemoveAllRacks.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllRacks();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});
			clearMenu.add(miRemoveAllRacks);

			final JMenuItem miRemoveAllMirrors = new JMenuItem("Remove All Mirrors");
			miRemoveAllMirrors.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllMirrors();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});
			clearMenu.add(miRemoveAllMirrors);

			final JMenuItem miRemoveAllParabolicTroughs = new JMenuItem("Remove All Parabolic Troughs");
			miRemoveAllParabolicTroughs.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllParabolicTroughs();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});
			clearMenu.add(miRemoveAllParabolicTroughs);

			final JMenuItem miRemoveAllParabolicDishes = new JMenuItem("Remove All Parabolic Dishes");
			miRemoveAllParabolicDishes.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllParabolicDishes();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});
			clearMenu.add(miRemoveAllParabolicDishes);

			final JMenuItem miRemoveAllFresnelReflectors = new JMenuItem("Remove All Fresnel Reflectors");
			miRemoveAllFresnelReflectors.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllFresnelReflectors();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});
			clearMenu.add(miRemoveAllFresnelReflectors);

			final JMenuItem miRemoveAllSensors = new JMenuItem("Remove All Sensors");
			miRemoveAllSensors.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllSensors();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});
			clearMenu.add(miRemoveAllSensors);

			final JMenuItem removeAllFloorsMenuItem = new JMenuItem("Remove All Floors");
			removeAllFloorsMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllFloors();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									Scene.getInstance().setEdited(true);
								}
							});
							return null;
						}
					});
				}
			});
			clearMenu.add(removeAllFloorsMenuItem);

			final JMenuItem miRemoveAllImportedNodes = new JMenuItem("Remove All Nodes");
			miRemoveAllImportedNodes.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
							if (selectedPart instanceof Foundation) {
								final Foundation f = (Foundation) selectedPart;
								f.removeAllImports();
								f.setMeshSelectionVisible(false);
								EventQueue.invokeLater(new Runnable() {
									@Override
									public void run() {
										MainPanel.getInstance().getEnergyViewButton().setSelected(false);
										Scene.getInstance().setEdited(true);
									}
								});
							}
							return null;
						}
					});
				}
			});
			clearMenu.add(miRemoveAllImportedNodes);

			final JMenu layoutMenu = new JMenu("Layout");

			final JMenuItem miSolarPanelArrays = new JMenuItem("Solar Panel Arrays...");
			layoutMenu.add(miSolarPanelArrays);
			miSolarPanelArrays.addActionListener(new ActionListener() {

				private Foundation f;
				private JComboBox<String> modelComboBox;
				private JComboBox<String> cellTypeComboBox;
				private JComboBox<String> colorOptionComboBox;
				private JComboBox<String> sizeComboBox;
				private JComboBox<String> shadeToleranceComboBox;
				private JComboBox<String> orientationComboBox;
				private JComboBox<String> rowAxisComboBox;
				private JTextField cellEfficiencyField;
				private JTextField noctField;
				private JTextField pmaxTcField;
				private int numberOfCellsInX = 6;
				private int numberOfCellsInY = 10;

				private void enableSettings(final boolean b) {
					sizeComboBox.setEnabled(b);
					cellTypeComboBox.setEnabled(b);
					colorOptionComboBox.setEnabled(b);
					shadeToleranceComboBox.setEnabled(b);
					cellEfficiencyField.setEnabled(b);
					noctField.setEnabled(b);
					pmaxTcField.setEnabled(b);
				}

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						f = (Foundation) selectedPart;
						int n = f.countParts(SolarPanel.class);
						if (n > 0 && JOptionPane.showConfirmDialog(MainFrame.getInstance(), "All existing " + n + " solar panels on this foundation must be removed before\na new layout can be applied. Do you want to continue?", "Confirmation", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
							return;
						}
						n = f.countParts(Rack.class);
						if (n > 0 && JOptionPane.showConfirmDialog(MainFrame.getInstance(), "All existing " + n + " solar panel racks on this foundation must be removed before\na new layout can be applied. Do you want to continue?", "Confirmation", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
							return;
						}

						final JPanel panel = new JPanel(new SpringLayout());

						final Map<String, PvModuleSpecs> modules = PvModulesData.getInstance().getModules();
						final String[] models = new String[modules.size() + 1];
						int j = 0;
						models[j] = "Custom";
						for (final String key : modules.keySet()) {
							models[++j] = key;
						}
						panel.add(new JLabel("Model:"));
						modelComboBox = new JComboBox<String>(models);
						modelComboBox.setSelectedItem(solarPanelModel);
						modelComboBox.addItemListener(new ItemListener() {
							@Override
							public void itemStateChanged(final ItemEvent e) {
								if (e.getStateChange() == ItemEvent.SELECTED) {
									final boolean isCustom = modelComboBox.getSelectedIndex() == 0;
									enableSettings(isCustom);
									if (!isCustom) {
										final PvModuleSpecs specs = modules.get(modelComboBox.getSelectedItem());
										cellTypeComboBox.setSelectedItem(specs.getCellType());
										shadeToleranceComboBox.setSelectedItem(specs.getShadeTolerance());
										cellEfficiencyField.setText(threeDecimalsFormat.format(specs.getCelLEfficiency() * 100));
										noctField.setText(threeDecimalsFormat.format(specs.getNoct()));
										pmaxTcField.setText(sixDecimalsFormat.format(specs.getPmaxTc()));
										final String s = threeDecimalsFormat.format(specs.getNominalWidth()) + "m \u00D7 " + threeDecimalsFormat.format(specs.getNominalLength()) + "m (" + specs.getLayout().width + " \u00D7 " + specs.getLayout().height + " cells)";
										sizeComboBox.setSelectedItem(s);
										colorOptionComboBox.setSelectedItem(specs.getColor());
									}
								}
							}
						});
						panel.add(modelComboBox);

						panel.add(new JLabel("Cell Type:"));
						cellTypeComboBox = new JComboBox<String>(new String[] { "Polycrystalline", "Monocrystalline", "Thin Film" });
						cellTypeComboBox.setSelectedIndex(solarPanelCellType);
						panel.add(cellTypeComboBox);

						panel.add(new JLabel("Color:"));
						colorOptionComboBox = new JComboBox<String>(new String[] { "Blue", "Black", "Gray" });
						colorOptionComboBox.setSelectedIndex(solarPanelColorOption);
						panel.add(colorOptionComboBox);

						panel.add(new JLabel("Size:"));
						sizeComboBox = new JComboBox<String>(solarPanelNominalSize.getStrings());
						final int nItems = sizeComboBox.getItemCount();
						for (int i = 0; i < nItems; i++) {
							if (Util.isZero(solarPanelHeight - solarPanelNominalSize.getNominalHeights()[i]) && Util.isZero(solarPanelWidth - solarPanelNominalSize.getNominalWidths()[i])) {
								sizeComboBox.setSelectedIndex(i);
							}
						}
						panel.add(sizeComboBox);

						panel.add(new JLabel("Solar Cell Efficiency (%):"));
						cellEfficiencyField = new JTextField(threeDecimalsFormat.format(solarCellEfficiencyPercentage));
						panel.add(cellEfficiencyField);

						panel.add(new JLabel("<html>Nominal Operating Cell Temperature (&deg;C):"));
						noctField = new JTextField(threeDecimalsFormat.format(solarPanelNominalOperatingCellTemperature));
						panel.add(noctField);

						panel.add(new JLabel("<html>Temperature Coefficient of Pmax (%/&deg;C):"));
						pmaxTcField = new JTextField(sixDecimalsFormat.format(solarPanelTemperatureCoefficientPmaxPercentage));
						panel.add(pmaxTcField);

						panel.add(new JLabel("Shade Tolerance:"));
						shadeToleranceComboBox = new JComboBox<String>(new String[] { "Partial", "High", "None" });
						shadeToleranceComboBox.setSelectedIndex(solarPanelShadeTolerance);
						panel.add(shadeToleranceComboBox);

						panel.add(new JLabel("Inverter Efficiency (%):"));
						final JTextField inverterEfficiencyField = new JTextField(threeDecimalsFormat.format(inverterEfficiencyPercentage));
						panel.add(inverterEfficiencyField);

						panel.add(new JLabel("Tile Angle (\u00B0):"));
						final JTextField tiltAngleField = new JTextField(threeDecimalsFormat.format(solarPanelTiltAngle));
						panel.add(tiltAngleField);

						panel.add(new JLabel("Orientation:"));
						orientationComboBox = new JComboBox<String>(new String[] { "Portrait", "Landscape" });
						orientationComboBox.setSelectedIndex(solarPanelOrientation);
						panel.add(orientationComboBox);

						panel.add(new JLabel("Row Axis:"));
						rowAxisComboBox = new JComboBox<String>(new String[] { "North-South", "East-West" });
						rowAxisComboBox.setSelectedIndex(solarPanelArrayRowAxis);
						panel.add(rowAxisComboBox);

						panel.add(new JLabel("Row Spacing (m):"));
						final JTextField rowSpacingField = new JTextField(threeDecimalsFormat.format(solarPanelArrayRowSpacing));
						panel.add(rowSpacingField);

						panel.add(new JLabel("Column Spacing (m):"));
						final JTextField colSpacingField = new JTextField(threeDecimalsFormat.format(solarPanelArrayColSpacing));
						panel.add(colSpacingField);

						panel.add(new JLabel("Base Height (m):"));
						final JTextField baseHeightField = new JTextField(threeDecimalsFormat.format(solarPanelArrayBaseHeight));
						panel.add(baseHeightField);

						SpringUtilities.makeCompactGrid(panel, 15, 2, 6, 6, 6, 6);

						enableSettings(modelComboBox.getSelectedIndex() == 0);

						final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
						final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
						final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Array Options");

						while (true) {
							dialog.setVisible(true);
							final Object choice = optionPane.getValue();
							if (choice == options[1] || choice == null) {
								break;
							} else {
								boolean ok = true;
								try {
									solarPanelArrayRowSpacing = Double.parseDouble(rowSpacingField.getText());
									solarPanelArrayColSpacing = Double.parseDouble(colSpacingField.getText());
									solarPanelArrayBaseHeight = Double.parseDouble(baseHeightField.getText());
									solarPanelTiltAngle = Double.parseDouble(tiltAngleField.getText());
									solarCellEfficiencyPercentage = Double.parseDouble(cellEfficiencyField.getText());
									inverterEfficiencyPercentage = Double.parseDouble(inverterEfficiencyField.getText());
									solarPanelTemperatureCoefficientPmaxPercentage = Double.parseDouble(pmaxTcField.getText());
									solarPanelNominalOperatingCellTemperature = Double.parseDouble(noctField.getText());
								} catch (final NumberFormatException exception) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
									ok = false;
								}
								if (ok) {
									final int i = sizeComboBox.getSelectedIndex();
									solarPanelWidth = solarPanelNominalSize.getNominalWidths()[i];
									solarPanelHeight = solarPanelNominalSize.getNominalHeights()[i];
									numberOfCellsInX = solarPanelNominalSize.getCellNx()[i];
									numberOfCellsInY = solarPanelNominalSize.getCellNy()[i];
									solarPanelOrientation = orientationComboBox.getSelectedIndex();
									if (solarPanelArrayRowSpacing < (solarPanelOrientation == 0 ? solarPanelHeight : solarPanelWidth) || solarPanelArrayColSpacing < (solarPanelOrientation == 0 ? solarPanelWidth : solarPanelHeight)) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar panel row or column spacing is too small.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (solarPanelArrayBaseHeight < 0) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar panel base height can't be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (solarPanelTiltAngle < -90 || solarPanelTiltAngle > 90) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar panel tilt angle must be between -90\u00B0 and 90\u00B0.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (Math.abs(0.5 * (solarPanelOrientation == 0 ? solarPanelHeight : solarPanelWidth) * Math.sin(Math.toRadians(solarPanelTiltAngle))) > solarPanelArrayBaseHeight) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar panels intersect with ground.", "Geometry Error", JOptionPane.ERROR_MESSAGE);
									} else if (solarCellEfficiencyPercentage < SolarPanel.MIN_SOLAR_CELL_EFFICIENCY_PERCENTAGE || solarCellEfficiencyPercentage > SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar cell efficiency must be between " + SolarPanel.MIN_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "% and " + SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "%.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (inverterEfficiencyPercentage < SolarPanel.MIN_INVERTER_EFFICIENCY_PERCENTAGE || inverterEfficiencyPercentage >= SolarPanel.MAX_INVERTER_EFFICIENCY_PERCENTAGE) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Inverter efficiency must be greater than " + SolarPanel.MIN_INVERTER_EFFICIENCY_PERCENTAGE + "% and less than " + SolarPanel.MAX_INVERTER_EFFICIENCY_PERCENTAGE + "%.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (solarPanelTemperatureCoefficientPmaxPercentage < -1 || solarPanelTemperatureCoefficientPmaxPercentage > 0) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Temperature coefficient of Pmax must be between -1% and 0% per Celsius degree.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (solarPanelNominalOperatingCellTemperature < 33 || solarPanelNominalOperatingCellTemperature > 58) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Nominal operating cell temperature must be between 33 and 58 Celsius degree.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else {
										addSolarPanelArrays();
										if (choice == options[0]) {
											break;
										}
									}
								}
							}
						}
					}
				}

				private void addSolarPanelArrays() {
					solarPanelArrayRowAxis = rowAxisComboBox.getSelectedIndex();
					solarPanelShadeTolerance = shadeToleranceComboBox.getSelectedIndex();
					solarPanelColorOption = colorOptionComboBox.getSelectedIndex();
					solarPanelCellType = cellTypeComboBox.getSelectedIndex();
					solarPanelModel = (String) modelComboBox.getSelectedItem();
					final SolarPanel sp = new SolarPanel();
					sp.setModelName((String) modelComboBox.getSelectedItem());
					sp.setRotated(solarPanelOrientation == 1);
					sp.setCellType(solarPanelCellType);
					sp.setColorOption(solarPanelColorOption);
					sp.setTiltAngle(solarPanelTiltAngle);
					sp.setPanelWidth(solarPanelWidth);
					sp.setPanelHeight(solarPanelHeight);
					sp.setNumberOfCellsInX(numberOfCellsInX);
					sp.setNumberOfCellsInY(numberOfCellsInY);
					sp.setBaseHeight(solarPanelArrayBaseHeight / Scene.getInstance().getAnnotationScale());
					sp.setShadeTolerance(solarPanelShadeTolerance);
					sp.setCellEfficiency(solarCellEfficiencyPercentage * 0.01);
					sp.setInverterEfficiency(inverterEfficiencyPercentage * 0.01);
					sp.setTemperatureCoefficientPmax(solarPanelTemperatureCoefficientPmaxPercentage * 0.01);
					sp.setNominalOperatingCellTemperature(solarPanelNominalOperatingCellTemperature);
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							f.addSolarPanelArrays(sp, solarPanelArrayRowSpacing, solarPanelArrayColSpacing, solarPanelArrayRowAxis);
							return null;
						}
					});
					updateAfterEdit();
				}

			});

			final JMenuItem miSolarRackArrays = new JMenuItem("Solar Panel Rack Arrays...");
			layoutMenu.add(miSolarRackArrays);
			miSolarRackArrays.addActionListener(new ActionListener() {

				private Foundation foundation;
				private JComboBox<String> modelComboBox;
				private JComboBox<String> orientationComboBox;
				private JComboBox<String> sizeComboBox;
				private JComboBox<String> cellTypeComboBox;
				private JComboBox<String> colorOptionComboBox;
				private JComboBox<String> shadeToleranceComboBox;
				private JComboBox<String> rowAxisComboBox;
				private JTextField cellEfficiencyField;
				private JTextField noctField;
				private JTextField pmaxTcField;
				private int numberOfCellsInX = 6;
				private int numberOfCellsInY = 10;

				private void enableSettings(final boolean b) {
					sizeComboBox.setEnabled(b);
					cellTypeComboBox.setEnabled(b);
					colorOptionComboBox.setEnabled(b);
					shadeToleranceComboBox.setEnabled(b);
					cellEfficiencyField.setEnabled(b);
					noctField.setEnabled(b);
					pmaxTcField.setEnabled(b);
				}

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						foundation = (Foundation) selectedPart;
						int n = foundation.countParts(Rack.class);
						if (n > 0 && JOptionPane.showConfirmDialog(MainFrame.getInstance(), "All existing " + n + " solar panel racks on this foundation must be removed before\na new layout can be applied. Do you want to continue?", "Confirmation", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
							return;
						}
						n = foundation.countParts(SolarPanel.class);
						if (n > 0 && JOptionPane.showConfirmDialog(MainFrame.getInstance(), "All existing " + n + " solar panels on this foundation must be removed before\na new layout can be applied. Do you want to continue?", "Confirmation", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
							return;
						}

						final JPanel panel = new JPanel(new SpringLayout());

						final Map<String, PvModuleSpecs> modules = PvModulesData.getInstance().getModules();
						final String[] models = new String[modules.size() + 1];
						int j = 0;
						models[j] = "Custom";
						for (final String key : modules.keySet()) {
							models[++j] = key;
						}
						panel.add(new JLabel("Solar Panel Model:"));
						modelComboBox = new JComboBox<String>(models);
						modelComboBox.setSelectedItem(solarPanelModel);
						modelComboBox.addItemListener(new ItemListener() {
							@Override
							public void itemStateChanged(final ItemEvent e) {
								if (e.getStateChange() == ItemEvent.SELECTED) {
									final boolean isCustom = modelComboBox.getSelectedIndex() == 0;
									enableSettings(isCustom);
									if (!isCustom) {
										final PvModuleSpecs specs = modules.get(modelComboBox.getSelectedItem());
										cellTypeComboBox.setSelectedItem(specs.getCellType());
										shadeToleranceComboBox.setSelectedItem(specs.getShadeTolerance());
										cellEfficiencyField.setText(threeDecimalsFormat.format(specs.getCelLEfficiency() * 100));
										noctField.setText(threeDecimalsFormat.format(specs.getNoct()));
										pmaxTcField.setText(sixDecimalsFormat.format(specs.getPmaxTc()));
										final String s = threeDecimalsFormat.format(specs.getNominalWidth()) + "m \u00D7 " + threeDecimalsFormat.format(specs.getNominalLength()) + "m (" + specs.getLayout().width + " \u00D7 " + specs.getLayout().height + " cells)";
										sizeComboBox.setSelectedItem(s);
										colorOptionComboBox.setSelectedItem(specs.getColor());
									}
								}
							}
						});
						panel.add(modelComboBox);

						panel.add(new JLabel("Solar Panel Cell Type:"));
						cellTypeComboBox = new JComboBox<String>(new String[] { "Polycrystalline", "Monocrystalline", "Thin Film" });
						cellTypeComboBox.setSelectedIndex(solarPanelCellType);
						panel.add(cellTypeComboBox);

						panel.add(new JLabel("Solar Panel Color:"));
						colorOptionComboBox = new JComboBox<String>(new String[] { "Blue", "Black", "Gray" });
						colorOptionComboBox.setSelectedIndex(solarPanelColorOption);
						panel.add(colorOptionComboBox);

						panel.add(new JLabel("Solar Panel Size:"));
						sizeComboBox = new JComboBox<String>(solarPanelNominalSize.getStrings());
						final int nItems = sizeComboBox.getItemCount();
						for (int i = 0; i < nItems; i++) {
							if (Util.isZero(solarPanelHeight - solarPanelNominalSize.getNominalHeights()[i]) && Util.isZero(solarPanelWidth - solarPanelNominalSize.getNominalWidths()[i])) {
								sizeComboBox.setSelectedIndex(i);
							}
						}
						panel.add(sizeComboBox);

						panel.add(new JLabel("Solar Cell Efficiency (%):"));
						cellEfficiencyField = new JTextField(threeDecimalsFormat.format(solarCellEfficiencyPercentage));
						panel.add(cellEfficiencyField);

						panel.add(new JLabel("<html>Nominal Operating Cell Temperature (&deg;C):"));
						noctField = new JTextField(threeDecimalsFormat.format(solarPanelNominalOperatingCellTemperature));
						panel.add(noctField);

						panel.add(new JLabel("<html>Temperature Coefficient of Pmax (%/&deg;C):"));
						pmaxTcField = new JTextField(sixDecimalsFormat.format(solarPanelTemperatureCoefficientPmaxPercentage));
						panel.add(pmaxTcField);

						panel.add(new JLabel("Shade Tolerance:"));
						shadeToleranceComboBox = new JComboBox<String>(new String[] { "Partial", "High", "None" });
						shadeToleranceComboBox.setSelectedIndex(solarPanelShadeTolerance);
						panel.add(shadeToleranceComboBox);

						panel.add(new JLabel("Inverter Efficiency (%):"));
						final JTextField inverterEfficiencyField = new JTextField(threeDecimalsFormat.format(inverterEfficiencyPercentage));
						panel.add(inverterEfficiencyField);

						panel.add(new JLabel("Tile Angle (\u00B0):"));
						final JTextField tiltAngleField = new JTextField(threeDecimalsFormat.format(solarPanelTiltAngle));
						panel.add(tiltAngleField);

						panel.add(new JLabel("Solar Panel Sub-Rows Per Rack:"));
						final JTextField rowsPerRackField = new JTextField(threeDecimalsFormat.format(solarPanelRowsPerRack));
						panel.add(rowsPerRackField);

						panel.add(new JLabel("Solar Panel Orientation:"));
						orientationComboBox = new JComboBox<String>(new String[] { "Portrait", "Landscape" });
						orientationComboBox.setSelectedIndex(solarPanelOrientation);
						panel.add(orientationComboBox);

						panel.add(new JLabel("Row Axis:"));
						rowAxisComboBox = new JComboBox<String>(new String[] { "North-South", "East-West" });
						rowAxisComboBox.setSelectedIndex(solarPanelArrayRowAxis);
						panel.add(rowAxisComboBox);

						panel.add(new JLabel("Inter-Row Center-to-Center Distance (m):"));
						final JTextField interrowSpacingField = new JTextField(threeDecimalsFormat.format(solarPanelRackArrayInterRowSpacing));
						panel.add(interrowSpacingField);

						panel.add(new JLabel("Pole Spacing X (m):"));
						final JTextField poleSpacingXField = new JTextField(threeDecimalsFormat.format(solarPanelRackPoleSpacingX));
						panel.add(poleSpacingXField);

						panel.add(new JLabel("Pole Spacing Y (m):"));
						final JTextField poleSpacingYField = new JTextField(threeDecimalsFormat.format(solarPanelRackPoleSpacingY));
						panel.add(poleSpacingYField);

						panel.add(new JLabel("Base Height (m):"));
						final JTextField baseHeightField = new JTextField(threeDecimalsFormat.format(solarPanelRackBaseHeight));
						panel.add(baseHeightField);

						SpringUtilities.makeCompactGrid(panel, 17, 2, 6, 6, 6, 6);

						enableSettings(modelComboBox.getSelectedIndex() == 0);

						final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
						final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
						final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Rack Options");

						while (true) {
							dialog.setVisible(true);
							final Object choice = optionPane.getValue();
							if (choice == options[1] || choice == null) {
								break;
							} else {
								boolean ok = true;
								try {
									solarPanelRackArrayInterRowSpacing = Double.parseDouble(interrowSpacingField.getText());
									solarPanelTiltAngle = Double.parseDouble(tiltAngleField.getText());
									solarPanelRowsPerRack = Integer.parseInt(rowsPerRackField.getText());
									solarCellEfficiencyPercentage = Double.parseDouble(cellEfficiencyField.getText());
									inverterEfficiencyPercentage = Double.parseDouble(inverterEfficiencyField.getText());
									solarPanelTemperatureCoefficientPmaxPercentage = Double.parseDouble(pmaxTcField.getText());
									solarPanelNominalOperatingCellTemperature = Double.parseDouble(noctField.getText());
									solarPanelRackPoleSpacingX = Double.parseDouble(poleSpacingXField.getText());
									solarPanelRackPoleSpacingY = Double.parseDouble(poleSpacingYField.getText());
									solarPanelRackBaseHeight = Double.parseDouble(baseHeightField.getText());
								} catch (final NumberFormatException exception) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
									ok = false;
								}
								if (ok) {
									final int i = sizeComboBox.getSelectedIndex();
									solarPanelWidth = solarPanelNominalSize.getNominalWidths()[i];
									solarPanelHeight = solarPanelNominalSize.getNominalHeights()[i];
									numberOfCellsInX = solarPanelNominalSize.getCellNx()[i];
									numberOfCellsInY = solarPanelNominalSize.getCellNy()[i];
									solarPanelOrientation = orientationComboBox.getSelectedIndex();
									final double rackHeight = (solarPanelOrientation == 0 ? solarPanelHeight : solarPanelWidth) * solarPanelRowsPerRack;
									if (solarPanelTiltAngle < -90 || solarPanelTiltAngle > 90) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Rack tilt angle must be between -90\u00B0 and 90\u00B0.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (solarPanelRackPoleSpacingX < 1 || solarPanelRackPoleSpacingX > 50) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Pole spacing X must be between 1 and 50 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (solarPanelRackBaseHeight < 0) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Base height can't be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (Math.abs(0.5 * rackHeight * Math.sin(Math.toRadians(solarPanelTiltAngle))) > solarPanelRackBaseHeight) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar panels intersect with ground.", "Geometry Error", JOptionPane.ERROR_MESSAGE);
									} else if (solarPanelRackPoleSpacingY < 1 || solarPanelRackPoleSpacingY > 50) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Pole spacing Y must be between 1 and 50 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (solarPanelRowsPerRack <= 0 || solarPanelRowsPerRack > 10) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Illegal value for solar panel rows per rack.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (solarCellEfficiencyPercentage < SolarPanel.MIN_SOLAR_CELL_EFFICIENCY_PERCENTAGE || solarCellEfficiencyPercentage > SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar cell efficiency must be between " + SolarPanel.MIN_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "% and " + SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "%.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (inverterEfficiencyPercentage < SolarPanel.MIN_INVERTER_EFFICIENCY_PERCENTAGE || inverterEfficiencyPercentage >= SolarPanel.MAX_INVERTER_EFFICIENCY_PERCENTAGE) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Inverter efficiency must be greater than " + SolarPanel.MIN_INVERTER_EFFICIENCY_PERCENTAGE + "% and less than " + SolarPanel.MAX_INVERTER_EFFICIENCY_PERCENTAGE + "%.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (solarPanelTemperatureCoefficientPmaxPercentage < -1 || solarPanelTemperatureCoefficientPmaxPercentage > 0) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Temperature coefficient of Pmax must be between -1% and 0% per Celsius degree.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (solarPanelNominalOperatingCellTemperature < 33 || solarPanelNominalOperatingCellTemperature > 58) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Nominal operating cell temperature must be between 33 and 58 Celsius degree.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (solarPanelRackArrayInterRowSpacing < rackHeight) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Inter-row center-to-center distance cannot be smaller than " + EnergyPanel.TWO_DECIMALS.format(rackHeight) + "m (" + solarPanelRowsPerRack + "\u00d7" + EnergyPanel.TWO_DECIMALS.format((solarPanelOrientation == 0 ? solarPanelHeight : solarPanelWidth)) + "m)", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else {
										addSolarRackArrays();
										if (choice == options[0]) {
											break;
										}
									}
								}
							}
						}
					}
				}

				private void addSolarRackArrays() {
					solarPanelColorOption = colorOptionComboBox.getSelectedIndex();
					solarPanelCellType = cellTypeComboBox.getSelectedIndex();
					solarPanelShadeTolerance = shadeToleranceComboBox.getSelectedIndex();
					solarPanelArrayRowAxis = rowAxisComboBox.getSelectedIndex();
					solarPanelModel = (String) modelComboBox.getSelectedItem();
					final SolarPanel sp = new SolarPanel();
					sp.setModelName((String) modelComboBox.getSelectedItem());
					sp.setRotated(solarPanelOrientation == 1);
					sp.setCellType(solarPanelCellType);
					sp.setColorOption(solarPanelColorOption);
					sp.setPanelWidth(solarPanelWidth);
					sp.setPanelHeight(solarPanelHeight);
					sp.setNumberOfCellsInX(numberOfCellsInX);
					sp.setNumberOfCellsInY(numberOfCellsInY);
					sp.setShadeTolerance(solarPanelShadeTolerance);
					sp.setCellEfficiency(solarCellEfficiencyPercentage * 0.01);
					sp.setInverterEfficiency(inverterEfficiencyPercentage * 0.01);
					sp.setTemperatureCoefficientPmax(solarPanelTemperatureCoefficientPmaxPercentage * 0.01);
					sp.setNominalOperatingCellTemperature(solarPanelNominalOperatingCellTemperature);
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							foundation.addSolarRackArrays(sp, solarPanelTiltAngle, solarPanelRackBaseHeight, solarPanelRowsPerRack, solarPanelRackArrayInterRowSpacing, solarPanelArrayRowAxis, solarPanelRackPoleSpacingX, solarPanelRackPoleSpacingY);
							return null;
						}
					});
					updateAfterEdit();
				}

			});

			layoutMenu.addSeparator();

			final JMenuItem miMirrorCircularArrays = new JMenuItem("Mirror Circular Layout...");
			layoutMenu.add(miMirrorCircularArrays);
			miMirrorCircularArrays.addActionListener(new ActionListener() {

				private Foundation f;
				private JComboBox<String> typeComboBox;

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						f = (Foundation) selectedPart;
						final int n = f.countParts(Mirror.class);
						if (n > 0 && JOptionPane.showConfirmDialog(MainFrame.getInstance(), "All existing " + n + " mirrors on this foundation must be removed before\na new layout can be applied. Do you want to continue?", "Confirmation", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
							return;
						}

						final JPanel panel = new JPanel(new SpringLayout());
						panel.add(new JLabel("Type:"));
						typeComboBox = new JComboBox<String>(new String[] { "Equal Azimuthal Spacing", "Radial Stagger" });
						typeComboBox.setSelectedIndex(mirrorCircularFieldLayout.getType());
						panel.add(typeComboBox);
						panel.add(new JLabel("Mirror Width:"));
						final JTextField widthField = new JTextField(threeDecimalsFormat.format(mirrorCircularFieldLayout.getMirrorWidth()));
						panel.add(widthField);
						panel.add(new JLabel("Mirror Height:"));
						final JTextField heightField = new JTextField(threeDecimalsFormat.format(mirrorCircularFieldLayout.getMirrorHeight()));
						panel.add(heightField);
						panel.add(new JLabel("Start Angle (CCW from East):"));
						final JTextField startAngleField = new JTextField(threeDecimalsFormat.format(mirrorCircularFieldLayout.getStartAngle()));
						panel.add(startAngleField);
						panel.add(new JLabel("End Angle (CCW from East):"));
						final JTextField endAngleField = new JTextField(threeDecimalsFormat.format(mirrorCircularFieldLayout.getEndAngle()));
						panel.add(endAngleField);
						panel.add(new JLabel("Radial Spacing:"));
						final JTextField rowSpacingField = new JTextField(threeDecimalsFormat.format(mirrorCircularFieldLayout.getRadialSpacing()));
						panel.add(rowSpacingField);
						panel.add(new JLabel("Radial Spacing Increase Ratio:"));
						final JTextField radialSpacingIncrementField = new JTextField(sixDecimalsFormat.format(mirrorCircularFieldLayout.getRadialSpacingIncrement()));
						panel.add(radialSpacingIncrementField);
						panel.add(new JLabel("Azimuthal Spacing:"));
						final JTextField azimuthalSpacingField = new JTextField(threeDecimalsFormat.format(mirrorCircularFieldLayout.getAzimuthalSpacing()));
						panel.add(azimuthalSpacingField);
						panel.add(new JLabel("Axis Road Width:"));
						final JTextField axisRoadWidthField = new JTextField(threeDecimalsFormat.format(mirrorCircularFieldLayout.getAxisRoadWidth()));
						panel.add(axisRoadWidthField);
						panel.add(new JLabel("Base Height:"));
						final JTextField baseHeightField = new JTextField(threeDecimalsFormat.format(mirrorCircularFieldLayout.getBaseHeight()));
						panel.add(baseHeightField);
						SpringUtilities.makeCompactGrid(panel, 10, 2, 6, 6, 6, 6);

						final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
						final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
						final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Circular Mirror Array Options");

						while (true) {
							dialog.setVisible(true);
							final Object choice = optionPane.getValue();
							if (choice == options[1] || choice == null) {
								break;
							} else {
								boolean ok = true;
								try {
									mirrorCircularFieldLayout.setRadialSpacing(Double.parseDouble(rowSpacingField.getText()));
									mirrorCircularFieldLayout.setRadialSpacingIncrement(Double.parseDouble(radialSpacingIncrementField.getText()));
									mirrorCircularFieldLayout.setAzimuthalSpacing(Double.parseDouble(azimuthalSpacingField.getText()));
									mirrorCircularFieldLayout.setMirrorWidth(Double.parseDouble(widthField.getText()));
									mirrorCircularFieldLayout.setMirrorHeight(Double.parseDouble(heightField.getText()));
									mirrorCircularFieldLayout.setStartAngle(Double.parseDouble(startAngleField.getText()));
									mirrorCircularFieldLayout.setEndAngle(Double.parseDouble(endAngleField.getText()));
									mirrorCircularFieldLayout.setAxisRoadWidth(Double.parseDouble(axisRoadWidthField.getText()));
									mirrorCircularFieldLayout.setBaseHeight(Double.parseDouble(baseHeightField.getText()));
								} catch (final NumberFormatException exception) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
									ok = false;
								}
								if (ok) {
									if (mirrorCircularFieldLayout.getRadialSpacing() < 0 || mirrorCircularFieldLayout.getAzimuthalSpacing() < 0) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Mirror spacing cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorCircularFieldLayout.getRadialSpacingIncrement() < 0) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Radial spacing increment ratio cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorCircularFieldLayout.getAxisRoadWidth() < 0) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Axis road width cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorCircularFieldLayout.getStartAngle() < 0 || mirrorCircularFieldLayout.getStartAngle() > 360) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Start angle must be between 0 and 360 degrees.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorCircularFieldLayout.getEndAngle() < 0 || mirrorCircularFieldLayout.getEndAngle() > 360) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "End angle must be between 0 and 360 degrees.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorCircularFieldLayout.getEndAngle() <= mirrorCircularFieldLayout.getStartAngle()) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "End angle must be greater than start angle.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorCircularFieldLayout.getMirrorWidth() < 1 || mirrorCircularFieldLayout.getMirrorWidth() > 50) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Mirror width must be between 1 and 50 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorCircularFieldLayout.getMirrorHeight() < 1 || mirrorCircularFieldLayout.getMirrorHeight() > 50) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Mirror height must be between 1 and 50 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorCircularFieldLayout.getBaseHeight() < 0) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Base height can't be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else {
										addCircularMirrorArrays();
										if (choice == options[0]) {
											break;
										}
									}
								}
							}
						}
					}
				}

				private void addCircularMirrorArrays() {
					mirrorCircularFieldLayout.setType(typeComboBox.getSelectedIndex());
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							final int count = f.addCircularMirrorArrays(mirrorCircularFieldLayout);
							if (count == 0) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Mirror array can't be created. Check your parameters.", "Error", JOptionPane.ERROR_MESSAGE);
							}
							return null;
						}
					});
					updateAfterEdit();
				}

			});

			final JMenuItem miMirrorRectangularArrays = new JMenuItem("Mirror Rectangular Layout...");
			layoutMenu.add(miMirrorRectangularArrays);
			miMirrorRectangularArrays.addActionListener(new ActionListener() {

				private Foundation f;
				private JComboBox<String> rowAxisComboBox;

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						f = (Foundation) selectedPart;
						final int n = f.countParts(Mirror.class);
						if (n > 0 && JOptionPane.showConfirmDialog(MainFrame.getInstance(), "All existing " + n + " mirrors on this foundation must be removed before\na new layout can be applied. Do you want to continue?", "Confirmation", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
							return;
						}

						final JPanel panel = new JPanel(new SpringLayout());
						panel.add(new JLabel("Row Axis:"));
						rowAxisComboBox = new JComboBox<String>(new String[] { "North-South", "East-West" });
						rowAxisComboBox.setSelectedIndex(mirrorRectangularFieldLayout.getRowAxis());
						panel.add(rowAxisComboBox);
						panel.add(new JLabel("Mirror Width:"));
						final JTextField widthField = new JTextField(threeDecimalsFormat.format(mirrorRectangularFieldLayout.getMirrorWidth()));
						panel.add(widthField);
						panel.add(new JLabel("Mirror Height:"));
						final JTextField heightField = new JTextField(threeDecimalsFormat.format(mirrorRectangularFieldLayout.getMirrorHeight()));
						panel.add(heightField);
						panel.add(new JLabel("Row Spacing:"));
						final JTextField rowSpacingField = new JTextField(threeDecimalsFormat.format(mirrorRectangularFieldLayout.getRowSpacing()));
						panel.add(rowSpacingField);
						panel.add(new JLabel("Column Spacing:"));
						final JTextField columnSpacingField = new JTextField(threeDecimalsFormat.format(mirrorRectangularFieldLayout.getColumnSpacing()));
						panel.add(columnSpacingField);
						panel.add(new JLabel("Base Height:"));
						final JTextField baseHeightField = new JTextField(threeDecimalsFormat.format(mirrorRectangularFieldLayout.getBaseHeight()));
						panel.add(baseHeightField);
						SpringUtilities.makeCompactGrid(panel, 6, 2, 6, 6, 6, 6);

						final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
						final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
						final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Rectangular Mirror Array Options");

						while (true) {
							dialog.setVisible(true);
							final Object choice = optionPane.getValue();
							if (choice == options[1] || choice == null) {
								break;
							} else {
								boolean ok = true;
								try {
									mirrorRectangularFieldLayout.setRowSpacing(Double.parseDouble(rowSpacingField.getText()));
									mirrorRectangularFieldLayout.setColumnSpacing(Double.parseDouble(columnSpacingField.getText()));
									mirrorRectangularFieldLayout.setMirrorWidth(Double.parseDouble(widthField.getText()));
									mirrorRectangularFieldLayout.setMirrorHeight(Double.parseDouble(heightField.getText()));
									mirrorRectangularFieldLayout.setBaseHeight(Double.parseDouble(baseHeightField.getText()));
								} catch (final NumberFormatException exception) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
									ok = false;
								}
								if (ok) {
									if (mirrorRectangularFieldLayout.getRowSpacing() < 0 || mirrorRectangularFieldLayout.getColumnSpacing() < 0) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Mirror spacing cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorRectangularFieldLayout.getMirrorWidth() < 1 || mirrorRectangularFieldLayout.getMirrorWidth() > 50) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Mirror width must be between 1 and 50 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorRectangularFieldLayout.getMirrorHeight() < 1 || mirrorRectangularFieldLayout.getMirrorHeight() > 50) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Mirror height must be between 1 and 50 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorRectangularFieldLayout.getBaseHeight() < 0) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Base height can't be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else {
										addRectangularMirrorArrays();
										if (choice == options[0]) {
											break;
										}
									}
								}
							}
						}
					}
				}

				private void addRectangularMirrorArrays() {
					mirrorRectangularFieldLayout.setRowAxis(rowAxisComboBox.getSelectedIndex());
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							final int count = f.addRectangularMirrorArrays(mirrorRectangularFieldLayout);
							if (count == 0) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Mirror array can't be created. Check your parameters.", "Error", JOptionPane.ERROR_MESSAGE);
							}
							return null;
						}
					});
					updateAfterEdit();
				}

			});

			final JMenuItem miMirrorFermatSpiralArrays = new JMenuItem("Mirror Spiral Layout...");
			layoutMenu.add(miMirrorFermatSpiralArrays);
			miMirrorFermatSpiralArrays.addActionListener(new ActionListener() {

				private Foundation f;
				private JComboBox<String> typeComboBox;

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						f = (Foundation) selectedPart;
						final int n = f.countParts(Mirror.class);
						if (n > 0 && JOptionPane.showConfirmDialog(MainFrame.getInstance(), "All existing " + n + " mirrors on this foundation must be removed before\na new layout can be applied. Do you want to continue?", "Confirmation", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
							return;
						}

						final JPanel panel = new JPanel(new SpringLayout());
						panel.add(new JLabel("Type:"));
						typeComboBox = new JComboBox<String>(new String[] { "Fermat Spiral" });
						typeComboBox.setSelectedIndex(mirrorSpiralFieldLayout.getType());
						panel.add(typeComboBox);
						panel.add(new JLabel("Mirror Width:"));
						final JTextField widthField = new JTextField(threeDecimalsFormat.format(mirrorSpiralFieldLayout.getMirrorWidth()));
						panel.add(widthField);
						panel.add(new JLabel("Mirror Height:"));
						final JTextField heightField = new JTextField(threeDecimalsFormat.format(mirrorSpiralFieldLayout.getMirrorHeight()));
						panel.add(heightField);
						panel.add(new JLabel("Start Turn:"));
						final JTextField startTurnField = new JTextField(mirrorSpiralFieldLayout.getStartTurn() + "");
						panel.add(startTurnField);
						panel.add(new JLabel("Scaling Factor:"));
						final JTextField scalingFactorField = new JTextField(threeDecimalsFormat.format(mirrorSpiralFieldLayout.getScalingFactor()));
						panel.add(scalingFactorField);
						panel.add(new JLabel("Radial Spacing Increase Ratio:"));
						final JTextField radialSpacingIncrementField = new JTextField(sixDecimalsFormat.format(mirrorSpiralFieldLayout.getRadialSpacingIncrement()));
						panel.add(radialSpacingIncrementField);
						panel.add(new JLabel("Start Angle (CCW from East):"));
						final JTextField startAngleField = new JTextField(threeDecimalsFormat.format(mirrorSpiralFieldLayout.getStartAngle()));
						panel.add(startAngleField);
						panel.add(new JLabel("End Angle (CCW from East):"));
						final JTextField endAngleField = new JTextField(threeDecimalsFormat.format(mirrorSpiralFieldLayout.getEndAngle()));
						panel.add(endAngleField);
						panel.add(new JLabel("Axis Road Width:"));
						final JTextField axisRoadWidthField = new JTextField(threeDecimalsFormat.format(mirrorSpiralFieldLayout.getAxisRoadWidth()));
						panel.add(axisRoadWidthField);
						panel.add(new JLabel("Base Height:"));
						final JTextField baseHeightField = new JTextField(threeDecimalsFormat.format(mirrorSpiralFieldLayout.getBaseHeight()));
						panel.add(baseHeightField);
						SpringUtilities.makeCompactGrid(panel, 10, 2, 6, 6, 6, 6);

						final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
						final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
						final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Spiral Mirror Array Options");

						while (true) {
							dialog.setVisible(true);
							final Object choice = optionPane.getValue();
							if (choice == options[1] || choice == null) {
								break;
							} else {
								boolean ok = true;
								try {
									mirrorSpiralFieldLayout.setMirrorWidth(Double.parseDouble(widthField.getText()));
									mirrorSpiralFieldLayout.setMirrorHeight(Double.parseDouble(heightField.getText()));
									mirrorSpiralFieldLayout.setStartTurn(Integer.parseInt(startTurnField.getText()));
									mirrorSpiralFieldLayout.setScalingFactor(Double.parseDouble(scalingFactorField.getText()));
									mirrorSpiralFieldLayout.setRadialSpacingIncrement(Double.parseDouble(radialSpacingIncrementField.getText()));
									mirrorSpiralFieldLayout.setStartAngle(Double.parseDouble(startAngleField.getText()));
									mirrorSpiralFieldLayout.setEndAngle(Double.parseDouble(endAngleField.getText()));
									mirrorSpiralFieldLayout.setAxisRoadWidth(Double.parseDouble(axisRoadWidthField.getText()));
									mirrorSpiralFieldLayout.setBaseHeight(Double.parseDouble(baseHeightField.getText()));
								} catch (final NumberFormatException exception) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
									ok = false;
								}
								if (ok) {
									if (mirrorSpiralFieldLayout.getStartTurn() < 0) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Start turn cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorSpiralFieldLayout.getScalingFactor() <= 0) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Scaling factor must be greater than zero.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorSpiralFieldLayout.getRadialSpacingIncrement() < 0) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Radial spacing increment ratio cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorSpiralFieldLayout.getAxisRoadWidth() < 0) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Axis road width cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorSpiralFieldLayout.getStartAngle() < 0 || mirrorSpiralFieldLayout.getStartAngle() > 360) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Start angle must be between 0 and 360 degrees.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorSpiralFieldLayout.getEndAngle() < 0 || mirrorSpiralFieldLayout.getEndAngle() > 360) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "End angle must be between 0 and 360 degrees.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorSpiralFieldLayout.getEndAngle() <= mirrorSpiralFieldLayout.getStartAngle()) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "End angle must be greater than start angle.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorSpiralFieldLayout.getMirrorWidth() < 1 || mirrorSpiralFieldLayout.getMirrorWidth() > 50) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Mirror width must be between 1 and 50 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorSpiralFieldLayout.getMirrorHeight() < 1 || mirrorSpiralFieldLayout.getMirrorHeight() > 50) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Mirror height must be between 1 and 50 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else if (mirrorSpiralFieldLayout.getBaseHeight() < 0) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Base height can't be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
									} else {
										addSpiralMirrorArrays();
										if (choice == options[0]) {
											break;
										}
									}
								}
							}
						}
					}
				}

				private void addSpiralMirrorArrays() {
					mirrorSpiralFieldLayout.setType(typeComboBox.getSelectedIndex());
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							final int count = f.addSpiralMirrorArrays(mirrorSpiralFieldLayout);
							if (count == 0) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Mirror array can't be created. Check your parameters.", "Error", JOptionPane.ERROR_MESSAGE);
							}
							return null;
						}
					});
					updateAfterEdit();
				}

			});

			final JMenuItem miAddUtilityBill = new JMenuItem("Add Utility Bill");
			miAddUtilityBill.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						UtilityBill b = f.getUtilityBill();
						if (b == null) {
							if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "No utility bill is found for this building. Create one?", "Utility Bill for Building #" + f.getId(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
								return;
							}
							b = new UtilityBill();
							f.setUtilityBill(b);
						}
						new UtilityBillDialog(b).setVisible(true);
						Scene.getInstance().setEdited(true);
					}
				}
			});

			final JMenuItem miDeleteUtilityBill = new JMenuItem("Delete Utility Bill");
			miDeleteUtilityBill.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						if (f.getUtilityBill() == null) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no utilitiy bill associated with this building.", "No Utility Bill", JOptionPane.INFORMATION_MESSAGE);
						} else {
							if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove the utility bill associated with this building?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
								final DeleteUtilityBillCommand c = new DeleteUtilityBillCommand(f);
								f.setUtilityBill(null);
								Scene.getInstance().setEdited(true);
								SceneManager.getInstance().getUndoManager().addEdit(c);
							}
						}
					}
				}
			});

			final JMenuItem miGroupMaster = new JCheckBoxMenuItem("Group Master");
			miGroupMaster.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						SceneManager.getInstance().getUndoManager().addEdit(new SetGroupMasterCommand((Foundation) selectedPart));
						((Foundation) selectedPart).setGroupMaster(miGroupMaster.isSelected());
						Scene.getInstance().setEdited(true);
					}
				}
			});

			final JCheckBoxMenuItem miBorderLine = new JCheckBoxMenuItem("Border Line");
			miBorderLine.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation foundation = (Foundation) selectedPart;
						SceneManager.getInstance().getUndoManager().addEdit(new ShowBorderLineCommand(foundation));
						foundation.getPolygon().setVisible(miBorderLine.isSelected());
						foundation.draw();
						Scene.getInstance().setEdited(true);
					}
				}
			});

			final JCheckBoxMenuItem miDisableEditPoints = new JCheckBoxMenuItem("Disable Edit Points");
			miDisableEditPoints.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						((Foundation) selectedPart).setLockEdit(miDisableEditPoints.isSelected());
						Scene.getInstance().setEdited(true);
					}
				}
			});

			final JMenu optionsMenu = new JMenu("Options");

			final JMenuItem miChildGridSize = new JMenuItem("Grid Size...");
			miChildGridSize.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Foundation)) {
						return;
					}
					final Foundation f = (Foundation) selectedPart;
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), "Grid Size (m)", f.getChildGridSize() * Scene.getInstance().getAnnotationScale());
						if (newValue == null) {
							break;
						} else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val < 0.1 || val > 5) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Grid size must be between 0.1 and 5 m.", "Error", JOptionPane.ERROR_MESSAGE);
								} else {
									f.setChildGridSize(val / Scene.getInstance().getAnnotationScale());
									updateAfterEdit();
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
			optionsMenu.add(miChildGridSize);

			final JMenu projectTypeSubMenu = new JMenu("Project Type");
			optionsMenu.add(projectTypeSubMenu);

			final ButtonGroup bgStructureTypes = new ButtonGroup();

			final JRadioButtonMenuItem rbmiTypeAutoDetected = new JRadioButtonMenuItem("Auto Detected");
			rbmiTypeAutoDetected.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation foundation = (Foundation) selectedPart;
						foundation.setProjectType(Foundation.TYPE_AUTO_DETECTED);
					}
				}
			});
			projectTypeSubMenu.add(rbmiTypeAutoDetected);
			bgStructureTypes.add(rbmiTypeAutoDetected);

			final JRadioButtonMenuItem rbmiTypeBuilding = new JRadioButtonMenuItem("Building");
			rbmiTypeBuilding.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation foundation = (Foundation) selectedPart;
						foundation.setProjectType(Foundation.TYPE_BUILDING);
					}
				}
			});
			projectTypeSubMenu.add(rbmiTypeBuilding);
			bgStructureTypes.add(rbmiTypeBuilding);

			final JRadioButtonMenuItem rbmiTypePvStation = new JRadioButtonMenuItem("Photovoltaic Solar Power System");
			rbmiTypePvStation.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation foundation = (Foundation) selectedPart;
						foundation.setProjectType(Foundation.TYPE_PV_PROJECT);
					}
				}
			});
			projectTypeSubMenu.add(rbmiTypePvStation);
			bgStructureTypes.add(rbmiTypePvStation);

			final JRadioButtonMenuItem rbmiTypeCspStation = new JRadioButtonMenuItem("Concentrated Solar Power System");
			rbmiTypeCspStation.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation foundation = (Foundation) selectedPart;
						foundation.setProjectType(Foundation.TYPE_CSP_PROJECT);
					}
				}
			});
			projectTypeSubMenu.add(rbmiTypeCspStation);
			bgStructureTypes.add(rbmiTypeCspStation);

			final JMenuItem miThermostat = new JMenuItem("Thermostat...");
			miThermostat.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation foundation = (Foundation) selectedPart;
						MainPanel.getInstance().getEnergyViewButton().setSelected(false);
						new ThermostatDialog(foundation).setVisible(true);
						TimeSeriesLogger.getInstance().logAdjustThermostatButton();
						Scene.getInstance().setEdited(true);
					}
				}
			});

			final JMenuItem miSize = new JMenuItem("Size...");
			miSize.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Foundation)) {
						return;
					}
					final Foundation f = (Foundation) selectedPart;
					final boolean hasChildren = !f.getChildren().isEmpty();
					final Vector3 v0 = f.getAbsPoint(0);
					final Vector3 v1 = f.getAbsPoint(1);
					final Vector3 v2 = f.getAbsPoint(2);
					double lx0 = v0.distance(v2) * Scene.getInstance().getAnnotationScale();
					double ly0 = v0.distance(v1) * Scene.getInstance().getAnnotationScale();
					double lz0 = f.getHeight() * Scene.getInstance().getAnnotationScale();

					final JPanel gui = new JPanel(new BorderLayout());
					final String title = "<html>Size of Foundation #" + f.getId() + " (in meters)</html>";
					gui.add(new JLabel(title), BorderLayout.NORTH);
					final JPanel inputPanel = new JPanel(new SpringLayout());
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					gui.add(inputPanel, BorderLayout.CENTER);
					JLabel l = new JLabel("Length: ", JLabel.TRAILING);
					inputPanel.add(l);
					final JTextField lxField = new JTextField(threeDecimalsFormat.format(lx0), 5);
					lxField.setEditable(!hasChildren);
					l.setLabelFor(lxField);
					inputPanel.add(lxField);
					l = new JLabel("Width: ", JLabel.TRAILING);
					inputPanel.add(l);
					final JTextField lyField = new JTextField(threeDecimalsFormat.format(ly0), 5);
					lyField.setEditable(!hasChildren);
					l.setLabelFor(lyField);
					inputPanel.add(lyField);
					l = new JLabel("Height: ", JLabel.TRAILING);
					inputPanel.add(l);
					final JTextField lzField = new JTextField(threeDecimalsFormat.format(lz0), 5);
					l.setLabelFor(lzField);
					inputPanel.add(lzField);
					SpringUtilities.makeCompactGrid(inputPanel, 3, 2, 6, 6, 6, 6);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(gui, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Foundation Size");
					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1] || choice == null) {
							break;
						} else {
							double lx1 = lx0, ly1 = ly0, lz1 = lz0;
							boolean ok = true;
							try {
								lx1 = Double.parseDouble(lxField.getText());
								ly1 = Double.parseDouble(lyField.getText());
								lz1 = Double.parseDouble(lzField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (lx1 < 1) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Length must be greater than one.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (ly1 < 1) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Width must be greater than one.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (lz1 < 0.01) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Height must be greater than 0.01.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									f.rescale(lx1 / lx0, ly1 / ly0, 1);
									f.setHeight(lz1 / Scene.getInstance().getAnnotationScale());
									f.draw();
									f.drawChildren();
									SceneManager.getInstance().refresh();
									SceneManager.getInstance().getUndoManager().addEdit(new ChangeFoundationSizeCommand(f, lx0, lx1, ly0, ly1, lz0, lz1));
									updateAfterEdit();
									lx0 = lx1;
									ly0 = ly1;
									lz0 = lz1;
									if (choice == options[0]) {
										break;
									}
								}
							}

						}
					}

				}
			});

			final JMenuItem miResize = new JMenuItem("Resize Structure Above");
			miResize.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Foundation)) {
						return;
					}
					final Foundation f = (Foundation) selectedPart;
					if (f.getChildren().isEmpty()) {
						return;
					}
					for (final HousePart p : Scene.getInstance().getParts()) {
						if (p instanceof Foundation) {
							if (p != f) {
								((Foundation) p).setResizeHouseMode(false);
							}
						}
					}
					f.setResizeHouseMode(true);
				}
			});

			final JMenu labelMenu = new JMenu("Label");

			final JCheckBoxMenuItem miLabelNone = new JCheckBoxMenuItem("None", true);
			miLabelNone.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (miLabelNone.isSelected()) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Foundation) {
							final Foundation f = (Foundation) selectedPart;
							final SetFoundationLabelCommand c = new SetFoundationLabelCommand(f);
							f.clearLabels();
							f.draw();
							SceneManager.getInstance().getUndoManager().addEdit(c);
							Scene.getInstance().setEdited(true);
							SceneManager.getInstance().refresh();
						}
					}
				}
			});
			labelMenu.add(miLabelNone);

			final JCheckBoxMenuItem miLabelCustom = new JCheckBoxMenuItem("Custom");
			miLabelCustom.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final SetFoundationLabelCommand c = new SetFoundationLabelCommand(f);
						f.setLabelCustom(miLabelCustom.isSelected());
						if (f.getLabelCustom()) {
							f.setLabelCustomText(JOptionPane.showInputDialog(MainFrame.getInstance(), "Custom Text", f.getLabelCustomText()));
						}
						f.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelCustom);

			final JCheckBoxMenuItem miLabelId = new JCheckBoxMenuItem("ID");
			miLabelId.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final SetFoundationLabelCommand c = new SetFoundationLabelCommand(f);
						f.setLabelId(miLabelId.isSelected());
						f.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelId);

			final JCheckBoxMenuItem miLabelNumberOfSolarPanels = new JCheckBoxMenuItem("Number of Solar Panels");
			miLabelNumberOfSolarPanels.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final SetFoundationLabelCommand c = new SetFoundationLabelCommand(f);
						f.setLabelNumberOfSolarPanels(miLabelNumberOfSolarPanels.isSelected());
						f.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelNumberOfSolarPanels);

			final JCheckBoxMenuItem miLabelPvEnergy = new JCheckBoxMenuItem("Photovoltaic Output");
			miLabelPvEnergy.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final SetFoundationLabelCommand c = new SetFoundationLabelCommand(f);
						f.setLabelPvEnergy(miLabelPvEnergy.isSelected());
						f.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelPvEnergy);

			final JCheckBoxMenuItem miLabelSolarPotential = new JCheckBoxMenuItem("Solar Potential");
			miLabelSolarPotential.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final SetFoundationLabelCommand c = new SetFoundationLabelCommand(f);
						f.setLabelSolarPotential(miLabelSolarPotential.isSelected());
						f.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelSolarPotential);

			final JCheckBoxMenuItem miLabelBuildingEnergy = new JCheckBoxMenuItem("Building Energy");
			miLabelBuildingEnergy.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final SetFoundationLabelCommand c = new SetFoundationLabelCommand(f);
						f.setLabelBuildingEnergy(miLabelBuildingEnergy.isSelected());
						f.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelBuildingEnergy);

			final JMenu powerTowerLabelMenu = new JMenu("Power Tower");
			labelMenu.add(powerTowerLabelMenu);

			final JCheckBoxMenuItem miLabelNumberOfMirrors = new JCheckBoxMenuItem("Number of Mirrors");
			miLabelNumberOfMirrors.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final SetFoundationLabelCommand c = new SetFoundationLabelCommand(f);
						f.setLabelNumberOfMirrors(miLabelNumberOfMirrors.isSelected());
						f.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			powerTowerLabelMenu.add(miLabelNumberOfMirrors);

			final JCheckBoxMenuItem miLabelPowerTowerHeight = new JCheckBoxMenuItem("Tower Height");
			miLabelPowerTowerHeight.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final SetFoundationLabelCommand c = new SetFoundationLabelCommand(f);
						f.setLabelPowerTowerHeight(miLabelPowerTowerHeight.isSelected());
						f.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			powerTowerLabelMenu.add(miLabelPowerTowerHeight);

			final JCheckBoxMenuItem miLabelPowerTowerOutput = new JCheckBoxMenuItem("Energy Output");
			miLabelPowerTowerOutput.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final SetFoundationLabelCommand c = new SetFoundationLabelCommand(f);
						f.setLabelPowerTowerOutput(miLabelPowerTowerOutput.isSelected());
						f.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			powerTowerLabelMenu.add(miLabelPowerTowerOutput);

			popupMenuForFoundation = createPopupMenu(false, true, new Runnable() {
				@Override
				public void run() {
					final HousePart p = SceneManager.getInstance().getSelectedPart();
					if (p instanceof Foundation) {
						final Foundation f = (Foundation) p;
						if (Scene.getInstance().isStudentMode()) {
							miDisableEditPoints.setEnabled(false);
							miThermostat.setEnabled(false);
						} else {
							miDisableEditPoints.setEnabled(true);
							miThermostat.setEnabled(true);
						}
						miDeleteUtilityBill.setEnabled(f.getUtilityBill() != null);
						Util.selectSilently(miGroupMaster, f.isGroupMaster());
						Util.selectSilently(miDisableEditPoints, f.getLockEdit());
						Util.selectSilently(miBorderLine, f.getPolygon().isVisible());
						Util.selectSilently(miLabelNone, !f.isLabelVisible());
						Util.selectSilently(miLabelCustom, f.getLabelCustom());
						Util.selectSilently(miLabelId, f.getLabelId());
						Util.selectSilently(miLabelPowerTowerOutput, f.getLabelPowerTowerOutput());
						Util.selectSilently(miLabelPowerTowerHeight, f.getLabelPowerTowerHeight());
						Util.selectSilently(miLabelNumberOfMirrors, f.getLabelNumberOfMirrors());
						Util.selectSilently(miLabelSolarPotential, f.getLabelSolarPotential());
						Util.selectSilently(miLabelPvEnergy, f.getLabelPvEnergy());
						Util.selectSilently(miLabelNumberOfSolarPanels, f.getLabelNumberOfSolarPanels());
						Util.selectSilently(miLabelBuildingEnergy, f.getLabelBuildingEnergy());
						powerTowerLabelMenu.setEnabled(f.hasSolarReceiver());
						switch (f.getProjectType()) {
						case Foundation.TYPE_BUILDING:
							Util.selectSilently(rbmiTypeBuilding, true);
							break;
						case Foundation.TYPE_PV_PROJECT:
							Util.selectSilently(rbmiTypePvStation, true);
							break;
						case Foundation.TYPE_CSP_PROJECT:
							Util.selectSilently(rbmiTypeCspStation, true);
							break;
						default:
							Util.selectSilently(rbmiTypeAutoDetected, true);
						}
						miResize.setEnabled(!f.getChildren().isEmpty());
						for (final HousePart x : Scene.getInstance().getParts()) {
							if (x instanceof Foundation) {
								if (x != f) {
									((Foundation) x).setResizeHouseMode(false);
								}
							}
						}
					}
					final HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
					final Node copyNode = Scene.getInstance().getCopyNode();
					miPaste.setEnabled(copyBuffer instanceof SolarCollector || copyNode != null);
				}
			});

			popupMenuForFoundation.add(miPaste);
			popupMenuForFoundation.add(miCopy);
			popupMenuForFoundation.addSeparator();
			popupMenuForFoundation.add(miImportCollada);
			popupMenuForFoundation.add(miResize);
			popupMenuForFoundation.add(miSize);
			popupMenuForFoundation.add(miRescale);
			popupMenuForFoundation.add(rotateMenu);
			popupMenuForFoundation.add(clearMenu);
			popupMenuForFoundation.add(layoutMenu);
			popupMenuForFoundation.addSeparator();
			popupMenuForFoundation.add(miDisableEditPoints);
			popupMenuForFoundation.add(miGroupMaster);
			popupMenuForFoundation.add(miBorderLine);
			popupMenuForFoundation.add(optionsMenu);
			popupMenuForFoundation.add(labelMenu);
			popupMenuForFoundation.addSeparator();
			popupMenuForFoundation.add(colorAction);
			// floor insulation only for the first floor, so this U-value is associated with the Foundation class, not the Floor class
			popupMenuForFoundation.add(createInsulationMenuItem(false));
			popupMenuForFoundation.add(createVolumetricHeatCapacityMenuItem());
			popupMenuForFoundation.add(miThermostat);
			popupMenuForFoundation.addSeparator();
			popupMenuForFoundation.add(miAddUtilityBill);
			popupMenuForFoundation.add(miDeleteUtilityBill);
			popupMenuForFoundation.addSeparator();

			final JMenu analysisMenu = new JMenu("Analysis");
			popupMenuForFoundation.add(analysisMenu);

			JMenu subMenu = new JMenu("Buildings");
			analysisMenu.add(subMenu);

			JMenuItem mi = new JMenuItem("Daily Building Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Foundation) {
						final EnergyDailyAnalysis analysis = new EnergyDailyAnalysis();
						if (SceneManager.getInstance().getSolarHeatMap()) {
							analysis.updateGraph();
						}
						analysis.show("Daily Building Energy");
					}
				}
			});
			subMenu.add(mi);

			mi = new JMenuItem("Annual Building Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Foundation) {
						new EnergyAnnualAnalysis().show("Annual Building Energy");
					}
				}
			});
			subMenu.add(mi);

			subMenu = new JMenu("Solar Panels");
			analysisMenu.add(subMenu);

			mi = new JMenuItem("Daily Solar Panel Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Foundation) {
						final Foundation f = (Foundation) SceneManager.getInstance().getSelectedPart();
						if (f.countParts(new Class[] { SolarPanel.class, Rack.class }) <= 0) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no solar panel on this foundation to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						final PvDailyAnalysis a = new PvDailyAnalysis();
						if (SceneManager.getInstance().getSolarHeatMap()) {
							a.updateGraph();
						}
						a.show();
					}
				}
			});
			subMenu.add(mi);

			mi = new JMenuItem("Annual Solar Panel Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						if (f.countParts(new Class[] { SolarPanel.class, Rack.class }) <= 0) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no solar panel on this foundation to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						final PvAnnualAnalysis a = new PvAnnualAnalysis();
						if (f.getUtilityBill() != null) {
							a.setUtilityBill(f.getUtilityBill());
						}
						a.show();
					}
				}
			});
			subMenu.add(mi);

			subMenu = new JMenu("Mirrors (Heliostats)");
			analysisMenu.add(subMenu);

			mi = new JMenuItem("Daily Mirror Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Foundation) {
						final Foundation f = (Foundation) SceneManager.getInstance().getSelectedPart();
						if (f.countParts(Mirror.class) <= 0) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no mirror on this foundation to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						final MirrorDailyAnalysis a = new MirrorDailyAnalysis();
						if (SceneManager.getInstance().getSolarHeatMap()) {
							a.updateGraph();
						}
						a.show();
					}
				}
			});
			subMenu.add(mi);

			mi = new JMenuItem("Annual Mirror Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						if (f.countParts(Mirror.class) <= 0) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no mirror on this foundation to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						new MirrorAnnualAnalysis().show();
					}
				}
			});
			subMenu.add(mi);

			subMenu = new JMenu("Parabolic Troughs");
			analysisMenu.add(subMenu);

			mi = new JMenuItem("Daily Parabolic Trough Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Foundation) {
						final Foundation f = (Foundation) SceneManager.getInstance().getSelectedPart();
						if (f.countParts(ParabolicTrough.class) <= 0) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no parabolic trough on this foundation to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						final ParabolicTroughDailyAnalysis a = new ParabolicTroughDailyAnalysis();
						if (SceneManager.getInstance().getSolarHeatMap()) {
							a.updateGraph();
						}
						a.show();
					}
				}
			});
			subMenu.add(mi);

			mi = new JMenuItem("Annual Parabolic Trough Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						if (f.countParts(ParabolicTrough.class) <= 0) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no parabolic trough on this foundation to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						new ParabolicTroughAnnualAnalysis().show();
					}
				}
			});
			subMenu.add(mi);

			subMenu = new JMenu("Parabolic Dishes");
			analysisMenu.add(subMenu);

			mi = new JMenuItem("Daily Parabolic Dish Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Foundation) {
						final Foundation f = (Foundation) SceneManager.getInstance().getSelectedPart();
						if (f.countParts(ParabolicDish.class) <= 0) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no parabolic dish on this foundation to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						final ParabolicDishDailyAnalysis a = new ParabolicDishDailyAnalysis();
						if (SceneManager.getInstance().getSolarHeatMap()) {
							a.updateGraph();
						}
						a.show();
					}
				}
			});
			subMenu.add(mi);

			mi = new JMenuItem("Annual Parabolic Dish Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						if (f.countParts(ParabolicDish.class) <= 0) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no parabolic dish on this foundation to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						new ParabolicDishAnnualAnalysis().show();
					}
				}
			});
			subMenu.add(mi);

			subMenu = new JMenu("Linear Fresnel Reflectors");
			analysisMenu.add(subMenu);

			mi = new JMenuItem("Daily Fresnel Reflector Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Foundation) {
						final Foundation f = (Foundation) SceneManager.getInstance().getSelectedPart();
						if (f.countParts(FresnelReflector.class) <= 0) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no Fresnel reflector on this foundation to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						final FresnelReflectorDailyAnalysis a = new FresnelReflectorDailyAnalysis();
						if (SceneManager.getInstance().getSolarHeatMap()) {
							a.updateGraph();
						}
						a.show();
					}
				}
			});
			subMenu.add(mi);

			mi = new JMenuItem("Annual Fresnel Reflector Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						if (f.countParts(FresnelReflector.class) <= 0) {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no Fresnel reflector on this foundation to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						new FresnelReflectorAnnualAnalysis().show();
					}
				}
			});
			subMenu.add(mi);

		}

		return popupMenuForFoundation;

	}

}
