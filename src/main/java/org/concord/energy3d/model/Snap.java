package org.concord.energy3d.model;

import java.io.Serializable;

import com.ardor3d.math.type.ReadOnlyVector3;

public class Snap implements Serializable {

    private static final long serialVersionUID = 1L;
    private static int currentAnnotationDrawnStamp = 1;
    private transient int annotationDrawn;
    private final Wall neighbor1;
    private final Wall neighbor2;
    private int pointIndex1;
    private int pointIndex2;

    public static void clearAnnotationDrawn() {
        currentAnnotationDrawnStamp = ++currentAnnotationDrawnStamp % 1000;
    }

    public Snap(final Wall neighbor1, final Wall neighbor2, final int pointIndex1, final int pointIndex2) {
        this.neighbor1 = neighbor1;
        this.neighbor2 = neighbor2;
        this.pointIndex1 = pointIndex1;
        this.pointIndex2 = pointIndex2;
    }

    Wall getNeighborOf(final HousePart housePart) {
        if (housePart == neighbor2)
            return neighbor1;
        else
            return neighbor2;
    }

    int getSnapPointIndexOf(final HousePart housePart) {
        fixPointIndices(); // TODO do this in constructor
        if (housePart == neighbor2)
            return pointIndex2;
        else
            return pointIndex1;
    }

    int getSnapPointIndexOfNeighborOf(final HousePart housePart) {
        fixPointIndices(); // TODO do this in constructor
        if (housePart == neighbor1)
            return pointIndex2;
        else
            return pointIndex1;
    }

    public ReadOnlyVector3 getSnapPointOfNeighborOf(final HousePart housePart) {
        return getNeighborOf(housePart).getAbsPoint(getSnapPointIndexOfNeighborOf(housePart));
    }

    private void fixPointIndices() {
        if (pointIndex1 >= 2)
            pointIndex1 = 2;
        else
            pointIndex1 = 0;

        if (pointIndex2 >= 2)
            pointIndex2 = 2;
        else
            pointIndex2 = 0;
    }

    public void setDrawn() {
        annotationDrawn = currentAnnotationDrawnStamp;
    }

    public boolean isDrawn() {
        return annotationDrawn == currentAnnotationDrawnStamp;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Snap) {
            final Snap s = (Snap) obj;
            return neighbor1 == s.neighbor1 && neighbor2 == s.neighbor2 && pointIndex1 == s.pointIndex1 && pointIndex2 == s.pointIndex2
                    || neighbor1 == s.neighbor2 && neighbor2 == s.neighbor1 && pointIndex1 == s.pointIndex2 && pointIndex2 == s.pointIndex1;
        }
        return false;
    }

    @Override
    public String toString() {
        return "(" + neighbor1 + ", " + pointIndex1 + ") - (" + neighbor2 + ", " + pointIndex2 + ")";
    }

}