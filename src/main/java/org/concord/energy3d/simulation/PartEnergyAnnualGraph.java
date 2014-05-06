package org.concord.energy3d.simulation;

import java.awt.Graphics2D;

/**
 * Graph for annual analysis of energy related to a house part.
 * 
 * @author Charles Xie
 * 
 */
class PartEnergyAnnualGraph extends AnnualGraph {

	private static final long serialVersionUID = 1L;

	PartEnergyAnnualGraph() {
		super();
	}

	void drawLegends(Graphics2D g2) {
		drawPartLegends(g2);
	}

	void drawCurves(Graphics2D g2) {
		drawPartCurves(g2);
	}

}
