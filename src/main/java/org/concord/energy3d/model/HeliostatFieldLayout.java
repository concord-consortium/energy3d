package org.concord.energy3d.model;

/**
 * @author Charles Xie
 */
public abstract class HeliostatFieldLayout {

    double apertureWidth = 5;
    double apertureHeight = 3;
    double startAngle = -180;
    double endAngle = 180;
    double axisRoadWidth = 0;
    double poleHeight = 2;

    public void setPoleHeight(final double poleHeight) {
        this.poleHeight = poleHeight;
    }

    public double getPoleHeight() {
        return poleHeight;
    }

    public void setApertureWidth(final double apertureWidth) {
        this.apertureWidth = apertureWidth;
    }

    public double getApertureWidth() {
        return apertureWidth;
    }

    public void setApertureHeight(final double apertureHeight) {
        this.apertureHeight = apertureHeight;
    }

    public double getApertureHeight() {
        return apertureHeight;
    }

    public void setStartAngle(final double startAngle) {
        this.startAngle = startAngle;
    }

    public double getStartAngle() {
        return startAngle;
    }

    public void setEndAngle(final double endAngle) {
        this.endAngle = endAngle;
    }

    public double getEndAngle() {
        return endAngle;
    }

    public void setAxisRoadWidth(final double axisRoadWidth) {
        this.axisRoadWidth = axisRoadWidth;
    }

    public double getAxisRoadWidth() {
        return axisRoadWidth;
    }

}