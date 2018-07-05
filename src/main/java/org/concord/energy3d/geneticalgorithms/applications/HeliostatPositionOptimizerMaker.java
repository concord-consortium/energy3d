package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.util.SpringUtilities;

/**
 * @author Charles Xie
 *
 */
public class HeliostatPositionOptimizerMaker extends OptimizerMaker {

	private HeliostatPositionOptimizer op;

	@Override
	public void make(final Foundation foundation) {

		final List<Mirror> heliostats = foundation.getHeliostats();
		if (heliostats.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no heliostat on this foundation.", "Information", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		final JPanel panel = new JPanel(new SpringLayout());
		panel.add(new JLabel("Solution:"));
		final JComboBox<String> solutionComboBox = new JComboBox<String>(new String[] { "Positions" });
		panel.add(solutionComboBox);
		panel.add(new JLabel());

		panel.add(new JLabel("Fitness function:"));
		final JComboBox<String> fitnessComboBox = new JComboBox<String>(new String[] { "Daily Output", "Annual Output" });
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
		final JTextField mutationRateField = new JTextField(selectedSearchMethod == 1 ? "0" : EnergyPanel.FIVE_DECIMALS.format(mutationRate));
		mutationRateField.setEnabled(selectedSearchMethod != 1);
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

		final JLabel localSearchRadiusLabel = new JLabel("Local search radius:");
		final JTextField localSearchRadiusField = new JTextField(EnergyPanel.FIVE_DECIMALS.format(localSearchRadius));
		final JLabel localSearchRadiusLabel2 = new JLabel("<html><font size=2>(0, 1]</font></html>");
		localSearchRadiusLabel.setEnabled(selectedSearchMethod > 0);
		localSearchRadiusField.setEnabled(selectedSearchMethod > 0);
		localSearchRadiusLabel2.setEnabled(selectedSearchMethod > 0);

		panel.add(new JLabel("Search method:"));
		final JComboBox<String> searchMethodComboBox = new JComboBox<String>(new String[] { "Global Search (Uniform Selection)", "Local Search (Random Optimization)" });
		searchMethodComboBox.setSelectedIndex(selectedSearchMethod);
		searchMethodComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					final boolean enabled = searchMethodComboBox.getSelectedIndex() > 0;
					localSearchRadiusLabel.setEnabled(enabled);
					localSearchRadiusField.setEnabled(enabled);
					localSearchRadiusLabel2.setEnabled(enabled);
					mutationRateField.setEnabled(!enabled);
					mutationRateField.setText(enabled ? "0" : EnergyPanel.FIVE_DECIMALS.format(mutationRate));
				}
			}
		});
		panel.add(searchMethodComboBox);
		panel.add(new JLabel());

		panel.add(localSearchRadiusLabel);
		panel.add(localSearchRadiusField);
		panel.add(localSearchRadiusLabel2);

		SpringUtilities.makeCompactGrid(panel, 11, 3, 6, 6, 6, 6);

		final Object[] options = new Object[] { "OK", "Cancel", "Fitness", "Gene" };
		final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION, null, options, options[0]);
		final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Genetic Algorithm Options for Optimizing Heliostat Positions");

		while (true) {
			dialog.setVisible(true);
			final Object choice = optionPane.getValue();
			if (choice == options[1] || choice == null) {
				break;
			} else if (choice == options[2]) {
				if (op != null) {
					if (op.getFittestOfGenerations() != null) {
						new FitnessGraph(op.getFittestOfGenerations()).display("Fitness Trend of Previous Run");
					}
				} else {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "No previous result is available.", "Information", JOptionPane.INFORMATION_MESSAGE);
				}
			} else if (choice == options[3]) {
				if (op != null) {
					op.population.sort();
					for (int i = 0; i < op.population.size(); i++) {
						System.out.println(i + " = " + op.individualToString(op.population.getIndividual(i)));
					}
					new GeneGraph(op.getPopulation().getIndividuals()).display();
				} else {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "No data is available.", "Information", JOptionPane.INFORMATION_MESSAGE);
				}
			} else {
				boolean ok = true;
				selectedSearchMethod = searchMethodComboBox.getSelectedIndex();
				try {
					populationSize = Integer.parseInt(populationField.getText());
					maximumGenerations = Integer.parseInt(generationField.getText());
					convergenceThreshold = Double.parseDouble(convergenceThresholdField.getText());
					if (selectedSearchMethod != 1) { // no mutation for local research
						mutationRate = Double.parseDouble(mutationRateField.getText());
					}
					localSearchRadius = Double.parseDouble(localSearchRadiusField.getText());
				} catch (final NumberFormatException exception) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Invalid value!", "Error", JOptionPane.ERROR_MESSAGE);
					ok = false;
				}
				if (ok) {
					if (populationSize <= 0) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Population size must be greater than zero.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else if (maximumGenerations < 0) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Maximum generations cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else if (mutationRate < 0 || mutationRate > 1) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Mutation rate must be between 0 and 1.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else if (convergenceThreshold < 0 || convergenceThreshold > 0.1) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Convergence threshold must be between 0 and 0.1.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else if (localSearchRadius < 0 || localSearchRadius > 1) {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "Local search radius must be between 0 and 1.", "Range Error", JOptionPane.ERROR_MESSAGE);
					} else {
						selectedFitnessFunction = fitnessComboBox.getSelectedIndex();
						selectedSelectionMethod = selectionComboBox.getSelectedIndex();
						op = new HeliostatPositionOptimizer(populationSize, foundation.getHeliostats().size() * 2, 0);
						op.setSelectionMethod(selectedSelectionMethod);
						op.setConvergenceThreshold(convergenceThreshold);
						op.setMaximumGenerations(maximumGenerations);
						op.setMutationRate(mutationRate);
						op.setOjectiveFunction(selectedFitnessFunction);
						op.setSearchMethod(selectedSearchMethod);
						op.setLocalSearchRadius(localSearchRadius);
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
