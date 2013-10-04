package org.concord.energy3d.logger;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.concord.energy3d.model.Snap;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.util.Util;
import org.concord.energy3d.util.WallVisitor;

import com.ardor3d.math.Vector2;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * @author Charles Xie
 * 
 */
class Building {

	private final static DecimalFormat FORMAT = new DecimalFormat("###.#");

	int id;
	int windowCount;
	private double height = Double.MAX_VALUE;
	private ArrayList<Wall> walls;
	private ArrayList<Vector2> floorVertices;

	Building(int id) {
		this.id = id;
		walls = new ArrayList<Wall>();
		floorVertices = new ArrayList<Vector2>();
	}

	void addWall(Wall w) {
		if (walls.contains(w))
			return;
		walls.add(w);
		double h = w.getHeight();
		if (height > h)
			height = h;
	}

	double getArea() {
		double area = 0;
		int n = floorVertices.size();
		for (int i = 0; i < n - 1; i++) {
			Vector2 v1 = floorVertices.get(i);
			Vector2 v2 = floorVertices.get(i + 1);
			area += v1.getX() * v2.getY() - v2.getX() * v1.getY();
		}
		return Math.abs(area * 0.5);
	}

	double getVolume() {
		return getArea() * height;
	}

	private void addVertex(ReadOnlyVector3 v3) {
		Vector2 v2 = new Vector2(v3.getX(), v3.getY());
		boolean b = false;
		for (Vector2 x : floorVertices) {
			if (Util.isEqual(x, v2)) {
				b = true;
				break;
			}
		}
		if (!b)
			floorVertices.add(v2);
	}

	private void exploreWallNeighbors() {
		if (walls.isEmpty())
			return;
		walls.get(0).visitNeighbors(new WallVisitor() {
			@Override
			public void visit(final Wall currentWall, final Snap prev, final Snap next) {
				int pointIndex = 0;
				if (next != null)
					pointIndex = next.getSnapPointIndexOf(currentWall);
				pointIndex = pointIndex + 1;
				final ReadOnlyVector3 p1 = currentWall.getAbsPoint(pointIndex == 1 ? 3 : 1);
				final ReadOnlyVector3 p2 = currentWall.getAbsPoint(pointIndex);
				addVertex(p1);
				addVertex(p2);
			}
		});
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
		s += " #Wall=" + walls.size();
		s += " #Window=" + windowCount;
		s += " height=" + FORMAT.format(height);
		exploreWallNeighbors();
		double area = walls.size() == floorVertices.size() ? getArea() : -1;
		s += " n=" + floorVertices.size();
		if (area > 0) {
			s += " area=" + FORMAT.format(area);
			s += " volume=" + FORMAT.format(area * height);
		}
		return s + ")";
	}

}
