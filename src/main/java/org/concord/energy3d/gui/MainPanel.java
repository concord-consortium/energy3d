package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.scene.PrintController;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.Operation;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.undo.AnimateSunCommand;
import org.concord.energy3d.undo.RotateBuildingCommand;
import org.concord.energy3d.undo.ShowAnnotationCommand;
import org.concord.energy3d.undo.ShowHeliodonCommand;
import org.concord.energy3d.undo.ShowShadowCommand;
import org.concord.energy3d.undo.SpinViewCommand;
import org.concord.energy3d.util.Config;

public class MainPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final MainPanel instance = new MainPanel();
	private MainFrame mainFrame;
	private JToolBar appToolbar;
	private JToggleButton selectButton;
	private JToggleButton wallButton;
	private JToggleButton roofButton;
	private JToggleButton windowButton;
	private JToggleButton platformButton;
	private JToggleButton shadowButton;
	private JToggleButton spinViewButton;
	private JToggleButton resizeButton;
	private JToggleButton heliodonButton;
	private JToggleButton sunAnimButton;
	private JToggleButton annotationToggleButton;
	private JToggleButton previewButton;
	private JToggleButton zoomButton;
	private JToggleButton noteButton;
	private JSplitPane energyCanvasNoteSplitPane;
	private EnergyPanel energyPanel;
	private JPanel canvasPanel;
	private JToggleButton energyPanelToggleButton;
	private JToggleButton energyViewButton;
	private JSplitPane canvasNoteSplitPane;
	private JScrollPane noteScrollPane;
	private JTextArea noteTextArea;
	private JToggleButton solarPanelButton;
	private JToggleButton treeButton;
	private JToggleButton sensorButton;
	private JToggleButton miscButton;
	private JButton rotateButton;
	private JButton treeArrowButton;
	private JButton roofArrowButton;
	private JButton miscArrowButton;
	private int defaultDividerSize = -1;
	private final JPopupMenu treeMenu;
	private final JPopupMenu roofMenu;
	private final JPopupMenu miscMenu;
	private Operation treeCommand = SceneManager.Operation.DRAW_DOGWOOD;
	private Operation roofCommand = SceneManager.Operation.DRAW_ROOF_PYRAMID;
	private Operation miscCommand = SceneManager.Operation.DRAW_DOOR;
	private final double buildingRotationAngleAbsolute = Math.PI / 18;
	private double buildingRotationAngle = -buildingRotationAngleAbsolute;

	private final MouseAdapter refreshUponMouseExit = new MouseAdapter() {
		@Override
		public void mouseExited(final MouseEvent e) {
			SceneManager.getInstance().refresh();
		}
	};

	MouseAdapter operationStickAndRefreshUponExit = new MouseAdapter() {
		@Override
		public void mouseClicked(final MouseEvent e) {
			if (e.getClickCount() > 1)
				SceneManager.getInstance().setOperationStick(true);
		}

		@Override
		public void mouseExited(final MouseEvent e) {
			SceneManager.getInstance().refresh();
		}
	};

	public static MainPanel getInstance() {
		return instance;
	}

	public void setMainFrame(final MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public MainFrame getMainFrame() {
		return mainFrame;
	}

	private MainPanel() {
		super();
		System.out.println("Version: " + Config.VERSION);
		System.out.print("Initiating GUI Panel...");
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
		initialize();

		// create roof menu
		final JCheckBoxMenuItem miPyramidRoof = new JCheckBoxMenuItem("Pyramid Roof", new ImageIcon(getClass().getResource("icons/roof_pyramid.png")), true);
		final JCheckBoxMenuItem miHipRoof = new JCheckBoxMenuItem("Hip Roof", new ImageIcon(getClass().getResource("icons/roof_hip.png")));
		final JCheckBoxMenuItem miShedRoof = new JCheckBoxMenuItem("Shed Roof", new ImageIcon(getClass().getResource("icons/roof_shed.png")));
		final JCheckBoxMenuItem miCustomRoof = new JCheckBoxMenuItem("Custom Roof", new ImageIcon(getClass().getResource("icons/roof_custom.png")));
		final JCheckBoxMenuItem miGableRoof = new JCheckBoxMenuItem("Gable Conversion", new ImageIcon(getClass().getResource("icons/roof_gable.png")));
		final ActionListener roofAction = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JCheckBoxMenuItem selected = (JCheckBoxMenuItem) e.getSource();
				roofButton.setIcon(selected.getIcon());
				if (selected == miPyramidRoof) {
					roofCommand = SceneManager.Operation.DRAW_ROOF_PYRAMID;
					roofButton.setToolTipText("Draw pyramid roof");
				} else if (selected == miHipRoof) {
					roofCommand = SceneManager.Operation.DRAW_ROOF_HIP;
					roofButton.setToolTipText("Draw hip roof");
				} else if (selected == miShedRoof) {
					roofCommand = SceneManager.Operation.DRAW_ROOF_SHED;
					roofButton.setToolTipText("Draw shed roof");
				} else if (selected == miCustomRoof) {
					roofCommand = SceneManager.Operation.DRAW_ROOF_CUSTOM;
					roofButton.setToolTipText("Draw custom roof");
				} else {
					roofCommand = Operation.DRAW_ROOF_GABLE;
					roofButton.setToolTipText("Convert to gable roof");
				}
				SceneManager.getInstance().setOperation(roofCommand);
				roofButton.setSelected(true);
				((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
			}
		};
		miPyramidRoof.addActionListener(roofAction);
		miHipRoof.addActionListener(roofAction);
		miShedRoof.addActionListener(roofAction);
		miCustomRoof.addActionListener(roofAction);
		miGableRoof.addActionListener(roofAction);
		roofMenu = new JPopupMenu();
		roofMenu.add(miPyramidRoof);
		roofMenu.add(miHipRoof);
		roofMenu.add(miShedRoof);
		roofMenu.add(miCustomRoof);
		roofMenu.addSeparator();
		roofMenu.add(miGableRoof);
		ButtonGroup bg = new ButtonGroup();
		bg.add(miPyramidRoof);
		bg.add(miHipRoof);
		bg.add(miShedRoof);
		bg.add(miCustomRoof);
		bg.add(miGableRoof);

		// create tree menu
		final JCheckBoxMenuItem miTree1 = new JCheckBoxMenuItem("Dogwood (Deciduous, Height=8m)", new ImageIcon(getClass().getResource("icons/dogwood.png")), true);
		final JCheckBoxMenuItem miTree2 = new JCheckBoxMenuItem("Maple (Deciduous, Height=12m)", new ImageIcon(getClass().getResource("icons/maple.png")));
		final JCheckBoxMenuItem miTree3 = new JCheckBoxMenuItem("Elm (Deciduous, Height=15m)", new ImageIcon(getClass().getResource("icons/elm.png")));
		final JCheckBoxMenuItem miTree4 = new JCheckBoxMenuItem("Oak (Deciduous, Height=16m)", new ImageIcon(getClass().getResource("icons/oak.png")));
		final JCheckBoxMenuItem miTree5 = new JCheckBoxMenuItem("Linden (Deciduous, Height=24m)", new ImageIcon(getClass().getResource("icons/linden.png")));
		final JCheckBoxMenuItem miTree6 = new JCheckBoxMenuItem("Cottonwood (Deciduous, Height=20m)", new ImageIcon(getClass().getResource("icons/cottonwood.png")));
		final JCheckBoxMenuItem miTree7 = new JCheckBoxMenuItem("Pine (Evergreen, Height=16m)", new ImageIcon(getClass().getResource("icons/pine.png")));
		final ActionListener treeAction = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JCheckBoxMenuItem selected = (JCheckBoxMenuItem) e.getSource();
				treeButton.setIcon(selected.getIcon());
				if (selected == miTree1) {
					treeCommand = SceneManager.Operation.DRAW_DOGWOOD;
					treeButton.setToolTipText("Insert a dogwood tree (deciduous, height=8m)");
				} else if (selected == miTree2) {
					treeCommand = SceneManager.Operation.DRAW_MAPLE;
					treeButton.setToolTipText("Insert a maple tree (deciduous, height=12m)");
				} else if (selected == miTree3) {
					treeCommand = SceneManager.Operation.DRAW_ELM;
					treeButton.setToolTipText("Insert an elm tree (deciduous, height=15m)");
				} else if (selected == miTree4) {
					treeCommand = SceneManager.Operation.DRAW_OAK;
					treeButton.setToolTipText("Insert an oak tree (deciduous, height=16m)");
				} else if (selected == miTree5) {
					treeCommand = SceneManager.Operation.DRAW_LINDEN;
					treeButton.setToolTipText("Insert a linden tree (deciduous, height=24m)");
				} else if (selected == miTree6) {
					treeCommand = SceneManager.Operation.DRAW_COTTONWOOD;
					treeButton.setToolTipText("Insert a cottonwood tree (deciduous, height=20m)");
				} else {
					treeCommand = SceneManager.Operation.DRAW_PINE;
					treeButton.setToolTipText("Insert a pine tree (evergreen, height=16m)");
				}
				SceneManager.getInstance().setOperation(treeCommand);
				treeButton.setSelected(true);
				((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
			}
		};
		miTree1.addActionListener(treeAction);
		miTree2.addActionListener(treeAction);
		miTree3.addActionListener(treeAction);
		miTree4.addActionListener(treeAction);
		miTree5.addActionListener(treeAction);
		miTree6.addActionListener(treeAction);
		miTree7.addActionListener(treeAction);
		treeMenu = new JPopupMenu();
		treeMenu.add(miTree1);
		treeMenu.add(miTree2);
		treeMenu.add(miTree3);
		treeMenu.add(miTree4);
		treeMenu.add(miTree5);
		treeMenu.add(miTree6);
		treeMenu.add(miTree7);
		bg = new ButtonGroup();
		bg.add(miTree1);
		bg.add(miTree2);
		bg.add(miTree3);
		bg.add(miTree4);
		bg.add(miTree5);
		bg.add(miTree6);
		bg.add(miTree7);

		// create misc menu
		final JCheckBoxMenuItem miDoor = new JCheckBoxMenuItem("Door", new ImageIcon(getClass().getResource("icons/door.png")), true);
		final JCheckBoxMenuItem miFloor = new JCheckBoxMenuItem("Floor", new ImageIcon(getClass().getResource("icons/floor.png")));
		final JCheckBoxMenuItem miJane = new JCheckBoxMenuItem("Jane", new ImageIcon(getClass().getResource("icons/jane.png")), true);
		final JCheckBoxMenuItem miJeni = new JCheckBoxMenuItem("Jeni", new ImageIcon(getClass().getResource("icons/jenny.png")), true);
		final JCheckBoxMenuItem miJill = new JCheckBoxMenuItem("Jill", new ImageIcon(getClass().getResource("icons/jill.png")), true);
		final JCheckBoxMenuItem miJack = new JCheckBoxMenuItem("Jack", new ImageIcon(getClass().getResource("icons/jack.png")), true);
		final JCheckBoxMenuItem miJohn = new JCheckBoxMenuItem("John", new ImageIcon(getClass().getResource("icons/john.png")), true);
		final JCheckBoxMenuItem miJose = new JCheckBoxMenuItem("Jose", new ImageIcon(getClass().getResource("icons/jose.png")), true);
		final ActionListener miscAction = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JCheckBoxMenuItem selected = (JCheckBoxMenuItem) e.getSource();
				miscButton.setIcon(selected.getIcon());
				if (selected == miDoor) {
					miscCommand = SceneManager.Operation.DRAW_DOOR;
					miscButton.setToolTipText("Draw door");
				} else if (selected == miJane) {
					miscCommand = SceneManager.Operation.DRAW_JANE;
					miscButton.setToolTipText("Draw Jane");
				} else if (selected == miJeni) {
					miscCommand = SceneManager.Operation.DRAW_JENI;
					miscButton.setToolTipText("Draw Jeni");
				} else if (selected == miJill) {
					miscCommand = SceneManager.Operation.DRAW_JILL;
					miscButton.setToolTipText("Draw Jill");
				} else if (selected == miJack) {
					miscCommand = SceneManager.Operation.DRAW_JACK;
					miscButton.setToolTipText("Draw Jack");
				} else if (selected == miJohn) {
					miscCommand = SceneManager.Operation.DRAW_JOHN;
					miscButton.setToolTipText("Draw John");
				} else if (selected == miJose) {
					miscCommand = SceneManager.Operation.DRAW_JOSE;
					miscButton.setToolTipText("Draw Jose");
				} else {
					miscCommand = SceneManager.Operation.DRAW_FLOOR;
					miscButton.setToolTipText("Draw floor");
				}
				SceneManager.getInstance().setOperation(miscCommand);
				miscButton.setSelected(true);
				((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
			}
		};
		miDoor.addActionListener(miscAction);
		miFloor.addActionListener(miscAction);
		miJane.addActionListener(miscAction);
		miJeni.addActionListener(miscAction);
		miJill.addActionListener(miscAction);
		miJack.addActionListener(miscAction);
		miJohn.addActionListener(miscAction);
		miJose.addActionListener(miscAction);
		miscMenu = new JPopupMenu();
		miscMenu.add(miDoor);
		miscMenu.add(miFloor);
		miscMenu.addSeparator();
		miscMenu.add(miJane);
		miscMenu.add(miJeni);
		miscMenu.add(miJill);
		miscMenu.add(miJack);
		miscMenu.add(miJohn);
		miscMenu.add(miJose);
		bg = new ButtonGroup();
		bg.add(miDoor);
		bg.add(miFloor);
		bg.add(miJane);
		bg.add(miJeni);
		bg.add(miJill);
		bg.add(miJack);
		bg.add(miJohn);
		bg.add(miJose);

		System.out.println("done");
	}

	private void initialize() {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		this.setSize(1000, 300);
		setLayout(new BorderLayout());
		this.add(getAppToolbar(), BorderLayout.NORTH);
		this.add(getEnergyCanvasNoteSplitPane(), BorderLayout.CENTER);
	}

	public JToolBar getAppToolbar() {
		if (appToolbar == null) {
			appToolbar = new JToolBar();
			appToolbar.setFloatable(false);
			appToolbar.add(getSelectButton());
			appToolbar.add(getZoomButton());
			appToolbar.add(getSpinViewButton());
			appToolbar.add(getPreviewButton());
			appToolbar.add(getNoteButton());
			appToolbar.addSeparator();
			appToolbar.add(getAnnotationToggleButton());
			appToolbar.add(getResizeButton());
			appToolbar.add(getRotateButton());
			appToolbar.addSeparator();
			appToolbar.add(getPlatformButton());
			appToolbar.add(getWallButton());
			appToolbar.add(getWindowButton());
			appToolbar.add(getSolarPanelButton());
			appToolbar.add(getSensorButton());
			appToolbar.add(getRoofButton());
			appToolbar.add(getRoofArrowButton());
			appToolbar.add(getMiscButton());
			appToolbar.add(getMiscArrowButton());
			appToolbar.add(getTreeButton());
			appToolbar.add(getTreeArrowButton());
			appToolbar.addSeparator();
			appToolbar.add(getShadowButton());
			appToolbar.add(getHeliodonButton());
			appToolbar.add(getSunAnimationButton());
			appToolbar.add(getEnergyViewButton());
			appToolbar.add(getEnergyPanelToggleButton());
			final ButtonGroup bg = new ButtonGroup();
			bg.add(selectButton);
			bg.add(zoomButton);
			bg.add(resizeButton);
			bg.add(platformButton);
			bg.add(wallButton);
			bg.add(windowButton);
			bg.add(roofButton);
			bg.add(solarPanelButton);
			bg.add(sensorButton);
			bg.add(treeButton);
			bg.add(miscButton);
		}
		return appToolbar;
	}

	public JToggleButton getSelectButton() {
		if (selectButton == null) {
			selectButton = new JToggleButton();
			selectButton.addMouseListener(refreshUponMouseExit);
			selectButton.setSelected(true);
			selectButton.setToolTipText("Select");
			selectButton.setIcon(new ImageIcon(MainPanel.class.getResource("icons/select.png")));
			selectButton.setFocusable(false);
			selectButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.SELECT);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
		}
		return selectButton;
	}

	private JToggleButton getWallButton() {
		if (wallButton == null) {
			wallButton = new JToggleButton();
			wallButton.setIcon(new ImageIcon(getClass().getResource("icons/wall.png")));
			wallButton.setToolTipText("Draw wall");
			wallButton.setFocusable(false);
			wallButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_WALL);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			wallButton.addMouseListener(operationStickAndRefreshUponExit);
		}
		return wallButton;
	}

	private JToggleButton getMiscButton() {
		if (miscButton == null) {
			miscButton = new JToggleButton();
			miscButton.setText("");
			miscButton.setToolTipText("Draw door");
			miscButton.setIcon(new ImageIcon(getClass().getResource("icons/door.png")));
			miscButton.setFocusable(false);
			miscButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().setOperation(miscCommand);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			miscButton.addMouseListener(operationStickAndRefreshUponExit);
		}
		return miscButton;
	}

	private JButton getMiscArrowButton() {
		if (miscArrowButton == null) {
			miscArrowButton = new JButton();
			miscArrowButton.setFocusable(false);
			final Dimension d = new Dimension(12, miscButton.getMaximumSize().height);
			miscArrowButton.setMaximumSize(d);
			miscArrowButton.setIcon(new Symbol.Arrow(Color.BLACK, d.width, d.height));
			miscArrowButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					miscMenu.show(miscButton, 0, miscButton.getHeight());
				}
			});
			miscArrowButton.setBorder(BorderFactory.createEmptyBorder());
			miscArrowButton.setFocusPainted(false);
		}
		return miscArrowButton;
	}

	private JToggleButton getWindowButton() {
		if (windowButton == null) {
			windowButton = new JToggleButton();
			windowButton.setIcon(new ImageIcon(getClass().getResource("icons/window.png")));
			windowButton.setToolTipText("Draw window");
			windowButton.setFocusable(false);
			windowButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_WINDOW);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			windowButton.addMouseListener(operationStickAndRefreshUponExit);
		}
		return windowButton;
	}

	private JToggleButton getPlatformButton() {
		if (platformButton == null) {
			platformButton = new JToggleButton();
			platformButton.setIcon(new ImageIcon(getClass().getResource("icons/foundation.png")));
			platformButton.setToolTipText("Draw platform");
			platformButton.setFocusable(false);
			platformButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_FOUNDATION);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			platformButton.addMouseListener(operationStickAndRefreshUponExit);
		}
		return platformButton;
	}

	public JToggleButton getShadowButton() {
		if (shadowButton == null) {
			shadowButton = new JToggleButton();
			shadowButton.addMouseListener(refreshUponMouseExit);
			shadowButton.setIcon(new ImageIcon(getClass().getResource("icons/shadow.png")));
			shadowButton.setToolTipText("Show shadows");
			shadowButton.setFocusable(false);
			shadowButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().getUndoManager().addEdit(new ShowShadowCommand());
					if (SceneManager.getInstance().isSunAnimation() || Heliodon.getInstance().isNightTime())
						SceneManager.getInstance().setShading(shadowButton.isSelected());
					else
						SceneManager.getInstance().setShading(false);
					SceneManager.getInstance().setShadow(shadowButton.isSelected());
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
					disableSunAnim();
					// Scene.getInstance().setEdited(true, false); // shadow not saved -- make sense because it doesn't work on some machines
				}
			});
		}
		return shadowButton;
	}

	public JToggleButton getSpinViewButton() {
		if (spinViewButton == null) {
			spinViewButton = new JToggleButton();
			spinViewButton.addMouseListener(refreshUponMouseExit);
			spinViewButton.setIcon(new ImageIcon(getClass().getResource("icons/spin.png")));
			spinViewButton.setToolTipText("Spin view");
			spinViewButton.setFocusable(false);
			spinViewButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().getUndoManager().addEdit(new SpinViewCommand());
					SceneManager.getInstance().toggleSpinView();
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
		}
		return spinViewButton;
	}

	public JToggleButton getHeliodonButton() {
		if (heliodonButton == null) {
			heliodonButton = new JToggleButton();
			heliodonButton.addMouseListener(refreshUponMouseExit);
			heliodonButton.setIcon(new ImageIcon(getClass().getResource("icons/sun_heliodon.png")));
			heliodonButton.setToolTipText("Show heliodon");
			heliodonButton.setFocusable(false);
			heliodonButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					SceneManager.getInstance().getUndoManager().addEdit(new ShowHeliodonCommand());
					SceneManager.getInstance().setHeliodonVisible(heliodonButton.isSelected());
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
					disableSunAnim();
					Scene.getInstance().setEdited(true, false);
				}
			});
		}
		return heliodonButton;
	}

	public JToggleButton getSunAnimationButton() {
		if (sunAnimButton == null) {
			sunAnimButton = new JToggleButton();
			sunAnimButton.addMouseListener(refreshUponMouseExit);
			sunAnimButton.setIcon(new ImageIcon(getClass().getResource("icons/sun_anim.png")));
			sunAnimButton.setEnabled(false);
			sunAnimButton.setToolTipText("Animate sun path");
			sunAnimButton.setFocusable(false);
			sunAnimButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					SceneManager.getInstance().getUndoManager().addEdit(new AnimateSunCommand());
					SceneManager.getInstance().setSunAnimation(sunAnimButton.isSelected());
					if (shadowButton.isSelected())
						SceneManager.getInstance().setShading(Heliodon.getInstance().isNightTime());
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
		}
		return sunAnimButton;
	}

	public JToggleButton getPreviewButton() {
		if (previewButton == null) {
			previewButton = new JToggleButton();
			previewButton.addMouseListener(refreshUponMouseExit);
			previewButton.setIcon(new ImageIcon(getClass().getResource("icons/print_preview.png")));
			previewButton.setToolTipText("Preview printable parts");
			previewButton.setFocusable(false);
			// must be ItemListner to be triggered when selection is changed by code
			previewButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (mainFrame != null) {
						mainFrame.getPreviewMenuItem().setSelected(previewButton.isSelected());
						mainFrame.getEditMenu().setEnabled(!previewButton.isSelected());
					}
					defaultTool();
					PrintController.getInstance().setPrintPreview(previewButton.isSelected());
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
		}
		return previewButton;
	}

	public void defaultTool() {
		getSelectButton().setSelected(true);
		SceneManager.getInstance().setOperation(Operation.SELECT);
		SceneManager.getInstance().refresh();
	}

	public JToggleButton getAnnotationToggleButton() {
		if (annotationToggleButton == null) {
			annotationToggleButton = new JToggleButton();
			annotationToggleButton.setSelected(Config.isApplet() ? false : true);
			annotationToggleButton.addMouseListener(refreshUponMouseExit);
			annotationToggleButton.setIcon(new ImageIcon(getClass().getResource("icons/annotation.png")));
			annotationToggleButton.setToolTipText("Show annotations");
			annotationToggleButton.setFocusable(false);
			annotationToggleButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().getUndoManager().addEdit(new ShowAnnotationCommand());
					Scene.getInstance().setAnnotationsVisible(annotationToggleButton.isSelected());
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
					Scene.getInstance().setEdited(true, false);
				}
			});
		}
		return annotationToggleButton;
	}

	private JToggleButton getZoomButton() {
		if (zoomButton == null) {
			zoomButton = new JToggleButton();
			zoomButton.addMouseListener(refreshUponMouseExit);
			zoomButton.setIcon(new ImageIcon(getClass().getResource("icons/zoom.png")));
			zoomButton.setToolTipText("Zoom");
			zoomButton.setFocusable(false);
			zoomButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.SELECT);
					SceneManager.getInstance().setZoomLock(zoomButton.isSelected());
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
		}
		return zoomButton;
	}

	public void setToolbarEnabledForPreview(final boolean enabled) {
		EventQueue.invokeLater(new Runnable() { // must be run in the event queue as this method may be called in a custom thread
			@Override
			public void run() {
				for (final Component c : getAppToolbar().getComponents()) {
					if (c != getPreviewButton() && c != getSelectButton() && c != getAnnotationToggleButton() && c != getZoomButton() && c != getSpinViewButton()) {
						if (!enabled || c != getSunAnimationButton() || getShadowButton().isSelected() || getHeliodonButton().isSelected())
							c.setEnabled(enabled);
					}
				}
			}
		});
	}

	public void setToolbarEnabledForReplay(final boolean enabled) {
		EventQueue.invokeLater(new Runnable() { // must be run in the event queue as this method may be called in a custom thread
			@Override
			public void run() {
				for (final Component c : getAppToolbar().getComponents()) {
					if (c != getNoteButton() && c != getShadowButton() && c != getEnergyViewButton() && c != getHeliodonButton() && c != getSelectButton() && c != getAnnotationToggleButton() && c != getZoomButton() && c != getSpinViewButton()) {
						if (!enabled || c != getSunAnimationButton() || getShadowButton().isSelected() || getHeliodonButton().isSelected())
							c.setEnabled(enabled);
					}
				}
			}
		});
	}

	private JSplitPane getEnergyCanvasNoteSplitPane() {
		if (energyCanvasNoteSplitPane == null) {
			energyCanvasNoteSplitPane = new JSplitPane();
			energyCanvasNoteSplitPane.setResizeWeight(1.0);
			energyCanvasNoteSplitPane.setRightComponent(getEnergyPanel());
			energyCanvasNoteSplitPane.setLeftComponent(getCanvasNoteSplitPane());
			energyCanvasNoteSplitPane.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
			defaultDividerSize = energyCanvasNoteSplitPane.getDividerSize();
		}
		return energyCanvasNoteSplitPane;
	}

	private EnergyPanel getEnergyPanel() {
		if (energyPanel == null) {
			energyPanel = EnergyPanel.getInstance();
		}
		return energyPanel;
	}

	public JPanel getCanvasPanel() {
		if (canvasPanel == null) {
			canvasPanel = new JPanel();
			canvasPanel.setLayout(new BorderLayout(0, 0));
		}
		return canvasPanel;
	}

	private JToggleButton getEnergyPanelToggleButton() {
		if (energyPanelToggleButton == null) {
			energyPanelToggleButton = new JToggleButton("");
			energyPanelToggleButton.setToolTipText("Show properties panel");
			energyPanelToggleButton.setSelected(true);
			energyPanelToggleButton.setIcon(new ImageIcon(getClass().getResource("icons/chart.png")));
			energyPanelToggleButton.setFocusable(false);
			energyPanelToggleButton.addMouseListener(refreshUponMouseExit);
			energyPanelToggleButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					setSplitComponentVisible(energyPanelToggleButton.isSelected(), getEnergyCanvasNoteSplitPane(), EnergyPanel.getInstance());
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
		}
		return energyPanelToggleButton;
	}

	public JToggleButton getEnergyViewButton() {
		if (energyViewButton == null) {
			energyViewButton = new JToggleButton("");
			energyViewButton.setToolTipText("Calculate energy of the day");
			energyViewButton.setIcon(new ImageIcon(getClass().getResource("icons/heatmap.png")));
			energyViewButton.addMouseListener(refreshUponMouseExit);
			energyViewButton.setFocusable(false);
			energyViewButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					EnergyPanel.getInstance().showHeatMapContrastSlider(energyViewButton.isSelected());
					if (energyViewButton.isSelected()) {
						defaultTool();
						SceneManager.getInstance().autoSelectBuilding(false);
					} else {
						EnergyPanel.getInstance().getDailyEnergyGraph().removeGraph();
					}
					SceneManager.getInstance().computeEnergyView(energyViewButton.isSelected());
				}
			});
		}
		return energyViewButton;
	}

	private JSplitPane getCanvasNoteSplitPane() {
		if (canvasNoteSplitPane == null) {
			canvasNoteSplitPane = new JSplitPane();
			canvasNoteSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			canvasNoteSplitPane.setTopComponent(getCanvasPanel());
			canvasNoteSplitPane.setBottomComponent(getNoteScrollPane());
			canvasNoteSplitPane.setResizeWeight(0.6);
			canvasNoteSplitPane.setDividerSize(0);
			getNoteScrollPane().setVisible(false);
		}
		return canvasNoteSplitPane;
	}

	private JScrollPane getNoteScrollPane() {
		if (noteScrollPane == null) {
			noteScrollPane = new JScrollPane(getNoteTextArea(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		}
		return noteScrollPane;
	}

	public JTextArea getNoteTextArea() {
		if (noteTextArea == null) {
			noteTextArea = new JTextArea(new MyPlainDocument()); // want to keep a copy of what was removed
			// noteTextArea.setWrapStyleWord(true); // don't call this, line break malfunctions
			// noteTextArea.setLineWrap(true);
			noteTextArea.getDocument().addDocumentListener(new DocumentListener() {
				public void updateEditFlag() {
					Scene.getInstance().setEdited(true, false);
					if (!Config.isApplet())
						MainFrame.getInstance().updateTitleBar();
				}

				@Override
				public void removeUpdate(final DocumentEvent e) {
					updateEditFlag();
				}

				@Override
				public void insertUpdate(final DocumentEvent e) {
					updateEditFlag();
				}

				@Override
				public void changedUpdate(final DocumentEvent e) {
				}
			});
		}
		return noteTextArea;
	}

	public void setNoteVisible(final boolean visible) {
		setSplitComponentVisible(visible, getCanvasNoteSplitPane(), noteScrollPane);
	}

	public boolean isNoteVisible() {
		return noteTextArea.isShowing();
	}

	private void setSplitComponentVisible(final boolean visible, final JSplitPane splitPane, final Component component) {
		getCanvasNoteSplitPane().getSize();
		getCanvasPanel().getPreferredSize();
		component.setVisible(visible);
		splitPane.setDividerSize(visible ? defaultDividerSize : 0);
		splitPane.resetToPreferredSizes();
	}

	private void disableSunAnim() {
		final boolean enableSunAnim = shadowButton.isSelected() || heliodonButton.isSelected();
		sunAnimButton.setEnabled(enableSunAnim);
		if (!enableSunAnim && sunAnimButton.isSelected()) {
			sunAnimButton.setSelected(false);
			SceneManager.getInstance().setSunAnimation(false);
		}
	}

	private JToggleButton getSolarPanelButton() {
		if (solarPanelButton == null) {
			solarPanelButton = new JToggleButton("");
			solarPanelButton.setToolTipText("Add solar panel");
			solarPanelButton.setIcon(new ImageIcon(getClass().getResource("icons/solarpanel.png")));
			solarPanelButton.setFocusable(false);
			solarPanelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_SOLAR_PANEL);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			solarPanelButton.addMouseListener(operationStickAndRefreshUponExit);
		}
		return solarPanelButton;
	}

	private JToggleButton getSensorButton() {
		if (sensorButton == null) {
			sensorButton = new JToggleButton("");
			sensorButton.setToolTipText("Add sensor module");
			sensorButton.setIcon(new ImageIcon(getClass().getResource("icons/sensor.png")));
			sensorButton.setFocusable(false);
			sensorButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_SENSOR);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			sensorButton.addMouseListener(operationStickAndRefreshUponExit);
		}
		return sensorButton;
	}

	private JToggleButton getTreeButton() {
		if (treeButton == null) {
			treeButton = new JToggleButton();
			treeButton.setToolTipText("Insert a dogwood tree");
			treeButton.setIcon(new ImageIcon(getClass().getResource("icons/dogwood.png")));
			treeButton.setFocusable(false);
			treeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().setOperation(treeCommand);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			treeButton.addMouseListener(operationStickAndRefreshUponExit);
		}
		return treeButton;
	}

	private JButton getTreeArrowButton() {
		if (treeArrowButton == null) {
			treeArrowButton = new JButton();
			treeArrowButton.setFocusable(false);
			final Dimension d = new Dimension(12, treeButton.getMaximumSize().height);
			treeArrowButton.setMaximumSize(d);
			treeArrowButton.setIcon(new Symbol.Arrow(Color.BLACK, d.width, d.height));
			treeArrowButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					treeMenu.show(treeButton, 0, treeButton.getHeight());
				}
			});
			treeArrowButton.setBorder(BorderFactory.createEmptyBorder());
			treeArrowButton.setFocusPainted(false);
		}
		return treeArrowButton;
	}

	private JToggleButton getRoofButton() {
		if (roofButton == null) {
			roofButton = new JToggleButton();
			roofButton.setIcon(new ImageIcon(getClass().getResource("icons/roof_pyramid.png")));
			roofButton.setToolTipText("Draw pyramid roof");
			roofButton.setFocusable(false);
			roofButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().setOperation(roofCommand);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			roofButton.addMouseListener(operationStickAndRefreshUponExit);
		}
		return roofButton;
	}

	private JButton getRoofArrowButton() {
		if (roofArrowButton == null) {
			roofArrowButton = new JButton();
			roofArrowButton.setFocusable(false);
			final Dimension d = new Dimension(12, roofButton.getMaximumSize().height);
			roofArrowButton.setMaximumSize(d);
			roofArrowButton.setIcon(new Symbol.Arrow(Color.BLACK, d.width, d.height));
			roofArrowButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					roofMenu.show(roofButton, 0, roofButton.getHeight());
				}
			});
			roofArrowButton.setBorder(BorderFactory.createEmptyBorder());
			roofArrowButton.setFocusPainted(false);
		}
		return roofArrowButton;
	}

	private JToggleButton getResizeButton() {
		if (resizeButton == null) {
			resizeButton = new JToggleButton();
			resizeButton.addMouseListener(refreshUponMouseExit);
			resizeButton.setIcon(new ImageIcon(getClass().getResource("icons/resize.png")));
			resizeButton.setToolTipText("Resize or move a building");
			resizeButton.setFocusable(false);
			resizeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.RESIZE);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
		}
		return resizeButton;
	}

	public JButton getRotateButton() {
		if (rotateButton == null) {
			rotateButton = new JButton();
			rotateButton.addMouseListener(refreshUponMouseExit);
			rotateButton.setIcon(new ImageIcon(getClass().getResource("icons/rotate_cw.png")));
			rotateButton.setToolTipText("<html>Rotate building in the clockwise direction; <br>Hold down the SHIFT key and press this button for counter-clockwise rotation.</html>");
			rotateButton.setFocusable(false);
			rotateButton.addMouseListener(new MouseAdapter() {
				private boolean mousePressed = false;

				@Override
				public void mousePressed(final MouseEvent e) {
					energyViewButton.setSelected(false);
					mousePressed = true;
					SceneManager.getInstance().resetBuildingRotationAngleRecorded();
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null || selectedPart instanceof Tree) {
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
							SceneManager.getInstance().refresh();
							EnergyPanel.getInstance().updateProperties();
						} else {
							JOptionPane.showMessageDialog(MainFrame.getInstance(), "You must select a building first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
							return;
						}
					}
					new Thread() {
						@Override
						public void run() {
							while (mousePressed) {
								SceneManager.getTaskManager().update(new Callable<Object>() {
									@Override
									public Object call() throws Exception {
										SceneManager.getInstance().rotateBuilding(buildingRotationAngle, true, true);
										return null;
									}
								});
								final int partCount = Scene.getInstance().getParts().size();
								try {
									Thread.sleep(100 + partCount * 5); // give it enough time for the above call to complete (the more parts it has, the more time it needs)
								} catch (final InterruptedException e) {
								}
							}
							final HousePart hp = SceneManager.getInstance().getSelectedPart();
							if (hp instanceof Foundation)
								SceneManager.getInstance().getUndoManager().addEdit(new RotateBuildingCommand((Foundation) hp, SceneManager.getInstance().getBuildingRotationAngleRecorded()));
						}
					}.start();
				}

				@Override
				public void mouseReleased(final MouseEvent e) {
					mousePressed = false;
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null)
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "No building is selected for rotation.", "No Building", JOptionPane.INFORMATION_MESSAGE);
				}
			});
		}
		return rotateButton;
	}

	public JToggleButton getNoteButton() {
		if (noteButton == null) {
			noteButton = new JToggleButton();
			noteButton.setToolTipText("Show note");
			noteButton.setIcon(new ImageIcon(MainPanel.class.getResource("icons/note.png")));
			noteButton.setFocusable(false);
			noteButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					MainPanel.getInstance().setNoteVisible(noteButton.isSelected());
					if (noteButton.isSelected())
						getNoteTextArea().requestFocusInWindow();
				}
			});
		}
		return noteButton;
	}

	public void setBuildingRotationAngle(final double x) {
		buildingRotationAngle = x;
	}

	public double getBuildingRotationAngleAbsolute() {
		return buildingRotationAngleAbsolute;
	}

}