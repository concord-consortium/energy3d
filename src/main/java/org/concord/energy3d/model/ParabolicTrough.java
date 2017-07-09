package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.Calendar;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.Atmosphere;
import org.concord.energy3d.util.FontManager;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.extension.effect.bloom.BloomRenderPass;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyTransform;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.renderer.state.CullState.Face;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Cylinder;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.ui.text.BMText.Justify;
import com.ardor3d.util.geom.BufferUtils;

public class ParabolicTrough extends HousePart implements Solar {

	private static final long serialVersionUID = 1L;
	private static final ColorRGBA SKY_BLUE = new ColorRGBA(135f / 256f, 206f / 256f, 250f / 256f, 1);
	private transient ReadOnlyVector3 normal;
	private transient ParabolicCylinder reflector;
	private transient Mesh reflectorBack;
	private transient Cylinder absorber, absorberCore;
	private transient Cylinder absorberEnd1, absorberEnd1Core;
	private transient Cylinder absorberEnd2, absorberEnd2Core;
	private transient Line outlines;
	private transient Line steelFrame;
	private transient Node unitsRoot;
	private transient Line lightBeams;
	private transient BMText label;
	private transient double copyLayoutGap = 1;
	private transient double yieldNow; // solar output at current hour
	private transient double yieldToday;
	private double reflectivity = 0.9; // a number in (0, 1), iron glass has a reflectivity of 0.9 (but dirt and dust reduce it to 0.82, this is accounted for by Atmosphere)
	private double troughLength = 6;
	private double troughWidth = 2;
	private double unitLength = 2;
	private double semilatusRectum = 2;
	private double relativeAzimuth = 0;
	private double baseHeight = 5;
	private boolean beamsVisible;
	private boolean labelEnergyOutput;
	private transient Vector3 oldTroughCenter;
	private transient double oldTroughLength, oldTroughWidth;
	private transient double oldRelativeAzimuth;
	private static transient BloomRenderPass bloomRenderPassLight, bloomRenderPassTube;
	private transient double baseZ;
	private int nSectionParabola = 16; // number of sections for the parabola cross section of a parabolic trough (must be power of 2)
	private int nSectionAxis = 32; // number of sections in the axis of a parabolic trough (must be power of 2)

	public ParabolicTrough() {
		super(1, 1, 0);
	}

	@Override
	protected void init() {
		super.init();

		if (Util.isZero(copyLayoutGap)) { // FIXME: Why is a transient member evaluated to zero?
			copyLayoutGap = 1;
		}
		if (Util.isZero(troughLength)) {
			troughLength = 6;
		}
		if (Util.isZero(troughWidth)) {
			troughWidth = 2;
		}
		if (Util.isZero(unitLength)) {
			unitLength = 2;
		}
		if (Util.isZero(semilatusRectum)) {
			semilatusRectum = 2;
		}
		if (Util.isZero(reflectivity)) {
			reflectivity = 0.9;
		}
		if (Util.isZero(nSectionParabola)) {
			nSectionParabola = 16;
		}
		if (Util.isZero(nSectionAxis)) {
			nSectionAxis = 32;
		}

		mesh = new ParabolicCylinder("Parabolic Cylinder", nSectionParabola, semilatusRectum, troughWidth, troughLength);
		mesh.setDefaultColor(SKY_BLUE);
		mesh.setModelBound(new OrientedBoundingBox());
		mesh.setUserData(new UserData(this));
		CullState cullState = new CullState();
		cullState.setCullFace(Face.Back);
		mesh.setRenderState(cullState);
		root.attachChild(mesh);
		reflector = (ParabolicCylinder) mesh;
		reflectorBack = mesh.makeCopy(true);
		reflectorBack.clearRenderState(StateType.Texture);
		reflectorBack.setDefaultColor(ColorRGBA.WHITE);
		cullState = new CullState();
		cullState.setCullFace(Face.None);
		reflectorBack.setRenderState(cullState);
		root.attachChild(reflectorBack);

		final ColorRGBA tubeColor = new ColorRGBA(0.8f, 0.8f, 0.8f, 0.8f);
		absorber = new Cylinder("Absorber Tube", 2, 10, 0.5, 0, true);
		final BlendState blend = new BlendState();
		blend.setBlendEnabled(true);
		absorber.setRenderState(blend);
		absorber.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
		absorber.setDefaultColor(tubeColor);
		absorber.setModelBound(new OrientedBoundingBox());
		root.attachChild(absorber);

		absorberCore = new Cylinder("Absorber Tube Core", 2, 4, 0.4, 0, true);
		absorberCore.setDefaultColor(ColorRGBA.BROWN);
		absorberCore.setModelBound(new OrientedBoundingBox());
		root.attachChild(absorberCore);

		absorberEnd1 = new Cylinder("Absorber End Tube 1", 2, 10, 0.5, 0, true);
		absorberEnd1.setRenderState(blend);
		absorberEnd1.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
		absorberEnd1.setDefaultColor(tubeColor);
		absorberEnd1.setModelBound(new OrientedBoundingBox());
		root.attachChild(absorberEnd1);

		absorberEnd1Core = new Cylinder("Absorber End Tube 1 Core", 2, 4, 0.4, 0, true);
		absorberEnd1Core.setDefaultColor(ColorRGBA.BROWN);
		absorberEnd1Core.setModelBound(new OrientedBoundingBox());
		root.attachChild(absorberEnd1Core);

		absorberEnd2 = new Cylinder("Absorber End Tube 2", 2, 10, 0.5, 0, true);
		absorberEnd2.setRenderState(blend);
		absorberEnd2.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
		absorberEnd2.setDefaultColor(tubeColor);
		absorberEnd2.setModelBound(new OrientedBoundingBox());
		root.attachChild(absorberEnd2);

		absorberEnd2Core = new Cylinder("Absorber End Tube 2 Core", 2, 4, 0.4, 0, true);
		absorberEnd2Core.setDefaultColor(ColorRGBA.BROWN);
		absorberEnd2Core.setModelBound(new OrientedBoundingBox());
		root.attachChild(absorberEnd2Core);

		final int nUnits = (int) Math.round(troughLength / unitLength);
		outlines = new Line("Parabolic Trough (Outline)");
		outlines.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4 + 2 * (reflector.getNumberOfSamples() + 1) * (nUnits + 1)));
		outlines.setDefaultColor(ColorRGBA.BLACK);
		outlines.setModelBound(new OrientedBoundingBox());
		outlines.setLineWidth(0.01f);
		outlines.setStipplePattern((short) 0xffff);
		Util.disablePickShadowLight(outlines);
		root.attachChild(outlines);

		steelFrame = new Line("Parabolic Trough Steel Frame");
		steelFrame.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		steelFrame.setDefaultColor(ColorRGBA.GRAY);
		steelFrame.setModelBound(new OrientedBoundingBox());
		steelFrame.setLineWidth(3);
		steelFrame.setStipplePattern((short) 0xffff);
		root.attachChild(steelFrame);

		lightBeams = new Line("Light Beams");
		lightBeams.setLineWidth(0.01f);
		lightBeams.setStipplePattern((short) 0xffff);
		lightBeams.setModelBound(null);
		Util.disablePickShadowLight(lightBeams);
		lightBeams.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4));
		lightBeams.setDefaultColor(new ColorRGBA(1f, 1f, 1f, 1f));
		root.attachChild(lightBeams);

		label = new BMText("Label", "#" + id, FontManager.getInstance().getPartNumberFont(), Align.Center, Justify.Center);
		Util.initHousePartLabel(label);
		label.setFontScale(0.5);
		label.setVisible(false);
		root.attachChild(label);

		unitsRoot = new Node("Units Root");
		root.attachChild(unitsRoot);
		updateTextureAndColor();

		if (!points.isEmpty()) {
			oldTroughCenter = points.get(0).clone();
		}
		oldTroughLength = troughLength;
		oldTroughWidth = troughWidth;

	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		if (editPointIndex <= 0) {
			final PickedHousePart picked = pickContainer(x, y, new Class<?>[] { Foundation.class });
			if (picked != null && picked.getUserData() != null) { // when the user data is null, it picks the land
				final Vector3 p = picked.getPoint().clone();
				final UserData ud = picked.getUserData();
				snapToGrid(p, getAbsPoint(0), getGridSize(), container instanceof Wall);
				points.get(0).set(toRelative(p));
				pickedNormal = ud.getRotatedNormal() == null ? ud.getNormal() : ud.getRotatedNormal();
			} else {
				pickedNormal = null;
			}
			if (outOfBound()) {
				if (oldTroughCenter != null) {
					points.get(0).set(oldTroughCenter);
				}
			} else {
				oldTroughCenter = points.get(0).clone();
			}
		} else {
			final ReadOnlyVector3 pEdit = getEditPointShape(editPointIndex).getTranslation();
			final Vector3 p;
			if (editPointIndex % 2 == 0) {
				final ReadOnlyVector3 p1 = getEditPointShape(editPointIndex == 2 ? 4 : 2).getTranslation();
				p = Util.closestPoint(pEdit, pEdit.subtract(p1, null).normalizeLocal(), x, y);
				if (p != null) {
					final double rl = p.distance(p1) * Scene.getInstance().getAnnotationScale();
					final Vector3 delta = toRelativeVector(p.subtract(pEdit, null)).multiplyLocal(0.5);
					points.get(0).addLocal(delta);
					getEditPointShape(editPointIndex).setTranslation(p);
					setTroughLength(rl);
					if (outOfBound()) {
						if (oldTroughCenter != null) {
							points.get(0).set(oldTroughCenter);
						}
						setTroughLength(oldTroughLength);
					} else {
						oldTroughCenter = points.get(0).clone();
						oldTroughLength = troughLength;
					}
				}
			} else {
				final ReadOnlyVector3 p1 = getEditPointShape(editPointIndex == 1 ? 3 : 1).getTranslation();
				p = Util.closestPoint(pEdit, pEdit.subtract(p1, null).normalizeLocal(), x, y);
				if (p != null) {
					final double rw = p.distance(p1) * Scene.getInstance().getAnnotationScale();
					final Vector3 delta = toRelativeVector(p.subtract(pEdit, null)).multiplyLocal(0.5);
					points.get(0).addLocal(delta);
					getEditPointShape(editPointIndex).setTranslation(p);
					setTroughWidth(rw);
					if (outOfBound()) {
						if (oldTroughCenter != null) {
							points.get(0).set(oldTroughCenter);
						}
						setTroughWidth(oldTroughWidth);
					} else {
						oldTroughCenter = points.get(0).clone();
						oldTroughWidth = troughWidth;
					}
				}
			}
		}
		if (container != null) {
			draw();
			drawChildren();
			setEditPointsVisible(true);
			setHighlight(!isDrawable());
		}
	}

	private boolean outOfBound() {
		drawMesh();
		if (container instanceof Foundation) {
			final Foundation foundation = (Foundation) container;
			final int n = Math.round(mesh.getMeshData().getVertexBuffer().limit() / 3);
			for (int i = 0; i < n; i++) {
				final Vector3 a = getVertex(i);
				if (a.getZ() < foundation.getHeight() * 1.1) { // left a 10% margin above the foundation
					return true;
				}
				if (!foundation.containsPoint(a.getX(), a.getY())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void drawMesh() {
		if (container == null) {
			return;
		}

		getEditPointShape(0).setDefaultColor(ColorRGBA.ORANGE);

		normal = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).multiply(1, 0, 1, null).normalize(null);
		if (Util.isEqual(normal, Vector3.UNIT_Z)) {
			normal = new Vector3(-0.001, 0, 1).normalizeLocal();
		}

		final double annotationScale = Scene.getInstance().getAnnotationScale();
		reflector.setSize(troughWidth / annotationScale, troughLength / annotationScale);
		reflector.setSemilatusRectum(semilatusRectum / annotationScale);
		reflector.updateModelBound();
		baseZ = container instanceof Foundation ? container.getHeight() : container.getPoints().get(0).getZ();
		points.get(0).setZ(baseZ + baseHeight);
		absorber.setHeight(reflector.getHeight());
		absorberCore.setHeight(reflector.getHeight() - 1);
		absorberEnd1.setHeight(0.5 * reflector.getSemilatusRectum());
		absorberEnd2.setHeight(absorberEnd1.getHeight());
		absorberEnd1Core.setHeight(absorberEnd1.getHeight() - 1);
		absorberEnd2Core.setHeight(absorberEnd2.getHeight() - 1);

		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		FloatBuffer outlineBuffer = outlines.getMeshData().getVertexBuffer();
		FloatBuffer steelFrameBuffer = steelFrame.getMeshData().getVertexBuffer();

		final int vertexCount = vertexBuffer.limit() / 6;
		final int j = vertexCount * 3; // number of vertex coordinates on each end
		final int j1 = (j - 3) / 2; // start index of middle point on end 1
		final int j2 = j + j1; // start index of middle point on end 2
		final Vector3 p1 = new Vector3(vertexBuffer.get(j1), vertexBuffer.get(j1 + 1), vertexBuffer.get(j1 + 2)); // middle point on end 1
		final Vector3 p2 = new Vector3(vertexBuffer.get(j2), vertexBuffer.get(j2 + 1), vertexBuffer.get(j2 + 2)); // middle point on end 2
		final Vector3 pd = p2.subtract(p1, null).normalizeLocal(); // normal in the direction of cylinder axis
		final double halfLength = troughLength * 0.5;
		final Vector3 center = getAbsPoint(0);

		final int nUnits = (int) Math.round(troughLength / unitLength);
		final int outlineBufferSize = 6 * (vertexCount - 1) * (nUnits + 3) + 12; // 12 is for the two lateral lines
		if (outlineBuffer.capacity() < outlineBufferSize) {
			outlineBuffer = BufferUtils.createFloatBuffer(outlineBufferSize);
			outlines.getMeshData().setVertexBuffer(outlineBuffer);
		} else {
			outlineBuffer.clear();
			outlineBuffer.rewind();
			outlineBuffer.limit(outlineBufferSize);
		}
		// draw parabolic lines of the two end faces
		for (int i = 0; i < vertexCount - 1; i++) {
			outlineBuffer.put(vertexBuffer.get(i * 3)).put(vertexBuffer.get(i * 3 + 1)).put(vertexBuffer.get(i * 3 + 2));
			outlineBuffer.put(vertexBuffer.get(i * 3 + 3)).put(vertexBuffer.get(i * 3 + 4)).put(vertexBuffer.get(i * 3 + 5));
			outlineBuffer.put(vertexBuffer.get(j + i * 3)).put(vertexBuffer.get(j + i * 3 + 1)).put(vertexBuffer.get(j + i * 3 + 2));
			outlineBuffer.put(vertexBuffer.get(j + i * 3 + 3)).put(vertexBuffer.get(j + i * 3 + 4)).put(vertexBuffer.get(j + i * 3 + 5));
		}
		// draw lateral lines connecting the two end faces
		outlineBuffer.put(vertexBuffer.get(0)).put(vertexBuffer.get(1)).put(vertexBuffer.get(2));
		outlineBuffer.put(vertexBuffer.get(j)).put(vertexBuffer.get(j + 1)).put(vertexBuffer.get(j + 2));
		outlineBuffer.put(vertexBuffer.get(j - 3)).put(vertexBuffer.get(j - 2)).put(vertexBuffer.get(j - 1));
		outlineBuffer.put(vertexBuffer.get(2 * j - 3)).put(vertexBuffer.get(2 * j - 2)).put(vertexBuffer.get(2 * j - 1));
		// draw seam lines between units
		for (double u = halfLength; u < troughLength; u += unitLength) {
			for (int i = 0; i < vertexCount - 1; i++) {
				final Vector3 v1 = new Vector3(vertexBuffer.get(i * 3), vertexBuffer.get(i * 3 + 1), vertexBuffer.get(i * 3 + 2));
				final Vector3 v2 = new Vector3(vertexBuffer.get(i * 3 + 3), vertexBuffer.get(i * 3 + 4), vertexBuffer.get(i * 3 + 5));
				v1.addLocal(0, u / annotationScale, 0);
				v2.addLocal(0, u / annotationScale, 0);
				outlineBuffer.put(v1.getXf()).put(v1.getYf()).put(v1.getZf());
				outlineBuffer.put(v2.getXf()).put(v2.getYf()).put(v2.getZf());
			}
		}
		for (double u = halfLength - unitLength; u > 0; u -= unitLength) {
			for (int i = 0; i < vertexCount - 1; i++) {
				final Vector3 v1 = new Vector3(vertexBuffer.get(i * 3), vertexBuffer.get(i * 3 + 1), vertexBuffer.get(i * 3 + 2));
				final Vector3 v2 = new Vector3(vertexBuffer.get(i * 3 + 3), vertexBuffer.get(i * 3 + 4), vertexBuffer.get(i * 3 + 5));
				v1.addLocal(0, u / annotationScale, 0);
				v2.addLocal(0, u / annotationScale, 0);
				outlineBuffer.put(v1.getXf()).put(v1.getYf()).put(v1.getZf());
				outlineBuffer.put(v2.getXf()).put(v2.getYf()).put(v2.getZf());
			}
		}

		// draw steel frame lines
		final int steelBufferSize = (nUnits + 2) * 6;
		if (steelFrameBuffer.capacity() < steelBufferSize) {
			steelFrameBuffer = BufferUtils.createFloatBuffer(steelBufferSize);
			steelFrame.getMeshData().setVertexBuffer(steelFrameBuffer);
		} else {
			steelFrameBuffer.clear();
			steelFrameBuffer.rewind();
			steelFrameBuffer.limit(steelBufferSize);
		}
		steelFrameBuffer.put(p1.getXf()).put(p1.getYf()).put(p1.getZf());
		steelFrameBuffer.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());

		unitsRoot.detachAllChildren();
		for (double u = halfLength; u < troughLength; u += unitLength) {
			final Vector3 p = pd.multiply((u - halfLength) / annotationScale, null);
			steelFrameBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
			steelFrameBuffer.put(p.getXf()).put(p.getYf()).put((float) (p.getZ() + 0.5 * reflector.getSemilatusRectum()));
			addPole(p.addLocal(center), baseHeight, baseZ);
		}
		for (double u = halfLength - unitLength; u > 0; u -= unitLength) {
			final Vector3 p = pd.multiply((u - halfLength) / annotationScale, null);
			steelFrameBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
			steelFrameBuffer.put(p.getXf()).put(p.getYf()).put((float) (p.getZ() + 0.5 * reflector.getSemilatusRectum()));
			addPole(p.addLocal(center), baseHeight, baseZ);
		}
		unitsRoot.getSceneHints().setCullHint(CullHint.Inherit);

		final ReadOnlyVector3 n = new Vector3(normal.getX(), 0, normal.getZ()).normalizeLocal();
		final Matrix3 rotation = new Matrix3().lookAt(n, Vector3.UNIT_Y);
		mesh.setRotation(rotation);
		mesh.setTranslation(getAbsPoint(0));
		reflectorBack.setRotation(rotation);
		reflectorBack.setTranslation(mesh.getTranslation());
		outlines.setRotation(rotation);
		outlines.setTranslation(mesh.getTranslation());
		absorber.setRotation(new Matrix3().applyRotationX(Math.PI / 2));
		absorber.setTranslation(mesh.getTranslation().add(n.multiply(0.5 * reflector.getSemilatusRectum(), null), null));
		absorberCore.setRotation(absorber.getRotation());
		absorberCore.setTranslation(absorber.getTranslation());
		final Vector3 endShift = n.multiply(0.5 * absorberEnd1.getHeight(), null);
		absorberEnd1.setTranslation(mesh.getTranslation().add(p1.add(endShift, null), null));
		absorberEnd2.setTranslation(mesh.getTranslation().add(p2.add(endShift, null), null));
		absorberEnd1.setRotation(rotation);
		absorberEnd2.setRotation(rotation);
		absorberEnd1Core.setTranslation(absorberEnd1.getTranslation());
		absorberEnd2Core.setTranslation(absorberEnd2.getTranslation());
		absorberEnd1Core.setRotation(rotation);
		absorberEnd2Core.setRotation(rotation);
		steelFrame.setRotation(rotation);
		steelFrame.setTranslation(mesh.getTranslation());

		mesh.updateModelBound();
		outlines.updateModelBound();
		steelFrame.updateModelBound();
		absorber.updateModelBound();
		absorberEnd1.updateModelBound();
		absorberEnd2.updateModelBound();

		if (bloomRenderPassTube == null) {
			bloomRenderPassTube = new BloomRenderPass(SceneManager.getInstance().getCamera(), 10);
			bloomRenderPassTube.setBlurIntensityMultiplier(0.75f);
			// bloomRenderPassTube.setNrBlurPasses(2);
			SceneManager.getInstance().getPassManager().add(bloomRenderPassTube);
		}
		if (!bloomRenderPassTube.contains(absorber)) {
			bloomRenderPassTube.add(absorber);
		}

		if (beamsVisible) {
			drawLightBeams();
		}

		updateLabel();

		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
		root.updateGeometricState(0);

	}

	public void drawLightBeams() {
		if (Heliodon.getInstance().isNightTime() || !beamsVisible) {
			lightBeams.setVisible(false);
			return;
		}
		final int nBeams = 10;
		FloatBuffer beamsBuffer = lightBeams.getMeshData().getVertexBuffer();
		final int beamsBufferSize = (nBeams + 1) * 12;
		if (beamsBuffer.capacity() < beamsBufferSize) {
			beamsBuffer = BufferUtils.createFloatBuffer(beamsBufferSize);
			lightBeams.getMeshData().setVertexBuffer(beamsBuffer);
		} else {
			beamsBuffer.rewind();
		}
		final Vector3 sunLocation = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).normalize(null);
		double dx, dy, dz;
		final double ny = sunLocation.getY();
		sunLocation.multiplyLocal(10000);
		final double focus = 0.5 * reflector.getSemilatusRectum();
		for (int i = 0; i <= nBeams; i++) {
			dx = reflector.getWidth() * (0.5 - (double) i / nBeams);
			dz = 0.5 * dx * dx / reflector.getSemilatusRectum();
			Vector3 d = mesh.getRotation().applyPost(new Vector3(dx, 0, dz), null);
			final Vector3 o = getAbsPoint(0).addLocal(d);
			// draw line to sun
			final Vector3 r = o.clone();
			r.addLocal(sunLocation);
			beamsBuffer.put(o.getXf()).put(o.getYf()).put(o.getZf());
			beamsBuffer.put(r.getXf()).put(r.getYf()).put(r.getZf());
			// draw line to focus
			dy = ny * (dz - focus);
			d = mesh.getRotation().applyPost(new Vector3(0, 0, focus), null);
			final Vector3 f = getAbsPoint(0).addLocal(d).addLocal(0, dy, 0);
			beamsBuffer.put(o.getXf()).put(o.getYf()).put(o.getZf());
			beamsBuffer.put(f.getXf()).put(f.getYf()).put(f.getZf());
		}
		lightBeams.updateModelBound();
		lightBeams.setVisible(true);
		if (bloomRenderPassLight == null) {
			bloomRenderPassLight = new BloomRenderPass(SceneManager.getInstance().getCamera(), 10);
			bloomRenderPassLight.setBlurIntensityMultiplier(0.5f);
			bloomRenderPassLight.setNrBlurPasses(2);
			SceneManager.getInstance().getPassManager().add(bloomRenderPassLight);
		}
		if (!bloomRenderPassLight.contains(lightBeams)) {
			bloomRenderPassLight.add(lightBeams);
		}
	}

	public void updateLabel() {
		String text = "";
		if (labelCustom && labelCustomText != null) {
			text += labelCustomText;
		}
		if (labelId) {
			text += (text.equals("") ? "" : "\n") + "#" + id;
		}
		if (labelEnergyOutput) {
			text += (text.equals("") ? "" : "\n") + (Util.isZero(solarPotentialToday) ? "Output" : EnergyPanel.TWO_DECIMALS.format(solarPotentialToday) + " kWh");
		}
		if (!text.equals("")) {
			label.setText(text);
			final double shift = 0.6 * reflector.getSemilatusRectum();
			label.setTranslation((getAbsCenter()).addLocal(normal.multiply(shift, null)));
			label.setVisible(true);
		} else {
			label.setVisible(false);
		}
	}

	private void addPole(final Vector3 position, final double poleHeight, final double baseZ) {
		final Cylinder pole = new Cylinder("Pole Cylinder", 10, 10, 10, 0);
		pole.setRadius(0.6);
		pole.setRenderState(offsetState);
		pole.setHeight(poleHeight - 0.5 * pole.getRadius()); // slightly shorter so that the pole won't penetrate the surface of the trough
		pole.setModelBound(new BoundingBox());
		pole.updateModelBound();
		position.setZ(baseZ + pole.getHeight() / 2);
		pole.setTranslation(position);
		unitsRoot.attachChild(pole);
	}

	@Override
	public boolean isDrawable() {
		if (container == null) {
			return true;
		}
		if (mesh.getWorldBound() == null) {
			return true;
		}
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart == null || selectedPart.isDrawCompleted()) { // if nothing is really selected, skip overlap check
			return true;
		}
		final OrientedBoundingBox bound = (OrientedBoundingBox) mesh.getWorldBound().clone(null);
		bound.setExtent(bound.getExtent().divide(1.1, null).addLocal(0, 0, 1));
		for (final HousePart child : container.getChildren()) {
			if (child != this && child instanceof ParabolicTrough && bound.intersects(child.mesh.getWorldBound())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void updateTextureAndColor() {
		updateTextureAndColor(mesh, ColorRGBA.LIGHT_GRAY, TextureMode.Full);
	}

	@Override
	protected String getTextureFileName() {
		return "trough_mirror.png";
	}

	@Override
	public ReadOnlyVector3 getNormal() {
		return normal;
	}

	@Override
	public boolean isPrintable() {
		return false;
	}

	@Override
	public double getGridSize() {
		return Math.min(troughLength, troughWidth) / Scene.getInstance().getAnnotationScale() / (SceneManager.getInstance().isFineGrid() ? 100.0 : 20.0);
	}

	@Override
	protected void computeArea() {
		area = troughLength * troughWidth;
	}

	@Override
	protected HousePart getContainerRelative() {
		return getTopContainer();
	}

	@Override
	public void drawHeatFlux() {
		// this method is left empty on purpose -- don't draw heat flux
	}

	public void moveTo(final HousePart target) {
		setContainer(target);
	}

	@Override
	public boolean isCopyable() {
		return true;
	}

	private double copyOverlapInDirectionOfHeight() { // copy only in the direction of trough height
		final double w1 = troughWidth / Scene.getInstance().getAnnotationScale();
		final Vector3 center = getAbsCenter();
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p.container == container && p != this) {
				if (p instanceof ParabolicTrough) {
					final ParabolicTrough s2 = (ParabolicTrough) p;
					final double w2 = s2.troughWidth / Scene.getInstance().getAnnotationScale();
					final double distance = p.getAbsCenter().distance(center);
					if (distance < (w1 + w2) * 0.499) {
						return distance;
					}
				}
			}
		}
		return -1;
	}

	@Override
	public HousePart copy(final boolean check) {
		final ParabolicTrough c = (ParabolicTrough) super.copy(false);
		if (check) {
			normal = container.getNormal();
			if (container instanceof Foundation) {
				if (!isPositionLegal(c, (Foundation) container)) {
					return null;
				}
			}
		}
		return c;
	}

	private boolean isPositionLegal(final ParabolicTrough trough, final Foundation foundation) {
		final Vector3 p0 = foundation.getAbsPoint(0);
		final Vector3 p1 = foundation.getAbsPoint(1);
		final Vector3 p2 = foundation.getAbsPoint(2);
		final double a = -Math.toRadians(relativeAzimuth) * Math.signum(p2.subtract(p0, null).getX() * p1.subtract(p0, null).getY());
		final Vector3 v = new Vector3(Math.cos(Math.PI / 2 + a), Math.sin(Math.PI / 2 + a), 0);
		final double length = (1 + copyLayoutGap) * troughWidth / Scene.getInstance().getAnnotationScale();
		final double s = Math.signum(foundation.getAbsCenter().subtractLocal(Scene.getInstance().getOriginalCopy().getAbsCenter()).dot(v));
		final double tx = length / p0.distance(p2);
		final double ty = length / p0.distance(p1);
		final double lx = s * v.getX() * tx;
		final double ly = s * v.getY() * ty;
		final double newX = points.get(0).getX() + lx;
		if (newX > 1 - tx || newX < tx) {
			return false;
		}
		final double newY = points.get(0).getY() + ly;
		if (newY > 1 - ty || newY < ty) {
			return false;
		}
		trough.points.get(0).setX(newX);
		trough.points.get(0).setY(newY);
		final double o = trough.copyOverlapInDirectionOfHeight(); // TODO
		if (o >= 0) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, your new trough is too close to an existing one (" + o + ").", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	public double getSystemEfficiency() {
		double e = reflectivity;
		final Atmosphere atm = Scene.getInstance().getAtmosphere();
		if (atm != null) {
			e *= 1 - atm.getDustLoss(Heliodon.getInstance().getCalendar().get(Calendar.MONTH));
		}
		return e;
	}

	/** a number between 0 and 1 */
	public void setReflectivity(final double efficiency) {
		this.reflectivity = efficiency;
	}

	/** a number between 0 and 1 */
	public double getReflectivity() {
		return reflectivity;
	}

	public void setTroughLength(final double troughLength) {
		this.troughLength = troughLength;
	}

	public double getTroughLength() {
		return troughLength;
	}

	public void setTroughWidth(final double troughWidth) {
		this.troughWidth = troughWidth;
	}

	public double getTroughWidth() {
		return troughWidth;
	}

	public void setSemilatusRectum(final double semilatusRectum) {
		this.semilatusRectum = semilatusRectum;
	}

	public double getSemilatusRectum() {
		return semilatusRectum;
	}

	public void setBaseHeight(final double baseHeight) {
		this.baseHeight = baseHeight;
	}

	public double getBaseHeight() {
		return baseHeight;
	}

	public void setRelativeAzimuth(double relativeAzimuth) {
		if (relativeAzimuth < 0) {
			relativeAzimuth += 360;
		} else if (relativeAzimuth > 360) {
			relativeAzimuth -= 360;
		}
		this.relativeAzimuth = relativeAzimuth;
		if (outOfBound()) { // undo the rotation
			this.relativeAzimuth = oldRelativeAzimuth;
		} else {
			oldRelativeAzimuth = this.relativeAzimuth;
		}
	}

	public double getRelativeAzimuth() {
		return relativeAzimuth;
	}

	public void move(final Vector3 v, final double steplength) {
		v.normalizeLocal().multiplyLocal(steplength);
		final Vector3 v_rel = toRelativeVector(v);
		points.get(0).addLocal(v_rel);
		draw();
		if (outOfBound()) {
			if (oldTroughCenter != null) {
				points.get(0).set(oldTroughCenter);
			}
		} else {
			oldTroughCenter = points.get(0).clone();
		}
	}

	public void set(final Vector3 center, final double width, final double height) {
		points.get(0).set(toRelative(center));
		setTroughLength(width);
		setTroughWidth(height);
		draw();
	}

	public double getUnitLength() {
		return unitLength;
	}

	public void setUnitLength(final double unitLength) {
		this.unitLength = unitLength;
	}

	@Override
	public void updateEditShapes() {
		final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
		final ReadOnlyTransform trans = mesh.getWorldTransform();
		final ReadOnlyVector3 n = normal == null ? Vector3.UNIT_Z : new Vector3(normal.getX(), 0, normal.getZ()).normalizeLocal();
		final double halfWidth = 0.5 * troughWidth / Scene.getInstance().getAnnotationScale();
		final double dy = halfWidth * halfWidth / (2 * (semilatusRectum / Scene.getInstance().getAnnotationScale()));
		final Vector3 shift = new Vector3(n.getX() * dy, 0, n.getZ() * dy);
		final int j = buf.limit() / 6;
		final Vector3 v1 = new Vector3();
		final Vector3 v2 = new Vector3();
		BufferUtils.populateFromBuffer(v1, buf, 0);
		BufferUtils.populateFromBuffer(v2, buf, j);
		final Vector3 p1 = trans.applyForward(v1).add(trans.applyForward(v2), null).multiplyLocal(0.5); // along the direction of length
		BufferUtils.populateFromBuffer(v1, buf, 0);
		BufferUtils.populateFromBuffer(v2, buf, j - 1);
		final Vector3 p2 = trans.applyForward(v1).add(trans.applyForward(v2), null).multiplyLocal(0.5).subtractLocal(shift); // along the direction of width
		BufferUtils.populateFromBuffer(v1, buf, j - 1);
		BufferUtils.populateFromBuffer(v2, buf, 2 * j - 1);
		final Vector3 p3 = trans.applyForward(v1).add(trans.applyForward(v2), null).multiplyLocal(0.5); // along the direction of length
		BufferUtils.populateFromBuffer(v1, buf, j);
		BufferUtils.populateFromBuffer(v2, buf, 2 * j - 1);
		final Vector3 p4 = trans.applyForward(v1).add(trans.applyForward(v2), null).multiplyLocal(0.5).subtractLocal(shift); // along the direction of width
		int i = 1;
		getEditPointShape(i++).setTranslation(p1);
		getEditPointShape(i++).setTranslation(p2);
		getEditPointShape(i++).setTranslation(p3);
		getEditPointShape(i++).setTranslation(p4);
		final ReadOnlyColorRGBA c = Scene.getInstance().isGroundImageLightColored() ? ColorRGBA.DARK_GRAY : ColorRGBA.WHITE;
		for (i = 1; i < 5; i++) {
			getEditPointShape(i).setDefaultColor(c);
		}
		super.updateEditShapes();
		getEditPointShape(0).setTranslation(p1.addLocal(p3).multiplyLocal(0.5).addLocal(0, 0, 0.15));
	}

	private Vector3 getVertex(final int i) {
		final Vector3 v = new Vector3();
		BufferUtils.populateFromBuffer(v, mesh.getMeshData().getVertexBuffer(), i);
		return mesh.getWorldTransform().applyForward(v);
	}

	@Override
	public void delete() {
		super.delete();
		if (bloomRenderPassLight != null) {
			if (bloomRenderPassLight.contains(lightBeams)) {
				bloomRenderPassLight.remove(lightBeams);
			}
		}
		if (bloomRenderPassTube != null) {
			if (bloomRenderPassTube.contains(absorber)) {
				bloomRenderPassTube.remove(absorber);
			}
		}
	}

	public void setBeamsVisible(final boolean beamsVisible) {
		this.beamsVisible = beamsVisible;
	}

	public boolean areBeamsVisible() {
		return beamsVisible;
	}

	public double getYieldNow() {
		return yieldNow;
	}

	public void setYieldNow(final double yieldNow) {
		this.yieldNow = yieldNow;
	}

	public double getYieldToday() {
		return yieldToday;
	}

	public void setYieldToday(final double yieldToday) {
		this.yieldToday = yieldToday;
	}

	@Override
	public void clearLabels() {
		super.clearLabels();
		labelEnergyOutput = false;
	}

	public boolean isLabelVisible() {
		return label.isVisible();
	}

	public void setLabelEnergyOutput(final boolean labelEnergyOutput) {
		this.labelEnergyOutput = labelEnergyOutput;
	}

	public boolean getLabelEnergyOutput() {
		return labelEnergyOutput;
	}

	public void setNSectionParabola(final int parabolaSectionCount) {
		nSectionParabola = parabolaSectionCount;
	}

	public int getNSectionParabola() {
		return nSectionParabola;
	}

	public void setNSectionAxis(final int axisSectionCount) {
		nSectionAxis = axisSectionCount;
	}

	public int getNSectionAxis() {
		return nSectionAxis;
	}

}
