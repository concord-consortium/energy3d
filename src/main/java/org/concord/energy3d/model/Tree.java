package org.concord.energy3d.model;

import java.util.Calendar;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.SelectUtil;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.BlendState.TestFunction;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.extension.BillboardNode;
import com.ardor3d.scenegraph.extension.BillboardNode.BillboardAlignment;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.scenegraph.shape.Cylinder;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.scenegraph.shape.Sphere;

public class Tree extends HousePart {

	private static final long serialVersionUID = 1L;
	public static final int TYPE1 = 0;
	public static final int TYPE2 = 1;
	public static final int TYPE3 = 2;
	private transient BillboardNode billboard;
	private transient Node collisionRoot;
	private transient Sphere sphere;
	private final int treeType;

	public Tree(final int treeType) {
		super(1, 1, 1);
		this.treeType = treeType;
		init();
	}

	@Override
	protected void init() {
		super.init();

		final double height;
		switch (treeType) {
		case TYPE2:
			height = 60;
			break;
		case TYPE3:
			height = 45;
			break;
		default:
			height = 30;
		}
		mesh = new Quad("Tree Quad", 30, height);
		mesh.setModelBound(new BoundingBox());
		mesh.updateModelBound();
		mesh.setRotation(new Matrix3().fromAngles(Math.PI / 2, 0, 0));
		mesh.setTranslation(0, 0, height / 2.0);
		mesh.getSceneHints().setPickingHint(PickingHint.Pickable, false);

		final BlendState bs = new BlendState();
		bs.setEnabled(true);
		bs.setBlendEnabled(false);
		bs.setTestEnabled(true);
		bs.setTestFunction(TestFunction.GreaterThan);
		bs.setReference(0.7f);
		mesh.setRenderState(bs);
		mesh.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);

		billboard = new BillboardNode("Billboard");
		billboard.setAlignment(BillboardAlignment.AxialZ);
		billboard.attachChild(mesh);
		root.attachChild(billboard);

		sphere = new Sphere("Tree Sphere", 10, 10, 14);
		sphere.setModelBound(new BoundingSphere());
		sphere.updateModelBound();
		final Cylinder cylinder = new Cylinder("Tree Cylinder", 10, 10, 1, 20);
		cylinder.setModelBound(new BoundingBox());
		cylinder.updateModelBound();

		switch (treeType) {
		case TYPE2:
			sphere.setScale(1, 1, 1.8);
			sphere.setTranslation(0, 0, 33);
			cylinder.setScale(1, 1, 2);
			cylinder.setTranslation(0, 0, 20);
			break;
		case TYPE3:
			sphere.setScale(1, 1, 1.2);
			sphere.setTranslation(0, 0, 20);
			cylinder.setTranslation(0, 0, 15);
			break;
		default:
			sphere.setScale(1, 1, 0.7);
			sphere.setTranslation(0, 0, 19);
			cylinder.setTranslation(0, 0, 10);
			break;
		}

		collisionRoot = new Node("Tree Collision Root");
		collisionRoot.attachChild(sphere);
		collisionRoot.attachChild(cylinder);
		if (points.size() > 0)
			collisionRoot.setTranslation(getAbsPoint(0));
		collisionRoot.updateWorldBound(true);
		collisionRoot.getSceneHints().setCullHint(CullHint.Always);
		root.attachChild(collisionRoot);

		sphere.setUserData(new UserData(this));
		cylinder.setUserData(new UserData(this, 0, true));

		updateTextureAndColor();

	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		final int index = 0;
		final PickedHousePart pick = SelectUtil.pickPart(x, y, new Class<?>[] { Foundation.class, null });
		if (pick != null) {
			final Vector3 p = pick.getPoint();
			snapToGrid(p, getAbsPoint(index), getGridSize());
			points.get(index).set(toRelative(p));
		}
		draw();
		setEditPointsVisible(true);
	}

	@Override
	public double getGridSize() {
		return 5.0;
	}

	@Override
	protected boolean mustHaveContainer() {
		return false;
	}

	@Override
	public boolean isPrintable() {
		return false;
	}

	@Override
	public boolean isDrawable() {
		return true;
	}

	@Override
	protected void drawMesh() {
		billboard.setTranslation(getAbsPoint(0));
		collisionRoot.setTranslation(getAbsPoint(0));
		final double scale = 1 / (Scene.getInstance().getAnnotationScale() / 0.2);
		billboard.setScale(scale);
		collisionRoot.setScale(scale);
	}

	@Override
	protected String getTextureFileName() {
		switch (treeType) {
		case TYPE2:
			return isShedded() ? "tree2_shedded.png" : "tree2.png";
		case TYPE3:
			return isShedded() ? "tree3_shedded.png" : "tree3.png";
		default:
			return isShedded() ? "tree1_shedded.png" : "tree1.png";
		}
	}

	private boolean isShedded() {
		final int month = Heliodon.getInstance().getCalender().get(Calendar.MONTH);
		return month > 10 || month < 4;
	}

	@Override
	public void updateTextureAndColor() {
		updateTextureAndColor(mesh, Scene.getInstance().getWallColor(), TextureMode.Full);
	}

	@Override
	public Spatial getIrradiationCollisionSpatial() {
		sphere.removeFromParent();
		if (!isShedded())
			collisionRoot.attachChild(sphere);
		collisionRoot.updateWorldBound(true);
		return collisionRoot;
	}

}
