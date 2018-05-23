package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.EventQueue;
import java.util.Calendar;

import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.geneticalgorithms.ObjectiveFunction;
import org.concord.energy3d.gui.BuildingDailyEnergyGraph;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;

import com.ardor3d.math.Vector3;

/**
 * Chromosome of an individual is encoded as follows:
 * 
 * foundation[0].center.x, foundation[0].center.y, ..., foundation[n].center.x, foundation[n].center.y
 * 
 * @author Charles Xie
 *
 */
public class BuildingLocationOptimizer extends NetEnergyOptimizer {

	private double xmin = -30;
	private double xmax = 30;
	private double ymin = -30;
	private double ymax = 30;

	public BuildingLocationOptimizer(final int populationSize, final int chromosomeLength, final int selectionMethod, final double convergenceThreshold, final int discretizationSteps) {
		super(populationSize, chromosomeLength, selectionMethod, convergenceThreshold, discretizationSteps);
	}

	public void setMinimumX(final double xmin) {
		this.xmin = xmin;
	}

	public double getMinimumX() {
		return xmin;
	}

	public void setMaximumX(final double xmax) {
		this.xmax = xmax;
	}

	public double getMaximumX() {
		return xmax;
	}

	public void setMinimumY(final double ymin) {
		this.ymin = ymin;
	}

	public double getMinimumY() {
		return ymin;
	}

	public void setMaximumY(final double ymax) {
		this.ymax = ymax;
	}

	public double getMaximumY() {
		return ymax;
	}

	@Override
	public void setFoundation(final Foundation foundation) {
		super.setFoundation(foundation);
		// initialize the population with the first-born being the current design
		final Vector3 center = foundation.getAbsCenter();
		final Individual firstBorn = population.getIndividual(0);
		double normalizedValue = (center.getX() * Scene.getInstance().getScale() - xmin) / (xmax - xmin);
		if (normalizedValue < 0) {
			normalizedValue = 0;
		} else if (normalizedValue > 1) {
			normalizedValue = 1;
		}
		firstBorn.setGene(0, normalizedValue);
		normalizedValue = (center.getY() * Scene.getInstance().getScale() - ymin) / (ymax - ymin);
		if (normalizedValue < 0) {
			normalizedValue = 0;
		} else if (normalizedValue > 1) {
			normalizedValue = 1;
		}
		firstBorn.setGene(1, normalizedValue);
	}

	@Override
	void computeIndividualFitness(final Individual individual) {
		final double geneX = individual.getGene(0);
		final double geneY = individual.getGene(1);
		final Vector3 displacement = foundation.getAbsCenter();
		displacement.subtractLocal((xmin + geneX * (xmax - xmin)) / Scene.getInstance().getScale(), (ymin + geneY * (ymax - ymin)) / Scene.getInstance().getScale(), 0).negateLocal();
		foundation.move(displacement);
		individual.setFitness(objectiveFunction.compute());
	}

	@Override
	public void applyFittest() {
		final Individual best = population.getFittest();
		final double geneX = best.getGene(0);
		final double geneY = best.getGene(1);
		final Vector3 displacement = foundation.getAbsCenter();
		displacement.subtractLocal((xmin + geneX * (xmax - xmin)) / Scene.getInstance().getScale(), (ymin + geneY * (ymax - ymin)) / Scene.getInstance().getScale(), 0).negateLocal();
		foundation.move(displacement);
		foundation.draw();
		System.out.println("Fittest: " + individualToString(best));
	}

	@Override
	String individualToString(final Individual individual) {
		return "(" + (xmin + individual.getGene(0) * (xmax - xmin)) + ", " + (ymin + individual.getGene(1) * (ymax - ymin)) + ") = " + individual.getFitness();
	}

	@Override
	void updateInfo(final Individual individual) {
		final Individual best = population.getIndividual(0);
		String s = null;
		switch (objectiveFunction.getType()) {
		case ObjectiveFunction.DAILY:
			s = "Daily Energy Use";
			if (Double.isNaN(individual.getFitness())) {
				s += ": " + EnergyPanel.ONE_DECIMAL.format(-best.getFitness());
			} else {
				s += "\nCurrent: " + EnergyPanel.ONE_DECIMAL.format(-individual.getFitness()) + ", Top: " + EnergyPanel.ONE_DECIMAL.format(-best.getFitness());
			}
			break;
		case ObjectiveFunction.ANNUAl:
			s = "Annual Energy Use";
			if (Double.isNaN(individual.getFitness())) {
				s += ": " + EnergyPanel.ONE_DECIMAL.format(-best.getFitness() * 365.0 / 12.0);
			} else {
				s += "\nCurrent: " + EnergyPanel.ONE_DECIMAL.format(-individual.getFitness() * 365.0 / 12.0) + "\nTop: " + EnergyPanel.ONE_DECIMAL.format(-best.getFitness() * 365.0 / 12.0);
			}
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

}
