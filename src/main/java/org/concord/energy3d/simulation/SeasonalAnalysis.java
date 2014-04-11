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
import static java.util.Calendar.MONTH;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.Util;

/**
 * This calculates and visualizes the seasonal trend and the yearly sum of all energy items for any selected part or building.
 *
 * For fast feedback, the sum is based on adding the energy items computed for the currently selected day of each month and then that number can be multiplied by 365/12.
 *
 * @author Charles Xie
 *
 */

public class SeasonalAnalysis {

	private final static int[] MONTHS = { JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER };

	private final Graph graph;
	private volatile boolean analysisStopped;

	public SeasonalAnalysis() {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		graph = selectedPart instanceof Foundation ? new BuildingEnergyGraph() : new PartEnergyGraph();
		graph.setPreferredSize(new Dimension(600, 400));
		graph.setBackground(Color.white);
	}

	private void runAnalysis() {
		EnergyPanel.getInstance().disableActions(true);
		Heliodon.getInstance().getCalander().set(MONTH, JANUARY);
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (final int m : MONTHS) {
					if (!analysisStopped) {
						Heliodon.getInstance().getCalander().set(MONTH, m);
						try {
							EnergyPanel.getInstance().computeNow();
						} catch (final Exception e) {
							e.printStackTrace();
						}
						updateGraph();
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								EnergyPanel.getInstance().getDateSpinner().setValue(Heliodon.getInstance().getCalander().getTime());
							}
						});
					}
				}
				EnergyPanel.getInstance().disableActions(false);
			}
		}, "Seasonal Analysis").start();
	}

	private void stopAnalysis() {
		analysisStopped = true;
	}

	public void show() {
		createDialog();
	}

	private void updateGraph() {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart instanceof Foundation) {
			final Foundation selectedBuilding = (Foundation) selectedPart;
			final double window = selectedBuilding.getPassiveSolarToday();
			final double solarPanel = selectedBuilding.getPhotovoltaicToday();
			final double heater = selectedBuilding.getHeatingToday();
			final double ac = selectedBuilding.getCoolingToday();
			final double net = selectedBuilding.getTotalEnergyToday();
			// System.out.println(window + ", " + solarPanel + ", " + heater + ", " + ac + ", " + net);
			graph.addData("Windows", window);
			graph.addData("Solar Panels", solarPanel);
			graph.addData("Heater", heater);
			graph.addData("AC", ac);
			graph.addData("Net", net);
		} else if (selectedPart instanceof Window) {
			final double solar = selectedPart.getSolarPotentialToday() * Scene.getInstance().getWindowSolarHeatingRate();
			graph.addData("Solar", solar);
			final double[] loss = selectedPart.getHeatLoss();
			double sum = 0;
			for (final double x : loss)
				sum += x;
			graph.addData("Heat Loss", sum);
		} else if (selectedPart instanceof Wall || selectedPart instanceof Roof || selectedPart instanceof Door) {
			final double[] loss = selectedPart.getHeatLoss();
			double sum = 0;
			for (final double x : loss)
				sum += x;
			graph.addData("Heat Loss", sum);
		} else if (selectedPart instanceof SolarPanel) {
			final SolarPanel solarPanel = (SolarPanel) selectedPart;
			final double solar = solarPanel.getSolarPotentialToday() * Scene.getInstance().getSolarPanelEfficiencyNotPercentage();
			graph.addData("Solar", solar);
		}
		graph.repaint();
	}

	private void createDialog() {

		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		String title = selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1);
		title = title.replaceAll("Foundation", "Building");
		final JDialog dialog = new JDialog(MainFrame.getInstance(), "Seasonal Analysis: " + title, true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		final JMenuBar menuBar = new JMenuBar();
		dialog.setJMenuBar(menuBar);

		final JMenuItem miClear = new JMenuItem("Clear Previous Results");
		final JMenuItem miView = new JMenuItem("View Raw Data");

		final JMenu menu = new JMenu("Options");
		menu.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
				miClear.setEnabled(graph.hasRecords());
				miView.setEnabled(graph.hasData());
			}

			@Override
			public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(final PopupMenuEvent e) {
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
			}
		});
		menu.add(miClear);

		miView.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				viewRawData(dialog);
			}
		});
		menu.add(miView);

		final JPanel contentPane = new JPanel(new BorderLayout());
		dialog.setContentPane(contentPane);

		final JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		contentPane.add(panel, BorderLayout.CENTER);

		panel.add(graph, BorderLayout.CENTER);

		final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		JButton button = new JButton("Run");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				Util.selectSilently(MainPanel.getInstance().getSolarButton(), true);
				graph.clearData();
				runAnalysis();
			}
		});
		buttonPanel.add(button);

		button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				stopAnalysis();
				if (graph.hasData()) {
					final Object[] options = { "Yes", "No" };
					if (JOptionPane.showOptionDialog(dialog, "Do you want to keep the results of this run?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]) == JOptionPane.YES_OPTION)
						graph.keepResults();
				}
				dialog.dispose();
			}
		});
		buttonPanel.add(button);

		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				stopAnalysis();
				dialog.dispose();
			}
		});

		dialog.pack();
		dialog.setLocationRelativeTo(MainFrame.getInstance());
		dialog.setVisible(true);

	}

	private void viewRawData(final JDialog parent) {
		String[] header = null;
		if (graph instanceof BuildingEnergyGraph) {
			header = new String[] { "Month", "Windows", "Solar Panels", "Heater", "AC", "Net" };
		} else {
			final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
			if (selectedPart instanceof SolarPanel) {
				header = new String[] { "Month", "Solar" };
			} else if (selectedPart instanceof Window) {
				header = new String[] { "Month", "Solar", "Heat Loss" };
			}
		}
		if (header == null) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "Problem in finding data.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		final int m = header.length;
		final int n = graph.getLength();
		final Object[][] column = new Object[n][m + 1];
		for (int i = 0; i < n; i++)
			column[i][0] = (i + 1);
		for (int j = 1; j < header.length; j++) {
			final List<Double> list = graph.getData(header[j]);
			for (int i = 0; i < n; i++)
				column[i][j] = list.get(i);
		}
		DataViewer.showDataWindow("Data", column, header, parent);
	}

}
