package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.BuildingEnergyDailyGraph;
import org.concord.energy3d.simulation.DailyGraph;
import org.concord.energy3d.simulation.EnergyDailyAnalysis;
import org.concord.energy3d.simulation.Graph;
import org.concord.energy3d.simulation.SolarRadiation;

/**
 * @author Charles Xie
 */
public class BuildingDailyEnergyGraph extends JPanel {

    private static final long serialVersionUID = 1L;

    private final BuildingEnergyDailyGraph graph;
    private Foundation building;
    private final Box buttonPanel;

    BuildingDailyEnergyGraph() {
        super(new BorderLayout());
        setPreferredSize(new Dimension(200, 100));

        buttonPanel = new Box(BoxLayout.Y_AXIS);
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(Box.createVerticalGlue());
        final JButton button = new JButton("Show");
        button.setAlignmentX(CENTER_ALIGNMENT);
        button.addActionListener(e -> {
            SceneManager.getInstance().autoSelectBuilding(true);
            final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
            if (selectedPart instanceof Foundation) {
                addGraph((Foundation) selectedPart);
                EnergyPanel.getInstance().validate();
            }
        });
        buttonPanel.add(button);
        buttonPanel.add(Box.createVerticalGlue());

        graph = new BuildingEnergyDailyGraph();
        graph.setPopup(false);
        graph.setBackground(Color.WHITE);
        graph.setBorder(BorderFactory.createEtchedBorder());
        graph.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() >= 2 && EnergyPanel.getInstance().checkCity()) {
                    if (SceneManager.getInstance().autoSelectBuilding(true) != null) {
                        final EnergyDailyAnalysis analysis = new EnergyDailyAnalysis();
                        analysis.updateGraph();
                        final Graph g = analysis.getGraph();
                        if (g instanceof DailyGraph) {
                            final DailyGraph dg = (DailyGraph) g;
                            dg.setMilitaryTime(graph.getMilitaryTime());
                        }
                        g.setGraphType(graph.getGraphType());
                        analysis.show("Daily Energy");
                    }
                }
            }
        });
    }

    public void setCalendar(final Calendar today) {
        graph.setCalendar(today);
    }

    public Foundation getBuilding() {
        return building;
    }

    public Map<String, List<Double>> getData() {
        return graph.getData();
    }

    public double getResult(final String name) {
        return graph.getSum(name);
    }

    public void clearData() {
        graph.clearData();
        graph.repaint();
    }

    void removeGraph() {
        removeAll();
        repaint();
        add(buttonPanel, BorderLayout.CENTER);
        repaint();
        EnergyPanel.getInstance().validate();
    }

    public boolean hasGraph() {
        return getComponentCount() > 0 && getComponent(0) == graph;
    }

    public void updateGraph() {
        if (building == null) {
            return;
        }
        graph.clearData();
        for (int i = 0; i < 24; i++) {
            SolarRadiation.getInstance().computeEnergyAtHour(i);
            graph.addData("Windows", building.getPassiveSolarNow());
            graph.addData("Solar Panels", building.getPhotovoltaicNow());
            graph.addData("Heater", building.getHeatingNow());
            graph.addData("AC", building.getCoolingNow());
            graph.addData("Net", building.getTotalEnergyNow());
        }
        repaint();
    }

    public void addGraph(final Foundation building) {
        removeAll();
        this.building = building;
        if (getWidth() > 0) {
            graph.setPreferredSize(new Dimension(getWidth() - 5, getHeight() - 5));
        } else {
            graph.setPreferredSize(new Dimension(getPreferredSize().width - 5, getPreferredSize().height - 5));
        }
        if (SceneManager.getInstance().getSolarHeatMap()) {
            updateGraph();
        }
        add(graph, BorderLayout.NORTH);
        repaint();
        EnergyPanel.getInstance().validate();
    }

    public String toJson() {
        StringBuilder s = new StringBuilder("{");
        if (building != null) {
            s.append("\"Building\": ").append(building.getId());
            final String[] names = {"Net", "AC", "Heater", "Windows", "Solar Panels"};
            for (final String name : names) {
                final List<Double> data = graph.getData(name);
                if (data == null) {
                    continue;
                }
                s.append(", \"").append(name).append("\": {");
                s.append("\"Hourly\": [");
                for (final Double x : data) {
                    s.append(Graph.FIVE_DECIMALS.format(x)).append(",");
                }
                s = new StringBuilder(s.substring(0, s.length() - 1));
                s.append("]\n");
                s.append(", \"Total\": ").append(Graph.ENERGY_FORMAT.format(getResult(name)));
                s.append("}");
            }
        } else {
            // TODO
        }
        s.append("}");
        return s.toString();
    }

}