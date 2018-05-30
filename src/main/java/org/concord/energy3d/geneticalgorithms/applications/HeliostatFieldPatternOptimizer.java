package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.EventQueue;
import java.util.Calendar;

import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.geneticalgorithms.ObjectiveFunction;
import org.concord.energy3d.gui.CspProjectDailyEnergyGraph;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HeliostatSpiralFieldLayout;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;

/**
 * Chromosome of an individual is encoded as follows:
 * 
 * aperture width (w), aperture height (h), divergence angle (d), radial expansion (e), start angle (t1), end angle (t2)
 * 
 * @author Charles Xie
 *
 */
public class HeliostatFieldPatternOptimizer extends SolarOutputOptimizer {

	private double divergenceAngle = Math.toDegrees(HeliostatSpiralFieldLayout.GOLDEN_ANGLE);
	private double minimumDivergenceAngle = 0;
	private double maximumDivergenceAngle = 180;
	private double radialExpansion;
	private double minimumRadialExpansion = 0;
	private double maximumRadialExpansion = 0.01;
	private double minimumApertureWidth = 1;
	private double maximumApertureWidth = 10;
	private double minimumApertureHeight = 1;
	private double maximumApertureHeight = 2;

	public HeliostatFieldPatternOptimizer(final int populationSize, final int chromosomeLength, final int selectionMethod, final double convergenceThreshold, final int discretizationSteps) {
		super(populationSize, chromosomeLength, selectionMethod, convergenceThreshold, discretizationSteps);
	}

	@Override
	public void setFoundation(final Foundation foundation) {
		super.setFoundation(foundation);
		final Mirror heliostat = foundation.getHeliostats().get(0);
		maximumApertureHeight = heliostat.getBaseHeight();
		// initialize the population with the first-born being the current design
		final Individual firstBorn = population.getIndividual(0);
		firstBorn.setGene(0, (heliostat.getApertureWidth() - minimumApertureWidth) / (maximumApertureWidth - minimumApertureWidth));
		firstBorn.setGene(1, (heliostat.getApertureHeight() - minimumApertureHeight) / (maximumApertureHeight - minimumApertureHeight));
		firstBorn.setGene(2, (divergenceAngle - minimumDivergenceAngle) / (maximumDivergenceAngle - minimumDivergenceAngle));
		firstBorn.setGene(3, (radialExpansion - minimumRadialExpansion) / (maximumRadialExpansion - minimumRadialExpansion));
	}

	public void setMinimumApertureWidth(final double minimumApertureWidth) {
		this.minimumApertureWidth = minimumApertureWidth;
	}

	public void setMaximumApertureWidth(final double maximumApertureWidth) {
		this.maximumApertureWidth = maximumApertureWidth;
	}

	public void setMinimumApertureHeight(final double minimumApertureHeight) {
		this.minimumApertureHeight = minimumApertureHeight;
	}

	public void setMaximumApertureHeight(final double maximumApertureHeight) {
		this.maximumApertureHeight = maximumApertureHeight;
	}

	public void setMinimumDivergenceAngle(final double minimumDivergenceAngle) {
		this.minimumDivergenceAngle = minimumDivergenceAngle;
	}

	public void setMaximumDivergeneAngle(final double maximumDivergenceAngle) {
		this.maximumDivergenceAngle = maximumDivergenceAngle;
	}

	public void setDivergenceAngle(final double divergenceAngle) {
		this.divergenceAngle = divergenceAngle;
	}

	public void setMinimumRadialExpansion(final double minimumRadialExpansion) {
		this.minimumRadialExpansion = minimumRadialExpansion;
	}

	public void setMaximumRadialExpansion(final double maximumRadialExpansion) {
		this.maximumRadialExpansion = maximumRadialExpansion;
	}

	public void setRadialExpansion(final double radialExpansion) {
		this.radialExpansion = radialExpansion;
	}

	@Override
	void computeIndividualFitness(final Individual individual) {
		final HeliostatSpiralFieldLayout layout = new HeliostatSpiralFieldLayout();
		layout.setApertureWidth(minimumApertureWidth + individual.getGene(0) * (maximumApertureWidth - minimumApertureWidth));
		layout.setApertureHeight(minimumApertureHeight + individual.getGene(1) * (maximumApertureHeight - minimumApertureHeight));
		layout.setDivergence(minimumDivergenceAngle + individual.getGene(2) * (maximumDivergenceAngle - minimumDivergenceAngle));
		layout.setRadialExpansionRatio(minimumRadialExpansion + individual.getGene(3) * (maximumRadialExpansion - minimumRadialExpansion));
		foundation.addHeliostatSpiralField(layout);
		final double output = objectiveFunction.compute();
		individual.setFitness(output);
	}

	@Override
	public void applyFittest() {
		final Individual best = population.getFittest();
		final HeliostatSpiralFieldLayout layout = new HeliostatSpiralFieldLayout();
		layout.setApertureWidth(minimumApertureWidth + best.getGene(0) * (maximumApertureWidth - minimumApertureWidth));
		layout.setApertureHeight(minimumApertureHeight + best.getGene(1) * (maximumApertureHeight - minimumApertureHeight));
		layout.setDivergence(minimumDivergenceAngle + best.getGene(2) * (maximumDivergenceAngle - minimumDivergenceAngle));
		layout.setRadialExpansionRatio(minimumRadialExpansion + best.getGene(3) * (maximumRadialExpansion - minimumRadialExpansion));
		foundation.addHeliostatSpiralField(layout);
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

	@Override
	public void displayFittest() {
		final Individual best = population.getIndividual(0);
		final Foundation receiver = foundation.getHeliostats().get(0).getReceiver();
		if (receiver != null) {
			String s = null;
			switch (objectiveFunction.getType()) {
			case ObjectiveFunction.DAILY:
				s = "Daily Output: " + EnergyPanel.TWO_DECIMALS.format(best.getFitness());
				break;
			case ObjectiveFunction.ANNUAl:
				s = "Annual Output: " + EnergyPanel.ONE_DECIMAL.format(best.getFitness() * 365.0 / 12.0);
				break;
			}
			receiver.setLabelCustomText(s);
			receiver.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	void updateInfo(final Individual individual) {
		final Individual best = population.getIndividual(0);
		final Foundation receiver = foundation.getHeliostats().get(0).getReceiver();
		if (receiver != null) {
			String s = null;
			switch (objectiveFunction.getType()) {
			case ObjectiveFunction.DAILY:
				s = "Daily Output\nCurrent: " + EnergyPanel.TWO_DECIMALS.format(individual.getFitness()) + ", Top: " + EnergyPanel.TWO_DECIMALS.format(best.getFitness());
				break;
			case ObjectiveFunction.ANNUAl:
				s = "Annual Output\nCurrent: " + EnergyPanel.ONE_DECIMAL.format(individual.getFitness() * 365.0 / 12.0) + ", Top: " + EnergyPanel.ONE_DECIMAL.format(best.getFitness() * 365.0 / 12.0);
				break;
			}
			receiver.setLabelCustomText(s);
			receiver.draw();
		}
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final Calendar today = Heliodon.getInstance().getCalendar();
				EnergyPanel.getInstance().getDateSpinner().setValue(today.getTime());
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (selectedPart instanceof Foundation) {
					final CspProjectDailyEnergyGraph g = EnergyPanel.getInstance().getCspProjectDailyEnergyGraph();
					g.setCalendar(today);
					EnergyPanel.getInstance().getCspProjectTabbedPane().setSelectedComponent(g);
					if (g.hasGraph()) {
						g.updateGraph();
					} else {
						g.addGraph((Foundation) selectedPart);
					}
				}
			}
		});
	}

}
