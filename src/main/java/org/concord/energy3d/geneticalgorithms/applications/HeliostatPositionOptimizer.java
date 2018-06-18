package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.EventQueue;
import java.util.Calendar;
import java.util.List;

import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.geneticalgorithms.ObjectiveFunction;
import org.concord.energy3d.geneticalgorithms.RectangularBound;
import org.concord.energy3d.gui.CspProjectDailyEnergyGraph;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;

import com.ardor3d.math.Vector3;

/**
 * Chromosome of an individual is encoded as follows:
 * 
 * heliostat[0].x, heliostat[0].y, ..., heliostat[n].x, heliostat[n].y
 * 
 * @author Charles Xie
 *
 */
public class HeliostatPositionOptimizer extends SolarOutputOptimizer {

	public HeliostatPositionOptimizer(final int populationSize, final int chromosomeLength, final int discretizationSteps) {
		super(populationSize, chromosomeLength, discretizationSteps);
	}

	@Override
	public void setFoundation(final Foundation foundation) {
		super.setFoundation(foundation);
		final List<Mirror> heliostats = foundation.getHeliostats();
		final Mirror heliostat = heliostats.get(0);
		final Foundation receiver = heliostat.getReceiver();
		if (receiver != null) {
			final Vector3 v0 = receiver.getAbsPoint(0);
			final Vector3 v1 = receiver.getAbsPoint(1);
			final Vector3 v2 = receiver.getAbsPoint(2);
			final Vector3 v3 = receiver.getAbsPoint(3);
			final double cx = 0.25 * (v0.getX() + v1.getX() + v2.getX() + v3.getX()) * Scene.getInstance().getScale();
			final double cy = 0.25 * (v0.getY() + v1.getY() + v2.getY() + v3.getY()) * Scene.getInstance().getScale();
			final double lx = v0.distance(v2) * Scene.getInstance().getScale();
			final double ly = v0.distance(v1) * Scene.getInstance().getScale();
			addConstraint(new RectangularBound(cx, cy, lx + heliostat.getApertureWidth(), ly + heliostat.getApertureHeight()));
		}
		// initialize the population with the first-born being the current design
		final Individual firstBorn = population.getIndividual(0);
		int i = 0;
		for (final Mirror m : heliostats) {
			firstBorn.setGene(i++, m.getPoints().get(0).getX());
			firstBorn.setGene(i++, m.getPoints().get(0).getY());
		}
	}

	@Override
	void computeIndividualFitness(final Individual individual) {
		final List<Mirror> heliostats = foundation.getHeliostats();
		for (int j = 0; j < individual.getChromosomeLength(); j++) {
			final double gene = individual.getGene(j);
			final Mirror m = heliostats.get(j / 2);
			if (j % 2 == 0) {
				m.getPoints().get(0).setX(gene);
			} else {
				m.getPoints().get(0).setY(gene);
			}
		}
		individual.setFitness(objectiveFunction.compute());
	}

	@Override
	public void applyFittest() {
		final Individual best = population.getFittest();
		final List<Mirror> heliostats = foundation.getHeliostats();
		for (int j = 0; j < best.getChromosomeLength(); j++) {
			final double gene = best.getGene(j);
			final Mirror m = heliostats.get(j / 2);
			if (j % 2 == 0) {
				m.getPoints().get(0).setX(gene);
			} else {
				m.getPoints().get(0).setY(gene);
			}
			m.draw();
		}
		System.out.println("Fittest: " + individualToString(best));
		displayFittest();
	}

	@Override
	String individualToString(final Individual individual) {
		String s = "(";
		for (int j = 0; j < individual.getChromosomeLength(); j++) {
			final double gene = individual.getGene(j);
			if (j % 2 == 0) {
				if (mins != null && maxs != null) {
					s += (mins[j] + gene * (maxs[j] - mins[j]));
				} else {
					s += gene;
				}
				s += ", ";
			} else {
				if (mins != null && maxs != null) {
					s += (mins[j] + gene * (maxs[j] - mins[j]));
				} else {
					s += gene;
				}
				s += " | ";
			}
		}
		return s.substring(0, s.length() - 3) + ") = " + individual.getFitness();
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
