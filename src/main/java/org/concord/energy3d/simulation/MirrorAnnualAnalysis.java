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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.concord.energy3d.gui.CspStationDailyEnergyGraph;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.ClipImage;
import org.concord.energy3d.util.Util;

/**
 * For fast feedback, only 12 days are calculated.
 * 
 * @author Charles Xie
 * 
 */
public class MirrorAnnualAnalysis extends Analysis {

	final static int[] MONTHS = { JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER };

	public MirrorAnnualAnalysis() {
		super();
		graph = new PartEnergyAnnualGraph();
		graph.setPreferredSize(new Dimension(600, 400));
		graph.setBackground(Color.WHITE);
	}

	private void runAnalysis(final JDialog parent) {
		graph.info = "Calculating...";
		graph.repaint();
		super.runAnalysis(new Runnable() {
			@Override
			public void run() {
				final EnergyPanel e = EnergyPanel.getInstance();
				Calendar today;
				for (final int m : MONTHS) {
					if (!analysisStopped) {
						final Calendar c = Heliodon.getInstance().getCalender();
						c.set(Calendar.MONTH, m);
						today = (Calendar) c.clone();
						Scene.getInstance().updateMirrors();
						final Throwable t = compute();
						if (t != null) {
							stopAnalysis();
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									Util.reportError(t);
								}
							});
							break;
						}
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Foundation) { // synchronize with daily graph
							CspStationDailyEnergyGraph g = e.getCspStationDailyEnergyGraph();
							if (g.hasGraph()) {
								g.setCalendar(today);
								g.updateGraph();
							}
						}
						final Calendar today2 = today;
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								e.getDateSpinner().setValue(c.getTime());
								if (selectedPart instanceof Foundation) {
									CspStationDailyEnergyGraph g = e.getCspStationDailyEnergyGraph();
									e.getCspStationTabbedPane().setSelectedComponent(g);
									if (!g.hasGraph()) {
										g.setCalendar(today2);
										g.addGraph((Foundation) selectedPart);
									}
								}
							}
						});
					}
				}
				EventQueue.invokeLater(new Runnable() {

					@Override
					public void run() {
						onCompletion();
						if (Heliodon.getInstance().getCalender().get(Calendar.MONTH) != Calendar.DECEMBER)
							return; // annual calculation aborted
						String current = Graph.TWO_DECIMALS.format(getResult("Solar"));
						String previousRuns = "";
						Map<String, Double> recordedResults = getRecordedResults("Solar");
						int n = recordedResults.size();
						if (n > 0) {
							Object[] keys = recordedResults.keySet().toArray();
							for (int i = n - 1; i >= 0; i--) {
								previousRuns += keys[i] + " : " + Graph.TWO_DECIMALS.format(recordedResults.get(keys[i]) * 365.0 / 12.0) + " kWh<br>";
							}
						}
						JOptionPane.showMessageDialog(parent, "<html>The calculated annual output is <b>" + current + " kWh</b>." + (previousRuns.equals("") ? "" : "<br>For details, look at the graph.<br><br><hr>Results from all previously recorded tests:<br>" + previousRuns) + "</html>", "Annual Solar Panel Output", JOptionPane.INFORMATION_MESSAGE);
					}

				});
			}
		});
	}

	@Override
	public void updateGraph() {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart != null) {
			if (selectedPart instanceof Mirror) {
				final Mirror m = (Mirror) selectedPart;
				graph.addData("Solar", m.getSolarPotentialToday() * m.getReflectivity());
			} else if (selectedPart instanceof Foundation) {
				double output = 0;
				for (HousePart p : Scene.getInstance().getParts()) {
					if (p instanceof Mirror && p.getTopContainer() == selectedPart) {
						final Mirror m = (Mirror) p;
						output += m.getSolarPotentialToday() * m.getReflectivity();
					}
				}
				graph.addData("Solar", output);
			} else if (selectedPart.getTopContainer() instanceof Foundation) {
				double output = 0;
				for (HousePart p : Scene.getInstance().getParts()) {
					if (p instanceof Mirror && p.getTopContainer() == selectedPart.getTopContainer()) {
						final Mirror m = (Mirror) p;
						output += m.getSolarPotentialToday() * m.getReflectivity();
					}
				}
				graph.addData("Solar", output);
			}
		} else {
			double output = 0;
			for (HousePart p : Scene.getInstance().getParts()) {
				if (p instanceof Mirror) {
					final Mirror m = (Mirror) p;
					output += m.getSolarPotentialToday() * m.getReflectivity();
				}
			}
			graph.addData("Solar", output);
		}
		graph.repaint();

	}

	public void show() {

		HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		String s = null;
		int cost = -1;
		String title = "Annual Yield of All Mirrors";
		if (selectedPart != null) {
			if (selectedPart instanceof Mirror) {
				cost = Cost.getInstance().getPartCost(selectedPart);
				s = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
				title = "Annual Yield";
			} else if (selectedPart instanceof Foundation || selectedPart.getTopContainer() instanceof Foundation) {
				title = "Annual Yield of Selected Platform";
			}
		}
		final JDialog dialog = new JDialog(MainFrame.getInstance(), s == null ? title : title + ": " + s + " (Cost: $" + cost + ")", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		graph.parent = dialog;

		final JMenuBar menuBar = new JMenuBar();
		dialog.setJMenuBar(menuBar);

		final JMenuItem miClear = new JMenuItem("Clear Previous Results");
		final JMenuItem miView = new JMenuItem("View Raw Data...");
		final JMenuItem miCopyImage = new JMenuItem("Copy Image");

		final JMenu menu = new JMenu("Options");
		menu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				miClear.setEnabled(graph.hasRecords());
				miView.setEnabled(graph.hasData());
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});
		menuBar.add(menu);

		miClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final int i = JOptionPane.showConfirmDialog(dialog, "Are you sure that you want to clear all the previous results\nrelated to the selected object?", "Confirmation", JOptionPane.YES_NO_OPTION);
				if (i != JOptionPane.YES_OPTION)
					return;
				graph.clearRecords();
				graph.repaint();
				TimeSeriesLogger.getInstance().logClearGraphData(graph.getClass().getSimpleName());
			}
		});
		menu.add(miClear);

		miView.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				DataViewer.viewRawData(dialog, graph, true);
			}
		});
		menu.add(miView);

		miCopyImage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ClipImage().copyImageToClipboard(graph);
			}
		});
		menu.add(miCopyImage);

		final JMenu showRunsMenu = new JMenu("Runs");
		showRunsMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				showRunsMenu.removeAll();
				if (!AnnualGraph.records.isEmpty()) {
					JMenuItem mi = new JMenuItem("Show All");
					mi.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							for (Results r : AnnualGraph.records)
								graph.hideRun(r.getID(), false);
							graph.repaint();
							TimeSeriesLogger.getInstance().logShowRun(graph.getClass().getSimpleName(), "All", true);
						}
					});
					showRunsMenu.add(mi);
					mi = new JMenuItem("Hide All");
					mi.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							for (Results r : AnnualGraph.records)
								graph.hideRun(r.getID(), true);
							graph.repaint();
							TimeSeriesLogger.getInstance().logShowRun(graph.getClass().getSimpleName(), "All", false);
						}
					});
					showRunsMenu.add(mi);
					showRunsMenu.addSeparator();
					Map<String, Double> recordedResults = getRecordedResults("Net");
					for (final Results r : AnnualGraph.records) {
						String key = r.getID() + (r.getFileName() == null ? "" : " (file: " + r.getFileName() + ")");
						Double result = recordedResults.get(key);
						final JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(r.getID() + ":" + r.getFileName() + (result == null ? "" : " - " + Math.round(recordedResults.get(key) * 365.0 / 12.0) + " kWh"), !graph.isRunHidden(r.getID()));
						cbmi.addItemListener(new ItemListener() {
							@Override
							public void itemStateChanged(final ItemEvent e) {
								graph.hideRun(r.getID(), !cbmi.isSelected());
								graph.repaint();
								TimeSeriesLogger.getInstance().logShowRun(graph.getClass().getSimpleName(), "" + r.getID(), cbmi.isSelected());
							}
						});
						showRunsMenu.add(cbmi);
					}
				}
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});
		menuBar.add(showRunsMenu);

		final JPanel contentPane = new JPanel(new BorderLayout());
		dialog.setContentPane(contentPane);

		final JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		contentPane.add(panel, BorderLayout.CENTER);

		panel.add(graph, BorderLayout.CENTER);

		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		runButton = new JButton("Run");
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				runButton.setEnabled(false);
				runAnalysis(dialog);
			}
		});
		buttonPanel.add(runButton);

		JButton button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				stopAnalysis();
				if (graph.hasData()) {
					final Object[] options = { "Yes", "No", "Cancel" };
					int i = JOptionPane.showOptionDialog(dialog, "Do you want to keep the results of this run?", "Confirmation", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
					if (i == JOptionPane.CANCEL_OPTION)
						return;
					if (i == JOptionPane.YES_OPTION)
						graph.keepResults();
				}
				windowLocation.setLocation(dialog.getLocationOnScreen());
				dialog.dispose();
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
		if (windowLocation.x > 0 && windowLocation.y > 0)
			dialog.setLocation(windowLocation);
		else
			dialog.setLocationRelativeTo(MainFrame.getInstance());
		dialog.setVisible(true);

	}

	@Override
	public String toJson() {
		String s = "{\"Months\": " + getNumberOfDataPoints();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart != null) {
			if (selectedPart instanceof Mirror) {
				s += ", \"Mirror\": \"" + selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1) + "\"";
			} else if (selectedPart instanceof Foundation) {
				s += ", \"Platform\": \"" + selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1) + "\"";
			} else if (selectedPart.getTopContainer() instanceof Foundation) {
				s += ", \"Platform\": \"" + selectedPart.getTopContainer().toString().substring(0, selectedPart.getTopContainer().toString().indexOf(')') + 1) + "\"";
			}
		} else {
			s += ", \"Mirror\": \"All\"";
		}
		String name = "Solar";
		List<Double> data = graph.getData(name);
		s += ", \"" + name + "\": {";
		s += "\"Monthly\": [";
		for (Double x : data) {
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
