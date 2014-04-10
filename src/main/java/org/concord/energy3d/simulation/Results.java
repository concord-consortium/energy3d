package org.concord.energy3d.simulation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This stores the results of a run with an ID, in a copied List.
 * 
 * @author Charles
 * 
 */
class Results {

	private static int id = -1;
	private Map<String, List<Double>> copy;

	Results(Map<String, List<Double>> data) {
		id++;
		copy = new HashMap<String, List<Double>>(data);
	}

	static int getID() {
		return id;
	}

	Map<String, List<Double>> getData() {
		return copy;
	}

}
