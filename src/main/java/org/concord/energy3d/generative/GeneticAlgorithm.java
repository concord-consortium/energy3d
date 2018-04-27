package org.concord.energy3d.generative;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import org.concord.energy3d.gui.CspProjectDailyEnergyGraph;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.Util;

import com.ardor3d.math.Vector3;

/**
 * @author Charles Xie
 *
 */
public class GeneticAlgorithm {

	private int maximumGeneration = 5;
	private double mutationRate = 0.1;
	private double crossoverRate = 0.5;
	private double selectionRate = 0.5;
	private final Population population;
	private int outsideGenerationCounter;
	private int computeCounter;
	private final double[] mins, maxs;
	private final Foundation foundation;
	private final List<Constraint> constraints;
	private final int populationSize;
	private final int chromosomeLength;
	private volatile boolean converged;

	public GeneticAlgorithm(final int populationSize, final Foundation foundation, final int maximumGeneration, final int selectionMethod, final double convergenceThreshold) {
		this.populationSize = populationSize;
		this.foundation = foundation;
		this.maximumGeneration = maximumGeneration;
		chromosomeLength = foundation.getHeliostats().size() * 2;
		population = new Population(populationSize, chromosomeLength, selectionMethod, convergenceThreshold);
		Vector3 v0 = foundation.getAbsPoint(0);
		Vector3 v1 = foundation.getAbsPoint(1);
		Vector3 v2 = foundation.getAbsPoint(2);
		Vector3 v3 = foundation.getAbsPoint(3);
		double cx = 0.25 * (v0.getX() + v1.getX() + v2.getX() + v3.getX()) * Scene.getInstance().getAnnotationScale();
		double cy = 0.25 * (v0.getY() + v1.getY() + v2.getY() + v3.getY()) * Scene.getInstance().getAnnotationScale();
		double lx = v0.distance(v2) * Scene.getInstance().getAnnotationScale();
		double ly = v0.distance(v1) * Scene.getInstance().getAnnotationScale();
		mins = new double[chromosomeLength];
		maxs = new double[chromosomeLength];
		for (int i = 0; i < chromosomeLength; i += 2) {
			setMinMax(i, cx - lx * 0.5, cx + lx * 0.5);
			setMinMax(i + 1, cy - ly * 0.5, cy + ly * 0.5);
		}
		constraints = new ArrayList<Constraint>();
		final Mirror heliostat = foundation.getHeliostats().get(0);
		final Foundation receiver = heliostat.getReceiver();
		if (receiver != null) {
			v0 = receiver.getAbsPoint(0);
			v1 = receiver.getAbsPoint(1);
			v2 = receiver.getAbsPoint(2);
			v3 = receiver.getAbsPoint(3);
			cx = 0.25 * (v0.getX() + v1.getX() + v2.getX() + v3.getX()) * Scene.getInstance().getAnnotationScale();
			cy = 0.25 * (v0.getY() + v1.getY() + v2.getY() + v3.getY()) * Scene.getInstance().getAnnotationScale();
			lx = v0.distance(v2) * Scene.getInstance().getAnnotationScale();
			ly = v0.distance(v1) * Scene.getInstance().getAnnotationScale();
			addConstraint(new RectangularBound(cx, cy, lx + heliostat.getMirrorWidth(), ly + heliostat.getMirrorHeight()));
		}
		// initialize the population with the first-born being the current design
		final Individual firstBorn = population.getIndividual(0);
		int i = 0;
		for (final Mirror m : foundation.getHeliostats()) {
			firstBorn.setGene(i++, m.getPoints().get(0).getX());
			firstBorn.setGene(i++, m.getPoints().get(0).getY());
		}
	}

	public void evolve(final int type) {

		onStart();
		outsideGenerationCounter = 0;
		computeCounter = 0;
		final HeliostatObjectiveFunction of = new HeliostatObjectiveFunction(type);

		while (!shouldTerminate()) { // the number of individuals to evaluate is MAX_GENERATION * population.size()
			for (int i = 0; i < population.size(); i++) {
				computeIndividual(i, of);
			}
			outsideGenerationCounter++;
		}
		population.sort();
		computeIndividual(0, of);

		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						onCompletion();
					}
				});
				return null;
			}
		});

	}

	private void computeIndividual(final int indexOfIndividual, final HeliostatObjectiveFunction of) {

		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() {
				if (!converged) {
					final int generation = computeCounter / populationSize;
					final Individual individual = population.getIndividual(indexOfIndividual);
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
					individual.setFitness(of.compute());
					System.out.println("Generation " + generation + ", individual " + indexOfIndividual + " = " + individual.getFitness());
					final boolean isAtTheEndOfGeneration = (computeCounter % populationSize) == (populationSize - 1);
					if (isAtTheEndOfGeneration) {
						population.saveGenes();
						population.selectSurvivors(selectionRate);
						population.crossover(crossoverRate);
						population.mutate(mutationRate);
						detectViolations();
						population.restoreGenes();
						converged = population.isConverged();
					}
					computeCounter++;
				} else {
					SceneManager.getTaskManager().clearTasks();
					onCompletion();
				}
				updateInfo(of.getType());
				return null;
			}
		});

	}

	// if anyone in the current population doesn't meed the constraints, the entire population dies and the algorithm reverts to the previous generation -- not efficient
	private void detectViolations() {
		for (int i = 0; i < populationSize; i++) {
			final Individual individual = population.getIndividual(i);
			final double[] x = new double[chromosomeLength / 2];
			final double[] y = new double[chromosomeLength / 2];
			for (int j = 0; j < chromosomeLength; j++) {
				final double gene = individual.getGene(j);
				final int j2 = j / 2;
				if (j % 2 == 0) {
					x[j2] = (mins[j] + gene * (maxs[j] - mins[j]));
				} else {
					y[j2] = (mins[j] + gene * (maxs[j] - mins[j]));
				}
			}
			for (int j2 = 0; j2 < x.length; j2++) {
				for (final Constraint c : constraints) {
					if (c instanceof RectangularBound) {
						final RectangularBound rb = (RectangularBound) c;
						if (rb.contains(x[j2], y[j2])) {
							population.setViolation(i, true);
						}
					}
				}
				// if (j2 > 0) { // detect position overlaps
				// final double wj2 = Math.max(foundation.getHeliostats().get(j2).getMirrorWidth(), foundation.getHeliostats().get(j2).getMirrorHeight());
				// for (int k2 = 0; k2 < j2; k2++) {
				// final double wk2 = Math.max(foundation.getHeliostats().get(k2).getMirrorWidth(), foundation.getHeliostats().get(k2).getMirrorHeight());
				// final double dx = x[j2] - x[k2];
				// final double dy = y[j2] - y[k2];
				// if (dx * dx + dy * dy < 0.49 * wj2 * wk2) {
				// population.setViolation(i, true);
				// }
				// }
				// }
			}
		}
	}

	private void updateInfo(final int type) {
		final Foundation receiver = foundation.getHeliostats().get(0).getReceiver();
		if (receiver != null) {
			switch (type) {
			case ObjectiveFunction.DAILY:
				receiver.setLabelCustomText("Daily Output = " + EnergyPanel.ONE_DECIMAL.format(population.getIndividual(0).getFitness()));
				break;
			case ObjectiveFunction.ANNUAl:
				receiver.setLabelCustomText("Annual Output = " + EnergyPanel.ONE_DECIMAL.format(population.getIndividual(0).getFitness() * 30));
				break;
			}
			receiver.draw();
		}
		final Calendar c = Heliodon.getInstance().getCalendar();
		final Calendar today = (Calendar) c.clone();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart instanceof Foundation) { // synchronize with daily graph
			final CspProjectDailyEnergyGraph g = EnergyPanel.getInstance().getCspProjectDailyEnergyGraph();
			if (g.hasGraph()) {
				g.setCalendar(today);
				g.updateGraph();
			}
		}
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				EnergyPanel.getInstance().getDateSpinner().setValue(c.getTime());
				if (selectedPart instanceof Foundation) {
					final CspProjectDailyEnergyGraph g = EnergyPanel.getInstance().getCspProjectDailyEnergyGraph();
					EnergyPanel.getInstance().getCspProjectTabbedPane().setSelectedComponent(g);
					if (!g.hasGraph()) {
						g.setCalendar(today);
						g.addGraph((Foundation) selectedPart);
					}
				}
			}
		});
	}

	public void addConstraint(final Constraint c) {
		constraints.add(c);
	}

	private boolean shouldTerminate() {
		return outsideGenerationCounter >= maximumGeneration;
	}

	public void setMinMax(final int i, final double min, final double max) {
		mins[i] = min;
		maxs[i] = max;
	}

	public Population getPopulation() {
		return population;
	}

	public void setCrossoverRate(final double crossoverRate) {
		this.crossoverRate = crossoverRate;
	}

	public double getCrossoverRate() {
		return crossoverRate;
	}

	public void setMutationRate(final double mutationRate) {
		this.mutationRate = mutationRate;
	}

	public double getMutationRate() {
		return mutationRate;
	}

	public void setSelectionRate(final double selectionRate) {
		this.selectionRate = selectionRate;
	}

	public double getSelectionRate() {
		return selectionRate;
	}

	private void onCompletion() {
		EnergyPanel.getInstance().progress(0);
		EnergyPanel.getInstance().disableDateSpinner(false);
		SceneManager.setExecuteAllTask(true);
		EnergyPanel.getInstance().cancel();
	}

	private void onStart() {
		EnergyPanel.getInstance().disableDateSpinner(true);
		SceneManager.getInstance().setHeatFluxDaily(true);
		Util.selectSilently(MainPanel.getInstance().getEnergyButton(), true);
		SceneManager.getInstance().setSolarHeatMapWithoutUpdate(true);
		SceneManager.getInstance().setHeatFluxVectorsVisible(true);
		SceneManager.getInstance().getSolarLand().setVisible(Scene.getInstance().getSolarMapForLand());
		SceneManager.setExecuteAllTask(false);
		Scene.getInstance().redrawAllNow();
	}

}
