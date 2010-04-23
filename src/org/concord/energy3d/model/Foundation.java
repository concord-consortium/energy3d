package org.concord.energy3d.model;

import org.concord.energy3d.scene.SceneManager;

import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.TextureManager;

public class Foundation extends HousePart {
	private static final long serialVersionUID = 1L;
	private static final double GRID_SIZE = 0.5;
//	private double height = 0.1;
	private transient Box mesh; // = new Box("Foundation", new Vector3(), new Vector3());

	public Foundation() {
		super(2, 4);
		height = 0.1;
	}
	protected void init() {
		super.init();
		mesh = new Box("Foundation", new Vector3(), new Vector3());
		root.attachChild(mesh);
//		allocateNewPoint();
		
		// Add a texture to the box.
		final TextureState ts = new TextureState();
		ts.setTexture(TextureManager.load("concrete.jpg", Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
		mesh.setRenderState(ts);		
		
		UserData userData = new UserData(this);
		mesh.setUserData(userData);			
	}
//	@Override
//	public void addPoint(int x, int y) {
//		if (drawCompleted)
//			return;
////			throw new RuntimeException("Drawing of this object is already completed");
//
//		if (points.size() >= numOfEditPoints)
//			drawCompleted = true;
//		else {
//			allocateNewPoint();
//			setPreviewPoint(x, y);
//		}		
//	}

//	protected void allocateNewPoint() {
//		Vector3 p = new Vector3();
//		points.add(p);
//		points.add(p);
//	}
	
	@Override
	public void setPreviewPoint(int x, int y) {
		PickedHousePart pick = SceneManager.getInstance().findMousePoint(x, y);
		if (pick != null) {
			final double H = 0;
			Vector3 p = pick.getPoint().addLocal(0, 0, H);
			p = grid(p, GRID_SIZE);
//			int index = (editPointIndex == -1) ? points.size() - 2 : editPointIndex;
			int index = editPointIndex;
			if (index == -1) {
				if (isFirstPointInserted())
					index = 3;
				else
					index = 0;
			}
			points.get(index).set(p);
			if (points.size() == 4) {
				if (index == 0 || index == 3) {
					points.get(1).set(points.get(0).getX(), points.get(3).getY(), H);
					points.get(2).set(points.get(3).getX(), points.get(0).getY(), H);
				} else {
					points.get(0).set(points.get(1).getX(), points.get(2).getY(), H);
					points.get(3).set(points.get(2).getX(), points.get(1).getY(), H);					
				}
			} else {
//				points.get(index+1).set(p);
				points.get(1).set(p);
			}
		}
		draw();
		showPoints();
		
		for (HousePart child : children)
			child.draw();
		
	}

	@Override
	protected void draw() {
		super.draw();
		boolean drawable = points.size() == 4;

//		for (int i = 0; i < points.size(); i++) {
//			Vector3 p = points.get(i);
//			// update location of point spheres
//			pointsRoot.getChild(i).setTranslation(p);
//		}

		if (drawable) {
			mesh.setData(points.get(0), points.get(3).add(0, 0, height, null));
			mesh.updateModelBound();			
		}
	}

}
