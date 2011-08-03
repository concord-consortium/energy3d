package org.concord.energy3d.shapes;

import java.nio.FloatBuffer;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.Util;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.geom.BufferUtils;
import com.sun.org.apache.xml.internal.serializer.utils.Utils;

public class SizeAnnotation extends Annotation {
	protected final Mesh arrows = new Mesh("Arrows");
	protected final BMText label = makeNewLabel();
	
	public SizeAnnotation() {
		super(new Line("Size annotation lines", BufferUtils.createVector3Buffer(12), null, null, null));
		arrows.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(6));
		arrows.setDefaultColor(ColorRGBA.BLACK);
		this.attachChild(arrows);
		this.attachChild(label);
	}
	
	public void setRange(ReadOnlyVector3 from, ReadOnlyVector3 to, final ReadOnlyVector3 center, final ReadOnlyVector3 faceDirection, final boolean front, final Align align, boolean autoFlipDirection) {
		setRange(from, to, center, faceDirection, front, align, autoFlipDirection, false);
	}
	
	public void setRange(ReadOnlyVector3 from, ReadOnlyVector3 to, final ReadOnlyVector3 center, final ReadOnlyVector3 faceDirection, final boolean front, final Align align, boolean autoFlipDirection, final boolean rotateTextAlongLine) {
		final double C = 0.1;
		final Vector3 v = new Vector3();
//		if (to.subtract(from, null).normalizeLocal().crossLocal(faceDirection).dot(Vector3.NEG_UNIT_Z) < 0) {
//			final ReadOnlyVector3 tmp = from;
//			from = to;
//			to = tmp;
//		}
		final Vector3 offset = new Vector3();
		if (front)
			offset.set(faceDirection).normalizeLocal().multiplyLocal(C);
		else {
			offset.set(to).subtractLocal(from).normalizeLocal().crossLocal(faceDirection).multiplyLocal(C);
			if (autoFlipDirection) {
				v.set(from).subtractLocal(center).normalizeLocal();
				if (v.dot(offset) < 0)
					offset.negateLocal();
			}
		}
		
		double angle = faceDirection.smallestAngleBetween(Vector3.NEG_UNIT_Y);
		if (faceDirection.dot(Vector3.UNIT_X) < 0)
			angle = -angle;
//		if (faceDirection.dot(Vector3.UNIT_Z) > 1.0 - MathUtils.ZERO_TOLERANCE) {
		if (rotateTextAlongLine) {
			final double zRot = Util.angleBetween(Vector3.NEG_UNIT_Y, offset.normalize(null), Vector3.UNIT_Z);
			label.setRotation(new Matrix3().fromAngles(-Math.PI / 2, 0, zRot));
		} else
			label.setRotation(new Matrix3().fromAngles(0, 0, angle));

		FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();

		// main line
		final Vector3 newFrom = new Vector3(from).addLocal(offset);
		final Vector3 newTo = new Vector3(to).addLocal(offset);
		final Vector3 middle = new Vector3(newFrom).addLocal(newTo).multiplyLocal(0.5);
		final Vector3 body = new Vector3(to).subtractLocal(from).multiplyLocal(0.5);
		vertexBuffer.put(newFrom.getXf()).put(newFrom.getYf()).put(newFrom.getZf());
		double s = (body.length() - 0.15) / body.length();
		v.set(body).multiplyLocal(s).addLocal(newFrom);
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		v.set(body).multiplyLocal(-s).addLocal(newTo);
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		vertexBuffer.put(newTo.getXf()).put(newTo.getYf()).put(newTo.getZf());

		offset.multiplyLocal(0.5);
		// from End
		v.set(from);
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		v.set(newFrom).addLocal(offset);
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());

		// to End
		v.set(to);
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		v.set(newTo).addLocal(offset);
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());

		// arrow
		offset.multiplyLocal(0.5);
		body.set(to).subtractLocal(from).normalizeLocal().multiplyLocal(0.05);
		
		mesh.updateModelBound();

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
				
		arrows.updateModelBound();

		label.setTranslation(middle);		
		label.setText("" + Math.round(to.subtract(from, null).length() * Scene.getInstance().getAnnotationScale() * 100) / 100.0 + Scene.getInstance().getUnit().getNotation());
		label.setAlign(align);
		
		label.updateModelBound();
	}
}
