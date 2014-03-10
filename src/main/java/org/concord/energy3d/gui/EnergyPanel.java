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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.JFXPanel;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.SolarIrradiation;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.Util;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;

public class EnergyPanel extends JPanel {
	public static final ReadOnlyColorRGBA[] solarColors = { ColorRGBA.BLUE, ColorRGBA.GREEN, ColorRGBA.YELLOW, ColorRGBA.RED };
	private static final double COST_PER_KWH = 0.13;
	private static final long serialVersionUID = 1L;
	private static final Map<String, Integer> cityLatitute = new HashMap<String, Integer>();
	private static final Map<String, int[]> avgMonthlyLowTemperatures = new HashMap<String, int[]>();
	private static final Map<String, int[]> avgMonthlyHighTemperatures = new HashMap<String, int[]>();
	private static final EnergyPanel instance = new EnergyPanel();
	private static final DecimalFormat twoDecimals = new DecimalFormat();
	private static final DecimalFormat noDecimals = new DecimalFormat();
	private static final DecimalFormat moneyDecimals = new DecimalFormat();
	private static boolean keepHeatmapOn = false;

	public enum UpdateRadiation {
		ALWAYS, NEVER, ONLY_IF_SLECTED_IN_GUI
	};

	static {
		twoDecimals.setMaximumFractionDigits(2);
		noDecimals.setMaximumFractionDigits(0);
		moneyDecimals.setMaximumFractionDigits(0);
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
		avgMonthlyLowTemperatures.put("Boston", new int[] { -6, -4, -1, 5, 10, 16, 18, 18, 14, 8, 3, -2 });
		avgMonthlyHighTemperatures.put("Boston", new int[] { 2, 4, 7, 13, 19, 24, 28, 27, 22, 16, 11, 5 });
		avgMonthlyLowTemperatures.put("Moscow", new int[] { -14, -14, -9, 0, 6, 10, 13, 11, 6, 1, -5, -10 });
		avgMonthlyHighTemperatures.put("Moscow", new int[] { -7, -6, 0, 9, 17, 22, 24, 22, 16, 8, 0, -5 });
		avgMonthlyLowTemperatures.put("Ottawa", new int[] { -16, -14, -7, 1, 7, 12, 15, 14, 9, 3, -2, -11 });
		avgMonthlyHighTemperatures.put("Ottawa", new int[] { -7, -5, 2, 11, 18, 23, 26, 24, 19, 13, 4, -4 });
		avgMonthlyLowTemperatures.put("Beijing", new int[] { -9, -7, -1, 7, 13, 18, 21, 20, 14, 7, -1, -7 });
		avgMonthlyHighTemperatures.put("Beijing", new int[] { 1, 4, 11, 19, 26, 30, 31, 29, 26, 19, 10, 3 });
		avgMonthlyLowTemperatures.put("Washington DC", new int[] { -2, -1, 3, 8, 13, 19, 22, 21, 17, 11, 5, 1 });
		avgMonthlyHighTemperatures.put("Washington DC", new int[] { 6, 8, 13, 19, 24, 29, 32, 31, 27, 30, 14, 8 });
		avgMonthlyLowTemperatures.put("Tehran", new int[] { 1, 3, 7, 13, 17, 22, 25, 25, 21, 15, 8, 3 });
		avgMonthlyHighTemperatures.put("Tehran", new int[] { 8, 11, 16, 23, 28, 34, 37, 36, 32, 25, 16, 10 });
		avgMonthlyLowTemperatures.put("Los Angeles", new int[] { 9, 9, 11, 12, 14, 16, 18, 18, 17, 15, 11, 8 });
		avgMonthlyHighTemperatures.put("Los Angeles", new int[] { 20, 21, 21, 23, 23, 26, 28, 29, 28, 26, 23, 20 });
		avgMonthlyLowTemperatures.put("Miami", new int[] { 16, 17, 18, 21, 23, 25, 26, 26, 26, 24, 21, 18 });
		avgMonthlyHighTemperatures.put("Miami", new int[] { 23, 24, 24, 26, 28, 31, 31, 32, 31, 29, 26, 24 });
		avgMonthlyLowTemperatures.put("Mexico City", new int[] { 6, 7, 9, 11, 12, 12, 12, 12, 12, 10, 8, 7 });
		avgMonthlyHighTemperatures.put("Mexico City", new int[] { 21, 23, 25, 26, 26, 24, 23, 23, 23, 22, 22, 21 });
		avgMonthlyLowTemperatures.put("Singapore", new int[] { 23, 23, 24, 24, 24, 24, 24, 24, 24, 24, 23, 23 });
		avgMonthlyHighTemperatures.put("Singapore", new int[] { 29, 31, 31, 32, 31, 31, 31, 31, 31, 31, 30, 29 });
		avgMonthlyLowTemperatures.put("Sydney", new int[] { 19, 19, 18, 15, 12, 9, 8, 8, 11, 14, 16, 18 });
		avgMonthlyHighTemperatures.put("Sydney", new int[] { 26, 26, 25, 23, 20, 17, 17, 18, 20, 22, 23, 25 });
		avgMonthlyLowTemperatures.put("Buenos Aires", new int[] { 20, 19, 18, 14, 11, 8, 8, 9, 11, 13, 16, 18 });
		avgMonthlyHighTemperatures.put("Buenos Aires", new int[] { 28, 27, 25, 22, 18, 15, 14, 16, 18, 21, 24, 27 });
	}

	private JFXPanel fxPanel;
	private final XYChart.Data<String, Number> wallsAreaChartData = new XYChart.Data<String, Number>("Area", 0);
	private final XYChart.Data<String, Number> windowsAreaChartData = new XYChart.Data<String, Number>("Area", 0);
	private final XYChart.Data<String, Number> doorsAreaChartData = new XYChart.Data<String, Number>("Area", 0);
	private final XYChart.Data<String, Number> roofsAreaChartData = new XYChart.Data<String, Number>("Area", 0);
	private final XYChart.Data<String, Number> wallsEnergyChartData = new XYChart.Data<String, Number>("Energy Loss", 0);
	private final XYChart.Data<String, Number> windowsEnergyChartData = new XYChart.Data<String, Number>("Energy Loss", 0);
	private final XYChart.Data<String, Number> doorsEnergyChartData = new XYChart.Data<String, Number>("Energy Loss", 0);
	private final XYChart.Data<String, Number> roofsEnergyChartData = new XYChart.Data<String, Number>("Energy Loss", 0);
	private final JTextField heatingRateTextField;
	private final JComboBox wallsComboBox;
	private final JComboBox doorsComboBox;
	private final JComboBox windowsComboBox;
	private final JComboBox roofsComboBox;
	private final JCheckBox autoCheckBox;
	private final JTextField heatingYearlyTextField;
	private final JTextField sunRateTextField;
	private final JTextField sunTodayTextField;
	private final JTextField sunYearlyTextField;
	private final JTextField heatingTodayTextField;
	private final JTextField coolingRateTextField;
	private final JTextField coolingTodayTextField;
	private final JTextField coolingYearlyTextField;
	private final JTextField totalRateTextField;
	private final JTextField totalTodayTextField;
	private final JTextField totalYearlyTextField;
	private final JTextField heatingCostTextField;
	private final JTextField coolingCostTextField;
	private final JTextField totalCostTextField;
	private final JSpinner insideTemperatureSpinner;
	private final JSpinner outsideTemperatureSpinner;
	private final JLabel dateLabel;
	private final JLabel timeLabel;
	private final JSpinner dateSpinner;
	private final JSpinner timeSpinner;
	private final JComboBox cityComboBox;
	private final JLabel latitudeLabel;
	private final JSpinner latitudeSpinner;
	private final JPanel panel_4;
	private final JSlider colorMapSlider;
	private final JPanel colormapPanel;
	private final JLabel legendLabel;
	private final JLabel contrastLabel;
	private final JProgressBar progressBar;
	private JTextField solarRateTextField;
	private JTextField solarTodayTextField;
	private JTextField solarYearlyTextField;

	private Thread thread;
	private double wallsArea;
	private double doorsArea;
	private double windowsArea;
	private double roofsArea;
	private double wallUFactor;
	private double doorUFactor;
	private double windowUFactor;
	private double roofUFactor;
	private boolean computeRequest;
	private boolean initJavaFxAlreadyCalled = false;
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
	private JPanel panel_5;
	private JLabel lblVolume;
	private JTextField volumnTextField;
	private JPanel allPanel;
	private JLabel lblSolarPotential;
	private JTextField solarPotentialAlltextField;
	private JLabel lblPassiveSolar;
	private JTextField passiveSolarTextField;
	private JLabel lblPhotovoltaic;
	private JTextField photovoltaicTextField;
	private JPanel panel_7;
	private JLabel lblKwh;
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

		cityComboBox = new JComboBox();
		cityComboBox.setModel(new DefaultComboBoxModel(new String[] { "", "Moscow", "Ottawa", "Boston", "Beijing", "Washington DC", "Tehran", "Los Angeles", "Miami", "Mexico City", "Singapore", "Sydney", "Buenos Aires" }));
		cityComboBox.setSelectedItem("Boston");
		cityComboBox.setMaximumRowCount(15);
		cityComboBox.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(final java.awt.event.ActionEvent e) {
				if (cityComboBox.getSelectedItem().equals(""))
					compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
				else {
					final Integer newLatitude = cityLatitute.get(cityComboBox.getSelectedItem());
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
				if (!cityComboBox.getSelectedItem().equals("") && !cityLatitute.values().contains(latitudeSpinner.getValue()))
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
		gbl_panel_2.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
		gbl_panel_2.rowWeights = new double[] { 0.0, 0.0 };
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
		gbc_lblArea.insets = new Insets(0, 0, 0, 5);
		gbc_lblArea.gridx = 0;
		gbc_lblArea.gridy = 1;
		panel_2.add(lblArea, gbc_lblArea);

		areaTextField = new JTextField();
		areaTextField.setEditable(false);
		final GridBagConstraints gbc_areaTextField = new GridBagConstraints();
		gbc_areaTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_areaTextField.insets = new Insets(0, 0, 0, 5);
		gbc_areaTextField.gridx = 1;
		gbc_areaTextField.gridy = 1;
		panel_2.add(areaTextField, gbc_areaTextField);
		areaTextField.setColumns(10);

		lblVolume = new JLabel("Volume:");
		final GridBagConstraints gbc_lblVolume = new GridBagConstraints();
		gbc_lblVolume.anchor = GridBagConstraints.EAST;
		gbc_lblVolume.insets = new Insets(0, 0, 0, 5);
		gbc_lblVolume.gridx = 2;
		gbc_lblVolume.gridy = 1;
		panel_2.add(lblVolume, gbc_lblVolume);

		volumnTextField = new JTextField();
		volumnTextField.setEditable(false);
		final GridBagConstraints gbc_volumnTextField = new GridBagConstraints();
		gbc_volumnTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_volumnTextField.gridx = 3;
		gbc_volumnTextField.gridy = 1;
		panel_2.add(volumnTextField, gbc_volumnTextField);
		volumnTextField.setColumns(10);

		allPanel = new JPanel();
		allPanel.setBorder(new TitledBorder(null, "All", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(allPanel);
		allPanel.setLayout(new BoxLayout(allPanel, BoxLayout.Y_AXIS));

		panel_5 = new JPanel();
		allPanel.add(panel_5);

		lblSolarPotential = new JLabel("Solar Potential:");
		panel_5.add(lblSolarPotential);

		solarPotentialAlltextField = new JTextField();
		solarPotentialAlltextField.setEditable(false);
		panel_5.add(solarPotentialAlltextField);
		solarPotentialAlltextField.setColumns(10);

		lblKwh = new JLabel("kWh");
		panel_5.add(lblKwh);

		panel_7 = new JPanel();
		allPanel.add(panel_7);

		lblPassiveSolar = new JLabel("Passive Solar:");
		panel_7.add(lblPassiveSolar);

		passiveSolarTextField = new JTextField();
		passiveSolarTextField.setEditable(false);
		panel_7.add(passiveSolarTextField);
		passiveSolarTextField.setColumns(10);

		lblPhotovoltaic = new JLabel("Photovoltaic:");
		panel_7.add(lblPhotovoltaic);

		photovoltaicTextField = new JTextField();
		photovoltaicTextField.setEditable(false);
		panel_7.add(photovoltaicTextField);
		photovoltaicTextField.setColumns(10);

		final JPanel panel_1 = new JPanel();
		panel_1.setVisible(false);
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Energy", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel_1);
		final GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 0.0, 0.0 };
		panel_1.setLayout(gbl_panel_1);

		final JLabel sunLabel = new JLabel("Passive Solar");
		final GridBagConstraints gbc_sunLabel = new GridBagConstraints();
		gbc_sunLabel.insets = new Insets(0, 0, 5, 5);
		gbc_sunLabel.gridx = 1;
		gbc_sunLabel.gridy = 0;
		panel_1.add(sunLabel, gbc_sunLabel);

		final JLabel solarLabel = new JLabel("Photovoltaic");
		solarLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_solarLabel = new GridBagConstraints();
		gbc_solarLabel.insets = new Insets(0, 0, 5, 5);
		gbc_solarLabel.gridx = 2;
		gbc_solarLabel.gridy = 0;
		panel_1.add(solarLabel, gbc_solarLabel);

		final JLabel heatingLabel = new JLabel("Heating");
		heatingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_heatingLabel = new GridBagConstraints();
		gbc_heatingLabel.insets = new Insets(0, 0, 5, 5);
		gbc_heatingLabel.gridx = 3;
		gbc_heatingLabel.gridy = 0;
		panel_1.add(heatingLabel, gbc_heatingLabel);

		final JLabel coolingLabel = new JLabel("Cooling");
		coolingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_coolingLabel = new GridBagConstraints();
		gbc_coolingLabel.insets = new Insets(0, 0, 5, 5);
		gbc_coolingLabel.gridx = 4;
		gbc_coolingLabel.gridy = 0;
		panel_1.add(coolingLabel, gbc_coolingLabel);

		final JLabel totalLabel = new JLabel("Total");
		totalLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_totalLabel = new GridBagConstraints();
		gbc_totalLabel.insets = new Insets(0, 0, 5, 0);
		gbc_totalLabel.gridx = 5;
		gbc_totalLabel.gridy = 0;
		panel_1.add(totalLabel, gbc_totalLabel);

		final JLabel nowLabel = new JLabel("Now (watts):");
		final GridBagConstraints gbc_nowLabel = new GridBagConstraints();
		gbc_nowLabel.insets = new Insets(0, 0, 5, 5);
		gbc_nowLabel.anchor = GridBagConstraints.WEST;
		gbc_nowLabel.gridx = 0;
		gbc_nowLabel.gridy = 1;
		panel_1.add(nowLabel, gbc_nowLabel);

		sunRateTextField = new JTextField();
		sunRateTextField.setEditable(false);
		final GridBagConstraints gbc_sunRateTextField = new GridBagConstraints();
		gbc_sunRateTextField.insets = new Insets(0, 0, 5, 5);
		gbc_sunRateTextField.weightx = 1.0;
		gbc_sunRateTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_sunRateTextField.gridx = 1;
		gbc_sunRateTextField.gridy = 1;
		sunRateTextField.setColumns(5);
		panel_1.add(sunRateTextField, gbc_sunRateTextField);

		solarRateTextField = new JTextField();
		solarRateTextField.setEditable(false);
		final GridBagConstraints gbc_solarRateTextField = new GridBagConstraints();
		gbc_solarRateTextField.weightx = 1.0;
		gbc_solarRateTextField.insets = new Insets(0, 0, 5, 5);
		gbc_solarRateTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_solarRateTextField.gridx = 2;
		gbc_solarRateTextField.gridy = 1;
		panel_1.add(solarRateTextField, gbc_solarRateTextField);
		solarRateTextField.setColumns(5);

		heatingRateTextField = new JTextField();
		final GridBagConstraints gbc_heatingRateTextField = new GridBagConstraints();
		gbc_heatingRateTextField.insets = new Insets(0, 0, 5, 5);
		gbc_heatingRateTextField.weightx = 1.0;
		gbc_heatingRateTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_heatingRateTextField.gridx = 3;
		gbc_heatingRateTextField.gridy = 1;
		panel_1.add(heatingRateTextField, gbc_heatingRateTextField);
		heatingRateTextField.setEditable(false);
		heatingRateTextField.setColumns(5);

		coolingRateTextField = new JTextField();
		coolingRateTextField.setEditable(false);
		final GridBagConstraints gbc_coolingRateTextField = new GridBagConstraints();
		gbc_coolingRateTextField.insets = new Insets(0, 0, 5, 5);
		gbc_coolingRateTextField.weightx = 1.0;
		gbc_coolingRateTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_coolingRateTextField.gridx = 4;
		gbc_coolingRateTextField.gridy = 1;
		panel_1.add(coolingRateTextField, gbc_coolingRateTextField);
		coolingRateTextField.setColumns(5);

		totalRateTextField = new JTextField();
		totalRateTextField.setEditable(false);
		final GridBagConstraints gbc_totalRateTextField = new GridBagConstraints();
		gbc_totalRateTextField.insets = new Insets(0, 0, 5, 0);
		gbc_totalRateTextField.weightx = 1.0;
		gbc_totalRateTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_totalRateTextField.gridx = 5;
		gbc_totalRateTextField.gridy = 1;
		panel_1.add(totalRateTextField, gbc_totalRateTextField);
		totalRateTextField.setColumns(5);

		final JLabel todayLabel = new JLabel("Today (kWh): ");
		final GridBagConstraints gbc_todayLabel = new GridBagConstraints();
		gbc_todayLabel.insets = new Insets(0, 0, 5, 5);
		gbc_todayLabel.anchor = GridBagConstraints.WEST;
		gbc_todayLabel.gridx = 0;
		gbc_todayLabel.gridy = 2;
		panel_1.add(todayLabel, gbc_todayLabel);

		sunTodayTextField = new JTextField();
		sunTodayTextField.setEditable(false);
		final GridBagConstraints gbc_sunTodayTextField = new GridBagConstraints();
		gbc_sunTodayTextField.insets = new Insets(0, 0, 5, 5);
		gbc_sunTodayTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_sunTodayTextField.gridx = 1;
		gbc_sunTodayTextField.gridy = 2;
		panel_1.add(sunTodayTextField, gbc_sunTodayTextField);
		sunTodayTextField.setColumns(5);

		solarTodayTextField = new JTextField();
		solarTodayTextField.setEditable(false);
		final GridBagConstraints gbc_solarTodayTextField = new GridBagConstraints();
		gbc_solarTodayTextField.insets = new Insets(0, 0, 5, 5);
		gbc_solarTodayTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_solarTodayTextField.gridx = 2;
		gbc_solarTodayTextField.gridy = 2;
		panel_1.add(solarTodayTextField, gbc_solarTodayTextField);
		solarTodayTextField.setColumns(5);

		heatingTodayTextField = new JTextField();
		heatingTodayTextField.setEditable(false);
		final GridBagConstraints gbc_heatingTodayTextField = new GridBagConstraints();
		gbc_heatingTodayTextField.insets = new Insets(0, 0, 5, 5);
		gbc_heatingTodayTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_heatingTodayTextField.gridx = 3;
		gbc_heatingTodayTextField.gridy = 2;
		panel_1.add(heatingTodayTextField, gbc_heatingTodayTextField);
		heatingTodayTextField.setColumns(5);

		coolingTodayTextField = new JTextField();
		coolingTodayTextField.setEditable(false);
		final GridBagConstraints gbc_coolingTodayTextField = new GridBagConstraints();
		gbc_coolingTodayTextField.insets = new Insets(0, 0, 5, 5);
		gbc_coolingTodayTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_coolingTodayTextField.gridx = 4;
		gbc_coolingTodayTextField.gridy = 2;
		panel_1.add(coolingTodayTextField, gbc_coolingTodayTextField);
		coolingTodayTextField.setColumns(5);

		totalTodayTextField = new JTextField();
		totalTodayTextField.setEditable(false);
		final GridBagConstraints gbc_totalTodayTextField = new GridBagConstraints();
		gbc_totalTodayTextField.insets = new Insets(0, 0, 5, 0);
		gbc_totalTodayTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_totalTodayTextField.gridx = 5;
		gbc_totalTodayTextField.gridy = 2;
		panel_1.add(totalTodayTextField, gbc_totalTodayTextField);
		totalTodayTextField.setColumns(5);

		final JLabel yearlyLabel = new JLabel("Yearly (kWh): ");
		final GridBagConstraints gbc_yearlyLabel = new GridBagConstraints();
		gbc_yearlyLabel.insets = new Insets(0, 0, 5, 5);
		gbc_yearlyLabel.anchor = GridBagConstraints.WEST;
		gbc_yearlyLabel.gridx = 0;
		gbc_yearlyLabel.gridy = 3;
		panel_1.add(yearlyLabel, gbc_yearlyLabel);

		panel_1.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel_1.getPreferredSize().height));

		sunYearlyTextField = new JTextField();
		sunYearlyTextField.setEditable(false);
		final GridBagConstraints gbc_sunYearlyTextField = new GridBagConstraints();
		gbc_sunYearlyTextField.insets = new Insets(0, 0, 5, 5);
		gbc_sunYearlyTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_sunYearlyTextField.gridx = 1;
		gbc_sunYearlyTextField.gridy = 3;
		panel_1.add(sunYearlyTextField, gbc_sunYearlyTextField);
		sunYearlyTextField.setColumns(5);

		solarYearlyTextField = new JTextField();
		solarYearlyTextField.setEditable(false);
		final GridBagConstraints gbc_solarYearlyTextField = new GridBagConstraints();
		gbc_solarYearlyTextField.insets = new Insets(0, 0, 5, 5);
		gbc_solarYearlyTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_solarYearlyTextField.gridx = 2;
		gbc_solarYearlyTextField.gridy = 3;
		panel_1.add(solarYearlyTextField, gbc_solarYearlyTextField);
		solarYearlyTextField.setColumns(5);

		heatingYearlyTextField = new JTextField();
		heatingYearlyTextField.setEditable(false);
		final GridBagConstraints gbc_heatingYearlyTextField = new GridBagConstraints();
		gbc_heatingYearlyTextField.insets = new Insets(0, 0, 5, 5);
		gbc_heatingYearlyTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_heatingYearlyTextField.gridx = 3;
		gbc_heatingYearlyTextField.gridy = 3;
		panel_1.add(heatingYearlyTextField, gbc_heatingYearlyTextField);
		heatingYearlyTextField.setColumns(5);

		coolingYearlyTextField = new JTextField();
		coolingYearlyTextField.setEditable(false);
		final GridBagConstraints gbc_coolingYearlyTextField = new GridBagConstraints();
		gbc_coolingYearlyTextField.insets = new Insets(0, 0, 5, 5);
		gbc_coolingYearlyTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_coolingYearlyTextField.gridx = 4;
		gbc_coolingYearlyTextField.gridy = 3;
		panel_1.add(coolingYearlyTextField, gbc_coolingYearlyTextField);
		coolingYearlyTextField.setColumns(5);

		totalYearlyTextField = new JTextField();
		totalYearlyTextField.setEditable(false);
		final GridBagConstraints gbc_totalYearlyTextField = new GridBagConstraints();
		gbc_totalYearlyTextField.insets = new Insets(0, 0, 5, 0);
		// gbc_totalYearlyTextField.insets = new Insets(0, 0, 5, 0);
		gbc_totalYearlyTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_totalYearlyTextField.gridx = 5;
		gbc_totalYearlyTextField.gridy = 3;
		panel_1.add(totalYearlyTextField, gbc_totalYearlyTextField);
		totalYearlyTextField.setColumns(5);

		final JLabel yearlyCostLabel = new JLabel("Yearly Cost:");
		final GridBagConstraints gbc_yearlyCostLabel = new GridBagConstraints();
		gbc_yearlyCostLabel.insets = new Insets(0, 0, 0, 5);
		gbc_yearlyCostLabel.anchor = GridBagConstraints.WEST;
		gbc_yearlyCostLabel.gridx = 0;
		gbc_yearlyCostLabel.gridy = 4;
		panel_1.add(yearlyCostLabel, gbc_yearlyCostLabel);

		heatingCostTextField = new JTextField();
		heatingCostTextField.setEditable(false);
		final GridBagConstraints gbc_heatingCostTextField = new GridBagConstraints();
		gbc_heatingCostTextField.insets = new Insets(0, 0, 0, 5);
		gbc_heatingCostTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_heatingCostTextField.gridx = 3;
		gbc_heatingCostTextField.gridy = 4;
		panel_1.add(heatingCostTextField, gbc_heatingCostTextField);
		heatingCostTextField.setColumns(5);

		coolingCostTextField = new JTextField();
		coolingCostTextField.setEditable(false);
		final GridBagConstraints gbc_coolingCostTextField = new GridBagConstraints();
		gbc_coolingCostTextField.insets = new Insets(0, 0, 0, 5);
		gbc_coolingCostTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_coolingCostTextField.gridx = 4;
		gbc_coolingCostTextField.gridy = 4;
		panel_1.add(coolingCostTextField, gbc_coolingCostTextField);
		coolingCostTextField.setColumns(5);

		totalCostTextField = new JTextField();
		totalCostTextField.setEditable(false);
		final GridBagConstraints gbc_totalCostTextField = new GridBagConstraints();
		gbc_totalCostTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_totalCostTextField.gridx = 5;
		gbc_totalCostTextField.gridy = 4;
		panel_1.add(totalCostTextField, gbc_totalCostTextField);
		totalCostTextField.setColumns(5);

		final Dimension size = heatingLabel.getMinimumSize();
		sunLabel.setMinimumSize(size);
		solarLabel.setMinimumSize(size);
		coolingLabel.setMinimumSize(size);
		totalLabel.setMinimumSize(size);

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
		doorsComboBox.setModel(new DefaultComboBoxModel(new String[] { "0.00 " }));
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

		final Component verticalGlue = Box.createVerticalGlue();
		add(verticalGlue);

		JPanel target = buildingPanel;
		target.setMaximumSize(new Dimension(target.getMaximumSize().width, target.getPreferredSize().height));
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
		target = allPanel;
		target.setMaximumSize(new Dimension(target.getMaximumSize().width, target.getPreferredSize().height));

		if (Config.isRestrictMode()) {
			coolingLabel.setVisible(false);
			coolingCostTextField.setVisible(false);
			coolingRateTextField.setVisible(false);
			coolingTodayTextField.setVisible(false);
			coolingYearlyTextField.setVisible(false);

			heatingLabel.setVisible(false);
			heatingCostTextField.setVisible(false);
			heatingRateTextField.setVisible(false);
			heatingTodayTextField.setVisible(false);
			heatingYearlyTextField.setVisible(false);

			totalLabel.setVisible(false);
			totalCostTextField.setVisible(false);
			totalRateTextField.setVisible(false);
			totalTodayTextField.setVisible(false);
			totalYearlyTextField.setVisible(false);

			yearlyCostLabel.setVisible(false);

			temperaturePanel.setVisible(false);
			uFactorPanel.setVisible(false);
		}

	}

	public void initJavaFXGUI() {
		if (fxPanel == null && !initJavaFxAlreadyCalled) {
			initJavaFxAlreadyCalled = true;
			try {
				System.out.println("initJavaFXGUI()");
				fxPanel = new JFXPanel();
				final GridBagConstraints gbc_fxPanel = new GridBagConstraints();
				fxPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
				gbc_fxPanel.gridwidth = 3;
				gbc_fxPanel.fill = GridBagConstraints.BOTH;
				gbc_fxPanel.insets = new Insets(0, 0, 5, 0);
				gbc_fxPanel.gridx = 0;
				gbc_fxPanel.gridy = 1;

				add(fxPanel, gbc_fxPanel);
				initFxComponents();
			} catch (final Throwable e) {
				System.out.println("Error occured when initializing JavaFX: JavaFX is probably not supported!");
				e.printStackTrace();
			}
		}
	}

	private void initFxComponents() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				final GridPane grid = new GridPane();
				final javafx.scene.Scene scene = new javafx.scene.Scene(grid, 800, 400);
				scene.getStylesheets().add("org/concord/energy3d/gui/css/fx.css");
				final NumberAxis yAxis = new NumberAxis(0, 100, 10);
				final CategoryAxis xAxis = new CategoryAxis();
				xAxis.setCategories(FXCollections.<String> observableArrayList(Arrays.asList("Area", "Energy Loss")));
				final StackedBarChart<String, Number> chart = new StackedBarChart<String, Number>(xAxis, yAxis);
				chart.setStyle("-fx-background-color: #" + Integer.toHexString(UIManager.getColor("Panel.background").getRGB() & 0x00FFFFFF) + ";");

				XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
				series.setName("Walls");
				series.getData().add(wallsAreaChartData);
				series.getData().add(wallsEnergyChartData);
				chart.getData().add(series);

				series = new XYChart.Series<String, Number>();
				series.setName("Doors");
				series.getData().add(doorsAreaChartData);
				series.getData().add(doorsEnergyChartData);
				chart.getData().add(series);

				series = new XYChart.Series<String, Number>();
				series.setName("Windows");
				series.getData().add(windowsAreaChartData);
				series.getData().add(windowsEnergyChartData);
				chart.getData().add(series);

				series = new XYChart.Series<String, Number>();
				series.setName("Roof");
				series.getData().add(roofsAreaChartData);
				series.getData().add(roofsEnergyChartData);
				chart.getData().add(series);

				grid.add(chart, 0, 0);
				fxPanel.setScene(scene);
			}
		});
	}

	public void updateAreaChart() {
		final double total = wallsArea + doorsArea + windowsArea + roofsArea;
		final boolean isZero = (total == 0.0);
		wallsAreaChartData.setYValue(isZero ? 0 : wallsArea / total * 100.0);
		doorsAreaChartData.setYValue(isZero ? 0 : doorsArea / total * 100.0);
		windowsAreaChartData.setYValue(isZero ? 0 : windowsArea / total * 100.0);
		roofsAreaChartData.setYValue(isZero ? 0 : roofsArea / total * 100.0);
	}

	public void updateEnergyLossChart() {
		final double walls = wallsArea * wallUFactor;
		final double doors = doorsArea * doorUFactor;
		final double windows = windowsArea * windowUFactor;
		final double roofs = roofsArea * roofUFactor;
		final double total = walls + windows + doors + roofs;
		final boolean isZero = (total == 0.0);
		wallsEnergyChartData.setYValue(isZero ? 0 : walls / total * 100.0);
		doorsEnergyChartData.setYValue(isZero ? 0 : doors / total * 100.0);
		windowsEnergyChartData.setYValue(isZero ? 0 : windows / total * 100.0);
		roofsEnergyChartData.setYValue(isZero ? 0 : roofs / total * 100.0);
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

	private void computeEnergy() {
		if (autoCheckBox.isSelected())
			updateOutsideTemperature();

		try {
			wallUFactor = parseUFactor(wallsComboBox);
			doorUFactor = parseUFactor(doorsComboBox);
			windowUFactor = parseUFactor(windowsComboBox);
			roofUFactor = parseUFactor(roofsComboBox);
		} catch (final Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(MainPanel.getInstance(), "Invalid U-Factor value: " + e.getMessage(), "Invalid U-Factor", JOptionPane.WARNING_MESSAGE);
			return;
		}

		wallsArea = 0;
		doorsArea = 0;
		windowsArea = 0;
		roofsArea = 0;

		/* compute area */
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Wall)
				wallsArea += part.computeArea();
			else if (part instanceof Window)
				windowsArea += part.computeArea();
			else if (part instanceof Door)
				doorsArea += part.computeArea();
			else if (part instanceof Roof)
				roofsArea += part.computeArea();
		}
		updateAreaChart();
		updateEnergyLossChart();

		final int insideTemperature = (Integer) insideTemperatureSpinner.getValue();
		final int outsideTemperature = (Integer) outsideTemperatureSpinner.getValue();
		computeEnergyLossRate(insideTemperature - outsideTemperature);
		final EnergyAmount energyRate = computeEnergyRate(Heliodon.getInstance().getSunLocation(), insideTemperature, outsideTemperature);
		sunRateTextField.setText(noDecimals.format(energyRate.solar));
		solarRateTextField.setText(noDecimals.format(energyRate.solarPanel));
		heatingRateTextField.setText(noDecimals.format(energyRate.heating));
		coolingRateTextField.setText(noDecimals.format(energyRate.cooling));
		totalRateTextField.setText(noDecimals.format(energyRate.heating + energyRate.cooling));

		final EnergyAmount energyToday = computeEnergyToday((Calendar) Heliodon.getInstance().getCalander().clone(), (Integer) insideTemperatureSpinner.getValue());
		sunTodayTextField.setText(twoDecimals.format(energyToday.solar));
		solarTodayTextField.setText(twoDecimals.format(energyToday.solarPanel));
		heatingTodayTextField.setText(twoDecimals.format(energyToday.heating));
		coolingTodayTextField.setText(twoDecimals.format(energyToday.cooling));
		totalTodayTextField.setText(twoDecimals.format(energyToday.heating + energyToday.cooling));

		final EnergyAmount energyYearly = computeEnergyYearly((Integer) insideTemperatureSpinner.getValue());
		sunYearlyTextField.setText(noDecimals.format(energyYearly.solar));
		solarYearlyTextField.setText(noDecimals.format(energyYearly.solarPanel));
		heatingYearlyTextField.setText(noDecimals.format(energyYearly.heating));
		coolingYearlyTextField.setText(noDecimals.format(energyYearly.cooling));
		totalYearlyTextField.setText(noDecimals.format(energyYearly.heating + energyYearly.cooling));

		heatingCostTextField.setText("$" + moneyDecimals.format(COST_PER_KWH * energyYearly.heating));
		coolingCostTextField.setText("$" + moneyDecimals.format(COST_PER_KWH * energyYearly.cooling));
		totalCostTextField.setText("$" + moneyDecimals.format(COST_PER_KWH * (energyYearly.heating + energyYearly.cooling)));

		progressBar.setValue(100);
	}

	private double parseUFactor(final JComboBox comboBox) {
		final String valueStr = comboBox.getSelectedItem().toString();
		final int indexOfSpace = valueStr.indexOf(' ');
		return Double.parseDouble(valueStr.substring(0, indexOfSpace != -1 ? indexOfSpace : valueStr.length()));
	}

	private EnergyAmount computeEnergyYearly(final double insideTemperature) {
		final EnergyAmount energyYearly = new EnergyAmount();
		final Calendar date = Calendar.getInstance();
		date.set(Calendar.DAY_OF_MONTH, 15);
		date.set(Calendar.MONTH, 0);
		for (int month = 0; month < 11; month++) {
			final EnergyAmount energyToday = computeEnergyToday(date, insideTemperature);
			final int daysInMonth = date.getActualMaximum(Calendar.DAY_OF_MONTH);
			energyYearly.solar += energyToday.solar * daysInMonth;
			energyYearly.solarPanel += energyToday.solarPanel * daysInMonth;
			energyYearly.heating += energyToday.heating * daysInMonth;
			energyYearly.cooling += energyToday.cooling * daysInMonth;
			date.add(Calendar.MONTH, 1);
		}
		return energyYearly;
	}

	private EnergyAmount computeEnergyToday(final Calendar today, final double insideTemperature) {
		final EnergyAmount energyToday = new EnergyAmount();
		final Heliodon heliodon = Heliodon.getInstance();

		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.HOUR_OF_DAY, 0);

		final double[] outsideTemperature;

		if (getCity().isEmpty()) {
			/*
			 * if there are no temperatures available for the selected city compute zero for cooling and heating
			 */
			outsideTemperature = new double[] { insideTemperature, insideTemperature };
			energyToday.heating = Double.NaN;
			energyToday.cooling = Double.NaN;
		} else
			outsideTemperature = computeOutsideTemperature(today);

		for (int hour = 0; hour < 24; hour++) {
			final EnergyAmount energyThisHour = computeEnergyRate(heliodon.computeSunLocation(today), insideTemperature, outsideTemperature[0] + (outsideTemperature[1] - outsideTemperature[0]) / 24 * hour);
			energyToday.solar += energyThisHour.solar / 1000.0;
			energyToday.solarPanel += energyThisHour.solarPanel / 1000.0;
			energyToday.heating += energyThisHour.heating / 1000.0;
			energyToday.cooling += energyThisHour.cooling / 1000.0;
			today.add(Calendar.HOUR_OF_DAY, 1);
		}
		final double coolingWithSolarPanel = Math.max(0.0, energyToday.cooling - energyToday.solarPanel);
		final double heatingWithSolarPanel = energyToday.heating - energyToday.solarPanel - (energyToday.cooling - coolingWithSolarPanel);
		energyToday.cooling = coolingWithSolarPanel;
		energyToday.heating = heatingWithSolarPanel;
		return energyToday;
	}

	private double[] computeOutsideTemperature(final Calendar today) {
		final int day = today.get(Calendar.DAY_OF_MONTH);
		final int daysInMonth = today.getActualMaximum(Calendar.DAY_OF_MONTH);
		final double[] outsideTemperature = new double[2];

		final Calendar monthFrom, monthTo;
		final int halfMonth = daysInMonth / 2;
		final double portion;
		final int totalDaysOfMonth;
		if (day < halfMonth) {
			monthFrom = (Calendar) today.clone();
			monthFrom.add(Calendar.MONTH, -1);
			monthTo = today;
			final int prevHalfMonth = monthFrom.getActualMaximum(Calendar.DAY_OF_MONTH) - monthFrom.getActualMaximum(Calendar.DAY_OF_MONTH) / 2;
			totalDaysOfMonth = prevHalfMonth + daysInMonth / 2;
			portion = (double) (day + prevHalfMonth) / totalDaysOfMonth;
		} else {
			monthFrom = today;
			monthTo = (Calendar) today.clone();
			monthTo.add(Calendar.MONTH, 1);
			final int nextHalfMonth = monthTo.getActualMaximum(Calendar.DAY_OF_MONTH) / 2;
			totalDaysOfMonth = halfMonth + nextHalfMonth;
			portion = (double) (day - halfMonth) / totalDaysOfMonth;
		}

		final int[] monthlyLowTemperatures = avgMonthlyLowTemperatures.get(getCity());
		final int[] monthlyHighTemperatures = avgMonthlyHighTemperatures.get(getCity());
		final int monthFromIndex = monthFrom.get(Calendar.MONTH);
		final int monthToIndex = monthTo.get(Calendar.MONTH);
		outsideTemperature[0] = monthlyLowTemperatures[monthFromIndex] + (monthlyLowTemperatures[monthToIndex] - monthlyLowTemperatures[monthFromIndex]) * portion;
		outsideTemperature[1] = monthlyHighTemperatures[monthFromIndex] + (monthlyHighTemperatures[monthToIndex] - monthlyHighTemperatures[monthFromIndex]) * portion;
		return outsideTemperature;
	}

	public String getCity() {
		return (String) cityComboBox.getSelectedItem();
	}

	public void setCity(final String city) {
		cityComboBox.setSelectedItem(city);
	}

	private EnergyAmount computeEnergyRate(final ReadOnlyVector3 sunLocation, final double insideTemperature, final double outsideTemperature) {
		if (computeRequest)
			throw new CancellationException();
		final EnergyAmount energyRate = new EnergyAmount();

		// if (Heliodon.getInstance().isVisible() && sunLocation.getZ() > 0.0) {
		if (sunLocation.getZ() > 0.0) {
			energyRate.solar = computeEnergySolarRate(sunLocation.normalize(null));
			energyRate.solarPanel = computeEnergySolarPanelRate(sunLocation.normalize(null));
		}

		final double energyLossRate = computeEnergyLossRate(insideTemperature - outsideTemperature);
		if (energyLossRate >= 0.0) {
			energyRate.heating = energyLossRate;
			energyRate.cooling = 0.0;
		} else {
			energyRate.cooling = -energyLossRate;
			energyRate.heating = 0.0;
		}

		if (Heliodon.getInstance().isVisible()) {
			final double heatingWithSolar = Math.max(0.0, energyRate.heating - energyRate.solar);
			final double coolingWithSolar = energyRate.cooling + energyRate.solar - (energyRate.heating - heatingWithSolar);
			energyRate.heating = heatingWithSolar;
			energyRate.cooling = coolingWithSolar;
			if (outsideTemperature < insideTemperature)
				energyRate.cooling = 0;
		}
		return energyRate;
	}

	private double computeEnergyLossRate(final double deltaT) {
		final double wallsEnergyLoss = wallsArea * wallUFactor * deltaT;
		final double doorsEnergyLoss = doorsArea * doorUFactor * deltaT;
		final double windowsEnergyLoss = windowsArea * windowUFactor * deltaT;
		final double roofsEnergyLoss = roofsArea * roofUFactor * deltaT;
		return wallsEnergyLoss + doorsEnergyLoss + windowsEnergyLoss + roofsEnergyLoss;
	}

	private double computeEnergySolarRate(final ReadOnlyVector3 sunVector) {
		double totalRate = 0.0;
		for (final HousePart part : Scene.getInstance().getParts())
			if (part instanceof Window) {
				final double dot = part.getContainer().getFaceDirection().dot(sunVector);
				if (dot > 0.0)
					totalRate += 100.0 * part.computeArea() * dot;
			}
		return totalRate;
	}

	private double computeEnergySolarPanelRate(final ReadOnlyVector3 sunVector) {
		double totalRate = 0.0;
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof SolarPanel) {
				final double dot = part.getContainer().getFaceDirection().dot(sunVector);
				if (dot > 0.0)
					totalRate += 70.0 * part.computeArea() * dot;
			}
		}
		return totalRate;
	}

	private void updateOutsideTemperature() {
		if (getCity().isEmpty())
			outsideTemperatureSpinner.setValue(15);
		else {
			final double[] temperature = computeOutsideTemperature(Heliodon.getInstance().getCalander());
			final double avgTemperature = (temperature[0] + temperature[1]) / 2.0;
			outsideTemperatureSpinner.setValue((int) Math.round(avgTemperature));
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
		final TitledBorder titledBorder = (TitledBorder) partPanel.getBorder();
		if (selectedPart == null)
			titledBorder.setTitle("Part");
		else
			titledBorder.setTitle("Part - " + selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1));
		partPanel.repaint();

		if (!iradiationEnabled || selectedPart == null || selectedPart instanceof Foundation || selectedPart instanceof Door)
			partEnergyTextField.setText("");
		else
			partEnergyTextField.setText(twoDecimals.format(selectedPart.getSolarPotential()));

		if (selectedPart != null && !(selectedPart instanceof Roof || selectedPart instanceof Floor)) {
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

		final Foundation foundation;
		if (selectedPart == null)
			foundation = null;
		else if (selectedPart instanceof Foundation)
			foundation = (Foundation) selectedPart;
		else
			foundation = (Foundation) selectedPart.getTopContainer();
		if (foundation != null) {
			if (iradiationEnabled)
				houseSolarPotentialTextField.setText(twoDecimals.format(foundation.getSolarPotential()));
			else
				houseSolarPotentialTextField.setText("");
			final double[] buildingGeometry = foundation.getBuildingGeometry();
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

		if (iradiationEnabled) {
			double solarPotentialAll = 0.0;
			double passiveSolar = 0.0;
			double photovoltaic = 0.0;
			for (final HousePart part : Scene.getInstance().getParts()) {
				if (!part.isFrozen()) {
					if (part instanceof Foundation)
						solarPotentialAll += part.getSolarPotential();
					else if (part instanceof Window)
						passiveSolar += part.getSolarPotential();
					else if (part instanceof SolarPanel)
						photovoltaic += part.getSolarPotential();
				}
			}
			solarPotentialAlltextField.setText(twoDecimals.format(solarPotentialAll));
			passiveSolarTextField.setText(twoDecimals.format(passiveSolar));
			photovoltaicTextField.setText(twoDecimals.format(photovoltaic));
		} else {
			solarPotentialAlltextField.setText("");
			passiveSolarTextField.setText("");
			photovoltaicTextField.setText("");
		}
	}

	public boolean isComputeRequest() {
		return computeRequest;
	}

}
