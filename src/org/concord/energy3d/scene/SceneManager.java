package org.concord.energy3d.scene;

import java.awt.Container;
import java.awt.Dimension;
import java.net.URL;
import java.nio.FloatBuffer;

import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.PickedHousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Roof2;
import org.concord.energy3d.model.UserData;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;

import com.ardor3d.annotation.MainThread;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.FrameHandler;
import com.ardor3d.framework.Updater;
import com.ardor3d.framework.jogl.JoglAwtCanvas;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.image.Texture;
import com.ardor3d.image.TextureStoreFormat;
import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.input.Key;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.MouseState;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.awt.AwtFocusWrapper;
import com.ardor3d.input.awt.AwtKeyboardWrapper;
import com.ardor3d.input.awt.AwtMouseManager;
import com.ardor3d.input.awt.AwtMouseWrapper;
import com.ardor3d.input.control.FirstPersonControl;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyHeldCondition;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.input.logical.MouseButtonPressedCondition;
import com.ardor3d.input.logical.MouseButtonReleasedCondition;
import com.ardor3d.input.logical.MouseMovedCondition;
import com.ardor3d.input.logical.MouseWheelMovedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.intersection.PickData;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.light.PointLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Quaternion;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.Camera.ProjectionMode;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.LightState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.renderer.state.MaterialState.ColorMaterial;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.shape.Dome;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.util.ContextGarbageCollector;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.Timer;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;

public class SceneManager implements com.ardor3d.framework.Scene, Runnable, Updater {
	public enum Operation {
		SELECT,
		DRAW_WALL,
		DRAW_DOOR,
		DRAW_ROOF,
		DRAW_ROOF_HIP,
		DRAW_WINDOW,
		DRAW_FOUNDATION,
		DRAW_FLOOR		
	}
//	public static final int SELECT = 0;
//	public static final int DRAW_WALL = 1;
//	public static final int DRAW_DOOR = 2;
//	public static final int DRAW_ROOF = 3;
//	public static final int DRAW_WINDOW = 4;
//	public static final int DRAW_FOUNDATION = 5;
//	public static final int DRAW_FLOOR = 6;

	private static SceneManager instance = null;
	private final Container panel;
	private final JoglAwtCanvas canvas;
	private final JoglCanvasRenderer renderer;
	private final FrameHandler frameHandler;
	private final LogicalLayer logicalLayer;
	private boolean _exit = false;
	protected final Node root = new Node("Root");
	private final Node housePartsNode = Scene.root; // new Node("House Parts");
	

	private Mesh floor;

	private static final int MOVE_SPEED = 5;
	// private static final double TURN_SPEED = 0.5;
	// private final Matrix3 _incr = new Matrix3();
	// private static final double MOUSE_TURN_SPEED = 0.1;
	private boolean rot = false;

	private PickResults pickResults;
	private HousePart drawn = null;

	private Operation operation = Operation.SELECT;
	protected HousePart lastHoveredObject;
	private LightState lightState;
	private double angle;
	private Matrix3 rotate = new Matrix3();
	private boolean topView;
	// private ReadOnlyVector3 axis = new Vector3(0, 0, 1);
	private FirstPersonControl control;

	public static SceneManager getInstance() {
		return instance;
	}

	public SceneManager(final Container panel) {
		instance = this;
		this.panel = panel;
		root.attachChild(housePartsNode);

		final DisplaySettings settings = new DisplaySettings(400, 300, 24, 0, 0, 16, 0, 8, false, false);
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

		frameHandler.addUpdater(this);

		panel.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent e) {
				final Dimension size = panel.getSize();
				if ((size.width == 0) && (size.height == 0)) {
					return;
				}
				final Camera camera = renderer.getCamera();
				if (camera != null) {
					camera.resize(size.width, size.height);
					resizeCamera(camera);
				}
			}
		});
		panel.add(canvas, "Center");
	}

	@MainThread
	public void init() {
		final Dimension size = panel.getSize();
		final Camera camera = renderer.getCamera();
		if ((size.width == 0) && (size.height == 0)) {
			return;
		}
		camera.resize(size.width, size.height);
//		camera.set
//		camera.setFrustumNear(0.1);
		resetCamera(canvas);

		AWTImageLoader.registerLoader();

		try {
			URL resource = SceneManager.class.getClassLoader().getResource("org/concord/energy3d/images/");
			System.out.println(resource);
			SimpleResourceLocator srl = new SimpleResourceLocator(resource);
			ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, srl);
		} catch (final Exception ex) {
			ex.printStackTrace();
		}

		// enable depth test
		final ZBufferState buf = new ZBufferState();
		buf.setEnabled(true);
		buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
		root.setRenderState(buf);

		/** Set up a basic, default light. */
		final PointLight light = new PointLight();
		// light.setDiffuse(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.75f));
		// light.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
		light.setDiffuse(new ColorRGBA(1, 1, 1, 1));
		light.setAmbient(new ColorRGBA(0.75f, 0.75f, 0.75f, 1.0f));

		// light.setLocation(new Vector3(5, -5, 10));
		light.setLocation(new Vector3(0, -20, 10));
		light.setEnabled(true);

		lightState = new LightState();
		lightState.setEnabled(false);
		lightState.attach(light);
		root.setRenderState(lightState);

		// Set up a reusable pick results
		pickResults = new PrimitivePickResults();
		pickResults.setCheckDistance(true);

		root.attachChild(createAxis());
		root.attachChild(createFloor());
		root.attachChild(createSky());

		// Wall w1 = testWall(0, 0, 0, 2);
		// Wall w2 = testWall(0, 2, 2, 2);
		// Wall w3 = testWall(2, 2, 2, 0);

		Scene.getInstance();

		registerInputTriggers();

		root.updateGeometricState(0, true);
	}

	// private Wall testWall(double x1, double y1, double x2, double y2) {
	// Wall wall;
	// wall = new Wall();
	// wall.getPoints().set(0, new Vector3(x1,y1,0));
	// wall.getPoints().set(1, new Vector3(x1,y1,1));
	// wall.getPoints().add(new Vector3(x2,y2,0));
	// wall.getPoints().add(new Vector3(x2,y2,1));
	// wall.complete();
	// wall.draw();
	// addHousePart(wall);
	// return wall;
	// }

	public void run() {
		try {
			frameHandler.init();

			while (!_exit) {
				try {
					frameHandler.updateFrame();
				} catch (Exception e) {
					e.printStackTrace();
				}
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

	@MainThread
	public void update(final ReadOnlyTimer timer) {
		final double tpf = timer.getTimePerFrame();
		logicalLayer.checkTriggers(tpf);				

		if (rot) {
			// Update the angle using the current tpf to rotate at a constant speed.
			angle = 1; //timer.getTimePerFrame() * 50;
			// Wrap the angle to keep it inside 0-360 range
			angle %= 360;

			// Update the rotation matrix using the angle and rotation axis.
			rotate.fromAngleNormalAxis(angle * MathUtils.DEG_TO_RAD, Vector3.UNIT_Z);
			
			renderer.getCamera().setLocation(rotate.applyPre(renderer.getCamera().getLocation(), null));
			renderer.getCamera().lookAt(0, 0, 1, Vector3.UNIT_Z);
			
			// Update the box rotation using the rotation matrix.
			root.setRotation(rotate);
		}
		root.updateGeometricState(tpf);
		
//			Scene.getInstance().updateTexture();
//		Scene.getInstance().init();

		// if (drawn != null)
		// drawn.getRoot().updateGeometricState(tpf, true);
	}

	private void quit(final Renderer renderer) {
		ContextGarbageCollector.doFinalCleanup(renderer);
		// _canvas.close();
	}

//	@Override
	public boolean renderUnto(Renderer renderer) {
//		if (!Scene.getInstance().getParts().isEmpty())
//			Scene.getInstance().renderTexture(renderer);
//		Scene.getInstance().init();
			renderer.draw(root);
//		Debugger.drawBounds(root, renderer, true);
		return true;
	}

//	@Override
	public PickResults doPick(Ray3 pickRay) {
		return null;
	}

	public JoglAwtCanvas getCanvas() {
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

		// Add a material to the box, to show both vertex color and lighting/shading.
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		floor.setRenderState(ms);

		return floor;
	}

	private Mesh createSky() {
		 Dome sky = new Dome("Sky", 100, 100, 100);
//		Sphere sky = new Sphere("Sky", 100, 100, 100);
//		sky.setTextureMode(TextureMode.Polar);
		sky.setRotation(new Quaternion(1, 0, 0, 1));
//		sky.setTranslation(0, 0, 10);
		// Add a texture to the box.
		final TextureState ts = new TextureState();
		ts.setTexture(TextureManager.load("sky6.jpg", Texture.MinificationFilter.Trilinear, TextureStoreFormat.GuessNoCompressedFormat, true));
		sky.setRenderState(ts);

		// Add a material to the box, to show both vertex color and lighting/shading.
		final MaterialState ms = new MaterialState();
		ms.setColorMaterial(ColorMaterial.Diffuse);
		sky.setRenderState(ms);

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

		Line lines = new Line("Axis", verts, null, colors, null);
		lines.getSceneHints().setLightCombineMode(LightCombineMode.Off);

		return lines;
	}

	private void registerInputTriggers() {
		control = new FirstPersonControl(Vector3.UNIT_Z) {
			
			@Override
			protected void rotate(Camera camera, double dx, double dy) {
				if (operation == Operation.SELECT && (drawn == null || drawn.isDrawCompleted()) && !topView)
					super.rotate(camera, dx, dy);
			}
		};
		control.setupKeyboardTriggers(logicalLayer);
		control.setupMouseTriggers(logicalLayer, true);
		control.setMoveSpeed(MOVE_SPEED);

		logicalLayer.registerTrigger(new InputTrigger(new MouseButtonPressedCondition(MouseButton.LEFT), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				MouseState mouseState = inputStates.getCurrent().getMouseState();
				if (operation == Operation.SELECT) {
					if (drawn == null || drawn.isDrawCompleted())
						selectHousePart(mouseState.getX(), mouseState.getY(), true);
				} else
					drawn.addPoint(mouseState.getX(), mouseState.getY());
			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new MouseButtonReleasedCondition(MouseButton.LEFT), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (operation == Operation.DRAW_ROOF)
					return;
				MouseState mouseState = inputStates.getCurrent().getMouseState();
				if (operation == Operation.SELECT) {
					if (drawn != null)
						drawn.complete();
					return;
				}

				if (!drawn.isDrawCompleted())
					drawn.addPoint(mouseState.getX(), mouseState.getY());

				if (drawn.isDrawCompleted()) {
//					Scene.root.updateWorldBound(true);
					drawn.hidePoints();
					drawn = newHousePart();
				}
			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new MouseMovedCondition(), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				final MouseState mouseState = inputStates.getCurrent().getMouseState();
				int x = mouseState.getX();
				int y = mouseState.getY();
//				System.out.println(drawn + " " + drawn.isDrawCompleted());
				if (drawn != null && !drawn.isDrawCompleted()) {
					drawn.setPreviewPoint(x, y);
				} else {
					selectHousePart(x, y, false);
				}
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.DELETE), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				removeHousePart(drawn);
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
				if (topView)
					moveUpDown(source, tpf, true);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.S), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (topView)
					moveUpDown(source, tpf, false);
			}
		}));
		// logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.W), new TriggerAction() {
		// public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
		// moveForward(source, tpf);
		// }
		// }));
		// logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.S), new TriggerAction() {
		// public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
		// moveBack(source, tpf);
		// }
		// }));
		// logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.A), new TriggerAction() {
		// public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
		// turnLeft(source, tpf);
		// }
		// }));
		// logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.D), new TriggerAction() {
		// public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
		// turnRight(source, tpf);
		// }
		// }));
		// logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.Q), new TriggerAction() {
		// public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
		// moveLeft(source, tpf);
		// }
		// }));
		// logicalLayer.registerTrigger(new InputTrigger(new KeyHeldCondition(Key.E), new TriggerAction() {
		// public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
		// moveRight(source, tpf);
		// }
		// }));
		//
		// // logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.ESCAPE), new TriggerAction() {
		// // public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
		// // exit.exit();
		// // }
		// // }));
		//
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.R), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				toggleRotation();
			}
		}));
		//
		// logicalLayer.registerTrigger(new InputTrigger(new KeyReleasedCondition(Key.U), new TriggerAction() {
		// public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
		// toggleRotation();
		// }
		// }));
		//
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.ZERO), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				resetCamera(source);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.ONE), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				topCameraView(source);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new KeyPressedCondition(Key.NINE), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				lookAtZero(source);
			}
		}));
		logicalLayer.registerTrigger(new InputTrigger(new MouseWheelMovedCondition(), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				move(source, tpf, inputStates.getCurrent().getMouseState().getDwheel());
			}
		}));
		//
		// final Predicate<TwoInputStates> mouseMovedAndOneButtonPressed = Predicates.and(TriggerConditions.mouseMoved(), Predicates.or(TriggerConditions.leftButtonDown(), TriggerConditions.rightButtonDown()));
		//
		// logicalLayer.registerTrigger(new InputTrigger(mouseMovedAndOneButtonPressed, new TriggerAction() {
		// public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
		// if (operation == SELECT && (drawn == null || drawn.isDrawCompleted())) {
		// final MouseState mouseState = inputStates.getCurrent().getMouseState();
		//
		// turn(source, mouseState.getDx() * tpf * -MOUSE_TURN_SPEED);
		// rotateUpDown(source, mouseState.getDy() * tpf * -MOUSE_TURN_SPEED);
		// }
		// }
		// }));
		// logicalLayer.registerTrigger(new InputTrigger(new MouseButtonCondition(ButtonState.DOWN, ButtonState.DOWN, ButtonState.UNDEFINED), new TriggerAction() {
		// public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
		// moveForward(source, tpf);
		// }
		// }));
		//
		// logicalLayer.registerTrigger(new InputTrigger(new MouseButtonCondition(ButtonState.DOWN, ButtonState.DOWN, ButtonState.UNDEFINED), new TriggerAction() {
		// public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
		// moveForward(source, tpf);
		// }
		// }));
		//
		// logicalLayer.registerTrigger(new InputTrigger(new AnyKeyCondition(), new TriggerAction() {
		// public void perform(Canvas source, TwoInputStates inputStates, double tpf) {
		// final InputState current = inputStates.getCurrent();
		//
		// System.out.println("Key character pressed: " + current.getKeyboardState().getKeyEvent().getKeyChar());
		// }
		// }));
	}

	private void hideAllEditPoints() {
		for (HousePart part : Scene.getInstance().getParts())
			part.hidePoints();
	}

	private void lookAtZero(final Canvas source) {
		source.getCanvasRenderer().getCamera().lookAt(Vector3.ZERO, Vector3.UNIT_Y);
	}

	public void resetCamera(final Canvas source) {
		topView = false;
		final Vector3 loc = new Vector3(1.0f, -5.0f, 1.0f);
		final Vector3 left = new Vector3(-1.0f, 0.0f, 0.0f);
		final Vector3 up = new Vector3(0.0f, 0.0f, 1.0f);
		final Vector3 dir = new Vector3(0.0f, 1.0f, 0.0f);

		Camera camera = source.getCanvasRenderer().getCamera();
		camera.setFrame(loc, left, up, dir);
//		camera.lookAt(0, 0, 0, Vector3.UNIT_Z);

		camera.setProjectionMode(ProjectionMode.Perspective);

//		float scale = 0.5f;
		resizeCamera(camera);
	}

	public void topCameraView(final Canvas source) {
		topView = true;
		final Vector3 loc = new Vector3(0, 0, 50);
		final Vector3 left = new Vector3(-1.0f, 0.0f, 0.0f);
		final Vector3 up = new Vector3(0.0f, -1.0f, 0.0f);
		final Vector3 dir = new Vector3(0.0f, 0.0f, -1.0f);

		Camera camera = source.getCanvasRenderer().getCamera();
		camera.setFrame(loc, left, up, dir);
		camera.setProjectionMode(ProjectionMode.Parallel);
//		float scale = 4;
//		double ratio = (double) camera.getWidth() / camera.getHeight();
//		camera.setFrustumTop(scale);
//		camera.setFrustumBottom(-scale);
//		camera.setFrustumLeft(-scale * ratio);
//		camera.setFrustumRight(scale * ratio);
//		camera.update();
		resizeCamera(camera);
	}

	private void resizeCamera(final Camera camera) {
		final double scale = topView ? 4 : 0.5;
		final double ratio = (double) camera.getWidth() / camera.getHeight();
		camera.setFrustumTop(scale);
		camera.setFrustumBottom(-scale);
		camera.setFrustumLeft(-scale*ratio);
		camera.setFrustumRight(scale*ratio);
		camera.update();
	}

	public void toggleRotation() {
		rot = !rot;
	}

	// private void rotateUpDown(final Canvas canvas, final double speed) {
	// final Camera camera = canvas.getCanvasRenderer().getCamera();
	//
	// final Vector3 temp = Vector3.fetchTempInstance();
	// _incr.fromAngleNormalAxis(speed, camera.getLeft());
	//
	// _incr.applyPost(camera.getLeft(), temp);
	// camera.setLeft(temp);
	//
	// _incr.applyPost(camera.getDirection(), temp);
	// camera.setDirection(temp);
	//
	// _incr.applyPost(camera.getUp(), temp);
	// camera.setUp(temp);
	//
	// Vector3.releaseTempInstance(temp);
	//
	// camera.normalize();
	//
	// }
	//
	// private void turnRight(final Canvas canvas, final double tpf) {
	// turn(canvas, -TURN_SPEED * tpf);
	// }
	//
	// private void turn(final Canvas canvas, final double speed) {
	// final Camera camera = canvas.getCanvasRenderer().getCamera();
	//
	// final Vector3 temp = Vector3.fetchTempInstance();
	// _incr.fromAngleNormalAxis(speed, camera.getUp());
	//
	// _incr.applyPost(camera.getLeft(), temp);
	// camera.setLeft(temp);
	//
	// _incr.applyPost(camera.getDirection(), temp);
	// camera.setDirection(temp);
	//
	// _incr.applyPost(camera.getUp(), temp);
	// camera.setUp(temp);
	// Vector3.releaseTempInstance(temp);
	//
	// camera.normalize();
	// }
	//
	// private void turnLeft(final Canvas canvas, final double tpf) {
	// turn(canvas, TURN_SPEED * tpf);
	// }
	//
	 private void move(final Canvas canvas, final double tpf, int val) {
	 final Camera camera = canvas.getCanvasRenderer().getCamera();
	 final Vector3 loc = Vector3.fetchTempInstance().set(camera.getLocation());
	 final Vector3 dir = Vector3.fetchTempInstance();
	 // if (camera.getProjectionMode() == ProjectionMode.Perspective) {
	 // dir.set(camera.getDirection());
	 // } else {
	 // // move up if in parallel mode
	 dir.set(camera.getDirection());
	 // }
	 dir.multiplyLocal(-val * MOVE_SPEED * 10 * tpf);
	 loc.addLocal(dir);
	 camera.setLocation(loc);
	 Vector3.releaseTempInstance(loc);
	 Vector3.releaseTempInstance(dir);
	 }

	private void moveUpDown(final Canvas canvas, final double tpf, boolean up) {
		final Camera camera = canvas.getCanvasRenderer().getCamera();
		final Vector3 loc = Vector3.fetchTempInstance().set(camera.getLocation());
		final Vector3 dir = Vector3.fetchTempInstance();

		dir.set(camera.getUp());

		if (topView)
			up = !up;

		dir.multiplyLocal((up ? 1 : -1) * MOVE_SPEED * tpf);
		loc.addLocal(dir);
		camera.setLocation(loc);
		Vector3.releaseTempInstance(loc);
		Vector3.releaseTempInstance(dir);
	}

	//
	// private void moveRight(final Canvas canvas, final double tpf) {
	// final Camera camera = canvas.getCanvasRenderer().getCamera();
	// final Vector3 loc = Vector3.fetchTempInstance().set(camera.getLocation());
	// final Vector3 dir = Vector3.fetchTempInstance();
	//
	// dir.set(camera.getLeft());
	//
	// dir.multiplyLocal(-MOVE_SPEED * tpf);
	// loc.addLocal(dir);
	// camera.setLocation(loc);
	// Vector3.releaseTempInstance(loc);
	// Vector3.releaseTempInstance(dir);
	// }
	//
	// private void moveBack(final Canvas canvas, final double tpf) {
	// final Camera camera = canvas.getCanvasRenderer().getCamera();
	// final Vector3 loc = Vector3.fetchTempInstance().set(camera.getLocation());
	// final Vector3 dir = Vector3.fetchTempInstance();
	// if (camera.getProjectionMode() == ProjectionMode.Perspective) {
	// dir.set(camera.getDirection());
	// } else {
	// // move up if in parallel mode
	// dir.set(camera.getUp());
	// }
	// dir.multiplyLocal(-MOVE_SPEED * tpf);
	// loc.addLocal(dir);
	// camera.setLocation(loc);
	// Vector3.releaseTempInstance(loc);
	// Vector3.releaseTempInstance(dir);
	// }

	public void setOperation(Operation operation) {
		this.operation = operation;
		if (drawn != null && !drawn.isDrawCompleted())
			removeHousePart(drawn);
		drawn = newHousePart();
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
			drawn = new Roof();
		else if (operation == Operation.DRAW_ROOF_HIP)
			drawn = new Roof2();
		else if (operation == Operation.DRAW_FLOOR)
			drawn = new Floor();
		else if (operation == Operation.DRAW_FOUNDATION)
			drawn = new Foundation();

		if (drawn != null)
			addHousePart(drawn);
		return drawn;
	}

	private void addHousePart(HousePart drawn) {
		housePartsNode.attachChild(drawn.getRoot());
		Scene.getInstance().add(drawn);
	}

	private void removeHousePart(HousePart drawn) {
		if (drawn == null)
			return;
		housePartsNode.detachChild(drawn.getRoot());
		Scene.getInstance().remove(drawn);
//		if (drawn instanceof Wall)
		drawn.delete();
	}

	public Operation getOperation() {
		return operation;
	}

	private void pick(int x, int y, Spatial target) {
		// Put together a pick ray
		final Vector2 pos = Vector2.fetchTempInstance().set(x, y);
		final Ray3 pickRay = Ray3.fetchTempInstance();
		canvas.getCanvasRenderer().getCamera().getPickRay(pos, false, pickRay);
		Vector2.releaseTempInstance(pos);

		// Do the pick
		PickingUtil.findPick(target, pickRay, pickResults);
		Ray3.releaseTempInstance(pickRay);
	}

	public PickedHousePart findMousePoint(int x, int y) {
		return findMousePoint(x, y, floor);
	}

	public PickedHousePart findMousePoint(int x, int y, Spatial target) {
		if (target == null)
			target = floor;
		pickResults.clear();
		pick(x, y, target);

		return getPickResult();
	}

	public PickedHousePart findMousePoint(int x, int y, Class<?> typeOfHousePart) {
		pickResults.clear();
		if (typeOfHousePart == null)
			pick(x, y, floor);
		else
			for (HousePart housePart : Scene.getInstance().getParts())
				if (typeOfHousePart.isInstance(housePart)) // && housePart != except)
					pick(x, y, housePart.getRoot());

		return getPickResult();
	}

	private PickedHousePart getPickResult() {
		PickedHousePart pickedHousePart = null;
		double polyDist = Double.MAX_VALUE;
		double pointDist = Double.MAX_VALUE;
		for (int i = 0; i < pickResults.getNumber(); i++) {
			final PickData pick = pickResults.getPickData(i);
			if (pick.getIntersectionRecord().getNumberOfIntersections() == 0)
				continue;
			Object obj = pick.getTargetMesh().getUserData();
			UserData userData = null;
			if (obj instanceof UserData)
				userData = (UserData) obj;
			Vector3 intersectionPoint = pick.getIntersectionRecord().getIntersectionPoint(0);
//			Vector3 intersectionPoint = root.getTransform().applyInverse(pick.getIntersectionRecord().getIntersectionPoint(0));
			PickedHousePart picked_i = new PickedHousePart(userData, intersectionPoint);
			double polyDist_i = pick.getClosestDistance();
			double pointDist_i = Double.MAX_VALUE;
			if (userData != null && polyDist_i - polyDist < 0.1) {
				for (Vector3 p : userData.getHousePart().getPoints()) {
					pointDist_i = p.distance(intersectionPoint);
					if (userData.getHousePart() == drawn)
						pointDist_i -= 0.1;
					if (pointDist_i < pointDist && (userData.getPointIndex() != -1 || pickedHousePart == null || pickedHousePart.getUserData().getPointIndex() == -1)
					// && (userData.getPointIndex() == -1 || pointDist < 0.5)
					) {
						pickedHousePart = picked_i;
						polyDist = polyDist_i;
						pointDist = pointDist_i;
					}
				}
			}
			if (pickedHousePart == null) {
				pickedHousePart = picked_i;
				polyDist = polyDist_i;
				pointDist = pointDist_i;
			}
		}
//		if (pickedHousePart.getUserData() == null)
//			System.out.println(pickedHousePart);
		return pickedHousePart;
	}

	private void selectHousePart(int x, int y, boolean edit) {
		PickedHousePart selectedMesh = findMousePoint(x, y, housePartsNode);
		UserData data = null;
		if (selectedMesh != null)
			data = selectedMesh.getUserData();

		if (data == null) {
			if (lastHoveredObject != null) {
				lastHoveredObject.hidePoints();
				lastHoveredObject = null;
			}
		} else if (edit && data.getPointIndex() != -1) {
			drawn = data.getHousePart();
			int pointIndex = data.getPointIndex();
			if (topView && drawn instanceof Wall)
				pointIndex -= 1;
			drawn.editPoint(pointIndex);
		} else {
			HousePart housePart = data.getHousePart();
			if (lastHoveredObject != null && lastHoveredObject != housePart) {
				lastHoveredObject.hidePoints();
				lastHoveredObject = null;
			}
			housePart.showPoints();
			lastHoveredObject = housePart;
			drawn = data.getHousePart();
		}
	}

	public void setLighting(boolean enable) {
		lightState.setEnabled(enable);
		root.updateWorldRenderStates(true);
	}

}
