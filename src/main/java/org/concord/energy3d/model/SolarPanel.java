package org.concord.energy3d.model;

import java.nio.FloatBuffer;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.intersection.PickData;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.state.OffsetState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.geom.BufferUtils;

public class SolarPanel extends HousePart {

	public static final double WIDTH = 1.0;
	public static final double HEIGHT = 1.65;
	private static final long serialVersionUID = 1L;
	private transient ReadOnlyVector3 normal;
	private transient Mesh outlineMesh;
	private transient Box surround;
	private double efficiency = 0; // between 0 and 100

	public SolarPanel() {
		super(1, 1, 0.0);
		efficiency = Scene.getInstance().getSolarPanelEfficiencyNotPercentage() * 100;
	}

	/** a percentage number between 0 and 100 */
	public void setEfficiency(final double efficiency) {
		this.efficiency = efficiency;
	}

	/** a percentage number between 0 and 100 */
	public double getEfficiency() {
		return efficiency;
	}

	/** a number between 0 and 1 for calculation */
	public double getEfficiencyNotPercentage() {
		return Util.isZero(efficiency) ? Scene.getInstance().getSolarPanelEfficiencyNotPercentage() : efficiency * 0.01;
	}

	@Override
	protected void init() {
		super.init();

		mesh = new Mesh("SolarPanel");
		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(6));
		mesh.getMeshData().setTextureBuffer(BufferUtils.createVector2Buffer(6), 0);
		mesh.setModelBound(new OrientedBoundingBox());
		mesh.setUserData(new UserData(this));
		root.attachChild(mesh);

		surround = new Box("SolarPanel (Surround)");
		surround.setModelBound(new OrientedBoundingBox());
		final OffsetState offset = new OffsetState();
		offset.setFactor(1);
		offset.setUnits(1);
		surround.setRenderState(offset);
		root.attachChild(surround);

		outlineMesh = new Line("SolarPanel (Outline)");
		outlineMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(8));
		outlineMesh.setDefaultColor(ColorRGBA.BLACK);
		outlineMesh.setModelBound(new OrientedBoundingBox());
		root.attachChild(outlineMesh);

		updateTextureAndColor();
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		final PickedHousePart picked = pickContainer(x, y, new Class<?>[] { Roof.class, Wall.class });
		if (picked != null) {
			final Vector3 p = picked.getPoint();
			if (container instanceof Wall)
				snapToGrid(p, getAbsPoint(0), getGridSize());
			else
				getTopContainer().snapToGrid(p, getAbsPoint(0), getGridSize());
			points.get(0).set(toRelative(p));
		}
		if (container != null) {
			draw();
			setEditPointsVisible(true);
			setHighlight(!isDrawable());
		}
	}

	@Override
	protected void drawMesh() {
		if (container == null)
			return;

		if (container instanceof Roof) {
			final PickResults pickResults = new PrimitivePickResults();
			final Ray3 ray = new Ray3(getAbsPoint(0).addLocal(0, 0, 1000), Vector3.NEG_UNIT_Z);
			PickingUtil.findPick(container.getRoot(), ray, pickResults);
			if (pickResults.getNumber() != 0) {
				final PickData pickData = pickResults.getPickData(0);
				final Vector3 p = pickData.getIntersectionRecord().getIntersectionPoint(0);
				points.get(0).setZ(p.getZ());
				final UserData userData = (UserData) ((Spatial) pickData.getTarget()).getUserData();
				final int roofPartIndex = userData.getIndex();
				normal = (ReadOnlyVector3) ((Roof) container).getRoofPartsRoot().getChild(roofPartIndex).getUserData();
			}
		} else
			normal = container.getNormal();

		if (normal == null)
			normal = Vector3.UNIT_Z;

		updateEditShapes();

		final double annotationScale = Scene.getInstance().getAnnotationScale();
		surround.setData(Vector3.ZERO, WIDTH / 2.0 / annotationScale, HEIGHT / 2.0 / annotationScale, 0.1);
		surround.updateModelBound();

		final FloatBuffer boxVertexBuffer = surround.getMeshData().getVertexBuffer();
		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		final FloatBuffer textureBuffer = mesh.getMeshData().getTextureBuffer(0);
		final FloatBuffer outlineBuffer = outlineMesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		outlineBuffer.rewind();
		textureBuffer.rewind();
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
		outlineMesh.updateModelBound();

		mesh.setTranslation(getAbsPoint(0));
		if (Util.isEqual(normal, Vector3.UNIT_Z))
			mesh.setRotation(new Matrix3());
		else
			mesh.setRotation(new Matrix3().lookAt(normal, Vector3.UNIT_Z));

		surround.setTranslation(mesh.getTranslation());
		surround.setRotation(mesh.getRotation());

		outlineMesh.setTranslation(mesh.getTranslation());
		outlineMesh.setRotation(mesh.getRotation());
	}

	@Override
	public boolean isDrawable() {
		if (container == null) // FIXME: There is a chance that a solar panel can be left without a container
			return true;
		if (mesh.getWorldBound() == null)
			return true;
		final OrientedBoundingBox bound = (OrientedBoundingBox) mesh.getWorldBound().clone(null);
		bound.setExtent(bound.getExtent().divide(1.1, null));
		for (final HousePart child : container.getChildren()) {
			if (child != this && child instanceof SolarPanel && bound.intersects(child.mesh.getWorldBound())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void updateTextureAndColor() {
		updateTextureAndColor(mesh, null, TextureMode.Full);
	}

	@Override
	protected String getTextureFileName() {
		return "solarpanel.png";
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
		return WIDTH / Scene.getInstance().getAnnotationScale() / 5.0;
	}

	@Override
	protected void computeArea() {
		area = WIDTH * HEIGHT;
	}

	@Override
	protected HousePart getContainerRelative() {
		return container instanceof Wall ? container : getTopContainer();
	}

	@Override
	public void drawHeatFlux() {
		// this method is left empty on purpose -- don't draw heat flux
	}

	public HousePart copy() {
		SolarPanel c = (SolarPanel) super.copy();
		Vector3 d = normal.cross(Vector3.UNIT_Z, null);
		d.normalizeLocal();
		if (Util.isZero(d.length()))
			d.set(1, 0, 0);
		d.multiplyLocal(WIDTH / Scene.getInstance().getAnnotationScale());
		d.addLocal(getContainerRelative().getPoints().get(0));
		Vector3 v = toRelative(d);
		c.points.get(0).setX(points.get(0).getX() + v.getX());
		c.points.get(0).setY(points.get(0).getY() + v.getY());
		c.points.get(0).setZ(points.get(0).getZ() + v.getZ());
		return c;
	}

}
