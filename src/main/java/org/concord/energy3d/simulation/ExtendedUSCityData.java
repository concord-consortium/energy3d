package org.concord.energy3d.simulation;

import java.util.HashMap;
import java.util.Map;

public class ExtendedUSCityData {

	private static final ExtendedUSCityData instance = new ExtendedUSCityData();
	private final Map<String, Float> cityLatitutes = new HashMap<String, Float>();
	private final Map<String, Float> cityAltitudes = new HashMap<String, Float>();
	private final Map<String, float[]> avgMonthlyLowTemperatures = new HashMap<String, float[]>();
	private final Map<String, float[]> avgMonthlyHighTemperatures = new HashMap<String, float[]>();

	public static ExtendedUSCityData getInstance() {
		return instance;
	}

	private ExtendedUSCityData() {

		// latitudes
		cityLatitutes.put("Albany, NY", 42.65f);
		cityLatitutes.put("Annapolis, MD", 38.97f);
		cityLatitutes.put("Atlanta, GA", 33.45f);
		cityLatitutes.put("Augusta, ME", 44.32f);
		cityLatitutes.put("Austin, TX", 30.25f);
		cityLatitutes.put("Baton Rouge, LA", 30.45f);
		cityLatitutes.put("Bismarck, ND", 46.81f);
		cityLatitutes.put("Boise, ID", 43.61f);
		cityLatitutes.put("Carson City, NV", 39.16f);
		cityLatitutes.put("Charleston, WV", 32.78f);
		cityLatitutes.put("Cheyenne, WY", 41.14f);
		cityLatitutes.put("Columbia, SC", 34f);
		cityLatitutes.put("Columbus, OH", 39.98f);
		cityLatitutes.put("Concord, NH", 43.2f);
		cityLatitutes.put("Denver, CO", 39.73f);
		cityLatitutes.put("Des Moines, IO", 41.6f);
		cityLatitutes.put("Dover, DE", 39.16f);
		cityLatitutes.put("Frankfort, KE", 38.19f);
		cityLatitutes.put("Harrisburg, PA", 40.26f);
		cityLatitutes.put("Hartford, CT", 41.76f);
		cityLatitutes.put("Helena, MN", 46.59f);
		cityLatitutes.put("Honolulu, HI", 21.3f);
		cityLatitutes.put("Indianapolis, IN", 39.79f);
		cityLatitutes.put("Jackson, MS", 32.31f);
		cityLatitutes.put("Jefferson City, MO", 38.57f);
		cityLatitutes.put("Juneau, AL", 58.3f);
		cityLatitutes.put("Lansing, MI", 42.73f);
		cityLatitutes.put("Lincoln, NE", 40.86f);
		cityLatitutes.put("Little Rock, AR", 34.73f);
		cityLatitutes.put("Madison, WI", 43.06f);
		cityLatitutes.put("Montgomery, AL", 32.36f);
		cityLatitutes.put("Montpelier, VT", 44.25f);
		cityLatitutes.put("Nashville, TN", 36.16f);
		cityLatitutes.put("Oklahoma City, OK", 35.48f);
		cityLatitutes.put("Olympia, WA", 47.04f);
		cityLatitutes.put("Phoenix, AZ", 33.45f);
		cityLatitutes.put("Pierre, SD", 44.36f);
		cityLatitutes.put("Providence, RI", 41.82f);
		cityLatitutes.put("Raleigh, NC", 35.78f);
		cityLatitutes.put("Richmond, VA", 35.53f);
		cityLatitutes.put("Sacramento, CA", 38.55f);
		cityLatitutes.put("Salem, OR", 44.93f);
		cityLatitutes.put("Salt Lake City, UT", 40.75f);
		cityLatitutes.put("Santa Fe, NM", 35.66f);
		cityLatitutes.put("Springfield, IL", 39.78f);
		cityLatitutes.put("St. Paul, MN", 44.94f);
		cityLatitutes.put("Tallahassee, FL", 30.45f);
		cityLatitutes.put("Topeka, KS", 39.05f);
		cityLatitutes.put("Trenton, NJ", 40.22f);

		// altitudes
		cityAltitudes.put("Albany, NY", 42.65f);
		cityAltitudes.put("Annapolis, MD", 38.97f);
		cityAltitudes.put("Atlanta, GA", 33.45f);
		cityAltitudes.put("Augusta, ME", 44.32f);
		cityAltitudes.put("Austin, TX", 30.25f);
		cityAltitudes.put("Baton Rouge, LA", 30.45f);
		cityAltitudes.put("Bismarck, ND", 46.81f);
		cityAltitudes.put("Boise, ID", 43.61f);
		cityAltitudes.put("Carson City, NV", 39.16f);
		cityAltitudes.put("Charleston, WV", 32.78f);
		cityAltitudes.put("Cheyenne, WY", 41.14f);
		cityAltitudes.put("Columbia, SC", 34f);
		cityAltitudes.put("Columbus, OH", 39.98f);
		cityAltitudes.put("Concord, NH", 43.2f);
		cityAltitudes.put("Denver, CO", 39.73f);
		cityAltitudes.put("Des Moines, IO", 41.6f);
		cityAltitudes.put("Dover, DE", 39.16f);
		cityAltitudes.put("Frankfort, KE", 38.19f);
		cityAltitudes.put("Harrisburg, PA", 40.26f);
		cityAltitudes.put("Hartford, CT", 41.76f);
		cityAltitudes.put("Helena, MN", 46.59f);
		cityAltitudes.put("Honolulu, HI", 21.3f);
		cityAltitudes.put("Indianapolis, IN", 39.79f);
		cityAltitudes.put("Jackson, MS", 32.31f);
		cityAltitudes.put("Jefferson City, MO", 38.57f);
		cityAltitudes.put("Juneau, AL", 58.3f);
		cityAltitudes.put("Lansing, MI", 42.73f);
		cityAltitudes.put("Lincoln, NE", 40.86f);
		cityAltitudes.put("Little Rock, AR", 34.73f);
		cityAltitudes.put("Madison, WI", 43.06f);
		cityAltitudes.put("Montgomery, AL", 32.36f);
		cityAltitudes.put("Montpelier, VT", 44.25f);
		cityAltitudes.put("Nashville, TN", 36.16f);
		cityAltitudes.put("Oklahoma City, OK", 35.48f);
		cityAltitudes.put("Olympia, WA", 47.04f);
		cityAltitudes.put("Phoenix, AZ", 33.45f);
		cityAltitudes.put("Pierre, SD", 44.36f);
		cityAltitudes.put("Providence, RI", 41.82f);
		cityAltitudes.put("Raleigh, NC", 35.78f);
		cityAltitudes.put("Richmond, VA", 35.53f);
		cityAltitudes.put("Sacramento, CA", 38.55f);
		cityAltitudes.put("Salem, OR", 44.93f);
		cityAltitudes.put("Salt Lake City, UT", 40.75f);
		cityAltitudes.put("Santa Fe, NM", 35.66f);
		cityAltitudes.put("Springfield, IL", 39.78f);
		cityAltitudes.put("St. Paul, MN", 44.94f);
		cityAltitudes.put("Tallahassee, FL", 30.45f);
		cityAltitudes.put("Topeka, KS", 39.05f);
		cityAltitudes.put("Trenton, NJ", 40.22f);

		// low temperatures
		avgMonthlyLowTemperatures.put("Albany, NY", new float[] { -6, -4, -1, 5, 10, 16, 18, 18, 14, 8, 3, -2 });

		// high temperatures
		avgMonthlyHighTemperatures.put("Albany, NY", new float[] { 2, 4, 7, 13, 19, 24, 28, 27, 22, 16, 11, 5 });

	}

}
