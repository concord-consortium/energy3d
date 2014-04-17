package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.concord.energy3d.model.Foundation;
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
	private JToggleButton doorButton = null;
	private JToggleButton roofButton = null;
	private JToggleButton windowButton = null;
	private JToggleButton platformButton = null;
	private JToggleButton lightButton = null;
	private JToggleButton rotAnimButton = null;
	private JToggleButton floorButton = null;
	private JToggleButton roofHipButton = null;
	private JToggleButton resizeButton = null;
	private JToggleButton heliodonButton = null;
	private JToggleButton sunAnimButton = null;
	private JToggleButton annotationToggleButton;
	private JToggleButton previewButton = null;
	private JToggleButton roofCustomButton = null;
	private JToggleButton zoomButton = null;
	private JToggleButton roofGableButton = null;
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
	private JButton treeArrowButton;
	private int defaultDividerSize = -1;
	private final JPopupMenu treeMenu;
	private Operation treeCommand = SceneManager.Operation.DRAW_TREE;

	private final MouseAdapter refreshUponMouseExit = new MouseAdapter() {
		@Override
		public void mouseExited(final MouseEvent e) {
			SceneManager.getInstance().refresh();
		}
	};

	java.awt.event.MouseAdapter operationStickAndRefreshUponExit = new java.awt.event.MouseAdapter() {
		@Override
		public void mouseClicked(final java.awt.event.MouseEvent e) {
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
		final JCheckBoxMenuItem shortTreeMenu = new JCheckBoxMenuItem("Short Tree", new ImageIcon(getClass().getResource("icons/tree.png")), true);
		final JCheckBoxMenuItem tallTreeMenu = new JCheckBoxMenuItem("Tall Tree", new ImageIcon(getClass().getResource("icons/tree_tall.png")));
		final ActionListener treeAction = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JCheckBoxMenuItem selected = (JCheckBoxMenuItem) e.getSource();
				treeButton.setIcon(selected.getIcon());
				if (selected == shortTreeMenu)
					treeCommand = SceneManager.Operation.DRAW_TREE;
				else
					treeCommand = SceneManager.Operation.DRAW_TREE_TALL;
				SceneManager.getInstance().setOperation(treeCommand);
				getTreeButton().setSelected(true);
				((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
			}
		};
		shortTreeMenu.addActionListener(treeAction);
		tallTreeMenu.addActionListener(treeAction);
		treeMenu = new JPopupMenu();
		treeMenu.add(shortTreeMenu);
		treeMenu.add(tallTreeMenu);
		final ButtonGroup bg = new ButtonGroup();
		bg.add(shortTreeMenu);
		bg.add(tallTreeMenu);
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
			appToolbar.add(getResizeButton());
			appToolbar.add(getZoomButton());
			appToolbar.add(getRotAnimButton());
			appToolbar.addSeparator();
			appToolbar.add(getAnnotationToggleButton());
			appToolbar.add(getPreviewButton());
			appToolbar.addSeparator();
			appToolbar.add(getPlatformButton());
			appToolbar.add(getWallButton());
			appToolbar.add(getDoorButton());
			appToolbar.add(getWindowButton());
			appToolbar.add(getRoofButton());
			appToolbar.add(getRoofHipButton());
			appToolbar.add(getRoofCustomButton());
			appToolbar.add(getRoofGableButton());
			appToolbar.add(getFloorButton());
			appToolbar.add(getSolarPanelButton());
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
			bg.add(doorButton);
			bg.add(windowButton);
			bg.add(roofButton);
			bg.add(roofHipButton);
			bg.add(roofCustomButton);
			bg.add(floorButton);
			bg.add(roofGableButton);
			bg.add(solarPanelButton);
			bg.add(treeButton);
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
			selectButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
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
			wallButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_WALL);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			wallButton.addMouseListener(operationStickAndRefreshUponExit);
		}
		return wallButton;
	}

	private JToggleButton getDoorButton() {
		if (doorButton == null) {
			doorButton = new JToggleButton();
			doorButton.setText("");
			doorButton.setToolTipText("Draw door");
			doorButton.setIcon(new ImageIcon(getClass().getResource("icons/door.png")));
			doorButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_DOOR);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			doorButton.addMouseListener(operationStickAndRefreshUponExit);
		}
		return doorButton;
	}

	private JToggleButton getRoofButton() {
		if (roofButton == null) {
			roofButton = new JToggleButton();
			roofButton.setIcon(new ImageIcon(getClass().getResource("icons/roof_pyramid.png")));
			roofButton.setToolTipText("Draw pyramid roof");
			roofButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_ROOF_PYRAMID);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			roofButton.addMouseListener(operationStickAndRefreshUponExit);
		}
		return roofButton;
	}

	private JToggleButton getWindowButton() {
		if (windowButton == null) {
			windowButton = new JToggleButton();
			windowButton.setIcon(new ImageIcon(getClass().getResource("icons/window.png")));
			windowButton.setToolTipText("Draw window");
			windowButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
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
			platformButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_FOUNDATION);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			platformButton.addMouseListener(operationStickAndRefreshUponExit);
		}
		return platformButton;
	}

	private JToggleButton getLightButton() {
		if (lightButton == null) {
			lightButton = new JToggleButton();
			lightButton.addMouseListener(refreshUponMouseExit);
			lightButton.setIcon(new ImageIcon(getClass().getResource("icons/shadow.png")));
			lightButton.setToolTipText("Show shadows");
			lightButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
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

	private JToggleButton getRotAnimButton() {
		if (rotAnimButton == null) {
			rotAnimButton = new JToggleButton();
			rotAnimButton.addMouseListener(refreshUponMouseExit);
			rotAnimButton.setIcon(new ImageIcon(getClass().getResource("icons/rotate.png")));
			rotAnimButton.setToolTipText("Spin view");
			rotAnimButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
					SceneManager.getInstance().toggleRotation();
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
		}
		return rotAnimButton;
	}

	private JToggleButton getFloorButton() {
		if (floorButton == null) {
			floorButton = new JToggleButton();
			floorButton.setIcon(new ImageIcon(getClass().getResource("icons/floor.png")));
			floorButton.setToolTipText("Draw floor");
			floorButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_FLOOR);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			floorButton.addMouseListener(operationStickAndRefreshUponExit);
		}
		return floorButton;
	}

	private JToggleButton getRoofHipButton() {
		if (roofHipButton == null) {
			roofHipButton = new JToggleButton();
			roofHipButton.setIcon(new ImageIcon(getClass().getResource("icons/roof_hip.png")));
			roofHipButton.setToolTipText("Draw hip roof");
			roofHipButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_ROOF_HIP);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			roofHipButton.addMouseListener(operationStickAndRefreshUponExit);
		}
		return roofHipButton;
	}

	private JToggleButton getResizeButton() {
		if (resizeButton == null) {
			resizeButton = new JToggleButton();
			resizeButton.addMouseListener(refreshUponMouseExit);
			resizeButton.setIcon(new ImageIcon(getClass().getResource("icons/resize.png")));
			resizeButton.setToolTipText("Resize or move a building");
			resizeButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
//					SceneManager.getInstance().setOperation(Operation.RESIZE);
//					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
					((Foundation) SceneManager.getInstance().getSelectedPart()).rotate();
				}
			});
		}
		return resizeButton;
	}

	public JToggleButton getHeliodonButton() {
		if (heliodonButton == null) {
			heliodonButton = new JToggleButton();
			heliodonButton.addMouseListener(refreshUponMouseExit);
			heliodonButton.setIcon(new ImageIcon(getClass().getResource("icons/sun_heliodon.png")));
			heliodonButton.setToolTipText("Show heliodon");
			heliodonButton.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(final java.awt.event.ItemEvent e) {
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
			sunAnimButton.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(final java.awt.event.ItemEvent e) {
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
			previewButton.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(final java.awt.event.ItemEvent e) {
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

	private JToggleButton getRoofCustomButton() {
		if (roofCustomButton == null) {
			roofCustomButton = new JToggleButton();
			roofCustomButton.setIcon(new ImageIcon(getClass().getResource("icons/roof_custom.png")));
			roofCustomButton.setToolTipText("Draw custom roof");
			roofCustomButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_ROOF_CUSTOM);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			roofCustomButton.addMouseListener(operationStickAndRefreshUponExit);
		}
		return roofCustomButton;
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
			zoomButton.addItemListener(new java.awt.event.ItemListener() {
				@Override
				public void itemStateChanged(final java.awt.event.ItemEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.SELECT);
					SceneManager.getInstance().setZoomLock(zoomButton.isSelected());
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
		}
		return zoomButton;
	}

	private JToggleButton getRoofGableButton() {
		if (roofGableButton == null) {
			roofGableButton = new JToggleButton();
			roofGableButton.setIcon(new ImageIcon(getClass().getResource("icons/roof_gable.png")));
			roofGableButton.setToolTipText("Convert to gable roof");
			roofGableButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(Operation.DRAW_ROOF_GABLE);
					((Component) SceneManager.getInstance().getCanvas()).requestFocusInWindow();
				}
			});
			roofGableButton.addMouseListener(operationStickAndRefreshUponExit);
		}
		return roofGableButton;
	}

	public void setToolbarEnabled(final boolean enabled) {
		for (final Component c : getAppToolbar().getComponents()) {
			if (c != getPreviewButton() && c != getSelectButton() && c != getAnnotationToggleButton() && c != getZoomButton() && c != getRotAnimButton()) {
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
				public void itemStateChanged(final ItemEvent arg0) {
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
			treeArrowButton.setIcon(new Icon() {
				@Override
				public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
					g.setColor(Color.BLACK);
					final int x2 = getIconWidth() / 2;
					final int y2 = getIconHeight() / 2;
					final int[] vx = new int[] { 2, getIconWidth() - 2, x2 };
					final int[] vy = new int[] { y2 - 2, y2 - 2, y2 + 4 };
					g.fillPolygon(vx, vy, vx.length);
				}

				@Override
				public int getIconWidth() {
					return treeArrowButton.getWidth();
				}

				@Override
				public int getIconHeight() {
					return treeArrowButton.getHeight();
				}
			});
			treeArrowButton.setMaximumSize(new Dimension(12, treeButton.getMaximumSize().height));
			treeArrowButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent arg0) {
					treeMenu.show(getTreeButton(), 0, getTreeButton().getHeight());
				}
			});
			treeArrowButton.setBorder(BorderFactory.createEmptyBorder());
			treeArrowButton.setFocusPainted(false);
		}
		return treeArrowButton;
	}

}
