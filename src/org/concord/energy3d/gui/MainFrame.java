package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.File;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.prefs.Preferences;

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
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.PrintController;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.Unit;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.CameraMode;
import org.concord.energy3d.scene.SceneManager.Operation;
import org.concord.energy3d.util.Config;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import javax.swing.JSeparator;
import javax.swing.JRadioButton;
import java.awt.event.WindowStateListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class MainFrame extends JFrame {
	private final static boolean IS_MAC = System.getProperty("os.name").startsWith("Mac");
	private static final long serialVersionUID = 1L;
	private static final MainFrame instance = new MainFrame();
	private JFileChooser fileChooser;
	private final JColorChooser colorChooser;
	private JMenuBar appMenuBar = null;
	private JMenu fileMenu = null;
	private JMenuItem newMenuItem = null;
	private JMenuItem openMenuItem = null;
	private JMenuItem saveMenuItem = null;
	private JMenuItem printMenuItem = null;
	private JCheckBoxMenuItem previewMenuItem = null;
	private JMenu cameraMenu = null;
	private JRadioButtonMenuItem orbitMenuItem = null;
	private JRadioButtonMenuItem firstPersonMenuItem = null;
	private JMenuItem saveasMenuItem;
	private JMenu viewMenu;
	private JMenu unitsMenu;
	private JRadioButtonMenuItem metersRadioButtonMenuItem;
	private JRadioButtonMenuItem centimetersRadioButtonMenuItem;
	private JRadioButtonMenuItem inchesRadioButtonMenuItem;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JMenu scaleMenu;
	private JMenuItem scaleMenuItem;
	private JCheckBoxMenuItem shadowMenu;
	private JCheckBoxMenuItem textureCheckBoxMenuItem;
	protected Object lastSelection; // @jve:decl-index=0:
	private JMenuItem colorMenuItem = null;
	private JCheckBoxMenuItem lightingMenu = null;
	private JMenuItem exitMenuItem = null;
	private JMenu helpMenu = null;
	private JMenuItem aboutMenuItem = null;
	private JDialog aboutDialog = null; // @jve:decl-index=0:visual-constraint="602,644"
	private JCheckBoxMenuItem wallThicknessMenuItem = null;
	private MainPanel mainPanel = null;
	private JCheckBoxMenuItem drawAnnotationsInward;
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

	private static class ExtensionFileFilter extends javax.swing.filechooser.FileFilter {
		String description;
		String extensions[];

		public ExtensionFileFilter(String description, String extension) {
			this(description, new String[] { extension });
		}

		public ExtensionFileFilter(String description, String extensions[]) {
			if (description == null) {
				this.description = extensions[0] + "{ " + extensions.length + "} ";
			} else {
				this.description = description;
			}
			this.extensions = (String[]) extensions.clone();
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public boolean accept(File file) {
			if (file.isDirectory()) {
				return true;
			} else {
				String path = file.getAbsolutePath().toLowerCase();
				for (int i = 0, n = extensions.length; i < n; i++) {
					String extension = extensions[i];
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
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MainFrame thisClass = new MainFrame();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
		setIconImage(Toolkit.getDefaultToolkit().getImage(MainFrame.class.getResource("/org/concord/energy3d/resources/icons/icon.gif")));
		try {
			final String directoryPath = Preferences.userNodeForPackage(MainApplication.class).get("dir", null);
			fileChooser = new JFileChooser(directoryPath);
			if (!Config.isWebStart() && directoryPath == null) {
				final File dir = new File(System.getProperties().getProperty("user.dir") + "/Energy3D Projects");
//				if (!dir.exists()) {
//					System.out.print("Making save directory..." + dir + "...");
//					final boolean success = dir.mkdir();
//					System.out.println(success ? "done" : "failed");
//				}
				fileChooser.setCurrentDirectory(dir);
			}
			fileChooser.addChoosableFileFilter(new ExtensionFileFilter("Energy3D Project (*.ser)", "ser"));
		} catch (Exception e) {
			fileChooser = null;
			e.printStackTrace();
		}
		colorChooser = new JColorChooser();
		final ReadOnlyColorRGBA defaultColor = HousePart.getDefaultColor();
		colorChooser.setColor(new Color(defaultColor.getRed(), defaultColor.getGreen(), defaultColor.getBlue()));
		initialize();
		updateTitleBar();
		System.out.println("done");
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		// this.setSize(600, 600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Energy3D v" + Config.VERSION);
		
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//		GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		
		this.setSize(
				Math.min(Preferences.userNodeForPackage(MainApplication.class).getInt("window_size_width", 900), screenSize.width),
				Math.min(Preferences.userNodeForPackage(MainApplication.class).getInt("window_size_height", 600), screenSize.height));
		this.setLocation((int) (screenSize.getWidth() - this.getSize().getWidth()) / 2, (int) (screenSize.getHeight() - this.getSize().getHeight()) / 2);
		this.setLocation(
				Preferences.userNodeForPackage(MainApplication.class).getInt("window_location_x", (int) (screenSize.getWidth() - this.getSize().getWidth()) / 2),
				Preferences.userNodeForPackage(MainApplication.class).getInt("window_location_y", (int) (screenSize.getHeight() - this.getSize().getHeight()) / 2));
		this.setLocation(
				MathUtils.clamp(this.getLocation().x, 0, screenSize.width - this.getSize().width), 
				MathUtils.clamp(this.getLocation().y, 0, screenSize.height - this.getSize().height));
//		this.setSize(900, 600); // XIE: reduce the length, as some older computers have only 1024x800 screen resolution.				
		final int windowState = Preferences.userNodeForPackage(MainApplication.class).getInt("window_state", JFrame.NORMAL);
		if ((windowState & JFrame.ICONIFIED) == 0)
			this.setExtendedState(windowState);
		
		this.setJMenuBar(getAppMenuBar());
		this.setContentPane(getMainPanel());
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(final ComponentEvent e) {
				if (MainFrame.this.getExtendedState() == 0 ) {
				Preferences.userNodeForPackage(MainApplication.class).putInt("window_location_x", e.getComponent().getLocation().x);
				Preferences.userNodeForPackage(MainApplication.class).putInt("window_location_y", e.getComponent().getLocation().y);
				}
			}
			@Override
			public void componentResized(final ComponentEvent e) {
				if (MainFrame.this.getExtendedState() == 0 ) {
					Preferences.userNodeForPackage(MainApplication.class).putInt("window_size_width", e.getComponent().getSize().width);
					Preferences.userNodeForPackage(MainApplication.class).putInt("window_size_height", e.getComponent().getSize().height);
				}
			}
		});
		addWindowStateListener(new WindowStateListener() {
			public void windowStateChanged(final WindowEvent e) {				
				Preferences.userNodeForPackage(MainApplication.class).putInt("window_state", e.getNewState());
				SceneManager.getInstance().update();
			}
		});		
		
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				SceneManager.getInstance().exit();
			}

			@Override
			public void windowDeiconified(final WindowEvent e) {
				SceneManager.getInstance().update();
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
			appMenuBar.add(getViewMenu());
			appMenuBar.add(getScaleMenu());
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
				public void menuCanceled(MenuEvent e) {
				}

				public void menuDeselected(MenuEvent e) {
					SceneManager.getInstance().update();
				}

				public void menuSelected(MenuEvent e) {
					mainPanel.getSelectButton().setSelected(true);
					SceneManager.getInstance().setOperation(SceneManager.Operation.SELECT);
				}
			});
			fileMenu.setText("File");
			fileMenu.add(getNewMenuItem());
			fileMenu.add(getOpenMenuItem());
			fileMenu.add(getSaveMenuItem());
			fileMenu.add(getSaveasMenuItem());
			fileMenu.add(getSeparator());
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
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Scene.newFile(40, 30);
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
			openMenuItem.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
			openMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SceneManager.getInstance().update(1);
					if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
						Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getSelectedFile().getParent());
						try {
							Scene.open(fileChooser.getSelectedFile().toURI().toURL());
							updateTitleBar();
						} catch (Throwable err) {
							err.printStackTrace();
							JOptionPane.showMessageDialog(MainFrame.this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			});
		}
		return openMenuItem;
	}

	protected void updateTitleBar() {
		if (Scene.getURL() == null)
			this.setTitle("Energy3D v" + Config.VERSION);
		else
			this.setTitle("Energy3D v" + Config.VERSION + " - " + new File(Scene.getURL().getFile()).toString().replaceAll("%20", " "));
	}

	/**
	 * This method initializes saveMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getSaveMenuItem() {
		if (saveMenuItem == null) {
			saveMenuItem = new JMenuItem("Save");
			saveMenuItem.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
			saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						final URL url = Scene.getURL();
						if (url != null)
							Scene.save(url);
						else //if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
//							Scene.save(fileChooser.getSelectedFile().toURI().toURL());
							saveFile();
					} catch (Throwable err) {
						err.printStackTrace();
						JOptionPane.showMessageDialog(MainFrame.this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
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
			printMenuItem.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK));
			printMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					final PrintController printController = PrintController.getInstance();
					if (!printController.isPrintPreview()) {					    
						MainFrame.getInstance().getPreviewMenuItem().setSelected(true);
						SceneManager.getTaskManager().update(new Callable<Object>() {
							public Object call() throws Exception {
								if (printController.isFinished())
									PrintController.getInstance().print();
								else
									SceneManager.getTaskManager().update(this);
								return null;
							}
						});
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
				public void itemStateChanged(java.awt.event.ItemEvent e) {
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
	private JMenu getCameraMenu() {
		if (cameraMenu == null) {
			cameraMenu = new JMenu();
			cameraMenu.addMenuListener(new MenuListener() {
				public void menuCanceled(MenuEvent e) {
				}

				public void menuDeselected(MenuEvent e) {
					SceneManager.getInstance().update();
				}

				public void menuSelected(MenuEvent e) {
					mainPanel.getSelectButton().setSelected(true);
					SceneManager.getInstance().setOperation(SceneManager.Operation.SELECT);
				}
			});
			cameraMenu.setText("Camera");
			cameraMenu.add(getOrbitMenuItem());
			cameraMenu.add(getFirstPersonMenuItem());
			ButtonGroup bg = new ButtonGroup();
			bg.add(orbitMenuItem);
			bg.add(firstPersonMenuItem);
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
				public void actionPerformed(java.awt.event.ActionEvent e) {
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
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setCameraControl(CameraMode.FIRST_PERSON);
				}
			});
		}
		return firstPersonMenuItem;
	}

	/**
	 * This method initializes colorMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getColorMenuItem() {
		if (colorMenuItem == null) {
			colorMenuItem = new JMenuItem();
			colorMenuItem.setText("House Color...");
			colorMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					final ActionListener actionListener = new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							final Color c = colorChooser.getColor();
							final float[] newColor = c.getComponents(null);
							HousePart.setDefaultColor(new ColorRGBA(newColor[0], newColor[1], newColor[2], newColor[3]));
							Scene.getInstance().setTextureEnabled(getTextureCheckBoxMenuItem().isSelected());
						}
					};
					textureCheckBoxMenuItem.setSelected(false);
					final JDialog colorDialog = JColorChooser.createDialog(MainFrame.this, "Select House Color", true, colorChooser, actionListener, null);
					colorDialog.setVisible(true);
				}
			});
		}
		return colorMenuItem;
	}

	/**
	 * This method initializes lightingMenu
	 * 
	 * @return javax.swing.JCheckBoxMenuItem
	 */
	public JCheckBoxMenuItem getLightingMenu() {
		if (lightingMenu == null) {
			lightingMenu = new JCheckBoxMenuItem();
			lightingMenu.setText("Shading");
			lightingMenu.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					SceneManager.getInstance().setShading(lightingMenu.isSelected());
				}
			});
		}
		return lightingMenu;
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
				public void actionPerformed(java.awt.event.ActionEvent e) {
//					System.exit(0);
					SceneManager.getInstance().exit();
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
			helpMenu.add(getAboutMenuItem());
		}
		return helpMenu;
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
			aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					final JDialog aboutDialog = getAboutDialog();
					final Dimension frameSize = MainFrame.this.getSize();
					final Dimension dialogSize = aboutDialog.getSize();
					final Point location = MainFrame.this.getLocation();
					aboutDialog.setLocation((int) (location.getX() + frameSize.getWidth() / 2 - dialogSize.getWidth() / 2), (int) (location.getY() + frameSize.getHeight() / 2 - dialogSize.getHeight() / 2));
					aboutDialog.setVisible(true);
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
			JPanel p = new JPanel(new BorderLayout());
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
				public void itemStateChanged(java.awt.event.ItemEvent e) {
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
				public void actionPerformed(ActionEvent e) {
					saveFile();
				}
			});
		}
		return saveasMenuItem;
	}

	private JMenu getViewMenu() {
		if (viewMenu == null) {
			viewMenu = new JMenu("View");
			viewMenu.addMenuListener(new MenuListener() {
				public void menuCanceled(MenuEvent e) {
				}

				public void menuDeselected(MenuEvent e) {
					SceneManager.getInstance().update();
				}

				public void menuSelected(MenuEvent e) {
					mainPanel.getSelectButton().setSelected(true);
					SceneManager.getInstance().setOperation(SceneManager.Operation.SELECT);
				}
			});
			viewMenu.add(getUnitsMenu());
			viewMenu.add(getLightingMenu());
			viewMenu.add(getShadowMenu());
			viewMenu.add(getTextureCheckBoxMenuItem());
			viewMenu.add(getColorMenuItem());
			viewMenu.add(getWallThicknessMenuItem());
			viewMenu.add(getDrawAnnotationsInward());
		}
		return viewMenu;
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
				public void itemStateChanged(java.awt.event.ItemEvent e) {
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
				public void actionPerformed(ActionEvent e) {
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
				public void actionPerformed(ActionEvent e) {
					Scene.getInstance().setUnit(Unit.Centimeter);
				}
			});
			buttonGroup.add(centimetersRadioButtonMenuItem);
			centimetersRadioButtonMenuItem.setSelected(true);
		}
		return centimetersRadioButtonMenuItem;
	}

	private JRadioButtonMenuItem getInchesRadioButtonMenuItem() {
		if (inchesRadioButtonMenuItem == null) {
			inchesRadioButtonMenuItem = new JRadioButtonMenuItem("Inches (\")");
			inchesRadioButtonMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Scene.getInstance().setUnit(Unit.Inches);
				}
			});
			buttonGroup.add(inchesRadioButtonMenuItem);
		}
		return inchesRadioButtonMenuItem;
	}

	private JMenu getScaleMenu() {
		if (scaleMenu == null) {
			scaleMenu = new JMenu("Scale");
			scaleMenu.addMenuListener(new MenuListener() {
				public void menuCanceled(MenuEvent e) {
				}

				public void menuDeselected(MenuEvent e) {
				}

				public void menuSelected(MenuEvent e) {
					mainPanel.getSelectButton().setSelected(true);
					SceneManager.getInstance().setOperation(SceneManager.Operation.SELECT);
				}
			});
			scaleMenu.add(getScaleMenuItem());
		}
		return scaleMenu;
	}

	private JMenuItem getScaleMenuItem() {
		if (scaleMenuItem == null) {
			scaleMenuItem = new JMenuItem("Scale...");
			scaleMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final ScaleDialog scaleDialog = new ScaleDialog();
					scaleDialog.setVisible(true);
				}
			});
		}
		return scaleMenuItem;
	}

	private JCheckBoxMenuItem getTextureCheckBoxMenuItem() {
		if (textureCheckBoxMenuItem == null) {
			textureCheckBoxMenuItem = new JCheckBoxMenuItem("Texture", true);
			textureCheckBoxMenuItem.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					Scene.getInstance().setTextureEnabled(textureCheckBoxMenuItem.isSelected());
				}
			});
		}
		return textureCheckBoxMenuItem;
	}

	private JCheckBoxMenuItem getDrawAnnotationsInward() {
		if (drawAnnotationsInward == null) {
			drawAnnotationsInward = new JCheckBoxMenuItem("Draw Annotations Inward");
			drawAnnotationsInward.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					Scene.setDrawAnnotationsInside(drawAnnotationsInward.isSelected());
				}
			});

		}
		return drawAnnotationsInward;
	}

	private JMenu getEditMenu() {
		if (editMenu == null) {
			editMenu = new JMenu("Edit");
			editMenu.addMenuListener(new MenuListener() {
				public void menuCanceled(MenuEvent e) {
				}

				public void menuDeselected(MenuEvent e) {
					SceneManager.getInstance().update();
				}

				public void menuSelected(MenuEvent e) {
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
			undoMenuItem.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
			undoMenuItem.setEnabled(false);
			undoMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().hideAllEditPoints();
					SceneManager.getInstance().getUndoManager().undo();
					SceneManager.getInstance().update();
					refreshUndoRedo();
				}
			});
		}
		return undoMenuItem;
	}

	private JMenuItem getRedoMenuItem() {
		if (redoMenuItem == null) {
			redoMenuItem = new JMenuItem("Redo");
			redoMenuItem.setAccelerator(IS_MAC ? KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.META_MASK) : KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
			redoMenuItem.setEnabled(false);
			redoMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().hideAllEditPoints();
					SceneManager.getInstance().getUndoManager().redo();
					SceneManager.getInstance().update();
					refreshUndoRedo();
				}
			});
		}
		return redoMenuItem;
	}

	private void saveFile() {
		if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
			Preferences.userNodeForPackage(MainApplication.class).put("dir", fileChooser.getSelectedFile().getParent());
			try {
				File file = fileChooser.getSelectedFile();
				if (!file.getName().toLowerCase().endsWith(".ser"))
					file = new File(file.toString() + ".ser");
				Scene.save(file.toURI().toURL());
				updateTitleBar();
			} catch (Throwable err) {
				err.printStackTrace();
				JOptionPane.showMessageDialog(MainFrame.this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
	}
	private JMenuItem getPageSetupMenuItem() {
		if (pageSetupMenuItem == null) {
			pageSetupMenuItem = new JMenuItem("Page Setup...");
			pageSetupMenuItem.addActionListener(new ActionListener() {
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
				public void actionPerformed(final ActionEvent e) {
					PrintController.getInstance().setScaleToFit(true);
				}
			});
			buttonGroup_1.add(scaleToFitRadioButtonMenuItem);
		}
		return scaleToFitRadioButtonMenuItem;
	}
	
	private JRadioButtonMenuItem getExactSizeRadioButtonMenuItem() {
		if (exactSizeRadioButtonMenuItem == null) {
			exactSizeRadioButtonMenuItem = new JRadioButtonMenuItem("Exact Size on Paper");
			exactSizeRadioButtonMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					PrintController.getInstance().setScaleToFit(false);
				}
			});
			buttonGroup_1.add(exactSizeRadioButtonMenuItem);
			exactSizeRadioButtonMenuItem.setSelected(true);
		}
		return exactSizeRadioButtonMenuItem;
	}
	
	private JSeparator getSeparator_2() {
		if (separator_2 == null) {
			separator_2 = new JSeparator();
		}
		return separator_2;
	}
}
