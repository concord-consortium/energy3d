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
	private final List<Individual> survivors = new ArrayList<Individual>();
	private double beta = 0.5;
	private final List<Individual> toMutate = new ArrayList<Individual>();
	private final List<Individual> toMutateOriginal = new ArrayList<Individual>();

	public Population(final int populationSize, final int chromosomeLength) {
		individuals = new Individual[populationSize];
		for (int i = 0; i < individuals.length; i++) {
			individuals[i] = new Individual(chromosomeLength);
		}
	}

	public int size() {
		return individuals.length;
	}

	public int getChromosomeLength() {
		return individuals[0].getChromosomeLength();
	}

	public Individual getIndividual(final int i) {
		if (i < 0 || i >= individuals.length) {
			throw new IllegalArgumentException("Individual index out of bound: " + i);
		}
		return individuals[i];
	}

	public void sort() {
		Arrays.sort(individuals);
	}

	/** select the survivors based on elitism specified by the rate of selection */
	public void selectSurvivors(final double selectionRate) {
		survivors.clear();
		sort();
		for (int i = 0; i < individuals.length; i++) {
			if (i < selectionRate * individuals.length) {
				survivors.add(individuals[i]);
			}
		}
	}

	// select a parent by the roulette wheel rule (fitness proportionate selection)
	private Parents selectParents(final double lowestFitness, final double sumOfFitness) {
		// spin the wheel to find dad
		Individual dad = null;
		double roulettWheelPosition = Math.random() * sumOfFitness;
		double spinWheel = 0;
		for (final Individual s : survivors) {
			spinWheel += s.getFitness() - lowestFitness;
			if (spinWheel >= roulettWheelPosition) {
				dad = s;
				break;
			}
		}
		// spin the wheel to find mom
		Individual mom = null;
		roulettWheelPosition = Math.random() * sumOfFitness;
		spinWheel = 0;
		for (final Individual s : survivors) {
			spinWheel += s.getFitness() - lowestFitness;
			if (spinWheel >= roulettWheelPosition) {
				if (s != dad) {
					mom = s;
				} else {
					final int index = survivors.indexOf(s);
					if (index == 0) {
						mom = survivors.get(1);
					} else if (index == survivors.size() - 1) {
						mom = survivors.get(survivors.size() - 2);
					} else {
						mom = survivors.get(Math.random() < 0.5 ? index - 1 : index + 1);
					}
				}
				break;
			}
		}
		return new Parents(dad, mom);
	}

	/** roulette wheel selection for uniform crossover */
	public void crossover(final double crossoverRate) {
		final int numberOfSurvivers = survivors.size();
		if (numberOfSurvivers <= 1) {
			return;
		}

		final double lowestFitness = individuals[numberOfSurvivers].getFitness();
		double sumOfFitness = 0;
		for (int i = 0; i < numberOfSurvivers; i++) {
			sumOfFitness += individuals[i].getFitness() - lowestFitness;
		}

		final int newBorn = individuals.length - numberOfSurvivers;
		final List<Parents> oldFolks = new ArrayList<Parents>(newBorn);
		while (oldFolks.size() * 2 < newBorn) { // multiplying 2 because each couple produces two children as shown in the mating algorithm below
			final Parents p = selectParents(lowestFitness, sumOfFitness);
			if (!oldFolks.contains(p)) {
				oldFolks.add(p);
			}
		}

		int childIndex = numberOfSurvivers;
		for (final Parents p : oldFolks) {
			final int n = p.dad.getChromosomeLength();
			final Individual child1 = new Individual(n);
			final Individual child2 = new Individual(n);
			beta = Math.random();
			for (int i = 0; i < n; i++) {
				final double di = p.dad.getGene(i);
				final double mi = p.mom.getGene(i);
				if (Math.random() < 0.5) {
					child1.setGene(i, beta * di + (1 - beta) * mi);
					child2.setGene(i, beta * mi + (1 - beta) * di);
				} else {
					child1.setGene(i, beta * mi + (1 - beta) * di);
					child2.setGene(i, beta * di + (1 - beta) * mi);
				}
			}
			individuals[childIndex] = child1;
			individuals[childIndex + 1] = child2;
			childIndex += 2;
		}

	}

	// elitism: don't mutate the top one
	public void mutate(final double mutationRate) {
		// randomly select a number of individual to mutate based on the mutation rate
		int m = (int) Math.round(mutationRate * (individuals.length - 1));
		if (m == 0) { // ensure at least one mutant?
			m = 1;
		}
		toMutate.clear();
		toMutateOriginal.clear();
		while (toMutate.size() < m) {
			final int k = (int) (1 + Math.random() * (individuals.length - 2));
			if (!toMutate.contains(individuals[k])) {
				toMutate.add(individuals[k]);
				toMutateOriginal.add(new Individual(individuals[k]));
			}
		}
		// randomly select a gene of a picked individual to mutate (only one gene to mutate at a time)
		for (final Individual i : toMutate) {
			final int n = (int) (Math.random() * (i.getChromosomeLength() - 1));
			i.setGene(n, Math.random());
		}
	}

	public void undoMutation() {
		if (toMutateOriginal.isEmpty() || toMutate.isEmpty()) {
			return;
		}
		for (final Individual ind : toMutate) {
			for (int j = 0; j < ind.getChromosomeLength(); j++) {
				ind.setGene(j, toMutateOriginal.get(toMutate.indexOf(ind)).getGene(j));
			}
		}
	}

}
