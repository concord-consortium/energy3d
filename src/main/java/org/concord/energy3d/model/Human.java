package org.concord.energy3d.model;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.util.SelectUtil;

import com.ardor3d.bounding.BoundingBox;
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
import com.ardor3d.scenegraph.shape.Quad;

public class Human extends HousePart {

	private static final long serialVersionUID = 1L;
	public static final int JANE = 0;
	public static final int JENNY = 1;
	public static final int JACK = 2;
	public static final int JOHN = 3;
	private transient BillboardNode billboard;
	private transient Node collisionRoot;
	private final int humanType;

	public Human(final int humanType) {
		super(1, 1, 1);
		this.humanType = humanType;
		init();
	}

	@Override
	protected void init() {
		super.init();

		final double height;
		final double width;
		switch (humanType) {
		case JANE:
			width = 2.5;
			height = 8;
			break;
		case JENNY:
			width = 3;
			height = 9;
			break;
		case JACK:
			width = 2.8;
			height = 9;
			break;
		case JOHN:
			width = 4;
			height = 10;
			break;
		default:
			width = 2.5;
			height = 8;
		}
		mesh = new Quad("Human Quad", width, height);
		mesh.setModelBound(new BoundingBox());
		mesh.updateModelBound();
		mesh.setRotation(new Matrix3().fromAngles(Math.PI / 2, 0, 0));
		mesh.setTranslation(0, width / 2, height / 2 + 1); // foundation height = 1
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

		collisionRoot = new Node("Tree Collision Root");
		if (points.size() > 0)
			collisionRoot.setTranslation(getAbsPoint(0));
		collisionRoot.updateWorldBound(true);
		collisionRoot.getSceneHints().setCullHint(CullHint.Always);
		root.attachChild(collisionRoot);

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
		switch (humanType) {
		case JANE:
			return "jane.png";
		case JENNY:
			return "jenny.png";
		case JACK:
			return "jack.png";
		case JOHN:
			return "john.png";
		default:
			return "jane.png";
		}
	}

	@Override
	public void updateTextureAndColor() {
		updateTextureAndColor(mesh, Scene.getInstance().getWallColor(), TextureMode.Full);
	}

	@Override
	public Spatial getCollisionSpatial() {
		collisionRoot.updateWorldBound(true);
		return collisionRoot;
	}

	public int getHumanType() {
		return humanType;
	}

	public String getHumanName() {
		switch (humanType) {
		case JANE:
			return "Jane";
		case JENNY:
			return "Jenny";
		case JACK:
			return "Jack";
		case JOHN:
			return "John";
		default:
			return "Unknown";
		}
	}

}
