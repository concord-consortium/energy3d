package org.concord.energy3d.scene;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.EnergyPanel.UpdateRadiation;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.gui.PopupMenuFactory;
import org.concord.energy3d.logger.PlayControl;
import org.concord.energy3d.model.CustomRoof;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HipRoof;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.PickedHousePart;
import org.concord.energy3d.model.PyramidRoof;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.ShedRoof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.UserData;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.CameraControl.ButtonAction;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.undo.AddPartCommand;
import org.concord.energy3d.undo.EditFoundationCommand;
import org.concord.energy3d.undo.EditPartCommand;
import org.concord.energy3d.undo.RemovePartCommand;
import org.concord.energy3d.undo.UndoManager;
import org.concord.energy3d.util.Blinker;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.Config.RenderMode;
import org.concord.energy3d.util.FontManager;
import org.concord.energy3d.util.SelectUtil;
import org.concord.energy3d.util.Util;

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
import com.ardor3d.framework.jogl.JoglSwingCanvas;
import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.image.util.awt.AWTImageLoader;
import com.ardor3d.input.ButtonState;
import com.ardor3d.input.FocusWrapper;
import com.ardor3d.input.Key;
import com.ardor3d.input.KeyboardState;
import com.ardor3d.input.KeyboardWrapper;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.MouseState;
import com.ardor3d.input.MouseWrapper;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyHeldCondition;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.KeyReleasedCondition;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.input.logical.MouseButtonClickedCondition;
import com.ardor3d.input.logical.MouseButtonPressedCondition;
import com.ardor3d.input.logical.MouseButtonReleasedCondition;
import com.ardor3d.input.logical.MouseMovedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.light.DirectionalLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector2;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Camera.ProjectionMode;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.pass.BasicPassManager;
import com.ardor3d.renderer.pass.RenderPass;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.extension.CameraNode;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.Dome;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.scenegraph.shape.Sphere;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.Align;
import com.ardor3d.util.GameTaskQueue;
import com.ardor3d.util.GameTaskQueueManager;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.Timer;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.ResourceSource;
import com.ardor3d.util.resource.SimpleResourceLocator;
import com.ardor3d.util.resource.URLResourceSource;

public class SceneManager implements com.ardor3d.framework.Scene, Runnable, Updater {

	public enum Operation {
		SELECT, RESIZE, ROTATE, DRAW_WALL, DRAW_DOOR, DRAW_ROOF_PYRAMID, DRAW_ROOF_HIP, DRAW_ROOF_SHED, DRAW_WINDOW, DRAW_FOUNDATION, DRAW_FLOOR, DRAW_ROOF_CUSTOM, DRAW_ROOF_GABLE, DRAW_SOLAR_PANEL, DRAW_SENSOR, DRAW_DOGWOOD, DRAW_ELM, DRAW_OAK, DRAW_LINDEN, DRAW_MAPLE, DRAW_COTTONWOOD, DRAW_PINE, DRAW_JANE, DRAW_JENI, DRAW_JILL, DRAW_JACK, DRAW_JOHN, DRAW_JOSE
	}

	public enum CameraMode {
		ORBIT, FIRST_PERSON
	}

	public enum ViewMode {
		NORMAL, TOP_VIEW, PRINT_PREVIEW, PRINT
	}

	private static final GameTaskQueueManager taskManager = GameTaskQueueManager.getManager("Task Manager");
	private static final SceneManager instance = new SceneManager(MainPanel.getInstance().getCanvasPanel());
	private static final double MOVE_SPEED = 5;
	private final Canvas canvas;
	private final FrameHandler frameHandler;
	private final LogicalLayer logicalLayer;
	private final Node root = new Node("Root");
	private final Node backgroundRoot = new Node("Scenary Root");
	private final BasicPassManager passManager = new BasicPassManager();
	private final Mesh land = new Quad("Land", 2000, 2000);
	private final Mesh solarLand = new Quad("Solar Land", 256, 256);
	private final Mesh collisionLand = new Quad("Collision Land", 2000, 2000);
	private final Mesh gridsMesh = new Line("Floor Grids");
	private final LightState lightState = new LightState();
	private final UndoManager undoManager = new UndoManager();
	private HousePart selectedHousePart = null;
	private HousePart hoveredHousePart = null;
	private Operation operation = Operation.SELECT;
	private CameraControl cameraControl;
	private final ParallelSplitShadowMapPass shadowPass;
	private ViewMode viewMode = ViewMode.NORMAL;
	private CameraNode cameraNode;
	private MouseState mouseState;
	private AddPartCommand addHousePartCommand;
	private EditPartCommand editHousePartCommand;
	private UserData pick;
	private TwoInputStates firstClickState;
	private double refreshTime = -1;
	private boolean mouseControlEnabled = true;
	private boolean rotAnim = false;
	private boolean heliodonControl;
	private boolean sunAnim;
	private boolean operationStick = false;
	private boolean operationFlag = false;
	private boolean refresh = true;
	private int refreshCount = 0;
	private boolean refreshOnlyMode = false;
	private boolean zoomLock = false;

	public static final double SKY_RADIUS = 1000;
	public final static byte DEFAULT_THEME = 0;
	public final static byte SKETCHUP_THEME = 1;
	private final byte theme = DEFAULT_THEME;

	private Sphere kinectPointer;
	private MouseState lastSelectedEditPointMouseState;
	private MouseState pasteMouseState;
	private Node newImport;
	private Vector3 houseMoveStartPoint;
	private ArrayList<Vector3> houseMovePoints;
	private boolean solarHeatMap = false;
	private boolean heatFluxDaily = true;
	private final Spatial axes;
	private boolean showBuildingLabels = false;
	private boolean showHeatFlux = false;
	private final Mesh sky;
	private TextureState daySkyState, nightSkyState;
	private double buildingRotationAngleRecorded;

	public static SceneManager getInstance() {
		return instance;
	}

	public static GameTaskQueueManager getTaskManager() {
		return taskManager;
	}

	private SceneManager(final Container panel) {
		System.out.print("Constructing SceneManager...");
		final DisplaySettings settings = new DisplaySettings(400, 300, 24, 0, 0, 24, 0, 4, false, false);

		final RendererFactory rendererFactory;
		if (Config.RENDER_MODE == RenderMode.NEWT)
			rendererFactory = new JoglNewtFactory(settings, this);
		else if (Config.RENDER_MODE == RenderMode.JOGL)
			rendererFactory = new JoglFactory(settings, this);
		else
			rendererFactory = new LwjglFactory(settings, this);

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
				if (Heliodon.getInstance() != null)
					Heliodon.getInstance().updateBloom();
			}
		});
		panel.add(canvasComponent, BorderLayout.CENTER);
		System.out.println("done");
		System.out.print("Initializing SceneManager...");
		AWTImageLoader.registerLoader();
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

		backgroundRoot.getSceneHints().setAllPickingHints(false);
		sky = createSky();
		backgroundRoot.attachChild(sky);
		backgroundRoot.attachChild(createLand());
		solarLand.setVisible(false);
		backgroundRoot.attachChild(solarLand);
		collisionLand.setModelBound(new BoundingBox());
		collisionLand.getSceneHints().setCullHint(CullHint.Always);
		root.attachChild(collisionLand);
		gridsMesh.getSceneHints().setCullHint(CullHint.Always);
		drawGrids(5);
		backgroundRoot.attachChild(gridsMesh);
		axes = createAxes();
		backgroundRoot.attachChild(axes);
		backgroundRoot.attachChild(createKinectPointer());
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
		// shadowPass.add(Scene.getRoot());
		shadowPass.add(Scene.getOriginalHouseRoot());
		// shadowPass.add(Scene.getTreesRoot());
		// shadowPass.addOccluder(Scene.getRoot());
		shadowPass.addOccluder(Scene.getOriginalHouseRoot());
		shadowPass.addOccluder(Scene.getNotReceivingShadowRoot());

		final Date today = Calendar.getInstance().getTime();
		new Heliodon(root, light, passManager, logicalLayer, today);

		// taskManager.update(new Callable<Object>() {
		// @Override
		// public Object call() throws Exception {
		// final Date today = Calendar.getInstance().getTime();
		// new Heliodon(root, light, passManager, logicalLayer, today);
		// return null;
		// }
		// });

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
			final boolean doRefresh = refresh || !refreshOnlyMode && (isTaskAvailable || isPrintPreviewAnim || Scene.isRedrawAll() || isUpdateTime || rotAnim || Blinker.getInstance().getTarget() != null || sunAnim || (cameraControl != null && cameraControl.isAnimating()));
			if (doRefresh || refreshCount > 0) {
				if (now > refreshTime)
					refreshTime = -1;
				refresh = false;
				if (doRefresh)
					refreshCount = 2;
				else
					refreshCount--;
				try {
					synchronized (this) {
						frameHandler.updateFrame();
					}
				} catch (final Throwable e) {
					e.printStackTrace();
					if (shadowPass.isEnabled()) {
						JOptionPane.showMessageDialog(MainPanel.getInstance(), "Your video card driver does not support shadows! Updating your video card drivers may fix this issue. Shadow rendering will be disabled now.", "Warning", JOptionPane.WARNING_MESSAGE);
						shadowPass.setEnabled(false);
					} else {
						Util.reportError(e);
						return;
					}
				}
				synchronized (this) {
					notifyAll();
				}
			} else
				frameHandler.getTimer().update();

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
		taskManager.getQueue(GameTaskQueue.UPDATE).setExecuteMultiple(true);
		taskManager.getQueue(GameTaskQueue.UPDATE).execute(canvas.getCanvasRenderer().getRenderer());

		if (operationFlag)
			executeOperation();

		if (mouseState != null)
			mouseMoved();

		if (Scene.isRedrawAll())
			Scene.getInstance().redrawAllNow();

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
			}
			heliodon.update();
		}

		if (cameraControl != null && cameraControl.isAnimating())
			cameraControl.animate();

		root.updateGeometricState(tpf);

	}

	@Override
	public boolean renderUnto(final Renderer renderer) {
		if (cameraNode == null) {
			initCamera();
			return false;
		}
		final float[] f = new float[2];
		((JoglSwingCanvas) canvas).getRequestedSurfaceScale(f);
		setWindowsVisible(false);
		passManager.renderPasses(renderer);
		try {
			if (!Heliodon.getInstance().isNightTime()) {
				shadowPass.renderPass(renderer);
			}
		} catch (final Throwable e) {
			e.printStackTrace();
			if (shadowPass.isEnabled()) {
				JOptionPane.showMessageDialog(MainPanel.getInstance(), "Your video card driver does not support shadows!\nUpdating your video card drivers may fix this issue.\nShadow rendering will be disabled now.", "Warning", JOptionPane.WARNING_MESSAGE);
				shadowPass.setEnabled(false);
			}
		}
		setWindowsVisible(true);
		passManager.renderPasses(renderer);
		// com.ardor3d.util.geom.Debugger.drawBounds(Scene.getRoot(), renderer, true);
		taskManager.getQueue(GameTaskQueue.RENDER).execute(renderer);
		return true;
	}

	private void setWindowsVisible(final boolean visible) {
		land.setVisible(!visible);
		for (final HousePart part : Scene.getInstance().getParts())
			if (part instanceof Window)
				part.getMesh().setVisible(visible);
			else
				part.getRoot().getSceneHints().setCullHint(visible ? CullHint.Always : CullHint.Inherit);
	}

	public void initCamera() {
		System.out.println("initCamera()");
		final Camera camera = getCamera();
		cameraNode = new CameraNode("Camera Node", camera);
		root.attachChild(cameraNode);
		cameraNode.updateFromCamera();
		Scene.getInstance().updateEditShapes();
		setCameraControl(CameraMode.ORBIT);
		resetCamera(ViewMode.NORMAL);
		SceneManager.getInstance().getCameraControl().reset();

		taskManager.update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				final Spatial compass = createCompass();
				compass.setScale(0.1);
				compass.setTranslation(-1, -0.7, 2);
				cameraNode.attachChild(compass);
				Scene.getInstance().updateEditShapes();
				return null;
			}
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
		switch (theme) {
		case DEFAULT_THEME:
			land.setDefaultColor(new ColorRGBA(0, 1, 0, 0.5f));
			break;
		case SKETCHUP_THEME:
			land.setDefaultColor(new ColorRGBA(1, 1, 1, 0.9f));
			break;
		}

		land.setRenderState(HousePart.offsetState);

		final BlendState blendState = new BlendState();
		blendState.setBlendEnabled(true);
		land.setRenderState(blendState);
		land.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);

		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		land.setRenderState(ms);
		land.updateModelBound();
		return land;
	}

	public void drawGrids(final double gridSize) {
		gridsMesh.setDefaultColor(ColorRGBA.BLUE);

		gridsMesh.setModelBound(new BoundingBox());
		Util.disablePickShadowLight(gridsMesh);

		final ReadOnlyVector3 width = Vector3.UNIT_X.multiply(2000, null);
		final ReadOnlyVector3 height = Vector3.UNIT_Y.multiply(2000, null);
		final ArrayList<ReadOnlyVector3> points = new ArrayList<ReadOnlyVector3>();
		final ReadOnlyVector3 pMiddle = Vector3.ZERO;

		final int cols = (int) (width.length() / gridSize);

		for (int col = 1; col < cols / 2 + 1; col++) {
			for (int neg = -1; neg <= 1; neg += 2) {
				final ReadOnlyVector3 lineP1 = width.normalize(null).multiplyLocal(neg * col * gridSize).addLocal(pMiddle).subtractLocal(height.multiply(0.5, null));
				points.add(lineP1);
				final ReadOnlyVector3 lineP2 = lineP1.add(height, null);
				points.add(lineP2);
				if (col == 0)
					break;
			}
		}

		final int rows = (int) (height.length() / gridSize);

		for (int row = 1; row < rows / 2 + 1; row++) {
			for (int neg = -1; neg <= 1; neg += 2) {
				final ReadOnlyVector3 lineP1 = height.normalize(null).multiplyLocal(neg * row * gridSize).addLocal(pMiddle).subtractLocal(width.multiply(0.5, null));
				points.add(lineP1);
				final ReadOnlyVector3 lineP2 = lineP1.add(width, null);
				points.add(lineP2);
				if (row == 0)
					break;
			}
		}
		final FloatBuffer buf = BufferUtils.createVector3Buffer(points.size());
		for (final ReadOnlyVector3 p : points)
			buf.put(p.getXf()).put(p.getYf()).put(0.01f);

		gridsMesh.getMeshData().setVertexBuffer(buf);
		gridsMesh.getMeshData().updateVertexCount();
		gridsMesh.updateModelBound();
	}

	public void setGridsVisible(final boolean visible) {
		gridsMesh.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
	}

	private Mesh createSky() {
		daySkyState = new TextureState();
		daySkyState.setTexture(TextureManager.load("daysky.jpg", Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
		nightSkyState = new TextureState();
		nightSkyState.setTexture(TextureManager.load("nightsky.jpg", Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
		final Dome sky = new Dome("Sky", 100, 100, SKY_RADIUS);
		sky.setRotation(new Matrix3().fromAngles(Math.PI / 2, 0, 0));
		sky.setRenderState(daySkyState);
		sky.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		sky.getSceneHints().setAllPickingHints(false);
		return sky;
	}

	public void changeSkyTexture() {
		if (sky != null)
			sky.setRenderState(Heliodon.getInstance().isNightTime() ? nightSkyState : daySkyState);
	}

	private Spatial createAxes() {
		final int axisLen = 1000;
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
		axisRoot.attachChild(line);

		// Y-Axis
		buf = BufferUtils.createVector3Buffer(2);
		buf.put(0).put(-axisLen).put(0);
		buf.put(0).put(axisLen).put(0);
		line = new Line("Y-Axis", buf, null, null, null);
		line.setDefaultColor(ColorRGBA.GREEN);
		Util.disablePickShadowLight(line);
		axisRoot.attachChild(line);

		// Z-Axis
		buf = BufferUtils.createVector3Buffer(2);
		buf.put(0).put(0).put(-axisLen);
		buf.put(0).put(0).put(axisLen);
		line = new Line("Z-Axis", buf, null, null, null);
		Util.disablePickShadowLight(line);
		line.setDefaultColor(ColorRGBA.BLUE);
		axisRoot.attachChild(line);

		return axisRoot;
	}

	private Mesh createKinectPointer() {
		kinectPointer = new Sphere("Kinect Pointer", 10, 10, 0.01);
		return kinectPointer;
	}

	private void initMouse() {

		if (!Config.isHeliodonMode()) {

			logicalLayer.registerTrigger(new InputTrigger(new MouseButtonPressedCondition(MouseButton.LEFT), new TriggerAction() {
				@Override
				public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
					((Component) canvas).requestFocusInWindow();
					if (Config.isMac()) { // control-click is mouse right-click on the Mac, skip
						final KeyboardState ks = inputStates.getCurrent().getKeyboardState();
						if (ks.isDown(Key.LCONTROL) || ks.isDown(Key.RCONTROL))
							return;
					}
					if (firstClickState == null) {
						firstClickState = inputStates;
						mousePressed(inputStates.getCurrent().getMouseState());
					} else {
						firstClickState = null;
						mouseReleased(inputStates.getCurrent().getMouseState());
					}
				}
			}));

			logicalLayer.registerTrigger(new InputTrigger(new MouseButtonReleasedCondition(MouseButton.LEFT), new TriggerAction() {
				@Override
				public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
					if (Config.isMac()) { // control-click is mouse right-click on the Mac, skip
						final KeyboardState ks = inputStates.getCurrent().getKeyboardState();
						if (ks.isDown(Key.LCONTROL) || ks.isDown(Key.RCONTROL))
							return;
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
						if (selectedHousePart instanceof Roof || selectedHousePart instanceof Floor || selectedHousePart instanceof SolarPanel || selectedHousePart instanceof Sensor || selectedHousePart instanceof Tree || selectedHousePart instanceof Human || p1.distance(p2) > 10) {
							firstClickState = null;
							mouseReleased(inputStates.getCurrent().getMouseState());
						}
					}
				}
			}));

			((Component) canvas).addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(final MouseEvent e) {
					if (Util.isRightClick(e)) {
						final JPanel cp = MainPanel.getInstance().getCanvasPanel();
						mouseRightClicked(e.getX(), cp.getHeight() - e.getY());
					}
				}
			});

		}

		logicalLayer.registerTrigger(new InputTrigger(new MouseMovedCondition(), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				refresh = true;
				if (!Config.isHeliodonMode())
					mouseState = inputStates.getCurrent().getMouseState();
			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new MouseButtonClickedCondition(MouseButton.LEFT), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (Config.isMac()) { // control-click is mouse right-click on the Mac, skip
					final KeyboardState ks = inputStates.getCurrent().getKeyboardState();
					if (ks.isDown(Key.LCONTROL) || ks.isDown(Key.RCONTROL))
						return;
				}
				if (!isTopView() && inputStates.getCurrent().getMouseState().getClickCount(MouseButton.LEFT) == 2) {
					if (PrintController.getInstance().isPrintPreview()) {
						final MouseState mouse = inputStates.getCurrent().getMouseState();
						final Ray3 pickRay = Camera.getCurrentCamera().getPickRay(new Vector2(mouse.getX(), mouse.getY()), false, null);
						final PickResults pickResults = new PrimitivePickResults();
						PickingUtil.findPick(PrintController.getInstance().getPagesRoot(), pickRay, pickResults, false);
						if (pickResults.getNumber() > 0)
							cameraControl.zoomAtPoint(pickResults.getPickData(0).getIntersectionRecord().getIntersectionPoint(0));
					} else {
						final PickedHousePart pickedHousePart = SelectUtil.pickPart(inputStates.getCurrent().getMouseState().getX(), inputStates.getCurrent().getMouseState().getY());
						if (pickedHousePart != null)
							cameraControl.zoomAtPoint(pickedHousePart.getPoint());
					}
				}
			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.LSHIFT), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				SelectUtil.setPickLayer(0);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyReleasedCondition(Key.LSHIFT), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				SelectUtil.setPickLayer(-1);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.DELETE), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				deleteCurrentHousePart();
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.BACK), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				deleteCurrentHousePart();
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.ESCAPE), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				hideAllEditPoints();
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.ZERO), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (!Config.isApplet())
					return;
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
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.I), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				System.out.println("---- Parts: ------------------------");
				System.out.println("size = " + Scene.getInstance().getParts().size());
				for (final HousePart part : Scene.getInstance().getParts())
					System.out.println(part);
				System.out.println("---- Scene: ------------------------");
				System.out.println("size = " + Scene.getOriginalHouseRoot().getNumberOfChildren());
				for (final Spatial mesh : Scene.getOriginalHouseRoot().getChildren()) {
					System.out.println(mesh);
				}
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.R), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				Scene.getInstance().redrawAll(true);
			}
		}));

		// XIE: Run/pause model replay
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.SPACE), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (PlayControl.active) {
					PlayControl.replaying = !PlayControl.replaying;
				}
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.LEFT), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (PlayControl.active) {
					PlayControl.replaying = false;
					PlayControl.backward = true;
				}
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.UP), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (PlayControl.active) {
					PlayControl.replaying = false;
					PlayControl.backward = true;
				}
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.RIGHT), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (PlayControl.active) {
					PlayControl.replaying = false;
					PlayControl.forward = true;
				}
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.DOWN), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (PlayControl.active) {
					PlayControl.replaying = false;
					PlayControl.forward = true;
				}
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.ESCAPE), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				PlayControl.active = false;
			}
		}));

	}

	public void setCameraControl(final CameraMode type) {
		if (cameraControl != null)
			cameraControl.removeTriggers(logicalLayer);
		if (type == CameraMode.ORBIT)
			cameraControl = new OrbitControl(Vector3.UNIT_Z);
		else if (type == CameraMode.FIRST_PERSON)
			cameraControl = new FirstPersonControl(Vector3.UNIT_Z);
		cameraControl.setupMouseTriggers(logicalLayer, true);
		cameraControl.setMoveSpeed(MOVE_SPEED);
		cameraControl.setKeyRotateSpeed(1);
	}

	public CameraControl getCameraControl() {
		return cameraControl;
	}

	public void hideAllEditPoints() {
		for (final HousePart part : Scene.getInstance().getParts()) {
			part.setEditPointsVisible(false);
			part.setGridsVisible(false);
		}
		selectedHousePart = null;
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
		ReadOnlyVector3 loc = new Vector3(0, -120, 50);
		ReadOnlyVector3 up = new Vector3(0, 0, 1);
		ReadOnlyVector3 lookAt = new Vector3(0, 0, 0);

		setCompassVisible(viewMode == ViewMode.NORMAL);

		if (viewMode == ViewMode.NORMAL) {
			cameraControl.setMouseButtonActions(ButtonAction.ROTATE, ButtonAction.MOVE);
			if (Config.isApplet()) {
				loc = new Vector3(20, -80, 50);
				lookAt = new Vector3(0, 0, 0);
			}
			camera.setProjectionMode(ProjectionMode.Perspective);
			resizeCamera();
		} else if (viewMode == ViewMode.TOP_VIEW) {
			camera.setProjectionMode(ProjectionMode.Parallel);
			loc = new Vector3(0, 0, 500);
			up = new Vector3(0, 1, 0);
			lookAt = new Vector3(0, 0, 0);
			final double boundLength = Util.findBoundLength(Scene.getRoot().getWorldBound());
			cameraControl.setMoveSpeed(boundLength / 2);
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
		if (bounds == null)
			resizeCamera(2);
		else
			resizeCamera(Util.findBoundLength(bounds));
	}

	public void resizeCamera(final double orthoWidth) {
		final Camera camera = getCamera();
		if (camera == null)
			return;
		final Dimension size = ((Component) canvas).getSize();
		camera.resize(size.width, size.height);
		final double ratio = (double) size.width / size.height;

		final double near = 1;
		final double far = 2000;
		if (camera.getProjectionMode() == ProjectionMode.Parallel)
			camera.setFrustum(near, far, -orthoWidth / 2, orthoWidth / 2, orthoWidth / ratio / 2, -orthoWidth / ratio / 2);
		else
			camera.setFrustumPerspective(45.0, ratio, near, far);
	}

	public void toggleSpinView() {
		cameraControl.reset();
		rotAnim = !rotAnim;
	}

	public boolean getSpinView() {
		return rotAnim;
	}

	public void setSpinView(final boolean spinView) {
		rotAnim = spinView;
	}

	public void setOperation(final Operation operation) {
		operationStick = false;
		if (this.operation != operation) {
			this.operation = operation;
			operationFlag = true;

			// need to be here to ensure immediate removal of unfinished house part before computeEnergy thread is started
			synchronized (this) {
				if (selectedHousePart != null) {
					if (selectedHousePart.isDrawCompleted())
						selectedHousePart.setEditPointsVisible(false);
					else
						Scene.getInstance().remove(selectedHousePart, false);
					selectedHousePart = null;
				}
			}
		}
	}

	public void setOperationStick(final boolean stick) {
		operationStick = stick;
	}

	public void executeOperation() {
		operationFlag = false;
		for (final HousePart part : Scene.getInstance().getParts())
			if (part instanceof Foundation)
				((Foundation) part).setResizeHouseMode(operation == Operation.RESIZE);

		if (viewMode != ViewMode.PRINT_PREVIEW)
			Scene.getInstance().drawResizeBounds();

		selectedHousePart = newHousePart();
		if (selectedHousePart != null)
			cameraControl.setLeftMouseButtonEnabled(false);
	}

	private HousePart newHousePart() {
		final HousePart drawn;
		setGridsVisible(false);
		if (operation == Operation.DRAW_WALL) {
			drawn = new Wall();
		} else if (operation == Operation.DRAW_DOOR)
			drawn = new Door();
		else if (operation == Operation.DRAW_WINDOW)
			drawn = new Window();
		else if (operation == Operation.DRAW_ROOF_PYRAMID)
			drawn = new PyramidRoof();
		else if (operation == Operation.DRAW_ROOF_HIP)
			drawn = new HipRoof();
		else if (operation == Operation.DRAW_ROOF_SHED)
			drawn = new ShedRoof();
		else if (operation == Operation.DRAW_ROOF_CUSTOM)
			drawn = new CustomRoof();
		else if (operation == Operation.DRAW_FLOOR)
			drawn = new Floor();
		else if (operation == Operation.DRAW_SOLAR_PANEL)
			drawn = new SolarPanel();
		else if (operation == Operation.DRAW_SENSOR)
			drawn = new Sensor();
		else if (operation == Operation.DRAW_FOUNDATION) {
			drawn = new Foundation();
			setGridsVisible(true);
		} else if (operation == Operation.DRAW_DOGWOOD) {
			drawn = new Tree(Tree.DOGWOOD);
			setGridsVisible(true);
		} else if (operation == Operation.DRAW_ELM) {
			drawn = new Tree(Tree.ELM);
			setGridsVisible(true);
		} else if (operation == Operation.DRAW_OAK) {
			drawn = new Tree(Tree.OAK);
			setGridsVisible(true);
		} else if (operation == Operation.DRAW_LINDEN) {
			drawn = new Tree(Tree.LINDEN);
			setGridsVisible(true);
		} else if (operation == Operation.DRAW_COTTONWOOD) {
			drawn = new Tree(Tree.COTTONWOOD);
			setGridsVisible(true);
		} else if (operation == Operation.DRAW_MAPLE) {
			drawn = new Tree(Tree.MAPLE);
			setGridsVisible(true);
		} else if (operation == Operation.DRAW_PINE) {
			drawn = new Tree(Tree.PINE);
			setGridsVisible(true);
		} else if (operation == Operation.DRAW_JANE) {
			drawn = new Human(Human.JANE);
			setGridsVisible(true);
		} else if (operation == Operation.DRAW_JENI) {
			drawn = new Human(Human.JENI);
			setGridsVisible(true);
		} else if (operation == Operation.DRAW_JILL) {
			drawn = new Human(Human.JILL);
			setGridsVisible(true);
		} else if (operation == Operation.DRAW_JACK) {
			drawn = new Human(Human.JACK);
			setGridsVisible(true);
		} else if (operation == Operation.DRAW_JOHN) {
			drawn = new Human(Human.JOHN);
			setGridsVisible(true);
		} else if (operation == Operation.DRAW_JOSE) {
			drawn = new Human(Human.JOSE);
			setGridsVisible(true);
		} else
			return null;

		Scene.getInstance().add(drawn, false);
		addHousePartCommand = new AddPartCommand(drawn);
		return drawn;

	}

	public Operation getOperation() {
		return operation;
	}

	public void setShading(final boolean enable) {
		taskManager.update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				lightState.setEnabled(enable);
				root.updateWorldRenderStates(true);
				return null;
			}
		});
	}

	public void setHeliodonVisible(final boolean selected) {
		heliodonControl = selected;
		Heliodon.getInstance().setVisible(selected);
		enableDisableRotationControl();
		EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);

	}

	public void setSunAnimation(final boolean selected) {
		sunAnim = selected;
	}

	public boolean isSunAnimation() {
		return sunAnim;
	}

	public void enableDisableRotationControl() {
		if (!mouseControlEnabled)
			return;

		if ((operation == Operation.SELECT || operation == Operation.RESIZE) && (selectedHousePart == null || selectedHousePart.isDrawCompleted()))
			cameraControl.setMouseEnabled(true);
		else
			cameraControl.setMouseEnabled(false);

		if (heliodonControl)
			cameraControl.setKeyRotateSpeed(0);
		else
			cameraControl.setKeyRotateSpeed(1);
	}

	public HousePart getSelectedPart() {
		return selectedHousePart;
	}

	public void setSelectedPart(final HousePart p) {
		if (p == null && selectedHousePart != null)
			selectedHousePart.setEditPointsVisible(false);
		selectedHousePart = p;
		if (selectedHousePart != null)
			selectedHousePart.setEditPointsVisible(true);
	}

	public boolean isTopView() {
		return viewMode == ViewMode.TOP_VIEW;
	}

	public void updatePrintPreviewScene(final boolean printPreview) {
		if (printPreview)
			Scene.saveCameraLocation();
		resetCamera(printPreview ? ViewMode.PRINT_PREVIEW : ViewMode.NORMAL);
		if (!printPreview) {
			Scene.loadCameraLocation();
			if (cameraControl instanceof OrbitControl)
				((OrbitControl) cameraControl).clearOrbitCenter();
		}
		backgroundRoot.getSceneHints().setCullHint(printPreview ? CullHint.Always : CullHint.Inherit);
	}

	public void setShadow(final boolean shadow) {
		taskManager.update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				shadowPass.setEnabled(shadow);
				root.updateWorldRenderStates(true);
				return null;
			}
		});
	}

	public CameraNode getCameraNode() {
		return cameraNode;
	}

	private Node createCompass() throws IOException {
		System.out.print("Loading compass...");
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
		txt.setAutoRotate(false);
		txt.setTranslation(2, 0.0, Z);
		txt.setRotation(new Matrix3().fromAngles(0.0, MathUtils.HALF_PI, -MathUtils.HALF_PI));
		compass.attachChild(txt);

		txt = new BMText("S", "S", FontManager.getInstance().getAnnotationFont(), Align.South);
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

		compass.addController(new SpatialController<Spatial>() {
			@Override
			public void update(final double time, final Spatial caller) {
				final Vector3 direction = getCamera().getDirection().normalize(null);
				direction.setZ(0);
				direction.normalizeLocal();
				double angle = -direction.smallestAngleBetween(Vector3.UNIT_Y);
				if (direction.dot(Vector3.UNIT_X) > 0)
					angle = -angle;
				angle -= MathUtils.HALF_PI;
				compass.setRotation(new Matrix3().fromAngles(0.0, 0.0, angle - 0.3));
			}
		});

		return compassNode;
	}

	public void setCompassVisible(final boolean visible) {
		cameraNode.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
	}

	public void updateHeliodonAndAnnotationSize() {
		if (heliodonControl)
			taskManager.update(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					Heliodon.getInstance().updateSize();
					return null;
				}
			});
	}

	private void mouseMoved() {
		if (!mouseControlEnabled)
			return;
		final int x = mouseState.getX();
		final int y = mouseState.getY();

		if (editHousePartCommand != null && editHousePartCommand.isReallyEdited())
			EnergyPanel.getInstance().cancel();

		if (selectedHousePart != null && !selectedHousePart.isDrawCompleted()) {
			selectedHousePart.setPreviewPoint(x, y);
		} else if (houseMoveStartPoint != null && (operation == Operation.RESIZE || selectedHousePart instanceof Foundation) && selectedHousePart.isDrawCompleted()) {
			final PickedHousePart pick = SelectUtil.pickPart(x, y, collisionLand);
			if (pick != null) {
				if (selectedHousePart instanceof Foundation) {
					final Foundation foundation = (Foundation) selectedHousePart;
					final Vector3 pickPoint = pick.getPoint().clone();
					// if (!foundation.insideBuilding(pickPoint.getX(), pickPoint.getY(), true)) { // only move the building when clicking outside
					final Vector3 d = pickPoint.multiply(1, 1, 0, null).subtractLocal(houseMoveStartPoint.multiply(1, 1, 0, null));
					foundation.move(d, houseMovePoints);
				}
			}
		} else if (houseMoveStartPoint != null && selectedHousePart.isDrawCompleted() && selectedHousePart instanceof Tree) {
			final PickedHousePart pick = SelectUtil.pickPart(x, y, collisionLand);
			if (pick != null) {
				final Vector3 d = pick.getPoint().multiply(1, 1, 0, null).subtractLocal(houseMoveStartPoint.multiply(1, 1, 0, null));
				((Tree) selectedHousePart).move(d, houseMovePoints);
			}
		} else if (houseMoveStartPoint != null && selectedHousePart.isDrawCompleted() && selectedHousePart instanceof Window) {
			final PickedHousePart pick = SelectUtil.pickPart(x, y, selectedHousePart.getContainer());
			if (pick != null) {
				final Vector3 d = pick.getPoint().clone().subtractLocal(houseMoveStartPoint);
				((Window) selectedHousePart).move(d, houseMovePoints);
			}
		} else if ((operation == Operation.SELECT || operation == Operation.RESIZE) && mouseState.getButtonState(MouseButton.LEFT) == ButtonState.UP && mouseState.getButtonState(MouseButton.MIDDLE) == ButtonState.UP && mouseState.getButtonState(MouseButton.RIGHT) == ButtonState.UP) {
			final PickedHousePart selectHousePart = SelectUtil.selectHousePart(x, y, false);
			pick = selectHousePart == null ? null : selectHousePart.getUserData();
			final HousePart housePart = pick == null ? null : pick.getHousePart();
			if (pick != null) {
				hoveredHousePart = housePart;
				if (hoveredHousePart.isFrozen())
					hoveredHousePart = null;
				if (pick.getIndex() != -1)
					lastSelectedEditPointMouseState = mouseState;
			} else
				hoveredHousePart = null;
		}
		mouseState = null;

		final Component canvasComponent = (Component) canvas;
		if (operation == Operation.SELECT && !zoomLock) {
			if (hoveredHousePart instanceof Foundation || hoveredHousePart instanceof SolarPanel || hoveredHousePart instanceof Sensor || hoveredHousePart instanceof Window || hoveredHousePart instanceof Tree || hoveredHousePart instanceof Human) {
				canvasComponent.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			} else {
				canvasComponent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		} else {
			canvasComponent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

	}

	public void setMouseControlEnabled(final boolean enabled) {
		mouseControlEnabled = enabled;
		cameraControl.setMouseEnabled(enabled);
	}

	@Override
	public void init() {
		if (Config.RENDER_MODE != RenderMode.LWJGL)
			initCamera();
		if (Config.isHeliodonMode())
			MainPanel.getInstance().getHeliodonButton().setSelected(true);

	}

	public boolean isShadingEnabled() {
		return lightState.isEnabled();
	}

	public boolean isShadowEnabled() {
		return shadowPass.isEnabled();
	}

	public void setZoomLock(final boolean zoomLock) {
		this.zoomLock = zoomLock;
		cameraControl.setLeftButtonAction(zoomLock ? ButtonAction.ZOOM : viewMode == ViewMode.NORMAL ? ButtonAction.ROTATE : ButtonAction.MOVE);

	}

	public void refresh() {
		refresh = true;
	}

	public synchronized void refreshNow() {
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

	public UndoManager getUndoManager() {
		return undoManager;
	}

	public Timer getTimer() {
		return frameHandler.getTimer();
	}

	public Mesh getLand() {
		return land;
	}

	public Mesh getSolarLand() {
		return solarLand;
	}

	public boolean isHeliodonVisible() {
		return heliodonControl;
	}

	// the x and y coordinates come from MouseEvent, not MouseState.
	private void mouseRightClicked(final int x, final int y) {
		mouseState = new MouseState(x, y, 0, 0, 0, null, null);
		pasteMouseState = mouseState;
		refresh = true;
		taskManager.update(new Callable<Object>() {
			@Override
			public Object call() {
				if (operation == Operation.SELECT) {
					final HousePart previousSelectedHousePart = selectedHousePart;
					if (mouseState == null) // for some reason, there is a small chance that mouseState is nullified here
						mouseState = new MouseState(x, y, 0, 0, 0, null, null);
					final PickedHousePart pickedHousePart = SelectUtil.selectHousePart(mouseState.getX(), mouseState.getY(), true);
					final UserData pick = pickedHousePart == null ? null : pickedHousePart.getUserData();
					selectedHousePart = pick == null ? null : pick.getHousePart();
					System.out.println("Right-clicked on: (" + mouseState.getX() + ", " + mouseState.getY() + ") " + pick);
					if (previousSelectedHousePart != null && previousSelectedHousePart != selectedHousePart) {
						previousSelectedHousePart.setEditPointsVisible(false);
						previousSelectedHousePart.setGridsVisible(false);
					}
					if (selectedHousePart != null) {
						selectedHousePart.complete(); // to undo edit flag set by SelectUtil above
						if (!PrintController.getInstance().isPrintPreview())
							selectedHousePart.setEditPointsVisible(true);
						EnergyPanel.getInstance().update();
					}
					EnergyPanel.getInstance().updateGraphs();
					final JPanel cp = MainPanel.getInstance().getCanvasPanel();
					PopupMenuFactory.getPopupMenu(onLand(pasteMouseState.getX(), pasteMouseState.getY())).show(cp, mouseState.getX(), cp.getHeight() - mouseState.getY());
				}
				return null;
			}
		});
	}

	private void mousePressed(final MouseState mouseState) {
		refresh = true;
		taskManager.update(new Callable<Object>() {
			@Override
			public Object call() {
				if (zoomLock)
					return null;
				System.out.println("OPERATION: " + operation);
				if (operation == Operation.SELECT || operation == Operation.RESIZE || operation == Operation.ROTATE || operation == Operation.DRAW_ROOF_GABLE) {
					if (selectedHousePart == null || selectedHousePart.isDrawCompleted()) {
						final HousePart previousSelectedHousePart = selectedHousePart;
						final PickedHousePart selectHousePart = SelectUtil.selectHousePart(mouseState.getX(), mouseState.getY(), true);
						final UserData pick = selectHousePart == null ? null : selectHousePart.getUserData();
						if (pick == null)
							selectedHousePart = null;
						else
							selectedHousePart = pick.getHousePart();
						if (selectedHousePart != null && selectedHousePart.isFrozen())
							selectedHousePart = null;
						System.out.println("Clicked on: " + pick);
						if (pick != null && pick.getHousePart() instanceof Roof)
							System.out.println("Area: " + ((Roof) pick.getHousePart()).getAreaWithoutOverhang((Mesh) ((Node) ((Roof) pick.getHousePart()).getRoofPartsRoot().getChild(pick.getIndex())).getChild(0)));
						if (pick != null && pick.isEditPoint()) {
							cameraControl.setLeftMouseButtonEnabled(false);
							// EnergyPanel.getInstance().cancel();
						}

						if (operation == Operation.RESIZE && selectedHousePart != null) {
							if (!(selectedHousePart instanceof Foundation)) {
								selectedHousePart.setEditPointsVisible(false);
								selectedHousePart = selectedHousePart.getTopContainer();
							}
						}
						if (selectedHousePart instanceof Window || selectedHousePart instanceof Tree || (operation == Operation.RESIZE && selectedHousePart instanceof Foundation)) {
							cameraControl.setLeftMouseButtonEnabled(false);
							if (selectedHousePart != null) { // there is a chance that selectedHousePart = null
								houseMoveStartPoint = selectHousePart.getPoint().clone();
								collisionLand.setTranslation(0, 0, houseMoveStartPoint.getZ());
								final ArrayList<Vector3> points = selectedHousePart.getPoints();
								houseMovePoints = new ArrayList<Vector3>(points.size());
								for (final Vector3 p : points)
									houseMovePoints.add(p.clone());
							}
						}

						if (previousSelectedHousePart != null && previousSelectedHousePart != selectedHousePart) {
							previousSelectedHousePart.setEditPointsVisible(false);
							previousSelectedHousePart.setGridsVisible(false);
						}
						if (selectedHousePart != null && !PrintController.getInstance().isPrintPreview()) {
							selectedHousePart.setEditPointsVisible(true);
							if (pick.isEditPoint() && pick.getIndex() != -1 || operation == Operation.RESIZE || selectedHousePart instanceof Window || selectedHousePart instanceof Tree || selectedHousePart instanceof Foundation) {
								selectedHousePart.setGridsVisible(true);
								if (selectedHousePart instanceof Foundation)
									editHousePartCommand = new EditFoundationCommand((Foundation) selectedHousePart, !pick.isEditPoint());
								else
									editHousePartCommand = new EditPartCommand(selectedHousePart);
							}
						}
						SelectUtil.nextPickLayer();
						if (operation == Operation.DRAW_ROOF_GABLE && selectedHousePart instanceof Roof) {
							System.out.println(selectedHousePart);
							System.out.println("deleting roof #" + pick.getIndex());
							final int roofPartIndex = pick.getIndex();
							final Roof roof = (Roof) selectedHousePart;
							roof.setGable(roofPartIndex, true, undoManager);
						}
					}
				} else {
					selectedHousePart.addPoint(mouseState.getX(), mouseState.getY());
				}
				return null;
			}
		});
	}

	private void mouseReleased(final MouseState mouseState) {
		refresh = true;
		taskManager.update(new Callable<Object>() {
			@Override
			public Object call() {
				if (selectedHousePart != null)
					selectedHousePart.setGridsVisible(false);
				if (operation == Operation.SELECT || operation == Operation.RESIZE) {
					// if (selectedHousePart != null && (!selectedHousePart.isDrawCompleted() || (operation == Operation.RESIZE && houseMoveStartPoint != null))) {
					if (selectedHousePart != null && (!selectedHousePart.isDrawCompleted() || houseMoveStartPoint != null)) {
						if (selectedHousePart.isDrawable()) {
							selectedHousePart.complete();
							if (editHousePartCommand != null && editHousePartCommand.isReallyEdited())
								EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
						} else {
							editHousePartCommand.undo();
							selectedHousePart.setHighlight(false);
							// selectedHousePart.reset();
							// selectedHousePart.draw();
							// undoManager.addEdit(new RemoveHousePartCommand(selectedHousePart));
							// Scene.getInstance().remove(selectedHousePart, true);
							selectedHousePart = null;
						}
						if (editHousePartCommand != null) {
							if (editHousePartCommand.isReallyEdited())
								undoManager.addEdit(editHousePartCommand);
							editHousePartCommand = null;
						}
					}
					if (!zoomLock)
						cameraControl.setLeftMouseButtonEnabled(true);
					houseMoveStartPoint = null;
					houseMovePoints = null;
				} else {
					if (selectedHousePart != null && !selectedHousePart.isDrawCompleted()) {
						selectedHousePart.addPoint(mouseState.getX(), mouseState.getY());
						if (selectedHousePart.isDrawCompleted() && !selectedHousePart.isDrawable()) {
							addHousePartCommand = null;
							Scene.getInstance().remove(selectedHousePart, true);
							selectedHousePart = null;
							if (operationStick)
								operationFlag = true;
						}
					}
					if (selectedHousePart != null && selectedHousePart.isDrawCompleted()) {
						if (selectedHousePart.isDrawable()) {
							if (addHousePartCommand != null)
								undoManager.addEdit(addHousePartCommand);
							addHousePartCommand = null;
						} else
							Scene.getInstance().remove(selectedHousePart, true);
						selectedHousePart.setEditPointsVisible(false);
						selectedHousePart = null;
						if (operationStick)
							operationFlag = true;
						EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
					}
					if (!operationFlag) {
						MainPanel.getInstance().defaultTool();
						cameraControl.setLeftMouseButtonEnabled(true);
					}
				}
				updateHeliodonAndAnnotationSize();
				EnergyPanel.getInstance().update();
				return null;
			}
		});
		EnergyPanel.getInstance().updateGraphs();
	}

	public void grabOrRelease() {
		if (selectedHousePart != null && !selectedHousePart.isDrawCompleted())
			mouseReleased(lastSelectedEditPointMouseState);
		else
			mousePressed(lastSelectedEditPointMouseState);
	}

	public void deleteCurrentHousePart() {
		if (selectedHousePart instanceof Foundation) {
			if (((Foundation) selectedHousePart).getLockEdit())
				return;
			if (!selectedHousePart.getChildren().isEmpty())
				if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Deleting the platform also deletes the building on it. Are you sure?", "Confirmation", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;
		}
		if (selectedHousePart != null)
			taskManager.update(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					final RemovePartCommand command = new RemovePartCommand(selectedHousePart);
					if (selectedHousePart instanceof Wall) { // undo/redo a gable roof
						final Roof roof = ((Wall) selectedHousePart).getRoof();
						if (roof != null) {
							final List<Map<Integer, List<Wall>>> gableInfo = new ArrayList<Map<Integer, List<Wall>>>();
							if (roof.getGableEditPointToWallMap() != null)
								gableInfo.add(roof.getGableEditPointToWallMap());
							command.setGableInfo(gableInfo);
						}
					} else if (selectedHousePart instanceof Foundation) { // undo/redo all the gable roofs
						final List<Roof> roofs = new ArrayList<Roof>();
						for (final HousePart part : Scene.getInstance().getParts()) {
							if (part instanceof Wall) {
								for (final HousePart child : part.getChildren()) {
									if (child instanceof Roof && !roofs.contains(child))
										roofs.add((Roof) child);
								}
							}
						}
						if (!roofs.isEmpty()) {
							final List<Map<Integer, List<Wall>>> gableInfo = new ArrayList<Map<Integer, List<Wall>>>();
							for (final Roof r : roofs) {
								if (r.getGableEditPointToWallMap() != null)
									gableInfo.add(r.getGableEditPointToWallMap());
							}
							command.setGableInfo(gableInfo);
						}
					}
					undoManager.addEdit(command);
					Scene.getInstance().remove(selectedHousePart, true);
					selectedHousePart = null;
					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							MainPanel.getInstance().getEnergyViewButton().setSelected(false);
						}
					});
					return null;
				}
			});
	}

	public void newImport(final URL file) throws IOException {
		final ResourceSource source = new URLResourceSource(file);
		final ColladaImporter colladaImporter = new ColladaImporter();
		Logger.getLogger(ColladaAnimUtils.class.getName()).setLevel(Level.SEVERE);
		Logger.getLogger(ColladaMaterialUtils.class.getName()).setLevel(Level.SEVERE);
		final ColladaStorage storage = colladaImporter.load(source);
		newImport = storage.getScene();
		// newImport.setTranslation(0, 0, 30);
		// newImport.setScale(0.025);
		Scene.getRoot().attachChild(newImport);
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
		EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
	}

	public boolean isHeatFluxDaily() {
		return heatFluxDaily;
	}

	public void setHeatFluxDaily(final boolean heatFluxDaily) {
		this.heatFluxDaily = heatFluxDaily;
	}

	public void setAxesVisible(final boolean b) {
		if (b)
			backgroundRoot.attachChild(axes);
		else
			backgroundRoot.detachChild(axes);
	}

	public boolean areAxesVisible() {
		return backgroundRoot.hasChild(axes);
	}

	public void setHeatFluxVectorsVisible(final boolean b) {
		showHeatFlux = b;
		for (final HousePart part : Scene.getInstance().getParts())
			part.updateHeatFluxVisibility();
	}

	public boolean areHeatFluxVectorsVisible() {
		return showHeatFlux;
	}

	public void setBuildingLabelsVisible(final boolean b) {
		showBuildingLabels = b;
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Foundation)
				((Foundation) part).showBuildingLabel(b);
		}
	}

	public boolean areBuildingLabelsVisible() {
		return showBuildingLabels;
	}

	public void resetBuildingRotationAngleRecorded() {
		buildingRotationAngleRecorded = 0;
	}

	public double getBuildingRotationAngleRecorded() {
		return buildingRotationAngleRecorded;
	}

	public void undoOrRedoBuildingRotation(final Foundation foundation, final double rotationAngle, final boolean undo) {
		setSelectedPart(foundation);
		taskManager.update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				rotateBuilding(undo ? -rotationAngle : rotationAngle, false, true);
				return null;
			}
		});
	}

	/** negative angle for clockwise rotation, positive angle for counter-clockwise rotation */
	public void rotateBuilding(final double angle, final boolean keepRecord, final boolean redraw) {
		System.out.println("rotateBuilding()");
		if (selectedHousePart != null) {
			if (selectedHousePart instanceof Foundation)
				((Foundation) selectedHousePart).rotate(angle);
			else
				selectedHousePart.getTopContainer().rotate(angle);
			if (keepRecord)
				buildingRotationAngleRecorded += angle;
			if (redraw)
				Scene.getInstance().redrawAll();
		}
	}

	public boolean isRefreshOnlyMode() {
		return refreshOnlyMode;
	}

	public void setRefreshOnlyMode(final boolean refreshOnlyMode) {
		this.refreshOnlyMode = refreshOnlyMode;
	}

	private boolean onLand(final int x, final int y) {
		return SelectUtil.pickPart(x, y, land) != null;
	}

	Vector3 getPickedLocationOnLand() {
		if (pasteMouseState != null) {
			final PickedHousePart pick = SelectUtil.pickPart(pasteMouseState.getX(), pasteMouseState.getY(), land);
			if (pick != null)
				return pick.getPoint().multiply(1, 1, 0, null);
			pasteMouseState = null;
		}
		return null;
	}

	Vector3 getPickedLocationOnWall() {
		if (pasteMouseState != null) {
			final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
			if (selectedPart instanceof Wall) {
				final PickedHousePart pick = SelectUtil.pickPart(pasteMouseState.getX(), pasteMouseState.getY(), selectedPart);
				if (pick != null)
					return pick.getPoint().clone();
			}
			pasteMouseState = null;
		}
		return null;
	}

	Vector3 getPickedLocationOnRoof() {
		if (pasteMouseState != null) {
			final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
			if (selectedPart instanceof Roof) {
				final PickedHousePart pick = SelectUtil.pickPart(pasteMouseState.getX(), pasteMouseState.getY(), selectedPart);
				if (pick != null)
					return pick.getPoint().clone();
			}
			pasteMouseState = null;
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
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "There are multiple buildings. You must select a building first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no building.", "No Building", JOptionPane.INFORMATION_MESSAGE);
					}
				}
			}
		} else {
			final HousePart topContainer = selectedPart.getTopContainer();
			if (selectedPart instanceof Foundation) {
				foundation = (Foundation) selectedPart;
			} else if (topContainer instanceof Foundation) {
				selectedPart.setEditPointsVisible(false);
				SceneManager.getInstance().setSelectedPart(topContainer);
				foundation = (Foundation) topContainer;
			} else {
				if (ask)
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "You must select a building first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
			}
		}
		return foundation;
	}

}
