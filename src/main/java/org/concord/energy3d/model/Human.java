package org.concord.energy3d.model;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.SelectUtil;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
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

    public final static Figure[] FIGURES = new Figure[]{
            new Figure("Jack", true, 2.8, 8.9), //
            new Figure("Jade", false, 2.4, 8.0), // 1
            new Figure("Jane", false, 2.0, 7.8), // 2
            new Figure("Jaye", true, 3.0, 9.1), // 3
            new Figure("Jean", false, 3.0, 9.0), // 4
            new Figure("Jedi", true, 3.0, 8.8), // 5
            new Figure("Jeff", true, 2.8, 8.7), // 6
            new Figure("Jena", false, 1.9, 8.2), // 7
            new Figure("Jeni", false, 2.8, 8.6), // 8
            new Figure("Jess", false, 3.5, 7.8), // 9
            new Figure("Jett", true, 2.9, 9.1), // 10
            new Figure("Jill", false, 3.0, 8.2), // 11
            new Figure("Joan", false, 2.5, 8.6), // 12
            new Figure("Joel", true, 3.9, 8.5), // 13
            new Figure("John", true, 3.8, 9.6), // 14
            new Figure("Jose", true, 8.0, 8.0), // 15
            new Figure("Judd", true, 2.8, 9.2), // 16
            new Figure("Judy", false, 2.2, 8.4), // 17
            new Figure("June", false, 1.6, 7.8), // 18
            new Figure("Juro", true, 2.8, 8.9)};

    private static final long serialVersionUID = 1L;
    private transient BillboardNode billboard;
    private int humanType;
    private transient double feetHeight;

    public Human() {
        this(0, 0);
    }

    public Human(final int humanType, final double feetHeight) {
        super(1, 1, 1);
        this.humanType = humanType;
        this.feetHeight = feetHeight;
        init();
        root.getSceneHints().setCullHint(CullHint.Always);
    }

    @Override
    protected void init() {
        super.init();

        mesh = new Quad("Human Quad", FIGURES[humanType].getWidth(), FIGURES[humanType].getHeight());
        mesh.setModelBound(new BoundingBox());
        mesh.updateModelBound();
        mesh.setRotation(new Matrix3().fromAngles(0.5 * Math.PI, 0, 0));
        translate(FIGURES[humanType].getWidth(), FIGURES[humanType].getHeight(), feetHeight); // stand on the ground by default
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

    // TODO: Called when we want this person to stand at a certain height
    private void translate(final double w, final double h, final double z) {
        mesh.setTranslation(0, w / 2, h / 2 + z);
    }

    @Override
    public void setPreviewPoint(final int x, final int y) {
        final PickedHousePart pick = SelectUtil.pickPart(x, y, new Class<?>[]{Foundation.class, Floor.class, Roof.class, null});
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
        billboard.setScale(0.2 / Scene.getInstance().getScale());
    }

    @Override
    protected String getTextureFileName() {
        return getHumanName().toLowerCase() + ".png";
    }

    @Override
    public void updateTextureAndColor() {
        updateTextureAndColor(mesh, ColorRGBA.WHITE);
    }

    public void setHumanType(final int humanType) {
        this.humanType = humanType;
        if (mesh instanceof Quad) {
            ((Quad) mesh).resize(FIGURES[humanType].getWidth(), FIGURES[humanType].getHeight());
            translate(FIGURES[humanType].getWidth(), FIGURES[humanType].getHeight(), feetHeight); // stand on the ground by default
        }
    }

    public int getHumanType() {
        return humanType;
    }

    public String getHumanName() {
        return getHumanName(humanType);
    }

    public static String getHumanName(final int who) {
        return FIGURES[who].getName();
    }

    public void move(final Vector3 v, final double steplength) {
        v.normalizeLocal().multiplyLocal(steplength);
        final Vector3 p = getAbsPoint(0).addLocal(v);
        points.get(0).set(toRelative(p));
    }

    public void setLocation(final Vector3 v) {
        points.get(0).set(toRelative(v));
    }

    @Override
    public void drawHeatFlux() {
        // this method is left empty on purpose -- don't draw heat flux
    }

    @Override
    protected void computeArea() {
        area = 0.0;
    }

    @Override
    public boolean isCopyable() {
        return true;
    }

    @Override
    public HousePart copy(final boolean check) {
        final Human c = (Human) super.copy(false);
        c.points.get(0).setX(points.get(0).getX() + 2); // shift the position of the copy
        return c;
    }

}