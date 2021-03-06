package org.concord.energy3d.geneticalgorithms.applications;

import org.concord.energy3d.model.Foundation;

/**
 * @author Charles Xie
 */
public abstract class OptimizerMaker {

    int selectedSearchMethod = Optimizer.GLOBAL_SEARCH_UNIFORM_SELECTION;
    double localSearchRadius = 0.1;
    double sharingRadius = 0.1;
    int selectedObjectiveFunction = 0;
    int selectedSelectionMethod = 0;
    int populationSize = 20;
    int maximumGenerations = 5;
    double convergenceThreshold = 0.01;
    double mutationRate = 0.1;
    Optimizer op;

    public abstract void make(Foundation foundation);

    public void stop() {
        if (op != null) {
            op.stop();
        }
    }

    public void run(final Foundation foundation, final boolean local, final boolean daily, final boolean profit,
                    final int population, final int generations, final float mutation, final float convergence, final float searchRadius) {
        // stub
    }

}