package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.concord.energy3d.simulation.CustomPrice;
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

		PvStationPricesPanel() {

			super(new SpringLayout());

			final CustomPrice price = Scene.getInstance().getCustomPrice();

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

			add(new JLabel("Azimuth–Altitude Dual-Axis Tracker: "));
			aadatField = new JTextField(FORMAT.format(price.getSolarPanelAadatPrice()), 6);
			add(aadatField);
			add(new JLabel("<html>$ per panel</html>"));

			SpringUtilities.makeCompactGrid(this, 6, 3, 6, 6, 6, 6);

		}

	}

	class CspStationPricesPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		final JTextField mirrorField;
		final JTextField heliostatField;
		final JTextField towerField;

		CspStationPricesPanel() {

			super(new SpringLayout());

			final CustomPrice price = Scene.getInstance().getCustomPrice();

			add(new JLabel("Mirror: "));
			mirrorField = new JTextField(FORMAT.format(price.getMirrorUnitPrice()), 6);
			add(mirrorField);
			add(new JLabel("<html>$ per square meter</html>"));

			add(new JLabel("Heliostat: "));
			heliostatField = new JTextField(FORMAT.format(price.getHeliostatPrice()), 6);
			add(heliostatField);
			add(new JLabel("<html>$ per square meter</html>"));

			add(new JLabel("Tower: "));
			towerField = new JTextField(FORMAT.format(price.getTowerUnitPrice()), 6);
			add(towerField);
			add(new JLabel("<html>$ per meter</html>"));

			SpringUtilities.makeCompactGrid(this, 3, 3, 6, 6, 6, 6);

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
		JPanel p = new JPanel(new BorderLayout());
		p.add(pvStationPricesPanel, BorderLayout.NORTH);
		tabbedPane.addTab("PV", p);

		final CspStationPricesPanel cspStationPricesPanel = new CspStationPricesPanel();
		cspStationPricesPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		p = new JPanel(new BorderLayout());
		p.add(cspStationPricesPanel, BorderLayout.NORTH);
		tabbedPane.addTab("CSP", p);

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				double solarPanelPrice;
				double solarPanelRackBasePrice;
				double solarPanelRackHeightPrice;
				double solarPanelHsatPrice;
				double solarPanelVsatPrice;
				double solarPanelAadatPrice;
				double mirrorUnitPrice;
				double heliostatPrice;
				try {
					solarPanelPrice = (int) Double.parseDouble(pvStationPricesPanel.solarPanelField.getText());
					solarPanelRackBasePrice = (int) Double.parseDouble(pvStationPricesPanel.rackBaseField.getText());
					solarPanelRackHeightPrice = (int) Double.parseDouble(pvStationPricesPanel.rackHeightField.getText());
					solarPanelHsatPrice = (int) Double.parseDouble(pvStationPricesPanel.hsatField.getText());
					solarPanelVsatPrice = (int) Double.parseDouble(pvStationPricesPanel.vsatField.getText());
					solarPanelAadatPrice = (int) Double.parseDouble(pvStationPricesPanel.aadatField.getText());
					mirrorUnitPrice = (int) Double.parseDouble(cspStationPricesPanel.mirrorField.getText());
					heliostatPrice = (int) Double.parseDouble(cspStationPricesPanel.heliostatField.getText());
				} catch (final NumberFormatException err) {
					err.printStackTrace();
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Invalid input: " + err.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
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
				if (mirrorUnitPrice < 0 && mirrorUnitPrice > 10000) {
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Your mirror unit price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (heliostatPrice < 0 && heliostatPrice > 10000) {
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Your heliostat price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				final CustomPrice price = Scene.getInstance().getCustomPrice();
				price.setSolarPanelPrice(solarPanelPrice);
				price.setSolarPanelRackBasePrice(solarPanelRackBasePrice);
				price.setSolarPanelRackHeightPrice(solarPanelRackHeightPrice);
				price.setSolarPanelHsatPrice(solarPanelHsatPrice);
				price.setSolarPanelVsatPrice(solarPanelVsatPrice);
				price.setSolarPanelAadatPrice(solarPanelAadatPrice);
				price.setMirrorUnitPrice(mirrorUnitPrice);
				price.setHeliostatPrice(heliostatPrice);

				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (selectedPart != null) {
					if (selectedPart instanceof Foundation) {
						EnergyPanel.getInstance().getPvStationInfoPanel().update((Foundation) selectedPart);
					} else {
						final Foundation foundation = selectedPart.getTopContainer();
						if (foundation != null) {
							EnergyPanel.getInstance().getPvStationInfoPanel().update(foundation);
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

		pack();
		setLocationRelativeTo(MainFrame.getInstance());

	}

}