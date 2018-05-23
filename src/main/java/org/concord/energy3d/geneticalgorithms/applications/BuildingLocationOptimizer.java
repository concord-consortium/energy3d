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
		firstBorn.setGene(0, (center.getX() * Scene.getInstance().getScale() - xmin) / (xmax - xmin));
		firstBorn.setGene(1, (center.getY() * Scene.getInstance().getScale() - ymin) / (ymax - ymin));
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
	void updateInfo() {
		switch (objectiveFunction.getType()) {
		case ObjectiveFunction.DAILY:
			foundation.setLabelCustomText("Daily Energy Use = " + EnergyPanel.TWO_DECIMALS.format(population.getIndividual(0).getFitness()));
			break;
		case ObjectiveFunction.ANNUAl:
			foundation.setLabelCustomText("Annual Energy Use = " + EnergyPanel.ONE_DECIMAL.format(population.getIndividual(0).getFitness() * 365.0 / 12.0));
			break;
		case ObjectiveFunction.RANDOM:
			foundation.setLabelCustomText(null);
			break;
		}
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
