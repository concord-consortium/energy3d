package org.concord.energy3d.model;

public class Snap {
	private HousePart housePart;
	private int pointIndex;
	private int thisPointIndex;
	
	public Snap(HousePart housePart, int pointIndex, int thisPointIndex) {
		this.housePart = housePart;
		this.pointIndex = pointIndex;
		this.thisPointIndex = thisPointIndex;
	}

	public HousePart getHousePart() {
		return housePart;
	}

	public int getOtherPointIndex() {
		return pointIndex;
	}

	public int getThisPointIndex() {
		return thisPointIndex;
	}
	
//	public void setThisPointIndex(int thisPointIndex) {
//		this.thisPointIndex = thisPointIndex;
//	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Snap) {
			Snap s = (Snap)obj;
			return housePart == s.getHousePart() && pointIndex == s.getOtherPointIndex();
		} else
			return false;
	}
}
