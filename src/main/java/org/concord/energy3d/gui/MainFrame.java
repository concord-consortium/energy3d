package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.undo.UndoableEdit;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.agents.ActionHistogram;
import org.concord.energy3d.logger.DesignReplay;
import org.concord.energy3d.logger.PlayControl;
import org.concord.energy3d.logger.PostProcessor;
import org.concord.energy3d.logger.SnapshotLogger;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.model.PartGroup;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.PrintController;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.CameraMode;
import org.concord.energy3d.scene.SceneManager.ViewMode;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.AnnualEnvironmentalTemperature;
import org.concord.energy3d.simulation.AnnualSensorData;
import org.concord.energy3d.simulation.Cost;
import org.concord.energy3d.simulation.DailyEnvironmentalTemperature;
import org.concord.energy3d.simulation.DailySensorData;
import org.concord.energy3d.simulation.EnergyAnnualAnalysis;
import org.concord.energy3d.simulation.EnergyDailyAnalysis;
import org.concord.energy3d.simulation.FresnelReflectorAnnualAnalysis;
import org.concord.energy3d.simulation.FresnelReflectorDailyAnalysis;
import org.concord.energy3d.simulation.GroupAnnualAnalysis;
import org.concord.energy3d.simulation.GroupDailyAnalysis;
import org.concord.energy3d.simulation.MirrorAnnualAnalysis;
import org.concord.energy3d.simulation.MirrorDailyAnalysis;
import org.concord.energy3d.simulation.MonthlySunshineHours;
import org.concord.energy3d.simulation.ParabolicDishAnnualAnalysis;
import org.concord.energy3d.simulation.ParabolicDishDailyAnalysis;
import org.concord.energy3d.simulation.ParabolicTroughAnnualAnalysis;
import org.concord.energy3d.simulation.ParabolicTroughDailyAnalysis;
import org.concord.energy3d.simulation.PvAnnualAnalysis;
import org.concord.energy3d.simulation.PvDailyAnalysis;
import org.concord.energy3d.simulation.UtilityBill;
import org.concord.energy3d.undo.ChangeBuildingColorCommand;
import org.concord.energy3d.undo.ChangeColorOfAllPartsOfSameTypeCommand;
import org.concord.energy3d.undo.ChangeColorOfConnectedWallsCommand;
import org.concord.energy3d.undo.ChangeLandColorCommand;
import org.concord.energy3d.undo.ChangePartColorCommand;
import org.concord.energy3d.undo.ChangeTextureCommand;
import org.concord.energy3d.undo.ChangeThemeCommand;
import org.concord.energy3d.undo.MyAbstractUndoableEdit;
import org.concord.energy3d.undo.MyUndoManager;
import org.concord.energy3d.undo.ShowAxesCommand;
import org.concord.energy3d.undo.ShowHeatFluxCommand;
import org.concord.energy3d.undo.ShowReflectorLightBeamsCommand;
import org.concord.energy3d.undo.ShowShadowCommand;
import org.concord.energy3d.undo.ShowSunAnglesCommand;
import org.concord.energy3d.undo.TopViewCommand;
import org.concord.energy3d.undo.ZoomCommand;
import org.concord.energy3d.util.ClipImage;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.FileChooser;
import org.concord.energy3d.util.Printout;
import org.concord.energy3d.util.Updater;
import org.concord.energy3d.util.Util;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final MainFrame instance = new MainFrame();
	private final List<JComponent> recentFileMenuItems = new ArrayList<JComponent>();
	private static final ExtensionFileFilter ng3Filter = new ExtensionFileFilter("Energy3D (*.ng3)", "ng3");
	private static final ExtensionFileFilter zipFilter = new ExtensionFileFilter("Zip (*.zip)", "zip");
	static final ExtensionFileFilter daeFilter = new ExtensionFileFilter("Collada (*.dae)", "dae");
	static final ExtensionFileFilter pngFilter = new ExtensionFileFilter("Image (*.png)", "png");
	static final ExtensionFileFilter objFilter = new ExtensionFileFilter("Wavefront (*.obj)", "obj");
	private final JColorChooser colorChooser;
	private int fileMenuItemCount;

	private JMenuBar appMenuBar;
	private JMenu fileMenu;
	private JMenuItem newMenuItem;
	private JMenuItem openMenuItem;
	private JMenuItem recoveryMenuItem;
	private JMenuItem replayFolderMenuItem;
	private JMenuItem replayLastFolderMenuItem;
	private JMenu replayControlsMenu;
	private JMenuItem forwardReplayMenuItem;
	private JMenuItem backwardReplayMenuItem;
	private JMenuItem endReplayMenuItem;
	private JMenuItem pauseReplayMenuItem;
	private JMenuItem analyzeFolderMenuItem;
	private JMenuItem saveMenuItem;
	private JMenuItem printMenuItem;
	private JCheckBoxMenuItem previewMenuItem;
	private JRadioButtonMenuItem orbitMenuItem;
	private JRadioButtonMenuItem firstPersonMenuItem;
	private JMenuItem resetCameraMenuItem;
	private JMenuItem saveasMenuItem;
	private JMenu viewMenu;
	private JMenu analysisMenu;
	private JMenuItem rescaleMenuItem;
	private JMenuItem overallUtilityBillMenuItem;
	private JMenuItem simulationSettingsMenuItem;
	private JMenuItem visualizationSettingsMenuItem;
	private JMenuItem annualEnergyAnalysisMenuItem;
	private JMenuItem annualEnergyAnalysisForSelectionMenuItem;
	private JMenuItem dailyEnergyAnalysisMenuItem;
	private JMenuItem dailyEnergyAnalysisForSelectionMenuItem;
	private JMenuItem groupDailyAnalysisMenuItem;
	private JMenuItem groupAnnualAnalysisMenuItem;
	private JMenuItem annualPvAnalysisMenuItem;
	private JMenuItem dailyPvAnalysisMenuItem;
	private JMenuItem annualMirrorAnalysisMenuItem;
	private JMenuItem dailyMirrorAnalysisMenuItem;
	private JMenuItem annualParabolicTroughAnalysisMenuItem;
	private JMenuItem dailyParabolicTroughAnalysisMenuItem;
	private JMenuItem annualParabolicDishAnalysisMenuItem;
	private JMenuItem dailyParabolicDishAnalysisMenuItem;
	private JMenuItem annualFresnelReflectorAnalysisMenuItem;
	private JMenuItem dailyFresnelReflectorAnalysisMenuItem;
	private JMenuItem annualSensorMenuItem;
	private JMenuItem dailySensorMenuItem;
	private JMenuItem constructionCostAnalysisMenuItem;
	private JMenuItem monthlySunshineHoursMenuItem;
	private JMenuItem annualEnvironmentalTemperatureMenuItem;
	private JMenuItem dailyEnvironmentalTemperatureMenuItem;
	private JCheckBoxMenuItem solarRadiationHeatMapMenuItem;
	private JCheckBoxMenuItem solarAbsorptionHeatMapMenuItem;
	private JCheckBoxMenuItem onlyReflectionHeatMapMenuItem;
	private JCheckBoxMenuItem showSolarLandMenuItem;
	private JCheckBoxMenuItem onlySolarComponentsInSolarMapMenuItem;
	private JCheckBoxMenuItem showHeatFluxVectorsMenuItem;
	private JCheckBoxMenuItem axesMenuItem;
	private JCheckBoxMenuItem sunAnglesMenuItem;
	private JCheckBoxMenuItem lightBeamsMenuItem;
	private JCheckBoxMenuItem shadowMenuItem;
	private JCheckBoxMenuItem roofDashedLineMenuItem;
	private JCheckBoxMenuItem disableShadowInActionMenuItem;
	private JMenuItem exitMenuItem;
	private JMenu helpMenu;
	private JMenuItem aboutMenuItem;
	private JDialog aboutDialog;
	private MainPanel mainPanel;
	private JCheckBoxMenuItem annotationsInwardMenuItem;
	private JMenu editMenu;
	private JMenuItem undoMenuItem;
	private JMenuItem redoMenuItem;
	private JMenuItem cutMenuItem;
	private JMenuItem copyMenuItem;
	private JMenuItem pasteMenuItem;
	private JMenuItem pageSetupMenuItem;
	private JRadioButtonMenuItem scaleToFitRadioButtonMenuItem;
	private JRadioButtonMenuItem exactSizeRadioButtonMenuItem;
	private final ButtonGroup printSizeOptionBbuttonGroup = new ButtonGroup();
	private JMenuItem importMenuItem, importColladaMenuItem, exportModelMenuItem;
	private JCheckBoxMenuItem snapMenuItem;
	private JCheckBoxMenuItem snapToGridsMenuItem;
	private JCheckBoxMenuItem topViewCheckBoxMenuItem;
	private JMenuItem zoomInMenuItem;
	private JMenuItem zoomOutMenuItem;
	private JMenu textureMenu;
	private JRadioButtonMenuItem noTextureMenuItem;
	private JRadioButtonMenuItem simpleTextureMenuItem;
	private JRadioButtonMenuItem fullTextureMenuItem;
	private final ButtonGroup textureButtonGroup = new ButtonGroup();
	private JMenu themeMenu;
	private JRadioButtonMenuItem cloudySkyMenuItem;
	private JRadioButtonMenuItem desertMenuItem;
	private JRadioButtonMenuItem grasslandMenuItem;
	private JRadioButtonMenuItem forestMenuItem;
	private final ButtonGroup themeButtonGroup = new ButtonGroup();
	private JMenuItem exportImageMenuItem;
	private JMenuItem copyImageMenuItem;
	private JMenuItem exportLogMenuItem;
	private JMenuItem enableAllEditPointsMenuItem;
	private JMenuItem disableAllEditPointsMenuItem;
	private JMenuItem specificationsMenuItem;
	private JMenuItem propertiesMenuItem;
	private JMenuItem customPricesMenuItem;
	private JMenuItem setRegionMenuItem;
	private JCheckBoxMenuItem noteCheckBoxMenuItem;
	private JCheckBoxMenuItem infoPanelCheckBoxMenuItem;
	private JMenu examplesMenu;
	private JMenu tutorialsMenu;
	private JCheckBoxMenuItem autoRecomputeEnergyMenuItem;
	private JMenuItem removeAllFoundationsMenuItem;
	private JMenuItem removeAllRoofsMenuItem;
	private JMenuItem removeAllFloorsMenuItem;
	private JMenuItem removeAllSolarPanelsMenuItem;
	private JMenuItem removeAllRacksMenuItem;
	private JMenuItem removeAllMirrorsMenuItem;
	private JMenuItem removeAllParabolicTroughsMenuItem;
	private JMenuItem removeAllParabolicDishesMenuItem;
	private JMenuItem removeAllFresnelReflectorsMenuItem;
	private JMenuItem removeAllSensorsMenuItem;
	private JMenuItem removeAllWallsMenuItem;
	private JMenuItem removeAllWindowsMenuItem;
	private JMenuItem removeAllWindowShuttersMenuItem;
	private JMenuItem removeAllTreesMenuItem;
	private JMenuItem removeAllHumansMenuItem;
	private JMenuItem removeAllLocksMenuItem;
	private JMenuItem removeAllUtilityBillsMenuItem;
	private JMenuItem fixProblemsMenuItem;
	private JMenuItem moveEastMenuItem;
	private JMenuItem moveWestMenuItem;
	private JMenuItem moveNorthMenuItem;
	private JMenuItem moveSouthMenuItem;
	private JMenuItem rotate180MenuItem;
	private JMenuItem rotate90CwMenuItem;
	private JMenuItem rotate90CcwMenuItem;
	private JMenu groundImageMenu;
	private JMenuItem useImageFileMenuItem;
	private JMenuItem useEarthViewMenuItem;
	private JMenuItem rescaleGroundImageMenuItem;
	private JMenuItem clearGroundImageMenuItem;
	private JCheckBoxMenuItem showGroundImageMenuItem;
	private String currentGroupType = "Solar Panel";

	public final static FilenameFilter ng3NameFilter = new FilenameFilter() {
		@Override
		public boolean accept(final File dir, final String name) {
			return name.endsWith(".ng3");
		}
	};

	private static class ExtensionFileFilter extends FileFilter {
		String description;
		String extensions[];

		public ExtensionFileFilter(final String description, final String extension) {
			this(description, new String[] { extension });
		}

		public ExtensionFileFilter(final String description, final String extensions[]) {
			if (description == null) {
				this.description = extensions[0] + "{ " + extensions.length + "} ";
			} else {
				this.description = description;
			}
			this.extensions = extensions.clone();
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public boolean accept(final File file) {
			if (file.isDirectory()) {
				return true;
			} else {
				final String path = file.getAbsolutePath().toLowerCase();
				for (int i = 0, n = extensions.length; i < n; i++) {
					final String extension = extensions[i];
					if ((path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1)) == '.')) {
						return true;
					}
				}
			}
			return false;
		}
	}

	public static MainFrame getInstance() {
		return instance;
	}

	public MainPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = MainPanel.getInstance();
			mainPanel.setMainFrame(this);
		}
		return mainPanel;
	}

	private MainFrame() {
		super();
		System.out.print("Initiating GUI...");
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icons/icon.png")));
		colorChooser = new JColorChooser();
		initialize();
		setMinimumSize(new Dimension(800, 600));
		System.out.println("done");
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			final ImageIcon icon_cw = new ImageIcon(MainPanel.class.getResource("icons/rotate_cw.png"));
			final ImageIcon icon_ccw = new ImageIcon(MainPanel.class.getResource("icons/rotate_ccw.png"));

			@Override
			public boolean dispatchKeyEvent(final KeyEvent e) {
				double a = MainPanel.getInstance().getRotationAngleAbsolute();
				if (e.isShiftDown()) {
					a *= 0.2;
					SceneManager.getInstance().setFineGrid(true);
				} else {
					SceneManager.getInstance().setFineGrid(false);
				}
				switch (e.getID()) {
				case KeyEvent.KEY_PRESSED:
				case KeyEvent.KEY_RELEASED:
					MainPanel.getInstance().getRotateButton().setIcon(e.isControlDown() ? icon_ccw : icon_cw);
					MainPanel.getInstance().setRotationAngle(e.isControlDown() ? a : -a);
					break;
				}
				return false;
			}
		});
	}

	private void initialize() {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setTitle("Energy3D V" + MainApplication.VERSION);

		setJMenuBar(getAppMenuBar());
		setContentPane(getMainPanel());

		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final Preferences pref = Preferences.userNodeForPackage(MainApplication.class);
		setSize(Math.min(pref.getInt("window_size_width", Math.max(900, MainPanel.getInstance().getAppToolbar().getPreferredSize().width)), screenSize.width), Math.min(pref.getInt("window_size_height", 600), screenSize.height));
		setLocation(pref.getInt("window_location_x", (int) (screenSize.getWidth() - getSize().getWidth()) / 2), pref.getInt("window_location_y", (int) (screenSize.getHeight() - getSize().getHeight()) / 2));
		setLocation(MathUtils.clamp(getLocation().x, 0, screenSize.width - getSize().width), MathUtils.clamp(getLocation().y, 0, screenSize.height - getSize().height));
		final int windowState = pref.getInt("window_state", JFrame.NORMAL);
		if ((windowState & JFrame.ICONIFIED) == 0) {
			setExtendedState(windowState);
		}

		if (Config.isMac()) {
			Mac.init();
		}

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(final ComponentEvent e) {
				if (MainFrame.this.getExtendedState() == 0) {
					pref.putInt("window_location_x", e.getComponent().getLocation().x);
					pref.putInt("window_location_y", e.getComponent().getLocation().y);
				}
			}

			@Override
			public void componentResized(final ComponentEvent e) {
				if (MainFrame.this.getExtendedState() == 0) {
					pref.putInt("window_size_width", e.getComponent().getSize().width);
					pref.putInt("window_size_height", e.getComponent().getSize().height);
				}
			}
		});

		addWindowStateListener(new WindowStateListener() {
			@Override
			public void windowStateChanged(final WindowEvent e) {
				pref.putInt("window_state", e.getNewState());
				SceneManager.getInstance().refresh();
			}
		});

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				exit();
			}

			@Override
			public void windowDeiconified(final WindowEvent e) {
				SceneManager.getInstance().refresh();
			}

			@Override
			public void windowActivated(final WindowEvent e) {
				// EnergyPanel.getInstance().initJavaFXGUI();
				// SceneManager.getInstance().refresh();
			}
		});
	}

	private JMenuBar getAppMenuBar() {
		if (appMenuBar == null) {
			appMenuBar = new JMenuBar();
			appMenuBar.add(getFileMenu());
			appMenuBar.add(getEditMenu());
			appMenuBar.add(getViewMenu());
			appMenuBar.add(getAnalysisMenu());
			appMenuBar.add(getTemplatesMenu());
			appMenuBar.add(getTutorialsMenu());
			appMenuBar.add(getHelpMenu());

			addCommonActionListeners(appMenuBar);
		}
		return appMenuBar;
	}

	private void addCommonActionListeners(final JMenuBar menuBar) {
		for (final Component c : menuBar.getComponents()) {
			if (c instanceof JMenu) {
				addCommonActionListeners((JMenu) c);
			}
		}
	}

	private void addCommonActionListeners(final JMenu menu) {
		for (final Component c : menu.getMenuComponents()) {
			if (c instanceof JMenuItem) {
				final JMenuItem menuItem = (JMenuItem) c;
				menuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						MainPanel.getInstance().defaultTool();
					}
				});
			}
		}
	}

	private JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = new JMenu();
			fileMenu.addMenuListener(new MenuListener() {

				private void enableMenuItems(final boolean b) {
					replayFolderMenuItem.setEnabled(b);
					replayLastFolderMenuItem.setEnabled(b);
					replayControlsMenu.setEnabled(b);
					analyzeFolderMenuItem.setEnabled(b);
					saveMenuItem.setEnabled(b);
				}

				@Override
				public void menuCanceled(final MenuEvent e) {
					enableMenuItems(true); // if any of these actions is registered with a keystroke, we must re-enable it
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					SceneManager.getInstance().refresh();
					enableMenuItems(true);
				}

				@Override
				public void menuSelected(final MenuEvent e) {

					MainPanel.getInstance().defaultTool();

					enableMenuItems(true);
					saveMenuItem.setEnabled(!Scene.isInternalFile()); // cannot overwrite a template

					// prevent multiple replay or postprocessing commands
					final boolean inactive = !PlayControl.active;
					replayFolderMenuItem.setEnabled(inactive);
					final File lastFolder = DesignReplay.getInstance().getLastFolder();
					replayLastFolderMenuItem.setEnabled(lastFolder != null && inactive);
					replayLastFolderMenuItem.setText(lastFolder != null ? "Replay Last Folder: " + lastFolder : "Replay Last Folder");
					replayControlsMenu.setEnabled(!inactive);
					analyzeFolderMenuItem.setEnabled(inactive);

					// recent files
					if (!recentFileMenuItems.isEmpty()) {
						for (final JComponent x : recentFileMenuItems) {
							fileMenu.remove(x);
						}
					}
					final String[] recentFiles = FileChooser.getInstance().getRecentFiles();
					if (recentFiles != null) {
						final int n = recentFiles.length;
						if (n > 0) {
							for (int i = 0; i < n; i++) {
								final JMenuItem x = new JMenuItem((i + 1) + "  " + Util.getFileName(recentFiles[i]));
								x.setToolTipText(recentFiles[i]);
								final File rf = new File(recentFiles[i]);
								x.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(final ActionEvent e) {
										boolean ok = false;
										if (Scene.getInstance().isEdited()) {
											final int save = JOptionPane.showConfirmDialog(MainFrame.this, "Do you want to save changes?", "Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
											if (save == JOptionPane.YES_OPTION) {
												save();
												if (!Scene.getInstance().isEdited()) {
													ok = true;
												}
											} else if (save != JOptionPane.CANCEL_OPTION) {
												ok = true;
											}
										} else {
											ok = true;
										}
										if (ok) {
											SceneManager.getTaskManager().update(new Callable<Object>() {
												@Override
												public Object call() {
													try {
														Scene.open(rf.toURI().toURL());
														updateTitleBar();
														FileChooser.getInstance().rememberFile(rf.getPath());
													} catch (final Throwable err) {
														Util.reportError(err, rf.getAbsolutePath());
													}
													return null;
												}
											});
										}
									}
								});
								fileMenu.insert(x, fileMenuItemCount + i);
								recentFileMenuItems.add(x);
							}
							final JSeparator s = new JSeparator();
							fileMenu.add(s, fileMenuItemCount + n);
							recentFileMenuItems.add(s);
						}
					}

				}
			});
			fileMenu.setText("File");
			addItemToFileMenu(getNewMenuItem());
			addItemToFileMenu(getOpenMenuItem());
			addItemToFileMenu(getSaveMenuItem());
			addItemToFileMenu(getSaveasMenuItem());
			addItemToFileMenu(getRecoveryMenuItem());
			addItemToFileMenu(new JSeparator());
			addItemToFileMenu(getImportMenuItem());
			addItemToFileMenu(getImportColladaMenuItem());
			addItemToFileMenu(getCopyImageMenuItem());
			addItemToFileMenu(getExportModelMenuItem());
			addItemToFileMenu(getExportImageMenuItem());
			addItemToFileMenu(getExportLogMenuItem());
			addItemToFileMenu(new JSeparator());

			addItemToFileMenu(getReplayFolderMenuItem());
			addItemToFileMenu(getReplayLastFolderMenuItem());

			replayControlsMenu = new JMenu("Replay Controls");
			replayControlsMenu.addMenuListener(new MenuListener() {
				@Override
				public void menuCanceled(final MenuEvent e) {
					// if any of these actions is registered with a keystroke, we must re-enable it
					endReplayMenuItem.setEnabled(true);
					pauseReplayMenuItem.setEnabled(true);
					forwardReplayMenuItem.setEnabled(true);
					backwardReplayMenuItem.setEnabled(true);
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					endReplayMenuItem.setEnabled(true);
					pauseReplayMenuItem.setEnabled(true);
					forwardReplayMenuItem.setEnabled(true);
					backwardReplayMenuItem.setEnabled(true);
				}

				@Override
				public void menuSelected(final MenuEvent e) {
					endReplayMenuItem.setEnabled(PlayControl.active);
					pauseReplayMenuItem.setEnabled(PlayControl.active);
					pauseReplayMenuItem.setText((PlayControl.replaying ? "Pause Replay" : "Resume Replay") + " (Space Bar)");
					forwardReplayMenuItem.setEnabled(!PlayControl.replaying);
					backwardReplayMenuItem.setEnabled(!PlayControl.replaying);
				}
			});
			addItemToFileMenu(replayControlsMenu);
			replayControlsMenu.add(getPauseReplayMenuItem());
			replayControlsMenu.add(getBackwardReplayMenuItem());
			replayControlsMenu.add(getForwardReplayMenuItem());
			replayControlsMenu.add(getEndReplayMenuItem());

			addItemToFileMenu(getAnalyzeFolderMenuItem());
			addItemToFileMenu(new JSeparator());

			addItemToFileMenu(getScaleToFitRadioButtonMenuItem());
			addItemToFileMenu(getExactSizeRadioButtonMenuItem());
			addItemToFileMenu(getPageSetupMenuItem());
			addItemToFileMenu(getPreviewMenuItem());
			addItemToFileMenu(getPrintMenuItem());
			addItemToFileMenu(new JSeparator());
			fileMenu.add(getExitMenuItem());
		}
		return fileMenu;
	}

	private void addItemToFileMenu(final JComponent c) {
		fileMenu.add(c);
		fileMenuItemCount++;
	}

	private JMenuItem getNewMenuItem() {
		if (newMenuItem == null) {
			newMenuItem = new JMenuItem("New");
			newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Config.isMac() ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK));
			newMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					boolean ok = false;
					if (Scene.getInstance().isEdited()) {
						final int save = JOptionPane.showConfirmDialog(MainFrame.this, "Do you want to save changes?", "Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
						if (save == JOptionPane.YES_OPTION) {
							save();
							if (!Scene.getInstance().isEdited()) {
								ok = true;
							}
						} else if (save != JOptionPane.CANCEL_OPTION) {
							ok = true;
						}
					} else {
						ok = true;
					}
					if (ok) {
						SceneManager.getTaskManager().update(new Callable<Object>() {
							@Override
							public Object call() throws Exception {
								try {
									Scene.newFile();
									SceneManager.getInstance().resetCamera(ViewMode.NORMAL);
									SceneManager.getInstance().getCameraControl().reset();
									EventQueue.invokeLater(new Runnable() {
										@Override
										public void run() {
											updateTitleBar();
											EnergyPanel.getInstance().update();
											EnergyPanel.getInstance().clearAllGraphs();
											EnergyPanel.getInstance().selectInstructionSheet(0);
										}
									});
								} catch (final Throwable err) {
									Util.reportError(err);
								}
								return null;
							}
						});
					}
				}
			});
		}
		return newMenuItem;
	}

	private JMenuItem getOpenMenuItem() {
		if (openMenuItem == null) {
			openMenuItem = new JMenuItem("Open...");
			openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Config.isMac() ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK));
			openMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (Scene.getInstance().isEdited()) {
						final int save = JOptionPane.showConfirmDialog(MainFrame.this, "Do you want to save changes?", "Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
						if (save == JOptionPane.YES_OPTION) {
							save();
							if (!Scene.getInstance().isEdited()) {
								open();
							}
						} else if (save != JOptionPane.CANCEL_OPTION) {
							open();
						}
					} else {
						open();
					}
				}
			});
		}
		return openMenuItem;
	}

	public void open() {
		SceneManager.getInstance().refresh(1);
		final File file = FileChooser.getInstance().showDialog(".ng3", ng3Filter, false);
		if (file == null) {
			return;
		}
		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() {
				try {
					Scene.open(file.toURI().toURL());
					FileChooser.getInstance().rememberFile(file.getPath());
				} catch (final Throwable err) {
					Util.reportError(err, file.getAbsolutePath());
				}
				return null;
			}
		});
		topViewCheckBoxMenuItem.setSelected(false);
	}

	private JMenuItem getRecoveryMenuItem() {
		if (recoveryMenuItem == null) {
			recoveryMenuItem = new JMenuItem("Recover from Log...");
			recoveryMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final File f = SnapshotLogger.getInstance().getLatestSnapshot();
					if (f != null) {
						SceneManager.getTaskManager().update(new Callable<Object>() {
							@Override
							public Object call() {
								try {
									Scene.open(f.toURI().toURL());
									EventQueue.invokeLater(new Runnable() {
										@Override
										public void run() {
											updateTitleBar();
											JOptionPane.showMessageDialog(MainFrame.instance, "<html>Please overwrite the file you wish to restore with the recovered file.</html>", "File Recovery", JOptionPane.INFORMATION_MESSAGE);
											saveasMenuItem.doClick();
										}
									});
								} catch (final Throwable err) {
									Util.reportError(err, "Recovery error");
								}
								return null;
							}
						});
					}
				}
			});
		}
		return recoveryMenuItem;
	}

	private JMenuItem getAnalyzeFolderMenuItem() {
		if (analyzeFolderMenuItem == null) {
			analyzeFolderMenuItem = new JMenuItem("Analyze Folder...");
			analyzeFolderMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(MainFrame.this, "This feature is for researchers only. Are you sure you want to continue?", "Research Mode", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE)) {
						return;
					}
					SceneManager.getInstance().refresh(1);
					final File dir = FileChooser.getInstance().showDialog(".", null, false);
					if (dir == null) {
						return;
					}

					if (dir.isDirectory()) {
						PostProcessor.getInstance().analyze(dir.listFiles(ng3NameFilter), new File(dir + System.getProperty("file.separator") + "prop.txt"), new Runnable() {
							@Override
							public void run() {
								updateTitleBar();
							}
						});
					}
				}
			});
		}
		return analyzeFolderMenuItem;
	}

	private JMenuItem getReplayFolderMenuItem() {
		if (replayFolderMenuItem == null) {
			replayFolderMenuItem = new JMenuItem("Replay Folder...");
			replayFolderMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(MainFrame.this, "This feature is for researchers only. Are you sure you want to continue?", "Research Mode", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE)) {
						return;
					}
					SceneManager.getInstance().refresh(1);
					final File dir = FileChooser.getInstance().showDialog(".", null, false);
					if (dir == null) {
						return;
					}
					if (dir.isDirectory()) {
						DesignReplay.getInstance().play(dir.listFiles(ng3NameFilter));
					}
				}
			});
		}
		return replayFolderMenuItem;

	}

	private JMenuItem getReplayLastFolderMenuItem() {
		if (replayLastFolderMenuItem == null) {
			replayLastFolderMenuItem = new JMenuItem("Replay Last Folder");
			replayLastFolderMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (DesignReplay.getInstance().getLastFolder() != null) {
						DesignReplay.getInstance().play(DesignReplay.getInstance().getLastFolder().listFiles(ng3NameFilter));
					}
				}
			});
		}
		return replayLastFolderMenuItem;
	}

	private JMenuItem getEndReplayMenuItem() {
		if (endReplayMenuItem == null) {
			endReplayMenuItem = new JMenuItem("End Replay (Escape Key)");
			endReplayMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					DesignReplay.active = false;
				}
			});
		}
		return endReplayMenuItem;
	}

	private JMenuItem getPauseReplayMenuItem() {
		if (pauseReplayMenuItem == null) {
			pauseReplayMenuItem = new JMenuItem("Pause Replay");
			pauseReplayMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (PlayControl.active) {
						PlayControl.replaying = !PlayControl.replaying;
					}
				}
			});
		}
		return pauseReplayMenuItem;
	}

	private JMenuItem getForwardReplayMenuItem() {
		if (forwardReplayMenuItem == null) {
			forwardReplayMenuItem = new JMenuItem("Replay Forward (Right Arrow Key)");
			forwardReplayMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (PlayControl.active) {
						PlayControl.replaying = false;
						PlayControl.forward = true;
					}
				}
			});
		}
		return forwardReplayMenuItem;
	}

	private JMenuItem getBackwardReplayMenuItem() {
		if (backwardReplayMenuItem == null) {
			backwardReplayMenuItem = new JMenuItem("Replay Backward (Left Arrow Key)");
			backwardReplayMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (PlayControl.active) {
						PlayControl.replaying = false;
						PlayControl.backward = true;
					}
				}
			});
		}
		return backwardReplayMenuItem;
	}

	public void updateTitleBar() {
		final String star = Scene.getInstance().isEdited() ? "*" : "";
		if (Scene.getURL() == null) {
			setTitle("Energy3D V" + MainApplication.VERSION + star);
		} else {
			if (Scene.isInternalFile()) {
				final String s = Scene.getURL().toString();
				setTitle("Energy3D V" + MainApplication.VERSION + " - @" + s.substring(s.lastIndexOf("/") + 1).replaceAll("%20", " ") + star);
			} else {
				setTitle("Energy3D V" + MainApplication.VERSION + " - " + new File(Scene.getURL().getFile()).toString().replaceAll("%20", " ") + star);
			}
		}
	}

	private JMenuItem getSaveMenuItem() {
		if (saveMenuItem == null) {
			saveMenuItem = new JMenuItem("Save");
			saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Config.isMac() ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK));
			saveMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					save();
				}
			});
		}
		return saveMenuItem;
	}

	private JMenuItem getPrintMenuItem() {
		if (printMenuItem == null) {
			printMenuItem = new JMenuItem("Print...");
			printMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Config.isMac() ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK));
			printMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final PrintController printController = PrintController.getInstance();
					if (!printController.isPrintPreview()) {
						getPreviewMenuItem().setSelected(true);
						new Thread("Energy3D Print") {
							@Override
							public void run() {
								while (!printController.isFinished()) {
									try {
										Thread.sleep(500);
									} catch (final InterruptedException e) {
										e.printStackTrace();
									}
								}
								PrintController.getInstance().print();
							}
						}.start();
					} else {
						PrintController.getInstance().print();
					}
				}
			});
		}
		return printMenuItem;
	}

	public JCheckBoxMenuItem getPreviewMenuItem() {
		if (previewMenuItem == null) {
			previewMenuItem = new JCheckBoxMenuItem("Print Preview");
			previewMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, Config.isMac() ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK));
			previewMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					mainPanel.getPreviewButton().setSelected(previewMenuItem.isSelected());
				}
			});
		}
		return previewMenuItem;
	}

	private JRadioButtonMenuItem getOrbitMenuItem() {
		if (orbitMenuItem == null) {
			orbitMenuItem = new JRadioButtonMenuItem();
			orbitMenuItem.setText("Orbit");
			orbitMenuItem.setSelected(true);
			orbitMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().setCameraControl(CameraMode.ORBIT);
				}
			});
		}
		return orbitMenuItem;
	}

	private JRadioButtonMenuItem getFirstPersonMenuItem() {
		if (firstPersonMenuItem == null) {
			firstPersonMenuItem = new JRadioButtonMenuItem();
			firstPersonMenuItem.setText("First Person");
			firstPersonMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().setCameraControl(CameraMode.FIRST_PERSON);
				}
			});
		}
		return firstPersonMenuItem;
	}

	private JMenuItem getResetCameraMenuItem() {
		if (resetCameraMenuItem == null) {
			resetCameraMenuItem = new JMenuItem();
			resetCameraMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, Config.isMac() ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK));
			resetCameraMenuItem.setText("Reset View");
			resetCameraMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().resetCamera();
				}
			});
		}
		return resetCameraMenuItem;
	}

	private JMenuItem getExitMenuItem() {
		if (exitMenuItem == null) {
			exitMenuItem = new JMenuItem();
			exitMenuItem.setText("Exit");
			exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Config.isMac() ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK));
			exitMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					exit();
				}
			});
		}
		return exitMenuItem;
	}

	private JMenu getHelpMenu() {
		if (helpMenu == null) {
			helpMenu = new JMenu("Help");
			final JMenuItem miUpdate = new JMenuItem("Check Update...");
			helpMenu.addMenuListener(new MenuListener() {

				@Override
				public void menuSelected(final MenuEvent e) {
					miUpdate.setEnabled(!Updater.isDownloadInProgress());
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					miUpdate.setEnabled(true);
				}

				@Override
				public void menuCanceled(final MenuEvent e) {
					miUpdate.setEnabled(true);
				}
			});
			// helpMenu.add(miUpdate); // TODO
			miUpdate.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Updater.download();
					if (Updater.isUpdateAvailable()) {
						Updater.install();
						if (Updater.isRestartRequested()) {
							MainApplication.restartApplication();
						}
					} else {
						JOptionPane.showMessageDialog(instance, "Your software is up to date.", "Update Status", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});

			// User data and models

			final JMenu userMenu = new JMenu("User Modeling");
			helpMenu.add(userMenu);
			helpMenu.addSeparator();

			final JMenu userDataMenu = new JMenu("Data");
			userMenu.add(userDataMenu);

			JMenuItem mi = new JMenuItem("Histogram");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					new ActionHistogram().showGui();
				}
			});
			userDataMenu.add(mi);

			final JMenu userModelMenu = new JMenu("Model");
			userMenu.add(userModelMenu);

			mi = new JMenuItem("Multinormial");
			userModelMenu.add(mi);

			mi = new JMenuItem("Markov");
			userModelMenu.add(mi);

			// Energy3D web pages

			mi = new JMenuItem("View Sites...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Util.openBrowser("http://energy.concord.org/energy3d/sites.html");
				}
			});
			helpMenu.add(mi);
			mi = new JMenuItem("View House Examples...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Util.openBrowser("http://energy.concord.org/energy3d/styles.html");
				}
			});
			helpMenu.add(mi);
			mi = new JMenuItem("View User Work...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Util.openBrowser("http://energy.concord.org/energy3d/models.html");
				}
			});
			helpMenu.add(mi);
			helpMenu.addSeparator();

			mi = new JMenuItem("Visit Home Page...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Util.openBrowser("http://energy3d.concord.org");
				}
			});
			helpMenu.add(mi);
			mi = new JMenuItem("Contact Us...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Util.openBrowser("http://energy.concord.org/energy3d/contact.html");
				}
			});
			helpMenu.add(mi);
			if (!Config.isMac()) {
				helpMenu.add(getAboutMenuItem());
			}
		}
		return helpMenu;
	}

	void showAbout() {
		final JDialog aboutDialog = getAboutDialog();
		final Dimension frameSize = getSize();
		final Dimension dialogSize = aboutDialog.getSize();
		final Point location = getLocation();
		aboutDialog.setLocation((int) (location.getX() + frameSize.getWidth() / 2 - dialogSize.getWidth() / 2), (int) (location.getY() + frameSize.getHeight() / 2 - dialogSize.getHeight() / 2));
		aboutDialog.setVisible(true);
	}

	private JMenuItem getAboutMenuItem() {
		if (aboutMenuItem == null) {
			aboutMenuItem = new JMenuItem();
			aboutMenuItem.setText("About...");
			aboutMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					showAbout();
				}
			});
		}
		return aboutMenuItem;
	}

	private JDialog getAboutDialog() {
		if (aboutDialog == null) {
			aboutDialog = new JDialog(this);
			aboutDialog.setTitle("About");
			final JPanel p = new JPanel(new BorderLayout(10, 10));
			p.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
			p.add(new JLabel("<html><h1>Energy3D</h1><h3><i>Learning to build a sustainable world</i></h3><br>Version: " + MainApplication.VERSION + ", Copyright 2011-" + Calendar.getInstance().get(Calendar.YEAR) + "<br>The Laboratory of Engineering Computation, Concord Consortium<hr><h3>Credit:</h3>This program is brought to you by:<ul><li>Dr. Saeid Nourian, developer of 3D user interface and graphics<li>Dr. Charles Xie, developer of simulation and analysis engines</ul><p>This program is based on Ardor3D and JOGL and provided to you under the MIT License.<br>Funding of this program is provided by the National Science Foundation through grants<br>0918449, 1304485, 1348530, 1503196, 1512868, and 1721054.</html>"), BorderLayout.CENTER);
			final JButton button = new JButton("Close");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent arg0) {
					aboutDialog.dispose();
				}
			});
			final JPanel p2 = new JPanel();
			p2.add(button);
			p.add(p2, BorderLayout.SOUTH);
			aboutDialog.setContentPane(p);
			aboutDialog.pack();
		}
		return aboutDialog;
	}

	private JMenuItem getSaveasMenuItem() {
		if (saveasMenuItem == null) {
			saveasMenuItem = new JMenuItem("Save As...");
			saveasMenuItem.setAccelerator(KeyStroke.getKeyStroke("F12"));
			saveasMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					saveFile(false);
				}
			});
		}
		return saveasMenuItem;
	}

	private JMenu getAnalysisMenu() {
		if (analysisMenu == null) {
			analysisMenu = new JMenu("Analysis");
			analysisMenu.addMenuListener(new MenuListener() {

				private void enableEnergyAnalysis(final boolean b) {
					annualEnergyAnalysisMenuItem.setEnabled(b);
					annualEnergyAnalysisForSelectionMenuItem.setEnabled(b);
					dailyEnergyAnalysisMenuItem.setEnabled(b);
					dailyEnergyAnalysisForSelectionMenuItem.setEnabled(b);
					// orientationalEnergyAnalysisMenuItem.setEnabled(b);
				}

				@Override
				public void menuCanceled(final MenuEvent e) {
					simulationSettingsMenuItem.setEnabled(true);
					visualizationSettingsMenuItem.setEnabled(true);
					enableEnergyAnalysis(true);
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					SceneManager.getInstance().refresh();
					simulationSettingsMenuItem.setEnabled(true);
					visualizationSettingsMenuItem.setEnabled(true);
					enableEnergyAnalysis(true);
				}

				@Override
				public void menuSelected(final MenuEvent e) {
					MainPanel.getInstance().defaultTool();
					final boolean b = !Scene.getInstance().isStudentMode();
					simulationSettingsMenuItem.setEnabled(b);
					visualizationSettingsMenuItem.setEnabled(b);
					enableEnergyAnalysis(!Scene.getInstance().getOnlySolarAnalysis());
				}
			});
			analysisMenu.add(getAnnualEnergyAnalysisMenuItem());
			analysisMenu.add(getAnnualEnergyAnalysisForSelectionMenuItem());
			analysisMenu.add(getDailyEnergyAnalysisMenuItem());
			analysisMenu.add(getDailyEnergyAnalysisForSelectionMenuItem());
			analysisMenu.addSeparator();
			analysisMenu.add(getAnnualPvAnalysisMenuItem());
			analysisMenu.add(getDailyPvAnalysisMenuItem());
			analysisMenu.addSeparator();
			analysisMenu.add(getAnnualMirrorAnalysisMenuItem());
			analysisMenu.add(getDailyMirrorAnalysisMenuItem());
			analysisMenu.addSeparator();
			analysisMenu.add(getAnnualParabolicTroughAnalysisMenuItem());
			analysisMenu.add(getDailyParabolicTroughAnalysisMenuItem());
			analysisMenu.addSeparator();
			analysisMenu.add(getAnnualParabolicDishAnalysisMenuItem());
			analysisMenu.add(getDailyParabolicDishAnalysisMenuItem());
			analysisMenu.addSeparator();
			analysisMenu.add(getAnnualFresnelReflectorAnalysisMenuItem());
			analysisMenu.add(getDailyFresnelReflectorAnalysisMenuItem());
			analysisMenu.addSeparator();
			analysisMenu.add(getGroupAnnualAnalysisMenuItem());
			analysisMenu.add(getGroupDailyAnalysisMenuItem());
			analysisMenu.addSeparator();
			analysisMenu.add(getConstructionCostAnalysisMenuItem());
			analysisMenu.add(getAnnualSensorMenuItem());
			analysisMenu.add(getDailySensorMenuItem());
			// analysisMenu.add(getOrientationalEnergyAnalysisMenuItem());
			analysisMenu.addSeparator();
			analysisMenu.add(getSimulationSettingsMenuItem());
		}
		return analysisMenu;
	}

	private JMenu getTemplatesMenu() {
		if (examplesMenu == null) {
			examplesMenu = new JMenu("Examples");
			examplesMenu.addMenuListener(new MenuListener() {
				@Override
				public void menuCanceled(final MenuEvent e) {
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					SceneManager.getInstance().refresh();
				}

				@Override
				public void menuSelected(final MenuEvent e) {
					MainPanel.getInstance().defaultTool();
				}
			});
			final JMenu benchmarksMenu = new JMenu("Benchmarks");
			examplesMenu.add(benchmarksMenu);
			final JMenu bestestMenu = new JMenu("Building Energy Simulation Tests");
			benchmarksMenu.add(bestestMenu);
			addModel(bestestMenu, "BESTEST Case 600", "benchmarks/bestest600.ng3");
			addModel(bestestMenu, "BESTEST Case 610", "benchmarks/bestest610.ng3");
			addModel(bestestMenu, "BESTEST Case 620", "benchmarks/bestest620.ng3");
			addModel(bestestMenu, "BESTEST Case 630", "benchmarks/bestest630.ng3");
			final JMenu simpleMenu = new JMenu("Simple Buildings");
			examplesMenu.add(simpleMenu);
			addModel(simpleMenu, "Apartment 1", "templates/apartment-template-1.ng3");
			addModel(simpleMenu, "Apartment 2", "templates/apartment-template-2.ng3");
			addModel(simpleMenu, "Box Gabled Roof", "templates/box-gabled-template.ng3");
			addModel(simpleMenu, "Bungalow", "templates/bungalow-template.ng3");
			addModel(simpleMenu, "Butterfly Roof", "templates/butterfly-template.ng3");
			addModel(simpleMenu, "Cape Cod", "templates/cape-cod-template.ng3");
			addModel(simpleMenu, "Colonial", "templates/colonial-template.ng3");
			addModel(simpleMenu, "Combination Roof", "templates/combination-roof-template.ng3");
			addModel(simpleMenu, "Cross Gabled Roof", "templates/cross-gabled-template.ng3");
			addModel(simpleMenu, "Cross Hipped Roof", "templates/cross-hipped-template.ng3");
			addModel(simpleMenu, "Dutch Colonial", "templates/gambrel-template.ng3");
			addModel(simpleMenu, "Flat Roof", "templates/flat-roof-template.ng3");
			addModel(simpleMenu, "Gable & Valley Roof", "templates/gable-valley-template.ng3");
			addModel(simpleMenu, "Gablet Roof", "templates/gablet-template.ng3");
			addModel(simpleMenu, "Hip Roof", "templates/hip-roof-template.ng3");
			addModel(simpleMenu, "Hip & Valley Roof", "templates/hip-valley-template.ng3");
			addModel(simpleMenu, "M-Shaped Roof", "templates/m-shaped-template.ng3");
			addModel(simpleMenu, "Mansard", "templates/mansard-template.ng3");
			addModel(simpleMenu, "Saltbox 1", "templates/saltbox-template-1.ng3");
			addModel(simpleMenu, "Saltbox 2", "templates/saltbox-template-2.ng3");
			addModel(simpleMenu, "Shed Roof", "templates/shed-roof-template.ng3");
			final JMenu complexMenu = new JMenu("Complex Buildings");
			examplesMenu.add(complexMenu);
			addModel(complexMenu, "Cape Cod with Front Porch", "templates/example-cape-cod-front-porch.ng3");
			addModel(complexMenu, "Cape Cod with Garage", "templates/example-cape-cod-attached-garage.ng3");
			addModel(complexMenu, "Colonial with Fence", "templates/example-colonial-fence.ng3");
			addModel(complexMenu, "Colonial with Front Porch", "templates/example-colonial-front-porch.ng3");
			addModel(complexMenu, "L-Shaped Colonial", "templates/example-colonial-l-shaped.ng3");
			addModel(complexMenu, "Dutch Colonial with Front Porch", "templates/example-dutch-colonial.ng3");
			addModel(complexMenu, "Federal", "templates/example-federal.ng3");
			addModel(complexMenu, "Victorian", "templates/example-victorian.ng3");
			addModel(complexMenu, "Shingle", "templates/example-shingle.ng3");
			addModel(complexMenu, "Sunroom", "templates/example-sunroom.ng3");
			addModel(complexMenu, "Barn House", "templates/example-barn-house.ng3");
			addModel(complexMenu, "Santa Fe Style House", "templates/example-santa-fe.ng3");
			final JMenu pvSolarMenu = new JMenu("Photovoltaic Systems");
			addModel(pvSolarMenu, "Solar Canopy: Wavy Top", "templates/example-solar-canopy-wavy-top.ng3");
			addModel(pvSolarMenu, "Solar Canopy: Curvy Top", "templates/example-solar-canopy-curvy-top.ng3");
			addModel(pvSolarMenu, "Solar Canopy: Bus Stop", "templates/example-solar-canopy-bus-stop.ng3");
			addModel(pvSolarMenu, "Solar Canopy: Parking Garage 1", "templates/example-solar-canopy-parking-garage-1.ng3");
			addModel(pvSolarMenu, "Solar Canopy: Parking Garage 2", "templates/example-solar-canopy-parking-garage-2.ng3");
			addModel(pvSolarMenu, "Solar Canopy: Overhang", "templates/example-solar-canopy-overhang.ng3");
			addModel(pvSolarMenu, "Solar Facades: Example 1", "templates/example-solar-facade1.ng3");
			addModel(pvSolarMenu, "Solar Facades: Example 2", "templates/example-solar-facade2.ng3");
			examplesMenu.add(pvSolarMenu);
			final JMenu cspSolarMenu = new JMenu("Concentrated Solar Power Systems");
			cspSolarMenu.setEnabled(false);
			examplesMenu.add(cspSolarMenu);
			final JMenu miscMenu = new JMenu("Miscellaneous");
			examplesMenu.add(miscMenu);
			addModel(miscMenu, "Dome", "templates/dome-template.ng3");
			addModel(miscMenu, "Egyptian Pyramid", "templates/egyptian-pyramid-template.ng3");
			addModel(miscMenu, "Mayan Pyramid", "templates/mayan-pyramid-template.ng3");
			addModel(miscMenu, "Stadium", "templates/stadium-template.ng3");
		}
		return examplesMenu;
	}

	private void addModel(final JMenu menu, final String type, final String url) {
		final JMenuItem mi = new JMenuItem(type);
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				openModel(MainApplication.class.getResource(url));
			}
		});
		menu.add(mi);
	}

	private JMenu getTutorialsMenu() {
		if (tutorialsMenu == null) {
			tutorialsMenu = new JMenu("Tutorials");
			tutorialsMenu.addMenuListener(new MenuListener() {
				@Override
				public void menuCanceled(final MenuEvent e) {
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					SceneManager.getInstance().refresh();
				}

				@Override
				public void menuSelected(final MenuEvent e) {
					MainPanel.getInstance().defaultTool();
				}
			});

			final JMenu uiBasicsMenu = new JMenu("User Interface Basics");
			uiBasicsMenu.setEnabled(false);
			tutorialsMenu.add(uiBasicsMenu);

			final JMenu buildingBasicsMenu = new JMenu("Building Science Basics");
			tutorialsMenu.add(buildingBasicsMenu);
			addModel(buildingBasicsMenu, "Two Houses of Different Sizes", "tutorials/building-size.ng3");
			addModel(buildingBasicsMenu, "Two Houses of Different Shapes", "tutorials/building-shape.ng3");
			addModel(buildingBasicsMenu, "Two Houses with Different Roof Insulations", "tutorials/building-roof-insulation.ng3");
			addModel(buildingBasicsMenu, "Two Houses with Different Roof Colors", "tutorials/building-cool-roof.ng3");
			addModel(buildingBasicsMenu, "Two Houses with Different Window SHGCs", "tutorials/building-window-shgc.ng3");
			addModel(buildingBasicsMenu, "Two Houses with Different Orientations", "tutorials/building-orientation.ng3");
			addModel(buildingBasicsMenu, "Two Houses with Different Thermostat Settings", "tutorials/building-thermostat.ng3");
			buildingBasicsMenu.addSeparator();
			addModel(buildingBasicsMenu, "Energy Use at Different Locations", "tutorials/building-location.ng3");
			addModel(buildingBasicsMenu, "Effect of Environment Albedo", "tutorials/building-albedo.ng3");
			addModel(buildingBasicsMenu, "Passive Heating with Windows", "tutorials/building-passive-heating.ng3");
			addModel(buildingBasicsMenu, "Passive Cooling with Trees", "tutorials/building-tree-passive-cooling.ng3");

			final JMenu solarBasicsMenu = new JMenu("Solar Science Basics");
			tutorialsMenu.add(solarBasicsMenu);
			addModel(solarBasicsMenu, "Solar Angles", "tutorials/solar-angles.ng3");
			addModel(solarBasicsMenu, "Solar Box", "tutorials/solar-box.ng3");
			addModel(solarBasicsMenu, "Solar Irradiance Heat Map", "tutorials/solar-heat-map.ng3");
			addModel(solarBasicsMenu, "Solar Analysis of Cities", "tutorials/city-block.ng3");
			solarBasicsMenu.addSeparator();
			addModel(solarBasicsMenu, "Solar Panel Tilt Angles", "tutorials/solar-panel-tilt-angle.ng3");
			addModel(solarBasicsMenu, "Solar Panel Azimuthal Angles", "tutorials/solar-panel-azimuth-angle.ng3");
			addModel(solarBasicsMenu, "Solar Panel Orientation", "tutorials/solar-panel-orientation.ng3");
			addModel(solarBasicsMenu, "Solar Panel Cell Efficiency", "tutorials/solar-panel-cell-efficiency.ng3");
			addModel(solarBasicsMenu, "Nominal Operating Cell Temperature", "tutorials/solar-panel-noct.ng3");
			addModel(solarBasicsMenu, "Solar Trackers", "tutorials/solar-trackers.ng3");

			final JMenu designBasicsMenu = new JMenu("Engineering Design Basics");
			tutorialsMenu.add(designBasicsMenu);
			addModel(designBasicsMenu, "Single-Variable, Single-Objective Optimization", "tutorials/guided-design-single-variable-single-objective.ng3");
			// addModel(designBasicsMenu, "Multi-Objective Optimization", "tutorials/guided-design-yield-area-vs-yield-cost.ng3");

			final JMenu pvMenu = new JMenu("Photovoltaic Solar Power");
			tutorialsMenu.add(pvMenu);
			addModel(pvMenu, "Single Solar Rack", "tutorials/solar-single-rack.ng3");
			addModel(pvMenu, "Multiple Solar Racks", "tutorials/solar-multiple-racks.ng3");
			addModel(pvMenu, "Solar Trackers for Racks", "tutorials/solar-tracker-racks.ng3");
			addModel(pvMenu, "Solar Canopy Form Factors", "tutorials/solar-canopy-form-factors.ng3");
			pvMenu.addSeparator();
			addModel(pvMenu, "Rooftop Solar Power System", "tutorials/pv-rooftop-system.ng3");
			addModel(pvMenu, "Parking Lot Solar Canopy", "tutorials/solar-canopy.ng3");
			addModel(pvMenu, "Photovoltaic Solar Farm: Fixed Rack Arrays", "tutorials/pv-fixed-rack-arrays.ng3");
			addModel(pvMenu, "Photovoltaic Solar Farm: HSAT Rack Arrays", "tutorials/pv-hsat-rack-arrays.ng3");
			addModel(pvMenu, "Photovoltaic Solar Farm: Cost-Effectiveness", "tutorials/solar-rack-why-array.ng3");
			addModel(pvMenu, "Photovoltaic Solar Farm: The Effect of Inter-Row Spacing", "tutorials/solar-rack-array-row-spacing.ng3");
			addModel(pvMenu, "Photovoltaic Solar Farm: Landscape vs. Portrait Arrays", "tutorials/solar-rack-array-row-spacing-portrait.ng3");

			final JMenu cspMenu = new JMenu("Concentrated Solar Power");
			tutorialsMenu.add(cspMenu);
			addModel(cspMenu, "Parabolic Trough Focal Line", "tutorials/parabolic-trough-focal-line.ng3");
			addModel(cspMenu, "Parabolic Trough Rim Angle", "tutorials/parabolic-trough-curvature.ng3");
			addModel(cspMenu, "Parabolic Trough Arrays", "tutorials/parabolic-trough-array.ng3");
			cspMenu.addSeparator();
			addModel(cspMenu, "Parabolic Dish Stirling Engine", "tutorials/parabolic-dish-single.ng3");
			addModel(cspMenu, "Parabolic Dish Focal Length", "tutorials/parabolic-dish-focal-length.ng3");
			addModel(cspMenu, "Parabolic Dish Array", "tutorials/parabolic-dish-array.ng3");
			cspMenu.addSeparator();
			addModel(cspMenu, "Linear Fresnel Reflectors", "tutorials/linear-fresnel-reflectors.ng3");
			addModel(cspMenu, "Compact Linear Fresnel Reflectors", "tutorials/compact-linear-fresnel-reflectors.ng3");
			cspMenu.addSeparator();
			addModel(cspMenu, "Concentrated Solar Power Tower", "tutorials/concentrated-solar-power-tower.ng3");
			addModel(cspMenu, "Cosine Efficiency", "tutorials/csp-cosine-efficiency.ng3");
			addModel(cspMenu, "Shadowing and Blocking", "tutorials/csp-shadowing-blocking.ng3");
			addModel(cspMenu, "Shadowing and Blocking (Reduced Mirror Height)", "tutorials/csp-shadowing-blocking-less.ng3");
			addModel(cspMenu, "Shadowing and Blocking (Increased Radial Spacing)", "tutorials/csp-shadowing-blocking-even-less.ng3");
			addModel(cspMenu, "The Effect of Solar Tower Height", "tutorials/csp-tower-height.ng3");
			addModel(cspMenu, "Fermat Spiral Layout of Heliostats (Sunflower Pattern)", "tutorials/csp-spiral-layout.ng3");

		}
		return tutorialsMenu;
	}

	private JMenu getViewMenu() {
		if (viewMenu == null) {
			viewMenu = new JMenu("View");
			viewMenu.addMenuListener(new MenuListener() {
				@Override
				public void menuCanceled(final MenuEvent e) {
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					SceneManager.getInstance().refresh();
				}

				@Override
				public void menuSelected(final MenuEvent e) {
					Util.selectSilently(showSolarLandMenuItem, Scene.getInstance().getSolarMapForLand());
					Util.selectSilently(onlySolarComponentsInSolarMapMenuItem, Scene.getInstance().getOnlySolarComponentsInSolarMap());
					Util.selectSilently(solarRadiationHeatMapMenuItem, SceneManager.getInstance().getSolarHeatMap());
					Util.selectSilently(solarAbsorptionHeatMapMenuItem, Scene.getInstance().getOnlyAbsorptionInSolarMap());
					Util.selectSilently(onlyReflectionHeatMapMenuItem, Scene.getInstance().getOnlyReflectedEnergyInMirrorSolarMap());
					Util.selectSilently(showHeatFluxVectorsMenuItem, Scene.getInstance().getAlwaysComputeHeatFluxVectors());
					Util.selectSilently(shadowMenuItem, SceneManager.getInstance().isShadowEnabled());
					Util.selectSilently(axesMenuItem, SceneManager.getInstance().areAxesVisible());
					Util.selectSilently(sunAnglesMenuItem, Scene.getInstance().areSunAnglesVisible());
					Util.selectSilently(lightBeamsMenuItem, Scene.getInstance().areLightBeamsVisible());
					Util.selectSilently(disableShadowInActionMenuItem, Scene.getInstance().getDisableShadowInAction());
					Util.selectSilently(roofDashedLineMenuItem, Scene.getInstance().areDashedLinesOnRoofShown());
					Util.selectSilently(lightBeamsMenuItem, Scene.getInstance().areLightBeamsVisible());
					MainPanel.getInstance().defaultTool();
				}
			});

			final JMenu solarHeatMapMenu = new JMenu("Solar Irradiance Heat Map Options");
			solarHeatMapMenu.add(getOnlySolarComponentsInSolarMapMenuItem());
			solarHeatMapMenu.add(getSolarAbsorptionHeatMapMenuItem());
			solarHeatMapMenu.add(getOnlyReflectionHeatMapMenuItem());
			solarHeatMapMenu.add(getShowSolarLandMenuItem());

			final JMenu weatherMenu = new JMenu("Weather");
			weatherMenu.add(getMonthlySunshineHoursMenuItem());
			weatherMenu.add(getAnnualEnvironmentalTemperatureMenuItem());
			weatherMenu.add(getDailyEnvironmentalTemperatureMenuItem());

			// viewMenu.add(getUnitsMenu()); // disable temporarily because it doesn't work to expectation
			viewMenu.add(getOrbitMenuItem());
			viewMenu.add(getFirstPersonMenuItem());
			viewMenu.addSeparator();
			final ButtonGroup bg = new ButtonGroup();
			bg.add(orbitMenuItem);
			bg.add(firstPersonMenuItem);
			viewMenu.add(getTopViewCheckBoxMenuItem());
			viewMenu.add(getResetCameraMenuItem());
			viewMenu.add(getZoomInMenuItem());
			viewMenu.add(getZoomOutMenuItem());
			viewMenu.addSeparator();
			viewMenu.add(weatherMenu);
			viewMenu.add(getTextureMenu());
			viewMenu.add(getThemeMenu());
			viewMenu.add(getGroundImageMenu());
			viewMenu.addSeparator();
			viewMenu.add(getSolarRadiationHeatMapMenuItem());
			viewMenu.add(solarHeatMapMenu);
			viewMenu.add(getHeatFluxMenuItem());
			viewMenu.add(getShadowMenuItem());
			viewMenu.add(getDisableShadowInActionMenuItem());
			viewMenu.add(getSunAnglesMenuItem());
			viewMenu.add(getLightBeamsMenuItem());
			viewMenu.add(getVisualizationSettingsMenuItem());
			viewMenu.addSeparator();
			viewMenu.add(getAxesMenuItem());
			viewMenu.add(getRoofDashedLineMenuItem());
			viewMenu.add(getAnnotationsInwardMenuItem());
			// viewMenu.add(getWallThicknessMenuItem());
			viewMenu.addSeparator();
			viewMenu.add(getInfoPanelCheckBoxMenuItem());
			viewMenu.add(getNoteCheckBoxMenuItem());

		}
		return viewMenu;
	}

	private JMenu getTextureMenu() {

		if (textureMenu == null) {
			textureMenu = new JMenu("Texture");
			textureMenu.addMenuListener(new MenuListener() {
				@Override
				public void menuCanceled(final MenuEvent e) {
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					SceneManager.getInstance().refresh();
				}

				@Override
				public void menuSelected(final MenuEvent e) {
					Util.selectSilently(noTextureMenuItem, Scene.getInstance().getTextureMode() == TextureMode.None);
					Util.selectSilently(simpleTextureMenuItem, Scene.getInstance().getTextureMode() == TextureMode.Simple);
					Util.selectSilently(fullTextureMenuItem, Scene.getInstance().getTextureMode() == TextureMode.Full);
				}
			});

			textureMenu.add(getNoTextureMenuItem());
			textureMenu.add(getSimpleTextureMenuItem());
			textureMenu.add(getFullTextureMenuItem());

		}
		return textureMenu;

	}

	private JMenu getThemeMenu() {

		if (themeMenu == null) {
			themeMenu = new JMenu("Theme");
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
					Util.selectSilently(cloudySkyMenuItem, Scene.getInstance().getTheme() == Scene.CLOUDY_SKY_THEME);
					Util.selectSilently(desertMenuItem, Scene.getInstance().getTheme() == Scene.DESERT_THEME);
					Util.selectSilently(grasslandMenuItem, Scene.getInstance().getTheme() == Scene.GRASSLAND_THEME);
					Util.selectSilently(forestMenuItem, Scene.getInstance().getTheme() == Scene.FOREST_THEME);
				}
			});

			themeMenu.add(getCloudySkyMenuItem());
			themeMenu.add(getDesertMenuItem());
			themeMenu.add(getGrasslandMenuItem());
			themeMenu.add(getForestMenuItem());

		}
		return themeMenu;

	}

	private JMenu getGroundImageMenu() {

		if (groundImageMenu == null) {
			groundImageMenu = new JMenu("Ground Image");
			groundImageMenu.addMenuListener(new MenuListener() {
				@Override
				public void menuCanceled(final MenuEvent e) {
					showGroundImageMenuItem.setEnabled(true);
					clearGroundImageMenuItem.setEnabled(true);
					rescaleGroundImageMenuItem.setEnabled(true);
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					showGroundImageMenuItem.setEnabled(true);
					clearGroundImageMenuItem.setEnabled(true);
					rescaleGroundImageMenuItem.setEnabled(true);
					SceneManager.getInstance().refresh();
				}

				@Override
				public void menuSelected(final MenuEvent e) {
					final boolean hasGroundImage = Scene.getInstance().isGroundImageEnabled();
					showGroundImageMenuItem.setEnabled(hasGroundImage);
					clearGroundImageMenuItem.setEnabled(hasGroundImage);
					rescaleGroundImageMenuItem.setEnabled(hasGroundImage && !Scene.getInstance().isGroundImageEarthView());
					Util.selectSilently(showGroundImageMenuItem, SceneManager.getInstance().getGroundImageLand().isVisible());
				}
			});

			groundImageMenu.add(getUseEarthViewMenuItem());
			groundImageMenu.add(getUseImageFileMenuItem());
			groundImageMenu.addSeparator();
			groundImageMenu.add(getRescaleGroundImageMenuItem());
			groundImageMenu.add(getClearGroundImageMenuItem());
			groundImageMenu.add(getShowGroundImageMenuItem());

		}
		return groundImageMenu;

	}

	private JMenuItem getUseEarthViewMenuItem() {
		if (useEarthViewMenuItem == null) {
			useEarthViewMenuItem = new JMenuItem("Use Image from Earth View...");
			useEarthViewMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					MapDialog.showDialog();
				}
			});
		}
		return useEarthViewMenuItem;
	}

	private JMenuItem getUseImageFileMenuItem() {
		if (useImageFileMenuItem == null) {
			useImageFileMenuItem = new JMenuItem("Use Image from File...");
			useImageFileMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final File file = FileChooser.getInstance().showDialog(".png", pngFilter, false);
					if (file == null) {
						return;
					}
					try {
						Scene.getInstance().setGroundImage(ImageIO.read(file), 1);
						Scene.getInstance().setGroundImageEarthView(false);
					} catch (final Throwable t) {
						t.printStackTrace();
						JOptionPane.showMessageDialog(MainFrame.this, t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
					Scene.getInstance().setEdited(true);
				}
			});
		}
		return useImageFileMenuItem;
	}

	private JMenuItem getRescaleGroundImageMenuItem() {
		if (rescaleGroundImageMenuItem == null) {
			rescaleGroundImageMenuItem = new JMenuItem("Rescale Image...");
			rescaleGroundImageMenuItem.addActionListener(new ActionListener() {
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
		}
		return rescaleGroundImageMenuItem;
	}

	private JMenuItem getClearGroundImageMenuItem() {
		if (clearGroundImageMenuItem == null) {
			clearGroundImageMenuItem = new JMenuItem("Clear Image");
			clearGroundImageMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().setGroundImage(null, 1);
					Scene.getInstance().setEdited(true);
				}
			});
		}
		return clearGroundImageMenuItem;
	}

	private JCheckBoxMenuItem getShowGroundImageMenuItem() {
		if (showGroundImageMenuItem == null) {
			showGroundImageMenuItem = new JCheckBoxMenuItem("Show Image");
			showGroundImageMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final boolean b = showGroundImageMenuItem.isSelected();
					SceneManager.getInstance().getGroundImageLand().setVisible(b);
					Scene.getInstance().setShowGroundImage(b);
					Scene.getInstance().setEdited(true);
					Scene.getInstance().redrawAll();
				}
			});
		}
		return showGroundImageMenuItem;
	}

	public JCheckBoxMenuItem getAxesMenuItem() {
		if (axesMenuItem == null) {
			axesMenuItem = new JCheckBoxMenuItem("Axes", true);
			axesMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final ShowAxesCommand c = new ShowAxesCommand();
					SceneManager.getInstance().setAxesVisible(axesMenuItem.isSelected());
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
		}
		return axesMenuItem;
	}

	private JCheckBoxMenuItem getSunAnglesMenuItem() {
		if (sunAnglesMenuItem == null) {
			sunAnglesMenuItem = new JCheckBoxMenuItem("Sun Angles");
			sunAnglesMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final ShowSunAnglesCommand c = new ShowSunAnglesCommand();
					Scene.getInstance().setSunAnglesVisible(sunAnglesMenuItem.isSelected());
					Heliodon.getInstance().drawSunTriangle();
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
		}
		return sunAnglesMenuItem;
	}

	private JCheckBoxMenuItem getLightBeamsMenuItem() {
		if (lightBeamsMenuItem == null) {
			lightBeamsMenuItem = new JCheckBoxMenuItem("Reflector Light Beams", true);
			lightBeamsMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final ShowReflectorLightBeamsCommand c = new ShowReflectorLightBeamsCommand();
					Scene.getInstance().setLightBeamsVisible(lightBeamsMenuItem.isSelected());
					Scene.getInstance().setEdited(true);
					Scene.getInstance().redrawAll();
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
		}
		return lightBeamsMenuItem;
	}

	private JCheckBoxMenuItem getDisableShadowInActionMenuItem() {
		if (disableShadowInActionMenuItem == null) {
			disableShadowInActionMenuItem = new JCheckBoxMenuItem("Disable Shadows in Action", false);
			disableShadowInActionMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					Scene.getInstance().setDisableShadowInAction(disableShadowInActionMenuItem.isSelected());
					Scene.getInstance().setEdited(true);
				}
			});
		}
		return disableShadowInActionMenuItem;
	}

	private JCheckBoxMenuItem getRoofDashedLineMenuItem() {
		if (roofDashedLineMenuItem == null) {
			roofDashedLineMenuItem = new JCheckBoxMenuItem("Roof Dashed Lines", false);
			roofDashedLineMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					Scene.getInstance().setDashedLinesOnRoofShown(roofDashedLineMenuItem.isSelected());
					Scene.getInstance().redrawAll();
					Scene.getInstance().setEdited(true);
				}
			});
		}
		return roofDashedLineMenuItem;
	}

	public JCheckBoxMenuItem getShadowMenuItem() {
		if (shadowMenuItem == null) {
			shadowMenuItem = new JCheckBoxMenuItem("Shadows", false);
			shadowMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final ShowShadowCommand c = new ShowShadowCommand();
					SceneManager.getInstance().setShadow(shadowMenuItem.isSelected());
					Util.selectSilently(MainPanel.getInstance().getShadowButton(), shadowMenuItem.isSelected());
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
		}
		return shadowMenuItem;
	}

	private JMenuItem getRescaleMenuItem() {
		if (rescaleMenuItem == null) {
			rescaleMenuItem = new JMenuItem("Rescale...");
			rescaleMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Foundation) {
						new RescaleBuildingDialog((Foundation) selectedPart).setVisible(true);
						return;
					}
					if (selectedPart == null) {
						new ScaleDialog().setVisible(true);
					}
				}
			});
		}
		return rescaleMenuItem;
	}

	private JMenuItem getSimulationSettingsMenuItem() {
		if (simulationSettingsMenuItem == null) {
			simulationSettingsMenuItem = new JMenuItem("Change Simulation Settings...");
			simulationSettingsMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					new SimulationSettingsDialog().setVisible(true);
				}
			});
		}
		return simulationSettingsMenuItem;
	}

	private JMenuItem getVisualizationSettingsMenuItem() {
		if (visualizationSettingsMenuItem == null) {
			visualizationSettingsMenuItem = new JMenuItem("Change Visualization Settings...");
			visualizationSettingsMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					new VisualizationSettingsDialog().setVisible(true);
				}
			});
		}
		return visualizationSettingsMenuItem;
	}

	private JMenuItem getAnnualEnergyAnalysisMenuItem() {
		if (annualEnergyAnalysisMenuItem == null) {
			annualEnergyAnalysisMenuItem = new JMenuItem("Annual Energy Analysis for Selected Building...");
			annualEnergyAnalysisMenuItem.setAccelerator(KeyStroke.getKeyStroke("F3"));
			annualEnergyAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (SceneManager.getInstance().autoSelectBuilding(true) instanceof Foundation) {
						new EnergyAnnualAnalysis().show("Annual Energy");
					}
				}
			});
		}
		return annualEnergyAnalysisMenuItem;
	}

	private JMenuItem getAnnualEnergyAnalysisForSelectionMenuItem() {
		if (annualEnergyAnalysisForSelectionMenuItem == null) {
			annualEnergyAnalysisForSelectionMenuItem = new JMenuItem("Annual Energy Analysis for Selected Part...");
			annualEnergyAnalysisForSelectionMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window || selectedPart instanceof Wall || selectedPart instanceof Roof || selectedPart instanceof Door || selectedPart instanceof SolarPanel || selectedPart instanceof Rack || selectedPart instanceof Foundation) {
						new EnergyAnnualAnalysis().show("Annual Energy for Selected Part");
					} else {
						JOptionPane.showMessageDialog(MainFrame.this, "You must select a building part first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});
		}
		return annualEnergyAnalysisForSelectionMenuItem;
	}

	public JMenuItem getDailyEnergyAnalysisMenuItem() {
		if (dailyEnergyAnalysisMenuItem == null) {
			dailyEnergyAnalysisMenuItem = new JMenuItem("Daily Energy Analysis for Selected Building...");
			dailyEnergyAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (SceneManager.getInstance().autoSelectBuilding(true) instanceof Foundation) {
						final EnergyDailyAnalysis analysis = new EnergyDailyAnalysis();
						if (SceneManager.getInstance().getSolarHeatMap()) {
							analysis.updateGraph();
						}
						analysis.show("Daily Energy");
					}
				}
			});
		}
		return dailyEnergyAnalysisMenuItem;
	}

	private JMenuItem getDailyEnergyAnalysisForSelectionMenuItem() {
		if (dailyEnergyAnalysisForSelectionMenuItem == null) {
			dailyEnergyAnalysisForSelectionMenuItem = new JMenuItem("Daily Energy Analysis for Selected Part...");
			dailyEnergyAnalysisForSelectionMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window || selectedPart instanceof Wall || selectedPart instanceof Roof || selectedPart instanceof Door || selectedPart instanceof SolarPanel || selectedPart instanceof Rack || selectedPart instanceof Foundation) {
						new EnergyDailyAnalysis().show("Daily Energy for Selected Part");
					} else {
						JOptionPane.showMessageDialog(MainFrame.this, "You must select a building part first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});
		}
		return dailyEnergyAnalysisForSelectionMenuItem;
	}

	private JMenuItem getAnnualPvAnalysisMenuItem() {
		if (annualPvAnalysisMenuItem == null) {
			annualPvAnalysisMenuItem = new JMenuItem("Annual Yield Analysis of Solar Panels...");
			annualPvAnalysisMenuItem.setAccelerator(KeyStroke.getKeyStroke("F4"));
			annualPvAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					int n = Scene.getInstance().countParts(new Class[] { SolarPanel.class, Rack.class });
					if (n <= 0) {
						JOptionPane.showMessageDialog(MainFrame.this, "There is no solar panel to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					final PvAnnualAnalysis a = new PvAnnualAnalysis();
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart != null) {
						Foundation foundation;
						if (selectedPart instanceof Foundation) {
							foundation = (Foundation) selectedPart;
						} else {
							foundation = selectedPart.getTopContainer();
						}
						if (foundation != null) {
							n = foundation.countParts(new Class[] { SolarPanel.class, Rack.class });
							if (n <= 0) {
								JOptionPane.showMessageDialog(MainFrame.this, "There is no solar panel on this building to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
							a.setUtilityBill(foundation.getUtilityBill());
						}
					} else {
						a.setUtilityBill(Scene.getInstance().getUtilityBill());
					}
					a.show();
				}
			});
		}
		return annualPvAnalysisMenuItem;
	}

	private JMenuItem getDailyPvAnalysisMenuItem() {
		if (dailyPvAnalysisMenuItem == null) {
			dailyPvAnalysisMenuItem = new JMenuItem("Daily Yield Analysis of Solar Panels...");
			dailyPvAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					int n = Scene.getInstance().countParts(new Class[] { SolarPanel.class, Rack.class });
					if (n <= 0) {
						JOptionPane.showMessageDialog(MainFrame.this, "There is no solar panel to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart != null) {
						Foundation foundation;
						if (selectedPart instanceof Foundation) {
							foundation = (Foundation) selectedPart;
						} else {
							foundation = selectedPart.getTopContainer();
						}
						if (foundation != null) {
							n = foundation.countParts(new Class[] { SolarPanel.class, Rack.class });
							if (n <= 0) {
								JOptionPane.showMessageDialog(MainFrame.this, "There is no solar panel on this building to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}
					final PvDailyAnalysis a = new PvDailyAnalysis();
					if (SceneManager.getInstance().getSolarHeatMap()) {
						a.updateGraph();
					}
					a.show();
				}
			});
		}
		return dailyPvAnalysisMenuItem;
	}

	private JMenuItem getDailyMirrorAnalysisMenuItem() {
		if (dailyMirrorAnalysisMenuItem == null) {
			dailyMirrorAnalysisMenuItem = new JMenuItem("Daily Yield Analysis of Mirrors (Heliostats)...");
			dailyMirrorAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					int n = Scene.getInstance().countParts(Mirror.class);
					if (n <= 0) {
						JOptionPane.showMessageDialog(MainFrame.this, "There is no mirror to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart != null) {
						Foundation foundation;
						if (selectedPart instanceof Foundation) {
							foundation = (Foundation) selectedPart;
						} else {
							foundation = selectedPart.getTopContainer();
						}
						if (foundation != null) {
							n = foundation.countParts(Mirror.class);
							if (n <= 0) {
								JOptionPane.showMessageDialog(MainFrame.this, "There is no mirror on this platform to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}
					final MirrorDailyAnalysis a = new MirrorDailyAnalysis();
					if (SceneManager.getInstance().getSolarHeatMap()) {
						a.updateGraph();
					}
					a.show();
				}
			});
		}
		return dailyMirrorAnalysisMenuItem;
	}

	private JMenuItem getAnnualMirrorAnalysisMenuItem() {
		if (annualMirrorAnalysisMenuItem == null) {
			annualMirrorAnalysisMenuItem = new JMenuItem("Annual Yield Analysis of Mirrors (Heliostats)...");
			annualMirrorAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					int n = Scene.getInstance().countParts(Mirror.class);
					if (n <= 0) {
						JOptionPane.showMessageDialog(MainFrame.this, "There is no mirror to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					final MirrorAnnualAnalysis a = new MirrorAnnualAnalysis();
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart != null) {
						Foundation foundation;
						if (selectedPart instanceof Foundation) {
							foundation = (Foundation) selectedPart;
						} else {
							foundation = selectedPart.getTopContainer();
						}
						if (foundation != null) {
							n = foundation.countParts(Mirror.class);
							if (n <= 0) {
								JOptionPane.showMessageDialog(MainFrame.this, "There is no mirror on this building to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}
					a.show();
				}
			});
		}
		return annualMirrorAnalysisMenuItem;
	}

	private JMenuItem getDailyParabolicTroughAnalysisMenuItem() {
		if (dailyParabolicTroughAnalysisMenuItem == null) {
			dailyParabolicTroughAnalysisMenuItem = new JMenuItem("Daily Yield Analysis of Parabolic Troughs...");
			dailyParabolicTroughAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					int n = Scene.getInstance().countParts(ParabolicTrough.class);
					if (n <= 0) {
						JOptionPane.showMessageDialog(MainFrame.this, "There is no parabolic trough to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart != null) {
						Foundation foundation;
						if (selectedPart instanceof Foundation) {
							foundation = (Foundation) selectedPart;
						} else {
							foundation = selectedPart.getTopContainer();
						}
						if (foundation != null) {
							n = foundation.countParts(ParabolicTrough.class);
							if (n <= 0) {
								JOptionPane.showMessageDialog(MainFrame.this, "There is no parabolic trough on this platform to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}
					final ParabolicTroughDailyAnalysis a = new ParabolicTroughDailyAnalysis();
					if (SceneManager.getInstance().getSolarHeatMap()) {
						a.updateGraph();
					}
					a.show();
				}
			});
		}
		return dailyParabolicTroughAnalysisMenuItem;
	}

	private JMenuItem getAnnualParabolicTroughAnalysisMenuItem() {
		if (annualParabolicTroughAnalysisMenuItem == null) {
			annualParabolicTroughAnalysisMenuItem = new JMenuItem("Annual Yield Analysis of Parabolic Troughs...");
			annualParabolicTroughAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					int n = Scene.getInstance().countParts(ParabolicTrough.class);
					if (n <= 0) {
						JOptionPane.showMessageDialog(MainFrame.this, "There is no parabolic trough to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					final ParabolicTroughAnnualAnalysis a = new ParabolicTroughAnnualAnalysis();
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart != null) {
						Foundation foundation;
						if (selectedPart instanceof Foundation) {
							foundation = (Foundation) selectedPart;
						} else {
							foundation = selectedPart.getTopContainer();
						}
						if (foundation != null) {
							n = foundation.countParts(ParabolicTrough.class);
							if (n <= 0) {
								JOptionPane.showMessageDialog(MainFrame.this, "There is no parabolic trough on this building to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}
					a.show();
				}
			});
		}
		return annualParabolicTroughAnalysisMenuItem;
	}

	private JMenuItem getDailyParabolicDishAnalysisMenuItem() {
		if (dailyParabolicDishAnalysisMenuItem == null) {
			dailyParabolicDishAnalysisMenuItem = new JMenuItem("Daily Yield Analysis of Parabolic Dishes...");
			dailyParabolicDishAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					int n = Scene.getInstance().countParts(ParabolicDish.class);
					if (n <= 0) {
						JOptionPane.showMessageDialog(MainFrame.this, "There is no parabolic dish to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart != null) {
						Foundation foundation;
						if (selectedPart instanceof Foundation) {
							foundation = (Foundation) selectedPart;
						} else {
							foundation = selectedPart.getTopContainer();
						}
						if (foundation != null) {
							n = foundation.countParts(ParabolicDish.class);
							if (n <= 0) {
								JOptionPane.showMessageDialog(MainFrame.this, "There is no parabolic dish on this platform to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}
					final ParabolicDishDailyAnalysis a = new ParabolicDishDailyAnalysis();
					if (SceneManager.getInstance().getSolarHeatMap()) {
						a.updateGraph();
					}
					a.show();
				}
			});
		}
		return dailyParabolicDishAnalysisMenuItem;
	}

	private JMenuItem getAnnualParabolicDishAnalysisMenuItem() {
		if (annualParabolicDishAnalysisMenuItem == null) {
			annualParabolicDishAnalysisMenuItem = new JMenuItem("Annual Yield Analysis of Parabolic Dishes...");
			annualParabolicDishAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					int n = Scene.getInstance().countParts(ParabolicDish.class);
					if (n <= 0) {
						JOptionPane.showMessageDialog(MainFrame.this, "There is no parabolic dish to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					final ParabolicDishAnnualAnalysis a = new ParabolicDishAnnualAnalysis();
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart != null) {
						Foundation foundation;
						if (selectedPart instanceof Foundation) {
							foundation = (Foundation) selectedPart;
						} else {
							foundation = selectedPart.getTopContainer();
						}
						if (foundation != null) {
							n = foundation.countParts(ParabolicDish.class);
							if (n <= 0) {
								JOptionPane.showMessageDialog(MainFrame.this, "There is no parabolic dish on this building to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}
					a.show();
				}
			});
		}
		return annualParabolicDishAnalysisMenuItem;
	}

	private JMenuItem getDailyFresnelReflectorAnalysisMenuItem() {
		if (dailyFresnelReflectorAnalysisMenuItem == null) {
			dailyFresnelReflectorAnalysisMenuItem = new JMenuItem("Daily Yield Analysis of Fresnel Reflectors...");
			dailyFresnelReflectorAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					int n = Scene.getInstance().countParts(FresnelReflector.class);
					if (n <= 0) {
						JOptionPane.showMessageDialog(MainFrame.this, "There is no Fresnel reflector to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart != null) {
						Foundation foundation;
						if (selectedPart instanceof Foundation) {
							foundation = (Foundation) selectedPart;
						} else {
							foundation = selectedPart.getTopContainer();
						}
						if (foundation != null) {
							n = foundation.countParts(FresnelReflector.class);
							if (n <= 0) {
								JOptionPane.showMessageDialog(MainFrame.this, "There is no Fresnel reflector on this platform to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}
					final FresnelReflectorDailyAnalysis a = new FresnelReflectorDailyAnalysis();
					if (SceneManager.getInstance().getSolarHeatMap()) {
						a.updateGraph();
					}
					a.show();
				}
			});
		}
		return dailyFresnelReflectorAnalysisMenuItem;
	}

	private JMenuItem getAnnualFresnelReflectorAnalysisMenuItem() {
		if (annualFresnelReflectorAnalysisMenuItem == null) {
			annualFresnelReflectorAnalysisMenuItem = new JMenuItem("Annual Yield Analysis of Fresnel Reflectors...");
			annualFresnelReflectorAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					int n = Scene.getInstance().countParts(FresnelReflector.class);
					if (n <= 0) {
						JOptionPane.showMessageDialog(MainFrame.this, "There is no Fresnel reflector to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					final FresnelReflectorAnnualAnalysis a = new FresnelReflectorAnnualAnalysis();
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart != null) {
						Foundation foundation;
						if (selectedPart instanceof Foundation) {
							foundation = (Foundation) selectedPart;
						} else {
							foundation = selectedPart.getTopContainer();
						}
						if (foundation != null) {
							n = foundation.countParts(FresnelReflector.class);
							if (n <= 0) {
								JOptionPane.showMessageDialog(MainFrame.this, "There is no Fresnel reflector on this building to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}
					a.show();
				}
			});
		}
		return annualFresnelReflectorAnalysisMenuItem;
	}

	private JMenuItem getGroupDailyAnalysisMenuItem() {
		if (groupDailyAnalysisMenuItem == null) {
			groupDailyAnalysisMenuItem = new JMenuItem("Daily Analysis for Group...");
			groupDailyAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					final PartGroup g = selectGroup();
					if (g != null) {
						final GroupDailyAnalysis a = new GroupDailyAnalysis(g);
						a.show(g.getType() + ": " + g.getIds());
					}
					SceneManager.getInstance().hideAllEditPoints();
				}
			});
		}
		return groupDailyAnalysisMenuItem;
	}

	private JMenuItem getGroupAnnualAnalysisMenuItem() {
		if (groupAnnualAnalysisMenuItem == null) {
			groupAnnualAnalysisMenuItem = new JMenuItem("Annual Analysis for Group...");
			groupAnnualAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					final PartGroup g = selectGroup();
					if (g != null) {
						final GroupAnnualAnalysis a = new GroupAnnualAnalysis(g);
						a.show(g.getType() + ": " + g.getIds());
					}
					SceneManager.getInstance().hideAllEditPoints();
				}
			});
		}
		return groupAnnualAnalysisMenuItem;
	}

	private ArrayList<Long> getIdArray(final Class<?> c) {
		final ArrayList<Long> idArray = new ArrayList<Long>();
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (c.isInstance(p)) {
				idArray.add(p.getId());
			}
		}
		Collections.sort(idArray);
		return idArray;
	}

	private Class<?> getCurrentGroupClass() {
		Class<?> c = null;
		if ("Wall".equals(currentGroupType)) {
			c = Wall.class;
		} else if ("Window".equals(currentGroupType)) {
			c = Window.class;
		} else if ("Roof".equals(currentGroupType)) {
			c = Roof.class;
		} else if ("Solar Panel".equals(currentGroupType)) {
			c = SolarPanel.class;
		} else if ("Solar Panel Rack".equals(currentGroupType)) {
			c = Rack.class;
		} else if ("Mirror".equals(currentGroupType)) {
			c = Mirror.class;
		} else if ("Parabolic Trough".equals(currentGroupType)) {
			c = ParabolicTrough.class;
		} else if ("Parabolic Dish".equals(currentGroupType)) {
			c = ParabolicDish.class;
		} else if ("Fresnel Reflector".equals(currentGroupType)) {
			c = FresnelReflector.class;
		} else if ("Foundation".equals(currentGroupType) || currentGroupType.startsWith("Foundation")) {
			c = Foundation.class;
		}
		return c;
	}

	private PartGroup selectGroup() {
		final JPanel gui = new JPanel(new BorderLayout(5, 5));
		gui.setBorder(BorderFactory.createTitledBorder("Types and IDs"));
		final DefaultListModel<Long> idListModel = new DefaultListModel<Long>();
		final JComboBox<String> typeComboBox = new JComboBox<String>(new String[] { "Solar Panel", "Solar Panel Rack", "Mirror", "Parabolic Trough", "Parabolic Dish", "Fresnel Reflector", "Window", "Wall", "Roof", "Foundation", "Foundation (Mean)" });
		if (currentGroupType != null) {
			typeComboBox.setSelectedItem(currentGroupType);
		}
		typeComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				idListModel.clear();
				currentGroupType = (String) typeComboBox.getSelectedItem();
				final Class<?> c = getCurrentGroupClass();
				if (c != null) {
					final ArrayList<Long> idArray = getIdArray(c);
					for (final Long id : idArray) {
						idListModel.addElement(id);
					}
				}
			}
		});
		final Class<?> c = getCurrentGroupClass();
		if (c != null) {
			final ArrayList<Long> idArray = getIdArray(c);
			for (final Long id : idArray) {
				idListModel.addElement(id);
			}
		}
		final JList<Long> idList = new JList<Long>(idListModel);
		idList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent e) {
				SceneManager.getInstance().hideAllEditPoints();
				final List<Long> selectedValues = idList.getSelectedValuesList();
				for (final Long i : selectedValues) {
					final HousePart p = Scene.getInstance().getPart(i);
					p.setEditPointsVisible(true);
					p.draw();
				}
			}
		});
		gui.add(typeComboBox, BorderLayout.NORTH);
		gui.add(new JScrollPane(idList), BorderLayout.CENTER);
		if (JOptionPane.showConfirmDialog(MainFrame.this, gui, "Select a Group", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.CANCEL_OPTION) {
			return null;
		}
		final List<Long> selectedIds = idList.getSelectedValuesList();
		if (selectedIds.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.this, "You must select a group of parts first.", "Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return new PartGroup((String) typeComboBox.getSelectedItem(), selectedIds);
	}

	private JMenuItem getAnnualSensorMenuItem() {
		if (annualSensorMenuItem == null) {
			annualSensorMenuItem = new JMenuItem("Collect Annual Sensor Data...");
			annualSensorMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (Scene.getInstance().hasSensor()) {
						new AnnualSensorData().show("Annual Sensor Data");
					} else {
						JOptionPane.showMessageDialog(MainFrame.this, "There is no sensor.", "No sensor", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});
		}
		return annualSensorMenuItem;
	}

	private JMenuItem getDailySensorMenuItem() {
		if (dailySensorMenuItem == null) {
			dailySensorMenuItem = new JMenuItem("Collect Daily Sensor Data...");
			dailySensorMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (Scene.getInstance().hasSensor()) {
						new DailySensorData().show("Daily Sensor Data");
					} else {
						JOptionPane.showMessageDialog(MainFrame.this, "There is no sensor.", "No sensor", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});
		}
		return dailySensorMenuItem;
	}

	private JCheckBoxMenuItem getSolarRadiationHeatMapMenuItem() {
		if (solarRadiationHeatMapMenuItem == null) {
			solarRadiationHeatMapMenuItem = new JCheckBoxMenuItem("Solar Irradiance Heat Map");
			solarRadiationHeatMapMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().setSolarHeatMap(solarRadiationHeatMapMenuItem.isSelected());
					Util.selectSilently(MainPanel.getInstance().getEnergyViewButton(), solarRadiationHeatMapMenuItem.isSelected());
				}
			});
		}
		return solarRadiationHeatMapMenuItem;
	}

	private JCheckBoxMenuItem getOnlySolarComponentsInSolarMapMenuItem() {
		if (onlySolarComponentsInSolarMapMenuItem == null) {
			onlySolarComponentsInSolarMapMenuItem = new JCheckBoxMenuItem("Show Only On Solar Components");
			onlySolarComponentsInSolarMapMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final boolean b = onlySolarComponentsInSolarMapMenuItem.isSelected();
					Scene.getInstance().setOnlySolarComponentsInSolarMap(b);
					MainPanel.getInstance().getEnergyViewButton().setSelected(false);
					Scene.getInstance().setEdited(true);
					Scene.getInstance().redrawAll();
				}
			});
		}
		return onlySolarComponentsInSolarMapMenuItem;
	}

	private JCheckBoxMenuItem getShowSolarLandMenuItem() {
		if (showSolarLandMenuItem == null) {
			showSolarLandMenuItem = new JCheckBoxMenuItem("Show on Land");
			showSolarLandMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					// final ShowSolarLandCommand c = new ShowSolarLandCommand();
					final boolean b = showSolarLandMenuItem.isSelected();
					SceneManager.getInstance().getSolarLand().setVisible(b);
					Scene.getInstance().setSolarMapForLand(b);
					MainPanel.getInstance().getEnergyViewButton().setSelected(false);
					Scene.getInstance().setEdited(true);
					Scene.getInstance().redrawAll();
					// SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
		}
		return showSolarLandMenuItem;
	}

	private JCheckBoxMenuItem getSolarAbsorptionHeatMapMenuItem() {
		if (solarAbsorptionHeatMapMenuItem == null) {
			solarAbsorptionHeatMapMenuItem = new JCheckBoxMenuItem("Show Only Absorbed Energy (Absorbers)");
			solarAbsorptionHeatMapMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().setOnlyAbsorptionInSolarMap(solarAbsorptionHeatMapMenuItem.isSelected());
					if (SceneManager.getInstance().getSolarHeatMap()) {
						SceneManager.getInstance().setSolarHeatMap(true);
					}
				}
			});
		}
		return solarAbsorptionHeatMapMenuItem;
	}

	private JCheckBoxMenuItem getOnlyReflectionHeatMapMenuItem() {
		if (onlyReflectionHeatMapMenuItem == null) {
			onlyReflectionHeatMapMenuItem = new JCheckBoxMenuItem("Show Only Reflected Energy (Mirrors and Fresnel Reflectors)");
			onlyReflectionHeatMapMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().setOnlyReflectedEnergyInMirrorSolarMap(onlyReflectionHeatMapMenuItem.isSelected());
					if (SceneManager.getInstance().getSolarHeatMap()) {
						SceneManager.getInstance().setSolarHeatMap(true);
					}
				}
			});
		}
		return onlyReflectionHeatMapMenuItem;
	}

	public JCheckBoxMenuItem getHeatFluxMenuItem() {
		if (showHeatFluxVectorsMenuItem == null) {
			showHeatFluxVectorsMenuItem = new JCheckBoxMenuItem("Heat Flux Vectors");
			showHeatFluxVectorsMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ShowHeatFluxCommand c = new ShowHeatFluxCommand();
					Scene.getInstance().setAlwaysComputeHeatFluxVectors(showHeatFluxVectorsMenuItem.isSelected());
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
		}
		return showHeatFluxVectorsMenuItem;
	}

	// private JMenuItem getOrientationalEnergyAnalysisMenuItem() {
	// if (orientationalEnergyAnalysisMenuItem == null) {
	// orientationalEnergyAnalysisMenuItem = new JMenuItem("Run Orientation Analysis...");
	// orientationalEnergyAnalysisMenuItem.addActionListener(new ActionListener() {
	// @Override
	// public void actionPerformed(final ActionEvent e) {
	// final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
	// if ("".equals(city)) {
	// JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
	// return;
	// }
	// final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
	// if (selectedPart == null) {
	// int count = 0;
	// HousePart hp = null;
	// for (final HousePart x : Scene.getInstance().getParts()) {
	// if (x instanceof Foundation) {
	// count++;
	// hp = x;
	// }
	// }
	// if (count == 1) {
	// SceneManager.getInstance().setSelectedPart(hp);
	// } else {
	// JOptionPane.showMessageDialog(MainFrame.this, "You must select a building or a component first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
	// return;
	// }
	// }
	// new EnergyAngularAnalysis().show("Orientation");
	// }
	// });
	// }
	// return orientationalEnergyAnalysisMenuItem;
	// }

	private JMenuItem getConstructionCostAnalysisMenuItem() {
		if (constructionCostAnalysisMenuItem == null) {
			constructionCostAnalysisMenuItem = new JMenuItem("Show Construction Costs...");
			constructionCostAnalysisMenuItem.setAccelerator(KeyStroke.getKeyStroke("F7"));
			constructionCostAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Cost.getInstance().showGraph();
				}
			});
		}
		return constructionCostAnalysisMenuItem;
	}

	private JMenuItem getMonthlySunshineHoursMenuItem() {
		if (monthlySunshineHoursMenuItem == null) {
			monthlySunshineHoursMenuItem = new JMenuItem("Monthly Sunshine Hours...");
			monthlySunshineHoursMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					new MonthlySunshineHours().showDialog();
				}
			});
		}
		return monthlySunshineHoursMenuItem;
	}

	private JMenuItem getAnnualEnvironmentalTemperatureMenuItem() {
		if (annualEnvironmentalTemperatureMenuItem == null) {
			annualEnvironmentalTemperatureMenuItem = new JMenuItem("Annual Environmental Temperature...");
			annualEnvironmentalTemperatureMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					new AnnualEnvironmentalTemperature().showDialog();
				}
			});
		}
		return annualEnvironmentalTemperatureMenuItem;
	}

	private JMenuItem getDailyEnvironmentalTemperatureMenuItem() {
		if (dailyEnvironmentalTemperatureMenuItem == null) {
			dailyEnvironmentalTemperatureMenuItem = new JMenuItem("Daily Environmental Temperature...");
			dailyEnvironmentalTemperatureMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					new DailyEnvironmentalTemperature().showDialog();
				}
			});
		}
		return dailyEnvironmentalTemperatureMenuItem;
	}

	private JCheckBoxMenuItem getAnnotationsInwardMenuItem() {
		if (annotationsInwardMenuItem == null) {
			annotationsInwardMenuItem = new JCheckBoxMenuItem("Annotations Inward");
			annotationsInwardMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					Scene.setDrawAnnotationsInside(annotationsInwardMenuItem.isSelected());
				}
			});

		}
		return annotationsInwardMenuItem;
	}

	public JMenu getEditMenu() {
		if (editMenu == null) {
			editMenu = new JMenu("Edit");
			editMenu.addMenuListener(new MenuListener() {

				private void enableMenuItems() {
					cutMenuItem.setEnabled(true);
					copyMenuItem.setEnabled(true);
					pasteMenuItem.setEnabled(true);
					removeAllLocksMenuItem.setEnabled(true);
					specificationsMenuItem.setEnabled(true);
					autoRecomputeEnergyMenuItem.setEnabled(true);
					rescaleMenuItem.setEnabled(true);
				}

				@Override
				public void menuCanceled(final MenuEvent e) {
					// enable the cut-copy-paste menu items when the menu disappears, otherwise the keystrokes will be disabled with the menu items
					enableMenuItems();
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					SceneManager.getInstance().refresh();
					// enable the cut-copy-paste menu items when the menu disappears, otherwise the keystrokes will be disabled with the menu items
					enableMenuItems();
				}

				@Override
				public void menuSelected(final MenuEvent e) {
					enableMenuItems();
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					cutMenuItem.setEnabled(selectedPart != null);
					copyMenuItem.setEnabled(selectedPart != null && selectedPart.isCopyable());
					final HousePart copyBuffer = Scene.getInstance().getCopyBuffer();
					pasteMenuItem.setEnabled(copyBuffer != null && !(copyBuffer instanceof Foundation));
					Util.selectSilently(noteCheckBoxMenuItem, MainPanel.getInstance().isNoteVisible());
					Util.selectSilently(infoPanelCheckBoxMenuItem, EnergyPanel.getInstance().isVisible());
					Util.selectSilently(snapToGridsMenuItem, Scene.getInstance().isSnapToGrids());
					MainPanel.getInstance().defaultTool();
					if (Scene.getInstance().isStudentMode()) {
						removeAllLocksMenuItem.setEnabled(false);
						specificationsMenuItem.setEnabled(false);
						autoRecomputeEnergyMenuItem.setEnabled(false);
						rescaleMenuItem.setEnabled(false);
					} else {
						rescaleMenuItem.setEnabled(selectedPart instanceof Foundation || selectedPart == null);
					}
					refreshUndoRedo();
				}
			});

			final JMenu clearMenu = new JMenu("Clear");
			clearMenu.add(getRemoveAllFoundationsMenuItem());
			clearMenu.add(getRemoveAllWallsMenuItem());
			clearMenu.add(getRemoveAllWindowsMenuItem());
			clearMenu.add(getRemoveAllWindowShuttersMenuItem());
			clearMenu.add(getRemoveAllSolarPanelsMenuItem());
			clearMenu.add(getRemoveAllRacksMenuItem());
			clearMenu.add(getRemoveAllMirrorsMenuItem());
			clearMenu.add(getRemoveAllParabolicTroughsMenuItem());
			clearMenu.add(getRemoveAllParabolicDishesMenuItem());
			clearMenu.add(getRemoveAllFresnelReflectorsMenuItem());
			clearMenu.add(getRemoveAllTreesMenuItem());
			clearMenu.add(getRemoveAllHumansMenuItem());
			clearMenu.add(getRemoveAllRoofsMenuItem());
			clearMenu.add(getRemoveAllFloorsMenuItem());
			clearMenu.add(getRemoveAllSensorsMenuItem());
			clearMenu.add(getRemoveAllLocksMenuItem());
			clearMenu.add(getRemoveAllUtilityBillsMenuItem());

			final JMenu moveMenu = new JMenu("Move");
			moveMenu.add(getMoveEastMenuItem());
			moveMenu.add(getMoveWestMenuItem());
			moveMenu.add(getMoveNorthMenuItem());
			moveMenu.add(getMoveSouthMenuItem());

			final JMenu rotateMenu = new JMenu("Rotate");
			rotateMenu.add(getRotate180MenuItem());
			rotateMenu.add(getRotate90CwMenuItem());
			rotateMenu.add(getRotate90CcwMenuItem());

			editMenu.add(getUndoMenuItem());
			editMenu.add(getRedoMenuItem());
			editMenu.addSeparator();
			editMenu.add(getCutMenuItem());
			editMenu.add(getCopyMenuItem());
			editMenu.add(getPasteMenuItem());
			editMenu.addSeparator();
			editMenu.add(moveMenu);
			editMenu.add(rotateMenu);
			editMenu.add(clearMenu);
			editMenu.addSeparator();
			editMenu.add(getEnableAllEditPointsMenuItem());
			editMenu.add(getDisableAllEditPointsMenuItem());
			editMenu.add(getFixProblemsMenuItem());
			editMenu.add(getSnapToGridsMenuItem());
			editMenu.add(getSnapMenuItem());
			editMenu.add(getAutoRecomputeEnergyMenuItem());
			editMenu.add(getRescaleMenuItem());
			editMenu.addSeparator();
			editMenu.add(getSetRegionMenuItem());
			editMenu.add(getCustomPricesMenuItem());
			editMenu.add(getSpecificationsMenuItem());
			editMenu.add(getOverallUtilityBillMenuItem());
			editMenu.addSeparator();
			editMenu.add(getPropertiesMenuItem());
		}
		return editMenu;
	}

	public void refreshUndoRedo() {
		final MyUndoManager um = SceneManager.getInstance().getUndoManager();
		final UndoableEdit lastEdit = um.lastEdit();
		long timestampUndo = -1;
		long timestampRedo = -1;
		if (lastEdit instanceof MyAbstractUndoableEdit) {
			if (um.editToBeUndone() != null) {
				timestampUndo = ((MyAbstractUndoableEdit) um.editToBeUndone()).getTimestamp();
			}
			if (um.editToBeRedone() != null) {
				timestampRedo = ((MyAbstractUndoableEdit) um.editToBeRedone()).getTimestamp();
			}
		}
		getUndoMenuItem().setText(um.getUndoPresentationName() + (timestampUndo == -1 ? "" : " (" + EnergyPanel.ONE_DECIMAL.format(0.001 * (System.currentTimeMillis() - timestampUndo)) + " seconds ago)"));
		getUndoMenuItem().setEnabled(um.canUndo());
		getRedoMenuItem().setText(um.getRedoPresentationName() + (timestampRedo == -1 ? "" : " (" + EnergyPanel.ONE_DECIMAL.format(0.001 * (System.currentTimeMillis() - timestampRedo)) + " seconds ago)"));
		getRedoMenuItem().setEnabled(um.canRedo());
	}

	private JMenuItem getUndoMenuItem() {
		if (undoMenuItem == null) {
			undoMenuItem = new JMenuItem("Undo");
			undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			undoMenuItem.setEnabled(false);
			undoMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							MainPanel.getInstance().defaultTool();
							SceneManager.getInstance().hideAllEditPoints();
							SceneManager.getInstance().getUndoManager().undo();
							SceneManager.getInstance().refresh();
							EnergyPanel.getInstance().update();
							return null;
						}
					});
				}
			});
		}
		return undoMenuItem;
	}

	private JMenuItem getRedoMenuItem() {
		if (redoMenuItem == null) {
			redoMenuItem = new JMenuItem("Redo");
			redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			redoMenuItem.setEnabled(false);
			redoMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							MainPanel.getInstance().defaultTool();
							SceneManager.getInstance().hideAllEditPoints();
							SceneManager.getInstance().getUndoManager().redo();
							SceneManager.getInstance().refresh();
							EnergyPanel.getInstance().update();
							return null;
						}
					});
				}
			});
		}
		return redoMenuItem;
	}

	private JMenuItem getCutMenuItem() {
		if (cutMenuItem == null) {
			cutMenuItem = new JMenuItem("Cut");
			cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			cutMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart != null) {
						Scene.getInstance().setCopyBuffer(selectedPart);
						SceneManager.getInstance().deleteCurrentSelection();
					}
				}
			});
		}
		return cutMenuItem;
	}

	private JMenuItem getCopyMenuItem() {
		if (copyMenuItem == null) {
			copyMenuItem = new JMenuItem("Copy");
			copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			copyMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart != null) {
						Scene.getInstance().setCopyBuffer(selectedPart);
					}
				}
			});
		}
		return copyMenuItem;
	}

	private JMenuItem getPasteMenuItem() {
		if (pasteMenuItem == null) {
			pasteMenuItem = new JMenuItem("Paste");
			pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Config.isMac() ? KeyEvent.META_MASK : InputEvent.CTRL_MASK));
			pasteMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							Scene.getInstance().paste();
							return null;
						}
					});
				}
			});
		}
		return pasteMenuItem;

	}

	private void saveFile(final boolean outsideTaskManager) {
		final File file = FileChooser.getInstance().showDialog(".ng3", ng3Filter, true);
		if (file == null) {
			return;
		}
		URL url = null;
		try {
			url = file.toURI().toURL();
		} catch (final MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		if (outsideTaskManager) {
			Scene.saveOutsideTaskManager(url);
		} else {
			Scene.save(url, true);
		}
		updateTitleBar();
		FileChooser.getInstance().rememberFile(file.getAbsolutePath());
	}

	void importFile() {
		final File file = FileChooser.getInstance().showDialog(".ng3", ng3Filter, false);
		if (file != null) {
			EnergyPanel.getInstance().clearRadiationHeatMap();
			SceneManager.getTaskManager().update(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					try {
						Scene.getInstance().importFile(file.toURI().toURL());
					} catch (final Throwable err) {
						Util.reportError(err);
					}
					return null;
				}
			});
		}
	}

	void importColladaFile() {
		final File file = FileChooser.getInstance().showDialog(".dae", daeFilter, false);
		if (file != null) {
			EnergyPanel.getInstance().clearRadiationHeatMap();
			SceneManager.getTaskManager().update(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					try {
						Scene.getInstance().importCollada(file);
					} catch (final Throwable err) {
						Util.reportError(err);
					}
					return null;
				}
			});
		}
	}

	private void exportObjFile() {
		final File file = FileChooser.getInstance().showDialog(".obj", objFilter, true);
		if (file != null) {
			EnergyPanel.getInstance().clearRadiationHeatMap();
			SceneManager.getTaskManager().update(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					try {
						Scene.getInstance().exportObj(file);
					} catch (final Throwable err) {
						Util.reportError(err);
					}
					return null;
				}
			});
		}
	}

	private JMenuItem getPageSetupMenuItem() {
		if (pageSetupMenuItem == null) {
			pageSetupMenuItem = new JMenuItem("Page Setup...");
			pageSetupMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					PrintController.getInstance().pageSetup();
				}
			});
		}
		return pageSetupMenuItem;
	}

	private JRadioButtonMenuItem getScaleToFitRadioButtonMenuItem() {
		if (scaleToFitRadioButtonMenuItem == null) {
			scaleToFitRadioButtonMenuItem = new JRadioButtonMenuItem("Scale To Fit Paper");
			scaleToFitRadioButtonMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					PrintController.getInstance().setScaleToFit(true);
				}
			});
			printSizeOptionBbuttonGroup.add(scaleToFitRadioButtonMenuItem);
			scaleToFitRadioButtonMenuItem.setSelected(true);
		}
		return scaleToFitRadioButtonMenuItem;
	}

	private JRadioButtonMenuItem getExactSizeRadioButtonMenuItem() {
		if (exactSizeRadioButtonMenuItem == null) {
			exactSizeRadioButtonMenuItem = new JRadioButtonMenuItem("Exact Size On Paper");
			exactSizeRadioButtonMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					PrintController.getInstance().setScaleToFit(false);
				}
			});
			printSizeOptionBbuttonGroup.add(exactSizeRadioButtonMenuItem);
		}
		return exactSizeRadioButtonMenuItem;
	}

	private JMenuItem getImportMenuItem() {
		if (importMenuItem == null) {
			importMenuItem = new JMenuItem("Import...");
			importMenuItem.setToolTipText("Import the content in an existing Energy3D file into the clicked location on the land as the center");
			importMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					importFile();
				}
			});
		}
		return importMenuItem;
	}

	private JMenuItem getImportColladaMenuItem() {
		if (importColladaMenuItem == null) {
			importColladaMenuItem = new JMenuItem("Import Collada...");
			importColladaMenuItem.setToolTipText("Import the content in an existing Collada file into the clicked location on the land as the center");
			importColladaMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					importColladaFile();
				}
			});
		}
		return importColladaMenuItem;
	}

	private JMenuItem getExportModelMenuItem() {
		if (exportModelMenuItem == null) {
			exportModelMenuItem = new JMenuItem("Export Scene as 3D Model...");
			exportModelMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					exportObjFile();
				}
			});
		}
		return exportModelMenuItem;
	}

	private JCheckBoxMenuItem getSnapMenuItem() {
		if (snapMenuItem == null) {
			snapMenuItem = new JCheckBoxMenuItem("Snap Walls");
			snapMenuItem.setSelected(true);
			snapMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					HousePart.setSnapToObjects(snapMenuItem.isSelected());
				}
			});
		}
		return snapMenuItem;
	}

	private JCheckBoxMenuItem getSnapToGridsMenuItem() {
		if (snapToGridsMenuItem == null) {
			snapToGridsMenuItem = new JCheckBoxMenuItem("Snap To Grids");
			snapToGridsMenuItem.setSelected(true);
			snapToGridsMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().setSnapToGrids(snapToGridsMenuItem.isSelected());
				}
			});
		}
		return snapToGridsMenuItem;
	}

	public JCheckBoxMenuItem getTopViewCheckBoxMenuItem() {
		if (topViewCheckBoxMenuItem == null) {
			topViewCheckBoxMenuItem = new JCheckBoxMenuItem("2D Top View");
			topViewCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Config.isMac() ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK));
			topViewCheckBoxMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							final TopViewCommand c = new TopViewCommand();
							final boolean isTopView = topViewCheckBoxMenuItem.isSelected();
							if (isTopView) {
								Scene.saveCameraLocation();
								SceneManager.getInstance().resetCamera(ViewMode.TOP_VIEW);
							} else {
								SceneManager.getInstance().resetCamera(ViewMode.NORMAL);
								Scene.loadCameraLocation();
							}
							SceneManager.getInstance().refresh();
							SceneManager.getInstance().getUndoManager().addEdit(c);
							return null;
						}
					});
				}
			});
		}
		return topViewCheckBoxMenuItem;
	}

	private JMenuItem getZoomInMenuItem() {
		if (zoomInMenuItem == null) {
			zoomInMenuItem = new JMenuItem("Zoom In");
			zoomInMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Config.isMac() ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK));
			zoomInMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ZoomCommand c = new ZoomCommand(true);
					SceneManager.getInstance().zoom(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
		}
		return zoomInMenuItem;
	}

	private JMenuItem getZoomOutMenuItem() {
		if (zoomOutMenuItem == null) {
			zoomOutMenuItem = new JMenuItem("Zoom Out");
			zoomOutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Config.isMac() ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK));
			zoomOutMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ZoomCommand c = new ZoomCommand(false);
					SceneManager.getInstance().zoom(false);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
		}
		return zoomOutMenuItem;
	}

	public JRadioButtonMenuItem getNoTextureMenuItem() {
		if (noTextureMenuItem == null) {
			noTextureMenuItem = new JRadioButtonMenuItem("No Texture");
			noTextureMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeTextureCommand c = new ChangeTextureCommand();
					Scene.getInstance().setTextureMode(TextureMode.None);
					Scene.getInstance().setEdited(true);
					if (MainPanel.getInstance().getEnergyViewButton().isSelected()) {
						MainPanel.getInstance().getEnergyViewButton().setSelected(false);
					}
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			textureButtonGroup.add(noTextureMenuItem);
		}
		return noTextureMenuItem;
	}

	public JRadioButtonMenuItem getSimpleTextureMenuItem() {
		if (simpleTextureMenuItem == null) {
			simpleTextureMenuItem = new JRadioButtonMenuItem("Simple Texture");
			simpleTextureMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeTextureCommand c = new ChangeTextureCommand();
					Scene.getInstance().setTextureMode(TextureMode.Simple);
					Scene.getInstance().setEdited(true);
					if (MainPanel.getInstance().getEnergyViewButton().isSelected()) {
						MainPanel.getInstance().getEnergyViewButton().setSelected(false);
					}
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			textureButtonGroup.add(simpleTextureMenuItem);
		}
		return simpleTextureMenuItem;
	}

	public JRadioButtonMenuItem getFullTextureMenuItem() {
		if (fullTextureMenuItem == null) {
			fullTextureMenuItem = new JRadioButtonMenuItem("Full Texture");
			fullTextureMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeTextureCommand c = new ChangeTextureCommand();
					Scene.getInstance().setTextureMode(TextureMode.Full);
					Scene.getInstance().setEdited(true);
					if (MainPanel.getInstance().getEnergyViewButton().isSelected()) {
						MainPanel.getInstance().getEnergyViewButton().setSelected(false);
					}
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			fullTextureMenuItem.setSelected(true);
			textureButtonGroup.add(fullTextureMenuItem);
		}
		return fullTextureMenuItem;
	}

	public JRadioButtonMenuItem getCloudySkyMenuItem() {
		if (cloudySkyMenuItem == null) {
			cloudySkyMenuItem = new JRadioButtonMenuItem("Cloudy Sky");
			cloudySkyMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeThemeCommand c = new ChangeThemeCommand();
					Scene.getInstance().setTheme(Scene.CLOUDY_SKY_THEME);
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			themeButtonGroup.add(cloudySkyMenuItem);
		}
		return cloudySkyMenuItem;
	}

	public JRadioButtonMenuItem getDesertMenuItem() {
		if (desertMenuItem == null) {
			desertMenuItem = new JRadioButtonMenuItem("Desert");
			desertMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeThemeCommand c = new ChangeThemeCommand();
					Scene.getInstance().setTheme(Scene.DESERT_THEME);
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			themeButtonGroup.add(desertMenuItem);
		}
		return desertMenuItem;
	}

	public JRadioButtonMenuItem getGrasslandMenuItem() {
		if (grasslandMenuItem == null) {
			grasslandMenuItem = new JRadioButtonMenuItem("Grassland");
			grasslandMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeThemeCommand c = new ChangeThemeCommand();
					Scene.getInstance().setTheme(Scene.GRASSLAND_THEME);
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			themeButtonGroup.add(grasslandMenuItem);
		}
		return grasslandMenuItem;
	}

	public JRadioButtonMenuItem getForestMenuItem() {
		if (forestMenuItem == null) {
			forestMenuItem = new JRadioButtonMenuItem("Forest");
			forestMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeThemeCommand c = new ChangeThemeCommand();
					Scene.getInstance().setTheme(Scene.FOREST_THEME);
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			themeButtonGroup.add(forestMenuItem);
		}
		return forestMenuItem;
	}

	void showColorDialogForParts() {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		ActionListener colorActionListener;
		if (selectedPart == null) {
			final ReadOnlyColorRGBA color = Scene.getInstance().getLandColor();
			if (color != null) {
				colorChooser.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
			}
			colorActionListener = new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ChangeLandColorCommand cmd = new ChangeLandColorCommand();
					final Color c = colorChooser.getColor();
					final float[] newColor = c.getComponents(null);
					Scene.getInstance().setLandColor(new ColorRGBA(newColor[0], newColor[1], newColor[2], 0.5f));
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(cmd);
				}
			};
		} else {
			if (!noTextureMenuItem.isSelected()) { // when the user wants to set the color, automatically switch to no texture
				if (JOptionPane.showConfirmDialog(this, "To set color for an individual part, we have to remove the texture. Is that OK?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
					return;
				}
				noTextureMenuItem.setSelected(true);
				Scene.getInstance().setTextureMode(TextureMode.None);
			}
			final ReadOnlyColorRGBA color = selectedPart.getColor();
			if (color != null) {
				colorChooser.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
			}
			colorActionListener = new ActionListener() {

				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null) {
						return;
					}
					final Color c = colorChooser.getColor();
					final float[] newColor = c.getComponents(null);
					final boolean restartPrintPreview = Scene.getInstance().getRoofColor().equals(ColorRGBA.WHITE) || c.equals(Color.WHITE);
					final ColorRGBA color = new ColorRGBA(newColor[0], newColor[1], newColor[2], newColor[3]);
					final JPanel panel = new JPanel();
					panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
					panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));

					if (selectedPart instanceof Wall) {

						final JRadioButton rb1 = new JRadioButton("Only this Wall", true);
						final JRadioButton rb2 = new JRadioButton("All Walls Connected to This One (Direct and Indirect)");
						final JRadioButton rb3 = new JRadioButton("All Walls of this Building");
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
						final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
						final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
						final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Wall Color");
						while (true) {
							dialog.setVisible(true);
							final Object choice = optionPane.getValue();
							if (choice == options[1]) {
								break;
							} else {
								if (rb1.isSelected()) { // apply to only this part
									final ChangePartColorCommand cmd = new ChangePartColorCommand(selectedPart);
									selectedPart.setColor(color);
									selectedPart.draw();
									SceneManager.getInstance().refresh();
									SceneManager.getInstance().getUndoManager().addEdit(cmd);
								} else if (rb2.isSelected()) {
									final ChangeColorOfConnectedWallsCommand cmd = new ChangeColorOfConnectedWallsCommand((Wall) selectedPart);
									Scene.getInstance().setColorOfConnectedWalls((Wall) selectedPart, color);
									SceneManager.getInstance().getUndoManager().addEdit(cmd);
								} else if (rb3.isSelected()) {
									final ChangeBuildingColorCommand cmd = new ChangeBuildingColorCommand(selectedPart);
									Scene.getInstance().setPartColorOfBuilding(selectedPart, color);
									SceneManager.getInstance().getUndoManager().addEdit(cmd);
								} else if (rb4.isSelected()) {
									final ChangeColorOfAllPartsOfSameTypeCommand cmd = new ChangeColorOfAllPartsOfSameTypeCommand(selectedPart);
									Scene.getInstance().setColorOfAllPartsOfSameType(selectedPart, color);
									SceneManager.getInstance().getUndoManager().addEdit(cmd);
								}
								Scene.getInstance().setWallColor(color); // remember the color decision for the next wall to be added
								if (choice == options[0]) {
									break;
								}
							}
						}

					} else if (selectedPart instanceof Roof) {

						final JRadioButton rb1 = new JRadioButton("Only this Roof", true);
						final JRadioButton rb2 = new JRadioButton("All Roofs");
						panel.add(rb1);
						panel.add(rb2);
						final ButtonGroup bg = new ButtonGroup();
						bg.add(rb1);
						bg.add(rb2);
						final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
						final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
						final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Roof Color");
						while (true) {
							dialog.setVisible(true);
							final Object choice = optionPane.getValue();
							if (choice == options[1]) {
								break;
							} else {
								if (rb1.isSelected()) { // apply to only this part
									final ChangePartColorCommand cmd = new ChangePartColorCommand(selectedPart);
									selectedPart.setColor(color);
									selectedPart.draw();
									SceneManager.getInstance().refresh();
									SceneManager.getInstance().getUndoManager().addEdit(cmd);
								} else {
									final ChangeColorOfAllPartsOfSameTypeCommand cmd = new ChangeColorOfAllPartsOfSameTypeCommand(selectedPart);
									Scene.getInstance().setColorOfAllPartsOfSameType(selectedPart, color);
									SceneManager.getInstance().getUndoManager().addEdit(cmd);
								}
								Scene.getInstance().setRoofColor(color); // remember the color decision for the next roof to be added
								if (choice == options[0]) {
									break;
								}
							}
						}

					} else if (selectedPart instanceof Foundation) {

						final JRadioButton rb1 = new JRadioButton("Only this Foundation", true);
						final JRadioButton rb2 = new JRadioButton("All Foundations");
						panel.add(rb1);
						panel.add(rb2);
						final ButtonGroup bg = new ButtonGroup();
						bg.add(rb1);
						bg.add(rb2);
						final Object[] options = new Object[] { "OK", "Cancel", "Apply" };
						final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, options, options[2]);
						final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Foundation Color");
						while (true) {
							dialog.setVisible(true);
							final Object choice = optionPane.getValue();
							if (choice == options[1]) {
								break;
							} else {
								if (rb1.isSelected()) { // apply to only this part
									final ChangePartColorCommand cmd = new ChangePartColorCommand(selectedPart);
									selectedPart.setColor(color);
									selectedPart.draw();
									SceneManager.getInstance().refresh();
									SceneManager.getInstance().getUndoManager().addEdit(cmd);
								} else {
									final ChangeColorOfAllPartsOfSameTypeCommand cmd = new ChangeColorOfAllPartsOfSameTypeCommand(selectedPart);
									Scene.getInstance().setColorOfAllPartsOfSameType(selectedPart, color);
									SceneManager.getInstance().getUndoManager().addEdit(cmd);
								}
								Scene.getInstance().setFoundationColor(color); // remember the color decision for the next foundation to be added
								if (choice == options[0]) {
									break;
								}
							}
						}

					} else {
						final ChangePartColorCommand cmd = new ChangePartColorCommand(selectedPart);
						selectedPart.setColor(color);
						selectedPart.draw();
						SceneManager.getInstance().refresh();
						SceneManager.getInstance().getUndoManager().addEdit(cmd);
						if (selectedPart instanceof Door) { // remember the color decision for the next part
							Scene.getInstance().setDoorColor(color);
						} else if (selectedPart instanceof Floor) {
							Scene.getInstance().setFloorColor(color);
						}
					}

					Scene.getInstance().setTextureMode(Scene.getInstance().getTextureMode());
					if (restartPrintPreview && PrintController.getInstance().isPrintPreview()) {
						PrintController.getInstance().restartAnimation();
					}
					MainPanel.getInstance().getEnergyViewButton().setSelected(false);
					Scene.getInstance().setEdited(true);
				}

			};
		}
		JColorChooser.createDialog(this, "Select Color", true, colorChooser, colorActionListener, null).setVisible(true);
	}

	public void open(final String filename) {
		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				try {
					Scene.open(new File(filename).toURI().toURL());
					FileChooser.getInstance().rememberFile(filename);
				} catch (final Throwable e) {
					Util.reportError(e, new File(filename).getAbsolutePath());
				}
				return null;
			}
		});
	}

	private void openModel(final URL url) {
		boolean ok = false;
		if (Scene.getInstance().isEdited()) {
			final int save = JOptionPane.showConfirmDialog(MainFrame.this, "Do you want to save changes?", "Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (save == JOptionPane.YES_OPTION) {
				save();
				if (!Scene.getInstance().isEdited()) {
					ok = true;
				}
			} else if (save != JOptionPane.CANCEL_OPTION) {
				ok = true;
			}
		} else {
			ok = true;
		}
		if (ok) {
			try {
				SceneManager.getTaskManager().update(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						Scene.open(url);
						return null;
					}
				});
			} catch (final Throwable e) {
				Util.reportError(e);
			}
		}
	}

	public void exit() {
		final String[] recentFiles = FileChooser.getInstance().getRecentFiles();
		if (recentFiles != null) {
			final int n = recentFiles.length;
			if (n > 0) {
				final Preferences pref = Preferences.userNodeForPackage(MainApplication.class);
				for (int i = 0; i < n; i++) {
					pref.put("Recent File " + i, recentFiles[n - i - 1]);
				}
			}
		}
		if (Scene.getInstance().isEdited()) {
			final int save = JOptionPane.showConfirmDialog(this, "Do you want to save changes?", "Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (save == JOptionPane.YES_OPTION) {
				save();
				while (Scene.isSaving()) {
					Thread.yield();
				}
				if (!Scene.getInstance().isEdited()) {
					MainApplication.exit();
				}
			} else if (save != JOptionPane.CANCEL_OPTION) {
				MainApplication.exit();
			}
		} else {
			MainApplication.exit();
		}
	}

	private void save() {
		try {
			final URL url = Scene.getURL();
			if (url != null) {
				if (Scene.isInternalFile()) {
					saveFile(true);
				} else {
					Scene.saveOutsideTaskManager(url);
				}
			} else {
				saveFile(true);
			}
			Scene.getInstance().setEdited(false);
		} catch (final Throwable err) {
			err.printStackTrace();
			JOptionPane.showMessageDialog(this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private JMenuItem getExportLogMenuItem() {
		if (exportLogMenuItem == null) {
			exportLogMenuItem = new JMenuItem("Export Log As Zip...");
			exportLogMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final File file = FileChooser.getInstance().showDialog(".zip", zipFilter, true);
					if (file == null) {
						return;
					}
					try {
						new LogZipper(file).createDialog();
					} catch (final Throwable err) {
						err.printStackTrace();
						JOptionPane.showMessageDialog(MainFrame.this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
		}
		return exportLogMenuItem;
	}

	private JMenuItem getCopyImageMenuItem() {
		if (copyImageMenuItem == null) {
			copyImageMenuItem = new JMenuItem("Copy Image");
			copyImageMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, (Config.isMac() ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK) | KeyEvent.ALT_MASK));
			copyImageMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					new ClipImage().copyImageToClipboard(MainPanel.getInstance().getCanvasPanel());
				}
			});
		}
		return copyImageMenuItem;
	}

	private JMenuItem getExportImageMenuItem() {
		if (exportImageMenuItem == null) {
			exportImageMenuItem = new JMenuItem("Export Scene As Image...");
			exportImageMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					exportImage();
				}
			});
		}
		return exportImageMenuItem;
	}

	private void exportImage() {
		System.out.print("Saving snapshot: ");
		final File file = FileChooser.getInstance().showDialog(".png", pngFilter, true);
		if (file == null) {
			return;
		}
		System.out.print(file + "...");
		try {
			final BufferedImage snapShot = Printout.takeSnapShot();
			ImageIO.write(snapShot, "png", file);
			System.out.println("done");
		} catch (final Throwable err) {
			err.printStackTrace();
			JOptionPane.showMessageDialog(this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private JMenuItem getSpecificationsMenuItem() {
		if (specificationsMenuItem == null) {
			specificationsMenuItem = new JMenuItem("Specifications...");
			specificationsMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					new SpecsDialog().setVisible(true);
				}
			});
		}
		return specificationsMenuItem;
	}

	private JMenuItem getOverallUtilityBillMenuItem() {
		if (overallUtilityBillMenuItem == null) {
			overallUtilityBillMenuItem = new JMenuItem("Overall Utility Bill...");
			overallUtilityBillMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					UtilityBill b = Scene.getInstance().getUtilityBill();
					if (b == null) {
						if (JOptionPane.showConfirmDialog(MainFrame.this, "<html>No overall utility bill is found. Create one?<br>(This applies to all the structures in this scene.)</html>", "Overall Utility Bill", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
							return;
						}
						b = new UtilityBill();
						Scene.getInstance().setUtilityBill(b);
					}
					new UtilityBillDialog(b).setVisible(true);
				}
			});
		}
		return overallUtilityBillMenuItem;
	}

	private JMenuItem getSetRegionMenuItem() {
		if (setRegionMenuItem == null) {
			setRegionMenuItem = new JMenuItem("Set Region...");
			setRegionMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					new GlobalMap(MainFrame.this).setVisible(true);
				}
			});
		}
		return setRegionMenuItem;
	}

	private JMenuItem getCustomPricesMenuItem() {
		if (customPricesMenuItem == null) {
			customPricesMenuItem = new JMenuItem("Custom Prices...");
			customPricesMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					new CustomPricesDialog().setVisible(true);
				}
			});
		}
		return customPricesMenuItem;
	}

	private JMenuItem getPropertiesMenuItem() {
		if (propertiesMenuItem == null) {
			propertiesMenuItem = new JMenuItem("Properties...");
			propertiesMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					new PropertiesDialog().setVisible(true);
				}
			});
		}
		return propertiesMenuItem;
	}

	private JCheckBoxMenuItem getNoteCheckBoxMenuItem() {
		if (noteCheckBoxMenuItem == null) {
			noteCheckBoxMenuItem = new JCheckBoxMenuItem("Show Note");
			noteCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke("F11"));
			noteCheckBoxMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					MainPanel.getInstance().setNoteVisible(noteCheckBoxMenuItem.isSelected());
					Util.selectSilently(MainPanel.getInstance().getNoteButton(), noteCheckBoxMenuItem.isSelected());
				}
			});
		}
		return noteCheckBoxMenuItem;
	}

	private JCheckBoxMenuItem getInfoPanelCheckBoxMenuItem() {
		if (infoPanelCheckBoxMenuItem == null) {
			infoPanelCheckBoxMenuItem = new JCheckBoxMenuItem("Show Information Panel", true);
			infoPanelCheckBoxMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					MainPanel.getInstance().setSplitComponentVisible(infoPanelCheckBoxMenuItem.isSelected(), MainPanel.getInstance().getEnergyCanvasNoteSplitPane(), EnergyPanel.getInstance());
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
		}
		return infoPanelCheckBoxMenuItem;
	}

	private JCheckBoxMenuItem getAutoRecomputeEnergyMenuItem() {
		if (autoRecomputeEnergyMenuItem == null) {
			autoRecomputeEnergyMenuItem = new JCheckBoxMenuItem("Automatically Recalculte Energy");
			autoRecomputeEnergyMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					EnergyPanel.setAutoRecomputeEnergy(autoRecomputeEnergyMenuItem.isSelected());
				}
			});
		}
		return autoRecomputeEnergyMenuItem;
	}

	private JMenuItem getRemoveAllRoofsMenuItem() {
		if (removeAllRoofsMenuItem == null) {
			removeAllRoofsMenuItem = new JMenuItem("Remove All Roofs");
			removeAllRoofsMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllRoofs();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
								}
							});
							return null;
						}
					});
				}
			});
		}
		return removeAllRoofsMenuItem;
	}

	private JMenuItem getRemoveAllFloorsMenuItem() {
		if (removeAllFloorsMenuItem == null) {
			removeAllFloorsMenuItem = new JMenuItem("Remove All Floors");
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
								}
							});
							return null;
						}
					});
				}
			});
		}
		return removeAllFloorsMenuItem;
	}

	private JMenuItem getRemoveAllSolarPanelsMenuItem() {
		if (removeAllSolarPanelsMenuItem == null) {
			removeAllSolarPanelsMenuItem = new JMenuItem("Remove All Solar Panels");
			removeAllSolarPanelsMenuItem.addActionListener(new ActionListener() {
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
								}
							});
							return null;
						}
					});
				}
			});
		}
		return removeAllSolarPanelsMenuItem;
	}

	private JMenuItem getRemoveAllRacksMenuItem() {
		if (removeAllRacksMenuItem == null) {
			removeAllRacksMenuItem = new JMenuItem("Remove All Solar Panel Packs");
			removeAllRacksMenuItem.addActionListener(new ActionListener() {
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
								}
							});
							return null;
						}
					});
				}
			});
		}
		return removeAllRacksMenuItem;
	}

	private JMenuItem getRemoveAllMirrorsMenuItem() {
		if (removeAllMirrorsMenuItem == null) {
			removeAllMirrorsMenuItem = new JMenuItem("Remove All Mirrors");
			removeAllMirrorsMenuItem.addActionListener(new ActionListener() {
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
								}
							});
							return null;
						}
					});
				}
			});
		}
		return removeAllMirrorsMenuItem;
	}

	private JMenuItem getRemoveAllParabolicTroughsMenuItem() {
		if (removeAllParabolicTroughsMenuItem == null) {
			removeAllParabolicTroughsMenuItem = new JMenuItem("Remove All Parabolic Troughs");
			removeAllParabolicTroughsMenuItem.addActionListener(new ActionListener() {
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
								}
							});
							return null;
						}
					});
				}
			});
		}
		return removeAllParabolicTroughsMenuItem;
	}

	private JMenuItem getRemoveAllParabolicDishesMenuItem() {
		if (removeAllParabolicDishesMenuItem == null) {
			removeAllParabolicDishesMenuItem = new JMenuItem("Remove All Parabolic Dishes");
			removeAllParabolicDishesMenuItem.addActionListener(new ActionListener() {
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
								}
							});
							return null;
						}
					});
				}
			});
		}
		return removeAllParabolicDishesMenuItem;
	}

	private JMenuItem getRemoveAllFresnelReflectorsMenuItem() {
		if (removeAllFresnelReflectorsMenuItem == null) {
			removeAllFresnelReflectorsMenuItem = new JMenuItem("Remove All Fresnel Reflectors");
			removeAllFresnelReflectorsMenuItem.addActionListener(new ActionListener() {
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
								}
							});
							return null;
						}
					});
				}
			});
		}
		return removeAllFresnelReflectorsMenuItem;
	}

	private JMenuItem getRemoveAllSensorsMenuItem() {
		if (removeAllSensorsMenuItem == null) {
			removeAllSensorsMenuItem = new JMenuItem("Remove All Sensors");
			removeAllSensorsMenuItem.addActionListener(new ActionListener() {
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
								}
							});
							return null;
						}
					});
				}
			});
		}
		return removeAllSensorsMenuItem;
	}

	private JMenuItem getRemoveAllWallsMenuItem() {
		if (removeAllWallsMenuItem == null) {
			removeAllWallsMenuItem = new JMenuItem("Remove All Walls");
			removeAllWallsMenuItem.addActionListener(new ActionListener() {
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
								}
							});
							return null;
						}
					});
				}
			});
		}
		return removeAllWallsMenuItem;
	}

	private JMenuItem getRemoveAllWindowsMenuItem() {
		if (removeAllWindowsMenuItem == null) {
			removeAllWindowsMenuItem = new JMenuItem("Remove All Windows");
			removeAllWindowsMenuItem.addActionListener(new ActionListener() {
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
								}
							});
							return null;
						}
					});
				}
			});
		}
		return removeAllWindowsMenuItem;
	}

	private JMenuItem getRemoveAllWindowShuttersMenuItem() {
		if (removeAllWindowShuttersMenuItem == null) {
			removeAllWindowShuttersMenuItem = new JMenuItem("Remove All Window Shutters");
			removeAllWindowShuttersMenuItem.addActionListener(new ActionListener() {
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
								}
							});
							return null;
						}
					});
				}
			});
		}
		return removeAllWindowShuttersMenuItem;
	}

	private JMenuItem getRemoveAllFoundationsMenuItem() {
		if (removeAllFoundationsMenuItem == null) {
			removeAllFoundationsMenuItem = new JMenuItem("Remove All Foundations");
			removeAllFoundationsMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllFoundations();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
								}
							});
							return null;
						}
					});
				}
			});
		}
		return removeAllFoundationsMenuItem;
	}

	private JMenuItem getRemoveAllTreesMenuItem() {
		if (removeAllTreesMenuItem == null) {
			removeAllTreesMenuItem = new JMenuItem("Remove All Trees");
			removeAllTreesMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllTrees();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									MainPanel.getInstance().getEnergyViewButton().setSelected(false);
								}
							});
							return null;
						}
					});
				}
			});
		}
		return removeAllTreesMenuItem;
	}

	private JMenuItem getRemoveAllHumansMenuItem() {
		if (removeAllHumansMenuItem == null) {
			removeAllHumansMenuItem = new JMenuItem("Remove All Humans");
			removeAllHumansMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().removeAllHumans();
							return null;
						}
					});
				}
			});
		}
		return removeAllHumansMenuItem;
	}

	private JMenuItem getRemoveAllUtilityBillsMenuItem() {
		if (removeAllUtilityBillsMenuItem == null) {
			removeAllUtilityBillsMenuItem = new JMenuItem("Remove All Utility Bills");
			removeAllUtilityBillsMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ArrayList<Foundation> list = new ArrayList<Foundation>();
					for (final HousePart p : Scene.getInstance().getParts()) {
						if (p instanceof Foundation && ((Foundation) p).getUtilityBill() != null) {
							list.add((Foundation) p);
						}
					}
					if (list.isEmpty()) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no utilitiy bill to remove.", "No Utility Bill", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + list.size() + " utility bills associated with buildings in this scene?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
						for (final Foundation f : list) {
							f.setUtilityBill(null);
						}
					}
					Scene.getInstance().setEdited(true);
				}
			});
		}
		return removeAllUtilityBillsMenuItem;
	}

	private JMenuItem getRotate180MenuItem() {
		if (rotate180MenuItem == null) {
			rotate180MenuItem = new JMenuItem("180\u00B0");
			rotate180MenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().rotate(Math.PI);
				}
			});
		}
		return rotate180MenuItem;
	}

	private JMenuItem getRotate90CwMenuItem() {
		if (rotate90CwMenuItem == null) {
			rotate90CwMenuItem = new JMenuItem("90\u00B0 Clockwise");
			rotate90CwMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().rotate(-Math.PI / 2);
				}
			});
		}
		return rotate90CwMenuItem;
	}

	private JMenuItem getRotate90CcwMenuItem() {
		if (rotate90CcwMenuItem == null) {
			rotate90CcwMenuItem = new JMenuItem("90\u00B0 Counter Clockwise");
			rotate90CcwMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().rotate(Math.PI / 2);
				}
			});
		}
		return rotate90CcwMenuItem;
	}

	private JMenuItem getMoveEastMenuItem() {
		if (moveEastMenuItem == null) {
			moveEastMenuItem = new JMenuItem("Move East (or Press 'E' Key)");
			moveEastMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (MainPanel.getInstance().getNoteTextArea().hasFocus()) {
						return;
					}
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							SceneManager.getInstance().move(new Vector3(1, 0, 0));
							return null;
						}
					});
				}
			});
		}
		return moveEastMenuItem;
	}

	private JMenuItem getMoveWestMenuItem() {
		if (moveWestMenuItem == null) {
			moveWestMenuItem = new JMenuItem("Move West (or Press 'W' Key)");
			moveWestMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (MainPanel.getInstance().getNoteTextArea().hasFocus()) {
						return;
					}
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							SceneManager.getInstance().move(new Vector3(-1, 0, 0));
							return null;
						}
					});
				}
			});
		}
		return moveWestMenuItem;
	}

	private JMenuItem getMoveSouthMenuItem() {
		if (moveSouthMenuItem == null) {
			moveSouthMenuItem = new JMenuItem("Move South (or Press 'S' Key)");
			moveSouthMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (MainPanel.getInstance().getNoteTextArea().hasFocus()) {
						return;
					}
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							SceneManager.getInstance().move(new Vector3(0, -1, 0));
							return null;
						}
					});
				}
			});
		}
		return moveSouthMenuItem;
	}

	private JMenuItem getMoveNorthMenuItem() {
		if (moveNorthMenuItem == null) {
			moveNorthMenuItem = new JMenuItem("Move North (or Press 'N' Key)");
			moveNorthMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (MainPanel.getInstance().getNoteTextArea().hasFocus()) {
						return;
					}
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							SceneManager.getInstance().move(new Vector3(0, 1, 0));
							return null;
						}
					});
				}
			});
		}
		return moveNorthMenuItem;
	}

	private JMenuItem getFixProblemsMenuItem() {
		if (fixProblemsMenuItem == null) {
			fixProblemsMenuItem = new JMenuItem("Fix Problems");
			fixProblemsMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().fixProblems(true);
							return null;
						}
					});
				}
			});
		}
		return fixProblemsMenuItem;
	}

	private JMenuItem getRemoveAllLocksMenuItem() {
		if (removeAllLocksMenuItem == null) {
			removeAllLocksMenuItem = new JMenuItem("Remove All Locks");
			removeAllLocksMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().lockAll(false);
							return null;
						}
					});
				}
			});
		}
		return removeAllLocksMenuItem;
	}

	private JMenuItem getEnableAllEditPointsMenuItem() {
		if (enableAllEditPointsMenuItem == null) {
			enableAllEditPointsMenuItem = new JMenuItem("Enable All Edit Points");
			enableAllEditPointsMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final List<Foundation> foundations = Scene.getInstance().getAllFoundations();
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							for (final Foundation f : foundations) {
								f.setLockEdit(false);
							}
							return null;
						}
					});
					Scene.getInstance().setEdited(true);
				}
			});
		}
		return enableAllEditPointsMenuItem;
	}

	private JMenuItem getDisableAllEditPointsMenuItem() {
		if (disableAllEditPointsMenuItem == null) {
			disableAllEditPointsMenuItem = new JMenuItem("Disable All Edit Points");
			disableAllEditPointsMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final List<Foundation> foundations = Scene.getInstance().getAllFoundations();
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							for (final Foundation f : foundations) {
								f.setLockEdit(true);
							}
							return null;
						}
					});
					Scene.getInstance().setEdited(true);
				}
			});
		}
		return disableAllEditPointsMenuItem;
	}

	public JColorChooser getColorChooser() {
		return colorChooser;
	}

}
