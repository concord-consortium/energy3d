package org.concord.energy3d.model;

/**
 * @author Charles Xie
 */
public class HeliostatRectangularFieldLayout extends HeliostatFieldLayout {

    private double rowSpacing = 1;
    private double columnSpacing = 1;
    private int rowAxis = 0;

    public void setRowAxis(final int rowAxis) {
        this.rowAxis = rowAxis;
    }

    public int getRowAxis() {
        return rowAxis;
    }

    public void setRowSpacing(final double rowSpacing) {
        this.rowSpacing = rowSpacing;
    }

    public double getRowSpacing() {
        return rowSpacing;
    }

    public void setColumnSpacing(final double columnSpacing) {
        this.columnSpacing = columnSpacing;
    }

    public double getColumnSpacing() {
        return columnSpacing;
    }

}