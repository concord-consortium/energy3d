package org.concord.energy3d.simulation;

import java.util.Collections;
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

	private static int n = 1;
	private int id;
	private Map<String, List<Double>> copy;

	Results(Map<String, List<Double>> data) {
		id = n;
		copy = new HashMap<String, List<Double>>(data);
		n++;
	}

	int getID() {
		return id;
	}

	Map<String, List<Double>> getData() {
		return copy;
	}

	double[] getBound() {
		double[] bound = new double[2];
		for (String key : copy.keySet()) {
			List<Double> list = copy.get(key);
			if (!list.isEmpty()) {
				double max = Collections.max(list);
				double min = Collections.min(list);
				if (min < bound[0])
					bound[0] = min;
				if (max > bound[1])
					bound[1] = max;
			}
		}
		return bound;
	}

}
