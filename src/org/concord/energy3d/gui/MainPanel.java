package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.ToolTipManager;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.PrintController;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.SceneManager.Operation;
import org.concord.energy3d.scene.SceneManager.ViewMode;
import org.concord.energy3d.shapes.Heliodon;
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
	private JToggleButton foundationButton = null;
	private JToggleButton lightButton = null;
	private JToggleButton topViewButton = null;
	private JToggleButton rotAnimButton = null;
	private JToggleButton gridButton = null;
	private JToggleButton snapButton = null;
	private JToggleButton floorButton = null;
	private JToggleButton roofHipButton = null;
	private JToggleButton resizeButton = null;
	private JToggleButton heliodonButton = null;
	private JToggleButton sunAnimButton = null;
	private JToggleButton annotationToggleButton;
	protected Object lastSelection; // @jve:decl-index=0:
	private JToggleButton previewButton = null;
	private JPanel calendarPanel = null;
	private JLabel dateLabel = null;
	private JSpinner dateSpinner = null;
	private JLabel timeLabel = null;
	private JSpinner timeSpinner = null;
	private JLabel latitudeLabel = null;
	private JComboBox cityComboBox = null;
	private JSpinner latitudeSpinner = null;
	private JToggleButton roofCustomButton = null;
	private JToggleButton zoomButton = null;
	private JToggleButton roofGableButton = null;
	public static MainPanel getInstance() {
		return instance;
	}

	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public MainFrame getMainFrame() {
		return mainFrame;
	}

	/**
	 * This is the default constructor
	 */
	private MainPanel() {
		super();
		System.out.print("Initiating GUI Panel...");
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
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
		this.setSize(1000, 300);
		this.setLayout(new BorderLayout());
		this.add(getAppToolbar(), BorderLayout.NORTH);
	}

	/**
	 * This method initializes appToolbar
	 * 
	 * @return javax.swing.JToolBar
	 */
	private JToolBar getAppToolbar() {
		if (appToolbar == null) {
			appToolbar = new JToolBar();
			final boolean showEditTools = !Config.isHeliodonMode();
			if (showEditTools) {
				appToolbar.add(getSelectButton());
				appToolbar.add(getResizeButton());
				appToolbar.add(getZoomButton());
				appToolbar.addSeparator();
				appToolbar.add(getFoundationButton());
				appToolbar.add(getWallButton());
				appToolbar.add(getDoorButton());
				appToolbar.add(getWindowButton());
				appToolbar.add(getRoofButton());
				appToolbar.add(getRoofHipButton());
				appToolbar.add(getRoofCustomButton());
				appToolbar.add(getRoofGableButton());
				appToolbar.add(getFloorButton());
				appToolbar.addSeparator();
			}
			appToolbar.add(getLightButton());
			appToolbar.add(getHeliodonButton());
			appToolbar.add(getSunAnimButton());
			appToolbar.add(getCalendarPanel());
			if (showEditTools) {
				appToolbar.addSeparator();
				appToolbar.add(getRotAnimButton());
				appToolbar.add(getTopViewButton());
				appToolbar.add(getGridButton());
				appToolbar.add(getSnapButton());
				appToolbar.addSeparator();
			} else
				appToolbar.add(getRotAnimButton());

			appToolbar.add(getAnnotationToggleButton());
			if (showEditTools)
				appToolbar.add(getPreviewButton());

			if (showEditTools) {
				final ButtonGroup bg = new ButtonGroup();
				bg.add(selectButton);
				bg.add(resizeButton);
				bg.add(zoomButton);
				bg.add(foundationButton);
				bg.add(wallButton);
				bg.add(doorButton);
				bg.add(windowButton);
				bg.add(roofButton);
				bg.add(roofHipButton);
				bg.add(roofCustomButton);
				bg.add(floorButton);
				bg.add(roofGableButton);
			}
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
			selectButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
				}
			});
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
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
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
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
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
			roofButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/roof_pyramid.png")));
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
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
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
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
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
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
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
			lightButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
				}
			});
			lightButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/shadow.png")));
			lightButton.setToolTipText("Show shadows");
			lightButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (mainFrame != null) {
						mainFrame.getLightingMenu().setSelected(lightButton.isSelected());
						mainFrame.getShadowMenu().setSelected(lightButton.isSelected());
					} else {
						SceneManager.getInstance().setShading(lightButton.isSelected());
						SceneManager.getInstance().setShadow(lightButton.isSelected());
					}
					final boolean showSunTools = lightButton.isSelected() || heliodonButton.isSelected();
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
			topViewButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
				}
			});
			topViewButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/top.png")));
			topViewButton.setToolTipText("Top view");
			topViewButton.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					SceneManager.getInstance().resetCamera(topViewButton.isSelected() ? ViewMode.TOP_VIEW : ViewMode.NORMAL);
					SceneManager.getInstance().update();
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
			rotAnimButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
				}
			});
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
			gridButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
				}
			});
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
			snapButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
				}
			});
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
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
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
			roofHipButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/roof_hip.png")));
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
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
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
			resizeButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
				}
			});
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
	 * This method initializes heliodonButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	public JToggleButton getHeliodonButton() {
		if (heliodonButton == null) {
			heliodonButton = new JToggleButton();
			heliodonButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
				}
			});
			heliodonButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/sun_heliodon.png")));
			heliodonButton.setToolTipText("Show sun heliodon");
			heliodonButton.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					SceneManager.getInstance().setSunControl(heliodonButton.isSelected());
					final boolean showSunTools = lightButton.isSelected() || heliodonButton.isSelected();
					calendarPanel.setVisible(showSunTools);
					sunAnimButton.setEnabled(showSunTools);
				}
			});
		}
		return heliodonButton;
	}

	/**
	 * This method initializes sunAnimButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getSunAnimButton() {
		if (sunAnimButton == null) {
			sunAnimButton = new JToggleButton();
			sunAnimButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
				}
			});
			sunAnimButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/sun_anim.png")));
			sunAnimButton.setEnabled(false);
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
	 * This method initializes previewButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	public JToggleButton getPreviewButton() {
		if (previewButton == null) {
			previewButton = new JToggleButton();
			previewButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
				}
			});
			previewButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/print_preview.png")));
			previewButton.setToolTipText("Preview printable parts");
			// must be ItemListner to be triggered when selection is changed by code
			previewButton.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					if (mainFrame != null)
						mainFrame.getPreviewMenuItem().setSelected(previewButton.isSelected());
					deselect();
					PrintController.getInstance().setPrintPreview(previewButton.isSelected());
				}
			});
		}
		return previewButton;
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
						heliodon.setDate((Date) dateSpinner.getValue());
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
						heliodon.setTime((Date) timeSpinner.getValue());
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
					SceneManager.getInstance().getHeliodon().setObserverLatitude(((Integer) latitudeSpinner.getValue()) / 180.0 * Math.PI);
				}
			});
		}
		return latitudeSpinner;
	}

	/**
	 * This method initializes roofCustomButton
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getRoofCustomButton() {
		if (roofCustomButton == null) {
			roofCustomButton = new JToggleButton();
			roofCustomButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/roof_custom.png")));
			roofCustomButton.setToolTipText("Draw custom roof");
			roofCustomButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.DRAW_ROOF_CUSTOM);
				}
			});
			roofCustomButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if (e.getClickCount() > 1)
						SceneManager.getInstance().setOperationStick(true);
				}
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
				}
			});
		}
		return roofCustomButton;
	}

	public void deselect() {
		lastSelection = null;
		getSelectButton().setSelected(true);
		SceneManager.getInstance().setOperation(Operation.SELECT);
	}

	private JToggleButton getAnnotationToggleButton() {
		if (annotationToggleButton == null) {
			annotationToggleButton = new JToggleButton();
			annotationToggleButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
				}
			});
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

	/**
	 * This method initializes zoomButton	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getZoomButton() {
		if (zoomButton == null) {
			zoomButton = new JToggleButton();
			zoomButton.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
				}
			});
			zoomButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/zoom.png")));
			zoomButton.setToolTipText("Zoom");
			zoomButton.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					SceneManager.getInstance().setOperation(SceneManager.Operation.SELECT);
					SceneManager.getInstance().setZoomLock(zoomButton.isSelected());
				}
			});
		}
		return zoomButton;
	}

	/**
	 * This method initializes roofGableButton	
	 * 	
	 * @return javax.swing.JToggleButton	
	 */
	private JToggleButton getRoofGableButton() {
		if (roofGableButton == null) {
			roofGableButton = new JToggleButton();
			roofGableButton.setIcon(new ImageIcon(getClass().getResource("/org/concord/energy3d/resources/icons/roof_gable.png")));
			roofGableButton.setToolTipText("Convert to gable roof");
			roofGableButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					SceneManager.getInstance().setOperation(Operation.DRAW_ROOF_GABLE);
				}
			});
			roofGableButton.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if (e.getClickCount() > 1)
						SceneManager.getInstance().setOperationStick(true);
				}
				@Override
				public void mouseExited(final MouseEvent e) {
					SceneManager.getInstance().update();
				}
			});			
		}
		return roofGableButton;
	}
}
