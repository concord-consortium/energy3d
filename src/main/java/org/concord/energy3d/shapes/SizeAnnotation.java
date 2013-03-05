package org.concord.energy3d.shapes;

import java.nio.FloatBuffer;
import java.text.DecimalFormat;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.geom.BufferUtils;

public class SizeAnnotation extends Annotation {
	private final Mesh arrows = new Mesh("Arrows");
	private ReadOnlyVector3 from;
	private ReadOnlyVector3 to;
	private ReadOnlyVector3 center;
	private ReadOnlyVector3 faceDirection;
	private Align align;
	private boolean front;
	private boolean autoFlipOffset;
	private boolean upsideDownText;
	private boolean drawInside;

	public SizeAnnotation() {
		super(new Line("Size annotation lines", BufferUtils.createVector3Buffer(8), null, null, null));
		arrows.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(6));
		arrows.setModelBound(new BoundingBox());
		Util.disablePickShadowLight(arrows);
		setColor(ColorRGBA.BLACK);
		attachChild(arrows);
		attachChild(label);
	}

	public void setRange(final ReadOnlyVector3 from, final ReadOnlyVector3 to, final ReadOnlyVector3 center, final ReadOnlyVector3 faceDirection, final boolean front, final Align align, final boolean autoFlipOffset, final boolean upsideDownText, final boolean drawInside) {
		if (from.distanceSquared(to) < MathUtils.ZERO_TOLERANCE)
			throw new RuntimeException("The 'from' and 'to' vectors are almost the same.");
		this.from = from;
		this.to = to;
		this.center = center;
		this.faceDirection = faceDirection;
		this.front = front;
		this.align = align;
		this.autoFlipOffset = autoFlipOffset;
		this.upsideDownText = upsideDownText;
		this.drawInside = drawInside;
		draw();
	}

	@Override
	public void draw() {
		final double C = 1.0;
		final Vector3 v = new Vector3();
		final Vector3 offset = new Vector3();
		if (front && !drawInside)
			offset.set(faceDirection).normalizeLocal().multiplyLocal(C).addLocal(0, 0, 0.05);
		else {
			offset.set(to).subtractLocal(from).normalizeLocal().crossLocal(faceDirection).multiplyLocal(C);
			if (autoFlipOffset) {
				v.set(from).subtractLocal(center).normalizeLocal();
				if (v.dot(offset) < 0)
					offset.negateLocal();
			}
		}

		if (drawInside)
			offset.negateLocal();

		final ReadOnlyVector3 dir = to.subtract(from, null).normalizeLocal();
		final int scale = upsideDownText ? -1 : 1;

//		label.setRotation(new Matrix3().fromAxes(dir.multiply(scale, null), faceDirection, faceDirection.cross(dir, null).multiplyLocal(scale)));

		final Vector3 xdir = dir.multiply(scale, null);
		final Vector3 ydir = faceDirection.normalize(null);
		final Vector3 zdir = ydir.cross(xdir, null).normalizeLocal();
		zdir.cross(ydir, xdir);
		final Matrix3 matrix = new Matrix3().fromAxes(xdir, ydir, zdir);
		label.setRotation(matrix);

		FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();

		// main line
		final Vector3 newFrom = new Vector3(from).addLocal(offset);
		final Vector3 newTo = new Vector3(to).addLocal(offset);
		final Vector3 middle = new Vector3(newFrom).addLocal(newTo).multiplyLocal(0.5);
		final Vector3 body = new Vector3(to).subtractLocal(from).multiplyLocal(0.5);

		label.setTranslation(middle);
		final DecimalFormat df = new DecimalFormat("#.##");
		label.setText("" + df.format(to.subtract(from, null).length() * Scene.getInstance().getAnnotationScale()) + Scene.getInstance().getUnit().getNotation());
		label.setAlign(align);
		label.updateWorldTransform(true);
		label.updateWorldBound(true);


		vertexBuffer.put(newFrom.getXf()).put(newFrom.getYf()).put(newFrom.getZf());
		final double bankSpace = label.getWidth() * 0.70;
		final double blankSpaceFactor = Math.max(0, body.length() - bankSpace) / body.length();
		v.set(body).multiplyLocal(blankSpaceFactor).addLocal(newFrom);
		vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		v.set(body).multiplyLocal(-blankSpaceFactor).addLocal(newTo);
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
		body.set(to).subtractLocal(from).normalizeLocal().multiplyLocal(0.5);

		mesh.updateModelBound();;

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

		updateWorldTransform(true);
		updateWorldBound(true);
//		this.setTranslation(faceDirection.multiply(0.05, null));
	}

	@Override
	public void setColor(final ReadOnlyColorRGBA color) {
		super.setColor(color);
		arrows.setDefaultColor(color);
	}

    @Override
    public Node makeCopy(final boolean shareGeometricData) {
        // get copy of basic spatial info
        final Node node = super.makeCopy(shareGeometricData);
        // because the above code calls the constructor of this object (which adds 3 children) and then clones the node.children (which then adds extra 3 children) we need to undo the effect of last step
        node.detachChildAt(5);
        node.detachChildAt(4);
        node.detachChildAt(3);
        return node;
    }
}
