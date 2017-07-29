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
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.CullState;
import com.ardor3d.renderer.state.CullState.Face;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.shape.Cylinder;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.ui.text.BMText.Justify;
import com.ardor3d.util.geom.BufferUtils;

/**
 * @author Charles Xie
 *
 */

public class ParabolicDish extends HousePart implements SolarCollector, Labelable {

	private static final long serialVersionUID = 1L;
	private static final ColorRGBA SKY_BLUE = new ColorRGBA(135f / 256f, 206f / 256f, 250f / 256f, 1);
	private transient ReadOnlyVector3 normal;
	private transient Paraboloid dish;
	private transient Mesh dishBack;
	private transient Line outlines;
	private transient Cylinder post;
	private transient Cylinder duct;
	private transient Cylinder receiver;
	private transient Line lightBeams;
	private transient BMText label;
	private transient double copyLayoutGap = 0.2;
	private transient double yieldNow; // solar output at current hour
	private transient double yieldToday;
	private double reflectance = 0.9; // a number in (0, 1), iron glass has a reflectance of 0.9 (but dirt and dust reduce it to 0.82, this is accounted for by Atmosphere)
	private double absorptance = 0.95; // the percentage of energy absorbed by the tube in the line of focus
	private double opticalEfficiency = 0.7;
	private double thermalEfficiency = 0.6;
	private double rimRadius = 3;
	private double focalLength = 2;
	private double relativeAzimuth = 0;
	private double baseHeight = 18;
	private boolean beamsVisible;
	private boolean labelEnergyOutput;
	private transient Vector3 oldDishCenter;
	private transient double oldRimRadius;
	private transient double oldRelativeAzimuth;
	private static transient BloomRenderPass bloomRenderPassLight, bloomRenderPassReceiver;
	private transient double baseZ;
	private int nRadialSections = 32; // number of sections in the radial direction of a parabolic dish (must be power of 2)
	private int nAxialSections = 32; // number of sections in the axial direction of a parabolic dish (must be power of 2)
	private boolean detailed; // allows us to draw more details when there are fewer dishes in the scene

	public ParabolicDish() {
		super(1, 1, 0);
	}

	@Override
	protected void init() {
		super.init();

		if (Util.isZero(copyLayoutGap)) { // FIXME: Why is a transient member evaluated to zero?
			copyLayoutGap = 0.2;
		}
		if (Util.isZero(rimRadius)) {
			rimRadius = 3;
		}
		if (Util.isZero(focalLength)) {
			focalLength = 2;
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
			thermalEfficiency = 0.6;
		}
		if (Util.isZero(nRadialSections)) {
			nRadialSections = 32;
		}
		if (Util.isZero(nAxialSections)) {
			nAxialSections = 32;
		}
		detailed = Scene.getInstance().countParts(this.getClass()) < 50;

		final double annotationScale = Scene.getInstance().getAnnotationScale();
		mesh = new Paraboloid("Paraboloid", rimRadius / annotationScale, 2.0 * Math.sqrt(focalLength / annotationScale), nAxialSections, nRadialSections);
		mesh.setDefaultColor(SKY_BLUE);
		mesh.setModelBound(new OrientedBoundingBox());
		mesh.setUserData(new UserData(this));
		CullState cullState = new CullState();
		cullState.setCullFace(Face.Back);
		mesh.setRenderState(cullState);
		root.attachChild(mesh);
		dish = (Paraboloid) mesh;
		dishBack = mesh.makeCopy(true);
		dishBack.clearRenderState(StateType.Texture);
		dishBack.setDefaultColor(ColorRGBA.WHITE);
		cullState = new CullState();
		cullState.setCullFace(Face.None);
		dishBack.setRenderState(cullState);
		root.attachChild(dishBack);

		post = new Cylinder("Post Cylinder", 2, detailed ? 10 : 2, 10, 0); // if there are many mirrors, reduce the solution of post
		post.setDefaultColor(ColorRGBA.WHITE);
		post.setRadius(0.6);
		post.setRenderState(offsetState);
		post.setModelBound(new BoundingBox());
		post.updateModelBound();
		root.attachChild(post);

		duct = new Cylinder("Duct Cylinder", 2, detailed ? 10 : 2, 10, 0); // if there are many mirrors, reduce the solution of post
		duct.setDefaultColor(ColorRGBA.WHITE);
		duct.setRadius(0.6);
		duct.setRenderState(offsetState);
		duct.setModelBound(new BoundingBox());
		duct.updateModelBound();
		root.attachChild(duct);

		receiver = new Cylinder("Receiver Cylinder", 2, detailed ? 10 : 2, 10, 0, true); // if there are many mirrors, reduce the solution of post
		receiver.setDefaultColor(ColorRGBA.WHITE);
		receiver.setRadius(2);
		receiver.setHeight(3);
		receiver.setRenderState(offsetState);
		receiver.setModelBound(new BoundingBox());
		receiver.updateModelBound();
		root.attachChild(receiver);

		outlines = new Line("Parabolic Dish (Outline)");
		outlines.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(2 * (dish.getRSamples() + 1)));
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
		lightBeams.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(2));
		lightBeams.setDefaultColor(new ColorRGBA(1f, 1f, 1f, 1f));
		root.attachChild(lightBeams);

		label = new BMText("Label", "#" + id, FontManager.getInstance().getPartNumberFont(), Align.Center, Justify.Center);
		Util.initHousePartLabel(label);
		label.setFontScale(0.5);
		label.setVisible(false);
		root.attachChild(label);

		updateTextureAndColor();

		if (!points.isEmpty()) {
			oldDishCenter = points.get(0).clone();
		}
		oldRimRadius = rimRadius;

	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		final PickedHousePart picked = pickContainer(x, y, new Class<?>[] { Foundation.class });
		if (picked != null && picked.getUserData() != null) { // when the user data is null, it picks the land
			final Vector3 p = picked.getPoint().clone();
			snapToGrid(p, getAbsPoint(0), getGridSize(), false);
			points.get(0).set(toRelative(p));
		}
		if (outOfBound()) {
			if (oldDishCenter != null) {
				points.get(0).set(oldDishCenter);
			}
		} else {
			oldDishCenter = points.get(0).clone();
		}
		if (container != null) {
			draw();
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

		normal = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).normalize(null);
		if (Util.isEqual(normal, Vector3.UNIT_Z)) {
			normal = new Vector3(-0.001, 0, 1).normalizeLocal();
		}

		final double annotationScale = Scene.getInstance().getAnnotationScale();
		dish.setRimRadius(rimRadius / annotationScale);
		dish.updateModelBound();
		baseZ = container instanceof Foundation ? container.getHeight() : container.getPoints().get(0).getZ();
		points.get(0).setZ(baseZ + baseHeight);

		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		FloatBuffer outlineBuffer = outlines.getMeshData().getVertexBuffer();

		final int vertexCount = vertexBuffer.limit() / 3;
		final Vector3 center = getAbsPoint(0);

		final int rSamples = dish.getRSamples() + 1;
		final int outlineBufferSize = 6 * rSamples;
		if (outlineBuffer.capacity() < outlineBufferSize) {
			outlineBuffer = BufferUtils.createFloatBuffer(outlineBufferSize);
			outlines.getMeshData().setVertexBuffer(outlineBuffer);
		} else {
			outlineBuffer.rewind();
			outlineBuffer.limit(outlineBufferSize);
		}
		// draw the rim line
		final float zOffset = 0.01f;
		for (int i = vertexCount - rSamples * 2; i < vertexCount - 1 - rSamples; i++) {
			outlineBuffer.put(vertexBuffer.get(i * 3)).put(vertexBuffer.get(i * 3 + 1)).put(vertexBuffer.get(i * 3 + 2) + zOffset);
			outlineBuffer.put(vertexBuffer.get(i * 3 + 3)).put(vertexBuffer.get(i * 3 + 4)).put(vertexBuffer.get(i * 3 + 5) + zOffset);
		}

		final Matrix3 rotation = new Matrix3().lookAt(normal, Vector3.UNIT_Y);
		mesh.setRotation(rotation);
		mesh.setTranslation(center);
		dishBack.setRotation(rotation);
		dishBack.setTranslation(mesh.getTranslation());
		outlines.setRotation(rotation);
		outlines.setTranslation(mesh.getTranslation());
		mesh.updateModelBound();
		outlines.updateModelBound();

		post.setHeight(baseHeight - 0.5 * post.getRadius()); // slightly shorter so that the pole won't penetrate the surface of the dish
		final Vector3 p = center.clone();
		p.setZ(baseZ + post.getHeight() / 2);
		post.setTranslation(p);

		final double flScaled = focalLength / annotationScale;
		duct.setHeight(flScaled + receiver.getHeight());
		duct.setRotation(mesh.getRotation());
		duct.setTranslation(center.clone().addLocal(normal.multiply(flScaled * 0.5, null)));

		receiver.setRotation(mesh.getRotation());
		receiver.setTranslation(center.clone().addLocal(normal.multiply(flScaled + receiver.getHeight() * 0.5, null)));

		if (bloomRenderPassReceiver == null) {
			bloomRenderPassReceiver = new BloomRenderPass(SceneManager.getInstance().getCamera(), 10);
			bloomRenderPassReceiver.setBlurIntensityMultiplier(0.5f);
			// bloomRenderPassTube.setNrBlurPasses(2);
			SceneManager.getInstance().getPassManager().add(bloomRenderPassReceiver);
		}
		if (!bloomRenderPassReceiver.contains(receiver)) {
			bloomRenderPassReceiver.add(receiver);
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
		final FloatBuffer beamsBuffer = lightBeams.getMeshData().getVertexBuffer();
		beamsBuffer.rewind();
		final Vector3 sunLocation = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).normalize(null);
		sunLocation.multiplyLocal(10000);
		final Vector3 o = getAbsPoint(0);
		// draw line to sun
		final Vector3 r = o.clone();
		r.addLocal(sunLocation);
		beamsBuffer.put(o.getXf()).put(o.getYf()).put(o.getZf());
		beamsBuffer.put(r.getXf()).put(r.getYf()).put(r.getZf());
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
			final double shift = duct.getHeight() + receiver.getHeight();
			label.setTranslation((getAbsCenter()).addLocal(normal.multiply(shift, null)));
			label.setVisible(true);
		} else {
			label.setVisible(false);
		}
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
			if (child != this && child instanceof ParabolicDish && bound.intersects(child.mesh.getWorldBound())) {
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
		return "mirror.png";
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
		return rimRadius / (Scene.getInstance().getAnnotationScale() * (SceneManager.getInstance().isFineGrid() ? 100.0 : 20.0));
	}

	@Override
	protected void computeArea() {
		area = rimRadius * rimRadius * Math.PI;
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

	private double checkCopyOverlap() {
		final double w1 = rimRadius / Scene.getInstance().getAnnotationScale();
		final Vector3 center = getAbsCenter();
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p.container == container && p != this) {
				if (p instanceof ParabolicDish) {
					final ParabolicDish s2 = (ParabolicDish) p;
					final double w2 = s2.rimRadius / Scene.getInstance().getAnnotationScale();
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
		final ParabolicDish c = (ParabolicDish) super.copy(false);
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

	private boolean isPositionLegal(final ParabolicDish dish, final Foundation foundation) {
		final Vector3 p0 = container.getAbsPoint(0);
		final Vector3 p1 = container.getAbsPoint(1);
		final Vector3 p2 = container.getAbsPoint(2);
		final double a = -Math.toRadians(relativeAzimuth) * Math.signum(p2.subtract(p0, null).getX() * p1.subtract(p0, null).getY());
		final Vector3 v = new Vector3(Math.cos(a), Math.sin(a), 0);
		final double length = (1 + copyLayoutGap) * rimRadius * 2 / Scene.getInstance().getAnnotationScale();
		final double s = Math.signum(container.getAbsCenter().subtractLocal(Scene.getInstance().getOriginalCopy().getAbsCenter()).dot(v));
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
		dish.points.get(0).setX(newX);
		dish.points.get(0).setY(newY);
		final double o = dish.checkCopyOverlap();
		if (o >= 0) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, your new parabolic dish is too close to an existing one (" + o + ").", "Error", JOptionPane.ERROR_MESSAGE);
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
	public void setOpticalEfficiency(final double opticalEfficiency) {
		this.opticalEfficiency = opticalEfficiency;
	}

	/** a number between 0 and 1 */
	public double getOpticalEfficiency() {
		return opticalEfficiency;
	}

	/** a number between 0 and 1 */
	public void setThermalEfficiency(final double thermalEfficiency) {
		this.thermalEfficiency = thermalEfficiency;
	}

	/** a number between 0 and 1 */
	public double getThermalEfficiency() {
		return thermalEfficiency;
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
			if (oldDishCenter != null) {
				points.get(0).set(oldDishCenter);
			}
		} else {
			oldDishCenter = points.get(0).clone();
		}
	}

	public void setRimRadius(final double rimRadius) {
		this.rimRadius = rimRadius;
	}

	public double getRimRadius() {
		return rimRadius;
	}

	public void setFocalLength(final double focalLength) {
		this.focalLength = focalLength;
		dish.setCurvatureParameter(2.0 * Math.sqrt(focalLength / Scene.getInstance().getAnnotationScale()));
	}

	public double getFocalLength() {
		return focalLength;
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
		if (bloomRenderPassReceiver != null) {
			if (bloomRenderPassReceiver.contains(receiver)) {
				bloomRenderPassReceiver.remove(receiver);
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

	public void setNRadialSections(final int nRadialSections) {
		this.nRadialSections = nRadialSections;
	}

	public int getNRadialSections() {
		return nRadialSections;
	}

	public void setNAxialSections(final int nAxialSections) {
		this.nAxialSections = nAxialSections;
	}

	public int getNAxialSections() {
		return nAxialSections;
	}

}
