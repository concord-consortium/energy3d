package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.concord.energy3d.model.*;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.CspFinancialModel;
import org.concord.energy3d.simulation.CspProjectCost;
import org.concord.energy3d.simulation.PieChart;
import org.concord.energy3d.simulation.PvDesignSpecs;
import org.concord.energy3d.util.ClipImage;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 */
public class CspProjectZoneCostGraph extends JPanel {

    public final static Color[] colors = new Color[]{new Color(250, 128, 114), new Color(135, 206, 250),
            new Color(50, 205, 50), new Color(218, 165, 32), new Color(225, 105, 155), new Color(155, 105, 225)};
    private static final long serialVersionUID = 1L;

    private final Box buttonPanel;
    private final JPanel budgetPanel;
    private final ColorBar budgetBar;
    private final JPopupMenu popupMenu;
    private final DecimalFormat noDecimals = new DecimalFormat();
    private Foundation foundation;

    private double collectorCost;
    private double receiverCost;
    private double landRentalCost;
    private double cleaningCost;
    private double maintenanceCost;
    private double loanInterest;
    private double totalCost;

    CspProjectZoneCostGraph() {

        super(new BorderLayout());

        noDecimals.setMaximumFractionDigits(0);
        budgetPanel = new JPanel(new BorderLayout());
        budgetBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
        budgetBar.setPreferredSize(new Dimension(100, 16));
        budgetBar.setToolTipText("<html>The total project cost must not exceed the limit (if specified).</html>");
        budgetPanel.add(budgetBar, BorderLayout.CENTER);

        buttonPanel = new Box(BoxLayout.Y_AXIS);
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(Box.createVerticalGlue());
        final JButton button = new JButton("Show");
        button.setAlignmentX(CENTER_ALIGNMENT);
        button.addActionListener(e -> {
            SceneManager.getInstance().autoSelectBuilding(true);
            final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
            if (selectedPart instanceof Foundation) {
                addGraph((Foundation) selectedPart);
                EnergyPanel.getInstance().validate();
            }
        });
        buttonPanel.add(button);
        buttonPanel.add(Box.createVerticalGlue());
        popupMenu = new JPopupMenu();
        popupMenu.setInvoker(this);
        popupMenu.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(final PopupMenuEvent e) {
            }

        });
        final JMenuItem mi = new JMenuItem("Copy Image");
        mi.addActionListener(e -> new ClipImage().copyImageToClipboard(this));
        popupMenu.add(mi);
    }

    private void calculateCost() {
        final CspFinancialModel model = Scene.getInstance().getCspFinancialModel();
        collectorCost = 0;
        if (foundation.hasSolarReceiver()) {
            receiverCost = CspProjectCost.getPartCost(foundation);
            loanInterest = receiverCost * model.getLoanInterestRate() * model.getLifespan();
            landRentalCost = 0;
            cleaningCost = 0;
            maintenanceCost = 0;
        } else {
            receiverCost = 0;
            int countModules = 0;
            for (final HousePart p : Scene.getInstance().getParts()) {
                if (p.getTopContainer() == foundation) {
                    if (p instanceof SolarCollector) { // assuming that sensor doesn't cost anything
                        collectorCost += CspProjectCost.getPartCost(p);
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
            landRentalCost = CspProjectCost.getPartCost(foundation);
            cleaningCost = model.getCleaningCost() * countModules * model.getLifespan();
            maintenanceCost = model.getMaintenanceCost() * countModules * model.getLifespan();
        }
        totalCost = landRentalCost + cleaningCost + maintenanceCost + loanInterest + collectorCost + receiverCost;
    }

    void removeGraph() {
        removeAll();
        repaint();
        add(buttonPanel, BorderLayout.CENTER);
        EnergyPanel.getInstance().validate();
        foundation = null;
    }

    public void updateBudget() {
        if (budgetPanel != null && foundation != null) {
            calculateCost();
            final PvDesignSpecs pvSpecs = Scene.getInstance().getPvDesignSpecs();
            budgetBar.setEnabled(pvSpecs.isBudgetEnabled());
            budgetBar.setMaximum(pvSpecs.getMaximumBudget());
            final String t = "Total (" + (pvSpecs.isBudgetEnabled() ? "\u2264 $" + noDecimals.format(pvSpecs.getMaximumBudget()) : "$") + ")";
            budgetPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
            budgetBar.setValue((float) totalCost);
            budgetBar.repaint();
            budgetPanel.repaint();
        }
    }

    public void addGraph(final Foundation foundation) {

        removeAll();

        this.foundation = foundation;
        calculateCost();
        updateBudget();

        add(budgetPanel, BorderLayout.NORTH);

        double[] data;
        String[] legends;
        CspFinancialModel model = Scene.getInstance().getCspFinancialModel();
        String years = "(" + model.getLifespan() + " years)";
        if (Util.isZero(receiverCost)) {
            data = new double[]{landRentalCost, cleaningCost, maintenanceCost, loanInterest, collectorCost};
            legends = new String[]{"Land " + years, "Cleaning " + years, "Maintenance " + years, "Loan Interest " + years, "Collectors (One-Time)"};
        } else {
            data = new double[]{landRentalCost, cleaningCost, maintenanceCost, loanInterest, collectorCost, receiverCost};
            legends = new String[]{"Land " + years, "Cleaning " + years, "Maintenance " + years, "Loan Interest " + years, "Collectors (One-Time)", "Receivers (One-Time)"};
        }

        PieChart pie = new PieChart(data, colors, legends, "$", null, "Move mouse for more info", false);
        pie.setBackground(Color.WHITE);
        pie.setPreferredSize(new Dimension(getWidth() - 5, getHeight() - budgetPanel.getHeight() - 5));
        pie.setBorder(BorderFactory.createEtchedBorder());
        pie.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    CspProjectCost.getInstance().showGraph();
                } else {
                    if (Util.isRightClick(e)) {
                        popupMenu.show(CspProjectZoneCostGraph.this, e.getX(), e.getY());
                    }
                }
            }
        });

        add(pie, BorderLayout.CENTER);

        repaint();

        EnergyPanel.getInstance().validate();

    }

}