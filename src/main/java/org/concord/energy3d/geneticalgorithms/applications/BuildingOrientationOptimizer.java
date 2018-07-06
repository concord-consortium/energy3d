package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.EventQueue;
import java.util.Calendar;
import java.util.Random;

import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.geneticalgorithms.ObjectiveFunction;
import org.concord.energy3d.gui.BuildingDailyEnergyGraph;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;

import com.ardor3d.math.MathUtils;

/**
 * Chromosome of an individual is encoded as follows:
 * 
 * foundation[0].azimuth (in degrees)
 * 
 * @author Charles Xie
 *
 */
public class BuildingOrientationOptimizer extends NetEnergyOptimizer {

	public BuildingOrientationOptimizer(final int populationSize, final int chromosomeLength, final int discretizationSteps) {
		super(populationSize, chromosomeLength, discretizationSteps);
		setGeneName(0, "Azimuth");
	}

	@Override
	public void setFoundation(final Foundation foundation) {
		super.setFoundation(foundation);
		// initialize the population with the first-born being the current design
		final Individual firstBorn = population.getIndividual(0);
		final double normalizedValue = foundation.getAzimuth() / 360.0;
		if (normalizedValue < 0 || normalizedValue > 1) {
			throw new RuntimeException("Foundation azimuth out of range");
		}
		firstBorn.setGene(0, normalizedValue);
		if (searchMethod == LOCAL_SEARCH_RANDOM_OPTIMIZATION) {
			final Random random = new Random();
			for (int i = 1; i < population.size(); i++) {
				final Individual individual = population.getIndividual(i);
				double v = random.nextGaussian() * localSearchRadius + normalizedValue;
				while (v < 0 || v > 1) {
					v = random.nextGaussian() * localSearchRadius + normalizedValue;
				}
				individual.setGene(0, v);
			}
		}
	}

	@Override
	void computeIndividualFitness(final Individual individual) {
		final double gene = individual.getGene(0);
		foundation.setAzimuth(gene * 360);
		// final double rawObjective = objectiveFunction.compute();
		final double rawObjective = Math.abs(Math.sin(2 * gene * MathUtils.TWO_PI)); // test function
		if (searchMethod == GLOBAL_SEARCH_FITNESS_SHARING) {
			final double nc = population.getNicheCount(individual, fitnessSharingRadius);
			System.out.println("****" + nc + ", " + rawObjective);
			individual.setFitness((rawObjective) / nc);
		} else {
			individual.setFitness(rawObjective);
		}
	}

	@Override
	public void applyFittest() {
		final Individual best = population.getFittest();
		final double gene = best.getGene(0);
		foundation.setAzimuth(gene * 360);
		displayFittest();
		foundation.draw();
		System.out.println("Fittest: " + individualToString(best));
		SceneManager.getInstance().refresh();
	}

	@Override
	String individualToString(final Individual individual) {
		return "(" + individual.getGene(0) * 360 + ") = " + individual.getFitness();
	}

	@Override
	public void displayFittest() {
		final Individual best = population.getIndividual(0);
		String s = null;
		switch (objectiveFunction.getType()) {
		case ObjectiveFunction.DAILY:
			s = "Daily Energy Use: " + EnergyPanel.ONE_DECIMAL.format(-best.getFitness());
			break;
		case ObjectiveFunction.ANNUAl:
			s = "Annual Energy Use: " + EnergyPanel.ONE_DECIMAL.format(-best.getFitness() * 365.0 / 12.0);
			break;
		}
		foundation.setLabelCustomText(s);
	}

	@Override
	void updateInfo(final Individual individual) {
		final Individual best = population.getIndividual(0);
		String s = null;
		switch (objectiveFunction.getType()) {
		case ObjectiveFunction.DAILY:
			s = "Daily Energy Use\nCurrent: " + EnergyPanel.ONE_DECIMAL.format(-individual.getFitness()) + ", Top: " + EnergyPanel.ONE_DECIMAL.format(-best.getFitness());
			break;
		case ObjectiveFunction.ANNUAl:
			s = "Annual Energy Use\nCurrent: " + EnergyPanel.ONE_DECIMAL.format(-individual.getFitness() * 365.0 / 12.0) + "\nTop: " + EnergyPanel.ONE_DECIMAL.format(-best.getFitness() * 365.0 / 12.0);
			break;
		}
		foundation.setLabelCustomText(s);
		foundation.draw();
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final Calendar today = Heliodon.getInstance().getCalendar();
				EnergyPanel.getInstance().getDateSpinner().setValue(today.getTime());
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (selectedPart instanceof Foundation) {
					final BuildingDailyEnergyGraph g = EnergyPanel.getInstance().getBuildingDailyEnergyGraph();
					g.setCalendar(today);
					EnergyPanel.getInstance().getBuildingTabbedPane().setSelectedComponent(g);
					if (g.hasGraph()) {
						g.updateGraph();
					} else {
						g.addGraph((Foundation) selectedPart);
					}
				}
			}
		});
	}

	private static BuildingOrientationOptimizerMaker maker;

	public static void make(final Foundation foundation) {
		if (maker == null) {
			maker = new BuildingOrientationOptimizerMaker();
		}
		maker.make(foundation);
	}

}
