package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
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
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.CityData;
import org.concord.energy3d.simulation.Cost;
import org.concord.energy3d.simulation.HeatLoad;
import org.concord.energy3d.simulation.SolarRadiation;
import org.concord.energy3d.util.Specifications;
import org.concord.energy3d.util.Util;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;

public class PropertiesPanel extends JPanel {

	public static final ReadOnlyColorRGBA[] solarColors = { ColorRGBA.BLUE, ColorRGBA.GREEN, ColorRGBA.YELLOW, ColorRGBA.RED };
	final static String[] U_FACTOR_CHOICES_WALL = { "1.42 (16\" brick masonry)", "0.44 (R13, 2x4 w/cellulose/fiberglass)", "0.32 (R18, 2x4 w/cellulose/fiberglass & 1\" rigid foam exterior)", "0.28 (R20, 2x6 w/cellulose/fiberglass)", "0.23 (R25, 2x6 w/cellulose/fiberglass & 1\" rigid foam exterior)" };
	final static String[] U_FACTOR_CHOICES_ROOF = { "1.0 (old house)", "0.26 (R22, cellulose/fiberglass)", "0.15 (R38, cellulose/fiberglass)", "0.11 (R50, cellulose/fiberglass)" };
	final static String[] U_FACTOR_CHOICES_DOOR = { "2.0 (wood)", "0.6 (insulated)" };
	final static String[] U_FACTOR_CHOICES_WINDOW = { "5.91 (single pane, 3 mm glass)", "3.12 (double pane, 6 mm airspace)", "2.66 (double pane, 6 mm argon space)", "1.53 (double pane, 13 mm argon space)" };
	final static String[] WINDOW_SHGC_CHOICES = { "25", "50", "80" };
	final static String[] SOLAR_PANEL_CONVERSION_EFFICIENCY_CHOICES = { "10", "15", "20" };

	private static final long serialVersionUID = 1L;
	private static final PropertiesPanel instance = new PropertiesPanel();
	private final DecimalFormat twoDecimals = new DecimalFormat();
	private final DecimalFormat noDecimals = new DecimalFormat();
	private static boolean autoRecomputeEnergy = false;
	private Thread thread;
	private boolean computeRequest;
	private boolean cancel;
	private Object disableActionsRequester;
	private boolean alreadyRenderedHeatmap = false;
	private boolean computeEnabled = true;
	private final List<PropertyChangeListener> propertyChangeListeners = Collections.synchronizedList(new ArrayList<PropertyChangeListener>());

	public enum UpdateRadiation {
		ALWAYS, ONLY_IF_SLECTED_IN_GUI
	};

	private final JComboBox<String> wallsComboBox;
	private final JComboBox<String> doorsComboBox;
	private final JComboBox<String> windowsComboBox;
	private final JComboBox<String> roofsComboBox;
	private final JComboBox<String> cityComboBox;
	private final JComboBox<String> solarPanelEfficiencyComboBox;
	private final JComboBox<String> windowSHGCComboBox;
	private final JTextField heatingTextField;
	private final JTextField coolingTextField;
	private final JTextField netEnergyTextField;
	private final JSpinner insideTemperatureSpinner;
	private final JTextField outsideTemperatureField;
	private final JTextField sunshineHoursField;
	private final JLabel dateLabel;
	private final JLabel timeLabel;
	private final JSpinner dateSpinner;
	private final JSpinner timeSpinner;
	private final JLabel latitudeLabel;
	private final JSpinner latitudeSpinner;
	private final JPanel heatMapPanel;
	private final JSlider colorMapSlider;
	private final JProgressBar progressBar;
	private final ColorBar budgetBar, heightBar, areaBar;
	private final JPanel budgetPanel, heightPanel, areaPanel;
	private JPanel partPanel;
	private JPanel buildingPanel;
	private JTextField windowTextField;
	private JTextField solarPanelTextField;
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
	private Foundation foundation;

	public static PropertiesPanel getInstance() {
		return instance;
	}

	private PropertiesPanel() {

		twoDecimals.setMaximumFractionDigits(2);
		noDecimals.setMaximumFractionDigits(0);

		setLayout(new BorderLayout());
		final JPanel dataPanel = new JPanel();
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
		add(new JScrollPane(dataPanel), BorderLayout.CENTER);

		final JPanel timeAndLocationPanel = new JPanel();
		timeAndLocationPanel.setToolTipText("<html>The outside temperature and the sun path<br>differ from time to time and from location to location.</html>");
		timeAndLocationPanel.setBorder(new TitledBorder(null, "Time & Location", TitledBorder.LEADING, TitledBorder.TOP));
		dataPanel.add(timeAndLocationPanel);
		final GridBagLayout gbl_panel_3 = new GridBagLayout();
		timeAndLocationPanel.setLayout(gbl_panel_3);

		dateLabel = new JLabel("Date: ");
		final GridBagConstraints gbc_dateLabel = new GridBagConstraints();
		gbc_dateLabel.gridx = 0;
		gbc_dateLabel.gridy = 0;
		timeAndLocationPanel.add(dateLabel, gbc_dateLabel);

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
		dateSpinner.addChangeListener(new ChangeListener() {
			boolean firstCall = true;

			@Override
			public void stateChanged(final ChangeEvent e) {
				if (firstCall) {
					firstCall = false;
					return;
				}
				if (disableActionsRequester == null) {
					final Heliodon heliodon = Heliodon.getInstance();
					if (heliodon != null)
						heliodon.setDate((Date) dateSpinner.getValue());
					compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					Scene.getInstance().setTreeLeaves();
					Scene.getInstance().setEdited(true);
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

		Arrays.sort(CityData.getInstance().getCities());
		cityComboBox = new JComboBox<String>();
		cityComboBox.setModel(new DefaultComboBoxModel<String>(CityData.getInstance().getCities()));
		cityComboBox.setSelectedItem("Boston, MA");
		cityComboBox.setMaximumRowCount(15);
		cityComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final String city = (String) cityComboBox.getSelectedItem();
				if (city.equals("")) {
					compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "No city is selected.\nSolar radiation will be overestimated.", "Warning", JOptionPane.WARNING_MESSAGE);
				} else {
					final Integer newLatitude = CityData.getInstance().getLatitutes().get(cityComboBox.getSelectedItem()).intValue();
					if (newLatitude.equals(latitudeSpinner.getValue()))
						compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					else
						setLatitude(newLatitude);
					if (CityData.getInstance().getSunshineHours().get(city) == null)
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "No sunshine data is found for " + city + ".\nSolar radiation will be overestimated.", "Warning", JOptionPane.WARNING_MESSAGE);
				}
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

		timeLabel = new JLabel("Time: ");
		final GridBagConstraints gbc_timeLabel = new GridBagConstraints();
		gbc_timeLabel.gridx = 0;
		gbc_timeLabel.gridy = 1;
		timeAndLocationPanel.add(timeLabel, gbc_timeLabel);

		timeSpinner = new JSpinner(new SpinnerDateModel());
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
				final Heliodon heliodon = Heliodon.getInstance();
				if (heliodon != null)
					heliodon.setTime((Date) timeSpinner.getValue());
				updateWeatherData();
				Scene.getInstance().setEdited(true);
				SceneManager.getInstance().changeSkyTexture();
				if (MainPanel.getInstance().getShadowButton().isSelected()) {
					SceneManager.getInstance().setShading(Heliodon.getInstance().isNightTime());
				}
				if (Scene.getInstance().getAlwaysComputeHeatFluxVectors() && SceneManager.getInstance().areHeatFluxVectorsShown()) { // for now, only heat flow arrows need to call redrawAll
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

		latitudeLabel = new JLabel("Latitude: ");
		final GridBagConstraints gbc_altitudeLabel = new GridBagConstraints();
		gbc_altitudeLabel.insets = new Insets(0, 1, 0, 0);
		gbc_altitudeLabel.gridx = 2;
		gbc_altitudeLabel.gridy = 1;
		timeAndLocationPanel.add(latitudeLabel, gbc_altitudeLabel);

		latitudeSpinner = new JSpinner();
		latitudeSpinner.setModel(new SpinnerNumberModel(Heliodon.DEFAULT_LATITUDE, -90, 90, 1));
		latitudeSpinner.addChangeListener(latitudeChangeListener);
		latitudeSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
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

		final JPanel conditionPanel = new JPanel();
		conditionPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Conditions", TitledBorder.LEADING, TitledBorder.TOP));
		dataPanel.add(conditionPanel);
		final GridBagLayout gbl_temperaturePanel = new GridBagLayout();
		conditionPanel.setLayout(gbl_temperaturePanel);

		final JLabel insideTemperatureLabel = new JLabel("Room \u00B0C: ");
		final GridBagConstraints gbc_insideTemperatureLabel = new GridBagConstraints();
		gbc_insideTemperatureLabel.gridx = 1;
		gbc_insideTemperatureLabel.gridy = 0;
		conditionPanel.add(insideTemperatureLabel, gbc_insideTemperatureLabel);

		insideTemperatureSpinner = new JSpinner();
		insideTemperatureSpinner.setToolTipText("Thermostat temperature setting for the inside of the house");
		insideTemperatureSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (disableActionsRequester == null)
					compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
				Scene.getInstance().setEdited(true);
			}
		});
		insideTemperatureSpinner.setModel(new SpinnerNumberModel(20, -70, 60, 1));
		final GridBagConstraints gbc_insideTemperatureSpinner = new GridBagConstraints();
		gbc_insideTemperatureSpinner.gridx = 2;
		gbc_insideTemperatureSpinner.gridy = 0;
		conditionPanel.add(insideTemperatureSpinner, gbc_insideTemperatureSpinner);

		final JLabel outsideTemperatureLabel = new JLabel(" Outside: ");
		final GridBagConstraints gbc_outsideTemperatureLabel = new GridBagConstraints();
		gbc_outsideTemperatureLabel.gridx = 3;
		gbc_outsideTemperatureLabel.gridy = 0;
		conditionPanel.add(outsideTemperatureLabel, gbc_outsideTemperatureLabel);

		conditionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, conditionPanel.getPreferredSize().height));

		outsideTemperatureField = new JTextField(4);
		outsideTemperatureField.setToolTipText("Outside temperature at this time and day");
		outsideTemperatureField.setEnabled(false);
		final GridBagConstraints gbc_outsideTemperatureField = new GridBagConstraints();
		gbc_outsideTemperatureField.gridx = 4;
		gbc_outsideTemperatureField.gridy = 0;
		conditionPanel.add(outsideTemperatureField, gbc_outsideTemperatureField);

		final JLabel sunshineLabel = new JLabel(" Sunshine: ");
		final GridBagConstraints gbc_sunshineLabel = new GridBagConstraints();
		gbc_sunshineLabel.gridx = 5;
		gbc_sunshineLabel.gridy = 0;
		conditionPanel.add(sunshineLabel, gbc_sunshineLabel);

		sunshineHoursField = new JTextField(4);
		sunshineHoursField.setToolTipText("Average sunshine hours in this month");
		sunshineHoursField.setEnabled(false);
		final GridBagConstraints gbc_sunshineHoursField = new GridBagConstraints();
		gbc_sunshineHoursField.gridx = 6;
		gbc_sunshineHoursField.gridy = 0;
		conditionPanel.add(sunshineHoursField, gbc_sunshineHoursField);

		final JPanel uFactorPanel = new JPanel();
		uFactorPanel.setToolTipText("<html><b>U-factor</b><br>measures how well a building element conducts heat.</html>");
		uFactorPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "U-Factor W/(m\u00B2.\u00B0C)", TitledBorder.LEADING, TitledBorder.TOP));
		dataPanel.add(uFactorPanel);
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
		wallsComboBox.setModel(new DefaultComboBoxModel<String>(U_FACTOR_CHOICES_WALL));
		wallsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final double uFactor = HeatLoad.parseValue(wallsComboBox);
				if (foundation != null) {
					final int count = Scene.getInstance().countParts(foundation, Wall.class);
					if (count > 0)
						if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "<html>Do you want to set the U-factors of " + count + " existing walls of<br>the selected building (#" + foundation.getId() + ") to " + wallsComboBox.getSelectedItem() + "?</html>", "U-factor of Walls", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
							for (final HousePart p : Scene.getInstance().getParts()) {
								if (p instanceof Wall && p.getTopContainer() == foundation)
									((Wall) p).setUFactor(uFactor);
							}
						}
				}
				compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
				updateCost();
				Scene.getInstance().setEdited(true);
			}
		});
		JButton arrowButton = Util.getButtonSubComponent(wallsComboBox);
		if (arrowButton != null) {
			arrowButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					foundation = MainFrame.getInstance().autoSelectBuilding(true);
				}
			});
		}
		final GridBagConstraints gbc_wallsComboBox = new GridBagConstraints();
		gbc_wallsComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_wallsComboBox.gridx = 1;
		gbc_wallsComboBox.gridy = 0;
		uFactorPanel.add(wallsComboBox, gbc_wallsComboBox);

		final JLabel roofsLabel = new JLabel("Roofs:");
		final GridBagConstraints gbc_roofsLabel = new GridBagConstraints();
		gbc_roofsLabel.anchor = GridBagConstraints.EAST;
		gbc_roofsLabel.insets = new Insets(0, 0, 0, 5);
		gbc_roofsLabel.gridx = 2;
		gbc_roofsLabel.gridy = 0;
		uFactorPanel.add(roofsLabel, gbc_roofsLabel);

		roofsComboBox = new WideComboBox();
		roofsComboBox.setEditable(true);
		roofsComboBox.setModel(new DefaultComboBoxModel<String>(U_FACTOR_CHOICES_ROOF));
		roofsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final double uFactor = HeatLoad.parseValue(roofsComboBox);
				if (foundation != null) {
					final int count = Scene.getInstance().countParts(foundation, Roof.class);
					if (count > 0)
						if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "<html>Do you want to set the U-factor of the roof<br>of the selected building (#" + foundation.getId() + ") to " + roofsComboBox.getSelectedItem() + "?</html>", "U-factor of Roof", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
							for (final HousePart p : Scene.getInstance().getParts()) {
								if (p instanceof Roof && p.getTopContainer() == foundation)
									((Roof) p).setUFactor(uFactor);
							}
						}
				}
				compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
				updateCost();
				Scene.getInstance().setEdited(true);
			}
		});
		arrowButton = Util.getButtonSubComponent(roofsComboBox);
		if (arrowButton != null) {
			arrowButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					foundation = MainFrame.getInstance().autoSelectBuilding(true);
				}
			});
		}
		final GridBagConstraints gbc_roofsComboBox = new GridBagConstraints();
		gbc_roofsComboBox.gridx = 3;
		gbc_roofsComboBox.gridy = 0;
		uFactorPanel.add(roofsComboBox, gbc_roofsComboBox);

		final JLabel windowsLabel = new JLabel("Windows:");
		final GridBagConstraints gbc_windowsLabel = new GridBagConstraints();
		gbc_windowsLabel.anchor = GridBagConstraints.EAST;
		gbc_windowsLabel.insets = new Insets(0, 0, 0, 5);
		gbc_windowsLabel.gridx = 0;
		gbc_windowsLabel.gridy = 1;
		uFactorPanel.add(windowsLabel, gbc_windowsLabel);

		windowsComboBox = new WideComboBox();
		windowsComboBox.setEditable(true);
		windowsComboBox.setModel(new DefaultComboBoxModel<String>(U_FACTOR_CHOICES_WINDOW));
		windowsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final double uFactor = HeatLoad.parseValue(windowsComboBox);
				if (foundation != null) {
					final int count = Scene.getInstance().countParts(foundation, Window.class);
					if (count > 0)
						if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "<html>Do you want to set the U-factors of " + count + " existing windows of<br>the selected building (#" + foundation.getId() + ") to " + windowsComboBox.getSelectedItem() + "?</html>", "U-factor of Windows", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
							for (final HousePart p : Scene.getInstance().getParts()) {
								if (p instanceof Window && p.getTopContainer() == foundation)
									((Window) p).setUFactor(uFactor);
							}
						}
				}
				compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
				updateCost();
				Scene.getInstance().setEdited(true);
			}
		});
		arrowButton = Util.getButtonSubComponent(windowsComboBox);
		if (arrowButton != null) {
			arrowButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					foundation = MainFrame.getInstance().autoSelectBuilding(true);
				}
			});
		}
		final GridBagConstraints gbc_windowsComboBox = new GridBagConstraints();
		gbc_windowsComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_windowsComboBox.gridx = 1;
		gbc_windowsComboBox.gridy = 1;
		uFactorPanel.add(windowsComboBox, gbc_windowsComboBox);

		final JLabel doorsLabel = new JLabel("Doors:");
		final GridBagConstraints gbc_doorsLabel = new GridBagConstraints();
		gbc_doorsLabel.anchor = GridBagConstraints.EAST;
		gbc_doorsLabel.insets = new Insets(0, 0, 5, 5);
		gbc_doorsLabel.gridx = 2;
		gbc_doorsLabel.gridy = 1;
		uFactorPanel.add(doorsLabel, gbc_doorsLabel);

		doorsComboBox = new WideComboBox();
		doorsComboBox.setEditable(true);
		doorsComboBox.setModel(new DefaultComboBoxModel<String>(U_FACTOR_CHOICES_DOOR));
		doorsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final double uFactor = HeatLoad.parseValue(doorsComboBox);
				if (foundation != null) {
					final int count = Scene.getInstance().countParts(foundation, Door.class);
					if (count > 0)
						if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "<html>Do you want to set the U-factors of " + count + " existing doors of<br>the selected building (#" + foundation.getId() + ") to " + doorsComboBox.getSelectedItem() + "?</html>", "U-factor of Doors", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
							for (final HousePart p : Scene.getInstance().getParts()) {
								if (p instanceof Door && p.getTopContainer() == foundation)
									((Door) p).setUFactor(uFactor);
							}
						}
				}
				compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
				updateCost();
				Scene.getInstance().setEdited(true);
			}
		});
		arrowButton = Util.getButtonSubComponent(doorsComboBox);
		if (arrowButton != null) {
			arrowButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					foundation = MainFrame.getInstance().autoSelectBuilding(true);
				}
			});
		}
		final GridBagConstraints gbc_doorsComboBox = new GridBagConstraints();
		gbc_doorsComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_doorsComboBox.gridx = 3;
		gbc_doorsComboBox.gridy = 1;
		uFactorPanel.add(doorsComboBox, gbc_doorsComboBox);

		uFactorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, uFactorPanel.getPreferredSize().height));

		final JPanel solarConversionPercentagePanel = new JPanel();
		solarConversionPercentagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, solarConversionPercentagePanel.getPreferredSize().height));
		solarConversionPercentagePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Solar Conversion (%)", TitledBorder.LEADING, TitledBorder.TOP));
		dataPanel.add(solarConversionPercentagePanel);

		final JLabel labelSHGC = new JLabel("Window (SHGC): ");
		labelSHGC.setToolTipText("<html><b>SHGC - Solar heat gain coefficient</b><br>measures the fraction of solar energy transmitted through a window.</html>");
		solarConversionPercentagePanel.add(labelSHGC);

		windowSHGCComboBox = new WideComboBox();
		windowSHGCComboBox.setEditable(true);
		windowSHGCComboBox.setModel(new DefaultComboBoxModel<String>(WINDOW_SHGC_CHOICES));
		windowSHGCComboBox.setSelectedIndex(1);
		windowSHGCComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				// validate the input
				final String s = (String) windowSHGCComboBox.getSelectedItem();
				double shgc = 50;
				try {
					shgc = Float.parseFloat(s);
				} catch (final NumberFormatException ex) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Wrong format: must be 25-80.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (shgc < 25 || shgc > 80) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Wrong range: must be 25-80.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (foundation != null) {
					final int count = Scene.getInstance().countParts(foundation, Window.class);
					if (count > 0)
						if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "<html>Do you want to set the solar heat gain coefficient of " + count + " existing windows<br>of the selected building (#" + foundation.getId() + ") to " + windowSHGCComboBox.getSelectedItem() + "%?</html>", "Solar Heat Gain Coffficient of Windows", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
							for (final HousePart p : Scene.getInstance().getParts()) {
								if (p instanceof Window && p.getTopContainer() == foundation)
									((Window) p).setSolarHeatGainCoefficient(shgc);
							}
						}
				}
				Scene.getInstance().setWindowSolarHeatGainCoefficient(shgc);
				updateCost();
				Scene.getInstance().setEdited(true);
			}
		});
		arrowButton = Util.getButtonSubComponent(windowSHGCComboBox);
		if (arrowButton != null) {
			arrowButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					foundation = MainFrame.getInstance().autoSelectBuilding(true);
				}
			});
		}
		solarConversionPercentagePanel.add(windowSHGCComboBox);

		final JLabel labelPV = new JLabel("Solar Panel: ");
		labelPV.setToolTipText("<html><b>Solar photovoltaic efficiency</b><br>measures the fraction of solar energy converted into electricity by a solar panel.</html>");
		solarConversionPercentagePanel.add(labelPV);

		solarPanelEfficiencyComboBox = new WideComboBox();
		solarPanelEfficiencyComboBox.setEditable(true);
		solarPanelEfficiencyComboBox.setModel(new DefaultComboBoxModel<String>(SOLAR_PANEL_CONVERSION_EFFICIENCY_CHOICES));
		solarPanelEfficiencyComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				// validate the input
				final String s = (String) solarPanelEfficiencyComboBox.getSelectedItem();
				double eff = 10;
				try {
					eff = Float.parseFloat(s);
				} catch (final NumberFormatException ex) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Wrong format: must be 10-20.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (eff < 10 || eff > 30) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Wrong range: must be 10-30.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (foundation != null) {
					final int count = Scene.getInstance().countParts(foundation, SolarPanel.class);
					if (count > 0)
						if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "<html>Do you want to set the efficiency of " + count + " existing solar panels<br>of the selected building (#" + foundation.getId() + ") to " + solarPanelEfficiencyComboBox.getSelectedItem() + "%?</html>", "Solar Panel Conversion Efficiency", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
							for (final HousePart p : Scene.getInstance().getParts()) {
								if (p instanceof SolarPanel && p.getTopContainer() == foundation)
									((SolarPanel) p).setEfficiency(eff);
							}
						}
				}
				Scene.getInstance().setSolarPanelEfficiency(eff);
				updateCost();
				Scene.getInstance().setEdited(true);
			}
		});
		arrowButton = Util.getButtonSubComponent(solarPanelEfficiencyComboBox);
		if (arrowButton != null) {
			arrowButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					foundation = MainFrame.getInstance().autoSelectBuilding(true);
				}
			});
		}
		solarConversionPercentagePanel.add(solarPanelEfficiencyComboBox);

		heatMapPanel = new JPanel(new BorderLayout());
		heatMapPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Heat Map Contrast", TitledBorder.LEADING, TitledBorder.TOP));
		dataPanel.add(heatMapPanel);

		colorMapSlider = new MySlider();
		colorMapSlider.setToolTipText("<html>Increase or decrease the color contrast of the heat map.</html>");
		colorMapSlider.setMinimum(15);
		colorMapSlider.setMaximum(95);
		colorMapSlider.setMinimumSize(colorMapSlider.getPreferredSize());
		colorMapSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				if (!colorMapSlider.getValueIsAdjusting()) {
					compute(SceneManager.getInstance().getSolarHeatMap() ? UpdateRadiation.ALWAYS : UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					Scene.getInstance().setEdited(true, false);
				}
			}
		});
		colorMapSlider.setSnapToTicks(true);
		colorMapSlider.setMinorTickSpacing(1);
		colorMapSlider.setMajorTickSpacing(5);
		heatMapPanel.add(colorMapSlider, BorderLayout.CENTER);
		heatMapPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, heatMapPanel.getPreferredSize().height));

		partPanel = new JPanel();
		partPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Part", TitledBorder.LEADING, TitledBorder.TOP));
		dataPanel.add(partPanel);

		buildingPanel = new JPanel();
		buildingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Building", TitledBorder.LEADING, TitledBorder.TOP));
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
		areaBar.setMaximum(Specifications.getInstance().getMaximumArea());
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
		heightBar.setMaximum(Specifications.getInstance().getMaximumHeight());
		heightPanel.add(heightBar, BorderLayout.CENTER);

		// cost for the selected building

		budgetPanel = new JPanel(new BorderLayout());
		budgetPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Cost ($)", TitledBorder.LEADING, TitledBorder.TOP));
		budgetPanel.setToolTipText("<html>The total material cost for the selected building<br><b>Must not exceed the limit (if specified).</b></html>");
		buildingPanel.add(budgetPanel);
		budgetBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
		budgetBar.setToolTipText(budgetPanel.getToolTipText());
		budgetBar.setPreferredSize(new Dimension(200, 16));
		budgetBar.setMaximum(Specifications.getInstance().getMaximumBudget());
		budgetBar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() > 1)
					Cost.getInstance().showGraph();
			}
		});
		budgetPanel.add(budgetBar, BorderLayout.CENTER);

		final Component verticalGlue = Box.createVerticalGlue();
		dataPanel.add(verticalGlue);

		progressBar = new JProgressBar();
		add(progressBar, BorderLayout.SOUTH);

		JPanel target = buildingPanel;
		target.setMaximumSize(new Dimension(target.getMaximumSize().width, target.getPreferredSize().height));

		final JPanel energyTodayPanel = new JPanel();
		buildingPanel.add(energyTodayPanel);
		energyTodayPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Energy Today (kWh)", TitledBorder.LEADING, TitledBorder.TOP));
		final GridBagLayout gbl_panel_1 = new GridBagLayout();
		energyTodayPanel.setLayout(gbl_panel_1);

		final JLabel windowLabel = new JLabel("Windows");
		windowLabel.setToolTipText("Renewable energy gained through windows");
		windowLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_windowLabel = new GridBagConstraints();
		gbc_windowLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_windowLabel.insets = new Insets(0, 0, 5, 5);
		gbc_windowLabel.gridx = 0;
		gbc_windowLabel.gridy = 0;
		energyTodayPanel.add(windowLabel, gbc_windowLabel);

		final JLabel solarPanelLabel = new JLabel("Solar Panels");
		solarPanelLabel.setToolTipText("Renewable energy harvested from solar panels");
		solarPanelLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_solarPanelLabel = new GridBagConstraints();
		gbc_solarPanelLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_solarPanelLabel.insets = new Insets(0, 0, 5, 5);
		gbc_solarPanelLabel.gridx = 1;
		gbc_solarPanelLabel.gridy = 0;
		energyTodayPanel.add(solarPanelLabel, gbc_solarPanelLabel);

		final JLabel heatingLabel = new JLabel("Heater");
		heatingLabel.setToolTipText("Nonrenewable energy for heating the building");
		heatingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_heatingLabel = new GridBagConstraints();
		gbc_heatingLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_heatingLabel.insets = new Insets(0, 0, 5, 5);
		gbc_heatingLabel.gridx = 2;
		gbc_heatingLabel.gridy = 0;
		energyTodayPanel.add(heatingLabel, gbc_heatingLabel);

		final JLabel coolingLabel = new JLabel("AC");
		coolingLabel.setToolTipText("Nonrenewable energy for cooling the building");
		coolingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_coolingLabel = new GridBagConstraints();
		gbc_coolingLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_coolingLabel.insets = new Insets(0, 0, 5, 5);
		gbc_coolingLabel.gridx = 3;
		gbc_coolingLabel.gridy = 0;
		energyTodayPanel.add(coolingLabel, gbc_coolingLabel);

		final JLabel netEnergyLabel = new JLabel("Net");
		netEnergyLabel.setToolTipText("<html><b>Net energy cost for this building</b><br>Negative if the energy it generates exceeds the energy it consumes.</html>");
		netEnergyLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_netEnergyLabel = new GridBagConstraints();
		gbc_netEnergyLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_netEnergyLabel.insets = new Insets(0, 0, 5, 0);
		gbc_netEnergyLabel.gridx = 4;
		gbc_netEnergyLabel.gridy = 0;
		energyTodayPanel.add(netEnergyLabel, gbc_netEnergyLabel);

		windowTextField = new JTextField();
		windowTextField.setToolTipText(windowLabel.getToolTipText());
		final GridBagConstraints gbc_windowTextField = new GridBagConstraints();
		gbc_windowTextField.weightx = 1.0;
		gbc_windowTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_windowTextField.insets = new Insets(0, 0, 0, 5);
		gbc_windowTextField.gridx = 0;
		gbc_windowTextField.gridy = 1;
		energyTodayPanel.add(windowTextField, gbc_windowTextField);
		windowTextField.setEditable(false);
		windowTextField.setColumns(5);

		solarPanelTextField = new JTextField();
		solarPanelTextField.setToolTipText(solarPanelLabel.getToolTipText());
		final GridBagConstraints gbc_solarPanelTextField = new GridBagConstraints();
		gbc_solarPanelTextField.weightx = 1.0;
		gbc_solarPanelTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_solarPanelTextField.insets = new Insets(0, 0, 0, 5);
		gbc_solarPanelTextField.gridx = 1;
		gbc_solarPanelTextField.gridy = 1;
		energyTodayPanel.add(solarPanelTextField, gbc_solarPanelTextField);
		solarPanelTextField.setEditable(false);
		solarPanelTextField.setColumns(5);

		heatingTextField = new JTextField();
		heatingTextField.setToolTipText(heatingLabel.getToolTipText());
		heatingTextField.setEditable(false);
		final GridBagConstraints gbc_heatingTextField = new GridBagConstraints();
		gbc_heatingTextField.weightx = 1.0;
		gbc_heatingTextField.insets = new Insets(0, 0, 0, 5);
		gbc_heatingTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_heatingTextField.gridx = 2;
		gbc_heatingTextField.gridy = 1;
		energyTodayPanel.add(heatingTextField, gbc_heatingTextField);
		heatingTextField.setColumns(5);

		coolingTextField = new JTextField();
		coolingTextField.setToolTipText(coolingLabel.getToolTipText());
		coolingTextField.setEditable(false);
		final GridBagConstraints gbc_coolingTextField = new GridBagConstraints();
		gbc_coolingTextField.weightx = 1.0;
		gbc_coolingTextField.insets = new Insets(0, 0, 0, 5);
		gbc_coolingTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_coolingTextField.gridx = 3;
		gbc_coolingTextField.gridy = 1;
		energyTodayPanel.add(coolingTextField, gbc_coolingTextField);
		coolingTextField.setColumns(5);

		netEnergyTextField = new JTextField();
		netEnergyTextField.setToolTipText(netEnergyLabel.getToolTipText());
		netEnergyTextField.setEditable(false);
		final GridBagConstraints gbc_netEnergyTextField = new GridBagConstraints();
		gbc_netEnergyTextField.weightx = 1.0;
		gbc_netEnergyTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_netEnergyTextField.gridx = 4;
		gbc_netEnergyTextField.gridy = 1;
		energyTodayPanel.add(netEnergyTextField, gbc_netEnergyTextField);
		netEnergyTextField.setColumns(5);

		energyTodayPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, energyTodayPanel.getPreferredSize().height));

		final Dimension size = heatingLabel.getMinimumSize();
		windowLabel.setMinimumSize(size);
		solarPanelLabel.setMinimumSize(size);
		coolingLabel.setMinimumSize(size);
		netEnergyLabel.setMinimumSize(size);
		target = partPanel;
		target.setMaximumSize(new Dimension(target.getMaximumSize().width, target.getPreferredSize().height));
		partPanel.setLayout(new BoxLayout(partPanel, BoxLayout.Y_AXIS));

		partPropertiesPanel = new JPanel();
		partPanel.add(partPropertiesPanel);

		partProperty1Label = new JLabel("Width:");
		partPropertiesPanel.add(partProperty1Label);

		partProperty1TextField = new JTextField();
		partProperty1TextField.setEditable(false);
		partPropertiesPanel.add(partProperty1TextField);
		partProperty1TextField.setColumns(4);

		partProperty2Label = new JLabel("Height:");
		partPropertiesPanel.add(partProperty2Label);

		partProperty2TextField = new JTextField();
		partProperty2TextField.setEditable(false);
		partPropertiesPanel.add(partProperty2TextField);
		partProperty2TextField.setColumns(4);

		partProperty3Label = new JLabel("Insolation:");
		partPropertiesPanel.add(partProperty3Label);

		partProperty3TextField = new JTextField();
		partProperty3TextField.setEditable(false);
		partPropertiesPanel.add(partProperty3TextField);
		partProperty3TextField.setColumns(4);

		partProperty4Label = new JLabel();
		partProperty4TextField = new JTextField();
		partProperty4TextField.setEditable(false);
		partProperty4TextField.setColumns(4);

	}

	public void compute(final UpdateRadiation updateRadiation) {
		if (!computeEnabled)
			return;
		updateWeatherData(); // TODO: There got to be a better way to do this
		if (thread != null && thread.isAlive())
			computeRequest = true;
		else {
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
						}
						progress(0);
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
				HeatLoad.getInstance().computeEnergyToday((Calendar) Heliodon.getInstance().getCalender().clone(), (Integer) insideTemperatureSpinner.getValue());
				SolarRadiation.getInstance().computeTotalEnergyForBuildings();
				notifyPropertyChangeListeners(new PropertyChangeEvent(PropertiesPanel.this, "Energy calculation completed", 0, 1));
				updatePartEnergy();
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

	private void updateWeatherData() {
		final String city = (String) cityComboBox.getSelectedItem();
		if (city.equals("")) {
			outsideTemperatureField.setText("15\u00B0C");
			sunshineHoursField.setText("10");
		} else {
			final Calendar c = Heliodon.getInstance().getCalender();
			outsideTemperatureField.setText(Math.round(CityData.getInstance().computeOutsideTemperature(c)) + "\u00B0C");
			final Map<String, int[]> sunshineHours = CityData.getInstance().getSunshineHours();
			final int month = c.get(Calendar.MONTH);
			sunshineHoursField.setText(Math.round(sunshineHours.get(city)[month] / 30.0) + "hrs");
		}
	}

	public JSpinner getDateSpinner() {
		return dateSpinner;
	}

	public JSpinner getTimeSpinner() {
		return timeSpinner;
	}

	public JSpinner getInsideTemperatureSpinner() {
		return insideTemperatureSpinner;
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
		latitudeSpinner.removeChangeListener(latitudeChangeListener);
		latitudeSpinner.setValue(latitude);
		latitudeSpinner.addChangeListener(latitudeChangeListener);
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
			synchronized (propertyChangeListeners) {
				for (final PropertyChangeListener x : propertyChangeListeners) {
					x.propertyChange(evt);
				}
			}
		}
	}

	public void updatePartProperties() {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart instanceof Wall) {
			final int n = wallsComboBox.getItemCount();
			for (int i = 0; i < n; i++) {
				final double choice = Scene.parsePropertyString(U_FACTOR_CHOICES_WALL[i]);
				if (Util.isZero(choice - selectedPart.getUFactor())) {
					Util.selectSilently(wallsComboBox, i);
					break;
				}
			}
		} else if (selectedPart instanceof Roof) {
			final int n = roofsComboBox.getItemCount();
			for (int i = 0; i < n; i++) {
				final double choice = Scene.parsePropertyString(U_FACTOR_CHOICES_ROOF[i]);
				if (Util.isZero(choice - selectedPart.getUFactor())) {
					Util.selectSilently(roofsComboBox, i);
					break;
				}
			}
		} else if (selectedPart instanceof Door) {
			final int n = doorsComboBox.getItemCount();
			for (int i = 0; i < n; i++) {
				final double choice = Scene.parsePropertyString(U_FACTOR_CHOICES_DOOR[i]);
				if (Util.isZero(choice - selectedPart.getUFactor())) {
					Util.selectSilently(doorsComboBox, i);
					break;
				}
			}
		} else if (selectedPart instanceof Window) {
			int n = windowsComboBox.getItemCount();
			for (int i = 0; i < n; i++) {
				final double choice = Scene.parsePropertyString(U_FACTOR_CHOICES_WINDOW[i]);
				if (Util.isZero(choice - selectedPart.getUFactor())) {
					Util.selectSilently(windowsComboBox, i);
					break;
				}
			}
			n = windowSHGCComboBox.getItemCount();
			for (int i = 0; i < n; i++) {
				final double choice = Scene.parsePropertyString(WINDOW_SHGC_CHOICES[i]);
				if (Util.isZero(choice - ((Window) selectedPart).getSolarHeatGainCoefficient())) {
					Util.selectSilently(windowSHGCComboBox, i);
					break;
				}
			}
		} else if (selectedPart instanceof SolarPanel) {
			final int n = solarPanelEfficiencyComboBox.getItemCount();
			for (int i = 0; i < n; i++) {
				final double choice = Scene.parsePropertyString(SOLAR_PANEL_CONVERSION_EFFICIENCY_CHOICES[i]);
				if (Util.isZero(choice - ((SolarPanel) selectedPart).getEfficiency())) {
					Util.selectSilently(solarPanelEfficiencyComboBox, i);
					break;
				}
			}
		}
	}

	public void updatePartEnergy() {
		final boolean iradiationEnabled = MainPanel.getInstance().getEnergyViewButton().isSelected();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();

		if (selectedPart instanceof Foundation) {
			partProperty1Label.setText("Width:");
			partProperty2Label.setText("Length:");
			partProperty3Label.setText("Insolation:");
			partPropertiesPanel.remove(partProperty4Label);
			partPropertiesPanel.remove(partProperty4TextField);
		} else if (selectedPart instanceof Sensor) {
			partProperty1Label.setText("X:");
			partProperty2Label.setText("Y:");
			partProperty3Label.setText("Z:");
			partProperty4Label.setText("Data:");
			partPropertiesPanel.add(partProperty4Label);
			partPropertiesPanel.add(partProperty4TextField);
		} else {
			partProperty1Label.setText("Width:");
			partProperty2Label.setText("Height:");
			partProperty3Label.setText("Insolation:");
			partPropertiesPanel.remove(partProperty4Label);
			partPropertiesPanel.remove(partProperty4TextField);
		}
		partPropertiesPanel.revalidate();

		((TitledBorder) partPanel.getBorder()).setTitle("Part" + (selectedPart == null ? "" : (" - " + selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1))));
		partPanel.repaint();

		if (!iradiationEnabled || selectedPart == null || selectedPart instanceof Door || selectedPart instanceof Foundation)
			partProperty3TextField.setText("");
		else {
			if (selectedPart instanceof Sensor) {
				final String light = twoDecimals.format(selectedPart.getSolarPotentialToday() / selectedPart.getArea());
				final String heatFlux = twoDecimals.format(selectedPart.getTotalHeatLoss() / selectedPart.getArea());
				partProperty4TextField.setText(light + ", " + heatFlux);
				partProperty4TextField.setToolTipText("Light sensor: " + light + ", heat flux sensor: " + heatFlux);
			} else
				partProperty3TextField.setText(twoDecimals.format(selectedPart.getSolarPotentialToday()));
		}

		if (selectedPart != null && !(selectedPart instanceof Roof || selectedPart instanceof Floor || selectedPart instanceof Tree || selectedPart instanceof Human)) {
			if (selectedPart instanceof SolarPanel) {
				partProperty1TextField.setText(twoDecimals.format(SolarPanel.WIDTH));
				partProperty2TextField.setText(twoDecimals.format(SolarPanel.HEIGHT));
			} else if (selectedPart instanceof Sensor) {
				final ReadOnlyVector3 v = ((Sensor) selectedPart).getAbsPoint(0);
				partProperty1TextField.setText(twoDecimals.format(v.getX() * Scene.getInstance().getAnnotationScale()));
				partProperty2TextField.setText(twoDecimals.format(v.getY() * Scene.getInstance().getAnnotationScale()));
				partProperty3TextField.setText(twoDecimals.format(v.getZ() * Scene.getInstance().getAnnotationScale()));
			} else {
				partProperty1TextField.setText(twoDecimals.format(selectedPart.getAbsPoint(0).distance(selectedPart.getAbsPoint(2)) * Scene.getInstance().getAnnotationScale()));
				partProperty2TextField.setText(twoDecimals.format(selectedPart.getAbsPoint(0).distance(selectedPart.getAbsPoint(1)) * Scene.getInstance().getAnnotationScale()));
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
			if (iradiationEnabled) {
				windowTextField.setText(twoDecimals.format(selectedBuilding.getPassiveSolarToday()));
				solarPanelTextField.setText(twoDecimals.format(selectedBuilding.getPhotovoltaicToday()));
				heatingTextField.setText(twoDecimals.format(selectedBuilding.getHeatingToday()));
				coolingTextField.setText(twoDecimals.format(selectedBuilding.getCoolingToday()));
				netEnergyTextField.setText(twoDecimals.format(selectedBuilding.getTotalEnergyToday()));
			} else {
				windowTextField.setText("");
				solarPanelTextField.setText("");
				heatingTextField.setText("");
				coolingTextField.setText("");
				netEnergyTextField.setText("");
			}
			final double[] buildingGeometry = selectedBuilding.getBuildingGeometry();
			if (buildingGeometry != null) {
				heightBar.setValue((float) buildingGeometry[0]);
				areaBar.setValue((float) buildingGeometry[1]);
			} else {
				heightBar.setValue(0);
				areaBar.setValue(0);
			}
		} else {
			windowTextField.setText("");
			solarPanelTextField.setText("");
			heatingTextField.setText("");
			coolingTextField.setText("");
			netEnergyTextField.setText("");
			heightBar.setValue(0);
			areaBar.setValue(0);
		}

		heightBar.repaint();
		areaBar.repaint();

	}

	public void updateCost() {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		final Foundation selectedBuilding;
		if (selectedPart == null)
			selectedBuilding = null;
		else if (selectedPart instanceof Foundation)
			selectedBuilding = (Foundation) selectedPart;
		else
			selectedBuilding = selectedPart.getTopContainer();
		int n = 0;
		if (selectedBuilding != null)
			n = Cost.getInstance().getBuildingCost(selectedBuilding);
		budgetBar.setValue(n);
		budgetBar.repaint();
	}

	public void update() {
		updateCost();
		updateBudgetBar();
		updateAreaBar();
		updateHeightBar();
		updatePartEnergy();
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

	public JComboBox<String> getSolarPanelEfficiencyComboBox() {
		return solarPanelEfficiencyComboBox;
	}

	public JComboBox<String> getWindowSHGCComboBox() {
		return windowSHGCComboBox;
	}

	public void updateBudgetBar() {
		String t = "Cost (";
		t += Specifications.getInstance().isBudgetEnabled() ? "\u2264 $" + noDecimals.format(Specifications.getInstance().getMaximumBudget()) : "$";
		t += ")";
		budgetPanel.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder("TitledBorder.border"), t, TitledBorder.LEADING, TitledBorder.TOP));
		budgetBar.setEnabled(Specifications.getInstance().isBudgetEnabled());
		budgetBar.setMaximum(Specifications.getInstance().getMaximumBudget());
		budgetBar.repaint();
	}

	public void updateAreaBar() {
		String t = "Area (";
		if (Specifications.getInstance().isAreaEnabled())
			t += twoDecimals.format(Specifications.getInstance().getMinimumArea()) + " - " + twoDecimals.format(Specifications.getInstance().getMaximumArea());
		t += "\u33A1)";
		areaPanel.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder("TitledBorder.border"), t, TitledBorder.LEADING, TitledBorder.TOP));
		areaBar.setEnabled(Specifications.getInstance().isAreaEnabled());
		areaBar.setMinimum(Specifications.getInstance().getMinimumArea());
		areaBar.setMaximum(Specifications.getInstance().getMaximumArea());
		areaBar.repaint();
	}

	public void updateHeightBar() {
		String t = "Height (";
		if (Specifications.getInstance().isHeightEnabled())
			t += twoDecimals.format(Specifications.getInstance().getMinimumHeight()) + " - " + twoDecimals.format(Specifications.getInstance().getMaximumHeight());
		t += "m)";
		heightPanel.setBorder(BorderFactory.createTitledBorder(UIManager.getBorder("TitledBorder.border"), t, TitledBorder.LEADING, TitledBorder.TOP));
		heightBar.setEnabled(Specifications.getInstance().isHeightEnabled());
		heightBar.setMinimum(Specifications.getInstance().getMinimumHeight());
		heightBar.setMaximum(Specifications.getInstance().getMaximumHeight());
		heightBar.repaint();
	}

	public void turnOffCompute() {
		if (SceneManager.getInstance().getSolarHeatMap())
			MainPanel.getInstance().getEnergyViewButton().setSelected(false);
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

}
