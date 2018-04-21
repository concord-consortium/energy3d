package org.concord.energy3d.generative;

/**
 * @author Charles Xie
 *
 */
public class Individual implements Comparable<Individual> {

	private final double[] chromosome;
	private double fitness; // store the fitness value evaluated by the objective function

	/** create an individual with the specified length and initialize its chromosome with random values between 0 and 1 */
	public Individual(final int length) {
		chromosome = new double[length];
		for (int i = 0; i < length; i++) {
			chromosome[i] = Math.random();
		}
	}

	public void setGene(final int i, final double g) {
		if (i < 0 || i >= chromosome.length) {
			throw new IllegalArgumentException("Gene index out of bound: " + i);
		}
		chromosome[i] = g;
	}

	public double getGene(final int i) {
		if (i < 0 || i >= chromosome.length) {
			throw new IllegalArgumentException("Gene index out of bound: " + i);
		}
		return chromosome[i];
	}

	public int getChromosomelength() {
		return chromosome.length;
	}

	public void setFitness(final double fitness) {
		this.fitness = fitness;
	}

	public double getFitness() {
		return fitness;
	}

	@Override
	public int compareTo(final Individual o) {
		if (fitness > o.fitness) {
			return 1;
		}
		if (fitness < o.fitness) {
			return -1;
		}
		return 0;
	}

}
