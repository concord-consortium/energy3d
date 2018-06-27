package org.concord.energy3d.geneticalgorithms.applications;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.util.SpringUtilities;

/**
 * @author Charles Xie
 *
 */
public class BuildingOrientationOptimizerMaker extends OptimizerMaker {

	private BuildingOrientationOptimizer op;

	@Override
	public void make(final Foundation foundation) {

		final List<Wall> walls = foundation.getWalls();
		if (walls.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no building on this foundation.", "Information", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		final JPanel panel = new JPanel(new SpringLayout());

		panel.add(new JLabel("Solution:"));
		final JComboBox<String> solutionComboBox = new JComboBox<String>(new String[] { "Building Orientation" });
		panel.add(solutionComboBox);
		panel.add(new JLabel());

		panel.add(new JLabel("Fitness function:"));
		final JComboBox<String> fitnessComboBox = new JComboBox<String>(new String[] { "Daily Energy Use", "Annual Energy Use" });
		fitnessComboBox.setSelectedIndex(selectedFitnessFunction);
		panel.add(fitnessComboBox);
		panel.add(new JLabel());

		panel.add(new JLabel("Chromosome type:"));
		final JComboBox<String> typeComboBox = new JComboBox<String>(new String[] { "Continuous" });
		panel.add(typeComboBox);
		panel.add(new JLabel());

		panel.add(new JLabel("Selection method:"));
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
		panel.add(new JLabel("<html><font size=2>Not %</font></html>"));

		panel.add(new JLabel("Convergence criterion:"));
		final JComboBox<String> convergenceCriterionComboBox = new JComboBox<String>(new String[] { "Bitwise (Nominal)" });
		panel.add(convergenceCriterionComboBox);
		panel.add(new JLabel());

		panel.add(new JLabel("Convergence threshold:"));
		final JTextField convergenceThresholdField = new JTextField(EnergyPanel.FIVE_DECIMALS.format(convergenceThreshold));
		panel.add(convergenceThresholdField);
		panel.add(new JLabel("<html><font size=2>Not %</font></html>"));

		panel.add(new JLabel("Initial scope:"));
		final JComboBox<String> scopeComboBox = new JComboBox<String>(new String[] { "Entire Range", "Nearest Niche" });
		scopeComboBox.setSelectedIndex(selectedScope);
		panel.add(scopeComboBox);
		panel.add(new JLabel());

		panel.add(new JLabel("Niche confinement radius:"));
		final JTextField nicheConfinmentRadiusField = new JTextField(EnergyPanel.FIVE_DECIMALS.format(nicheConfinementRadius));
		panel.add(nicheConfinmentRadiusField);
		panel.add(new JLabel("<html><font size=2>(0, 1]</font></html>"));

		SpringUtilities.makeCompactGrid(panel, 11, 3, 6, 6, 6, 6);

		final Object[] options = new Object[] { "OK", "Cancel", "View" };
		final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION, null, options, options[0]);
		final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Genetic Algorithm Options for Optimizing Building Orientation");

		while (true) {
			dialog.setVisible(true);
			final Object choice = optionPane.getValue();
			if (choice == options[1] || choice == null) {
				break;
			} else if (choice == options[2]) {
				if (op != null) {
					op.population.sort();
					for (int i = 0; i < op.population.size() / 2; i++) {
						System.out.println(i + " = " + op.individualToString(op.population.getIndividual(i)));
					}
				}
			} else {
				boolean ok = true;
				try {
					populationSize = Integer.parseInt(populationField.getText());
					maximumGenerations = Integer.parseInt(generationField.getText());
					convergenceThreshold = Double.parseDouble(convergenceThresholdField.getText());
					mutationRate = Double.parseDouble(mutationRateField.getText());
					nicheConfinementRadius = Double.parseDouble(nicheConfinmentRadiusField.getText());
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
					} else if (nicheConfinementRadius < 0 || nicheConfinementRadius > 1) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Niche confinement radius must be between 0 and 1.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else {
						selectedFitnessFunction = fitnessComboBox.getSelectedIndex();
						selectedSelectionMethod = selectionComboBox.getSelectedIndex();
						selectedScope = scopeComboBox.getSelectedIndex();
						op = new BuildingOrientationOptimizer(populationSize, 2, 0);
						op.setSelectionMethod(selectedSelectionMethod);
						op.setConvergenceThreshold(convergenceThreshold);
						op.setMaximumGenerations(maximumGenerations);
						op.setMutationRate(mutationRate);
						op.setFoundation(foundation);
						op.setOjectiveFunction(selectedFitnessFunction);
						op.setNicheConfinement(selectedScope == 1);
						op.setNicheConfinementRadius(nicheConfinementRadius);
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
