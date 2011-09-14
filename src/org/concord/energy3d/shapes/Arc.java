package org.concord.energy3d.shapes;

import java.nio.FloatBuffer;

import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.util.geom.BufferUtils;

public class Arc extends Line {

	public Arc() {
		
	}
	
	public Arc(final String name, final int vertices) {
		super(name);
//		setAntialiased(true);
//        final BlendState blend = new BlendState();
//        blend.setBlendEnabled(true);
//        setRenderState(blend);
//		getSceneHints().setLightCombineMode(LightCombineMode.Off);
		getMeshData().setIndexMode(IndexMode.LineStrip);
		getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(vertices));
		Util.disablePickShadowLight(this);
	}

	public void set(final double radius, final double start, final double end) {
		final FloatBuffer buf = getMeshData().getVertexBuffer();
		buf.limit(buf.capacity());
		this.getMeshData().updateVertexCount();
		buf.rewind();
		double arc = end - start;
		final int n = buf.limit() / 3;
		for (int i = 0; i < n; i++) {
			double theta = start + arc / (n - 1) * i;
			float x = (float) (MathUtils.cos(theta) * radius);
			float y = (float) (MathUtils.sin(theta) * radius);
			buf.put(x).put(y).put(0);
		}
		getMeshData().updateVertexCount();
	}

//	private FloatBuffer createColor(int samples) {
//		FloatBuffer buf = BufferUtils.createColorBuffer(samples);
//		for (int i = 0; i < samples; i++) {
//			buf.put(1).put(1).put(1).put(1);
//		}
//		return buf;
//	}
//
//	private FloatBuffer createNormal(int samples) {
//		FloatBuffer buf = BufferUtils.createVector3Buffer(samples);
//		for (int i = 0; i < samples; i++) {
//			buf.put(0).put(0).put(1);
//		}
//		return buf;
//	}

}