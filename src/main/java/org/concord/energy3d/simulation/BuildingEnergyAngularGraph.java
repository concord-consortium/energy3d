package org.concord.energy3d.simulation;

import java.awt.Graphics2D;

/**
 * Graph for angular analysis of building energy.
 * 
 * @author Charles Xie
 * 
 */
class BuildingEnergyAngularGraph extends AngularGraph {

	private static final long serialVersionUID = 1L;

	BuildingEnergyAngularGraph() {
		super();
	}

	void drawLegends(Graphics2D g2) {
		drawBuildingLegends(g2);
	}

	void drawCurves(Graphics2D g2) {
		drawBuildingCurves(g2);
	}

}
