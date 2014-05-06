package org.concord.energy3d.simulation;

import java.util.ArrayList;
import java.util.List;

/**
 * Seasonal graph (12 months)
 * 
 * @author Charles Xie
 * 
 */
abstract class AnnualGraph extends Graph {

	private static final long serialVersionUID = 1L;

	static List<Results> records;

	static {
		records = new ArrayList<Results>();
	}

	AnnualGraph() {
		super();
		xAxisLabel = "Month";
		xmin = 0;
		xmax = 11;
		numberOfTicks = 12;
	}

	double getXAxisLabelScalingFactor() {
		return 1.0;
	}

	String getXAxisUnit() {
		return "";
	}

}
