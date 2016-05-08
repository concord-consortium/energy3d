package org.concord.energy3d.simulation;

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
import java.util.List;
import java.util.Set;

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
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

/**
 * This calculates and visualizes the angular dependence of all energy items for any selected part or building.
 * 
 * @author Charles Xie
 *
 */
public class EnergyAngularAnalysis extends Analysis {

	static int nRotation = 8;

	public EnergyAngularAnalysis() {
		super();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		graph = selectedPart instanceof Foundation ? new BuildingEnergyAngularGraph() : new PartEnergyAngularGraph();
		graph.setPreferredSize(new Dimension(600, 400));
		graph.setBackground(Color.white);
	}

	private void runAnalysis(final JDialog parent) {
		super.runAnalysis(new Runnable() {
			@Override
			public void run() {
				SceneManager.getInstance().setRefreshOnlyMode(true);
				for (int i = 0; i < nRotation; i++) {
					if (!analysisStopped) {
						SceneManager.getInstance().rotateBuilding(2.0 * Math.PI / nRotation, false);
						Scene.getInstance().redrawAllNow();
						final Throwable t = compute();
						if (t != null) {
							stopAnalysis();
							EventQueue.invokeLater(new Runnable() {
								@Override
								public void run() {
									Util.reportError(t);
								}
							});
							break;
						}
						try {
							Thread.sleep(500);
						} catch (final InterruptedException e) {
						}
					}
				}
				SceneManager.getInstance().setRefreshOnlyMode(false);
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						onCompletion();
					}
				});
			}
		});
	}

	@Override
	public void updateGraph() {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart instanceof Foundation) {
			if (graph instanceof BuildingEnergyAngularGraph) {
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
			Window window = (Window) selectedPart;
			final double solar = selectedPart.getSolarPotentialToday() * window.getSolarHeatGainCoefficient();
			graph.addData("Solar", solar);
			final double[] loss = selectedPart.getHeatLoss();
			double sum = 0;
			for (final double x : loss)
				sum += x;
			graph.addData("Heat Gain", -sum);
		} else if (selectedPart instanceof Wall || selectedPart instanceof Roof || selectedPart instanceof Door) {
			final double[] loss = selectedPart.getHeatLoss();
			double sum = 0;
			for (final double x : loss)
				sum += x;
			graph.addData("Heat Gain", -sum);
		} else if (selectedPart instanceof SolarPanel) {
			final SolarPanel solarPanel = (SolarPanel) selectedPart;
			final double solar = solarPanel.getSolarPotentialToday() * solarPanel.getEfficiency();
			graph.addData("Solar", solar);
		}
		graph.repaint();
	}

	public void show(final String title) {

		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		String s = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
		if (selectedPart instanceof Foundation) {
			s = s.replaceAll("Foundation", "Building");
			if (selectedPart.getChildren().isEmpty()) {
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no building on this platform.", "No Building", JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			if (!isBuildingComplete((Foundation) selectedPart)) {
				if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "The selected building has not been completed.\nAre you sure to continue?", "Incomplete Building", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
					return;
			}
		} else if (selectedPart instanceof Tree) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "Energy analysis is not applicable to a tree.", "Not Applicable", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		final JDialog dialog = new JDialog(MainFrame.getInstance(), title + ": " + s + " (Construction cost: $" + Cost.getInstance().getTotalCost() + ")", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		graph.parent = dialog;

		final JMenuBar menuBar = new JMenuBar();
		dialog.setJMenuBar(menuBar);

		final JMenuItem miClear = new JMenuItem("Clear Previous Results");
		final JMenuItem miView = new JMenuItem("View Raw Data...");

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
				DataViewer.viewRawData(dialog, graph);
			}
		});
		menu.add(miView);

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
							for (final String name : dataNames)
								graph.hideData(name, false);
							graph.repaint();
							TimeSeriesLogger.getInstance().logShowCurve(graph.getClass().getSimpleName(), "All", true);
						}
					});
					showTypeMenu.add(mi);
					mi = new JMenuItem("Hide All");
					mi.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							for (final String name : dataNames)
								graph.hideData(name, true);
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
				if (!AngularGraph.records.isEmpty()) {
					JMenuItem mi = new JMenuItem("Show All");
					mi.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							for (final Results r : AngularGraph.records)
								graph.hideRun(r.getID(), false);
							graph.repaint();
							TimeSeriesLogger.getInstance().logShowRun(graph.getClass().getSimpleName(), "All", true);
						}
					});
					showRunsMenu.add(mi);
					mi = new JMenuItem("Hide All");
					mi.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							for (final Results r : AngularGraph.records)
								graph.hideRun(r.getID(), true);
							graph.repaint();
							TimeSeriesLogger.getInstance().logShowRun(graph.getClass().getSimpleName(), "All", false);
						}
					});
					showRunsMenu.add(mi);
					showRunsMenu.addSeparator();
					for (final Results r : AngularGraph.records) {
						final JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(Integer.toString(r.getID()), !graph.isRunHidden(r.getID()));
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
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		String s = "{\"Angles\": " + getNumberOfDataPoints() + ", \"Increment\": 45";
		String[] names;
		if (selectedPart instanceof Foundation) {
			s += ", \"Building\": " + selectedPart.getId();
			names = new String[] { "Net", "AC", "Heater", "Windows", "Solar Panels" };
		} else {
			s += ", \"Part\": \"" + selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1) + "\"";
			names = new String[] { "Solar", "Heat Gain" };
		}
		for (String name : names) {
			List<Double> data = graph.getData(name);
			if (data == null)
				continue;
			s += ", \"" + name + "\": {";
			s += "\"Data\": [";
			for (Double x : data) {
				s += Graph.ENERGY_FORMAT.format(x) + ",";
			}
			s = s.substring(0, s.length() - 1);
			s += "]\n";
			s += "}";
		}
		s += "}";
		return s;
	}

}
