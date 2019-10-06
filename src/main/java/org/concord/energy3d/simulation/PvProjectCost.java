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
import org.concord.energy3d.gui.PvProjectCostGraph;
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

    public double getTotalCost() {
        double cost = 0;
        final List<Foundation> foundations = Scene.getInstance().getAllFoundations();
        if (!foundations.isEmpty()) {
            for (final Foundation f : foundations) {
                cost += getCostByFoundation(f);
            }
        }
        PvFinancialModel fm = Scene.getInstance().getPvFinancialModel();
        cost += Scene.getInstance().countSolarPanels() * (fm.getCleaningCost() + fm.getMaintenanceCost()) * fm.getLifespan();
        return cost;
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

    public double getCostByFoundation(final Foundation foundation) {
        if (foundation == null || foundation.getProjectType() != Foundation.TYPE_PV_PROJECT) {
            return 0;
        }
        double sum = getPartCost(foundation);
        for (final HousePart p : Scene.getInstance().getParts()) {
            if (p.getTopContainer() == foundation) {
                if (p instanceof SolarPanel || p instanceof Rack) {
                    sum += getPartCost(p);
                }
            }
        }
        return sum;
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
                    details += "$" + (int) getCostByFoundation(foundation) + " (" + foundation.getId() + ") | ";
                }
            }
        }
        if (selectedFoundation == null) {
            if (count > 0) {
                details = details.substring(0, details.length() - 2);
            }
        }

        PvFinancialModel financialModel = Scene.getInstance().getPvFinancialModel();

        double landRentalCostSum = 0;
        double solarPanelCostSum = 0;
        double cleaningCostSum = 0;
        double maintenanceCostSum = 0;
        String info;
        if (selectedFoundation != null) {
            info = "Zone #" + selectedFoundation.getId();
            landRentalCostSum = getPartCost(selectedFoundation);
            for (final HousePart p : Scene.getInstance().getParts()) {
                if (p.getTopContainer() == selectedFoundation) {
                    if (p instanceof SolarPanel) {
                        solarPanelCostSum += getPartCost(p);
                        cleaningCostSum += financialModel.getCleaningCost();
                        maintenanceCostSum += financialModel.getMaintenanceCost();
                    } else if (p instanceof Rack) {
                        solarPanelCostSum += getPartCost(p);
                        int n = ((Rack) p).getNumberOfSolarPanels();
                        cleaningCostSum += financialModel.getCleaningCost() * n;
                        maintenanceCostSum += financialModel.getMaintenanceCost() * n;
                    }
                }
            }
        } else {
            info = count > 1 ? count + " zones" : count + " zone";
            for (final HousePart p : Scene.getInstance().getParts()) {
                if (p instanceof Foundation) {
                    landRentalCostSum += getPartCost(p);
                } else if (p instanceof SolarPanel) {
                    solarPanelCostSum += getPartCost(p);
                    cleaningCostSum += financialModel.getCleaningCost();
                    maintenanceCostSum += financialModel.getMaintenanceCost();
                } else if (p instanceof Rack) {
                    solarPanelCostSum += getPartCost(p);
                    cleaningCostSum += financialModel.getCleaningCost() * ((Rack) p).getNumberOfSolarPanels();
                    maintenanceCostSum += financialModel.getMaintenanceCost() * ((Rack) p).getNumberOfSolarPanels();
                }
            }
        }
        cleaningCostSum *= financialModel.getLifespan();
        maintenanceCostSum *= financialModel.getLifespan();

        final double[] data = new double[]{landRentalCostSum, cleaningCostSum, maintenanceCostSum, solarPanelCostSum};
        final String years = "(" + financialModel.getLifespan() + " years)";
        final String[] legends = new String[]{"Land Rental " + years, "Cleaning " + years, "Maintenance " + years, "Solar Panels (One-Time)"};

        // show them in a popup window
        final PieChart pie = new PieChart(data, PvProjectCostGraph.colors, legends, "$", info, count > 1 ? details : null, true);
        pie.setBackground(Color.WHITE);
        pie.setBorder(BorderFactory.createEtchedBorder());
        final JDialog dialog = new JDialog(MainFrame.getInstance(), "Project Costs by Category", true);
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