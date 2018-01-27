package org.concord.energy3d.simulation;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
 *
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

		final JRadioButtonMenuItem miLine = new JRadioButtonMenuItem("Line");
		miLine.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					graph.setGraphType(Graph.LINE_CHART);
					graph.repaint();
				}
			}
		});
		chartMenu.add(miLine);
		chartGroup.add(miLine);
		miLine.setSelected(graph.getGraphType() == Graph.LINE_CHART);

		final JRadioButtonMenuItem miArea = new JRadioButtonMenuItem("Area");
		miArea.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					graph.setGraphType(Graph.AREA_CHART);
					graph.repaint();
				}
			}
		});
		chartMenu.add(miArea);
		chartGroup.add(miArea);
		miArea.setSelected(graph.getGraphType() == Graph.AREA_CHART);

		miClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final int i = JOptionPane.showConfirmDialog(dialog, "Are you sure that you want to clear all the previous results\nrelated to the selected object?", "Confirmation", JOptionPane.YES_NO_OPTION);
				if (i != JOptionPane.YES_OPTION) {
					return;
				}
				graph.clearRecords();
				graph.repaint();
				TimeSeriesLogger.getInstance().logClearGraphData(graph.getClass().getSimpleName());
			}
		});
		menu.add(miClear);

		miView.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (selectedParts == null) {
					DataViewer.viewRawData(dialog, graph, selectAll);
				} else {
					DataViewer.viewRawData(dialog, graph, selectedParts);
				}
			}
		});
		menu.add(miView);

		final JMenuItem miCopyImage = new JMenuItem("Copy Image");
		miCopyImage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				new ClipImage().copyImageToClipboard(graph);
			}
		});
		menu.add(miCopyImage);

		if (exportStoredResults) {
			miExportStoredResults.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final double[][] solarResults = Scene.getInstance().getSolarResults();
					if (solarResults != null) {
						String s = "";
						for (int i = 0; i < solarResults.length; i++) {
							s += "\"" + AnnualGraph.THREE_LETTER_MONTH[i] + "\": \"";
							for (int j = 0; j < solarResults[i].length; j++) {
								s += EnergyPanel.FIVE_DECIMALS.format(solarResults[i][j]).replaceAll(",", "") + " ";
							}
							s = s.trim() + "\",\n";
						}
						s = s.substring(0, s.length() - 1);
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(s), null);
					}
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
					mi.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							for (final Results r : AnnualGraph.records) {
								graph.hideRun(r.getID(), false);
							}
							graph.repaint();
							TimeSeriesLogger.getInstance().logShowRun(graph.getClass().getSimpleName(), "All", true);
						}
					});
					menu.add(mi);
					mi = new JMenuItem("Hide All");
					mi.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							for (final Results r : AnnualGraph.records) {
								graph.hideRun(r.getID(), true);
							}
							graph.repaint();
							TimeSeriesLogger.getInstance().logShowRun(graph.getClass().getSimpleName(), "All", false);
						}
					});
					menu.add(mi);
					menu.addSeparator();
					final Map<String, Double> recordedResults = getRecordedResults("Net");
					for (final Results r : AnnualGraph.records) {
						final String key = r.getID() + (r.getFileName() == null ? "" : " (file: " + r.getFileName() + ")");
						final Double result = recordedResults.get(key);
						final JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(r.getID() + ":" + r.getFileName() + (result == null ? "" : " - " + Math.round(recordedResults.get(key) * 365.0 / 12.0) + " kWh"), !graph.isRunHidden(r.getID()));
						cbmi.addItemListener(new ItemListener() {
							@Override
							public void itemStateChanged(final ItemEvent e) {
								graph.hideRun(r.getID(), !cbmi.isSelected());
								graph.repaint();
								TimeSeriesLogger.getInstance().logShowRun(graph.getClass().getSimpleName(), "" + r.getID(), cbmi.isSelected());
							}
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
