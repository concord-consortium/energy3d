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
import javax.swing.JPanel;

import static java.util.Calendar.*;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
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
		graph = new Graph();
		graph.setPreferredSize(new Dimension(600, 400));
		graph.setBackground(Color.white);
	}

	private void init() {
		clearSum();
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

	private void clearSum() {
		for (HousePart x : Scene.getInstance().getParts())
			x.setSolarPotentialSum(0);
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if (e.getSource() == EnergyPanel.getInstance()) {
			HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
			Foundation selectedBuilding;
			if (selectedPart == null) {
				selectedBuilding = null;
			} else if (selectedPart instanceof Foundation) {
				selectedBuilding = (Foundation) selectedPart;
			} else {
				selectedBuilding = (Foundation) selectedPart.getTopContainer();
			}
			if (selectedBuilding != null) {
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
				graph.repaint();
			}
			if (month < MONTHS.length - 1) {
				MainPanel.getInstance().getSolarButton().doClick();
				month++;
				run();
			}
		}
	}

	private void createDialog() {

		final JDialog dialog = new JDialog(MainFrame.getInstance(), "Seasonal Analysis", true);
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
				String[] header = new String[] { "Month", "Windows", "Solar Panels", "Heater", "AC", "Net" };
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
