package org.concord.energy3d.geneticalgorithms.applications;

/**
 * @author Charles Xie
 */
public abstract class NetEnergyOptimizer extends Optimizer {

    NetEnergyOptimizer(final int populationSize, final int chromosomeLength, final int discretizationSteps) {
        super(populationSize, chromosomeLength, discretizationSteps);
    }

    @Override
    public void setOjectiveFunction(final int objectiveFunctionType) {
        objectiveFunction = new NetEnergyObjectiveFunction(objectiveFunctionType, foundation);
    }

}