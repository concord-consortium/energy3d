package org.concord.energy3d.model;

/**
 * HousePart implementing this interface will have more complex heat transfer model. This interface also excludes objects such as trees, 
 * which should not have been derived from HousePart, from heat transfer calculation.
 * 
 * @author Charles Xie
 * 
 */

public interface Thermalizable {

	public void setVolumetricHeatCapacity(final double volumetricHeatCapacity);

	public double getVolumetricHeatCapacity();

}
