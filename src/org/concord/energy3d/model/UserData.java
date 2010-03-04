package org.concord.energy3d.model;

public class UserData {
	private HousePart housePart;
	private int pointIndex;
	
	public UserData(HousePart housePart) {
		this(housePart, -1);
	}
	
	public UserData(HousePart housePart, int pointIndex) {
		this.housePart = housePart;
		this.pointIndex = pointIndex;
	}

	public HousePart getHousePart() {
		return housePart;
	}

	public int getPointIndex() {
		return pointIndex;
	}

	public String toString() {
		return housePart + " index = " + pointIndex;
	}
}
