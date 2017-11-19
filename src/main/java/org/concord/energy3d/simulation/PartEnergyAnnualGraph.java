package org.concord.energy3d.simulation;

import java.awt.Graphics2D;

/**
 * Graph for annual analysis of energy related to a house part.
 * 
 * @author Charles Xie
 * 
 */
public class PartEnergyAnnualGraph extends AnnualGraph {

	private static final long serialVersionUID = 1L;

	public PartEnergyAnnualGraph() {
		super();
	}

	@Override
	void drawLegends(final Graphics2D g2) {
		drawPartLegends(g2);
	}

	@Override
	void drawCurves(final Graphics2D g2) {
		drawPartCurves(g2);
	}

}
