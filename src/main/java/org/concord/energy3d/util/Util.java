package org.concord.energy3d.util;

import java.util.List;

import org.concord.energy3d.scene.SceneManager;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.bounding.BoundingVolume.Type;
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
		return 2 * bounds.asType(Type.Sphere).getRadius();
	}

	/* require that a and b are normalized */
	public static double angleBetween(final ReadOnlyVector3 a, final ReadOnlyVector3 b, final ReadOnlyVector3 n) {
		return Math.atan2(b.dot(n.cross(a, null)), b.dot(a));
	}

	public static String toString(final ReadOnlyVector3 v) {
		final double C = 1000.0;
		return "(" + Math.round(v.getX()*C) / C + ", " + Math.round(v.getY()*C) / C + ", " + Math.round(v.getZ()*C) / C + ")";
	}

	public static void disablePickShadowLight(final Spatial spatial) {
		spatial.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		spatial.getSceneHints().setCastsShadows(false);
		spatial.getSceneHints().setLightCombineMode(LightCombineMode.Off);
	}

	public static void disablePickShadowLight(final Spatial spatial, final boolean disablePick) {
		spatial.getSceneHints().setPickingHint(PickingHint.Pickable, !disablePick);
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

	private static double area(final double x1, final double y1, final double x2, final double y2, final double x3, final double y3)
	{
	   return Math.abs((x1*(y2-y3) + x2*(y3-y1)+ x3*(y1-y2))/2.0);
	}

	/* A function to check whether point P(x, y) lies inside the triangle formed by A(x1, y1), B(x2, y2) and C(x3, y3) */
	public static boolean isPointInsideTriangle(final ReadOnlyVector2 p, final ReadOnlyVector2 p1, final ReadOnlyVector2 p2, final ReadOnlyVector2 p3)
	{
	   final double A = area (p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY());
	   final double A1 = area (p.getX(), p.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY());
	   final double A2 = area (p1.getX(), p1.getY(), p.getX(), p.getY(), p3.getX(), p3.getY());
	   final double A3 = area (p1.getX(), p1.getY(), p2.getX(), p2.getY(), p.getX(), p.getY());
	   return isEqual(A1 + A2 + A3, A);
	}

	public static Vector3 closestPoint(final ReadOnlyVector3 p1, final ReadOnlyVector3 v1, final int x, final int y) {
		final Ray3 pickRay = SceneManager.getInstance().getCamera().getPickRay(new Vector2(x, y), false, null);
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

	public static double round(final double x) {
		return Math.round(x * 100.0) / 100.0;
	}

	public static ReadOnlyVector3 closestPointBetweenTwoLines(final ReadOnlyVector3 p1, final ReadOnlyVector3 dir1, final ReadOnlyVector3 p2, final ReadOnlyVector3 dir2)
	{
		final ReadOnlyVector3 u = dir1;
		final ReadOnlyVector3 v = dir2;
		final ReadOnlyVector3 w = p1.subtract(p2, null);
	    final double a = u.dot(u);         // always >= 0
	    final double b = u.dot(v);
	    final double c = v.dot(v);         // always >= 0
	    final double d = u.dot(w);
	    final double e = v.dot(w);
	    final double D = a*c - b*b;        // always >= 0
	    double sc;

	    if (Util.isZero(D))          // the lines are almost parallel
	        sc = 0.0;
	    else
	        sc = (b*e - c*d) / D;

	    return u.multiply(sc, null).addLocal(p1);
	}
	
	public static boolean isEqual(final ReadOnlyVector3 a, final ReadOnlyVector3 b) {
		return isZero(a.distance(b));
	}

	public static boolean isEqual(final double a, final double b) {
		return isZero(a - b);
	}

	public static boolean isZero(final double x) {
		return Math.abs(x) < MathUtils.ZERO_TOLERANCE;
	}
}
