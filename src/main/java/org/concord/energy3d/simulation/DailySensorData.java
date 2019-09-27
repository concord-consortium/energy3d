package org.concord.energy3d.simulation;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.scene.Scene;

/**
 * This calculates and visualizes the daily data of a sensor (e.g., light sensor and heat flux sensor).
 * <p>
 * For fast feedback, only 24 hours are calculated.
 *
 * @author Charles Xie
 */

public class DailySensorData extends EnergyDailyAnalysis {

    public DailySensorData() {
        super();
        graph = new PartEnergyDailyGraph();
        graph.instrumentType = Graph.SENSOR;
        graph.setPreferredSize(new Dimension(600, 400));
        graph.setBackground(Color.white);
        graph.yAxisLabel = "Energy Density (kWh/m\u00B2)";
    }

    @Override
    public void updateGraph() {
        final int n = (int) Math.round(60.0 / Scene.getInstance().getTimeStep());
        final List<HousePart> parts = Scene.getInstance().getParts();
        for (final HousePart p : parts) {
            if (p instanceof Sensor) {
                final Sensor sensor = (Sensor) p;
                String label = sensor.getLabelText() != null ? sensor.getLabelText() : sensor.getId() + "";
                String lid = "Light: #" + label;
                String hid = "Heat Flux: #" + label;
                graph.hideData(lid, sensor.isLightOff());
                graph.hideData(hid, sensor.isHeatFluxOff());
                final double area = sensor.getArea();
                for (int i = 0; i < 24; i++) {
                    SolarRadiation.getInstance().computeEnergyAtHour(i);
                    if (!sensor.isLightOff()) {
                        final double solar = sensor.getSolarPotentialNow();
                        graph.addData(lid, solar / area);
                    }
                    if (!sensor.isHeatFluxOff()) {
                        final double[] loss = sensor.getHeatLoss();
                        int t0 = n * i;
                        double sum = 0;
                        for (int k = t0; k < t0 + n; k++)
                            sum += loss[k];
                        graph.addData(hid, -sum / area);
                    }
                }
            }
        }
        graph.repaint();
    }

    @Override
    public String toJson() {
        String s = "{\"Hours\": " + getNumberOfDataPoints() + ", \"Data\": [";
        final List<HousePart> parts = Scene.getInstance().getParts();
        for (final HousePart p : parts) {
            if (p instanceof Sensor) {
                final Sensor sensor = (Sensor) p;
                final long id = sensor.getId();
                s += "{\"ID\": " + id;
                String label = sensor.getLabelText() != null ? sensor.getLabelText() : id + "";
                if (!sensor.isLightOff()) {
                    List<Double> lightData = graph.getData("Light: #" + label);
                    s += ", \"Light\": [";
                    for (Double x : lightData) {
                        s += Graph.FIVE_DECIMALS.format(x) + ",";
                    }
                    s = s.substring(0, s.length() - 1);
                    s += "]\n";
                }
                if (!sensor.isHeatFluxOff()) {
                    List<Double> heatData = graph.getData("Heat Flux: #" + label);
                    s += ", \"HeatFlux\": [";
                    for (Double x : heatData) {
                        s += Graph.FIVE_DECIMALS.format(x) + ",";
                    }
                    s = s.substring(0, s.length() - 1);
                    s += "]";
                }
                s += "},";
            }
        }
        s = s.substring(0, s.length() - 1);
        s += "]}";
        return s;
    }

}