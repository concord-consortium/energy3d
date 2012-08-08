package org.concord.energy3d.model;

import java.util.ArrayList;
import java.util.List;

import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.triangulation.point.ardor3d.ArdorVector3PolygonPoint;

import com.ardor3d.math.Plane;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
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
			final Vector3 p = Util.closestPoint(base, Vector3.UNIT_Z, x, y);
			if (p == null)
				return;
			snapToGrid(p, getAbsPoint(editPointIndex), getGridSize());
			height = Math.max(0, p.getZ() - container.getPoints().get(1).getZ());
			final double z = container.getPoints().get(1).getZ() + height;
			for (final Vector3 v : points)
				v.setZ(z);
		} else {
			final Ray3 pickRay = SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getPickRay(new Vector2(x, y), false, null);
			final Vector3 p = new Vector3();
			if (pickRay.intersectsPlane(new Plane(Vector3.UNIT_Z, points.get(0).getZ()), p)) {
				snapToGrid(p, getAbsPoint(editPointIndex), getGridSize(), false);
				snapToWallsPolygon(p);
				points.get(editPointIndex).set(toRelative(p, container.getContainer()));
			}
		}
		if (container != null)
			setEditPointsVisible(true);
		draw();
		drawWalls();
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
	protected void processRoofEditPoints(final List<? extends ReadOnlyVector3> wallUpperPoints) {
		if (recalculateEditPoints) {
			recalculateEditPoints = false;
			points.clear();

			points.add(toRelative(getCenter(), container.getContainer()));

			// add or update edit points
			final int n = wallUpperPoints.size();
//			if (n > points.size()) {
				for (int i = 0; i < n; i++) {
					final ReadOnlyVector3 p1 = wallUpperPoints.get(i);
					final ReadOnlyVector3 p2 = wallUpperPoints.get((i + 1) % n);
					// middle of wall = (p1 + p2) / 2
					final Vector3 v = new Vector3(p1.getX() + p2.getX(), p1.getY() + p2.getY(), 0).multiplyLocal(0.5);
					// add -normal*0.2 to middle point of wall
					final Wall wall = findGableWall(p1, p2);
					if (wall != null) {
						final ReadOnlyVector3 normal = wall.getFaceDirection();
						v.addLocal(normal.multiply(0.2, null).negateLocal());
					}
					v.set(toRelative(v, container.getContainer()));
					points.add(v);
				}
//			}

			computeHeight(wallUpperPoints);
			applyHeight();
		} else {
			applyHeight();
		}
	}

	@Override
	protected void setHeight(final double newHeight, final boolean finalize) {
		super.setHeight(newHeight, finalize);
		for (final Vector3 p : points)
			p.setZ(container.getPoints().get(1).getZ() + newHeight);
	}
}
