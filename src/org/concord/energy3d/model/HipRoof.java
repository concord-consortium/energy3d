package org.concord.energy3d.model;

import java.util.ArrayList;

import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

public class HipRoof extends Roof {
	private static final long serialVersionUID = 1L;
	private static final double GRID_SIZE = 0.5;
	transient private boolean recalculateEditPoints;

	public HipRoof() {
		super(1, 3, 0.5);
	}

	public void setPreviewPoint(int x, int y) {
		if (editPointIndex == -1) {
			pickContainer(x, y, Wall.class);
			recalculateEditPoints = true;
		} else if (editPointIndex == 0) {
			Vector3 base = center;
			Vector3 p = closestPoint(base, Vector3.UNIT_Z, x, y);
			p = grid(p, GRID_SIZE);
			height = Math.max(0, p.getZ() - base.getZ());
		} else if (editPointIndex == 1 || editPointIndex == 2) {
//			Vector3 p = closestPoint(points.get(editPointIndex), Vector3.UNIT_Y, x, y);
			Vector3 p = closestPoint(getAbsPoint(editPointIndex), Vector3.UNIT_Y, x, y);
			p = grid(p, GRID_SIZE);
			points.get(editPointIndex).set(toRelative(p, container.getContainer()));
		}
		draw();
		if (container != null)
			showPoints();

	}

	protected Polygon makePolygon(ArrayList<PolygonPoint> wallUpperPoints) {
		final Polygon polygon = new Polygon(wallUpperPoints);
		final Vector3 p1 = getAbsPoint(1);
		PolygonPoint roofUpperPoint1 = new PolygonPoint(p1.getX(), p1.getY(), p1.getZ());
		final Vector3 p2 = getAbsPoint(2);
		PolygonPoint roofUpperPoint2 = new PolygonPoint(p2.getX(), p2.getY(), p2.getZ());
		polygon.addSteinerPoint(roofUpperPoint1);
		polygon.addSteinerPoint(roofUpperPoint2);
		return polygon;
	}

	@Override
	protected void processRoofPoints(ArrayList<PolygonPoint> wallUpperPoints, ArrayList<ReadOnlyVector3> wallNormals) {
		super.processRoofPoints(wallUpperPoints, wallNormals);

		if (recalculateEditPoints) {
			// upper points
//			points.get(0).set(center.getX(), center.getY(), center.getZ() + height);
			points.get(0).set(toRelative(center, container.getContainer()).addLocal(0, 0, height));
			if (editPointIndex == -1) {
//				points.get(1).set(center.getX(), center.getY() - 1, center.getZ() + height);
//				points.get(2).set(center.getX(), center.getY() + 1, center.getZ() + height);
				points.get(1).set(toRelative(center, container.getContainer()).addLocal(0, -0.2, height));
				points.get(2).set(toRelative(center, container.getContainer()).addLocal(0, 0.2, height));
			}
			recalculateEditPoints = false;
		} else {
			points.get(0).setZ(center.getZ() + height);
			points.get(1).setZ(center.getZ() + height);
			points.get(2).setZ(center.getZ() + height);
		}
	}
}
