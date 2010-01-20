package cc.energy3d.model;

import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.shape.Quad;

public class DrawnRectangle extends Drawn {
//	boolean firstPointDrawn = false;
	private Vector3 firstPoint = null; 

	public DrawnRectangle() {
		mesh = new Quad("", 0, 0);
//		mesh.getMeshData().setVertexBuffer(buffer);
		root.attachChild(mesh);
		buffer = mesh.getMeshData().getVertexBuffer();
	}
	
	public void addPoint(Vector3 point) {
		if (firstPoint == null) {
			firstPoint = point;
//			super.addPoint(point);
		} else {
			drawRectangle(point);
//			Vector3 d = point.subtract(firstPoint, null);
//			super.addPoint(firstPoint.add(d.getX(), 0, 0, null));
//			super.addPoint(point);
//			super.addPoint(firstPoint.add(0, d.getY(), 0, null));
			drawCompleted = true;
		}
	}
	
	public void setPreviewPoint(Vector3 point) {
		if (!drawCompleted && firstPoint != null) {
			drawRectangle(point);
		}
	}

	private void drawRectangle(Vector3 point) {
		Vector3 d = point.subtract(firstPoint, null);
		Vector3 p1 = firstPoint;
		Vector3 p2 = (firstPoint.add(d.getX(), 0, 0, null));
		Vector3 p3 = (point);
		Vector3 p4 = (firstPoint.add(0, d.getY(), 0, null));
		
		int i = 0; 
		buffer.put(i++, p1.getXf()).put(i++, p1.getYf()).put(i++, p1.getZf());
		buffer.put(i++, p2.getXf()).put(i++, p2.getYf()).put(i++, p2.getZf());
		buffer.put(i++, p3.getXf()).put(i++, p3.getYf()).put(i++, p3.getZf());
		buffer.put(i++, p4.getXf()).put(i++, p4.getYf()).put(i++, p4.getZf());
	}
	
	
}
