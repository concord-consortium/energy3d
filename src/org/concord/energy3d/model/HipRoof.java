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
		super(1, 3, 0.5);
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		if (editPointIndex == -1) {
			pickContainer(x, y, Wall.class);
			recalculateEditPoints = true;
		} else if (editPointIndex == 0) {
			final ReadOnlyVector3 base = getCenter();
			final Vector3 p = Util.closestPoint(base, Vector3.UNIT_Z, x, y);
			snapToGrid(p, getAbsPoint(editPointIndex), getGridSize());
			height = Math.max(0, p.getZ() - base.getZ());
		} else if (editPointIndex == 1 || editPointIndex == 2) {
			final Vector3 p = Util.closestPoint(getAbsPoint(editPointIndex), Vector3.UNIT_Y, x, y);
			snapToGrid(p, getAbsPoint(editPointIndex), getGridSize(), false);
			if (insideWallsPolygon(p))
				points.get(editPointIndex).set(toRelative(p, container.getContainer()));
		}
		draw();
		drawWalls();
		if (container != null)
			setEditPointsVisible(true);

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
			// upper points
			points.get(0).set(toRelative(center, container.getContainer()).addLocal(0, 0, height));
			if (editPointIndex == -1) {
				final Vector3 p = new Vector3();
				p.set(center).addLocal(0, -5, height);
				snapToWallsPolygon(p);
				p.setX(center.getX());
				points.get(1).set(toRelative(p, container.getContainer()));
				p.set(center).addLocal(0, 5, height);
				snapToWallsPolygon(p);
				p.setX(center.getX());
				points.get(2).set(toRelative(p, container.getContainer()));
			}
			recalculateEditPoints = false;
		} else {
			points.get(0).setZ(center.getZ() + height);
			points.get(1).setZ(center.getZ() + height);
			points.get(2).setZ(center.getZ() + height);
		}
	}
}
