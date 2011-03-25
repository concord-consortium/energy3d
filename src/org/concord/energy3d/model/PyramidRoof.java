package org.concord.energy3d.model;

import java.util.ArrayList;

import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

public class PyramidRoof extends Roof {
	private static final long serialVersionUID = 1L;

	public PyramidRoof() {
		super(1, 1, 0.5);
	}
	
	public void setPreviewPoint(int x, int y) {
		if (editPointIndex == -1) {
			pick(x, y, Wall.class);
		} else {
			Vector3 base = center;
			Vector3 p = closestPoint(base, Vector3.UNIT_Z, x, y);
			p = grid(p, GRID_SIZE);
			height = Math.max(0, p.getZ() - base.getZ());
//			setHeight(Math.max(0, p.getZ() - base.getZ()), true);
		}
		draw();
		if (container != null)
			showPoints();
	}

	protected Polygon makePolygon(ArrayList<PolygonPoint> wallUpperPoints) {
		final Polygon polygon = new Polygon(wallUpperPoints);
		polygon.addSteinerPoint(new PolygonPoint(center.getX(), center.getY(), center.getZ() + height));
		return polygon;		
	}

////	protected ArrayList<PolygonPoint> exploreWallNeighbors(Wall startWall) {
//	protected void processRoofPoints(ArrayList<PolygonPoint> wallUpperPoints, ArrayList<ReadOnlyVector3> wallNormals) {
////		final ArrayList<PolygonPoint> wallUpperPoints = super.exploreWallNeighbors(startWall);
//		final double edgeLenght = 0.3;
//		Vector3 op = new Vector3();
//		double maxY;
//		maxY = wallUpperPoints.get(0).getY();
//		for (PolygonPoint p : wallUpperPoints) {
//			op.set(p.getX(), p.getY(), 0).subtractLocal(center.getX(), center.getY(), 0).normalizeLocal().multiplyLocal(edgeLenght);
//			op.addLocal(p.getX(), p.getY(), p.getZ());
//			p.set(op.getX(), op.getY(), op.getZ()+0.01);
//			if (p.getY() > maxY)
//				maxY = p.getY();			
//		}
//		points.get(0).set(center.getX(), center.getY(), center.getZ() + height);
////		return wallUpperPoints;
//	}
	
}
