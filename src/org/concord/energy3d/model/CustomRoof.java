package org.concord.energy3d.model;

import java.util.ArrayList;
import java.util.List;

import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.MeshLib;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.triangulation.point.ardor3d.ArdorVector3PolygonPoint;

import com.ardor3d.math.Plane;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector2;
import com.ardor3d.math.type.ReadOnlyVector3;

public class CustomRoof extends Roof {
	private static final long serialVersionUID = 1L;
	private transient boolean recalculateEditPoints;

	public CustomRoof() {
		super(1, 1, 0.5);
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		if (editPointIndex == -1) {
			recalculateEditPoints = true;
			pickContainer(x, y, Wall.class);
		} else if (editPointIndex == 0) {
			final ReadOnlyVector3 base = getCenter();
			Vector3 p = MeshLib.closestPoint(base, Vector3.UNIT_Z, x, y);
			if (p == null)
				return;
			p = grid(p, getAbsPoint(editPointIndex), getGridSize());
			height = Math.max(0, p.getZ() - container.getPoints().get(1).getZ());
			final double z = container.getPoints().get(1).getZ() + height;
			for (final Vector3 v : points)
				v.setZ(z);
		} else {
			final Ray3 pickRay = SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getPickRay(new Vector2(x, y), false, null);
			Vector3 p = new Vector3();
			if (pickRay.intersectsPlane(new Plane(Vector3.UNIT_Z, points.get(0).getZ()), p)) {
				p = grid(p, getAbsPoint(editPointIndex), getGridSize(), false);
				if (!MeshLib.insidePolygon(p, wallUpperPoints)) {
					final ReadOnlyVector2 p2D = MeshLib.snapToPolygon(p, wallUpperPoints, wallNormals);
					p.set(p2D.getX(), p2D.getY(), p.getZ());
				}
				points.get(editPointIndex).set(toRelative(p, container.getContainer()));
			}
		}
		if (container != null)
			setEditPointsVisible(true);
		draw();
		drawWalls();
//		if (container != null)
//			setEditPointsVisible(true);
	}

	@Override
	protected Polygon applySteinerPoint(final Polygon polygon) {
		final ArrayList<Vector3> steinerPoints = new ArrayList<Vector3>(points.size());
		for (int i = 1; i < points.size(); i++) {
			final Vector3 p = getAbsPoint(i);
			if (!steinerPoints.contains(p)) {
				steinerPoints.add(p);
				polygon.addSteinerPoint(new ArdorVector3PolygonPoint(p));
			}
		}
		return polygon;
	}

	@Override
	protected void processRoofPoints(final List<? extends ReadOnlyVector3> wallUpperPoints) {
		if (recalculateEditPoints) {
			recalculateEditPoints = false;
			points.clear();

			points.add(toRelative(getCenter(), container.getContainer()).addLocal(0, 0, height));

			// add or update edit points
			final double z = container.getPoints().get(1).getZ() + height;
			if (wallUpperPoints.size() > points.size()) {
				for (int i = 0; i < wallUpperPoints.size() - 1; i++) {
					final ReadOnlyVector3 p1 = wallUpperPoints.get(i);
					final ReadOnlyVector3 p2 = wallUpperPoints.get(i + 1);
					// middle of wall = (p1 + p2) / 2
					final Vector3 v = new Vector3(p1.getX() + p2.getX(), p1.getY() + p2.getY(), 0).multiplyLocal(0.5);
					v.setZ(z);
					// add -normal*0.2 to middle point of wall
					v.addLocal(wallNormals.get(i).multiply(0.2, null).negateLocal());
					v.set(toRelative(v, container.getContainer()));
					points.add(v);
				}
			}
		} else {
			for (final Vector3 p : points)
				p.setZ(container.getPoints().get(1).getZ() + height);
		}
	}

	@Override
	protected void setHeight(final double newHeight, final boolean finalize) {
		super.setHeight(newHeight, finalize);
		for (final Vector3 p : points)
			p.setZ(container.getPoints().get(1).getZ() + newHeight);
	}
}
