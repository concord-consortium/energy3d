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
import org.concord.energy3d.scene.SceneManager;

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
	void updateGraph() {
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
			Window window = (Window) selectedPart;
			final double solar = selectedPart.getSolarPotentialToday() * window.getSolarHeatGainCoefficientNotPercentage();
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
			final double solar = solarPanel.getSolarPotentialToday() * solarPanel.getEfficiencyNotPercentage();
			graph.addData("Solar", solar);
		}
		graph.repaint();
	}

}
