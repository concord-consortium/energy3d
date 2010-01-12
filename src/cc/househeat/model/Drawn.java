package cc.househeat.model;

import java.nio.FloatBuffer;
import java.util.LinkedList;

import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.geom.BufferUtils;

public abstract class Drawn {
	protected final Node root = new Node("Drawn Object");
	protected Mesh mesh;
	protected FloatBuffer buffer = BufferUtils.createVector3Buffer(100);
	protected LinkedList<Vector3> points = new LinkedList<Vector3>();
	protected int n = 0;
	protected int[] nArray = new int[1];
	protected boolean drawCompleted = false;

//	public void addPoint(Vector3 point) {
//		if (drawCompleted)
//			throw new RuntimeException("Drawing of this object is already completed");
//		Vector3 firstPoint = new Vector3(buffer.get(0), buffer.get(1), buffer.get(2));
//		double d = point.distance(firstPoint);
//		if (d < 0.5) {
//			drawCompleted = true;
//			point = firstPoint;
//		}
//
//		int i = n * 3;
//		buffer.put(i++, point.getXf()).put(i++, point.getYf()).put(i++, point.getZf());
//		n++;
//
//		if (n % 2 == 0) {
//			i = n * 3;
//			buffer.put(i++, point.getXf()).put(i++, point.getYf()).put(i++, point.getZf());
//			n++;
//		}
//		nArray[0] = n;
//		mesh.getMeshData().setIndexLengths(nArray);
//	}

	public void addPoint(Vector3 point) {
		if (drawCompleted)
			throw new RuntimeException("Drawing of this object is already completed");
		int i = n * 3;
		buffer.put(i++, point.getXf()).put(i++, point.getYf()).put(i++, point.getZf());
		n++;
	}
	
	public void setPreviewPoint(Vector3 point) {
		if (!drawCompleted && n > 0) {
			Vector3 firstPoint = new Vector3(buffer.get(0), buffer.get(1), buffer.get(2));
			double d = point.distance(firstPoint);
			if (d < 0.5) {
				point = firstPoint;
			}

			int i = n * 3;
			buffer.put(i++, point.getXf()).put(i++, point.getYf()).put(i++, point.getZf());
			nArray[0] = n + 1;
			mesh.getMeshData().setIndexLengths(nArray);

		}
	}

	public Node getRoot() {
		return root;
	}

	public boolean isDrawCompleted() {
		return drawCompleted;
	}

}
