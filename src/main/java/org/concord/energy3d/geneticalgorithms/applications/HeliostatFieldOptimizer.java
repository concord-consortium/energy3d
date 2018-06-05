package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.EventQueue;
import java.util.Calendar;

import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.geneticalgorithms.ObjectiveFunction;
import org.concord.energy3d.gui.CspProjectDailyEnergyGraph;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;

/**
 * @author Charles Xie
 *
 */
public abstract class HeliostatFieldOptimizer extends SolarOutputOptimizer {

	double minimumApertureWidth = 2;
	double maximumApertureWidth = 10;
	double minimumApertureHeight = 1;
	double maximumApertureHeight = 2;

	double minimumRadialExpansion = 0;
	double maximumRadialExpansion = 0.01;

	boolean outputPerApertureSquareMeter;
	boolean netProfit;
	double pricePerKWh = 0.225;
	double dailyCostPerApertureSquareMeter = 0.1;

	public HeliostatFieldOptimizer(final int populationSize, final int chromosomeLength, final int selectionMethod, final double convergenceThreshold, final int discretizationSteps) {
		super(populationSize, chromosomeLength, selectionMethod, convergenceThreshold, discretizationSteps);
	}

	public void setPricePerKWh(final double x) {
		pricePerKWh = x;
	}

	public void setDailyCostPerApertureSquareMeter(final double x) {
		dailyCostPerApertureSquareMeter = x;
	}

	public void setOutputPerApertureSquareMeter(final boolean b) {
		outputPerApertureSquareMeter = b;
	}

	public void setNetProfit(final boolean b) {
		netProfit = b;
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

	public void setMinimumRadialExpansion(final double minimumRadialExpansion) {
		this.minimumRadialExpansion = minimumRadialExpansion;
	}

	public void setMaximumRadialExpansion(final double maximumRadialExpansion) {
		this.maximumRadialExpansion = maximumRadialExpansion;
	}

	@Override
	public void displayFittest() {
		final Individual best = population.getIndividual(0);
		final Foundation receiver = foundation.getHeliostats().get(0).getReceiver();
		if (receiver != null) {
			String s = null;
			switch (objectiveFunction.getType()) {
			case ObjectiveFunction.DAILY:
				if (netProfit) {
					s = "Daily Profit";
				} else if (outputPerApertureSquareMeter) {
					s = "Daily Output per Aperture Square Meter";
				} else {
					s = "Total Daily Output";
				}
				s += ": " + EnergyPanel.TWO_DECIMALS.format(best.getFitness());
				break;
			case ObjectiveFunction.ANNUAl:
				if (netProfit) {
					s = "Annual Profit";
				} else if (outputPerApertureSquareMeter) {
					s = "Annual Output per Aperture Square Meter";
				} else {
					s = "Total Annual Output";
				}
				s += ": " + EnergyPanel.ONE_DECIMAL.format(best.getFitness() * 365.0 / 12.0);
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
				if (netProfit) {
					s = "Daily Profit";
				} else if (outputPerApertureSquareMeter) {
					s = "Daily Output per Aperture Square Meter";
				} else {
					s = "Total Daily Output";
				}
				s += "\nCurrent: " + EnergyPanel.TWO_DECIMALS.format(individual.getFitness()) + ", Top: " + EnergyPanel.TWO_DECIMALS.format(best.getFitness());
				break;
			case ObjectiveFunction.ANNUAl:
				if (netProfit) {
					s = "Annual Profit";
				} else if (outputPerApertureSquareMeter) {
					s = "Annual Output per Aperture Square Meter";
				} else {
					s = "Total Annual Output";
				}
				s += "\nCurrent: " + EnergyPanel.ONE_DECIMAL.format(individual.getFitness() * 365.0 / 12.0) + ", Top: " + EnergyPanel.ONE_DECIMAL.format(best.getFitness() * 365.0 / 12.0);
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
