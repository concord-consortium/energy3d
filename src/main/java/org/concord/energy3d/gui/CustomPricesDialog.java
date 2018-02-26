package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.CspCustomPrice;
import org.concord.energy3d.simulation.PvCustomPrice;
import org.concord.energy3d.simulation.PvModuleSpecs;
import org.concord.energy3d.simulation.PvModulesData;
import org.concord.energy3d.util.SpringUtilities;

/**
 * @author Charles Xie
 * 
 */
class CustomPricesDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final static DecimalFormat FORMAT = new DecimalFormat("#0.##");
	private final static Color pvBackgroundColor = new Color(169, 223, 191);
	private final static Color cspBackgroundColor = new Color(252, 243, 207);

	class PvModulePricesPanel extends JPanel {

		private static final long serialVersionUID = 1L;
		JTextField[] priceFields;

		PvModulePricesPanel() {

			super(new SpringLayout());

			final PvCustomPrice price = Scene.getInstance().getPvCustomPrice();
			final Map<String, PvModuleSpecs> modules = PvModulesData.getInstance().getModules();
			priceFields = new JTextField[modules.size()];
			int i = 0;
			for (final String key : modules.keySet()) {
				add(createPvLabel(key + ": "));
				add(new JLabel("$"));
				priceFields[i] = new JTextField(FORMAT.format(price.getPvModelPrice(key)), 6);
				add(priceFields[i]);
				add(new JLabel(modules.get(key).getBrand()));
				i++;
			}
			SpringUtilities.makeCompactGrid(this, i, 4, 6, 6, 6, 3);

		}

	}

	class PvSystemPricesPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		final JTextField solarPanelField;
		final JTextField rackBaseField;
		final JTextField rackHeightField;
		final JTextField hsatField;
		final JTextField vsatField;
		final JTextField aadatField;
		JTextField lifespanField;
		JTextField landCostField;

		PvSystemPricesPanel() {

			super(new SpringLayout());

			final PvCustomPrice price = Scene.getInstance().getPvCustomPrice();

			add(createPvLabel("Life Span: "));
			add(new JLabel());
			lifespanField = new JTextField(FORMAT.format(price.getLifespan()), 6);
			add(lifespanField);
			add(new JLabel("<html>Years</html>"));

			add(createPvLabel("Land Cost: "));
			add(new JLabel("$"));
			landCostField = new JTextField(FORMAT.format(price.getLandUnitPrice()), 6);
			add(landCostField);
			add(new JLabel("<html>Per year per m<sup>2</sup></html>"));

			add(createPvLabel("Custom Solar Panel: "));
			add(new JLabel("$"));
			solarPanelField = new JTextField(FORMAT.format(price.getSolarPanelPrice()), 6);
			add(solarPanelField);
			add(new JLabel("<html>Per panel</html>"));

			add(createPvLabel("Rack Base (Below 1m): "));
			add(new JLabel("$"));
			rackBaseField = new JTextField(FORMAT.format(price.getSolarPanelRackBasePrice()), 6);
			add(rackBaseField);
			add(new JLabel("<html>Per panel</html>"));

			add(createPvLabel("Rack Extra Height (Beyond 1m): "));
			add(new JLabel("$"));
			rackHeightField = new JTextField(FORMAT.format(price.getSolarPanelRackHeightPrice()), 6);
			add(rackHeightField);
			add(new JLabel("<html>Per meter per panel</html>"));

			add(createPvLabel("Horizontal Single-Axis Tracker: "));
			add(new JLabel("$"));
			hsatField = new JTextField(FORMAT.format(price.getSolarPanelHsatPrice()), 6);
			add(hsatField);
			add(new JLabel("<html>Per panel</html>"));

			add(createPvLabel("Vertical Single-Axis Tracker: "));
			add(new JLabel("$"));
			vsatField = new JTextField(FORMAT.format(price.getSolarPanelVsatPrice()), 6);
			add(vsatField);
			add(new JLabel("<html>Per panel</html>"));

			add(createPvLabel("Azimuth–Altitude Dual-Axis Tracker: "));
			add(new JLabel("$"));
			aadatField = new JTextField(FORMAT.format(price.getSolarPanelAadatPrice()), 6);
			add(aadatField);
			add(new JLabel("<html>Per panel</html>"));

			SpringUtilities.makeCompactGrid(this, 8, 4, 6, 6, 6, 3);

		}

	}

	class CspStationPricesPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		final JTextField heliostatField;
		final JTextField towerField;
		final JTextField parabolicTroughField;
		final JTextField parabolicDishField;
		final JTextField fresnelReflectorField;
		JTextField lifespanField;
		JTextField landCostField;

		CspStationPricesPanel() {

			super(new SpringLayout());

			final CspCustomPrice price = Scene.getInstance().getCspCustomPrice();

			add(createCspLabel("Life Span: "));
			add(new JLabel());
			lifespanField = new JTextField(FORMAT.format(price.getLifespan()), 6);
			add(lifespanField);
			add(new JLabel("<html>Years</html>"));

			add(createCspLabel("Land Cost: "));
			add(new JLabel("$"));
			landCostField = new JTextField(FORMAT.format(price.getLandUnitPrice()), 6);
			add(landCostField);
			add(new JLabel("<html>Per year per m<sup>2</sup></html>"));

			add(createCspLabel("Heliostat: "));
			add(new JLabel("$"));
			heliostatField = new JTextField(FORMAT.format(price.getHeliostatUnitPrice()), 6);
			add(heliostatField);
			add(new JLabel("<html>Per m<sup>2</sup></html>"));

			add(createCspLabel("Tower: "));
			add(new JLabel("$"));
			towerField = new JTextField(FORMAT.format(price.getTowerUnitPrice()), 6);
			add(towerField);
			add(new JLabel("<html>Per meter height</html>"));

			add(createCspLabel("Parabolic Trough: "));
			add(new JLabel("$"));
			parabolicTroughField = new JTextField(FORMAT.format(price.getParabolicTroughUnitPrice()), 6);
			add(parabolicTroughField);
			add(new JLabel("<html>Per m<sup>2</sup></html>"));

			add(createCspLabel("Parabolic Dish: "));
			add(new JLabel("$"));
			parabolicDishField = new JTextField(FORMAT.format(price.getParabolicDishUnitPrice()), 6);
			add(parabolicDishField);
			add(new JLabel("<html>Per m<sup>2</sup></html>"));

			add(createCspLabel("Fresnel Reflector: "));
			add(new JLabel("$"));
			fresnelReflectorField = new JTextField(FORMAT.format(price.getFresnelReflectorUnitPrice()), 6);
			add(fresnelReflectorField);
			add(new JLabel("<html>Per m<sup>2</sup></html>"));

			SpringUtilities.makeCompactGrid(this, 7, 4, 6, 6, 6, 3);

		}

	}

	private JLabel createPvLabel(final String text) {
		final JLabel l = new JLabel(text);
		l.setOpaque(true);
		l.setBackground(pvBackgroundColor);
		return l;
	}

	private JLabel createCspLabel(final String text) {
		final JLabel l = new JLabel(text);
		l.setOpaque(true);
		l.setBackground(cspBackgroundColor);
		return l;
	}

	public CustomPricesDialog() {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Custom Prices");

		final JTabbedPane tabbedPane = new JTabbedPane();
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		final PvSystemPricesPanel pvSystemPricesPanel = new PvSystemPricesPanel();
		pvSystemPricesPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		final JPanel pvSystemPanel = new JPanel(new BorderLayout());
		pvSystemPanel.add(pvSystemPricesPanel, BorderLayout.NORTH);
		tabbedPane.addTab("PV System", pvSystemPanel);

		final PvModulePricesPanel pvModulePricesPanel = new PvModulePricesPanel();
		pvModulePricesPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		final JPanel pvModulePanel = new JPanel(new BorderLayout());
		final JScrollPane scrollPane = new JScrollPane(pvModulePricesPanel);
		scrollPane.setPreferredSize(new Dimension(100, 300));
		pvModulePanel.add(scrollPane, BorderLayout.NORTH);
		tabbedPane.addTab("PV Models", pvModulePanel);

		final CspStationPricesPanel cspSystemPricesPanel = new CspStationPricesPanel();
		cspSystemPricesPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		final JPanel cspSystemPanel = new JPanel(new BorderLayout());
		cspSystemPanel.add(cspSystemPricesPanel, BorderLayout.NORTH);
		tabbedPane.addTab("CSP System", cspSystemPanel);

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				int pvLifespan;
				double pvLandUnitPrice;
				double solarPanelPrice;
				double solarPanelRackBasePrice;
				double solarPanelRackHeightPrice;
				double solarPanelHsatPrice;
				double solarPanelVsatPrice;
				double solarPanelAadatPrice;

				final double[] pvModelPrices = new double[pvModulePricesPanel.priceFields.length];

				int cspLifespan;
				double cspLandUnitPrice;
				double heliostatUnitPrice;
				double towerHeightUnitPrice;
				double parabolicTroughUnitPrice;
				final double parabolicDishUnitPrice;
				double fresnelReflectorUnitPrice;
				try {
					pvLifespan = Integer.parseInt(pvSystemPricesPanel.lifespanField.getText());
					pvLandUnitPrice = Double.parseDouble(pvSystemPricesPanel.landCostField.getText());
					solarPanelPrice = Double.parseDouble(pvSystemPricesPanel.solarPanelField.getText());
					solarPanelRackBasePrice = Double.parseDouble(pvSystemPricesPanel.rackBaseField.getText());
					solarPanelRackHeightPrice = Double.parseDouble(pvSystemPricesPanel.rackHeightField.getText());
					solarPanelHsatPrice = Double.parseDouble(pvSystemPricesPanel.hsatField.getText());
					solarPanelVsatPrice = Double.parseDouble(pvSystemPricesPanel.vsatField.getText());
					solarPanelAadatPrice = Double.parseDouble(pvSystemPricesPanel.aadatField.getText());

					for (int i = 0; i < pvModelPrices.length; i++) {
						pvModelPrices[i] = Double.parseDouble(pvModulePricesPanel.priceFields[i].getText());
					}

					cspLifespan = Integer.parseInt(cspSystemPricesPanel.lifespanField.getText());
					cspLandUnitPrice = Double.parseDouble(cspSystemPricesPanel.landCostField.getText());
					heliostatUnitPrice = Double.parseDouble(cspSystemPricesPanel.heliostatField.getText());
					towerHeightUnitPrice = Double.parseDouble(cspSystemPricesPanel.towerField.getText());
					parabolicTroughUnitPrice = Double.parseDouble(cspSystemPricesPanel.parabolicTroughField.getText());
					parabolicDishUnitPrice = Double.parseDouble(cspSystemPricesPanel.parabolicDishField.getText());
					fresnelReflectorUnitPrice = Double.parseDouble(cspSystemPricesPanel.fresnelReflectorField.getText());
				} catch (final NumberFormatException err) {
					err.printStackTrace();
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Invalid input: " + err.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// PV system

				if (pvLifespan < 5 && pvLifespan > 30) {
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Your PV lifespan is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (pvLandUnitPrice < 0 && pvLandUnitPrice > 1000) {
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Your land price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (solarPanelPrice < 0 && solarPanelPrice > 10000) {
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Your solar panel price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (solarPanelRackBasePrice < 0 && solarPanelRackBasePrice > 1000) {
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Your price for solar panel rack base is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (solarPanelRackHeightPrice < 0 && solarPanelRackHeightPrice > 1000) {
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Your price for solar panel rack height is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (solarPanelHsatPrice < 0 && solarPanelHsatPrice > 10000) {
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Your HSAT price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (solarPanelVsatPrice < 0 && solarPanelVsatPrice > 10000) {
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Your VSAT price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (solarPanelAadatPrice < 0 && solarPanelAadatPrice > 10000) {
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Your AADAT price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				for (int i = 0; i < pvModelPrices.length; i++) {
					if (pvModelPrices[i] < 0 && pvModelPrices[i] > 10000) {
						JOptionPane.showMessageDialog(CustomPricesDialog.this, "Your solar panel price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				// CSP system

				if (cspLifespan < 5 && cspLifespan > 50) {
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Your CSP lifespan is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (cspLandUnitPrice < 0 && cspLandUnitPrice > 1000) {
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Your land price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (heliostatUnitPrice < 0 && heliostatUnitPrice > 10000) {
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Your mirror unit price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (towerHeightUnitPrice < 0 && towerHeightUnitPrice > 100000) {
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Your tower height unit price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (parabolicTroughUnitPrice < 0 && parabolicTroughUnitPrice > 10000) {
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Your parabolic trough unit price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (parabolicDishUnitPrice < 0 && parabolicDishUnitPrice > 10000) {
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Your parabolic trough unit price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (fresnelReflectorUnitPrice < 0 && fresnelReflectorUnitPrice > 10000) {
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Your Fresnel reflector unit price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				final PvCustomPrice pvPrice = Scene.getInstance().getPvCustomPrice();
				pvPrice.setLifespan(pvLifespan);
				pvPrice.setLandUnitPrice(pvLandUnitPrice);
				pvPrice.setSolarPanelPrice(solarPanelPrice);
				pvPrice.setSolarPanelRackBasePrice(solarPanelRackBasePrice);
				pvPrice.setSolarPanelRackHeightPrice(solarPanelRackHeightPrice);
				pvPrice.setSolarPanelHsatPrice(solarPanelHsatPrice);
				pvPrice.setSolarPanelVsatPrice(solarPanelVsatPrice);
				pvPrice.setSolarPanelAadatPrice(solarPanelAadatPrice);

				final Map<String, PvModuleSpecs> modules = PvModulesData.getInstance().getModules();
				int i = 0;
				for (final String key : modules.keySet()) {
					pvPrice.setPvModelPrice(key, pvModelPrices[i]);
					i++;
				}

				final CspCustomPrice cspPrice = Scene.getInstance().getCspCustomPrice();
				cspPrice.setLifespan(cspLifespan);
				cspPrice.setLandUnitPrice(cspLandUnitPrice);
				cspPrice.setHeliostatUnitPrice(heliostatUnitPrice);
				cspPrice.setTowerUnitPrice(towerHeightUnitPrice);
				cspPrice.setParabolicTroughUnitPrice(parabolicTroughUnitPrice);
				cspPrice.setParabolicDishUnitPrice(parabolicDishUnitPrice);
				cspPrice.setFresnelReflectorUnitPrice(fresnelReflectorUnitPrice);

				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (selectedPart != null) {
					if (selectedPart instanceof Foundation) {
						EnergyPanel.getInstance().getPvProjectInfoPanel().update((Foundation) selectedPart);
						EnergyPanel.getInstance().getCspProjectInfoPanel().update((Foundation) selectedPart);
					} else {
						final Foundation foundation = selectedPart.getTopContainer();
						if (foundation != null) {
							EnergyPanel.getInstance().getPvProjectInfoPanel().update(foundation);
							EnergyPanel.getInstance().getCspProjectInfoPanel().update(foundation);
						}
					}
				}

				CustomPricesDialog.this.dispose();

			}
		});
		okButton.setActionCommand("OK");
		buttonPanel.add(okButton);
		getRootPane().setDefaultButton(okButton);

		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				CustomPricesDialog.this.dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPanel.add(cancelButton);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(final WindowEvent e) {
				switch (Scene.getInstance().getProjectType()) {
				case Foundation.TYPE_PV_PROJECT:
					tabbedPane.setSelectedComponent(pvSystemPanel);
					break;
				case Foundation.TYPE_CSP_PROJECT:
					tabbedPane.setSelectedComponent(cspSystemPanel);
					break;
				}
			}
		});

		pack();
		setLocationRelativeTo(MainFrame.getInstance());

	}

}