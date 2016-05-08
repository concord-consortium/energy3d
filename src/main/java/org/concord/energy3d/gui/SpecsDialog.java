package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.concord.energy3d.gui.EnergyPanel.UpdateRadiation;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.simulation.DesignSpecs;

/**
 * @author Charles Xie
 * 
 */
class SpecsDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final static DecimalFormat FORMAT1 = new DecimalFormat("#0.##");
	private final static DecimalFormat FORMAT2 = new DecimalFormat("##");

	private final JCheckBox budgetCheckBox;
	private final JTextField budgetField;

	private final JCheckBox areaCheckBox;
	private final JTextField minimumAreaField, maximumAreaField;

	private final JCheckBox heightCheckBox;
	private final JTextField minimumHeightField, maximumHeightField;

	private final JCheckBox windowToFloorRatioCheckBox;
	private final JTextField minimumWindowToFloorRatioField, maximumWindowToFloorRatioField;

	private final JCheckBox numberOfSolarPanelsCheckBox;
	private final JTextField minimumNumberOfSolarPanelsField, maximumNumberOfSolarPanelsField;

	private final JCheckBox numberOfWindowsCheckBox;
	private final JTextField minimumNumberOfWindowsField, maximumNumberOfWindowsField;

	private final JCheckBox numberOfWallsCheckBox;
	private final JTextField minimumNumberOfWallsField, maximumNumberOfWallsField;

	private void enableBudgetItems(boolean b) {
		budgetField.setEnabled(b);
	}

	private void enableWindowItems(boolean b) {
		minimumNumberOfWindowsField.setEnabled(b);
		maximumNumberOfWindowsField.setEnabled(b);
	}

	private void enableSolarPanelItems(boolean b) {
		minimumNumberOfSolarPanelsField.setEnabled(b);
		maximumNumberOfSolarPanelsField.setEnabled(b);
	}

	private void enableWindowToFloorRatioItems(boolean b) {
		minimumWindowToFloorRatioField.setEnabled(b);
		maximumWindowToFloorRatioField.setEnabled(b);
	}

	private void enableAreaItems(boolean b) {
		minimumAreaField.setEnabled(b);
		maximumAreaField.setEnabled(b);
	}

	private void enableHeightItems(boolean b) {
		minimumHeightField.setEnabled(b);
		maximumHeightField.setEnabled(b);
	}

	private void enableNumberOfWallsItems(boolean b) {
		minimumNumberOfWallsField.setEnabled(b);
		maximumNumberOfWallsField.setEnabled(b);
	}

	public SpecsDialog() {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Specifications");

		final DesignSpecs specs = Scene.getInstance().getDesignSpecs();

		getContentPane().setLayout(new BorderLayout());
		JPanel container = new JPanel(new GridLayout(1, 2, 10, 10));
		container.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		getContentPane().add(container, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		container.add(panel);

		// set the budget limit

		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createTitledBorder("Budget ($)"));
		panel.add(p);
		budgetCheckBox = new JCheckBox("", specs.isBudgetEnabled());
		budgetCheckBox.setToolTipText("Select to apply a budget");
		budgetField = new JTextField(FORMAT2.format(specs.getMaximumBudget()), 6);
		p.add(budgetCheckBox);
		p.add(new JLabel("<"));
		p.add(budgetField);
		budgetCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				enableBudgetItems(budgetCheckBox.isSelected());
			}
		});
		enableBudgetItems(specs.isBudgetEnabled());

		// set the maximum number of windows allowed

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createTitledBorder("Number Of Windows"));
		panel.add(p);
		numberOfWindowsCheckBox = new JCheckBox("", specs.isNumberOfWindowsEnabled());
		numberOfWindowsCheckBox.setToolTipText("Select to apply a requirement of the number of windows");
		p.add(numberOfWindowsCheckBox);
		minimumNumberOfWindowsField = new JTextField("" + specs.getMinimumNumberOfWindows(), 6);
		p.add(minimumNumberOfWindowsField);
		p.add(new JLabel("-"));
		maximumNumberOfWindowsField = new JTextField("" + specs.getMaximumNumberOfWindows(), 6);
		p.add(maximumNumberOfWindowsField);
		numberOfWindowsCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				enableWindowItems(numberOfWindowsCheckBox.isSelected());
			}
		});
		enableWindowItems(specs.isNumberOfWindowsEnabled());

		// set the maximum number of solar panels allowed

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createTitledBorder("Number Of Solar Panels"));
		panel.add(p);
		numberOfSolarPanelsCheckBox = new JCheckBox("", specs.isNumberOfSolarPanelsEnabled());
		numberOfSolarPanelsCheckBox.setToolTipText("Select to apply a requirement of the number of solar panels");
		p.add(numberOfSolarPanelsCheckBox);
		minimumNumberOfSolarPanelsField = new JTextField("" + specs.getMinimumNumberOfSolarPanels(), 6);
		p.add(minimumNumberOfSolarPanelsField);
		p.add(new JLabel("-"));
		maximumNumberOfSolarPanelsField = new JTextField("" + specs.getMaximumNumberOfSolarPanels(), 6);
		p.add(maximumNumberOfSolarPanelsField);
		numberOfSolarPanelsCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				enableSolarPanelItems(numberOfSolarPanelsCheckBox.isSelected());
			}
		});
		enableSolarPanelItems(specs.isNumberOfSolarPanelsEnabled());

		// set minimum and maximum numbers of walls

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createTitledBorder("Number of Walls"));
		panel.add(p);
		numberOfWallsCheckBox = new JCheckBox("", specs.isNumberOfWallsEnabled());
		numberOfWallsCheckBox.setToolTipText("Select to apply a requirement for the number of walls");
		numberOfWallsCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				enableNumberOfWallsItems(numberOfWallsCheckBox.isSelected());
			}
		});
		p.add(numberOfWallsCheckBox);
		minimumNumberOfWallsField = new JTextField(specs.getMinimumNumberOfWalls() + "", 6);
		p.add(minimumNumberOfWallsField);
		p.add(new JLabel("-"));
		maximumNumberOfWallsField = new JTextField(specs.getMaximumNumberOfWalls() + "", 6);
		p.add(maximumNumberOfWallsField);
		enableNumberOfWallsItems(specs.isNumberOfWallsEnabled());

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		container.add(panel);

		// set the minimum and maximum window to floor area ratio

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createTitledBorder("Window-to-Floor Area Ratio"));
		panel.add(p);
		windowToFloorRatioCheckBox = new JCheckBox("", specs.isWindowToFloorRatioEnabled());
		windowToFloorRatioCheckBox.setToolTipText("Select to apply a requirement of window-to-floor area ratio");
		windowToFloorRatioCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				enableWindowToFloorRatioItems(windowToFloorRatioCheckBox.isSelected());
			}
		});
		p.add(windowToFloorRatioCheckBox);
		minimumWindowToFloorRatioField = new JTextField(FORMAT1.format(specs.getMinimumWindowToFloorRatio()), 6);
		p.add(minimumWindowToFloorRatioField);
		p.add(new JLabel("-"));
		maximumWindowToFloorRatioField = new JTextField(FORMAT1.format(specs.getMaximumWindowToFloorRatio()), 6);
		p.add(maximumWindowToFloorRatioField);
		enableWindowToFloorRatioItems(specs.isWindowToFloorRatioEnabled());

		// set the minimum and maximum areas

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createTitledBorder("Area of Building (\u33A1)"));
		panel.add(p);
		areaCheckBox = new JCheckBox("", specs.isAreaEnabled());
		areaCheckBox.setToolTipText("Select to apply a requirement of building area");
		areaCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				enableAreaItems(areaCheckBox.isSelected());
			}
		});
		p.add(areaCheckBox);
		minimumAreaField = new JTextField(FORMAT1.format(specs.getMinimumArea()), 6);
		p.add(minimumAreaField);
		p.add(new JLabel("-"));
		maximumAreaField = new JTextField(FORMAT1.format(specs.getMaximumArea()), 6);
		p.add(maximumAreaField);
		enableAreaItems(specs.isAreaEnabled());

		// set the minimum and maximum heights

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createTitledBorder("Height of Building (m)"));
		panel.add(p);
		heightCheckBox = new JCheckBox("", specs.isHeightEnabled());
		heightCheckBox.setToolTipText("Select to apply a height requirement");
		heightCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				enableHeightItems(heightCheckBox.isSelected());
			}
		});
		p.add(heightCheckBox);
		minimumHeightField = new JTextField(FORMAT1.format(specs.getMinimumHeight()), 6);
		p.add(minimumHeightField);
		p.add(new JLabel("-"));
		maximumHeightField = new JTextField(FORMAT1.format(specs.getMaximumHeight()), 6);
		p.add(maximumHeightField);

		enableHeightItems(specs.isHeightEnabled());

		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setBorder(BorderFactory.createTitledBorder("TBD"));
		JCheckBox cb = new JCheckBox();
		cb.setEnabled(false);
		p.add(cb);
		JTextField tf = new JTextField("", 6);
		tf.setEnabled(false);
		p.add(tf);
		p.add(new JLabel("-"));
		tf = new JTextField("", 6);
		tf.setEnabled(false);
		p.add(tf);
		panel.add(p);

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				int maximumBudget;
				int minimumNumberOfSolarPanels, maximumNumberOfSolarPanels;
				int minimumNumberOfWindows, maximumNumberOfWindows;
				int minimumNumberOfWalls, maximumNumberOfWalls;
				double minimumArea, maximumArea, minimumHeight, maximumHeight;
				double minimumWindowToFloorRatio, maximumWindowToFloorRatio;
				try {
					maximumBudget = (int) Double.parseDouble(budgetField.getText());
					minimumNumberOfWindows = (int) Double.parseDouble(minimumNumberOfWindowsField.getText());
					maximumNumberOfWindows = (int) Double.parseDouble(maximumNumberOfWindowsField.getText());
					minimumNumberOfSolarPanels = (int) Double.parseDouble(minimumNumberOfSolarPanelsField.getText());
					maximumNumberOfSolarPanels = (int) Double.parseDouble(maximumNumberOfSolarPanelsField.getText());
					minimumNumberOfWalls = (int) Double.parseDouble(minimumNumberOfWallsField.getText());
					maximumNumberOfWalls = (int) Double.parseDouble(maximumNumberOfWallsField.getText());
					minimumArea = Double.parseDouble(minimumAreaField.getText());
					maximumArea = Double.parseDouble(maximumAreaField.getText());
					minimumHeight = Double.parseDouble(minimumHeightField.getText());
					maximumHeight = Double.parseDouble(maximumHeightField.getText());
					minimumWindowToFloorRatio = Double.parseDouble(minimumWindowToFloorRatioField.getText());
					maximumWindowToFloorRatio = Double.parseDouble(maximumWindowToFloorRatioField.getText());
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

				if (minimumNumberOfWindows < 0 || maximumNumberOfWindows < 0) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Number of windows cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (minimumNumberOfWindows >= maximumNumberOfWindows) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Maximum number of windows must be greater than minimum.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (minimumNumberOfWalls < 3 || maximumNumberOfWalls < 3) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Number of walls must be greater than 2 to form a closed building.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (minimumNumberOfWalls >= maximumNumberOfWalls) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Maximum number of walls must be greater than minimum.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (minimumNumberOfSolarPanels < 0 || maximumNumberOfSolarPanels < 0) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Number of solar panels cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (minimumNumberOfSolarPanels >= maximumNumberOfSolarPanels) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Maximum number of solar panels must be greater than minimum.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (minimumWindowToFloorRatio <= 0 || maximumWindowToFloorRatio <= 0) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Window-to-floor ratio must be positive.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (minimumWindowToFloorRatio >= maximumWindowToFloorRatio) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Maximum window-to-floor area ratio must be greater than minimum.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (minimumArea < 0 || maximumArea < 0) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Area cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (minimumArea >= maximumArea) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Minimum area must be less than maximum area.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (minimumHeight < 0 || maximumHeight < 0) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Height cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (minimumHeight >= maximumHeight) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Minimum height must be less than maximum height.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				specs.setBudgetEnabled(budgetCheckBox.isSelected());
				specs.setMaximumBudget(maximumBudget);

				specs.setNumberOfWindowsEnabled(numberOfWindowsCheckBox.isSelected());
				specs.setMinimumNumberOfWindows(minimumNumberOfWindows);
				specs.setMaximumNumberOfWindows(maximumNumberOfWindows);

				specs.setNumberOfSolarPanelsEnabled(numberOfSolarPanelsCheckBox.isSelected());
				specs.setMinimumNumberOfSolarPanels(minimumNumberOfSolarPanels);
				specs.setMaximumNumberOfSolarPanels(maximumNumberOfSolarPanels);

				specs.setNumberOfWallsEnabled(numberOfWallsCheckBox.isSelected());
				specs.setMaximumNumberOfWalls(maximumNumberOfWalls);
				specs.setMinimumNumberOfWalls(minimumNumberOfWalls);

				specs.setAreaEnabled(areaCheckBox.isSelected());
				specs.setMaximumArea(maximumArea);
				specs.setMinimumArea(minimumArea);

				specs.setHeightEnabled(heightCheckBox.isSelected());
				specs.setMaximumHeight(maximumHeight);
				specs.setMinimumHeight(minimumHeight);

				specs.setWindowToFloorRatioEnabled(windowToFloorRatioCheckBox.isSelected());
				specs.setMinimumWindowToFloorRatio(minimumWindowToFloorRatio);
				specs.setMaximumWindowToFloorRatio(maximumWindowToFloorRatio);

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