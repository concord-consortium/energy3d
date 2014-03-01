package org.concord.energy3d.logger;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Snap;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.Util;
import org.concord.energy3d.util.WallVisitor;

import com.ardor3d.math.Vector2;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * @author Charles Xie
 * 
 */
class Building {

	private final static DecimalFormat FORMAT1 = new DecimalFormat("###0.##");
	private final static DecimalFormat FORMAT4 = new DecimalFormat("###0.####");

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
	public int hashCode() {
		return id;
	}

	boolean isComplete() {
		exploreWallNeighbors();
		return walls.size() == floorVertices.size() && !walls.isEmpty();
	}

	String getGeometryJson() {
		double scale = Scene.getInstance().getAnnotationScale();
		String s = "\"Height\": " + FORMAT1.format(height * scale);
		double area = getArea();
		s += ", \"Area\": " + FORMAT1.format(Math.abs(area * scale * scale));
		double volume = Math.abs(area) * height;
		s += ", \"Volume\": " + FORMAT1.format(volume * scale * scale * scale);
		if (area != 0) {
			s += ", \"CentroidX\": " + FORMAT1.format(getCentroidX() / area * scale);
			s += ", \"CentroidY\": " + FORMAT1.format(getCentroidY() / area * scale);
		}
		return s;
	}

	String getSolarEnergy() {
		HousePart x = LoggerUtil.getTopContainer(walls.get(0));
		if (!(x instanceof Foundation))
			return null;
		Foundation foundation = (Foundation) x;
		return FORMAT1.format(foundation.getSolarPotential());
	}

	double getSolarValue() {
		HousePart x = LoggerUtil.getTopContainer(walls.get(0));
		if (!(x instanceof Foundation))
			return -1;
		Foundation foundation = (Foundation) x;
		return foundation.getSolarPotential();
	}

	String toJson() {
		String s = "\"ID\": " + id;
		if (isComplete()) {
			s += ", \"WallCount\": " + walls.size();
			if (windowCount > 0)
				s += ", \"WindowCount\": " + windowCount;
			s += ", " + getGeometryJson();
			String solar = getSolarEnergy();
			if (solar != null)
				s += ", \"SolarEnergy\": " + solar;
		}
		return s;
	}

	@Override
	public String toString() {
		String s = "(ID=" + id;
		if (isComplete()) {
			s += " #wall=" + walls.size();
			if (windowCount > 0)
				s += " #window=" + windowCount;
			s += " height=" + FORMAT1.format(height);
			double area = getArea();
			s += " area=" + FORMAT1.format(Math.abs(area));
			double volume = Math.abs(area) * height;
			s += " volume=" + FORMAT1.format(volume);
			s += " centroid=\"" + FORMAT1.format(getCentroidX() / area) + "," + FORMAT1.format(getCentroidY() / area) + "\"";
			double solar = getSolarValue();
			if (solar >= 0) {
				s += " solar_energy=" + solar;
				s += " solar_energy_density=" + FORMAT4.format(solar / volume);
			}
		}
		return s + ")";
	}

}
