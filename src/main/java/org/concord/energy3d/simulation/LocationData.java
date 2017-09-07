package org.concord.energy3d.simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public final class LocationData {

	private static final LocationData instance = new LocationData();
	private final Map<String, Float> latitudes = new HashMap<String, Float>();
	private final Map<String, Float> longitudes = new HashMap<String, Float>();
	private final Map<String, Float> altitudes = new HashMap<String, Float>();
	private final Map<String, int[]> lowTemperatures = new HashMap<String, int[]>();
	private final Map<String, int[]> highTemperatures = new HashMap<String, int[]>();
	private final Map<String, int[]> sunshineHours = new HashMap<String, int[]>();
	private final String[] cities;

	public static LocationData getInstance() {
		return instance;
	}

	private LocationData() {

		final Map<String, String> data = new HashMap<String, String>();
		InputStream is = null;
		final int cut = 35;
		try {
			is = getClass().getResourceAsStream("cities/data.txt");
			final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = reader.readLine()) != null) {
				data.put(line.substring(0, cut).trim(), line.substring(cut).trim());
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}

		cities = new String[data.size() + 1];
		cities[0] = "";
		int i = 1;
		for (final String s : data.keySet()) {
			cities[i++] = s;
			final String[] t = data.get(s).split(",");
			longitudes.put(s, Float.parseFloat(t[0].trim()));
			latitudes.put(s, Float.parseFloat(t[1].trim()));
			altitudes.put(s, Float.parseFloat(t[2].trim()));
			final int[] los = new int[12];
			final int[] his = new int[12];
			for (int k = 0; k < 12; k++) {
				los[k] = Integer.parseInt(t[3 + 2 * k].trim());
				his[k] = Integer.parseInt(t[4 + 2 * k].trim());
			}
			lowTemperatures.put(s, los);
			highTemperatures.put(s, his);
			if (t.length > 27) {
				final int[] sunshine = new int[12];
				for (int k = 0; k < 12; k++) {
					sunshine[k] = Integer.parseInt(t[27 + k].trim());
				}
				sunshineHours.put(s, sunshine);
			}
		}

	}

	// The great-circle distance or orthodromic distance is the shortest distance between two points on the surface of a sphere, measured along the surface of the sphere
	// see: https://en.wikipedia.org/wiki/Great-circle_distance
	public String getClosestCity(final double lon, final double lat) {
		double min = Double.MAX_VALUE;
		String city = null;
		double distance;
		for (int i = 1; i < cities.length; i++) {
			final String c = cities[i];
			distance = getDistance(lon, lat, longitudes.get(c), latitudes.get(c));
			if (distance < min) {
				min = distance;
				city = c;
			}
		}
		return city;
	}

	// the spherical law of cosines: https://en.wikipedia.org/wiki/Spherical_law_of_cosines
	private static double getDistance(double lon1, double lat1, double lon2, double lat2) {
		lon1 = Math.toRadians(lon1);
		lat1 = Math.toRadians(lat1);
		lon2 = Math.toRadians(lon2);
		lat2 = Math.toRadians(lat2);
		return Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(Math.abs(lon1 - lon2)));
	}

	public String[] getCities() {
		return cities;
	}

	public Map<String, Float> getLongitudes() {
		return longitudes;
	}

	public Map<String, Float> getLatitudes() {
		return latitudes;
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
