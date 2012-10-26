package org.concord.energy3d.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
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
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;

public class EnergyPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final EnergyPanel instance = new EnergyPanel();
	private final JFXPanel fxPanel;
	private final XYChart.Data<String, Number> wallsArea = new XYChart.Data<String, Number>("Area", 0);
	private final XYChart.Data<String, Number> windowsArea = new XYChart.Data<String, Number>("Area", 0);
	private final XYChart.Data<String, Number> doorsArea = new XYChart.Data<String, Number>("Area", 0);
	private final XYChart.Data<String, Number> roofsArea = new XYChart.Data<String, Number>("Area", 0);
	private final XYChart.Data<String, Number> wallsEnergy = new XYChart.Data<String, Number>("Energy Loss", 0);
	private final XYChart.Data<String, Number> windowsEnergy = new XYChart.Data<String, Number>("Energy Loss", 0);
	private final XYChart.Data<String, Number> doorsEnergy = new XYChart.Data<String, Number>("Energy Loss", 0);
	private final XYChart.Data<String, Number> roofsEnergy = new XYChart.Data<String, Number>("Energy Loss", 0);
	private final JTextField energyLossTextField;
	private final JTextField insideTemperatureTextField;
	private final JTextField outsideTemperatureTextField;
	private final JComboBox wallsComboBox;
	private final JComboBox doorsComboBox;
	private final JComboBox windowsComboBox;
	private final JComboBox roofsComboBox;

	public static EnergyPanel getInstance() {
		return instance;
	}

	private EnergyPanel() {
		setMinimumSize(new Dimension(230, 0));
		setPreferredSize(new Dimension(230, 388));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		final JPanel panel_1 = new JPanel();
		add(panel_1);

		final JLabel label = new JLabel("Energy Loss:");
		panel_1.add(label);

		energyLossTextField = new JTextField();
		panel_1.add(energyLossTextField);
		energyLossTextField.setEditable(false);
		energyLossTextField.setColumns(10);

		final JLabel wattsLabel = new JLabel("watts");
		panel_1.add(wattsLabel);

		panel_1.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel_1.getPreferredSize().height));

		fxPanel = new JFXPanel();
		final GridBagConstraints gbc_fxPanel = new GridBagConstraints();
		fxPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
		gbc_fxPanel.gridwidth = 3;
		gbc_fxPanel.fill = GridBagConstraints.BOTH;
		gbc_fxPanel.insets = new Insets(0, 0, 5, 0);
		gbc_fxPanel.gridx = 0;
		gbc_fxPanel.gridy = 1;
		if (true)
			add(fxPanel, gbc_fxPanel);

		final JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Temperature", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel);
		final GridBagLayout gbl_panel = new GridBagLayout();
		panel.setLayout(gbl_panel);

		final JCheckBox autoCheckBox = new JCheckBox("Auto outside temperature");
		final GridBagConstraints gbc_autoCheckBox = new GridBagConstraints();
		gbc_autoCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_autoCheckBox.gridwidth = 4;
		gbc_autoCheckBox.gridx = 0;
		gbc_autoCheckBox.gridy = 0;
		panel.add(autoCheckBox, gbc_autoCheckBox);

		final JLabel insideTemperatureLabel = new JLabel("Inside:");
		final GridBagConstraints gbc_insideTemperatureLabel = new GridBagConstraints();
		gbc_insideTemperatureLabel.insets = new Insets(0, 0, 5, 5);
		gbc_insideTemperatureLabel.gridx = 0;
		gbc_insideTemperatureLabel.gridy = 1;
		panel.add(insideTemperatureLabel, gbc_insideTemperatureLabel);

		insideTemperatureTextField = new JTextField();
		insideTemperatureTextField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				computeAreaAndEnergy();
			}
		});
		insideTemperatureTextField.setText("75");
		final GridBagConstraints gbc_insideTemperatureTextField = new GridBagConstraints();
		gbc_insideTemperatureTextField.insets = new Insets(0, 0, 5, 5);
		gbc_insideTemperatureTextField.gridx = 1;
		gbc_insideTemperatureTextField.gridy = 1;
		panel.add(insideTemperatureTextField, gbc_insideTemperatureTextField);
		insideTemperatureTextField.setColumns(4);

		final JLabel outsideTemperatureLabel = new JLabel("Outside:");
		final GridBagConstraints gbc_outsideTemperatureLabel = new GridBagConstraints();
		gbc_outsideTemperatureLabel.insets = new Insets(0, 0, 5, 5);
		gbc_outsideTemperatureLabel.gridx = 2;
		gbc_outsideTemperatureLabel.gridy = 1;
		panel.add(outsideTemperatureLabel, gbc_outsideTemperatureLabel);

		outsideTemperatureTextField = new JTextField();
		outsideTemperatureTextField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				computeAreaAndEnergy();
			}
		});
		outsideTemperatureTextField.setText("65");
		final GridBagConstraints gbc_outsideTemperatureTextField = new GridBagConstraints();
		gbc_outsideTemperatureTextField.insets = new Insets(0, 0, 5, 5);
		gbc_outsideTemperatureTextField.gridx = 3;
		gbc_outsideTemperatureTextField.gridy = 1;
		panel.add(outsideTemperatureTextField, gbc_outsideTemperatureTextField);
		outsideTemperatureTextField.setColumns(4);

		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));

		final JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "U-Factor", TitledBorder.LEADING, TitledBorder.TOP, null, null));
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
				computeAreaAndEnergy();
			}
		});
		wallsComboBox.setModel(new DefaultComboBoxModel(new String[] {"0.09"}));
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
				computeAreaAndEnergy();
			}
		});
		doorsComboBox.setModel(new DefaultComboBoxModel(new String[] {"0.50"}));
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
				computeAreaAndEnergy();
			}
		});
		windowsComboBox.setModel(new DefaultComboBoxModel(new String[] {"0.30"}));
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
				computeAreaAndEnergy();
			}
		});
		roofsComboBox.setModel(new DefaultComboBoxModel(new String[] {"0.05"}));
		roofsComboBox.setPreferredSize(new Dimension(50, 20));
		roofsComboBox.setEditable(true);
		final GridBagConstraints gbc_roofsComboBox = new GridBagConstraints();
		gbc_roofsComboBox.gridx = 3;
		gbc_roofsComboBox.gridy = 1;
		panel_2.add(roofsComboBox, gbc_roofsComboBox);

		panel_2.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel_2.getPreferredSize().height));

		final Component verticalGlue = Box.createVerticalGlue();
		add(verticalGlue);
		initFxComponents();
	}

	private void initFxComponents() {

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				final GridPane grid = new GridPane();
				final Scene scene = new Scene(grid, 800, 400);
				scene.getStylesheets().add("org/concord/energy3d/css/fx.css");
				final NumberAxis yAxis = new NumberAxis(0, 100, 10);
				final CategoryAxis xAxis = new CategoryAxis();
				xAxis.setCategories(FXCollections.<String> observableArrayList(Arrays.asList("Area", "Energy Loss")));
				final StackedBarChart<String, Number> chart = new StackedBarChart<String, Number>(xAxis, yAxis);
				chart.setStyle("-fx-background-color: #" + Integer.toHexString(UIManager.getColor("Panel.background").getRGB() & 0x00FFFFFF) + ";");

				XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
				series.setName("Walls");
				series.getData().add(wallsArea);
				series.getData().add(wallsEnergy);
				chart.getData().add(series);

				series = new XYChart.Series<String, Number>();
				series.setName("Doors");
				series.getData().add(doorsArea);
				series.getData().add(doorsEnergy);
				chart.getData().add(series);

				series = new XYChart.Series<String, Number>();
				series.setName("Windows");
				series.getData().add(windowsArea);
				series.getData().add(windowsEnergy);
				chart.getData().add(series);

				series = new XYChart.Series<String, Number>();
				series.setName("Roof");
				series.getData().add(roofsArea);
				series.getData().add(roofsEnergy);
				chart.getData().add(series);

				// grid.setVgap(20);
				// grid.setHgap(20);
				grid.add(chart, 0, 0);
				fxPanel.setScene(scene);
			}
		});

	}

	public void updateArea(final double walls, final double doors, final double windows, final double roofs) {
		final double total = (walls + windows + doors + roofs) / 100.0;
		wallsArea.setYValue(walls / total);
		doorsArea.setYValue(doors / total);
		windowsArea.setYValue(windows / total);
		roofsArea.setYValue(roofs / total);
	}

	public void updateEnergyLoss(final double walls, final double doors, final double windows, final double roofs) {
		final double total = (walls + windows + doors + roofs) / 100.0;
		energyLossTextField.setText("" + total);
		wallsEnergy.setYValue(walls / total);
		doorsEnergy.setYValue(doors / total);
		windowsEnergy.setYValue(windows / total);
		roofsEnergy.setYValue(roofs / total);
	}

	public void computeAreaAndEnergy() {
		double wallsArea = 0;
		double doorsArea = 0;
		double windowsArea = 0;
		double roofsArea = 0;
		for (final HousePart part : org.concord.energy3d.scene.Scene.getInstance().getParts()) {
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

		final double deltaT = Double.parseDouble(insideTemperatureTextField.getText()) - Double.parseDouble(outsideTemperatureTextField.getText());
		final double wallsEnergyLoss = wallsArea * Double.parseDouble((String)wallsComboBox.getSelectedItem()) * deltaT ;
		final double doorsEnergyLoss = doorsArea *  Double.parseDouble((String)doorsComboBox.getSelectedItem()) * deltaT;
		final double windowsEnergyLoss = windowsArea * Double.parseDouble((String)windowsComboBox.getSelectedItem()) * deltaT;
		final double roofsEnergyLoss = roofsArea * Double.parseDouble((String)roofsComboBox.getSelectedItem()) * deltaT;

		updateEnergyLoss(wallsEnergyLoss, doorsEnergyLoss, windowsEnergyLoss, roofsEnergyLoss);
	}

}
