package org.concord.energy3d.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

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
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.shapes.Heliodon;

import com.ardor3d.math.type.ReadOnlyVector3;

public class EnergyPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final double[] averageTemperature = new double[] { 28.8, 29.4, 37.1, 47.2, 57.9, 67.2, 72.7, 71, 64.1, 54.0, 43.7, 32.8 };
	private static final int[] daysInMonth = new int[] { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	private static final int[] heatingDegreeDays = new int[] { 654, 548, 471, 292, 166, 67, 11, 14, 74, 232, 371, 550 };
	private static final int[] coolingDegreeDays = new int[] { 0, 0, 1, 7, 17, 52, 124, 95, 34, 4, 0, 0 };
	private static final double COST_PER_KWH = 0.13;
	private static final EnergyPanel instance = new EnergyPanel();
	private final DecimalFormat twoDecimals = new DecimalFormat("###,###.##");
	private final DecimalFormat noDecimals = new DecimalFormat("###,###");
	private final DecimalFormat moneyDecimals = new DecimalFormat("$###,###");
	private final EnergyAmount solar = new EnergyAmount();
	private final EnergyAmount heating = new EnergyAmount();
	private final EnergyAmount cooling = new EnergyAmount();
	private final double[] solarEnergyArray = new double[12];
	private double wallsArea;
	private double doorsArea;
	private double windowsArea;
	private double roofsArea;
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
	private final JTextField solarCostTextField;
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
	private Thread thread;
	private boolean computeRequest;

	public class EnergyAmount {
		double rate;
		double today;
		double yearly;
		double cost;
	}

	public static EnergyPanel getInstance() {
		return instance;
	}

	private EnergyPanel() {
		setMinimumSize(new Dimension(250, 0));
		setPreferredSize(new Dimension(250, 388));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		final JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Temperature Today (C)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel);
		final GridBagLayout gbl_panel = new GridBagLayout();
		panel.setLayout(gbl_panel);

		autoCheckBox = new JCheckBox("Auto");
		autoCheckBox.setSelected(true);
		autoCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final boolean selected = autoCheckBox.isSelected();
				insideTemperatureSpinner.setEnabled(!selected);
				outsideTemperatureSpinner.setEnabled(!selected);
				if (selected) {
					insideTemperatureSpinner.setValue(20);
					updateOutsideTemperature();
				}
				computeEnergy();
			}
		});
		final GridBagConstraints gbc_autoCheckBox = new GridBagConstraints();
		gbc_autoCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_autoCheckBox.gridx = 0;
		gbc_autoCheckBox.gridy = 0;
		panel.add(autoCheckBox, gbc_autoCheckBox);

		final JLabel insideTemperatureLabel = new JLabel("Inside:");
		final GridBagConstraints gbc_insideTemperatureLabel = new GridBagConstraints();
		gbc_insideTemperatureLabel.insets = new Insets(0, 0, 0, 5);
		gbc_insideTemperatureLabel.gridx = 1;
		gbc_insideTemperatureLabel.gridy = 0;
		panel.add(insideTemperatureLabel, gbc_insideTemperatureLabel);

		insideTemperatureSpinner = new JSpinner();
		insideTemperatureSpinner.setEnabled(false);
		insideTemperatureSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				computeEnergy();
			}
		});
		insideTemperatureSpinner.setModel(new SpinnerNumberModel(20, -70, 60, 1));
		final GridBagConstraints gbc_insideTemperatureSpinner = new GridBagConstraints();
		gbc_insideTemperatureSpinner.insets = new Insets(0, 0, 0, 5);
		gbc_insideTemperatureSpinner.gridx = 2;
		gbc_insideTemperatureSpinner.gridy = 0;
		panel.add(insideTemperatureSpinner, gbc_insideTemperatureSpinner);

		final JLabel outsideTemperatureLabel = new JLabel("Outside:");
		final GridBagConstraints gbc_outsideTemperatureLabel = new GridBagConstraints();
		gbc_outsideTemperatureLabel.insets = new Insets(0, 0, 0, 5);
		gbc_outsideTemperatureLabel.gridx = 3;
		gbc_outsideTemperatureLabel.gridy = 0;
		panel.add(outsideTemperatureLabel, gbc_outsideTemperatureLabel);

		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));

		outsideTemperatureSpinner = new JSpinner();
		outsideTemperatureSpinner.setEnabled(false);
		outsideTemperatureSpinner.setModel(new SpinnerNumberModel(10, -70, 60, 1));
		outsideTemperatureSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				computeEnergy();
			}
		});
		final GridBagConstraints gbc_outsideTemperatureSpinner = new GridBagConstraints();
		gbc_outsideTemperatureSpinner.gridx = 4;
		gbc_outsideTemperatureSpinner.gridy = 0;
		panel.add(outsideTemperatureSpinner, gbc_outsideTemperatureSpinner);

		final JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Energy", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel_1);
		final GridBagLayout gbl_panel_1 = new GridBagLayout();
		panel_1.setLayout(gbl_panel_1);

		final JLabel solarLabel = new JLabel("Solar");
		solarLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_solarLabel = new GridBagConstraints();
		gbc_solarLabel.insets = new Insets(0, 0, 5, 5);
		gbc_solarLabel.gridx = 1;
		gbc_solarLabel.gridy = 0;
		panel_1.add(solarLabel, gbc_solarLabel);

		final JLabel heatingLabel = new JLabel("Heating");
		heatingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_heatingLabel = new GridBagConstraints();
		gbc_heatingLabel.insets = new Insets(0, 0, 5, 5);
		gbc_heatingLabel.gridx = 2;
		gbc_heatingLabel.gridy = 0;
		panel_1.add(heatingLabel, gbc_heatingLabel);

		final JLabel coolingLabel = new JLabel("Cooling");
		coolingLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_coolingLabel = new GridBagConstraints();
		gbc_coolingLabel.insets = new Insets(0, 0, 5, 5);
		gbc_coolingLabel.gridx = 3;
		gbc_coolingLabel.gridy = 0;
		panel_1.add(coolingLabel, gbc_coolingLabel);

		final JLabel totalLabel = new JLabel("Total");
		totalLabel.setHorizontalAlignment(SwingConstants.CENTER);
		final GridBagConstraints gbc_totalLabel = new GridBagConstraints();
		gbc_totalLabel.insets = new Insets(0, 0, 5, 0);
		gbc_totalLabel.gridx = 4;
		gbc_totalLabel.gridy = 0;
		panel_1.add(totalLabel, gbc_totalLabel);

		final JLabel nowLabel = new JLabel("Now (watts):");
		final GridBagConstraints gbc_nowLabel = new GridBagConstraints();
		gbc_nowLabel.anchor = GridBagConstraints.WEST;
		gbc_nowLabel.insets = new Insets(0, 0, 5, 5);
		gbc_nowLabel.gridx = 0;
		gbc_nowLabel.gridy = 1;
		panel_1.add(nowLabel, gbc_nowLabel);

		solarRateTextField = new JTextField();
		solarRateTextField.setEditable(false);
		final GridBagConstraints gbc_solarRateTextField = new GridBagConstraints();
		gbc_solarRateTextField.weightx = 1.0;
		gbc_solarRateTextField.insets = new Insets(0, 0, 5, 5);
		gbc_solarRateTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_solarRateTextField.gridx = 1;
		gbc_solarRateTextField.gridy = 1;
		panel_1.add(solarRateTextField, gbc_solarRateTextField);
		solarRateTextField.setColumns(10);

		heatingRateTextField = new JTextField();
		final GridBagConstraints gbc_heatingRateTextField = new GridBagConstraints();
		gbc_heatingRateTextField.weightx = 1.0;
		gbc_heatingRateTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_heatingRateTextField.insets = new Insets(0, 0, 5, 5);
		gbc_heatingRateTextField.gridx = 2;
		gbc_heatingRateTextField.gridy = 1;
		panel_1.add(heatingRateTextField, gbc_heatingRateTextField);
		heatingRateTextField.setEditable(false);
		heatingRateTextField.setColumns(10);

		coolingRateTextField = new JTextField();
		coolingRateTextField.setEditable(false);
		final GridBagConstraints gbc_coolingRateTextField = new GridBagConstraints();
		gbc_coolingRateTextField.weightx = 1.0;
		gbc_coolingRateTextField.insets = new Insets(0, 0, 5, 5);
		gbc_coolingRateTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_coolingRateTextField.gridx = 3;
		gbc_coolingRateTextField.gridy = 1;
		panel_1.add(coolingRateTextField, gbc_coolingRateTextField);
		coolingRateTextField.setColumns(10);

		totalRateTextField = new JTextField();
		totalRateTextField.setEditable(false);
		final GridBagConstraints gbc_totalRateTextField = new GridBagConstraints();
		gbc_totalRateTextField.weightx = 1.0;
		gbc_totalRateTextField.insets = new Insets(0, 0, 5, 0);
		gbc_totalRateTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_totalRateTextField.gridx = 4;
		gbc_totalRateTextField.gridy = 1;
		panel_1.add(totalRateTextField, gbc_totalRateTextField);
		totalRateTextField.setColumns(10);

		final JLabel todayLabel = new JLabel("Today (kWh):");
		final GridBagConstraints gbc_todayLabel = new GridBagConstraints();
		gbc_todayLabel.anchor = GridBagConstraints.WEST;
		gbc_todayLabel.insets = new Insets(0, 0, 5, 5);
		gbc_todayLabel.gridx = 0;
		gbc_todayLabel.gridy = 2;
		panel_1.add(todayLabel, gbc_todayLabel);

		solarTodayTextField = new JTextField();
		solarTodayTextField.setEditable(false);
		final GridBagConstraints gbc_solarTodayTextField = new GridBagConstraints();
		gbc_solarTodayTextField.insets = new Insets(0, 0, 5, 5);
		gbc_solarTodayTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_solarTodayTextField.gridx = 1;
		gbc_solarTodayTextField.gridy = 2;
		panel_1.add(solarTodayTextField, gbc_solarTodayTextField);
		solarTodayTextField.setColumns(10);

		heatingTodayTextField = new JTextField();
		heatingTodayTextField.setEditable(false);
		final GridBagConstraints gbc_heatingTodayTextField = new GridBagConstraints();
		gbc_heatingTodayTextField.insets = new Insets(0, 0, 5, 5);
		gbc_heatingTodayTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_heatingTodayTextField.gridx = 2;
		gbc_heatingTodayTextField.gridy = 2;
		panel_1.add(heatingTodayTextField, gbc_heatingTodayTextField);
		heatingTodayTextField.setColumns(10);

		coolingTodayTextField = new JTextField();
		coolingTodayTextField.setEditable(false);
		final GridBagConstraints gbc_coolingTodayTextField = new GridBagConstraints();
		gbc_coolingTodayTextField.insets = new Insets(0, 0, 5, 5);
		gbc_coolingTodayTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_coolingTodayTextField.gridx = 3;
		gbc_coolingTodayTextField.gridy = 2;
		panel_1.add(coolingTodayTextField, gbc_coolingTodayTextField);
		coolingTodayTextField.setColumns(10);

		totalTodayTextField = new JTextField();
		totalTodayTextField.setEditable(false);
		final GridBagConstraints gbc_totalTodayTextField = new GridBagConstraints();
		gbc_totalTodayTextField.insets = new Insets(0, 0, 5, 0);
		gbc_totalTodayTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_totalTodayTextField.gridx = 4;
		gbc_totalTodayTextField.gridy = 2;
		panel_1.add(totalTodayTextField, gbc_totalTodayTextField);
		totalTodayTextField.setColumns(10);

		final JLabel yearlyLabel = new JLabel("Yearly (kWh):");
		final GridBagConstraints gbc_yearlyLabel = new GridBagConstraints();
		gbc_yearlyLabel.anchor = GridBagConstraints.WEST;
		gbc_yearlyLabel.insets = new Insets(0, 0, 5, 5);
		gbc_yearlyLabel.gridx = 0;
		gbc_yearlyLabel.gridy = 3;
		panel_1.add(yearlyLabel, gbc_yearlyLabel);

		panel_1.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel_1.getPreferredSize().height));

		solarYearlyTextField = new JTextField();
		solarYearlyTextField.setEditable(false);
		final GridBagConstraints gbc_solarYearlyTextField = new GridBagConstraints();
		gbc_solarYearlyTextField.insets = new Insets(0, 0, 5, 5);
		gbc_solarYearlyTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_solarYearlyTextField.gridx = 1;
		gbc_solarYearlyTextField.gridy = 3;
		panel_1.add(solarYearlyTextField, gbc_solarYearlyTextField);
		solarYearlyTextField.setColumns(10);

		heatingYearlyTextField = new JTextField();
		heatingYearlyTextField.setEditable(false);
		final GridBagConstraints gbc_heatingYearlyTextField = new GridBagConstraints();
		gbc_heatingYearlyTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_heatingYearlyTextField.insets = new Insets(0, 0, 5, 5);
		gbc_heatingYearlyTextField.gridx = 2;
		gbc_heatingYearlyTextField.gridy = 3;
		panel_1.add(heatingYearlyTextField, gbc_heatingYearlyTextField);
		heatingYearlyTextField.setColumns(10);

		coolingYearlyTextField = new JTextField();
		coolingYearlyTextField.setEditable(false);
		final GridBagConstraints gbc_coolingYearlyTextField = new GridBagConstraints();
		gbc_coolingYearlyTextField.insets = new Insets(0, 0, 5, 5);
		gbc_coolingYearlyTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_coolingYearlyTextField.gridx = 3;
		gbc_coolingYearlyTextField.gridy = 3;
		panel_1.add(coolingYearlyTextField, gbc_coolingYearlyTextField);
		coolingYearlyTextField.setColumns(10);

		totalYearlyTextField = new JTextField();
		totalYearlyTextField.setEditable(false);
		final GridBagConstraints gbc_totalYearlyTextField = new GridBagConstraints();
		gbc_totalYearlyTextField.insets = new Insets(0, 0, 5, 0);
		gbc_totalYearlyTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_totalYearlyTextField.gridx = 4;
		gbc_totalYearlyTextField.gridy = 3;
		panel_1.add(totalYearlyTextField, gbc_totalYearlyTextField);
		totalYearlyTextField.setColumns(10);

		final JLabel yearlyCostLabel = new JLabel("Yearly Cost:");
		final GridBagConstraints gbc_yearlyCostLabel = new GridBagConstraints();
		gbc_yearlyCostLabel.anchor = GridBagConstraints.WEST;
		gbc_yearlyCostLabel.insets = new Insets(0, 0, 0, 5);
		gbc_yearlyCostLabel.gridx = 0;
		gbc_yearlyCostLabel.gridy = 4;
		panel_1.add(yearlyCostLabel, gbc_yearlyCostLabel);

		solarCostTextField = new JTextField();
		solarCostTextField.setEditable(false);
		final GridBagConstraints gbc_solarCostTextField = new GridBagConstraints();
		gbc_solarCostTextField.insets = new Insets(0, 0, 0, 5);
		gbc_solarCostTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_solarCostTextField.gridx = 1;
		gbc_solarCostTextField.gridy = 4;
		panel_1.add(solarCostTextField, gbc_solarCostTextField);
		solarCostTextField.setColumns(10);

		heatingCostTextField = new JTextField();
		heatingCostTextField.setEditable(false);
		final GridBagConstraints gbc_heatingCostTextField = new GridBagConstraints();
		gbc_heatingCostTextField.insets = new Insets(0, 0, 0, 5);
		gbc_heatingCostTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_heatingCostTextField.gridx = 2;
		gbc_heatingCostTextField.gridy = 4;
		panel_1.add(heatingCostTextField, gbc_heatingCostTextField);
		heatingCostTextField.setColumns(10);

		coolingCostTextField = new JTextField();
		coolingCostTextField.setEditable(false);
		final GridBagConstraints gbc_coolingCostTextField = new GridBagConstraints();
		gbc_coolingCostTextField.insets = new Insets(0, 0, 0, 5);
		gbc_coolingCostTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_coolingCostTextField.gridx = 3;
		gbc_coolingCostTextField.gridy = 4;
		panel_1.add(coolingCostTextField, gbc_coolingCostTextField);
		coolingCostTextField.setColumns(10);

		totalCostTextField = new JTextField();
		totalCostTextField.setEditable(false);
		final GridBagConstraints gbc_totalCostTextField = new GridBagConstraints();
		gbc_totalCostTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_totalCostTextField.gridx = 4;
		gbc_totalCostTextField.gridy = 4;
		panel_1.add(totalCostTextField, gbc_totalCostTextField);
		totalCostTextField.setColumns(10);

		final Dimension size = heatingLabel.getMinimumSize();
		coolingLabel.setMinimumSize(size);
		solarLabel.setMinimumSize(size);
		totalLabel.setMinimumSize(size);

		// fxPanel = new JFXPanel();
		// final GridBagConstraints gbc_fxPanel = new GridBagConstraints();
		// fxPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
		// gbc_fxPanel.gridwidth = 3;
		// gbc_fxPanel.fill = GridBagConstraints.BOTH;
		// gbc_fxPanel.insets = new Insets(0, 0, 5, 0);
		// gbc_fxPanel.gridx = 0;
		// gbc_fxPanel.gridy = 1;
		//
		// add(fxPanel, gbc_fxPanel);

		final JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "U-Factor (W/m2/C)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
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

		wallsComboBox = new JComboBox();
		wallsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				computeEnergy();
			}
		});
		wallsComboBox.setModel(new DefaultComboBoxModel(new String[] { "0.28" }));
		wallsComboBox.setPreferredSize(new Dimension(50, 20));
		final GridBagConstraints gbc_wallsComboBox = new GridBagConstraints();
		gbc_wallsComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_wallsComboBox.gridx = 1;
		gbc_wallsComboBox.gridy = 0;
		panel_2.add(wallsComboBox, gbc_wallsComboBox);
		wallsComboBox.setEditable(true);

		final JLabel doorsLabel = new JLabel("Doors:");
		final GridBagConstraints gbc_doorsLabel = new GridBagConstraints();
		gbc_doorsLabel.anchor = GridBagConstraints.EAST;
		gbc_doorsLabel.insets = new Insets(0, 0, 5, 5);
		gbc_doorsLabel.gridx = 2;
		gbc_doorsLabel.gridy = 0;
		panel_2.add(doorsLabel, gbc_doorsLabel);

		doorsComboBox = new JComboBox();
		doorsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				computeEnergy();
			}
		});
		doorsComboBox.setModel(new DefaultComboBoxModel(new String[] { "1.14" }));
		doorsComboBox.setPreferredSize(new Dimension(50, 20));
		final GridBagConstraints gbc_doorsComboBox = new GridBagConstraints();
		gbc_doorsComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_doorsComboBox.gridx = 3;
		gbc_doorsComboBox.gridy = 0;
		panel_2.add(doorsComboBox, gbc_doorsComboBox);
		doorsComboBox.setEditable(true);

		final JLabel windowsLabel = new JLabel("Windows:");
		final GridBagConstraints gbc_windowsLabel = new GridBagConstraints();
		gbc_windowsLabel.anchor = GridBagConstraints.EAST;
		gbc_windowsLabel.insets = new Insets(0, 0, 0, 5);
		gbc_windowsLabel.gridx = 0;
		gbc_windowsLabel.gridy = 1;
		panel_2.add(windowsLabel, gbc_windowsLabel);

		windowsComboBox = new JComboBox();
		windowsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				computeEnergy();
			}
		});
		windowsComboBox.setModel(new DefaultComboBoxModel(new String[] { "1.89" }));
		windowsComboBox.setPreferredSize(new Dimension(50, 20));
		final GridBagConstraints gbc_windowsComboBox = new GridBagConstraints();
		gbc_windowsComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_windowsComboBox.gridx = 1;
		gbc_windowsComboBox.gridy = 1;
		panel_2.add(windowsComboBox, gbc_windowsComboBox);
		windowsComboBox.setEditable(true);

		final JLabel roofsLabel = new JLabel("Roofs:");
		final GridBagConstraints gbc_roofsLabel = new GridBagConstraints();
		gbc_roofsLabel.anchor = GridBagConstraints.EAST;
		gbc_roofsLabel.insets = new Insets(0, 0, 0, 5);
		gbc_roofsLabel.gridx = 2;
		gbc_roofsLabel.gridy = 1;
		panel_2.add(roofsLabel, gbc_roofsLabel);

		roofsComboBox = new JComboBox();
		roofsComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				computeEnergy();
			}
		});
		roofsComboBox.setModel(new DefaultComboBoxModel(new String[] { "0.14" }));
		roofsComboBox.setPreferredSize(new Dimension(50, 20));
		roofsComboBox.setEditable(true);
		final GridBagConstraints gbc_roofsComboBox = new GridBagConstraints();
		gbc_roofsComboBox.gridx = 3;
		gbc_roofsComboBox.gridy = 1;
		panel_2.add(roofsComboBox, gbc_roofsComboBox);

		panel_2.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel_2.getPreferredSize().height));

		final Component verticalGlue = Box.createVerticalGlue();
		add(verticalGlue);
		new Thread() {
			@Override
			public void run() {
				try {
					Thread.sleep(10000);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
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
			};
		}.start();
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

				// grid.setVgap(20);
				// grid.setHgap(20);
				grid.add(chart, 0, 0);
				fxPanel.setScene(scene);
			}
		});

	}

	public void updateArea(final double walls, final double doors, final double windows, final double roofs) {
		final double total = walls + windows + doors + roofs;
		final boolean isZero = (total == 0.0);
		wallsAreaChartData.setYValue(isZero ? 0 : walls / total * 100.0);
		doorsAreaChartData.setYValue(isZero ? 0 : doors / total * 100.0);
		windowsAreaChartData.setYValue(isZero ? 0 : windows / total * 100.0);
		roofsAreaChartData.setYValue(isZero ? 0 : roofs / total * 100.0);
	}

	public void updateEnergyLoss(final double walls, final double doors, final double windows, final double roofs) {
		final double total = walls + windows + doors + roofs;
		heatingRateTextField.setText("" + total);
		final boolean isZero = (total == 0.0);
		wallsEnergyChartData.setYValue(isZero ? 0 : walls / total * 100.0);
		doorsEnergyChartData.setYValue(isZero ? 0 : doors / total * 100.0);
		windowsEnergyChartData.setYValue(isZero ? 0 : windows / total * 100.0);
		roofsEnergyChartData.setYValue(isZero ? 0 : roofs / total * 100.0);
	}

	public void computeEnergy() {
		if (thread != null && thread.isAlive()) {
			computeRequest = true;
		} else {
			thread = new Thread() {
				@Override
				public void run() {
					do {
						try {
							Thread.sleep(500);
						} catch (final InterruptedException e) {
							e.printStackTrace();
						}
						computeRequest = false;
						computeEnergyNow();
					} while (computeRequest);
				}
			};
			thread.start();
		}
	}

	private void computeEnergyNow() {
		System.out.println("computeEnergyNow()");
		if (autoCheckBox.isSelected())
			updateOutsideTemperature();

		computeSolarEnergyRate();
		computeSolarEnergyToday();
		computeSolarEnergyYearly();

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
		updateArea(wallsArea, doorsArea, windowsArea, roofsArea);

		/* compute energy loss/gain rate and energy loss/gain today */
		final double energyLossRate = computeEnergyLossRate((Integer) insideTemperatureSpinner.getValue() - (Integer) outsideTemperatureSpinner.getValue(), true);
		final double energyLossToday = energyLossRate / 1000.0 * 24.0;

		if (energyLossRate > 0.0) {
			heating.rate = energyLossRate;
			cooling.rate = 0.0;
			heating.today = energyLossToday;
			cooling.today = 0.0;
			heatingRateTextField.setText(noDecimals.format(energyLossRate));
			coolingRateTextField.setText("0");
			heatingTodayTextField.setText(twoDecimals.format(energyLossToday));
			coolingTodayTextField.setText("0");
		} else {
			cooling.rate = -energyLossRate;
			heating.rate = 0.0;
			cooling.today = -energyLossToday;
			heating.today = 0.0;
			coolingRateTextField.setText(noDecimals.format(energyLossRate == 0.0 ? 0.0 : -energyLossRate));
			heatingRateTextField.setText("0");
			coolingTodayTextField.setText(twoDecimals.format(energyLossToday == 0.0 ? 0.0 : -energyLossToday));
			heatingTodayTextField.setText("0");
		}

		/* compute yearly energy loss */
		double yearlyEnergyLoss = 0.0;
		double yearlyEnergyGain = 0.0;
		double yearlyTotal = 0.0;
		for (int i = 0; i < 12; i++) {
			// final double temperature = toCelsius(averageTemperature[i]);
			// final double deltaT = Double.parseDouble(insideTemperatureTextField.getText()) - temperature;
			// final double monthlyEnergy = computeEnergyLossRate(deltaT, false) / 1000.0 * 24.0 * daysInMonth[i];
			// if (monthlyEnergy > 0.0)
			// yearlyEnergyLoss += monthlyEnergy;
			// else
			// yearlyEnergyGain += -monthlyEnergy;
			final double monthlyEnergyLoss = computeEnergyLossRate(heatingDegreeDays[i], false) / 1000.0 * 24.0;
			final double monthlyEnergyGain = computeEnergyLossRate(coolingDegreeDays[i], false) / 1000.0 * 24.0;
			// yearlyEnergyLoss += monthlyEnergyLoss;
			// yearlyEnergyGain += monthlyEnergyGain;
			// yearlyTotal += addEnergy(solarEnergyArray[i], monthlyEnergyLoss, monthlyEnergyGain);
			yearlyEnergyLoss += computeHeatingEnergy(monthlyEnergyLoss, solarEnergyArray[i]);
			yearlyEnergyGain += computeCoolingEnergy(monthlyEnergyLoss, monthlyEnergyGain, solarEnergyArray[i]);
		}
		yearlyTotal = yearlyEnergyLoss + yearlyEnergyGain;
		heating.yearly = yearlyEnergyLoss;
		cooling.yearly = yearlyEnergyGain;
		heatingYearlyTextField.setText(noDecimals.format(yearlyEnergyLoss));
		coolingYearlyTextField.setText(noDecimals.format(yearlyEnergyGain));

		/* compute yearly energy cost */
		heating.cost = yearlyEnergyLoss * COST_PER_KWH;
		cooling.cost = yearlyEnergyGain * COST_PER_KWH;
		heatingCostTextField.setText(moneyDecimals.format(heating.cost));
		coolingCostTextField.setText(moneyDecimals.format(cooling.cost));

		totalRateTextField.setText(noDecimals.format(computeCoolingAndHeatingRate(heating.rate, cooling.rate, solar.rate)));
		totalTodayTextField.setText(twoDecimals.format(computeCoolingAndHeatingEnergy(heating.today, cooling.today, solar.today)));
		totalYearlyTextField.setText(noDecimals.format(yearlyTotal));
		totalCostTextField.setText(moneyDecimals.format(yearlyTotal * COST_PER_KWH));

	}

	public double computeCoolingAndHeatingRate(final double energyLoss, final double energyGain, final double solarEnergy) {
		if (energyLoss > 0.0)
			return computeHeatingEnergy(energyLoss, solarEnergy);
		else
			return computeCoolingEnergy(energyLoss, energyGain, solarEnergy);
	}

	private double computeCoolingAndHeatingEnergy(final double energyLoss, final double energyGain, final double solarEnergy) {
		return computeHeatingEnergy(energyLoss, solarEnergy) + computeCoolingEnergy(energyLoss, energyGain, solarEnergy);
	}

	private double computeCoolingEnergy(final double energyLoss, final double energyGain, final double solarEnergy) {
		if (Heliodon.getInstance().isVisible())
			return energyGain + computeHeatingEnergy(solarEnergy, energyLoss);
		else
			return energyGain;
	}

	private double computeHeatingEnergy(final double energyLoss, final double solarEnergy) {
		if (Heliodon.getInstance().isVisible())
			return Math.max(0, energyLoss - solarEnergy);
		else
			return energyLoss;
	}

	// public double addEnergy(final double solar, final double heating, final double cooling) {
	// return Math.max(0.0, heating - solar) + cooling + solar;
	// }

	private double computeEnergyLossRate(final double deltaT, final boolean draw) {
		final double wallsEnergyLoss = wallsArea * Double.parseDouble((String) wallsComboBox.getSelectedItem()) * deltaT;
		final double doorsEnergyLoss = doorsArea * Double.parseDouble((String) doorsComboBox.getSelectedItem()) * deltaT;
		final double windowsEnergyLoss = windowsArea * Double.parseDouble((String) windowsComboBox.getSelectedItem()) * deltaT;
		final double roofsEnergyLoss = roofsArea * Double.parseDouble((String) roofsComboBox.getSelectedItem()) * deltaT;
		if (draw)
			updateEnergyLoss(wallsEnergyLoss, doorsEnergyLoss, windowsEnergyLoss, roofsEnergyLoss);
		return wallsEnergyLoss + doorsEnergyLoss + windowsEnergyLoss + roofsEnergyLoss;
	}

	private void computeSolarEnergyRate() {
		solar.rate = 0.0;
		if (Heliodon.getInstance().isVisible() && !Heliodon.getInstance().isNightTime())
			solar.rate = computeSolarEnergyRate(Heliodon.getInstance().getSunLocation().normalize(null));
		solarRateTextField.setText(noDecimals.format(solar.rate));
	}

	private double computeSolarEnergyRate(final ReadOnlyVector3 sunVector) {
		double totalRate = 0.0;
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Window) {
				final double dot = part.getContainer().getFaceDirection().dot(sunVector);
				if (dot > 0.0)
					totalRate += 1000.0 * part.computeArea() * dot;
			}
		}
		return totalRate;
	}

	private void computeSolarEnergyToday() {
		solar.today = 0.0;
		if (Heliodon.getInstance().isVisible())
			solar.today = computeSolarEnergyToday((Calendar) Heliodon.getInstance().getCalander().clone());
		solarTodayTextField.setText(twoDecimals.format(solar.today));
	}

	private double computeSolarEnergyToday(final Calendar today) {
		double totalRateToday = 0.0;
		final Heliodon heliodon = Heliodon.getInstance();
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.HOUR, 0);
		for (int i = 0; i < 24; i++) {
			totalRateToday += computeSolarEnergyRate(heliodon.computeSunLocation(today));
			today.add(Calendar.HOUR, 1);
		}
		return totalRateToday / 1000.0 / 24.0;
	}

	private void computeSolarEnergyYearly() {
		double totalEnergyYearly = 0.0;
		if (Heliodon.getInstance().isVisible()) {
			final Calendar date = Calendar.getInstance();
			date.set(Calendar.DAY_OF_MONTH, 0);
			date.set(Calendar.MONTH, 0);
			for (int monthIndex = 0; monthIndex < 11; monthIndex++) {
				solarEnergyArray[monthIndex] = computeSolarEnergyToday(date) * daysInMonth[monthIndex];
				totalEnergyYearly += solarEnergyArray[monthIndex];
				date.add(Calendar.MONTH, 1);
			}
		}
		solar.yearly = totalEnergyYearly;
		solar.cost = totalEnergyYearly * COST_PER_KWH;
		solarYearlyTextField.setText(noDecimals.format(solar.yearly));
		solarCostTextField.setText(moneyDecimals.format(solar.cost));
	}

	private double toCelsius(final double f) {
		return ((f - 32.0) * 5.0 / 9.0);
	}

	private void updateOutsideTemperature() {
		final Date date = (Date) MainPanel.getInstance().getDateSpinner().getValue();
		outsideTemperatureSpinner.setValue((int) Math.round(toCelsius(averageTemperature[date.getMonth()])));
	}

}
