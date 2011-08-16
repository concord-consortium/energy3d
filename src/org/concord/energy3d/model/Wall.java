package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.SelectUtil;
import org.concord.energy3d.util.WallVisitor;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.transform.coordinate.AnyToXYTransform;
import org.poly2tri.transform.coordinate.XYToAnyTransform;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.BoundingBox;
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
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.geom.BufferUtils;

public class Wall extends HousePart {
	static private final long serialVersionUID = 1L;
	static private final double GRID_SIZE = 0.5;
	static private final double MIN_WALL_LENGTH = 0.1;
	static private double defaultWallHeight = 1f;
	static private int currentVisitStamp = 1;
	transient private int visitStamp;
	transient private Mesh backMesh;
	transient private Mesh surroundMesh;
	transient private Mesh invisibleMesh;
	transient private Mesh windowsSurroundMesh;
	transient private Mesh wireframeMesh;
	// transient private boolean[] neightborPerpendicular;
	private final double wallThickness = 0.1;
	private final Snap[] neighbors = new Snap[2];
	private Vector3 thicknessNormal;
	private boolean isShortWall;
	transient private ArrayList<Vector3> wallGablePoints; // TODO remove this
	transient private Roof roof;
	transient private ArrayList<ReadOnlyVector3> wallPolygonPoints;

	public static void clearVisits() {
		currentVisitStamp = ++currentVisitStamp % 1000;
	}

	public Wall() {
		// super(2, 4, defaultWallHeight, true);
		super(2, 4, defaultWallHeight);
	}

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
		mesh.setModelBound(new BoundingBox());
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
		surroundMesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		surroundMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(8));
		surroundMesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(8));
		surroundMesh.setDefaultColor(ColorRGBA.GRAY);
		surroundMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		// final OffsetState offsetState = new OffsetState();
		// offsetState.setTypeEnabled(OffsetType.Fill, true);
		// // offsetState.setFactor(1.1f);
		// // offsetState.setUnits(4f);
		// offsetState.setFactor(1);
		// offsetState.setUnits(1);
		// surroundMesh.setRenderState(offsetState);
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
		windowsSurroundMesh.setModelBound(new BoundingBox());
		root.attachChild(windowsSurroundMesh);

		wireframeMesh = new Mesh("Wall (Wireframe)");
		wireframeMesh.getMeshData().setIndexMode(IndexMode.Quads);
		wireframeMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		wireframeMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		wireframeMesh.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		wireframeMesh.getSceneHints().setCastsShadows(false);
		wireframeMesh.setDefaultColor(ColorRGBA.BLACK);
		wireframeMesh.setRenderState(new WireframeState());
		wireframeMesh.setModelBound(new BoundingBox());
		root.attachChild(wireframeMesh);

		updateTextureAndColor(Scene.getInstance().isTextureEnabled());

		UserData userData = new UserData(this);
		mesh.setUserData(userData);
		backMesh.setUserData(userData);
		surroundMesh.setUserData(userData);
		invisibleMesh.setUserData(userData);
	}

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
//					p.set(toRelative(getAbsPoint(i)));
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
					p = grid(p, GRID_SIZE);
					p = closestPoint(closesPoint, dir, p, Vector3.NEG_UNIT_Z);
					p.setX(MathUtils.clamp(p.getX(), Math.min(container.points.get(0).getX(), container.points.get(2).getX()), Math.max(container.points.get(0).getX(), container.points.get(2).getX())));
					p.setY(MathUtils.clamp(p.getY(), Math.min(container.points.get(0).getY(), container.points.get(1).getY()), Math.max(container.points.get(0).getY(), container.points.get(1).getY())));
					p.getZ();;
				}
			}
			if (container != null)
				p.setZ(container.height);
			int index = (editPointIndex == -1) ? points.size() - 2 : editPointIndex;
			final Vector3 p_snap = new Vector3(p);
			Snap snap = snap(p_snap, index);
			if (snap != null && (!isFirstPointInserted() || p_snap.subtract(getAbsPoint(index == 0 ? 2 : 0), null).length() > MIN_WALL_LENGTH))
				p.set(p_snap);
			else
				snap = null;

			if (snap == null) {
				boolean foundationSnap = snapFoundation(p);
				if (!foundationSnap)
					p = grid(p, GRID_SIZE, false);
			}
			setNeighbor(index, snap, true);
			if (index == 2) // make sure z of 2nd base point is same as 2st (needed for platform picking side)
				p.setZ(points.get(0).getZ());
			final Vector3 p_rel = toRelative(p);
			points.get(index).set(p_rel);
			points.get(index + 1).set(p_rel).setZ(p.getZ() + height);
		} else if (editPointIndex == 1 || editPointIndex == 3) {
			int lower = (editPointIndex == 1) ? 0 : 2;
			Vector3 base = getAbsPoint(lower);
			Vector3 closestPoint = closestPoint(base, Vector3.UNIT_Z, x, y);
			Snap snap = snap(closestPoint, -1);
			if (snap == null)
				closestPoint = grid(closestPoint, GRID_SIZE);
			defaultWallHeight = height = Math.max(0.1, closestPoint.getZ() - base.getZ());
			final double z = height + base.getZ();
			points.get(1).setZ(z);
			points.get(3).setZ(z);
		}

		thicknessNormal = null;
		isShortWall = true;
		draw();
		drawChildren();
		showPoints();

		if (isDrawable())
			drawNeighborWalls();

	}

	protected Snap snap(Vector3 p, int index) {
		if (!isSnapToObjects())
			return null;
		ReadOnlyVector3 closestPoint = null;
		double closestDistance = Double.MAX_VALUE;
		Wall closestWall = null;
		int closestPointIndex = -1;
		for (HousePart housePart : Scene.getInstance().getParts()) {
			if (housePart instanceof Wall && housePart != this) { // && (neighbors[0] == null || neighbors[0].getNeighborOf(this) != housePart) && (neighbors[1] == null || neighbors[1].getNeighborOf(this) != housePart)) {
				Wall wall = (Wall) housePart;
				int i = 0;
				// for (Vector3 p2 : wall.getAbsPoints()) {
				for (int j = 0; j < wall.points.size(); j++) {
					final ReadOnlyVector3 p2 = wall.getAbsPoint(j);

					double distance = p.distance(p2);
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
		if (closestDistance < SNAP_DISTANCE) {
			p.set(closestPoint);
			return new Snap(this, closestWall, index, closestPointIndex);
		} else {
			return null;
		}
	}

	private boolean snapFoundation(final Vector3 current) {
		if (container == null)
			return false;
		ReadOnlyVector3 snapPoint = null;
		double snapDistance = Double.MAX_VALUE;
		// for (Vector3 p : container.getAbsPoints()) {
		for (int i = 0; i < container.points.size(); i++) {
			final ReadOnlyVector3 p = container.getAbsPoint(i);

			final double d = p.distance(current);
			if (d < snapDistance) {
				snapDistance = d;
				snapPoint = p;
			}
		}
		if (snapDistance < SNAP_DISTANCE) {
			current.set(snapPoint.getX(), snapPoint.getY(), current.getZ());
			return true;
		} else
			return false;
	}

	private boolean isDrawable() {
		return points.size() >= 4 && points.get(0).subtract(points.get(2), null).length() > MIN_WALL_LENGTH;
	}

	protected void drawMesh() {
		if (!isDrawable())
			return;

		updateEditShapes();

		// final ArrayList<Vector3> abspoints = abspoints;
		final Vector3 normal = computeNormal(); // getAbsPoint(2).subtract(getAbsPoint(0), null).cross(getAbsPoint(1).subtract(getAbsPoint(0), null), null).normalize(null);

		final FloatBuffer invisibleVertexBuffer = invisibleMesh.getMeshData().getVertexBuffer();
		invisibleVertexBuffer.rewind();
		Vector3 p;

		float z = (float) height;
		if (wallGablePoints != null)
			for (final Vector3 gablePoint : wallGablePoints)
				if (gablePoint.getZf() > z)
					z = gablePoint.getZf();

		p = getAbsPoint(1);
		invisibleVertexBuffer.put(p.getXf()).put(p.getYf()).put(z);
		p = getAbsPoint(0);
		invisibleVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
		p = getAbsPoint(2);
		invisibleVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
		p = getAbsPoint(3);
		invisibleVertexBuffer.put(p.getXf()).put(p.getYf()).put(z);

		final Polygon polygon = stretchToRoof(computeWallAndWindowPolygon(false));

		wallPolygonPoints = extractPolygonPoints(polygon);

		toXY(polygon);

		final AnyToXYTransform toXY = new AnyToXYTransform(normal.getX(), normal.getY(), normal.getZ());
		final XYToAnyTransform fromXY = new XYToAnyTransform(normal.getX(), normal.getY(), normal.getZ());

		// keep it for platform resizing
		// if (original == null)
		// for (HousePart child : children) {
		// child.draw();
		// }

		FloatBuffer wireframeVertexBuffer = wireframeMesh.getMeshData().getVertexBuffer();
		final int requiredSize = (1 + children.size()) * 4 * 3;
		if (wireframeVertexBuffer.capacity() < requiredSize) {
			wireframeVertexBuffer = BufferUtils.createVector3Buffer((1 + children.size()) * 4);
			wireframeMesh.getMeshData().setVertexBuffer(wireframeVertexBuffer);
		} else {
			wireframeVertexBuffer.rewind();
			wireframeVertexBuffer.limit(wireframeVertexBuffer.capacity());
		}

		Vector3 p01 = getAbsPoint(1).subtract(getAbsPoint(0), null).normalizeLocal();
		Vector3 p02 = getAbsPoint(2).subtract(getAbsPoint(0), null).normalizeLocal();
		TPoint o = new TPoint(getAbsPoint(0).getX(), getAbsPoint(0).getY(), getAbsPoint(0).getZ());
		TPoint u = new TPoint(p01.getX(), p01.getY(), p01.getZ());
		TPoint v = new TPoint(p02.getX(), p02.getY(), p02.getZ());

		toXY.transform(o);
		toXY.transform(u);
		toXY.transform(v);

		try {
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

			drawBackMesh(computeWallAndWindowPolygon(true), fromXY);
			drawSurroundMesh(thicknessNormal);
			drawWindowsSurroundMesh(thicknessNormal);

			// draw wireframe
			Vector3 w;
			w = getAbsPoint(1);
			wireframeVertexBuffer.put(w.getXf()).put(w.getYf()).put(w.getZf() + 0.01f);
			w = getAbsPoint(0);
			wireframeVertexBuffer.put(w.getXf()).put(w.getYf()).put(w.getZf() + 0.01f);
			w = getAbsPoint(2);
			wireframeVertexBuffer.put(w.getXf()).put(w.getYf()).put(w.getZf());
			if (wallGablePoints == null) {
				w = getAbsPoint(3);
				wireframeVertexBuffer.put(w.getXf()).put(w.getYf()).put(w.getZf());
			}

			wireframeVertexBuffer.limit(wireframeVertexBuffer.position());

			backMesh.updateModelBound();
			surroundMesh.updateModelBound();
			windowsSurroundMesh.updateModelBound();
			wireframeMesh.updateModelBound();
			invisibleMesh.updateModelBound();

			root.updateWorldBound(true);
		} catch (Throwable e) {
			e.printStackTrace();
		}
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
			// final ArrayList<Vector3> winPoints = child.getAbsPoints();
			if (child instanceof Window && includeWindow(child)) {
				ArrayList<PolygonPoint> holePoints = new ArrayList<PolygonPoint>();
				// p = winPoints.get(1);
				p = child.getAbsPoint(1);
				holePoints.add(new PolygonPoint(p.getX() + trans.getX(), p.getY() + trans.getY(), p.getZ()));
				// p = winPoints.get(0);
				p = child.getAbsPoint(0);
				holePoints.add(new PolygonPoint(p.getX() + trans.getX(), p.getY() + trans.getY(), p.getZ()));
				// p = winPoints.get(2);
				p = child.getAbsPoint(2);
				holePoints.add(new PolygonPoint(p.getX() + trans.getX(), p.getY() + trans.getY(), p.getZ()));
				// p = winPoints.get(3);
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

	// private Polygon stretchToRoof(final Polygon polygon) {
	// final int[] upper = { 0, 3 };
	//
	// for (final int i : upper) {
	// final TriangulationPoint tp = polygon.getPoints().get(i);
	// tp.set(tp.getX(), tp.getY(), findRoofIntersection(new Vector3(tp.getX(), tp.getY(), tp.getZ()), false));
	// }
	//
	// TriangulationPoint tp = polygon.getPoints().get(0);
	// final Vector3 o = new Vector3(tp.getX(), tp.getY(), tp.getZ());
	// tp = polygon.getPoints().get(3);
	// final Vector3 dir = new Vector3(tp.getX(), tp.getY(), tp.getZ()).subtract(o, null);
	// dir.setZ(0);
	// final double length = dir.length();
	// dir.normalizeLocal();
	//
	// for (double d = length - 0.1; d > 0.1; d -= 0.1) {
	// final Vector3 p = dir.multiply(d, null).addLocal(o);
	// final double findRoofIntersection = findRoofIntersection(p, false);
	// polygon.getPoints().add(new PolygonPoint(p.getX(), p.getY(), findRoofIntersection));
	// }
	//
	// return polygon;
	// }

	// private Polygon stretchToRoof(final Polygon polygon, final boolean updateWallGablePoints) {
	// final int[] upper = { 0, 3 };
	//
	// for (final int i : upper) {
	// final TriangulationPoint tp = polygon.getPoints().get(i);
	// tp.set(tp.getX(), tp.getY(), findRoofIntersection(new Vector3(tp.getX(), tp.getY(), tp.getZ()), false));
	// }
	//
	// if (updateWallGablePoints) {
	// wallGableStretchPoints = new ArrayList<ReadOnlyVector3>();
	// wallGableStretchPoints.add(new Vector3(polygon.getPoints().get(3).getX(), polygon.getPoints().get(3).getY(), polygon.getPoints().get(3).getZ()));
	// }
	//
	// TriangulationPoint tp = polygon.getPoints().get(0);
	// final Vector3 o = new Vector3(tp.getX(), tp.getY(), tp.getZ());
	// tp = polygon.getPoints().get(3);
	// final Vector3 dir = new Vector3(tp.getX(), tp.getY(), tp.getZ()).subtract(o, null);
	// dir.setZ(0);
	// final double length = dir.length();
	// dir.normalizeLocal();
	//
	// Vector3 direction = null;
	//
	// for (double d = length - 0.1; d > 0.1; d -= 0.1) {
	// final Vector3 p = dir.multiply(d, null).addLocal(o);
	// final double findRoofIntersection = findRoofIntersection(p, false);
	//
	// if (updateWallGablePoints) {
	// final ReadOnlyVector3 currentStretchPoint = new Vector3(p.getX(), p.getY(), findRoofIntersection);
	// final Vector3 currentDirection = currentStretchPoint.subtract(wallGableStretchPoints.get(wallGableStretchPoints.size() - 1), null).normalizeLocal();
	//
	// if (direction == null) {
	// direction = currentDirection;
	// } else if (direction.dot(currentDirection) < 1.0 - MathUtils.ZERO_TOLERANCE) {
	// direction = null;
	// wallGableStretchPoints.add(currentStretchPoint);
	// polygon.getPoints().add(new PolygonPoint(p.getX(), p.getY(), findRoofIntersection));
	// }
	// }
	// }
	//
	// if (updateWallGablePoints)
	// wallGableStretchPoints.add(new Vector3(polygon.getPoints().get(0).getX(), polygon.getPoints().get(0).getY(), polygon.getPoints().get(0).getZ()));
	//
	// return polygon;
	// }

	private Polygon stretchToRoof(final Polygon polygon) {
		final int[] upper = { 0, 3 };

		for (final int i : upper) {
			final TriangulationPoint tp = polygon.getPoints().get(i);
			tp.set(tp.getX(), tp.getY(), findRoofIntersection(new Vector3(tp.getX(), tp.getY(), tp.getZ()), false));
		}

		TriangulationPoint tp = polygon.getPoints().get(0);
		final Vector3 o = new Vector3(tp.getX(), tp.getY(), tp.getZ());
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
			final double findRoofIntersection = findRoofIntersection(p, false);

			final ReadOnlyVector3 currentStretchPoint = new Vector3(p.getX(), p.getY(), findRoofIntersection);
			final Vector3 currentDirection = currentStretchPoint.subtract(previousStretchPoint, null).normalizeLocal();

			if (direction == null) {
				direction = currentDirection;
			} else if (direction.dot(currentDirection) < 1.0 - MathUtils.ZERO_TOLERANCE) {
				direction = null;
				polygon.getPoints().add(new PolygonPoint(p.getX(), p.getY(), findRoofIntersection));
			}
			previousStretchPoint = currentStretchPoint;
		}
		return polygon;
	}

	private ArrayList<ReadOnlyVector3> extractPolygonPoints(final Polygon polygon) {
		final ArrayList<ReadOnlyVector3> gablePoints = new ArrayList<ReadOnlyVector3>();

		gablePoints.add(new Vector3(polygon.getPoints().get(3).getX(), polygon.getPoints().get(3).getY(), polygon.getPoints().get(3).getZ()));

		for (int i = 4; i < polygon.getPoints().size(); i++)
			gablePoints.add(new Vector3(polygon.getPoints().get(i).getX(), polygon.getPoints().get(i).getY(), polygon.getPoints().get(i).getZ()));

		for (int i = 0; i < 4; i++)
			gablePoints.add(new Vector3(polygon.getPoints().get(i).getX(), polygon.getPoints().get(i).getY(), polygon.getPoints().get(i).getZ()));

		return gablePoints;
	}

	public double findRoofIntersection(final ReadOnlyVector3 v, final boolean backMesh) {
		if (roof == null)
			return v.getZ();
		final PickResults pickResults = new PrimitivePickResults();
		PickingUtil.findPick(roof.getFlattenedMeshesRoot(), new Ray3(new Vector3(v.getX(), v.getY(), 0), Vector3.UNIT_Z), pickResults);
		if (pickResults.getNumber() > 0) {
			final Vector3 intersectionPoint = pickResults.getPickData(0).getIntersectionRecord().getIntersectionPoint(0);
			return intersectionPoint.getZ() - (backMesh ? 0.1 : 0.0);
		}
		return v.getZ();
	}

	public boolean isPerpendicularToNeighbor(final int neighbor) {
		final Vector3 dir = getAbsPoint(2).subtract(getAbsPoint(0), null).normalizeLocal();
		// final ArrayList<Vector3> abspoints = neighbors[neighbor].getNeighborOf(this).abspoints;
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

		toXY(stretchToRoof(polygon));

		Poly2Tri.triangulate(polygon);
		ArdorMeshMapper.updateTriangleMesh(backMesh, polygon, fromXY);
		ArdorMeshMapper.updateVertexNormals(backMesh, polygon.getTriangles(), fromXY);
		backMesh.getMeshData().updateVertexCount();
	}

	private Polygon enforceRangeAndRemoveDuplicatedGablePoints(final Polygon polygon) {
		final List<TriangulationPoint> polygonPoints = polygon.getPoints();
		for (int i = 4; i < polygon.pointCount(); i++) {
			final Vector2 min = new Vector2(Math.min(polygonPoints.get(1).getX(), polygonPoints.get(2).getX()), Math.min(polygonPoints.get(1).getY(), polygonPoints.get(2).getY()));
			final Vector2 max = new Vector2(Math.max(polygonPoints.get(1).getX(), polygonPoints.get(2).getX()), Math.max(polygonPoints.get(1).getY(), polygonPoints.get(2).getY()));
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

		// reduce the roof vertices of the wall if they are at same location as the neighbor x,y
		// if (wallGablePoints != null) {
		// final Vector3 gablePointPrj = new Vector3();
		// final Vector3 neighborUpperPointPrj = new Vector3(getAbsPoint(neighbor == 0 ? 1 : 3));
		// neighborUpperPointPrj.setZ(0);
		// int i = 4;
		// for (final Vector3 gablePoint : wallGablePoints) {
		// gablePointPrj.set(gablePoint).setZ(0);
		// if (gablePointPrj.distance(neighborUpperPointPrj) < MathUtils.ZERO_TOLERANCE * 2) {
		// final TriangulationPoint p = polygon.getPoints().get(i);
		// p.set(p.getX() + v.getX(), p.getY() + v.getY(), p.getZ());
		// }
		// i++;
		// }
		// }

		// now reduce the actual wall points
		p1.set(p1.getX() + v.getX(), p1.getY() + v.getY(), p1.getZ());
		p2.set(p2.getX() + v.getX(), p2.getY() + v.getY(), p2.getZ());

	}

	public Vector3 getThicknessNormal() {
		if (thicknessNormal != null)
			return thicknessNormal;
		// final ArrayList<Vector3> abspoints = abspoints;
		cull(true);

		final Vector3 n = computeNormal();

		final Snap neighbor;
		// if (editPointIndex != -1)
		neighbor = neighbors[editPointIndex == 0 || editPointIndex == 1 ? 1 : 0];
		// else if (isFirstPointInserted())
		// neighbor = neighbors[1];
		// else
		// neighbor = neighbors[0];

		// if (neighbor != null && neighbor.getNeighborOf(this).getAbsPoints().size() >= 4) {
		if (neighbor != null && neighbor.getNeighborOf(this).getPoints().size() >= 4) {
			// final ArrayList<Vector3> otherPoints = neighbor.getNeighborOf(this).getAbsPoints();//
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
		// System.out.println(n + "");
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

	private void cull(boolean back) {
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
		// final ArrayList<Vector3> abspoints = abspoints;
		final FloatBuffer vertexBuffer = surroundMesh.getMeshData().getVertexBuffer();
		final FloatBuffer normalBuffer = surroundMesh.getMeshData().getNormalBuffer();
		vertexBuffer.position(0);
		normalBuffer.position(0);
		final Vector3 p2 = new Vector3();
		// final int[] order;
		//
		// if (neighbors[0] == null && neighbors[1] == null)
		// order = new int[] { 0, 1, 3, 2 };
		// else if (neighbors[0] == null) {
		// if (Scene.getInstance().isDrawThickness() && !this.isShortWall && neightborPerpendicular[1])
		// order = new int[] { 0, 1, 3, 2 };
		// else
		// order = new int[] { 0, 1, 3 };
		// } else if (neighbors[1] == null) {
		// if (Scene.getInstance().isDrawThickness() && !this.isShortWall && neightborPerpendicular[0])
		// order = new int[] { 0, 1, 3, 2 };
		// else
		// order = new int[] { 1, 3, 2 };
		// } else {
		//
		// }

		final ArrayList<Integer> order = new ArrayList<Integer>(4);
		order.add(1);
		order.add(3);
		final boolean drawThicknessAndIsLongWall = Scene.getInstance().isDrawThickness() && !this.isShortWall;
		if (neighbors[0] == null || (drawThicknessAndIsLongWall && isPerpendicularToNeighbor(0)))
			order.add(0, 0);
		if (neighbors[1] == null || (drawThicknessAndIsLongWall && isPerpendicularToNeighbor(1)))
			order.add(2);

		// if (Scene.getInstance().isDrawThickness() && !this.isShortWall)
		// order = new int[] { 0, 1, 3, 2 };
		// else if (neighbors[0] != null && neighbors[1] != null)
		// order = new int[] { 1, 3 };
		// else if (neighbors[0] != null)
		// order = new int[] { 1, 3, 2 };
		// else if (neighbors[1] != null)
		// order = new int[] { 0, 1, 3 };
		// else
		// order = new int[] { 0, 1, 3, 2 };

		final Vector3 sideNormal = thickness.cross(0, 0, 1, null).normalizeLocal();
		for (int i : order) {
			final ReadOnlyVector3 p = getAbsPoint(i);
			vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
			p2.set(p).addLocal(thickness);
			vertexBuffer.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());

			if (i == 1 || i == 3) {
				normalBuffer.put(0).put(0).put(1);
				normalBuffer.put(0).put(0).put(1);
			} else if (i == 0 || i == 2) {
				if (i == 2)
					sideNormal.negateLocal();
				normalBuffer.put(sideNormal.getXf()).put(sideNormal.getYf()).put(sideNormal.getZf());
				normalBuffer.put(sideNormal.getXf()).put(sideNormal.getYf()).put(sideNormal.getZf());
			}
		}

		while (vertexBuffer.position() < vertexBuffer.capacity())
			vertexBuffer.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
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
		for (HousePart child : children) {
			// final ArrayList<Vector3> winPoints = child.getAbsPoints();
			if (child instanceof Window && includeWindow(child)) {
				int[] order = order1;
				// final Vector3 windowDirection = winPoints.get(2).subtract(winPoints.get(0), null);
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
	}

	// public Snap getOtherSnap(Wall previous) {
	// for (Snap s : neighbors)
	// if (s != null && s.getNeighborOf(this) != previous)
	// return s;
	// return null;
	// }

	public Snap getOtherSnap(final Snap snap) {
		if (snap == null && neighbors[1] != null)
			return neighbors[1];
		for (Snap s : neighbors)
			if (s != null && !s.equals(snap))
				return s;
		return null;
	}

	private void setNeighbor(final int pointIndex, Snap newSnap, final boolean updateNeighbors) {
		final int i = pointIndex < 2 ? 0 : 1;
		final Snap oldSnap = neighbors[i];

		if (newSnap == null && !updateNeighbors) // see if it is attached to another wall
			for (HousePart part : Scene.getInstance().getParts())
				if (part instanceof Wall && part != this) {
					final Vector3 point = points.get(pointIndex);
					final Wall wall = (Wall) part;
					if (point.distance(part.getAbsPoint(0)) < 0.001) {
						newSnap = new Snap(this, wall, pointIndex, 0);
						wall.setNeighbor(0, newSnap, false);
						break;
					} else if (part.getPoints().size() > 2 && point.distance(part.getAbsPoint(2)) < 0.001) {
						newSnap = new Snap(this, wall, pointIndex, 2);
						wall.setNeighbor(2, newSnap, false);
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

	public void delete() {
		for (int i = 0; i < neighbors.length; i++)
			if (neighbors[i] != null)
				neighbors[i].getNeighborOf(this).setNeighbor(neighbors[i].getSnapPointIndexOfNeighborOf(this), null, false);
		// final ArrayList<HousePart> children = (ArrayList<HousePart>)this.children.clone();
		// final Iterator<HousePart> children = this.children.iterator();
		// while (children.hasNext())
		// for (HousePart child : children)
		// Scene.getInstance().remove(child);
		// children.clear();
	}

	protected void setHeight(final double newHeight, final boolean finalize) {
		super.setHeight(newHeight, finalize);
		points.get(1).setZ(newHeight + container.height);
		if (isFirstPointInserted())
			points.get(3).setZ(newHeight + container.height);
	}

	public void flatten(double flattenTime) {
		thicknessNormal = getThicknessNormal();
		final Vector3 n = thicknessNormal.normalize(null);
		double angle = n.smallestAngleBetween(Vector3.UNIT_X);
		angle -= Math.PI / 2;

		if (n.dot(Vector3.UNIT_Y) < 0)
			angle = Math.PI - angle;

		root.setRotation((new Matrix3().fromAngles(0, 0, -flattenTime * angle)));
		super.flatten(flattenTime);

		for (HousePart part : children)
			if (!part.isPrintable())
				// part.draw();
				part.getRoot().setTransform(root.getTransform());
	}

	public ReadOnlyVector3 getFaceDirection() {
		if (thicknessNormal == null)
			thicknessNormal = getThicknessNormal();
		return thicknessNormal.negate(null).normalizeLocal();
	}

	protected void drawAnnotations() {
		if (points.size() < 4)
			return;
		final ReadOnlyVector3 faceDirection = getFaceDirection();
		int annotCounter = 0;
		int angleAnnotCounter = 0;

		// fetchSizeAnnot(annotCounter++).setRange(getAbsPoint(0), getAbsPoint(2), center, faceDirection, original == null, original == null ? Align.South : Align.Center, true);
		// if (original != null || neighbors[0] == null || !neighbors[0].isDrawn()) {
		// fetchSizeAnnot(annotCounter++).setRange(getAbsPoint(0), getAbsPoint(1), center, faceDirection, original == null, Align.Center, true);
		// if (neighbors[0] != null)
		// neighbors[0].setDrawn();
		// }
		// if (original != null || neighbors[1] == null || !neighbors[1].isDrawn()) {
		// fetchSizeAnnot(annotCounter++).setRange(getAbsPoint(2), getAbsPoint(3), center, faceDirection, original == null, Align.Center, true);
		// if (neighbors[1] != null)
		// neighbors[1].setDrawn();
		// }
		// if (original != null)
		// fetchSizeAnnot(annotCounter++).setRange(getAbsPoint(1), getAbsPoint(3), center, faceDirection, original == null, Align.Center, true);

		if (wallPolygonPoints != null) {
			final int n = wallPolygonPoints.size();
			final Vector3 actualNormal = wallPolygonPoints.get(n - 3).subtract(wallPolygonPoints.get(n - 2), null).normalizeLocal().crossLocal(wallPolygonPoints.get(n - 1).subtract(wallPolygonPoints.get(n - 2), null).normalizeLocal());
			final boolean reverse = actualNormal.dot(getFaceDirection()) > 0;

			for (int i = 0; i < wallPolygonPoints.size() - 1; i++) {
				final boolean front = i == wallPolygonPoints.size() - 3 && original == null;
				fetchSizeAnnot(annotCounter++).setRange(wallPolygonPoints.get(i), wallPolygonPoints.get(i + 1), getCenter(), faceDirection, front, front ? Align.South : Align.Center, true, true, reverse);
				if (i > 0)
					fetchAngleAnnot(angleAnnotCounter++).setRange(wallPolygonPoints.get(i), wallPolygonPoints.get(i - 1), wallPolygonPoints.get(i + 1), getFaceDirection());
				else
					fetchAngleAnnot(angleAnnotCounter++).setRange(wallPolygonPoints.get(0), wallPolygonPoints.get(wallPolygonPoints.size() - 2), wallPolygonPoints.get(1), getFaceDirection());
			}
		}

		for (int i = annotCounter; i < sizeAnnotRoot.getChildren().size(); i++)
			sizeAnnotRoot.getChild(i).getSceneHints().setCullHint(CullHint.Always);

		// Angle annotations
		// annotCounter = 0;
		// fetchAngleAnnot90(annotCounter++).setRange(getAbsPoint(0), getAbsPoint(2), getAbsPoint(1), getFaceDirection());
		// fetchAngleAnnot90(annotCounter++).setRange(getAbsPoint(1), getAbsPoint(3), getAbsPoint(0), getFaceDirection());
		// fetchAngleAnnot90(annotCounter++).setRange(getAbsPoint(2), getAbsPoint(0), getAbsPoint(3), getFaceDirection());
		// fetchAngleAnnot90(annotCounter++).setRange(getAbsPoint(3), getAbsPoint(1), getAbsPoint(2), getFaceDirection());

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

		Wall.clearVisits();
		visitNeighborsForward(currentWall, null, visitor);
	}

	public void visitNeighborsForward(Wall currentWall, Snap prevSnap, final WallVisitor visitor) {
		Snap nextSnap = currentWall.getOtherSnap(prevSnap);
		if (!currentWall.isVisited()) {
			visitor.visit(currentWall, prevSnap, nextSnap);
			currentWall.visit();
		}

		while (nextSnap != null) {
			currentWall = nextSnap.getNeighborOf(currentWall);
			if (currentWall.isVisited())
				return;
			prevSnap = nextSnap;
			nextSnap = currentWall.getOtherSnap(prevSnap);
			visitor.visit(currentWall, prevSnap, nextSnap);
			currentWall.visit();
		}

	}

	// protected void visitNeighborsForward(final boolean forward, final WallVisitor visitor) {
	// Wall currentWall = this;
	// Wall.clearVisits();
	// Snap prevSnap = neighbors[forward ? 0 : 1];
	// Snap nextSnap = neighbors[forward ? 1 : 0];
	// // if (next == null)
	// // return;
	// // else
	// // currentWall = next.getNeighborOf(this);
	// while (nextSnap != null && currentWall != null && !currentWall.isVisited()) {
	// currentWall = nextSnap.getNeighborOf(currentWall);
	// prevSnap = nextSnap;
	// nextSnap = currentWall.getOtherSnap(nextSnap);
	// visitor.visit(currentWall, prevSnap, nextSnap);
	// currentWall.visit();
	// }
	// }

	private void drawNeighborWalls() {
		final ArrayList<Wall> walls = new ArrayList<Wall>();
		// visitNeighborsForward(true, new WallVisitor() {
		Wall.clearVisits();
		if (neighbors[0] != null)
			visitNeighborsForward(this, neighbors[1], new WallVisitor() {
				boolean nextIsShort = false;

				public void visit(Wall wall, Snap prev, Snap next) {
					visitWall(wall, prev);
					walls.add(wall);
					wall.isShortWall = nextIsShort;
					nextIsShort = !nextIsShort;
				}
			});

		if (neighbors[1] != null)
			visitNeighborsForward(this, neighbors[0], new WallVisitor() {
				boolean nextIsShort = false;

				public void visit(Wall wall, Snap prev, Snap next) {
					visitWall(wall, prev);
					walls.add(wall);
					wall.isShortWall = nextIsShort;
					nextIsShort = !nextIsShort;
				}
			});

		// this.draw();
		for (Wall wall : walls)
			wall.draw();
	}

	private void visitWall(final Wall wall, final Snap prev) {
		if (wall == Wall.this)
			return;
		final int pointIndex = prev.getSnapPointIndexOf(wall);
		// final ArrayList<Vector3> points = wall.abspoints;
		final Vector3 wallDir = wall.getAbsPoint(pointIndex == 0 ? 2 : 0).subtract(wall.getAbsPoint(pointIndex), null).normalizeLocal();

		final int otherPointIndex = prev.getSnapPointIndexOfNeighborOf(wall);
		final Wall other = prev.getNeighborOf(wall);
		// final ArrayList<Vector3> otherPoints = other.getAbsPoints();
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

	protected String getDefaultTextureFileName() {
		return "wall.jpg";
	}

	public boolean isVisited() {
		return visitStamp == currentVisitStamp;
	}

	public void visit() {
		visitStamp = currentVisitStamp;
		// System.out.println(this);
	}

	public void setGablePoints(final ArrayList<Vector3> wallGablePoints) {
		this.wallGablePoints = wallGablePoints;
	}

	public void setRoof(final Roof roof) {
		this.roof = roof;
	}

	// public void drawNeighbors() {
	// for (final Snap snap : neighbors)
	// if (snap != null)
	// snap.getNeighborOf(this).draw();
	// }

	@Override
	public void setOriginal(final HousePart original) {
		root.detachChild(this.invisibleMesh);
		root.detachChild(backMesh);
		root.detachChild(surroundMesh);
		root.detachChild(windowsSurroundMesh);
		root.detachChild(wireframeMesh);
		final Mesh orgInvisibleMesh = ((Wall) original).invisibleMesh;
		this.invisibleMesh = orgInvisibleMesh.makeCopy(true);
		this.invisibleMesh.setUserData(new UserData(this, ((UserData) orgInvisibleMesh.getUserData()).getIndex(), false));
		root.attachChild(invisibleMesh);
		wallPolygonPoints = ((Wall) original).wallPolygonPoints;

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

}