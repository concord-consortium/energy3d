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

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.SpringUtilities;

/**
 * @author Charles Xie
 * 
 */
class CustomPricesDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final static DecimalFormat FORMAT = new DecimalFormat("#0.##");

	class BuildingPricesPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		final JTextField foundationField;
		final JTextField wallField;
		final JTextField roofField;
		final JTextField doorField;
		final JTextField windowField;
		final JTextField solarPanelField;

		BuildingPricesPanel() {

			super(new SpringLayout());

			add(new JLabel("Foundation: "));
			foundationField = new JTextField(6);
			add(foundationField);
			add(new JLabel("<html>$/m<sup>2</sup></html>"));

			add(new JLabel("Wall: "));
			wallField = new JTextField(6);
			add(wallField);
			add(new JLabel("<html>$/m<sup>2</sup></html>"));

			add(new JLabel("Roof: "));
			roofField = new JTextField(6);
			add(roofField);
			add(new JLabel("<html>$/m<sup>2</sup></html>"));

			add(new JLabel("Window: "));
			windowField = new JTextField(6);
			add(windowField);
			add(new JLabel("<html>$/m<sup>2</sup></html>"));

			add(new JLabel("Door: "));
			doorField = new JTextField(6);
			add(doorField);
			add(new JLabel("<html>$/m<sup>2</sup></html>"));

			add(new JLabel("Solar Panel: "));
			solarPanelField = new JTextField(FORMAT.format(Scene.getInstance().getCustomPrice().getResidentialSolarPanelPrice()), 6);
			add(solarPanelField);
			add(new JLabel("<html>$/panel</html>"));

			SpringUtilities.makeCompactGrid(this, 6, 3, 6, 6, 6, 6);

		}

	}

	class PvStationPricesPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		final JTextField solarPanelField;
		final JTextField rackField;
		final JTextField trackerField;

		PvStationPricesPanel() {

			super(new SpringLayout());

			add(new JLabel("Solar Panel: "));
			solarPanelField = new JTextField(FORMAT.format(Scene.getInstance().getCustomPrice().getCommercialSolarPanelPrice()), 6);
			add(solarPanelField);
			add(new JLabel("<html>$ per panel</html>"));

			add(new JLabel("Rack: "));
			rackField = new JTextField(6);
			add(rackField);
			add(new JLabel("<html>$ per meter of height</html>"));

			add(new JLabel("Tracker: "));
			trackerField = new JTextField(6);
			add(trackerField);
			add(new JLabel("<html>$</html>"));

			SpringUtilities.makeCompactGrid(this, 3, 3, 6, 6, 6, 6);

		}

	}

	class CspStationPricesPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		final JTextField mirrorField;
		final JTextField heliostatField;

		CspStationPricesPanel() {

			super(new SpringLayout());

			add(new JLabel("Mirror: "));
			mirrorField = new JTextField(6);
			add(mirrorField);
			add(new JLabel("<html>$/<sup>2</sup></html>"));

			add(new JLabel("Heliostat: "));
			heliostatField = new JTextField(6);
			add(heliostatField);
			add(new JLabel("<html>$ per unit</html>"));

			SpringUtilities.makeCompactGrid(this, 2, 3, 6, 6, 6, 6);

		}

	}

	public CustomPricesDialog() {

		super(MainFrame.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Custom Prices");

		final JTabbedPane tabbedPane = new JTabbedPane();
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(tabbedPane, BorderLayout.CENTER);

		final BuildingPricesPanel buildingPricesPanel = new BuildingPricesPanel();
		buildingPricesPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		JPanel p = new JPanel(new BorderLayout());
		p.add(buildingPricesPanel, BorderLayout.NORTH);
		tabbedPane.addTab("Building", p);

		final PvStationPricesPanel pvStationPricesPanel = new PvStationPricesPanel();
		pvStationPricesPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		p = new JPanel(new BorderLayout());
		p.add(pvStationPricesPanel, BorderLayout.NORTH);
		tabbedPane.addTab("PV Station", p);

		final CspStationPricesPanel cspStationPricesPanel = new CspStationPricesPanel();
		cspStationPricesPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		p = new JPanel(new BorderLayout());
		p.add(cspStationPricesPanel, BorderLayout.NORTH);
		tabbedPane.addTab("CSP Station", p);

		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				double solarPanelPrice;
				try {
					solarPanelPrice = (int) Double.parseDouble(buildingPricesPanel.solarPanelField.getText());
				} catch (final NumberFormatException err) {
					err.printStackTrace();
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Invalid input: " + err.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
					return;
				}

				// range check
				if (solarPanelPrice < 0 && solarPanelPrice > 10000) {
					JOptionPane.showMessageDialog(CustomPricesDialog.this, "Your solar panel price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
					return;
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