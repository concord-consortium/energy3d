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
import javax.swing.JFileChooser;
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

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.logger.DesignReplay;
import org.concord.energy3d.logger.PlayControl;
import org.concord.energy3d.logger.PostProcessor;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.PartGroup;
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
import org.concord.energy3d.simulation.AnnualEnvironmentalTemperature;
import org.concord.energy3d.simulation.AnnualSensorData;
import org.concord.energy3d.simulation.Cost;
import org.concord.energy3d.simulation.DailyEnvironmentalTemperature;
import org.concord.energy3d.simulation.DailySensorData;
import org.concord.energy3d.simulation.EnergyAngularAnalysis;
import org.concord.energy3d.simulation.EnergyAnnualAnalysis;
import org.concord.energy3d.simulation.EnergyDailyAnalysis;
import org.concord.energy3d.simulation.GroupAnnualAnalysis;
import org.concord.energy3d.simulation.GroupDailyAnalysis;
import org.concord.energy3d.simulation.SolarAnnualAnalysis;
import org.concord.energy3d.simulation.SolarDailyAnalysis;
import org.concord.energy3d.simulation.UtilityBill;
import org.concord.energy3d.undo.ChangeBuildingColorCommand;
import org.concord.energy3d.undo.ChangeLandColorCommand;
import org.concord.energy3d.undo.ChangePartColorCommand;
import org.concord.energy3d.undo.ChangeTextureCommand;
import org.concord.energy3d.undo.ChangeThemeCommand;
import org.concord.energy3d.undo.ShowAxesCommand;
import org.concord.energy3d.undo.ShowHeatFluxCommand;
import org.concord.energy3d.undo.ShowShadowCommand;
import org.concord.energy3d.undo.TopViewCommand;
import org.concord.energy3d.undo.ZoomCommand;
import org.concord.energy3d.util.ClipImage;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.FileChooser;
import org.concord.energy3d.util.Printout;
import org.concord.energy3d.util.Util;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final MainFrame instance = new MainFrame();
	private JMenuBar appMenuBar;
	private JMenu fileMenu;
	private int fileMenuItemCount;
	private final List<JComponent> recentFileMenuItems = new ArrayList<JComponent>();
	private JMenuItem newMenuItem;
	private JMenuItem openMenuItem;
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
	private JMenuItem annualEnergyAnalysisMenuItem;
	private JMenuItem annualEnergyAnalysisForSelectionMenuItem;
	private JMenuItem dailyEnergyAnalysisMenuItem;
	private JMenuItem dailyEnergyAnalysisForSelectionMenuItem;
	private JMenuItem groupDailyAnalysisMenuItem;
	private JMenuItem groupAnnualAnalysisMenuItem;
	private JMenuItem annualSolarAnalysisMenuItem;
	private JMenuItem dailySolarAnalysisMenuItem;
	private JMenuItem annualSensorMenuItem;
	private JMenuItem dailySensorMenuItem;
	private JMenuItem orientationalEnergyAnalysisMenuItem;
	private JMenuItem constructionCostAnalysisMenuItem;
	private JMenuItem annualEnvironmentalTemperatureMenuItem;
	private JMenuItem dailyEnvironmentalTemperatureMenuItem;
	private JCheckBoxMenuItem solarRadiationHeatMapMenuItem;
	private JCheckBoxMenuItem solarAbsorptionHeatMapMenuItem;
	private JCheckBoxMenuItem showHeatFluxVectorsMenuItem;
	private JCheckBoxMenuItem axesMenuItem;
	private JCheckBoxMenuItem shadowMenuItem;
	private JCheckBoxMenuItem roofDashedLineMenuItem;
	private JCheckBoxMenuItem buildingLabelsMenuItem;
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
	private JMenuItem importMenuItem;
	private JCheckBoxMenuItem snapMenuItem;
	private JCheckBoxMenuItem gridsMenuItem;
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
	private JMenuItem importColladaMenuItem;
	private JMenuItem exportImageMenuItem;
	private JMenuItem copyImageMenuItem;
	private JMenuItem exportLogMenuItem;
	private JMenuItem lockAllMenuItem;
	private JMenuItem specificationsMenuItem;
	private JMenuItem propertiesMenuItem;
	private JCheckBoxMenuItem noteCheckBoxMenuItem;
	private JCheckBoxMenuItem infoPanelCheckBoxMenuItem;
	private JMenu templatesMenu;
	private JMenu otherTemplatesMenu;

	private final FileChooser fileChooser;
	private final JColorChooser colorChooser;
	private final ExtensionFileFilter ng3Filter = new ExtensionFileFilter("Energy3D Project (*.ng3)", "ng3");
	private final ExtensionFileFilter pngFilter = new ExtensionFileFilter("Image (*.png)", "png");
	private final ExtensionFileFilter daeFilter = new ExtensionFileFilter("Collada (*.dae)", "dae");
	private final ExtensionFileFilter zipFilter = new ExtensionFileFilter("Zip (*.zip)", "zip");
	private JCheckBoxMenuItem autoRecomputeEnergyMenuItem;
	private JMenuItem removeAllRoofsMenuItem;
	private JMenuItem removeAllFloorsMenuItem;
	private JMenuItem removeAllSolarPanelsMenuItem;
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
		final Preferences pref = Preferences.userNodeForPackage(MainApplication.class);
		final String directoryPath = pref.get("dir", null);
		fileChooser = new FileChooser(directoryPath);
		if (!Config.isWebStart() && directoryPath == null)
			fileChooser.setCurrentDirectory(new File(System.getProperties().getProperty("user.dir")));
		fileChooser.addRecentFile(pref.get("Recent File 0", null));
		fileChooser.addRecentFile(pref.get("Recent File 1", null));
		fileChooser.addRecentFile(pref.get("Recent File 2", null));
		fileChooser.addRecentFile(pref.get("Recent File 3", null));

		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(ng3Filter);
		fileChooser.setFileFilter(ng3Filter);
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
		if ((windowState & JFrame.ICONIFIED) == 0)
			setExtendedState(windowState);

		if (Config.isMac())
			Mac.init();

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
			appMenuBar.add(getHelpMenu());

			addCommonActionListeners(appMenuBar);
		}
		return appMenuBar;
	}

	private void addCommonActionListeners(final JMenuBar menuBar) {
		for (final Component c : menuBar.getComponents())
			if (c instanceof JMenu)
				addCommonActionListeners((JMenu) c);
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
					importColladaMenuItem.setEnabled(b);
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
					saveMenuItem.setEnabled(!Scene.isTemplate()); // cannot overwrite a template

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
						for (final JComponent x : recentFileMenuItems)
							fileMenu.remove(x);
					}
					final String[] recentFiles = fileChooser.getRecentFiles();
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
												if (!Scene.getInstance().isEdited())
													ok = true;
											} else if (save != JOptionPane.CANCEL_OPTION)
												ok = true;
										} else
											ok = true;
										if (ok)
											SceneManager.getTaskManager().update(new Callable<Object>() {
												@Override
												public Object call() {
													try {
														Scene.open(rf.toURI().toURL());
														updateTitleBar();
														fileChooser.rememberFile(rf.getPath());
													} catch (final Throwable err) {
														Util.reportError(err);
													}
													return null;
												}
											});
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
			addItemToFileMenu(new JSeparator());
			addItemToFileMenu(getCopyImageMenuItem());
			addItemToFileMenu(getExportImageMenuItem());
			addItemToFileMenu(getExportLogMenuItem());
			addItemToFileMenu(getImportMenuItem());
			addItemToFileMenu(getImportColladaMenuItem());
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
							if (!Scene.getInstance().isEdited())
								ok = true;
						} else if (save != JOptionPane.CANCEL_OPTION)
							ok = true;
					} else
						ok = true;
					if (ok) {
						new Thread() {
							@Override
							public void run() {
								try {
									Scene.newFile();
									SceneManager.getInstance().resetCamera(ViewMode.NORMAL);
									SceneManager.getInstance().getCameraControl().reset();
									updateTitleBar();
								} catch (final Throwable err) {
									Util.reportError(err);
								}
							}
						}.start();
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
							if (!Scene.getInstance().isEdited())
								open();
						} else if (save != JOptionPane.CANCEL_OPTION)
							open();
					} else
						open();
				}
			});
		}
		return openMenuItem;
	}

	public void open() {
		SceneManager.getInstance().refresh(1);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.addChoosableFileFilter(ng3Filter);
		fileChooser.removeChoosableFileFilter(pngFilter);
		fileChooser.removeChoosableFileFilter(daeFilter);
		fileChooser.removeChoosableFileFilter(zipFilter);
		fileChooser.setFileFilter(ng3Filter);
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			SceneManager.getInstance().resetCamera(ViewMode.NORMAL);
			Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getSelectedFile().getParent());
			final File file;
			if (!fileChooser.getSelectedFile().getName().toLowerCase().endsWith(".ng3"))
				file = new File(fileChooser.getSelectedFile().toString() + ".ng3");
			else
				file = fileChooser.getSelectedFile();
			SceneManager.getTaskManager().update(new Callable<Object>() {
				@Override
				public Object call() {
					try {
						Scene.open(file.toURI().toURL());
						fileChooser.rememberFile(file.getPath());
					} catch (final Throwable err) {
						Util.reportError(err);
					}
					return null;
				}
			});
			topViewCheckBoxMenuItem.setSelected(false);
		}
	}

	private JMenuItem getAnalyzeFolderMenuItem() {
		if (analyzeFolderMenuItem == null) {
			analyzeFolderMenuItem = new JMenuItem("Analyze Folder...");
			analyzeFolderMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(MainFrame.this, "This feature is for researchers only. Are you sure you want to continue?", "Research Mode", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE))
						return;
					SceneManager.getInstance().refresh(1);
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					fileChooser.removeChoosableFileFilter(ng3Filter);
					fileChooser.removeChoosableFileFilter(pngFilter);
					fileChooser.removeChoosableFileFilter(daeFilter);
					fileChooser.removeChoosableFileFilter(zipFilter);
					if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
						Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getSelectedFile().getParent());
						final File dir = fileChooser.getSelectedFile();
						if (dir.isDirectory()) {
							PostProcessor.getInstance().analyze(dir.listFiles(ng3NameFilter), new File(fileChooser.getCurrentDirectory() + System.getProperty("file.separator") + "prop.txt"), new Runnable() {
								@Override
								public void run() {
									updateTitleBar();
								}
							});
						}
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
					if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(MainFrame.this, "This feature is for researchers only. Are you sure you want to continue?", "Research Mode", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE))
						return;
					SceneManager.getInstance().refresh(1);
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					fileChooser.removeChoosableFileFilter(ng3Filter);
					fileChooser.removeChoosableFileFilter(pngFilter);
					fileChooser.removeChoosableFileFilter(daeFilter);
					fileChooser.removeChoosableFileFilter(zipFilter);
					if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
						Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getSelectedFile().getParent());
						final File dir = fileChooser.getSelectedFile();
						if (dir.isDirectory()) {
							DesignReplay.getInstance().play(dir.listFiles(ng3NameFilter));
						}
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
					if (DesignReplay.getInstance().getLastFolder() != null)
						DesignReplay.getInstance().play(DesignReplay.getInstance().getLastFolder().listFiles(ng3NameFilter));
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
			if (Scene.isTemplate()) {
				String s = Scene.getURL().toString();
				setTitle("Energy3D V" + MainApplication.VERSION + " - TEMPLATE: " + s.substring(s.lastIndexOf("/") + 1).replaceAll("%20", " ") + star);
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
						new Thread() {
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
					} else
						PrintController.getInstance().print();
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
			helpMenu = new JMenu();
			helpMenu.setText("Help");
			JMenuItem mi = new JMenuItem("Download PDF User's Guide...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Util.openBrowser("http://energy.concord.org/energy3d/Energy3D-Guide.pdf");
				}
			});
			helpMenu.add(mi);
			mi = new JMenuItem("Visit Home Page...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Util.openBrowser("http://energy.concord.org/energy3d");
				}
			});
			helpMenu.add(mi);
			mi = new JMenuItem("View Examples...");
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
			mi = new JMenuItem("Contact Us...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Util.openBrowser("http://energy.concord.org/energy3d/contact.html");
				}
			});
			helpMenu.add(mi);
			if (!Config.isMac())
				helpMenu.add(getAboutMenuItem());
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
			p.add(new JLabel("<html><h1>Energy3D</h1><h3><i>Learning to build a sustainable future</i></h3><br>Version: " + MainApplication.VERSION + ", Copyright 2011-" + Calendar.getInstance().get(Calendar.YEAR) + "<br>The Intelligent Learning Technology Laboratory, Concord Consortium<hr><h3>Credit:</h3>This program is brought to you by:<ul><li>Dr. Saeid Nourian, developer of 3D user interface and graphics<li>Dr. Charles Xie, developer of simulation and analysis engines</ul><p>This program is based on Ardor3D and JOGL and provided to you under the MIT License.<br>Funding of this program is provided by the National Science Foundation through grants<br>0918449, 1304485, 1348530, 1503196, and 1512868.</html>"), BorderLayout.CENTER);
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
					saveFile();
				}
			});
		}
		return saveasMenuItem;
	}

	private JMenu getAnalysisMenu() {
		if (analysisMenu == null) {
			analysisMenu = new JMenu("Analysis");
			analysisMenu.addMenuListener(new MenuListener() {

				private void enableEnergyAnalysis(boolean b) {
					annualEnergyAnalysisMenuItem.setEnabled(b);
					annualEnergyAnalysisForSelectionMenuItem.setEnabled(b);
					dailyEnergyAnalysisMenuItem.setEnabled(b);
					dailyEnergyAnalysisForSelectionMenuItem.setEnabled(b);
					orientationalEnergyAnalysisMenuItem.setEnabled(b);
				}

				@Override
				public void menuCanceled(final MenuEvent e) {
					simulationSettingsMenuItem.setEnabled(true);
					enableEnergyAnalysis(true);
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					SceneManager.getInstance().refresh();
					simulationSettingsMenuItem.setEnabled(true);
					enableEnergyAnalysis(true);
				}

				@Override
				public void menuSelected(final MenuEvent e) {
					MainPanel.getInstance().defaultTool();
					simulationSettingsMenuItem.setEnabled(!Scene.getInstance().isStudentMode());
					enableEnergyAnalysis(!Scene.getInstance().getOnlySolarAnalysis());
				}
			});
			analysisMenu.add(getAnnualEnergyAnalysisMenuItem());
			analysisMenu.add(getAnnualEnergyAnalysisForSelectionMenuItem());
			analysisMenu.add(getDailyEnergyAnalysisMenuItem());
			analysisMenu.add(getDailyEnergyAnalysisForSelectionMenuItem());
			analysisMenu.addSeparator();
			analysisMenu.add(getAnnualSolarAnalysisMenuItem());
			analysisMenu.add(getDailySolarAnalysisMenuItem());
			analysisMenu.addSeparator();
			analysisMenu.add(getGroupAnnualAnalysisMenuItem());
			analysisMenu.add(getGroupDailyAnalysisMenuItem());
			analysisMenu.addSeparator();
			analysisMenu.add(getConstructionCostAnalysisMenuItem());
			analysisMenu.add(getAnnualEnvironmentalTemperatureMenuItem());
			analysisMenu.add(getDailyEnvironmentalTemperatureMenuItem());
			analysisMenu.add(getAnnualSensorMenuItem());
			analysisMenu.add(getDailySensorMenuItem());
			analysisMenu.add(getOrientationalEnergyAnalysisMenuItem());
			analysisMenu.addSeparator();
			analysisMenu.add(getSimulationSettingsMenuItem());
		}
		return analysisMenu;
	}

	private JMenu getTemplatesMenu() {
		if (templatesMenu == null) {
			templatesMenu = new JMenu("Templates");
			templatesMenu.addMenuListener(new MenuListener() {
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
			addTemplate(templatesMenu, "Apartment 1", "templates/apartment-template-1.ng3");
			addTemplate(templatesMenu, "Apartment 2", "templates/apartment-template-2.ng3");
			addTemplate(templatesMenu, "Box Gabled Roof", "templates/box-gabled-template.ng3");
			addTemplate(templatesMenu, "Bungalow", "templates/bungalow-template.ng3");
			addTemplate(templatesMenu, "Butterfly Roof", "templates/butterfly-template.ng3");
			addTemplate(templatesMenu, "Cape Cod", "templates/cape-cod-template.ng3");
			addTemplate(templatesMenu, "Colonial", "templates/colonial-template.ng3");
			addTemplate(templatesMenu, "Combination Roof", "templates/combination-roof-template.ng3");
			addTemplate(templatesMenu, "Cross Gabled Roof", "templates/cross-gabled-template.ng3");
			addTemplate(templatesMenu, "Cross Hipped Roof", "templates/cross-hipped-template.ng3");
			addTemplate(templatesMenu, "Dutch Colonial", "templates/gambrel-template.ng3");
			addTemplate(templatesMenu, "Flat Roof", "templates/flat-roof-template.ng3");
			addTemplate(templatesMenu, "Gable & Valley Roof", "templates/gable-valley-template.ng3");
			addTemplate(templatesMenu, "Gablet Roof", "templates/gablet-template.ng3");
			addTemplate(templatesMenu, "Hip Roof", "templates/hip-roof-template.ng3");
			addTemplate(templatesMenu, "Hip & Valley Roof", "templates/hip-valley-template.ng3");
			addTemplate(templatesMenu, "M-Shaped Roof", "templates/m-shaped-template.ng3");
			addTemplate(templatesMenu, "Mansard", "templates/mansard-template.ng3");
			addTemplate(templatesMenu, "Saltbox 1", "templates/saltbox-template-1.ng3");
			addTemplate(templatesMenu, "Saltbox 2", "templates/saltbox-template-2.ng3");
			addTemplate(templatesMenu, "Shed Roof", "templates/shed-roof-template.ng3");
			templatesMenu.addSeparator();
			otherTemplatesMenu = new JMenu("Others");
			templatesMenu.add(otherTemplatesMenu);
			addTemplate(otherTemplatesMenu, "Dome", "templates/dome-template.ng3");
			addTemplate(otherTemplatesMenu, "Egyptian Pyramid", "templates/egyptian-pyramid-template.ng3");
			addTemplate(otherTemplatesMenu, "Mayan Pyramid", "templates/mayan-pyramid-template.ng3");
			addTemplate(otherTemplatesMenu, "Stadium", "templates/stadium-template.ng3");
		}
		return templatesMenu;
	}

	private void addTemplate(final JMenu menu, final String type, final String url) {
		JMenuItem mi = new JMenuItem(type);
		mi.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openTemplate(MainApplication.class.getResource(url));
			}
		});
		menu.add(mi);
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
					Util.selectSilently(solarRadiationHeatMapMenuItem, SceneManager.getInstance().getSolarHeatMap());
					Util.selectSilently(solarAbsorptionHeatMapMenuItem, Scene.getInstance().getOnlyAbsorptionInSolarMap());
					Util.selectSilently(showHeatFluxVectorsMenuItem, Scene.getInstance().getAlwaysComputeHeatFluxVectors());
					Util.selectSilently(shadowMenuItem, SceneManager.getInstance().isShadowEnabled());
					Util.selectSilently(axesMenuItem, SceneManager.getInstance().areAxesVisible());
					Util.selectSilently(buildingLabelsMenuItem, SceneManager.getInstance().areBuildingLabelsVisible());
					Util.selectSilently(roofDashedLineMenuItem, Scene.getInstance().areDashedLinesOnRoofShown());
					MainPanel.getInstance().defaultTool();
				}
			});

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
			viewMenu.add(getTextureMenu());
			viewMenu.add(getThemeMenu());
			viewMenu.addSeparator();
			viewMenu.add(getSolarRadiationHeatMapMenuItem());
			viewMenu.add(getSolarAbsorptionHeatMapMenuItem());
			viewMenu.add(getHeatFluxMenuItem());
			viewMenu.add(getAxesMenuItem());
			viewMenu.add(getShadowMenuItem());
			viewMenu.addSeparator();
			viewMenu.add(getRoofDashedLineMenuItem());
			viewMenu.add(getBuildingLabelsMenuItem());
			viewMenu.add(getAnnotationsInwardMenuItem());
			// viewMenu.add(getWallThicknessMenuItem());
			viewMenu.addSeparator();
			viewMenu.add(getInfoPanelCheckBoxMenuItem());
			viewMenu.add(getNoteCheckBoxMenuItem());

		}
		return viewMenu;
	}

	public JMenu getTextureMenu() {

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

	public JMenu getThemeMenu() {

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
					Util.selectSilently(cloudySkyMenuItem, Scene.getInstance().getTheme() == Scene.CLOUDY_SKY);
					Util.selectSilently(desertMenuItem, Scene.getInstance().getTheme() == Scene.DESERT);
					Util.selectSilently(grasslandMenuItem, Scene.getInstance().getTheme() == Scene.GRASSLAND);
					Util.selectSilently(forestMenuItem, Scene.getInstance().getTheme() == Scene.FOREST);
				}
			});

			themeMenu.add(getCloudySkyMenuItem());
			themeMenu.add(getDesertMenuItem());
			themeMenu.add(getGrasslandMenuItem());
			themeMenu.add(getForestMenuItem());

		}
		return themeMenu;

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

	private JCheckBoxMenuItem getBuildingLabelsMenuItem() {
		if (buildingLabelsMenuItem == null) {
			buildingLabelsMenuItem = new JCheckBoxMenuItem("Building Labels", false);
			buildingLabelsMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					SceneManager.getInstance().setBuildingLabelsVisible(buildingLabelsMenuItem.isSelected());
					Scene.getInstance().setEdited(true);
				}
			});
		}
		return buildingLabelsMenuItem;
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
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
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
			simulationSettingsMenuItem = new JMenuItem("Change Settings...");
			simulationSettingsMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					new SimulationSettingsDialog().setVisible(true);
				}
			});
		}
		return simulationSettingsMenuItem;
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
					if (SceneManager.getInstance().autoSelectBuilding(true) instanceof Foundation)
						new EnergyAnnualAnalysis().show("Annual Energy");
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
					if (selectedPart instanceof Window || selectedPart instanceof Wall || selectedPart instanceof Roof || selectedPart instanceof Door || selectedPart instanceof SolarPanel || selectedPart instanceof Foundation) {
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
						if (SceneManager.getInstance().getSolarHeatMap())
							analysis.updateGraph();
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
					if (selectedPart instanceof Window || selectedPart instanceof Wall || selectedPart instanceof Roof || selectedPart instanceof Door || selectedPart instanceof SolarPanel || selectedPart instanceof Foundation) {
						new EnergyDailyAnalysis().show("Daily Energy for Selected Part");
					} else {
						JOptionPane.showMessageDialog(MainFrame.this, "You must select a building part first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});
		}
		return dailyEnergyAnalysisForSelectionMenuItem;
	}

	private JMenuItem getAnnualSolarAnalysisMenuItem() {
		if (annualSolarAnalysisMenuItem == null) {
			annualSolarAnalysisMenuItem = new JMenuItem("Annual Yield Analysis of Solar Panels...");
			annualSolarAnalysisMenuItem.setAccelerator(KeyStroke.getKeyStroke("F4"));
			annualSolarAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					int n = Scene.getInstance().getNumberOfSolarPanels();
					if (n <= 0) {
						JOptionPane.showMessageDialog(MainFrame.this, "There is no solar panel to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					final SolarAnnualAnalysis a = new SolarAnnualAnalysis();
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart != null) {
						Foundation foundation;
						if (selectedPart instanceof Foundation) {
							foundation = (Foundation) selectedPart;
						} else {
							foundation = selectedPart.getTopContainer();
						}
						if (foundation != null) {
							n = foundation.countParts(SolarPanel.class);
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
		return annualSolarAnalysisMenuItem;
	}

	private JMenuItem getDailySolarAnalysisMenuItem() {
		if (dailySolarAnalysisMenuItem == null) {
			dailySolarAnalysisMenuItem = new JMenuItem("Daily Yield Analysis of Solar Panels...");
			dailySolarAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					int n = Scene.getInstance().getNumberOfSolarPanels();
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
							n = foundation.countParts(SolarPanel.class);
							if (n <= 0) {
								JOptionPane.showMessageDialog(MainFrame.this, "There is no solar panel on this building to analyze.", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}
					final SolarDailyAnalysis a = new SolarDailyAnalysis();
					if (SceneManager.getInstance().getSolarHeatMap())
						a.updateGraph();
					a.show();
				}
			});
		}
		return dailySolarAnalysisMenuItem;
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
					PartGroup g = selectGroup();
					if (g != null) {
						GroupDailyAnalysis a = new GroupDailyAnalysis(g.getIds());
						a.show(g.getType() + " " + g.getIds());
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
					PartGroup g = selectGroup();
					if (g != null) {
						GroupAnnualAnalysis a = new GroupAnnualAnalysis(g.getIds());
						a.show(g.getType() + " " + g.getIds());
					}
					SceneManager.getInstance().hideAllEditPoints();
				}
			});
		}
		return groupAnnualAnalysisMenuItem;
	}

	private ArrayList<Long> getIdArray(Class<?> c) {
		ArrayList<Long> idArray = new ArrayList<Long>();
		for (HousePart p : Scene.getInstance().getParts()) {
			if (c.isInstance(p)) {
				idArray.add(p.getId());
			}
		}
		Collections.sort(idArray);
		return idArray;
	}

	private PartGroup selectGroup() {
		JPanel gui = new JPanel(new BorderLayout(5, 5));
		gui.setBorder(BorderFactory.createTitledBorder("Types and IDs"));
		final DefaultListModel<Long> idListModel = new DefaultListModel<Long>();
		final JComboBox<String> typeComboBox = new JComboBox<String>(new String[] { "Wall", "Window", "Roof", "Solar Panel" });
		typeComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				idListModel.clear();
				String type = (String) typeComboBox.getSelectedItem();
				Class<?> c = null;
				if ("Wall".equals(type)) {
					c = Wall.class;
				} else if ("Window".equals(type)) {
					c = Window.class;
				} else if ("Roof".equals(type)) {
					c = Roof.class;
				} else if ("Solar Panel".equals(type)) {
					c = SolarPanel.class;
				}
				if (c != null) {
					ArrayList<Long> idArray = getIdArray(c);
					for (Long id : idArray) {
						idListModel.addElement(id);
					}
				}
			}
		});
		final ArrayList<Long> idArray = getIdArray(Wall.class);
		for (Long id : idArray) {
			idListModel.addElement(id);
		}
		final JList<Long> idList = new JList<Long>(idListModel);
		idList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				SceneManager.getInstance().hideAllEditPoints();
				List<Long> selectedValues = idList.getSelectedValuesList();
				for (Long i : selectedValues) {
					HousePart p = Scene.getInstance().getPart(i);
					p.setEditPointsVisible(true);
					p.draw();
				}
			}
		});
		gui.add(typeComboBox, BorderLayout.NORTH);
		gui.add(new JScrollPane(idList), BorderLayout.CENTER);
		if (JOptionPane.showConfirmDialog(MainFrame.this, gui, "Select a Group", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.CANCEL_OPTION)
			return null;
		List<Long> selectedIds = idList.getSelectedValuesList();
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
					if (Scene.getInstance().hasSensor())
						new AnnualSensorData().show("Annual Sensor Data");
					else
						JOptionPane.showMessageDialog(MainFrame.this, "There is no sensor.", "No sensor", JOptionPane.INFORMATION_MESSAGE);
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
					if (Scene.getInstance().hasSensor())
						new DailySensorData().show("Daily Sensor Data");
					else
						JOptionPane.showMessageDialog(MainFrame.this, "There is no sensor.", "No sensor", JOptionPane.INFORMATION_MESSAGE);
				}
			});
		}
		return dailySensorMenuItem;
	}

	private JCheckBoxMenuItem getSolarRadiationHeatMapMenuItem() {
		if (solarRadiationHeatMapMenuItem == null) {
			solarRadiationHeatMapMenuItem = new JCheckBoxMenuItem("Solar Heat Map");
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

	private JCheckBoxMenuItem getSolarAbsorptionHeatMapMenuItem() {
		if (solarAbsorptionHeatMapMenuItem == null) {
			solarAbsorptionHeatMapMenuItem = new JCheckBoxMenuItem("Show Only Absorbed Energy in Solar Heat Map");
			solarAbsorptionHeatMapMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().setOnlyAbsorptionInSolarMap(solarAbsorptionHeatMapMenuItem.isSelected());
					if (SceneManager.getInstance().getSolarHeatMap())
						SceneManager.getInstance().setSolarHeatMap(true);
				}
			});
		}
		return solarAbsorptionHeatMapMenuItem;
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

	private JMenuItem getOrientationalEnergyAnalysisMenuItem() {
		if (orientationalEnergyAnalysisMenuItem == null) {
			orientationalEnergyAnalysisMenuItem = new JMenuItem("Run Orientation Analysis...");
			orientationalEnergyAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
					if ("".equals(city)) {
						JOptionPane.showMessageDialog(MainFrame.this, "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null) {
						int count = 0;
						HousePart hp = null;
						for (final HousePart x : Scene.getInstance().getParts()) {
							if (x instanceof Foundation) {
								count++;
								hp = x;
							}
						}
						if (count == 1) {
							SceneManager.getInstance().setSelectedPart(hp);
						} else {
							JOptionPane.showMessageDialog(MainFrame.this, "You must select a building or a component first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
							return;
						}
					}
					new EnergyAngularAnalysis().show("Orientation");
				}
			});
		}
		return orientationalEnergyAnalysisMenuItem;
	}

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

	private JMenuItem getAnnualEnvironmentalTemperatureMenuItem() {
		if (annualEnvironmentalTemperatureMenuItem == null) {
			annualEnvironmentalTemperatureMenuItem = new JMenuItem("Show Annual Environmental Temperature...");
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
			dailyEnvironmentalTemperatureMenuItem = new JMenuItem("Show Daily Environmental Temperature...");
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
					lockAllMenuItem.setEnabled(true);
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
					MainPanel.getInstance().defaultTool();
					if (Scene.getInstance().isStudentMode()) {
						lockAllMenuItem.setEnabled(false);
						removeAllLocksMenuItem.setEnabled(false);
						specificationsMenuItem.setEnabled(false);
						autoRecomputeEnergyMenuItem.setEnabled(false);
						rescaleMenuItem.setEnabled(false);
					} else {
						rescaleMenuItem.setEnabled(selectedPart instanceof Foundation || selectedPart == null);
					}
				}
			});

			final JMenu clearMenu = new JMenu("Clear");
			clearMenu.add(getRemoveAllWindowsMenuItem());
			clearMenu.add(getRemoveAllWindowShuttersMenuItem());
			clearMenu.add(getRemoveAllSolarPanelsMenuItem());
			clearMenu.add(getRemoveAllTreesMenuItem());
			clearMenu.add(getRemoveAllHumansMenuItem());
			clearMenu.add(getRemoveAllRoofsMenuItem());
			clearMenu.add(getRemoveAllFloorsMenuItem());
			clearMenu.add(getRemoveAllLocksMenuItem());
			clearMenu.add(getRemoveAllUtilityBillsMenuItem());

			final JMenu moveMenu = new JMenu("Move");
			moveMenu.add(getMoveEastMenuItem());
			moveMenu.add(getMoveWestMenuItem());
			moveMenu.add(getMoveNorthMenuItem());
			moveMenu.add(getMoveSouthMenuItem());

			editMenu.add(getUndoMenuItem());
			editMenu.add(getRedoMenuItem());
			editMenu.addSeparator();
			editMenu.add(getCutMenuItem());
			editMenu.add(getCopyMenuItem());
			editMenu.add(getPasteMenuItem());
			editMenu.add(moveMenu);
			editMenu.add(clearMenu);
			editMenu.addSeparator();
			editMenu.add(getFixProblemsMenuItem());
			editMenu.add(getGridsMenuItem());
			editMenu.add(getSnapMenuItem());
			editMenu.add(getAutoRecomputeEnergyMenuItem());
			editMenu.add(getLockAllMenuItem());
			editMenu.add(getRescaleMenuItem());
			editMenu.addSeparator();
			editMenu.add(getOverallUtilityBillMenuItem());
			editMenu.add(getSpecificationsMenuItem());
			editMenu.addSeparator();
			editMenu.add(getPropertiesMenuItem());
		}
		return editMenu;
	}

	public void refreshUndoRedo() {
		getUndoMenuItem().setText(SceneManager.getInstance().getUndoManager().getUndoPresentationName());
		getUndoMenuItem().setEnabled(SceneManager.getInstance().getUndoManager().canUndo());
		getRedoMenuItem().setText(SceneManager.getInstance().getUndoManager().getRedoPresentationName());
		getRedoMenuItem().setEnabled(SceneManager.getInstance().getUndoManager().canRedo());
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
						SceneManager.getInstance().deleteCurrentHousePart();
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
					if (selectedPart != null)
						Scene.getInstance().setCopyBuffer(selectedPart);
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

	private void saveFile() {
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.addChoosableFileFilter(ng3Filter);
		fileChooser.removeChoosableFileFilter(pngFilter);
		fileChooser.removeChoosableFileFilter(daeFilter);
		fileChooser.removeChoosableFileFilter(zipFilter);
		fileChooser.setFileFilter(ng3Filter);
		if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getSelectedFile().getParent());
			try {
				File file = fileChooser.getSelectedFile();
				if (!file.getName().toLowerCase().endsWith(".ng3"))
					file = new File(file.toString() + ".ng3");
				boolean doIt = true;
				if (file.exists()) {
					if (JOptionPane.showConfirmDialog(this, "File " + file + " exists. Do you want to overwrite it?", "Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
						doIt = false;
					}
				}
				if (doIt) {
					Scene.save(file.toURI().toURL(), true);
					updateTitleBar();
					fileChooser.rememberFile(file.getPath());
				}
			} catch (final Throwable err) {
				err.printStackTrace();
				JOptionPane.showMessageDialog(this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	void importFile() {
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.addChoosableFileFilter(ng3Filter);
		fileChooser.removeChoosableFileFilter(pngFilter);
		fileChooser.removeChoosableFileFilter(daeFilter);
		fileChooser.removeChoosableFileFilter(zipFilter);
		fileChooser.setFileFilter(ng3Filter);
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getSelectedFile().getParent());
			SceneManager.getTaskManager().update(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					try {
						Scene.importFile(fileChooser.getSelectedFile().toURI().toURL());
					} catch (final Throwable err) {
						Util.reportError(err);
					}
					return null;
				}
			});
		}
	}

	private void importColladaFile() {
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.removeChoosableFileFilter(pngFilter);
		fileChooser.removeChoosableFileFilter(ng3Filter);
		fileChooser.removeChoosableFileFilter(zipFilter);
		fileChooser.addChoosableFileFilter(daeFilter);
		fileChooser.setFileFilter(daeFilter);
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getSelectedFile().getParent());
			try {
				SceneManager.getInstance().newImport(fileChooser.getSelectedFile().toURI().toURL());
			} catch (final Throwable err) {
				err.printStackTrace();
				JOptionPane.showMessageDialog(this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
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
			importMenuItem.setToolTipText("Import the content in an existing file into the clicked location on the land as the center");
			importMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					importFile();
				}
			});
		}
		return importMenuItem;
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

	private JCheckBoxMenuItem getGridsMenuItem() {
		if (gridsMenuItem == null) {
			gridsMenuItem = new JCheckBoxMenuItem("Snap To Grids");
			gridsMenuItem.setSelected(true);
			gridsMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					HousePart.setSnapToGrids(gridsMenuItem.isSelected());
				}
			});
		}
		return gridsMenuItem;
	}

	public JCheckBoxMenuItem getTopViewCheckBoxMenuItem() {
		if (topViewCheckBoxMenuItem == null) {
			topViewCheckBoxMenuItem = new JCheckBoxMenuItem("2D Top View");
			topViewCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Config.isMac() ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK));
			topViewCheckBoxMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final TopViewCommand c = new TopViewCommand();
					final boolean isTopView = topViewCheckBoxMenuItem.isSelected();
					if (isTopView) {
						Scene.saveCameraLocation();
						SceneManager.getInstance().resetCamera(ViewMode.TOP_VIEW);
					} else {
						Scene.loadCameraLocation();
						SceneManager.getInstance().resetCamera(ViewMode.NORMAL);
					}
					SceneManager.getInstance().refresh();
					SceneManager.getInstance().getUndoManager().addEdit(c);
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
					ZoomCommand c = new ZoomCommand(true);
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
					ZoomCommand c = new ZoomCommand(false);
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
					if (MainPanel.getInstance().getEnergyViewButton().isSelected())
						MainPanel.getInstance().getEnergyViewButton().setSelected(false);
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
					if (MainPanel.getInstance().getEnergyViewButton().isSelected())
						MainPanel.getInstance().getEnergyViewButton().setSelected(false);
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
					if (MainPanel.getInstance().getEnergyViewButton().isSelected())
						MainPanel.getInstance().getEnergyViewButton().setSelected(false);
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
					Scene.getInstance().setTheme(Scene.CLOUDY_SKY);
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
					Scene.getInstance().setTheme(Scene.DESERT);
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
					Scene.getInstance().setTheme(Scene.GRASSLAND);
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
					Scene.getInstance().setTheme(Scene.FOREST);
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
		ActionListener actionListener;
		if (selectedPart == null) {
			final ReadOnlyColorRGBA color = Scene.getInstance().getLandColor();
			if (color != null)
				colorChooser.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
			actionListener = new ActionListener() {
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
				if (JOptionPane.showConfirmDialog(this, "To set color for an individual part, we have to remove the texture. Is that OK?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION)
					return;
				noTextureMenuItem.setSelected(true);
				Scene.getInstance().setTextureMode(TextureMode.None);
			}
			final ReadOnlyColorRGBA color = selectedPart.getColor();
			if (color != null)
				colorChooser.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
			actionListener = new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null)
						return;
					final Color c = colorChooser.getColor();
					final float[] newColor = c.getComponents(null);
					final boolean restartPrintPreview = Scene.getInstance().getRoofColor().equals(ColorRGBA.WHITE) || c.equals(Color.WHITE);
					final ColorRGBA color = new ColorRGBA(newColor[0], newColor[1], newColor[2], newColor[3]);
					if (selectedPart instanceof Wall) {
						final JPanel panel = new JPanel();
						panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
						panel.setBorder(BorderFactory.createTitledBorder("Apply to:"));
						final JRadioButton rb1 = new JRadioButton("Only this Wall", true);
						final JRadioButton rb2 = new JRadioButton("All Walls of this Building");
						panel.add(rb1);
						panel.add(rb2);
						final ButtonGroup bg = new ButtonGroup();
						bg.add(rb1);
						bg.add(rb2);
						if (JOptionPane.showConfirmDialog(MainFrame.this, panel, "Scope", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION)
							return;
						if (rb1.isSelected()) { // apply to only this part
							final ChangePartColorCommand cmd = new ChangePartColorCommand(selectedPart);
							selectedPart.setColor(color);
							SceneManager.getInstance().getUndoManager().addEdit(cmd);
						} else {
							final ChangeBuildingColorCommand cmd = new ChangeBuildingColorCommand(selectedPart);
							Scene.getInstance().setPartColorOfBuilding(selectedPart, color);
							SceneManager.getInstance().getUndoManager().addEdit(cmd);
						}
						Scene.getInstance().setWallColor(color); // remember the color decision for the next wall
					} else {
						final ChangePartColorCommand cmd = new ChangePartColorCommand(selectedPart);
						selectedPart.setColor(color);
						SceneManager.getInstance().getUndoManager().addEdit(cmd);
						if (selectedPart instanceof Roof) { // remember the color decision for the next part
							Scene.getInstance().setRoofColor(color);
						} else if (selectedPart instanceof Door) {
							Scene.getInstance().setDoorColor(color);
						} else if (selectedPart instanceof Floor) {
							Scene.getInstance().setFloorColor(color);
						} else if (selectedPart instanceof Foundation) {
							Scene.getInstance().setFoundationColor(color);
						}
					}
					Scene.getInstance().setTextureMode(Scene.getInstance().getTextureMode());
					if (restartPrintPreview && PrintController.getInstance().isPrintPreview())
						PrintController.getInstance().restartAnimation();
					MainPanel.getInstance().getEnergyViewButton().setSelected(false);
					Scene.getInstance().setEdited(true);
				}
			};
		}
		JColorChooser.createDialog(this, "Select Color", true, colorChooser, actionListener, null).setVisible(true);
	}

	public void open(final String filename) {
		try {
			SceneManager.getTaskManager().update(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					Scene.open(new File(filename).toURI().toURL());
					fileChooser.rememberFile(filename);
					return null;
				}
			});
		} catch (final Throwable e) {
			Util.reportError(e);
		}
	}

	private void openTemplate(final URL url) {
		boolean ok = false;
		if (Scene.getInstance().isEdited()) {
			final int save = JOptionPane.showConfirmDialog(MainFrame.this, "Do you want to save changes?", "Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (save == JOptionPane.YES_OPTION) {
				save();
				if (!Scene.getInstance().isEdited())
					ok = true;
			} else if (save != JOptionPane.CANCEL_OPTION)
				ok = true;
		} else
			ok = true;
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
		final String[] recentFiles = fileChooser.getRecentFiles();
		if (recentFiles != null) {
			final int n = recentFiles.length;
			if (n > 0) {
				final Preferences pref = Preferences.userNodeForPackage(MainApplication.class);
				for (int i = 0; i < n; i++)
					pref.put("Recent File " + i, recentFiles[n - i - 1]);
			}
		}
		if (Scene.getInstance().isEdited()) {
			final int save = JOptionPane.showConfirmDialog(this, "Do you want to save changes?", "Save", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (save == JOptionPane.YES_OPTION) {
				save();
				if (!Scene.getInstance().isEdited())
					MainApplication.exit();
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
				if (Scene.isTemplate()) {
					saveFile();
				} else {
					Scene.save(url, false);
				}
			} else {
				saveFile();
			}
			Scene.getInstance().setEdited(false, false);
		} catch (final Throwable err) {
			err.printStackTrace();
			JOptionPane.showMessageDialog(this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private JMenuItem getImportColladaMenuItem() {
		if (importColladaMenuItem == null) {
			importColladaMenuItem = new JMenuItem("Import Collada...");
			importColladaMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					importColladaFile();
				}
			});
		}
		return importColladaMenuItem;
	}

	private JMenuItem getExportLogMenuItem() {
		if (exportLogMenuItem == null) {
			exportLogMenuItem = new JMenuItem("Export Log As Zip...");
			exportLogMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fileChooser.addChoosableFileFilter(zipFilter);
					fileChooser.removeChoosableFileFilter(pngFilter);
					fileChooser.removeChoosableFileFilter(ng3Filter);
					fileChooser.removeChoosableFileFilter(daeFilter);
					fileChooser.setFileFilter(zipFilter);
					if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
						try {
							File file = fileChooser.getSelectedFile();
							if (!file.getName().toLowerCase().endsWith(".zip"))
								file = new File(file.toString() + ".zip");
							boolean doIt = true;
							if (file.exists()) {
								if (JOptionPane.showConfirmDialog(MainFrame.this, "File " + file + " exists. Do you want to overwrite it?", "Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
									doIt = false;
								}
							}
							if (doIt)
								new LogZipper(file).createDialog();
						} catch (final Throwable err) {
							err.printStackTrace();
							JOptionPane.showMessageDialog(MainFrame.this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
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
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.addChoosableFileFilter(pngFilter);
		fileChooser.removeChoosableFileFilter(ng3Filter);
		fileChooser.removeChoosableFileFilter(daeFilter);
		fileChooser.removeChoosableFileFilter(zipFilter);
		fileChooser.setFileFilter(pngFilter);
		if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			System.out.print("Saving snapshot: ");
			Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getSelectedFile().getParent());
			try {
				File file = fileChooser.getSelectedFile();
				if (!file.getName().toLowerCase().endsWith(".png"))
					file = new File(file.toString() + ".png");
				System.out.print(file + "...");
				boolean doIt = true;
				if (file.exists()) {
					if (JOptionPane.showConfirmDialog(this, "File " + file + " exists. Do you want to overwrite it?", "Overwrite", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
						doIt = false;
					}
				}
				if (doIt) {
					final BufferedImage snapShot = Printout.takeSnapShot();
					ImageIO.write(snapShot, "png", file);
					System.out.println("done");
				}
			} catch (final Throwable err) {
				err.printStackTrace();
				JOptionPane.showMessageDialog(this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
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
						if (JOptionPane.showConfirmDialog(MainFrame.this, "<html>No overall utility bill is found. Create one?<br>(This applies to all the structures in this scene.)</html>", "Overall Utility Bill", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION)
							return;
						b = new UtilityBill();
						Scene.getInstance().setUtilityBill(b);
					}
					new UtilityBillDialog(b).setVisible(true);
				}
			});
		}
		return overallUtilityBillMenuItem;
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
			infoPanelCheckBoxMenuItem = new JCheckBoxMenuItem("Show Information Panel");
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
							Scene.getInstance().removeAllSolarPanels();
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
					ArrayList<Foundation> list = new ArrayList<Foundation>();
					for (HousePart p : Scene.getInstance().getParts()) {
						if (p instanceof Foundation && ((Foundation) p).getUtilityBill() != null) {
							list.add((Foundation) p);
						}
					}
					if (list.isEmpty()) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no utilitiy bill to remove.", "No Utility Bill", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + list.size() + " utility bills associated with buildings in this scene?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
						for (Foundation f : list)
							f.setUtilityBill(null);
					}
					Scene.getInstance().setEdited(true);
				}
			});
		}
		return removeAllUtilityBillsMenuItem;
	}

	private JMenuItem getMoveEastMenuItem() {
		if (moveEastMenuItem == null) {
			moveEastMenuItem = new JMenuItem("Move East");
			moveEastMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0)); // use 0 to specify no modifier
			moveEastMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (MainPanel.getInstance().getNoteTextArea().hasFocus())
						return;
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
			moveWestMenuItem = new JMenuItem("Move West");
			moveWestMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0)); // use 0 to specify no modifier
			moveWestMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (MainPanel.getInstance().getNoteTextArea().hasFocus())
						return;
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
			moveSouthMenuItem = new JMenuItem("Move South");
			moveSouthMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0)); // use 0 to specify no modifier
			moveSouthMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (MainPanel.getInstance().getNoteTextArea().hasFocus())
						return;
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
			moveNorthMenuItem = new JMenuItem("Move North");
			moveNorthMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0)); // use 0 to specify no modifier
			moveNorthMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (MainPanel.getInstance().getNoteTextArea().hasFocus())
						return;
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
							Scene.getInstance().fixProblems();
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

	private JMenuItem getLockAllMenuItem() {
		if (lockAllMenuItem == null) {
			lockAllMenuItem = new JMenuItem("Lock All");
			lockAllMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() {
							Scene.getInstance().lockAll(true);
							return null;
						}
					});
				}
			});
		}
		return lockAllMenuItem;
	}

	public JColorChooser getColorChooser() {
		return colorChooser;
	}

}
