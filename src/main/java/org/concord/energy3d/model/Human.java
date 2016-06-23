package org.concord.energy3d.model;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.util.SelectUtil;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.BlendState.TestFunction;
import com.ardor3d.scenegraph.extension.BillboardNode;
import com.ardor3d.scenegraph.extension.BillboardNode.BillboardAlignment;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Quad;

public class Human extends HousePart {

	private static final long serialVersionUID = 1L;
	public static final int JANE = 0;
	public static final int JENI = 1;
	public static final int JACK = 2;
	public static final int JOHN = 3;
	public static final int JILL = 4;
	public static final int JOSE = 5;
	private transient BillboardNode billboard;
	private final int humanType;

	public Human(final int humanType) {
		super(1, 1, 1);
		this.humanType = humanType;
		init();
		root.getSceneHints().setCullHint(CullHint.Always);
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
		case JENI:
			width = 3;
			height = 9;
			break;
		case JILL:
			width = 3;
			height = 8;
			break;
		case JACK:
			width = 2.8;
			height = 9;
			break;
		case JOHN:
			width = 4;
			height = 10;
			break;
		case JOSE:
			width = 8;
			height = 8;
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
		mesh.setUserData(new UserData(this, 0, true));

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

		updateTextureAndColor();
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
		final int index = 0;
		final PickedHousePart pick = SelectUtil.pickPart(x, y, new Class<?>[] { Foundation.class, null });
		if (pick != null) {
			final Vector3 p = pick.getPoint().clone();
			snapToGrid(p, getAbsPoint(index), getGridSize());
			points.get(index).set(toRelative(p));
			root.getSceneHints().setCullHint(CullHint.Never);
		}
		draw();
		setEditPointsVisible(true);
	}

	@Override
	public double getGridSize() {
		return SceneManager.getInstance().isFineGrid() ? 1 : 5;
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
		final double scale = 1 / (Scene.getInstance().getAnnotationScale() / 0.2);
		billboard.setScale(scale);
	}

	@Override
	protected String getTextureFileName() {
		switch (humanType) {
		case JANE:
			return "jane.png";
		case JENI:
			return "jenny.png";
		case JILL:
			return "jill.png";
		case JACK:
			return "jack.png";
		case JOHN:
			return "john.png";
		case JOSE:
			return "jose.png";
		default:
			return "jane.png";
		}
	}

	@Override
	public void updateTextureAndColor() {
		updateTextureAndColor(mesh, Scene.WHITE, TextureMode.Full);
	}

	public int getHumanType() {
		return humanType;
	}

	public String getHumanName() {
		switch (humanType) {
		case JANE:
			return "Jane";
		case JENI:
			return "Jeni";
		case JILL:
			return "Jill";
		case JACK:
			return "Jack";
		case JOHN:
			return "John";
		case JOSE:
			return "Jose";
		default:
			return "Unknown";
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

	public boolean isCopyable() {
		return true;
	}

	public HousePart copy(boolean check) {
		Human c = (Human) super.copy(false);
		c.points.get(0).setX(points.get(0).getX() + 2); // shift the position of the copy
		return c;
	}

}
