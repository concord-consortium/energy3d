package org.concord.energy3d.simulation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JMenuBar;

import org.concord.energy3d.gui.CspProjectDailyEnergyGraph;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.*;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.BugReporter;

/**
 * For fast feedback, only 12 days are calculated.
 *
 * @author Charles Xie
 */
public class ParabolicDishAnnualAnalysis extends AnnualAnalysis {

    static List<double[]> storedResults;

    public ParabolicDishAnnualAnalysis() {
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
                final double roi = fm.calculateROI(CspProjectCost.getInstance().getTotalArea(), Scene.getInstance().countParts(ParabolicDish.class), annualOutput);
                double paybackPeriod = roi > -100 ? 100.0 / (roi + 100.0) * lifespan : Double.POSITIVE_INFINITY;
                reportResults(storedResults, annualOutput, lifespan, roi, paybackPeriod, parent);
                storedResults.add(new double[]{annualOutput, lifespan, roi, paybackPeriod});
            });
            return null;
        });

    }

    @Override
    public void updateGraph() {
        final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
        if (selectedPart != null) {
            if (selectedPart instanceof ParabolicDish) {
                final ParabolicDish d = (ParabolicDish) selectedPart;
                graph.addData("Solar", d.getSolarPotentialToday() * d.getSystemEfficiency());
            } else if (selectedPart instanceof Foundation) {
                double output = 0;
                for (final HousePart p : Scene.getInstance().getParts()) {
                    if (p instanceof ParabolicDish && p.getTopContainer() == selectedPart) {
                        final ParabolicDish d = (ParabolicDish) p;
                        output += d.getSolarPotentialToday() * d.getSystemEfficiency();
                    }
                }
                graph.addData("Solar", output);
            } else if (selectedPart.getTopContainer() != null) {
                double output = 0;
                for (final HousePart p : Scene.getInstance().getParts()) {
                    if (p instanceof ParabolicDish && p.getTopContainer() == selectedPart.getTopContainer()) {
                        final ParabolicDish d = (ParabolicDish) p;
                        output += d.getSolarPotentialToday() * d.getSystemEfficiency();
                    }
                }
                graph.addData("Solar", output);
            }
        } else {
            double output = 0;
            for (final HousePart p : Scene.getInstance().getParts()) {
                if (p instanceof ParabolicDish) {
                    final ParabolicDish d = (ParabolicDish) p;
                    output += d.getSolarPotentialToday() * d.getSystemEfficiency();
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
        String title = "Annual Yield of All Parabolic Dishes (" + Scene.getInstance().countParts(ParabolicDish.class) + " Dishes)";
        if (selectedPart != null) {
            if (selectedPart instanceof ParabolicDish) {
                cost = (int) CspProjectCost.getPartCost(selectedPart);
                s = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                title = "Annual Yield";
            } else if (selectedPart instanceof Foundation) {
                title = "Annual Yield of Selected Foundation (" + ((Foundation) selectedPart).countParts(ParabolicDish.class) + " Parabolic Dishes)";
            } else if (selectedPart.getTopContainer() != null) {
                title = "Annual Yield of Selected Foundation (" + selectedPart.getTopContainer().countParts(ParabolicDish.class) + " Parabolic Dishes)";
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
            if (selectedPart instanceof ParabolicDish) {
                s += ", \"Parabolic Dish\": \"" + selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1) + "\"";
            } else if (selectedPart instanceof Foundation) {
                s += ", \"Foundation\": \"" + selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1) + "\"";
            } else if (selectedPart.getTopContainer() != null) {
                s += ", \"Foundation\": \"" + selectedPart.getTopContainer().toString().substring(0, selectedPart.getTopContainer().toString().indexOf(')') + 1) + "\"";
            }
        } else {
            s += ", \"Parabolic Dish\": \"All\"";
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