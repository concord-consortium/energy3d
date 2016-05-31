package org.concord.energy3d.model;

import java.awt.geom.Path2D;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.AngleAnnotation;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.util.MeshLib;
import org.concord.energy3d.util.PolygonWithHoles;
import org.concord.energy3d.util.SelectUtil;
import org.concord.energy3d.util.Util;
import org.concord.energy3d.util.WallVisitor;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.geometry.primitives.Point;
import org.poly2tri.transform.coordinate.AnyToXYTransform;
import org.poly2tri.transform.coordinate.XYToAnyTransform;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.point.ardor3d.ArdorVector3Point;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.geom.BufferUtils;

public class Wall extends HousePart implements Thermalizable {

	private static final long serialVersionUID = 1L;
	private static final double DEFAULT_WALL_HEIGHT = 30.0; // the recommended default wall height is 6m
	private static double userDefaultWallHeight = DEFAULT_WALL_HEIGHT;
	private static int currentVisitStamp = 1;
	private static boolean extendToRoofEnabled = true;
	public static final int SOLID_WALL = 0;
	public static final int EMPTY = 1;
	public static final int COLUMNS_ONLY = 2;
	public static final int RAILINGS_ONLY = 3;
	public static final int COLUMNS_RAILINGS = 4;
	public static final int VERTICAL_EDGES_ONLY = 5;

	private transient Mesh backMesh;
	private transient Mesh surroundMesh;
	private transient Mesh invisibleMesh;
	private transient Mesh windowsSurroundMesh;
	private transient Mesh outlineMesh;
	private transient Roof roof;
	private transient int visitStamp;
	private transient Vector3 normal;
	private transient AnyToXYTransform toXY;
	private transient XYToAnyTransform fromXY;
	private transient List<List<Vector3>> wallAndWindowsPoints;
	private double wallThickness;
	private transient Snap[] neighbors;
	private transient Vector3 thicknessNormal;
	private boolean isShortWall;
	private double volumetricHeatCapacity = 0.5; // unit: kWh/m^3/C (1 kWh = 3.6 MJ)
	private double uValue = 0.28; // default is R20 (IECC code for Massachusetts: https://energycode.pnl.gov/EnergyCodeReqs/index.jsp?state=Massachusetts)
	private int type = SOLID_WALL;
	private transient Mesh columns;
	private transient Mesh railings;
	private double columnRadius = 1;
	private double railingRadius = 0.1;
	private Floor floor;

	public static void resetDefaultWallHeight() {
		userDefaultWallHeight = DEFAULT_WALL_HEIGHT;
	}

	public static void clearVisits() {
		currentVisitStamp = ++currentVisitStamp % 1000;
	}

	public Wall() {
		super(2, 4, userDefaultWallHeight);
	}

	@Override
	protected boolean mustHaveContainer() {
		return false;
	}

	@Override
	protected void init() {
		super.init();
		wallThickness = 0.5;
		neighbors = new Snap[2];
		if (thicknessNormal != null)
			thicknessNormal.normalizeLocal().multiplyLocal(wallThickness);

		if (Util.isZero(uValue))
			uValue = 0.28;
		if (Util.isZero(volumetricHeatCapacity))
			volumetricHeatCapacity = 0.5;
		if (Util.isZero(columnRadius))
			columnRadius = 1;
		if (Util.isZero(railingRadius))
			railingRadius = 0.1;

		mesh = new Mesh("Wall");
		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(1));
		// mesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		mesh.setRenderState(offsetState);
		mesh.setModelBound(new BoundingBox());
		root.attachChild(mesh);

		backMesh = new Mesh("Wall (Back)");
		backMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(1));
		backMesh.setDefaultColor(ColorRGBA.LIGHT_GRAY);
		backMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		backMesh.setRenderState(offsetState);
		backMesh.setModelBound(new BoundingBox());
		root.attachChild(backMesh);

		surroundMesh = new Mesh("Wall (Surround)");
		surroundMesh.getMeshData().setIndexMode(IndexMode.Quads);
		surroundMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(12));
		surroundMesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(12));
		surroundMesh.setDefaultColor(ColorRGBA.GRAY);
		surroundMesh.setRenderState(offsetState);
		surroundMesh.setModelBound(new BoundingBox());
		root.attachChild(surroundMesh);

		invisibleMesh = new Mesh("Wall (Invisible)");
		invisibleMesh.getMeshData().setIndexMode(IndexMode.Quads);
		invisibleMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		invisibleMesh.getSceneHints().setCullHint(CullHint.Always);
		invisibleMesh.setRenderState(offsetState);
		invisibleMesh.setModelBound(new BoundingBox());
		root.attachChild(invisibleMesh);

		windowsSurroundMesh = new Mesh("Wall (Windows Surround)");
		windowsSurroundMesh.getMeshData().setIndexMode(IndexMode.Quads);
		windowsSurroundMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(1000));
		windowsSurroundMesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(1000));
		windowsSurroundMesh.setDefaultColor(ColorRGBA.GRAY);
		windowsSurroundMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		windowsSurroundMesh.setRenderState(offsetState);
		/* lets not use bounds for this mesh because when there are no windows its bounds is set to center 0,0,0 which shifts the overall bounds toward zero */
		windowsSurroundMesh.setModelBound(null);
		root.attachChild(windowsSurroundMesh);

		outlineMesh = new Line("Wall (Outline)");
		outlineMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(8));
		outlineMesh.setDefaultColor(ColorRGBA.BLACK);
		outlineMesh.setModelBound(null);
		Util.disablePickShadowLight(outlineMesh);
		root.attachChild(outlineMesh);

		updateTextureAndColor();

		final UserData userData = new UserData(this);
		mesh.setUserData(userData);
		backMesh.setUserData(userData);
		surroundMesh.setUserData(userData);
		invisibleMesh.setUserData(userData);

		columns = new Mesh("Columns");
		columns.getMeshData().setIndexMode(IndexMode.Quads);
		columns.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(500));
		columns.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(500));
		columns.setRenderState(offsetState);
		columns.setModelBound(new BoundingBox());
		root.attachChild(columns);

		railings = new Mesh("Railings");
		railings.getMeshData().setIndexMode(IndexMode.Quads);
		railings.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(1000));
		railings.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(1000));
		railings.setRenderState(offsetState);
		railings.setModelBound(new BoundingBox());
		root.attachChild(railings);

	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		Snap.clearAnnotationDrawn();
		if (editPointIndex == -1 || editPointIndex == 0 || editPointIndex == 2) {
			final HousePart previousContainer = container;
			PickedHousePart picked = pickContainer(x, y, new Class<?>[] { Foundation.class });
			if (container != previousContainer && previousContainer != null && (isFirstPointInserted() || container == null)) {
				container = previousContainer;
				picked = null;
			}

			if (container == null)
				return;

			Vector3 p = null;
			if (picked != null)
				p = picked.getPoint().clone();
			else
				p = findClosestPointOnFoundation(x, y);

			if (p == null)
				return;

			if (container != null)
				p.setZ(container.height);
			final int index = (editPointIndex == -1) ? points.size() - 2 : editPointIndex;
			boolean snappedToWall = snapToWall(p, index);
			if (!snappedToWall) {
				snapToGrid(p, getAbsPoint(index), getGridSize(), false);
				snappedToWall = snapToWall(p, index); // see if it can be snapped after grid move
			}

			if (!snappedToWall)
				snapToFoundation(p);

			if (index == 2) // make sure z of 2nd base point is same as 2st (needed for platform picking side)
				p.setZ(points.get(0).getZ());
			final Vector3 p_rel = toRelative(p);
			points.get(index).set(p_rel);
			points.get(index + 1).set(p_rel).setZ(p.getZ() + height);
		} else if (editPointIndex == 1 || editPointIndex == 3) {
			final int lower = (editPointIndex == 1) ? 0 : 2;
			final Vector3 base = getAbsPoint(lower);
			final Vector3 closestPoint = Util.closestPoint(base, Vector3.UNIT_Z, x, y);
			snapToGrid(closestPoint, getAbsPoint(editPointIndex), getGridSize());
			height = Math.max(getGridSize(), closestPoint.getZ() - base.getZ());
			userDefaultWallHeight = height;
			final double z = height + base.getZ();
			points.get(1).setZ(z);
			points.get(3).setZ(z);
		}

		Scene.getInstance().connectWalls();
		if (roof != null)
			roof.getIntersectionCache().clear();
		drawThisAndNeighbors(false);
		setEditPointsVisible(true);

		if (container != null)
			((Foundation) container).scanChildrenHeight();
	}

	@Override
	public double getGridSize() {
		return SceneManager.getInstance().isFineGrid() ? 0.5 : 2.5;
	}

	public Vector3 findClosestPointOnFoundation(final int x, final int y) {
		final PickedHousePart floorPick = SelectUtil.pickPart(x, y, (HousePart) null);
		if (floorPick != null) {
			Vector3 p = floorPick.getPoint().clone();
			ReadOnlyVector3 closesPoint = container.points.get(0);
			for (int i = 1; i < 4; i++)
				if (closesPoint.distance(p) > container.points.get(i).distance(p))
					closesPoint = container.points.get(i);
			ReadOnlyVector3 secondClosesPoint = closesPoint == container.points.get(0) ? container.points.get(1) : container.points.get(0);
			for (int i = 0; i < 4; i++)
				if (secondClosesPoint.distance(p) > container.points.get(i).distance(p) && container.points.get(i) != closesPoint)
					secondClosesPoint = container.points.get(i);
			final Vector3 dir = closesPoint.subtract(secondClosesPoint, null).normalizeLocal();
			p = Util.closestPoint(closesPoint, dir, p, Vector3.NEG_UNIT_Z);
			snapToGrid(p, getAbsPoint(editPointIndex == -1 ? points.size() - 2 : editPointIndex), getGridSize());
			p = Util.closestPoint(closesPoint, dir, p, Vector3.NEG_UNIT_Z);
			p.setX(MathUtils.clamp(p.getX(), Math.min(container.points.get(0).getX(), container.points.get(2).getX()), Math.max(container.points.get(0).getX(), container.points.get(2).getX())));
			p.setY(MathUtils.clamp(p.getY(), Math.min(container.points.get(0).getY(), container.points.get(1).getY()), Math.max(container.points.get(0).getY(), container.points.get(1).getY())));
			p.getZ();
			return p;
		} else
			return null;

	}

	private void drawThisAndNeighbors(final boolean extendToRoofEnabled) {
		thicknessNormal = null;
		isShortWall = true;
		Wall.extendToRoofEnabled = extendToRoofEnabled;
		if (isDrawable())
			computeInsideDirectionOfAttachedWalls(true);
		draw();
		drawChildren();
		Wall.extendToRoofEnabled = true;
	}

	@Override
	public void complete() {
		drawThisAndNeighbors(true); // needed in order to extend wall to roof
		super.complete();
	}

	protected boolean snapToWall(final Vector3 p, final int index) {
		ReadOnlyVector3 closestPoint = null;
		double closestDistance = Double.MAX_VALUE;
		for (final HousePart housePart : container.getChildren()) {
			if (housePart instanceof Wall && housePart != this) {
				final Wall wall = (Wall) housePart;
				for (int i = 0; i < wall.points.size(); i += 2) {
					final ReadOnlyVector3 p2 = wall.getAbsPoint(i);
					final double distance = p.distance(p2);
					if (distance < closestDistance) {
						closestPoint = p2;
						closestDistance = distance;
					}
				}
			}
		}

		final double snapDistance = isSnapToObjects() ? getGridSize() : SNAP_DISTANCE;

		final boolean snap;
		if (isFirstPointInserted() && p.subtract(getAbsPoint(index == 0 ? 2 : 0), null).length() < getGridSize() * 2)
			snap = false;
		else if (closestDistance < snapDistance)
			snap = true;
		else if (neighbors[index / 2] != null && closestDistance < snapDistance + getGridSize())
			snap = true;
		else
			snap = false;

		if (snap) {
			p.set(closestPoint);
			return true;
		} else
			return false;
	}

	private boolean snapToFoundation(final Vector3 current) {
		if (container == null)
			return false;
		ReadOnlyVector3 snapPoint = null;
		double snapDistance = Double.MAX_VALUE;
		final int[] indices = new int[] { 0, 2, 3, 1, 0 };
		for (int i = 0; i < indices.length - 1; i++) {
			final Vector3 p1 = container.getAbsPoint(indices[i]);
			final Vector3 p2 = container.getAbsPoint(indices[i + 1]);
			final Vector2 p2D = Util.projectPointOnLine(new Vector2(current.getX(), current.getY()), new Vector2(p1.getX(), p1.getY()), new Vector2(p2.getX(), p2.getY()), true);
			final Vector3 p = new Vector3(p2D.getX(), p2D.getY(), current.getZ());
			final double d = p.distance(current);
			if (d < snapDistance) {
				snapDistance = d;
				snapPoint = p;
			}
		}

		if (snapDistance < getGridSize() / 2) {
			current.set(snapPoint.getX(), snapPoint.getY(), current.getZ());
			return true;
		} else
			return false;
	}

	@Override
	public boolean isDrawable() {
		return super.isDrawable() && !isAtSamePlaceAsAnotherPart(this);
	}

	private boolean isAtSamePlaceAsAnotherPart(final HousePart selectedHousePart) {
		if (selectedHousePart instanceof Wall) {
			final Vector3 p0 = selectedHousePart.getAbsPoint(0);
			final Vector3 p2 = selectedHousePart.getAbsPoint(2);
			for (final HousePart part : selectedHousePart.getContainer().getChildren())
				if (part != selectedHousePart && part instanceof Wall && part.isDrawCompleted()) {
					final Vector3 q0 = part.getAbsPoint(0);
					final Vector3 q2 = part.getAbsPoint(2);
					if ((p0.equals(q0) && p2.equals(q2)) || (p2.equals(q0) && p0.equals(q2)))
						return true;
				}
		}
		return false;
	}

	@Override
	protected void drawMesh() {
		mesh.getSceneHints().setCullHint(isDrawable() && type == SOLID_WALL ? CullHint.Inherit : CullHint.Always);
		final boolean b = isDrawable() && !isFrozen() && type == SOLID_WALL;
		backMesh.getSceneHints().setCullHint(b ? CullHint.Inherit : CullHint.Always);
		surroundMesh.getSceneHints().setCullHint(b ? CullHint.Inherit : CullHint.Always);
		windowsSurroundMesh.getSceneHints().setCullHint(b ? CullHint.Inherit : CullHint.Always);

		if (!isDrawable())
			return;

		computeNormalAndXYTransform();

		wallAndWindowsPoints = computeWallAndWindowPolygon(false);
		extendToRoof(wallAndWindowsPoints.get(0));

		switch (type) {
		case EMPTY:
			outlineMesh.getSceneHints().setCullHint(CullHint.Always);
			columns.getSceneHints().setCullHint(CullHint.Always);
			railings.getSceneHints().setCullHint(CullHint.Always);
			break;
		case VERTICAL_EDGES_ONLY:
			outlineMesh.getSceneHints().setCullHint(CullHint.Always);
			columns.getSceneHints().setCullHint(CullHint.Inherit);
			railings.getSceneHints().setCullHint(CullHint.Always);
			drawVerticalEdges();
			break;
		case COLUMNS_ONLY:
			outlineMesh.getSceneHints().setCullHint(CullHint.Always);
			columns.getSceneHints().setCullHint(CullHint.Inherit);
			railings.getSceneHints().setCullHint(CullHint.Always);
			drawColumns(10);
			break;
		case RAILINGS_ONLY:
			outlineMesh.getSceneHints().setCullHint(CullHint.Always);
			columns.getSceneHints().setCullHint(CullHint.Always);
			railings.getSceneHints().setCullHint(CullHint.Inherit);
			drawRailings(1);
			break;
		case COLUMNS_RAILINGS:
			outlineMesh.getSceneHints().setCullHint(CullHint.Always);
			columns.getSceneHints().setCullHint(CullHint.Inherit);
			railings.getSceneHints().setCullHint(CullHint.Inherit);
			drawColumns(10);
			drawRailings(1);
			break;
		default:
			outlineMesh.getSceneHints().setCullHint(isDrawable() ? CullHint.Inherit : CullHint.Always);
			columns.getSceneHints().setCullHint(CullHint.Always);
			railings.getSceneHints().setCullHint(CullHint.Always);
			if (Scene.getInstance().isDrawThickness() && isShortWall) {
				final Vector3 dir = getAbsPoint(2).subtract(getAbsPoint(0), null).normalizeLocal();
				if (neighbors[0] != null && neighbors[0].getNeighborOf(this).isFirstPointInserted()) {
					if (isPerpendicularToNeighbor(0))
						reduceBackMeshWidth(wallAndWindowsPoints.get(0), dir, 0);
				}
				if (neighbors[1] != null && neighbors[1].getNeighborOf(this).isFirstPointInserted()) {
					dir.normalizeLocal().negateLocal();
					if (isPerpendicularToNeighbor(1))
						reduceBackMeshWidth(wallAndWindowsPoints.get(0), dir, 1);
				}
			}
			drawOutline(wallAndWindowsPoints);
			if (!isFrozen()) {
				drawBackMesh(computeWallAndWindowPolygon(true));
				drawSurroundMesh(thicknessNormal);
				drawWindowsSurroundMesh(thicknessNormal);
			}
		}
		drawPolygon(wallAndWindowsPoints, mesh, true, true, true);
		drawPolygon(wallAndWindowsPoints, invisibleMesh, false, false, false);
		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
		CollisionTreeManager.INSTANCE.removeCollisionTree(invisibleMesh);

		root.updateWorldBound(true);
	}

	private void addPointToQuad(final ReadOnlyVector3 v1, final ReadOnlyVector3 v2, final Vector3 dir, final FloatBuffer vertexBuffer, final FloatBuffer normalBuffer) {
		final ReadOnlyVector3 p1 = new Vector3(v1).addLocal(dir);
		final ReadOnlyVector3 p3 = new Vector3(v2).addLocal(dir);
		dir.negateLocal();
		final ReadOnlyVector3 p2 = new Vector3(v1).addLocal(dir);
		final ReadOnlyVector3 p4 = new Vector3(v2).addLocal(dir);
		vertexBuffer.put(p1.getXf()).put(p1.getYf()).put(p1.getZf());
		vertexBuffer.put(p3.getXf()).put(p3.getYf()).put(p3.getZf());
		vertexBuffer.put(p4.getXf()).put(p4.getYf()).put(p4.getZf());
		vertexBuffer.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
		for (int i = 0; i < 4; i++)
			normalBuffer.put(normal.getXf()).put(normal.getYf()).put(normal.getZf());
	}

	private void drawVerticalEdges() {
		columns.setDefaultColor(getColor());
		final FloatBuffer vertexBuffer = columns.getMeshData().getVertexBuffer();
		final FloatBuffer normalBuffer = columns.getMeshData().getNormalBuffer();
		vertexBuffer.rewind();
		normalBuffer.rewind();
		vertexBuffer.limit(vertexBuffer.capacity());
		normalBuffer.limit(normalBuffer.capacity());

		final ReadOnlyVector3 o = getAbsPoint(0);
		final ReadOnlyVector3 u = getAbsPoint(2).subtract(o, null);

		final Vector3 dir = new Vector3(u).normalizeLocal().multiplyLocal(columnRadius);
		addPointToQuad(o, getAbsPoint(1), dir, vertexBuffer, normalBuffer);
		addPointToQuad(getAbsPoint(2), getAbsPoint(3), dir, vertexBuffer, normalBuffer);

		vertexBuffer.limit(vertexBuffer.position());
		normalBuffer.limit(normalBuffer.position());
		columns.getMeshData().updateVertexCount();
		columns.updateModelBound();
	}

	private void drawColumns(final double distance) {
		columns.setDefaultColor(getColor());
		final FloatBuffer vertexBuffer = columns.getMeshData().getVertexBuffer();
		final FloatBuffer normalBuffer = columns.getMeshData().getNormalBuffer();
		vertexBuffer.rewind();
		normalBuffer.rewind();
		vertexBuffer.limit(vertexBuffer.capacity());
		normalBuffer.limit(normalBuffer.capacity());

		final ReadOnlyVector3 o = getAbsPoint(0);
		final ReadOnlyVector3 u = getAbsPoint(2).subtract(o, null);
		final ReadOnlyVector3 v = getAbsPoint(1).subtract(o, null);
		final int cols = (int) Math.max(2, u.length() / distance);

		// Vector3 dir = new Vector3(v).normalizeLocal().multiplyLocal(columnRadius);
		// addPointToQuad(getAbsPoint(1), getAbsPoint(3), dir, vertexBuffer, normalBuffer);
		final Vector3 dir = new Vector3(u).normalizeLocal().multiplyLocal(columnRadius);
		addPointToQuad(o, getAbsPoint(1), dir, vertexBuffer, normalBuffer);
		addPointToQuad(getAbsPoint(2), getAbsPoint(3), dir, vertexBuffer, normalBuffer);

		final Vector3 p = new Vector3();
		for (int col = 1; col < cols; col++) {
			u.multiply((double) col / cols, p).addLocal(o);
			addPointToQuad(p, p.add(v, null), dir, vertexBuffer, normalBuffer);
		}

		vertexBuffer.limit(vertexBuffer.position());
		normalBuffer.limit(normalBuffer.position());
		columns.getMeshData().updateVertexCount();
		columns.updateModelBound();
	}

	private Floor getFloor() {
		for (final HousePart hp : children) {
			if (hp instanceof Floor)
				return (Floor) hp;
		}
		return null;
	}

	private void drawRailings(final double distance) {
		railings.setDefaultColor(getColor());
		final FloatBuffer vertexBuffer = railings.getMeshData().getVertexBuffer();
		final FloatBuffer normalBuffer = railings.getMeshData().getNormalBuffer();
		vertexBuffer.rewind();
		normalBuffer.rewind();
		vertexBuffer.limit(vertexBuffer.capacity());
		normalBuffer.limit(normalBuffer.capacity());

		final ReadOnlyVector3 o = getAbsPoint(0);
		final ReadOnlyVector3 u = getAbsPoint(2).subtract(o, null);
		final Vector3 v = getAbsPoint(1).subtract(o, null);
		final int cols = (int) Math.max(2, u.length() / distance);

		floor = getFloor();
		if (floor == null) {
			visitNeighbors(new WallVisitor() {
				@Override
				public void visit(final Wall currentWall, final Snap prev, final Snap next) {
					final Floor f = currentWall.getFloor();
					if (f != null)
						floor = f;
				}
			});
		}
		if (floor == null) {

			final double heightRatio = 0.33;
			v.multiplyLocal(heightRatio);
			Vector3 dir = new Vector3(v).normalizeLocal().multiplyLocal(railingRadius * 3);
			addPointToQuad(getAbsPoint(1).multiplyLocal(1, 1, heightRatio), getAbsPoint(3).multiplyLocal(1, 1, heightRatio), dir, vertexBuffer, normalBuffer);
			dir = new Vector3(u).normalizeLocal().multiplyLocal(railingRadius);
			// addPointToQuad(o, getAbsPoint(1), dir, vertexBuffer, normalBuffer);
			// addPointToQuad(getAbsPoint(2), getAbsPoint(3), dir, vertexBuffer, normalBuffer);

			final Vector3 p = new Vector3();
			for (int col = 0; col <= cols; col++) {
				u.multiply((double) col / cols, p).addLocal(o.getX(), o.getY(), 0);
				addPointToQuad(p, p.add(v, null), dir, vertexBuffer, normalBuffer);
			}

		} else {

			final double z0 = floor.getAbsPoint(0).getZ();
			Vector3 dir = new Vector3(v).normalizeLocal().multiplyLocal(railingRadius * 3);
			addPointToQuad(getAbsPoint(1), getAbsPoint(3), dir, vertexBuffer, normalBuffer);
			dir = new Vector3(u).normalizeLocal().multiplyLocal(railingRadius);
			Vector3 q = getAbsPoint(1);
			addPointToQuad(q, new Vector3(q.getX(), q.getY(), z0), dir, vertexBuffer, normalBuffer);
			q = getAbsPoint(3);
			addPointToQuad(q, new Vector3(q.getX(), q.getY(), z0), dir, vertexBuffer, normalBuffer);

			q = new Vector3(0, 0, q.getZ() - z0);
			final Vector3 p = new Vector3();
			for (int col = 1; col < cols; col++) {
				u.multiply((double) col / cols, p).addLocal(o.getX(), o.getY(), z0);
				addPointToQuad(p, p.add(q, null), dir, vertexBuffer, normalBuffer);
			}

		}

		vertexBuffer.limit(vertexBuffer.position());
		normalBuffer.limit(normalBuffer.position());
		railings.getMeshData().updateVertexCount();
		railings.updateModelBound();
	}

	private void drawPolygon(final List<List<Vector3>> wallAndWindowsPoints, final Mesh mesh, final boolean drawHoles, final boolean normal, final boolean texture) {
		final List<PolygonPoint> polygonPoints = new ArrayList<PolygonPoint>(wallAndWindowsPoints.get(0).size());
		for (final Vector3 p : wallAndWindowsPoints.get(0)) {
			final PolygonPoint tp = new PolygonPoint(p.getX(), p.getY(), p.getZ());
			toXY.transform(tp);
			polygonPoints.add(tp);
		}
		final PolygonWithHoles polygon = new PolygonWithHoles(polygonPoints);
		if (drawHoles) {
			for (int i = 1; i < wallAndWindowsPoints.size(); i++) {
				final List<PolygonPoint> holePoints = new ArrayList<PolygonPoint>(wallAndWindowsPoints.get(i).size());
				for (final Vector3 p : wallAndWindowsPoints.get(i)) {
					final PolygonPoint tp = new PolygonPoint(p.getX(), p.getY(), p.getZ());
					toXY.transform(tp);
					holePoints.add(tp);
				}
				polygon.addHole(new Polygon(holePoints));
			}
		}

		if (texture) {
			final double scale = Scene.getInstance().getTextureMode() == TextureMode.Simple ? 1.0 : 8.0;
			final boolean fullTexture = Scene.getInstance().getTextureMode() == TextureMode.Full;
			final ReadOnlyVector3 p0 = getAbsPoint(0);
			final ReadOnlyVector3 p01 = getAbsPoint(1).subtractLocal(p0).normalizeLocal().multiplyLocal(scale * (fullTexture ? 1.5 : 1.0));
			final ReadOnlyVector3 p02 = getAbsPoint(2).subtractLocal(p0).normalizeLocal().multiplyLocal(scale * (fullTexture ? 2.0 : 1.0));
			final TPoint o = new TPoint(p0.getX(), p0.getY(), p0.getZ());
			final TPoint u = new TPoint(p01.getX(), p01.getY(), p01.getZ());
			final TPoint v = new TPoint(p02.getX(), p02.getY(), p02.getZ());
			toXY.transform(o);
			toXY.transform(u);
			MeshLib.fillMeshWithPolygon(mesh, polygon, fromXY, normal, o, u, v, true);
		} else
			MeshLib.fillMeshWithPolygon(mesh, polygon, fromXY, normal, null, null, null, true);
	}

	private void drawOutline(final List<List<Vector3>> wallAndWindowsPoints) {
		final List<Vector3> wallPolygonPoints = wallAndWindowsPoints.get(0);
		FloatBuffer outlineVertexBuffer = outlineMesh.getMeshData().getVertexBuffer();
		final int requiredSize = 2 * (wallPolygonPoints.size() + (wallAndWindowsPoints.size() - 1) * 4);
		if (outlineVertexBuffer.capacity() / 3 < requiredSize) {
			outlineVertexBuffer = BufferUtils.createVector3Buffer(requiredSize);
			outlineMesh.getMeshData().setVertexBuffer(outlineVertexBuffer);
		} else {
			outlineVertexBuffer.rewind();
			outlineVertexBuffer.limit(outlineVertexBuffer.capacity());
		}
		outlineVertexBuffer.rewind();

		ReadOnlyVector3 prev = wallPolygonPoints.get(wallPolygonPoints.size() - 1);
		for (final ReadOnlyVector3 point : wallPolygonPoints) {
			outlineVertexBuffer.put(prev.getXf()).put(prev.getYf()).put(prev.getZf());
			prev = point;
			outlineVertexBuffer.put(point.getXf()).put(point.getYf()).put(point.getZf());
		}

		for (int i = 1; i < wallAndWindowsPoints.size(); i++) {
			final List<Vector3> windowHolePoints = wallAndWindowsPoints.get(i);
			prev = windowHolePoints.get(3);
			for (int j = 0; j < 4; j++) {
				final ReadOnlyVector3 point = windowHolePoints.get(j);
				outlineVertexBuffer.put(prev.getXf()).put(prev.getYf()).put(prev.getZf());
				prev = point;
				outlineVertexBuffer.put(point.getXf()).put(point.getYf()).put(point.getZf());
			}
		}
		outlineVertexBuffer.limit(outlineVertexBuffer.position());
		outlineMesh.getMeshData().updateVertexCount();
		outlineMesh.updateModelBound();
		outlineMesh.setTranslation(getNormal().multiply(0.001, null));
	}

	public List<List<Vector3>> computeWallAndWindowPolygon(final boolean backMesh) {
		final List<List<Vector3>> polygonPoints = new ArrayList<List<Vector3>>();
		final ReadOnlyVector3 trans = backMesh ? getThicknessNormal() : Vector3.ZERO;
		// Start the polygon with (1) then 0, 2, 3, [roof points] so that roof
		// points are appended to the end of vertex list
		final ArrayList<Vector3> wallPoints = new ArrayList<Vector3>(4);
		addPolygonPoint(wallPoints, this, 1, trans);
		addPolygonPoint(wallPoints, this, 0, trans);
		addPolygonPoint(wallPoints, this, 2, trans);
		addPolygonPoint(wallPoints, this, 3, trans);
		polygonPoints.add(wallPoints);

		// Add window holes
		for (final HousePart child : children) {
			if (child instanceof Window && includeWindow(child))
				polygonPoints.add(computeWindowHole(child, trans));
		}
		return polygonPoints;
	}

	private List<Vector3> computeWindowHole(final HousePart window, final ReadOnlyVector3 trans) {
		final List<Vector3> windowPoints = new ArrayList<Vector3>(4);
		addPolygonPoint(windowPoints, window, 1, trans);
		addPolygonPoint(windowPoints, window, 0, trans);
		addPolygonPoint(windowPoints, window, 2, trans);
		addPolygonPoint(windowPoints, window, 3, trans);
		return windowPoints;
	}

	private void addPolygonPoint(final List<Vector3> points, final HousePart housePart, final int index, final ReadOnlyVector3 trans) {
		points.add(housePart.getAbsPoint(index).addLocal(trans));
	}

	private void extendToRoof(final List<Vector3> polygon) {
		if (!extendToRoofEnabled)
			return;

		final int[] upper = { 0, 3 };

		for (final int i : upper) {
			final Vector3 tp = polygon.get(i);
			final double z = findRoofIntersection(tp);
			tp.set(tp.getX(), tp.getY(), z);
		}

		Vector3 tp = polygon.get(0);
		final ReadOnlyVector3 o = tp;
		tp = polygon.get(3);
		final Vector3 dir = tp.subtract(o, null);
		dir.setZ(0);
		final double length = dir.length();
		dir.normalizeLocal();

		if (roof != null && !isFrozen()) {
			Vector3 direction = null;
			ReadOnlyVector3 previousStretchPoint = polygon.get(3);

			final double step = 0.1;
			for (double d = length - step; d > step; d -= step) {
				final Vector3 p = dir.multiply(d, null).addLocal(o);
				final double findRoofIntersection = findRoofIntersection(p);

				final ReadOnlyVector3 currentStretchPoint = new Vector3(p.getX(), p.getY(), findRoofIntersection);
				final Vector3 currentDirection = currentStretchPoint.subtract(previousStretchPoint, null).normalizeLocal();

				if (direction == null) {
					direction = currentDirection;
				} else if (direction.dot(currentDirection) < 1.0 - MathUtils.ZERO_TOLERANCE) {
					direction = null;
					p.setZ(findRoofIntersection);
					polygon.add(p);
				}
				previousStretchPoint = currentStretchPoint;
			}
		}
	}

	public double findRoofIntersection(final ReadOnlyVector3 p) {
		return findRoofIntersection(p, Vector3.UNIT_Z, -0.02).getZ();
	}

	public ReadOnlyVector3 findRoofIntersection(final ReadOnlyVector3 p, final ReadOnlyVector3 direction, final double offset) {
		if (roof == null)
			return p;

		final int key = Objects.hash(p.getX(), p.getY(), p.getZ());
		ReadOnlyVector3 result = roof.getIntersectionCache().get(key);
		if (result == null) {
			final Vector3 origin = new Vector3(p.getX(), p.getY(), direction.equals(Vector3.UNIT_Z) ? 0 : p.getZ());
			final PickResults pickResults = new PrimitivePickResults();
			PickingUtil.findPick(roof.getRoofPartsRoot(), new Ray3(origin, direction), pickResults, false);
			if (pickResults.getNumber() > 0)
				result = pickResults.getPickData(0).getIntersectionRecord().getIntersectionPoint(0).add(direction.multiply(roof.getOverhangLength() > 0.05 ? offset : 0, null), null);
			else
				result = p;
			roof.getIntersectionCache().put(key, result);
		}
		return result;
	}

	public boolean isPerpendicularToNeighbor(final int neighbor) {
		final Vector3 dir = getAbsPoint(2).subtract(getAbsPoint(0), null).normalizeLocal();
		final int i = neighbors[neighbor].getSnapPointIndexOfNeighborOf(this);
		final Vector3 otherDir = getAbsPoint(i == 0 ? 2 : 0).subtract(getAbsPoint(i), null).normalizeLocal();
		return Math.abs(dir.dot(otherDir)) < 0.1;
	}

	public boolean includeWindow(final HousePart window) {
		return window.getPoints().size() >= 4 && window.getAbsPoint(2).distance(window.getAbsPoint(0)) >= 0.1 && window.getAbsPoint(1).distance(window.getAbsPoint(0)) >= 0.1;
	}

	private void drawBackMesh(final List<List<Vector3>> polygon) {
		final Vector3 dir = getAbsPoint(2).subtract(getAbsPoint(0), null).normalizeLocal();
		if (neighbors[0] != null && neighbors[0].getNeighborOf(this).isFirstPointInserted() && !(Scene.getInstance().isDrawThickness() && isShortWall && isPerpendicularToNeighbor(0))) {
			reduceBackMeshWidth(polygon.get(0), dir, 0);
		}

		if (neighbors[1] != null && neighbors[1].getNeighborOf(this).isFirstPointInserted() && !(Scene.getInstance().isDrawThickness() && isShortWall && isPerpendicularToNeighbor(1))) {
			dir.normalizeLocal().negateLocal();
			reduceBackMeshWidth(polygon.get(0), dir, 1);
		}

		enforceGablePointsRangeAndRemoveDuplicatedGablePoints(polygon.get(0));
		extendToRoof(polygon.get(0));

		/* lower the z of back wall to ensure it doesn't stick up through the roof */
		if (roof != null)
			for (final Vector3 p : polygon.get(0))
				p.setZ(p.getZ() - 0.3);

		drawPolygon(polygon, backMesh, true, true, false);
	}

	private void enforceGablePointsRangeAndRemoveDuplicatedGablePoints(final List<Vector3> polygonPoints) {
		final Vector2 min = new Vector2(Math.min(polygonPoints.get(1).getX(), polygonPoints.get(2).getX()), Math.min(polygonPoints.get(1).getY(), polygonPoints.get(2).getY()));
		final Vector2 max = new Vector2(Math.max(polygonPoints.get(1).getX(), polygonPoints.get(2).getX()), Math.max(polygonPoints.get(1).getY(), polygonPoints.get(2).getY()));
		for (int i = 4; i < polygonPoints.size(); i++) {
			final Vector3 tp = polygonPoints.get(i);
			tp.set(Math.max(tp.getX(), min.getX()), Math.max(tp.getY(), min.getY()), tp.getZ());
			tp.set(Math.min(tp.getX(), max.getX()), Math.min(tp.getY(), max.getY()), tp.getZ());
			for (int j = 0; j < i; j++) {
				final Vector3 tpj = polygonPoints.get(j);
				if (Util.isEqual(tpj.getX(), tp.getX()) && Util.isEqual(tpj.getY(), tp.getY())) {
					polygonPoints.remove(i);
					i--;
					break;
				}
			}
		}
	}

	private void reduceBackMeshWidth(final List<Vector3> polygon, final ReadOnlyVector3 wallDir, final int neighbor) {
		final Snap snap = neighbors[neighbor];
		final int neighborPointIndex = snap.getSnapPointIndexOfNeighborOf(this);
		final Wall otherWall = snap.getNeighborOf(this);
		final Vector3 otherWallDir = otherWall.getAbsPoint(neighborPointIndex == 0 ? 2 : 0).subtract(otherWall.getAbsPoint(neighborPointIndex), null).normalizeLocal();
		final double angle = Math.max(0.1, otherWallDir.smallestAngleBetween(wallDir));
		final double angle360;
		if (wallDir.dot(otherWall.getThicknessNormal().normalize(null)) < 0)
			angle360 = Math.PI + angle;
		else
			angle360 = angle;

		final boolean reverse = angle360 >= Math.PI;
		final double length = wallThickness * Math.tan((Math.PI - angle) / 2) * (reverse ? -1 : 1);

		final Vector3 v = wallDir.normalize(null).multiplyLocal(length);

		final Vector3 p1 = polygon.get(neighbor == 0 ? 1 : 2);
		final Vector3 p2 = polygon.get(neighbor == 0 ? 0 : 3);

		// now reduce the actual wall points
		p1.set(p1.getX() + v.getX(), p1.getY() + v.getY(), p1.getZ());
		p2.set(p2.getX() + v.getX(), p2.getY() + v.getY(), p2.getZ());

	}

	public Vector3 getThicknessNormal() {
		if (thicknessNormal != null)
			return thicknessNormal;
		computeNormalAndXYTransform();
		final Vector3 n = normal.clone();
		final Snap neighbor;

		final int whichNeighbor;
		if (editPointIndex == 0 || editPointIndex == 1) {
			/*
			 * if edit point has snapped to a new wall then use the angle with new wall to determine inside direction of this wall otherwise use the angle with the other wall attached to none moving corner of the this wall
			 */
			if (neighbors[0] == null)
				whichNeighbor = 1;
			else
				whichNeighbor = 0;
		} else {
			if (neighbors[1] == null)
				whichNeighbor = 0;
			else
				whichNeighbor = 1;
		}
		neighbor = neighbors[whichNeighbor];

		if (neighbor != null && neighbor.getNeighborOf(this).getPoints().size() >= 4) {
			final HousePart other = neighbor.getNeighborOf(this);
			final int otherPointIndex = neighbor.getSnapPointIndexOfNeighborOf(this);
			final Vector3 otherWallDir = other.getAbsPoint(otherPointIndex == 0 ? 2 : 0).subtract(other.getAbsPoint(otherPointIndex), null).normalizeLocal();
			if (n.dot(otherWallDir) < 0)
				n.negateLocal();
		} else {
			final ReadOnlyVector3 camera = SceneManager.getInstance().getCamera().getDirection();
			if (camera.dot(n) < 0)
				n.negateLocal();
		}
		n.multiplyLocal(wallThickness);
		thicknessNormal = n;
		return thicknessNormal;
	}

	private void computeNormalAndXYTransform() {
		final Vector3 p02 = getAbsPoint(2).subtract(getAbsPoint(0), null).normalizeLocal();
		final Vector3 p01 = getAbsPoint(1).subtract(getAbsPoint(0), null).normalizeLocal();
		normal = p02.crossLocal(p01).normalizeLocal();
		toXY = new AnyToXYTransform(normal.getX(), normal.getY(), normal.getZ());
		fromXY = new XYToAnyTransform(normal.getX(), normal.getY(), normal.getZ());
	}

	private void drawSurroundMesh(final ReadOnlyVector3 thickness) {
		final boolean drawThicknessAndIsLongWall = Scene.getInstance().isDrawThickness() && !isShortWall;
		final boolean noNeighbor0 = neighbors[0] == null || (drawThicknessAndIsLongWall && isPerpendicularToNeighbor(0));
		final boolean noNeighbor1 = neighbors[1] == null || (drawThicknessAndIsLongWall && isPerpendicularToNeighbor(1));

		final boolean visible = roof == null || noNeighbor0 || noNeighbor1;
		surroundMesh.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
		if (!visible) {
			if (surroundMesh.getModelBound() != null)
				surroundMesh.setModelBound(null);
		} else {
			if (surroundMesh.getModelBound() == null)
				surroundMesh.setModelBound(new BoundingBox());
		}
		if (!visible)
			return;

		final FloatBuffer vertexBuffer = surroundMesh.getMeshData().getVertexBuffer();
		final FloatBuffer normalBuffer = surroundMesh.getMeshData().getNormalBuffer();
		vertexBuffer.rewind();
		normalBuffer.rewind();
		vertexBuffer.limit(vertexBuffer.capacity());
		normalBuffer.limit(normalBuffer.capacity());

		final Vector3 sideNormal = thickness.cross(0, 0, 1, null).normalizeLocal();
		if (noNeighbor0)
			addSurroundQuad(0, 1, sideNormal.negate(null), thickness, vertexBuffer, normalBuffer);
		if (noNeighbor1)
			addSurroundQuad(3, 2, sideNormal, thickness, vertexBuffer, normalBuffer);
		if (roof == null)
			addSurroundQuad(1, 3, Vector3.UNIT_Z, thickness, vertexBuffer, normalBuffer);

		vertexBuffer.limit(vertexBuffer.position());
		normalBuffer.limit(normalBuffer.position());
		surroundMesh.getMeshData().updateVertexCount();
		surroundMesh.updateModelBound();
		CollisionTreeManager.INSTANCE.removeCollisionTree(surroundMesh);
	}

	protected void addSurroundQuad(final int i1, final int i2, final ReadOnlyVector3 n, final ReadOnlyVector3 thickness, final FloatBuffer vertexBuffer, final FloatBuffer normalBuffer) {
		final ReadOnlyVector3 p1 = getAbsPoint(i1);
		final ReadOnlyVector3 p2 = p1.add(thickness, null);
		final ReadOnlyVector3 p3 = getAbsPoint(i2);
		final ReadOnlyVector3 p4 = p3.add(thickness, null);
		vertexBuffer.put(p1.getXf()).put(p1.getYf()).put(p1.getZf());
		vertexBuffer.put(p3.getXf()).put(p3.getYf()).put(p3.getZf());
		vertexBuffer.put(p4.getXf()).put(p4.getYf()).put(p4.getZf());
		vertexBuffer.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());

		for (int i = 0; i < 4; i++)
			normalBuffer.put(n.getXf()).put(n.getYf()).put(n.getZf());
	}

	private void drawWindowsSurroundMesh(final Vector3 thickness) {
		final FloatBuffer vertexBuffer = windowsSurroundMesh.getMeshData().getVertexBuffer();
		final FloatBuffer normalBuffer = windowsSurroundMesh.getMeshData().getNormalBuffer();
		vertexBuffer.rewind();
		normalBuffer.rewind();
		vertexBuffer.limit(vertexBuffer.capacity());
		normalBuffer.limit(vertexBuffer.capacity());
		final int[] order1 = new int[] { 0, 1, 3, 2, 0 };
		final int[] order2 = new int[] { 2, 3, 1, 0, 2 };
		final Vector3 sideNormal = thickness.cross(0, 0, 1, null).normalizeLocal();
		final Vector3 n = new Vector3();
		final Vector3 p = new Vector3();
		final Vector3 wallDirection = getAbsPoint(2).subtract(getAbsPoint(0), null);
		for (final HousePart child : children) {
			if (child instanceof Window && includeWindow(child)) {
				int[] order = order1;
				final Vector3 windowDirection = child.getAbsPoint(2).subtract(child.getAbsPoint(0), null);
				if (windowDirection.dot(wallDirection) < 0)
					order = order2;
				for (int index = 0; index < order.length - 1; index++) {
					int i = order[index];
					p.set(child.getAbsPoint(i));
					vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
					p.set(child.getAbsPoint(i)).addLocal(thickness);
					vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
					i = order[index + 1];
					p.set(child.getAbsPoint(i)).addLocal(thickness);
					vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
					p.set(child.getAbsPoint(i));
					vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());

					if (index == 1 || index == 3) {
						int z = 1;
						if (index == 1)
							z = -z;
						final boolean reversedThickness = getAbsPoint(1).subtract(getAbsPoint(0), null).normalizeLocal().crossLocal(wallDirection.normalize(null)).dot(thicknessNormal.normalize(null)) >= 0;
						if (!reversedThickness)
							z = -z;
						for (int j = 0; j < 4; j++)
							normalBuffer.put(0).put(0).put(z);
					} else if (index == 0 || index == 2) {
						n.set(sideNormal);
						if (index == 2)
							n.negateLocal();
						for (int j = 0; j < 4; j++)
							normalBuffer.put(n.getXf()).put(n.getYf()).put(n.getZf());
					}
				}
			}
		}
		final int pos = vertexBuffer.position();
		vertexBuffer.limit(pos != 0 ? pos : 1);

		windowsSurroundMesh.getMeshData().updateVertexCount();
		windowsSurroundMesh.updateModelBound();
	}

	public Snap getOtherSnap(final Snap snap) {
		if (snap == null && neighbors[1] != null)
			return neighbors[1];
		for (final Snap s : neighbors)
			if (s != null && !s.equals(snap))
				return s;
		return null;
	}

	private void setNeighbor(final int pointIndex, final Snap newSnap, final boolean updateNeighbors) {
		final int i = pointIndex < 2 ? 0 : 1;
		final Snap oldSnap = neighbors[i];
		neighbors[i] = newSnap;

		// if (newSnap == null && !updateNeighbors) { // see if it is attached to another wall
		// connectedWalls();
		// return;
		// }

		if (!updateNeighbors || oldSnap == newSnap || (oldSnap != null && oldSnap.equals(newSnap)))
			return;

		if (oldSnap != null) {
			final Wall neighbor = oldSnap.getNeighborOf(this);
			neighbor.setNeighbor(oldSnap.getSnapPointIndexOfNeighborOf(this), null, false);
			neighbor.draw();
		}

		if (newSnap != null) {
			final Wall neighbor = newSnap.getNeighborOf(this);
			neighbor.setNeighbor(newSnap.getSnapPointIndexOfNeighborOf(this), newSnap, false);
		}
	}

	@Override
	protected void setHeight(final double newHeight, final boolean finalize) {
		super.setHeight(newHeight, finalize);
		points.get(1).setZ(newHeight + container.height);
		if (isFirstPointInserted())
			points.get(3).setZ(newHeight + container.height);
	}

	@Override
	public void flatten(final double flattenTime) {
		final ReadOnlyVector3 n = getNormal();
		double angle = n.smallestAngleBetween(Vector3.NEG_UNIT_Y);

		if (n.dot(Vector3.UNIT_X) < 0)
			angle = -angle;

		root.setRotation((new Matrix3().fromAngles(0, 0, -flattenTime * angle)));

		super.flatten(flattenTime);

		for (final HousePart part : children)
			if (!part.isPrintable()) {
				part.getRoot().setTransform(root.getTransform());
				part.getRoot().updateGeometricState(0);
			}
	}

	@Override
	public ReadOnlyVector3 getNormal() {
		if (thicknessNormal == null)
			thicknessNormal = getThicknessNormal();
		return thicknessNormal.negate(null).normalizeLocal();
	}

	@Override
	public void drawAnnotations() {
		if (points.size() < 4)
			return;
		final ReadOnlyVector3 faceDirection = getNormal();
		int annotCounter = 0;
		int angleAnnotCounter = 0;

		if (wallAndWindowsPoints != null) {
			final List<Vector3> wallPolygonPoints = wallAndWindowsPoints.get(0);
			final Vector3 actualNormal = wallPolygonPoints.get(0).subtract(wallPolygonPoints.get(1), null).normalizeLocal().crossLocal(wallPolygonPoints.get(2).subtract(wallPolygonPoints.get(1), null).normalizeLocal()).negateLocal();
			final boolean reverse = actualNormal.dot(getNormal()) < 0;

			final double lowestWallZ = Math.min(wallPolygonPoints.get(0).getZ(), wallPolygonPoints.get(3).getZ());
			double low = lowestWallZ;
			double hi = Math.max(wallPolygonPoints.get(0).getZ(), wallPolygonPoints.get(3).getZ());
			for (int i = 4; i < wallPolygonPoints.size(); i++) {
				if (wallPolygonPoints.get(i).getZ() < low)
					low = wallPolygonPoints.get(i).getZ();
				if (wallPolygonPoints.get(i).getZ() > hi)
					hi = wallPolygonPoints.get(i).getZ();
			}

			final float lineWidth = original == null ? 1f : 2f;
			final boolean isRectangular = hi - low < 0.1;
			;

			if (isRectangular) {
				final ReadOnlyVector3 p1 = wallPolygonPoints.get(0).multiply(new Vector3(1, 1, 0), null).addLocal(0, 0, lowestWallZ);
				final ReadOnlyVector3 p2 = wallPolygonPoints.get(1);
				final ReadOnlyVector3 p3 = wallPolygonPoints.get(2);
				final ReadOnlyVector3 p4 = wallPolygonPoints.get(3).multiply(new Vector3(1, 1, 0), null).addLocal(0, 0, lowestWallZ);

				final boolean front = false;

				fetchSizeAnnot(annotCounter++).setRange(p1, p2, getCenter(), faceDirection, front, front ? Align.South : Align.Center, true, reverse, Scene.isDrawAnnotationsInside());
				fetchSizeAnnot(annotCounter++).setRange(p2, p3, getCenter(), faceDirection, original == null, original == null ? Align.South : Align.Center, true, reverse, Scene.isDrawAnnotationsInside());
				fetchSizeAnnot(annotCounter++).setRange(p3, p4, getCenter(), faceDirection, front, front ? Align.South : Align.Center, true, reverse, Scene.isDrawAnnotationsInside());
				fetchSizeAnnot(annotCounter++).setRange(p4, p1, getCenter(), faceDirection, front, front ? Align.South : Align.Center, true, reverse, Scene.isDrawAnnotationsInside());

				for (int i = 0; i < annotCounter; i++)
					fetchSizeAnnot(i).setLineWidth(lineWidth);

				fetchAngleAnnot(angleAnnotCounter++).setRange(p2, p1, p3, getNormal());
				fetchAngleAnnot(angleAnnotCounter++).setRange(p3, p2, p4, getNormal());
				fetchAngleAnnot(angleAnnotCounter++).setRange(p4, p3, p1, getNormal());
				fetchAngleAnnot(angleAnnotCounter++).setRange(p1, p4, p2, getNormal());

				for (int i = 0; i < annotCounter; i++)
					fetchAngleAnnot(i).setLineWidth(lineWidth);

			} else
				for (int i = 0; i < wallPolygonPoints.size(); i++) {
					final boolean front = i == 1 && original == null;
					final ReadOnlyVector3 p1 = wallPolygonPoints.get(i);
					final ReadOnlyVector3 p2 = wallPolygonPoints.get((i + 1) % wallPolygonPoints.size());
					final ReadOnlyVector3 p3 = wallPolygonPoints.get((i + 2) % wallPolygonPoints.size());
					final double minLength = 4.0;
					if (p1.distance(p2) > minLength) {
						final ReadOnlyVector3 min = new Vector3(Math.min(p1.getX(), Math.min(p2.getX(), p3.getX())), Math.min(p1.getY(), Math.min(p2.getY(), p3.getY())), 0);
						final ReadOnlyVector3 max = new Vector3(Math.max(p1.getX(), Math.max(p2.getX(), p3.getX())), Math.max(p1.getY(), Math.max(p2.getY(), p3.getY())), 0);
						final ReadOnlyVector3 center = min.add(max, null).divideLocal(2.0).addLocal(0, 0, getCenter().getZ());
						final SizeAnnotation sizeAnnot = fetchSizeAnnot(annotCounter++);
						sizeAnnot.setRange(p1, p2, center, faceDirection, front, front ? Align.South : Align.Center, true, reverse, Scene.isDrawAnnotationsInside());
						sizeAnnot.setLineWidth(lineWidth);
					}
					if (p1.distance(p2) > minLength && p2.distance(p3) > minLength) {
						final AngleAnnotation angleAnnot = fetchAngleAnnot(angleAnnotCounter++);
						angleAnnot.setRange(p2, p1, p3, getNormal());
						angleAnnot.setLineWidth(lineWidth);
					}
				}
		}
	}

	public void visitNeighbors(final WallVisitor visitor) {
		Wall currentWall = this;
		Snap snap = null;
		Wall.clearVisits();
		while (currentWall != null && !currentWall.isVisited()) {
			currentWall.visit();
			snap = currentWall.getOtherSnap(snap);
			if (snap == null)
				break;
			currentWall = snap.getNeighborOf(currentWall);
		}

		visitNeighborsForward(currentWall, null, visitor);
	}

	public void visitNeighborsForward(Wall currentWall, Snap snap, final WallVisitor visitor) {
		class VisitInfo {
			Wall wall;
			Snap prev, next;

			VisitInfo(final Wall wall, final Snap prev, final Snap next) {
				this.wall = wall;
				this.prev = prev;
				this.next = next;
			}
		}
		final ArrayList<VisitInfo> visits = new ArrayList<VisitInfo>();
		Wall.clearVisits();
		while (currentWall != null && !currentWall.isVisited()) {
			final Snap prevSnap = snap;
			snap = currentWall.getOtherSnap(snap);
			visits.add(new VisitInfo(currentWall, prevSnap, snap));
			currentWall.visit();
			if (snap == null)
				break;
			currentWall = snap.getNeighborOf(currentWall);
		}
		for (final VisitInfo visit : visits)
			visitor.visit(visit.wall, visit.prev, visit.next);
	}

	public void computeInsideDirectionOfAttachedWalls(final boolean drawNeighborWalls) {
		if (this.thicknessNormal != null)
			return;

		final ArrayList<Wall> walls;
		if (drawNeighborWalls)
			walls = new ArrayList<Wall>();
		else
			walls = null;

		final double[] side = new double[] { 0.0 };

		Wall.clearVisits();
		visitNeighbors(new WallVisitor() {
			@Override
			public void visit(final Wall wall, final Snap prev, final Snap next) {
				if (next != null) {
					final int indexP2 = next.getSnapPointIndexOf(wall);
					final ReadOnlyVector3 p1 = wall.getAbsPoint(indexP2 == 0 ? 2 : 0);
					final ReadOnlyVector3 p2 = wall.getAbsPoint(indexP2);
					final ReadOnlyVector3 p3 = next.getNeighborOf(wall).getAbsPoint(next.getSnapPointIndexOfNeighborOf(wall) == 0 ? 2 : 0);
					final ReadOnlyVector3 p1_p2 = p2.subtract(p1, null).normalizeLocal();
					final ReadOnlyVector3 p2_p3 = p3.subtract(p2, null).normalizeLocal();
					side[0] += Util.angleBetween(p1_p2, p2_p3, Vector3.UNIT_Z);
				}
				if (drawNeighborWalls && wall != Wall.this && !walls.contains(wall))
					walls.add(wall);
			}
		});

		Wall.clearVisits();
		visitNeighbors(new WallVisitor() {
			@Override
			public void visit(final Wall wall, final Snap prev, final Snap next) {
				if (next != null) {
					final int indexP2 = next.getSnapPointIndexOf(wall);
					final Vector3 p1 = wall.getAbsPoint(indexP2 == 0 ? 2 : 0);
					final Vector3 p2 = wall.getAbsPoint(indexP2);
					final Vector3 p1_p2 = p2.subtract(p1, null);
					wall.thicknessNormal = p1_p2.cross(Vector3.UNIT_Z, null).normalizeLocal().multiplyLocal(wallThickness);
					if (side[0] > 0)
						wall.thicknessNormal.negateLocal();
				} else if (prev != null) {
					final int indexP2 = prev.getSnapPointIndexOf(wall);
					final Vector3 p2 = wall.getAbsPoint(indexP2);
					final Vector3 p3 = wall.getAbsPoint(indexP2 == 0 ? 2 : 0);
					final Vector3 p2_p3 = p3.subtract(p2, null);
					wall.thicknessNormal = p2_p3.cross(Vector3.UNIT_Z, null).normalizeLocal().multiplyLocal(wallThickness);
					if (side[0] > 0)
						wall.thicknessNormal.negateLocal();
				}
			}
		});

		if (drawNeighborWalls)
			for (final HousePart wall : walls) {
				wall.draw();
				wall.drawChildren();
			}
	}

	@Override
	protected String getTextureFileName() {
		return Scene.getInstance().getTextureMode() == TextureMode.Simple ? "wall.png" : "wall_brick.png";
	}

	public boolean isVisited() {
		return visitStamp == currentVisitStamp;
	}

	public void visit() {
		visitStamp = currentVisitStamp;
	}

	public void setRoof(final Roof roof) {
		this.roof = roof;
	}

	@Override
	public void setOriginal(final HousePart original) {
		final Wall originalWall = (Wall) original;
		this.thicknessNormal = originalWall.getThicknessNormal();
		root.detachChild(invisibleMesh);
		root.detachChild(backMesh);
		root.detachChild(surroundMesh);
		root.detachChild(windowsSurroundMesh);
		root.detachChild(outlineMesh);
		backMesh = originalWall.backMesh.makeCopy(true);
		surroundMesh = originalWall.surroundMesh.makeCopy(true);
		windowsSurroundMesh = originalWall.windowsSurroundMesh.makeCopy(true);
		outlineMesh = originalWall.outlineMesh.makeCopy(true);
		((Line) outlineMesh).setLineWidth(printOutlineThickness);
		root.attachChild(backMesh);
		root.attachChild(surroundMesh);
		root.attachChild(windowsSurroundMesh);
		root.attachChild(outlineMesh);

		final Mesh orgInvisibleMesh = originalWall.invisibleMesh;
		invisibleMesh = orgInvisibleMesh.makeCopy(true);
		invisibleMesh.setUserData(new UserData(this, ((UserData) orgInvisibleMesh.getUserData()).getIndex(), false));
		root.attachChild(invisibleMesh);
		wallAndWindowsPoints = originalWall.wallAndWindowsPoints;

		super.setOriginal(original);
	}

	public Roof getRoof() {
		return roof;
	}

	@Override
	public void drawGrids(final double gridSize) {
		final ReadOnlyVector3 p0 = getAbsPoint(0);
		final ReadOnlyVector3 p2 = getAbsPoint(2);
		final ReadOnlyVector3 width = p2.subtract(p0, null);
		final ArrayList<ReadOnlyVector3> points = new ArrayList<ReadOnlyVector3>();

		final int cols = (int) (width.length() / gridSize);

		double gableHeight = height;
		ReadOnlyVector3 gablePeakBase = p0;
		for (int col = 1; col < cols; col++) {
			final ReadOnlyVector3 lineP1 = width.normalize(null).multiplyLocal(col * gridSize).addLocal(p0);
			points.add(lineP1);
			final ReadOnlyVector3 lineP2 = findRoofIntersection(new Vector3(lineP1.getX(), lineP1.getY(), height), Vector3.UNIT_Z, 0);
			points.add(lineP2);
			if (lineP2.getZ() > gableHeight) {
				gableHeight = lineP2.getZ();
				gablePeakBase = lineP1;
			}
		}

		final ReadOnlyVector3 height = getAbsPoint(1).subtractLocal(p0).normalizeLocal().multiplyLocal(gableHeight);
		final int rows = (int) (gableHeight / gridSize);

		for (int row = 1; row < rows; row++) {
			final ReadOnlyVector3 pMiddle = height.normalize(null).multiplyLocal(row * gridSize).addLocal(gablePeakBase);
			ReadOnlyVector3 lineP1 = new Vector3(p0.getX(), p0.getY(), pMiddle.getZ());
			ReadOnlyVector3 lineP2 = new Vector3(p2.getX(), p2.getY(), pMiddle.getZ());
			if (pMiddle.getZ() > this.height) {
				ReadOnlyVector3 tmp;
				tmp = findRoofIntersection(pMiddle, width.normalize(null), 0);
				if (tmp != pMiddle)
					lineP1 = tmp;
				tmp = findRoofIntersection(pMiddle, width.normalize(null).negateLocal(), 0);
				if (tmp != pMiddle)
					lineP2 = tmp;
			}
			points.add(lineP1);
			points.add(lineP2);
		}
		if (points.size() < 2)
			return;
		final FloatBuffer buf = BufferUtils.createVector3Buffer(points.size());
		for (final ReadOnlyVector3 p : points)
			buf.put(p.getXf()).put(p.getYf()).put(p.getZf());
		gridsMesh.getMeshData().setVertexBuffer(buf);
	}

	@Override
	public void reset() {
		super.reset();
		if (root == null)
			init();
		thicknessNormal = null;
		neighbors[0] = neighbors[1] = null;
	}

	public void setBackMeshesVisible(final boolean visible) {
		backMesh.setVisible(visible);
		surroundMesh.setVisible(visible);
		windowsSurroundMesh.setVisible(visible);
	}

	@Override
	public void updateTextureAndColor() {
		updateTextureAndColor(mesh, getColor() == null ? Scene.getInstance().getWallColor() : getColor());
	}

	public void connectedWalls() {
		if (!isDrawable() || (neighbors[0] != null && neighbors[1] != null))
			return;
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Wall && part != this && part.isDrawCompleted()) {
				final Wall otherWall = (Wall) part;
				for (int index = 0; index < 2; index++)
					if (neighbors[index] == null)
						for (int otherIndex = 0; otherIndex < 2; otherIndex++) {
							// if ((otherWall.neighbors[otherIndex] == null || otherWall.neighbors[otherIndex].getNeighborOf(otherWall) == this) && Util.isEqual(otherWall.getAbsPoint(otherIndex * 2), getAbsPoint(index * 2))) {
							if (otherWall.neighbors[otherIndex] == null && Util.isEqual(otherWall.getAbsPoint(otherIndex * 2), getAbsPoint(index * 2))) {
								setNeighbor(index * 2, new Snap(this, otherWall, index * 2, otherIndex * 2), true);
								break;
							}
						}
			}
		}
	}

	public boolean windowsFit() {
		for (final HousePart part : children)
			if (part instanceof Window)
				if (!fits(part))
					return false;
		return true;
	}

	@Override
	public boolean fits(final HousePart window) {
		final List<Vector3> hole = computeWindowHole(window, Vector3.ZERO);
		applyXYTransform(hole);
		final double minDistanceToRoof = 0.1 * getGridSize();
		final ArrayList<Vector3> polygon = new ArrayList<Vector3>(wallAndWindowsPoints.get(0).size());
		for (int i = 0; i < wallAndWindowsPoints.get(0).size(); i++) {
			final Vector3 p = wallAndWindowsPoints.get(0).get(i).clone();
			if (i == 0 || i > 2)
				p.subtractLocal(0, 0, minDistanceToRoof);
			polygon.add(p);
		}
		applyXYTransform(polygon);
		for (final Vector3 p : hole)
			if (!Util.insidePolygon(p, polygon))
				return false;
		return true;
	}

	public void applyXYTransform(final List<Vector3> hole) {
		for (final Vector3 p : hole) {
			final Point point = new ArdorVector3Point(p);
			toXY.transform(point);
			p.set(point.getX(), point.getY(), point.getZ());
		}
	}

	@Override
	protected void computeArea() {
		area = Util.computeArea(mesh);
	}

	@Override
	public double getArea() {
		double areaWithoutWindows = area;
		for (final HousePart child : children)
			if (child instanceof Window || child instanceof Door)
				areaWithoutWindows -= child.getArea();
		if (areaWithoutWindows < 0)
			areaWithoutWindows = 0;
		return areaWithoutWindows;
	}

	@Override
	protected boolean isHorizontal() {
		return false;
	}

	public List<Vector3> getWallPolygonPoints() {
		return wallAndWindowsPoints.get(0);
	}

	@Override
	public Spatial getCollisionSpatial() {
		if (SceneManager.getInstance().isTopView())
			return root;
		else
			return invisibleMesh;
	}

	@Override
	public void drawHeatFlux() {
		if (type != SOLID_WALL)
			return;
		double zmax = -Double.MAX_VALUE;
		final List<Vector3> wallPolygonPoints = getWallPolygonPoints();
		for (final Vector3 a : wallPolygonPoints) {
			if (a.getZ() > zmax)
				zmax = a.getZ();
		}

		final Path2D.Double path = new Path2D.Double();
		path.moveTo(0, 0);
		final Vector3 v1 = new Vector3();
		final Vector3 v2 = new Vector3();
		wallPolygonPoints.get(1).subtract(wallPolygonPoints.get(0), v1);
		wallPolygonPoints.get(2).subtract(wallPolygonPoints.get(0), v2);
		if (Util.isZero(v1.getX()) && Util.isZero(v2.getX())) {
			path.moveTo(v1.getY(), v1.getZ());
			path.lineTo(v2.getY(), v2.getZ());
			for (int i = 3; i < wallPolygonPoints.size(); i++) {
				wallPolygonPoints.get(i).subtract(wallPolygonPoints.get(0), v2);
				path.lineTo(v2.getY(), v2.getZ());
			}
		} else { // always use the Y plane unless it is a X plane as above
			path.moveTo(v1.getX(), v1.getZ());
			path.lineTo(v2.getX(), v2.getZ());
			for (int i = 3; i < wallPolygonPoints.size(); i++) {
				wallPolygonPoints.get(i).subtract(wallPolygonPoints.get(0), v2);
				path.lineTo(v2.getX(), v2.getZ());
			}
		}
		path.lineTo(0, 0);
		path.closePath();

		heatFlux.getSceneHints().setCullHint(CullHint.Inherit);

		FloatBuffer arrowsVertices = heatFlux.getMeshData().getVertexBuffer();
		final int cols = (int) Math.max(2, getAbsPoint(0).distance(getAbsPoint(2)) / heatFluxUnitArea);
		final int rows = (int) Math.max(2, zmax / heatFluxUnitArea);
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
				for (int i = 0; i < rows - 1; i++) {
					g = i + 0.5;
					a.setX(o.getX() + g * v.getX() / rows + h * u.getX() / cols);
					a.setY(o.getY() + g * v.getY() / rows + h * u.getY() / cols);
					a.setZ(o.getZ() + g * zmax / rows);
					a.subtract(wallPolygonPoints.get(0), v1);
					if (Util.isZero(v1.getX())) {
						if (!path.contains(v1.getY(), v1.getZ()))
							break;
					} else {
						if (!path.contains(v1.getX(), v1.getZ()))
							break;
					}
					drawArrow(a, normal, arrowsVertices, heat);
				}
			}
			heatFlux.getMeshData().updateVertexCount();
			heatFlux.updateModelBound();
		}

		updateHeatFluxVisibility();
	}

	@Override
	public void delete() {
		if (roof != null) {
			roof.remove(this);
		}
	}

	public Snap[] getNeighbors() {
		return neighbors;
	}

	@Override
	public boolean isCopyable() {
		return false;
	}

	@Override
	public void setUValue(final double uValue) {
		this.uValue = uValue;
	}

	@Override
	public double getUValue() {
		return uValue;
	}

	@Override
	public void setVolumetricHeatCapacity(final double volumetricHeatCapacity) {
		this.volumetricHeatCapacity = volumetricHeatCapacity;
	}

	@Override
	public double getVolumetricHeatCapacity() {
		return volumetricHeatCapacity;
	}

	public void setType(final int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	@Override
	public boolean isValid() {
		if (!super.isValid())
			return false;
		return super.isDrawable();
	}

}