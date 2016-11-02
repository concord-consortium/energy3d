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
import java.util.Set;
import java.util.concurrent.Callable;

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

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.ClipImage;
import org.concord.energy3d.util.Util;

/**
 * This calculates and visualizes the seasonal trend and the yearly sum of all energy items for any selected part or building.
 *
 * For fast feedback, only 12 days are calculated.
 *
 * @author Charles Xie
 *
 */
public class EnergyAnnualAnalysis extends Analysis {

	final static int[] MONTHS = { JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER };

	public EnergyAnnualAnalysis() {
		super();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		graph = selectedPart instanceof Foundation ? new BuildingEnergyAnnualGraph() : new PartEnergyAnnualGraph();
		graph.setPreferredSize(new Dimension(600, 400));
		graph.setBackground(Color.WHITE);
	}

	private void runAnalysis(final JDialog parent) {
		graph.info = "Calculating...";
		graph.repaint();
		onStart();
		final EnergyPanel e = EnergyPanel.getInstance();
		for (final int m : MONTHS) {
			SceneManager.getTaskManager().update(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					Calendar today;
					if (!analysisStopped) {
						final Calendar c = Heliodon.getInstance().getCalendar();
						c.set(Calendar.MONTH, m);
						today = (Calendar) c.clone();
						final Throwable t = compute();
						if (t != null) {
							stopAnalysis();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									Util.reportError(t);
								}
							});
						}
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Foundation) { // synchronize with daily graph
							if (e.getBuildingDailyEnergyGraph().hasGraph()) {
								e.getBuildingDailyEnergyGraph().setCalendar(today);
								e.getBuildingDailyEnergyGraph().updateGraph();
							}
						}
						final Calendar today2 = today;
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								EnergyPanel.getInstance().getDateSpinner().setValue(c.getTime());
								if (selectedPart instanceof Foundation) {
									e.getBuildingTabbedPane().setSelectedComponent(e.getBuildingDailyEnergyGraph());
									if (!e.getBuildingDailyEnergyGraph().hasGraph()) {
										e.getBuildingDailyEnergyGraph().setCalendar(today2);
										e.getBuildingDailyEnergyGraph().addGraph((Foundation) selectedPart);
									}
								}
							}
						});
					}
					return null;
				}
			});
		}
		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						onCompletion();
						if (graph instanceof BuildingEnergyAnnualGraph) {
							if (Heliodon.getInstance().getCalendar().get(Calendar.MONTH) != Calendar.DECEMBER) {
								return; // annual calculation aborted
							}
							final int net = (int) Math.round(getResult("Net"));
							String previousRuns = "";
							final Map<String, Double> recordedResults = getRecordedResults("Net");
							final int n = recordedResults.size();
							if (n > 0) {
								final Object[] keys = recordedResults.keySet().toArray();
								for (int i = n - 1; i >= 0; i--) {
									previousRuns += keys[i] + " : " + Math.round(recordedResults.get(keys[i]) * 365.0 / 12.0) + " kWh<br>";
								}
							}
							JOptionPane.showMessageDialog(parent, "<html>The calculated annual net energy is <b>" + net + " kWh</b>." + (previousRuns.equals("") ? "" : "<br>For details, look at the graph.<br><br><hr>Results from all previously recorded tests:<br>" + previousRuns) + "</html>", "Annual Net Energy", JOptionPane.INFORMATION_MESSAGE);
						}
					}
				});
				return null;
			}
		});
	}

	@Override
	public void updateGraph() {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart instanceof Foundation) {
			if (graph instanceof BuildingEnergyAnnualGraph) {
				final Foundation selectedBuilding = (Foundation) selectedPart;
				final double window = selectedBuilding.getPassiveSolarToday();
				final double solarPanel = selectedBuilding.getPhotovoltaicToday();
				final double heater = selectedBuilding.getHeatingToday();
				final double ac = selectedBuilding.getCoolingToday();
				final double net = selectedBuilding.getTotalEnergyToday();
				graph.addData("Windows", window);
				graph.addData("Solar Panels", solarPanel);
				graph.addData("Heater", heater);
				graph.addData("AC", ac);
				graph.addData("Net", net);
			} else {
				graph.addData("Solar", selectedPart.getSolarPotentialToday());
			}
		} else if (selectedPart instanceof Window) {
			final Window window = (Window) selectedPart;
			final double solar = selectedPart.getSolarPotentialToday() * window.getSolarHeatGainCoefficient();
			graph.addData("Solar", solar);
			final double[] loss = selectedPart.getHeatLoss();
			double sum = 0;
			for (final double x : loss) {
				sum += x;
			}
			graph.addData("Heat Gain", -sum);
		} else if (selectedPart instanceof Wall || selectedPart instanceof Roof || selectedPart instanceof Door) {
			final double[] loss = selectedPart.getHeatLoss();
			double sum = 0;
			for (final double x : loss) {
				sum += x;
			}
			graph.addData("Heat Gain", -sum);
		} else if (selectedPart instanceof SolarPanel) {
			graph.addData("Solar", ((SolarPanel) selectedPart).getYieldToday());
		}
		graph.repaint();
	}

	public void show(final String title) {

		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		String s = null;
		int cost = -1;
		if (selectedPart != null) {
			cost = Cost.getInstance().getPartCost(selectedPart);
			if (graph.type == Graph.SENSOR) {
				SceneManager.getInstance().setSelectedPart(null);
			} else {
				s = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
				if (selectedPart instanceof Foundation) {
					cost = Cost.getInstance().getTotalCost();
					s = s.replaceAll("Foundation", "Building");
					if (selectedPart.getChildren().isEmpty()) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no building on this platform.", "No Building", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					if (!isBuildingComplete((Foundation) selectedPart)) {
						if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "The selected building has not been completed.\nAre you sure to continue?", "Incomplete Building", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
							return;
						}
					}
				} else if (selectedPart instanceof Tree) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Energy analysis is not applicable to a tree.", "Not Applicable", JOptionPane.INFORMATION_MESSAGE);
					return;
				}
			}
		}
		final JDialog dialog = new JDialog(MainFrame.getInstance(), s == null ? title : title + ": " + s + " (Construction cost: $" + cost + ")", true);
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
			public void menuSelected(final MenuEvent e) {
				miClear.setEnabled(graph.hasRecords());
				miView.setEnabled(graph.hasData());
			}

			@Override
			public void menuDeselected(final MenuEvent e) {
			}

			@Override
			public void menuCanceled(final MenuEvent e) {
			}
		});
		menuBar.add(menu);

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
				DataViewer.viewRawData(dialog, graph, false);
			}
		});
		menu.add(miView);

		miCopyImage.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				new ClipImage().copyImageToClipboard(graph);
			}
		});
		menu.add(miCopyImage);

		final JMenu showTypeMenu = new JMenu("Types");
		showTypeMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(final MenuEvent e) {
				showTypeMenu.removeAll();
				final Set<String> dataNames = graph.getDataNames();
				if (!dataNames.isEmpty()) {
					JMenuItem mi = new JMenuItem("Show All");
					mi.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							for (final String name : dataNames) {
								graph.hideData(name, false);
							}
							graph.repaint();
							TimeSeriesLogger.getInstance().logShowCurve(graph.getClass().getSimpleName(), "All", true);
						}
					});
					showTypeMenu.add(mi);
					mi = new JMenuItem("Hide All");
					mi.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							for (final String name : dataNames) {
								graph.hideData(name, true);
							}
							graph.repaint();
							TimeSeriesLogger.getInstance().logShowCurve(graph.getClass().getSimpleName(), "All", false);
						}
					});
					showTypeMenu.add(mi);
					showTypeMenu.addSeparator();
					for (final String name : dataNames) {
						final JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(name, !graph.isDataHidden(name));
						cbmi.addItemListener(new ItemListener() {
							@Override
							public void itemStateChanged(final ItemEvent e) {
								graph.hideData(name, !cbmi.isSelected());
								graph.repaint();
								TimeSeriesLogger.getInstance().logShowCurve(graph.getClass().getSimpleName(), name, cbmi.isSelected());
							}
						});
						showTypeMenu.add(cbmi);
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
		menuBar.add(showTypeMenu);

		final JMenu showRunsMenu = new JMenu("Runs");
		showRunsMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(final MenuEvent e) {
				showRunsMenu.removeAll();
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
					showRunsMenu.add(mi);
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
					showRunsMenu.add(mi);
					showRunsMenu.addSeparator();
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
						showRunsMenu.add(cbmi);
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

		final JButton button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				stopAnalysis();
				if (graph.hasData()) {
					final Object[] options = { "Yes", "No", "Cancel" };
					final int i = JOptionPane.showOptionDialog(dialog, "Do you want to keep the results of this run?", "Confirmation", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
					if (i == JOptionPane.CANCEL_OPTION) {
						return;
					}
					if (i == JOptionPane.YES_OPTION) {
						graph.keepResults();
					}
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
		if (windowLocation.x > 0 && windowLocation.y > 0) {
			dialog.setLocation(windowLocation);
		} else {
			dialog.setLocationRelativeTo(MainFrame.getInstance());
		}
		dialog.setVisible(true);

	}

	@Override
	public String toJson() {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		String s = "{\"Months\": " + getNumberOfDataPoints();
		String[] names;
		if (selectedPart instanceof Foundation) {
			s += ", \"Building\": " + selectedPart.getId();
			names = new String[] { "Net", "AC", "Heater", "Windows", "Solar Panels" };
		} else {
			s += ", \"Part\": \"" + selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1) + "\"";
			names = new String[] { "Solar", "Heat Gain" };
		}
		for (final String name : names) {
			final List<Double> data = graph.getData(name);
			if (data == null) {
				continue;
			}
			s += ", \"" + name + "\": {";
			s += "\"Monthly\": [";
			for (final Double x : data) {
				s += Graph.ENERGY_FORMAT.format(x) + ",";
			}
			s = s.substring(0, s.length() - 1);
			s += "]\n";
			s += ", \"Total\": " + Graph.ENERGY_FORMAT.format(getResult(name));
			s += "}";
		}
		s += "}";
		return s;
	}

}
