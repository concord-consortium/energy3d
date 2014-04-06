package org.concord.energy3d.simulation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import static java.util.Calendar.*;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;

/**
 * This calculates and visualizes the seasonal trend and the yearly sum of all energy items for any selected part or building.
 * 
 * For fast feedback, by default, the sum is based on adding the energy items computed for the first day of each month and then that number is multiplied by 365/12.
 * 
 * @author Charles Xie
 * 
 */

public class SeasonalAnalysis implements PropertyChangeListener {

	private final static int[] MONTHS = { JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER };

	private int month = 0;
	private int day = 1;
	private Graph graph;

	public SeasonalAnalysis() {
		HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		graph = selectedPart instanceof Foundation ? new BuildingEnergyGraph() : new PartEnergyGraph();
		graph.setPreferredSize(new Dimension(600, 400));
		graph.setBackground(Color.white);
	}

	private void init() {
		month = 0;
		graph.clearData();
	}

	private void run() {
		Heliodon.getInstance().getCalander().set(0, MONTHS[month], day);
		EnergyPanel.getInstance().getDateSpinner().setValue(Heliodon.getInstance().getCalander().getTime());
		MainPanel.getInstance().getSolarButton().doClick();
		graph.repaint();
	}

	public void show() {
		if (MainPanel.getInstance().getSolarButton().isSelected())
			MainPanel.getInstance().getSolarButton().doClick();
		createDialog();
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if (e.getSource() == EnergyPanel.getInstance()) {
			HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
			if (selectedPart instanceof Foundation) {
				Foundation selectedBuilding = (Foundation) selectedPart;
				double window = selectedBuilding.getPassiveSolarToday();
				double solarPanel = selectedBuilding.getPhotovoltaicToday();
				double heater = selectedBuilding.getHeatingToday();
				double ac = selectedBuilding.getCoolingToday();
				double net = selectedBuilding.getTotalEnergyToday();
				// System.out.println(month + ", " + window + ", " + solarPanel + ", " + heater + ", " + ac + ", " + net);
				graph.addData("Windows", window);
				graph.addData("Solar Panels", solarPanel);
				graph.addData("Heater", heater);
				graph.addData("AC", ac);
				graph.addData("Net", net);
			} else if (selectedPart instanceof Window) {
				Window window = (Window) selectedPart;
				double solar = window.getSolarPotentialToday() * Scene.getInstance().getWindowSolarHeatingRate();
				graph.addData("Solar", solar);
				double[] loss = window.getHeatLoss();
				double sum = 0;
				for (double x : loss)
					sum += x;
				graph.addData("Heat Transfer", sum);
			} else if (selectedPart instanceof SolarPanel) {
				SolarPanel solarPanel = (SolarPanel) selectedPart;
				double solar = solarPanel.getSolarPotentialToday() * Scene.getInstance().getSolarPanelEfficiency();
				graph.addData("Solar", solar);
			}
			graph.repaint();
			if (month < MONTHS.length - 1) {
				MainPanel.getInstance().getSolarButton().doClick();
				month++;
				run();
			}
		}
	}

	private void createDialog() {

		HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		final JDialog dialog = new JDialog(MainFrame.getInstance(), "Seasonal Analysis: " + (selectedPart == null ? "" : selectedPart.toString().substring(0, selectedPart.toString().indexOf(')') + 1)), true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JPanel contentPane = new JPanel(new BorderLayout());
		dialog.setContentPane(contentPane);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEtchedBorder());
		contentPane.add(panel, BorderLayout.CENTER);

		panel.add(graph, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		JButton button = new JButton("Run");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				init();
				run();
			}
		});
		buttonPanel.add(button);

		button = new JButton("Raw Data");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String[] header = null;
				if (graph instanceof BuildingEnergyGraph) {
					header = new String[] { "Month", "Windows", "Solar Panels", "Heater", "AC", "Net" };
				} else {
					HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
					if (selectedPart instanceof SolarPanel) {
						header = new String[] { "Month", "Solar" };
					} else if (selectedPart instanceof Window) {
						header = new String[] { "Month", "Solar", "Heat Transfer" };
					}
				}
				if (header == null) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Problem in finding data.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				int m = header.length;
				int n = graph.getLength();
				Object[][] column = new Object[n][m + 1];
				for (int i = 0; i < n; i++)
					column[i][0] = (i + 1);
				for (int j = 1; j < header.length; j++) {
					List<Double> list = graph.getData(header[j]);
					for (int i = 0; i < n; i++)
						column[i][j] = list.get(i);
				}
				DataViewer.showDataWindow("Data", column, header, dialog);
			}
		});
		buttonPanel.add(button);

		button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				EnergyPanel.getInstance().removePropertyChangeListener(SeasonalAnalysis.this);
				dialog.dispose();
			}
		});
		buttonPanel.add(button);

		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				EnergyPanel.getInstance().removePropertyChangeListener(SeasonalAnalysis.this);
				dialog.dispose();
			}
		});

		dialog.pack();
		dialog.setLocationRelativeTo(MainFrame.getInstance());
		dialog.setVisible(true);

	}

}
