package org.concord.energy3d.model;

/**
 * For solar panels and racks (not including mirrors as mirrors must do exact tracking -- no option)
 *
 * @author Charles Xie
 *
 */
public interface Trackable extends Solar {

	public static final int NO_TRACKER = 0;
	public static final int HORIZONTAL_SINGLE_AXIS_TRACKER = 1;
	public static final int ALTAZIMUTH_DUAL_AXIS_TRACKER = 2;
	public static final int VERTICAL_SINGLE_AXIS_TRACKER = 3;

	public static final int NORTH_SOUTH_AXIS = 0;
	public static final int EAST_WEST_AXIS = 1;

	public void setTracker(final int tracker);

	public int getTracker();

	public void setRotationAxis(final int rotationAxis);

	public int getRotationAxis();

}
