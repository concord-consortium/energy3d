package org.concord.energy3d.model;

import java.awt.geom.Path2D;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.Util;
import org.concord.energy3d.util.WallVisitor;

import com.ardor3d.math.Vector2;
import com.ardor3d.math.type.ReadOnlyVector3;

/**
 * @author Charles Xie
 * 
 */
public class Building {

	private final static DecimalFormat FORMAT1 = new DecimalFormat("###0.##");
	private final static DecimalFormat FORMAT4 = new DecimalFormat("###0.####");

	private Foundation foundation;
	private final ArrayList<Wall> walls;
	private final ArrayList<Vector2> wallVertices;
	private int windowCount;
	private double area, cx, cy, height;
	private boolean wallComplete;
	private Path2D.Double wallPath;

	public Building(final Foundation foundation) {
		this.foundation = foundation;
		walls = new ArrayList<Wall>();
		for (HousePart x : foundation.getChildren()) {
			if (x instanceof Wall)
				walls.add((Wall) x);
		}
		wallVertices = new ArrayList<Vector2>();
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

			private void addVertex(final ReadOnlyVector3 v3) {
				final Vector2 v2 = new Vector2(v3.getX(), v3.getY());
				boolean b = false;
				for (final Vector2 x : wallVertices) {
					if (Util.isEqual(x, v2)) {
						b = true;
						break;
					}
				}
				if (!b)
					wallVertices.add(v2);
			}
		});
		wallComplete = walls.size() == wallVertices.size();
	}

	public boolean isWallComplete() {
		return wallComplete;
	}

	public int getID() {
		return (int) foundation.getId();
	}

	/** @return false if the building does not conform */
	public boolean calculate() {

		if (!wallComplete)
			return false;

		final int n = wallVertices.size();
		final double scale = Scene.getInstance().getAnnotationScale();

		height = foundation.getBoundingHeight() * scale;

		area = 0;
		Vector2 v1, v2;
		for (int i = 0; i < n - 1; i++) {
			v1 = wallVertices.get(i);
			v2 = wallVertices.get(i + 1);
			area += v1.getX() * v2.getY() - v2.getX() * v1.getY();
		}
		v1 = wallVertices.get(n - 1);
		v2 = wallVertices.get(0);
		area += v1.getX() * v2.getY() - v2.getX() * v1.getY();
		area *= 0.5;

		cx = 0;
		cy = 0;
		for (int i = 0; i < n - 1; i++) {
			v1 = wallVertices.get(i);
			v2 = wallVertices.get(i + 1);
			cx += (v1.getX() * v2.getY() - v2.getX() * v1.getY()) * (v1.getX() + v2.getX());
			cy += (v1.getX() * v2.getY() - v2.getX() * v1.getY()) * (v1.getY() + v2.getY());
		}
		v1 = wallVertices.get(n - 1);
		v2 = wallVertices.get(0);
		cx += (v1.getX() * v2.getY() - v2.getX() * v1.getY()) * (v1.getX() + v2.getX());
		cy += (v1.getX() * v2.getY() - v2.getX() * v1.getY()) * (v1.getY() + v2.getY());
		cx /= 6 * area;
		cy /= 6 * area;
		cx *= scale;
		cy *= scale;
		area = Math.abs(area) * scale * scale;

		return true;

	}

	/** call calculate() before calling this */
	public double getArea() {
		return area;
	}

	/** call calculate() before calling this */
	public double getHeight() {
		return height;
	}

	/** call calculate() before calling this */
	public double getCenterX() {
		return cx;
	}

	/** call calculate() before calling this */
	public double getCenterY() {
		return cy;
	}

	public boolean contains(final double x, final double y, final boolean init) {
		if (wallComplete)
			return false;
		final int n = wallVertices.size();
		if (init) {
			if (wallPath == null)
				wallPath = new Path2D.Double();
			else
				wallPath.reset();
			Vector2 v = wallVertices.get(0);
			wallPath.moveTo(v.getX(), v.getY());
			for (int i = 1; i < n; i++) {
				v = wallVertices.get(i);
				wallPath.lineTo(v.getX(), v.getY());
			}
			v = wallVertices.get(0);
			wallPath.lineTo(v.getX(), v.getY());
			wallPath.closePath();
		}
		return wallPath != null ? wallPath.contains(x, y) : false;
	}

	public HousePart getRoof() {
		if (walls.isEmpty())
			return null;
		return walls.get(0).getRoof();
	}

	public void setWindowCount(int n) {
		windowCount = n;
	}

	public int getWindowCount() {
		return windowCount;
	}

	@Override
	public boolean equals(final Object o) {
		if ((!(o instanceof Building)))
			return false;
		final Building b = (Building) o;
		return b.foundation == foundation;
	}

	@Override
	public int hashCode() {
		return getID();
	}

	public String getGeometryJson() {
		if (calculate()) {
			String s = "\"Height\": " + FORMAT1.format(height);
			s += ", \"Area\": " + FORMAT1.format(area);
			s += ", \"CentroidX\": " + FORMAT1.format(cx);
			s += ", \"CentroidY\": " + FORMAT1.format(cy);
			return s;
		}
		return null;
	}

	public String getSolarEnergy() {
		Foundation x = walls.get(0).getTopContainer();
		return x == null ? null : FORMAT1.format(x.getSolarPotentialToday());
	}

	public double getSolarValue() {
		Foundation x = walls.get(0).getTopContainer();
		return x == null ? -1 : x.getSolarPotentialToday();
	}

	String toJson() {
		String s = "\"ID\": " + getID();
		if (wallComplete) {
			s += ", \"WallCount\": " + walls.size();
			if (windowCount > 0)
				s += ", \"WindowCount\": " + windowCount;
			s += ", " + getGeometryJson();
			final String solar = getSolarEnergy();
			if (solar != null)
				s += ", \"SolarEnergy\": " + solar;
		}
		return s;
	}

	@Override
	public String toString() {
		String s = "(ID=" + getID();
		if (calculate()) {
			s += " #wall=" + walls.size();
			if (windowCount > 0)
				s += " #window=" + windowCount;
			s += " height=" + FORMAT1.format(height);
			s += " area=" + FORMAT1.format(area);
			s += " centroid=\"" + FORMAT1.format(cx) + "," + FORMAT1.format(cy) + "\"";
			final double solar = getSolarValue();
			if (solar >= 0) {
				s += " solar_energy=" + solar;
				s += " solar_energy_density=" + FORMAT4.format(solar / (area * height));
			}
		}
		return s + ")";
	}

}
