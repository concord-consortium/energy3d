package org.concord.energy3d.geneticalgorithms.applications;

import java.util.List;

import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.geneticalgorithms.ObjectiveFunction;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HeliostatSpiralFieldLayout;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.Scene;

/**
 * Chromosome of an individual is encoded as follows:
 * 
 * aperture width (w), aperture height (h), divergence angle (d), radial expansion (e), start angle (t1), end angle (t2)
 * 
 * @author Charles Xie
 *
 */
public class HeliostatSpiralFieldOptimizer extends HeliostatFieldOptimizer {

	private static double divergenceAngle = Math.toDegrees(HeliostatSpiralFieldLayout.GOLDEN_ANGLE); // cache for the next run
	private double minimumDivergenceAngle = 5;
	private double maximumDivergenceAngle = 175;

	private static double radialExpansion; // cache for the next run

	public HeliostatSpiralFieldOptimizer(final int populationSize, final int chromosomeLength, final int discretizationSteps) {
		super(populationSize, chromosomeLength, discretizationSteps);
	}

	@Override
	public void setFoundation(final Foundation foundation) {
		super.setFoundation(foundation);
		final Mirror heliostat = foundation.getHeliostats().get(0);
		maximumApertureHeight = heliostat.getBaseHeight() * Scene.getInstance().getScale() * 2;
		// initialize the population with the first-born being the current design
		final Individual firstBorn = population.getIndividual(0);
		firstBorn.setGene(0, (heliostat.getApertureWidth() - minimumApertureWidth) / (maximumApertureWidth - minimumApertureWidth));
		firstBorn.setGene(1, (heliostat.getApertureHeight() - minimumApertureHeight) / (maximumApertureHeight - minimumApertureHeight));
		firstBorn.setGene(2, (divergenceAngle - minimumDivergenceAngle) / (maximumDivergenceAngle - minimumDivergenceAngle));
		firstBorn.setGene(3, (radialExpansion - minimumRadialExpansion) / (maximumRadialExpansion - minimumRadialExpansion));

		setGeneName(0, "Aperture Width");
		setInitialGene(0, heliostat.getApertureWidth());
		setGeneMinimum(0, minimumApertureWidth);
		setGeneMaximum(0, maximumApertureWidth);

		setGeneName(1, "Aperture Height");
		setInitialGene(1, heliostat.getApertureHeight());
		setGeneMinimum(1, minimumApertureHeight);
		setGeneMaximum(1, maximumApertureHeight);

		setGeneName(2, "Divergence Angle");
		setInitialGene(2, divergenceAngle);
		setGeneMinimum(2, minimumDivergenceAngle);
		setGeneMaximum(2, maximumDivergenceAngle);

		setGeneName(3, "Radial Expansion");
		setInitialGene(3, radialExpansion);
		setGeneMinimum(3, minimumRadialExpansion);
		setGeneMaximum(3, maximumRadialExpansion);

	}

	public void setMinimumDivergenceAngle(final double minimumDivergenceAngle) {
		this.minimumDivergenceAngle = minimumDivergenceAngle;
	}

	public void setMaximumDivergenceAngle(final double maximumDivergenceAngle) {
		this.maximumDivergenceAngle = maximumDivergenceAngle;
	}

	@Override
	void computeIndividualFitness(final Individual individual) {
		final HeliostatSpiralFieldLayout layout = new HeliostatSpiralFieldLayout();
		layout.setApertureWidth(minimumApertureWidth + individual.getGene(0) * (maximumApertureWidth - minimumApertureWidth));
		layout.setApertureHeight(minimumApertureHeight + individual.getGene(1) * (maximumApertureHeight - minimumApertureHeight));
		layout.setDivergence(minimumDivergenceAngle + individual.getGene(2) * (maximumDivergenceAngle - minimumDivergenceAngle));
		layout.setRadialExpansionRatio(minimumRadialExpansion + individual.getGene(3) * (maximumRadialExpansion - minimumRadialExpansion));
		foundation.generateHeliostatField(layout);
		final List<Mirror> heliostats = foundation.getHeliostats();
		if (heliostats.isEmpty()) { // sometimes the layout fails, so this individual is absolutely unfit
			individual.setFitness(-Double.MAX_VALUE);
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
		final HeliostatSpiralFieldLayout layout = new HeliostatSpiralFieldLayout();
		layout.setApertureWidth(minimumApertureWidth + best.getGene(0) * (maximumApertureWidth - minimumApertureWidth));
		layout.setApertureHeight(minimumApertureHeight + best.getGene(1) * (maximumApertureHeight - minimumApertureHeight));
		divergenceAngle = minimumDivergenceAngle + best.getGene(2) * (maximumDivergenceAngle - minimumDivergenceAngle);
		layout.setDivergence(divergenceAngle);
		radialExpansion = minimumRadialExpansion + best.getGene(3) * (maximumRadialExpansion - minimumRadialExpansion);
		layout.setRadialExpansionRatio(radialExpansion);
		foundation.generateHeliostatField(layout);
		setFinalGene(0, layout.getApertureWidth());
		setFinalGene(1, layout.getApertureHeight());
		setFinalGene(2, layout.getDivergence());
		setFinalGene(3, layout.getRadialExpansionRatio());
		setFinalFitness(best.getFitness());
		System.out.println("Fittest: " + individualToString(best));
		displayFittest();
	}

	@Override
	String individualToString(final Individual individual) {
		String s = "(";
		s += (minimumApertureWidth + individual.getGene(0) * (maximumApertureWidth - minimumApertureWidth)) + ", ";
		s += (minimumApertureHeight + individual.getGene(1) * (maximumApertureHeight - minimumApertureHeight)) + ", ";
		s += (minimumDivergenceAngle + individual.getGene(2) * (maximumDivergenceAngle - minimumDivergenceAngle)) + ", ";
		s += (minimumRadialExpansion + individual.getGene(3) * (maximumRadialExpansion - minimumRadialExpansion)) + ", ";
		return s.substring(0, s.length() - 2) + ") = " + individual.getFitness();
	}

	private static HeliostatSpiralFieldOptimizerMaker maker;

	public static void make(final Foundation foundation) {
		if (maker == null) {
			maker = new HeliostatSpiralFieldOptimizerMaker();
		}
		maker.make(foundation);
	}

}
