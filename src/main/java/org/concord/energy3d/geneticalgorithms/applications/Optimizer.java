package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.EventQueue;
import java.util.List;
import java.util.concurrent.Callable;

import org.concord.energy3d.geneticalgorithms.Constraint;
import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.geneticalgorithms.ObjectiveFunction;
import org.concord.energy3d.geneticalgorithms.Population;
import org.concord.energy3d.geneticalgorithms.RectangularBound;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 *
 */
public abstract class Optimizer {

	int maximumGeneration = 5;
	double mutationRate = 0.1;
	double crossoverRate = 0.5;
	double selectionRate = 0.5;
	Population population;
	int outsideGenerationCounter;
	int computeCounter;
	double[] mins, maxs;
	Foundation foundation;
	List<Constraint> constraints;
	int populationSize;
	int chromosomeLength;
	int objectiveFunctionType = ObjectiveFunction.DAILY;
	volatile boolean converged;
	ObjectiveFunction objectiveFunction;

	abstract void computeIndividual(final int indexOfIndividual);

	public void evolve() {

		onStart();
		outsideGenerationCounter = 0;
		computeCounter = 0;

		while (!shouldTerminate()) { // the number of individuals to evaluate is maximumGeneration * population.size(), subject to the convergence criterion
			for (int i = 0; i < population.size(); i++) {
				computeIndividual(i);
			}
			outsideGenerationCounter++;
		}
		population.sort();
		computeIndividual(0);

		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						onCompletion();
					}
				});
				return null;
			}
		});

	}

	// if anyone in the current population doesn't meed the constraints, the entire population dies and the algorithm reverts to the previous generation -- not efficient
	void detectViolations() {
		for (int i = 0; i < populationSize; i++) {
			final Individual individual = population.getIndividual(i);
			final double[] x = new double[chromosomeLength / 2];
			final double[] y = new double[chromosomeLength / 2];
			for (int j = 0; j < chromosomeLength; j++) {
				final double gene = individual.getGene(j);
				final int j2 = j / 2;
				if (j % 2 == 0) {
					x[j2] = (mins[j] + gene * (maxs[j] - mins[j]));
				} else {
					y[j2] = (mins[j] + gene * (maxs[j] - mins[j]));
				}
			}
			for (int j2 = 0; j2 < x.length; j2++) {
				for (final Constraint c : constraints) {
					if (c instanceof RectangularBound) {
						final RectangularBound rb = (RectangularBound) c;
						if (rb.contains(x[j2], y[j2])) {
							population.setViolation(i, true);
						}
					}
				}
				// if (j2 > 0) { // detect position overlaps
				// final double wj2 = Math.max(foundation.getHeliostats().get(j2).getMirrorWidth(), foundation.getHeliostats().get(j2).getMirrorHeight());
				// for (int k2 = 0; k2 < j2; k2++) {
				// final double wk2 = Math.max(foundation.getHeliostats().get(k2).getMirrorWidth(), foundation.getHeliostats().get(k2).getMirrorHeight());
				// final double dx = x[j2] - x[k2];
				// final double dy = y[j2] - y[k2];
				// if (dx * dx + dy * dy < 0.49 * wj2 * wk2) {
				// population.setViolation(i, true);
				// }
				// }
				// }
			}
		}
	}

	public void addConstraint(final Constraint c) {
		constraints.add(c);
	}

	boolean shouldTerminate() {
		return outsideGenerationCounter >= maximumGeneration;
	}

	public void setMinMax(final int i, final double min, final double max) {
		mins[i] = min;
		maxs[i] = max;
	}

	public Population getPopulation() {
		return population;
	}

	public void setCrossoverRate(final double crossoverRate) {
		this.crossoverRate = crossoverRate;
	}

	public double getCrossoverRate() {
		return crossoverRate;
	}

	public void setMutationRate(final double mutationRate) {
		this.mutationRate = mutationRate;
	}

	public double getMutationRate() {
		return mutationRate;
	}

	public void setSelectionRate(final double selectionRate) {
		this.selectionRate = selectionRate;
	}

	public double getSelectionRate() {
		return selectionRate;
	}

	void onCompletion() {
		EnergyPanel.getInstance().progress(0);
		EnergyPanel.getInstance().disableDateSpinner(false);
		SceneManager.setExecuteAllTask(true);
		EnergyPanel.getInstance().cancel();
	}

	void onStart() {
		EnergyPanel.getInstance().disableDateSpinner(true);
		SceneManager.getInstance().setHeatFluxDaily(true);
		Util.selectSilently(MainPanel.getInstance().getEnergyButton(), true);
		SceneManager.getInstance().setSolarHeatMapWithoutUpdate(true);
		SceneManager.getInstance().setHeatFluxVectorsVisible(true);
		SceneManager.getInstance().getSolarLand().setVisible(Scene.getInstance().getSolarMapForLand());
		SceneManager.setExecuteAllTask(false);
		Scene.getInstance().redrawAllNow();
	}

}
