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
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.DailyGraph;
import org.concord.energy3d.simulation.FresnelReflectorDailyAnalysis;
import org.concord.energy3d.simulation.Graph;
import org.concord.energy3d.simulation.HeliostatDailyAnalysis;
import org.concord.energy3d.simulation.ParabolicDishDailyAnalysis;
import org.concord.energy3d.simulation.ParabolicTroughDailyAnalysis;
import org.concord.energy3d.simulation.PartEnergyDailyGraph;
import org.concord.energy3d.simulation.SolarRadiation;

/**
 * @author Charles Xie
 */
public class CspProjectDailyEnergyGraph extends JPanel {

    private static final long serialVersionUID = 1L;

    private final PartEnergyDailyGraph graph;
    private Foundation base;
    private final Box buttonPanel;

    public CspProjectDailyEnergyGraph() {
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
                if (e.getClickCount() >= 2 && EnergyPanel.getInstance().checkRegion()) {
                    final Foundation f = SceneManager.getInstance().autoSelectBuilding(true);
                    if (f != null) {
                        if (f.countParts(ParabolicTrough.class) > 0) {
                            final ParabolicTroughDailyAnalysis analysis = new ParabolicTroughDailyAnalysis();
                            analysis.updateGraph();
                            final Graph g = analysis.getGraph();
                            if (g instanceof DailyGraph) {
                                final DailyGraph dg = (DailyGraph) g;
                                dg.setMilitaryTime(graph.getMilitaryTime());
                            }
                            g.setGraphType(graph.getGraphType());
                            analysis.show();
                        } else if (f.countParts(ParabolicDish.class) > 0) {
                            final ParabolicDishDailyAnalysis analysis = new ParabolicDishDailyAnalysis();
                            analysis.updateGraph();
                            final Graph g = analysis.getGraph();
                            if (g instanceof DailyGraph) {
                                final DailyGraph dg = (DailyGraph) g;
                                dg.setMilitaryTime(graph.getMilitaryTime());
                            }
                            g.setGraphType(graph.getGraphType());
                            analysis.show();
                        } else if (f.countParts(FresnelReflector.class) > 0) {
                            final FresnelReflectorDailyAnalysis analysis = new FresnelReflectorDailyAnalysis();
                            analysis.updateGraph();
                            final Graph g = analysis.getGraph();
                            if (g instanceof DailyGraph) {
                                final DailyGraph dg = (DailyGraph) g;
                                dg.setMilitaryTime(graph.getMilitaryTime());
                            }
                            g.setGraphType(graph.getGraphType());
                            analysis.show();
                        } else if (f.countParts(Mirror.class) > 0) {
                            final HeliostatDailyAnalysis analysis = new HeliostatDailyAnalysis();
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
        final List<ParabolicTrough> troughs = base.getParabolicTroughs();
        if (!troughs.isEmpty()) { // favor parabolic troughs if there are also mirrors or Fresnel reflectors
            for (int i = 0; i < 24; i++) {
                SolarRadiation.getInstance().computeEnergyAtHour(i);
                double output = 0;
                for (final ParabolicTrough t : troughs) {
                    output += t.getSolarPotentialNow() * t.getSystemEfficiency();
                }
                graph.addData("Solar", output);
            }
        } else {
            final List<ParabolicDish> dishes = base.getParabolicDishes();
            if (!dishes.isEmpty()) {
                for (int i = 0; i < 24; i++) {
                    SolarRadiation.getInstance().computeEnergyAtHour(i);
                    double output = 0;
                    for (final ParabolicDish d : dishes) {
                        output += d.getSolarPotentialNow() * d.getSystemEfficiency();
                    }
                    graph.addData("Solar", output);
                }
            } else {
                final List<FresnelReflector> fresnels = base.getFresnelReflectors();
                if (!fresnels.isEmpty()) {
                    for (int i = 0; i < 24; i++) {
                        SolarRadiation.getInstance().computeEnergyAtHour(i);
                        double output = 0;
                        for (final FresnelReflector r : fresnels) {
                            output += r.getSolarPotentialNow() * r.getSystemEfficiency();
                        }
                        graph.addData("Solar", output);
                    }
                } else {
                    final List<Mirror> mirrors = base.getHeliostats();
                    if (!mirrors.isEmpty()) {
                        for (int i = 0; i < 24; i++) {
                            SolarRadiation.getInstance().computeEnergyAtHour(i);
                            double output = 0;
                            for (final Mirror m : mirrors) {
                                output += m.getSolarPotentialNow() * m.getSystemEfficiency();
                            }
                            graph.addData("Solar", output);
                        }
                    }
                }
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
        String s = "{";
        if (base != null) {
            s += "\"Foundation\": " + base.getId();
            final List<Double> data = graph.getData("Solar");
            if (data != null) {
                s += ", \"Solar\": {";
                s += "\"Hourly\": [";
                for (final Double x : data) {
                    s += Graph.FIVE_DECIMALS.format(x) + ",";
                }
                s = s.substring(0, s.length() - 1);
                s += "]\n";
                s += ", \"Total\": " + Graph.ENERGY_FORMAT.format(getResult("Solar"));
                s += "}";
            }
        } else {
            // TODO
        }
        s += "}";
        return s;
    }

}