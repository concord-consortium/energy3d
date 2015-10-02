package org.concord.energy3d.simulation;

import java.util.Calendar;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.shapes.Heliodon;

/**
 * Everything about weather: temperature, precipitation, sunshine, wind, etc.
 * 
 * @author Charles Xie
 * 
 */

public class Weather {

	private static final Weather instance = new Weather();

	public static Weather getInstance() {
		return instance;
	}

	private Weather() {
	}

	// we only know the average lowest and highest temperatures of the months. So we have to interpolate between these monthly data to get the daily data.
	public static double[] computeOutsideTemperature(final Calendar today, final String location) {

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

		final int[] monthlyLowTemperatures = LocationData.getInstance().getLowTemperatures().get(location);
		final int[] monthlyHighTemperatures = LocationData.getInstance().getHighTemperatures().get(location);
		if (monthlyHighTemperatures != null && monthlyLowTemperatures != null) {
			final int monthFromIndex = monthFrom.get(Calendar.MONTH);
			final int monthToIndex = monthTo.get(Calendar.MONTH);
			outsideTemperature[0] = monthlyLowTemperatures[monthFromIndex] + (monthlyLowTemperatures[monthToIndex] - monthlyLowTemperatures[monthFromIndex]) * portion;
			outsideTemperature[1] = monthlyHighTemperatures[monthFromIndex] + (monthlyHighTemperatures[monthToIndex] - monthlyHighTemperatures[monthFromIndex]) * portion;
		} else {
			outsideTemperature[0] = 0;
			outsideTemperature[1] = 20;
		}
		return outsideTemperature;

	}

	// interpolate between the lowest and highest temperatures of the day to get the temperature of a given minute in the day
	public static double getOutsideTemperatureAtMinute(final double[] outsideTemperatureRange, final int minute) {
		return outsideTemperatureRange[1] + (outsideTemperatureRange[0] - outsideTemperatureRange[1]) * Math.abs(minute / 720.0 - 1);
	}

	public double getCurrentOutsideTemperature() {
		Calendar now = Heliodon.getInstance().getCalender();
		final double[] outsideTemperatureRange = computeOutsideTemperature(now, (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem());
		return getOutsideTemperatureAtMinute(outsideTemperatureRange, now.get(Calendar.MINUTE) + now.get(Calendar.HOUR_OF_DAY) * 60);
	}

}
