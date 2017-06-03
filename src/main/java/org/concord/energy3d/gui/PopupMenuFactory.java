package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.MirrorCircularFieldLayout;
import org.concord.energy3d.model.MirrorRectangularFieldLayout;
import org.concord.energy3d.model.MirrorSpiralFieldLayout;
import org.concord.energy3d.model.NodeState;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Thermalizable;
import org.concord.energy3d.model.Trackable;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.UserData;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.AnnualGraph;
import org.concord.energy3d.simulation.Cost;
import org.concord.energy3d.simulation.EnergyAnnualAnalysis;
import org.concord.energy3d.simulation.EnergyDailyAnalysis;
import org.concord.energy3d.simulation.MirrorAnnualAnalysis;
import org.concord.energy3d.simulation.MirrorDailyAnalysis;
import org.concord.energy3d.simulation.PvAnnualAnalysis;
import org.concord.energy3d.simulation.PvDailyAnalysis;
import org.concord.energy3d.simulation.UtilityBill;
import org.concord.energy3d.undo.*;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.FileChooser;
import org.concord.energy3d.util.SpringUtilities;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;

/**
 * Pop-up menus for customizing individual elements.
 *
 * @author Charles Xie
 *
 */

public class PopupMenuFactory {

	private static DecimalFormat integerFormat = new DecimalFormat();
	private static DecimalFormat threeDecimalsFormat = new DecimalFormat();
	private static DecimalFormat sixDecimalsFormat = new DecimalFormat();

	static {
		integerFormat.setMaximumFractionDigits(0);
		threeDecimalsFormat.setMaximumFractionDigits(3);
		sixDecimalsFormat.setMaximumFractionDigits(6);
	}

	private static JPopupMenu popupMenuForWindow;
	private static JPopupMenu popupMenuForWall;
	private static JPopupMenu popupMenuForRoof;
	private static JPopupMenu popupMenuForDoor;
	private static JPopupMenu popupMenuForFloor;
	private static JPopupMenu popupMenuForTree;
	private static JPopupMenu popupMenuForHuman;
	private static JPopupMenu popupMenuForFoundation;
	private static JPopupMenu popupMenuForSolarPanel;
	private static JPopupMenu popupMenuForRack;
	private static JPopupMenu popupMenuForMirror;
	private static JPopupMenu popupMenuForSensor;
	private static JPopupMenu popupMenuForLand;
	private static JPopupMenu popupMenuForSky;
	private static JPopupMenu popupMenuForMesh;

	// cached values
	private static JColorChooser colorChooser = new JColorChooser();
	private static double solarPanelArrayBaseHeight = 1;
	private static int solarPanelArrayRowAxis = 0;
	private static double solarPanelWidth = 0.99;
	private static double solarPanelHeight = 1.96;
	private static double solarPanelArrayRowSpacing = solarPanelHeight + 1;
	private static double solarPanelArrayColSpacing = solarPanelWidth + 1;
	private static int solarPanelOrientation = 0;
	private static int solarPanelColorOption = SolarPanel.COLOR_OPTION_BLUE;
	private static int solarPanelCellType = SolarPanel.MONOCRYSTALLINE;
	private static int solarPanelRowsPerRack = 3;
	private static double solarPanelRackArrayInterRowSpacing = solarPanelHeight * solarPanelRowsPerRack + 1;
	private static double solarPanelRackPoleSpacingX = 4;
	private static double solarPanelRackPoleSpacingY = 2;
	private static double solarPanelRackBaseHeight = 3;
	private static double solarPanelTiltAngle = 0;
	private static double solarCellEfficiencyPercentage = 15;
	private static double inverterEfficiencyPercentage = 95;
	private static int solarPanelShadeTolerance = SolarPanel.PARTIAL_SHADE_TOLERANCE;
	private static double solarPanelTemperatureCoefficientPmaxPercentage = -0.5;
	private static double solarPanelNominalOperatingCellTemperature = 48;
	private static MirrorRectangularFieldLayout mirrorRectangularFieldLayout = new MirrorRectangularFieldLayout();
	private static MirrorCircularFieldLayout mirrorCircularFieldLayout = new MirrorCircularFieldLayout();
	private static MirrorSpiralFieldLayout mirrorSpiralFieldLayout = new MirrorSpiralFieldLayout();

	private static Action colorAction = new AbstractAction("Color...") {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent e) {
			MainFrame.getInstance().showColorDialogForParts();
		}
	};

	private PopupMenuFactory() {
	}

	public static JPopupMenu getPopupMenu(final boolean onLand) {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart instanceof Window) {
			return getPopupMenuForWindow();
		}
		if (selectedPart instanceof Wall) {
			return getPopupMenuForWall();
		}
		if (selectedPart instanceof Roof) {
			return getPopupMenuForRoof();
		}
		if (selectedPart instanceof Door) {
			return getPopupMenuForDoor();
		}
		if (selectedPart instanceof Floor) {
			return getPopupMenuForFloor();
		}
		if (selectedPart instanceof Foundation) {
			final Foundation f = (Foundation) selectedPart;
			if (f.getSelectedMesh() != null) {
				return getPopupMenuForMesh();
			}
			return getPopupMenuForFoundation();
		}
		if (selectedPart instanceof SolarPanel) {
			return getPopupMenuForSolarPanel();
		}
		if (selectedPart instanceof Rack) {
			return getPopupMenuForRack();
		}
		if (selectedPart instanceof Mirror) {
			return getPopupMenuForMirror();
		}
		if (selectedPart instanceof Sensor) {
			return getPopupMenuForSensor();
		}
		if (selectedPart instanceof Tree) {
			return getPopupMenuForTree();
		}
		if (selectedPart instanceof Human) {
			return getPopupMenuForHuman();
		}
		return onLand ? getPopupMenuForLand() : getPopupMenuForSky();
	}

	private static void addPrefabMenuItem(final String type, final String url, final JMenu menu) {
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

	private static JPopupMenu getPopupMenuForLand() {

		if (popupMenuForLand == null) {

			final JMenuItem miInfo = new JMenuItem("Land");
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
							Scene.getInstance().pasteToPickedLocationOnLand();
							return null;
						}
					});
				}
			});

			final JMenuItem miRemoveAllTrees = new JMenuItem("Remove All Trees");
			miRemoveAllTrees.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().removeAllTrees();
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

			final JMenuItem miRemoveAllHumans = new JMenuItem("Remove All Humans");
			miRemoveAllHumans.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().removeAllHumans();
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

			final JMenuItem miRemoveAllBuildings = new JMenuItem("Remove All Foundations");
			miRemoveAllBuildings.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().removeAllFoundations();
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

			final JMenuItem miImportEnergy3D = new JMenuItem("Import...");
			miImportEnergy3D.setToolTipText("Import the content in an existing Energy3D file into the clicked location on the land as the center");
			miImportEnergy3D.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					MainFrame.getInstance().importFile();
				}
			});

			final JMenuItem miImportCollada = new JMenuItem("Import Collada...");
			miImportCollada.setToolTipText("Import the content in an existing Collada file into the clicked location on the land as the center");
			miImportCollada.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					MainFrame.getInstance().importColladaFile();
				}
			});

			final JMenu miImportPrefabMenu = new JMenu("Import a Prefab");
			addPrefabMenuItem("Back Hip Roof Porch", "prefabs/back-hip-roof-porch.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Balcony", "prefabs/balcony1.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Bell Tower", "prefabs/bell-tower.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Box", "prefabs/box.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Chimney", "prefabs/chimney.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Connecting Porch", "prefabs/connecting-porch.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Cylinder Tower", "prefabs/cylinder-tower.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Fence", "prefabs/fence1.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Flat-Top Porch", "prefabs/flat-top-porch.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Fountain", "prefabs/fountain.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Front Door Overhang", "prefabs/front-door-overhang.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Gable Dormer", "prefabs/gable-dormer.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Hexagonal Gazebo", "prefabs/hexagonal-gazebo.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Hexagonal Tower", "prefabs/hexagonal-tower.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Lighthouse", "prefabs/lighthouse.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Octagonal Tower", "prefabs/octagonal-tower.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Shed Dormer", "prefabs/shed-dormer.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Solarium", "prefabs/solarium1.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Square Tower", "prefabs/square-tower.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Stair", "prefabs/stair1.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Tall Front Door Overhang", "prefabs/tall-front-door-overhang.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Temple Front", "prefabs/temple-front.ng3", miImportPrefabMenu);
			addPrefabMenuItem("Waterfront Deck", "prefabs/waterfront-deck.ng3", miImportPrefabMenu);

			final JMenuItem miAlbedo = new JMenuItem("Albedo...");
			miAlbedo.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String title = "<html>Background Albedo (dimensionless [0, 1])<hr><font size=2>Examples:<br>0.17 (soil), 0.25 (grass), 0.40 (sand), 0.55 (concrete), snow (0.9)</html>";
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), title, Scene.getInstance().getGround().getAlbedo());
						if (newValue == null) {
							break;
						} else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val < 0 || val > 1) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Albedo value must be in 0-1.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									final ChangeBackgroundAlbedoCommand c = new ChangeBackgroundAlbedoCommand();
									Scene.getInstance().getGround().setAlbedo(val);
									updateAfterEdit();
									SceneManager.getInstance().getUndoManager().addEdit(c);
									break;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});

			final JMenuItem miSnowReflection = new JMenuItem("Snow Reflection...");
			miSnowReflection.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final JPanel gui = new JPanel(new BorderLayout());
					final String title = "<html>Increase of indirect solar radiation due to snow reflection<br>(a dimensionless parameter within [0, 0.2])</html>";
					gui.add(new JLabel(title), BorderLayout.NORTH);
					final JPanel inputPanel = new JPanel(new SpringLayout());
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					gui.add(inputPanel, BorderLayout.CENTER);
					final JTextField[] fields = new JTextField[12];
					for (int i = 0; i < 12; i++) {
						final JLabel l = new JLabel(AnnualGraph.THREE_LETTER_MONTH[i] + ": ", JLabel.TRAILING);
						inputPanel.add(l);
						fields[i] = new JTextField(threeDecimalsFormat.format(Scene.getInstance().getGround().getSnowReflectionFactor(i)), 5);
						l.setLabelFor(fields[i]);
						inputPanel.add(fields[i]);
					}
					SpringUtilities.makeCompactGrid(inputPanel, 12, 2, 6, 6, 6, 6);
					while (true) {
						if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), gui, "Snow reflection factor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.CANCEL_OPTION) {
							break;
						}
						boolean pass = true;
						final double[] val = new double[12];
						for (int i = 0; i < 12; i++) {
							try {
								val[i] = Double.parseDouble(fields[i].getText());
								if (val[i] < 0 || val[i] > 0.2) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Snow reflection factor must be in 0-0.2.", "Range Error", JOptionPane.ERROR_MESSAGE);
									pass = false;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), fields[i].getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								pass = false;
							}
						}
						if (pass) {
							final ChangeSnowReflectionFactorCommand c = new ChangeSnowReflectionFactorCommand();
							for (int i = 0; i < 12; i++) {
								Scene.getInstance().getGround().setSnowReflectionFactor(val[i], i);
							}
							updateAfterEdit();
							SceneManager.getInstance().getUndoManager().addEdit(c);
							break;
						}
					}
				}
			});

			final JMenuItem miThermalDiffusivity = new JMenuItem("Ground Thermal Diffusivity...");
			miThermalDiffusivity.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String title = "<html>Ground Thermal Diffusivity (m<sup>2</sup>/s)<hr><font size=2>Examples:<br>0.039 (sand), 0.046 (clay), 0.05 (silt)</html>";
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), title, Scene.getInstance().getGround().getThermalDiffusivity());
						if (newValue == null) {
							break;
						} else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val <= 0) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Ground thermal diffusivity must be positive.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									final ChangeGroundThermalDiffusivityCommand c = new ChangeGroundThermalDiffusivityCommand();
									Scene.getInstance().getGround().setThermalDiffusivity(val);
									updateAfterEdit();
									SceneManager.getInstance().getUndoManager().addEdit(c);
									break;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});

			final JMenuItem miClearImage = new JMenuItem("Clear Image");
			final JMenuItem miRescaleImage = new JMenuItem("Rescale Image...");
			final JCheckBoxMenuItem miShowImage = new JCheckBoxMenuItem("Show Image");

			final JMenu groundImageMenu = new JMenu("Ground Image");
			groundImageMenu.addMenuListener(new MenuListener() {
				@Override
				public void menuCanceled(final MenuEvent e) {
					miShowImage.setEnabled(true);
					miClearImage.setEnabled(true);
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					miShowImage.setEnabled(true);
					miClearImage.setEnabled(true);
				}

				@Override
				public void menuSelected(final MenuEvent e) {
					final boolean hasGroundImage = Scene.getInstance().isGroundImageEnabled();
					miShowImage.setEnabled(hasGroundImage);
					miClearImage.setEnabled(hasGroundImage);
					Util.selectSilently(miShowImage, SceneManager.getInstance().getGroundImageLand().isVisible());
				}
			});

			final JMenuItem miUseEarthView = new JMenuItem("Use Image from Earth View...");
			miUseEarthView.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					MapDialog.showDialog();
				}
			});
			groundImageMenu.add(miUseEarthView);

			final JMenuItem miUseImageFile = new JMenuItem("Use Image from File...");
			miUseImageFile.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final File file = FileChooser.getInstance().showDialog(".png", MainFrame.pngFilter, false);
					if (file == null) {
						return;
					}
					try {
						Scene.getInstance().setGroundImage(ImageIO.read(file), 1);
						Scene.getInstance().setGroundImageEarthView(false);
					} catch (final Throwable t) {
						t.printStackTrace();
						JOptionPane.showMessageDialog(MainFrame.getInstance(), t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
					Scene.getInstance().setEdited(true);
				}
			});
			groundImageMenu.add(miUseImageFile);
			groundImageMenu.addSeparator();

			miRescaleImage.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String title = "Scale the ground image";
					while (true) {
						final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), title, Scene.getInstance().getGroundImageScale());
						if (newValue == null) {
							break;
						} else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val <= 0) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "The scaling factor must be positive.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									// final ChangeGroundThermalDiffusivityCommand c = new ChangeGroundThermalDiffusivityCommand();
									Scene.getInstance().setGroundImageScale(val);
									// SceneManager.getInstance().getUndoManager().addEdit(c);
									break;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
					Scene.getInstance().setEdited(true);
				}
			});
			groundImageMenu.add(miRescaleImage);

			miClearImage.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().setGroundImage(null, 1);
					Scene.getInstance().setEdited(true);
				}
			});
			groundImageMenu.add(miClearImage);

			miShowImage.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final boolean b = miShowImage.isSelected();
					SceneManager.getInstance().getGroundImageLand().setVisible(b);
					Scene.getInstance().setShowGroundImage(b);
					Scene.getInstance().setEdited(true);
					Scene.getInstance().redrawAll();
				}
			});
			groundImageMenu.add(miShowImage);

			popupMenuForLand = new JPopupMenu();
			popupMenuForLand.setInvoker(MainPanel.getInstance().getCanvasPanel());
			popupMenuForLand.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
					final HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
					miPaste.setEnabled(copyBuffer instanceof Tree || copyBuffer instanceof Human || copyBuffer instanceof Foundation);
				}

				@Override
				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				}

				@Override
				public void popupMenuCanceled(final PopupMenuEvent e) {
				}

			});

			popupMenuForLand.add(miInfo);
			// popupMenuForLand.addSeparator();
			popupMenuForLand.add(miPaste);
			popupMenuForLand.add(miRemoveAllTrees);
			popupMenuForLand.add(miRemoveAllHumans);
			popupMenuForLand.add(miRemoveAllBuildings);
			popupMenuForLand.addSeparator();
			popupMenuForLand.add(miImportEnergy3D);
			popupMenuForLand.add(miImportCollada);
			popupMenuForLand.add(miImportPrefabMenu);
			popupMenuForLand.addSeparator();
			popupMenuForLand.add(groundImageMenu);
			popupMenuForLand.add(colorAction);
			popupMenuForLand.add(miAlbedo);
			popupMenuForLand.add(miSnowReflection);
			popupMenuForLand.add(miThermalDiffusivity);

		}

		return popupMenuForLand;

	}

	private static JPopupMenu getPopupMenuForSky() {

		if (popupMenuForSky == null) {

			final JMenuItem miInfo = new JMenuItem("Sky");
			miInfo.setEnabled(false);
			miInfo.setOpaque(true);
			miInfo.setBackground(Config.isMac() ? Color.BLACK : Color.GRAY);
			miInfo.setForeground(Color.WHITE);

			final JCheckBoxMenuItem miHeliodon = new JCheckBoxMenuItem("Heliodon");
			miHeliodon.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					MainPanel.getInstance().getHeliodonButton().doClick();
				}
			});

			final JMenu themeMenu = new JMenu("Theme");
			final ButtonGroup themeButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem miCloudySky = new JRadioButtonMenuItem("Cloudy Sky");
			miCloudySky.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeThemeCommand c = new ChangeThemeCommand();
					Scene.getInstance().setTheme(Scene.CLOUDY_SKY_THEME);
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			themeButtonGroup.add(miCloudySky);
			themeMenu.add(miCloudySky);

			final JRadioButtonMenuItem miDesert = new JRadioButtonMenuItem("Desert");
			miDesert.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeThemeCommand c = new ChangeThemeCommand();
					Scene.getInstance().setTheme(Scene.DESERT_THEME);
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			themeButtonGroup.add(miDesert);
			themeMenu.add(miDesert);

			final JRadioButtonMenuItem miGrassland = new JRadioButtonMenuItem("Grassland");
			miGrassland.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeThemeCommand c = new ChangeThemeCommand();
					Scene.getInstance().setTheme(Scene.GRASSLAND_THEME);
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			themeButtonGroup.add(miGrassland);
			themeMenu.add(miGrassland);

			final JRadioButtonMenuItem miForest = new JRadioButtonMenuItem("Forest");
			miForest.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeThemeCommand c = new ChangeThemeCommand();
					Scene.getInstance().setTheme(Scene.FOREST_THEME);
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			themeButtonGroup.add(miForest);
			themeMenu.add(miForest);

			themeMenu.addMenuListener(new MenuListener() {
				@Override
				public void menuCanceled(final MenuEvent e) {
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					SceneManager.getInstance().refresh();
				}

				@Override
				public void menuSelected(final MenuEvent e) {
					Util.selectSilently(miCloudySky, Scene.getInstance().getTheme() == Scene.CLOUDY_SKY_THEME);
					Util.selectSilently(miDesert, Scene.getInstance().getTheme() == Scene.DESERT_THEME);
					Util.selectSilently(miGrassland, Scene.getInstance().getTheme() == Scene.GRASSLAND_THEME);
					Util.selectSilently(miForest, Scene.getInstance().getTheme() == Scene.FOREST_THEME);
				}
			});

			final JMenuItem miDustLoss = new JMenuItem("Dust & Pollen...");
			miDustLoss.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final JPanel gui = new JPanel(new BorderLayout());
					final String title = "<html><b>Soiling loss factor:</b><br>Loss of productivity due to atmospheric dust and pollen<br>(a dimensionless parameter within [0, 1])</html>";
					gui.add(new JLabel(title), BorderLayout.NORTH);
					final JPanel inputPanel = new JPanel(new SpringLayout());
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					gui.add(inputPanel, BorderLayout.CENTER);
					final JTextField[] fields = new JTextField[12];
					for (int i = 0; i < 12; i++) {
						final JLabel l = new JLabel(AnnualGraph.THREE_LETTER_MONTH[i] + ": ", JLabel.LEFT);
						inputPanel.add(l);
						fields[i] = new JTextField(threeDecimalsFormat.format(Scene.getInstance().getAtmosphere().getDustLoss(i)), 5);
						l.setLabelFor(fields[i]);
						inputPanel.add(fields[i]);
					}
					SpringUtilities.makeCompactGrid(inputPanel, 12, 2, 6, 6, 6, 6);
					while (true) {
						if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), gui, "Dust and pollen loss", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.CANCEL_OPTION) {
							break;
						}
						boolean pass = true;
						final double[] val = new double[12];
						for (int i = 0; i < 12; i++) {
							try {
								val[i] = Double.parseDouble(fields[i].getText());
								if (val[i] < 0 || val[i] > 1) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Dust and pollen loss value must be in 0-1.", "Range Error", JOptionPane.ERROR_MESSAGE);
									pass = false;
								}
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), fields[i].getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								pass = false;
							}
						}
						if (pass) {
							final ChangeAtmosphericDustLossCommand c = new ChangeAtmosphericDustLossCommand();
							for (int i = 0; i < 12; i++) {
								Scene.getInstance().getAtmosphere().setDustLoss(val[i], i);
							}
							updateAfterEdit();
							SceneManager.getInstance().getUndoManager().addEdit(c);
							break;
						}
					}
				}
			});

			popupMenuForSky = new JPopupMenu();
			popupMenuForSky.setInvoker(MainPanel.getInstance().getCanvasPanel());
			popupMenuForSky.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
					Util.selectSilently(miHeliodon, MainPanel.getInstance().getHeliodonButton().isSelected());
				}

				@Override
				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				}

				@Override
				public void popupMenuCanceled(final PopupMenuEvent e) {
				}

			});

			popupMenuForSky.add(miInfo);
			// popupMenuForSky.addSeparator();
			popupMenuForSky.add(miDustLoss);
			popupMenuForSky.add(miHeliodon);
			popupMenuForSky.add(themeMenu);

		}

		return popupMenuForSky;

	}

	private static JPopupMenu getPopupMenuForFloor() {

		if (popupMenuForFloor == null) {

			final JMenuItem miInfo = new JMenuItem("Floor");
			miInfo.setEnabled(false);
			miInfo.setOpaque(true);
			miInfo.setBackground(Config.isMac() ? Color.BLACK : Color.GRAY);
			miInfo.setForeground(Color.WHITE);

			popupMenuForFloor = new JPopupMenu();
			popupMenuForFloor.setInvoker(MainPanel.getInstance().getCanvasPanel());
			popupMenuForFloor.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null) {
						return;
					}
					final String s = selectedPart.toString();
					miInfo.setText(s.substring(0, s.indexOf(')') + 1) + " ($" + Cost.getInstance().getPartCost(selectedPart) + ")");
				}

				@Override
				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				}

				@Override
				public void popupMenuCanceled(final PopupMenuEvent e) {
				}

			});

			popupMenuForFloor.add(miInfo);
			// popupMenuForFloor.addSeparator();
			popupMenuForFloor.add(colorAction);

		}

		return popupMenuForFloor;

	}

	private static JPopupMenu getPopupMenuForWindow() {

		if (popupMenuForWindow == null) {

			final JMenu shutterMenu = new JMenu("Shutters");

			popupMenuForWindow = createPopupMenu(true, true, new Runnable() {
				@Override
				public void run() {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						shutterMenu.setEnabled(selectedPart.getContainer() instanceof Wall);
					}
				}
			});

			final JMenu muntinMenu = new JMenu("Muntins");

			final JCheckBoxMenuItem cbmiHorizontalBars = new JCheckBoxMenuItem("Horizontal Bars");
			cbmiHorizontalBars.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window w = (Window) selectedPart;
						w.setHorizontalBars(cbmiHorizontalBars.isSelected());
						w.draw();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			muntinMenu.add(cbmiHorizontalBars);

			final JCheckBoxMenuItem cbmiVerticalBars = new JCheckBoxMenuItem("Vertical Bars");
			cbmiVerticalBars.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window w = (Window) selectedPart;
						w.setVerticalBars(cbmiVerticalBars.isSelected());
						w.draw();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			muntinMenu.add(cbmiVerticalBars);
			muntinMenu.addSeparator();

			final ButtonGroup muntinButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem miMoreBars = new JRadioButtonMenuItem("More Bars");
			miMoreBars.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window w = (Window) selectedPart;
						w.setStyle(Window.MORE_MUNTIN_BARS);
						w.draw();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			muntinButtonGroup.add(miMoreBars);
			muntinMenu.add(miMoreBars);

			final JRadioButtonMenuItem miMediumBars = new JRadioButtonMenuItem("Medium Bars");
			miMediumBars.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window w = (Window) selectedPart;
						w.setStyle(Window.MEDIUM_MUNTIN_BARS);
						w.draw();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			muntinButtonGroup.add(miMediumBars);
			muntinMenu.add(miMediumBars);

			final JRadioButtonMenuItem miLessBars = new JRadioButtonMenuItem("Less Bars");
			miLessBars.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window w = (Window) selectedPart;
						w.setStyle(Window.LESS_MUNTIN_BARS);
						w.draw();
						Scene.getInstance().setEdited(true);
					}
				}
			});
			muntinButtonGroup.add(miLessBars);
			muntinMenu.add(miLessBars);

			muntinMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(final MenuEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window window = (Window) selectedPart;
						switch (window.getStyle()) {
						case Window.MORE_MUNTIN_BARS:
							Util.selectSilently(miMoreBars, true);
							break;
						case Window.MEDIUM_MUNTIN_BARS:
							Util.selectSilently(miMediumBars, true);
							break;
						case Window.LESS_MUNTIN_BARS:
							Util.selectSilently(miLessBars, true);
							break;
						}
						// NO_MUNTIN_BAR backward compatibility
						Util.selectSilently(cbmiHorizontalBars, window.getStyle() != Window.NO_MUNTIN_BAR && window.getHorizontalBars());
						Util.selectSilently(cbmiVerticalBars, window.getStyle() != Window.NO_MUNTIN_BAR && window.getVerticalBars());
					}
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					muntinMenu.setEnabled(true);
				}

				@Override
				public void menuCanceled(final MenuEvent e) {
					muntinMenu.setEnabled(true);
				}

			});

			final JCheckBoxMenuItem cbmiBothShutters = new JCheckBoxMenuItem("Both Shutters");
			final JCheckBoxMenuItem cbmiLeftShutter = new JCheckBoxMenuItem("Left Shutter");
			final JCheckBoxMenuItem cbmiRightShutter = new JCheckBoxMenuItem("Right Shutter");

			cbmiLeftShutter.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window w = (Window) selectedPart;
						final ChangeWindowShuttersCommand c = new ChangeWindowShuttersCommand(w);
						w.setLeftShutter(cbmiLeftShutter.isSelected());
						w.draw();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			shutterMenu.add(cbmiLeftShutter);

			cbmiRightShutter.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window w = (Window) selectedPart;
						final ChangeWindowShuttersCommand c = new ChangeWindowShuttersCommand(w);
						w.setRightShutter(cbmiRightShutter.isSelected());
						w.draw();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			shutterMenu.add(cbmiRightShutter);

			cbmiBothShutters.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window w = (Window) selectedPart;
						final ChangeWindowShuttersCommand c = new ChangeWindowShuttersCommand(w);
						w.setLeftShutter(cbmiBothShutters.isSelected());
						w.setRightShutter(cbmiBothShutters.isSelected());
						w.draw();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			shutterMenu.add(cbmiBothShutters);
			shutterMenu.addSeparator();

			final JMenuItem miShutterColor = new JMenuItem("Color...");
			miShutterColor.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Window)) {
						return;
					}
					final Window window = (Window) selectedPart;
					final JColorChooser colorChooser = MainFrame.getInstance().getColorChooser();
					final ReadOnlyColorRGBA color = window.getShutterColor();
					if (color != null) {
						colorChooser.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
					}
					final ActionListener actionListener = new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							final Color c = colorChooser.getColor();
							final float[] newColor = c.getComponents(null);
							final ColorRGBA color = new ColorRGBA(newColor[0], newColor[1], newColor[2], 1);
							final JPanel panel = new JPanel();
							panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
							panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
							final JRadioButton rb1 = new JRadioButton("Only this Window", true);
							final JRadioButton rb2 = new JRadioButton("All Windows on this " + (window.getContainer() instanceof Wall ? "Wall" : "Roof"));
							final JRadioButton rb3 = new JRadioButton("All Windows of this Building");
							panel.add(rb1);
							panel.add(rb2);
							panel.add(rb3);
							final ButtonGroup bg = new ButtonGroup();
							bg.add(rb1);
							bg.add(rb2);
							bg.add(rb3);

							final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
							final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
							final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Shutter Color");

							while (true) {
								dialog.setVisible(true);
								final Object choice = optionPane.getValue();
								if (choice == options[1]) {
									break;
								} else {
									if (rb1.isSelected()) { // apply to only this window
										final ChangeShutterColorCommand cmd = new ChangeShutterColorCommand(window);
										window.setShutterColor(color);
										window.draw();
										SceneManager.getInstance().getUndoManager().addEdit(cmd);
									} else if (rb2.isSelected()) {
										final ChangeContainerShutterColorCommand cmd = new ChangeContainerShutterColorCommand(window.getContainer());
										Scene.getInstance().setWindowColorInContainer(window.getContainer(), color, true);
										SceneManager.getInstance().getUndoManager().addEdit(cmd);
									} else {
										final ChangeBuildingShutterColorCommand cmd = new ChangeBuildingShutterColorCommand(window);
										Scene.getInstance().setShutterColorOfBuilding(window, color);
										SceneManager.getInstance().getUndoManager().addEdit(cmd);
									}
									Scene.getInstance().setEdited(true);
									SceneManager.getInstance().refresh();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					};
					JColorChooser.createDialog(MainFrame.getInstance(), "Select Shutter Color", true, colorChooser, actionListener, null).setVisible(true);
				}
			});
			shutterMenu.add(miShutterColor);

			final JMenuItem miShutterLength = new JMenuItem("Relative Length...");
			miShutterLength.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Window)) {
						return;
					}
					final Window window = (Window) selectedPart;
					final String partInfo = window.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final String title = "<html>Relative Length of Shutter for " + partInfo + "</html>";
					final String footnote = "<html><hr width=400></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Window Shutter", true);
					final JRadioButton rb2 = new JRadioButton("All Window Shutters on this Wall");
					final JRadioButton rb3 = new JRadioButton("All Window Shutters of this Building");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					gui.add(panel, BorderLayout.CENTER);
					final JTextField inputField = new JTextField(window.getShutterLength() + "");
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Shutter Length (Relative)");

					while (true) {
						dialog.setVisible(true);
						inputField.selectAll();
						inputField.requestFocusInWindow();
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							double val = 0;
							try {
								val = Double.parseDouble(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (val <= 0 || val > 1) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Relative shutter length must be within (0, 1).", "Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final ChangeShutterLengthCommand c = new ChangeShutterLengthCommand(window);
										window.setShutterLength(val);
										window.draw();
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb2.isSelected()) {
										Scene.getInstance().setShutterLengthInContainer(window.getContainer(), val);
									} else if (rb3.isSelected()) {
										Scene.getInstance().setShutterLengthOfBuilding(window, val);
									}
									SceneManager.getInstance().refresh();
									Scene.getInstance().setEdited(true);
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					}
				}
			});
			shutterMenu.add(miShutterLength);

			shutterMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(final MenuEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window) {
						final Window window = (Window) selectedPart;
						Util.selectSilently(cbmiLeftShutter, window.getLeftShutter());
						Util.selectSilently(cbmiRightShutter, window.getRightShutter());
						Util.selectSilently(cbmiBothShutters, window.getLeftShutter() && window.getRightShutter());
					}
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					shutterMenu.setEnabled(true);
				}

				@Override
				public void menuCanceled(final MenuEvent e) {
					shutterMenu.setEnabled(true);
				}

			});

			final JMenuItem miShgc = new JMenuItem("Solar Heat Gain Coefficient...");
			miShgc.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Window)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Window window = (Window) selectedPart;
					final String title = "<html>Solar Heat Gain Coefficient of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>Examples:<br><table><tr><td><font size=2>Single glass (clear)</td><td><font size=2>0.66</td></tr><tr><td><font size=2>Single glass (green tint)</td><td><font size=2>0.55</td></tr><tr><td><font size=2>Triple glass (air spaces)</td><td><font size=2>0.39</td></tr></table><hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Window", true);
					final JRadioButton rb2 = new JRadioButton("All Windows on this " + (selectedPart.getContainer() instanceof Wall ? "Wall" : "Roof"));
					final JRadioButton rb3 = new JRadioButton("All Windows of this Building");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					gui.add(panel, BorderLayout.CENTER);
					final JTextField inputField = new JTextField(window.getSolarHeatGainCoefficient() + "");
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Heat Gain Coefficient (SHGC)");

					while (true) {
						dialog.setVisible(true);
						inputField.selectAll();
						inputField.requestFocusInWindow();
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							double val = 0;
							try {
								val = Double.parseDouble(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (val < 0 || val > 1) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar heat gain coefficient must be between 0 and 1.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final ChangeWindowShgcCommand c = new ChangeWindowShgcCommand(window);
										window.setSolarHeatGainCoefficient(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb2.isSelected()) {
										final ChangeContainerWindowShgcCommand c = new ChangeContainerWindowShgcCommand(window.getContainer());
										Scene.getInstance().setWindowShgcInContainer(window.getContainer(), val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb3.isSelected()) {
										final Foundation foundation = window.getTopContainer();
										final ChangeBuildingWindowShgcCommand c = new ChangeBuildingWindowShgcCommand(foundation);
										Scene.getInstance().setWindowShgcOfBuilding(foundation, val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					}
				}
			});

			final JMenuItem miTint = new JMenuItem("Tint...");
			miTint.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Window)) {
						return;
					}
					final Window window = (Window) selectedPart;
					final JColorChooser colorChooser = MainFrame.getInstance().getColorChooser();
					final ReadOnlyColorRGBA color = window.getColor();
					if (color != null) {
						colorChooser.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
					}
					final ActionListener actionListener = new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							final Color c = colorChooser.getColor();
							final float[] newColor = c.getComponents(null);
							final ColorRGBA color = new ColorRGBA(newColor[0], newColor[1], newColor[2], (float) (1.0 - window.getSolarHeatGainCoefficient()));
							final JPanel panel = new JPanel();
							panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
							panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
							final JRadioButton rb1 = new JRadioButton("Only this Window", true);
							final JRadioButton rb2 = new JRadioButton("All Windows on this " + (window.getContainer() instanceof Wall ? "Wall" : "Roof"));
							final JRadioButton rb3 = new JRadioButton("All Windows of this Building");
							panel.add(rb1);
							panel.add(rb2);
							panel.add(rb3);
							final ButtonGroup bg = new ButtonGroup();
							bg.add(rb1);
							bg.add(rb2);
							bg.add(rb3);

							final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
							final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
							final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Window Tint");

							while (true) {
								dialog.setVisible(true);
								final Object choice = optionPane.getValue();
								if (choice == options[1]) {
									break;
								} else {
									if (rb1.isSelected()) { // apply to only this window
										final ChangePartColorCommand cmd = new ChangePartColorCommand(window);
										window.setColor(color);
										SceneManager.getInstance().getUndoManager().addEdit(cmd);
									} else if (rb2.isSelected()) {
										final ChangeContainerWindowColorCommand cmd = new ChangeContainerWindowColorCommand(window.getContainer());
										Scene.getInstance().setWindowColorInContainer(window.getContainer(), color, false);
										SceneManager.getInstance().getUndoManager().addEdit(cmd);
									} else {
										final ChangeBuildingColorCommand cmd = new ChangeBuildingColorCommand(window);
										Scene.getInstance().setPartColorOfBuilding(window, color);
										SceneManager.getInstance().getUndoManager().addEdit(cmd);
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					};
					JColorChooser.createDialog(MainFrame.getInstance(), "Select Tint", true, colorChooser, actionListener, null).setVisible(true);
				}
			});

			final JMenuItem miSize = new JMenuItem("Size...");
			miSize.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Window)) {
						return;
					}
					final Window window = (Window) selectedPart;
					final HousePart container = window.getContainer();
					final Foundation foundation = window.getTopContainer();
					final String partInfo = window.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
					gui.add(inputPanel, BorderLayout.CENTER);
					inputPanel.add(new JLabel("Width (m): "));
					final JTextField widthField = new JTextField(threeDecimalsFormat.format(window.getWindowWidth()));
					inputPanel.add(widthField);
					inputPanel.add(new JLabel("Height (m): "));
					final JTextField heightField = new JTextField(threeDecimalsFormat.format(window.getWindowHeight()));
					inputPanel.add(heightField);
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Window", true);
					final JRadioButton rb2 = new JRadioButton("All Windows on this " + (window.getContainer() instanceof Wall ? "Wall" : "Roof"));
					final JRadioButton rb3 = new JRadioButton("All Windows of this Building");
					scopePanel.add(rb1);
					scopePanel.add(rb2);
					scopePanel.add(rb3);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					gui.add(scopePanel, BorderLayout.NORTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { "Set Size for " + partInfo, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Window Size");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							double w = 0, h = 0;
							try {
								w = Double.parseDouble(widthField.getText());
								h = Double.parseDouble(heightField.getText());
							} catch (final NumberFormatException x) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								double wmax = 10;
								if (container instanceof Wall) {
									wmax = ((Wall) container).getWallWidth() * 0.99;
								}
								double hmax = 10;
								if (container instanceof Wall) {
									hmax = ((Wall) container).getWallHeight() * 0.99;
								}
								if (w < 0.1 || w > wmax) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Width must be between 0.1 and " + (int) wmax + " m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (h < 0.1 || h > hmax) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Height must be between 0.1 and " + (int) hmax + " m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final SetPartSizeCommand c = new SetPartSizeCommand(window);
										window.setWindowWidth(w);
										window.setWindowHeight(h);
										window.draw();
										window.getContainer().draw();
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb2.isSelected()) {
										final ChangeContainerWindowSizeCommand c = new ChangeContainerWindowSizeCommand(window.getContainer());
										Scene.getInstance().setWindowSizeInContainer(window.getContainer(), w, h);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									} else if (rb3.isSelected()) {
										final SetSizeForWindowsOnFoundationCommand c = new SetSizeForWindowsOnFoundationCommand(foundation);
										foundation.setSizeForWindows(w, h);
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					}
				}
			});

			popupMenuForWindow.addSeparator();
			popupMenuForWindow.add(miSize);
			popupMenuForWindow.add(miTint);
			popupMenuForWindow.add(createInsulationMenuItem(true));
			popupMenuForWindow.add(miShgc);
			popupMenuForWindow.add(muntinMenu);
			popupMenuForWindow.add(shutterMenu);
			popupMenuForWindow.addSeparator();

			JMenuItem mi = new JMenuItem("Daily Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Window) {
						new EnergyDailyAnalysis().show("Daily Energy for Window");
					}
				}
			});
			popupMenuForWindow.add(mi);

			mi = new JMenuItem("Annual Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Window) {
						new EnergyAnnualAnalysis().show("Annual Energy for Window");
					}
				}
			});
			popupMenuForWindow.add(mi);

		}

		return popupMenuForWindow;

	}

	private static JPopupMenu getPopupMenuForWall() {

		if (popupMenuForWall == null) {

			final JMenuItem miPaste = new JMenuItem("Paste");
			miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			miPaste.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().pasteToPickedLocationOnWall();
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

			final JMenuItem miThickness = new JMenuItem("Thickness...");
			miThickness.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Wall)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Wall w = (Wall) selectedPart;
					final String title = "<html>Thickness of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>Thickness of wall is in meters.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Wall", true);
					final JRadioButton rb2 = new JRadioButton("All Walls on This Foundation");
					final JRadioButton rb3 = new JRadioButton("All Walls");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(w.getThickness() * Scene.getInstance().getAnnotationScale()));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Wall Thickness");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							double val = 0;
							boolean ok = true;
							try {
								val = Double.parseDouble(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (val < 0.1 || val > 10) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "The thickness of a wall must be between 0.1 and 10 meters.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									val /= Scene.getInstance().getAnnotationScale();
									Wall.setDefaultThickess(val);
									if (rb1.isSelected()) {
										final ChangeWallThicknessCommand c = new ChangeWallThicknessCommand(w);
										w.setThickness(val);
										w.draw();
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = w.getTopContainer();
										final ChangeFoundationWallThicknessCommand c = new ChangeFoundationWallThicknessCommand(foundation);
										foundation.setThicknessOfWalls(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final ChangeThicknessForAllWallsCommand c = new ChangeThicknessForAllWallsCommand(w);
										Scene.getInstance().setThicknessForAllWalls(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 2;
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}

							}
						}
					}

				}
			});

			final JMenuItem miHeight = new JMenuItem("Height...");
			miHeight.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Wall)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Wall w = (Wall) selectedPart;
					final String title = "<html>Height of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>Height of wall is in meters.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Wall", true);
					final JRadioButton rb2 = new JRadioButton("All Walls Connected to This One (Direct and Indirect)");
					final JRadioButton rb3 = new JRadioButton("All Walls on This Foundation");
					final JRadioButton rb4 = new JRadioButton("All Walls");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					panel.add(rb4);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					bg.add(rb4);
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
					case 3:
						rb4.setSelected(true);
						break;
					}
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(w.getHeight() * Scene.getInstance().getAnnotationScale()));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Wall Height");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							double val = 0;
							boolean ok = true;
							try {
								val = Double.parseDouble(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (val < 1 || val > 1000) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "The height of a wall must be between 1 and 1000 meters.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									val /= Scene.getInstance().getAnnotationScale();
									if (rb1.isSelected()) {
										final ChangeWallHeightCommand c = new ChangeWallHeightCommand(w);
										w.setHeight(val, true);
										w.draw();
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final ChangeHeightForConnectedWallsCommand c = new ChangeHeightForConnectedWallsCommand(w);
										Scene.getInstance().setHeightOfConnectedWalls(w, val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final Foundation foundation = w.getTopContainer();
										final ChangeFoundationWallHeightCommand c = new ChangeFoundationWallHeightCommand(foundation);
										foundation.setHeightOfWalls(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 2;
									} else if (rb4.isSelected()) {
										final ChangeHeightForAllWallsCommand c = new ChangeHeightForAllWallsCommand(w);
										Scene.getInstance().setHeightForAllWalls(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 3;
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}

							}
						}
					}

				}
			});

			popupMenuForWall = createPopupMenu(false, false, new Runnable() {
				@Override
				public void run() {
					final HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
					miPaste.setEnabled(copyBuffer instanceof Window || copyBuffer instanceof SolarPanel || copyBuffer instanceof Rack);
				}
			});

			popupMenuForWall.add(miPaste);
			popupMenuForWall.add(miClear);
			popupMenuForWall.addSeparator();
			popupMenuForWall.add(colorAction);
			popupMenuForWall.add(miThickness);
			popupMenuForWall.add(miHeight);
			popupMenuForWall.add(createInsulationMenuItem(false));
			popupMenuForWall.add(createVolumetricHeatCapacityMenuItem());
			popupMenuForWall.addSeparator();

			final JMenu typeMenu = new JMenu("Type");
			popupMenuForWall.add(typeMenu);
			popupMenuForWall.addSeparator();
			final ButtonGroup typeGroup = new ButtonGroup();

			final JRadioButtonMenuItem rbmiSolidWall = new JRadioButtonMenuItem("Solid Wall");
			rbmiSolidWall.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						final Wall wall = (Wall) selectedPart;
						final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
						wall.setType(Wall.SOLID_WALL);
						wall.draw();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			typeMenu.add(rbmiSolidWall);
			typeGroup.add(rbmiSolidWall);

			final JRadioButtonMenuItem rbmiEmpty = new JRadioButtonMenuItem("Empty");
			rbmiEmpty.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						final Wall wall = (Wall) selectedPart;
						final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
						wall.setType(Wall.EMPTY);
						wall.draw();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			typeMenu.add(rbmiEmpty);
			typeGroup.add(rbmiEmpty);

			final JRadioButtonMenuItem rbmiEdges = new JRadioButtonMenuItem("Vertical Edges");
			rbmiEdges.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						final Wall wall = (Wall) selectedPart;
						final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
						wall.setType(Wall.VERTICAL_EDGES_ONLY);
						wall.draw();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			typeMenu.add(rbmiEdges);
			typeGroup.add(rbmiEdges);

			final JRadioButtonMenuItem rbmiColumns = new JRadioButtonMenuItem("Columns");
			rbmiColumns.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						final Wall wall = (Wall) selectedPart;
						final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
						wall.setType(Wall.COLUMNS_ONLY);
						wall.draw();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			typeMenu.add(rbmiColumns);
			typeGroup.add(rbmiColumns);

			final JRadioButtonMenuItem rbmiRails = new JRadioButtonMenuItem("Rails");
			rbmiRails.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						final Wall wall = (Wall) selectedPart;
						final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
						wall.setType(Wall.RAILS_ONLY);
						wall.draw();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			typeMenu.add(rbmiRails);
			typeGroup.add(rbmiRails);

			final JRadioButtonMenuItem rbmiColumnsAndRailings = new JRadioButtonMenuItem("Columns & Railings");
			rbmiColumnsAndRailings.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						final Wall wall = (Wall) selectedPart;
						final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
						wall.setType(Wall.COLUMNS_RAILS);
						wall.draw();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			typeMenu.add(rbmiColumnsAndRailings);
			typeGroup.add(rbmiColumnsAndRailings);

			final JRadioButtonMenuItem rbmiFence = new JRadioButtonMenuItem("Fence");
			rbmiFence.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						final Wall wall = (Wall) selectedPart;
						final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
						wall.setType(Wall.FENCE);
						wall.draw();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			typeMenu.add(rbmiFence);
			typeGroup.add(rbmiFence);

			final JRadioButtonMenuItem rbmiSteelFrame = new JRadioButtonMenuItem("Steel Frame");
			rbmiSteelFrame.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						final Wall wall = (Wall) selectedPart;
						final ChangeWallTypeCommand c = new ChangeWallTypeCommand(wall);
						wall.setType(Wall.STEEL_FRAME);
						wall.draw();
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
					}
				}
			});
			typeMenu.add(rbmiSteelFrame);
			typeGroup.add(rbmiSteelFrame);

			typeMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(final MenuEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Wall) {
						final Wall wall = (Wall) selectedPart;
						switch (wall.getType()) {
						case Wall.SOLID_WALL:
							Util.selectSilently(rbmiSolidWall, true);
							break;
						case Wall.EMPTY:
							Util.selectSilently(rbmiEmpty, true);
							break;
						case Wall.VERTICAL_EDGES_ONLY:
							Util.selectSilently(rbmiEdges, true);
							break;
						case Wall.COLUMNS_ONLY:
							Util.selectSilently(rbmiColumns, true);
							break;
						case Wall.RAILS_ONLY:
							Util.selectSilently(rbmiRails, true);
							break;
						case Wall.COLUMNS_RAILS:
							Util.selectSilently(rbmiColumnsAndRailings, true);
							break;
						case Wall.STEEL_FRAME:
							Util.selectSilently(rbmiSteelFrame, true);
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

			JMenuItem mi = new JMenuItem("Daily Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Wall) {
						new EnergyDailyAnalysis().show("Daily Energy for Wall");
					}
				}
			});
			popupMenuForWall.add(mi);

			mi = new JMenuItem("Annual Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Wall) {
						new EnergyAnnualAnalysis().show("Annual Energy for Wall");
					}
				}
			});
			popupMenuForWall.add(mi);

		}

		return popupMenuForWall;

	}

	private static JPopupMenu getPopupMenuForRoof() {

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
									final ChangeRoofOverhangCommand c = new ChangeRoofOverhangCommand(roof);
									roof.setOverhangLength(val / Scene.getInstance().getAnnotationScale());
									roof.draw();
									final Foundation f = roof.getTopContainer();
									f.drawChildren();
									SceneManager.getInstance().refresh();
									updateAfterEdit();
									SceneManager.getInstance().getUndoManager().addEdit(c);
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

			popupMenuForRoof = createPopupMenu(false, false, new Runnable() {
				@Override
				public void run() {
					final HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
					miPaste.setEnabled(copyBuffer instanceof SolarPanel || copyBuffer instanceof Window || copyBuffer instanceof Rack);
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

			JMenuItem mi = new JMenuItem("Daily Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
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
					if (SceneManager.getInstance().getSelectedPart() instanceof Roof) {
						new EnergyAnnualAnalysis().show("Annual Energy for Roof");
					}
				}
			});
			popupMenuForRoof.add(mi);

		}

		return popupMenuForRoof;

	}

	private static JPopupMenu getPopupMenuForDoor() {

		if (popupMenuForDoor == null) {
			popupMenuForDoor = createPopupMenu(false, false, null);
			popupMenuForDoor.addSeparator();
			popupMenuForDoor.add(colorAction);
			popupMenuForDoor.add(createInsulationMenuItem(true));
			popupMenuForDoor.add(createVolumetricHeatCapacityMenuItem());
			popupMenuForDoor.addSeparator();

			JMenuItem mi = new JMenuItem("Daily Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Door) {
						new EnergyDailyAnalysis().show("Daily Energy for Door");
					}
				}
			});
			popupMenuForDoor.add(mi);

			mi = new JMenuItem("Annual Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Door) {
						new EnergyAnnualAnalysis().show("Annual Energy for Door");
					}
				}
			});
			popupMenuForDoor.add(mi);

		}

		return popupMenuForDoor;

	}

	private static JPopupMenu getPopupMenuForFoundation() {

		if (popupMenuForFoundation == null) {

			final JMenuItem miImportCollada = new JMenuItem("Import Collada...");
			miImportCollada.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final File file = FileChooser.getInstance().showDialog(".dae", MainFrame.daeFilter, false);
						if (file != null) {
							EnergyPanel.getInstance().clearRadiationHeatMap();
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
				private JComboBox<String> cellTypeComboBox;
				private JComboBox<String> colorOptionComboBox;
				private JComboBox<String> sizeComboBox;
				private JComboBox<String> orientationComboBox;
				private JComboBox<String> shadeToleranceComboBox;
				private JComboBox<String> rowAxisComboBox;
				private int numberOfCellsInX = 6;
				private int numberOfCellsInY = 10;

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

						panel.add(new JLabel("Cell Type:"));
						cellTypeComboBox = new JComboBox<String>(new String[] { "Monocrystalline", "Polycrystalline" });
						cellTypeComboBox.setSelectedIndex(solarPanelCellType);
						panel.add(cellTypeComboBox);

						panel.add(new JLabel("Color:"));
						colorOptionComboBox = new JComboBox<String>(new String[] { "Blue", "Black" });
						colorOptionComboBox.setSelectedIndex(solarPanelColorOption);
						panel.add(colorOptionComboBox);

						panel.add(new JLabel("Size:"));
						sizeComboBox = new JComboBox<String>(new String[] { "0.99m \u00D7 1.65m (6 \u00D7 10 cells)", "1.04m \u00D7 1.55m (8 \u00D7 12 cells)", "0.99m \u00D7 1.96m (6 \u00D7 12 cells)" });
						if (Util.isZero(0.99 - solarPanelWidth) && Util.isZero(1.65 - solarPanelHeight)) {
							sizeComboBox.setSelectedIndex(0);
						} else if (Util.isZero(1.04 - solarPanelWidth) && Util.isZero(1.55 - solarPanelHeight)) {
							sizeComboBox.setSelectedIndex(1);
						} else {
							sizeComboBox.setSelectedIndex(2);
						}
						panel.add(sizeComboBox);

						panel.add(new JLabel("Orientation:"));
						orientationComboBox = new JComboBox<String>(new String[] { "Portrait", "Landscape" });
						orientationComboBox.setSelectedIndex(solarPanelOrientation);
						panel.add(orientationComboBox);

						panel.add(new JLabel("Solar Cell Efficiency (%):"));
						final JTextField cellEfficiencyField = new JTextField(threeDecimalsFormat.format(solarCellEfficiencyPercentage));
						panel.add(cellEfficiencyField);

						panel.add(new JLabel("<html>Nominal Operating Cell Temperature (&deg;C):"));
						final JTextField noctField = new JTextField(threeDecimalsFormat.format(solarPanelNominalOperatingCellTemperature));
						panel.add(noctField);

						panel.add(new JLabel("<html>Temperature Coefficient of Pmax (%/&deg;C):"));
						final JTextField pmaxField = new JTextField(sixDecimalsFormat.format(solarPanelTemperatureCoefficientPmaxPercentage));
						panel.add(pmaxField);

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

						SpringUtilities.makeCompactGrid(panel, 14, 2, 6, 6, 6, 6);

						final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
						final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
						final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Array Options");

						while (true) {
							dialog.setVisible(true);
							final Object choice = optionPane.getValue();
							if (choice == options[1]) {
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
									solarPanelTemperatureCoefficientPmaxPercentage = Double.parseDouble(pmaxField.getText());
									solarPanelNominalOperatingCellTemperature = Double.parseDouble(noctField.getText());
								} catch (final NumberFormatException exception) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
									ok = false;
								}
								if (ok) {
									switch (sizeComboBox.getSelectedIndex()) {
									case 0:
										solarPanelWidth = 0.99;
										solarPanelHeight = 1.65;
										numberOfCellsInX = 6;
										numberOfCellsInY = 10;
										break;
									case 1:
										solarPanelWidth = 1.04;
										solarPanelHeight = 1.55;
										numberOfCellsInX = 8;
										numberOfCellsInY = 12;
										break;
									default:
										solarPanelWidth = 0.99;
										solarPanelHeight = 1.96;
										numberOfCellsInX = 6;
										numberOfCellsInY = 12;
										break;
									}
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
					final SolarPanel sp = new SolarPanel();
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
				private JComboBox<String> orientationComboBox;
				private JComboBox<String> sizeComboBox;
				private JComboBox<String> cellTypeComboBox;
				private JComboBox<String> colorOptionComboBox;
				private JComboBox<String> shadeToleranceComboBox;
				private JComboBox<String> rowAxisComboBox;
				private int numberOfCellsInX = 6;
				private int numberOfCellsInY = 10;

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

						panel.add(new JLabel("Solar Panel Orientation:"));
						orientationComboBox = new JComboBox<String>(new String[] { "Portrait", "Landscape" });
						orientationComboBox.setSelectedIndex(solarPanelOrientation);
						panel.add(orientationComboBox);

						panel.add(new JLabel("Solar Panel Cell Type:"));
						cellTypeComboBox = new JComboBox<String>(new String[] { "Monocrystalline", "Polycrystalline" });
						cellTypeComboBox.setSelectedIndex(solarPanelCellType);
						panel.add(cellTypeComboBox);

						panel.add(new JLabel("Solar Panel Color:"));
						colorOptionComboBox = new JComboBox<String>(new String[] { "Blue", "Black" });
						colorOptionComboBox.setSelectedIndex(solarPanelColorOption);
						panel.add(colorOptionComboBox);

						panel.add(new JLabel("Solar Panel Size:"));
						sizeComboBox = new JComboBox<String>(new String[] { "0.99m \u00D7 1.65m (6 \u00D7 10 cells)", "1.04m \u00D7 1.55m (8 \u00D7 12 cells)", "0.99m \u00D7 1.96m (6 \u00D7 12 cells)" });
						if (Util.isZero(0.99 - solarPanelWidth) && Util.isZero(1.65 - solarPanelHeight)) {
							sizeComboBox.setSelectedIndex(0);
						} else if (Util.isZero(1.04 - solarPanelWidth) && Util.isZero(1.55 - solarPanelHeight)) {
							sizeComboBox.setSelectedIndex(1);
						} else {
							sizeComboBox.setSelectedIndex(2);
						}
						panel.add(sizeComboBox);

						panel.add(new JLabel("Solar Cell Efficiency (%):"));
						final JTextField cellEfficiencyField = new JTextField(threeDecimalsFormat.format(solarCellEfficiencyPercentage));
						panel.add(cellEfficiencyField);

						panel.add(new JLabel("<html>Nominal Operating Cell Temperature (&deg;C):"));
						final JTextField noctField = new JTextField(threeDecimalsFormat.format(solarPanelNominalOperatingCellTemperature));
						panel.add(noctField);

						panel.add(new JLabel("<html>Temperature Coefficient of Pmax (%/&deg;C):"));
						final JTextField pmaxField = new JTextField(sixDecimalsFormat.format(solarPanelTemperatureCoefficientPmaxPercentage));
						panel.add(pmaxField);

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

						SpringUtilities.makeCompactGrid(panel, 16, 2, 6, 6, 6, 6);

						final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
						final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
						final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Rack Options");

						while (true) {
							dialog.setVisible(true);
							final Object choice = optionPane.getValue();
							if (choice == options[1]) {
								break;
							} else {
								boolean ok = true;
								try {
									solarPanelRackArrayInterRowSpacing = Double.parseDouble(interrowSpacingField.getText());
									solarPanelTiltAngle = Double.parseDouble(tiltAngleField.getText());
									solarPanelRowsPerRack = Integer.parseInt(rowsPerRackField.getText());
									solarCellEfficiencyPercentage = Double.parseDouble(cellEfficiencyField.getText());
									inverterEfficiencyPercentage = Double.parseDouble(inverterEfficiencyField.getText());
									solarPanelTemperatureCoefficientPmaxPercentage = Double.parseDouble(pmaxField.getText());
									solarPanelNominalOperatingCellTemperature = Double.parseDouble(noctField.getText());
									solarPanelRackPoleSpacingX = Double.parseDouble(poleSpacingXField.getText());
									solarPanelRackPoleSpacingY = Double.parseDouble(poleSpacingYField.getText());
									solarPanelRackBaseHeight = Double.parseDouble(baseHeightField.getText());
								} catch (final NumberFormatException exception) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
									ok = false;
								}
								if (ok) {
									switch (sizeComboBox.getSelectedIndex()) {
									case 0:
										solarPanelWidth = 0.99;
										solarPanelHeight = 1.65;
										numberOfCellsInX = 6;
										numberOfCellsInY = 10;
										break;
									case 1:
										solarPanelWidth = 1.04;
										solarPanelHeight = 1.55;
										numberOfCellsInX = 8;
										numberOfCellsInY = 12;
										break;
									default:
										solarPanelWidth = 0.99;
										solarPanelHeight = 1.96;
										numberOfCellsInX = 6;
										numberOfCellsInY = 12;
										break;
									}
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
					final SolarPanel sp = new SolarPanel();
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
							if (choice == options[1]) {
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
							if (choice == options[1]) {
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
							if (choice == options[1]) {
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

			final JCheckBoxMenuItem miLock = new JCheckBoxMenuItem("Lock");
			miLock.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation foundation = (Foundation) selectedPart;
						SceneManager.getInstance().getUndoManager().addEdit(new LockPartCommand(foundation));
						final boolean lock = miLock.isSelected();
						foundation.setFreeze(lock);
						for (final HousePart p : Scene.getInstance().getParts()) {
							if (p.getTopContainer() == foundation) {
								p.setFreeze(lock);
							}
						}
						if (lock) {
							SceneManager.getInstance().hideAllEditPoints();
						}
						foundation.draw();
						foundation.drawChildren();
						Scene.getInstance().setEdited(true);
					}
				}
			});

			final JCheckBoxMenuItem miDisableEdits = new JCheckBoxMenuItem("Disable Edits");
			miDisableEdits.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						((Foundation) selectedPart).setLockEdit(miDisableEdits.isSelected());
						Scene.getInstance().setEdited(true);
					}
				}
			});

			final JMenu editOptionsMenu = new JMenu("Edit Options");

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
			editOptionsMenu.add(miChildGridSize);

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

			final JMenuItem miHeight = new JMenuItem("Size...");
			miHeight.addActionListener(new ActionListener() {
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
						if (choice == options[1]) {
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
								if (lx1 <= 0) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Length must be greater than zero.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (ly1 <= 0) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Width must be greater than zero.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (lz1 < 0) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Height must be greater than zero.", "Range Error", JOptionPane.ERROR_MESSAGE);
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
							miLock.setEnabled(false);
							miDisableEdits.setEnabled(false);
							miThermostat.setEnabled(false);
						} else {
							miLock.setEnabled(true);
							miDisableEdits.setEnabled(true);
							miThermostat.setEnabled(true);
						}
						miDeleteUtilityBill.setEnabled(f.getUtilityBill() != null);
						Util.selectSilently(miGroupMaster, f.isGroupMaster());
						Util.selectSilently(miLock, f.isFrozen());
						Util.selectSilently(miDisableEdits, f.getLockEdit());
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
						powerTowerLabelMenu.setEnabled(f.isSolarReceiverVisible());
					}
					final HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
					final Node copyNode = Scene.getInstance().getCopyNode();
					miPaste.setEnabled(copyBuffer instanceof SolarPanel || copyBuffer instanceof Mirror || copyBuffer instanceof Rack || copyNode != null);
				}
			});

			popupMenuForFoundation.add(miPaste);
			popupMenuForFoundation.add(miCopy);
			popupMenuForFoundation.addSeparator();
			popupMenuForFoundation.add(miImportCollada);
			popupMenuForFoundation.add(miHeight);
			popupMenuForFoundation.add(miRescale);
			popupMenuForFoundation.add(rotateMenu);
			popupMenuForFoundation.add(clearMenu);
			popupMenuForFoundation.add(layoutMenu);
			popupMenuForFoundation.addSeparator();
			popupMenuForFoundation.add(miGroupMaster);
			popupMenuForFoundation.add(miBorderLine);
			popupMenuForFoundation.add(miLock);
			popupMenuForFoundation.add(miDisableEdits);
			popupMenuForFoundation.add(editOptionsMenu);
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

			JMenuItem mi = new JMenuItem("Daily Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Foundation) {
						final EnergyDailyAnalysis analysis = new EnergyDailyAnalysis();
						if (SceneManager.getInstance().getSolarHeatMap()) {
							analysis.updateGraph();
						}
						analysis.show("Daily Energy");
					}
				}
			});
			analysisMenu.add(mi);

			mi = new JMenuItem("Annual Energy Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Foundation) {
						new EnergyAnnualAnalysis().show("Annual Energy");
					}
				}
			});
			analysisMenu.add(mi);
			analysisMenu.addSeparator();

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
			analysisMenu.add(mi);

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
			analysisMenu.add(mi);
			analysisMenu.addSeparator();

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
			analysisMenu.add(mi);

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
			analysisMenu.add(mi);

		}

		return popupMenuForFoundation;

	}

	private static JPopupMenu getPopupMenuForSensor() {

		if (popupMenuForSensor == null) {

			final JCheckBoxMenuItem miLight = new JCheckBoxMenuItem("Light", true);
			miLight.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Sensor)) {
						return;
					}
					final Sensor s = (Sensor) selectedPart;
					s.setLightOff(!miLight.isSelected());
					Scene.getInstance().setEdited(true);
				}
			});

			final JCheckBoxMenuItem miHeatFlux = new JCheckBoxMenuItem("Heat Flux", true);
			miHeatFlux.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Sensor)) {
						return;
					}
					final Sensor s = (Sensor) selectedPart;
					s.setHeatFluxOff(!miHeatFlux.isSelected());
					Scene.getInstance().setEdited(true);
				}
			});

			popupMenuForSensor = createPopupMenu(false, false, new Runnable() {
				@Override
				public void run() {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Sensor)) {
						return;
					}
					final Sensor s = (Sensor) selectedPart;
					Util.selectSilently(miLight, !s.isLightOff());
					Util.selectSilently(miHeatFlux, !s.isHeatFluxOff());
				}
			});

			popupMenuForSensor.addSeparator();
			popupMenuForSensor.add(miLight);
			popupMenuForSensor.add(miHeatFlux);

		}

		return popupMenuForSensor;

	}

	private static JPopupMenu getPopupMenuForSolarPanel() {

		if (popupMenuForSolarPanel == null) {

			final JMenu trackerMenu = new JMenu("Tracker");
			final JMenu shadeToleranceMenu = new JMenu("Shade Tolerance");

			final ButtonGroup shadeToleranceButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem miHighTolerance = new JRadioButtonMenuItem("High Tolerance...");
			shadeToleranceButtonGroup.add(miHighTolerance);
			miHighTolerance.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel sp = (SolarPanel) selectedPart;
					final String partInfo = sp.toString().substring(0, sp.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					final String title = "<html>Choose shade tolerance level for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The energy generated by this panel comes from each cell proportionally (ideal case).<hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, panel }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "High Shade Tolerance");
					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final SetShadeToleranceCommand c = new SetShadeToleranceCommand(sp);
								sp.setShadeTolerance(SolarPanel.HIGH_SHADE_TOLERANCE);
								sp.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final Foundation foundation = sp.getTopContainer();
								final SetShadeToleranceForSolarPanelsOnFoundationCommand c = new SetShadeToleranceForSolarPanelsOnFoundationCommand(foundation);
								foundation.setShadeToleranceForSolarPanels(SolarPanel.HIGH_SHADE_TOLERANCE);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetShadeToleranceForAllSolarPanelsCommand c = new SetShadeToleranceForAllSolarPanelsCommand();
								Scene.getInstance().setShadeToleranceForAllSolarPanels(SolarPanel.HIGH_SHADE_TOLERANCE);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 2;
							}
							updateAfterEdit();
							if (choice == options[0]) {
								break;
							}
						}
					}
				}
			});

			final JRadioButtonMenuItem miPartialTolerance = new JRadioButtonMenuItem("Partial Tolerance...", true);
			shadeToleranceButtonGroup.add(miPartialTolerance);
			miPartialTolerance.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel sp = (SolarPanel) selectedPart;
					final String partInfo = sp.toString().substring(0, sp.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					final String title = "<html>Choose shade tolerance level for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>Use bypass diodes to direct current under shading conditions.<hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, panel }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Partial Shade Tolerance");
					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final SetShadeToleranceCommand c = new SetShadeToleranceCommand(sp);
								sp.setShadeTolerance(SolarPanel.PARTIAL_SHADE_TOLERANCE);
								sp.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final Foundation foundation = sp.getTopContainer();
								final SetShadeToleranceForSolarPanelsOnFoundationCommand c = new SetShadeToleranceForSolarPanelsOnFoundationCommand(foundation);
								foundation.setShadeToleranceForSolarPanels(SolarPanel.PARTIAL_SHADE_TOLERANCE);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetShadeToleranceForAllSolarPanelsCommand c = new SetShadeToleranceForAllSolarPanelsCommand();
								Scene.getInstance().setShadeToleranceForAllSolarPanels(SolarPanel.PARTIAL_SHADE_TOLERANCE);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 2;
							}
							updateAfterEdit();
							if (choice == options[0]) {
								break;
							}
						}
					}
				}
			});

			final JRadioButtonMenuItem miNoTolerance = new JRadioButtonMenuItem("No Tolerance...");
			shadeToleranceButtonGroup.add(miNoTolerance);
			miNoTolerance.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel sp = (SolarPanel) selectedPart;
					final String partInfo = sp.toString().substring(0, sp.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					final String title = "<html>Choose shade tolerance level for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>Shading greatly reduces the output of the entire panel.<hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, panel }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "No Shade Tolerance");
					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final SetShadeToleranceCommand c = new SetShadeToleranceCommand(sp);
								sp.setShadeTolerance(SolarPanel.NO_SHADE_TOLERANCE);
								sp.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final Foundation foundation = sp.getTopContainer();
								final SetShadeToleranceForSolarPanelsOnFoundationCommand c = new SetShadeToleranceForSolarPanelsOnFoundationCommand(foundation);
								foundation.setShadeToleranceForSolarPanels(SolarPanel.NO_SHADE_TOLERANCE);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetShadeToleranceForAllSolarPanelsCommand c = new SetShadeToleranceForAllSolarPanelsCommand();
								Scene.getInstance().setShadeToleranceForAllSolarPanels(SolarPanel.NO_SHADE_TOLERANCE);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 2;
							}
							updateAfterEdit();
							if (choice == options[0]) {
								break;
							}
						}
					}
				}
			});

			final ButtonGroup trackerButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem miNoTracker = new JRadioButtonMenuItem("No Tracker...", true);
			trackerButtonGroup.add(miNoTracker);
			miNoTracker.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel sp = (SolarPanel) selectedPart;
					final String partInfo = sp.toString().substring(0, sp.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					final String title = "<html>Disable tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>No tracker will be used.<hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, panel }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "No Tracker");
					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final SetSolarTrackerCommand c = new SetSolarTrackerCommand(sp);
								sp.setTracker(Trackable.NO_TRACKER);
								sp.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final Foundation foundation = sp.getTopContainer();
								final SetSolarTrackersOnFoundationCommand c = new SetSolarTrackersOnFoundationCommand(foundation, sp);
								foundation.setTrackerForSolarPanels(Trackable.NO_TRACKER);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetSolarTrackersForAllCommand c = new SetSolarTrackersForAllCommand(sp);
								Scene.getInstance().setTrackerForAllSolarPanels(Trackable.NO_TRACKER);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 2;
							}
							updateAfterEdit();
							if (choice == options[0]) {
								break;
							}
						}
					}
				}
			});

			final JRadioButtonMenuItem miHorizontalSingleAxisTracker = new JRadioButtonMenuItem("Horizontal Single-Axis Tracker...");
			trackerButtonGroup.add(miHorizontalSingleAxisTracker);
			miHorizontalSingleAxisTracker.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel sp = (SolarPanel) selectedPart;
					final String partInfo = sp.toString().substring(0, sp.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					final String title = "<html>Enable horizontal single-axis tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2><hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, panel }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Horizontal Single-Axis Tracker");
					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final SetSolarTrackerCommand c = new SetSolarTrackerCommand(sp);
								sp.setTracker(SolarPanel.HORIZONTAL_SINGLE_AXIS_TRACKER);
								sp.draw();
								SceneManager.getInstance().refresh();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final Foundation foundation = sp.getTopContainer();
								final SetSolarTrackersOnFoundationCommand c = new SetSolarTrackersOnFoundationCommand(foundation, sp);
								foundation.setTrackerForSolarPanels(SolarPanel.HORIZONTAL_SINGLE_AXIS_TRACKER);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetSolarTrackersForAllCommand c = new SetSolarTrackersForAllCommand(sp);
								Scene.getInstance().setTrackerForAllSolarPanels(SolarPanel.HORIZONTAL_SINGLE_AXIS_TRACKER);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 2;
							}
							updateAfterEdit();
							if (choice == options[0]) {
								break;
							}
						}
					}
				}
			});

			final JRadioButtonMenuItem miVerticalSingleAxisTracker = new JRadioButtonMenuItem("Vertical Single-Axis Tracker...");
			trackerButtonGroup.add(miVerticalSingleAxisTracker);
			miVerticalSingleAxisTracker.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel sp = (SolarPanel) selectedPart;
					final String partInfo = sp.toString().substring(0, sp.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					final String title = "<html>Enable vertical single-axis tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2><hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, panel }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Vertical Single-Axis Tracker");
					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final SetSolarTrackerCommand c = new SetSolarTrackerCommand(sp);
								sp.setTracker(SolarPanel.VERTICAL_SINGLE_AXIS_TRACKER);
								sp.draw();
								SceneManager.getInstance().refresh();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final Foundation foundation = sp.getTopContainer();
								final SetSolarTrackersOnFoundationCommand c = new SetSolarTrackersOnFoundationCommand(foundation, sp);
								foundation.setTrackerForSolarPanels(SolarPanel.VERTICAL_SINGLE_AXIS_TRACKER);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetSolarTrackersForAllCommand c = new SetSolarTrackersForAllCommand(sp);
								Scene.getInstance().setTrackerForAllSolarPanels(SolarPanel.VERTICAL_SINGLE_AXIS_TRACKER);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 2;
							}
							updateAfterEdit();
							if (choice == options[0]) {
								break;
							}
						}
					}
				}
			});

			final JRadioButtonMenuItem miAltazimuthDualAxisTracker = new JRadioButtonMenuItem("Altazimuth Dual-Axis Tracker...");
			trackerButtonGroup.add(miAltazimuthDualAxisTracker);
			miAltazimuthDualAxisTracker.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel sp = (SolarPanel) selectedPart;
					final String partInfo = sp.toString().substring(0, sp.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					final String title = "<html>Enable altitude-azimuth dual-axis tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The Alt/Az dual-axis solar tracker will rotate the solar panel to face the sun exactly.<hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, panel }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Altitude-Azimuth Dual-Axis Tracker");
					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final SetSolarTrackerCommand c = new SetSolarTrackerCommand(sp);
								sp.setTracker(SolarPanel.ALTAZIMUTH_DUAL_AXIS_TRACKER);
								sp.draw();
								SceneManager.getInstance().refresh();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final Foundation foundation = sp.getTopContainer();
								final SetSolarTrackersOnFoundationCommand c = new SetSolarTrackersOnFoundationCommand(foundation, sp);
								foundation.setTrackerForSolarPanels(SolarPanel.ALTAZIMUTH_DUAL_AXIS_TRACKER);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetSolarTrackersForAllCommand c = new SetSolarTrackersForAllCommand(sp);
								Scene.getInstance().setTrackerForAllSolarPanels(SolarPanel.ALTAZIMUTH_DUAL_AXIS_TRACKER);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 2;
							}
							updateAfterEdit();
							if (choice == options[0]) {
								break;
							}
						}
					}
				}
			});

			final JMenu orientationMenu = new JMenu("Orientation");
			final ButtonGroup orientationGroup = new ButtonGroup();

			final JRadioButtonMenuItem rbmiLandscape = new JRadioButtonMenuItem("Landscape");
			rbmiLandscape.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (rbmiLandscape.isSelected()) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (!(selectedPart instanceof SolarPanel)) {
							return;
						}
						final SolarPanel s = (SolarPanel) selectedPart;
						final RotateSolarPanelCommand c = new RotateSolarPanelCommand(s);
						s.setRotated(true);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						s.draw();
						updateAfterEdit();
					}
				}
			});
			orientationMenu.add(rbmiLandscape);
			orientationGroup.add(rbmiLandscape);

			final JRadioButtonMenuItem rbmiPortrait = new JRadioButtonMenuItem("Portrait", true);
			rbmiPortrait.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (rbmiPortrait.isSelected()) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (!(selectedPart instanceof SolarPanel)) {
							return;
						}
						final SolarPanel s = (SolarPanel) selectedPart;
						final RotateSolarPanelCommand c = new RotateSolarPanelCommand(s);
						s.setRotated(false);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						s.draw();
						updateAfterEdit();
					}
				}
			});
			orientationMenu.add(rbmiPortrait);
			orientationGroup.add(rbmiPortrait);

			final JMenu labelMenu = new JMenu("Label");

			final JCheckBoxMenuItem miLabelNone = new JCheckBoxMenuItem("None", true);
			miLabelNone.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (miLabelNone.isSelected()) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof SolarPanel) {
							final SolarPanel s = (SolarPanel) selectedPart;
							final SetSolarPanelLabelCommand c = new SetSolarPanelLabelCommand(s);
							s.clearLabels();
							s.draw();
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
					if (selectedPart instanceof SolarPanel) {
						final SolarPanel s = (SolarPanel) selectedPart;
						final SetSolarPanelLabelCommand c = new SetSolarPanelLabelCommand(s);
						s.setLabelCustom(miLabelCustom.isSelected());
						if (s.getLabelCustom()) {
							s.setLabelCustomText(JOptionPane.showInputDialog(MainFrame.getInstance(), "Custom Text", s.getLabelCustomText()));
						}
						s.draw();
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
					if (selectedPart instanceof SolarPanel) {
						final SolarPanel s = (SolarPanel) selectedPart;
						final SetSolarPanelLabelCommand c = new SetSolarPanelLabelCommand(s);
						s.setLabelId(miLabelId.isSelected());
						s.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelId);

			final JCheckBoxMenuItem miLabelCellEfficiency = new JCheckBoxMenuItem("Cell Efficiency");
			miLabelCellEfficiency.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof SolarPanel) {
						final SolarPanel s = (SolarPanel) selectedPart;
						final SetSolarPanelLabelCommand c = new SetSolarPanelLabelCommand(s);
						s.setLabelCellEfficiency(miLabelCellEfficiency.isSelected());
						s.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelCellEfficiency);

			final JCheckBoxMenuItem miLabelTiltAngle = new JCheckBoxMenuItem("Tilt Angle");
			miLabelTiltAngle.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof SolarPanel) {
						final SolarPanel s = (SolarPanel) selectedPart;
						final SetSolarPanelLabelCommand c = new SetSolarPanelLabelCommand(s);
						s.setLabelTiltAngle(miLabelTiltAngle.isSelected());
						s.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelTiltAngle);

			final JCheckBoxMenuItem miLabelTracker = new JCheckBoxMenuItem("Tracker");
			miLabelTracker.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof SolarPanel) {
						final SolarPanel s = (SolarPanel) selectedPart;
						final SetSolarPanelLabelCommand c = new SetSolarPanelLabelCommand(s);
						s.setLabelTracker(miLabelTracker.isSelected());
						s.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelTracker);

			final JCheckBoxMenuItem miLabelEnergyOutput = new JCheckBoxMenuItem("Energy Output");
			miLabelEnergyOutput.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof SolarPanel) {
						final SolarPanel s = (SolarPanel) selectedPart;
						final SetSolarPanelLabelCommand c = new SetSolarPanelLabelCommand(s);
						s.setLabelEnergyOutput(miLabelEnergyOutput.isSelected());
						s.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelEnergyOutput);

			final JMenuItem miTiltAngle = new JMenuItem("Tilt Angle...");
			miTiltAngle.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final SolarPanel sp = (SolarPanel) selectedPart;
					final String title = "<html>Tilt Angle of " + partInfo + " (&deg;)</html>";
					final String footnote = "<html><hr><font size=2>The tilt angle of a solar panel is the angle between its surface and the base surface.<br>The tilt angle must be between -90&deg; and 90&deg;.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("Only this Row", true);
					final JRadioButton rb3 = new JRadioButton("All Solar Panels on This Foundation");
					final JRadioButton rb4 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					panel.add(rb4);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					bg.add(rb4);
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
					case 3:
						rb4.setSelected(true);
						break;
					}
					gui.add(panel, BorderLayout.CENTER);
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(sp.getTiltAngle()));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Tilt Angle");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							double val = 0;
							boolean ok = true;
							try {
								val = Double.parseDouble(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (val < -90 || val > 90) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "The tilt angle must be between -90 and 90 degrees.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (Util.isZero(val - 90)) {
										val = 89.999;
									} else if (Util.isZero(val + 90)) {
										val = -89.999;
									}
									if (rb1.isSelected()) {
										final ChangeTiltAngleCommand c = new ChangeTiltAngleCommand(sp);
										sp.setTiltAngle(val);
										sp.draw();
										SceneManager.getInstance().refresh();
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final List<SolarPanel> row = sp.getRow();
										final ChangeTiltAngleForSolarPanelRowCommand c = new ChangeTiltAngleForSolarPanelRowCommand(row);
										for (final SolarPanel x : row) {
											x.setTiltAngle(val);
											x.draw();
										}
										SceneManager.getInstance().refresh();
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final Foundation foundation = sp.getTopContainer();
										final ChangeFoundationSolarPanelTiltAngleCommand c = new ChangeFoundationSolarPanelTiltAngleCommand(foundation);
										foundation.setTiltAngleForSolarPanels(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 2;
									} else if (rb4.isSelected()) {
										final ChangeTiltAngleForAllSolarPanelsCommand c = new ChangeTiltAngleForAllSolarPanelsCommand();
										Scene.getInstance().setTiltAngleForAllSolarPanels(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 3;
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					}
				}
			});

			final JMenuItem miAzimuth = new JMenuItem("Azimuth...");
			miAzimuth.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final SolarPanel sp = (SolarPanel) selectedPart;
					final Foundation foundation = sp.getTopContainer();
					final String title = "<html>Azimuth Angle of " + partInfo + " (&deg;)</html>";
					final String footnote = "<html><hr><font size=2>The azimuth angle is measured clockwise from the true north.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					gui.add(panel, BorderLayout.CENTER);
					double a = sp.getRelativeAzimuth() + foundation.getAzimuth();
					if (a > 360) {
						a -= 360;
					}
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(a));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Azimuth");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							double val = 0;
							boolean ok = true;
							try {
								val = Double.parseDouble(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								a = val - foundation.getAzimuth();
								if (a < 0) {
									a += 360;
								}
								if (rb1.isSelected()) {
									final ChangeAzimuthCommand c = new ChangeAzimuthCommand(sp);
									sp.setRelativeAzimuth(a);
									sp.draw();
									SceneManager.getInstance().refresh();
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 0;
								} else if (rb2.isSelected()) {
									final ChangeFoundationSolarPanelAzimuthCommand c = new ChangeFoundationSolarPanelAzimuthCommand(foundation);
									foundation.setAzimuthForSolarPanels(a);
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 1;
								} else if (rb3.isSelected()) {
									final ChangeAzimuthForAllSolarPanelsCommand c = new ChangeAzimuthForAllSolarPanelsCommand();
									Scene.getInstance().setAzimuthForAllSolarPanels(a);
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 2;
								}
								updateAfterEdit();
								if (choice == options[0]) {
									break;
								}
							}
						}
					}
				}
			});

			final JMenuItem miSize = new JMenuItem("Size...");
			miSize.addActionListener(new ActionListener() {

				private double w = 0.99;
				private double h = 1.65;
				private int numberOfCellsInX = 6;
				private int numberOfCellsInY = 10;

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel s = (SolarPanel) selectedPart;
					final String partInfo = s.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					gui.setBorder(BorderFactory.createTitledBorder("Choose Size for " + partInfo));
					final JComboBox<String> typeComboBox = new JComboBox<String>(new String[] { "0.99m \u00D7 1.65m (6 \u00D7 10 cells)", "1.04m \u00D7 1.55m (8 \u00D7 12 cells)", "0.99m \u00D7 1.96m (6 \u00D7 12 cells)" });
					if (Util.isZero(s.getPanelHeight() - 1.65)) {
						typeComboBox.setSelectedIndex(0);
						w = 0.99;
						h = 1.65;
					} else if (Util.isZero(s.getPanelHeight() - 1.55)) {
						typeComboBox.setSelectedIndex(1);
						w = 1.04;
						h = 1.55;
					} else if (Util.isZero(s.getPanelHeight() - 1.96)) {
						typeComboBox.setSelectedIndex(2);
						w = 0.99;
						h = 1.96;
					}
					typeComboBox.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(final ItemEvent e) {
							switch (typeComboBox.getSelectedIndex()) {
							case 0:
								w = 0.99;
								h = 1.65;
								numberOfCellsInX = 6;
								numberOfCellsInY = 10;
								break;
							case 1:
								w = 1.04;
								h = 1.55;
								numberOfCellsInX = 8;
								numberOfCellsInY = 12;
								break;
							case 2:
								w = 0.99;
								h = 1.96;
								numberOfCellsInX = 6;
								numberOfCellsInY = 12;
								break;
							}
						}
					});
					gui.add(typeComboBox, BorderLayout.NORTH);
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), gui, "Set Size", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					final ChooseSolarPanelSizeCommand c = new ChooseSolarPanelSizeCommand(s);
					s.setPanelWidth(w);
					s.setPanelHeight(h);
					s.setNumberOfCellsInX(numberOfCellsInX);
					s.setNumberOfCellsInY(numberOfCellsInY);
					s.draw();
					SceneManager.getInstance().getUndoManager().addEdit(c);
					updateAfterEdit();
				}
			});

			// @deprecated: module structure is related to size and may not be set independently
			final JMenuItem miModuleStructure = new JMenuItem("Module Structure...");
			miModuleStructure.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel s = (SolarPanel) selectedPart;
					final Foundation foundation = s.getTopContainer();
					int nx = s.getNumberOfCellsInX();
					int ny = s.getNumberOfCellsInY();
					final String partInfo = s.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final JPanel inputFields = new JPanel();
					inputFields.setBorder(BorderFactory.createTitledBorder("Cell Numbers for " + partInfo));
					final JSpinner nxSpinner = new JSpinner(new SpinnerNumberModel(nx, 1, 20, 1));
					inputFields.add(nxSpinner);
					inputFields.add(new JLabel("  \u00D7  "));
					final JSpinner nySpinner = new JSpinner(new SpinnerNumberModel(ny, 1, 20, 1));
					inputFields.add(nySpinner);
					nxSpinner.setEnabled(false);
					nySpinner.setEnabled(false);
					final JPanel scopeFields = new JPanel();
					scopeFields.setLayout(new BoxLayout(scopeFields, BoxLayout.Y_AXIS));
					scopeFields.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					scopeFields.add(rb1);
					scopeFields.add(rb2);
					scopeFields.add(rb3);
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
					final JPanel panel = new JPanel(new BorderLayout(0, 8));
					panel.add(inputFields, BorderLayout.NORTH);
					panel.add(new JLabel(new ImageIcon(PopupMenuFactory.class.getResource("icons/solarcells.png"))));
					panel.add(scopeFields, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Cells of Module");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							nx = (Integer) nxSpinner.getValue();
							ny = (Integer) nySpinner.getValue();
							if (rb1.isSelected()) {
								final ChangeCellNumbersCommand c = new ChangeCellNumbersCommand(s);
								s.setNumberOfCellsInX(nx);
								s.setNumberOfCellsInY(ny);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final ChangeFoundationSolarPanelCellNumbersCommand c = new ChangeFoundationSolarPanelCellNumbersCommand(foundation);
								foundation.setCellNumbersForSolarPanels(nx, ny);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final ChangeCellNumbersForAllSolarPanelsCommand c = new ChangeCellNumbersForAllSolarPanelsCommand();
								Scene.getInstance().setCellNumbersForAllSolarPanels(nx, ny);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 2;
							}
							updateAfterEdit();
							if (choice == options[0]) {
								break;
							}
						}
					}
				}
			});

			final JMenuItem miBaseHeight = new JMenuItem("Base Height...");
			miBaseHeight.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final SolarPanel sp = (SolarPanel) selectedPart;
					final Foundation foundation = sp.getTopContainer();
					final String title = "<html>Base Height (m) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("Only this Row");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb4 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
					panel.add(rb4);
					final ButtonGroup bg = new ButtonGroup();
					bg.add(rb1);
					bg.add(rb2);
					bg.add(rb3);
					bg.add(rb4);
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
					case 3:
						rb4.setSelected(true);
						break;
					}
					gui.add(panel, BorderLayout.CENTER);
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(sp.getBaseHeight() * Scene.getInstance().getAnnotationScale()));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Base Height");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							double val = 0;
							boolean ok = true;
							try {
								val = Double.parseDouble(inputField.getText()) / Scene.getInstance().getAnnotationScale();
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (rb1.isSelected()) {
									final ChangeBaseHeightCommand c = new ChangeBaseHeightCommand(sp);
									sp.setBaseHeight(val);
									sp.draw();
									SceneManager.getInstance().refresh();
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 0;
								} else if (rb2.isSelected()) {
									final List<SolarPanel> row = sp.getRow();
									final ChangeBaseHeightForSolarPanelRowCommand c = new ChangeBaseHeightForSolarPanelRowCommand(row);
									for (final SolarPanel x : row) {
										x.setBaseHeight(val);
										x.draw();
									}
									SceneManager.getInstance().refresh();
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 1;
								} else if (rb3.isSelected()) {
									final ChangeFoundationSolarPanelBaseHeightCommand c = new ChangeFoundationSolarPanelBaseHeightCommand(foundation);
									foundation.setBaseHeightForSolarPanels(val);
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 2;
								} else if (rb4.isSelected()) {
									final ChangeBaseHeightForAllSolarPanelsCommand c = new ChangeBaseHeightForAllSolarPanelsCommand();
									Scene.getInstance().setBaseHeightForAllSolarPanels(val);
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 3;
								}
								updateAfterEdit();
								if (choice == options[0]) {
									break;
								}
							}
						}
					}
				}
			});

			final JCheckBoxMenuItem cbmiDrawSunBeam = new JCheckBoxMenuItem("Draw Sun Beam");
			cbmiDrawSunBeam.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel sp = (SolarPanel) selectedPart;
					sp.setSunBeamVisible(cbmiDrawSunBeam.isSelected());
					sp.drawSunBeam();
					sp.draw();
					Scene.getInstance().setEdited(true);
				}
			});

			popupMenuForSolarPanel = createPopupMenu(true, true, new Runnable() {
				@Override
				public void run() {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel sp = (SolarPanel) selectedPart;
					switch (sp.getShadeTolerance()) {
					case SolarPanel.HIGH_SHADE_TOLERANCE:
						Util.selectSilently(miHighTolerance, true);
						break;
					case SolarPanel.PARTIAL_SHADE_TOLERANCE:
						Util.selectSilently(miPartialTolerance, true);
						break;
					case SolarPanel.NO_SHADE_TOLERANCE:
						Util.selectSilently(miNoTolerance, true);
						break;
					}
					Util.selectSilently(cbmiDrawSunBeam, sp.isDrawSunBeamVisible());
					Util.selectSilently(rbmiLandscape, sp.isRotated());
					Util.selectSilently(rbmiPortrait, !sp.isRotated());
					Util.selectSilently(miLabelNone, !sp.isLabelVisible());
					Util.selectSilently(miLabelCustom, sp.getLabelCustom());
					Util.selectSilently(miLabelId, sp.getLabelId());
					Util.selectSilently(miLabelCellEfficiency, sp.getLabelCellEfficiency());
					Util.selectSilently(miLabelTiltAngle, sp.getLabelTiltAngle());
					Util.selectSilently(miLabelTracker, sp.getLabelTracker());
					Util.selectSilently(miLabelEnergyOutput, sp.getLabelEnergyOutput());

					switch (sp.getTracker()) {
					case Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER:
						Util.selectSilently(miAltazimuthDualAxisTracker, true);
						break;
					case Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER:
						Util.selectSilently(miHorizontalSingleAxisTracker, true);
						break;
					case Trackable.VERTICAL_SINGLE_AXIS_TRACKER:
						Util.selectSilently(miVerticalSingleAxisTracker, true);
						break;
					case Trackable.NO_TRACKER:
						Util.selectSilently(miNoTracker, true);
						break;
					}
					miAltazimuthDualAxisTracker.setEnabled(true);
					miHorizontalSingleAxisTracker.setEnabled(true);
					miVerticalSingleAxisTracker.setEnabled(true);
					if (sp.getContainer() instanceof Roof) {
						final Roof roof = (Roof) sp.getContainer();
						final boolean flat = Util.isZero(roof.getHeight());
						miAltazimuthDualAxisTracker.setEnabled(flat);
						miHorizontalSingleAxisTracker.setEnabled(flat);
						miVerticalSingleAxisTracker.setEnabled(flat);
					} else if (sp.getContainer() instanceof Wall || sp.getContainer() instanceof Rack) {
						miAltazimuthDualAxisTracker.setEnabled(false);
						miHorizontalSingleAxisTracker.setEnabled(false);
						miVerticalSingleAxisTracker.setEnabled(false);
					}
					if (sp.getTracker() != Trackable.NO_TRACKER) {
						miTiltAngle.setEnabled(sp.getTracker() == Trackable.VERTICAL_SINGLE_AXIS_TRACKER);
						miAzimuth.setEnabled(false);
					} else {
						miTiltAngle.setEnabled(true);
						miAzimuth.setEnabled(true);
						miBaseHeight.setEnabled(true);
						if (sp.getContainer() instanceof Roof) {
							final Roof roof = (Roof) sp.getContainer();
							if (roof.getHeight() > 0) {
								miTiltAngle.setEnabled(false);
								miAzimuth.setEnabled(false);
								miBaseHeight.setEnabled(false);
							}
						} else if (sp.getContainer() instanceof Wall || sp.getContainer() instanceof Rack) {
							miTiltAngle.setEnabled(false);
							miAzimuth.setEnabled(false);
							miBaseHeight.setEnabled(false);
						}
					}
				}
			});

			final JMenuItem miCells = new JMenuItem("Solar Cells...");
			miCells.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final SolarPanel solarPanel = (SolarPanel) selectedPart;
					final String title = "<html>Solar Cell Properties of " + selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1) + "</html>";
					String footnote = "<html><hr><font size=2><b>How efficiently can a solar cell convert light into electricity?</b><br>The Shockley-Queisser limit is 34%. The theoretical limit for multilayer cells<br>is 86%. As of 2017, the best solar panel in the market has an efficiency of 24%.<br>The highest efficiency you can choose is limited to " + SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "%.<hr>";
					footnote += "<font size=2>Solar cells made of monocrystalline silicon are usually round or semi-round.<br>Hence, there is a small fraction of area on a solar panel not covered by cells.<br>In other words, a monocrystalline solar panel has a smaller packing density.";
					footnote += "<br><font size=2>Solar cells made of polycrystalline silicon are usually square. Compared with a<br>monocrystalline solar panel, a polycrystalline one has a higher packing density.<br>Color has no relationship with efficiency.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.SOUTH);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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

					final JPanel inputPanel = new JPanel(new SpringLayout());
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					gui.add(inputPanel, BorderLayout.CENTER);
					JLabel label = new JLabel("Type: ", JLabel.LEFT);
					inputPanel.add(label);
					final JComboBox<String> typeComboBox = new JComboBox<String>(new String[] { "Monocrystalline", "Polycrystalline" });
					if (solarPanel.getCellType() == SolarPanel.POLYCRYSTALLINE) {
						typeComboBox.setSelectedIndex(1);
					}
					label.setLabelFor(typeComboBox);
					inputPanel.add(typeComboBox);
					label = new JLabel("Color: ", JLabel.LEFT);
					inputPanel.add(label);
					final JComboBox<String> colorComboBox = new JComboBox<String>(new String[] { "Blue", "Black" });
					if (solarPanel.getColorOption() == SolarPanel.COLOR_OPTION_BLACK) {
						colorComboBox.setSelectedIndex(1);
					}
					label.setLabelFor(colorComboBox);
					inputPanel.add(colorComboBox);
					label = new JLabel("Efficiency (%): ", JLabel.LEFT);
					inputPanel.add(label);
					final JTextField efficiencyField = new JTextField(EnergyPanel.TWO_DECIMALS.format(solarPanel.getCellEfficiency() * 100));
					label.setLabelFor(efficiencyField);
					inputPanel.add(efficiencyField);
					SpringUtilities.makeCompactGrid(inputPanel, 3, 2, 6, 6, 6, 6);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Cell Properties");

					while (true) {
						efficiencyField.selectAll();
						efficiencyField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							double val = 0;
							boolean ok = true;
							try {
								val = Double.parseDouble(efficiencyField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), efficiencyField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (val < SolarPanel.MIN_SOLAR_CELL_EFFICIENCY_PERCENTAGE || val > SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar cell efficiency must be between " + SolarPanel.MIN_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "% and " + SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									final int cellType = typeComboBox.getSelectedIndex();
									final int colorOption = colorComboBox.getSelectedIndex();
									if (rb1.isSelected()) {
										final ChangeSolarCellPropertiesCommand c = new ChangeSolarCellPropertiesCommand(solarPanel);
										solarPanel.setCellEfficiency(val * 0.01);
										solarPanel.setCellType(cellType);
										solarPanel.setColorOption(colorOption);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = solarPanel.getTopContainer();
										final ChangeFoundationSolarCellPropertiesCommand c = new ChangeFoundationSolarCellPropertiesCommand(foundation);
										foundation.setSolarCellEfficiency(val * 0.01);
										foundation.setCellTypeForSolarPanels(cellType);
										foundation.setColorForSolarPanels(colorOption);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final ChangeSolarCellPropertiesForAllCommand c = new ChangeSolarCellPropertiesForAllCommand();
										Scene.getInstance().setSolarCellEfficiencyForAll(val * 0.01);
										Scene.getInstance().setCellTypeForAllSolarPanels(cellType);
										Scene.getInstance().setColorForAllSolarPanels(colorOption);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 2;
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					}
				}
			});

			final JMenuItem miTemperatureEffects = new JMenuItem("Temperature Effects...");
			miTemperatureEffects.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final SolarPanel solarPanel = (SolarPanel) selectedPart;
					final String title = "<html>Temperature Effects of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>Increased temperature reduces solar cell efficiency. To determine this temperature effect,<br>it is important to know the expected operating temperature: the Nominal Operating Cell<br>Temperature (NOCT). NOCT ranges from 33&deg;C to 58&deg;C.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					gui.add(panel, BorderLayout.SOUTH);

					final JPanel inputPanel = new JPanel(new SpringLayout());
					gui.add(inputPanel, BorderLayout.CENTER);
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

					JLabel label = new JLabel("<html>Nominal Operating Cell Temperature (&deg;C): ", JLabel.LEFT);
					inputPanel.add(label);
					final JTextField noctField = new JTextField(EnergyPanel.TWO_DECIMALS.format(solarPanel.getNominalOperatingCellTemperature()));
					label.setLabelFor(noctField);
					inputPanel.add(noctField);
					label = new JLabel("<html>Temperature Coefficient of Pmax (%/&deg;C): ", JLabel.LEFT);
					inputPanel.add(label);
					final JTextField pmaxField = new JTextField(EnergyPanel.TWO_DECIMALS.format(solarPanel.getTemperatureCoefficientPmax() * 100));
					label.setLabelFor(pmaxField);
					inputPanel.add(pmaxField);
					SpringUtilities.makeCompactGrid(inputPanel, 2, 2, 6, 6, 6, 6);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Temperature Effects");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							double noct = 0;
							double pmax = 0;
							boolean ok = true;
							try {
								noct = Double.parseDouble(noctField.getText());
								pmax = Double.parseDouble(pmaxField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (noct < 33 || noct > 58) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Nominal Operating Cell Temperature must be between 33 and 58 Celsius degrees.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (pmax < -1 || pmax > 0) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Temperature coefficient of Pmax must be between -1% and 0% per Celsius degree.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final SetTemperatureEffectsCommand c = new SetTemperatureEffectsCommand(solarPanel);
										solarPanel.setTemperatureCoefficientPmax(pmax * 0.01);
										solarPanel.setNominalOperatingCellTemperature(noct);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = solarPanel.getTopContainer();
										final SetFoundationTemperatureEffectsCommand c = new SetFoundationTemperatureEffectsCommand(foundation);
										foundation.setTemperatureCoefficientPmax(pmax * 0.01);
										foundation.setNominalOperatingCellTemperature(noct);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final SetTemperatrureEffectsForAllCommand c = new SetTemperatrureEffectsForAllCommand();
										Scene.getInstance().setTemperatureCoefficientPmaxForAll(pmax * 0.01);
										Scene.getInstance().setNominalOperatingCellTemperatureForAll(noct);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 2;
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					}
				}
			});

			final JMenuItem miInverterEff = new JMenuItem("Inverter Efficiency...");
			miInverterEff.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final SolarPanel solarPanel = (SolarPanel) selectedPart;
					final String title = "<html>Inverter Efficiency (%) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The efficiency of a micro inverter for converting electricity from DC to AC is typically 95%.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Solar Panel", true);
					final JRadioButton rb2 = new JRadioButton("All Solar Panels on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Solar Panels");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					gui.add(panel, BorderLayout.CENTER);
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(solarPanel.getInverterEfficiency() * 100));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Inverter Efficiency");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							double val = 0;
							boolean ok = true;
							try {
								val = Double.parseDouble(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (val < SolarPanel.MIN_INVERTER_EFFICIENCY_PERCENTAGE || val >= SolarPanel.MAX_INVERTER_EFFICIENCY_PERCENTAGE) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Inverter efficiency must be greater than " + SolarPanel.MIN_INVERTER_EFFICIENCY_PERCENTAGE + "% and less than " + SolarPanel.MAX_INVERTER_EFFICIENCY_PERCENTAGE + "%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final ChangeInverterEfficiencyCommand c = new ChangeInverterEfficiencyCommand(solarPanel);
										solarPanel.setInverterEfficiency(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = solarPanel.getTopContainer();
										final ChangeFoundationInverterEfficiencyCommand c = new ChangeFoundationInverterEfficiencyCommand(foundation);
										foundation.setSolarPanelInverterEfficiency(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final ChangeInverterEfficiencyForAllCommand c = new ChangeInverterEfficiencyForAllCommand();
										Scene.getInstance().setSolarPanelInverterEfficiencyForAll(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 2;
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					}
				}
			});

			trackerMenu.add(miNoTracker);
			trackerMenu.add(miHorizontalSingleAxisTracker);
			trackerMenu.add(miVerticalSingleAxisTracker);
			trackerMenu.add(miAltazimuthDualAxisTracker);

			shadeToleranceMenu.add(miNoTolerance);
			shadeToleranceMenu.add(miPartialTolerance);
			shadeToleranceMenu.add(miHighTolerance);

			final JMenuItem miDeleteRow = new JMenuItem("Delete Row");
			miDeleteRow.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof SolarPanel)) {
						return;
					}
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllSolarPanels(((SolarPanel) selectedPart).getRow());
							return null;
						}
					});
				}
			});
			popupMenuForSolarPanel.add(miDeleteRow);

			popupMenuForSolarPanel.addSeparator();
			popupMenuForSolarPanel.add(miCells);
			popupMenuForSolarPanel.add(miSize);
			popupMenuForSolarPanel.add(miTemperatureEffects);
			popupMenuForSolarPanel.add(orientationMenu);
			popupMenuForSolarPanel.add(shadeToleranceMenu);
			popupMenuForSolarPanel.addSeparator();
			popupMenuForSolarPanel.add(miTiltAngle);
			popupMenuForSolarPanel.add(miAzimuth);
			popupMenuForSolarPanel.add(miBaseHeight);
			popupMenuForSolarPanel.add(miInverterEff);
			popupMenuForSolarPanel.add(trackerMenu);
			popupMenuForSolarPanel.addSeparator();
			popupMenuForSolarPanel.add(cbmiDrawSunBeam);
			popupMenuForSolarPanel.add(labelMenu);
			popupMenuForSolarPanel.addSeparator();

			JMenuItem mi = new JMenuItem("Daily Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof SolarPanel) {
						new PvDailyAnalysis().show();
					}
				}
			});
			popupMenuForSolarPanel.add(mi);

			mi = new JMenuItem("Annual Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof SolarPanel) {
						new PvAnnualAnalysis().show();
					}
				}
			});
			popupMenuForSolarPanel.add(mi);

		}

		return popupMenuForSolarPanel;

	}

	private static JPopupMenu getPopupMenuForRack() {

		if (popupMenuForRack == null) {

			final JMenu trackerMenu = new JMenu("Tracker");

			final JMenuItem miPaste = new JMenuItem("Paste");
			miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			miPaste.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().pasteToPickedLocationOnRack();
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
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack rack = (Rack) selectedPart;
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							if (rack.isMonolithic()) {
								rack.setMonolithic(false);
								rack.draw();
							} else {
								Scene.getInstance().removeAllSolarPanels(null);
								EventQueue.invokeLater(new Runnable() {
									@Override
									public void run() {
										MainPanel.getInstance().getEnergyViewButton().setSelected(false);
									}
								});
							}
							return null;
						}
					});
				}
			});

			final JMenuItem miTiltAngle = new JMenuItem("Tilt Angle...");
			miTiltAngle.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Rack rack = (Rack) selectedPart;
					final String title = "<html>Tilt Angle of " + partInfo + " (&deg;)</html>";
					final String footnote = "<html><hr><font size=2>The tilt angle of a rack is the angle between its surface and the ground surface.<br>The tilt angle must be between -90&deg; and 90&deg;.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on This Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					gui.add(panel, BorderLayout.CENTER);
					final JTextField inputField = new JTextField(rack.getTiltAngle() + "");
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Tilt Angle");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							double val = 0;
							boolean ok = true;
							try {
								val = Double.parseDouble(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (val < -90 || val > 90) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "The tilt angle must be between -90 and 90 degrees.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (Util.isZero(val - 90)) {
										val = 89.999;
									} else if (Util.isZero(val + 90)) {
										val = -89.999;
									}
									if (rb1.isSelected()) {
										final ChangeTiltAngleCommand c = new ChangeTiltAngleCommand(rack);
										rack.setTiltAngle(val);
										rack.draw();
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = rack.getTopContainer();
										final ChangeFoundationRackTiltAngleCommand c = new ChangeFoundationRackTiltAngleCommand(foundation);
										foundation.setTiltAngleForRacks(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final ChangeTiltAngleForAllRacksCommand c = new ChangeTiltAngleForAllRacksCommand();
										Scene.getInstance().setTiltAngleForAllRacks(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 2;
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					}
				}
			});

			final JMenuItem miAzimuth = new JMenuItem("Azimuth...");
			miAzimuth.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Rack rack = (Rack) selectedPart;
					final Foundation foundation = rack.getTopContainer();
					final String title = "<html>Azimuth Angle of " + partInfo + " (&deg;)</html>";
					final String footnote = "<html><hr><font size=2>The azimuth angle is measured clockwise from the true north.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					gui.add(panel, BorderLayout.CENTER);
					double a = rack.getRelativeAzimuth() + foundation.getAzimuth();
					if (a > 360) {
						a -= 360;
					}
					final JTextField inputField = new JTextField(a + "");
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Azimuth Angle");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							try {
								a = Double.parseDouble(inputField.getText()) - foundation.getAzimuth();
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (a < 0) {
									a += 360;
								}
								if (rb1.isSelected()) {
									final ChangeAzimuthCommand c = new ChangeAzimuthCommand(rack);
									rack.setRelativeAzimuth(a);
									rack.draw();
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 0;
								} else if (rb2.isSelected()) {
									final ChangeFoundationRackAzimuthCommand c = new ChangeFoundationRackAzimuthCommand(foundation);
									foundation.setAzimuthForRacks(a);
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 1;
								} else if (rb3.isSelected()) {
									final ChangeAzimuthForAllRacksCommand c = new ChangeAzimuthForAllRacksCommand();
									Scene.getInstance().setAzimuthForAllRacks(a);
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 2;
								}
								updateAfterEdit();
								if (choice == options[0]) {
									break;
								}
							}
						}
					}
				}
			});

			final JMenuItem miRackSize = new JMenuItem("Size...");
			miRackSize.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack rack = (Rack) selectedPart;
					final Foundation foundation = rack.getTopContainer();
					final String partInfo = rack.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel inputPanel = new JPanel(new SpringLayout());
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					gui.add(inputPanel, BorderLayout.CENTER);
					JLabel label = new JLabel("Width (m): ", JLabel.TRAILING);
					inputPanel.add(label);
					final JTextField widthField = new JTextField(threeDecimalsFormat.format(rack.getRackWidth()));
					label.setLabelFor(widthField);
					inputPanel.add(widthField);
					label = new JLabel("Length (m): ", JLabel.TRAILING);
					inputPanel.add(label);
					final JTextField heightField = new JTextField(threeDecimalsFormat.format(rack.getRackHeight()));
					label.setLabelFor(heightField);
					inputPanel.add(heightField);
					SpringUtilities.makeCompactGrid(inputPanel, 2, 2, 6, 6, 6, 6);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					final JOptionPane optionPane = new JOptionPane(new Object[] { "Set Size for " + partInfo, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Rack Size");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							double w = 0, h = 0;
							boolean ok = true;
							try {
								w = Double.parseDouble(widthField.getText());
								h = Double.parseDouble(heightField.getText());
							} catch (final NumberFormatException x) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (w < 1 || w > 500) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Width must be between 1 and 500 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (h < 1 || h > 20) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Length must be between 1 and 20 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final SetPartSizeCommand c = new SetPartSizeCommand(rack);
										rack.setRackWidth(w);
										rack.setRackHeight(h);
										rack.ensureFullSolarPanels(false);
										rack.draw();
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final SetSizeForRacksOnFoundationCommand c = new SetSizeForRacksOnFoundationCommand(foundation);
										foundation.setSizeForRacks(w, h);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final SetSizeForAllRacksCommand c = new SetSizeForAllRacksCommand();
										Scene.getInstance().setSizeForAllRacks(w, h);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 2;
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					}
				}
			});

			final JMenuItem miBaseHeight = new JMenuItem("Base Height...");
			miBaseHeight.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Rack rack = (Rack) selectedPart;
					final Foundation foundation = rack.getTopContainer();
					final String title = "<html>Base Height (m) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2></html>";
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					final JPanel gui = new JPanel(new BorderLayout());
					gui.add(panel, BorderLayout.CENTER);
					final JTextField inputField = new JTextField(rack.getBaseHeight() * Scene.getInstance().getAnnotationScale() + "");
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Base Height");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							double val = 0;
							try {
								val = Double.parseDouble(inputField.getText()) / Scene.getInstance().getAnnotationScale();
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (rb1.isSelected()) {
									final ChangeBaseHeightCommand c = new ChangeBaseHeightCommand(rack);
									rack.setBaseHeight(val);
									rack.draw();
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 0;
								} else if (rb2.isSelected()) {
									final ChangeFoundationRackBaseHeightCommand c = new ChangeFoundationRackBaseHeightCommand(foundation);
									foundation.setBaseHeightForRacks(val);
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 1;
								} else if (rb3.isSelected()) {
									final ChangeBaseHeightForAllRacksCommand c = new ChangeBaseHeightForAllRacksCommand();
									Scene.getInstance().setBaseHeightForAllRacks(val);
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 2;
								}
								updateAfterEdit();
								if (choice == options[0]) {
									break;
								}
							}
						}
					}
				}
			});

			final JMenuItem miPoleSpacing = new JMenuItem("Pole Settings...");
			miPoleSpacing.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack rack = (Rack) selectedPart;
					final String partInfo = rack.toString().substring(0, rack.toString().indexOf(')') + 1);
					final String title = "<html>Pole Settings of " + partInfo + "</html>";

					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
					gui.add(inputPanel, BorderLayout.CENTER);
					inputPanel.add(new JLabel("Distance X (m): "));
					final JTextField dxField = new JTextField(threeDecimalsFormat.format(rack.getPoleDistanceX()));
					inputPanel.add(dxField);
					inputPanel.add(new JLabel("Distance Y (m): "));
					final JTextField dyField = new JTextField(threeDecimalsFormat.format(rack.getPoleDistanceY()));
					inputPanel.add(dyField);
					inputPanel.add(new JLabel("Visible: "));
					final JComboBox<String> visibleComboBox = new JComboBox<String>(new String[] { "Yes", "No" });
					visibleComboBox.setSelectedIndex(rack.isPoleVisible() ? 0 : 1);
					inputPanel.add(visibleComboBox);
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Pole Settings");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							double dx = 0, dy = 0;
							try {
								dx = Double.parseDouble(dxField.getText());
								dy = Double.parseDouble(dyField.getText());
							} catch (final NumberFormatException x) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (dx < 1 || dx > 50) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Dx must be between 1 and 50 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (dy < 1 || dy > 50) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Dy must be between 1 and 50 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									final boolean visible = visibleComboBox.getSelectedIndex() == 0;
									if (rb1.isSelected()) {
										final ChangeRackPoleSettingsCommand c = new ChangeRackPoleSettingsCommand(rack);
										rack.setPoleDistanceX(dx);
										rack.setPoleDistanceY(dy);
										rack.setPoleVisible(visible);
										rack.draw();
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = rack.getTopContainer();
										final ChangePoleSettingsForRacksOnFoundationCommand c = new ChangePoleSettingsForRacksOnFoundationCommand(foundation);
										foundation.setPoleSpacingForRacks(dx, dy, visible);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final ChangePoleSettingsForAllRacksCommand c = new ChangePoleSettingsForAllRacksCommand();
										Scene.getInstance().setPoleSpacingForAllRacks(dx, dy, visible);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 2;
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					}

				}
			});

			final JMenuItem miSolarPanelArray = new JMenuItem("Solar Panel Array...");
			miSolarPanelArray.addActionListener(new ActionListener() {

				private Rack rack;
				private JComboBox<String> sizeComboBox;
				private JComboBox<String> orientationComboBox;
				private JComboBox<String> cellTypeComboBox;
				private JComboBox<String> colorOptionComboBox;
				private JComboBox<String> shadeToleranceComboBox;
				private double cellEfficiency;
				private double inverterEfficiency;
				private double pmax;
				private double noct;
				private int nx, ny;

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					rack = (Rack) selectedPart;
					final int n = rack.getChildren().size();
					if (n > 0 && JOptionPane.showConfirmDialog(MainFrame.getInstance(), "All existing " + n + " solar panels on this rack must be removed before\na new layout can be applied. Do you want to continue?", "Confirmation", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					final SolarPanel solarPanel = rack.getSolarPanel();
					final JPanel panel = new JPanel(new SpringLayout());
					panel.add(new JLabel("Panel Size:"));
					sizeComboBox = new JComboBox<String>(new String[] { "0.99m \u00D7 1.65m (6 \u00D7 10 cells)", "1.04m \u00D7 1.55m (8 \u00D7 12 cells)", "0.99m \u00D7 1.96m (6 \u00D7 12 cells)" });
					if (Util.isZero(0.99 - solarPanel.getPanelWidth()) && Util.isZero(1.65 - solarPanel.getPanelHeight())) {
						sizeComboBox.setSelectedIndex(0);
					} else if (Util.isZero(1.04 - solarPanel.getPanelWidth()) && Util.isZero(1.55 - solarPanel.getPanelHeight())) {
						sizeComboBox.setSelectedIndex(1);
					} else {
						sizeComboBox.setSelectedIndex(2);
					}
					panel.add(sizeComboBox);
					panel.add(new JLabel("Orientation:"));
					orientationComboBox = new JComboBox<String>(new String[] { "Portrait", "Landscape" });
					orientationComboBox.setSelectedIndex(solarPanel.isRotated() ? 1 : 0);
					panel.add(orientationComboBox);
					panel.add(new JLabel("Number of Cells in X:"));
					final JTextField nxField = new JTextField("" + solarPanel.getNumberOfCellsInX());
					panel.add(nxField);
					panel.add(new JLabel("Number of Cells in Y:"));
					final JTextField nyField = new JTextField("" + solarPanel.getNumberOfCellsInY());
					panel.add(nyField);
					panel.add(new JLabel("Cell Type:"));
					cellTypeComboBox = new JComboBox<String>(new String[] { "Monocrystalline", "Polycrystalline" });
					cellTypeComboBox.setSelectedIndex(solarPanel.getCellType());
					panel.add(cellTypeComboBox);
					panel.add(new JLabel("Color:"));
					colorOptionComboBox = new JComboBox<String>(new String[] { "Blue", "Black" });
					colorOptionComboBox.setSelectedIndex(solarPanel.getColorOption());
					panel.add(colorOptionComboBox);
					panel.add(new JLabel("Solar Cell Efficiency (%):"));
					final JTextField cellEfficiencyField = new JTextField(threeDecimalsFormat.format(solarPanel.getCellEfficiency() * 100));
					panel.add(cellEfficiencyField);
					panel.add(new JLabel("<html>Nominal Operating Cell Temperature (&deg;C):"));
					final JTextField noctField = new JTextField(threeDecimalsFormat.format(solarPanel.getNominalOperatingCellTemperature()));
					panel.add(noctField);
					panel.add(new JLabel("<html>Temperature Coefficient of Pmax (%/&deg;C):"));
					final JTextField pmaxField = new JTextField(sixDecimalsFormat.format(solarPanel.getTemperatureCoefficientPmax() * 100));
					panel.add(pmaxField);
					panel.add(new JLabel("Shade Tolerance:"));
					shadeToleranceComboBox = new JComboBox<String>(new String[] { "Partial", "High", "None" });
					shadeToleranceComboBox.setSelectedIndex(solarPanel.getShadeTolerance());
					panel.add(shadeToleranceComboBox);
					panel.add(new JLabel("Inverter Efficiency (%):"));
					final JTextField inverterEfficiencyField = new JTextField(threeDecimalsFormat.format(solarPanel.getInverterEfficiency() * 100));
					panel.add(inverterEfficiencyField);
					SpringUtilities.makeCompactGrid(panel, 11, 2, 6, 6, 6, 6);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Array Options");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							try {
								cellEfficiency = Double.parseDouble(cellEfficiencyField.getText());
								inverterEfficiency = Double.parseDouble(inverterEfficiencyField.getText());
								pmax = Double.parseDouble(pmaxField.getText());
								noct = Double.parseDouble(noctField.getText());
								nx = Integer.parseInt(nxField.getText());
								ny = Integer.parseInt(nyField.getText());
							} catch (final NumberFormatException ex) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (cellEfficiency < SolarPanel.MIN_SOLAR_CELL_EFFICIENCY_PERCENTAGE || cellEfficiency > SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar cell efficiency must be between " + SolarPanel.MIN_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "% and " + SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (inverterEfficiency < SolarPanel.MIN_INVERTER_EFFICIENCY_PERCENTAGE || inverterEfficiency >= SolarPanel.MAX_INVERTER_EFFICIENCY_PERCENTAGE) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Inverter efficiency must be greater than " + SolarPanel.MIN_INVERTER_EFFICIENCY_PERCENTAGE + "% and less than " + SolarPanel.MAX_INVERTER_EFFICIENCY_PERCENTAGE + "%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (pmax < -1 || pmax > 0) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Temperature coefficient of Pmax must be between -1% and 0% per Celsius degree.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (noct < 33 || noct > 58) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Nominal Cell Operating Temperature must be between 33 and 58 Celsius degrees.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (nx <= 0 || nx > 20 || ny <= 0 || ny > 20) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Number of cells in X or Y direction must be between 1 and 20.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									setSolarPanels();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					}
				}

				private void setSolarPanels() {
					final SolarPanel solarPanel = rack.getSolarPanel();
					final SetSolarPanelArrayOnRackCommand command = rack.isMonolithic() ? new SetSolarPanelArrayOnRackCommand(rack) : null;
					switch (sizeComboBox.getSelectedIndex()) {
					case 0:
						solarPanel.setPanelWidth(0.99);
						solarPanel.setPanelHeight(1.65);
						solarPanel.setNumberOfCellsInX(6);
						solarPanel.setNumberOfCellsInX(10);
						break;
					case 1:
						solarPanel.setPanelWidth(1.04);
						solarPanel.setPanelHeight(1.55);
						solarPanel.setNumberOfCellsInX(8);
						solarPanel.setNumberOfCellsInX(12);
						break;
					default:
						solarPanel.setPanelWidth(0.99);
						solarPanel.setPanelHeight(1.96);
						solarPanel.setNumberOfCellsInX(6);
						solarPanel.setNumberOfCellsInX(12);
						break;
					}
					solarPanel.setRotated(orientationComboBox.getSelectedIndex() == 1);
					solarPanel.setNumberOfCellsInX(nx);
					solarPanel.setNumberOfCellsInY(ny);
					solarPanel.setCellType(cellTypeComboBox.getSelectedIndex());
					solarPanel.setColorOption(colorOptionComboBox.getSelectedIndex());
					solarPanel.setCellEfficiency(cellEfficiency * 0.01);
					solarPanel.setInverterEfficiency(inverterEfficiency * 0.01);
					solarPanel.setTemperatureCoefficientPmax(pmax * 0.01);
					solarPanel.setNominalOperatingCellTemperature(noct);
					solarPanel.setShadeTolerance(shadeToleranceComboBox.getSelectedIndex());
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							rack.addSolarPanels();
							if (command != null) {
								SceneManager.getInstance().getUndoManager().addEdit(command);
							}
							return null;
						}
					});
					updateAfterEdit();
				}

			});

			final JMenu solarPanelMenu = new JMenu("Solar Panel Properties");

			final JMenuItem miSolarPanelSize = new JMenuItem("Size...");
			solarPanelMenu.add(miSolarPanelSize);
			miSolarPanelSize.addActionListener(new ActionListener() {

				private double w = 0.99;
				private double h = 1.65;
				private int numberOfCellsInX = 6;
				private int numberOfCellsInY = 10;
				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final SolarPanel s = r.getSolarPanel();
					w = s.getPanelWidth();
					h = s.getPanelHeight();
					final String partInfo = r.toString().substring(0, r.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					gui.setBorder(BorderFactory.createTitledBorder("Solar Panel Size for " + partInfo));
					final JComboBox<String> typeComboBox = new JComboBox<String>(new String[] { "0.99m \u00D7 1.65m (6 \u00D7 10 cells)", "1.04m \u00D7 1.55m (8 \u00D7 12 cells)", "0.99m \u00D7 1.96m (6 \u00D7 12 cells)" });
					if (Util.isZero(s.getPanelHeight() - 1.65)) {
						typeComboBox.setSelectedIndex(0);
					} else if (Util.isZero(s.getPanelHeight() - 1.55)) {
						typeComboBox.setSelectedIndex(1);
					} else if (Util.isZero(s.getPanelHeight() - 1.96)) {
						typeComboBox.setSelectedIndex(2);
					}
					typeComboBox.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(final ItemEvent e) {
							switch (typeComboBox.getSelectedIndex()) {
							case 0:
								w = 0.99;
								h = 1.65;
								numberOfCellsInX = 6;
								numberOfCellsInY = 10;
								break;
							case 1:
								w = 1.04;
								h = 1.55;
								numberOfCellsInX = 8;
								numberOfCellsInY = 12;
								break;
							case 2:
								w = 0.99;
								h = 1.96;
								numberOfCellsInX = 6;
								numberOfCellsInY = 12;
								break;
							}
						}
					});
					gui.add(typeComboBox, BorderLayout.NORTH);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					gui.add(scopePanel, BorderLayout.CENTER);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(gui, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Size");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final ChooseSolarPanelSizeForRackCommand c = new ChooseSolarPanelSizeForRackCommand(r);
								s.setPanelWidth(w);
								s.setPanelHeight(h);
								s.setNumberOfCellsInX(numberOfCellsInX);
								s.setNumberOfCellsInY(numberOfCellsInY);
								r.ensureFullSolarPanels(false);
								r.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final SetSolarPanelSizeForRacksOnFoundationCommand c = new SetSolarPanelSizeForRacksOnFoundationCommand(foundation);
								foundation.setSolarPanelSizeForRacks(w, h, numberOfCellsInX, numberOfCellsInY);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetSolarPanelSizeForAllRacksCommand c = new SetSolarPanelSizeForAllRacksCommand();
								Scene.getInstance().setSolarPanelSizeForAllRacks(w, h, numberOfCellsInX, numberOfCellsInY);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 2;
							}
							updateAfterEdit();
							if (choice == options[0]) {
								break;
							}
						}
					}
				}
			});

			final JMenuItem miSolarPanelOrientation = new JMenuItem("Orientation...");
			solarPanelMenu.add(miSolarPanelOrientation);
			miSolarPanelOrientation.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final SolarPanel s = r.getSolarPanel();
					final String partInfo = r.toString().substring(0, r.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					gui.setBorder(BorderFactory.createTitledBorder("Solar Panel Orientation for " + partInfo));
					final JComboBox<String> orientationComboBox = new JComboBox<String>(new String[] { "Portrait", "Landscape" });
					orientationComboBox.setSelectedIndex(s.isRotated() ? 1 : 0);
					gui.add(orientationComboBox, BorderLayout.NORTH);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					gui.add(scopePanel, BorderLayout.CENTER);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(gui, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Orientation");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final RotateSolarPanelsOnRackCommand c = new RotateSolarPanelsOnRackCommand(r);
								s.setRotated(orientationComboBox.getSelectedIndex() == 1);
								r.ensureFullSolarPanels(false);
								r.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final RotateSolarPanelsForRacksOnFoundationCommand c = new RotateSolarPanelsForRacksOnFoundationCommand(foundation);
								foundation.rotateSolarPanelsOnRacks(orientationComboBox.getSelectedIndex() == 1);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final RotateSolarPanelsOnAllRacksCommand c = new RotateSolarPanelsOnAllRacksCommand();
								Scene.getInstance().rotateSolarPanelsOnAllRacks(orientationComboBox.getSelectedIndex() == 1);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 2;
							}
							updateAfterEdit();
							if (choice == options[0]) {
								break;
							}
						}
					}
				}
			});

			// @deprecated: module structure is related to size and may not be set independently
			final JMenuItem miModuleStructure = new JMenuItem("Module Structure...");
			// solarPanelMenu.add(miModuleStructure);
			miModuleStructure.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final SolarPanel s = r.getSolarPanel();
					final Foundation foundation = r.getTopContainer();
					int nx = s.getNumberOfCellsInX();
					int ny = s.getNumberOfCellsInY();
					final String partInfo = r.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final JPanel inputFields = new JPanel();
					inputFields.setBorder(BorderFactory.createTitledBorder("Cell Numbers for " + partInfo));
					final JTextField nxField = new JTextField(nx + "", 10);
					inputFields.add(nxField);
					inputFields.add(new JLabel("  \u00D7  "));
					final JTextField nyField = new JTextField(ny + "", 10);
					inputFields.add(nyField);
					final JPanel scopeFields = new JPanel();
					scopeFields.setLayout(new BoxLayout(scopeFields, BoxLayout.Y_AXIS));
					scopeFields.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
					scopeFields.add(rb1);
					scopeFields.add(rb2);
					scopeFields.add(rb3);
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
					final JPanel panel = new JPanel(new BorderLayout(0, 8));
					panel.add(inputFields, BorderLayout.NORTH);
					panel.add(new JLabel(new ImageIcon(PopupMenuFactory.class.getResource("icons/solarcells.png"))));
					panel.add(scopeFields, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Cells of Panels on Rack");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							try {
								nx = Integer.parseInt(nxField.getText());
								ny = Integer.parseInt(nyField.getText());
							} catch (final NumberFormatException ex) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (nx <= 0 || nx > 20 || ny <= 0 || ny > 20) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Number of cells in X or Y direction must be between 1 and 20.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final ChangeCellNumbersCommand c = new ChangeCellNumbersCommand(s);
										s.setNumberOfCellsInX(nx);
										s.setNumberOfCellsInY(ny);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final ChangeFoundationSolarPanelCellNumbersCommand c = new ChangeFoundationSolarPanelCellNumbersCommand(foundation);
										foundation.setCellNumbersForSolarPanels(nx, ny);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final ChangeCellNumbersForAllSolarPanelsCommand c = new ChangeCellNumbersForAllSolarPanelsCommand();
										Scene.getInstance().setCellNumbersForAllSolarPanels(nx, ny);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 2;
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					}
				}
			});

			final JMenuItem miSolarPanelCellType = new JMenuItem("Cell Type...");
			solarPanelMenu.add(miSolarPanelCellType);
			miSolarPanelCellType.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final SolarPanel s = r.getSolarPanel();
					final String partInfo = r.toString().substring(0, r.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					gui.setBorder(BorderFactory.createTitledBorder("Choose Cell Type for " + partInfo));
					final JComboBox<String> cellTypeComboBox = new JComboBox<String>(new String[] { "Monocrystalline", "Polycrystalline" });
					cellTypeComboBox.setSelectedIndex(s.getCellType());
					gui.add(cellTypeComboBox, BorderLayout.NORTH);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					gui.add(scopePanel, BorderLayout.CENTER);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(gui, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Cell Type");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final SetCellTypeForSolarPanelsOnRackCommand c = new SetCellTypeForSolarPanelsOnRackCommand(r);
								s.setCellType(cellTypeComboBox.getSelectedIndex());
								r.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final SetSolarPanelCellTypeForRacksOnFoundationCommand c = new SetSolarPanelCellTypeForRacksOnFoundationCommand(foundation);
								foundation.setSolarPanelCellTypeForRacks(cellTypeComboBox.getSelectedIndex());
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetSolarPanelCellTypeForAllRacksCommand c = new SetSolarPanelCellTypeForAllRacksCommand();
								Scene.getInstance().setSolarPanelCellTypeForAllRacks(cellTypeComboBox.getSelectedIndex());
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 2;
							}
							updateAfterEdit();
							if (choice == options[0]) {
								break;
							}
						}
					}
				}
			});

			final JMenuItem miSolarPanelColor = new JMenuItem("Color...");
			solarPanelMenu.add(miSolarPanelColor);
			miSolarPanelColor.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final SolarPanel s = r.getSolarPanel();
					final String partInfo = r.toString().substring(0, r.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					gui.setBorder(BorderFactory.createTitledBorder("Choose Color for " + partInfo));
					final JComboBox<String> colorComboBox = new JComboBox<String>(new String[] { "Blue", "Black" });
					colorComboBox.setSelectedIndex(s.getColorOption());
					gui.add(colorComboBox, BorderLayout.NORTH);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					gui.add(scopePanel, BorderLayout.CENTER);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(gui, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Color");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final SetColorForSolarPanelsOnRackCommand c = new SetColorForSolarPanelsOnRackCommand(r);
								s.setColorOption(colorComboBox.getSelectedIndex());
								r.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final SetSolarPanelColorForRacksOnFoundationCommand c = new SetSolarPanelColorForRacksOnFoundationCommand(foundation);
								foundation.setSolarPanelColorForRacks(colorComboBox.getSelectedIndex());
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetSolarPanelColorForAllRacksCommand c = new SetSolarPanelColorForAllRacksCommand();
								Scene.getInstance().setSolarPanelColorForAllRacks(colorComboBox.getSelectedIndex());
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 2;
							}
							updateAfterEdit();
							if (choice == options[0]) {
								break;
							}
						}
					}
				}
			});

			final JMenuItem miSolarCellEfficiency = new JMenuItem("Solar Cell Efficiency...");
			solarPanelMenu.add(miSolarCellEfficiency);
			miSolarCellEfficiency.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final SolarPanel s = r.getSolarPanel();
					final String title = "Set Solar Cell Efficiency (%) for " + r.toString().substring(0, r.toString().indexOf(')') + 1);
					final String footnote = "<html><hr><font size=2>How efficient can a solar panel be for converting light into electricity?<br>The Shockley-Queisser limit is 34%.<br>The theoretical limit for multilayer cells is 86%.<br>As of 2017, the best solar panel in the market has an efficiency of 24%.<br>The highest efficiency you can choose is limited to " + SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "%.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					final JTextField inputField = new JTextField(threeDecimalsFormat.format(s.getCellEfficiency() * 100));
					gui.add(inputField, BorderLayout.NORTH);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					gui.add(scopePanel, BorderLayout.CENTER);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Cell Efficiency");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							try {
								solarCellEfficiencyPercentage = Double.parseDouble(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (solarCellEfficiencyPercentage < SolarPanel.MIN_SOLAR_CELL_EFFICIENCY_PERCENTAGE || solarCellEfficiencyPercentage > SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Solar cell efficiency must be between " + SolarPanel.MIN_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "% and " + SolarPanel.MAX_SOLAR_CELL_EFFICIENCY_PERCENTAGE + "%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final SetSolarCellEfficiencyForRackCommand c = new SetSolarCellEfficiencyForRackCommand(r);
										s.setCellEfficiency(solarCellEfficiencyPercentage * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final SetSolarCellEfficiencyForRacksOnFoundationCommand c = new SetSolarCellEfficiencyForRacksOnFoundationCommand(foundation);
										foundation.setSolarCellEfficiencyForRacks(solarCellEfficiencyPercentage * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final SetSolarCellEfficiencyForAllRacksCommand c = new SetSolarCellEfficiencyForAllRacksCommand();
										Scene.getInstance().setSolarCellEfficiencyForAllRacks(solarCellEfficiencyPercentage * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 2;
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					}
				}
			});

			final JMenuItem miNoct = new JMenuItem("Nominal Operating Cell Temperature...");
			solarPanelMenu.add(miNoct);
			miNoct.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final SolarPanel s = r.getSolarPanel();
					final String title = "<html>Nominal Operating Cell Temperature (&deg;C) for " + r.toString().substring(0, r.toString().indexOf(')') + 1);
					final String footnote = "<html><hr><font size=2>Increased temperature reduces solar cell efficiency.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					final JTextField inputField = new JTextField(threeDecimalsFormat.format(s.getNominalOperatingCellTemperature()));
					gui.add(inputField, BorderLayout.NORTH);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					gui.add(scopePanel, BorderLayout.CENTER);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Nominal Operating Cell Temperature");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							try {
								solarPanelNominalOperatingCellTemperature = Double.parseDouble(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (solarPanelNominalOperatingCellTemperature < 33 || solarPanelNominalOperatingCellTemperature > 58) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Nominal Operating Cell Temperature must be between 33 and 58 degrees.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final SetNoctForRackCommand c = new SetNoctForRackCommand(r);
										s.setNominalOperatingCellTemperature(solarPanelNominalOperatingCellTemperature);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final SetNoctForRacksOnFoundationCommand c = new SetNoctForRacksOnFoundationCommand(foundation);
										foundation.setNominalOperatingCellTemperatureForRacks(solarPanelNominalOperatingCellTemperature);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final SetNoctForAllRacksCommand c = new SetNoctForAllRacksCommand();
										Scene.getInstance().setNominalOperatingCellTemperatureForAllRacks(solarPanelNominalOperatingCellTemperature);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 2;
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					}
				}
			});

			final JMenuItem miPmax = new JMenuItem("Temperature Coefficient of Pmax...");
			solarPanelMenu.add(miPmax);
			miPmax.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final SolarPanel s = r.getSolarPanel();
					final String title = "<html>Temperature Coefficienct of Pmax (%/&deg;C) for " + r.toString().substring(0, r.toString().indexOf(')') + 1);
					final String footnote = "<html><hr><font size=2>Increased temperature reduces solar cell efficiency.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					final JTextField inputField = new JTextField(threeDecimalsFormat.format(s.getTemperatureCoefficientPmax() * 100));
					gui.add(inputField, BorderLayout.NORTH);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					gui.add(scopePanel, BorderLayout.CENTER);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Temperature Coefficient of Pmax");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							try {
								solarPanelTemperatureCoefficientPmaxPercentage = Double.parseDouble(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (solarPanelTemperatureCoefficientPmaxPercentage < -1 || solarPanelTemperatureCoefficientPmaxPercentage > 0) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Temperature coefficient of Pmax must be between -1 and 0", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final SetTemperatureCoefficientPmaxForRackCommand c = new SetTemperatureCoefficientPmaxForRackCommand(r);
										s.setTemperatureCoefficientPmax(solarPanelTemperatureCoefficientPmaxPercentage * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final SetTemperatureCoefficientPmaxForRacksOnFoundationCommand c = new SetTemperatureCoefficientPmaxForRacksOnFoundationCommand(foundation);
										foundation.setTemperatureCoefficientPmaxForRacks(solarPanelTemperatureCoefficientPmaxPercentage * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final SetTemperatureCoefficientPmaxForAllRacksCommand c = new SetTemperatureCoefficientPmaxForAllRacksCommand();
										Scene.getInstance().setTemperatureCoefficientPmaxForAllRacks(solarPanelTemperatureCoefficientPmaxPercentage * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 2;
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					}
				}
			});

			final JMenuItem miSolarPanelShadeTolerance = new JMenuItem("Shade Tolerance...");
			solarPanelMenu.add(miSolarPanelShadeTolerance);
			miSolarPanelShadeTolerance.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final SolarPanel s = r.getSolarPanel();
					final String title = "Set Solar Panel Shade Tolerance for " + r.toString().substring(0, r.toString().indexOf(')') + 1);
					final String footnote = "<html><hr><font size=2>Use bypass diodes to direct current under shading conditions.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					final JComboBox<String> toleranceComboBox = new JComboBox<String>(new String[] { "Partial", "High", "None" });
					toleranceComboBox.setSelectedIndex(s.getShadeTolerance());
					gui.add(toleranceComboBox, BorderLayout.NORTH);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					gui.add(scopePanel, BorderLayout.CENTER);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Solar Panel Shade Tolerance");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final SetShadeToleranceForSolarPanelsOnRackCommand c = new SetShadeToleranceForSolarPanelsOnRackCommand(r);
								s.setShadeTolerance(toleranceComboBox.getSelectedIndex());
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final SetSolarPanelShadeToleranceForRacksOnFoundationCommand c = new SetSolarPanelShadeToleranceForRacksOnFoundationCommand(foundation);
								foundation.setSolarPanelShadeToleranceForRacks(toleranceComboBox.getSelectedIndex());
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final SetSolarPanelShadeToleranceForAllRacksCommand c = new SetSolarPanelShadeToleranceForAllRacksCommand();
								Scene.getInstance().setSolarPanelShadeToleranceForAllRacks(toleranceComboBox.getSelectedIndex());
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 2;
							}
							updateAfterEdit();
							if (choice == options[0]) {
								break;
							}
						}
					}
				}
			});

			final JMenuItem miInverterEfficiency = new JMenuItem("Inverter Efficiency...");
			solarPanelMenu.add(miInverterEfficiency);
			miInverterEfficiency.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack r = (Rack) selectedPart;
					final Foundation foundation = r.getTopContainer();
					final SolarPanel s = r.getSolarPanel();
					final String title = "Set Inverter Efficiency (%) for " + r.toString().substring(0, r.toString().indexOf(')') + 1);
					final String footnote = "<html><hr><font size=2>The efficiency of a micro inverter for converting electricity<br>from DC to AC is typically 95%.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout(5, 5));
					final JTextField inputField = new JTextField(threeDecimalsFormat.format(s.getInverterEfficiency() * 100));
					gui.add(inputField, BorderLayout.NORTH);
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
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
					gui.add(scopePanel, BorderLayout.CENTER);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Inverter Efficiency");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							boolean ok = true;
							try {
								inverterEfficiencyPercentage = Double.parseDouble(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (inverterEfficiencyPercentage < SolarPanel.MIN_INVERTER_EFFICIENCY_PERCENTAGE || inverterEfficiencyPercentage > SolarPanel.MAX_INVERTER_EFFICIENCY_PERCENTAGE) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Inverter efficiency must be between " + SolarPanel.MIN_INVERTER_EFFICIENCY_PERCENTAGE + "% and " + SolarPanel.MAX_INVERTER_EFFICIENCY_PERCENTAGE + "%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final SetInverterEfficiencyForRackCommand c = new SetInverterEfficiencyForRackCommand(r);
										s.setInverterEfficiency(inverterEfficiencyPercentage * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final SetInverterEfficiencyForRacksOnFoundationCommand c = new SetInverterEfficiencyForRacksOnFoundationCommand(foundation);
										foundation.setInverterEfficiencyForRacks(inverterEfficiencyPercentage * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final SetInverterEfficiencyForAllRacksCommand c = new SetInverterEfficiencyForAllRacksCommand();
										Scene.getInstance().setInverterEfficiencyForAllRacks(inverterEfficiencyPercentage * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 2;
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					}
				}
			});

			final ButtonGroup trackerButtonGroup = new ButtonGroup();

			final JRadioButtonMenuItem miNoTracker = new JRadioButtonMenuItem("No Tracker...", true);
			trackerButtonGroup.add(miNoTracker);
			miNoTracker.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack rack = (Rack) selectedPart;
					final String partInfo = rack.toString().substring(0, rack.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					final String title = "<html>Disable tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>No tracker will be used.<hr></html>";
					final Object[] params = { title, footnote, panel };
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), params, "Disable solar tracker", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					if (rb1.isSelected()) {
						final SetSolarTrackerCommand c = new SetSolarTrackerCommand(rack);
						rack.setTracker(Trackable.NO_TRACKER);
						rack.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 0;
					} else if (rb2.isSelected()) {
						final Foundation foundation = rack.getTopContainer();
						final SetSolarTrackersOnFoundationCommand c = new SetSolarTrackersOnFoundationCommand(foundation, rack);
						foundation.setTrackerForRacks(Trackable.NO_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 1;
					} else if (rb3.isSelected()) {
						final SetSolarTrackersForAllCommand c = new SetSolarTrackersForAllCommand(rack);
						Scene.getInstance().setTrackerForAllRacks(Trackable.NO_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 2;
					}
					updateAfterEdit();
				}
			});

			final JRadioButtonMenuItem miHorizontalSingleAxisTracker = new JRadioButtonMenuItem("Horizontal Single-Axis Tracker...");
			trackerButtonGroup.add(miHorizontalSingleAxisTracker);
			miHorizontalSingleAxisTracker.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack rack = (Rack) selectedPart;
					final String partInfo = rack.toString().substring(0, rack.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					final String title = "<html>Enable horizontal single-axis tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2><hr></html>";
					final Object[] params = { title, footnote, panel };
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), params, "Enable horizontal single-axis solar tracker", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					if (rb1.isSelected()) {
						final SetSolarTrackerCommand c = new SetSolarTrackerCommand(rack);
						rack.setTracker(Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER);
						rack.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 0;
					} else if (rb2.isSelected()) {
						final Foundation foundation = rack.getTopContainer();
						final SetSolarTrackersOnFoundationCommand c = new SetSolarTrackersOnFoundationCommand(foundation, rack);
						foundation.setTrackerForRacks(Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 1;
					} else if (rb3.isSelected()) {
						final SetSolarTrackersForAllCommand c = new SetSolarTrackersForAllCommand(rack);
						Scene.getInstance().setTrackerForAllRacks(Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 2;
					}
					updateAfterEdit();
				}
			});

			final JRadioButtonMenuItem miVerticalSingleAxisTracker = new JRadioButtonMenuItem("Vertical Single-Axis Tracker...");
			trackerButtonGroup.add(miVerticalSingleAxisTracker);
			miVerticalSingleAxisTracker.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack rack = (Rack) selectedPart;
					final String partInfo = rack.toString().substring(0, rack.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					final String title = "<html>Enable vertical single-axis tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2><hr></html>";
					final Object[] params = { title, footnote, panel };
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), params, "Enable vertical single-axis solar tracker", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					if (rb1.isSelected()) {
						final SetSolarTrackerCommand c = new SetSolarTrackerCommand(rack);
						rack.setTracker(Trackable.VERTICAL_SINGLE_AXIS_TRACKER);
						rack.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 0;
					} else if (rb2.isSelected()) {
						final Foundation foundation = rack.getTopContainer();
						final SetSolarTrackersOnFoundationCommand c = new SetSolarTrackersOnFoundationCommand(foundation, rack);
						foundation.setTrackerForRacks(Trackable.VERTICAL_SINGLE_AXIS_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 1;
					} else if (rb3.isSelected()) {
						final SetSolarTrackersForAllCommand c = new SetSolarTrackersForAllCommand(rack);
						Scene.getInstance().setTrackerForAllRacks(Trackable.VERTICAL_SINGLE_AXIS_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 2;
					}
					updateAfterEdit();
				}
			});

			final JRadioButtonMenuItem miAltazimuthDualAxisTracker = new JRadioButtonMenuItem("Altazimuth Dual-Axis Tracker...");
			trackerButtonGroup.add(miAltazimuthDualAxisTracker);
			miAltazimuthDualAxisTracker.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack rack = (Rack) selectedPart;
					final String partInfo = rack.toString().substring(0, rack.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Rack", true);
					final JRadioButton rb2 = new JRadioButton("All Racks on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Racks");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					final String title = "<html>Enable altitude-azimuth dual-axis tracker for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The Alt/Az dual-axis solar tracker will rotate the solar panel to face the sun exactly.<hr></html>";
					final Object[] params = { title, footnote, panel };
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), params, "Enable altitude-azimuth dual-axis solar tracker", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
						return;
					}
					if (rb1.isSelected()) {
						final SetSolarTrackerCommand c = new SetSolarTrackerCommand(rack);
						rack.setTracker(Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER);
						rack.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 0;
					} else if (rb2.isSelected()) {
						final Foundation foundation = rack.getTopContainer();
						final SetSolarTrackersOnFoundationCommand c = new SetSolarTrackersOnFoundationCommand(foundation, rack);
						foundation.setTrackerForRacks(Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 1;
					} else if (rb3.isSelected()) {
						final SetSolarTrackersForAllCommand c = new SetSolarTrackersForAllCommand(rack);
						Scene.getInstance().setTrackerForAllRacks(Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER);
						SceneManager.getInstance().getUndoManager().addEdit(c);
						selectedScopeIndex = 2;
					}
					updateAfterEdit();
				}
			});

			trackerMenu.add(miNoTracker);
			trackerMenu.add(miHorizontalSingleAxisTracker);
			trackerMenu.add(miVerticalSingleAxisTracker);
			trackerMenu.add(miAltazimuthDualAxisTracker);

			final JCheckBoxMenuItem cbmiDrawSunBeam = new JCheckBoxMenuItem("Draw Sun Beam");
			cbmiDrawSunBeam.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final Rack rack = (Rack) selectedPart;
					rack.setSunBeamVisible(cbmiDrawSunBeam.isSelected());
					rack.drawSunBeam();
					rack.draw();
					Scene.getInstance().setEdited(true);
				}
			});

			final JMenu labelMenu = new JMenu("Label");

			final JCheckBoxMenuItem miLabelNone = new JCheckBoxMenuItem("None", true);
			miLabelNone.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (miLabelNone.isSelected()) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Rack) {
							final Rack r = (Rack) selectedPart;
							final SetRackLabelCommand c = new SetRackLabelCommand(r);
							r.clearLabels();
							r.draw();
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
					if (selectedPart instanceof Rack) {
						final Rack r = (Rack) selectedPart;
						final SetRackLabelCommand c = new SetRackLabelCommand(r);
						r.setLabelCustom(miLabelCustom.isSelected());
						if (r.getLabelCustom()) {
							r.setLabelCustomText(JOptionPane.showInputDialog(MainFrame.getInstance(), "Custom Text", r.getLabelCustomText()));
						}
						r.draw();
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
					if (selectedPart instanceof Rack) {
						final Rack r = (Rack) selectedPart;
						final SetRackLabelCommand c = new SetRackLabelCommand(r);
						r.setLabelId(miLabelId.isSelected());
						r.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelId);

			final JCheckBoxMenuItem miLabelCellEfficiency = new JCheckBoxMenuItem("Cell Efficiency");
			miLabelCellEfficiency.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Rack) {
						final Rack r = (Rack) selectedPart;
						final SetRackLabelCommand c = new SetRackLabelCommand(r);
						r.setLabelCellEfficiency(miLabelCellEfficiency.isSelected());
						r.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelCellEfficiency);

			final JCheckBoxMenuItem miLabelTiltAngle = new JCheckBoxMenuItem("Tilt Angle");
			miLabelTiltAngle.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Rack) {
						final Rack r = (Rack) selectedPart;
						final SetRackLabelCommand c = new SetRackLabelCommand(r);
						r.setLabelTiltAngle(miLabelTiltAngle.isSelected());
						r.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelTiltAngle);

			final JCheckBoxMenuItem miLabelTracker = new JCheckBoxMenuItem("Tracker");
			miLabelTracker.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Rack) {
						final Rack r = (Rack) selectedPart;
						final SetRackLabelCommand c = new SetRackLabelCommand(r);
						r.setLabelTracker(miLabelTracker.isSelected());
						r.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelTracker);

			final JCheckBoxMenuItem miLabelEnergyOutput = new JCheckBoxMenuItem("Energy Output");
			miLabelEnergyOutput.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Rack) {
						final Rack r = (Rack) selectedPart;
						final SetRackLabelCommand c = new SetRackLabelCommand(r);
						r.setLabelEnergyOutput(miLabelEnergyOutput.isSelected());
						r.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelEnergyOutput);

			popupMenuForRack = createPopupMenu(true, true, new Runnable() {

				@Override
				public void run() {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Rack)) {
						return;
					}
					final HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
					miPaste.setEnabled(copyBuffer instanceof SolarPanel);
					final Rack rack = (Rack) selectedPart;
					switch (rack.getTracker()) {
					case Trackable.ALTAZIMUTH_DUAL_AXIS_TRACKER:
						Util.selectSilently(miAltazimuthDualAxisTracker, true);
						break;
					case Trackable.HORIZONTAL_SINGLE_AXIS_TRACKER:
						Util.selectSilently(miHorizontalSingleAxisTracker, true);
						break;
					case Trackable.VERTICAL_SINGLE_AXIS_TRACKER:
						Util.selectSilently(miVerticalSingleAxisTracker, true);
						break;
					case Trackable.NO_TRACKER:
						Util.selectSilently(miNoTracker, true);
						break;
					}
					miAltazimuthDualAxisTracker.setEnabled(true);
					miHorizontalSingleAxisTracker.setEnabled(true);
					miVerticalSingleAxisTracker.setEnabled(true);
					if (rack.getContainer() instanceof Roof) {
						final Roof roof = (Roof) rack.getContainer();
						final boolean flat = Util.isZero(roof.getHeight());
						miAltazimuthDualAxisTracker.setEnabled(flat);
						miHorizontalSingleAxisTracker.setEnabled(flat);
						miVerticalSingleAxisTracker.setEnabled(flat);
					}
					if (rack.getTracker() != Trackable.NO_TRACKER) {
						miTiltAngle.setEnabled(rack.getTracker() == Trackable.VERTICAL_SINGLE_AXIS_TRACKER);
						miAzimuth.setEnabled(false);
					} else {
						miTiltAngle.setEnabled(true);
						miAzimuth.setEnabled(true);
						miBaseHeight.setEnabled(true);
						miPoleSpacing.setEnabled(true);
						if (rack.getContainer() instanceof Roof) {
							final Roof roof = (Roof) rack.getContainer();
							if (roof.getHeight() > 0) {
								miTiltAngle.setEnabled(false);
								miAzimuth.setEnabled(false);
								miBaseHeight.setEnabled(false);
								miPoleSpacing.setEnabled(false);
							}
						}
					}
					Util.selectSilently(cbmiDrawSunBeam, rack.isDrawSunBeamVisible());
					Util.selectSilently(miLabelNone, !rack.isLabelVisible());
					Util.selectSilently(miLabelCustom, rack.getLabelCustom());
					Util.selectSilently(miLabelId, rack.getLabelId());
					Util.selectSilently(miLabelCellEfficiency, rack.getLabelCellEfficiency());
					Util.selectSilently(miLabelTiltAngle, rack.getLabelTiltAngle());
					Util.selectSilently(miLabelTracker, rack.getLabelTracker());
					Util.selectSilently(miLabelEnergyOutput, rack.getLabelEnergyOutput());
				}

			});

			popupMenuForRack.add(miPaste);
			popupMenuForRack.add(miClear);
			popupMenuForRack.addSeparator();
			popupMenuForRack.add(miSolarPanelArray);
			popupMenuForRack.add(solarPanelMenu);
			popupMenuForRack.addSeparator();
			popupMenuForRack.add(miTiltAngle);
			popupMenuForRack.add(miAzimuth);
			popupMenuForRack.add(miRackSize);
			popupMenuForRack.add(miBaseHeight);
			popupMenuForRack.add(miPoleSpacing);
			popupMenuForRack.add(trackerMenu);
			popupMenuForRack.addSeparator();
			popupMenuForRack.add(cbmiDrawSunBeam);
			popupMenuForRack.add(labelMenu);
			popupMenuForRack.addSeparator();

			JMenuItem mi = new JMenuItem("Daily Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Rack) {
						new PvDailyAnalysis().show();
					}
				}
			});
			popupMenuForRack.add(mi);

			mi = new JMenuItem("Annual Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Rack) {
						new PvAnnualAnalysis().show();
					}
				}
			});
			popupMenuForRack.add(mi);

		}

		return popupMenuForRack;

	}

	private static JPopupMenu getPopupMenuForMirror() {

		if (popupMenuForMirror == null) {

			final JCheckBoxMenuItem cbmiDrawSunBeam = new JCheckBoxMenuItem("Draw Sun Beam");
			cbmiDrawSunBeam.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final Mirror m = (Mirror) selectedPart;
					m.setDrawSunBeam(cbmiDrawSunBeam.isSelected());
					m.draw();
					Scene.getInstance().setEdited(true);
				}
			});

			final JMenuItem miSetHeliostat = new JMenuItem("Set Heliostat...");
			miSetHeliostat.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final Mirror m = (Mirror) selectedPart;
					final String partInfo = m.toString().substring(0, m.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Mirror", true);
					final JRadioButton rb2 = new JRadioButton("All Mirrors on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Mirrors");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					final JTextField inputField = new JTextField(m.getHeliostatTarget().getId() + "");
					gui.add(inputField, BorderLayout.SOUTH);

					final String title = "<html>Set the ID of the foundation of the target tower for " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The sunlight reflected by this mirror will focus on the top of the target foundation.<hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Heliostat Target");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							int id = 0;
							boolean ok = true;
							try {
								id = Integer.parseInt(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								final HousePart p = Scene.getInstance().getPart(id);
								if (id < 0) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "ID cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (!(p instanceof Foundation)) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "ID must be that of a foundation.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (p instanceof Foundation) {
										final Foundation target = (Foundation) p;
										if (rb1.isSelected()) {
											final Foundation oldTarget = m.getHeliostatTarget();
											final ChangeMirrorTargetCommand c = new ChangeMirrorTargetCommand(m);
											m.setHeliostatTarget(target);
											m.draw();
											if (oldTarget != null) {
												oldTarget.drawSolarReceiver();
											}
											SceneManager.getInstance().getUndoManager().addEdit(c);
											selectedScopeIndex = 0;
										} else if (rb2.isSelected()) {
											final Foundation foundation = m.getTopContainer();
											final ChangeFoundationMirrorTargetCommand c = new ChangeFoundationMirrorTargetCommand(foundation);
											foundation.setTargetForMirrors(target);
											SceneManager.getInstance().getUndoManager().addEdit(c);
											selectedScopeIndex = 1;
										} else if (rb3.isSelected()) {
											final ChangeTargetForAllMirrorsCommand c = new ChangeTargetForAllMirrorsCommand();
											Scene.getInstance().setTargetForAllMirrors(target);
											SceneManager.getInstance().getUndoManager().addEdit(c);
											selectedScopeIndex = 2;
										}
										target.drawSolarReceiver();
										updateAfterEdit();
										if (choice == options[0]) {
											break;
										}
									}
								}
							}
						}
					}
				}
			});

			final JMenuItem miDisableHeliostat = new JMenuItem("Disable Heliostat...");
			miDisableHeliostat.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final Mirror m = (Mirror) selectedPart;
					final String partInfo = m.toString().substring(0, m.toString().indexOf(')') + 1);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Mirror", true);
					final JRadioButton rb2 = new JRadioButton("All Mirrors on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Mirrors");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					final String title = "<html>Disable heliostat for " + partInfo + "</html>";
					final String footnote = "<html><hr></html>";
					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, panel }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Disable Heliostat");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							if (rb1.isSelected()) {
								final ChangeMirrorTargetCommand c = new ChangeMirrorTargetCommand(m);
								if (m.getHeliostatTarget() != null) {
									m.getHeliostatTarget().drawSolarReceiver();
								}
								m.setHeliostatTarget(null);
								m.draw();
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 0;
							} else if (rb2.isSelected()) {
								final Foundation foundation = m.getTopContainer();
								final ChangeFoundationMirrorTargetCommand c = new ChangeFoundationMirrorTargetCommand(foundation);
								foundation.setTargetForMirrors(null);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 1;
							} else if (rb3.isSelected()) {
								final ChangeTargetForAllMirrorsCommand c = new ChangeTargetForAllMirrorsCommand();
								Scene.getInstance().setTargetForAllMirrors(null);
								SceneManager.getInstance().getUndoManager().addEdit(c);
								selectedScopeIndex = 2;
							}
							updateAfterEdit();
							if (choice == options[0]) {
								break;
							}
						}
					}
				}
			});

			final JMenuItem miZenith = new JMenuItem("Tilt Angle...");
			miZenith.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final Mirror m = (Mirror) selectedPart;
					final String partInfo = m.toString().substring(0, m.toString().indexOf(')') + 1);
					final String title = "<html>Tilt Angle of " + partInfo + " (&deg;)</html>";
					final String footnote = "<html><hr><font size=2>The tilt angle of a mirror is the angle between its surface and the base surface.<br>The tilt angle must be between -90&deg; and 90&deg;.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Mirror", true);
					final JRadioButton rb2 = new JRadioButton("All Mirrors on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Mirrors");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(m.getTiltAngle()));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Mirror Tilt Angle");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							double val = 0;
							boolean ok = true;
							try {
								val = Double.parseDouble(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (val < -90 || val > 90) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "The tilt angle must be between -90 and 90 degrees.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (Util.isZero(val - 90)) {
										val = 89.999;
									} else if (Util.isZero(val + 90)) {
										val = -89.999;
									}
									if (rb1.isSelected()) {
										final ChangeTiltAngleCommand c = new ChangeTiltAngleCommand(m);
										m.setTiltAngle(val);
										m.draw();
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = m.getTopContainer();
										final ChangeFoundationMirrorTiltAngleCommand c = new ChangeFoundationMirrorTiltAngleCommand(foundation);
										foundation.setZenithAngleForMirrors(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final ChangeTiltAngleForAllMirrorsCommand c = new ChangeTiltAngleForAllMirrorsCommand();
										Scene.getInstance().setTiltAngleForAllMirrors(val);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 2;
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					}
				}
			});

			final JMenuItem miAzimuth = new JMenuItem("Azimuth...");
			miAzimuth.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Mirror mirror = (Mirror) selectedPart;
					final Foundation foundation = mirror.getTopContainer();
					final String title = "<html>Azimuth Angle (&deg;) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>The azimuth angle is measured clockwise from the true north.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Mirror", true);
					final JRadioButton rb2 = new JRadioButton("All Mirrors on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Mirrors");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					double a = mirror.getRelativeAzimuth() + foundation.getAzimuth();
					if (a > 360) {
						a -= 360;
					}
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(a));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Mirror Azimuth");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							double val = 0;
							boolean ok = true;
							try {
								val = Double.parseDouble(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								a = val - foundation.getAzimuth();
								if (a < 0) {
									a += 360;
								}
								if (rb1.isSelected()) {
									final ChangeAzimuthCommand c = new ChangeAzimuthCommand(mirror);
									mirror.setRelativeAzimuth(a);
									mirror.draw();
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 0;
								} else if (rb2.isSelected()) {
									final ChangeFoundationMirrorAzimuthCommand c = new ChangeFoundationMirrorAzimuthCommand(foundation);
									foundation.setAzimuthForMirrors(a);
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 1;
								} else if (rb3.isSelected()) {
									final ChangeAzimuthForAllMirrorsCommand c = new ChangeAzimuthForAllMirrorsCommand();
									Scene.getInstance().setAzimuthForAllMirrors(a);
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 2;
								}
								updateAfterEdit();
								if (choice == options[0]) {
									break;
								}
							}
						}
					}
				}
			});

			final JMenuItem miSize = new JMenuItem("Size...");
			miSize.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final Mirror m = (Mirror) selectedPart;
					final Foundation foundation = m.getTopContainer();
					final String partInfo = m.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
					gui.add(inputPanel, BorderLayout.CENTER);
					inputPanel.add(new JLabel("Width: "));
					final JTextField widthField = new JTextField(threeDecimalsFormat.format(m.getMirrorWidth()));
					inputPanel.add(widthField);
					inputPanel.add(new JLabel("Length: "));
					final JTextField heightField = new JTextField(threeDecimalsFormat.format(m.getMirrorHeight()));
					inputPanel.add(heightField);
					inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
					final JPanel scopePanel = new JPanel();
					scopePanel.setLayout(new BoxLayout(scopePanel, BoxLayout.Y_AXIS));
					scopePanel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Mirror", true);
					final JRadioButton rb2 = new JRadioButton("All Mirrors on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Mirrors");
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
					final JOptionPane optionPane = new JOptionPane(new Object[] { "Set size for " + partInfo, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Mirror Size");

					while (true) {
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							double w = 0, h = 0;
							boolean ok = true;
							try {
								w = Double.parseDouble(widthField.getText());
								h = Double.parseDouble(heightField.getText());
							} catch (final NumberFormatException x) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid input!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (w < 1 || w > 50) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Width must be between 1 and 50 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else if (h < 1 || h > 50) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Height must be between 1 and 50 m.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final SetPartSizeCommand c = new SetPartSizeCommand(m);
										m.setMirrorWidth(w);
										m.setMirrorHeight(h);
										m.draw();
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final SetSizeForMirrorsOnFoundationCommand c = new SetSizeForMirrorsOnFoundationCommand(foundation);
										foundation.setSizeForMirrors(w, h);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final SetSizeForAllMirrorsCommand c = new SetSizeForAllMirrorsCommand();
										Scene.getInstance().setSizeForAllMirrors(w, h);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 2;
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					}
				}
			});

			final JMenuItem miBaseHeight = new JMenuItem("Base Height...");
			miBaseHeight.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Mirror m = (Mirror) selectedPart;
					final Foundation foundation = m.getTopContainer();
					final String title = "<html>Base Height (m) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Mirror", true);
					final JRadioButton rb2 = new JRadioButton("All Mirrors on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Mirrors");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(m.getBaseHeight() * Scene.getInstance().getAnnotationScale()));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Mirror Base Height");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							double val = 0;
							boolean ok = true;
							try {
								val = Double.parseDouble(inputField.getText()) / Scene.getInstance().getAnnotationScale();
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (rb1.isSelected()) {
									final ChangeBaseHeightCommand c = new ChangeBaseHeightCommand(m);
									m.setBaseHeight(val);
									m.draw();
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 0;
								} else if (rb2.isSelected()) {
									final ChangeFoundationMirrorBaseHeightCommand c = new ChangeFoundationMirrorBaseHeightCommand(foundation);
									foundation.setBaseHeightForMirrors(val);
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 1;
								} else if (rb3.isSelected()) {
									final ChangeBaseHeightForAllMirrorsCommand c = new ChangeBaseHeightForAllMirrorsCommand();
									Scene.getInstance().setBaseHeightForAllMirrors(val);
									SceneManager.getInstance().getUndoManager().addEdit(c);
									selectedScopeIndex = 2;
								}
								updateAfterEdit();
								if (choice == options[0]) {
									break;
								}
							}
						}
					}

				}
			});

			final JMenu labelMenu = new JMenu("Label");

			final JCheckBoxMenuItem miLabelNone = new JCheckBoxMenuItem("None", true);
			miLabelNone.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (miLabelNone.isSelected()) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Mirror) {
							final Mirror r = (Mirror) selectedPart;
							final SetMirrorLabelCommand c = new SetMirrorLabelCommand(r);
							r.clearLabels();
							r.draw();
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
					if (selectedPart instanceof Mirror) {
						final Mirror f = (Mirror) selectedPart;
						final SetMirrorLabelCommand c = new SetMirrorLabelCommand(f);
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
					if (selectedPart instanceof Mirror) {
						final Mirror r = (Mirror) selectedPart;
						final SetMirrorLabelCommand c = new SetMirrorLabelCommand(r);
						r.setLabelId(miLabelId.isSelected());
						r.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelId);

			final JCheckBoxMenuItem miLabelEnergyOutput = new JCheckBoxMenuItem("Energy Output");
			miLabelEnergyOutput.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Mirror) {
						final Mirror r = (Mirror) selectedPart;
						final SetMirrorLabelCommand c = new SetMirrorLabelCommand(r);
						r.setLabelEnergyOutput(miLabelEnergyOutput.isSelected());
						r.draw();
						SceneManager.getInstance().getUndoManager().addEdit(c);
						Scene.getInstance().setEdited(true);
						SceneManager.getInstance().refresh();
					}
				}
			});
			labelMenu.add(miLabelEnergyOutput);

			popupMenuForMirror = createPopupMenu(true, true, new Runnable() {
				@Override
				public void run() {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final Mirror m = (Mirror) selectedPart;
					if (m.getHeliostatTarget() == null) {
						miZenith.setEnabled(true);
						miAzimuth.setEnabled(true);
						miDisableHeliostat.setEnabled(false);
					} else {
						miZenith.setEnabled(false);
						miAzimuth.setEnabled(false);
						miDisableHeliostat.setEnabled(true);
					}
					Util.selectSilently(cbmiDrawSunBeam, m.getDrawSunBeam());
					Util.selectSilently(miLabelNone, !m.isLabelVisible());
					Util.selectSilently(miLabelCustom, m.getLabelCustom());
					Util.selectSilently(miLabelId, m.getLabelId());
					Util.selectSilently(miLabelEnergyOutput, m.getLabelEnergyOutput());
				}
			});

			final JMenuItem miReflectivity = new JMenuItem("Reflectivity...");
			miReflectivity.addActionListener(new ActionListener() {

				private int selectedScopeIndex = 0; // remember the scope selection as the next action will likely be applied to the same scope

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (!(selectedPart instanceof Mirror)) {
						return;
					}
					final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
					final Mirror m = (Mirror) selectedPart;
					final String title = "<html>Reflectivity (%) of " + partInfo + "</html>";
					final String footnote = "<html><hr><font size=2>Reflectivity can be affected by pollen and dust.<hr></html>";
					final JPanel gui = new JPanel(new BorderLayout());
					final JPanel panel = new JPanel();
					gui.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
					final JRadioButton rb1 = new JRadioButton("Only this Mirror", true);
					final JRadioButton rb2 = new JRadioButton("All Mirrors on this Foundation");
					final JRadioButton rb3 = new JRadioButton("All Mirrors");
					panel.add(rb1);
					panel.add(rb2);
					panel.add(rb3);
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
					final JTextField inputField = new JTextField(EnergyPanel.TWO_DECIMALS.format(m.getReflectivity() * 100));
					gui.add(inputField, BorderLayout.SOUTH);

					final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
					final JOptionPane optionPane = new JOptionPane(new Object[] { title, footnote, gui }, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
					final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Mirror Reflectivity");

					while (true) {
						inputField.selectAll();
						inputField.requestFocusInWindow();
						dialog.setVisible(true);
						final Object choice = optionPane.getValue();
						if (choice == options[1]) {
							break;
						} else {
							double val = 0;
							boolean ok = true;
							try {
								val = Double.parseDouble(inputField.getText());
							} catch (final NumberFormatException exception) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), inputField.getText() + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
								ok = false;
							}
							if (ok) {
								if (val < 50 || val > 99) {
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "Mirror reflectivity must be between 50% and 99%.", "Range Error", JOptionPane.ERROR_MESSAGE);
								} else {
									if (rb1.isSelected()) {
										final ChangeMirrorReflectivityCommand c = new ChangeMirrorReflectivityCommand(m);
										m.setReflectivity(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 0;
									} else if (rb2.isSelected()) {
										final Foundation foundation = m.getTopContainer();
										final ChangeFoundationMirrorReflectivityCommand c = new ChangeFoundationMirrorReflectivityCommand(foundation);
										foundation.setReflectivityForMirrors(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 1;
									} else if (rb3.isSelected()) {
										final ChangeReflectivityForAllMirrorsCommand c = new ChangeReflectivityForAllMirrorsCommand();
										Scene.getInstance().setReflectivityForAllMirrors(val * 0.01);
										SceneManager.getInstance().getUndoManager().addEdit(c);
										selectedScopeIndex = 2;
									}
									updateAfterEdit();
									if (choice == options[0]) {
										break;
									}
								}
							}
						}
					}
				}
			});

			popupMenuForMirror.addSeparator();
			popupMenuForMirror.add(miSetHeliostat);
			popupMenuForMirror.add(miDisableHeliostat);
			popupMenuForMirror.addSeparator();
			popupMenuForMirror.add(miZenith);
			popupMenuForMirror.add(miAzimuth);
			popupMenuForMirror.addSeparator();
			popupMenuForMirror.add(cbmiDrawSunBeam);
			popupMenuForMirror.add(labelMenu);
			popupMenuForMirror.addSeparator();
			popupMenuForMirror.add(miSize);
			popupMenuForMirror.add(miBaseHeight);
			popupMenuForMirror.add(miReflectivity);
			popupMenuForMirror.addSeparator();

			JMenuItem mi = new JMenuItem("Daily Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Mirror) {
						new MirrorDailyAnalysis().show();
					}
				}
			});
			popupMenuForMirror.add(mi);

			mi = new JMenuItem("Annual Yield Analysis...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().getSelectedPart() instanceof Mirror) {
						new MirrorAnnualAnalysis().show();
					}
				}
			});
			popupMenuForMirror.add(mi);

		}

		return popupMenuForMirror;

	}

	private static JPopupMenu getPopupMenuForTree() {

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
						SceneManager.getInstance().getUndoManager().addEdit(new LockPartCommand(tree));
						final boolean lock = miLock.isSelected();
						tree.setFreeze(lock);
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
						Util.selectSilently(miLock, p.isFrozen());
					}
				}
			});

			popupMenuForTree.addSeparator();
			popupMenuForTree.add(miLock);
			popupMenuForTree.add(miPolygon);

		}

		return popupMenuForTree;

	}

	private static JPopupMenu getPopupMenuForHuman() {

		if (popupMenuForHuman == null) {
			popupMenuForHuman = createPopupMenu(true, true, null);
		}
		return popupMenuForHuman;

	}

	private static JPopupMenu getPopupMenuForMesh() {

		if (popupMenuForMesh == null) {

			final JMenuItem miInfo = new JMenuItem("Mesh");
			miInfo.setEnabled(false);
			miInfo.setOpaque(true);
			miInfo.setBackground(Config.isMac() ? Color.BLACK : Color.GRAY);
			miInfo.setForeground(Color.WHITE);

			final JMenuItem miMessThickness = new JMenuItem("Thickness...");
			miMessThickness.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final Mesh m = f.getSelectedMesh();
						if (m != null) {
							final Node n = m.getParent();
							final String title = "<html>Adjust the distance between two mesh faces to create some thickness<br>A larger thickness also mitigates the z-fighting effect.</html>";
							while (true) {
								final String newValue = JOptionPane.showInputDialog(MainFrame.getInstance(), title, f.getMeshThickness(n) * Scene.getInstance().getAnnotationScale());
								if (newValue == null) {
									break;
								} else {
									try {
										final double val = Double.parseDouble(newValue);
										if (val < 0 || val > 1) {
											JOptionPane.showMessageDialog(MainFrame.getInstance(), "Thickness must be between 0 and 1 meter.", "Range Error", JOptionPane.ERROR_MESSAGE);
										} else {
											SceneManager.getTaskManager().update(new Callable<Object>() {
												@Override
												public Object call() throws Exception {
													f.setMeshThickness(n, val / Scene.getInstance().getAnnotationScale());
													f.draw();
													return null;
												}
											});
											break;
										}
									} catch (final NumberFormatException exception) {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
									}
								}
							}
						}
					}
				}
			});

			final JMenuItem miReverseNormalVector = new JMenuItem("Reverse Mesh Normal Vector");
			miReverseNormalVector.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						SceneManager.getTaskManager().update(new Callable<Object>() {
							@Override
							public Object call() throws Exception {
								final Mesh m = f.getSelectedMesh();
								if (m != null) {
									Util.reverseFace(m);
									f.getNodeState(m.getParent()).reverseNormalOfMesh(((UserData) m.getUserData()).getMeshIndex());
									f.draw();
									updateAfterEdit();
								}
								return null;
							}
						});
					}
				}
			});

			final JMenuItem miAlignBottom = new JMenuItem("Align Node Bottom with Ground Level");
			miAlignBottom.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final Mesh m = f.getSelectedMesh();
						if (m != null) {
							SceneManager.getTaskManager().update(new Callable<Object>() {
								@Override
								public Object call() throws Exception {
									final Node n = m.getParent();
									if (n != null) {
										final OrientedBoundingBox boundingBox = Util.getOrientedBoundingBox(n);
										final double zBottom = boundingBox.getCenter().getZ() - boundingBox.getZAxis().getZ() * boundingBox.getExtent().getZ() - f.getHeight();
										f.translateImportedNode(n, 0, 0, -zBottom);
										f.draw();
										updateAfterEdit();
									}
									return null;
								}
							});
						}
					}
				}
			});

			final JMenuItem miAlignCenter = new JMenuItem("Align Node Center with Foundation Center");
			miAlignCenter.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final Mesh m = f.getSelectedMesh();
						if (m != null) {
							SceneManager.getTaskManager().update(new Callable<Object>() {
								@Override
								public Object call() throws Exception {
									final Node n = m.getParent();
									if (n != null) {
										final OrientedBoundingBox boundingBox = Util.getOrientedBoundingBox(n);
										final ReadOnlyVector3 shift = boundingBox.getCenter().subtract(f.getAbsCenter(), null);
										f.translateImportedNode(n, shift.getX(), shift.getY(), 0);
										f.setMeshSelectionVisible(false);
										f.draw();
										updateAfterEdit();
									}
									return null;
								}
							});
						}
					}
				}
			});

			final JMenuItem miCopyNode = new JMenuItem("Copy Node");
			miCopyNode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			miCopyNode.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
							if (selectedPart instanceof Foundation) {
								final Foundation f = (Foundation) selectedPart;
								final Mesh m = f.getSelectedMesh();
								if (m != null) {
									final Node n = m.getParent();
									Scene.getInstance().setCopyNode(n, f.getNodeState(n));
								}

							}
							return null;
						}
					});
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
							final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
							if (selectedPart instanceof Foundation) {
								final Foundation f = (Foundation) selectedPart;
								final Mesh m = f.getSelectedMesh();
								if (m != null) {
									Scene.getInstance().pasteToPickedLocationOnMesh(m);
									Scene.getInstance().setEdited(true);
									updateAfterEdit();
								}
							}
							return null;
						}
					});
				}
			});

			popupMenuForMesh = new JPopupMenu();
			popupMenuForMesh.setInvoker(MainPanel.getInstance().getCanvasPanel());
			popupMenuForMesh.addPopupMenuListener(new PopupMenuListener() {

				@Override
				public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final Mesh m = f.getSelectedMesh();
						if (m != null) {
							String name = f.getNodeState(m.getParent()).getName();
							if (name == null) {
								name = "Undefined";
							}
							miInfo.setText(m.getName() + " (" + name + ")");
							final OrientedBoundingBox boundingBox = Util.getOrientedBoundingBox(m.getParent());
							final ReadOnlyVector3 center = boundingBox.getCenter();
							final double zBottom = center.getZ() - boundingBox.getZAxis().getZ() * boundingBox.getExtent().getZ();
							miAlignBottom.setEnabled(!Util.isZero(zBottom - f.getHeight()));
							final Vector3 foundationCenter = f.getAbsCenter();
							miAlignCenter.setEnabled(!Util.isEqual(new Vector2(foundationCenter.getX(), foundationCenter.getY()), new Vector2(center.getX(), center.getY())));
							final HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
							miPaste.setEnabled(copyBuffer instanceof SolarPanel || copyBuffer instanceof Rack);
						}
					}
				}

				@Override
				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
					miAlignBottom.setEnabled(true);
					miAlignCenter.setEnabled(true);
				}

				@Override
				public void popupMenuCanceled(final PopupMenuEvent e) {
					miAlignBottom.setEnabled(true);
					miAlignCenter.setEnabled(true);
				}

			});

			final JMenuItem miDeleteMesh = new JMenuItem("Delete Mesh");
			miDeleteMesh.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final Mesh m = f.getSelectedMesh();
						if (m != null) {
							SceneManager.getTaskManager().update(new Callable<Object>() {
								@Override
								public Object call() throws Exception {
									f.deleteMesh(m);
									updateAfterEdit();
									return null;
								}
							});
						}
					}
				}
			});

			final JMenuItem miRestoreDeletedMeshes = new JMenuItem("Restore Deleted Meshes (Reload Required)");
			miRestoreDeletedMeshes.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final Mesh m = f.getSelectedMesh();
						if (m != null) {
							SceneManager.getTaskManager().update(new Callable<Object>() {
								@Override
								public Object call() throws Exception {
									f.restoreDeletedMeshes(m.getParent());
									updateAfterEdit();
									return null;
								}
							});
						}
					}
				}
			});

			final JMenuItem miCutNode = new JMenuItem("Cut Node");
			miCutNode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			miCutNode.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final Mesh m = f.getSelectedMesh();
						if (m != null) {
							SceneManager.getTaskManager().update(new Callable<Object>() {
								@Override
								public Object call() throws Exception {
									final Node n = m.getParent();
									Scene.getInstance().setCopyNode(n, f.getNodeState(n));
									f.deleteNode(n);
									updateAfterEdit();
									return null;
								}
							});
						}
					}
				}
			});

			final JMenuItem miMeshProperties = new JMenuItem("Mesh Properties...");
			miMeshProperties.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final Mesh m = f.getSelectedMesh();
						if (m != null) {
							final UserData ud = (UserData) m.getUserData();

							final JPanel gui = new JPanel(new BorderLayout());
							final String title = "<html>A mesh is a basic unit (e.g., a triangle or a line) of geometry of a structure.</html>";
							gui.add(new JLabel(title), BorderLayout.NORTH);
							final JPanel propertiesPanel = new JPanel(new SpringLayout());
							propertiesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
							gui.add(propertiesPanel, BorderLayout.CENTER);

							// index mode
							JLabel label = new JLabel("Index Mode: ", JLabel.TRAILING);
							propertiesPanel.add(label);
							JTextField textField = new JTextField(m.getMeshData().getIndexMode(0) + "", 5);
							textField.setEditable(false);
							label.setLabelFor(textField);
							propertiesPanel.add(textField);

							// vertex count
							label = new JLabel("Vertex Count: ", JLabel.TRAILING);
							propertiesPanel.add(label);
							textField = new JTextField(m.getMeshData().getVertexCount() + "", 5);
							textField.setEditable(false);
							label.setLabelFor(textField);
							propertiesPanel.add(textField);

							// normal
							label = new JLabel("Normal Vector: ", JLabel.TRAILING);
							propertiesPanel.add(label);
							final ReadOnlyVector3 normal = ((UserData) m.getUserData()).getNormal();
							textField = new JTextField("(" + threeDecimalsFormat.format(normal.getX()) + ", " + threeDecimalsFormat.format(normal.getY()) + ", " + threeDecimalsFormat.format(normal.getZ()) + "), relative", 5);
							textField.setEditable(false);
							label.setLabelFor(textField);
							propertiesPanel.add(textField);

							// color
							label = new JLabel("Color: ", JLabel.TRAILING);
							propertiesPanel.add(label);
							final ReadOnlyColorRGBA rgb = m.getDefaultColor();
							colorChooser.setColor(new Color(Math.round(rgb.getRed() * 255), Math.round(rgb.getGreen() * 255), Math.round(rgb.getBlue() * 255)));
							label.setLabelFor(colorChooser);
							propertiesPanel.add(colorChooser);

							SpringUtilities.makeCompactGrid(propertiesPanel, 4, 2, 6, 6, 6, 6);
							if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), gui, "Mesh Properties: " + miInfo.getText(), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
								final Color color = colorChooser.getColor();
								m.clearRenderState(StateType.Texture);
								m.setDefaultColor(new ColorRGBA(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1));
								final NodeState ns = f.getNodeState(m.getParent());
								ns.setMeshColor(ud.getMeshIndex(), m.getDefaultColor());
								f.draw();
							}
						}
					}
				}
			});

			final JMenuItem miNodeProperties = new JMenuItem("Node Properties...");
			miNodeProperties.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						final Foundation f = (Foundation) selectedPart;
						final Mesh m = f.getSelectedMesh();
						if (m != null) {
							final Node n = m.getParent();
							if (n != null) {
								final NodeState ns = f.getNodeState(n);
								final JPanel gui = new JPanel(new BorderLayout());
								final String title = "<html>A node contains a set of meshes that represent<br>the geometry of the structure.</html>";
								gui.add(new JLabel(title), BorderLayout.NORTH);
								final JPanel propertiesPanel = new JPanel(new SpringLayout());
								propertiesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
								gui.add(propertiesPanel, BorderLayout.CENTER);

								// name
								JLabel label = new JLabel("Name: ", JLabel.TRAILING);
								propertiesPanel.add(label);
								final JTextField nameField = new JTextField(ns.getName(), 5);
								label.setLabelFor(nameField);
								propertiesPanel.add(nameField);

								// name
								label = new JLabel("File: ", JLabel.TRAILING);
								propertiesPanel.add(label);
								final JTextField fileField = new JTextField(Util.getFileName(ns.getSourceURL().getPath()), 5);
								label.setLabelFor(fileField);
								propertiesPanel.add(fileField);

								// children count
								label = new JLabel("Children: ", JLabel.TRAILING);
								propertiesPanel.add(label);
								final JTextField textField = new JTextField(n.getNumberOfChildren() + "", 5);
								textField.setEditable(false);
								label.setLabelFor(textField);
								propertiesPanel.add(textField);

								SpringUtilities.makeCompactGrid(propertiesPanel, 3, 2, 6, 6, 6, 6);

								if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), gui, "Node Properties", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
									final String nodeName = nameField.getText();
									if (nodeName != null && !nodeName.trim().equals("")) {
										n.setName(nodeName);
										f.getNodeState(n).setName(nodeName);
									} else {
										JOptionPane.showMessageDialog(MainFrame.getInstance(), "Node must have a name!", "Name Error", JOptionPane.ERROR_MESSAGE);
									}
								}
							}
						}
					}
				}
			});

			popupMenuForMesh.add(miInfo);
			popupMenuForMesh.add(miCutNode);
			popupMenuForMesh.add(miPaste);
			popupMenuForMesh.add(miCopyNode);
			popupMenuForMesh.addSeparator();
			popupMenuForMesh.add(miAlignBottom);
			popupMenuForMesh.add(miAlignCenter);
			popupMenuForMesh.add(miMessThickness);
			popupMenuForMesh.add(miNodeProperties);
			popupMenuForMesh.addSeparator();
			popupMenuForMesh.add(miDeleteMesh);
			popupMenuForMesh.add(miReverseNormalVector);
			popupMenuForMesh.add(miRestoreDeletedMeshes);
			popupMenuForMesh.add(miMeshProperties);

		}

		return popupMenuForMesh;

	}

	private static JMenuItem createInsulationMenuItem(final boolean useUValue) {
		final JMenuItem mi = new JMenuItem("Insulation...");
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (!(selectedPart instanceof Thermalizable)) {
					return;
				}
				final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
				final Thermalizable t = (Thermalizable) selectedPart;

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

				while (true)

				{
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), panel, "Input: " + partInfo, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)

					{

						final String newValue = siField.getText();
						try {
							final double val = Double.parseDouble(newValue);
							if (val <= 0) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "U-value must be positive.", "Range Error", JOptionPane.ERROR_MESSAGE);
							} else {
								if (rb1.isSelected()) {
									final ChangePartUValueCommand c = new ChangePartUValueCommand(selectedPart);
									t.setUValue(val);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								} else {
									final ChangeBuildingUValueCommand c = new ChangeBuildingUValueCommand(selectedPart);
									Scene.getInstance().setUValuesOfSameTypeInBuilding(selectedPart, val);
									SceneManager.getInstance().getUndoManager().addEdit(c);
								}
								updateAfterEdit();
								EnergyPanel.getInstance().getConstructionCostGraph().updateBudget();
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

	private static JMenuItem createVolumetricHeatCapacityMenuItem() {
		final JMenuItem mi = new JMenuItem("Volumeric Heat Capacity...");
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (!(selectedPart instanceof Thermalizable)) {
					return;
				}
				final String partInfo = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
				final Thermalizable t = (Thermalizable) selectedPart;
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
								final ChangeVolumetricHeatCapacityCommand c = new ChangeVolumetricHeatCapacityCommand(selectedPart);
								t.setVolumetricHeatCapacity(val);
								updateAfterEdit();
								SceneManager.getInstance().getUndoManager().addEdit(c);
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

	private static JPopupMenu createPopupMenu(final boolean hasCopyMenu, final boolean pastable, final Runnable runWhenBecomingVisible) {

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
				miInfo.setText(s.substring(0, s.indexOf(')') + 1) + " ($" + Cost.getInstance().getPartCost(selectedPart) + ")");
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

	private static void updateAfterEdit() {
		EnergyPanel.getInstance().updateProperties();
		EnergyPanel.getInstance().clearRadiationHeatMap();
		Scene.getInstance().setEdited(true);
	}

}
