package org.concord.energy3d.geneticalgorithms.applications;

/**
 * @author Charles Xie
 */
public abstract class SolarOutputOptimizer extends Optimizer {

    SolarOutputOptimizer(final int populationSize, final int chromosomeLength, final int discretizationSteps) {
        super(populationSize, chromosomeLength, discretizationSteps);
    }

    @Override
    public void setOjectiveFunction(final int objectiveFunctionType) {
        objectiveFunction = new SolarOutputObjectiveFunction(objectiveFunctionType);
    }

}