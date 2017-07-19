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
import com.ardor3d.renderer.state.OffsetState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.scenegraph.shape.Cylinder;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.ui.text.BMText.Justify;
import com.ardor3d.util.geom.BufferUtils;

/**
 * @author Charles Xie
 *
 */

public class FresnelReflector extends HousePart implements Solar, Labelable {

	private static final long serialVersionUID = 1L;
	private static final ColorRGBA SKY_BLUE = new ColorRGBA(135f / 256f, 206f / 256f, 250f / 256f, 1);
	private transient ReadOnlyVector3 normal;
	private transient Box reflector;
	private transient Line outlines;
	private transient Node modulesRoot;
	private transient Line lightBeams;
	private transient BMText label;
	private transient double copyLayoutGap = 0.2;
	private transient double yieldNow; // solar output at current hour
	private transient double yieldToday;
	private Foundation absorber;
	private double reflectance = 0.9; // a number in (0, 1), iron glass has a reflectance of 0.9 (but dirt and dust reduce it to 0.82, this is accounted for by Atmosphere)
	private double absorptance = 0.95; // the percentage of energy absorbed by the absorber tube
	private double opticalEfficiency = 0.7;
	private double moduleLength = 3;
	private double moduleWidth = 2;
	private double length = 2 * moduleLength;
	private double relativeAzimuth = 0;
	private double baseHeight = 15;
	private boolean beamsVisible;
	private boolean labelEnergyOutput;
	private transient Vector3 oldReflectorCenter;
	private transient double oldLength, oldModuleWidth;
	private transient double oldRelativeAzimuth;
	private transient double baseZ;
	private int nSectionLength = 16; // number of sections in the direction of length (must be power of 2)
	private int nSectionWidth = 4; // number of sections in the direction of width (must be power of 2)
	private boolean detailed; // allows us to draw more details when there are fewer reflectors in the scene
	private static transient BloomRenderPass bloomRenderPass;

	public FresnelReflector() {
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
		if (Util.isZero(length)) {
			length = 2 * moduleLength;
		}
		if (Util.isZero(moduleWidth)) {
			moduleWidth = 2;
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
		if (Util.isZero(nSectionLength)) {
			nSectionLength = 16;
		}
		if (Util.isZero(nSectionWidth)) {
			nSectionWidth = 4;
		}
		detailed = Scene.getInstance().countParts(this.getClass()) < 50;

		if (absorber != null) { // FIXME: Somehow the absorber foundation, when copied, doesn't point to the right object. This is not a prefect solution, but it fixes the problem.
			absorber = (Foundation) Scene.getInstance().getPart(absorber.getId());
		}

		mesh = new Mesh("Fresnel Reflector Face");
		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(6));
		mesh.getMeshData().setTextureBuffer(BufferUtils.createVector2Buffer(6), 0);
		mesh.setDefaultColor(SKY_BLUE);
		mesh.setModelBound(new OrientedBoundingBox());
		mesh.setUserData(new UserData(this));
		root.attachChild(mesh);

		reflector = new Box("Fresnel Reflector Box");
		reflector.setModelBound(new OrientedBoundingBox());
		final OffsetState offset = new OffsetState();
		offset.setFactor(1);
		offset.setUnits(1);
		reflector.setRenderState(offset);
		root.attachChild(reflector);

		final int nModules = Math.max(1, getNumberOfModules());
		outlines = new Line("Fresnel Reflector (Outline)");
		outlines.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(8 + (nModules - 1) * 2));
		outlines.setDefaultColor(ColorRGBA.BLACK);
		outlines.setModelBound(new OrientedBoundingBox());
		outlines.setLineWidth(0.01f);
		outlines.setStipplePattern((short) 0xffff);
		Util.disablePickShadowLight(outlines);
		root.attachChild(outlines);

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

		modulesRoot = new Node("Modules Root");
		root.attachChild(modulesRoot);
		updateTextureAndColor();

		if (!points.isEmpty()) {
			oldReflectorCenter = points.get(0).clone();
		}
		oldLength = length;
		oldModuleWidth = moduleWidth;

	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
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
				if (oldReflectorCenter != null) {
					points.get(0).set(oldReflectorCenter);
				}
			} else {
				oldReflectorCenter = points.get(0).clone();
			}
		} else {
			final ReadOnlyVector3 pEdit = getEditPointShape(editPointIndex).getTranslation();
			final Vector3 p;
			if (editPointIndex % 2 == 0) {
				final ReadOnlyVector3 p1 = getEditPointShape(editPointIndex == 2 ? 4 : 2).getTranslation();
				p = Util.closestPoint(pEdit, pEdit.subtract(p1, null).normalizeLocal(), x, y);
				if (p != null) {
					final double rw = p.distance(p1) * Scene.getInstance().getAnnotationScale();
					final Vector3 delta = toRelativeVector(p.subtract(pEdit, null)).multiplyLocal(0.5);
					points.get(0).addLocal(delta);
					getEditPointShape(editPointIndex).setTranslation(p);
					setModuleWidth(rw);
					if (outOfBound()) {
						if (oldReflectorCenter != null) {
							points.get(0).set(oldReflectorCenter);
						}
						setModuleWidth(oldLength);
					} else {
						oldReflectorCenter = points.get(0).clone();
						oldLength = length;
					}
				}
			} else {
				final ReadOnlyVector3 p1 = getEditPointShape(editPointIndex == 1 ? 3 : 1).getTranslation();
				p = Util.closestPoint(pEdit, pEdit.subtract(p1, null).normalizeLocal(), x, y);
				if (p != null) {
					final double rl = p.distance(p1) * Scene.getInstance().getAnnotationScale();
					final Vector3 delta = toRelativeVector(p.subtract(pEdit, null)).multiplyLocal(0.5);
					points.get(0).addLocal(delta);
					getEditPointShape(editPointIndex).setTranslation(p);
					setLength(rl);
					if (outOfBound()) {
						if (oldReflectorCenter != null) {
							points.get(0).set(oldReflectorCenter);
						}
						setLength(oldModuleWidth);
					} else {
						oldReflectorCenter = points.get(0).clone();
						oldModuleWidth = moduleWidth;
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
		baseZ = container instanceof Foundation ? container.getHeight() : container.getPoints().get(0).getZ();
		points.get(0).setZ(baseZ + baseHeight);

		final Vector3 center = getAbsPoint(0);
		final double annotationScale = Scene.getInstance().getAnnotationScale();
		reflector.setData(new Vector3(0, 0, 0), 0.5 * moduleWidth / annotationScale, 0.5 * length / annotationScale, 0.15);
		reflector.updateModelBound();

		final FloatBuffer boxVertexBuffer = reflector.getMeshData().getVertexBuffer();
		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		final FloatBuffer textureBuffer = mesh.getMeshData().getTextureBuffer(0);
		FloatBuffer outlineBuffer = outlines.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		textureBuffer.rewind();

		final int nModules = Math.max(1, getNumberOfModules());
		final int outlineBufferSize = 24 + nModules * 6;
		if (outlineBuffer.capacity() < outlineBufferSize) {
			outlineBuffer = BufferUtils.createFloatBuffer(outlineBufferSize);
			outlines.getMeshData().setVertexBuffer(outlineBuffer);
		} else {
			outlineBuffer.rewind();
			outlineBuffer.limit(outlineBufferSize);
		}

		int i = 8 * 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(1).put(0);
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		i += 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(0).put(0);
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		i += 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(0).put(1);
		textureBuffer.put(0).put(1);
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		i += 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(1).put(1);
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		i = 8 * 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(1).put(0);
		outlineBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));

		mesh.updateModelBound();

		modulesRoot.detachAllChildren();
		if (nModules > 1) {
			final Vector3 p0 = new Vector3(vertexBuffer.get(3), vertexBuffer.get(4), vertexBuffer.get(5)); // (0, 0)
			final Vector3 p1 = new Vector3(vertexBuffer.get(6), vertexBuffer.get(7), vertexBuffer.get(8)); // (1, 0)
			final Vector3 pd = p1.subtract(p0, null).normalizeLocal();
			for (double u = moduleLength; u < length; u += moduleLength) {
				final Vector3 p = pd.multiply((u - 0.5 * length) / annotationScale, null);
				addPole(p.addLocal(center), baseHeight, baseZ);
			}
			final Vector3 p2 = new Vector3(vertexBuffer.get(0), vertexBuffer.get(1), vertexBuffer.get(2)); // (0, 1)
			final Vector3 pm = p2.add(p0, null).multiplyLocal(0.5);
			final Vector3 pn = p2.subtract(p0, null).multiplyLocal(0.5);
			for (double u = moduleLength; u < length; u += moduleLength) {
				final Vector3 p = pd.multiply(u / annotationScale, null).addLocal(pm);
				Vector3 q = p.add(pn, null);
				outlineBuffer.put(q.getXf()).put(q.getYf()).put(q.getZf());
				q = p.subtract(pn, null);
				outlineBuffer.put(q.getXf()).put(q.getYf()).put(q.getZf());
			}
		} else {
			addPole(center, baseHeight, baseZ);
		}
		outlines.updateModelBound();
		modulesRoot.getSceneHints().setCullHint(CullHint.Inherit);

		if (absorber != null) {
			final Vector3 o = absorber.getSolarReceiverCenter();
			o.setY(center.getY());
			final Vector3 p = center.clone().subtractLocal(o).negateLocal().normalizeLocal();
			final Vector3 q = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).normalize(null);
			normal = p.add(q, null).multiplyLocal(0.5).normalizeLocal();
		}
		final ReadOnlyVector3 n = new Vector3(normal.getX(), 0, normal.getZ()).normalizeLocal();
		final Matrix3 rotation = new Matrix3().lookAt(n, Vector3.UNIT_Y);

		mesh.setRotation(rotation);
		mesh.setTranslation(center);
		reflector.setRotation(mesh.getRotation());
		reflector.setTranslation(mesh.getTranslation());
		outlines.setRotation(mesh.getRotation());
		outlines.setTranslation(mesh.getTranslation());

		mesh.updateModelBound();
		reflector.updateModelBound();
		outlines.updateModelBound();

		if (beamsVisible) {
			drawLightBeams();
		}

		updateLabel();

		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
		CollisionTreeManager.INSTANCE.removeCollisionTree(reflector);
		root.updateGeometricState(0);

	}

	public void drawLightBeams() {
		if (Heliodon.getInstance().isNightTime() || absorber == null || !beamsVisible) {
			lightBeams.setVisible(false);
			return;
		}
		final Vector3 o = getAbsPoint(0);
		final Vector3 c = absorber.getSolarReceiverCenter();
		c.setY(o.getY());
		final double length = c.distance(o);
		final Vector3 sunLocation = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).normalize(null);
		final FloatBuffer beamsVertices = lightBeams.getMeshData().getVertexBuffer();
		beamsVertices.rewind();

		final Vector3 r = new Vector3(o);
		r.addLocal(sunLocation.multiply(5000, null));
		beamsVertices.put(o.getXf()).put(o.getYf()).put(o.getZf());
		beamsVertices.put(r.getXf()).put(r.getYf()).put(r.getZf());

		final Vector3 s = sunLocation.multiplyLocal(length);
		final Vector3 p = new Matrix3().fromAngleAxis(Math.PI, normal).applyPost(s, null);
		p.addLocal(o);
		beamsVertices.put(o.getXf()).put(o.getYf()).put(o.getZf());
		beamsVertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
		lightBeams.updateModelBound();
		lightBeams.setVisible(true);
		if (bloomRenderPass == null) {
			bloomRenderPass = new BloomRenderPass(SceneManager.getInstance().getCamera(), 10);
			bloomRenderPass.setBlurIntensityMultiplier(0.5f);
			bloomRenderPass.setNrBlurPasses(2);
			SceneManager.getInstance().getPassManager().add(bloomRenderPass);
		}
		if (!bloomRenderPass.contains(lightBeams)) {
			bloomRenderPass.add(lightBeams);
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
			final double shift = 1;
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
		pole.setHeight(poleHeight - 0.5 * pole.getRadius()); // slightly shorter so that the pole won't penetrate the surface of the reflector
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
			if (child != this && child instanceof FresnelReflector && bound.intersects(child.mesh.getWorldBound())) {
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
		return Math.min(length, moduleWidth) / Scene.getInstance().getAnnotationScale() / (SceneManager.getInstance().isFineGrid() ? 100.0 : 20.0);
	}

	@Override
	protected void computeArea() {
		area = length * moduleWidth;
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

	private double checkCopyOverlap() { // copy only in the direction of reflector width (shorter side)
		final double w1 = moduleWidth / Scene.getInstance().getAnnotationScale();
		final Vector3 center = getAbsCenter();
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p.container == container && p != this && p instanceof FresnelReflector) {
				final FresnelReflector s2 = (FresnelReflector) p;
				final double w2 = s2.moduleWidth / Scene.getInstance().getAnnotationScale();
				final double distance = p.getAbsCenter().distance(center);
				if (distance < (w1 + w2) * 0.499) {
					return distance;
				}
			}
		}
		return -1;
	}

	@Override
	public HousePart copy(final boolean check) {
		final FresnelReflector c = (FresnelReflector) super.copy(false);
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

	// reflectors align with the north-south axis, so copy and paste only in the x-direction (east-west axis)
	private boolean isPositionLegal(final FresnelReflector reflector, final Foundation foundation) {
		final Vector3 p0 = foundation.getAbsPoint(0);
		final Vector3 p2 = foundation.getAbsPoint(2);
		double dx;
		double s;
		final FresnelReflector nearest = foundation.getNearestFresnelReflector(this);
		if (nearest != null) { // use the nearest reflector as the reference to infer next position
			final Vector3 d = getAbsCenter().subtractLocal(nearest.getAbsCenter());
			dx = Math.abs(d.getX());
			if (dx > moduleWidth * 5 / Scene.getInstance().getAnnotationScale()) {
				dx = (1 + copyLayoutGap) * moduleWidth / Scene.getInstance().getAnnotationScale();
				s = Math.signum(foundation.getAbsCenter().getX() - Scene.getInstance().getOriginalCopy().getAbsCenter().getX());
			} else {
				s = Math.signum(d.getX());
			}
		} else {
			dx = (1 + copyLayoutGap) * moduleWidth / Scene.getInstance().getAnnotationScale();
			s = Math.signum(foundation.getAbsCenter().getX() - Scene.getInstance().getOriginalCopy().getAbsCenter().getX());
		}
		s *= -Math.signum(Math.cos(Math.toRadians(foundation.getAzimuth())));
		final double tx = dx / p0.distance(p2);
		final double newX = points.get(0).getX() + s * tx;
		if (newX > 1 - tx || newX < tx) {
			return false;
		}
		reflector.points.get(0).setX(newX);
		final double o = reflector.checkCopyOverlap();
		if (o >= 0) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, your new Fresnel reflector is too close to an existing one (" + o + ").", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	public double getSystemEfficiency() {
		double e = reflectance * absorptance * opticalEfficiency;
		final Atmosphere atm = Scene.getInstance().getAtmosphere();
		if (atm != null) {
			e *= 1 - atm.getDustLoss(Heliodon.getInstance().getCalendar().get(Calendar.MONTH));
		}
		return e;
	}

	/** a number between 0 and 1 */
	public void setOpticalEfficiency(final double opticalEfficiency) {
		this.opticalEfficiency = opticalEfficiency;
	}

	/** a number between 0 and 1 */
	public double getOpticalEfficiency() {
		return opticalEfficiency;
	}

	/** a number between 0 and 1 */
	public void setReflectance(final double reflectance) {
		this.reflectance = reflectance;
	}

	/** a number between 0 and 1 */
	public double getReflectance() {
		return reflectance;
	}

	/** a number between 0 and 1 */
	public void setAbsorptance(final double absorptance) {
		this.absorptance = absorptance;
	}

	/** a number between 0 and 1 */
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

	public void move(final Vector3 v, final double steplength) {
		v.normalizeLocal().multiplyLocal(steplength);
		final Vector3 v_rel = toRelativeVector(v);
		points.get(0).addLocal(v_rel);
		draw();
		if (outOfBound()) {
			if (oldReflectorCenter != null) {
				points.get(0).set(oldReflectorCenter);
			}
		} else {
			oldReflectorCenter = points.get(0).clone();
		}
	}

	public void set(final Vector3 center, final double length, final double width) {
		points.get(0).set(toRelative(center));
		setLength(length);
		setModuleWidth(width);
		draw();
	}

	public double getModuleLength() {
		return moduleLength;
	}

	public void setModuleLength(final double moduleLength) {
		this.moduleLength = moduleLength;
	}

	public void setLength(final double length) {
		this.length = length;
	}

	public double getLength() {
		return length;
	}

	public void setModuleWidth(final double moduleWidth) {
		this.moduleWidth = moduleWidth;
	}

	public double getModuleWidth() {
		return moduleWidth;
	}

	public void ensureFullModules(final boolean dragged) {
		boolean ok = false;
		if (dragged) {
			if (editPointIndex > 0) { // the reflector has been resized
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
			setLength(n * moduleLength);
			drawMesh();
		}
	}

	@Override
	public void updateEditShapes() {
		final FloatBuffer buf = mesh.getMeshData().getVertexBuffer();
		final ReadOnlyTransform trans = mesh.getWorldTransform();
		final Vector3 v1 = new Vector3();
		final Vector3 v2 = new Vector3();
		BufferUtils.populateFromBuffer(v1, buf, 0);
		BufferUtils.populateFromBuffer(v2, buf, 1);
		final Vector3 p1 = trans.applyForward(v1).add(trans.applyForward(v2), null).multiplyLocal(0.5);
		BufferUtils.populateFromBuffer(v1, buf, 1);
		BufferUtils.populateFromBuffer(v2, buf, 2);
		final Vector3 p2 = trans.applyForward(v1).add(trans.applyForward(v2), null).multiplyLocal(0.5);
		BufferUtils.populateFromBuffer(v1, buf, 2);
		BufferUtils.populateFromBuffer(v2, buf, 4);
		final Vector3 p3 = trans.applyForward(v1).add(trans.applyForward(v2), null).multiplyLocal(0.5);
		BufferUtils.populateFromBuffer(v1, buf, 4);
		BufferUtils.populateFromBuffer(v2, buf, 0);
		final Vector3 p4 = trans.applyForward(v1).add(trans.applyForward(v2), null).multiplyLocal(0.5);
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

	public void setBeamVisible(final boolean beamsVisible) {
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

	public double getOutputToday() {
		return solarPotentialToday * getSystemEfficiency();
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
		return (int) Math.round(length / moduleLength);
	}

	public void setAbsorber(final Foundation absorber) {
		this.absorber = absorber;
	}

	public Foundation getAbsorber() {
		return absorber;
	}

	@Override
	public void delete() {
		super.delete();
		if (bloomRenderPass != null) {
			if (bloomRenderPass.contains(lightBeams)) {
				bloomRenderPass.remove(lightBeams);
			}
		}
	}

	public void setNSectionLength(final int nSelectionLength) {
		this.nSectionLength = nSelectionLength;
	}

	public int getNSectionLength() {
		return nSectionLength;
	}

	public void setNSectionWidth(final int nSelectionWidth) {
		this.nSectionWidth = nSelectionWidth;
	}

	public int getNSectionWidth() {
		return nSectionWidth;
	}

}
