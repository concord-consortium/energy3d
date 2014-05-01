package org.concord.energy3d.model;

import java.util.List;

import org.concord.energy3d.util.Util;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.point.ardor3d.ArdorVector3PolygonPoint;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

public class HipRoof extends Roof {
	private static final long serialVersionUID = 1L;
	private transient boolean recalculateEditPoints;

	public HipRoof() {
		super(3);
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		final EditState editState = new EditState();

		if (editPointIndex == -1) {
			pickContainer(x, y, Wall.class);
			recalculateEditPoints = true;
		} else if (editPointIndex == 0) {
			final ReadOnlyVector3 base = getCenter();
			final Vector3 p = Util.closestPoint(base, Vector3.UNIT_Z, x, y);
			snapToGrid(p, getAbsPoint(editPointIndex), getGridSize());
			height = Math.max(0, p.getZ() - base.getZ());
		} else if (editPointIndex == 1 || editPointIndex == 2) {
			final Vector3 dir = getContainerRelative().getAbsPoint(1).subtractLocal(getContainerRelative().getAbsPoint(0));
			final Vector3 p = Util.closestPoint(getAbsPoint(editPointIndex), dir, x, y);
			snapToGrid(p, getAbsPoint(editPointIndex), getGridSize(), false);
			if (insideWallsPolygon(p))
				points.get(editPointIndex).set(toRelative(p));
		}
		postEdit(editState);
	}

	@Override
	protected Polygon applySteinerPoint(final Polygon polygon) {
		final TriangulationPoint roofUpperPoint1 = new ArdorVector3PolygonPoint(getAbsPoint(1));
		final TriangulationPoint roofUpperPoint2 = new ArdorVector3PolygonPoint(getAbsPoint(2));
		polygon.addSteinerPoint(roofUpperPoint1);
		if (!roofUpperPoint2.equals(roofUpperPoint1))
			polygon.addSteinerPoint(roofUpperPoint2);
		return polygon;
	}

	@Override
	protected void processRoofEditPoints(final List<? extends ReadOnlyVector3> wallUpperPoints) {
		final ReadOnlyVector3 center = getCenter();
		if (recalculateEditPoints) {
			recalculateEditPoints = false;
			points.get(0).set(toRelative(center));
			if (editPointIndex == -1) {
				final Vector3 dir = getContainerRelative().getAbsPoint(1).subtractLocal(getContainerRelative().getAbsPoint(0)).normalizeLocal();
				Vector3 point1 = findFarthestIntersection(wallUpperPoints, center, center.add(dir.multiply(-50, null), null));
				if (point1 == null)
					point1 = center.clone();
				point1.addLocal(dir.multiply(2, null));
				points.get(1).set(toRelative(point1));

				Vector3 point2 = findFarthestIntersection(wallUpperPoints, center, center.add(dir.multiply(50, null), null));
				if (point2 == null)
					point2 = center.clone();
				point2.addLocal(dir.multiply(-2, null));
				points.get(2).set(toRelative(point2));
			}
			computeHeight(wallUpperPoints);
			applyHeight();
		} else {
			applyHeight();
		}
	}

	private Vector3 findFarthestIntersection(final List<? extends ReadOnlyVector3> wallUpperPoints, final ReadOnlyVector3 center, final Vector3 p) {
		double farthestDistance = 0;
		Vector3 farthestIntersection = null;
		final int n = wallUpperPoints.size();
		for (int i = 0; i < n; i++) {
			final Vector3 intersect = Util.intersectLineSegments(center, p, wallUpperPoints.get(i), wallUpperPoints.get((i + 1) % n));
			if (intersect != null) {
				final double d = intersect.distanceSquared(center);
				if (d > farthestDistance) {
					farthestDistance = d;
					farthestIntersection = intersect;
				}
			}
		}
		return farthestIntersection;
	}
}
