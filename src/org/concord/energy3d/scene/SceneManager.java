package org.concord.energy3d.scene;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.undo.UndoManager;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.CustomRoof;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HipRoof;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.PickedHousePart;
import org.concord.energy3d.model.PyramidRoof;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.UserData;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.CameraControl.ButtonAction;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.undo.AddHousePartCommand;
import org.concord.energy3d.undo.EditFoundationCommand;
import org.concord.energy3d.undo.EditHousePartCommand;
import org.concord.energy3d.undo.MakeGableCommand;
import org.concord.energy3d.undo.RemoveHousePartCommand;
import org.concord.energy3d.util.Blinker;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.FontManager;
import org.concord.energy3d.util.SelectUtil;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.extension.model.collada.jdom.ColladaAnimUtils;
import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.ColladaMaterialUtils;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.extension.shadow.map.ParallelSplitShadowMapPass;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.FrameHandler;
import com.ardor3d.framework.Updater;
import com.ardor3d.framework.jogl.JoglAwtCanvas;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.input.ButtonState;
import com.ardor3d.input.Key;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.MouseState;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.awt.AwtFocusWrapper;
import com.ardor3d.input.awt.AwtKeyboardWrapper;
import com.ardor3d.input.awt.AwtMouseManager;
import com.ardor3d.input.awt.AwtMouseWrapper;
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
import com.ardor3d.light.DirectionalLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Camera.ProjectionMode;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.TextureRendererFactory;
import com.ardor3d.renderer.jogl.JoglTextureRendererProvider;
import com.ardor3d.renderer.pass.BasicPassManager;
import com.ardor3d.renderer.pass.RenderPass;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.renderer.state.OffsetState;
import com.ardor3d.renderer.state.OffsetState.OffsetType;
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
import com.ardor3d.util.ContextGarbageCollector;
import com.ardor3d.util.GameTaskQueue;
import com.ardor3d.util.GameTaskQueueManager;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.Timer;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.ResourceSource;
import com.ardor3d.util.resource.SimpleResourceLocator;

public class SceneManager implements com.ardor3d.framework.Scene, Runnable, Updater {

	public enum Operation {
		SELECT, RESIZE, DRAW_WALL, DRAW_DOOR, DRAW_ROOF, DRAW_ROOF_HIP, DRAW_WINDOW, DRAW_FOUNDATION, DRAW_FLOOR, DRAW_ROOF_CUSTOM, DRAW_ROOF_GABLE
	}

	public enum CameraMode {
		ORBIT, FIRST_PERSON
	}

	public enum ViewMode {
		NORMAL, TOP_VIEW, PRINT_PREVIEW, PRINT
	}

	private static final double MOVE_SPEED = 5;

	private static final GameTaskQueueManager taskManager = GameTaskQueueManager.getManager("Task Manager");
	private static final SceneManager instance = new SceneManager(MainPanel.getInstance());
	private static final boolean JOGL = true;
	private final Canvas canvas;
	private final FrameHandler frameHandler;
	private final LogicalLayer logicalLayer;
	private final Node root = new Node("Root");
	private final Node backgroundRoot = new Node("Scenary Root");
	private final BasicPassManager passManager = new BasicPassManager();
	private final Mesh floor = new Quad("Floor", 200, 200);
	private final Mesh gridsMesh = new Line("Floor Grids");
	private final LightState lightState = new LightState();
	private final UndoManager undoManager = new UndoManager();
	private HousePart selectedHousePart = null;
	private HousePart hoveredHousePart = null;
	private Operation operation = Operation.SELECT;
	private Heliodon heliodon;
	private CameraControl cameraControl;
	private ParallelSplitShadowMapPass shadowPass;
	private ViewMode viewMode = ViewMode.NORMAL;
	private CameraNode cameraNode;
	private TwoInputStates moveState;
	private AddHousePartCommand addHousePartCommand;
	private EditHousePartCommand editHousePartCommand;
	private UserData pick;
	private double updateTime = -1;
	private long lastRenderTime;
	private boolean mouseControlEnabled = true;
	private boolean drawBounds = false;
	private boolean exit = false;
	private boolean rotAnim = false;
	private boolean sunControl;
	private boolean sunAnim;
	private boolean operationStick = false;
	private boolean operationFlag = false;
	private boolean update = true;
	private boolean zoomLock = false;

	public final static byte DEFAULT_THEME = 0;
	public final static byte SKETCHUP_THEME = 1;
	private byte theme = DEFAULT_THEME;

	public static SceneManager getInstance() {
		return instance;
	}

	public static GameTaskQueueManager getTaskManager() {
		return taskManager;
	}

	private SceneManager(final Container panel) {
		System.out.print("Constructing SceneManager...");
		// final DisplaySettings settings = new DisplaySettings(800, 600, 32, 60, 0, 8, 0, 0, false, false);
		final DisplaySettings settings = new DisplaySettings(800, 600, 32, 60, 0, 8, 0, 4, false, false);
		if (JOGL) {
			canvas = new JoglAwtCanvas(settings, new JoglCanvasRenderer(this));
			TextureRendererFactory.INSTANCE.setProvider(new JoglTextureRendererProvider());
		} else {
			// try {
			// canvas = new LwjglAwtCanvas(settings, new LwjglCanvasRenderer(this));
			// TextureRendererFactory.INSTANCE.setProvider(new LwjglTextureRendererProvider());
			// } catch (LWJGLException e) {
			// throw new RuntimeException(e);
			// }
		}

		frameHandler = new FrameHandler(new Timer());
		frameHandler.addCanvas(canvas);

		logicalLayer = new LogicalLayer();
		final AwtMouseWrapper mouseWrapper = new AwtMouseWrapper((Component) canvas, new AwtMouseManager((Component) canvas));
		final AwtKeyboardWrapper keyboardWrapper = new AwtKeyboardWrapper((Component) canvas);
		final AwtFocusWrapper focusWrapper = new AwtFocusWrapper((Component) canvas);
		final PhysicalLayer physicalLayer = new PhysicalLayer(keyboardWrapper, mouseWrapper, focusWrapper);
		logicalLayer.registerInput(canvas, physicalLayer);

		frameHandler.addUpdater(this);
		frameHandler.addUpdater(PrintController.getInstance());
		frameHandler.addUpdater(Blinker.getInstance());

		panel.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent e) {
				resizeCamera();
				update();
				if (heliodon != null)
					heliodon.updateBloom();
			}
		});
		panel.add((Component) canvas, BorderLayout.CENTER);
		System.out.println("done");
		System.out.print("Initializing SceneManager...");
		AWTImageLoader.registerLoader();
		try {
			ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, new SimpleResourceLocator(SceneManager.class.getClassLoader().getResource("org/concord/energy3d/resources/images/")));
			ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, new SimpleResourceLocator(SceneManager.class.getClassLoader().getResource("org/concord/energy3d/resources/")));
			ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, new SimpleResourceLocator(SceneManager.class.getClassLoader().getResource("org/concord/energy3d/resources/")));
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

		backgroundRoot.attachChild(createSky());
		backgroundRoot.attachChild(createFloor());
		drawGrids(0.1);
		backgroundRoot.attachChild(gridsMesh);
		backgroundRoot.attachChild(createAxis());
		root.attachChild(backgroundRoot);
		root.attachChild(Scene.getRoot());

		final RenderPass rootPass = new RenderPass();
		rootPass.add(root);
		passManager.add(rootPass);

		// shadowPass = new ParallelSplitShadowMapPass(light, 512, 3);
		shadowPass = new ParallelSplitShadowMapPass(light, 3072, 3);
		shadowPass.setEnabled(false);
		shadowPass.setUseObjectCullFace(true);
		shadowPass.add(floor);
		shadowPass.add(Scene.getRoot());
		shadowPass.addOccluder(Scene.getRoot());

		final Date today = Calendar.getInstance().getTime();
		heliodon = new Heliodon(root, light, passManager, logicalLayer, today);
		taskManager.update(new Callable<Object>() {
			public Object call() throws Exception {
				MainPanel.getInstance().getDateSpinner().setValue(today);
				MainPanel.getInstance().getTimeSpinner().setValue(today);
				return null;
			}
		});

		// Scene.getInstance();

		// SelectUtil.init(floor, Scene.getRoot());
		initMouse();

		root.updateGeometricState(0, true);
		System.out.println("Finished initialization.");
	}

	public synchronized void run() {
		frameHandler.init();
		while (!exit) {
			logicalLayer.checkTriggers(frameHandler.getTimer().getTimePerFrame());
			final double now = frameHandler.getTimer().getTimeInSeconds();
			final boolean isUpdateTime = updateTime != -1 && now <= updateTime;
			final boolean isTaskAvailable = taskManager.getQueue(GameTaskQueue.UPDATE).size() > 0 || taskManager.getQueue(GameTaskQueue.RENDER).size() > 0;
			final boolean isPrintPreviewAnim = !PrintController.getInstance().isFinished();
			if (update || isTaskAvailable || isPrintPreviewAnim || Scene.isRedrawAll() || isUpdateTime || rotAnim || Blinker.getInstance().getTarget() != null || sunAnim || cameraControl.isAnimating()) {
				if (now > updateTime)
					updateTime = -1;
				update = false;
				frameHandler.updateFrame();
			} else
				frameHandler.getTimer().update();
			final double syncNS = 1000000000.0 / 60.0;
			long sinceLast = System.nanoTime() - lastRenderTime;
			if (sinceLast < syncNS) {
				try {
					Thread.sleep(Math.round((syncNS - sinceLast) / 1000000L));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			lastRenderTime = System.nanoTime();
		}
	}

	public void update(final ReadOnlyTimer timer) {
		final double tpf = timer.getTimePerFrame();
		passManager.updatePasses(tpf);
		taskManager.getQueue(GameTaskQueue.UPDATE).execute(canvas.getCanvasRenderer().getRenderer());

		if (operationFlag)
			executeOperation();

		if (moveState != null)
			executeMouseMove();

		Scene.getInstance().update();

		if (rotAnim && viewMode == ViewMode.NORMAL) {
			final Matrix3 rotate = new Matrix3();
			rotate.fromAngleNormalAxis(45 * tpf * MathUtils.DEG_TO_RAD, Vector3.UNIT_Z);
			final Camera camera = canvas.getCanvasRenderer().getCamera();
			camera.setLocation(rotate.applyPre(camera.getLocation(), null));
			camera.lookAt(0, 0, 1, Vector3.UNIT_Z);
			getCameraNode().updateFromCamera();
		}

		if (sunAnim)
			heliodon.setHourAngle(heliodon.getHourAngle() + tpf * 0.5, true, true);

		heliodon.update();

		if (cameraControl.isAnimating())
			cameraControl.animate();

		root.updateGeometricState(tpf);
	}

	public boolean renderUnto(final Renderer renderer) {
		if (cameraNode == null)
			initCamera();

		if (drawBounds && selectedHousePart != null) {
			if (selectedHousePart instanceof Roof) {
				final Node flattenedMeshesRoot = ((Roof) selectedHousePart).getRoofPartsRoot();
				if (flattenedMeshesRoot != null && pick != null) {
					com.ardor3d.util.geom.Debugger.drawBounds(flattenedMeshesRoot.getChild(pick.getIndex()), renderer, true);
					// com.ardor3d.util.geom.Debugger.drawBounds(((Node) flattenedMeshesRoot.getChild(pick.getIndex())).getChild(0), renderer, true);
					System.out.println(flattenedMeshesRoot.getChild(pick.getIndex()).getWorldBound());
				}
			} else {
				if (selectedHousePart instanceof Wall)
					com.ardor3d.util.geom.Debugger.drawBounds(((Wall) selectedHousePart).getRoot().getChild(3), renderer, true);
				else
					com.ardor3d.util.geom.Debugger.drawBounds(selectedHousePart.getRoot(), renderer, true);
				if (selectedHousePart.getMesh() != null)
					System.out.println(selectedHousePart.getMesh().getWorldBound());
			}
		}

		// com.ardor3d.util.geom.Debugger.drawBounds(Scene.getInstance().getRoot(), renderer, true);

		passManager.renderPasses(renderer);
		try {
			shadowPass.renderPass(renderer);
		} catch (Exception e) {
			e.printStackTrace();
			shadowPass.setEnabled(false);
		}
		taskManager.getQueue(GameTaskQueue.RENDER).execute(renderer);
		return true;
	}

	public void initCamera() {
		cameraNode = new CameraNode("Camera Node", canvas.getCanvasRenderer().getCamera());
		root.attachChild(cameraNode);
		cameraNode.updateFromCamera();
		setCameraControl(CameraMode.ORBIT);
		resetCamera(ViewMode.NORMAL);

		taskManager.update(new Callable<Object>() {
			public Object call() throws Exception {
				final Spatial compass = createCompass();
				compass.setScale(0.1);
				compass.setTranslation(-1, -0.7, 2);
				cameraNode.attachChild(compass);
				return null;
			}
		});
	}

	public PickResults doPick(Ray3 pickRay) {
		return null;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	private Mesh createFloor() {
		switch (theme) {
		case DEFAULT_THEME:
			floor.setDefaultColor(new ColorRGBA(0, 1, 0, 0.5f));
			break;
		case SKETCHUP_THEME:
			floor.setDefaultColor(new ColorRGBA(1, 1, 1, 0.9f));
			break;
		}

		final OffsetState offsetState = new OffsetState();
		offsetState.setTypeEnabled(OffsetType.Fill, true);
		offsetState.setFactor(0.5f);
		offsetState.setUnits(0.5f);
		floor.setRenderState(offsetState);

		final BlendState blendState = new BlendState();
		blendState.setBlendEnabled(true);
		floor.setRenderState(blendState);
		floor.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);

		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		floor.setRenderState(ms);
		floor.updateModelBound();
		return floor;
	}

	public void drawGrids(final double gridSize) {
		gridsMesh.setDefaultColor(ColorRGBA.BLUE);
		// gridsMesh.setDefaultColor(new ColorRGBA(0, 0, 1, 1f));
		// final BlendState blend = new BlendState();
		// blend.setBlendEnabled(true);
		// gridsMesh.setRenderState(blend);

		gridsMesh.setModelBound(new BoundingBox());
		Util.disablePickShadowLight(gridsMesh);

		final ReadOnlyVector3 width = Vector3.UNIT_X.multiply(200, null);
		final ReadOnlyVector3 height = Vector3.UNIT_Y.multiply(200, null);
		final ArrayList<ReadOnlyVector3> points = new ArrayList<ReadOnlyVector3>();
		final ReadOnlyVector3 pMiddle = Vector3.ZERO;

		final int cols = (int) (width.length() / gridSize);

		for (int col = 0; col < cols / 2 + 1; col++) {
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

		for (int row = 0; row < rows / 2 + 1; row++) {
			for (int neg = -1; neg <= 1; neg += 2) {
				final ReadOnlyVector3 lineP1 = height.normalize(null).multiplyLocal(neg * row * gridSize).addLocal(pMiddle).subtractLocal(width.multiply(0.5, null));
				points.add(lineP1);
				final ReadOnlyVector3 lineP2 = lineP1.add(width, null);
				points.add(lineP2);
				if (row == 0)
					break;
			}
		}
		// if (points.size() < 2)
		// return;
		final FloatBuffer buf = BufferUtils.createVector3Buffer(points.size());
		for (final ReadOnlyVector3 p : points)
			buf.put(p.getXf()).put(p.getYf()).put(0.01f);

		gridsMesh.getMeshData().setVertexBuffer(buf);
		gridsMesh.getMeshData().updateVertexCount();
		gridsMesh.updateModelBound();
//		gridsMesh.getSceneHints().setCullHint(CullHint.Always);
	}

	public void setGridsVisible(final boolean visible) {
		gridsMesh.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
	}

	private Mesh createSky() {
		final Dome sky = new Dome("Sky", 100, 100, 100);
		sky.setRotation(new Matrix3().fromAngles(Math.PI / 2, 0, 0));
		final TextureState ts = new TextureState();
		if (theme == DEFAULT_THEME) {
			ts.setTexture(TextureManager.load("sky.jpg", Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
		}
		sky.setRenderState(ts);
		sky.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		return sky;
	}

	private Spatial createAxis() {
		final int axisLen = 100;
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

	private void initMouse() {

		if (!Config.isHeliodonMode())
			logicalLayer.registerTrigger(new InputTrigger(new MouseButtonPressedCondition(MouseButton.LEFT), new TriggerAction() {
				public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
					update = true;
					taskManager.update(new Callable<Object>() {
						public Object call() {
							MouseState mouseState = inputStates.getCurrent().getMouseState();
							if (operation == Operation.SELECT || operation == Operation.RESIZE || operation == Operation.DRAW_ROOF_GABLE) {
								if (selectedHousePart == null || selectedHousePart.isDrawCompleted()) {
									final HousePart previousSelectedHousePart = selectedHousePart;
									final UserData pick = SelectUtil.selectHousePart(mouseState.getX(), mouseState.getY(), true);
									if (pick == null)
										selectedHousePart = null;
									else
										selectedHousePart = pick.getHousePart();
									System.out.print("Clicked on: " + pick);
									if (pick != null && pick.isEditPoint())
										cameraControl.setMouseLeftButtonAction(ButtonAction.NONE);

									if (previousSelectedHousePart != null && previousSelectedHousePart != selectedHousePart) {
										previousSelectedHousePart.setEditPointsVisible(false);
										previousSelectedHousePart.setGridsVisible(false);
									}
									if (selectedHousePart != null && !PrintController.getInstance().isPrintPreview()) {
										selectedHousePart.setEditPointsVisible(true);
										if (pick.isEditPoint() && pick.getIndex() != -1) {
											selectedHousePart.setGridsVisible(true);
											if (selectedHousePart instanceof Foundation)
												editHousePartCommand = new EditFoundationCommand((Foundation) selectedHousePart);
											else
												editHousePartCommand = new EditHousePartCommand(selectedHousePart);
										}
									}
									SelectUtil.nextPickLayer();
									if (operation == Operation.DRAW_ROOF_GABLE && selectedHousePart instanceof Roof) {
										System.out.println(selectedHousePart);
										System.out.println("deleting roof #" + pick.getIndex());
										final int roofPartIndex = pick.getIndex();
										final Roof roof = (Roof) selectedHousePart;
										undoManager.addEdit(new MakeGableCommand(roof, roofPartIndex));
										roof.setGable(roofPartIndex, true);
										MainFrame.getInstance().refreshUndoRedo();
									}
								}
							} else {
								// selectedHousePart.setPreviewPoint(mouseState.getX(), mouseState.getY());
								selectedHousePart.addPoint(mouseState.getX(), mouseState.getY());

							}
							// enableDisableRotationControl();
							return null;
						}
					});
				}
			}));

		if (!Config.isHeliodonMode())
			logicalLayer.registerTrigger(new InputTrigger(new MouseButtonReleasedCondition(MouseButton.LEFT), new TriggerAction() {
				public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
					update = true;
					taskManager.update(new Callable<Object>() {
						public Object call() {
							if (selectedHousePart != null)
								selectedHousePart.setGridsVisible(false);
							MouseState mouseState = inputStates.getCurrent().getMouseState();
							boolean sceneChanged = false;
							if (operation == Operation.SELECT || operation == Operation.RESIZE) {
								if (selectedHousePart != null && !selectedHousePart.isDrawCompleted()) {
									if (selectedHousePart.isDrawable())
										selectedHousePart.complete();
									else {
										editHousePartCommand.undo();
										selectedHousePart.reset();
										selectedHousePart.draw();
										undoManager.addEdit(new RemoveHousePartCommand(selectedHousePart));
										MainFrame.getInstance().refreshUndoRedo();
										Scene.getInstance().remove(selectedHousePart);
										selectedHousePart = null;
									}
									sceneChanged = true;
									if (editHousePartCommand != null) {
										if (editHousePartCommand.isReallyEdited()) {
											undoManager.addEdit(editHousePartCommand);
											MainFrame.getInstance().refreshUndoRedo();
										}
										editHousePartCommand = null;
									}
								}
								if (!zoomLock)
									cameraControl.setMouseLeftButtonAction(ButtonAction.ROTATE);
							} else {
								if (!selectedHousePart.isDrawCompleted()) {
									selectedHousePart.addPoint(mouseState.getX(), mouseState.getY());
									if (selectedHousePart.isDrawCompleted() && !selectedHousePart.isDrawable()) {
										addHousePartCommand = null;
										Scene.getInstance().remove(selectedHousePart);
										selectedHousePart = null;
										selectedHousePart = null;
										if (operationStick)
											operationFlag = true;
										// else
										// MainPanel.getInstance().deselect();
									}
									sceneChanged = true;
								}
								if (selectedHousePart != null && selectedHousePart.isDrawCompleted()) {
									if (addHousePartCommand != null) {
										undoManager.addEdit(addHousePartCommand);
										MainFrame.getInstance().refreshUndoRedo();
										addHousePartCommand = null;
									}
									selectedHousePart.setEditPointsVisible(false);
									selectedHousePart = null;
									if (operationStick)
										operationFlag = true;
									// else
									// MainPanel.getInstance().deselect();
								}
								if (!operationFlag) {
									MainPanel.getInstance().deselect();
									cameraControl.setMouseLeftButtonAction(ButtonAction.ROTATE);
								}
							}
							// if (HousePart.getGridsHighlightedHousePart() != null) {
							// HousePart.getGridsHighlightedHousePart().setGridsVisible(false);
							// HousePart.setGridsHighlightedHousePart(null);
							// }
							// if (!operationFlag)
							// cameraControl.setMouseLeftButtonAction(ButtonAction.MOVE);
							// enableDisableRotationControl();
							if (sceneChanged)
								updateHeliodonAndAnnotationSize();
							return null;
						}
					});
				}
			}));

		if (!Config.isHeliodonMode())
			logicalLayer.registerTrigger(new InputTrigger(new MouseMovedCondition(), new TriggerAction() {
				public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
					update = true;
					moveState = inputStates;
				}
			}));

		logicalLayer.registerTrigger(new InputTrigger(new MouseButtonClickedCondition(MouseButton.LEFT), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (inputStates.getCurrent().getMouseState().getClickCount(MouseButton.LEFT) == 2) {
					final PickedHousePart pickedHousePart = SelectUtil.pickPart(inputStates.getCurrent().getMouseState().getX(), inputStates.getCurrent().getMouseState().getY(), root);
					final Vector3 clickedPoint = pickedHousePart.getPoint();
					cameraControl.zoomAtPoint(clickedPoint);
				}
			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.LSHIFT), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				SelectUtil.setPickLayer(0);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyReleasedCondition(Key.LSHIFT), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				SelectUtil.setPickLayer(-1);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.DELETE), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				taskManager.update(new Callable<Object>() {
					public Object call() throws Exception {
						undoManager.addEdit(new RemoveHousePartCommand(selectedHousePart));
						MainFrame.getInstance().refreshUndoRedo();
						Scene.getInstance().remove(selectedHousePart);
						selectedHousePart = null;
						return null;
					}
				});
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.BACK), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				taskManager.update(new Callable<Object>() {
					public Object call() throws Exception {
						undoManager.addEdit(new RemoveHousePartCommand(selectedHousePart));
						MainFrame.getInstance().refreshUndoRedo();
						Scene.getInstance().remove(selectedHousePart);
						selectedHousePart = null;
						return null;
					}
				});
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.ESCAPE), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				hideAllEditPoints();
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.Q), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				// moveUpDown(source, tpf, true);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.Z), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				// if (!inputStates.getCurrent().getKeyboardState().isAtLeastOneDown(Key.LCONTROL, Key.RCONTROL))
				// moveUpDown(source, tpf, false);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.W), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (viewMode == ViewMode.TOP_VIEW)
					moveUpDown(source, tpf, true);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.S), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (viewMode == ViewMode.TOP_VIEW)
					moveUpDown(source, tpf, false);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.ZERO), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				resetCamera(viewMode);
				update = true;
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.X), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				heliodon.setHourAngle(heliodon.getHourAngle() + 0.03, true, true);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.Z), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				heliodon.setHourAngle(heliodon.getHourAngle() - 0.03, true, true);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.UP), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (!sunControl)
					return;
				heliodon.setObserverLatitude(heliodon.getObserverLatitude() + 0.01);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.DOWN), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (!sunControl)
					return;
				heliodon.setObserverLatitude(heliodon.getObserverLatitude() - 0.01);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.RIGHT), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (!sunControl)
					return;
				heliodon.setDeclinationAngle(heliodon.getDeclinationAngle() + 0.01, true, true);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.LEFT), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (!sunControl)
					return;
				heliodon.setDeclinationAngle(heliodon.getDeclinationAngle() - 0.01, true, true);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.B), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				drawBounds = !drawBounds;
				if (drawBounds)
					System.out.println("Enabling draw bounds...");
				else
					System.out.println("Disabling draw bounds...");
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.I), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				System.out.println("---- Parts: ------------------------");
				System.out.println("size = " + Scene.getInstance().getParts().size());
				for (final HousePart part : Scene.getInstance().getParts())
					System.out.println(part);
				System.out.println("---- Scene: ------------------------");
				System.out.println("size = " + Scene.getInstance().getOriginalHouseRoot().getNumberOfChildren());
				for (final Spatial mesh : Scene.getInstance().getOriginalHouseRoot().getChildren()) {
					System.out.println(mesh);
				}
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.R), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				Scene.getInstance().redrawAll();
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.X), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				Scene.getInstance().removeAllRoofs();
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
		cameraControl.setupKeyboardTriggers(logicalLayer);
		cameraControl.setupMouseTriggers(logicalLayer, true);
		cameraControl.setMoveSpeed(MOVE_SPEED);
		cameraControl.setKeyRotateSpeed(1);
	}

	public void hideAllEditPoints() {
		for (HousePart part : Scene.getInstance().getParts()) {
			part.setEditPointsVisible(false);
			part.setGridsVisible(false);
		}
		update = true;
	}

	public void resetCamera(final ViewMode viewMode) {
		this.viewMode = viewMode;
		final Camera camera = canvas.getCanvasRenderer().getCamera();

		// cameraControl.setMouseButtonActions(ButtonAction.ROTATE, ButtonAction.MOVE);
		cameraControl.setMouseButtonActions(ButtonAction.ROTATE, ButtonAction.MOVE);
		cameraControl.setMoveSpeed(MOVE_SPEED);
		// ReadOnlyVector3 loc = new Vector3(1.0f, -10.0f, 6.0f);
		ReadOnlyVector3 loc = new Vector3(1.0f, -5.0f, 3.0f);
		ReadOnlyVector3 left = new Vector3(-1.0f, 0.0f, 0.0f);
		ReadOnlyVector3 up = new Vector3(0.0f, 0.0f, 1.0f);
		ReadOnlyVector3 lookAt = new Vector3(0.0f, 0.0f, 0.0f);

		setCompassVisible(viewMode == ViewMode.NORMAL);

		if (viewMode == ViewMode.NORMAL) {
			camera.setProjectionMode(ProjectionMode.Perspective);
			resizeCamera();
		} else if (viewMode == ViewMode.TOP_VIEW) {
			camera.setProjectionMode(ProjectionMode.Parallel);
			cameraControl.setMouseButtonActions(ButtonAction.MOVE, ButtonAction.NONE);
			cameraControl.setMoveSpeed(5 * MOVE_SPEED);
			loc = new Vector3(0, 0, 10);
			up = new Vector3(0.0f, -1.0f, 0.0f);
			lookAt = new Vector3(0.0f, 0.0f, -1.0f);
			resizeCamera(2);
		} else if (viewMode == ViewMode.PRINT) {
			cameraControl.setMouseButtonActions(ButtonAction.MOVE, ButtonAction.MOVE);
			camera.setProjectionMode(ProjectionMode.Parallel);
			/* location will be set in PrintController.print() */
			// loc = new Vector3(0, -10, 0);
			final double pageWidth = PrintController.getInstance().getPageWidth();
			final double pageHeight = PrintController.getInstance().getPageHeight();
			final Dimension canvasSize = ((Component) canvas).getSize();
			final double ratio = (double) canvasSize.width / canvasSize.height;

			if (ratio > pageWidth / pageHeight)
				resizeCamera(pageHeight * ratio);
			else
				resizeCamera(pageWidth);
		} else if (viewMode == ViewMode.PRINT_PREVIEW) {
			cameraControl.setMouseButtonActions(ButtonAction.MOVE, ButtonAction.MOVE);
			camera.setProjectionMode(ProjectionMode.Perspective);
			// final int rows = PrintController.getInstance().getRows();
			// final double pageHeight = PrintController.getInstance().getPageHeight() + PrintController.getMargin();
			// final double w = PrintController.getInstance().getCols() * (PrintController.getInstance().getPageWidth() + PrintController.getMargin());
			// final double h = rows * pageHeight;
			// loc = new Vector3(0, -Math.max(w, h), rows % 2 != 0 ? 0 : pageHeight / 2);
			loc = PrintController.getInstance().getZoomAllCameraLocation();
			lookAt = loc.add(0, 1, 0, null);
			resizeCamera(PrintController.getInstance().getPageWidth());
		}

		camera.setFrame(loc, left, up, lookAt);
		camera.lookAt(lookAt, Vector3.UNIT_Z);

		cameraControl.reset();
		cameraNode.updateFromCamera();
	}

	private void resizeCamera() {
		resizeCamera(2);
	}

	private void resizeCamera(final double orthoWidth) {
		final Camera camera = canvas.getCanvasRenderer().getCamera();
		if (camera == null)
			return;
		final Dimension size = ((Component) canvas).getSize();
		camera.resize(size.width, size.height);
		final double ratio = (double) size.width / size.height;

		final double near = 1;
		final double far = 1000;
		if (camera.getProjectionMode() == ProjectionMode.Parallel)
			camera.setFrustum(near, far, -orthoWidth / 2, orthoWidth / 2, -orthoWidth / ratio / 2, orthoWidth / ratio / 2);
		else
			camera.setFrustumPerspective(45.0, ratio, near, far);
	}

	public void toggleRotation() {
		rotAnim = !rotAnim;
	}

	private void moveUpDown(final Canvas canvas, final double tpf, boolean up) {
		final Camera camera = canvas.getCanvasRenderer().getCamera();
		final Vector3 loc = new Vector3(camera.getUp());
		if (viewMode == ViewMode.TOP_VIEW)
			up = !up;
		loc.multiplyLocal((up ? 1 : -1) * MOVE_SPEED * tpf).addLocal(camera.getLocation());
		camera.setLocation(loc);
		cameraNode.updateFromCamera();
		SceneManager.getInstance().update();
	}

	public void setOperation(Operation operation) {
		this.operationStick = false;
		this.operation = operation;
		this.operationFlag = true;
		update();
	}

	public void setOperationStick(boolean stick) {
		this.operationStick = stick;
	}

	public void executeOperation() {
		this.operationFlag = false;
		if (selectedHousePart != null) {
			if (selectedHousePart.isDrawCompleted())
				selectedHousePart.setEditPointsVisible(false);
			else
				Scene.getInstance().remove(selectedHousePart);
		}

		for (HousePart part : Scene.getInstance().getParts())
			if (part instanceof Foundation)
				((Foundation) part).setResizeHouseMode(operation == Operation.RESIZE);

		if (viewMode != ViewMode.PRINT_PREVIEW)
			Scene.getInstance().drawResizeBounds();

		selectedHousePart = newHousePart();
		if (selectedHousePart != null)
			cameraControl.setMouseLeftButtonAction(ButtonAction.NONE);
		// enableDisableRotationControl();
	}

	private HousePart newHousePart() {
		final HousePart drawn;
		setGridsVisible(false);
		if (operation == Operation.DRAW_WALL) {
			drawn = new Wall();
			setGridsVisible(true);
		} else if (operation == Operation.DRAW_DOOR)
			drawn = new Door();
		else if (operation == Operation.DRAW_WINDOW)
			drawn = new Window();
		else if (operation == Operation.DRAW_ROOF)
			drawn = new PyramidRoof();
		else if (operation == Operation.DRAW_ROOF_HIP)
			drawn = new HipRoof();
		else if (operation == Operation.DRAW_ROOF_CUSTOM)
			drawn = new CustomRoof();
		else if (operation == Operation.DRAW_FLOOR)
			drawn = new Floor();
		else if (operation == Operation.DRAW_FOUNDATION) {
			drawn = new Foundation();
			setGridsVisible(true);
		} else
			return null;

		Scene.getInstance().add(drawn);
		addHousePartCommand = new AddHousePartCommand(drawn);
		return drawn;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setShading(final boolean enable) {
		taskManager.update(new Callable<Object>() {
			public Object call() throws Exception {
				lightState.setEnabled(enable);
				root.updateWorldRenderStates(true);
				return null;
			}
		});
	}

	public void setSunControl(boolean selected) {
		this.sunControl = selected;
		heliodon.setVisible(selected);
		enableDisableRotationControl();
	}

	public void setSunAnim(boolean selected) {
		this.sunAnim = selected;
	}

	public void enableDisableRotationControl() {
		if (!mouseControlEnabled)
			return;

		if ((operation == Operation.SELECT || operation == Operation.RESIZE) && (selectedHousePart == null || selectedHousePart.isDrawCompleted()))
			cameraControl.setMouseEnabled(true);
		else
			cameraControl.setMouseEnabled(false);

		if (sunControl)
			cameraControl.setKeyRotateSpeed(0);
		else
			cameraControl.setKeyRotateSpeed(1);
	}

	public HousePart getSelectedPart() {
		return selectedHousePart;
	}

	public boolean isTopView() {
		return viewMode == ViewMode.TOP_VIEW;
	}

	public void exit() {
		System.out.print("exiting...");
		this.exit = true;
		canvas.getCanvasRenderer().makeCurrentContext();
		ContextGarbageCollector.doFinalCleanup(canvas.getCanvasRenderer().getRenderer());
		System.out.println("done");
		System.exit(0);
	}

	public void updatePrintPreviewScene(boolean printPreview) {
		resetCamera(printPreview ? ViewMode.PRINT_PREVIEW : ViewMode.NORMAL);
		backgroundRoot.getSceneHints().setCullHint(printPreview ? CullHint.Always : CullHint.Inherit);
		backgroundRoot.getSceneHints().setAllPickingHints(false);
	}

	public void setShadow(final boolean shadow) {
		taskManager.update(new Callable<Object>() {
			public Object call() throws Exception {
				shadowPass.setEnabled(shadow);
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
			public void update(double time, Spatial caller) {
				final Vector3 direction = canvas.getCanvasRenderer().getCamera().getDirection().normalize(null);
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

	public void setCompassVisible(boolean visible) {
		cameraNode.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
	}

	public void updateHeliodonAndAnnotationSize() {
		if (sunControl)
			taskManager.update(new Callable<Object>() {
				public Object call() throws Exception {
					// Scene.getInstance().updateTextSizes();
					heliodon.updateSize();
					return null;
				}
			});
	}

	public void executeMouseMove() {
		if (!mouseControlEnabled)
			return;
		final MouseState mouseState = moveState.getCurrent().getMouseState();
		moveState = null;
		int x = mouseState.getX();
		int y = mouseState.getY();

		if (selectedHousePart != null && !selectedHousePart.isDrawCompleted()) {
			selectedHousePart.setPreviewPoint(x, y);
		} else if (operation == Operation.SELECT && mouseState.getButtonState(MouseButton.LEFT) == ButtonState.UP && mouseState.getButtonState(MouseButton.MIDDLE) == ButtonState.UP && mouseState.getButtonState(MouseButton.RIGHT) == ButtonState.UP) {
			pick = SelectUtil.selectHousePart(x, y, false);
			if (pick != null) {
				if (hoveredHousePart != null && hoveredHousePart != selectedHousePart && hoveredHousePart != pick.getHousePart())
					hoveredHousePart.setEditPointsVisible(false);
				hoveredHousePart = pick.getHousePart();
				if (hoveredHousePart != null && hoveredHousePart != selectedHousePart && !PrintController.getInstance().isPrintPreview())
					hoveredHousePart.setEditPointsVisible(true);
			} else {
				if (hoveredHousePart != null && hoveredHousePart != selectedHousePart)
					hoveredHousePart.setEditPointsVisible(false);
				hoveredHousePart = null;
			}
		}
	}

	public ViewMode getViewMode() {
		return viewMode;
	}

	public boolean isRotationAnimationOn() {
		return rotAnim;
	}

	public void setMouseControlEnabled(final boolean enabled) {
		this.mouseControlEnabled = enabled;
		cameraControl.setMouseEnabled(enabled);
	}

	public Heliodon getHeliodon() {
		return heliodon;
	}

	@Override
	public void init() {
		if (JOGL)
			initCamera();
		if (Config.isHeliodonMode())
			MainPanel.getInstance().getHeliodonButton().setSelected(true);

	}

	public boolean isShadingEnabled() {
		return lightState.isEnabled();
	}

	public boolean isShadowEnaled() {
		return shadowPass.isEnabled();
	}

	public void setZoomLock(boolean zoomLock) {
		this.zoomLock = zoomLock;
		cameraControl.setMouseButtonActions(zoomLock ? ButtonAction.ZOOM : ButtonAction.MOVE, ButtonAction.ROTATE);
	}

	public void update() {
		update = true;
	}

	public void update(final double updateTime) {
		this.updateTime = frameHandler.getTimer().getTimeInSeconds() + updateTime;
	}

	public UndoManager getUndoManager() {
		return undoManager;
	}

	public Timer getTimer() {
		return frameHandler.getTimer();
	}

	public Spatial getFloor() {
		return floor;
	}
}
