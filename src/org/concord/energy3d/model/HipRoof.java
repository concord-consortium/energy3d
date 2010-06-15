package org.concord.energy3d.model;

import java.util.ArrayList;

import org.poly2tri.polygon.Polygon;
import org.poly2tri.polygon.PolygonPoint;

import com.ardor3d.math.Vector3;

public class HipRoof extends Roof {
	private static final long serialVersionUID = 1L;
	private static final double GRID_SIZE = 0.5;

	public HipRoof() {
		super(1, 3, 0.5);
	}

	public void setPreviewPoint(int x, int y) {
		if (editPointIndex == -1) {
			pick(x, y, Wall.class);
		} else if (editPointIndex == 0){
			Vector3 base = center;
			Vector3 p = closestPoint(base, base.add(0, 0, 1, null), x, y);
			p = grid(p, GRID_SIZE);
			height = findHeight(base, p);
		} else if (editPointIndex == 1 || editPointIndex == 2) {
			Vector3 p = closestPoint(points.get(0), points.get(0).add(Vector3.UNIT_Y, null), x, y);
			p = grid(p, GRID_SIZE);
			points.get(editPointIndex).set(p);
		}
		draw();
		showPoints();

	}

	protected Polygon makePolygon(ArrayList<PolygonPoint> wallUpperPoints) {
		final double edgeLenght = 0.3;
		Vector3 op = new Vector3();
		for (PolygonPoint p : wallUpperPoints) {
			op.set(p.getX(), p.getY(), 0).subtractLocal(center.getX(), center.getY(), 0).normalizeLocal().multiplyLocal(edgeLenght);
			op.addLocal(p.getX(), p.getY(), p.getZ());
			p.set(op.getX(), op.getY(), op.getZ()+0.01);
		}
		points.get(0).set(center.getX(), center.getY(), center.getZ() + height);
		
		// upper points
		points.get(0).set(center.getX(), center.getY(), center.getZ() + height);
		if (editPointIndex == -1) {
			points.get(1).set(center.getX(), center.getY()-1, center.getZ() + height);
			points.get(2).set(center.getX(), center.getY()+1, center.getZ() + height);
		} else {
			points.get(1).setZ(center.getZ() + height);
			points.get(2).setZ(center.getZ() + height);
		}
		PolygonPoint roofUpperPoint1 = new PolygonPoint(points.get(1).getX(), points.get(1).getY(), points.get(1).getZ());
		PolygonPoint roofUpperPoint2 = new PolygonPoint(points.get(2).getX(), points.get(2).getY(), points.get(2).getZ());
		final Polygon polygon = new Polygon(wallUpperPoints);
		polygon.addSteinerPoint(roofUpperPoint1);
		polygon.addSteinerPoint(roofUpperPoint2);
		return polygon;						
	}

}
