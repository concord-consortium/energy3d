package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
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
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.PrintController;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.Unit;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.CameraMode;
import org.concord.energy3d.scene.SceneManager.Operation;
import org.concord.energy3d.scene.SceneManager.ViewMode;
import org.concord.energy3d.shapes.Heliodon;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.JComboBox;
import javax.swing.SpinnerNumberModel;
import java.awt.Insets;

public class MainFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private static final MainFrame instance = new MainFrame();
	private final JFileChooser fileChooser;
	private final JColorChooser colorChooser;
	private JPanel jContentPane = null;
	private JMenuBar appMenuBar = null;
	private JMenu fileMenu = null;
	private JToolBar appToolbar = null;
	private JToggleButton selectButton = null;
	private JToggleButton wallButton = null;
	private JToggleButton doorButton = null;
	private JToggleButton roofButton = null;
	private JToggleButton windowButton = null;
	private JToggleButton foundationButton = null;
	private JToggleButton lightButton = null;
	private JToggleButton topViewButton = null;
	private JToggleButton rotAnimButton = null;
	private JToggleButton gridButton = null;
	private JToggleButton snapButton = null;
	private JToggleButton floorButton = null;
	private JToggleButton roofHipButton = null;
	private JToggleButton resizeButton = null;
	private JToggleButton sunButton = null;
	private JToggleButton sunAnimButton = null;
	private JMenuItem newMenuItem = null;
	private JMenuItem openMenuItem = null;
	private JMenuItem saveMenuItem = null;
	private JMenuItem printMenuItem = null;
	private JCheckBoxMenuItem previewMenuItem = null;
	private JMenu cameraMenu = null;
	private JRadioButtonMenuItem orbitMenuItem = null;
	private JRadioButtonMenuItem firstPersonMenuItem = null;
	private JMenuItem saveasMenuItem;
	private JToggleButton annotationToggleButton;
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
	private JMenu debugMenu = null;
	private JMenuItem infoMenuItem = null;
	private JCheckBoxMenuItem lightingMenu = null;
	private JToggleButton previewButton = null;
	private JMenuItem exitMenuItem = null;
	private JMenu helpMenu = null;
	private JMenuItem aboutMenuItem = null;
	private JDialog aboutDialog = null;  //  @jve:decl-index=0:visual-constraint="602,644"
	private JCheckBoxMenuItem wallThicknessMenuItem = null;
	private JPanel calendarPanel = null;
	private JLabel dateLabel = null;
	private JSpinner dateSpinner = null;
	private JLabel timeLabel = null;
	private JSpinner timeSpinner = null;
	private JLabel latitudeLabel = null;
	private JComboBox cityComboBox = null;
	private JSpinner latitudeSpinner = null;
	
	public static MainFrame getInstance() {
		return instance;
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
			appMenuBar.add(getViewMenu());
			appMenuBar.add(getScaleMenu());
			appMenuBar.add(getCameraMenu());
			appMenuBar.add(getDebugMenu());
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
				}

				public void menuSelected(MenuEvent e) {
					getSelectButton().setSelected(true);
					SceneManager.getInstance().setOperation(SceneManager.Operation.SELECT);
				}
			});
			fileMenu.setText("File");
			fileMenu.add(getNewMenuItem());
			fileMenu.add(getOpenMenuItem());
			fileMenu.add(getSaveMenuItem());
			fileMenu.add(getSaveasMenuItem());
			fileMenu.add(getPreviewMenuItem());
			fileMenu.add(getPrintMenuItem());
			fileMenu.add(getExitMenuItem());
		}
		return fileMenu;
	}

	/**
	 * This method initializes appToolbar
	 * 
	 * @return javax.swing.JToolBar
	 */
	private JToolBar getAppToolbar() {
		if (appToolbar == null) {
			appToolbar = new JToolBar();
			appToolbar.add(getSelectButton());
			appToolbar.add(getResizeButton());
			appToolbar.addSeparator();
			appToolbar.add(getFoundationButton());
			appToolbar.add(getWallButton());
			appToolbar.add(getDoorButton());
			appToolbar.add(getWindowButton());
			appToolbar.add(getRoofButton());
			appToolbar.add(getRoofHipButton());
			appToolbar.add(getFloorButton());
			appToolbar.addSeparator();
			appToolbar.add(getLightButton());
			appToolbar.add(getSunButton());
			appToolbar.add(getSunAnimButton());
			appToolbar.add(getCalendarPanel());
			appToolbar.addSeparator();
			appToolbar.add(getRotAnimButton());
			appToolbar.add(getTopViewButton());
			appToolbar.add(getGridButton());
			appToolbar.add(getSnapButton());
			appToolbar.addSeparator();
			appToolbar.add(getAnnotationToggleButton());
			appToolbar.add(getPreviewButton());
			
			final ButtonGroup bg = new ButtonGroup();
			bg.add(selectButton);
			bg.add(resizeButton);
			bg.add(foundationButton);
			bg.add(wallButton);
			bg.add(doorButton);
			bg.add(windowButton);
			bg.add(roofButton);
			bg.add(roofHipButton);
			bg.add(floorButton);
		}
		return appToolbar;
	}

	/**
	 * This method initializes selectButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	public JToggleButton getSelectButton() {
		if (selectButton == null) {
			selectButton = new JToggleButton();
			selectButton.setSelected(true);
			selectButton.setToolTipText("Select");
			selectButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/select.png")));
			selectButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.SELECT);
				}
			});
		}
		return selectButton;
	}

	/**
	 * This method initializes wallButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getWallButton() {
		if (wallButton == null) {
			wallButton = new JToggleButton();
			wallButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/wall.png")));
			wallButton.setToolTipText("Draw wall");
			wallButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					 SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_WALL);
				}
			});
			wallButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if (e.getClickCount() > 1)
						SceneManager.getInstance().setOperationStick(true);					
				}
			});
		}
		return wallButton;
	}

	/**
	 * This method initializes doorButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getDoorButton() {
		if (doorButton == null) {
			doorButton = new JToggleButton();
			doorButton.setText("");
			doorButton.setToolTipText("Draw door");
			doorButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/door.png")));
			doorButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					 SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_DOOR);
				}
			});
			doorButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if (e.getClickCount() > 1)
						SceneManager.getInstance().setOperationStick(true);					
				}
			});
		}
		return doorButton;
	}

	/**
	 * This method initializes roofButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JToggleButton getRoofButton() {
		if (roofButton == null) {
			roofButton = new JToggleButton();
			roofButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/pyramid.png")));
			roofButton.setToolTipText("Draw pyramid roof");
			roofButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					 SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_ROOF);
				}
			});
			roofButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if (e.getClickCount() > 1)
						SceneManager.getInstance().setOperationStick(true);					
				}
			});
		}
		return roofButton;
	}

	/**
	 * This method initializes windowButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getWindowButton() {
		if (windowButton == null) {
			windowButton = new JToggleButton();
			windowButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/window.png")));
			windowButton.setToolTipText("Draw window");
			windowButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					 SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_WINDOW);
				}
			});
			windowButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if (e.getClickCount() > 1)
						SceneManager.getInstance().setOperationStick(true);					
				}
			});
		}
		return windowButton;
	}

	/**
	 * This method initializes foundationButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getFoundationButton() {
		if (foundationButton == null) {
			foundationButton = new JToggleButton();
			foundationButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/foundation.png")));
			foundationButton.setToolTipText("Draw foundation");
			foundationButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_FOUNDATION);
				}
			});
			foundationButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if (e.getClickCount() > 1)
						SceneManager.getInstance().setOperationStick(true);
				}
			});
		}
		return foundationButton;
	}

	/**
	 * This method initializes lightButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getLightButton() {
		if (lightButton == null) {
			lightButton = new JToggleButton();
			lightButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/shadow.png")));
			lightButton.setToolTipText("Show shadows");
			lightButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					lightingMenu.setSelected(lightButton.isSelected());
					shadowMenu.setSelected(lightButton.isSelected());
					final boolean showSunTools = lightButton.isSelected() || sunButton.isSelected();
					calendarPanel.setVisible(showSunTools);
					sunAnimButton.setEnabled(showSunTools);
				}
			});
		}
		return lightButton;
	}

	/**
	 * This method initializes topViewButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getTopViewButton() {
		if (topViewButton == null) {
			topViewButton = new JToggleButton();
			topViewButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/top.png")));
			topViewButton.setToolTipText("Top view");
			topViewButton.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					SceneManager.getInstance().resetCamera(topViewButton.isSelected() ? ViewMode.TOP_VIEW : ViewMode.NORMAL);
				}
			});
		}
		return topViewButton;
	}

	/**
	 * This method initializes rotAnimButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getRotAnimButton() {
		if (rotAnimButton == null) {
			rotAnimButton = new JToggleButton();
			rotAnimButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/rotate.png")));
			rotAnimButton.setToolTipText("Animate scene roatation");
			rotAnimButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SceneManager.getInstance().toggleRotation();
				}
			});
		}
		return rotAnimButton;
	}

	/**
	 * This method initializes gridButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getGridButton() {
		if (gridButton == null) {
			gridButton = new JToggleButton();
			gridButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/grid.png")));
			gridButton.setToolTipText("Grids");
			gridButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					HousePart.setSnapToGrids(gridButton.isSelected());
				}
			});
		}
		return gridButton;
	}

	/**
	 * This method initializes snapButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getSnapButton() {
		if (snapButton == null) {
			snapButton = new JToggleButton();
			snapButton.setSelected(true);
			snapButton.setToolTipText("Snap");
			snapButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/snap.png")));
			snapButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					HousePart.setSnapToObjects(snapButton.isSelected());
				}
			});
		}
		return snapButton;
	}

	/**
	 * This method initializes floorButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getFloorButton() {
		if (floorButton == null) {
			floorButton = new JToggleButton();
			floorButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/floor.png")));
			floorButton.setToolTipText("Draw floor");
			floorButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					 SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_FLOOR);
				}
			});
			floorButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if (e.getClickCount() > 1)
						SceneManager.getInstance().setOperationStick(true);					
				}
			});
		}
		return floorButton;
	}

	/**
	 * This method initializes roofHipButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getRoofHipButton() {
		if (roofHipButton == null) {
			roofHipButton = new JToggleButton();
			roofHipButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/hip.png")));
			roofHipButton.setToolTipText("Draw hip roof");
			roofHipButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					 SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_ROOF_HIP);
				}
			});
			roofHipButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if (e.getClickCount() > 1)
						SceneManager.getInstance().setOperationStick(true);					
				}
			});
		}
		return roofHipButton;
	}

	/**
	 * This method initializes resizeButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getResizeButton() {
		if (resizeButton == null) {
			resizeButton = new JToggleButton();
			resizeButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/resize.png")));
			resizeButton.setToolTipText("Resize house");
			resizeButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(Operation.RESIZE);
				}
			});
		}
		return resizeButton;
	}

	/**
	 * This method initializes sunButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getSunButton() {
		if (sunButton == null) {
			sunButton = new JToggleButton();
			sunButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/sun_heliodon.png")));
			sunButton.setToolTipText("Show sun heliodon");
			sunButton.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					SceneManager.getInstance().setSunControl(sunButton.isSelected());
					final boolean showSunTools = lightButton.isSelected() || sunButton.isSelected();
					calendarPanel.setVisible(showSunTools);
					sunAnimButton.setEnabled(showSunTools);					
				}
			});
		}
		return sunButton;
	}

	/**
	 * This method initializes sunAnimButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getSunAnimButton() {
		if (sunAnimButton == null) {
			sunAnimButton = new JToggleButton();
			sunAnimButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/sun_anim.png")));
			sunAnimButton.setToolTipText("Animate sun");
			sunAnimButton.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					SceneManager.getInstance().setSunAnim(sunAnimButton.isSelected());
				}
			});
		}
		return sunAnimButton;
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
					Scene.getInstance().newFile();
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
			openMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
						Scene.getInstance().open(fileChooser.getSelectedFile());
					}
				}
			});
		}
		return openMenuItem;
	}

	/**
	 * This method initializes saveMenuItem
	 * 
	 * @return javax.swing.JMenuItem
	 */
	private JMenuItem getSaveMenuItem() {
		if (saveMenuItem == null) {
			saveMenuItem = new JMenuItem("Save");
			saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						final File file = Scene.getFile();
						if (file != null)
							Scene.getInstance().save(file);
						else if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
							Scene.getInstance().save(fileChooser.getSelectedFile());
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
			printMenuItem = new JMenuItem("Print");
			printMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					final PrintController printController = PrintController.getInstance();
					if (!printController.isPrintPreview()) {
						MainFrame.getInstance().getPreviewMenuItem().setSelected(true);
						while (!printController.isFinished())
							Thread.yield();
					}					
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
					deselect();
					PrintController.getInstance().setPrintPreview(previewMenuItem.isSelected());
					previewButton.setSelected(previewMenuItem.isSelected());
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
				}

				public void menuSelected(MenuEvent e) {
					getSelectButton().setSelected(true);
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
	 * This method initializes debugMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	private JMenu getDebugMenu() {
		if (debugMenu == null) {
			debugMenu = new JMenu();
			debugMenu.setText("Debug");
			debugMenu.add(getInfoMenuItem());
		}
		return debugMenu;
	}

	/**
	 * This method initializes infoMenuItem	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */
	private JMenuItem getInfoMenuItem() {
		if (infoMenuItem == null) {
			infoMenuItem = new JMenuItem();
			infoMenuItem.setText("Print Info");
			infoMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("******** Scene Info *********");
					for (final HousePart part : Scene.getInstance().getParts())
						System.out.println(part + "\t" + part.getRoot().getWorldBound());
					System.out.println("*****************************");
				}
			});
		}
		return infoMenuItem;
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
					SceneManager.getInstance().setLighting(lightingMenu.isSelected());
				}
			});
		}
		return lightingMenu;
	}

	/**
	 * This method initializes previewButton	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getPreviewButton() {
		if (previewButton == null) {
			previewButton = new JToggleButton();
			previewButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/print_preview.png")));
			previewButton.setToolTipText("Preview printable parts");
			previewButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					previewMenuItem.setSelected(previewButton.isSelected());
				}
			});
		}
		return previewButton;
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
					System.exit(0);
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
					aboutDialog.setLocation((int)(location.getX() + frameSize.getWidth() / 2 - dialogSize.getWidth() / 2), (int)(location.getY() + frameSize.getHeight() / 2 - dialogSize.getHeight() / 2));
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
			p.add(new JLabel("<html><h2>Energy3D</h2><hr><h3>Credit:</h3>This program is brought to you by:<ul><li>Dr. Saeid Nourian, Lead Developer<li>Dr. Charles Xie, Co-developer</ul><p>This program is licensed under the GNU Lesser General Public License V3.0<br>and based on Ardor3D. Funding of this project is provided by the National<br>Science Foundation under grant #0918449 to the Concord Consortium. </html>"), BorderLayout.CENTER);
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

	/**
	 * This method initializes calendarPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCalendarPanel() {
		if (calendarPanel == null) {
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 3;
			gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.insets = new Insets(0, 0, 0, 1);
			gridBagConstraints4.gridy = 1;
			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
			gridBagConstraints31.fill = GridBagConstraints.BOTH;
			gridBagConstraints31.gridy = 0;
			gridBagConstraints31.insets = new Insets(0, 1, 0, 1);
			gridBagConstraints31.gridwidth = 2;
			gridBagConstraints31.gridx = 2;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 2;
			gridBagConstraints21.gridy = 1;
			latitudeLabel = new JLabel();
			latitudeLabel.setText(" Latitude:");
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.gridy = 1;
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			timeLabel = new JLabel();
			timeLabel.setText(" Time: ");
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.gridy = 0;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			dateLabel = new JLabel();
			dateLabel.setText(" Date: ");
			calendarPanel = new JPanel();
			calendarPanel.setLayout(new GridBagLayout());
			calendarPanel.setVisible(false);
			calendarPanel.add(dateLabel, gridBagConstraints);
			calendarPanel.add(getDateSpinner(), gridBagConstraints1);
			calendarPanel.add(timeLabel, gridBagConstraints2);
			calendarPanel.add(getTimeSpinner(), gridBagConstraints3);
			calendarPanel.add(latitudeLabel, gridBagConstraints21);
			calendarPanel.add(getCityComboBox(), gridBagConstraints31);
			calendarPanel.add(getLatitudeSpinner(), gridBagConstraints4);
			
			calendarPanel.setMaximumSize(new Dimension(calendarPanel.getPreferredSize().width, 2147483647));
		}
		return calendarPanel;
	}

	/**
	 * This method initializes dateSpinner	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	public JSpinner getDateSpinner() {
		if (dateSpinner == null) {
			final SpinnerDateModel model = new SpinnerDateModel();
			final Calendar date = Calendar.getInstance();
			// initially set the date to September 29 so that it will resize itself to max
			date.set(2011, 8, 29);
			model.setValue(date.getTime());
			dateSpinner = new JSpinner(model);
			dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MMMM dd"));
			dateSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					final Heliodon heliodon = SceneManager.getInstance().getHeliodon();
					if (heliodon != null)
						heliodon.setDate((Date)dateSpinner.getValue());
				}
			});
		}
		return dateSpinner;
	}

	/**
	 * This method initializes timeSpinner	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	public JSpinner getTimeSpinner() {
		if (timeSpinner == null) {
			timeSpinner = new JSpinner(new SpinnerDateModel());
			timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "H:mm"));
			timeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {					
					final Heliodon heliodon = SceneManager.getInstance().getHeliodon();
					if (heliodon != null)
						heliodon.setTime((Date)timeSpinner.getValue());
				}
			});
		}
		return timeSpinner;
	}

	/**
	 * This method initializes cityComboBox	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getCityComboBox() {
		if (cityComboBox == null) {
			cityComboBox = new JComboBox();
			cityComboBox.addItem("");
			cityComboBox.addItem("Boston");
			cityComboBox.addItem("Washington DC");
			cityComboBox.addItem("Los Angeles");
			cityComboBox.addItem("San Francisco");
			cityComboBox.addItem("Tehran");
			cityComboBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					int latitude = 0;
					switch (cityComboBox.getSelectedIndex()) {
					case 1:
						latitude = 42;
						break;
					case 2:
						latitude = 38;
						break;
					case 3:
						latitude = 34;
						break;
					case 4:
						latitude = 37;
						break;
					case 5:
						latitude = 35;
						break;
					case 6:
						latitude = 39;
						break;
					}
					latitudeSpinner.setValue(latitude);
				}
			});
		}
		return cityComboBox;
	}

	/**
	 * This method initializes latitudeSpinner	
	 * 	
	 * @return javax.swing.JSpinner	
	 */
	private JSpinner getLatitudeSpinner() {
		if (latitudeSpinner == null) {
			latitudeSpinner = new JSpinner();
			latitudeSpinner.setModel(new SpinnerNumberModel(0, -90, 90, 1));
			latitudeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					SceneManager.getInstance().getHeliodon().setObserverLatitude(((Integer)latitudeSpinner.getValue()) / 180.0 * Math.PI);
				}
			});
		}
		return latitudeSpinner;
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
		final File dir = new File(System.getProperties().getProperty("user.dir") + "/Energy3D Projects");
		if (!dir.exists()) {
			System.out.print("Making save directory...");
			final boolean success = dir.mkdir();
			System.out.println(success ? "done" : "failed");
		}
		fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(dir);
		fileChooser.addChoosableFileFilter(new ExtensionFileFilter("Energy3D Project (*.ser)", "ser"));
		
		colorChooser = new JColorChooser();
		final ReadOnlyColorRGBA defaultColor = HousePart.getDefaultColor();
		colorChooser.setColor(new Color(defaultColor.getRed(), defaultColor.getGreen(), defaultColor.getBlue()));

		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(1092, 600);
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((int)(screenSize.getWidth() - this.getSize().getWidth()) / 2, (int)(screenSize.getHeight() - this.getSize().getHeight()) / 2);
//		this.setSize(172, 600);
//		this.setSize(400*2, 550*2);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setJMenuBar(getAppMenuBar());
		this.setContentPane(getJContentPane());
		this.setTitle("Energy3D");
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				SceneManager.getInstance().exit();
			}
		});
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getAppToolbar(), BorderLayout.NORTH);
		}
		return jContentPane;
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		jContentPane.setVisible(visible);
	}

	public void deselect() {
		lastSelection = null;
		getSelectButton().setSelected(true);
		SceneManager.getInstance().setOperation(Operation.SELECT);
	}

	private JMenuItem getSaveasMenuItem() {
		if (saveasMenuItem == null) {
			saveasMenuItem = new JMenuItem("Save As...");
			saveasMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
						try {
							Scene.getInstance().save(fileChooser.getSelectedFile());
						} catch (Throwable err) {
							err.printStackTrace();
							JOptionPane.showMessageDialog(MainFrame.this, err.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
						}
				}
			});
		}
		return saveasMenuItem;
	}

	private JToggleButton getAnnotationToggleButton() {
		if (annotationToggleButton == null) {
			annotationToggleButton = new JToggleButton();
			annotationToggleButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/annotation.png")));
			annotationToggleButton.setToolTipText("Show annotations");
			annotationToggleButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Scene.getInstance().setAnnotationsVisible(annotationToggleButton.isSelected());
				}
			});
		}
		return annotationToggleButton;
	}

	private JMenu getViewMenu() {
		if (viewMenu == null) {
			viewMenu = new JMenu("View");
			viewMenu.addMenuListener(new MenuListener() {
				public void menuCanceled(MenuEvent e) {
				}

				public void menuDeselected(MenuEvent e) {
				}

				public void menuSelected(MenuEvent e) {
					getSelectButton().setSelected(true);
					SceneManager.getInstance().setOperation(SceneManager.Operation.SELECT);
				}
			});
			viewMenu.add(getUnitsMenu());
			viewMenu.add(getLightingMenu());
			viewMenu.add(getShadowMenu());
			viewMenu.add(getTextureCheckBoxMenuItem());
			viewMenu.add(getColorMenuItem());
			viewMenu.add(getWallThicknessMenuItem());
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
					getSelectButton().setSelected(true);
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
				private String previousScaleInput = "200%";

				public void actionPerformed(ActionEvent e) {
					boolean done = false;
					while (!done) {
						// previousScaleInput = "200%";
						String result = JOptionPane.showInputDialog(MainFrame.this, "Please enter the scale factor in percentage:", previousScaleInput);
						if (result == null)
							break;
						else
							result = result.trim();
						if (result.endsWith("%") && result.length() >= 1)
							result = result.substring(0, result.length() - 1);
						try {
							final Scene scene = Scene.getInstance();
							scene.setAnnotationScale(scene.getAnnotationScale() * Double.parseDouble(result) / 100.0);
							previousScaleInput = result + "%";
							done = true;
						} catch (Exception err) {
							err.printStackTrace();
							JOptionPane.showMessageDialog(MainFrame.this, err.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
						}
					}
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
//					HousePart.setTextureEnabled(textureCheckBoxMenuItem.isSelected());
					Scene.getInstance().setTextureEnabled(textureCheckBoxMenuItem.isSelected());
				}
			});
		}
		return textureCheckBoxMenuItem;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"

class ExtensionFileFilter extends javax.swing.filechooser.FileFilter {
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
		toLower(this.extensions);
	}

	private void toLower(String array[]) {
		for (int i = 0, n = array.length; i < n; i++) {
			array[i] = array[i].toLowerCase();
		}
	}

	public String getDescription() {
		return description;
	}

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