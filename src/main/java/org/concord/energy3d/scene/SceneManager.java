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
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.gui.PopupMenuFactory;
import org.concord.energy3d.logger.PlayControl;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.model.CustomRoof;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.GambrelRoof;
import org.concord.energy3d.model.HipRoof;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.PickedHousePart;
import org.concord.energy3d.model.PyramidRoof;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.ShedRoof;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Trackable;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.UserData;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.CameraControl.ButtonAction;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.undo.AddPartCommand;
import org.concord.energy3d.undo.ChangeAzimuthCommand;
import org.concord.energy3d.undo.EditFoundationCommand;
import org.concord.energy3d.undo.EditPartCommand;
import org.concord.energy3d.undo.MoveBuildingCommand;
import org.concord.energy3d.undo.RackEditPartCommand;
import org.concord.energy3d.undo.RemovePartCommand;
import org.concord.energy3d.undo.RotateBuildingCommand;
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
	public static final int SKY_RADIUS = 10000;
	private static final GameTaskQueueManager taskManager = GameTaskQueueManager.getManager("Task Manager");
	private static final SceneManager instance = new SceneManager(MainPanel.getInstance().getCanvasPanel());
	private static final double MOVE_SPEED = 5;
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
	private final Quad mapLand;
	private final Mesh gridsMesh;
	private final Spatial axes;
	private final LightState lightState = new LightState();
	private final UndoManager undoManager = new UndoManager();
	private HousePart selectedPart = null;
	private HousePart hoveredPart = null;
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
	private MouseState pasteMouseState;
	private Node newImport;
	private Vector3 houseMoveStartPoint;
	private ArrayList<Vector3> houseMovePoints;
	private double refreshTime = -1;
	private int refreshCount = 0;
	private boolean mouseControlEnabled = true;
	private boolean rotAnim = false;
	private boolean heliodonControl;
	private boolean sunAnim;
	private boolean operationStick = false;
	private boolean operationFlag = false;
	private boolean refresh = true;
	private boolean zoomLock = false;
	private boolean solarHeatMap = false;
	private boolean heatFluxDaily = true;
	private boolean showBuildingLabels = false;
	private boolean showHeatFlux = false;
	private boolean cameraChanged;
	private boolean fineGrid;
	private long frames;
	private long framesStartTime = -1;

	public enum Operation {
		SELECT, RESIZE, ROTATE, DRAW_WALL, DRAW_DOOR, DRAW_ROOF_PYRAMID, DRAW_ROOF_HIP, DRAW_ROOF_SHED, DRAW_ROOF_GAMBREL, DRAW_ROOF_CUSTOM, DRAW_ROOF_GABLE, DRAW_WINDOW, DRAW_FOUNDATION, DRAW_FLOOR, DRAW_SOLAR_PANEL, DRAW_RACK, DRAW_MIRROR, DRAW_SENSOR, DRAW_DOGWOOD, DRAW_ELM, DRAW_OAK, DRAW_LINDEN, DRAW_MAPLE, DRAW_COTTONWOOD, DRAW_PINE, DRAW_JANE, DRAW_JENI, DRAW_JILL, DRAW_JACK, DRAW_JOHN, DRAW_JOSE
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
			rendererFactory = null;
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
		mapLand = new Quad("Map Land");
		resizeMapLand(1);
		mapLand.setModelBound(new BoundingBox());
		mapLand.updateModelBound();
		mapLand.updateWorldBound(true);
		mapLand.setVisible(false);
		backgroundRoot.getSceneHints().setAllPickingHints(false);
		backgroundRoot.attachChild(sky);
		backgroundRoot.attachChild(land);
		backgroundRoot.attachChild(collisionLand);
		backgroundRoot.attachChild(solarLand);
		backgroundRoot.attachChild(mapLand);
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
		shadowPass.add(mapLand);
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
					Util.reportError(e);
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
		taskManager.getQueue(GameTaskQueue.UPDATE).setExecuteMultiple(true);
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
							m.drawLightBeams(); // call this so that the light beams can be set invisible
						} else {
							if (m.getHeliostatTarget() != null) {
								m.draw();
							}
						}
					} else if (part instanceof SolarPanel) {
						final SolarPanel sp = (SolarPanel) part;
						if (!night && sp.getTracker() != Trackable.NO_TRACKER) {
							sp.draw();
						}
						if (sp.isDrawSunBeamVisible()) {
							sp.drawSunBeam();
						}
					} else if (part instanceof Rack) {
						final Rack rack = (Rack) part;
						if (!night && rack.getTracker() != Trackable.NO_TRACKER) {
							rack.draw();
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
		setWindowsVisible(false);
		try {
			passManager.renderPasses(renderer);
		} catch (final Throwable e) {
			e.printStackTrace();
		}
		try {
			if (!Heliodon.getInstance().isNightTime()) {
				shadowPass.renderPass(renderer);
			}
		} catch (final Throwable e) {
			e.printStackTrace();
			if (shadowPass.isEnabled()) {
				shadowPass.setEnabled(false);
			}
		}
		setWindowsVisible(true);
		passManager.renderPasses(renderer);
		// com.ardor3d.util.geom.Debugger.drawBounds(Scene.getRoot(), renderer, true);
		taskManager.getQueue(GameTaskQueue.RENDER).execute(renderer);
		if (framesStartTime != -1) {
			frames++;
			final long t = (System.nanoTime() - framesStartTime) / 1000000000;
			if (t != 0) {
				System.out.println(frames / t);
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
		final Quad land = new Quad("Land", SKY_RADIUS * 2, SKY_RADIUS * 2);
		land.setDefaultColor(new ColorRGBA(0, 1, 0, 0.5f));
		land.setRenderState(HousePart.offsetState);
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

	public Mesh drawGrids(final double gridSize) {
		final Mesh gridsMesh = new Line("Ground Grids");
		gridsMesh.getSceneHints().setCullHint(CullHint.Always);
		gridsMesh.setDefaultColor(new ColorRGBA(0, 0, 1, 1));
		final BlendState blendState = new BlendState();
		blendState.setBlendEnabled(true);
		gridsMesh.setRenderState(blendState);
		gridsMesh.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);

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
				if (col == 0) {
					break;
				}
			}
		}

		final int rows = (int) (height.length() / gridSize);

		for (int row = 1; row < rows / 2 + 1; row++) {
			for (int neg = -1; neg <= 1; neg += 2) {
				final ReadOnlyVector3 lineP1 = height.normalize(null).multiplyLocal(neg * row * gridSize).addLocal(pMiddle).subtractLocal(width.multiply(0.5, null));
				points.add(lineP1);
				final ReadOnlyVector3 lineP2 = lineP1.add(width, null);
				points.add(lineP2);
				if (row == 0) {
					break;
				}
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
		new Thread() {
			@Override
			public void run() {
				final TextureState ts = new TextureState();
				ts.setTexture(TextureManager.load("daysky.jpg", Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
				sky.setRenderState(ts);
			}
		}.start();
		return sky;
	}

	public void changeSkyTexture() {
		if (sky != null) {
			final boolean isNightTime = Heliodon.getInstance().isNightTime();
			String textureFile;
			switch (Scene.getInstance().getTheme()) {
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

		logicalLayer.registerTrigger(new InputTrigger(new MouseButtonPressedCondition(MouseButton.LEFT), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
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
			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new MouseButtonReleasedCondition(MouseButton.LEFT), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
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
				EnergyPanel.getInstance().update();
				cameraChanged = true;
			}
		});

		((Component) canvas).addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(final MouseWheelEvent e) {
				TimeSeriesLogger.getInstance().logCamera("Zoom");
			}
		});

		logicalLayer.registerTrigger(new InputTrigger(new MouseMovedCondition(), new TriggerAction() {

			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				refresh = true;
				mouseState = inputStates.getCurrent().getMouseState();
			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new MouseButtonClickedCondition(MouseButton.LEFT), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
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
			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.LSHIFT), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				// SelectUtil.setPickLayer(0);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyReleasedCondition(Key.LSHIFT), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				// SelectUtil.setPickLayer(-1);
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
				for (final HousePart part : Scene.getInstance().getParts()) {
					System.out.println(part);
				}
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

		// Run/pause model replay
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
				if (SceneManager.getInstance().isTopView()) {
					moveWithKey(inputStates.getCurrent().getKeyboardState(), new Vector3(-1, 0, 0));
				} else {
					if (selectedPart instanceof Window) {
						final Vector3 v = selectedPart.getNormal().clone();
						v.crossLocal(Vector3.UNIT_Z);
						moveWithKey(inputStates.getCurrent().getKeyboardState(), v);
						Scene.getInstance().redrawAll();
					}
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
				if (SceneManager.getInstance().isTopView()) {
					moveWithKey(inputStates.getCurrent().getKeyboardState(), new Vector3(0, 1, 0));
				} else {
					if (selectedPart instanceof Window) {
						final Vector3 n = selectedPart.getNormal().clone();
						final Vector3 v = n.cross(Vector3.UNIT_Z, null);
						moveWithKey(inputStates.getCurrent().getKeyboardState(), v.crossLocal(n));
						Scene.getInstance().redrawAll();
					}
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
				if (SceneManager.getInstance().isTopView()) {
					moveWithKey(inputStates.getCurrent().getKeyboardState(), new Vector3(1, 0, 0));
				} else {
					if (selectedPart instanceof Window) {
						final Vector3 v = selectedPart.getNormal().clone();
						v.crossLocal(Vector3.UNIT_Z).negateLocal();
						moveWithKey(inputStates.getCurrent().getKeyboardState(), v);
						Scene.getInstance().redrawAll();
					}
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
				if (SceneManager.getInstance().isTopView()) {
					moveWithKey(inputStates.getCurrent().getKeyboardState(), new Vector3(0, -1, 0));
				} else {
					if (selectedPart instanceof Window) {
						final Vector3 n = selectedPart.getNormal().clone();
						final Vector3 v = n.cross(Vector3.UNIT_Z, null).negateLocal();
						moveWithKey(inputStates.getCurrent().getKeyboardState(), v.crossLocal(n));
						Scene.getInstance().redrawAll();
					}
				}
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.ESCAPE), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				PlayControl.active = false;
			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.W), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				moveWithKey(inputStates.getCurrent().getKeyboardState(), new Vector3(-1, 0, 0));
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.E), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				moveWithKey(inputStates.getCurrent().getKeyboardState(), new Vector3(1, 0, 0));
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.S), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				moveWithKey(inputStates.getCurrent().getKeyboardState(), new Vector3(0, -1, 0));
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.N), new TriggerAction() {
			@Override
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				moveWithKey(inputStates.getCurrent().getKeyboardState(), new Vector3(0, 1, 0));
			}
		}));

	}

	private void moveWithKey(final KeyboardState ks, final Vector3 v) {
		if (ks.isDown(Key.LCONTROL) || ks.isDown(Key.RCONTROL) || ks.isDown(Key.LMETA) || ks.isDown(Key.LMETA)) {
			return; // Ctrl/Cmd+key is often used for other purposes such as Ctrl+S or Cmd+S
		}
		fineGrid = ks.isDown(Key.LSHIFT) || ks.isDown(Key.RSHIFT);
		move(v);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				EnergyPanel.getInstance().updateProperties();
			}
		});
	}

	public void move(final Vector3 v) {
		MoveBuildingCommand c = null;
		if (selectedPart == null) {
			c = new MoveBuildingCommand(null, v);
			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p instanceof Foundation) {
					((Foundation) p).move(v, p.getGridSize());
				}
			}
			Scene.getInstance().redrawAll();
		} else if (selectedPart instanceof Foundation) {
			c = new MoveBuildingCommand((Foundation) selectedPart, v);
			final Foundation f = (Foundation) selectedPart;
			f.move(v, selectedPart.getGridSize());
			f.draw();
			f.drawChildren();
			SceneManager.getInstance().refresh();
		} else if (selectedPart instanceof Window) {
			final Window w = (Window) selectedPart;
			w.move(v);
			w.draw();
		} else if (selectedPart instanceof Mirror) {
			final Mirror m = (Mirror) selectedPart;
			m.move(v, selectedPart.getGridSize());
			m.draw();
		} else if (selectedPart instanceof SolarPanel) {
			final SolarPanel s = (SolarPanel) selectedPart;
			s.move(v, selectedPart.getGridSize());
			s.draw();
		} else if (selectedPart instanceof Rack) {
			final Rack r = (Rack) selectedPart;
			r.move(v, selectedPart.getGridSize());
			r.draw();
		}
		if (c != null) {
			undoManager.addEdit(c);
		}
		SceneManager.getInstance().refresh();
		Scene.getInstance().setEdited(true);
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
		if (bounds == null) {
			resizeCamera(2);
		} else {
			resizeCamera(Util.findBoundLength(bounds));
		}
	}

	public void resizeCamera(final double orthoWidth) {
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
		operationStick = false;
		if (this.operation != operation) {
			this.operation = operation;
			operationFlag = true;
			// need to be here to ensure immediate removal of unfinished house part before computeEnergy thread is started
			SceneManager.getTaskManager().update(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					if (selectedPart != null) {
						if (selectedPart.isDrawCompleted()) {
							selectedPart.setEditPointsVisible(false);
						} else {
							Scene.getInstance().remove(selectedPart, false);
						}
						selectedPart = null;
					}
					return null;
				}
			});
		}
	}

	public void setOperationStick(final boolean stick) {
		operationStick = stick;
	}

	public void executeOperation() {
		operationFlag = false;
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Foundation) {
				((Foundation) part).setResizeHouseMode(operation == Operation.RESIZE);
			}
		}

		if (viewMode != ViewMode.PRINT_PREVIEW) {
			Scene.getInstance().drawResizeBounds();
		}

		selectedPart = newHousePart();
		if (selectedPart != null) {
			cameraControl.setLeftMouseButtonEnabled(false);
		}
	}

	private HousePart newHousePart() {
		final HousePart drawn;
		setGridsVisible(false);
		if (operation == Operation.DRAW_WALL) {
			drawn = new Wall();
			drawn.setColor(Scene.getInstance().getWallColor());
		} else if (operation == Operation.DRAW_DOOR) {
			drawn = new Door();
			drawn.setColor(Scene.getInstance().getDoorColor());
		} else if (operation == Operation.DRAW_WINDOW) {
			drawn = new Window();
		} else if (operation == Operation.DRAW_ROOF_PYRAMID) {
			drawn = new PyramidRoof();
			drawn.setColor(Scene.getInstance().getRoofColor());
		} else if (operation == Operation.DRAW_ROOF_HIP) {
			drawn = new HipRoof();
			drawn.setColor(Scene.getInstance().getRoofColor());
		} else if (operation == Operation.DRAW_ROOF_SHED) {
			drawn = new ShedRoof();
			drawn.setColor(Scene.getInstance().getRoofColor());
		} else if (operation == Operation.DRAW_ROOF_GAMBREL) {
			drawn = new GambrelRoof();
			drawn.setColor(Scene.getInstance().getRoofColor());
		} else if (operation == Operation.DRAW_ROOF_CUSTOM) {
			drawn = new CustomRoof();
			drawn.setColor(Scene.getInstance().getRoofColor());
		} else if (operation == Operation.DRAW_FLOOR) {
			drawn = new Floor();
			drawn.setColor(Scene.getInstance().getFloorColor());
		} else if (operation == Operation.DRAW_SOLAR_PANEL) {
			drawn = new SolarPanel(false);
		} else if (operation == Operation.DRAW_RACK) {
			drawn = new Rack();
		} else if (operation == Operation.DRAW_MIRROR) {
			drawn = new Mirror();
		} else if (operation == Operation.DRAW_SENSOR) {
			drawn = new Sensor();
		} else if (operation == Operation.DRAW_FOUNDATION) {
			drawn = new Foundation();
			setGridsVisible(true);
			drawn.setColor(Scene.getInstance().getFoundationColor());
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
		} else {
			return null;
		}

		Scene.getInstance().add(drawn, false);
		addPartCommand = new AddPartCommand(drawn);
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
		EnergyPanel.getInstance().clearRadiationHeatMap();

	}

	public void setSunAnimation(final boolean selected) {
		sunAnim = selected;
	}

	public boolean isSunAnimation() {
		return sunAnim;
	}

	public void enableDisableRotationControl() {
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
		if (p == null && selectedPart != null) {
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

	public void updatePrintPreviewScene(final boolean printPreview) {
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
				if (direction.dot(Vector3.UNIT_X) > 0) {
					angle = -angle;
				}
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
		if (heliodonControl) {
			taskManager.update(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					Heliodon.getInstance().updateSize();
					return null;
				}
			});
		}
	}

	private void mouseMoved() {
		if (!mouseControlEnabled) {
			return;
		}
		final int x = mouseState.getX();
		final int y = mouseState.getY();
		try {
			if (selectedPart != null) {
				if (!selectedPart.isDrawCompleted()) {
					selectedPart.setPreviewPoint(x, y);
				} else if (houseMoveStartPoint != null) {
					if ((operation == Operation.RESIZE || selectedPart instanceof Foundation)) {
						final PickedHousePart pick = SelectUtil.pickPart(x, y, collisionLand);
						if (pick != null) {
							if (selectedPart instanceof Foundation) {
								final Foundation foundation = (Foundation) selectedPart;
								final Vector3 pickPoint = pick.getPoint().clone();
								// if (!foundation.insideBuilding(pickPoint.getX(), pickPoint.getY(), true)) { // only move the building when clicking outside
								final Vector3 d = pickPoint.multiply(1, 1, 0, null).subtractLocal(houseMoveStartPoint.multiply(1, 1, 0, null));
								foundation.move(d, houseMovePoints);
							}
						}
					} else if (selectedPart instanceof Tree) {
						final PickedHousePart pick = SelectUtil.pickPart(x, y, collisionLand);
						if (pick != null) {
							final Vector3 d = pick.getPoint().multiply(1, 1, 0, null).subtractLocal(houseMoveStartPoint.multiply(1, 1, 0, null));
							((Tree) selectedPart).move(d, houseMovePoints);
						}
					} else if (selectedPart instanceof Window) {
						final PickedHousePart pick = SelectUtil.pickPart(x, y, selectedPart.getContainer());
						if (pick != null) {
							final Vector3 d = pick.getPoint().subtract(houseMoveStartPoint, null);
							((Window) selectedPart).move(d, houseMovePoints);
						}
					}
				}
			} else if ((operation == Operation.SELECT || operation == Operation.RESIZE) && mouseState.getButtonState(MouseButton.LEFT) == ButtonState.UP && mouseState.getButtonState(MouseButton.MIDDLE) == ButtonState.UP && mouseState.getButtonState(MouseButton.RIGHT) == ButtonState.UP) {
				final PickedHousePart pickedPart = SelectUtil.selectHousePart(x, y, false);
				pick = pickedPart == null ? null : pickedPart.getUserData();
				final HousePart housePart = pick == null ? null : pick.getHousePart();
				if (pick != null) {
					hoveredPart = housePart;
					if (hoveredPart.isFrozen()) {
						hoveredPart = null;
					}
					if (pick.getIndex() != -1) {
						lastSelectedEditPointMouseState = mouseState;
					}
				} else {
					hoveredPart = null;
				}
			}
			mouseState = null;
		} catch (final Throwable t) {
			t.printStackTrace();
			Util.reportError(t);
		}
		final Component canvasComponent = (Component) canvas;
		if (!zoomLock && (operation == Operation.SELECT || operation == Operation.RESIZE) && hoveredPart != null && pick.getIndex() == -1 && (hoveredPart instanceof SolarPanel || hoveredPart instanceof Sensor || hoveredPart instanceof Window || hoveredPart instanceof Tree || hoveredPart instanceof Human)) {
			canvasComponent.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
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
		if (Config.RENDER_MODE != RenderMode.LWJGL) {
			initCamera();
		}
	}

	public boolean isShadingEnabled() {
		return lightState.isEnabled();
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

	public boolean isShadowEnabled() {
		return shadowPass.isEnabled();
	}

	public void setZoomLock(final boolean zoomLock) {
		this.zoomLock = zoomLock;
		cameraControl.setLeftButtonAction(zoomLock ? ButtonAction.ZOOM : viewMode == ViewMode.NORMAL ? ButtonAction.ROTATE : ButtonAction.MOVE);

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

	public Mesh getMapLand() {
		return mapLand;
	}

	public void resizeMapLand(final double scale) {
		final double d = 68.75 / 0.2 * scale;
		mapLand.resize(d, d);
		mapLand.updateModelBound();
		mapLand.updateWorldBound(true);
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
		pasteMouseState = mouseState;
		refresh = true;
		taskManager.update(new Callable<Object>() {
			@Override
			public Object call() {
				try {
					if (operation == Operation.SELECT) {
						final HousePart previousSelectedHousePart = selectedPart;
						if (mouseState == null) {
							mouseState = new MouseState(x, y, 0, 0, 0, null, null);
						}
						final PickedHousePart pickedHousePart = SelectUtil.selectHousePart(mouseState.getX(), mouseState.getY(), true);
						final UserData pick = pickedHousePart == null ? null : pickedHousePart.getUserData();
						selectedPart = pick == null ? null : pick.getHousePart();
						if (e.isAltDown()) {
							if (selectedPart instanceof SolarPanel && selectedPart.getContainer() instanceof Rack) { // special case
								selectedPart = selectedPart.getContainer();
							}
						}
						System.out.println("Right-clicked on: (" + mouseState.getX() + ", " + mouseState.getY() + ") " + pick);
						if (previousSelectedHousePart != null && previousSelectedHousePart != selectedPart) {
							previousSelectedHousePart.setEditPointsVisible(false);
							previousSelectedHousePart.setGridsVisible(false);
						}
						if (selectedPart != null) {
							selectedPart.complete(); // to undo edit flag set by SelectUtil above
							if (!PrintController.getInstance().isPrintPreview()) {
								selectedPart.setEditPointsVisible(true);
							}
							EnergyPanel.getInstance().update();
						}
						EnergyPanel.getInstance().updateGraphs();
						EnergyPanel.getInstance().updateProperties();
						final JPanel cp = MainPanel.getInstance().getCanvasPanel();
						PopupMenuFactory.getPopupMenu(onLand(pasteMouseState.getX(), pasteMouseState.getY())).show(cp, mouseState.getX(), cp.getHeight() - mouseState.getY());
					}
				} catch (final Throwable t) {
					t.printStackTrace();
					Util.reportError(t);
				}
				return null;
			}
		});
	}

	private void mousePressed(final MouseState mouseState, final KeyboardState keyboardState) {
		refresh = true;
		taskManager.update(new Callable<Object>() {
			@Override
			public Object call() {
				if (zoomLock) {
					return null;
				}
				System.out.println("OPERATION: " + operation);
				try {
					if (operation == Operation.SELECT || operation == Operation.RESIZE || operation == Operation.ROTATE || operation == Operation.DRAW_ROOF_GABLE) {
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
								if (selectedPart.isFrozen()) {
									selectedPart = null;
								}
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

							if (operation == Operation.SELECT || operation == Operation.ROTATE) {
								if (previousSelectedPart instanceof Foundation) {
									((Foundation) previousSelectedPart).updateAzimuthArrowVisibility(false);
								}
								if (selectedPart instanceof Foundation) {
									((Foundation) selectedPart).drawAzimuthArrow();
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
							if (selectedPart instanceof Window || selectedPart instanceof Tree || (selectedPart instanceof Foundation && pick.getIndex() != -1)) {
								cameraControl.setLeftMouseButtonEnabled(false);
								houseMoveStartPoint = pickedPart.getPoint().clone();
								collisionLand.setTranslation(0, 0, houseMoveStartPoint.getZ());
								final ArrayList<Vector3> points = selectedPart.getPoints();
								houseMovePoints = new ArrayList<Vector3>(points.size());
								for (final Vector3 p : points) {
									houseMovePoints.add(p.clone());
								}
							}

							if (previousSelectedPart != null && previousSelectedPart != selectedPart && operation != Operation.RESIZE) {
								previousSelectedPart.setEditPointsVisible(false);
								previousSelectedPart.setGridsVisible(false);
								if (previousSelectedPart != null) {
									final Foundation foundationOfPreviousSelectedPart = previousSelectedPart instanceof Foundation ? (Foundation) previousSelectedPart : previousSelectedPart.getTopContainer();
									if (foundationOfPreviousSelectedPart != null) {
										if (selectedPart == null) {
											foundationOfPreviousSelectedPart.setMovePointsVisible(false);
										} else if (foundationOfPreviousSelectedPart != (selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer())) {
											foundationOfPreviousSelectedPart.setMovePointsVisible(false);
										}
									}
								}
							}
							if (selectedPart != null && !PrintController.getInstance().isPrintPreview()) {
								selectedPart.setEditPointsVisible(true);
								if (pick.isEditPoint() && pick.getIndex() != -1 || operation == Operation.RESIZE || selectedPart instanceof Window || selectedPart instanceof Tree) {
									selectedPart.setGridsVisible(true);
									if (selectedPart instanceof Foundation) {
										editPartCommand = new EditFoundationCommand((Foundation) selectedPart, !pick.isEditPoint());
									} else if (selectedPart instanceof Rack) {
										editPartCommand = new RackEditPartCommand((Rack) selectedPart);
									} else {
										editPartCommand = new EditPartCommand(selectedPart);
									}
								}
							}
							SelectUtil.nextPickLayer();
							if (operation == Operation.DRAW_ROOF_GABLE && selectedPart instanceof Roof) {
								System.out.println(selectedPart);
								System.out.println("deleting roof #" + pick.getIndex());
								final int roofPartIndex = pick.getIndex();
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
					Util.reportError(t);
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
				try {
					if (selectedPart != null) {
						selectedPart.setGridsVisible(false);
					}
					if (operation == Operation.SELECT || operation == Operation.RESIZE) {
						if (selectedPart != null && (!selectedPart.isDrawCompleted() || houseMoveStartPoint != null)) {
							if (selectedPart.isDrawable()) {
								selectedPart.complete();
								if (editPartCommand != null && editPartCommand.isReallyEdited()) {
									EnergyPanel.getInstance().clearRadiationHeatMap();
								}
							} else {
								if (editPartCommand != null) {
									editPartCommand.undo();
									selectedPart.setHighlight(false);
									selectedPart.complete();
								}
							}
							if (editPartCommand != null) {
								if (editPartCommand.isReallyEdited()) {
									undoManager.addEdit(editPartCommand);
								}
								editPartCommand = null;
							}
						}
						if (!zoomLock) {
							cameraControl.setLeftMouseButtonEnabled(true);
						}
						houseMoveStartPoint = null;
						houseMovePoints = null;
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
									undoManager.addEdit(addPartCommand);
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
							EnergyPanel.getInstance().clearRadiationHeatMap();
						}
						if (!operationFlag) {
							MainPanel.getInstance().defaultTool();
							cameraControl.setLeftMouseButtonEnabled(true);
						}
					}
					updateHeliodonAndAnnotationSize();
					if (selectedPart instanceof Foundation) {
						Scene.getInstance().updateTrackables();
					}
					EnergyPanel.getInstance().update();
				} catch (final Throwable t) {
					t.printStackTrace();
					Util.reportError(t);
				}
				return null;
			}
		});
		EnergyPanel.getInstance().updateGraphs();
	}

	public void grabOrRelease() {
		if (selectedPart != null && !selectedPart.isDrawCompleted()) {
			mouseReleased(lastSelectedEditPointMouseState);
		} else {
			mousePressed(lastSelectedEditPointMouseState, null);
		}
	}

	public void deleteCurrentHousePart() {
		if (selectedPart instanceof Foundation) {
			if (((Foundation) selectedPart).getLockEdit()) {
				return;
			}
			if (!selectedPart.getChildren().isEmpty()) {
				if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Deleting the platform also deletes the building on it. Are you sure?", "Confirmation", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					return;
				}
			}
		}
		if (selectedPart != null) {
			taskManager.update(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					final RemovePartCommand command = new RemovePartCommand(selectedPart);
					if (selectedPart instanceof Wall) { // undo/redo a gable roof
						final Roof roof = ((Wall) selectedPart).getRoof();
						if (roof != null) {
							final List<Map<Integer, List<Wall>>> gableInfo = new ArrayList<Map<Integer, List<Wall>>>();
							if (roof.getGableEditPointToWallMap() != null) {
								gableInfo.add(roof.getGableEditPointToWallMap());
							}
							command.setGableInfo(gableInfo);
						}
					} else if (selectedPart instanceof Foundation) { // undo/redo all the gable roofs
						final List<Roof> roofs = ((Foundation) selectedPart).getRoofs();
						if (!roofs.isEmpty()) {
							final List<Map<Integer, List<Wall>>> gableInfo = new ArrayList<Map<Integer, List<Wall>>>();
							for (final Roof r : roofs) {
								if (r.getGableEditPointToWallMap() != null) {
									gableInfo.add(r.getGableEditPointToWallMap());
								}
							}
							command.setGableInfo(gableInfo);
						}
					}
					undoManager.addEdit(command);
					Scene.getInstance().remove(selectedPart, true);
					selectedPart = null;
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
		EnergyPanel.getInstance().clearRadiationHeatMap();
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

	public void setBuildingLabelsVisible(final boolean b) {
		showBuildingLabels = b;
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Foundation) {
				((Foundation) part).showBuildingLabel(b);
			}
		}
	}

	public boolean areBuildingLabelsVisible() {
		return showBuildingLabels;
	}

	/** negative angle for clockwise rotation, positive angle for counter-clockwise rotation */
	public void rotateBuilding(final double angle, final boolean redraw) {
		System.out.println("rotateBuilding()");
		if (selectedPart != null) {
			if (selectedPart instanceof Foundation) {
				((Foundation) selectedPart).rotate(angle, null);
			} else {
				selectedPart.getTopContainer().rotate(angle, null);
			}
			if (redraw) {
				Scene.getInstance().redrawAll();
			}
		}
	}

	/** negative angle for clockwise rotation, positive angle for counter-clockwise rotation */
	public void rotateAllBuildings(final double angle) {
		System.out.println("rotateBuildings()");
		final Vector3 origin = new Vector3();
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Foundation) {
				((Foundation) p).rotate(angle, origin);
			}
		}
		Scene.getInstance().redrawAll();
	}

	public void rotate(final double angle) {
		taskManager.update(new Callable<Object>() {
			@Override
			public Object call() {
				final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
				if (selectedPart instanceof Foundation) {
					final RotateBuildingCommand c = new RotateBuildingCommand((Foundation) selectedPart, angle);
					SceneManager.getInstance().rotateBuilding(angle, true);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				} else if (selectedPart instanceof SolarPanel) {
					final SolarPanel solarPanel = (SolarPanel) selectedPart;
					final ChangeAzimuthCommand c = new ChangeAzimuthCommand(solarPanel);
					solarPanel.setRelativeAzimuth(solarPanel.getRelativeAzimuth() + Math.toDegrees(angle));
					solarPanel.draw();
					SceneManager.getInstance().getUndoManager().addEdit(c);
				} else if (selectedPart instanceof Mirror) {
					final Mirror mirror = (Mirror) selectedPart;
					final ChangeAzimuthCommand c = new ChangeAzimuthCommand(mirror);
					mirror.setRelativeAzimuth(mirror.getRelativeAzimuth() + Math.toDegrees(angle));
					mirror.draw();
					SceneManager.getInstance().getUndoManager().addEdit(c);
				} else if (selectedPart == null) {
					final RotateBuildingCommand c = new RotateBuildingCommand(null, angle);
					rotateAllBuildings(angle);
					SceneManager.getInstance().getUndoManager().addEdit(c);
				}
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						EnergyPanel.getInstance().updateProperties();
					}
				});
				return null;
			}
		});
	}

	private boolean onLand(final int x, final int y) {
		return SelectUtil.pickPart(x, y, land) != null;
	}

	Vector3 getPickedLocationOnLand() {
		if (pasteMouseState != null) {
			final PickedHousePart pick = SelectUtil.pickPart(pasteMouseState.getX(), pasteMouseState.getY(), land);
			if (pick != null) {
				return pick.getPoint().multiply(1, 1, 0, null);
			}
			pasteMouseState = null;
		}
		return null;
	}

	Vector3 getPickedLocationOnFoundation() {
		if (pasteMouseState != null) {
			final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
			if (selectedPart instanceof Foundation) {
				final PickedHousePart pick = SelectUtil.pickPart(pasteMouseState.getX(), pasteMouseState.getY(), selectedPart);
				if (pick != null) {
					return pick.getPoint().clone();
				}
			}
			pasteMouseState = null;
		}
		return null;
	}

	Vector3 getPickedLocationOnWall() {
		if (pasteMouseState != null) {
			final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
			if (selectedPart instanceof Wall) {
				final PickedHousePart pick = SelectUtil.pickPart(pasteMouseState.getX(), pasteMouseState.getY(), selectedPart);
				if (pick != null) {
					return pick.getPoint().clone();
				}
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
				if (pick != null) {
					return pick.getPoint().clone();
				}
			}
			pasteMouseState = null;
		}
		return null;
	}

	Vector3 getPickedLocationOnRack() {
		if (pasteMouseState != null) {
			final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
			if (selectedPart instanceof Rack) {
				final PickedHousePart pick = SelectUtil.pickPart(pasteMouseState.getX(), pasteMouseState.getY(), selectedPart);
				if (pick != null) {
					return pick.getPoint().clone();
				}
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
				if (ask) {
					JOptionPane.showMessageDialog(MainFrame.getInstance(), "You must select a building first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
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

}
