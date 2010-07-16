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
	public Arc(String name) {
		super(name);
		setAntialiased(true);
	}

	public void set(int samples, double radius, double start, double end) {
		FloatBuffer vertexBuf = createVertex(samples, radius, start, end);
//		FloatBuffer normalBuf = createNormal(samples);
//		FloatBuffer colorBuf = createColor(samples);

		getMeshData().setVertexBuffer(vertexBuf);
//		getMeshData().setNormalBuffer(normalBuf);
//		getMeshData().setColorBuffer(colorBuf);
		getMeshData().setIndexMode(IndexMode.LineStrip);
		getSceneHints().setLightCombineMode(LightCombineMode.Off);
		setAntialiased(true);
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

	private FloatBuffer createVertex(int sample, double radius, double start, double end) {
		FloatBuffer buf = BufferUtils.createVector3Buffer(sample);

		buf.rewind();
		double arc = end - start;
		for (int i = 0; i < sample; i++) {
			double theta = start + arc / (sample - 1) * i;
			float x = (float) (MathUtils.cos(theta) * radius);
			float y = (float) (MathUtils.sin(theta) * radius);
			buf.put(x).put(y).put(0);
		}
		return buf;
	}
}