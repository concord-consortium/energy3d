package org.concord.energy3d.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.poly2tri.polygon.PolygonPoint;

import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Sphere;

public abstract class HousePart implements Serializable {
	private static final long serialVersionUID = 1L;
	private static double flattenPos = 3;
	private static boolean snapToObjects = true;
	private static boolean snapToGrids = false;	
	protected transient Node root; // = new Node();
	protected transient Node pointsRoot; // = new SwitchNode("Edit Points");
	protected final int numOfDrawPoints, numOfEditPoints;
	protected final ArrayList<Vector3> points;
	protected final ArrayList<HousePart> children = new ArrayList<HousePart>();
	protected HousePart container = null;
	protected boolean drawCompleted = false;
	protected int editPointIndex = -1;
	private transient PickResults pickResults;
	private boolean firstPointInserted = false;
	protected transient ArrayList<Vector3> abspoints;
	protected double height;
	protected transient double orgHeight;
	protected boolean relativeToHorizontal;
	private double pos;

	public static boolean isSnapToObjects() {
		return snapToObjects;
	}
	
	public static void setSnapToObjects(boolean snapToObjects) {
		HousePart.snapToObjects = snapToObjects;
	}

	public static boolean isSnapToGrids() {
		return snapToGrids;
	}

	public static void setSnapToGrids(boolean snapToGrid) {
		HousePart.snapToGrids = snapToGrid;
	}
	
	public HousePart(int numOfDrawPoints, int numOfEditPoints, double height) {		
		this(numOfDrawPoints, numOfEditPoints, height, false);
	}

	public HousePart(int numOfDrawPoints, int numOfEditPoints, double height, boolean relativeToHorizontal) {
		// System.out.println("Creating " + this + "...");
		this.numOfDrawPoints = numOfDrawPoints;
		this.numOfEditPoints = numOfEditPoints;
		this.height = this.orgHeight = height;
		this.relativeToHorizontal = relativeToHorizontal;		
		points = new ArrayList<Vector3>(numOfEditPoints);
		init();

		allocateNewPoint();

	}

	protected void init() {
		pos = flattenPos++;
		orgHeight = height;
		abspoints = new ArrayList<Vector3>(numOfEditPoints);
		for (int i = 0; i < points.size(); i++)
			abspoints.add(points.get(i).clone());
		root = new Node(toString());		
		pointsRoot = new Node("Edit Points");

		// Set up a reusable pick results
		pickResults = new PrimitivePickResults();
		pickResults.setCheckDistance(true);

		// hidePoints();
		final Vector3 origin = new Vector3();
		for (int i = 0; i < numOfEditPoints; i++) {
			Sphere pointShape = new Sphere("Point", origin, 8, 8, 0.05);
			pointsRoot.attachChild(pointShape);
			pointShape.setUserData(new UserData(this, i));
//			pointShape.setModelBound(new BoundingBox());
			pointShape.updateModelBound(); // important
			pointShape.getSceneHints().setCullHint(CullHint.Always);
			pointShape.setCastsShadows(false);
			// pointShape.updateWorldBound(true);
		}
		// pointsRoot.setAllVisible();
		// pointsRoot.updateWorldBound(false);
		root.attachChild(pointsRoot);
		// pointsRoot.setAllNonVisible();
		// root.updateGeometricState(0);
	}
	
	private void initCheck() {
		if (root == null) {
			init();
			draw();
		}		
	}

	public Node getRoot() {
		initCheck();
		return root;
	}

	public void complete() {
		drawCompleted = true;
	}

	public boolean isDrawCompleted() {
		return drawCompleted;
	}

	public boolean isFirstPointInserted() {
		return firstPointInserted;
	}

	public ArrayList<Vector3> getPoints() {
		initCheck();	
		return abspoints;
	}

	public void addChild(HousePart housePart) {
		children.add(housePart);
	}

	public boolean removeChild(HousePart housePart) {
		return children.remove(housePart);
	}

	public ArrayList<HousePart> getChildren() {
		return children;
	}
	
	public double getHeight() {
		return orgHeight;
	}

	public void setHeight(double newHeight, boolean finalize) {
			this.height = newHeight;
			if (finalize)
				this.orgHeight = newHeight;
		}

	public void showPoints() {
		for (int i = 0; i < points.size(); i++)
			pointsRoot.getChild(i).getSceneHints().setCullHint(CullHint.Inherit);
	}

	public void hidePoints() {
		for (int i = 0; i < points.size(); i++)
			pointsRoot.getChild(i).getSceneHints().setCullHint(CullHint.Always);
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
//		Vector3 subtract = upperPoint.subtract(base, null);
//		if (subtract.dot(0, 0, -1) >= 0)
//			return 0;
//		else
//			return subtract.length();
		double subtract = upperPoint.getZ() - base.getZ();
		return Math.max(0, subtract);
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

	// protected void pick(int x, int y, Spatial target) {
	// // Put together a pick ray
	// final Vector2 pos = Vector2.fetchTempInstance().set(x, y);
	// final Ray3 pickRay = Ray3.fetchTempInstance();
	// SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getPickRay(pos, false, pickRay);
	// Vector2.releaseTempInstance(pos);
	//
	// // Do the pick
	// // pickResults.clear();
	// PickingUtil.findPick(target, pickRay, pickResults);
	// Ray3.releaseTempInstance(pickRay);
	// }

	protected PickedHousePart pick(int x, int y) {
		return pick(x, y, (Class<? extends HousePart>) null);
	}

	protected PickedHousePart pick(int x, int y, Class<?>[] typesOfHousePart) {
		for (Class<?> c : typesOfHousePart) {
			PickedHousePart picked = pick(x, y, c);
			if (picked != null)
				return picked;
		}
		return null;
	}

	protected PickedHousePart pick(int x, int y, Class<?> typeOfHousePart) {
		PickedHousePart picked = null;
		// if (container == null || points.size() < 4)
		if (!firstPointInserted)
			picked = SceneManager.getInstance().findMousePoint(x, y, typeOfHousePart);
		else
			picked = SceneManager.getInstance().findMousePoint(x, y, container == null ? null : container.getRoot());

		if (!firstPointInserted) {
			UserData userData = null;
			if (picked != null)
				userData = picked.getUserData();
			if (container == null || userData == null || container != userData.getHousePart()) {
				if (container != null)
					container.removeChild(this);
				if (userData != null && userData.getHousePart().isDrawCompleted()) {
					container = userData.getHousePart();
					container.addChild(this);
				} else
					container = null;
			}
		}
		return picked;
	}

	protected Vector3 toRelative(Vector3 org) {
		if (container == null)
			return org;
		ArrayList<Vector3> wallPoints = container.getPoints();
		Vector3 origin = wallPoints.get(0);
//		p = p.subtract(origin, null);
		Vector3 p = org.subtract(origin, null); //.normalizeLocal();
		Vector3 wallx = wallPoints.get(2).subtract(origin, null);//.normalize(null);
		Vector3 wally = wallPoints.get(1).subtract(origin, null);//.normalize(null);
//		Vector3 pointOnWall = new Vector3(wallx.dot(p), 0, wally.dot(p));
//		Vector3 pointOnWall = new Vector3(wallx.dot(p), (relativeToHorizontal) ? wally.dot(p): p.getY(), (relativeToHorizontal) ? p.getZ() : wally.dot(p));
//		double y = p.getY();
//		if (wallx.getX() == 0)
//			y = p.getX();
		Vector3 pointOnWall = new Vector3(wallx.getX() == 0 ? p.getY() / wallx.getY() : p.getX() / wallx.getX(), (relativeToHorizontal) ? p.getY() / wally.getY(): org.getY(), (relativeToHorizontal) ? org.getZ() : p.getZ() / wally.getZ());
		return pointOnWall;
	}

	protected Vector3 toAbsolute(Vector3 p) {
		if (container == null)
			return p;
		ArrayList<Vector3> containerPoints = container.getPoints();
		Vector3 origin = containerPoints.get(0);
		Vector3 wallx = containerPoints.get(2).subtract(origin, null); //.normalize(null);
		Vector3 wally = containerPoints.get(1).subtract(origin, null); //.normalize(null);
//		Vector3 pointOnSpace = origin.add(wallx.multiply(p.getX(), null), null).add(wally.multiply(p.getZ(), null), null);
		Vector3 pointOnSpace = origin.add(wallx.multiply(p.getX(), null), null).add(wally.multiply((relativeToHorizontal) ? p.getY() : p.getZ(), null), null);
		if (relativeToHorizontal)
			pointOnSpace.setZ(pointOnSpace.getZ() + p.getZ());
//		else
//			pointOnSpace.setY(pointOnSpace.getZ() + p.getY());
		return pointOnSpace;
	}

	protected Snap snap(Vector3 p, int index) {
		if (!snapToObjects)
			return null;
		Vector3 closestPoint = null;
		double closestDistance = Double.MAX_VALUE;
		Wall closestWall = null;
		int closestPointIndex = -1;
		for (HousePart housePart : Scene.getInstance().getParts()) {
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
		} else {
			return null;
		}
	}
	
	protected ArrayList<PolygonPoint> exploreWallNeighbors(Wall startWall) {
		ArrayList<PolygonPoint> poly = new ArrayList<PolygonPoint>();
		Wall currentWall = startWall;
		Wall prevWall = null;
		while (currentWall != null) {
			Snap next = currentWall.next(prevWall);
			prevWall = currentWall;
			if (next == null)
				break;
			currentWall = (Wall) next.getNeighbor();
			if (currentWall == startWall)
				break;
		}

		startWall = currentWall;
		prevWall = null;
		while (currentWall != null && currentWall.isFirstPointInserted()) {
			Snap next = currentWall.next(prevWall);
			int pointIndex = 0;
			if (next != null)
				pointIndex = next.getThisPointIndex();
			pointIndex = pointIndex + 1;
			addPointToPolygon(poly, currentWall.getPoints().get(pointIndex == 1 ? 3 : 1));
			addPointToPolygon(poly, currentWall.getPoints().get(pointIndex));
			prevWall = currentWall;
			if (next == null)
				break;
			currentWall = (Wall) next.getNeighbor();
			if (currentWall == startWall)
				break;
		}

		return poly;
	}
	
	protected void addPointToPolygon(ArrayList<PolygonPoint> poly, Vector3 p) {
		PolygonPoint polygonPoint = new PolygonPoint(p.getX(), p.getY(), p.getZ());
		if (!poly.contains(polygonPoint)) {
//			avg.addLocal(p);
			poly.add(polygonPoint);
		}
	}	
	
	protected Vector3 grid(Vector3 p, double gridSize) {
		return grid(p, gridSize, true);
	}
	
	protected Vector3 grid(Vector3 p, double gridSize, boolean snapToZ) {
		if (snapToGrids) {
//			final double C = 2.0;
			p.set(Math.round(p.getX() / gridSize) * gridSize, Math.round(p.getY() / gridSize) * gridSize, !snapToZ ? p.getZ() : Math.round(p.getZ() / gridSize) * gridSize);
		}
		return p;
	}

	public void addPoint(int x, int y) {
		firstPointInserted = true;
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
		for (int i = 0; i < numOfEditPoints / numOfDrawPoints; i++) {
			points.add(new Vector3());
			abspoints.add(new Vector3());
		}
	}

	public void delete() {

	}	

	public abstract void setPreviewPoint(int x, int y);

	public void draw() {
		if (root == null)
			init();		

		for (int i = 0; i < points.size(); i++) {
			Vector3 p = points.get(i);
			p = toAbsolute(p);
			pointsRoot.getChild(i).setTranslation(p);
			abspoints.get(i).set(p);
		}	
		
//		flatten();
	}

//	public void recalculateRelativePoints() {
//		for (int i=0; i<points.size(); i++) {
//			points.get(i).set(toRelative(abspoints.get(i)));
//		}
//	}
	
	private void flatten() {
		root.setTranslation(pos, 0, 0);
	}

}
