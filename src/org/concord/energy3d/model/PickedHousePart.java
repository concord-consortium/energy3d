package org.concord.energy3d.model;

import com.ardor3d.math.Vector3;

public class PickedHousePart {
	private UserData userData;
	private Vector3 point;
	
	public PickedHousePart(UserData userData, Vector3 point) {
		this.userData = userData;
		this.point = point;
	}

	public UserData getUserData() {
		return userData;
	}

	public Vector3 getPoint() {
		return point;
	}
	
	public String toString() {
		return userData + " @" + point;
	}
}
