package org.concord.energy3d.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CancellationException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.CityData;
import org.concord.energy3d.simulation.HeatLoad;
import org.concord.energy3d.simulation.SolarIrradiation;
import org.concord.energy3d.util.Util;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;

public class EnergyPanel extends JPanel {
	public static final ReadOnlyColorRGBA[] solarColors = { ColorRGBA.BLUE, ColorRGBA.GREEN, ColorRGBA.YELLOW, ColorRGBA.RED };
	private static final long serialVersionUID = 1L;
	private static final EnergyPanel instance = new EnergyPanel();
	private final DecimalFormat twoDecimals = new DecimalFormat();
	private final DecimalFormat noDecimals = new DecimalFormat();
	private static boolean keepHeatmapOn = false;

	public enum UpdateRadiation {
		ALWAYS, NEVER, ONLY_IF_SLECTED_IN_GUI
	};

	private final JComboBox<String> wallsComboBox;
	private final JComboBox<String> doorsComboBox;
	private final JComboBox<String> windowsComboBox;
	private final JComboBox<String> roofsComboBox;
	private final JComboBox<String> cityComboBox;
	private final JCheckBox autoCheckBox;
	private final JTextField heatingTodayTextField;
	private final JTextField coolingTodayTextField;
	private final JTextField totalTodayTextField;
	private final JSpinner insideTemperatureSpinner;
	private final JSpinner outsideTemperatureSpinner;
	private final JLabel dateLabel;
	private final JLabel timeLabel;
	private final JSpinner dateSpinner;
	private final JSpinner timeSpinner;
	private final JLabel latitudeLabel;
	private final JSpinner latitudeSpinner;
	private final JPanel panel_4;
	private final JSlider colorMapSlider;
	private final JPanel colormapPanel;
	private final JLabel legendLabel;
	private final JLabel contrastLabel;
	private final JProgressBar progressBar;

	private Thread thread;
	private boolean computeRequest;
	private final boolean initJavaFxAlreadyCalled = false;
	private boolean alreadyRenderedHeatmap = false;
	private UpdateRadiation updateRadiation;
	private boolean computeEnabled = true;
	private final ArrayList<PropertyChangeListener> propertyChangeListeners = new ArrayList<PropertyChangeListener>();
	private JPanel partPanel;
	private JLabel partEnergyLabel;
	private JTextField partEnergyTextField;
	private JPanel buildingPanel;
	private JLabel solarPotentialKWhLabel;
	private JLabel lblSolarPotentialEnergy;
	private JTextField houseSolarPotentialTextField;
	private JLabel lblNewLabel;
	private JPanel panel;
	private JPanel panel_2;
	private JLabel lblPosition;
	private JTextField positionTextField;
	private JLabel lblArea;
	private JTextField areaTextField;
	private JLabel lblHeight;
	private JTextField heightTextField;
	private JLabel lblVolume;
	private JTextField volumnTextField;
	private JTextField passiveSolarTextField;
	private JTextField photovoltaicTextField;
	private JPanel panel_6;
	private JPanel panel_8;
	private JLabel lblWidth;
	private JTextField partWidthTextField;
	private JLabel lblHeight_1;
	private JTextField partHeightTextField;

	private static class EnergyAmount {
		double solar;
		double solarPanel;
		double heating;
		double cooling;
	}

	public static EnergyPanel getInstance() {
		return instance;
	}

	private EnergyPanel() {
		twoDecimals.setMaximumFractionDigits(2);
		noDecimals.setMaximumFractionDigits(0);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		this.repaint();
		this.paint(null);

		progressBar = new JProgressBar();
		add(progressBar);

		final JPanel panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(null, "Time & Location", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel_3);
		final GridBagLayout gbl_panel_3 = new GridBagLayout();
		panel_3.setLayout(gbl_panel_3);

		dateLabel = new JLabel("Date: ");
		final GridBagConstraints gbc_dateLabel = new GridBagConstraints();
		gbc_dateLabel.gridx = 0;
		gbc_dateLabel.gridy = 0;
		panel_3.add(dateLabel, gbc_dateLabel);

		dateSpinner = new JSpinner();
		dateSpinner.setModel(new SpinnerDateModel(new Date(1380427200000L), null, null, Calendar.MONTH));
		dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MMMM dd"));
		dateSpinner.addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
			@Override
			public void ancestorResized(final HierarchyEvent e) {
				dateSpinner.setMinimumSize(dateSpinner.getPreferredSize());
				dateSpinner.setPreferredSize(dateSpinner.getPreferredSize());
				dateSpinner.removeHierarchyBoundsListener(this);
			}
		});
		dateSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
			boolean firstCall = true;

			@Override
			public void stateChanged(final javax.swing.event.ChangeEvent e) {
				if (firstCall) {
					firstCall = false;
					return;
				}
				final Heliodon heliodon = Heliodon.getInstance();
				if (heliodon != null)
					heliodon.setDate((Date) dateSpinner.getValue());
				compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
				Scene.getInstance().setEdited(true);
			}
		});
		final GridBagConstraints gbc_dateSpinner = new GridBagConstraints();
		gbc_dateSpinner.insets = new Insets(0, 0, 1, 1);
		gbc_dateSpinner.gridx = 1;
		gbc_dateSpinner.gridy = 0;
		panel_3.add(dateSpinner, gbc_dateSpinner);

		cityComboBox = new JComboBox<String>();
		cityComboBox.setModel(new DefaultComboBoxModel<String>(CityData.getInstance().getCities()));
		cityComboBox.setSelectedItem("Boston");
		cityComboBox.setMaximumRowCount(15);
		cityComboBox.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent e) {
				if (cityComboBox.getSelectedItem().equals(""))
					compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
				else {
					final Integer newLatitude = CityData.getInstance().getCityLatitutes().get(cityComboBox.getSelectedItem());
					if (newLatitude.equals(latitudeSpinner.getValue()))
						compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					else
						latitudeSpinner.setValue(newLatitude);
				}
				Scene.getInstance().setEdited(true);
			}
		});

		final GridBagConstraints gbc_cityComboBox = new GridBagConstraints();
		gbc_cityComboBox.gridwidth = 2;
		gbc_cityComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_cityComboBox.gridx = 2;
		gbc_cityComboBox.gridy = 0;
		panel_3.add(cityComboBox, gbc_cityComboBox);

		timeLabel = new JLabel("Time: ");
		final GridBagConstraints gbc_timeLabel = new GridBagConstraints();
		gbc_timeLabel.gridx = 0;
		gbc_timeLabel.gridy = 1;
		panel_3.add(timeLabel, gbc_timeLabel);

		timeSpinner = new JSpinner(new SpinnerDateModel());
		timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "H:mm"));
		timeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
			private boolean firstCall = true;

			@Override
			public void stateChanged(final javax.swing.event.ChangeEvent e) {
				// ignore the first event
				if (firstCall) {
					firstCall = false;
					return;
				}
				final Heliodon heliodon = Heliodon.getInstance();
				if (heliodon != null)
					heliodon.setTime((Date) timeSpinner.getValue());
				compute(UpdateRadiation.NEVER);
				Scene.getInstance().setEdited(true);
			}
		});
		final GridBagConstraints gbc_timeSpinner = new GridBagConstraints();
		gbc_timeSpinner.insets = new Insets(0, 0, 0, 1);
		gbc_timeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_timeSpinner.gridx = 1;
		gbc_timeSpinner.gridy = 1;
		panel_3.add(timeSpinner, gbc_timeSpinner);

		latitudeLabel = new JLabel("Latitude: ");
		final GridBagConstraints gbc_altitudeLabel = new GridBagConstraints();
		gbc_altitudeLabel.insets = new Insets(0, 1, 0, 0);
		gbc_altitudeLabel.gridx = 2;
		gbc_altitudeLabel.gridy = 1;
		panel_3.add(latitudeLabel, gbc_altitudeLabel);

		latitudeSpinner = new JSpinner();
		latitudeSpinner.setModel(new SpinnerNumberModel(Heliodon.DEFAULT_LATITUDE, -90, 90, 1));
		latitudeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
			@Override
			public void stateChanged(final javax.swing.event.ChangeEvent e) {
				if (!cityComboBox.getSelectedItem().equals("") && !CityData.getInstance().getCityLatitutes().values().contains(latitudeSpinner.getValue()))
					cityComboBox.setSelectedItem("");
				Heliodon.getInstance().setLatitude(((Integer) latitudeSpinner.getValue()) / 180.0 * Math.PI);
				compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
				Scene.getInstance().setEdited(true);
			}
		});
		final GridBagConstraints gbc_latitudeSpinner = new GridBagConstraints();
		gbc_latitudeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_latitudeSpinner.gridx = 3;
		gbc_latitudeSpinner.gridy = 1;
		panel_3.add(latitudeSpinner, gbc_latitudeSpinner);

		panel_3.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel_3.getPreferredSize().height));

		panel_4 = new JPanel();
		panel_4.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Solar Irradiation Heat Map", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel_4);
		final GridBagLayout gbl_panel_4 = new GridBagLayout();
		panel_4.setLayout(gbl_panel_4);

		legendLabel = new JLabel("Color Scale: ");
		final GridBagConstraints gbc_legendLabel = new GridBagConstraints();
		gbc_legendLabel.insets = new Insets(5, 0, 0, 0);
		gbc_legendLabel.anchor = GridBagConstraints.WEST;
		gbc_legendLabel.gridx = 0;
		gbc_legendLabel.gridy = 0;
		panel_4.add(legendLabel, gbc_legendLabel);

		colorMapSlider = new JSlider();
		colorMapSlider.setMinimum(10);
		colorMapSlider.setMaximum(90);
		colorMapSlider.setMinimumSize(colorMapSlider.getPreferredSize());
		colorMapSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (!colorMapSlider.getValueIsAdjusting()) {
					compute(SceneManager.getInstance().isSolarColorMap() ? UpdateRadiation.ALWAYS : UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					Scene.getInstance().setEdited(true, false);
				}
			}
		});

		colormapPanel = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(final Graphics g) {
				final int STEP = 5;
				final Dimension size = getSize();
				for (int x = 0; x < size.width - STEP; x += STEP) {
					final ColorRGBA color = SolarIrradiation.getInstance().computeColor(x, size.width);
					g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
					g.fillRect(x, 0, x + STEP, size.height);
				}
			}
		};
		final GridBagConstraints gbc_colormapPanel = new GridBagConstraints();
		gbc_colormapPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_colormapPanel.gridy = 0;
		gbc_colormapPanel.gridx = 1;
		panel_4.add(colormapPanel, gbc_colormapPanel);

		contrastLabel = new JLabel("Contrast: ");
		final GridBagConstraints gbc_contrastLabel = new GridBagConstraints();
		gbc_contrastLabel.anchor = GridBagConstraints.WEST;
		gbc_contrastLabel.gridx = 0;
		gbc_contrastLabel.gridy = 1;
		panel_4.add(contrastLabel, gbc_contrastLabel);
		colorMapSlider.setSnapToTicks(true);
		colorMapSlider.setMinorTickSpacing(10);
		colorMapSlider.setMajorTickSpacing(10);
		colorMapSlider.setPaintTicks(true);
		final GridBagConstraints gbc_colorMapSlider = new GridBagConstraints();
		gbc_colorMapSlider.gridy = 1;
		gbc_colorMapSlider.gridx = 1;
		panel_4.add(colorMapSlider, gbc_colorMapSlider);

		panel_4.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel_4.getPreferredSize().height));

		final JPanel temperaturePanel = new JPanel();
		temperaturePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Temperature \u00B0C", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(temperaturePanel);
		final GridBagLayout gbl_temperaturePanel = new GridBagLayout();
		temperaturePanel.setLayout(gbl_temperaturePanel);

		final JLabel insideTemperatureLabel = new JLabel("Inside: ");
		insideTemperatureLabel.setToolTipText("");
		final GridBagConstraints gbc_insideTemperatureLabel = new GridBagConstraints();
		gbc_insideTemperatureLabel.gridx = 1;
		gbc_insideTemperatureLabel.gridy = 0;
		temperaturePanel.add(insideTemperatureLabel, gbc_insideTemperatureLabel);

		insideTemperatureSpinner = new JSpinner();
		insideTemperatureSpinner.setToolTipText("Thermostat temperature setting for the inside of the house");
		insideTemperatureSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				compute(UpdateRadiation.NEVER);
			}
		});
		insideTemperatureSpinner.setModel(new SpinnerNumberModel(20, -70, 60, 1));
		final GridBagConstraints gbc_insideTemperatureSpinner = new GridBagConstraints();
		gbc_insideTemperatureSpinner.gridx = 2;
		gbc_insideTemperatureSpinner.gridy = 0;
		temperaturePanel.add(insideTemperatureSpinner, gbc_insideTemperatureSpinner);

		final JLabel outsideTemperatureLabel = new JLabel(" Outside: ");
		outsideTemperatureLabel.setToolTipText("");
		final GridBagConstraints gbc_outsideTemperatureLabel = new GridBagConstraints();
		gbc_outsideTemperatureLabel.gridx = 3;
		gbc_outsideTemperatureLabel.gridy = 0;
		temperaturePanel.add(outsideTemperatureLabel, gbc_outsideTemperatureLabel);

		temperaturePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, temperaturePanel.getPreferredSize().height));

		outsideTemperatureSpinner = new JSpinner();
		outsideTemperatureSpinner.setToolTipText("Outside temperature at this time and day");
		outsideTemperatureSpinner.setEnabled(false);
		outsideTemperatureSpinner.setModel(new SpinnerNumberModel(10, -70, 60, 1));
		outsideTemperatureSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (thread == null)
					compute(UpdateRadiation.NEVER);
			}
		});
		final GridBagConstraints gbc_outsideTemperatureSpinner = new GridBagConstraints();
		gbc_outsideTemperatureSpinner.gridx = 4;
		gbc_outsideTemperatureSpinner.gridy = 0;
		temperaturePanel.add(outsideTemperatureSpinner, gbc_outsideTemperatureSpinner);

		autoCheckBox = new JCheckBox("Auto");
		autoCheckBox.setToolTipText("Automatically set the outside temperature based on historic average of the selected city");
		autoCheckBox.setSelected(true);
		autoCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final boolean selected = autoCheckBox.isSelected();
				outsideTemperatureSpinner.setEnabled(!selected);
				if (selected)
					updateOutsideTemperature();
				compute(UpdateRadiation.NEVER);
			}
		});
		final GridBagConstraints gbc_autoCheckBox = new GridBagConstraints();
		gbc_autoCheckBox.gridx = 5;
		gbc_autoCheckBox.gridy = 0;
		temperaturePanel.add(autoCheckBox, gbc_autoCheckBox);

		final JPanel uFactorPanel = new JPanel();
		uFactorPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "U-Factor W/(m\u00B2.\u00B0C)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(uFactorPanel);
		final GridBagLayout gbl_uFactorPanel = new GridBagLayout();
		uFactorPanel.setLayout(gbl_uFactorPanel);

		final JLabel wallsLabel = new JLabel("Walls:");
		final GridBagConstraints gbc_wallsLabel = new GridBagConstraints();
		gbc_wallsLabel.anchor = GridBagConstraints.EAST;
		gbc_wallsLabel.insets = new Insets(0, 0, 5, 5);
		gbc_wallsLabel.gridx = 0;
		gbc_wallsLabel.gridy = 0;
		uFactorPanel.add(wallsLabel, gbc_wallsLabel);

		wallsComboBox = new WideComboBox();
		wallsComboBox.setEditable(true);
		wallsComboBox.setModel(new DefaultComboBoxModel(new String[] { "0.28 ", "0.67 (Concrete 8\")", "0.41 (Masonary Brick 8\")", "0.04 (Flat Metal 8\" Fiberglass Insulation)" }));
		wallsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				compute(UpdateRadiation.NEVER);
			}
		});
		final GridBagConstraints gbc_wallsComboBox = new GridBagConstraints();
		gbc_wallsComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_wallsComboBox.gridx = 1;
		gbc_wallsComboBox.gridy = 0;
		uFactorPanel.add(wallsComboBox, gbc_wallsComboBox);

		final JLabel doorsLabel = new JLabel("Doors:");
		final GridBagConstraints gbc_doorsLabel = new GridBagConstraints();
		gbc_doorsLabel.anchor = GridBagConstraints.EAST;
		gbc_doorsLabel.insets = new Insets(0, 0, 5, 5);
		gbc_doorsLabel.gridx = 2;
		gbc_doorsLabel.gridy = 0;
		uFactorPanel.add(doorsLabel, gbc_doorsLabel);

		doorsComboBox = new WideComboBox();
		doorsComboBox.setEditable(true);
		doorsComboBox.setModel(new DefaultComboBoxModel(new String[] { "1.14 ", "1.20 (Steel)", "0.64 (Wood)" }));
		doorsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				compute(UpdateRadiation.NEVER);
			}
		});
		final GridBagConstraints gbc_doorsComboBox = new GridBagConstraints();
		gbc_doorsComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_doorsComboBox.gridx = 3;
		gbc_doorsComboBox.gridy = 0;
		uFactorPanel.add(doorsComboBox, gbc_doorsComboBox);

		final JLabel windowsLabel = new JLabel("Windows:");
		final GridBagConstraints gbc_windowsLabel = new GridBagConstraints();
		gbc_windowsLabel.anchor = GridBagConstraints.EAST;
		gbc_windowsLabel.insets = new Insets(0, 0, 0, 5);
		gbc_windowsLabel.gridx = 0;
		gbc_windowsLabel.gridy = 1;
		uFactorPanel.add(windowsLabel, gbc_windowsLabel);

		windowsComboBox = new WideComboBox();
		windowsComboBox.setEditable(true);
		windowsComboBox.setModel(new DefaultComboBoxModel(new String[] { "1.89 ", "1.22 (Single Pane)", "0.70 (Double Pane)" }));
		windowsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				compute(UpdateRadiation.NEVER);
			}
		});
		final GridBagConstraints gbc_windowsComboBox = new GridBagConstraints();
		gbc_windowsComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_windowsComboBox.gridx = 1;
		gbc_windowsComboBox.gridy = 1;
		uFactorPanel.add(windowsComboBox, gbc_windowsComboBox);

		final JLabel roofsLabel = new JLabel("Roofs:");
		final GridBagConstraints gbc_roofsLabel = new GridBagConstraints();
		gbc_roofsLabel.anchor = GridBagConstraints.EAST;
		gbc_roofsLabel.insets = new Insets(0, 0, 0, 5);
		gbc_roofsLabel.gridx = 2;
		gbc_roofsLabel.gridy = 1;
		uFactorPanel.add(roofsLabel, gbc_roofsLabel);

		roofsComboBox = new WideComboBox();
		roofsComboBox.setEditable(true);
		roofsComboBox.setModel(new DefaultComboBoxModel(new String[] { "0.14 ", "0.23 (Concrete 3\")", "0.11 (Flat Metal 3\" Fiberglass Insulation)", "0.10 (Wood 3\" Fiberglass Insulation)" }));
		roofsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				compute(UpdateRadiation.NEVER);
			}
		});
		final GridBagConstraints gbc_roofsComboBox = new GridBagConstraints();
		gbc_roofsComboBox.gridx = 3;
		gbc_roofsComboBox.gridy = 1;
		uFactorPanel.add(roofsComboBox, gbc_roofsComboBox);

		uFactorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, uFactorPanel.getPreferredSize().height));

		partPanel = new JPanel();
		partPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Part", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(partPanel);

		buildingPanel = new JPanel();
		buildingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Building", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(buildingPanel);
		buildingPanel.setLayout(new BoxLayout(buildingPanel, BoxLayout.Y_AXIS));

		panel = new JPanel();
		buildingPanel.add(panel);

		lblSolarPotentialEnergy = new JLabel("Solar Potential:");
		panel.add(lblSolarPotentialEnergy);

		houseSolarPotentialTextField = new JTextField();
		houseSolarPotentialTextField.setEditable(false);
		panel.add(houseSolarPotentialTextField);
		houseSolarPotentialTextField.setColumns(10);

		lblNewLabel = new JLabel("kWh");
		panel.add(lblNewLabel);

		panel_2 = new JPanel();
		buildingPanel.add(panel_2);
		final GridBagLayout gbl_panel_2 = new GridBagLayout();
		panel_2.setLayout(gbl_panel_2);

		lblPosition = new JLabel("Position:");
		final GridBagConstraints gbc_lblPosition = new GridBagConstraints();
		gbc_lblPosition.anchor = GridBagConstraints.EAST;
		gbc_lblPosition.insets = new Insets(0, 0, 5, 5);
		gbc_lblPosition.gridx = 0;
		gbc_lblPosition.gridy = 0;
		panel_2.add(lblPosition, gbc_lblPosition);

		positionTextField = new JTextField();
		positionTextField.setEditable(false);
		final GridBagConstraints gbc_positionTextField = new GridBagConstraints();
		gbc_positionTextField.weightx = 1.0;
		gbc_positionTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_positionTextField.anchor = GridBagConstraints.NORTH;
		gbc_positionTextField.insets = new Insets(0, 0, 5, 5);
		gbc_positionTextField.gridx = 1;
		gbc_positionTextField.gridy = 0;
		panel_2.add(positionTextField, gbc_positionTextField);
		positionTextField.setColumns(10);

		lblHeight = new JLabel("Height:");
		final GridBagConstraints gbc_lblHeight = new GridBagConstraints();
		gbc_lblHeight.anchor = GridBagConstraints.EAST;
		gbc_lblHeight.insets = new Insets(0, 0, 5, 5);
		gbc_lblHeight.gridx = 2;
		gbc_lblHeight.gridy = 0;
		panel_2.add(lblHeight, gbc_lblHeight);

		heightTextField = new JTextField();
		heightTextField.setEditable(false);
		final GridBagConstraints gbc_heightTextField = new GridBagConstraints();
		gbc_heightTextField.weightx = 1.0;
		gbc_heightTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_heightTextField.insets = new Insets(0, 0, 5, 0);
		gbc_heightTextField.anchor = GridBagConstraints.NORTH;
		gbc_heightTextField.gridx = 3;
		gbc_heightTextField.gridy = 0;
		panel_2.add(heightTextField, gbc_heightTextField);
		heightTextField.setColumns(10);

		lblArea = new JLabel("Area:");
		final GridBagConstraints gbc_lblArea = new GridBagConstraints();
		gbc_lblArea.anchor = GridBagConstraints.EAST;
		gbc_lblArea.insets = new Insets(0, 0, 10, 5);
		gbc_lblArea.gridx = 0;
		gbc_lblArea.gridy = 1;
		panel_2.add(lblArea, gbc_lblArea);

		areaTextField = new JTextField();
		areaTextField.setEditable(false);
		final GridBagConstraints gbc_areaTextField = new GridBagConstraints();
		gbc_areaTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_areaTextField.insets = new Insets(0, 0, 10, 5);
		gbc_areaTextField.gridx = 1;
		gbc_areaTextField.gridy = 1;
		panel_2.add(areaTextField, gbc_areaTextField);
		areaTextField.setColumns(10);

		lblVolume = new JLabel("Volume:");
		final GridBagConstraints gbc_lblVolume = new GridBagConstraints();
		gbc_lblVolume.anchor = GridBagConstraints.EAST;
		gbc_lblVolume.insets = new Insets(0, 0, 10, 5);
		gbc_lblVolume.gridx = 2;
		gbc_lblVolume.gridy = 1;
		panel_2.add(lblVolume, gbc_lblVolume);

		volumnTextField = new JTextField();
		volumnTextField.setEditable(false);
		final GridBagConstraints gbc_volumnTextField = new GridBagConstraints();
		gbc_volumnTextField.insets = new Insets(0, 0, 10, 0);
		gbc_volumnTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_volumnTextField.gridx = 3;
		gbc_volumnTextField.gridy = 1;
		panel_2.add(volumnTextField, gbc_volumnTextField);
		volumnTextField.setColumns(10);

		final Component verticalGlue = Box.createVerticalGlue();
		add(verticalGlue);

		JPanel target = buildingPanel;
		target.setMaximumSize(new Dimension(target.getMaximumSize().width, target.getPreferredSize().height));

		final JPanel panel_1 = new JPanel();
		buildingPanel.add(panel_1);
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Energy Today (kWh)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		final GridBagLayout gbl_panel_1 = new GridBagLayout();
		panel_1.setLayout(gbl_panel_1);

		final JLabel sunLabel = new JLabel("Windows");
		sunLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_sunLabel = new GridBagConstraints();
		gbc_sunLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_sunLabel.insets = new Insets(0, 0, 5, 5);
		gbc_sunLabel.gridx = 0;
		gbc_sunLabel.gridy = 0;
		panel_1.add(sunLabel, gbc_sunLabel);

		final JLabel solarLabel = new JLabel("Solar Panels");
		solarLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_solarLabel = new GridBagConstraints();
		gbc_solarLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_solarLabel.insets = new Insets(0, 0, 5, 5);
		gbc_solarLabel.gridx = 1;
		gbc_solarLabel.gridy = 0;
		panel_1.add(solarLabel, gbc_solarLabel);

		final JLabel heatingLabel = new JLabel("Heater");
		heatingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_heatingLabel = new GridBagConstraints();
		gbc_heatingLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_heatingLabel.insets = new Insets(0, 0, 5, 5);
		gbc_heatingLabel.gridx = 2;
		gbc_heatingLabel.gridy = 0;
		panel_1.add(heatingLabel, gbc_heatingLabel);

		final JLabel coolingLabel = new JLabel("AC");
		coolingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_coolingLabel = new GridBagConstraints();
		gbc_coolingLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_coolingLabel.insets = new Insets(0, 0, 5, 5);
		gbc_coolingLabel.gridx = 3;
		gbc_coolingLabel.gridy = 0;
		panel_1.add(coolingLabel, gbc_coolingLabel);

		final JLabel totalLabel = new JLabel("Net");
		totalLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_totalLabel = new GridBagConstraints();
		gbc_totalLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_totalLabel.insets = new Insets(0, 0, 5, 0);
		gbc_totalLabel.gridx = 4;
		gbc_totalLabel.gridy = 0;
		panel_1.add(totalLabel, gbc_totalLabel);

		passiveSolarTextField = new JTextField();
		final GridBagConstraints gbc_passiveSolarTextField = new GridBagConstraints();
		gbc_passiveSolarTextField.weightx = 1.0;
		gbc_passiveSolarTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_passiveSolarTextField.insets = new Insets(0, 0, 0, 5);
		gbc_passiveSolarTextField.gridx = 0;
		gbc_passiveSolarTextField.gridy = 1;
		panel_1.add(passiveSolarTextField, gbc_passiveSolarTextField);
		passiveSolarTextField.setEditable(false);
		passiveSolarTextField.setColumns(5);

		photovoltaicTextField = new JTextField();
		final GridBagConstraints gbc_photovoltaicTextField = new GridBagConstraints();
		gbc_photovoltaicTextField.weightx = 1.0;
		gbc_photovoltaicTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_photovoltaicTextField.insets = new Insets(0, 0, 0, 5);
		gbc_photovoltaicTextField.gridx = 1;
		gbc_photovoltaicTextField.gridy = 1;
		panel_1.add(photovoltaicTextField, gbc_photovoltaicTextField);
		photovoltaicTextField.setEditable(false);
		photovoltaicTextField.setColumns(5);

		heatingTodayTextField = new JTextField();
		heatingTodayTextField.setEditable(false);
		final GridBagConstraints gbc_heatingTodayTextField = new GridBagConstraints();
		gbc_heatingTodayTextField.weightx = 1.0;
		gbc_heatingTodayTextField.insets = new Insets(0, 0, 0, 5);
		gbc_heatingTodayTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_heatingTodayTextField.gridx = 2;
		gbc_heatingTodayTextField.gridy = 1;
		panel_1.add(heatingTodayTextField, gbc_heatingTodayTextField);
		heatingTodayTextField.setColumns(5);

		coolingTodayTextField = new JTextField();
		coolingTodayTextField.setEditable(false);
		final GridBagConstraints gbc_coolingTodayTextField = new GridBagConstraints();
		gbc_coolingTodayTextField.weightx = 1.0;
		gbc_coolingTodayTextField.insets = new Insets(0, 0, 0, 5);
		gbc_coolingTodayTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_coolingTodayTextField.gridx = 3;
		gbc_coolingTodayTextField.gridy = 1;
		panel_1.add(coolingTodayTextField, gbc_coolingTodayTextField);
		coolingTodayTextField.setColumns(5);

		totalTodayTextField = new JTextField();
		totalTodayTextField.setEditable(false);
		final GridBagConstraints gbc_totalTodayTextField = new GridBagConstraints();
		gbc_totalTodayTextField.weightx = 1.0;
		gbc_totalTodayTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_totalTodayTextField.gridx = 4;
		gbc_totalTodayTextField.gridy = 1;
		panel_1.add(totalTodayTextField, gbc_totalTodayTextField);
		totalTodayTextField.setColumns(5);

		panel_1.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel_1.getPreferredSize().height));

		final Dimension size = heatingLabel.getMinimumSize();
		sunLabel.setMinimumSize(size);
		solarLabel.setMinimumSize(size);
		coolingLabel.setMinimumSize(size);
		totalLabel.setMinimumSize(size);
		target = partPanel;
		target.setMaximumSize(new Dimension(target.getMaximumSize().width, target.getPreferredSize().height));
		partPanel.setLayout(new BoxLayout(partPanel, BoxLayout.Y_AXIS));

		panel_6 = new JPanel();
		partPanel.add(panel_6);

		partEnergyLabel = new JLabel("Solar Potential:");
		panel_6.add(partEnergyLabel);

		partEnergyTextField = new JTextField();
		panel_6.add(partEnergyTextField);
		partEnergyTextField.setEditable(false);
		partEnergyTextField.setColumns(10);

		solarPotentialKWhLabel = new JLabel("kWh");
		panel_6.add(solarPotentialKWhLabel);

		panel_8 = new JPanel();
		partPanel.add(panel_8);

		lblWidth = new JLabel("Width:");
		panel_8.add(lblWidth);

		partWidthTextField = new JTextField();
		partWidthTextField.setEditable(false);
		panel_8.add(partWidthTextField);
		partWidthTextField.setColumns(10);

		lblHeight_1 = new JLabel("Height:");
		panel_8.add(lblHeight_1);

		partHeightTextField = new JTextField();
		partHeightTextField.setEditable(false);
		panel_8.add(partHeightTextField);
		partHeightTextField.setColumns(10);
	}

	public void compute(final UpdateRadiation updateRadiation) {
		if (!computeEnabled)
			return;
		this.updateRadiation = updateRadiation;
		if (thread != null && thread.isAlive())
			computeRequest = true;
		else {
			thread = new Thread("Energy Computer") {
				@Override
				public void run() {
					do {
						computeRequest = false;
						/* since this thread can accept multiple computeRequest, cannot use updateRadiationColorMap parameter directly */
						try {
							computeNow(EnergyPanel.this.updateRadiation);
						} catch (final Throwable e) {
							e.printStackTrace();
							Util.reportError(e);
						}
						try {
							Thread.sleep(500);
						} catch (final InterruptedException e) {
							e.printStackTrace();
						}
						progressBar.setValue(0);
					} while (computeRequest);
					thread = null;
				}
			};
			thread.start();
		}
	}

	public void computeNow(final UpdateRadiation updateRadiation) {
		try {
			System.out.println("EnergyPanel.computeNow()");
			progressBar.setValue(0);
			if (updateRadiation != UpdateRadiation.NEVER) {
				if (updateRadiation == UpdateRadiation.ALWAYS || (SceneManager.getInstance().isSolarColorMap() && (!alreadyRenderedHeatmap || keepHeatmapOn))) {
					alreadyRenderedHeatmap = true;
					SolarIrradiation.getInstance().compute();
					notifyPropertyChangeListeners(new PropertyChangeEvent(EnergyPanel.this, "Solar energy calculation completed", 0, 1));
				} else {
					if (SceneManager.getInstance().isSolarColorMap())
						MainPanel.getInstance().getSolarButton().setSelected(false);
					int numberOfHouses = 0;
					for (final HousePart part : Scene.getInstance().getParts()) {
						if (part instanceof Foundation && !part.getChildren().isEmpty() && !part.isFrozen())
							numberOfHouses++;
						if (numberOfHouses >= 2)
							break;
					}
					for (final HousePart part : Scene.getInstance().getParts())
						if (part instanceof Foundation)
							((Foundation) part).setSolarLabelValue(numberOfHouses >= 2 && !part.getChildren().isEmpty() && !part.isFrozen() ? -1 : -2);
					SceneManager.getInstance().refresh();
				}
			}
			computeEnergy();
			updatePartEnergy();
		} catch (final CancellationException e) {
			System.out.println("Energy compute cancelled.");
		} catch (final RuntimeException e) {
			e.printStackTrace();
		}
	}

	// TODO: There should be a better way to do this.
	public void clearIrradiationHeatMap() {
		compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
	}

	private void computeEnergy() {
		if (autoCheckBox.isSelected())
			updateOutsideTemperature();

		HeatLoad.getInstance().computeEnergyToday((Calendar) Heliodon.getInstance().getCalander().clone(), (Integer) insideTemperatureSpinner.getValue());
	}

	private void updateOutsideTemperature() {
		if (cityComboBox.getSelectedItem().equals(""))
			outsideTemperatureSpinner.setValue(15);
		else {
//			final double[] temperature = CityData.getInstance().computeOutsideTemperature(Heliodon.getInstance().getCalander(), (String) cityComboBox.getSelectedItem());
//			final double avgTemperature = (temperature[0] + temperature[1]) / 2.0;
//			outsideTemperatureSpinner.setValue((int) Math.round(avgTemperature));
			outsideTemperatureSpinner.setValue(Math.round(CityData.getInstance().computeOutsideTemperature(Heliodon.getInstance().getCalander())));
		}
	}

	public JSpinner getDateSpinner() {
		return dateSpinner;
	}

	public JSpinner getTimeSpinner() {
		return timeSpinner;
	}

	public void progress() {
		progressBar.setValue(progressBar.getValue() + 1);
	}

	public void setLatitude(final int latitude) {
		latitudeSpinner.setValue(latitude);
	}

	public int getLatitude() {
		return (Integer) latitudeSpinner.getValue();
	}

	public JSlider getColorMapSlider() {
		return colorMapSlider;
	}

	public void clearAlreadyRendered() {
		alreadyRenderedHeatmap = false;
	}

	public static void setKeepHeatmapOn(final boolean on) {
		keepHeatmapOn = on;
	}

	public void setComputeEnabled(final boolean computeEnabled) {
		this.computeEnabled = computeEnabled;
	}

	@Override
	public void addPropertyChangeListener(final PropertyChangeListener pcl) {
		propertyChangeListeners.add(pcl);
	}

	@Override
	public void removePropertyChangeListener(final PropertyChangeListener pcl) {
		propertyChangeListeners.remove(pcl);
	}

	private void notifyPropertyChangeListeners(final PropertyChangeEvent evt) {
		if (!propertyChangeListeners.isEmpty()) {
			for (final PropertyChangeListener x : propertyChangeListeners) {
				x.propertyChange(evt);
			}
		}
	}

	public void updatePartEnergy() {
		final boolean iradiationEnabled = MainPanel.getInstance().getSolarButton().isSelected();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();

		((TitledBorder) partPanel.getBorder()).setTitle("Part" + (selectedPart == null ? "" : (" - " + selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1))));
		partPanel.repaint();

		if (!iradiationEnabled || selectedPart == null || selectedPart instanceof Foundation || selectedPart instanceof Door)
			partEnergyTextField.setText("");
		else
			partEnergyTextField.setText(twoDecimals.format(selectedPart.getSolarPotentialToday()));

		if (selectedPart != null && !(selectedPart instanceof Roof || selectedPart instanceof Floor || selectedPart instanceof Tree)) {
			if (selectedPart instanceof SolarPanel) {
				partWidthTextField.setText(twoDecimals.format(SolarPanel.WIDTH));
				partHeightTextField.setText(twoDecimals.format(SolarPanel.HEIGHT));
			} else {
				partWidthTextField.setText(twoDecimals.format((selectedPart.getAbsPoint(0).distance(selectedPart.getAbsPoint(2))) * Scene.getInstance().getAnnotationScale()));
				partHeightTextField.setText(twoDecimals.format((selectedPart.getAbsPoint(0).distance(selectedPart.getAbsPoint(1))) * Scene.getInstance().getAnnotationScale()));
			}
		} else {
			partWidthTextField.setText("");
			partHeightTextField.setText("");
		}

		final Foundation selectedBuilding;
		if (selectedPart == null)
			selectedBuilding = null;
		else if (selectedPart instanceof Foundation)
			selectedBuilding = (Foundation) selectedPart;
		else
			selectedBuilding = (Foundation) selectedPart.getTopContainer();

		if (selectedBuilding != null) {
			if (iradiationEnabled) {
				houseSolarPotentialTextField.setText(twoDecimals.format(selectedBuilding.getSolarPotentialToday()));
				passiveSolarTextField.setText(twoDecimals.format(selectedBuilding.getPassiveSolarToday()));
				photovoltaicTextField.setText(twoDecimals.format(selectedBuilding.getPhotovoltaicToday()));
				heatingTodayTextField.setText(twoDecimals.format(selectedBuilding.getHeatingToday()));
				coolingTodayTextField.setText(twoDecimals.format(selectedBuilding.getCoolingToday()));
				totalTodayTextField.setText(twoDecimals.format(selectedBuilding.getTotalEnergyToday()));
			} else {
				houseSolarPotentialTextField.setText("");
				passiveSolarTextField.setText("");
				photovoltaicTextField.setText("");
			}
			final double[] buildingGeometry = selectedBuilding.getBuildingGeometry();
			if (buildingGeometry != null) {
				positionTextField.setText("(" + twoDecimals.format(buildingGeometry[3]) + ", " + twoDecimals.format(buildingGeometry[4]) + ")");
				heightTextField.setText(twoDecimals.format(buildingGeometry[0]));
				areaTextField.setText(twoDecimals.format(buildingGeometry[1]));
				volumnTextField.setText(twoDecimals.format(buildingGeometry[2]));
			} else {
				positionTextField.setText("");
				heightTextField.setText("");
				areaTextField.setText("");
				volumnTextField.setText("");
			}
		} else {
			houseSolarPotentialTextField.setText("");
			positionTextField.setText("");
			heightTextField.setText("");
			areaTextField.setText("");
			volumnTextField.setText("");
		}

	}

	public boolean isComputeRequest() {
		return computeRequest;
	}

	public JComboBox<String> getWallsComboBox() {
		return wallsComboBox;
	}

	public JComboBox<String> getDoorsComboBox() {
		return doorsComboBox;
	}

	public JComboBox<String> getWindowsComboBox() {
		return windowsComboBox;
	}

	public JComboBox<String> getRoofsComboBox() {
		return roofsComboBox;
	}

	public JComboBox<String> getCityComboBox() {
		return cityComboBox;
	}

}
