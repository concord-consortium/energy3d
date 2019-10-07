package org.concord.energy3d.simulation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.*;

import org.concord.energy3d.gui.CspProjectDailyEnergyGraph;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.BugReporter;

/**
 * For fast feedback, only 12 days are calculated.
 *
 * @author Charles Xie
 */
public class HeliostatAnnualAnalysis extends AnnualAnalysis {

    static List<double[]> storedResults;

    public HeliostatAnnualAnalysis() {
        super();
        graph = new PartEnergyAnnualGraph();
        graph.setPreferredSize(new Dimension(600, 400));
        graph.setBackground(Color.WHITE);
        if (storedResults == null) {
            storedResults = new ArrayList<>();
        }
    }

    @Override
    void runAnalysis(final JDialog parent) {
        graph.info = "Calculating...";
        graph.repaint();
        onStart();
        final EnergyPanel e = EnergyPanel.getInstance();
        for (final int m : MONTHS) {
            SceneManager.getTaskManager().update(() -> {
                if (!analysisStopped) {
                    final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
                    if (selectedPart instanceof Tree || selectedPart instanceof Human) { // make sure that we deselect trees or humans, which cannot be attributed to a foundation
                        SceneManager.getInstance().setSelectedPart(null);
                    }
                    final Calendar c = Heliodon.getInstance().getCalendar();
                    c.set(Calendar.MONTH, m);
                    final Calendar today = (Calendar) c.clone();
                    Scene.getInstance().updateTrackables();
                    final Throwable t = compute();
                    if (t != null) {
                        stopAnalysis();
                        EventQueue.invokeLater(() -> BugReporter.report(t));
                    }
                    if (selectedPart instanceof Foundation) { // synchronize with daily graph
                        final CspProjectDailyEnergyGraph g = e.getCspProjectDailyEnergyGraph();
                        if (g.hasGraph()) {
                            g.setCalendar(today);
                            g.updateGraph();
                        }
                    }
                    EventQueue.invokeLater(() -> {
                        e.getDateSpinner().setValue(c.getTime());
                        if (selectedPart instanceof Foundation) {
                            final CspProjectDailyEnergyGraph g = e.getCspProjectDailyEnergyGraph();
                            e.getCspProjectTabbedPane().setSelectedComponent(g);
                            if (!g.hasGraph()) {
                                g.setCalendar(today);
                                g.addGraph((Foundation) selectedPart);
                            }
                        }
                    });
                }
                return null;
            });

        }

        SceneManager.getTaskManager().update(() -> {
            EventQueue.invokeLater(() -> {
                onCompletion();
                if (Heliodon.getInstance().getCalendar().get(Calendar.MONTH) != Calendar.DECEMBER) {
                    return; // annual calculation aborted
                }
                final double annualOutput = getResult("Solar");
                final CspFinancialModel fm = Scene.getInstance().getCspFinancialModel();
                final int lifespan = fm.getLifespan();
                final double roi = fm.calculateROI(CspProjectCost.getInstance().getTotalArea(), Scene.getInstance().countParts(Mirror.class), annualOutput);
                reportResults(storedResults, annualOutput, lifespan, roi, parent);
                storedResults.add(new double[]{annualOutput, lifespan, roi});
            });
            return null;
        });

    }

    @Override
    public void updateGraph() {
        final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
        if (selectedPart != null) {
            if (selectedPart instanceof Mirror) {
                final Mirror m = (Mirror) selectedPart;
                graph.addData("Solar", m.getSolarPotentialToday() * m.getSystemEfficiency());
            } else if (selectedPart instanceof Foundation) {
                double output = 0;
                for (final HousePart p : Scene.getInstance().getParts()) {
                    if (p instanceof Mirror && p.getTopContainer() == selectedPart) {
                        final Mirror m = (Mirror) p;
                        output += m.getSolarPotentialToday() * m.getSystemEfficiency();
                    }
                }
                graph.addData("Solar", output);
            } else if (selectedPart.getTopContainer() != null) {
                double output = 0;
                for (final HousePart p : Scene.getInstance().getParts()) {
                    if (p instanceof Mirror && p.getTopContainer() == selectedPart.getTopContainer()) {
                        final Mirror m = (Mirror) p;
                        output += m.getSolarPotentialToday() * m.getSystemEfficiency();
                    }
                }
                graph.addData("Solar", output);
            }
        } else {
            double output = 0;
            for (final HousePart p : Scene.getInstance().getParts()) {
                if (p instanceof Mirror) {
                    final Mirror m = (Mirror) p;
                    output += m.getSolarPotentialToday() * m.getSystemEfficiency();
                }
            }
            graph.addData("Solar", output);
        }
        graph.repaint();

    }

    public void show() {
        final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
        String s = null;
        int cost = -1;
        String title = "Annual Yield of All Heliostats (" + Scene.getInstance().countParts(Mirror.class) + " Heliostats)";
        if (selectedPart != null) {
            if (selectedPart instanceof Mirror) {
                cost = (int) CspProjectCost.getPartCost(selectedPart);
                s = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                title = "Annual Yield";
            } else if (selectedPart instanceof Foundation) {
                title = "Annual Yield of Selected Foundation (" + ((Foundation) selectedPart).countParts(Mirror.class) + " Heliostats)";
            } else if (selectedPart.getTopContainer() != null) {
                title = "Annual Yield of Selected Foundation (" + selectedPart.getTopContainer().countParts(Mirror.class) + " Heliostats)";
            }
        }
        final JDialog dialog = createDialog(s == null ? title : title + ": " + s + " (Cost: $" + cost + ")");
        final JMenuBar menuBar = new JMenuBar();
        dialog.setJMenuBar(menuBar);
        menuBar.add(createOptionsMenu(dialog, null, true, true));
        menuBar.add(createRunsMenu());
        dialog.setVisible(true);
    }

    @Override
    public String toJson() {
        String s = "{\"Months\": " + getNumberOfDataPoints();
        final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
        if (selectedPart != null) {
            if (selectedPart instanceof Mirror) {
                s += ", \"Heliostat\": \"" + selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1) + "\"";
            } else if (selectedPart instanceof Foundation) {
                s += ", \"Foundation\": \"" + selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1) + "\"";
            } else if (selectedPart.getTopContainer() != null) {
                s += ", \"Foundation\": \"" + selectedPart.getTopContainer().toString().substring(0, selectedPart.getTopContainer().toString().indexOf(')') + 1) + "\"";
            }
        } else {
            s += ", \"Heliostat\": \"All\"";
        }
        final String name = "Solar";
        final List<Double> data = graph.getData(name);
        s += ", \"" + name + "\": {";
        s += "\"Monthly\": [";
        for (final Double x : data) {
            s += Graph.ENERGY_FORMAT.format(x) + ",";
        }
        s = s.substring(0, s.length() - 1);
        s += "]\n";
        s += ", \"Total\": " + Graph.ENERGY_FORMAT.format(getResult(name));
        s += "}";
        s += "}";
        return s;
    }

}