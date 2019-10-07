package org.concord.energy3d.simulation;

import static java.util.Calendar.APRIL;
import static java.util.Calendar.AUGUST;
import static java.util.Calendar.DECEMBER;
import static java.util.Calendar.FEBRUARY;
import static java.util.Calendar.JANUARY;
import static java.util.Calendar.JULY;
import static java.util.Calendar.JUNE;
import static java.util.Calendar.MARCH;
import static java.util.Calendar.MAY;
import static java.util.Calendar.NOVEMBER;
import static java.util.Calendar.OCTOBER;
import static java.util.Calendar.SEPTEMBER;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.concord.energy3d.MainApplication;
import org.concord.energy3d.agents.AnalysisEvent;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.model.Building;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 */
public abstract class Analysis {

    public final static int[] MONTHS = {JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER};

    Graph graph;
    volatile boolean analysisStopped;
    private static Point windowLocation = new Point();
    private JButton runButton;

    public double getResult(final String name) {
        return graph.getSum(name);
    }

    public Map<String, Double> getRecordedResults(final String name) {
        final Map<String, Double> recordedResults = new TreeMap<>();
        for (final Results r : graph.getRecords()) {
            final Map<String, List<Double>> x = r.getData();
            final List<Double> list = x.get(name);
            if (list != null) {
                double sum = 0;
                for (final Double d : list) {
                    sum += d;
                }
                recordedResults.put(r.getID() + (r.getFileName() == null ? "" : " (file: " + r.getFileName() + ")"), sum);
            }
        }
        return recordedResults;
    }

    public int getNumberOfDataPoints() {
        return graph.getLength();
    }

    void stopAnalysis() {
        analysisStopped = true;
        EnergyPanel.getInstance().cancel();
    }

    void reportResults(List<double[]> storedResults, double annualOutput, int lifespan, double roi, JDialog parent) {
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
            final JDialog dialog = optionPane.createDialog(parent, "Annual Electricity Output and Return on Investment");
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
                JOptionPane.showMessageDialog(parent, "<html>" + (n + 1) + " data points copied to system clipboard.", "Confirmation", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            StringBuilder report = new StringBuilder("<html>");
            report.append("The calculated annual output is <b>" + Graph.TWO_DECIMALS.format(annualOutput) + " kWh</b>.");
            report.append("<br>Based on this prediction, the ROI over " + lifespan + " years is <b>" + Graph.TWO_DECIMALS.format(roi) + "%</b>.");
            report.append("</html>");
            JOptionPane.showMessageDialog(parent, report.toString(), "Annual Electricity Output and Return on Investment", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    void viewFullHistory(List<double[]> storedResults) {
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

    // return the exception if unsuccessful
    Throwable compute() {
        EventQueue.invokeLater(() -> graph.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)));
        try {
            EnergyPanel.getInstance().computeNow();
        } catch (final Throwable e) {
            return e;
        } finally {
            EventQueue.invokeLater(() -> graph.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)));
        }
        updateGraph();
        return null;
    }

    public abstract void updateGraph();

    public Graph getGraph() {
        return graph;
    }

    void onCompletion() {
        MainApplication.addEvent(new AnalysisEvent(Scene.getURL(), System.currentTimeMillis(), getClass().getSimpleName(), graph.data));
        TimeSeriesLogger.getInstance().logAnalysis(this);
        EnergyPanel.getInstance().progress(0);
        runButton.setEnabled(true);
        EnergyPanel.getInstance().disableDateSpinner(false);
        SceneManager.setExecuteAllTask(true);
    }

    void onStart() {
        EnergyPanel.getInstance().disableDateSpinner(true);
        SceneManager.getInstance().setHeatFluxDaily(true);
        Util.selectSilently(MainPanel.getInstance().getEnergyButton(), true);
        SceneManager.getInstance().setSolarHeatMapWithoutUpdate(true);
        SceneManager.getInstance().setHeatFluxVectorsVisible(true);
        SceneManager.getInstance().getSolarLand().setVisible(Scene.getInstance().getSolarMapForLand());
        graph.clearData();
        SceneManager.setExecuteAllTask(false);
        Scene.getInstance().redrawAllNow();
    }

    static boolean isBuildingAcceptable(final Foundation foundation) {
        return new Building(foundation).areWallsAcceptable();
    }

    public abstract String toJson();

    abstract void runAnalysis(final JDialog parent);

    JDialog createDialog(final String title) {

        final JDialog dialog = new JDialog(MainFrame.getInstance(), title, true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        graph.parent = dialog;

        final JPanel contentPane = new JPanel(new BorderLayout());
        dialog.setContentPane(contentPane);

        final JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        contentPane.add(panel, BorderLayout.CENTER);

        panel.add(graph, BorderLayout.CENTER);

        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        runButton = new JButton("Run");
        runButton.addActionListener(e -> {
            runButton.setEnabled(false);
            analysisStopped = false;
            runAnalysis(dialog);
        });
        buttonPanel.add(runButton);

        JButton button = new JButton("Close");
        button.addActionListener(e -> {
            stopAnalysis();
            if (graph.hasData()) {
                final Object[] options = {"Yes", "No", "Cancel"};
                final int i = JOptionPane.showOptionDialog(dialog, "Do you want to keep the results of this run in the graph?", "Confirmation", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
                if (i == JOptionPane.CANCEL_OPTION) {
                    return;
                }
                if (i == JOptionPane.YES_OPTION) {
                    graph.keepResults();
                }
            }
            windowLocation.setLocation(dialog.getLocationOnScreen());
            dialog.dispose();
        });
        buttonPanel.add(button);

        button = new JButton("View Full History");
        button.addActionListener(e -> {
            if (this instanceof PvAnnualAnalysis) {
                viewFullHistory(PvAnnualAnalysis.storedResults);
            } else if (this instanceof HeliostatAnnualAnalysis) {
                viewFullHistory(HeliostatAnnualAnalysis.storedResults);
            } else if (this instanceof FresnelReflectorAnnualAnalysis) {
                viewFullHistory(FresnelReflectorAnnualAnalysis.storedResults);
            } else if (this instanceof ParabolicTroughAnnualAnalysis) {
                viewFullHistory(ParabolicTroughAnnualAnalysis.storedResults);
            } else if (this instanceof ParabolicDishAnnualAnalysis) {
                viewFullHistory(ParabolicDishAnnualAnalysis.storedResults);
            } else {
                JOptionPane.showMessageDialog(MainFrame.getInstance(), "Under construction...", "Full History", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        buttonPanel.add(button);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                stopAnalysis();
                windowLocation.setLocation(dialog.getLocationOnScreen());
                dialog.dispose();
            }
        });

        dialog.pack();
        if (windowLocation.x > 0 && windowLocation.y > 0) {
            dialog.setLocation(windowLocation);
        } else {
            dialog.setLocationRelativeTo(MainFrame.getInstance());
        }

        return dialog;

    }

    JMenu createTypesMenu() {

        final JMenu menu = new JMenu("Types");
        menu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(final MenuEvent e) {
                menu.removeAll();
                final Set<String> dataNames = graph.getDataNames();
                if (!dataNames.isEmpty()) {
                    JMenuItem mi = new JMenuItem("Show All");
                    mi.addActionListener(e1 -> {
                        for (final String name : dataNames) {
                            graph.hideData(name, false);
                        }
                        graph.repaint();
                        TimeSeriesLogger.getInstance().logShowCurve(graph.getClass().getSimpleName(), "All", true);
                    });
                    menu.add(mi);
                    mi = new JMenuItem("Hide All");
                    mi.addActionListener(e1 -> {
                        for (final String name : dataNames) {
                            graph.hideData(name, true);
                        }
                        graph.repaint();
                        TimeSeriesLogger.getInstance().logShowCurve(graph.getClass().getSimpleName(), "All", false);
                    });
                    menu.add(mi);
                    menu.addSeparator();
                    for (final String name : dataNames) {
                        final JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(name, !graph.isDataHidden(name));
                        cbmi.addItemListener(e1 -> {
                            graph.hideData(name, !cbmi.isSelected());
                            graph.repaint();
                            TimeSeriesLogger.getInstance().logShowCurve(graph.getClass().getSimpleName(), name, cbmi.isSelected());
                        });
                        menu.add(cbmi);
                    }
                }
            }

            @Override
            public void menuDeselected(final MenuEvent e) {
            }

            @Override
            public void menuCanceled(final MenuEvent e) {
            }
        });

        return menu;

    }

}