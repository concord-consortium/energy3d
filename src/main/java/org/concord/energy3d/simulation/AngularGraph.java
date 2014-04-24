package org.concord.energy3d.simulation;

import java.util.ArrayList;
import java.util.List;

/**
 * Angle graph (360 degrees)
 * 
 * @author Charles Xie
 * 
 */
abstract class AngularGraph extends Graph {

	private static final long serialVersionUID = 1L;

	static List<Results> records;

	static {
		records = new ArrayList<Results>();
	}

	AngularGraph() {
		super();
		xAxisLabel = "Rotation Angle";
		xmin = 0;
		xmax = 7;
		numberOfTicks = AngularAnalysis.nRotation;
	}

	double getXAxisLabelScalingFactor() {
		return Math.toDegrees(2.0 * Math.PI / AngularAnalysis.nRotation);
	}

	String getXAxisUnit() {
		return "\u00B0";
	}

}
