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
import java.awt.event.KeyEvent;
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
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.PrintController;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.Scene.Unit;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.CameraMode;
import org.concord.energy3d.scene.SceneManager.Operation;
import org.concord.energy3d.scene.SceneManager.ViewMode;
import org.concord.energy3d.undo.ChangeColorTextureCommand;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.Printout;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final MainFrame instance = new MainFrame();
	private JFileChooser fileChooser;
	private final JColorChooser colorChooser;
	private JMenuBar appMenuBar = null;
	private JMenu fileMenu = null;
	private JMenuItem newMenuItem = null;
	private JMenuItem openMenuItem = null;
	private JMenuItem openFolderMenuItem = null;
	private JMenuItem saveMenuItem = null;
	private JMenuItem printMenuItem = null;
	private JCheckBoxMenuItem previewMenuItem = null;
	private JMenu cameraMenu = null;
	private JRadioButtonMenuItem orbitMenuItem = null;
	private JRadioButtonMenuItem firstPersonMenuItem = null;
	private JMenuItem saveasMenuItem;
	private JMenu sceneMenu;
	private JMenu unitsMenu;
	private JRadioButtonMenuItem metersRadioButtonMenuItem;
	private JRadioButtonMenuItem centimetersRadioButtonMenuItem;
	private JRadioButtonMenuItem inchesRadioButtonMenuItem;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JMenuItem scaleMenuItem;
	private JCheckBoxMenuItem shadowMenu;
	protected Object lastSelection;
	private JCheckBoxMenuItem shadeMenu = null;
	private JMenuItem exitMenuItem = null;
	private JMenu helpMenu = null;
	private JMenuItem aboutMenuItem = null;
	private JDialog aboutDialog = null;
	private JCheckBoxMenuItem wallThicknessMenuItem = null;
	private MainPanel mainPanel = null;
	private JCheckBoxMenuItem annotationsInward;
	private JMenu editMenu;
	private JMenuItem undoMenuItem;
	private JMenuItem redoMenuItem;
	private JMenuItem pageSetupMenuItem;
	private JSeparator separator;
	private JSeparator separator_1;
	private JRadioButtonMenuItem scaleToFitRadioButtonMenuItem;
	private JRadioButtonMenuItem exactSizeRadioButtonMenuItem;
	private JSeparator separator_2;
	private final ButtonGroup buttonGroup_1 = new ButtonGroup();
	private JMenuItem importMenuItem;
	private JSeparator separator_3;
	private JCheckBoxMenuItem snapCheckBoxMenuItem;
	private JCheckBoxMenuItem gridsCheckBoxMenuItem;
	private JSeparator separator_4;
	private JSeparator separator_5;
	private JSeparator separator_6;
	private JCheckBoxMenuItem topViewCheckBoxMenuItem;
	private JSeparator separator_7;
	private JSeparator separator_8;
	private JMenuItem roofOverhangLengthMenuItem;
	private JSeparator separator_9;
	private JRadioButtonMenuItem noTextureRadioButtonMenuItem;
	private JRadioButtonMenuItem simpleTextureRadioButtonMenuItem;
	private JRadioButtonMenuItem fullTextureRadioButtonMenuItem;
	private final ButtonGroup buttonGroup_2 = new ButtonGroup();
	private JMenu colorMenu;
	private JMenuItem platformColorMenuItem;
	private JMenuItem wallColorMenuItem;
	private JMenuItem doorColorMenuItem;
	private JMenuItem floorColorMenuItem;
	private JMenuItem roofColorMenuItem;
	private JMenuItem importColladaMenuItem;
	private JMenuItem saveAsImageMenuItem;
	private JMenuItem freezeMenuItem;
	private JMenuItem unfreezeMenuItem;
	private JSeparator separator_10;

	private final ExtensionFileFilter ng3Filter = new ExtensionFileFilter("Energy3D Project (*.ng3)", "ng3");
	private final ExtensionFileFilter serFilter = new ExtensionFileFilter("Old Energy3D Project (*.ser)", "ser");
	private final ExtensionFileFilter pngFilter = new ExtensionFileFilter("Image (*.png)", "png");

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

	/**
	 * This method initializes mainPanel
	 *
	 * @return org.concord.energy3d.gui.MainPanel
	 */
	public MainPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = MainPanel.getInstance();
			mainPanel.setMainFrame(this);
		}
		return mainPanel;
	}

	/**
	 * @param args
	 */
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

	/**
	 * This is the default constructor
	 */
	private MainFrame() {
		super();
		System.out.print("Initiating GUI...");
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("icons/icon.gif")));
		try {
			final String directoryPath = Preferences.userNodeForPackage(MainApplication.class).get("dir", null);
			fileChooser = new JFileChooser(directoryPath);
			if (!Config.isWebStart() && directoryPath == null) {
				final File dir = new File(System.getProperties().getProperty("user.dir") + "/Energy3D Projects");
				fileChooser.setCurrentDirectory(dir);
			}
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.addChoosableFileFilter(ng3Filter);
			fileChooser.setFileFilter(ng3Filter);
		} catch (final Exception e) {
			fileChooser = null;
			e.printStackTrace();
		}
		colorChooser = new JColorChooser();
		initialize();
		System.out.println("done");
	}

	/**
	 * This method initializes this
	 *
	 * @return void
	 */
	private void initialize() {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setTitle("Energy3D v" + Config.VERSION);

		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		this.setSize(Math.min(Preferences.userNodeForPackage(MainApplication.class).getInt("window_size_width", 900), screenSize.width), Math.min(Preferences.userNodeForPackage(MainApplication.class).getInt("window_size_height", 600), screenSize.height));
		this.setLocation((int) (screenSize.getWidth() - this.getSize().getWidth()) / 2, (int) (screenSize.getHeight() - this.getSize().getHeight()) / 2);
		this.setLocation(Preferences.userNodeForPackage(MainApplication.class).getInt("window_location_x", (int) (screenSize.getWidth() - this.getSize().getWidth()) / 2), Preferences.userNodeForPackage(MainApplication.class).getInt("window_location_y", (int) (screenSize.getHeight() - this.getSize().getHeight()) / 2));
		this.setLocation(MathUtils.clamp(this.getLocation().x, 0, screenSize.width - this.getSize().width), MathUtils.clamp(this.getLocation().y, 0, screenSize.height - this.getSize().height));
		final int windowState = Preferences.userNodeForPackage(MainApplication.class).getInt("window_state", JFrame.NORMAL);
		if ((windowState & JFrame.ICONIFIED) == 0)
			setExtendedState(windowState);

		setJMenuBar(getAppMenuBar());
		setContentPane(getMainPanel());

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

		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(final java.awt.event.WindowEvent e) {
				exit();
			}

			@Override
			public void windowDeiconified(final WindowEvent e) {
				SceneManager.getInstance().refresh();
			}

			@Override
			public void windowActivated(final WindowEvent arg0) {
				if (Config.EXPERIMENT)
					EnergyPanel.getInstance().initJavaFXGUI();
				SceneManager.getInstance().refresh();
			}
		});
	}

	/**
	 * This method initializes appMenuBar
	 *
	 * @return javax.swing.JMenuBar
	 */
	private JMenuBar getAppMenuBar() {
		if (appMenuBar == null) {
			appMenuBar = new JMenuBar();
			appMenuBar.add(getFileMenu());
			appMenuBar.add(getEditMenu());
			appMenuBar.add(getSceneMenu());
			appMenuBar.add(getCameraMenu());
			appMenuBar.add(getHelpMenu());
		}
		return appMenuBar;
	}

	/**
	 * This method initializes fileMenu
	 *
	 * @return javax.swing.JMenu
	 */
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
					mainPanel.getSelectButton().setSelected(true);
					SceneManager.getInstance().setOperation(SceneManager.Operation.SELECT);
				}
			});
			fileMenu.setText("File");
			fileMenu.add(getNewMenuItem());
			fileMenu.add(getOpenMenuItem());
			if (Config.isAssessmentMode())
				fileMenu.add(getOpenFolderMenuItem());
			fileMenu.add(getSaveMenuItem());
			fileMenu.add(getSaveasMenuItem());
			fileMenu.add(getSaveAsImageMenuItem());
			fileMenu.add(getSeparator());
			fileMenu.add(getImportMenuItem());
			// fileMenu.add(getImportColladaMenuItem());
			fileMenu.add(getSeparator_3());
			fileMenu.add(getScaleToFitRadioButtonMenuItem());
			fileMenu.add(getExactSizeRadioButtonMenuItem());
			fileMenu.add(getSeparator_2());
			fileMenu.add(getPageSetupMenuItem());
			fileMenu.add(getPreviewMenuItem());
			fileMenu.add(getPrintMenuItem());
			fileMenu.add(getSeparator_1());
			fileMenu.add(getExitMenuItem());
		}
		return fileMenu;
	}

	/**
	 * This method initializes newMenuItem
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getNewMenuItem() {
		if (newMenuItem == null) {
			newMenuItem = new JMenuItem("New");
			newMenuItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
					Scene.newFile();
					SceneManager.getInstance().resetCamera(ViewMode.NORMAL);
					SceneManager.getInstance().getCameraControl().reset();
					updateTitleBar();
				}
			});
		}
		return newMenuItem;
	}

	/**
	 * This method initializes openMenuItem
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getOpenMenuItem() {
		if (openMenuItem == null) {
			openMenuItem = new JMenuItem("Open...");
			openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Config.isMac() ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK));
			openMenuItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
					SceneManager.getInstance().refresh(1);
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fileChooser.addChoosableFileFilter(ng3Filter);
					fileChooser.addChoosableFileFilter(serFilter);
					fileChooser.removeChoosableFileFilter(pngFilter);
					fileChooser.setFileFilter(ng3Filter);
					if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
						Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getSelectedFile().getParent());
						File file = fileChooser.getSelectedFile();
						if (!file.getName().toLowerCase().endsWith(".ng3"))
							file = new File(file.toString() + ".ng3");
						try {
							Scene.open(file.toURI().toURL());
							updateTitleBar();
						} catch (final Throwable err) {
							showUnexpectedErrorMessage(err);
						}
					}
				}
			});
		}
		return openMenuItem;
	}

	private JMenuItem getOpenFolderMenuItem() {
		if (openFolderMenuItem == null) {
			openFolderMenuItem = new JMenuItem("Open Folder...");
			openFolderMenuItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
					SceneManager.getInstance().refresh(1);
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					fileChooser.removeChoosableFileFilter(ng3Filter);
					fileChooser.removeChoosableFileFilter(serFilter);
					fileChooser.removeChoosableFileFilter(pngFilter);
					if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
						Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getSelectedFile().getParent());
						final File dir = fileChooser.getSelectedFile();
						if (dir.isDirectory()) {
							final File[] files = dir.listFiles(new FilenameFilter() {
								@Override
								public boolean accept(final File dir, final String name) {
									return name.endsWith(".ng3");
								}
							});
							final int n = files.length;
							new Thread() {
								@Override
								public void run() {
									int i = -1;
									while (i < n) {
										if (Config.replaying) {
											i++;
											if (i == n) // cycle back
												i = 0;
											System.out.println("Play back " + i + " of " + n);
											try {
												Scene.open(files[i].toURI().toURL());
												updateTitleBar();
												sleep(1000);
											} catch (final Exception e) {
												e.printStackTrace();
											}
										} else {
											if (Config.backward) {
												if (i > 0) {
													i--;
													System.out.println("Play back " + i + " of " + n);
													try {
														Scene.open(files[i].toURI().toURL());
														updateTitleBar();
													} catch (final Exception e) {
														e.printStackTrace();
													}
												}
												Config.backward = false;
											} else if (Config.forward) {
												if (i < n - 1) {
													i++;
													System.out.println("Play back " + i + " of " + n);
													try {
														Scene.open(files[i].toURI().toURL());
														updateTitleBar();
													} catch (final Exception e) {
														e.printStackTrace();
													}
												}
												Config.forward = false;
											}
										}
									}
								}
							}.start();
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
			setTitle("Energy3D v" + Config.VERSION + star);
		else
			setTitle("Energy3D v" + Config.VERSION + " - " + new File(Scene.getURL().getFile()).toString().replaceAll("%20", " ") + star);
	}

	/**
	 * This method initializes saveMenuItem
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getSaveMenuItem() {
		if (saveMenuItem == null) {
			saveMenuItem = new JMenuItem("Save");
			saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Config.isMac() ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK));
			saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
					save();
				}
			});
		}
		return saveMenuItem;
	}

	/**
	 * This method initializes printMenuItem
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getPrintMenuItem() {
		if (printMenuItem == null) {
			printMenuItem = new JMenuItem("Print...");
			printMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Config.isMac() ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK));
			printMenuItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
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

	/**
	 * This method initializes previewMenuItem
	 *
	 * @return javax.swing.JCheckBoxMenuItem
	 */
	public JCheckBoxMenuItem getPreviewMenuItem() {
		if (previewMenuItem == null) {
			previewMenuItem = new JCheckBoxMenuItem("Print Preview");
			previewMenuItem.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(final java.awt.event.ItemEvent e) {
					mainPanel.getPreviewButton().setSelected(previewMenuItem.isSelected());
				}
			});
		}
		return previewMenuItem;
	}

	/**
	 * This method initializes cameraMenu
	 *
	 * @return javax.swing.JMenu
	 */
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
			cameraMenu.add(getSeparator_7());
			cameraMenu.add(getTopViewCheckBoxMenuItem());
		}
		return cameraMenu;
	}

	/**
	 * This method initializes orbitMenuItem
	 *
	 * @return javax.swing.JRadioButtonMenuItem
	 */
	private JRadioButtonMenuItem getOrbitMenuItem() {
		if (orbitMenuItem == null) {
			orbitMenuItem = new JRadioButtonMenuItem();
			orbitMenuItem.setText("Orbit");
			orbitMenuItem.setSelected(true);
			orbitMenuItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setCameraControl(CameraMode.ORBIT);
				}
			});
		}
		return orbitMenuItem;
	}

	/**
	 * This method initializes firstPersonMenuItem
	 *
	 * @return javax.swing.JRadioButtonMenuItem
	 */
	private JRadioButtonMenuItem getFirstPersonMenuItem() {
		if (firstPersonMenuItem == null) {
			firstPersonMenuItem = new JRadioButtonMenuItem();
			firstPersonMenuItem.setText("First Person");
			firstPersonMenuItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setCameraControl(CameraMode.FIRST_PERSON);
				}
			});
		}
		return firstPersonMenuItem;
	}

	/**
	 * This method initializes lightingMenu
	 *
	 * @return javax.swing.JCheckBoxMenuItem
	 */
	public JCheckBoxMenuItem getShadeMenu() {
		if (shadeMenu == null) {
			shadeMenu = new JCheckBoxMenuItem();
			shadeMenu.setText("Shade");
			shadeMenu.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(final java.awt.event.ItemEvent e) {
					SceneManager.getInstance().setShading(shadeMenu.isSelected());
				}
			});
		}
		return shadeMenu;
	}

	/**
	 * This method initializes exitMenuItem
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getExitMenuItem() {
		if (exitMenuItem == null) {
			exitMenuItem = new JMenuItem();
			exitMenuItem.setText("Exit");
			exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
					exit();
				}
			});
		}
		return exitMenuItem;
	}

	/**
	 * This method initializes helpMenu
	 *
	 * @return javax.swing.JMenu
	 */
	private JMenu getHelpMenu() {
		if (helpMenu == null) {
			helpMenu = new JMenu();
			helpMenu.setText("Help");
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

	/**
	 * This method initializes aboutMenuItem
	 *
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getAboutMenuItem() {
		if (aboutMenuItem == null) {
			aboutMenuItem = new JMenuItem();
			aboutMenuItem.setText("About");
			aboutMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					showAbout();
				}
			});
		}
		return aboutMenuItem;
	}

	/**
	 * This method initializes aboutDialog
	 *
	 * @return javax.swing.JDialog
	 */
	private JDialog getAboutDialog() {
		if (aboutDialog == null) {
			aboutDialog = new JDialog(this);
			aboutDialog.setTitle("About");
			final JPanel p = new JPanel(new BorderLayout());
			p.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
			p.add(new JLabel("<html><h2>Energy3D</h2><br>Version: " + Config.VERSION + "<hr><h3>Credit:</h3>This program is brought to you by:<ul><li>Dr. Saeid Nourian, Lead Developer<li>Dr. Charles Xie, Co-developer</ul><p>This program is licensed under the GNU Lesser General Public License V3.0<br>and based on Ardor3D. Funding of this project is provided by the National<br>Science Foundation under grant #0918449 to the Concord Consortium. </html>"), BorderLayout.CENTER);
			aboutDialog.setContentPane(p);
			aboutDialog.pack();
		}
		return aboutDialog;
	}

	/**
	 * This method initializes wallThicknessMenuItem
	 *
	 * @return javax.swing.JCheckBoxMenuItem
	 */
	private JCheckBoxMenuItem getWallThicknessMenuItem() {
		if (wallThicknessMenuItem == null) {
			wallThicknessMenuItem = new JCheckBoxMenuItem();
			wallThicknessMenuItem.setText("Draw Wall Thickness");
			wallThicknessMenuItem.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(final java.awt.event.ItemEvent e) {
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
			saveasMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					saveFile();
				}
			});
		}
		return saveasMenuItem;
	}

	private JMenu getSceneMenu() {
		if (sceneMenu == null) {
			sceneMenu = new JMenu("Scene");
			sceneMenu.addMenuListener(new MenuListener() {
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
			if (Config.isClassroomMode()) {
				sceneMenu.add(getFreezeMenuItem());
				sceneMenu.add(getUnfreezeMenuItem());
			}
			sceneMenu.add(getSeparator_10());
			sceneMenu.add(getUnitsMenu());
			sceneMenu.add(getScaleMenuItem());
			sceneMenu.add(getSeparator_8());
			sceneMenu.add(getRoofOverhangLengthMenuItem());
			sceneMenu.add(getSeparator_9());
			sceneMenu.add(getGridsCheckBoxMenuItem());
			sceneMenu.add(getSnapCheckBoxMenuItem());
			sceneMenu.add(getSeparator_4());
			sceneMenu.add(getShadeMenu());
			sceneMenu.add(getShadowMenu());
			sceneMenu.add(getSeparator_5());
			sceneMenu.add(getNoTextureRadioButtonMenuItem());
			sceneMenu.add(getSimpleTextureRadioButtonMenuItem());
			sceneMenu.add(getFullTextureRadioButtonMenuItem());
			sceneMenu.add(getColorMenu());
			sceneMenu.add(getSeparator_6());
			sceneMenu.add(getWallThicknessMenuItem());
			sceneMenu.add(getAnnotationsInward());
		}
		return sceneMenu;
	}

	private JMenu getUnitsMenu() {
		if (unitsMenu == null) {
			unitsMenu = new JMenu("Units");
			unitsMenu.add(getMetersRadioButtonMenuItem());
			unitsMenu.add(getCentimetersRadioButtonMenuItem());
			unitsMenu.add(getInchesRadioButtonMenuItem());
		}
		return unitsMenu;
	}

	public JCheckBoxMenuItem getShadowMenu() {
		if (shadowMenu == null) {
			shadowMenu = new JCheckBoxMenuItem("Shadows", false);
			shadowMenu.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(final java.awt.event.ItemEvent e) {
					SceneManager.getInstance().setShadow(shadowMenu.isSelected());
				}
			});
		}
		return shadowMenu;
	}

	private JRadioButtonMenuItem getMetersRadioButtonMenuItem() {
		if (metersRadioButtonMenuItem == null) {
			metersRadioButtonMenuItem = new JRadioButtonMenuItem("Meters (m)");
			metersRadioButtonMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().setUnit(Unit.Meter);
				}
			});
			buttonGroup.add(metersRadioButtonMenuItem);
			metersRadioButtonMenuItem.setSelected(true);
		}
		return metersRadioButtonMenuItem;
	}

	private JRadioButtonMenuItem getCentimetersRadioButtonMenuItem() {
		if (centimetersRadioButtonMenuItem == null) {
			centimetersRadioButtonMenuItem = new JRadioButtonMenuItem("Centimeters (cm)");
			centimetersRadioButtonMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().setUnit(Unit.Centimeter);
				}
			});
			buttonGroup.add(centimetersRadioButtonMenuItem);
		}
		return centimetersRadioButtonMenuItem;
	}

	private JRadioButtonMenuItem getInchesRadioButtonMenuItem() {
		if (inchesRadioButtonMenuItem == null) {
			inchesRadioButtonMenuItem = new JRadioButtonMenuItem("Inches (\")");
			inchesRadioButtonMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().setUnit(Unit.Inches);
				}
			});
			buttonGroup.add(inchesRadioButtonMenuItem);
		}
		return inchesRadioButtonMenuItem;
	}

	private JMenuItem getScaleMenuItem() {
		if (scaleMenuItem == null) {
			scaleMenuItem = new JMenuItem("Scale...");
			scaleMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final ScaleDialog scaleDialog = new ScaleDialog();
					scaleDialog.setVisible(true);
				}
			});
		}
		return scaleMenuItem;
	}

	private JCheckBoxMenuItem getAnnotationsInward() {
		if (annotationsInward == null) {
			annotationsInward = new JCheckBoxMenuItem("Annotations Inward");
			annotationsInward.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(final java.awt.event.ItemEvent e) {
					Scene.setDrawAnnotationsInside(annotationsInward.isSelected());
				}
			});

		}
		return annotationsInward;
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
					mainPanel.getSelectButton().setSelected(true);
					SceneManager.getInstance().setOperation(SceneManager.Operation.SELECT);
				}
			});
			editMenu.add(getUndoMenuItem());
			editMenu.add(getRedoMenuItem());
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
					SceneManager.getInstance().hideAllEditPoints();
					SceneManager.getInstance().getUndoManager().undo();
					Scene.getInstance().redrawAll();
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
					SceneManager.getInstance().hideAllEditPoints();
					SceneManager.getInstance().getUndoManager().redo();
					Scene.getInstance().redrawAll();
				}
			});
		}
		return redoMenuItem;
	}

	private void saveFile() {
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.addChoosableFileFilter(ng3Filter);
		fileChooser.removeChoosableFileFilter(serFilter);
		fileChooser.removeChoosableFileFilter(pngFilter);
		fileChooser.setFileFilter(ng3Filter);
		if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
			Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getSelectedFile().getParent());
			try {
				File file = fileChooser.getSelectedFile();
				if (!file.getName().toLowerCase().endsWith(".ng3"))
					file = new File(file.toString() + ".ng3");
				Scene.save(file.toURI().toURL(), true);
				updateTitleBar();
			} catch (final Throwable err) {
				err.printStackTrace();
				JOptionPane.showMessageDialog(MainFrame.this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void importFile() {
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.addChoosableFileFilter(ng3Filter);
		fileChooser.removeChoosableFileFilter(serFilter);
		fileChooser.removeChoosableFileFilter(pngFilter);
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
		// TODO
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.removeChoosableFileFilter(serFilter);
		fileChooser.removeChoosableFileFilter(pngFilter);
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

	private JSeparator getSeparator() {
		if (separator == null) {
			separator = new JSeparator();
		}
		return separator;
	}

	private JSeparator getSeparator_1() {
		if (separator_1 == null) {
			separator_1 = new JSeparator();
		}
		return separator_1;
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
			exactSizeRadioButtonMenuItem = new JRadioButtonMenuItem("Exact Size on Paper");
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

	private JSeparator getSeparator_2() {
		if (separator_2 == null) {
			separator_2 = new JSeparator();
		}
		return separator_2;
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

	private JSeparator getSeparator_3() {
		if (separator_3 == null) {
			separator_3 = new JSeparator();
		}
		return separator_3;
	}

	private JCheckBoxMenuItem getSnapCheckBoxMenuItem() {
		if (snapCheckBoxMenuItem == null) {
			snapCheckBoxMenuItem = new JCheckBoxMenuItem("Snap Walls");
			snapCheckBoxMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					HousePart.setSnapToObjects(snapCheckBoxMenuItem.isSelected());
				}
			});
		}
		return snapCheckBoxMenuItem;
	}

	private JCheckBoxMenuItem getGridsCheckBoxMenuItem() {
		if (gridsCheckBoxMenuItem == null) {
			gridsCheckBoxMenuItem = new JCheckBoxMenuItem("Snap To Grids");
			gridsCheckBoxMenuItem.setSelected(true);
			gridsCheckBoxMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					HousePart.setSnapToGrids(gridsCheckBoxMenuItem.isSelected());
				}
			});
		}
		return gridsCheckBoxMenuItem;
	}

	private JSeparator getSeparator_4() {
		if (separator_4 == null) {
			separator_4 = new JSeparator();
		}
		return separator_4;
	}

	private JSeparator getSeparator_5() {
		if (separator_5 == null) {
			separator_5 = new JSeparator();
		}
		return separator_5;
	}

	private JSeparator getSeparator_6() {
		if (separator_6 == null) {
			separator_6 = new JSeparator();
		}
		return separator_6;
	}

	private JCheckBoxMenuItem getTopViewCheckBoxMenuItem() {
		if (topViewCheckBoxMenuItem == null) {
			topViewCheckBoxMenuItem = new JCheckBoxMenuItem("Top View");
			topViewCheckBoxMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().resetCamera(topViewCheckBoxMenuItem.isSelected() ? ViewMode.TOP_VIEW : ViewMode.NORMAL);
					SceneManager.getInstance().refresh();
				}
			});
		}
		return topViewCheckBoxMenuItem;
	}

	private JSeparator getSeparator_7() {
		if (separator_7 == null) {
			separator_7 = new JSeparator();
		}
		return separator_7;
	}

	private JSeparator getSeparator_8() {
		if (separator_8 == null) {
			separator_8 = new JSeparator();
		}
		return separator_8;
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

	private JSeparator getSeparator_9() {
		if (separator_9 == null) {
			separator_9 = new JSeparator();
		}
		return separator_9;
	}

	public JRadioButtonMenuItem getNoTextureRadioButtonMenuItem() {
		if (noTextureRadioButtonMenuItem == null) {
			noTextureRadioButtonMenuItem = new JRadioButtonMenuItem("No Texture");
			noTextureRadioButtonMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().getUndoManager().addEdit(new ChangeColorTextureCommand());
					Scene.getInstance().setTextureMode(TextureMode.None);
				}
			});
			buttonGroup_2.add(noTextureRadioButtonMenuItem);
		}
		return noTextureRadioButtonMenuItem;
	}

	public JRadioButtonMenuItem getSimpleTextureRadioButtonMenuItem() {
		if (simpleTextureRadioButtonMenuItem == null) {
			simpleTextureRadioButtonMenuItem = new JRadioButtonMenuItem("Simple Texture");
			simpleTextureRadioButtonMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().getUndoManager().addEdit(new ChangeColorTextureCommand());
					Scene.getInstance().setTextureMode(TextureMode.Simple);
				}
			});
			buttonGroup_2.add(simpleTextureRadioButtonMenuItem);
		}
		return simpleTextureRadioButtonMenuItem;
	}

	public JRadioButtonMenuItem getFullTextureRadioButtonMenuItem() {
		if (fullTextureRadioButtonMenuItem == null) {
			fullTextureRadioButtonMenuItem = new JRadioButtonMenuItem("Full Texture");
			fullTextureRadioButtonMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().getUndoManager().addEdit(new ChangeColorTextureCommand());
					Scene.getInstance().setTextureMode(TextureMode.Full);
				}
			});
			fullTextureRadioButtonMenuItem.setSelected(true);
			buttonGroup_2.add(fullTextureRadioButtonMenuItem);
		}
		return fullTextureRadioButtonMenuItem;
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
					showColorDialogForHousePart(Operation.DRAW_ROOF);
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
				case DRAW_ROOF:
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
		if (fullTextureRadioButtonMenuItem.isSelected()) {
			noTextureRadioButtonMenuItem.setSelected(true);
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
		case DRAW_ROOF:
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
		fileChooser.removeChoosableFileFilter(serFilter);
		fileChooser.setFileFilter(pngFilter);
		if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
			System.out.print("Saving snapshot: ");
			Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getSelectedFile().getParent());
			try {
				File file = fileChooser.getSelectedFile();
				if (!file.getName().toLowerCase().endsWith(".png"))
					file = new File(file.toString() + ".png");
				System.out.print(file + "...");
				final BufferedImage snapShot = Printout.takeSnapShot();
				ImageIO.write(snapShot, "png", file);
				System.out.println("done");
			} catch (final Throwable err) {
				err.printStackTrace();
				JOptionPane.showMessageDialog(MainFrame.this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private JMenuItem getFreezeMenuItem() {
		if (freezeMenuItem == null) {
			freezeMenuItem = new JMenuItem("Freeze");
			freezeMenuItem.setVisible(false);
			freezeMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().setFreeze(true);
				}
			});
		}
		return freezeMenuItem;
	}

	private JMenuItem getUnfreezeMenuItem() {
		if (unfreezeMenuItem == null) {
			unfreezeMenuItem = new JMenuItem("Unfreeze");
			unfreezeMenuItem.setVisible(false);
			unfreezeMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Scene.getInstance().setFreeze(false);
				}
			});
		}
		return unfreezeMenuItem;
	}

	private JSeparator getSeparator_10() {
		if (separator_10 == null) {
			separator_10 = new JSeparator();
			separator_10.setVisible(false);
		}
		return separator_10;
	}
}
