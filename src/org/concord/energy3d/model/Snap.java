package org.concord.energy3d.model;

public class Snap {
	private HousePart housePart;
	private int neighborPointIndex;
	private int thisPointIndex;
	
	public Snap(HousePart housePart, int thisPointIndex, int neighborPointIndex) {
		this.housePart = housePart;
		this.neighborPointIndex = neighborPointIndex;
		this.thisPointIndex = thisPointIndex;
	}

	public HousePart getHousePart() {
		return housePart;
	}

	public int getNeighborPointIndex() {
		return neighborPointIndex;
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
			return housePart == s.getHousePart() && neighborPointIndex == s.getNeighborPointIndex();
		} else
			return false;
	}
}
