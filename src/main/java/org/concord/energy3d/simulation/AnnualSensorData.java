package org.concord.energy3d.simulation;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.scene.Scene;

/**
 * This calculates and visualizes the seasonal trend and the yearly sum of a sensor (e.g., light sensor and heat flux sensor).
 *
 * For fast feedback, only 12 days are calculated.
 *
 * @author Charles Xie
 *
 */

public class AnnualSensorData extends EnergyAnnualAnalysis {

	public AnnualSensorData() {
		super();
		graph = new PartEnergyAnnualGraph();
		graph.type = Graph.SENSOR;
		graph.setPreferredSize(new Dimension(600, 400));
		graph.setBackground(Color.white);
		graph.yAxisLabel = "Energy Density (kWh/m\u00B2)";
	}

	@Override
	public void updateGraph() {
		final List<HousePart> parts = Scene.getInstance().getParts();
		for (final HousePart p : parts) {
			if (p instanceof Sensor) {
				final Sensor sensor = (Sensor) p;
				final double area = sensor.getArea();
				final double solar = sensor.getSolarPotentialToday();
				graph.addData("Light: #" + sensor.getId(), solar / area);
				final double[] loss = sensor.getHeatLoss();
				double sum = 0;
				for (final double x : loss)
					sum += x;
				graph.addData("Heat Flux: #" + sensor.getId(), -sum / area);
			}
		}

		graph.repaint();

	}

	@Override
	public String toJson() {
		String s = "{\"Months\": " + getNumberOfDataPoints() + ", \"Data\": [";
		final List<HousePart> parts = Scene.getInstance().getParts();
		for (final HousePart p : parts) {
			if (p instanceof Sensor) {
				final Sensor sensor = (Sensor) p;
				final long id = sensor.getId();
				List<Double> lightData = graph.getData("Light: #" + id);
				s += "{\"ID\": " + id;
				s += ", \"Light\": [";
				for (Double x : lightData) {
					s += Graph.FIVE_DECIMALS.format(x) + ",";
				}
				s = s.substring(0, s.length() - 1);
				s += "]\n";
				List<Double> heatData = graph.getData("Heat Flux: #" + id);
				s += ", \"HeatFlux\": [";
				for (Double x : heatData) {
					s += Graph.FIVE_DECIMALS.format(x) + ",";
				}
				s = s.substring(0, s.length() - 1);
				s += "]";
				s += "},";
			}
		}
		s = s.substring(0, s.length() - 1);
		s += "]}";
		return s;
	}

}
