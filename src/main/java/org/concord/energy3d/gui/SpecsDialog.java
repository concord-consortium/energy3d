package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.concord.energy3d.gui.EnergyPanel.UpdateRadiation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.Specifications;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 * 
 */
class SpecsDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final static DecimalFormat FORMAT1 = new DecimalFormat("#0.##");
	private final static DecimalFormat FORMAT2 = new DecimalFormat("##");

	private final JCheckBox budgetCheckBox;
	private final JTextField budgetTextField;
	private final JLabel budgetLabel;
	private final JCheckBox minimumAreaCheckBox;
	private final JTextField minimumAreaTextField;
	private final JLabel minimumAreaLabel;
	private final JCheckBox maximumAreaCheckBox;
	private final JTextField maximumAreaTextField;
	private final JLabel maximumAreaLabel;
	private final JCheckBox minimumHeightCheckBox;
	private final JTextField minimumHeightTextField;
	private final JLabel minimumHeightLabel;
	private final JCheckBox maximumHeightCheckBox;
	private final JTextField maximumHeightTextField;
	private final JLabel maximumHeightLabel;

	private void enableBudgetItems(boolean b) {
		budgetTextField.setEnabled(b);
		budgetLabel.setEnabled(b);
	}

	private void enableAreaItems(boolean b) {
		minimumAreaTextField.setEnabled(b);
		maximumAreaTextField.setEnabled(b);
		minimumAreaLabel.setEnabled(b);
		maximumAreaLabel.setEnabled(b);
	}

	private void enableHeightItems(boolean b) {
		minimumHeightTextField.setEnabled(b);
		maximumHeightTextField.setEnabled(b);
		minimumHeightLabel.setEnabled(b);
		maximumHeightLabel.setEnabled(b);
	}

	public SpecsDialog() {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Specifications");

		getContentPane().setLayout(new BorderLayout());
		final JPanel panel = new JPanel(new GridLayout(5, 3, 8, 8));
		panel.setBorder(new EmptyBorder(15, 15, 15, 15));
		getContentPane().add(panel, BorderLayout.CENTER);

		// set the budget limit
		budgetCheckBox = new JCheckBox("Maximum Budget: ", Specifications.getInstance().isBudgetEnabled());
		budgetCheckBox.setToolTipText("Select to apply a budget");
		budgetTextField = new JTextField(FORMAT2.format(Specifications.getInstance().getMaximumBudget()), 6);
		budgetLabel = new JLabel("Dollars");
		panel.add(budgetCheckBox);
		panel.add(budgetTextField);
		panel.add(budgetLabel);

		budgetCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				enableBudgetItems(budgetCheckBox.isSelected());
			}
		});
		enableBudgetItems(Specifications.getInstance().isBudgetEnabled());

		// set the minimum and maximum areas
		minimumAreaCheckBox = new JCheckBox("Minimum Area: ", Specifications.getInstance().isAreaEnabled());
		minimumAreaCheckBox.setToolTipText("Select to apply an area requirement");
		minimumAreaTextField = new JTextField(FORMAT1.format(Specifications.getInstance().getMinimumArea()), 6);
		minimumAreaLabel = new JLabel("\u33A1");
		panel.add(minimumAreaCheckBox);
		panel.add(minimumAreaTextField);
		panel.add(minimumAreaLabel);

		maximumAreaCheckBox = new JCheckBox("Maximum Area: ", Specifications.getInstance().isAreaEnabled());
		maximumAreaCheckBox.setToolTipText(minimumAreaCheckBox.getToolTipText());
		maximumAreaTextField = new JTextField(FORMAT1.format(Specifications.getInstance().getMaximumArea()), 6);
		maximumAreaLabel = new JLabel("\u33A1");
		panel.add(maximumAreaCheckBox);
		panel.add(maximumAreaTextField);
		panel.add(maximumAreaLabel);

		minimumAreaCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean b = minimumAreaCheckBox.isSelected();
				Util.selectSilently(maximumAreaCheckBox, b);
				enableAreaItems(b);
			}
		});
		maximumAreaCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean b = maximumAreaCheckBox.isSelected();
				Util.selectSilently(minimumAreaCheckBox, b);
				enableAreaItems(b);
			}
		});
		enableAreaItems(Specifications.getInstance().isAreaEnabled());

		// set the minimum and maximum heights
		minimumHeightCheckBox = new JCheckBox("Minimum Height: ", Specifications.getInstance().isHeightEnabled());
		minimumHeightCheckBox.setToolTipText("Select to apply a height requirement");
		minimumHeightTextField = new JTextField(FORMAT1.format(Specifications.getInstance().getMinimumHeight()), 6);
		minimumHeightLabel = new JLabel("m");
		panel.add(minimumHeightCheckBox);
		panel.add(minimumHeightTextField);
		panel.add(minimumHeightLabel);

		maximumHeightCheckBox = new JCheckBox("Maximum Height: ", Specifications.getInstance().isHeightEnabled());
		maximumHeightCheckBox.setToolTipText(minimumHeightCheckBox.getToolTipText());
		maximumHeightTextField = new JTextField(FORMAT1.format(Specifications.getInstance().getMaximumHeight()), 6);
		maximumHeightLabel = new JLabel("m");
		panel.add(maximumHeightCheckBox);
		panel.add(maximumHeightTextField);
		panel.add(maximumHeightLabel);

		minimumHeightCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean b = minimumHeightCheckBox.isSelected();
				Util.selectSilently(maximumHeightCheckBox, b);
				enableHeightItems(b);
			}
		});
		maximumHeightCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean b = maximumHeightCheckBox.isSelected();
				Util.selectSilently(minimumHeightCheckBox, b);
				enableHeightItems(b);
			}
		});
		enableHeightItems(Specifications.getInstance().isHeightEnabled());

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				int maximumBudget;
				double minimumArea, maximumArea, minimumHeight, maximumHeight;
				try {
					maximumBudget = (int) Double.parseDouble(budgetTextField.getText());
					minimumArea = Double.parseDouble(minimumAreaTextField.getText());
					maximumArea = Double.parseDouble(maximumAreaTextField.getText());
					minimumHeight = Double.parseDouble(minimumHeightTextField.getText());
					maximumHeight = Double.parseDouble(maximumHeightTextField.getText());
				} catch (final NumberFormatException err) {
					err.printStackTrace();
					JOptionPane.showMessageDialog(SpecsDialog.this, "Invalid input: " + err.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}
				// range check
				if (maximumBudget <= 1000) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Your budget is too low to construct a building.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (minimumArea < 0 || maximumArea < 0) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Minimum or maximum area cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (minimumArea >= maximumArea) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Minimum area must be less than maximum area.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (minimumHeight < 0 || maximumHeight < 0) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Minimum or maximum height cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (minimumHeight >= maximumHeight) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Minimum height must be less than maximum height.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				Specifications.getInstance().setMaximumBudget(maximumBudget);
				Specifications.getInstance().setMaximumArea(maximumArea);
				Specifications.getInstance().setMinimumArea(minimumArea);
				Specifications.getInstance().setMaximumHeight(maximumHeight);
				Specifications.getInstance().setMinimumHeight(minimumHeight);
				Specifications.getInstance().setBudgetEnabled(budgetCheckBox.isSelected());
				Specifications.getInstance().setAreaEnabled(minimumAreaCheckBox.isSelected());
				Specifications.getInstance().setHeightEnabled(minimumHeightCheckBox.isSelected());
				Scene.getInstance().setEdited(true);
				EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
				SpecsDialog.this.dispose();
			}
		});
		okButton.setActionCommand("OK");
		buttonPanel.add(okButton);
		getRootPane().setDefaultButton(okButton);

		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				SpecsDialog.this.dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPanel.add(cancelButton);

		pack();
		setLocationRelativeTo(MainFrame.getInstance());

	}

}