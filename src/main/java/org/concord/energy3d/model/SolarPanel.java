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

	public SolarPanel() {
		super(1, 1, 0.0);
	}
	
	@Override
	protected void init() {
		super.init();
		relativeToHorizontal = true;
		
		mesh = new Box("SolarPanel", new Vector3(), 1, 2, 0.1);
		mesh.setModelBound(new OrientedBoundingBox());
		mesh.setUserData(new UserData(this));		
		root.attachChild(mesh);
		
		updateTextureAndColor();
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		final PickedHousePart picked = pickContainer(x, y, Roof.class);
		if (picked != null) {
			final Vector3 p = picked.getPoint();
			container.getContainer().getContainer().snapToGrid(p, getAbsPoint(0), getGridSize());
			points.get(0).set(toRelative(p, container.getContainer().getContainer()));
		}
		if (container != null) {
			draw();
			setEditPointsVisible(true);
		}
	}
	
	@Override
	public Vector3 getAbsPoint(final int index) {
		return toAbsolute(points.get(index), container == null ? null : container.getContainer().getContainer());
	}	

	@Override
	protected void drawMesh() {
		updateEditShapes();
		
		final PickResults pickResults = new PrimitivePickResults();
		final Ray3 ray = new Ray3(getAbsPoint(0).addLocal(0, 0, 1000), Vector3.NEG_UNIT_Z);
		PickingUtil.findPick(container.getRoot(), ray, pickResults);
		
		final PickData pickData = pickResults.getPickData(0);
		final Vector3 p = pickData.getIntersectionRecord().getIntersectionPoint(0);
		mesh.setTranslation(p);
		
		final UserData userData = (UserData) ((Spatial) pickData.getTarget()).getUserData();
		final int roofPartIndex = userData.getIndex();
		final ReadOnlyVector3 normal = (ReadOnlyVector3) ((Roof) container).getRoofPartsRoot().getChild(roofPartIndex).getUserData();
		if (Util.isEqual(normal, Vector3.UNIT_Z))
			mesh.setRotation(new Matrix3());
		else
			mesh.setRotation(new Matrix3().lookAt(normal, Vector3.UNIT_Z));
	}
	
	@Override
	public boolean isDrawable() {
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
	public boolean isPrintable() {
		return false;
	}	

}
