package org.concord.energy3d.simulation;

import java.util.ArrayList;
import java.util.List;

/**
 * Annual graph (12 months)
 * 
 * @author Charles Xie
 * 
 */
abstract class AnnualGraph extends Graph {

	private static final long serialVersionUID = 1L;
	private static final String[] THREE_LETTER_MONTH = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

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

	String getXAxisLabel(int i) {
		return THREE_LETTER_MONTH[i];
	}

	double getXAxisLabelScalingFactor() {
		return 1.0;
	}

	String getXAxisUnit() {
		return "";
	}

}
