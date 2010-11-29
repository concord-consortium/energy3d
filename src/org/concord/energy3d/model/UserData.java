package org.concord.energy3d.model;

import com.ardor3d.math.Vector3;

public class UserData {
	private HousePart housePart;
	private int pointIndex;
	private Vector3 printCenter;
	
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
		return housePart + " index = " + pointIndex + " print center = " + printCenter;
	}

	public Vector3 getPrintCenter() {
		return printCenter;
	}

	public void setPrintCenter(Vector3 printCenter) {
		this.printCenter = printCenter;
	}
}
