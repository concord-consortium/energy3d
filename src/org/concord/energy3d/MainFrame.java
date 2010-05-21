package org.concord.energy3d;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.CameraMode;
import org.concord.energy3d.scene.SceneManager.Operation;


public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L; 
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
	/**
	 * This method initializes appMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	private JMenuBar getAppMenuBar() {
		if (appMenuBar == null) {
			appMenuBar = new JMenuBar();
			appMenuBar.add(getFileMenu());
			appMenuBar.add(getCameraMenu());
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
			fileMenu.setText("File");
			fileMenu.add(getNewMenuItem());
			fileMenu.add(getOpenMenuItem());
			fileMenu.add(getSaveMenuItem());
			fileMenu.add(getPreviewMenuItem());
			fileMenu.add(getPrintMenuItem());
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
			appToolbar.add(getRotAnimButton());
			appToolbar.add(getTopViewButton());
			appToolbar.add(getGridButton());
			appToolbar.add(getSnapButton());
			
			ButtonGroup bg = new ButtonGroup();
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
	private JToggleButton getSelectButton() {
		if (selectButton == null) {
			selectButton = new JToggleButton();
			selectButton.setText("Select");
			selectButton.setSelected(true);
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
			wallButton.setText("Wall");
			wallButton.setMaximumSize(new Dimension(38, 28));
			wallButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_WALL);
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
			doorButton.setText("Door");
			doorButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_DOOR);
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
			roofButton.setText("Roof");
			roofButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_ROOF);
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
			windowButton.setText("Window");			
			windowButton.setMaximumSize(new Dimension(59, 28));
			windowButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_WINDOW);
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
			foundationButton.setText("Foundation");
			foundationButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_FOUNDATION);
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
			lightButton.setText("Light");
			lightButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setLighting(lightButton.isSelected());
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
			topViewButton.setText("Top View");
			topViewButton.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
//					JoglAwtCanvas canvas = SceneManager.getInstance().getCanvas();
					if (topViewButton.isSelected())
						SceneManager.getInstance().topCameraView();
					else
						SceneManager.getInstance().resetCamera();
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
			rotAnimButton.setText("Rotate Anim");
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
			gridButton.setText("Grid");
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
			snapButton.setText("Snap");
			snapButton.setSelected(true);
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
			floorButton.setText("Floor");
			floorButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_FLOOR);
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
			roofHipButton.setText("Roof Hip");
			roofHipButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_ROOF_HIP);
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
			resizeButton.setText("Resize");
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
			sunButton.setText("Sun");
			sunButton.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					SceneManager.getInstance().setSunControl(sunButton.isSelected());
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
			sunAnimButton.setText("Sun Anim");
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
			openMenuItem = new JMenuItem("Open");
			openMenuItem.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Scene.open();
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
					Scene.getInstance().save();
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
					SceneManager.getInstance().print();
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
	private JCheckBoxMenuItem getPreviewMenuItem() {
		if (previewMenuItem == null) {
			previewMenuItem = new JCheckBoxMenuItem("Print Preview");
			previewMenuItem.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
//					Scene.getInstance().setFlatten(previewMenuItem.isSelected());
					SceneManager.getInstance().setPrintPreview(previewMenuItem.isSelected());					
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
		 * @param args
		 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
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
	public MainFrame() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(900, 600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setJMenuBar(getAppMenuBar());
		this.setContentPane(getJContentPane());
		this.setTitle("Solar House Simulation");
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
//			jContentPane.setVisible(false);
			jContentPane.add(getAppToolbar(), BorderLayout.NORTH);
		}
		return jContentPane;
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		jContentPane.setVisible(visible);
	}
}
