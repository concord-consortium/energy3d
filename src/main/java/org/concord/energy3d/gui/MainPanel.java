package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.PrintController;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.Operation;
import org.concord.energy3d.util.Config;

public class MainPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final MainPanel instance = new MainPanel();
	private MainFrame mainFrame;
	private JToolBar appToolbar = null;
	private JToggleButton selectButton = null;
	private JToggleButton wallButton = null;
	private JToggleButton roofButton = null;
	private JToggleButton windowButton = null;
	private JToggleButton platformButton = null;
	private JToggleButton lightButton = null;
	private JToggleButton spinAnimationButton = null;
	private JToggleButton resizeButton = null;
	private JToggleButton heliodonButton = null;
	private JToggleButton sunAnimButton = null;
	private JToggleButton annotationToggleButton;
	private JToggleButton previewButton = null;
	private JToggleButton zoomButton = null;
	private JToggleButton noteButton;
	private JSplitPane energyCanvasNoteSplitPane;
	private EnergyPanel energyPanel;
	private JPanel canvasPanel;
	private JToggleButton energyToggleButton;
	private JToggleButton solarButton;
	private JSplitPane canvasNoteSplitPane;
	private JScrollPane noteScrollPane;
	private JTextArea noteTextArea;
	private JToggleButton solarPanelButton;
	private JToggleButton treeButton;
	private JToggleButton miscButton;
	private JButton rotateButton;
	private JButton treeArrowButton;
	private JButton roofArrowButton;
	private JButton miscArrowButton;
	private int defaultDividerSize = -1;
	private final JPopupMenu treeMenu;
	private final JPopupMenu roofMenu;
	private final JPopupMenu miscMenu;
	private Operation treeCommand = SceneManager.Operation.DRAW_TREE;
	private Operation roofCommand = SceneManager.Operation.DRAW_ROOF_PYRAMID;
	private Operation miscCommand = SceneManager.Operation.DRAW_DOOR;
	private double buildingRotationAngleAbsolute = Math.PI / 18;
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

	final static Map<String, Integer> cityLatitute = new HashMap<String, Integer>();
	static {
		cityLatitute.put("Moscow", 55);
		cityLatitute.put("Ottawa", 45);
		cityLatitute.put("Boston", 42);
		cityLatitute.put("Beijing", 39);
		cityLatitute.put("Washington DC", 38);
		cityLatitute.put("Tehran", 35);
		cityLatitute.put("Los Angeles", 34);
		cityLatitute.put("Miami", 25);
		cityLatitute.put("Mexico City", 19);
		cityLatitute.put("Singapore", 1);
		cityLatitute.put("Sydney", -33);
		cityLatitute.put("Buenos Aires", -34);
	}

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
		final JCheckBoxMenuItem pyramidRoofMenu = new JCheckBoxMenuItem("Pyramid Roof", new ImageIcon(getClass().getResource("icons/roof_pyramid.png")), true);
		final JCheckBoxMenuItem hipRoofMenu = new JCheckBoxMenuItem("Hip Roof", new ImageIcon(getClass().getResource("icons/roof_hip.png")));
		final JCheckBoxMenuItem customRoofMenu = new JCheckBoxMenuItem("Custom Roof", new ImageIcon(getClass().getResource("icons/roof_custom.png")));
		final JCheckBoxMenuItem gableRoofMenu = new JCheckBoxMenuItem("Gable Roof", new ImageIcon(getClass().getResource("icons/roof_gable.png")));
		final ActionListener roofAction = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JCheckBoxMenuItem selected = (JCheckBoxMenuItem) e.getSource();
				roofButton.setIcon(selected.getIcon());
				if (selected == pyramidRoofMenu) {
					roofCommand = SceneManager.Operation.DRAW_ROOF_PYRAMID;
					roofButton.setToolTipText("Draw pyramid roof");
				} else if (selected == hipRoofMenu) {
					roofCommand = SceneManager.Operation.DRAW_ROOF_HIP;
					roofButton.setToolTipText("Draw hip roof");
				} else if (selected == customRoofMenu) {
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
		pyramidRoofMenu.addActionListener(roofAction);
		hipRoofMenu.addActionListener(roofAction);
		customRoofMenu.addActionListener(roofAction);
		gableRoofMenu.addActionListener(roofAction);
		roofMenu = new JPopupMenu();
		roofMenu.add(pyramidRoofMenu);
		roofMenu.add(hipRoofMenu);
		roofMenu.add(customRoofMenu);
		roofMenu.add(gableRoofMenu);
		ButtonGroup bg = new ButtonGroup();
		bg.add(pyramidRoofMenu);
		bg.add(hipRoofMenu);
		bg.add(customRoofMenu);
		bg.add(gableRoofMenu);

		// create tree menu
		final JCheckBoxMenuItem shortTreeMenu = new JCheckBoxMenuItem("Short Tree", new ImageIcon(getClass().getResource("icons/tree.png")), true);
		final JCheckBoxMenuItem tallTreeMenu = new JCheckBoxMenuItem("Tall Tree", new ImageIcon(getClass().getResource("icons/tree_tall.png")));
		final ActionListener treeAction = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JCheckBoxMenuItem selected = (JCheckBoxMenuItem) e.getSource();
				treeButton.setIcon(selected.getIcon());
				if (selected == shortTreeMenu) {
					treeCommand = SceneManager.Operation.DRAW_TREE;
					treeButton.setToolTipText("Insert tree");
				} else {
					treeCommand = SceneManager.Operation.DRAW_TREE_TALL;
					treeButton.setToolTipText("Insert tall tree");
				}
				SceneManager.getInstance().setOperation(treeCommand);
				treeButton.setSelected(true);
				((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
			}
		};
		shortTreeMenu.addActionListener(treeAction);
		tallTreeMenu.addActionListener(treeAction);
		treeMenu = new JPopupMenu();
		treeMenu.add(shortTreeMenu);
		treeMenu.add(tallTreeMenu);
		bg = new ButtonGroup();
		bg.add(shortTreeMenu);
		bg.add(tallTreeMenu);

		// create misc menu
		final JCheckBoxMenuItem doorMenu = new JCheckBoxMenuItem("Door", new ImageIcon(getClass().getResource("icons/door.png")), true);
		final JCheckBoxMenuItem floorMenu = new JCheckBoxMenuItem("Floor", new ImageIcon(getClass().getResource("icons/floor.png")));
		final ActionListener miscAction = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JCheckBoxMenuItem selected = (JCheckBoxMenuItem) e.getSource();
				miscButton.setIcon(selected.getIcon());
				if (selected == doorMenu) {
					miscCommand = SceneManager.Operation.DRAW_DOOR;
					miscButton.setToolTipText("Draw door");
				} else {
					miscCommand = SceneManager.Operation.DRAW_FLOOR;
					miscButton.setToolTipText("Draw floor");
				}
				SceneManager.getInstance().setOperation(miscCommand);
				miscButton.setSelected(true);
				((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
			}
		};
		doorMenu.addActionListener(miscAction);
		floorMenu.addActionListener(miscAction);
		miscMenu = new JPopupMenu();
		miscMenu.add(doorMenu);
		miscMenu.add(floorMenu);
		bg = new ButtonGroup();
		bg.add(doorMenu);
		bg.add(floorMenu);

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
			appToolbar.add(getResizeButton());
			appToolbar.add(getRotateButton());
			appToolbar.add(getSpinAnimationButton());
			appToolbar.addSeparator();
			appToolbar.add(getAnnotationToggleButton());
			appToolbar.add(getPreviewButton());
			appToolbar.add(getNoteButton());
			appToolbar.addSeparator();
			appToolbar.add(getPlatformButton());
			appToolbar.add(getWallButton());
			appToolbar.add(getWindowButton());
			appToolbar.add(getSolarPanelButton());
			appToolbar.add(getRoofButton());
			appToolbar.add(getRoofArrowButton());
			appToolbar.add(getMiscButton());
			appToolbar.add(getMiscArrowButton());
			appToolbar.add(getTreeButton());
			appToolbar.add(getTreeArrowButton());
			appToolbar.addSeparator();
			appToolbar.add(getLightButton());
			appToolbar.add(getHeliodonButton());
			appToolbar.add(getSunAnimButton());
			appToolbar.add(getSolarButton());
			appToolbar.add(getEnergyToggleButton());
			final ButtonGroup bg = new ButtonGroup();
			bg.add(selectButton);
			bg.add(zoomButton);
			bg.add(resizeButton);
			bg.add(platformButton);
			bg.add(wallButton);
			bg.add(windowButton);
			bg.add(roofButton);
			bg.add(solarPanelButton);
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
			final Dimension d = new Dimension(12, miscButton.getMaximumSize().height);
			miscArrowButton.setMaximumSize(d);
			miscArrowButton.setIcon(new ArrowIcon(d.width, d.height, Color.BLACK));
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

	public JToggleButton getLightButton() {
		if (lightButton == null) {
			lightButton = new JToggleButton();
			lightButton.addMouseListener(refreshUponMouseExit);
			lightButton.setIcon(new ImageIcon(getClass().getResource("icons/shadow.png")));
			lightButton.setToolTipText("Show shadows");
			lightButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					if (mainFrame != null) {
						mainFrame.getShadeMenuItem().setSelected(lightButton.isSelected());
						mainFrame.getShadowMenuItem().setSelected(lightButton.isSelected());
					} else {
						SceneManager.getInstance().setShading(lightButton.isSelected());
						SceneManager.getInstance().setShadow(lightButton.isSelected());
					}
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
					disableSunAnim();
					// Scene.getInstance().setEdited(true, false); // shadow not saved -- make sense because it doesn't work on some machines
				}
			});
		}
		return lightButton;
	}

	private JToggleButton getSpinAnimationButton() {
		if (spinAnimationButton == null) {
			spinAnimationButton = new JToggleButton();
			spinAnimationButton.addMouseListener(refreshUponMouseExit);
			spinAnimationButton.setIcon(new ImageIcon(getClass().getResource("icons/spin.png")));
			spinAnimationButton.setToolTipText("Spin view");
			spinAnimationButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					SceneManager.getInstance().toggleRotation();
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
		}
		return spinAnimationButton;
	}

	public JToggleButton getHeliodonButton() {
		if (heliodonButton == null) {
			heliodonButton = new JToggleButton();
			heliodonButton.addMouseListener(refreshUponMouseExit);
			heliodonButton.setIcon(new ImageIcon(getClass().getResource("icons/sun_heliodon.png")));
			heliodonButton.setToolTipText("Show heliodon");
			heliodonButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					SceneManager.getInstance().setHeliodonControl(heliodonButton.isSelected());
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
					disableSunAnim();
					Scene.getInstance().setEdited(true, false);
				}
			});
		}
		return heliodonButton;
	}

	public JToggleButton getSunAnimButton() {
		if (sunAnimButton == null) {
			sunAnimButton = new JToggleButton();
			sunAnimButton.addMouseListener(refreshUponMouseExit);
			sunAnimButton.setIcon(new ImageIcon(getClass().getResource("icons/sun_anim.png")));
			sunAnimButton.setEnabled(false);
			sunAnimButton.setToolTipText("Animate sun path");
			sunAnimButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					SceneManager.getInstance().setSunAnim(sunAnimButton.isSelected());
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
			// must be ItemListner to be triggered when selection is changed by code
			previewButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (mainFrame != null) {
						mainFrame.getPreviewMenuItem().setSelected(previewButton.isSelected());
						mainFrame.getEditMenu().setEnabled(!previewButton.isSelected());
						mainFrame.getCameraMenu().setEnabled(!previewButton.isSelected());
					}
					deselect();
					PrintController.getInstance().setPrintPreview(previewButton.isSelected());
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
		}
		return previewButton;
	}

	public void deselect() {
		getSelectButton().setSelected(true);
		SceneManager.getInstance().setOperation(Operation.SELECT);
	}

	public JToggleButton getAnnotationToggleButton() {
		if (annotationToggleButton == null) {
			annotationToggleButton = new JToggleButton();
			annotationToggleButton.setSelected(Config.isApplet() ? false : true);
			annotationToggleButton.addMouseListener(refreshUponMouseExit);
			annotationToggleButton.setIcon(new ImageIcon(getClass().getResource("icons/annotation.png")));
			annotationToggleButton.setToolTipText("Show annotations");
			annotationToggleButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
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

	public void setToolbarEnabled(final boolean enabled) {
		for (final Component c : getAppToolbar().getComponents()) {
			if (c != getPreviewButton() && c != getSelectButton() && c != getAnnotationToggleButton() && c != getZoomButton() && c != getSpinAnimationButton()) {
				if (!enabled || c != getSunAnimButton() || getLightButton().isSelected() || getHeliodonButton().isSelected())
					c.setEnabled(enabled);
			}
		}
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

	private JToggleButton getEnergyToggleButton() {
		if (energyToggleButton == null) {
			energyToggleButton = new JToggleButton("");
			energyToggleButton.setToolTipText("Show energy analysis");
			energyToggleButton.setSelected(true);
			energyToggleButton.setIcon(new ImageIcon(getClass().getResource("icons/chart.png")));
			energyToggleButton.addMouseListener(refreshUponMouseExit);
			energyToggleButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					setSplitComponentVisible(energyToggleButton.isSelected(), getEnergyCanvasNoteSplitPane(), EnergyPanel.getInstance());
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
		}
		return energyToggleButton;
	}

	public JToggleButton getSolarButton() {
		if (solarButton == null) {
			solarButton = new JToggleButton("");
			solarButton.setToolTipText("Calculate energy of the day");
			solarButton.setIcon(new ImageIcon(getClass().getResource("icons/heatmap.png")));
			solarButton.addMouseListener(refreshUponMouseExit);
			solarButton.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					SceneManager.getInstance().setSolarColorMap(solarButton.isSelected());
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
		}
		return solarButton;
	}

	private JSplitPane getCanvasNoteSplitPane() {
		if (canvasNoteSplitPane == null) {
			canvasNoteSplitPane = new JSplitPane();
			canvasNoteSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			if (Config.isMac()) {
				canvasNoteSplitPane.setTopComponent(getNoteScrollPane());
				canvasNoteSplitPane.setBottomComponent(getCanvasPanel());
				canvasNoteSplitPane.setResizeWeight(0.4);
			} else {
				canvasNoteSplitPane.setTopComponent(getCanvasPanel());
				canvasNoteSplitPane.setBottomComponent(getNoteScrollPane());
				canvasNoteSplitPane.setResizeWeight(0.6);
			}
			canvasNoteSplitPane.setDividerSize(0);
			getNoteScrollPane().setVisible(false);
		}
		return canvasNoteSplitPane;
	}

	private JScrollPane getNoteScrollPane() {
		if (noteScrollPane == null) {
			noteScrollPane = new JScrollPane();
			// noteScrollPane.setPreferredSize(new Dimension(100, 100));
			noteScrollPane.setMinimumSize(getNoteTextArea().getMinimumSize());
			noteScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			noteScrollPane.setViewportView(getNoteTextArea());
		}
		return noteScrollPane;
	}

	public JTextArea getNoteTextArea() {
		if (noteTextArea == null) {
			noteTextArea = new JTextArea(new MyPlainDocument()); // want to keep a copy of what was removed
			noteTextArea.setWrapStyleWord(true);
			noteTextArea.setLineWrap(true);
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
		final boolean enableSunAnim = lightButton.isSelected() || heliodonButton.isSelected();
		sunAnimButton.setEnabled(enableSunAnim);
		if (!enableSunAnim && sunAnimButton.isSelected()) {
			sunAnimButton.setSelected(false);
			SceneManager.getInstance().setSunAnim(false);
		}
	}

	private JToggleButton getSolarPanelButton() {
		if (solarPanelButton == null) {
			solarPanelButton = new JToggleButton("");
			solarPanelButton.setToolTipText("Add solar panel");
			solarPanelButton.setIcon(new ImageIcon(getClass().getResource("icons/solarpanel.png")));
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

	private JToggleButton getTreeButton() {
		if (treeButton == null) {
			treeButton = new JToggleButton();
			treeButton.setToolTipText("Insert tree");
			treeButton.setIcon(new ImageIcon(getClass().getResource("icons/tree.png")));
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
			final Dimension d = new Dimension(12, treeButton.getMaximumSize().height);
			treeArrowButton.setMaximumSize(d);
			treeArrowButton.setIcon(new ArrowIcon(d.width, d.height, Color.BLACK));
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
			final Dimension d = new Dimension(12, roofButton.getMaximumSize().height);
			roofArrowButton.setMaximumSize(d);
			roofArrowButton.setIcon(new ArrowIcon(d.width, d.height, Color.BLACK));
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

	private JButton getRotateButton() {
		if (rotateButton == null) {
			rotateButton = new JButton();
			rotateButton.addMouseListener(refreshUponMouseExit);
			rotateButton.setIcon(new ImageIcon(getClass().getResource("icons/rotate_cw.png")));
			rotateButton.setToolTipText("<html>Rotate a building clockwisely; <br>Hold down the SHIFT key and press this button to rotate counter-clockwisely.</html>");
			rotateButton.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					rotateButton.setIcon(new ImageIcon(getClass().getResource("icons/" + (e.isShiftDown() ? "rotate_ccw.png" : "rotate_cw.png"))));
					buildingRotationAngle = e.isShiftDown() ? buildingRotationAngleAbsolute : -buildingRotationAngleAbsolute;
				}

				@Override
				public void keyReleased(KeyEvent e) {
					buildingRotationAngle = -buildingRotationAngleAbsolute;
					rotateButton.setIcon(new ImageIcon(getClass().getResource("icons/rotate_cw.png")));
				}
			});
			rotateButton.addMouseListener(new MouseAdapter() {
				private boolean mousePressed = false;

				public void mousePressed(final MouseEvent e) {
					mousePressed = true;
					new Thread() {
						public void run() {
							while (mousePressed) {
								SceneManager.getTaskManager().update(new Callable<Object>() {
									@Override
									public Object call() throws Exception {
										SceneManager.getInstance().rotateBuilding(buildingRotationAngle);
										return null;
									}
								});
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
								}
							}
						}

					}.start();
				}

				public void mouseReleased(MouseEvent e) {
					mousePressed = false;
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
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

}