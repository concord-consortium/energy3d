package org.concord.energy3d.model;

import java.io.Serializable;
import java.nio.FloatBuffer;
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
import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.OffsetState.OffsetType;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.OffsetState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.scenegraph.shape.Sphere;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.ui.text.BMText.Justify;
import com.ardor3d.util.TextureManager;

public abstract class HousePart implements Serializable {
	static final private long serialVersionUID = 1L;
	static final protected double SNAP_DISTANCE = 0.5;
	static protected int printSequence;
	static protected ReadOnlyColorRGBA defaultColor = ColorRGBA.GRAY;
	static private boolean snapToObjects = true;
	static private boolean snapToGrids = false;
	static private boolean drawAnnotations = false;
	static private int globalDrawFlag = 1;
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
	transient private int drawFlag;	
	transient protected String textureFileName;
	protected final int numOfDrawPoints, numOfEditPoints;
	protected final ArrayList<Vector3> points;
	protected final ArrayList<HousePart> children = new ArrayList<HousePart>();
	protected HousePart container = null;
	protected boolean drawCompleted = false;
	protected int editPointIndex = -1;
	protected double height;
	protected boolean relativeToHorizontal;
	private boolean firstPointInserted = false;
	private Vector3 flattenCenter;
	private double labelOffset = -0.01;
	
	public static void clearDrawFlags() {
		globalDrawFlag++;
		if (globalDrawFlag >= Integer.MAX_VALUE)
			globalDrawFlag = 1;
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
		orgHeight = height;
		center = new Vector3();
		abspoints = new ArrayList<Vector3>(numOfEditPoints);
		for (int i = 0; i < points.size(); i++)
			abspoints.add(new Vector3());
		root = new Node(toString());
		pointsRoot = new Node("Edit Points");
		sizeAnnotRoot = new Node("Size Annotations");
		sizeAnnotRoot.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		angleAnnotRoot = new Node("Angle Annotations");
		angleAnnotRoot.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		labelsRoot = new Node("Labels");
		
//		sizeAnnotRoot.getSceneHints().setCullHint(CullHint.Always);
//		angleAnnotRoot.getSceneHints().setCullHint(CullHint.Always);
		
		setAnnotationsVisible(drawAnnotations);
		
		// Set up a reusable pick results
//		final Vector3 origin = new Vector3();
		for (int i = 0; i < numOfEditPoints; i++)
			addNewEditPointShape(i);
		
		root.attachChild(pointsRoot);
		root.attachChild(sizeAnnotRoot);
		root.attachChild(angleAnnotRoot);
		root.attachChild(labelsRoot);
		
//		final OffsetState offsetState = new OffsetState();
//		offsetState.setTypeEnabled(OffsetType.Line, true);
//		offsetState.setFactor(1);
//		offsetState.setUnits(10);
//		root.setRenderState(offsetState);		
		
		
		computeAbsPoints();
		
		if (textureFileName == null)
			textureFileName = getDefaultTextureFileName();
	}

	private void addNewEditPointShape(int i) {
		final Sphere pointShape = new Sphere("Point", Vector3.ZERO, 8, 8, 0.05);
		pointShape.setUserData(new UserData(this, i, true));
		pointShape.updateModelBound(); // important
		pointShape.getSceneHints().setCullHint(CullHint.Always);
		pointShape.setModelBound(new BoundingBox());
		pointsRoot.attachChild(pointShape);
//		System.out.println(i);
	}
	
	public Mesh getEditPointShape(final int i) {
		if (i >= pointsRoot.getNumberOfChildren())
			addNewEditPointShape(i);
		return (Mesh)pointsRoot.getChild(i);
	}
	
	protected String getDefaultTextureFileName() {
		return null;
	}

	public void setOriginal(HousePart original) {
		this.original = original;
		root.detachChild(pointsRoot);
		this.center = original.center;
		if (original.mesh != null) {
			root.detachChild(this.mesh);
			this.mesh = original.mesh.makeCopy(true);
			this.mesh.setUserData(new UserData(this, ((UserData)original.mesh.getUserData()).getIndex(), false));
			root.attachChild(this.mesh);
//			this.sizeAnnotRoot = original.sizeAnnotRoot.makeCopy(false);
//			root.attachChild(sizeAnnotRoot);
		}
//		drawAnnotations();
//		setAnnotationsVisible(drawAnnotations);
		root.updateWorldBound(true);	
	}

	public HousePart getOriginal() {
		return original;
	}

	public Node getRoot() {
		if (root == null)
			init();
		return root;
	}

	public ArrayList<Vector3> getAbsPoints() {
		if (root == null)
			init();
		return abspoints;
	}

	public ArrayList<Vector3> getPoints() {
		if (root == null)
			init();
		return points;
	}
	
	public void complete() {
		drawCompleted = true;
		orgHeight = height;
	}

	public boolean isDrawCompleted() {
		return drawCompleted;
	}

	public boolean isFirstPointInserted() {
		return firstPointInserted;
	}

	public ArrayList<HousePart> getChildren() {
		return children;
	}

	protected void setHeight(double newHeight, boolean finalize) {
		this.height = newHeight;
		if (finalize)
			this.orgHeight = newHeight;
	}

	public void showPoints() {
//		for (int i = 0; i < points.size(); i++) {
		for (int i = 0; i < pointsRoot.getNumberOfChildren(); i++) {
			computeEditPointScale(i);
			pointsRoot.getChild(i).getSceneHints().setCullHint(CullHint.Inherit);
		}
	}

	protected void computeEditPointScale(int i) {
			pointsRoot.getChild(i).setScale(0.15 * SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getLocation().subtract(pointsRoot.getChild(i).getTranslation(), null).length());
			pointsRoot.getChild(i).updateGeometricState(0);
	}

	public void hidePoints() {
//		for (int i = 0; i < points.size(); i++)
//			pointsRoot.getChild(i).getSceneHints().setCullHint(CullHint.Always);
		for (final Spatial child : pointsRoot.getChildren())
			child.getSceneHints().setCullHint(CullHint.Always);		
	}

	public void setEditPoint(int i) {
		editPointIndex = i;
		drawCompleted = false;
	}
	
	public int getEditPoint() {
		return editPointIndex;
	}	
	
	protected Vector3 closestPoint(final ReadOnlyVector3 p1, final ReadOnlyVector3 v1, final int x, final int y) {
		final Ray3 pickRay = SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getPickRay(new Vector2(x, y), false, null);
		final Vector3 closest = closestPoint(p1, v1, pickRay.getOrigin(), pickRay.getDirection());
		return closest;
	}

	private Vector3 closestPoint(ReadOnlyVector3 p1, ReadOnlyVector3 p21, ReadOnlyVector3 p3, ReadOnlyVector3 p43) {
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
					container.getChildren().remove(this);
				if (userData != null && userData.getHousePart().isDrawCompleted()) {
					container = userData.getHousePart();
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
		ArrayList<Vector3> wallPoints = container.getAbsPoints();
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
		ArrayList<Vector3> containerPoints = container.getAbsPoints();
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
			if (points != abspoints)
				abspoints.add(new Vector3());
		}
	}
	
//	protected void clearDrawFlag() {
//		drawFlag = 0;
//	}

	public void draw() {
		if (drawFlag == globalDrawFlag)
			return;		
		drawFlag = globalDrawFlag;
		
		if (root == null)
			init();

		computeAbsPoints();

		drawMesh();
		
		computeCenter();

		CollisionTreeManager.INSTANCE.removeCollisionTree(root);

//		if (isFlatten && original != null && isPrintable() && isDrawCompleted()) // && flattenTime >= 0) // TODO If draw not completed then it shouldn't even exist at this point!
//			flatten();

//		if (original != null && isPrintable())
//		if (isPrintable())
//			drawLabels();

//		if (drawAnnotations)
			drawAnnotations();
	}

	protected void computeAbsPoints() {
		for (int i = 0; i < points.size(); i++) {
			final Vector3 p = toAbsolute(points.get(i));
			abspoints.get(i).set(p);
			pointsRoot.getChild(i).setTranslation(p);
		}
	}

//	protected void computeCenter() {
//		center.set(0, 0, 0);
//		for (int i = 0; i < abspoints.size(); i++)
//			center.addLocal(abspoints.get(i));
//		center.multiplyLocal(1.0 / abspoints.size());
//	}
	
	protected void computeCenter() {
		center.set(0, 0, 0);
		for (int i = 0; i < abspoints.size(); i++)
			center.addLocal(abspoints.get(i));
		center.multiplyLocal(1.0 / abspoints.size());
		
		if (mesh == null)
			return;
		double minX, minY, minZ;
		double maxX, maxY, maxZ;
		minX = minY = minZ = Double.MAX_VALUE;
		maxX = maxY = maxZ = -Double.MAX_VALUE;
		
		final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
		buf.rewind();
		while (buf.hasRemaining()) {
			final double x = buf.get(); 
			final double y = buf.get();
			final double z = buf.get();
			if (x < minX)
				minX = x;
			if (y < minY)
				minY = y;
			if (z < minZ)
				minZ = z;
			if (x > maxX)
				maxX = x;
			if (y > maxY)
				maxY = y;
			if (z > maxZ)
				maxZ = z;
		}
		center.set(minX + (maxX - minX) / 2.0, minY + (maxY - minY) / 2.0, minZ + (maxZ - minZ) / 2.0);		
	}
	
	
	protected void updateEditShapes() {
		int i = 0;
		for (final Spatial editShape : pointsRoot.getChildren()) {
			editShape.setTranslation(abspoints.get(i++));
		}
	}
	
	public void flattenInit() {
		flattenCenter = new Vector3();
		flatten(1.0);
		flattenCenter = new Vector3(mesh.getWorldBound().getCenter());
	}

	public void flatten(double flattenTime) {
		mesh.updateModelBound();
		root.updateWorldTransform(true);
		root.updateWorldBound(true);
		final Vector3 targetCenter = new Vector3(((UserData) mesh.getUserData()).getPrintCenter());
//		root.setTranslation(0, 0, 0);
		
		
//		if (flattenTime == 1.0)
//			center = new Vector3(root.getWorldBound().getCenter());

		Vector3 currentCenter = new Vector3(center);
		
//		root.getTransform().applyForward(currentCenter);
//		Vector3 center = root.getRotation().applyPost(this.center, null);
		
//		root.updateWorldBound(true);
		if (this instanceof Floor) {
			System.out.print(flattenTime + "\t" + Util.toString(currentCenter) + "\t");
			currentCenter = new Vector3(root.getWorldBound().getCenter());
			System.out.println(Util.toString(currentCenter) + "\t" + Util.toString(targetCenter));
			if (flattenTime == 1.0)
				System.out.println("pause");
//			
		}
		
//		if (flattenTime == 1.0) {
//			root.setTranslation(targetCenter);
//		} else {			
			final Vector3 subtractLocal = targetCenter.subtractLocal(flattenCenter);
			root.setTranslation(subtractLocal.multiplyLocal(flattenTime));
//		}
		drawAnnotations();
		root.updateWorldTransform(true);
		root.updateWorldBound(true);
	}

	public boolean isPrintable() {
		return true;
	}

	public int drawLabels(int printSequence) {
		if (!isPrintable())
			return printSequence;
		final String text = "(" + (printSequence++ + 1) + ")";
		final BMText label = fetchBMText(text, 0);
		
		final Vector3 offset;
		if (original == null)
			offset = getFaceDirection().multiply(0.5, null);
		else {
			offset = new Vector3(0, labelOffset, 0);
		}
		root.getTransform().applyInverseVector(offset);
		offset.addLocal(center);
		label.setTranslation(offset);
		return printSequence;
	}
	
	public void hideLabels() {
		for (final Spatial label : labelsRoot.getChildren())
			label.getSceneHints().setCullHint(CullHint.Always);
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
		return Vector3.UNIT_Z;
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
//		System.out.println("Removing: " + this);
//		Scene.getInstance().getOriginalHouseRoot().detachChild(root);
//		Scene.getInstance().getParts().remove(this);
//		
//		for (HousePart child : children)
//			child.delete();
////			Scene.getInstance().remove(child);
//		children.clear();
//		
	}

	protected void drawAnnotations() {
	}

	protected abstract void drawMesh();

	public void setAnnotationsVisible(boolean visible) {
		drawAnnotations  = visible;
		final CullHint cull = visible ? CullHint.Inherit : CullHint.Always;
		sizeAnnotRoot.getSceneHints().setCullHint(cull);
		angleAnnotRoot.getSceneHints().setCullHint(cull);
	}

	public void updateTextureAndColor(final boolean textureEnabled) {
		if (textureEnabled) {
			final TextureState ts = new TextureState();
			ts.setTexture(TextureManager.load(textureFileName, Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
			mesh.setRenderState(ts);
			mesh.setDefaultColor(ColorRGBA.WHITE);
		} else {
			mesh.clearRenderState(StateType.Texture);
			mesh.setDefaultColor(defaultColor);
		}
	}
	
	public HousePart getContainer() {
		return container;
	}
	
	public Mesh getMesh() {
		return mesh;	
	}

	@Override
	public String toString() {
//		return this.getClass().getSimpleName() + "(" + Integer.toHexString(this.hashCode()) + "), editPoint = " + editPointIndex;
		String s = this.getClass().getSimpleName() + "(" + Integer.toHexString(this.hashCode()) + ")";		
		for (int i = 0; i < points.size(); i += 2)
//			if (root == null)
				s += "\t" + Util.toString(abspoints.get(i));
//			else
//				s += "\t" + Util.toString(root.getTransform().applyForward(points.get(i), null));		
		s += ("\teditPoint = " + editPointIndex);
		return s;
	}

	public void setLabelOffset(double labelOffset) {
		this.labelOffset = labelOffset;
	}
	
}  