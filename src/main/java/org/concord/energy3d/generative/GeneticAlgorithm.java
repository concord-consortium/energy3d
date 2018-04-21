package org.concord.energy3d.generative;

/**
 * @author Charles Xie
 *
 */
public class GeneticAlgorithm {

	private final static int MAX_GENERATION = 10;

	private double mutationRate = 0.1;
	private double crossoverRate = 0.9;
	private double selectionRate = 0.5;
	private final Population population;
	private int generation;
	private final double[] mins, maxs;

	public GeneticAlgorithm(final int populationSize, final int chromosomeLength) {
		population = new Population(populationSize, chromosomeLength);
		mins = new double[chromosomeLength];
		maxs = new double[chromosomeLength];
	}

	public void evolve() {
		while (!shouldTerminate()) {
			System.out.println("GA: " + generation + " generation -- " + mins[0] + ", " + maxs[0] + " | " + mins[1] + ", " + maxs[1]);
			generation++;
		}
	}

	private boolean shouldTerminate() {
		return generation >= MAX_GENERATION;
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

}
