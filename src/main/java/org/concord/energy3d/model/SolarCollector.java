package org.concord.energy3d.model;

import com.ardor3d.math.Vector3;

/**
 * A component that collects solar energy, such as solar panels and mirrors
 * 
 * @author Charles Xie
 *
 */
public interface SolarCollector {

	public void move(final Vector3 v, final double steplength);

	public void setPoleHeight(final double poleHeight);

	public double getPoleHeight();

	public void setSunBeamVisible(final boolean visible);

	public boolean isSunBeamVisible();

	public void drawSunBeam();

	public double getYieldNow();

	public void setYieldNow(final double yieldNow);

	public double getYieldToday();

	public void setYieldToday(final double yieldToday);

}
