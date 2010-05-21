package org.concord.energy3d.model;

import java.util.ArrayList;

import org.concord.energy3d.scene.SelectUtil;

import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.TextureManager;

public class Foundation extends HousePart {
	private static final long serialVersionUID = 1L;
	private static final double GRID_SIZE = 0.5;
	private transient boolean resizeHouseMode = false;
	// private double height = 0.1;
	private transient Box mesh; // = new Box("Foundation", new Vector3(), new Vector3());
	private transient Box boundingMesh;
	private double boundingHeight;
	private transient double newBoundingHeight;

	public Foundation() {
		super(2, 8, 0.1);
//		height = orgHeight = 0.1;
	}

	protected void init() {
		super.init();
		resizeHouseMode = false;
		if (boundingHeight == 0)
			boundingHeight = 2;
//		newBoundingHeight = boundingHeight;
//		newBoundingHeight = -1;
		mesh = new Box("Foundation", new Vector3(), new Vector3());
		boundingMesh = new Box("Foundation (Bounding)", new Vector3(), new Vector3());
		root.attachChild(mesh);
		// root.attachChild(boundingMesh);
		// allocateNewPoint();

		// Add a texture to the box.
		final TextureState ts = new TextureState();
		ts.setTexture(TextureManager.load("concrete.jpg", Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
		mesh.setRenderState(ts);

		WireframeState wire = new WireframeState();
		boundingMesh.setRenderState(wire);

		UserData userData = new UserData(this);
		mesh.setUserData(userData);
		boundingMesh.setUserData(userData);
	}

	public void setResizeHouseMode(boolean resizeHouseMode) {
		this.resizeHouseMode = resizeHouseMode;
		if (resizeHouseMode) {
			adjustBoundingHeight();
			root.attachChild(boundingMesh);
		} else
			root.detachChild(boundingMesh);		
	}
	
	public boolean isResizeHouseMode() {
		return resizeHouseMode;
	}
	
	// @Override
	// public void addPoint(int x, int y) {
	// if (drawCompleted)
	// return;
	// // throw new RuntimeException("Drawing of this object is already completed");
	//
	// if (points.size() >= numOfEditPoints)
	// drawCompleted = true;
	// else {
	// allocateNewPoint();
	// setPreviewPoint(x, y);
	// }
	// }

	// protected void allocateNewPoint() {
	// Vector3 p = new Vector3();
	// points.add(p);
	// points.add(p);
	// }

	@Override
	public void showPoints() {
		for (int i = 0; i < points.size(); i++) {
			if (!resizeHouseMode && i >= 4)
				pointsRoot.getChild(i).getSceneHints().setCullHint(CullHint.Always);
			else
				pointsRoot.getChild(i).getSceneHints().setCullHint(CullHint.Inherit);
		}
	}	
	
	@Override
	public void addChild(HousePart housePart) {
		super.addChild(housePart);		
//		applyNewHeight(boundingHeight, newBoundingHeight, true);		
	}
	
	public void complete() {
		super.complete();
		applyNewHeight(boundingHeight, newBoundingHeight, true);
	}	

	@Override
	public void setPreviewPoint(int x, int y) {
		int index = editPointIndex;
		if (index == -1) {
			if (isFirstPointInserted())
				index = 3;
			else
				index = 0;
		}
		PickedHousePart pick = SelectUtil.pickPart(x, y, (Spatial)null);
		Vector3 p = points.get(index);
		if (pick != null) {
			p = pick.getPoint();
			p = grid(p, GRID_SIZE);
		}
		points.get(index).set(p);
		if (!isFirstPointInserted()) {
			points.get(1).set(p);
			points.get(2).set(p);
		} else {
			if (index < 4) {
				if (index == 0 || index == 3) {
					points.get(1).set(points.get(0).getX(), points.get(3).getY(), 0);
					points.get(2).set(points.get(3).getX(), points.get(0).getY(), 0);
				} else {
					points.get(0).set(points.get(1).getX(), points.get(2).getY(), 0);
					points.get(3).set(points.get(2).getX(), points.get(1).getY(), 0);
				}
			} else {
				int lower = (editPointIndex == 1) ? 0 : 2;
				System.out.println("x,y = " + x + "," + y);
				Vector3 base = abspoints.get(lower);
				System.out.println("base = " + base);
				Vector3 closestPoint = closestPoint(base, base.add(0, 0, 1, null), x, y);
				System.out.println("closest = " + closestPoint);
//				Snap snap = snap(closestPoint, -1);
//				if (snap == null)
					closestPoint = grid(closestPoint, GRID_SIZE);
				newBoundingHeight = findHeight(base, closestPoint);
				applyNewHeight(boundingHeight, newBoundingHeight, false);
//				boundingHeight = newHeight;
			}
			for (int i = 0; i < 4; i++)
				points.get(i + 4).set(points.get(i)).setZ(newBoundingHeight);
		}
		
//		if (editPointIndex != -1)
//			for (HousePart child : children)
//				child.recalculateRelativePoints();
		
		draw();
		showPoints();

		for (HousePart child : children)
			child.draw();

	}

	private void applyNewHeight(double oldHeight, double newHeight, boolean finalize) {
		System.out.println("newH = " + newHeight + ", oldH = " + oldHeight);
		double scale = newHeight / oldHeight;
		System.out.println("fac = " + scale);
		
//		ArrayList<HousePart> children = this.children;
		applyNewHeight(children, scale, finalize);
		if (finalize)
			boundingHeight = newHeight;
	}

	private void applyNewHeight(ArrayList<HousePart> children, double scale, boolean finalize) {
		for (HousePart child : children) {
			if (child instanceof Wall || child instanceof Floor || child instanceof Roof) {
				child.setHeight(child.getHeight() * scale, finalize);	
				applyNewHeight(child.getChildren(), scale, finalize);
			}
		}
	}

	@Override
	public void draw() {
//		if (resizeHouseMode) {
//			adjustBoundingHeight();
//			root.attachChild(boundingMesh);
//		} else
//			root.detachChild(boundingMesh);
		super.draw();
		boolean drawable = points.size() == 8;


		// for (int i = 0; i < points.size(); i++) {
		// Vector3 p = points.get(i);
		// // update location of point spheres
		// pointsRoot.getChild(i).setTranslation(p);
		// }

		if (drawable) {
			mesh.setData(points.get(0), points.get(3).add(0, 0, height, null));
			mesh.updateModelBound();
			boundingMesh.setData(points.get(0), points.get(7));
			boundingMesh.updateModelBound();
		}
	}
	
	private void adjustBoundingHeight() {
		boundingHeight = 0;
		for (HousePart child : children) {
			for (Vector3 p : child.getPoints()) {
				boundingHeight = Math.max(boundingHeight, p.getZ());
			}
		}
		boundingHeight += 0.5;
		for (int i = 4; i < 8; i++)
			points.get(i).setZ(boundingHeight);
//		if (newBoundingHeight == -1)
			newBoundingHeight = boundingHeight;
	}
	
	protected void flatten() {		
		root.setRotation((new Matrix3().fromAngles(flattenTime * Math.PI / 2, 0, 0)));
//		root.setTranslation(pos, 0, 0);
//		root.setTranslation(flattenTime * 5*(int) (pos / 3), 0, flattenTime * 3*(2 + pos % 3));
//		root.setTranslation(flattenTime * printX, 0, flattenTime * printY);
		super.flatten();
	}	

}
