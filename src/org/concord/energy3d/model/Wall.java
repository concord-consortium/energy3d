package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.WallVisitor;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.transform.coordinate.AnyToXYTransform;
import org.poly2tri.transform.coordinate.XYToAnyTransform;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.OffsetState;
import com.ardor3d.renderer.state.OffsetState.OffsetType;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.scenegraph.Mesh;
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
	transient private ArrayList<Vector3> wallGablePoints;

	public static void clearVisits() {
		currentVisitStamp = ++currentVisitStamp % 1000;
	}

	public Wall() {
		super(2, 4, defaultWallHeight, true);
	}

	protected void init() {
		super.init();
		mesh = new Mesh("Wall");
		backMesh = new Mesh("Wall (Back)");
		surroundMesh = new Mesh("Wall (Surround)");
		invisibleMesh = new Mesh("Wall (Invisible)");
		windowsSurroundMesh = new Mesh("Wall (Windows Surround)");
		wireframeMesh = new Mesh("Wall (Wireframe)");

		root.attachChild(mesh);
		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		mesh.getMeshData().setTextureBuffer(BufferUtils.createVector2Buffer(4), 0);
		mesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);

		root.attachChild(backMesh);
		backMesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		backMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		backMesh.getMeshData().setTextureBuffer(BufferUtils.createVector2Buffer(4), 0);
		backMesh.setDefaultColor(ColorRGBA.LIGHT_GRAY);
		backMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);

		root.attachChild(surroundMesh);
		surroundMesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		surroundMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(8));
		surroundMesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(8));
		surroundMesh.setDefaultColor(ColorRGBA.GRAY);
		surroundMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		final OffsetState offsetState = new OffsetState();
		offsetState.setTypeEnabled(OffsetType.Fill, true);
		offsetState.setFactor(1.1f);
		offsetState.setUnits(4f);
		surroundMesh.setRenderState(offsetState);

		root.attachChild(invisibleMesh);
		invisibleMesh.getMeshData().setIndexMode(IndexMode.Quads);
		invisibleMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		invisibleMesh.getSceneHints().setCullHint(CullHint.Always);

		root.attachChild(windowsSurroundMesh);
		windowsSurroundMesh.getMeshData().setIndexMode(IndexMode.Quads);
		windowsSurroundMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(1000));
		windowsSurroundMesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(1000));
		windowsSurroundMesh.setDefaultColor(ColorRGBA.GRAY);
		windowsSurroundMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);

		root.attachChild(wireframeMesh);
		wireframeMesh.getMeshData().setIndexMode(IndexMode.Quads);
		wireframeMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		wireframeMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		wireframeMesh.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		wireframeMesh.getSceneHints().setCastsShadows(false);
		wireframeMesh.setDefaultColor(ColorRGBA.BLACK);
		wireframeMesh.setRenderState(new WireframeState());

		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		mesh.setRenderState(ms);

		updateTextureAndColor(Scene.getInstance().isTextureEnabled());

		UserData userData = new UserData(this);
		mesh.setUserData(userData);
		backMesh.setUserData(userData);
		surroundMesh.setUserData(userData);
		invisibleMesh.setUserData(userData);

		// neightborPerpendicular = new boolean[2];
	}

	public void setPreviewPoint(final int x, final int y) {
		Snap.clearAnnotationDrawn();
		if (editPointIndex == -1 || editPointIndex == 0 || editPointIndex == 2) {
			final HousePart previousContainer = container;
			final PickedHousePart picked = pick(x, y, new Class<?>[] { Foundation.class, null });
			if (container != previousContainer)
				for (int i = 0; i < points.size(); i++)
					points.get(i).set(toRelative(abspoints.get(i)));
			if (picked != null) {
				Vector3 p = picked.getPoint();
				if (container != null)
					p.setZ(container.height);
				int index = (editPointIndex == -1) ? points.size() - 2 : editPointIndex;
				final Vector3 p_snap = new Vector3(p);
				Snap snap = snap(p_snap, index);
				if (snap != null && (!isFirstPointInserted() || p_snap.subtract(abspoints.get(index == 0 ? 2 : 0), null).length() > MIN_WALL_LENGTH))
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
			}
		} else if (editPointIndex == 1 || editPointIndex == 3) {
			int lower = (editPointIndex == 1) ? 0 : 2;
			Vector3 base = abspoints.get(lower);
			Vector3 closestPoint = closestPoint(base, Vector3.UNIT_Z, x, y);
			Snap snap = snap(closestPoint, -1);
			if (snap == null)
				closestPoint = grid(closestPoint, GRID_SIZE);
			defaultWallHeight = height = Math.max(0.1, closestPoint.getZ() - base.getZ());
			final double z = height + base.getZ();
			points.get(1).setZ(z);
			;
			points.get(3).setZ(z);
		}

		thicknessNormal = null;
		isShortWall = true;
		draw();
		showPoints();

		if (isDrawable())
			drawNeighborWalls();

	}

	protected Snap snap(Vector3 p, int index) {
		if (!isSnapToObjects())
			return null;
		Vector3 closestPoint = null;
		double closestDistance = Double.MAX_VALUE;
		Wall closestWall = null;
		int closestPointIndex = -1;
		for (HousePart housePart : Scene.getInstance().getParts()) {
			if (housePart instanceof Wall && housePart != this) { // && (neighbors[0] == null || neighbors[0].getNeighborOf(this) != housePart) && (neighbors[1] == null || neighbors[1].getNeighborOf(this) != housePart)) {
				Wall wall = (Wall) housePart;
				int i = 0;
				for (Vector3 p2 : wall.getAbsPoints()) {
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
		Vector3 snapPoint = null;
		double snapDistance = Double.MAX_VALUE;
		for (Vector3 p : container.getAbsPoints()) {
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

		final ArrayList<Vector3> points = abspoints;
		final Vector3 normal = points.get(2).subtract(points.get(0), null).cross(points.get(1).subtract(points.get(0), null), null).normalize(null);

		final ArrayList<PolygonPoint> polyPoints = new ArrayList<PolygonPoint>();

		final FloatBuffer invisibleVertexBuffer = invisibleMesh.getMeshData().getVertexBuffer();
		invisibleVertexBuffer.rewind();
		Vector3 p;

		float z = (float)height;
		if (wallGablePoints != null)
			for (final Vector3 gablePoint : wallGablePoints)
				if (gablePoint.getZf() > z)
					z = gablePoint.getZf();

		p = points.get(1);
		invisibleVertexBuffer.put(p.getXf()).put(p.getYf()).put(z);
		p = points.get(0);
		invisibleVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
		p = points.get(2);
		invisibleVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
		p = points.get(3);
		invisibleVertexBuffer.put(p.getXf()).put(p.getYf()).put(z);		

		// start the polygon with (1) then 0, 2, 3, [roof points] so that roof points are appended to the end of vertex list
		p = points.get(1);
		polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
		p = points.get(0);
		polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
		p = points.get(2);
		polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
		p = points.get(3);
		polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
		if (wallGablePoints != null)
			for (final Vector3 gablePoint : wallGablePoints)
				polyPoints.add(new PolygonPoint(gablePoint.getX(), gablePoint.getY(), gablePoint.getZ()));

		final AnyToXYTransform toXY = new AnyToXYTransform(normal.getX(), normal.getY(), normal.getZ());
		final XYToAnyTransform fromXY = new XYToAnyTransform(normal.getX(), normal.getY(), normal.getZ());

		for (TriangulationPoint tp : polyPoints)
			toXY.transform(tp);

		final Polygon polygon = new Polygon(polyPoints);

		// keep it for platform resizing
		if (original == null)
			for (HousePart child : children) {
				child.draw();
			}

		FloatBuffer wireframeVertexBuffer = wireframeMesh.getMeshData().getVertexBuffer();
		final int requiredSize = (1 + children.size()) * 4 * 3;
		if (wireframeVertexBuffer.capacity() < requiredSize) {
			wireframeVertexBuffer = BufferUtils.createVector3Buffer((1 + children.size()) * 4);
			wireframeMesh.getMeshData().setVertexBuffer(wireframeVertexBuffer);
		} else {
			wireframeVertexBuffer.rewind();
			wireframeVertexBuffer.limit(wireframeVertexBuffer.capacity());
		}

		// Add window holes
		for (HousePart child : children) {
			ArrayList<Vector3> winPoints = child.getAbsPoints();
			if (child instanceof Window && includeWindow(winPoints)) {
				PolygonPoint pp;
				ArrayList<PolygonPoint> holePoints = new ArrayList<PolygonPoint>();
				p = winPoints.get(0);
				wireframeVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
				pp = new PolygonPoint(p.getX(), p.getY(), p.getZ());
				toXY.transform(pp);
				holePoints.add(pp);
				p = winPoints.get(2);
				wireframeVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
				pp = new PolygonPoint(p.getX(), p.getY(), p.getZ());
				toXY.transform(pp);
				holePoints.add(pp);
				p = winPoints.get(3);
				wireframeVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
				pp = new PolygonPoint(p.getX(), p.getY(), p.getZ());
				toXY.transform(pp);
				holePoints.add(pp);
				p = winPoints.get(1);
				wireframeVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
				pp = new PolygonPoint(p.getX(), p.getY(), p.getZ());
				toXY.transform(pp);
				holePoints.add(pp);
				polygon.addHole(new Polygon(holePoints));
			}
		}

		Vector3 p01 = points.get(1).subtract(points.get(0), null).normalizeLocal();
		Vector3 p02 = points.get(2).subtract(points.get(0), null).normalizeLocal();
		TPoint o = new TPoint(points.get(0).getX(), points.get(0).getY(), points.get(0).getZ());
		TPoint u = new TPoint(p01.getX(), p01.getY(), p01.getZ());
		TPoint v = new TPoint(p02.getX(), p02.getY(), p02.getZ());

		toXY.transform(o);
		toXY.transform(u);
		toXY.transform(v);

		try {
			if (Scene.getInstance().isDrawThickness() && isShortWall) {
				final Vector3 dir = abspoints.get(2).subtract(abspoints.get(0), null).normalizeLocal();

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

			drawBackMesh(polygon, fromXY);
			drawSurroundMesh(thicknessNormal);
			drawWindowsSurroundMesh(thicknessNormal);

			// draw wireframe
			// final FloatBuffer wireframeVertexBuffer = wireframeMesh.getMeshData().getVertexBuffer();
			// wireframeVertexBuffer.rewind();
			Vector3 w;
			w = abspoints.get(1);
			wireframeVertexBuffer.put(w.getXf()).put(w.getYf()).put(w.getZf() + 0.01f);
			w = abspoints.get(0);
			wireframeVertexBuffer.put(w.getXf()).put(w.getYf()).put(w.getZf() + 0.01f);
			w = abspoints.get(2);
			wireframeVertexBuffer.put(w.getXf()).put(w.getYf()).put(w.getZf());
			// w = abspoints.get(3);
			// wireframeVertexBuffer.put(w.getXf()).put(w.getYf()).put(w.getZf());
			if (wallGablePoints == null) {
				w = abspoints.get(3);
				wireframeVertexBuffer.put(w.getXf()).put(w.getYf()).put(w.getZf());
			}

			wireframeVertexBuffer.limit(wireframeVertexBuffer.position());

			backMesh.updateModelBound();
			surroundMesh.updateModelBound();
			windowsSurroundMesh.updateModelBound();
			wireframeMesh.updateModelBound();
			invisibleMesh.updateModelBound();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public boolean isPerpendicularToNeighbor(final int neighbor) {
		final Vector3 dir = abspoints.get(2).subtract(abspoints.get(0), null).normalizeLocal();
		final ArrayList<Vector3> abspoints = neighbors[neighbor].getNeighborOf(this).abspoints;
		final int i = neighbors[neighbor].getSnapPointIndexOfNeighborOf(this);
		final Vector3 otherDir = abspoints.get(i == 0 ? 2 : 0).subtract(abspoints.get(i), null).normalizeLocal();
		return Math.abs(dir.dot(otherDir)) < 0.1;
	}

	public boolean includeWindow(ArrayList<Vector3> winPoints) {
		return winPoints.size() >= 4 && winPoints.get(2).subtract(winPoints.get(0), null).length() >= 0.1;
	}

	private void drawBackMesh(final Polygon polygon, final XYToAnyTransform fromXY) {
		System.out.println("Before reduction:");
		for (TriangulationPoint p : polygon.getPoints())
			System.out.println("new PolygonPoint(" + p.getX() + ", " + p.getY() + ", " + p.getZ() + ")");

		final Vector3 dir = abspoints.get(2).subtract(abspoints.get(0), null).normalizeLocal();
		if (neighbors[0] != null && neighbors[0].getNeighborOf(this).isFirstPointInserted() && !(Scene.getInstance().isDrawThickness() && isShortWall && isPerpendicularToNeighbor(0)))
			reduceBackMeshWidth(polygon, dir, 0);

		if (neighbors[1] != null && neighbors[1].getNeighborOf(this).isFirstPointInserted() && !(Scene.getInstance().isDrawThickness() && isShortWall && isPerpendicularToNeighbor(1))) {
			dir.normalizeLocal().negateLocal();
			reduceBackMeshWidth(polygon, dir, 1);
		}
		

		// reduce height of backMesh by 0.5
		final TriangulationPoint d = new TPoint(0,0,0.5);
		fromXY.transform(d);
		for (int i = 4; i < polygon.pointCount(); i++) {
			final TriangulationPoint p = polygon.getPoints().get(i);		
			p.set(p.getX() + d.getX(), p.getY() + d.getY(), p.getZ() + d.getZ());
		}

		backMesh.setTranslation(getThicknessNormal());

		// Poly2Tri.triangulate(polygon);
		try {
			System.out.println("After reduction:");
			for (TriangulationPoint p : polygon.getPoints())
				System.out.println("new PolygonPoint(" + p.getX() + ", " + p.getY() + ", " + p.getZ() + ")");
			Poly2Tri.triangulate(polygon);
		} catch (RuntimeException e) {
			e.printStackTrace();
			System.out.println("Triangulate exception received with the following polygon:");
			for (TriangulationPoint p : polygon.getPoints())
				System.out.println("new PolygonPoint(" + p.getX() + ", " + p.getY() + ", " + p.getZ() + ")");
			throw e;
		}
		ArdorMeshMapper.updateTriangleMesh(backMesh, polygon, fromXY);
		ArdorMeshMapper.updateVertexNormals(backMesh, polygon.getTriangles(), fromXY);
		backMesh.getMeshData().updateVertexCount();
	}

	private void reduceBackMeshWidth(final Polygon polygon, final ReadOnlyVector3 wallDir, final int neighbor) {
		final Snap snap = neighbors[neighbor];
		final int neighborPointIndex = snap.getSnapPointIndexOfNeighborOf(this);
		final Wall otherWall = snap.getNeighborOf(this);
		final Vector3 otherWallDir = otherWall.getAbsPoints().get(neighborPointIndex == 0 ? 2 : 0).subtract(otherWall.getAbsPoints().get(neighborPointIndex), null).normalizeLocal();
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
		if (wallGablePoints != null) {
			final Vector3 gablePointPrj = new Vector3();
			final Vector3 neighborUpperPointPrj = new Vector3(abspoints.get(neighbor == 0 ? 1 : 3));
			neighborUpperPointPrj.setZ(0);
			int i = 4;
			for (final Vector3 gablePoint : wallGablePoints) {
				gablePointPrj.set(gablePoint).setZ(0);
				if (gablePointPrj.distance(neighborUpperPointPrj) < MathUtils.ZERO_TOLERANCE * 2) {
					final TriangulationPoint p = polygon.getPoints().get(i);
					p.set(p.getX() + v.getX(), p.getY() + v.getY(), p.getZ());
				}
				i++;
			}
		}

		// now reduce the actual wall points
		p1.set(p1.getX() + v.getX(), p1.getY() + v.getY(), p1.getZ());
		p2.set(p2.getX() + v.getX(), p2.getY() + v.getY(), p2.getZ());

		// final TriangulationPoint p1 = polygon.getPoints().get(neighbor == 0 ? 0 : 3);
		// p1.set(p1.getX() + v.getX(), p1.getY() + v.getY(), p1.getZ());
		// final TriangulationPoint p2 = polygon.getPoints().get(neighbor == 0 ? 1 : 2);
		// p2.set(p2.getX() + v.getX(), p2.getY() + v.getY(), p2.getZ());

		// double minX = Math.min(p1.getX(), p2.getX());
		// double maxX = Math.max(p1.getX(), p2.getX());
		// double minY = Math.min(polygon.getPoints().get(0).getY(), polygon.getPoints().get(1).getY());
		// double maxY = Math.max(polygon.getPoints().get(0).getY(), polygon.getPoints().get(1).getY());

		// for (int i=4; i<polygon.pointCount(); i++) {
		// final TriangulationPoint p = polygon.getPoints().get(i);
		// if (p.getX() < minX)
		// p.set(minX, p1.getY(), p1.getZ());
		// if (p.getX() > maxX)
		// p.set(maxX, p1.getY(), p1.getZ());
		//
		// if (p.getY() < minY)
		// p.set(p.getX(), minY, p1.getZ());
		// if (p.getY() > maxY)
		// p.set(p.getX(), maxY, p1.getZ());
		// }

	}

	public Vector3 getThicknessNormal() {
		if (thicknessNormal != null)
			return thicknessNormal;
		final ArrayList<Vector3> points = abspoints;
		cull(true);

		final Vector3 p02 = points.get(2).subtract(points.get(0), null).normalizeLocal();
		final Vector3 p01 = points.get(1).subtract(points.get(0), null).normalizeLocal();
		final Vector3 n = p02.crossLocal(p01).normalizeLocal();

		final Snap neighbor;
		// if (editPointIndex != -1)
		neighbor = neighbors[editPointIndex == 0 || editPointIndex == 1 ? 1 : 0];
		// else if (isFirstPointInserted())
		// neighbor = neighbors[1];
		// else
		// neighbor = neighbors[0];

		if (neighbor != null && neighbor.getNeighborOf(this).getAbsPoints().size() >= 4) {
			final ArrayList<Vector3> otherPoints = neighbor.getNeighborOf(this).getAbsPoints();
			final int otherPointIndex = neighbor.getSnapPointIndexOfNeighborOf(this);
			final Vector3 otherWallDir = otherPoints.get(otherPointIndex == 0 ? 2 : 0).subtract(otherPoints.get(otherPointIndex), null).normalizeLocal();

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
			final ReadOnlyVector3 p = abspoints.get(i);
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
		final Vector3 wallDirection = abspoints.get(2).subtract(abspoints.get(0), null);
		for (HousePart child : children) {
			final ArrayList<Vector3> winPoints = child.getAbsPoints();
			if (child instanceof Window && includeWindow(winPoints)) {
				int[] order = order1;
				final Vector3 windowDirection = winPoints.get(2).subtract(winPoints.get(0), null);
				if (windowDirection.dot(wallDirection) < 0)
					order = order2;
				for (int index = 0; index < order.length - 1; index++) {
					int i = order[index];
					p.set(winPoints.get(i));
					vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
					p.set(winPoints.get(i)).addLocal(thickness);
					vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
					i = order[index + 1];
					p.set(winPoints.get(i)).addLocal(thickness);
					vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
					p.set(winPoints.get(i));
					vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());

					if (index == 1 || index == 3) {
						int z = 1;
						if (index == 1)
							z = -z;
						final boolean reversedThickness = abspoints.get(1).subtract(abspoints.get(0), null).normalizeLocal().crossLocal(wallDirection.normalize(null)).dot(thicknessNormal.normalize(null)) >= 0;
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
					if (point.distance(part.getAbsPoints().get(0)) < 0.001) {
						newSnap = new Snap(this, wall, pointIndex, 0);
						wall.setNeighbor(0, newSnap, false);
						break;
					} else if (part.getAbsPoints().size() > 2 && point.distance(part.getAbsPoints().get(2)) < 0.001) {
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
				part.draw();
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

		fetchSizeAnnot(annotCounter++).setRange(abspoints.get(0), abspoints.get(2), center, faceDirection, original == null, original == null ? Align.South : Align.Center, true);
		if (original != null || neighbors[0] == null || !neighbors[0].isDrawn()) {
			fetchSizeAnnot(annotCounter++).setRange(abspoints.get(0), abspoints.get(1), center, faceDirection, original == null, Align.Center, true);
			if (neighbors[0] != null)
				neighbors[0].setDrawn();
		}
		if (original != null || neighbors[1] == null || !neighbors[1].isDrawn()) {
			fetchSizeAnnot(annotCounter++).setRange(abspoints.get(2), abspoints.get(3), center, faceDirection, original == null, Align.Center, true);
			if (neighbors[1] != null)
				neighbors[1].setDrawn();
		}
		if (original != null)
			fetchSizeAnnot(annotCounter++).setRange(abspoints.get(1), abspoints.get(3), center, faceDirection, original == null, Align.Center, true);

		for (int i = annotCounter; i < sizeAnnotRoot.getChildren().size(); i++)
			sizeAnnotRoot.getChild(i).getSceneHints().setCullHint(CullHint.Always);

		// Angle annotations
		annotCounter = 0;
		fetchAngleAnnot90(annotCounter++).setRange(abspoints.get(0), abspoints.get(2), abspoints.get(1), getFaceDirection());
		fetchAngleAnnot90(annotCounter++).setRange(abspoints.get(1), abspoints.get(3), abspoints.get(0), getFaceDirection());
		fetchAngleAnnot90(annotCounter++).setRange(abspoints.get(2), abspoints.get(0), abspoints.get(3), getFaceDirection());
		fetchAngleAnnot90(annotCounter++).setRange(abspoints.get(3), abspoints.get(1), abspoints.get(2), getFaceDirection());

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
		final ArrayList<Vector3> points = wall.abspoints;
		final Vector3 wallDir = points.get(pointIndex == 0 ? 2 : 0).subtract(points.get(pointIndex), null).normalizeLocal();

		final int otherPointIndex = prev.getSnapPointIndexOfNeighborOf(wall);
		final ArrayList<Vector3> otherPoints = prev.getNeighborOf(wall).getAbsPoints();
		final Vector3 otherWallDir = otherPoints.get(otherPointIndex == 0 ? 2 : 0).subtract(otherPoints.get(otherPointIndex), null).normalizeLocal();

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

	// public void drawNeighbors() {
	// for (final Snap snap : neighbors)
	// if (snap != null)
	// snap.getNeighborOf(this).draw();
	// }
}