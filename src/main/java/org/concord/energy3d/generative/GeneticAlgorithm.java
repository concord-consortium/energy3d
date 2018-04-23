package org.concord.energy3d.generative;

import java.awt.EventQueue;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import org.concord.energy3d.gui.CspProjectDailyEnergyGraph;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.Util;

import com.ardor3d.math.Vector3;

/**
 * @author Charles Xie
 *
 */
public class GeneticAlgorithm {

	private final static int MAX_GENERATION = 5;

	private double mutationRate = 0.1;
	private double crossoverRate = 0.9;
	private double selectionRate = 0.5;
	private final Population population;
	private int outsideGenerationCounter;
	private int computeCounter;
	private final double[] mins, maxs;
	private final Foundation foundation;

	public GeneticAlgorithm(final int populationSize, final int chromosomeLength, final Foundation foundation) {
		this.foundation = foundation;
		population = new Population(populationSize, chromosomeLength);
		final Vector3 v0 = foundation.getAbsPoint(0);
		final Vector3 v1 = foundation.getAbsPoint(1);
		final Vector3 v2 = foundation.getAbsPoint(2);
		final Vector3 v3 = foundation.getAbsPoint(3);
		final double cx = 0.25 * (v0.getX() + v1.getX() + v2.getX() + v3.getX()) * Scene.getInstance().getAnnotationScale();
		final double cy = 0.25 * (v0.getY() + v1.getY() + v2.getY() + v3.getY()) * Scene.getInstance().getAnnotationScale();
		final double lx = v0.distance(v2) * Scene.getInstance().getAnnotationScale();
		final double ly = v0.distance(v1) * Scene.getInstance().getAnnotationScale();
		mins = new double[chromosomeLength];
		maxs = new double[chromosomeLength];
		for (int i = 0; i < chromosomeLength; i += 2) {
			setMinMax(i, cx - lx * 0.5, cx + lx * 0.5);
			setMinMax(i + 1, cy - ly * 0.5, cy + ly * 0.5);
		}
	}

	public void evolve() {
		outsideGenerationCounter = 0;
		computeCounter = 0;
		onStart();
		final HeliostatObjectiveFunction of = new HeliostatObjectiveFunction();
		final List<Mirror> heliostats = foundation.getHeliostats();
		while (!shouldTerminate()) {
			for (int i = 0; i < population.size(); i++) {
				final int indexOfIndividual = i;
				SceneManager.getTaskManager().update(new Callable<Object>() {
					@Override
					public Object call() {
						final int generation = computeCounter / population.size();
						final Individual individual = population.getIndividual(indexOfIndividual);
						for (int j = 0; j < individual.getChromosomelength(); j++) {
							final double gene = individual.getGene(j);
							// final double val = (mins[j] + gene * (maxs[j] - mins[j]));
							final int j2 = j / 2;
							final Mirror m = heliostats.get(j2);
							if (j % 2 == 0) {
								m.getPoints().get(0).setX(gene);
							} else {
								m.getPoints().get(0).setY(gene);
							}
						}
						Scene.getInstance().updateTrackables();
						individual.setFitness(of.compute());
						System.out.println("Generation " + generation + ", individual " + indexOfIndividual + " = " + individual.getFitness());

						final boolean generationEnd = (computeCounter % population.size()) == (population.size() - 1);
						if (generationEnd) {
							population.select(selectionRate);
							population.crossover(crossoverRate);
							population.mutate(mutationRate);
						}

						// update graph
						final Calendar c = Heliodon.getInstance().getCalendar();
						final Calendar today = (Calendar) c.clone();
						final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
						if (selectedPart instanceof Foundation) { // synchronize with daily graph
							final CspProjectDailyEnergyGraph g = EnergyPanel.getInstance().getCspProjectDailyEnergyGraph();
							if (g.hasGraph()) {
								g.setCalendar(today);
								g.updateGraph();
							}
						}
						final Calendar today2 = today;
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								EnergyPanel.getInstance().getDateSpinner().setValue(c.getTime());
								if (selectedPart instanceof Foundation) {
									final CspProjectDailyEnergyGraph g = EnergyPanel.getInstance().getCspProjectDailyEnergyGraph();
									EnergyPanel.getInstance().getCspProjectTabbedPane().setSelectedComponent(g);
									if (!g.hasGraph()) {
										g.setCalendar(today2);
										g.addGraph((Foundation) selectedPart);
									}
								}
							}
						});

						computeCounter++;

						return null;

					}
				});
			}
			outsideGenerationCounter++;
		}
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

	private boolean shouldTerminate() {
		return outsideGenerationCounter >= MAX_GENERATION;
	}

	public void setMinMax(final int i, final double min, final double max) {
		mins[i] = min;
		maxs[i] = max;
	}

	// initialize the population with the first-born being the current design
	public void initializePopulation(final double[] v) {
		if (population.getChromosomeLength() != v.length) {
			throw new IllegalArgumentException("Input data must have the same length as the chromosome length of the population");
		}
		final Individual firstBorn = population.getIndividual(0);
		for (int i = 0; i < v.length; i++) {
			firstBorn.setGene(i, v[i]);
		}
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

	private void onCompletion() {
		EnergyPanel.getInstance().progress(0);
		EnergyPanel.getInstance().disableDateSpinner(false);
		SceneManager.setExecuteAllTask(true);
	}

	private void onStart() {
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
