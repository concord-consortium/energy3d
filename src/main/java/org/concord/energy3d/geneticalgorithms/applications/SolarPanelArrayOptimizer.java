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
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;

import com.ardor3d.math.Vector3;

/**
 * Chromosome of an individual is encoded as follows:
 * 
 * inter-row spacing (d), tilt angle (a)
 *
 * assuming the base height is fixed and the number of rows on each rack increases when the tilt angle decreases (otherwise the maximum inter-row spacing would always be preferred)
 * 
 * @author Charles Xie
 *
 */
public class SolarPanelArrayOptimizer extends SolarOutputOptimizer {

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
		final Vector3 q = rack.getAbsCenter().subtractLocal(racks.get(1).getAbsCenter());
		final double interrowSpacing = Math.abs(q.dot(p.normalize(null))) * Scene.getInstance().getScale();
		final double maximumInterrowSpacing = p.length() * Scene.getInstance().getScale();
		// initialize the population with the first-born being the current design
		final Individual firstBorn = population.getIndividual(0);
		firstBorn.setGene(0, interrowSpacing / maximumInterrowSpacing);
		firstBorn.setGene(1, 0.5 * (1.0 + rack.getTiltAngle() / 90.0));
	}

	@Override
	void computeIndividualFitness(final Individual individual) {
		final List<Rack> racks = foundation.getRacks();
		for (int j = 0; j < individual.getChromosomeLength(); j++) {
			final double gene = individual.getGene(j);
			final Rack rack = racks.get(j);
			rack.setTiltAngle((2 * gene - 1) * 90);
		}
		individual.setFitness(objectiveFunction.compute());
	}

	@Override
	public void applyFittest() {
		final List<Rack> racks = foundation.getRacks();
		final Individual best = population.getFittest();
		for (int j = 0; j < best.getChromosomeLength(); j++) {
			final double gene = best.getGene(j);
			final Rack rack = racks.get(j);
			rack.setTiltAngle((2 * gene - 1) * 90);
			rack.draw();
		}
		System.out.println("Fittest: " + individualToString(best));
	}

	@Override
	String individualToString(final Individual individual) {
		String s = "(";
		for (int i = 0; i < individual.getChromosomeLength(); i++) {
			final double gene = individual.getGene(i);
			s += (2 * gene - 1) * 90 + ", ";
		}
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
