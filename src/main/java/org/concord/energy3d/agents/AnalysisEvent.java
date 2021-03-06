package org.concord.energy3d.agents;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An analysis event also records the results of the analysis that can be used by the user to review his own work.
 * 
 * @author Charles Xie
 *
 */
public class AnalysisEvent implements NonundoableEvent {

	String name;
	long timestamp;
	URL file;
	Map<String, List<Double>> data;

	public AnalysisEvent(final URL file, final long timestamp, final String name, final Map<String, List<Double>> results) {
		this.file = file;
		this.timestamp = timestamp;
		this.name = name;
		data = Collections.synchronizedMap(new HashMap<String, List<Double>>());
		for (final String x : results.keySet()) {
			data.put(x, new ArrayList<Double>(results.get(x)));
		}
	}

	/** @return true if this is an event related to daily analysis (24 data points) */
	public boolean isDaily() {
		for (final String key : data.keySet()) {
			if (data.get(key).size() > 12) {
				return true;
			}
		}
		return false;
	}

	public Map<String, List<Double>> getResults() {
		return data;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public char getOneLetterCode() {
		if (isDaily()) {
			return 'A';
		}
		return 'Y';
	}

	@Override
	public URL getFile() {
		return file;
	}

}
