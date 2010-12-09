package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;
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
import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
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
	private static final double MIN_WALL_LENGTH = 0.1;
	private static double defaultWallHeight = 1f;
	private final double wallThickness = 0.1;
	private final Snap[] neighbors = new Snap[2];
	private Vector3 thicknessNormal;
//	private transient Mesh mesh;
	private transient Mesh backMesh;
	private transient Mesh surroundMesh;
	private transient Mesh invisibleMesh; // used to be called invisibleMesh
	private transient Mesh windowsSurroundMesh;
	private transient Mesh wireframeMesh;

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
//		mesh.setModelBound(null);
		mesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);

		root.attachChild(backMesh);
		backMesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		backMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		backMesh.getMeshData().setTextureBuffer(BufferUtils.createVector2Buffer(4), 0);
		backMesh.setDefaultColor(ColorRGBA.LIGHT_GRAY);
//		backMesh.setModelBound(null);
		backMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);

		root.attachChild(surroundMesh);
		surroundMesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		surroundMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(8));
		surroundMesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(8));
		surroundMesh.setDefaultColor(ColorRGBA.GRAY);
//		surroundMesh.setModelBound(null);
		surroundMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);

		root.attachChild(invisibleMesh);
		invisibleMesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		invisibleMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
//		invisibleMesh.setModelBound(new BoundingSphere());
//		invisibleMesh.setModelBound(new BoundingBox());
		invisibleMesh.getSceneHints().setCullHint(CullHint.Always);

		root.attachChild(windowsSurroundMesh);
		windowsSurroundMesh.getMeshData().setIndexMode(IndexMode.Quads);
		windowsSurroundMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(1000));
		windowsSurroundMesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(1000));
		windowsSurroundMesh.setDefaultColor(ColorRGBA.GRAY);
//		windowsSurroundMesh.setModelBound(null);
		windowsSurroundMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);

		root.attachChild(wireframeMesh);
		wireframeMesh.getMeshData().setIndexMode(IndexMode.Quads);
		wireframeMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
//		wireframeMesh.setModelBound(null);
		wireframeMesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);
		wireframeMesh.getSceneHints().setCastsShadows(false);
		wireframeMesh.setRenderState(new WireframeState());
		wireframeMesh.setDefaultColor(ColorRGBA.BLACK);

		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		mesh.setRenderState(ms);
		// surroundMesh.setRenderState(ms);

		updateTexture(Scene.getInstance().isTextureEnabled());

		UserData userData = new UserData(this);
		mesh.setUserData(userData);
		backMesh.setUserData(userData);
		surroundMesh.setUserData(userData);
		invisibleMesh.setUserData(userData);

		// code commented because it sets neighbors to null when Print Preview creates clones of it that are not added to instance.parts
//		if (neighbors != null)
//			for (int i = 0; i < neighbors.length; i++)
//				if (neighbors[i] != null && !Scene.getInstance().getParts().contains(neighbors[i].getNeighborOf(this)))
//					neighbors[i] = null;

	}

//	public void updateTexture() {
//		if (textureEnabled) {
//			final TextureState ts = new TextureState();
//			ts.setTexture(TextureManager.load("wall7.jpg", Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
//			frontMesh.setRenderState(ts);
//			frontMesh.setDefaultColor(ColorRGBA.WHITE);
//		} else {
//			frontMesh.clearRenderState(StateType.Texture);
//			frontMesh.setDefaultColor(defaultColor);
//		}
//	}

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
//			Vector3 base = points.get(lower);
			Vector3 base = abspoints.get(lower);
//			Vector3 closestPoint = closestPoint(base, base.add(0, 0, 10, null), x, y);
			Vector3 closestPoint = closestPoint(base, Vector3.UNIT_Z, x, y);
			Snap snap = snap(closestPoint, -1);
			if (snap == null)
				closestPoint = grid(closestPoint, GRID_SIZE);
//			defaultWallHeight = height = findHeight(base, closestPoint);
			defaultWallHeight = height = Math.max(0.1, closestPoint.getZ() - base.getZ());
			final double z = height + base.getZ();
			points.get(1).setZ(z);;
			points.get(3).setZ(z);			
		}

		thicknessNormal = null;
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
				for (Vector3 p2 : wall.getPoints()) {
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
	
	private boolean isDrawable() {
		return points.size() >= 4 && points.get(0).subtract(points.get(2), null).length() > MIN_WALL_LENGTH;
	}

	protected void drawMesh() {
		if (!isDrawable())
			return;

		final ArrayList<Vector3> points = abspoints;
		final Vector3 normal = points.get(2).subtract(points.get(0), null).cross(points.get(1).subtract(points.get(0), null), null).normalize(null);

		final ArrayList<PolygonPoint> polyPoints = new ArrayList<PolygonPoint>();

		final FloatBuffer invisibleVertexBuffer = invisibleMesh.getMeshData().getVertexBuffer();
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

		// keep it for platform resizing
		if (original == null)
			for (HousePart child : children) {
				child.draw();
			}

		if (Util.DEBUG) {
			System.out.println("drawing polygon...");
			for (PolygonPoint x : polyPoints)
				System.out.print("(" + x.getX() + "," + x.getY() + "," + x.getZ() + ")-");
			System.out.println('\b');
			System.out.println("drawing holes...");
		}
		// Add window holes
		for (HousePart child : children) {
			ArrayList<Vector3> winPoints = child.getPoints();
			if (child instanceof Window && includeWindow(winPoints)) {
				PolygonPoint pp;
				ArrayList<PolygonPoint> holePoints = new ArrayList<PolygonPoint>();
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
				if (Util.DEBUG) {
					for (PolygonPoint x : holePoints)
						System.out.print(x.getX() + "," + x.getY() + "," + x.getZ() + ",");
				}
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
			mesh.updateModelBound();

			Vector3 n = drawBackMesh(polygon, fromXY);
			drawSurroundMesh(n);
			drawWindowsSurroundMesh(n);

			invisibleMesh.updateModelBound();
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

	}

	public boolean includeWindow(ArrayList<Vector3> winPoints) {
		return winPoints.size() >= 4 && winPoints.get(2).subtract(winPoints.get(0), null).length() >= 0.1;
	}

	private Vector3 drawBackMesh(final Polygon polygon, final XYToAnyTransform fromXY) {
		thicknessNormal = decideThicknessNormal();

		final ArrayList<Vector3> points = abspoints;
		final Vector3 dir = points.get(2).subtract(points.get(0), null).normalizeLocal();
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

		backMesh.setTranslation(thicknessNormal);
		return thicknessNormal;
	}

	private void reduceBackMeshWidth(final Polygon polygon, final ReadOnlyVector3 wallDir, final int neighbor) {
		final Snap snap = neighbors[neighbor];
		final int neighborPointIndex = snap.getSnapPointIndexOfNeighborOf(this);
		final Wall otherWall = snap.getNeighborOf(this);
		final Vector3 otherWallDir = otherWall.getPoints().get(neighborPointIndex == 0 ? 2 : 0).subtract(otherWall.getPoints().get(neighborPointIndex), null).normalizeLocal();
		final double angle = Math.max(0.1, otherWallDir.smallestAngleBetween(wallDir));
		final double angle360;
		if (wallDir.dot(otherWall.decideThicknessNormal().normalize(null)) < 0)
			angle360 = Math.PI + angle;
		else
			angle360 = angle;

		final boolean reverse = angle360 >= Math.PI;
		final double length = wallThickness * Math.tan((Math.PI - angle) / 2) * (reverse ? -1 : 1);

		final Vector3 v = wallDir.normalize(null).multiplyLocal(length);
		
		final TriangulationPoint p1 = polygon.getPoints().get(neighbor == 0 ? 0 : 1);
		p1.set(p1.getX() + v.getX(), p1.getY() + v.getY(), p1.getZ());
		final TriangulationPoint p2 = polygon.getPoints().get(neighbor == 0 ? 3 : 2);
		p2.set(p2.getX() + v.getX(), p2.getY() + v.getY(), p2.getZ());
		
	}

	private Vector3 decideThicknessNormal() {
		if (thicknessNormal != null)
			return thicknessNormal;
		final ArrayList<Vector3> points = abspoints;
		cull(true);

		final Vector3 p02 = points.get(2).subtract(points.get(0), null).normalizeLocal();
		final Vector3 p01 = points.get(1).subtract(points.get(0), null).normalizeLocal();
		final Vector3 n = p02.crossLocal(p01).normalizeLocal();

		final Snap neighbor;
		if (editPointIndex != -1)
			neighbor = neighbors[editPointIndex < 2 ? 0 : 1];
		else if (isFirstPointInserted())
			neighbor = neighbors[1];
		else
			neighbor = neighbors[0];
		
//		final Snap neighbor = neighbors[0] != null ? neighbors[0] : neighbors[1];

		if (neighbor != null && neighbor.getNeighborOf(this).getPoints().size() >= 4) {
			final ArrayList<Vector3> otherPoints = neighbor.getNeighborOf(this).getPoints();
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
		n.multiplyLocal(wallThickness);
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
		final ArrayList<Vector3> points = abspoints;
		final FloatBuffer vertexBuffer = surroundMesh.getMeshData().getVertexBuffer();
		final FloatBuffer normalBuffer = surroundMesh.getMeshData().getNormalBuffer();
		vertexBuffer.position(0);
		normalBuffer.position(0);
		final Vector3 p2 = new Vector3();
		final int[] order;

		if (neighbors[0] != null && neighbors[1] != null)
			order = new int[] { 1, 3 };
		else if (neighbors[0] != null)
			order = new int[] { 1, 3, 2 };
		else if (neighbors[1] != null)
			order = new int[] { 0, 1, 3 };
		else
			order = new int[] { 0, 1, 3, 2 };

		final Vector3 sideNormal = thickness.cross(0, 0, 1, null).normalizeLocal();
		for (int i : order) {
			final ReadOnlyVector3 p = points.get(i);
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
			final ArrayList<Vector3> winPoints = child.getPoints();
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

	public Snap next(Wall previous) {
		for (Snap s : neighbors)
			if (s != null && s.getNeighborOf(this) != previous)
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
					final Wall wall = (Wall)part;
					if (point.distance(part.getPoints().get(0)) < 0.001) {
						newSnap = new Snap(this, wall, pointIndex, 0);
						wall.setNeighbor(0, newSnap, false);
						break;
					} else if (part.getPoints().size() > 2 && point.distance(part.getPoints().get(2)) < 0.001) {
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
		for (HousePart child : children)
			Scene.getInstance().remove(child);
	}

	protected void setHeight(final double newHeight, final boolean finalize) {
		super.setHeight(newHeight, finalize);
		points.get(1).setZ(newHeight);
		if (isFirstPointInserted())
			points.get(3).setZ(newHeight);
//		draw();
	}

	public void flatten(double flattenTime) {
		thicknessNormal = decideThicknessNormal();
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
			thicknessNormal = decideThicknessNormal();
//		return thicknessNormal.negate(null).normalizeLocal().multiplyLocal(0.5);
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
		Wall prevWall = null;
		Snap.clearVisits();
		while (currentWall != null) {
			final Snap next = currentWall.next(prevWall);
			prevWall = currentWall;
			if (next == null || next.isVisited())
				break;
			currentWall = (Wall) next.getNeighborOf(currentWall);
			next.visit();
		}

		Snap.clearVisits();
		prevWall = null;
		Snap prev = null;
		while (currentWall != null) {
			final Snap next = currentWall.next(prevWall);

			visitor.visit(currentWall, prev, next);

			prevWall = currentWall;
			prev = next;
			if (next == null || next.isVisited())
				break;
			else {
				currentWall = (Wall) next.getNeighborOf(currentWall);
				next.visit();
			}
		}
	}

	protected void visitNeighborsForward(final boolean forward, final WallVisitor visitor) {
		Wall currentWall = this;
		Wall prevWall = null;

		Snap.clearVisits();
		Snap prev = neighbors[forward ? 1 : 0];
		if (prev != null)
			prevWall = prev.getNeighborOf(this);
		while (currentWall != null) {
			final Snap next = currentWall.next(prevWall);

			visitor.visit(currentWall, prev, next);

			prevWall = currentWall;
			prev = next;
			if (next == null || next.isVisited())
				break;
			else {
				currentWall = (Wall) next.getNeighborOf(currentWall);
				next.visit();
			}
		}
	}

	private void drawNeighborWalls() {
		final ArrayList<Wall> walls = new ArrayList<Wall>();
		
		visitNeighborsForward(true, new WallVisitor() {
			public void visit(Wall wall, Snap prev, Snap next) {
				visitWall(wall, prev);
				walls.add(wall);
			}
		});

		visitNeighborsForward(false, new WallVisitor() {
			public void visit(Wall wall, Snap prev, Snap next) {
				visitWall(wall, prev);
				walls.add(wall);
			}
		});
		
		for (Wall wall : walls)
			wall.draw();
	}

//	private void visitWall(final Wall wall, final Snap prev) {
//		if (wall == Wall.this) {
//			return;
//		}
////		final int j = prev.getSnapPointIndexOfNeighborOf(wall);
//		final int pointIndex = prev.getSnapPointIndexOf(wall);
//		final ArrayList<Vector3> points = wall.abspoints;
//		final Vector3 wallDir = points.get(pointIndex == 0 ? 2 : 0).subtract(points.get(pointIndex), null).normalizeLocal();
//
////		final int i = prev.getSnapPointIndexOf(wall);
//		final int otherPointIndex = prev.getSnapPointIndexOfNeighborOf(wall);
//		final ArrayList<Vector3> otherPoints = prev.getNeighborOf(wall).getPoints();
//		final Vector3 otherWallDir = otherPoints.get(otherPointIndex == 0 ? 2 : 0).subtract(otherPoints.get(otherPointIndex), null).normalizeLocal();
//		
//		final double angle = Util.angleBetween(otherWallDir, wallDir, Vector3.UNIT_Z);
//		final boolean normalsMustBeSameDirection = angle >= Math.PI / 2 && angle <= Math.PI * 3 / 2;
//
//		final Vector3 n1 = new Vector3(wall.thicknessNormal).normalizeLocal();
//		final Vector3 n2 = new Vector3(prev.getNeighborOf(wall).thicknessNormal).normalizeLocal();
//		final double dot = n1.dot(n2);
//		boolean reverse = false;
//		if (dot == 0) {
//			if (Util.angleBetween(n2, n1, Vector3.UNIT_Z) > Math.PI)
////			if (n1.dot(otherWallDir) < 0)
//				reverse = true;
//		} else {
//			if (normalsMustBeSameDirection && dot < 0)
//				reverse = true;
//			else if (!normalsMustBeSameDirection && dot > 0)
//				reverse = true;
//		}
//		
//		if (reverse)
//			wall.thicknessNormal.negateLocal();
//	}
	
	private void visitWall(final Wall wall, final Snap prev) {
		if (wall == Wall.this)
			return;
		final int pointIndex = prev.getSnapPointIndexOf(wall);
		final ArrayList<Vector3> points = wall.abspoints;
		final Vector3 wallDir = points.get(pointIndex == 0 ? 2 : 0).subtract(points.get(pointIndex), null).normalizeLocal();

		final int otherPointIndex = prev.getSnapPointIndexOfNeighborOf(wall);
		final ArrayList<Vector3> otherPoints = prev.getNeighborOf(wall).getPoints();
		final Vector3 otherWallDir = otherPoints.get(otherPointIndex == 0 ? 2 : 0).subtract(otherPoints.get(otherPointIndex), null).normalizeLocal();
		
		final Vector3 n1 = new Vector3(wall.thicknessNormal).normalizeLocal();
		final Vector3 n2 = new Vector3(prev.getNeighborOf(wall).thicknessNormal).normalizeLocal();
		final Vector3 add = n1.add(n2, null).normalizeLocal();
		
		final double dotWall1 = Math.signum(add.dot(wallDir));
		final double dotWall2 = Math.signum(add.dot(otherWallDir));
		final boolean reverse = dotWall1 != dotWall2;
		if (reverse || (dotWall1 == 0 && dotWall2 == 0 && n1.dot(n2) < 0))
			wall.thicknessNormal.negateLocal();
	}	

	protected String getDefaultTextureFileName() {
		return "wall.jpg";
	}
	
}