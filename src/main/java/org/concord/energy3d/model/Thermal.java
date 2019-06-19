package org.concord.energy3d.model;

/**
 * HousePart implementing this interface does thermal physics. This interface also excludes objects such as trees, which should not have been derived from HousePart, from thermal calculation.
 *
 * @author Charles Xie
 */

public interface Thermal {

    void setUValue(double uValue);

    double getUValue();

    void setVolumetricHeatCapacity(double volumetricHeatCapacity);

    double getVolumetricHeatCapacity();

}