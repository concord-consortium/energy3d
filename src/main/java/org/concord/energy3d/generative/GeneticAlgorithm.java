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

	private final static int MAX_GENERATION = 5;

	private double mutationRate = 0.1;
	private double crossoverRate = 0.9;
	private double selectionRate = 0.5;
	private final Population population;
	private int outsideGenerationCounter;
	private int computeCounter;
	private final double[] mins, maxs;
	private final Foundation foundation;
	private final List<Constraint> constraints;
	private final int populationSize;
	private final int chromosomeLength;

	public GeneticAlgorithm(final int populationSize, final int chromosomeLength, final Foundation foundation) {
		this.foundation = foundation;
		this.populationSize = populationSize;
		this.chromosomeLength = chromosomeLength;
		population = new Population(populationSize, chromosomeLength);
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
		final Foundation receiver = foundation.getHeliostats().get(0).getReceiver();
		if (receiver != null) {
			v0 = receiver.getAbsPoint(0);
			v1 = receiver.getAbsPoint(1);
			v2 = receiver.getAbsPoint(2);
			v3 = receiver.getAbsPoint(3);
			cx = 0.25 * (v0.getX() + v1.getX() + v2.getX() + v3.getX()) * Scene.getInstance().getAnnotationScale();
			cy = 0.25 * (v0.getY() + v1.getY() + v2.getY() + v3.getY()) * Scene.getInstance().getAnnotationScale();
			lx = v0.distance(v2) * Scene.getInstance().getAnnotationScale();
			ly = v0.distance(v1) * Scene.getInstance().getAnnotationScale();
			addConstraint(new CircularBound(cx, cy, Math.max(lx, ly) * 0.5, false));
		}
	}

	public void evolve() {

		onStart();
		outsideGenerationCounter = 0;
		computeCounter = 0;
		final HeliostatObjectiveFunction of = new HeliostatObjectiveFunction();

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
					population.selectSurvivors(selectionRate);
					population.crossover(crossoverRate);
					population.mutate(mutationRate);
				}

				updateGraph();
				computeCounter++;

				return null;

			}
		});

	}

	boolean meetConstraints(final Individual individual) {
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
				if (c instanceof CircularBound) {
					final CircularBound cb = (CircularBound) c;
					if (!cb.meet(x[j2], y[j2])) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private void updateGraph() {
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
		return outsideGenerationCounter >= MAX_GENERATION;
	}

	public void setMinMax(final int i, final double min, final double max) {
		mins[i] = min;
		maxs[i] = max;
	}

	// initialize the population with the first-born being the current design
	public void initializePopulation(final double[] v) {
		if (population.getChromosomeLength() != v.length) {
			throw new IllegalArgumentException("Input data must have the same length as the chromosome length of the population");
		}
		final Individual firstBorn = population.getIndividual(0);
		for (int i = 0; i < v.length; i++) {
			firstBorn.setGene(i, v[i]);
		}
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
