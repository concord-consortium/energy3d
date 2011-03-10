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
		} else if (editPointIndex > 0) { // 1 || editPointIndex == 2) {
			final Ray3 pickRay = SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getPickRay(new Vector2(x, y), false, null);
			Vector3 p = new Vector3();
			if (pickRay.intersectsPlane(new Plane(Vector3.UNIT_Z, 2.0), p)) {
				System.out.println("moving to: " + p);
				// Vector3 p = closestPoint(points.get(0), Vector3.UNIT_Y, x, y);
				p = grid(p, GRID_SIZE);
				points.get(editPointIndex).set(p);
			}
		}
		draw();
		if (container != null)
			showPoints();

	}

	protected Polygon makePolygon(ArrayList<PolygonPoint> wallUpperPoints) {
		// upper points
		points.get(0).set(center.getX(), center.getY(), center.getZ() + height);
		// if (editPointIndex == -1) {
		// points.get(1).set(center.getX(), center.getY()-1, center.getZ() + height);
		// points.get(2).set(center.getX(), center.getY()+1, center.getZ() + height);
		// } else {
		// points.get(1).setZ(center.getZ() + height);
		// points.get(2).setZ(center.getZ() + height);
		// }
		final Polygon polygon = new Polygon(wallUpperPoints);
		for (int i = 1; i < points.size(); i++) {
			final Vector3 p = points.get(i);
			polygon.addSteinerPoint(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
		}
		return polygon;
	}

	protected void processRoofPoints(ArrayList<PolygonPoint> wallUpperPoints, ArrayList<ReadOnlyVector3> wallNormals) {
		// shift the wall according to edgeLength
		final double edgeLenght = 0.3;
		final Vector3 op = new Vector3();
//		for (PolygonPoint p : wallUpperPoints) {
		for (int i = 0; i < wallUpperPoints.size(); i++) {
			final PolygonPoint p = wallUpperPoints.get(i);
//			op.set(p.getX(), p.getY(), 0).subtractLocal(center.getX(), center.getY(), 0).normalizeLocal().multiplyLocal(edgeLenght);
			op.set(wallNormals.get(i)).multiplyLocal(edgeLenght);
			op.addLocal(p.getX(), p.getY(), p.getZ());
//			p.set(op.getX(), op.getY(), op.getZ() + 0.01);
			p.set(op.getX(), op.getY(), op.getZ());
		}
		final double z = 2; // center.getZ() + height;
		points.get(0).set(center.getX(), center.getY(), z);

		if (wallUpperPoints.size() > points.size()) {
			final Vector3 v = new Vector3();
//			System.out.println("Roof Edit Points:");
			for (int i = 0; i < wallUpperPoints.size() - 1; i++) {
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
//				System.out.println(v);
			}
		}
	}

}
