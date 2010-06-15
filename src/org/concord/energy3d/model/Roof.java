package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.poly2tri.Poly2Tri;
import org.poly2tri.polygon.Polygon;
import org.poly2tri.polygon.PolygonPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.bounding.CollisionTreeManager;
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
	protected transient Mesh mesh;
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

	public void draw() {
		if (root == null)
			init();
		if (container == null)
			return;
		
//		super.draw();
		
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
		
		// force bound update
		mesh.updateModelBound();
		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
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
		root.setRotation((new Matrix3().fromAngles(flattenTime * Math.PI / 2, 0, 0)));
		super.flatten();
	}
	
	protected double computeLabelTop() {
		return labelTop;
	}	
	
	protected ReadOnlyVector3 getFaceDirection() {
		return Vector3.UNIT_Z;
	}
	
	protected void drawAnnotations() {
		ReadOnlyVector3 faceDirection = getFaceDirection();
		int annotCounter = 0;
		Vector3 a = Vector3.fetchTempInstance();
		Vector3 b = Vector3.fetchTempInstance();
		
		for (int i=0; i<wallUpperPoints.size(); i++) {
			PolygonPoint p = wallUpperPoints.get(i);
			a.set(p.getX(), p.getY(), p.getZ());
			p = wallUpperPoints.get((i+1)%wallUpperPoints.size());
			b.set(p.getX(), p.getY(), p.getZ());
			drawAnnot(a, b, faceDirection, annotCounter++, Align.Center);
		}
		
		for (int i = annotCounter; i < annotRoot.getChildren().size(); i++)
			annotRoot.getChild(i).getSceneHints().setCullHint(CullHint.Always);
		
		Vector3.releaseTempInstance(a);
		Vector3.releaseTempInstance(b);
	}	
}
