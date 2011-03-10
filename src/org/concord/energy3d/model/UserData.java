package org.concord.energy3d.model;

import com.ardor3d.math.Vector3;

public class UserData {
	private final HousePart housePart;
	private final int index;
	private final boolean isEditPoint; 
	private Vector3 printCenter;
	
	public UserData(HousePart housePart) {
		this(housePart, -1, false);
	}
	
	public UserData(final HousePart housePart, final int index, final boolean isEditPoint) {
		this.housePart = housePart;
		this.index = index;
		this.isEditPoint = isEditPoint;
	}

	public HousePart getHousePart() {
		return housePart;
	}

	public int getIndex() {
		return index;
	}

	public Vector3 getPrintCenter() {
		return printCenter;
	}

	public void setPrintCenter(Vector3 printCenter) {
		this.printCenter = printCenter;
	}
	
	public boolean isEditPoint() {
		return isEditPoint;
	}

	public String toString() {
		return housePart + " index = " + index + " print center = " + printCenter;
	}

}
