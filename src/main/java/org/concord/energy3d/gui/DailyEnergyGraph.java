package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.BuildingEnergyDailyGraph;
import org.concord.energy3d.simulation.SolarRadiation;

/**
 * @author Charles Xie
 *
 */
public class DailyEnergyGraph extends JPanel {

	private static final long serialVersionUID = 1L;

	private BuildingEnergyDailyGraph graph;
	private Foundation building;

	public DailyEnergyGraph() {
		super(new BorderLayout());
		graph = new BuildingEnergyDailyGraph();
		graph.setPopup(false);
		graph.setBackground(Color.WHITE);
		graph.setBorder(BorderFactory.createEtchedBorder());
		graph.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					MainFrame.getInstance().getDailyEnergyAnalysisMenuItem().doClick();
				}
			}
		});
	}

	public void removeGraph() {
		remove(graph);
		repaint();
	}

	public void updateGraph() {
		if (building == null)
			return;
		graph.clearData();
		for (int i = 0; i < 24; i++) {
			SolarRadiation.getInstance().computeEnergyAtHour(i);
			graph.addData("Windows", building.getPassiveSolarNow());
			graph.addData("Solar Panels", building.getPhotovoltaicNow());
			graph.addData("Heater", building.getHeatingNow());
			graph.addData("AC", building.getCoolingNow());
			graph.addData("Net", building.getTotalEnergyNow());
		}
		repaint();
	}

	public void addGraph(Foundation building) {
		this.building = building;
		removeGraph();
		graph.setPreferredSize(new Dimension(getWidth() - 10, getHeight()));
		if (SceneManager.getInstance().getSolarHeatMap()) {
			updateGraph();
		}
		add(graph, BorderLayout.CENTER);
		repaint();
	}

}
