package org.concord.energy3d.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.concord.energy3d.exception.InvisibleException;
import org.concord.energy3d.scene.SceneManager;
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
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.BlendState.BlendEquation;
import com.ardor3d.renderer.state.BlendState.SourceFunction;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Line;
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
import com.ardor3d.util.geom.BufferUtils;

public abstract class HousePart implements Serializable {
	private static final long serialVersionUID = 1L;
	protected static final double SNAP_DISTANCE = 0.5;
	protected static int printSequence;
	protected static ReadOnlyColorRGBA defaultColor = ColorRGBA.GRAY;
	protected static boolean drawAnnotations = false;
	private static HousePart gridsHighlightedHousePart;
	private static boolean snapToObjects = true;
	private static boolean snapToGrids = false;
	protected transient final int numOfDrawPoints;
	protected transient final int numOfEditPoints;
	protected transient Node root;
	protected transient Node pointsRoot;
	protected transient double orgHeight;
	protected transient HousePart original = null;
	protected transient Node labelsRoot;
	protected transient Node sizeAnnotRoot;
	protected transient Node angleAnnotRoot;
	protected transient Mesh mesh;
	protected transient Mesh gridsMesh;
	protected transient String textureFileName;
	protected transient boolean relativeToHorizontal;
	protected final ArrayList<Vector3> points;
	protected final ArrayList<HousePart> children = new ArrayList<HousePart>();
	protected HousePart container = null;
	protected double height;
	protected int editPointIndex = -1;
	protected boolean drawCompleted = false;
	protected Vector3 flattenCenter;
	private double labelOffset = -0.01;
	private boolean firstPointInserted = false;

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

	public static HousePart getGridsHighlightedHousePart() {
		return gridsHighlightedHousePart;
	}

	public static void setGridsHighlightedHousePart(HousePart gridsHighlightedHousePart) {
		HousePart.gridsHighlightedHousePart = gridsHighlightedHousePart;
	}

	/* if an attribute is serializable or is not needed after deserialization then they are passed as parameters to constructor */
	public HousePart(final int numOfDrawPoints, final int numOfEditPoints, final double height) {
		this.numOfDrawPoints = numOfDrawPoints;
		this.numOfEditPoints = numOfEditPoints;
		this.height = this.orgHeight = height;
		points = new ArrayList<Vector3>(numOfEditPoints);
		init();
		allocateNewPoint();
	}

	public double getGridSize() {
		return 0.5;
	}

	/* if an attribute is transient but is always needed then it should be set to default here */
	protected void init() {
		relativeToHorizontal = false;
		orgHeight = height;
		root = new Node(toString());
		pointsRoot = new Node("Edit Points");
		sizeAnnotRoot = new Node("Size Annotations");
		sizeAnnotRoot.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		angleAnnotRoot = new Node("Angle Annotations");
		angleAnnotRoot.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		labelsRoot = new Node("Labels");

		setAnnotationsVisible(drawAnnotations);

		// Set up a reusable pick results
		for (int i = 0; i < points.size(); i++)
			addNewEditPointShape(i);

		root.attachChild(pointsRoot);
		root.attachChild(sizeAnnotRoot);
		root.attachChild(angleAnnotRoot);
		root.attachChild(labelsRoot);

		if (textureFileName == null)
			textureFileName = getDefaultTextureFileName();

		gridsMesh = new Line("Grids");
		gridsMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(2));
		gridsMesh.setDefaultColor(ColorRGBA.BLUE);
//		gridsMesh.setDefaultColor(new ColorRGBA(0, 1, 1, 0.5f));
//		gridsMesh.setDefaultColor(new ColorRGBA(0, 0, 1, 0.9f));
//		final BlendState blend = new BlendState();
//		blend.setBlendEnabled(true);
//		gridsMesh.setRenderState(blend);
//		gridsMesh.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
		gridsMesh.setModelBound(new BoundingBox());		
		Util.disablePickShadowLight(gridsMesh);
		root.attachChild(gridsMesh);		
		setGridsVisible(false);
	}

	private void addNewEditPointShape(int i) {
		final Sphere pointShape = new Sphere("Point", Vector3.ZERO, 8, 8, 0.05);
		pointShape.setUserData(new UserData(this, i, true));
		pointShape.updateModelBound(); // important
		pointShape.getSceneHints().setCullHint(CullHint.Always);
		pointShape.setModelBound(new BoundingBox());
		pointsRoot.attachChild(pointShape);
	}

	public Mesh getEditPointShape(final int i) {
		if (i >= pointsRoot.getNumberOfChildren())
			addNewEditPointShape(i);
		return (Mesh) pointsRoot.getChild(i);
	}

	protected String getDefaultTextureFileName() {
		return null;
	}

	public void setOriginal(HousePart original) {
		this.original = original;
		root.detachChild(pointsRoot);
		if (original.mesh != null) {
			root.detachChild(mesh);
			mesh = original.mesh.makeCopy(true);
			mesh.setUserData(new UserData(this, ((UserData) original.mesh.getUserData()).getIndex(), false));
			root.attachChild(mesh);
		}
		drawAnnotations();
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

	public ArrayList<Vector3> getPoints() {
		if (root == null)
			init();
		return points;
	}

	public void complete() throws InvisibleException {
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
		for (final Spatial child : pointsRoot.getChildren())
			child.getSceneHints().setCullHint(CullHint.Always);
		// hideGrids();
		// if (container != null)
		// container.setGridsVisible(false);
	}

	public void setEditPoint(int i) {
		editPointIndex = i;
		drawCompleted = false;
		// if (this instanceof Roof)
		// setGridsVisible(true);
		// else if (container != null)
		// container.setGridsVisible(true);
	}

	public int getEditPoint() {
		return editPointIndex;
	}

	protected Vector3 closestPoint(final ReadOnlyVector3 p1, final ReadOnlyVector3 v1, final int x, final int y) {
		final Ray3 pickRay = SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getPickRay(new Vector2(x, y), false, null);
		final Vector3 closest = closestPoint(p1, v1, pickRay.getOrigin(), pickRay.getDirection());
		return closest;
	}

	protected Vector3 closestPoint(ReadOnlyVector3 p1, ReadOnlyVector3 p21, ReadOnlyVector3 p3, ReadOnlyVector3 p43) {
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
			PickedHousePart picked = pickContainer(x, y, c);
			if (picked != null)
				return picked;
		}
		return null;
	}

	protected PickedHousePart pickContainer(int x, int y, Class<?> typeOfHousePart) {
		final HousePart previousContainer = container;
		final PickedHousePart picked;
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
//		if (gridsHighlightedHousePart != container) {
//			if (gridsHighlightedHousePart != null)
////				gridsHighlightedHousePart.setGridsVisible(false);
//				gridsHighlightedHousePart.gridsMesh.getSceneHints().setCullHint(CullHint.Always);
//			else
//				SceneManager.getInstance().setGridsVisible(false);
//			if (container != null)
//				container.drawGrids(getGridSize());
//			else if (this instanceof Foundation || this instanceof Wall)
//				SceneManager.getInstance().setGridsVisible(true);
//			if (this instanceof Roof)
//				gridsHighlightedHousePart = this;
//			else
//				gridsHighlightedHousePart = container;
//		}
		if (previousContainer != container) {
			if (previousContainer == null)
				SceneManager.getInstance().setGridsVisible(false);
			else
				previousContainer.gridsMesh.getSceneHints().setCullHint(CullHint.Always);
		}
		if (container != null && !(this instanceof Roof)) {
			container.drawGrids(getGridSize());
			container.gridsMesh.getSceneHints().setCullHint(CullHint.Inherit);
		} else if (this instanceof Foundation || this instanceof Wall) {
			SceneManager.getInstance().setGridsVisible(true);
		}
		return picked;
	}

	protected Vector3 toRelative(Vector3 org) {
		return toRelative(org, container);
	}

	protected Vector3 toRelative(final ReadOnlyVector3 org, final HousePart container) {
		if (container == null)
			return new Vector3(org);
		Vector3 origin = container.getAbsPoint(0);
		Vector3 p = org.subtract(origin, null);
		Vector3 wallx = container.getAbsPoint(2).subtract(origin, null);
		Vector3 wally = container.getAbsPoint(1).subtract(origin, null);
		Vector3 pointOnWall = new Vector3(wallx.getX() == 0 ? p.getY() / wallx.getY() : p.getX() / wallx.getX(), (relativeToHorizontal) ? p.getY() / wally.getY() : org.getY(), (relativeToHorizontal) ? org.getZ() : p.getZ() / wally.getZ());
		return pointOnWall;
	}

	protected Vector3 toAbsolute(final ReadOnlyVector3 p) {
		return toAbsolute(p, container);
	}

	protected Vector3 toAbsolute(final ReadOnlyVector3 p, final HousePart container) {
		if (container == null)
			return new Vector3(p);
		final ReadOnlyVector3 origin = container.getAbsPoint(0);
		ReadOnlyVector3 width = container.getAbsPoint(2).subtract(origin, null);
		if (width.length() < MathUtils.ZERO_TOLERANCE)
		    width = new Vector3(MathUtils.ZERO_TOLERANCE, 0, 0);		
		ReadOnlyVector3 height = container.getAbsPoint(1).subtract(origin, null);
        if (height.length() < MathUtils.ZERO_TOLERANCE)
            height = new Vector3(0, relativeToHorizontal ? MathUtils.ZERO_TOLERANCE : 0, relativeToHorizontal ? 0 : MathUtils.ZERO_TOLERANCE);        		
		Vector3 pointOnSpace = origin.add(width.multiply(p.getX(), null), null).add(height.multiply((relativeToHorizontal) ? p.getY() : p.getZ(), null), null);
		if (relativeToHorizontal)
			pointOnSpace.setZ(pointOnSpace.getZ() + p.getZ());
		if (!Vector3.isValid(pointOnSpace))
		    System.out.println("xxx");
		return pointOnSpace;
	}

	protected Vector3 grid(Vector3 p, double gridSize) {
		return grid(p, gridSize, true);
	}

	protected Vector3 grid(final Vector3 p, final double gridSize, final boolean snapToZ) {
		// if (snapToGrids) {
		// p.set(Math.round(p.getX() / gridSize) * gridSize, Math.round(p.getY() / gridSize) * gridSize, !snapToZ ? p.getZ() : Math.round(p.getZ() / gridSize) * gridSize);
		// }
		// if (snapToGrids) {
		// p.set(Math.round(p.getX() / gridSize) * gridSize, Math.round(p.getY() / gridSize) * gridSize, !snapToZ ? p.getZ() : Math.round(p.getZ() / gridSize) * gridSize);
		// }
		// return p;

		// if (isSnapToGrids()) {
		// if (container.getContainer() != null)
		// p.subtractLocal(0, 0, container.getContainer().getHeight());
		// p.set(Math.round(p.getX() / gridSize) * gridSize, Math.round(p.getY() / gridSize) * gridSize, !snapToZ ? p.getZ() : Math.round(p.getZ() / gridSize) * gridSize);
		// if (container.getContainer() != null)
		// p.addLocal(0, 0, container.getContainer().getHeight());
		// }
		// return p;

		if (isSnapToGrids()) {
			if (container != null) {
				final ReadOnlyVector3 p0 = container.getAbsPoint(0);

				final ReadOnlyVector3 origin;
				if (relativeToHorizontal) {
					// origin = container.getAbsPoint(2).subtractLocal(p0).addLocal(container.getAbsPoint(1).subtractLocal(p0)).multiplyLocal(0.5).addLocal(p0);
					// origin = root.getWorldBound().getCenter();
					final ReadOnlyVector3 center;
					if (this instanceof Roof)
						center = getCenter();
					else
						center = container.getCenter();

					if (snapToZ)
						origin = center;
					else
						origin = center.add(0, 0, p.getZ(), null);
				} else
					origin = p0;

				final ReadOnlyVector3 originToP = p.subtract(origin, null);
				;
				final Vector3 newP = new Vector3();
				// if (!snapToZ) {
				final ReadOnlyVector3 horizontalDir = new Vector3(originToP.getX(), !snapToZ ? 0 : originToP.getY(), 0);
				final double snapedHorizontalLength = Math.round(horizontalDir.length() / gridSize) * gridSize;
				newP.set(horizontalDir).normalizeLocal().multiplyLocal(snapedHorizontalLength);
				// }

				final double snapedVerticalLength = Math.round((!snapToZ ? originToP.getY() : originToP.getZ()) / gridSize) * gridSize;
				// newP.set(newP.getX(), !snapToZ ? snapedVerticalLength : newP.getY(), !snapToZ ? p.getZ() : snapedVerticalLength);
				newP.set(newP.getX(), !snapToZ ? snapedVerticalLength : 0, !snapToZ ? 0 : snapedVerticalLength);
				return newP.addLocal(origin);

				// final ReadOnlyVector3 p0p = p.subtract(p0, null);
				// final ReadOnlyVector3 h = new Vector3(p0p.getX(), p0p.getY(), 0);
				// final double snapedHorizontalLength = Math.round(h.length() / gridSize) * gridSize;
				// final Vector3 newp0p = h.normalize(null).multiplyLocal(snapedHorizontalLength);
				//
				// final double snapedVerticalLength = Math.round(p0p.getZ() / gridSize) * gridSize;
				// newp0p.setZ(snapedVerticalLength);
				// return newp0p.addLocal(p0);
			} else
				p.set(Math.round(p.getX() / gridSize) * gridSize, Math.round(p.getY() / gridSize) * gridSize, !snapToZ ? p.getZ() : Math.round(p.getZ() / gridSize) * gridSize);
		}
		return p;

	}

	public void addPoint(int x, int y) throws InvisibleException {
		if (container != null || !mustHaveContainer()) {
			firstPointInserted = true;
			if (drawCompleted)
				throw new RuntimeException("Drawing of this object is already completed");

			if (points.size() >= numOfEditPoints)
//				drawCompleted = true;
			    complete();
			else {
				allocateNewPoint();
				setPreviewPoint(x, y);
			}
		}
	}

	protected boolean mustHaveContainer() {
		return true;
	}

	private void allocateNewPoint() {
		for (int i = 0; i < numOfEditPoints / numOfDrawPoints; i++) {
			points.add(new Vector3());
		}
	}

	public void draw() {
		if (root == null)
			init();
		drawMesh();
		if (this instanceof Roof)
			drawGrids(getGridSize());
		else if (container != null)
			container.drawGrids(getGridSize());
		updateEditShapes();
		CollisionTreeManager.INSTANCE.removeCollisionTree(root);
		drawAnnotations();
	}

	public void drawGrids(final double gridSize) {

	}

	public void setGridsVisible(final boolean visible) {
		if (this instanceof Foundation) {
//			if (SceneManager.getInstance() != null)
				SceneManager.getInstance().setGridsVisible(visible);
		} else if (this instanceof Roof) {
			if (visible)
				drawGrids(getGridSize());
			gridsMesh.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
		} else if (container != null) {
			if (visible)
				container.drawGrids(getGridSize());
			container.gridsMesh.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
		}
	}

	protected void updateEditShapes() {
		for (int i = 0; i < points.size(); i++)
			getEditPointShape(i).setTranslation(getAbsPoint(i));
	}

	public void flattenInit() {
		flattenCenter = new Vector3();
		flatten(1.0);
		flattenCenter = new Vector3(mesh.getWorldBound().getCenter());
	}

	protected ReadOnlyVector3 getCenter() {
		return mesh.getModelBound().getCenter();
	}

	public void flatten(double flattenTime) {
		final Vector3 targetCenter = new Vector3(((UserData) mesh.getUserData()).getPrintCenter());
		root.setTranslation(targetCenter.subtractLocal(flattenCenter).multiplyLocal(flattenTime));
		root.updateGeometricState(0);
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
		offset.addLocal(getCenter());
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
			label = (BMText) labelsRoot.getChild(index);
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

	protected SizeAnnotation fetchSizeAnnot(final int annotCounter) {
		return fetchSizeAnnot(annotCounter, sizeAnnotRoot);
	}

	protected SizeAnnotation fetchSizeAnnot(final int annotCounter, final Node sizeAnnotRoot) {
		final SizeAnnotation annot;
		if (annotCounter < sizeAnnotRoot.getChildren().size()) {
			annot = (SizeAnnotation) sizeAnnotRoot.getChild(annotCounter);
			annot.getSceneHints().setCullHint(CullHint.Inherit);
			for (int i = annotCounter + 1; i < sizeAnnotRoot.getChildren().size(); i++)
				sizeAnnotRoot.getChild(i).getSceneHints().setCullHint(CullHint.Always);
		} else {
			annot = new SizeAnnotation();
			sizeAnnotRoot.attachChild(annot);
		}
		return annot;
	}

	protected AngleAnnotation fetchAngleAnnot(final int annotCounter) {
		return fetchAngleAnnot(annotCounter, angleAnnotRoot);
	}

	protected AngleAnnotation fetchAngleAnnot(final int annotCounter, final Node angleAnnotRoot) {
		final AngleAnnotation annot;
		if (annotCounter < angleAnnotRoot.getChildren().size()) {
			annot = (AngleAnnotation) angleAnnotRoot.getChild(annotCounter);
			annot.getSceneHints().setCullHint(CullHint.Inherit);
			for (int i = annotCounter + 1; i < angleAnnotRoot.getChildren().size(); i++)
				angleAnnotRoot.getChild(i).getSceneHints().setCullHint(CullHint.Always);
		} else {
			annot = new AngleAnnotation();
			angleAnnotRoot.attachChild(annot);
		}
		return annot;
	}

	public abstract void setPreviewPoint(int x, int y);

	public void delete() {
	}

	protected void drawAnnotations() {
	}

	protected abstract void drawMesh();

	public void setAnnotationsVisible(boolean visible) {
		drawAnnotations = visible;
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
		String s = this.getClass().getSimpleName() + "(" + Integer.toHexString(this.hashCode()) + ")";
		for (int i = 0; i < points.size(); i += 2)
			s += "\t" + Util.toString(getAbsPoint(i));
		s += ("\teditPoint = " + editPointIndex);
		return s;
	}

	public void setLabelOffset(double labelOffset) {
		this.labelOffset = labelOffset;
	}

	public Vector3 getAbsPoint(final int index) {
		return toAbsolute(points.get(index));
	}

	protected void drawChildren() {
		for (final HousePart child : children)
			child.draw();
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(final double height) {
		this.height = height;
	}
	
	public void reset() {
		
	}
}
