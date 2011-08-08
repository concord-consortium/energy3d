package org.concord.energy3d.shapes;

import java.nio.FloatBuffer;

import com.ardor3d.math.MathUtils;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.util.geom.BufferUtils;

/**
 * As Circle consists of Line Segments, you can use Line methods to set property such as width, stipple.
 * 
 * @author mulova
 */
public class Arc extends Line {

	/**
	 * create line with
	 * 
	 * @param name
	 *            mesh name
	 * @param samples
	 *            sampling rate
	 * @param radius
	 */
	public Arc(final String name, final int vertices) {
		super(name);
		setAntialiased(true);
		getMeshData().setIndexMode(IndexMode.LineStrip);
		getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(vertices));
		getSceneHints().setLightCombineMode(LightCombineMode.Off);		
	}

	public void set(final double radius, final double start, final double end) {
		final FloatBuffer buf = getMeshData().getVertexBuffer();
		buf.limit(buf.capacity());
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