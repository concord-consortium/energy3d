package org.concord.energy3d.simulation;

import java.awt.Graphics2D;

/**
 * Graph for annual analysis of building energy.
 * 
 * @author Charles Xie
 * 
 */
public class BuildingEnergyAnnualGraph extends AnnualGraph {

	private static final long serialVersionUID = 1L;

	public BuildingEnergyAnnualGraph() {
		super();
	}

	@Override
	void drawLegends(final Graphics2D g2) {
		drawBuildingLegends(g2);
	}

	@Override
	void drawCurves(final Graphics2D g2) {
		drawBuildingCurves(g2);
	}

}
