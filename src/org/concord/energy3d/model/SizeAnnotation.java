package org.concord.energy3d.model;

import java.nio.FloatBuffer;

import com.ardor3d.example.ui.BMFontLoader;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.util.geom.BufferUtils;

public class SizeAnnotation extends Node {
	private final BMText label = new BMText("textSpatial1", "0.0", BMFontLoader.defaultFont(), BMText.Align.Center, BMText.Justify.Center);
	private final Line lines = new Line("Size annotation lines", BufferUtils.createVector3Buffer(2), null, null, null); 
	
	public SizeAnnotation() {
		this.attachChild(lines);
		this.attachChild(label);
	}
	
	public void setRange(final ReadOnlyVector3 from, final ReadOnlyVector3 to, final ReadOnlyVector3 center, final ReadOnlyVector3 faceDirection) {
		final double C = 0.1;
		Vector3 v = new Vector3();
		final Vector3 offset = new Vector3(to).subtractLocal(from).crossLocal(faceDirection).normalizeLocal().multiplyLocal(C);
		v.set(from).subtractLocal(center).normalizeLocal();
		if (v.dot(offset) < 0)
			offset.negateLocal();
			
		final FloatBuffer vertexBuffer = lines.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		v.set(from);		
		v.addLocal(offset); //.subtractLocal(center).normalizeLocal().multiplyLocal(C).addLocal(from);
		Vector3 middle = new Vector3(v);
//		v.setZ(abspoints.get(i).getZ());
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());

		v.set(to);		
//		v.subtractLocal(center).normalizeLocal().multiplyLocal(C).addLocal(to);
		v.addLocal(offset);
//		v.setZ(abspoints.get(i).getZ());
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());

		middle.addLocal(v).multiplyLocal(0.5);
		
		label.setTranslation(middle);	
		label.setText(""+Math.round(to.subtract(from, null).length() * 100) / 100.0);
		
		lines.setDefaultColor(ColorRGBA.BLACK);
	}
}
