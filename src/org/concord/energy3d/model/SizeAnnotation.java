package org.concord.energy3d.model;

import java.nio.FloatBuffer;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.geom.BufferUtils;

public class SizeAnnotation extends Annotation {
	protected final Mesh arrows = new Mesh("Arrows");
	
	public SizeAnnotation() {
		super(null); // TODO temporarly
		this.attachChild(arrows);
		arrows.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(6));
		arrows.setDefaultColor(ColorRGBA.BLACK);
	}
	
	protected Mesh getMesh() {
		if (mesh != null)
			return mesh;
		else
			return new Line("Size annotation lines", BufferUtils.createVector3Buffer(12), null, null, null);
	}

	public void setRange(final ReadOnlyVector3 from, final ReadOnlyVector3 to, final ReadOnlyVector3 center, final ReadOnlyVector3 faceDirection, final boolean front, final Align align, boolean autoFlipDirection) {
		final double C = 0.1;
		Vector3 v = Vector3.fetchTempInstance();
		final Vector3 offset = Vector3.fetchTempInstance();
		if (front)
			offset.set(faceDirection).normalizeLocal().multiplyLocal(C);
		else {
			offset.set(to).subtractLocal(from).crossLocal(faceDirection).normalizeLocal().multiplyLocal(C);
			if (autoFlipDirection) {
				v.set(from).subtractLocal(center).normalizeLocal();
				if (v.dot(offset) < 0)
					offset.negateLocal();
			}
		}

		FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();

		// main line
		final Vector3 newFrom = Vector3.fetchTempInstance().set(from).addLocal(offset);
		final Vector3 newTo = Vector3.fetchTempInstance().set(to).addLocal(offset);
		final Vector3 middle = Vector3.fetchTempInstance().set(newFrom).addLocal(newTo).multiplyLocal(0.5);
		Vector3 body = Vector3.fetchTempInstance().set(to).subtractLocal(from).multiplyLocal(0.5);
		vertexBuffer.put(newFrom.getXf()).put(newFrom.getYf()).put(newFrom.getZf());
		double s = (body.length() - 0.15) / body.length();
		v.set(body).multiplyLocal(s).addLocal(newFrom);
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		v.set(body).multiplyLocal(-s).addLocal(newTo);
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		vertexBuffer.put(newTo.getXf()).put(newTo.getYf()).put(newTo.getZf());

		offset.multiplyLocal(0.5);
		// from End
		v.set(from);// .subtractLocal(offset);
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		v.set(newFrom).addLocal(offset);
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());

		// to End
		v.set(to);// .subtractLocal(offset);
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		v.set(newTo).addLocal(offset);
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());

		// arrow
		offset.multiplyLocal(0.5);
		body = Vector3.fetchTempInstance().set(to).subtractLocal(from).normalizeLocal().multiplyLocal(0.05);
		// // arrow right side
		// v.set(newFrom);
		// vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		// v.addLocal(offset).addLocal(body);
		// vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		// // arrow left side
		// v.set(newFrom);
		// vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		// v.subtractLocal(offset).addLocal(body);
		// vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());

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

		// Vector3 middle = Vector3.fetchTempInstance().set(newFrom).addLocal(newTo).multiplyLocal(0.5);
		label.setTranslation(middle);
		label.setText("" + Math.round(to.subtract(from, null).length() * 100) / 100.0 + "m");
		label.setAlign(align);

		Vector3.releaseTempInstance(newFrom);
		Vector3.releaseTempInstance(newTo);
		Vector3.releaseTempInstance(middle);
		Vector3.releaseTempInstance(v);
		Vector3.releaseTempInstance(body);
		Vector3.releaseTempInstance(offset);
	}
}
