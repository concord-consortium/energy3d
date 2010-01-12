package cc.househeat.model;

import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.hint.LightCombineMode;

public class DrawnLines extends Drawn {
	public DrawnLines() {	
//		buffer.put(1).put(1).put(1);
//		buffer.put(2).put(2).put(2);
		
		mesh = new Line("Lines", buffer, null, null, null);		
		mesh.getSceneHints().setLightCombineMode(LightCombineMode.Off);
//		mesh.getMeshData().setIndexMode(IndexMode.Quads);
		root.attachChild(mesh);
	}

	@Override
	public void addPoint(Vector3 point) {
		if (drawCompleted)
			throw new RuntimeException("Drawing of this object is already completed");
		Vector3 firstPoint = new Vector3(buffer.get(0), buffer.get(1), buffer.get(2));
		double d = point.distance(firstPoint);
		if (d < 0.5) {
			drawCompleted = true;
			point = firstPoint;
		}

		int i = n * 3;
		buffer.put(i++, point.getXf()).put(i++, point.getYf()).put(i++, point.getZf());
		n++;

		if (n % 2 == 0) {
			i = n * 3;
			buffer.put(i++, point.getXf()).put(i++, point.getYf()).put(i++, point.getZf());
			n++;
		}
		nArray[0] = n;
		mesh.getMeshData().setIndexLengths(nArray);
	}
	
}
