package org.concord.energy3d.scene;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.concurrent.Callable;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HipRoof;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.PyramidRoof;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.CameraControl.ButtonAction;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.util.Blinker;
import org.concord.energy3d.util.FontManager;
import org.concord.energy3d.util.SelectUtil;
import org.concord.energy3d.util.Util;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.tools.ardor3d.ArdorMeshMapper;

import com.ardor3d.annotation.MainThread;
import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.extension.effect.bloom.BloomRenderPass;
import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
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
import com.ardor3d.input.logical.MouseButtonPressedCondition;
import com.ardor3d.input.logical.MouseButtonReleasedCondition;
import com.ardor3d.input.logical.MouseMovedCondition;
import com.ardor3d.input.logical.MouseWheelMovedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.light.DirectionalLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Transform;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Camera.ProjectionMode;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.TextureRendererFactory;
import com.ardor3d.renderer.jogl.JoglTextureRendererProvider;
import com.ardor3d.renderer.pass.BasicPassManager;
import com.ardor3d.renderer.pass.RenderPass;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.ClipState;
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
import com.ardor3d.scenegraph.shape.Cylinder;
import com.ardor3d.scenegraph.shape.Dome;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.scenegraph.shape.Sphere;
import com.ardor3d.ui.text.BMText;
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
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class SceneManager implements com.ardor3d.framework.Scene, Runnable, Updater {
	public enum Operation {
		SELECT, RESIZE, DRAW_WALL, DRAW_DOOR, DRAW_ROOF, DRAW_ROOF_HIP, DRAW_WINDOW, DRAW_FOUNDATION, DRAW_FLOOR
	}

	public enum CameraMode {
		ORBIT, FIRST_PERSON
	}

	public enum ViewMode {
		NORMAL, TOP_VIEW, PRINT_PREVIEW, PRINT
	}

	private static final double MOVE_SPEED = 5;

	static public final GameTaskQueueManager taskManager = GameTaskQueueManager.getManager("Task Manager");
	static private final SceneManager instance = new SceneManager(MainFrame.getInstance().getContentPane());
	static private final boolean JOGL = true;
	private final Canvas canvas;
	private final FrameHandler frameHandler;
	private final LogicalLayer logicalLayer;
	private final Node root = new Node("Root");
	private final Node backgroundRoot = new Node("Scenary Root");
	private final BasicPassManager passManager = new BasicPassManager();
	private final Mesh floor = new Quad("Floor", 200, 200);
	private final LightState lightState = new LightState();
	private boolean exit = false;
	private boolean rotAnim = false;
	private HousePart drawn = null;
	private Operation operation = Operation.SELECT;
	private double sunAngle = 45, sunBaseAngle = 135;
	private Heliodon heliodon;
	private CameraControl control;
	private ParallelSplitShadowMapPass shadowPass;
	private Sphere sun;
	private Node sunHeliodon;
	private Node sunRot;
	private boolean sunControl;
	private boolean sunAnim;
	private BloomRenderPass bloomRenderPass;
	private ViewMode viewMode = ViewMode.NORMAL;
	private boolean operationFlag = false;
	private CameraNode cameraNode;
	private boolean operationStick = false;
	private TwoInputStates moveState;
	private boolean drawBounds = false;
	private long lastRenderTime;

	public static SceneManager getInstance() {
		return instance;
	}

	private SceneManager(final Container panel) {// throws LWJGLException {
		System.out.print("Initializing scene manager...");
		// instance = this;
		// root.attachChild(Scene.getRoot());

		// final DisplaySettings settings = new DisplaySettings(800, 600, 32, 60, 0, 8, 0, 0, false, false);
		final DisplaySettings settings = new DisplaySettings(800, 600, 32, 60, 0, 8, 0, 8, false, false);
		if (JOGL)
			canvas = new JoglAwtCanvas(settings, new JoglCanvasRenderer(this));
		// else
		// canvas = new LwjglAwtCanvas(settings, new LwjglCanvasRenderer(this));

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

		if (JOGL)
			TextureRendererFactory.INSTANCE.setProvider(new JoglTextureRendererProvider());
		// else
		// TextureRendererFactory.INSTANCE.setProvider(new LwjglTextureRendererProvider());

		panel.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent e) {
				resizeCamera();
			}
		});
		panel.add((Component) canvas, "Center");
		System.out.println("done");
	}

	@MainThread
	public void init() {
		System.out.print("Initializing scene manager models...");
		AWTImageLoader.registerLoader();
		try {
			ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, new SimpleResourceLocator(SceneManager.class.getClassLoader().getResource("org/concord/energy3d/resources/images/")));
			ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, new SimpleResourceLocator(SceneManager.class.getClassLoader().getResource("org/concord/energy3d/resources/")));
			ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, new SimpleResourceLocator(SceneManager.class.getClassLoader().getResource("org/concord/energy3d/resources/")));
		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		cameraNode = new CameraNode("Camera Node", canvas.getCanvasRenderer().getCamera());
		root.attachChild(cameraNode);
		cameraNode.updateFromCamera();
		setCameraControl(CameraMode.ORBIT);
		resetCamera(ViewMode.NORMAL);

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
		backgroundRoot.attachChild(createAxis());
		root.attachChild(backgroundRoot);
		root.attachChild(Scene.getRoot());

		final RenderPass rootPass = new RenderPass();
		rootPass.add(root);
		passManager.add(rootPass);

		// shadowPass = new ParallelSplitShadowMapPass(light, 512, 3);
		shadowPass = new ParallelSplitShadowMapPass(light, 3072, 3);
		shadowPass.setUseObjectCullFace(true);
		shadowPass.add(floor);
		shadowPass.add(Scene.getRoot());
		shadowPass.addOccluder(Scene.getRoot());

//		createSunHeliodon();
		heliodon = new Heliodon(root, light, passManager);
//		updateSunHeliodon();
		Scene.getInstance();

		SelectUtil.init(floor, Scene.getRoot());
		registerInputTriggers();

		taskManager.update(new Callable<Object>() {
			public Object call() throws Exception {
				final Spatial compass = loadCompassModel();
				compass.setScale(0.1);
				compass.setTranslation(-1, -0.7, 2);
				cameraNode.attachChild(compass);
				return null;
			}
		});

		root.updateGeometricState(0, true);
		System.out.println("done");
	}

	protected void test() {
		PolygonPoint p[] = new PolygonPoint[4];
		p[0] = new PolygonPoint(-1.0292539544430312, 0.09999999999999999, 1.362032135164121);
		p[1] = new PolygonPoint(0.8537302277848444, 0.09999999999999991, 1.362032135164121);
		p[2] = new PolygonPoint(0.8537302277848444, 1.1, 1.362032135164121);
		p[3] = new PolygonPoint(-1.0292539544430312, 1.1, 1.362032135164121);
		Polygon polygon = new Polygon(p);

		double data[] = new double[] { -0.8409555362202437, 0.8999999999999999, 1.362032135164121, -0.5114333043303654, 0.8999999999999999, 1.362032135164121, -0.5114333043303654, 0.6, 1.362032135164121, -0.8409555362202437, 0.6, 1.362032135164121, -0.3702094906632748, 0.8999999999999999, 1.362032135164121, -0.08776186332909341, 0.8999999999999999, 1.362032135164121, -0.08776186332909341, 0.6, 1.362032135164121, -0.3702094906632748, 0.6, 1.362032135164121, 0.053461950337997166, 0.8999999999999999, 1.362032135164121, 0.33590957767217855, 0.8999999999999999, 1.362032135164121, 0.33590957767217855, 0.6, 1.362032135164121, 0.053461950337997166, 0.6, 1.362032135164121, 0.47713339133926946, 0.8999999999999999, 1.362032135164121, 0.6654318095620568, 0.8999999999999999, 1.362032135164121, 0.6654318095620568, 0.5999999999999999, 1.362032135164121, 0.47713339133926946, 0.5999999999999999, 1.362032135164121, -0.7938809316645468, 0.44999999999999996, 1.362032135164121, -0.5114333043303654,
				0.44999999999999996, 1.362032135164121, -0.5114333043303654, 0.14999999999999997, 1.362032135164121, -0.7938809316645468, 0.14999999999999997, 1.362032135164121, -0.3702094906632748, 0.44999999999999996, 1.362032135164121, -0.08776186332909341, 0.44999999999999996, 1.362032135164121, -0.08776186332909341, 0.14999999999999994, 1.362032135164121, -0.3702094906632748, 0.14999999999999997, 1.362032135164121, 0.053461950337997166, 0.44999999999999996, 1.362032135164121, 0.33590957767217855, 0.4499999999999999, 1.362032135164121, 0.33590957767217855, 0.1499999999999999, 1.362032135164121, 0.053461950337997166, 0.14999999999999994, 1.362032135164121, 0.47713339133926946, 0.4499999999999999, 1.362032135164121, 0.6654318095620568, 0.4499999999999999, 1.362032135164121, 0.6654318095620568, 0.1499999999999999, 1.362032135164121, 0.47713339133926946, 0.1499999999999999, 1.362032135164121 };

		for (int i = 0; i < data.length; i += 12) {
			p = new PolygonPoint[] { new PolygonPoint(data[i], data[i + 1], data[i + 2]), new PolygonPoint(data[i + 3], data[i + 4], data[i + 5]), new PolygonPoint(data[i + 6], data[i + 7], data[i + 8]), new PolygonPoint(data[i + 9], data[i + 10], data[i + 11]) };
			Polygon hole = new Polygon(p);
			polygon.addHole(hole);
		}

		Mesh mesh = new Mesh();
		mesh.setDefaultColor(ColorRGBA.BLUE);
		root.attachChild(mesh);

		Poly2Tri.triangulate(polygon);
		ArdorMeshMapper.updateTriangleMesh(mesh, polygon);
	}

	public void run() {
		frameHandler.init();
		while (!exit) {
			frameHandler.updateFrame();
			final double syncNS = 1000000000.0 / 60;
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
		HousePart.clearDrawFlags();
		passManager.updatePasses(tpf);
		logicalLayer.checkTriggers(tpf);

		taskManager.getQueue(GameTaskQueue.UPDATE).execute(canvas.getCanvasRenderer().getRenderer());
		Scene.getInstance().update();

		if (rotAnim && viewMode == ViewMode.NORMAL) {
			final Matrix3 rotate = new Matrix3();
			rotate.fromAngleNormalAxis(45 * tpf * MathUtils.DEG_TO_RAD, Vector3.UNIT_Z);
			final Camera camera = canvas.getCanvasRenderer().getCamera();
			camera.setLocation(rotate.applyPre(camera.getLocation(), null));
			camera.lookAt(0, 0, 1, Vector3.UNIT_Z);
			getCameraNode().updateFromCamera();
		}

		if (sunAnim) {
//			sunAngle++;
			heliodon.setSunAngle(heliodon.getSunAngle() + 1);
//			updateSunHeliodon();
		}

		root.updateGeometricState(tpf);
	}

	public boolean renderUnto(Renderer renderer) {
		// try {
		// final boolean doRender = moveState != null;

		if (moveState != null)
			executeMouseMove();
		// else
		// return false;

		if (operationFlag)
			executeOperation();

		// if (!Scene.getInstance().getParts().isEmpty())
		// Scene.getInstance().renderTexture(renderer);
		// Scene.getInstance().init();

		if (drawBounds && drawn != null)
			com.ardor3d.util.geom.Debugger.drawBounds(drawn.getRoot(), renderer, true);

		// com.ardor3d.util.geom.Debugger.drawBounds(Scene.getInstance().getOriginalHouseRoot(), renderer, true);

		// if (doRender)
		passManager.renderPasses(renderer);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		return true;
	}

//	private Spatial createSunHeliodon() {
//		sunHeliodon = new Node();
//		Cylinder cyl = new Cylinder("Sun Curve", 10, 50, 5, 0.3);
//		Transform trans = new Transform();
//		trans.setMatrix(new Matrix3().fromAngleAxis(Math.PI / 2, Vector3.UNIT_X));
//		cyl.setDefaultColor(ColorRGBA.YELLOW);
//		cyl.setTransform(trans);
//		sunHeliodon.attachChild(cyl);
//
//		final ClipState cs = new ClipState();
//		cs.setEnableClipPlane(0, true);
//		cs.setClipPlaneEquation(0, 0, 0, 1, -0.19);
//		cyl.setRenderState(cs);
//
//		Cylinder baseCyl = new Cylinder("Sun Curve", 10, 50, 5, 0.2);
//		baseCyl.setTranslation(0, 0, 0.1);
//		sunHeliodon.attachChild(baseCyl);
//
//		sun = new Sphere("Sun", 20, 20, 0.3);
//		sun.setTranslation(0, 0, 5);
//		sunRot = new Node("Sun Root");
//		sunRot.attachChild(sun);
//		sunHeliodon.attachChild(sunRot);
//
//		reverseNormals(sun.getMeshData().getNormalBuffer());
//
//		MaterialState material = new MaterialState();
//		material.setEmissive(ColorRGBA.WHITE);
//		sun.setRenderState(material);
//
//		return sunHeliodon;
//	}

	private void reverseNormals(FloatBuffer normalBuffer) {
		normalBuffer.rewind();
		int i = 0;
		while (normalBuffer.hasRemaining()) {
			float f = normalBuffer.get();
			normalBuffer.position(i);
			normalBuffer.put(-f);
			i++;
		}
	}

	private void updateSunHeliodon() {
		if (sunAnim)
			sunAngle %= 180;
		else {
			sunAngle = Math.max(sunAngle, 1);
			sunAngle = Math.min(sunAngle, 179);
		}
		final Matrix3 m = new Matrix3().fromAngleAxis((-90 + sunAngle) * Math.PI / 180, Vector3.UNIT_Y);
		sunRot.setRotation(m);

		sunBaseAngle = sunBaseAngle % 360;
		sunHeliodon.setRotation(new Matrix3().fromAngleAxis(sunBaseAngle * Math.PI / 180, Vector3.UNIT_Z));

		DirectionalLight light = (DirectionalLight) lightState.get(0);
		sunHeliodon.updateWorldTransform(true);
		light.setDirection(sun.getWorldTranslation().negate(null));
		sunHeliodon.updateGeometricState(0);
	}

	public PickResults doPick(Ray3 pickRay) {
		return null;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	private Mesh createFloor() {
		// floor = new Quad("Floor", 200, 200);
		floor.setDefaultColor(new ColorRGBA(0, 1, 0, 0.5f));

		final BlendState blendState = new BlendState();
		blendState.setBlendEnabled(true);
		blendState.setTestEnabled(true);
		floor.setRenderState(blendState);
		floor.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
		floor.getSceneHints().setLightCombineMode(LightCombineMode.Off);

		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		floor.setRenderState(ms);
		floor.updateModelBound();
		return floor;
	}

	private Mesh createSky() {
		final Dome sky = new Dome("Sky", 100, 100, 100);
		sky.setRotation(new Matrix3().fromAngles(Math.PI / 2, 0, 0));
		final TextureState ts = new TextureState();
		ts.setTexture(TextureManager.load("sky.jpg", Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
		sky.setRenderState(ts);
		sky.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		return sky;
	}

	private Spatial createAxis() {
		final int axisLen = 100;

		final FloatBuffer verts = BufferUtils.createVector3Buffer(12);
		verts.put(0).put(0).put(0);
		verts.put(-axisLen).put(0).put(0);
		verts.put(0).put(0).put(0);
		verts.put(axisLen).put(0).put(0);
		verts.put(0).put(0).put(0);
		verts.put(0).put(-axisLen).put(0);
		verts.put(0).put(0).put(0);
		verts.put(0).put(axisLen).put(0);
		verts.put(0).put(0).put(0);
		verts.put(0).put(0).put(-axisLen);
		verts.put(0).put(0).put(0);
		verts.put(0).put(0).put(axisLen);

		final FloatBuffer colors = BufferUtils.createColorBuffer(12);
		colors.put(1).put(0).put(0).put(0);
		colors.put(1).put(0).put(0).put(0);
		colors.put(1).put(0).put(0).put(0);
		colors.put(1).put(0).put(0).put(0);
		colors.put(0).put(1).put(0).put(0);
		colors.put(0).put(1).put(0).put(0);
		colors.put(0).put(1).put(0).put(0);
		colors.put(0).put(1).put(0).put(0);
		colors.put(0).put(0).put(1).put(0);
		colors.put(0).put(0).put(1).put(0);
		colors.put(0).put(0).put(1).put(0);
		colors.put(0).put(0).put(1).put(0);

		final Line axis = new Line("Axis", verts, null, colors, null);
		axis.getSceneHints().setLightCombineMode(LightCombineMode.Off);

		return axis;
	}

	private void registerInputTriggers() {

		logicalLayer.registerTrigger(new InputTrigger(new MouseButtonPressedCondition(MouseButton.LEFT), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				taskManager.update(new Callable<Object>() {
					public Object call() throws Exception {
						MouseState mouseState = inputStates.getCurrent().getMouseState();
						if (operation == Operation.SELECT || operation == Operation.RESIZE) {
							if (drawn == null || drawn.isDrawCompleted()) {
								if (drawn != null)
									drawn.hidePoints();
								drawn = SelectUtil.selectHousePart(mouseState.getX(), mouseState.getY(), true);
								System.out.println("Clicked on: " + drawn);
								SelectUtil.nextPickLayer();
							}
						} else
							drawn.addPoint(mouseState.getX(), mouseState.getY());

						enableDisableRotationControl();

						return null;
					}
				});

			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new MouseButtonReleasedCondition(MouseButton.LEFT), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {

				taskManager.update(new Callable<Object>() {
					public Object call() throws Exception {
						MouseState mouseState = inputStates.getCurrent().getMouseState();
						if (operation == Operation.SELECT || operation == Operation.RESIZE) {
							if (drawn != null && !drawn.isDrawCompleted())
								drawn.complete();
						} else {
							if (!drawn.isDrawCompleted())
								drawn.addPoint(mouseState.getX(), mouseState.getY());

							if (drawn.isDrawCompleted()) {
								drawn.hidePoints();
								drawn = null;
								if (operationStick)
									operationFlag = true;
								else {
									MainFrame.getInstance().deselect();
								}
							}
						}

						enableDisableRotationControl();
						updateHeliodonSize();

						return null;
					}
				});

			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new MouseMovedCondition(), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				// mouseMoveFlag = true;
				moveState = inputStates;
			}
		}));

		final KeyHeldCondition cond1 = new KeyHeldCondition(Key.LCONTROL);
		final MouseMovedCondition cond2 = new MouseMovedCondition();
		final Predicate<TwoInputStates> condition = Predicates.and(cond1, cond2);
		logicalLayer.registerTrigger(new InputTrigger(condition, new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				int dy = inputStates.getCurrent().getMouseState().getDy();
				if (dy < -4)
					dy = -4;
				if (dy > 4)
					dy = 4;
				zoom(canvas, tpf, dy / 5.0);
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
						Scene.getInstance().remove(drawn);
						drawn = null;
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
				moveUpDown(source, tpf, true);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.Z), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				moveUpDown(source, tpf, false);
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
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.R), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				toggleRotation();
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.ZERO), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				resetCamera(viewMode);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new MouseWheelMovedCondition(), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				zoom(source, tpf, inputStates.getCurrent().getMouseState().getDwheel());
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.X), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				heliodon.setHourAngle(heliodon.getHourAngle() + 0.03);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.Z), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				heliodon.setHourAngle(heliodon.getHourAngle() - 0.03);
			}
		}));		
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.UP), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (!sunControl)
					return;
//				sunAngle--;
//				updateSunHeliodon();
//				heliodon.setSunAngle(heliodon.getSunAngle() - 1);
				heliodon.setObserverLatitude(heliodon.getObserverLatitude() + 0.01);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.DOWN), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (!sunControl)
					return;
//				sunAngle++;
//				updateSunHeliodon();
//				heliodon.setSunAngle(heliodon.getSunAngle() + 1);
				heliodon.setObserverLatitude(heliodon.getObserverLatitude() - 0.01);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.RIGHT), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (!sunControl)
					return;
//				sunBaseAngle++;
//				updateSunHeliodon();
//				heliodon.setBaseAngle(heliodon.getBaseAngle() + 1);
//				heliodon.setOffset(heliodon.getOffset() + 0.01);
//				heliodon.computeSunLocation(1, 0.5, 0.5);
//				heliodon.setHourAngle(heliodon.getHourAngle() + 0.1);
				heliodon.setDeclinationAngle(heliodon.getDeclinationAngle() + 0.01);
//				heliodon.setObserverLatitude(heliodon.getObserverLatitude() + 0.01);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.LEFT), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (!sunControl)
					return;
//				sunBaseAngle--;
//				updateSunHeliodon();
//				heliodon.setBaseAngle(heliodon.getBaseAngle() - 1);
//				heliodon.setOffset(heliodon.getOffset() - 0.01);
//				heliodon.computeSunLocation(-1, 0.5, 0.5);
//				heliodon.setHourAngle(heliodon.getHourAngle() - 0.1);
				heliodon.setDeclinationAngle(heliodon.getDeclinationAngle() - 0.01);
//				heliodon.setObserverLatitude(heliodon.getObserverLatitude() - 0.01);
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
	}

	public void setCameraControl(CameraMode type) {
		// this.cameraMode = type;
		if (control != null)
			control.removeTriggers(logicalLayer);

		if (type == CameraMode.ORBIT)
			control = new OrbitControl(Vector3.UNIT_Z);
		else if (type == CameraMode.FIRST_PERSON)
			control = new FirstPersonControl(Vector3.UNIT_Z);
		control.setupKeyboardTriggers(logicalLayer);
		control.setupMouseTriggers(logicalLayer, true);
		control.setMoveSpeed(MOVE_SPEED);
		control.setKeyRotateSpeed(1);
	}

	private void hideAllEditPoints() {
		for (HousePart part : Scene.getInstance().getParts())
			part.hidePoints();
	}

	public void resetCamera(final ViewMode viewMode) {
		this.viewMode = viewMode;
		final Camera camera = canvas.getCanvasRenderer().getCamera();

		// setCameraControl(cameraMode);
		control.setMouseButtonActions(ButtonAction.ROTATE, ButtonAction.MOVE);
		control.setMoveSpeed(MOVE_SPEED);
		Vector3 loc = new Vector3(1.0f, -8.0f, 1.0f);
		Vector3 left = new Vector3(-1.0f, 0.0f, 0.0f);
		Vector3 up = new Vector3(0.0f, 0.0f, 1.0f);
		Vector3 dir = new Vector3(0.0f, 1.0f, 0.0f);

		setCompassVisible(viewMode == ViewMode.NORMAL);

		if (viewMode == ViewMode.NORMAL) {
			camera.setProjectionMode(ProjectionMode.Perspective);
			resizeCamera();
		} else if (viewMode == ViewMode.TOP_VIEW) {
			camera.setProjectionMode(ProjectionMode.Parallel);
			control.setMouseButtonActions(ButtonAction.MOVE, ButtonAction.NONE);
			control.setMoveSpeed(5 * MOVE_SPEED);
			loc = new Vector3(0, 0, 10);
			up = new Vector3(0.0f, -1.0f, 0.0f);
			dir = new Vector3(0.0f, 0.0f, -1.0f);
			resizeCamera(2);
		} else if (viewMode == ViewMode.PRINT) {
			control.setMouseButtonActions(ButtonAction.MOVE, ButtonAction.MOVE);
			camera.setProjectionMode(ProjectionMode.Parallel);
			loc = new Vector3(0, -1, 0);
			final double pageWidth = PrintController.getInstance().getPageWidth();
			final double pageHeight = PrintController.getInstance().getPageHeight();
			final double ratio = (double) camera.getWidth() / camera.getHeight();
			if (ratio > pageWidth / pageHeight)
				resizeCamera(pageHeight * ratio);
			else
				resizeCamera(pageWidth);
		} else if (viewMode == ViewMode.PRINT_PREVIEW) {
			control.setMouseButtonActions(ButtonAction.MOVE, ButtonAction.MOVE);
			camera.setProjectionMode(ProjectionMode.Perspective);
			final int rows = PrintController.getInstance().getRows();
			final double pageHeight = PrintController.getInstance().getPageHeight() + PrintController.getMargin();
			final double w = PrintController.getInstance().getCols() * (PrintController.getInstance().getPageWidth() + PrintController.getMargin());
			final double h = rows * pageHeight;
			loc = new Vector3(0, -Math.max(w, h), rows % 2 != 0 ? 0 : pageHeight / 2);
			resizeCamera(PrintController.getInstance().getPageWidth());
		}

		camera.setFrame(loc, left, up, dir);
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

		if (camera.getProjectionMode() == ProjectionMode.Parallel)
			camera.setFrustum(0.01, 200, -orthoWidth / 2, orthoWidth / 2, -orthoWidth / ratio / 2, orthoWidth / ratio / 2);
		else
			camera.setFrustumPerspective(45.0, ratio, 0.01, 200);
	}

	public void toggleRotation() {
		rotAnim = !rotAnim;
	}

	private void zoom(final Canvas canvas, final double tpf, double val) {
		if (Camera.getCurrentCamera().getProjectionMode() == ProjectionMode.Parallel) {
			final double fac = val > 0 ? 1.1 : 0.9;
			final Camera camera = canvas.getCanvasRenderer().getCamera();
			camera.setFrustumTop(camera.getFrustumTop() * fac);
			camera.setFrustumBottom(camera.getFrustumBottom() * fac);
			camera.setFrustumLeft(camera.getFrustumLeft() * fac);
			camera.setFrustumRight(camera.getFrustumRight() * fac);
			camera.update();
			control.setMoveSpeed(2 * camera.getFrustumTop() * camera.getFrustumTop());
		} else {
			final Camera camera = canvas.getCanvasRenderer().getCamera();
			final Vector3 loc = new Vector3(camera.getDirection()).multiplyLocal(-val * MOVE_SPEED * 10 * tpf).addLocal(camera.getLocation());
			camera.setLocation(loc);

			if (control instanceof OrbitControl)
				((OrbitControl) control).computeNewFrontDistance();
		}
		getCameraNode().updateFromCamera();
	}

	private void moveUpDown(final Canvas canvas, final double tpf, boolean up) {
		final Camera camera = canvas.getCanvasRenderer().getCamera();
		final Vector3 loc = new Vector3(camera.getUp());
		if (viewMode == ViewMode.TOP_VIEW)
			up = !up;
		loc.multiplyLocal((up ? 1 : -1) * MOVE_SPEED * tpf).addLocal(camera.getLocation());
		camera.setLocation(loc);
	}

	public void setOperation(Operation operation) {
		this.operationStick = false;
		this.operation = operation;
		this.operationFlag = true;
	}

	public void setOperationStick(boolean stick) {
		this.operationStick = stick;
	}

	public void executeOperation() {
		System.out.println("executeOperation()");
		this.operationFlag = false;
		if (drawn != null && !drawn.isDrawCompleted())
			Scene.getInstance().remove(drawn);
		for (HousePart part : Scene.getInstance().getParts())
			if (part instanceof Foundation) {
				((Foundation) part).setResizeHouseMode(operation == Operation.RESIZE);
			}
		if (viewMode != ViewMode.PRINT_PREVIEW)
			Scene.getInstance().drawResizeBounds();
		drawn = newHousePart();
		enableDisableRotationControl();
	}

	private HousePart newHousePart() {
		HousePart drawn = null;
		if (operation == Operation.DRAW_WALL)
			drawn = new Wall();
		else if (operation == Operation.DRAW_DOOR)
			drawn = new Door();
		else if (operation == Operation.DRAW_WINDOW)
			drawn = new Window();
		else if (operation == Operation.DRAW_ROOF)
			drawn = new PyramidRoof();
		else if (operation == Operation.DRAW_ROOF_HIP)
			drawn = new HipRoof();
		else if (operation == Operation.DRAW_FLOOR)
			drawn = new Floor();
		else if (operation == Operation.DRAW_FOUNDATION)
			drawn = new Foundation();

		if (drawn != null)
			Scene.getInstance().add(drawn);
		return drawn;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setLighting(final boolean enable) {
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

//		taskManager.update(new Callable<Object>() {
//			public Object call() throws Exception {
//				if (sunControl) {
//					updateHeliodonSize();
//					root.attachChild(sunHeliodon);
//				} else
//					root.detachChild(sunHeliodon);
//
//				if (bloomRenderPass != null)
//					passManager.remove(bloomRenderPass);
//				if (sunControl) {
//					bloomRenderPass = new BloomRenderPass(canvas.getCanvasRenderer().getCamera(), 4);
//					if (!bloomRenderPass.isSupported()) {
//						System.out.println("Bloom not supported!");
//					} else {
//						bloomRenderPass.add(sun);
//					}
//					passManager.add(bloomRenderPass);
//				}
//
//				enableDisableRotationControl();
//				return null;
//			}
//		});
	}

	public void setSunAnim(boolean selected) {
		this.sunAnim = selected;
	}

	public void enableDisableRotationControl() {
		if ((operation == Operation.SELECT || operation == Operation.RESIZE) && (drawn == null || drawn.isDrawCompleted())) // && viewMode != ViewMode.TOP_VIEW) // && viewMode != ViewMode.PRINT_PREVIEW)
			control.setMouseEnabled(true);
		else
			control.setMouseEnabled(false);

		if (sunControl)
			control.setKeyRotateSpeed(0);
		else
			control.setKeyRotateSpeed(1);
	}

	public HousePart getSelectedPart() {
		return drawn;
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
		// if (printPreview) {
		// resetCamera(ViewMode.PRINT_PREVIEW);
		// root.detachChild(floor);
		// root.detachChild(axis);
		// root.detachChild(sky);
		// } else {
		// resetCamera(ViewMode.NORMAL);
		// root.attachChild(floor);
		// root.attachChild(axis);
		// root.attachChild(sky);
		// }

		resetCamera(printPreview ? ViewMode.PRINT_PREVIEW : ViewMode.NORMAL);
		backgroundRoot.getSceneHints().setCullHint(printPreview ? CullHint.Always : CullHint.Inherit);
	}

	public void setShadow(final boolean shadow) {
		taskManager.update(new Callable<Object>() {
			public Object call() throws Exception {
				if (shadow)
					passManager.add(shadowPass);
				else
					passManager.remove(shadowPass);
				return null;
			}
		});		
	}

	public CameraNode getCameraNode() {
		return cameraNode;
	}

	private Node loadCompassModel() throws IOException {
		System.out.print("Loading compass...");
		final ResourceSource source = ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_MODEL, "compass.dae");
		final ColladaImporter colladaImporter = new ColladaImporter();

		// Load the collada scene
		final ColladaStorage storage = colladaImporter.load(source);
		final Node compass = storage.getScene();
		BMText txt;

		final double Z = 0.2;
		txt = new BMText("N", "N", FontManager.getInstance().getAnnotationFont());
		txt.setAutoRotate(false);
		txt.setTranslation(3, -0.3, Z);
		txt.setRotation(new Matrix3().fromAngleAxis(-Math.PI / 2, Vector3.UNIT_Y).multiplyLocal(new Matrix3().fromAngleAxis(Math.PI / 2, Vector3.UNIT_Z)));
		compass.attachChild(txt);

		txt = new BMText("S", "S", FontManager.getInstance().getAnnotationFont());
		txt.setAutoRotate(false);
		txt.setTranslation(-2, -0.2, Z);
		txt.setRotation(new Matrix3().fromAngleAxis(-Math.PI / 2, Vector3.UNIT_Y).multiplyLocal(new Matrix3().fromAngleAxis(Math.PI / 2, Vector3.UNIT_Z)));
		compass.attachChild(txt);

		txt = new BMText("E", "E", FontManager.getInstance().getAnnotationFont());
		txt.setAutoRotate(false);
		txt.setTranslation(-0.2, 2.1, Z);
		txt.setRotation(new Matrix3().fromAngleAxis(-Math.PI / 2, Vector3.UNIT_X));
		compass.attachChild(txt);

		txt = new BMText("W", "W", FontManager.getInstance().getAnnotationFont());
		txt.setAutoRotate(false);
		txt.setTranslation(-0.4, -2, Z);
		txt.setRotation(new Matrix3().fromAngleAxis(Math.PI / 2, Vector3.UNIT_X));
		compass.attachChild(txt);

		final DirectionalLight light = new DirectionalLight();
		light.setDirection(new Vector3(0, 0, -1));
		// light.setAmbient(new ColorRGBA(1, 1, 1, 1));
		light.setEnabled(true);

		final LightState lightState = new LightState();
		lightState.attach(light);
		compass.setRenderState(lightState);

		compass.updateWorldRenderStates(true);

		compass.addController(new SpatialController<Spatial>() {
			public void update(double time, Spatial caller) {
				final Vector3 direction = Camera.getCurrentCamera().getDirection().normalize(null);
				direction.setZ(0);
				direction.normalizeLocal();
				double angle = -direction.smallestAngleBetween(Vector3.UNIT_Y);
				if (direction.dot(Vector3.UNIT_X) > 0)
					angle = -angle;
				angle -= Math.PI / 2;
				compass.setRotation(new Matrix3().fromAngleAxis(angle, Vector3.UNIT_Z));
			}
		});

		final Node compassNode1 = new Node();
		compassNode1.setRotation(new Matrix3().fromAngleAxis(-Math.PI / 2, Vector3.UNIT_X));
		compassNode1.attachChild(compass);
		final Node compassNode2 = new Node();
		compassNode2.attachChild(compassNode1);
		System.out.println("done");
		return compassNode2;
	}

	public void setCompassVisible(boolean visible) {
		cameraNode.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
	}

	public void updateHeliodonSize() {
		if (sunControl)
			taskManager.update(new Callable<Object>() {
				public Object call() throws Exception {
//					Scene.getRoot().updateWorldBound(true);
//					final BoundingVolume bounds = Scene.getRoot().getWorldBound();
//					if (bounds == null)
//						sunHeliodon.setScale(1);
//					else {
//						final double scale = (Util.findBoundLength(bounds) / 2.0 + bounds.getCenter().length()) / 5.0;
//						System.out.println("scale = " + scale);
//						sunHeliodon.setScale(scale);
//					}
					heliodon.updateSize();
					return null;
				}
			});
	}

	public void executeMouseMove() {
		// this.mouseMoveFlag = false;
		final MouseState mouseState = moveState.getCurrent().getMouseState();
		moveState = null;
		int x = mouseState.getX();
		int y = mouseState.getY();

		if (drawn != null && !drawn.isDrawCompleted()) {
			drawn.setPreviewPoint(x, y);
		} else if (operation == Operation.SELECT && mouseState.getButtonState(MouseButton.LEFT) == ButtonState.UP && mouseState.getButtonState(MouseButton.MIDDLE) == ButtonState.UP && mouseState.getButtonState(MouseButton.RIGHT) == ButtonState.UP) {
			drawn = SelectUtil.selectHousePart(x, y, false);
		}
	}

	public ViewMode getViewMode() {
		return viewMode;
	}

	public boolean isRotationAnimationOn() {
		return rotAnim;
	}

}
