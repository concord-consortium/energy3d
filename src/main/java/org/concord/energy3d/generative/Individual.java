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
		this(length, true);
	}

	/** @return a copy */
	public Individual(final Individual original) {
		this(original.chromosome.length, false);
		System.arraycopy(original.chromosome, 0, chromosome, 0, chromosome.length);
	}

	private Individual(final int length, final boolean randomize) {
		chromosome = new double[length];
		if (randomize) {
			for (int i = 0; i < length; i++) {
				chromosome[i] = Math.random();
			}
		}
	}

	public void copyGenes(final Individual original) {
		System.arraycopy(original.chromosome, 0, chromosome, 0, chromosome.length);
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

	public int getChromosomeLength() {
		return chromosome.length;
	}

	public void setFitness(final double fitness) {
		this.fitness = fitness;
	}

	public double getFitness() {
		return fitness;
	}

	/** highest fitness first, lowest fitness last */
	@Override
	public int compareTo(final Individual o) {
		if (fitness > o.fitness) {
			return -1;
		}
		if (fitness < o.fitness) {
			return 1;
		}
		return 0;
	}

	@Override
	public String toString() {
		String s = "";
		for (final double x : chromosome) {
			s += x + ", ";
		}
		return "(" + s.substring(0, s.length() - 2) + ") : " + fitness;
	}

}
