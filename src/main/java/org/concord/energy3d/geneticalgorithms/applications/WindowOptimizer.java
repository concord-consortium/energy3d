package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.EventQueue;
import java.util.Calendar;

import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.geneticalgorithms.ObjectiveFunction;
import org.concord.energy3d.gui.CspProjectDailyEnergyGraph;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;

/**
 * @author Charles Xie
 *
 */
public class WindowOptimizer extends NetEnergyOptimizer {

	public WindowOptimizer(final int populationSize, final int chromosomeLength, final int selectionMethod, final double convergenceThreshold) {
		super(populationSize, chromosomeLength, selectionMethod, convergenceThreshold);
	}

	@Override
	void computeIndividualFitness(final Individual individual) {
		for (int j = 0; j < individual.getChromosomeLength(); j++) {
			final double gene = individual.getGene(j);
			final int j2 = j / 2;
			final Mirror m = foundation.getHeliostats().get(j2);
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
		for (int j = 0; j < best.getChromosomeLength(); j++) {
			final double gene = best.getGene(j);
			final int j2 = j / 2;
			final Mirror m = foundation.getHeliostats().get(j2);
			if (j % 2 == 0) {
				m.getPoints().get(0).setX(gene);
			} else {
				m.getPoints().get(0).setY(gene);
			}
		}
		System.out.println("Fittest: " + individualToString(best));
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
	void updateInfo() {
		final Foundation receiver = foundation.getHeliostats().get(0).getReceiver();
		if (receiver != null) {
			switch (objectiveFunction.getType()) {
			case ObjectiveFunction.DAILY:
				receiver.setLabelCustomText("Daily Output = " + EnergyPanel.ONE_DECIMAL.format(population.getIndividual(0).getFitness()));
				break;
			case ObjectiveFunction.ANNUAl:
				receiver.setLabelCustomText("Annual Output = " + EnergyPanel.ONE_DECIMAL.format(population.getIndividual(0).getFitness() * 30));
				break;
			case ObjectiveFunction.RANDOM:
				receiver.setLabelCustomText(null);
				break;
			}
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
