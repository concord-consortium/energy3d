package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.HeatLoad;
import org.concord.energy3d.simulation.LocationData;
import org.concord.energy3d.simulation.SolarRadiation;
import org.concord.energy3d.simulation.Weather;
import org.concord.energy3d.undo.ChangeCityCommand;
import org.concord.energy3d.undo.ChangeDateCommand;
import org.concord.energy3d.undo.ChangeLatitudeCommand;
import org.concord.energy3d.undo.ChangeSolarHeatMapColorContrastCommand;
import org.concord.energy3d.undo.ChangeTimeCommand;
import org.concord.energy3d.util.Util;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;

public class EnergyPanel extends JPanel {

	public static final ReadOnlyColorRGBA[] solarColors = { ColorRGBA.BLUE, ColorRGBA.GREEN, ColorRGBA.YELLOW, ColorRGBA.RED };

	private static final long serialVersionUID = 1L;
	private static final EnergyPanel instance = new EnergyPanel();
	private final DecimalFormat noDecimal = new DecimalFormat();
	private final DecimalFormat oneDecimal = new DecimalFormat();
	private final DecimalFormat twoDecimals = new DecimalFormat();
	private static boolean autoRecomputeEnergy = false;
	private Thread thread;
	private boolean computeRequest;
	private boolean cancel;
	private boolean alreadyRenderedHeatmap = false;
	private boolean computeEnabled = true;

	public enum UpdateRadiation {
		ALWAYS, ONLY_IF_SLECTED_IN_GUI
	};

	private final JPanel dataPanel;
	private final JComboBox<String> cityComboBox;
	private final JTextField outsideTemperatureField;
	private final JTextField sunshineHoursField;
	private final JSpinner dateSpinner;
	private final JSpinner timeSpinner;
	private final JSpinner latitudeSpinner;
	private final JPanel heatMapPanel;
	private final JSlider colorMapSlider;
	private final JProgressBar progressBar;
	private final JPanel thermostatPanel;
	private final JTextField thermostatTemperatureField;
	private final JButton adjustThermostatButton;
	private JPanel partPanel;
	private JPanel timeAndLocationPanel;
	private JLabel partProperty1Label;
	private JLabel partProperty2Label;
	private JLabel partProperty3Label;
	private JTextField partProperty1TextField;
	private JTextField partProperty2TextField;
	private JTextField partProperty3TextField;
	private ChangeListener latitudeChangeListener;
	private ConstructionCostGraph constructionCostGraph;
	private BuildingDailyEnergyGraph buildingDailyEnergyGraph;
	private PvStationDailyEnergyGraph pvStationDailyEnergyGraph;
	private CspStationDailyEnergyGraph cspStationDailyEnergyGraph;
	private BuildingInfoPanel buildingInfoPanel;
	private PvStationInfoPanel pvStationInfoPanel;
	private CspStationInfoPanel cspStationInfoPanel;
	private JTabbedPane buildingTabbedPane, pvStationTabbedPane, cspStationTabbedPane;
	private JPanel buildingPanel, pvStationPanel, cspStationPanel;
	private boolean disableDateSpinner;

	public static EnergyPanel getInstance() {
		return instance;
	}

	private EnergyPanel() {

		noDecimal.setMaximumFractionDigits(0);
		oneDecimal.setMaximumFractionDigits(1);
		twoDecimals.setMaximumFractionDigits(2);

		setLayout(new BorderLayout());
		dataPanel = new JPanel();
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
		add(new JScrollPane(dataPanel), BorderLayout.CENTER);

		// date, time, and location

		timeAndLocationPanel = new JPanel();
		timeAndLocationPanel.setBorder(createTitledBorder("Project", true));
		dataPanel.add(timeAndLocationPanel);
		final GridBagLayout gbl_panel_3 = new GridBagLayout();
		timeAndLocationPanel.setLayout(gbl_panel_3);

		final GridBagConstraints gbc_dateLabel = new GridBagConstraints();
		gbc_dateLabel.gridx = 0;
		gbc_dateLabel.gridy = 0;
		timeAndLocationPanel.add(createLabel("Date: "), gbc_dateLabel);

		dateSpinner = createSpinner(new SpinnerDateModel(Calendar.getInstance().getTime(), null, null, Calendar.MONTH));
		JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "MMMM dd");
		dateSpinner.setEditor(dateEditor);
		dateEditor.getTextField().setColumns((int) ("September 30".length() * 0.7));
		dateSpinner.addChangeListener(new ChangeListener() {
			private boolean firstCall = true;
			private Date lastDate;

			@Override
			public void stateChanged(final ChangeEvent e) {
				if (firstCall) {
					firstCall = false;
					return;
				}
				if (!disableDateSpinner) {
					final ChangeDateCommand c = new ChangeDateCommand();
					final Date d = (Date) dateSpinner.getValue();
					if (lastDate != null) { // fix a bug that causes the spinner to fire when going from Dec into Jan
						final Calendar c0 = new GregorianCalendar();
						c0.setTime(lastDate);
						final Calendar c1 = new GregorianCalendar();
						c1.setTime(d);
						if (c0.get(Calendar.MONTH) == c1.get(Calendar.MONTH) && c0.get(Calendar.DAY_OF_MONTH) == c1.get(Calendar.DAY_OF_MONTH))
							return;
					}
					Scene.getInstance().setDate(d);
					Heliodon.getInstance().setDate(d);
					compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					Scene.getInstance().setTreeLeaves();
					Scene.getInstance().updateMirrors();
					Scene.getInstance().updateSolarPanels();
					Scene.getInstance().setEdited(true);
					updateThermostat();
					EnergyPanel.this.validate();
					lastDate = d;
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			}
		});
		final GridBagConstraints gbc_dateSpinner = new GridBagConstraints();
		gbc_dateSpinner.insets = new Insets(0, 0, 1, 1);
		gbc_dateSpinner.gridx = 1;
		gbc_dateSpinner.gridy = 0;
		timeAndLocationPanel.add(dateSpinner, gbc_dateSpinner);

		latitudeChangeListener = new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				cityComboBox.setSelectedItem("");
			}
		};

		Arrays.sort(LocationData.getInstance().getCities());
		cityComboBox = new JComboBox<String>();
		cityComboBox.setFont(new Font(cityComboBox.getFont().getName(), Font.PLAIN, cityComboBox.getFont().getSize() - 2));
		cityComboBox.setModel(new DefaultComboBoxModel<String>(LocationData.getInstance().getCities()));
		cityComboBox.setSelectedItem("Boston, MA");
		cityComboBox.setMaximumRowCount(15);
		cityComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final String city = (String) cityComboBox.getSelectedItem();
				if (city.equals("")) {
					compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "No city is selected.\nEnergy simulation will not be accurate.", "Warning", JOptionPane.WARNING_MESSAGE);
					Scene.getInstance().setCity(city);
				} else {
					final ChangeCityCommand c = new ChangeCityCommand();
					setLatitude((int) LocationData.getInstance().getLatitutes().get(cityComboBox.getSelectedItem()).floatValue());
					compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					Scene.getInstance().setCity(city);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
				Scene.getInstance().updateMirrors();
				Scene.getInstance().updateSolarPanels();
				Scene.getInstance().setTreeLeaves();
				Scene.getInstance().setEdited(true);
			}
		});

		final GridBagConstraints gbc_cityComboBox = new GridBagConstraints();
		gbc_cityComboBox.gridwidth = 2;
		gbc_cityComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_cityComboBox.gridx = 2;
		gbc_cityComboBox.gridy = 0;
		timeAndLocationPanel.add(cityComboBox, gbc_cityComboBox);

		final GridBagConstraints gbc_timeLabel = new GridBagConstraints();
		gbc_timeLabel.gridx = 0;
		gbc_timeLabel.gridy = 1;
		timeAndLocationPanel.add(createLabel("Time: "), gbc_timeLabel);

		timeSpinner = createSpinner(new SpinnerDateModel());
		timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "H:mm"));
		timeSpinner.addChangeListener(new ChangeListener() {
			private boolean firstCall = true;
			private Date lastDate;

			@Override
			public void stateChanged(final ChangeEvent e) {
				if (firstCall) { // ignore the first event
					firstCall = false;
					return;
				}
				final ChangeTimeCommand c = new ChangeTimeCommand();
				final Date d = (Date) timeSpinner.getValue();
				if (lastDate != null) { // fix a bug that causes the spinner to fire when crossing day boundary
					final Calendar c0 = new GregorianCalendar();
					c0.setTime(lastDate);
					final Calendar c1 = new GregorianCalendar();
					c1.setTime(d);
					if (c0.get(Calendar.HOUR_OF_DAY) == c1.get(Calendar.HOUR_OF_DAY) && c0.get(Calendar.MINUTE) == c1.get(Calendar.MINUTE))
						return;
				}
				Heliodon.getInstance().setTime(d);
				Scene.getInstance().setDate(d);
				updateWeatherData();
				updateThermostat();
				Scene.getInstance().setEdited(true);
				SceneManager.getInstance().changeSkyTexture();
				SceneManager.getInstance().setShading(Heliodon.getInstance().isNightTime());
				// if (MainPanel.getInstance().getShadowButton().isSelected()) {
				// SceneManager.getInstance().setShading(Heliodon.getInstance().isNightTime());
				// } else {
				// SceneManager.getInstance().setShading(false);
				// }
				if (Scene.getInstance().getAlwaysComputeHeatFluxVectors() && SceneManager.getInstance().areHeatFluxVectorsVisible()) { // for now, only heat flow arrows need to call redrawAll
					SceneManager.getInstance().setHeatFluxDaily(false);
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							for (final HousePart part : Scene.getInstance().getParts())
								part.drawHeatFlux();
							return null;
						}
					});
				}
				Scene.getInstance().updateMirrors();
				Scene.getInstance().updateSolarPanels();
				lastDate = d;
				SceneManager.getInstance().getUndoManager().addEdit(c);
			}
		});
		final GridBagConstraints gbc_timeSpinner = new GridBagConstraints();
		gbc_timeSpinner.insets = new Insets(0, 0, 0, 1);
		gbc_timeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_timeSpinner.gridx = 1;
		gbc_timeSpinner.gridy = 1;
		timeAndLocationPanel.add(timeSpinner, gbc_timeSpinner);

		final GridBagConstraints gbc_altitudeLabel = new GridBagConstraints();
		gbc_altitudeLabel.insets = new Insets(0, 1, 0, 0);
		gbc_altitudeLabel.gridx = 2;
		gbc_altitudeLabel.gridy = 1;
		timeAndLocationPanel.add(createLabel("Latitude: "), gbc_altitudeLabel);

		latitudeSpinner = createSpinner(new SpinnerNumberModel(Heliodon.DEFAULT_LATITUDE, -90, 90, 1));
		latitudeSpinner.addChangeListener(latitudeChangeListener);
		latitudeSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				final ChangeLatitudeCommand c = new ChangeLatitudeCommand();
				Heliodon.getInstance().setLatitude(((Integer) latitudeSpinner.getValue()) / 180.0 * Math.PI);
				compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
				Scene.getInstance().updateMirrors();
				Scene.getInstance().updateSolarPanels();
				Scene.getInstance().setEdited(true);
				SceneManager.getInstance().getUndoManager().addEdit(c);
			}
		});
		final GridBagConstraints gbc_latitudeSpinner = new GridBagConstraints();
		gbc_latitudeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_latitudeSpinner.gridx = 3;
		gbc_latitudeSpinner.gridy = 1;
		timeAndLocationPanel.add(latitudeSpinner, gbc_latitudeSpinner);

		timeAndLocationPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, timeAndLocationPanel.getPreferredSize().height));

		final GridBagConstraints gbc_outsideTemperatureLabel = new GridBagConstraints();
		gbc_outsideTemperatureLabel.insets = new Insets(0, 8, 1, 1);
		gbc_outsideTemperatureLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_outsideTemperatureLabel.gridx = 0;
		gbc_outsideTemperatureLabel.gridy = 2;
		timeAndLocationPanel.add(createLabel("Temp.: "), gbc_outsideTemperatureLabel);

		outsideTemperatureField = createTextField();
		outsideTemperatureField.setToolTipText("Current outside temperature at this time and day");
		outsideTemperatureField.setEditable(false);
		outsideTemperatureField.setEnabled(false);
		outsideTemperatureField.setBackground(Color.WHITE);
		final GridBagConstraints gbc_outsideTemperatureField = new GridBagConstraints();
		gbc_outsideTemperatureField.fill = GridBagConstraints.HORIZONTAL;
		gbc_outsideTemperatureField.gridx = 1;
		gbc_outsideTemperatureField.gridy = 2;
		timeAndLocationPanel.add(outsideTemperatureField, gbc_outsideTemperatureField);

		final GridBagConstraints gbc_sunshineLabel = new GridBagConstraints();
		gbc_sunshineLabel.insets = new Insets(0, 5, 0, 0);
		gbc_sunshineLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_sunshineLabel.gridx = 2;
		gbc_sunshineLabel.gridy = 2;
		timeAndLocationPanel.add(createLabel("Sunshine: "), gbc_sunshineLabel);

		sunshineHoursField = createTextField();
		sunshineHoursField.setToolTipText("Average sunshine hours in this month");
		sunshineHoursField.setEditable(false);
		sunshineHoursField.setEnabled(false);
		sunshineHoursField.setBackground(Color.WHITE);
		final GridBagConstraints gbc_sunshineHoursField = new GridBagConstraints();
		gbc_sunshineHoursField.fill = GridBagConstraints.HORIZONTAL;
		gbc_sunshineHoursField.gridx = 3;
		gbc_sunshineHoursField.gridy = 2;
		timeAndLocationPanel.add(sunshineHoursField, gbc_sunshineHoursField);

		// part panel

		partPanel = new JPanel(new BorderLayout(10, 0));
		partPanel.setBorder(createTitledBorder("Part", true));
		partPanel.setMaximumSize(new Dimension(partPanel.getMaximumSize().width, partPanel.getPreferredSize().height));
		dataPanel.add(partPanel);

		final JPanel labelPanel = new JPanel(new GridLayout(3, 1, 2, 2));
		partPanel.add(labelPanel, BorderLayout.WEST);
		partProperty1Label = createLabel(" X:");
		labelPanel.add(partProperty1Label);
		partProperty2Label = createLabel(" Y:");
		labelPanel.add(partProperty2Label);
		partProperty3Label = createLabel(" Z:");
		labelPanel.add(partProperty3Label);

		final JPanel fieldPanel = new JPanel(new GridLayout(3, 1, 2, 2));
		partPanel.add(fieldPanel, BorderLayout.CENTER);
		partProperty1TextField = createTextField();
		partProperty1TextField.setEnabled(false);
		partProperty1TextField.setBackground(Color.WHITE);
		fieldPanel.add(partProperty1TextField);
		partProperty2TextField = createTextField();
		partProperty2TextField.setEnabled(false);
		partProperty2TextField.setBackground(Color.WHITE);
		fieldPanel.add(partProperty2TextField);
		partProperty3TextField = createTextField();
		partProperty3TextField.setEnabled(false);
		partProperty3TextField.setBackground(Color.WHITE);
		fieldPanel.add(partProperty3TextField);

		// pv station panel
		pvStationPanel = new JPanel();
		pvStationPanel.setBorder(createTitledBorder("Photovoltaic Power Station", true));
		pvStationPanel.setLayout(new BoxLayout(pvStationPanel, BoxLayout.Y_AXIS));

		pvStationTabbedPane = new JTabbedPane();
		pvStationTabbedPane.setFont(new Font(pvStationTabbedPane.getFont().getName(), Font.PLAIN, pvStationTabbedPane.getFont().getSize() - 1));
		pvStationPanel.add(pvStationTabbedPane);

		pvStationInfoPanel = new PvStationInfoPanel();
		pvStationTabbedPane.add("Info", pvStationInfoPanel);

		pvStationDailyEnergyGraph = new PvStationDailyEnergyGraph();
		pvStationTabbedPane.add("Output", pvStationDailyEnergyGraph);

		pvStationTabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (pvStationTabbedPane.getSelectedComponent() == pvStationDailyEnergyGraph) {
					if (SceneManager.getInstance().getSolarHeatMap()) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Foundation) {
							pvStationDailyEnergyGraph.addGraph((Foundation) selectedPart);
						} else {
							pvStationDailyEnergyGraph.removeGraph();
						}
					}
				}
				TimeSeriesLogger.getInstance().logGraphTab(pvStationTabbedPane.getTitleAt(pvStationTabbedPane.getSelectedIndex()));
			}
		});
		pvStationPanel.setMaximumSize(new Dimension(pvStationPanel.getMaximumSize().width, pvStationPanel.getPreferredSize().height));

		// csp station panel
		cspStationPanel = new JPanel();
		cspStationPanel.setBorder(createTitledBorder("Concentrated Solar Power Station", true));
		cspStationPanel.setLayout(new BoxLayout(cspStationPanel, BoxLayout.Y_AXIS));

		cspStationTabbedPane = new JTabbedPane();
		cspStationTabbedPane.setFont(new Font(cspStationTabbedPane.getFont().getName(), Font.PLAIN, cspStationTabbedPane.getFont().getSize() - 1));
		cspStationPanel.add(cspStationTabbedPane);

		cspStationInfoPanel = new CspStationInfoPanel();
		cspStationTabbedPane.add("Info", cspStationInfoPanel);

		cspStationDailyEnergyGraph = new CspStationDailyEnergyGraph();
		cspStationTabbedPane.add("Output", cspStationDailyEnergyGraph);

		cspStationTabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (cspStationTabbedPane.getSelectedComponent() == cspStationDailyEnergyGraph) {
					if (SceneManager.getInstance().getSolarHeatMap()) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Foundation) {
							cspStationDailyEnergyGraph.addGraph((Foundation) selectedPart);
						} else {
							cspStationDailyEnergyGraph.removeGraph();
						}
					}
				}
				TimeSeriesLogger.getInstance().logGraphTab(cspStationTabbedPane.getTitleAt(cspStationTabbedPane.getSelectedIndex()));
			}
		});
		cspStationPanel.setMaximumSize(new Dimension(cspStationPanel.getMaximumSize().width, cspStationPanel.getPreferredSize().height));

		// building panel

		buildingPanel = new JPanel();
		buildingPanel.setBorder(createTitledBorder("Building", true));
		dataPanel.add(buildingPanel);
		buildingPanel.setLayout(new BoxLayout(buildingPanel, BoxLayout.Y_AXIS));

		thermostatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); // thermostat for the selected building
		thermostatPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		thermostatPanel.add(createLabel(" Thermostat: "), BorderLayout.WEST);
		thermostatTemperatureField = createTextField();
		thermostatTemperatureField.setPreferredSize(new Dimension(72, sunshineHoursField.getPreferredSize().height));
		thermostatTemperatureField.setEnabled(false);
		thermostatTemperatureField.setBackground(Color.WHITE);
		thermostatPanel.add(thermostatTemperatureField, BorderLayout.CENTER);
		adjustThermostatButton = new JButton("Adjust");
		adjustThermostatButton.setFont(new Font(adjustThermostatButton.getFont().getName(), Font.PLAIN, adjustThermostatButton.getFont().getSize() - 2));
		adjustThermostatButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (selectedPart == null)
					return;
				Foundation foundation = null;
				if (selectedPart instanceof Foundation) {
					foundation = (Foundation) selectedPart;
				} else {
					foundation = selectedPart.getTopContainer();
				}
				MainPanel.getInstance().getEnergyViewButton().setSelected(false);
				new ThermostatDialog(foundation).setVisible(true);
				TimeSeriesLogger.getInstance().logAdjustThermostatButton();
			}
		});
		buildingPanel.add(thermostatPanel);

		buildingTabbedPane = new JTabbedPane();
		buildingTabbedPane.setFont(new Font(buildingTabbedPane.getFont().getName(), Font.PLAIN, buildingTabbedPane.getFont().getSize() - 1));
		buildingPanel.add(buildingTabbedPane);

		buildingInfoPanel = new BuildingInfoPanel(); // basics panel
		buildingTabbedPane.add("Basics", buildingInfoPanel);

		constructionCostGraph = new ConstructionCostGraph(); // construction cost graph
		buildingTabbedPane.add("Cost", constructionCostGraph);

		buildingDailyEnergyGraph = new BuildingDailyEnergyGraph(); // hourly energy graph
		buildingTabbedPane.add("Energy", buildingDailyEnergyGraph);

		buildingTabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (buildingTabbedPane.getSelectedComponent() == buildingDailyEnergyGraph) {
					if (SceneManager.getInstance().getSolarHeatMap()) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Foundation) {
							buildingDailyEnergyGraph.addGraph((Foundation) selectedPart);
						} else {
							buildingDailyEnergyGraph.removeGraph();
						}
					}
				}
				TimeSeriesLogger.getInstance().logGraphTab(buildingTabbedPane.getTitleAt(buildingTabbedPane.getSelectedIndex()));
			}
		});
		buildingPanel.setMaximumSize(new Dimension(buildingPanel.getMaximumSize().width, buildingPanel.getPreferredSize().height));

		dataPanel.add(Box.createVerticalGlue());

		// heat map slider and progress bar

		heatMapPanel = new JPanel(new BorderLayout());
		heatMapPanel.setBorder(createTitledBorder("Heat Map Contrast", true));

		colorMapSlider = new MySlider();
		colorMapSlider.setToolTipText("<html>Increase or decrease the color contrast of the solar heat map.</html>");
		colorMapSlider.setMinimum(15);
		colorMapSlider.setMaximum(95);
		colorMapSlider.setMinimumSize(colorMapSlider.getPreferredSize());
		colorMapSlider.setSnapToTicks(true);
		colorMapSlider.setMinorTickSpacing(1);
		colorMapSlider.setMajorTickSpacing(5);
		colorMapSlider.setFocusable(false);
		colorMapSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (!colorMapSlider.getValueIsAdjusting()) {
					final ChangeSolarHeatMapColorContrastCommand c = new ChangeSolarHeatMapColorContrastCommand();
					Scene.getInstance().setSolarHeatMapColorContrast(colorMapSlider.getValue());
					compute(SceneManager.getInstance().getSolarHeatMap() ? UpdateRadiation.ALWAYS : UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					Scene.getInstance().setEdited(true, false);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
			}
		});
		heatMapPanel.add(colorMapSlider, BorderLayout.CENTER);
		heatMapPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, heatMapPanel.getPreferredSize().height));

		progressBar = new JProgressBar();
		add(progressBar, BorderLayout.SOUTH);

	}

	public void compute(final UpdateRadiation updateRadiation) {
		if (!computeEnabled)
			return;
		updateWeatherData(); // TODO: There got to be a better way to do this
		if (thread != null && thread.isAlive())
			computeRequest = true;
		else {
			((Component) SceneManager.getInstance().getCanvas()).setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			thread = new Thread("Energy Computation") {
				@Override
				public void run() {
					do {
						computeRequest = false;
						cancel = false;
						/* since this thread can accept multiple computeRequest, cannot use updateRadiationColorMap parameter directly */
						try {
							final boolean doCompute = updateRadiation == UpdateRadiation.ALWAYS || (SceneManager.getInstance().getSolarHeatMap() && (!alreadyRenderedHeatmap || autoRecomputeEnergy));
							if (doCompute) {
								alreadyRenderedHeatmap = true;
								computeNow();
								if (!cancel) {
									SceneManager.getInstance().getSolarLand().setVisible(true);
									SceneManager.getInstance().refresh();
								} else if (!autoRecomputeEnergy)
									turnOffCompute();
							} else
								turnOffCompute();
						} catch (final Throwable e) {
							e.printStackTrace();
							Util.reportError(e);
							return;
						} finally {
							((Component) SceneManager.getInstance().getCanvas()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
						EventQueue.invokeLater(new Runnable() { // must run this Swing UI update in the event queue to avoid a possible deadlock
							@Override
							public void run() {
								progress(0);
								if (SceneManager.getInstance().getSolarHeatMap()) {
									final HousePart p = SceneManager.getInstance().getSelectedPart();
									if (p instanceof Foundation) {
										Foundation f = (Foundation) p;
										switch (f.getSupportingType()) {
										case Foundation.BUILDING:
											Util.setSilently(buildingTabbedPane, buildingDailyEnergyGraph);
											buildingDailyEnergyGraph.addGraph(f);
											TimeSeriesLogger.getInstance().logAnalysis(buildingDailyEnergyGraph);
											break;
										case Foundation.PV_STATION:
											Util.setSilently(pvStationTabbedPane, pvStationDailyEnergyGraph);
											pvStationDailyEnergyGraph.addGraph(f);
											TimeSeriesLogger.getInstance().logAnalysis(pvStationDailyEnergyGraph);
											break;
										case Foundation.CSP_STATION:
											Util.setSilently(cspStationTabbedPane, cspStationDailyEnergyGraph);
											cspStationDailyEnergyGraph.addGraph(f);
											TimeSeriesLogger.getInstance().logAnalysis(cspStationDailyEnergyGraph);
											break;
										}
									}
								}
							}
						});
					} while (computeRequest);
					thread = null;
				}
			};
			thread.start();
		}
	}

	public void computeNow() {
		try {
			System.out.println("EnergyPanel.computeNow()");
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					progressBar.setValue(0);
					progressBar.setStringPainted(false);
				}
			});

			synchronized (SceneManager.getInstance()) {
				final int timeStep = SolarRadiation.getInstance().getTimeStep();
				for (final HousePart part : Scene.getInstance().getParts())
					part.setHeatLoss(new double[SolarRadiation.MINUTES_OF_DAY / timeStep]);
				SolarRadiation.getInstance().compute();
				final Calendar c = (Calendar) Heliodon.getInstance().getCalender().clone();
				HeatLoad.getInstance().computeEnergyToday(c);
				SolarRadiation.getInstance().computeTotalEnergyForBuildings();
			}
			Scene.getInstance().setTreeLeaves();

			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					updateWeatherData();
					updateProperties();
					progressBar.setValue(100);
				}
			});

		} catch (final CancellationException e) {
			System.out.println("Energy calculation cancelled.");
		}

	}

	// TODO: There should be a better way to do this.
	public void clearRadiationHeatMap() {
		compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
	}

	public void updateWeatherData() {
		final String city = (String) cityComboBox.getSelectedItem();
		if (city.equals("")) {
			switch (Scene.getInstance().getUnit()) {
			case InternationalSystemOfUnits:
				outsideTemperatureField.setText("15 \u00B0C");
				break;
			case USCustomaryUnits:
				outsideTemperatureField.setText(Math.round(32.0 + 9.0 * 15.0 / 5.0) + " \u00B0F");
				break;
			}
			sunshineHoursField.setText("10");
		} else {
			switch (Scene.getInstance().getUnit()) {
			case InternationalSystemOfUnits:
				outsideTemperatureField.setText(Math.round(Weather.getInstance().getCurrentOutsideTemperature()) + " \u00B0C");
				break;
			case USCustomaryUnits:
				outsideTemperatureField.setText(Math.round(32.0 + 9.0 * Weather.getInstance().getCurrentOutsideTemperature() / 5.0) + " \u00B0F");
				break;
			}
			final Map<String, int[]> sunshineHours = LocationData.getInstance().getSunshineHours();
			final int month = Heliodon.getInstance().getCalender().get(Calendar.MONTH);
			sunshineHoursField.setText(Math.round(sunshineHours.get(city)[month] / 30.0) + " hours");
		}
	}

	public JTabbedPane getBuildingTabbedPane() {
		return buildingTabbedPane;
	}

	public JTabbedPane getPvStationTabbedPane() {
		return pvStationTabbedPane;
	}

	public JTabbedPane getCspStationTabbedPane() {
		return cspStationTabbedPane;
	}

	public JSpinner getDateSpinner() {
		return dateSpinner;
	}

	public JSpinner getTimeSpinner() {
		return timeSpinner;
	}

	public JSpinner getLatitudeSpinner() {
		return latitudeSpinner;
	}

	public BuildingInfoPanel getBasicsPanel() {
		return buildingInfoPanel;
	}

	public ConstructionCostGraph getConstructionCostGraph() {
		return constructionCostGraph;
	}

	public BuildingDailyEnergyGraph getBuildingDailyEnergyGraph() {
		return buildingDailyEnergyGraph;
	}

	public PvStationDailyEnergyGraph getPvStationDailyEnergyGraph() {
		return pvStationDailyEnergyGraph;
	}

	public CspStationDailyEnergyGraph getCspStationDailyEnergyGraph() {
		return cspStationDailyEnergyGraph;
	}

	/** call when loading a new file */
	public void clearAllGraphs() {
		constructionCostGraph.removeGraph();
		buildingDailyEnergyGraph.removeGraph();
		pvStationDailyEnergyGraph.removeGraph();
		cspStationDailyEnergyGraph.removeGraph();
	}

	public void progress(final int percentage) {
		if (percentage == 0) {
			progressBar.setValue(0);
			progressBar.setStringPainted(false);
		} else {
			progressBar.setValue(percentage);
			progressBar.setStringPainted(true);
		}
	}

	public void setCity(final String city) {
		cityComboBox.setSelectedItem(city);
		cityComboBox.repaint(); // in some cases, this must be called in order to update the view
	}

	public void setLatitude(final int latitude) {
		Util.setSilently(latitudeSpinner, latitude);
		Heliodon.getInstance().setLatitude(Math.toRadians(latitude));
	}

	public JSlider getColorMapSlider() {
		return colorMapSlider;
	}

	public void clearAlreadyRendered() {
		alreadyRenderedHeatmap = false;
	}

	public static void setAutoRecomputeEnergy(final boolean on) {
		autoRecomputeEnergy = on;
	}

	public void setComputeEnabled(final boolean computeEnabled) {
		this.computeEnabled = computeEnabled;
	}

	public void updateProperties() {

		// update part properties

		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();

		final boolean energyViewShown = MainPanel.getInstance().getEnergyViewButton().isSelected();
		double meterToFeet = 1;
		switch (Scene.getInstance().getUnit()) {
		case InternationalSystemOfUnits:
			meterToFeet = 1;
			break;
		case USCustomaryUnits:
			meterToFeet = 3.28084;
			break;
		}
		final double scale = Scene.getInstance().getAnnotationScale() * meterToFeet;

		final TitledBorder partPanelBorder = (TitledBorder) partPanel.getBorder();
		if (selectedPart != null) {
			final ReadOnlyVector3 v = selectedPart.getAbsPoint(0);
			if (selectedPart instanceof Tree) {
				final Tree tree = (Tree) selectedPart;
				if (tree.isDrawable()) {
					partPanelBorder.setTitle("Tree (" + tree.getId() + "): " + tree.getTreeName());
					partProperty1Label.setText("  Spread:");
					partProperty2Label.setText("  Height:");
					partProperty3Label.setText("  Position:");
					final double l = v.length();
					double a = 90 + Math.toDegrees(Math.asin(-v.getY() / l));
					if (v.getX() < 0)
						a = 360 - a;
					if (Util.isZero(a - 360)) // reset 360 to 0
						a = 0;
					partProperty1TextField.setText(oneDecimal.format(tree.getWidth() * scale) + " m");
					partProperty2TextField.setText(oneDecimal.format(tree.getHeight() * scale) + " m");
					partProperty3TextField.setText("(" + oneDecimal.format(v.getX() * scale) + ", " + oneDecimal.format(v.getY() * scale) + ") m or (" + oneDecimal.format(l * scale) + " m, " + oneDecimal.format(a) + "\u00B0)");
					partProperty1TextField.setToolTipText("The spread of the tree");
					partProperty2TextField.setToolTipText("The height of the tree");
					partProperty3TextField.setToolTipText("The (x, y) or polar coordinates on the land");
				}
			} else if (selectedPart instanceof Human) {
				final Human human = (Human) selectedPart;
				if (human.isDrawable()) {
					partPanelBorder.setTitle("Human (" + human.getId() + "): " + human.getHumanName());
					partProperty1Label.setText("  X:");
					partProperty2Label.setText("  Y:");
					partProperty3Label.setText("  Z:");
					partProperty1TextField.setText(oneDecimal.format(v.getX() * scale) + " m");
					partProperty2TextField.setText(oneDecimal.format(v.getY() * scale) + " m");
					partProperty3TextField.setText(oneDecimal.format(v.getZ() * scale) + " m");
				}
			} else if (selectedPart instanceof SolarPanel) {
				final SolarPanel sp = (SolarPanel) selectedPart;
				if (sp.isDrawable()) {
					partPanelBorder.setTitle("Solar Panel (" + sp.getId() + ")");
					Foundation f = (Foundation) sp.getTopContainer();
					partProperty1Label.setText("  Size & Position:");
					partProperty1TextField.setText(twoDecimals.format(sp.getPanelWidth() * meterToFeet) + "\u00d7" + twoDecimals.format(sp.getPanelHeight() * meterToFeet) + " m, (" + oneDecimal.format(v.getX() * scale) + ", " + oneDecimal.format(v.getY() * scale) + ", " + oneDecimal.format(v.getZ() * scale) + ") m");
					partProperty2Label.setText("  Angles:");
					double a = sp.getRelativeAzimuth() + f.getAzimuth();
					if (a >= 360)
						a -= 360;
					boolean flat = (sp.getContainer() instanceof Roof && Util.isZero(sp.getContainer().getHeight())) || (sp.getContainer() instanceof Foundation);
					partProperty2TextField.setText(flat ? "tilt: " + oneDecimal.format(sp.getTiltAngle()) + "\u00B0, azimuth: " + oneDecimal.format(a) + "\u00B0" : " --- ");
					partProperty1TextField.setToolTipText("The length, width, and (x, y, z) coordinates of the solar panel");
					partProperty2TextField.setToolTipText("The angles of the solar panel");
					String eff = oneDecimal.format(sp.getCellEfficiency() * 100) + "%";
					if (energyViewShown) {
						partProperty3Label.setText("  Efficiency & Yield:");
						partProperty3TextField.setText(eff + ", " + twoDecimals.format(sp.getSolarPotentialToday() * sp.getCellEfficiency()) + " kWh");
						partProperty3TextField.setToolTipText("The solar cell efficiency and daily yield of the solar panel");
					} else {
						partProperty3Label.setText("  Efficiency:");
						partProperty3TextField.setText(eff);
						partProperty3TextField.setToolTipText("The solar cell efficiency of the solar panel");
					}
				}
			} else if (selectedPart instanceof Mirror) {
				final Mirror m = (Mirror) selectedPart;
				if (m.isDrawable()) {
					partPanelBorder.setTitle("Mirror (" + m.getId() + ")");
					Foundation f = (Foundation) m.getTopContainer();
					partProperty1Label.setText("  Size & Position:");
					partProperty1TextField.setText(twoDecimals.format(m.getMirrorWidth() * meterToFeet) + "\u00d7" + twoDecimals.format(m.getMirrorHeight() * meterToFeet) + " m, (" + oneDecimal.format(v.getX() * scale) + ", " + oneDecimal.format(v.getY() * scale) + ", " + oneDecimal.format(v.getZ() * scale) + ") m");
					partProperty2Label.setText("  Angles:");
					double a = m.getRelativeAzimuth() + f.getAzimuth();
					if (a >= 360)
						a -= 360;
					boolean flat = m.getContainer() instanceof Foundation;
					partProperty2TextField.setText(flat ? "tilt: " + oneDecimal.format(m.getTiltAngle()) + "\u00B0, azimuth: " + oneDecimal.format(a) + "\u00B0" : " --- ");
					partProperty1TextField.setToolTipText("The length, width, and (x, y, z) coordinates of the solar panel");
					partProperty2TextField.setToolTipText("The angles of the solar panel");
					String reflectivity = oneDecimal.format(m.getReflectivity() * 100) + "%";
					if (energyViewShown) {
						partProperty3Label.setText("  Reflectivity & Yield:");
						partProperty3TextField.setText(reflectivity + ", " + twoDecimals.format(m.getSolarPotentialToday() * m.getReflectivity()) + " kWh");
						partProperty3TextField.setToolTipText("The reflectivity and yield of this mirror");
					} else {
						partProperty3Label.setText("  Reflectivity:");
						partProperty3TextField.setText(reflectivity);
						partProperty3TextField.setToolTipText("The reflectivity of this mirror");
					}
				}
			} else if (selectedPart instanceof Sensor) {
				final Sensor sensor = (Sensor) selectedPart;
				if (sensor.isDrawable()) {
					partPanelBorder.setTitle("Sensor (" + sensor.getId() + ")");
					partProperty1Label.setText("  Position:");
					partProperty2Label.setText("  Light:");
					partProperty3Label.setText("  Heat:");
					partProperty1TextField.setText("(" + oneDecimal.format(v.getX() * scale) + ", " + oneDecimal.format(v.getY() * scale) + ", " + oneDecimal.format(v.getZ() * scale) + ") m");
					partProperty2TextField.setText(twoDecimals.format(sensor.getSolarPotentialToday() / sensor.getArea()) + " kWh/day/m\u00B2");
					partProperty3TextField.setText(twoDecimals.format(sensor.getTotalHeatLoss() / sensor.getArea()) + " kWh/day/m\u00B2");
					partProperty1TextField.setToolTipText("The (x, y, z) coordinates of the sensor");
					partProperty2TextField.setToolTipText("The light intensity measured by the sensor");
					partProperty3TextField.setToolTipText("The heat flux measured by the sensor");
				}
			} else if (selectedPart instanceof Foundation) {
				final Foundation foundation = (Foundation) selectedPart;
				if (foundation.isDrawable()) {
					final Vector3 v1 = foundation.getAbsPoint(1);
					final Vector3 v2 = foundation.getAbsPoint(2);
					final Vector3 v3 = foundation.getAbsPoint(3);
					final double cx = 0.25 * (v.getX() + v1.getX() + v2.getX() + v3.getX());
					final double cy = 0.25 * (v.getY() + v1.getY() + v2.getY() + v3.getY());
					final double lx = v.distance(v2);
					final double ly = v.distance(v1);
					final double lz = foundation.getHeight();
					partPanelBorder.setTitle("Foundation (" + foundation.getId() + ")");
					partProperty1Label.setText("  Size:");
					partProperty2Label.setText("  Position:");
					partProperty3Label.setText("  Azimuth:");
					partProperty1TextField.setText(twoDecimals.format(lx * scale) + "\u00d7" + (twoDecimals.format(ly * scale)) + "\u00d7" + (twoDecimals.format(lz * scale)) + " m \u2248 " + twoDecimals.format(lx * ly * scale * scale) + " m\u00B2");
					partProperty2TextField.setText("(" + twoDecimals.format(cx * scale) + ", " + twoDecimals.format(cy * scale) + ") m");
					partProperty3TextField.setText(noDecimal.format(foundation.getAzimuth()) + "\u00B0");
					partProperty1TextField.setToolTipText("The length and width of the foundation");
					partProperty2TextField.setToolTipText("The (x, y) coordinate of the center of the foundation");
					partProperty3TextField.setToolTipText("The azimuth of the reference edge");
				}
			} else if (selectedPart instanceof Roof) {
				final Roof roof = (Roof) selectedPart;
				if (roof.isDrawable()) {
					partProperty1Label.setText("  Area:");
					partProperty1TextField.setText(twoDecimals.format(roof.getArea()) + " m\u00B2");
					partProperty2Label.setText("  Rise:");
					partProperty2TextField.setText(twoDecimals.format(roof.getHeight() * scale) + " m");
					partProperty1TextField.setToolTipText("The total area of the roof");
					partProperty2TextField.setToolTipText("<html>The rise of the roof<br>(the highest point of the roof to the top of the walls</html>");
					final String id = "Roof (" + roof.getId() + ")";
					final String rval = oneDecimal.format(Util.toUsRValue(roof.getUValue()));
					if (energyViewShown) {
						partPanelBorder.setTitle(id + " - R-value = " + rval);
						partProperty3Label.setText("  Solar:");
						partProperty3TextField.setText(twoDecimals.format(roof.getSolarPotentialToday() * (1 - roof.getAlbedo())) + " kWh");
						partProperty3TextField.setToolTipText("The solar potential of the roof of the day");
					} else {
						partPanelBorder.setTitle(id);
						partProperty3Label.setText("  R-value:");
						partProperty3TextField.setText(rval + " (US system)");
						partProperty3TextField.setToolTipText("The R-value of the roof");
					}
				}
			} else if (selectedPart instanceof Window) {
				final Window window = (Window) selectedPart;
				if (window.isDrawable()) {
					final double lx = v.distance(window.getAbsPoint(2));
					final double ly = v.distance(window.getAbsPoint(1));
					partProperty1Label.setText("  Size:");
					partProperty1TextField.setText(twoDecimals.format(lx * scale) + "\u00d7" + (twoDecimals.format(ly * scale)) + " m \u2248 " + twoDecimals.format(lx * ly * scale * scale) + " m\u00B2");
					partProperty2Label.setText("  U-value:");
					partProperty2TextField.setText(twoDecimals.format(Util.toUsUValue(window.getUValue())) + " (US system)");
					partProperty1TextField.setToolTipText("The width and height of the window");
					partProperty2TextField.setToolTipText("The U-value of the window");
					final String id = "Window (" + window.getId() + ")";
					final String shgc = twoDecimals.format(window.getSolarHeatGainCoefficient());
					if (energyViewShown) {
						partPanelBorder.setTitle(id + " - SHGC = " + shgc);
						partProperty3Label.setText("  Gain:");
						partProperty3TextField.setText(twoDecimals.format(window.getSolarPotentialToday() * window.getSolarHeatGainCoefficient()) + " kWh");
						partProperty3TextField.setToolTipText("The solar heat gain of the window of the day");
					} else {
						partPanelBorder.setTitle(id);
						partProperty3Label.setText("  SHGC:");
						partProperty3TextField.setText(shgc);
						partProperty3TextField.setToolTipText("The solar heat gain coefficient (SHGC) of the window");
					}
				}
			} else if (selectedPart instanceof Wall) {
				final Wall wall = (Wall) selectedPart;
				if (wall.isDrawable()) {
					final Vector3 v1 = wall.getAbsPoint(1);
					final Vector3 v2 = wall.getAbsPoint(2);
					final Vector3 v3 = wall.getAbsPoint(3);
					final double cx = 0.25 * (v.getX() + v1.getX() + v2.getX() + v3.getX());
					final double cy = 0.25 * (v.getY() + v1.getY() + v2.getY() + v3.getY());
					final double lx = v.distance(v2);
					final double ly = v.distance(v1);
					partProperty1Label.setText("  Size:");
					partProperty1TextField.setText(twoDecimals.format(lx * scale) + "\u00d7" + (twoDecimals.format(ly * scale)) + " m \u2248 " + twoDecimals.format(lx * ly * scale * scale) + " m\u00B2");
					partProperty2Label.setText("  Position:");
					partProperty2TextField.setText("(" + twoDecimals.format(cx * scale) + ", " + twoDecimals.format(cy * scale) + ") m");
					partProperty1TextField.setToolTipText("The width and height of the wall");
					partProperty2TextField.setToolTipText("The (x, y) coordinate of the center of the wall");
					final String id = "Wall (" + wall.getId() + ")";
					final String rval = oneDecimal.format(Util.toUsRValue(wall.getUValue()));
					if (energyViewShown) {
						partPanelBorder.setTitle(id + " - R-value = " + rval);
						partProperty3Label.setText("  Solar:");
						partProperty3TextField.setText(twoDecimals.format(wall.getSolarPotentialToday() * (1 - wall.getAlbedo())) + " kWh");
						partProperty3TextField.setToolTipText("The solar potential of the wall");
					} else {
						partPanelBorder.setTitle(id);
						partProperty3Label.setText("  R-value:");
						partProperty3TextField.setText(rval + " (US system)");
						partProperty3TextField.setToolTipText("The R-value of the wall");
					}
				}
			} else if (selectedPart instanceof Door) {
				final Door door = (Door) selectedPart;
				if (door.isDrawable()) {
					final Vector3 v1 = door.getAbsPoint(1);
					final Vector3 v2 = door.getAbsPoint(2);
					final Vector3 v3 = door.getAbsPoint(3);
					final double cx = 0.25 * (v.getX() + v1.getX() + v2.getX() + v3.getX());
					final double cy = 0.25 * (v.getY() + v1.getY() + v2.getY() + v3.getY());
					final double lx = v.distance(v2);
					final double ly = v.distance(v1);
					partPanelBorder.setTitle("Door (" + door.getId() + ")");
					partProperty1Label.setText("  Size:");
					partProperty1TextField.setText(twoDecimals.format(lx * scale) + "\u00d7" + (twoDecimals.format(ly * scale)) + " m \u2248 " + twoDecimals.format(lx * ly * scale * scale) + " m\u00B2");
					partProperty2Label.setText("  Position:");
					partProperty2TextField.setText("(" + twoDecimals.format(cx * scale) + ", " + twoDecimals.format(cy * scale) + ") m");
					partProperty3Label.setText("  U-value:");
					partProperty3TextField.setText(twoDecimals.format(Util.toUsUValue(door.getUValue())) + " (US system)");
					partProperty1TextField.setToolTipText("The width and height of the door");
					partProperty2TextField.setToolTipText("The (x, y) coordinates of the center of the door");
					partProperty3TextField.setToolTipText("The U-value of the wall");
				}
			} else if (selectedPart instanceof Floor) {
				final Floor floor = (Floor) selectedPart;
				if (floor.isDrawable()) {
					partPanelBorder.setTitle("Floor (" + floor.getId() + ")");
					partProperty1Label.setText("  Area:");
					partProperty2Label.setText("  Position:");
					partProperty3Label.setText("  Height:");
					partProperty1TextField.setText(oneDecimal.format(floor.getArea()) + " m\u00B2");
					if (floor.getPoints().size() > 1) {
						final Vector3 v1 = floor.getAbsPoint(1);
						final Vector3 v2 = floor.getAbsPoint(2);
						final Vector3 v3 = floor.getAbsPoint(3);
						final double cx = 0.25 * (v.getX() + v1.getX() + v2.getX() + v3.getX());
						final double cy = 0.25 * (v.getY() + v1.getY() + v2.getY() + v3.getY());
						partProperty2TextField.setText("(" + oneDecimal.format(cx * scale) + ", " + oneDecimal.format(cy * scale) + ") m");
					}
					partProperty3TextField.setText(oneDecimal.format(v.getZ() * scale) + " m");
					partProperty1TextField.setToolTipText("The area of the floor");
					partProperty2TextField.setToolTipText("The (x, y) position of the center of the floor");
					partProperty3TextField.setToolTipText("The height of the floor");
				}
			}
		} else {
			partPanelBorder.setTitle("Part");
			partProperty1Label.setText("  -");
			partProperty2Label.setText("  -");
			partProperty3Label.setText("  -");
			partProperty1TextField.setText("");
			partProperty2TextField.setText("");
			partProperty3TextField.setText("");
			partProperty1TextField.setToolTipText(null);
			partProperty2TextField.setToolTipText(null);
			partProperty3TextField.setToolTipText(null);
		}
		partPanel.repaint();

		// update building properties

		final Foundation selectedFoundation;
		if (selectedPart == null) {
			selectedFoundation = null;
		} else if (selectedPart instanceof Foundation) {
			selectedFoundation = (Foundation) selectedPart;
		} else {
			selectedFoundation = selectedPart.getTopContainer();
		}
		if (selectedFoundation != null) {
			switch (selectedFoundation.getSupportingType()) {
			case Foundation.BUILDING:
				dataPanel.remove(pvStationPanel);
				dataPanel.remove(cspStationPanel);
				dataPanel.add(buildingPanel, 2);
				buildingInfoPanel.update(selectedFoundation);
				final Calendar c = Heliodon.getInstance().getCalender();
				final int temp = selectedFoundation.getThermostat().getTemperature(c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY, c.get(Calendar.HOUR_OF_DAY));
				switch (Scene.getInstance().getUnit()) {
				case InternationalSystemOfUnits:
					thermostatTemperatureField.setText(temp + " \u00B0C");
					break;
				case USCustomaryUnits:
					thermostatTemperatureField.setText(Math.round(32.0 + 9.0 * temp / 5.0) + " \u00B0F");
					break;
				}
				thermostatPanel.add(adjustThermostatButton, BorderLayout.EAST);
				String s2 = selectedFoundation.toString();
				s2 = s2.substring(0, s2.indexOf(')') + 1);
				final int i1 = s2.indexOf('(');
				final int i2 = s2.indexOf(')');
				((TitledBorder) buildingPanel.getBorder()).setTitle("Building #" + s2.substring(i1 + 1, i2));
				break;
			case Foundation.PV_STATION:
				dataPanel.remove(buildingPanel);
				dataPanel.remove(cspStationPanel);
				dataPanel.add(pvStationPanel, 2);
				pvStationInfoPanel.update(selectedFoundation);
				break;
			case Foundation.CSP_STATION:
				dataPanel.remove(buildingPanel);
				dataPanel.remove(pvStationPanel);
				dataPanel.add(cspStationPanel, 2);
				cspStationInfoPanel.update(selectedFoundation);
				break;
			}
		} else {
			dataPanel.remove(buildingPanel);
			dataPanel.remove(pvStationPanel);
			dataPanel.remove(cspStationPanel);
		}
		buildingInfoPanel.repaint();
		buildingPanel.repaint();
		dataPanel.validate();
		dataPanel.repaint();

	}

	public void updateThermostat() {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart == null)
			return;
		final Foundation selectedBuilding = selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer();
		if (selectedBuilding != null) {
			final Calendar c = Heliodon.getInstance().getCalender();
			final double temp = selectedBuilding.getThermostat().getTemperature(c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY, c.get(Calendar.HOUR_OF_DAY));
			switch (Scene.getInstance().getUnit()) {
			case InternationalSystemOfUnits:
				thermostatTemperatureField.setText(temp + " \u00B0C");
				break;
			case USCustomaryUnits:
				thermostatTemperatureField.setText(Math.round(32.0 + 9.0 * temp / 5.0) + " \u00B0F");
				break;
			}
			thermostatPanel.add(adjustThermostatButton, BorderLayout.EAST);
			adjustThermostatButton.setEnabled(!Scene.getInstance().isStudentMode());
		} else {
			thermostatTemperatureField.setText(null);
			thermostatPanel.remove(adjustThermostatButton);
		}
	}

	public void update() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final String s = Scene.getInstance().getProjectName();
				timeAndLocationPanel.setBorder(createTitledBorder("Project: " + (s != null ? s : ""), true));
				buildingInfoPanel.updateArea();
				buildingInfoPanel.updateHeight();
				buildingInfoPanel.updateWindowToFloorRatio();
				buildingInfoPanel.updateSolarPanel();
				buildingInfoPanel.updateWindow();
				buildingInfoPanel.updateWall();
				SceneManager.getTaskManager().update(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						updateProperties();
						return null;
					}
				});
			}
		});
	}

	public boolean isCancelled() {
		return cancel || computeRequest;
	}

	public void cancel() {
		cancel = true;
	}

	public JComboBox<String> getCityComboBox() {
		return cityComboBox;
	}

	public void showHeatMapContrastSlider(final boolean b) {
		if (b)
			dataPanel.add(heatMapPanel);
		else
			dataPanel.remove(heatMapPanel);
		dataPanel.repaint();
	}

	public void turnOffCompute() {
		if (SceneManager.getInstance().getSolarHeatMap())
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					MainPanel.getInstance().getEnergyViewButton().setSelected(false);
				}
			});
		int numberOfHouses = 0;
		synchronized (SceneManager.getInstance()) {
			for (final HousePart part : Scene.getInstance().getParts()) {
				if (part instanceof Foundation && !part.getChildren().isEmpty() && !part.isFrozen())
					numberOfHouses++;
				if (numberOfHouses >= 2)
					break;
			}
			for (final HousePart part : Scene.getInstance().getParts())
				if (part instanceof Foundation)
					((Foundation) part).setSolarLabelValue(numberOfHouses >= 2 && !part.getChildren().isEmpty() && !part.isFrozen() ? -1 : -2);
		}

		if (SceneManager.getInstance().getSolarLand().isVisible()) {
			SceneManager.getInstance().getSolarLand().setVisible(false);
			Scene.getInstance().redrawAll();
		}
	}

	public void updateGraphs() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (selectedPart instanceof Foundation) {
					final Foundation f = (Foundation) selectedPart;
					switch (f.getSupportingType()) {
					case Foundation.BUILDING:
						constructionCostGraph.addGraph(f);
						buildingDailyEnergyGraph.addGraph(f);
						break;
					case Foundation.PV_STATION:
						pvStationDailyEnergyGraph.addGraph(f);
						break;
					case Foundation.CSP_STATION:
						cspStationDailyEnergyGraph.addGraph(f);
						break;
					}
				} else {
					constructionCostGraph.removeGraph();
					buildingDailyEnergyGraph.removeGraph();
					pvStationDailyEnergyGraph.removeGraph();
					cspStationDailyEnergyGraph.removeGraph();
				}
			}
		});
	}

	private JLabel createLabel(final String text) {
		final JLabel label = new JLabel(text);
		label.setFont(new Font(label.getFont().getName(), Font.PLAIN, label.getFont().getSize() - 2));
		return label;
	}

	private JTextField createTextField() {
		final JTextField text = new JTextField();
		text.setFont(new Font(text.getFont().getName(), Font.PLAIN, text.getFont().getSize() - 1));
		return text;
	}

	private JSpinner createSpinner(final SpinnerModel model) {
		final JSpinner spinner = new JSpinner(model);
		spinner.setFont(new Font(spinner.getFont().getName(), Font.PLAIN, spinner.getFont().getSize() - 1));
		return spinner;
	}

	static TitledBorder createTitledBorder(final String title, final boolean smaller) {
		final TitledBorder b = BorderFactory.createTitledBorder(UIManager.getBorder("TitledBorder.border"), title, TitledBorder.LEADING, TitledBorder.TOP);
		b.setTitleFont(new Font(b.getTitleFont().getFontName(), Font.PLAIN, b.getTitleFont().getSize() - (smaller ? 2 : 1)));
		return b;
	}

	public void disableDateSpinner(final boolean b) {
		disableDateSpinner = b;
	}

}
