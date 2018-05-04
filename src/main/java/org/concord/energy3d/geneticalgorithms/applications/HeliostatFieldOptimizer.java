package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.EventQueue;
import java.util.Calendar;
import java.util.concurrent.Callable;

import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.geneticalgorithms.ObjectiveFunction;
import org.concord.energy3d.geneticalgorithms.RectangularBound;
import org.concord.energy3d.gui.CspProjectDailyEnergyGraph;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;

import com.ardor3d.math.Vector3;

/**
 * @author Charles Xie
 *
 */
public class HeliostatFieldOptimizer extends Optimizer {

	public HeliostatFieldOptimizer(final int populationSize, final int chromosomeLength, final int selectionMethod, final double convergenceThreshold) {
		super(populationSize, chromosomeLength, selectionMethod, convergenceThreshold);
	}

	@Override
	public void setFoundation(final Foundation foundation) {
		super.setFoundation(foundation);
		final Mirror heliostat = foundation.getHeliostats().get(0);
		final Foundation receiver = heliostat.getReceiver();
		if (receiver != null) {
			final Vector3 v0 = receiver.getAbsPoint(0);
			final Vector3 v1 = receiver.getAbsPoint(1);
			final Vector3 v2 = receiver.getAbsPoint(2);
			final Vector3 v3 = receiver.getAbsPoint(3);
			final double cx = 0.25 * (v0.getX() + v1.getX() + v2.getX() + v3.getX()) * Scene.getInstance().getAnnotationScale();
			final double cy = 0.25 * (v0.getY() + v1.getY() + v2.getY() + v3.getY()) * Scene.getInstance().getAnnotationScale();
			final double lx = v0.distance(v2) * Scene.getInstance().getAnnotationScale();
			final double ly = v0.distance(v1) * Scene.getInstance().getAnnotationScale();
			addConstraint(new RectangularBound(cx, cy, lx + heliostat.getMirrorWidth(), ly + heliostat.getMirrorHeight()));
		}
		// initialize the population with the first-born being the current design
		final Individual firstBorn = population.getIndividual(0);
		int i = 0;
		for (final Mirror m : foundation.getHeliostats()) {
			firstBorn.setGene(i++, m.getPoints().get(0).getX());
			firstBorn.setGene(i++, m.getPoints().get(0).getY());
		}
	}

	private void computeIndividualFitness(final Individual individual) {
		for (int j = 0; j < individual.getChromosomeLength(); j++) {
			final double gene = individual.getGene(j);
			final int j2 = j / 2;
			final Mirror m = foundation.getHeliostats().get(j2);
			if (j % 2 == 0) {
				m.getPoints().get(0).setX(gene);
			} else {
				m.getPoints().get(0).setY(gene);
			}
		}
		individual.setFitness(objectiveFunction.compute());
	}

	@Override
	public void applyFittest() {
		final Individual best = population.getFittest();
		for (int j = 0; j < best.getChromosomeLength(); j++) {
			final double gene = best.getGene(j);
			final int j2 = j / 2;
			final Mirror m = foundation.getHeliostats().get(j2);
			if (j % 2 == 0) {
				m.getPoints().get(0).setX(gene);
			} else {
				m.getPoints().get(0).setY(gene);
			}
		}
		System.out.println("Fittest: " + individualToString(best));
	}

	private String individualToString(final Individual individual) {
		String s = "(";
		for (int j = 0; j < individual.getChromosomeLength(); j++) {
			final double gene = individual.getGene(j);
			if (j % 2 == 0) {
				if (mins != null && maxs != null) {
					s += (mins[j] + gene * (maxs[j] - mins[j]));
				} else {
					s += gene;
				}
				s += ", ";
			} else {
				if (mins != null && maxs != null) {
					s += (mins[j] + gene * (maxs[j] - mins[j]));
				} else {
					s += gene;
				}
				s += " | ";
			}
		}
		return s.substring(0, s.length() - 3) + ") = " + individual.getFitness();
	}

	@Override
	void computeIndividual(final int indexOfIndividual) {

		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() {

				final int populationSize = population.size();

				if (populationSize > 9) { // implement standard GA

					if (!converged) {
						final Individual individual = population.getIndividual(indexOfIndividual);
						computeIndividualFitness(individual);
						final int generation = computeCounter / populationSize;
						System.out.println("Generation " + generation + ", individual " + indexOfIndividual + " : " + individualToString(individual));
						final boolean isAtTheEndOfGeneration = (computeCounter % populationSize) == (populationSize - 1);
						if (isAtTheEndOfGeneration) {
							population.saveGenes();
							population.runSGA(selectionRate, crossoverRate);
							if (detectViolations()) {
								population.restoreGenes();
							} else {
								converged = population.isSGAConverged();
								if (!converged) {
									population.mutate(mutationRate);
								}
							}
						}
					} else {
						SceneManager.getTaskManager().clearTasks();
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								onCompletion();
								applyFittest();
							}
						});
					}

				} else { // implement micro GA -- it doesn't exit when converged; At convergence, we restart by mating the winner with four new individuals that are randomly chosen

					final Individual individual = population.getIndividual(indexOfIndividual);
					computeIndividualFitness(individual);
					final int generation = computeCounter / populationSize;
					System.out.println("Generation " + generation + ", individual " + indexOfIndividual + " : " + individualToString(individual));
					final boolean isAtTheEndOfGeneration = (computeCounter % populationSize) == (populationSize - 1);
					if (isAtTheEndOfGeneration) {
						population.saveGenes();
						population.runMGA();
						if (detectViolations()) {
							population.restoreGenes();
						} else {
							if (population.isMGAConverged()) {
								population.restartMGA();
							}
						}
					}

				}

				computeCounter++;
				updateInfo();
				return null;

			}
		});

	}

	@Override
	void updateInfo() {
		final Foundation receiver = foundation.getHeliostats().get(0).getReceiver();
		if (receiver != null) {
			switch (objectiveFunction.getType()) {
			case ObjectiveFunction.DAILY:
				receiver.setLabelCustomText("Daily Output = " + EnergyPanel.ONE_DECIMAL.format(population.getIndividual(0).getFitness()));
				break;
			case ObjectiveFunction.ANNUAl:
				receiver.setLabelCustomText("Annual Output = " + EnergyPanel.ONE_DECIMAL.format(population.getIndividual(0).getFitness() * 30));
				break;
			case ObjectiveFunction.RANDOM:
				receiver.setLabelCustomText(null);
				break;
			}
			receiver.draw();
		}
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final Calendar today = Heliodon.getInstance().getCalendar();
				EnergyPanel.getInstance().getDateSpinner().setValue(today.getTime());
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (selectedPart instanceof Foundation) {
					final CspProjectDailyEnergyGraph g = EnergyPanel.getInstance().getCspProjectDailyEnergyGraph();
					g.setCalendar(today);
					EnergyPanel.getInstance().getCspProjectTabbedPane().setSelectedComponent(g);
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
