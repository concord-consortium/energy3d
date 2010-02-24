package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.poly2tri.Poly2Tri;
import org.poly2tri.polygon.Polygon;
import org.poly2tri.polygon.PolygonPoint;
import org.poly2tri.position.AnyToXYTransform;
import org.poly2tri.position.XYToAnyTransform;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public class Wall extends HousePart {
	private double wallHeight = 0.8f;
	private double wallThickness = 0.1;
	private ArrayList<HousePart> children = new ArrayList<HousePart>();
	private Mesh mesh = new Mesh("Wall");
	private Mesh backMesh = new Mesh("Wall (Back)");
	private Mesh surroundMesh = new Mesh("Wall (Surround)");
	private FloatBuffer vertexBuffer = BufferUtils.createVector3Buffer(4);
	private FloatBuffer textureBuffer = BufferUtils.createVector2Buffer(4);
	private Snap[] neighbor = new Snap[2];
//	private Vector3 normal = new Vector3();

	public Wall() {
		super(2, 4);
		root.attachChild(mesh);
		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		// mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		mesh.getMeshData().setVertexBuffer(vertexBuffer);
		mesh.getMeshData().setTextureBuffer(textureBuffer, 0);

		root.attachChild(backMesh);
		backMesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		backMesh.getMeshData().setVertexBuffer(vertexBuffer);
		backMesh.getMeshData().setTextureBuffer(textureBuffer, 0);

		root.attachChild(surroundMesh);
		surroundMesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		surroundMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(8));
		surroundMesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(8));
		// surroundMesh.getMeshData().setTextureBuffer(textureBuffer, 0);

		// Add a material to the box, to show both vertex color and lighting/shading.
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		mesh.setRenderState(ms);

		// Add a texture to the box.
		final TextureState ts = new TextureState();
		ts.setTexture(TextureManager.load("brick_wall.jpg", Texture.MinificationFilter.Trilinear, Format.GuessNoCompression, true));
		mesh.setRenderState(ts);
		// backMesh.setRenderState(ts);

		mesh.setUserData(new UserData(this));

		allocateNewPoint();
	}

	public void addChild(HousePart housePart) {
		children.add(housePart);
	}

	public boolean removeChild(HousePart housePart) {
		return children.remove(housePart);
	}

	public void addPoint(int x, int y) {
		if (drawCompleted)
			throw new RuntimeException("Drawing of this object is already completed");

		if (points.size() >= numOfEditPoints)
			drawCompleted = true;
		else {
			allocateNewPoint();
			setPreviewPoint(x, y);
		}
	}

	private void allocateNewPoint() {
		Vector3 p = new Vector3();
		points.add(p);
		points.add(p);
	}

	private Vector3 getUpperPoint(Vector3 p) {
		return new Vector3(p.getX(), p.getY(), wallHeight);
	}

	public void setPreviewPoint(int x, int y) {
		if (editPointIndex == -1 || editPointIndex == 0 || editPointIndex == 2) {
			Vector3 p = SceneManager.getInstance().findMousePoint(x, y);
			if (p != null) {
				int index = (editPointIndex == -1) ? points.size() - 2 : editPointIndex;
				Snap snap = snap(p, index);
				setNeighbor(index == 0 ? 0 : 1, snap);
				if (snap != null)
					((Wall) snap.getHousePart()).setNeighbor(snap.getOtherPointIndex(), new Snap(this, index, snap.getOtherPointIndex()));
				points.set(index, p);
				points.set(index + 1, getUpperPoint(p));
			}
		} else if (editPointIndex == 1 || editPointIndex == 3) {
			int lower = (editPointIndex == 1) ? 0 : 2;
			Vector3 base = points.get(lower);
			Vector3 closestPoint = closestPoint(base, base.add(0, 0, 1, null), x, y);
			// neighbor[1] = snap(closestPoint);
			wallHeight = findHeight(base, closestPoint);
			points.set(1, getUpperPoint(points.get(1)));
			points.set(3, getUpperPoint(points.get(3)));

		}
		draw();
	}

	@Override
	protected void draw() {
		boolean drawable = points.size() >= 4;

		vertexBuffer.position(0);

		for (int i = 0; i < points.size(); i++) {
			Vector3 p = points.get(i);
			// if (drawable)
			// vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
			// polyPoints.add(new ArdorVector3PolygonPoint(p));

			// update location of point spheres
			pointsRoot.getChild(i).setTranslation(p);
			pointsRoot.setVisible(i, true);
		}

		if (drawable) {
			final float TEXTURE_SCALE_X = (float) points.get(2).subtract(points.get(0), null).length();
			final float TEXTURE_SCALE_Y = (float) points.get(3).subtract(points.get(2), null).length();
			// texture coords
			textureBuffer.position(0);
			textureBuffer.put(0).put(0);
			textureBuffer.put(0).put(TEXTURE_SCALE_Y);
			textureBuffer.put(TEXTURE_SCALE_X).put(0);
			textureBuffer.put(TEXTURE_SCALE_X).put(TEXTURE_SCALE_Y);

			Vector3 normal = points.get(3).subtract(points.get(1), null).cross(points.get(2).subtract(points.get(1), null), null).normalize(null);

			ArrayList<PolygonPoint> polyPoints = new ArrayList<PolygonPoint>();
			// polyPoints.add(new PolygonPoint(0,0,0));
			// polyPoints.add(new PolygonPoint(1,0,0));
			// polyPoints.add(new PolygonPoint(1,1,0));
			// polyPoints.add(new PolygonPoint(0,1,0));

			Vector3 p;
			p = points.get(0);
			polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
			p = points.get(2);
			polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
			p = points.get(3);
			polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
			p = points.get(1);
			polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));

			// polyPoints.add(new PolygonPoint(-1,-1));
			// polyPoints.add(new PolygonPoint(-1,1));
			// polyPoints.add(new PolygonPoint(1,1));
			// polyPoints.add(new PolygonPoint(1,0.9));

			try {
				Vector3 v = normal;
				AnyToXYTransform toXY = new AnyToXYTransform(v.getX(), v.getY(), v.getZ());
				XYToAnyTransform fromXY = new XYToAnyTransform(v.getX(), v.getY(), v.getZ());
				//			        
				for (TriangulationPoint tp : polyPoints)
					toXY.transform(tp);

				Polygon polygon = new Polygon(polyPoints);

				for (HousePart child : children) {
					// if (!child.isDrawCompleted())
					// continue;
					Window win = (Window) child;
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

				Poly2Tri.triangulate(polygon);
				ArdorMeshMapper.updateTriangleMesh(mesh, polygon, fromXY);
				// ArdorMeshMapper.updateVertexNormals(mesh, polygon.getTriangles(), fromXY);
				ArdorMeshMapper.updateFaceNormals(mesh, polygon.getTriangles(), fromXY);
				ArdorMeshMapper.updateTextureCoordinates(mesh, polygon.getTriangles(), 1, 0);

				Poly2Tri.triangulate(polygon);
				ArdorMeshMapper.updateTriangleMesh(backMesh, polygon, fromXY);
				ArdorMeshMapper.updateFaceNormals(backMesh, polygon.getTriangles(), fromXY);
				ArdorMeshMapper.updateTextureCoordinates(backMesh, polygon.getTriangles(), 1, 0);

				Vector3 n = decideThicknessNormal();
				
				System.out.println("Thickness Normal = " + n);

				backMesh.setTranslation(n);

				drawSurroundMesh(n);

			} catch (Exception e) {
				e.printStackTrace();
				// mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
			}

			// root.detachChild(backMesh);
			// backMesh = (Mesh) mesh..clone();
			// backMesh.setTranslation(0, 0.1, 0);
			// root.attachChild(backMesh);

			// force bound update
			CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);

			for (HousePart child : children)
				child.draw();
			
//			for (Snap neighbor : this.neighbor)
//				if (neighbor != null)
//					neighbor.getHousePart().draw();
		}

	}

	private Vector3 decideThicknessNormal() {
		FloatBuffer normalBuffer = mesh.getMeshData().getNormalBuffer();
		normalBuffer.position(0);
//		Vector3 n = new Vector3(normalBuffer.get(), normalBuffer.get(), normalBuffer.get());
		Vector3 p02 = points.get(2).subtract(points.get(0), null).normalizeLocal();
		Vector3 p01 = points.get(1).subtract(points.get(0), null).normalizeLocal();
		Vector3 n = p02.crossLocal(p01).normalizeLocal();
		System.out.println("normal = " + n);
//		n.negateLocal();
//		normal.set(n);
		n.multiplyLocal(wallThickness);

		Snap neighbor = this.neighbor[0];
		if (neighbor == null)
			neighbor = this.neighbor[1];

		if (neighbor != null) {
			Wall otherWall = (Wall) neighbor.getHousePart();
			ArrayList<Vector3> otherPoints = otherWall.getPoints();
			int otherPointIndex = neighbor.getOtherPointIndex();
			Vector3 a = otherPoints.get(otherPointIndex);
			Vector3 b = otherPoints.get(otherPointIndex == 0 ? 2 : 0);
			Vector3 ab = b.subtract(a, null).normalizeLocal();
			if (n.dot(ab) < 0) {
				n.negateLocal();
				System.out.println("REVERSE");
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
		vertexBuffer.position(0);
		Vector3 p2 = Vector3.fetchTempInstance();
		int[] order = { 0, 1, 3, 2 };
		for (int i : order) {
			ReadOnlyVector3 p = points.get(i);
			vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
			p2.set(p).addLocal(thickness);
			vertexBuffer.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
		}
		Vector3.releaseTempInstance(p2);
		surroundMesh.setRandomColors();
	}

	public Snap next(Wall previous) {
		// for (int i = 0; i < neighbor.length; i++)
		// if (neighbor[i] != null && neighbor[i].getHousePart() != previous)
		// return new Snap(neighbor[i].getHousePart(), i);
		for (Snap s : neighbor)
			if (s != null && s.getHousePart() != previous)
				return s;

		return null;
	}

	private void setNeighbor(int pointIndex, Snap snap) {
		int i = pointIndex < 2 ? 0 : 1;
		 if (neighbor[i] != null && !neighbor[i].equals(snap))
		 ((Wall)neighbor[i].getHousePart()).removeNeighbor(this);
//		if (neighbor[i] == null)
			neighbor[i] = snap;
	}

	private void removeNeighbor(Wall wall) {
		for (int i = 0; i < neighbor.length; i++)
			if (neighbor[i] != null && neighbor[i].getHousePart() == wall)
				neighbor[i] = null;
	}

	public void destroy() {
		for (int i = 0; i < neighbor.length; i++)
			if (neighbor[i] != null)
				((Wall) neighbor[i].getHousePart()).removeNeighbor(this);
	}

//	public ReadOnlyVector3 getNormal() {
//		return normal;
//	}

}