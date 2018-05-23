package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.EventQueue;
import java.util.Calendar;
import java.util.List;

import org.concord.energy3d.geneticalgorithms.Individual;
import org.concord.energy3d.geneticalgorithms.ObjectiveFunction;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.PvProjectDailyEnergyGraph;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;

import com.ardor3d.math.Vector3;

/**
 * Chromosome of an individual is encoded as follows:
 * 
 * row spacing (d), tilt angle (a), panel row number on rack (r)
 *
 * assuming the base height is fixed and the number of rows on each rack increases when the tilt angle decreases (otherwise the maximum inter-row spacing would always be preferred)
 * 
 * @author Charles Xie
 *
 */
public class SolarPanelArrayOptimizer extends SolarOutputOptimizer {

	private double minimumRowSpacing;
	private double maximumRowSpacing;
	private final int minimumPanelRows = 1;
	private final int maximumPanelRows = 5;
	private double baseHeight;
	private SolarPanel solarPanel;
	private boolean outputPerSolarPanel;
	private boolean netProfit;
	private double pricePerKWh = 0.225;
	private double dailyCostPerSolarPanel = 0.15;

	public SolarPanelArrayOptimizer(final int populationSize, final int chromosomeLength, final int selectionMethod, final double convergenceThreshold, final int discretizationSteps) {
		super(populationSize, chromosomeLength, selectionMethod, convergenceThreshold, discretizationSteps);
	}

	@Override
	public void setFoundation(final Foundation foundation) {
		super.setFoundation(foundation);
		final Vector3 p = foundation.getAbsPoint(1).subtract(foundation.getAbsPoint(0), null);
		final List<Rack> racks = foundation.getRacks();
		final int n = racks.size();
		if (n > 0) {
			final Rack rack = racks.get(0);
			solarPanel = rack.getSolarPanel();
			baseHeight = rack.getBaseHeight() * Scene.getInstance().getScale();
			final int panelRowsPerRack = rack.getSolarPanelRowAndColumnNumbers()[1];
			maximumRowSpacing = p.length() * Scene.getInstance().getScale() - rack.getRackHeight(); // two racks at the opposite edges of the rectangular area
			minimumRowSpacing = rack.getRackHeight(); // two racks that border each other
			final Individual firstBorn = population.getIndividual(0); // initialize the population with the first-born being the current design
			if (n > 1) {
				final Vector3 q = rack.getAbsCenter().subtractLocal(racks.get(1).getAbsCenter());
				final double rowSpacing = Math.abs(q.dot(p.normalize(null))) * Scene.getInstance().getScale();
				firstBorn.setGene(0, (rowSpacing - minimumRowSpacing) / (maximumRowSpacing - minimumRowSpacing));
			} else {
				firstBorn.setGene(0, 1);
			}
			firstBorn.setGene(1, 0.5 * (1.0 + rack.getTiltAngle() / 90.0));
			firstBorn.setGene(2, (double) (panelRowsPerRack - minimumPanelRows) / (double) (maximumPanelRows - minimumPanelRows));
		} else {
			throw new RuntimeException("Must have at least one solar panel rack on this foundation");
		}
	}

	public void setPricePerKWh(final double x) {
		pricePerKWh = x;
	}

	public void setDailyCostPerSolarPanel(final double x) {
		dailyCostPerSolarPanel = x;
	}

	public void setOutputPerSolarPanel(final boolean b) {
		outputPerSolarPanel = b;
	}

	public void setNetProfit(final boolean b) {
		netProfit = b;
	}

	@Override
	void computeIndividualFitness(final Individual individual) {
		final double rowSpacing = minimumRowSpacing + individual.getGene(0) * (maximumRowSpacing - minimumRowSpacing);
		final double tiltAngle = (2 * individual.getGene(1) - 1) * 90;
		final int panelRowsPerRack = (int) Math.round(minimumPanelRows + individual.getGene(2) * (maximumPanelRows - minimumPanelRows));
		foundation.addSolarRackArrays(solarPanel, tiltAngle, baseHeight, panelRowsPerRack, rowSpacing, 1);
		final double output = objectiveFunction.compute();
		final int count = foundation.countSolarPanels();
		if (netProfit) {
			double cost = dailyCostPerSolarPanel;
			if (objectiveFunction.getType() == ObjectiveFunction.ANNUAl) {
				cost *= 12;
			}
			individual.setFitness(output * pricePerKWh - cost * count);
		} else if (outputPerSolarPanel) {
			individual.setFitness(output / count);
		} else {
			individual.setFitness(output);
		}
	}

	@Override
	public void applyFittest() {
		final Individual best = population.getFittest();
		final double rowSpacing = minimumRowSpacing + best.getGene(0) * (maximumRowSpacing - minimumRowSpacing);
		final double tiltAngle = (2 * best.getGene(1) - 1) * 90;
		final int panelRowsPerRack = (int) Math.round(minimumPanelRows + best.getGene(2) * (maximumPanelRows - minimumPanelRows));
		foundation.addSolarRackArrays(solarPanel, tiltAngle, baseHeight, panelRowsPerRack, rowSpacing, 1);
		System.out.println("Fittest: " + individualToString(best));
	}

	@Override
	String individualToString(final Individual individual) {
		String s = "(";
		s += (minimumRowSpacing + individual.getGene(0) * (maximumRowSpacing - minimumRowSpacing)) + ", ";
		s += (2 * individual.getGene(1) - 1) * 90 + ", ";
		return s.substring(0, s.length() - 2) + ") = " + individual.getFitness();
	}

	@Override
	void updateInfo() {
		switch (objectiveFunction.getType()) {
		case ObjectiveFunction.DAILY:
			String s;
			if (netProfit) {
				s = "Net Daily Profit";
			} else if (outputPerSolarPanel) {
				s = "Daily Output per Solar Panel";
			} else {
				s = "Total Daily Output";
			}
			foundation.setLabelCustomText(s + " = " + EnergyPanel.TWO_DECIMALS.format(population.getIndividual(0).getFitness()));
			break;
		case ObjectiveFunction.ANNUAl:
			if (netProfit) {
				s = "Net Annual Profit";
			} else if (outputPerSolarPanel) {
				s = "Annual Output per Solar Panel";
			} else {
				s = "Total Annual Output";
			}
			foundation.setLabelCustomText(s + " = " + EnergyPanel.ONE_DECIMAL.format(population.getIndividual(0).getFitness() * 365.0 / 12.0));
			break;
		}
		foundation.draw();
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final Calendar today = Heliodon.getInstance().getCalendar();
				EnergyPanel.getInstance().getDateSpinner().setValue(today.getTime());
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (selectedPart instanceof Foundation) { // synchronize with daily graph
					final PvProjectDailyEnergyGraph g = EnergyPanel.getInstance().getPvProjectDailyEnergyGraph();
					g.setCalendar(today);
					EnergyPanel.getInstance().getPvProjectTabbedPane().setSelectedComponent(g);
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
