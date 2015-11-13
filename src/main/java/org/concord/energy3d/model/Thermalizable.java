package org.concord.energy3d.model;

/**
 * HousePart implementing this interface does thermal physics. This interface also excludes objects such as trees, which should not have been derived from HousePart, from thermal calculation.
 * 
 * @author Charles Xie
 * 
 */

public interface Thermalizable {

	public void setUValue(double uValue);

	public double getUValue();

	public void setVolumetricHeatCapacity(double volumetricHeatCapacity);

	public double getVolumetricHeatCapacity();

}
