package cc.househeat.model;

import com.ardor3d.image.Texture;
import com.ardor3d.image.Image.Format;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.util.TextureManager;

public class DrawnWall extends Drawn {
	private static final float WALL_HEIGHT = 0.5f;
	private Vector3 lastPoint = null; 	
	
	public DrawnWall() {
//		mesh = new Quad("",1,1);
		mesh = new Mesh("Walls");
		root.attachChild(mesh);
		mesh.getMeshData().setVertexBuffer(buffer);
		mesh.getMeshData().setIndexMode(IndexMode.QuadStrip);
//		buffer = mesh.getMeshData().getVertexBuffer();
		
        // Add a material to the box, to show both vertex color and lighting/shading.
        final MaterialState ms = new MaterialState();
        ms.setColorMaterial(ColorMaterial.Diffuse);
        mesh.setRenderState(ms);
        
        // Add a texture to the box.
        final TextureState ts = new TextureState();
        ts.setTexture(TextureManager.load("http://www.madclint.co.za/brick_wall.jpg", Texture.MinificationFilter.Trilinear,
                Format.GuessNoCompression, true));
        mesh.setRenderState(ts);
        
		
	}
	
	@Override
	public void addPoint(Vector3 point) {
//		if (lastPoint != null)
//			drawWall(lastPoint, point);
//		lastPoint = point;
		int i = n * 3;
		buffer.put(i++, point.getXf()).put(i++, point.getYf()).put(i++, point.getZf());			
		buffer.put(i++, point.getXf()).put(i++, point.getYf()).put(i++, WALL_HEIGHT);
		n += 2;		
		buffer.put(i++, point.getXf()).put(i++, point.getYf()).put(i++, point.getZf());			
		buffer.put(i++, point.getXf()).put(i++, point.getYf()).put(i++, WALL_HEIGHT);
		
//		buffer.put(i++, lastPoint.getXf()).put(i++, lastPoint.getYf()).put(i++, WALL_HEIGHT);
//		buffer.put(i++, lastPoint.getXf()).put(i++, lastPoint.getYf()).put(i++, lastPoint.getZf());
		nArray[0] = n + 2; //n + 4;
		mesh.getMeshData().setIndexLengths(nArray);
		
	}
	
	@Override
	public void setPreviewPoint(Vector3 point) {
//		drawWall(lastPoint, point);
		int i = n * 3;
		buffer.put(i++, point.getXf()).put(i++, point.getYf()).put(i++, point.getZf());			
		buffer.put(i++, point.getXf()).put(i++, point.getYf()).put(i++, WALL_HEIGHT);
		
	}

	public void drawWall(Vector3 lastPoint, Vector3 point) {
		if (!drawCompleted) {
//			Vector3 firstPoint = new Vector3(buffer.get(0), buffer.get(1), buffer.get(2));
//			double d = point.distance(firstPoint);
//			if (d < 0.5) {
//				point = firstPoint;
//			}
			
			int i = 0; //n * 3;
			buffer.put(i++, point.getXf()).put(i++, point.getYf()).put(i++, point.getZf());			
			buffer.put(i++, point.getXf()).put(i++, point.getYf()).put(i++, WALL_HEIGHT);
			buffer.put(i++, lastPoint.getXf()).put(i++, lastPoint.getYf()).put(i++, WALL_HEIGHT);
			buffer.put(i++, lastPoint.getXf()).put(i++, lastPoint.getYf()).put(i++, lastPoint.getZf());
//			nArray[0] = 5; //n + 4;
//			mesh.getMeshData().setIndexLengths(nArray);
		}
	}
	
}
