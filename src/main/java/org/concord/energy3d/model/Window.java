package org.concord.energy3d.model;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.shapes.Annotation;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyTransform;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.geom.BufferUtils;

public class Window extends HousePart implements Thermalizable {

	public static final int NO_MUNTIN_BAR = -1;
	public static final int MORE_MUNTIN_BARS = 0;
	public static final int MEDIUM_MUNTIN_BARS = 1;
	public static final int LESS_MUNTIN_BARS = 2;

	private static final long serialVersionUID = 1L;
	private transient BMText label1;
	private transient Line bars;
	private int style = MORE_MUNTIN_BARS;

	// range: 0.25-0.80 (we choose 0.5 by default) - http://www.energystar.gov/index.cfm?c=windows_doors.pr_ind_tested
	private double solarHeatGainCoefficient = 0.5;
	private double uValue = 2.0; // default is IECC code for Massachusetts (https://energycode.pnl.gov/EnergyCodeReqs/index.jsp?state=Massachusetts);
	private double volumetricHeatCapacity = 0.5; // unit: kWh/m^3/C (1 kWh = 3.6 MJ)
	private ReadOnlyVector3 normal;

	public Window() {
		super(2, 4, 30.0);
	}

	@Override
	protected void init() {
		label1 = Annotation.makeNewLabel();
		super.init();
		mesh = new Mesh("Window");
		// mesh.getMeshData().setIndexMode(IndexMode.TriangleStrip);
		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(6));
		// mesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(4));
		mesh.setModelBound(new BoundingBox());
		mesh.getSceneHints().setCullHint(CullHint.Always);

		mesh.setUserData(new UserData(this));
		root.attachChild(mesh);

		label1.setAlign(Align.SouthWest);
		root.attachChild(label1);

		bars = new Line("Window (bars)");
		bars.setLineWidth(3);
		bars.setModelBound(new BoundingBox());
		Util.disablePickShadowLight(bars);
		bars.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(8));
		root.attachChild(bars);

	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		int index = editPointIndex;
		if (index == -1) {
			if (isFirstPointInserted())
				index = 3;
			else
				index = 0;
		}
		final PickedHousePart pick = pickContainer(x, y, new Class[] { Wall.class, Roof.class });
		Vector3 p = points.get(index);
		if (pick != null) {
			p.set(pick.getPoint());
			snapToGrid(p, getAbsPoint(index), getGridSize(), false);
			p = toRelative(p);
			if (container instanceof Wall) {
				toAbsolute(p);
				p = enforceContraints(p);
			}
		} else
			return;

		final ArrayList<Vector3> orgPoints = new ArrayList<Vector3>(points.size()); // (ArrayList<Vector3>) ObjectCloner.deepCopy(points);
		for (final Vector3 v : points)
			orgPoints.add(v.clone());

		points.get(index).set(p);

		if (!isFirstPointInserted()) {
			points.get(1).set(p);
			if (container instanceof Roof)
				normal = (ReadOnlyVector3) ((Roof) container).getRoofPartsRoot().getChild(pick.getUserData().getIndex()).getUserData();
		} else if (container instanceof Wall) {
			if (index == 0 || index == 3) {
				points.get(1).set(points.get(0).getX(), 0, points.get(3).getZ());
				points.get(2).set(points.get(3).getX(), 0, points.get(0).getZ());
			} else {
				points.get(0).set(points.get(1).getX(), 0, points.get(2).getZ());
				points.get(3).set(points.get(2).getX(), 0, points.get(1).getZ());
			}
		} else {			
			final Vector3 u = Vector3.UNIT_Z.cross(normal, null);
			final Vector3 v = normal.cross(u, null);
			if (index == 0 || index == 3) {
				final Vector3 p0 = getAbsPoint(0);
				final Vector3 p3 = getAbsPoint(3);				
				points.get(1).set(toRelative(Util.closestPoint(p0, v, p3, u)));
				points.get(2).set(toRelative(Util.closestPoint(p0, u, p3, v)));
			} else {
				final Vector3 p1 = getAbsPoint(1);
				final Vector3 p2 = getAbsPoint(2);				
				points.get(0).set(toRelative(Util.closestPoint(p1, v, p2, u)));
				points.get(3).set(toRelative(Util.closestPoint(p2, u, p1, v)));
			}			
//			points.get(0).setX(0.51);
//			points.get(0).setY(0.6);
//			points.get(1).setX(0.51);
//			points.get(1).setY(0.7);
//			points.get(2).setX(0.6);
//			points.get(2).setY(0.6);
//			points.get(3).setX(0.6);
//			points.get(3).setY(0.7);
			
		}

		if (isFirstPointInserted())
			if (container instanceof Wall && !((Wall) container).fits(this)) {
				for (int i = 0; i < points.size(); i++)
					points.get(i).set(orgPoints.get(i));
				return;
			}

		if (container != null) {
			draw();
			setEditPointsVisible(true);
			container.draw();
		}
	}

	@Override
	protected void drawMesh() {
		if (points.size() < 4)
			return;

		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		{
			ReadOnlyVector3 p = getAbsPoint(0);
			vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
			p = getAbsPoint(2);
			vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
			p = getAbsPoint(1);
			vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
			vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
			p = getAbsPoint(2);
			vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
			p = getAbsPoint(3);
			vertexBuffer.put(p.getXf()).put(p.getYf()).put(p.getZf());
		}
		mesh.updateModelBound();
		CollisionTreeManager.INSTANCE.updateCollisionTree(mesh);

		if (container instanceof Roof || style == NO_MUNTIN_BAR || isFrozen() || Util.isEqual(getAbsPoint(2), getAbsPoint(0)) || Util.isEqual(getAbsPoint(1), getAbsPoint(0)))
			bars.getSceneHints().setCullHint(CullHint.Always);
		else {
			final double divisionLength = 3.0 + style * 3.0;
			bars.getSceneHints().setCullHint(CullHint.Inherit);
			final Vector3 halfThickness = ((Wall) container).getThicknessNormal().multiply(0.5, null);
			FloatBuffer barsVertices = bars.getMeshData().getVertexBuffer();
			final int cols = (int) Math.max(2, getAbsPoint(0).distance(getAbsPoint(2)) / divisionLength);
			final int rows = (int) Math.max(2, getAbsPoint(0).distance(getAbsPoint(1)) / divisionLength);
			if (barsVertices.capacity() < (4 + rows + cols) * 6) {
				barsVertices = BufferUtils.createVector3Buffer((4 + rows + cols) * 2);
				bars.getMeshData().setVertexBuffer(barsVertices);
			} else {
				barsVertices.rewind();
				barsVertices.limit(barsVertices.capacity());
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
				u.multiply((double) col / cols, p).addLocal(o);
				barsVertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
				p.addLocal(v);
				barsVertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
			}
			for (int row = 1; row < rows; row++) {
				v.multiply((double) row / rows, p).addLocal(o);
				barsVertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
				p.addLocal(u);
				barsVertices.put(p.getXf()).put(p.getYf()).put(p.getZf());
			}
			p.set(halfThickness).negateLocal().normalizeLocal();
			barsVertices.limit(barsVertices.position());
			bars.getMeshData().updateVertexCount();
			bars.updateModelBound();
		}
	}

	@Override
	public void drawAnnotations() {
		if (points.size() < 4)
			return;
		int annotCounter = 0;

		final ReadOnlyVector3 v02 = container.getAbsPoint(2).subtract(container.getAbsPoint(0), null);

		final boolean reversedFace = v02.normalize(null).crossLocal(container.getNormal()).dot(Vector3.NEG_UNIT_Z) < 0.0;
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
		double xy = cornerXY.length();
		if (reversedFace)
			xy = v02.length() - xy;
		label1.setText("(" + Math.round(Scene.getInstance().getAnnotationScale() * 10 * xy) / 10.0 + ", " + Math.round(Scene.getInstance().getAnnotationScale() * 10.0 * (getAbsPoint(i0).getZ() - container.getAbsPoint(0).getZ())) / 10.0 + ")");

		final ReadOnlyTransform trans = container.getRoot().getTransform();
		final ReadOnlyVector3 faceDirection = trans.applyForwardVector(container.getNormal(), null);
		label1.setTranslation(getAbsPoint(i0));
		label1.setRotation(new Matrix3().fromAngles(0, 0, -Util.angleBetween(v02.normalize(null).multiplyLocal(reversedFace ? -1 : 1), Vector3.UNIT_X, Vector3.UNIT_Z)));

		final Vector3 center = trans.applyForward(getCenter(), null);
		final float lineWidth = original == null ? 1f : 2f;

		SizeAnnotation annot = fetchSizeAnnot(annotCounter++);
		annot.setRange(getAbsPoint(i0), getAbsPoint(i1), center, faceDirection, false, Align.Center, true, true, false);
		annot.setLineWidth(lineWidth);

		annot = fetchSizeAnnot(annotCounter++);
		annot.setRange(getAbsPoint(i0), getAbsPoint(i2), center, faceDirection, false, Align.Center, true, false, false);
		annot.setLineWidth(lineWidth);
	}

	@Override
	public ReadOnlyVector3 getCenter() {
		return bars.getModelBound().getCenter();
	}

	@Override
	public boolean isPrintable() {
		return false;
	}

	@Override
	public void setAnnotationsVisible(final boolean visible) {
		super.setAnnotationsVisible(visible);
		if (label1 != null)
			label1.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
	}

	private Vector3 enforceContraints(final ReadOnlyVector3 p) {
		if (container == null)
			return new Vector3(p);
		double wallx = container.getAbsPoint(2).subtract(container.getAbsPoint(0), null).length();
		if (Util.isZero(wallx))
			wallx = MathUtils.ZERO_TOLERANCE;
		final double margin = 0.2 / wallx;
		double x = Math.max(p.getX(), margin);
		x = Math.min(x, 1 - margin);
		return new Vector3(x, p.getY(), p.getZ());
	}

	@Override
	public void updateTextureAndColor() {
	}

	public void hideBars() {
		if (bars != null)
			bars.getSceneHints().setCullHint(CullHint.Always);
	}

	@Override
	public Vector3 getAbsPoint(final int index) {
		return container != null ? container.getRoot().getTransform().applyForward(super.getAbsPoint(index)) : super.getAbsPoint(index);
	}

	@Override
	public double getGridSize() {
		return 1.0;
	}

	@Override
	protected String getTextureFileName() {
		return null;
	}

	@Override
	public ReadOnlyVector3 getNormal() {
		return container.getNormal();
	}

	public void move(final Vector3 d, final ArrayList<Vector3> houseMoveStartPoints) {
		final List<Vector3> orgPoints = new ArrayList<Vector3>(points.size());
		for (int i = 0; i < points.size(); i++)
			orgPoints.add(points.get(i));

		final Wall wall = (Wall) container;
		final ReadOnlyVector3 d_rel = toRelative(getAbsPoint(0).subtract(d, null)).subtractLocal(points.get(0)).negateLocal();
		for (int i = 0; i < points.size(); i++) {
			final Vector3 newP = houseMoveStartPoints.get(i).add(d_rel, null);
			points.set(i, newP);
		}

		if (!wall.fits(this))
			for (int i = 0; i < points.size(); i++)
				points.set(i, orgPoints.get(i));

		draw();
		container.draw();

	}

	public void setStyle(final int style) {
		this.style = style;
	}

	public int getStyle() {
		return style;
	}

	public void setSolarHeatGainCoefficient(final double shgc) {
		solarHeatGainCoefficient = shgc;
	}

	public double getSolarHeatGainCoefficient() {
		return solarHeatGainCoefficient;
	}

	@Override
	protected void computeArea() {
		if (isDrawCompleted()) {
			final Vector3 p0 = getAbsPoint(0);
			final Vector3 p1 = getAbsPoint(1);
			final Vector3 p2 = getAbsPoint(2);
			final double C = 100.0;
			final double annotationScale = Scene.getInstance().getAnnotationScale();
			area = Math.round(Math.round(p2.subtract(p0, null).length() * annotationScale * C) / C * Math.round(p1.subtract(p0, null).length() * annotationScale * C) / C * C) / C;
		} else
			area = 0.0;
	}

	public void moveTo(HousePart target) {
		double w1 = target.getAbsPoint(0).distance(target.getAbsPoint(2));
		double h1 = target.getAbsPoint(0).distance(target.getAbsPoint(1));
		double w2 = container.getAbsPoint(0).distance(container.getAbsPoint(2));
		double h2 = container.getAbsPoint(0).distance(container.getAbsPoint(1));
		double ratioW = w2 / w1;
		double ratioH = h2 / h1;
		Vector3 v0 = points.get(0);
		Vector3 v1 = points.get(1);
		v1.setX(v0.getX() + (v1.getX() - v0.getX()) * ratioW);
		v1.setY(v0.getY() + (v1.getY() - v0.getY()) * ratioW);
		v1.setZ(v0.getZ() + (v1.getZ() - v0.getZ()) * ratioH);
		Vector3 v2 = points.get(2);
		v2.setX(v0.getX() + (v2.getX() - v0.getX()) * ratioW);
		v2.setY(v0.getY() + (v2.getY() - v0.getY()) * ratioW);
		v2.setZ(v0.getZ() + (v2.getZ() - v0.getZ()) * ratioH);
		Vector3 v3 = points.get(3);
		v3.setX(v0.getX() + (v3.getX() - v0.getX()) * ratioW);
		v3.setY(v0.getY() + (v3.getY() - v0.getY()) * ratioW);
		v3.setZ(v0.getZ() + (v3.getZ() - v0.getZ()) * ratioH);
		setContainer(target);
	}

	public boolean isCopyable() {
		return true;
	}

	/** tolerance is a fraction relative to the width of a solar panel */
	private boolean overlap(double tolerance) {
		tolerance *= getAbsPoint(0).distance(getAbsPoint(2));
		Vector3 center = getAbsCenter();
		for (HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Window && p != this && p.getContainer() == container) {
				if (p.getAbsCenter().distance(center) < tolerance)
					return true;
			}
		}
		return false;
	}

	public HousePart copy(boolean check) {
		Window c = (Window) super.copy(false);
		if (check) {
			double s = Math.signum(toRelative(container.getAbsCenter()).subtractLocal(toRelative(Scene.getInstance().getOriginalCopy().getAbsCenter())).dot(Vector3.UNIT_X));
			double shift = s * (points.get(0).distance(points.get(2)) + 0.01); // give it a small gap
			int n = c.getPoints().size();
			for (int i = 0; i < n; i++) {
				double newX = points.get(i).getX() + shift;
				if (newX > 1 - shift / 2 || newX < shift / 2) // reject it if out of range
					return null;
			}
			for (int i = 0; i < n; i++) {
				c.points.get(i).setX(points.get(i).getX() + shift);
			}
			if (c.overlap(0.1)) {
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, your new window is too close to an existing one.", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}
		return c;
	}

	public void setUValue(final double uValue) {
		this.uValue = uValue;
	}

	public double getUValue() {
		return uValue;
	}

	public void setVolumetricHeatCapacity(final double volumetricHeatCapacity) {
		this.volumetricHeatCapacity = volumetricHeatCapacity;
	}

	public double getVolumetricHeatCapacity() {
		return volumetricHeatCapacity;
	}

	@Override
	protected HousePart getContainerRelative() {
		return container instanceof Roof ? container.getContainerRelative() : container;
	}
}
