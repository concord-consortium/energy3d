package org.concord.energy3d.generative;

/**
 * @author Charles Xie
 *
 */
public abstract class GeneticAlgorithm {

	private final static int MAX_GENERATION = 10;

	private final double mutationRate;
	private final double crossoverRate;
	private final double selectionRate;
	private final Population population;
	private int generation;

	public GeneticAlgorithm(final int populationSize, final int chromosomeLength, final double mutationRate, final double crossoverRate, final double selectionRate) {
		population = new Population(populationSize, chromosomeLength);
		this.mutationRate = mutationRate;
		this.crossoverRate = crossoverRate;
		this.selectionRate = selectionRate;
	}

	public void evolve() {
		while (!shouldTerminate()) {
			if (generation > MAX_GENERATION) {
				break;
			}
			generation++;
		}
	}

	private boolean shouldTerminate() {
		return false;
	}

	// initialize the population with the first-born being the current design
	public void initializePopulation(final double[] v) {
		final Individual firstBorn = population.getIndividual(0);
		for (int i = 0; i < v.length; i++) {
			firstBorn.setGene(i, v[i]);
		}
	}

	public Population getPopulation() {
		return population;
	}

}
