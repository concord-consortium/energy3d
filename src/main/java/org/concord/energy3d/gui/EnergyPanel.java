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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
import javax.swing.text.DefaultCaret;

import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FoundationPolygon;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.NodeState;
import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.UserData;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.AnnualEnvironmentalTemperature;
import org.concord.energy3d.simulation.DailyEnvironmentalTemperature;
import org.concord.energy3d.simulation.HeatLoad;
import org.concord.energy3d.simulation.LocationData;
import org.concord.energy3d.simulation.MonthlySunshineHours;
import org.concord.energy3d.simulation.SolarRadiation;
import org.concord.energy3d.simulation.Weather;
import org.concord.energy3d.undo.ChangeCityCommand;
import org.concord.energy3d.undo.ChangeDateCommand;
import org.concord.energy3d.undo.ChangeLatitudeCommand;
import org.concord.energy3d.undo.ChangeSolarHeatMapColorContrastCommand;
import org.concord.energy3d.undo.ChangeTimeCommand;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;

public class EnergyPanel extends JPanel {

	public static final ReadOnlyColorRGBA[] solarColors = { ColorRGBA.BLUE, ColorRGBA.GREEN, ColorRGBA.YELLOW, ColorRGBA.RED };
	public static final DecimalFormat NO_DECIMAL = new DecimalFormat();
	public static final DecimalFormat ONE_DECIMAL = new DecimalFormat();
	public static final DecimalFormat TWO_DECIMALS = new DecimalFormat();

	private static final long serialVersionUID = 1L;
	private static final EnergyPanel instance = new EnergyPanel();
	private static boolean autoRecomputeEnergy;
	private volatile boolean computeRequest;
	private volatile boolean computing;
	private volatile boolean cancel;
	private boolean alreadyRenderedHeatmap;
	private boolean computeEnabled = true;

	public enum UpdateRadiation {
		ALWAYS, ONLY_IF_SLECTED_IN_GUI
	};

	private final JPanel dataPanel;
	private final JComboBox<String> regionComboBox;
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
	private BuildingCostGraph buildingCostGraph;
	private PvProjectCostGraph pvProjectCostGraph;
	private CspProjectCostGraph cspProjectCostGraph;
	private BuildingDailyEnergyGraph buildingDailyEnergyGraph;
	private PvStationDailyEnergyGraph pvStationDailyEnergyGraph;
	private CspStationDailyEnergyGraph cspStationDailyEnergyGraph;
	private BuildingInfoPanel buildingInfoPanel;
	private PvStationInfoPanel pvStationInfoPanel;
	private CspStationInfoPanel cspStationInfoPanel;
	private JTabbedPane buildingTabbedPane, pvStationTabbedPane, cspStationTabbedPane, instructionTabbedPane;
	private JPanel buildingPanel, pvStationPanel, cspStationPanel, instructionPanel;
	private final MyEditorPane[] instructionSheets = new MyEditorPane[3];
	private boolean disableDateSpinner;
	private long computingStartMillis;

	public static EnergyPanel getInstance() {
		return instance;
	}

	private EnergyPanel() {

		NO_DECIMAL.setMaximumFractionDigits(0);
		ONE_DECIMAL.setMaximumFractionDigits(1);
		TWO_DECIMALS.setMaximumFractionDigits(2);

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
		final JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "MMMM dd");
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
						if (c0.get(Calendar.MONTH) == c1.get(Calendar.MONTH) && c0.get(Calendar.DAY_OF_MONTH) == c1.get(Calendar.DAY_OF_MONTH)) {
							return;
						}
					}
					Scene.getInstance().setDate(d);
					Heliodon.getInstance().setDate(d);
					clearRadiationHeatMap();
					Scene.getInstance().updateTreeLeaves();
					Scene.getInstance().updateTrackables();
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
				regionComboBox.setSelectedItem("");
			}
		};

		Arrays.sort(LocationData.getInstance().getCities());
		regionComboBox = new JComboBox<String>();
		regionComboBox.setFont(new Font(regionComboBox.getFont().getName(), Font.PLAIN, regionComboBox.getFont().getSize() - 2));
		regionComboBox.setModel(new DefaultComboBoxModel<String>(LocationData.getInstance().getCities()));
		regionComboBox.setSelectedItem("Boston, MA");
		regionComboBox.setMaximumRowCount(15);
		regionComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final String city = (String) regionComboBox.getSelectedItem();
				if (city.equals("")) {
					clearRadiationHeatMap();
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "No region is selected.\nEnergy simulation will not be accurate.", "Warning", JOptionPane.WARNING_MESSAGE);
					Scene.getInstance().setCity(city);
				} else {
					final ChangeCityCommand c = new ChangeCityCommand();
					setLatitude((int) LocationData.getInstance().getLatitudes().get(regionComboBox.getSelectedItem()).floatValue());
					clearRadiationHeatMap();
					Scene.getInstance().setCity(city);
					SceneManager.getInstance().getUndoManager().addEdit(c);
					final LocationData ld = LocationData.getInstance();
					regionComboBox.setToolTipText("<html>(" + ld.getLatitudes().get(city) + "\u00B0, " + ld.getLongitudes().get(city) + "\u00B0, " + ld.getAltitudes().get(city).intValue() + "m)<br>Use Edit>Set Region... to select country and region.</html>");
				}
				Scene.getInstance().updateTrackables();
				Scene.getInstance().updateTreeLeaves();
				Scene.getInstance().setEdited(true);
			}
		});
		regionComboBox.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (Util.isRightClick(e)) {
					final JPopupMenu popupMenu = new JPopupMenu();
					final JMenuItem mi = new JMenuItem("Show Map...");
					mi.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							new GlobalMap(MainFrame.getInstance()).setVisible(true);
						}
					});
					popupMenu.add(mi);
					popupMenu.pack();
					popupMenu.show(regionComboBox, 0, 0);
				}
			}
		});

		final GridBagConstraints gbc_cityComboBox = new GridBagConstraints();
		gbc_cityComboBox.gridwidth = 2;
		gbc_cityComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_cityComboBox.gridx = 2;
		gbc_cityComboBox.gridy = 0;
		timeAndLocationPanel.add(regionComboBox, gbc_cityComboBox);

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
					if (c0.get(Calendar.HOUR_OF_DAY) == c1.get(Calendar.HOUR_OF_DAY) && c0.get(Calendar.MINUTE) == c1.get(Calendar.MINUTE)) {
						return;
					}
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
							for (final HousePart part : Scene.getInstance().getParts()) {
								part.drawHeatFlux();
							}
							return null;
						}
					});
				}
				Scene.getInstance().updateTrackables();
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
				clearRadiationHeatMap();
				Scene.getInstance().updateTrackables();
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
		outsideTemperatureField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (Util.isRightClick(e)) {
					final JPopupMenu popupMenu = new JPopupMenu();
					JMenuItem mi = new JMenuItem("Daily Environmental Temperature...");
					mi.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							final String city = (String) regionComboBox.getSelectedItem();
							if ("".equals(city)) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
							new DailyEnvironmentalTemperature().showDialog();
						}
					});
					popupMenu.add(mi);
					mi = new JMenuItem("Annual Environmental Temperature...");
					mi.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							final String city = (String) regionComboBox.getSelectedItem();
							if ("".equals(city)) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
							new AnnualEnvironmentalTemperature().showDialog();
						}
					});
					popupMenu.add(mi);
					popupMenu.pack();
					popupMenu.show(outsideTemperatureField, 0, 0);
				}
			}
		});
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
		sunshineHoursField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (Util.isRightClick(e)) {
					final JPopupMenu popupMenu = new JPopupMenu();
					final JMenuItem mi = new JMenuItem("Monthly Sunshine Hours...");
					mi.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							final String city = (String) regionComboBox.getSelectedItem();
							if ("".equals(city)) {
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
							new MonthlySunshineHours().showDialog();
						}
					});
					popupMenu.add(mi);
					popupMenu.pack();
					popupMenu.show(sunshineHoursField, 0, 0);
				}
			}
		});
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

		final MouseListener propertyTextFieldMouseListener = new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				final HousePart part = SceneManager.getInstance().getSelectedPart();
				if (part != null) {
					if (Util.isRightClick(e)) {
						final JPopupMenu popupMenu = new JPopupMenu();
						final JMenuItem mi = new JMenuItem("Show More Properties...");
						mi.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(final ActionEvent e) {
								final JDialog d = PropertiesDialogFactory.getDialog();
								if (d != null) {
									d.setLocationRelativeTo(popupMenu.getInvoker());
									d.setVisible(true);
								}
							}
						});
						popupMenu.add(mi);
						popupMenu.pack();
						popupMenu.show(e.getComponent(), 0, 0);
					}
				}
			}
		};

		final JPanel fieldPanel = new JPanel(new GridLayout(3, 1, 2, 2));
		partPanel.add(fieldPanel, BorderLayout.CENTER);
		partProperty1TextField = createTextField();
		partProperty1TextField.addMouseListener(propertyTextFieldMouseListener);
		partProperty1TextField.setEnabled(false);
		partProperty1TextField.setBackground(Color.WHITE);
		fieldPanel.add(partProperty1TextField);
		partProperty2TextField = createTextField();
		partProperty2TextField.addMouseListener(propertyTextFieldMouseListener);
		partProperty2TextField.setEnabled(false);
		partProperty2TextField.setBackground(Color.WHITE);
		fieldPanel.add(partProperty2TextField);
		partProperty3TextField = createTextField();
		partProperty3TextField.addMouseListener(propertyTextFieldMouseListener);
		partProperty3TextField.setEnabled(false);
		partProperty3TextField.setBackground(Color.WHITE);
		fieldPanel.add(partProperty3TextField);

		// pv station panel
		pvStationPanel = new JPanel();
		pvStationPanel.setBorder(createTitledBorder("Photovoltaic Solar Power System", true));
		pvStationPanel.setLayout(new BoxLayout(pvStationPanel, BoxLayout.Y_AXIS));

		pvStationTabbedPane = new JTabbedPane();
		pvStationTabbedPane.setFont(new Font(pvStationTabbedPane.getFont().getName(), Font.PLAIN, pvStationTabbedPane.getFont().getSize() - 1));
		pvStationPanel.add(pvStationTabbedPane);

		pvStationInfoPanel = new PvStationInfoPanel();
		pvStationTabbedPane.add("Info", pvStationInfoPanel);

		pvProjectCostGraph = new PvProjectCostGraph(); // cost graph
		pvStationTabbedPane.add("Cost", pvProjectCostGraph);

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
		cspStationPanel.setBorder(createTitledBorder("Concentrated Solar Power System", true));
		cspStationPanel.setLayout(new BoxLayout(cspStationPanel, BoxLayout.Y_AXIS));

		cspStationTabbedPane = new JTabbedPane();
		cspStationTabbedPane.setFont(new Font(cspStationTabbedPane.getFont().getName(), Font.PLAIN, cspStationTabbedPane.getFont().getSize() - 1));
		cspStationPanel.add(cspStationTabbedPane);

		cspStationInfoPanel = new CspStationInfoPanel();
		cspStationTabbedPane.add("Info", cspStationInfoPanel);

		cspProjectCostGraph = new CspProjectCostGraph(); // cost graph
		cspStationTabbedPane.add("Cost", cspProjectCostGraph);

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
				if (selectedPart == null) {
					return;
				}
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

		buildingCostGraph = new BuildingCostGraph(); // cost graph
		buildingTabbedPane.add("Cost", buildingCostGraph);

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

		// instruction panel

		instructionPanel = new JPanel();
		instructionPanel.setBorder(createTitledBorder("Instruction & Documentation", true));
		instructionPanel.setLayout(new BoxLayout(instructionPanel, BoxLayout.Y_AXIS));
		dataPanel.add(instructionPanel);

		instructionTabbedPane = new JTabbedPane();
		instructionTabbedPane.setFont(new Font(instructionTabbedPane.getFont().getName(), Font.PLAIN, instructionTabbedPane.getFont().getSize() - 1));
		instructionPanel.add(instructionTabbedPane);

		for (int i = 0; i < instructionSheets.length; i++) {
			instructionSheets[i] = new MyEditorPane(i);
			instructionSheets[i].setContentType("text/plain");
			((DefaultCaret) instructionSheets[i].getEditorPane().getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
			final JScrollPane scroller = new JScrollPane(instructionSheets[i].getEditorPane());
			scroller.setPreferredSize(new Dimension(200, 220)); // somehow setting a preferred size of the scroller triggers the line wrapping of JEditorPane
			instructionTabbedPane.add(scroller, "Sheet " + (i + 1));
		}

		// heat map slider and progress bar

		heatMapPanel = new JPanel(new BorderLayout());
		heatMapPanel.setBorder(createTitledBorder("Solar Irradiance Heat Map Contrast", true));

		colorMapSlider = new MySlider();
		colorMapSlider.setToolTipText("<html>Increase or decrease the color contrast of the solar irradiance heat map.</html>");
		colorMapSlider.setMinimum(15);
		colorMapSlider.setMaximum(95);
		colorMapSlider.setMinimumSize(colorMapSlider.getPreferredSize());
		colorMapSlider.setSnapToTicks(true);
		colorMapSlider.setMinorTickSpacing(1);
		colorMapSlider.setMajorTickSpacing(5);
		// colorMapSlider.setFocusable(false); // set it false will disable keyboard interaction with the slider
		colorMapSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (!colorMapSlider.getValueIsAdjusting()) {
					((Component) SceneManager.getInstance().getCanvas()).setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					final ChangeSolarHeatMapColorContrastCommand c = new ChangeSolarHeatMapColorContrastCommand();
					Scene.getInstance().setSolarHeatMapColorContrast(colorMapSlider.getValue());
					SceneManager.getTaskManager().update(new Callable<Object>() {
						@Override
						public Object call() throws Exception {
							// compute(SceneManager.getInstance().getSolarHeatMap() ? UpdateRadiation.ALWAYS : UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
							SolarRadiation.getInstance().updateTextures();
							SceneManager.getInstance().refresh();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									((Component) SceneManager.getInstance().getCanvas()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
								}
							});
							return null;
						}
					});
					Scene.getInstance().setEdited(true);
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
		if (!computeEnabled) {
			return;
		}
		computingStartMillis = System.currentTimeMillis();
		EventQueue.invokeLater(new Runnable() { // must run this Swing UI update in the event queue to avoid a possible deadlock
			@Override
			public void run() {
				updateWeatherData(); // TODO: There got to be a better way to do this.
				((Component) SceneManager.getInstance().getCanvas()).setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			}
		});
		final boolean doCompute = updateRadiation == UpdateRadiation.ALWAYS || (SceneManager.getInstance().getSolarHeatMap() && (!alreadyRenderedHeatmap || autoRecomputeEnergy));
		if (!doCompute && computing) {
			cancel();
			return;
		}
		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				computing = true;
				computeRequest = false;
				cancel = false;
				try {
					if (doCompute) {
						alreadyRenderedHeatmap = true;
						computeNow();
						if (!cancel) {
							SceneManager.getInstance().getSolarLand().setVisible(Scene.getInstance().getSolarMapForLand());
							SceneManager.getInstance().refresh();
						} else if (!autoRecomputeEnergy) {
							turnOffCompute();
						}
					} else {
						turnOffCompute();
					}
				} catch (final Throwable e) {
					e.printStackTrace();
					Util.reportError(e);
					return null;
				} finally {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							((Component) SceneManager.getInstance().getCanvas()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
					});
				}
				EventQueue.invokeLater(new Runnable() { // must run this Swing UI update in the event queue to avoid a possible deadlock
					@Override
					public void run() {
						progress(0);
						if (SceneManager.getInstance().getSolarHeatMap()) {
							final HousePart p = SceneManager.getInstance().getSelectedPart();
							if (p instanceof Foundation) {
								final Foundation f = (Foundation) p;
								switch (f.getProjectType()) {
								case Foundation.TYPE_BUILDING:
									Util.setSilently(buildingTabbedPane, buildingDailyEnergyGraph);
									buildingDailyEnergyGraph.addGraph(f);
									TimeSeriesLogger.getInstance().logAnalysis(buildingDailyEnergyGraph);
									break;
								case Foundation.TYPE_PV_STATION:
									Util.setSilently(pvStationTabbedPane, pvStationDailyEnergyGraph);
									pvStationDailyEnergyGraph.addGraph(f);
									TimeSeriesLogger.getInstance().logAnalysis(pvStationDailyEnergyGraph);
									break;
								case Foundation.TYPE_CSP_STATION:
									Util.setSilently(cspStationTabbedPane, cspStationDailyEnergyGraph);
									cspStationDailyEnergyGraph.addGraph(f);
									TimeSeriesLogger.getInstance().logAnalysis(cspStationDailyEnergyGraph);
									break;
								}
							}
						}
						computing = false;
					}
				});
				return null;
			}
		});
	}

	public void computeNow() {
		try {
			System.out.println("EnergyPanel.computeNow()");
			cancel = false;
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					progressBar.setValue(0);
					progressBar.setStringPainted(false);
				}
			});

			Scene.getInstance().fixProblems(false);

			final int timeStep = Scene.getInstance().getTimeStep();
			for (final HousePart part : Scene.getInstance().getParts()) {
				part.setHeatLoss(new double[SolarRadiation.MINUTES_OF_DAY / timeStep]);
			}
			SolarRadiation.getInstance().compute();
			final Calendar c = (Calendar) Heliodon.getInstance().getCalendar().clone();
			HeatLoad.getInstance().computeEnergyToday(c);
			SolarRadiation.getInstance().computeEnergyOfToday();
			Scene.getInstance().updateTreeLeaves();
			Scene.getInstance().updateLabels(); // we can't call Scene.getInstance().redrawAll() here as it will screw up the radiation texture

			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					updateWeatherData();
					updateProperties();
					progressBar.setValue(100);
					progressBar.setString("100% (" + ONE_DECIMAL.format((System.currentTimeMillis() - computingStartMillis) * 0.001) + " seconds)");
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
		final String city = (String) regionComboBox.getSelectedItem();
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
			final int month = Heliodon.getInstance().getCalendar().get(Calendar.MONTH);
			try {
				sunshineHoursField.setText(Math.round(sunshineHours.get(city)[month] / 30.0) + " hours");
			} catch (final Exception e) {
				Util.reportError(e);
			}
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

	public BuildingInfoPanel getBuildingInfoPanel() {
		return buildingInfoPanel;
	}

	public PvStationInfoPanel getPvStationInfoPanel() {
		return pvStationInfoPanel;
	}

	public CspStationInfoPanel getCspStationInfoPanel() {
		return cspStationInfoPanel;
	}

	public BuildingCostGraph getBuildingCostGraph() {
		return buildingCostGraph;
	}

	public PvProjectCostGraph getPvProjectCostGraph() {
		return pvProjectCostGraph;
	}

	public CspProjectCostGraph getCspProjectCostGraph() {
		return cspProjectCostGraph;
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
		buildingCostGraph.removeGraph();
		pvProjectCostGraph.removeGraph();
		cspProjectCostGraph.removeGraph();
		buildingDailyEnergyGraph.removeGraph();
		pvStationDailyEnergyGraph.removeGraph();
		cspStationDailyEnergyGraph.removeGraph();
	}

	public void selectInstructionSheet(final int i) {
		if (i >= 0 && i < instructionSheets.length) {
			instructionTabbedPane.setSelectedIndex(i);
		}
	}

	public void progress(final int percentage) {
		if (percentage == 0) {
			progressBar.setValue(0);
			progressBar.setStringPainted(false);
		} else {
			progressBar.setValue(percentage);
			final double t = (System.currentTimeMillis() - computingStartMillis) * 0.001;
			final double remainingTime = t * (100.0 / percentage - 1.0);
			String remainingTimeString;
			if (remainingTime > 300) {
				remainingTimeString = remainingTime < 0.1 ? "" : " down, " + ONE_DECIMAL.format(remainingTime / 60) + "m to go";
			} else {
				remainingTimeString = remainingTime < 0.1 ? "" : " down, " + ONE_DECIMAL.format(remainingTime) + "s to go";
			}
			progressBar.setString(Math.min(100, percentage) + "% (" + ONE_DECIMAL.format(t) + "s" + remainingTimeString + ")");
			progressBar.setStringPainted(true);
		}
		progressBar.repaint();
	}

	public void setCity(final String city) {
		regionComboBox.setSelectedItem(city);
		regionComboBox.repaint(); // in some cases, this must be called in order to update the view
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

	// As this method may be called from a non-Event-Queue thread, updating GUI must be done through invokeLater.
	public void updateProperties() {

		// update part properties

		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		final boolean energyViewShown = MainPanel.getInstance().getEnergyViewButton().isSelected();
		final double meterToFoot;
		final String lengthUnit;
		switch (Scene.getInstance().getUnit()) {
		case USCustomaryUnits:
			meterToFoot = 3.28084;
			lengthUnit = "ft";
			break;
		default:
			meterToFoot = 1;
			lengthUnit = "m";
		}
		final double scale = Scene.getInstance().getAnnotationScale() * meterToFoot;

		final TitledBorder partPanelBorder = (TitledBorder) partPanel.getBorder();
		if (selectedPart != null) {
			final ReadOnlyVector3 v = selectedPart.getAbsPoint(0);
			if (selectedPart instanceof Tree) {
				final Tree tree = (Tree) selectedPart;
				if (tree.isDrawable()) {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							partPanelBorder.setTitle("Tree (" + tree.getId() + "): " + tree.getTreeName());
							partProperty1Label.setText("  Spread:");
							partProperty2Label.setText("  Height:");
							partProperty3Label.setText("  Position:");
							final double l = v.length();
							double a = 90 + Math.toDegrees(Math.asin(-v.getY() / l));
							if (v.getX() < 0) {
								a = 360 - a;
							}
							if (Util.isZero(a - 360)) {
								a = 0;
							}
							partProperty1TextField.setText(ONE_DECIMAL.format(tree.getWidth() * scale) + lengthUnit);
							partProperty2TextField.setText(ONE_DECIMAL.format(tree.getHeight() * scale) + lengthUnit);
							partProperty3TextField.setText("(" + ONE_DECIMAL.format(v.getX() * scale) + ", " + ONE_DECIMAL.format(v.getY() * scale) + ")" + lengthUnit + " or (" + ONE_DECIMAL.format(l * scale) + lengthUnit + ", " + ONE_DECIMAL.format(a) + "\u00B0)");
							partProperty1TextField.setToolTipText("The spread of the tree");
							partProperty2TextField.setToolTipText("The height of the tree");
							partProperty3TextField.setToolTipText("The (x, y) or polar coordinates on the land");
						}
					});
				}
			} else if (selectedPart instanceof Human) {
				final Human human = (Human) selectedPart;
				if (human.isDrawable()) {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							partPanelBorder.setTitle("Human (" + human.getId() + "): " + human.getHumanName());
							partProperty1Label.setText("  X:");
							partProperty2Label.setText("  Y:");
							partProperty3Label.setText("  Z:");
							partProperty1TextField.setText(ONE_DECIMAL.format(v.getX() * scale) + lengthUnit);
							partProperty2TextField.setText(ONE_DECIMAL.format(v.getY() * scale) + lengthUnit);
							partProperty3TextField.setText(ONE_DECIMAL.format(v.getZ() * scale) + lengthUnit);
						}
					});
				}
			} else if (selectedPart instanceof SolarPanel) {
				final SolarPanel sp = (SolarPanel) selectedPart;
				if (sp.isDrawable()) {
					final Foundation f = sp.getTopContainer();
					if (f != null) {
						double a = sp.getRelativeAzimuth() + f.getAzimuth();
						if (a >= 360) {
							a -= 360;
						}
						final double az = a;
						final boolean flat = (sp.getContainer() instanceof Roof && Util.isZero(sp.getContainer().getHeight())) || (sp.getContainer() instanceof Foundation);
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								String title = "Solar Panel (" + sp.getId() + "), " + (sp.getModelName() == null ? "" : "Model: " + sp.getModelName());
								final String trackerName = sp.getTrackerName();
								if (trackerName != null) {
									title += ", Tracker: " + trackerName;
								}
								partPanelBorder.setTitle(title);
								partProperty1Label.setText("  Size & Position:");
								partProperty1TextField.setText(TWO_DECIMALS.format(sp.getPanelWidth() * meterToFoot) + "\u00d7" + TWO_DECIMALS.format(sp.getPanelHeight() * meterToFoot) + lengthUnit + ", (" + ONE_DECIMAL.format(v.getX() * scale) + ", " + ONE_DECIMAL.format(v.getY() * scale) + ", " + ONE_DECIMAL.format(v.getZ() * scale) + ")" + lengthUnit);
								partProperty2Label.setText("  Angles:");
								partProperty2TextField.setText(flat ? "tilt: " + ONE_DECIMAL.format(Util.isZero(sp.getTiltAngle()) ? Math.toDegrees(Math.asin(sp.getNormal().getY())) : sp.getTiltAngle()) + "\u00B0, azimuth: " + ONE_DECIMAL.format(az) + "\u00B0" : " --- ");
								partProperty1TextField.setToolTipText("The length, width, and (x, y, z) coordinates of the solar panel");
								partProperty2TextField.setToolTipText("The angles of the solar panel");
								final String eff = ONE_DECIMAL.format(sp.getCellEfficiency() * 100) + "%";
								if (energyViewShown) {
									partProperty3Label.setText("  Efficiency & Yield:");
									partProperty3TextField.setText(eff + ", " + TWO_DECIMALS.format(sp.getSolarPotentialToday()) + " kWh");
									partProperty3TextField.setToolTipText("The solar cell efficiency and daily yield of the solar panel");
								} else {
									partProperty3Label.setText("  Efficiency:");
									partProperty3TextField.setText(eff);
									partProperty3TextField.setToolTipText("The solar cell efficiency of the solar panel");
								}
							}
						});
					}
				}
			} else if (selectedPart instanceof Rack) {
				final Rack rack = (Rack) selectedPart;
				if (rack.isDrawable()) {
					final Foundation f = rack.getTopContainer();
					if (f != null) {
						double a = rack.getRelativeAzimuth() + f.getAzimuth();
						if (a >= 360) {
							a -= 360;
						}
						final double az = a;
						final int n = rack.isMonolithic() ? rack.getNumberOfSolarPanels() : rack.getChildren().size();
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								String title = "Rack (" + rack.getId() + ")";
								final SolarPanel s = rack.getSolarPanel();
								if (s.getModelName() != null) {
									title += ", Model: " + s.getModelName();
								}
								final String trackerName = rack.getTrackerName();
								if (trackerName != null) {
									title += ", Tracker: " + trackerName;
								}
								partPanelBorder.setTitle(title);
								partProperty1Label.setText("  Size & Position:");
								partProperty1TextField.setText(TWO_DECIMALS.format(rack.getRackWidth() * meterToFoot) + "\u00d7" + TWO_DECIMALS.format(rack.getRackHeight() * meterToFoot) + lengthUnit + ", (" + ONE_DECIMAL.format(v.getX() * scale) + ", " + ONE_DECIMAL.format(v.getY() * scale) + ", " + ONE_DECIMAL.format(v.getZ() * scale) + ")" + lengthUnit);
								partProperty2Label.setText("  Angles:");
								partProperty2TextField.setText("tilt: " + ONE_DECIMAL.format(Util.isZero(rack.getTiltAngle()) ? Math.toDegrees(Math.asin(rack.getNormal().getY())) : rack.getTiltAngle()) + "\u00B0, azimuth: " + ONE_DECIMAL.format(az) + "\u00B0");
								partProperty1TextField.setToolTipText("The length, width, and (x, y, z) coordinates of the rack");
								partProperty2TextField.setToolTipText("The angles of the rack");
								partProperty3Label.setText("  Solar Panels:");
								final SolarPanel sp = rack.getSolarPanel();
								final String eff = ONE_DECIMAL.format(sp.getCellEfficiency() * 100) + "%";
								if (energyViewShown) {
									partProperty3Label.setText("  Efficiency & Yield:");
									partProperty3TextField.setText(eff + ", " + TWO_DECIMALS.format(rack.getSolarPotentialToday()) + " kWh");
									partProperty3TextField.setToolTipText("The solar cell efficiency and daily yield of the solar panel array on the rack");
								} else {
									if (rack.isMonolithic()) {
										final int[] rnc = rack.getSolarPanelRowAndColumnNumbers();
										partProperty3TextField.setText("" + n + " (" + rnc[0] + "\u00D7" + rnc[1] + "), " + s.getPanelWidth() + "\u00D7" + s.getPanelHeight() + lengthUnit + ", " + eff);
									} else {
										partProperty3TextField.setText("" + n);
									}
									partProperty3TextField.setToolTipText("Number and type of solar panels on this rack");
								}
							}
						});
					}
				}
			} else if (selectedPart instanceof Mirror) {
				final Mirror m = (Mirror) selectedPart;
				if (m.isDrawable()) {
					final Foundation f = m.getTopContainer();
					if (f != null) {
						double a = m.getRelativeAzimuth() + f.getAzimuth();
						if (a >= 360) {
							a -= 360;
						}
						final double az = a;
						final boolean flat = m.getContainer() instanceof Foundation;
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								partPanelBorder.setTitle("Mirror (" + m.getId() + ")");
								partProperty1Label.setText("  Size & Position:");
								partProperty1TextField.setText(TWO_DECIMALS.format(m.getMirrorWidth() * meterToFoot) + "\u00d7" + TWO_DECIMALS.format(m.getMirrorHeight() * meterToFoot) + lengthUnit + ", (" + ONE_DECIMAL.format(v.getX() * scale) + ", " + ONE_DECIMAL.format(v.getY() * scale) + ", " + ONE_DECIMAL.format(v.getZ() * scale) + ")" + lengthUnit);
								partProperty2Label.setText("  Angles:");
								partProperty2TextField.setText(flat ? "tilt: " + ONE_DECIMAL.format(m.getTiltAngle()) + "\u00B0, azimuth: " + ONE_DECIMAL.format(az) + "\u00B0" : " --- ");
								partProperty1TextField.setToolTipText("The length, width, and (x, y, z) coordinates of the mirror");
								partProperty2TextField.setToolTipText("The angles of the mirror");
								final String reflectance = ONE_DECIMAL.format(m.getReflectance() * 100) + "%";
								if (energyViewShown) {
									partProperty3Label.setText("  Reflectance & Yield:");
									partProperty3TextField.setText(reflectance + ", " + ONE_DECIMAL.format(m.getSolarPotentialToday() * m.getSystemEfficiency()) + " kWh");
									partProperty3TextField.setToolTipText("The reflectance and yield of this mirror");
								} else {
									partProperty3Label.setText("  Reflectance:");
									partProperty3TextField.setText(reflectance);
									partProperty3TextField.setToolTipText("The reflectance of this mirror");
								}
							}
						});
					}
				}
			} else if (selectedPart instanceof ParabolicTrough) {
				final ParabolicTrough t = (ParabolicTrough) selectedPart;
				if (t.isDrawable()) {
					final Foundation f = t.getTopContainer();
					if (f != null) {
						double a = t.getRelativeAzimuth() + f.getAzimuth();
						if (a >= 360) {
							a -= 360;
						}
						final double focalLength = t.getSemilatusRectum() * 0.5;
						final double d = t.getApertureWidth();
						final double h = d * d / (16 * focalLength);
						final double rimAngle = Math.toDegrees(Math.atan(1.0 / (d / (8 * h) - (2 * h) / d)));
						final double b = 4 * h / d;
						final double c = Math.sqrt(b * b + 1);
						final double s = 0.5 * d * c + 2 * focalLength * Math.log(b + c);
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								partPanelBorder.setTitle("Parabolic Trough (" + t.getId() + ")");
								partProperty1Label.setText("  Length & Position:");
								partProperty1TextField.setText(TWO_DECIMALS.format(t.getTroughLength() * meterToFoot) + lengthUnit + ", module:" + TWO_DECIMALS.format(t.getModuleLength() * meterToFoot) + lengthUnit + ", (" + ONE_DECIMAL.format(v.getX() * scale) + ", " + ONE_DECIMAL.format(v.getY() * scale) + ", " + ONE_DECIMAL.format(v.getZ() * scale) + ")" + lengthUnit);
								partProperty2Label.setText("  Parabola Shape:");
								partProperty2TextField.setText("f=" + ONE_DECIMAL.format(focalLength * meterToFoot) + lengthUnit + ", d=" + ONE_DECIMAL.format(t.getApertureWidth() * meterToFoot) + lengthUnit + ", h=" + ONE_DECIMAL.format(h * meterToFoot) + lengthUnit + ", \u03C6=" + ONE_DECIMAL.format(rimAngle >= 0 ? rimAngle : 180 + rimAngle) + "\u00B0");
								partProperty1TextField.setToolTipText("Assembly length, module length, and (x, y, z) coordinates of the parabolic trough");
								partProperty2TextField.setToolTipText("Parameters of the parabolic shape");
								final String reflect = "R=" + ONE_DECIMAL.format(t.getReflectance() * 100) + "%, s=" + ONE_DECIMAL.format(s * t.getTroughLength() * meterToFoot * meterToFoot) + lengthUnit + "\u00B2, a=" + ONE_DECIMAL.format(d * t.getTroughLength() * meterToFoot * meterToFoot) + lengthUnit + "\u00B2";
								if (energyViewShown) {
									partProperty3Label.setText("  Reflection & Yield:");
									partProperty3TextField.setText(reflect + ", " + ONE_DECIMAL.format(t.getSolarPotentialToday() * t.getSystemEfficiency()) + " kWh");
									partProperty3TextField.setToolTipText("The reflectance and yield of this parabolic trough");
								} else {
									partProperty3Label.setText("  Reflection:");
									partProperty3TextField.setText(reflect);
									partProperty3TextField.setToolTipText("The reflectance of this parabolic trough");
								}
							}
						});
					}
				}
			} else if (selectedPart instanceof ParabolicDish) {
				final ParabolicDish d = (ParabolicDish) selectedPart;
				if (d.isDrawable()) {
					final Foundation f = d.getTopContainer();
					if (f != null) {
						double a = d.getRelativeAzimuth() + f.getAzimuth();
						if (a >= 360) {
							a -= 360;
						}
						final double focalLength = d.getFocalLength();
						final double rimRadius = d.getRimRadius();
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								partPanelBorder.setTitle("Parabolic Dish (" + d.getId() + ")");
								partProperty1Label.setText("  Size & Position:");
								partProperty1TextField.setText("Rim radius=" + TWO_DECIMALS.format(rimRadius * meterToFoot) + lengthUnit + ", (" + ONE_DECIMAL.format(v.getX() * scale) + ", " + ONE_DECIMAL.format(v.getY() * scale) + ", " + ONE_DECIMAL.format(v.getZ() * scale) + ")" + lengthUnit);
								partProperty2Label.setText("  Parabola Shape:");
								partProperty2TextField.setText("Focal length=" + ONE_DECIMAL.format(focalLength * meterToFoot) + lengthUnit);
								partProperty1TextField.setToolTipText("Rim radius and (x, y, z) coordinates of the parabolic dish");
								partProperty2TextField.setToolTipText("Parameters of the parabolic shape");
								final String reflect = "R=" + ONE_DECIMAL.format(d.getReflectance() * 100) + "%";
								if (energyViewShown) {
									partProperty3Label.setText("  Reflection & Yield:");
									partProperty3TextField.setText(reflect + ", " + ONE_DECIMAL.format(d.getSolarPotentialToday() * d.getSystemEfficiency()) + " kWh");
									partProperty3TextField.setToolTipText("The reflectance and yield of this parabolic dish");
								} else {
									partProperty3Label.setText("  Reflection:");
									partProperty3TextField.setText(reflect);
									partProperty3TextField.setToolTipText("The reflectance of this parabolic dish");
								}
							}
						});
					}
				}
			} else if (selectedPart instanceof FresnelReflector) {
				final FresnelReflector r = (FresnelReflector) selectedPart;
				if (r.isDrawable()) {
					final Foundation f = r.getTopContainer();
					if (f != null) {
						double a = r.getRelativeAzimuth() + f.getAzimuth();
						if (a >= 360) {
							a -= 360;
						}
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								partPanelBorder.setTitle("Fresnel Reflector (" + r.getId() + ")");
								partProperty1Label.setText("  Length & Position:");
								partProperty1TextField.setText(TWO_DECIMALS.format(r.getLength() * meterToFoot) + lengthUnit + ", module:" + TWO_DECIMALS.format(r.getModuleLength() * meterToFoot) + lengthUnit + ", (" + ONE_DECIMAL.format(v.getX() * scale) + ", " + ONE_DECIMAL.format(v.getY() * scale) + ", " + ONE_DECIMAL.format(v.getZ() * scale) + ")" + lengthUnit);
								partProperty2Label.setText("  Width:");
								partProperty2TextField.setText(ONE_DECIMAL.format(r.getModuleWidth() * meterToFoot) + lengthUnit);
								partProperty1TextField.setToolTipText("Assembly length, module length, and (x, y, z) coordinates of the parabolic trough");
								partProperty2TextField.setToolTipText("Parameters of the Fresnel reflector");
								final String reflect = "R=" + ONE_DECIMAL.format(r.getReflectance() * 100) + "%, a=" + ONE_DECIMAL.format(r.getModuleWidth() * r.getLength() * meterToFoot * meterToFoot) + lengthUnit + "\u00B2";
								if (energyViewShown) {
									partProperty3Label.setText("  Reflection & Yield:");
									partProperty3TextField.setText(reflect + ", " + ONE_DECIMAL.format(r.getSolarPotentialToday() * r.getSystemEfficiency()) + " kWh");
									partProperty3TextField.setToolTipText("The reflectance and yield of this Fresnel reflector");
								} else {
									partProperty3Label.setText("  Reflection:");
									partProperty3TextField.setText(reflect);
									partProperty3TextField.setToolTipText("The reflectance of this Fresnel reflector");
								}
							}
						});
					}
				}
			} else if (selectedPart instanceof Sensor) {
				final Sensor sensor = (Sensor) selectedPart;
				if (sensor.isDrawable()) {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							partPanelBorder.setTitle("Sensor (" + sensor.getId() + ")");
							partProperty1Label.setText("  Position:");
							partProperty2Label.setText("  Light:");
							partProperty3Label.setText("  Heat:");
							partProperty1TextField.setText("(" + ONE_DECIMAL.format(v.getX() * scale) + ", " + ONE_DECIMAL.format(v.getY() * scale) + ", " + ONE_DECIMAL.format(v.getZ() * scale) + ")" + lengthUnit);
							partProperty2TextField.setText(TWO_DECIMALS.format(sensor.getSolarPotentialToday() / sensor.getArea()) + " kWh/day/m\u00B2");
							partProperty3TextField.setText(TWO_DECIMALS.format(sensor.getTotalHeatLoss() / sensor.getArea()) + " kWh/day/m\u00B2");
							partProperty1TextField.setToolTipText("The (x, y, z) coordinates of the sensor");
							partProperty2TextField.setToolTipText("The light intensity measured by the sensor");
							partProperty3TextField.setToolTipText("The heat flux measured by the sensor");
						}
					});
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
					final double az = foundation.getAzimuth();
					final String landArea;
					final FoundationPolygon polygon = foundation.getPolygon();
					if (polygon != null && polygon.isVisible()) {
						landArea = " (inset:" + ONE_DECIMAL.format(polygon.getArea()) + ")";
					} else {
						landArea = "";
					}
					final Mesh selectedMesh;
					final Node selectedNode;
					final OrientedBoundingBox nodeBox, meshBox;
					final List<Node> nodes = foundation.getImportedNodes();
					if (nodes != null) {
						selectedMesh = foundation.getSelectedMesh();
						if (selectedMesh != null) {
							selectedNode = selectedMesh.getParent();
							nodeBox = Util.getOrientedBoundingBox(selectedNode);
							meshBox = Util.getOrientedBoundingBox(selectedMesh);
						} else {
							selectedNode = null;
							nodeBox = null;
							meshBox = null;
						}
					} else {
						selectedMesh = null;
						selectedNode = null;
						nodeBox = null;
						meshBox = null;
					}
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							if (selectedNode != null) {
								final double xNodeBox = 2 * nodeBox.getExtent().getX() * scale;
								final double yNodeBox = 2 * nodeBox.getExtent().getY() * scale;
								final double zNodeBox = 2 * nodeBox.getExtent().getZ() * scale;
								final double xMeshBox = 2 * meshBox.getExtent().getX() * scale;
								final double yMeshBox = 2 * meshBox.getExtent().getY() * scale;
								final double zMeshBox = 2 * meshBox.getExtent().getZ() * scale;
								final ReadOnlyVector3 meshBoxCenter = meshBox.getCenter();
								final NodeState ns = foundation.getNodeState(selectedNode);
								final Vector3 position = ns.getRelativePosition().add(foundation.getAbsCenter(), null);
								Vector3 meshNormal = null;
								int meshIndex = -1;
								if (selectedMesh.getUserData() instanceof UserData) {
									final UserData ud = (UserData) selectedMesh.getUserData();
									meshIndex = ud.getMeshIndex();
									meshNormal = (Vector3) ud.getNormal();
									if (!Util.isZero(az)) {
										selectedNode.getRotation().applyPost(meshNormal, meshNormal);
									}
								}
								// System.out.println(">>>" + Util.computeFirstNormal(selectedMesh) + ", " + Util.getFirstNormalFromBuffer(selectedMesh));
								final String meshBoxString = TWO_DECIMALS.format(xMeshBox) + "\u00d7" + (TWO_DECIMALS.format(yMeshBox)) + "\u00d7" + (TWO_DECIMALS.format(zMeshBox)) + lengthUnit;
								final String meshCenterString = "(" + ONE_DECIMAL.format(meshBoxCenter.getX() * scale) + ", " + ONE_DECIMAL.format(meshBoxCenter.getY() * scale) + ", " + ONE_DECIMAL.format(meshBoxCenter.getZ() * scale) + ")" + lengthUnit;
								final String meshNormalString = meshNormal != null ? "(" + TWO_DECIMALS.format(meshNormal.getX()) + ", " + TWO_DECIMALS.format(meshNormal.getY()) + ", " + TWO_DECIMALS.format(meshNormal.getZ()) + ")" : "";
								partPanelBorder.setTitle("Node #" + foundation.getImportedNodes().indexOf(selectedNode) + " (" + Util.getFileName(ns.getSourceURL().getPath()).replace("%20", " ") + "), Mesh #" + meshIndex + ", Base #" + foundation.getId());
								partProperty1Label.setText("  Node:");
								partProperty2Label.setText("  Mesh:");
								partProperty1TextField.setText(TWO_DECIMALS.format(xNodeBox) + "\u00d7" + (TWO_DECIMALS.format(yNodeBox)) + "\u00d7" + (TWO_DECIMALS.format(zNodeBox)) + lengthUnit + ", (" + TWO_DECIMALS.format(position.getX() * scale) + ", " + TWO_DECIMALS.format(position.getY() * scale) + ")" + lengthUnit);
								partProperty2TextField.setText(meshBoxString + ", " + meshCenterString);
								partProperty1TextField.setToolTipText("<html>Dimension and location of the bounding box of the selected node:<br>" + partProperty1TextField.getText() + "<br>File:" + ns.getSourceURL().getFile() + "</html>");
								partProperty2TextField.setToolTipText("<html>Dimension and location of the bounding box of the selected mesh:<br>" + partProperty2TextField.getText() + "</html>");
								if (energyViewShown) {
									double dailyMeshSolarPotential = 0;
									final double[] meshSolarPotential = SolarRadiation.getInstance().getSolarPotential(selectedMesh);
									for (final double x : meshSolarPotential) {
										dailyMeshSolarPotential += x;
									}
									partProperty3Label.setText("  Solar:");
									partProperty3TextField.setText("\u2191" + meshNormalString + ", " + TWO_DECIMALS.format(dailyMeshSolarPotential) + " kWh");
									partProperty3TextField.setToolTipText("Normal vector and solar potential of the selected mesh");
								} else {
									partProperty3Label.setText("  Normal:");
									partProperty3TextField.setText("\u2191" + meshNormalString + ", " + selectedMesh.getMeshData().getVertexCount() + " vertices");
									partProperty3TextField.setToolTipText("<html>Normal vector and vertex count of the selected mesh");
								}
							} else {
								partPanelBorder.setTitle("Foundation (" + foundation.getId() + ")");
								partProperty1Label.setText("  Size:");
								partProperty2Label.setText("  Position:");
								partProperty3Label.setText("  Azimuth:");
								partProperty1TextField.setText(TWO_DECIMALS.format(lx * scale) + "\u00d7" + (TWO_DECIMALS.format(ly * scale)) + "\u00d7" + (TWO_DECIMALS.format(lz * scale)) + lengthUnit + "\u2248" + ONE_DECIMAL.format(lx * ly * scale * scale) + landArea + lengthUnit + "\u00B2");
								partProperty2TextField.setText("(" + TWO_DECIMALS.format(cx * scale) + ", " + TWO_DECIMALS.format(cy * scale) + ")" + lengthUnit);
								partProperty3TextField.setText(NO_DECIMAL.format(az) + "\u00B0");
								partProperty1TextField.setToolTipText("The length and width of the foundation");
								partProperty2TextField.setToolTipText("The (x, y) coordinate of the center of the foundation");
								partProperty3TextField.setToolTipText("The azimuth of the reference edge");
							}
						}
					});
				}
			} else if (selectedPart instanceof Roof) {
				final Roof roof = (Roof) selectedPart;
				if (roof.isDrawable()) {
					final double area = roof.getArea();
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							partProperty1Label.setText("  Area:");
							partProperty1TextField.setText(TWO_DECIMALS.format(area) + lengthUnit + "\u00B2");
							partProperty2Label.setText("  Rise:");
							partProperty2TextField.setText(TWO_DECIMALS.format(roof.getHeight() * scale) + lengthUnit);
							partProperty1TextField.setToolTipText("The total area of the roof");
							partProperty2TextField.setToolTipText("<html>The rise of the roof<br>(the highest point of the roof to the top of the walls</html>");
							final String id = "Roof (" + roof.getId() + ")";
							final String rval = ONE_DECIMAL.format(Util.toUsRValue(roof.getUValue()));
							if (energyViewShown) {
								partPanelBorder.setTitle(id + " - R-value = " + rval);
								partProperty3Label.setText("  Solar:");
								partProperty3TextField.setText(TWO_DECIMALS.format(roof.getSolarPotentialToday() * (1 - roof.getAlbedo())) + " kWh");
								partProperty3TextField.setToolTipText("The solar potential of the roof of the day");
							} else {
								partPanelBorder.setTitle(id);
								partProperty3Label.setText("  R-value:");
								partProperty3TextField.setText(rval + " (US system)");
								partProperty3TextField.setToolTipText("The R-value of the roof");
							}
						}
					});
				}
			} else if (selectedPart instanceof Window) {
				final Window window = (Window) selectedPart;
				if (window.isDrawable()) {
					final double lx = window.getWindowWidth();
					final double ly = window.getWindowHeight();
					final Vector3 v1 = window.getAbsPoint(1);
					final Vector3 v2 = window.getAbsPoint(2);
					final Vector3 v3 = window.getAbsPoint(3);
					final double cx = 0.25 * (v.getX() + v1.getX() + v2.getX() + v3.getX());
					final double cy = 0.25 * (v.getY() + v1.getY() + v2.getY() + v3.getY());
					final double cz = 0.25 * (v.getZ() + v1.getZ() + v2.getZ() + v3.getZ());
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							partProperty1Label.setText("  Size & Center:");
							partProperty1TextField.setText(TWO_DECIMALS.format(lx) + "\u00d7" + (TWO_DECIMALS.format(ly)) + lengthUnit + " \u2248 " + TWO_DECIMALS.format(lx * ly) + lengthUnit + "\u00B2, (" + TWO_DECIMALS.format(cx * scale) + ", " + TWO_DECIMALS.format(cy * scale) + ", " + TWO_DECIMALS.format(cz * scale) + ")" + lengthUnit);
							partProperty2Label.setText("  U-value:");
							partProperty2TextField.setText(TWO_DECIMALS.format(Util.toUsUValue(window.getUValue())) + " (US system)");
							partProperty1TextField.setToolTipText("The width and height of the window");
							partProperty2TextField.setToolTipText("The U-value of the window");
							final String id = "Window (" + window.getId() + ")";
							final String shgc = TWO_DECIMALS.format(window.getSolarHeatGainCoefficient());
							if (energyViewShown) {
								partPanelBorder.setTitle(id + " - SHGC = " + shgc);
								partProperty3Label.setText("  Gain:");
								partProperty3TextField.setText(TWO_DECIMALS.format(window.getSolarPotentialToday() * window.getSolarHeatGainCoefficient()) + " kWh");
								partProperty3TextField.setToolTipText("The solar heat gain of the window of the day");
							} else {
								partPanelBorder.setTitle(id);
								partProperty3Label.setText("  SHGC:");
								partProperty3TextField.setText(shgc);
								partProperty3TextField.setToolTipText("The solar heat gain coefficient (SHGC) of the window");
							}
						}
					});
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
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							partProperty1Label.setText("  Size:");
							partProperty1TextField.setText(TWO_DECIMALS.format(lx * scale) + "\u00d7" + (TWO_DECIMALS.format(ly * scale)) + lengthUnit + " \u2248 " + TWO_DECIMALS.format(lx * ly * scale * scale) + lengthUnit + " \u00B2");
							partProperty2Label.setText("  Position:");
							partProperty2TextField.setText("(" + TWO_DECIMALS.format(cx * scale) + ", " + TWO_DECIMALS.format(cy * scale) + ")" + lengthUnit);
							partProperty1TextField.setToolTipText("The width and height of the wall");
							partProperty2TextField.setToolTipText("The (x, y) coordinate of the center of the wall");
							final String id = "Wall (" + wall.getId() + ")";
							final String rval = ONE_DECIMAL.format(Util.toUsRValue(wall.getUValue()));
							if (energyViewShown) {
								partPanelBorder.setTitle(id + " - R-value = " + rval);
								partProperty3Label.setText("  Solar:");
								partProperty3TextField.setText(TWO_DECIMALS.format(wall.getSolarPotentialToday() * (1 - wall.getAlbedo())) + " kWh");
								partProperty3TextField.setToolTipText("The solar potential of the wall");
							} else {
								partPanelBorder.setTitle(id);
								partProperty3Label.setText("  R-value:");
								partProperty3TextField.setText(rval + " (US system)");
								partProperty3TextField.setToolTipText("The R-value of the wall");
							}
						}
					});
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
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							partPanelBorder.setTitle("Door (" + door.getId() + ")");
							partProperty1Label.setText("  Size:");
							partProperty1TextField.setText(TWO_DECIMALS.format(lx * scale) + "\u00d7" + (TWO_DECIMALS.format(ly * scale)) + lengthUnit + " \u2248 " + TWO_DECIMALS.format(lx * ly * scale * scale) + lengthUnit + "\u00B2");
							partProperty2Label.setText("  Position:");
							partProperty2TextField.setText("(" + TWO_DECIMALS.format(cx * scale) + ", " + TWO_DECIMALS.format(cy * scale) + ")" + lengthUnit);
							partProperty3Label.setText("  U-value:");
							partProperty3TextField.setText(TWO_DECIMALS.format(Util.toUsUValue(door.getUValue())) + " (US system)");
							partProperty1TextField.setToolTipText("The width and height of the door");
							partProperty2TextField.setToolTipText("The (x, y) coordinates of the center of the door");
							partProperty3TextField.setToolTipText("The U-value of the wall");
						}
					});
				}
			} else if (selectedPart instanceof Floor) {
				final Floor floor = (Floor) selectedPart;
				if (floor.isDrawable()) {
					final double cx, cy;
					if (floor.getPoints().size() > 1) {
						final Vector3 v1 = floor.getAbsPoint(1);
						final Vector3 v2 = floor.getAbsPoint(2);
						final Vector3 v3 = floor.getAbsPoint(3);
						cx = 0.25 * (v.getX() + v1.getX() + v2.getX() + v3.getX());
						cy = 0.25 * (v.getY() + v1.getY() + v2.getY() + v3.getY());
					} else {
						cx = Double.NaN;
						cy = Double.NaN;
					}
					final double cz = v.getZ();
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							partPanelBorder.setTitle("Floor (" + floor.getId() + ")");
							partProperty1Label.setText("  Area:");
							partProperty2Label.setText("  Position:");
							partProperty3Label.setText("  Height:");
							partProperty1TextField.setText(ONE_DECIMAL.format(floor.getArea()) + lengthUnit + "\u00B2");
							if (!Double.isNaN(cx) && !Double.isNaN(cy)) {
								partProperty2TextField.setText("(" + ONE_DECIMAL.format(cx * scale) + ", " + ONE_DECIMAL.format(cy * scale) + ")" + lengthUnit);
							}
							partProperty3TextField.setText(ONE_DECIMAL.format(cz * scale) + lengthUnit);
							partProperty1TextField.setToolTipText("The area of the floor");
							partProperty2TextField.setToolTipText("The (x, y) position of the center of the floor");
							partProperty3TextField.setToolTipText("The height of the floor");
						}
					});
				}
			}
		} else {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					final int numberOfSolarPanels = Scene.getInstance().countSolarPanels();
					if (numberOfSolarPanels > 0) {
						partPanelBorder.setTitle("Solar Panels");
						partProperty1Label.setText("  Total Number:");
						partProperty1TextField.setText("" + numberOfSolarPanels);
						partProperty1TextField.setToolTipText("Total number of solar panels");
						partProperty2Label.setText("  -");
						partProperty2TextField.setText("");
						partProperty2TextField.setToolTipText(null);
						partProperty3Label.setText("  -");
						partProperty3TextField.setText("");
						partProperty3TextField.setToolTipText(null);
					} else {
						final int numberOfMirrors = Scene.getInstance().countParts(Mirror.class);
						if (numberOfMirrors > 0) {
							partPanelBorder.setTitle("Mirrors");
							partProperty1Label.setText("  Total Number:");
							partProperty1TextField.setText("" + numberOfMirrors);
							partProperty1TextField.setToolTipText("Total number of mirrors");
							partProperty2Label.setText("  -");
							partProperty2TextField.setText("");
							partProperty2TextField.setToolTipText(null);
							partProperty3Label.setText("  -");
							partProperty3TextField.setText("");
							partProperty3TextField.setToolTipText(null);
						} else {
							final int numberOfParabolicTroughs = Scene.getInstance().countParts(ParabolicTrough.class);
							if (numberOfParabolicTroughs > 0) {
								partPanelBorder.setTitle("Parabolic Troughs");
								partProperty1Label.setText("  Total Number:");
								partProperty1TextField.setText("" + numberOfParabolicTroughs);
								partProperty1TextField.setToolTipText("Total number of parabolic troughs");
								partProperty2Label.setText("  -");
								partProperty2TextField.setText("");
								partProperty2TextField.setToolTipText(null);
								partProperty3Label.setText("  -");
								partProperty3TextField.setText("");
								partProperty3TextField.setToolTipText(null);
							} else {
								final int numberOfParabolicDishes = Scene.getInstance().countParts(ParabolicDish.class);
								if (numberOfParabolicDishes > 0) {
									partPanelBorder.setTitle("Parabolic Dishes");
									partProperty1Label.setText("  Total Number:");
									partProperty1TextField.setText("" + numberOfParabolicDishes);
									partProperty1TextField.setToolTipText("Total number of parabolic dishes");
									partProperty2Label.setText("  -");
									partProperty2TextField.setText("");
									partProperty2TextField.setToolTipText(null);
									partProperty3Label.setText("  -");
									partProperty3TextField.setText("");
									partProperty3TextField.setToolTipText(null);
								} else {
									final int numberOfNodes = Scene.getInstance().countNodes();
									if (numberOfNodes > 0) {
										partPanelBorder.setTitle("Structures");
										partProperty1Label.setText("  Total Nodes:");
										partProperty1TextField.setText("" + numberOfNodes);
										partProperty1TextField.setToolTipText("Total number of structure nodes");
										partProperty2Label.setText("  Total Meshes:");
										partProperty2TextField.setText("" + Scene.getInstance().countMeshes());
										partProperty2TextField.setToolTipText("Total number of structure meshes");
										partProperty3Label.setText("  -");
										partProperty3TextField.setText("");
										partProperty3TextField.setToolTipText(null);
									} else {
										partPanelBorder.setTitle("Part");
										partProperty1Label.setText("  -");
										partProperty1TextField.setText("");
										partProperty1TextField.setToolTipText(null);
										partProperty2Label.setText("  -");
										partProperty2TextField.setText("");
										partProperty2TextField.setToolTipText(null);
										partProperty3Label.setText("  -");
										partProperty3TextField.setText("");
										partProperty3TextField.setToolTipText(null);
									}
								}
							}
						}
					}
				}
			});
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
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (selectedFoundation != null) {
					switch (selectedFoundation.getProjectType()) {
					case Foundation.TYPE_BUILDING:
						dataPanel.remove(instructionPanel);
						dataPanel.remove(pvStationPanel);
						dataPanel.remove(cspStationPanel);
						dataPanel.add(buildingPanel, 2);
						final Calendar c = Heliodon.getInstance().getCalendar();
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
						buildingInfoPanel.update(selectedFoundation);
						break;
					case Foundation.TYPE_PV_STATION:
						dataPanel.remove(instructionPanel);
						dataPanel.remove(buildingPanel);
						dataPanel.remove(cspStationPanel);
						dataPanel.add(pvStationPanel, 2);
						pvStationInfoPanel.update(selectedFoundation);
						break;
					case Foundation.TYPE_CSP_STATION:
						dataPanel.remove(instructionPanel);
						dataPanel.remove(buildingPanel);
						dataPanel.remove(pvStationPanel);
						dataPanel.add(cspStationPanel, 2);
						cspStationInfoPanel.update(selectedFoundation);
						break;
					case -1:
						dataPanel.remove(instructionPanel);
						dataPanel.remove(buildingPanel);
						dataPanel.remove(pvStationPanel);
						dataPanel.remove(cspStationPanel);
						break;
					}
				} else {
					dataPanel.remove(buildingPanel);
					dataPanel.remove(pvStationPanel);
					dataPanel.remove(cspStationPanel);
					dataPanel.add(instructionPanel, 2);
					for (int i = 0; i < instructionSheets.length; i++) {
						final String contentType = Scene.getInstance().getInstructionSheetTextType(i);
						instructionSheets[i].setContentType(contentType == null ? "text/plain" : contentType);
						instructionSheets[i].setText(Scene.getInstance().getInstructionSheetText(i));
					}
				}
				dataPanel.validate();
				dataPanel.repaint();
			}
		});

	}

	public void updateThermostat() {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart == null) {
			return;
		}
		final Foundation selectedBuilding = selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer();
		if (selectedBuilding != null) {
			final Calendar c = Heliodon.getInstance().getCalendar();
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
				String title = "";
				switch (Scene.getInstance().getProjectType()) {
				case Foundation.TYPE_BUILDING:
					title = "Building";
					break;
				case Foundation.TYPE_PV_STATION:
					title = "PV";
					break;
				case Foundation.TYPE_CSP_STATION:
					title = "CSP";
					break;
				}
				String author = "";
				if (Scene.getInstance().getDesigner() != null) {
					final String name = Scene.getInstance().getDesigner().getName();
					if (!"User".equals(name)) {
						author += name + "'s";
					}
				}
				timeAndLocationPanel.setBorder(createTitledBorder(author + " " + title + " Project: " + (s != null ? s : ""), true));
				buildingInfoPanel.updateAreaBounds();
				buildingInfoPanel.updateHeightBounds();
				buildingInfoPanel.updateWindowToFloorRatioBounds();
				buildingInfoPanel.updateSolarPanelNumberBounds();
				buildingInfoPanel.updateWindowNumberBounds();
				buildingInfoPanel.updateWallNumberBounds();
				pvStationInfoPanel.updateSolarPanelNumberMaximum();
				pvStationInfoPanel.updateBudgetMaximum();
				cspStationInfoPanel.updateMirrorNumberMaximum();
				cspStationInfoPanel.updateParabolicTroughNumberMaximum();
				cspStationInfoPanel.updateBudgetMaximum();
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
		return regionComboBox;
	}

	public void showHeatMapContrastSlider(final boolean b) {
		if (b) {
			dataPanel.add(heatMapPanel);
		} else {
			dataPanel.remove(heatMapPanel);
		}
		dataPanel.repaint();
	}

	public void turnOffCompute() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (SceneManager.getInstance().getSolarHeatMap()) {
					MainPanel.getInstance().getEnergyViewButton().setSelected(false);
				}
				SceneManager.getInstance().getSolarLand().setVisible(false);
				Scene.getInstance().redrawAll();
			}
		});
	}

	public void updateGraphs() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (selectedPart instanceof Foundation) {
					final Foundation f = (Foundation) selectedPart;
					switch (f.getProjectType()) {
					case Foundation.TYPE_BUILDING:
						buildingCostGraph.addGraph(f);
						buildingDailyEnergyGraph.addGraph(f);
						break;
					case Foundation.TYPE_PV_STATION:
						pvProjectCostGraph.addGraph(f);
						pvStationDailyEnergyGraph.addGraph(f);
						break;
					case Foundation.TYPE_CSP_STATION:
						cspProjectCostGraph.addGraph(f);
						cspStationDailyEnergyGraph.addGraph(f);
						break;
					}
				} else {
					buildingCostGraph.removeGraph();
					pvProjectCostGraph.removeGraph();
					cspProjectCostGraph.removeGraph();
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

	public void setComputingStartMillis(final long t) {
		computingStartMillis = t;
	}

}
