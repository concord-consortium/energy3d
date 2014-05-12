package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.concord.energy3d.gui.EnergyPanel.UpdateRadiation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.Specifications;

/**
 * @author Charles Xie
 * 
 */
class SpecsDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final static DecimalFormat FORMAT1 = new DecimalFormat("#0.##");
	private final static DecimalFormat FORMAT2 = new DecimalFormat("##");

	public SpecsDialog() {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Specifications");

		getContentPane().setLayout(new BorderLayout());
		final JPanel panel = new JPanel(new GridLayout(5, 3, 8, 8));
		panel.setBorder(new EmptyBorder(15, 15, 15, 15));
		getContentPane().add(panel, BorderLayout.CENTER);

		// set the budget limit
		panel.add(new JLabel("Maximum Budget: "));
		final JTextField budgetTextField = new JTextField(FORMAT2.format(Specifications.getInstance().getMaximumBudget()));
		panel.add(budgetTextField);
		budgetTextField.setColumns(6);
		panel.add(new JLabel("Dollars"));

		// set the minimum area
		panel.add(new JLabel("Minimum Area: "));
		final JTextField minimumAreaTextField = new JTextField(FORMAT1.format(Specifications.getInstance().getMinimumArea()));
		panel.add(minimumAreaTextField);
		minimumAreaTextField.setColumns(6);
		panel.add(new JLabel("\u33A1"));

		// set the maximum area
		panel.add(new JLabel("Maximum Area: "));
		final JTextField maximumAreaTextField = new JTextField(FORMAT1.format(Specifications.getInstance().getMaximumArea()));
		panel.add(maximumAreaTextField);
		maximumAreaTextField.setColumns(6);
		panel.add(new JLabel("\u33A1"));

		// set the minimum height
		panel.add(new JLabel("Minimum Height: "));
		final JTextField minimumHeightTextField = new JTextField(FORMAT1.format(Specifications.getInstance().getMinimumHeight()));
		panel.add(minimumHeightTextField);
		minimumHeightTextField.setColumns(6);
		panel.add(new JLabel("m"));

		// set the maximum height
		panel.add(new JLabel("Maximum Height: "));
		final JTextField maximumHeightTextField = new JTextField(FORMAT1.format(Specifications.getInstance().getMaximumHeight()));
		panel.add(maximumHeightTextField);
		maximumHeightTextField.setColumns(6);
		panel.add(new JLabel("m"));

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
				if (minimumHeight < 0 || maximumHeight < 0) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Minimum or maximum height cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				Specifications.getInstance().setMaximumBudget(maximumBudget);
				Specifications.getInstance().setMaximumArea(maximumArea);
				Specifications.getInstance().setMinimumArea(minimumArea);
				Specifications.getInstance().setMaximumHeight(maximumHeight);
				Specifications.getInstance().setMinimumHeight(minimumHeight);
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