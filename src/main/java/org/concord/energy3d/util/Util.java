package org.concord.energy3d.util;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.SceneManager;
import org.poly2tri.geometry.polygon.PolygonPoint;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector2;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.AutoFade;
import com.ardor3d.ui.text.BMText.AutoScale;

public class Util {

	public static double findBoundLength(final BoundingVolume bounds) {
		double d;
		if (bounds instanceof BoundingSphere)
			d = ((BoundingSphere)bounds).getRadius() * 2;
		else {
			final BoundingBox boundingBox = (BoundingBox)bounds;
			d = 2 * Math.max(boundingBox.getXExtent(), Math.max(boundingBox.getYExtent(), boundingBox.getZExtent()));
		}
		return d;
	}

	public static double findExactHeight(final ArrayList<HousePart> parts) {
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (final HousePart part : parts) {
			for (int i = 0; i < part.getPoints().size(); i++) {
				final Vector3 p = part.getAbsPoint(i);
				final double z = p.getZ();
				if (z > max)
					max = z;
				if (z < min)
					min = z;
			}
		}
		return max - min;
	}

	/** require that a and b are normalized **/
	public static double angleBetween(final ReadOnlyVector3 a, final ReadOnlyVector3 b, final ReadOnlyVector3 n) {
		return Math.atan2(b.dot(n.cross(a, null)), b.dot(a));
	}

	public static long degree(final double radian) {
		return Math.round(radian * 180.0 / Math.PI);
	}

	public static String toString(final ReadOnlyVector3 v) {
		return "(" + Math.round(v.getX()*100) / 100.0 + ", " + Math.round(v.getY()*100) / 100.0 + ", " + Math.round(v.getZ()*100) / 100.0 + ")";
	}

	public static String toString(final double v) {
		return "" + Math.round(v*100) / 100.0;
	}

	public static Vector3 get(final Vector3 p, final FloatBuffer buf) {
		return p.set(buf.get(), buf.get(), buf.get());
	}

	public static void put(final Vector3 p, final FloatBuffer buf) {
		buf.put(p.getXf()).put(p.getYf()).put(p.getZf());
	}

	public static PolygonPoint toPolygonPoint(final Vector3 p) {
		return new PolygonPoint(p.getX(), p.getY(), p.getZ());
	}

	public static void disablePickShadowLight(final Spatial spatial) {
		spatial.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		spatial.getSceneHints().setCastsShadows(false);
		spatial.getSceneHints().setLightCombineMode(LightCombineMode.Off);
	}

	public static boolean insidePolygon(final ReadOnlyVector3 p, final List<? extends ReadOnlyVector3> polygon) {
		int counter = 0;
		int i;
		double xinters;
		ReadOnlyVector3 p1, p2;

		final int n = polygon.size();
		p1 = polygon.get(0);
		for (i = 1; i <= n; i++) {
			p2 = polygon.get(i % n);
			if (p.getY() > Math.min(p1.getY(), p2.getY())) {
				if (p.getY() <= Math.max(p1.getY(), p2.getY())) {
					if (p.getX() <= Math.max(p1.getX(), p2.getX())) {
						if (p1.getY() != p2.getY()) {
							xinters = (p.getY() - p1.getY()) * (p2.getX() - p1.getX()) / (p2.getY() - p1.getY()) + p1.getX();
							if (p1.getX() == p2.getX() || p.getX() <= xinters)
								counter++;
						}
					}
				}
			}
			p1 = p2;
		}

		if (counter % 2 == 0)
			return false;
		else
			return true;
	}

	public static Vector3 closestPoint(final ReadOnlyVector3 p1, final ReadOnlyVector3 v1, final int x, final int y) {
		final Ray3 pickRay = SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getPickRay(new Vector2(x, y), false, null);
		final Vector3 closest = closestPoint(p1, v1, pickRay.getOrigin(), pickRay.getDirection());
		return closest;
	}

	public static Vector3 closestPoint(final ReadOnlyVector3 p1, final ReadOnlyVector3 p21, final ReadOnlyVector3 p3, final ReadOnlyVector3 p43) {
		final double EPS = 0.0001;
		Vector3 p13;
		double d1343, d4321, d1321, d4343, d2121;
		double numer, denom;

		p13 = p1.subtract(p3, null);
		if (Math.abs(p43.getX()) < EPS && Math.abs(p43.getY()) < EPS && Math.abs(p43.getZ()) < EPS)
			return null;
		if (Math.abs(p21.length()) < EPS)
			return null;

		d1343 = p13.getX() * p43.getX() + p13.getY() * p43.getY() + p13.getZ() * p43.getZ();
		d4321 = p43.getX() * p21.getX() + p43.getY() * p21.getY() + p43.getZ() * p21.getZ();
		d1321 = p13.getX() * p21.getX() + p13.getY() * p21.getY() + p13.getZ() * p21.getZ();
		d4343 = p43.getX() * p43.getX() + p43.getY() * p43.getY() + p43.getZ() * p43.getZ();
		d2121 = p21.getX() * p21.getX() + p21.getY() * p21.getY() + p21.getZ() * p21.getZ();

		denom = d2121 * d4343 - d4321 * d4321;
		if (Math.abs(denom) < EPS)
			return null;
		numer = d1343 * d4321 - d1321 * d4343;

		final double mua = numer / denom;
		final Vector3 pa = new Vector3(p1.getX() + mua * p21.getX(), p1.getY() + mua * p21.getY(), p1.getZ() + mua * p21.getZ());

		return pa;
	}

	public static Vector2 closestPoint(final ReadOnlyVector2 p1, final ReadOnlyVector2 p2, final ReadOnlyVector2 p) {
		final double l2 = p1.distanceSquared(p2);
		if (l2 == 0.0)
			return p1.clone();
		final double t = p.subtract(p1, null).dot(p2.subtract(p1, null)) / l2; // dot(p - v, w - v) / l2;
		if (t < 0.0)
			return p1.clone();
		else if (t > 1.0)
			return p2.clone();
		else
			return p2.subtract(p1, null).multiplyLocal(t).addLocal(p1); // v + t * (w - v);
	}

	public static Vector2 snapToPolygon(final ReadOnlyVector3 point, final List<? extends ReadOnlyVector3> polygon, final List<? extends ReadOnlyVector3> wallNormals) {
		final Vector2 p = new Vector2(point.getX(), point.getY());
		final Vector2 l1 = new Vector2();
		final Vector2 l2 = new Vector2();
		double shortestDistance = Double.MAX_VALUE;
		Vector2 closestPoint = null;
		ReadOnlyVector3 closestNormal = null;
		final int n = polygon.size();
		for (int i = 0; i < n; i++) {
			final ReadOnlyVector3 pp1 = polygon.get(i);
			l1.set(pp1.getX(), pp1.getY());
			final ReadOnlyVector3 pp2 = polygon.get((i + 1) % n);
			l2.set(pp2.getX(), pp2.getY());
			if (l1.distanceSquared(l2) > MathUtils.ZERO_TOLERANCE) {
				final Vector2 pointOnLine = closestPoint(l1, l2, p);
				final double distance = pointOnLine.distanceSquared(p);
				if (distance < shortestDistance) {
					shortestDistance = distance;
					closestPoint = pointOnLine;
					if (l1.distanceSquared(closestPoint) <= l2.distanceSquared(pointOnLine))
						closestNormal = wallNormals.get(i);
					else
						closestNormal = wallNormals.get((i + 1) % n);
				}
			}
		}

		closestPoint.addLocal(-closestNormal.getX() / 100.0, -closestNormal.getY() / 100.0);
		return closestPoint;
	}

	public static Vector3 intersectLineSegments(final ReadOnlyVector3 a1, final ReadOnlyVector3 a2, final ReadOnlyVector3 b1, final ReadOnlyVector3 b2) {
	    final double ua_t = (b2.getX() - b1.getX()) * (a1.getY() - b1.getY()) - (b2.getY() - b1.getY()) * (a1.getX() - b1.getX());
	    final double ub_t = (a2.getX() - a1.getX()) * (a1.getY() - b1.getY()) - (a2.getY() - a1.getY()) * (a1.getX() - b1.getX());
	    final double u_b  = (b2.getY() - b1.getY()) * (a2.getX() - a1.getX()) - (b2.getX() - b1.getX()) * (a2.getY() - a1.getY());

	    if ( u_b != 0 ) {
	    	final double ua = ua_t / u_b;
	    	final double ub = ub_t / u_b;

	        if ( 0 <= ua && ua <= 1 && 0 <= ub && ub <= 1 )
	            return new Vector3(a1.getX() + ua * (a2.getX() - a1.getX()), a1.getY() + ua * (a2.getY() - a1.getY()), a1.getZ());
	        else
	            return null;
	    } else
	    	return null;
	}

	public static void initHousePartLabel(final BMText label) {
		label.setFontScale(0.6);
		label.setAutoScale(AutoScale.FixedScreenSize);
		label.setAutoFade(AutoFade.Off);
	}
}
