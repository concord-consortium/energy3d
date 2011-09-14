package org.concord.energy3d.model;

import java.nio.FloatBuffer;

import org.concord.energy3d.shapes.Annotation;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyTransform;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.geom.BufferUtils;

public class Window extends HousePart {
	private static final long serialVersionUID = 1L;
	private static final double GRID_SIZE = 0.15;
//	private transient Mesh mesh;
//	private transient FloatBuffer vertexBuffer;
//	private transient FloatBuffer normalBuffer;
	transient private BMText label1;
	transient private Line bars;

	public Window() {
		super(2, 4, 0.30);
	}

	protected void init() {
		label1 = Annotation.makeNewLabel();
		super.init();
////		for (int i = 0; i < points.size(); i++)
////			getAbsPoint(i).set(toAbsolute(getAbsPoint(i)));
		mesh = new Mesh("Window");
		mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		mesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(4));
		mesh.setModelBound(new BoundingBox());
		mesh.getSceneHints().setCullHint(CullHint.Always);

		// Transparency
		mesh.setDefaultColor(new ColorRGBA(0.3f, 0.4f, 0.5f, 0.7f));
		BlendState blendState = new BlendState();
		blendState.setBlendEnabled(true);
		blendState.setTestEnabled(true);
		mesh.setRenderState(blendState);
		mesh.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);

		// Add a material to the box, to show both vertex color and lighting/shading.
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.AmbientAndDiffuse);
		mesh.setRenderState(ms);
		mesh.setUserData(new UserData(this));
		
		root.attachChild(mesh);
		
		label1.setAlign(Align.SouthWest);
		root.attachChild(label1);
		
		bars = new Line("Window (bars)");
//		bars.setAntialiased(true);
//        final BlendState blend = new BlendState();
//        blend.setBlendEnabled(true);
//        bars.setRenderState(blend);		
		bars.setLineWidth(3);
		bars.setModelBound(new BoundingBox());
//		bars.getSceneHints().setCastsShadows(false);
//		bars.getSceneHints().setPickingHint(PickingHint.Pickable, false);
//		bars.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		Util.disablePickShadowLight(bars);
		bars.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(8));
		root.attachChild(bars);
	}

	public void addPoint(int x, int y) {
		if (container != null)
			super.addPoint(x, y);
	}

	public void setPreviewPoint(int x, int y) {
		int index = editPointIndex;
		if (index == -1) {
			if (isFirstPointInserted())
				index = 3;
			else
				index = 0;
		}
		PickedHousePart pick = pickContainer(x, y, Wall.class);
		Vector3 p = points.get(index);
		if (pick != null) {
			p = pick.getPoint();
			p = grid(p, GRID_SIZE);
			p = toRelative(p);
		}
		points.get(index).set(p);
		if (!isFirstPointInserted()) {
			points.get(1).set(p);
		} else {
			if (index == 0 || index == 3) {
				points.get(1).set(points.get(0).getX(), 0, points.get(3).getZ());
				points.get(2).set(points.get(3).getX(), 0, points.get(0).getZ());
			} else {
				points.get(0).set(points.get(1).getX(), 0, points.get(2).getZ());
				points.get(3).set(points.get(2).getX(), 0, points.get(1).getZ());
			}
		}

//		draw();
//		showPoints();

		if (container != null) {
			draw();
			showPoints();
			container.draw();
		}
	}

	protected void drawMesh() {
		if (points.size() < 4)
			return;
		
		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		for (int i = 0; i < points.size(); i++) {
			final ReadOnlyVector3 p = getAbsPoint(i);
			vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
		}

		// Compute normals
		final Vector3 normal = getAbsPoint(2).subtract(getAbsPoint(0), null).crossLocal(getAbsPoint(1).subtract(getAbsPoint(0), null)).normalizeLocal();
		normal.negateLocal();
		final FloatBuffer normalBuffer = mesh.getMeshData().getNormalBuffer();
		normalBuffer.rewind();
		for (int i = 0; i < points.size(); i++)
			normalBuffer.put(normal.getXf()).put(normal.getYf()).put(normal.getZf());

		mesh.updateModelBound();
		
		final double divisionLength = 0.3;
		final Vector3 halfThickness = ((Wall)container).getThicknessNormal().multiply(0.5, null);
		FloatBuffer barsVertices = bars.getMeshData().getVertexBuffer();
//		FloatBuffer barsNormals = bars.getMeshData().getNormalBuffer();
		final int cols = (int)Math.max(2, getAbsPoint(0).distance(getAbsPoint(2)) / divisionLength);
		final int rows = (int)Math.max(2, getAbsPoint(0).distance(getAbsPoint(1)) / divisionLength);
		if (barsVertices.capacity() < (4 + rows + cols) * 6) {
			barsVertices = BufferUtils.createVector3Buffer((4 + rows + cols) * 2);
//			barsNormals = BufferUtils.createVector3Buffer((4 + rows + cols) * 2);
			bars.getMeshData().setVertexBuffer(barsVertices);
			bars.getMeshData().setNormalBuffer(barsVertices);
		} else {
			barsVertices.rewind();
			barsVertices.limit(barsVertices.capacity());
//			barsNormals.rewind();
//			barsNormals.limit(barsVertices.capacity());			
		}
			
		barsVertices.rewind();		
		final Vector3 p = new Vector3();
		getAbsPoint(0).add(halfThickness, p);
		barsVertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
		getAbsPoint(1).add(halfThickness, p);
		barsVertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
		getAbsPoint(1).add(halfThickness, p);
		barsVertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
		getAbsPoint(3).add(halfThickness, p);
		barsVertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
		getAbsPoint(3).add(halfThickness, p);
		barsVertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
		getAbsPoint(2).add(halfThickness, p);
		barsVertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
		getAbsPoint(2).add(halfThickness, p);
		barsVertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
		getAbsPoint(0).add(halfThickness, p);
		barsVertices.put(p.getXf()).put(p.getYf()).put(p.getZf());		
		
		final ReadOnlyVector3 o = getAbsPoint(0).add(halfThickness, null);
		final ReadOnlyVector3 u = getAbsPoint(2).subtract(getAbsPoint(0), null);
		final ReadOnlyVector3 v = getAbsPoint(1).subtract(getAbsPoint(0), null);
		for (int col = 1; col < cols; col++) {
			u.multiply((double)col/cols, p).addLocal(o);
			barsVertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
			p.addLocal(v);
			barsVertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
		}
		for (int row = 1; row < rows; row++) {
			v.multiply((double)row/rows, p).addLocal(o);
			barsVertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
			p.addLocal(u);
			barsVertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
		}
		p.set(halfThickness).negateLocal().normalizeLocal();
//		for (int i = 0; i < barsVertices.position() / 3; i++) {
//			barsNormals.put(p.getXf()).put(p.getYf()).put(p.getZf());
//			barsNormals.put(1).put(1).put(1);
//		}
		barsVertices.limit(barsVertices.position());
//		barsNormals.limit(barsNormals.position());
		bars.getMeshData().updateVertexCount();
		bars.updateModelBound();
	}

	@Override
	protected void drawAnnotations() {
		if (points.size() < 4)
			return;
		int annotCounter = 0;

		final ReadOnlyVector3 v02 = container.getAbsPoint(2).subtract(container.getAbsPoint(0), null);
//		if (label1 == null) {
//			label1 = Annotation.makeNewLabel();
//			label1.setAlign(Align.SouthWest);
//			root.attachChild(label1);
//		}
		
		final boolean reversedFace = v02.normalize(null).crossLocal(container.getFaceDirection()).dot(Vector3.NEG_UNIT_Z) < 0.0;
		final boolean reversedH;
		if (points.get(0).getX() > points.get(2).getX())
			reversedH = !reversedFace;
		else
			reversedH = reversedFace;
		final boolean reversedV = getAbsPoint(0).getZ() > getAbsPoint(1).getZ();
		
		final int i0, i1, i2;
		if (reversedH && reversedV) {
			i0 = 3;
			i2 = 1;
			i1 = 2;
		} else if (reversedH) {
			i0 = 2;
			i1 = 3;
			i2 = 0;
		} else if (reversedV) {
			i0 = 1;
			i1 = 0;
			i2 = 3;
		} else {
			i0 = 0;
			i1 = 1;
			i2 = 2;
		}
		
		
		final Vector3 cornerXY = getAbsPoint(i0).subtract(container.getAbsPoint(0), null);
		cornerXY.setZ(0);		
		double xy = cornerXY.length(); //Math.sqrt(getAbsPoint(i1).getX() * getAbsPoint(i1).getX() + getAbsPoint(i1).getY() * getAbsPoint(i1).getY());
		if (reversedFace)
			xy = v02.length() - xy; 
		label1.setText("(" + Math.round(10 * xy) / 10.0 + ", " + Math.round(10 * getAbsPoint(i0).getZf()) / 10.0 + ")");
		
		final ReadOnlyTransform trans = container.getRoot().getTransform();
		final ReadOnlyVector3 faceDirection = trans.applyForwardVector(container.getFaceDirection(), null);
//		final ReadOnlyVector3 moveToFront = faceDirection.multiply(0.04, null);
		label1.setTranslation(getAbsPoint(i0));
		label1.setRotation(new Matrix3().fromAngles(0, 0, -Util.angleBetween(v02.normalize(null).multiplyLocal(reversedFace ? -1 : 1), Vector3.UNIT_X, Vector3.UNIT_Z)));
		SizeAnnotation annot = fetchSizeAnnot(annotCounter++);
		final Vector3 center = trans.applyForward(getCenter(), null);
		annot.setRange(getAbsPoint(i0), getAbsPoint(i1), center, faceDirection, false, Align.Center, true, true, false);
//		annot.setTranslation(moveToFront);

		annot = fetchSizeAnnot(annotCounter++);
		annot.setRange(getAbsPoint(i0), getAbsPoint(i2), center, faceDirection, false, Align.Center, true, false, false);
//		annot.setTranslation(moveToFront);
	}

	public boolean isPrintable() {
		return false;
	}

	public void setAnnotationsVisible(boolean visible) {
		super.setAnnotationsVisible(visible);
		if (label1 != null)
			label1.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
	}
	
//	protected void computeAbsPoints() {
//		for (int i = 0; i < points.size(); i++) {
//			final Vector3 p = toAbsolute(enforceContraints(points.get(i)));
//			getAbsPoint(i).set(p);
//			toAbsolute(enforceContraints(points.get(i)));
//			pointsRoot.getChild(i).setTranslation(p);
//		}
//	}
	
	private Vector3 enforceContraints(Vector3 p) {
		if (container == null)
			return new Vector3(p);
		final double wallx = container.getAbsPoint(2).subtract(container.getAbsPoint(0), null).length();
		final double margin = 0.2 / wallx;
		double x = Math.max(p.getX(), margin);
		x = Math.min(x, 1 - margin);
		return new Vector3(x, p.getY(), p.getZ());
	}
	
	public void updateTextureAndColor(final boolean textureEnabled) {
	}
	
	public void hideBars() {
		if (bars != null)
			bars.getSceneHints().setCullHint(CullHint.Always);
	}
	
	public Vector3 getAbsPoint(final int index) {
		final Vector3 absolute = toAbsolute(enforceContraints(points.get(index)));
		if (container != null)
			container.getRoot().getTransform().applyForward(absolute);
		return absolute;
	}
	
	@Override
	public void setOriginal(final HousePart original) {
		super.setOriginal(original);
//		root.detachChild(sizeAnnotRoot);
//		container.getRoot().attachChild(sizeAnnotRoot);
	}
	
}
