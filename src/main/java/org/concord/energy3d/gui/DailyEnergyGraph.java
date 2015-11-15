package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.simulation.BuildingEnergyDailyGraph;
import org.concord.energy3d.simulation.SolarRadiation;

/**
 * @author Charles Xie
 *
 */
public class DailyEnergyGraph extends JPanel {

	private static final long serialVersionUID = 1L;

	private BuildingEnergyDailyGraph graph;

	public DailyEnergyGraph() {
		super(new BorderLayout());
	}

	public void removeGraph() {
		if (graph != null)
			remove(graph);
		repaint();
	}

	public void updateGraph(Foundation building) {
		for (int i = 0; i < 24; i++) {
			SolarRadiation.getInstance().computeEnergyAtHour(i);
			graph.addData("Windows", building.getPassiveSolarNow());
			graph.addData("Solar Panels", building.getPhotovoltaicNow());
			graph.addData("Heater", building.getHeatingNow());
			graph.addData("AC", building.getCoolingNow());
			graph.addData("Net", building.getTotalEnergyNow());
		}
	}

	public void addGraph(Foundation building) {

		removeGraph();

		graph = new BuildingEnergyDailyGraph();
		graph.setPreferredSize(new Dimension(getWidth() - 10, getHeight()));
		graph.setBackground(Color.WHITE);
		graph.setBorder(BorderFactory.createEtchedBorder());
		graph.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					MainFrame.getInstance().getDailyEnergyAnalysisMenuItem().doClick();
				}
			}
		});

		if (MainPanel.getInstance().getEnergyViewButton().isSelected()) {
			updateGraph(building);
		}

		add(graph, BorderLayout.CENTER);

		repaint();

	}

}
