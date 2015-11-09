package org.concord.energy3d.model;

import java.util.ArrayList;
import java.util.Calendar;

import org.concord.energy3d.gui.EnergyPanel;
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
	public static final int ELM = 1;
	public static final int MAPLE = 2;
	public static final int PINE = 3;
	public static final int OAK = 4;
	public static final int LINDEN = 5;
	private transient BillboardNode billboard;
	private transient Node collisionRoot;
	private transient Mesh crown;
	private final int treeType;
	private boolean showPolygons;

	public Tree(final int treeType) {
		super(1, 1, 1);
		this.treeType = treeType;
		init();
		root.getSceneHints().setCullHint(CullHint.Always);
	}

	@Override
	protected void init() {
		super.init();

		final double width, height;
		switch (treeType) {
		case LINDEN:
			width = 80;
			height = 100;
			break;
		case OAK:
			width = 70;
			height = 80;
			break;
		case ELM:
			width = 60;
			height = 75;
			break;
		case MAPLE:
			width = 30;
			height = 60;
			break;
		case PINE:
			width = 30;
			height = 80;
			break;
		default:
			width = 30;
			height = 40;
		}
		mesh = new Quad("Tree Quad", width, height);
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

		switch (treeType) {
		case PINE:
			crown = new Cone("Tree Crown", 2, 6, 18, 20, false); // axis samples, radial samples, radius, height, closed
			break;
		default:
			crown = new Sphere("Tree Crown", 4, 8, 14); // z samples, radial samples, radius
		}
		crown.setModelBound(new BoundingSphere());
		crown.updateModelBound();
		final Cylinder trunk = new Cylinder("Tree Trunk", 10, 10, 1, 20);
		trunk.setModelBound(new BoundingBox());
		trunk.updateModelBound();

		switch (treeType) {
		case LINDEN:
			crown.setScale(3, 3, 3.2);
			crown.setTranslation(0, 0, 55);
			trunk.setScale(5, 5, 2);
			trunk.setTranslation(0, 0, 20);
			break;
		case OAK:
			crown.setScale(2.5, 2.5, 3);
			crown.setTranslation(0, 0, 45);
			trunk.setScale(5, 5, 2);
			trunk.setTranslation(0, 0, 20);
			break;
		case ELM:
			crown.setScale(2, 2, 2.5);
			crown.setTranslation(0, 0, 40);
			trunk.setScale(2, 2, 2);
			trunk.setTranslation(0, 0, 20);
			break;
		case MAPLE:
			crown.setScale(1, 1, 2.1);
			crown.setTranslation(0, 0, 32);
			trunk.setTranslation(0, 0, 10);
			break;
		case PINE:
			crown.setScale(1, 1, -4.0);
			crown.setTranslation(0, 0, 45);
			trunk.setTranslation(0, 0, 10);
			break;
		default:
			crown.setScale(1, 1, 1.2);
			crown.setTranslation(0, 0, 24);
			trunk.setTranslation(0, 0, 10);
			break;
		}

		collisionRoot = new Node("Tree Collision Root");
		collisionRoot.attachChild(crown);
		collisionRoot.attachChild(trunk);
		if (points.size() > 0)
			collisionRoot.setTranslation(getAbsPoint(0));
		collisionRoot.updateWorldTransform(true);
		collisionRoot.updateWorldBound(true);
		collisionRoot.getSceneHints().setCullHint(showPolygons ? CullHint.Never : CullHint.Always);
		root.attachChild(collisionRoot);

		crown.setUserData(new UserData(this));
		trunk.setUserData(new UserData(this));

		updateTextureAndColor();

	}

	public void setShowPolygons(boolean showPolygons) {
		this.showPolygons = showPolygons;
		collisionRoot.getSceneHints().setCullHint(showPolygons ? CullHint.Never : CullHint.Always);
	}

	public boolean getShowPolygons() {
		return showPolygons;
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		final int index = 0;
		final PickedHousePart pick = SelectUtil.pickPart(x, y, new Class<?>[] { Foundation.class, null });
		if (pick != null) {
			final Vector3 p = pick.getPoint();
			snapToGrid(p, getAbsPoint(index), getGridSize());
			points.get(index).set(toRelative(p));
			root.getSceneHints().setCullHint(CullHint.Never);
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
		case LINDEN:
			return isShedded() ? "linden_shedded.png" : "linden.png";
		case OAK:
			return isShedded() ? "oak_shedded.png" : "oak.png";
		case ELM:
			return isShedded() ? "elm_shedded.png" : "elm.png";
		case MAPLE:
			return isShedded() ? "maple_shedded.png" : "maple.png";
		case DOGWOOD:
			return isShedded() ? "dogwood_shedded.png" : "dogwood.png";
		default:
			return "pine.png";
		}
	}

	private boolean isShedded() {
		if (treeType == PINE)
			return false;
		final int month = Heliodon.getInstance().getCalender().get(Calendar.MONTH);
		final boolean northHemisphereWinter = month > 10 || month < 4;
		final boolean southHemisphereWinter = month > 4 && month < 10;
		return EnergyPanel.getInstance().getLatitude() > 0 ? northHemisphereWinter : southHemisphereWinter;
	}

	@Override
	public void updateTextureAndColor() {
		updateTextureAndColor(mesh, Scene.WHITE, TextureMode.Full);
	}

	@Override
	public Spatial getCollisionSpatial() {
		if (showPolygons)
			return getRadiationCollisionSpatial();
		else {
			crown.removeFromParent();
			collisionRoot.updateWorldBound(true);
			return collisionRoot;
		}
	}

	@Override
	public Spatial getRadiationCollisionSpatial() {
		if (isShedded())
			crown.removeFromParent();
		else
			collisionRoot.attachChild(crown);
		collisionRoot.updateWorldTransform(true);
		collisionRoot.updateWorldBound(true);
		return collisionRoot;
	}

	public int getTreeType() {
		return treeType;
	}

	public String getTreeName() {
		switch (treeType) {
		case LINDEN:
			return "Linden";
		case OAK:
			return "Oak";
		case ELM:
			return "Elm";
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

	@Override
	protected void computeArea() {
		area = 0.0;
	}

	public void move(Vector3 d, ArrayList<Vector3> houseMovePoints) {
		final Vector3 newP = houseMovePoints.get(0).add(d, null);
		points.set(0, newP);
		draw();
	}

	public boolean isCopyable() {
		return true;
	}

	public HousePart copy(boolean check) {
		Tree c = (Tree) super.copy(false);
		c.points.get(0).setX(points.get(0).getX() + 10); // shift the position of the copy
		return c;
	}

}
