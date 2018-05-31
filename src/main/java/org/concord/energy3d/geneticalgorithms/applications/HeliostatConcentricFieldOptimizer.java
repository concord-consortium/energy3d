package org.concord.energy3d.geneticalgorithms.applications;

import java.util.List;

import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.geneticalgorithms.ObjectiveFunction;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HeliostatConcentricFieldLayout;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.Scene;

/**
 * Chromosome of an individual is encoded as follows:
 * 
 * aperture width (0), aperture height (1), azimuthal spacing (2), radial spacing (3), radial expansion (4), start angle (t1), end angle (t2)
 * 
 * @author Charles Xie
 *
 */
public class HeliostatConcentricFieldOptimizer extends HeliostatFieldOptimizer {

	private static double azimuthalSpacing = 1; // cache for next run
	private double minimumAzimuthalSpacing = 0;
	private double maximumAzimuthalSpacing = 10;

	private static double radialSpacing = 1; // cache for next run
	private double minimumRadialSpacing = 0;
	private double maximumRadialSpacing = 5;

	private static double radialExpansion; // cache for next run

	public HeliostatConcentricFieldOptimizer(final int populationSize, final int chromosomeLength, final int selectionMethod, final double convergenceThreshold, final int discretizationSteps) {
		super(populationSize, chromosomeLength, selectionMethod, convergenceThreshold, discretizationSteps);
	}

	@Override
	public void setFoundation(final Foundation foundation) {
		super.setFoundation(foundation);
		final Mirror heliostat = foundation.getHeliostats().get(0);
		maximumApertureHeight = heliostat.getBaseHeight() * Scene.getInstance().getScale();
		// initialize the population with the first-born being the current design
		final Individual firstBorn = population.getIndividual(0);
		firstBorn.setGene(0, (heliostat.getApertureWidth() - minimumApertureWidth) / (maximumApertureWidth - minimumApertureWidth));
		firstBorn.setGene(1, (heliostat.getApertureHeight() - minimumApertureHeight) / (maximumApertureHeight - minimumApertureHeight));
		firstBorn.setGene(2, (azimuthalSpacing - minimumAzimuthalSpacing) / (maximumAzimuthalSpacing - minimumAzimuthalSpacing));
		firstBorn.setGene(3, (radialSpacing - minimumRadialSpacing) / (maximumRadialSpacing - minimumRadialSpacing));
		firstBorn.setGene(4, (radialExpansion - minimumRadialExpansion) / (maximumRadialExpansion - minimumRadialExpansion));
	}

	public void setMinimumAzimuthalSpacing(final double minimumAzimuthalSpacing) {
		this.minimumAzimuthalSpacing = minimumAzimuthalSpacing;
	}

	public void setMaximumAzimuthalSpacing(final double maximumAzimuthalSpacing) {
		this.maximumAzimuthalSpacing = maximumAzimuthalSpacing;
	}

	public void setMinimumRadialSpacing(final double minimumRadialSpacing) {
		this.minimumRadialSpacing = minimumRadialSpacing;
	}

	public void setMaximumRadialSpacing(final double maximumRadialSpacing) {
		this.maximumRadialSpacing = maximumRadialSpacing;
	}

	@Override
	void computeIndividualFitness(final Individual individual) {
		final HeliostatConcentricFieldLayout layout = new HeliostatConcentricFieldLayout();
		layout.setApertureWidth(minimumApertureWidth + individual.getGene(0) * (maximumApertureWidth - minimumApertureWidth));
		layout.setApertureHeight(minimumApertureHeight + individual.getGene(1) * (maximumApertureHeight - minimumApertureHeight));
		layout.setAzimuthalSpacing(minimumAzimuthalSpacing + individual.getGene(2) * (maximumAzimuthalSpacing - minimumAzimuthalSpacing));
		layout.setRadialSpacing(minimumRadialSpacing + individual.getGene(3) * (maximumRadialSpacing - minimumRadialSpacing));
		layout.setRadialExpansionRatio(minimumRadialExpansion + individual.getGene(4) * (maximumRadialExpansion - minimumRadialExpansion));
		foundation.generateHeliostatField(layout);
		final List<Mirror> heliostats = foundation.getHeliostats();
		if (heliostats.isEmpty()) {
			individual.setFitness(-Double.MAX_VALUE); // unfit
		} else {
			final double output = objectiveFunction.compute();
			final Mirror heliostat = heliostats.get(0);
			final double totalApertureArea = heliostats.size() * heliostat.getApertureWidth() * heliostat.getApertureHeight();
			if (netProfit) {
				double cost = dailyCostPerApertureSquareMeter;
				if (objectiveFunction.getType() == ObjectiveFunction.ANNUAl) {
					cost *= 12;
				}
				individual.setFitness(output * pricePerKWh - cost * totalApertureArea);
			} else if (outputPerApertureSquareMeter) {
				individual.setFitness(output / totalApertureArea);
			} else {
				individual.setFitness(output);
			}
		}
	}

	@Override
	public void applyFittest() {
		final Individual best = population.getFittest();
		final HeliostatConcentricFieldLayout layout = new HeliostatConcentricFieldLayout();
		layout.setApertureWidth(minimumApertureWidth + best.getGene(0) * (maximumApertureWidth - minimumApertureWidth));
		layout.setApertureHeight(minimumApertureHeight + best.getGene(1) * (maximumApertureHeight - minimumApertureHeight));
		azimuthalSpacing = minimumAzimuthalSpacing + best.getGene(2) * (maximumAzimuthalSpacing - minimumAzimuthalSpacing);
		layout.setAzimuthalSpacing(azimuthalSpacing);
		radialSpacing = minimumRadialSpacing + best.getGene(3) * (maximumRadialSpacing - minimumRadialSpacing);
		layout.setRadialSpacing(radialSpacing);
		radialExpansion = minimumRadialExpansion + best.getGene(4) * (maximumRadialExpansion - minimumRadialExpansion);
		layout.setRadialExpansionRatio(radialExpansion);
		foundation.generateHeliostatField(layout);
		System.out.println("Fittest: " + individualToString(best));
		displayFittest();
	}

	@Override
	String individualToString(final Individual individual) {
		String s = "(";
		s += (minimumApertureWidth + individual.getGene(0) * (maximumApertureWidth - minimumApertureWidth)) + ", ";
		s += (minimumApertureHeight + individual.getGene(1) * (maximumApertureHeight - minimumApertureHeight)) + ", ";
		s += (minimumAzimuthalSpacing + individual.getGene(2) * (maximumAzimuthalSpacing - minimumAzimuthalSpacing)) + ", ";
		s += (minimumRadialSpacing + individual.getGene(3) * (maximumRadialSpacing - minimumRadialSpacing)) + ", ";
		s += (minimumRadialExpansion + individual.getGene(4) * (maximumRadialExpansion - minimumRadialExpansion)) + ", ";
		return s.substring(0, s.length() - 2) + ") = " + individual.getFitness();
	}

}
