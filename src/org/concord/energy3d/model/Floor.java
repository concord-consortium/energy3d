package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.concord.energy3d.util.Util;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.TextureManager;

public class Floor extends Roof {
	private static final long serialVersionUID = 1L;
	private static final double GRID_SIZE = 0.5;

	public Floor() {
		super(1, 1, 0.5);
	}
	
	protected void init() {
		super.init();
		root.detachChild(bottomMesh);
		bottomMesh = null;
	}

	public void setPreviewPoint(int x, int y) {
			pick(x, y, Wall.class);
			if (container != null) {
			Vector3 base = container.getPoints().get(0);
			Vector3 p = closestPoint(base, base.add(0, 0, 1, null), x, y);
			p = grid(p, GRID_SIZE);
			height = findHeight(base, p) + base.getZ();
		draw();
		showPoints();
			}

	}

//	public void draw() {
//		if (root == null)
//			init();
//
//		if (container == null)
//			return;
//		
////		super.init();
//
//		
//		ArrayList<PolygonPoint> wallUpperPoints = exploreWallNeighbors((Wall) container);
//		shiftToOutterEdge(wallUpperPoints);
//		Polygon ps = new Polygon(wallUpperPoints);
//		Poly2Tri.triangulate(ps);
//
//		ArdorMeshMapper.updateTriangleMesh(mesh, ps);
//		ArdorMeshMapper.updateVertexNormals(mesh, ps.getTriangles());
//		ArdorMeshMapper.updateFaceNormals(mesh, ps.getTriangles());
//		ArdorMeshMapper.updateTextureCoordinates(mesh, ps.getTriangles(), 1, 0);
//		
//		mesh.getMeshData().updateVertexCount();
//
//		for (int i = 0; i < points.size(); i++) {
//			Vector3 p = points.get(i);
//			pointsRoot.getChild(i).setTranslation(p);
//		}
//
//		updateLabelLocation();
//		if (flattenTime > 0)
//			flatten();
//		
//		// force bound update
//		mesh.updateModelBound();
//		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
//	}

	protected Polygon makePolygon(ArrayList<PolygonPoint> wallUpperPoints) {
		center.set(0, 0, 0);
		double maxY = wallUpperPoints.get(0).getY();
		for (PolygonPoint p : wallUpperPoints) {
			center.addLocal(p.getX(), p.getY(), height);
			p.set(p.getX(), p.getY(), height);
			if (p.getY() > maxY)
				maxY = p.getY();			
		}
		center.multiplyLocal(1.0 / wallUpperPoints.size());
		labelTop = (maxY-center.getY());
		points.get(0).set(center);
		final Polygon polygon = new Polygon(wallUpperPoints);
		return polygon;
	}
	
	public void setHeight(double newHeight, boolean finalize) {
		super.setHeight(newHeight, finalize);
		draw();		
	}

	protected void fillMeshWithPolygon(Mesh mesh, Polygon polygon) {
		try {
			Poly2Tri.triangulate(polygon);
			ArdorMeshMapper.updateTriangleMesh(mesh, polygon);
			
//			if (Util.DEBUG) {
//				System.out.println("Mesh: ");
//				final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
//				vertexBuffer.rewind();
//				while (vertexBuffer.hasRemaining())
//					System.out.print(vertexBuffer.get() + ", ");
//				System.out.println("\b");
//			}
			
			ArdorMeshMapper.updateVertexNormals(mesh, polygon.getTriangles());
			ArdorMeshMapper.updateFaceNormals(mesh, polygon.getTriangles());
			// ArdorMeshMapper.updateVertexNormals(mesh, polygon.getTriangles());
			ArdorMeshMapper.updateTextureCoordinates(mesh, polygon.getTriangles(), 0.1, 0);
			mesh.getMeshData().updateVertexCount();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateTexture() {
		if (textureEnabled) {
			final TextureState ts = new TextureState();
			ts.setTexture(TextureManager.load("floor3.jpg", Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
			mesh.setRenderState(ts);
			mesh.setDefaultColor(ColorRGBA.WHITE);
			if (bottomMesh != null) {
				bottomMesh.setRenderState(ts);
				bottomMesh.setDefaultColor(ColorRGBA.WHITE);
			}
		} else {
			mesh.clearRenderState(StateType.Texture);
			mesh.setDefaultColor(defaultColor);
			if (bottomMesh != null) {
				bottomMesh.clearRenderState(StateType.Texture);
				bottomMesh.setDefaultColor(defaultColor);
			}
		}
	}	
	
}
