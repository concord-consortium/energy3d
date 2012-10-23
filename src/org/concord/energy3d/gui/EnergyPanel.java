package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
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

import javax.swing.JPanel;

public class EnergyPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final EnergyPanel instance = new EnergyPanel();
	private final JFXPanel fxPanel;
	final XYChart.Data<String, Number> wallsArea = new XYChart.Data<String, Number>("Area", 0);
	final XYChart.Data<String, Number> windowsArea = new XYChart.Data<String, Number>("Area", 0);
	final XYChart.Data<String, Number> doorsArea = new XYChart.Data<String, Number>("Area", 0);
	final XYChart.Data<String, Number> roofsArea = new XYChart.Data<String, Number>("Area", 0);
	final XYChart.Data<String, Number> wallsEnergy = new XYChart.Data<String, Number>("Energy Loss", 0);
	final XYChart.Data<String, Number> windowsEnergy = new XYChart.Data<String, Number>("Energy Loss", 0);
	final XYChart.Data<String, Number> doorsEnergy = new XYChart.Data<String, Number>("Energy Loss", 0);
	final XYChart.Data<String, Number> roofsEnergy = new XYChart.Data<String, Number>("Energy Loss", 0);

	public static EnergyPanel getInstance() {
		return instance;
	}

	private EnergyPanel() {
		setMinimumSize(new Dimension(230, 0));
		setPreferredSize(new Dimension(230, 0));
		setLayout(new BorderLayout(0, 0));
		fxPanel = new JFXPanel();
		add(fxPanel, BorderLayout.CENTER);
		initFxComponents();
	}

	XYChart.Series<String, Number> series;

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

				series = new XYChart.Series<String, Number>();
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

	public void updateArea(final double walls, final double windows, final double doors, final double roofs) {
		final double total = (walls + windows + doors + roofs) / 100.0;
		System.out.println(total);
		wallsArea.setYValue(walls / total);
		windowsArea.setYValue(windows / total);
		doorsArea.setYValue(doors / total);
		roofsArea.setYValue(roofs / total);
	}

	public void updateEnergyLoss(final double walls, final double windows, final double doors, final double roofs) {
		final double total = (walls + windows + doors + roofs) / 100.0;
		System.out.println(total);
		wallsEnergy.setYValue(walls / total);
		windowsEnergy.setYValue(windows / total);
		doorsEnergy.setYValue(doors / total);
		roofsEnergy.setYValue(roofs / total);
	}

}
