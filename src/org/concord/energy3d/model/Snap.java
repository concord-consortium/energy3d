package org.concord.energy3d.model;

public class Snap {
	private HousePart housePart;
	private int pointIndex;
	
	public Snap(HousePart housePart, int pointIndex) {
		this.housePart = housePart;
		this.pointIndex = pointIndex;
		
	}

	public HousePart getHousePart() {
		return housePart;
	}

	public int getPointIndex() {
		return pointIndex;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Snap) {
			Snap s = (Snap)obj;
			return housePart == s.getHousePart() && pointIndex == s.getPointIndex();
		} else
			return false;
	}
	
	

}
