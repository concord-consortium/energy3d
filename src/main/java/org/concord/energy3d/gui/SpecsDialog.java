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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.CspDesignSpecs;
import org.concord.energy3d.simulation.DesignSpecs;
import org.concord.energy3d.simulation.PvDesignSpecs;

/**
 * @author Charles Xie
 * 
 */
class SpecsDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final static DecimalFormat FORMAT1 = new DecimalFormat("#0.##");
	private final static DecimalFormat FORMAT2 = new DecimalFormat("##");

	class PvSpecsPanel extends JPanel {

		private static final long serialVersionUID = 1L;
		final JCheckBox budgetCheckBox;
		final JTextField budgetField;

		final JCheckBox numberOfSolarPanelsCheckBox;
		final JTextField maximumNumberOfSolarPanelsField;

		private void enableBudgetItems(final boolean b) {
			budgetField.setEnabled(b);
		}

		private void enableSolarPanelItems(final boolean b) {
			maximumNumberOfSolarPanelsField.setEnabled(b);
		}

		PvSpecsPanel() {

			super(new BorderLayout());

			final PvDesignSpecs specs = Scene.getInstance().getPvDesignSpecs();

			final JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			add(panel, BorderLayout.NORTH);

			// set the budget limit

			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.setBorder(BorderFactory.createTitledBorder("Budget ($)"));
			panel.add(p);
			budgetCheckBox = new JCheckBox("", specs.isBudgetEnabled());
			budgetCheckBox.setToolTipText("Select to apply a budget");
			budgetField = new JTextField(FORMAT2.format(specs.getMaximumBudget()), 10);
			p.add(budgetCheckBox);
			p.add(new JLabel("<"));
			p.add(budgetField);
			budgetCheckBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					enableBudgetItems(budgetCheckBox.isSelected());
				}
			});
			enableBudgetItems(specs.isBudgetEnabled());

			// set the maximum number of solar panels allowed

			p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.setBorder(BorderFactory.createTitledBorder("Number of Solar Panels"));
			panel.add(p);
			numberOfSolarPanelsCheckBox = new JCheckBox("", specs.isNumberOfSolarPanelsEnabled());
			numberOfSolarPanelsCheckBox.setToolTipText("Select to apply a requirement of the number of solar panels");
			p.add(numberOfSolarPanelsCheckBox);
			p.add(new JLabel("<"));
			maximumNumberOfSolarPanelsField = new JTextField("" + specs.getMaximumNumberOfSolarPanels(), 10);
			p.add(maximumNumberOfSolarPanelsField);
			numberOfSolarPanelsCheckBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					enableSolarPanelItems(numberOfSolarPanelsCheckBox.isSelected());
				}
			});
			enableSolarPanelItems(specs.isNumberOfSolarPanelsEnabled());

		}

	}

	class CspSpecsPanel extends JPanel {

		private static final long serialVersionUID = 1L;
		final JCheckBox budgetCheckBox;
		final JTextField budgetField;

		final JCheckBox numberOfMirrorsCheckBox;
		final JTextField maximumNumberOfMirrorsField;

		private void enableBudgetItems(final boolean b) {
			budgetField.setEnabled(b);
		}

		private void enableMirrorItems(final boolean b) {
			maximumNumberOfMirrorsField.setEnabled(b);
		}

		CspSpecsPanel() {

			super(new BorderLayout());

			final CspDesignSpecs specs = Scene.getInstance().getCspDesignSpecs();

			final JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			add(panel, BorderLayout.NORTH);

			// set the budget limit

			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.setBorder(BorderFactory.createTitledBorder("Budget ($)"));
			panel.add(p);
			budgetCheckBox = new JCheckBox("", specs.isBudgetEnabled());
			budgetCheckBox.setToolTipText("Select to apply a budget");
			budgetField = new JTextField(FORMAT2.format(specs.getMaximumBudget()), 10);
			p.add(budgetCheckBox);
			p.add(new JLabel("<"));
			p.add(budgetField);
			budgetCheckBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					enableBudgetItems(budgetCheckBox.isSelected());
				}
			});
			enableBudgetItems(specs.isBudgetEnabled());

			// set the maximum number of mirrors allowed

			p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.setBorder(BorderFactory.createTitledBorder("Number of Mirrors"));
			panel.add(p);
			numberOfMirrorsCheckBox = new JCheckBox("", specs.isNumberOfMirrorsEnabled());
			numberOfMirrorsCheckBox.setToolTipText("Select to apply a requirement of the number of mirrors");
			p.add(numberOfMirrorsCheckBox);
			p.add(new JLabel("<"));
			maximumNumberOfMirrorsField = new JTextField("" + specs.getMaximumNumberOfMirrors(), 10);
			p.add(maximumNumberOfMirrorsField);
			numberOfMirrorsCheckBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					enableMirrorItems(numberOfMirrorsCheckBox.isSelected());
				}
			});
			enableMirrorItems(specs.isNumberOfMirrorsEnabled());

		}

	}

	class BuildingSpecsPanel extends JPanel {

		private static final long serialVersionUID = 1L;
		final JCheckBox budgetCheckBox;
		final JTextField budgetField;

		final JCheckBox areaCheckBox;
		final JTextField minimumAreaField, maximumAreaField;

		final JCheckBox heightCheckBox;
		final JTextField minimumHeightField, maximumHeightField;

		final JCheckBox windowToFloorRatioCheckBox;
		final JTextField minimumWindowToFloorRatioField, maximumWindowToFloorRatioField;

		final JCheckBox numberOfSolarPanelsCheckBox;
		final JTextField minimumNumberOfSolarPanelsField, maximumNumberOfSolarPanelsField;

		final JCheckBox numberOfWindowsCheckBox;
		final JTextField minimumNumberOfWindowsField, maximumNumberOfWindowsField;

		final JCheckBox numberOfWallsCheckBox;
		final JTextField minimumNumberOfWallsField, maximumNumberOfWallsField;

		private void enableBudgetItems(final boolean b) {
			budgetField.setEnabled(b);
		}

		private void enableWindowItems(final boolean b) {
			minimumNumberOfWindowsField.setEnabled(b);
			maximumNumberOfWindowsField.setEnabled(b);
		}

		private void enableSolarPanelItems(final boolean b) {
			minimumNumberOfSolarPanelsField.setEnabled(b);
			maximumNumberOfSolarPanelsField.setEnabled(b);
		}

		private void enableWindowToFloorRatioItems(final boolean b) {
			minimumWindowToFloorRatioField.setEnabled(b);
			maximumWindowToFloorRatioField.setEnabled(b);
		}

		private void enableAreaItems(final boolean b) {
			minimumAreaField.setEnabled(b);
			maximumAreaField.setEnabled(b);
		}

		private void enableHeightItems(final boolean b) {
			minimumHeightField.setEnabled(b);
			maximumHeightField.setEnabled(b);
		}

		private void enableNumberOfWallsItems(final boolean b) {
			minimumNumberOfWallsField.setEnabled(b);
			maximumNumberOfWallsField.setEnabled(b);
		}

		BuildingSpecsPanel() {

			super(new GridLayout(1, 2, 10, 10));

			final DesignSpecs specs = Scene.getInstance().getDesignSpecs();

			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			add(panel);

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
				public void itemStateChanged(final ItemEvent e) {
					enableBudgetItems(budgetCheckBox.isSelected());
				}
			});
			enableBudgetItems(specs.isBudgetEnabled());

			// set the maximum number of windows allowed

			p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.setBorder(BorderFactory.createTitledBorder("Number of Windows"));
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
				public void itemStateChanged(final ItemEvent e) {
					enableWindowItems(numberOfWindowsCheckBox.isSelected());
				}
			});
			enableWindowItems(specs.isNumberOfWindowsEnabled());

			// set the maximum number of solar panels allowed

			p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.setBorder(BorderFactory.createTitledBorder("Number of Solar Panels"));
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
				public void itemStateChanged(final ItemEvent e) {
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
				public void itemStateChanged(final ItemEvent e) {
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
			add(panel);

			// set the minimum and maximum window to floor area ratio

			p = new JPanel(new FlowLayout(FlowLayout.LEFT));
			p.setBorder(BorderFactory.createTitledBorder("Window-to-Floor Area Ratio"));
			panel.add(p);
			windowToFloorRatioCheckBox = new JCheckBox("", specs.isWindowToFloorRatioEnabled());
			windowToFloorRatioCheckBox.setToolTipText("Select to apply a requirement of window-to-floor area ratio");
			windowToFloorRatioCheckBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
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
				public void itemStateChanged(final ItemEvent e) {
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
				public void itemStateChanged(final ItemEvent e) {
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
			final JCheckBox cb = new JCheckBox();
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

		}

	}

	public SpecsDialog() {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Specifications");

		final JTabbedPane tabbedPane = new JTabbedPane();
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		final BuildingSpecsPanel buildingSpecsPanel = new BuildingSpecsPanel();
		buildingSpecsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		tabbedPane.addTab("Building", buildingSpecsPanel);

		final PvSpecsPanel pvSpecsPanel = new PvSpecsPanel();
		pvSpecsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		tabbedPane.addTab("PV", pvSpecsPanel);

		final CspSpecsPanel cspSpecsPanel = new CspSpecsPanel();
		cspSpecsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		tabbedPane.addTab("CSP", cspSpecsPanel);

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {

				// Building

				int maximumBudgetOfBuilding;
				int minimumNumberOfSolarPanelsOnBuilding, maximumNumberOfSolarPanelsOnBuilding;
				int minimumNumberOfWindows, maximumNumberOfWindows;
				int minimumNumberOfWalls, maximumNumberOfWalls;
				double minimumAreaOfBuilding, maximumAreaOfBuilding, minimumHeightOfBuilding, maximumHeightOfBuilding;
				double minimumWindowToFloorRatio, maximumWindowToFloorRatio;
				try {
					maximumBudgetOfBuilding = Integer.parseInt(buildingSpecsPanel.budgetField.getText());
					minimumNumberOfWindows = Integer.parseInt(buildingSpecsPanel.minimumNumberOfWindowsField.getText());
					maximumNumberOfWindows = Integer.parseInt(buildingSpecsPanel.maximumNumberOfWindowsField.getText());
					minimumNumberOfSolarPanelsOnBuilding = Integer.parseInt(buildingSpecsPanel.minimumNumberOfSolarPanelsField.getText());
					maximumNumberOfSolarPanelsOnBuilding = Integer.parseInt(buildingSpecsPanel.maximumNumberOfSolarPanelsField.getText());
					minimumNumberOfWalls = Integer.parseInt(buildingSpecsPanel.minimumNumberOfWallsField.getText());
					maximumNumberOfWalls = Integer.parseInt(buildingSpecsPanel.maximumNumberOfWallsField.getText());
					minimumAreaOfBuilding = Double.parseDouble(buildingSpecsPanel.minimumAreaField.getText());
					maximumAreaOfBuilding = Double.parseDouble(buildingSpecsPanel.maximumAreaField.getText());
					minimumHeightOfBuilding = Double.parseDouble(buildingSpecsPanel.minimumHeightField.getText());
					maximumHeightOfBuilding = Double.parseDouble(buildingSpecsPanel.maximumHeightField.getText());
					minimumWindowToFloorRatio = Double.parseDouble(buildingSpecsPanel.minimumWindowToFloorRatioField.getText());
					maximumWindowToFloorRatio = Double.parseDouble(buildingSpecsPanel.maximumWindowToFloorRatioField.getText());
				} catch (final NumberFormatException err) {
					err.printStackTrace();
					JOptionPane.showMessageDialog(SpecsDialog.this, "Invalid input: " + err.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// range check
				if (maximumBudgetOfBuilding <= 1000) {
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

				if (minimumNumberOfSolarPanelsOnBuilding < 0 || maximumNumberOfSolarPanelsOnBuilding < 0) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Number of solar panels cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (minimumNumberOfSolarPanelsOnBuilding >= maximumNumberOfSolarPanelsOnBuilding) {
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

				if (minimumAreaOfBuilding < 0 || maximumAreaOfBuilding < 0) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Area cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (minimumAreaOfBuilding >= maximumAreaOfBuilding) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Minimum area must be less than maximum area.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (minimumHeightOfBuilding < 0 || maximumHeightOfBuilding < 0) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Height cannot be negative.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (minimumHeightOfBuilding >= maximumHeightOfBuilding) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Minimum height must be less than maximum height.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				final DesignSpecs specs = Scene.getInstance().getDesignSpecs();

				specs.setBudgetEnabled(buildingSpecsPanel.budgetCheckBox.isSelected());
				specs.setMaximumBudget(maximumBudgetOfBuilding);

				specs.setNumberOfWindowsEnabled(buildingSpecsPanel.numberOfWindowsCheckBox.isSelected());
				specs.setMinimumNumberOfWindows(minimumNumberOfWindows);
				specs.setMaximumNumberOfWindows(maximumNumberOfWindows);

				specs.setNumberOfSolarPanelsEnabled(buildingSpecsPanel.numberOfSolarPanelsCheckBox.isSelected());
				specs.setMinimumNumberOfSolarPanels(minimumNumberOfSolarPanelsOnBuilding);
				specs.setMaximumNumberOfSolarPanels(maximumNumberOfSolarPanelsOnBuilding);

				specs.setNumberOfWallsEnabled(buildingSpecsPanel.numberOfWallsCheckBox.isSelected());
				specs.setMaximumNumberOfWalls(maximumNumberOfWalls);
				specs.setMinimumNumberOfWalls(minimumNumberOfWalls);

				specs.setAreaEnabled(buildingSpecsPanel.areaCheckBox.isSelected());
				specs.setMaximumArea(maximumAreaOfBuilding);
				specs.setMinimumArea(minimumAreaOfBuilding);

				specs.setHeightEnabled(buildingSpecsPanel.heightCheckBox.isSelected());
				specs.setMaximumHeight(maximumHeightOfBuilding);
				specs.setMinimumHeight(minimumHeightOfBuilding);

				specs.setWindowToFloorRatioEnabled(buildingSpecsPanel.windowToFloorRatioCheckBox.isSelected());
				specs.setMinimumWindowToFloorRatio(minimumWindowToFloorRatio);
				specs.setMaximumWindowToFloorRatio(maximumWindowToFloorRatio);

				// PV

				int maximumBudgetOfPV;
				int maximumNumberOfSolarPanelsOnPV;
				try {
					maximumBudgetOfPV = Integer.parseInt(pvSpecsPanel.budgetField.getText());
					maximumNumberOfSolarPanelsOnPV = Integer.parseInt(pvSpecsPanel.maximumNumberOfSolarPanelsField.getText());
				} catch (final NumberFormatException err) {
					err.printStackTrace();
					JOptionPane.showMessageDialog(SpecsDialog.this, "Invalid input: " + err.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// range check
				if (maximumBudgetOfPV <= 1000) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Your budget is too low to construct a PV array.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (maximumNumberOfSolarPanelsOnPV <= 0) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Number of solar panels on a PV site must be greater than zero.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				final PvDesignSpecs pvSpecs = Scene.getInstance().getPvDesignSpecs();

				pvSpecs.setBudgetEnabled(pvSpecsPanel.budgetCheckBox.isSelected());
				pvSpecs.setMaximumBudget(maximumBudgetOfPV);

				pvSpecs.setNumberOfSolarPanelsEnabled(pvSpecsPanel.numberOfSolarPanelsCheckBox.isSelected());
				pvSpecs.setMaximumNumberOfSolarPanels(maximumNumberOfSolarPanelsOnPV);

				// CSP

				int maximumBudgetOfCSP;
				int maximumNumberOfMirrors;
				try {
					maximumBudgetOfCSP = Integer.parseInt(cspSpecsPanel.budgetField.getText());
					maximumNumberOfMirrors = Integer.parseInt(cspSpecsPanel.maximumNumberOfMirrorsField.getText());
				} catch (final NumberFormatException err) {
					err.printStackTrace();
					JOptionPane.showMessageDialog(SpecsDialog.this, "Invalid input: " + err.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// range check
				if (maximumBudgetOfCSP <= 1000) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Your budget is too low to construct a CSP power plant.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (maximumNumberOfMirrors <= 0) {
					JOptionPane.showMessageDialog(SpecsDialog.this, "Number of mirrors on a CSP site must be greater than zero.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				final CspDesignSpecs cspSpecs = Scene.getInstance().getCspDesignSpecs();

				cspSpecs.setBudgetEnabled(cspSpecsPanel.budgetCheckBox.isSelected());
				cspSpecs.setMaximumBudget(maximumBudgetOfCSP);

				cspSpecs.setNumberOfMirrorsEnabled(cspSpecsPanel.numberOfMirrorsCheckBox.isSelected());
				cspSpecs.setMaximumNumberOfMirrors(maximumNumberOfMirrors);

				final HousePart part = SceneManager.getInstance().getSelectedPart();
				if (part instanceof Foundation) {
					final Foundation foundation = (Foundation) part;
					switch (foundation.getStructureType()) {
					case Foundation.TYPE_BUILDING:
						EnergyPanel.getInstance().getBuildingInfoPanel().update(foundation);
						break;
					case Foundation.TYPE_PV_STATION:
						EnergyPanel.getInstance().getPvStationInfoPanel().update(foundation);
						break;
					case Foundation.TYPE_CSP_STATION:
						EnergyPanel.getInstance().getCspStationInfoPanel().update(foundation);
						break;
					}
				}

				Scene.getInstance().setEdited(true);
				EnergyPanel.getInstance().clearRadiationHeatMap();

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