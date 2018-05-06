package org.concord.energy3d.geneticalgorithms.applications;

/**
 * @author Charles Xie
 *
 */
public abstract class NetEnergyOptimizer extends Optimizer {

	public NetEnergyOptimizer(final int populationSize, final int chromosomeLength, final int selectionMethod, final double convergenceThreshold) {
		super(populationSize, chromosomeLength, selectionMethod, convergenceThreshold);
	}

	@Override
	public void setOjectiveFunction(final int objectiveFunctionType) {
		objectiveFunction = new NetEnergyObjectiveFunction(objectiveFunctionType, foundation);
	}

}
