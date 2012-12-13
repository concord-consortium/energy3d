package org.concord.energy3d.model;

import java.awt.Color;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.AngleAnnotation;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.util.FontManager;
import org.concord.energy3d.util.SelectUtil;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
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
	private static final long serialVersionUID = 1L;

	public static final OffsetState offsetState = new OffsetState();

	protected static final double SNAP_DISTANCE = 0.5;
	protected static int printSequence;
	protected static final float printWireframeThickness = 2f;
	private static HousePart gridsHighlightedHousePart;
	private static boolean snapToObjects = false;
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
	protected transient boolean relativeToHorizontal;
	protected final ArrayList<Vector3> points;
	protected final ArrayList<HousePart> children = new ArrayList<HousePart>();
	protected HousePart container = null;
	protected double height;
	protected int editPointIndex = -1;
	protected boolean drawCompleted = false;
	private transient boolean isPrintVertical;
	private double labelOffset = -0.01;
	private boolean firstPointInserted = false;
	private boolean freeze;

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
		relativeToHorizontal = false;
		orgHeight = height;
		flattenCenter = new Vector3();
		isPrintVertical = false;

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
	}

	public double getGridSize() {
		return 1.0;
	}

	private void addNewEditPointShape(final int i) {
		final Sphere pointShape = new Sphere("Point", Vector3.ZERO, 8, 8, 0.05);
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

	protected String getTextureFileName() {
		return null;
	}

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

	protected PickedHousePart pick(final int x, final int y, final Class<?>[] typesOfHousePart) {
		for (final Class<?> c : typesOfHousePart) {
			final PickedHousePart picked = pickContainer(x, y, c);
			if (picked != null)
				return picked;
		}
		return null;
	}

	protected PickedHousePart pickContainer(final int x, final int y, final Class<?> typeOfHousePart) {
		final HousePart previousContainer = container;
		final PickedHousePart picked;
		if (!firstPointInserted || container == null)
			picked = SelectUtil.pickPart(x, y, typeOfHousePart);
		else
			picked = SelectUtil.pickPart(x, y, container == null ? null : container.getRoot());

		if (!firstPointInserted && picked != null) {
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
		if (previousContainer != container) {
			if (previousContainer == null)
				SceneManager.getInstance().setGridsVisible(false);
			else if (container != null)
				previousContainer.gridsMesh.getSceneHints().setCullHint(CullHint.Always);

			if (container != null && !(this instanceof Roof)) {
//				container.drawGrids(getGridSize());
//				container.gridsMesh.getSceneHints().setCullHint(CullHint.Inherit);
				setGridsVisible(true);
			} else if (this instanceof Foundation) {
				SceneManager.getInstance().setGridsVisible(true);
			}
		}
		return picked;
	}

	protected Vector3 toRelative(final Vector3 org) {
		return toRelative(org, container);
	}

	protected Vector3 toRelative(final ReadOnlyVector3 org, final HousePart container) {
		if (container == null)
			return new Vector3(org);
		final Vector3 origin = container.getAbsPoint(0);
		final Vector3 p = org.subtract(origin, null);
		final Vector3 wallx = container.getAbsPoint(2).subtract(origin, null);
		final Vector3 wally = container.getAbsPoint(1).subtract(origin, null);
		final Vector3 pointOnWall = new Vector3(Math.abs(wallx.getX()) < MathUtils.ZERO_TOLERANCE ? p.getY() / wallx.getY() : p.getX() / wallx.getX(), (relativeToHorizontal) ? p.getY() / wally.getY() : org.getY(), (relativeToHorizontal) ? org.getZ() : p.getZ() / wally.getZ());
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
		final Vector3 pointOnSpace = origin.add(width.multiply(p.getX(), null), null).add(height.multiply((relativeToHorizontal) ? p.getY() : p.getZ(), null), null);
		if (relativeToHorizontal)
			pointOnSpace.setZ(pointOnSpace.getZ() + p.getZ());
//		if (!Vector3.isValid(pointOnSpace))
//			System.out.println("xxx");
		roundPoint(pointOnSpace);
		return pointOnSpace;
	}

	protected Vector3 roundPoint(final Vector3 pointOnSpace) {
		final double C = 1000.0;
		return pointOnSpace.set(Math.round(pointOnSpace.getX() * C) / C, Math.round(pointOnSpace.getY() * C) / C, Math.round(pointOnSpace.getZ() * C) / C);
	}

	protected void snapToGrid(final Vector3 p, final ReadOnlyVector3 current, final double gridSize) {
		snapToGrid(p, current, gridSize, true);
	}

	protected void snapToGrid(final Vector3 p, final ReadOnlyVector3 current, final double gridSize, final boolean snapToZ) {
		if (isSnapToGrids()) {
			if (p.distance(current) < gridSize)
				p.set(current);
			else if (container != null) {
				final ReadOnlyVector3 p0 = container.getAbsPoint(0);
				final ReadOnlyVector3 origin;
				if (relativeToHorizontal) {
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
				final ReadOnlyVector3 horizontalDir = new Vector3(originToP.getX(), snapToZ ? originToP.getY() : 0, 0);
				final double snapedHorizontalLength = Math.round(horizontalDir.length() / gridSize) * gridSize;
				final ReadOnlyVector3 u = horizontalDir.normalize(null).multiplyLocal(snapedHorizontalLength);

				final ReadOnlyVector3 verticalDir = new Vector3(0, snapToZ ? 0 : originToP.getY(), snapToZ ? originToP.getZ() : 0);
				final double snapedVerticalLength = Math.round(verticalDir.length() / gridSize) * gridSize;
				final ReadOnlyVector3 v = verticalDir.normalize(null).multiplyLocal(snapedVerticalLength);
				p.set(origin).addLocal(u).addLocal(v);
			} else
				p.set(Math.round(p.getX() / gridSize) * gridSize, Math.round(p.getY() / gridSize) * gridSize, !snapToZ ? p.getZ() : Math.round(p.getZ() / gridSize) * gridSize);
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

//			if (isFrozen()) {
//				final BlendState blendState = new BlendState();
//				blendState.setBlendEnabled(true);
//				root.setRenderState(blendState);
//			} else
//				root.clearRenderState(StateType.Blend);

//			root.getSceneHints().setRenderBucketType(isFrozen() ? RenderBucketType.Transparent : RenderBucketType.Inherit);
//			root.getSceneHints().setTransparencyType(isFrozen() ? TransparencyType.TwoPass : TransparencyType.Inherit);
//			root.getSceneHints().setLightCombineMode(isFrozen() ? LightCombineMode.Off : LightCombineMode.Inherit);

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
			gridsMesh.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
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
//			final Camera camera = Camera.getCurrentCamera();
//			final Camera camera = SceneManager.getInstance().getCameraNode().getCamera();
			final Camera camera = SceneManager.getInstance().getCamera();
			if (camera != null)	// for Lwjgl
				getEditPointShape(i).setScale(camera.getLocation().distance(p) / 10);
			getEditPointShape(i).setTranslation(p);
		}
		/* remove remaining edit shapes */
		for (int i = points.size(); i < pointsRoot.getNumberOfChildren(); i++)
			pointsRoot.detachChildAt(points.size());
	}

	public void computeOrientedBoundingBox() {
		flattenCenter.set(0, 0, 0);
		computeOrientedBoundingBox(mesh);
		flattenCenter.set(mesh.getWorldBound().getCenter());
	}

	protected void computeOrientedBoundingBox(final Mesh mesh) {
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
		if (isFrozen()) {
			mesh.clearRenderState(StateType.Texture);
			mesh.setDefaultColor(Scene.GRAY);
		} else if (textureMode == TextureMode.None || getTextureFileName() == null) {
			mesh.clearRenderState(StateType.Texture);
			mesh.setDefaultColor(defaultColor);
		} else {
			final TextureState ts = new TextureState();
			final Texture texture = getTexture(getTextureFileName(), defaultColor);
			ts.setTexture(texture);
			mesh.setRenderState(ts);
			if (textureMode == TextureMode.None)
				mesh.setDefaultColor(defaultColor);
			else
				mesh.setDefaultColor(Scene.WHITE);
		}
	}

	private Texture getTexture(final String filename, final ReadOnlyColorRGBA defaultColor) {
		final Texture texture = TextureManager.load(filename, Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true);
		if (Scene.getInstance().getTextureMode() == TextureMode.Simple) {
			final Color color = new Color(defaultColor.getRed(), defaultColor.getGreen(), defaultColor.getBlue());
			final Image image = texture.getImage();
			final ByteBuffer data = image.getData(0);
			for (int y = 0; y < image.getHeight(); y++)
				for (int x = 0; x < image.getWidth(); x++) {
					final byte alpha = data.get((y * image.getWidth() + x) * 4 + 3);
					if (alpha == 0) {
						data.put((y * image.getWidth() + x) * 4, (byte) color.getRed());
						data.put((y * image.getWidth() + x) * 4 + 1, (byte) color.getGreen());
						data.put((y * image.getWidth() + x) * 4 + 2, (byte) color.getBlue());
					}
				}
			texture.setImage(image);
		}
		return texture;
	}

	public HousePart getContainer() {
		return container;
	}

	public Mesh getMesh() {
		return mesh;
	}

	@Override
	public String toString() {
		String s = this.getClass().getSimpleName() + "(" + Integer.toHexString(hashCode()) + ")";
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

	public boolean isDrawable() {
		return points.size() >= 4 && getAbsPoint(2).distance(getAbsPoint(0)) > MathUtils.ZERO_TOLERANCE && getAbsPoint(1).distance(getAbsPoint(0)) > MathUtils.ZERO_TOLERANCE;
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
		for (final ReadOnlyVector3 p : points) {
			if (!Vector3.isValid(p))
				return false;
		}
		return true;
	}

	public double computeArea() {
		final Vector3 p0 = getAbsPoint(0);
		final Vector3 p1 = getAbsPoint(1);
		final Vector3 p2 = getAbsPoint(2);
		final double C = 100.0;
//		return Math.round(Math.round(Math.abs(p1.getX() - p2.getX()) * C) / C * Math.round(Math.abs(p1.getZ() - p2.getZ()) * C) / C * C) / C;
		return Math.round(Math.round(p2.subtract(p0, null).length() * C) / C * Math.round(p1.subtract(p0, null).length() * C) / C * C) / C;
	}

//	protected void adjustTransparency(final Mesh mesh) {
//		final ReadOnlyColorRGBA c = mesh.getDefaultColor();
//		mesh.setDefaultColor(new ColorRGBA(c.getRed(), c.getGreen(), c.getBlue(), isFrozen() ? Scene.TRANSPARENCY_LEVEL : 1));
//	}

	public void setFreeze(final boolean freeze) {
		this.freeze = freeze;
	}

	public boolean isFrozen() {
		return freeze;
	}

}
