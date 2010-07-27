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
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public abstract class Roof extends HousePart {
	private static final long serialVersionUID = 1L;
	protected static final double GRID_SIZE = 0.5;
	private transient Mesh mesh;
	protected transient Mesh bottomMesh;
//	private transient FloatBuffer vertexBuffer;
	protected double labelTop;
	private transient ArrayList<PolygonPoint> wallUpperPoints;

	public Roof(int numOfDrawPoints, int numOfEditPoints, double height) {
		super(numOfDrawPoints, numOfEditPoints, height);
	}

	protected void init() {
		super.init();
		mesh = new Mesh("Roof/Floor");
		bottomMesh = new Mesh("Roof/Floor (bottom)");
//		vertexBuffer = BufferUtils.createVector3Buffer(4);
		root.attachChild(mesh);
		root.attachChild(bottomMesh);
		
		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));

		bottomMesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		bottomMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		
		// Add a material to the box, to show both vertex color and lighting/shading.
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		mesh.setRenderState(ms);
		bottomMesh.setRenderState(ms);

		// Add a texture to the box.
		final TextureState ts = new TextureState();
		ts.setTexture(TextureManager.load("roof2.jpg", Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
		mesh.setRenderState(ts);
		bottomMesh.setRenderState(ts);

		final UserData userData = new UserData(this);
		mesh.setUserData(userData);
		bottomMesh.setUserData(userData);
	}

	protected void computeAbsPoints() {
	}

	protected void computeCenter() {
	}

	protected void updateMesh() {
		if (container == null) {
			resetToZero(mesh.getMeshData().getVertexBuffer());
			if (bottomMesh != null)
				resetToZero(bottomMesh.getMeshData().getVertexBuffer());
			hidePoints();
			return;			
		}

		wallUpperPoints = exploreWallNeighbors((Wall) container);	

//		center.set(0, 0, 0);
//		for (PolygonPoint p : wallUpperPoints)
//			center.addLocal(p.getX(), p.getY(), p.getZ());
//		center.multiplyLocal(1.0 / wallUpperPoints.size());
//		points.get(0).set(center.getX(), center.getY(), center.getZ() + height);

//		final Polygon polygon = makePolygon(wallUpperPoints);
		if (bottomMesh != null)
			fillMeshWithPolygon(bottomMesh, new Polygon(wallUpperPoints));
		fillMeshWithPolygon(mesh, makePolygon(wallUpperPoints));

		for (int i = 0; i < points.size(); i++) {
			Vector3 p = points.get(i);
			pointsRoot.getChild(i).setTranslation(p);
		}

		// updateLabelLocation();

		// if (flattenTime > 0)
		// flatten();
		//
		// drawAnnotations();

		mesh.updateModelBound();
	}

	private void resetToZero(final FloatBuffer buff) {
		buff.rewind();
		while (buff.hasRemaining())
			buff.put(0);
	}

	private void fillMeshWithPolygon(Mesh mesh, Polygon polygon) {
		Poly2Tri.triangulate(polygon);
		ArdorMeshMapper.updateTriangleMesh(mesh, polygon);
		ArdorMeshMapper.updateVertexNormals(mesh, polygon.getTriangles());
		ArdorMeshMapper.updateFaceNormals(mesh, polygon.getTriangles());
		ArdorMeshMapper.updateTextureCoordinates(mesh, polygon.getTriangles(), 1, 0);
		mesh.getMeshData().updateVertexCount();
	}

	protected ArrayList<PolygonPoint> exploreWallNeighbors(Wall startWall) {
		ArrayList<PolygonPoint> poly = new ArrayList<PolygonPoint>();
		Wall currentWall = startWall;
		Wall prevWall = null;
		Snap.clearVisits();
		while (currentWall != null) {
			Snap next = currentWall.next(prevWall);
			prevWall = currentWall;
			if (next == null || next.isVisited())
				break;
			currentWall = (Wall) next.getNeighborOf(currentWall);
			next.visit();
//			if (currentWall == startWall)
//				break;
		}

		Snap.clearVisits();
		startWall = currentWall;
		prevWall = null;
		int i = 1;
		center.set(0, 0, 0);
		while (currentWall != null && currentWall.isFirstPointInserted()) {
			System.out.println("wall (" + i++ + "): " + currentWall);
			Snap next = currentWall.next(prevWall);
			int pointIndex = 0;
			if (next != null)
				pointIndex = next.getSnapPointIndexOf(currentWall);
			pointIndex = pointIndex + 1;
			final Vector3 p1 = currentWall.getPoints().get(pointIndex == 1 ? 3 : 1);
			final Vector3 p2 = currentWall.getPoints().get(pointIndex);
			addPointToPolygon(poly, p1);
			addPointToPolygon(poly, p2);
			prevWall = currentWall;
			
			currentWall.setRoof(this);
			
			if (next == null || next.isVisited())
				break;
			currentWall = (Wall) next.getNeighborOf(currentWall);
			next.visit();
//			if (currentWall == startWall)
//				break;
		}
		
		center.multiplyLocal(1.0 / poly.size());
		points.get(0).set(center.getX(), center.getY(), center.getZ() + height);		

		return poly;
	}

	private void addPointToPolygon(ArrayList<PolygonPoint> poly, Vector3 p) {
		PolygonPoint polygonPoint = new PolygonPoint(p.getX(), p.getY(), p.getZ());
		if (!poly.contains(polygonPoint)) {
			poly.add(polygonPoint);
			center.addLocal(p);
		}
	}

	protected Polygon makePolygon(ArrayList<PolygonPoint> wallUpperPoints) {
		return new Polygon(wallUpperPoints);
	}

	protected void flatten() {
		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		if (original == null)
			original = null;
		final FloatBuffer orgVertexBuffer = ((Roof) original).mesh.getMeshData().getVertexBuffer();
		final Vector3 p1 = new Vector3();
		final Vector3 p2 = new Vector3();
		final Vector3 p3 = new Vector3();

		float pos = 0;
//		for (int i = 0; i < vertexBuffer.capacity() / 9; i++) {
		for (int i = 0; i < vertexBuffer.limit() / 9; i++) {
			pos += 0.5;
			final int xPos = i * 9;
			orgVertexBuffer.position(xPos);
			p1.set(orgVertexBuffer.get(), orgVertexBuffer.get(), orgVertexBuffer.get());
			p2.set(orgVertexBuffer.get(), orgVertexBuffer.get(), orgVertexBuffer.get());
			p3.set(orgVertexBuffer.get(), orgVertexBuffer.get(), orgVertexBuffer.get());
			flattenTriangle(p1, p2, p3);
			vertexBuffer.position(xPos);
			vertexBuffer.put(p1.getXf()).put(p1.getYf()).put(p1.getZf());
			vertexBuffer.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());
			vertexBuffer.put(p3.getXf()).put(p3.getYf()).put(p3.getZf());
		}

		mesh.updateModelBound();
		if (bottomMesh != null)
			bottomMesh.getSceneHints().setCullHint(CullHint.Always);
	

		// Vector3.releaseTempInstance(p1);
		// Vector3.releaseTempInstance(p2);
		// Vector3.releaseTempInstance(p3);

		// root.setRotation((new Matrix3().fromAngles(flattenTime * Math.PI / 2, 0, 0)));
		// super.flatten();
	}

	private void flattenTriangle(Vector3 p1, Vector3 p2, Vector3 p3) {
		final Vector3 v = new Vector3(p3).subtractLocal(p1);
		final Vector3 normal = new Vector3(p2).subtractLocal(p1).crossLocal(v);
		normal.normalizeLocal();
		double angle = flattenTime * normal.smallestAngleBetween(Vector3.UNIT_Y);
		v.set(p3).subtractLocal(p1).normalizeLocal();
		normal.crossLocal(Vector3.UNIT_Y);
		final Matrix3 m = new Matrix3().fromAngleAxis(angle, normal);
		m.applyPost(p1, p1);
		m.applyPost(p2, p2);
		m.applyPost(p3, p3);

		// Vector3 targetCenter = new Vector3(printSequence % PRINT_COLS * PRINT_SPACE, printSequence / PRINT_COLS * PRINT_SPACE, 0);
		// computePrintCenter(targetCenter, printSequence);
		computePrintCenter();
		final Vector3 targetCenter = new Vector3(printCenter);
		final Vector3 currentCenter = v.set(p1).addLocal(p2).addLocal(p3).multiplyLocal(1.0 / 3.0);
		final Vector3 d = targetCenter.subtractLocal(currentCenter).multiplyLocal(flattenTime);
		p1.addLocal(d);
		p2.addLocal(d);
		p3.addLocal(d);

		// Vector3.releaseTempInstance(v);
		// Vector3.releaseTempInstance(normal);
		// Vector3.releaseTempInstance(targetCenter);
		// Matrix3.releaseTempInstance(m);
	}

	protected void computeLabelTop(final Vector3 top) {
		top.set(0, 0, labelTop);
		// return labelTop;
	}

	public ReadOnlyVector3 getFaceDirection() {
		return Vector3.UNIT_Z;
	}

	protected void drawAnnotations() {
		if (container == null)
			return;
		// ReadOnlyVector3 faceDirection = getFaceDirection();
		int annotCounter = 0, angleAnnotCounter = 0;

		// if (flattenTime == 0) {
		if (original == null) {
			for (int i = 0; i < wallUpperPoints.size(); i++) {
				PolygonPoint p = wallUpperPoints.get(i);
				Vector3 a = new Vector3(p.getX(), p.getY(), p.getZ());
				p = wallUpperPoints.get((i + 1) % wallUpperPoints.size());
				Vector3 b = new Vector3(p.getX(), p.getY(), p.getZ());
				fetchSizeAnnot(annotCounter++).setRange(a, b, center, getFaceDirection(), original == null, Align.Center, true);
				// drawSizeAnnot(a, b, faceDirection, annotCounter++, Align.Center, true);
			}
		} else {
			final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();

			final Vector3 p1 = new Vector3();
			final Vector3 p2 = new Vector3();
			final Vector3 p3 = new Vector3();

			float pos = 0;
			for (int i = 0; i < vertexBuffer.capacity() / 9; i++) {
				// int i = 0;
				pos += 0.5;
				final int xPos = i * 9;
				vertexBuffer.position(xPos);
				p1.set(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
				p2.set(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
				p3.set(vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get());
				// drawSizeAnnot(p1, p2, Vector3.UNIT_Y, annotCounter++, Align.Center, false);
				// drawSizeAnnot(p2, p3, Vector3.UNIT_Y, annotCounter++, Align.Center, false);
				// drawSizeAnnot(p3, p1, Vector3.UNIT_Y, annotCounter++, Align.Center, false);
				fetchSizeAnnot(annotCounter++).setRange(p1, p2, center, Vector3.UNIT_Y, original == null, Align.Center, false);
				fetchSizeAnnot(annotCounter++).setRange(p2, p3, center, Vector3.UNIT_Y, original == null, Align.Center, false);
				fetchSizeAnnot(annotCounter++).setRange(p3, p1, center, Vector3.UNIT_Y, original == null, Align.Center, false);

				// Angle annotations
				fetchAngleAnnot(angleAnnotCounter++).setRange(p1, p2, p3);
				fetchAngleAnnot(angleAnnotCounter++).setRange(p2, p3, p1);
				fetchAngleAnnot(angleAnnotCounter++).setRange(p3, p1, p2);

			}

			// Vector3.releaseTempInstance(p1);
			// Vector3.releaseTempInstance(p2);
			// Vector3.releaseTempInstance(p3);
		}

		for (int i = annotCounter; i < sizeAnnotRoot.getChildren().size(); i++)
			sizeAnnotRoot.getChild(i).getSceneHints().setCullHint(CullHint.Always);

		// Vector3.releaseTempInstance(a);
		// Vector3.releaseTempInstance(b);
	}

	// public int setPrintSequence(int printSequence) {
	// int numOfPages = 0;
	// for (int i=0; i<mesh.getMeshData().getVertexCount() / 9; i++)
	// numOfPages += super.setPrintSequence(printSequence + numOfPages);
	// return numOfPages;
	// }

	protected void updateLabels() {
		final Vector3 p1 = new Vector3();
		final Vector3 p2 = new Vector3();
		final Vector3 p3 = new Vector3();

		final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
		buf.rewind();
		for (int triangle = 0; triangle < buf.limit() / 9; triangle++) {
			p1.set(buf.get(), buf.get(), buf.get());
			p2.set(buf.get(), buf.get(), buf.get());
			p3.set(buf.get(), buf.get(), buf.get());
			double height = Math.max(Math.max(p1.getZ(), p2.getZ()), p3.getZ());
			p1.addLocal(p2).addLocal(p3).multiplyLocal(1.0 / 3.0);
			p1.setZ(height + 0.3);
			final String text = "(" + (printSequence++ + 1) + ")";
			final BMText label = fetchBMText(text, triangle);
			label.setTranslation(p1);
		}
	}

}
