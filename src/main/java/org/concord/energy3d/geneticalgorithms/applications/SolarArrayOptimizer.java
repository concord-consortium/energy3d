package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.EventQueue;
import java.util.Calendar;
import java.util.concurrent.Callable;

import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.geneticalgorithms.ObjectiveFunction;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.PvProjectDailyEnergyGraph;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;

/**
 * @author Charles Xie
 *
 */
public class SolarArrayOptimizer extends Optimizer {

	public SolarArrayOptimizer(final int populationSize, final int chromosomeLength, final Foundation foundation, final int maximumGeneration, final int selectionMethod, final double convergenceThreshold, final int objectiveFunctionType) {
		super(populationSize, chromosomeLength, foundation, maximumGeneration, selectionMethod, convergenceThreshold, objectiveFunctionType);
		// initialize the population with the first-born being the current design
		final Individual firstBorn = population.getIndividual(0);
		int i = 0;
		for (final Rack r : foundation.getRacks()) {
			firstBorn.setGene(i++, 0.5 * (1.0 + r.getTiltAngle() / 90.0));
		}
	}

	@Override
	void computeIndividual(final int indexOfIndividual) {

		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() {
				if (!converged) {
					final int generation = computeCounter / populationSize;
					final Individual individual = population.getIndividual(indexOfIndividual);
					for (int j = 0; j < individual.getChromosomeLength(); j++) {
						final double gene = individual.getGene(j);
						final Rack rack = foundation.getRacks().get(j);
						rack.setTiltAngle((2 * gene - 1) * 90);
					}
					individual.setFitness(objectiveFunction.compute());
					System.out.println("Generation " + generation + ", individual " + indexOfIndividual + " = " + individual.getFitness());
					final boolean isAtTheEndOfGeneration = (computeCounter % populationSize) == (populationSize - 1);
					if (isAtTheEndOfGeneration) {
						population.saveGenes();
						population.selectSurvivors(selectionRate);
						population.crossover(crossoverRate);
						population.mutate(mutationRate);
						detectViolations();
						population.restoreGenes();
						converged = population.isConverged();
					}
					computeCounter++;
				} else {
					SceneManager.getTaskManager().clearTasks();
					onCompletion();
				}
				updateInfo();
				return null;
			}
		});

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
		}
		foundation.draw();
		final Calendar c = Heliodon.getInstance().getCalendar();
		final Calendar today = (Calendar) c.clone();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart instanceof Foundation) { // synchronize with daily graph
			final PvProjectDailyEnergyGraph g = EnergyPanel.getInstance().getPvProjectDailyEnergyGraph();
			if (g.hasGraph()) {
				g.setCalendar(today);
				g.updateGraph();
			}
		}
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				EnergyPanel.getInstance().getDateSpinner().setValue(c.getTime());
				if (selectedPart instanceof Foundation) {
					final PvProjectDailyEnergyGraph g = EnergyPanel.getInstance().getPvProjectDailyEnergyGraph();
					EnergyPanel.getInstance().getPvProjectTabbedPane().setSelectedComponent(g);
					if (!g.hasGraph()) {
						g.setCalendar(today);
						g.addGraph((Foundation) selectedPart);
					}
				}
			}
		});
	}

}
