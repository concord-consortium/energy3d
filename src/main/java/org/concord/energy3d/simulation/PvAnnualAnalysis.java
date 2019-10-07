package org.concord.energy3d.simulation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.PvProjectDailyEnergyGraph;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
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
public class PvAnnualAnalysis extends AnnualAnalysis {

    private UtilityBill utilityBill;
    private static List<double[]> storedResults;

    public PvAnnualAnalysis() {
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
                        final PvProjectDailyEnergyGraph g = e.getPvProjectDailyEnergyGraph();
                        if (g.hasGraph()) {
                            g.setCalendar(today);
                            g.updateGraph();
                        }
                    }
                    EventQueue.invokeLater(() -> {
                        e.getDateSpinner().setValue(c.getTime());
                        if (selectedPart instanceof Foundation) {
                            final PvProjectDailyEnergyGraph g = e.getPvProjectDailyEnergyGraph();
                            e.getPvProjectTabbedPane().setSelectedComponent(g);
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
                final PvFinancialModel fm = Scene.getInstance().getPvFinancialModel();
                final int lifespan = fm.getLifespan();
                final double roi = fm.calculateROI(Scene.getInstance().getTotalFoundationAreas(), Scene.getInstance().countSolarPanels(), annualOutput);
                final int n = storedResults.size();
                if (n > 0) {
                    String previousResults = "<table border=1>";
                    previousResults += "<tr bgcolor=#cccccc><td><b>Run</b></td><td><b>Annual Electricity (kWh)</b></td><td><b>Life Span (Year)</b></td><td><b>ROI (%)</b></td></tr>";
                    int m = n < 5 ? 0 : n - 5;
                    for (int i = n - 1; i >= m; i--) {
                        previousResults += (i % 2 == 0 ? "<tr bgcolor=#cceecc>" : "<tr bgcolor=#eeccee>") + "<td>#" + (i + 1) + "</td>";
                        double[] results = storedResults.get(i);
                        for (int j = 0; j < results.length; j++) {
                            previousResults += "<td>" + Graph.TWO_DECIMALS.format(results[j]) + "</td>";
                        }
                        previousResults += "</tr>";
                    }
                    previousResults += "</table>";
                    final Object[] options = new Object[]{"OK", "Copy Data"};
                    String msg = "<html>The calculated annual output is <b>" + Graph.TWO_DECIMALS.format(annualOutput) + " kWh</b>.";
                    msg += "<br>Based on this prediction, the ROI over " + lifespan + " years is <b>" + Graph.TWO_DECIMALS.format(roi) + "%</b>.";
                    msg += "<br><hr>Compare with the results from last " + (n - m) + " runs:<br>" + previousResults + "</html>";
                    final JOptionPane optionPane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, options, options[0]);
                    final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Annual Electricity Output and Return on Investment");
                    dialog.setVisible(true);
                    final Object choice = optionPane.getValue();
                    if (choice == options[1]) {
                        String output = "";
                        for (int i = 0; i < n; i++) {
                            double[] results = storedResults.get(i);
                            for (int j = 0; j < results.length; j++) {
                                output += results[j];
                                if (j < results.length - 1) {
                                    output += ", ";
                                }
                            }
                            output += "\n";
                        }
                        output += annualOutput + ", " + (double) lifespan + ", " + roi;
                        final Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                        clpbrd.setContents(new StringSelection(output), null);
                        JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>" + (n + 1) + " data points copied to system clipboard.", "Confirmation", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    StringBuilder report = new StringBuilder("<html>");
                    report.append("The calculated annual output is <b>" + Graph.TWO_DECIMALS.format(annualOutput) + " kWh</b>.");
                    report.append("<br>Based on this prediction, the ROI over " + lifespan + " years is <b>" + Graph.TWO_DECIMALS.format(roi) + "%</b>.");
                    report.append("</html>");
                    JOptionPane.showMessageDialog(parent, report.toString(), "Annual Electricity Output and Return on Investment", JOptionPane.INFORMATION_MESSAGE);
                }
                storedResults.add(new double[]{annualOutput, lifespan, roi});
            });
            return null;
        });
    }

    @Override
    void viewFullHistory() {
        final int n = storedResults.size();
        if (n <= 0) {
            JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>No previous run.</html>", "Full History", JOptionPane.INFORMATION_MESSAGE);
        } else {
            String previousResults = "<table width=100% border=1><tr bgcolor=#cccccc><td><b><font size=3>Run</b></td><td><b><font size=3>Annual Electricity (kWh)</b></td><td><b><font size=3>Life Span (year)</b></td><td><b><font size=3>ROI (%)</b></td></tr>";
            for (int i = n - 1; i >= 0; i--) {
                previousResults += (i % 2 == 0 ? "<tr bgcolor=#cceecc>" : "<tr bgcolor=#eeccee>") + "<td><font size=3>#" + (i + 1) + "</td>";
                double[] results = storedResults.get(i);
                for (int j = 0; j < results.length; j++) {
                    previousResults += "<td><font size=3>" + Graph.TWO_DECIMALS.format(results[j]) + "</td>";
                }
                previousResults += "</tr>";
            }
            previousResults += "</table>";
            final Object[] options = new Object[]{"OK", "Copy Data"};
            final JEditorPane htmlPane = new JEditorPane();
            htmlPane.setContentType("text/html");
            htmlPane.setText("<html>" + previousResults + "</html>");
            htmlPane.setEditable(false);
            final JScrollPane scrollPane = new JScrollPane(htmlPane);
            scrollPane.setPreferredSize(new Dimension(400, 400));
            final JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, options, options[0]);
            final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Results from All Previous Runs");
            dialog.setVisible(true);
            final Object choice = optionPane.getValue();
            if (choice == options[1]) {
                String output = "";
                for (int i = 0; i < n; i++) {
                    double[] results = storedResults.get(i);
                    for (int j = 0; j < results.length; j++) {
                        output += results[j];
                        if (j < results.length - 1) {
                            output += ", ";
                        }
                    }
                    output += "\n";
                }
                final Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                clpbrd.setContents(new StringSelection(output), null);
                JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>" + n + " data points copied to system clipboard.</html>", "Confirmation", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    @Override
    public void updateGraph() {
        final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
        if (selectedPart != null) {
            if (selectedPart instanceof SolarPanel) {
                graph.addData("Solar", ((SolarPanel) selectedPart).getYieldToday());
            } else if (selectedPart instanceof Rack) {
                graph.addData("Solar", ((Rack) selectedPart).getYieldToday());
            } else if (selectedPart instanceof Foundation) {
                double output = 0;
                for (final HousePart p : Scene.getInstance().getParts()) {
                    if (p.getTopContainer() == selectedPart) {
                        if (p instanceof SolarPanel) {
                            output += ((SolarPanel) p).getYieldToday();
                        } else if (p instanceof Rack) {
                            output += ((Rack) p).getYieldToday();
                        }
                    }
                }
                graph.addData("Solar", output);
            } else if (selectedPart.getTopContainer() != null) {
                double output = 0;
                for (final HousePart p : Scene.getInstance().getParts()) {
                    if (p.getTopContainer() == selectedPart.getTopContainer()) {
                        if (p instanceof SolarPanel) {
                            output += ((SolarPanel) p).getYieldToday();
                        } else if (p instanceof Rack) {
                            output += ((Rack) p).getYieldToday();
                        }
                    }
                }
                graph.addData("Solar", output);
            }
        } else {
            double output = 0;
            for (final HousePart p : Scene.getInstance().getParts()) {
                if (p instanceof SolarPanel) {
                    output += ((SolarPanel) p).getYieldToday();
                } else if (p instanceof Rack) {
                    output += ((Rack) p).getYieldToday();
                }
            }
            graph.addData("Solar", output);
        }
        graph.repaint();

    }

    public void setUtilityBill(final UtilityBill utilityBill) {
        if (utilityBill == null) {
            return;
        }
        this.utilityBill = utilityBill;
        final double[] bill = utilityBill.getMonthlyEnergy();
        for (double v : bill) {
            graph.addData("Utility", v / (365.0 / 12.0));
        }
        graph.repaint();
    }

    @Override
    void onStart() {
        super.onStart();
        if (utilityBill != null) {
            final double[] bill = utilityBill.getMonthlyEnergy();
            for (double v : bill) {
                graph.addData("Utility", v / (365.0 / 12.0));
            }
        }
    }

    public void show() {
        final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
        String s = null;
        int cost = -1;
        String title = "Annual Yield of All Solar Panels (" + Scene.getInstance().countSolarPanels() + " Panels)";
        if (selectedPart != null) {
            if (selectedPart instanceof SolarPanel) {
                cost = (int) ProjectCost.getCost(selectedPart);
                s = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                title = "Annual Yield";
            } else if (selectedPart instanceof Rack) {
                final Rack rack = (Rack) selectedPart;
                cost = (int) ProjectCost.getCost(rack);
                s = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
                title = "Annual Yield (" + rack.getNumberOfSolarPanels() + " Solar Panels)";
            } else if (selectedPart instanceof Foundation) {
                title = "Annual Yield on Selected Foundation (" + ((Foundation) selectedPart).getNumberOfSolarPanels() + " Solar Panels)";
            } else if (selectedPart.getTopContainer() != null) {
                title = "Annual Yield on Selected Foundation (" + selectedPart.getTopContainer().getNumberOfSolarPanels() + " Solar Panels)";
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
            if (selectedPart instanceof SolarPanel) {
                s += ", \"Panel\": \"" + selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1) + "\"";
            } else if (selectedPart instanceof Rack) {
                s += ", \"Rack\": \"" + selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1) + "\"";
            } else if (selectedPart instanceof Foundation) {
                s += ", \"Foundation\": \"" + selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1) + "\"";
            } else if (selectedPart.getTopContainer() != null) {
                s += ", \"Foundation\": \"" + selectedPart.getTopContainer().toString().substring(0, selectedPart.getTopContainer().toString().indexOf(')') + 1) + "\"";
            }
        } else {
            s += ", \"Panel\": \"All\"";
        }
        final String name = "Solar";
        final List<Double> data = graph.getData(name);
        s += ", \"" + name + "\": {";
        s += "\"Monthly\": [";
        if (data != null) {
            for (final Double x : data) {
                s += Graph.ENERGY_FORMAT.format(x) + ",";
            }
            s = s.substring(0, s.length() - 1);
        }
        s += "]\n";
        s += ", \"Total\": " + Graph.ENERGY_FORMAT.format(getResult(name));
        s += "}";
        s += "}";
        return s;
    }

}