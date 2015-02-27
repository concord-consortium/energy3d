package org.concord.energy3d.simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.concord.energy3d.gui.PropertiesPanel;
import org.concord.energy3d.shapes.Heliodon;

public class CityData {

	private static final CityData instance = new CityData();
	private final Map<String, Float> latitutes = new HashMap<String, Float>();
	private final Map<String, Float> altitudes = new HashMap<String, Float>();
	private final Map<String, int[]> lowTemperatures = new HashMap<String, int[]>();
	private final Map<String, int[]> highTemperatures = new HashMap<String, int[]>();
	private final Map<String, int[]> sunshineHours = new HashMap<String, int[]>();
	private final String[] cities;

	public static CityData getInstance() {
		return instance;
	}

	private CityData() {

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

		final int[] monthlyLowTemperatures = lowTemperatures.get(city);
		final int[] monthlyHighTemperatures = highTemperatures.get(city);
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
		final double[] outsideTemperatureRange = CityData.getInstance().computeOutsideTemperature(Heliodon.getInstance().getCalender(), (String) PropertiesPanel.getInstance().getCityComboBox().getSelectedItem());
		return computeOutsideTemperatureRange(outsideTemperatureRange, now.get(Calendar.MINUTE) + now.get(Calendar.HOUR_OF_DAY) * 60);
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
