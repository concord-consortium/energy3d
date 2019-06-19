package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FoundationPolygon;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.simulation.PvCustomPrice;
import org.concord.energy3d.simulation.PvDesignSpecs;

/**
 * @author Charles Xie
 */
public class PvProjectInfoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final DecimalFormat noDecimals = new DecimalFormat();
    private final JPanel countPanel, costPanel, landAreaPanel, panelAreaPanel;
    private final ColorBar countBar, costBar, landAreaBar, panelAreaBar;

    PvProjectInfoPanel() {

        super(new BorderLayout());

        noDecimals.setMaximumFractionDigits(0);

        final JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        add(container, BorderLayout.NORTH);

        // solar panel count on the selected base

        countPanel = new JPanel(new BorderLayout());
        countPanel.setBorder(EnergyPanel.createTitledBorder("Number of solar panels", true));
        container.add(countPanel);
        countBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
        countBar.setUnit("");
        countBar.setUnitPrefix(false);
        countBar.setVerticalLineRepresentation(false);
        countBar.setDecimalDigits(0);
        countBar.setToolTipText(countPanel.getToolTipText());
        countBar.setPreferredSize(new Dimension(100, 16));
        countPanel.add(countBar, BorderLayout.CENTER);

        // average area of a solar panel on the selected base

        landAreaPanel = new JPanel(new BorderLayout());
        landAreaPanel.setBorder(EnergyPanel.createTitledBorder("<html>Average land area occupied by a panel (m<sup>2</sup>)</html>", true));
        container.add(landAreaPanel);
        landAreaBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
        landAreaBar.setUnit("");
        landAreaBar.setVerticalLineRepresentation(false);
        landAreaBar.setDecimalDigits(2);
        landAreaBar.setMaximum(10);
        landAreaBar.setToolTipText(landAreaPanel.getToolTipText());
        landAreaBar.setPreferredSize(new Dimension(100, 16));
        landAreaPanel.add(landAreaBar, BorderLayout.CENTER);

        // total solar panel area on the selected base

        panelAreaPanel = new JPanel(new BorderLayout());
        panelAreaPanel.setBorder(EnergyPanel.createTitledBorder("<html>Total surface area of panels (m<sup>2</sup>)</html>", true));
        container.add(panelAreaPanel);
        panelAreaBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
        panelAreaBar.setUnit("");
        panelAreaBar.setVerticalLineRepresentation(false);
        panelAreaBar.setDecimalDigits(2);
        panelAreaBar.setMaximum(100000);
        panelAreaBar.setToolTipText(panelAreaPanel.getToolTipText());
        panelAreaBar.setPreferredSize(new Dimension(100, 16));
        panelAreaPanel.add(panelAreaBar, BorderLayout.CENTER);

        // solar panel cost on the selected base

        costPanel = new JPanel(new BorderLayout());
        costPanel.setBorder(EnergyPanel.createTitledBorder("Total cost", true));
        container.add(costPanel);
        costBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
        costBar.setUnit("$");
        costBar.setUnitPrefix(true);
        costBar.setVerticalLineRepresentation(false);
        costBar.setDecimalDigits(0);
        costBar.setToolTipText(costPanel.getToolTipText());
        costBar.setPreferredSize(new Dimension(100, 16));
        costPanel.add(costBar, BorderLayout.CENTER);

        final JPanel placeholder = new JPanel();
        placeholder.setPreferredSize(new Dimension(100, 100));
        container.add(placeholder);

    }

    void update(final Foundation foundation) {
        final PvDesignSpecs specs = Scene.getInstance().getPvDesignSpecs();
        if (specs == null) {
            return;
        }
        final PvCustomPrice price = Scene.getInstance().getPvCustomPrice();
        if (price == null) {
            return;
        }
        int countSolarPanels = 0;
        double cost = 0;
        double panelArea = 0;
        final List<SolarPanel> panels = foundation.getSolarPanels();
        if (!panels.isEmpty()) {
            countSolarPanels += panels.size();
            for (final SolarPanel s : panels) {
                cost += price.getTotalCost(s);
                panelArea += s.getPanelWidth() * s.getPanelHeight();
            }
        }
        final List<Rack> racks = foundation.getRacks();
        if (!racks.isEmpty()) {
            for (final Rack r : racks) {
                countSolarPanels += r.getNumberOfSolarPanels();
                cost += price.getTotalCost(r);
                panelArea += r.getArea();
            }
        }
        countBar.setValue(countSolarPanels);
        countBar.setMaximum(specs.getMaximumNumberOfSolarPanels());
        countBar.setEnabled(specs.isNumberOfSolarPanelsEnabled());
        float landArea;
        final FoundationPolygon polygon = foundation.getPolygon();
        if (polygon != null && polygon.isVisible()) {
            landArea = (float) polygon.getArea();
        } else {
            landArea = (float) foundation.getArea();
        }
        cost += landArea * price.getLandUnitPrice() * price.getLifespan();
        landAreaBar.setValue(countSolarPanels == 0 ? 0 : landArea / countSolarPanels);
        costBar.setValue(Math.round(cost));
        costBar.setMaximum(specs.getMaximumBudget());
        costBar.setEnabled(specs.isBudgetEnabled());
        String t = "Total cost over " + price.getLifespan() + " years";
        if (specs.isBudgetEnabled()) {
            t += " (" + "<$" + specs.getMaximumBudget() + ")";
        }
        costPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
        panelAreaBar.setValue((float) panelArea);
        panelAreaBar.setMaximum(foundation.getArea());
        repaint();
    }

    public void updateBudgetMaximum() {
        final PvCustomPrice price = Scene.getInstance().getPvCustomPrice();
        if (price == null) {
            return;
        }
        final PvDesignSpecs specs = Scene.getInstance().getPvDesignSpecs();
        if (specs == null) {
            return;
        }
        String t = "Total cost over " + price.getLifespan() + " years";
        if (specs.isBudgetEnabled()) {
            t += " (" + "<$" + specs.getMaximumBudget() + ")";
        }
        costBar.setMaximum(specs.getMaximumBudget());
        costPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
        costBar.setEnabled(specs.isBudgetEnabled());
        costBar.repaint();
    }

    public void updateSolarPanelNumberMaximum() {
        final PvDesignSpecs specs = Scene.getInstance().getPvDesignSpecs();
        if (specs == null) {
            return;
        }
        String t = "Number of solar panels";
        if (specs.isNumberOfSolarPanelsEnabled()) {
            t += " (" + "<" + specs.getMaximumNumberOfSolarPanels() + ")";
        }
        countBar.setMaximum(specs.getMaximumNumberOfSolarPanels());
        countPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
        countBar.setEnabled(specs.isNumberOfSolarPanelsEnabled());
        countBar.repaint();
    }

}