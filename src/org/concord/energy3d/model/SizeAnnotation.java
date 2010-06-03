package org.concord.energy3d.model;

import java.nio.FloatBuffer;

import com.ardor3d.example.ui.BMFontLoader;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.util.geom.BufferUtils;

public class SizeAnnotation extends Node {
	private final BMText label = new BMText("textSpatial1", "0.0", BMFontLoader.defaultFont(), BMText.Align.Center, BMText.Justify.Center);
	private final Line lines = new Line("Size annotation lines", BufferUtils.createVector3Buffer(10), null, null, null);
	private final Mesh arrows = new Mesh("Arrows");
	
	public SizeAnnotation() {
		this.attachChild(lines);
		this.attachChild(label);
		this.attachChild(arrows);
		arrows.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(6));
		arrows.setDefaultColor(ColorRGBA.BLACK);
		lines.setDefaultColor(ColorRGBA.BLACK);
	}
	
	public void setRange(final ReadOnlyVector3 from, final ReadOnlyVector3 to, final ReadOnlyVector3 center, final ReadOnlyVector3 faceDirection) {		
		final double C = 0.1;
		Vector3 v = Vector3.fetchTempInstance();
		final Vector3 offset = new Vector3(to).subtractLocal(from).crossLocal(faceDirection).normalizeLocal().multiplyLocal(C);
		v.set(from).subtractLocal(center).normalizeLocal();
		if (v.dot(offset) < 0)
			offset.negateLocal();
			
		FloatBuffer vertexBuffer = lines.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		
		// main line
		Vector3 newFrom = Vector3.fetchTempInstance().set(from).addLocal(offset);
		vertexBuffer.put(newFrom.getXf()).put(newFrom.getYf()).put(newFrom.getZf());
		Vector3 newTo = Vector3.fetchTempInstance().set(to).addLocal(offset);
		vertexBuffer.put(newTo.getXf()).put(newTo.getYf()).put(newTo.getZf());				
		
		offset.multiplyLocal(0.5);
		// from End
		v.set(from);//.subtractLocal(offset);
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		v.set(newFrom).addLocal(offset);
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		
		// to End
		v.set(to);//.subtractLocal(offset);
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		v.set(newTo).addLocal(offset);
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());

		// arrow		
		offset.multiplyLocal(0.5);
		Vector3 body = Vector3.fetchTempInstance().set(to).subtractLocal(from).normalizeLocal().multiplyLocal(0.05);
//		// arrow right side
//		v.set(newFrom);
//		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
//		v.addLocal(offset).addLocal(body);		
//		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
//		// arrow left side
//		v.set(newFrom);
//		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
//		v.subtractLocal(offset).addLocal(body);		
//		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());	
		
		vertexBuffer = arrows.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		// arrow from
		v.set(newFrom);
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		v.addLocal(offset).addLocal(body);		
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		v.set(newFrom).subtractLocal(offset).addLocal(body);		
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());			
		// arrow to
		body.negateLocal();
		v.set(newTo);
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		v.addLocal(offset).addLocal(body);		
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		v.set(newTo).subtractLocal(offset).addLocal(body);		
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());			
		
				
		Vector3 middle = Vector3.fetchTempInstance().set(newFrom).addLocal(newTo).multiplyLocal(0.5);		
		label.setTranslation(middle);	
		label.setText(""+Math.round(to.subtract(from, null).length() * 100) / 100.0);
		
		
		
		Vector3.releaseTempInstance(newFrom);
		Vector3.releaseTempInstance(newTo);
		Vector3.releaseTempInstance(middle);
		Vector3.releaseTempInstance(v);
		Vector3.releaseTempInstance(body);
	}
}
