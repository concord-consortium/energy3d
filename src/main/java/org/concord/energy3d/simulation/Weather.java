package org.concord.energy3d.simulation;

import java.util.Calendar;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.shapes.Heliodon;

/**
 * Everything about weather: temperature, precipitation, sunshine, wind, etc.
 *
 * @author Charles Xie
 */

public class Weather {

    private static final Weather instance = new Weather();
    private static final double OMEGA_DAY = Math.PI / 720.0; // the daily cycle is 1440 minutes

    public static Weather getInstance() {
        return instance;
    }

    private Weather() {
    }

    // we only know the average lowest and highest temperatures of the months. So we have to interpolate between these monthly data to get the daily data.
    static double[] computeOutsideTemperature(final Calendar today, final String location) {

        final int day = today.get(Calendar.DAY_OF_MONTH);
        final int daysOfCurrentMonth = today.getActualMaximum(Calendar.DAY_OF_MONTH);
        final int halfOfCurrentMonth = daysOfCurrentMonth / 2;
        final double[] outsideTemperature = new double[2];

        // interpolate the temperatures
        final Calendar month1, month2;
        final double weight;
        final int length;
        if (day < halfOfCurrentMonth) { // use previous month
            month1 = (Calendar) today.clone();
            month1.add(Calendar.MONTH, -1);
            month2 = today;
            final int halfOfPreviousMonth = month1.getActualMaximum(Calendar.DAY_OF_MONTH) / 2;
            length = halfOfPreviousMonth + halfOfCurrentMonth;
            weight = (double) (day + halfOfPreviousMonth) / length;
        } else { // use next month
            month1 = today;
            month2 = (Calendar) today.clone();
            month2.add(Calendar.MONTH, 1);
            final int halfOfNextMonth = month2.getActualMaximum(Calendar.DAY_OF_MONTH) / 2;
            length = halfOfCurrentMonth + halfOfNextMonth;
            weight = (double) (day - halfOfCurrentMonth) / length;
        }

        final int[] monthlyLowTemperatures = LocationData.getInstance().getLowTemperatures().get(location);
        final int[] monthlyHighTemperatures = LocationData.getInstance().getHighTemperatures().get(location);
        if (monthlyHighTemperatures != null && monthlyLowTemperatures != null) {
            final int m1 = month1.get(Calendar.MONTH);
            final int m2 = month2.get(Calendar.MONTH);
            outsideTemperature[0] = monthlyLowTemperatures[m1] + (monthlyLowTemperatures[m2] - monthlyLowTemperatures[m1]) * weight;
            outsideTemperature[1] = monthlyHighTemperatures[m1] + (monthlyHighTemperatures[m2] - monthlyHighTemperatures[m1]) * weight;
        } else {
            outsideTemperature[0] = 0;
            outsideTemperature[1] = 20;
        }
        return outsideTemperature;

    }

    // interpolate between the lowest and highest temperatures of the day to get the temperature of a given minute in the day
    double getOutsideTemperatureAtMinute(final double tmax, final double tmin, final int minute) {
        return 0.5 * (tmax + tmin) - 0.5 * (tmax - tmin) * Math.cos(OMEGA_DAY * minute);
        // return tmax + (tmin - tmax) * Math.abs(minute / 720.0 - 1);
    }

    public double getCurrentOutsideTemperature() {
        Calendar now = Heliodon.getInstance().getCalendar();
        final double[] t = computeOutsideTemperature(now, (String) EnergyPanel.getInstance().getRegionComboBox().getSelectedItem());
        return getOutsideTemperatureAtMinute(t[1], t[0], now.get(Calendar.MINUTE) + now.get(Calendar.HOUR_OF_DAY) * 60);
    }

}