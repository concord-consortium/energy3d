package org.concord.energy3d.simulation;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.shapes.Heliodon;

public class CityData {
	private static final CityData instance = new CityData();
	private final Map<String, Integer> cityLatitutes = new HashMap<String, Integer>();
	private final Map<String, Integer> cityAltitudes = new HashMap<String, Integer>();
	private final Map<String, int[]> avgMonthlyLowTemperatures = new HashMap<String, int[]>();
	private final Map<String, int[]> avgMonthlyHighTemperatures = new HashMap<String, int[]>();
	private final String[] cities;

	public static CityData getInstance() {
		return instance;
	}

	private CityData() {

		cities = new String[] {"", "Moscow", "Ottawa", "Boston", "Beijing", "Washington DC", "Tehran", "Los Angeles", "Miami", "Mexico City", "Singapore", "Sydney", "Buenos Aires"};

		// latitudes
		cityLatitutes.put("Moscow", 55);
		cityLatitutes.put("Ottawa", 45);
		cityLatitutes.put("Boston", 42);
		cityLatitutes.put("Beijing", 39);
		cityLatitutes.put("Washington DC", 38);
		cityLatitutes.put("Tehran", 35);
		cityLatitutes.put("Los Angeles", 34);
		cityLatitutes.put("Miami", 25);
		cityLatitutes.put("Mexico City", 19);
		cityLatitutes.put("Singapore", 1);
		cityLatitutes.put("Sydney", -33);
		cityLatitutes.put("Buenos Aires", -34);

		// altitudes
		cityAltitudes.put("Moscow", 151);
		cityAltitudes.put("Ottawa", 114);
		cityAltitudes.put("Boston", 2);
		cityAltitudes.put("Beijing", 44);
		cityAltitudes.put("Washington DC", 2);
		cityAltitudes.put("Tehran", 1189);
		cityAltitudes.put("Los Angeles", 71);
		cityAltitudes.put("Miami", 2);
		cityAltitudes.put("Mexico City", 2421);
		cityAltitudes.put("Singapore", 2);
		cityAltitudes.put("Sydney", 2);
		cityAltitudes.put("Buenos Aires", 25);
		
		// low and high temperatures
		avgMonthlyLowTemperatures.put("Boston", new int[] { -6, -4, -1, 5, 10, 16, 18, 18, 14, 8, 3, -2 });
		avgMonthlyHighTemperatures.put("Boston", new int[] { 2, 4, 7, 13, 19, 24, 28, 27, 22, 16, 11, 5 });
		avgMonthlyLowTemperatures.put("Moscow", new int[] { -14, -14, -9, 0, 6, 10, 13, 11, 6, 1, -5, -10 });
		avgMonthlyHighTemperatures.put("Moscow", new int[] { -7, -6, 0, 9, 17, 22, 24, 22, 16, 8, 0, -5 });
		avgMonthlyLowTemperatures.put("Ottawa", new int[] { -16, -14, -7, 1, 7, 12, 15, 14, 9, 3, -2, -11 });
		avgMonthlyHighTemperatures.put("Ottawa", new int[] { -7, -5, 2, 11, 18, 23, 26, 24, 19, 13, 4, -4 });
		avgMonthlyLowTemperatures.put("Beijing", new int[] { -9, -7, -1, 7, 13, 18, 21, 20, 14, 7, -1, -7 });
		avgMonthlyHighTemperatures.put("Beijing", new int[] { 1, 4, 11, 19, 26, 30, 31, 29, 26, 19, 10, 3 });
		avgMonthlyLowTemperatures.put("Washington DC", new int[] { -2, -1, 3, 8, 13, 19, 22, 21, 17, 11, 5, 1 });
		avgMonthlyHighTemperatures.put("Washington DC", new int[] { 6, 8, 13, 19, 24, 29, 32, 31, 27, 30, 14, 8 });
		avgMonthlyLowTemperatures.put("Tehran", new int[] { 1, 3, 7, 13, 17, 22, 25, 25, 21, 15, 8, 3 });
		avgMonthlyHighTemperatures.put("Tehran", new int[] { 8, 11, 16, 23, 28, 34, 37, 36, 32, 25, 16, 10 });
		avgMonthlyLowTemperatures.put("Los Angeles", new int[] { 9, 9, 11, 12, 14, 16, 18, 18, 17, 15, 11, 8 });
		avgMonthlyHighTemperatures.put("Los Angeles", new int[] { 20, 21, 21, 23, 23, 26, 28, 29, 28, 26, 23, 20 });
		avgMonthlyLowTemperatures.put("Miami", new int[] { 16, 17, 18, 21, 23, 25, 26, 26, 26, 24, 21, 18 });
		avgMonthlyHighTemperatures.put("Miami", new int[] { 23, 24, 24, 26, 28, 31, 31, 32, 31, 29, 26, 24 });
		avgMonthlyLowTemperatures.put("Mexico City", new int[] { 6, 7, 9, 11, 12, 12, 12, 12, 12, 10, 8, 7 });
		avgMonthlyHighTemperatures.put("Mexico City", new int[] { 21, 23, 25, 26, 26, 24, 23, 23, 23, 22, 22, 21 });
		avgMonthlyLowTemperatures.put("Singapore", new int[] { 23, 23, 24, 24, 24, 24, 24, 24, 24, 24, 23, 23 });
		avgMonthlyHighTemperatures.put("Singapore", new int[] { 29, 31, 31, 32, 31, 31, 31, 31, 31, 31, 30, 29 });
		avgMonthlyLowTemperatures.put("Sydney", new int[] { 19, 19, 18, 15, 12, 9, 8, 8, 11, 14, 16, 18 });
		avgMonthlyHighTemperatures.put("Sydney", new int[] { 26, 26, 25, 23, 20, 17, 17, 18, 20, 22, 23, 25 });
		avgMonthlyLowTemperatures.put("Buenos Aires", new int[] { 20, 19, 18, 14, 11, 8, 8, 9, 11, 13, 16, 18 });
		avgMonthlyHighTemperatures.put("Buenos Aires", new int[] { 28, 27, 25, 22, 18, 15, 14, 16, 18, 21, 24, 27 });

	}

	public double[] computeOutsideTemperature(final Calendar today, final String city) {
		final int day = today.get(Calendar.DAY_OF_MONTH);
		final int daysInMonth = today.getActualMaximum(Calendar.DAY_OF_MONTH);
		final double[] outsideTemperature = new double[2];

		final Calendar monthFrom, monthTo;
		final int halfMonth = daysInMonth / 2;
		final double portion;
		final int totalDaysOfMonth;
		if (day < halfMonth) {
			monthFrom = (Calendar) today.clone();
			monthFrom.add(Calendar.MONTH, -1);
			monthTo = today;
			final int prevHalfMonth = monthFrom.getActualMaximum(Calendar.DAY_OF_MONTH) - monthFrom.getActualMaximum(Calendar.DAY_OF_MONTH) / 2;
			totalDaysOfMonth = prevHalfMonth + daysInMonth / 2;
			portion = (double) (day + prevHalfMonth) / totalDaysOfMonth;
		} else {
			monthFrom = today;
			monthTo = (Calendar) today.clone();
			monthTo.add(Calendar.MONTH, 1);
			final int nextHalfMonth = monthTo.getActualMaximum(Calendar.DAY_OF_MONTH) / 2;
			totalDaysOfMonth = halfMonth + nextHalfMonth;
			portion = (double) (day - halfMonth) / totalDaysOfMonth;
		}

		final int[] monthlyLowTemperatures = avgMonthlyLowTemperatures.get(city);
		final int[] monthlyHighTemperatures = avgMonthlyHighTemperatures.get(city);
		final int monthFromIndex = monthFrom.get(Calendar.MONTH);
		final int monthToIndex = monthTo.get(Calendar.MONTH);
		outsideTemperature[0] = monthlyLowTemperatures[monthFromIndex] + (monthlyLowTemperatures[monthToIndex] - monthlyLowTemperatures[monthFromIndex]) * portion;
		outsideTemperature[1] = monthlyHighTemperatures[monthFromIndex] + (monthlyHighTemperatures[monthToIndex] - monthlyHighTemperatures[monthFromIndex]) * portion;
		return outsideTemperature;
	}

	public double computeOutsideTemperatureRange(final double[] outsideTemperatureRange, final int minute) {
		return outsideTemperatureRange[1] + (outsideTemperatureRange[0] - outsideTemperatureRange[1]) * Math.abs(minute - 12 * 60) / 60 / 12;
	}

	public double computeOutsideTemperature(final Calendar now) {
		final double[] outsideTemperatureRange = CityData.getInstance().computeOutsideTemperature(Heliodon.getInstance().getCalender(), (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem());
		return computeOutsideTemperatureRange(outsideTemperatureRange, now.get(Calendar.MINUTE) + now.get(Calendar.HOUR_OF_DAY) * 60);
	}

	public String[] getCities() {
		return cities;
	}

	public Map<String, Integer> getCityLatitutes() {
		return cityLatitutes;
	}

	public Map<String, Integer> getCityAltitudes() {
		return cityAltitudes;
	}

	public Map<String, int[]> getAvgMonthlyLowTemperatures() {
		return avgMonthlyLowTemperatures;
	}

	public Map<String, int[]> getAvgMonthlyHighTemperatures() {
		return avgMonthlyHighTemperatures;
	}

}
