package org.concord.energy3d.geneticalgorithms;

/**
 * @author Charles Xie
 */
public class Individual implements Comparable<Individual> {

    private final double[] chromosome;
    private double fitness = Double.NaN; // store the fitness value evaluated by the objective function (NaN means not evaluated yet)

    /**
     * create an individual with the specified length and initialize its chromosome with random values between 0 and 1 with the discretization steps if specified as a positive number
     */
    public Individual(final int length, final int discretizationSteps) {
        this(length, true, discretizationSteps);
    }

    public Individual(final Individual original) {
        this(original.chromosome.length, false, 0);
        System.arraycopy(original.chromosome, 0, chromosome, 0, chromosome.length);
        fitness = original.fitness;
    }

    private Individual(final int length, final boolean randomize, final int discretizationSteps) {
        chromosome = new double[length];
        if (randomize) {
            if (discretizationSteps > 0) {
                for (int i = 0; i < length; i++) {
                    final double r = Math.random();
                    final int n = (int) (r * discretizationSteps);
                    chromosome[i] = (double) n / (double) discretizationSteps;
                }
            } else {
                for (int i = 0; i < length; i++) {
                    chromosome[i] = Math.random();
                }
            }
        }
    }

    /**
     * @return the Euclidean distance between the chromosomes (phenotypic only as we don't use genotypic (bit) coding)
     */
    public double distance(final Individual individual) {
        if (chromosome.length == 1) {
            return Math.abs(chromosome[0] - individual.chromosome[0]);
        }
        double sum = 0;
        for (int i = 0; i < chromosome.length; i++) {
            final double d = chromosome[i] - individual.chromosome[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    void copyGenes(final Individual original) {
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

    /**
     * highest fitness first, lowest fitness last
     */
    @Override
    public int compareTo(final Individual o) {
        return Double.compare(o.fitness, fitness);
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