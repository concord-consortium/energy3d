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

public class Sensor extends HousePart {

	private static final long serialVersionUID = 1L;

	public static final double WIDTH = 0.15;
	public static final double HEIGHT = 0.1;
	private transient ReadOnlyVector3 normal;
	private transient double area;
	private transient Mesh wireframeMesh;
	private transient Box surround;

	public Sensor() {
		super(1, 1, 0.0);
	}

	@Override
	protected void init() {
		super.init();

		mesh = new Mesh("Sensor");
		mesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(6));
		mesh.getMeshData().setTextureBuffer(BufferUtils.createVector2Buffer(6), 0);
		mesh.setModelBound(new OrientedBoundingBox());
		mesh.setUserData(new UserData(this));
		root.attachChild(mesh);

		surround = new Box("Sensor (Surround)");
		surround.setModelBound(new OrientedBoundingBox());
		final OffsetState offset = new OffsetState();
		offset.setFactor(1);
		offset.setUnits(1);
		surround.setRenderState(offset);
		root.attachChild(surround);

		wireframeMesh = new Line("Sensor (Wireframe)");
		wireframeMesh.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(8));
		wireframeMesh.setDefaultColor(ColorRGBA.BLACK);
		wireframeMesh.setModelBound(new OrientedBoundingBox());
		root.attachChild(wireframeMesh);

		updateTextureAndColor();
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		final PickedHousePart picked = pickContainer(x, y, new Class<?>[] { Roof.class, Wall.class, Foundation.class });
		if (picked != null) {
			final Vector3 p = picked.getPoint();
			snapToGrid(p, getAbsPoint(0), getGridSize(), false);
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
			normal = container.getFaceDirection();
		updateEditShapes();

		final double annotationScale = Scene.getInstance().getAnnotationScale();
		area = WIDTH * HEIGHT;
		surround.setData(Vector3.ZERO, WIDTH / 2.0 / annotationScale, HEIGHT / 2.0 / annotationScale, 0.02); // last arg sets close to zero so the sensor doesn't cast shadow
		surround.updateModelBound();

		final FloatBuffer boxVertexBuffer = surround.getMeshData().getVertexBuffer();
		final FloatBuffer vertexBuffer = mesh.getMeshData().getVertexBuffer();
		final FloatBuffer textureBuffer = mesh.getMeshData().getTextureBuffer(0);
		final FloatBuffer wireframeBuffer = wireframeMesh.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();
		wireframeBuffer.rewind();
		textureBuffer.rewind();
		int i = 8 * 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(1).put(0);
		wireframeBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		i += 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(0).put(0);
		wireframeBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		wireframeBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		i += 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(0).put(1);
		textureBuffer.put(0).put(1);
		wireframeBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		wireframeBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		i += 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(1).put(1);
		wireframeBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		wireframeBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		i = 8 * 3;
		vertexBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));
		textureBuffer.put(1).put(0);
		wireframeBuffer.put(boxVertexBuffer.get(i)).put(boxVertexBuffer.get(i + 1)).put(boxVertexBuffer.get(i + 2));

		mesh.updateModelBound();
		wireframeMesh.updateModelBound();

		mesh.setTranslation(getAbsPoint(0));
		if (normal != null) { // FIXME: Sometimes normal is null
			if (Util.isEqual(normal, Vector3.UNIT_Z))
				mesh.setRotation(new Matrix3());
			else
				mesh.setRotation(new Matrix3().lookAt(normal, Vector3.UNIT_Z));
		}

		surround.setTranslation(mesh.getTranslation());
		surround.setRotation(mesh.getRotation());

		wireframeMesh.setTranslation(mesh.getTranslation());
		wireframeMesh.setRotation(mesh.getRotation());
	}

	@Override
	public boolean isDrawable() {
		if (this.mesh.getWorldBound() == null)
			return true;
		final OrientedBoundingBox bound = (OrientedBoundingBox) this.mesh.getWorldBound().clone(null);
		bound.setExtent(bound.getExtent().divide(1.1, null));
		for (final HousePart solarPanel : container.getChildren()) {
			if (solarPanel != this && bound.intersects(solarPanel.mesh.getWorldBound())) {
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
		return "sensor.png";
	}

	@Override
	public ReadOnlyVector3 getFaceDirection() {
		return normal;
	}

	@Override
	public boolean isPrintable() {
		return false;
	}

	@Override
	public double getGridSize() {
		return WIDTH / Scene.getInstance().getAnnotationScale();
	}

	@Override
	public double computeArea() {
		return area;
	}

	@Override
	protected HousePart getContainerRelative() {
		if (container instanceof Wall)
			return container;
		else
			return getTopContainer();
	}

}