package org.concord.energy3d.gui;

import java.awt.BorderLayout;
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
	private final JFXPanel fxPanel;

	public EnergyPanel() {
		setLayout(new BorderLayout(0, 0));
		fxPanel = new JFXPanel();
		add(fxPanel, BorderLayout.CENTER);
		initFxComponents();
	}

	private void initFxComponents() {

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				final GridPane grid = new GridPane();
				final Scene scene = new Scene(grid, 800, 400);

				/**
				 * Construct and populate Bar chart. It uses 2 series of data.
				 */
				final NumberAxis yAxis = new NumberAxis(0, 100, 10);
				final CategoryAxis xAxis = new CategoryAxis();
				xAxis.setCategories(FXCollections.<String>observableArrayList(Arrays.asList("Area", "Energy Loss")));
				final StackedBarChart barChart = new StackedBarChart(xAxis, yAxis);
				final XYChart.Series bar1 = new XYChart.Series();
				bar1.setName("Walls");
				bar1.getData().add(getData(50, "Area"));
				bar1.getData().add(getData(30, "Energy Loss"));

				final XYChart.Series bar2 = new XYChart.Series();
				bar2.setName("Windows");
				bar2.getData().add(getData(10, "Area"));
				bar2.getData().add(getData(40, "Energy Loss"));

				barChart.getData().addAll(bar1, bar2);
				grid.setVgap(20);
				grid.setHgap(20);
				grid.add(barChart, 2, 0);
				fxPanel.setScene(scene);
			}
		});

	}

	private XYChart.Data getData(final double x, final double y) {
		final XYChart.Data data = new XYChart.Data();
		data.setXValue(x);
		data.setYValue(y);
		return data;
	}

	private XYChart.Data getData(final double x, final String y) {
		final XYChart.Data data = new XYChart.Data();
		data.setYValue(x);
		data.setXValue(y);
		return data;
	}
}
