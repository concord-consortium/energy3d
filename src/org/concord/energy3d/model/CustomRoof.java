package org.concord.energy3d.model;

import java.util.ArrayList;

import org.concord.energy3d.scene.SceneManager;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;

import com.ardor3d.math.Plane;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;

public class CustomRoof extends Roof {
	private static final long serialVersionUID = 1L;
	private static final double GRID_SIZE = 0.5;

	public CustomRoof() {
		super(1, 1, 0.5);		
	}

	public void setPreviewPoint(int x, int y) {
		if (editPointIndex == -1) {
			pick(x, y, Wall.class);
		} else if (editPointIndex == 0) {
			Vector3 base = center;
			Vector3 p = closestPoint(base, Vector3.UNIT_Z, x, y);
			p = grid(p, GRID_SIZE);
			height = Math.max(0, p.getZ() - base.getZ());
			final double z = center.getZ() + height;
			for (final Vector3 v : points)
				v.setZ(z);
		} else if (editPointIndex > 0) {
			final Ray3 pickRay = SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getPickRay(new Vector2(x, y), false, null);
			Vector3 p = new Vector3();
			if (pickRay.intersectsPlane(new Plane(Vector3.UNIT_Z, points.get(0).getZ()), p)) {
				p = grid(p, GRID_SIZE);
				points.get(editPointIndex).set(p);
			}
		}
		draw();
		if (container != null)
			showPoints();

	}

	protected Polygon makePolygon(ArrayList<PolygonPoint> wallUpperPoints) {
		final Polygon polygon = new Polygon(wallUpperPoints);
		for (int i = 1; i < points.size(); i++) {
			final Vector3 p = points.get(i);
			polygon.addSteinerPoint(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
		}
		return polygon;
	}

	protected void processRoofPoints(ArrayList<PolygonPoint> wallUpperPoints, ArrayList<ReadOnlyVector3> wallNormals) {
		super.processRoofPoints(wallUpperPoints, wallNormals);
		
		// add or update edit points
		final double z = center.getZ() + height;
		if (wallUpperPoints.size() > points.size()) {
			final Vector3 v = new Vector3();
			for (int i = 0; i < wallUpperPoints.size() - 1; i = i + 1) {
				final PolygonPoint p1 = wallUpperPoints.get(i);
				final PolygonPoint p2 = wallUpperPoints.get(i + 1);
				// middle of wall = (p1 + p2) / 2
				v.set(p1.getX() + p2.getX(), p1.getY() + p2.getY(), 0).multiplyLocal(0.5);
				v.setZ(z);
				// add -normal*0.2 to middle point of wall
				v.addLocal(wallNormals.get(i).multiply(0.2, null).negateLocal());
				if (i + 1 < points.size())
					points.get(i + 1).set(v);
				else
					points.add(v.clone());
			}
		}
	}

	@Override
	protected void setHeight(double newHeight, boolean finalize) {
		super.setHeight(newHeight, finalize);
		for (final Vector3 p : points)
			p.setZ(center.getZ() + newHeight);
	}
	
	

}
