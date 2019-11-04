package org.concord.energy3d.model;

import java.util.ArrayList;
import java.util.Calendar;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.SelectUtil;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingSphere;
import com.ardor3d.math.ColorRGBA;
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

    public final static Plant[] PLANTS = new Plant[]{
            new Plant("Dogwood", false, 6.0, 8.0, 500), //
            new Plant("Elm", false, 12, 15, 2000), // 1
            new Plant("Maple", false, 6, 12, 1000), // 2
            new Plant("Pine", true, 6, 16, 1500), // 3
            new Plant("Oak", false, 14, 16, 2000), // 4
            new Plant("Linden", false, 18, 24, 3000), // 5
            new Plant("Cottonwood", false, 16, 20, 2500)};

    private static final long serialVersionUID = 1L;
    private double treeWidth, treeHeight;
    private transient BillboardNode billboard;
    private transient Node collisionRoot;
    private transient Mesh crown;
    private transient Cylinder trunk;
    private int treeType = 0;
    private boolean showPolygons;
    private static Calendar leaf_shed_northern_hemisphere, leaf_grow_northern_hemisphere;
    private static Calendar leaf_shed_southern_hemisphere, leaf_grow_southern_hemisphere;

    static {
        leaf_grow_northern_hemisphere = (Calendar) Calendar.getInstance().clone();
        leaf_grow_northern_hemisphere.set(Calendar.MONTH, Calendar.APRIL);
        leaf_grow_northern_hemisphere.set(Calendar.DAY_OF_MONTH, 15);
        leaf_shed_northern_hemisphere = (Calendar) Calendar.getInstance().clone();
        leaf_shed_northern_hemisphere.set(Calendar.MONTH, Calendar.NOVEMBER);
        leaf_shed_northern_hemisphere.set(Calendar.DAY_OF_MONTH, 15);

        leaf_grow_southern_hemisphere = (Calendar) Calendar.getInstance().clone();
        leaf_grow_southern_hemisphere.set(Calendar.MONTH, Calendar.OCTOBER);
        leaf_grow_southern_hemisphere.set(Calendar.DAY_OF_MONTH, 1);
        leaf_shed_southern_hemisphere = (Calendar) Calendar.getInstance().clone();
        leaf_shed_southern_hemisphere.set(Calendar.MONTH, Calendar.MAY);
        leaf_shed_southern_hemisphere.set(Calendar.DAY_OF_MONTH, 1);
    }

    public Tree() {
        super(1, 1, 1);
        init();
        root.getSceneHints().setCullHint(CullHint.Always);
    }

    @Override
    protected void init() {
        super.init();

        treeWidth = PLANTS[treeType].getWidth() / Scene.getInstance().getScale();
        treeHeight = PLANTS[treeType].getHeight() / Scene.getInstance().getScale();

        mesh = new Quad("Tree Quad", treeWidth, treeHeight);
        mesh.setModelBound(new BoundingBox());
        mesh.updateModelBound();
        mesh.setRotation(new Matrix3().fromAngles(0.5 * Math.PI, 0, 0));
        mesh.setTranslation(0, 0, 0.5 * treeHeight);
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

        if (PLANTS[treeType].getName().equals("Pine")) {
            crown = new Cone("Tree Crown", 2, 6, 18, 20, false); // axis samples, radial samples, radius, height, closed
        } else {
            crown = new Sphere("Tree Crown", 4, 8, 14); // z samples, radial samples, radius
        }
        crown.setModelBound(new BoundingSphere());
        crown.updateModelBound();
        trunk = new Cylinder("Tree Trunk", 10, 10, 1, 20);
        trunk.setModelBound(new BoundingBox());
        trunk.updateModelBound();
        setPlantGeometry();

        collisionRoot = new Node("Tree Collision Root");
        collisionRoot.attachChild(crown);
        collisionRoot.attachChild(trunk);
        if (points.size() > 0) {
            collisionRoot.setTranslation(getAbsPoint(0));
        }
        collisionRoot.updateWorldTransform(true);
        collisionRoot.updateWorldBound(true);
        collisionRoot.getSceneHints().setCullHint(showPolygons ? CullHint.Never : CullHint.Always);
        root.attachChild(collisionRoot);

        crown.setUserData(new UserData(this));
        trunk.setUserData(new UserData(this));

        updateTextureAndColor();

    }

    @Override
    public double getHeight() {
        return treeHeight;
    }

    public double getWidth() {
        return treeWidth;
    }

    public void setShowPolygons(final boolean showPolygons) {
        this.showPolygons = showPolygons;
        collisionRoot.getSceneHints().setCullHint(showPolygons ? CullHint.Never : CullHint.Always);
    }

    public boolean getShowPolygons() {
        return showPolygons;
    }

    @Override
    public void setPreviewPoint(final int x, final int y) {
        if (lockEdit) {
            return;
        }
        final PickedHousePart pick = SelectUtil.pickPart(x, y, new Class<?>[]{Foundation.class, Roof.class, Floor.class, null}, true);
        if (pick != null) {
            final Vector3 p = pick.getPoint().clone();
            snapToGrid(p, getAbsPoint(0), getGridSize(), false);
            points.get(0).set(toRelative(p));
            root.getSceneHints().setCullHint(CullHint.Never);
        }
        draw();
        setEditPointsVisible(true);
    }

    @Override
    public double getGridSize() {
        return SceneManager.getInstance().isFineGrid() ? 0.2 : 1;
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
        final double scale = 0.2 / Scene.getInstance().getScale();
        billboard.setScale(scale);
        collisionRoot.setScale(scale);
    }

    @Override
    protected String getTextureFileName() {
        return PLANTS[treeType].getName().toLowerCase() + (isShedded() ? "_shedded.png" : ".png");
    }

    public String getPlantName() {
        return getPlantName(treeType);
    }

    public static String getPlantName(final int which) {
        return PLANTS[which].getName();
    }

    private boolean isShedded() {
        if (PLANTS[treeType].isEvergreen()) {
            return false;
        }
        final Calendar c = Heliodon.getInstance().getCalendar();
        final int year = c.get(Calendar.YEAR);
        if (Heliodon.getInstance().getLatitude() > 0) {
            leaf_grow_northern_hemisphere.set(Calendar.YEAR, year); // make sure that the year is the same
            leaf_shed_northern_hemisphere.set(Calendar.YEAR, year);
            return !(c.before(leaf_shed_northern_hemisphere) && c.after(leaf_grow_northern_hemisphere));
        } else {
            leaf_grow_southern_hemisphere.set(Calendar.YEAR, year); // make sure that the year is the same
            leaf_shed_southern_hemisphere.set(Calendar.YEAR, year);
            return !(c.before(leaf_shed_southern_hemisphere) || c.after(leaf_grow_southern_hemisphere));
        }
    }

    @Override
    public void updateTextureAndColor() {
        updateTextureAndColor(mesh, ColorRGBA.WHITE);
    }

    @Override
    public Spatial getCollisionSpatial() {
        if (showPolygons) {
            return getRadiationCollisionSpatial();
        } else {
            crown.removeFromParent();
            collisionRoot.updateWorldBound(true);
            return collisionRoot;
        }
    }

    @Override
    public Spatial getRadiationCollisionSpatial() {
        if (isShedded()) {
            crown.removeFromParent();
        } else {
            collisionRoot.attachChild(crown);
        }
        collisionRoot.updateWorldTransform(true);
        collisionRoot.updateWorldBound(true);
        return collisionRoot;
    }

    public int getPlantType() {
        return treeType;
    }

    public void setPlantType(final int plantType) {
        treeType = plantType;
        if (PLANTS[treeType].getName().equals("Pine")) {
            if (!(crown instanceof Cone)) {
                crown.removeFromParent();
                collisionRoot.detachChild(crown);
                crown = new Cone("Tree Crown", 2, 6, 18, 20, false); // axis samples, radial samples, radius, height, closed
                crown.setModelBound(new BoundingSphere());
                crown.updateModelBound();
                crown.setUserData(new UserData(this));
                collisionRoot.attachChild(crown);
            }
        } else {
            if (!(crown instanceof Sphere)) {
                crown.removeFromParent();
                collisionRoot.detachChild(crown);
                crown = new Sphere("Tree Crown", 4, 8, 14); // z samples, radial samples, radius
                crown.setModelBound(new BoundingSphere());
                crown.updateModelBound();
                crown.setUserData(new UserData(this));
                collisionRoot.attachChild(crown);
            }
        }
        treeWidth = PLANTS[treeType].getWidth() / Scene.getInstance().getScale();
        treeHeight = PLANTS[treeType].getHeight() / Scene.getInstance().getScale();
        if (mesh instanceof Quad) {
            ((Quad) mesh).resize(treeWidth, treeHeight);
        }
        mesh.setTranslation(0, 0, 0.5 * treeHeight);
        setPlantGeometry();
    }

    private void setPlantGeometry() {
        switch (treeType) {
            default: // dogwood
                crown.setScale(1, 1, 1.2);
                crown.setTranslation(0, 0, 24);
                trunk.setScale(2, 2, 1);
                trunk.setTranslation(0, 0, 10);
                break;
            case 1: // elm
                crown.setScale(2, 2, 2.5);
                crown.setTranslation(0, 0, 40);
                trunk.setScale(2, 2, 2);
                trunk.setTranslation(0, 0, 20);
                break;
            case 2: // maple
                crown.setScale(1, 1, 2.1);
                crown.setTranslation(0, 0, 32);
                trunk.setScale(2, 2, 1);
                trunk.setTranslation(0, 0, 10);
                break;
            case 3: // pine
                crown.setScale(1, 1, -4.0);
                crown.setTranslation(0, 0, 45);
                trunk.setScale(2, 2, 1);
                trunk.setTranslation(0, 0, 10);
                break;
            case 4: // oak
                crown.setScale(2.5, 2.5, 3);
                crown.setTranslation(0, 0, 45);
                trunk.setScale(5, 5, 2);
                trunk.setTranslation(0, 0, 20);
                break;
            case 5: // linden
                crown.setScale(3.5, 3.5, 4);
                crown.setTranslation(0, 0, 65);
                trunk.setScale(5, 5, 2);
                trunk.setTranslation(0, 0, 20);
                break;
            case 6: // cottonwood
                crown.setScale(3, 3, 3.5);
                crown.setTranslation(0, 0, 55);
                trunk.setScale(8, 8, 2);
                trunk.setTranslation(0, 0, 20);
                break;
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

    public void move(final Vector3 d, final ArrayList<Vector3> houseMovePoints) {
        if (lockEdit) {
            return;
        }
        final Vector3 newP = houseMovePoints.get(0).add(d, null);
        points.set(0, newP);
        draw();
    }

    public void move(final Vector3 v, final double steplength) {
        if (lockEdit) {
            return;
        }
        v.normalizeLocal().multiplyLocal(steplength);
        final Vector3 p = getAbsPoint(0).addLocal(v);
        points.get(0).set(toRelative(p));
    }

    public void setLocation(final Vector3 v) {
        points.get(0).set(toRelative(v));
    }

    @Override
    public boolean isCopyable() {
        return true;
    }

    @Override
    public HousePart copy(final boolean check) {
        final Tree c = (Tree) super.copy(false);
        c.points.get(0).setX(points.get(0).getX() + 10); // shift the position of the copy
        return c;
    }

}