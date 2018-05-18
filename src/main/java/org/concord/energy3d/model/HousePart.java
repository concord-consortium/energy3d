package org.concord.energy3d.model;

import java.awt.Color;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.AngleAnnotation;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.simulation.SolarRadiation;
import org.concord.energy3d.util.FontManager;
import org.concord.energy3d.util.ObjectCloner;
import org.concord.energy3d.util.SelectUtil;
import org.concord.energy3d.util.Util;
import org.concord.energy3d.util.WallVisitor;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.intersection.PickData;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Transform;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Camera.ProjectionMode;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.OffsetState;
import com.ardor3d.renderer.state.OffsetState.OffsetType;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.Sphere;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.ui.text.BMText.Justify;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

/*
 * This class should have been called a more generic name than its current one. New classes that have nothing to do with a house have to inherit from this class
 * because of the binary serialization used to save state (hence its name cannot be changed).
 *
 */

public abstract class HousePart implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final int TEXTURE_EDGE = -1;
	public static final int TEXTURE_NONE = 0;
	public static final OffsetState offsetState = new OffsetState();
	protected static final double SNAP_DISTANCE = 0.5;
	protected static final double STRETCH_ROOF_STEP = 1;
	protected static final float printOutlineThickness = 2f;
	protected static int printSequence;
	private static HousePart gridsHighlightedHousePart;
	private static boolean snapToObjects = true;
	protected transient final int numOfDrawPoints;
	protected transient final int numOfEditPoints;
	protected transient HousePart original = null;
	protected transient Node root;
	protected transient Node pointsRoot;
	protected transient Node labelsRoot;
	protected transient Node sizeAnnotRoot;
	protected transient Node angleAnnotRoot;
	protected transient Mesh mesh;
	protected transient Mesh gridsMesh;
	protected transient Vector3 flattenCenter;
	protected transient double orgHeight;
	protected transient double area;
	protected transient int containerRoofIndex;
	protected transient double solarPotentialToday;
	private transient double[] solarPotential;
	private transient double[] heatLoss;
	private transient double solarPotentialNow; // solar potential of current hour
	private transient boolean isPrintVertical;

	protected final ArrayList<Vector3> points;
	protected final ArrayList<HousePart> children = new ArrayList<HousePart>();
	protected HousePart container;
	protected double height;
	protected long id;
	protected int editPointIndex = -1;
	protected boolean drawCompleted = false;
	private ReadOnlyColorRGBA color = ColorRGBA.LIGHT_GRAY; // custom color
	private double labelOffset = -0.01;
	private boolean firstPointInserted = false;
	boolean labelCustom;
	boolean labelId;
	String labelCustomText;
	boolean lockEdit;
	int textureType = TEXTURE_NONE;
	static final ColorRGBA disabledColor = new ColorRGBA(0.5f, 0.5f, 0.5f, 0.5f);

	transient Line heatFlux;
	transient ReadOnlyVector3 pickedNormal;

	private static Map<String, Texture> cachedTextures = new HashMap<String, Texture>();

	static {
		offsetState.setTypeEnabled(OffsetType.Fill, true);
		offsetState.setFactor(1f);
		offsetState.setUnits(1f);
	}

	public static void clearCachedTextures() {
		cachedTextures.clear();
	}

	public static boolean isSnapToObjects() {
		return snapToObjects;
	}

	public static void setSnapToObjects(final boolean snapToObjects) {
		HousePart.snapToObjects = snapToObjects;
	}

	public static HousePart getGridsHighlightedHousePart() {
		return gridsHighlightedHousePart;
	}

	public static void setGridsHighlightedHousePart(final HousePart gridsHighlightedHousePart) {
		HousePart.gridsHighlightedHousePart = gridsHighlightedHousePart;
	}

	public void setColor(final ReadOnlyColorRGBA color) {
		this.color = color;
	}

	public ReadOnlyColorRGBA getColor() {
		return color;
	}

	/* if an attribute is serializable or is not needed after deserialization then they are passed as parameters to constructor */
	public HousePart(final int numOfDrawPoints, final int numOfEditPoints, final double height, final boolean complete) {
		this.numOfDrawPoints = numOfDrawPoints;
		this.numOfEditPoints = numOfEditPoints;
		this.height = height;
		points = new ArrayList<Vector3>(numOfEditPoints);
		init();
		allocateNewPoint();
		if (complete) {
			while (points.size() != numOfEditPoints) {
				allocateNewPoint();
			}
			firstPointInserted = true;
			complete();
		}
	}

	public HousePart(final int numOfDrawPoints, final int numOfEditPoints, final double height) {
		this(numOfDrawPoints, numOfEditPoints, height, false);
	}

	/* if an attribute is transient but is always needed then it should be set to default here */
	protected void init() {
		orgHeight = height;
		flattenCenter = new Vector3();
		isPrintVertical = false;

		if (id == 0) {
			id = Scene.getInstance().nextID();
		}

		root = new Node(toString());
		pointsRoot = new Node("Edit Points");
		sizeAnnotRoot = new Node("Size Annotations");
		sizeAnnotRoot.getSceneHints().setAllPickingHints(false);
		angleAnnotRoot = new Node("Angle Annotations");
		angleAnnotRoot.getSceneHints().setAllPickingHints(false);
		labelsRoot = new Node("Labels");
		labelsRoot.getSceneHints().setAllPickingHints(false);

		setAnnotationsVisible(Scene.getInstance().areAnnotationsVisible());

		// Set up a reusable pick results
		for (int i = 0; i < points.size(); i++) {
			addNewEditPointShape(i);
		}

		root.attachChild(pointsRoot);
		root.attachChild(sizeAnnotRoot);
		root.attachChild(angleAnnotRoot);
		root.attachChild(labelsRoot);

		gridsMesh = new Line("Grids");
		gridsMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(2));
		gridsMesh.setDefaultColor(new ColorRGBA(0, 0, 1, 0.25f));
		gridsMesh.setModelBound(null);
		final BlendState blendState = new BlendState();
		blendState.setBlendEnabled(true);
		gridsMesh.setRenderState(blendState);
		gridsMesh.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
		Util.disablePickShadowLight(gridsMesh);
		root.attachChild(gridsMesh);
		setGridsVisible(false);

		heatFlux = new Line("Heat Flux");
		heatFlux.setLineWidth(1);
		heatFlux.setModelBound(null);
		Util.disablePickShadowLight(heatFlux);
		heatFlux.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(6));
		heatFlux.setDefaultColor(ColorRGBA.YELLOW);
		root.attachChild(heatFlux);

		if (color == null) {
			if (this instanceof Foundation) {
				color = Scene.getInstance().getDefaultFoundationColor();
			} else if (this instanceof Door) {
				color = Scene.getInstance().getDefaultDoorColor();
			} else if (this instanceof Roof) {
				color = Scene.getInstance().getDefaultRoofColor();
			} else if (this instanceof Wall) {
				color = Scene.getInstance().getDefaultWallColor();
			} else {
				color = ColorRGBA.LIGHT_GRAY;
			}
		}

	}

	public double getGridSize() {
		return 2.5;
	}

	private void addNewEditPointShape(final int i) {
		final Sphere pointShape = new Sphere("Point", Vector3.ZERO, 8, 8, this instanceof SolarCollector ? 0.05 : 0.1);
		pointShape.setUserData(new UserData(this, i, true));
		pointShape.updateModelBound(); // important
		pointShape.setVisible(false);
		pointShape.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		pointShape.getSceneHints().setCastsShadows(false);
		pointShape.setModelBound(new BoundingBox());
		pointsRoot.attachChild(pointShape);
	}

	public Mesh getEditPointShape(final int i) {
		if (i >= pointsRoot.getNumberOfChildren()) {
			addNewEditPointShape(i);
		}
		return (Mesh) pointsRoot.getChild(i);
	}

	abstract protected String getTextureFileName();

	public void setOriginal(final HousePart original) {
		this.original = original;
		root.detachChild(pointsRoot);
		if (original.mesh != null) {
			root.detachChild(mesh);
			mesh = original.mesh.makeCopy(true);
			mesh.setUserData(new UserData(this, ((UserData) original.mesh.getUserData()).getEditPointIndex(), false));
			root.attachChild(mesh);
		}
		drawAnnotations();
		root.updateWorldBound(true);
	}

	public HousePart getOriginal() {
		return original;
	}

	public Node getRoot() {
		if (root == null) {
			init();
		}
		return root;
	}

	public ArrayList<Vector3> getPoints() {
		if (root == null) {
			init();
		}
		return points;
	}

	public void complete() {
		firstPointInserted = true;
		drawCompleted = true;
		orgHeight = height;
		try {
			if (isDrawable()) {
				computeArea();
			}
		} catch (final Exception e) {
			// It's normal to get exception when cleaning up incomplete windows
			e.printStackTrace();
		}
	}

	public boolean isDrawCompleted() {
		return drawCompleted;
	}

	public void setDrawCompleted(final boolean completed) {
		drawCompleted = completed;
	}

	public boolean isFirstPointInserted() {
		return firstPointInserted;
	}

	public ArrayList<HousePart> getChildren() {
		return children;
	}

	protected void setHeight(final double newHeight, final boolean finalize) {
		height = newHeight;
		if (finalize) {
			orgHeight = newHeight;
		}
	}

	public void setEditPointsVisible(final boolean visible) {
		if (pointsRoot != null) {
			for (int i = 0; i < pointsRoot.getNumberOfChildren(); i++) {
				getEditPointShape(i).setVisible(visible);
			}
		}
	}

	public void setEditPoint(final int i) {
		editPointIndex = i;
		drawCompleted = false;
	}

	public int getEditPoint() {
		return editPointIndex;
	}

	protected PickedHousePart pickContainer(final int x, final int y, final Class<?> typeOfHousePart) {
		return pickContainer(x, y, new Class<?>[] { typeOfHousePart });
	}

	protected PickedHousePart pickContainer(final int x, final int y, final Class<?>[] typesOfHousePart) {
		final HousePart previousContainer = container;
		final PickedHousePart picked;
		if (!firstPointInserted || container == null) {
			picked = SelectUtil.pickPart(x, y, typesOfHousePart);
		} else {
			picked = SelectUtil.pickPart(x, y, container);
		}

		if (!firstPointInserted && picked != null) {
			UserData userData = null;
			if (picked != null) {
				userData = picked.getUserData();
			}
			if (container == null || userData == null || container != userData.getHousePart()) {
				if (container != null) {
					container.getChildren().remove(this);
					if (this instanceof Roof) {
						((Wall) container).visitNeighbors(new WallVisitor() {
							@Override
							public void visit(final Wall wall, final Snap prev, final Snap next) {
								wall.setRoof(null);
							}
						});
					}
				}
				if (userData != null && userData.getHousePart().isDrawCompleted()) {
					if (!(userData.getHousePart() instanceof FoundationPolygon) && (!(this instanceof Roof) || ((Wall) userData.getHousePart()).getRoof() == null)) {
						container = userData.getHousePart();
						container.getChildren().add(this);
					}
				} else {
					container = null;
				}
			}
		}
		if (previousContainer != container) {
			if (previousContainer == null) {
				SceneManager.getInstance().setGridsVisible(false);
			} else if (container != null) {
				previousContainer.gridsMesh.getSceneHints().setCullHint(CullHint.Always);
			}

			if (container != null && !(this instanceof Roof)) {
				if (Scene.getInstance().isSnapToGrids()) {
					setGridsVisible(true);
				} else {
					setLinePatternVisible(true);
				}
			} else if (this instanceof Foundation) {
				SceneManager.getInstance().setGridsVisible(true);
			}
		}
		return picked;
	}

	protected boolean isHorizontal() {
		return true;
	}

	public Vector3 toRelative(final ReadOnlyVector3 p) {
		final HousePart container = getContainerRelative();
		if (container == null) {
			return p.clone();
		}
		final Vector3 p0 = container.getAbsPoint(0);
		final Vector3 p1 = container.getAbsPoint(1);
		final Vector3 p2 = container.getAbsPoint(2);
		final Vector2 p_2d = new Vector2(p.getX(), p.getY());
		final Vector2 p0_2d = new Vector2(p0.getX(), p0.getY());
		final double uScale = Util.projectPointOnLineScale(p_2d, p0_2d, new Vector2(p2.getX(), p2.getY()));
		final double vScale;
		final boolean relativeToHorizontal = getContainerRelative().isHorizontal();
		if (relativeToHorizontal) {
			vScale = Util.projectPointOnLineScale(p_2d, p0_2d, new Vector2(p1.getX(), p1.getY()));
			return new Vector3(uScale, vScale, p.getZ());
		} else {
			vScale = Util.projectPointOnLineScale(new Vector2(0, p.getZ()), new Vector2(0, p0.getZ()), new Vector2(0, p1.getZ()));
			return new Vector3(uScale, 0.0, vScale);
		}
	}

	public Vector3 toRelativeVector(final ReadOnlyVector3 v) {
		if (getContainerRelative() == null) { // a foundation does not have a container, return a clone of itself
			return v.clone();
		}
		return toRelative(v.add(getContainerRelative().getAbsPoint(0), null));
	}

	protected Vector3 toAbsolute(final ReadOnlyVector3 p) {
		return toAbsolute(p, null);
	}

	protected Vector3 toAbsolute(final ReadOnlyVector3 p, final Vector3 result) {
		final HousePart container = getContainerRelative();
		if (container == null) {
			return result == null ? new Vector3(p) : result.set(p);
		}

		final Vector3 u = Vector3.fetchTempInstance();
		final Vector3 v = Vector3.fetchTempInstance();
		final Vector3 p0 = Vector3.fetchTempInstance();

		Vector3 pointOnSpace;
		try {
			container.getAbsPoint(0, p0);
			container.getAbsPoint(2, u).subtract(p0, u);
			if (Util.isZero(u.length())) {
				u.set(MathUtils.ZERO_TOLERANCE, 0, 0);
			}
			container.getAbsPoint(1, v).subtract(p0, v);

			final boolean relativeToHorizontal = getContainerRelative().isHorizontal();
			if (Util.isZero(v.length())) {
				v.set(0, relativeToHorizontal ? MathUtils.ZERO_TOLERANCE : 0, relativeToHorizontal ? 0 : MathUtils.ZERO_TOLERANCE);
			}
			pointOnSpace = p0.add(u.multiply(p.getX(), u), u).add(v.multiply((relativeToHorizontal) ? p.getY() : p.getZ(), v), result);
			if (relativeToHorizontal) {
				pointOnSpace.setZ(pointOnSpace.getZ() + p.getZ());
			}
		} finally {
			Vector3.releaseTempInstance(u);
			Vector3.releaseTempInstance(v);
			Vector3.releaseTempInstance(p0);
		}
		/* do not round the result, otherwise neighboring walls won't have exact same edit points */
		return pointOnSpace;
	}

	protected void snapToGrid(final Vector3 p, final ReadOnlyVector3 current, final double gridSize) {
		snapToGrid(p, current, gridSize, true);
	}

	protected void snapToGrid(final Vector3 p, final ReadOnlyVector3 previous, final double gridSize, final boolean snapToZ) {
		if (Scene.getInstance().isSnapToGrids()) {
			final Vector3 newP = new Vector3();
			if (container == null) {
				newP.set(Math.round(p.getX() / gridSize) * gridSize, Math.round(p.getY() / gridSize) * gridSize, !snapToZ ? p.getZ() : Math.round(p.getZ() / gridSize) * gridSize);
			} else if (snapToZ) {
				final double baseZ = getAbsPoint(0).getZ();
				final double vScale = Util.projectPointOnLineScale(new Vector2(0, p.getZ()), new Vector2(), new Vector2(0, 1)) - baseZ;
				final double vScaleRounded = Math.round(vScale / gridSize) * gridSize;
				newP.set(p);
				newP.setZ(baseZ + vScaleRounded);
			} else {
				final Vector3 p0 = getContainerRelative().getAbsPoint(0);
				final Vector3 p1 = getContainerRelative().getAbsPoint(1);
				final Vector3 p2 = getContainerRelative().getAbsPoint(2);
				final ReadOnlyVector3 u = p2.subtract(p0, null);
				final ReadOnlyVector3 v = p1.subtract(p0, null);
				final double uScale = Util.projectPointOnLineScale(p, p0, p2);
				final double vScale = Util.projectPointOnLineScale(p, p0, p1);
				final double uScaleRounded = Math.round(u.length() * uScale / gridSize) * gridSize;
				final double vScaleRounded = Math.round(v.length() * vScale / gridSize) * gridSize;
				newP.set(p0).addLocal(u.normalize(null).multiplyLocal(uScaleRounded)).addLocal(v.normalize(null).multiplyLocal(vScaleRounded));
				if (getContainerRelative().isHorizontal()) {
					newP.setZ(p.getZ());
				}
			}
			if (newP.distance(p) < previous.distance(p) * 0.40) {
				p.set(newP);
			} else {
				p.set(previous);
			}
		}
	}

	public void addPoint(final int x, final int y) {
		setPreviewPoint(x, y);
		if (container != null || !mustHaveContainer()) {
			firstPointInserted = true;
			if (drawCompleted) {
				throw new RuntimeException("Drawing of this object is already completed");
			}

			if (points.size() >= numOfEditPoints) {
				complete();
			} else {
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
		try {
			if (root == null) {
				init();
			}

			drawMesh();
			if (isDrawable()) {
				computeArea();
			}
			updateTextureAndColor();
			updateEditShapes();
			updateEditPoints();
			clearAnnotations();
			if (isDrawable()) {
				drawAnnotations();
			}
			root.updateGeometricState(0);
		} catch (final Throwable e) {
			e.printStackTrace();
		}
	}

	public void drawGrids(final double gridSize) {
	}

	public void setGridsVisible(final boolean visible) {
		if (container == null) {
			SceneManager.getInstance().setGridsVisible(Scene.getInstance().isSnapToGrids() && visible);
		} else if (this instanceof Roof) {
			if (visible) {
				drawGrids(getGridSize());
			}
			if (gridsMesh != null) {
				gridsMesh.setVisible(Scene.getInstance().isSnapToGrids() && visible);
			}
		} else if (container != null) {
			if (visible) {
				container.drawGrids(getGridSize());
			}
			if (container.gridsMesh != null) {
				container.gridsMesh.getSceneHints().setCullHint(Scene.getInstance().isSnapToGrids() && visible ? CullHint.Inherit : CullHint.Always);
			}
		}
	}

	public void setLinePatternVisible(final boolean visible) {
		if (container instanceof Foundation) {
			final Foundation foundation = (Foundation) container;
			foundation.drawLinePattern();
			foundation.linePatternMesh.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
		}
	}

	public void updateEditShapes() {
		final Vector3 p = Vector3.fetchTempInstance();
		try {
			for (int i = 0; i < points.size(); i++) {
				getAbsPoint(i, p);
				if (!Double.isFinite(p.getZ())) {
					p.setZ(1000);
				}
				getEditPointShape(i).setTranslation(p);
			}
			for (int i = 0; i < pointsRoot.getNumberOfChildren(); i++) {
				final Camera camera = SceneManager.getInstance().getCamera();
				if (camera != null && camera.getProjectionMode() != ProjectionMode.Parallel) {
					final double distance = camera.getLocation().distance(getEditPointShape(i).getTranslation());
					getEditPointShape(i).setScale(distance > 0.1 ? distance / 10 : 0.01);
				} else {
					getEditPointShape(i).setScale(camera.getFrustumTop() / 4);
				}
			}
		} finally {
			Vector3.releaseTempInstance(p);
		}
		// /* remove remaining edit shapes */
		// for (int i = points.size(); i < pointsRoot.getNumberOfChildren(); i++) {
		// pointsRoot.detachChildAt(points.size());
		// }
	}

	public void computeOrientedBoundingBox() {
		final ReadOnlyVector3 center = computeOrientedBoundingBox(mesh);
		flattenCenter.set(center);
	}

	protected static ReadOnlyVector3 computeOrientedBoundingBox(final Mesh mesh) {
		return Util.getOrientedBoundingBox(mesh).getCenter();
	}

	protected ReadOnlyVector3 getCenter() {
		return mesh.getModelBound().getCenter();
	}

	public void flatten(final double flattenTime) {
		if (isPrintable()) {
			if (isPrintVertical) {
				root.setRotation(new Matrix3().fromAngles(0, -Math.PI / 2.0 * flattenTime, 0).multiply(root.getRotation(), null));
			}
			final Vector3 targetCenter = new Vector3(((UserData) mesh.getUserData()).getPrintCenter());
			root.setTranslation(targetCenter.subtractLocal(flattenCenter).multiplyLocal(flattenTime));
			root.updateGeometricState(0);
		}
	}

	public boolean isPrintable() {
		return true;
	}

	public int drawLabels(int printSequence) {
		if (!isPrintable()) {
			return printSequence;
		}
		final String text = "(" + (printSequence++ + 1) + ")";
		final BMText label = fetchBMText(text, 0);

		final Vector3 offset;
		if (original == null) {
			offset = getNormal().multiply(0.5, null);
		} else {
			offset = new Vector3(0, labelOffset, 0);
		}
		root.getTransform().applyInverseVector(offset);
		offset.addLocal(getCenter());
		label.setTranslation(offset);
		return printSequence;
	}

	public void hideLabels() {
		for (final Spatial label : labelsRoot.getChildren()) {
			label.getSceneHints().setCullHint(CullHint.Always);
		}
	}

	protected BMText fetchBMText(final String text, final int index) {
		final BMText label;
		if (labelsRoot.getChildren().size() > index) {
			label = (BMText) labelsRoot.getChild(index);
			label.setText(text);
			label.getSceneHints().setCullHint(CullHint.Inherit);
		} else {
			label = new BMText("Label Text", text, FontManager.getInstance().getPartNumberFont(), Align.Center, Justify.Center);
			Util.initHousePartLabel(label);
			labelsRoot.attachChild(label);
		}
		return label;
	}

	public ReadOnlyVector3 getNormal() {
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
		} else {
			annot = new SizeAnnotation();
			sizeAnnotRoot.attachChild(annot);
		}
		return annot;
	}

	protected void clearAnnotations() {
		for (final Spatial annot : sizeAnnotRoot.getChildren()) {
			annot.getSceneHints().setCullHint(CullHint.Always);
		}
		for (final Spatial annot : angleAnnotRoot.getChildren()) {
			annot.getSceneHints().setCullHint(CullHint.Always);
		}
	}

	protected AngleAnnotation fetchAngleAnnot(final int annotCounter) {
		return fetchAngleAnnot(annotCounter, angleAnnotRoot);
	}

	protected AngleAnnotation fetchAngleAnnot(final int annotCounter, final Node angleAnnotRoot) {
		final AngleAnnotation annot;
		if (annotCounter < angleAnnotRoot.getChildren().size()) {
			annot = (AngleAnnotation) angleAnnotRoot.getChild(annotCounter);
			annot.getSceneHints().setCullHint(CullHint.Inherit);
		} else {
			annot = new AngleAnnotation();
			angleAnnotRoot.attachChild(annot);
		}
		return annot;
	}

	public abstract void setPreviewPoint(int x, int y);

	public void delete() {
	}

	public void drawAnnotations() {
	}

	protected abstract void drawMesh();

	public void setAnnotationsVisible(final boolean visible) {
		final CullHint cull = visible ? CullHint.Inherit : CullHint.Always;
		sizeAnnotRoot.getSceneHints().setCullHint(cull);
		angleAnnotRoot.getSceneHints().setCullHint(cull);
	}

	public abstract void updateTextureAndColor();

	protected void updateTextureAndColor(final Mesh mesh, final ReadOnlyColorRGBA defaultColor) {
		if (this instanceof Tree) { // special treatment because the same mesh of a tree has two textures (shed or not)
			final TextureState ts = new TextureState();
			final Texture texture = getTexture(getTextureFileName(), textureType == TEXTURE_EDGE, defaultColor, lockEdit);
			ts.setTexture(texture);
			mesh.setRenderState(ts);
		} else {
			if (SceneManager.getInstance().getSolarHeatMap()) {
				if (isDrawable()) {
					if (this instanceof Foundation || this instanceof Wall || this instanceof Roof || this instanceof Floor) {
						SolarRadiation.getInstance().initMeshTextureData(mesh, mesh, this instanceof Roof ? (ReadOnlyVector3) mesh.getParent().getUserData() : getNormal());
					}
				}
			} else if (getTextureFileName() == null) {
				mesh.clearRenderState(StateType.Texture);
				mesh.setDefaultColor(defaultColor);
			} else {
				final TextureState ts = new TextureState();
				final Texture texture = getTexture(getTextureFileName(), textureType == TEXTURE_EDGE, defaultColor, false);
				ts.setTexture(texture);
				mesh.setRenderState(ts);
				mesh.setDefaultColor(ColorRGBA.WHITE);
			}
		}
	}

	private Texture getTexture(final String filename, final boolean isTransparent, final ReadOnlyColorRGBA color, final boolean grayout) {
		Texture texture = TextureManager.load(filename, Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true);
		if (isTransparent) {
			final Color c = new Color(color.getRed(), color.getGreen(), color.getBlue());
			final Texture coloredTexture = cachedTextures.get(filename + ":" + c);
			if (coloredTexture != null) {
				return coloredTexture;
			}
			final Image image = texture.getImage();
			final Image coloredImage = new Image(); // make a copy
			coloredImage.setDataFormat(image.getDataFormat());
			coloredImage.setDataType(image.getDataType());
			coloredImage.setWidth(image.getWidth());
			coloredImage.setHeight(image.getHeight());
			coloredImage.setMipMapByteSizes(image.getMipMapByteSizes());
			final ByteBuffer data = image.getData(0);
			final ByteBuffer coloredData = ByteBuffer.allocate(data.capacity());
			byte alpha;
			int i;
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					i = (y * image.getWidth() + x) * 4;
					alpha = data.get(i + 3);
					if (alpha == 0) { // when it is transparent, put the default color of the part
						coloredData.put(i, (byte) c.getRed());
						coloredData.put(i + 1, (byte) c.getGreen());
						coloredData.put(i + 2, (byte) c.getBlue());
					}
				}
			}
			coloredImage.addData(coloredData);
			texture = TextureManager.loadFromImage(coloredImage, Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat);
			cachedTextures.put(filename + ":" + c, texture);
		}
		if (grayout) {
			final Texture grayoutTexture = cachedTextures.get(filename + ":grayout");
			if (grayoutTexture != null) {
				return grayoutTexture;
			}
			final Image image = texture.getImage();
			final Image grayImage = new Image(); // make a copy
			grayImage.setDataFormat(image.getDataFormat());
			grayImage.setDataType(image.getDataType());
			grayImage.setWidth(image.getWidth());
			grayImage.setHeight(image.getHeight());
			grayImage.setMipMapByteSizes(image.getMipMapByteSizes());
			final ByteBuffer data = image.getData(0);
			final ByteBuffer grayData = ByteBuffer.allocate(data.capacity());
			byte alpha, red, green, blue, gray;
			int i;
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					i = (y * image.getWidth() + x) * 4;
					red = data.get(i);
					green = data.get(i + 1);
					blue = data.get(i + 2);
					alpha = data.get(i + 3);
					gray = (byte) Math.min(red, green);
					gray = (byte) Math.min(blue, gray);
					grayData.put(i, gray);
					grayData.put(i + 1, gray);
					grayData.put(i + 2, gray);
					grayData.put(i + 3, alpha);
				}
			}
			grayImage.addData(grayData);
			texture = TextureManager.loadFromImage(grayImage, Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat);
			cachedTextures.put(filename + ":grayout", texture);
		}
		return texture;
	}

	public void setContainer(final HousePart container) {
		this.container = container;
	}

	public HousePart getContainer() {
		return container;
	}

	protected HousePart getContainerRelative() {
		return container;
	}

	public Mesh getMesh() {
		return mesh;
	}

	@Override
	public String toString() {
		String s = this.getClass().getSimpleName() + "(" + id + ")";
		for (int i = 0; i < points.size(); i += 2) {
			s += "  " + Util.toString2D(getAbsPoint(i));
		}
		s += ("  editPoint=" + editPointIndex);
		return s;
	}

	public void setLabelOffset(final double labelOffset) {
		this.labelOffset = labelOffset;
	}

	public Vector3 getAbsPoint(final int index) {
		if (index >= points.size()) {
			Scene.getInstance().fixProblems(false);
			throw new RuntimeException("Energy3D OutOfBoundException for HousePart: " + toString());
		}
		return toAbsolute(points.get(index), null);
	}

	public Vector3 getAbsPoint(final int index, final Vector3 result) {
		if (index >= points.size()) {
			return null;
		}
		return toAbsolute(points.get(index), result);
	}

	public void drawChildren() {
		for (final HousePart child : children) {
			child.drawChildren();
			child.draw();
		}
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(final double height) {
		this.height = height;
	}

	public void reset() {

	}

	public boolean isDrawable() {
		return points.size() >= 4 && getAbsPoint(0).distance(getAbsPoint(2)) >= 0.1 * getGridSize() && getAbsPoint(0).distance(getAbsPoint(1)) >= 0.1 * getGridSize();
	}

	public void setPrintVertical(final boolean isVertical) {
		isPrintVertical = isVertical;
		flattenCenter.set(0, 0, 0);
		flatten(1.0);
		computeOrientedBoundingBox();
	}

	public boolean isValid() {
		// if (!isDrawable()) // The call to isDrawable() is moved to sub classes to avoid Wall/Window infinite calls
		// return false;
		for (final ReadOnlyVector3 p : points) {
			if (!Vector3.isValid(p)) {
				return false;
			}
		}
		return true;
	}

	public double getArea() {
		if (Util.isZero(area)) {
			computeArea();
		}
		return area;
	}

	protected abstract void computeArea();

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public Foundation getTopContainer() {
		HousePart c = getContainer();
		if (c == null) {
			return null;
		}
		HousePart x = null;
		while (c != null) {
			x = c;
			c = c.getContainer();
		}
		return (Foundation) x;
	}

	public void setHighlight(final boolean highlight) {
		if (highlight) {
			final OffsetState offset = new OffsetState();
			offset.setFactor(-1);
			offset.setUnits(-1);
			mesh.setRenderState(offset);
			mesh.setDefaultColor(ColorRGBA.RED);
		} else {
			mesh.clearRenderState(StateType.Offset);
			mesh.setDefaultColor(getColor());
		}
	}

	public void setSolarPotential(final double[] solarPotential) {
		this.solarPotential = solarPotential;
	}

	public double[] getSolarPotential() {
		return solarPotential;
	}

	public void setHeatLoss(final double[] heatLoss) {
		this.heatLoss = heatLoss;
	}

	public double[] getHeatLoss() {
		return heatLoss;
	}

	public double getTotalHeatLoss() {
		if (heatLoss == null) {
			return 0;
		}
		double sum = 0;
		for (final double x : heatLoss) {
			sum += x;
		}
		return sum;
	}

	/** Note: For solar panels, this stores the energy that is actually converted into electricity */
	public double getSolarPotentialNow() {
		return solarPotentialNow;
	}

	/** Note: For solar panels, this stores the energy that is actually converted into electricity */
	public void setSolarPotentialNow(final double solarPotentialNow) {
		this.solarPotentialNow = solarPotentialNow;
	}

	/** Note: For solar panels, this stores the energy that is actually converted into electricity */
	public double getSolarPotentialToday() {
		return solarPotentialToday;
	}

	/** Note: For solar panels, this stores the energy that is actually converted into electricity */
	public void setSolarPotentialToday(final double solarPotentialToday) {
		this.solarPotentialToday = solarPotentialToday;
	}

	public Mesh getRadiationMesh() {
		return mesh;
	}

	public Spatial getRadiationCollisionSpatial() {
		return getRadiationMesh();
	}

	public Spatial getEditPointsRoot() {
		return pointsRoot;
	}

	public Spatial getCollisionSpatial() {
		return mesh;
	}

	/** use the lightness of color to approximate albedo */
	public float getAlbedo() {
		if (textureType != TEXTURE_NONE && textureType != TEXTURE_EDGE) { // TODO
			return 0.2f;
		}
		ReadOnlyColorRGBA c = null;
		if (color != null) {
			c = color;
		} else {
			if (this instanceof Foundation) {
				c = Scene.getInstance().getDefaultFoundationColor();
			} else if (this instanceof Door) {
				c = Scene.getInstance().getDefaultDoorColor();
			} else if (this instanceof Roof) {
				c = Scene.getInstance().getDefaultRoofColor();
			} else if (this instanceof Wall) {
				c = Scene.getInstance().getDefaultWallColor();
			} else {
				c = ColorRGBA.LIGHT_GRAY;
			}
		}
		float min = Math.min(c.getRed(), c.getGreen());
		min = Math.min(min, c.getBlue());
		float max = Math.max(c.getRed(), c.getGreen());
		max = Math.max(max, c.getBlue());
		return 0.5f * (min + max);
	}

	double calculateHeatVector() {
		double heat = 0;
		double a = area;
		if (this instanceof Foundation) {
			final Building building = new Building((Foundation) this);
			if (building.areWallsAcceptable()) {
				building.calculate(true);
				a = building.getArea(); // reduce the area of the foundation to the floor area within the building envelope
			}
		}
		if (heatLoss != null) {
			if (SceneManager.getInstance().isHeatFluxDaily()) {
				for (final double x : heatLoss) {
					heat += x;
				}
				heat /= a * heatLoss.length;
				heatFlux.setDefaultColor(ColorRGBA.YELLOW);
			} else {
				final int hourOfDay4 = Heliodon.getInstance().getCalendar().get(Calendar.HOUR_OF_DAY) * 4;
				heat = (heatLoss[hourOfDay4] + heatLoss[hourOfDay4 + 1] + heatLoss[hourOfDay4 + 2] + heatLoss[hourOfDay4 + 3]) / (4 * a);
				heatFlux.setDefaultColor(ColorRGBA.WHITE);
			}
		}
		return heat;
	}

	public void drawHeatFlux() {
		FloatBuffer arrowsVertices = heatFlux.getMeshData().getVertexBuffer();
		final int cols = (int) Math.max(2, getAbsPoint(0).distance(getAbsPoint(2)) / Scene.getInstance().getHeatVectorGridSize());
		final int rows = (int) Math.max(2, getAbsPoint(0).distance(getAbsPoint(1)) / Scene.getInstance().getHeatVectorGridSize());
		arrowsVertices = BufferUtils.createVector3Buffer(rows * cols * 6);
		heatFlux.getMeshData().setVertexBuffer(arrowsVertices);
		final double heat = calculateHeatVector();
		if (heat != 0) {
			final ReadOnlyVector3 o = getAbsPoint(0);
			final ReadOnlyVector3 u = getAbsPoint(2).subtract(o, null);
			final ReadOnlyVector3 v = getAbsPoint(1).subtract(o, null);
			final ReadOnlyVector3 normal = getNormal();
			final Vector3 a = new Vector3();
			double g, h;
			for (int j = 0; j < cols; j++) {
				h = j + 0.5;
				for (int i = 0; i < rows; i++) {
					g = i + 0.5;
					a.setX(o.getX() + g * v.getX() / rows + h * u.getX() / cols);
					a.setY(o.getY() + g * v.getY() / rows + h * u.getY() / cols);
					a.setZ(o.getZ() + g * v.getZ() / rows + h * u.getZ() / cols);
					drawArrow(a, normal, arrowsVertices, heat);
				}
			}
			heatFlux.getMeshData().updateVertexCount();
			heatFlux.updateModelBound();
		}
		updateHeatFluxVisibility();
	}

	protected void drawArrow(final ReadOnlyVector3 o, final ReadOnlyVector3 normal, final FloatBuffer arrowsVertices, final double heat) {
		if (this instanceof Wall) {
			final Wall wall = (Wall) this;
			for (final HousePart x : wall.children) {
				if (x instanceof Window || x instanceof Door) {
					final Vector3 vo = x.toRelative(o);
					double xmin = 2;
					double zmin = 2;
					double xmax = -2;
					double zmax = -2;
					for (final Vector3 a : x.points) {
						if (a.getX() > xmax) {
							xmax = a.getX();
						}
						if (a.getZ() > zmax) {
							zmax = a.getZ();
						}
						if (a.getX() < xmin) {
							xmin = a.getX();
						}
						if (a.getZ() < zmin) {
							zmin = a.getZ();
						}
					}
					if (vo.getX() > xmin && vo.getZ() > zmin && vo.getX() < xmax && vo.getZ() < zmax) {
						return;
					}
				}
			}
		}

		arrowsVertices.put(o.getXf()).put(o.getYf()).put(o.getZf());
		final Vector3 p = new Vector3();
		normal.multiply(Scene.getInstance().getHeatVectorLength() * Math.abs(heat), p);
		final Vector3 p2 = new Vector3();
		o.add(p, p2);
		arrowsVertices.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
		if (heat < 0) {
			p2.set(o);
		}
		if (heat != 0) {
			final float arrowLength = 0.5f;
			p.normalizeLocal();
			final double sign = Math.signum(heat);
			if (this instanceof Roof) {
				final float px = (float) (p.getX() * arrowLength * sign);
				final float py = (float) (p.getY() * arrowLength * sign);
				final float pz = (float) (p.getZ() * arrowLength * sign);
				final float yp = -pz;
				final float zp = py;
				arrowsVertices.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
				arrowsVertices.put(p2.getXf() - px).put(p2.getYf() - py + yp * 0.25f).put(p2.getZf() - pz + zp * 0.25f);
				arrowsVertices.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
				arrowsVertices.put(p2.getXf() - px).put(p2.getYf() - py - yp * 0.25f).put(p2.getZf() - pz - zp * 0.25f);
			} else if (this instanceof Foundation) {
				final float cos = (float) (p.dot(Vector3.UNIT_X) * sign);
				final float sin = (float) (p.dot(Vector3.UNIT_Z) * sign);
				arrowsVertices.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
				arrowsVertices.put(p2.getXf() - arrowLength * cos).put(p2.getYf() - arrowLength * 0.5f).put(p2.getZf() - arrowLength * sin);
				arrowsVertices.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
				arrowsVertices.put(p2.getXf() - arrowLength * cos).put(p2.getYf() + arrowLength * 0.5f).put(p2.getZf() - arrowLength * sin);
			} else {
				final float cos = (float) (p.dot(Vector3.UNIT_X) * sign);
				final float sin = (float) (p.dot(Vector3.UNIT_Y) * sign);
				arrowsVertices.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
				arrowsVertices.put(p2.getXf() - arrowLength * cos).put(p2.getYf() - arrowLength * sin).put(p2.getZf() - arrowLength * 0.5f);
				arrowsVertices.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
				arrowsVertices.put(p2.getXf() - arrowLength * cos).put(p2.getYf() - arrowLength * sin).put(p2.getZf() + arrowLength * 0.5f);
			}
		}
	}

	public void updateHeatFluxVisibility() {
		heatFlux.setVisible(Scene.getInstance().getAlwaysComputeHeatFluxVectors() && SceneManager.getInstance().areHeatFluxVectorsVisible());
	}

	public abstract boolean isCopyable();

	public HousePart copy(final boolean check) {
		final HousePart c = (HousePart) ObjectCloner.deepCopy(this);
		c.container = this.container;
		c.id = Scene.getInstance().nextID();
		c.lockEdit = false;
		return c;
	}

	public Vector3 getAbsCenter() {
		double x = 0, y = 0, z = 0;
		final int n = points.size();
		for (int i = 0; i < n; i++) {
			final Vector3 v = getAbsPoint(i);
			x += v.getX();
			y += v.getY();
			z += v.getZ();
		}
		final double invert = 1.0 / n;
		return new Vector3(x * invert, y * invert, z * invert);
	}

	protected ReadOnlyVector3 computeNormalAndKeepOnSurface() {
		if (container == null) {
			return null;
		}
		if (container instanceof Rack) {
			final Rack rack = (Rack) container;
			final PickResults pickResults = new PrimitivePickResults();
			final Ray3 ray = new Ray3(getAbsPoint(0).multiplyLocal(1, 1, 0), Vector3.UNIT_Z);
			PickingUtil.findPick(container.getCollisionSpatial(), ray, pickResults, false);
			if (pickResults.getNumber() != 0) {
				final PickData pickData = pickResults.getPickData(0);
				final Vector3 p = pickData.getIntersectionRecord().getIntersectionPoint(0);
				points.get(0).setZ(p.getZ());
			} else {
				if (rack.getBaseHeight() < Math.abs(0.5 * rack.getRackHeight() / Scene.getInstance().getAnnotationScale() * Math.sin(Math.toRadians(rack.getTiltAngle())))) {
					final Ray3 ray2 = new Ray3(getAbsPoint(0).multiplyLocal(1, 1, 0), Vector3.NEG_UNIT_Z);
					PickingUtil.findPick(container.getCollisionSpatial(), ray2, pickResults, false);
					if (pickResults.getNumber() != 0) {
						final PickData pickData = pickResults.getPickData(0);
						final Vector3 p = pickData.getIntersectionRecord().getIntersectionPoint(0);
						points.get(0).setZ(p.getZ());
					}
				}
			}
			return rack.getNormal();
		} else if (container instanceof Roof) {
			final Roof roof = (Roof) container;
			final int[] editPointToRoofIndex = new int[points.size()];
			final PickResults pickResults = new PrimitivePickResults();
			for (int i = 0; i < points.size(); i++) {
				pickResults.clear();
				final Ray3 ray = new Ray3(getAbsPoint(i).multiplyLocal(1, 1, 0), Vector3.UNIT_Z);
				for (final Spatial roofPart : roof.getRoofPartsRoot().getChildren()) {
					if (roofPart.getSceneHints().getCullHint() != CullHint.Always) {
						PickingUtil.findPick(((Node) roofPart).getChild(0), ray, pickResults, false);
						if (pickResults.getNumber() != 0) {
							break;
						}
					}
				}
				if (pickResults.getNumber() != 0) {
					final PickData pickData = pickResults.getPickData(0);
					final Vector3 p = pickData.getIntersectionRecord().getIntersectionPoint(0);
					points.get(i).setZ(p.getZ());
					final UserData userData = (UserData) ((Spatial) pickData.getTarget()).getUserData();
					final int roofPartIndex = userData.getEditPointIndex();
					editPointToRoofIndex[i] = roofPartIndex;
				}

				// find roofPart with most edit points on it
				containerRoofIndex = editPointToRoofIndex[0];
				if (points.size() > 1) {
					containerRoofIndex = 0;
					final Map<Integer, Integer> counts = new HashMap<Integer, Integer>(points.size());
					for (final int roofIndex : editPointToRoofIndex) {
						counts.put(roofIndex, counts.get(roofIndex) == null ? 1 : counts.get(roofIndex) + 1);
					}

					int highestCount = 0;
					for (final int roofIndex : editPointToRoofIndex) {
						if (counts.get(roofIndex) > highestCount) {
							highestCount = counts.get(roofIndex);
							containerRoofIndex = roofIndex;
						}
					}
				}
			}
			return (ReadOnlyVector3) roof.getRoofPartsRoot().getChild(containerRoofIndex).getUserData();
		} else if (container instanceof Foundation) {
			final Foundation foundation = (Foundation) container;
			final List<Node> nodes = foundation.getImportedNodes();
			if (nodes != null) {
				final Map<Vector3, ReadOnlyVector3> intersections = new HashMap<Vector3, ReadOnlyVector3>();
				final PickResults pickResults = new PrimitivePickResults();
				for (final Node n : nodes) {
					for (final Spatial s : n.getChildren()) {
						if (s instanceof Mesh) {
							final Mesh m = (Mesh) s;
							pickResults.clear();
							PickingUtil.findPick(m, new Ray3(getAbsPoint(0).multiplyLocal(1, 1, 0), Vector3.UNIT_Z), pickResults, false);
							if (pickResults.getNumber() > 0) {
								intersections.put(pickResults.getPickData(0).getIntersectionRecord().getIntersectionPoint(0), ((UserData) m.getUserData()).getNormal());
							}
						}
					}
				}
				if (!intersections.isEmpty()) {
					double zmax = -Double.MAX_VALUE;
					ReadOnlyVector3 normal = null;
					for (final Vector3 v : intersections.keySet()) {
						if (v.getZ() > zmax) {
							zmax = v.getZ();
							normal = intersections.get(v);
						}
					}
					if (normal != null) {
						pickedNormal = normal;
						return normal;
					}
				}
			}
		}

		return container.getNormal();

	}

	protected boolean fits(final HousePart child) {
		return true;
	}

	public void clearLabels() {
		labelId = false;
		labelCustom = false;
	}

	public void setLabelId(final boolean labelId) {
		this.labelId = labelId;
	}

	public boolean getLabelId() {
		return labelId;
	}

	public void setLabelCustom(final boolean labelCustom) {
		this.labelCustom = labelCustom;
	}

	public boolean getLabelCustom() {
		return labelCustom;
	}

	public void setLabelCustomText(final String labelCustomText) {
		this.labelCustomText = labelCustomText;
	}

	public String getLabelCustomText() {
		return labelCustomText;
	}

	public void addPrintMeshes(final List<Mesh> list) {
	}

	protected void addPrintMesh(final List<Mesh> list, final Mesh mesh) {
		if (mesh.getSceneHints().getCullHint() != CullHint.Always) {
			final Mesh newMesh = mesh.makeCopy(false);
			final MaterialState material = new MaterialState();
			material.setDiffuse(mesh.getDefaultColor());
			newMesh.setRenderState(material);
			newMesh.getMeshData().transformVertices((Transform) mesh.getWorldTransform());
			newMesh.getMeshData().transformNormals((Transform) mesh.getWorldTransform(), true);
			list.add(newMesh);
		}
	}

	void updateEditPoints() {
		for (int i = 0; i < pointsRoot.getNumberOfChildren(); i++) {
			getEditPointShape(i).setDefaultColor(lockEdit ? disabledColor : ColorRGBA.WHITE);
		}
	}

	public void setLockEdit(final boolean b) {
		lockEdit = b;
		updateEditPoints();
	}

	public boolean getLockEdit() {
		return lockEdit;
	}

	public void setTextureType(final int textureType) {
		this.textureType = textureType;
	}

	public int getTextureType() {
		return textureType;
	}

}
