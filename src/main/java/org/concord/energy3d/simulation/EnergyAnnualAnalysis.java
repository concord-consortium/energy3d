package org.concord.energy3d.simulation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.swing.JDialog;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.BugReporter;

/**
 * This calculates and visualizes the seasonal trend and the yearly sum of all energy items for any selected part or building.
 *
 * For fast feedback, only 12 days are calculated.
 *
 * @author Charles Xie
 *
 */
public class EnergyAnnualAnalysis extends AnnualAnalysis {

	public EnergyAnnualAnalysis() {
		super();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		graph = selectedPart instanceof Foundation ? new BuildingEnergyAnnualGraph() : new PartEnergyAnnualGraph();
		graph.setPreferredSize(new Dimension(600, 400));
		graph.setBackground(Color.WHITE);
	}

	@Override
	void runAnalysis(final JDialog parent) {
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
									BugReporter.report(t);
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
							final Map<String, Double> recordedResults = getRecordedResults("Net");
							final int n = recordedResults.size();
							if (n > 0) {
								String previousRuns = "";
								final Object[] keys = recordedResults.keySet().toArray();
								for (int i = n - 1; i >= 0; i--) {
									previousRuns += keys[i] + " : " + Graph.TWO_DECIMALS.format(recordedResults.get(keys[i]) * 365.0 / 12.0) + " kWh<br>";
								}
								final Object[] options = new Object[] { "OK", "Copy Data" };
								final String msg = "<html>The calculated annual net energy is <b>" + net + " kWh</b>.<br><hr>Results from previously recorded tests:<br>" + previousRuns + "</html>";
								final JOptionPane optionPane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, options, options[0]);
								final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Annual Net Energy");
								dialog.setVisible(true);
								final Object choice = optionPane.getValue();
								if (choice == options[1]) {
									String output = "";
									for (int i = 0; i < n; i++) {
										output += Graph.TWO_DECIMALS.format(recordedResults.get(keys[i]) * 365.0 / 12.0) + "\n";
									}
									output += Graph.TWO_DECIMALS.format(getResult("Net"));
									final Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
									clpbrd.setContents(new StringSelection(output), null);
									JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>" + (n + 1) + " data points copied to system clipboard.<br><hr>" + output, "Confirmation", JOptionPane.INFORMATION_MESSAGE);
								}
							} else {
								JOptionPane.showMessageDialog(parent, "<html>The calculated annual net energy is <b>" + net + " kWh</b>.</html>", "Annual Net Energy", JOptionPane.INFORMATION_MESSAGE);
							}
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
		} else if (selectedPart instanceof Rack) {
			graph.addData("Solar", ((Rack) selectedPart).getYieldToday());
		}
		graph.repaint();
	}

	public void show(final String title) {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		String s = null;
		int cost = -1;
		if (selectedPart != null) {
			cost = (int) BuildingCost.getPartCost(selectedPart);
			if (graph.instrumentType == Graph.SENSOR) {
				SceneManager.getInstance().setSelectedPart(null);
			} else {
				s = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
				if (selectedPart instanceof Foundation) {
					cost = (int) BuildingCost.getInstance().getCostByFoundation((Foundation) selectedPart);
					s = s.replaceAll("Foundation", "Building");
					if (selectedPart.getChildren().isEmpty()) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no building on this foundation.", "No Building", JOptionPane.WARNING_MESSAGE);
						return;
					}
					if (!isBuildingComplete((Foundation) selectedPart)) {
						if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "The selected building has not been completed.\nAre you sure to continue?", "Incomplete Building", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
							return;
						}
					}
				} else if (selectedPart instanceof Tree) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Energy analysis is not applicable to a tree.", "Not Applicable", JOptionPane.WARNING_MESSAGE);
					return;
				}
			}
		}
		final JDialog dialog = createDialog(s == null ? title : title + ": " + s + " (Construction cost: $" + cost + ")");
		final JMenuBar menuBar = new JMenuBar();
		dialog.setJMenuBar(menuBar);
		menuBar.add(createOptionsMenu(dialog, null, false));
		menuBar.add(createTypesMenu());
		menuBar.add(createRunsMenu());
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
