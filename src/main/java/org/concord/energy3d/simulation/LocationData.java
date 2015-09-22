package org.concord.energy3d.simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public final class LocationData {

	private static final LocationData instance = new LocationData();
	private final Map<String, Float> latitutes = new HashMap<String, Float>();
	private final Map<String, Float> altitudes = new HashMap<String, Float>();
	private final Map<String, int[]> lowTemperatures = new HashMap<String, int[]>();
	private final Map<String, int[]> highTemperatures = new HashMap<String, int[]>();
	private final Map<String, int[]> sunshineHours = new HashMap<String, int[]>();
	private final String[] cities;

	public static LocationData getInstance() {
		return instance;
	}

	private LocationData() {

		Map<String, String> data = new HashMap<String, String>();
		InputStream is = null;
		final int cut = 29;
		try {
			is = getClass().getResourceAsStream("cities/data.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = reader.readLine()) != null) {
				data.put(line.substring(0, cut).trim(), line.substring(cut).trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		cities = new String[data.size() + 1];
		cities[0] = "";
		int i = 1;
		for (String s : data.keySet()) {
			cities[i++] = s;
			String[] t = data.get(s).split(",");
			latitutes.put(s, Float.parseFloat(t[0].trim()));
			altitudes.put(s, Float.parseFloat(t[1].trim()));
			int[] los = new int[12];
			int[] his = new int[12];
			for (int k = 0; k < 12; k++) {
				los[k] = Integer.parseInt(t[2 + 2 * k].trim());
				his[k] = Integer.parseInt(t[3 + 2 * k].trim());
			}
			lowTemperatures.put(s, los);
			highTemperatures.put(s, his);
			if (t.length > 26) {
				int[] sunshine = new int[12];
				for (int k = 0; k < 12; k++) {
					sunshine[k] = Integer.parseInt(t[26 + k].trim());
				}
				sunshineHours.put(s, sunshine);
			}
		}

	}

	public String[] getCities() {
		return cities;
	}

	public Map<String, Float> getLatitutes() {
		return latitutes;
	}

	public Map<String, Float> getAltitudes() {
		return altitudes;
	}

	public Map<String, int[]> getSunshineHours() {
		return sunshineHours;
	}

	public Map<String, int[]> getLowTemperatures() {
		return lowTemperatures;
	}

	public Map<String, int[]> getHighTemperatures() {
		return highTemperatures;
	}

}
