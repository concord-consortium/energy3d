package org.concord.energy3d.model;

/**
 * For solar panels and racks (not including mirrors as mirrors must do exact tracking -- no option)
 *
 * @author Charles Xie
 */
public interface Trackable extends SolarCollector {

    int NO_TRACKER = 0;
    int HORIZONTAL_SINGLE_AXIS_TRACKER = 1;
    int ALTAZIMUTH_DUAL_AXIS_TRACKER = 2;
    int VERTICAL_SINGLE_AXIS_TRACKER = 3;
    int TILTED_SINGLE_AXIS_TRACKER = 4;

    void setTracker(final int tracker);

    int getTracker();

}