package org.concord.energy3d.model;

import java.util.ArrayList;

import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.shape.Sphere;

public abstract class HousePart {
	protected final Node root = new Node();
	protected final Node pointsRoot = new Node();
	protected boolean drawCompleted = false;
	protected final ArrayList<Vector3> points;
	protected final int numOfDrawPoints, numofEditPoints;
	protected int editPointIndex = -1;

	public HousePart(int x, int y, int numOfDrawPoints, int numOfEditPoints) {
		this.numOfDrawPoints = numOfDrawPoints;
		this.numofEditPoints = numOfEditPoints;
		points = new ArrayList<Vector3>(numOfDrawPoints);
//		points.add(firstPoint);
		final Vector3 origin = new Vector3();		
		for (int i = 0; i < numOfEditPoints; i++) {
			Sphere pointShape = new Sphere("Point", origin, 5, 5, 0.1);
//			pointShape.setTranslation(firstPoint);
			pointsRoot.attachChild(pointShape);
			pointShape.setUserData(i);
			pointShape.updateModelBound();

		}
		root.attachChild(pointsRoot);
		root.setUserData(this);
		addPoint(x, y);
	}

	public Node getRoot() {
		return root;
	}

	public void complete() {
		drawCompleted = true;
	}

	public boolean isDrawCompleted() {
		return drawCompleted;
	}

	public void showPoints() {
		root.attachChild(pointsRoot);
	}

	public void hidePoints() {
		root.detachChild(pointsRoot);
	}

//	public void addPoint(Vector3 p) {
//		if (drawCompleted)
//			throw new RuntimeException("Drawing of this object is already completed");
//		if (editPointIndex == -1) {
//			draw(p, points.size());
//			points.add(p);
//		} else
//			draw(p, editPointIndex);
//
//		if (points.size() >= numOfDrawPoints)
//			drawCompleted = true;
//	}

	public void addPoint(int x, int y) {
		if (drawCompleted)
			throw new RuntimeException("Drawing of this object is already completed");
			
		Vector3 p = SceneManager.getInstance().findMousePoint(x, y);
			draw();
			points.add(p);

		if (points.size() >= numOfDrawPoints)
			drawCompleted = true;
	}
	
	public void editPoint(int i) {
		editPointIndex = i;
		drawCompleted = false;
	}

	public void setPreviewPoint(int x, int y) {
//		if (drawCompleted)
//			throw new RuntimeException("Drawing of this object is already completed");
//		if (editPointIndex == -1)
////			draw(p, points.size());
////			draw();
//			points.set(points.size()-1, p);
//		else
////			draw(p, editPointIndex);
//			points.set(editPointIndex, p);
//			
//		draw();
	}
	
	protected double findAltitude(Vector3 p, int x, int y) {
		final Vector2 pos = Vector2.fetchTempInstance().set(x, y);
		final Ray3 pickRay = Ray3.fetchTempInstance();
		SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getPickRay(pos, false, pickRay);
		Vector2.releaseTempInstance(pos);
		
		Vector3 closest = LineLineIntersect(p, p.add(new Vector3(0, 0, 1), null), pickRay.getOrigin(), pickRay.getOrigin().add(pickRay.getDirection(), null));
		
		Ray3.releaseTempInstance(pickRay);
		
		Vector3 subtract = closest.subtract(p, null);
		if (subtract.dot(0, 0, -1) >= 0)
			return 0.1;
		else
			return subtract.length();

	}

	Vector3 LineLineIntersect(
			ReadOnlyVector3 p1,ReadOnlyVector3 p2,ReadOnlyVector3 p3,ReadOnlyVector3 p4)
			{
		final double EPS = 0.0001;
		Vector3 p13,p43,p21;
			   double d1343,d4321,d1321,d4343,d2121;
			   double numer,denom;

			   p13 = p1.subtract(p3, null);
			   p43 = p4.subtract(p3, null);
			   if (Math.abs(p43.getX()) < EPS && Math.abs(p43.getY()) < EPS && Math.abs(p43.getZ())  < EPS)
			      return null;
			   p21 = p2.subtract(p1, null);
			   if (Math.abs(p21.length())  < EPS)
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

			   double mua = numer / denom;
			   double mub = (d1343 + d4321 * (mua)) / d4343;

			   Vector3 pa = new Vector3(p1.getX() + mua * p21.getX(), p1.getY() + mua * p21.getY(), p1.getZ() + mua * p21.getZ());
//			   Vector3 pb = new Vector3(p3.getX() + mub * p43.getX(), p3.getY() + mub * p43.getY(), p3.getZ() + mub * p43.getZ());

			   return pa;
			}	
	
//	protected abstract void draw(Vector3 p, int i);


	protected abstract void draw();

}
