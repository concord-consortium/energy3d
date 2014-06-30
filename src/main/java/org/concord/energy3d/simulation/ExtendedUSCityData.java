package org.concord.energy3d.simulation;

import java.util.HashMap;
import java.util.Map;

public class ExtendedUSCityData {

	private static final ExtendedUSCityData instance = new ExtendedUSCityData();
	private final Map<String, Float> cityLatitutes = new HashMap<String, Float>();
	private final Map<String, Float> cityAltitudes = new HashMap<String, Float>();
	private final Map<String, float[]> avgMonthlyLowAndHighs = new HashMap<String, float[]>();

	public static ExtendedUSCityData getInstance() {
		return instance;
	}

	private ExtendedUSCityData() {

		// latitudes
		cityLatitutes.put("Albany, NY", 42.65f);
		cityLatitutes.put("Annapolis, MD", 38.97f);
		cityLatitutes.put("Atlanta, GA", 33.75f);
		cityLatitutes.put("Augusta, ME", 44.32f);
		cityLatitutes.put("Austin, TX", 30.26f);
		cityLatitutes.put("Baton Rouge, LA", 30.45f);
		cityLatitutes.put("Bismarck, ND", 46.81f);
		cityLatitutes.put("Boise, ID", 43.61f);
		cityLatitutes.put("Carson City, NV", 39.16f);
		cityLatitutes.put("Charleston, WV", 38.34f);
		cityLatitutes.put("Cheyenne, WY", 41.13f);
		cityLatitutes.put("Columbia, SC", 34f);
		cityLatitutes.put("Columbus, OH", 39.95f);
		cityLatitutes.put("Concord, NH", 43.2f);
		cityLatitutes.put("Denver, CO", 39.73f);
		cityLatitutes.put("Des Moines, IO", 41.59f);
		cityLatitutes.put("Dover, DE", 39.15f);
		cityLatitutes.put("Frankfort, KE", 38.2f);
		cityLatitutes.put("Harrisburg, PA", 40.27f);
		cityLatitutes.put("Hartford, CT", 41.76f);
		cityLatitutes.put("Helena, MN", 46.59f);
		cityLatitutes.put("Honolulu, HI", 21.3f);
		cityLatitutes.put("Indianapolis, IN", 39.76f);
		cityLatitutes.put("Jackson, MS", 32.29f);
		cityLatitutes.put("Jefferson City, MO", 38.57f);
		cityLatitutes.put("Juneau, AK", 58.32f);
		cityLatitutes.put("Lansing, MI", 42.73f);
		cityLatitutes.put("Lincoln, NE", 40.8f);
		cityLatitutes.put("Little Rock, AR", 34.74f);
		cityLatitutes.put("Madison, WI", 43.07f);
		cityLatitutes.put("Montgomery, AL", 32.36f);
		cityLatitutes.put("Montpelier, VT", 44.25f);
		cityLatitutes.put("Nashville, TN", 36.16f);
		cityLatitutes.put("Oklahoma City, OK", 35.46f);
		cityLatitutes.put("Olympia, WA", 47.03f);
		cityLatitutes.put("Phoenix, AZ", 33.44f);
		cityLatitutes.put("Pierre, SD", 44.36f);
		cityLatitutes.put("Providence, RI", 41.82f);
		cityLatitutes.put("Raleigh, NC", 35.77f);
		cityLatitutes.put("Richmond, VA", 37.54f);
		cityLatitutes.put("Sacramento, CA", 38.58f);
		cityLatitutes.put("Salem, OR", 44.94f);
		cityLatitutes.put("Salt Lake City, UT", 40.76f);
		cityLatitutes.put("Santa Fe, NM", 35.68f);
		cityLatitutes.put("Springfield, IL", 39.78f);
		cityLatitutes.put("St. Paul, MN", 44.95f);
		cityLatitutes.put("Tallahassee, FL", 30.43f);
		cityLatitutes.put("Topeka, KS", 39.05f);
		cityLatitutes.put("Trenton, NJ", 40.22f);

		// altitudes
		cityAltitudes.put("Albany, NY", 115f);
		cityAltitudes.put("Annapolis, MD", 12f);
		cityAltitudes.put("Atlanta, Ga", 320f);
		cityAltitudes.put("Augusta, ME", 45f);
		cityAltitudes.put("Austin, TX", 130f);
		cityAltitudes.put("Baton Rouge, LA", 17f);
		cityAltitudes.put("Bismarck, ND", 514f);
		cityAltitudes.put("Boise, ID", 824f);
		cityAltitudes.put("Carson City, NV", 1463f);
		cityAltitudes.put("Charleston, WV", 182f);
		cityAltitudes.put("Cheyenne, WY", 1848f);
		cityAltitudes.put("Columbia, SC", 89f);
		cityAltitudes.put("Columbus, OH", 275f);
		cityAltitudes.put("Concord, NH", 88f);
		cityAltitudes.put("Denver, CO", 1609f);
		cityAltitudes.put("Des Moines, IA", 294f);
		cityAltitudes.put("Dover, DE", 11f);
		cityAltitudes.put("Frankfort, KY", 245f);
		cityAltitudes.put("Harrisburg, PA", 98f);
		cityAltitudes.put("Hartford, CT", 18f);
		cityAltitudes.put("Helena, MT", 1237f);
		cityAltitudes.put("Honolulu, HI", 4f);
		cityAltitudes.put("Indianapolis, IN", 218f);
		cityAltitudes.put("Jackson, MS", 100f);
		cityAltitudes.put("Jefferson City, MO", 192f);
		cityAltitudes.put("Juneau, AK", 17f);
		cityAltitudes.put("Lansing, MI", 260f);
		cityAltitudes.put("Lincoln, NE", 358f);
		cityAltitudes.put("Little Rock, AR", 152f);
		cityAltitudes.put("Madison, WI", 263f);
		cityAltitudes.put("Montgomery, AL", 73f);
		cityAltitudes.put("Montpelier, VT", 158f);
		cityAltitudes.put("Nashville, TN", 180f);
		cityAltitudes.put("Oklahoma City, OK", 397f);
		cityAltitudes.put("Olympia, WA", 29f);
		cityAltitudes.put("Phoenix, AZ", 340f);
		cityAltitudes.put("Pierre, SD", 442f);
		cityAltitudes.put("Providence, RI", 23f);
		cityAltitudes.put("Raleigh, NC", 103f);
		cityAltitudes.put("Richmond, VA", 57.9f);
		cityAltitudes.put("Sacramento, CA", 9f);
		cityAltitudes.put("Salem, OR", 46.7f);
		cityAltitudes.put("Salt Lake City, UT", 1320f);
		cityAltitudes.put("Santa Fe, NM", 2133.6f);
		cityAltitudes.put("Springfield, IL", 178.6f);
		cityAltitudes.put("St. Paul, MN", 264f);
		cityAltitudes.put("Tallahassee, FL", 57f);
		cityAltitudes.put("Topeka, KS", 288f);
		cityAltitudes.put("Trenton, NJ", 15f);

		// high temperatures
		avgMonthlyLowAndHighs.put("Albany, NY", new float[] { -9, -1, -8, 2 - 3, 7, 3, 14, 8, 21, 14, 26, 17, 28, 16, 27, 11, 22, 16, 4, 0, 9, -6, 2 });
		avgMonthlyLowAndHighs.put("Annapolis, MD", new float[] { -2, 7, -1, 9, 3, 14, 9, 20, 14, 25, 19, 30, 22, 32, 22, 31, 18, 27, 11, 21, 6, 15, 1, 9 });
		avgMonthlyLowAndHighs.put("Atlanta, Ga", new float[] { 1, 11, 3, 14, 7, 18, 11, 23, 16, 27, 20, 30, 22, 32, 22, 31, 18, 28, 12, 23, 7, 18, 3, 12 });
		avgMonthlyLowAndHighs.put("Augusta, ME", new float[] { -12, -2 - 9, 0, -5, 4, 2, 12, 7, 18, 12, 23, 16, 26, 15, 26, 11, 21, 4, 14, -1, 7, -7, 1 });
		avgMonthlyLowAndHighs.put("Austin, TX", new float[] { 5, 17, 7, 18, 11, 22, 15, 27, 19, 31, 22, 33, 23, 36, 24, 36, 21, 33, 16, 28, 11, 22, 6, 17 });
		avgMonthlyLowAndHighs.put("Baton Rouge, LA", new float[] { 4, 16, 6, 18, 9, 22, 13, 26, 18, 29, 21, 32, 23, 33, 22, 33, 19, 31, 14, 27, 9, 22, 6, 17 });
		avgMonthlyLowAndHighs.put("Bismarck, ND", new float[] { -17, -5, -13, -2 - 7, 4, -1, 14, 6, 20, 11, 25, 14, 29, 13, 29, 7, 22, 0, 14, -7, 4, -14, -3 });
		avgMonthlyLowAndHighs.put("Boise, ID", new float[] { -4, 3, -2, 7, 1, 13, 4, 17, 8, 22, 12, 27, 16, 33, 16, 32, 11, 26, 5, 18, 0, 9, -4, 3 });
		avgMonthlyLowAndHighs.put("Carson City, NV", new float[] { -11, -1, -9, 1, -6, 7, 1, 15, 6, 21, 12, 27, 14, 29, 13, 27, 9, 23, 3, 16, -2, 8, -8, 1 });
		avgMonthlyLowAndHighs.put("Charleston, WV", new float[] { -3, 6, -2, 8, 2, 13, 7, 20, 12, 24, 17, 28, 19, 29, 18, 29, 14, 26, 7, 20, 3, 14, -2, 8 });
		avgMonthlyLowAndHighs.put("Cheyenne, WY", new float[] { -8, 4, -7, 5, -4, 9, -1, 13, 4, 18, 9, 24, 13, 28, 12, 27, 7, 22, 1, 15, -4, 8, -8, 3 });
		avgMonthlyLowAndHighs.put("Columbia, SC", new float[] { 3, 14, 5, 17, 8, 22, 12, 27, 17, 31, 21, 33, 23, 35, 22, 34, 19, 31, 13, 26, 8, 21, 4, 16 });
		avgMonthlyLowAndHighs.put("Columbus, OH", new float[] { -7, 3, -6, 6, -2, 11, 4, 18, 10, 24, 16, 28, 18, 30, 16, 29, 12, 26, 5, 19, 0, 12, -5, 6 });
		avgMonthlyLowAndHighs.put("Concord, NH", new float[] { -12, -1, -10, 2, -5, 7, 1, 14, 6, 21, 12, 25, 14, 28, 13, 27, 8, 23, 2, 16, -2, 9, -8, 2 });
		avgMonthlyLowAndHighs.put("Denver, CO", new float[] { -9, 8, -8, 9, -4, 13, 1, 17, 6, 22, 10, 27, 13, 31, 12, 30, 7, 26, 1, 19, -5, 12, -9, 8 });
		avgMonthlyLowAndHighs.put("Des Moines, IA", new float[] { -10, -1 - 7, 2, -1, 9, 5, 17, 11, 22, 17, 28, 19, 30, 18, 29, 13, 24, 6, 17, -1, 9, -8, 1 });
		avgMonthlyLowAndHighs.put("Dover, DE", new float[] { -3, 5, -2, 7, 2, 11, 7, 17, 12, 22, 17, 27, 21, 29, 19, 28, 16, 25, 9, 19, 4, 13, -1, 7 });
		avgMonthlyLowAndHighs.put("Frankfort, KY", new float[] { -6, 5, -4, 8, -1, 13, 5, 19, 10, 24, 16, 29, 18, 31, 17, 31, 13, 27, 6, 21, 1, 14, -3, 7 });
		avgMonthlyLowAndHighs.put("Harrisburg, PA", new float[] { -5, 3, -4, 5, 0, 11, 6, 17, 11, 22, 16, 27, 19, 29, 18, 29, 13, 24, 7, 18, 2, 12, -3, 6 });
		avgMonthlyLowAndHighs.put("Hartford, CT", new float[] { -8, 2, -6, 4, -2, 9, 4, 16, 9, 21, 15, 26, 18, 29, 17, 28, 12, 24, 6, 17, 2, 11, -4, 5 });
		avgMonthlyLowAndHighs.put("Helena, MT", new float[] { -9, 2, -7, 4, -1, 11, 5, 18, 11, 23, 17, 28, 19, 31, 18, 30, 13, 26, 6, 19, 0, 11, -7, 3 });
		avgMonthlyLowAndHighs.put("Honolulu, HI", new float[] { 18, 27, 18, 27, 19, 28, 20, 28, 21, 29, 22, 31, 23, 31, 23, 31, 23, 31, 22, 31, 21, 29, 19, 28 });
		avgMonthlyLowAndHighs.put("Indianapolis, IN", new float[] { -7, 1, -6, 4, -1, 9, 5, 17, 11, 22, 16, 27, 18, 29, 17, 28, 13, 24, 6, 18, 1, 11, -5, 3 });
		avgMonthlyLowAndHighs.put("Jackson, MS", new float[] { 2, 13, 4, 16, 7, 21, 11, 24, 17, 28, 21, 32, 22, 33, 22, 33, 18, 31, 12, 25, 7, 9, 3, 14 });
		avgMonthlyLowAndHighs.put("Jefferson City, MO", new float[] { -6, 4, -4, 8, 1, 13, 7, 19, 12, 24, 18, 29, 20, 31, 19, 31, 14, 27, 7, 21, 2, 13, -4, 6 });
		avgMonthlyLowAndHighs.put("Juneau, AK", new float[] { -4, 1, -4, 2, -2, 4, 1, 9, 5, 14, 8, 17, 10, 18, 9, 17, 7, 13, 3, 8, -2, 3, -3, 1 });
		avgMonthlyLowAndHighs.put("Lansing, MI", new float[] { -8, -1, -7, 1, -3, 7, 3, 14, 8, 21, 14, 26, 16, 28, 15, 27, 11, 23, 5, 16, 0, 8, -6, 1 });
		avgMonthlyLowAndHighs.put("Lincoln, NE", new float[] { -10, 2, -8, 4, -2, 11, 4, 18, 10, 23, 16, 29, 19, 32, 18, 30, 12, 26, 5, 19, -2, 11, -8, 3 });
		avgMonthlyLowAndHighs.put("Little Rock, AR", new float[] { -1, 11, 2, 13, 6, 18, 11, 23, 16, 27, 21, 32, 23, 34, 22, 34, 18, 30, 12, 24, 6, 17, 1, 11 });
		avgMonthlyLowAndHighs.put("Madison, WI", new float[] { -12, -2, -11, 0, -4, 7, 2, 14, 8, 21, 13, 26, 16, 28, 14, 27, 9, 23, 3, 16, -2, 8, -9, 0 });
		avgMonthlyLowAndHighs.put("Montgomery, AL", new float[] { 4, 14, 5, 17, 9, 21, 13, 25, 17, 29, 21, 32, 23, 33, 23, 33, 19, 31, 13, 26, 8, 19, 5, 16 });
		avgMonthlyLowAndHighs.put("Montpelier, VT", new float[] { -14, -3, -12, -1 - 7, 4, 0, 12, 6, 19, 11, 23, 13, 26, 12, 25, 8, 21, 2, 13, -3, 7, -10, 0 });
		avgMonthlyLowAndHighs.put("Nashville, TN", new float[] { -2, 8, 0, 11, 4, 16, 9, 22, 14, 26, 18, 30, 21, 32, 20, 32, 16, 28, 9, 22, 4, 16, -1, 10 });
		avgMonthlyLowAndHighs.put("Oklahoma City, OK", new float[] { -2, 10, 1, 13, 5, 17, 10, 22, 16, 27, 20, 31, 22, 34, 22, 34, 17, 29, 11, 23, 4, 17, -1, 11 });
		avgMonthlyLowAndHighs.put("Olympia, WA", new float[] { 1, 8, 1, 9, 2, 12, 3, 15, 6, 18, 9, 22, 11, 25, 11, 26, 8, 22, 5, 16, 2, 10, 1, 7 });
		avgMonthlyLowAndHighs.put("Phoenix, AZ", new float[] { 8, 19, 9, 22, 12, 25, 16, 29, 21, 35, 26, 40, 29, 41, 28, 41, 25, 38, 18, 32, 12, 24, 7, 19 });
		avgMonthlyLowAndHighs.put("Pierre, SD", new float[] { -12, -1, -10, 2, -4, 7, 1, 16, 8, 21, 13, 27, 17, 32, 16, 31, 9, 24, 2, 16, -5, 7, -11, -1 });
		avgMonthlyLowAndHighs.put("Providence, RI", new float[] { -6, 3, -4, 4, -1, 9, 4, 15, 9, 20, 14, 26, 18, 28, 17, 27, 13, 23, 7, 17, 2, 12, -3, 6 });
		avgMonthlyLowAndHighs.put("Raleigh, NC", new float[] { -1, 11, 0, 13, 4, 17, 9, 22, 13, 27, 19, 31, 21, 32, 20, 32, 16, 28, 10, 23, 5, 17, 1, 12 });
		avgMonthlyLowAndHighs.put("Richmond, VA", new float[] { -2, 8, -1, 11, 3, 16, 8, 21, 13, 26, 18, 30, 21, 32, 19, 31, 16, 27, 9, 22, 4, 16, -1, 11 });
		avgMonthlyLowAndHighs.put("Sacramento, CA", new float[] { 5, 12, 7, 16, 8, 19, 9, 23, 12, 27, 14, 31 });
		avgMonthlyLowAndHighs.put("Salem, OR", new float[] { 2, 9, 2, 11, 3, 14, 4, 16, 7, 20, 9, 23, 12, 28, 12, 28, 9, 25, 6, 18, 3, 13, 1, 8 });
		avgMonthlyLowAndHighs.put("Salt Lake City, UT", new float[] { -3, 3, -1, 7, 3, 12, 6, 16, 11, 22, 16, 22, 21, 32, 19, 32, 14, 26, 8, 18, 2, 10 - 3, 4 });
		avgMonthlyLowAndHighs.put("Santa Fe, NM", new float[] { -8, 7, -6, 9, -3, 13, 0, 18, 5, 23, 9, 29, 13, 30, 12, 28, 8, 26, 2, 19, -4, 12, -8, 6 });
		avgMonthlyLowAndHighs.put("Springfield, IL", new float[] { -7, 2, -5, 4, 0, 11, 6, 18, 12, 24, 17, 28, 18, 30, 18, 29, 13, 26, 7, 19, 1, 11, -5, 3 });
		avgMonthlyLowAndHighs.put("St. Paul, MN", new float[] { -14, -4, -11, -2 - 4, 5, 2, 14, 9, 21, 14, 26, 17, 28, 16, 27, 11, 22, 4, 14, -3, 5, -11, -3 });
		avgMonthlyLowAndHighs.put("Tallahassee, FL", new float[] { 4, 17, 6, 20, 8, 23, 11, 27, 17, 31, 21, 33, 22, 33, 22, 33, 20, 31, 14, 27, 9, 23, 5, 18 });
		avgMonthlyLowAndHighs.put("Topeka, KS", new float[] { -7, 4, -4, 7, 1, 13, 7, 19, 12, 24, 18, 29, 21, 32, 19, 32, 13, 27, 7, 20, 1, 13, -6, 6 });
		avgMonthlyLowAndHighs.put("Trenton, NJ", new float[] { -6, 4, -4, 6, -1, 11, 4, 17, 9, 22, 15, 27, 18, 30, 17, 29, 12, 25, 6, 19, 2, 13, -3, 7 });

	}

}
