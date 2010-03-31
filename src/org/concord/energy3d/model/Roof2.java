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
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public class Roof2 extends HousePart {
	private static final long serialVersionUID = 1L;
	private static final double GRID_SIZE = 0.5;
//	private double height = 0.5;
	private transient Mesh mesh;
	private transient FloatBuffer vertexBuffer;
	private transient Vector3 avg;

	public Roof2() {
		super(1, 3);
		height = 0.5;
	}

	protected void init() {
		super.init();
		mesh = new Mesh("Roof");
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

	@Override
	public void setPreviewPoint(int x, int y) {
		if (editPointIndex == -1) {
			pick(x, y, Wall.class);
		} else if (editPointIndex == 0){
			Vector3 base = avg;
			Vector3 p = closestPoint(base, base.add(0, 0, 1, null), x, y);
			p = grid(p, GRID_SIZE);
			height = findHeight(base, p);
		} else if (editPointIndex == 1 || editPointIndex == 2) {
			Vector3 p = closestPoint(points.get(0), points.get(0).add(Vector3.UNIT_Y, null), x, y);
			p = grid(p, GRID_SIZE);
			points.get(editPointIndex).set(p);
		}
		draw();
		showPoints();

	}

	@Override
	protected void draw() {
		if (root == null)
			init();

		if (container == null)
			return;

		ArrayList<PolygonPoint> wallUpperPoints = exploreWallNeighbors((Wall) container);
		
		avg = new Vector3();
		for (PolygonPoint p : wallUpperPoints)
			avg.addLocal(p.getX(), p.getY(), p.getZ());
		avg.multiplyLocal(1f / (wallUpperPoints.size()));
		
		shiftToOutterEdge(wallUpperPoints);
		
		points.get(0).set(avg.getX(), avg.getY(), avg.getZ() + height);
		if (editPointIndex == -1) {
			points.get(1).set(avg.getX(), avg.getY()-1, avg.getZ() + height);
			points.get(2).set(avg.getX(), avg.getY()+1, avg.getZ() + height);
		} else {
			points.get(1).setZ(avg.getZ() + height);
			points.get(2).setZ(avg.getZ() + height);
		}
		PolygonPoint roofUpperPoint1 = new PolygonPoint(points.get(1).getX(), points.get(1).getY(), points.get(1).getZ());
		PolygonPoint roofUpperPoint2 = new PolygonPoint(points.get(2).getX(), points.get(2).getY(), points.get(2).getZ());

//		System.out.println("Polygon Points:");
//		for (PolygonPoint p : wallUpperPoints) {
//			System.out.println(p.getXf() + "\t" + p.getYf() + "\t" + p.getZf());
//		}

		Polygon ps = new Polygon(wallUpperPoints);
		ps.addSteinerPoint(roofUpperPoint1);
		ps.addSteinerPoint(roofUpperPoint2);
		Poly2Tri.triangulate(ps);

//		System.out.println("Triangulated Points:");
//		for (DelaunayTriangle t : ps.getTriangles()) {
//			t.printDebug();
//		}
		
		ArdorMeshMapper.updateTriangleMesh(mesh, ps);
		ArdorMeshMapper.updateVertexNormals(mesh, ps.getTriangles());
		ArdorMeshMapper.updateFaceNormals(mesh, ps.getTriangles());
		ArdorMeshMapper.updateTextureCoordinates(mesh, ps.getTriangles(), 1, 0);
		
		mesh.getMeshData().updateVertexCount();
		// mesh.setRandomColors();

		for (int i = 0; i < points.size(); i++) {
			Vector3 p = points.get(i);
			// update location of point spheres
			pointsRoot.getChild(i).setTranslation(p);
//			((Sphere) pointsRoot.getChild(i)).updateModelBound();
//			pointsRoot.setVisible(i, true);
		}

		// force bound update
		mesh.updateModelBound();
		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
	}
	
	private void shiftToOutterEdge(ArrayList<PolygonPoint> wallUpperPoints) {
		final double edgeLenght = 0.3;
		Vector3 op = new Vector3();
		for (PolygonPoint p : wallUpperPoints) {
			op.set(p.getX(), p.getY(), 0).subtractLocal(avg.getX(), avg.getY(), 0).normalizeLocal().multiplyLocal(edgeLenght);
			op.addLocal(p.getX(), p.getY(), p.getZ());
			p.set(op.getX(), op.getY(), op.getZ()+0.01);
		}
	}

}
