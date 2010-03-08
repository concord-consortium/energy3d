package org.concord.energy3d.model;

import java.io.Serializable;

public class Snap implements Serializable {
	private static final long serialVersionUID = 1L;
	private HousePart neighbor;
	private int neighborPointIndex;
	private int thisPointIndex;
	
	public Snap(HousePart neighbor, int thisPointIndex, int neighborPointIndex) {
		this.neighbor = neighbor;
		this.neighborPointIndex = neighborPointIndex;
		this.thisPointIndex = thisPointIndex;
	}

	public HousePart getNeighbor() {
		return neighbor;
	}

	public int getNeighborPointIndex() {
		return neighborPointIndex;
	}

	public int getThisPointIndex() {
		return thisPointIndex;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Snap) {
			Snap s = (Snap)obj;
			return neighbor == s.getNeighbor() && neighborPointIndex == s.getNeighborPointIndex();
		} else
			return false;
	}
}
