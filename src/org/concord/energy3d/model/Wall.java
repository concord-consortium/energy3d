package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.AngleAnnotation;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.util.SelectUtil;
import org.concord.energy3d.util.Util;
import org.concord.energy3d.util.WallVisitor;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.transform.coordinate.AnyToXYTransform;
import org.poly2tri.transform.coordinate.XYToAnyTransform;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.point.ardor3d.ArdorVector3PolygonPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.OrientedBoundingBox;
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
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.OffsetState;
import com.ardor3d.renderer.state.OffsetState.OffsetType;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.geom.BufferUtils;

public class Wall extends HousePart {
	private static final long serialVersionUID = 1L;
	// private static final double getGridSize() = 0.5;
	// private static final double MIN_WALL_LENGTH = 0.1;
	private static final double MIN_WALL_LENGTH = 0.01;
	private static double defaultWallHeight = 2.0; // the recommended default wall height is 20cm
	private static int currentVisitStamp = 1;
	private transient Mesh backMesh;
	private transient Mesh surroundMesh;
	private transient Mesh invisibleMesh;
	private transient Mesh windowsSurroundMesh;
	private transient Mesh wireframeMesh;
	// private transient Line gridsMesh;
	private transient Roof roof;
	private transient ArrayList<Vector3> wallPolygonPoints;
	private transient int visitStamp;
	private final double wallThickness = 0.05;
	private final Snap[] neighbors = new Snap[2];
	private Vector3 thicknessNormal;
	private boolean isShortWall;

	public static void clearVisits() {
		currentVisitStamp = ++currentVisitStamp % 1000;
	}

	public Wall() {
		super(2, 4, defaultWallHeight);
	}

	@Override
	protected boolean mustHaveContainer() {
		return false;
	}

	@Override
	protected void init() {
		super.init();
		relativeToHorizontal = true;

		mesh = new Mesh("Wall");
		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		mesh.getMeshData().setTextureBuffer(BufferUtils.createVector2Buffer(4), 0);
		mesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		mesh.setRenderState(ms);
		final OffsetState offsetState = new OffsetState();
		offsetState.setTypeEnabled(OffsetType.Fill, true);
		offsetState.setFactor(1f);
		offsetState.setUnits(1f);
		mesh.setRenderState(offsetState);
		mesh.setModelBound(new OrientedBoundingBox());
		root.attachChild(mesh);

		backMesh = new Mesh("Wall (Back)");
		backMesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		backMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		backMesh.getMeshData().setTextureBuffer(BufferUtils.createVector2Buffer(4), 0);
		backMesh.setDefaultColor(ColorRGBA.LIGHT_GRAY);
		backMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		backMesh.setModelBound(new BoundingBox());
		root.attachChild(backMesh);

		surroundMesh = new Mesh("Wall (Surround)");
		surroundMesh.getMeshData().setIndexMode(IndexMode.Quads);
		surroundMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(12));
		surroundMesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(12));
		surroundMesh.setDefaultColor(ColorRGBA.GRAY);
//		surroundMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		surroundMesh.setModelBound(new BoundingBox());
		root.attachChild(surroundMesh);

		invisibleMesh = new Mesh("Wall (Invisible)");
		invisibleMesh.getMeshData().setIndexMode(IndexMode.Quads);
		invisibleMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		invisibleMesh.getSceneHints().setCullHint(CullHint.Always);
		invisibleMesh.setModelBound(new BoundingBox());
		root.attachChild(invisibleMesh);

		windowsSurroundMesh = new Mesh("Wall (Windows Surround)");
		windowsSurroundMesh.getMeshData().setIndexMode(IndexMode.Quads);
		windowsSurroundMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(1000));
		windowsSurroundMesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(1000));
		windowsSurroundMesh.setDefaultColor(ColorRGBA.GRAY);
		windowsSurroundMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		// windowsSurroundMesh.setModelBound(new BoundingBox());
		/* lets not use bounds for this mesh because when there are no windows its bounds is set to center 0,0,0 which shifts the overall bounds toward zero */
		windowsSurroundMesh.setModelBound(null);
		root.attachChild(windowsSurroundMesh);

		wireframeMesh = new Line("Wall (Wireframe)");
		wireframeMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(8));
		wireframeMesh.setDefaultColor(ColorRGBA.BLACK);
		wireframeMesh.setModelBound(new BoundingBox());
		Util.disablePickShadowLight(wireframeMesh);
		root.attachChild(wireframeMesh);

		// gridsMesh = new Line("Wall (Grids)");
		// gridsMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(2));
		// gridsMesh.setDefaultColor(ColorRGBA.BLUE);
		// gridsMesh.setModelBound(new BoundingBox());
		// Util.disablePickShadowLight(gridsMesh);
		// root.attachChild(gridsMesh);
		// setGridsVisible(false);

		updateTextureAndColor(Scene.getInstance().isTextureEnabled());

		final UserData userData = new UserData(this);
		mesh.setUserData(userData);
		backMesh.setUserData(userData);
		surroundMesh.setUserData(userData);
		invisibleMesh.setUserData(userData);
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		Snap.clearAnnotationDrawn();
		if (editPointIndex == -1 || editPointIndex == 0 || editPointIndex == 2) {
			final HousePart previousContainer = container;
			final PickedHousePart picked = pick(x, y, new Class<?>[] { Foundation.class, null });
			if (container != previousContainer && previousContainer != null)
				return;
			if (container != previousContainer)
				for (int i = 0; i < points.size(); i++) {
					final Vector3 p = points.get(i);
					p.setX(MathUtils.clamp(p.getX(), 0, 1));
					p.setY(MathUtils.clamp(p.getY(), 0, 1));
					if (i == 0 || i == 2)
						p.setZ(container.height);
					else
						p.setZ(container.height + height);
				}
			Vector3 p = null;
			if (picked != null) {
				p = picked.getPoint();
			} else {
				final PickedHousePart floorPick = SelectUtil.pickPart(x, y, (Spatial) null);
				if (floorPick != null) {
					p = floorPick.getPoint();
					ReadOnlyVector3 closesPoint = container.points.get(0);
					for (int i = 1; i < 4; i++)
						if (closesPoint.distance(p) > container.points.get(i).distance(p))
							closesPoint = container.points.get(i);
					ReadOnlyVector3 secondClosesPoint = closesPoint == container.points.get(0) ? container.points.get(1) : container.points.get(0);
					for (int i = 0; i < 4; i++)
						if (secondClosesPoint.distance(p) > container.points.get(i).distance(p) && container.points.get(i) != closesPoint)
							secondClosesPoint = container.points.get(i);
					final Vector3 dir = closesPoint.subtract(secondClosesPoint, null).normalizeLocal();
					p = closestPoint(closesPoint, dir, p, Vector3.NEG_UNIT_Z);
					p = grid(p, getGridSize());
					p = closestPoint(closesPoint, dir, p, Vector3.NEG_UNIT_Z);
					p.setX(MathUtils.clamp(p.getX(), Math.min(container.points.get(0).getX(), container.points.get(2).getX()), Math.max(container.points.get(0).getX(), container.points.get(2).getX())));
					p.setY(MathUtils.clamp(p.getY(), Math.min(container.points.get(0).getY(), container.points.get(1).getY()), Math.max(container.points.get(0).getY(), container.points.get(1).getY())));
					p.getZ();
				}
			}
			if (p == null)
				return;
			if (container != null)
				p.setZ(container.height);
			final int index = (editPointIndex == -1) ? points.size() - 2 : editPointIndex;
//			final Vector3 p_snap = new Vector3(p);
//			Snap snap = snapWall(p_snap, index);
//			if (snap != null && (!isFirstPointInserted() || p_snap.subtract(getAbsPoint(index == 0 ? 2 : 0), null).length() > MIN_WALL_LENGTH))
//			final boolean snapWall = snapWall(p_snap, index);
			boolean snapWall = snapWall(p, index);
//			if (!snapWall && (!isFirstPointInserted() || p_snap.subtract(getAbsPoint(index == 0 ? 2 : 0), null).length() > MIN_WALL_LENGTH))
//				p.set(p_snap);
//			else
//				snap = null;

//			if (snap == null) {
			if (!snapWall) {
			p = grid(p, getGridSize(), false);
			snapWall = snapWall(p, index);		// see if it can be snapped after grid move
			}

			if (!snapWall) {
				final boolean foundationSnap = snapFoundation(p);
				if (!foundationSnap) {
				}
			}
//			setNeighbor(index, snap, true);
			if (index == 2) // make sure z of 2nd base point is same as 2st (needed for platform picking side)
				p.setZ(points.get(0).getZ());
			final Vector3 p_rel = toRelative(p);
			points.get(index).set(p_rel);
			points.get(index + 1).set(p_rel).setZ(p.getZ() + height);
		} else if (editPointIndex == 1 || editPointIndex == 3) {
			final int lower = (editPointIndex == 1) ? 0 : 2;
			final Vector3 base = getAbsPoint(lower);
			Vector3 closestPoint = closestPoint(base, Vector3.UNIT_Z, x, y);
//			Snap snap = snapWall(closestPoint, -1);
			final boolean snapWall = snapWall(closestPoint, lower);
			if (!snapWall)
				closestPoint = grid(closestPoint, getGridSize());
			defaultWallHeight = height = Math.max(0.1, closestPoint.getZ() - base.getZ());
			final double z = height + base.getZ();
			points.get(1).setZ(z);
			points.get(3).setZ(z);
		}

		thicknessNormal = null;
		isShortWall = true;
		draw();
		drawChildren();
		setEditPointsVisible(true);

		if (isDrawable())
			drawNeighborWalls();
	}

	protected boolean snapWall(final Vector3 p, final int index) {
//		if (!isSnapToObjects())
//			return null;
		ReadOnlyVector3 closestPoint = null;
		double closestDistance = Double.MAX_VALUE;
		Wall closestWall = null;
		int closestPointIndex = -1;
		for (final HousePart housePart : Scene.getInstance().getParts()) {
			if (housePart instanceof Wall && housePart != this) {
				final Wall wall = (Wall) housePart;
				int i = 0;
				for (int j = 0; j < wall.points.size(); j++) {
					final ReadOnlyVector3 p2 = wall.getAbsPoint(j);

					final double distance = p.distance(p2);
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
		if (closestDistance < (isSnapToObjects() ? SNAP_DISTANCE : getGridSize()) && (!isFirstPointInserted() || p.subtract(getAbsPoint(index == 0 ? 2 : 0), null).length() > MIN_WALL_LENGTH)) {
			p.set(closestPoint);
			setNeighbor(index, new Snap(this, closestWall, index, closestPointIndex), true);
			return true;
		} else {
			setNeighbor(index, null, true);
			return false;
		}
	}

	private boolean snapFoundation(final Vector3 current) {
//		if (!isSnapToObjects() || container == null)
		if (container == null)
			return false;
		ReadOnlyVector3 snapPoint = null;
		double snapDistance = Double.MAX_VALUE;
//		for (int i = 0; i < container.points.size(); i++) {
		final int[] indices = new int[] {0, 2, 3, 1, 0};
		for (int i = 0; i < indices.length - 1; i++) {
//			final ReadOnlyVector3 p = container.getAbsPoint(i);
			final Vector3 p = Util.getClosetPoint(container.getAbsPoint(indices[i]).addLocal(0, 0, current.getZ()), container.getAbsPoint(indices[i+1]).addLocal(0, 0, current.getZ()), current, true);
			final double d = p.distance(current);
			if (d < snapDistance) {
				snapDistance = d;
				snapPoint = p;
			}
		}

//		if (snapDistance < (isSnapToObjects() ? SNAP_DISTANCE : getGridSize())) {
		if (snapDistance < getGridSize() / 2) {
			current.set(snapPoint.getX(), snapPoint.getY(), current.getZ());
			return true;
		} else
			return false;
	}

	@Override
	public boolean isDrawable() {
		return points.size() >= 4 && points.get(0).subtract(points.get(2), null).length() > MIN_WALL_LENGTH;
	}

	@Override
	protected void drawMesh() {
		final CullHint cull = isDrawable() ? CullHint.Inherit : CullHint.Always;
		mesh.getSceneHints().setCullHint(cull);
		backMesh.getSceneHints().setCullHint(cull);
		surroundMesh.getSceneHints().setCullHint(cull);
		windowsSurroundMesh.getSceneHints().setCullHint(cull);
		wireframeMesh.getSceneHints().setCullHint(cull);

		if (!isDrawable())
			return;

		final Vector3 normal = computeNormal();
		final Polygon polygon = stretchToRoof(computeWallAndWindowPolygon(false));

		wallPolygonPoints = extractPolygonPoints(polygon);

		drawWireframe(wallPolygonPoints, extractPolygonHoles(polygon));

		toXY(polygon);

		final AnyToXYTransform toXY = new AnyToXYTransform(normal.getX(), normal.getY(), normal.getZ());
		final XYToAnyTransform fromXY = new XYToAnyTransform(normal.getX(), normal.getY(), normal.getZ());

		final Vector3 p01 = getAbsPoint(1).subtract(getAbsPoint(0), null).normalizeLocal();
		final Vector3 p02 = getAbsPoint(2).subtract(getAbsPoint(0), null).normalizeLocal();
		final TPoint o = new TPoint(getAbsPoint(0).getX(), getAbsPoint(0).getY(), getAbsPoint(0).getZ());
		final TPoint u = new TPoint(p01.getX(), p01.getY(), p01.getZ());
		final TPoint v = new TPoint(p02.getX(), p02.getY(), p02.getZ());

		toXY.transform(o);
		toXY.transform(u);
		toXY.transform(v);

//		try {
			if (Scene.getInstance().isDrawThickness() && isShortWall) {
				final Vector3 dir = getAbsPoint(2).subtract(getAbsPoint(0), null).normalizeLocal();

				if (neighbors[0] != null && neighbors[0].getNeighborOf(this).isFirstPointInserted()) {
					if (isPerpendicularToNeighbor(0))
						reduceBackMeshWidth(polygon, dir, 0);
				}

				if (neighbors[1] != null && neighbors[1].getNeighborOf(this).isFirstPointInserted()) {
					dir.normalizeLocal().negateLocal();
					if (isPerpendicularToNeighbor(1))
						reduceBackMeshWidth(polygon, dir, 1);
				}
			}

			Poly2Tri.triangulate(polygon);
			ArdorMeshMapper.updateTriangleMesh(mesh, polygon, fromXY);
			ArdorMeshMapper.updateVertexNormals(mesh, polygon.getTriangles(), fromXY);
			ArdorMeshMapper.updateTextureCoordinates(mesh, polygon.getTriangles(), 1, o, u, v);
			mesh.getMeshData().updateVertexCount();
			mesh.updateModelBound();

			final Polygon invisiblePolygon = new Polygon(ArdorVector3PolygonPoint.toPoints(extractPolygonPoints(polygon)));
			Poly2Tri.triangulate(invisiblePolygon);
			ArdorMeshMapper.updateTriangleMesh(invisibleMesh, invisiblePolygon, fromXY);
			invisibleMesh.getMeshData().updateVertexCount();
			invisibleMesh.updateModelBound();

			drawBackMesh(computeWallAndWindowPolygon(true), fromXY);
			drawSurroundMesh(thicknessNormal);
			drawWindowsSurroundMesh(thicknessNormal);

			backMesh.updateModelBound();
			surroundMesh.updateModelBound();
			windowsSurroundMesh.updateModelBound();
			wireframeMesh.updateModelBound();
			invisibleMesh.updateModelBound();

			root.updateWorldBound(true);
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
	}

	private void drawWireframe(final ArrayList<? extends ReadOnlyVector3> wallPolygonPoints, final ArrayList<ReadOnlyVector3> wallPolygonHoles) {
		FloatBuffer wireframeVertexBuffer = wireframeMesh.getMeshData().getVertexBuffer();
		final int requiredSize = 2 * (wallPolygonPoints.size() + wallPolygonHoles.size());
		if (wireframeVertexBuffer.capacity() / 3 < requiredSize) {
			wireframeVertexBuffer = BufferUtils.createVector3Buffer(requiredSize);
			wireframeMesh.getMeshData().setVertexBuffer(wireframeVertexBuffer);
		} else {
			wireframeVertexBuffer.rewind();
			wireframeVertexBuffer.limit(wireframeVertexBuffer.capacity());
		}
		wireframeVertexBuffer.rewind();

		ReadOnlyVector3 prev = wallPolygonPoints.get(wallPolygonPoints.size() - 1);
		for (final ReadOnlyVector3 point : wallPolygonPoints) {
			wireframeVertexBuffer.put(prev.getXf()).put(prev.getYf()).put(prev.getZf());
			prev = point;
			wireframeVertexBuffer.put(point.getXf()).put(point.getYf()).put(point.getZf());
		}

		for (int i = 0; i < wallPolygonHoles.size() / 4; i++) {
			prev = wallPolygonHoles.get(i * 4 + 3);
			for (int j = 0; j < 4; j++) {
				final ReadOnlyVector3 point = wallPolygonHoles.get(i * 4 + j);
				wireframeVertexBuffer.put(prev.getXf()).put(prev.getYf()).put(prev.getZf());
				prev = point;
				wireframeVertexBuffer.put(point.getXf()).put(point.getYf()).put(point.getZf());
			}
		}
		wireframeVertexBuffer.limit(wireframeVertexBuffer.position());
		wireframeMesh.getMeshData().updateVertexCount();
	}

	public Polygon computeWallAndWindowPolygon(final boolean backMesh) {
		final ArrayList<PolygonPoint> polygonPoints = new ArrayList<PolygonPoint>();
		final ReadOnlyVector3 trans = backMesh ? getThicknessNormal() : Vector3.ZERO;
		Vector3 p = new Vector3();
		// Start the polygon with (1) then 0, 2, 3, [roof points] so that roof points are appended to the end of vertex list
		p.set(getAbsPoint(1)).addLocal(trans);
		polygonPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
		p.set(getAbsPoint(0)).addLocal(trans);
		polygonPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
		p.set(getAbsPoint(2)).addLocal(trans);
		polygonPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
		p.set(getAbsPoint(3)).addLocal(trans);
		polygonPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));

		final Polygon polygon = new Polygon(polygonPoints);

		// Add window holes
		for (final HousePart child : children) {
			if (child instanceof Window && includeWindow(child)) {
				final ArrayList<PolygonPoint> holePoints = new ArrayList<PolygonPoint>();
				p = child.getAbsPoint(1);
				holePoints.add(new PolygonPoint(p.getX() + trans.getX(), p.getY() + trans.getY(), p.getZ()));
				p = child.getAbsPoint(0);
				holePoints.add(new PolygonPoint(p.getX() + trans.getX(), p.getY() + trans.getY(), p.getZ()));
				p = child.getAbsPoint(2);
				holePoints.add(new PolygonPoint(p.getX() + trans.getX(), p.getY() + trans.getY(), p.getZ()));
				p = child.getAbsPoint(3);
				holePoints.add(new PolygonPoint(p.getX() + trans.getX(), p.getY() + trans.getY(), p.getZ()));
				polygon.addHole(new Polygon(holePoints));
			}
		}
		return polygon;
	}

	private Polygon toXY(final Polygon polygon) {
		final Vector3 normal = computeNormal();
		final AnyToXYTransform toXY = new AnyToXYTransform(normal.getX(), normal.getY(), normal.getZ());

		for (final TriangulationPoint tp : polygon.getPoints())
			toXY.transform(tp);

		if (polygon.getHoles() != null)
			for (final Polygon hole : polygon.getHoles()) {
				for (final TriangulationPoint tp : hole.getPoints())
					toXY.transform(tp);
			}
		return polygon;
	}

	private Polygon stretchToRoof(final Polygon polygon) {
		// final double baseZ = getAbsPoint(0).getZ() + 0.01;
		final int[] upper = { 0, 3 };

		for (final int i : upper) {
			final TriangulationPoint tp = polygon.getPoints().get(i);
			tp.set(tp.getX(), tp.getY(), findRoofIntersection(new Vector3(tp.getX(), tp.getY(), tp.getZ())));
		}

		TriangulationPoint tp = polygon.getPoints().get(0);
		final Vector3 o = new Vector3(tp.getX(), tp.getY(), tp.getZ());
		// final Vector3 o = new Vector3(tp.getX(), tp.getY(), tp.getZ());
		tp = polygon.getPoints().get(3);
		final Vector3 dir = new Vector3(tp.getX(), tp.getY(), tp.getZ()).subtract(o, null);
		dir.setZ(0);
		final double length = dir.length();
		dir.normalizeLocal();

		Vector3 direction = null;
		ReadOnlyVector3 previousStretchPoint = new Vector3(polygon.getPoints().get(3).getX(), polygon.getPoints().get(3).getY(), polygon.getPoints().get(3).getZ());

		final double step = 0.01;
		for (double d = length - step; d > step; d -= step) {
			final Vector3 p = dir.multiply(d, null).addLocal(o);
			final double findRoofIntersection = findRoofIntersection(p);

			final ReadOnlyVector3 currentStretchPoint = new Vector3(p.getX(), p.getY(), findRoofIntersection);
			final Vector3 currentDirection = currentStretchPoint.subtract(previousStretchPoint, null).normalizeLocal();

			if (direction == null) {
				direction = currentDirection;
			} else if (direction.dot(currentDirection) < 1.0 - MathUtils.ZERO_TOLERANCE) {
				// } else if (direction.smallestAngleBetween(currentDirection) >= 10.0 / 180.0 * Math.PI) {
				direction = null;
				polygon.getPoints().add(new PolygonPoint(p.getX(), p.getY(), findRoofIntersection));
			}
			previousStretchPoint = currentStretchPoint;
		}
		return polygon;
	}

	private ArrayList<Vector3> extractPolygonPoints(final Polygon polygon) {
		final ArrayList<Vector3> gablePoints = new ArrayList<Vector3>();
		for (int i = 0; i < polygon.getPoints().size(); i++)
			gablePoints.add(new Vector3(polygon.getPoints().get(i).getX(), polygon.getPoints().get(i).getY(), polygon.getPoints().get(i).getZ()));
		return gablePoints;
	}

	private ArrayList<ReadOnlyVector3> extractPolygonHoles(final Polygon polygon) {
		final ArrayList<ReadOnlyVector3> gablePoints = new ArrayList<ReadOnlyVector3>();
		if (polygon.getHoles() != null)
			for (final Polygon hole : polygon.getHoles())
				for (int i = 0; i < hole.getPoints().size(); i++)
					gablePoints.add(new Vector3(hole.getPoints().get(i).getX(), hole.getPoints().get(i).getY(), hole.getPoints().get(i).getZ()));
		return gablePoints;
	}

	public double findRoofIntersection(final ReadOnlyVector3 v) {
		return findRoofIntersection(v, Vector3.UNIT_Z, -0.02).getZ();
	}

	public ReadOnlyVector3 findRoofIntersection(final ReadOnlyVector3 v, final ReadOnlyVector3 direction, final double offset) {
		if (roof == null)
			return v;
		final PickResults pickResults = new PrimitivePickResults();
		PickingUtil.findPick(roof.getRoofPartsRoot(), new Ray3(new Vector3(v.getX(), v.getY(), direction.equals(Vector3.UNIT_Z) ? 0 : v.getZ()), direction), pickResults);
		// PickingUtil.findPick(roof.getRoofPartsRoot(), new Ray3(new Vector3(v), direction), pickResults);
		if (pickResults.getNumber() > 0) {
			// final Vector3 intersectionPoint = pickResults.getPickData(0).getIntersectionRecord().getIntersectionPoint(0);
			// return intersectionPoint.getZ() - 0.02; // (backMesh ? 0.1 : 0.0);
			return pickResults.getPickData(0).getIntersectionRecord().getIntersectionPoint(0).add(direction.multiply(Roof.getOverhangLength() > 0.05 ? offset : 0, null), null);
		} else
			return v;
	}

	public boolean isPerpendicularToNeighbor(final int neighbor) {
		final Vector3 dir = getAbsPoint(2).subtract(getAbsPoint(0), null).normalizeLocal();
		final int i = neighbors[neighbor].getSnapPointIndexOfNeighborOf(this);
		final Vector3 otherDir = getAbsPoint(i == 0 ? 2 : 0).subtract(getAbsPoint(i), null).normalizeLocal();
		return Math.abs(dir.dot(otherDir)) < 0.1;
	}

	public boolean includeWindow(final HousePart window) {
		return window.getPoints().size() >= 4 && window.getAbsPoint(2).subtract(window.getAbsPoint(0), null).length() >= 0.1;
	}

	private void drawBackMesh(final Polygon polygon, final XYToAnyTransform fromXY) {
		final Vector3 dir = getAbsPoint(2).subtract(getAbsPoint(0), null).normalizeLocal();
		if (neighbors[0] != null && neighbors[0].getNeighborOf(this).isFirstPointInserted() && !(Scene.getInstance().isDrawThickness() && isShortWall && isPerpendicularToNeighbor(0))) {
			reduceBackMeshWidth(polygon, dir, 0);
		}

		if (neighbors[1] != null && neighbors[1].getNeighborOf(this).isFirstPointInserted() && !(Scene.getInstance().isDrawThickness() && isShortWall && isPerpendicularToNeighbor(1))) {
			dir.normalizeLocal().negateLocal();
			reduceBackMeshWidth(polygon, dir, 1);
		}

		enforceRangeAndRemoveDuplicatedGablePoints(polygon);
		stretchToRoof(polygon);

		// // reduce the height of the back mesh a little
		// for (final TriangulationPoint tp : polygon.getPoints())
		// tp.set(tp.getX(), tp.getY(), tp.getZ() - 0.02);

		toXY(polygon);

		Poly2Tri.triangulate(polygon);
		ArdorMeshMapper.updateTriangleMesh(backMesh, polygon, fromXY);
		ArdorMeshMapper.updateVertexNormals(backMesh, polygon.getTriangles(), fromXY);
		backMesh.getMeshData().updateVertexCount();
	}

	// TODO what does this method do??
	private Polygon enforceRangeAndRemoveDuplicatedGablePoints(final Polygon polygon) {
		final List<TriangulationPoint> polygonPoints = polygon.getPoints();
		final Vector2 min = new Vector2(Math.min(polygonPoints.get(1).getX(), polygonPoints.get(2).getX()), Math.min(polygonPoints.get(1).getY(), polygonPoints.get(2).getY()));
		final Vector2 max = new Vector2(Math.max(polygonPoints.get(1).getX(), polygonPoints.get(2).getX()), Math.max(polygonPoints.get(1).getY(), polygonPoints.get(2).getY()));
		for (int i = 4; i < polygon.pointCount(); i++) {
			final TriangulationPoint tp = polygonPoints.get(i);
			tp.set(Math.max(tp.getX(), min.getX()), Math.max(tp.getY(), min.getY()), tp.getZ());
			tp.set(Math.min(tp.getX(), max.getX()), Math.min(tp.getY(), max.getY()), tp.getZ());
			for (int j = 0; j < i; j++) {
				final TriangulationPoint tpj = polygon.getPoints().get(j);
				if (Math.abs(tpj.getX() - tp.getX()) < MathUtils.ZERO_TOLERANCE && Math.abs(tpj.getY() - tp.getY()) < MathUtils.ZERO_TOLERANCE) {
					polygonPoints.remove(i);
					i--;
					break;
				}
			}
		}
		return polygon;
	}

	private void reduceBackMeshWidth(final Polygon polygon, final ReadOnlyVector3 wallDir, final int neighbor) {
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

		final TriangulationPoint p1 = polygon.getPoints().get(neighbor == 0 ? 1 : 2);
		final TriangulationPoint p2 = polygon.getPoints().get(neighbor == 0 ? 0 : 3);

		// now reduce the actual wall points
		p1.set(p1.getX() + v.getX(), p1.getY() + v.getY(), p1.getZ());
		p2.set(p2.getX() + v.getX(), p2.getY() + v.getY(), p2.getZ());

	}

	public Vector3 getThicknessNormal() {
		if (thicknessNormal != null)
			return thicknessNormal;
		cull(true);
		final Vector3 n = computeNormal();
		final Snap neighbor;

		final int whichNeighbor;
		if (editPointIndex == 0 || editPointIndex == 1) {
			/* if edit point has snapped to a new wall then use the angle with new wall to determine inside direction of this wall otherwise use the angle with the other wall attached to none moving corner of the this wall */
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

//		neighbor = neighbors[editPointIndex == 0 || editPointIndex == 1 ? 1 : 0];


		// if (neighbor != null && neighbor.getNeighborOf(this).getAbsPoints().size() >= 4) {
		if (neighbor != null && neighbor.getNeighborOf(this).getPoints().size() >= 4) {
			final Wall other = neighbor.getNeighborOf(this);
			final int otherPointIndex = neighbor.getSnapPointIndexOfNeighborOf(this);
			final Vector3 otherWallDir = other.getAbsPoint(otherPointIndex == 0 ? 2 : 0).subtract(other.getAbsPoint(otherPointIndex), null).normalizeLocal();
			if (n.dot(otherWallDir) < 0) {
				n.negateLocal();
				cull(false);
			}
		} else {
			final ReadOnlyVector3 camera = SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getDirection();
			if (camera.dot(n) < 0) {
				n.negateLocal();
				cull(false);
			}
		}
		n.multiplyLocal(wallThickness);
		thicknessNormal = n;
		return thicknessNormal;
	}

	private Vector3 computeNormal() {
		final Vector3 p02 = getAbsPoint(2).subtract(getAbsPoint(0), null).normalizeLocal();
		final Vector3 p01 = getAbsPoint(1).subtract(getAbsPoint(0), null).normalizeLocal();
		final Vector3 n = p02.crossLocal(p01).normalizeLocal();
		return n;
	}

	private void cull(final boolean back) {
		// if (back) {
		// mesh.setRenderState(CULL_FRONT);
		// backMesh.setRenderState(CULL_BACK);
		// surroundMesh.setRenderState(CULL_BACK);
		// windowsSurroundMesh.setRenderState(CULL_FRONT);
		// } else {
		// mesh.setRenderState(CULL_BACK);
		// backMesh.setRenderState(CULL_FRONT);
		// surroundMesh.setRenderState(CULL_FRONT);
		// windowsSurroundMesh.setRenderState(CULL_BACK);
		// }
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
		// windowsSurroundMesh.updateModelBound();
		// windowsSurroundMesh.updateWorldBound(false);
	}

	public Snap getOtherSnap(final Snap snap) {
		if (snap == null && neighbors[1] != null)
			return neighbors[1];
		for (final Snap s : neighbors)
			if (s != null && !s.equals(snap))
				return s;
		return null;
	}

	private void setNeighbor(final int pointIndex, Snap newSnap, final boolean updateNeighbors) {
		final int i = pointIndex < 2 ? 0 : 1;
		final Snap oldSnap = neighbors[i];
		// if (newSnap != null && !newSnap.equals(oldSnap))
		// System.out.println(this + "\t.setNeighbor: " + pointIndex + "\t" + newSnap);

		if (newSnap == null && !updateNeighbors) // see if it is attached to another wall
			for (final HousePart part : Scene.getInstance().getParts())
				if (part instanceof Wall && part != this) {
					final Vector3 point = getAbsPoint(pointIndex);
					final Wall wall = (Wall) part;
					if (point.distance(part.getAbsPoint(0)) < MathUtils.ZERO_TOLERANCE) {
						newSnap = new Snap(this, wall, pointIndex, 0);
						wall.setNeighbor(0, newSnap, false);
						wall.drawNeighborWalls();
						break;
					} else if (part.getPoints().size() > 2 && point.distance(part.getAbsPoint(2)) < MathUtils.ZERO_TOLERANCE) {
						newSnap = new Snap(this, wall, pointIndex, 2);
						wall.setNeighbor(2, newSnap, false);
						wall.drawNeighborWalls();
						break;
					}
				}
		neighbors[i] = newSnap;
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
	public void delete() {
		for (int i = 0; i < neighbors.length; i++) {
			if (neighbors[i] != null) {
				final Wall neighbor = neighbors[i].getNeighborOf(this);
				neighbor.setNeighbor(neighbors[i].getSnapPointIndexOfNeighborOf(this), null, false);
				neighbor.draw(); // TODO This is the 2nd time we would draw neighbor (once in setNeighbor) but we need it because otherwise the back wall sticks out
			}
		}
	}

	// public void delete() {
	// for (int i = 0; i < neighbors.length; i++)
	// if (neighbors[i] != null) {
	// final Wall neighbor = neighbors[i].getNeighborOf(this);
	// final int pointIndex = neighbors[i].getSnapPointIndexOfNeighborOf(this);
	// final Vector3 targetP = neighbor.getAbsPoint(pointIndex);
	//
	// // search for other walls that snap to this neighbor
	// boolean found = false;
	// for (final HousePart part : Scene.getInstance().getParts()) {
	// if (part instanceof Wall && part != this) {
	// final boolean p0 = part.getAbsPoint(0).distance(targetP) < MathUtils.ZERO_TOLERANCE;
	// final boolean p2 = part.getAbsPoint(2).distance(targetP) < MathUtils.ZERO_TOLERANCE;
	// if (p0 || p2) {
	// neighbor.setNeighbor(pointIndex, new Snap(neighbor, (Wall) part, pointIndex, p0 ? 0 : 2), false);
	// found = true;
	// break;
	// }
	// }
	// }
	// if (!found)
	// neighbor.setNeighbor(neighbors[i].getSnapPointIndexOfNeighborOf(this), null, false);
	// }
	// }

	@Override
	protected void setHeight(final double newHeight, final boolean finalize) {
		super.setHeight(newHeight, finalize);
		points.get(1).setZ(newHeight + container.height);
		if (isFirstPointInserted())
			points.get(3).setZ(newHeight + container.height);
	}

	@Override
	public void flatten(final double flattenTime) {
		thicknessNormal = getThicknessNormal();
		final Vector3 n = thicknessNormal.normalize(null);
		double angle = n.smallestAngleBetween(Vector3.UNIT_X);
		angle -= Math.PI / 2;

		if (n.dot(Vector3.UNIT_Y) < 0)
			angle = Math.PI - angle;

		root.setRotation((new Matrix3().fromAngles(0, 0, -flattenTime * angle)));
//		System.out.println(new Matrix3().fromAngles(0, -Math.PI / 2.0 * flattenTime, 0).multiply(root.getRotation(), null));
//		root.setRotation(new Matrix3().fromAngles(0, -Math.PI / 2.0 * flattenTime, 0).multiply(root.getRotation(), null));

		super.flatten(flattenTime);

		for (final HousePart part : children)
			if (!part.isPrintable()) {
				part.getRoot().setTransform(root.getTransform());
				part.getRoot().updateGeometricState(0);
			}
	}

	@Override
	public ReadOnlyVector3 getFaceDirection() {
		if (thicknessNormal == null)
			thicknessNormal = getThicknessNormal();
		return thicknessNormal.negate(null).normalizeLocal();
	}

	@Override
	public void drawAnnotations() {
		if (points.size() < 4)
			return;
		final ReadOnlyVector3 faceDirection = getFaceDirection();
		int annotCounter = 0;
		int angleAnnotCounter = 0;

		if (wallPolygonPoints != null) {
			final Vector3 actualNormal = wallPolygonPoints.get(0).subtract(wallPolygonPoints.get(1), null).normalizeLocal().crossLocal(wallPolygonPoints.get(2).subtract(wallPolygonPoints.get(1), null).normalizeLocal()).negateLocal();
			final boolean reverse = actualNormal.dot(getFaceDirection()) < 0;

			// ReadOnlyVector3 p1 = null;
			// ReadOnlyVector3 to = null;

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
			final boolean isRectangular = hi - low < 0.1;;

			if (isRectangular) {				final ReadOnlyVector3 p1 = wallPolygonPoints.get(0).multiply(new Vector3(1, 1, 0), null).addLocal(0, 0, lowestWallZ);
				final ReadOnlyVector3 p2 = wallPolygonPoints.get(1);
				final ReadOnlyVector3 p3 = wallPolygonPoints.get(2);
//				final ReadOnlyVector3 p4 = wallPolygonPoints.get(3);
				final ReadOnlyVector3 p4 = wallPolygonPoints.get(3).multiply(new Vector3(1, 1, 0), null).addLocal(0, 0, lowestWallZ);

				final boolean front = false;

				fetchSizeAnnot(annotCounter++).setRange(p1, p2, getCenter(), faceDirection, front, front ? Align.South : Align.Center, true, reverse, Scene.isDrawAnnotationsInside());
				fetchSizeAnnot(annotCounter++).setRange(p2, p3, getCenter(), faceDirection, original == null, original == null ? Align.South : Align.Center, true, reverse, Scene.isDrawAnnotationsInside());
				fetchSizeAnnot(annotCounter++).setRange(p3, p4, getCenter(), faceDirection, front, front ? Align.South : Align.Center, true, reverse, Scene.isDrawAnnotationsInside());
				fetchSizeAnnot(annotCounter++).setRange(p4, p1, getCenter(), faceDirection, front, front ? Align.South : Align.Center, true, reverse, Scene.isDrawAnnotationsInside());

				for (int i = 0; i < annotCounter ; i++)
					fetchSizeAnnot(i).setLineWidth(lineWidth);

				fetchAngleAnnot(angleAnnotCounter++).setRange(p2, p1, p3, getFaceDirection());
				fetchAngleAnnot(angleAnnotCounter++).setRange(p3, p2, p4, getFaceDirection());
				fetchAngleAnnot(angleAnnotCounter++).setRange(p4, p3, p1, getFaceDirection());
				fetchAngleAnnot(angleAnnotCounter++).setRange(p1, p4, p2, getFaceDirection());

				for (int i = 0; i < annotCounter ; i++)
					fetchAngleAnnot(i).setLineWidth(lineWidth);

			} else
				for (int i = 0; i < wallPolygonPoints.size(); i++) {
					final boolean front = i == 1 && original == null;
					// if (p1 == null)
					final ReadOnlyVector3 p1 = wallPolygonPoints.get(i);
					final ReadOnlyVector3 p2 = wallPolygonPoints.get((i + 1) % wallPolygonPoints.size());
					final ReadOnlyVector3 p3 = wallPolygonPoints.get((i + 2) % wallPolygonPoints.size());
					final double minLength = 0.4;
					if (p1.distance(p2) > minLength) {
//						final ReadOnlyVector3 min = new Vector3(Math.min(p1.getX(), Math.min(p2.getX(), p3.getX())), Math.min(p1.getY(), Math.min(p2.getY(), p3.getY())), Math.min(p1.getZ(), Math.min(p2.getZ(), p3.getZ())));
//						final ReadOnlyVector3 max = new Vector3(Math.max(p1.getX(), Math.max(p2.getX(), p3.getX())), Math.max(p1.getY(), Math.max(p2.getY(), p3.getY())), Math.max(p1.getZ(), Math.max(p2.getZ(), p3.getZ())));
						final ReadOnlyVector3 min = new Vector3(Math.min(p1.getX(), Math.min(p2.getX(), p3.getX())), Math.min(p1.getY(), Math.min(p2.getY(), p3.getY())), 0);
						final ReadOnlyVector3 max = new Vector3(Math.max(p1.getX(), Math.max(p2.getX(), p3.getX())), Math.max(p1.getY(), Math.max(p2.getY(), p3.getY())), 0);
						final ReadOnlyVector3 center = min.add(max, null).divideLocal(2.0).addLocal(0, 0, getCenter().getZ());
						final SizeAnnotation sizeAnnot = fetchSizeAnnot(annotCounter++);
						sizeAnnot.setRange(p1, p2, center, faceDirection, front, front ? Align.South : Align.Center, true, reverse, Scene.isDrawAnnotationsInside());
						sizeAnnot.setLineWidth(lineWidth);
					}
					// if (to.distance(from) > 0.1 && to.distance(afterTo) > 0.1)
					if (p1.distance(p2) > minLength && p2.distance(p3) > minLength) {
						final AngleAnnotation angleAnnot = fetchAngleAnnot(angleAnnotCounter++);
						angleAnnot.setRange(p2, p1, p3, getFaceDirection());
						angleAnnot.setLineWidth(lineWidth);
					}
				}
		}
	}

	protected void visitNeighbors(final WallVisitor visitor) {
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
		Wall.clearVisits();
		while (currentWall != null && !currentWall.isVisited()) {
			final Snap prevSnap = snap;
			snap = currentWall.getOtherSnap(snap);
			visitor.visit(currentWall, prevSnap, snap);
			currentWall.visit();
			if (snap == null)
				break;
			currentWall = snap.getNeighborOf(currentWall);
		}
	}

	// public void visitNeighborsForward(Wall currentWall, Snap prevSnap, final WallVisitor visitor) {
	// Snap nextSnap = currentWall.getOtherSnap(prevSnap);
	// int i = 0;
	//
	// if (!currentWall.isVisited()) {
	// visitor.visit(currentWall, prevSnap, nextSnap);
	// currentWall.visit();
	// i++;
	// }
	//
	// while (nextSnap != null) {
	// currentWall = nextSnap.getNeighborOf(currentWall);
	// if (currentWall.isVisited())
	// return;
	// prevSnap = nextSnap;
	// nextSnap = currentWall.getOtherSnap(prevSnap);
	// visitor.visit(currentWall, prevSnap, nextSnap);
	// currentWall.visit();
	// i++;
	// }
	// System.out.println("total walls visited: " + i);
	//
	// }

	private void drawNeighborWalls() {
		final ArrayList<Wall> walls = new ArrayList<Wall>();
		Wall.clearVisits();
		if (neighbors[0] != null)
			visitNeighborsForward(this, neighbors[1], new WallVisitor() {
				boolean nextIsShort = false;

				@Override
				public void visit(final Wall wall, final Snap prev, final Snap next) {
					visitWallAndReverseThickness(wall, prev);
					walls.add(wall);
					wall.isShortWall = nextIsShort;
					nextIsShort = !nextIsShort;
				}
			});

		if (neighbors[1] != null)
			visitNeighborsForward(this, neighbors[0], new WallVisitor() {
				boolean nextIsShort = false;

				@Override
				public void visit(final Wall wall, final Snap prev, final Snap next) {
					visitWallAndReverseThickness(wall, prev);
					walls.add(wall);
					wall.isShortWall = nextIsShort;
					nextIsShort = !nextIsShort;
				}
			});

		for (final Wall wall : walls)
			wall.draw();
	}

	private void visitWallAndReverseThickness(final Wall wall, final Snap prev) {
		if (wall == Wall.this || !wall.isFirstPointInserted())
			return;
		final int pointIndex = prev.getSnapPointIndexOf(wall);
		final Vector3 wallDir = wall.getAbsPoint(pointIndex == 0 ? 2 : 0).subtract(wall.getAbsPoint(pointIndex), null).normalizeLocal();

		final int otherPointIndex = prev.getSnapPointIndexOfNeighborOf(wall);
		final Wall other = prev.getNeighborOf(wall);
		if (!other.isFirstPointInserted())
			return;
		final Vector3 otherWallDir = other.getAbsPoint(otherPointIndex == 0 ? 2 : 0).subtract(other.getAbsPoint(otherPointIndex), null).normalizeLocal();

		final Vector3 n1 = new Vector3(wall.getThicknessNormal()).normalizeLocal();
		final Vector3 n2 = new Vector3(prev.getNeighborOf(wall).getThicknessNormal()).normalizeLocal();
		final Vector3 add = n1.add(n2, null).normalizeLocal();

		final double dotWall1 = Math.signum(add.dot(wallDir));
		final double dotWall2 = Math.signum(add.dot(otherWallDir));
		final boolean reverse = dotWall1 != dotWall2;
		if (reverse || (dotWall1 == 0 && dotWall2 == 0 && n1.dot(n2) < 0))
			wall.getThicknessNormal().negateLocal();
	}

	@Override
	protected String getDefaultTextureFileName() {
		return "wall.jpg";
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
		root.detachChild(invisibleMesh);
		root.detachChild(backMesh);
		root.detachChild(surroundMesh);
		root.detachChild(windowsSurroundMesh);
		root.detachChild(wireframeMesh);
		backMesh = originalWall.backMesh.makeCopy(true);
		surroundMesh = originalWall.surroundMesh.makeCopy(true);
		windowsSurroundMesh = originalWall.windowsSurroundMesh.makeCopy(true);
		wireframeMesh = originalWall.wireframeMesh.makeCopy(true);
		root.attachChild(backMesh);
		root.attachChild(surroundMesh);
		root.attachChild(windowsSurroundMesh);
		root.attachChild(wireframeMesh);

		final Mesh orgInvisibleMesh = originalWall.invisibleMesh;
		invisibleMesh = orgInvisibleMesh.makeCopy(true);
		invisibleMesh.setUserData(new UserData(this, ((UserData) orgInvisibleMesh.getUserData()).getIndex(), false));
		root.attachChild(invisibleMesh);
		wallPolygonPoints = originalWall.wallPolygonPoints;

		super.setOriginal(original);
	}

	@Override
	protected void drawChildren() {
		super.drawChildren();
		visitNeighbors(new WallVisitor() {
			@Override
			public void visit(final Wall wall, final Snap prev, final Snap next) {
				for (final HousePart child : wall.getChildren())
					if (child instanceof Roof || child instanceof Floor)
						child.draw();
			}
		});
	}

	public HousePart getRoof() {
		return roof;
	}

	@Override
	public void drawGrids(final double gridSize) {
		final ReadOnlyVector3 p0 = getAbsPoint(0);
		// final ReadOnlyVector3 p0 = wallPolygonPoints.get(1);
		final ReadOnlyVector3 p2 = getAbsPoint(2);
		// final ReadOnlyVector3 p2 = wallPolygonPoints.get(2);
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
		// final ReadOnlyVector3 height = wallPolygonPoints.get(1).subtractLocal(p0).normalizeLocal().multiplyLocal(gableHeight);
		final int rows = (int) (gableHeight / gridSize);

		for (int row = 1; row < rows; row++) {
			final ReadOnlyVector3 pMiddle = height.normalize(null).multiplyLocal(row * gridSize).addLocal(gablePeakBase);
			ReadOnlyVector3 lineP1 = new Vector3(p0.getX(), p0.getY(), pMiddle.getZ());
			ReadOnlyVector3 lineP2 = new Vector3(p2.getX(), p2.getY(), pMiddle.getZ());
			if (pMiddle.getZ() > this.height) {
				ReadOnlyVector3 tmp;
				// tmp = findRoofIntersection(pMiddle, false, Vector3.UNIT_Z);
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

		// gridsMesh.getMeshData().updateVertexCount();
		// gridsMesh.updateModelBound();
		// gridsMesh.updateWorldBound(false);
		// gridsMesh.getSceneHints().setCullHint(CullHint.Inherit);
	}

	// @Override
	// protected Vector3 grid(final Vector3 p, final double gridSize, final boolean snapToZ) {
	// if (isSnapToGrids()) {
	// if (container != null)
	// p.subtractLocal(0, 0, container.getHeight());
	// p.set(Math.round(p.getX() / gridSize) * gridSize, Math.round(p.getY() / gridSize) * gridSize, !snapToZ ? p.getZ() : Math.round(p.getZ() / gridSize) * gridSize);
	// if (container != null)
	// p.addLocal(0, 0, container.getHeight());
	// }
	// return p;
	// }

	@Override
	public void reset() {
		super.reset();
		thicknessNormal = null;
	}

	public void setBackMeshesVisible(final boolean visible) {
		backMesh.setVisible(visible);
		surroundMesh.setVisible(visible);
		windowsSurroundMesh.setVisible(visible);
	}

	// @Override
	// public String toString() {
	// return super.toString() + "\t" + (neighbors[0]) + "\t" + (neighbors[1] == null);
	// }
}