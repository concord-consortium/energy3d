package org.concord.energy3d.model;

import java.io.Serializable;

public class Snap implements Serializable {
	private static final long serialVersionUID = 1L;
	private static int currentAnnotationDrawnStamp = 1;
	private static int currentVisitStamp = 1;
	private HousePart neighbor1;
	private HousePart neighbor2;
	private int pointIndex1;
	private int pointIndex2;
	private transient int annotationDrawn;
	private transient int visitStamp;
	
	public static void clearAnnotationDrawn() {
		currentAnnotationDrawnStamp = ++currentAnnotationDrawnStamp % 1000;
	}
	
	public static void clearVisits() {
		currentVisitStamp = ++currentVisitStamp % 1000;
	}	
	
	public Snap(HousePart neighbor1, HousePart neighbor2, int pointIndex1, int pointIndex2) {
		this.neighbor1 = neighbor1;
		this.neighbor2 = neighbor2;
		this.pointIndex1 = pointIndex1;
		this.pointIndex2 = pointIndex2;
	}

	public HousePart getNeighborOf(HousePart housePart) {
		if (housePart == neighbor1)
			return neighbor2;
		else
			return neighbor1;
	}

	public int getSnapPointIndexOf(HousePart housePart) {
		if (housePart == neighbor1)
			return pointIndex1;
		else
			return pointIndex2;
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

	public boolean isVisited() {
		return visitStamp == currentVisitStamp;
	}

	public void visit() {
		visitStamp = currentVisitStamp;
	}	
	

//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof Snap) {
//			Snap s = (Snap)obj;
//			return neighbor1 == s.neighbor1 && neighbor2 == s.neighbor2 && neighborPointIndex == s.getNeighborPointIndex();
//		} else
//			return false;
//	}
}
