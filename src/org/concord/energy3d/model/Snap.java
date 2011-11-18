package org.concord.energy3d.model;

import java.io.Serializable;

public class Snap implements Serializable {
	private static final long serialVersionUID = 1L;
	private static int currentAnnotationDrawnStamp = 1;
	private transient int annotationDrawn;
	private Wall neighbor1;
	private Wall neighbor2;
	private int pointIndex1;
	private int pointIndex2;	
	
	public static void clearAnnotationDrawn() {
		currentAnnotationDrawnStamp = ++currentAnnotationDrawnStamp % 1000;
	}
	
	public Snap(Wall neighbor1, Wall neighbor2, int pointIndex1, int pointIndex2) {
		this.neighbor1 = neighbor1;
		this.neighbor2 = neighbor2;
		this.pointIndex1 = pointIndex1;
		this.pointIndex2 = pointIndex2;
	}

	public Wall getNeighborOf(HousePart housePart) {
		if (housePart == neighbor2)
			return neighbor1;
		else
			return neighbor2;
	}

	public int getSnapPointIndexOf(HousePart housePart) {
		if (housePart == neighbor2)
			return pointIndex2;
		else
			return pointIndex1;
	}
	
	public int getSnapPointIndexOfNeighborOf(HousePart housePart) {
		if (housePart == neighbor1)
			return pointIndex2;
		else
			return pointIndex1;
	}
	
	public void setDrawn() {
		annotationDrawn = currentAnnotationDrawnStamp;
	}
	
	public boolean isDrawn() {
		return annotationDrawn == currentAnnotationDrawnStamp;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Snap) {
			Snap s = (Snap)obj;
			return neighbor1 == s.neighbor1 && neighbor2 == s.neighbor2 && pointIndex1 == s.pointIndex1 && pointIndex2 == s.pointIndex2;
		} else
			return false;
	}
	
	@Override
	public String toString() {
		return "(" + neighbor1 + ", " + pointIndex1 + ") - ("+ neighbor2 + ", " + pointIndex2 + ")";
	}
}
