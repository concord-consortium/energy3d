package org.concord.energy3d.scene;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.extension.model.collada.jdom.ColladaAnimUtils;
import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.ColladaMaterialUtils;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.extension.shadow.map.ParallelSplitShadowMapPass;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.FrameHandler;
import com.ardor3d.framework.Updater;
import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.image.util.ImageLoaderUtil;
import com.ardor3d.image.util.awt.AWTImageLoader;
import com.ardor3d.input.*;
import com.ardor3d.input.logical.*;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.light.DirectionalLight;
import com.ardor3d.math.*;
import com.ardor3d.math.type.ReadOnlyVector2;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Camera.ProjectionMode;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.pass.BasicPassManager;
import com.ardor3d.renderer.pass.RenderPass;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.*;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.OffsetState.OffsetType;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.extension.CameraNode;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.Dome;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.Timer;
import com.ardor3d.util.*;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.ResourceSource;
import com.ardor3d.util.resource.SimpleResourceLocator;
import org.concord.energy3d.MainApplication;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.gui.PopupMenuFactory;
import org.concord.energy3d.logger.PlayControl;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.model.*;
import org.concord.energy3d.scene.CameraControl.ButtonAction;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.undo.*;
import org.concord.energy3d.util.*;
import org.concord.energy3d.util.Config.RenderMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SceneManager implements com.ardor3d.framework.Scene, Runnable, Updater {

    static final int SKY_RADIUS = 1000000;
    private static final GameTaskQueueManager taskManager = GameTaskQueueManager.getManager("Task Manager");
    private static final SceneManager instance = new SceneManager(MainPanel.getInstance().getCanvasPanel());
    private static final double MOVE_SPEED = 5;
    private static boolean executeAllTask = true;
    private final Canvas canvas;
    private final FrameHandler frameHandler;
    private final LogicalLayer logicalLayer;
    private final Node root = new Node("Root");
    private final Node backgroundRoot = new Node("Scenary Root");
    private final BasicPassManager passManager = new BasicPassManager();
    private final ParallelSplitShadowMapPass shadowPass;
    private final Mesh sky;
    private final Mesh land;
    private final Mesh collisionLand;
    private final Mesh solarLand;
    private final Quad groundImageLand;
    private final Mesh gridsMesh;
    private final Spatial axes;
    private final LightState lightState = new LightState();
    private final MyUndoManager undoManager = new MyUndoManager();
    private HousePart selectedPart;
    private HousePart hoveredPart;
    private Operation operation = Operation.SELECT;
    private CameraControl cameraControl;
    private ViewMode viewMode = ViewMode.NORMAL;
    private CameraNode cameraNode;
    private MouseState mouseState;
    private AddPartCommand addPartCommand;
    private EditPartCommand editPartCommand;
    private UserData pick;
    private TwoInputStates firstClickState;
    private MouseState lastSelectedEditPointMouseState;
    private MouseState pickMouseState;
    private Vector3 objectMoveStartPoint;
    private ArrayList<Vector3> objectMovePoints;
    private Map<Foundation, ArrayList<Vector3>> objectGroupMovePoints;
    private double refreshTime = -1;
    private int refreshCount;
    private boolean mouseControlEnabled = true;
    private boolean rotAnim;
    private boolean heliodonControl;
    private boolean sunAnim;
    private boolean operationStick;
    private boolean operationFlag;
    private boolean refresh = true;
    private boolean zoomLock;
    private boolean solarHeatMap;
    private boolean heatFluxDaily = true;
    private boolean showHeatFlux;
    private boolean cameraChanged;
    private boolean fineGrid;
    private long frames;
    private long framesStartTime = -1;

    private final java.util.Timer keyHolder = new java.util.Timer();
    private KeyHolderTask arrowKeyHolderTask;
    private final short keyHolderInterval = 200;
    private volatile boolean arrowKeyBlock;

    private class KeyHolderTask extends TimerTask {

        private final KeyboardState keyboardState;
        private final Vector3 direction;

        KeyHolderTask(final KeyboardState keyboardState, final Vector3 direction) {
            super();
            this.keyboardState = keyboardState;
            this.direction = direction;
        }

        @Override
        public void run() {
            if (!arrowKeyBlock) {
                arrowKeyBlock = true;
                taskManager.update(() -> {
                    moveWithKey(keyboardState, direction);
                    arrowKeyBlock = false;
                    return null;
                });
            }
        }

    }

    public enum Operation {
        SELECT, RESIZE, ROTATE, DRAW_WINDOW, DRAW_FOUNDATION, DRAW_EXTERIOR_WALL, DRAW_INTERIOR_WALL, DRAW_DOOR, ADD_ROOF_PYRAMID, ADD_ROOF_HIP, ADD_ROOF_SHED, ADD_ROOF_GAMBREL, ADD_ROOF_CUSTOM, GABLE_ROOF, ADD_FLOOR, ADD_SOLAR_PANEL, ADD_RACK, ADD_HELIOSTAT, ADD_PARABOLIC_TROUGH, ADD_PARABOLIC_DISH, ADD_FRESNEL_REFLECTOR, ADD_SOLAR_WATER_HEATER, ADD_TAPE_MEASURE, ADD_SENSOR, ADD_BOX, ADD_PLANT, ADD_HUMAN
    }

    public enum CameraMode {
        ORBIT, FIRST_PERSON
    }

    public enum ViewMode {
        NORMAL, TOP_VIEW, PRINT_PREVIEW, PRINT
    }

    public static SceneManager getInstance() {
        return instance;
    }

    static boolean isTaskManagerThread() {
        return MainApplication.isSceneManagerThread();
    }

    public static GameTaskQueueManager getTaskManager() {
        return taskManager;
    }

    private SceneManager(final Container panel) {
        System.out.print("Constructing SceneManager...");
        final DisplaySettings settings = new DisplaySettings(400, 300, 24, 0, 0, 24, 0, 4, false, false);

        final RendererFactory rendererFactory;
        if (Config.RENDER_MODE == RenderMode.NEWT) {
            rendererFactory = new JoglNewtFactory(settings, this);
        } else if (Config.RENDER_MODE == RenderMode.JOGL) {
            rendererFactory = new JoglFactory(settings, this);
        } else {
            throw new RuntimeException("No renderer factory!");
        }

        final MouseWrapper mouseWrapper = rendererFactory.getMouseWrapper();
        final KeyboardWrapper keyboardWrapper = rendererFactory.getKeyboardWrapper();
        final FocusWrapper focusWrapper = rendererFactory.getFocusWrapper();
        canvas = rendererFactory.getCanvas();

        final Component canvasComponent = (Component) canvas;
        canvasComponent.setMinimumSize(new Dimension(100, 100));
        canvasComponent.setPreferredSize(new Dimension(100, 100));

        frameHandler = new FrameHandler(new Timer());
        frameHandler.addCanvas(canvas);

        logicalLayer = new LogicalLayer();

        final PhysicalLayer physicalLayer = new PhysicalLayer(keyboardWrapper, mouseWrapper, focusWrapper);
        logicalLayer.registerInput(canvas, physicalLayer);

        frameHandler.addUpdater(this);
        frameHandler.addUpdater(PrintController.getInstance());
        frameHandler.addUpdater(Blinker.getInstance());

        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                resizeCamera();
                refresh(1);
                if (Heliodon.getInstance() != null) {
                    Heliodon.getInstance().updateBloom();
                }
                Foundation.updateBloom();
                Mirror.updateBloom();
            }
        });
        panel.add(canvasComponent, BorderLayout.CENTER);
        System.out.println("done");
        System.out.print("Initializing SceneManager...");
        AWTImageLoader.registerLoader();
        ImageLoaderUtil.registerDefaultHandler(new AWTImageLoader());
        try {
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, new SimpleResourceLocator(getClass().getResource("images/")));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, new SimpleResourceLocator(getClass().getResource("fonts/")));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, new SimpleResourceLocator(getClass().getResource("models/")));
        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        // enable depth test
        final ZBufferState zbuf = new ZBufferState();
        zbuf.setEnabled(true);
        zbuf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        root.setRenderState(zbuf);

        final DirectionalLight light = new DirectionalLight();
        light.setDirection(new Vector3(0, 0, -1));
        light.setAmbient(new ColorRGBA(1, 1, 1, 1));
        light.setEnabled(true);

        lightState.setEnabled(false);
        lightState.attach(light);
        root.setRenderState(lightState);

        sky = createSky();
        land = createLand();
        gridsMesh = drawGrids(5);
        axes = createAxes();
        collisionLand = new Quad("Collision Land", SKY_RADIUS * 2, SKY_RADIUS * 2);
        collisionLand.getSceneHints().setCullHint(CullHint.Always);
        collisionLand.setModelBound(new BoundingBox());
        collisionLand.updateModelBound();
        collisionLand.updateWorldBound(true);
        solarLand = new Quad("Solar Land", 256, 256);
        solarLand.setModelBound(new BoundingBox());
        solarLand.updateModelBound();
        solarLand.updateWorldBound(true);
        solarLand.setVisible(false);
        groundImageLand = new Quad("Ground Image");
        initGroundImageLand(1);
        groundImageLand.setModelBound(new BoundingBox());
        groundImageLand.updateModelBound();
        groundImageLand.updateWorldBound(true);
        groundImageLand.setVisible(false);
        backgroundRoot.getSceneHints().setAllPickingHints(false);
        backgroundRoot.attachChild(sky);
        backgroundRoot.attachChild(land);
        backgroundRoot.attachChild(collisionLand);
        backgroundRoot.attachChild(solarLand);
        backgroundRoot.attachChild(groundImageLand);
        backgroundRoot.attachChild(gridsMesh);
        backgroundRoot.attachChild(axes);
        root.attachChild(backgroundRoot);
        root.attachChild(Scene.getRoot());

        final RenderPass rootPass = new RenderPass();
        rootPass.add(root);
        passManager.add(rootPass);

        shadowPass = new ParallelSplitShadowMapPass(light, 2048, 4);
        // shadowPass = new ParallelSplitShadowMapPass(light, 3072, 3);
        shadowPass.setEnabled(false);
        shadowPass.setUseSceneTexturing(true);
        shadowPass.setUseObjectCullFace(true);
        shadowPass.add(land);
        shadowPass.add(solarLand);
        shadowPass.add(groundImageLand);
        shadowPass.add(Scene.getOriginalHouseRoot());
        shadowPass.addOccluder(Scene.getOriginalHouseRoot());
        shadowPass.addOccluder(Scene.getNotReceivingShadowRoot());

        final Date today = Calendar.getInstance().getTime();
        new Heliodon(root, light, passManager, logicalLayer, today);

        initMouse();

        root.updateGeometricState(0, true);
        System.out.println("Finished initialization.");
    }

    @Override
    public void run() {
        frameHandler.init();
        long frameStartTime;
        final long msPerFrame = 1000 / 60;
        while (true) {
            frameStartTime = System.currentTimeMillis();
            logicalLayer.checkTriggers(frameHandler.getTimer().getTimePerFrame());
            final double now = frameHandler.getTimer().getTimeInSeconds();
            final boolean isUpdateTime = refreshTime != -1 && now <= refreshTime;
            final boolean isTaskAvailable = taskManager.getQueue(GameTaskQueue.UPDATE).size() > 0 || taskManager.getQueue(GameTaskQueue.RENDER).size() > 0;
            final boolean isPrintPreviewAnim = !PrintController.getInstance().isFinished();
            final boolean doRefresh = refresh || isTaskAvailable || isPrintPreviewAnim || Scene.isRedrawAll() || isUpdateTime || rotAnim || Blinker.getInstance().getTarget() != null || sunAnim || (cameraControl != null && cameraControl.isAnimating());
            if (doRefresh || refreshCount > 0) {
                if (now > refreshTime) {
                    refreshTime = -1;
                }
                refresh = false;
                if (doRefresh) {
                    refreshCount = 2;
                } else {
                    refreshCount--;
                }
                try {
                    frameHandler.updateFrame();
                } catch (final Throwable e) {
                    e.printStackTrace();
                    BugReporter.report(e);
                    return;
                }
                synchronized (this) {
                    notifyAll();
                }
            } else {
                frameHandler.getTimer().update();
            }

            final long sleepTime = msPerFrame - (System.currentTimeMillis() - frameStartTime);
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void update(final ReadOnlyTimer timer) {
        final double tpf = timer.getTimePerFrame();
        passManager.updatePasses(tpf);
        taskManager.getQueue(GameTaskQueue.UPDATE).setExecuteMultiple(executeAllTask);
        taskManager.getQueue(GameTaskQueue.UPDATE).execute(canvas.getCanvasRenderer().getRenderer());

        if (operationFlag) {
            executeOperation();
        }

        if (mouseState != null) {
            mouseMoved();
        }

        if (Scene.isRedrawAll()) {
            Scene.getInstance().redrawAllNow();
        }

        if (rotAnim && viewMode == ViewMode.NORMAL && canvas.getCanvasRenderer() != null) {
            final Matrix3 rotate = new Matrix3();
            rotate.fromAngleNormalAxis(45 * tpf * MathUtils.DEG_TO_RAD, Vector3.UNIT_Z);
            final Camera camera = getCamera();
            camera.setLocation(rotate.applyPre(camera.getLocation(), null));
            camera.lookAt(0, 0, 1, Vector3.UNIT_Z);
            getCameraNode().updateFromCamera();
            Scene.getInstance().updateEditShapes();
        }

        final Heliodon heliodon = Heliodon.getInstance();
        if (heliodon != null) {
            if (sunAnim) {
                heliodon.setHourAngle(heliodon.getHourAngle() + tpf * 0.5, true, true, false);
                SceneManager.getInstance().changeSkyTexture();
                SceneManager.getInstance().setShading(heliodon.isNightTime());
                final boolean night = Heliodon.getInstance().isNightTime();
                for (final HousePart part : Scene.getInstance().getParts()) {
                    if (part instanceof Mirror) {
                        final Mirror m = (Mirror) part;
                        if (night) {
                            m.drawSunBeam(); // call this so that the light beams can be set invisible
                        } else {
                            if (m.getReceiver() != null) {
                                m.draw();
                            }
                        }
                    } else if (part instanceof ParabolicTrough) {
                        final ParabolicTrough t = (ParabolicTrough) part;
                        if (night) {
                            t.drawSunBeam(); // call this so that the light beams can be set invisible
                        } else {
                            t.draw();
                        }
                    } else if (part instanceof ParabolicDish) {
                        final ParabolicDish d = (ParabolicDish) part;
                        if (night) {
                            d.drawSunBeam(); // call this so that the light beams can be set invisible
                        } else {
                            d.draw();
                        }
                    } else if (part instanceof FresnelReflector) {
                        final FresnelReflector f = (FresnelReflector) part;
                        if (night) {
                            f.drawSunBeam(); // call this so that the light beams can be set invisible
                        } else {
                            f.draw();
                        }
                    } else if (part instanceof SolarPanel) {
                        final SolarPanel sp = (SolarPanel) part;
                        if (!night && sp.getTracker() != Trackable.NO_TRACKER) {
                            sp.draw();
                        }
                        if (sp.isSunBeamVisible()) {
                            sp.drawSunBeam();
                        }
                    } else if (part instanceof Rack) {
                        final Rack rack = (Rack) part;
                        if (!night && rack.getTracker() != Trackable.NO_TRACKER) {
                            rack.draw();
                        }
                        if (rack.isSunBeamVisible()) {
                            rack.drawSunBeam();
                        }
                    }
                }
            }
            heliodon.update();
        }

        if (cameraControl != null && cameraControl.isAnimating()) {
            cameraControl.animate();
        }

        root.updateGeometricState(tpf);
    }

    @Override
    public boolean renderUnto(final Renderer renderer) {
        if (cameraNode == null) {
            initCamera();
            return false;
        }
        // setWindowsVisible(false);
        try {
            passManager.renderPasses(renderer);
        } catch (final Throwable t) {
            t.printStackTrace();
        }
        if (shadowPass.isEnabled() && !Heliodon.getInstance().isNightTime()) {
            final Camera camera = SceneManager.getInstance().getCamera();
            if (camera != null && camera.getProjectionMode() != ProjectionMode.Parallel) {
                final double distance = 4 * camera.getLocation().length();
                shadowPass.setMaxShadowDistance(distance);
            } else {
                shadowPass.setMaxShadowDistance(2000);
            }
            try {
                shadowPass.renderPass(renderer);
            } catch (final Throwable e) {
                e.printStackTrace();
                shadowPass.setEnabled(false);
            }
        }
        setWindowsVisible(true);
        try {
            passManager.renderPasses(renderer);
        } catch (final Throwable t) {
            t.printStackTrace();
        }
        // com.ardor3d.util.geom.Debugger.drawBounds(Scene.getRoot(), renderer, true);
        setWindowsVisible(false);
        taskManager.getQueue(GameTaskQueue.RENDER).execute(renderer);
        if (framesStartTime != -1) {
            frames++;
            final long t = (System.nanoTime() - framesStartTime) / 1000000000;
            if (t != 0) {
                System.out.println("FPS = " + frames / t);
            }
        }
        return true;
    }

    private void setWindowsVisible(final boolean visible) {
        backgroundRoot.getSceneHints().setCullHint(visible ? CullHint.Always : CullHint.Inherit);
        for (final HousePart part : Scene.getInstance().getParts()) {
            if (part instanceof Window) {
                part.getMesh().setVisible(visible);
            } else {
                part.getRoot().getSceneHints().setCullHint(visible ? CullHint.Always : CullHint.Inherit);
            }
        }
    }

    private void initCamera() {
        System.out.println("initCamera()");
        final Camera camera = getCamera();
        cameraNode = new CameraNode("Camera Node", camera);
        root.attachChild(cameraNode);
        cameraNode.updateFromCamera();
        Scene.getInstance().updateEditShapes();
        setCameraControl(CameraMode.ORBIT);
        resetCamera(ViewMode.NORMAL);
        SceneManager.getInstance().getCameraControl().reset();

        taskManager.update(() -> {
            final Spatial compass = createCompass();
            compass.setScale(0.1);
            compass.setTranslation(-1, -0.7, 2);
            cameraNode.attachChild(compass);
            final Spatial earth = createEarth();
            earth.setScale(0.00012);
            earth.setTranslation(-1, -0.67, 2);
            cameraNode.attachChild(earth);
            Scene.getInstance().updateEditShapes();
            return null;
        });
    }

    @Override
    public PickResults doPick(final Ray3 pickRay) {
        return null;
    }

    public FrameHandler getFrameHandler() {
        return frameHandler;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    private Mesh createLand() {
        final Quad land = new Quad("Land", SKY_RADIUS * 2, SKY_RADIUS * 2);
        land.setDefaultColor(new ColorRGBA(0, 1.0f, 0.75f, 0.5f));
        final OffsetState offsetState = new OffsetState();
        offsetState.setTypeEnabled(OffsetType.Fill, true);
        offsetState.setFactor(10);
        offsetState.setUnits(10);
        land.setRenderState(offsetState);
        final BlendState blendState = new BlendState();
        blendState.setBlendEnabled(true);
        land.setRenderState(blendState);
        land.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
        final MaterialState ms = new MaterialState();
        ms.setColorMaterial(ColorMaterial.Diffuse);
        land.setRenderState(ms);
        land.updateModelBound();
        land.updateWorldBound(true);
        return land;
    }

    private Mesh drawGrids(final double gridSize) {
        final Mesh gridsMesh = new Line("Ground Grids");
        gridsMesh.getSceneHints().setCullHint(CullHint.Always);
        gridsMesh.setDefaultColor(new ColorRGBA(0, 0, 1, 1));
        final BlendState blendState = new BlendState();
        blendState.setBlendEnabled(true);
        gridsMesh.setRenderState(blendState);
        gridsMesh.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);

        final ReadOnlyVector3 width = Vector3.UNIT_X.multiply(2000, null);
        final ReadOnlyVector3 height = Vector3.UNIT_Y.multiply(2000, null);
        final ArrayList<ReadOnlyVector3> points = new ArrayList<>();
        final ReadOnlyVector3 pMiddle = Vector3.ZERO;

        final int cols = (int) (width.length() / gridSize);

        for (int col = 1; col < cols / 2 + 1; col++) {
            for (int neg = -1; neg <= 1; neg += 2) {
                final ReadOnlyVector3 lineP1 = width.normalize(null).multiplyLocal(neg * col * gridSize).addLocal(pMiddle).subtractLocal(height.multiply(0.5, null));
                points.add(lineP1);
                final ReadOnlyVector3 lineP2 = lineP1.add(height, null);
                points.add(lineP2);
            }
        }

        final int rows = (int) (height.length() / gridSize);

        for (int row = 1; row < rows / 2 + 1; row++) {
            for (int neg = -1; neg <= 1; neg += 2) {
                final ReadOnlyVector3 lineP1 = height.normalize(null).multiplyLocal(neg * row * gridSize).addLocal(pMiddle).subtractLocal(width.multiply(0.5, null));
                points.add(lineP1);
                final ReadOnlyVector3 lineP2 = lineP1.add(width, null);
                points.add(lineP2);
            }
        }
        final FloatBuffer buf = BufferUtils.createVector3Buffer(points.size());
        for (final ReadOnlyVector3 p : points) {
            buf.put(p.getXf()).put(p.getYf()).put(0.01f);
        }

        gridsMesh.getMeshData().setVertexBuffer(buf);
        gridsMesh.getMeshData().updateVertexCount();

        Util.disablePickShadowLight(gridsMesh);
        gridsMesh.setModelBound(new BoundingBox());
        gridsMesh.updateModelBound();
        gridsMesh.updateWorldBound(true);
        return gridsMesh;
    }

    public void setGridsVisible(final boolean visible) {
        gridsMesh.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
    }

    private Mesh createSky() {
        final Dome sky = new Dome("Sky", 100, 100, SKY_RADIUS);
        sky.setRotation(new Matrix3().fromAngles(Math.PI / 2, 0, 0));
        sky.getSceneHints().setLightCombineMode(LightCombineMode.Off);
        sky.getSceneHints().setAllPickingHints(false);
        sky.updateModelBound();
        sky.updateWorldBound(true);
        final TextureState ts = new TextureState();
        ts.setTexture(TextureManager.load("daysky.jpg", Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
        sky.setRenderState(ts);
        return sky;
    }

    public void changeSkyTexture() {
        if (sky != null) {
            final boolean isNightTime = Heliodon.getInstance().isNightTime();
            String textureFile;
            switch (Scene.getInstance().getEnvironment()) {
                case Scene.DESERT_THEME:
                    textureFile = isNightTime ? "desert-night.jpg" : "desert.jpg";
                    break;
                case Scene.GRASSLAND_THEME:
                    textureFile = isNightTime ? "grassland-night.jpg" : "grassland.jpg";
                    break;
                case Scene.FOREST_THEME:
                    textureFile = isNightTime ? "forest-night.jpg" : "forest.jpg";
                    break;
                default:
                    textureFile = isNightTime ? "nightsky.jpg" : "daysky.jpg";
            }
            final TextureState ts = new TextureState();
            ts.setTexture(TextureManager.load(textureFile, Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
            sky.setRenderState(ts);
        }
    }

    private Spatial createAxes() {
        final int axisLen = SKY_RADIUS;
        final Node axisRoot = new Node();
        FloatBuffer buf;
        Line line;

        // X-Axis
        buf = BufferUtils.createVector3Buffer(2);
        buf.put(-axisLen).put(0).put(0);
        buf.put(axisLen).put(0).put(0);
        line = new Line("X-Axis", buf, null, null, null);
        line.setDefaultColor(ColorRGBA.RED);
        Util.disablePickShadowLight(line);
        line.setModelBound(new BoundingBox());
        line.updateModelBound();
        axisRoot.attachChild(line);

        // Y-Axis
        buf = BufferUtils.createVector3Buffer(2);
        buf.put(0).put(-axisLen).put(0);
        buf.put(0).put(axisLen).put(0);
        line = new Line("Y-Axis", buf, null, null, null);
        line.setDefaultColor(ColorRGBA.GREEN);
        Util.disablePickShadowLight(line);
        line.setModelBound(new BoundingBox());
        line.updateModelBound();
        axisRoot.attachChild(line);

        // Z-Axis
        buf = BufferUtils.createVector3Buffer(2);
        buf.put(0).put(0).put(-axisLen);
        buf.put(0).put(0).put(axisLen);
        line = new Line("Z-Axis", buf, null, null, null);
        Util.disablePickShadowLight(line);
        line.setDefaultColor(ColorRGBA.BLUE);
        line.setModelBound(new BoundingBox());
        line.updateModelBound();
        axisRoot.attachChild(line);

        axisRoot.updateWorldBound(true);
        return axisRoot;
    }

    private void initMouse() {

        logicalLayer.registerTrigger(new InputTrigger(new MouseButtonPressedCondition(MouseButton.LEFT), (source, inputStates, tpf) -> {
            ((Component) canvas).requestFocusInWindow();
            if (Config.isMac()) { // control-click is mouse right-click on the Mac, skip
                final KeyboardState ks = inputStates.getCurrent().getKeyboardState();
                if (ks.isDown(Key.LCONTROL) || ks.isDown(Key.RCONTROL)) {
                    return;
                }
            }
            if (firstClickState == null) {
                firstClickState = inputStates;
                mousePressed(inputStates.getCurrent().getMouseState(), inputStates.getCurrent().getKeyboardState());
            } else {
                firstClickState = null;
                mouseReleased(inputStates.getCurrent().getMouseState());
            }
        }));

        logicalLayer.registerTrigger(new InputTrigger(new MouseButtonReleasedCondition(MouseButton.LEFT), (source, inputStates, tpf) -> {
            if (Config.isMac()) { // control-click is mouse right-click on the Mac, skip
                final KeyboardState ks = inputStates.getCurrent().getKeyboardState();
                if (ks.isDown(Key.LCONTROL) || ks.isDown(Key.RCONTROL)) {
                    return;
                }
            }
            // if editing object using select or resize then only mouse drag is allowed
            if (operation == Operation.SELECT || operation == Operation.RESIZE) {
                firstClickState = null;
                mouseReleased(inputStates.getCurrent().getMouseState());
            } else if (firstClickState != null) {
                final MouseState mouseState = inputStates.getCurrent().getMouseState();
                final MouseState prevMouseState = firstClickState.getCurrent().getMouseState();
                final ReadOnlyVector2 p1 = new Vector2(prevMouseState.getX(), prevMouseState.getY());
                final ReadOnlyVector2 p2 = new Vector2(mouseState.getX(), mouseState.getY());
                if (!(selectedPart instanceof Foundation || selectedPart instanceof Wall || selectedPart instanceof Window || selectedPart instanceof Door) || p1.distance(p2) > 10) {
                    firstClickState = null;
                    mouseReleased(inputStates.getCurrent().getMouseState());
                }
            }
        }));

        ((Component) canvas).addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (Util.isRightClick(e)) {
                    mouseRightClicked(e);
                }
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                if (Util.isRightClick(e)) {
                    if (cameraChanged) {
                        TimeSeriesLogger.getInstance().logCamera("Pan");
                        cameraChanged = false;
                    }
                }
            }
        });

        ((Component) canvas).addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(final MouseEvent e) {
                // EnergyPanel.getInstance().update(); // sometimes drag initiates incomplete objects whose properties are problematic
                cameraChanged = true;
            }
        });

        ((Component) canvas).addMouseWheelListener(e -> TimeSeriesLogger.getInstance().logCamera("Zoom"));

        logicalLayer.registerTrigger(new InputTrigger(new MouseMovedCondition(), (source, inputStates, tpf) -> {
            refresh = true;
            mouseState = inputStates.getCurrent().getMouseState();
        }));

        logicalLayer.registerTrigger(new InputTrigger(new MouseButtonClickedCondition(MouseButton.LEFT), (source, inputStates, tpf) -> {
            if (Config.isMac()) { // control-click is mouse right-click on the Mac, skip
                final KeyboardState ks = inputStates.getCurrent().getKeyboardState();
                if (ks.isDown(Key.LCONTROL) || ks.isDown(Key.RCONTROL)) {
                    return;
                }
            }
            if (!isTopView() && inputStates.getCurrent().getMouseState().getClickCount(MouseButton.LEFT) == 2) {
                if (PrintController.getInstance().isPrintPreview()) {
                    final MouseState mouse = inputStates.getCurrent().getMouseState();
                    final Ray3 pickRay = Camera.getCurrentCamera().getPickRay(new Vector2(mouse.getX(), mouse.getY()), false, null);
                    final PickResults pickResults = new PrimitivePickResults();
                    PickingUtil.findPick(PrintController.getInstance().getPagesRoot(), pickRay, pickResults, false);
                    if (pickResults.getNumber() > 0) {
                        cameraControl.zoomAtPoint(pickResults.getPickData(0).getIntersectionRecord().getIntersectionPoint(0));
                    }
                } else {
                    final PickedHousePart pickedHousePart = SelectUtil.pickPart(inputStates.getCurrent().getMouseState().getX(), inputStates.getCurrent().getMouseState().getY());
                    if (pickedHousePart != null) {
                        cameraControl.zoomAtPoint(pickedHousePart.getPoint());
                    }
                }
            }
        }));

        logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.LSHIFT), (source, inputStates, tpf) -> {
            // SelectUtil.setPickLayer(0);
        }));
        logicalLayer.registerTrigger(new InputTrigger(new KeyReleasedCondition(Key.LSHIFT), (source, inputStates, tpf) -> {
            // SelectUtil.setPickLayer(-1);
        }));
        logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.DELETE), (source, inputStates, tpf) -> deleteCurrentSelection()));
        logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.BACK), (source, inputStates, tpf) -> deleteCurrentSelection()));
        logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.ESCAPE), (source, inputStates, tpf) -> hideAllEditPoints()));
        logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.ZERO), (source, inputStates, tpf) -> {
            final KeyboardState ks = inputStates.getCurrent().getKeyboardState();
            if (Config.isMac()) {
                if (ks.isDown(Key.LMETA) || ks.isDown(Key.RMETA)) {
                    resetCamera();
                }
            } else {
                if (ks.isDown(Key.LCONTROL) || ks.isDown(Key.RCONTROL)) {
                    resetCamera();
                }
            }
        }));
        logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.I), (source, inputStates, tpf) -> {
            System.out.println("---- Parts: ------------------------");
            System.out.println("size = " + Scene.getInstance().getParts().size());
            for (final HousePart part : Scene.getInstance().getParts()) {
                System.out.println(part);
            }
            System.out.println("---- Scene: ------------------------");
            System.out.println("size = " + Scene.getOriginalHouseRoot().getNumberOfChildren());
            for (final Spatial mesh : Scene.getOriginalHouseRoot().getChildren()) {
                System.out.println(mesh);
            }
        }));
        logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.R), (source, inputStates, tpf) -> Scene.getInstance().redrawAll(true)));

        // Run/pause model replay
        logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.SPACE), (source, inputStates, tpf) -> {
            if (PlayControl.active) {
                PlayControl.replaying = !PlayControl.replaying;
            }
        }));

        logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.LEFT), (source, inputStates, tpf) -> {
            if (PlayControl.active) {
                PlayControl.replaying = false;
                PlayControl.backward = true;
            }
            if (isTopView()) {
                if (tooManyPartsToMove()) {
                    moveWithKey(inputStates.getCurrent().getKeyboardState(), new Vector3(-1, 0, 0));
                } else {
                    if (arrowKeyHolderTask != null) {
                        arrowKeyHolderTask.cancel();
                    }
                    arrowKeyHolderTask = new KeyHolderTask(inputStates.getCurrent().getKeyboardState(), new Vector3(-1, 0, 0));
                    keyHolder.scheduleAtFixedRate(arrowKeyHolderTask, 0, keyHolderInterval);
                }
            } else {
                if (selectedPart instanceof Window) {
                    final Vector3 v = selectedPart.getNormal().clone();
                    v.crossLocal(Vector3.UNIT_Z);
                    moveWithKey(inputStates.getCurrent().getKeyboardState(), v);
                    Scene.getInstance().redrawAll();
                }
            }
        }));
        logicalLayer.registerTrigger(new InputTrigger(new KeyReleasedCondition(Key.LEFT), (source, inputStates, tpf) -> {
            if (arrowKeyHolderTask != null) {
                arrowKeyHolderTask.cancel();
            }
        }));

        logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.UP), (source, inputStates, tpf) -> {
            if (PlayControl.active) {
                PlayControl.replaying = false;
                PlayControl.backward = true;
            }
            if (isTopView()) {
                if (tooManyPartsToMove()) {
                    moveWithKey(inputStates.getCurrent().getKeyboardState(), new Vector3(0, 1, 0));
                } else {
                    if (arrowKeyHolderTask != null) {
                        arrowKeyHolderTask.cancel();
                    }
                    arrowKeyHolderTask = new KeyHolderTask(inputStates.getCurrent().getKeyboardState(), new Vector3(0, 1, 0));
                    keyHolder.scheduleAtFixedRate(arrowKeyHolderTask, 0, keyHolderInterval);
                }
            } else {
                if (selectedPart instanceof Window) {
                    final Vector3 n = selectedPart.getNormal().clone();
                    final Vector3 v = n.cross(Vector3.UNIT_Z, null);
                    moveWithKey(inputStates.getCurrent().getKeyboardState(), v.crossLocal(n));
                    Scene.getInstance().redrawAll();
                }
            }
        }));
        logicalLayer.registerTrigger(new InputTrigger(new KeyReleasedCondition(Key.UP), (source, inputStates, tpf) -> {
            if (arrowKeyHolderTask != null) {
                arrowKeyHolderTask.cancel();
            }
        }));

        logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.RIGHT), (source, inputStates, tpf) -> {
            if (PlayControl.active) {
                PlayControl.replaying = false;
                PlayControl.forward = true;
            }
            if (isTopView()) {
                if (tooManyPartsToMove()) {
                    moveWithKey(inputStates.getCurrent().getKeyboardState(), new Vector3(1, 0, 0));
                } else {
                    if (arrowKeyHolderTask != null) {
                        arrowKeyHolderTask.cancel();
                    }
                    arrowKeyHolderTask = new KeyHolderTask(inputStates.getCurrent().getKeyboardState(), new Vector3(1, 0, 0));
                    keyHolder.scheduleAtFixedRate(arrowKeyHolderTask, 0, keyHolderInterval);
                }
            } else {
                if (selectedPart instanceof Window) {
                    final Vector3 v = selectedPart.getNormal().clone();
                    v.crossLocal(Vector3.UNIT_Z).negateLocal();
                    moveWithKey(inputStates.getCurrent().getKeyboardState(), v);
                    Scene.getInstance().redrawAll();
                }
            }
        }));
        logicalLayer.registerTrigger(new InputTrigger(new KeyReleasedCondition(Key.RIGHT), (source, inputStates, tpf) -> {
            if (arrowKeyHolderTask != null) {
                arrowKeyHolderTask.cancel();
            }
        }));

        logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.DOWN), (source, inputStates, tpf) -> {
            if (PlayControl.active) {
                PlayControl.replaying = false;
                PlayControl.forward = true;
            }
            if (isTopView()) {
                if (tooManyPartsToMove()) {
                    moveWithKey(inputStates.getCurrent().getKeyboardState(), new Vector3(0, -1, 0));
                } else {
                    if (arrowKeyHolderTask != null) {
                        arrowKeyHolderTask.cancel();
                    }
                    arrowKeyHolderTask = new KeyHolderTask(inputStates.getCurrent().getKeyboardState(), new Vector3(0, -1, 0));
                    keyHolder.scheduleAtFixedRate(arrowKeyHolderTask, 0, keyHolderInterval);
                }
            } else {
                if (selectedPart instanceof Window) {
                    final Vector3 n = selectedPart.getNormal().clone();
                    final Vector3 v = n.cross(Vector3.UNIT_Z, null).negateLocal();
                    moveWithKey(inputStates.getCurrent().getKeyboardState(), v.crossLocal(n));
                    Scene.getInstance().redrawAll();
                }
            }
        }));
        logicalLayer.registerTrigger(new InputTrigger(new KeyReleasedCondition(Key.DOWN), (source, inputStates, tpf) -> {
            if (arrowKeyHolderTask != null) {
                arrowKeyHolderTask.cancel();
            }
        }));

        logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.ESCAPE), (source, inputStates, tpf) -> PlayControl.active = false));

        logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.W), (source, inputStates, tpf) -> {
            if (tooManyPartsToMove()) {
                moveWithKey(inputStates.getCurrent().getKeyboardState(), new Vector3(-1, 0, 0));
            } else {
                if (arrowKeyHolderTask != null) {
                    arrowKeyHolderTask.cancel();
                }
                arrowKeyHolderTask = new KeyHolderTask(inputStates.getCurrent().getKeyboardState(), new Vector3(-1, 0, 0));
                keyHolder.scheduleAtFixedRate(arrowKeyHolderTask, 0, keyHolderInterval);
            }
        }));
        logicalLayer.registerTrigger(new InputTrigger(new KeyReleasedCondition(Key.W), (source, inputStates, tpf) -> {
            if (arrowKeyHolderTask != null) {
                arrowKeyHolderTask.cancel();
            }
        }));

        logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.E), (source, inputStates, tpf) -> {
            if (tooManyPartsToMove()) {
                moveWithKey(inputStates.getCurrent().getKeyboardState(), new Vector3(1, 0, 0));
            } else {
                if (arrowKeyHolderTask != null) {
                    arrowKeyHolderTask.cancel();
                }
                arrowKeyHolderTask = new KeyHolderTask(inputStates.getCurrent().getKeyboardState(), new Vector3(1, 0, 0));
                keyHolder.scheduleAtFixedRate(arrowKeyHolderTask, 0, keyHolderInterval);
            }
        }));
        logicalLayer.registerTrigger(new InputTrigger(new KeyReleasedCondition(Key.E), (source, inputStates, tpf) -> {
            if (arrowKeyHolderTask != null) {
                arrowKeyHolderTask.cancel();
            }
        }));

        logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.S), (source, inputStates, tpf) -> {
            if (tooManyPartsToMove()) {
                moveWithKey(inputStates.getCurrent().getKeyboardState(), new Vector3(0, -1, 0));
            } else {
                if (arrowKeyHolderTask != null) {
                    arrowKeyHolderTask.cancel();
                }
                arrowKeyHolderTask = new KeyHolderTask(inputStates.getCurrent().getKeyboardState(), new Vector3(0, -1, 0));
                keyHolder.scheduleAtFixedRate(arrowKeyHolderTask, 0, keyHolderInterval);
            }
        }));
        logicalLayer.registerTrigger(new InputTrigger(new KeyReleasedCondition(Key.S), (source, inputStates, tpf) -> {
            if (arrowKeyHolderTask != null) {
                arrowKeyHolderTask.cancel();
            }
        }));

        logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.N), (source, inputStates, tpf) -> {
            if (tooManyPartsToMove()) {
                moveWithKey(inputStates.getCurrent().getKeyboardState(), new Vector3(0, 1, 0));
            } else {
                if (arrowKeyHolderTask != null) {
                    arrowKeyHolderTask.cancel();
                }
                arrowKeyHolderTask = new KeyHolderTask(inputStates.getCurrent().getKeyboardState(), new Vector3(0, 1, 0));
                keyHolder.scheduleAtFixedRate(arrowKeyHolderTask, 0, keyHolderInterval);
            }
        }));
        logicalLayer.registerTrigger(new InputTrigger(new KeyReleasedCondition(Key.N), (source, inputStates, tpf) -> {
            if (arrowKeyHolderTask != null) {
                arrowKeyHolderTask.cancel();
            }
        }));

    }

    // if there are too many parts to move, don't invoke a timer -- a move would be too slow
    private boolean tooManyPartsToMove() {
        if (selectedPart == null) {
            return Scene.getInstance().getParts().size() > 100;
        }
        if (selectedPart instanceof Foundation) {
            final Foundation f = (Foundation) selectedPart;
            if (f.hasSolarReceiver()) { // a solar receiver could cause all the reflectors that point to it to redraw, which is costly
                return true;
            }
            return f.getChildren().size() > 100;
        }
        return false;
    }

    private void moveWithKey(final KeyboardState ks, final Vector3 v) {
        if (ks.isDown(Key.LCONTROL) || ks.isDown(Key.RCONTROL) || ks.isDown(Key.LMETA) || ks.isDown(Key.LMETA)) {
            return; // Ctrl/Cmd+key is often used for other purposes such as Ctrl+S or Cmd+S
        }
        fineGrid = ks.isDown(Key.LSHIFT) || ks.isDown(Key.RSHIFT);
        move(v);
        EventQueue.invokeLater(() -> EnergyPanel.getInstance().updateProperties());
    }

    public void move(final Vector3 v) {
        if (SceneManager.getInstance().getSolarHeatMap()) {
            EnergyPanel.getInstance().updateRadiationHeatMap();
        }
        final MovePartCommand c = new MovePartCommand(selectedPart, v);
        if (selectedPart == null) {
            for (final HousePart p : Scene.getInstance().getParts()) {
                if (p instanceof Foundation) {
                    ((Foundation) p).move(v, p.getGridSize());
                }
            }
            Scene.getInstance().redrawAll();
        } else if (selectedPart instanceof Foundation) {
            final Foundation f = (Foundation) selectedPart;
            if (f.isGroupMaster()) {
                final List<Foundation> g = Scene.getInstance().getFoundationGroup(f);
                if (g != null) {
                    for (final Foundation x : g) {
                        x.move(v, selectedPart.getGridSize());
                    }
                }
            } else {
                f.move(v, selectedPart.getGridSize());
            }
        } else if (selectedPart instanceof FoundationPolygon) {
            ((Foundation) selectedPart.getContainer()).moveAllWithinPolygon(v);

        } else if (selectedPart instanceof Roof) {
            if (viewMode == ViewMode.TOP_VIEW) {
                final Foundation f = selectedPart.getTopContainer();
                if (f.isGroupMaster()) {
                    final List<Foundation> g = Scene.getInstance().getFoundationGroup(f);
                    if (g != null) {
                        for (final Foundation x : g) {
                            x.move(v, selectedPart.getGridSize());
                        }
                    }
                } else {
                    f.move(v, selectedPart.getGridSize());
                }
            }
        } else if (selectedPart instanceof Window) {
            final Window w = (Window) selectedPart;
            w.move(v);
            w.draw();
        } else if (selectedPart instanceof SolarCollector) {
            final SolarCollector sc = (SolarCollector) selectedPart;
            sc.move(v, selectedPart.getGridSize());
            selectedPart.draw();
        } else if (selectedPart instanceof Tree) {
            final Tree t = (Tree) selectedPart;
            t.move(v, selectedPart.getGridSize());
            t.draw();
        } else if (selectedPart instanceof Human) {
            final Human h = (Human) selectedPart;
            h.move(v, selectedPart.getGridSize());
            h.draw();
        }
        undoManager.addEdit(c);
        SceneManager.getInstance().refresh();
        EventQueue.invokeLater(() -> Scene.getInstance().setEdited(true));
    }

    public void setCameraControl(final CameraMode type) {
        if (cameraControl != null) {
            cameraControl.removeTriggers(logicalLayer);
        }
        if (type == CameraMode.ORBIT) {
            cameraControl = new OrbitControl(Vector3.UNIT_Z);
        } else if (type == CameraMode.FIRST_PERSON) {
            cameraControl = new FirstPersonControl(Vector3.UNIT_Z);
        }
        if (cameraControl != null) {
            cameraControl.setupMouseTriggers(logicalLayer, true);
            cameraControl.setMoveSpeed(MOVE_SPEED);
            cameraControl.setKeyRotateSpeed(1);
        }
    }

    public CameraControl getCameraControl() {
        return cameraControl;
    }

    public void hideAllEditPoints() {
        for (final HousePart part : Scene.getInstance().getParts()) {
            part.setEditPointsVisible(false);
            part.setGridsVisible(false);
            part.setLinePatternVisible(false);
        }
        selectedPart = null;
        refresh = true;
    }

    public void resetCamera() {
        resetCamera(viewMode);
        cameraControl.reset();
        refresh = true;
    }

    public void resetCamera(final ViewMode viewMode) {
        System.out.println("resetCamera()");
        this.viewMode = viewMode;
        final Camera camera = getCamera();
        cameraControl.setMouseButtonActions(ButtonAction.MOVE, ButtonAction.MOVE);
        cameraControl.setMoveSpeed(MOVE_SPEED);
        ReadOnlyVector3 loc = new Vector3(0, -100, 25);
        ReadOnlyVector3 up = new Vector3(0, 0, 1);
        ReadOnlyVector3 lookAt = new Vector3(0, 0, 0);

        setCompassVisible(viewMode == ViewMode.NORMAL);

        if (viewMode == ViewMode.NORMAL) {
            cameraControl.setMouseButtonActions(ButtonAction.ROTATE, ButtonAction.MOVE);
            camera.setProjectionMode(ProjectionMode.Perspective);
            resizeCamera();
        } else if (viewMode == ViewMode.TOP_VIEW) {
            camera.setProjectionMode(ProjectionMode.Parallel);
            loc = new Vector3(0, 0, 500);
            up = new Vector3(0, 1, 0);
            lookAt = new Vector3(0, 0, 0);
            final double boundLength = Util.findBoundLength(Scene.getRoot().getWorldBound());
            cameraControl.setMoveSpeed(boundLength * 2);
            resizeCamera(boundLength);
        } else if (viewMode == ViewMode.PRINT) {
            camera.setProjectionMode(ProjectionMode.Parallel);
            /* location will be set in PrintController.print() */
            loc = new Vector3(0, -10, 0);
            up = new Vector3(0, 0, -1);
        } else if (viewMode == ViewMode.PRINT_PREVIEW) {
            cameraControl.setMouseButtonActions(ButtonAction.MOVE, ButtonAction.MOVE);
            camera.setProjectionMode(ProjectionMode.Perspective);
            loc = PrintController.getInstance().getZoomAllCameraLocation();
            lookAt = loc.add(0, 1, 0, null);
            resizeCamera(PrintController.getInstance().getPageWidth());
        }

        camera.setLocation(loc);
        camera.lookAt(lookAt, up);
        camera.update();

        cameraNode.updateFromCamera();
        Scene.getInstance().updateEditShapes();
    }

    public ViewMode getViewMode() {
        return viewMode;
    }

    private void resizeCamera() {
        final BoundingVolume bounds = Scene.getRoot().getWorldBound();
        if (bounds == null) {
            resizeCamera(2);
        } else {
            resizeCamera(Util.findBoundLength(bounds));
        }
    }

    void resizeCamera(final double orthoWidth) {
        final Camera camera = getCamera();
        if (camera == null) {
            return;
        }
        final Dimension size = ((Component) canvas).getSize();
        camera.resize(size.width, size.height);
        final double ratio = (double) size.width / size.height;
        final double near = 1;
        final double far = 2 * SKY_RADIUS;
        if (camera.getProjectionMode() == ProjectionMode.Parallel) {
            camera.setFrustum(near, far, -orthoWidth / 2, orthoWidth / 2, orthoWidth / ratio / 2, -orthoWidth / ratio / 2);
        } else {
            camera.setFrustumPerspective(45.0, ratio, near, far);
        }
    }

    public void toggleSpinView() {
        cameraControl.reset();
        rotAnim = !rotAnim;
        if (rotAnim) {
            framesStartTime = System.nanoTime();
            frames = 0;
        } else {
            framesStartTime = -1;
        }
    }

    public boolean getSpinView() {
        return rotAnim;
    }

    public void setSpinView(final boolean spinView) {
        rotAnim = spinView;
    }

    public void setOperation(final Operation operation) {
        if (isZoomLock()) {
            setZoomLock(false);
        }
        operationStick = false;
        if (this.operation != operation) {
            this.operation = operation;
            operationFlag = true;
            // need to be here to ensure immediate removal of unfinished house part before computeEnergy thread is started
            taskManager.update(() -> {
                if (selectedPart != null) {
                    if (selectedPart.isDrawCompleted()) {
                        selectedPart.setEditPointsVisible(false);
                    } else {
                        Scene.getInstance().remove(selectedPart, false);
                    }
                    if (operation != Operation.RESIZE) { // need to keep selectedPart when resizing
                        selectedPart = null;
                    }
                }
                return null;
            });
        }
    }

    public void setOperationStick(final boolean stick) {
        operationStick = stick;
    }

    private void executeOperation() {
        operationFlag = false;
        if (operation == Operation.RESIZE) {
            if (selectedPart instanceof Foundation) {
                ((Foundation) selectedPart).setResizeHouseMode(true);
            }
        } else {
            for (final HousePart part : Scene.getInstance().getParts()) {
                if (part instanceof Foundation) {
                    ((Foundation) part).setResizeHouseMode(false);
                }
            }
        }

        if (viewMode != ViewMode.PRINT_PREVIEW) {
            Scene.getInstance().drawResizeBounds();
        }

        selectedPart = newPart();
        if (selectedPart != null) {
            cameraControl.setLeftMouseButtonEnabled(false);
        }
    }

    private HousePart newPart() {
        final HousePart drawn;
        setGridsVisible(false);
        if (operation == Operation.DRAW_EXTERIOR_WALL) {
            drawn = new Wall();
            drawn.setColor(Scene.getInstance().getDefaultWallColor());
        } else if (operation == Operation.DRAW_DOOR) {
            drawn = new Door();
            drawn.setColor(Scene.getInstance().getDefaultDoorColor());
        } else if (operation == Operation.DRAW_WINDOW) {
            drawn = new Window();
        } else if (operation == Operation.ADD_ROOF_PYRAMID) {
            drawn = new PyramidRoof();
            drawn.setColor(Scene.getInstance().getDefaultRoofColor());
        } else if (operation == Operation.ADD_ROOF_HIP) {
            drawn = new HipRoof();
            drawn.setColor(Scene.getInstance().getDefaultRoofColor());
        } else if (operation == Operation.ADD_ROOF_SHED) {
            drawn = new ShedRoof();
            drawn.setColor(Scene.getInstance().getDefaultRoofColor());
        } else if (operation == Operation.ADD_ROOF_GAMBREL) {
            drawn = new GambrelRoof();
            drawn.setColor(Scene.getInstance().getDefaultRoofColor());
        } else if (operation == Operation.ADD_ROOF_CUSTOM) {
            drawn = new CustomRoof();
            drawn.setColor(Scene.getInstance().getDefaultRoofColor());
        } else if (operation == Operation.ADD_FLOOR) {
            drawn = new Floor();
            drawn.setColor(Scene.getInstance().getDefaultFloorColor());
        } else if (operation == Operation.ADD_SOLAR_PANEL) {
            drawn = new SolarPanel();
        } else if (operation == Operation.ADD_RACK) {
            drawn = new Rack();
        } else if (operation == Operation.ADD_HELIOSTAT) {
            drawn = new Mirror();
        } else if (operation == Operation.ADD_PARABOLIC_TROUGH) {
            drawn = new ParabolicTrough();
        } else if (operation == Operation.ADD_PARABOLIC_DISH) {
            drawn = new ParabolicDish();
        } else if (operation == Operation.ADD_FRESNEL_REFLECTOR) {
            drawn = new FresnelReflector();
        } else if (operation == Operation.ADD_SENSOR) {
            drawn = new Sensor();
        } else if (operation == Operation.DRAW_FOUNDATION) {
            drawn = new Foundation();
            setGridsVisible(Scene.getInstance().isSnapToGrids());
            drawn.setColor(Scene.getInstance().getDefaultFoundationColor());
        } else if (operation == Operation.ADD_PLANT) {
            drawn = new Tree();
            setGridsVisible(true);
        } else if (operation == Operation.ADD_HUMAN) {
            drawn = new Human();
            setGridsVisible(true);
        } else {
            return null;
        }

        Scene.getInstance().add(drawn, false);
        EventQueue.invokeLater(() -> Scene.getInstance().setEdited(true));
        addPartCommand = new AddPartCommand(drawn);
        return drawn;

    }

    public Operation getOperation() {
        return operation;
    }

    public void setShading(final boolean enable) {
        taskManager.update(() -> {
            lightState.setEnabled(enable);
            root.updateWorldRenderStates(true);
            return null;
        });
    }

    public void setHeliodonVisible(final boolean selected) {
        heliodonControl = selected;
        Heliodon.getInstance().setVisible(selected);
        enableDisableRotationControl();
        EnergyPanel.getInstance().updateRadiationHeatMap();
    }

    public void setSunAnimation(final boolean selected) {
        sunAnim = selected;
    }

    public boolean isSunAnimation() {
        return sunAnim;
    }

    private void enableDisableRotationControl() {
        if (!mouseControlEnabled) {
            return;
        }
        if ((operation == Operation.SELECT || operation == Operation.RESIZE) && (selectedPart == null || selectedPart.isDrawCompleted())) {
            cameraControl.setMouseEnabled(true);
        } else {
            cameraControl.setMouseEnabled(false);
        }
        if (heliodonControl) {
            cameraControl.setKeyRotateSpeed(0);
        } else {
            cameraControl.setKeyRotateSpeed(1);
        }
    }

    public HousePart getSelectedPart() {
        return selectedPart;
    }

    public void setSelectedPart(final HousePart p) {
        if (selectedPart != null) {
            selectedPart.setEditPointsVisible(false);
        }
        selectedPart = p;
        if (selectedPart != null) {
            selectedPart.setEditPointsVisible(true);
        }
    }

    public boolean isTopView() {
        return viewMode == ViewMode.TOP_VIEW;
    }

    void updatePrintPreviewScene(final boolean printPreview) {
        if (printPreview) {
            Scene.saveCameraLocation();
        }
        resetCamera(printPreview ? ViewMode.PRINT_PREVIEW : ViewMode.NORMAL);
        if (!printPreview) {
            Scene.loadCameraLocation();
            if (cameraControl instanceof OrbitControl) {
                ((OrbitControl) cameraControl).clearOrbitCenter();
            }
        }
        backgroundRoot.getSceneHints().setCullHint(printPreview ? CullHint.Always : CullHint.Inherit);
    }

    CameraNode getCameraNode() {
        return cameraNode;
    }

    private Node createEarth() throws IOException {
        final ResourceSource source = ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_MODEL, "earth.dae");
        final ColladaImporter colladaImporter = new ColladaImporter();
        final ColladaStorage storage = colladaImporter.load(source);
        final Node earth = storage.getScene();
        final DirectionalLight light = new DirectionalLight();
        light.setDirection(new Vector3(0, 0, -1));
        light.setEnabled(true);
        final LightState lightState = new LightState();
        lightState.attach(light);
        earth.setRenderState(lightState);
        earth.getSceneHints().setLightCombineMode(LightCombineMode.Replace);
        earth.updateWorldRenderStates(true);
        final Node node = new Node();
        node.setRotation(new Matrix3().fromAngles(-MathUtils.HALF_PI, 0.0, 0.0));
        node.attachChild(earth);
        earth.addController((time, caller) -> {
            final Vector3 direction = getCamera().getDirection().normalize(null);
            direction.setZ(0);
            direction.normalizeLocal();
            double angle = -direction.smallestAngleBetween(Vector3.UNIT_Y);
            if (direction.dot(Vector3.UNIT_X) > 0) {
                angle = -angle;
            }
            angle -= MathUtils.HALF_PI;
            earth.setRotation(new Matrix3().fromAngles(0, 0, angle));
        });
        return node;
    }

    private Node createCompass() throws IOException {
        final ResourceSource source = ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_MODEL, "compass.dae");
        final ColladaImporter colladaImporter = new ColladaImporter();

        // Load the collada scene
        Logger.getLogger(ColladaAnimUtils.class.getName()).setLevel(Level.SEVERE);
        Logger.getLogger(ColladaMaterialUtils.class.getName()).setLevel(Level.SEVERE);
        final ColladaStorage storage = colladaImporter.load(source);
        final Node compass = storage.getScene();
        BMText txt;

        final double Z = 0.1;
        txt = new BMText("N", "N", FontManager.getInstance().getAnnotationFont(), Align.South);
        txt.setTextColor(ColorRGBA.BLACK);
        txt.setAutoRotate(false);
        txt.setTranslation(2, 0.0, Z);
        txt.setRotation(new Matrix3().fromAngles(0.0, MathUtils.HALF_PI, -MathUtils.HALF_PI));
        compass.attachChild(txt);

        txt = new BMText("S", "S", FontManager.getInstance().getAnnotationFont(), Align.South);
        txt.setTextColor(ColorRGBA.BLACK);
        txt.setAutoRotate(false);
        txt.setTranslation(-2, -0.0, Z);
        txt.setRotation(new Matrix3().fromAngles(0.0, -MathUtils.HALF_PI, MathUtils.HALF_PI));
        compass.attachChild(txt);

        txt = new BMText("W", "W", FontManager.getInstance().getAnnotationFont(), Align.South);
        txt.setAutoRotate(false);
        txt.setTranslation(-0.0, 2, Z);
        txt.setRotation(new Matrix3().fromAngles(-MathUtils.HALF_PI, 0.0, 0.0));
        compass.attachChild(txt);

        txt = new BMText("E", "E", FontManager.getInstance().getAnnotationFont(), Align.South);
        txt.setAutoRotate(false);
        txt.setTranslation(-0.0, -2, Z);
        txt.setRotation(new Matrix3().fromAngles(MathUtils.HALF_PI, MathUtils.PI, 0.0));
        compass.attachChild(txt);

        final DirectionalLight light = new DirectionalLight();
        light.setDirection(new Vector3(0, 0, -1));
        light.setEnabled(true);

        final LightState lightState = new LightState();
        lightState.attach(light);
        compass.setRenderState(lightState);
        compass.getSceneHints().setLightCombineMode(LightCombineMode.Replace);

        compass.updateWorldRenderStates(true);

        final Node compassNode = new Node();
        compassNode.setRotation(new Matrix3().fromAngles(-MathUtils.HALF_PI, 0.0, 0.0));
        compassNode.attachChild(compass);
        System.out.println("done");

        compass.addController((time, caller) -> {
            final Vector3 direction = getCamera().getDirection().normalize(null);
            direction.setZ(0);
            direction.normalizeLocal();
            double angle = -direction.smallestAngleBetween(Vector3.UNIT_Y);
            if (direction.dot(Vector3.UNIT_X) > 0) {
                angle = -angle;
            }
            angle -= MathUtils.HALF_PI;
            compass.setRotation(new Matrix3().fromAngles(0.0, 0.0, angle - 0.3));
        });

        return compassNode;
    }

    private void setCompassVisible(final boolean visible) {
        cameraNode.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
    }

    void updateHeliodonAndAnnotationSize() {
        if (heliodonControl) {
            taskManager.update(() -> {
                Heliodon.getInstance().updateSize();
                return null;
            });
        }
    }

    private void mouseMoved() {
        if (!mouseControlEnabled) {
            return;
        }
        final int x = mouseState.getX();
        final int y = mouseState.getY();
        if (Scene.getInstance().getDisableShadowInAction()) {
            if (mouseState.getButtonState(MouseButton.LEFT) == ButtonState.DOWN || mouseState.getButtonState(MouseButton.RIGHT) == ButtonState.DOWN) {
                if (MainPanel.getInstance().getShadowButton().isSelected()) {
                    shadowPass.setEnabled(false);
                }
            } else {
                if (MainPanel.getInstance().getShadowButton().isSelected()) {
                    shadowPass.setEnabled(true);
                }
            }
        }
        try {
            if (selectedPart != null) {
                if (!selectedPart.isDrawCompleted()) {
                    selectedPart.setPreviewPoint(x, y);
                    if (selectedPart instanceof Meshable) { // don't draw grid if it sits on an imported mesh
                        selectedPart.setGridsVisible(((Meshable) selectedPart).getMeshLocator() == null);
                    }
                } else if (objectMoveStartPoint != null) {
                    if ((operation == Operation.RESIZE || selectedPart instanceof Foundation)) {
                        final PickedHousePart pick = SelectUtil.pickPart(x, y, collisionLand);
                        if (pick != null) {
                            if (selectedPart instanceof Foundation) {
                                final Foundation foundation = (Foundation) selectedPart;
                                final Vector3 pickPoint = pick.getPoint().clone();
                                // if (!foundation.insideBuilding(pickPoint.getX(), pickPoint.getY(), true)) { // only move the building when clicking outside
                                final Vector3 d = pickPoint.multiply(1, 1, 0, null).subtractLocal(objectMoveStartPoint.multiply(1, 1, 0, null));
                                if (foundation.isGroupMaster()) {
                                    final List<Foundation> g = Scene.getInstance().getFoundationGroup(foundation);
                                    if (g != null) {
                                        for (final Foundation f : g) {
                                            final ArrayList<Vector3> movePoints = objectGroupMovePoints.get(f);
                                            if (movePoints != null) { // just in case this foundation's move point hasn't been included yet
                                                f.move(d, movePoints);
                                            }
                                        }
                                    }
                                } else {
                                    foundation.move(d, objectMovePoints);
                                }
                            }
                        }
                    } else if (selectedPart instanceof Tree) {
                        final PickedHousePart pick = SelectUtil.pickPart(x, y, collisionLand);
                        if (pick != null) {
                            final Vector3 d = pick.getPoint().multiply(1, 1, 0, null).subtractLocal(objectMoveStartPoint.multiply(1, 1, 0, null));
                            ((Tree) selectedPart).move(d, objectMovePoints);
                        }
                    } else if (selectedPart instanceof Window) {
                        final PickedHousePart pick = SelectUtil.pickPart(x, y, selectedPart.getContainer());
                        if (pick != null) {
                            final Vector3 d = pick.getPoint().subtract(objectMoveStartPoint, null);
                            ((Window) selectedPart).move(d, objectMovePoints);
                        }
                    }
                }
            }
            hoveredPart = null;
            if ((operation == Operation.SELECT || operation == Operation.RESIZE) && mouseState.getButtonState(MouseButton.LEFT) == ButtonState.UP && mouseState.getButtonState(MouseButton.MIDDLE) == ButtonState.UP && mouseState.getButtonState(MouseButton.RIGHT) == ButtonState.UP) {
                final PickedHousePart pickedPart = SelectUtil.selectHousePart(x, y, false);
                pick = pickedPart == null ? null : pickedPart.getUserData();
                final HousePart housePart = pick == null ? null : pick.getHousePart();
                if (pick != null) {
                    hoveredPart = housePart;
                    if (pick.getEditPointIndex() != -1) {
                        lastSelectedEditPointMouseState = mouseState;
                    }
                }
            }
            mouseState = null;
        } catch (final Throwable t) {
            t.printStackTrace();
            BugReporter.report(t);
        }
        // this method is run by the main Energy3D thread, so invoke the Swing code later
        EventQueue.invokeLater(() -> {
            final Component canvasComponent = (Component) canvas;
            if (operation == Operation.ADD_BOX) {
                canvasComponent.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                canvasComponent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                if (!zoomLock && (operation == Operation.SELECT || operation == Operation.RESIZE) && hoveredPart != null) {
                    if (hoveredPart instanceof Tree || hoveredPart instanceof Human) {
                        canvasComponent.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    } else if (hoveredPart instanceof SolarCollector) {
                        if (pick.getEditPointIndex() >= 0) {
                            canvasComponent.setCursor(Cursor.getPredefinedCursor(pick.getEditPointIndex() == 0 ? Cursor.MOVE_CURSOR : Cursor.HAND_CURSOR));
                        }
                    } else {
                        if (pick.getEditPointIndex() == -1) {
                            if (hoveredPart instanceof Window) { // for windows, there is no apparent move point
                                canvasComponent.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                            }
                        }
                    }
                }
            }
        });
    }

    public void setMouseControlEnabled(final boolean enabled) {
        mouseControlEnabled = enabled;
        cameraControl.setMouseEnabled(enabled);
    }

    @Override
    public void init() {
        if (Config.RENDER_MODE != RenderMode.LWJGL) {
            initCamera();
        }
    }

    boolean isShadingEnabled() {
        return lightState.isEnabled();
    }

    public void setShadow(final boolean shadow) {
        taskManager.update(() -> {
            shadowPass.setEnabled(shadow);
            root.updateWorldRenderStates(true);
            return null;
        });
    }

    public boolean isShadowEnabled() {
        return shadowPass.isEnabled();
    }

    public void setZoomLock(final boolean zoomLock) {
        this.zoomLock = zoomLock;
        cameraControl.setLeftButtonAction(zoomLock ? ButtonAction.ZOOM : viewMode == ViewMode.NORMAL ? ButtonAction.ROTATE : ButtonAction.MOVE);
    }

    public boolean isZoomLock() {
        return this.zoomLock;
    }

    public void zoom(final boolean in) {
        cameraControl.zoom(canvas, 0.1, in ? -1 : 1);
    }

    public void refresh() {
        refresh = true;
    }

    public void refreshNow() {
        refresh = true;
        try {
            wait();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void refresh(final double updateDurationInSeconds) {
        refreshTime = frameHandler.getTimer().getTimeInSeconds() + updateDurationInSeconds;
    }

    public MyUndoManager getUndoManager() {
        return undoManager;
    }

    Timer getTimer() {
        return frameHandler.getTimer();
    }

    public Mesh getLand() {
        return land;
    }

    public Mesh getSolarLand() {
        return solarLand;
    }

    public Mesh getGroundImageLand() {
        return groundImageLand;
    }

    // The image that Google Maps API returns is 640x640. That image needs to be rescaled in such a way that one meter wall in a Google Map is exactly same length as one meter in Energy3D.
    // I used the Drydock as a reference to find the correct scale factor, which is roughly 70.11 for Boston, MA,
    // but this factor varies with latitude as Google Maps use the Mercator projection.
    private final static double BOSTON_MAP_SCALE_FACTOR = 70.11;
    private final static double MERCATOR_PROJECTION_SCALE_CONSTANT = BOSTON_MAP_SCALE_FACTOR / Math.cos(Math.toRadians(Heliodon.DEFAULT_LATITUDE));

    private void initGroundImageLand(final double scale) {
        final double d = BOSTON_MAP_SCALE_FACTOR / 0.2 * scale; // 0.2 is the obsolete annotation scale by default, can't call Scene.getSceneScale() yet
        groundImageLand.resize(d, d);
        final OffsetState offsetState = new OffsetState();
        offsetState.setTypeEnabled(OffsetType.Fill, true);
        offsetState.setFactor(2);
        offsetState.setUnits(2);
        groundImageLand.setRenderState(offsetState);
        groundImageLand.updateModelBound();
        groundImageLand.updateWorldBound(true);
    }

    void resizeGroundImageLand(final double scale) {
        final double lat = Scene.getInstance().getGeoLocation() != null ? Math.toRadians(Scene.getInstance().getGeoLocation().getLatitude()) : Math.toRadians(Heliodon.DEFAULT_LATITUDE);
        final double d = MERCATOR_PROJECTION_SCALE_CONSTANT * Math.cos(lat) * scale / Scene.getInstance().getScale();
        groundImageLand.resize(d, d);
        groundImageLand.updateModelBound();
        groundImageLand.updateWorldBound(true);
    }

    public boolean isHeliodonVisible() {
        return heliodonControl;
    }

    // the x and y coordinates come from MouseEvent, not MouseState.
    private void mouseRightClicked(final MouseEvent e) {
        final JPanel cp = MainPanel.getInstance().getCanvasPanel();
        final int x = e.getX();
        final int y = cp.getHeight() - e.getY();
        mouseState = new MouseState(x, y, 0, 0, 0, null, null);
        pickMouseState = mouseState;
        refresh = true;
        taskManager.update(() -> {
            try {
                if (operation == Operation.SELECT || operation == Operation.RESIZE) {
                    final HousePart previousSelectedHousePart = selectedPart;
                    if (mouseState == null) {
                        mouseState = new MouseState(x, y, 0, 0, 0, null, null);
                    }
                    final PickedHousePart pickedHousePart = SelectUtil.selectHousePart(mouseState.getX(), mouseState.getY(), true);
                    final UserData pick = pickedHousePart == null ? null : pickedHousePart.getUserData();
                    selectedPart = pick == null ? null : pick.getHousePart();
                    if (selectedPart instanceof Foundation) {
                        final Foundation foundation = (Foundation) selectedPart;
                        if (foundation.getImportedNodes() != null) { // if this foundation contains any imported node, pick a mesh
                            foundation.pickMesh(x, y);
                        }
                    } else {
                        if (e.isAltDown()) {
                            if (selectedPart instanceof SolarPanel && selectedPart.getContainer() instanceof Rack) { // special case (to be removed later)
                                selectedPart = selectedPart.getContainer();
                            }
                        }
                    }
                    System.out.println("Right-clicked on: (" + mouseState.getX() + ", " + mouseState.getY() + ") " + pick);
                    if (previousSelectedHousePart != null && previousSelectedHousePart != selectedPart) {
                        previousSelectedHousePart.setEditPointsVisible(false);
                        previousSelectedHousePart.setGridsVisible(false);
                        previousSelectedHousePart.setLinePatternVisible(false);
                    }
                    if (selectedPart != null) {
                        selectedPart.complete(); // to undo edit flag set by SelectUtil above. FIXME: This taints the wall's heat map texture
                        if (!PrintController.getInstance().isPrintPreview()) {
                            selectedPart.setEditPointsVisible(true);
                        }
                        EnergyPanel.getInstance().update();
                    }
                    EnergyPanel.getInstance().updateGraphs();
                    EnergyPanel.getInstance().updateProperties();
                    final int mouseStateX = mouseState.getX();
                    final int mouseStateY = mouseState.getY();
                    final boolean pickOnLand = onLand(pickMouseState.getX(), pickMouseState.getY());
                    // seriously, our error log on 6/14/2017 showed that this caused deadlock if not invoked later!
                    EventQueue.invokeLater(() -> {
                        final JPopupMenu popup = PopupMenuFactory.getPopupMenu(e, pickOnLand);
                        if (popup != null) {
                            final JPanel cp1 = MainPanel.getInstance().getCanvasPanel();
                            popup.show(cp1, mouseStateX, cp1.getHeight() - mouseStateY);
                        }
                    });
                }
            } catch (final Throwable t) {
                t.printStackTrace();
                BugReporter.report(t);
            }
            return null;
        });
    }

    private void mousePressed(final MouseState mouseState, final KeyboardState keyboardState) {
        refresh = true;
        taskManager.update(() -> {
            if (zoomLock) {
                return null;
            }
            System.out.println("OPERATION: " + operation);
            try {
                if (operation == Operation.SELECT || operation == Operation.RESIZE || operation == Operation.ROTATE || operation == Operation.GABLE_ROOF) {
                    if (selectedPart == null || selectedPart.isDrawCompleted()) {
                        final HousePart previousSelectedPart = selectedPart;
                        final PickedHousePart pickedPart = SelectUtil.selectHousePart(mouseState.getX(), mouseState.getY(), true);
                        final UserData pick = pickedPart == null ? null : pickedPart.getUserData();
                        if (pick == null) {
                            selectedPart = null;
                        } else {
                            selectedPart = pick.getHousePart();
                        }
                        if (selectedPart != null) {
                            // if (selectedPart.getLockEdit()) {
                            // selectedPart = null;
                            // }
                            if (keyboardState.isDown(Key.LMENU) || keyboardState.isDown(Key.RMENU)) {
                                if (selectedPart instanceof SolarPanel && selectedPart.getContainer() instanceof Rack) { // special case
                                    selectedPart = selectedPart.getContainer();
                                }
                            }
                        }
                        System.out.println("Clicked on: " + pick);
                        if (pick != null && pick.isEditPoint()) {
                            cameraControl.setLeftMouseButtonEnabled(false);
                        }

                        if (operation == Operation.RESIZE) {
                            for (final HousePart p : Scene.getInstance().getParts()) {
                                if (p instanceof Foundation) {
                                    if (p != selectedPart) {
                                        ((Foundation) p).setResizeHouseMode(false);
                                    }
                                }
                            }
                            if (selectedPart != null) {
                                if (selectedPart instanceof Foundation) {
                                    final Foundation foundation = (Foundation) selectedPart;
                                    foundation.setResizeHouseMode(true);
                                } else {
                                    final Foundation foundation = selectedPart.getTopContainer();
                                    if (foundation != null) {
                                        foundation.setResizeHouseMode(true);
                                        setSelectedPart(foundation);
                                    }
                                }
                            }
                        }

                        if (operation == Operation.SELECT || operation == Operation.ROTATE) {
                            if (previousSelectedPart instanceof Foundation) {
                                final Foundation foundation = (Foundation) previousSelectedPart;
                                foundation.updateAzimuthArrowVisibility(false);
                            }
                            if (selectedPart instanceof Foundation) {
                                final Foundation foundation = (Foundation) selectedPart;
                                foundation.drawAzimuthArrow();
                                foundation.pickMesh(mouseState.getX(), mouseState.getY());
                            }
                            if (selectedPart != null) {
                                final Foundation foundationOfSelectedPart = selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer();
                                if (foundationOfSelectedPart != null) {
                                    foundationOfSelectedPart.setMovePointsVisible(true);
                                }
                            }
                        }
                        if (operation == Operation.RESIZE && selectedPart != null) {
                            if (!(selectedPart instanceof Foundation)) {
                                selectedPart.setEditPointsVisible(false);
                                selectedPart = selectedPart.getTopContainer();
                            }
                        }
                        if (selectedPart instanceof Window || selectedPart instanceof Tree || (selectedPart instanceof Foundation && pick.getEditPointIndex() != -1)) {
                            cameraControl.setLeftMouseButtonEnabled(false);
                            objectMoveStartPoint = pickedPart.getPoint().clone();
                            collisionLand.setTranslation(0, 0, objectMoveStartPoint.getZ());
                            final ArrayList<Vector3> points = selectedPart.getPoints();
                            if (objectMovePoints == null) {
                                objectMovePoints = new ArrayList<>();
                            } else {
                                objectMovePoints.clear();
                            }
                            for (final Vector3 p : points) {
                                objectMovePoints.add(p.clone());
                            }
                            if (selectedPart instanceof Foundation) {
                                final Foundation f = (Foundation) selectedPart;
                                if (f.isGroupMaster()) {
                                    final List<Foundation> g = Scene.getInstance().getFoundationGroup(f);
                                    if (g != null && !g.isEmpty()) {
                                        if (objectGroupMovePoints == null) {
                                            objectGroupMovePoints = new HashMap<>();
                                        } else {
                                            objectGroupMovePoints.clear();
                                        }
                                        for (final Foundation a : g) {
                                            final ArrayList<Vector3> b = new ArrayList<>();
                                            objectGroupMovePoints.put(a, b);
                                            for (final Vector3 p : a.getPoints()) {
                                                b.add(p.clone());
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (previousSelectedPart != null && previousSelectedPart != selectedPart && operation != Operation.RESIZE) {
                            previousSelectedPart.setEditPointsVisible(false);
                            previousSelectedPart.setGridsVisible(false);
                            previousSelectedPart.setLinePatternVisible(false);
                            final Foundation foundationOfPreviousSelectedPart = previousSelectedPart instanceof Foundation ? (Foundation) previousSelectedPart : previousSelectedPart.getTopContainer();
                            if (foundationOfPreviousSelectedPart != null) {
                                if (selectedPart == null) {
                                    foundationOfPreviousSelectedPart.setMovePointsVisible(false);
                                } else if (foundationOfPreviousSelectedPart != (selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer())) {
                                    foundationOfPreviousSelectedPart.setMovePointsVisible(false);
                                }
                                foundationOfPreviousSelectedPart.clearSelectedMesh();
                                foundationOfPreviousSelectedPart.setResizeHouseMode(false);
                            }
                        }
                        if (selectedPart != null && !PrintController.getInstance().isPrintPreview()) {
                            selectedPart.setEditPointsVisible(true);
                            if (pick.isEditPoint() && pick.getEditPointIndex() != -1 || operation == Operation.RESIZE || selectedPart instanceof Window || selectedPart instanceof Tree) {
                                if (Scene.getInstance().isSnapToGrids()) {
                                    selectedPart.setGridsVisible(true);
                                } else {
                                    selectedPart.setLinePatternVisible(true);
                                }
                                if (selectedPart instanceof Foundation) {
                                    editPartCommand = new EditFoundationCommand((Foundation) selectedPart, !pick.isEditPoint());
                                } else if (selectedPart instanceof Rack) {
                                    editPartCommand = new EditRackCommand((Rack) selectedPart);
                                } else if (selectedPart instanceof ParabolicTrough) {
                                    editPartCommand = new EditParabolicTroughCommand((ParabolicTrough) selectedPart);
                                } else {
                                    editPartCommand = new EditPartCommand(selectedPart);
                                }
                            }
                        }
                        SelectUtil.nextPickLayer();
                        if (operation == Operation.GABLE_ROOF && selectedPart instanceof Roof) {
                            System.out.println("deleting roof #" + pick.getEditPointIndex());
                            final int roofPartIndex = pick.getEditPointIndex();
                            final Roof roof = (Roof) selectedPart;
                            roof.setGable(roofPartIndex, true, undoManager);
                        }
                    }
                } else {
                    if (selectedPart != null) { // selected part can be null in modes other than specified in the if clause
                        selectedPart.addPoint(mouseState.getX(), mouseState.getY());
                    }
                }
            } catch (final Throwable t) {
                t.printStackTrace();
                BugReporter.report(t);
            }
            return null;
        });

    }

    private void mouseReleased(final MouseState mouseState) {
        refresh = true;
        pickMouseState = mouseState; // this method is not called when mouse is right-clicked, so we have to set the pick mouse state here
        taskManager.update(() -> {
            try {
                if (operation == Operation.ADD_BOX) {
                    final AddMultiplePartsCommand cmd = Scene.getInstance().importFile(MainApplication.class.getResource("prefabs/box.ng3"));
                    if (cmd != null) {
                        Foundation foundation = null;
                        final List<HousePart> parts = cmd.getParts();
                        for (final HousePart p : parts) {
                            if (p instanceof Foundation) {
                                foundation = (Foundation) p;
                                break;
                            }
                        }
                        if (foundation != null) {
                            setSelectedPart(foundation);
                            if (isTooFar(foundation)) {
                                cmd.undo();
                                EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(MainFrame.getInstance(), "This position was not allowed because it was too far from the center.", "Illegal position", JOptionPane.WARNING_MESSAGE));
                            }
                        }
                    }
                    if (!operationStick) {
                        MainPanel.getInstance().defaultTool();
                        cameraControl.setLeftMouseButtonEnabled(true);
                    }
                } else {
                    if (selectedPart != null) {
                        selectedPart.setGridsVisible(false);
                        selectedPart.setLinePatternVisible(false);
                    }
                    if (operation == Operation.SELECT || operation == Operation.RESIZE) {
                        if (selectedPart != null && (!selectedPart.isDrawCompleted() || objectMoveStartPoint != null)) {
                            if (selectedPart.isDrawable()) {
                                selectedPart.complete();
                                if (editPartCommand != null && editPartCommand.isReallyEdited()) {
                                    if (SceneManager.getInstance().getSolarHeatMap()) {
                                        EnergyPanel.getInstance().updateRadiationHeatMap();
                                    }
                                }
                            } else {
                                if (editPartCommand != null) {
                                    editPartCommand.undo();
                                    selectedPart.setHighlight(false);
                                    selectedPart.complete();
                                }
                            }
                            if (editPartCommand != null) {
                                if (isTooFar(selectedPart)) {
                                    editPartCommand.undo();
                                    final String name = selectedPart.getClass().getSimpleName() + " (" + selectedPart.getId() + ")";
                                    EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(MainFrame.getInstance(), "Moving " + name + " was not allowed because it was moved too far from the center.", "Illegal position", JOptionPane.WARNING_MESSAGE));
                                } else {
                                    if (editPartCommand.isReallyEdited()) {
                                        undoManager.addEdit(editPartCommand);
                                    }
                                }
                                editPartCommand = null;
                            }
                        }
                        if (!zoomLock) {
                            cameraControl.setLeftMouseButtonEnabled(true);
                        }
                        objectMoveStartPoint = null;
                        if (objectMovePoints != null) {
                            objectMovePoints.clear();
                        }
                        if (objectGroupMovePoints != null) {
                            objectGroupMovePoints.clear();
                        }
                        if (cameraChanged) {
                            TimeSeriesLogger.getInstance().logCamera(zoomLock ? "Zoom" : "Rotate");
                            cameraChanged = false;
                        }
                    } else {
                        if (selectedPart != null && !selectedPart.isDrawCompleted()) {
                            selectedPart.addPoint(mouseState.getX(), mouseState.getY());
                            if (selectedPart.isDrawCompleted() && !selectedPart.isDrawable()) {
                                addPartCommand = null;
                                Scene.getInstance().remove(selectedPart, true);
                                selectedPart = null;
                                if (operationStick) {
                                    operationFlag = true;
                                }
                            }
                        }
                        if (selectedPart != null && selectedPart.isDrawCompleted()) {
                            final boolean drawCompletedOrg = selectedPart.isDrawCompleted();
                            selectedPart.setDrawCompleted(false); // because solar panels.isDrawble() only works if incomplete
                            if (selectedPart.isDrawable()) {
                                selectedPart.setDrawCompleted(drawCompletedOrg);
                                if (addPartCommand != null) {
                                    boolean addSuccess = true;
                                    if (isTooFar(selectedPart)) { // prevent an object to be placed at a very far position
                                        Scene.getInstance().remove(selectedPart, true);
                                        final String name = selectedPart.getClass().getSimpleName() + " (" + selectedPart.getId() + ")";
                                        EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(MainFrame.getInstance(),
                                                "Adding " + name + " was not allowed because it was placed too far from the center.", "Illegal position", JOptionPane.WARNING_MESSAGE));
                                        addSuccess = false;
                                    } else if (selectedPart instanceof Rack) {
                                        Rack rack = (Rack) selectedPart;
                                        if (rack.outOfBound()) {
                                            Scene.getInstance().remove(selectedPart, true);
                                            final String name = selectedPart.getClass().getSimpleName() + " (" + selectedPart.getId() + ")";
                                            EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(MainFrame.getInstance(),
                                                    "Adding " + name + " was not allowed because it would not be completely inside the underlying surface.", "Illegal position", JOptionPane.WARNING_MESSAGE));
                                            addSuccess = false;
                                        } else {
                                            if (rack.getContainer() instanceof Roof || rack.getContainer() instanceof Wall) {
                                                rack.setPoleHeight(0.5 / Scene.getInstance().getScale());
                                                rack.draw();
                                            }
                                        }
                                    } else if (selectedPart instanceof SolarPanel) {
                                        SolarPanel panel = (SolarPanel) selectedPart;
                                        if (panel.outOfBound()) {
                                            Scene.getInstance().remove(selectedPart, true);
                                            final String name = selectedPart.getClass().getSimpleName() + " (" + selectedPart.getId() + ")";
                                            EventQueue.invokeLater(() -> JOptionPane.showMessageDialog(MainFrame.getInstance(),
                                                    "Adding " + name + " was not allowed because it would not be completely inside the underlying surface.", "Illegal position", JOptionPane.WARNING_MESSAGE));
                                            addSuccess = false;
                                        } else {
                                            if (panel.getContainer() instanceof Roof || panel.getContainer() instanceof Wall) {
                                                panel.setPoleHeight(0.5 / Scene.getInstance().getScale());
                                                panel.draw();
                                            }
                                        }
                                    }
                                    if (addSuccess) {
                                        undoManager.addEdit(addPartCommand);
                                        // only when we add a new foundation do we ensure the order of its points (later a foundation can be rotated, altering the order)
                                        if (selectedPart instanceof Foundation) {
                                            ((Foundation) selectedPart).ensureFoundationPointOrder();
                                        }
                                    }
                                }
                                addPartCommand = null;
                            } else {
                                Scene.getInstance().remove(selectedPart, true);
                            }
                            selectedPart.setEditPointsVisible(false);
                            selectedPart = null;
                            if (operationStick) {
                                operationFlag = true;
                            }
                            if (SceneManager.getInstance().getSolarHeatMap()) {
                                EnergyPanel.getInstance().updateRadiationHeatMap();
                            }
                        }
                        if (!operationFlag) {
                            MainPanel.getInstance().defaultTool();
                            cameraControl.setLeftMouseButtonEnabled(true);
                        }
                    }
                    updateHeliodonAndAnnotationSize();
                    if (selectedPart instanceof Foundation) {
                        final Foundation foundation = (Foundation) selectedPart;
                        if (!foundation.getLockEdit()) {
                            Scene.getInstance().updateTrackables(foundation);
                        }
                    } else if (selectedPart instanceof Rack) {
                        ((Rack) selectedPart).ensureFullSolarPanels(true);
                    } else if (selectedPart instanceof ParabolicTrough) {
                        ((ParabolicTrough) selectedPart).ensureFullModules(true);
                    } else if (selectedPart instanceof FresnelReflector) {
                        ((FresnelReflector) selectedPart).ensureFullModules(true);
                    }
                }
                EnergyPanel.getInstance().update();
            } catch (final Throwable t) {
                t.printStackTrace();
                BugReporter.report(t);
            }
            return null;
        });
        EnergyPanel.getInstance().updateGraphs();

    }

    private boolean isTooFar(final HousePart p) {
        if (isTopView()) {
            return false;
        }
        final double rc = p.getAbsCenter().length();
        final Rectangle2D bounds = Scene.getInstance().getFoundationBounds(true);
        double rmax = SKY_RADIUS / 1000;
        if (!Util.isZero(bounds.getWidth() - 0.2) || !Util.isZero(bounds.getHeight() - 0.2)) {
            final double b = 5 * (bounds.getWidth() + bounds.getHeight());
            if (b > rmax) {
                rmax = b;
            }
        }
        return rc > rmax;
    }

    public void grabOrRelease() {
        if (selectedPart != null && !selectedPart.isDrawCompleted()) {
            mouseReleased(lastSelectedEditPointMouseState);
        } else {
            mousePressed(lastSelectedEditPointMouseState, null);
        }
    }

    public void deleteCurrentSelection() {
        if (selectedPart == null || selectedPart.getLockEdit() || (selectedPart.getTopContainer() != null && selectedPart.getTopContainer().getLockEdit() && !(selectedPart instanceof SolarCollector))) {
            return;
        }
        if (selectedPart instanceof Foundation) {
            final Foundation foundation = (Foundation) selectedPart;
            if (!foundation.getChildren().isEmpty() && foundation.getSelectedMesh() == null) {
                if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Deleting the foundation also deletes all the objects on it. Are you sure?", "Confirmation", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
            }
        }
        taskManager.update(() -> {
            if (selectedPart != null) { // sometimes selectedPart can be null after the callable is sent to the task manager
                if (selectedPart instanceof Foundation && ((Foundation) selectedPart).getSelectedMesh() != null) { // a mesh is selected, instead of a part
                    final Foundation f = (Foundation) selectedPart;
                    final Mesh m = f.getSelectedMesh();
                    if (m != null) {
                        f.deleteMesh(m);
                    }
                } else {
                    final RemovePartCommand c = new RemovePartCommand(selectedPart);
                    if (selectedPart instanceof Wall) { // undo/redo a gable roof
                        final Roof roof = ((Wall) selectedPart).getRoof();
                        if (roof != null) {
                            final List<Map<Integer, List<Wall>>> gableInfo = new ArrayList<>();
                            if (roof.getGableEditPointToWallMap() != null) {
                                gableInfo.add(roof.getGableEditPointToWallMap());
                            }
                            c.setGableInfo(gableInfo);
                        }
                    } else if (selectedPart instanceof Foundation) { // undo/redo all the gable roofs
                        final List<Roof> roofs = ((Foundation) selectedPart).getRoofs();
                        if (!roofs.isEmpty()) {
                            final List<Map<Integer, List<Wall>>> gableInfo = new ArrayList<>();
                            for (final Roof r : roofs) {
                                if (r.getGableEditPointToWallMap() != null) {
                                    gableInfo.add(r.getGableEditPointToWallMap());
                                }
                            }
                            c.setGableInfo(gableInfo);
                        }
                    }
                    undoManager.addEdit(c);
                    Scene.getInstance().remove(selectedPart, true);
                }
                if (selectedPart.getContainer() != null) { // redraw its container since we are not calling the costly redrawAll any more
                    selectedPart.getContainer().draw();
                }
                selectedPart = null;
                EventQueue.invokeLater(() -> MainPanel.getInstance().getEnergyButton().setSelected(false));
            }
            return null;
        });
    }

    public Camera getCamera() {
        return canvas.getCanvasRenderer().getCamera();
    }

    public boolean getSolarHeatMap() {
        return solarHeatMap;
    }

    public void setSolarHeatMapWithoutUpdate(final boolean solarHeatMap) {
        this.solarHeatMap = solarHeatMap;
    }

    public void setSolarHeatMap(final boolean solarHeatMap) {
        setSolarHeatMapWithoutUpdate(solarHeatMap);
        EnergyPanel.getInstance().clearAlreadyRendered();
        EnergyPanel.getInstance().updateRadiationHeatMap();
        // For some reason, rack's texture doesn't change correctly until it is redrawn. Rack has to change between a repeated texture (solar panel) and a single texture (solar radiation)
        taskManager.update(() -> {
            for (final SolarPanel panel : Scene.getInstance().getAllSolarPanels()) { // draw the cell outlines
                panel.draw();
            }
            for (final Rack rack : Scene.getInstance().getAllRacks()) { // draw the panel outlines
                rack.draw();
            }
            if (!solarHeatMap) { // reset the mesh colors after the heat map goes off
                final List<Foundation> foundations = Scene.getInstance().getAllFoundations();
                for (final Foundation f : foundations) {
                    f.resetImportedMeshColors();
                    f.draw();
                }
            }
            return null;
        });
    }

    public boolean isHeatFluxDaily() {
        return heatFluxDaily;
    }

    public void setHeatFluxDaily(final boolean heatFluxDaily) {
        this.heatFluxDaily = heatFluxDaily;
    }

    public void setAxesVisible(final boolean b) {
        if (b) {
            backgroundRoot.attachChild(axes);
        } else {
            backgroundRoot.detachChild(axes);
        }
    }

    public boolean areAxesVisible() {
        return backgroundRoot.hasChild(axes);
    }

    public void setHeatFluxVectorsVisible(final boolean b) {
        showHeatFlux = b;
        for (final HousePart part : Scene.getInstance().getParts()) {
            part.updateHeatFluxVisibility();
        }
    }

    public boolean areHeatFluxVectorsVisible() {
        return showHeatFlux;
    }

    /**
     * negative angle for clockwise rotation, positive angle for counter-clockwise rotation
     */
    public void rotateFoundation(final double angle, final boolean redraw) {
        if (selectedPart != null) {
            Foundation f;
            if (selectedPart instanceof Foundation) {
                f = (Foundation) selectedPart;
            } else {
                f = selectedPart.getTopContainer();
            }
            if (!f.getLockEdit()) {
                if (f.isGroupMaster()) {
                    final List<Foundation> g = Scene.getInstance().getFoundationGroup(f);
                    if (g != null) {
                        final Vector3 center = f.toRelative(f.getCenter().clone());
                        for (final Foundation x : g) {
                            x.rotate(angle, center, true);
                        }
                    }
                } else {
                    f.rotate(angle, null, true);
                }
                if (redraw) {
                    Scene.getInstance().redrawFoundationNow(f);
                }
            }
        }
    }

    /**
     * negative angle for clockwise rotation, positive angle for counter-clockwise rotation
     */
    public void rotateAllFoundations(final double angle) {
        final Vector3 origin = new Vector3();
        for (final HousePart p : Scene.getInstance().getParts()) {
            if (p instanceof Foundation) {
                final Foundation f = (Foundation) p;
                if (!f.getLockEdit()) {
                    f.rotate(angle, origin, true);
                }
            }
        }
        Scene.getInstance().redrawAll();
    }

    public void rotate(final double angle) {
        if (SceneManager.getInstance().getSolarHeatMap()) {
            EnergyPanel.getInstance().updateRadiationHeatMap();
        }
        taskManager.update(() -> {
            final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
            if (selectedPart instanceof Foundation) {
                final RotateBuildingCommand c = new RotateBuildingCommand((Foundation) selectedPart, angle);
                SceneManager.getInstance().rotateFoundation(angle, true);
                SceneManager.getInstance().getUndoManager().addEdit(c);
            } else if (selectedPart instanceof SolarPanel) {
                final SolarPanel solarPanel = (SolarPanel) selectedPart;
                final ChangeAzimuthCommand c = new ChangeAzimuthCommand(solarPanel);
                solarPanel.setRelativeAzimuth(solarPanel.getRelativeAzimuth() + Math.toDegrees(angle));
                solarPanel.draw();
                SceneManager.getInstance().getUndoManager().addEdit(c);
            } else if (selectedPart instanceof Rack) {
                final Rack rack = (Rack) selectedPart;
                final ChangeAzimuthCommand c = new ChangeAzimuthCommand(rack);
                rack.setRelativeAzimuth(rack.getRelativeAzimuth() + Math.toDegrees(angle));
                rack.draw();
                SceneManager.getInstance().getUndoManager().addEdit(c);
            } else if (selectedPart instanceof Mirror) {
                final Mirror mirror = (Mirror) selectedPart;
                final ChangeAzimuthCommand c = new ChangeAzimuthCommand(mirror);
                mirror.setRelativeAzimuth(mirror.getRelativeAzimuth() + Math.toDegrees(angle));
                mirror.draw();
                SceneManager.getInstance().getUndoManager().addEdit(c);
            } else if (selectedPart == null) {
                final RotateBuildingCommand c = new RotateBuildingCommand(null, angle);
                rotateAllFoundations(angle);
                SceneManager.getInstance().getUndoManager().addEdit(c);
            }
            EventQueue.invokeLater(() -> EnergyPanel.getInstance().updateProperties());
            return null;
        });
    }

    private boolean onLand(final int x, final int y) {
        return SelectUtil.pickPart(x, y, land) != null;
    }

    Vector3 getPickedLocationOnLand() {
        if (pickMouseState != null) {
            final PickedHousePart pick = SelectUtil.pickPart(pickMouseState.getX(), pickMouseState.getY(), land);
            if (pick != null) {
                return pick.getPoint().multiply(1, 1, 0, null);
            }
            pickMouseState = null;
        }
        return null;
    }

    public Vector3 getPickedLocationOnFoundation() {
        if (pickMouseState != null) {
            final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
            if (selectedPart instanceof Foundation) {
                final PickedHousePart pick = SelectUtil.pickPart(pickMouseState.getX(), pickMouseState.getY(), selectedPart);
                if (pick != null) {
                    return pick.getPoint().clone();
                }
            }
            pickMouseState = null;
        }
        return null;
    }

    Vector3 getPickedLocationOnWall() {
        if (pickMouseState != null) {
            final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
            if (selectedPart instanceof Wall) {
                final PickedHousePart pick = SelectUtil.pickPart(pickMouseState.getX(), pickMouseState.getY(), selectedPart);
                if (pick != null) {
                    return pick.getPoint().clone();
                }
            }
            pickMouseState = null;
        }
        return null;
    }

    Vector3 getPickedLocationOnRoof() {
        if (pickMouseState != null) {
            final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
            if (selectedPart instanceof Roof) {
                final PickedHousePart pick = SelectUtil.pickPart(pickMouseState.getX(), pickMouseState.getY(), selectedPart);
                if (pick != null) {
                    return pick.getPoint().clone();
                }
            }
            pickMouseState = null;
        }
        return null;
    }

    Vector3 getPickedLocationOnFloor() {
        if (pickMouseState != null) {
            final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
            if (selectedPart instanceof Floor) {
                final PickedHousePart pick = SelectUtil.pickPart(pickMouseState.getX(), pickMouseState.getY(), selectedPart);
                if (pick != null) {
                    return pick.getPoint().clone();
                }
            }
            pickMouseState = null;
        }
        return null;
    }

    Vector3 getPickedLocationOnRack() {
        if (pickMouseState != null) {
            final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
            if (selectedPart instanceof Rack) {
                final PickedHousePart pick = SelectUtil.pickPart(pickMouseState.getX(), pickMouseState.getY(), selectedPart);
                if (pick != null) {
                    return pick.getPoint().clone();
                }
            }
            pickMouseState = null;
        }
        return null;
    }

    Vector3 getPickedLocationOnMesh(final Mesh mesh) {
        if (pickMouseState != null) {
            final PickedHousePart pick = SelectUtil.pickPart(pickMouseState.getX(), pickMouseState.getY(), mesh);
            if (pick != null) {
                return pick.getPoint().clone();
            }
            pickMouseState = null;
        }
        return null;
    }

    public void computeEnergyView(final boolean b) {
        setHeatFluxDaily(true);
        setSolarHeatMap(b);
        setHeatFluxVectorsVisible(b);
        ((Component) canvas).requestFocusInWindow();
    }

    public Foundation autoSelectBuilding(final boolean ask) {
        Foundation foundation = null;
        final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
        if (selectedPart == null || selectedPart instanceof Tree || selectedPart instanceof Human) {
            SceneManager.getInstance().setSelectedPart(null);
            int count = 0;
            HousePart hp = null;
            for (final HousePart x : Scene.getInstance().getParts()) {
                if (x instanceof Foundation) {
                    count++;
                    hp = x;
                }
            }
            if (count == 1) {
                SceneManager.getInstance().setSelectedPart(hp);
                foundation = (Foundation) hp;
            } else {
                if (ask) {
                    if (count > 1) {
                        JOptionPane.showMessageDialog(MainFrame.getInstance(), "There are multiple buildings. You must select a building first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no building.", "No Building", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        } else {
            final Foundation topContainer = selectedPart.getTopContainer();
            if (selectedPart instanceof Foundation) {
                foundation = (Foundation) selectedPart;
            } else if (topContainer != null) {
                selectedPart.setEditPointsVisible(false);
                SceneManager.getInstance().setSelectedPart(topContainer);
                foundation = topContainer;
            } else {
                if (ask) {
                    JOptionPane.showMessageDialog(MainFrame.getInstance(), "You must select a building first.", "No Selection", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
        return foundation;
    }

    public void setFineGrid(final boolean b) {
        fineGrid = b;
    }

    public boolean isFineGrid() {
        return fineGrid;
    }

    public BasicPassManager getPassManager() {
        return passManager;
    }

    public static void setExecuteAllTask(final boolean executeAllTask) {
        SceneManager.executeAllTask = executeAllTask;
    }

    // make sure that mouse states are nullified in case they trigger inconsistent actions when mouse moves or clicks at the wrong time (e.g., while loading)
    void clearMouseState() {
        mouseState = null;
        pickMouseState = null;
    }

    public void cursorWait(final boolean on) {
        if (EventQueue.isDispatchThread()) {
            ((Component) canvas).setCursor(Cursor.getPredefinedCursor(on ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        } else {
            EventQueue.invokeLater(() -> ((Component) canvas).setCursor(Cursor.getPredefinedCursor(on ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR)));
        }
    }

}