package org.concord.energy3d.simulation;

import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
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
		graph.yAxisLabel = "Energy Density (kWh/m\u00B2)";
	}

	@Override
	void updateGraph() {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart instanceof Window) {
			double area = selectedPart.getAbsPoint(0).distance(selectedPart.getAbsPoint(2)) * Scene.getInstance().getAnnotationScale(); // width
			area *= selectedPart.getAbsPoint(0).distance(selectedPart.getAbsPoint(1)) * Scene.getInstance().getAnnotationScale(); // height
			final double solar = selectedPart.getSolarPotentialToday() * Scene.getInstance().getWindowSolarHeatingRate();
			graph.addData("Solar", solar / area);
			final double[] loss = selectedPart.getHeatLoss();
			double sum = 0;
			for (final double x : loss)
				sum += x;
			graph.addData("Heat Loss", sum / area);
		} else if (selectedPart instanceof Wall || selectedPart instanceof Door) {
			double area = selectedPart.getAbsPoint(0).distance(selectedPart.getAbsPoint(2)) * Scene.getInstance().getAnnotationScale(); // width
			area *= selectedPart.getAbsPoint(0).distance(selectedPart.getAbsPoint(1)) * Scene.getInstance().getAnnotationScale(); // height
			final double[] loss = selectedPart.getHeatLoss();
			double sum = 0;
			for (final double x : loss)
				sum += x;
			graph.addData("Heat Loss", sum / area);
		} else if (selectedPart instanceof Foundation) {
			double area = selectedPart.getAbsPoint(0).distance(selectedPart.getAbsPoint(2)) * Scene.getInstance().getAnnotationScale(); // width
			area *= selectedPart.getAbsPoint(0).distance(selectedPart.getAbsPoint(1)) * Scene.getInstance().getAnnotationScale(); // height
			graph.addData("Solar", selectedPart.getSolarPotentialToday() / area);
		} else if (selectedPart instanceof SolarPanel) {
			final SolarPanel solarPanel = (SolarPanel) selectedPart;
			final double solar = solarPanel.getSolarPotentialToday() * Scene.getInstance().getSolarPanelEfficiencyNotPercentage();
			graph.addData("Solar", solar / (SolarPanel.WIDTH * SolarPanel.HEIGHT));
		}
		graph.repaint();
	}

}
