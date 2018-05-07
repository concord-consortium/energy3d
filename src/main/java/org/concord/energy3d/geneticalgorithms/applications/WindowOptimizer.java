package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.EventQueue;
import java.util.Calendar;
import java.util.List;

import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.geneticalgorithms.ObjectiveFunction;
import org.concord.energy3d.gui.BuildingDailyEnergyGraph;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;

/**
 * Chromosome of an individual is encoded as follows:
 * 
 * window[0].width, window[0].height, ..., window[n].width, window[n].height
 * 
 * @author Charles Xie
 *
 */
public class WindowOptimizer extends NetEnergyOptimizer {

	private double maximumRatioWidth = 0.9;
	private double minimumRatioWidth = 0.1;
	private double maximumRatioHeight = 0.9;
	private double minimumRatioHeight = 0.1;

	public WindowOptimizer(final int populationSize, final int chromosomeLength, final int selectionMethod, final double convergenceThreshold) {
		super(populationSize, chromosomeLength, selectionMethod, convergenceThreshold);
	}

	public void setWidthBounds(final double minimumRatioWidth, final double maximumRatioWidth) {
		this.minimumRatioWidth = minimumRatioWidth;
		this.maximumRatioWidth = maximumRatioWidth;
	}

	public void setHeightBounds(final double minimumRatioHeight, final double maximumRatioHeight) {
		this.minimumRatioHeight = minimumRatioHeight;
		this.maximumRatioHeight = maximumRatioHeight;
	}

	@Override
	public void setFoundation(final Foundation foundation) {
		super.setFoundation(foundation);
		final List<Window> windows = foundation.getWindows();
		// initialize the population with the first-born being the current design
		final Individual firstBorn = population.getIndividual(0);
		int i = 0;
		for (final Window w : windows) {
			if (w.getContainer() instanceof Wall) {
				final Wall wall = (Wall) w.getContainer();
				final double wmin = minimumRatioWidth * wall.getWallWidth();
				final double wmax = maximumRatioWidth * wall.getWallWidth();
				double val = (w.getWindowWidth() - wmin) / (wmax - wmin);
				firstBorn.setGene(i++, Math.min(1, val));
				final double hmin = minimumRatioHeight * wall.getWallHeight();
				final double hmax = maximumRatioHeight * wall.getWallHeight();
				val = (w.getWindowHeight() - hmin) / (hmax - hmin);
				firstBorn.setGene(i++, Math.min(1, val));
			} else {
				throw new RuntimeException("Windows must be on walls!");
			}
		}
	}

	@Override
	void computeIndividualFitness(final Individual individual) {
		final List<Window> windows = foundation.getWindows();
		for (int j = 0; j < individual.getChromosomeLength(); j++) {
			final double gene = individual.getGene(j);
			final Window w = windows.get(j / 2);
			final Wall wall = (Wall) w.getContainer();
			switch (j % 2) {
			case 0:
				final double wmin = minimumRatioWidth * wall.getWallWidth();
				final double wmax = maximumRatioWidth * wall.getWallWidth();
				w.setWindowWidth(wmin + gene * (wmax - wmin));
				break;
			case 1:
				final double hmin = minimumRatioHeight * wall.getWallHeight();
				final double hmax = maximumRatioHeight * wall.getWallHeight();
				w.setWindowHeight(hmin + gene * (hmax - hmin));
				break;
			}
			w.draw();
			wall.draw();
		}
		individual.setFitness(objectiveFunction.compute());
	}

	@Override
	public void applyFittest() {
		final List<Window> windows = foundation.getWindows();
		final Individual best = population.getFittest();
		for (int j = 0; j < best.getChromosomeLength(); j++) {
			final double gene = best.getGene(j);
			final Window w = windows.get(j / 2);
			final Wall wall = (Wall) w.getContainer();
			switch (j % 2) {
			case 0:
				final double wmin = minimumRatioWidth * wall.getWallWidth();
				final double wmax = maximumRatioWidth * wall.getWallWidth();
				w.setWindowWidth(wmin + gene * (wmax - wmin));
				break;
			case 1:
				final double hmin = minimumRatioHeight * wall.getWallHeight();
				final double hmax = maximumRatioHeight * wall.getWallHeight();
				w.setWindowHeight(hmin + gene * (hmax - hmin));
				break;
			}
			w.draw();
			wall.draw();
		}
		System.out.println("Fittest: " + individualToString(best));
	}

	@Override
	String individualToString(final Individual individual) {
		String s = "(";
		final List<Window> windows = foundation.getWindows();
		for (int j = 0; j < individual.getChromosomeLength(); j++) {
			final double gene = individual.getGene(j);
			final Window w = windows.get(j / 2);
			final Wall wall = (Wall) w.getContainer();
			switch (j % 2) {
			case 0:
				final double wmin = minimumRatioWidth * wall.getWallWidth();
				final double wmax = maximumRatioWidth * wall.getWallWidth();
				s += (wmin + gene * (wmax - wmin)) + ", ";
				break;
			case 1:
				final double hmin = minimumRatioHeight * wall.getWallHeight();
				final double hmax = maximumRatioHeight * wall.getWallHeight();
				s += (hmin + gene * (hmax - hmin)) + " | ";
				break;
			}
		}
		return s.substring(0, s.length() - 3) + ") = " + individual.getFitness();
	}

	@Override
	void updateInfo() {
		if (foundation != null) {
			switch (objectiveFunction.getType()) {
			case ObjectiveFunction.DAILY:
				foundation.setLabelCustomText("Daily Output = " + EnergyPanel.ONE_DECIMAL.format(population.getIndividual(0).getFitness()));
				break;
			case ObjectiveFunction.ANNUAl:
				foundation.setLabelCustomText("Annual Output = " + EnergyPanel.ONE_DECIMAL.format(population.getIndividual(0).getFitness() * 30));
				break;
			case ObjectiveFunction.RANDOM:
				foundation.setLabelCustomText(null);
				break;
			}
			foundation.draw();
		}
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
