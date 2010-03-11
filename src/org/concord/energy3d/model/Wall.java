package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.concord.energy3d.scene.SceneManager;
import org.poly2tri.Poly2Tri;
import org.poly2tri.polygon.Polygon;
import org.poly2tri.polygon.PolygonPoint;
import org.poly2tri.position.AnyToXYTransform;
import org.poly2tri.position.XYToAnyTransform;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.point.TPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public class Wall extends HousePart {
	private static final long serialVersionUID = 1L;
	private static double defaultWallHeight = 1f;
	private double wallHeight = defaultWallHeight;
	private double wallThickness = 0.1;
	private transient Mesh mesh;
	private transient Mesh backMesh;
	private transient Mesh surroundMesh;
	private transient FloatBuffer vertexBuffer;
	private transient FloatBuffer textureBuffer;
	private Snap[] neighbor = new Snap[2];

	public Wall() {
		super(2, 4);

	}
	
	protected void init() {
		super.init();
		mesh = new Mesh("Wall");
		backMesh = new Mesh("Wall (Back)");
		surroundMesh = new Mesh("Wall (Surround)");
		vertexBuffer = BufferUtils.createVector3Buffer(4);
		textureBuffer = BufferUtils.createVector2Buffer(4);
		
		root.attachChild(mesh);
		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		// mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		mesh.getMeshData().setVertexBuffer(vertexBuffer);
		mesh.getMeshData().setTextureBuffer(textureBuffer, 0);

		root.attachChild(backMesh);
		backMesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		backMesh.getMeshData().setVertexBuffer(vertexBuffer);
		backMesh.getMeshData().setTextureBuffer(textureBuffer, 0);
		backMesh.setDefaultColor(ColorRGBA.LIGHT_GRAY);

		root.attachChild(surroundMesh);
		surroundMesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		surroundMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(8));
		surroundMesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(8));
		surroundMesh.setDefaultColor(ColorRGBA.GRAY);
		// surroundMesh.getMeshData().setTextureBuffer(textureBuffer, 0);

		// Add a material to the box, to show both vertex color and lighting/shading.
		// final MaterialState ms = new MaterialState();
		// ms.setColorMaterial(ColorMaterial.Diffuse);
		// mesh.setRenderState(ms);

		// ShadingState shadingState = new ShadingState();
		// shadingState.setShadingMode(ShadingMode.Flat);
		// surroundMesh.setRenderState(shadingState);

		// Add a texture to the box.
		final TextureState ts = new TextureState();
		ts.setTexture(TextureManager.load("brick_wall.jpg", Texture.MinificationFilter.Trilinear, Format.GuessNoCompression, true));
		mesh.setRenderState(ts);
		// backMesh.setRenderState(ts);

		UserData userData = new UserData(this);
		mesh.setUserData(userData);
		backMesh.setUserData(userData);
		surroundMesh.setUserData(userData);
	}

	private Vector3 getUpperPoint(Vector3 p) {
		return new Vector3(p.getX(), p.getY(), wallHeight + points.get(0).getZ());
	}

	public void setPreviewPoint(int x, int y) {
		System.out.println("moving wall...");
		if (editPointIndex == -1 || editPointIndex == 0 || editPointIndex == 2) {
			PickedHousePart picked = pick(x, y, new Class<?>[] {Foundation.class, null}); //Foundation.class);
			if (picked != null) {
				// container = picked.getUserData().getHousePart();
				Vector3 p = picked.getPoint();
				// if (p != null) {
				int index = (editPointIndex == -1) ? points.size() - 2 : editPointIndex;
				Snap snap = snap(p, index);
				setNeighbor(index, snap, true);
				points.set(index, p);
				points.set(index + 1, getUpperPoint(p));
			}
		} else if (editPointIndex == 1 || editPointIndex == 3) {
			int lower = (editPointIndex == 1) ? 0 : 2;
			Vector3 base = points.get(lower);
			Vector3 closestPoint = closestPoint(base, base.add(0, 0, 1, null), x, y);
			snap(closestPoint, -1);
			// neighbor[1] = snap(closestPoint);
			defaultWallHeight = wallHeight = findHeight(base, closestPoint);
			points.set(1, getUpperPoint(points.get(1)));
			points.set(3, getUpperPoint(points.get(3)));

		}
		draw();
		showPoints();

		for (Snap neighbor : this.neighbor)
			if (neighbor != null)
				neighbor.getNeighbor().draw();

	}

	@Override
	protected void draw() {
		boolean drawable = points.size() >= 4 && !points.get(0).equals(points.get(2));

		vertexBuffer.position(0);

		for (int i = 0; i < points.size(); i++) {
			Vector3 p = points.get(i);
			// update location of point spheres
			pointsRoot.getChild(i).setTranslation(p);
//			pointsRoot.getChild(i).updateWorldBound(true);
		}

		if (drawable) {
			Vector3 normal = points.get(2).subtract(points.get(0), null).cross(points.get(1).subtract(points.get(0), null), null).normalize(null);

			ArrayList<PolygonPoint> polyPoints = new ArrayList<PolygonPoint>();

			Vector3 p;
			p = points.get(0);
			polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
			p = points.get(2);
			polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
			p = points.get(3);
			polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
			p = points.get(1);
			polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));

			try {
				AnyToXYTransform toXY = new AnyToXYTransform(normal.getX(), normal.getY(), normal.getZ());
				XYToAnyTransform fromXY = new XYToAnyTransform(normal.getX(), normal.getY(), normal.getZ());

				for (TriangulationPoint tp : polyPoints)
					toXY.transform(tp);

				Polygon polygon = new Polygon(polyPoints);

				for (HousePart child : children) {
					if (child instanceof Window) {
						Window win = (Window) child;
						if (win.getPoints().size() < 4)
							continue;
						PolygonPoint pp;
						ArrayList<PolygonPoint> holePoints = new ArrayList<PolygonPoint>();
						ArrayList<Vector3> points = child.getPoints();
						p = win.convertFromWallRelativeToAbsolute(points.get(0));
						pp = new PolygonPoint(p.getX(), p.getY(), p.getZ());
						toXY.transform(pp);
						holePoints.add(pp);
						p = win.convertFromWallRelativeToAbsolute(points.get(2));
						pp = new PolygonPoint(p.getX(), p.getY(), p.getZ());
						toXY.transform(pp);
						holePoints.add(pp);
						p = win.convertFromWallRelativeToAbsolute(points.get(3));
						pp = new PolygonPoint(p.getX(), p.getY(), p.getZ());
						toXY.transform(pp);
						holePoints.add(pp);
						p = win.convertFromWallRelativeToAbsolute(points.get(1));
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

				Poly2Tri.triangulate(polygon);
				ArdorMeshMapper.updateTriangleMesh(mesh, polygon, fromXY);
				ArdorMeshMapper.updateVertexNormals(mesh, polygon.getTriangles(), fromXY);
				ArdorMeshMapper.updateTextureCoordinates(mesh, polygon.getTriangles(), 1, o, u, v);

				Poly2Tri.triangulate(polygon);
				ArdorMeshMapper.updateTriangleMesh(backMesh, polygon, fromXY);
				ArdorMeshMapper.updateVertexNormals(backMesh, polygon.getTriangles(), fromXY);

				Vector3 n = decideThicknessNormal();

				backMesh.setTranslation(n);

				drawSurroundMesh(n);

			} catch (Exception e) {
				e.printStackTrace();
			}

			// force bound update
//			mesh.updateModelBound();
//			backMesh.updateModelBound();
//			surroundMesh.updateModelBound();
//			root.updateWorldBound(true);
			root.updateGeometricState(0);
			CollisionTreeManager.INSTANCE.removeCollisionTree(root);

			for (HousePart child : children)
				child.draw();
		}

	}

	private Vector3 decideThicknessNormal() {
		FloatBuffer normalBuffer = mesh.getMeshData().getNormalBuffer();
		normalBuffer.position(0);
		// Vector3 n = new Vector3(normalBuffer.get(), normalBuffer.get(), normalBuffer.get());
		Vector3 p02 = points.get(2).subtract(points.get(0), null).normalizeLocal();
		Vector3 p01 = points.get(1).subtract(points.get(0), null).normalizeLocal();
		Vector3 n = p02.crossLocal(p01).normalizeLocal();
		n.multiplyLocal(wallThickness);

		Snap neighbor = this.neighbor[0];
		if (neighbor == null)
			neighbor = this.neighbor[1];

		if (neighbor != null && neighbor.getNeighbor().getPoints().size() >= 4) {
			Wall otherWall = (Wall) neighbor.getNeighbor();
			ArrayList<Vector3> otherPoints = otherWall.getPoints();
			int otherPointIndex = neighbor.getNeighborPointIndex();
			Vector3 a = otherPoints.get(otherPointIndex);
			Vector3 b = otherPoints.get(otherPointIndex == 0 ? 2 : 0);
			Vector3 ab = b.subtract(a, null).normalizeLocal();
			if (n.dot(ab) < 0) {
				n.negateLocal();
			}
		} else {
			ReadOnlyVector3 camera = SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getDirection();
			if (camera.dot(n) < 0)
				n.negateLocal();
		}
		return n;
	}

	private void drawSurroundMesh(ReadOnlyVector3 thickness) {
		FloatBuffer vertexBuffer = surroundMesh.getMeshData().getVertexBuffer();
		FloatBuffer normalBuffer = surroundMesh.getMeshData().getNormalBuffer();
		vertexBuffer.position(0);
		normalBuffer.position(0);
		Vector3 p2 = Vector3.fetchTempInstance();
		int[] order;
		if (neighbor[0] != null)
			order = new int[] { 1, 3, 2 };
		else if (neighbor[1] != null)
			order = new int[] { 0, 1, 3 };
		else if (neighbor[0] != null && neighbor[0] != null)
			order = new int[] { 1, 3 };
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
			} else if (i == 0) {
				normalBuffer.put(sideNormal.getXf()).put(sideNormal.getYf()).put(sideNormal.getZf());
				normalBuffer.put(sideNormal.getXf()).put(sideNormal.getYf()).put(sideNormal.getZf());
			} else if (i == 2) {
				sideNormal.negateLocal();
				normalBuffer.put(sideNormal.getXf()).put(sideNormal.getYf()).put(sideNormal.getZf());
				normalBuffer.put(sideNormal.getXf()).put(sideNormal.getYf()).put(sideNormal.getZf());
			}
		}

		while (vertexBuffer.position() < vertexBuffer.capacity())
			vertexBuffer.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
		Vector3.releaseTempInstance(p2);
	}

	public Snap next(Wall previous) {
		for (Snap s : neighbor)
			if (s != null && s.getNeighbor() != previous)
				return s;
		return null;
	}

	private void setNeighbor(int pointIndex, Snap newNeighbor, boolean updateNeighbors) {
		int i = pointIndex < 2 ? 0 : 1;
		Snap oldNeighbor = neighbor[i];
		if (updateNeighbors || oldNeighbor == null) // do not update if already has neighbor, unless this update was initiated by this wall
			neighbor[i] = newNeighbor;

		if (!updateNeighbors || oldNeighbor == newNeighbor || (oldNeighbor != null && oldNeighbor.equals(newNeighbor)))
			return;

		if (oldNeighbor != null)
			((Wall) oldNeighbor.getNeighbor()).removeNeighbor(oldNeighbor.getNeighborPointIndex(), pointIndex, this);

		if (newNeighbor != null)
			((Wall) newNeighbor.getNeighbor()).setNeighbor(newNeighbor.getNeighborPointIndex(), new Snap(this, newNeighbor.getNeighborPointIndex(), newNeighbor.getThisPointIndex()), false);
	}

	private void removeNeighbor(int pointIndex, int requestingPointIndex, Wall wall) {
		int i = pointIndex < 2 ? 0 : 1;
		if (neighbor[i] != null && neighbor[i].getNeighbor() == wall && neighbor[i].getNeighborPointIndex() == requestingPointIndex)
			neighbor[i] = null;
		draw();
	}

	public void destroy() {
		for (int i = 0; i < neighbor.length; i++)
			if (neighbor[i] != null)
				((Wall) neighbor[i].getNeighbor()).setNeighbor(neighbor[i].getNeighborPointIndex(), null, false); // .removeNeighbor(this);
	}

}