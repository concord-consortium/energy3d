package org.concord.energy3d.simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.shapes.Heliodon;

public class CityData {
	private static final CityData instance = new CityData();
	private final Map<String, Float> cityLatitutes = new HashMap<String, Float>();
	private final Map<String, Float> cityAltitudes = new HashMap<String, Float>();
	private final Map<String, int[]> avgMonthlyLowTemperatures = new HashMap<String, int[]>();
	private final Map<String, int[]> avgMonthlyHighTemperatures = new HashMap<String, int[]>();
	private final String[] cities;

	public static CityData getInstance() {
		return instance;
	}

	private CityData() {

		Map<String, String> data = new HashMap<String, String>();
		InputStream is = null;
		try {
			is = getClass().getResourceAsStream("cities/data.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = reader.readLine()) != null) {
				data.put(line.substring(0, 20).trim(), line.substring(20).trim());
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
			cityLatitutes.put(s, Float.parseFloat(t[0].trim()));
			cityAltitudes.put(s, Float.parseFloat(t[1].trim()));
			int[] los = new int[12];
			int[] his = new int[12];
			for (int k = 0; k < 12; k++) {
				los[k] = Integer.parseInt(t[2 + 2 * k].trim());
				his[k] = Integer.parseInt(t[3 + 2 * k].trim());
			}
			avgMonthlyLowTemperatures.put(s, los);
			avgMonthlyHighTemperatures.put(s, his);
		}

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

	public Map<String, Float> getCityLatitutes() {
		return cityLatitutes;
	}

	public Map<String, Float> getCityAltitudes() {
		return cityAltitudes;
	}

	public Map<String, int[]> getAvgMonthlyLowTemperatures() {
		return avgMonthlyLowTemperatures;
	}

	public Map<String, int[]> getAvgMonthlyHighTemperatures() {
		return avgMonthlyHighTemperatures;
	}

}
