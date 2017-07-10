package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.simulation.CspCustomPrice;
import org.concord.energy3d.simulation.CspDesignSpecs;

/**
 * @author Charles Xie
 *
 */
public class CspStationInfoPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final DecimalFormat noDecimals = new DecimalFormat();
	private final JPanel countPanel, costPanel, packingDensityPanel, moduleCountPanel;
	private final ColorBar countBar, costBar, packingDensityBar, moduleCountBar;

	public CspStationInfoPanel() {

		super(new BorderLayout());

		noDecimals.setMaximumFractionDigits(0);

		final JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		add(container, BorderLayout.NORTH);

		// mirror/trough count on the selected base

		countPanel = new JPanel(new BorderLayout());
		countPanel.setBorder(EnergyPanel.createTitledBorder("Number of mirrors", true));
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

		// mirror/trough cost on the selected base

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

	}

	void update(final Foundation foundation) {

		final List<ParabolicTrough> troughs = foundation.getParabolicTroughs();
		if (!troughs.isEmpty()) {
			countBar.setValue(troughs.size());
			int totalModules = 0;
			for (final ParabolicTrough t : troughs) {
				totalModules += t.getNumberOfModules();
			}
			moduleCountBar.setValue(totalModules);
			countPanel.setBorder(EnergyPanel.createTitledBorder("Number of parabolic troughs", true));
			double cost = 0;
			double reflectingArea = 0;
			double troughArea = 0;
			final CspCustomPrice price = Scene.getInstance().getCspCustomPrice();
			for (final ParabolicTrough t : troughs) {
				troughArea = t.getTroughLength() * t.getApertureWidth();
				cost += price.getParabolicTroughUnitPrice() * troughArea;
				reflectingArea += troughArea;
			}
			cost += foundation.getArea() * price.getLandUnitPrice() * price.getLifespan();
			costBar.setValue(Math.round(cost));
			final CspDesignSpecs specs = Scene.getInstance().getCspDesignSpecs();
			String t = "Total cost over " + price.getLifespan() + " years";
			if (specs.isBudgetEnabled()) {
				t += " (" + "<$" + specs.getMaximumBudget() + ")";
			}
			costPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
			packingDensityBar.setValue((float) (reflectingArea / foundation.getArea()));
		} else {
			final List<Mirror> mirrors = foundation.getMirrors();
			countBar.setValue(mirrors.size());
			moduleCountBar.setValue(mirrors.size());
			countPanel.setBorder(EnergyPanel.createTitledBorder("Number of mirrors", true));
			double cost = 0;
			double reflectingArea = 0;
			double mirrorArea = 0;
			final CspCustomPrice price = Scene.getInstance().getCspCustomPrice();
			final ArrayList<Foundation> towers = new ArrayList<Foundation>();
			for (final Mirror m : mirrors) {
				mirrorArea = m.getMirrorWidth() * m.getMirrorHeight();
				cost += price.getMirrorUnitPrice() * mirrorArea;
				cost += price.getHeliostatPrice() * mirrorArea;
				reflectingArea += mirrorArea;
				if (m.getHeliostatTarget() != null) {
					if (!towers.contains(m.getHeliostatTarget())) {
						towers.add(m.getHeliostatTarget());
					}
				}
			}
			if (!towers.isEmpty()) {
				for (final Foundation tower : towers) {
					cost += price.getTowerUnitPrice() * tower.getSolarReceiverHeight(0) * Scene.getInstance().getAnnotationScale();
				}
			}
			cost += foundation.getArea() * price.getLandUnitPrice() * price.getLifespan();
			costBar.setValue(Math.round(cost));
			final CspDesignSpecs specs = Scene.getInstance().getCspDesignSpecs();
			String t = "Total cost over " + price.getLifespan() + " years";
			if (specs.isBudgetEnabled()) {
				t += " (" + "<$" + specs.getMaximumBudget() + ")";
			}
			costPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
			packingDensityBar.setValue((float) (reflectingArea / foundation.getArea()));
		}
	}

	public void updateBudgetMaximum() {
		final CspCustomPrice price = Scene.getInstance().getCspCustomPrice();
		final CspDesignSpecs specs = Scene.getInstance().getCspDesignSpecs();
		String t = "Total cost over " + price.getLifespan() + " years";
		if (specs.isBudgetEnabled()) {
			t += " (" + "<$" + specs.getMaximumBudget() + ")";
		}
		costBar.setMaximum(specs.getMaximumBudget());
		costPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
		costBar.setEnabled(specs.isBudgetEnabled());
		costBar.repaint();
	}

	public void updateMirrorNumberMaximum() {
		if (Scene.getInstance().countParts(new Class[] { Mirror.class }) > 0) {
			final CspDesignSpecs specs = Scene.getInstance().getCspDesignSpecs();
			String t = "Number of mirrors";
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
		if (Scene.getInstance().countParts(new Class[] { ParabolicTrough.class }) > 0) {
			final CspDesignSpecs specs = Scene.getInstance().getCspDesignSpecs();
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
