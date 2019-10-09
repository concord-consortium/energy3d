package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;

import javax.swing.*;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.CspFinancialModel;
import org.concord.energy3d.simulation.PvFinancialModel;
import org.concord.energy3d.util.SpringUtilities;

/**
 * @author Charles Xie
 */
public class FinancialSettingsDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final static DecimalFormat FORMAT = new DecimalFormat("#0.##");
    private final static Color pvBackgroundColor = new Color(169, 223, 191);
    private final static Color cspBackgroundColor = new Color(252, 243, 207);

    class PvSystemFinancePanel extends JPanel {

        private static final long serialVersionUID = 1L;

        final JTextField rackBaseField;
        final JTextField rackHeightField;
        final JTextField hsatField;
        final JTextField vsatField;
        final JTextField aadatField;
        JTextField lifespanField;
        JTextField kWhSellingPriceField;
        JTextField landCostField;
        JTextField cleaningCostField;
        JTextField maintenanceCostField;
        JTextField loanInterestRateField;

        PvSystemFinancePanel() {

            super();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            final PvFinancialModel finance = Scene.getInstance().getPvFinancialModel();

            JPanel container = new JPanel(new SpringLayout());
            container.setBorder(BorderFactory.createTitledBorder("Revenue Goals"));
            add(container);

            container.add(createPvLabel("Project Lifespan: "));
            container.add(new JLabel());
            lifespanField = new JTextField(FORMAT.format(finance.getLifespan()), 6);
            container.add(lifespanField);
            container.add(new JLabel("<html>Years</html>"));

            container.add(createPvLabel("Electricity Selling Price: "));
            container.add(new JLabel("$"));
            kWhSellingPriceField = new JTextField(FORMAT.format(finance.getkWhSellingPrice()), 6);
            container.add(kWhSellingPriceField);
            container.add(new JLabel("<html>Per kWh</html>"));

            SpringUtilities.makeCompactGrid(container, 2, 4, 6, 6, 6, 3);

            container = new JPanel(new SpringLayout());
            container.setBorder(BorderFactory.createTitledBorder("Operational Costs"));
            add(container);

            container.add(createPvLabel("Land Rental: "));
            container.add(new JLabel("$"));
            landCostField = new JTextField(FORMAT.format(finance.getLandRentalCost()), 6);
            container.add(landCostField);
            container.add(new JLabel("<html>Per year per m<sup>2</sup></html>"));

            container.add(createPvLabel("Cleaning Service: "));
            container.add(new JLabel("$"));
            cleaningCostField = new JTextField(FORMAT.format(finance.getCleaningCost()), 6);
            container.add(cleaningCostField);
            container.add(new JLabel("<html>Per year per panel</html>"));

            container.add(createPvLabel("Maintenance: "));
            container.add(new JLabel("$"));
            maintenanceCostField = new JTextField(FORMAT.format(finance.getMaintenanceCost()), 6);
            container.add(maintenanceCostField);
            container.add(new JLabel("<html>Per year per panel</html>"));

            container.add(createPvLabel("Loan Interest Rate: "));
            container.add(new JLabel("%"));
            loanInterestRateField = new JTextField(FORMAT.format(finance.getLoanInterestRate() * 100), 6);
            container.add(loanInterestRateField);
            container.add(new JLabel("<html>For upfront costs</html>"));

            SpringUtilities.makeCompactGrid(container, 4, 4, 6, 6, 6, 3);

            container = new JPanel(new SpringLayout());
            container.setBorder(BorderFactory.createTitledBorder("Upfront Costs (Labor Included)"));
            add(container);

            container.add(createPvLabel("Photovoltaic Solar Panel: "));
            container.add(new JLabel("$"));
            final JButton solarPanelMarketplace = new JButton("Set Price");
            solarPanelMarketplace.addActionListener(e -> {
                PvModelsDialog dialog = new PvModelsDialog();
                dialog.setVisible(true);
            });
            container.add(solarPanelMarketplace);
            container.add(new JLabel("<html>Per panel</html>"));

            container.add(createPvLabel("Rack Base (Below 1m): "));
            container.add(new JLabel("$"));
            rackBaseField = new JTextField(FORMAT.format(finance.getSolarPanelRackBaseCost()), 6);
            container.add(rackBaseField);
            container.add(new JLabel("<html>Per panel</html>"));

            container.add(createPvLabel("Rack Extra Height (Beyond 1m): "));
            container.add(new JLabel("$"));
            rackHeightField = new JTextField(FORMAT.format(finance.getSolarPanelRackHeightCost()), 6);
            container.add(rackHeightField);
            container.add(new JLabel("<html>Per meter per panel</html>"));

            container.add(createPvLabel("Horizontal Single-Axis Tracker: "));
            container.add(new JLabel("$"));
            hsatField = new JTextField(FORMAT.format(finance.getSolarPanelHsatCost()), 6);
            container.add(hsatField);
            container.add(new JLabel("<html>Per panel, if used</html>"));

            container.add(createPvLabel("Vertical Single-Axis Tracker: "));
            container.add(new JLabel("$"));
            vsatField = new JTextField(FORMAT.format(finance.getSolarPanelVsatCost()), 6);
            container.add(vsatField);
            container.add(new JLabel("<html>Per panel, if used</html>"));

            container.add(createPvLabel("Azimuth–Altitude Dual-Axis Tracker: "));
            container.add(new JLabel("$"));
            aadatField = new JTextField(FORMAT.format(finance.getSolarPanelAadatCost()), 6);
            container.add(aadatField);
            container.add(new JLabel("<html>Per panel, if used</html>"));

            SpringUtilities.makeCompactGrid(container, 6, 4, 6, 6, 6, 3);

        }

    }

    class CspSystemFinancePanel extends JPanel {

        private static final long serialVersionUID = 1L;

        private JTextField heliostatField;
        private JTextField towerField;
        private JTextField parabolicTroughField;
        final JTextField parabolicDishField;
        final JTextField fresnelReflectorField;
        JTextField lifespanField;
        JTextField landCostField;
        JTextField kWhSellingPriceField;
        JTextField cleaningCostField;
        JTextField maintenanceCostField;
        JTextField loanInterestRateField;

        CspSystemFinancePanel() {

            super();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            final CspFinancialModel finance = Scene.getInstance().getCspFinancialModel();

            JPanel container = new JPanel(new SpringLayout());
            container.setBorder(BorderFactory.createTitledBorder("Revenue Goals"));
            add(container);

            container.add(createCspLabel("Project Lifespan: "));
            container.add(new JLabel());
            lifespanField = new JTextField(FORMAT.format(finance.getLifespan()), 6);
            container.add(lifespanField);
            container.add(new JLabel("<html>Years</html>"));

            container.add(createCspLabel("Electricity Selling Price: "));
            container.add(new JLabel("$"));
            kWhSellingPriceField = new JTextField(FORMAT.format(finance.getkWhSellingPrice()), 6);
            container.add(kWhSellingPriceField);
            container.add(new JLabel("<html>Per kWh</html>"));

            SpringUtilities.makeCompactGrid(container, 2, 4, 6, 6, 6, 3);

            container = new JPanel(new SpringLayout());
            container.setBorder(BorderFactory.createTitledBorder("Operational Costs"));
            add(container);

            container.add(createCspLabel("Land Rental: "));
            container.add(new JLabel("$"));
            landCostField = new JTextField(FORMAT.format(finance.getLandRentalCost()), 6);
            container.add(landCostField);
            container.add(new JLabel("<html>Per year per m<sup>2</sup></html>"));

            container.add(createCspLabel("Cleaning Cost: "));
            container.add(new JLabel("$"));
            cleaningCostField = new JTextField(FORMAT.format(finance.getCleaningCost()), 6);
            container.add(cleaningCostField);
            container.add(new JLabel("<html>Per year per unit</html>"));

            container.add(createCspLabel("Maintenance Cost: "));
            container.add(new JLabel("$"));
            maintenanceCostField = new JTextField(FORMAT.format(finance.getMaintenanceCost()), 6);
            container.add(maintenanceCostField);
            container.add(new JLabel("<html>Per year per unit</html>"));

            container.add(createCspLabel("Loan Interest Rate: "));
            container.add(new JLabel("%"));
            loanInterestRateField = new JTextField(FORMAT.format(finance.getLoanInterestRate() * 100), 6);
            container.add(loanInterestRateField);
            container.add(new JLabel("<html>For upfront costs</html>"));

            SpringUtilities.makeCompactGrid(container, 4, 4, 6, 6, 6, 3);

            container = new JPanel(new SpringLayout());
            container.setBorder(BorderFactory.createTitledBorder("Upfront Costs (Labor Included)"));
            add(container);

            container.add(createCspLabel("Heliostat Cost: "));
            container.add(new JLabel("$"));
            heliostatField = new JTextField(FORMAT.format(finance.getHeliostatUnitCost()), 6);
            container.add(heliostatField);
            container.add(new JLabel("<html>Per m<sup>2</sup></html>"));

            container.add(createCspLabel("Fresnel Reflector Cost: "));
            container.add(new JLabel("$"));
            fresnelReflectorField = new JTextField(FORMAT.format(finance.getFresnelReflectorUnitCost()), 6);
            container.add(fresnelReflectorField);
            container.add(new JLabel("<html>Per m<sup>2</sup></html>"));

            container.add(createCspLabel("Receiver Cost: "));
            container.add(new JLabel("$"));
            towerField = new JTextField(FORMAT.format(finance.getReceiverUnitCost()), 6);
            container.add(towerField);
            container.add(new JLabel("<html>Per meter in height</html>"));

            container.add(createCspLabel("Parabolic Trough Cost: "));
            container.add(new JLabel("$"));
            parabolicTroughField = new JTextField(FORMAT.format(finance.getParabolicTroughUnitCost()), 6);
            container.add(parabolicTroughField);
            container.add(new JLabel("<html>Per m<sup>2</sup></html>"));

            container.add(createCspLabel("Parabolic Dish: "));
            container.add(new JLabel("$"));
            parabolicDishField = new JTextField(FORMAT.format(finance.getParabolicDishUnitCost()), 6);
            container.add(parabolicDishField);
            container.add(new JLabel("<html>Per m<sup>2</sup></html>"));

            SpringUtilities.makeCompactGrid(container, 5, 4, 6, 6, 6, 3);

        }

    }

    static JLabel createPvLabel(final String text) {
        final JLabel l = new JLabel(text);
        l.setOpaque(true);
        l.setBackground(pvBackgroundColor);
        l.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 2));
        return l;
    }

    static JLabel createCspLabel(final String text) {
        final JLabel l = new JLabel(text);
        l.setOpaque(true);
        l.setBackground(cspBackgroundColor);
        l.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 2));
        return l;
    }

    private JTabbedPane tabbedPane;
    private JPanel pvSystemPanel;
    private JPanel cspSystemPanel;

    public FinancialSettingsDialog() {

        super(MainFrame.getInstance(), true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle("Cost & Revenue Settings");

        tabbedPane = new JTabbedPane();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        final PvSystemFinancePanel pvSystemFinancePanel = new PvSystemFinancePanel();
        pvSystemFinancePanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pvSystemPanel = new JPanel(new BorderLayout());
        pvSystemPanel.add(pvSystemFinancePanel, BorderLayout.NORTH);
        tabbedPane.addTab("PV System", pvSystemPanel);

        final CspSystemFinancePanel cspSystemFinancePanel = new CspSystemFinancePanel();
        cspSystemFinancePanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        cspSystemPanel = new JPanel(new BorderLayout());
        cspSystemPanel.add(cspSystemFinancePanel, BorderLayout.NORTH);
        tabbedPane.addTab("CSP System", cspSystemPanel);

        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        final JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            int pvLifespan;
            double pvKWhSellPrice;
            double pvLandUnitCost;
            double pvCleaningCost;
            double pvMaintenanceCost;
            double pvLoanInterestCost;
            double solarPanelRackBaseCost;
            double solarPanelRackHeightCost;
            double solarPanelHsatCost;
            double solarPanelVsatCost;
            double solarPanelAadatCost;

            int cspLifespan;
            double cspKWhSellPrice;
            double cspLandUnitCost;
            double cspCleaningCost;
            double cspMaintenanceCost;
            double cspLoanInterestCost;
            double heliostatUnitCost;
            double towerHeightUnitCost;
            double parabolicTroughUnitCost;
            final double parabolicDishUnitCost;
            double fresnelReflectorUnitCost;
            try {
                pvLifespan = Integer.parseInt(pvSystemFinancePanel.lifespanField.getText());
                pvKWhSellPrice = Double.parseDouble(pvSystemFinancePanel.kWhSellingPriceField.getText());
                pvLandUnitCost = Double.parseDouble(pvSystemFinancePanel.landCostField.getText());
                pvCleaningCost = Double.parseDouble(pvSystemFinancePanel.cleaningCostField.getText());
                pvMaintenanceCost = Double.parseDouble(pvSystemFinancePanel.maintenanceCostField.getText());
                pvLoanInterestCost = Double.parseDouble(pvSystemFinancePanel.loanInterestRateField.getText());
                solarPanelRackBaseCost = Double.parseDouble(pvSystemFinancePanel.rackBaseField.getText());
                solarPanelRackHeightCost = Double.parseDouble(pvSystemFinancePanel.rackHeightField.getText());
                solarPanelHsatCost = Double.parseDouble(pvSystemFinancePanel.hsatField.getText());
                solarPanelVsatCost = Double.parseDouble(pvSystemFinancePanel.vsatField.getText());
                solarPanelAadatCost = Double.parseDouble(pvSystemFinancePanel.aadatField.getText());

                cspLifespan = Integer.parseInt(cspSystemFinancePanel.lifespanField.getText());
                cspKWhSellPrice = Double.parseDouble(cspSystemFinancePanel.kWhSellingPriceField.getText());
                cspLandUnitCost = Double.parseDouble(cspSystemFinancePanel.landCostField.getText());
                cspCleaningCost = Double.parseDouble(cspSystemFinancePanel.cleaningCostField.getText());
                cspMaintenanceCost = Double.parseDouble(cspSystemFinancePanel.maintenanceCostField.getText());
                cspLoanInterestCost = Double.parseDouble(cspSystemFinancePanel.loanInterestRateField.getText());
                heliostatUnitCost = Double.parseDouble(cspSystemFinancePanel.heliostatField.getText());
                towerHeightUnitCost = Double.parseDouble(cspSystemFinancePanel.towerField.getText());
                parabolicTroughUnitCost = Double.parseDouble(cspSystemFinancePanel.parabolicTroughField.getText());
                parabolicDishUnitCost = Double.parseDouble(cspSystemFinancePanel.parabolicDishField.getText());
                fresnelReflectorUnitCost = Double.parseDouble(cspSystemFinancePanel.fresnelReflectorField.getText());
            } catch (final NumberFormatException err) {
                err.printStackTrace();
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Invalid input: " + err.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // PV system

            if (pvLifespan < 10 || pvLifespan > 30) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your PV project lifespan is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (pvKWhSellPrice <= 0 || pvKWhSellPrice > 1) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your sell price per kWh is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (pvLandUnitCost < 0 || pvLandUnitCost > 1000) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your land price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (pvCleaningCost < 0 || pvCleaningCost > 100) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your cleaning price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (pvMaintenanceCost < 0 || pvMaintenanceCost > 100) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your maintenance price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (pvLoanInterestCost < 0 || pvLoanInterestCost > 100) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your loan interest is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (solarPanelRackBaseCost < 0 || solarPanelRackBaseCost > 1000) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your price for solar panel rack base is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (solarPanelRackHeightCost < 0 || solarPanelRackHeightCost > 1000) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your price for solar panel rack height is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (solarPanelHsatCost < 0 || solarPanelHsatCost > 10000) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your HSAT price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (solarPanelVsatCost < 0 || solarPanelVsatCost > 10000) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your VSAT price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (solarPanelAadatCost < 0 || solarPanelAadatCost > 10000) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your AADAT price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // CSP system

            if (cspLifespan < 20 || cspLifespan > 50) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your CSP project lifespan is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (cspKWhSellPrice <= 0 || cspKWhSellPrice > 1) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your sell price per kWh is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (cspLandUnitCost < 0 || cspLandUnitCost > 1000) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your land price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (cspCleaningCost < 0 || cspCleaningCost > 100) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your cleaning price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (cspMaintenanceCost < 0 || cspMaintenanceCost > 100) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your maintenance price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (cspLoanInterestCost < 0 || cspLoanInterestCost > 100) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your loan interest is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (heliostatUnitCost < 0 || heliostatUnitCost > 10000) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your mirror unit price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (towerHeightUnitCost < 0 || towerHeightUnitCost > 100000) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your tower height unit price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (parabolicTroughUnitCost < 0 || parabolicTroughUnitCost > 10000) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your parabolic trough unit price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (parabolicDishUnitCost < 0 || parabolicDishUnitCost > 10000) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your parabolic trough unit price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (fresnelReflectorUnitCost < 0 || fresnelReflectorUnitCost > 10000) {
                JOptionPane.showMessageDialog(FinancialSettingsDialog.this, "Your Fresnel reflector unit price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            final PvFinancialModel pvFinance = Scene.getInstance().getPvFinancialModel();
            pvFinance.setLifespan(pvLifespan);
            pvFinance.setkWhSellingPrice(pvKWhSellPrice);
            pvFinance.setLandRentalCost(pvLandUnitCost);
            pvFinance.setCleaningCost(pvCleaningCost);
            pvFinance.setMaintenanceCost(pvMaintenanceCost);
            pvFinance.setLoanInterestRate(pvLoanInterestCost * 0.01);
            pvFinance.setSolarPanelRackBaseCost(solarPanelRackBaseCost);
            pvFinance.setSolarPanelRackHeightCost(solarPanelRackHeightCost);
            pvFinance.setSolarPanelHsatCost(solarPanelHsatCost);
            pvFinance.setSolarPanelVsatCost(solarPanelVsatCost);
            pvFinance.setSolarPanelAadatCost(solarPanelAadatCost);

            final CspFinancialModel cspFinance = Scene.getInstance().getCspFinancialModel();
            cspFinance.setLifespan(cspLifespan);
            cspFinance.setkWhSellingPrice(cspKWhSellPrice);
            cspFinance.setLandRentalCost(cspLandUnitCost);
            cspFinance.setCleaningCost(cspCleaningCost);
            cspFinance.setMaintenanceCost(cspMaintenanceCost);
            cspFinance.setLoanInterestRate(cspLoanInterestCost * 0.01);
            cspFinance.setHeliostatUnitCost(heliostatUnitCost);
            cspFinance.setReceiverUnitCost(towerHeightUnitCost);
            cspFinance.setParabolicTroughUnitCost(parabolicTroughUnitCost);
            cspFinance.setParabolicDishUnitCost(parabolicDishUnitCost);
            cspFinance.setFresnelReflectorUnitCost(fresnelReflectorUnitCost);

            final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
            if (selectedPart != null) {
                if (selectedPart instanceof Foundation) {
                    EnergyPanel.getInstance().getPvProjectZoneInfoPanel().update((Foundation) selectedPart);
                    EnergyPanel.getInstance().getCspProjectZoneInfoPanel().update((Foundation) selectedPart);
                } else {
                    final Foundation foundation = selectedPart.getTopContainer();
                    if (foundation != null) {
                        EnergyPanel.getInstance().getPvProjectZoneInfoPanel().update(foundation);
                        EnergyPanel.getInstance().getCspProjectZoneInfoPanel().update(foundation);
                    }
                }
            }

            FinancialSettingsDialog.this.dispose();

        });
        okButton.setActionCommand("OK");
        buttonPanel.add(okButton);
        getRootPane().setDefaultButton(okButton);

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> FinancialSettingsDialog.this.dispose());
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

    public void selectPvPrices() {
        tabbedPane.setSelectedComponent(pvSystemPanel);
    }

    public void selectCspPrices() {
        tabbedPane.setSelectedComponent(cspSystemPanel);
    }

}