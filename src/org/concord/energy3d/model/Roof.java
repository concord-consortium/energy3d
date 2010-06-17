package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.poly2tri.Poly2Tri;
import org.poly2tri.polygon.Polygon;
import org.poly2tri.polygon.PolygonPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public abstract class Roof extends HousePart {
	private static final long serialVersionUID = 1L;
	protected static final double GRID_SIZE = 0.5;
	transient Mesh mesh;
	private transient FloatBuffer vertexBuffer;
	protected double labelTop;
	private transient ArrayList<PolygonPoint> wallUpperPoints;

	public Roof(int numOfDrawPoints, int numOfEditPoints, double height) {
		super(numOfDrawPoints, numOfEditPoints, height);
	}

	protected void init() {
		super.init();
		mesh = new Mesh("Roof/Floor");
		vertexBuffer = BufferUtils.createVector3Buffer(4);
		root.attachChild(mesh);
		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(vertexBuffer);

		// Add a material to the box, to show both vertex color and lighting/shading.
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		mesh.setRenderState(ms);

		// Add a texture to the box.
		final TextureState ts = new TextureState();
		ts.setTexture(TextureManager.load("roof2.jpg", Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
		mesh.setRenderState(ts);

		mesh.setUserData(new UserData(this));
	}

	protected void updateMesh() {
		// if (root == null)
		// init();
		if (container == null)
			return;

		wallUpperPoints = exploreWallNeighbors((Wall) container);
		center.set(0, 0, 0);
		for (PolygonPoint p : wallUpperPoints)
			center.addLocal(p.getX(), p.getY(), p.getZ());
		center.multiplyLocal(1.0 / wallUpperPoints.size());

		final Polygon polygon = makePolygon(wallUpperPoints);
		Poly2Tri.triangulate(polygon);

		ArdorMeshMapper.updateTriangleMesh(mesh, polygon);
		ArdorMeshMapper.updateVertexNormals(mesh, polygon.getTriangles());
		ArdorMeshMapper.updateFaceNormals(mesh, polygon.getTriangles());
		ArdorMeshMapper.updateTextureCoordinates(mesh, polygon.getTriangles(), 1, 0);

		mesh.getMeshData().updateVertexCount();

		for (int i = 0; i < points.size(); i++) {
			Vector3 p = points.get(i);
			pointsRoot.getChild(i).setTranslation(p);
		}

		updateLabelLocation();

		if (flattenTime > 0)
			flatten();

		drawAnnotations();

		mesh.updateModelBound();
	}

	protected ArrayList<PolygonPoint> exploreWallNeighbors(Wall startWall) {
		ArrayList<PolygonPoint> poly = new ArrayList<PolygonPoint>();
		Wall currentWall = startWall;
		Wall prevWall = null;
		while (currentWall != null) {
			Snap next = currentWall.next(prevWall);
			prevWall = currentWall;
			if (next == null)
				break;
			currentWall = (Wall) next.getNeighborOf(currentWall);
			if (currentWall == startWall)
				break;
		}

		startWall = currentWall;
		prevWall = null;
		while (currentWall != null && currentWall.isFirstPointInserted()) {
			Snap next = currentWall.next(prevWall);
			int pointIndex = 0;
			if (next != null)
				pointIndex = next.getSnapPointIndexOf(currentWall);
			pointIndex = pointIndex + 1;
			addPointToPolygon(poly, currentWall.getPoints().get(pointIndex == 1 ? 3 : 1));
			addPointToPolygon(poly, currentWall.getPoints().get(pointIndex));
			prevWall = currentWall;
			if (next == null)
				break;
			currentWall = (Wall) next.getNeighborOf(currentWall);
			if (currentWall == startWall)
				break;
		}

		return poly;
	}

	private void addPointToPolygon(ArrayList<PolygonPoint> poly, Vector3 p) {
		PolygonPoint polygonPoint = new PolygonPoint(p.getX(), p.getY(), p.getZ());
		if (!poly.contains(polygonPoint)) {
			poly.add(polygonPoint);
		}
	}

	protected Polygon makePolygon(ArrayList<PolygonPoint> wallUpperPoints) {
		return new Polygon(wallUpperPoints);
	}

	protected void flatten() {
		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		final FloatBuffer orgVertexBuffer = ((Roof)original).mesh.getMeshData().getVertexBuffer();
		final Vector3 p1 = Vector3.fetchTempInstance();
		final Vector3 p2 = Vector3.fetchTempInstance();
		final Vector3 p3 = Vector3.fetchTempInstance();

		float pos = 0;
		for (int i = 0; i < vertexBuffer.capacity() / 9; i++) {
			pos += 0.5;
				final int xPos = i * 9;
				orgVertexBuffer.position(xPos);
				p1.set(orgVertexBuffer.get(), orgVertexBuffer.get(), orgVertexBuffer.get());
				p2.set(orgVertexBuffer.get(), orgVertexBuffer.get(), orgVertexBuffer.get());
				p3.set(orgVertexBuffer.get(), orgVertexBuffer.get(), orgVertexBuffer.get());
				flattenTriangle(p1, p2, p3, printSequence + i);
				vertexBuffer.position(xPos);
				vertexBuffer.put(p1.getXf()).put(p1.getYf()).put(p1.getZf());
				vertexBuffer.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
				vertexBuffer.put(p3.getXf()).put(p3.getYf()).put(p3.getZf());
		}

		Vector3.releaseTempInstance(p1);
		Vector3.releaseTempInstance(p2);
		Vector3.releaseTempInstance(p3);

		root.setRotation((new Matrix3().fromAngles(flattenTime * Math.PI / 2, 0, 0)));
//		super.flatten();
	}

	private void flattenTriangle(Vector3 p1, Vector3 p2, Vector3 p3, int printSequence) {
		final Vector3 v = Vector3.fetchTempInstance();
		final Vector3 normal = Vector3.fetchTempInstance();		
		v.set(p3).subtractLocal(p1);
		normal.set(p2).subtractLocal(p1).crossLocal(v);
		normal.normalizeLocal();		
		double angle = flattenTime * normal.smallestAngleBetween(Vector3.UNIT_Z);
		v.set(p3).subtractLocal(p1).normalizeLocal();
		normal.crossLocal(Vector3.UNIT_Z);
		final Matrix3 m = Matrix3.fetchTempInstance().fromAngleAxis(angle, normal);
		m.applyPost(p1, p1);
		m.applyPost(p2, p2);
		m.applyPost(p3, p3);
		
//		root.setTranslation(0, 0, 0);
		Vector3 targetCenter = new Vector3(printSequence % PRINT_COLS * PRINT_SPACE, printSequence / PRINT_COLS * PRINT_SPACE, 0);
		Vector3 currentCenter = v.set(p1).addLocal(p2).addLocal(p3).multiplyLocal(1.0/3.0);
		final Vector3 d = targetCenter.subtractLocal(currentCenter).multiplyLocal(flattenTime);
//		root.setTranslation(d);
		p1.addLocal(d);
		p2.addLocal(d);
		p3.addLocal(d);
		
		Vector3.releaseTempInstance(v );
		Vector3.releaseTempInstance(normal);
		Matrix3.releaseTempInstance(m);
	}

	protected double computeLabelTop() {
		return labelTop;
	}

	protected ReadOnlyVector3 getFaceDirection() {
		return Vector3.UNIT_Z;
	}

	protected void drawAnnotations() {
		if (container == null)
			return;
		ReadOnlyVector3 faceDirection = getFaceDirection();
		int annotCounter = 0;
		Vector3 a = Vector3.fetchTempInstance();
		Vector3 b = Vector3.fetchTempInstance();

		if (flattenTime == 0) {
		for (int i = 0; i < wallUpperPoints.size(); i++) {
			PolygonPoint p = wallUpperPoints.get(i);
			a.set(p.getX(), p.getY(), p.getZ());
			p = wallUpperPoints.get((i + 1) % wallUpperPoints.size());
			b.set(p.getX(), p.getY(), p.getZ());
			drawAnnot(a, b, faceDirection, annotCounter++, Align.Center, true);
		}
		} else {
			final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
			final Vector3 p1 = Vector3.fetchTempInstance();
			final Vector3 p2 = Vector3.fetchTempInstance();
			final Vector3 p3 = Vector3.fetchTempInstance();

			float pos = 0;
			for (int i = 0; i < vertexBuffer.capacity() / 9; i++) {
				pos += 0.5;
					final int xPos = i * 9;
					System.out.println(i + " " + xPos);
					vertexBuffer.position(xPos);
					p1.set(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
					p2.set(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
					p3.set(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
//					flattenTriangle(p1, p2, p3, printSequence + i);
//					a.set(p2).subtractLocal(p1).crossLocal(Vector3.UNIT_Y).normalizeLocal();
					drawAnnot(p1, p2, Vector3.UNIT_Z, annotCounter++, Align.Center, false);
//					a.set(p3).subtractLocal(p2).crossLocal(Vector3.UNIT_Y).normalizeLocal();
					drawAnnot(p2, p3, Vector3.UNIT_Z, annotCounter++, Align.Center, false);
//					a.set(p1).subtractLocal(p3).crossLocal(Vector3.UNIT_Y).normalizeLocal();
					drawAnnot(p3, p1, Vector3.UNIT_Z, annotCounter++, Align.Center, false);
			}

			Vector3.releaseTempInstance(p1);
			Vector3.releaseTempInstance(p2);
			Vector3.releaseTempInstance(p3);
		}

		for (int i = annotCounter; i < annotRoot.getChildren().size(); i++)
			annotRoot.getChild(i).getSceneHints().setCullHint(CullHint.Always);

		Vector3.releaseTempInstance(a);
		Vector3.releaseTempInstance(b);
	}
	
	public int setPrintSequence(int printSequence) {
		super.setPrintSequence(printSequence);
		return mesh.getMeshData().getVertexCount() / 3;
	}
	
}
