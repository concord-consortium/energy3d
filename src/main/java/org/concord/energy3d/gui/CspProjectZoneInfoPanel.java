package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.simulation.CspFinancialModel;
import org.concord.energy3d.simulation.CspDesignSpecs;
import org.concord.energy3d.simulation.CspProjectCost;

/**
 * @author Charles Xie
 */
public class CspProjectZoneInfoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JPanel countPanel, costPanel, packingDensityPanel, moduleCountPanel;
    private final ColorBar countBar, costBar, packingDensityBar, moduleCountBar;

    CspProjectZoneInfoPanel() {

        super(new BorderLayout());

        final JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        add(container, BorderLayout.NORTH);

        // mirror/trough/dish/Fresnel reflector count on the selected base

        countPanel = new JPanel(new BorderLayout());
        countPanel.setBorder(EnergyPanel.createTitledBorder("Number of heliostats", true));
        container.add(countPanel);
        countBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
        countBar.setUnit("");
        countBar.setUnitPrefix(false);
        countBar.setVerticalLineRepresentation(false);
        countBar.setDecimalDigits(0);
        countBar.setToolTipText(countPanel.getToolTipText());
        countBar.setPreferredSize(new Dimension(100, 16));
        countPanel.add(countBar, BorderLayout.CENTER);

        moduleCountPanel = new JPanel(new BorderLayout());
        moduleCountPanel.setBorder(EnergyPanel.createTitledBorder("Number of modules", true));
        container.add(moduleCountPanel);
        moduleCountBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
        moduleCountBar.setUnit("");
        moduleCountBar.setUnitPrefix(false);
        moduleCountBar.setVerticalLineRepresentation(false);
        moduleCountBar.setDecimalDigits(0);
        moduleCountBar.setToolTipText(moduleCountPanel.getToolTipText());
        moduleCountBar.setPreferredSize(new Dimension(100, 16));
        moduleCountPanel.add(moduleCountBar, BorderLayout.CENTER);

        // total reflector area on the selected base

        packingDensityPanel = new JPanel(new BorderLayout());
        packingDensityPanel.setBorder(EnergyPanel.createTitledBorder("<html>Packing density (reflecting area / field area)</html>", true));
        container.add(packingDensityPanel);
        packingDensityBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
        packingDensityBar.setUnit("");
        packingDensityBar.setVerticalLineRepresentation(false);
        packingDensityBar.setDecimalDigits(2);
        packingDensityBar.setMaximum(1);
        packingDensityBar.setToolTipText(packingDensityPanel.getToolTipText());
        packingDensityBar.setPreferredSize(new Dimension(100, 16));
        packingDensityPanel.add(packingDensityBar, BorderLayout.CENTER);

        // mirror/trough/dish cost on the selected base

        costPanel = new JPanel(new BorderLayout());
        costPanel.setBorder(EnergyPanel.createTitledBorder("Total cost", true));
        container.add(costPanel);
        costBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
        costBar.setMaximum(10000000);
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

        final CspFinancialModel model = Scene.getInstance().getCspFinancialModel();
        final List<ParabolicTrough> troughs = foundation.getParabolicTroughs();
        if (!troughs.isEmpty()) {
            countBar.setValue(troughs.size());
            int totalModules = 0;
            for (final ParabolicTrough t : troughs) {
                totalModules += t.getNumberOfModules();
            }
            moduleCountBar.setValue(totalModules);
            countPanel.setBorder(EnergyPanel.createTitledBorder("Number of parabolic troughs", true));
            double reflectingArea = 0;
            double troughArea;
            for (final ParabolicTrough t : troughs) {
                troughArea = t.getTroughLength() * t.getApertureWidth();
                reflectingArea += troughArea;
            }
            packingDensityBar.setValue((float) (reflectingArea / foundation.getArea()));
            costBar.setValue(Math.round(CspProjectCost.getInstance().getCostByFoundation(foundation)));
            final CspDesignSpecs specs = Scene.getInstance().getCspDesignSpecs();
            String t = "Total cost over " + model.getLifespan() + " years";
            if (specs.isBudgetEnabled()) {
                t += " (" + "<$" + specs.getMaximumBudget() + ")";
            }
            costPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
        } else {
            final List<ParabolicDish> dishes = foundation.getParabolicDishes();
            if (!dishes.isEmpty()) {
                countBar.setValue(dishes.size());
                moduleCountBar.setValue(dishes.size());
                countPanel.setBorder(EnergyPanel.createTitledBorder("Number of parabolic dishes", true));
                double reflectingArea = 0;
                double rimArea;
                for (final ParabolicDish d : dishes) {
                    rimArea = d.getRimRadius() * d.getRimRadius() * Math.PI;
                    reflectingArea += rimArea;
                }
                packingDensityBar.setValue((float) (reflectingArea / foundation.getArea()));
                costBar.setValue(Math.round(CspProjectCost.getInstance().getCostByFoundation(foundation)));
                final CspDesignSpecs specs = Scene.getInstance().getCspDesignSpecs();
                String t = "Total cost over " + model.getLifespan() + " years";
                if (specs.isBudgetEnabled()) {
                    t += " (" + "<$" + specs.getMaximumBudget() + ")";
                }
                costPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
            } else {
                final List<FresnelReflector> fresnels = foundation.getFresnelReflectors();
                if (fresnels.isEmpty()) {
                    final List<Mirror> heliostats = foundation.getHeliostats();
                    countBar.setValue(heliostats.size());
                    moduleCountBar.setValue(heliostats.size());
                    countPanel.setBorder(EnergyPanel.createTitledBorder(heliostats.size() > 0 ? "Number of heliostats" : "------", true));
                    double reflectingArea = 0;
                    double apertureArea;
                    for (final Mirror m : heliostats) {
                        apertureArea = m.getApertureWidth() * m.getApertureHeight();
                        reflectingArea += apertureArea;
                    }
                    packingDensityBar.setValue((float) (reflectingArea / foundation.getArea()));
                    costBar.setValue(Math.round(CspProjectCost.getInstance().getCostByFoundation(foundation)));
                    final CspDesignSpecs specs = Scene.getInstance().getCspDesignSpecs();
                    String t = "Total cost over " + model.getLifespan() + " years";
                    if (specs.isBudgetEnabled()) {
                        t += " (" + "<$" + specs.getMaximumBudget() + ")";
                    }
                    costPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
                } else {
                    countBar.setValue(fresnels.size());
                    int totalModules = 0;
                    for (final FresnelReflector r : fresnels) {
                        totalModules += r.getNumberOfModules();
                    }
                    moduleCountBar.setValue(totalModules);
                    countPanel.setBorder(EnergyPanel.createTitledBorder("Number of Fresnel reflectors", true));
                    double reflectingArea = 0;
                    double unitArea;
                    for (final FresnelReflector r : fresnels) {
                        unitArea = r.getLength() * r.getModuleWidth();
                        reflectingArea += unitArea;
                    }
                    packingDensityBar.setValue((float) (reflectingArea / foundation.getArea()));
                    costBar.setValue(Math.round(CspProjectCost.getInstance().getCostByFoundation(foundation)));
                    final CspDesignSpecs specs = Scene.getInstance().getCspDesignSpecs();
                    String t = "Total cost over " + model.getLifespan() + " years";
                    if (specs.isBudgetEnabled()) {
                        t += " (" + "<$" + specs.getMaximumBudget() + ")";
                    }
                    costPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
                }
            }
        }

    }

    public void updateBudgetMaximum() {
        final CspFinancialModel model = Scene.getInstance().getCspFinancialModel();
        if (model == null) {
            return;
        }
        final CspDesignSpecs specs = Scene.getInstance().getCspDesignSpecs();
        if (specs == null) {
            return;
        }
        String t = "Total cost over " + model.getLifespan() + " years";
        if (specs.isBudgetEnabled()) {
            t += " (" + "<$" + specs.getMaximumBudget() + ")";
        }
        costBar.setMaximum(specs.getMaximumBudget());
        costPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
        costBar.setEnabled(specs.isBudgetEnabled());
        costBar.repaint();
    }

    public void updateHeliostatNumberMaximum() {
        if (Scene.getInstance().countParts(Mirror.class) > 0) {
            final CspDesignSpecs specs = Scene.getInstance().getCspDesignSpecs();
            if (specs == null) {
                return;
            }
            String t = "Number of heliostats";
            if (specs.isNumberOfMirrorsEnabled()) {
                t += " (" + "<" + specs.getMaximumNumberOfMirrors() + ")";
            }
            countBar.setMaximum(specs.getMaximumNumberOfMirrors());
            countPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
            countBar.setEnabled(specs.isNumberOfMirrorsEnabled());
            countBar.repaint();
        }
    }

    public void updateParabolicTroughNumberMaximum() {
        if (Scene.getInstance().countParts(ParabolicTrough.class) > 0) {
            final CspDesignSpecs specs = Scene.getInstance().getCspDesignSpecs();
            if (specs == null) {
                return;
            }
            String t = "Number of parabolic troughs";
            if (specs.isNumberOfParabolicTroughsEnabled()) {
                t += " (" + "<" + specs.getMaximumNumberOfParabolicTroughs() + ")";
            }
            countBar.setMaximum(specs.getMaximumNumberOfParabolicTroughs());
            countPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
            countBar.setEnabled(specs.isNumberOfParabolicTroughsEnabled());
            countBar.repaint();
        }
    }

}