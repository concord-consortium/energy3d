package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.Map;

import javax.swing.*;

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
 */
class FinanceDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final static DecimalFormat FORMAT = new DecimalFormat("#0.##");
    private final static Color pvBackgroundColor = new Color(169, 223, 191);
    private final static Color cspBackgroundColor = new Color(252, 243, 207);

    class PvModuleCostsPanel extends JPanel {

        private static final long serialVersionUID = 1L;
        JTextField[] priceFields;

        PvModuleCostsPanel() {

            super(new SpringLayout());

            final PvCustomPrice price = Scene.getInstance().getPvCustomPrice();
            final Map<String, PvModuleSpecs> modules = PvModulesData.getInstance().getModules();
            priceFields = new JTextField[modules.size()];
            int i = 0;
            for (final String key : modules.keySet()) {
                add(createPvLabel(key + ": "));
                add(new JLabel("$"));
                priceFields[i] = new JTextField(FORMAT.format(price.getPvModelCost(key)), 6);
                add(priceFields[i]);
                add(new JLabel(modules.get(key).getBrand()));
                i++;
            }
            SpringUtilities.makeCompactGrid(this, i, 4, 6, 6, 6, 3);

        }

    }

    class PvSystemFinancePanel extends JPanel {

        private static final long serialVersionUID = 1L;

        final JTextField solarPanelField;
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

        PvSystemFinancePanel() {

            super();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            final PvCustomPrice finance = Scene.getInstance().getPvCustomPrice();

            JPanel container = new JPanel(new SpringLayout());
            container.setBorder(BorderFactory.createTitledBorder("Target Goals"));
            add(container);

            container.add(createPvLabel("Life Span: "));
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

            SpringUtilities.makeCompactGrid(container, 3, 4, 6, 6, 6, 3);

            container = new JPanel(new SpringLayout());
            container.setBorder(BorderFactory.createTitledBorder("Upfront Costs"));
            add(container);

            container.add(createPvLabel("Custom Solar Panel: "));
            container.add(new JLabel("$"));
            solarPanelField = new JTextField(FORMAT.format(finance.getSolarPanelCost()), 6);
            container.add(solarPanelField);
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
            container.add(new JLabel("<html>Per panel</html>"));

            container.add(createPvLabel("Vertical Single-Axis Tracker: "));
            container.add(new JLabel("$"));
            vsatField = new JTextField(FORMAT.format(finance.getSolarPanelVsatCost()), 6);
            container.add(vsatField);
            container.add(new JLabel("<html>Per panel</html>"));

            container.add(createPvLabel("Azimuth–Altitude Dual-Axis Tracker: "));
            container.add(new JLabel("$"));
            aadatField = new JTextField(FORMAT.format(finance.getSolarPanelAadatCost()), 6);
            container.add(aadatField);
            container.add(new JLabel("<html>Per panel</html>"));

            SpringUtilities.makeCompactGrid(container, 6, 4, 6, 6, 6, 3);

        }

    }

    class CspSystemFinancePanel extends JPanel {

        private static final long serialVersionUID = 1L;

        final JTextField heliostatField;
        final JTextField towerField;
        final JTextField parabolicTroughField;
        final JTextField parabolicDishField;
        final JTextField fresnelReflectorField;
        JTextField lifespanField;
        JTextField landCostField;
        JTextField kWhSellingPriceField;
        JTextField cleaningCostField;
        JTextField maintenanceCostField;

        CspSystemFinancePanel() {

            super();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            final CspCustomPrice price = Scene.getInstance().getCspCustomPrice();

            JPanel container = new JPanel(new SpringLayout());
            container.setBorder(BorderFactory.createTitledBorder("Target Goals"));
            add(container);

            container.add(createCspLabel("Life Span: "));
            container.add(new JLabel());
            lifespanField = new JTextField(FORMAT.format(price.getLifespan()), 6);
            container.add(lifespanField);
            container.add(new JLabel("<html>Years</html>"));

            container.add(createCspLabel("Electricity Selling Price: "));
            container.add(new JLabel("$"));
            kWhSellingPriceField = new JTextField(FORMAT.format(price.getkWhSellingPrice()), 6);
            container.add(kWhSellingPriceField);
            container.add(new JLabel("<html>Per kWh</html>"));

            SpringUtilities.makeCompactGrid(container, 2, 4, 6, 6, 6, 3);

            container = new JPanel(new SpringLayout());
            container.setBorder(BorderFactory.createTitledBorder("Operational Costs"));
            add(container);

            container.add(createCspLabel("Land Rental: "));
            container.add(new JLabel("$"));
            landCostField = new JTextField(FORMAT.format(price.getLandRentalCost()), 6);
            container.add(landCostField);
            container.add(new JLabel("<html>Per year per m<sup>2</sup></html>"));

            container.add(createCspLabel("Cleaning Cost: "));
            container.add(new JLabel("$"));
            cleaningCostField = new JTextField(FORMAT.format(price.getCleaningCost()), 6);
            container.add(cleaningCostField);
            container.add(new JLabel("<html>Per year per unit</html>"));

            container.add(createCspLabel("Maintenance Cost: "));
            container.add(new JLabel("$"));
            maintenanceCostField = new JTextField(FORMAT.format(price.getMaintenanceCost()), 6);
            container.add(maintenanceCostField);
            container.add(new JLabel("<html>Per year per unit</html>"));

            SpringUtilities.makeCompactGrid(container, 3, 4, 6, 6, 6, 3);

            container = new JPanel(new SpringLayout());
            container.setBorder(BorderFactory.createTitledBorder("Upfront Costs"));
            add(container);

            container.add(createCspLabel("Heliostat Cost: "));
            container.add(new JLabel("$"));
            heliostatField = new JTextField(FORMAT.format(price.getHeliostatUnitCost()), 6);
            container.add(heliostatField);
            container.add(new JLabel("<html>Per m<sup>2</sup></html>"));

            container.add(createCspLabel("Tower: "));
            container.add(new JLabel("$"));
            towerField = new JTextField(FORMAT.format(price.getTowerUnitCost()), 6);
            container.add(towerField);
            container.add(new JLabel("<html>Per meter height</html>"));

            container.add(createCspLabel("Parabolic Trough Cost: "));
            container.add(new JLabel("$"));
            parabolicTroughField = new JTextField(FORMAT.format(price.getParabolicTroughUnitCost()), 6);
            container.add(parabolicTroughField);
            container.add(new JLabel("<html>Per m<sup>2</sup></html>"));

            container.add(createCspLabel("Parabolic Dish: "));
            container.add(new JLabel("$"));
            parabolicDishField = new JTextField(FORMAT.format(price.getParabolicDishUnitCost()), 6);
            container.add(parabolicDishField);
            container.add(new JLabel("<html>Per m<sup>2</sup></html>"));

            container.add(createCspLabel("Fresnel Reflector Cost: "));
            container.add(new JLabel("$"));
            fresnelReflectorField = new JTextField(FORMAT.format(price.getFresnelReflectorUnitCost()), 6);
            container.add(fresnelReflectorField);
            container.add(new JLabel("<html>Per m<sup>2</sup></html>"));

            SpringUtilities.makeCompactGrid(container, 5, 4, 6, 6, 6, 3);

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

    private JTabbedPane tabbedPane;
    private JPanel pvSystemPanel;
    private JPanel cspSystemPanel;

    FinanceDialog() {

        super(MainFrame.getInstance(), true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setTitle("Financing the Project");

        tabbedPane = new JTabbedPane();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        final PvSystemFinancePanel pvSystemFinancePanel = new PvSystemFinancePanel();
        pvSystemFinancePanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        pvSystemPanel = new JPanel(new BorderLayout());
        pvSystemPanel.add(pvSystemFinancePanel, BorderLayout.NORTH);
        tabbedPane.addTab("PV System", pvSystemPanel);

        final PvModuleCostsPanel pvModuleCostsPanel = new PvModuleCostsPanel();
        pvModuleCostsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        final JPanel pvModulePanel = new JPanel(new BorderLayout());
        final JScrollPane scrollPane = new JScrollPane(pvModuleCostsPanel);
        scrollPane.setPreferredSize(new Dimension(100, 300));
        pvModulePanel.add(scrollPane, BorderLayout.NORTH);
        tabbedPane.addTab("PV Models", pvModulePanel);

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
            double solarPanelCost;
            double solarPanelRackBaseCost;
            double solarPanelRackHeightCost;
            double solarPanelHsatCost;
            double solarPanelVsatCost;
            double solarPanelAadatCost;

            final double[] pvModelCosts = new double[pvModuleCostsPanel.priceFields.length];

            int cspLifespan;
            double cspKWhSellPrice;
            double cspLandUnitCost;
            double cspCleaningCost;
            double cspMaintenanceCost;
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
                solarPanelCost = Double.parseDouble(pvSystemFinancePanel.solarPanelField.getText());
                solarPanelRackBaseCost = Double.parseDouble(pvSystemFinancePanel.rackBaseField.getText());
                solarPanelRackHeightCost = Double.parseDouble(pvSystemFinancePanel.rackHeightField.getText());
                solarPanelHsatCost = Double.parseDouble(pvSystemFinancePanel.hsatField.getText());
                solarPanelVsatCost = Double.parseDouble(pvSystemFinancePanel.vsatField.getText());
                solarPanelAadatCost = Double.parseDouble(pvSystemFinancePanel.aadatField.getText());

                for (int i = 0; i < pvModelCosts.length; i++) {
                    pvModelCosts[i] = Double.parseDouble(pvModuleCostsPanel.priceFields[i].getText());
                }

                cspLifespan = Integer.parseInt(cspSystemFinancePanel.lifespanField.getText());
                cspKWhSellPrice = Double.parseDouble(cspSystemFinancePanel.kWhSellingPriceField.getText());
                cspLandUnitCost = Double.parseDouble(cspSystemFinancePanel.landCostField.getText());
                cspCleaningCost = Double.parseDouble(cspSystemFinancePanel.cleaningCostField.getText());
                cspMaintenanceCost = Double.parseDouble(cspSystemFinancePanel.maintenanceCostField.getText());
                heliostatUnitCost = Double.parseDouble(cspSystemFinancePanel.heliostatField.getText());
                towerHeightUnitCost = Double.parseDouble(cspSystemFinancePanel.towerField.getText());
                parabolicTroughUnitCost = Double.parseDouble(cspSystemFinancePanel.parabolicTroughField.getText());
                parabolicDishUnitCost = Double.parseDouble(cspSystemFinancePanel.parabolicDishField.getText());
                fresnelReflectorUnitCost = Double.parseDouble(cspSystemFinancePanel.fresnelReflectorField.getText());
            } catch (final NumberFormatException err) {
                err.printStackTrace();
                JOptionPane.showMessageDialog(FinanceDialog.this, "Invalid input: " + err.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // PV system

            if (pvLifespan < 5 || pvLifespan > 30) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your PV lifespan is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (pvKWhSellPrice <= 0 || pvKWhSellPrice > 1) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your sell price per kWh is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (pvLandUnitCost < 0 || pvLandUnitCost > 1000) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your land price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (pvCleaningCost < 0 || pvCleaningCost > 100) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your cleaning price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (pvMaintenanceCost < 0 || pvMaintenanceCost > 100) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your maintenance price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (solarPanelCost < 0 || solarPanelCost > 10000) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your solar panel price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (solarPanelRackBaseCost < 0 || solarPanelRackBaseCost > 1000) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your price for solar panel rack base is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (solarPanelRackHeightCost < 0 || solarPanelRackHeightCost > 1000) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your price for solar panel rack height is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (solarPanelHsatCost < 0 || solarPanelHsatCost > 10000) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your HSAT price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (solarPanelVsatCost < 0 || solarPanelVsatCost > 10000) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your VSAT price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (solarPanelAadatCost < 0 || solarPanelAadatCost > 10000) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your AADAT price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            for (double pvModelPrice : pvModelCosts) {
                if (pvModelPrice < 0 || pvModelPrice > 10000) {
                    JOptionPane.showMessageDialog(FinanceDialog.this, "Your solar panel price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // CSP system

            if (cspLifespan < 5 || cspLifespan > 50) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your CSP lifespan is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (cspKWhSellPrice <= 0 || cspKWhSellPrice > 1) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your sell price per kWh is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (cspLandUnitCost < 0 || cspLandUnitCost > 1000) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your land price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (cspCleaningCost < 0 || cspCleaningCost > 100) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your cleaning price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (cspMaintenanceCost < 0 || cspMaintenanceCost > 100) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your maintenance price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (heliostatUnitCost < 0 || heliostatUnitCost > 10000) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your mirror unit price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (towerHeightUnitCost < 0 || towerHeightUnitCost > 100000) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your tower height unit price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (parabolicTroughUnitCost < 0 || parabolicTroughUnitCost > 10000) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your parabolic trough unit price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (parabolicDishUnitCost < 0 || parabolicDishUnitCost > 10000) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your parabolic trough unit price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (fresnelReflectorUnitCost < 0 || fresnelReflectorUnitCost > 10000) {
                JOptionPane.showMessageDialog(FinanceDialog.this, "Your Fresnel reflector unit price is out of range.", "Range Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            final PvCustomPrice pvFinance = Scene.getInstance().getPvCustomPrice();
            pvFinance.setLifespan(pvLifespan);
            pvFinance.setkWhSellingPrice(pvKWhSellPrice);
            pvFinance.setLandRentalCost(pvLandUnitCost);
            pvFinance.setCleaningCost(pvCleaningCost);
            pvFinance.setMaintenanceCost(pvMaintenanceCost);
            pvFinance.setSolarPanelCost(solarPanelCost);
            pvFinance.setSolarPanelRackBaseCost(solarPanelRackBaseCost);
            pvFinance.setSolarPanelRackHeightCost(solarPanelRackHeightCost);
            pvFinance.setSolarPanelHsatCost(solarPanelHsatCost);
            pvFinance.setSolarPanelVsatCost(solarPanelVsatCost);
            pvFinance.setSolarPanelAadatCost(solarPanelAadatCost);

            final Map<String, PvModuleSpecs> modules = PvModulesData.getInstance().getModules();
            int i = 0;
            for (final String key : modules.keySet()) {
                pvFinance.setPvModelCost(key, pvModelCosts[i]);
                i++;
            }

            final CspCustomPrice cspFinance = Scene.getInstance().getCspCustomPrice();
            cspFinance.setLifespan(cspLifespan);
            cspFinance.setkWhSellingPrice(cspKWhSellPrice);
            cspFinance.setLandRentalCost(cspLandUnitCost);
            cspFinance.setCleaningCost(cspCleaningCost);
            cspFinance.setMaintenanceCost(cspMaintenanceCost);
            cspFinance.setHeliostatUnitCost(heliostatUnitCost);
            cspFinance.setTowerUnitCost(towerHeightUnitCost);
            cspFinance.setParabolicTroughUnitCost(parabolicTroughUnitCost);
            cspFinance.setParabolicDishUnitCost(parabolicDishUnitCost);
            cspFinance.setFresnelReflectorUnitCost(fresnelReflectorUnitCost);

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

            FinanceDialog.this.dispose();

        });
        okButton.setActionCommand("OK");
        buttonPanel.add(okButton);
        getRootPane().setDefaultButton(okButton);

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> FinanceDialog.this.dispose());
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

    void selectPvPrices() {
        tabbedPane.setSelectedComponent(pvSystemPanel);
    }

    void selectCspPrices() {
        tabbedPane.setSelectedComponent(cspSystemPanel);
    }

}