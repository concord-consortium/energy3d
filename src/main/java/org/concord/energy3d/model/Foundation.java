package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.util.FontManager;
import org.concord.energy3d.util.SelectUtil;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.ui.text.BMText.Justify;
import com.ardor3d.util.geom.BufferUtils;

public class Foundation extends HousePart {
	private static final long serialVersionUID = 1L;
	private transient Mesh boundingMesh;
	private transient Mesh wireframeMesh;
	private transient ArrayList<Vector3> orgPoints;
	private transient double newBoundingHeight;
	private transient double boundingHeight;
	private transient boolean resizeHouseMode = false;
	private transient double minX;
	private transient double minY;
	private transient double maxX;
	private transient double maxY;
	private transient BMText solarLabel;

	public Foundation() {
		super(2, 8, 1);
	}

	public Foundation(final double xLength, final double yLength) {
		super(2, 8, 1, true);
		points.get(0).set(-xLength / 2.0, -yLength / 2.0, 0);
		points.get(2).set(xLength / 2.0, -yLength / 2.0, 0);
		points.get(1).set(-xLength / 2.0, yLength / 2.0, 0);
		points.get(3).set(xLength / 2.0, yLength / 2.0, 0);
	}

	@Override
	protected boolean mustHaveContainer() {
		return false;
	}

	@Override
	protected void init() {
		super.init();
		resizeHouseMode = false;

		mesh = new Box("Foundation", new Vector3(), new Vector3());
		mesh.setRenderState(offsetState);
		mesh.setModelBound(new BoundingBox());
		updateTextureAndColor();
		root.attachChild(mesh);

		boundingMesh = new Line("Foundation (Bounding)");
		boundingMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(24));
		boundingMesh.setModelBound(new BoundingBox());
		Util.disablePickShadowLight(boundingMesh);
		boundingMesh.getSceneHints().setCullHint(CullHint.Always);
		root.attachChild(boundingMesh);

		wireframeMesh = new Line("Foundation (wireframe)");
		wireframeMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(24));
		wireframeMesh.setDefaultColor(ColorRGBA.BLACK);
		wireframeMesh.setModelBound(new BoundingBox());
		Util.disablePickShadowLight(wireframeMesh);
		root.attachChild(wireframeMesh);

		final UserData userData = new UserData(this);
		mesh.setUserData(userData);
		boundingMesh.setUserData(userData);

		setLabelOffset(-0.11);

		solarLabel = new BMText("Solar value Text", "0", FontManager.getInstance().getPartNumberFont(), Align.Center, Justify.Center);
		Util.initHousePartLabel(solarLabel);
		solarLabel.setFontScale(0.75);
		solarLabel.setVisible(false);
		root.attachChild(solarLabel);
	}

	public void setResizeHouseMode(final boolean resizeHouseMode) {
		this.resizeHouseMode = resizeHouseMode;
		if (!isFrozen()) {
			if (resizeHouseMode)
				scanChildrenHeight();
			setEditPointsVisible(resizeHouseMode);
			boundingMesh.getSceneHints().setCullHint(resizeHouseMode ? CullHint.Inherit : CullHint.Always);
		}
	}

	public boolean isResizeHouseMode() {
		return resizeHouseMode;
	}

	@Override
	public void setEditPointsVisible(final boolean visible) {
		if (!visible && resizeHouseMode)
			return;
		else {
			for (int i = 0; i < points.size(); i++) {
				if (!visible)
					pointsRoot.getChild(i).getSceneHints().setCullHint(CullHint.Always);
				else {
					if (!resizeHouseMode && i >= 4)
						pointsRoot.getChild(i).getSceneHints().setCullHint(CullHint.Always);
					else
						pointsRoot.getChild(i).getSceneHints().setCullHint(CullHint.Inherit);
				}
			}
		}
	}

	@Override
	public void complete() {
		super.complete();
		newBoundingHeight = points.get(4).getZ() - height; // problem?
		applyNewHeight(boundingHeight, newBoundingHeight, true);
		if (!resizeHouseMode && orgPoints != null) {
			final double dx = Math.abs(points.get(2).getX() - points.get(0).getX());
			final double dxOrg = Math.abs(orgPoints.get(2).getX() - orgPoints.get(0).getX());
			final double ratioX = dx / dxOrg;
			final double dy = Math.abs(points.get(1).getY() - points.get(0).getY());
			final double dyOrg = Math.abs(orgPoints.get(1).getY() - orgPoints.get(0).getY());
			final double ratioY = dy / dyOrg;
			final ArrayList<HousePart> roofs = new ArrayList<HousePart>();
			for (final HousePart child : children) {
				reverseFoundationResizeEffect(child, dx, dxOrg, ratioX, dy, dyOrg, ratioY);
				if (child instanceof Wall) {
					final HousePart roof = ((Wall) child).getRoof();
					if (roof != null && !roofs.contains(roof)) {
						reverseFoundationResizeEffect(roof, dx, dxOrg, ratioX, dy, dyOrg, ratioY);
						roofs.add(roof);
					}
				}
			}
			orgPoints = null;
		}
	}

	private void reverseFoundationResizeEffect(final HousePart child, final double dx, final double dxOrg, final double ratioX, final double dy, final double dyOrg, final double ratioY) {
		for (final Vector3 childPoint : child.getPoints()) {
			double x = childPoint.getX() / ratioX;
			if (editPointIndex == 0 || editPointIndex == 1)
				x += (dx - dxOrg) / dx;
			childPoint.setX(x);
			double y = childPoint.getY() / ratioY;
			if (editPointIndex == 0 || editPointIndex == 2)
				y += (dy - dyOrg) / dy;
			childPoint.setY(y);
		}
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		int index = editPointIndex;
		if (index == -1) {
			if (isFirstPointInserted())
				index = 3;
			else
				index = 0;
		}
		final PickedHousePart pick = SelectUtil.pickPart(x, y, (Spatial) null);
		Vector3 p = points.get(index);
		if (pick != null && index < 4) {
			p = pick.getPoint();
			snapToGrid(p, getAbsPoint(index), getGridSize());
			if (resizeHouseMode)
				p = ensureNotTooSmall(p, index);
			else
				p = ensureIncludesChildren(p, index);
			points.get(index).set(p);
		}
		if (!isFirstPointInserted()) {
			points.get(1).set(p);
			points.get(2).set(p);
			points.get(3).set(p);
		} else {
			if (index == 0 || index == 3) {
				points.get(1).set(points.get(0).getX(), points.get(3).getY(), 0);
				points.get(2).set(points.get(3).getX(), points.get(0).getY(), 0);
			} else if (index == 1 || index == 2) {
				points.get(0).set(points.get(1).getX(), points.get(2).getY(), 0);
				points.get(3).set(points.get(2).getX(), points.get(1).getY(), 0);
			} else {
				final int lower = editPointIndex - 4;
				final Vector3 base = getAbsPoint(lower);
				final Vector3 closestPoint = Util.closestPoint(base, Vector3.UNIT_Z, x, y);
				final Vector3 currentPoint = getAbsPoint(index);
				snapToGrid(closestPoint, currentPoint, getGridSize());
				if (!closestPoint.equals(currentPoint)) {
					newBoundingHeight = Math.max(0, closestPoint.getZ() - height);
					applyNewHeight(boundingHeight, newBoundingHeight, false);
				}
			}
			syncUpperPoints();
		}

		if (resizeHouseMode)
			Scene.getInstance().redrawAll();
		else
			draw();
		setEditPointsVisible(true);
	}

	private Vector3 ensureNotTooSmall(final Vector3 p, final int index) {
		final double width = 1;
		final double x2 = getAbsPoint(index == 0 || index == 1 ? 2 : 0).getX();
		if (getAbsPoint(index).getX() > x2) {
			if (p.getX() - x2 < width)
				p.setX(x2 + width);
		} else {
			if (x2 - p.getX() < width)
				p.setX(x2 - width);
		}

		final double y2 = getAbsPoint(index == 0 || index == 2 ? 1 : 0).getY();
		if (getAbsPoint(index).getY() > y2) {
			if (p.getY() - y2 < width)
				p.setY(y2 + width);
		} else {
			if (y2 - p.getY() < width)
				p.setY(y2 - width);
		}

		return p;
	}

	private void syncUpperPoints() {
		for (int i = 0; i < 4; i++)
			points.get(i + 4).set(points.get(i)).setZ(Math.max(height, newBoundingHeight + height));
	}

	private Vector3 ensureIncludesChildren(final Vector3 p, final int index) {
		if (children.isEmpty())
			return p;

		if (((index == 0 || index == 1) && points.get(0).getX() < points.get(2).getX()) || ((index == 2 || index == 3) && points.get(2).getX() < points.get(0).getX())) {
			if (p.getX() > minX)
				p.setX(minX);
		} else {
			if (p.getX() < maxX)
				p.setX(maxX);
		}

		if (((index == 0 || index == 2) && points.get(0).getY() < points.get(1).getY()) || ((index == 1 || index == 3) && points.get(1).getY() < points.get(0).getY())) {
			if (p.getY() > minY)
				p.setY(minY);
		} else {
			if (p.getY() < maxY)
				p.setY(maxY);
		}

		return p;
	}

	private void applyNewHeight(final double orgHeight, final double newHeight, final boolean finalize) {
		if (newHeight == 0 || newHeight == orgHeight)
			return;
		final double scale = newHeight / orgHeight;

		applyNewHeight(children, scale, finalize);
		if (finalize)
			boundingHeight = newHeight;
	}

	private void applyNewHeight(final ArrayList<HousePart> children, final double scale, final boolean finalize) {
		for (final HousePart child : children) {
			if (child instanceof Wall || child instanceof Floor || child instanceof Roof) {
				child.setHeight(child.orgHeight * scale, finalize);
				applyNewHeight(child.getChildren(), scale, finalize);
			}
		}
	}

	public void scaleHouse(final double scale) {
		final double h = points.get(4).getZ() - height;
		applyNewHeight(h, h * 10, true);

		final double oldHeight = height;
		height *= scale;
		final double addHeight = height - oldHeight;
		for (final HousePart wall : children) {
			for (final Vector3 point : wall.points)
				point.addLocal(0, 0, addHeight);
			for (final HousePart floor : wall.children)
				if (floor instanceof Floor)
					floor.setHeight(floor.getHeight() + addHeight);
		}

		for (int i = 0; i < points.size(); i++)
			points.get(i).multiplyLocal(10);
	}

	@Override
	protected void drawMesh() {
		if (boundingHeight == 0)
			scanChildrenHeight();
		final boolean drawable = points.size() == 8;
		if (drawable) {
			((Box) mesh).setData(points.get(0), points.get(3).add(0, 0, height, null));
			mesh.updateModelBound();
			CollisionTreeManager.INSTANCE.updateCollisionTree(mesh);
			drawWireframe(boundingMesh, points.get(7).getZf());
			drawWireframe(wireframeMesh, (float) height);
			updateSolarLabelPosition();
		}
	}

	private void drawWireframe(final Mesh mesh, final float height) {
		final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
		buf.rewind();
		final Vector3 p0 = getAbsPoint(0);
		final Vector3 p1 = getAbsPoint(1);
		final Vector3 p2 = getAbsPoint(2);
		final Vector3 p3 = getAbsPoint(3);

		putWireframePoint(buf, p0);
		putWireframePoint(buf, p2);
		putWireframePoint(buf, p2);
		putWireframePoint(buf, p3);
		putWireframePoint(buf, p3);
		putWireframePoint(buf, p1);
		putWireframePoint(buf, p1);
		putWireframePoint(buf, p0);

		putWireframePoint(buf, p0, height);
		putWireframePoint(buf, p2, height);
		putWireframePoint(buf, p2, height);
		putWireframePoint(buf, p3, height);
		putWireframePoint(buf, p3, height);
		putWireframePoint(buf, p1, height);
		putWireframePoint(buf, p1, height);
		putWireframePoint(buf, p0, height);

		putWireframePoint(buf, p0);
		putWireframePoint(buf, p0, height);
		putWireframePoint(buf, p2);
		putWireframePoint(buf, p2, height);
		putWireframePoint(buf, p3);
		putWireframePoint(buf, p3, height);
		putWireframePoint(buf, p1);
		putWireframePoint(buf, p1, height);

		mesh.updateModelBound();
	}

	@Override
	public void drawGrids(final double gridSize) {
		final ReadOnlyVector3 p0 = getAbsPoint(0);
		final ReadOnlyVector3 p1 = getAbsPoint(1);
		final ReadOnlyVector3 p2 = getAbsPoint(2);
		final ReadOnlyVector3 width = p2.subtract(p0, null);
		final ReadOnlyVector3 height = p1.subtract(p0, null);
		final ArrayList<ReadOnlyVector3> points = new ArrayList<ReadOnlyVector3>();
		final ReadOnlyVector3 pMiddle = width.add(height, null).multiplyLocal(0.5).addLocal(p0);

		final int cols = (int) (width.length() / gridSize);

		for (int col = 0; col < cols / 2 + 1; col++) {
			for (int neg = -1; neg <= 1; neg += 2) {
				final ReadOnlyVector3 lineP1 = width.normalize(null).multiplyLocal(neg * col * gridSize).addLocal(pMiddle).subtractLocal(height.multiply(0.5, null));
				points.add(lineP1);
				final ReadOnlyVector3 lineP2 = lineP1.add(height, null);
				points.add(lineP2);
				if (col == 0)
					break;
			}
		}

		final int rows = (int) (height.length() / gridSize);

		for (int row = 0; row < rows / 2 + 1; row++) {
			for (int neg = -1; neg <= 1; neg += 2) {
				final ReadOnlyVector3 lineP1 = height.normalize(null).multiplyLocal(neg * row * gridSize).addLocal(pMiddle).subtractLocal(width.multiply(0.5, null));
				points.add(lineP1);
				final ReadOnlyVector3 lineP2 = lineP1.add(width, null);
				points.add(lineP2);
				if (row == 0)
					break;
			}
		}
		if (points.size() < 2)
			return;
		final FloatBuffer buf = BufferUtils.createVector3Buffer(points.size());
		for (final ReadOnlyVector3 p : points)
			buf.put(p.getXf()).put(p.getYf()).put((float) this.height + 0.1f);

		gridsMesh.getMeshData().setVertexBuffer(buf);
	}

	private void putWireframePoint(final FloatBuffer buf, final Vector3 p) {
		putWireframePoint(buf, p, 0);
	}

	private void putWireframePoint(final FloatBuffer buf, final Vector3 p, final float height) {
		buf.put(p.getXf()).put(p.getYf()).put(p.getZf() + height);
	}

	public void scanChildrenHeight() {
		if (!isFirstPointInserted())
			return;
		boundingHeight = scanChildrenHeight(this) - height;
		for (int i = 4; i < 8; i++) {
			points.get(i).setZ(boundingHeight + height);
		}
		newBoundingHeight = boundingHeight;
		syncUpperPoints();
		updateEditShapes();
		updateSolarLabelPosition();
	}

	public void updateSolarLabelPosition() {
		final ReadOnlyVector3 center = getCenter();
		solarLabel.setTranslation(center.getX(), center.getY(), boundingHeight + height + 6.0);
	}

	private double scanChildrenHeight(final HousePart part) {
		double maxHeight = 0;
		if (part instanceof Wall || part instanceof Roof) {
			for (int i = 0; i < part.points.size(); i++) {
				final ReadOnlyVector3 p = part.getAbsPoint(i);
				maxHeight = Math.max(maxHeight, p.getZ());
			}
		}
		for (final HousePart child : part.children)
			maxHeight = Math.max(maxHeight, scanChildrenHeight(child));
		return maxHeight;
	}

	@Override
	public void flattenInit() {
		super.flattenInit();
		flattenCenter.setY(0);
	}

	@Override
	public void flatten(final double flattenTime) {
		root.setRotation((new Matrix3().fromAngles(flattenTime * Math.PI / 2, 0, 0)));
		super.flatten(flattenTime);
	}

	@Override
	public void drawAnnotations() {
		final int[] order = { 0, 1, 3, 2, 0 };
		int annotCounter = 0;
		for (int i = 0; i < order.length - 1; i++, annotCounter++) {
			final SizeAnnotation annot = fetchSizeAnnot(annotCounter++);
			annot.setRange(getAbsPoint(order[i]), getAbsPoint(order[i + 1]), getCenter(), getFaceDirection(), false, Align.Center, true, true, false);
			annot.setLineWidth(original == null ? 1f : 2f);
		}
	}

	@Override
	public void setEditPoint(int editPoint) {
		if (!resizeHouseMode && editPoint > 3)
			editPoint -= 4;
		super.setEditPoint(editPoint);
		if (!resizeHouseMode) {
			prepareForNotResizing();
			minX = Double.MAX_VALUE;
			minY = Double.MAX_VALUE;
			maxX = -Double.MAX_VALUE;
			maxY = -Double.MAX_VALUE;
			for (final HousePart part : children) {
				final Vector3 p1 = part.getAbsPoint(0);
				final Vector3 p2 = part.getAbsPoint(2);
				minX = Math.min(p1.getX(), minX);
				minX = Math.min(p2.getX(), minX);
				minY = Math.min(p1.getY(), minY);
				minY = Math.min(p2.getY(), minY);
				maxX = Math.max(p1.getX(), maxX);
				maxX = Math.max(p2.getX(), maxX);
				maxY = Math.max(p1.getY(), maxY);
				maxY = Math.max(p2.getY(), maxY);
			}
		}
	}

	public void prepareForNotResizing() {
		orgPoints = new ArrayList<Vector3>(4);
		for (int i = 0; i < 4; i++)
			orgPoints.add(points.get(i).clone());
	}

	@Override
	protected String getTextureFileName() {
		return Scene.getInstance().getTextureMode() == TextureMode.Full ? "foundation.jpg" : null;
	}

	@Override
	protected ReadOnlyVector3 getCenter() {
		return super.getCenter().multiply(new Vector3(1, 1, 0), null);
	}

	@Override
	public boolean isPrintable() {
		return false;
	}

	@Override
	public double getGridSize() {
		return 1.0;
	}

	@Override
	public void updateTextureAndColor() {
		updateTextureAndColor(mesh, Scene.getInstance().getFoundationColor());
	}

	public void setSolarValue(final long solarValue) {
		if (solarValue == 0)
			solarLabel.setVisible(false);
		else {
			solarLabel.setVisible(true);
			solarLabel.setText("(#" + id + ")\n" + String.valueOf(solarValue) + "kWh");
		}
	}
}
