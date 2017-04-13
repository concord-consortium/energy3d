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
import java.util.ArrayList;
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

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.ClipImage;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 *
 */
public class GroupDailyAnalysis extends Analysis {

	private final List<HousePart> selectedParts;

	public GroupDailyAnalysis(final List<Long> ids) {
		super();
		selectedParts = new ArrayList<HousePart>();
		for (final Long i : ids) {
			selectedParts.add(Scene.getInstance().getPart(i));
		}
		double i = 0;
		final double n = selectedParts.size();
		for (final HousePart p : selectedParts) {
			final int a = (int) ((n - i) / n * 128);
			final int b = 255 - a;
			Graph.setColor("Solar " + p.getId(), new Color(255, a, b));
			Graph.setColor("PV " + p.getId(), new Color(255, a, b));
			Graph.setColor("CSP " + p.getId(), new Color(255, a, b));
			Graph.setColor("Heat Gain " + p.getId(), new Color(a, b, 255));
			Graph.setColor("Building " + p.getId(), new Color(a, b, 255));
			i++;
		}
		graph = new PartEnergyDailyGraph();
		graph.setPreferredSize(new Dimension(600, 400));
		graph.setBackground(Color.WHITE);
	}

	private void runAnalysis(final JDialog parent) {
		graph.info = "Calculating...";
		graph.repaint();
		onStart();
		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() {
				final Throwable t = compute();
				if (t != null) {
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							Util.reportError(t);
						}
					});
				}
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						onCompletion();
					}
				});
				return null;
			}
		});
	}

	@Override
	public void updateGraph() {
		final int n = (int) Math.round(60.0 / Scene.getInstance().getTimeStep());
		for (int i = 0; i < 24; i++) {
			SolarRadiation.getInstance().computeEnergyAtHour(i);
			for (final HousePart p : selectedParts) {
				if (p instanceof Window) {
					final Window window = (Window) p;
					final double solar = p.getSolarPotentialNow() * window.getSolarHeatGainCoefficient();
					graph.addData("Solar " + p.getId(), solar);
					final double[] loss = p.getHeatLoss();
					final int t0 = n * i;
					double sum = 0;
					for (int k = t0; k < t0 + n; k++) {
						sum += loss[k];
					}
					graph.addData("Heat Gain " + p.getId(), -sum);
				} else if (p instanceof Wall || p instanceof Roof) {
					final double[] loss = p.getHeatLoss();
					final int t0 = n * i;
					double sum = 0;
					for (int k = t0; k < t0 + n; k++) {
						sum += loss[k];
					}
					graph.addData("Heat Gain " + p.getId(), -sum);
				} else if (p instanceof SolarPanel) {
					graph.addData("Solar " + p.getId(), ((SolarPanel) p).getYieldNow());
				} else if (p instanceof Rack) {
					graph.addData("Solar " + p.getId(), ((Rack) p).getYieldNow());
				} else if (p instanceof Mirror) {
					final Mirror mirror = (Mirror) p;
					final double solar = mirror.getSolarPotentialNow() * mirror.getSystemEfficiency();
					graph.addData("Solar " + p.getId(), solar);
				} else if (p instanceof Foundation) {
					final Foundation foundation = (Foundation) p;
					switch (foundation.getSupportingType()) {
					case Foundation.PV_STATION:
						final double pv = foundation.getPhotovoltaicNow();
						graph.addData("PV " + p.getId(), pv);
						break;
					case Foundation.CSP_STATION:
						final double csp = foundation.getCspNow();
						graph.addData("CSP " + p.getId(), csp);
						break;
					case Foundation.BUILDING:
						final double totalEnergy = foundation.getTotalEnergyNow();
						graph.addData("Building " + p.getId(), totalEnergy);
						break;
					}
				}
			}
		}
		graph.repaint();
	}

	public void show(final String title) {

		final JDialog dialog = new JDialog(MainFrame.getInstance(), title, true);
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
				final int i = JOptionPane.showConfirmDialog(dialog, "Are you sure that you want to clear all the previous results\nrelated to the selected objects?", "Confirmation", JOptionPane.YES_NO_OPTION);
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
				DataViewer.viewRawData(dialog, graph, selectedParts);
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
				if (!DailyGraph.records.isEmpty()) {
					JMenuItem mi = new JMenuItem("Show All");
					mi.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							for (final Results r : DailyGraph.records) {
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
							for (final Results r : DailyGraph.records) {
								graph.hideRun(r.getID(), true);
							}
							graph.repaint();
							TimeSeriesLogger.getInstance().logShowRun(graph.getClass().getSimpleName(), "All", false);
						}
					});
					showRunsMenu.add(mi);
					showRunsMenu.addSeparator();
					final Map<String, Double> recordedResults = getRecordedResults("Net");
					for (final Results r : DailyGraph.records) {
						final String key = r.getID() + (r.getFileName() == null ? "" : " (file: " + r.getFileName() + ")");
						final Double result = recordedResults.get(key);
						final JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(r.getID() + ":" + r.getFileName() + (result == null ? "" : " - " + Math.round(recordedResults.get(key)) + " kWh"), !graph.isRunHidden(r.getID()));
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
		String type = "Unknown";
		final ArrayList<String> names = new ArrayList<String>();
		for (final HousePart p : selectedParts) {
			if (p instanceof SolarPanel) {
				names.add("Solar " + p.getId());
				type = "Solar Panel";
			} else if (p instanceof Rack) {
				names.add("Solar " + p.getId());
				type = "Rack";
			} else if (p instanceof Mirror) {
				names.add("Solar " + p.getId());
				type = "Mirror";
			} else if (p instanceof Wall) {
				names.add("Heat Gain " + p.getId());
				type = "Wall";
			} else if (p instanceof Roof) {
				names.add("Heat Gain " + p.getId());
				type = "Roof";
			} else if (p instanceof Door) {
				names.add("Heat Gain " + p.getId());
				type = "Door";
			} else if (p instanceof Window) {
				names.add("Solar " + p.getId());
				names.add("Heat Gain " + p.getId());
				type = "Window";
			}
		}
		String s = "{\"Type\": \"" + type + "\"";
		for (final String name : names) {
			final List<Double> data = graph.getData(name);
			if (data == null) {
				continue;
			}
			s += ", \"" + name + "\": {";
			s += "\"Hourly\": [";
			for (final Double x : data) {
				s += Graph.FIVE_DECIMALS.format(x) + ",";
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