package org.concord.energy3d.generative;

import java.util.Arrays;

/**
 * @author Charles Xie
 *
 */
public class Population {

	private final Individual[] individuals;

	public Population(final int populationSize, final int chromosomeLength) {
		individuals = new Individual[populationSize];
		for (int i = 0; i < individuals.length; i++) {
			individuals[i] = new Individual(chromosomeLength);
		}
	}

	public void sortByFitness() {
		Arrays.sort(individuals);
	}

	public int getSize() {
		return individuals.length;
	}

}
