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
			pick(x, y, Wall.class);
			recalculateEditPoints = true;
		} else if (editPointIndex == 0) {
			Vector3 base = center;
			Vector3 p = closestPoint(base, Vector3.UNIT_Z, x, y);
			p = grid(p, GRID_SIZE);
			height = Math.max(0, p.getZ() - base.getZ());
		} else if (editPointIndex == 1 || editPointIndex == 2) {
			Vector3 p = closestPoint(points.get(editPointIndex), Vector3.UNIT_Y, x, y);
			p = grid(p, GRID_SIZE);
			points.get(editPointIndex).set(p);
			draw();
			clearDrawFlag();
			draw();
			clearDrawFlag();
		}
		draw();
		if (container != null)
			showPoints();

	}

	protected Polygon makePolygon(ArrayList<PolygonPoint> wallUpperPoints) {
		// // upper points
		// points.get(0).set(center.getX(), center.getY(), center.getZ() + height);
		// if (editPointIndex == -1) {
		// points.get(1).set(center.getX(), center.getY()-1, center.getZ() + height);
		// points.get(2).set(center.getX(), center.getY()+1, center.getZ() + height);
		// } else {
		// points.get(1).setZ(center.getZ() + height);
		// points.get(2).setZ(center.getZ() + height);
		// }
		final Polygon polygon = new Polygon(wallUpperPoints);
		PolygonPoint roofUpperPoint1 = new PolygonPoint(points.get(1).getX(), points.get(1).getY(), points.get(1).getZ());
		PolygonPoint roofUpperPoint2 = new PolygonPoint(points.get(2).getX(), points.get(2).getY(), points.get(2).getZ());
		System.out.println("new SteinerPoint(" + roofUpperPoint1.getX() + ", " + roofUpperPoint1.getY() + ", " + roofUpperPoint1.getZ() + ")");
		System.out.println("new SteinerPoint(" + roofUpperPoint2.getX() + ", " + roofUpperPoint2.getY() + ", " + roofUpperPoint2.getZ() + ")");
		polygon.addSteinerPoint(roofUpperPoint1);
		polygon.addSteinerPoint(roofUpperPoint2);
		return polygon;
	}

	@Override
	protected void processRoofPoints(ArrayList<PolygonPoint> wallUpperPoints, ArrayList<ReadOnlyVector3> wallNormals) {
		super.processRoofPoints(wallUpperPoints, wallNormals);

		if (recalculateEditPoints) {
			// upper points
			points.get(0).set(center.getX(), center.getY(), center.getZ() + height);
			if (editPointIndex == -1) {
				points.get(1).set(center.getX(), center.getY() - 1, center.getZ() + height);
				points.get(2).set(center.getX(), center.getY() + 1, center.getZ() + height);
			}
			recalculateEditPoints = false;
		} else {
			points.get(0).setZ(center.getZ() + height);
			points.get(1).setZ(center.getZ() + height);
			points.get(2).setZ(center.getZ() + height);
		}
	}
}
