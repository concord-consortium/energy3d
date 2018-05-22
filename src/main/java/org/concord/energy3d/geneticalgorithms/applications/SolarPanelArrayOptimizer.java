package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.EventQueue;
import java.util.Calendar;
import java.util.List;

import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.geneticalgorithms.ObjectiveFunction;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.PvProjectDailyEnergyGraph;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;

import com.ardor3d.math.Vector3;

/**
 * Chromosome of an individual is encoded as follows:
 * 
 * row spacing (d), tilt angle (a)
 *
 * assuming the base height is fixed and the number of rows on each rack increases when the tilt angle decreases (otherwise the maximum inter-row spacing would always be preferred)
 * 
 * @author Charles Xie
 *
 */
public class SolarPanelArrayOptimizer extends SolarOutputOptimizer {

	private double minimumRowSpacing;
	private double maximumRowSpacing;
	private double baseHeight;
	private SolarPanel solarPanel;
	private int panelRowsPerRack;

	public SolarPanelArrayOptimizer(final int populationSize, final int chromosomeLength, final int selectionMethod, final double convergenceThreshold, final int discretizationSteps) {
		super(populationSize, chromosomeLength, selectionMethod, convergenceThreshold, discretizationSteps);
	}

	@Override
	public void setFoundation(final Foundation foundation) {
		super.setFoundation(foundation);
		final List<Rack> racks = foundation.getRacks();
		final int n = racks.size();
		if (n < 2) {
			throw new RuntimeException("Must start with at least two existing racks.");
		}
		final Vector3 p = foundation.getAbsPoint(1).subtract(foundation.getAbsPoint(0), null);
		final Rack rack = racks.get(0);
		solarPanel = rack.getSolarPanel();
		baseHeight = rack.getBaseHeight() * Scene.getInstance().getScale();
		panelRowsPerRack = rack.getSolarPanelRowAndColumnNumbers()[1];
		final Vector3 q = rack.getAbsCenter().subtractLocal(racks.get(1).getAbsCenter());
		final double rowSpacing = Math.abs(q.dot(p.normalize(null))) * Scene.getInstance().getScale();
		maximumRowSpacing = p.length() * Scene.getInstance().getScale();
		minimumRowSpacing = rack.getRackHeight();
		// initialize the population with the first-born being the current design
		final Individual firstBorn = population.getIndividual(0);
		firstBorn.setGene(0, (rowSpacing - minimumRowSpacing) / maximumRowSpacing);
		firstBorn.setGene(1, 0.5 * (1.0 + rack.getTiltAngle() / 90.0));
	}

	@Override
	void computeIndividualFitness(final Individual individual) {
		final double rowSpacing = minimumRowSpacing + individual.getGene(0) * maximumRowSpacing;
		final double tiltAngle = (2 * individual.getGene(1) - 1) * 90;
		foundation.addSolarRackArrays(solarPanel, tiltAngle, baseHeight, panelRowsPerRack, rowSpacing, 1);
		individual.setFitness(objectiveFunction.compute());
	}

	@Override
	public void applyFittest() {
		final Individual best = population.getFittest();
		final double rowSpacing = minimumRowSpacing + best.getGene(0) * maximumRowSpacing;
		final double tiltAngle = (2 * best.getGene(1) - 1) * 90;
		foundation.addSolarRackArrays(solarPanel, tiltAngle, baseHeight, panelRowsPerRack, rowSpacing, 1);
		System.out.println("Fittest: " + individualToString(best));
	}

	@Override
	String individualToString(final Individual individual) {
		String s = "(";
		s += (minimumRowSpacing + individual.getGene(0) * maximumRowSpacing) + ", ";
		s += (2 * individual.getGene(1) - 1) * 90 + ", ";
		return s.substring(0, s.length() - 2) + ") = " + individual.getFitness();
	}

	@Override
	void updateInfo() {
		switch (objectiveFunction.getType()) {
		case ObjectiveFunction.DAILY:
			foundation.setLabelCustomText("Daily Output = " + EnergyPanel.ONE_DECIMAL.format(population.getIndividual(0).getFitness()));
			break;
		case ObjectiveFunction.ANNUAl:
			foundation.setLabelCustomText("Annual Output = " + EnergyPanel.ONE_DECIMAL.format(population.getIndividual(0).getFitness() * 30));
			break;
		case ObjectiveFunction.RANDOM:
			foundation.setLabelCustomText(null);
			break;
		}
		foundation.draw();
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final Calendar today = Heliodon.getInstance().getCalendar();
				EnergyPanel.getInstance().getDateSpinner().setValue(today.getTime());
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (selectedPart instanceof Foundation) { // synchronize with daily graph
					final PvProjectDailyEnergyGraph g = EnergyPanel.getInstance().getPvProjectDailyEnergyGraph();
					g.setCalendar(today);
					EnergyPanel.getInstance().getPvProjectTabbedPane().setSelectedComponent(g);
					if (g.hasGraph()) {
						g.updateGraph();
					} else {
						g.addGraph((Foundation) selectedPart);
					}
				}
			}
		});
	}

}
