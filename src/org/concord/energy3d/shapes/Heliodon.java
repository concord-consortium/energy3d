package org.concord.energy3d.shapes;

import java.nio.FloatBuffer;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.extension.effect.bloom.BloomRenderPass;
import com.ardor3d.framework.Canvas;
import com.ardor3d.input.ButtonState;
import com.ardor3d.input.Key;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.LogicalLayer;
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
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Transform;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.pass.BasicPassManager;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.BlendState;
import com.ardor3d.renderer.state.ClipState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.hint.LightCombineMode;
import com.ardor3d.scenegraph.hint.TransparencyType;
import com.ardor3d.scenegraph.shape.Cylinder;
import com.ardor3d.scenegraph.shape.Sphere;
import com.ardor3d.util.geom.BufferUtils;

public class Heliodon {
	private final Node root = new Node("Heliodon Root");
	private final Node sunRing = new Node("Sun Ring");
	private final Node sunRot = new Node("Sun Rot");
	private final Spatial sun = new Sphere("Sun", 20, 20, 0.3);
	private final DirectionalLight light;
	private final BloomRenderPass bloomRenderPass;
	private double tiltAngle = 23.45 / 180 * Math.PI; // 50.0 / 180 * Math.PI;
	private double offset = 0; // -Math.PI / 2;
	private double baseAngle = 0;
	private double sunAngle = 90;
	private double hourAngle;
	private double declinationAngle;
	private double observerLatitude;
	private final Line sunPath;
	private final Mesh sunRegion;
	private final Mesh line;
	private boolean sunGrabbed = false;
	private final PickResults pickResults;

	public Heliodon(final Node scene, final DirectionalLight light, final BasicPassManager passManager, final LogicalLayer logicalLayer) {
		this.light = light;
		
		this.pickResults = new PrimitivePickResults();
		this.pickResults.setCheckDistance(true);
		
		this.bloomRenderPass = new BloomRenderPass(SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera(), 4);
		passManager.add(bloomRenderPass);
		bloomRenderPass.add(sun);

		sunPath = new Line("Sun Path", BufferUtils.createVector3Buffer(100), null, null, null);
		sunPath.getMeshData().setIndexMode(IndexMode.LineStrip);
		sunPath.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		root.attachChild(sunPath);
		
		final FloatBuffer cbuf = BufferUtils.createColorBuffer(2);
		cbuf.put(1).put(0).put(0).put(0).put(0).put(0).put(1);
		line = new Line("Line", BufferUtils.createVector3Buffer(2), null, cbuf, null);
		line.getMeshData().setIndexMode(IndexMode.LineStrip);
		line.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		root.attachChild(line);		

		sunRegion = new Mesh("Sun Region");		
		sunRegion.getMeshData().setVertexBuffer(BufferUtils.createVector3Buffer(5040 / 3));
		sunRegion.getMeshData().setIndexMode(IndexMode.Quads);
		sunRegion.setDefaultColor(new ColorRGBA(1f, 1f, 0f, 0.5f));
		final BlendState blendState = new BlendState();
		blendState.setBlendEnabled(true);		
		sunRegion.setRenderState(blendState);		
		sunRegion.setRenderState(new WireframeState());
		sunRegion.getSceneHints().setTransparencyType(TransparencyType.TwoPass);
		sunRegion.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
		sunRegion.getSceneHints().setLightCombineMode(LightCombineMode.Off);		
		sunRegion.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);		
		root.attachChild(sunRegion);

		root.getSceneHints().setCullHint(CullHint.Always);
		final Cylinder cyl = new Cylinder("Curve", 10, 50, 5, 0.3);
		final Transform trans = new Transform();
		trans.setMatrix(new Matrix3().fromAngleAxis(Math.PI / 2, Vector3.UNIT_X));
		cyl.setDefaultColor(ColorRGBA.YELLOW);
		cyl.setTransform(trans);
//		sunRing.attachChild(cyl);
		// sunRing.setTranslation(0, offset, 0);
		// sunRing.setRotation(new Matrix3().fromAngleAxis(-tiltAngle, Vector3.UNIT_X));

		final Cylinder baseCyl = new Cylinder("Base", 10, 50, 5, 0.2);
		baseCyl.setTranslation(0, 0, 0.1);
		root.attachChild(baseCyl);

		sun.setTranslation(0, 0, 5);
		// sunRot.attachChild(sun);
		sunRing.attachChild(sunRot);
		root.attachChild(sunRing);
		root.attachChild(sun);
		draw();

		scene.attachChild(root);

		// reverseNormals(sun.getMeshData().getNormalBuffer());

		final MaterialState material = new MaterialState();
		material.setEmissive(ColorRGBA.WHITE);
		sun.setRenderState(material);

		final ClipState cs = new ClipState();
		cs.setEnableClipPlane(0, true);
		cs.setClipPlaneEquation(0, 0, 0, 1, 0);
		cyl.setRenderState(cs);
		sunPath.setRenderState(cs);
		sunRegion.setRenderState(cs);
		
		logicalLayer.registerTrigger(new InputTrigger(new MouseButtonPressedCondition(MouseButton.LEFT), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				final int x = inputStates.getCurrent().getMouseState().getX();
				final int y = inputStates.getCurrent().getMouseState().getY();
				final Ray3 pickRay = SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getPickRay(new Vector2(x, y), false, null);
				pickResults.clear();
				PickingUtil.findPick(sun, pickRay, pickResults);
				if (pickResults.getNumber() != 0)
					sunGrabbed = true;
				else
					sunGrabbed = false;
				SceneManager.getInstance().setMouseControlEnabled(!sunGrabbed);
			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new MouseButtonReleasedCondition(MouseButton.LEFT), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				sunGrabbed = false;
				SceneManager.getInstance().setMouseControlEnabled(true);
			}
		}));

		logicalLayer.registerTrigger(new InputTrigger(new MouseMovedCondition(), new TriggerAction() {
			public void perform(final Canvas source, final TwoInputStates inputStates, final double tpf) {
				if (!sunGrabbed)
					return;
				final int x = inputStates.getCurrent().getMouseState().getX();
				final int y = inputStates.getCurrent().getMouseState().getY();
				final Ray3 pickRay = SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getPickRay(new Vector2(x, y), false, null);

//				if (inputStates.getCurrent().getKeyboardState().isDown(Key.P)) {
//				final FloatBuffer buf1 = line.getMeshData().getVertexBuffer();
//				buf1.rewind();
//				buf1.put(pickRay.getOrigin().getXf()).put(pickRay.getOrigin().getYf()).put(pickRay.getOrigin().getZf());
//				System.out.println(pickRay.getOrigin());
////				buf1.put(1).put(-7).put(3);
//				final Vector3 pp = pickRay.getOrigin().add(pickRay.getDirection().multiply(10, null), null);
//				buf1.put(pp.getXf()).put(pp.getYf()).put(pp.getZf());
////				buf1.put(0).put(0).put(0);
//				
////				System.out.println("origin = " + (int)pickRay.getOrigin().getXf() + ", " + (int)pickRay.getOrigin().getYf() + ", " + (int)pickRay.getOrigin().getZf());
////				pp.set(pickRay.getDirection());
////				System.out.println("\tp2 = " + (int)pp.getXf() + ", " + (int)pp.getYf() + ", " + (int)pp.getZf());
////				System.out.println("\tdirection = " + (int)(pp.getXf()*10) + ", " + (int)(10*pp.getYf()) + ", " + (int)(10*pp.getZf()));
////				System.out.println(pickRay);
////				System.out.println(pp);
////				System.out.println(SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getModelViewMatrix());
//				}
				
//				final double d = pickRay.distanceSquared(sun.getTranslation(), null);
//				System.out.println("d2 = " + d);
				
				double smallestDistance = Double.MAX_VALUE;
				final Vector3 p = new Vector3();
				final Vector3 result = new Vector3();
				FloatBuffer buf = sunPath.getMeshData().getVertexBuffer();
				buf.rewind();
				while (buf.hasRemaining()) {
					p.set(buf.get(), buf.get(), buf.get());
					final double d = pickRay.distanceSquared(p, null);
					if (d < smallestDistance) {
						smallestDistance = d;
						result.set(p);
					}
				}
				int rowCounter = 0;
				int resultRow = 0;
				if (smallestDistance > 0.1) {
					buf = sunRegion.getMeshData().getVertexBuffer();
					buf.rewind();
					final double r = 5.0 / 2.0;
					final Vector3 prev = new Vector3();
					while (buf.hasRemaining()) {
						p.set(buf.get(), buf.get(), buf.get());
						final double d = pickRay.distanceSquared(p, null);
						if (d < smallestDistance) {
							smallestDistance = d;
							result.set(p);
							resultRow = rowCounter;
						}
						if (prev.distance(p) > r)
							rowCounter++;
						prev.set(p);
					}
				}
				sun.setTranslation(result);
				System.out.println("declinationAngle = " + computeDeclinationAngle(result.getX(), result.getY(), result.getZ()));
				declinationAngle = computeDeclinationAngle(result.getX(), result.getY(), result.getZ());
				if (!Double.isNaN(declinationAngle))
				drawSunPath();
			}
		}));		
	}

	public Node getRoot() {
		return root;
	}

	public double getTiltAngle() {
		return tiltAngle;
	}

	public void setTiltAngle(final double tiltAngle) {
		this.tiltAngle = tiltAngle;
	}

	public double getOffset() {
		return offset;
	}

	public double getBaseAngle() {
		return baseAngle;
	}

	public void setBaseAngle(final double baseAngle) {
		this.baseAngle = baseAngle % 360;
		root.setRotation(new Matrix3().fromAngleAxis(baseAngle * Math.PI / 180, Vector3.UNIT_Z));
		drawSun();

	}

	public double getSunAngle() {
		return sunAngle;
	}

	public void setSunAngle(final double sunAngle) {
		this.sunAngle = sunAngle;
		sunRot.setRotation(new Matrix3().fromAngleAxis((-90 + sunAngle) * Math.PI / 180, Vector3.UNIT_Y));
		drawSun();
	}

	public double getHourAngle() {
		return hourAngle;
	}

	public void setHourAngle(double hourAngle) {
		this.hourAngle = toPlusMinusPIRange(hourAngle, -Math.PI, Math.PI);
		draw();
	}

	public double getDeclinationAngle() {
		return declinationAngle;
	}

	public void setDeclinationAngle(double declinationAngle) {
		this.declinationAngle = toPlusMinusPIRange(declinationAngle, -tiltAngle, tiltAngle);
		draw();
	}

	public double getObserverLatitude() {
		return observerLatitude;
	}

	public void setObserverLatitude(double observerLatitude) {
		this.observerLatitude = toPlusMinusPIRange(observerLatitude, -Math.PI / 2.0, Math.PI / 2.0);
		draw();
	}

	public void setVisible(final boolean visible) {
		root.getSceneHints().setCullHint(visible ? CullHint.Inherit : CullHint.Always);
		bloomRenderPass.setEnabled(visible);
		if (visible)
			updateSize();
	}

	public void updateSize() {
		Scene.getRoot().updateWorldBound(true);
		final BoundingVolume bounds = Scene.getRoot().getWorldBound();
		if (bounds == null)
			root.setScale(1);
		else {
			final double scale = (Util.findBoundLength(bounds) / 2.0 + bounds.getCenter().length()) / 5.0;
			System.out.println("Heliodon scale = " + scale);
			root.setScale(scale);
		}
	}

	private Vector3 computeSunLocation(final double hourAngle, final double declinationAngle, final double observerLatitude) {
		final double altitudeAngle = Math.asin(Math.sin(declinationAngle) * Math.sin(observerLatitude) + Math.cos(declinationAngle) * Math.cos(hourAngle) * Math.cos(observerLatitude));
		final double x_azm = Math.sin(hourAngle) * Math.cos(declinationAngle);
		final double y_azm = (-(Math.cos(hourAngle)) * Math.cos(declinationAngle) * Math.sin(observerLatitude)) + (Math.cos(observerLatitude) * Math.sin(declinationAngle));
		final double azimuthAngle = Math.atan2(y_azm, x_azm);

		final double r = 5;
		final double x = r * Math.cos(azimuthAngle) * Math.sin(Math.PI / 2 - altitudeAngle);
		final double y = r * Math.sin(azimuthAngle) * Math.sin(Math.PI / 2 - altitudeAngle);
		final double z = r * Math.cos(Math.PI / 2 - altitudeAngle);

		// System.out.println("houseAngle = " + toDegree(hourAngle) + ", declinationAngle = " + toDegree(declinationAngle) + ", observerLatitude = " + toDegree(observerLatitude) + " --> altitudeAngle = " + toDegree(altitudeAngle) + ", azimuthAngle = " + toDegree(azimuthAngle) + " (" + x + ", " + y + ", " + z + ")");

		return new Vector3(x, y, z);
	}
	
	private double computeDeclinationAngle(final double x, final double y, final double z) {
		final double hourAngle = 1;
		
		final double r = 5;
		final double altitudeAngle = -(Math.acos(z / r) - Math.PI / 2.0); 
		final double azimuthAngle =  Math.asin(y / r / Math.sin(Math.PI / 2.0 - altitudeAngle));
		return Math.acos(Math.sin(azimuthAngle) / Math.sin(hourAngle) * Math.cos(altitudeAngle));
			
//		final double altitudeAngle = Math.asin(Math.sin(declinationAngle) * Math.sin(observerLatitude) + Math.cos(declinationAngle) * Math.cos(hourAngle) * Math.cos(observerLatitude));
//		final double x_azm = Math.sin(hourAngle) * Math.cos(declinationAngle);
//		final double y_azm = (-(Math.cos(hourAngle)) * Math.cos(declinationAngle) * Math.sin(observerLatitude)) + (Math.cos(observerLatitude) * Math.sin(declinationAngle));
//		final double azimuthAngle = Math.atan2(y_azm, x_azm);
//		
//		final double azimuthAngle = Math.asin(Math.sin(hourAngle) * Math.cos(declinationAngle) / Math.cos(altitudeAngle));
//
//		final double x = r * Math.cos(azimuthAngle) * Math.sin(Math.PI / 2.0 - altitudeAngle);
//		final double y = r * Math.sin(azimuthAngle) * Math.sin(Math.PI / 2.0 - altitudeAngle);
//		final double z = r * Math.cos(Math.PI / 2.0 - altitudeAngle);
//
//		// System.out.println("houseAngle = " + toDegree(hourAngle) + ", declinationAngle = " + toDegree(declinationAngle) + ", observerLatitude = " + toDegree(observerLatitude) + " --> altitudeAngle = " + toDegree(altitudeAngle) + ", azimuthAngle = " + toDegree(azimuthAngle) + " (" + x + ", " + y + ", " + z + ")");
//
//		return new Vector3(x, y, z);
	}	

//	private int toDegree(final double radian) {
//		return (int) (radian / Math.PI * 180);
//	}

	private double toPlusMinusPIRange(final double radian, double min, double max) {
		final double twoPI = Math.PI * 2.0;
		double result = radian - (int) (radian / twoPI) * twoPI;
		if (Math.abs(result) > Math.PI)
			result = -Math.signum(result) * (twoPI - Math.abs(result));
		if (result < min)
			result = min;
		else if (result > max)
			result = max;
		return result;
	}

	private void drawSunRegion() {
		final FloatBuffer buf = sunRegion.getMeshData().getVertexBuffer();
		buf.limit(buf.capacity());
		buf.rewind();
		final double declinationStep = 2.0 * tiltAngle / 10.0;
		final double hourStep = 2.0 * Math.PI / 70.0;
		int limit = 0;
		for (double hourAngle = -Math.PI; hourAngle < Math.PI - hourStep / 2.0; hourAngle += hourStep) {
			for (double declinationAngle = -tiltAngle; declinationAngle < tiltAngle - declinationStep / 2.0; declinationAngle += declinationStep) {
				double hourAngle2 = hourAngle + hourStep;
				double declinationAngle2 = declinationAngle + declinationStep;
				if (hourAngle2 > Math.PI)
					hourAngle2 = Math.PI;
				if (declinationAngle2 > tiltAngle)
					declinationAngle2 = tiltAngle;
				final Vector3 v1 = computeSunLocation(hourAngle, declinationAngle, observerLatitude);
				final Vector3 v2 = computeSunLocation(hourAngle2, declinationAngle, observerLatitude);
				final Vector3 v3 = computeSunLocation(hourAngle2, declinationAngle2, observerLatitude);
				final Vector3 v4 = computeSunLocation(hourAngle, declinationAngle2, observerLatitude);
				if (v1.getZ() >= 0 || v2.getZ() >= 0 || v3.getZ() >= 0 || v4.getZ() >= 0) {
					buf.put(v1.getXf()).put(v1.getYf()).put(v1.getZf()).put(v2.getXf()).put(v2.getYf()).put(v2.getZf()).put(v3.getXf()).put(v3.getYf()).put(v3.getZf()).put(v4.getXf()).put(v4.getYf()).put(v4.getZf());
					limit += 12;
				}
			}
		}
		buf.limit(limit);
		System.out.println("limit = " + limit + " / " + buf.capacity());
	}

	private void drawSunPath() {
		final FloatBuffer buf = sunPath.getMeshData().getVertexBuffer();
		buf.limit(buf.capacity());
		buf.rewind();
		final double step = 2.0 * Math.PI / (buf.capacity() / 3.0 - 1.0);
		int limit = 0;
		for (double hourAngle = -Math.PI; hourAngle < Math.PI + step / 2.0; hourAngle += step) {
			final Vector3 v = computeSunLocation(hourAngle, declinationAngle, observerLatitude);
			if (v.getZ() > 0) {
				buf.put(v.getXf()).put(v.getYf()).put(v.getZf());
				limit += 3;
			}
		}
		buf.limit(limit);
		sunPath.updateModelBound();
	}

	private void drawSun() {
		final Vector3 sunLocation = computeSunLocation(hourAngle, declinationAngle, observerLatitude);
		if (!Double.isNaN(sunLocation.length())) {
			sun.setTranslation(sunLocation);
			light.setDirection(sunLocation.negate(null));
		} else
			new RuntimeException().printStackTrace();
	}

	private void draw() {
		drawSunRegion();
		drawSunPath();
		drawSun();
	}

	public void updateBloom() {
		bloomRenderPass.markNeedsRefresh();
		
	}
}
