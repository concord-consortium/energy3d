package org.concord.energy3d.geneticalgorithms.applications;

import java.awt.event.ItemEvent;
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
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.util.SpringUtilities;

/**
 * @author Charles Xie
 */
public class SolarPanelTiltAngleOptimizerMaker extends OptimizerMaker {

    @Override
    public void make(final Foundation foundation) {

        final List<Rack> racks = foundation.getRacks();
        if (racks.isEmpty()) {
            JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no solar panel rack on this foundation.", "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        final JPanel panel = new JPanel(new SpringLayout());
        panel.add(new JLabel("Solution:"));
        final JComboBox<String> solutionComboBox = new JComboBox<>(new String[]{"Tilt Angles"});
        panel.add(solutionComboBox);
        panel.add(new JLabel());

        panel.add(new JLabel("Objective:"));
        final JComboBox<String> objectiveComboBox = new JComboBox<>(new String[]{"Daily Output", "Annual Output"});
        objectiveComboBox.setSelectedIndex(selectedObjectiveFunction);
        panel.add(objectiveComboBox);
        panel.add(new JLabel());

        panel.add(new JLabel("Chromosome type:"));
        final JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Continuous"});
        panel.add(typeComboBox);
        panel.add(new JLabel());

        panel.add(new JLabel("Selection method:"));
        final JComboBox<String> selectionComboBox = new JComboBox<>(new String[]{"Roulette Wheel", "Tournament"});
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
        final JComboBox<String> convergenceCriterionComboBox = new JComboBox<>(new String[]{"Bitwise (Nominal)"});
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
        final JComboBox<String> searchMethodComboBox = new JComboBox<>(new String[]{"Global Search (Uniform Selection)", "Local Search (Random Optimization)"});
        searchMethodComboBox.setSelectedIndex(selectedSearchMethod);
        searchMethodComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                final boolean enabled = searchMethodComboBox.getSelectedIndex() > 0;
                localSearchRadiusLabel.setEnabled(enabled);
                localSearchRadiusField.setEnabled(enabled);
                localSearchRadiusLabel2.setEnabled(enabled);
                mutationRateField.setEnabled(!enabled);
                mutationRateField.setText(enabled ? "0" : EnergyPanel.FIVE_DECIMALS.format(mutationRate));
            }
        });
        panel.add(searchMethodComboBox);
        panel.add(new JLabel());

        panel.add(localSearchRadiusLabel);
        panel.add(localSearchRadiusField);
        panel.add(localSearchRadiusLabel2);

        SpringUtilities.makeCompactGrid(panel, 11, 3, 6, 6, 6, 6);

        final Object[] options = new Object[]{"OK", "Cancel", "Previous Results"};
        final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION, null, options, options[0]);
        final JDialog dialog = optionPane.createDialog(MainFrame.getInstance(), "Genetic Algorithm Options for Finding Optimal Solar Panel Tilt Angle");

        while (true) {
            dialog.setVisible(true);
            final Object choice = optionPane.getValue();
            if (choice == options[1] || choice == null) {
                break;
            } else if (choice == options[2]) {
                if (op != null) {
                    op.population.sort();
                    for (int i = 0; i < op.population.size(); i++) {
                        System.out.println(i + " = " + op.individualToString(op.population.getIndividual(i)));
                    }
                    op.displayResults(choice.toString());
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
                        selectedObjectiveFunction = objectiveComboBox.getSelectedIndex();
                        selectedSelectionMethod = selectionComboBox.getSelectedIndex();
                        op = new SolarPanelTiltAngleOptimizer(populationSize, foundation.getRacks().size(), 0);
                        op.setSelectionMethod(selectedSelectionMethod);
                        op.setConvergenceThreshold(convergenceThreshold);
                        op.setMaximumGenerations(maximumGenerations);
                        op.setMutationRate(mutationRate);
                        op.setOjectiveFunction(selectedObjectiveFunction);
                        op.setSearchMethod(selectedSearchMethod);
                        op.setLocalSearchRadius(localSearchRadius);
                        op.setFoundation(foundation);
                        op.evolve();
                        if (choice == options[0]) {
                            break;
                        }
                    }
                }
            }

        }

    }

    @Override
    public void run(final Foundation foundation, final boolean local, final boolean daily, final boolean profit, final int population, final int generations, final float mutation, final float convergence, final float searchRadius) {
        op = new SolarPanelTiltAngleOptimizer(population > 0 ? population : populationSize, foundation.getRacks().size(), 0);
        op.setSelectionMethod(selectedSelectionMethod);
        op.setConvergenceThreshold(convergence > 0 ? convergence : convergenceThreshold);
        op.setMaximumGenerations(generations > 0 ? generations : maximumGenerations);
        op.setMutationRate(mutation >= 0 ? mutation : mutationRate);
        op.setOjectiveFunction(selectedObjectiveFunction);
        op.setSearchMethod(local ? Optimizer.LOCAL_SEARCH_RANDOM_OPTIMIZATION : Optimizer.GLOBAL_SEARCH_UNIFORM_SELECTION);
        op.setLocalSearchRadius(searchRadius > 0 ? searchRadius : 0.05);
        op.setFoundation(foundation);
        op.evolve();
    }

}