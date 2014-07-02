package org.concord.energy3d.simulation;

import java.util.HashMap;
import java.util.Map;

public class ExtendedWorldCityData {

	private static final ExtendedWorldCityData instance = new ExtendedWorldCityData();
	private final Map<String, Float> cityLatitutes = new HashMap<String, Float>();
	private final Map<String, Float> cityAltitudes = new HashMap<String, Float>();
	private final Map<String, float[]> avgMonthlyLowAndHighs = new HashMap<String, float[]>();

	public static ExtendedWorldCityData getInstance() {
		return instance;
	}

	private ExtendedWorldCityData() {

		// latitudes
		cityLatitutes.put("New York", 40.71f);

		// altitudes
		cityAltitudes.put("New York", 9.75f);

		// high temperatures
		avgMonthlyLowAndHighs.put("New York", new float[] { -3, 4, -4, 5, 1, 9, 6, 16, 12, 20, 17, 27, 18, 28, 19, 27, 16, 25, 10, 19, 5, 12, -1, 5 });

		// for (String key : avgMonthlyLowAndHighs.keySet())
		// System.out.println(key + "********************" + ((float[]) avgMonthlyLowAndHighs.get(key)).length);

	}

}
