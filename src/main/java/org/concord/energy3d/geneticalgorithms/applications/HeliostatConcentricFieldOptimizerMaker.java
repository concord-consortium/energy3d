package org.concord.energy3d.geneticalgorithms.applications;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.concord.energy3d.geneticalgorithms.ObjectiveFunction;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.util.SpringUtilities;

/**
 * @author Charles Xie
 *
 */
public class HeliostatConcentricFieldOptimizerMaker extends OptimizerMaker {

	private double minimumApertureWidth = 1;
	private double maximumApertureWidth = 10;
	private double minimumApertureHeight = 1;
	private double maximumApertureHeight = 2;
	private double minimumAzimuthalSpacing = 0;
	private double maximumAzimuthalSpacing = 10;
	private double minimumRadialSpacing = 0;
	private double maximumRadialSpacing = 5;
	private double minimumRadialExpansion = 0;
	private double maximumRadialExpansion = 0.01;

	private double pricePerKWh = 0.225;
	private double dailyCostPerApertureSquareMeter = 0.1;

	public HeliostatConcentricFieldOptimizerMaker(final Foundation foundation) {
		this.foundation = foundation;
	}

	@Override
	public void make() {

		final List<Mirror> heliostats = foundation.getHeliostats();
		if (heliostats.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no heliostat on this foundation.", "Information", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		final JPanel panel = new JPanel(new SpringLayout());
		panel.add(new JLabel("Solution:"));
		final JComboBox<String> solutionComboBox = new JComboBox<String>(new String[] { "Field Pattern" });
		panel.add(solutionComboBox);
		panel.add(new JLabel());

		panel.add(new JLabel("Fitness function:"));
		final JComboBox<String> fitnessComboBox = new JComboBox<String>(new String[] { "Daily Total Output", "Annual Total Output", "Daily Average Output", "Annual Average Output", "Daily Profit", "Annual Profit" });
		fitnessComboBox.setSelectedIndex(selectedFitnessFunction);
		panel.add(fitnessComboBox);
		panel.add(new JLabel());

		panel.add(new JLabel("Electricity price:"));
		final JTextField priceField = new JTextField(pricePerKWh + "");
		panel.add(priceField);
		panel.add(new JLabel("<html><font size=2>$/kWh</font></html>"));

		panel.add(new JLabel("Cost:"));
		final JTextField dailyCostField = new JTextField(dailyCostPerApertureSquareMeter + "");
		panel.add(dailyCostField);
		panel.add(new JLabel("<html><font size=2>$/day/m&sup2;</font></html>"));

		panel.add(new JLabel("Minimum heliostat aperture width:"));
		final JTextField minimumApertureWidthField = new JTextField(EnergyPanel.TWO_DECIMALS.format(minimumApertureWidth));
		panel.add(minimumApertureWidthField);
		panel.add(new JLabel("<html><font size=2>Meters</font></html>"));

		panel.add(new JLabel("Maximum heliostat aperture width:"));
		final JTextField maximumApertureWidthField = new JTextField(EnergyPanel.TWO_DECIMALS.format(maximumApertureWidth));
		panel.add(maximumApertureWidthField);
		panel.add(new JLabel("<html><font size=2>Meters</font></html>"));

		panel.add(new JLabel("Minimum heliostat aperture height:"));
		final JTextField minimumApertureHeightField = new JTextField(EnergyPanel.TWO_DECIMALS.format(minimumApertureHeight));
		panel.add(minimumApertureHeightField);
		panel.add(new JLabel("<html><font size=2>Meters</font></html>"));

		panel.add(new JLabel("Maximum heliostat aperture height:"));
		final JTextField maximumApertureHeightField = new JTextField(EnergyPanel.TWO_DECIMALS.format(maximumApertureHeight));
		panel.add(maximumApertureHeightField);
		panel.add(new JLabel("<html><font size=2>Meters</font></html>"));

		panel.add(new JLabel("Minimum azimuthal spacing:"));
		final JTextField minimumAzimuthalSpacingField = new JTextField(EnergyPanel.TWO_DECIMALS.format(minimumAzimuthalSpacing));
		panel.add(minimumAzimuthalSpacingField);
		panel.add(new JLabel("<html><font size=2>Meters</font></html>"));

		panel.add(new JLabel("Maximum azimuthal spacing:"));
		final JTextField maximumAzimuthalSpacingField = new JTextField(EnergyPanel.TWO_DECIMALS.format(maximumAzimuthalSpacing));
		panel.add(maximumAzimuthalSpacingField);
		panel.add(new JLabel("<html><font size=2>Meters</font></html>"));

		panel.add(new JLabel("Minimum radial spacing:"));
		final JTextField minimumRadialSpacingField = new JTextField(EnergyPanel.TWO_DECIMALS.format(minimumRadialSpacing));
		panel.add(minimumRadialSpacingField);
		panel.add(new JLabel("<html><font size=2>Meters</font></html>"));

		panel.add(new JLabel("Maximum radial spacing:"));
		final JTextField maximumRadialSpacingField = new JTextField(EnergyPanel.TWO_DECIMALS.format(maximumRadialSpacing));
		panel.add(maximumRadialSpacingField);
		panel.add(new JLabel("<html><font size=2>Meters</font></html>"));

		panel.add(new JLabel("Minimum radial expansion:"));
		final JTextField minimumRadialExpansionField = new JTextField(EnergyPanel.FIVE_DECIMALS.format(minimumRadialExpansion));
		panel.add(minimumRadialExpansionField);
		panel.add(new JLabel("<html><font size=2>Dimensionless</font></html>"));

		panel.add(new JLabel("Maximum radial expansion:"));
		final JTextField maximumRadialExpansionField = new JTextField(EnergyPanel.FIVE_DECIMALS.format(maximumRadialExpansion));
		panel.add(maximumRadialExpansionField);
		panel.add(new JLabel("<html><font size=2>Dimensionless</font></html>"));

		panel.add(new JLabel("Type:"));
		final JComboBox<String> typeComboBox = new JComboBox<String>(new String[] { "Continuous" });
		panel.add(typeComboBox);
		panel.add(new JLabel());

		panel.add(new JLabel("Selection:"));
		final JComboBox<String> selectionComboBox = new JComboBox<String>(new String[] { "Roulette Wheel", "Tournament" });
		selectionComboBox.setSelectedIndex(selectedSelectionMethod);
		panel.add(selectionComboBox);
		panel.add(new JLabel());

		panel.add(new JLabel("Population size:"));
		final JTextField populationField = new JTextField(populationSize + "");
		panel.add(populationField);
		panel.add(new JLabel());

		panel.add(new JLabel("Maximum generations:"));
		final JTextField generationField = new JTextField(maximumGenerations + "");
		panel.add(generationField);
		panel.add(new JLabel());

		panel.add(new JLabel("Mutation rate:"));
		final JTextField mutationRateField = new JTextField(EnergyPanel.FIVE_DECIMALS.format(mutationRate));
		panel.add(mutationRateField);
		panel.add(new JLabel());

		panel.add(new JLabel("Convergence criterion:"));
		final JComboBox<String> convergenceCriterionComboBox = new JComboBox<String>(new String[] { "Bitwise (Nominal)" });
		panel.add(convergenceCriterionComboBox);
		panel.add(new JLabel());

		panel.add(new JLabel("Convergence threshold:"));
		final JTextField convergenceThresholdField = new JTextField(EnergyPanel.FIVE_DECIMALS.format(convergenceThreshold));
		panel.add(convergenceThresholdField);
		panel.add(new JLabel());

		SpringUtilities.makeCompactGrid(panel, 21, 3, 6, 6, 6, 6);

		final Object[] options = new Object[] { "OK", "Cancel" };
		final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION, null, options, options[0]);
		final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Genetic Algorithm Options for Optimizing Concentric Heliostat Field");

		while (true) {
			dialog.setVisible(true);
			final Object choice = optionPane.getValue();
			if (choice == options[1] || choice == null) {
				break;
			} else {
				boolean ok = true;
				try {
					pricePerKWh = Double.parseDouble(priceField.getText());
					dailyCostPerApertureSquareMeter = Double.parseDouble(dailyCostField.getText());
					minimumAzimuthalSpacing = Double.parseDouble(minimumAzimuthalSpacingField.getText());
					maximumAzimuthalSpacing = Double.parseDouble(maximumAzimuthalSpacingField.getText());
					minimumRadialSpacing = Double.parseDouble(minimumRadialSpacingField.getText());
					maximumRadialSpacing = Double.parseDouble(maximumRadialSpacingField.getText());
					minimumRadialExpansion = Double.parseDouble(minimumRadialExpansionField.getText());
					maximumRadialExpansion = Double.parseDouble(maximumRadialExpansionField.getText());
					minimumApertureWidth = Double.parseDouble(minimumApertureWidthField.getText());
					maximumApertureWidth = Double.parseDouble(maximumApertureWidthField.getText());
					minimumApertureHeight = Double.parseDouble(minimumApertureHeightField.getText());
					maximumApertureHeight = Double.parseDouble(maximumApertureHeightField.getText());
					populationSize = Integer.parseInt(populationField.getText());
					maximumGenerations = Integer.parseInt(generationField.getText());
					convergenceThreshold = Double.parseDouble(convergenceThresholdField.getText());
					mutationRate = Double.parseDouble(mutationRateField.getText());
				} catch (final NumberFormatException exception) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
					ok = false;
				}
				if (ok) {
					if (populationSize <= 0) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Population size must be greater than zero.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else if (maximumGenerations <= 1) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Maximum generations must be greater than one.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else if (mutationRate < 0 || mutationRate > 1) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Mutation rate must be between 0 and 1.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else if (convergenceThreshold < 0 || convergenceThreshold > 0.1) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Convergence threshold must be between 0 and 0.1.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else if (minimumAzimuthalSpacing < 0 || maximumAzimuthalSpacing < 0) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Azimuthal spacing should not be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else if (minimumAzimuthalSpacing >= maximumAzimuthalSpacing) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Maximum azimuthal spacing must be greater than minimum azimuthal spacing.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else if (minimumRadialSpacing < 0 || maximumRadialSpacing < 0) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Radial spacing should not be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else if (minimumRadialSpacing >= maximumRadialSpacing) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Maximum radial spacing must be greater than minimum radial spacing.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else if (minimumRadialExpansion < 0 || maximumRadialExpansion < 0) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Radial expansion ratio should not be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else if (minimumRadialExpansion >= maximumRadialExpansion) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Maximum radial expansion ratio must be greater than minimum radial expansion ratio.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else if (minimumApertureWidth < 0 || maximumApertureWidth < 0) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Aperture width should not be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else if (minimumApertureWidth >= maximumApertureWidth) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Maximum aperture width must be greater than minimum aperture width.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else if (minimumApertureHeight < 0 || maximumApertureHeight < 0) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Aperture height should not be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else if (minimumApertureHeight >= maximumApertureHeight) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Maximum aperture height must be greater than minimum aperture height.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else {
						selectedFitnessFunction = fitnessComboBox.getSelectedIndex();
						selectedSelectionMethod = selectionComboBox.getSelectedIndex();
						final HeliostatConcentricFieldOptimizer op = new HeliostatConcentricFieldOptimizer(populationSize, foundation.getHeliostats().size() * 2, 0);
						op.setSelectionMethod(selectedSelectionMethod);
						op.setConvergenceThreshold(convergenceThreshold);
						op.setMinimumAzimuthalSpacing(minimumAzimuthalSpacing);
						op.setMaximumAzimuthalSpacing(maximumAzimuthalSpacing);
						op.setMinimumRadialSpacing(minimumRadialSpacing);
						op.setMaximumRadialSpacing(maximumRadialSpacing);
						op.setMinimumRadialExpansion(minimumRadialExpansion);
						op.setMaximumRadialExpansion(maximumRadialExpansion);
						op.setMinimumApertureWidth(minimumApertureWidth);
						op.setMaximumApertureWidth(maximumApertureWidth);
						op.setMinimumApertureHeight(minimumApertureHeight);
						op.setMaximumApertureHeight(maximumApertureHeight);
						op.setMaximumGenerations(maximumGenerations);
						op.setMutationRate(mutationRate);
						op.setDailyCostPerApertureSquareMeter(dailyCostPerApertureSquareMeter);
						op.setPricePerKWh(pricePerKWh);
						switch (selectedFitnessFunction) {
						case 0:
							op.setOjectiveFunction(ObjectiveFunction.DAILY);
							break;
						case 1:
							op.setOjectiveFunction(ObjectiveFunction.ANNUAl);
							break;
						case 2:
							op.setOjectiveFunction(ObjectiveFunction.DAILY);
							op.setOutputPerApertureSquareMeter(true);
							break;
						case 3:
							op.setOjectiveFunction(ObjectiveFunction.ANNUAl);
							op.setOutputPerApertureSquareMeter(true);
							break;
						case 4:
							op.setOjectiveFunction(ObjectiveFunction.DAILY);
							op.setNetProfit(true);
							break;
						case 5:
							op.setOjectiveFunction(ObjectiveFunction.ANNUAl);
							op.setNetProfit(true);
							break;
						}
						op.setFoundation(foundation);
						op.setupFoundationConstraint();
						op.evolve();
						if (choice == options[0]) {
							break;
						}
					}
				}
			}
		}

	}

}
