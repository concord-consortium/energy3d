package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.Calendar;
import java.util.List;

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
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Cylinder;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.ui.text.BMText.Justify;
import com.ardor3d.util.geom.BufferUtils;

/**
 * @author Charles Xie
 *
 */

public class ParabolicTrough extends HousePart implements SolarReflector, Labelable {

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
	private transient Node modulesRoot;
	private transient Line lightBeams;
	private transient BMText label;
	private transient double copyLayoutGap = 0.2;
	private transient double yieldNow; // solar output at current hour
	private transient double yieldToday;
	private double reflectance = 0.9; // a number in (0, 1), iron glass has a reflectance of 0.9 (but dirt and dust reduce it to 0.82, this is accounted for by Atmosphere)
	private double absorptance = 0.95; // the percentage of energy absorbed by the tube in the line of focus
	private double opticalEfficiency = 0.7;
	private double thermalEfficiency = 0.3;
	private double moduleLength = 3;
	private double troughLength = 2 * moduleLength;
	private double apertureWidth = 2;
	private double semilatusRectum = 2;
	private double relativeAzimuth = 0;
	private double baseHeight = 15;
	private boolean beamsVisible;
	private boolean labelEnergyOutput;
	private transient Vector3 oldTroughCenter;
	private transient double oldTroughLength, oldApertureWidth;
	private transient double oldRelativeAzimuth;
	private static transient BloomRenderPass bloomRenderPassLight, bloomRenderPassTube;
	private transient double baseZ;
	private int nSectionParabola = 16; // number of sections for the parabola cross section of a parabolic trough (must be power of 2)
	private int nSectionAxis = 32; // number of sections in the axis of a parabolic trough (must be power of 2)
	private transient boolean detailed; // allows us to draw more details when there are fewer troughs in the scene

	public ParabolicTrough() {
		super(1, 1, 0);
	}

	@Override
	protected void init() {
		super.init();

		if (Util.isZero(copyLayoutGap)) { // FIXME: Why is a transient member evaluated to zero?
			copyLayoutGap = 0.2;
		}
		if (Util.isZero(moduleLength)) {
			moduleLength = 3;
		}
		if (Util.isZero(troughLength)) {
			troughLength = 2 * moduleLength;
		}
		if (Util.isZero(apertureWidth)) {
			apertureWidth = 2;
		}
		if (Util.isZero(semilatusRectum)) {
			semilatusRectum = 2;
		}
		if (Util.isZero(reflectance)) {
			reflectance = 0.9;
		}
		if (Util.isZero(absorptance)) {
			absorptance = 0.95;
		}
		if (Util.isZero(opticalEfficiency)) {
			opticalEfficiency = 0.7;
		}
		if (Util.isZero(thermalEfficiency)) {
			thermalEfficiency = 0.3;
		}
		if (Util.isZero(nSectionParabola)) {
			nSectionParabola = 16;
		}
		if (Util.isZero(nSectionAxis)) {
			nSectionAxis = 32;
		}
		detailed = Scene.getInstance().countParts(this.getClass()) < 50;

		mesh = new ParabolicCylinder("Parabolic Cylinder", nSectionParabola, semilatusRectum, apertureWidth, troughLength);
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
		absorber = new Cylinder("Absorber Tube", 2, detailed ? 10 : 4, 0.5, 0, true);
		final BlendState blend = new BlendState();
		blend.setBlendEnabled(true);
		absorber.setRenderState(blend);
		absorber.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
		absorber.setDefaultColor(tubeColor);
		absorber.setModelBound(new OrientedBoundingBox());
		root.attachChild(absorber);

		absorberEnd1 = new Cylinder("Absorber End Tube 1", 2, detailed ? 10 : 4, 0.5, 0, true);
		absorberEnd1.setRenderState(blend);
		absorberEnd1.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
		absorberEnd1.setDefaultColor(tubeColor);
		absorberEnd1.setModelBound(new OrientedBoundingBox());
		root.attachChild(absorberEnd1);

		absorberEnd2 = new Cylinder("Absorber End Tube 2", 2, detailed ? 10 : 4, 0.5, 0, true);
		absorberEnd2.setRenderState(blend);
		absorberEnd2.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
		absorberEnd2.setDefaultColor(tubeColor);
		absorberEnd2.setModelBound(new OrientedBoundingBox());
		root.attachChild(absorberEnd2);

		if (detailed) {
			absorberCore = new Cylinder("Absorber Tube Core", 2, 4, 0.4, 0, true);
			absorberCore.setDefaultColor(ColorRGBA.BROWN);
			absorberCore.setModelBound(new OrientedBoundingBox());
			root.attachChild(absorberCore);
			absorberEnd1Core = new Cylinder("Absorber End Tube 1 Core", 2, 4, 0.4, 0, true);
			absorberEnd1Core.setDefaultColor(ColorRGBA.BROWN);
			absorberEnd1Core.setModelBound(new OrientedBoundingBox());
			root.attachChild(absorberEnd1Core);
			absorberEnd2Core = new Cylinder("Absorber End Tube 2 Core", 2, 4, 0.4, 0, true);
			absorberEnd2Core.setDefaultColor(ColorRGBA.BROWN);
			absorberEnd2Core.setModelBound(new OrientedBoundingBox());
			root.attachChild(absorberEnd2Core);
		}

		final int nModules = getNumberOfModules();
		outlines = new Line("Parabolic Trough (Outline)");
		outlines.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4 + 2 * (reflector.getNumberOfSamples() + 1) * (nModules + 1)));
		outlines.setDefaultColor(ColorRGBA.BLACK);
		outlines.setModelBound(new OrientedBoundingBox());
		outlines.setLineWidth(1f);
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
		lightBeams.setLineWidth(1f);
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

		modulesRoot = new Node("Modules Root");
		root.attachChild(modulesRoot);
		updateTextureAndColor();

		if (!points.isEmpty()) {
			oldTroughCenter = points.get(0).clone();
		}
		oldTroughLength = troughLength;
		oldApertureWidth = apertureWidth;

	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		if (lockEdit) {
			return;
		}
		if (editPointIndex <= 0) {
			final PickedHousePart picked = pickContainer(x, y, new Class<?>[] { Foundation.class });
			if (picked != null && picked.getUserData() != null) { // when the user data is null, it picks the land
				final Vector3 p = picked.getPoint().clone();
				final UserData ud = picked.getUserData();
				snapToGrid(p, getAbsPoint(0), getGridSize(), false);
				points.get(0).set(toRelative(p));
				pickedNormal = ud.getRotatedNormal() == null ? ud.getNormal() : ud.getRotatedNormal();
			} else {
				pickedNormal = null;
			}
			if (outOfBound()) {
				if (oldTroughCenter != null && !oldTroughCenter.equals(new Vector3())) { // TODO: Need to find a better way to do this
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
					setApertureWidth(rw);
					if (outOfBound()) {
						if (oldTroughCenter != null) {
							points.get(0).set(oldTroughCenter);
						}
						setApertureWidth(oldApertureWidth);
					} else {
						oldTroughCenter = points.get(0).clone();
						oldApertureWidth = apertureWidth;
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
		final double az = Math.toRadians(relativeAzimuth);

		final double annotationScale = Scene.getInstance().getAnnotationScale();
		reflector.setSize(apertureWidth / annotationScale, troughLength / annotationScale);
		reflector.setSemilatusRectum(semilatusRectum / annotationScale);
		reflector.updateModelBound();
		baseZ = container instanceof Foundation ? container.getHeight() : container.getPoints().get(0).getZ();
		points.get(0).setZ(baseZ + baseHeight);
		absorber.setHeight(reflector.getHeight());
		absorberEnd1.setHeight(0.5 * reflector.getSemilatusRectum());
		absorberEnd2.setHeight(absorberEnd1.getHeight());
		if (detailed) {
			absorberCore.setHeight(reflector.getHeight() - 1);
			absorberEnd1Core.setHeight(absorberEnd1.getHeight() - 1);
			absorberEnd2Core.setHeight(absorberEnd2.getHeight() - 1);
		}

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

		final int nModules = Math.max(1, getNumberOfModules());
		final int outlineBufferSize = 6 * (vertexCount - 1) * (nModules + 2) + 12; // 12 is for the two lateral lines
		if (outlineBuffer.capacity() < outlineBufferSize) {
			outlineBuffer = BufferUtils.createFloatBuffer(outlineBufferSize);
			outlines.getMeshData().setVertexBuffer(outlineBuffer);
		} else {
			outlineBuffer.rewind();
			outlineBuffer.limit(outlineBufferSize);
		}
		// draw parabolic lines of the two end faces
		int i3;
		for (int i = 0; i < vertexCount - 1; i++) {
			i3 = i * 3;
			outlineBuffer.put(vertexBuffer.get(i3)).put(vertexBuffer.get(i3 + 1)).put(vertexBuffer.get(i3 + 2));
			outlineBuffer.put(vertexBuffer.get(i3 + 3)).put(vertexBuffer.get(i3 + 4)).put(vertexBuffer.get(i3 + 5));
			outlineBuffer.put(vertexBuffer.get(j + i3)).put(vertexBuffer.get(j + i3 + 1)).put(vertexBuffer.get(j + i3 + 2));
			outlineBuffer.put(vertexBuffer.get(j + i3 + 3)).put(vertexBuffer.get(j + i3 + 4)).put(vertexBuffer.get(j + i3 + 5));
		}
		// draw lateral lines connecting the two end faces
		outlineBuffer.put(vertexBuffer.get(0)).put(vertexBuffer.get(1)).put(vertexBuffer.get(2));
		outlineBuffer.put(vertexBuffer.get(j)).put(vertexBuffer.get(j + 1)).put(vertexBuffer.get(j + 2));
		outlineBuffer.put(vertexBuffer.get(j - 3)).put(vertexBuffer.get(j - 2)).put(vertexBuffer.get(j - 1));
		outlineBuffer.put(vertexBuffer.get(2 * j - 3)).put(vertexBuffer.get(2 * j - 2)).put(vertexBuffer.get(2 * j - 1));
		// draw seam lines between units
		if (nModules > 1) { // if there is only one module, don't draw
			for (int k = 1; k < nModules; k++) {
				final double ua = k * moduleLength / annotationScale;
				for (int i = 0; i < vertexCount - 1; i++) {
					i3 = i * 3;
					final Vector3 v1 = new Vector3(vertexBuffer.get(i3), vertexBuffer.get(i3 + 1), vertexBuffer.get(i3 + 2));
					final Vector3 v2 = new Vector3(vertexBuffer.get(i3 + 3), vertexBuffer.get(i3 + 4), vertexBuffer.get(i3 + 5));
					v1.addLocal(0, ua, 0);
					v2.addLocal(0, ua, 0);
					outlineBuffer.put(v1.getXf()).put(v1.getYf()).put(v1.getZf());
					outlineBuffer.put(v2.getXf()).put(v2.getYf()).put(v2.getZf());
				}
			}
		}
		outlineBuffer.limit(vertexCount * 12 + (vertexCount - 1) * 6 * (nModules - 1));

		// draw steel frame lines
		final int steelBufferSize = nModules * 6;
		if (steelFrameBuffer.capacity() < steelBufferSize) {
			steelFrameBuffer = BufferUtils.createFloatBuffer(steelBufferSize);
			steelFrame.getMeshData().setVertexBuffer(steelFrameBuffer);
		} else {
			steelFrameBuffer.rewind();
			steelFrameBuffer.limit(steelBufferSize);
		}

		modulesRoot.detachAllChildren();
		if (nModules > 1) {
			final Vector3 qd = new Matrix3().applyRotationZ(-az).applyPost(pd, null);
			for (double u = moduleLength; u < troughLength; u += moduleLength) {
				final double step = (u - halfLength) / annotationScale;
				final Vector3 p = pd.multiply(step, null);
				steelFrameBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
				steelFrameBuffer.put(p.getXf()).put(p.getYf()).put((float) (p.getZ() + 0.5 * reflector.getSemilatusRectum()));
				final Vector3 q = qd.multiply(step, null);
				addPole(q.addLocal(center), baseHeight, baseZ);
			}
			steelFrameBuffer.limit((nModules - 1) * 6);
			steelFrame.getSceneHints().setCullHint(CullHint.Inherit);
		} else {
			addPole(center, baseHeight, baseZ);
			steelFrame.getSceneHints().setCullHint(CullHint.Always); // if there is only one module, don't draw frames
		}
		modulesRoot.getSceneHints().setCullHint(CullHint.Inherit);

		final Vector3 sunDirection = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).normalizeLocal();
		final Vector3 rotationAxis = new Vector3(Math.sin(az), Math.cos(az), 0);
		final double axisSunDot = sunDirection.dot(rotationAxis);
		rotationAxis.multiplyLocal(Util.isZero(axisSunDot) ? 0.001 : axisSunDot); // avoid singularity when the direction of the sun is perpendicular to the axis of the trough
		normal = sunDirection.subtractLocal(rotationAxis).normalizeLocal();
		if (Util.isEqual(normal, Vector3.UNIT_Z)) {
			normal = new Vector3(-0.001, 0, 1).normalizeLocal();
		}
		final Matrix3 rotation = new Matrix3().lookAt(normal, rotationAxis);
		mesh.setRotation(rotation);
		mesh.setTranslation(center);
		reflectorBack.setRotation(rotation);
		reflectorBack.setTranslation(mesh.getTranslation());
		outlines.setRotation(rotation);
		outlines.setTranslation(mesh.getTranslation());

		final Vector3 axis = rotationAxis.cross(Vector3.UNIT_Z, null);
		absorber.setRotation(new Matrix3().fromAngleAxis(Math.acos(rotationAxis.dot(Vector3.UNIT_Z)), axis));
		absorber.setTranslation(mesh.getTranslation().add(normal.multiply(0.5 * reflector.getSemilatusRectum(), null), null));
		final Vector3 endShift = normal.multiply(0.5 * absorberEnd1.getHeight(), null);
		absorberEnd1.setTranslation(mesh.getTranslation().add(rotation.applyPost(p1, null).add(endShift, null), null));
		absorberEnd2.setTranslation(mesh.getTranslation().add(rotation.applyPost(p2, null).add(endShift, null), null));
		absorberEnd1.setRotation(rotation);
		absorberEnd2.setRotation(rotation);
		steelFrame.setRotation(rotation);
		steelFrame.setTranslation(mesh.getTranslation());
		if (detailed) {
			absorberCore.setRotation(absorber.getRotation());
			absorberCore.setTranslation(absorber.getTranslation());
			absorberEnd1Core.setTranslation(absorberEnd1.getTranslation());
			absorberEnd2Core.setTranslation(absorberEnd2.getTranslation());
			absorberEnd1Core.setRotation(rotation);
			absorberEnd2Core.setRotation(rotation);
		}

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
			drawSunBeam();
		}

		updateLabel();

		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
		root.updateGeometricState(0);

	}

	@Override
	public void drawSunBeam() {
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
		final Vector3 sunLocation = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).normalizeLocal();
		double dx, dy, dz;
		final double ny = sunLocation.getY();
		sunLocation.multiplyLocal(10000);
		final double focus = 0.5 * reflector.getSemilatusRectum();
		final Vector3 focusPoint = mesh.getRotation().applyPost(new Vector3(0, 0, focus), null);
		for (int i = 0; i <= nBeams; i++) {
			dx = reflector.getWidth() * (0.5 - (double) i / nBeams);
			dz = 0.5 * dx * dx / reflector.getSemilatusRectum();
			final Vector3 d = mesh.getRotation().applyPost(new Vector3(dx, 0, dz), null);
			final Vector3 o = getAbsPoint(0).addLocal(d);
			// draw line to sun
			final Vector3 r = o.clone();
			r.addLocal(sunLocation);
			beamsBuffer.put(o.getXf()).put(o.getYf()).put(o.getZf());
			beamsBuffer.put(r.getXf()).put(r.getYf()).put(r.getZf());
			// draw line to focus
			dy = ny * (dz - focus);
			final Vector3 f = getAbsPoint(0).addLocal(focusPoint).addLocal(0, dy, 0);
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

	@Override
	public void updateLabel() {
		String text = "";
		if (labelCustom && labelCustomText != null) {
			text += labelCustomText;
		}
		if (labelId) {
			text += (text.equals("") ? "" : "\n") + "#" + id;
		}
		if (labelEnergyOutput) {
			text += (text.equals("") ? "" : "\n") + (Util.isZero(solarPotentialToday) ? "Output" : EnergyPanel.ONE_DECIMAL.format(solarPotentialToday * getSystemEfficiency()) + " kWh");
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
		final Cylinder pole = new Cylinder("Pole Cylinder", 2, detailed ? 10 : 2, 10, 0);
		pole.setRadius(0.6);
		pole.setRenderState(offsetState);
		pole.setHeight(poleHeight - 0.5 * pole.getRadius()); // slightly shorter so that the pole won't penetrate the surface of the trough
		pole.setModelBound(new BoundingBox());
		pole.updateModelBound();
		position.setZ(baseZ + pole.getHeight() / 2);
		pole.setTranslation(position);
		modulesRoot.attachChild(pole);
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
		return Math.min(troughLength, apertureWidth) / (Scene.getInstance().getAnnotationScale() * (SceneManager.getInstance().isFineGrid() ? 100.0 : 20.0));
	}

	@Override
	protected void computeArea() {
		final double focalLength = semilatusRectum * 0.5;
		final double h = apertureWidth * apertureWidth / (16 * focalLength);
		final double b = 4 * h / apertureWidth;
		final double c = Math.sqrt(b * b + 1);
		final double s = 0.5 * apertureWidth * c + 2 * focalLength * Math.log(b + c);
		area = troughLength * s;
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

	private double checkCopyOverlap(final boolean inWidth) {
		final double w1 = (inWidth ? apertureWidth : troughLength) / Scene.getInstance().getAnnotationScale();
		final Vector3 center = getAbsCenter();
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p.container == container && p != this) {
				if (p instanceof ParabolicTrough) {
					final ParabolicTrough s2 = (ParabolicTrough) p;
					final double w2 = (inWidth ? s2.apertureWidth : s2.troughLength) / Scene.getInstance().getAnnotationScale();
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

	private boolean isPositionLegal(final ParabolicTrough copy, final Foundation foundation) {
		final Vector3 p0 = foundation.getAbsPoint(0);
		final Vector3 p1 = foundation.getAbsPoint(1);
		final Vector3 p2 = foundation.getAbsPoint(2);
		final double a = -Math.toRadians(relativeAzimuth) * Math.signum(p2.subtract(p0, null).getX() * p1.subtract(p0, null).getY());
		final Vector3 v = new Vector3(Math.cos(Math.PI / 2 + a), Math.sin(Math.PI / 2 + a), 0);
		double len;
		double s;
		boolean inWidth = true;
		final ParabolicTrough nearest = foundation.getNearestParabolicTrough(this);
		if (nearest != null) { // use the nearest reflector as the reference to infer next position
			final Vector3 d = getAbsCenter().subtractLocal(nearest.getAbsCenter());
			len = d.length();
			if (apertureWidth > len * Scene.getInstance().getAnnotationScale()) {
				inWidth = false;
			}
			if (len > Math.min(apertureWidth, troughLength) * 5 / Scene.getInstance().getAnnotationScale()) {
				len = (1 + copyLayoutGap) * apertureWidth / Scene.getInstance().getAnnotationScale();
				s = Math.signum(foundation.getAbsCenter().subtractLocal(Scene.getInstance().getOriginalCopy().getAbsCenter()).dot(v));
			} else {
				final double vx = v.getX();
				final double vy = v.getY();
				if (Math.abs(d.getX()) > Math.abs(d.getY())) {
					if (Math.abs(vx) < Math.abs(vy)) {
						v.setX(vy);
						v.setY(vx);
					}
				} else {
					if (Math.abs(vx) > Math.abs(vy)) {
						v.setX(vy);
						v.setY(vx);
					}
				}
				s = Math.signum(d.dot(v));
			}
		} else {
			len = (1 + copyLayoutGap) * apertureWidth / Scene.getInstance().getAnnotationScale();
			s = Math.signum(foundation.getAbsCenter().subtractLocal(Scene.getInstance().getOriginalCopy().getAbsCenter()).dot(v));
		}
		final double tx = len / p0.distance(p2);
		final double ty = len / p0.distance(p1);
		final double lx = s * v.getX() * tx;
		final double ly = s * v.getY() * ty;
		final double newX = points.get(0).getX() + lx;
		if (newX > 1 - 0.5 * tx || newX < 0.5 * tx) {
			return false;
		}
		final double newY = points.get(0).getY() + ly;
		if (newY > 1 - 0.5 * ty || newY < 0.5 * ty) {
			return false;
		}
		copy.points.get(0).setX(newX);
		copy.points.get(0).setY(newY);
		final double o = copy.checkCopyOverlap(inWidth);
		if (o >= 0) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, your new trough is too close to an existing one (" + o + ").", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	public double getSystemEfficiency() {
		double e = reflectance * absorptance * opticalEfficiency * thermalEfficiency;
		final Atmosphere atm = Scene.getInstance().getAtmosphere();
		if (atm != null) {
			e *= 1 - atm.getDustLoss(Heliodon.getInstance().getCalendar().get(Calendar.MONTH));
		}
		return e;
	}

	/** a number between 0 and 1 */
	@Override
	public void setOpticalEfficiency(final double opticalEfficiency) {
		this.opticalEfficiency = opticalEfficiency;
	}

	/** a number between 0 and 1 */
	@Override
	public double getOpticalEfficiency() {
		return opticalEfficiency;
	}

	/** a number between 0 and 1 */
	@Override
	public void setThermalEfficiency(final double thermalEfficiency) {
		this.thermalEfficiency = thermalEfficiency;
	}

	/** a number between 0 and 1 */
	@Override
	public double getThermalEfficiency() {
		return thermalEfficiency;
	}

	/** a number between 0 and 1 */
	@Override
	public void setReflectance(final double reflectance) {
		this.reflectance = reflectance;
	}

	/** a number between 0 and 1 */
	@Override
	public double getReflectance() {
		return reflectance;
	}

	/** a number between 0 and 1 */
	@Override
	public void setAbsorptance(final double absorptance) {
		this.absorptance = absorptance;
	}

	/** a number between 0 and 1 */
	@Override
	public double getAbsorptance() {
		return absorptance;
	}

	@Override
	public void setBaseHeight(final double baseHeight) {
		this.baseHeight = baseHeight;
	}

	@Override
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

	@Override
	public void move(final Vector3 v, final double steplength) {
		if (lockEdit) {
			return;
		}
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
		setApertureWidth(height);
		draw();
	}

	public double getModuleLength() {
		return moduleLength;
	}

	public void setModuleLength(final double moduleLength) {
		this.moduleLength = moduleLength;
	}

	public void setTroughLength(final double troughLength) {
		this.troughLength = troughLength;
	}

	public double getTroughLength() {
		return troughLength;
	}

	public void setApertureWidth(final double apertureWidth) {
		this.apertureWidth = apertureWidth;
	}

	public double getApertureWidth() {
		return apertureWidth;
	}

	public void setSemilatusRectum(final double semilatusRectum) {
		this.semilatusRectum = semilatusRectum;
	}

	public double getSemilatusRectum() {
		return semilatusRectum;
	}

	public void ensureFullModules(final boolean dragged) {
		boolean ok = false;
		if (dragged) {
			if (editPointIndex > 0) { // the trough has been resized
				ok = true;
			}
		} else {
			ok = true;
		}
		if (ok) {
			int n = getNumberOfModules();
			if (n <= 0) {
				n = 1;
			}
			setTroughLength(n * moduleLength);
			drawMesh();
			updateEditShapes();
		}
	}

	@Override
	public void updateEditShapes() {
		final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
		final ReadOnlyTransform trans = mesh.getWorldTransform();
		final ReadOnlyVector3 n = normal == null ? Vector3.UNIT_Z : new Vector3(normal.getX(), 0, normal.getZ()).normalizeLocal();
		final double halfWidth = 0.5 * apertureWidth / Scene.getInstance().getAnnotationScale();
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
		final ReadOnlyColorRGBA c = Scene.getInstance().isGroundImageLightColored() ? ColorRGBA.DARK_GRAY : (lockEdit ? disabledColor : ColorRGBA.WHITE);
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

	@Override
	public void setSunBeamVisible(final boolean beamsVisible) {
		this.beamsVisible = beamsVisible;
	}

	@Override
	public boolean isSunBeamVisible() {
		return beamsVisible;
	}

	@Override
	public double getYieldNow() {
		return yieldNow;
	}

	@Override
	public void setYieldNow(final double yieldNow) {
		this.yieldNow = yieldNow;
	}

	@Override
	public double getYieldToday() {
		return yieldToday;
	}

	@Override
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

	public int getNumberOfModules() {
		return (int) Math.round(troughLength / moduleLength);
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

	@Override
	public void addPrintMeshes(final List<Mesh> list) {
		addPrintMesh(list, reflectorBack);
		addPrintMesh(list, absorber);
		addPrintMesh(list, absorberCore);
		addPrintMesh(list, absorberEnd1);
		addPrintMesh(list, absorberEnd1Core);
		addPrintMesh(list, absorberEnd2);
		addPrintMesh(list, absorberEnd2Core);
		for (final Spatial mesh : modulesRoot.getChildren()) {
			addPrintMesh(list, (Mesh) mesh);
		}
	}

}
