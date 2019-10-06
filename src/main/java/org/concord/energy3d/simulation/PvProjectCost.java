package org.concord.energy3d.simulation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.PvProjectZoneCostGraph;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

/**
 * Calculate the cost of a PV project, be it a ground-mounted solar farm or a rooftop system.
 *
 * @author Charles Xie
 */
public class PvProjectCost extends ProjectCost {

    private static PvProjectCost instance = new PvProjectCost();

    private PvProjectCost() {
    }

    public static PvProjectCost getInstance() {
        return instance;
    }

    public static double getPartCost(final HousePart part) {
        final PvFinancialModel model = Scene.getInstance().getPvFinancialModel();
        if (part instanceof SolarPanel) {
            return model.getCost((SolarPanel) part);
        }
        if (part instanceof Rack) {
            return model.getCost((Rack) part);
        }
        if (part instanceof Foundation) {
            final Foundation f = (Foundation) part;
            return f.getArea() * model.getLandRentalCost() * model.getLifespan();
        }
        return 0;
    }

    public static double getTotalSolarPanelCost() {
        double total = 0;
        for (final HousePart p : Scene.getInstance().getParts()) {
            if (p instanceof SolarPanel || p instanceof Rack) {
                total += getPartCost(p);
            }
        }
        return total;
    }

    public double getTotalCost() {
        double solarPanelCost = 0;
        double landRentalCost = 0;
        PvFinancialModel model = Scene.getInstance().getPvFinancialModel();
        final List<Foundation> foundations = Scene.getInstance().getAllFoundations();
        if (!foundations.isEmpty()) {
            for (final Foundation f : foundations) {
                final List<SolarPanel> panels = f.getSolarPanels();
                if (!panels.isEmpty()) {
                    for (final SolarPanel s : panels) {
                        solarPanelCost += model.getCost(s);
                    }
                }
                final List<Rack> racks = f.getRacks();
                if (!racks.isEmpty()) {
                    for (final Rack r : racks) {
                        solarPanelCost += model.getCost(r);
                    }
                }
                landRentalCost += f.getArea() * model.getLandRentalCost() * model.getLifespan();
            }
        }
        double cost = solarPanelCost + landRentalCost;
        cost += Scene.getInstance().countSolarPanels() * (model.getCleaningCost() + model.getMaintenanceCost()) * model.getLifespan();
        cost += solarPanelCost * model.getLoanInterestRate() * model.getLifespan();
        return cost;
    }

    public double getCostByFoundation(final Foundation foundation) {
        if (foundation == null || foundation.getProjectType() != Foundation.TYPE_PV_PROJECT) {
            return 0;
        }
        PvFinancialModel model = Scene.getInstance().getPvFinancialModel();
        double solarPanelCost = 0;
        double cleaningCost = 0;
        double maintenanceCost = 0;
        double loanInterest = 0;
        double landRentalCost = getPartCost(foundation);
        for (final HousePart p : Scene.getInstance().getParts()) {
            if (p.getTopContainer() == foundation) {
                if (p instanceof SolarPanel) {
                    solarPanelCost += getPartCost(p);
                    cleaningCost += model.getCleaningCost();
                    maintenanceCost += model.getMaintenanceCost();
                }
                if (p instanceof Rack) {
                    solarPanelCost += getPartCost(p);
                    int n = ((Rack) p).getNumberOfSolarPanels();
                    cleaningCost += model.getCleaningCost() * n;
                    maintenanceCost += model.getMaintenanceCost() * n;
                }
            }
        }
        cleaningCost *= model.getLifespan();
        maintenanceCost *= model.getLifespan();
        loanInterest = solarPanelCost * model.getLoanInterestRate() * model.getLifespan();
        return landRentalCost + cleaningCost + maintenanceCost + loanInterest + solarPanelCost;
    }

    @Override
    void showPieChart() {

        final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
        final Foundation selectedFoundation;
        if (selectedPart == null || selectedPart instanceof Tree || selectedPart instanceof Human) {
            selectedFoundation = null;
        } else if (selectedPart instanceof Foundation) {
            selectedFoundation = (Foundation) selectedPart;
        } else {
            selectedFoundation = selectedPart.getTopContainer();
            selectedPart.setEditPointsVisible(false);
            SceneManager.getInstance().setSelectedPart(selectedFoundation);
        }

        String details = "";
        int count = 0;
        for (final HousePart p : Scene.getInstance().getParts()) {
            if (p instanceof Foundation) {
                count++;
                if (selectedFoundation == null) {
                    final Foundation foundation = (Foundation) p;
                    details += "Zone #" + foundation.getId() + ": $" + Graph.TWO_DECIMALS.format(getCostByFoundation(foundation)) + " | ";
                }
            }
        }
        if (selectedFoundation == null) {
            if (count > 0) {
                details = details.substring(0, details.length() - 3);
            }
        }

        PvFinancialModel financialModel = Scene.getInstance().getPvFinancialModel();

        double landRentalCost = 0;
        double solarPanelCost = 0;
        double cleaningCost = 0;
        double maintenanceCost = 0;
        String info;
        if (selectedFoundation != null) {
            info = "Zone #" + selectedFoundation.getId();
            landRentalCost = getPartCost(selectedFoundation);
            for (final HousePart p : Scene.getInstance().getParts()) {
                if (p.getTopContainer() == selectedFoundation) {
                    if (p instanceof SolarPanel) {
                        solarPanelCost += getPartCost(p);
                        cleaningCost += financialModel.getCleaningCost();
                        maintenanceCost += financialModel.getMaintenanceCost();
                    } else if (p instanceof Rack) {
                        solarPanelCost += getPartCost(p);
                        int n = ((Rack) p).getNumberOfSolarPanels();
                        cleaningCost += financialModel.getCleaningCost() * n;
                        maintenanceCost += financialModel.getMaintenanceCost() * n;
                    }
                }
            }
        } else {
            info = count > 1 ? count + " zones" : count + " zone";
            for (final HousePart p : Scene.getInstance().getParts()) {
                if (p instanceof Foundation) {
                    landRentalCost += getPartCost(p);
                } else if (p instanceof SolarPanel) {
                    solarPanelCost += getPartCost(p);
                    cleaningCost += financialModel.getCleaningCost();
                    maintenanceCost += financialModel.getMaintenanceCost();
                } else if (p instanceof Rack) {
                    solarPanelCost += getPartCost(p);
                    cleaningCost += financialModel.getCleaningCost() * ((Rack) p).getNumberOfSolarPanels();
                    maintenanceCost += financialModel.getMaintenanceCost() * ((Rack) p).getNumberOfSolarPanels();
                }
            }
        }
        cleaningCost *= financialModel.getLifespan();
        maintenanceCost *= financialModel.getLifespan();
        double loanInterest = solarPanelCost * financialModel.getLoanInterestRate() * financialModel.getLifespan();

        final double[] data = new double[]{landRentalCost, cleaningCost, maintenanceCost, loanInterest, solarPanelCost};
        final String years = "(" + financialModel.getLifespan() + " years)";
        final String[] legends = new String[]{"Land Rental " + years, "Cleaning " + years, "Maintenance " + years, "Loan Interest " + years, "Solar Panels (One-Time)"};

        // show them in a popup window
        final PieChart pie = new PieChart(data, PvProjectZoneCostGraph.colors, legends, "$", info, count > 1 ? details : null, true);
        pie.setBackground(Color.WHITE);
        pie.setBorder(BorderFactory.createEtchedBorder());
        final JDialog dialog = new JDialog(MainFrame.getInstance(), "Cost Breakdown", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(pie, BorderLayout.CENTER);
        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JButton buttonClose = new JButton("Close");
        buttonClose.addActionListener(e -> dialog.dispose());
        buttonPanel.add(buttonClose);
        dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(MainFrame.getInstance());
        dialog.setVisible(true);

    }

}