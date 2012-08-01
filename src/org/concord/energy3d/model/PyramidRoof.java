package org.concord.energy3d.model;

import java.util.List;

import org.concord.energy3d.util.Util;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.triangulation.point.ardor3d.ArdorVector3PolygonPoint;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

public class PyramidRoof extends Roof {
	private static final long serialVersionUID = 1L;
	private transient boolean recalculateEditPoints;

	public PyramidRoof() {
		super(1, 1, 0.5);
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		if (editPointIndex == -1) {
			pickContainer(x, y, Wall.class);
			recalculateEditPoints = true;;
		} else {
			final ReadOnlyVector3 base = new Vector3(getAbsPoint(0).getX(), getAbsPoint(0).getY(), getCenter().getZ());
			final Vector3 p = Util.closestPoint(base, Vector3.UNIT_Z, x, y);
			snapToGrid(p, getAbsPoint(editPointIndex), getGridSize());
			height = Math.max(0, p.getZ() - base.getZ());
		}
		draw();
		drawWalls();
		if (container != null)
			setEditPointsVisible(true);
	}

	@Override
	protected Polygon applySteinerPoint(final Polygon polygon) {
		polygon.addSteinerPoint(new ArdorVector3PolygonPoint(getAbsPoint(0)));
		return polygon;
	}

	@Override
	protected void processRoofEditPoints(final List<? extends ReadOnlyVector3> wallUpperPoints) {
		final ReadOnlyVector3 center = getCenter();
		if (recalculateEditPoints) {
			points.get(0).set(center.getX(), center.getY(), center.getZ() + height);
			points.get(0).set(toRelative(points.get(0), container.getContainer()));
			recalculateEditPoints = false;
		} else
			points.get(0).setZ(center.getZ() + height);
	}
}
