package org.concord.energy3d.simulation;

import java.awt.Color;
import java.awt.Dimension;

import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

/**
 * This calculates and visualizes the seasonal trend and the yearly sum of the energy density of solar irradiation and heat loss on the surface of a selected house part.
 * 
 * For fast feedback, only 12 days are calculated.
 * 
 * @author Charles Xie
 * 
 */

public class EnergyDensityAnalysis extends SeasonalAnalysis {

	public EnergyDensityAnalysis() {
		super();
		graph = new PartEnergyGraph();
		graph.setPreferredSize(new Dimension(600, 400));
		graph.setBackground(Color.white);
		graph.yAxisLabel = "Energy Density (kWh/m\u00B2)";
	}

	@Override
	void updateGraph() {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart instanceof Window) {
			final double area = selectedPart.computeArea();
			final double solar = selectedPart.getSolarPotentialToday() * Scene.getInstance().getWindowSolarHeatingRate();
			graph.addData("Solar", solar / area);
			final double[] loss = selectedPart.getHeatLoss();
			double sum = 0;
			for (final double x : loss)
				sum += x;
			graph.addData("Heat Gain", -sum / area);
		} else if (selectedPart instanceof Wall || selectedPart instanceof Door || selectedPart instanceof Roof) {
			double area = selectedPart.computeArea();
			final double[] loss = selectedPart.getHeatLoss();
			double sum = 0;
			for (final double x : loss)
				sum += x;
			graph.addData("Heat Gain", -sum / area);
		} else if (selectedPart instanceof Foundation) {
			graph.addData("Solar", selectedPart.getSolarPotentialToday() / selectedPart.computeArea());
		} else if (selectedPart instanceof SolarPanel) {
			final SolarPanel solarPanel = (SolarPanel) selectedPart;
			final double solar = solarPanel.getSolarPotentialToday() * Scene.getInstance().getSolarPanelEfficiencyNotPercentage();
			graph.addData("Solar", solar / (SolarPanel.WIDTH * SolarPanel.HEIGHT));
		}
		graph.repaint();
	}

}
