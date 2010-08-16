package org.concord.energy3d.scene;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.concurrent.Callable;

import org.concord.energy3d.MainFrame;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HipRoof;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.PyramidRoof;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.CameraControl.ButtonAction;
import org.concord.energy3d.util.Blinker;
import org.concord.energy3d.util.SelectUtil;
import org.lwjgl.LWJGLException;

import com.ardor3d.annotation.MainThread;
import com.ardor3d.extension.effect.bloom.BloomRenderPass;
import com.ardor3d.extension.shadow.map.ParallelSplitShadowMapPass;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.FrameHandler;
import com.ardor3d.framework.Updater;
import com.ardor3d.framework.jogl.JoglAwtCanvas;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.framework.lwjgl.LwjglAwtCanvas;
import com.ardor3d.framework.lwjgl.LwjglCanvasRenderer;
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
import com.ardor3d.renderer.lwjgl.LwjglTextureRendererProvider;
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
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.Cylinder;
import com.ardor3d.scenegraph.shape.Dome;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.scenegraph.shape.Sphere;
import com.ardor3d.util.ContextGarbageCollector;
import com.ardor3d.util.GameTaskQueue;
import com.ardor3d.util.GameTaskQueueManager;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.Timer;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;

public class SceneManager implements com.ardor3d.framework.Scene, Runnable, Updater {
	public enum Operation {
		SELECT, RESIZE, DRAW_WALL, DRAW_DOOR, DRAW_ROOF, DRAW_ROOF_HIP, DRAW_WINDOW, DRAW_FOUNDATION, DRAW_FLOOR
	}

	public enum CameraMode {
		ORBIT, FIRST_PERSON
	}

	public enum ViewMode {
		NORMAL, TOP_VIEW, PRINT_PREVIEW
	}

	private static final double MOVE_SPEED = 5;

	private static SceneManager instance = null;
	// private final JoglAwtCanvas canvas;
	private final Canvas canvas;
	private final FrameHandler frameHandler;
	private final LogicalLayer logicalLayer;
	private boolean exit = false;
	protected final Node root = new Node("Root");
	// private final Node housePartsNode = Scene.root;
	private Mesh floor;
	private boolean rotAnim = false;
	private HousePart drawn = null;
	private Operation operation = Operation.SELECT;
	private LightState lightState;
	private double angle, sunAngle = 90, sunBaseAngle = 0;
	private Matrix3 rotate = new Matrix3();
	private CameraControl control;
	private BasicPassManager passManager = new BasicPassManager();
	private ParallelSplitShadowMapPass shadowPass;
	private Sphere sun;
	private Node sunHeliodon;
	private Node sunRot;
	private boolean sunControl;
	private boolean sunAnim;
	private BloomRenderPass bloomRenderPass;
	private Line axis;
	// private boolean dirtyRenderer;
	private Dome sky;
	private ViewMode viewMode = ViewMode.NORMAL;
	private final GameTaskQueueManager taskManager = GameTaskQueueManager.getManager("Task Manager");
	private CameraMode cameraMode = CameraMode.ORBIT;

	private boolean operationFlag = false;
	private static final boolean JOGL = false;

	public static SceneManager getInstance() {
		return instance;
	}

	public SceneManager(final Container panel) throws LWJGLException {
		System.out.print("Initializing scene manager...");
		instance = this;
		root.attachChild(Scene.getRoot());

		final DisplaySettings settings = new DisplaySettings(800, 600, 32, 60, 0, 8, 0, 0, false, false);
		if (JOGL)
			canvas = new JoglAwtCanvas(settings, new JoglCanvasRenderer(this));
		else
			canvas = new LwjglAwtCanvas(settings, new LwjglCanvasRenderer(this));

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
		else
			TextureRendererFactory.INSTANCE.setProvider(new LwjglTextureRendererProvider());

		panel.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent e) {
				final Dimension size = panel.getSize();
				if ((size.width == 0) && (size.height == 0)) {
					return;
				}
				final Camera camera = canvas.getCanvasRenderer().getCamera();
				if (camera != null) {
					resizeCamera(camera);
				}
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
			ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, new SimpleResourceLocator(SceneManager.class.getClassLoader().getResource("org/concord/energy3d/images/")));
			ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, new SimpleResourceLocator(SceneManager.class.getClassLoader().getResource("org/concord/energy3d/fonts/")));
		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		// enable depth test
		final ZBufferState buf = new ZBufferState();
		buf.setEnabled(true);
		buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
		root.setRenderState(buf);

		final DirectionalLight light = new DirectionalLight();
		light.setDirection(new Vector3(0, 0, -1));
		light.setAmbient(new ColorRGBA(1, 1, 1, 1));
		light.setEnabled(true);

		lightState = new LightState();
		lightState.setEnabled(false);
		lightState.attach(light);
		root.setRenderState(lightState);

		root.attachChild(createAxis());
		root.attachChild(createFloor());
		root.attachChild(createSky());

		final RenderPass rootPass = new RenderPass();
		rootPass.add(root);
		passManager.add(rootPass);

		// shadowPass = new ParallelSplitShadowMapPass(light, 512, 3);
		shadowPass = new ParallelSplitShadowMapPass(light, 3072, 3);
		shadowPass.setUseObjectCullFace(true);
		shadowPass.add(floor);
		shadowPass.add(Scene.getRoot());
		shadowPass.addOccluder(Scene.getRoot());

		createSunHeliodon();
		Scene.getInstance();

		SelectUtil.init(floor, Scene.getRoot());
		registerInputTriggers();

		frameHandler.updateFrame();
		resetCamera(ViewMode.NORMAL);
		canvas.getCanvasRenderer().getCamera().setFrustumPerspective(45.0, 16 / 10.0, 1, 1000);

		root.updateGeometricState(0, true);
		System.out.println("done");
	}

	public void run() {
		try {
			frameHandler.init();
			while (!exit) {
				frameHandler.updateFrame();
				Thread.yield();
			}
		} catch (final Throwable t) {
			t.printStackTrace();
		}
	}
	
	public void update(final ReadOnlyTimer timer) {
		if (operationFlag)
			executeOperation();
		
		// Scene.getInstance().updateTexture();
		final double tpf = timer.getTimePerFrame();
		passManager.updatePasses(tpf);
		logicalLayer.checkTriggers(tpf);
	
		taskManager.getQueue(GameTaskQueue.UPDATE).execute(canvas.getCanvasRenderer().getRenderer());
		Scene.getInstance().update();
	
		int val = 1;
		if (rotAnim) {
			angle = val;
			rotate.fromAngleNormalAxis(angle * MathUtils.DEG_TO_RAD, Vector3.UNIT_Z);
			final Camera camera = canvas.getCanvasRenderer().getCamera();
			camera.setLocation(rotate.applyPre(camera.getLocation(), null));
			camera.lookAt(0, 0, val, Vector3.UNIT_Z);
			root.setRotation(rotate);
		}
	
		if (sunAnim) {
			sunAngle++;
			updateSunHeliodon();
		}
	
		root.updateGeometricState(tpf);
	}

	public boolean renderUnto(Renderer renderer) {
		// if (!Scene.getInstance().getParts().isEmpty())
		// Scene.getInstance().renderTexture(renderer);
		// Scene.getInstance().init();
	
		// renderer.draw(root);
		// if (drawn != null)
		// com.ardor3d.util.geom.Debugger.drawBounds(drawn.getRoot(), renderer, true);
	
		passManager.renderPasses(renderer);
		return true;
	}

	private Spatial createSunHeliodon() {
		sunHeliodon = new Node();
		Cylinder cyl = new Cylinder("Sun Curve", 10, 50, 5, 0.3);
		Transform trans = new Transform();
		trans.setMatrix(new Matrix3().fromAngleAxis(Math.PI / 2, Vector3.UNIT_X));
		// trans.setTranslation(0, -1, 0);
		cyl.setDefaultColor(ColorRGBA.YELLOW);
		cyl.setTransform(trans);
		sunHeliodon.attachChild(cyl);

		final ClipState cs = new ClipState();
		cs.setEnableClipPlane(0, true);
		cs.setClipPlaneEquation(0, 0, 0, 1, -0.19);
		cyl.setRenderState(cs);

		Cylinder baseCyl = new Cylinder("Sun Curve", 10, 50, 5, 0.2);
		baseCyl.setTranslation(0, 0, 0.1);
		sunHeliodon.attachChild(baseCyl);

		sun = new Sphere("Sun", 20, 20, 0.3);
		sun.setTranslation(0, 0, 5);
		sunRot = new Node("Sun Root");
		sunRot.attachChild(sun);
		sunHeliodon.attachChild(sunRot);

		reverseNormals(sun.getMeshData().getNormalBuffer());

		MaterialState material = new MaterialState();
		material.setEmissive(ColorRGBA.WHITE);
		sun.setRenderState(material);

		return sunHeliodon;
	}

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
		sunRot.setRotation(new Matrix3().fromAngleAxis((-90 + sunAngle) * Math.PI / 180, Vector3.UNIT_Y));
		DirectionalLight light = (DirectionalLight) lightState.get(0);
		light.setDirection(sun.getWorldTranslation().negate(null));

		sunBaseAngle = sunBaseAngle % 360;
		sunHeliodon.setRotation(new Matrix3().fromAngleAxis(sunBaseAngle * Math.PI / 180, Vector3.UNIT_Z));
		sunHeliodon.updateGeometricState(0);
	}

	public PickResults doPick(Ray3 pickRay) {
		return null;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	private Mesh createFloor() {
		floor = new Quad("Floor", 200, 200);
		floor.setDefaultColor(new ColorRGBA(0, 1, 0, 0.5f));

		BlendState blendState = new BlendState();
		blendState.setBlendEnabled(true);
		blendState.setTestEnabled(true);
		floor.setRenderState(blendState);
		floor.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
		floor.getSceneHints().setLightCombineMode(LightCombineMode.Off);

		// Add a material to the box, to show both vertex color and lighting/shading.
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		floor.setRenderState(ms);

		floor.updateModelBound();

		return floor;
	}

	private Mesh createSky() {
		sky = new Dome("Sky", 100, 100, 100);
		// sky.setRotation(new Quaternion(1, 0, 0, 1));
		sky.setRotation(new Matrix3().fromAngles(Math.PI / 2, 0, 0));
		// Sphere sky = new Sphere("Sky", 100, 100, 100);
		// sky.setTextureMode(TextureMode.Polar);
		// sky.setTranslation(0, 0, 10);

		// reverseNormals(sky.getMeshData().getNormalBuffer());

		final TextureState ts = new TextureState();
		ts.setTexture(TextureManager.load("sky6.jpg", Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
		sky.setRenderState(ts);

		// final MaterialState ms = new MaterialState();
		// ms.setColorMaterial(ColorMaterial.Diffuse);
		// sky.setRenderState(ms);

		sky.getSceneHints().setLightCombineMode(LightCombineMode.Off);

		return sky;
	}

	private Spatial createAxis() {
		final int axisLen = 100;

		FloatBuffer verts = BufferUtils.createVector3Buffer(12);
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

		FloatBuffer colors = BufferUtils.createColorBuffer(12);
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

		axis = new Line("Axis", verts, null, colors, null);
		axis.getSceneHints().setLightCombineMode(LightCombineMode.Off);

		return axis;
	}

	private void registerInputTriggers() {
		// control = new FirstPersonControl(Vector3.UNIT_Z) {
		//
		// @Override
		// protected void rotate(Camera camera, double dx, double dy) {
		// if ((operation == Operation.SELECT || operation == Operation.RESIZE) && !sunControl && (drawn == null || drawn.isDrawCompleted()) && !topView)
		// super.rotate(camera, dx, dy);
		// }
		// };
		// control = new FirstPersonControl(Vector3.UNIT_Z);

		// setCameraControl(CameraMode.ORBIT);
		// control = new OrbitControl(Vector3.UNIT_Z);
		// control.setupKeyboardTriggers(logicalLayer);
		// control.setupMouseTriggers(logicalLayer, true);
		// control.setMoveSpeed(MOVE_SPEED);
		// control.setKeyRotateSpeed(1);

		logicalLayer.registerTrigger(new InputTrigger(new MouseButtonPressedCondition(MouseButton.LEFT), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				MouseState mouseState = inputStates.getCurrent().getMouseState();
				if (operation == Operation.SELECT || operation == Operation.RESIZE) {
					if (drawn == null || drawn.isDrawCompleted()) {
						if (drawn != null)
							drawn.hidePoints();
						drawn = SelectUtil.selectHousePart(mouseState.getX(), mouseState.getY(), true);
						System.out.println(drawn);
						// if (pickLayer != -1)
						// pickLayer = (pickLayer + 1) % Math.max(1, pickResults.getNumber() / 2);
						SelectUtil.nextPickLayer();
					}
				} else
					drawn.addPoint(mouseState.getX(), mouseState.getY());

				enableDisableRotationControl();
			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new MouseButtonReleasedCondition(MouseButton.LEFT), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				// if (operation == Operation.DRAW_ROOF)
				// return;
				MouseState mouseState = inputStates.getCurrent().getMouseState();
				if (operation == Operation.SELECT || operation == Operation.RESIZE) {
					if (drawn != null && !drawn.isDrawCompleted())
						drawn.complete();
					return;
				}

				int x = mouseState.getX();
				int y = mouseState.getY();
				if (!drawn.isDrawCompleted())
					drawn.addPoint(x, y);

				if (drawn.isDrawCompleted()) {
					drawn.hidePoints();
					// if (operation == Operation.DRAW_FLOOR || operation == Operation.DRAW_ROOF || operation == Operation.DRAW_ROOF_HIP) {
					MainFrame.getInstance().getSelectButton().setSelected(true);
					operation = Operation.SELECT;
					drawn = null;
					// } else {
					// drawn = newHousePart();
					// drawn.setPreviewPoint(x, y);
					// }
				}

				enableDisableRotationControl();
			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new MouseMovedCondition(), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				final MouseState mouseState = inputStates.getCurrent().getMouseState();
				int x = mouseState.getX();
				int y = mouseState.getY();
				if (drawn != null && !drawn.isDrawCompleted()) {
					drawn.setPreviewPoint(x, y);
				} else if (operation == Operation.SELECT && mouseState.getButtonState(MouseButton.LEFT) == ButtonState.UP && mouseState.getButtonState(MouseButton.MIDDLE) == ButtonState.UP && mouseState.getButtonState(MouseButton.RIGHT) == ButtonState.UP) {
					drawn = SelectUtil.selectHousePart(x, y, false);
				}
				// enableDisableRotationControl();
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.LCONTROL), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				SelectUtil.setPickLayer(0);
				// pickLayer = 0;
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyReleasedCondition(Key.LCONTROL), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				SelectUtil.setPickLayer(0);
				// pickLayer = -1;
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.DELETE), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				// removeHousePart(drawn);
				Scene.getInstance().remove(drawn);
				drawn = null;
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
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.UP), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (!sunControl)
					return;
				sunAngle--;
				updateSunHeliodon();
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.DOWN), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (!sunControl)
					return;
				sunAngle++;
				updateSunHeliodon();
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.RIGHT), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (!sunControl)
					return;
				sunBaseAngle++;
				updateSunHeliodon();
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.LEFT), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (!sunControl)
					return;
				sunBaseAngle--;
				updateSunHeliodon();
			}
		}));
	}

	public void setCameraControl(CameraMode type) {
		this.cameraMode = type;
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

	// private void lookAtZero(final Canvas source) {
	// source.getCanvasRenderer().getCamera().setLocation(0, -2, 0);
	// source.getCanvasRenderer().getCamera().lookAt(Vector3.ZERO, Vector3.UNIT_Z); // .negate(null));
	// }

	public void resetCamera() {
		resetCamera(this.viewMode);
	}

	public void resetCamera(final ViewMode viewMode) {
		this.viewMode = viewMode;
		final Camera camera = canvas.getCanvasRenderer().getCamera();

		// resizeCamera(camera);

		setCameraControl(cameraMode);
		control.setMouseButtonActions(ButtonAction.ROTATE, ButtonAction.MOVE);
		control.setMoveSpeed(MOVE_SPEED);
		Vector3 loc = new Vector3(1.0f, -8.0f, 1.0f);
		Vector3 left = new Vector3(-1.0f, 0.0f, 0.0f);
		Vector3 up = new Vector3(0.0f, 0.0f, 1.0f);
		Vector3 dir = new Vector3(0.0f, 1.0f, 0.0f);

		if (viewMode == ViewMode.TOP_VIEW) {
			camera.setProjectionMode(ProjectionMode.Parallel);
			control.setMouseButtonActions(ButtonAction.MOVE, ButtonAction.NONE);
			control.setMoveSpeed(MOVE_SPEED / 10);
			loc = new Vector3(0, 0, 50);
			up = new Vector3(0.0f, -1.0f, 0.0f);
			dir = new Vector3(0.0f, 0.0f, -1.0f);
			double fac = 10;
			camera.setFrustumTop(camera.getFrustumTop() * fac);
			camera.setFrustumBottom(camera.getFrustumBottom() * fac);
			camera.setFrustumLeft(camera.getFrustumLeft() * fac);
			camera.setFrustumRight(camera.getFrustumRight() * fac);
			camera.update();
		} else if (viewMode == ViewMode.PRINT_PREVIEW) {
			control.setMouseButtonActions(ButtonAction.MOVE, ButtonAction.MOVE);
			// control.setMoveSpeed(MOVE_SPEED / 10);
			// camera.setProjectionMode(ProjectionMode.Parallel);
			camera.setProjectionMode(ProjectionMode.Perspective);
			// loc = new Vector3(5, -20, 5);
			// loc = new Vector3(0, -20, 0);
			loc = new Vector3(0, -HousePart.PRINT_SPACE * HousePart.PRINT_COLS, 0);
			// root.setScale(0.04);
		} else {
			camera.setProjectionMode(ProjectionMode.Perspective);
			root.setScale(1);
		}

		camera.setFrame(loc, left, up, dir);
		if (viewMode != ViewMode.TOP_VIEW)
			resizeCamera(camera);

	}

	// public void topCameraView() {
	// topView = true;
	// control.setMouseButtonActions(ButtonAction.MOVE, ButtonAction.NONE);
	// control.setMoveSpeed(MOVE_SPEED * 10);
	// final Vector3 loc = new Vector3(0, 0, 50);
	// final Vector3 left = new Vector3(-1.0f, 0.0f, 0.0f);
	// final Vector3 up = new Vector3(0.0f, -1.0f, 0.0f);
	// final Vector3 dir = new Vector3(0.0f, 0.0f, -1.0f);
	//
	// Camera camera = canvas.getCanvasRenderer().getCamera();
	// camera.setFrame(loc, left, up, dir);
	// camera.setProjectionMode(ProjectionMode.Parallel);
	// resizeCamera(camera);
	// }

	private void resizeCamera(final Camera camera) {
		final Dimension size = ((Component) canvas).getSize();
		camera.resize(size.width, size.height);
		final double scale = 0.4; // 4; //topView ? 4 : 0.5;
		final double ratio = (double) camera.getWidth() / camera.getHeight();
		camera.setFrustumTop(scale);
		camera.setFrustumBottom(-scale);
		camera.setFrustumLeft(-scale * ratio);
		camera.setFrustumRight(scale * ratio);
		camera.update();
	}

	public void toggleRotation() {
		rotAnim = !rotAnim;
	}

	private void zoom(final Canvas canvas, final double tpf, int val) {
		if (viewMode == ViewMode.TOP_VIEW) {
			// System.out.println(val);
			// double scale = val > 0 ? val / 10.0 : 0.01 / (-val);
			final double fac = val > 0 ? 1.1 : 0.9;
			// final double scale2 = root.getScale().getX() * fac;
			// System.out.println(scale2);
			// root.setScale(scale2);
			final Camera camera = canvas.getCanvasRenderer().getCamera();
			camera.setFrustumTop(camera.getFrustumTop() * fac);
			camera.setFrustumBottom(camera.getFrustumBottom() * fac);
			camera.setFrustumLeft(camera.getFrustumLeft() * fac);
			camera.setFrustumRight(camera.getFrustumRight() * fac);
			camera.update();

			control.setMoveSpeed(1.1 * fac * control.getMoveSpeed());
		} else {
			final Camera camera = canvas.getCanvasRenderer().getCamera();
			final Vector3 loc = new Vector3(camera.getDirection()).multiplyLocal(-val * MOVE_SPEED * 10 * tpf).addLocal(camera.getLocation());
			// final Vector3 loc = new Vector3(camera.getLocation()).addLocal(dir);
			camera.setLocation(loc);

			if (control instanceof OrbitControl)
				((OrbitControl) control).computeNewFrontDistance();
		}
	}

	private void moveUpDown(final Canvas canvas, final double tpf, boolean up) {
		final Camera camera = canvas.getCanvasRenderer().getCamera();
		final Vector3 loc = new Vector3(camera.getUp());
		if (viewMode == ViewMode.TOP_VIEW)
			up = !up;
		loc.multiplyLocal((up ? 1 : -1) * MOVE_SPEED * tpf).addLocal(camera.getLocation());
		// final Vector3 loc = new Vector3(camera.getLocation());
		// loc.addLocal(dir);
		camera.setLocation(loc);
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
		this.operationFlag  = true;
	}
	
	public void executeOperation() { 
		this.operationFlag = false;
	// if (operation == Operation.SELECT) {
		// drawn = null;
		// return;
		// }
		if (drawn != null && !drawn.isDrawCompleted())
			// removeHousePart(drawn);
			Scene.getInstance().remove(drawn);
		// if (operation == Operation.RESIZE) {
		// Foundation.setResizeHouseMode(true);
		// } else
		// Foundation.setResizeHouseMode(false);
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
			// addHousePart(drawn);
			Scene.getInstance().add(drawn);
		return drawn;
	}

	// private void addHousePart(HousePart drawn) {
	// housePartsNode.attachChild(drawn.getRoot());
	// Scene.getInstance().add(drawn);
	// }
	//
	// private void removeHousePart(HousePart drawn) {
	// if (drawn == null)
	// return;
	// housePartsNode.detachChild(drawn.getRoot());
	// drawn.delete();
	// Scene.getInstance().remove(drawn);
	// }

	public Operation getOperation() {
		return operation;
	}

	public void setLighting(final boolean enable) {
		taskManager.update(new Callable<Object>() {
			public Object call() throws Exception {
				lightState.setEnabled(enable);
				root.updateWorldRenderStates(true);
				if (!enable)
//					passManager.add(shadowPass);
//				else
					passManager.remove(shadowPass);
				return null;
			}
		});

		// lightState.setEnabled(enable);
		// root.updateWorldRenderStates(true);
		// if (enable)
		// passManager.add(pssmPass);
		// else
		// passManager.remove(pssmPass);
	}

	public void setSunControl(boolean selected) {
		this.sunControl = selected;

		taskManager.update(new Callable<Object>() {
			public Object call() throws Exception {
				if (sunControl)
					root.attachChild(sunHeliodon);
				else
					root.detachChild(sunHeliodon);

				if (bloomRenderPass != null)
					passManager.remove(bloomRenderPass);
				if (sunControl) {
					bloomRenderPass = new BloomRenderPass(canvas.getCanvasRenderer().getCamera(), 4);
					// bloomRenderPass.setUseCurrentScene(true);
					if (!bloomRenderPass.isSupported()) {
						System.out.println("Bloom not supported!");
					} else {
						bloomRenderPass.add(sun);
					}
					passManager.add(bloomRenderPass);
				}

				enableDisableRotationControl();
				return null;
			}
		});

		// if (selected)
		// root.attachChild(sunHeliodon);
		// else
		// root.detachChild(sunHeliodon);
		//
		// if (bloomRenderPass != null)
		// passManager.remove(bloomRenderPass);
		// if (selected) {
		// bloomRenderPass = new BloomRenderPass(canvas.getCanvasRenderer().getCamera(), 4);
		// // bloomRenderPass.setUseCurrentScene(true);
		// if (!bloomRenderPass.isSupported()) {
		// System.out.println("Bloom not supported!");
		// } else {
		// bloomRenderPass.add(sun);
		// }
		// passManager.add(bloomRenderPass);
		// }
		//
		// enableDisableRotationControl();
	}

	public void setSunAnim(boolean selected) {
		this.sunAnim = selected;
	}

	public void enableDisableRotationControl() {
		if ((operation == Operation.SELECT || operation == Operation.RESIZE) && (drawn == null || drawn.isDrawCompleted())) // && viewMode != ViewMode.TOP_VIEW) // && viewMode != ViewMode.PRINT_PREVIEW)
			// control.setMouseRotateSpeed(0.005);
			control.setMouseEnabled(true);
		else
			// control.setMouseRotateSpeed(0.000000001);
			control.setMouseEnabled(false);

		if (sunControl)
			control.setKeyRotateSpeed(0);
		else
			control.setKeyRotateSpeed(1);
	}

	// public void print() {
	// // PrintExporter printExporter = new PrintExporter(PrintPreviewController.getInstance().getPrintParts().size());
	// PrintExporter printExporter = new PrintExporter();
	// // double scale = 0.2;
	// // root.setScale(scale);
	// Camera camera = Camera.getCurrentCamera();
	// // Vector3 location = new Vector3(camera.getLocation());
	// // Vector3 direction = new Vector3(camera.getDirection());
	// // ReadOnlyVector3 up = camera.getUp();
	// // for (HousePart part : Scene.getInstance().getPrintParts()) {
	// // for (HousePart part : PrintPreviewController.getInstance().getPrintParts()) {
	// for (Vector3 pos: PrintPreviewController.getInstance().printCenters) {
	// // if (printExporter.getCurrentPage() < Scene.getInstance().getPrintParts().size()) {
	// // HousePart part = Scene.getInstance().getPrintParts().get(printExporter.getCurrentPage());
	// // Vector3 pos = new Vector3(part.getPrintSequence() * scale, -5, part.getPrintY() * scale);
	// // Vector3 pos = new Vector3(part.getPrintSequence() % HousePart.PRINT_COLS * HousePart.PRINT_SPACE * scale, -5, part.getPrintSequence() / HousePart.PRINT_COLS * HousePart.PRINT_SPACE * scale);
	// // Vector3 pos = part.getPrintCenter();
	// System.out.println(pos);
	// camera.setLocation(pos.getX(), pos.getY() - 5, pos.getZ());
	// camera.lookAt(pos.add(0, 1, 0, null), Vector3.UNIT_Z);
	// try {
	// Thread.sleep(100);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// final JoglCanvasRenderer canvasRenderer = canvas.getCanvasRenderer();
	// canvasRenderer.setCurrentContext();
	// ScreenExporter.exportCurrentScreen(canvasRenderer.getRenderer(), printExporter);
	// canvasRenderer.releaseCurrentContext();
	// }
	// PrinterJob job = PrinterJob.getPrinterJob();
	// job.setPrintable(printExporter);
	// if (job.printDialog())
	// try {
	// job.print();
	// } catch (PrinterException exc) {
	// System.out.println(exc);
	// }
	// // camera.setLocation(location);
	// // camera.lookAt(location.addLocal(direction), up);
	// resetCamera(viewMode);
	// }

	// This class is used to hold an image while on the clipboard.
	public static class ImageSelection implements Transferable {
		private Image image;

		public ImageSelection(Image image) {
			this.image = image;
		}

		// Returns supported flavors
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { DataFlavor.imageFlavor };
		}

		// Returns true if flavor is supported
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.imageFlavor.equals(flavor);
		}

		// Returns image
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (!DataFlavor.imageFlavor.equals(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			}
			return image;
		}
	}

	// public void setPrintPreview(final boolean printPreview) {
	// final JoglCanvasRenderer canvasRenderer = canvas.getCanvasRenderer();
	// // new Thread() {
	// // public void run() {
	// if (printPreview) {
	// resetCamera(ViewMode.PRINT_PREVIEW);
	// root.detachChild(floor);
	// root.detachChild(axis);
	// root.detachChild(sky);
	// canvasRenderer.setCurrentContext();
	// canvasRenderer.getRenderer().setBackgroundColor(ColorRGBA.WHITE);
	// canvasRenderer.releaseCurrentContext();
	// Scene.getInstance().flatten(printPreview);
	// } else {
	// Scene.getInstance().flatten(printPreview);
	// resetCamera(ViewMode.NORMAL);
	// root.attachChild(floor);
	// root.attachChild(axis);
	// root.attachChild(sky);
	// canvasRenderer.setCurrentContext();
	// canvasRenderer.getRenderer().setBackgroundColor(ColorRGBA.BLACK);
	// canvasRenderer.releaseCurrentContext();
	// }
	// // }
	// // }.start();
	// }

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
		// final JoglCanvasRenderer canvasRenderer = canvas.getCanvasRenderer();
		if (printPreview) {
			resetCamera(ViewMode.PRINT_PREVIEW);
			root.detachChild(floor);
			root.detachChild(axis);
			root.detachChild(sky);
			// canvasRenderer.setCurrentContext();
			// canvasRenderer.getRenderer().setBackgroundColor(ColorRGBA.WHITE);
			// canvasRenderer.releaseCurrentContext();
			// Scene.getInstance().flatten(printPreview);
		} else {
			// Scene.getInstance().flatten(printPreview);
			resetCamera(ViewMode.NORMAL);
			root.attachChild(floor);
			root.attachChild(axis);
			root.attachChild(sky);
			// canvasRenderer.setCurrentContext();
			// canvasRenderer.getRenderer().setBackgroundColor(ColorRGBA.BLACK);
			// canvasRenderer.releaseCurrentContext();
		}
	}

	public void setShadow(boolean shadow) {
		if (shadow)
			passManager.add(shadowPass);
		else
			passManager.remove(shadowPass);		
	}
}
