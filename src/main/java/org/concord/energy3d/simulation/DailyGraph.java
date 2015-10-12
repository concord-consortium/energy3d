package org.concord.energy3d.simulation;

import java.util.ArrayList;
import java.util.List;

/**
 * Daily graph (24 hours)
 * 
 * @author Charles Xie
 * 
 */
abstract class DailyGraph extends Graph {

	private static final long serialVersionUID = 1L;

	static List<Results> records;

	static {
		records = new ArrayList<Results>();
	}

	DailyGraph() {
		super();
		xAxisLabel = "Hour";
		xmin = 0;
		xmax = 23;
		numberOfTicks = 24;
	}

	double getXAxisLabelScalingFactor() {
		return 1.0;
	}

	String getXAxisUnit() {
		return "";
	}

}
