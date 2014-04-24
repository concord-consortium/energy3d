package org.concord.energy3d.simulation;

import java.awt.Graphics2D;

/**
 * Graph for angular analysis of energy related to a house part.
 * 
 * @author Charles Xie
 * 
 */
class PartEnergyAngularGraph extends AngularGraph {

	private static final long serialVersionUID = 1L;

	PartEnergyAngularGraph() {
		super();
	}

	void drawLegends(Graphics2D g2) {
		drawPartLegends(g2);
	}

	void drawCurves(Graphics2D g2) {
		drawPartCurves(g2);
	}

}
