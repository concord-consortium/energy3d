package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

public class Wall extends HousePart {
	private double wallHeight = 0.8f;
	private ArrayList<HousePart> children = new ArrayList<HousePart>();
	private Mesh mesh = new Mesh("Wall");
	private FloatBuffer vertexBuffer = BufferUtils.createVector3Buffer(4);
	private FloatBuffer textureBuffer = BufferUtils.createVector2Buffer(4);
	private Snap[] neighbor = new Snap[2];

	public Wall() {
		super(2, 4);
		root.attachChild(mesh);
		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
//		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		mesh.getMeshData().setVertexBuffer(vertexBuffer);
		mesh.getMeshData().setTextureBuffer(textureBuffer, 0);

		// Add a material to the box, to show both vertex color and lighting/shading.
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		mesh.setRenderState(ms);

		// Add a texture to the box.
		final TextureState ts = new TextureState();
		ts.setTexture(TextureManager.load("brick_wall.jpg", Texture.MinificationFilter.Trilinear, Format.GuessNoCompression, true));
		mesh.setRenderState(ts);

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
					((Wall)snap.getHousePart()).setNeighbor(snap.getOtherPointIndex(), new Snap(this, index, snap.getOtherPointIndex()));
				points.set(index, p);
				points.set(index + 1, getUpperPoint(p));
			}
		} else if (editPointIndex == 1 || editPointIndex == 3) {
			int lower = (editPointIndex == 1) ? 0 : 2;
			Vector3 base = points.get(lower);
			Vector3 closestPoint = closestPoint(base, base.add(0, 0, 1, null), x, y);
//			neighbor[1] = snap(closestPoint);
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
			if (drawable)
				vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
//				polyPoints.add(new ArdorVector3PolygonPoint(p));

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
						
//			ArrayList<PolygonPoint> polyPoints = new ArrayList<PolygonPoint>();
//			polyPoints.add(new PolygonPoint(0,0,0));
////			polyPoints.add(new PolygonPoint(1,0,0));
////			polyPoints.add(new PolygonPoint(1,1,0));
////			polyPoints.add(new PolygonPoint(0,1,0));
//			Vector3 p;
//			p = points.get(0);
//			polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
//			p = points.get(1);
//			polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
//			p = points.get(2);
//			polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
//			p = points.get(3);
//			polyPoints.add(new PolygonPoint(p.getX(), p.getY(), p.getZ()));
//			Polygon ps = new Polygon(polyPoints );
//			try {
//				Poly2Tri.triangulate(ps);
//				ArdorMeshMapper.updateTriangleMesh(mesh, ps);
//				ArdorMeshMapper.updateVertexNormals(mesh, ps.getTriangles());
//				ArdorMeshMapper.updateFaceNormals(mesh, ps.getTriangles());
//				ArdorMeshMapper.updateTextureCoordinates(mesh, 1, 0);			
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
////				mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
//			}

			// force bound update
			CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);

			for (HousePart child : children)
				child.draw();
		}

	}

	public Snap next(Wall previous) {
//		for (int i = 0; i < neighbor.length; i++)
//			if (neighbor[i] != null && neighbor[i].getHousePart() != previous)
//				return new Snap(neighbor[i].getHousePart(), i);
		for (Snap s : neighbor)
			if (s != null && s.getHousePart() != previous)
				return s;

		return null;
	}

	private void setNeighbor(int pointIndex, Snap snap) {
		int i = pointIndex < 2 ? 0 : 1;
//		if (neighbor[i] != null && !neighbor[i].equals(snap))
//			((Wall)neighbor[i].getHousePart()).removeNeighbor(this);
		if (neighbor[i] == null)
			neighbor[i] = snap;
	}
	
	private void removeNeighbor(Wall wall) {
		for (int i=0; i<neighbor.length; i++)
			if (neighbor[i] != null && neighbor[i].getHousePart() == wall)
				neighbor[i] = null;
	}
	
	public void destroy() {
		for (int i=0; i<neighbor.length; i++)
			if (neighbor[i] != null)
				((Wall)neighbor[i].getHousePart()).removeNeighbor(this);
	}

}