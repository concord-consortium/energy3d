package org.concord.energy3d.simulation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 *
 */
public class FresnelReflectorDailyAnalysis extends DailyAnalysis {

	public FresnelReflectorDailyAnalysis() {
		super();
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
						final String current = Graph.TWO_DECIMALS.format(getResult("Solar"));
						final Map<String, Double> recordedResults = getRecordedResults("Solar");
						final int n = recordedResults.size();
						if (n > 0) {
							String previousRuns = "";
							final Object[] keys = recordedResults.keySet().toArray();
							for (int i = n - 1; i >= 0; i--) {
								previousRuns += keys[i] + " : " + Graph.TWO_DECIMALS.format(recordedResults.get(keys[i])) + " kWh<br>";
							}
							final Object[] options = new Object[] { "OK", "Copy Data" };
							final String msg = "<html>The calculated daily output is <b>" + current + " kWh</b>.<br><hr>Results from previously recorded tests:<br>" + previousRuns + "</html>";
							final JOptionPane optionPane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, options, options[0]);
							final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Daily Output");
							dialog.setVisible(true);
							final Object choice = optionPane.getValue();
							if (choice == options[1]) {
								String output = "";
								for (int i = 0; i < n; i++) {
									output += Graph.TWO_DECIMALS.format(recordedResults.get(keys[i])) + "\n";
								}
								output += current;
								final Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
								clpbrd.setContents(new StringSelection(output), null);
								JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>" + (n + 1) + " data points copied to system clipboard.<br><hr>" + output, "Confirmation", JOptionPane.INFORMATION_MESSAGE);
							}
						} else {
							JOptionPane.showMessageDialog(parent, "<html>The calculated daily output is <b>" + current + " kWh</b>.</html>", "Daily Output", JOptionPane.INFORMATION_MESSAGE);
						}
					}
				});
				return null;
			}
		});
	}

	@Override
	public void updateGraph() {
		for (int i = 0; i < 24; i++) {
			SolarRadiation.getInstance().computeEnergyAtHour(i);
			final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
			if (selectedPart != null) {
				if (selectedPart instanceof FresnelReflector) {
					final FresnelReflector r = (FresnelReflector) selectedPart;
					graph.addData("Solar", r.getSolarPotentialNow() * r.getSystemEfficiency());
				} else if (selectedPart instanceof Foundation) {
					double output = 0;
					for (final HousePart p : Scene.getInstance().getParts()) {
						if (p instanceof FresnelReflector && p.getTopContainer() == selectedPart) {
							final FresnelReflector r = (FresnelReflector) p;
							output += r.getSolarPotentialNow() * r.getSystemEfficiency();
						}
					}
					graph.addData("Solar", output);
				} else if (selectedPart.getTopContainer() instanceof Foundation) {
					double output = 0;
					for (final HousePart p : Scene.getInstance().getParts()) {
						if (p instanceof FresnelReflector && p.getTopContainer() == selectedPart.getTopContainer()) {
							final FresnelReflector r = (FresnelReflector) p;
							output += r.getSolarPotentialNow() * r.getSystemEfficiency();
						}
					}
					graph.addData("Solar", output);
				}
			} else {
				double output = 0;
				for (final HousePart p : Scene.getInstance().getParts()) {
					if (p instanceof FresnelReflector) {
						final FresnelReflector r = (FresnelReflector) p;
						output += r.getSolarPotentialNow() * r.getSystemEfficiency();
					}
				}
				graph.addData("Solar", output);
			}
		}
		graph.repaint();
	}

	public void show() {

		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		String s = null;
		int cost = -1;
		String title = "Daily Yield of All Fresnel Reflectors";
		if (selectedPart != null) {
			if (selectedPart instanceof FresnelReflector) {
				cost = (int) CspProjectCost.getPartCost(selectedPart);
				s = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
				title = "Daily Yield";
			} else if (selectedPart instanceof Foundation || selectedPart.getTopContainer() instanceof Foundation) {
				title = "Daily Yield of Selected Foundation";
			}
		}
		final JDialog dialog = new JDialog(MainFrame.getInstance(), s == null ? title : title + ": " + s + " (Cost: $" + cost + ")", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		graph.parent = dialog;

		final JMenuBar menuBar = new JMenuBar();
		dialog.setJMenuBar(menuBar);

		menuBar.add(createOptionsMenu(dialog, null, true));
		menuBar.add(createRunsMenu());

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
		String s = "{";
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart != null) {
			if (selectedPart instanceof FresnelReflector) {
				s += "\"Fresnel Reflector\": \"" + selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1) + "\"";
			} else if (selectedPart instanceof Foundation) {
				s += "\"Foundation\": \"" + selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1) + "\"";
			} else if (selectedPart.getTopContainer() instanceof Foundation) {
				s += "\"Foundation\": \"" + selectedPart.getTopContainer().toString().substring(0, selectedPart.getTopContainer().toString().indexOf(')') + 1) + "\"";
			}
		} else {
			s += "\"Fresnel Reflector\": \"All\"";
		}
		final String name = "Solar";
		final List<Double> data = graph.getData(name);
		s += ", \"" + name + "\": {";
		s += "\"Hourly\": [";
		for (final Double x : data) {
			s += Graph.FIVE_DECIMALS.format(x) + ",";
		}
		s = s.substring(0, s.length() - 1);
		s += "]\n";
		s += ", \"Total\": " + Graph.ENERGY_FORMAT.format(getResult(name));
		s += "}";
		s += "}";
		return s;
	}

}
