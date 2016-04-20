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
import org.concord.energy3d.simulation.DesignSpecs;
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

	private final JCheckBox minimumWindowToFloorRatioCheckBox;
	private final JTextField minimumWindowToFloorRatioTextField;
	private final JLabel minimumWindowToFloorRatioLabel;
	private final JCheckBox maximumWindowToFloorRatioCheckBox;
	private final JTextField maximumWindowToFloorRatioTextField;
	private final JLabel maximumWindowToFloorRatioLabel;

	private final JCheckBox maximumSolarPanelCheckBox;
	private final JTextField maximumSolarPanelTextField;
	private final JLabel maximumSolarPanelLabel;

	private final JCheckBox maximumWindowCheckBox;
	private final JTextField maximumWindowTextField;
	private final JLabel maximumWindowLabel;

	private final JCheckBox minimumNumberOfWallsCheckBox;
	private final JTextField minimumNumberOfWallsTextField;
	private final JLabel minimumNumberOfWallsLabel;
	private final JCheckBox maximumNumberOfWallsCheckBox;
	private final JTextField maximumNumberOfWallsTextField;
	private final JLabel maximumNumberOfWallsLabel;

	private void enableBudgetItems(boolean b) {
		budgetTextField.setEnabled(b);
		budgetLabel.setEnabled(b);
	}

	private void enableWindowItems(boolean b) {
		maximumWindowTextField.setEnabled(b);
		maximumWindowLabel.setEnabled(b);
	}

	private void enableSolarPanelItems(boolean b) {
		maximumSolarPanelTextField.setEnabled(b);
		maximumSolarPanelLabel.setEnabled(b);
	}

	private void enableWindowToFloorRatioItems(boolean b) {
		minimumWindowToFloorRatioTextField.setEnabled(b);
		maximumWindowToFloorRatioTextField.setEnabled(b);
		minimumWindowToFloorRatioLabel.setEnabled(b);
		maximumWindowToFloorRatioLabel.setEnabled(b);
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

	private void enableNumberOfWallsItems(boolean b) {
		minimumNumberOfWallsTextField.setEnabled(b);
		maximumNumberOfWallsTextField.setEnabled(b);
		minimumNumberOfWallsLabel.setEnabled(b);
		maximumNumberOfWallsLabel.setEnabled(b);
	}

	public SpecsDialog() {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Specifications");

		getContentPane().setLayout(new BorderLayout());
		final JPanel panel = new JPanel(new GridLayout(11, 3, 8, 8));
		panel.setBorder(new EmptyBorder(15, 15, 15, 15));
		getContentPane().add(panel, BorderLayout.CENTER);

		final DesignSpecs specs = Scene.getInstance().getDesignSpecs();

		// set the budget limit
		
		budgetCheckBox = new JCheckBox("Maximum Budget: ", specs.isBudgetEnabled());
		budgetCheckBox.setToolTipText("Select to apply a budget");
		budgetTextField = new JTextField(FORMAT2.format(specs.getMaximumBudget()), 6);
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
		enableBudgetItems(specs.isBudgetEnabled());

		// set the maximum number of windows allowed
		
		maximumWindowCheckBox = new JCheckBox("Maximum Number of Windows: ", specs.isWindowEnabled());
		maximumWindowCheckBox.setToolTipText("Select to apply a limit of windows");
		maximumWindowTextField = new JTextField("" + specs.getMaximumNumberOfWindows(), 6);
		maximumWindowLabel = new JLabel("");
		panel.add(maximumWindowCheckBox);
		panel.add(maximumWindowTextField);
		panel.add(maximumWindowLabel);
		maximumWindowCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				enableWindowItems(maximumWindowCheckBox.isSelected());
			}
		});
		enableWindowItems(specs.isWindowEnabled());

		// set the maximum number of solar panels allowed
		
		maximumSolarPanelCheckBox = new JCheckBox("Maximum Number of Solar Panels: ", specs.isSolarPanelEnabled());
		maximumSolarPanelCheckBox.setToolTipText("Select to apply a limit of solar panels");
		maximumSolarPanelTextField = new JTextField("" + specs.getMaximumNumberOfSolarPanels(), 6);
		maximumSolarPanelLabel = new JLabel("");
		panel.add(maximumSolarPanelCheckBox);
		panel.add(maximumSolarPanelTextField);
		panel.add(maximumSolarPanelLabel);
		maximumSolarPanelCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				enableSolarPanelItems(maximumSolarPanelCheckBox.isSelected());
			}
		});
		enableSolarPanelItems(specs.isSolarPanelEnabled());

		// set the minimum and maximum window to floor area ratio

		minimumWindowToFloorRatioCheckBox = new JCheckBox("Minimum Window/Floor Area Ratio: ", specs.isWindowToFloorRatioEnabled());
		minimumWindowToFloorRatioCheckBox.setToolTipText("Select to apply a window-to-floor area requirement");
		minimumWindowToFloorRatioTextField = new JTextField(FORMAT1.format(specs.getMinimumWindowToFloorRatio()), 6);
		minimumWindowToFloorRatioLabel = new JLabel("Dimensionless");
		panel.add(minimumWindowToFloorRatioCheckBox);
		panel.add(minimumWindowToFloorRatioTextField);
		panel.add(minimumWindowToFloorRatioLabel);

		maximumWindowToFloorRatioCheckBox = new JCheckBox("Maximum Window/Floor Area Ratio: ", specs.isWindowToFloorRatioEnabled());
		maximumWindowToFloorRatioCheckBox.setToolTipText("Select to apply a window-to-floor area requirement");
		maximumWindowToFloorRatioTextField = new JTextField(FORMAT1.format(specs.getMaximumWindowToFloorRatio()), 6);
		maximumWindowToFloorRatioLabel = new JLabel("Dimensionless");
		panel.add(maximumWindowToFloorRatioCheckBox);
		panel.add(maximumWindowToFloorRatioTextField);
		panel.add(maximumWindowToFloorRatioLabel);

		minimumWindowToFloorRatioCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean b = minimumWindowToFloorRatioCheckBox.isSelected();
				Util.selectSilently(maximumWindowToFloorRatioCheckBox, b);
				enableWindowToFloorRatioItems(b);
			}
		});
		maximumWindowToFloorRatioCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean b = maximumWindowToFloorRatioCheckBox.isSelected();
				Util.selectSilently(minimumWindowToFloorRatioCheckBox, b);
				enableWindowToFloorRatioItems(b);
			}
		});
		enableWindowToFloorRatioItems(specs.isWindowToFloorRatioEnabled());

		// set the minimum and maximum areas

		minimumAreaCheckBox = new JCheckBox("Minimum Area: ", specs.isAreaEnabled());
		minimumAreaCheckBox.setToolTipText("Select to apply an area requirement");
		minimumAreaTextField = new JTextField(FORMAT1.format(specs.getMinimumArea()), 6);
		minimumAreaLabel = new JLabel("\u33A1");
		panel.add(minimumAreaCheckBox);
		panel.add(minimumAreaTextField);
		panel.add(minimumAreaLabel);

		maximumAreaCheckBox = new JCheckBox("Maximum Area: ", specs.isAreaEnabled());
		maximumAreaCheckBox.setToolTipText(minimumAreaCheckBox.getToolTipText());
		maximumAreaTextField = new JTextField(FORMAT1.format(specs.getMaximumArea()), 6);
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
		enableAreaItems(specs.isAreaEnabled());

		// set the minimum and maximum heights

		minimumHeightCheckBox = new JCheckBox("Minimum Height: ", specs.isHeightEnabled());
		minimumHeightCheckBox.setToolTipText("Select to apply a height requirement");
		minimumHeightTextField = new JTextField(FORMAT1.format(specs.getMinimumHeight()), 6);
		minimumHeightLabel = new JLabel("m");
		panel.add(minimumHeightCheckBox);
		panel.add(minimumHeightTextField);
		panel.add(minimumHeightLabel);

		maximumHeightCheckBox = new JCheckBox("Maximum Height: ", specs.isHeightEnabled());
		maximumHeightCheckBox.setToolTipText(minimumHeightCheckBox.getToolTipText());
		maximumHeightTextField = new JTextField(FORMAT1.format(specs.getMaximumHeight()), 6);
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
		enableHeightItems(specs.isHeightEnabled());

		// set minimum and maximum numbers of walls

		minimumNumberOfWallsCheckBox = new JCheckBox("Minimum Number Of Walls: ", specs.isWallEnabled());
		minimumNumberOfWallsCheckBox.setToolTipText("Select to apply a requirement for the number of walls");
		minimumNumberOfWallsTextField = new JTextField(specs.getMinimumNumberOfWalls() + "", 6);
		minimumNumberOfWallsLabel = new JLabel("");
		panel.add(minimumNumberOfWallsCheckBox);
		panel.add(minimumNumberOfWallsTextField);
		panel.add(minimumNumberOfWallsLabel);

		maximumNumberOfWallsCheckBox = new JCheckBox("Maximum Number of Walls: ", specs.isWallEnabled());
		maximumNumberOfWallsCheckBox.setToolTipText(minimumNumberOfWallsCheckBox.getToolTipText());
		maximumNumberOfWallsTextField = new JTextField(specs.getMaximumNumberOfWalls() + "", 6);
		maximumNumberOfWallsLabel = new JLabel("");
		panel.add(maximumNumberOfWallsCheckBox);
		panel.add(maximumNumberOfWallsTextField);
		panel.add(maximumNumberOfWallsLabel);

		minimumNumberOfWallsCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean b = minimumNumberOfWallsCheckBox.isSelected();
				Util.selectSilently(maximumNumberOfWallsCheckBox, b);
				enableNumberOfWallsItems(b);
			}
		});
		maximumNumberOfWallsCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean b = maximumNumberOfWallsCheckBox.isSelected();
				Util.selectSilently(minimumNumberOfWallsCheckBox, b);
				enableNumberOfWallsItems(b);
			}
		});
		enableNumberOfWallsItems(specs.isWallEnabled());

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				int maximumBudget;
				int maximumNumberOfSolarPanels, maximumNumberOfWindows;
				int minimumNumberOfWalls, maximumNumberOfWalls;
				double minimumArea, maximumArea, minimumHeight, maximumHeight;
				double minimumWindowToFloorRatio, maximumWindowToFloorRatio;
				try {
					maximumBudget = (int) Double.parseDouble(budgetTextField.getText());
					maximumNumberOfWindows = (int) Double.parseDouble(maximumWindowTextField.getText());
					maximumNumberOfSolarPanels = (int) Double.parseDouble(maximumSolarPanelTextField.getText());
					minimumNumberOfWalls = (int) Double.parseDouble(minimumNumberOfWallsTextField.getText());
					maximumNumberOfWalls = (int) Double.parseDouble(maximumNumberOfWallsTextField.getText());
					minimumArea = Double.parseDouble(minimumAreaTextField.getText());
					maximumArea = Double.parseDouble(maximumAreaTextField.getText());
					minimumHeight = Double.parseDouble(minimumHeightTextField.getText());
					maximumHeight = Double.parseDouble(maximumHeightTextField.getText());
					minimumWindowToFloorRatio = Double.parseDouble(minimumWindowToFloorRatioTextField.getText());
					maximumWindowToFloorRatio = Double.parseDouble(maximumWindowToFloorRatioTextField.getText());
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
				if (maximumNumberOfWindows < 0) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Maximum number of windows cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (minimumNumberOfWalls < 3) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Minimum number of walls cannot be less than 3 to form a closed building.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (maximumNumberOfWalls < 3) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Maximum number of walls cannot be less than 3 to form a closed building.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (maximumNumberOfSolarPanels < 0) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Maximum number of solar panels cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (minimumWindowToFloorRatio <= 0 || maximumWindowToFloorRatio <= 0) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Minimum or maximium window-to-floor ratio must be positive.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (minimumWindowToFloorRatio >= 1 || maximumWindowToFloorRatio >= 1) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Minimum or maximum window-to-floor ratio must be less than 1.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (minimumWindowToFloorRatio >= maximumWindowToFloorRatio) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Minimum cannot be greater than maximum window-to-floor ratio.", "Range Error", JOptionPane.ERROR_MESSAGE);
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
				specs.setMaximumBudget(maximumBudget);
				specs.setMaximumNumberOfWindows(maximumNumberOfWindows);
				specs.setMaximumNumberOfSolarPanels(maximumNumberOfSolarPanels);
				specs.setMaximumArea(maximumArea);
				specs.setMinimumArea(minimumArea);
				specs.setMaximumHeight(maximumHeight);
				specs.setMinimumHeight(minimumHeight);
				specs.setMaximumNumberOfWalls(maximumNumberOfWalls);
				specs.setMinimumNumberOfWalls(minimumNumberOfWalls);
				specs.setMinimumWindowToFloorRatio(minimumWindowToFloorRatio);
				specs.setMaximumWindowToFloorRatio(maximumWindowToFloorRatio);
				specs.setBudgetEnabled(budgetCheckBox.isSelected());
				specs.setMaximumNumberOfWindowsEnabled(maximumWindowCheckBox.isSelected());
				specs.setMaximumNumberOfSolarPanelsEnabled(maximumSolarPanelCheckBox.isSelected());
				specs.setAreaEnabled(minimumAreaCheckBox.isSelected());
				specs.setHeightEnabled(minimumHeightCheckBox.isSelected());
				specs.setWallEnabled(minimumNumberOfWallsCheckBox.isSelected());
				specs.setWindowToFloorRatioEnabled(minimumWindowToFloorRatioCheckBox.isSelected());
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