package org.concord.energy3d.logger;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.ardor3d.math.Vector2;

/**
 * @author Charles Xie
 * 
 */
class Building {

	private final static DecimalFormat FORMAT = new DecimalFormat("###.#");

	int id;
	int wallCount;
	int windowCount;
	double height = Double.MAX_VALUE;
	ArrayList<Vector2> floorVertices;

	Building(int id) {
		this.id = id;
		floorVertices = new ArrayList<Vector2>();
	}

	double getArea() {
		double area = 0;
		int n = floorVertices.size();
		for (int i = 0; i < n - 1; i++) {
			Vector2 v1 = floorVertices.get(i);
			Vector2 v2 = floorVertices.get(i + 1);
			area += v1.getX() * v2.getY() - v2.getX() * v1.getY();
		}
		return area * 0.5;
	}

	double getVolume() {
		return getArea() * height;
	}

	@Override
	public boolean equals(Object o) {
		if ((!(o instanceof Building)))
			return false;
		Building b = (Building) o;
		return b.id == id;
	}

	@Override
	public String toString() {
		String s = "(ID=" + id;
		s += " #Wall=" + wallCount;
		s += " #Window=" + windowCount;
		s += " height=" + FORMAT.format(height);
		double area = getArea();
		s += " area=" + FORMAT.format(area);
		s += " volume=" + FORMAT.format(area * height);
		return s + ")";
	}

}
