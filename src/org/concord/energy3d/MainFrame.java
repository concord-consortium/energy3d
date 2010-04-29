package org.concord.energy3d;

import java.awt.BorderLayout;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.Operation;

import com.ardor3d.framework.jogl.JoglAwtCanvas;


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
	private JButton saveButton = null;
	private JButton newButton = null;
	private JButton openButton = null;
	private JToggleButton rotAnimButton = null;
	private JToggleButton gridButton = null;
	private JToggleButton snapButton = null;
	private JToggleButton floorButton = null;
	private JToggleButton roofHipButton = null;
	private JToggleButton resizeButton = null;
	private JToggleButton sunButton = null;
	private JToggleButton sunAnimButton = null;

	/**
	 * This method initializes appMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	private JMenuBar getAppMenuBar() {
		if (appMenuBar == null) {
			appMenuBar = new JMenuBar();
			appMenuBar.add(getFileMenu());
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
			appToolbar.add(getNewButton());
			appToolbar.add(getOpenButton());
			appToolbar.add(getSaveButton());
			appToolbar.addSeparator();
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
			wallButton.setText("Wall     ");
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
			windowButton.setText("Window     ");			
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
					JoglAwtCanvas canvas = SceneManager.getInstance().getCanvas();
					if (topViewButton.isSelected())
						SceneManager.getInstance().topCameraView(canvas);
					else
						SceneManager.getInstance().resetCamera(canvas);
				}
			});
		}
		return topViewButton;
	}

	/**
	 * This method initializes saveButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getSaveButton() {
		if (saveButton == null) {
			saveButton = new JButton();
			saveButton.setText("Save");
			saveButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Scene.getInstance().save();
				}
			});
		}
		return saveButton;
	}

	/**
	 * This method initializes newButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getNewButton() {
		if (newButton == null) {
			newButton = new JButton();
			newButton.setText("New");
			newButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Scene.getInstance().newFile();
				}
			});
		}
		return newButton;
	}

	/**
	 * This method initializes openButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getOpenButton() {
		if (openButton == null) {
			openButton = new JButton();
			openButton.setText("Open");
			openButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Scene.open();
				}
			});
		}
		return openButton;
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
