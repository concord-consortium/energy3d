package org.concord.energy3d.logger;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.concord.energy3d.gui.EnergyPanel;
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

	private double getArea() {
		double area = 0;
		int n = floorVertices.size();
		Vector2 v1, v2;
		for (int i = 0; i < n - 1; i++) {
			v1 = floorVertices.get(i);
			v2 = floorVertices.get(i + 1);
			area += v1.getX() * v2.getY() - v2.getX() * v1.getY();
		}
		v1 = floorVertices.get(n - 1);
		v2 = floorVertices.get(0);
		area += v1.getX() * v2.getY() - v2.getX() * v1.getY();
		return area * 0.5;
	}

	private double getCentroidX() {
		double cx = 0;
		int n = floorVertices.size();
		Vector2 v1, v2;
		for (int i = 0; i < n - 1; i++) {
			v1 = floorVertices.get(i);
			v2 = floorVertices.get(i + 1);
			cx += (v1.getX() * v2.getY() - v2.getX() * v1.getY()) * (v1.getX() + v2.getX());
		}
		v1 = floorVertices.get(n - 1);
		v2 = floorVertices.get(0);
		cx += (v1.getX() * v2.getY() - v2.getX() * v1.getY()) * (v1.getX() + v2.getX());
		return cx / 6;
	}

	private double getCentroidY() {
		double cy = 0;
		int n = floorVertices.size();
		Vector2 v1, v2;
		for (int i = 0; i < n - 1; i++) {
			v1 = floorVertices.get(i);
			v2 = floorVertices.get(i + 1);
			cy += (v1.getX() * v2.getY() - v2.getX() * v1.getY()) * (v1.getY() + v2.getY());
		}
		v1 = floorVertices.get(n - 1);
		v2 = floorVertices.get(0);
		cy += (v1.getX() * v2.getY() - v2.getX() * v1.getY()) * (v1.getY() + v2.getY());
		return cy / 6;
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
				pointIndex++;
				addVertex(currentWall.getAbsPoint(pointIndex == 1 ? 3 : 1));
				addVertex(currentWall.getAbsPoint(pointIndex));
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
		exploreWallNeighbors();
		String s = "(ID=" + id;
		if (walls.size() == floorVertices.size() && !walls.isEmpty()) {
			s += " #Wall=" + walls.size();
			if (windowCount > 0)
				s += " #Window=" + windowCount;
			s += " height=" + FORMAT.format(height);
			double area = getArea();
			s += " area=" + FORMAT.format(Math.abs(area));
			s += " volume=" + FORMAT.format(Math.abs(area) * height);
			s += " centroid=" + FORMAT.format(getCentroidX() / area) + ", " + FORMAT.format(getCentroidY() / area);
			double solar = EnergyPanel.getInstance().getSolarTotal().get(LoggerUtil.getTopContainer(walls.get(0)));
			s += " solar_energy=" + FORMAT.format(EnergyPanel.getInstance().convertSolarValue(solar));
		}
		return s + ")";
	}

}
