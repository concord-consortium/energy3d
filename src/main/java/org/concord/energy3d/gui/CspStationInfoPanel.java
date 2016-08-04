package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.simulation.Cost;

/**
 * @author Charles Xie
 *
 */
public class CspStationInfoPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final DecimalFormat noDecimals = new DecimalFormat();
	private JPanel countPanel, costPanel, reflectingAreaPanel;
	private ColorBar countBar, costBar, reflectingAreaBar;

	public CspStationInfoPanel() {

		super(new BorderLayout());

		noDecimals.setMaximumFractionDigits(0);

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		add(container, BorderLayout.NORTH);

		// mirror count on the selected base

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

		// total solar panel area on the selected base

		reflectingAreaPanel = new JPanel(new BorderLayout());
		reflectingAreaPanel.setBorder(EnergyPanel.createTitledBorder("<html>Total reflecting area (m<sup>2</sup>)</html>", true));
		container.add(reflectingAreaPanel);
		reflectingAreaBar = new ColorBar(Color.WHITE, Color.LIGHT_GRAY);
		reflectingAreaBar.setUnit("");
		reflectingAreaBar.setVerticalLineRepresentation(false);
		reflectingAreaBar.setDecimalDigits(2);
		reflectingAreaBar.setToolTipText(reflectingAreaPanel.getToolTipText());
		reflectingAreaBar.setPreferredSize(new Dimension(100, 16));
		reflectingAreaPanel.add(reflectingAreaBar, BorderLayout.CENTER);

		// mirror cost on the selected base

		costPanel = new JPanel(new BorderLayout());
		costPanel.setBorder(EnergyPanel.createTitledBorder("Cost of mirrors", true));
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

	void update(Foundation foundation) {
		List<Mirror> mirrors = foundation.getMirrors();
		countBar.setValue(mirrors.size());
		double cost = 0;
		double reflectingArea = 0;
		for (Mirror m : mirrors) {
			cost += Cost.getInstance().getPartCost(m);
			reflectingArea += m.getMirrorWidth() * m.getMirrorHeight();
		}
		costBar.setValue(Math.round(cost));
		reflectingAreaBar.setValue((float) reflectingArea);
	}

}
