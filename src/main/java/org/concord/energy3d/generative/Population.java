package org.concord.energy3d.generative;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Charles Xie
 *
 */
public class Population {

	private final Individual[] individuals;
	private final List<Individual> selectedIndividuals = new ArrayList<Individual>();
	private double beta = 0.5;

	public Population(final int populationSize, final int chromosomeLength) {
		individuals = new Individual[populationSize];
		for (int i = 0; i < individuals.length; i++) {
			individuals[i] = new Individual(chromosomeLength);
		}
	}

	public int getChromosomeLength() {
		return individuals[0].getChromosomelength();
	}

	public Individual getIndividual(final int i) {
		if (i < 0 || i >= individuals.length) {
			throw new IllegalArgumentException("Individual index out of bound: " + i);
		}
		return individuals[i];
	}

	public void select(final double selectionRate) {
		selectedIndividuals.clear();
		Arrays.sort(individuals);
		for (int i = 0; i < individuals.length; i++) {
			if (i < selectionRate * individuals.length) {
				selectedIndividuals.add(individuals[i]);
			}
		}
	}

	// uniform crossover
	public void crossover(final double crossoverRate) {
		if (selectedIndividuals.size() <= 1) {
			return;
		}
		final Individual dad = selectedIndividuals.get(0);
		final Individual mom = selectedIndividuals.get(1);
		final int n = dad.getChromosomelength();
		final Individual child1 = new Individual(n);
		final Individual child2 = new Individual(n);
		beta = Math.random();
		for (int i = 0; i < n; i++) {
			final double di = dad.getGene(i);
			final double mi = mom.getGene(i);
			if (Math.random() < 0.5) {
				child1.setGene(i, beta * di + (1 - beta) * mi);
				child2.setGene(i, beta * mi + (1 - beta) * di);
			} else {
				child1.setGene(i, beta * mi + (1 - beta) * di);
				child2.setGene(i, beta * di + (1 - beta) * mi);
			}
		}
		individuals[2] = child1;
		individuals[3] = child2;
	}

	public void mutate(final double mutationRate) {
		for (int i = 0; i < individuals.length; i++) {
			if (Math.random() < mutationRate) {
				final int n = (int) (Math.random() * individuals[i].getChromosomelength());
				individuals[i].setGene(n, Math.random());
			}
		}
	}

	public int size() {
		return individuals.length;
	}

}
