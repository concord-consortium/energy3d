package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.concord.energy3d.scene.SceneManager;
import org.poly2tri.Poly2Tri;
import org.poly2tri.polygon.Polygon;
import org.poly2tri.polygon.PolygonPoint;
import org.poly2tri.transform.coordinate.AnyToXYTransform;
import org.poly2tri.transform.coordinate.XYToAnyTransform;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.CullState.Face;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public class Wall extends HousePart {
	private static final long serialVersionUID = 1L;
	private static final double GRID_SIZE = 0.5;
	private static double defaultWallHeight = 1f;
	private static CullState CULL_FRONT = new CullState();
	private static CullState CULL_BACK = new CullState();
	private double wallThickness = 0.1;
	private transient Mesh mesh;
	private transient Mesh backMesh;
	private transient Mesh surroundMesh;
	private transient Mesh invisibleMesh;
	private transient Mesh windowsSurroundMesh;
	private transient Mesh wireframeMesh;
	private Snap[] neighbors = new Snap[2];
	private transient boolean reversedThickness;
	private Vector3 thicknessNormal;
	private Roof roof;	

	static {
		CULL_FRONT.setCullFace(Face.Front);
		CULL_BACK.setCullFace(Face.Back);
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
		mesh.setModelBound(null);
		mesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);

		root.attachChild(backMesh);
		backMesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		backMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		backMesh.getMeshData().setTextureBuffer(BufferUtils.createVector2Buffer(4), 0);
		backMesh.setDefaultColor(ColorRGBA.LIGHT_GRAY);
		backMesh.setModelBound(null);
		backMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);

		root.attachChild(surroundMesh);
		surroundMesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		surroundMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(8));
		surroundMesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(8));
		surroundMesh.setDefaultColor(ColorRGBA.GRAY);
		surroundMesh.setModelBound(null);
		surroundMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);

		root.attachChild(invisibleMesh);
		invisibleMesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		invisibleMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		invisibleMesh.setModelBound(new BoundingBox());
		invisibleMesh.getSceneHints().setCullHint(CullHint.Always);

		root.attachChild(windowsSurroundMesh);
		windowsSurroundMesh.getMeshData().setIndexMode(IndexMode.Quads);
		windowsSurroundMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(1000));
		windowsSurroundMesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(1000));
		windowsSurroundMesh.setDefaultColor(ColorRGBA.GRAY);
		windowsSurroundMesh.setModelBound(null);
		windowsSurroundMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		
		root.attachChild(wireframeMesh);
		wireframeMesh.getMeshData().setIndexMode(IndexMode.Quads);
		wireframeMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		wireframeMesh.setModelBound(null);
		wireframeMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);		
		wireframeMesh.getSceneHints().setCastsShadows(false);
		wireframeMesh.setRenderState(new WireframeState());
		wireframeMesh.setDefaultColor(ColorRGBA.BLACK);

		// Add a texture to the box.
		final TextureState ts = new TextureState();
		ts.setTexture(TextureManager.load("wall7.jpg", Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
		mesh.setRenderState(ts);
		
		mesh.setDefaultColor(defaultColor);
		

		UserData userData = new UserData(this);
		mesh.setUserData(userData);
		backMesh.setUserData(userData);
		surroundMesh.setUserData(userData);
		invisibleMesh.setUserData(userData);
	}

	private Vector3 getUpperPoint(Vector3 p) {
		return new Vector3(p.getX(), p.getY(), height + points.get(0).getZ());
	}

	public void setPreviewPoint(int x, int y) {
		Snap.clearAnnotationDrawn();
		if (editPointIndex == -1 || editPointIndex == 0 || editPointIndex == 2) {
			final HousePart previousContainer = container;
			PickedHousePart picked = pick(x, y, new Class<?>[] { Foundation.class, null });
			if (container != previousContainer)
				for (int i=0; i<points.size(); i++)
					points.get(i).set(toRelative(abspoints.get(i)));
			if (picked != null) {
				Vector3 p = picked.getPoint();
				if (container != null)
					p.setZ(container.getHeight());
				int index = (editPointIndex == -1) ? points.size() - 2 : editPointIndex;
				Snap snap = snap(p, index);
				if (snap == null) {
					boolean foundationSnap = foundationSnap(p);
//				if (snap == null)
					if (!foundationSnap)
					p = grid(p, GRID_SIZE, false);
				}
				setNeighbor(index, snap, true);
				if (index == 2) // make sure z of 2nd base point is same as 2st (needed for platform picking side)
					p.setZ(points.get(0).getZ());
				Vector3 p_rel = toRelative(p);
				points.get(index).set(p_rel);
				points.get(index + 1).set(p_rel).setZ(p.getZ() + height);
			}
		} else if (editPointIndex == 1 || editPointIndex == 3) {
			int lower = (editPointIndex == 1) ? 0 : 2;
			Vector3 base = points.get(lower);
			Vector3 closestPoint = closestPoint(base, base.add(0, 0, 1, null), x, y);
			Snap snap = snap(closestPoint, -1);
			if (snap == null)
				closestPoint = grid(closestPoint, GRID_SIZE);
			// neighbor[1] = snap(closestPoint);
			defaultWallHeight = height = findHeight(base, closestPoint);
			points.set(1, getUpperPoint(points.get(1)));
			points.set(3, getUpperPoint(points.get(3)));

		}

		thicknessNormal = null;
		draw();
		showPoints();

//		for (HousePart child : children)
//			child.draw();
		
		for (Snap neighbor : this.neighbors)
			if (neighbor != null) {
				neighbor.getNeighborOf(this).draw();
			}
		
		if (roof != null)
			roof.draw();

	}

	// @Override
	// protected void updateMesh() {
	// super.draw();
	// // if (root == null)
	// // init();
	// //
	// // for (int i = 0; i < points.size(); i++) {
	// // Vector3 p = points.get(i);
	// // p = toAbsolute(p);
	// // pointsRoot.getChild(i).setTranslation(p);
	// // }
	//		
	// boolean drawable = points.size() >= 4 && !points.get(0).equals(points.get(2));
	//		
	// // System.out.println("rel = " + points.get(0));
	// // System.out.println("abs = " + toAbsolute(points.get(0)));
	//		
	// ArrayList<Vector3> points = abspoints;
	//
	// if (drawable) {
	// Vector3 normal = points.get(2).subtract(points.get(0), null).cross(points.get(1).subtract(points.get(0), null), null).normalize(null);
	//
	// ArrayList<PolygonPoint> polyPoints = new ArrayList<PolygonPoint>();
	//
	// FloatBuffer invisibleVertexBuffer = invisibleMesh.getMeshData().getVertexBuffer();
	// invisibleVertexBuffer.rewind();
	// Vector3 p;
	//			
	// p = points.get(0);
	// // p = toAbsolute(p);
	// // System.out.println("invis abs Y = " + p.getY());
	// invisibleVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
	// p = points.get(1);
	// // p = toAbsolute(p);
	// invisibleVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
	// p = points.get(2);
	// // p = toAbsolute(p);
	// // System.out.println("abs = " + p);
	// invisibleVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
	// p = points.get(3);
	// // p = toAbsolute(p);
	// invisibleVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
	//
	// p = points.get(0);
	// // p = toAbsolute(p);
	// polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
	// p = points.get(2);
	// // p = toAbsolute(p);
	// polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
	// p = points.get(3);
	// // p = toAbsolute(p);
	// polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
	// p = points.get(1);
	// // p = toAbsolute(p);
	// polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
	//
	// try {
	// AnyToXYTransform toXY = new AnyToXYTransform(normal.getX(), normal.getY(), normal.getZ());
	// XYToAnyTransform fromXY = new XYToAnyTransform(normal.getX(), normal.getY(), normal.getZ());
	//
	// for (TriangulationPoint tp : polyPoints)
	// toXY.transform(tp);
	//
	// Polygon polygon = new Polygon(polyPoints);
	//
	// for (HousePart child : children) {
	// if (child instanceof Window) {
	// Window win = (Window) child;
	// if (win.getPoints().size() < 4)
	// continue;
	// PolygonPoint pp;
	// ArrayList<PolygonPoint> holePoints = new ArrayList<PolygonPoint>();
	// ArrayList<Vector3> winPoints = child.getPoints();
	// p = winPoints.get(0);
	// pp = new PolygonPoint(p.getX(), p.getY(), p.getZ());
	// toXY.transform(pp);
	// holePoints.add(pp);
	// p = winPoints.get(2);
	// pp = new PolygonPoint(p.getX(), p.getY(), p.getZ());
	// toXY.transform(pp);
	// holePoints.add(pp);
	// p = winPoints.get(3);
	// pp = new PolygonPoint(p.getX(), p.getY(), p.getZ());
	// toXY.transform(pp);
	// holePoints.add(pp);
	// p = winPoints.get(1);
	// pp = new PolygonPoint(p.getX(), p.getY(), p.getZ());
	// toXY.transform(pp);
	// holePoints.add(pp);
	// polygon.addHole(new Polygon(holePoints));
	// }
	//
	// }
	//
	// Vector3 p01 = points.get(1).subtract(points.get(0), null).normalizeLocal(); // .multiplyLocal(1/TEXTURE_SCALE_Y);
	// Vector3 p02 = points.get(2).subtract(points.get(0), null).normalizeLocal(); // .multiplyLocal(1/TEXTURE_SCALE_X);
	// TPoint o = new TPoint(points.get(0).getX(), points.get(0).getY(), points.get(0).getZ());
	// TPoint u = new TPoint(p01.getX(), p01.getY(), p01.getZ());
	// TPoint v = new TPoint(p02.getX(), p02.getY(), p02.getZ());
	//
	// toXY.transform(o);
	// toXY.transform(u);
	// toXY.transform(v);
	//
	// Poly2Tri.triangulate(polygon);
	// ArdorMeshMapper.updateTriangleMesh(mesh, polygon, fromXY);
	// ArdorMeshMapper.updateVertexNormals(mesh, polygon.getTriangles(), fromXY);
	// ArdorMeshMapper.updateTextureCoordinates(mesh, polygon.getTriangles(), 1, o, u, v);
	// mesh.getMeshData().updateVertexCount();
	//
	// // if (!isFlatten()) {
	// Vector3 n = drawBackMesh(polygon, fromXY);
	// drawSurroundMesh(n);
	// drawWindowsSurroundMesh(n);
	// // }
	//
	// // force bound update
	// // mesh.updateModelBound();
	// // backMesh.updateModelBound();
	// // surroundMesh.updateModelBound();
	// // root.updateWorldBound(true);
	// // mesh.updateModelBound();
	// // backMesh.updateModelBound();
	// // surroundMesh.updateModelBound();
	// invisibleMesh.updateModelBound();
	// // root.updateGeometricState(0);
	// CollisionTreeManager.INSTANCE.removeCollisionTree(root);
	//					
	// for (HousePart child : children)
	// child.draw();
	//					
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }
	//
	// }

	private boolean foundationSnap(Vector3 current) {
		if (container == null)
			return false;
		Vector3 snapPoint = null;
		double snapDistance = Double.MAX_VALUE;
		for (Vector3 p : container.getPoints()) {
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

	protected void updateMesh() {
		final boolean drawable = points.size() >= 4 && !points.get(0).equals(points.get(2));
		if (!drawable)
			return;

		final ArrayList<Vector3> points = abspoints;
		final Vector3 normal = points.get(2).subtract(points.get(0), null).cross(points.get(1).subtract(points.get(0), null), null).normalize(null);

		final ArrayList<PolygonPoint> polyPoints = new ArrayList<PolygonPoint>();

		FloatBuffer invisibleVertexBuffer = invisibleMesh.getMeshData().getVertexBuffer();
		invisibleVertexBuffer.rewind();
		Vector3 p;

		p = points.get(0);
		invisibleVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
		p = points.get(1);
		invisibleVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
		p = points.get(2);
		invisibleVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
		p = points.get(3);
		invisibleVertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());

		p = points.get(0);
		polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
		p = points.get(2);
		polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
		p = points.get(3);
		polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
		p = points.get(1);
		polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));

		final AnyToXYTransform toXY = new AnyToXYTransform(normal.getX(), normal.getY(), normal.getZ());
		final XYToAnyTransform fromXY = new XYToAnyTransform(normal.getX(), normal.getY(), normal.getZ());

		for (TriangulationPoint tp : polyPoints)
			toXY.transform(tp);

		final Polygon polygon = new Polygon(polyPoints);

		// Add window holes
		for (HousePart child : children) {
			if (child instanceof Window) {
//				Window win = (Window) child;
				if (child.getPoints().size() < 4)
					continue;
				PolygonPoint pp;
				ArrayList<PolygonPoint> holePoints = new ArrayList<PolygonPoint>();
//				System.out.println("win[0] = " + child.getPoints().get(0));
				ArrayList<Vector3> winPoints = child.getPoints();
//				p = winPoints.get(0);
				p = winPoints.get(0);
				pp = new PolygonPoint(p.getX(), p.getY(), p.getZ());
				toXY.transform(pp);
				holePoints.add(pp);
				p = winPoints.get(2);
				pp = new PolygonPoint(p.getX(), p.getY(), p.getZ());
				toXY.transform(pp);
				holePoints.add(pp);
				p = winPoints.get(3);
				pp = new PolygonPoint(p.getX(), p.getY(), p.getZ());
				toXY.transform(pp);
				holePoints.add(pp);
				p = winPoints.get(1);
				pp = new PolygonPoint(p.getX(), p.getY(), p.getZ());
				toXY.transform(pp);
				holePoints.add(pp);
				polygon.addHole(new Polygon(holePoints));
			}

		}

		Vector3 p01 = points.get(1).subtract(points.get(0), null).normalizeLocal(); // .multiplyLocal(1/TEXTURE_SCALE_Y);
		Vector3 p02 = points.get(2).subtract(points.get(0), null).normalizeLocal(); // .multiplyLocal(1/TEXTURE_SCALE_X);
		TPoint o = new TPoint(points.get(0).getX(), points.get(0).getY(), points.get(0).getZ());
		TPoint u = new TPoint(p01.getX(), p01.getY(), p01.getZ());
		TPoint v = new TPoint(p02.getX(), p02.getY(), p02.getZ());

		toXY.transform(o);
		toXY.transform(u);
		toXY.transform(v);

		try {
			Poly2Tri.triangulate(polygon);
			ArdorMeshMapper.updateTriangleMesh(mesh, polygon, fromXY);
			ArdorMeshMapper.updateVertexNormals(mesh, polygon.getTriangles(), fromXY);
			ArdorMeshMapper.updateTextureCoordinates(mesh, polygon.getTriangles(), 1, o, u, v);
			mesh.getMeshData().updateVertexCount();

			// if (!isFlatten()) {
			Vector3 n = drawBackMesh(polygon, fromXY);
			drawSurroundMesh(n);
			drawWindowsSurroundMesh(n);
			// }

			invisibleMesh.updateModelBound();
			// CollisionTreeManager.INSTANCE.removeCollisionTree(root);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		// draw wireframe
		final FloatBuffer buf = wireframeMesh.getMeshData().getVertexBuffer();
		buf.rewind();
		Vector3 w;
		w = abspoints.get(0);
		buf.put(w.getXf()).put(w.getYf()).put(w.getZf());
		w = abspoints.get(2);
		buf.put(w.getXf()).put(w.getYf()).put(w.getZf());
		w = abspoints.get(3);
		buf.put(w.getXf()).put(w.getYf()).put(w.getZf());
		w = abspoints.get(1);
		buf.put(w.getXf()).put(w.getYf()).put(w.getZf());

		// keep it for platform resizing
		if (original == null)
		for (HousePart child : children)
			child.draw();
				
	}

	private Vector3 drawBackMesh(Polygon polygon, XYToAnyTransform fromXY) {
		ArrayList<Vector3> points = abspoints;
		Vector3 dir = points.get(2).subtract(points.get(0), null).normalizeLocal();
		if (neighbors[0] != null && neighbors[0].getNeighborOf(this).isFirstPointInserted())
			reduceBackMeshWidth(polygon, dir, 0);

		if (neighbors[1] != null && neighbors[1].getNeighborOf(this).isFirstPointInserted()) {
			dir.normalizeLocal().negateLocal();
			reduceBackMeshWidth(polygon, dir, 1);
		}

		Poly2Tri.triangulate(polygon);
		ArdorMeshMapper.updateTriangleMesh(backMesh, polygon, fromXY);
		ArdorMeshMapper.updateVertexNormals(backMesh, polygon.getTriangles(), fromXY);
		backMesh.getMeshData().updateVertexCount();

		thicknessNormal = decideThicknessNormal();

		backMesh.setTranslation(thicknessNormal);
		return thicknessNormal;
	}

	private void reduceBackMeshWidth(Polygon polygon, final Vector3 dir, final int neighbor) {
		final int neighborPointIndex = neighbors[neighbor].getSnapPointIndexOfNeighborOf(this);
		ArrayList<Vector3> points2 = neighbors[neighbor].getNeighborOf(this).getPoints();
		Vector3 dir2 = points2.get(neighborPointIndex == 0 ? 2 : 0).subtract(points2.get(neighborPointIndex), null).normalizeLocal();
		final double angle = Math.max(0.1, dir2.smallestAngleBetween(dir) / 2);
		dir.multiplyLocal(wallThickness * Math.sin(Math.PI / 2 - angle) / Math.sin(angle));
		TriangulationPoint p = polygon.getPoints().get(neighbor == 0 ? 0 : 1);
		p.set(p.getX() + dir.getX(), p.getY() + dir.getY(), p.getZ());
		p = polygon.getPoints().get(neighbor == 0 ? 3 : 2);
		p.set(p.getX() + dir.getX(), p.getY() + dir.getY(), p.getZ());
	}

	private Vector3 decideThicknessNormal() {
		final ArrayList<Vector3> points = abspoints;
		cull(true);

		Vector3 p02 = points.get(2).subtract(points.get(0), null).normalizeLocal();
		Vector3 p01 = points.get(1).subtract(points.get(0), null).normalizeLocal();
		Vector3 n = p02.crossLocal(p01).normalizeLocal();
		n.multiplyLocal(wallThickness);

		reversedThickness = false;

		Snap neighbor = this.neighbors[0];
		if (neighbor == null)
			neighbor = this.neighbors[1];

		if (neighbor != null && neighbor.getNeighborOf(this).getPoints().size() >= 4) {
			Wall otherWall = (Wall) neighbor.getNeighborOf(this);
			ArrayList<Vector3> otherPoints = otherWall.getPoints();
			int otherPointIndex = neighbor.getSnapPointIndexOfNeighborOf(this);
			Vector3 a = otherPoints.get(otherPointIndex);
			Vector3 b = otherPoints.get(otherPointIndex == 0 ? 2 : 0);
			Vector3 ab = b.subtract(a, null).normalizeLocal();
			if (n.dot(ab) < 0) {
				n.negateLocal();
				reversedThickness = true;
				cull(false);
			}
		} else {
			ReadOnlyVector3 camera = SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getDirection();
			if (camera.dot(n) < 0) {
				n.negateLocal();
				reversedThickness = true;
				cull(false);
			}
		}
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

	private void drawSurroundMesh(ReadOnlyVector3 thickness) {
		final ArrayList<Vector3> points = abspoints;
		final FloatBuffer vertexBuffer = surroundMesh.getMeshData().getVertexBuffer();
		final FloatBuffer normalBuffer = surroundMesh.getMeshData().getNormalBuffer();
		vertexBuffer.position(0);
		normalBuffer.position(0);
		final Vector3 p2 = new Vector3();
		int[] order;

		if (neighbors[0] != null && neighbors[1] != null)
			order = new int[] { 1, 3 };
		else if (neighbors[0] != null)
			order = new int[] { 1, 3, 2 };
		else if (neighbors[1] != null)
			order = new int[] { 0, 1, 3 };
		else
			order = new int[] { 0, 1, 3, 2 };

		Vector3 sideNormal = thickness.cross(0, 0, 1, null).normalizeLocal();
		for (int i : order) {
			ReadOnlyVector3 p = points.get(i);
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
//		Vector3.releaseTempInstance(p2);
	}

	private void drawWindowsSurroundMesh(Vector3 thickness) {
		FloatBuffer vertexBuffer = windowsSurroundMesh.getMeshData().getVertexBuffer();
		FloatBuffer normalBuffer = windowsSurroundMesh.getMeshData().getNormalBuffer();
		vertexBuffer.rewind();
		normalBuffer.rewind();
		vertexBuffer.limit(vertexBuffer.capacity());
		normalBuffer.limit(vertexBuffer.capacity());
		final int[] order1 = new int[] { 0, 1, 3, 2, 0 };
		final int[] order2 = new int[] { 2, 3, 1, 0, 2 };
		Vector3 sideNormal = thickness.cross(0, 0, 1, null).normalizeLocal();
		Vector3 n = new Vector3();
		Vector3 p = new Vector3();
		final Vector3 wallDirection = abspoints.get(2).subtract(abspoints.get(0), null);
		for (HousePart child : children) {
			if (child instanceof Window && child.isFirstPointInserted()) {
				int[] order = order1;
				Vector3 windowDirection = child.getPoints().get(2).subtract(child.getPoints().get(0), null);
				if (windowDirection.dot(wallDirection) < 0)
					order = order2;
				for (int index = 0; index < order.length - 1; index++) {
					int i = order[index];
					p.set(child.getPoints().get(i));
					vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
					p.set(child.getPoints().get(i)).addLocal(thickness);
					vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
					i = order[index + 1];
					p.set(child.getPoints().get(i)).addLocal(thickness);
					vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
					p.set(child.getPoints().get(i));
					vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());

					if (index == 1 || index == 3) {
						int z = 1;
						if (index == 1)
							z = -z;
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
		int pos = vertexBuffer.position();
		vertexBuffer.limit(pos != 0 ? pos : 1);
	}

	public Snap next(Wall previous) {
		for (Snap s : neighbors)
			if (s != null && s.getNeighborOf(this) != previous)
				return s;
		return null;
	}

	private void setNeighbor(int pointIndex, Snap newNeighbor, boolean updateNeighbors) {
		int i = pointIndex < 2 ? 0 : 1;
		Snap oldNeighbor = neighbors[i];
		if (updateNeighbors || oldNeighbor == null) // do not update if already has neighbor, unless this update was initiated by this wall
			neighbors[i] = newNeighbor;

		if (!updateNeighbors || oldNeighbor == newNeighbor || (oldNeighbor != null && oldNeighbor.equals(newNeighbor)))
			return;

		if (oldNeighbor != null)
			((Wall) oldNeighbor.getNeighborOf(this)).removeNeighbor(oldNeighbor.getSnapPointIndexOfNeighborOf(this), pointIndex, this);

		if (newNeighbor != null)
			((Wall) newNeighbor.getNeighborOf(this)).setNeighbor(newNeighbor.getSnapPointIndexOfNeighborOf(this), newNeighbor, false);
	}

	private void removeNeighbor(int pointIndex, int requestingPointIndex, Wall wall) {
		int i = pointIndex < 2 ? 0 : 1;
		if (neighbors[i] != null && neighbors[i].getNeighborOf(this) == wall && neighbors[i].getSnapPointIndexOfNeighborOf(this) == requestingPointIndex)
			neighbors[i] = null;
		draw();
	}

	public void delete() {
		for (int i = 0; i < neighbors.length; i++)
			if (neighbors[i] != null)
				((Wall) neighbors[i].getNeighborOf(this)).setNeighbor(neighbors[i].getSnapPointIndexOfNeighborOf(this), null, false); // .removeNeighbor(this);
	}

	public void setHeight(double newHeight, boolean finalize) {
		super.setHeight(newHeight, finalize);
		points.get(1).setZ(newHeight);
		if (isFirstPointInserted())
			points.get(3).setZ(newHeight);
		draw();
	}

	protected void flatten() {
		thicknessNormal = decideThicknessNormal();
		Vector3 n = thicknessNormal.normalize(null);
		double angle = n.smallestAngleBetween(Vector3.UNIT_X);
		angle -= Math.PI / 2;

		if (n.dot(Vector3.UNIT_Y) < 0)
			angle = Math.PI - angle;

		root.setRotation((new Matrix3().fromAngles(0, 0, -flattenTime * angle)));
		super.flatten();
	}

	public ReadOnlyVector3 getFaceDirection() {
		if (thicknessNormal == null)
			thicknessNormal = decideThicknessNormal();
		return thicknessNormal.negate(null).normalizeLocal().multiplyLocal(0.5);
	}

	protected void drawAnnotations() {
		if (points.size() < 4)
			return;
		ReadOnlyVector3 faceDirection = getFaceDirection();
		int annotCounter = 0;
		
//		drawSizeAnnot(abspoints.get(0), abspoints.get(2), faceDirection, annotCounter++, original == null ? Align.South : Align.Center, true);
		fetchSizeAnnot(annotCounter++).setRange(abspoints.get(0), abspoints.get(2), center, faceDirection, original == null, original == null ? Align.South : Align.Center, true);
		if (original != null || neighbors[0] == null || !neighbors[0].isDrawn()) {
//			drawSizeAnnot(abspoints.get(0), abspoints.get(1), faceDirection, annotCounter++, Align.Center, true);
			fetchSizeAnnot(annotCounter++).setRange(abspoints.get(0), abspoints.get(1), center, faceDirection, original == null, Align.Center, true);
			if (neighbors[0] != null)
				neighbors[0].setDrawn();
		}
		if (original != null || neighbors[1] == null || !neighbors[1].isDrawn()) {
//			drawSizeAnnot(abspoints.get(2), abspoints.get(3), faceDirection, annotCounter++, Align.Center, true);
			fetchSizeAnnot(annotCounter++).setRange(abspoints.get(2), abspoints.get(3), center, faceDirection, original == null, Align.Center, true);
			if (neighbors[1] != null)
				neighbors[1].setDrawn();
		}
		if (original != null)
//			drawSizeAnnot(abspoints.get(1), abspoints.get(3), faceDirection, annotCounter++, Align.Center, true);
			fetchSizeAnnot(annotCounter++).setRange(abspoints.get(1), abspoints.get(3), center, faceDirection, original == null, Align.Center, true);

		for (int i = annotCounter; i < sizeAnnotRoot.getChildren().size(); i++)
			sizeAnnotRoot.getChild(i).getSceneHints().setCullHint(CullHint.Always);
		
		
		// Angle annotations
		annotCounter = 0;
		fetchAngleAnnot90(annotCounter++).setRange(abspoints.get(0), abspoints.get(2), abspoints.get(1));
		fetchAngleAnnot90(annotCounter++).setRange(abspoints.get(1), abspoints.get(3), abspoints.get(0));
		fetchAngleAnnot90(annotCounter++).setRange(abspoints.get(2), abspoints.get(0), abspoints.get(3));
		fetchAngleAnnot90(annotCounter++).setRange(abspoints.get(3), abspoints.get(1), abspoints.get(2));

	}

	public void setRoof(Roof roof) {
		this.roof = roof;
		
	}

	// private void drawAnnot(int a, int b, ReadOnlyVector3 faceDirection, int annotCounter, Align align) {
	// final SizeAnnotation annot;
	// if (annotCounter < annotRoot.getChildren().size()) {
	// annot = (SizeAnnotation) annotRoot.getChild(annotCounter);
	// annot.getSceneHints().setCullHint(CullHint.Inherit);
	// } else {
	// annot = new SizeAnnotation();
	// annotRoot.attachChild(annot);
	// }
	// annot.setRange(abspoints.get(a), abspoints.get(b), center, faceDirection, original == null, align);
	// }
}