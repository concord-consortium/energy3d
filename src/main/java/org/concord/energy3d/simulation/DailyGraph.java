package org.concord.energy3d.simulation;

import java.util.ArrayList;
import java.util.List;

/**
 * Daily graph (24 hours)
 *
 * @author Charles Xie
 */
public abstract class DailyGraph extends Graph {

    private static final long serialVersionUID = 1L;
    private boolean militaryTime = false;

    static List<Results> records;

    static {
        records = new ArrayList<>();
    }

    DailyGraph() {
        super();
        xAxisLabel = "Hour";
        yAxisLabel = "Energy per Hour (kWh)";
        xmin = 0;
        xmax = 23;
        numberOfTicks = 24;
    }

    public void setMilitaryTime(final boolean militaryTime) {
        this.militaryTime = militaryTime;
    }

    public boolean getMilitaryTime() {
        return militaryTime;
    }

    @Override
    String getXAxisLabel(final int i) {
        if (militaryTime) {
            return Math.round(i * getXAxisLabelScalingFactor()) + getXAxisUnit();
        }
        if (i < 12) {
            return i + "am";
        }
        if (i == 12) {
            return "12pm";
        }
        return (i - 12) + "pm";
    }

    @Override
    double getXAxisLabelScalingFactor() {
        return 1.0;
    }

    @Override
    String getXAxisUnit() {
        return "";
    }

}