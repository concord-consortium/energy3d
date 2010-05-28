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
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public class Floor extends HousePart {
	private static final long serialVersionUID = 1L;
	private static final double GRID_SIZE = 0.5;
//	private double height = 0.5;
	private transient Mesh mesh;
	private transient FloatBuffer vertexBuffer;
//	private transient Vector3 avg;

	public Floor() {
		super(1, 1, 0.5);
//		height = 0.5;
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

		mesh.setUserData(new UserData(this, 0));
	}

	@Override
	public void setPreviewPoint(int x, int y) {
//		if (editPointIndex == -1) {
			pick(x, y, Wall.class);
//		} else {
			if (container != null) {
			Vector3 base = container.getPoints().get(0);
			Vector3 p = closestPoint(base, base.add(0, 0, 1, null), x, y);
			p = grid(p, GRID_SIZE);
			height = findHeight(base, p) + base.getZ();
//		}
		draw();
		showPoints();
			}

	}

	@Override
	public void draw() {
		if (root == null)
			init();

		if (container == null)
			return;
		
//		super.init();

		
		ArrayList<PolygonPoint> wallUpperPoints = exploreWallNeighbors((Wall) container);
		shiftToOutterEdge(wallUpperPoints);
		Polygon ps = new Polygon(wallUpperPoints);
		Poly2Tri.triangulate(ps);

		ArdorMeshMapper.updateTriangleMesh(mesh, ps);
		ArdorMeshMapper.updateVertexNormals(mesh, ps.getTriangles());
		ArdorMeshMapper.updateFaceNormals(mesh, ps.getTriangles());
		ArdorMeshMapper.updateTextureCoordinates(mesh, ps.getTriangles(), 1, 0);
		
		mesh.getMeshData().updateVertexCount();

		for (int i = 0; i < points.size(); i++) {
			Vector3 p = points.get(i);
			pointsRoot.getChild(i).setTranslation(p);
		}

		if (flattenTime > 0)
			flatten();
		
		// force bound update
		mesh.updateModelBound();
		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
	}

	private void shiftToOutterEdge(ArrayList<PolygonPoint> wallUpperPoints) {
		center.set(0, 0, 0);
		for (PolygonPoint p : wallUpperPoints) {
			center.addLocal(p.getX(), p.getY(), height);
			p.set(p.getX(), p.getY(), height);
		}
		center.multiplyLocal(1.0 / wallUpperPoints.size());
	}
	
	public void setHeight(double newHeight, boolean finalize) {
		super.setHeight(newHeight, finalize);
//		points.get(0).setZ(newHeight);
		draw();		
	}	

	protected void flatten() {		
		root.setRotation((new Matrix3().fromAngles(flattenTime * Math.PI / 2, 0, 0)));
//		root.setTranslation(flattenTime * 5*(int) (pos / 3), height, flattenTime * 3*(2 + pos % 3));
//		root.setTranslation(flattenTime * printX, height, flattenTime * printY);
		super.flatten();
	}		
	
}
