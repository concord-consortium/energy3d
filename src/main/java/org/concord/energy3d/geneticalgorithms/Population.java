package org.concord.energy3d.geneticalgorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.concord.energy3d.util.Util;

/**
 * This class implements simple genetic algorithm (SGA) and micro genetic algorithm (MGA).
 * 
 * @author Charles Xie
 *
 */
public class Population {

	public final static int ROULETTE_WHEEL = 0;
	public final static int TOURNAMENT = 1;

	private final Individual[] individuals;
	private final Individual[] savedGeneration;
	private final boolean[] violations;
	private double beta = 0.5;
	private final List<Individual> survivors = new ArrayList<Individual>();
	private final List<Individual> mutants = new ArrayList<Individual>();
	private int selectionMethod = ROULETTE_WHEEL;
	private double convergenceThreshold = 0.01;
	private int discretizationSteps;

	public Population(final int populationSize, final int chromosomeLength, final int discretizationSteps) {
		individuals = new Individual[populationSize];
		savedGeneration = new Individual[populationSize];
		violations = new boolean[populationSize];
		for (int i = 0; i < individuals.length; i++) {
			individuals[i] = new Individual(chromosomeLength, discretizationSteps);
			savedGeneration[i] = new Individual(chromosomeLength, discretizationSteps);
			violations[i] = false;
		}
	}

	public double getNicheCount(final Individual selected, final double sigma) {
		double nicheCount = 0;
		for (final Individual i : individuals) {
			final double d = selected.distance(i);
			double share = 0;
			if (d < sigma) {
				share = 1.0 - d / sigma;
			}
			nicheCount += share;
		}
		return nicheCount;
	}

	public void setSelectionMethod(final int selectionMethod) {
		this.selectionMethod = selectionMethod;
	}

	public int getSelectionMethod() {
		return selectionMethod;
	}

	public void setConvergenceThreshold(final double convergenceThreshold) {
		this.convergenceThreshold = convergenceThreshold;
	}

	public double getConvergenceThreshold() {
		return convergenceThreshold;
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

	public Individual[] getIndividuals() {
		return individuals;
	}

	public void sort() {
		Arrays.sort(individuals);
	}

	public void setViolation(final int i, final boolean b) {
		violations[i] = b;
	}

	public void saveGenes() {
		for (int i = 0; i < individuals.length; i++) {
			savedGeneration[i].copyGenes(individuals[i]);
			violations[i] = false;
		}
	}

	public void restoreGenes() {
		for (int i = 0; i < individuals.length; i++) {
			if (violations[i]) {
				individuals[i].copyGenes(savedGeneration[i]);
			}
		}
	}

	public Individual getFittest() {
		double max = -Double.MAX_VALUE;
		Individual best = null;
		for (final Individual i : individuals) {
			if (Double.isNaN(i.getFitness())) { // fitness not computed yet, skip
				continue;
			}
			if (i.getFitness() > max) {
				max = i.getFitness();
				best = i;
			}
		}
		return best;
	}

	/* Implement micro genetic algorithm (MGA) */

	public void restartMGA() {
		individuals[0] = getFittest();
		for (int k = 1; k < individuals.length; k++) {
			individuals[k] = new Individual(individuals[0].getChromosomeLength(), discretizationSteps);
		}
	}

	public void runMGA() {
		final int size = individuals.length;
		if (size < 5) {
			throw new RuntimeException("Must have at least five individuals for micro GA");
		}
		sort();
		final int n = individuals[0].getChromosomeLength();
		final Individual[] originals = new Individual[size];
		for (int k = 0; k < size; k++) {
			originals[k] = new Individual(individuals[k]);
		}
		for (int k = 1; k < size; k++) {
			int i = (int) Math.round(Math.random() * (size - 1));
			int j;
			do {
				j = (int) Math.round(Math.random() * (size - 1));
			} while (j == i);
			final int d = individuals[i].getFitness() > individuals[j].getFitness() ? i : j;
			i = (int) Math.round(Math.random() * (size - 1));
			do {
				j = (int) Math.round(Math.random() * (size - 1));
			} while (j == i);
			int m = individuals[i].getFitness() > individuals[j].getFitness() ? i : j;
			if (m == d) {// just select dad's neighbor
				if (d == 0) {
					m = 1;
				} else if (d == size - 1) {
					m = size - 2;
				} else {
					m = Math.random() < 0.5 ? d - 1 : d + 1;
				}
			}
			beta = Math.random();
			for (i = 0; i < n; i++) {
				final double di = originals[d].getGene(i);
				final double mi = originals[m].getGene(i);
				individuals[k].setGene(i, beta * di + (1 - beta) * mi);
			}
		}
	}

	// check convergence bitwisely (so-called nominal convergence)
	public boolean isMGAConverged() {
		final int n = getChromosomeLength();
		for (int i = 0; i < n; i++) {
			double average = 0;
			for (int j = 0; j < individuals.length; j++) {
				average += individuals[j].getGene(i);
			}
			average /= individuals.length;
			for (int j = 0; j < individuals.length; j++) {
				if (Math.abs(individuals[j].getGene(i) / average - 1.0) > convergenceThreshold) {
					return false;
				}
			}
		}
		return true;
	}

	/* Implement simple genetic algorithm (SGA) */

	public void runSGA(final double selectionRate, final double crossoverRate) {
		selectSurvivors(selectionRate);
		crossover(crossoverRate);
	}

	// select the survivors based on elitism specified by the rate of selection
	private void selectSurvivors(final double selectionRate) {
		survivors.clear();
		sort();
		final int imax = (int) (selectionRate * individuals.length);
		for (int i = 0; i < imax; i++) {
			survivors.add(individuals[i]);
		}
	}

	// select a parent by the roulette wheel rule (fitness proportionate selection)
	private Parents selectParentsByRouletteWheel(final double lowestFitness, final double sumOfFitness) {
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
		do {
			roulettWheelPosition = Math.random() * sumOfFitness;
			spinWheel = 0;
			for (final Individual s : survivors) {
				spinWheel += s.getFitness() - lowestFitness;
				if (spinWheel >= roulettWheelPosition) {
					if (s != dad) {
						mom = s;
					}
					break;
				}
			}
		} while (mom == null);
		return new Parents(dad, mom);
	}

	// select a parent by tournament
	private Parents selectParentsByTournament() {
		final int numberOfSurvivers = survivors.size();
		if (numberOfSurvivers <= 1) {
			throw new RuntimeException("Must have at least two survivors to be used as parents");
		}
		final int n1 = numberOfSurvivers - 1;

		// find dad first
		int i = (int) Math.round(Math.random() * n1);
		int j;
		do {
			j = (int) Math.round(Math.random() * n1);
		} while (j == i);
		final int d = survivors.get(i).getFitness() > survivors.get(j).getFitness() ? i : j;

		// now find mom
		i = (int) Math.round(Math.random() * n1);
		do {
			j = (int) Math.round(Math.random() * n1);
		} while (j == i);
		int m = survivors.get(i).getFitness() > survivors.get(j).getFitness() ? i : j;

		// if mom is the same with dad, try again until otherwise
		while (m == d) {
			i = (int) Math.round(Math.random() * n1);
			do {
				j = (int) Math.round(Math.random() * n1);
			} while (j == i);
			m = survivors.get(i).getFitness() > survivors.get(j).getFitness() ? i : j;
		}

		return new Parents(survivors.get(d), survivors.get(m));

	}

	// uniform crossover
	private void crossover(final double crossoverRate) {
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
			final Parents p;
			switch (selectionMethod) {
			case TOURNAMENT:
				p = selectParentsByTournament();
				break;
			default:
				p = selectParentsByRouletteWheel(lowestFitness, sumOfFitness);
			}
			if (!oldFolks.contains(p)) {
				oldFolks.add(p);
			}
		}

		int childIndex = numberOfSurvivers;
		for (final Parents p : oldFolks) {
			final int n = p.dad.getChromosomeLength();
			final Individual child1 = new Individual(n, discretizationSteps);
			final Individual child2 = new Individual(n, discretizationSteps);
			beta = Math.random();
			for (int i = 0; i < n; i++) {
				final double di = p.dad.getGene(i);
				final double mi = p.mom.getGene(i);
				if (Math.random() < crossoverRate) {
					child1.setGene(i, beta * di + (1 - beta) * mi);
					child2.setGene(i, beta * mi + (1 - beta) * di);
				} else {
					child1.setGene(i, beta * mi + (1 - beta) * di);
					child2.setGene(i, beta * di + (1 - beta) * mi);
				}
			}
			if (childIndex < individuals.length) {
				individuals[childIndex] = child1;
			}
			if (childIndex + 1 < individuals.length) {
				individuals[childIndex + 1] = child2;
			}
			childIndex += 2;
		}

	}

	public void mutate(final double mutationRate) {
		if (Util.isZero(mutationRate)) {
			return;
		}
		// randomly select a number of individual to mutate based on the mutation rate
		int m = (int) Math.round(mutationRate * (individuals.length - 1));
		if (m == 0) { // ensure at least one mutant?
			m = 1;
		}
		mutants.clear();
		while (mutants.size() < m) {
			final int k = (int) (1 + Math.random() * (individuals.length - 2)); // elitism: don't mutate the top one
			if (!mutants.contains(individuals[k])) {
				mutants.add(individuals[k]);
			}
		}
		// randomly select a gene of a picked individual to mutate (only one gene to mutate at a time)
		for (final Individual i : mutants) {
			final int n = (int) (Math.random() * (i.getChromosomeLength() - 1));
			i.setGene(n, Math.random());
		}
	}

	// check convergence bitwisely (so-called nominal convergence)
	public boolean isSGAConverged() {
		if (survivors.size() < 2) {
			return true;
		}
		final int n = getChromosomeLength();
		final int m = Math.max(2, survivors.size() / 2);
		for (int i = 0; i < n; i++) {
			double average = 0;
			for (int j = 0; j < m; j++) {
				average += survivors.get(j).getGene(i);
			}
			average /= m;
			for (int j = 0; j < m; j++) {
				if (Math.abs(survivors.get(j).getGene(i) / average - 1.0) > convergenceThreshold) {
					return false;
				}
			}
		}
		return true;
	}

}
