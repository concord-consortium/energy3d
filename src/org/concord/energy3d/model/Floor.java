package org.concord.energy3d.model;

import java.util.ArrayList;

import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.util.TextureManager;

public class Floor extends Roof {
	private static final long serialVersionUID = 1L;
	private static final double GRID_SIZE = 0.2;

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
			Vector3 p = closestPoint(base, Vector3.UNIT_Z, x, y);
			p = grid(p, GRID_SIZE);
			height = Math.max(0, p.getZ() - base.getZ()) + base.getZ();
		draw();
		showPoints();
			}

	}

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
	
	protected void fillMeshWithPolygon(Mesh mesh, Polygon polygon) {
		try {
			Poly2Tri.triangulate(polygon);
			ArdorMeshMapper.updateTriangleMesh(mesh, polygon);
			ArdorMeshMapper.updateVertexNormals(mesh, polygon.getTriangles());
			ArdorMeshMapper.updateFaceNormals(mesh, polygon.getTriangles());
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
		mesh.updateGeometricState(0);
	}	
	
	protected void flatten() {
		root.setRotation((new Matrix3().fromAngles(-flattenTime * Math.PI / 2, 0, 0)));
		
		root.setTranslation(0, 0, 0);
		final Vector3 targetCenter = new Vector3((ReadOnlyVector3) mesh.getUserData());
		final Vector3 currentCenter = new Vector3(center);
		
		root.getTransform().applyForward(currentCenter);
		final Vector3 subtractLocal = targetCenter.subtractLocal(currentCenter);
		root.setTranslation(subtractLocal.multiplyLocal(flattenTime));		
	}
	
	protected void updateLabels() {
		final String text = "(" + (printSequence++ + 1) + ")";
		final BMText label = fetchBMText(text, 0);
				
		label.setTranslation(center);
		Vector3 up = new Vector3();
		if (original == null)
			up.set(getFaceDirection());
		else
			up.set(0, -0.01, 0);
		root.getTransform().applyInverseVector(up);
		label.setTranslation(center.getX() + up.getX(), center.getY() + up.getY(), center.getZ() + up.getZ());
	}	
}
