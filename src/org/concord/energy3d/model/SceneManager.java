package org.concord.energy3d.model;

import java.awt.Container;
import java.awt.Dimension;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;

import com.ardor3d.annotation.MainThread;
import com.ardor3d.example.ExampleBase;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.FrameHandler;
import com.ardor3d.framework.Updater;
import com.ardor3d.framework.jogl.JoglAwtCanvas;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.input.ButtonState;
import com.ardor3d.input.InputState;
import com.ardor3d.input.Key;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.MouseState;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.awt.AwtFocusWrapper;
import com.ardor3d.input.awt.AwtKeyboardWrapper;
import com.ardor3d.input.awt.AwtMouseManager;
import com.ardor3d.input.awt.AwtMouseWrapper;
import com.ardor3d.input.logical.AnyKeyCondition;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyHeldCondition;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.KeyReleasedCondition;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.input.logical.MouseButtonClickedCondition;
import com.ardor3d.input.logical.MouseButtonCondition;
import com.ardor3d.input.logical.MouseMovedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TriggerConditions;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.intersection.IntersectionRecord;
import com.ardor3d.intersection.PickData;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.Camera.ProjectionMode;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.util.ContextGarbageCollector;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.Timer;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class SceneManager implements com.ardor3d.framework.Scene, Runnable, Updater {
	public static final int SELECT = 0;
	public static final int DRAW_LINES = 1;
	public static final int DRAW_RECTANGLE = 2;

	private static SceneManager instance = null;
	private final Container panel;
	private final JoglAwtCanvas canvas;
	private final JoglCanvasRenderer renderer;
	private final FrameHandler frameHandler;
	private final LogicalLayer logicalLayer;
	private boolean _exit = false;
	protected final Node root = new Node("Root");
	private final Node housePartsNode = new Node("House Parts");

	// private final static float CUBE_ROTATE_SPEED = 1;
	// private final Vector3 rotationAxis = new Vector3(1, 1, 0);
	// private double angle = 0;
	private Mesh floor;
	// private final Matrix3 rotation = new Matrix3();

	private static final int MOVE_SPEED = 4;
	private static final double TURN_SPEED = 0.5;
	private final Matrix3 _incr = new Matrix3();
	private static final double MOUSE_TURN_SPEED = 0.1;
	private int rotationSign = 1;

	private PickResults pickResults;
	private HousePart drawn = null;

	private int operation = SELECT;
	protected HousePart lastHoveredObject;

	public SceneManager(final Container panel) {
		instance = this;
		this.panel = panel;
		root.attachChild(housePartsNode);

		final DisplaySettings settings = new DisplaySettings(400, 300, 24, 0, 0, 16, 0, 0, false, false);
		renderer = new JoglCanvasRenderer(this);
		canvas = new JoglAwtCanvas(settings, renderer);
		frameHandler = new FrameHandler(new Timer());
		frameHandler.addCanvas(canvas);

		logicalLayer = new LogicalLayer();
		final AwtMouseWrapper mouseWrapper = new AwtMouseWrapper(canvas, new AwtMouseManager(canvas));
		final AwtKeyboardWrapper keyboardWrapper = new AwtKeyboardWrapper(canvas);
		final AwtFocusWrapper focusWrapper = new AwtFocusWrapper(canvas);
		final PhysicalLayer pl = new PhysicalLayer(keyboardWrapper, mouseWrapper, focusWrapper);
		logicalLayer.registerInput(canvas, pl);

		/**
		 * Create a ZBuffer to display pixels closest to the camera above farther ones.
		 */
		final ZBufferState buf = new ZBufferState();
		buf.setEnabled(true);
		buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
		root.setRenderState(buf);

		// // ---- LIGHTS
		// /** Set up a basic, default light. */
		// final PointLight light = new PointLight();
		// light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
		// light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
		// light.setLocation(new Vector3(100, 100, 100));
		// light.setEnabled(true);
		//
		// /** Attach the light to a lightState and the lightState to rootNode. */
		// LightState _lightState = new LightState();
		// _lightState.setEnabled(true);
		// _lightState.attach(light);
		// root.setRenderState(_lightState);

		// initScene();

		// registerInputTriggers();
		frameHandler.addUpdater(this);

		// canvas.setSize(new Dimension(400, 300));
		// canvas.setVisible(true);

		panel.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent e) {
				final Dimension size = panel.getSize();
				if ((size.width == 0) && (size.height == 0)) {
					return;
				}
				final Camera camera = renderer.getCamera();
				if (camera != null) {
					// camera.setFrustumPerspective(fovY, aspect, near, far);
					camera.resize(size.width, size.height);
				}
			}
		});

		panel.add(canvas, "Center");

	}

	@MainThread
	public void init() {
		// drawn = new DrawnRectangle();
		// root.attachChild(drawn.getRoot());

		final Dimension size = panel.getSize();
		final Camera camera = renderer.getCamera();
		if ((size.width == 0) && (size.height == 0)) {
			return;
		}
		camera.resize(size.width, size.height);
		resetCamera(canvas);

		// enable depth test
		final ZBufferState buf = new ZBufferState();
		buf.setEnabled(true);
		buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
		root.setRenderState(buf);

		AWTImageLoader.registerLoader();

		try {
			SimpleResourceLocator srl = new SimpleResourceLocator(ExampleBase.class.getClassLoader().getResource("org/concord/energy3d/images/"));
			ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, srl);
			// srl = new SimpleResourceLocator(ExampleBase.class.getClassLoader().getResource(
			// "com/ardor3d/example/media/models/"));
			// ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, srl);
		} catch (final URISyntaxException ex) {
			ex.printStackTrace();
		}

		// Set up a reusable pick results
		pickResults = new PrimitivePickResults();
		pickResults.setCheckDistance(true);

		Node axis = createAxis();
		root.attachChild(axis);

		// make floor
		floor = new Quad("Floor", 100, 100);
		floor.setDefaultColor(new ColorRGBA(0, 1, 0, 0.5f));
		// box.setRotation(new Matrix3().fromAngles(-90 * MathUtils.DEG_TO_RAD, 0, 0));
		root.attachChild(floor);

		BlendState blendState = new BlendState();
		blendState.setBlendEnabled(true);
		blendState.setSourceFunction(BlendState.SourceFunction.SourceAlpha);
		blendState.setDestinationFunction(BlendState.DestinationFunction.OneMinusSourceAlpha);
		blendState.setTestEnabled(true);
		blendState.setTestFunction(BlendState.TestFunction.GreaterThan);
		floor.setRenderState(blendState);
		floor.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);

		// Quad q2 = new Quad("Front", 1, 1);
		// q2.setDefaultColor(new ColorRGBA(1, 0, 0, 0));
		// root.attachChild(q2);

		// Add a texture to the box.
		// final TextureState ts = new TextureState();
		// ts.setTexture(TextureManager.load("images/ardor3d_white_256.jpg", Texture.MinificationFilter.Trilinear, Format.GuessNoCompression, true));
		// box.setRenderState(ts);
		//
		// final PointLight light = new PointLight();
		//
		// final Random random = new Random();
		//
		// final float r = random.nextFloat();
		// final float g = random.nextFloat();
		// final float b = random.nextFloat();
		// final float a = random.nextFloat();
		//
		// light.setDiffuse(new ColorRGBA(r, g, b, a));
		// light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
		// light.setLocation(new Vector3(MOVE_SPEED, MOVE_SPEED, MOVE_SPEED));
		// light.setEnabled(true);

		/** Attach the light to a lightState and the lightState to rootNode. */
		// final LightState lightState = new LightState();
		// lightState.setEnabled(true);
		// lightState.attach(light);
		// root.setRenderState(lightState);

		// root.attachChild(box);

		// add Axis
		// AxisRods axis = new AxisRods("Axis", true, 1, 0.01);
		// axis.setTranslation(5, 5, 5);
		// Node axis = createAxis();
		// root.attachChild(axis);

		registerInputTriggers();

		root.updateGeometricState(0, true);
	}

	private Node createAxis() {
		final int axisLen = 50;
		Node axis = new Node("Axis");

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

		Line line = new Line("Axis", verts, null, colors, null);
		line.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		axis.attachChild(line);

		return axis;
	}

	public void run() {
		try {
			frameHandler.init();

			while (!_exit) {
				frameHandler.updateFrame();
				Thread.yield();

			}
			// grab the graphics context so cleanup will work out.
			canvas.getCanvasRenderer().setCurrentContext();
			quit(canvas.getCanvasRenderer().getRenderer());
		} catch (final Throwable t) {
			System.err.println("Throwable caught in MainThread - exiting");
			t.printStackTrace(System.err);
		}
	}

	protected void quit(final Renderer renderer) {
		ContextGarbageCollector.doFinalCleanup(renderer);
		// _canvas.close();
	}

	@Override
	public PickResults doPick(Ray3 pickRay) {
		return null;
	}

	@Override
	public boolean renderUnto(Renderer renderer) {
		renderer.draw(root);
//		Debugger.drawBounds(root, renderer, true);
		return true;
	}

	public JoglAwtCanvas getCanvas() {
		return canvas;
	}

	private void registerInputTriggers() {
		logicalLayer.registerTrigger(new InputTrigger(new MouseButtonClickedCondition(MouseButton.LEFT), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				MouseState mouseState = inputStates.getCurrent().getMouseState();
				if (operation == SELECT) {
					if (drawn.isDrawCompleted())
						selectHousePart(mouseState.getX(), mouseState.getY(), true);
					else
						drawn.complete();
					return;
				}
				Vector3 v = findMousePoint(mouseState.getX(), mouseState.getY());
				int x = mouseState.getX();
				int y = mouseState.getY();
				if (drawn == null || drawn.isDrawCompleted()) {
					if (operation == DRAW_LINES) {
						
						if (v != null) {
							
							drawn = new Wall(x, y);
							
					// else if (operation == DRAW_RECTANGLE)
					// drawn.editPoint(0);
					// drawn = new DrawnRectangle();
					housePartsNode.attachChild(drawn.getRoot());
					House.getInstance().add(drawn);
						}
					}
				} else
					drawn.addPoint(x, y);
					
			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new MouseMovedCondition(), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				// if (operation != SELECT) {
				MouseState mouseState = inputStates.getCurrent().getMouseState();
//				Vector3 v = findMousePoint(mouseState.getX(), mouseState.getY());
				int x = inputStates.getCurrent().getMouseState().getX();
				int y = inputStates.getCurrent().getMouseState().getY();				
//				if (drawn != null && v != null && !drawn.isDrawCompleted())
				if (drawn != null && !drawn.isDrawCompleted())
					drawn.setPreviewPoint(x, y);
				else {
					selectHousePart(mouseState.getX(), mouseState.getY(), false);
				}
				// }
			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.W), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				moveForward(source, tpf);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.S), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				moveBack(source, tpf);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.A), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				turnLeft(source, tpf);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.D), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				turnRight(source, tpf);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.Q), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				moveLeft(source, tpf);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.E), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				moveRight(source, tpf);
			}
		}));

		// logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.ESCAPE), new TriggerAction() {
		// public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
		// exit.exit();
		// }
		// }));

		// logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(toggleRotationKey), new TriggerAction() {
		// public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
		// toggleRotation();
		// }
		// }));
		logicalLayer.registerTrigger(new InputTrigger(new KeyReleasedCondition(Key.U), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				toggleRotation();
			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.ZERO), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				resetCamera(source);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.NINE), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				lookAtZero(source);
			}
		}));

		final Predicate<TwoInputStates> mouseMovedAndOneButtonPressed = Predicates.and(TriggerConditions.mouseMoved(), Predicates.or(TriggerConditions.leftButtonDown(), TriggerConditions.rightButtonDown()));

		logicalLayer.registerTrigger(new InputTrigger(mouseMovedAndOneButtonPressed, new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (operation == SELECT) {
					final MouseState mouseState = inputStates.getCurrent().getMouseState();

					turn(source, mouseState.getDx() * tpf * -MOUSE_TURN_SPEED);
					rotateUpDown(source, mouseState.getDy() * tpf * -MOUSE_TURN_SPEED);
				}
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new MouseButtonCondition(ButtonState.DOWN, ButtonState.DOWN, ButtonState.UNDEFINED), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				moveForward(source, tpf);
			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new MouseButtonCondition(ButtonState.DOWN, ButtonState.DOWN, ButtonState.UNDEFINED), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				moveForward(source, tpf);
			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new AnyKeyCondition(), new TriggerAction() {
			public void perform(Canvas source, TwoInputStates inputStates, double tpf) {
				final InputState current = inputStates.getCurrent();

				System.out.println("Key character pressed: " + current.getKeyboardState().getKeyEvent().getKeyChar());
			}
		}));
	}

	private void lookAtZero(final Canvas source) {
		source.getCanvasRenderer().getCamera().lookAt(Vector3.ZERO, Vector3.UNIT_Y);
	}

	private void resetCamera(final Canvas source) {
		// final Vector3 loc = new Vector3(1.0f, 1.0f, 5.0f);
		// final Vector3 left = new Vector3(-1.0f, 0.0f, 0.0f);
		// final Vector3 up = new Vector3(0.0f, 1.0f, 0.0f);
		// final Vector3 dir = new Vector3(-1.0f, 0.0f, -1.0f);

		final Vector3 loc = new Vector3(1.0f, -5.0f, 1.0f);
		final Vector3 left = new Vector3(-1.0f, 0.0f, 0.0f);
		final Vector3 up = new Vector3(0.0f, 0.0f, 1.0f);
		final Vector3 dir = new Vector3(0.0f, 1.0f, 0.0f);

		source.getCanvasRenderer().getCamera().setFrame(loc, left, up, dir);
	}

	private void toggleRotation() {
		rotationSign *= -1;
	}

	@MainThread
	public void update(final ReadOnlyTimer timer) {
		final double tpf = timer.getTimePerFrame();

		logicalLayer.checkTriggers(tpf);

		// box.setRotation(new Matrix3().fromAngles(1, 1, 1));

		// // rotate away
		//
		// angle += tpf * CUBE_ROTATE_SPEED * rotationSign;
		//		
		// rotation.fromAngleAxis(angle, rotationAxis);
		// box.setRotation(rotation);
		//		
		// box.updateGeometricState(tpf, true);

		if (drawn != null)
			drawn.getRoot().updateGeometricState(tpf, true);

	}

	private void rotateUpDown(final Canvas canvas, final double speed) {
		final Camera camera = canvas.getCanvasRenderer().getCamera();

		final Vector3 temp = Vector3.fetchTempInstance();
		_incr.fromAngleNormalAxis(speed, camera.getLeft());

		_incr.applyPost(camera.getLeft(), temp);
		camera.setLeft(temp);

		_incr.applyPost(camera.getDirection(), temp);
		camera.setDirection(temp);

		_incr.applyPost(camera.getUp(), temp);
		camera.setUp(temp);

		Vector3.releaseTempInstance(temp);

		camera.normalize();

	}

	private void turnRight(final Canvas canvas, final double tpf) {
		turn(canvas, -TURN_SPEED * tpf);
	}

	private void turn(final Canvas canvas, final double speed) {
		final Camera camera = canvas.getCanvasRenderer().getCamera();

		final Vector3 temp = Vector3.fetchTempInstance();
		_incr.fromAngleNormalAxis(speed, camera.getUp());

		_incr.applyPost(camera.getLeft(), temp);
		camera.setLeft(temp);

		_incr.applyPost(camera.getDirection(), temp);
		camera.setDirection(temp);

		_incr.applyPost(camera.getUp(), temp);
		camera.setUp(temp);
		Vector3.releaseTempInstance(temp);

		camera.normalize();
	}

	private void turnLeft(final Canvas canvas, final double tpf) {
		turn(canvas, TURN_SPEED * tpf);
	}

	private void moveForward(final Canvas canvas, final double tpf) {
		final Camera camera = canvas.getCanvasRenderer().getCamera();
		final Vector3 loc = Vector3.fetchTempInstance().set(camera.getLocation());
		final Vector3 dir = Vector3.fetchTempInstance();
		if (camera.getProjectionMode() == ProjectionMode.Perspective) {
			dir.set(camera.getDirection());
		} else {
			// move up if in parallel mode
			dir.set(camera.getUp());
		}
		dir.multiplyLocal(MOVE_SPEED * tpf);
		loc.addLocal(dir);
		camera.setLocation(loc);
		Vector3.releaseTempInstance(loc);
		Vector3.releaseTempInstance(dir);
	}

	private void moveLeft(final Canvas canvas, final double tpf) {
		final Camera camera = canvas.getCanvasRenderer().getCamera();
		final Vector3 loc = Vector3.fetchTempInstance().set(camera.getLocation());
		final Vector3 dir = Vector3.fetchTempInstance();

		dir.set(camera.getLeft());

		dir.multiplyLocal(MOVE_SPEED * tpf);
		loc.addLocal(dir);
		camera.setLocation(loc);
		Vector3.releaseTempInstance(loc);
		Vector3.releaseTempInstance(dir);
	}

	private void moveRight(final Canvas canvas, final double tpf) {
		final Camera camera = canvas.getCanvasRenderer().getCamera();
		final Vector3 loc = Vector3.fetchTempInstance().set(camera.getLocation());
		final Vector3 dir = Vector3.fetchTempInstance();

		dir.set(camera.getLeft());

		dir.multiplyLocal(-MOVE_SPEED * tpf);
		loc.addLocal(dir);
		camera.setLocation(loc);
		Vector3.releaseTempInstance(loc);
		Vector3.releaseTempInstance(dir);
	}

	private void moveBack(final Canvas canvas, final double tpf) {
		final Camera camera = canvas.getCanvasRenderer().getCamera();
		final Vector3 loc = Vector3.fetchTempInstance().set(camera.getLocation());
		final Vector3 dir = Vector3.fetchTempInstance();
		if (camera.getProjectionMode() == ProjectionMode.Perspective) {
			dir.set(camera.getDirection());
		} else {
			// move up if in parallel mode
			dir.set(camera.getUp());
		}
		dir.multiplyLocal(-MOVE_SPEED * tpf);
		loc.addLocal(dir);
		camera.setLocation(loc);
		Vector3.releaseTempInstance(loc);
		Vector3.releaseTempInstance(dir);
	}

	public static SceneManager getInstance() {
		return instance;
	}

	public void setOperation(int operation) {
		this.operation = operation;
		if (operation == DRAW_RECTANGLE)
			drawn.editPoint(0);
	}

	public int getOperation() {
		return operation;
	}

	private void pick(int x, int y, Spatial target) {
		// Put together a pick ray
		final Vector2 pos = Vector2.fetchTempInstance().set(x, y);
		final Ray3 pickRay = Ray3.fetchTempInstance();
		canvas.getCanvasRenderer().getCamera().getPickRay(pos, false, pickRay);
		Vector2.releaseTempInstance(pos);

		// Do the pick
		pickResults.clear();
		PickingUtil.findPick(target, pickRay, pickResults);
		Ray3.releaseTempInstance(pickRay);
	}

	public Vector3 findMousePoint(int x, int y) {
		pick(x, y, floor);

		if (pickResults.getNumber() > 0) {
			final PickData pick = pickResults.getPickData(0);
			final IntersectionRecord intersectionRecord = pick.getIntersectionRecord();
			if (intersectionRecord.getNumberOfIntersections() > 0)
				return intersectionRecord.getIntersectionPoint(0);
		}
		return null;
	}

	private Mesh findMouseSelection(int x, int y) {
		pick(x, y, housePartsNode);

		System.out.println(pickResults.getNumber());
		if (pickResults.getNumber() > 0) {
			final PickData pick = pickResults.getPickData(0);
			// System.out.println(pick.getTargetMesh());
			// System.out.println(pick.getTargetPrimitives());
			// System.out.println(pick.getIntersectionRecord());
			final IntersectionRecord intersectionRecord = pick.getIntersectionRecord();
			if (intersectionRecord.getNumberOfIntersections() > 0) {
				System.out.println("PICK");
				return pick.getTargetMesh();
			}
		}
		System.out.println("NO PICK");
		return null;
	}

	private void selectHousePart(int x, int y, boolean edit) {
		Mesh selectedMesh = findMouseSelection(x, y);
		// if (selectedMesh == null)
		// return;
		Object data = null;
		if (selectedMesh != null) {
			data = selectedMesh.getUserData();
			if (data == null)
				data = selectedMesh.getParent().getUserData();
		}
		if (data == null || data instanceof HousePart) {
			HousePart housePart = (HousePart) data;
			if (lastHoveredObject != null && lastHoveredObject != housePart) {
				lastHoveredObject.hidePoints();
				lastHoveredObject = null;
			}
			if (housePart != null) {
				housePart.showPoints();
				lastHoveredObject = housePart;
			}
		} else if (edit && data instanceof Integer) {
			drawn = (HousePart) selectedMesh.getParent().getParent().getUserData();
			drawn.editPoint((Integer) data);
		}
	}

}
