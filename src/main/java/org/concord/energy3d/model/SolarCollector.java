package org.concord.energy3d.model;

import com.ardor3d.math.Vector3;

/**
 * A component that collects solar energy, such as solar panels and mirrors
 *
 * @author Charles Xie
 */
public interface SolarCollector {

    void move(final Vector3 v, final double steplength);

    void setPoleHeight(final double poleHeight);

    double getPoleHeight();

    void setSunBeamVisible(final boolean visible);

    boolean isSunBeamVisible();

    void drawSunBeam();

    double getYieldNow();

    void setYieldNow(final double yieldNow);

    double getYieldToday();

    void setYieldToday(final double yieldToday);

}