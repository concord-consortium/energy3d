package org.concord.energy3d.model;

import org.concord.energy3d.scene.SceneManager;

import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.TextureManager;

public class Foundation extends HousePart {
	private static final long serialVersionUID = 1L;
	private static final double GRID_SIZE = 0.5;
	private static boolean resizeHouseMode = false;
	// private double height = 0.1;
	private transient Box mesh; // = new Box("Foundation", new Vector3(), new Vector3());
	private transient Box boundingMesh;
	private double boundingHeight = 2;

	public static void setResizeHouseMode(boolean resizeHouseMode) {
		Foundation.resizeHouseMode = resizeHouseMode;
	}

	public static boolean isResizeHouseMode() {
		return resizeHouseMode;
	}

	public Foundation() {
		super(2, 8);
		height = 0.1;
	}

	protected void init() {
		super.init();
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
	public void setPreviewPoint(int x, int y) {
		int index = editPointIndex;
		if (index == -1) {
			if (isFirstPointInserted())
				index = 3;
			else
				index = 0;
		}
		PickedHousePart pick = SceneManager.getInstance().findMousePoint(x, y);
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
				Vector3 base = points.get(lower);
				Vector3 closestPoint = closestPoint(base, base.add(0, 0, 1, null), x, y);
				Snap snap = snap(closestPoint, -1);
				if (snap == null)
					closestPoint = grid(closestPoint, GRID_SIZE);
				// neighbor[1] = snap(closestPoint);
				double newHeight = findHeight(base, closestPoint);
				applyNewHeight(boundingHeight, newHeight);
				boundingHeight = newHeight;
			}
			for (int i = 0; i < 4; i++)
				points.get(i + 4).set(points.get(i)).setZ(boundingHeight);
		}
		draw();
		showPoints();

		for (HousePart child : children)
			child.draw();

	}

	private void applyNewHeight(double boundingHeight2, double newHeight) {
		for (HousePart child : children) {
			if (child instanceof Wall) {
				Wall wall = (Wall) child;
				wall.setHeight(newHeight);
			}
		}

	}

	@Override
	public void draw() {
		super.draw();
		boolean drawable = points.size() == 8;

		if (resizeHouseMode)
			root.attachChild(boundingMesh);
		else
			root.detachChild(boundingMesh);

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

}
