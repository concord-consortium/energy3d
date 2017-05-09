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
import org.concord.energy3d.simulation.Cost;
import org.concord.energy3d.simulation.DesignSpecs;

/**
 * @author Charles Xie
 *
 */
public class PvStationInfoPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final DecimalFormat noDecimals = new DecimalFormat();
	private final JPanel countPanel, costPanel, landAreaPanel, panelAreaPanel;
	private final ColorBar countBar, costBar, landAreaBar, panelAreaBar;

	public PvStationInfoPanel() {

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
		panelAreaBar.setToolTipText(panelAreaPanel.getToolTipText());
		panelAreaBar.setPreferredSize(new Dimension(100, 16));
		panelAreaPanel.add(panelAreaBar, BorderLayout.CENTER);

		// solar panel cost on the selected base

		costPanel = new JPanel(new BorderLayout());
		costPanel.setBorder(EnergyPanel.createTitledBorder("Cost of solar panels", true));
		container.add(costPanel);
		costBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
		costBar.setUnit("$");
		costBar.setUnitPrefix(true);
		costBar.setVerticalLineRepresentation(false);
		costBar.setDecimalDigits(0);
		costBar.setToolTipText(costPanel.getToolTipText());
		costBar.setPreferredSize(new Dimension(100, 16));
		costPanel.add(costBar, BorderLayout.CENTER);

	}

	void update(final Foundation foundation) {
		final DesignSpecs specs = Scene.getInstance().getDesignSpecs();
		int countSolarPanels = 0;
		double cost = 0;
		double panelArea = 0;
		final List<SolarPanel> panels = foundation.getSolarPanels();
		if (!panels.isEmpty()) {
			countSolarPanels += panels.size();
			for (final SolarPanel s : panels) {
				cost += Cost.getInstance().getPartCost(s);
				panelArea += s.getPanelWidth() * s.getPanelHeight();
			}
		}
		final List<Rack> racks = foundation.getRacks();
		if (!racks.isEmpty()) {
			for (final Rack r : racks) {
				countSolarPanels += r.getNumberOfSolarPanels();
				cost += Cost.getInstance().getPartCost(r);
				panelArea += r.getArea();
			}
		}
		countBar.setValue(countSolarPanels);
		countBar.setMinimum(specs.getMinimumNumberOfSolarPanels());
		countBar.setMaximum(specs.getMaximumNumberOfSolarPanels());
		countBar.setEnabled(specs.isNumberOfSolarPanelsEnabled());
		float landArea;
		final FoundationPolygon polygon = foundation.getPolygon();
		if (polygon != null && polygon.isVisible()) {
			landArea = (float) polygon.getArea();
		} else {
			landArea = (float) foundation.getArea();
		}
		landAreaBar.setValue(landArea / countSolarPanels);
		costBar.setValue(Math.round(cost));
		panelAreaBar.setValue((float) panelArea);
	}

	public void updateSolarPanelNumberBounds() {
		final DesignSpecs specs = Scene.getInstance().getDesignSpecs();
		String t = "Number of solar panels";
		if (specs.isNumberOfSolarPanelsEnabled()) {
			t += " (" + specs.getMinimumNumberOfSolarPanels() + " - " + specs.getMaximumNumberOfSolarPanels() + ")";
		}
		countBar.setMinimum(specs.getMinimumNumberOfSolarPanels());
		countBar.setMaximum(specs.getMaximumNumberOfSolarPanels());
		countPanel.setBorder(EnergyPanel.createTitledBorder(t, true));
		countBar.setEnabled(specs.isNumberOfSolarPanelsEnabled());
		countBar.repaint();
	}

}
