package org.concord.energy3d.model;

import java.nio.FloatBuffer;

import org.concord.energy3d.util.Arc;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.geom.BufferUtils;

public class Angle90Annotation extends Annotation {

	public Angle90Annotation(HousePart housePart) {
		super(housePart);
		this.detachChild(label);	// TODO move label to subclass
	}

	protected Mesh getMesh() {
		if (mesh != null)
			return mesh;
		else {
			final Mesh mesh = new Mesh();
			mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(3));
			mesh.getMeshData().setIndexMode(IndexMode.LineStrip);
			return mesh;
			
		}
	}
	
//	public void setRange(final ReadOnlyVector3 p1, final ReadOnlyVector3 p2, final ReadOnlyVector3 p3) {
//		Vector3 a = Vector3.fetchTempInstance().set(p2).subtractLocal(p1).normalizeLocal();
//		Vector3 b = Vector3.fetchTempInstance().set(p3).subtractLocal(p1).normalizeLocal();		
//		Vector3 c = Vector3.fetchTempInstance();
//		Vector3 v = Vector3.fetchTempInstance();
//		
//		final double length = 0.2;
//		a.multiplyLocal(length);
//		b.multiplyLocal(length);
//		c.set(a).addLocal(b);
//		
//		v.set(housePart.getFaceDirection()).scaleAddLocal(0.01f, p1);
//		a.addLocal(v);
//		b.addLocal(v);
//		c.addLocal(v);
//		
//		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
//		vertexBuffer.rewind();
//		vertexBuffer.put(a.getXf()).put(a.getYf()).put(a.getZf());
//		vertexBuffer.put(c.getXf()).put(c.getYf()).put(c.getZf());
//		vertexBuffer.put(b.getXf()).put(b.getYf()).put(b.getZf());
//		
//		Vector3.releaseTempInstance(a);
//		Vector3.releaseTempInstance(b);
//		Vector3.releaseTempInstance(c);		
//		Vector3.releaseTempInstance(v);
//	}
	
	public void setRange(final ReadOnlyVector3 p1, final ReadOnlyVector3 p2, final ReadOnlyVector3 p3) {
		final Vector3 a = new Vector3(p2).subtractLocal(p1).normalizeLocal();
		final Vector3 b = new Vector3(p3).subtractLocal(p1).normalizeLocal();		
		final Vector3 c = new Vector3();
		final Vector3 v = new Vector3();
		
		final double length = 0.2;
		a.multiplyLocal(length);
		b.multiplyLocal(length);
		c.set(a).addLocal(b);
		
		v.set(housePart.getFaceDirection()).scaleAddLocal(0.01f, p1);
		a.addLocal(v);
		b.addLocal(v);
		c.addLocal(v);
		
		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		vertexBuffer.put(a.getXf()).put(a.getYf()).put(a.getZf());
		vertexBuffer.put(c.getXf()).put(c.getYf()).put(c.getZf());
		vertexBuffer.put(b.getXf()).put(b.getYf()).put(b.getZf());
		
//		Vector3.releaseTempInstance(a);
//		Vector3.releaseTempInstance(b);
//		Vector3.releaseTempInstance(c);		
//		Vector3.releaseTempInstance(v);
	}	
	
}
