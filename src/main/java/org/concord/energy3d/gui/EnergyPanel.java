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
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.DesignSpecs;
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
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;

public class EnergyPanel extends JPanel {

	public static final ReadOnlyColorRGBA[] solarColors = { ColorRGBA.BLUE, ColorRGBA.GREEN, ColorRGBA.YELLOW, ColorRGBA.RED };

	private static final long serialVersionUID = 1L;
	private static final EnergyPanel instance = new EnergyPanel();
	private final DecimalFormat twoDecimals = new DecimalFormat();
	private final DecimalFormat noDecimals = new DecimalFormat();
	private static boolean autoRecomputeEnergy = false;
	private Thread thread;
	private boolean computeRequest;
	private boolean cancel;
	private Object disableActionsRequester;
	private boolean alreadyRenderedHeatmap = false;
	private boolean computeEnabled = true;
	private final List<PropertyChangeListener> propertyChangeListeners = new ArrayList<PropertyChangeListener>();

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
	private final ColorBar heightBar, areaBar;
	private final JPanel thermostatPanel, heightPanel, areaPanel;
	private final JTextField thermostatTemperatureField;
	private final JButton adjustThermostatButton;
	private JPanel partPanel;
	private JPanel buildingPanel;
	private JPanel partPropertiesPanel;
	private JLabel partProperty1Label;
	private JLabel partProperty2Label;
	private JLabel partProperty3Label;
	private JLabel partProperty4Label;
	private JTextField partProperty1TextField;
	private JTextField partProperty2TextField;
	private JTextField partProperty3TextField;
	private JTextField partProperty4TextField;
	private ChangeListener latitudeChangeListener;
	private InfoPanel infoPanel;
	private ConstructionCostGraph constructionCostGraph;
	private DailyEnergyGraph dailyEnergyGraph;
	private JTabbedPane graphTabbedPane;

	public static EnergyPanel getInstance() {
		return instance;
	}

	private EnergyPanel() {

		twoDecimals.setMaximumFractionDigits(2);
		noDecimals.setMaximumFractionDigits(0);

		setLayout(new BorderLayout());
		dataPanel = new JPanel();
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
		add(new JScrollPane(dataPanel), BorderLayout.CENTER);

		final JPanel timeAndLocationPanel = new JPanel();
		timeAndLocationPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 2, 0));
		dataPanel.add(timeAndLocationPanel);
		final GridBagLayout gbl_panel_3 = new GridBagLayout();
		timeAndLocationPanel.setLayout(gbl_panel_3);

		final GridBagConstraints gbc_dateLabel = new GridBagConstraints();
		gbc_dateLabel.gridx = 0;
		gbc_dateLabel.gridy = 0;
		timeAndLocationPanel.add(createLabel("Date: "), gbc_dateLabel);

		dateSpinner = createSpinner(new SpinnerDateModel(Calendar.getInstance().getTime(), null, null, Calendar.MONTH));
		dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MMMM dd"));
		dateSpinner.addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
			@Override
			public void ancestorResized(final HierarchyEvent e) {
				dateSpinner.setMinimumSize(dateSpinner.getPreferredSize());
				dateSpinner.setPreferredSize(dateSpinner.getPreferredSize());
				dateSpinner.removeHierarchyBoundsListener(this);
			}
		});
		dateSpinner.addChangeListener(new ChangeListener() {
			boolean firstCall = true;

			@Override
			public void stateChanged(final ChangeEvent e) {
				if (firstCall) {
					firstCall = false;
					return;
				}
				if (disableActionsRequester == null) {
					SceneManager.getInstance().getUndoManager().addEdit(new ChangeDateCommand());
					final Heliodon heliodon = Heliodon.getInstance();
					heliodon.setDate((Date) dateSpinner.getValue());
					compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					Scene.getInstance().setDate(heliodon.getCalender().getTime());
					Scene.getInstance().setTreeLeaves();
					Scene.getInstance().setEdited(true);
					updateThermostat();
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
				} else {
					SceneManager.getInstance().getUndoManager().addEdit(new ChangeCityCommand());
					final Integer newLatitude = LocationData.getInstance().getLatitutes().get(cityComboBox.getSelectedItem()).intValue();
					if (newLatitude.equals(latitudeSpinner.getValue()))
						compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					else {
						setLatitude(newLatitude);
						Heliodon.getInstance().setLatitude(((Integer) latitudeSpinner.getValue()) / 180.0 * Math.PI);
						compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
					if (LocationData.getInstance().getSunshineHours().get(city) == null)
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "No sunshine data is found for " + city + ".\nSolar radiation will be overestimated.", "Warning", JOptionPane.WARNING_MESSAGE);
				}
				Scene.getInstance().setCity(city);
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

			@Override
			public void stateChanged(final ChangeEvent e) {
				// ignore the first event
				if (firstCall) {
					firstCall = false;
					return;
				}
				SceneManager.getInstance().getUndoManager().addEdit(new ChangeTimeCommand());
				final Heliodon heliodon = Heliodon.getInstance();
				heliodon.setTime((Date) timeSpinner.getValue());
				Scene.getInstance().setDate(heliodon.getCalender().getTime());
				updateWeatherData();
				updateThermostat();
				Scene.getInstance().setEdited(true);
				SceneManager.getInstance().changeSkyTexture();
				if (MainPanel.getInstance().getShadowButton().isSelected()) {
					SceneManager.getInstance().setShading(Heliodon.getInstance().isNightTime());
				} else {
					SceneManager.getInstance().setShading(false);
				}
				if (Scene.getInstance().getAlwaysComputeHeatFluxVectors() && SceneManager.getInstance().areHeatFluxVectorsVisible()) { // for now, only heat flow arrows need to call redrawAll
					SceneManager.getInstance().setHeatFluxDaily(false);
					for (final HousePart part : Scene.getInstance().getParts())
						part.drawHeatFlux();
				}
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
				SceneManager.getInstance().getUndoManager().addEdit(new ChangeLatitudeCommand());
				Heliodon.getInstance().setLatitude(((Integer) latitudeSpinner.getValue()) / 180.0 * Math.PI);
				compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
				Scene.getInstance().setEdited(true);
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

		heatMapPanel = new JPanel(new BorderLayout());
		heatMapPanel.setBorder(createTitledBorder("Heat Map Contrast", true));
		// dataPanel.add(heatMapPanel);

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
					SceneManager.getInstance().getUndoManager().addEdit(new ChangeSolarHeatMapColorContrastCommand());
					Scene.getInstance().setSolarHeatMapColorContrast(colorMapSlider.getValue());
					compute(SceneManager.getInstance().getSolarHeatMap() ? UpdateRadiation.ALWAYS : UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					Scene.getInstance().setEdited(true, false);
				}
			}
		});
		heatMapPanel.add(colorMapSlider, BorderLayout.CENTER);
		heatMapPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, heatMapPanel.getPreferredSize().height));

		partPanel = new JPanel();
		partPanel.setBorder(createTitledBorder("Part", true));
		dataPanel.add(partPanel);

		buildingPanel = new JPanel();
		buildingPanel.setBorder(createTitledBorder("Building", true));
		dataPanel.add(buildingPanel);
		buildingPanel.setLayout(new BoxLayout(buildingPanel, BoxLayout.Y_AXIS));

		final JPanel buildingSizePanel = new JPanel(new GridLayout(1, 2, 0, 0));
		buildingPanel.add(buildingSizePanel);

		// area for the selected building

		areaPanel = new JPanel(new BorderLayout());
		areaPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Area (\u33A1)", TitledBorder.LEADING, TitledBorder.TOP));
		areaPanel.setToolTipText("<html>The area of the selected building<br><b>Must be within the specified range (if any).</b></html>");
		buildingSizePanel.add(areaPanel);
		areaBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
		areaBar.setUnit("");
		areaBar.setUnitPrefix(false);
		areaBar.setVerticalLineRepresentation(false);
		areaBar.setDecimalDigits(1);
		areaBar.setToolTipText(areaPanel.getToolTipText());
		areaBar.setPreferredSize(new Dimension(100, 16));
		areaPanel.add(areaBar, BorderLayout.CENTER);

		// height for the selected building

		heightPanel = new JPanel(new BorderLayout());
		heightPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Height (m)", TitledBorder.LEADING, TitledBorder.TOP));
		heightPanel.setToolTipText("<html>The height of the selected building<br><b>Must be within the specified range (if any).</b></html>");
		buildingSizePanel.add(heightPanel);
		heightBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
		heightBar.setUnit("");
		heightBar.setUnitPrefix(false);
		heightBar.setVerticalLineRepresentation(false);
		heightBar.setDecimalDigits(1);
		heightBar.setToolTipText(heightPanel.getToolTipText());
		heightBar.setPreferredSize(new Dimension(100, 16));
		heightPanel.add(heightBar, BorderLayout.CENTER);

		// thermostat for the selected building

		thermostatPanel = new JPanel(new BorderLayout(5, 0));
		thermostatPanel.add(createLabel("   Thermostat: "), BorderLayout.WEST);
		thermostatTemperatureField = createTextField();
		thermostatTemperatureField.setEnabled(false);
		thermostatTemperatureField.setBackground(Color.WHITE);
		thermostatPanel.add(thermostatTemperatureField, BorderLayout.CENTER);
		adjustThermostatButton = new JButton("Adjust");
		adjustThermostatButton.setFont(new Font(adjustThermostatButton.getFont().getName(), Font.PLAIN, adjustThermostatButton.getFont().getSize() - 2));
		adjustThermostatButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
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
			}
		});
		buildingPanel.add(thermostatPanel);

		progressBar = new JProgressBar();
		add(progressBar, BorderLayout.SOUTH);

		JPanel target = buildingPanel;
		target.setMaximumSize(new Dimension(target.getMaximumSize().width, target.getPreferredSize().height));

		graphTabbedPane = new JTabbedPane();
		graphTabbedPane.setFont(new Font(graphTabbedPane.getFont().getName(), Font.PLAIN, graphTabbedPane.getFont().getSize() - 1));
		graphTabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (graphTabbedPane.getSelectedComponent() == dailyEnergyGraph) {
					if (SceneManager.getInstance().getSolarHeatMap()) {
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Foundation) {
							EnergyPanel.getInstance().getDailyEnergyGraph().addGraph((Foundation) selectedPart);
						} else {
							EnergyPanel.getInstance().getDailyEnergyGraph().removeGraph();
						}
					}
				}
			}
		});
		dataPanel.add(graphTabbedPane);
		dataPanel.add(Box.createVerticalGlue());

		constructionCostGraph = new ConstructionCostGraph();
		graphTabbedPane.add("Cost", constructionCostGraph);

		dailyEnergyGraph = new DailyEnergyGraph();
		graphTabbedPane.add("Energy", dailyEnergyGraph);

		infoPanel = new InfoPanel();
		graphTabbedPane.add("Info", infoPanel);

		target = partPanel;
		target.setMaximumSize(new Dimension(target.getMaximumSize().width, target.getPreferredSize().height));
		partPanel.setLayout(new BoxLayout(partPanel, BoxLayout.Y_AXIS));

		partPropertiesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		partPanel.add(partPropertiesPanel);

		partProperty1Label = createLabel("Width:");
		partPropertiesPanel.add(partProperty1Label);

		partProperty1TextField = createTextField();
		partProperty1TextField.setEnabled(false);
		partProperty1TextField.setBackground(Color.WHITE);
		partPropertiesPanel.add(partProperty1TextField);
		partProperty1TextField.setColumns(4);

		partProperty2Label = createLabel("Height:");
		partPropertiesPanel.add(partProperty2Label);

		partProperty2TextField = createTextField();
		partProperty2TextField.setEnabled(false);
		partProperty2TextField.setBackground(Color.WHITE);
		partPropertiesPanel.add(partProperty2TextField);
		partProperty2TextField.setColumns(4);

		partProperty3Label = createLabel("Insolation:");
		partPropertiesPanel.add(partProperty3Label);

		partProperty3TextField = createTextField();
		partProperty3TextField.setEnabled(false);
		partProperty3TextField.setBackground(Color.WHITE);
		partPropertiesPanel.add(partProperty3TextField);
		partProperty3TextField.setColumns(4);

		partProperty4Label = createLabel("");
		partProperty4TextField = createTextField();
		partProperty4TextField.setEnabled(false);
		partProperty4TextField.setBackground(Color.WHITE);
		partProperty4TextField.setColumns(4);

	}

	public void compute(final UpdateRadiation updateRadiation) {
		if (!computeEnabled)
			return;
		updateWeatherData(); // TODO: There got to be a better way to do this
		if (thread != null && thread.isAlive())
			computeRequest = true;
		else {
			((Component) SceneManager.getInstance().getCanvas()).setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			thread = new Thread("Energy Computer") {
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
						} finally {
							((Component) SceneManager.getInstance().getCanvas()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
						EventQueue.invokeLater(new Runnable() { // must run this Swing UI update in the event queue to avoid a possible deadlock
							@Override
							public void run() {
								progress(0);
								if (SceneManager.getInstance().getSolarHeatMap()) {
									EnergyPanel.getInstance().getGraphTabbedPane().setSelectedComponent(EnergyPanel.getInstance().getDailyEnergyGraph());
									final HousePart p = SceneManager.getInstance().getSelectedPart();
									if (p instanceof Foundation) {
										dailyEnergyGraph.addGraph((Foundation) p);
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
			System.out.println("PropertiesPanel.computeNow()");
			progressBar.setValue(0);
			progressBar.setStringPainted(false);

			synchronized (SceneManager.getInstance()) {
				updateWeatherData();
				final int timeStep = SolarRadiation.getInstance().getTimeStep();
				for (final HousePart part : Scene.getInstance().getParts())
					part.setHeatLoss(new double[SolarRadiation.MINUTES_OF_DAY / timeStep]);
				SolarRadiation.getInstance().compute();
				final Calendar c = (Calendar) Heliodon.getInstance().getCalender().clone();
				HeatLoad.getInstance().computeEnergyToday(c);
				SolarRadiation.getInstance().computeTotalEnergyForBuildings();
				notifyPropertyChangeListeners(new PropertyChangeEvent(EnergyPanel.this, "Energy calculation completed", 0, 1));
				updateProperties();
				Scene.getInstance().setTreeLeaves();
			}

			progressBar.setValue(100);

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

	public JTabbedPane getGraphTabbedPane() {
		return graphTabbedPane;
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

	public InfoPanel getInfoPanel() {
		return infoPanel;
	}

	public ConstructionCostGraph getConstructionCostGraph() {
		return constructionCostGraph;
	}

	public DailyEnergyGraph getDailyEnergyGraph() {
		return dailyEnergyGraph;
	}

	/** call when loading a new file */
	public void clearAllGraphs() {
		constructionCostGraph.removeGraph();
		dailyEnergyGraph.removeGraph();
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

	public static void setAutoRecomputeEnergy(final boolean on) {
		autoRecomputeEnergy = on;
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

	public void updateProperties() {
		final boolean energyViewShown = MainPanel.getInstance().getEnergyViewButton().isSelected();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();

		if (selectedPart instanceof Tree) {
			partProperty1Label.setText("Spread:");
			partProperty2Label.setText("Height:");
			partProperty3Label.setText("Species:");
			partPropertiesPanel.add(partProperty3Label);
			partPropertiesPanel.add(partProperty3TextField);
			partPropertiesPanel.remove(partProperty4Label);
			partPropertiesPanel.remove(partProperty4TextField);
		} else if (selectedPart instanceof Sensor) {
			partProperty1Label.setText("X:");
			partProperty2Label.setText("Y:");
			partProperty3Label.setText("Z:");
			partProperty4Label.setText("Data:");
			partPropertiesPanel.add(partProperty3Label);
			partPropertiesPanel.add(partProperty3TextField);
			partPropertiesPanel.add(partProperty4Label);
			partPropertiesPanel.add(partProperty4TextField);
		} else {
			partProperty1Label.setText("Width:");
			partProperty2Label.setText("Height:");
			partPropertiesPanel.remove(partProperty3Label);
			partPropertiesPanel.remove(partProperty3TextField);
			partPropertiesPanel.remove(partProperty4Label);
			partPropertiesPanel.remove(partProperty4TextField);
		}
		partPropertiesPanel.revalidate();

		((TitledBorder) partPanel.getBorder()).setTitle("Part" + (selectedPart == null ? "" : (" - " + selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1))));
		partPanel.repaint();

		if (!energyViewShown || selectedPart == null || selectedPart instanceof Door || selectedPart instanceof Foundation)
			partProperty3TextField.setText("");
		else {
			if (selectedPart instanceof Sensor) {
				final String light = twoDecimals.format(selectedPart.getSolarPotentialToday() / selectedPart.getArea());
				final String heatFlux = twoDecimals.format(selectedPart.getTotalHeatLoss() / selectedPart.getArea());
				partProperty4TextField.setText(light + ", " + heatFlux);
				partProperty4TextField.setToolTipText("Light sensor: " + light + ", heat flux sensor: " + heatFlux);
			} else {
				partProperty3TextField.setText(twoDecimals.format(selectedPart.getSolarPotentialToday()));
			}
		}

		if (selectedPart != null && !(selectedPart instanceof Roof || selectedPart instanceof Floor || selectedPart instanceof Human)) {
			double meterToFeet = 1;
			switch (Scene.getInstance().getUnit()) {
			case InternationalSystemOfUnits:
				meterToFeet = 1;
				break;
			case USCustomaryUnits:
				meterToFeet = 3.28084;
				break;
			}
			if (selectedPart instanceof SolarPanel) {
				partProperty1TextField.setText(twoDecimals.format(SolarPanel.WIDTH * meterToFeet));
				partProperty2TextField.setText(twoDecimals.format(SolarPanel.HEIGHT * meterToFeet));
			} else if (selectedPart instanceof Sensor) {
				final ReadOnlyVector3 v = ((Sensor) selectedPart).getAbsPoint(0);
				partProperty1TextField.setText(twoDecimals.format(v.getX() * Scene.getInstance().getAnnotationScale() * meterToFeet));
				partProperty2TextField.setText(twoDecimals.format(v.getY() * Scene.getInstance().getAnnotationScale() * meterToFeet));
				partProperty3TextField.setText(twoDecimals.format(v.getZ() * Scene.getInstance().getAnnotationScale() * meterToFeet));
			} else if (selectedPart instanceof Tree) {
				final Tree tree = (Tree) selectedPart;
				partProperty1TextField.setText(twoDecimals.format(tree.getWidth() * Scene.getInstance().getAnnotationScale() * meterToFeet));
				partProperty2TextField.setText(twoDecimals.format(tree.getHeight() * Scene.getInstance().getAnnotationScale() * meterToFeet));
				partProperty3TextField.setText(tree.getTreeName());
			} else {
				partProperty1TextField.setText(twoDecimals.format(selectedPart.getAbsPoint(0).distance(selectedPart.getAbsPoint(2)) * Scene.getInstance().getAnnotationScale() * meterToFeet));
				partProperty2TextField.setText(twoDecimals.format(selectedPart.getAbsPoint(0).distance(selectedPart.getAbsPoint(1)) * Scene.getInstance().getAnnotationScale() * meterToFeet));
			}
		} else {
			partProperty1TextField.setText("");
			partProperty2TextField.setText("");
		}

		final Foundation selectedBuilding;
		if (selectedPart == null)
			selectedBuilding = null;
		else if (selectedPart instanceof Foundation)
			selectedBuilding = (Foundation) selectedPart;
		else
			selectedBuilding = selectedPart.getTopContainer();

		if (selectedBuilding != null) {
			final double[] buildingGeometry = selectedBuilding.getBuildingGeometry();
			if (buildingGeometry != null) {
				switch (Scene.getInstance().getUnit()) {
				case InternationalSystemOfUnits:
					heightBar.setValue((float) buildingGeometry[0]);
					areaBar.setValue((float) buildingGeometry[1]);
					break;
				case USCustomaryUnits:
					heightBar.setValue((float) (buildingGeometry[0] * 3.28084));
					areaBar.setValue((float) (buildingGeometry[1] * 3.28084 * 3.28084));
					break;
				}
			} else {
				heightBar.setValue(0);
				areaBar.setValue(0);
			}
			final Calendar c = Heliodon.getInstance().getCalender();
			int temp = selectedBuilding.getThermostat().getTemperature(c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY, c.get(Calendar.HOUR_OF_DAY));
			switch (Scene.getInstance().getUnit()) {
			case InternationalSystemOfUnits:
				thermostatTemperatureField.setText(temp + " \u00B0C");
				break;
			case USCustomaryUnits:
				thermostatTemperatureField.setText(Math.round(32.0 + 9.0 * temp / 5.0) + " \u00B0F");
				break;
			}
			thermostatPanel.add(adjustThermostatButton, BorderLayout.EAST);
			String s2 = selectedBuilding.toString();
			s2 = s2.substring(0, s2.indexOf(')') + 1);
			int i1 = s2.indexOf('(');
			int i2 = s2.indexOf(')');
			((TitledBorder) buildingPanel.getBorder()).setTitle("Building #" + s2.substring(i1 + 1, i2));
		} else {
			heightBar.setValue(0);
			areaBar.setValue(0);
			thermostatTemperatureField.setText(null);
			thermostatPanel.remove(adjustThermostatButton);
			((TitledBorder) buildingPanel.getBorder()).setTitle("Building");
		}
		buildingPanel.repaint();

		heightBar.repaint();
		areaBar.repaint();

	}

	public void updateThermostat() {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart == null)
			return;
		final Foundation selectedBuilding = selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer();
		if (selectedBuilding != null) {
			final Calendar c = Heliodon.getInstance().getCalender();
			double temp = selectedBuilding.getThermostat().getTemperature(c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY, c.get(Calendar.HOUR_OF_DAY));
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
		updateAreaBar();
		updateHeightBar();
		updateProperties();
	}

	/** Apply this when the UI is set programmatically (not by the user) */
	public void requestDisableActions(final Object requester) {
		disableActionsRequester = requester;
	}

	public Object getDisableActionsRequester() {
		return disableActionsRequester;
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

	public void updateAreaBar() {
		final DesignSpecs specs = Scene.getInstance().getDesignSpecs();
		final double r = 3.28084 * 3.28084;
		String t = "Area (";
		switch (Scene.getInstance().getUnit()) {
		case InternationalSystemOfUnits:
			if (specs.isAreaEnabled())
				t += twoDecimals.format(specs.getMinimumArea()) + " - " + twoDecimals.format(specs.getMaximumArea()) + " ";
			t += "m\u00B2)";
			areaBar.setMinimum(specs.getMinimumArea());
			areaBar.setMaximum(specs.getMaximumArea());
			break;
		case USCustomaryUnits:
			if (specs.isAreaEnabled())
				t += noDecimals.format(specs.getMinimumArea() * r) + " - " + noDecimals.format(specs.getMaximumArea() * r) + " ";
			t += "ft\u00B2)";
			areaBar.setMinimum(specs.getMinimumArea() * r);
			areaBar.setMaximum(specs.getMaximumArea() * r);
			break;
		}
		areaPanel.setBorder(createTitledBorder(t, true));
		areaBar.setEnabled(specs.isAreaEnabled());
		areaBar.repaint();
	}

	public void updateHeightBar() {
		final DesignSpecs specs = Scene.getInstance().getDesignSpecs();
		final double r = 3.28084;
		String t = "Height (";
		switch (Scene.getInstance().getUnit()) {
		case InternationalSystemOfUnits:
			if (specs.isHeightEnabled())
				t += twoDecimals.format(specs.getMinimumHeight()) + " - " + twoDecimals.format(specs.getMaximumHeight()) + " ";
			t += "m)";
			heightBar.setMinimum(specs.getMinimumHeight());
			heightBar.setMaximum(specs.getMaximumHeight());
			break;
		case USCustomaryUnits:
			if (specs.isHeightEnabled())
				t += noDecimals.format(specs.getMinimumHeight() * r) + " - " + noDecimals.format(specs.getMaximumHeight() * r) + " ";
			t += "ft)";
			heightBar.setMinimum(specs.getMinimumHeight() * r);
			heightBar.setMaximum(specs.getMaximumHeight() * r);
			break;
		}
		heightPanel.setBorder(createTitledBorder(t, true));
		heightBar.setEnabled(specs.isHeightEnabled());
		heightBar.repaint();
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
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart instanceof Foundation) {
			final Foundation foundation = (Foundation) selectedPart;
			if (foundation != constructionCostGraph.getBuilding()) {
				constructionCostGraph.addGraph(foundation);
			}
			if (foundation != dailyEnergyGraph.getBuilding()) {
				if (SceneManager.getInstance().getSolarHeatMap())
					dailyEnergyGraph.addGraph(foundation);
			}
		} else {
			constructionCostGraph.removeGraph();
			dailyEnergyGraph.removeGraph();
		}
	}

	private JLabel createLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(new Font(label.getFont().getName(), Font.PLAIN, label.getFont().getSize() - 2));
		return label;
	}

	private JTextField createTextField() {
		JTextField text = new JTextField();
		text.setFont(new Font(text.getFont().getName(), Font.PLAIN, text.getFont().getSize() - 1));
		return text;
	}

	private JSpinner createSpinner(SpinnerModel model) {
		JSpinner spinner = new JSpinner(model);
		spinner.setFont(new Font(spinner.getFont().getName(), Font.PLAIN, spinner.getFont().getSize() - 1));
		return spinner;
	}

	TitledBorder createTitledBorder(String title, boolean smaller) {
		TitledBorder b = BorderFactory.createTitledBorder(UIManager.getBorder("TitledBorder.border"), title, TitledBorder.LEADING, TitledBorder.TOP);
		b.setTitleFont(new Font(b.getTitleFont().getFontName(), Font.PLAIN, b.getTitleFont().getSize() - (smaller ? 2 : 1)));
		return b;
	}

}
