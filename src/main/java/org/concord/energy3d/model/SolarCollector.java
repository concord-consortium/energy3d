package org.concord.energy3d.model;

/**
 * A component that collects solar energy, such as solar panels and mirrors
 * 
 * @author Charles Xie
 *
 */
public interface SolarCollector {

	public void setBaseHeight(final double baseHeight);

	public double getBaseHeight();

	public double getYieldNow();

	public void setYieldNow(final double yieldNow);

	public double getYieldToday();

	public void setYieldToday(final double yieldToday);

}
