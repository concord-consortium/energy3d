package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Callable;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
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
import javax.swing.text.BadLocationException;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.logger.SnapshotLogger;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.scene.PrintController;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.Operation;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.undo.AnimateSunCommand;
import org.concord.energy3d.undo.ChangeAzimuthCommand;
import org.concord.energy3d.undo.RotateBuildingCommand;
import org.concord.energy3d.undo.ShowAnnotationCommand;
import org.concord.energy3d.undo.ShowHeliodonCommand;
import org.concord.energy3d.undo.ShowShadowCommand;
import org.concord.energy3d.undo.SpinViewCommand;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.Util;

public class MainPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final MainPanel instance = new MainPanel();
	private JToolBar appToolbar;
	private JToggleButton selectButton;
	private JToggleButton wallButton;
	private JToggleButton roofButton;
	private JToggleButton baseButton;
	private JToggleButton shadowButton;
	private JToggleButton spinViewButton;
	private JToggleButton resizeButton;
	private JToggleButton heliodonButton;
	private JToggleButton sunAnimButton;
	private JToggleButton annotationButton;
	private JToggleButton previewButton;
	private JToggleButton zoomButton;
	private JToggleButton noteButton;
	private JSplitPane energyCanvasNoteSplitPane;
	private EnergyPanel energyPanel;
	private JPanel canvasPanel;
	private JToggleButton energyButton;
	private JSplitPane canvasNoteSplitPane;
	private JScrollPane noteScrollPane;
	private JTextArea noteTextArea;
	private JToggleButton solarButton;
	private JToggleButton miscButton;
	private JButton rotateButton;
	private JButton baseArrowButton;
	private JButton wallArrowButton;
	private JButton roofArrowButton;
	private JButton miscArrowButton;
	private JButton solaArrowButton;
	private int defaultDividerSize = -1;
	private final JPopupMenu baseMenu;
	private final JPopupMenu wallMenu;
	private final JPopupMenu roofMenu;
	private final JPopupMenu miscMenu;
	private final JPopupMenu solaMenu;
	private Operation baseCommand = SceneManager.Operation.DRAW_FOUNDATION;
	private Operation wallCommand = SceneManager.Operation.DRAW_EXTERIOR_WALL;
	private Operation roofCommand = SceneManager.Operation.ADD_ROOF_PYRAMID;
	private Operation miscCommand = SceneManager.Operation.DRAW_WINDOW;
	private Operation solaCommand = SceneManager.Operation.ADD_RACK;
	private final double rotationAngleAbsolute = 5 * Math.PI / 180;
	private double rotationAngle = -rotationAngleAbsolute;
	private String noteString = "";

	private final MouseAdapter refreshUponMouseExit = new MouseAdapter() {
		@Override
		public void mouseExited(final MouseEvent e) {
			SceneManager.getInstance().refresh();
		}
	};

	private final MouseAdapter operationStickAndRefreshUponExit = new MouseAdapter() {
		@Override
		public void mouseClicked(final MouseEvent e) {
			if (e.getClickCount() > 1) {
				SceneManager.getInstance().setOperationStick(true);
			}
		}

		@Override
		public void mouseExited(final MouseEvent e) {
			SceneManager.getInstance().refresh();
		}
	};

	public static MainPanel getInstance() {
		return instance;
	}

	private MainPanel() {
		super();
		System.out.println("Version: " + MainApplication.VERSION);
		System.out.print("Initiating MainPanel...");
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
		initialize();

		// create base menu
		final JCheckBoxMenuItem miFoundation = new JCheckBoxMenuItem("Foundation", new ImageIcon(getClass().getResource("icons/foundation.png")), true);
		final JCheckBoxMenuItem miBox = new JCheckBoxMenuItem("Box", new ImageIcon(getClass().getResource("icons/box.png")), true);
		final ActionListener baseAction = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JCheckBoxMenuItem selected = (JCheckBoxMenuItem) e.getSource();
				baseButton.setIcon(selected.getIcon());
				if (selected == miFoundation) {
					baseCommand = SceneManager.Operation.DRAW_FOUNDATION;
					baseButton.setToolTipText("Draw a foundation");
				} else if (selected == miBox) {
					baseCommand = SceneManager.Operation.ADD_BOX;
					baseButton.setToolTipText("Add a box");
				}
				SceneManager.getInstance().setOperation(baseCommand);
				baseButton.setSelected(true);
				((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
			}
		};
		miFoundation.addActionListener(baseAction);
		miBox.addActionListener(baseAction);
		baseMenu = new JPopupMenu();
		baseMenu.add(miFoundation);
		baseMenu.add(miBox);
		ButtonGroup bg = new ButtonGroup();
		bg.add(miFoundation);
		bg.add(miBox);

		// create wall menu
		final JCheckBoxMenuItem miExteriorWall = new JCheckBoxMenuItem("Exterior Wall", new ImageIcon(getClass().getResource("icons/exterior_wall.png")), true);
		final JCheckBoxMenuItem miInteriorWall = new JCheckBoxMenuItem("Interior Wall", new ImageIcon(getClass().getResource("icons/interior_wall.png")), true);
		miInteriorWall.setEnabled(false);
		final ActionListener wallAction = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JCheckBoxMenuItem selected = (JCheckBoxMenuItem) e.getSource();
				wallButton.setIcon(selected.getIcon());
				if (selected == miExteriorWall) {
					wallCommand = SceneManager.Operation.DRAW_EXTERIOR_WALL;
					wallButton.setToolTipText("Draw an exterior wall");
				} else if (selected == miInteriorWall) {
					wallCommand = SceneManager.Operation.DRAW_INTERIOR_WALL;
					wallButton.setToolTipText("Draw an interior wall");
				}
				SceneManager.getInstance().setOperation(wallCommand);
				wallButton.setSelected(true);
				((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
			}
		};
		miExteriorWall.addActionListener(wallAction);
		miInteriorWall.addActionListener(wallAction);
		wallMenu = new JPopupMenu();
		wallMenu.add(miExteriorWall);
		wallMenu.add(miInteriorWall);
		bg = new ButtonGroup();
		bg.add(miExteriorWall);
		bg.add(miInteriorWall);

		// create roof menu
		final JCheckBoxMenuItem miPyramidRoof = new JCheckBoxMenuItem("Pyramid Roof", new ImageIcon(getClass().getResource("icons/pyramid_roof.png")), true);
		final JCheckBoxMenuItem miHipRoof = new JCheckBoxMenuItem("Hip Roof", new ImageIcon(getClass().getResource("icons/hip_roof.png")));
		final JCheckBoxMenuItem miShedRoof = new JCheckBoxMenuItem("Shed Roof", new ImageIcon(getClass().getResource("icons/shed_roof.png")));
		final JCheckBoxMenuItem miGambrelRoof = new JCheckBoxMenuItem("Gambrel Roof", new ImageIcon(getClass().getResource("icons/gambler_roof.png")));
		final JCheckBoxMenuItem miCustomRoof = new JCheckBoxMenuItem("Custom Roof", new ImageIcon(getClass().getResource("icons/custom_roof.png")));
		final JCheckBoxMenuItem miGableRoof = new JCheckBoxMenuItem("Gable Conversion", new ImageIcon(getClass().getResource("icons/gable.png")));
		final ActionListener roofAction = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JCheckBoxMenuItem selected = (JCheckBoxMenuItem) e.getSource();
				roofButton.setIcon(selected.getIcon());
				if (selected == miPyramidRoof) {
					roofCommand = SceneManager.Operation.ADD_ROOF_PYRAMID;
					roofButton.setToolTipText("Add a pyramid roof");
				} else if (selected == miHipRoof) {
					roofCommand = SceneManager.Operation.ADD_ROOF_HIP;
					roofButton.setToolTipText("Add a hip roof");
				} else if (selected == miShedRoof) {
					roofCommand = SceneManager.Operation.ADD_ROOF_SHED;
					roofButton.setToolTipText("Add a shed roof");
				} else if (selected == miGambrelRoof) {
					roofCommand = SceneManager.Operation.ADD_ROOF_GAMBREL;
					roofButton.setToolTipText("Add a gambrel roof");
				} else if (selected == miCustomRoof) {
					roofCommand = SceneManager.Operation.ADD_ROOF_CUSTOM;
					roofButton.setToolTipText("Add a custom roof");
				} else {
					roofCommand = Operation.GABLE_ROOF;
					roofButton.setToolTipText("Convert to a gable roof");
				}
				SceneManager.getInstance().setOperation(roofCommand);
				roofButton.setSelected(true);
				((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
			}
		};
		miPyramidRoof.addActionListener(roofAction);
		miHipRoof.addActionListener(roofAction);
		miShedRoof.addActionListener(roofAction);
		miGambrelRoof.addActionListener(roofAction);
		miCustomRoof.addActionListener(roofAction);
		miGableRoof.addActionListener(roofAction);
		roofMenu = new JPopupMenu();
		roofMenu.add(miPyramidRoof);
		roofMenu.add(miHipRoof);
		roofMenu.add(miShedRoof);
		roofMenu.add(miGambrelRoof);
		roofMenu.add(miCustomRoof);
		roofMenu.addSeparator();
		roofMenu.add(miGableRoof);
		bg = new ButtonGroup();
		bg.add(miPyramidRoof);
		bg.add(miHipRoof);
		bg.add(miShedRoof);
		bg.add(miGambrelRoof);
		bg.add(miCustomRoof);
		bg.add(miGableRoof);

		// create misc menu
		final JCheckBoxMenuItem miWindow = new JCheckBoxMenuItem("Window", new ImageIcon(getClass().getResource("icons/window.png")), true);
		final JCheckBoxMenuItem miDoor = new JCheckBoxMenuItem("Door", new ImageIcon(getClass().getResource("icons/door.png")), true);
		final JCheckBoxMenuItem miFloor = new JCheckBoxMenuItem("Floor", new ImageIcon(getClass().getResource("icons/floor.png")));
		final JCheckBoxMenuItem miPlant = new JCheckBoxMenuItem("Plant", new ImageIcon(getClass().getResource("icons/plant.png")), true);
		final JCheckBoxMenuItem miHuman = new JCheckBoxMenuItem("Human", new ImageIcon(getClass().getResource("icons/human.png")));
		final JCheckBoxMenuItem miSensor = new JCheckBoxMenuItem("Sensor Module", new ImageIcon(getClass().getResource("icons/sensor.png")));
		final JCheckBoxMenuItem miTapeMeasure = new JCheckBoxMenuItem("Tape Measure", new ImageIcon(getClass().getResource("icons/tape_measure.png")));
		miTapeMeasure.setEnabled(false);
		final ActionListener miscAction = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JCheckBoxMenuItem selected = (JCheckBoxMenuItem) e.getSource();
				miscButton.setIcon(selected.getIcon());
				if (selected == miWindow) {
					miscCommand = SceneManager.Operation.DRAW_WINDOW;
					miscButton.setToolTipText("Draw a window");
				} else if (selected == miDoor) {
					miscCommand = SceneManager.Operation.DRAW_DOOR;
					miscButton.setToolTipText("Draw a door");
				} else if (selected == miFloor) {
					miscCommand = SceneManager.Operation.ADD_FLOOR;
					miscButton.setToolTipText("Add a floor");
				} else if (selected == miPlant) {
					miscCommand = SceneManager.Operation.ADD_PLANT;
					miscButton.setToolTipText("Add a plant");
				} else if (selected == miHuman) {
					miscCommand = SceneManager.Operation.ADD_HUMAN;
					miscButton.setToolTipText("Add a human");
				} else if (selected == miSensor) {
					miscCommand = SceneManager.Operation.ADD_SENSOR;
					miscButton.setToolTipText("Add a sensor module");
				} else if (selected == miTapeMeasure) {
					miscCommand = SceneManager.Operation.ADD_TAPE_MEASURE;
					miscButton.setToolTipText("Add a tape measure");
				}
				SceneManager.getInstance().setOperation(miscCommand);
				miscButton.setSelected(true);
				((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
			}
		};
		miWindow.addActionListener(miscAction);
		miDoor.addActionListener(miscAction);
		miFloor.addActionListener(miscAction);
		miPlant.addActionListener(miscAction);
		miHuman.addActionListener(miscAction);
		miSensor.addActionListener(miscAction);
		miTapeMeasure.addActionListener(miscAction);
		miscMenu = new JPopupMenu();
		miscMenu.add(miWindow);
		miscMenu.add(miDoor);
		miscMenu.add(miFloor);
		miscMenu.addSeparator();
		miscMenu.add(miPlant);
		miscMenu.add(miHuman);
		miscMenu.addSeparator();
		miscMenu.add(miSensor);
		miscMenu.add(miTapeMeasure);
		bg = new ButtonGroup();
		bg.add(miWindow);
		bg.add(miDoor);
		bg.add(miFloor);
		bg.add(miPlant);
		bg.add(miHuman);
		bg.add(miSensor);
		bg.add(miTapeMeasure);

		// create solar menu
		final JCheckBoxMenuItem miRack = new JCheckBoxMenuItem("Solar Panel Rack", new ImageIcon(getClass().getResource("icons/rack.png")), true);
		final JCheckBoxMenuItem miSolarPanel = new JCheckBoxMenuItem("Single Solar Panel", new ImageIcon(getClass().getResource("icons/solar_panel.png")));
		final JCheckBoxMenuItem miHeliostat = new JCheckBoxMenuItem("Heliostat", new ImageIcon(getClass().getResource("icons/mirror.png")));
		final JCheckBoxMenuItem miParabolicTrough = new JCheckBoxMenuItem("Parabolic Trough", new ImageIcon(getClass().getResource("icons/parabolic_trough.png")));
		final JCheckBoxMenuItem miParabolicDish = new JCheckBoxMenuItem("Parabolic Dish", new ImageIcon(getClass().getResource("icons/parabolic_dish.png")));
		final JCheckBoxMenuItem miFresnelReflector = new JCheckBoxMenuItem("Linear Fresnel Reflector", new ImageIcon(getClass().getResource("icons/fresnel_reflector.png")));
		final JCheckBoxMenuItem miSolarWaterHeater = new JCheckBoxMenuItem("Solar Water Heater", new ImageIcon(getClass().getResource("icons/solar_water_heater.png")));
		miSolarWaterHeater.setEnabled(false);
		final ActionListener solarAction = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JCheckBoxMenuItem selected = (JCheckBoxMenuItem) e.getSource();
				solarButton.setIcon(selected.getIcon());
				if (selected == miSolarPanel) {
					solaCommand = SceneManager.Operation.ADD_SOLAR_PANEL;
					solarButton.setToolTipText("Add a single solar panel");
				} else if (selected == miRack) {
					solaCommand = SceneManager.Operation.ADD_RACK;
					solarButton.setToolTipText("Add a rack of solar panels");
				} else if (selected == miHeliostat) {
					solaCommand = SceneManager.Operation.ADD_HELIOSTAT;
					solarButton.setToolTipText("Add a heliostat");
				} else if (selected == miParabolicTrough) {
					solaCommand = SceneManager.Operation.ADD_PARABOLIC_TROUGH;
					solarButton.setToolTipText("Add a parabolic trough");
				} else if (selected == miParabolicDish) {
					solaCommand = SceneManager.Operation.ADD_PARABOLIC_DISH;
					solarButton.setToolTipText("Add a parabolic dish");
				} else if (selected == miFresnelReflector) {
					solaCommand = SceneManager.Operation.ADD_FRESNEL_REFLECTOR;
					solarButton.setToolTipText("Add a linear Fresnel reflector");
				} else if (selected == miSolarWaterHeater) {
					solaCommand = SceneManager.Operation.ADD_SOLAR_WATER_HEATER;
					solarButton.setToolTipText("Add a solar water heater");
				}
				SceneManager.getInstance().setOperation(solaCommand);
				solarButton.setSelected(true);
				((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
			}
		};
		miSolarPanel.addActionListener(solarAction);
		miRack.addActionListener(solarAction);
		miSolarWaterHeater.addActionListener(solarAction);
		miHeliostat.addActionListener(solarAction);
		miParabolicTrough.addActionListener(solarAction);
		miParabolicDish.addActionListener(solarAction);
		miFresnelReflector.addActionListener(solarAction);
		solaMenu = new JPopupMenu();
		solaMenu.add(miRack);
		solaMenu.add(miSolarPanel);
		solaMenu.addSeparator();
		solaMenu.add(miSolarWaterHeater);
		solaMenu.add(miHeliostat);
		solaMenu.add(miParabolicTrough);
		solaMenu.add(miParabolicDish);
		solaMenu.add(miFresnelReflector);
		bg = new ButtonGroup();
		bg.add(miSolarPanel);
		bg.add(miRack);
		bg.add(miSolarWaterHeater);
		bg.add(miHeliostat);
		bg.add(miParabolicTrough);
		bg.add(miParabolicDish);
		bg.add(miFresnelReflector);

		System.out.println("done");
	}

	private void initialize() {
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		this.setSize(1000, 300);
		setLayout(new BorderLayout());
		this.add(getAppToolbar(), BorderLayout.NORTH);
		this.add(getEnergyCanvasNoteSplitPane(), BorderLayout.CENTER);
	}

	JToolBar getAppToolbar() {
		if (appToolbar == null) {
			appToolbar = new JToolBar();
			appToolbar.setFloatable(false);
			appToolbar.add(getSelectButton());
			appToolbar.add(getZoomButton());
			appToolbar.add(getSpinViewButton());
			appToolbar.add(getPreviewButton());
			appToolbar.add(getNoteButton());
			appToolbar.addSeparator();
			appToolbar.add(getAnnotationButton());
			appToolbar.add(getResizeButton());
			appToolbar.add(getRotateButton());
			appToolbar.addSeparator();
			appToolbar.add(getBaseButton());
			appToolbar.add(getBaseArrowButton());
			appToolbar.add(getWallButton());
			appToolbar.add(getWallArrowButton());
			appToolbar.add(getRoofButton());
			appToolbar.add(getRoofArrowButton());
			appToolbar.add(getMiscButton());
			appToolbar.add(getMiscArrowButton());
			appToolbar.add(getSolarButton());
			appToolbar.add(getSolaArrowButton());
			appToolbar.addSeparator();
			appToolbar.add(getShadowButton());
			appToolbar.add(getHeliodonButton());
			appToolbar.add(getSunAnimationButton());
			appToolbar.add(getEnergyButton());
			final ButtonGroup bg = new ButtonGroup();
			bg.add(selectButton);
			bg.add(zoomButton);
			bg.add(resizeButton);
			bg.add(baseButton);
			bg.add(wallButton);
			bg.add(roofButton);
			bg.add(solarButton);
			bg.add(miscButton);
		}
		return appToolbar;
	}

	private static void addMouseOverEffect(final AbstractButton button) {
		if (Config.isMac()) { // Mac OS X does not have the same behavior as Windows 10, so we mimic it for Mac
			final Color defaultColor = button.getBackground();
			button.setOpaque(true);
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(final MouseEvent e) {
					button.setBackground(button.isSelected() ? SystemColor.controlShadow : defaultColor);
				}

				@Override
				public void mouseEntered(final MouseEvent e) {
					button.setBackground(SystemColor.controlLtHighlight);
				}
			});
			button.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					button.setBackground(button.isSelected() ? SystemColor.controlShadow : defaultColor);
				}
			});
		}
	}

	private JToggleButton getSelectButton() {
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
					defaultTool();
				}
			});
			addMouseOverEffect(selectButton);
		}
		return selectButton;
	}

	private JToggleButton getWallButton() {
		if (wallButton == null) {
			wallButton = new JToggleButton();
			wallButton.setIcon(new ImageIcon(getClass().getResource("icons/exterior_wall.png")));
			wallButton.setToolTipText("Draw an exterior wall");
			wallButton.setFocusable(false);
			wallButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_EXTERIOR_WALL);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			wallButton.addMouseListener(operationStickAndRefreshUponExit);
			addMouseOverEffect(wallButton);
		}
		return wallButton;
	}

	private JButton getWallArrowButton() {
		if (wallArrowButton == null) {
			wallArrowButton = new JButton();
			wallArrowButton.setFocusable(false);
			final Dimension d = new Dimension(12, wallButton.getMaximumSize().height);
			wallArrowButton.setMaximumSize(d);
			wallArrowButton.setIcon(new Symbol.Arrow(Color.BLACK, d.width, d.height));
			wallArrowButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					wallMenu.show(wallButton, 0, wallButton.getHeight());
				}
			});
			wallArrowButton.setBorder(BorderFactory.createEmptyBorder());
			wallArrowButton.setFocusPainted(false);
		}
		return wallArrowButton;
	}

	private JToggleButton getMiscButton() {
		if (miscButton == null) {
			miscButton = new JToggleButton();
			miscButton.setText("");
			miscButton.setToolTipText("Draw a window");
			miscButton.setIcon(new ImageIcon(getClass().getResource("icons/window.png")));
			miscButton.setFocusable(false);
			miscButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().setOperation(miscCommand);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			miscButton.addMouseListener(operationStickAndRefreshUponExit);
			addMouseOverEffect(miscButton);
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

	private JToggleButton getBaseButton() {
		if (baseButton == null) {
			baseButton = new JToggleButton();
			baseButton.setIcon(new ImageIcon(getClass().getResource("icons/foundation.png")));
			baseButton.setToolTipText("Draw a foundation");
			baseButton.setFocusable(false);
			baseButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().setOperation(baseCommand);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			baseButton.addMouseListener(operationStickAndRefreshUponExit);
			addMouseOverEffect(baseButton);
		}
		return baseButton;
	}

	private JButton getBaseArrowButton() {
		if (baseArrowButton == null) {
			baseArrowButton = new JButton();
			baseArrowButton.setFocusable(false);
			final Dimension d = new Dimension(12, baseButton.getMaximumSize().height);
			baseArrowButton.setMaximumSize(d);
			baseArrowButton.setIcon(new Symbol.Arrow(Color.BLACK, d.width, d.height));
			baseArrowButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					baseMenu.show(baseButton, 0, baseButton.getHeight());
				}
			});
			baseArrowButton.setBorder(BorderFactory.createEmptyBorder());
			baseArrowButton.setFocusPainted(false);
		}
		return baseArrowButton;
	}

	public JToggleButton getShadowButton() {
		if (shadowButton == null) {
			shadowButton = new JToggleButton();
			shadowButton.addMouseListener(refreshUponMouseExit);
			shadowButton.setIcon(new ImageIcon(getClass().getResource("icons/shadow.png")));
			shadowButton.setToolTipText("Show shadows");
			shadowButton.setFocusable(false);
			shadowButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final ShowShadowCommand c = new ShowShadowCommand();
					if (SceneManager.getInstance().isSunAnimation() || Heliodon.getInstance().isNightTime()) {
						SceneManager.getInstance().setShading(shadowButton.isSelected());
					} else {
						SceneManager.getInstance().setShading(false);
					}
					SceneManager.getInstance().setShadow(shadowButton.isSelected());
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
					disableSunAnim();
					SceneManager.getInstance().getUndoManager().addEdit(c);
					// Scene.getInstance().setEdited(false); // shadow not saved -- make sense because it doesn't work on some machines
					// org.concord.energy3d.util.Util.reportError(new RuntimeException("Error from Xie"));
				}
			});
			addMouseOverEffect(shadowButton);
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
			spinViewButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					SceneManager.getInstance().getUndoManager().addEdit(new SpinViewCommand());
					SceneManager.getInstance().toggleSpinView();
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			addMouseOverEffect(spinViewButton);
		}
		return spinViewButton;
	}

	public JToggleButton getHeliodonButton() {
		if (heliodonButton == null) {
			heliodonButton = new JToggleButton();
			heliodonButton.addMouseListener(refreshUponMouseExit);
			heliodonButton.setIcon(new ImageIcon(getClass().getResource("icons/heliodon.png")));
			heliodonButton.setToolTipText("Show heliodon");
			heliodonButton.setFocusable(false);
			heliodonButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final ShowHeliodonCommand c = new ShowHeliodonCommand();
					SceneManager.getInstance().setHeliodonVisible(heliodonButton.isSelected());
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
					disableSunAnim();
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			addMouseOverEffect(heliodonButton);
		}
		return heliodonButton;
	}

	private void disableSunAnim() {
		final boolean enableSunAnim = shadowButton.isSelected() || heliodonButton.isSelected();
		sunAnimButton.setEnabled(enableSunAnim);
		if (!enableSunAnim && sunAnimButton.isSelected()) {
			sunAnimButton.setSelected(false);
			SceneManager.getInstance().setSunAnimation(false);
		}
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
					energyButton.setSelected(false);
					final AnimateSunCommand c = new AnimateSunCommand();
					SceneManager.getInstance().setSunAnimation(sunAnimButton.isSelected());
					if (shadowButton.isSelected()) {
						SceneManager.getInstance().setShading(Heliodon.getInstance().isNightTime());
					}
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			addMouseOverEffect(sunAnimButton);
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
			previewButton.addItemListener(new ItemListener() { // must be ItemListner to be triggered when selection is changed by code
				@Override
				public void itemStateChanged(final ItemEvent e) {
					MainFrame.getInstance().getPreviewMenuItem().setSelected(previewButton.isSelected());
					MainFrame.getInstance().getEditMenu().setEnabled(!previewButton.isSelected());
					defaultTool();
					PrintController.getInstance().setPrintPreview(previewButton.isSelected());
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			addMouseOverEffect(previewButton);
		}
		return previewButton;
	}

	public void defaultTool() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				getSelectButton().setSelected(true);
				((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
			}
		});
		SceneManager.getInstance().setOperation(Operation.SELECT);
		SceneManager.getInstance().refresh();
	}

	public JToggleButton getAnnotationButton() {
		if (annotationButton == null) {
			annotationButton = new JToggleButton();
			// annotationButton.setSelected(true);
			annotationButton.addMouseListener(refreshUponMouseExit);
			annotationButton.setIcon(new ImageIcon(getClass().getResource("icons/annotation.png")));
			annotationButton.setToolTipText("Show annotations");
			annotationButton.setFocusable(false);
			annotationButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final ShowAnnotationCommand c = new ShowAnnotationCommand();
					Scene.getInstance().setAnnotationsVisible(annotationButton.isSelected());
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
					Scene.getInstance().setEdited(true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			});
			addMouseOverEffect(annotationButton);
		}
		return annotationButton;
	}

	private JToggleButton getZoomButton() {
		if (zoomButton == null) {
			zoomButton = new JToggleButton();
			zoomButton.addMouseListener(refreshUponMouseExit);
			zoomButton.setIcon(new ImageIcon(getClass().getResource("icons/zoom.png")));
			zoomButton.setToolTipText("Zoom");
			zoomButton.setFocusable(false);
			zoomButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (SceneManager.getInstance().isZoomLock()) {
						defaultTool();
					} else {
						SceneManager.getInstance().setOperation(SceneManager.Operation.SELECT);
						SceneManager.getInstance().setZoomLock(true);
						((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
					}
				}
			});
			addMouseOverEffect(zoomButton);
		}
		return zoomButton;
	}

	public void setToolbarEnabledForPreview(final boolean enabled) {
		EventQueue.invokeLater(new Runnable() { // must be run in the event queue as this method may be called in a custom thread
			@Override
			public void run() {
				for (final Component c : getAppToolbar().getComponents()) {
					if (c != getPreviewButton() && c != getSelectButton() && c != getAnnotationButton() && c != getZoomButton() && c != getSpinViewButton()) {
						if (!enabled || c != getSunAnimationButton() || getShadowButton().isSelected() || getHeliodonButton().isSelected()) {
							c.setEnabled(enabled);
						}
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
					if (c != getNoteButton() && c != getShadowButton() && c != getEnergyButton() && c != getHeliodonButton() && c != getSelectButton() && c != getAnnotationButton() && c != getZoomButton() && c != getSpinViewButton()) {
						if (!enabled || c != getSunAnimationButton() || getShadowButton().isSelected() || getHeliodonButton().isSelected()) {
							c.setEnabled(enabled);
						}
					}
				}
			}
		});
	}

	JSplitPane getEnergyCanvasNoteSplitPane() {
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

	public JToggleButton getEnergyButton() {
		if (energyButton == null) {
			energyButton = new JToggleButton("");
			energyButton.setToolTipText("Calculate energy of the day");
			energyButton.setIcon(new ImageIcon(getClass().getResource("icons/calculate.png")));
			energyButton.addMouseListener(refreshUponMouseExit);
			energyButton.setFocusable(false);
			energyButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					final EnergyPanel p = EnergyPanel.getInstance();
					p.showHeatMapContrastSlider(energyButton.isSelected());
					if (energyButton.isSelected()) {
						defaultTool();
						SceneManager.getInstance().autoSelectBuilding(false);
						if (EnergyPanel.getInstance().adjustCellSize()) {
							Util.selectSilently(energyButton, false);
						} else {
							SceneManager.getInstance().computeEnergyView(true);
						}
					} else {
						p.getBuildingDailyEnergyGraph().clearData();
						p.getBuildingDailyEnergyGraph().removeGraph();
						p.getPvProjectDailyEnergyGraph().clearData();
						p.getPvProjectDailyEnergyGraph().removeGraph();
						p.getCspProjectDailyEnergyGraph().clearData();
						p.getCspProjectDailyEnergyGraph().removeGraph();
						SceneManager.getInstance().computeEnergyView(false);
					}
				}
			});
			addMouseOverEffect(energyButton);
		}
		return energyButton;
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

				private void updateEditFlag() {
					Scene.getInstance().setEdited(true);
					MainFrame.getInstance().updateTitleBar();
				}

				@Override
				public void removeUpdate(final DocumentEvent e) {
					updateEditFlag();
					SnapshotLogger.getInstance().setNoteEdited(true);
					if (noteTextArea.getDocument() instanceof MyPlainDocument) {
						String s = ((MyPlainDocument) noteTextArea.getDocument()).getRemovedString();
						if (s != null) {
							s = s.replace("\n", "-linebreak-");
							s = s.replace("\t", "-tab-");
							s = s.replace("\\", "\\\\");
							s = s.replace("\"", "\\\"");
							noteString += "D(" + e.getOffset() + "," + s + ")";
							TimeSeriesLogger.getInstance().logNote();
						}
					}
				}

				@Override
				public void insertUpdate(final DocumentEvent e) {
					updateEditFlag();
					SnapshotLogger.getInstance().setNoteEdited(true);
					String s = null;
					try {
						s = noteTextArea.getDocument().getText(e.getOffset(), e.getLength());
					} catch (final BadLocationException e1) {
						e1.printStackTrace();
					}
					if (s != null) {
						s = s.replace("\n", "-linebreak-");
						s = s.replace("\t", "-tab-");
						s = s.replace("\\", "\\\\");
						s = s.replace("\"", "\\\"");
						noteString += "I(" + e.getOffset() + "," + s + ")";
						TimeSeriesLogger.getInstance().logNote();
					}
				}

				@Override
				public void changedUpdate(final DocumentEvent e) {
					SnapshotLogger.getInstance().setNoteEdited(true);
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

	void setSplitComponentVisible(final boolean visible, final JSplitPane splitPane, final Component component) {
		getCanvasNoteSplitPane().getSize();
		getCanvasPanel().getPreferredSize();
		component.setVisible(visible);
		splitPane.setDividerSize(visible ? defaultDividerSize : 0);
		splitPane.resetToPreferredSizes();
	}

	private JToggleButton getSolarButton() {
		if (solarButton == null) {
			solarButton = new JToggleButton("");
			solarButton.setToolTipText("Add a solar panel rack");
			solarButton.setIcon(new ImageIcon(getClass().getResource("icons/rack.png")));
			solarButton.setFocusable(false);
			solarButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().setOperation(solaCommand);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			solarButton.addMouseListener(operationStickAndRefreshUponExit);
			addMouseOverEffect(solarButton);
		}
		return solarButton;
	}

	private JButton getSolaArrowButton() {
		if (solaArrowButton == null) {
			solaArrowButton = new JButton();
			solaArrowButton.setFocusable(false);
			final Dimension d = new Dimension(12, solarButton.getMaximumSize().height);
			solaArrowButton.setMaximumSize(d);
			solaArrowButton.setIcon(new Symbol.Arrow(Color.BLACK, d.width, d.height));
			solaArrowButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					solaMenu.show(solarButton, 0, solarButton.getHeight());
				}
			});
			solaArrowButton.setBorder(BorderFactory.createEmptyBorder());
			solaArrowButton.setFocusPainted(false);
		}
		return solaArrowButton;
	}

	private JToggleButton getRoofButton() {
		if (roofButton == null) {
			roofButton = new JToggleButton();
			roofButton.setIcon(new ImageIcon(getClass().getResource("icons/pyramid_roof.png")));
			roofButton.setToolTipText("Add a pyramid roof");
			roofButton.setFocusable(false);
			roofButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().setOperation(roofCommand);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			roofButton.addMouseListener(operationStickAndRefreshUponExit);
			addMouseOverEffect(roofButton);
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
					if (SceneManager.getInstance().getOperation() == Operation.RESIZE) {
						defaultTool();
					} else {
						SceneManager.getInstance().setOperation(SceneManager.Operation.RESIZE);
						((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
					}
				}
			});
			addMouseOverEffect(resizeButton);
		}
		return resizeButton;
	}

	JButton getRotateButton() {
		if (rotateButton == null) {
			rotateButton = new JButton();
			if (Config.isMac()) { // for some reason, the newer version of Mac draws border for JButton (but not JToggleButton)
				rotateButton.setBorderPainted(false);
			}
			rotateButton.addMouseListener(refreshUponMouseExit);
			rotateButton.setIcon(new ImageIcon(getClass().getResource("icons/rotate_cw.png")));
			rotateButton.setToolTipText("<html>Rotate in the clockwise direction (change azimuth).<br>Hold down the Ctrl key and press this button for counter-clockwise rotation.<br>Hold down the Shift key while pressing this button to rotate more slowly.<br>If a component is selected, rotate around its center. Otherwise rotate everything around the origin.</html>");
			rotateButton.setFocusable(false);
			addMouseOverEffect(rotateButton);
			rotateButton.addMouseListener(new MouseAdapter() {
				private volatile boolean mousePressed = false;

				@Override
				public void mousePressed(final MouseEvent e) {
					energyButton.setSelected(false);
					mousePressed = true;
					final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart == null || selectedPart instanceof Tree || selectedPart instanceof Human) {
						int count = 0;
						HousePart hp = null;
						for (final HousePart x : Scene.getInstance().getParts()) {
							if (x instanceof Foundation) {
								count++;
								hp = x;
							}
						}
						if (count == 1) { // if there is only one building, automatically select it to ensure that we always rotate around its center
							SceneManager.getInstance().setSelectedPart(hp);
							SceneManager.getInstance().refresh();
							EnergyPanel.getInstance().updateProperties();
						}
					}
					new Thread("Energy3D Continuous Rotation") {
						private int count;
						private ChangeAzimuthCommand c;

						@Override
						public void run() {
							final HousePart part = SceneManager.getInstance().getSelectedPart();
							if (part != null) {
								c = new ChangeAzimuthCommand(part);
							}
							while (mousePressed) {
								SceneManager.getTaskManager().update(new Callable<Object>() {
									@Override
									public Object call() throws Exception {
										if (part == null) {
											SceneManager.getInstance().rotateAllFoundations(rotationAngle);
										} else {
											if (part instanceof Foundation) {
												SceneManager.getInstance().rotateFoundation(rotationAngle, true);
											} else if (part instanceof SolarPanel) {
												final SolarPanel solarPanel = (SolarPanel) part;
												solarPanel.setRelativeAzimuth(solarPanel.getRelativeAzimuth() + Math.toDegrees(rotationAngle));
												solarPanel.draw();
											} else if (part instanceof Rack) {
												final Rack rack = (Rack) part;
												rack.setRelativeAzimuth(rack.getRelativeAzimuth() + Math.toDegrees(rotationAngle));
												rack.draw();
											} else if (part instanceof Mirror) {
												final Mirror mirror = (Mirror) part;
												mirror.setRelativeAzimuth(mirror.getRelativeAzimuth() + Math.toDegrees(rotationAngle));
												mirror.draw();
											}
										}
										count++;
										Scene.getInstance().setEdited(true);
										EventQueue.invokeLater(new Runnable() {
											@Override
											public void run() {
												EnergyPanel.getInstance().updateProperties();
											}
										});
										return null;
									}
								});
								final int partCount = Scene.getInstance().getParts().size();
								try {
									Thread.sleep(200 + partCount * 5); // give it enough time for the above call to complete (the more parts it has, the more time it needs)
								} catch (final InterruptedException e) {
								}
							}
							// undo only after the thread ends
							if (part == null) {
								SceneManager.getInstance().getUndoManager().addEdit(new RotateBuildingCommand(null, rotationAngle * count));
							} else {
								if (part instanceof Foundation) {
									SceneManager.getInstance().getUndoManager().addEdit(new RotateBuildingCommand((Foundation) part, rotationAngle * count));
								} else if (part instanceof SolarPanel || part instanceof Rack || part instanceof Mirror) {
									if (c != null) {
										SceneManager.getInstance().getUndoManager().addEdit(c);
									}
								}
							}
						}
					}.start();
				}

				@Override
				public void mouseReleased(final MouseEvent e) {
					mousePressed = false;
				}
			});
		}
		return rotateButton;
	}

	JToggleButton getNoteButton() {
		if (noteButton == null) {
			noteButton = new JToggleButton();
			noteButton.setToolTipText("Show note");
			noteButton.setIcon(new ImageIcon(MainPanel.class.getResource("icons/note.png")));
			noteButton.setFocusable(false);
			noteButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					MainPanel.getInstance().setNoteVisible(noteButton.isSelected());
					if (noteButton.isSelected()) {
						getNoteTextArea().requestFocusInWindow();
					}
				}
			});
			addMouseOverEffect(noteButton);
		}
		return noteButton;
	}

	public void setRotationAngle(final double x) {
		rotationAngle = x;
	}

	public double getRotationAngleAbsolute() {
		return rotationAngleAbsolute;
	}

	/** the string that gets inserted or removed in the note area */
	public String getNoteString() {
		return noteString;
	}

	public void setNoteString(final String s) {
		noteString = s;
	}

}