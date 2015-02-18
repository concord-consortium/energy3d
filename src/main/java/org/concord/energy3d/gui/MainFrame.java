package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileFilter;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.logger.DesignReplay;
import org.concord.energy3d.logger.PostProcessor;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.PrintController;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.Scene.Unit;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.CameraMode;
import org.concord.energy3d.scene.SceneManager.Operation;
import org.concord.energy3d.scene.SceneManager.ViewMode;
import org.concord.energy3d.simulation.AnnualSensorData;
import org.concord.energy3d.simulation.Cost;
import org.concord.energy3d.simulation.EnergyAngularAnalysis;
import org.concord.energy3d.simulation.EnergyAnnualAnalysis;
import org.concord.energy3d.simulation.SolarIrradiation;
import org.concord.energy3d.undo.ChangeColorTextureCommand;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.FileChooser;
import org.concord.energy3d.util.Mac;
import org.concord.energy3d.util.Printout;
import org.concord.energy3d.util.Util;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final MainFrame instance = new MainFrame();
	private JMenuBar appMenuBar = null;
	private JMenu fileMenu = null;
	private int fileMenuItemCount;
	private final List<JComponent> recentFileMenuItems = new ArrayList<JComponent>();
	private JMenuItem newMenuItem = null;
	private JMenuItem openMenuItem = null;
	private JMenuItem openFolderMenuItem = null;
	private JMenuItem analyzeFolderMenuItem = null;
	private JMenuItem saveMenuItem = null;
	private JMenuItem printMenuItem = null;
	private JCheckBoxMenuItem previewMenuItem = null;
	private JMenu cameraMenu = null;
	private JRadioButtonMenuItem orbitMenuItem = null;
	private JRadioButtonMenuItem firstPersonMenuItem = null;
	private JMenuItem resetCameraMenuItem = null;
	private JMenuItem saveasMenuItem;
	private JMenu viewMenu;
	private JMenu analysisMenu;
	private JMenu unitsMenu;
	private JRadioButtonMenuItem metersMenuItem;
	private JRadioButtonMenuItem centimetersMenuItem;
	private JRadioButtonMenuItem inchesMenuItem;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JMenuItem rescaleMenuItem;
	private JMenuItem simulationSettingsMenuItem;
	private JMenuItem annualEnergyAnalysisMenuItem;
	private JMenuItem annualEnergyAnalysisForSelectionMenuItem;
	private JMenuItem sensorMenuItem;
	private JMenuItem orientationalEnergyAnalysisMenuItem;
	private JMenuItem materialCostAnalysisMenuItem;
	private JMenuItem dailyAnalysisMenuItem;
	private JCheckBoxMenuItem solarHeatMapMenuItem;
	private JCheckBoxMenuItem onlyAbsorptionInSolarMapMenuItem;
	private JCheckBoxMenuItem showHeatFluxVectorsMenuItem;
	private JCheckBoxMenuItem axesMenuItem;
	private JCheckBoxMenuItem shadowMenuItem;
	private JCheckBoxMenuItem buildingLabelsMenuItem;
	protected Object lastSelection;
	private JMenuItem exitMenuItem = null;
	private JMenu helpMenu = null;
	private JMenuItem aboutMenuItem = null;
	private JDialog aboutDialog = null;
	private MainPanel mainPanel = null;
	private JCheckBoxMenuItem annotationsInwardMenuItem;
	private JMenu editMenu;
	private JMenuItem undoMenuItem;
	private JMenuItem redoMenuItem;
	private JMenuItem pageSetupMenuItem;
	private JRadioButtonMenuItem scaleToFitRadioButtonMenuItem;
	private JRadioButtonMenuItem exactSizeRadioButtonMenuItem;
	private final ButtonGroup printSizeOptionBbuttonGroup = new ButtonGroup();
	private JMenuItem importMenuItem;
	private JCheckBoxMenuItem snapMenuItem;
	private JCheckBoxMenuItem gridsMenuItem;
	private JCheckBoxMenuItem topViewCheckBoxMenuItem;
	private JMenuItem roofOverhangLengthMenuItem;
	private JRadioButtonMenuItem noTextureMenuItem;
	private JRadioButtonMenuItem simpleTextureMenuItem;
	private JRadioButtonMenuItem fullTextureMenuItem;
	private final ButtonGroup textureButtonGroup = new ButtonGroup();
	private JMenu colorMenu;
	private JMenuItem platformColorMenuItem;
	private JMenuItem wallColorMenuItem;
	private JMenuItem doorColorMenuItem;
	private JMenuItem floorColorMenuItem;
	private JMenuItem roofColorMenuItem;
	private JMenuItem importColladaMenuItem;
	private JMenuItem exportImageMenuItem;
	private JMenuItem exportLogMenuItem;
	private JMenuItem lockAllMenuItem;
	private JMenuItem unlockAllMenuItem;
	private JMenuItem lockSelectionMenuItem;
	private JCheckBoxMenuItem disableFoundationCheckBoxMenuItem;
	private JMenuItem specificationsMenuItem;
	private JCheckBoxMenuItem noteCheckBoxMenuItem;

	private final FileChooser fileChooser;
	private final JColorChooser colorChooser;
	private final ExtensionFileFilter ng3Filter = new ExtensionFileFilter("Energy3D Project (*.ng3)", "ng3");
	private final ExtensionFileFilter pngFilter = new ExtensionFileFilter("Image (*.png)", "png");
	private final ExtensionFileFilter daeFilter = new ExtensionFileFilter("Collada (*.dae)", "dae");
	private final ExtensionFileFilter zipFilter = new ExtensionFileFilter("Zip (*.zip)", "zip");
	private JCheckBoxMenuItem autoRecomputeEnergyMenuItem;
	private JMenuItem removeAllRoofsMenuItem;

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
	}

	private void initialize() {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setTitle("Energy3D V" + Config.VERSION);

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
			appMenuBar.add(getCameraMenu());
			appMenuBar.add(getAnalysisMenu());
			appMenuBar.add(getHelpMenu());
		}
		return appMenuBar;
	}

	private JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = new JMenu();
			fileMenu.addMenuListener(new MenuListener() {
				@Override
				public void menuCanceled(final MenuEvent e) {
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					SceneManager.getInstance().refresh();
				}

				@Override
				public void menuSelected(final MenuEvent e) {
					MainFrame.this.deselect();
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
											new Thread() {
												@Override
												public void run() {
													try {
														Scene.open(rf.toURI().toURL());
														updateTitleBar();
														fileChooser.rememberFile(rf.getPath());
													} catch (final Throwable err) {
														showUnexpectedErrorMessage(err);
													}
												}
											}.start();
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
			addItemToFileMenu(getExportImageMenuItem());
			addItemToFileMenu(getExportLogMenuItem());
			addItemToFileMenu(getImportMenuItem());
			addItemToFileMenu(getImportColladaMenuItem());
			addItemToFileMenu(new JSeparator());
			if (!Config.isRestrictMode()) {
				addItemToFileMenu(getOpenFolderMenuItem());
				addItemToFileMenu(getAnalyzeFolderMenuItem());
				addItemToFileMenu(new JSeparator());
			}
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
									showUnexpectedErrorMessage(err);
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
			new Thread() {
				@Override
				public void run() {
					try {
						Scene.open(file.toURI().toURL());
						fileChooser.rememberFile(file.getPath());
					} catch (final Throwable err) {
						showUnexpectedErrorMessage(err);
					}
				}
			}.start();
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
							PostProcessor.analyze(dir.listFiles(new FilenameFilter() {
								@Override
								public boolean accept(final File dir, final String name) {
									return name.endsWith(".ng3");
								}
							}), new File(fileChooser.getCurrentDirectory() + System.getProperty("file.separator") + "prop.txt"), new Runnable() {
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

	private JMenuItem getOpenFolderMenuItem() {
		if (openFolderMenuItem == null) {
			openFolderMenuItem = new JMenuItem("Open Folder...");
			openFolderMenuItem.addActionListener(new ActionListener() {
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
							DesignReplay.play(dir.listFiles(new FilenameFilter() {
								@Override
								public boolean accept(final File dir, final String name) {
									return name.endsWith(".ng3");
								}
							}));
						}
					}
				}
			});
		}
		return openFolderMenuItem;
	}

	public void updateTitleBar() {
		final String star = Scene.getInstance().isEdited() ? "*" : "";
		if (Scene.getURL() == null)
			setTitle("Energy3D V" + Config.VERSION + star);
		else
			setTitle("Energy3D V" + Config.VERSION + " - " + new File(Scene.getURL().getFile()).toString().replaceAll("%20", " ") + star);
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

	public JMenu getCameraMenu() {
		if (cameraMenu == null) {
			cameraMenu = new JMenu();
			cameraMenu.addMenuListener(new MenuListener() {
				@Override
				public void menuCanceled(final MenuEvent e) {
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					SceneManager.getInstance().refresh();
				}

				@Override
				public void menuSelected(final MenuEvent e) {
					mainPanel.getSelectButton().setSelected(true);
					SceneManager.getInstance().setOperation(SceneManager.Operation.SELECT);
				}
			});
			cameraMenu.setText("Camera");
			cameraMenu.add(getOrbitMenuItem());
			cameraMenu.add(getFirstPersonMenuItem());
			final ButtonGroup bg = new ButtonGroup();
			bg.add(orbitMenuItem);
			bg.add(firstPersonMenuItem);
			cameraMenu.addSeparator();
			cameraMenu.add(getTopViewCheckBoxMenuItem());
			cameraMenu.add(getResetCameraMenuItem());
		}
		return cameraMenu;
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
			JMenuItem mi = new JMenuItem("Download Models...");
			mi.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Util.openBrowser("http://energy.concord.org/energy3d/models.html");
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

	public void showAbout() {
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
			p.add(new JLabel("<html><h2>Energy3D</h2><br>Version: " + Config.VERSION + "<hr><h3>Credit:</h3>This program is brought to you by two scientists at the Concord Consortium:<ul><li>Dr. Saeid Nourian, Lead Developer<li>Dr. Charles Xie, Co-developer</ul><p>This program is licensed under the GNU Lesser General Public License V3.0<br>and based on Ardor3D. Funding of this project is provided by the National<br>Science Foundation under grants #0918449, #1304485, and #1348530.</html>"), BorderLayout.CENTER);
			final JButton button = new JButton("Thanks!");
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

	public void deselect() {
		lastSelection = null;
		mainPanel.getSelectButton().setSelected(true);
		SceneManager.getInstance().setOperation(Operation.SELECT);
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
				@Override
				public void menuCanceled(final MenuEvent e) {
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					SceneManager.getInstance().refresh();
				}

				@Override
				public void menuSelected(final MenuEvent e) {
					mainPanel.getSelectButton().setSelected(true);
					SceneManager.getInstance().setOperation(SceneManager.Operation.SELECT);
				}
			});
			analysisMenu.add(getMaterialCostAnalysisMenuItem());
			analysisMenu.addSeparator();
			analysisMenu.add(getAnnualEnergyAnalysisMenuItem());
			analysisMenu.add(getAnnualEnergyAnalysisForSelectionMenuItem());
			analysisMenu.add(getDailyAnalysisMenuItem());
			analysisMenu.add(getOrientationalEnergyAnalysisMenuItem());
			analysisMenu.addSeparator();
			analysisMenu.add(getSensorMenuItem());
			if (!Config.isRestrictMode()) {
				analysisMenu.addSeparator();
				analysisMenu.add(getSimulationSettingsMenuItem());
			}
		}
		return analysisMenu;
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
					Util.selectSilently(solarHeatMapMenuItem, SceneManager.getInstance().getSolarHeatMap());
					Util.selectSilently(onlyAbsorptionInSolarMapMenuItem, Scene.getInstance().getOnlyAbsorptionInSolarMap());
					Util.selectSilently(showHeatFluxVectorsMenuItem, Scene.getInstance().getAlwaysComputeHeatFluxVectors());
					Util.selectSilently(shadowMenuItem, SceneManager.getInstance().isShadowEnabled());
					Util.selectSilently(axesMenuItem, SceneManager.getInstance().areAxesShown());
					Util.selectSilently(buildingLabelsMenuItem, SceneManager.getInstance().areBuildingLabelsShown());
					mainPanel.getSelectButton().setSelected(true);
					SceneManager.getInstance().setOperation(SceneManager.Operation.SELECT);
				}
			});
			viewMenu.add(getUnitsMenu());
			viewMenu.addSeparator();
			viewMenu.add(getNoTextureMenuItem());
			viewMenu.add(getSimpleTextureMenuItem());
			viewMenu.add(getFullTextureMenuItem());
			viewMenu.add(getColorMenu());
			viewMenu.addSeparator();
			viewMenu.add(getSolarHeatMapMenuItem());
			viewMenu.add(getSolarAbsorptionMapMenuItem());
			viewMenu.add(getHeatFluxMenuItem());
			viewMenu.add(getAxesMenuItem());
			viewMenu.add(getShadowMenuItem());
			viewMenu.add(getBuildingLabelsMenuItem());
			viewMenu.add(getAnnotationsInwardMenuItem());
			// viewMenu.add(getWallThicknessMenuItem());

		}
		return viewMenu;
	}

	private JMenu getUnitsMenu() {
		if (unitsMenu == null) {
			unitsMenu = new JMenu("Units");
			unitsMenu.add(getMetersMenuItem());
			unitsMenu.add(getCentimetersMenuItem());
			unitsMenu.add(getInchesMenuItem());
		}
		return unitsMenu;
	}

	public JCheckBoxMenuItem getAxesMenuItem() {
		if (axesMenuItem == null) {
			axesMenuItem = new JCheckBoxMenuItem("Axes", true);
			axesMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					SceneManager.getInstance().showAxes(axesMenuItem.isSelected());
					Scene.getInstance().setEdited(true);
				}
			});
		}
		return axesMenuItem;
	}

	public JCheckBoxMenuItem getBuildingLabelsMenuItem() {
		if (buildingLabelsMenuItem == null) {
			buildingLabelsMenuItem = new JCheckBoxMenuItem("Building Labels", false);
			buildingLabelsMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					SceneManager.getInstance().showBuildingLabels(buildingLabelsMenuItem.isSelected());
					Scene.getInstance().setEdited(true);
				}
			});
		}
		return buildingLabelsMenuItem;
	}

	public JCheckBoxMenuItem getShadowMenuItem() {
		if (shadowMenuItem == null) {
			shadowMenuItem = new JCheckBoxMenuItem("Shadows", false);
			shadowMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					SceneManager.getInstance().setShadow(shadowMenuItem.isSelected());
					Util.selectSilently(MainPanel.getInstance().getShadowButton(), shadowMenuItem.isSelected());
				}
			});
		}
		return shadowMenuItem;
	}

	private JRadioButtonMenuItem getMetersMenuItem() {
		if (metersMenuItem == null) {
			metersMenuItem = new JRadioButtonMenuItem("Meters (m)");
			metersMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().setUnit(Unit.Meter);
				}
			});
			buttonGroup.add(metersMenuItem);
			metersMenuItem.setSelected(true);
		}
		return metersMenuItem;
	}

	private JRadioButtonMenuItem getCentimetersMenuItem() {
		if (centimetersMenuItem == null) {
			centimetersMenuItem = new JRadioButtonMenuItem("Centimeters (cm)");
			centimetersMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().setUnit(Unit.Centimeter);
				}
			});
			buttonGroup.add(centimetersMenuItem);
		}
		return centimetersMenuItem;
	}

	private JRadioButtonMenuItem getInchesMenuItem() {
		if (inchesMenuItem == null) {
			inchesMenuItem = new JRadioButtonMenuItem("Inches (\")");
			inchesMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().setUnit(Unit.Inches);
				}
			});
			buttonGroup.add(inchesMenuItem);
		}
		return inchesMenuItem;
	}

	private JMenuItem getRescaleMenuItem() {
		if (rescaleMenuItem == null) {
			rescaleMenuItem = new JMenuItem("Rescale...");
			rescaleMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					new ScaleDialog().setVisible(true);
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
			annualEnergyAnalysisMenuItem = new JMenuItem("Run Annual Energy Analysis for Building...");
			annualEnergyAnalysisMenuItem.setAccelerator(KeyStroke.getKeyStroke("F4"));
			annualEnergyAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (autoSelectBuilding(true) instanceof Foundation)
						new EnergyAnnualAnalysis().show("Annual Energy");
				}
			});
		}
		return annualEnergyAnalysisMenuItem;
	}

	private JMenuItem getAnnualEnergyAnalysisForSelectionMenuItem() {
		if (annualEnergyAnalysisForSelectionMenuItem == null) {
			annualEnergyAnalysisForSelectionMenuItem = new JMenuItem("Run Annual Energy Analysis for Selected Part...");
			annualEnergyAnalysisForSelectionMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof Window || selectedPart instanceof Wall || selectedPart instanceof Roof || selectedPart instanceof Door || selectedPart instanceof SolarPanel) {
						new EnergyAnnualAnalysis().show("Annual Energy for Selected Part");
					} else {
						JOptionPane.showMessageDialog(MainFrame.this, "You must select a building part first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});
		}
		return annualEnergyAnalysisForSelectionMenuItem;
	}

	private JMenuItem getSensorMenuItem() {
		if (sensorMenuItem == null) {
			sensorMenuItem = new JMenuItem("Collect Sensor Data...");
			sensorMenuItem.setAccelerator(KeyStroke.getKeyStroke("F9"));
			sensorMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (Scene.getInstance().hasSensor())
						new AnnualSensorData().show("Sensor Data");
					else
						JOptionPane.showMessageDialog(MainFrame.this, "There is no sensor.", "No sensor", JOptionPane.INFORMATION_MESSAGE);
				}
			});
		}
		return sensorMenuItem;
	}

	private JCheckBoxMenuItem getSolarHeatMapMenuItem() {
		if (solarHeatMapMenuItem == null) {
			solarHeatMapMenuItem = new JCheckBoxMenuItem("Solar Heat Map");
			solarHeatMapMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().setSolarHeatMap(solarHeatMapMenuItem.isSelected());
					Scene.getInstance().redrawAll();
					SceneManager.getInstance().refresh();
				}
			});
		}
		return solarHeatMapMenuItem;
	}

	private JCheckBoxMenuItem getSolarAbsorptionMapMenuItem() {
		if (onlyAbsorptionInSolarMapMenuItem == null) {
			onlyAbsorptionInSolarMapMenuItem = new JCheckBoxMenuItem("Show Only Absorption in Solar Heat Map");
			onlyAbsorptionInSolarMapMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().setOnlyAbsorptionInSolarMap(onlyAbsorptionInSolarMapMenuItem.isSelected());
					if (SceneManager.getInstance().getSolarHeatMap())
						SceneManager.getInstance().setSolarHeatMap(true);
				}
			});
		}
		return onlyAbsorptionInSolarMapMenuItem;
	}

	private JCheckBoxMenuItem getHeatFluxMenuItem() {
		if (showHeatFluxVectorsMenuItem == null) {
			showHeatFluxVectorsMenuItem = new JCheckBoxMenuItem("Heat Flux Vectors");
			showHeatFluxVectorsMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().setAlwaysComputeHeatFluxVectors(showHeatFluxVectorsMenuItem.isSelected());
					SolarIrradiation.getInstance().drawHeatFlux();
					SceneManager.getInstance().refresh();
				}
			});
		}
		return showHeatFluxVectorsMenuItem;
	}

	private JMenuItem getDailyAnalysisMenuItem() {
		if (dailyAnalysisMenuItem == null) {
			dailyAnalysisMenuItem = new JMenuItem("Run Daily Energy Analysis...");
			dailyAnalysisMenuItem.setEnabled(false);
			dailyAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null) {
						JOptionPane.showMessageDialog(MainFrame.this, "You must select a building or a component first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
				}
			});
		}
		return dailyAnalysisMenuItem;
	}

	private JMenuItem getOrientationalEnergyAnalysisMenuItem() {
		if (orientationalEnergyAnalysisMenuItem == null) {
			orientationalEnergyAnalysisMenuItem = new JMenuItem("Run Orientation Analysis...");
			orientationalEnergyAnalysisMenuItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
			orientationalEnergyAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
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

	private JMenuItem getMaterialCostAnalysisMenuItem() {
		if (materialCostAnalysisMenuItem == null) {
			materialCostAnalysisMenuItem = new JMenuItem("Show Material Costs...");
			materialCostAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Cost.getInstance().showGraph();
				}
			});
		}
		return materialCostAnalysisMenuItem;
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
				@Override
				public void menuCanceled(final MenuEvent e) {
				}

				@Override
				public void menuDeselected(final MenuEvent e) {
					SceneManager.getInstance().refresh();
				}

				@Override
				public void menuSelected(final MenuEvent e) {
					if (lockSelectionMenuItem != null)
						lockSelectionMenuItem.setEnabled(SceneManager.getInstance().getSelectedPart() != null);
					Util.selectSilently(noteCheckBoxMenuItem, MainPanel.getInstance().isNoteVisible());
					mainPanel.getSelectButton().setSelected(true);
					SceneManager.getInstance().setOperation(SceneManager.Operation.SELECT);
					final HousePart selected = SceneManager.getInstance().getSelectedPart();
					if (disableFoundationCheckBoxMenuItem != null) {
						if (selected instanceof Foundation) {
							disableFoundationCheckBoxMenuItem.setEnabled(true);
							Util.selectSilently(disableFoundationCheckBoxMenuItem, ((Foundation) selected).getLockEdit());
						} else {
							disableFoundationCheckBoxMenuItem.setEnabled(false);
							Util.selectSilently(disableFoundationCheckBoxMenuItem, false);
						}
					}
				}
			});
			editMenu.add(getUndoMenuItem());
			editMenu.add(getRedoMenuItem());
			editMenu.addSeparator();
			editMenu.add(getRescaleMenuItem());
			editMenu.addSeparator();
			editMenu.add(getGridsMenuItem());
			editMenu.add(getSnapMenuItem());
			if (!Config.isRestrictMode()) {
				editMenu.add(getAutoRecomputeEnergyMenuItem());
			}
			editMenu.addSeparator();
			editMenu.add(getRoofOverhangLengthMenuItem());
			editMenu.add(getRemoveAllRoofsMenuItem());
			editMenu.addSeparator();
			if (!Config.isRestrictMode()) {
				editMenu.add(getDisableFoundationCheckBoxMenuItem());
				editMenu.add(getLockSelectionMenuItem());
				editMenu.add(getLockAllMenuItem());
				editMenu.add(getUnlockAllMenuItem());
				editMenu.addSeparator();
				editMenu.add(getSpecificationsMenuItem());
				editMenu.addSeparator();
			}
			editMenu.add(getNoteCheckBoxMenuItem());
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
					MainPanel.getInstance().deselect();
					SceneManager.getInstance().hideAllEditPoints();
					SceneManager.getInstance().getUndoManager().undo();
					SceneManager.getInstance().refresh();
					EnergyPanel.getInstance().update();
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
					MainPanel.getInstance().deselect();
					SceneManager.getInstance().hideAllEditPoints();
					SceneManager.getInstance().getUndoManager().redo();
					SceneManager.getInstance().refresh();
					EnergyPanel.getInstance().update();
				}
			});
		}
		return redoMenuItem;
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

	private void importFile() {
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.addChoosableFileFilter(ng3Filter);
		fileChooser.removeChoosableFileFilter(pngFilter);
		fileChooser.removeChoosableFileFilter(daeFilter);
		fileChooser.removeChoosableFileFilter(zipFilter);
		fileChooser.setFileFilter(ng3Filter);
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getSelectedFile().getParent());
			try {
				Scene.importFile(fileChooser.getSelectedFile().toURI().toURL());
			} catch (final Throwable err) {
				err.printStackTrace();
				JOptionPane.showMessageDialog(this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
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
					final boolean isTopView = topViewCheckBoxMenuItem.isSelected();
					if (isTopView) {
						Scene.saveCameraLocation();
						SceneManager.getInstance().resetCamera(ViewMode.TOP_VIEW);
					} else {
						Scene.loadCameraLocation();
						SceneManager.getInstance().resetCamera(ViewMode.NORMAL);
					}
					SceneManager.getInstance().refresh();
				}
			});
		}
		return topViewCheckBoxMenuItem;
	}

	private JMenuItem getRoofOverhangLengthMenuItem() {
		if (roofOverhangLengthMenuItem == null) {
			roofOverhangLengthMenuItem = new JMenuItem("Roof Overhang Length...");
			roofOverhangLengthMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					while (true) {
						SceneManager.getInstance().refresh(1);
						final String newValue = JOptionPane.showInputDialog(MainFrame.this, "What is the length of roof overhang?", Scene.getInstance().getOverhangLength() * Scene.getInstance().getAnnotationScale());
						if (newValue == null)
							break;
						else {
							try {
								final double val = Double.parseDouble(newValue);
								if (val < 0 || val > 5)
									JOptionPane.showMessageDialog(MainFrame.this, "Overhang value must be between 0 and 5.", "Error", JOptionPane.ERROR_MESSAGE);
								else {
									Scene.getInstance().setOverhangLength(val / Scene.getInstance().getAnnotationScale());
									Scene.getInstance().redrawAll();
									EventQueue.invokeLater(new Runnable() {
										@Override
										public void run() {
											MainPanel.getInstance().getEnergyViewButton().setSelected(false);
										}
									});
									Scene.getInstance().setEdited(true);
									break;
								}
							} catch (final NumberFormatException e1) {
								e1.printStackTrace();
								JOptionPane.showMessageDialog(MainFrame.this, "" + newValue + " is an invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			});
		}
		return roofOverhangLengthMenuItem;
	}

	public JRadioButtonMenuItem getNoTextureMenuItem() {
		if (noTextureMenuItem == null) {
			noTextureMenuItem = new JRadioButtonMenuItem("No Texture");
			noTextureMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().getUndoManager().addEdit(new ChangeColorTextureCommand());
					Scene.getInstance().setTextureMode(TextureMode.None);
					Scene.getInstance().setEdited(true);
					if (MainPanel.getInstance().getEnergyViewButton().isSelected())
						MainPanel.getInstance().getEnergyViewButton().setSelected(false);
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
					SceneManager.getInstance().getUndoManager().addEdit(new ChangeColorTextureCommand());
					Scene.getInstance().setTextureMode(TextureMode.Simple);
					Scene.getInstance().setEdited(true);
					if (MainPanel.getInstance().getEnergyViewButton().isSelected())
						MainPanel.getInstance().getEnergyViewButton().setSelected(false);
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
					SceneManager.getInstance().getUndoManager().addEdit(new ChangeColorTextureCommand());
					Scene.getInstance().setTextureMode(TextureMode.Full);
					Scene.getInstance().setEdited(true);
					if (MainPanel.getInstance().getEnergyViewButton().isSelected())
						MainPanel.getInstance().getEnergyViewButton().setSelected(false);
				}
			});
			fullTextureMenuItem.setSelected(true);
			textureButtonGroup.add(fullTextureMenuItem);
		}
		return fullTextureMenuItem;
	}

	private JMenu getColorMenu() {
		if (colorMenu == null) {
			colorMenu = new JMenu("Building Colors");
			colorMenu.add(getWallColorMenuItem());
			colorMenu.add(getRoofColorMenuItem());
			colorMenu.add(getPlatformColorMenuItem());
			colorMenu.add(getDoorColorMenuItem());
			colorMenu.add(getFloorColorMenuItem());
		}
		return colorMenu;
	}

	private JMenuItem getPlatformColorMenuItem() {
		if (platformColorMenuItem == null) {
			platformColorMenuItem = new JMenuItem("Platform Color...");
			platformColorMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					showColorDialogForWholeHouse(Operation.DRAW_FOUNDATION);
				}
			});
		}
		return platformColorMenuItem;
	}

	private JMenuItem getWallColorMenuItem() {
		if (wallColorMenuItem == null) {
			wallColorMenuItem = new JMenuItem("Wall Color...");
			wallColorMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					showColorDialogForWholeHouse(Operation.DRAW_WALL);
				}
			});
		}
		return wallColorMenuItem;
	}

	private JMenuItem getDoorColorMenuItem() {
		if (doorColorMenuItem == null) {
			doorColorMenuItem = new JMenuItem("Door Color...");
			doorColorMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					showColorDialogForWholeHouse(Operation.DRAW_DOOR);
				}
			});
		}
		return doorColorMenuItem;
	}

	private JMenuItem getFloorColorMenuItem() {
		if (floorColorMenuItem == null) {
			floorColorMenuItem = new JMenuItem("Floor Color...");
			floorColorMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					showColorDialogForWholeHouse(Operation.DRAW_FLOOR);
				}
			});
		}
		return floorColorMenuItem;
	}

	private JMenuItem getRoofColorMenuItem() {
		if (roofColorMenuItem == null) {
			roofColorMenuItem = new JMenuItem("Roof Color...");
			roofColorMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					showColorDialogForWholeHouse(Operation.DRAW_ROOF_PYRAMID);
				}
			});
		}
		return roofColorMenuItem;
	}

	Foundation autoSelectBuilding(final boolean ask) {
		Foundation foundation = null;
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart == null || selectedPart instanceof Tree || selectedPart instanceof Human) {
			SceneManager.getInstance().setSelectedPart(null);
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
				foundation = (Foundation) hp;
			} else {
				if (ask)
					JOptionPane.showMessageDialog(this, "There are multiple buildings. You must select a building first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
			}
		} else {
			final HousePart topContainer = selectedPart.getTopContainer();
			if (selectedPart instanceof Foundation) {
				foundation = (Foundation) selectedPart;
			} else if (topContainer instanceof Foundation) {
				selectedPart.setEditPointsVisible(false);
				SceneManager.getInstance().setSelectedPart(topContainer);
				foundation = (Foundation) topContainer;
			} else {
				if (ask)
					JOptionPane.showMessageDialog(this, "You must select a building first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		return foundation;
	}

	void showColorDialogForIndividualPart() {
		if (!noTextureMenuItem.isSelected()) { // when the user wants to set the color, automatically switch to no texture
			if (JOptionPane.showConfirmDialog(this, "To set color for an individual part, we have to remove the texture. Is that OK?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION)
				return;
			noTextureMenuItem.setSelected(true);
			Scene.getInstance().setTextureMode(TextureMode.None);
		}
		ReadOnlyColorRGBA color = ColorRGBA.WHITE;
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart != null)
			color = selectedPart.getColor();
		colorChooser.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
		final ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Color c = colorChooser.getColor();
				final float[] newColor = c.getComponents(null);
				final boolean restartPrintPreview = Scene.getInstance().getRoofColor().equals(ColorRGBA.WHITE) || c.equals(Color.WHITE);
				final ColorRGBA color = new ColorRGBA(newColor[0], newColor[1], newColor[2], newColor[3]);
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (selectedPart != null) {
					selectedPart.setColor(color);
					Scene.getInstance().setTextureMode(Scene.getInstance().getTextureMode());
				}
				if (restartPrintPreview && PrintController.getInstance().isPrintPreview())
					PrintController.getInstance().restartAnimation();
				MainPanel.getInstance().getEnergyViewButton().setSelected(false);
				Scene.getInstance().setEdited(true);
			}
		};
		JColorChooser.createDialog(this, "Select Color", true, colorChooser, actionListener, null).setVisible(true);
		SceneManager.getInstance().getUndoManager().addEdit(new ChangeColorTextureCommand());
	}

	private int countFloors(final Foundation foundation) {
		int n = 0;
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p.getTopContainer() == foundation && p instanceof Floor)
				n++;
		}
		return n;
	}

	private int countDoors(final Foundation foundation) {
		int n = 0;
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p.getTopContainer() == foundation && p instanceof Door)
				n++;
		}
		return n;
	}

	private void showColorDialogForWholeHouse(final Operation operation) {
		if (fullTextureMenuItem.isSelected()) { // when the user wants to set the color, remove the texture first
			if (JOptionPane.showConfirmDialog(this, "To set colors for this building, we have to remove the full texture. Is that OK?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION)
				return;
			noTextureMenuItem.setSelected(true);
			Scene.getInstance().setTextureMode(TextureMode.None);
		}
		final Foundation foundation = autoSelectBuilding(true);
		if (foundation == null)
			return;
		switch (operation) {
		case DRAW_WALL:
			if (JOptionPane.showConfirmDialog(this, "<html>This will set color for all walls of the selected building.<br>Do you want to continue?</html>", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.NO_OPTION)
				return;
			break;
		case DRAW_DOOR:
			if (countDoors(foundation) == 0) {
				JOptionPane.showMessageDialog(this, "<html>There is no door for the selected building.</html>", "Message", JOptionPane.INFORMATION_MESSAGE);
				return;
			} else if (countDoors(foundation) > 1) {
				if (JOptionPane.showConfirmDialog(this, "<html>This will set color for all doors of the selected building.<br>Do you want to continue?</html>", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.NO_OPTION)
					return;
			}
			break;
		case DRAW_FLOOR:
			if (countFloors(foundation) == 0) {
				JOptionPane.showMessageDialog(this, "<html>There is no floor for the selected building.</html>", "Message", JOptionPane.INFORMATION_MESSAGE);
				return;
			} else if (countFloors(foundation) > 1) {
				if (JOptionPane.showConfirmDialog(this, "<html>This will set color for all floors of the selected building.<br>Do you want to continue?</html>", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.NO_OPTION)
					return;
			}
			break;
		default:
			break;
		}

		ReadOnlyColorRGBA color;
		switch (operation) {
		case DRAW_FOUNDATION:
			color = Scene.getInstance().getFoundationColor();
			break;
		case DRAW_WALL:
			color = Scene.getInstance().getWallColor();
			break;
		case DRAW_DOOR:
			color = Scene.getInstance().getDoorColor();
			break;
		case DRAW_FLOOR:
			color = Scene.getInstance().getFloorColor();
			break;
		case DRAW_ROOF_PYRAMID:
			color = Scene.getInstance().getRoofColor();
			break;
		default:
			color = ColorRGBA.WHITE;
		}
		colorChooser.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
		final ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Color c = colorChooser.getColor();
				final float[] newColor = c.getComponents(null);
				final boolean restartPrintPreview = Scene.getInstance().getRoofColor().equals(ColorRGBA.WHITE) || c.equals(Color.WHITE);
				final ColorRGBA color = new ColorRGBA(newColor[0], newColor[1], newColor[2], newColor[3]);
				switch (operation) {
				case DRAW_FOUNDATION:
					// Scene.getInstance().setFoundationColor(color);
					foundation.setColor(color);
					break;
				case DRAW_WALL:
					// Scene.getInstance().setWallColor(color);
					for (final HousePart p : Scene.getInstance().getParts()) {
						if (p instanceof Wall && p.getTopContainer() == foundation)
							p.setColor(color);
					}
					break;
				case DRAW_DOOR:
					// Scene.getInstance().setDoorColor(color);
					for (final HousePart p : Scene.getInstance().getParts()) {
						if (p instanceof Door && p.getTopContainer() == foundation)
							p.setColor(color);
					}
					break;
				case DRAW_FLOOR:
					// Scene.getInstance().setFloorColor(color);
					for (final HousePart p : Scene.getInstance().getParts()) {
						if (p instanceof Floor && p.getTopContainer() == foundation)
							p.setColor(color);
					}
					break;
				case DRAW_ROOF_PYRAMID:
					// Scene.getInstance().setRoofColor(color);
					for (final HousePart p : Scene.getInstance().getParts()) {
						if (p instanceof Roof && p.getTopContainer() == foundation)
							p.setColor(color);
					}
					break;
				default:
					break;
				}
				Scene.getInstance().setTextureMode(Scene.getInstance().getTextureMode());
				if (restartPrintPreview && PrintController.getInstance().isPrintPreview())
					PrintController.getInstance().restartAnimation();
				MainPanel.getInstance().getEnergyViewButton().setSelected(false);
				Scene.getInstance().setEdited(true);
			}
		};
		JColorChooser.createDialog(this, "Select Color", true, colorChooser, actionListener, null).setVisible(true);
		SceneManager.getInstance().getUndoManager().addEdit(new ChangeColorTextureCommand());
	}

	public void showUnexpectedErrorMessage(final Throwable err) {
		err.printStackTrace();
		final String message = err.getMessage();
		JOptionPane.showMessageDialog(this, message != null ? message : "Unexpected error occured!", "Error", JOptionPane.ERROR_MESSAGE);
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
			showUnexpectedErrorMessage(e);
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
					SceneManager.getInstance().exit();
			} else if (save != JOptionPane.CANCEL_OPTION)
				SceneManager.getInstance().exit();
		} else
			SceneManager.getInstance().exit();
	}

	private void save() {
		try {
			final URL url = Scene.getURL();
			if (url != null)
				Scene.save(url, false);
			else
				saveFile();
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

	private JMenuItem getLockAllMenuItem() {
		if (lockAllMenuItem == null) {
			lockAllMenuItem = new JMenuItem("Lock All");
			lockAllMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().lockAll(true);
				}
			});
		}
		return lockAllMenuItem;
	}

	private JMenuItem getUnlockAllMenuItem() {
		if (unlockAllMenuItem == null) {
			unlockAllMenuItem = new JMenuItem("Unlock All");
			unlockAllMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().lockAll(false);
				}
			});
		}
		return unlockAllMenuItem;
	}

	private JMenuItem getLockSelectionMenuItem() {
		if (lockSelectionMenuItem == null) {
			lockSelectionMenuItem = new JMenuItem("Lock Selection");
			lockSelectionMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().lockSelection(true);
				}
			});
		}
		return lockSelectionMenuItem;
	}

	private JCheckBoxMenuItem getDisableFoundationCheckBoxMenuItem() {
		if (disableFoundationCheckBoxMenuItem == null) {
			disableFoundationCheckBoxMenuItem = new JCheckBoxMenuItem("Disable Foundation Edits");
			disableFoundationCheckBoxMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final HousePart selected = SceneManager.getInstance().getSelectedPart();
					if (selected instanceof Foundation) {
						((Foundation) selected).setLockEdit(disableFoundationCheckBoxMenuItem.isSelected());
					}
				}
			});
		}
		return disableFoundationCheckBoxMenuItem;
	}

	private JMenuItem getSpecificationsMenuItem() {
		if (specificationsMenuItem == null) {
			specificationsMenuItem = new JMenuItem("Specifications");
			specificationsMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					new SpecsDialog().setVisible(true);
				}
			});
		}
		return specificationsMenuItem;
	}

	private JCheckBoxMenuItem getNoteCheckBoxMenuItem() {
		if (noteCheckBoxMenuItem == null) {
			noteCheckBoxMenuItem = new JCheckBoxMenuItem("Show Note");
			noteCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke("F3"));
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
					Scene.getInstance().removeAllRoofs();
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							MainPanel.getInstance().getEnergyViewButton().setSelected(false);
						}
					});
				}
			});
		}
		return removeAllRoofsMenuItem;
	}
}
