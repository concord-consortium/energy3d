package org.concord.energy3d.model;

import java.awt.Color;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.AngleAnnotation;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.simulation.SolarIrradiation;
import org.concord.energy3d.util.FontManager;
import org.concord.energy3d.util.SelectUtil;
import org.concord.energy3d.util.Util;
import org.concord.energy3d.util.WallVisitor;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Camera.ProjectionMode;
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
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.scenegraph.shape.Sphere;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.ui.text.BMText.Justify;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public abstract class HousePart implements Serializable {
	public static final OffsetState offsetState = new OffsetState();
	private static final long serialVersionUID = 1L;
	protected static final double SNAP_DISTANCE = 0.5;
	protected static int printSequence;
	protected static final float printOutlineThickness = 2f;
	private static HousePart gridsHighlightedHousePart;
	private static boolean snapToObjects = true;
	private static boolean snapToGrids = true;
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
	private transient boolean isPrintVertical;
	private transient double[] solarPotential;
	transient double[] heatLoss;
	private transient double solarPotentialToday;

	protected final ArrayList<Vector3> points;
	protected final ArrayList<HousePart> children = new ArrayList<HousePart>();
	protected HousePart container = null;
	protected double height;
	protected long id;
	protected int editPointIndex = -1;
	protected boolean drawCompleted = false;
	private double labelOffset = -0.01;
	private boolean firstPointInserted = false;
	private boolean freeze;
	private ReadOnlyColorRGBA color; // custom color

	transient Line heatArrows;
	transient static float arrowUnitArea = 2;

	private static Map<String, Texture> cachedGrayTextures = new HashMap<String, Texture>();

	static {
		offsetState.setTypeEnabled(OffsetType.Fill, true);
		offsetState.setFactor(1f);
		offsetState.setUnits(1f);
	}

	public static boolean isSnapToObjects() {
		return snapToObjects;
	}

	public static void setSnapToObjects(final boolean snapToObjects) {
		HousePart.snapToObjects = snapToObjects;
	}

	public static boolean isSnapToGrids() {
		return snapToGrids;
	}

	public static void setSnapToGrids(final boolean snapToGrid) {
		HousePart.snapToGrids = snapToGrid;
	}

	public static HousePart getGridsHighlightedHousePart() {
		return gridsHighlightedHousePart;
	}

	public static void setGridsHighlightedHousePart(final HousePart gridsHighlightedHousePart) {
		HousePart.gridsHighlightedHousePart = gridsHighlightedHousePart;
	}

	/** set the custom color of this wall */
	public void setColor(ReadOnlyColorRGBA color) {
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
			while (points.size() != numOfEditPoints)
				allocateNewPoint();
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

		if (id == 0)
			id = Scene.getInstance().nextID();

		root = new Node(toString());
		pointsRoot = new Node("Edit Points");
		sizeAnnotRoot = new Node("Size Annotations");
		sizeAnnotRoot.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		angleAnnotRoot = new Node("Angle Annotations");
		angleAnnotRoot.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		labelsRoot = new Node("Labels");

		setAnnotationsVisible(Scene.getInstance().isAnnotationsVisible());

		// Set up a reusable pick results
		for (int i = 0; i < points.size(); i++)
			addNewEditPointShape(i);

		root.attachChild(pointsRoot);
		root.attachChild(sizeAnnotRoot);
		root.attachChild(angleAnnotRoot);
		root.attachChild(labelsRoot);

		gridsMesh = new Line("Grids");
		gridsMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(2));
		gridsMesh.setDefaultColor(ColorRGBA.BLUE);
		gridsMesh.setModelBound(null);
		Util.disablePickShadowLight(gridsMesh);
		root.attachChild(gridsMesh);
		setGridsVisible(false);

		heatArrows = new Line("Heat Arrows");
		heatArrows.setLineWidth(1);
		heatArrows.setModelBound(new BoundingBox());
		Util.disablePickShadowLight(heatArrows);
		heatArrows.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(6));
		heatArrows.setDefaultColor(ColorRGBA.YELLOW);
		root.attachChild(heatArrows);

	}

	public double getGridSize() {
		return 2.5;
	}

	private void addNewEditPointShape(final int i) {
		final Sphere pointShape = new Sphere("Point", Vector3.ZERO, 8, 8, 0.1);
		pointShape.setUserData(new UserData(this, i, true));
		pointShape.updateModelBound(); // important
		pointShape.getSceneHints().setCullHint(CullHint.Always);
		pointShape.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		pointShape.getSceneHints().setCastsShadows(false);
		pointShape.setModelBound(new BoundingBox());
		pointsRoot.attachChild(pointShape);
	}

	public Mesh getEditPointShape(final int i) {
		if (i >= pointsRoot.getNumberOfChildren())
			addNewEditPointShape(i);
		return (Mesh) pointsRoot.getChild(i);
	}

	abstract protected String getTextureFileName();

	public void setOriginal(final HousePart original) {
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

	public void complete() {
		drawCompleted = true;
		orgHeight = height;
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
		if (finalize)
			orgHeight = newHeight;
	}

	public void setEditPointsVisible(final boolean visible) {
		for (int i = 0; i < pointsRoot.getNumberOfChildren(); i++)
			pointsRoot.getChild(i).getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
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
		if (!firstPointInserted || container == null)
			picked = SelectUtil.pickPart(x, y, typesOfHousePart);
		else
			picked = SelectUtil.pickPart(x, y, container);

		if (!firstPointInserted && picked != null) {
			UserData userData = null;
			if (picked != null)
				userData = picked.getUserData();
			if (container == null || userData == null || container != userData.getHousePart()) {
				if (container != null) {
					container.getChildren().remove(this);
					if (this instanceof Roof)
						((Wall) container).visitNeighbors(new WallVisitor() {
							@Override
							public void visit(final Wall wall, final Snap prev, final Snap next) {
								wall.setRoof(null);
							}
						});
				}
				if (userData != null && userData.getHousePart().isDrawCompleted()) {
					if (!(this instanceof Roof) || ((Wall) userData.getHousePart()).getRoof() == null) {
						container = userData.getHousePart();
						container.getChildren().add(this);
					}
				} else
					container = null;
			}
		}
		if (previousContainer != container) {
			if (previousContainer == null)
				SceneManager.getInstance().setGridsVisible(false);
			else if (container != null)
				previousContainer.gridsMesh.getSceneHints().setCullHint(CullHint.Always);

			if (container != null && !(this instanceof Roof)) {
				setGridsVisible(true);
			} else if (this instanceof Foundation) {
				SceneManager.getInstance().setGridsVisible(true);
			}
		}
		return picked;
	}

	protected boolean isHorizontal() {
		return true;
	}

	protected Vector3 toRelative(final ReadOnlyVector3 p) {
		final HousePart container = getContainerRelative();
		if (container == null)
			return new Vector3(p);

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

	protected Vector3 toAbsolute(final ReadOnlyVector3 p) {
		final HousePart container = getContainerRelative();
		if (container == null)
			return new Vector3(p);

		final ReadOnlyVector3 p0 = container.getAbsPoint(0);
		ReadOnlyVector3 u = container.getAbsPoint(2).subtract(p0, null);
		if (Util.isZero(u.length()))
			u = new Vector3(MathUtils.ZERO_TOLERANCE, 0, 0);
		ReadOnlyVector3 v = container.getAbsPoint(1).subtract(p0, null);

		final boolean relativeToHorizontal = getContainerRelative().isHorizontal();
		if (Util.isZero(v.length()))
			v = new Vector3(0, relativeToHorizontal ? MathUtils.ZERO_TOLERANCE : 0, relativeToHorizontal ? 0 : MathUtils.ZERO_TOLERANCE);
		final Vector3 pointOnSpace = p0.add(u.multiply(p.getX(), null), null).add(v.multiply((relativeToHorizontal) ? p.getY() : p.getZ(), null), null);
		if (relativeToHorizontal)
			pointOnSpace.setZ(pointOnSpace.getZ() + p.getZ());
		/* do not round the result, otherwise neighboring walls won't have exact same edit points */
		return pointOnSpace;
	}

	protected void snapToGrid(final Vector3 p, final ReadOnlyVector3 current, final double gridSize) {
		snapToGrid(p, current, gridSize, true);
	}

	protected void snapToGrid(final Vector3 p, final ReadOnlyVector3 previous, final double gridSize, final boolean snapToZ) {
		if (isSnapToGrids()) {
			final Vector3 newP = new Vector3();
			if (container == null)
				newP.set(Math.round(p.getX() / gridSize) * gridSize, Math.round(p.getY() / gridSize) * gridSize, !snapToZ ? p.getZ() : Math.round(p.getZ() / gridSize) * gridSize);
			else if (snapToZ) {
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
				if (getContainerRelative().isHorizontal())
					newP.setZ(p.getZ());
			}
			if (newP.distance(p) < previous.distance(p) * 0.40)
				p.set(newP);
			else
				p.set(previous);
		}
	}

	public void addPoint(final int x, final int y) {
		setPreviewPoint(x, y);
		if (container != null || !mustHaveContainer()) {
			firstPointInserted = true;
			if (drawCompleted)
				throw new RuntimeException("Drawing of this object is already completed");

			if (points.size() >= numOfEditPoints)
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
		try {
			if (root == null)
				init();

			drawMesh();
			updateTextureAndColor();
			updateEditShapes();
			clearAnnotations();
			if (isDrawable() && !isFrozen())
				drawAnnotations();
			root.updateGeometricState(0);
		} catch (final Throwable e) {
			e.printStackTrace();
		}
	}

	public void drawGrids(final double gridSize) {

	}

	public void setGridsVisible(final boolean visible) {
		if (container == null) {
			SceneManager.getInstance().setGridsVisible(visible);
		} else if (this instanceof Roof) {
			if (visible)
				drawGrids(getGridSize());
			if (gridsMesh != null)
				gridsMesh.setVisible(visible);
		} else if (container != null) {
			if (visible)
				container.drawGrids(getGridSize());
			if (container.gridsMesh != null)
				container.gridsMesh.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
		}
	}

	public void updateEditShapes() {
		for (int i = 0; i < points.size(); i++) {
			final Vector3 p = getAbsPoint(i);
			final Camera camera = SceneManager.getInstance().getCamera();
			if (camera != null && camera.getProjectionMode() != ProjectionMode.Parallel) // for Lwjgl
				getEditPointShape(i).setScale(camera.getLocation().distance(p) / 10);
			else
				getEditPointShape(i).setScale(camera.getFrustumTop() / 4);
			getEditPointShape(i).setTranslation(p);
		}
		/* remove remaining edit shapes */
		for (int i = points.size(); i < pointsRoot.getNumberOfChildren(); i++)
			pointsRoot.detachChildAt(points.size());
	}

	public void computeOrientedBoundingBox() {
		// flattenCenter.set(0, 0, 0);
		final ReadOnlyVector3 center = computeOrientedBoundingBox(mesh);
		flattenCenter.set(center);
		// flattenCenter.set(mesh.getWorldBound().getCenter());
	}

	protected ReadOnlyVector3 computeOrientedBoundingBox(final Mesh mesh) {
		final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
		buf.rewind();
		final FloatBuffer newbuf = BufferUtils.createFloatBuffer(buf.limit());
		while (buf.hasRemaining()) {
			final Vector3 v = new Vector3(buf.get(), buf.get(), buf.get());
			mesh.getWorldTransform().applyForward(v);
			newbuf.put(v.getXf()).put(v.getYf()).put(v.getZf());
		}
		final OrientedBoundingBox boundingBox = new OrientedBoundingBox();
		boundingBox.computeFromPoints(newbuf);
		boundingBox.transform(mesh.getWorldTransform().invert(null), mesh.getModelBound());
		mesh.updateWorldBound(true);
		return boundingBox.getCenter();
	}

	public void flattenInit() {
		flattenCenter = new Vector3();
		flatten(1.0);
		flattenCenter = new Vector3(mesh.getWorldBound().getCenter());
	}

	protected ReadOnlyVector3 getCenter() {
		return mesh.getModelBound().getCenter();
	}

	public void flatten(final double flattenTime) {
		if (isPrintable()) {
			if (isPrintVertical)
				root.setRotation(new Matrix3().fromAngles(0, -Math.PI / 2.0 * flattenTime, 0).multiply(root.getRotation(), null));
			final Vector3 targetCenter = new Vector3(((UserData) mesh.getUserData()).getPrintCenter());
			root.setTranslation(targetCenter.subtractLocal(flattenCenter).multiplyLocal(flattenTime));
			root.updateGeometricState(0);
		}
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
			Util.initHousePartLabel(label);
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
		} else {
			annot = new SizeAnnotation();
			sizeAnnotRoot.attachChild(annot);
		}
		return annot;
	}

	protected void clearAnnotations() {
		for (final Spatial annot : sizeAnnotRoot.getChildren())
			annot.getSceneHints().setCullHint(CullHint.Always);
		for (final Spatial annot : angleAnnotRoot.getChildren())
			annot.getSceneHints().setCullHint(CullHint.Always);
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
		updateTextureAndColor(mesh, defaultColor, Scene.getInstance().getTextureMode());
	}

	protected void updateTextureAndColor(final Mesh mesh, final ReadOnlyColorRGBA defaultColor, final TextureMode textureMode) {
		if (this instanceof Tree) { // special treatment because the same mesh of a tree has two textures (shed or not)
			final TextureState ts = new TextureState();
			final Texture texture = getTexture(getTextureFileName(), textureMode == TextureMode.Simple, defaultColor, isFrozen());
			// texture.setWrap(WrapMode.Clamp);
			ts.setTexture(texture);
			mesh.setRenderState(ts);
		} else {
			if (SceneManager.getInstance().getSolarColorMap()) {
				if (this.isDrawable() && (this instanceof Foundation || this instanceof Wall || this instanceof Roof))
					SolarIrradiation.getInstance().initMeshTextureData(mesh, mesh, this instanceof Roof ? (ReadOnlyVector3) mesh.getParent().getUserData() : getFaceDirection());
			} else if (isFrozen()) {
				mesh.clearRenderState(StateType.Texture);
				mesh.setDefaultColor(Scene.GRAY);
			} else if (textureMode == TextureMode.None || getTextureFileName() == null) {
				mesh.clearRenderState(StateType.Texture);
				mesh.setDefaultColor(defaultColor);
			} else {
				final TextureState ts = new TextureState();
				final Texture texture = getTexture(getTextureFileName(), textureMode == TextureMode.Simple, defaultColor, false);
				ts.setTexture(texture);
				mesh.setRenderState(ts);
				mesh.setDefaultColor(defaultColor != null ? defaultColor : ColorRGBA.WHITE);
			}
		}
	}

	private Texture getTexture(final String filename, final boolean isTransparent, final ReadOnlyColorRGBA defaultColor, final boolean grayout) {
		Texture texture = TextureManager.load(filename, Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true);
		if (isTransparent) {
			final Color color = new Color(defaultColor.getRed(), defaultColor.getGreen(), defaultColor.getBlue());
			final Image image = texture.getImage();
			final ByteBuffer data = image.getData(0);
			byte alpha;
			int i;
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					i = (y * image.getWidth() + x) * 4;
					alpha = data.get(i + 3);
					if (alpha == 0) { // when it is transparent, put the default color of the part
						data.put(i, (byte) color.getRed());
						data.put(i + 1, (byte) color.getGreen());
						data.put(i + 2, (byte) color.getBlue());
					}
				}
			}
			texture.setImage(image);
		}
		if (grayout) {
			final Texture grayoutTexture = cachedGrayTextures.get(filename + ":grayout");
			if (grayoutTexture != null)
				return grayoutTexture;
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
			cachedGrayTextures.put(filename + ":grayout", texture);
		}
		return texture;
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
		for (int i = 0; i < points.size(); i += 2)
			s += "\t" + Util.toString(getAbsPoint(i));
		s += ("\teditPoint = " + editPointIndex);
		return s;
	}

	public void setLabelOffset(final double labelOffset) {
		this.labelOffset = labelOffset;
	}

	public Vector3 getAbsPoint(final int index) {
		return toAbsolute(points.get(index));
	}

	protected void drawChildren() {
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
		return points.size() >= 4 && getAbsPoint(0).distance(getAbsPoint(2)) >= getGridSize() && getAbsPoint(0).distance(getAbsPoint(1)) >= getGridSize();
	}

	public void setPrintVertical(final boolean isVertical) {
		isPrintVertical = isVertical;
		flattenCenter.set(0, 0, 0);
		flatten(1.0);
		computeOrientedBoundingBox();
	}

	public boolean isValid() {
		if (!isDrawable())
			return false;
		for (final ReadOnlyVector3 p : points)
			if (!Vector3.isValid(p))
				return false;
		return true;
	}

	public double computeArea() {
		if (!isDrawCompleted())
			return 0.0;
		final Vector3 p0 = getAbsPoint(0);
		final Vector3 p1 = getAbsPoint(1);
		final Vector3 p2 = getAbsPoint(2);
		final double C = 100.0;
		final double annotationScale = Scene.getInstance().getAnnotationScale();
		return Math.round(Math.round(p2.subtract(p0, null).length() * annotationScale * C) / C * Math.round(p1.subtract(p0, null).length() * annotationScale * C) / C * C) / C;
	}

	public void setFreeze(final boolean freeze) {
		this.freeze = freeze;
	}

	public boolean isFrozen() {
		return freeze;
	}

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

	public Foundation getTopContainer() {
		HousePart c = this.getContainer();
		if (c == null)
			return null;
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
			mesh.setDefaultColor(ColorRGBA.WHITE);
		}
	}

	public void setSolarPotential(final double solarPotential[]) {
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
		if (heatLoss == null)
			return 0;
		double sum = 0;
		for (final double x : heatLoss)
			sum += x;
		return sum;
	}

	public double getSolarPotentialToday() {
		return solarPotentialToday;
	}

	public void setSolarPotentialToday(final double solarPotentialToday) {
		this.solarPotentialToday = solarPotentialToday;
	}

	public boolean isIrradiation() {
		return true;
	}

	public Mesh getIrradiationMesh() {
		return mesh;
	}

	public Spatial getIrradiationCollisionSpatial() {
		return getIrradiationMesh();
	}

	public Spatial getEditPointsRoot() {
		return pointsRoot;
	}

	public Spatial getCollisionSpatial() {
		return mesh;
	}

	double calculateHeatVector() {
		double heat = 0;
		if (heatLoss != null) {
			if (SceneManager.getInstance().getHeatFlowDaily()) {
				for (final double x : heatLoss)
					heat += x;
				heat /= (computeArea() * heatLoss.length);
				heatArrows.setDefaultColor(ColorRGBA.YELLOW);
			} else {
				int hourOfDay = Heliodon.getInstance().getCalender().get(Calendar.HOUR_OF_DAY);
				heat = heatLoss[hourOfDay * 4] / computeArea();
				heatArrows.setDefaultColor(ColorRGBA.WHITE);
			}
		}
		return heat;
	}

	void drawArrows() {

		if (SceneManager.getInstance().getHeatFlowArrows()) {

			heatArrows.getSceneHints().setCullHint(CullHint.Inherit);

			FloatBuffer arrowsVertices = heatArrows.getMeshData().getVertexBuffer();
			final int cols = (int) Math.max(2, getAbsPoint(0).distance(getAbsPoint(2)) / arrowUnitArea);
			final int rows = (int) Math.max(2, getAbsPoint(0).distance(getAbsPoint(1)) / arrowUnitArea);
			arrowsVertices = BufferUtils.createVector3Buffer(rows * cols * 6);
			heatArrows.getMeshData().setVertexBuffer(arrowsVertices);
			double heat = calculateHeatVector();
			if (heat != 0) {
				final ReadOnlyVector3 o = getAbsPoint(0);
				final ReadOnlyVector3 u = getAbsPoint(2).subtract(o, null);
				final ReadOnlyVector3 v = getAbsPoint(1).subtract(o, null);
				final ReadOnlyVector3 normal = getFaceDirection();
				Vector3 a = new Vector3();
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
				heatArrows.getMeshData().updateVertexCount();
				heatArrows.updateModelBound();
			}

		} else {

			heatArrows.getSceneHints().setCullHint(CullHint.Always);

		}

	}

	void drawArrow(ReadOnlyVector3 o, ReadOnlyVector3 normal, FloatBuffer arrowsVertices, double heat) {

		if (this instanceof Wall) {
			Wall wall = (Wall) this;
			for (HousePart x : wall.children) {
				if (x instanceof Window || x instanceof Door) {
					Vector3 vo = x.toRelative(o);
					double xmin = 2;
					double zmin = 2;
					double xmax = -2;
					double zmax = -2;
					for (Vector3 a : x.points) {
						if (a.getX() > xmax)
							xmax = a.getX();
						if (a.getZ() > zmax)
							zmax = a.getZ();
						if (a.getX() < xmin)
							xmin = a.getX();
						if (a.getZ() < zmin)
							zmin = a.getZ();
					}
					if (vo.getX() > xmin && vo.getZ() > zmin && vo.getX() < xmax && vo.getZ() < zmax)
						return;
				}
			}
		}

		arrowsVertices.put(o.getXf()).put(o.getYf()).put(o.getZf());
		final Vector3 p = new Vector3();
		normal.multiply(Scene.getInstance().getHeatVectorLength() * Math.abs(heat), p);
		Vector3 p2 = new Vector3();
		o.add(p, p2);
		arrowsVertices.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
		if (heat < 0)
			p2.set(o);
		if (heat != 0) {
			float arrowLength = 0.5f;
			p.normalizeLocal();
			double sign = Math.signum(heat);
			if (this instanceof Roof) {
				float px = (float) (p.getX() * arrowLength * sign);
				float py = (float) (p.getY() * arrowLength * sign);
				float pz = (float) (p.getZ() * arrowLength * sign);
				float yp = -pz;
				float zp = py;
				arrowsVertices.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
				arrowsVertices.put(p2.getXf() - px).put(p2.getYf() - py + yp * 0.25f).put(p2.getZf() - pz + zp * 0.25f);
				arrowsVertices.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
				arrowsVertices.put(p2.getXf() - px).put(p2.getYf() - py - yp * 0.25f).put(p2.getZf() - pz - zp * 0.25f);
			} else {
				float cos = (float) (p.dot(Vector3.UNIT_X) * sign);
				float sin = (float) (p.dot(Vector3.UNIT_Y) * sign);
				arrowsVertices.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
				arrowsVertices.put(p2.getXf() - arrowLength * cos).put(p2.getYf() - arrowLength * sin).put(p2.getZf() - arrowLength * 0.5f);
				arrowsVertices.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
				arrowsVertices.put(p2.getXf() - arrowLength * cos).put(p2.getYf() - arrowLength * sin).put(p2.getZf() + arrowLength * 0.5f);
			}
		}

	}

}
