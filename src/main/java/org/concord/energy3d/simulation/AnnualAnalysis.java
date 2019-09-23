package org.concord.energy3d.simulation;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.ClipImage;

/**
 * @author Charles Xie
 */
abstract class AnnualAnalysis extends Analysis {

    JMenu createOptionsMenu(final JDialog dialog, final List<HousePart> selectedParts, final boolean selectAll, final boolean exportStoredResults) {

        final JMenuItem miClear = new JMenuItem("Clear Previous Results");
        final JMenuItem miView = new JMenuItem("View Raw Data...");
        final JMenuItem miExportStoredResults = new JMenuItem("Export Stored Hourly Results");

        final JMenu menu = new JMenu("Options");
        menu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(final MenuEvent e) {
                miClear.setEnabled(graph.hasRecords());
                miView.setEnabled(graph.hasData());
                miExportStoredResults.setEnabled(Scene.getInstance().getSolarResults() != null);
            }

            @Override
            public void menuDeselected(final MenuEvent e) {
            }

            @Override
            public void menuCanceled(final MenuEvent e) {
            }
        });

        final JMenu chartMenu = new JMenu("Chart");
        final ButtonGroup chartGroup = new ButtonGroup();
        menu.add(chartMenu);

        final JRadioButtonMenuItem miBar = new JRadioButtonMenuItem("Bar");
        miBar.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                graph.setGraphType(Graph.BAR_CHART);
                graph.repaint();
            }
        });
        chartMenu.add(miBar);
        chartGroup.add(miBar);
        miBar.setSelected(graph.getGraphType() == Graph.BAR_CHART);

        final JRadioButtonMenuItem miLine = new JRadioButtonMenuItem("Line");
        miLine.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                graph.setGraphType(Graph.LINE_CHART);
                graph.repaint();
            }
        });
        chartMenu.add(miLine);
        chartGroup.add(miLine);
        miLine.setSelected(graph.getGraphType() == Graph.LINE_CHART);

        final JRadioButtonMenuItem miArea = new JRadioButtonMenuItem("Area");
        miArea.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                graph.setGraphType(Graph.AREA_CHART);
                graph.repaint();
            }
        });
        chartMenu.add(miArea);
        chartGroup.add(miArea);
        miArea.setSelected(graph.getGraphType() == Graph.AREA_CHART);

        miClear.addActionListener(e -> {
            final int i = JOptionPane.showConfirmDialog(dialog, "Are you sure that you want to clear all the previous results\nrelated to the selected object?",
                    "Confirmation", JOptionPane.YES_NO_OPTION);
            if (i != JOptionPane.YES_OPTION) {
                return;
            }
            graph.clearRecords();
            graph.repaint();
            TimeSeriesLogger.getInstance().logClearGraphData(graph.getClass().getSimpleName());
        });
        menu.add(miClear);

        miView.addActionListener(e -> {
            if (selectedParts == null) {
                DataViewer.viewRawData(dialog, graph, selectAll);
            } else {
                DataViewer.viewRawData(dialog, graph, selectedParts);
            }
        });
        menu.add(miView);

        final JMenuItem miCopyImage = new JMenuItem("Copy Image");
        miCopyImage.addActionListener(e -> new ClipImage().copyImageToClipboard(graph));
        menu.add(miCopyImage);

        if (exportStoredResults) {
            miExportStoredResults.addActionListener(e -> {
                final double[][] solarResults = Scene.getInstance().getSolarResults();
                if (solarResults != null) {
                    double sum = 0;
                    final double scale = 1; // Hack to fix the results in case we make a mistake that can be quickly remedied
                    for (int i = 0; i < solarResults.length; i++) {
                        for (int j = 0; j < solarResults[i].length; j++) {
                            solarResults[i][j] *= scale;
                            sum += solarResults[i][j];
                        }
                    }
                    sum *= 365.0 / 12.0;
                    StringBuilder s = new StringBuilder();
                    for (int i = 0; i < solarResults.length; i++) {
                        s.append("\"").append(AnnualGraph.THREE_LETTER_MONTH[i]).append("\": \"");
                        for (int j = 0; j < solarResults[i].length; j++) {
                            s.append(EnergyPanel.FIVE_DECIMALS.format(solarResults[i][j]).replaceAll(",", "")).append(" ");
                        }
                        s = new StringBuilder(s.toString().trim() + "\",\n\t");
                    }
                    s = new StringBuilder(s.substring(0, s.length() - 1));
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s.toString()), null);
                    JOptionPane.showMessageDialog(dialog, "A total of " + EnergyPanel.TWO_DECIMALS.format(sum) + " KWh was copied to the clipboard.", "Export", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            menu.add(miExportStoredResults);
        }

        return menu;

    }

    JMenu createRunsMenu() {

        final JMenu menu = new JMenu("Runs");
        menu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(final MenuEvent e) {
                menu.removeAll();
                if (!AnnualGraph.records.isEmpty()) {
                    JMenuItem mi = new JMenuItem("Show All");
                    mi.addActionListener(e1 -> {
                        for (final Results r : AnnualGraph.records) {
                            graph.hideRun(r.getID(), false);
                        }
                        graph.repaint();
                        TimeSeriesLogger.getInstance().logShowRun(graph.getClass().getSimpleName(), "All", true);
                    });
                    menu.add(mi);
                    mi = new JMenuItem("Hide All");
                    mi.addActionListener(e1 -> {
                        for (final Results r : AnnualGraph.records) {
                            graph.hideRun(r.getID(), true);
                        }
                        graph.repaint();
                        TimeSeriesLogger.getInstance().logShowRun(graph.getClass().getSimpleName(), "All", false);
                    });
                    menu.add(mi);
                    menu.addSeparator();
                    final Map<String, Double> recordedResults = getRecordedResults("Net");
                    for (final Results r : AnnualGraph.records) {
                        final String key = r.getID() + (r.getFileName() == null ? "" : " (file: " + r.getFileName() + ")");
                        final Double result = recordedResults.get(key);
                        final JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(r.getID() + ":" + r.getFileName() + (result == null ? "" : " - "
                                + Math.round(recordedResults.get(key) * 365.0 / 12.0) + " kWh"), !graph.isRunHidden(r.getID()));
                        cbmi.addItemListener(e1 -> {
                            graph.hideRun(r.getID(), !cbmi.isSelected());
                            graph.repaint();
                            TimeSeriesLogger.getInstance().logShowRun(graph.getClass().getSimpleName(), "" + r.getID(), cbmi.isSelected());
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