package org.concord.energy3d.simulation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.concord.energy3d.gui.CspProjectZoneCostGraph;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.model.*;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

/**
 * Calculate the cost of a CSP project, covering four types of CSP technologies.
 *
 * @author Charles Xie
 */
public class CspProjectCost extends ProjectCost {

    private static CspProjectCost instance = new CspProjectCost();

    private CspProjectCost() {
    }

    public static CspProjectCost getInstance() {
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
        return cost;
    }

    public static double getTotalUpFrontCost() {
        double total = 0;
        for (final HousePart p : Scene.getInstance().getParts()) {
            if (p instanceof SolarCollector) {
                total += getPartCost(p);
            } else if (p instanceof Foundation) {
                Foundation f = (Foundation) p;
                if (f.hasSolarReceiver()) {
                    total += getPartCost(f);
                }
            }
        }
        return total;
    }

    public static double getPartCost(final HousePart part) {
        final CspFinancialModel model = Scene.getInstance().getCspFinancialModel();
        if (part instanceof Mirror) {
            return model.getHeliostatUnitCost() * part.getArea();
        }
        if (part instanceof ParabolicTrough) {
            return model.getParabolicTroughUnitCost() * part.getArea();
        }
        if (part instanceof ParabolicDish) {
            return model.getParabolicDishUnitCost() * part.getArea();
        }
        if (part instanceof FresnelReflector) {
            return model.getFresnelReflectorUnitCost() * part.getArea();
        }
        if (part instanceof Foundation) {
            final Foundation f = (Foundation) part;
            if (f.hasSolarReceiver()) { // TODO: solar receiver height may not be accurate while the model is still loading
                return model.getReceiverUnitCost() * f.getSolarReceiverHeight(0) * Scene.getInstance().getScale();
            }
            return f.getArea() * model.getLandRentalCost() * model.getLifespan();
        }
        return 0;
    }

    @Override
    public double getCostByFoundation(final Foundation foundation) {
        if (foundation == null || foundation.getProjectType() != Foundation.TYPE_CSP_PROJECT) {
            return 0;
        }
        final CspFinancialModel model = Scene.getInstance().getCspFinancialModel();
        if (foundation.hasSolarReceiver()) {
            double receiverCost = getPartCost(foundation);
            double loanInterest = receiverCost * model.getLoanInterestRate() * model.getLifespan();
            return receiverCost + loanInterest;
        }
        double collectorCost = 0;
        int countModules = 0;
        for (final HousePart p : Scene.getInstance().getParts()) {
            if (p.getTopContainer() == foundation) {
                if (p instanceof SolarCollector) { // assuming that sensor doesn't cost anything
                    collectorCost += getPartCost(p);
                    if (p instanceof Mirror || p instanceof ParabolicDish) {
                        countModules++;
                    } else if (p instanceof ParabolicTrough) {
                        countModules += ((ParabolicTrough) p).getNumberOfModules();
                    } else if (p instanceof FresnelReflector) {
                        countModules += ((FresnelReflector) p).getNumberOfModules();
                    }
                }
            }
        }
        double landRentalCost = getPartCost(foundation);
        double cleaningCost = model.getCleaningCost() * countModules * model.getLifespan();
        double maintenanceCost = model.getMaintenanceCost() * countModules * model.getLifespan();
        double loanInterest = collectorCost * model.getLoanInterestRate() * model.getLifespan();
        return landRentalCost + cleaningCost + maintenanceCost + loanInterest + collectorCost;
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

        final CspFinancialModel model = Scene.getInstance().getCspFinancialModel();

        String details = "";
        int count = 0;
        for (final HousePart p : Scene.getInstance().getParts()) {
            if (p instanceof Foundation) {
                final Foundation foundation = (Foundation) p;
                if (selectedFoundation == null) {
                    String type = foundation.hasSolarReceiver() ? "Receiver" : "Field";
                    details += type + " #" + foundation.getId() + ": $" + Graph.TWO_DECIMALS.format(getCostByFoundation(foundation)) + " | ";
                }
                count++;
            }
        }
        if (selectedFoundation == null) {
            if (count > 0) {
                details = details.substring(0, details.length() - 3);
            }
        }

        double collectorCost = 0;
        double receiverCost = 0;
        double landRentalCost = 0;
        double cleaningCost = 0;
        double maintenanceCost = 0;
        double loanInterest = 0;
        String info;
        if (selectedFoundation != null) {
            if (selectedFoundation.hasSolarReceiver()) {
                info = "Receiver #" + selectedFoundation.getId();
                receiverCost = getPartCost(selectedFoundation);
                loanInterest = receiverCost * model.getLoanInterestRate() * model.getLifespan();
            } else {
                info = "Field #" + selectedFoundation.getId();
                int countModules = 0;
                for (final HousePart p : Scene.getInstance().getParts()) {
                    if (p.getTopContainer() == selectedFoundation) {
                        if (p instanceof SolarCollector) { // assuming that sensor doesn't cost anything
                            collectorCost += getPartCost(p);
                            if (p instanceof Mirror || p instanceof ParabolicDish) {
                                countModules++;
                            } else if (p instanceof ParabolicTrough) {
                                countModules += ((ParabolicTrough) p).getNumberOfModules();
                            } else if (p instanceof FresnelReflector) {
                                countModules += ((FresnelReflector) p).getNumberOfModules();
                            }
                        }
                    }
                }
                loanInterest = collectorCost * model.getLoanInterestRate() * model.getLifespan();
                cleaningCost = model.getCleaningCost() * countModules * model.getLifespan();
                maintenanceCost = model.getMaintenanceCost() * countModules * model.getLifespan();
                landRentalCost = getPartCost(selectedFoundation);
            }
        } else {
            info = count > 1 ? count + " zones" : count + " zone";
            List<Mirror> heliostats = Scene.getInstance().getAllHeliostats();
            if (!heliostats.isEmpty()) {
                final List<Foundation> towers = new ArrayList<>();
                for (final Mirror m : heliostats) {
                    if (m.getReceiver() != null) {
                        if (!towers.contains(m.getReceiver())) {
                            towers.add(m.getReceiver());
                        }
                    }
                }
                if (!towers.isEmpty()) {
                    for (final Foundation tower : towers) {
                        receiverCost += getPartCost(tower);
                    }
                }
            }
            final List<FresnelReflector> reflectors = Scene.getInstance().getAllFresnelReflectors();
            if (!reflectors.isEmpty()) {
                final List<Foundation> absorbers = new ArrayList<>();
                for (final FresnelReflector r : reflectors) {
                    if (r.getReceiver() != null) {
                        if (!absorbers.contains(r.getReceiver())) {
                            absorbers.add(r.getReceiver());
                        }
                    }
                }
                if (!absorbers.isEmpty()) {
                    for (final Foundation absorber : absorbers) {
                        receiverCost += getPartCost(absorber);
                    }
                }
            }
            int countModules = 0;
            List<Foundation> zones = new ArrayList<>();
            for (final HousePart p : Scene.getInstance().getParts()) {
                if (p instanceof SolarCollector) {
                    collectorCost += getPartCost(p);
                    if (p instanceof Mirror || p instanceof ParabolicDish) {
                        countModules++;
                    } else if (p instanceof ParabolicTrough) {
                        countModules += ((ParabolicTrough) p).getNumberOfModules();
                    } else if (p instanceof FresnelReflector) {
                        countModules += ((FresnelReflector) p).getNumberOfModules();
                    }
                    if (!zones.contains(p.getTopContainer())) {
                        zones.add(p.getTopContainer());
                    }
                }
            }
            for (Foundation zone : zones) {
                landRentalCost += zone.getArea();
            }
            landRentalCost *= model.getLandRentalCost() * model.getLifespan();
            cleaningCost = model.getCleaningCost() * countModules * model.getLifespan();
            maintenanceCost = model.getMaintenanceCost() * countModules * model.getLifespan();
            loanInterest = (collectorCost + receiverCost) * model.getLoanInterestRate() * model.getLifespan();
        }

        double[] data;
        String[] legends;
        String years = "(" + model.getLifespan() + " years)";
        if (Util.isZero(receiverCost)) {
            data = new double[]{landRentalCost, cleaningCost, maintenanceCost, loanInterest, collectorCost};
            legends = new String[]{"Land Rental " + years, "Cleaning " + years, "Maintenance " + years, "Loan Interest " + years, "Collectors (One-Time)"};
        } else {
            data = new double[]{landRentalCost, cleaningCost, maintenanceCost, loanInterest, collectorCost, receiverCost};
            legends = new String[]{"Land Rental " + years, "Cleaning " + years, "Maintenance " + years, "Loan Interest " + years, "Collectors (One-Time)", "Receivers (One-Time)"};
        }

        // show them in a popup window
        final PieChart pie = new PieChart(data, CspProjectZoneCostGraph.colors, legends, "$", info, count > 1 ? details : null, true);
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