package org.concord.energy3d.model;

import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.Scene.TextureMode;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.SelectUtil;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.extension.model.collada.jdom.ColladaAnimUtils;
import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.ColladaMaterialUtils;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
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
import com.ardor3d.scenegraph.shape.Cylinder;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.scenegraph.shape.Sphere;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.ResourceSource;

public class Tree extends HousePart {
	private static final long serialVersionUID = 1L;
	private static Spatial treeModel;
	private static boolean isBillboard = true;
	private transient Spatial model;
	private transient BillboardNode billboard;
	private transient Node collisionRoot;
	private transient Sphere sphere;

	public static void loadModel() {
		new Thread() {
			@Override
			public void run() {
				System.out.print("Loading tree model...");
				Thread.yield();
				if (isBillboard) {
				} else {
					final ResourceSource source = ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_MODEL, "tree3.dae");
					final ColladaImporter colladaImporter = new ColladaImporter();
					Logger.getLogger(ColladaAnimUtils.class.getName()).setLevel(Level.SEVERE);
					Logger.getLogger(ColladaMaterialUtils.class.getName()).setLevel(Level.SEVERE);
					ColladaStorage storage;
					try {
						storage = colladaImporter.load(source);
						treeModel = storage.getScene();
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
				System.out.println("done");
			}
		}.start();
	}

	public Tree() {
		super(1, 1, 1);
		init();
	}

	@Override
	protected void init() {
		super.init();
		relativeToHorizontal = true;

		if (!isBillboard) {
			model = treeModel.makeCopy(true);
			root.attachChild(model);
		} else {
			mesh = new Quad("Tree Quad", 30, 30);
			mesh.setModelBound(new BoundingBox());
			mesh.updateModelBound();
			mesh.setRotation(new Matrix3().fromAngles(Math.PI / 2, 0, 0));
			mesh.setTranslation(0, 0, 15);

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
			sphere.setScale(1, 1, 0.7);
			sphere.setTranslation(0, 0, 19);
			sphere.setModelBound(new BoundingSphere());
			sphere.updateModelBound();
			final Cylinder cylinder = new Cylinder("Tree Cylinder", 10, 10, 1, 20);
			cylinder.setTranslation(0, 0, 10);
			cylinder.setModelBound(new BoundingBox());
			cylinder.updateModelBound();

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
		}
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
		if (isBillboard) {
			billboard.setTranslation(getAbsPoint(0));
			collisionRoot.setTranslation(getAbsPoint(0));
			final double scale = 1 / (Scene.getInstance().getAnnotationScale() / 0.2);
			billboard.setScale(scale);
			collisionRoot.setScale(scale);
		} else
			model.setTranslation(getAbsPoint(0));
	}

	@Override
	protected String getTextureFileName() {
		if (isShedded())
			return "tree_shedded.png";
		else
			return "tree.png";
	}

	private boolean isShedded() {
		final int month = Heliodon.getInstance().getCalander().get(Calendar.MONTH);
		return month > 10 || month < 4;
	}

	@Override
	public void updateTextureAndColor() {
		if (isBillboard)
			updateTextureAndColor(mesh, Scene.getInstance().getWallColor(), TextureMode.Full);
	}

	public Node getCollisionRoot() {
		sphere.removeFromParent();
		if (!isShedded())
			collisionRoot.attachChild(sphere);
		collisionRoot.updateWorldBound(true);
		return collisionRoot;
	}

}
