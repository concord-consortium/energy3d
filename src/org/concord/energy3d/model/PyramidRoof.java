package org.concord.energy3d.model;

import java.util.ArrayList;

import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

public class PyramidRoof extends Roof {
	private static final long serialVersionUID = 1L;
	transient private boolean recalculateEditPoints;

	public PyramidRoof() {
		super(1, 1, 0.5);
	}
	
	public void setPreviewPoint(int x, int y) {		
		if (editPointIndex == -1) {
			pickContainer(x, y, Wall.class);
			recalculateEditPoints = true;
		} else {
			final ReadOnlyVector3 base = new Vector3(getAbsPoint(0).getX(), getAbsPoint(0).getY(), getCenter().getZ());
			Vector3 p = closestPoint(base, Vector3.UNIT_Z, x, y);
			p = grid(p, GRID_SIZE);
			height = Math.max(0, p.getZ() - base.getZ());
		}
		draw();
		drawWalls();
		if (container != null)
			showPoints();
//		Scene.getInstance().updateTextSizes();
	}

	protected Polygon makePolygon(ArrayList<PolygonPoint> wallUpperPoints) {
		final Polygon polygon = new Polygon(wallUpperPoints);
		for (int i = 0; i < points.size(); i++) {
			final Vector3 abspoint = getAbsPoint(i);
			polygon.addSteinerPoint(new PolygonPoint(abspoint.getX(), abspoint.getY(), abspoint.getZ()));
		}		
		return polygon;
	}

	@Override
	protected void processRoofPoints(ArrayList<PolygonPoint> wallUpperPoints, ArrayList<ReadOnlyVector3> wallNormals) {
		super.processRoofPoints(wallUpperPoints, wallNormals);
		final ReadOnlyVector3 center = getCenter();
		if (recalculateEditPoints) {
			points.get(0).set(center.getX(), center.getY(), center.getZ() + height);
			points.get(0).set(toRelative(points.get(0), container.getContainer()));
			recalculateEditPoints = false;
		} else
			points.get(0).setZ(center.getZ() + height);
	}	
	
}
