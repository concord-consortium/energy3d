package org.concord.energy3d.generative;

/**
 * @author Charles Xie
 *
 */
public abstract class GeneticAlgorithm {

	private final int populationSize;
	private final double mutationRate;
	private final double crossoverRate;
	private final double selectionRate;

	public GeneticAlgorithm(final int populationSize, final double mutationRate, final double crossoverRate, final double selectionRate) {
		this.populationSize = populationSize;
		this.mutationRate = mutationRate;
		this.crossoverRate = crossoverRate;
		this.selectionRate = selectionRate;
	}

}
