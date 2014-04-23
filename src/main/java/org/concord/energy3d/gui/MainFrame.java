package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import java.util.concurrent.Callable;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
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
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.logger.PostProcessor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.scene.PrintController;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.Scene.Unit;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.CameraMode;
import org.concord.energy3d.scene.SceneManager.Operation;
import org.concord.energy3d.scene.SceneManager.ViewMode;
import org.concord.energy3d.simulation.Cost;
import org.concord.energy3d.simulation.EnergyAnalysis;
import org.concord.energy3d.simulation.EnergyDensityAnalysis;
import org.concord.energy3d.undo.ChangeColorTextureCommand;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.Printout;
import org.concord.energy3d.util.Util;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final MainFrame instance = new MainFrame();
	private JMenuBar appMenuBar = null;
	private JMenu fileMenu = null;
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
	private JMenuItem seasonalEnergyAnalysisMenuItem;
	private JMenuItem seasonalEnergyDensityAnalysisMenuItem;
	private JMenuItem constructionCostAnalysisMenuItem;
	private JMenuItem dailyAnalysisMenuItem;
	private JCheckBoxMenuItem axesMenuItem;
	private JCheckBoxMenuItem shadowMenuItem;
	private JCheckBoxMenuItem buildingLabelsMenuItem;
	protected Object lastSelection;
	private JCheckBoxMenuItem shadeMenuItem = null;
	private JMenuItem exitMenuItem = null;
	private JMenu helpMenu = null;
	private JMenuItem aboutMenuItem = null;
	private JDialog aboutDialog = null;
	private JCheckBoxMenuItem wallThicknessMenuItem = null;
	private MainPanel mainPanel = null;
	private JCheckBoxMenuItem annotationsInwardMenuItem;
	private JMenu editMenu;
	private JMenuItem undoMenuItem;
	private JMenuItem redoMenuItem;
	private JMenuItem pageSetupMenuItem;
	private JRadioButtonMenuItem scaleToFitRadioButtonMenuItem;
	private JRadioButtonMenuItem exactSizeRadioButtonMenuItem;
	private final ButtonGroup buttonGroup_1 = new ButtonGroup();
	private JMenuItem importMenuItem;
	private JCheckBoxMenuItem snapMenuItem;
	private JCheckBoxMenuItem gridsMenuItem;
	private JCheckBoxMenuItem topViewCheckBoxMenuItem;
	private JMenuItem roofOverhangLengthMenuItem;
	private JRadioButtonMenuItem noTextureMenuItem;
	private JRadioButtonMenuItem simpleTextureMenuItem;
	private JRadioButtonMenuItem fullTextureMenuItem;
	private final ButtonGroup buttonGroup_2 = new ButtonGroup();
	private JMenu colorMenu;
	private JMenuItem platformColorMenuItem;
	private JMenuItem wallColorMenuItem;
	private JMenuItem doorColorMenuItem;
	private JMenuItem floorColorMenuItem;
	private JMenuItem roofColorMenuItem;
	private JMenuItem importColladaMenuItem;
	private JMenuItem saveAsImageMenuItem;
	private JMenuItem lockAllMenuItem;
	private JMenuItem unlockAllMenuItem;
	private JMenuItem lockSelectionMenuItem;
	private JCheckBoxMenuItem noteCheckBoxMenuItem;

	private final JFileChooser fileChooser;
	private final JColorChooser colorChooser;
	private final ExtensionFileFilter ng3Filter = new ExtensionFileFilter("Energy3D Project (*.ng3)", "ng3");
	private final ExtensionFileFilter pngFilter = new ExtensionFileFilter("Image (*.png)", "png");
	private final ExtensionFileFilter daeFilter = new ExtensionFileFilter("Collada (*.dae)", "dae");
	private JCheckBoxMenuItem keepHeatmapOnMenuItem;
	private JMenuItem removeAllRoofsMenuItem;

	private static class ExtensionFileFilter extends javax.swing.filechooser.FileFilter {
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

	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final MainFrame thisClass = new MainFrame();
				thisClass.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				thisClass.setVisible(true);
			}
		});
	}

	private MainFrame() {
		super();
		System.out.print("Initiating GUI...");
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icons/icon.png")));
		final String directoryPath = Preferences.userNodeForPackage(MainApplication.class).get("dir", null);
		fileChooser = new JFileChooser(directoryPath);
		if (!Config.isWebStart() && directoryPath == null) {
			final File dir = new File(System.getProperties().getProperty("user.dir") + "/Energy3D Projects");
			fileChooser.setCurrentDirectory(dir);
		}
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.addChoosableFileFilter(ng3Filter);
		fileChooser.setFileFilter(ng3Filter);
		colorChooser = new JColorChooser();
		initialize();
		System.out.println("done");
	}

	private void initialize() {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setTitle("Energy3D v" + Config.VERSION);

		setJMenuBar(getAppMenuBar());
		setContentPane(getMainPanel());

		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(Math.min(Preferences.userNodeForPackage(MainApplication.class).getInt("window_size_width", Math.max(900, MainPanel.getInstance().getAppToolbar().getPreferredSize().width)), screenSize.width), Math.min(Preferences.userNodeForPackage(MainApplication.class).getInt("window_size_height", 600), screenSize.height));
		this.setLocation((int) (screenSize.getWidth() - this.getSize().getWidth()) / 2, (int) (screenSize.getHeight() - this.getSize().getHeight()) / 2);
		this.setLocation(Preferences.userNodeForPackage(MainApplication.class).getInt("window_location_x", (int) (screenSize.getWidth() - this.getSize().getWidth()) / 2), Preferences.userNodeForPackage(MainApplication.class).getInt("window_location_y", (int) (screenSize.getHeight() - this.getSize().getHeight()) / 2));
		this.setLocation(MathUtils.clamp(this.getLocation().x, 0, screenSize.width - this.getSize().width), MathUtils.clamp(this.getLocation().y, 0, screenSize.height - this.getSize().height));
		final int windowState = Preferences.userNodeForPackage(MainApplication.class).getInt("window_state", JFrame.NORMAL);
		if ((windowState & JFrame.ICONIFIED) == 0)
			setExtendedState(windowState);

		if (Config.isMac()) {
			final Application anApp = new Application();
			anApp.setEnabledPreferencesMenu(true);
			anApp.addApplicationListener(new ApplicationAdapter() {
				@Override
				public void handleQuit(final ApplicationEvent e) {
					e.setHandled(true);
					exit();
				}

				@Override
				public void handlePreferences(final ApplicationEvent e) {
					e.setHandled(true);
				}

				@Override
				public void handleAbout(final ApplicationEvent e) {
					showAbout();
					e.setHandled(true);
				}

				@Override
				public void handleOpenFile(final ApplicationEvent e) {
					open(e.getFilename());
					e.setHandled(true);
				}
			});
		}

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(final ComponentEvent e) {
				if (MainFrame.this.getExtendedState() == 0) {
					Preferences.userNodeForPackage(MainApplication.class).putInt("window_location_x", e.getComponent().getLocation().x);
					Preferences.userNodeForPackage(MainApplication.class).putInt("window_location_y", e.getComponent().getLocation().y);
				}
			}

			@Override
			public void componentResized(final ComponentEvent e) {
				if (MainFrame.this.getExtendedState() == 0) {
					Preferences.userNodeForPackage(MainApplication.class).putInt("window_size_width", e.getComponent().getSize().width);
					Preferences.userNodeForPackage(MainApplication.class).putInt("window_size_height", e.getComponent().getSize().height);
				}
			}
		});

		addWindowStateListener(new WindowStateListener() {
			@Override
			public void windowStateChanged(final WindowEvent e) {
				Preferences.userNodeForPackage(MainApplication.class).putInt("window_state", e.getNewState());
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
					MainFrame.getInstance().deselect();
				}
			});
			fileMenu.setText("File");
			fileMenu.add(getNewMenuItem());
			fileMenu.add(getOpenMenuItem());
			fileMenu.add(getSaveMenuItem());
			fileMenu.add(getSaveasMenuItem());
			fileMenu.add(getSaveAsImageMenuItem());
			fileMenu.addSeparator();
			fileMenu.add(getImportMenuItem());
			fileMenu.add(getImportColladaMenuItem());
			fileMenu.addSeparator();
			if (!Config.isRestrictMode()) {
				fileMenu.add(getOpenFolderMenuItem());
				fileMenu.add(getAnalyzeFolderMenuItem());
				fileMenu.addSeparator();
			}
			fileMenu.add(getScaleToFitRadioButtonMenuItem());
			fileMenu.add(getExactSizeRadioButtonMenuItem());
			fileMenu.addSeparator();
			fileMenu.add(getPageSetupMenuItem());
			fileMenu.add(getPreviewMenuItem());
			fileMenu.add(getPrintMenuItem());
			fileMenu.addSeparator();
			fileMenu.add(getExitMenuItem());
		}
		return fileMenu;
	}

	private JMenuItem getNewMenuItem() {
		if (newMenuItem == null) {
			newMenuItem = new JMenuItem("New");
			newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Config.isMac() ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK));
			newMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.newFile();
					SceneManager.getInstance().resetCamera(ViewMode.NORMAL);
					SceneManager.getInstance().getCameraControl().reset();
					updateTitleBar();
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
						final int save = JOptionPane.showConfirmDialog(MainFrame.this, "Do you want to save changes?", "Save", JOptionPane.YES_NO_CANCEL_OPTION);
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
		fileChooser.setFileFilter(ng3Filter);
		if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
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
						updateTitleBar();
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
					if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(MainFrame.this, "This feature is for researchers only. Are you sure you want to continue?", "Research Mode", JOptionPane.YES_NO_OPTION))
						return;
					SceneManager.getInstance().refresh(1);
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					fileChooser.removeChoosableFileFilter(ng3Filter);
					fileChooser.removeChoosableFileFilter(pngFilter);
					fileChooser.removeChoosableFileFilter(daeFilter);
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
					if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(MainFrame.this, "This feature is for researchers only. Are you sure you want to continue?", "Research Mode", JOptionPane.YES_NO_OPTION))
						return;
					SceneManager.getInstance().refresh(1);
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					fileChooser.removeChoosableFileFilter(ng3Filter);
					fileChooser.removeChoosableFileFilter(pngFilter);
					fileChooser.removeChoosableFileFilter(daeFilter);
					if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
						Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getSelectedFile().getParent());
						final File dir = fileChooser.getSelectedFile();
						if (dir.isDirectory()) {
							PostProcessor.open(dir.listFiles(new FilenameFilter() {
								@Override
								public boolean accept(final File dir, final String name) {
									return name.endsWith(".ng3");
								}
							}), new Runnable() {
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
						MainFrame.getInstance().getPreviewMenuItem().setSelected(true);
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

	public JCheckBoxMenuItem getShadeMenuItem() {
		if (shadeMenuItem == null) {
			shadeMenuItem = new JCheckBoxMenuItem();
			shadeMenuItem.setText("Shade");
			shadeMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					SceneManager.getInstance().setShading(shadeMenuItem.isSelected());
				}
			});
		}
		return shadeMenuItem;
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

	private void showAbout() {
		final JDialog aboutDialog = getAboutDialog();
		final Dimension frameSize = MainFrame.this.getSize();
		final Dimension dialogSize = aboutDialog.getSize();
		final Point location = MainFrame.this.getLocation();
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
			JButton button = new JButton("Thanks!");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					aboutDialog.dispose();
				}
			});
			JPanel p2 = new JPanel();
			p2.add(button);
			p.add(p2, BorderLayout.SOUTH);
			aboutDialog.setContentPane(p);
			aboutDialog.pack();
		}
		return aboutDialog;
	}

	private JCheckBoxMenuItem getWallThicknessMenuItem() {
		if (wallThicknessMenuItem == null) {
			wallThicknessMenuItem = new JCheckBoxMenuItem();
			wallThicknessMenuItem.setText("Draw Wall Thickness");
			wallThicknessMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					Scene.getInstance().setDrawThickness(wallThicknessMenuItem.isSelected());
				}
			});
		}
		return wallThicknessMenuItem;
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
			analysisMenu.add(getSeasonalEnergyAnalysisMenuItem());
			analysisMenu.add(getSeasonalEnergyDensityAnalysisMenuItem());
			analysisMenu.add(getDailyAnalysisMenuItem());
			analysisMenu.add(getConstructionCostAnalysisMenuItem());
			if (!Config.isRestrictMode()) {
				analysisMenu.addSeparator();
				analysisMenu.add(getSimulationSettingsMenuItem());
				analysisMenu.add(getKeepHeatmapOnMenuItem());
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
					axesMenuItem.setSelected(SceneManager.getInstance().areAxesShown());
					buildingLabelsMenuItem.setSelected(SceneManager.getInstance().areBuildingLabelsShown());
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
			viewMenu.add(getAxesMenuItem());
			viewMenu.add(getShadeMenuItem());
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
					Util.selectSilently(MainPanel.getInstance().getLightButton(), shadowMenuItem.isSelected());
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

	private JMenuItem getSeasonalEnergyAnalysisMenuItem() {
		if (seasonalEnergyAnalysisMenuItem == null) {
			seasonalEnergyAnalysisMenuItem = new JMenuItem("Run Seasonal Energy Analysis...");
			seasonalEnergyAnalysisMenuItem.setAccelerator(KeyStroke.getKeyStroke("F4"));
			seasonalEnergyAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null) {
						int count = 0;
						HousePart hp = null;
						synchronized (Scene.getInstance().getParts()) {
							for (HousePart x : Scene.getInstance().getParts()) {
								if (x instanceof Foundation) {
									count++;
									hp = x;
								}
							}
						}
						if (count == 1) {
							SceneManager.getInstance().setSelectedPart(hp);
						} else {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "You must select a building or a component first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
							return;
						}
					} else if (selectedPart instanceof Tree) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Energy analysis is not applicable to a tree.", "Not Applicable", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					new EnergyAnalysis().show("Seasonal Energy");
				}
			});
		}
		return seasonalEnergyAnalysisMenuItem;
	}

	private JMenuItem getSeasonalEnergyDensityAnalysisMenuItem() {
		if (seasonalEnergyDensityAnalysisMenuItem == null) {
			seasonalEnergyDensityAnalysisMenuItem = new JMenuItem("Run Seasonal Energy Density Analysis...");
			seasonalEnergyDensityAnalysisMenuItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
			seasonalEnergyDensityAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "You must select a component first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					if (selectedPart instanceof Tree) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Energy density analysis is not applicable to a tree.", "Not Applicable", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					new EnergyDensityAnalysis().show("Seasonal Energy Density");
				}
			});
		}
		return seasonalEnergyDensityAnalysisMenuItem;
	}

	private JMenuItem getDailyAnalysisMenuItem() {
		if (dailyAnalysisMenuItem == null) {
			dailyAnalysisMenuItem = new JMenuItem("Run Daily Energy Analysis...");
			dailyAnalysisMenuItem.setAccelerator(KeyStroke.getKeyStroke("F6"));
			dailyAnalysisMenuItem.setEnabled(false);
			dailyAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "You must select a building or a component first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
				}
			});
		}
		return dailyAnalysisMenuItem;
	}

	private JMenuItem getConstructionCostAnalysisMenuItem() {
		if (constructionCostAnalysisMenuItem == null) {
			constructionCostAnalysisMenuItem = new JMenuItem("Run Construction Cost Analysis...");
			constructionCostAnalysisMenuItem.setAccelerator(KeyStroke.getKeyStroke("F7"));
			constructionCostAnalysisMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null) {
						int count = 0;
						HousePart hp = null;
						synchronized (Scene.getInstance().getParts()) {
							for (HousePart x : Scene.getInstance().getParts()) {
								if (x instanceof Foundation) {
									count++;
									hp = x;
								}
							}
						}
						if (count == 1) {
							if (hp.getChildren().isEmpty()) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no building on this platform.", "No Building", JOptionPane.INFORMATION_MESSAGE);
								return;
							} else {
								SceneManager.getInstance().setSelectedPart(hp);
								EnergyPanel.getInstance().updateCost();
							}
						} else {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "You must select a building first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
							return;
						}
					} else if (selectedPart instanceof Tree) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Cost analysis is not applicable to a tree.", "Not Applicable", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					Cost.getInstance().show();
				}
			});
		}
		return constructionCostAnalysisMenuItem;
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
					lockSelectionMenuItem.setEnabled(SceneManager.getInstance().getSelectedPart() != null);
					Util.selectSilently(noteCheckBoxMenuItem, MainPanel.getInstance().isNoteVisible());
					mainPanel.getSelectButton().setSelected(true);
					SceneManager.getInstance().setOperation(SceneManager.Operation.SELECT);
				}
			});
			editMenu.add(getUndoMenuItem());
			editMenu.add(getRedoMenuItem());
			editMenu.addSeparator();
			editMenu.add(getRescaleMenuItem());
			editMenu.addSeparator();
			editMenu.add(getGridsMenuItem());
			editMenu.add(getSnapMenuItem());
			editMenu.addSeparator();
			editMenu.add(getRoofOverhangLengthMenuItem());
			editMenu.add(getRemoveAllRoofsMenuItem());
			editMenu.addSeparator();
			if (!Config.isRestrictMode()) {
				editMenu.add(getLockSelectionMenuItem());
				editMenu.add(getLockAllMenuItem());
				editMenu.add(getUnlockAllMenuItem());
			}
			editMenu.addSeparator();
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
		fileChooser.setFileFilter(ng3Filter);
		if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
			Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getSelectedFile().getParent());
			try {
				File file = fileChooser.getSelectedFile();
				if (!file.getName().toLowerCase().endsWith(".ng3"))
					file = new File(file.toString() + ".ng3");
				boolean doIt = true;
				if (file.exists()) {
					if (JOptionPane.showConfirmDialog(MainFrame.this, "File " + file + " exists. Do you want to overwrite it?", "Overwrite", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
						doIt = false;
					}
				}
				if (doIt) {
					Scene.save(file.toURI().toURL(), true);
					updateTitleBar();
				}
			} catch (final Throwable err) {
				err.printStackTrace();
				JOptionPane.showMessageDialog(MainFrame.this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void importFile() {
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.addChoosableFileFilter(ng3Filter);
		fileChooser.removeChoosableFileFilter(pngFilter);
		fileChooser.removeChoosableFileFilter(daeFilter);
		fileChooser.setFileFilter(ng3Filter);
		if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
			Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getSelectedFile().getParent());
			try {
				Scene.importFile(fileChooser.getSelectedFile().toURI().toURL());
			} catch (final Throwable err) {
				err.printStackTrace();
				JOptionPane.showMessageDialog(MainFrame.this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void importColladaFile() {
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.removeChoosableFileFilter(pngFilter);
		fileChooser.removeChoosableFileFilter(ng3Filter);
		fileChooser.addChoosableFileFilter(daeFilter);
		fileChooser.setFileFilter(daeFilter);
		if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
			Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getSelectedFile().getParent());
			try {
				SceneManager.getInstance().newImport(fileChooser.getSelectedFile().toURI().toURL());
			} catch (final Throwable err) {
				err.printStackTrace();
				JOptionPane.showMessageDialog(MainFrame.this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
			buttonGroup_1.add(scaleToFitRadioButtonMenuItem);
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
			buttonGroup_1.add(exactSizeRadioButtonMenuItem);
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

	private JCheckBoxMenuItem getTopViewCheckBoxMenuItem() {
		if (topViewCheckBoxMenuItem == null) {
			topViewCheckBoxMenuItem = new JCheckBoxMenuItem("Top View");
			topViewCheckBoxMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final boolean isTopView = topViewCheckBoxMenuItem.isSelected();
					if (isTopView)
						Scene.saveCameraLocation();
					SceneManager.getInstance().resetCamera(isTopView ? ViewMode.TOP_VIEW : ViewMode.NORMAL);
					if (!isTopView)
						Scene.loadCameraLocation();
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
				}
			});
			buttonGroup_2.add(noTextureMenuItem);
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
				}
			});
			buttonGroup_2.add(simpleTextureMenuItem);
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
				}
			});
			fullTextureMenuItem.setSelected(true);
			buttonGroup_2.add(fullTextureMenuItem);
		}
		return fullTextureMenuItem;
	}

	private JMenu getColorMenu() {
		if (colorMenu == null) {
			colorMenu = new JMenu("Color");
			colorMenu.add(getRoofColorMenuItem());
			colorMenu.add(getFloorColorMenuItem());
			colorMenu.add(getDoorColorMenuItem());
			colorMenu.add(getWallColorMenuItem());
			colorMenu.add(getPlatformColorMenuItem());
		}
		return colorMenu;
	}

	private JMenuItem getPlatformColorMenuItem() {
		if (platformColorMenuItem == null) {
			platformColorMenuItem = new JMenuItem("Platform Color...");
			platformColorMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					showColorDialogForHousePart(Operation.DRAW_FOUNDATION);
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
					showColorDialogForHousePart(Operation.DRAW_WALL);
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
					showColorDialogForHousePart(Operation.DRAW_DOOR);
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
					showColorDialogForHousePart(Operation.DRAW_FLOOR);
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
					showColorDialogForHousePart(Operation.DRAW_ROOF_PYRAMID);
				}
			});
		}
		return roofColorMenuItem;
	}

	private void showColorDialogForHousePart(final Operation operation) {
		final ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Color c = colorChooser.getColor();
				final float[] newColor = c.getComponents(null);
				final boolean restartPrintPreview = Scene.getInstance().getRoofColor().equals(ColorRGBA.WHITE) || c.equals(Color.WHITE);
				final ColorRGBA color = new ColorRGBA(newColor[0], newColor[1], newColor[2], newColor[3]);
				switch (operation) {
				case DRAW_FOUNDATION:
					Scene.getInstance().setFoundationColor(color);
					break;
				case DRAW_WALL:
					Scene.getInstance().setWallColor(color);
					break;
				case DRAW_DOOR:
					Scene.getInstance().setDoorColor(color);
					break;
				case DRAW_FLOOR:
					Scene.getInstance().setFloorColor(color);
					break;
				case DRAW_ROOF_PYRAMID:
					Scene.getInstance().setRoofColor(color);
					break;
				default:
					break;
				}
				Scene.getInstance().setTextureMode(Scene.getInstance().getTextureMode());

				if (restartPrintPreview && PrintController.getInstance().isPrintPreview())
					PrintController.getInstance().restartAnimation();

			}
		};
		SceneManager.getInstance().getUndoManager().addEdit(new ChangeColorTextureCommand());
		if (fullTextureMenuItem.isSelected()) {
			noTextureMenuItem.setSelected(true);
			Scene.getInstance().setTextureMode(TextureMode.None);
		}

		final ReadOnlyColorRGBA color;
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
		final JDialog colorDialog = JColorChooser.createDialog(MainFrame.this, "Select House Color", true, colorChooser, actionListener, null);
		colorDialog.setVisible(true);
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
					updateTitleBar();
					return null;
				}
			});
		} catch (final Throwable e) {
			showUnexpectedErrorMessage(e);
		}
	}

	private void exit() {
		if (Scene.getInstance().isEdited()) {
			final int save = JOptionPane.showConfirmDialog(this, "Do you want to save changes?", "Save", JOptionPane.YES_NO_CANCEL_OPTION);
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
			JOptionPane.showMessageDialog(MainFrame.this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

	private JMenuItem getSaveAsImageMenuItem() {
		if (saveAsImageMenuItem == null) {
			saveAsImageMenuItem = new JMenuItem("Save As Image...");
			saveAsImageMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					saveAsImage();
				}
			});
		}
		return saveAsImageMenuItem;
	}

	private void saveAsImage() {
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.addChoosableFileFilter(pngFilter);
		fileChooser.removeChoosableFileFilter(ng3Filter);
		fileChooser.removeChoosableFileFilter(daeFilter);
		fileChooser.setFileFilter(pngFilter);
		if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
			System.out.print("Saving snapshot: ");
			Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getSelectedFile().getParent());
			try {
				File file = fileChooser.getSelectedFile();
				if (!file.getName().toLowerCase().endsWith(".png"))
					file = new File(file.toString() + ".png");
				System.out.print(file + "...");
				boolean doIt = true;
				if (file.exists()) {
					if (JOptionPane.showConfirmDialog(MainFrame.this, "File " + file + " exists. Do you want to overwrite it?", "Overwrite", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
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
				JOptionPane.showMessageDialog(MainFrame.this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

	private JCheckBoxMenuItem getKeepHeatmapOnMenuItem() {
		if (keepHeatmapOnMenuItem == null) {
			keepHeatmapOnMenuItem = new JCheckBoxMenuItem("Keep Heat Map On");
			keepHeatmapOnMenuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					EnergyPanel.setKeepHeatmapOn(keepHeatmapOnMenuItem.isSelected());
				}
			});
		}
		return keepHeatmapOnMenuItem;
	}

	private JMenuItem getRemoveAllRoofsMenuItem() {
		if (removeAllRoofsMenuItem == null) {
			removeAllRoofsMenuItem = new JMenuItem("Remove All Roofs");
			removeAllRoofsMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().removeAllRoofs();
				}
			});
		}
		return removeAllRoofsMenuItem;
	}
}
