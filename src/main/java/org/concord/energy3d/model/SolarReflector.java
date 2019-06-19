package org.concord.energy3d.model;

/**
 * A component that collects solar energy through reflection and concentration, such as heliostats and parabolic dishes
 *
 * @author Charles Xie
 */
public interface SolarReflector extends SolarCollector {

    /**
     * a number between 0 and 1
     */
    void setReflectance(final double reflectance);

    /**
     * a number between 0 and 1
     */
    double getReflectance();

    /**
     * a number between 0 and 1
     */
    void setAbsorptance(final double absorptance);

    /**
     * a number between 0 and 1
     */
    double getAbsorptance();

    /**
     * a number between 0 and 1
     */
    void setOpticalEfficiency(final double opticalEfficiency);

    /**
     * a number between 0 and 1
     */
    double getOpticalEfficiency();

    /**
     * a number between 0 and 1
     */
    void setThermalEfficiency(final double thermalEfficiency);

    /**
     * a number between 0 and 1
     */
    double getThermalEfficiency();

}