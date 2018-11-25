package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.EventQueue;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.geneticalgorithms.ObjectiveFunction;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.PvProjectDailyEnergyGraph;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;

/**
 * Chromosome of an individual is encoded as follows:
 * 
 * rack[0].tiltAngle, ..., rack[n].tiltAngle
 * 
 * @author Charles Xie
 *
 */
public class SolarPanelTiltAngleOptimizer extends SolarOutputOptimizer {

	public SolarPanelTiltAngleOptimizer(final int populationSize, final int chromosomeLength, final int discretizationSteps) {
		super(populationSize, chromosomeLength, discretizationSteps);
	}

	@Override
	public void setFoundation(final Foundation foundation) {
		super.setFoundation(foundation);
		// initialize the population with the first-born being the current design
		final Random random = new Random();
		final Individual firstBorn = population.getIndividual(0);
		int i = 0;
		final List<Rack> racks = foundation.getRacks();
		for (final Rack r : racks) {
			final double normalizedValue = 0.5 * (1.0 + r.getTiltAngle() / 90.0);
			firstBorn.setGene(i, normalizedValue);
			if (searchMethod == LOCAL_SEARCH_RANDOM_OPTIMIZATION) {
				for (int k = 1; k < population.size(); k++) {
					final Individual individual = population.getIndividual(k);
					double v = random.nextGaussian() * localSearchRadius + normalizedValue;
					while (v < 0 || v > 1) {
						v = random.nextGaussian() * localSearchRadius + normalizedValue;
					}
					individual.setGene(i, v);
				}
			}
			setGeneName(i, "Tilt Angle (" + r.getId() + ")");
			setGeneMinimum(i, -90);
			setGeneMaximum(i, 90);
			setInitialGene(i, r.getTiltAngle());
			i++;
		}
	}

	@Override
	void computeIndividualFitness(final Individual individual) {
		final List<Rack> racks = foundation.getRacks();
		for (int i = 0; i < individual.getChromosomeLength(); i++) {
			final double gene = individual.getGene(i);
			final Rack rack = racks.get(i);
			rack.setTiltAngle((2 * gene - 1) * 90);
		}
		individual.setFitness(objectiveFunction.compute());
	}

	@Override
	public void applyFittest() {
		final List<Rack> racks = foundation.getRacks();
		final Individual best = population.getFittest();
		for (int i = 0; i < best.getChromosomeLength(); i++) {
			final double gene = best.getGene(i);
			final Rack rack = racks.get(i);
			rack.setTiltAngle((2 * gene - 1) * 90);
			rack.draw();
			setFinalGene(i, rack.getTiltAngle());
		}
		setFinalFitness(best.getFitness());
		System.out.println("Fittest: " + individualToString(best));
		displayFittest();
	}

	@Override
	String individualToString(final Individual individual) {
		String s = "(";
		for (int i = 0; i < individual.getChromosomeLength(); i++) {
			final double gene = individual.getGene(i);
			s += (2 * gene - 1) * 90 + ", ";
		}
		return s.substring(0, s.length() - 2) + ") = " + individual.getFitness();
	}

	@Override
	public void displayFittest() {
		final Individual best = population.getIndividual(0);
		String s = null;
		switch (objectiveFunction.getType()) {
		case ObjectiveFunction.DAILY:
			s = "Daily Output: " + EnergyPanel.TWO_DECIMALS.format(best.getFitness());
			break;
		case ObjectiveFunction.ANNUAL:
			s = "Annual Output: " + EnergyPanel.ONE_DECIMAL.format(best.getFitness() * 365.0 / 12.0);
			break;
		}
		foundation.setLabelCustomText(s);
		foundation.draw();
		SceneManager.getInstance().refresh();
		super.displayFittest();
	}

	@Override
	void updateInfo(final Individual individual) {
		final Individual best = population.getIndividual(0);
		String s = null;
		switch (objectiveFunction.getType()) {
		case ObjectiveFunction.DAILY:
			s = "Daily Output\nCurrent: " + EnergyPanel.TWO_DECIMALS.format(individual.getFitness()) + ", Top: " + EnergyPanel.TWO_DECIMALS.format(best.getFitness());
			break;
		case ObjectiveFunction.ANNUAL:
			s = "Annual Output\nCurrent: " + EnergyPanel.ONE_DECIMAL.format(individual.getFitness() * 365.0 / 12.0) + ", Top: " + EnergyPanel.ONE_DECIMAL.format(best.getFitness() * 365.0 / 12.0);
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
				if (selectedPart instanceof Foundation) { // synchronize with daily graph
					final PvProjectDailyEnergyGraph g = EnergyPanel.getInstance().getPvProjectDailyEnergyGraph();
					g.setCalendar(today);
					EnergyPanel.getInstance().getPvProjectTabbedPane().setSelectedComponent(g);
					if (g.hasGraph()) {
						g.updateGraph();
					} else {
						g.addGraph((Foundation) selectedPart);
					}
				}
			}
		});
	}

	private static SolarPanelTiltAngleOptimizerMaker maker;

	public static void make(final Foundation foundation) {
		if (maker == null) {
			maker = new SolarPanelTiltAngleOptimizerMaker();
		}
		maker.make(foundation);
	}

	public static void stopIt() {
		if (maker != null) {
			maker.stop();
		}
	}

	public static void runIt(final Foundation foundation, final boolean local, final boolean daily, final boolean profit, final int population, final int generations, final float mutation) {
		if (maker == null) {
			maker = new SolarPanelTiltAngleOptimizerMaker();
		}
		maker.run(foundation, local, daily, profit, population, generations, mutation);
	}

}
