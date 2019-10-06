package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

import javax.imageio.ImageIO;
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
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.metal.MetalTabbedPaneUI;
import javax.swing.text.DefaultCaret;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.agents.AnalysisEvent;
import org.concord.energy3d.agents.OperationEvent;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FoundationPolygon;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.GeoLocation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.NodeState;
import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.SolarCollector;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.UserData;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.AnnualEnvironmentalTemperature;
import org.concord.energy3d.simulation.CspProjectCost;
import org.concord.energy3d.simulation.DailyEnvironmentalTemperature;
import org.concord.energy3d.simulation.HeatLoad;
import org.concord.energy3d.simulation.LocationData;
import org.concord.energy3d.simulation.MonthlySunshineHours;
import org.concord.energy3d.simulation.PvProjectCost;
import org.concord.energy3d.simulation.SolarRadiation;
import org.concord.energy3d.simulation.Weather;
import org.concord.energy3d.speech.Talker;
import org.concord.energy3d.undo.ChangeCityCommand;
import org.concord.energy3d.undo.ChangeDateCommand;
import org.concord.energy3d.undo.ChangeLatitudeCommand;
import org.concord.energy3d.undo.ChangeSolarHeatMapColorContrastCommand;
import org.concord.energy3d.undo.ChangeTimeCommand;
import org.concord.energy3d.util.BugReporter;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;

public class EnergyPanel extends JPanel {

    public static final ReadOnlyColorRGBA[] solarColors = {ColorRGBA.BLUE, ColorRGBA.GREEN, ColorRGBA.YELLOW, ColorRGBA.RED};
    public static final DecimalFormat NO_DECIMAL = new DecimalFormat();
    public static final DecimalFormat ONE_DECIMAL = new DecimalFormat();
    public static final DecimalFormat TWO_DECIMALS = new DecimalFormat();
    public static final DecimalFormat FIVE_DECIMALS = new DecimalFormat();

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
    }

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
    private BuildingCostGraph buildingCostGraph;
    private PvProjectZoneCostGraph pvProjectZoneCostGraph;
    private CspProjectCostGraph cspProjectCostGraph;
    private BuildingDailyEnergyGraph buildingDailyEnergyGraph;
    private PvProjectDailyEnergyGraph pvProjectDailyEnergyGraph;
    private CspProjectDailyEnergyGraph cspProjectDailyEnergyGraph;
    private BuildingInfoPanel buildingInfoPanel;
    private PvProjectZoneInfoPanel pvProjectZoneInfoPanel;
    private CspProjectInfoPanel cspProjectInfoPanel;
    private JTabbedPane buildingTabbedPane, pvProjectTabbedPane, cspProjectTabbedPane, instructionTabbedPane;
    private TabbedPaneUI instructionTabbedPaneUI;
    private JPanel buildingPanel, pvProjectPanel, cspProjectPanel, instructionPanel;
    private volatile boolean canReadInstruction = true;
    private final MyEditorPane[] instructionSheets = new MyEditorPane[Scene.INSTRUCTION_SHEET_NUMBER];
    private boolean disableDateSpinner;
    private long computingStartMillis;

    public static EnergyPanel getInstance() {
        return instance;
    }

    private EnergyPanel() {

        NO_DECIMAL.setMaximumFractionDigits(0);
        ONE_DECIMAL.setMaximumFractionDigits(1);
        TWO_DECIMALS.setMaximumFractionDigits(2);
        FIVE_DECIMALS.setMaximumFractionDigits(5);

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
        final JTextField dateEditorField = dateEditor.getTextField();
        dateEditorField.setFont(new Font(dateEditorField.getFont().getName(), Font.PLAIN, dateEditorField.getFont().getSize() - (Config.isMac() ? 2 : 1)));
        dateEditorField.setColumns((int) ("September 30".length() * 0.7));
        dateSpinner.addChangeListener(new ChangeListener() {
            // private boolean firstCall = true;
            private Date lastDate;

            @Override
            public void stateChanged(final ChangeEvent e) {
                // if (firstCall) {
                // firstCall = false;
                // return;
                // }
                if (!disableDateSpinner) {
                    final ChangeDateCommand c = new ChangeDateCommand();
                    final Date d = (Date) dateSpinner.getValue();
                    if (Util.sameDateOfYear(d, c.getOldDate())) { // although GUI may not trigger this event, script can
                        return;
                    }
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
                    if (SceneManager.getInstance().getSolarHeatMap()) {
                        updateRadiationHeatMap();
                    }
                    updateScene();
                    Scene.getInstance().setEdited(true);
                    SceneManager.getInstance().getUndoManager().addEdit(c);
                    lastDate = d;
                }
            }
        });
        final GridBagConstraints gbc_dateSpinner = new GridBagConstraints();
        gbc_dateSpinner.insets = new Insets(0, 0, 1, 1);
        gbc_dateSpinner.gridx = 1;
        gbc_dateSpinner.gridy = 0;
        timeAndLocationPanel.add(dateSpinner, gbc_dateSpinner);

        ChangeListener latitudeChangeListener = new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                regionComboBox.setSelectedItem("");
            }
        };

        Arrays.sort(LocationData.getInstance().getCities());
        regionComboBox = new JComboBox<>();
        regionComboBox.setFont(new Font(regionComboBox.getFont().getName(), Font.PLAIN, regionComboBox.getFont().getSize() - (Config.isMac() ? 4 : 2)));
        regionComboBox.setModel(new DefaultComboBoxModel<String>(LocationData.getInstance().getCities()));
        regionComboBox.setSelectedItem("Boston, MA");
        regionComboBox.setMaximumRowCount(15);
        regionComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                final String city = (String) regionComboBox.getSelectedItem();
                if (city.equals("")) {
                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "No region is selected.\nEnergy simulation will not be accurate.", "Warning", JOptionPane.WARNING_MESSAGE);
                    Scene.getInstance().setCity(city);
                } else {
                    final ChangeCityCommand c = new ChangeCityCommand();
                    if (c.getOldValue().equals(city)) { // although GUI may not trigger this event, script can
                        return;
                    }
                    setLatitude((int) LocationData.getInstance().getLatitudes().get(regionComboBox.getSelectedItem()).floatValue());
                    Scene.getInstance().setCity(city);
                    SceneManager.getInstance().getUndoManager().addEdit(c);
                    final LocationData ld = LocationData.getInstance();
                    regionComboBox.setToolTipText("<html>(" + ld.getLatitudes().get(city) + "&deg;, " + ld.getLongitudes().get(city) + "&deg;), elevation " + ld.getAltitudes().get(city).intValue() + "m<br>Use Edit>Set Region... to select country and region.</html>");
                }
                if (SceneManager.getInstance().getSolarHeatMap()) {
                    updateRadiationHeatMap();
                }
                updateScene();
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
        final JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "h:mm a");
        timeSpinner.setEditor(timeEditor);
        final JTextField timeEditorField = timeEditor.getTextField();
        timeEditorField.setFont(new Font(timeEditorField.getFont().getName(), Font.PLAIN, timeEditorField.getFont().getSize() - (Config.isMac() ? 2 : 1)));
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
                if (Util.sameTimeOfDay(d, c.getOldTime())) {
                    return;
                }
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
                updateScene();
                // for now, only heat flow arrows need to call redrawAll
                if (Scene.getInstance().getAlwaysComputeHeatFluxVectors() && SceneManager.getInstance().areHeatFluxVectorsVisible()) {
                    SceneManager.getInstance().setHeatFluxDaily(false);
                    SceneManager.getTaskManager().update(() -> {
                        for (final HousePart part : Scene.getInstance().getParts()) {
                            part.drawHeatFlux();
                        }
                        return null;
                    });
                }
                Scene.getInstance().setEdited(true);
                SceneManager.getInstance().getUndoManager().addEdit(c);
                lastDate = d;
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
        latitudeSpinner.addChangeListener(e -> {
            final ChangeLatitudeCommand c = new ChangeLatitudeCommand();
            Heliodon.getInstance().setLatitude(((Double) latitudeSpinner.getValue()) / 180.0 * Math.PI);
            if (SceneManager.getInstance().getSolarHeatMap()) {
                updateRadiationHeatMap();
            }
            updateScene();
            Scene.getInstance().setEdited(true);
            SceneManager.getInstance().getUndoManager().addEdit(c);
        });
        ((JSpinner.DefaultEditor) latitudeSpinner.getEditor()).getTextField().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (Util.isRightClick(e)) {
                    final GeoLocation geo = Scene.getInstance().getGeoLocation();
                    if (geo == null) {
                        return;
                    }
                    final JPopupMenu popupMenu = new JPopupMenu();
                    final JMenuItem mi = new JMenuItem("\"lat\": " + FIVE_DECIMALS.format(geo.getLatitude()) + ", \"lng\": " + FIVE_DECIMALS.format(geo.getLongitude()));
                    mi.addActionListener(e12 -> Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(mi.getText()), null));
                    popupMenu.add(mi);
                    popupMenu.pack();
                    popupMenu.show(latitudeSpinner, 0, 0);
                }
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
                final JPopupMenu popupMenu = new JPopupMenu();
                JMenuItem mi = new JMenuItem("Daily Environmental Temperature...");
                mi.addActionListener(e1 -> {
                    if (checkCity()) {
                        new DailyEnvironmentalTemperature().showDialog();
                    }
                });
                popupMenu.add(mi);
                mi = new JMenuItem("Annual Environmental Temperature...");
                mi.addActionListener(e2 -> {
                    if (checkCity()) {
                        new AnnualEnvironmentalTemperature().showDialog();
                    }
                });
                popupMenu.add(mi);
                popupMenu.pack();
                popupMenu.show(outsideTemperatureField, 0, 0);
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
                final JPopupMenu popupMenu = new JPopupMenu();
                final JMenuItem mi = new JMenuItem("Monthly Sunshine Hours...");
                mi.addActionListener(event -> {
                    if (checkCity()) {
                        new MonthlySunshineHours().showDialog();
                    }
                });
                popupMenu.add(mi);
                popupMenu.pack();
                popupMenu.show(sunshineHoursField, 0, 0);
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
        partPanel.setBorder(createTitledBorder("-", true));
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
                    final JDialog d = PropertiesDialogFactory.getDialog();
                    if (d != null) {
                        final JPopupMenu popupMenu = new JPopupMenu();
                        final JMenuItem mi = new JMenuItem("Show More Properties...");
                        mi.addActionListener(event -> {
                            d.setLocationRelativeTo(popupMenu.getInvoker());
                            d.setVisible(true);
                        });
                        popupMenu.add(mi);
                        popupMenu.pack();
                        popupMenu.show(e.getComponent(), 0, 0);
                    }
                } else {
                    if (Util.isRightClick(e) && e.getComponent() == partProperty1TextField) { // convenience tool for copying data for virtual solar grid
                        switch (Scene.getInstance().getProjectType()) {
                            case Foundation.TYPE_PV_PROJECT:
                                final JPopupMenu popupMenu = new JPopupMenu();
                                final JMenuItem mi = new JMenuItem("\"module_number\": " + Scene.getInstance().countSolarPanels());
                                mi.addActionListener(event -> Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(mi.getText()), null));
                                popupMenu.add(mi);
                                popupMenu.pack();
                                popupMenu.show(e.getComponent(), 0, 0);
                                break;
                            case Foundation.TYPE_CSP_PROJECT:
                                // TODO
                                break;
                        }
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

        // PV project panel
        pvProjectPanel = new JPanel();
        pvProjectPanel.setBorder(createTitledBorder("Photovoltaic Solar Power System", true));
        pvProjectPanel.setLayout(new BoxLayout(pvProjectPanel, BoxLayout.Y_AXIS));

        pvProjectTabbedPane = new JTabbedPane();
        pvProjectTabbedPane.setFont(new Font(pvProjectTabbedPane.getFont().getName(), Font.PLAIN, pvProjectTabbedPane.getFont().getSize() - 1));
        pvProjectPanel.add(pvProjectTabbedPane);

        pvProjectZoneInfoPanel = new PvProjectZoneInfoPanel();
        pvProjectTabbedPane.add("Info", pvProjectZoneInfoPanel);

        pvProjectZoneCostGraph = new PvProjectZoneCostGraph(); // cost graph
        pvProjectTabbedPane.add("Cost", pvProjectZoneCostGraph);

        pvProjectDailyEnergyGraph = new PvProjectDailyEnergyGraph();
        pvProjectTabbedPane.add("Output", pvProjectDailyEnergyGraph);

        pvProjectTabbedPane.addChangeListener(e -> {
            if (pvProjectTabbedPane.getSelectedComponent() == pvProjectDailyEnergyGraph) {
                if (SceneManager.getInstance().getSolarHeatMap()) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof Foundation) {
                        pvProjectDailyEnergyGraph.addGraph((Foundation) selectedPart);
                    } else {
                        pvProjectDailyEnergyGraph.removeGraph();
                    }
                }
            }
            TimeSeriesLogger.getInstance().logGraphTab(pvProjectTabbedPane.getTitleAt(pvProjectTabbedPane.getSelectedIndex()));
        });
        pvProjectPanel.setMaximumSize(new Dimension(pvProjectPanel.getMaximumSize().width, pvProjectPanel.getPreferredSize().height));

        // CSP project panel
        cspProjectPanel = new JPanel();
        cspProjectPanel.setBorder(createTitledBorder("Concentrated Solar Power System", true));
        cspProjectPanel.setLayout(new BoxLayout(cspProjectPanel, BoxLayout.Y_AXIS));

        cspProjectTabbedPane = new JTabbedPane();
        cspProjectTabbedPane.setFont(new Font(cspProjectTabbedPane.getFont().getName(), Font.PLAIN, cspProjectTabbedPane.getFont().getSize() - 1));
        cspProjectPanel.add(cspProjectTabbedPane);

        cspProjectInfoPanel = new CspProjectInfoPanel();
        cspProjectTabbedPane.add("Info", cspProjectInfoPanel);

        cspProjectCostGraph = new CspProjectCostGraph(); // cost graph
        cspProjectTabbedPane.add("Cost", cspProjectCostGraph);

        cspProjectDailyEnergyGraph = new CspProjectDailyEnergyGraph();
        cspProjectTabbedPane.add("Output", cspProjectDailyEnergyGraph);

        cspProjectTabbedPane.addChangeListener(e -> {
            if (cspProjectTabbedPane.getSelectedComponent() == cspProjectDailyEnergyGraph) {
                if (SceneManager.getInstance().getSolarHeatMap()) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof Foundation) {
                        cspProjectDailyEnergyGraph.addGraph((Foundation) selectedPart);
                    } else {
                        cspProjectDailyEnergyGraph.removeGraph();
                    }
                }
            }
            TimeSeriesLogger.getInstance().logGraphTab(cspProjectTabbedPane.getTitleAt(cspProjectTabbedPane.getSelectedIndex()));
        });
        cspProjectPanel.setMaximumSize(new Dimension(cspProjectPanel.getMaximumSize().width, cspProjectPanel.getPreferredSize().height));

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
        adjustThermostatButton.addActionListener(e -> {
            final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
            if (selectedPart == null) {
                return;
            }
            Foundation foundation;
            if (selectedPart instanceof Foundation) {
                foundation = (Foundation) selectedPart;
            } else {
                foundation = selectedPart.getTopContainer();
            }
            MainPanel.getInstance().getEnergyButton().setSelected(false);
            new ThermostatDialog(foundation).setVisible(true);
            TimeSeriesLogger.getInstance().logAdjustThermostatButton();
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

        buildingTabbedPane.addChangeListener(e -> {
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
        });
        buildingPanel.setMaximumSize(new Dimension(buildingPanel.getMaximumSize().width, buildingPanel.getPreferredSize().height));

        dataPanel.add(Box.createVerticalGlue());

        // instruction panel

        instructionPanel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            public String getToolTipText(final MouseEvent e) {
                final Border border = getBorder();
                if (border instanceof TitledBorder) {
                    final TitledBorder tb = (TitledBorder) border;
                    final FontMetrics fm = getFontMetrics(getFont());
                    final int titleWidth = fm.stringWidth(tb.getTitle()) + 20;
                    final Rectangle bounds = new Rectangle(0, 0, titleWidth, fm.getHeight());
                    return bounds.contains(e.getPoint()) ? super.getToolTipText() : null;
                }
                return super.getToolTipText(e);
            }
        };
        instructionPanel.setToolTipText("Click here to listen");
        instructionPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                final Border border = instructionPanel.getBorder();
                if (border instanceof TitledBorder) {
                    final TitledBorder tb = (TitledBorder) border;
                    final FontMetrics fm = instructionPanel.getFontMetrics(instructionPanel.getFont());
                    final int titleWidth = fm.stringWidth(tb.getTitle()) + 20;
                    final Rectangle bounds = new Rectangle(0, 0, titleWidth, fm.getHeight());
                    if (bounds.contains(e.getPoint())) {
                        final int i = instructionTabbedPane.getSelectedIndex();
                        String text = null;
                        try {
                            text = instructionSheets[i].getRawText();
                        } catch (final IOException ex) {
                            ex.printStackTrace();
                        }
                        if (text != null && !text.trim().equals("")) {
                            if (MainApplication.VERSION.compareTo("8.4.5") >= 0) {
                                if (canReadInstruction) {
                                    setReadInstructionState(true);
                                    instructionPanel.setToolTipText("Click here to stop the speaker");
                                    Talker.getInstance().say(text);
                                    Talker.getInstance().setCompletionCallback(() -> resetReadInstruction());
                                    canReadInstruction = false;
                                } else {
                                    Talker.getInstance().interrupt();
                                    canReadInstruction = true;
                                }
                            }
                        }
                    }
                    instructionPanel.repaint();
                }
            }
        });
        instructionPanel.setLayout(new BoxLayout(instructionPanel, BoxLayout.Y_AXIS));
        setReadInstructionState(false);
        dataPanel.add(instructionPanel);

        instructionTabbedPane = new JTabbedPane();
        instructionTabbedPane.setFont(new Font(instructionTabbedPane.getFont().getName(), Font.PLAIN, instructionTabbedPane.getFont().getSize() - (Config.isMac() ? 3 : 2)));
        instructionTabbedPaneUI = instructionTabbedPane.getUI();
        instructionTabbedPane.addChangeListener(e -> {
            if (instructionTabbedPane.isShowing()) {
                final HashMap<String, Object> attributes = new HashMap<>();
                attributes.put("Sheet", instructionTabbedPane.getSelectedIndex());
                MainApplication.addEvent(new OperationEvent(Scene.getURL(), System.currentTimeMillis(), "Instruction Sheet Selection", attributes));
            }
        });
        instructionPanel.add(instructionTabbedPane);

        for (int i = 0; i < instructionSheets.length; i++) {
            instructionSheets[i] = new MyEditorPane(i, false);
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
        colorMapSlider.addChangeListener(e -> {
            if (!colorMapSlider.getValueIsAdjusting()) {
                ((Component) SceneManager.getInstance().getCanvas()).setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                final ChangeSolarHeatMapColorContrastCommand c = new ChangeSolarHeatMapColorContrastCommand();
                Scene.getInstance().setSolarHeatMapColorContrast(colorMapSlider.getValue());
                SceneManager.getTaskManager().update(() -> {
                    // compute(SceneManager.getInstance().getSolarHeatMap() ? UpdateRadiation.ALWAYS : UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
                    SolarRadiation.getInstance().updateTextures();
                    SceneManager.getInstance().refresh();
                    EventQueue.invokeLater(() -> ((Component) SceneManager.getInstance().getCanvas()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)));
                    return null;
                });
                Scene.getInstance().setEdited(true);
                SceneManager.getInstance().getUndoManager().addEdit(c);
            }
        });
        heatMapPanel.add(colorMapSlider, BorderLayout.CENTER);
        heatMapPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, heatMapPanel.getPreferredSize().height));

        progressBar = new JProgressBar();
        add(progressBar, BorderLayout.SOUTH);

    }

    public void updateScene() {
        updateWeatherData();
        updateThermostat();
        SceneManager.getTaskManager().update(() -> {
            Heliodon.getInstance().drawSun(); // we must call this to update the translation of the mesh so as to know if the sun is above the horizon
            SceneManager.getInstance().changeSkyTexture();
            SceneManager.getInstance().setShading(Heliodon.getInstance().isNightTime());
            Scene.getInstance().updateTrackables();
            Scene.getInstance().updateTreeLeaves();
            return null;
        });
    }

    public void showInstructionTabHeaders(final boolean b) {
        if (b) {
            instructionTabbedPane.setUI(instructionTabbedPaneUI);
        } else {
            instructionTabbedPane.setUI(new MetalTabbedPaneUI() {
                @Override
                protected int calculateTabAreaHeight(final int tabPlacement, final int horizRunCount, final int maxTabHeight) {
                    return 0;
                }

                @Override
                protected void paintTabArea(final Graphics g, final int tabPlacement, final int selectedIndex) {
                }
            });
        }
    }

    public void compute(final UpdateRadiation updateRadiation) {
        if (!computeEnabled) {
            return;
        }
        computingStartMillis = System.currentTimeMillis();
        // must run this Swing UI update in the event queue to avoid a possible deadlock
        EventQueue.invokeLater(() -> {
            updateWeatherData(); // TODO: There got to be a better way to do this.
            ((Component) SceneManager.getInstance().getCanvas()).setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        });
        final boolean doCompute = updateRadiation == UpdateRadiation.ALWAYS || (SceneManager.getInstance().getSolarHeatMap() && (!alreadyRenderedHeatmap || autoRecomputeEnergy));
        if (!doCompute && computing) {
            cancel();
            return;
        }
        SceneManager.getTaskManager().update(() -> {
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
                BugReporter.report(e);
                return null;
            } finally {
                EventQueue.invokeLater(() -> ((Component) SceneManager.getInstance().getCanvas()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)));
            }
            // must run this Swing UI update in the event queue to avoid a possible deadlock
            EventQueue.invokeLater(() -> {
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
                                MainApplication.addEvent(new AnalysisEvent(Scene.getURL(), System.currentTimeMillis(), buildingDailyEnergyGraph.getClass().getSimpleName(), buildingDailyEnergyGraph.getData()));
                                break;
                            case Foundation.TYPE_PV_PROJECT:
                                Util.setSilently(pvProjectTabbedPane, pvProjectDailyEnergyGraph);
                                pvProjectDailyEnergyGraph.addGraph(f);
                                TimeSeriesLogger.getInstance().logAnalysis(pvProjectDailyEnergyGraph);
                                MainApplication.addEvent(new AnalysisEvent(Scene.getURL(), System.currentTimeMillis(), pvProjectDailyEnergyGraph.getClass().getSimpleName(), pvProjectDailyEnergyGraph.getData()));
                                break;
                            case Foundation.TYPE_CSP_PROJECT:
                                Util.setSilently(cspProjectTabbedPane, cspProjectDailyEnergyGraph);
                                cspProjectDailyEnergyGraph.addGraph(f);
                                TimeSeriesLogger.getInstance().logAnalysis(cspProjectDailyEnergyGraph);
                                MainApplication.addEvent(new AnalysisEvent(Scene.getURL(), System.currentTimeMillis(), cspProjectDailyEnergyGraph.getClass().getSimpleName(), cspProjectDailyEnergyGraph.getData()));
                                break;
                        }
                    }
                }
                computing = false;
            });
            return null;
        });
    }

    boolean adjustCellSize() {
        final double cellSize = Scene.getInstance().getSolarStep() * Scene.getInstance().getScale();
        final int cellCount = (int) (Scene.getInstance().getTotalFoundationAreas() / (cellSize * cellSize));
        final int maxCells = 100000; // should we make this dependent of project type?
        if (cellCount > maxCells) {
            final Object[] options = new Object[]{"OK, adjust it!", "No, just go with it!"};
            final int x = JOptionPane.showOptionDialog(MainFrame.getInstance(), "<html>Cell size for others (" + cellSize + "m) is probably too small for this model.<br>Consider adjust it to speed up simulations.</html>", "Cell Size Adjustment Suggestion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
            if (x == JOptionPane.YES_OPTION) {
                final VisualizationSettingsDialog v = new VisualizationSettingsDialog();
                v.getCellSizeField().selectAll();
                v.getCellSizeField().requestFocusInWindow();
                v.setVisible(true);
                return true;
            } else if (x == -1) {
                return true;
            }
        }
        return false;
    }

    public void computeNow() {
        try {
            cancel = false;
            EventQueue.invokeLater(() -> {
                progressBar.setValue(0);
                progressBar.setStringPainted(false);
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

            final double[] hourlyResults = new double[24];
            final List<SolarCollector> collectors = Scene.getInstance().getAllSolarCollectorsNoSensor();
            for (int i = 0; i < 24; i++) {
                SolarRadiation.getInstance().computeEnergyAtHour(i);
                double output = 0;
                if (!collectors.isEmpty()) {
                    for (final SolarCollector sc : collectors) {
                        output += sc.getYieldNow();
                    }
                }
                hourlyResults[i] = output;
            }
            Scene.getInstance().setSolarResults(c.get(Calendar.MONTH), hourlyResults);

            EventQueue.invokeLater(() -> {
                updateWeatherData();
                updateProperties();
                progressBar.setValue(100);
                progressBar.setString("100% (" + ONE_DECIMAL.format((System.currentTimeMillis() - computingStartMillis) * 0.001) + " seconds)");
            });

        } catch (final CancellationException e) {
            System.out.println("Energy calculation cancelled.");
            SolarRadiation.getInstance().resetTrackables();
        }

    }

    // TODO: There should be a better way to do this.
    public void updateRadiationHeatMap() {
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
                sunshineHoursField.setText(ONE_DECIMAL.format(sunshineHours.get(city)[month] / 30.0) + " hours");
            } catch (final Exception e) {
                BugReporter.report(e);
            }
        }
    }

    public JTabbedPane getBuildingTabbedPane() {
        return buildingTabbedPane;
    }

    public JTabbedPane getPvProjectTabbedPane() {
        return pvProjectTabbedPane;
    }

    public JTabbedPane getCspProjectTabbedPane() {
        return cspProjectTabbedPane;
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

    public PvProjectZoneInfoPanel getPvProjectZoneInfoPanel() {
        return pvProjectZoneInfoPanel;
    }

    public CspProjectInfoPanel getCspProjectInfoPanel() {
        return cspProjectInfoPanel;
    }

    public BuildingCostGraph getBuildingCostGraph() {
        return buildingCostGraph;
    }

    public PvProjectZoneCostGraph getPvProjectZoneCostGraph() {
        return pvProjectZoneCostGraph;
    }

    public CspProjectCostGraph getCspProjectCostGraph() {
        return cspProjectCostGraph;
    }

    public BuildingDailyEnergyGraph getBuildingDailyEnergyGraph() {
        return buildingDailyEnergyGraph;
    }

    public PvProjectDailyEnergyGraph getPvProjectDailyEnergyGraph() {
        return pvProjectDailyEnergyGraph;
    }

    public CspProjectDailyEnergyGraph getCspProjectDailyEnergyGraph() {
        return cspProjectDailyEnergyGraph;
    }

    /**
     * call when loading a new file
     */
    public void clearAllGraphs() {
        buildingCostGraph.removeGraph();
        pvProjectZoneCostGraph.removeGraph();
        cspProjectCostGraph.removeGraph();
        buildingDailyEnergyGraph.removeGraph();
        pvProjectDailyEnergyGraph.removeGraph();
        cspProjectDailyEnergyGraph.removeGraph();
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

    static void setAutoRecomputeEnergy(final boolean on) {
        autoRecomputeEnergy = on;
    }

    public void setComputeEnabled(final boolean computeEnabled) {
        this.computeEnabled = computeEnabled;
    }

    // As this method may be called from a non-Event-Queue thread, updating GUI must be done through invokeLater.
    public void updateProperties() {

        // update part properties

        final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
        final boolean energyViewShown = MainPanel.getInstance().getEnergyButton().isSelected();
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
        final double scale = Scene.getInstance().getScale() * meterToFoot;

        final TitledBorder partPanelBorder = (TitledBorder) partPanel.getBorder();
        if (selectedPart != null) {
            final ReadOnlyVector3 v = selectedPart.getAbsPoint(0);
            if (selectedPart instanceof Tree) {
                final Tree tree = (Tree) selectedPart;
                if (tree.isDrawable()) {
                    EventQueue.invokeLater(() -> {
                        partPanelBorder.setTitle("Tree (" + tree.getId() + "): " + tree.getPlantName());
                        partProperty1Label.setText("  Spread & Height:");
                        partProperty2Label.setText("  Type:");
                        partProperty3Label.setText("  Position:");
                        final double l = v.length();
                        double a = 90 + Math.toDegrees(Math.asin(-v.getY() / l));
                        if (v.getX() < 0) {
                            a = 360 - a;
                        }
                        if (Util.isZero(a - 360)) {
                            a = 0;
                        }
                        partProperty1TextField.setText(ONE_DECIMAL.format(tree.getWidth() * scale) + lengthUnit + ", " + ONE_DECIMAL.format(tree.getHeight() * scale) + lengthUnit);
                        partProperty2TextField.setText(tree.getPlantName() + " (" + (Tree.PLANTS[tree.getPlantType()].isEvergreen() ? "Evergreen" : "Deciduous") + ")");
                        partProperty3TextField.setText("("
                                + ONE_DECIMAL.format(v.getX() * scale) + ", "
                                + ONE_DECIMAL.format(v.getY() * scale) + ")" + lengthUnit + " or ("
                                + ONE_DECIMAL.format(l * scale) + lengthUnit + ", "
                                + ONE_DECIMAL.format(a) + "\u00B0)");
                        partProperty1TextField.putClientProperty("tooltip", "The spread and height of the tree");
                        partProperty2TextField.putClientProperty("tooltip", "The type of the tree");
                        partProperty3TextField.putClientProperty("tooltip", "The (x, y) or polar coordinates on the land");
                    });
                }
            } else if (selectedPart instanceof Human) {
                final Human human = (Human) selectedPart;
                if (human.isDrawable()) {
                    EventQueue.invokeLater(() -> {
                        partPanelBorder.setTitle("Human (" + human.getId() + "): " + human.getHumanName());
                        partProperty1Label.setText("  X:");
                        partProperty2Label.setText("  Y:");
                        partProperty3Label.setText("  Z:");
                        partProperty1TextField.setText(ONE_DECIMAL.format(v.getX() * scale) + lengthUnit);
                        partProperty2TextField.setText(ONE_DECIMAL.format(v.getY() * scale) + lengthUnit);
                        partProperty3TextField.setText(ONE_DECIMAL.format(v.getZ() * scale) + lengthUnit);
                        partProperty1TextField.putClientProperty("tooltip", "X coordinate");
                        partProperty2TextField.putClientProperty("tooltip", "Y coordinate");
                        partProperty3TextField.putClientProperty("tooltip", "Z coordinate");
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
                        EventQueue.invokeLater(() -> {
                            String title = "Solar Panel (" + sp.getId() + "), " + (sp.getModelName() == null ? "" : "Model: " + sp.getModelName());
                            final String trackerName = sp.getTrackerName();
                            if (trackerName != null) {
                                title += ", Tracker: " + trackerName;
                            }
                            partPanelBorder.setTitle(title);
                            partProperty1Label.setText("  Size & Position:");
                            partProperty1TextField.setText(TWO_DECIMALS.format(sp.getPanelWidth() * meterToFoot) + "\u00d7"
                                    + TWO_DECIMALS.format(sp.getPanelHeight() * meterToFoot) + lengthUnit + ", ("
                                    + ONE_DECIMAL.format(v.getX() * scale) + ", "
                                    + ONE_DECIMAL.format(v.getY() * scale) + ", "
                                    + ONE_DECIMAL.format(v.getZ() * scale) + ")" + lengthUnit + ", pole:"
                                    + TWO_DECIMALS.format(sp.getPoleHeight() * scale * meterToFoot) + lengthUnit);
                            partProperty1TextField.putClientProperty("tooltip", "The length, width, center coordinates, and pole height of the solar panel");
                            partProperty2Label.setText("  Angles:");
                            partProperty2TextField.setText(flat ? "tilt: " + ONE_DECIMAL.format(Util.isZero(sp.getTiltAngle()) ? Math.toDegrees(Math.asin(sp.getNormal().getY())) : sp.getTiltAngle())
                                    + "\u00B0, azimuth: " + ONE_DECIMAL.format(az) + "\u00B0" : " --- ");
                            partProperty2TextField.putClientProperty("tooltip", "The angles of the solar panel");
                            final String eff = ONE_DECIMAL.format(sp.getCellEfficiency() * 100) + "%";
                            if (energyViewShown) {
                                partProperty3Label.setText("  Efficiency & Yield:");
                                partProperty3TextField.setText(eff + ", " + TWO_DECIMALS.format(sp.getSolarPotentialToday()) + " kWh");
                                partProperty3TextField.putClientProperty("tooltip", "The solar cell efficiency and daily yield of the solar panel");
                            } else {
                                partProperty3Label.setText("  Efficiency:");
                                partProperty3TextField.setText(eff);
                                partProperty3TextField.putClientProperty("tooltip", "The solar cell efficiency of the solar panel");
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
                        EventQueue.invokeLater(() -> {
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
                            partProperty1Label.setText("  Size & Center:");
                            partProperty1TextField.setText(TWO_DECIMALS.format(rack.getRackWidth() * meterToFoot) + "\u00d7"
                                    + TWO_DECIMALS.format(rack.getRackHeight() * meterToFoot) + lengthUnit + ", ("
                                    + ONE_DECIMAL.format(v.getX() * scale) + ", "
                                    + ONE_DECIMAL.format(v.getY() * scale) + ", "
                                    + ONE_DECIMAL.format(v.getZ() * scale) + ")" + lengthUnit + ", pole:"
                                    + TWO_DECIMALS.format(rack.getPoleHeight() * scale * meterToFoot) + lengthUnit);
                            partProperty1TextField.putClientProperty("tooltip", "The length, width, center coordinates, and pole height of the rack");
                            partProperty2Label.setText("  Angles:");
                            partProperty2TextField.setText("tilt: " + ONE_DECIMAL.format(Util.isZero(rack.getTiltAngle()) ? Math.toDegrees(Math.asin(rack.getNormal().getY())) : rack.getTiltAngle())
                                    + "\u00B0, azimuth: " + ONE_DECIMAL.format(az) + "\u00B0");
                            partProperty2TextField.putClientProperty("tooltip", "The angles of the rack");
                            partProperty3Label.setText("  Solar Panels:");
                            final SolarPanel sp = rack.getSolarPanel();
                            final String eff = ONE_DECIMAL.format(sp.getCellEfficiency() * 100) + "%";
                            if (energyViewShown) {
                                partProperty3Label.setText("  Efficiency & Yield:");
                                partProperty3TextField.setText(eff + ", " + TWO_DECIMALS.format(rack.getSolarPotentialToday()) + " kWh");
                                partProperty3TextField.putClientProperty("tooltip", "The solar cell efficiency and daily yield of the solar panel array on the rack");
                            } else {
                                if (rack.isMonolithic()) {
                                    final int[] rnc = rack.getSolarPanelRowAndColumnNumbers();
                                    partProperty3TextField.setText("" + n + " (" + rnc[0] + "\u00D7" + rnc[1] + "), " + s.getPanelWidth() + "\u00D7" + s.getPanelHeight() + lengthUnit + ", " + eff);
                                } else {
                                    partProperty3TextField.setText("" + n);
                                }
                                partProperty3TextField.putClientProperty("tooltip", "Number and type of solar panels on this rack");
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
                        EventQueue.invokeLater(() -> {
                            partPanelBorder.setTitle("Heliostat (" + m.getId() + ")");
                            partProperty1Label.setText("  Size & Center:");
                            partProperty1TextField.setText(TWO_DECIMALS.format(m.getApertureWidth() * meterToFoot) + "\u00d7"
                                    + TWO_DECIMALS.format(m.getApertureHeight() * meterToFoot) + lengthUnit + ", ("
                                    + ONE_DECIMAL.format(v.getX() * scale) + ", "
                                    + ONE_DECIMAL.format(v.getY() * scale) + ", "
                                    + ONE_DECIMAL.format(v.getZ() * scale) + ")" + lengthUnit + ", pole:"
                                    + TWO_DECIMALS.format(m.getPoleHeight() * scale * meterToFoot) + lengthUnit);
                            partProperty1TextField.putClientProperty("tooltip", "The length, width, and center coordinates of the heliostat");
                            partProperty2Label.setText("  Angles:");
                            partProperty2TextField.setText(flat ? "tilt: " + ONE_DECIMAL.format(m.getTiltAngle()) + "\u00B0, azimuth: " + ONE_DECIMAL.format(az) + "\u00B0" : " --- ");
                            partProperty2TextField.putClientProperty("tooltip", "The angles of the heliostat");
                            final Foundation receiver = m.getReceiver();
                            final String s = "R=" + ONE_DECIMAL.format(m.getReflectance() * 100) + "%" + (receiver == null ? "" : ", \u03B7="
                                    + ONE_DECIMAL.format(receiver.getSolarReceiverEfficiency() * 100) + "%");
                            if (energyViewShown) {
                                partProperty3Label.setText("  Properties & Yield:");
                                partProperty3TextField.setText(s + ", " + ONE_DECIMAL.format(m.getSolarPotentialToday() * m.getSystemEfficiency()) + " kWh");
                                partProperty3TextField.putClientProperty("tooltip", "The physical properties and electric yield of this heliostat");
                            } else {
                                partProperty3Label.setText("  Properties:");
                                partProperty3TextField.setText(s);
                                partProperty3TextField.putClientProperty("tooltip", "The physical properties of this heliostat");
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
                        final double az = a;
                        // http://www.powerfromthesun.net/Book/chapter08/chapter08.html
                        final double focalLength = t.getSemilatusRectum() * 0.5;
                        final double d = t.getApertureWidth();
                        final double h = d * d / (16 * focalLength);
                        final double rimAngle = Math.toDegrees(Math.atan(1.0 / (d / (8 * h) - (2 * h) / d)));
                        final double b = 4 * h / d;
                        final double c = Math.sqrt(b * b + 1);
                        final double s = 0.5 * d * c + 2 * focalLength * Math.log(b + c);
                        EventQueue.invokeLater(() -> {
                            partPanelBorder.setTitle("Parabolic Trough (" + t.getId() + ")");
                            partProperty1Label.setText("  Length & Center:");
                            partProperty1TextField.setText(TWO_DECIMALS.format(t.getTroughLength() * meterToFoot) + lengthUnit + ", module:"
                                    + TWO_DECIMALS.format(t.getModuleLength() * meterToFoot) + lengthUnit + ", ("
                                    + ONE_DECIMAL.format(v.getX() * scale) + ", "
                                    + ONE_DECIMAL.format(v.getY() * scale) + ", "
                                    + ONE_DECIMAL.format(v.getZ() * scale) + ")" + lengthUnit + ", azimuth:"
                                    + ONE_DECIMAL.format(az) + "\u00B0");
                            partProperty1TextField.putClientProperty("tooltip", "Assembly length, module length, center coordinates, and azimith of the parabolic trough");
                            partProperty2Label.setText("  Parabola Shape:");
                            partProperty2TextField.setText("f=" + ONE_DECIMAL.format(focalLength * meterToFoot) + lengthUnit + ", d="
                                    + ONE_DECIMAL.format(t.getApertureWidth() * meterToFoot) + lengthUnit + ", h="
                                    + ONE_DECIMAL.format(h * meterToFoot) + lengthUnit + ", \u03C6="
                                    + ONE_DECIMAL.format(rimAngle >= 0 ? rimAngle : 180 + rimAngle) + "\u00B0");
                            partProperty2TextField.putClientProperty("tooltip", "Parameters of the parabolic shape");
                            final String str = "R=" + ONE_DECIMAL.format(t.getReflectance() * 100) + "%, s="
                                    + ONE_DECIMAL.format(s * t.getTroughLength() * meterToFoot * meterToFoot) + lengthUnit + "\u00B2, a="
                                    + ONE_DECIMAL.format(d * t.getTroughLength() * meterToFoot * meterToFoot) + lengthUnit + "\u00B2, \u03B1="
                                    + ONE_DECIMAL.format(t.getAbsorptance() * 100) + "%";
                            if (energyViewShown) {
                                partProperty3Label.setText("  Properties & Yield:");
                                partProperty3TextField.setText(str + ", " + ONE_DECIMAL.format(t.getSolarPotentialToday() * t.getSystemEfficiency()) + " kWh");
                                partProperty3TextField.putClientProperty("tooltip", "The properties and yield of this parabolic trough");
                            } else {
                                partProperty3Label.setText("  Properties:");
                                partProperty3TextField.setText(str);
                                partProperty3TextField.putClientProperty("tooltip", "The properties of this parabolic trough");
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
                        EventQueue.invokeLater(() -> {
                            partPanelBorder.setTitle("Parabolic Dish (" + d.getId() + ")");
                            partProperty1Label.setText("  Size & Center:");
                            partProperty1TextField.setText("Rim radius="
                                    + TWO_DECIMALS.format(rimRadius * meterToFoot) + lengthUnit + ", ("
                                    + ONE_DECIMAL.format(v.getX() * scale) + ", "
                                    + ONE_DECIMAL.format(v.getY() * scale) + ", "
                                    + ONE_DECIMAL.format(v.getZ() * scale) + ")" + lengthUnit + ", pole:"
                                    + TWO_DECIMALS.format(d.getPoleHeight() * scale * meterToFoot) + lengthUnit);
                            partProperty1TextField.putClientProperty("tooltip", "Rim radius and center coordinates of the parabolic dish");
                            partProperty2Label.setText("  Parabola Shape:");
                            partProperty2TextField.setText("Focal length=" + ONE_DECIMAL.format(focalLength * meterToFoot) + lengthUnit);
                            partProperty2TextField.putClientProperty("tooltip", "Parameters of the parabolic shape");
                            final String str = "R=" + ONE_DECIMAL.format(d.getReflectance() * 100) + "%, \u03B1=" + ONE_DECIMAL.format(d.getAbsorptance() * 100) + "%";
                            if (energyViewShown) {
                                partProperty3Label.setText("  Properties & Yield:");
                                partProperty3TextField.setText(str + ", " + ONE_DECIMAL.format(d.getSolarPotentialToday() * d.getSystemEfficiency()) + " kWh");
                                partProperty3TextField.putClientProperty("tooltip", "The properties and yield of this parabolic dish");
                            } else {
                                partProperty3Label.setText("  Properties:");
                                partProperty3TextField.setText(str);
                                partProperty3TextField.putClientProperty("tooltip", "The properties of this parabolic dish");
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
                        final double az = a;
                        EventQueue.invokeLater(() -> {
                            partPanelBorder.setTitle("Fresnel Reflector (" + r.getId() + ")");
                            partProperty1Label.setText("  Center & Azimuth:");
                            partProperty1TextField.setText("("
                                    + ONE_DECIMAL.format(v.getX() * scale) + ", "
                                    + ONE_DECIMAL.format(v.getY() * scale) + ", "
                                    + ONE_DECIMAL.format(v.getZ() * scale) + ")" + lengthUnit + ", azimuth:"
                                    + ONE_DECIMAL.format(az) + "\u00B0" + ", pole:"
                                    + TWO_DECIMALS.format(r.getPoleHeight() * scale * meterToFoot) + lengthUnit);
                            partProperty1TextField.putClientProperty("tooltip", "Center coordinates and azimuth of the Fresnel reflector");
                            partProperty2Label.setText("  Length & Width:");
                            partProperty2TextField.setText(TWO_DECIMALS.format(r.getLength() * meterToFoot) + lengthUnit + ", module:"
                                    + TWO_DECIMALS.format(r.getModuleLength() * meterToFoot) + lengthUnit + ", " + ONE_DECIMAL.format(r.getModuleWidth() * meterToFoot) + lengthUnit);
                            partProperty2TextField.putClientProperty("tooltip", "Assembly length, module length, and width of the Fresnel reflector");
                            final Foundation receiver = r.getReceiver();
                            final String str = "R="
                                    + ONE_DECIMAL.format(r.getReflectance() * 100) + "%, a="
                                    + ONE_DECIMAL.format(r.getModuleWidth() * r.getLength() * meterToFoot * meterToFoot) + lengthUnit + "\u00B2" + (receiver == null ? "" : ", \u03B7="
                                    + ONE_DECIMAL.format(receiver.getSolarReceiverEfficiency() * 100) + "%");
                            if (energyViewShown) {
                                partProperty3Label.setText("  Properties & Yield:");
                                partProperty3TextField.setText(str + ", " + ONE_DECIMAL.format(r.getSolarPotentialToday() * r.getSystemEfficiency()) + " kWh");
                                partProperty3TextField.putClientProperty("tooltip", "The properties and yield of this Fresnel reflector");
                            } else {
                                partProperty3Label.setText("  Properties:");
                                partProperty3TextField.setText(str);
                                partProperty3TextField.putClientProperty("tooltip", "The properties of this Fresnel reflector");
                            }
                        });
                    }
                }
            } else if (selectedPart instanceof Sensor) {
                final Sensor sensor = (Sensor) selectedPart;
                if (sensor.isDrawable()) {
                    EventQueue.invokeLater(() -> {
                        partPanelBorder.setTitle("Sensor (" + sensor.getId() + ")");
                        partProperty1Label.setText("  Position:");
                        partProperty2Label.setText("  Thermal:");
                        partProperty3Label.setText("  Light:");
                        partProperty1TextField.setText("("
                                + ONE_DECIMAL.format(v.getX() * scale) + ", "
                                + ONE_DECIMAL.format(v.getY() * scale) + ", "
                                + ONE_DECIMAL.format(v.getZ() * scale) + ")" + lengthUnit);
                        partProperty2TextField.setText(TWO_DECIMALS.format(-sensor.getTotalHeatLoss() / sensor.getArea()) + " kWh/day/m\u00B2");
                        partProperty3TextField.setText(TWO_DECIMALS.format(sensor.getSolarPotentialToday() / sensor.getArea()) + " kWh/day/m\u00B2");
                        partProperty1TextField.putClientProperty("tooltip", "The (x, y, z) coordinates of the sensor");
                        partProperty2TextField.putClientProperty("tooltip", "The heat flux measured by the sensor");
                        partProperty3TextField.putClientProperty("tooltip", "The light intensity measured by the sensor");
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
                    EventQueue.invokeLater(() -> {
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
                            final String meshCenterString = "("
                                    + ONE_DECIMAL.format(meshBoxCenter.getX() * scale) + ", "
                                    + ONE_DECIMAL.format(meshBoxCenter.getY() * scale) + ", "
                                    + ONE_DECIMAL.format(meshBoxCenter.getZ() * scale) + ")" + lengthUnit;
                            final String meshNormalString = meshNormal != null ? "(" + TWO_DECIMALS.format(meshNormal.getX()) + ", " + TWO_DECIMALS.format(meshNormal.getY()) + ", "
                                    + TWO_DECIMALS.format(meshNormal.getZ()) + ")" : "";
                            partPanelBorder.setTitle("Node #" + foundation.getImportedNodes().indexOf(selectedNode) + " ("
                                    + Util.getFileName(ns.getSourceURL().getPath()).replace("%20", " ") + "), Mesh #" + meshIndex + ", Base #" + foundation.getId());
                            partProperty1Label.setText("  Node:");
                            partProperty2Label.setText("  Mesh:");
                            partProperty1TextField.setText(TWO_DECIMALS.format(xNodeBox) + "\u00d7"
                                    + TWO_DECIMALS.format(yNodeBox) + "\u00d7"
                                    + TWO_DECIMALS.format(zNodeBox) + lengthUnit + ", ("
                                    + TWO_DECIMALS.format(position.getX() * scale) + ", "
                                    + TWO_DECIMALS.format(position.getY() * scale) + ")" + lengthUnit);
                            partProperty2TextField.setText(meshBoxString + ", " + meshCenterString);
                            partProperty1TextField.putClientProperty("tooltip", "Dimension and location of the bounding box of the selected node<br>File:" + ns.getSourceURL().getFile());
                            partProperty2TextField.putClientProperty("tooltip", "Dimension and location of the bounding box of the selected mesh");
                            if (energyViewShown) {
                                double dailyMeshSolarPotential = 0;
                                final double[] meshSolarPotential = SolarRadiation.getInstance().getSolarPotential(selectedMesh);
                                for (final double x : meshSolarPotential) {
                                    dailyMeshSolarPotential += x;
                                }
                                partProperty3Label.setText("  Solar:");
                                partProperty3TextField.setText("\u2191" + meshNormalString + ", " + TWO_DECIMALS.format(dailyMeshSolarPotential) + " kWh");
                                partProperty3TextField.putClientProperty("tooltip", "Normal vector and solar potential of the selected mesh");
                            } else {
                                partProperty3Label.setText("  Normal:");
                                partProperty3TextField.setText("\u2191" + meshNormalString + ", " + selectedMesh.getMeshData().getVertexCount() + " vertices");
                                partProperty3TextField.putClientProperty("tooltip", "Normal vector and vertex count of the selected mesh");
                            }
                        } else {
                            int solarPanelCount = 0;
                            if (Scene.getInstance().getProjectType() == Foundation.TYPE_PV_PROJECT) {
                                solarPanelCount = foundation.countSolarPanels();
                            }
                            partPanelBorder.setTitle("Foundation (id = " + foundation.getId() + (solarPanelCount > 0 ? ", " + solarPanelCount + " solar panels" : "") + ")");
                            partProperty1Label.setText("  Size:");
                            partProperty1TextField.setText(TWO_DECIMALS.format(lx * scale) + "\u00d7"
                                    + TWO_DECIMALS.format(ly * scale) + "\u00d7"
                                    + TWO_DECIMALS.format(lz * scale) + lengthUnit + ", Area\u2248"
                                    + ONE_DECIMAL.format(lx * ly * scale * scale) + landArea + lengthUnit + "\u00B2");
                            partProperty1TextField.putClientProperty("tooltip", "The length and width of the foundation");
                            partProperty2Label.setText("  Position:");
                            partProperty2TextField.setText("(" + TWO_DECIMALS.format(cx * scale) + ", " + TWO_DECIMALS.format(cy * scale) + ")" + lengthUnit);
                            partProperty2TextField.putClientProperty("tooltip", "The (x, y) coordinate of the center of the foundation");
                            partProperty3Label.setText("  Azimuth:");
                            partProperty3TextField.setText(TWO_DECIMALS.format(az) + "\u00B0");
                            partProperty3TextField.putClientProperty("tooltip", "The azimuth of the reference edge");
                        }
                    });
                }
            } else if (selectedPart instanceof Roof) {
                final Roof roof = (Roof) selectedPart;
                if (roof.isDrawable()) {
                    final double area = roof.getArea();
                    EventQueue.invokeLater(() -> {
                        partPanelBorder.setTitle("Roof (" + roof.getId() + ")");
                        partProperty1Label.setText("  Area & Rise:");
                        partProperty1TextField.setText("Area = " + TWO_DECIMALS.format(area) + lengthUnit + "\u00B2, Rise = " + TWO_DECIMALS.format(roof.getHeight() * scale) + lengthUnit);
                        partProperty1TextField.putClientProperty("tooltip", "The total area and the rise of the roof<br>(The rise is the highest point of the roof to the top of the walls.)");
                        partProperty2Label.setText("  Thermal:");
                        partProperty3Label.setText("  Solar:");
                        final String rval = ONE_DECIMAL.format(Util.toUsRValue(roof.getUValue()));
                        final boolean isBuildingProject = Scene.getInstance().getProjectType() == Foundation.TYPE_BUILDING;
                        final float absorptance = 1 - roof.getAlbedo();
                        if (energyViewShown) {
                            partProperty2TextField.setText("R-value = " + rval + ", Gain = " + TWO_DECIMALS.format(-roof.getTotalHeatLoss()) + " kWh");
                            partProperty2TextField.putClientProperty("tooltip", "The R-value and daily thermal gain of the roof");
                            if (isBuildingProject) {
                                partProperty3TextField.setText("Absorptance = " + TWO_DECIMALS.format(absorptance) + ", Absorption = "
                                        + TWO_DECIMALS.format(roof.getSolarPotentialToday() * absorptance) + " kWh");
                                partProperty3TextField.putClientProperty("tooltip", "The absorptance and daily solar heat gain of the roof surface");
                            } else {
                                partProperty3TextField.setText("Radiation energy = " + TWO_DECIMALS.format(roof.getSolarPotentialToday()) + " kWh");
                                partProperty3TextField.putClientProperty("tooltip", "The solar radiation energy onto this roof surface");
                            }
                        } else {
                            partProperty2TextField.setText("R-value = " + rval + " (US system)");
                            partProperty2TextField.putClientProperty("tooltip", "The R-value of the roof");
                            partProperty3TextField.setText("Absorptance = " + TWO_DECIMALS.format(absorptance));
                            partProperty3TextField.putClientProperty("tooltip", "The absorptance of the roof surface");
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
                    EventQueue.invokeLater(() -> {
                        partPanelBorder.setTitle("Window (" + window.getId() + ")");
                        partProperty1Label.setText("  Size & Center:");
                        partProperty1TextField.setText(TWO_DECIMALS.format(lx) + "\u00d7"
                                + TWO_DECIMALS.format(ly) + lengthUnit + " \u2248 "
                                + TWO_DECIMALS.format(lx * ly) + lengthUnit + "\u00B2, ("
                                + TWO_DECIMALS.format(cx * scale) + ", "
                                + TWO_DECIMALS.format(cy * scale) + ", "
                                + TWO_DECIMALS.format(cz * scale) + ")" + lengthUnit);
                        partProperty1TextField.putClientProperty("tooltip", "The width, height, and center of the window");
                        partProperty2Label.setText("  Thermal:");
                        partProperty3Label.setText("  Solar:");
                        final String shgc = TWO_DECIMALS.format(window.getSolarHeatGainCoefficient());
                        if (energyViewShown) {
                            partProperty2TextField.setText("U-Value = " + TWO_DECIMALS.format(Util.toUsUValue(window.getUValue())) + ", Gain = " + TWO_DECIMALS.format(-window.getTotalHeatLoss()) + " kWh");
                            partProperty2TextField.putClientProperty("tooltip", "The U-value and daily thermal gain of the window");
                            partProperty3TextField.setText("SHGC = " + shgc + ", Gain = " + TWO_DECIMALS.format(window.getSolarPotentialToday() * window.getSolarHeatGainCoefficient()) + " kWh");
                            partProperty3TextField.putClientProperty("tooltip", "The SHGC value and daily solar gain of the window");
                        } else {
                            partProperty2TextField.setText("U-Value = " + TWO_DECIMALS.format(Util.toUsUValue(window.getUValue())) + " (US system)");
                            partProperty2TextField.putClientProperty("tooltip", "The U-value of the window");
                            partProperty3TextField.setText("SHGC = " + shgc);
                            partProperty3TextField.putClientProperty("tooltip", "The solar heat gain coefficient (SHGC) of the window");
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
                    final double cz = 0.25 * (v.getZ() + v1.getZ() + v2.getZ() + v3.getZ());
                    final double lx = v.distance(v2);
                    final double ly = v.distance(v1);
                    EventQueue.invokeLater(() -> {
                        partPanelBorder.setTitle("Wall (" + wall.getId() + ")");
                        partProperty1Label.setText("  Size & Center:");
                        partProperty1TextField.setText(TWO_DECIMALS.format(lx * scale) + "\u00d7"
                                + TWO_DECIMALS.format(ly * scale) + lengthUnit + " \u2248 "
                                + TWO_DECIMALS.format(lx * ly * scale * scale) + lengthUnit + "\u00B2 (eff. "
                                + TWO_DECIMALS.format(wall.getArea()) + "), ("
                                + TWO_DECIMALS.format(cx * scale) + ", "
                                + TWO_DECIMALS.format(cy * scale) + ", "
                                + TWO_DECIMALS.format(cz * scale) + ")" + lengthUnit);
                        partProperty1TextField.putClientProperty("tooltip", "The width, height, and center of the wall");
                        partProperty2Label.setText("  Thermal:");
                        partProperty3Label.setText("  Solar:");
                        final String rval = ONE_DECIMAL.format(Util.toUsRValue(wall.getUValue()));
                        final float absorptance = 1 - wall.getAlbedo();
                        if (energyViewShown) {
                            partProperty2TextField.setText("R-Value = " + rval + ", Gain = " + TWO_DECIMALS.format(-wall.getTotalHeatLoss()) + " kWh");
                            partProperty2TextField.putClientProperty("tooltip", "The R-value and daily thermal gain of the wall");
                            partProperty3TextField.setText("Absorptance = " + TWO_DECIMALS.format(absorptance) + ", Absorption = "
                                    + TWO_DECIMALS.format(wall.getSolarPotentialToday() * absorptance) + " kWh");
                            partProperty3TextField.putClientProperty("tooltip", "The absorptance and daily solar heat gain of the wall surface");
                        } else {
                            partProperty2TextField.setText("R-Value = " + rval + " (US system)");
                            partProperty2TextField.putClientProperty("tooltip", "The R-value of the wall");
                            partProperty3TextField.setText("Absorptance = " + TWO_DECIMALS.format(absorptance));
                            partProperty3TextField.putClientProperty("tooltip", "The absorptance of the wall surface");
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
                    final double cz = 0.25 * (v.getZ() + v1.getZ() + v2.getZ() + v3.getZ());
                    final double lx = v.distance(v2);
                    final double ly = v.distance(v1);
                    EventQueue.invokeLater(() -> {
                        partPanelBorder.setTitle("Door (" + door.getId() + ")");
                        partProperty1Label.setText("  Size & Center:");
                        partProperty1TextField.setText(TWO_DECIMALS.format(lx * scale) + "\u00d7"
                                + TWO_DECIMALS.format(ly * scale) + lengthUnit + " \u2248 "
                                + TWO_DECIMALS.format(lx * ly * scale * scale) + lengthUnit + "\u00B2, ("
                                + TWO_DECIMALS.format(cx * scale) + ", "
                                + TWO_DECIMALS.format(cy * scale) + ", "
                                + TWO_DECIMALS.format(cz * scale) + ")" + lengthUnit);
                        partProperty1TextField.putClientProperty("tooltip", "The width, height, and center of the door");
                        partProperty2Label.setText("  Thermal:");
                        partProperty3Label.setText("  Solar:");
                        final String uval = TWO_DECIMALS.format(Util.toUsUValue(door.getUValue()));
                        final float absorptance = 1 - door.getAlbedo();
                        if (energyViewShown) {
                            partProperty2TextField.setText("U-Value = " + uval + ", Gain = " + TWO_DECIMALS.format(-door.getTotalHeatLoss()) + " kWh");
                            partProperty2TextField.putClientProperty("tooltip", "The R-value and daily thermal gain of the door");
                            partProperty3TextField.setText("Absorptance = " + TWO_DECIMALS.format(absorptance) + ", Absorption = "
                                    + TWO_DECIMALS.format(door.getSolarPotentialToday() * absorptance) + " kWh");
                            partProperty3TextField.putClientProperty("tooltip", "The absorptance and daily solar heat gain of the door surface");
                        } else {
                            partProperty2TextField.setText("U-Value = " + uval + " (US system)");
                            partProperty2TextField.putClientProperty("tooltip", "The U-value of the door");
                            partProperty3TextField.setText("Absorptance = " + TWO_DECIMALS.format(absorptance));
                            partProperty3TextField.putClientProperty("tooltip", "The absorptance of the door surface");
                        }
                    });
                }
            } else if (selectedPart instanceof Floor) {
                final Floor floor = (Floor) selectedPart;
                if (floor.isDrawable()) {
                    final double cx, cy;
                    final double cz = v.getZ();
                    if (floor.getPoints().size() > 1) {
                        final Vector3 v1 = floor.getAbsPoint(1);
                        final Vector3 v2 = floor.getAbsPoint(2);
                        final Vector3 v3 = floor.getAbsPoint(3);
                        cx = 0.25 * (v.getX() + v1.getX() + v2.getX() + v3.getX());
                        cy = 0.25 * (v.getY() + v1.getY() + v2.getY() + v3.getY());
                    } else {
                        cx = v.getX();
                        cy = v.getY();
                    }
                    EventQueue.invokeLater(() -> {
                        partPanelBorder.setTitle("Floor (" + floor.getId() + ")");
                        partProperty1Label.setText("  Area & Center");
                        partProperty1TextField.setText(ONE_DECIMAL.format(floor.getArea()) + lengthUnit + "\u00B2, ("
                                + ONE_DECIMAL.format(cx * scale) + ", " + ONE_DECIMAL.format(cy * scale) + ", " + ONE_DECIMAL.format(cz * scale) + ")" + lengthUnit);
                        partProperty1TextField.putClientProperty("tooltip", "The area and center of the floor");
                        partProperty2Label.setText("  Thermal:");
                        partProperty2TextField.setText("N.A.");
                        partProperty2TextField.putClientProperty("tooltip", "Not applicable to thermal analysis");
                        partProperty3Label.setText("  Solar:");
                        partProperty3TextField.setText("N.A.");
                        partProperty3TextField.putClientProperty("tooltip", "Not applicable to solar analysis");
                    });
                }
            }
        } else {
            EventQueue.invokeLater(() -> {
                final int numberOfSolarPanels = Scene.getInstance().countSolarPanels();
                if (numberOfSolarPanels > 0) {
                    partPanelBorder.setTitle("Solar Panels");
                    partProperty1Label.setText("  Total Number:");
                    partProperty1TextField.setText("" + numberOfSolarPanels);
                    partProperty1TextField.putClientProperty("tooltip", "Total number of solar panels");
                    partProperty2Label.setText("  Total Cost:");
                    partProperty2TextField.setText("$" + TWO_DECIMALS.format(PvProjectCost.getInstance().getTotalCost()));
                    partProperty2TextField.putClientProperty("tooltip", "Total project cost");
                    partProperty3Label.setText("  -");
                    partProperty3TextField.setText("");
                    partProperty3TextField.putClientProperty("tooltip", null);
                } else {
                    final int numberOfHeliostats = Scene.getInstance().countParts(Mirror.class);
                    if (numberOfHeliostats > 0) {
                        partPanelBorder.setTitle("Heliostats");
                        partProperty1Label.setText("  Total Number:");
                        partProperty1TextField.setText("" + numberOfHeliostats);
                        partProperty1TextField.putClientProperty("tooltip", "Total number of heliostats");
                        partProperty2Label.setText("  Total Cost:");
                        partProperty2TextField.setText("$" + TWO_DECIMALS.format(CspProjectCost.getInstance().getTotalCost()));
                        partProperty2TextField.putClientProperty("tooltip", "Total project cost");
                        partProperty3Label.setText("  -");
                        partProperty3TextField.setText("");
                        partProperty3TextField.putClientProperty("tooltip", null);
                    } else {
                        final int numberOfParabolicTroughs = Scene.getInstance().countParts(ParabolicTrough.class);
                        if (numberOfParabolicTroughs > 0) {
                            partPanelBorder.setTitle("Parabolic Troughs");
                            partProperty1Label.setText("  Total Number:");
                            partProperty1TextField.setText("" + numberOfParabolicTroughs);
                            partProperty1TextField.putClientProperty("tooltip", "Total number of parabolic troughs");
                            partProperty2Label.setText("  Total Cost:");
                            partProperty2TextField.setText("$" + TWO_DECIMALS.format(CspProjectCost.getInstance().getTotalCost()));
                            partProperty2TextField.putClientProperty("tooltip", "Total project cost");
                            partProperty3Label.setText("  -");
                            partProperty3TextField.setText("");
                            partProperty3TextField.putClientProperty("tooltip", null);
                        } else {
                            final int numberOfParabolicDishes = Scene.getInstance().countParts(ParabolicDish.class);
                            if (numberOfParabolicDishes > 0) {
                                partPanelBorder.setTitle("Parabolic Dishes");
                                partProperty1Label.setText("  Total Number:");
                                partProperty1TextField.setText("" + numberOfParabolicDishes);
                                partProperty1TextField.putClientProperty("tooltip", "Total number of parabolic dishes");
                                partProperty2Label.setText("  Total Cost:");
                                partProperty2TextField.setText("$" + TWO_DECIMALS.format(CspProjectCost.getInstance().getTotalCost()));
                                partProperty2TextField.putClientProperty("tooltip", "Total project cost");
                                partProperty3Label.setText("  -");
                                partProperty3TextField.setText("");
                                partProperty3TextField.putClientProperty("tooltip", null);
                            } else {
                                final int numberOfFresnelReflectors = Scene.getInstance().countParts(FresnelReflector.class);
                                if (numberOfFresnelReflectors > 0) {
                                    partPanelBorder.setTitle("Fresnel Reflectors");
                                    partProperty1Label.setText("  Total Number:");
                                    partProperty1TextField.setText("" + numberOfFresnelReflectors);
                                    partProperty1TextField.putClientProperty("tooltip", "Total number of Fresnel reflectors");
                                    partProperty2Label.setText("  Total Cost:");
                                    partProperty2TextField.setText("$" + TWO_DECIMALS.format(CspProjectCost.getInstance().getTotalCost()));
                                    partProperty2TextField.putClientProperty("tooltip", "Total project cost");
                                    partProperty3Label.setText("  -");
                                    partProperty3TextField.setText("");
                                    partProperty3TextField.putClientProperty("tooltip", null);
                                } else {
                                    final int numberOfNodes = Scene.getInstance().countNodes();
                                    if (numberOfNodes > 0) {
                                        partPanelBorder.setTitle("Structures");
                                        partProperty1Label.setText("  Total Nodes:");
                                        partProperty1TextField.setText("" + numberOfNodes);
                                        partProperty1TextField.putClientProperty("tooltip", "Total number of structure nodes");
                                        partProperty2Label.setText("  Total Meshes:");
                                        partProperty2TextField.setText("" + Scene.getInstance().countMeshes());
                                        partProperty2TextField.putClientProperty("tooltip", "Total number of structure meshes");
                                        partProperty3Label.setText("  -");
                                        partProperty3TextField.setText("");
                                        partProperty3TextField.putClientProperty("tooltip", null);
                                    } else {
                                        partPanelBorder.setTitle(" -");
                                        partProperty1Label.setText("  -");
                                        partProperty1TextField.setText("");
                                        partProperty1TextField.putClientProperty("tooltip", null);
                                        partProperty2Label.setText("  -");
                                        partProperty2TextField.setText("");
                                        partProperty2TextField.putClientProperty("tooltip", null);
                                        partProperty3Label.setText("  -");
                                        partProperty3TextField.setText("");
                                        partProperty3TextField.putClientProperty("tooltip", null);
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
        EventQueue.invokeLater(() -> {
            if (selectedFoundation != null) {
                switch (selectedFoundation.getProjectType()) {
                    case Foundation.TYPE_BUILDING:
                        dataPanel.remove(instructionPanel);
                        dataPanel.remove(pvProjectPanel);
                        dataPanel.remove(cspProjectPanel);
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
                    case Foundation.TYPE_PV_PROJECT:
                        dataPanel.remove(instructionPanel);
                        dataPanel.remove(buildingPanel);
                        dataPanel.remove(cspProjectPanel);
                        dataPanel.add(pvProjectPanel, 2);
                        pvProjectZoneInfoPanel.update(selectedFoundation);
                        pvProjectPanel.setBorder(createTitledBorder("Photovoltaic Solar Power System (Zone #" + selectedFoundation.getId() + ")", true));
                        break;
                    case Foundation.TYPE_CSP_PROJECT:
                        dataPanel.remove(instructionPanel);
                        dataPanel.remove(buildingPanel);
                        dataPanel.remove(pvProjectPanel);
                        dataPanel.add(cspProjectPanel, 2);
                        cspProjectInfoPanel.update(selectedFoundation);
                        break;
                    case -1:
                        dataPanel.remove(instructionPanel);
                        dataPanel.remove(buildingPanel);
                        dataPanel.remove(pvProjectPanel);
                        dataPanel.remove(cspProjectPanel);
                        break;
                }
            } else {
                dataPanel.remove(buildingPanel);
                dataPanel.remove(pvProjectPanel);
                dataPanel.remove(cspProjectPanel);
                dataPanel.add(instructionPanel, 2);
                for (int i = 0; i < instructionSheets.length; i++) {
                    final String contentType = Scene.getInstance().getInstructionSheetTextType(i);
                    instructionSheets[i].setContentType(contentType == null ? "text/plain" : contentType);
                    if (!instructionSheets[i].getText().equals(Scene.getInstance().getInstructionSheetText(i))) {
                        instructionSheets[i].setText(Scene.getInstance().getInstructionSheetText(i));
                    }
                }
            }
            dataPanel.validate();
            dataPanel.repaint();
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
        EventQueue.invokeLater(() -> {
            final String s = Scene.getInstance().getProjectName();
            String title = "";
            switch (Scene.getInstance().getProjectType()) {
                case Foundation.TYPE_BUILDING:
                    title = "Building";
                    break;
                case Foundation.TYPE_PV_PROJECT:
                    title = "PV";
                    break;
                case Foundation.TYPE_CSP_PROJECT:
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
            dateSpinner.setEnabled(!Scene.getInstance().isDateFixed());
            regionComboBox.setEnabled(!Scene.getInstance().isLocationFixed());
            latitudeSpinner.setEnabled(!Scene.getInstance().isLocationFixed());
            buildingInfoPanel.updateAreaBounds();
            buildingInfoPanel.updateHeightBounds();
            buildingInfoPanel.updateWindowToFloorRatioBounds();
            buildingInfoPanel.updateSolarPanelNumberBounds();
            buildingInfoPanel.updateWindowNumberBounds();
            buildingInfoPanel.updateWallNumberBounds();
            pvProjectZoneInfoPanel.updateSolarPanelNumberMaximum();
            pvProjectZoneInfoPanel.updateBudgetMaximum();
            cspProjectInfoPanel.updateHeliostatNumberMaximum();
            cspProjectInfoPanel.updateParabolicTroughNumberMaximum();
            cspProjectInfoPanel.updateBudgetMaximum();
            SceneManager.getTaskManager().update(() -> {
                updateProperties();
                return null;
            });
        });
    }

    public boolean isCancelled() {
        return cancel || computeRequest;
    }

    public void cancel() {
        cancel = true;
        EventQueue.invokeLater(() -> MainPanel.getInstance().getEnergyButton().setSelected(false));
    }

    public JComboBox<String> getCityComboBox() {
        return regionComboBox;
    }

    void showHeatMapContrastSlider(final boolean b) {
        if (b) {
            dataPanel.add(heatMapPanel);
        } else {
            dataPanel.remove(heatMapPanel);
        }
        dataPanel.repaint();
    }

    private void turnOffCompute() {
        EventQueue.invokeLater(() -> {
            if (SceneManager.getInstance().getSolarHeatMap()) {
                MainPanel.getInstance().getEnergyButton().setSelected(false);
            }
            SceneManager.getInstance().getSolarLand().setVisible(false);
            Scene.getInstance().redrawAll();
        });
    }

    public void updateGraphs() {
        EventQueue.invokeLater(() -> {
            final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
            if (selectedPart instanceof Foundation) {
                final Foundation f = (Foundation) selectedPart;
                switch (f.getProjectType()) {
                    case Foundation.TYPE_BUILDING:
                        buildingCostGraph.addGraph(f);
                        buildingDailyEnergyGraph.addGraph(f);
                        break;
                    case Foundation.TYPE_PV_PROJECT:
                        pvProjectZoneCostGraph.addGraph(f);
                        pvProjectDailyEnergyGraph.addGraph(f);
                        break;
                    case Foundation.TYPE_CSP_PROJECT:
                        cspProjectCostGraph.addGraph(f);
                        cspProjectDailyEnergyGraph.addGraph(f);
                        break;
                }
            } else {
                buildingCostGraph.removeGraph();
                pvProjectZoneCostGraph.removeGraph();
                cspProjectCostGraph.removeGraph();
                buildingDailyEnergyGraph.removeGraph();
                pvProjectDailyEnergyGraph.removeGraph();
                cspProjectDailyEnergyGraph.removeGraph();
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
        text.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(final MouseEvent e) {
                final Object tooltip = text.getClientProperty("tooltip");
                text.setToolTipText(tooltip == null ? null : "<html>" + tooltip + "<hr>" + text.getText() + "</html>");
            }
        });
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

    private static ImageTitledBorder createImageTitledBorder(final String title, final Image image, final boolean smaller) {
        final ImageTitledBorder b = new ImageTitledBorder(title, image);
        b.setTitleFont(new Font(b.getTitleFont().getFontName(), Font.PLAIN, b.getTitleFont().getSize() - (smaller ? 2 : 1)));
        return b;
    }

    public void disableDateSpinner(final boolean b) {
        disableDateSpinner = b;
    }

    public void setComputingStartMillis(final long t) {
        computingStartMillis = t;
    }

    boolean checkCity() {
        final String city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
        if ("".equals(city)) {
            JOptionPane.showMessageDialog(MainFrame.getInstance(), "Can't perform this task without specifying a city.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void setReadInstructionState(final boolean on) {
        Image image = null;
        try {
            image = ImageIO.read(getClass().getResource(on ? "icons/speaker_on.png" : "icons/speaker_off.png"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        instructionPanel.setBorder(createImageTitledBorder("Instruction & Documentation", image, true));
    }

    public void resetReadInstruction() {
        setReadInstructionState(false);
        canReadInstruction = true;
        instructionPanel.setToolTipText("Click here to listen");
    }

}