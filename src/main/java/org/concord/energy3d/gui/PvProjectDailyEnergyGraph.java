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
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.DailyGraph;
import org.concord.energy3d.simulation.Graph;
import org.concord.energy3d.simulation.PartEnergyDailyGraph;
import org.concord.energy3d.simulation.PvDailyAnalysis;
import org.concord.energy3d.simulation.SolarRadiation;

/**
 * @author Charles Xie
 */
public class PvProjectDailyEnergyGraph extends JPanel {

    private static final long serialVersionUID = 1L;

    private final PartEnergyDailyGraph graph;
    private Foundation base;
    private final Box buttonPanel;

    PvProjectDailyEnergyGraph() {
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

        graph = new PartEnergyDailyGraph();
        graph.setPopup(false);
        graph.setBackground(Color.WHITE);
        graph.setBorder(BorderFactory.createEtchedBorder());
        graph.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() >= 2 && EnergyPanel.getInstance().checkCity()) {
                    if (SceneManager.getInstance().autoSelectBuilding(true) != null) {
                        final PvDailyAnalysis analysis = new PvDailyAnalysis();
                        analysis.updateGraph();
                        final Graph g = analysis.getGraph();
                        if (g instanceof DailyGraph) {
                            final DailyGraph dg = (DailyGraph) g;
                            dg.setMilitaryTime(graph.getMilitaryTime());
                        }
                        g.setGraphType(graph.getGraphType());
                        analysis.show();
                    }
                }
            }
        });
    }

    public void setCalendar(final Calendar today) {
        graph.setCalendar(today);
    }

    public Foundation getBuilding() {
        return base;
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
        if (base == null) {
            return;
        }
        graph.clearData();
        final List<SolarPanel> panels = base.getSolarPanels();
        final List<Rack> racks = base.getRacks();
        if (!panels.isEmpty() || !racks.isEmpty()) {
            for (int i = 0; i < 24; i++) {
                SolarRadiation.getInstance().computeEnergyAtHour(i);
                double output = 0;
                if (!panels.isEmpty()) {
                    for (final SolarPanel sp : panels) {
                        output += sp.getYieldNow();
                    }
                }
                if (!racks.isEmpty()) {
                    for (final Rack r : racks) {
                        output += r.getYieldNow();
                    }
                }
                graph.addData("Solar", output);
            }
        }
        repaint();
    }

    public void addGraph(final Foundation base) {
        removeAll();
        this.base = base;
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
        if (base != null) {
            s.append("\"Foundation\": ").append(base.getId());
            final List<Double> data = graph.getData("Solar");
            if (data != null) {
                s.append(", \"Solar\": {");
                s.append("\"Hourly\": [");
                for (final Double x : data) {
                    s.append(Graph.FIVE_DECIMALS.format(x)).append(",");
                }
                s = new StringBuilder(s.substring(0, s.length() - 1));
                s.append("]\n");
                s.append(", \"Total\": ").append(Graph.ENERGY_FORMAT.format(getResult("Solar")));
                s.append("}");
            }
        } else {
            // TODO
        }
        s.append("}");
        return s.toString();
    }

}