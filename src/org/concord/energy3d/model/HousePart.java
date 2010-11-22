package org.concord.energy3d.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Angle90Annotation;
import org.concord.energy3d.shapes.AngleAnnotation;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.util.FontManager;
import org.concord.energy3d.util.SelectUtil;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Sphere;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.ui.text.BMText.Justify;

public abstract class HousePart implements Serializable {
	static final private long serialVersionUID = 1L;
	static final protected double SNAP_DISTANCE = 0.5;
	static protected double flattenTime = 0;
	static protected int printSequence;
	static protected int printPage;
	static protected boolean textureEnabled = true;
	static protected ReadOnlyColorRGBA defaultColor = ColorRGBA.GRAY;
	static private boolean isFlatten = false;
	static private boolean snapToObjects = true;
	static private boolean snapToGrids = false;
	static private boolean drawAnnotations = false;
	static private long idCounter = 1;
	static private int globalDrawFlag = 1;
	protected final int numOfDrawPoints, numOfEditPoints;
	protected final ArrayList<Vector3> points;
	protected final ArrayList<HousePart> children = new ArrayList<HousePart>();
	protected HousePart container = null;
	protected boolean drawCompleted = false;
	protected int editPointIndex = -1;
	protected double height;
	protected boolean relativeToHorizontal;
	private boolean firstPointInserted = false;
	transient protected Node root;
	transient protected Node pointsRoot;
	transient protected ArrayList<Vector3> abspoints;
	transient protected double orgHeight;
	transient protected HousePart original = null;
	transient protected Vector3 center;
	transient protected Node labelsRoot;
	transient protected Node sizeAnnotRoot;
	transient protected Node angleAnnotRoot;
	transient protected Mesh mesh;
	transient private long id;
	transient private ReadOnlyVector3 defaultDirection;
	transient private int drawFlag;	
	
	public static void clearDrawFlags() {
		globalDrawFlag++;
		if (globalDrawFlag >= Integer.MAX_VALUE)
			globalDrawFlag = 1;
	}

	public static void setFlattenTime(double flattenTime) {
		if (flattenTime < 0)
			flattenTime = 0;
		if (flattenTime > 1)
			flattenTime = 1;
		HousePart.flattenTime = flattenTime;
		HousePart.printSequence = HousePart.printPage = 0;
	}
	
	public static void setFlatten(boolean flatten) {
		HousePart.isFlatten = flatten;
	}	

	public static double getFlattenTime() {
		return flattenTime;
	}

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
	
	public static ReadOnlyColorRGBA getDefaultColor() {
		return defaultColor;
	}

	public static void setDefaultColor(ReadOnlyColorRGBA defaultColor) {
		HousePart.defaultColor = defaultColor;
	}

	public HousePart(int numOfDrawPoints, int numOfEditPoints, double height) {
		this(numOfDrawPoints, numOfEditPoints, height, false);
	}

	public HousePart(int numOfDrawPoints, int numOfEditPoints, double height, boolean relativeToHorizontal) {		
		this.numOfDrawPoints = numOfDrawPoints;
		this.numOfEditPoints = numOfEditPoints;
		this.height = this.orgHeight = height;
		this.relativeToHorizontal = relativeToHorizontal;
		points = new ArrayList<Vector3>(numOfEditPoints);
		init();
		allocateNewPoint();
	}

	protected void init() {	
		id = idCounter++;
		if (Util.DEBUG)
		System.out.print("Deep cloning...");
		if (Util.DEBUG)
		System.out.print("Instantiating Nodes...");
		defaultDirection = new Vector3(0, 0, 0.5);
		orgHeight = height;
		center = new Vector3();
		abspoints = new ArrayList<Vector3>(numOfEditPoints);
		for (int i = 0; i < points.size(); i++)
			abspoints.add(new Vector3());
		root = new Node(toString());
		pointsRoot = new Node("Edit Points");
		sizeAnnotRoot = new Node("Size Annotations");
		angleAnnotRoot = new Node("Angle Annotations");
		labelsRoot = new Node("Labels");
		
		// Set up a reusable pick results
		if (Util.DEBUG)
		System.out.print("Creating Edit Points...");
		final Vector3 origin = new Vector3();
		for (int i = 0; i < numOfEditPoints; i++) {
			Sphere pointShape = new Sphere("Point", origin, 8, 8, 0.05);
			pointsRoot.attachChild(pointShape);
			pointShape.setUserData(new UserData(this, i));
//			pointShape.setModelBound(new BoundingBox());
			pointShape.updateModelBound(); // important
			pointShape.getSceneHints().setCullHint(CullHint.Always);
		}
		if (Util.DEBUG)
		System.out.print("Attaching Nodes...");
		root.attachChild(pointsRoot);
		root.attachChild(sizeAnnotRoot);
		root.attachChild(angleAnnotRoot);
		root.attachChild(labelsRoot);
		
		computeAbsPoints();
		if (Util.DEBUG)
		System.out.println("done");
	}

	public void setOriginal(HousePart original) {
		this.original = original;
	}

	public HousePart getOriginal() {
		return original;
	}

	public Node getRoot() {
		if (root == null)
			init();
		return root;
	}

	public ArrayList<Vector3> getPoints() {
		if (root == null)
			init();
		return abspoints;
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

//	public void addChild(HousePart housePart) {
//		children.add(housePart);
//	}
//
//	public boolean removeChild(HousePart housePart) {
//		return children.remove(housePart);
//	}

	public ArrayList<HousePart> getChildren() {
		return children;
	}

//	public double getHeight() {
//		return orgHeight;
//	}

	protected void setHeight(double newHeight, boolean finalize) {
		this.height = newHeight;
		if (finalize)
			this.orgHeight = newHeight;
	}

	public void showPoints() {		
		for (int i = 0; i < points.size(); i++) {
			pointsRoot.getChild(i).getSceneHints().setCullHint(CullHint.Inherit);
			updateEditPointScale(i);
		}
	}

	protected void updateEditPointScale(int i) {
			pointsRoot.getChild(i).setScale(0.15 * SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getLocation().subtract(pointsRoot.getChild(i).getTranslation(), null).length());
	}

	public void hidePoints() {
		for (int i = 0; i < points.size(); i++)
			pointsRoot.getChild(i).getSceneHints().setCullHint(CullHint.Always);
	}

	public void editPoint(int i) {
		editPointIndex = i;
		drawCompleted = false;
	}
	
	public int getEditPoint() {
		return editPointIndex;
	}	
	
//	protected double findHeight(Vector3 base, Vector3 upperPoint) {
//		double subtract = upperPoint.getZ() - base.getZ();
//		return Math.max(0, subtract);
//	}

	protected Vector3 closestPoint(final ReadOnlyVector3 p1, final ReadOnlyVector3 v1, final int x, final int y) {
		final Ray3 pickRay = SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getPickRay(new Vector2(x, y), false, null);
		final Vector3 closest = closestPoint(p1, v1, pickRay.getOrigin(), pickRay.getDirection());
		return closest;
	}

//	private Vector3 closestPoint(ReadOnlyVector3 p1, ReadOnlyVector3 p2, ReadOnlyVector3 p3, ReadOnlyVector3 p4) {
//		final double EPS = 0.0001;
//		Vector3 p13, p43, p21;
//		double d1343, d4321, d1321, d4343, d2121;
//		double numer, denom;
//
//		p13 = p1.subtract(p3, null);
//		p43 = p4.subtract(p3, null);
//		if (Math.abs(p43.getX()) < EPS && Math.abs(p43.getY()) < EPS && Math.abs(p43.getZ()) < EPS)
//			return null;
//		p21 = p2.subtract(p1, null);
//		if (Math.abs(p21.length()) < EPS)
//			return null;
//
//		d1343 = p13.getX() * p43.getX() + p13.getY() * p43.getY() + p13.getZ() * p43.getZ();
//		d4321 = p43.getX() * p21.getX() + p43.getY() * p21.getY() + p43.getZ() * p21.getZ();
//		d1321 = p13.getX() * p21.getX() + p13.getY() * p21.getY() + p13.getZ() * p21.getZ();
//		d4343 = p43.getX() * p43.getX() + p43.getY() * p43.getY() + p43.getZ() * p43.getZ();
//		d2121 = p21.getX() * p21.getX() + p21.getY() * p21.getY() + p21.getZ() * p21.getZ();
//
//		denom = d2121 * d4343 - d4321 * d4321;
//		if (Math.abs(denom) < EPS)
//			return null;
//		numer = d1343 * d4321 - d1321 * d4343;
//
//		double mua = numer / denom;
//		Vector3 pa = new Vector3(p1.getX() + mua * p21.getX(), p1.getY() + mua * p21.getY(), p1.getZ() + mua * p21.getZ());
//
//		return pa;
//	}
	
	private Vector3 closestPoint(ReadOnlyVector3 p1, ReadOnlyVector3 p21, ReadOnlyVector3 p3, ReadOnlyVector3 p43) {
		final double EPS = 0.0001;
		Vector3 p13; //, p43, p21;
		double d1343, d4321, d1321, d4343, d2121;
		double numer, denom;

		p13 = p1.subtract(p3, null);
//		p43 = p4.subtract(p3, null);
		if (Math.abs(p43.getX()) < EPS && Math.abs(p43.getY()) < EPS && Math.abs(p43.getZ()) < EPS)
			return null;
//		p21 = p2.subtract(p1, null);
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
		Vector3 pa = new Vector3(p1.getX() + mua * p21.getX(), p1.getY() + mua * p21.getY(), p1.getZ() + mua * p21.getZ());

		return pa;
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
		if (!firstPointInserted || container == null)
			picked = SelectUtil.pickPart(x, y, typeOfHousePart);
		else
			picked = SelectUtil.pickPart(x, y, container == null ? null : container.getRoot());

		if (!firstPointInserted || container == null) {
			UserData userData = null;
			if (picked != null)
				userData = picked.getUserData();
			if (container == null || userData == null || container != userData.getHousePart()) {
				if (container != null)
//					container.removeChild(this);
					container.getChildren().remove(this);
				if (userData != null && userData.getHousePart().isDrawCompleted()) {
					container = userData.getHousePart();
//					container.addChild(this);
					container.getChildren().add(this);
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
		Vector3 p = org.subtract(origin, null);
		Vector3 wallx = wallPoints.get(2).subtract(origin, null);
		Vector3 wally = wallPoints.get(1).subtract(origin, null);
		Vector3 pointOnWall = new Vector3(wallx.getX() == 0 ? p.getY() / wallx.getY() : p.getX() / wallx.getX(), (relativeToHorizontal) ? p.getY() / wally.getY() : org.getY(), (relativeToHorizontal) ? org.getZ() : p.getZ() / wally.getZ());
		return pointOnWall;
	}

	protected Vector3 toAbsolute(Vector3 p) {
		if (container == null)
			return p;
		ArrayList<Vector3> containerPoints = container.getPoints();
		Vector3 origin = containerPoints.get(0);
		Vector3 wallx = containerPoints.get(2).subtract(origin, null);
		Vector3 wally = containerPoints.get(1).subtract(origin, null);
		Vector3 pointOnSpace = origin.add(wallx.multiply(p.getX(), null), null).add(wally.multiply((relativeToHorizontal) ? p.getY() : p.getZ(), null), null);
		if (relativeToHorizontal)
			pointOnSpace.setZ(pointOnSpace.getZ() + p.getZ());
		return pointOnSpace;
	}

	protected Vector3 grid(Vector3 p, double gridSize) {
		return grid(p, gridSize, true);
	}

	protected Vector3 grid(Vector3 p, double gridSize, boolean snapToZ) {
		if (snapToGrids) {
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

	public void draw() {
//		System.out.println("(" + printSequence + ")");
		if (drawFlag == globalDrawFlag)
			return;		
		drawFlag = globalDrawFlag;
		
		if (Util.DEBUG)
		System.out.println("drawing..." + this);
		if (root == null) {
			log("init()");
			init();
		}

		log("computeAbsPoints()");
		computeAbsPoints();
		log("computeCenter()");
		computeCenter();

		log("updateMesh()");
		updateMesh();

		log("CollisionTreeManager.INSTANCE.removeCollisionTree()");
		CollisionTreeManager.INSTANCE.removeCollisionTree(root);


		if (isFlatten && original != null && isPrintable() && isDrawCompleted()) { // && flattenTime >= 0) // TODO If draw not completed then it shouldn't even exist at this point!
			log("flatten()");
			flatten();
		}

		if (original != null && isPrintable()) {
			log("updateLabels()");
			updateLabels();
		}

		if (drawAnnotations) {
			log("drawAnnotations()");
			drawAnnotations();
		}		

//		for (HousePart child : children)
//			child.draw();

		// for (HousePart child : children)
		// child.draw();
				
	}

	protected void computeAbsPoints() {
		for (int i = 0; i < points.size(); i++) {
			Vector3 p = points.get(i);
			p = toAbsolute(p);
			abspoints.get(i).set(p);
			pointsRoot.getChild(i).setTranslation(p);
		}
	}

	protected void computeCenter() {
		center.set(0, 0, 0);
		for (int i = 0; i < abspoints.size(); i++)
			center.addLocal(abspoints.get(i));
		center.multiplyLocal(1.0 / abspoints.size());
	}

	protected void flatten() {
		root.setTranslation(0, 0, 0);
//		computePrintCenter();	//TODO move to setPreview(true) doesn't need to recompute every time
//		final Vector3 targetCenter = new Vector3(printCenter);
//		final Vector3 targetCenter = new Vector3((ReadOnlyVector3) root.getUserData());
//		final Vector3 targetCenter = new Vector3((ReadOnlyVector3) mesh.getUserData());
		final Vector3 targetCenter = new Vector3(((UserData) mesh.getUserData()).getPrintCenter());
		final Vector3 currentCenter = new Vector3(center);
		
		root.getTransform().applyForward(currentCenter);
		final Vector3 subtractLocal = targetCenter.subtractLocal(currentCenter);
		root.setTranslation(subtractLocal.multiplyLocal(flattenTime));
	}

	public boolean isPrintable() {
		return true;
	}

//	protected void computeLabelTop(final Vector3 top) {
//		top.set(0, 0, height / 2);
////		return height / 1;
//	}

	protected void updateLabels() {
		final String text = "(" + (printSequence++ + 1) + ")";
		final BMText label = fetchBMText(text, 0);
				
		label.setTranslation(center);
		Vector3 up = new Vector3();
		if (original == null)
			up.set(getFaceDirection());
		else
			up.set(0, -0.01, 0);
		root.getTransform().applyInverseVector(up);
		label.setTranslation(center.getX() + up.getX(), center.getY() + up.getY(), center.getZ() + up.getZ());
	}
	
	protected BMText fetchBMText(final String text, final int index) {
		final BMText label;
		if (labelsRoot.getChildren().size() > index) {
			label = (BMText)labelsRoot.getChild(index);
			label.setText(text);
			label.getSceneHints().setCullHint(CullHint.Inherit);			
		} else {			
			label = new BMText("Label Text", text, FontManager.getInstance().getPartNumberFont(), Align.Center, Justify.Center);
			labelsRoot.attachChild(label);
		}
		return label;
	}

	public ReadOnlyVector3 getFaceDirection() {
		return defaultDirection;
	}

	protected SizeAnnotation fetchSizeAnnot(int annotCounter) {
		final SizeAnnotation annot;
		if (annotCounter < sizeAnnotRoot.getChildren().size()) {
			annot = (SizeAnnotation) sizeAnnotRoot.getChild(annotCounter);
			annot.getSceneHints().setCullHint(CullHint.Inherit);
		} else {
			annot = new SizeAnnotation();
			sizeAnnotRoot.attachChild(annot);
		}
		return annot;
	}

	protected AngleAnnotation fetchAngleAnnot(int annotCounter) {
		final AngleAnnotation annot;
		if (annotCounter < angleAnnotRoot.getChildren().size()) {
			annot = (AngleAnnotation) angleAnnotRoot.getChild(annotCounter);
			annot.getSceneHints().setCullHint(CullHint.Inherit);
		} else {
			annot = new AngleAnnotation(this);
			angleAnnotRoot.attachChild(annot);
		}
		return annot;
	}

	protected Angle90Annotation fetchAngleAnnot90(int annotCounter) {
		final Angle90Annotation annot;
		if (annotCounter < angleAnnotRoot.getChildren().size()) {
			annot = (Angle90Annotation) angleAnnotRoot.getChild(annotCounter);
			annot.getSceneHints().setCullHint(CullHint.Inherit);
		} else {
			annot = new Angle90Annotation(this);
			angleAnnotRoot.attachChild(annot);
		}
		return annot;
	}
	
	public abstract void setPreviewPoint(int x, int y);

	public void delete() {
	}

	protected void drawAnnotations() {
	}

	protected abstract void updateMesh();

	public void setAnnotationsVisible(boolean visible) {
		drawAnnotations  = visible;
		final CullHint cull = visible ? CullHint.Inherit : CullHint.Always;
		sizeAnnotRoot.getSceneHints().setCullHint(cull);
		angleAnnotRoot.getSceneHints().setCullHint(cull);
	}

	public void log(String s) {
		if (Util.DEBUG)
			System.out.println(this + "\t" + s);
	}

	public static void setTextureEnabled(boolean enabled) {
		textureEnabled  = enabled;		
	}

	public void updateTexture() {
	}
	
	public HousePart getContainer() {
		return container;
	}
	
	public String toString() {
		return this.getClass().getName() + "(" + id + ")";
	}

	public Mesh getMesh() {
		return mesh;
	}
	
}  