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
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.Util;
import org.poly2tri.geometry.primitives.Point;
import org.poly2tri.transform.coordinate.AnyToXYTransform;
import org.poly2tri.transform.coordinate.XYToAnyTransform;
import org.poly2tri.triangulation.point.TPoint;

import com.ardor3d.image.Image;
import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.image.PixelDataType;
import com.ardor3d.image.Texture.MinificationFilter;
import com.ardor3d.image.Texture2D;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.TextureKey;
import com.ardor3d.util.geom.BufferUtils;

public class EnergyPanel extends JPanel {
	public static final ReadOnlyColorRGBA[] solarColors = { ColorRGBA.BLUE, ColorRGBA.GREEN, ColorRGBA.YELLOW, ColorRGBA.RED };
	public static final double SOLAR_STEP = 2.0;
	private static final int SOLAR_MINUTE_STEP = 15;
	private static final long serialVersionUID = 1L;
	private static final Map<String, Integer> cityLatitute = new HashMap<String, Integer>();
	private static final Map<String, int[]> avgMonthlyLowTemperatures = new HashMap<String, int[]>();
	private static final Map<String, int[]> avgMonthlyHighTemperatures = new HashMap<String, int[]>();
	private static final double COST_PER_KWH = 0.13;
	private static final EnergyPanel instance = new EnergyPanel();
	private static final DecimalFormat twoDecimals = new DecimalFormat();
	private static final DecimalFormat noDecimals = new DecimalFormat();
	private static final DecimalFormat moneyDecimals = new DecimalFormat();
	private static final RuntimeException cancelException = new RuntimeException("CANCEL");
	private static boolean keepHeatmapOn = false;

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
	private final JTextField solarRateTextField;
	private final JTextField solarTodayTextField;
	private final JTextField solarYearlyTextField;
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

	private final Map<Mesh, double[][]> solarOnWall = new HashMap<Mesh, double[][]>();
	private final Map<Integer, Integer> powerOfTwo = new HashMap<Integer, Integer>();
	private final Map<HousePart, Double> solarTotal = new HashMap<HousePart, Double>();
	private final Map<Mesh, Boolean> textureCoordsAlreadyComputed = new HashMap<Mesh, Boolean>();
	private final List<Spatial> solarCollidables = new ArrayList<Spatial>();
	private double[][] solarOnLand;
	private Thread thread;
	private double wallsArea;
	private double doorsArea;
	private double windowsArea;
	private double roofsArea;
	private double wallUFactor;
	private double doorUFactor;
	private double windowUFactor;
	private double roofUFactor;
	private long maxSolarValue;
	private boolean computeRequest;
	private boolean initJavaFxAlreadyCalled = false;
	private boolean alreadyRendered = false;
	private int counter = 0;

	private static class EnergyAmount {
		double solar;
		double heating;
		double cooling;
	}

	public static EnergyPanel getInstance() {
		return instance;
	}

	private EnergyPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

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
				compute();
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
					compute();
				else {
					final Integer newLatitude = cityLatitute.get(cityComboBox.getSelectedItem());
					if (newLatitude.equals(latitudeSpinner.getValue()))
						compute();
					else
						latitudeSpinner.setValue(newLatitude);
				}
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
				compute();
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
				compute();
			}
		});
		final GridBagConstraints gbc_latitudeSpinner = new GridBagConstraints();
		gbc_latitudeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_latitudeSpinner.gridx = 3;
		gbc_latitudeSpinner.gridy = 1;
		panel_3.add(latitudeSpinner, gbc_latitudeSpinner);

		panel_3.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel_3.getPreferredSize().height));

		panel_4 = new JPanel();
		panel_4.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Radiation Heat Map", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel_4);
		final GridBagLayout gbl_panel_4 = new GridBagLayout();
		panel_4.setLayout(gbl_panel_4);

		legendLabel = new JLabel("Color Scale: ");
		final GridBagConstraints gbc_legendLabel = new GridBagConstraints();
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
				if (!colorMapSlider.getValueIsAdjusting())
					compute();
			}
		});

		colormapPanel = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(final Graphics g) {
				final int STEP = 5;
				final Dimension size = getSize();
				for (int x = 0; x < size.width - STEP; x += STEP) {
					final ColorRGBA color = computeSolarColor(x, size.width);
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

		final JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Temperature \u00B0C", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel);
		final GridBagLayout gbl_panel = new GridBagLayout();
		panel.setLayout(gbl_panel);

		final JLabel insideTemperatureLabel = new JLabel("Inside: ");
		insideTemperatureLabel.setToolTipText("");
		final GridBagConstraints gbc_insideTemperatureLabel = new GridBagConstraints();
		gbc_insideTemperatureLabel.gridx = 1;
		gbc_insideTemperatureLabel.gridy = 0;
		panel.add(insideTemperatureLabel, gbc_insideTemperatureLabel);

		insideTemperatureSpinner = new JSpinner();
		insideTemperatureSpinner.setToolTipText("Thermostat temperature setting for the inside of the house");
		insideTemperatureSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				compute();
			}
		});
		insideTemperatureSpinner.setModel(new SpinnerNumberModel(20, -70, 60, 1));
		final GridBagConstraints gbc_insideTemperatureSpinner = new GridBagConstraints();
		gbc_insideTemperatureSpinner.gridx = 2;
		gbc_insideTemperatureSpinner.gridy = 0;
		panel.add(insideTemperatureSpinner, gbc_insideTemperatureSpinner);

		final JLabel outsideTemperatureLabel = new JLabel(" Outside: ");
		outsideTemperatureLabel.setToolTipText("");
		final GridBagConstraints gbc_outsideTemperatureLabel = new GridBagConstraints();
		gbc_outsideTemperatureLabel.gridx = 3;
		gbc_outsideTemperatureLabel.gridy = 0;
		panel.add(outsideTemperatureLabel, gbc_outsideTemperatureLabel);

		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));

		outsideTemperatureSpinner = new JSpinner();
		outsideTemperatureSpinner.setToolTipText("Outside temperature at this time and day");
		outsideTemperatureSpinner.setEnabled(false);
		outsideTemperatureSpinner.setModel(new SpinnerNumberModel(10, -70, 60, 1));
		outsideTemperatureSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (thread == null)
					compute();
			}
		});
		final GridBagConstraints gbc_outsideTemperatureSpinner = new GridBagConstraints();
		gbc_outsideTemperatureSpinner.gridx = 4;
		gbc_outsideTemperatureSpinner.gridy = 0;
		panel.add(outsideTemperatureSpinner, gbc_outsideTemperatureSpinner);

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
				compute();
			}
		});
		final GridBagConstraints gbc_autoCheckBox = new GridBagConstraints();
		gbc_autoCheckBox.gridx = 5;
		gbc_autoCheckBox.gridy = 0;
		panel.add(autoCheckBox, gbc_autoCheckBox);

		final JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Energy", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel_1);
		final GridBagLayout gbl_panel_1 = new GridBagLayout();
		panel_1.setLayout(gbl_panel_1);

		final JLabel solarLabel = new JLabel("Solar");
		solarLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_solarLabel = new GridBagConstraints();
		gbc_solarLabel.gridx = 1;
		gbc_solarLabel.gridy = 0;
		panel_1.add(solarLabel, gbc_solarLabel);

		final JLabel heatingLabel = new JLabel("Heating");
		heatingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_heatingLabel = new GridBagConstraints();
		gbc_heatingLabel.gridx = 2;
		gbc_heatingLabel.gridy = 0;
		panel_1.add(heatingLabel, gbc_heatingLabel);

		final JLabel coolingLabel = new JLabel("Cooling");
		coolingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_coolingLabel = new GridBagConstraints();
		gbc_coolingLabel.gridx = 3;
		gbc_coolingLabel.gridy = 0;
		panel_1.add(coolingLabel, gbc_coolingLabel);

		final JLabel totalLabel = new JLabel("Total");
		totalLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_totalLabel = new GridBagConstraints();
		gbc_totalLabel.gridx = 4;
		gbc_totalLabel.gridy = 0;
		panel_1.add(totalLabel, gbc_totalLabel);

		final JLabel nowLabel = new JLabel("Now (watts):");
		final GridBagConstraints gbc_nowLabel = new GridBagConstraints();
		gbc_nowLabel.anchor = GridBagConstraints.WEST;
		gbc_nowLabel.gridx = 0;
		gbc_nowLabel.gridy = 1;
		panel_1.add(nowLabel, gbc_nowLabel);

		solarRateTextField = new JTextField();
		solarRateTextField.setEditable(false);
		final GridBagConstraints gbc_solarRateTextField = new GridBagConstraints();
		gbc_solarRateTextField.weightx = 1.0;
		gbc_solarRateTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_solarRateTextField.gridx = 1;
		gbc_solarRateTextField.gridy = 1;
		solarRateTextField.setColumns(5);
		panel_1.add(solarRateTextField, gbc_solarRateTextField);

		heatingRateTextField = new JTextField();
		final GridBagConstraints gbc_heatingRateTextField = new GridBagConstraints();
		gbc_heatingRateTextField.weightx = 1.0;
		gbc_heatingRateTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_heatingRateTextField.gridx = 2;
		gbc_heatingRateTextField.gridy = 1;
		panel_1.add(heatingRateTextField, gbc_heatingRateTextField);
		heatingRateTextField.setEditable(false);
		heatingRateTextField.setColumns(5);

		coolingRateTextField = new JTextField();
		coolingRateTextField.setEditable(false);
		final GridBagConstraints gbc_coolingRateTextField = new GridBagConstraints();
		gbc_coolingRateTextField.weightx = 1.0;
		gbc_coolingRateTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_coolingRateTextField.gridx = 3;
		gbc_coolingRateTextField.gridy = 1;
		panel_1.add(coolingRateTextField, gbc_coolingRateTextField);
		coolingRateTextField.setColumns(5);

		totalRateTextField = new JTextField();
		totalRateTextField.setEditable(false);
		final GridBagConstraints gbc_totalRateTextField = new GridBagConstraints();
		gbc_totalRateTextField.weightx = 1.0;
		gbc_totalRateTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_totalRateTextField.gridx = 4;
		gbc_totalRateTextField.gridy = 1;
		panel_1.add(totalRateTextField, gbc_totalRateTextField);
		totalRateTextField.setColumns(5);

		final JLabel todayLabel = new JLabel("Today (kWh): ");
		final GridBagConstraints gbc_todayLabel = new GridBagConstraints();
		gbc_todayLabel.anchor = GridBagConstraints.WEST;
		gbc_todayLabel.gridx = 0;
		gbc_todayLabel.gridy = 2;
		panel_1.add(todayLabel, gbc_todayLabel);

		solarTodayTextField = new JTextField();
		solarTodayTextField.setEditable(false);
		final GridBagConstraints gbc_solarTodayTextField = new GridBagConstraints();
		gbc_solarTodayTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_solarTodayTextField.gridx = 1;
		gbc_solarTodayTextField.gridy = 2;
		panel_1.add(solarTodayTextField, gbc_solarTodayTextField);
		solarTodayTextField.setColumns(5);

		heatingTodayTextField = new JTextField();
		heatingTodayTextField.setEditable(false);
		final GridBagConstraints gbc_heatingTodayTextField = new GridBagConstraints();
		gbc_heatingTodayTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_heatingTodayTextField.gridx = 2;
		gbc_heatingTodayTextField.gridy = 2;
		panel_1.add(heatingTodayTextField, gbc_heatingTodayTextField);
		heatingTodayTextField.setColumns(5);

		coolingTodayTextField = new JTextField();
		coolingTodayTextField.setEditable(false);
		final GridBagConstraints gbc_coolingTodayTextField = new GridBagConstraints();
		gbc_coolingTodayTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_coolingTodayTextField.gridx = 3;
		gbc_coolingTodayTextField.gridy = 2;
		panel_1.add(coolingTodayTextField, gbc_coolingTodayTextField);
		coolingTodayTextField.setColumns(5);

		totalTodayTextField = new JTextField();
		totalTodayTextField.setEditable(false);
		final GridBagConstraints gbc_totalTodayTextField = new GridBagConstraints();
		gbc_totalTodayTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_totalTodayTextField.gridx = 4;
		gbc_totalTodayTextField.gridy = 2;
		panel_1.add(totalTodayTextField, gbc_totalTodayTextField);
		totalTodayTextField.setColumns(5);

		final JLabel yearlyLabel = new JLabel("Yearly (kWh): ");
		final GridBagConstraints gbc_yearlyLabel = new GridBagConstraints();
		gbc_yearlyLabel.anchor = GridBagConstraints.WEST;
		gbc_yearlyLabel.gridx = 0;
		gbc_yearlyLabel.gridy = 3;
		panel_1.add(yearlyLabel, gbc_yearlyLabel);

		panel_1.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel_1.getPreferredSize().height));

		solarYearlyTextField = new JTextField();
		solarYearlyTextField.setEditable(false);
		final GridBagConstraints gbc_solarYearlyTextField = new GridBagConstraints();
		gbc_solarYearlyTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_solarYearlyTextField.gridx = 1;
		gbc_solarYearlyTextField.gridy = 3;
		panel_1.add(solarYearlyTextField, gbc_solarYearlyTextField);
		solarYearlyTextField.setColumns(5);

		heatingYearlyTextField = new JTextField();
		heatingYearlyTextField.setEditable(false);
		final GridBagConstraints gbc_heatingYearlyTextField = new GridBagConstraints();
		gbc_heatingYearlyTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_heatingYearlyTextField.gridx = 2;
		gbc_heatingYearlyTextField.gridy = 3;
		panel_1.add(heatingYearlyTextField, gbc_heatingYearlyTextField);
		heatingYearlyTextField.setColumns(5);

		coolingYearlyTextField = new JTextField();
		coolingYearlyTextField.setEditable(false);
		final GridBagConstraints gbc_coolingYearlyTextField = new GridBagConstraints();
		gbc_coolingYearlyTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_coolingYearlyTextField.gridx = 3;
		gbc_coolingYearlyTextField.gridy = 3;
		panel_1.add(coolingYearlyTextField, gbc_coolingYearlyTextField);
		coolingYearlyTextField.setColumns(5);

		totalYearlyTextField = new JTextField();
		totalYearlyTextField.setEditable(false);
		final GridBagConstraints gbc_totalYearlyTextField = new GridBagConstraints();
		// gbc_totalYearlyTextField.insets = new Insets(0, 0, 5, 0);
		gbc_totalYearlyTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_totalYearlyTextField.gridx = 4;
		gbc_totalYearlyTextField.gridy = 3;
		panel_1.add(totalYearlyTextField, gbc_totalYearlyTextField);
		totalYearlyTextField.setColumns(5);

		final JLabel yearlyCostLabel = new JLabel("Yearly Cost:");
		final GridBagConstraints gbc_yearlyCostLabel = new GridBagConstraints();
		gbc_yearlyCostLabel.anchor = GridBagConstraints.WEST;
		gbc_yearlyCostLabel.gridx = 0;
		gbc_yearlyCostLabel.gridy = 4;
		panel_1.add(yearlyCostLabel, gbc_yearlyCostLabel);

		heatingCostTextField = new JTextField();
		heatingCostTextField.setEditable(false);
		final GridBagConstraints gbc_heatingCostTextField = new GridBagConstraints();
		gbc_heatingCostTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_heatingCostTextField.gridx = 2;
		gbc_heatingCostTextField.gridy = 4;
		panel_1.add(heatingCostTextField, gbc_heatingCostTextField);
		heatingCostTextField.setColumns(5);

		coolingCostTextField = new JTextField();
		coolingCostTextField.setEditable(false);
		final GridBagConstraints gbc_coolingCostTextField = new GridBagConstraints();
		gbc_coolingCostTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_coolingCostTextField.gridx = 3;
		gbc_coolingCostTextField.gridy = 4;
		panel_1.add(coolingCostTextField, gbc_coolingCostTextField);
		coolingCostTextField.setColumns(5);

		totalCostTextField = new JTextField();
		totalCostTextField.setEditable(false);
		final GridBagConstraints gbc_totalCostTextField = new GridBagConstraints();
		gbc_totalCostTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_totalCostTextField.gridx = 4;
		gbc_totalCostTextField.gridy = 4;
		panel_1.add(totalCostTextField, gbc_totalCostTextField);
		totalCostTextField.setColumns(5);

		final Dimension size = heatingLabel.getMinimumSize();
		coolingLabel.setMinimumSize(size);
		solarLabel.setMinimumSize(size);
		totalLabel.setMinimumSize(size);

		final JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "U-Factor W/(m\u00B2.\u00B0C)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel_2);
		final GridBagLayout gbl_panel_2 = new GridBagLayout();
		panel_2.setLayout(gbl_panel_2);

		final JLabel wallsLabel = new JLabel("Walls:");
		final GridBagConstraints gbc_wallsLabel = new GridBagConstraints();
		gbc_wallsLabel.anchor = GridBagConstraints.EAST;
		gbc_wallsLabel.insets = new Insets(0, 0, 5, 5);
		gbc_wallsLabel.gridx = 0;
		gbc_wallsLabel.gridy = 0;
		panel_2.add(wallsLabel, gbc_wallsLabel);

		wallsComboBox = new WideComboBox();
		wallsComboBox.setEditable(true);
		wallsComboBox.setModel(new DefaultComboBoxModel(new String[] { "0.28 ", "0.67 (Concrete 8\")", "0.41 (Masonary Brick 8\")", "0.04 (Flat Metal 8\" Fiberglass Insulation)" }));
		wallsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				compute();
			}
		});
		final GridBagConstraints gbc_wallsComboBox = new GridBagConstraints();
		gbc_wallsComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_wallsComboBox.gridx = 1;
		gbc_wallsComboBox.gridy = 0;
		panel_2.add(wallsComboBox, gbc_wallsComboBox);

		final JLabel doorsLabel = new JLabel("Doors:");
		final GridBagConstraints gbc_doorsLabel = new GridBagConstraints();
		gbc_doorsLabel.anchor = GridBagConstraints.EAST;
		gbc_doorsLabel.insets = new Insets(0, 0, 5, 5);
		gbc_doorsLabel.gridx = 2;
		gbc_doorsLabel.gridy = 0;
		panel_2.add(doorsLabel, gbc_doorsLabel);

		doorsComboBox = new WideComboBox();
		doorsComboBox.setEditable(true);
		doorsComboBox.setModel(new DefaultComboBoxModel(new String[] { "0.00 " }));
		doorsComboBox.setModel(new DefaultComboBoxModel(new String[] { "1.14 ", "1.20 (Steel)", "0.64 (Wood)" }));
		doorsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				compute();
			}
		});
		final GridBagConstraints gbc_doorsComboBox = new GridBagConstraints();
		gbc_doorsComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_doorsComboBox.gridx = 3;
		gbc_doorsComboBox.gridy = 0;
		panel_2.add(doorsComboBox, gbc_doorsComboBox);

		final JLabel windowsLabel = new JLabel("Windows:");
		final GridBagConstraints gbc_windowsLabel = new GridBagConstraints();
		gbc_windowsLabel.anchor = GridBagConstraints.EAST;
		gbc_windowsLabel.insets = new Insets(0, 0, 0, 5);
		gbc_windowsLabel.gridx = 0;
		gbc_windowsLabel.gridy = 1;
		panel_2.add(windowsLabel, gbc_windowsLabel);

		windowsComboBox = new WideComboBox();
		windowsComboBox.setEditable(true);
		windowsComboBox.setModel(new DefaultComboBoxModel(new String[] { "1.89 ", "1.22 (Single Pane)", "0.70 (Double Pane)" }));
		windowsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				compute();
			}
		});
		final GridBagConstraints gbc_windowsComboBox = new GridBagConstraints();
		gbc_windowsComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_windowsComboBox.gridx = 1;
		gbc_windowsComboBox.gridy = 1;
		panel_2.add(windowsComboBox, gbc_windowsComboBox);

		final JLabel roofsLabel = new JLabel("Roofs:");
		final GridBagConstraints gbc_roofsLabel = new GridBagConstraints();
		gbc_roofsLabel.anchor = GridBagConstraints.EAST;
		gbc_roofsLabel.insets = new Insets(0, 0, 0, 5);
		gbc_roofsLabel.gridx = 2;
		gbc_roofsLabel.gridy = 1;
		panel_2.add(roofsLabel, gbc_roofsLabel);

		roofsComboBox = new WideComboBox();
		roofsComboBox.setEditable(true);
		roofsComboBox.setModel(new DefaultComboBoxModel(new String[] { "0.14 ", "0.23 (Concrete 3\")", "0.11 (Flat Metal 3\" Fiberglass Insulation)", "0.10 (Wood 3\" Fiberglass Insulation)" }));
		roofsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				compute();
			}
		});
		final GridBagConstraints gbc_roofsComboBox = new GridBagConstraints();
		gbc_roofsComboBox.gridx = 3;
		gbc_roofsComboBox.gridy = 1;
		panel_2.add(roofsComboBox, gbc_roofsComboBox);

		panel_2.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel_2.getPreferredSize().height));

		final Component verticalGlue = Box.createVerticalGlue();
		add(verticalGlue);
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

	public void compute() {
		if (thread != null) {
			computeRequest = true;
		} else {
			thread = new Thread() {
				@Override
				public void run() {
					do {
						computeRequest = false;
						try {
							computeNow();
							try {
								Thread.sleep(500);
							} catch (final InterruptedException e) {
								e.printStackTrace();
							}
							progressBar.setValue(0);
						} catch (final RuntimeException e) {
							if (e != cancelException)
								e.printStackTrace();
						}
					} while (computeRequest);
					thread = null;
				}
			};
			thread.start();
		}
	}

	private void computeNow() {
		System.out.println("computeEnergyNow()");

		progressBar.setValue(0);

		if (SceneManager.getInstance().isSolarColorMap() && (!alreadyRendered || keepHeatmapOn)) {
			alreadyRendered = true;
			computeRadiation();
		} else {
			if (SceneManager.getInstance().isSolarColorMap()) {
				MainPanel.getInstance().getSolarButton().setSelected(false);
			}
			int counter = 0;
			for (final HousePart part : Scene.getInstance().getParts()) {
				if (part instanceof Foundation && !part.getChildren().isEmpty() && !part.isFrozen())
					counter++;
				if (counter >= 2)
					break;
			}
			for (final HousePart part : Scene.getInstance().getParts())
				if (part instanceof Foundation)
					((Foundation) part).setSolarValue(counter >= 2 && !part.getChildren().isEmpty() && !part.isFrozen() ? 0 : -1);
			SceneManager.getInstance().refresh();
		}

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
		solarRateTextField.setText(noDecimals.format(energyRate.solar));
		heatingRateTextField.setText(noDecimals.format(energyRate.heating));
		coolingRateTextField.setText(noDecimals.format(energyRate.cooling));
		totalRateTextField.setText(noDecimals.format(energyRate.heating + energyRate.cooling));

		final EnergyAmount energyToday = computeEnergyToday((Calendar) Heliodon.getInstance().getCalander().clone(), (Integer) insideTemperatureSpinner.getValue());
		solarTodayTextField.setText(twoDecimals.format(energyToday.solar));
		heatingTodayTextField.setText(twoDecimals.format(energyToday.heating));
		coolingTodayTextField.setText(twoDecimals.format(energyToday.cooling));
		totalTodayTextField.setText(twoDecimals.format(energyToday.heating + energyToday.cooling));

		final EnergyAmount energyYearly = computeEnergyYearly((Integer) insideTemperatureSpinner.getValue());
		solarYearlyTextField.setText(noDecimals.format(energyYearly.solar));
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
			/* if there are no temperatures available for the selected city compute zero for cooling and heating */
			outsideTemperature = new double[] { insideTemperature, insideTemperature };
			energyToday.heating = Double.NaN;
			energyToday.cooling = Double.NaN;
		} else
			outsideTemperature = computeOutsideTemperature(today);

		for (int hour = 0; hour < 24; hour++) {
			final EnergyAmount energyThisHour = computeEnergyRate(heliodon.computeSunLocation(today), insideTemperature, outsideTemperature[0] + (outsideTemperature[1] - outsideTemperature[0]) / 24 * hour);
			energyToday.solar += energyThisHour.solar / 1000.0;
			energyToday.heating += energyThisHour.heating / 1000.0;
			energyToday.cooling += energyThisHour.cooling / 1000.0;
			today.add(Calendar.HOUR_OF_DAY, 1);
		}
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
			throw cancelException;
		final EnergyAmount energyRate = new EnergyAmount();

		if (Heliodon.getInstance().isVisible() && !Heliodon.getInstance().isNightTime())
			energyRate.solar = computeEnergySolarRate(sunLocation.normalize(null));

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
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Window) {
				final double dot = part.getContainer().getFaceDirection().dot(sunVector);
				if (dot > 0.0)
					totalRate += 100.0 * part.computeArea() * dot;
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

	private void computeRadiation() {
		System.out.println("computeRadiation()");
		initSolarCollidables();
		solarOnWall.clear();
		solarTotal.clear();
		textureCoordsAlreadyComputed.clear();
		solarOnLand = null;
		maxSolarValue = 1;
		counter = 0;
		// computeSolarOnLand(Heliodon.getInstance().getSunLocation());
//		computeRadiationOnWalls(Heliodon.getInstance().getSunLocation());
//		computeRadiationOnRoofs(Heliodon.getInstance().getSunLocation());
		computeRadiationToday((Calendar) Heliodon.getInstance().getCalander().clone());
		updateSolarValueOnAllHouses();
		System.out.println("COUNTER = " + counter);
	}

	private void initSolarCollidables() {
		solarCollidables.clear();
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Wall)
				solarCollidables.add(((Wall) part).getInvisibleMesh());
			else if (part instanceof Roof)
				solarCollidables.add(((Roof) part).getRoofPartsRoot());
		}
	}

	// private void computeRadiationOnWallsOrg(final ReadOnlyVector3 sunLocation) {
	// if (sunLocation.getZ() <= 0)
	// return;
	// final ReadOnlyVector3 directionTowardSun = sunLocation.normalize(null);
	// /* needed in order to prevent picking collision with neighboring wall at wall edge */
	// final double OFFSET = 0.1;
	// final ReadOnlyVector3 offset = directionTowardSun.multiply(OFFSET, null);
	// final ArrayList<HousePart> parts = Scene.getInstance().getParts();
	// for (int i = 0; i < parts.size(); i++) {
	// final HousePart part = parts.get(i);
	// if (part instanceof Wall && part.isDrawCompleted() && part.getFaceDirection().dot(directionTowardSun) > 0) {
	// final Wall wall = (Wall) part;
	// final int rows = (int) Math.ceil(Math.round(wall.getHighestPoint()) / SOLAR_STEP);
	// final ReadOnlyVector3 origin = part.getAbsPoint(0);
	// final ReadOnlyVector3 p2 = wall.getAbsPoint(2);
	// final int cols = (int) Math.ceil(p2.subtract(origin, null).length() / SOLAR_STEP);
	// double[][] solar = solarOnWall.get(wall);
	// if (solar == null) {
	// solar = new double[roundToPowerOfTwo(rows)][roundToPowerOfTwo(cols)];
	// solarOnWall.put(wall, solar);
	// }
	//
	// final double baseZ = origin.getZ();
	// final ReadOnlyVector3 dir = p2.subtract(origin, null).normalizeLocal();
	// final Vector3 p = new Vector3();
	// final double dot = part.getFaceDirection().dot(directionTowardSun);
	// for (int col = 1; col < cols - 1; col++) {
	// p.set(dir).multiplyLocal(col * SOLAR_STEP).addLocal(origin);
	// final double w;
	// if (col == cols - 1)
	// w = p2.distance(p);
	// else
	// w = SOLAR_STEP;
	// for (int row = 0; row < rows; row++) {
	// if (computeRequest)
	// throw cancelException;
	// p.setZ(baseZ + row * SOLAR_STEP);
	// final double h;
	// if (row == rows - 1)
	// h = wall.getHighestPoint() - (row * SOLAR_STEP);
	// else
	// h = SOLAR_STEP;
	// final Ray3 pickRay = new Ray3(p.add(offset, null), directionTowardSun);
	// final PickResults pickResults = new PrimitivePickResults();
	// for (final Spatial spatial : solarCollidables)
	// if (spatial != wall.getInvisibleMesh()) {
	// PickingUtil.findPick(spatial, pickRay, pickResults, false);
	// }
	// if (pickResults.getNumber() == 0) {
	// solar[row][col] += dot;
	// int repeat = 1;
	// if (col == 1) {
	// solar[row][0] += dot;
	// repeat++;
	// } else if (col == cols - 2) {
	// solar[row][cols - 1] += dot;
	// repeat++;
	// }
	// final HousePart house = wall.getContainer();
	// final Double val = solarTotal.get(house);
	// solarTotal.put(house, val == null ? 0 : val + repeat * dot * w * h * Scene.getInstance().getAnnotationScale());
	// }
	// }
	// }
	// if (rows < solar.length)
	// for (int col = 0; col < solar[0].length; col++) {
	// solar[solar.length - 1][col] = solar[0][col];
	// if (rows != solar.length - 1)
	// solar[rows][col] = solar[0][col];
	// }
	//
	// if (cols < solar[0].length)
	// for (int row = 0; row < solar.length; row++) {
	// solar[row][solar[0].length - 1] = solar[row][0];
	// if (cols != solar[0].length - 1)
	// solar[row][cols] = solar[row][0];
	// }
	// }
	// }
	// }

	private void computeRadiationOnRoofs(final ReadOnlyVector3 sunLocation) {
		if (sunLocation.getZ() <= 0)
			return;
		final ReadOnlyVector3 directionTowardSun = sunLocation.normalize(null);
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Roof && part.isDrawCompleted()) { // && faceDirection.dot(directionTowardSun) > 0) {
				final Roof roof = (Roof) part;
				for (final Spatial roofPart : roof.getRoofPartsRoot().getChildren()) {
					final ReadOnlyVector3 faceDirection = (ReadOnlyVector3) roofPart.getUserData();
					if (faceDirection.dot(directionTowardSun) > 0) {
						final Mesh mesh = (Mesh) ((Node) roofPart).getChild(0);
						final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
						ReadOnlyVector3 lowestPoint, secondLowestPoint, highestPoint, secondHighestPoint;
						lowestPoint = secondLowestPoint = highestPoint = secondHighestPoint = null;
						vertexBuffer.rewind();
						final int index = faceDirection.equals(Vector3.UNIT_Z) ? 1 : 2;
						while (vertexBuffer.hasRemaining()) {
							final ReadOnlyVector3 p = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
							if (p.equals(lowestPoint) || p.equals(secondLowestPoint) || p.equals(highestPoint) || p.equals(secondHighestPoint))
								continue;
							if (lowestPoint == null || p.getValue(index) < lowestPoint.getValue(index)) {
								secondLowestPoint = lowestPoint;
								lowestPoint = p;
							} else if (secondLowestPoint == null || p.getValue(index) < secondLowestPoint.getValue(index))
								secondLowestPoint = p;
							if (highestPoint == null || p.getValue(index) > highestPoint.getValue(index)) {
								secondHighestPoint = highestPoint;
								highestPoint = p;
							} else if (secondHighestPoint == null || p.getValue(index) > secondHighestPoint.getValue(index))
								secondHighestPoint = p;
						}

						if (lowestPoint.getX() == secondLowestPoint.getX() && lowestPoint.getX() == highestPoint.getX()) {
							final ReadOnlyVector3 tmp = highestPoint;
							highestPoint = secondHighestPoint;
							secondHighestPoint = tmp;
						}

						// else
						// while (vertexBuffer.hasRemaining()) {
						// final ReadOnlyVector3 p = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
						// if (lowestPoint == null || p.getZ() < lowestPoint.getZ()) {
						// secondLowestPoint = lowestPoint;
						// lowestPoint = p;
						// } else if (secondLowestPoint == null || p.getZ() < secondLowestPoint.getZ())
						// secondLowestPoint = p;
						// if (highestPoint == null || p.get.get(Z() > highestPoint.getZ()) {
						// secondHighestPoint = highestPoint;
						// highestPoint = p;
						// } else if (secondHighestPoint == null || p.getZ() > secondHighestPoint.getZ())
						// secondHighestPoint = p;
						// }
						// final double z = Util.distanceFromPointToLine(highestPoint, lowestPoint, secondLowestPoint.subtract(lowestPoint, null).normalizeLocal());
						final ReadOnlyVector3 o = lowestPoint;
						final ReadOnlyVector3 u;
						if (lowestPoint.getValue(index) != secondLowestPoint.getValue(index) && highestPoint.getValue(index) == secondHighestPoint.getValue(index))
							u = secondHighestPoint.subtract(highestPoint, null);
						else
							u = secondLowestPoint.subtract(lowestPoint, null);
						final ReadOnlyVector3 v = u.cross(faceDirection, null);

						// final ReadOnlyVector3 projectedHighestPoint = Util.closestPoint(o, v, highestPoint, u);
						final ReadOnlyVector3 projectedHighestPoint = Util.closestPointBetweenTwoLines(lowestPoint, v, highestPoint, u);
						// final ReadOnlyVector3 p1 = lowestPoint.multiply(1, 1, 0, null).addLocal(0, 0, z);
						// final ReadOnlyVector3 o = lowestPoint;
						// final ReadOnlyVector3 u = secondLowestPoint.subtract(o, null);
						// final ReadOnlyVector3 v = p1.subtract(o, null);
						// final FloatBuffer textureBuffer = mesh.getMeshData().getTextureBuffer(0);
						// textureBuffer.rewind();
						// vertexBuffer.rewind();
						// while (vertexBuffer.hasRemaining()) {
						// final ReadOnlyVector3 p = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
						// System.out.println(p);
						// final Vector3 uP = Util.closestPoint(o, u, p, v.negate(null));
						// final float uScale = (float) (uP.distance(o) / u.length());
						// final Vector3 vP = Util.closestPoint(o, v, p, u.negate(null));
						// final float vScale = (float) (vP.distance(o) / v.length());
						// textureBuffer.put(uScale).put(vScale);
						// }
						// computeRadiationOnMesh(directionTowardSun, roof.getContainer(), mesh, mesh, faceDirection, lowestPoint, projectedHighestPoint, secondLowestPoint, false);
						computeRadiationOnMesh(directionTowardSun, roof.getContainer(), mesh, mesh, faceDirection, false);
					}
				}
			}
		}
	}

	private void computeRadiationOnWalls(final ReadOnlyVector3 sunLocation) {
		if (sunLocation.getZ() <= 0)
			return;
		final ReadOnlyVector3 directionTowardSun = sunLocation.normalize(null);
		// /* needed in order to prevent picking collision with neighboring wall at wall edge */
		// final double OFFSET = 0.1;
		// final ReadOnlyVector3 offset = directionTowardSun.multiply(OFFSET, null);
		// final ArrayList<HousePart> parts = Scene.getInstance().getParts();
		// for (int i = 0; i < parts.size(); i++) {
		// final HousePart part = parts.get(i);
		for (final HousePart part : Scene.getInstance().getParts()) {
			final ReadOnlyVector3 faceDirection = part.getFaceDirection();
			if (part instanceof Wall && part.isDrawCompleted() && faceDirection.dot(directionTowardSun) > 0) {
				final Wall wall = (Wall) part;
				// final int rows = (int) Math.ceil(Math.round(wall.getHighestPoint()) / SOLAR_STEP);
				// final ReadOnlyVector3 p1 = wall.getAbsPoint(1);
				final ReadOnlyVector3 p1 = wall.getAbsPoint(1).multiplyLocal(1, 1, 0).addLocal(0, 0, wall.getHighestPoint());

				// computeRadiationOnMesh(directionTowardSun, wall.getContainer(), wall.getMesh(), wall.getInvisibleMesh(), faceDirection, part.getAbsPoint(0), p1, wall.getAbsPoint(2), true);
				computeRadiationOnMesh(directionTowardSun, wall.getContainer(), wall.getMesh(), wall.getInvisibleMesh(), faceDirection, true);
			}
		}
	}

	// private void computeRadiationOnMesh(final ReadOnlyVector3 directionTowardSun, final HousePart house, final Mesh drawMesh, final Mesh collisionMesh, final ReadOnlyVector3 faceDirection, final ReadOnlyVector3 origin, final ReadOnlyVector3 p1, final ReadOnlyVector3 p2, final boolean addToTotal) {
	// /* needed in order to prevent picking collision with neighboring wall at wall edge */
	// final double OFFSET = 0.1;
	// final ReadOnlyVector3 offset = directionTowardSun.multiply(OFFSET, null);
	//
	// final int rows = (int) Math.ceil(p1.subtract(origin, null).length() / SOLAR_STEP);
	// final int cols = (int) Math.ceil(p2.subtract(origin, null).length() / SOLAR_STEP);
	// // double[][] solar = solarOnWall.get(wall);
	// double[][] solar = solarOnWall.get(drawMesh);
	// if (solar == null) {
	// solar = new double[roundToPowerOfTwo(rows)][roundToPowerOfTwo(cols)];
	// // solarOnWall.put(wall, solar);
	// solarOnWall.put(drawMesh, solar);
	// }
	//
	// // final double baseZ = origin.getZ();
	// final ReadOnlyVector3 u = p2.subtract(origin, null).normalizeLocal();
	// final ReadOnlyVector3 v = p1.subtract(origin, null).normalizeLocal();
	// final double dot = faceDirection.dot(directionTowardSun);
	// // for (int col = 1; col < cols - 1; col++) {
	// for (int col = 0; col < cols; col++) {
	// // p.set(u).multiplyLocal(col * SOLAR_STEP).addLocal(origin);
	// final ReadOnlyVector3 pU = u.multiply(col * SOLAR_STEP, null).addLocal(origin);
	// final double w;
	// if (col == cols - 1)
	// w = p2.distance(pU);
	// else
	// w = SOLAR_STEP;
	// for (int row = 0; row < rows; row++) {
	// if (computeRequest)
	// throw cancelException;
	// // p.setZ(baseZ + row * SOLAR_STEP);
	// // p.addLocal(v.multiply(row * SOLAR_STEP, null));
	// final ReadOnlyVector3 p = v.multiply(row * SOLAR_STEP, null).addLocal(pU);
	// final double h;
	// if (row == rows - 1)
	// // h = wall.getHighestPoint() - (row * SOLAR_STEP);
	// h = p1.getZ() - (row * SOLAR_STEP);
	// else
	// h = SOLAR_STEP;
	// final Ray3 pickRay = new Ray3(p.add(offset, null), directionTowardSun);
	// final PickResults pickResults = new PrimitivePickResults();
	// for (final Spatial spatial : solarCollidables)
	// if (spatial != collisionMesh) {
	// PickingUtil.findPick(spatial, pickRay, pickResults, false);
	// if (pickResults.getNumber() != 0)
	// break;
	// }
	// if (pickResults.getNumber() == 0) {
	// solar[row][col] += dot;
	// final int repeat = 1;
	// // if (col == 1) {
	// // solar[row][0] += dot;
	// // repeat++;
	// // } else if (col == cols - 2) {
	// // solar[row][cols - 1] += dot;
	// // repeat++;
	// // }
	// if (addToTotal) {
	// final Double val = solarTotal.get(house);
	// solarTotal.put(house, val == null ? 0 : val + repeat * dot * w * h * Scene.getInstance().getAnnotationScale());
	// }
	// }
	// }
	// }
	// if (rows < solar.length)
	// for (int col = 0; col < solar[0].length; col++) {
	// solar[solar.length - 1][col] = solar[0][col];
	// if (rows != solar.length - 1)
	// solar[rows][col] = solar[rows - 1][col];
	// }
	//
	// if (cols < solar[0].length)
	// for (int row = 0; row < solar.length; row++) {
	// solar[row][solar[0].length - 1] = solar[row][0];
	// if (cols != solar[0].length - 1)
	// solar[row][cols] = solar[row][cols - 1];
	// }
	//
	// updateRadiationMeshTextureCoords(drawMesh, origin, u, v, rows, cols);
	// }

	private void computeRadiationOnMesh(final ReadOnlyVector3 directionTowardSun, final HousePart house, final Mesh drawMesh, final Mesh collisionMesh, final ReadOnlyVector3 normal, final boolean addToTotal) {
		/* needed in order to prevent picking collision with neighboring wall at wall edge */
		final double OFFSET = 0.1;
		final ReadOnlyVector3 offset = directionTowardSun.multiply(OFFSET, null);

		final AnyToXYTransform toXY = new AnyToXYTransform(normal.getX(), normal.getY(), normal.getZ());
		final XYToAnyTransform fromXY = new XYToAnyTransform(normal.getX(), normal.getY(), normal.getZ());

		final FloatBuffer vertexBuffer = drawMesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		double minX, minY, maxX, maxY;
		minX = minY = Double.POSITIVE_INFINITY;
		maxX = maxY = Double.NEGATIVE_INFINITY;
		double z = Double.NaN;
		while (vertexBuffer.hasRemaining()) {
			final Point p = new TPoint(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			toXY.transform(p);
			if (p.getX() < minX)
				minX = p.getX();
			if (p.getX() > maxX)
				maxX = p.getX();
			if (p.getY() < minY)
				minY = p.getY();
			if (p.getY() > maxY)
				maxY = p.getY();
			if (Double.isNaN(z))
				z = p.getZ();
		}

		final Point tmp = new TPoint(minX, minY, z);
		fromXY.transform(tmp);
		final ReadOnlyVector3 origin = new Vector3(tmp.getX(), tmp.getY(), tmp.getZ());

		tmp.set(minX, maxY, z);
		fromXY.transform(tmp);
		final ReadOnlyVector3 p1 = new Vector3(tmp.getX(), tmp.getY(), tmp.getZ());

		tmp.set(maxX, minY, z);
		fromXY.transform(tmp);
		final ReadOnlyVector3 p2 = new Vector3(tmp.getX(), tmp.getY(), tmp.getZ());

		final int rows = (int) Math.ceil(p1.subtract(origin, null).length() / SOLAR_STEP);
		final int cols = (int) Math.ceil(p2.subtract(origin, null).length() / SOLAR_STEP);
		double[][] solar = solarOnWall.get(drawMesh);
		if (solar == null) {
			solar = new double[roundToPowerOfTwo(rows)][roundToPowerOfTwo(cols)];
			solarOnWall.put(drawMesh, solar);
		}
		final ReadOnlyVector3 u = p2.subtract(origin, null).normalizeLocal();
		final ReadOnlyVector3 v = p1.subtract(origin, null).normalizeLocal();
		final double dot = normal.dot(directionTowardSun);
		for (int col = 0; col < cols; col++) {
			final ReadOnlyVector3 pU = u.multiply(col * SOLAR_STEP, null).addLocal(origin);
			final double w = (col == cols - 1) ? p2.distance(pU) : SOLAR_STEP;
			for (int row = 0; row < rows; row++) {
				if (computeRequest)
					throw cancelException;
				final ReadOnlyVector3 p = v.multiply(row * SOLAR_STEP, null).addLocal(pU);
				final double h;
				if (row == rows - 1)
					h = p1.getZ() - (row * SOLAR_STEP);
				else
					h = SOLAR_STEP;
				final Ray3 pickRay = new Ray3(p.add(offset, null), directionTowardSun);
				final PickResults pickResults = new PrimitivePickResults();
				for (final Spatial spatial : solarCollidables)
					if (spatial != collisionMesh) {
						PickingUtil.findPick(spatial, pickRay, pickResults, false);
						if (pickResults.getNumber() != 0)
							break;
					}
				if (pickResults.getNumber() == 0) {
					solar[row][col] += dot;
					final int repeat = 1;
					if (addToTotal) {
						final Double val = solarTotal.get(house);
						solarTotal.put(house, val == null ? 0 : val + repeat * dot * w * h * Scene.getInstance().getAnnotationScale());
					}
				}
			}
		}
		if (rows < solar.length)
			for (int col = 0; col < solar[0].length; col++) {
				solar[solar.length - 1][col] = solar[0][col];
//				for (int remainingRow = rows; remainingRow < solar.length - 1; remainingRow++)
//					solar[remainingRow][col] = solar[rows - 1][col];
				 if (rows != solar.length - 1)
				 solar[rows][col] = solar[rows - 1][col];
				// if (rows + 1 != solar.length - 1 && rows + 1 < solar.length)
				// solar[rows + 1][col] = solar[rows - 1][col];
			}

		if (cols < solar[0].length)
			for (int row = 0; row < solar.length; row++) {
				solar[row][solar[0].length - 1] = solar[row][0];
//				for (int remainingCol = cols; remainingCol < solar[0].length - 1; remainingCol++)
//					solar[row][remainingCol] = solar[row][cols - 1];

				 if (cols != solar[0].length - 1)
				 solar[row][cols] = solar[row][cols - 1];
				// if (cols + 1 != solar[0].length - 1 && cols + 1 < solar[0].length)
				// solar[row][cols + 1] = solar[row][cols - 1];
			}

		if (textureCoordsAlreadyComputed.get(drawMesh) == null) {
			counter ++;
			updateRadiationMeshTextureCoords(drawMesh, origin, u, v, rows, cols);
			textureCoordsAlreadyComputed.put(drawMesh, Boolean.TRUE);
		}
	}

	private void updateRadiationMeshTextureCoords(final Mesh drawMesh, final ReadOnlyVector3 origin, final ReadOnlyVector3 uDir, final ReadOnlyVector3 vDir, final int rows, final int cols) {
		final ReadOnlyVector3 o = origin;
		final ReadOnlyVector3 u = uDir.multiply(roundToPowerOfTwo(cols) * EnergyPanel.SOLAR_STEP, null);
		final ReadOnlyVector3 v = vDir.multiply(roundToPowerOfTwo(rows) * EnergyPanel.SOLAR_STEP, null);
		final FloatBuffer vertexBuffer = drawMesh.getMeshData().getVertexBuffer();
		final FloatBuffer textureBuffer = drawMesh.getMeshData().getTextureBuffer(0);
		vertexBuffer.rewind();
		textureBuffer.rewind();
		while (vertexBuffer.hasRemaining()) {
			final ReadOnlyVector3 p = new Vector3(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
			final Vector3 uP = Util.closestPoint(o, u, p, v.negate(null));
			final float uScale = (float) (uP.distance(o) / u.length());
			final Vector3 vP = Util.closestPoint(o, v, p, u.negate(null));
			final float vScale = (float) (vP.distance(o) / v.length());
			textureBuffer.put(uScale).put(vScale);
		}
	}

	private void computeRadiationOnLand(final ReadOnlyVector3 sunLocation) {
		if (sunLocation.getZ() <= 0)
			return;
		final ReadOnlyVector3 directionTowardSun = sunLocation.normalize(null);
		/* needed in order to prevent picking collision with neighboring wall at wall edge */
		final double OFFSET = 0.1;
		final ReadOnlyVector3 offset = directionTowardSun.multiply(OFFSET, null);
		final double SOLAR_STEP = 8;
		final int rows = (int) (256 / SOLAR_STEP);
		final int cols = rows;
		if (solarOnLand == null)
			solarOnLand = new double[rows][cols];
		final Vector3 p = new Vector3();
		for (int col = 0; col < cols; col++) {
			p.setX((col - cols / 2) * SOLAR_STEP);
			for (int row = 0; row < rows; row++) {
				if (computeRequest)
					throw cancelException;
				p.setY((row - rows / 2) * SOLAR_STEP);
				final Ray3 pickRay = new Ray3(p.add(offset, null), directionTowardSun);
				final PickResults pickResults = new PrimitivePickResults();
				for (final Spatial spatial : solarCollidables)
					PickingUtil.findPick(spatial, pickRay, pickResults, false);
				if (pickResults.getNumber() == 0)
					solarOnLand[row][col] += directionTowardSun.dot(Vector3.UNIT_Z);
			}
		}
	}

	private void computeRadiationToday(final Calendar today) {
		final Heliodon heliodon = Heliodon.getInstance();
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.HOUR_OF_DAY, 0);
		for (int minute = 0; minute < 1440; minute += SOLAR_MINUTE_STEP) {
			final ReadOnlyVector3 sunLocation = heliodon.computeSunLocation(today);
			if (sunLocation.getZ() > 0) {
				computeRadiationOnWalls(sunLocation);
				computeRadiationOnRoofs(sunLocation);
				computeRadiationOnLand(sunLocation);
			}
			maxSolarValue++;
			today.add(Calendar.MINUTE, SOLAR_MINUTE_STEP);
			progress();
		}
		maxSolarValue *= (100 - colorMapSlider.getValue()) / 100.0;
	}

	private void updateSolarValueOnAllHouses() {
		applySolarTexture(SceneManager.getInstance().getSolarLand(), solarOnLand, maxSolarValue);
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Foundation) {
				final List<Roof> roofs = new ArrayList<Roof>();
				final Foundation foundation = (Foundation) part;
				for (final HousePart houseChild : foundation.getChildren()) {
					if (houseChild instanceof Wall) {
						// applySolarTexture(houseChild.getMesh(), solarOnWall.get(houseChild), maxSolarValue);
						final Wall wall = (Wall) houseChild;
						applySolarTexture(houseChild.getMesh(), solarOnWall.get(wall.getMesh()), maxSolarValue);
						print(wall, solarOnWall.get(wall.getMesh()));
						final Roof roof = (Roof) wall.getRoof();
						if (roof != null && !roofs.contains(roof))
							roofs.add(roof);
					}
				}
				for (final Roof roof : roofs)
					for (final Spatial roofPart : roof.getRoofPartsRoot().getChildren()) {
						final Mesh mesh = (Mesh) ((Node) roofPart).getChild(0);
						applySolarTexture(mesh, solarOnWall.get(mesh), maxSolarValue);
						print(roof, solarOnWall.get(mesh));
					}
				final Double val = solarTotal.get(foundation);
				foundation.setSolarValue(val == null ? 0 : val.longValue() / (60 / SOLAR_MINUTE_STEP));
			}
		}
		SceneManager.getInstance().refresh();
	}

	private void print(final HousePart part, final double[][] solar) {
		System.out.println(part);
		if (solar == null)
			System.out.println("null");
		else
			for (int i = 0; i < solar.length; i++) {
				for (int j = 0; j < solar[0].length; j++)
					System.out.print((int) Math.round(solar[i][j]) + " ");
				System.out.println();
			}

	}

	private void progress() {
		progressBar.setValue(progressBar.getValue() + 1);
	}

	public void setLatitude(final int latitude) {
		latitudeSpinner.setValue(latitude);
	}

	public int getLatitude() {
		return (Integer) latitudeSpinner.getValue();
	}

	public int roundToPowerOfTwo(final int n) {
		final Integer result = powerOfTwo.get(n);
		if (result != null)
			return result;
		else {
			int powerOfTwo = 2;
			while (true) {
				if (powerOfTwo >= n)
					return powerOfTwo;
				powerOfTwo *= 2;
			}
		}
	}

	public JSlider getColorMapSlider() {
		return colorMapSlider;
	}

	private void applySolarTexture(final Mesh mesh, final double[][] solarData, final long maxValue) {
		final int rows;
		final int cols;
		if (solarData == null) {
			rows = cols = 1;
		} else {
			rows = solarData.length;
			cols = solarData[0].length;
		}

		final ByteBuffer data = BufferUtils.createByteBuffer(cols * rows * 3);
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				final double value = solarData == null ? 0 : solarData[row][col];
				final ColorRGBA color = computeSolarColor(value, maxValue);
				data.put((byte) (color.getRed() * 255)).put((byte) (color.getGreen() * 255)).put((byte) (color.getBlue() * 255));
			}
		}

		final Image image = new Image(ImageDataFormat.RGB, PixelDataType.UnsignedByte, cols, rows, data, null);
		final Texture2D texture = new Texture2D();
		texture.setTextureKey(TextureKey.getRTTKey(MinificationFilter.NearestNeighborNoMipMaps));
		texture.setImage(image);
		final TextureState textureState = new TextureState();
		textureState.setTexture(texture);
		mesh.setRenderState(textureState);
	}

	private ColorRGBA computeSolarColor(final double value, final long maxValue) {
		final ReadOnlyColorRGBA[] colors = EnergyPanel.solarColors;
		long valuePerColorRange = maxValue / (colors.length - 1);
		final int colorIndex;
		if (valuePerColorRange == 0) {
			valuePerColorRange = 1;
			colorIndex = 0;
		} else
			colorIndex = (int) Math.min(value / valuePerColorRange, colors.length - 2);
		final float scalar = Math.min(1.0f, (float) (value - valuePerColorRange * colorIndex) / valuePerColorRange);
		final ColorRGBA color = new ColorRGBA().lerpLocal(colors[colorIndex], colors[colorIndex + 1], scalar);
		return color;
	}

	public void clearAlreadyRendered() {
		alreadyRendered = false;
	}

	public static void setKeepHeatmapOn(final boolean on) {
		keepHeatmapOn = on;
	}

}
