package org.concord.energy3d.model;

import java.nio.FloatBuffer;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
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
import com.ardor3d.scenegraph.Line;
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
	private transient Cylinder absorber;
	private transient Cylinder absorberEnd1;
	private transient Cylinder absorberEnd2;
	private transient Line outlines;
	private transient Line steelFrame;
	private transient Node sectionsRoot;
	private transient Line lightBeams;
	private transient BMText label;
	private transient double copyLayoutGap = 1;
	private transient double yieldNow; // solar output at current hour
	private transient double yieldToday;
	private double reflectivity = 0.9; // a number in (0, 1), iron glass has a reflectivity of 0.9 (but dirt and dust reduce it to 0.82, this is accounted for by Atmosphere)
	private double troughLength = 5;
	private double troughWidth = 2;
	private double semilatusRectum = 2;
	private double relativeAzimuth = 0;
	private double baseHeight = 5;
	private double sectionDistanceX = 4;
	private double sectionDistanceY = 2;
	private boolean drawSunBeam;
	private boolean labelEnergyOutput;
	private transient Vector3 oldTroughCenter;
	private transient double oldTroughLength, oldTroughWidth;
	private static transient BloomRenderPass bloomRenderPass;
	private transient double baseZ;

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
			troughLength = 5;
		}
		if (Util.isZero(troughWidth)) {
			troughWidth = 2;
		}
		if (Util.isZero(semilatusRectum)) {
			semilatusRectum = 2;
		}
		if (Util.isZero(reflectivity)) {
			reflectivity = 0.9;
		}

		mesh = new ParabolicCylinder("Parabolic Cylinder", 10, semilatusRectum, troughWidth, troughLength);
		mesh.setDefaultColor(SKY_BLUE);
		mesh.setModelBound(new OrientedBoundingBox());
		mesh.setUserData(new UserData(this));
		root.attachChild(mesh);
		reflector = (ParabolicCylinder) mesh;

		absorber = new Cylinder("Absorber Tube", 10, 10, 10, 0);
		absorber.setRadius(0.5);
		absorber.setDefaultColor(ColorRGBA.GRAY);
		absorber.setModelBound(new OrientedBoundingBox());
		root.attachChild(absorber);

		absorberEnd1 = new Cylinder("Absorber End Tube 1", 10, 10, 10, 0);
		absorberEnd1.setRadius(0.5);
		absorberEnd1.setDefaultColor(ColorRGBA.GRAY);
		absorberEnd1.setModelBound(new OrientedBoundingBox());
		root.attachChild(absorberEnd1);

		absorberEnd2 = new Cylinder("Absorber End Tube 2", 10, 10, 10, 0);
		absorberEnd2.setRadius(0.5);
		absorberEnd2.setDefaultColor(ColorRGBA.GRAY);
		absorberEnd2.setModelBound(new OrientedBoundingBox());
		root.attachChild(absorberEnd2);

		outlines = new Line("Parabolic Trough (Outline)");
		outlines.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(4 * (reflector.getNumberOfSamples() + 1)));
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

		sectionsRoot = new Node("Poles Root");
		root.attachChild(sectionsRoot);
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
					final double rw = p.distance(p1) * Scene.getInstance().getAnnotationScale();
					final Vector3 delta = toRelativeVector(p.subtract(pEdit, null)).multiplyLocal(0.5);
					points.get(0).addLocal(delta);
					getEditPointShape(editPointIndex).setTranslation(p);
					setTroughLength(rw);
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
					final double rh = p.distance(p1) * Scene.getInstance().getAnnotationScale();
					final Vector3 delta = toRelativeVector(p.subtract(pEdit, null)).multiplyLocal(0.5);
					points.get(0).addLocal(delta);
					getEditPointShape(editPointIndex).setTranslation(p);
					setTroughWidth(rh);
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
		absorberEnd1.setHeight(0.5 * reflector.getSemilatusRectum());
		absorberEnd2.setHeight(absorberEnd1.getHeight());

		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		final FloatBuffer outlineBuffer = outlines.getMeshData().getVertexBuffer();
		FloatBuffer steelFrameBuffer = steelFrame.getMeshData().getVertexBuffer();
		outlineBuffer.rewind();
		final int vertexCount = vertexBuffer.limit() / 6;
		for (int i = 0; i < vertexCount - 1; i++) {
			outlineBuffer.put(vertexBuffer.get(i * 3)).put(vertexBuffer.get(i * 3 + 1)).put(vertexBuffer.get(i * 3 + 2));
			outlineBuffer.put(vertexBuffer.get(i * 3 + 3)).put(vertexBuffer.get(i * 3 + 4)).put(vertexBuffer.get(i * 3 + 5));
		}
		final int j = (vertexCount - 2) * 3 + 6;
		for (int i = 0; i < vertexCount - 1; i++) {
			outlineBuffer.put(vertexBuffer.get(j + i * 3)).put(vertexBuffer.get(j + i * 3 + 1)).put(vertexBuffer.get(j + i * 3 + 2));
			outlineBuffer.put(vertexBuffer.get(j + i * 3 + 3)).put(vertexBuffer.get(j + i * 3 + 4)).put(vertexBuffer.get(j + i * 3 + 5));
		}
		outlineBuffer.put(vertexBuffer.get(0)).put(vertexBuffer.get(1)).put(vertexBuffer.get(2));
		outlineBuffer.put(vertexBuffer.get(j)).put(vertexBuffer.get(j + 1)).put(vertexBuffer.get(j + 2));
		outlineBuffer.put(vertexBuffer.get(j - 3)).put(vertexBuffer.get(j - 2)).put(vertexBuffer.get(j - 1));
		outlineBuffer.put(vertexBuffer.get(2 * j - 3)).put(vertexBuffer.get(2 * j - 2)).put(vertexBuffer.get(2 * j - 1));

		final int j1 = (j - 3) / 2; // index of middle point on one end
		final int j2 = (3 * j - 3) / 2; // index of middle point on the other end
		final Vector3 p1 = new Vector3(vertexBuffer.get(j1), vertexBuffer.get(j1 + 1), vertexBuffer.get(j1 + 2));
		final Vector3 p2 = new Vector3(vertexBuffer.get(j2), vertexBuffer.get(j2 + 1), vertexBuffer.get(j2 + 2));
		final Vector3 pd = p2.subtract(p1, null).normalizeLocal();

		mesh.updateModelBound();
		outlines.updateModelBound();
		absorber.updateModelBound();

		final ReadOnlyVector3 n = new Vector3(normal.getX(), 0, normal.getZ()).normalizeLocal();
		final Matrix3 rotation = new Matrix3().lookAt(n, Vector3.UNIT_Y);
		mesh.setRotation(rotation);
		mesh.setTranslation(getAbsPoint(0));
		outlines.setRotation(rotation);
		outlines.setTranslation(mesh.getTranslation());
		absorber.setRotation(new Matrix3().applyRotationX(Math.PI / 2));
		absorber.setTranslation(mesh.getTranslation().add(n.multiply(0.5 * reflector.getSemilatusRectum(), null), null));
		final Vector3 endShift = n.multiply(0.5 * absorberEnd1.getHeight(), null);
		absorberEnd1.setTranslation(mesh.getTranslation().add(p1.add(endShift, null), null));
		absorberEnd2.setTranslation(mesh.getTranslation().add(p2.add(endShift, null), null));
		absorberEnd1.setRotation(rotation);
		absorberEnd2.setRotation(rotation);

		final int steelBufferSize = ((int) (Math.round(troughLength / sectionDistanceX)) * 2 + 2) * 3;
		if (steelFrameBuffer.capacity() < steelBufferSize) {
			steelFrameBuffer = BufferUtils.createFloatBuffer(steelBufferSize);
			steelFrame.getMeshData().setVertexBuffer(steelFrameBuffer);
		} else {
			steelFrameBuffer.rewind();
		}
		steelFrameBuffer.put(p1.getXf()).put(p1.getYf()).put(p1.getZf());
		steelFrameBuffer.put(p2.getXf()).put(p2.getYf()).put(p2.getZf());

		sectionsRoot.detachAllChildren();
		final double halfLength = troughLength * 0.5;
		final Vector3 center = getAbsPoint(0);
		for (double u = halfLength; u < troughLength; u += sectionDistanceX) {
			final Vector3 p = pd.multiply((u - halfLength) / annotationScale, null);
			steelFrameBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
			steelFrameBuffer.put(p.getXf()).put(p.getYf()).put((float) (p.getZ() + 0.5 * reflector.getSemilatusRectum()));
			addPole(p.addLocal(center), baseHeight, baseZ);
		}
		for (double u = halfLength - sectionDistanceX; u > 0; u -= sectionDistanceX) {
			final Vector3 p = pd.multiply((u - halfLength) / annotationScale, null);
			steelFrameBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
			steelFrameBuffer.put(p.getXf()).put(p.getYf()).put((float) (p.getZ() + 0.5 * reflector.getSemilatusRectum()));
			addPole(p.addLocal(center), baseHeight, baseZ);
		}
		sectionsRoot.getSceneHints().setCullHint(CullHint.Inherit);

		steelFrame.updateModelBound();
		steelFrame.setRotation(rotation);
		steelFrame.setTranslation(mesh.getTranslation());

		if (drawSunBeam) {
			drawLightBeams();
		}

		updateLabel();

		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
		root.updateGeometricState(0);

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
			final double shift = 1;
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
		sectionsRoot.attachChild(pole);
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

	private transient double oldRelativeAzimuth;

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

	public double getSectionDistanceX() {
		return sectionDistanceX;
	}

	public void setSectionDistanceX(final double sectionDistanceX) {
		this.sectionDistanceX = sectionDistanceX;
	}

	public double getSectionDistanceY() {
		return sectionDistanceY;
	}

	public void setSectionDistanceY(final double sectionDistanceY) {
		this.sectionDistanceY = sectionDistanceY;
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
		BufferUtils.populateFromBuffer(v1, buf, j);
		BufferUtils.populateFromBuffer(v2, buf, 2 * j - 1);
		final Vector3 p3 = trans.applyForward(v1).add(trans.applyForward(v2), null).multiplyLocal(0.5).subtractLocal(shift); // along the direction of width
		BufferUtils.populateFromBuffer(v1, buf, j - 1);
		BufferUtils.populateFromBuffer(v2, buf, 2 * j - 1);
		final Vector3 p4 = trans.applyForward(v1).add(trans.applyForward(v2), null).multiplyLocal(0.5); // along the direction of length
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
		getEditPointShape(0).setTranslation(p1.addLocal(p4).multiplyLocal(0.5).addLocal(0, 0, 0.15));
	}

	private Vector3 getVertex(final int i) {
		final Vector3 v = new Vector3();
		BufferUtils.populateFromBuffer(v, mesh.getMeshData().getVertexBuffer(), i);
		return mesh.getWorldTransform().applyForward(v);
	}

	public void drawLightBeams() {
		if (Heliodon.getInstance().isNightTime() || !drawSunBeam) {
			lightBeams.setVisible(false);
			return;
		}
		final Vector3 o = getAbsPoint(0);
		final Vector3 sunLocation = Heliodon.getInstance().computeSunLocation(Heliodon.getInstance().getCalendar()).normalize(null);
		final FloatBuffer beamsVertices = lightBeams.getMeshData().getVertexBuffer();
		beamsVertices.rewind();
		final Vector3 r = o.clone(); // draw sun vector
		r.addLocal(sunLocation.multiply(10000, null));
		beamsVertices.put(o.getXf()).put(o.getYf()).put(o.getZf());
		beamsVertices.put(r.getXf()).put(r.getYf()).put(r.getZf());
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
	public void delete() {
		super.delete();
		if (bloomRenderPass != null) {
			if (bloomRenderPass.contains(lightBeams)) {
				bloomRenderPass.remove(lightBeams);
			}
		}
	}

	public void setSunBeamVisible(final boolean drawSunBeam) {
		this.drawSunBeam = drawSunBeam;
	}

	public boolean isDrawSunBeamVisible() {
		return drawSunBeam;
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

}
