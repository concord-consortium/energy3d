package org.concord.energy3d.model;

import java.awt.geom.Path2D;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.util.FontManager;
import org.concord.energy3d.util.SelectUtil;
import org.concord.energy3d.util.Util;
import org.concord.energy3d.util.WallVisitor;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.ui.text.BMText.Justify;
import com.ardor3d.util.geom.BufferUtils;

public class Foundation extends HousePart {

	private static final long serialVersionUID = 1L;
	private static DecimalFormat format = new DecimalFormat();
	private transient ArrayList<Vector3> orgPoints;
	private transient ArrayList<Vector2> floorVertices;
	private transient Mesh boundingMesh;
	private transient Mesh outlineMesh;
	private transient Mesh surroundMesh;
	private transient BMText buildingLabel;
	private transient double newBoundingHeight;
	private transient double boundingHeight;
	private transient double minX;
	private transient double minY;
	private transient double maxX;
	private transient double maxY;
	private transient double passiveSolarNow; // energy terms of current hour
	private transient double photovoltaicNow;
	private transient double heatingNow;
	private transient double coolingNow;
	private transient double totalEnergyNow;
	private transient double passiveSolarToday; // energy terms of current day
	private transient double photovoltaicToday;
	private transient double heatingToday;
	private transient double coolingToday;
	private transient double totalEnergyToday;
	private transient boolean resizeHouseMode = false;
	private transient boolean useOrgPoints = false;
	private boolean lockEdit = false;

	static {
		format.setGroupingUsed(true);
		format.setMaximumFractionDigits(0);
	}

	public Foundation() {
		super(2, 8, 1);
		root.getSceneHints().setCullHint(CullHint.Always);
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

		mesh = new Mesh("Foundation");
		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(6));
		mesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(6));
		mesh.getMeshData().setTextureBuffer(BufferUtils.createVector2Buffer(6), 0);
		mesh.setRenderState(offsetState);
		mesh.setModelBound(new BoundingBox());
		root.attachChild(mesh);

		surroundMesh = new Mesh("Foundation (Surround)");
		surroundMesh.getMeshData().setIndexMode(IndexMode.Quads);
		surroundMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(16));
		surroundMesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(16));
		surroundMesh.setRenderState(offsetState);
		surroundMesh.setModelBound(new BoundingBox());
		root.attachChild(surroundMesh);

		boundingMesh = new Line("Foundation (Bounding)");
		boundingMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(24));
		boundingMesh.setModelBound(new BoundingBox());
		Util.disablePickShadowLight(boundingMesh);
		boundingMesh.getSceneHints().setCullHint(CullHint.Always);
		root.attachChild(boundingMesh);

		outlineMesh = new Line("Foundation (Outline)");
		outlineMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(24));
		outlineMesh.setDefaultColor(ColorRGBA.BLACK);
		outlineMesh.setModelBound(new BoundingBox());
		Util.disablePickShadowLight(outlineMesh);
		root.attachChild(outlineMesh);

		final UserData userData = new UserData(this);
		mesh.setUserData(userData);
		boundingMesh.setUserData(userData);

		setLabelOffset(-0.11);

		buildingLabel = new BMText("Solar value Text", "0", FontManager.getInstance().getPartNumberFont(), Align.Center, Justify.Center);
		Util.initHousePartLabel(buildingLabel);
		buildingLabel.setFontScale(0.75);
		buildingLabel.setVisible(false);
		root.attachChild(buildingLabel);

		updateTextureAndColor();
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
		if (lockEdit && editPointIndex < 4)
			return;
		final int index;
		if (editPointIndex == -1)
			index = isFirstPointInserted() ? 3 : 0;
		else if (SceneManager.getInstance().isTopView() && editPointIndex > 3)
			index = editPointIndex - 4;
		else
			index = editPointIndex;

		final PickedHousePart pick = SelectUtil.pickPart(x, y, (HousePart) null);
		Vector3 p;
		if (pick != null && index < 4) {
			p = pick.getPoint();
			snapToGrid(p, getAbsPoint(index), getGridSize());
			root.getSceneHints().setCullHint(CullHint.Never);
		} else
			p = points.get(index).clone();

		if (!isFirstPointInserted()) {
			points.get(index).set(p);
			points.get(1).set(p.add(0, 0.1, 0, null));
			points.get(2).set(p.add(0.1, 0, 0, null));
			points.get(3).set(p.add(0.1, 0.1, 0, null));
		} else {
			if (index < 4) {
				p = ensureDistanceFromOtherFoundations(p, index);
				if (!resizeHouseMode)
					ensureIncludesChildren(p, index);

				final int oppositeIndex;
				if (index == 0)
					oppositeIndex = 3;
				else if (index == 1)
					oppositeIndex = 2;
				else if (index == 2)
					oppositeIndex = 1;
				else
					oppositeIndex = 0;

				if (!Util.isEqual(p.getX(), points.get(oppositeIndex).getX()) && !Util.isEqual(p.getY(), points.get(oppositeIndex).getY())) {
					points.get(index).set(p);
					if (index == 0) {
						points.get(1).set(Util.projectPointOnLine(p, points.get(3), points.get(1), false));
						points.get(2).set(Util.projectPointOnLine(p, points.get(3), points.get(2), false));
					} else if (index == 3) {
						points.get(1).set(Util.projectPointOnLine(p, points.get(0), points.get(1), false));
						points.get(2).set(Util.projectPointOnLine(p, points.get(0), points.get(2), false));
					} else if (index == 1) {
						points.get(0).set(Util.projectPointOnLine(p, points.get(2), points.get(0), false));
						points.get(3).set(Util.projectPointOnLine(p, points.get(2), points.get(3), false));
					} else if (index == 2) {
						points.get(0).set(Util.projectPointOnLine(p, points.get(1), points.get(0), false));
						points.get(3).set(Util.projectPointOnLine(p, points.get(1), points.get(3), false));
					}
				}
			} else {
				final int lower = editPointIndex - 4;
				final Vector3 base = getAbsPoint(lower);
				final Vector3 closestPoint = Util.closestPoint(base, Vector3.UNIT_Z, x, y);
				final Vector3 currentPoint = getAbsPoint(index);
				snapToGrid(closestPoint, currentPoint, getGridSize());
				if (closestPoint.getZ() < height + getGridSize())
					closestPoint.setZ(height + getGridSize());
				if (!closestPoint.equals(currentPoint)) {
					newBoundingHeight = Math.max(0, closestPoint.getZ() - height);
					applyNewHeight(boundingHeight, newBoundingHeight, false);
				}
			}
			syncUpperPoints();
		}

		if (resizeHouseMode)
			drawChildren();
		draw();
		setEditPointsVisible(true);
	}

	@Override
	protected void drawChildren() {
		final List<HousePart> children = new ArrayList<HousePart>();
		collectChildren(this, children);
		for (final HousePart part : children)
			if (part instanceof Roof)
				part.draw();
		for (final HousePart part : children)
			part.draw();
	}

	private void collectChildren(final HousePart part, final List<HousePart> children) {
		if (!children.contains(part))
			children.add(part);
		for (final HousePart child : part.getChildren())
			collectChildren(child, children);
	}

	// private Vector3 ensureNotTooSmall(final Vector3 p, final int index) {
	// final double MIN_LENGHT = getGridSize();
	// final double x2 = getAbsPoint(index == 0 || index == 1 ? 2 : 0).getX();
	// if (getAbsPoint(index).getX() > x2) {
	// if (p.getX() - x2 < MIN_LENGHT)
	// p.setX(x2 + MIN_LENGHT);
	// } else {
	// if (x2 - p.getX() < MIN_LENGHT)
	// p.setX(x2 - MIN_LENGHT);
	// }
	//
	// final double y2 = getAbsPoint(index == 0 || index == 2 ? 1 : 0).getY();
	// if (getAbsPoint(index).getY() > y2) {
	// if (p.getY() - y2 < MIN_LENGHT)
	// p.setY(y2 + MIN_LENGHT);
	// } else {
	// if (y2 - p.getY() < MIN_LENGHT)
	// p.setY(y2 - MIN_LENGHT);
	// }
	//
	// return p;
	// }

	private void syncUpperPoints() {
		for (int i = 0; i < 4; i++)
			points.get(i + 4).set(points.get(i)).setZ(Math.max(height, newBoundingHeight + height));
	}

	private void ensureIncludesChildren(final Vector3 p, final int index) {
		if (children.isEmpty())
			return;

		useOrgPoints = true;
		final List<Vector2> insidePoints = new ArrayList<Vector2>(children.size() * 2);
		for (final HousePart part : children) {
			final Vector3 p0 = part.getAbsPoint(0);
			final Vector3 p2 = part.getAbsPoint(2);
			insidePoints.add(new Vector2(p0.getX(), p0.getY()));
			insidePoints.add(new Vector2(p2.getX(), p2.getY()));
		}
		useOrgPoints = false;

		double uScaleMin = Double.MAX_VALUE;
		double uScaleMax = -Double.MAX_VALUE;
		double vScaleMin = Double.MAX_VALUE;
		double vScaleMax = -Double.MAX_VALUE;
		for (final Vector2 insidePoint : insidePoints) {
			final double uScale = Util.projectPointOnLineScale(insidePoint, new Vector2(points.get(0).getX(), points.get(0).getY()), new Vector2(points.get(2).getX(), points.get(2).getY()));
			if (uScaleMin > uScale)
				uScaleMin = uScale;
			if (uScaleMax < uScale)
				uScaleMax = uScale;
			final double vScale = Util.projectPointOnLineScale(insidePoint, new Vector2(points.get(0).getX(), points.get(0).getY()), new Vector2(points.get(1).getX(), points.get(1).getY()));
			if (vScaleMin > vScale)
				vScaleMin = vScale;
			if (vScaleMax < vScale)
				vScaleMax = vScale;
		}

		final double uScaleP = Util.projectPointOnLineScale(new Vector2(p.getX(), p.getY()), new Vector2(points.get(0).getX(), points.get(0).getY()), new Vector2(points.get(2).getX(), points.get(2).getY()));
		final double vScaleP = Util.projectPointOnLineScale(new Vector2(p.getX(), p.getY()), new Vector2(points.get(0).getX(), points.get(0).getY()), new Vector2(points.get(1).getX(), points.get(1).getY()));
		final double uScaleP0 = Util.projectPointOnLineScale(new Vector2(points.get(0).getX(), points.get(0).getY()), new Vector2(points.get(0).getX(), points.get(0).getY()), new Vector2(points.get(2).getX(), points.get(2).getY()));
		final double uScaleP2 = Util.projectPointOnLineScale(new Vector2(points.get(2).getX(), points.get(2).getY()), new Vector2(points.get(0).getX(), points.get(0).getY()), new Vector2(points.get(2).getX(), points.get(2).getY()));
		final double vScaleP0 = Util.projectPointOnLineScale(new Vector2(points.get(0).getX(), points.get(0).getY()), new Vector2(points.get(0).getX(), points.get(0).getY()), new Vector2(points.get(1).getX(), points.get(1).getY()));
		final double vScaleP1 = Util.projectPointOnLineScale(new Vector2(points.get(1).getX(), points.get(1).getY()), new Vector2(points.get(0).getX(), points.get(0).getY()), new Vector2(points.get(1).getX(), points.get(1).getY()));
		final boolean isOnRight = uScaleP2 >= uScaleP0 && (index == 2 || index == 3);
		final boolean isOnTop = vScaleP1 >= vScaleP0 && (index == 1 || index == 3);

		final double uScale;
		if (isOnRight && uScaleP < uScaleMax)
			uScale = uScaleMax;
		else if (!isOnRight && uScaleP > uScaleMin)
			uScale = uScaleMin;
		else
			uScale = uScaleP;

		final double vScale;
		if (isOnTop && vScaleP < vScaleMax)
			vScale = vScaleMax;
		else if (!isOnTop && vScaleP > vScaleMin)
			vScale = vScaleMin;
		else
			vScale = vScaleP;

		final Vector3 u = points.get(2).subtract(points.get(0), null);
		final Vector3 v = points.get(1).subtract(points.get(0), null);
		p.set(points.get(0)).addLocal(u.multiplyLocal(uScale)).addLocal(v.multiplyLocal(vScale));
	}

	private Vector3 ensureDistanceFromOtherFoundations(final Vector3 p, final int index) {
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Foundation && part != this) {
				final Vector3 p0 = part.getAbsPoint(0);
				final Vector3 p1 = part.getAbsPoint(1);
				final Vector3 p2 = part.getAbsPoint(2);
				final double minDistance = 0;
				final double minX = Math.min(p0.getX(), p2.getX()) - minDistance;
				final double maxX = Math.max(p0.getX(), p2.getX()) + minDistance;
				final double minY = Math.min(p0.getY(), p1.getY()) - minDistance;
				final double maxY = Math.max(p0.getY(), p1.getY()) + minDistance;
				if (isFirstPointInserted()) {
					final double oppositeX = getAbsPoint(index == 0 || index == 1 ? 2 : 0).getX();
					final double oppositeY = getAbsPoint(index == 0 || index == 2 ? 1 : 0).getY();
					if (!(oppositeX <= minX && p.getX() <= minX || oppositeX >= maxX && p.getX() >= maxX || oppositeY <= minY && p.getY() <= minY || oppositeY >= maxY && p.getY() >= maxY)) {
						return getAbsPoint(index);
					}
				} else {
					if (p.getX() > minX && p.getX() < maxX && p.getY() > minY && p.getY() < maxY) {
						double shortestDistance = Double.MAX_VALUE;
						double distance;
						final Vector3 newP = new Vector3();
						distance = p.getX() - minX;
						if (distance < shortestDistance) {
							shortestDistance = distance;
							newP.set(minX, p.getY(), p.getZ());
						}
						distance = maxX - p.getX();
						if (distance < shortestDistance) {
							shortestDistance = distance;
							newP.set(maxX, p.getY(), p.getZ());
						}
						distance = p.getY() - minY;
						if (distance < shortestDistance) {
							shortestDistance = distance;
							newP.set(p.getX(), minY, p.getZ());
						}
						distance = maxY - p.getY();
						if (distance < shortestDistance) {
							shortestDistance = distance;
							newP.set(p.getX(), maxY, p.getZ());
						}
						return newP;
					}
				}
			}
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
			drawTopMesh();
			drawSurroundMesh();
			drawOutline(boundingMesh, points.get(7).getZf());
			drawOutline(outlineMesh, (float) height);
			updateSolarLabelPosition();
		}
	}

	public void drawSurroundMesh() {
		final FloatBuffer vertexBuffer = surroundMesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		ReadOnlyVector3 p;
		final Vector3 p0 = getAbsPoint(0);
		final Vector3 p1 = getAbsPoint(1);
		final Vector3 p2 = getAbsPoint(2);
		final Vector3 p3 = getAbsPoint(3);
		p = p0;
		vertexBuffer.put(p.getXf()).put(p.getYf()).put((float) height);
		vertexBuffer.put(p.getXf()).put(p.getYf()).put(0f);
		p = p2;
		vertexBuffer.put(p.getXf()).put(p.getYf()).put(0f);
		vertexBuffer.put(p.getXf()).put(p.getYf()).put((float) height);
		vertexBuffer.put(p.getXf()).put(p.getYf()).put((float) height);
		vertexBuffer.put(p.getXf()).put(p.getYf()).put(0f);
		p = p3;
		vertexBuffer.put(p.getXf()).put(p.getYf()).put(0f);
		vertexBuffer.put(p.getXf()).put(p.getYf()).put((float) height);
		vertexBuffer.put(p.getXf()).put(p.getYf()).put((float) height);
		vertexBuffer.put(p.getXf()).put(p.getYf()).put(0f);
		p = p1;
		vertexBuffer.put(p.getXf()).put(p.getYf()).put(0f);
		vertexBuffer.put(p.getXf()).put(p.getYf()).put((float) height);
		vertexBuffer.put(p.getXf()).put(p.getYf()).put((float) height);
		vertexBuffer.put(p.getXf()).put(p.getYf()).put(0f);
		p = p0;
		vertexBuffer.put(p.getXf()).put(p.getYf()).put(0f);
		vertexBuffer.put(p.getXf()).put(p.getYf()).put((float) height);

		final FloatBuffer normalBuffer = surroundMesh.getMeshData().getNormalBuffer();
		normalBuffer.rewind();
		final ReadOnlyVector3 n1 = p2.subtract(p0, null).normalizeLocal().crossLocal(Vector3.UNIT_Z);
		final ReadOnlyVector3 n2 = p3.subtract(p2, null).normalizeLocal().crossLocal(Vector3.UNIT_Z);
		ReadOnlyVector3 normal;
		normal = n1;
		for (int i = 0; i < 4; i++)
			normalBuffer.put(normal.getXf()).put(normal.getYf()).put(normal.getZf());
		normal = n2;
		for (int i = 0; i < 4; i++)
			normalBuffer.put(normal.getXf()).put(normal.getYf()).put(normal.getZf());
		normal = n1.negate(null);
		for (int i = 0; i < 4; i++)
			normalBuffer.put(normal.getXf()).put(normal.getYf()).put(normal.getZf());
		normal = n2.negate(null);
		for (int i = 0; i < 4; i++)
			normalBuffer.put(normal.getXf()).put(normal.getYf()).put(normal.getZf());

		surroundMesh.updateModelBound();
		CollisionTreeManager.INSTANCE.updateCollisionTree(surroundMesh);
	}

	public void drawTopMesh() {
		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		ReadOnlyVector3 p;
		final Vector3 p0 = getAbsPoint(0);
		final Vector3 p1 = getAbsPoint(1);
		final Vector3 p2 = getAbsPoint(2);
		final Vector3 p3 = getAbsPoint(3);
		p = p0;
		vertexBuffer.put(p.getXf()).put(p.getYf()).put((float) height);
		p = p2;
		vertexBuffer.put(p.getXf()).put(p.getYf()).put((float) height);
		p = p1;
		vertexBuffer.put(p.getXf()).put(p.getYf()).put((float) height);
		vertexBuffer.put(p.getXf()).put(p.getYf()).put((float) height);
		p = p2;
		vertexBuffer.put(p.getXf()).put(p.getYf()).put((float) height);
		p = p3;
		vertexBuffer.put(p.getXf()).put(p.getYf()).put((float) height);

		final ReadOnlyVector3 normal = Vector3.UNIT_Z;
		final FloatBuffer normalBuffer = mesh.getMeshData().getNormalBuffer();
		normalBuffer.rewind();
		for (int i = 0; i < 6; i++)
			normalBuffer.put(normal.getXf()).put(normal.getYf()).put(normal.getZf());

		final FloatBuffer textureBuffer = mesh.getMeshData().getTextureBuffer(0);
		textureBuffer.rewind();
		textureBuffer.put(0).put(0);
		textureBuffer.put(1).put(0);
		textureBuffer.put(0).put(1);
		textureBuffer.put(0).put(1);
		textureBuffer.put(1).put(0);
		textureBuffer.put(1).put(1);

		mesh.updateModelBound();
		CollisionTreeManager.INSTANCE.updateCollisionTree(mesh);
	}

	private void drawOutline(final Mesh mesh, final float height) {
		final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
		buf.rewind();
		final Vector3 p0 = getAbsPoint(0);
		final Vector3 p1 = getAbsPoint(1);
		final Vector3 p2 = getAbsPoint(2);
		final Vector3 p3 = getAbsPoint(3);

		putOutlinePoint(buf, p0);
		putOutlinePoint(buf, p2);
		putOutlinePoint(buf, p2);
		putOutlinePoint(buf, p3);
		putOutlinePoint(buf, p3);
		putOutlinePoint(buf, p1);
		putOutlinePoint(buf, p1);
		putOutlinePoint(buf, p0);

		putOutlinePoint(buf, p0, height);
		putOutlinePoint(buf, p2, height);
		putOutlinePoint(buf, p2, height);
		putOutlinePoint(buf, p3, height);
		putOutlinePoint(buf, p3, height);
		putOutlinePoint(buf, p1, height);
		putOutlinePoint(buf, p1, height);
		putOutlinePoint(buf, p0, height);

		putOutlinePoint(buf, p0);
		putOutlinePoint(buf, p0, height);
		putOutlinePoint(buf, p2);
		putOutlinePoint(buf, p2, height);
		putOutlinePoint(buf, p3);
		putOutlinePoint(buf, p3, height);
		putOutlinePoint(buf, p1);
		putOutlinePoint(buf, p1, height);

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

	private void putOutlinePoint(final FloatBuffer buf, final Vector3 p) {
		putOutlinePoint(buf, p, 0);
	}

	private void putOutlinePoint(final FloatBuffer buf, final Vector3 p, final float height) {
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
		buildingLabel.setTranslation(center.getX(), center.getY(), boundingHeight + height + 6.0);
	}

	private double scanChildrenHeight(final HousePart part) {
		double maxHeight = height;
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
			annot.setRange(getAbsPoint(order[i]), getAbsPoint(order[i + 1]), getCenter(), getNormal(), false, Align.Center, true, true, false);
			annot.setLineWidth(original == null ? 1f : 2f);
		}
	}

	@Override
	public void setEditPoint(int editPoint) {
		if (!resizeHouseMode && editPoint > 3)
			editPoint -= 4;
		super.setEditPoint(editPoint);
		if (!resizeHouseMode) {
			saveOrgPoints();
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

	public void saveOrgPoints() {
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
		return 5.0;
	}

	@Override
	public void updateTextureAndColor() {
		surroundMesh.setDefaultColor(Scene.getInstance().getTextureMode() == TextureMode.Full ? ColorRGBA.GRAY : (getColor() == null ? Scene.getInstance().getFoundationColor() : getColor()));
		updateTextureAndColor(mesh, getColor() == null ? Scene.getInstance().getFoundationColor() : getColor());
	}

	public void showBuildingLabel(final boolean b) {
		buildingLabel.setVisible(b && SceneManager.getInstance().getSolarHeatMap());
	}

	public void setSolarLabelValue(final double solarValue) {
		scanChildrenHeight();
		if (solarValue == -2)
			buildingLabel.setVisible(false);
		else {
			buildingLabel.setVisible(SceneManager.getInstance().areBuildingLabelsVisible());
			final String idLabel = "(#" + id + ")";
			if (solarValue == -1 || solarValue == 0)
				buildingLabel.setText(idLabel);
			else
				buildingLabel.setText(idLabel + "\n" + format.format(solarValue) + "kWh");
		}
	}

	public void move(final Vector3 d, final ArrayList<Vector3> houseMovePoints) {
		if (lockEdit)
			return;
		final List<Vector3> orgPoints = new ArrayList<Vector3>(houseMovePoints.size());
		for (int i = 0; i < points.size(); i++)
			orgPoints.add(points.get(i));

		for (int i = 0; i < points.size(); i++) {
			final Vector3 newP = houseMovePoints.get(i).add(d, null);
			points.set(i, newP);
			if (i == points.size() - 1 && ensureDistanceFromOtherFoundations(newP, i) != newP) {
				for (int j = 0; j < points.size(); j++)
					points.set(j, orgPoints.get(j));
				return;
			}
		}
		Scene.getInstance().redrawAll();
	}

	private void initFloor() {
		if (floorVertices == null)
			floorVertices = new ArrayList<Vector2>();
		else
			floorVertices.clear();
		if (children.isEmpty())
			return;
		final HousePart firstBorn = children.get(0);
		if (firstBorn instanceof Wall)
			((Wall) firstBorn).visitNeighbors(new WallVisitor() {
				@Override
				public void visit(final Wall currentWall, final Snap prev, final Snap next) {
					int pointIndex = 0;
					if (next != null)
						pointIndex = next.getSnapPointIndexOf(currentWall);
					pointIndex++;
					addVertex(currentWall.getAbsPoint(pointIndex == 1 ? 3 : 1));
					addVertex(currentWall.getAbsPoint(pointIndex));
				}
			});
	}

	private void addVertex(final ReadOnlyVector3 v3) {
		final Vector2 v2 = new Vector2(v3.getX(), v3.getY());
		boolean b = false;
		for (final Vector2 x : floorVertices) {
			if (Util.isEqual(x, v2)) {
				b = true;
				break;
			}
		}
		if (!b)
			floorVertices.add(v2);
	}

	private Path2D.Double path;

	public boolean insideBuilding(final double x, final double y, final boolean init) {
		if (init) {
			initFloor();
			final int n = floorVertices.size();
			int m = 0;
			for (final HousePart p : children) {
				if (p instanceof Wall)
					m++;
			}
			if (n <= 0 || m != n) // not closed in the latter case
				return false;
			if (path == null)
				path = new Path2D.Double();
			else
				path.reset();
			Vector2 v = floorVertices.get(0);
			path.moveTo(v.getX(), v.getY());
			for (int i = 1; i < n; i++) {
				v = floorVertices.get(i);
				path.lineTo(v.getX(), v.getY());
			}
			v = floorVertices.get(0);
			path.lineTo(v.getX(), v.getY());
			path.closePath();
		}
		final int n = floorVertices.size();
		int m = 0;
		for (final HousePart p : children) {
			if (p instanceof Wall)
				m++;
		}
		if (n <= 0 || m != n) // not closed in the latter case
			return false;
		return path != null ? path.contains(x, y) : false;
	}

	public double[] getBuildingGeometry() {

		initFloor();
		final int n = floorVertices.size();
		int m = 0;
		for (final HousePart p : children) {
			if (p instanceof Wall)
				m++;
		}
		if (n <= 0 || m != n) // not closed in the latter case
			return null;

		final double scale = Scene.getInstance().getAnnotationScale();
		final double height = boundingHeight * scale;

		double area = 0;
		Vector2 v1, v2;
		for (int i = 0; i < n - 1; i++) {
			v1 = floorVertices.get(i);
			v2 = floorVertices.get(i + 1);
			area += v1.getX() * v2.getY() - v2.getX() * v1.getY();
		}
		v1 = floorVertices.get(n - 1);
		v2 = floorVertices.get(0);
		area += v1.getX() * v2.getY() - v2.getX() * v1.getY();
		area *= 0.5;

		double cx = 0, cy = 0;
		for (int i = 0; i < n - 1; i++) {
			v1 = floorVertices.get(i);
			v2 = floorVertices.get(i + 1);
			cx += (v1.getX() * v2.getY() - v2.getX() * v1.getY()) * (v1.getX() + v2.getX());
			cy += (v1.getX() * v2.getY() - v2.getX() * v1.getY()) * (v1.getY() + v2.getY());
		}
		v1 = floorVertices.get(n - 1);
		v2 = floorVertices.get(0);
		cx += (v1.getX() * v2.getY() - v2.getX() * v1.getY()) * (v1.getX() + v2.getX());
		cy += (v1.getX() * v2.getY() - v2.getX() * v1.getY()) * (v1.getY() + v2.getY());
		cx /= 6 * area;
		cy /= 6 * area;
		cx *= scale;
		cy *= scale;
		area = Math.abs(area) * scale * scale;

		return new double[] { height, area, height * area, cx, cy };

	}

	public double getPassiveSolarNow() {
		return passiveSolarNow;
	}

	public void setPassiveSolarNow(final double passiveSolarNow) {
		this.passiveSolarNow = passiveSolarNow;
	}

	public double getPhotovoltaicNow() {
		return photovoltaicNow;
	}

	public void setPhotovoltaicNow(final double photovoltaicNow) {
		this.photovoltaicNow = photovoltaicNow;
	}

	public double getHeatingNow() {
		return heatingNow;
	}

	public void setHeatingNow(final double heatingNow) {
		this.heatingNow = heatingNow;
	}

	public double getCoolingNow() {
		return coolingNow;
	}

	public void setCoolingNow(final double coolingNow) {
		this.coolingNow = coolingNow;
	}

	public double getTotalEnergyNow() {
		return totalEnergyNow;
	}

	public void setTotalEnergyNow(final double totalEnergyNow) {
		this.totalEnergyNow = totalEnergyNow;
	}

	public double getPassiveSolarToday() {
		return passiveSolarToday;
	}

	public void setPassiveSolarToday(final double passiveSolarToday) {
		this.passiveSolarToday = passiveSolarToday;
	}

	public double getPhotovoltaicToday() {
		return photovoltaicToday;
	}

	public void setPhotovoltaicToday(final double photovoltaicToday) {
		this.photovoltaicToday = photovoltaicToday;
	}

	public double getHeatingToday() {
		return heatingToday;
	}

	public void setHeatingToday(final double heatingToday) {
		this.heatingToday = heatingToday;
	}

	public double getCoolingToday() {
		return coolingToday;
	}

	public void setCoolingToday(final double coolingToday) {
		this.coolingToday = coolingToday;
	}

	public double getTotalEnergyToday() {
		return totalEnergyToday;
	}

	public void setTotalEnergyToday(final double totalEnergyToday) {
		this.totalEnergyToday = totalEnergyToday;
	}

	public void rotate(final double angle) {
		final Matrix3 matrix = new Matrix3().fromAngles(0, 0, angle);
		final ReadOnlyVector3 center = toRelative(getCenter().clone());
		for (int i = 0; i < points.size(); i++) {
			final Vector3 p = getAbsPoint(i);
			final Vector3 op = p.subtract(center, null);
			matrix.applyPost(op, op);
			op.add(center, p);
			points.get(i).set(toRelative(p));
		}
		// Scene.getInstance().redrawAll();
	}

	@Override
	public Vector3 getAbsPoint(final int index) {
		if (useOrgPoints && orgPoints != null)
			return super.toAbsolute(orgPoints.get(index));
		else
			return super.getAbsPoint(index);
	}

	public void setLockEdit(final boolean b) {
		lockEdit = b;
	}

	public boolean getLockEdit() {
		return lockEdit;
	}

	/* Draw the heat flux through the floor area on the foundation */
	@Override
	public void drawHeatFlux() {

		FloatBuffer arrowsVertices = heatFlux.getMeshData().getVertexBuffer();
		final int cols = (int) Math.max(2, getAbsPoint(0).distance(getAbsPoint(2)) / heatFluxUnitArea);
		final int rows = (int) Math.max(2, getAbsPoint(0).distance(getAbsPoint(1)) / heatFluxUnitArea);
		arrowsVertices = BufferUtils.createVector3Buffer(rows * cols * 6);
		heatFlux.getMeshData().setVertexBuffer(arrowsVertices);
		final double heat = calculateHeatVector();
		if (heat != 0) {
			final ReadOnlyVector3 o = getAbsPoint(0);
			final ReadOnlyVector3 u = getAbsPoint(2).subtract(o, null);
			final ReadOnlyVector3 v = getAbsPoint(1).subtract(o, null);
			final ReadOnlyVector3 normal = getNormal().negate(null);
			final Vector3 a = new Vector3();
			double g, h;
			boolean init = true;
			for (int j = 0; j < cols; j++) {
				h = j + 0.5;
				for (int i = 0; i < rows; i++) {
					g = i + 0.5;
					a.setX(o.getX() + g * v.getX() / rows + h * u.getX() / cols);
					a.setY(o.getY() + g * v.getY() / rows + h * u.getY() / cols);
					if (insideBuilding(a.getX(), a.getY(), init)) {
						a.setZ(o.getZ());
						drawArrow(a, normal, arrowsVertices, heat);
					}
					if (init)
						init = false;
				}
			}
			heatFlux.getMeshData().updateVertexCount();
			heatFlux.updateModelBound();
		}

		updateHeatFluxVisibility();

	}

	@Override
	protected void computeArea() {
		if (isDrawCompleted()) {
			final Vector3 p0 = getAbsPoint(0);
			final Vector3 p1 = getAbsPoint(1);
			final Vector3 p2 = getAbsPoint(2);
			final double C = 100.0;
			final double annotationScale = Scene.getInstance().getAnnotationScale();
			area = Math.round(Math.round(p2.subtract(p0, null).length() * annotationScale * C) / C * Math.round(p1.subtract(p0, null).length() * annotationScale * C) / C * C) / C;
		} else
			area = 0.0;
	}

}
