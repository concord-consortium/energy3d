package org.concord.energy3d.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.simulation.Cost;

/**
 * @author Charles Xie
 *
 */
public class PvStationInfoPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final DecimalFormat noDecimals = new DecimalFormat();
	private JPanel countPanel, costPanel;
	private ColorBar countBar, costBar;

	public PvStationInfoPanel() {

		super(new BorderLayout());

		noDecimals.setMaximumFractionDigits(0);

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		add(container, BorderLayout.NORTH);

		// solar panel count for the selected base

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

		// solar panel cost for the selected base

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

	void update(Foundation foundation) {
		List<SolarPanel> panels = foundation.getSolarPanels();
		countBar.setValue(panels.size());
		double cost = 0;
		for (SolarPanel s : panels) {
			cost += Cost.getInstance().getPartCost(s);
		}
		costBar.setValue(Math.round(cost));
	}

}
