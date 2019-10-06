package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.simulation.PieChart;
import org.concord.energy3d.simulation.PvDesignSpecs;
import org.concord.energy3d.simulation.PvFinancialModel;
import org.concord.energy3d.simulation.PvProjectCost;
import org.concord.energy3d.util.ClipImage;
import org.concord.energy3d.util.Util;

/**
 * @author Charles Xie
 */
public class PvProjectZoneCostGraph extends JPanel {

    public final static Color[] colors = new Color[]{new Color(250, 128, 114), new Color(135, 206, 250),
            new Color(50, 205, 50), new Color(218, 165, 32), new Color(225, 105, 155)};
    private static final long serialVersionUID = 1L;

    private final Box buttonPanel;
    private final JPanel budgetPanel;
    private final ColorBar budgetBar;
    private final JPopupMenu popupMenu;
    private final DecimalFormat noDecimal = new DecimalFormat();
    private Foundation foundation;

    // the following costs are only for the selected zone, represented by the foundation
    private double landRentalCost;
    private double solarPanelCost;
    private double cleaningCost;
    private double maintenanceCost;
    private double loanInterest;
    private double totalCost;

    PvProjectZoneCostGraph() {

        super(new BorderLayout());

        noDecimal.setMaximumFractionDigits(0);
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
        PvFinancialModel financialModel = Scene.getInstance().getPvFinancialModel();
        solarPanelCost = 0;
        cleaningCost = 0;
        maintenanceCost = 0;
        for (final HousePart p : Scene.getInstance().getParts()) {
            if (p.getTopContainer() == foundation) {
                if (p instanceof SolarPanel) {
                    solarPanelCost += PvProjectCost.getPartCost(p);
                    cleaningCost += financialModel.getCleaningCost();
                    maintenanceCost += financialModel.getMaintenanceCost();
                }
                if (p instanceof Rack) {
                    solarPanelCost += PvProjectCost.getPartCost(p);
                    int n = ((Rack) p).getNumberOfSolarPanels();
                    cleaningCost += financialModel.getCleaningCost() * n;
                    maintenanceCost += financialModel.getMaintenanceCost() * n;
                }
            }
        }
        cleaningCost *= financialModel.getLifespan();
        maintenanceCost *= financialModel.getLifespan();
        landRentalCost = PvProjectCost.getPartCost(foundation);
        loanInterest = solarPanelCost * financialModel.getLoanInterestRate() * financialModel.getLifespan();
        totalCost = landRentalCost + cleaningCost + maintenanceCost + loanInterest + solarPanelCost;
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
            final String t = "Total (" + (pvSpecs.isBudgetEnabled() ? "\u2264 $" + noDecimal.format(pvSpecs.getMaximumBudget()) : "$") + ")";
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

        final double[] data = new double[]{landRentalCost, cleaningCost, maintenanceCost, loanInterest, solarPanelCost};
        PvFinancialModel financialModel = Scene.getInstance().getPvFinancialModel();
        final String years = "(" + financialModel.getLifespan() + " years)";
        final String[] legends = new String[]{"Land Rental " + years, "Cleaning " + years, "Maintenance " + years, "Loan Interest " + years, "Solar Panels (One-Time)"};

        PieChart pie = new PieChart(data, colors, legends, "$", null, "Move mouse for more info", false);
        pie.setBackground(Color.WHITE);
        pie.setPreferredSize(new Dimension(getWidth() - 5, getHeight() - budgetPanel.getHeight() - 5));
        pie.setBorder(BorderFactory.createEtchedBorder());
        pie.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    PvProjectCost.getInstance().showGraph();
                } else {
                    if (Util.isRightClick(e)) {
                        popupMenu.show(PvProjectZoneCostGraph.this, e.getX(), e.getY());
                    }
                }
            }
        });

        add(pie, BorderLayout.CENTER);

        repaint();

        EnergyPanel.getInstance().validate();

    }

}