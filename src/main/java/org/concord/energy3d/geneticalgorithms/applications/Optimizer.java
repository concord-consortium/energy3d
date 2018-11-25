package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import org.concord.energy3d.geneticalgorithms.Constraint;
import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.geneticalgorithms.ObjectiveFunction;
import org.concord.energy3d.geneticalgorithms.Population;
import org.concord.energy3d.geneticalgorithms.RectangularBound;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

import com.ardor3d.math.Vector3;

/**
 * If the population size is less than 10, we use the micro genetic algorithm. Otherwise, we use the standard genetic algorithm.
 * 
 * @author Charles Xie
 *
 */
public abstract class Optimizer {

	public final static int GLOBAL_SEARCH_UNIFORM_SELECTION = 0;
	public final static int LOCAL_SEARCH_RANDOM_OPTIMIZATION = 1; // https://en.wikipedia.org/wiki/Random_optimization
	public final static int GLOBAL_SEARCH_FITNESS_SHARING = 2; // https://stackoverflow.com/questions/13775810/what-is-niching-scheme
	private final static int MICRO_GA_MAX_POPULATION = 9;

	volatile boolean stop;
	double mutationRate = 0.1;
	double crossoverRate = 0.5;
	double selectionRate = 0.5;
	int maximumGenerations = 5;
	Individual[] fittestOfGenerations;
	Population population;
	int outsideGenerationCounter;
	int computeCounter;
	double[] mins, maxs;
	Foundation foundation;
	double cx, cy, lx, ly;
	List<Constraint> constraints;
	volatile boolean converged;
	ObjectiveFunction objectiveFunction;
	double fitnessSharingRadius = 0.1;
	int searchMethod = GLOBAL_SEARCH_UNIFORM_SELECTION;
	double localSearchRadius = 0.1;
	String[] geneNames;
	double[] geneMinima;
	double[] geneMaxima;
	boolean[] isGeneInteger;
	double[] initialGene, finalGene;
	double initialFitness, finalFitness;

	public Optimizer(final int populationSize, final int chromosomeLength, final int discretizationSteps) {
		population = new Population(populationSize, chromosomeLength, discretizationSteps);
		constraints = new ArrayList<Constraint>();
		geneNames = new String[chromosomeLength];
		geneMinima = new double[chromosomeLength];
		geneMaxima = new double[chromosomeLength];
		isGeneInteger = new boolean[chromosomeLength];
		initialGene = new double[chromosomeLength];
		finalGene = new double[chromosomeLength];
		Arrays.fill(isGeneInteger, false);
	}

	public void setInitialGene(final int i, final double g) {
		initialGene[i] = g;
	}

	public double getInitialGene(final int i) {
		return initialGene[i];
	}

	public void setInitialFitness(final double f) {
		initialFitness = f;
	}

	public double getInitialFitness() {
		return initialFitness;
	}

	public void setFinalGene(final int i, final double g) {
		finalGene[i] = g;
	}

	public double getFinalGene(final int i) {
		return finalGene[i];
	}

	public void setFinalFitness(final double f) {
		finalFitness = f;
	}

	public double getFinalFitness() {
		return finalFitness;
	}

	public void setGeneInteger(final int i, final boolean b) {
		isGeneInteger[i] = b;
	}

	public boolean isGeneInteger(final int i) {
		return isGeneInteger[i];
	}

	public void setGeneName(final int i, final String name) {
		geneNames[i] = name;
	}

	public String getGeneName(final int i) {
		return geneNames[i];
	}

	public void setGeneMinimum(final int i, final double min) {
		geneMinima[i] = min;
	}

	public double getGeneMinimum(final int i) {
		return geneMinima[i];
	}

	public void setGeneMaximum(final int i, final double max) {
		geneMaxima[i] = max;
	}

	public double getGeneMaximum(final int i) {
		return geneMaxima[i];
	}

	public void setSearchMethod(final int searchMethod) {
		this.searchMethod = searchMethod;
	}

	public int getSearchMethod() {
		return searchMethod;
	}

	public void setLocalSearchRadius(final double localSearchRadius) {
		this.localSearchRadius = localSearchRadius;
	}

	public double getLocalSearchRadius() {
		return localSearchRadius;
	}

	public void setSharingRadius(final double sharingRadius) {
		this.fitnessSharingRadius = sharingRadius;
	}

	public double getSharingRadius() {
		return fitnessSharingRadius;
	}

	public void setSelectionMethod(final int selectionMethod) {
		population.setSelectionMethod(selectionMethod);
	}

	public void setConvergenceThreshold(final double convergenceThreshold) {
		population.setConvergenceThreshold(convergenceThreshold);
	}

	public void setMaximumGenerations(final int maximumGenerations) {
		this.maximumGenerations = maximumGenerations;
		fittestOfGenerations = new Individual[maximumGenerations + 1];
	}

	public Individual[] getFittestOfGenerations() {
		return fittestOfGenerations;
	}

	public void setFoundation(final Foundation foundation) {
		this.foundation = foundation;
	}

	public abstract void setOjectiveFunction(final int objectiveFunctionType);

	public int getObjectiveFunctionType() {
		return objectiveFunction.getType();
	}

	public void setupFoundationConstraint() {
		final Vector3 v0 = foundation.getAbsPoint(0);
		final Vector3 v1 = foundation.getAbsPoint(1);
		final Vector3 v2 = foundation.getAbsPoint(2);
		final Vector3 v3 = foundation.getAbsPoint(3);
		cx = 0.25 * (v0.getX() + v1.getX() + v2.getX() + v3.getX()) * Scene.getInstance().getScale();
		cy = 0.25 * (v0.getY() + v1.getY() + v2.getY() + v3.getY()) * Scene.getInstance().getScale();
		lx = v0.distance(v2) * Scene.getInstance().getScale();
		ly = v0.distance(v1) * Scene.getInstance().getScale();
		final int chromosomeLength = population.getChromosomeLength();
		mins = new double[chromosomeLength];
		maxs = new double[chromosomeLength];
		for (int i = 0; i < chromosomeLength; i += 2) {
			setMinMax(i, cx - lx * 0.5, cx + lx * 0.5);
			setMinMax(i + 1, cy - ly * 0.5, cy + ly * 0.5);
		}
	}

	public abstract void applyFittest();

	public void evolve() {

		onStart();
		outsideGenerationCounter = 0;
		computeCounter = 0;
		Arrays.fill(fittestOfGenerations, null);

		if (maximumGenerations > 1) {
			while (!shouldTerminate()) { // the number of individuals to evaluate is maximumGeneration * population.size(), subject to the convergence criterion
				for (int i = 0; i < population.size(); i++) {
					computeIndividual(i);
				}
				outsideGenerationCounter++;
			}
		}

		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() {
				if (maximumGenerations > 1) {
					applyFittest(); // show the fittest
				}
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

	abstract String individualToString(final Individual individual);

	abstract void computeIndividualFitness(final Individual individual);

	private void computeIndividual(final int indexOfIndividual) {

		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() {

				if (stop) {
					SceneManager.getTaskManager().clearTasks();
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							onCompletion();
						}
					});
					return null;
				}

				final int populationSize = population.size();

				if (populationSize > MICRO_GA_MAX_POPULATION) { // implement simple GA

					if (!converged) {
						final Individual individual = population.getIndividual(indexOfIndividual);
						computeIndividualFitness(individual);
						final int generation = computeCounter / populationSize;
						System.out.println("Generation " + generation + ", individual " + indexOfIndividual + " : " + individualToString(individual));
						final boolean isAtTheEndOfGeneration = (computeCounter % populationSize) == (populationSize - 1);
						if (isAtTheEndOfGeneration) {
							population.saveGenes();
							population.runSGA(selectionRate, crossoverRate);
							fittestOfGenerations[generation + 1] = new Individual(population.getFittest());
							if (detectViolations()) {
								population.restoreGenes();
							} else {
								converged = population.isSGAConverged();
								if (!converged && searchMethod == GLOBAL_SEARCH_UNIFORM_SELECTION) {
									population.mutate(mutationRate);
								}
							}
						}
					} else {
						SceneManager.getTaskManager().clearTasks();
						applyFittest();
						EventQueue.invokeLater(new Runnable() {
							@Override
							public void run() {
								onCompletion();
							}
						});
					}

				} else { // implement micro GA -- it doesn't exit when converged; At convergence, we restart by mating the winner with four new individuals that are randomly chosen

					final Individual individual = population.getIndividual(indexOfIndividual);
					computeIndividualFitness(individual);
					final int generation = computeCounter / populationSize;
					System.out.println("Generation " + generation + ", individual " + indexOfIndividual + " : " + individualToString(individual));
					final boolean isAtTheEndOfGeneration = (computeCounter % populationSize) == (populationSize - 1);
					if (isAtTheEndOfGeneration) {
						population.saveGenes();
						population.runMGA();
						fittestOfGenerations[generation + 1] = new Individual(population.getFittest());
						if (detectViolations()) {
							population.restoreGenes();
						} else {
							if (population.isMGAConverged()) {
								population.restartMGA();
							}
						}
					}

				}

				computeCounter++;
				updateInfo(population.getIndividual(indexOfIndividual));
				if (computeCounter == 1 && indexOfIndividual == 0) {
					fittestOfGenerations[0] = population.getIndividual(0);
					initialFitness = population.getIndividual(0).getFitness();
				}
				return null;

			}
		});

	}

	// if anyone in the current population doesn't meed the constraints, the entire population dies and the algorithm reverts to the previous generation -- not efficient
	boolean detectViolations() {
		boolean detected = false;
		if (mins != null && maxs != null) {
			final int chromosomeLength = population.getChromosomeLength();
			final int populationSize = population.size();
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
								detected = true;
							}
						}
					}
				}
			}
		}
		return detected;
	}

	public void addConstraint(final Constraint c) {
		constraints.add(c);
	}

	public void stop() {
		stop = true;
	}

	boolean shouldTerminate() {
		return outsideGenerationCounter >= maximumGenerations;
	}

	public void setMinMax(final int i, final double min, final double max) {
		mins[i] = min;
		maxs[i] = max;
	}

	public Population getPopulation() {
		return population;
	}

	void onCompletion() {
		EnergyPanel.getInstance().progress(0);
		EnergyPanel.getInstance().disableDateSpinner(false);
		SceneManager.setExecuteAllTask(true);
		EnergyPanel.getInstance().cancel();
		TimeSeriesLogger.getInstance().logOptimization(this);
	}

	void onStart() {
		EnergyPanel.getInstance().disableDateSpinner(true);
		SceneManager.getInstance().setHeatFluxDaily(true);
		Util.selectSilently(MainPanel.getInstance().getEnergyButton(), true);
		SceneManager.getInstance().setSolarHeatMapWithoutUpdate(true);
		SceneManager.getInstance().setHeatFluxVectorsVisible(true);
		SceneManager.getInstance().getSolarLand().setVisible(Scene.getInstance().getSolarMapForLand());
		SceneManager.setExecuteAllTask(false);
		Scene.getInstance().redrawAllNow();
	}

	abstract void updateInfo(Individual individual);

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

	public void displayResults(final String title) {
		new ResultGraphPanel(this).display(title);
	}

	public void displayFittest() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {

				final JTabbedPane tabbedPane = new JTabbedPane();

				final int n = population.getChromosomeLength();
				final String[] header = new String[] { "", "Initial", "Final" };
				final Object[][] data = new Object[n + 1][3];
				for (int i = 0; i < n; i++) {
					data[i][0] = geneNames[i];
					data[i][1] = EnergyPanel.FIVE_DECIMALS.format(initialGene[i]);
					data[i][2] = EnergyPanel.FIVE_DECIMALS.format(finalGene[i]);
				}
				data[n][0] = "Objective";
				data[n][1] = EnergyPanel.FIVE_DECIMALS.format(initialFitness);
				data[n][2] = EnergyPanel.FIVE_DECIMALS.format(finalFitness);
				final JTable table = new JTable(data, header);
				// table.getTableHeader().setBackground(Color.GRAY); // Why doesn't it work?
				final JPanel ui = new JPanel(new BorderLayout(5, 5));
				final JScrollPane scrollPane = new JScrollPane(table);
				final Dimension size = new Dimension(360, 200);
				scrollPane.setPreferredSize(size);
				scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				table.getColumnModel().getColumn(0).setPreferredWidth(180);
				ui.add(scrollPane, BorderLayout.CENTER);

				tabbedPane.setFont(new Font(null, Font.PLAIN, 8));
				tabbedPane.setBorder(BorderFactory.createTitledBorder(null, "Intermediates", TitledBorder.CENTER, TitledBorder.TOP));
				ui.add(tabbedPane, BorderLayout.SOUTH);

				for (int i = 0; i < n; i++) {
					final GeneTemporalGraph geneGraph = new GeneTemporalGraph(fittestOfGenerations, i, geneMinima[i], geneMaxima[i]);
					geneGraph.setBackground(Color.WHITE);
					geneGraph.setPreferredSize(size);
					tabbedPane.addTab(geneNames[i], geneGraph);
				}

				final ObjectiveTemporalGraph objectiveGraph = new ObjectiveTemporalGraph(getFittestOfGenerations());
				objectiveGraph.setBackground(Color.WHITE);
				objectiveGraph.setPreferredSize(size);
				tabbedPane.addTab("Objective", objectiveGraph);

				final Object[] options = new Object[] { "Close", "More" };
				final JOptionPane optionPane = new JOptionPane(ui, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION, null, options, options[0]);
				final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Results");

				while (true) {
					dialog.setVisible(true);
					final Object choice = optionPane.getValue();
					if (choice == options[1]) {
						population.sort();
						for (int i = 0; i < population.size(); i++) {
							System.out.println(i + " = " + individualToString(population.getIndividual(i)));
						}
						displayResults("Population Distribution");
					} else {
						break;
					}
				}

			}
		});

	}

	public String toJson() {
		String json = "{";
		json += "\"Generations\": " + maximumGenerations;
		json += ", \"Population\": " + population.size();
		json += ", \"Mutation Rate\": " + mutationRate;
		switch (population.getSelectionMethod()) {
		case Population.TOURNAMENT:
			json += ", \"Selection Method\": \"Tournament\"";
			break;
		default:
			json += ", \"Selection Method\": \"Roulette Wheel\"";
		}
		switch (searchMethod) {
		case LOCAL_SEARCH_RANDOM_OPTIMIZATION:
			json += ", \"Search Method\": \"Local Search Random Optimization\"";
			break;
		case GLOBAL_SEARCH_FITNESS_SHARING:
			json += ", \"Search Method\": \"Global Search Fitness Sharing\"";
			break;
		default:
			json += ", \"Search Method\": \"Global Search Uniform Selection\"";
		}
		json += ", \"Convergence Threshold:\": " + population.getConvergenceThreshold();
		json += ", \"Parameters\": [";
		final int n = population.getChromosomeLength();
		for (int i = 0; i < n; i++) {
			json += "{\"Name\": \"" + geneNames[i] + "\", \"Initial Value\": " + initialGene[i] + ", \"Final Value\": " + finalGene[i] + "}";
			if (i < n - 1) {
				json += ", ";
			}
		}
		json += "]";
		json += ", \"Initial Objective\": " + initialFitness + ", \"Final Objective\": " + finalFitness;
		switch (objectiveFunction.getType()) {
		case ObjectiveFunction.ANNUAL:
			json += ", \"Time Period\": \"Annual\"";
			break;
		default:
			json += ", \"Time Period\": \"Daily\"";
		}
		json += "}";
		return json;
	}

}
