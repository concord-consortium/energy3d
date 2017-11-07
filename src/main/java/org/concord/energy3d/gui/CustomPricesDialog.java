package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.CspCustomPrice;
import org.concord.energy3d.simulation.PvCustomPrice;
import org.concord.energy3d.util.SpringUtilities;

/**
 * @author Charles Xie
 * 
 */
class CustomPricesDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final static DecimalFormat FORMAT = new DecimalFormat("#0.##");

	class PvStationPricesPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		final JTextField solarPanelField;
		final JTextField rackBaseField;
		final JTextField rackHeightField;
		final JTextField hsatField;
		final JTextField vsatField;
		final JTextField aadatField;
		JTextField lifespanField;
		JTextField landCostField;

		PvStationPricesPanel() {

			super(new SpringLayout());

			final PvCustomPrice price = Scene.getInstance().getPvCustomPrice();

			add(new JLabel("Life Span: "));
			lifespanField = new JTextField(FORMAT.format(price.getLifespan()), 6);
			add(lifespanField);
			add(new JLabel("<html>Years</html>"));

			add(new JLabel("Land Cost: "));
			landCostField = new JTextField(FORMAT.format(price.getLandUnitPrice()), 6);
			add(landCostField);
			add(new JLabel("<html>$ per year per m<sup>2</sup></html>"));

			add(new JLabel("Solar Panel: "));
			solarPanelField = new JTextField(FORMAT.format(price.getSolarPanelPrice()), 6);
			add(solarPanelField);
			add(new JLabel("<html>$ per panel</html>"));

			add(new JLabel("Rack Base (Below 1m): "));
			rackBaseField = new JTextField(FORMAT.format(price.getSolarPanelRackBasePrice()), 6);
			add(rackBaseField);
			add(new JLabel("<html>$ per panel</html>"));

			add(new JLabel("Rack Extra Height (Beyond 1m): "));
			rackHeightField = new JTextField(FORMAT.format(price.getSolarPanelRackHeightPrice()), 6);
			add(rackHeightField);
			add(new JLabel("<html>$ per meter per panel</html>"));

			add(new JLabel("Horizontal Single-Axis Tracker: "));
			hsatField = new JTextField(FORMAT.format(price.getSolarPanelHsatPrice()), 6);
			add(hsatField);
			add(new JLabel("<html>$ per panel</html>"));

			add(new JLabel("Vertical Single-Axis Tracker: "));
			vsatField = new JTextField(FORMAT.format(price.getSolarPanelVsatPrice()), 6);
			add(vsatField);
			add(new JLabel("<html>$ per panel</html>"));

			add(new JLabel("Azimuth�Altitude Dual-Axis Tracker: "));
			aadatField = new JTextField(FORMAT.format(price.getSolarPanelAadatPrice()), 6);
			add(aadatField);
			add(new JLabel("<html>$ per panel</html>"));

			SpringUtilities.makeCompactGrid(this, 8, 3, 6, 6, 6, 6);

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

			add(new JLabel("Life Span: "));
			lifespanField = new JTextField(FORMAT.format(price.getLifespan()), 6);
			add(lifespanField);
			add(new JLabel("<html>Years</html>"));

			add(new JLabel("Land Cost: "));
			landCostField = new JTextField(FORMAT.format(price.getLandUnitPrice()), 6);
			add(landCostField);
			add(new JLabel("<html>$ per year per m<sup>2</sup></html>"));

			add(new JLabel("Mirror (Heliostat): "));
			heliostatField = new JTextField(FORMAT.format(price.getHeliostatUnitPrice()), 6);
			add(heliostatField);
			add(new JLabel("<html>$ per m<sup>2</sup></html>"));

			add(new JLabel("Tower: "));
			towerField = new JTextField(FORMAT.format(price.getTowerUnitPrice()), 6);
			add(towerField);
			add(new JLabel("<html>$ per meter height</html>"));

			add(new JLabel("Parabolic Trough: "));
			parabolicTroughField = new JTextField(FORMAT.format(price.getParabolicTroughUnitPrice()), 6);
			add(parabolicTroughField);
			add(new JLabel("<html>$ per m<sup>2</sup></html>"));

			add(new JLabel("Parabolic Dish: "));
			parabolicDishField = new JTextField(FORMAT.format(price.getParabolicDishUnitPrice()), 6);
			add(parabolicDishField);
			add(new JLabel("<html>$ per m<sup>2</sup></html>"));

			add(new JLabel("Fresnel Reflector: "));
			fresnelReflectorField = new JTextField(FORMAT.format(price.getFresnelReflectorUnitPrice()), 6);
			add(fresnelReflectorField);
			add(new JLabel("<html>$ per m<sup>2</sup></html>"));

			SpringUtilities.makeCompactGrid(this, 7, 3, 6, 6, 6, 6);

		}

	}

	public CustomPricesDialog() {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Custom Prices");

		final JTabbedPane tabbedPane = new JTabbedPane();
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		final PvStationPricesPanel pvStationPricesPanel = new PvStationPricesPanel();
		pvStationPricesPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		final JPanel pvPanel = new JPanel(new BorderLayout());
		pvPanel.add(pvStationPricesPanel, BorderLayout.NORTH);
		tabbedPane.addTab("PV", pvPanel);

		final CspStationPricesPanel cspStationPricesPanel = new CspStationPricesPanel();
		cspStationPricesPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		final JPanel cspPanel = new JPanel(new BorderLayout());
		cspPanel.add(cspStationPricesPanel, BorderLayout.NORTH);
		tabbedPane.addTab("CSP", cspPanel);

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

				int cspLifespan;
				double cspLandUnitPrice;
				double heliostatUnitPrice;
				double towerHeightUnitPrice;
				double parabolicTroughUnitPrice;
				final double parabolicDishUnitPrice;
				double fresnelReflectorUnitPrice;
				try {
					pvLifespan = Integer.parseInt(pvStationPricesPanel.lifespanField.getText());
					pvLandUnitPrice = Double.parseDouble(pvStationPricesPanel.landCostField.getText());
					solarPanelPrice = Double.parseDouble(pvStationPricesPanel.solarPanelField.getText());
					solarPanelRackBasePrice = Double.parseDouble(pvStationPricesPanel.rackBaseField.getText());
					solarPanelRackHeightPrice = Double.parseDouble(pvStationPricesPanel.rackHeightField.getText());
					solarPanelHsatPrice = Double.parseDouble(pvStationPricesPanel.hsatField.getText());
					solarPanelVsatPrice = Double.parseDouble(pvStationPricesPanel.vsatField.getText());
					solarPanelAadatPrice = Double.parseDouble(pvStationPricesPanel.aadatField.getText());

					cspLifespan = Integer.parseInt(cspStationPricesPanel.lifespanField.getText());
					cspLandUnitPrice = Double.parseDouble(cspStationPricesPanel.landCostField.getText());
					heliostatUnitPrice = Double.parseDouble(cspStationPricesPanel.heliostatField.getText());
					towerHeightUnitPrice = Double.parseDouble(cspStationPricesPanel.towerField.getText());
					parabolicTroughUnitPrice = Double.parseDouble(cspStationPricesPanel.parabolicTroughField.getText());
					parabolicDishUnitPrice = Double.parseDouble(cspStationPricesPanel.parabolicDishField.getText());
					fresnelReflectorUnitPrice = Double.parseDouble(cspStationPricesPanel.fresnelReflectorField.getText());
				} catch (final NumberFormatException err) {
					err.printStackTrace();
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Invalid input: " + err.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// PV

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

				// CSP

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
						EnergyPanel.getInstance().getPvStationInfoPanel().update((Foundation) selectedPart);
						EnergyPanel.getInstance().getCspStationInfoPanel().update((Foundation) selectedPart);
					} else {
						final Foundation foundation = selectedPart.getTopContainer();
						if (foundation != null) {
							EnergyPanel.getInstance().getPvStationInfoPanel().update(foundation);
							EnergyPanel.getInstance().getCspStationInfoPanel().update(foundation);
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
				case Foundation.TYPE_PV_STATION:
					tabbedPane.setSelectedComponent(pvPanel);
					break;
				case Foundation.TYPE_CSP_STATION:
					tabbedPane.setSelectedComponent(cspPanel);
					break;
				}
			}
		});

		pack();
		setLocationRelativeTo(MainFrame.getInstance());

	}

}