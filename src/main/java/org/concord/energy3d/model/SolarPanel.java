package org.concord.energy3d.model;

import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.OrientedBoundingBox;
import com.ardor3d.intersection.PickData;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.shape.Box;

public class SolarPanel extends HousePart {
	private static final long serialVersionUID = 1L;
	private static final double widthExtent = 0.7;
	private transient ReadOnlyVector3 normal;
	private transient double area;

	public SolarPanel() {
		super(1, 1, 0.0);
	}

	@Override
	protected void init() {
		super.init();
		updateRelativeToHorizontalFlag();

		final double yExtent = 1.6;
		area = widthExtent * yExtent;
		mesh = new Box("SolarPanel", new Vector3(), widthExtent / 2.0 / 0.2, yExtent / 2.0 / 0.2, 0.1);
		mesh.setModelBound(new OrientedBoundingBox());
		mesh.setUserData(new UserData(this));
		root.attachChild(mesh);

		updateTextureAndColor();
	}

	private void updateRelativeToHorizontalFlag() {
		if (container != null)
			relativeToHorizontal = container instanceof Roof;
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		final PickedHousePart picked = pickContainer(x, y, new Class<?>[] { Roof.class, Wall.class });
		updateRelativeToHorizontalFlag();
		if (picked != null) {
			final Vector3 p = picked.getPoint();
			if (container instanceof Wall)
				snapToGrid(p, getAbsPoint(0), getGridSize());
			else
				getTopContainer().snapToGrid(p, getAbsPoint(0), getGridSize());
			points.get(0).set(toRelative(p, getContainerRelative()));
		}
		if (container != null) {
			draw();
			setEditPointsVisible(true);
			setHighlight(!isDrawable());
		}
	}

	@Override
	public Vector3 getAbsPoint(final int index) {
		return toAbsolute(points.get(index), getContainerRelative());
	}

	@Override
	protected void drawMesh() {
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

		mesh.setTranslation(getAbsPoint(0));
		if (Util.isEqual(normal, Vector3.UNIT_Z))
			mesh.setRotation(new Matrix3());
		else
			mesh.setRotation(new Matrix3().lookAt(normal, Vector3.UNIT_Z));
	}

	@Override
	public boolean isDrawable() {
//		final Vector3 p1 = getAbsPoint(0);
//		p1.setZ(0);
		if (this.mesh.getWorldBound() == null)
			return true;
		final OrientedBoundingBox bound = (OrientedBoundingBox) this.mesh.getWorldBound().clone(null);
		bound.setExtent(bound.getExtent().divide(1.1, null));
		for (final HousePart solarPanel : container.getChildren()) {
//			final Vector3 p2 = solarPanel.getAbsPoint(0);
//			p2.setZ(0);
//			if (solarPanel != this && p1.distance(p2) < widthExtent / 1.5 / 0.2) {
			if (solarPanel != this && bound.intersects(solarPanel.mesh.getWorldBound())) {
//				mesh.setDefaultColor(ColorRGBA.RED);
				return false;
			}
		}
//		mesh.setDefaultColor(ColorRGBA.WHITE);
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
	public ReadOnlyVector3 getFaceDirection() {
		return normal;
	}

	@Override
	public boolean isPrintable() {
		return false;
	}

	@Override
	public double getGridSize() {
//		return 1.5;
		return widthExtent / 0.2;
	}

	@Override
	public double computeArea() {
		return area;
	}

	private HousePart getContainerRelative() {
		if (container instanceof Wall)
			return container;
		else
			return getTopContainer();
	}

}
