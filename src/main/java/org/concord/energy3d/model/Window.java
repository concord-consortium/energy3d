package org.concord.energy3d.model;

import java.awt.geom.Rectangle2D;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Annotation;
import org.concord.energy3d.shapes.SizeAnnotation;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyTransform;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.geom.BufferUtils;

public class Window extends HousePart implements Thermalizable {
	private static final long serialVersionUID = 1L;
	public static final int NO_MUNTIN_BAR = -1; // TODO: remove this in 2017 as it is no longer needed
	public static final int MORE_MUNTIN_BARS = 0;
	public static final int MEDIUM_MUNTIN_BARS = 1;
	public static final int LESS_MUNTIN_BARS = 2;
	private transient Mesh collisionMesh;
	private transient BMText label1;
	private transient Line bars;
	private transient int roofIndex;

	private ReadOnlyVector3 normal;
	private double solarHeatGainCoefficient = 0.5; // range: 0.25-0.80 (we choose 0.5 by default) - http://www.energystar.gov/index.cfm?c=windows_doors.pr_ind_tested
	private double uValue = 2.0; // default is IECC code for Massachusetts (https://energycode.pnl.gov/EnergyCodeReqs/index.jsp?state=Massachusetts);
	private double volumetricHeatCapacity = 0.5; // unit: kWh/m^3/C (1 kWh = 3.6MJ)
	private int style = MORE_MUNTIN_BARS;
	private boolean noHorizontalBars; // has to use negative as serialization defaults to false
	private boolean noVerticalBars;
	private ReadOnlyColorRGBA glassColor;

	public Window() {
		super(2, 4, 30.0);
	}

	@Override
	protected void init() {
		label1 = Annotation.makeNewLabel();
		super.init();

		if (Util.isZero(uValue))
			uValue = 2;
		if (Util.isZero(solarHeatGainCoefficient))
			solarHeatGainCoefficient = 0.5;
		else if (solarHeatGainCoefficient > 1) // backward compatibility, SHGC used to range from 0 to 100
			solarHeatGainCoefficient *= 0.01;
		if (Util.isZero(volumetricHeatCapacity))
			volumetricHeatCapacity = 0.5;
		if (glassColor == null)
			setColor(new ColorRGBA(0.3f, 0.3f, 0.5f, 0.5f));

		mesh = new Mesh("Window");
		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(6));
		mesh.getMeshData().setNormalBuffer(BufferUtils.createVector3Buffer(6));
		mesh.setModelBound(new BoundingBox());
		mesh.getSceneHints().setAllPickingHints(false);
		if (glassColor == null)
			glassColor = new ColorRGBA(0.3f, 0.3f, 0.5f, 0.5f);
		mesh.setDefaultColor(glassColor);
		final BlendState blend = new BlendState();
		blend.setBlendEnabled(true);
		// blend.setTestEnabled(true);
		mesh.setRenderState(blend);
		mesh.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		mesh.setRenderState(ms);
		root.attachChild(mesh);

		collisionMesh = new Mesh("Window Collision");
		collisionMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(6));
		collisionMesh.setVisible(false);
		collisionMesh.setUserData(new UserData(this));
		collisionMesh.setModelBound(new BoundingBox());
		root.attachChild(collisionMesh);

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

		final ArrayList<Vector3> orgPoints = new ArrayList<Vector3>(points.size());
		for (final Vector3 v : points)
			orgPoints.add(v.clone());

		points.get(index).set(p);

		if (container instanceof Roof)
			computeNormalAndKeepOnRoof();

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
			final boolean isFlat = Vector3.UNIT_Z.equals(normal);
			final ReadOnlyVector3 u = isFlat ? Vector3.UNIT_X : Vector3.UNIT_Z.cross(normal, null);
			final ReadOnlyVector3 v = isFlat ? Vector3.UNIT_Y : normal.cross(u, null);
			if (index == 0 || index == 3) {
				final Vector3 p0 = getAbsPoint(0);
				final Vector3 p3 = getAbsPoint(3);
				points.get(1).set(toRelative(Util.closestPoint(p0, v, p3, u)));
				points.get(2).set(toRelative(Util.closestPoint(p0, u, p3, v)));
			} else {
				final Vector3 p1 = getAbsPoint(1);
				final Vector3 p2 = getAbsPoint(2);
				points.get(0).set(toRelative(Util.closestPoint(p1, v, p2, u)));
				points.get(3).set(toRelative(Util.closestPoint(p1, u, p2, v)));
			}
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

		mesh.setVisible(container instanceof Roof);

		normal = computeNormalAndKeepOnRoof();
		final boolean isDrawable = isDrawable();
		setHighlight(!isDrawable);
		updateEditShapes();

		final boolean drawBars = style != NO_MUNTIN_BAR && !isFrozen() && !Util.isEqual(getAbsPoint(2), getAbsPoint(0)) && !Util.isEqual(getAbsPoint(1), getAbsPoint(0));
		final ReadOnlyVector3 meshOffset, barsOffset;
		if (!isDrawable) {
			meshOffset = new Vector3();
			barsOffset = normal.multiply(0.1, null);
		} else if (container instanceof Roof) {
			if (drawBars) {
				meshOffset = normal.multiply(-0.1, null);
				barsOffset = new Vector3();
			} else {
				meshOffset = new Vector3();
				barsOffset = null;
			}
		} else {
			meshOffset = normal.multiply(-0.6, null);
			if (drawBars)
				barsOffset = normal.multiply(-0.5, null);
			else
				barsOffset = null;
		}

		fillRectangleBuffer(mesh.getMeshData().getVertexBuffer(), meshOffset);
		final FloatBuffer normalBuffer = mesh.getMeshData().getNormalBuffer();
		for (int i = 0; i < 6; i += 1)
			BufferUtils.setInBuffer(normal.negate(null), normalBuffer, i);

		fillRectangleBuffer(collisionMesh.getMeshData().getVertexBuffer(), normal.multiply(0.1, null));

		mesh.updateModelBound();
		collisionMesh.updateModelBound();
		CollisionTreeManager.INSTANCE.removeCollisionTree(mesh);
		CollisionTreeManager.INSTANCE.removeCollisionTree(collisionMesh);

		if (!drawBars)
			bars.getSceneHints().setCullHint(CullHint.Always);
		else {
			bars.getSceneHints().setCullHint(CullHint.Inherit);
			final double divisionLength = 3.0 + style * 3.0;
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

			int i = 0;
			BufferUtils.setInBuffer(getAbsPoint(0).addLocal(barsOffset), barsVertices, i++);
			BufferUtils.setInBuffer(getAbsPoint(1).addLocal(barsOffset), barsVertices, i++);
			BufferUtils.setInBuffer(getAbsPoint(1).addLocal(barsOffset), barsVertices, i++);
			BufferUtils.setInBuffer(getAbsPoint(3).addLocal(barsOffset), barsVertices, i++);
			BufferUtils.setInBuffer(getAbsPoint(3).addLocal(barsOffset), barsVertices, i++);
			BufferUtils.setInBuffer(getAbsPoint(2).addLocal(barsOffset), barsVertices, i++);
			BufferUtils.setInBuffer(getAbsPoint(2).addLocal(barsOffset), barsVertices, i++);
			BufferUtils.setInBuffer(getAbsPoint(0).addLocal(barsOffset), barsVertices, i++);

			final ReadOnlyVector3 o = getAbsPoint(0).add(barsOffset, null);
			final ReadOnlyVector3 u = getAbsPoint(2).subtract(getAbsPoint(0), null);
			final ReadOnlyVector3 v = getAbsPoint(1).subtract(getAbsPoint(0), null);
			final Vector3 p = new Vector3();
			if (!noVerticalBars) {
				for (int col = 1; col < cols; col++) {
					u.multiply((double) col / cols, p).addLocal(o);
					BufferUtils.setInBuffer(p, barsVertices, i++);
					p.addLocal(v);
					BufferUtils.setInBuffer(p, barsVertices, i++);
				}
			}
			if (!noHorizontalBars) {
				for (int row = 1; row < rows; row++) {
					v.multiply((double) row / rows, p).addLocal(o);
					BufferUtils.setInBuffer(p, barsVertices, i++);
					p.addLocal(u);
					BufferUtils.setInBuffer(p, barsVertices, i++);
				}
			}
			barsVertices.limit(i * 3);
			bars.getMeshData().updateVertexCount();
			bars.updateModelBound();
		}
	}

	private void fillRectangleBuffer(final FloatBuffer vertexBuffer, final ReadOnlyVector3 meshOffset) {
		BufferUtils.setInBuffer(getAbsPoint(0).addLocal(meshOffset), vertexBuffer, 0);
		BufferUtils.setInBuffer(getAbsPoint(2).addLocal(meshOffset), vertexBuffer, 1);
		BufferUtils.setInBuffer(getAbsPoint(1).addLocal(meshOffset), vertexBuffer, 2);
		BufferUtils.setInBuffer(getAbsPoint(1).addLocal(meshOffset), vertexBuffer, 3);
		BufferUtils.setInBuffer(getAbsPoint(2).addLocal(meshOffset), vertexBuffer, 4);
		BufferUtils.setInBuffer(getAbsPoint(3).addLocal(meshOffset), vertexBuffer, 5);
	}

	@Override
	public void drawAnnotations() {
		if (points.size() < 4)
			return;
		int annotCounter = 0;

		Vector3 p0 = getAbsPoint(0);
		Vector3 p1 = getAbsPoint(1);
		Vector3 p2 = getAbsPoint(2);
		Vector3 p3 = getAbsPoint(3);
		final int vIndex = getNormal().equals(Vector3.UNIT_Z) ? 1 : 2;
		if (!Util.isEqual(p0.getValue(vIndex), p2.getValue(vIndex))) {
			final Vector3 tmp = p0;
			p0 = p2;
			p2 = p3;
			p3 = p1;
			p1 = tmp;
		}
		if (p0.getValue(vIndex) > p1.getValue(vIndex)) {
			swap(p0, p1);
			swap(p2, p3);
		}
		final Vector3 p01 = p1.subtract(p0, null).normalizeLocal();
		if (p2.subtract(p0, null).normalizeLocal().dot(p01.cross(getNormal(), null)) < 0) {
			swap(p0, p2);
			swap(p1, p3);
		}
		final Vector3 cornerXY = p0.subtract(container.getAbsPoint(0), null);
		cornerXY.setZ(0);

		final ReadOnlyVector3 faceDirection = getNormal();
		if (container instanceof Wall) {
			final ReadOnlyVector3 v02 = container.getAbsPoint(2).subtract(container.getAbsPoint(0), null);
			final boolean reversedFace = v02.normalize(null).crossLocal(container.getNormal()).dot(Vector3.NEG_UNIT_Z) < 0.0;
			double xy = cornerXY.length();
			if (reversedFace)
				xy = v02.length() - xy;
			label1.setText("(" + Math.round(Scene.getInstance().getAnnotationScale() * 10 * xy) / 10.0 + ", " + Math.round(Scene.getInstance().getAnnotationScale() * 10.0 * (p0.getZ() - container.getAbsPoint(0).getZ())) / 10.0 + ")");
			label1.setTranslation(p0);
			label1.setRotation(new Matrix3().fromAngles(0, 0, -Util.angleBetween(v02.normalize(null).multiplyLocal(reversedFace ? -1 : 1), Vector3.UNIT_X, Vector3.UNIT_Z)));
		}

		final ReadOnlyVector3 center = getCenter();
		final float lineWidth = original == null ? 1f : 2f;

		SizeAnnotation annot = fetchSizeAnnot(annotCounter++);
		annot.setRange(p0, p1, center, faceDirection, false, Align.Center, true, true, false);
		annot.setLineWidth(lineWidth);

		annot = fetchSizeAnnot(annotCounter++);
		annot.setRange(p0, p2, center, faceDirection, false, Align.Center, true, false, false);
		annot.setLineWidth(lineWidth);
	}

	private void swap(final Vector3 v1, final Vector3 v2) {
		final Vector3 tmp = Vector3.fetchTempInstance();
		tmp.set(v1);
		v1.set(v2);
		v2.set(tmp);
		Vector3.releaseTempInstance(tmp);
	}

	@Override
	public ReadOnlyVector3 getCenter() {
		// return bars.getModelBound().getCenter();
		return mesh.getModelBound().getCenter();
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
		return SceneManager.getInstance().isFineGrid() ? 1.0 : 2.0;
	}

	@Override
	protected String getTextureFileName() {
		return null;
	}

	@Override
	public ReadOnlyVector3 getNormal() {
		return normal;
	}

	public void move(final Vector3 d, final ArrayList<Vector3> houseMoveStartPoints) {
		final Vector3 p0 = getAbsPoint(0);
		final Vector3 p = toAbsolute(houseMoveStartPoints.get(0)).addLocal(d);
		snapToGrid(p, p0, getGridSize(), false);
		final Vector3 p_rel = toRelative(p);
		final Vector3 d_snap_rel = p_rel.subtract(houseMoveStartPoints.get(0), null);
		points.get(0).set(p_rel);

		for (int i = 1; i < 4; i++)
			points.get(i).set(houseMoveStartPoints.get(i).add(d_snap_rel, null));

		draw();
		container.draw();
	}

	public void move(final Vector3 v) {
		v.multiplyLocal(getGridSize());
		ArrayList<Vector3> movePoints = new ArrayList<Vector3>(points.size());
		for (final Vector3 p : points)
			movePoints.add(p.clone());
		move(v, movePoints);
	}

	public void setStyle(final int style) {
		this.style = style;
	}

	public int getStyle() {
		return style;
	}

	public void setSolarHeatGainCoefficient(final double shgc) {
		solarHeatGainCoefficient = shgc;
		setColor(new ColorRGBA(glassColor.getRed(), glassColor.getGreen(), glassColor.getBlue(), (float) (1.0 - shgc)));
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

	public void moveTo(final HousePart target) {
		final double w1 = target.getAbsPoint(0).distance(target.getAbsPoint(2));
		final double h1 = target.getAbsPoint(0).distance(target.getAbsPoint(1));
		final double w2 = container.getAbsPoint(0).distance(container.getAbsPoint(2));
		final double h2 = container.getAbsPoint(0).distance(container.getAbsPoint(1));
		final double ratioW = w2 / w1;
		final double ratioH = h2 / h1;
		final Vector3 v0 = points.get(0);
		final Vector3 v1 = points.get(1);
		v1.setX(v0.getX() + (v1.getX() - v0.getX()) * ratioW);
		v1.setY(v0.getY() + (v1.getY() - v0.getY()) * ratioW);
		v1.setZ(v0.getZ() + (v1.getZ() - v0.getZ()) * ratioH);
		final Vector3 v2 = points.get(2);
		v2.setX(v0.getX() + (v2.getX() - v0.getX()) * ratioW);
		v2.setY(v0.getY() + (v2.getY() - v0.getY()) * ratioW);
		v2.setZ(v0.getZ() + (v2.getZ() - v0.getZ()) * ratioH);
		final Vector3 v3 = points.get(3);
		v3.setX(v0.getX() + (v3.getX() - v0.getX()) * ratioW);
		v3.setY(v0.getY() + (v3.getY() - v0.getY()) * ratioW);
		v3.setZ(v0.getZ() + (v3.getZ() - v0.getZ()) * ratioH);
		setContainer(target);
	}

	@Override
	public boolean isCopyable() {
		return true;
	}

	/** tolerance is a fraction relative to the width of a window */
	private boolean overlap(double tolerance) {
		tolerance *= getAbsPoint(0).distance(getAbsPoint(2));
		final Vector3 center = getAbsCenter();
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Window && p != this && p.getContainer() == container) {
				if (p.getAbsCenter().distance(center) < tolerance)
					return true;
			}
		}
		return false;
	}

	@Override
	public HousePart copy(final boolean check) {
		final Window c = (Window) super.copy(false);
		if (check) {
			if (container instanceof Wall) {
				final double s = Math.signum(toRelative(container.getAbsCenter()).subtractLocal(toRelative(Scene.getInstance().getOriginalCopy().getAbsCenter())).dot(Vector3.UNIT_X));
				final double shift = s * (points.get(0).distance(points.get(2)) * 2); // place the next window one width away
				final int n = c.getPoints().size();
				for (int i = 0; i < n; i++) {
					final double newX = points.get(i).getX() + shift;
					if (newX > 1 - shift / 20 || newX < shift / 20) // reject it if out of range
						return null;
				}
				for (int i = 0; i < n; i++) {
					c.points.get(i).setX(points.get(i).getX() + shift);
				}
				if (c.overlap(0.9)) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, your new window is too close to an existing one.", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			} else if (container instanceof Roof) {
				if (normal == null) {
					// don't remove this error message just in case this happens again
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Normal of window [" + c + "] is null. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
				final Vector3 d = normal.cross(Vector3.UNIT_Z, null);
				d.normalizeLocal();
				if (Util.isZero(d.length()))
					d.set(1, 0, 0);
				final Vector3 d0 = d.clone();
				final double shift = getAbsPoint(0).distance(getAbsPoint(2)) + 0.01; // give it a small gap
				d.multiplyLocal(shift);
				d.addLocal(getContainerRelative().getPoints().get(0));
				final Vector3 v = toRelative(d);
				final Vector3 originalCenter = Scene.getInstance().getOriginalCopy().getAbsCenter();
				final double s = Math.signum(container.getAbsCenter().subtractLocal(originalCenter).dot(d0));
				final int n = c.getPoints().size();
				for (int i = 0; i < n; i++) {
					c.points.get(i).setX(points.get(i).getX() + s * v.getX());
					c.points.get(i).setY(points.get(i).getY() + s * v.getY());
					c.points.get(i).setZ(points.get(i).getZ() + s * v.getZ());
				}
				final Roof roof = (Roof) c.container;
				boolean outsideWalls = false;
				for (int i = 0; i < n; i++) {
					if (!roof.insideWallsPolygon(c.getAbsPoint(i))) {
						outsideWalls = true;
						break;
					}
				}
				if (outsideWalls) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, you are not allowed to paste a window outside a roof.", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
				if (c.overlap(0.9)) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "Sorry, your new window is too close to an existing one.", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			}
		}
		return c;
	}

	@Override
	public void setColor(final ReadOnlyColorRGBA color) {
		glassColor = color;
		if (mesh != null)
			mesh.setDefaultColor(glassColor);
	}

	@Override
	public ReadOnlyColorRGBA getColor() {
		return glassColor;
	}

	@Override
	public void setUValue(final double uValue) {
		this.uValue = uValue;
	}

	@Override
	public double getUValue() {
		return uValue;
	}

	@Override
	public void setVolumetricHeatCapacity(final double volumetricHeatCapacity) {
		this.volumetricHeatCapacity = volumetricHeatCapacity;
	}

	@Override
	public double getVolumetricHeatCapacity() {
		return volumetricHeatCapacity;
	}

	@Override
	protected HousePart getContainerRelative() {
		return container instanceof Roof ? container.getContainerRelative() : container;
	}

	public void setRoofIndex(final int index) {
		this.roofIndex = index;
	}

	public int getRoofIndex() {
		return roofIndex;
	}

	@Override
	public Spatial getCollisionSpatial() {
		return collisionMesh;
	}

	@Override
	public Mesh getRadiationMesh() {
		return collisionMesh;
	}

	@Override
	public Spatial getRadiationCollisionSpatial() {
		return getRadiationMesh();
	}

	@Override
	public boolean isDrawable() {
		if (container == null) // FIXME: There is a chance that a solar panel can be left without a container
			return true;
		if (!super.isDrawable())
			return false;
		if (!container.fits(this))
			return false;

		computeNormalAndKeepOnRoof(); // in case roof changes and has different roof part #s
		final boolean isRoof = container instanceof Roof;
		final Rectangle2D thisWindow = makeRectangle(this, isRoof);
		for (final HousePart part : container.getChildren())
			if (part != this && part instanceof Window && (!isRoof || part.containerRoofIndex == this.containerRoofIndex)) {
				final Rectangle2D otherWindow = makeRectangle((Window) part, isRoof);
				if (thisWindow.intersects(otherWindow))
					return false;
			}
		return true;
	}

	private Rectangle2D makeRectangle(final Window window, final boolean isRoof) {
		final ReadOnlyTransform transform;
		if (isRoof)
			transform = ((Roof) container).getRoofPartsRoot().getChild(window.containerRoofIndex).getWorldTransform();
		else
			transform = null;
		Rectangle2D thisWindow = null;
		for (int i = 0; i < window.getPoints().size(); i++) {
			final Vector3 p = isRoof ? window.getAbsPoint(i) : window.points.get(i);
			if (isRoof)
				transform.applyInverse(p);
			final double y = isRoof ? p.getY() : p.getZ();
			if (thisWindow == null)
				thisWindow = new Rectangle2D.Double(p.getX(), y, 0, 0);
			thisWindow.add(p.getX(), y);
		}
		return thisWindow;
	}

	@Override
	public boolean isValid() {
		if (!super.isValid())
			return false;
		return super.isDrawable();
	}

	public void setHorizontalBars(final boolean horizontalBars) {
		noHorizontalBars = !horizontalBars;
	}

	public boolean getHorizontalBars() {
		return !noHorizontalBars;
	}

	public void setVerticalBars(final boolean verticalBars) {
		noVerticalBars = !verticalBars;
	}

	public boolean getVerticalBars() {
		return !noVerticalBars;
	}

}