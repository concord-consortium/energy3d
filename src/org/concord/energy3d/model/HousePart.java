package org.concord.energy3d.model;

import java.util.ArrayList;

import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.extension.SwitchNode;
import com.ardor3d.scenegraph.shape.Sphere;

public abstract class HousePart {
	protected final Node root = new Node();
	protected final SwitchNode pointsRoot = new SwitchNode("Edit Points");
	protected final int numOfDrawPoints, numOfEditPoints;
	protected final ArrayList<Vector3> points;
	protected final ArrayList<HousePart> children = new ArrayList<HousePart>();
	protected boolean drawCompleted = false;
	protected int editPointIndex = -1;
	protected HousePart container = null;
	private PickResults pickResults;

	public HousePart(int numOfDrawPoints, int numOfEditPoints) {
		this.numOfDrawPoints = numOfDrawPoints;
		this.numOfEditPoints = numOfEditPoints;
		points = new ArrayList<Vector3>(numOfDrawPoints);
		// Set up a reusable pick results
		pickResults = new PrimitivePickResults();
		pickResults.setCheckDistance(true);

		final Vector3 origin = new Vector3();
		for (int i = 0; i < numOfEditPoints; i++) {
			Sphere pointShape = new Sphere("Point", origin, 20, 20, 0.1);
			pointsRoot.attachChild(pointShape);
			pointShape.setUserData(new UserData(this, i));
			pointShape.updateModelBound(); // important
		}
		root.attachChild(pointsRoot);
		allocateNewPoint();
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

	public ArrayList<Vector3> getPoints() {
		return points;
	}
	
	public void addChild(HousePart housePart) {
		children.add(housePart);
	}

	public boolean removeChild(HousePart housePart) {
		return children.remove(housePart);
	}	

	public void showPoints() {
		for (int i=0; i<points.size(); i++)
			pointsRoot.setVisible(i, true);
	}

	public void hidePoints() {
		pointsRoot.setAllNonVisible();
	}

	public void editPoint(int i) {
		editPointIndex = i;
		drawCompleted = false;
	}

	protected Vector3 closestPoint(Vector3 p1, Vector3 p2, int x, int y) {
		final Vector2 pos = Vector2.fetchTempInstance().set(x, y);
		final Ray3 pickRay = Ray3.fetchTempInstance();
		SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getPickRay(pos, false, pickRay);
		Vector2.releaseTempInstance(pos);

		Vector3 closest = closestPoint(p1, p2, pickRay.getOrigin(), pickRay.getOrigin().add(pickRay.getDirection(), null));

		Ray3.releaseTempInstance(pickRay);

		return closest;

	}
	
	protected double findHeight(Vector3 base, Vector3 upperPoint) {
		Vector3 subtract = upperPoint.subtract(base, null);
		if (subtract.dot(0, 0, -1) >= 0)
			return 0;
		else
			return subtract.length();
	}

	protected Vector3 closestPoint(ReadOnlyVector3 p1, ReadOnlyVector3 p2, ReadOnlyVector3 p3, ReadOnlyVector3 p4) {
		final double EPS = 0.0001;
		Vector3 p13, p43, p21;
		double d1343, d4321, d1321, d4343, d2121;
		double numer, denom;

		p13 = p1.subtract(p3, null);
		p43 = p4.subtract(p3, null);
		if (Math.abs(p43.getX()) < EPS && Math.abs(p43.getY()) < EPS && Math.abs(p43.getZ()) < EPS)
			return null;
		p21 = p2.subtract(p1, null);
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

		double mua = numer / denom;
		// double mub = (d1343 + d4321 * (mua)) / d4343;

		Vector3 pa = new Vector3(p1.getX() + mua * p21.getX(), p1.getY() + mua * p21.getY(), p1.getZ() + mua * p21.getZ());
		// Vector3 pb = new Vector3(p3.getX() + mub * p43.getX(), p3.getY() + mub * p43.getY(), p3.getZ() + mub * p43.getZ());

		return pa;
	}

//	protected void pick(int x, int y, Spatial target) {
//		// Put together a pick ray
//		final Vector2 pos = Vector2.fetchTempInstance().set(x, y);
//		final Ray3 pickRay = Ray3.fetchTempInstance();
//		SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getPickRay(pos, false, pickRay);
//		Vector2.releaseTempInstance(pos);
//
//		// Do the pick
////		pickResults.clear();
//		PickingUtil.findPick(target, pickRay, pickResults);
//		Ray3.releaseTempInstance(pickRay);
//	}
	
	protected PickedHousePart pick(int x, int y, Class<? extends HousePart> typeOfHousePart) {
		PickedHousePart picked = null;
		if (container == null || points.size() < 4)
			picked = SceneManager.getInstance().findMousePoint(x, y, typeOfHousePart);
		else
			picked = SceneManager.getInstance().findMousePoint(x, y, container.getRoot());
		if (picked != null)
			if (container == null || container != picked.getUserData().getHousePart()) {
				if (container != null)
					container.removeChild(this);
				container = picked.getUserData().getHousePart();
				container.addChild(this);
			}		
		return picked;
	}
	
	protected Vector3 convertToWallRelative(Vector3 p) {
		ArrayList<Vector3> wallPoints = container.getPoints();
		Vector3 origin = wallPoints.get(0);
		p = p.subtract(origin, null);
		Vector3 wallx = wallPoints.get(2).subtract(origin, null).normalize(null);
		Vector3 wally = wallPoints.get(1).subtract(origin, null).normalize(null);
		Vector3 pointOnWall = new Vector3(wallx.dot(p), 0, wally.dot(p));
		return pointOnWall;
	}

	protected Vector3 convertFromWallRelativeToAbsolute(Vector3 p) {
		ArrayList<Vector3> wallPoints = container.getPoints();
		Vector3 origin = wallPoints.get(0);
		Vector3 wallx = wallPoints.get(2).subtract(origin, null).normalize(null);
		Vector3 wally = wallPoints.get(1).subtract(origin, null).normalize(null);
		Vector3 pointOnSpace = origin.add(wallx.multiply(p.getX(), null), null).add(wally.multiply(p.getZ(), null), null);
		return pointOnSpace;
	}	
	

	protected Snap snap(Vector3 p, int index) {
		Vector3 closestPoint = null;
		double closestDistance = Double.MAX_VALUE;
		Wall closestWall = null;
		int closestPointIndex = -1;
		for (HousePart housePart : House.getInstance().getParts()) {
			if (housePart instanceof Wall && housePart != this) {
				Wall wall = (Wall) housePart;
				int i = 0;
				for (Vector3 p2 : wall.getPoints()) {
					double distance = p.distance(p2);
					if (distance < closestDistance) {
						closestPoint = p2;
						closestDistance = distance;
						closestWall = wall;
						closestPointIndex = i;
					}
					i++;
				}
			}
		}
		if (closestDistance < 0.5) {
			p.set(closestPoint);
			return new Snap(closestWall, index, closestPointIndex);
		} else
			return null;
	}

	public void addPoint(int x, int y) {
		if (drawCompleted)
			throw new RuntimeException("Drawing of this object is already completed");

		if (points.size() >= numOfEditPoints)
			drawCompleted = true;
		else {
			allocateNewPoint();
			setPreviewPoint(x, y);
		}
	}	

	private void allocateNewPoint() {
		Vector3 p = new Vector3();
		for (int i=0; i<numOfEditPoints/numOfDrawPoints; i++)
			points.add(p);
	}

	public abstract void setPreviewPoint(int x, int y);

	protected abstract void draw();

}
