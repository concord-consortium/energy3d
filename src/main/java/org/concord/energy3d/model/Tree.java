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
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.extension.BillboardNode;
import com.ardor3d.scenegraph.extension.BillboardNode.BillboardAlignment;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.PickingHint;
import com.ardor3d.scenegraph.shape.Cone;
import com.ardor3d.scenegraph.shape.Cylinder;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.scenegraph.shape.Sphere;

public class Tree extends HousePart {

	private static final long serialVersionUID = 1L;
	public static final int DOGWOOD = 0;
	public static final int OAK = 1;
	public static final int MAPLE = 2;
	public static final int PINE = 3;
	private transient BillboardNode billboard;
	private transient Node collisionRoot;
	private transient Mesh sphere;
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
		case OAK:
			height = 60;
			break;
		case MAPLE:
			height = 45;
			break;
		case PINE:
			height = 50;
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

		if (treeType == PINE)
			sphere = new Cone("Tree Sphere", 2, 6, 15, 20, false);
		else
			sphere = new Sphere("Tree Sphere", 4, 6, 14);
		sphere.setModelBound(new BoundingSphere());
		sphere.updateModelBound();
		final Cylinder cylinder = new Cylinder("Tree Cylinder", 10, 10, 1, 20);
		cylinder.setModelBound(new BoundingBox());
		cylinder.updateModelBound();

		switch (treeType) {
		case OAK:
			sphere.setScale(1, 1, 1.8);
			sphere.setTranslation(0, 0, 33);
			cylinder.setScale(1, 1, 2);
			cylinder.setTranslation(0, 0, 20);
			break;
		case MAPLE:
			sphere.setScale(1, 1, 1.2);
			sphere.setTranslation(0, 0, 26);
			cylinder.setTranslation(0, 0, 10);
			break;
		case PINE:
			sphere.setScale(1, 1, -2.3);
			sphere.setTranslation(0, 0, 28);
			cylinder.setTranslation(0, 0, 10);
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
		case OAK:
			return isShedded() ? "oak_shedded.png" : "oak.png";
		case MAPLE:
			return isShedded() ? "maple_shedded.png" : "maple.png";
		case PINE:
			return "pine.png";
		default:
			return isShedded() ? "dogwood_shedded.png" : "dogwood.png";
		}
	}

	private boolean isShedded() {
		if (treeType == PINE)
			return false;
		final int month = Heliodon.getInstance().getCalender().get(Calendar.MONTH);
		return month > 10 || month < 4;
	}

	@Override
	public void updateTextureAndColor() {
		updateTextureAndColor(mesh, Scene.WHITE, TextureMode.Full);
	}

	@Override
	public Spatial getCollisionSpatial() {
		if (sphere == null)
			init();
		sphere.removeFromParent();
		if (!isShedded())
			collisionRoot.attachChild(sphere);
		collisionRoot.updateWorldBound(true);
		return collisionRoot;
	}

	@Override
	public Spatial getIrradiationCollisionSpatial() {
		return getCollisionSpatial();
	}

	public int getTreeType() {
		return treeType;
	}

	public String getTreeName() {
		switch (treeType) {
		case OAK:
			return "Oak";
		case MAPLE:
			return "Maple";
		case PINE:
			return "Pine";
		default:
			return "Dogwood";
		}
	}

	@Override
	public void drawHeatFlux() {
		// this method is left empty on purpose -- don't draw heat flux
	}

}
