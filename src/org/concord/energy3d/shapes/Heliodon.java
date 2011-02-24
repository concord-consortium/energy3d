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
import com.ardor3d.intersection.IntersectionRecord;
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
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.pass.BasicPassManager;
import com.ardor3d.renderer.pass.RenderPass;
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
	private final Spatial sun = new Sphere("Sun", 20, 20, 0.03);
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
		// computeDeclinationAngle(0,0,5);
		this.light = light;

		this.pickResults = new PrimitivePickResults();
		this.pickResults.setCheckDistance(true);

		this.bloomRenderPass = new BloomRenderPass(SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera(), 4);
		passManager.add(bloomRenderPass);
//		bloomRenderPass.add(sun);

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
//		sunRegion.setRenderState(new WireframeState());
		sunRegion.getSceneHints().setTransparencyType(TransparencyType.TwoPass);
		sunRegion.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
		sunRegion.getSceneHints().setLightCombineMode(LightCombineMode.Off);
		sunRegion.getSceneHints().setRenderBucketType(RenderBucketType.Transparent);
		root.attachChild(sunRegion);
		
		final RenderPass wireframePass = new RenderPass();
		wireframePass.setPassState(new WireframeState());
		wireframePass.add(sunRegion);
		passManager.add(wireframePass);

		root.getSceneHints().setCullHint(CullHint.Always);
		final Cylinder cyl = new Cylinder("Curve", 10, 50, 5, 0.3);
		final Transform trans = new Transform();
		trans.setMatrix(new Matrix3().fromAngleAxis(Math.PI / 2, Vector3.UNIT_X));
		cyl.setDefaultColor(ColorRGBA.YELLOW);
		cyl.setTransform(trans);
		// sunRing.attachChild(cyl);
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
				final Camera camera = SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera();
				final Ray3 pickRay = camera.getPickRay(new Vector2(x, y), false, null);
//				final ReadOnlyVector3 cameraLocation = camera.getLocation();	
				
//				pickRay.setOrigin(new Vector3(0, 0, 10));
//				pickRay.setDirection(new Vector3(0, 0, -1));
				
				 final FloatBuffer buf1 = line.getMeshData().getVertexBuffer();
				 buf1.rewind();
				 buf1.put(pickRay.getOrigin().getXf()).put(pickRay.getOrigin().getYf()).put(pickRay.getOrigin().getZf());
				 final Vector3 pp = pickRay.getOrigin().add(pickRay.getDirection().multiply(20, null), null);
				 buf1.put(pp.getXf()).put(pp.getYf()).put(pp.getZf());
				
				pickResults.clear();
				PickingUtil.findPick(sunRegion, pickRay, pickResults);
				final Vector3 intersectionPoint;
				if (pickResults.getNumber() > 0) {
					final IntersectionRecord intersectionRecord = pickResults.getPickData(0).getIntersectionRecord();
					System.out.println(intersectionRecord.getNumberOfIntersections());
					for (int i=0; i<intersectionRecord.getNumberOfIntersections(); i++)
						System.out.println(intersectionRecord.getIntersectionPoint(i));
					
					intersectionPoint = intersectionRecord.getIntersectionPoint(intersectionRecord.getFurthestIntersection());
//					intersectionPoint = intersectionRecord.getIntersectionPoint(0);
				} else
					intersectionPoint = null;

				double smallestDistance = Double.MAX_VALUE;
				final Vector3 result = new Vector3();
				final Vector3 p = new Vector3();
				FloatBuffer buf = sunPath.getMeshData().getVertexBuffer();
//				buf.rewind();
//				while (buf.hasRemaining()) {
//					p.set(buf.get(), buf.get(), buf.get());
//					final double d;
//					if (intersectionPoint != null)
//						d = intersectionPoint.distanceSquared(p);
//					else
//						d = pickRay.distanceSquared(p, null);
//					if (d < smallestDistance) {
//						smallestDistance = d;
//						result.set(p);
//					}
//				}
				int rowCounter = 0;
				int resultRow = -1;
				if (smallestDistance > 0.1) {
					buf = sunRegion.getMeshData().getVertexBuffer();
					buf.rewind();
					final double r = 5.0 / 2.0;
					final Vector3 prev = new Vector3();
					int vertexCounter = 0;
					while (buf.hasRemaining()) {
						p.set(buf.get(), buf.get(), buf.get());
//						final double d = pickRay.distanceSquared(p, null);
						final double d;
						if (intersectionPoint != null)
							d = intersectionPoint.distanceSquared(p);
						else
							d = pickRay.distanceSquared(p, null);						
						if (d < smallestDistance) {
							smallestDistance = d;
							result.set(p);
							resultRow = vertexCounter >= 2 ? rowCounter + 1 : rowCounter;
						}
						if (prev.distance(p) > r && prev.lengthSquared() != 0)
							rowCounter++;
						prev.set(p);
						vertexCounter = (vertexCounter + 1) % 4;
					}
				}
				rowCounter++;
//				sun.setTranslation(result);
				if (intersectionPoint != null)
				sun.setTranslation(intersectionPoint);
				System.out.println(smallestDistance + "\t" + intersectionPoint);
				if (resultRow != -1)
					declinationAngle = -tiltAngle + (2.0 * tiltAngle * resultRow / rowCounter);
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

//		System.out.println("houseAngle = " + toDegree(hourAngle) + ", declinationAngle = " + toDegree(declinationAngle) + ", observerLatitude = " + toDegree(observerLatitude) + " --> altitudeAngle = " + toDegree(altitudeAngle) + ", azimuthAngle = " + toDegree(azimuthAngle) + " (" + x + ", " + y + ", " + z + ")");

		return new Vector3(x, y, z);
	}

	// private double computeDeclinationAngle(double x, double y, double z) {
	// final double hourAngle = 3.0368729;
	// x = 0.4111789607654713;
	// y = 1.9897431565380506;
	// z = -4.5685724283668545;
	//
	// final double r = 5;
	// final double altitudeAngle = -(Math.acos(z / r) - Math.PI / 2.0);
	// double azimuthAngle = Math.asin(y / r / Math.sin(Math.PI / 2.0 - altitudeAngle));
	// // if (Double.isNaN(azimuthAngle))
	// // azimuthAngle = 0;
	// // double declinationAngle = Math.acos(Math.sin(azimuthAngle) / Math.sin(hourAngle) * Math.cos(altitudeAngle));
	// // double declinationAngle = Math.asin(-Math.cos(azimuthAngle)*Math.cos(altitudeAngle)*Math.cos(observerLatitude)-Math.sin(altitudeAngle)*Math.sin(observerLatitude));
	//
	// Math.tan(azimuthAngle) = y_azm / x_azm;
	// Math.cos(declinationAngle) = x_azm / Math.sin(hourAngle);
	//
	// Math.cos(declinationAngle) = (-(Math.cos(hourAngle)) * Math.cos(declinationAngle) * Math.sin(observerLatitude)) + (Math.cos(observerLatitude) * Math.sin(declinationAngle)) / Math.tan(azimuthAngle) / Math.sin(hourAngle);
	//
	// Math.tan(azimuthAngle) * x_azm = (-(Math.cos(hourAngle)) * Math.cos(declinationAngle) * Math.sin(observerLatitude)) + (Math.cos(observerLatitude) * Math.sin(declinationAngle));
	//
	// final double x_azm = Math.sin(hourAngle) * Math.cos(declinationAngle);
	// final double y_azm = (-(Math.cos(hourAngle)) * Math.cos(declinationAngle) * Math.sin(observerLatitude)) + (Math.cos(observerLatitude) * Math.sin(declinationAngle));
	// final double azimuthAngle = Math.atan2(y_azm, x_azm);
	//
	// System.out.println("Altitude = " + toDegree(altitudeAngle) + "\tAzimuth = " + toDegree(azimuthAngle) + "\tDeclination = " + toDegree(declinationAngle));
	// if (Double.isNaN(declinationAngle))
	// declinationAngle = 0;
	// return declinationAngle;
	//
	// // final double altitudeAngle = Math.asin(Math.sin(declinationAngle) * Math.sin(observerLatitude) + Math.cos(declinationAngle) * Math.cos(hourAngle) * Math.cos(observerLatitude));
	// // final double x_azm = Math.sin(hourAngle) * Math.cos(declinationAngle);
	// // final double y_azm = (-(Math.cos(hourAngle)) * Math.cos(declinationAngle) * Math.sin(observerLatitude)) + (Math.cos(observerLatitude) * Math.sin(declinationAngle));
	// // final double azimuthAngle = Math.atan2(y_azm, x_azm);
	// //
	// // final double azimuthAngle = Math.asin(Math.sin(hourAngle) * Math.cos(declinationAngle) / Math.cos(altitudeAngle));
	// //
	// // final double x = r * Math.cos(azimuthAngle) * Math.sin(Math.PI / 2.0 - altitudeAngle);
	// // final double y = r * Math.sin(azimuthAngle) * Math.sin(Math.PI / 2.0 - altitudeAngle);
	// // final double z = r * Math.cos(Math.PI / 2.0 - altitudeAngle);
	// //
	// // // System.out.println("houseAngle = " + toDegree(hourAngle) + ", declinationAngle = " + toDegree(declinationAngle) + ", observerLatitude = " + toDegree(observerLatitude) + " --> altitudeAngle = " + toDegree(altitudeAngle) + ", azimuthAngle = " + toDegree(azimuthAngle) + " (" + x + ", " + y + ", " + z + ")");
	// //
	// // return new Vector3(x, y, z);
	// }

	private int toDegree(final double radian) {
		return (int) (radian / Math.PI * 180);
	}

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
//		final FloatBuffer buf = sunRegion.getMeshData().getVertexBuffer();
//		
//		buf.rewind();
//		buf.put(-4.58242f).put(-1.9897431f).put(-0.2057967f).put(-4.58242f).put(-1.9897431f).put(0.2057967f).put(-4.729603f).put(-1.6080236f).put(0.21240671f).put(-4.729603f).put(-1.6080236f).put(-0.21240671f);
//		buf.put(-4.58242f).put(-1.9897431f).put(0.2057967f).put(-4.5455246f).put(-1.9897431f).put(0.61573315f).put(-4.691523f).put(-1.6080236f).put(0.63550997f).put(-4.729603f).put(-1.6080236f).put(0.21240671f);
//		buf.put(-4.5455246f).put(-1.9897431f).put(0.61573315f).put(-4.472032f).put(-1.9897431f).put(1.0207121f).put(-4.6156697f).put(-1.6080236f).put(1.0534965f).put(-4.691523f).put(-1.6080236f).put(0.63550997f);
//		buf.put(-4.472032f).put(-1.9897431f).put(1.0207121f).put(-4.3625326f).put(-1.9897431f).put(1.4174728f).put(-4.5026536f).put(-1.6080236f).put(1.4630008f).put(-4.6156697f).put(-1.6080236f).put(1.0534965f);
//		buf.put(-4.3625326f).put(-1.9897431f).put(1.4174728f).put(-4.2179093f).put(-1.9897431f).put(1.8028209f).put(-4.353385f).put(-1.6080236f).put(1.860726f).put(-4.5026536f).put(-1.6080236f).put(1.4630008f);
//		buf.put(-4.2179093f).put(-1.9897431f).put(1.8028209f).put(-4.0393257f).put(-1.9897431f).put(2.1736538f).put(-4.169065f).put(-1.6080236f).put(2.2434697f).put(-4.353385f).put(-1.6080236f).put(1.860726f);
//		buf.put(-4.0393257f).put(-1.9897431f).put(2.1736538f).put(-3.8282197f).put(-1.9897431f).put(2.5269856f).put(-3.9511786f).put(-1.6080236f).put(2.6081502f).put(-4.169065f).put(-1.6080236f).put(2.2434697f);
//		buf.put(-3.8282197f).put(-1.9897431f).put(2.5269856f).put(-3.586291f).put(-1.9897431f).put(2.8599718f).put(-3.7014797f).put(-1.6080236f).put(2.9518313f).put(-3.9511786f).put(-1.6080236f).put(2.6081502f);
//		buf.put(-3.586291f).put(-1.9897431f).put(2.8599718f).put(-3.3154879f).put(-1.9897431f).put(3.169931f).put(-3.4219785f).put(-1.6080236f).put(3.2717464f).put(-3.7014797f).put(-1.6080236f).put(2.9518313f);
//		buf.put(-3.3154879f).put(-1.9897431f).put(3.169931f).put(-3.0179904f).put(-1.9897431f).put(3.4543679f).put(-3.1149254f).put(-1.6080236f).put(3.565319f).put(-3.4219785f).put(-1.6080236f).put(3.2717464f);
//		buf.put(-3.0179904f).put(-1.9897431f).put(3.4543679f).put(-2.6961937f).put(-1.9897431f).put(3.710992f).put(-2.7827928f).put(-1.6080236f).put(3.830186f).put(-3.1149254f).put(-1.6080236f).put(3.565319f);
//		buf.put(-2.6961937f).put(-1.9897431f).put(3.710992f).put(-2.3526888f).put(-1.9897431f).put(3.9377377f).put(-2.428255f).put(-1.6080236f).put(4.064214f).put(-2.7827928f).put(-1.6080236f).put(3.830186f);
//		buf.put(-2.3526888f).put(-1.9897431f).put(3.9377377f).put(-1.9902414f).put(-1.9897431f).put(4.1327786f).put(-2.054166f).put(-1.6080236f).put(4.26552f).put(-2.428255f).put(-1.6080236f).put(4.064214f);
//		buf.put(-1.9902414f).put(-1.9897431f).put(4.1327786f).put(-1.6117698f).put(-1.9897431f).put(4.294545f).put(-1.6635385f).put(-1.6080236f).put(4.4324822f).put(-2.054166f).put(-1.6080236f).put(4.26552f);
//		buf.put(-1.6117698f).put(-1.9897431f).put(4.294545f).put(-1.2203213f).put(-1.9897431f).put(4.421735f).put(-1.2595168f).put(-1.6080236f).put(4.563757f).put(-1.6635385f).put(-1.6080236f).put(4.4324822f);
//		buf.put(-1.2203213f).put(-1.9897431f).put(4.421735f).put(-0.81904733f).put(-1.9897431f).put(4.513323f).put(-0.84535444f).put(-1.6080236f).put(4.6582866f).put(-1.2595168f).put(-1.6080236f).put(4.563757f);
//		buf.put(-0.81904733f).put(-1.9897431f).put(4.513323f).put(-0.41117895f).put(-1.9897431f).put(4.5685725f).put(-0.42438567f).put(-1.6080236f).put(4.715311f).put(-0.84535444f).put(-1.6080236f).put(4.6582866f);
//		buf.put(-0.41117895f).put(-1.9897431f).put(4.5685725f).put(1.2183662E-16f).put(-1.9897431f).put(4.5870385f).put(9.8463054E-17f).put(-1.6080236f).put(4.73437f).put(-0.42438567f).put(-1.6080236f).put(4.715311f);
//		buf.put(1.2183662E-16f).put(-1.9897431f).put(4.5870385f).put(0.41117895f).put(-1.9897431f).put(4.5685725f).put(0.42438567f).put(-1.6080236f).put(4.715311f).put(9.8463054E-17f).put(-1.6080236f).put(4.73437f);
//		buf.put(0.41117895f).put(-1.9897431f).put(4.5685725f).put(0.81904733f).put(-1.9897431f).put(4.513323f).put(0.84535444f).put(-1.6080236f).put(4.6582866f).put(0.42438567f).put(-1.6080236f).put(4.715311f);
//		buf.put(0.81904733f).put(-1.9897431f).put(4.513323f).put(1.2203213f).put(-1.9897431f).put(4.421735f).put(1.2595168f).put(-1.6080236f).put(4.563757f).put(0.84535444f).put(-1.6080236f).put(4.6582866f);
//		buf.put(1.2203213f).put(-1.9897431f).put(4.421735f).put(1.6117698f).put(-1.9897431f).put(4.294545f).put(1.6635385f).put(-1.6080236f).put(4.4324822f).put(1.2595168f).put(-1.6080236f).put(4.563757f);
//		buf.put(1.6117698f).put(-1.9897431f).put(4.294545f).put(1.9902414f).put(-1.9897431f).put(4.1327786f).put(2.054166f).put(-1.6080236f).put(4.26552f).put(1.6635385f).put(-1.6080236f).put(4.4324822f);
//		buf.put(1.9902414f).put(-1.9897431f).put(4.1327786f).put(2.3526888f).put(-1.9897431f).put(3.9377377f).put(2.428255f).put(-1.6080236f).put(4.064214f).put(2.054166f).put(-1.6080236f).put(4.26552f);
//		buf.put(2.3526888f).put(-1.9897431f).put(3.9377377f).put(2.6961937f).put(-1.9897431f).put(3.710992f).put(2.7827928f).put(-1.6080236f).put(3.830186f).put(2.428255f).put(-1.6080236f).put(4.064214f);
//		buf.put(2.6961937f).put(-1.9897431f).put(3.710992f).put(3.0179904f).put(-1.9897431f).put(3.4543679f).put(3.1149254f).put(-1.6080236f).put(3.565319f).put(2.7827928f).put(-1.6080236f).put(3.830186f);
//		buf.put(3.0179904f).put(-1.9897431f).put(3.4543679f).put(3.3154879f).put(-1.9897431f).put(3.169931f).put(3.4219785f).put(-1.6080236f).put(3.2717464f).put(3.1149254f).put(-1.6080236f).put(3.565319f);
//		buf.put(3.3154879f).put(-1.9897431f).put(3.169931f).put(3.586291f).put(-1.9897431f).put(2.8599718f).put(3.7014797f).put(-1.6080236f).put(2.9518313f).put(3.4219785f).put(-1.6080236f).put(3.2717464f);
//		buf.put(3.586291f).put(-1.9897431f).put(2.8599718f).put(3.8282197f).put(-1.9897431f).put(2.5269856f).put(3.9511786f).put(-1.6080236f).put(2.6081502f).put(3.7014797f).put(-1.6080236f).put(2.9518313f);
//		buf.put(3.8282197f).put(-1.9897431f).put(2.5269856f).put(4.0393257f).put(-1.9897431f).put(2.1736538f).put(4.169065f).put(-1.6080236f).put(2.2434697f).put(3.9511786f).put(-1.6080236f).put(2.6081502f);
//		buf.put(4.0393257f).put(-1.9897431f).put(2.1736538f).put(4.2179093f).put(-1.9897431f).put(1.8028209f).put(4.353385f).put(-1.6080236f).put(1.860726f).put(4.169065f).put(-1.6080236f).put(2.2434697f);
//		buf.put(4.2179093f).put(-1.9897431f).put(1.8028209f).put(4.3625326f).put(-1.9897431f).put(1.4174728f).put(4.5026536f).put(-1.6080236f).put(1.4630008f).put(4.353385f).put(-1.6080236f).put(1.860726f);
//		buf.put(4.3625326f).put(-1.9897431f).put(1.4174728f).put(4.472032f).put(-1.9897431f).put(1.0207121f).put(4.6156697f).put(-1.6080236f).put(1.0534965f).put(4.5026536f).put(-1.6080236f).put(1.4630008f);
//		buf.put(4.472032f).put(-1.9897431f).put(1.0207121f).put(4.5455246f).put(-1.9897431f).put(0.61573315f).put(4.691523f).put(-1.6080236f).put(0.63550997f).put(4.6156697f).put(-1.6080236f).put(1.0534965f);
//		buf.put(4.5455246f).put(-1.9897431f).put(0.61573315f).put(4.58242f).put(-1.9897431f).put(0.2057967f).put(4.729603f).put(-1.6080236f).put(0.21240671f).put(4.691523f).put(-1.6080236f).put(0.63550997f);
//		buf.put(4.58242f).put(-1.9897431f).put(0.2057967f).put(4.58242f).put(-1.9897431f).put(-0.2057967f).put(4.729603f).put(-1.6080236f).put(-0.21240671f).put(4.729603f).put(-1.6080236f).put(0.21240671f);
//		buf.put(-4.729603f).put(-1.6080236f).put(-0.21240671f).put(-4.729603f).put(-1.6080236f).put(0.21240671f).put(-4.8451138f).put(-1.2155358f).put(0.2175943f).put(-4.8451138f).put(-1.2155358f).put(-0.2175943f);
//		buf.put(-4.729603f).put(-1.6080236f).put(0.21240671f).put(-4.691523f).put(-1.6080236f).put(0.63550997f).put(-4.8061037f).put(-1.2155358f).put(0.65103096f).put(-4.8451138f).put(-1.2155358f).put(0.2175943f);
//		buf.put(-4.691523f).put(-1.6080236f).put(0.63550997f).put(-4.6156697f).put(-1.6080236f).put(1.0534965f).put(-4.728398f).put(-1.2155358f).put(1.0792259f).put(-4.8061037f).put(-1.2155358f).put(0.65103096f);
//		buf.put(-4.6156697f).put(-1.6080236f).put(1.0534965f).put(-4.5026536f).put(-1.6080236f).put(1.4630008f).put(-4.6126213f).put(-1.2155358f).put(1.4987316f).put(-4.728398f).put(-1.2155358f).put(1.0792259f);
//		buf.put(-4.5026536f).put(-1.6080236f).put(1.4630008f).put(-4.353385f).put(-1.6080236f).put(1.860726f).put(-4.4597073f).put(-1.2155358f).put(1.9061702f).put(-4.6126213f).put(-1.2155358f).put(1.4987316f);
//		buf.put(-4.353385f).put(-1.6080236f).put(1.860726f).put(-4.169065f).put(-1.6080236f).put(2.2434697f).put(-4.270886f).put(-1.2155358f).put(2.2982616f).put(-4.4597073f).put(-1.2155358f).put(1.9061702f);
//		buf.put(-4.169065f).put(-1.6080236f).put(2.2434697f).put(-3.9511786f).put(-1.6080236f).put(2.6081502f).put(-4.047678f).put(-1.2155358f).put(2.6718488f).put(-4.270886f).put(-1.2155358f).put(2.2982616f);
//		buf.put(-3.9511786f).put(-1.6080236f).put(2.6081502f).put(-3.7014797f).put(-1.6080236f).put(2.9518313f).put(-3.7918806f).put(-1.2155358f).put(3.0239239f).put(-4.047678f).put(-1.2155358f).put(2.6718488f);
//		buf.put(-3.7014797f).put(-1.6080236f).put(2.9518313f).put(-3.4219785f).put(-1.6080236f).put(3.2717464f).put(-3.505553f).put(-1.2155358f).put(3.351652f).put(-3.7918806f).put(-1.2155358f).put(3.0239239f);
//		buf.put(-3.4219785f).put(-1.6080236f).put(3.2717464f).put(-3.1149254f).put(-1.6080236f).put(3.565319f).put(-3.191001f).put(-1.2155358f).put(3.6523945f).put(-3.505553f).put(-1.2155358f).put(3.351652f);
//		buf.put(-3.1149254f).put(-1.6080236f).put(3.565319f).put(-2.7827928f).put(-1.6080236f).put(3.830186f).put(-2.850757f).put(-1.2155358f).put(3.9237301f).put(-3.191001f).put(-1.2155358f).put(3.6523945f);
//		buf.put(-2.7827928f).put(-1.6080236f).put(3.830186f).put(-2.428255f).put(-1.6080236f).put(4.064214f).put(-2.48756f).put(-1.2155358f).put(4.163474f).put(-2.850757f).put(-1.2155358f).put(3.9237301f);
//		buf.put(-2.428255f).put(-1.6080236f).put(4.064214f).put(-2.054166f).put(-1.6080236f).put(4.26552f).put(-2.1043348f).put(-1.2155358f).put(4.3696966f).put(-2.48756f).put(-1.2155358f).put(4.163474f);
//		buf.put(-2.054166f).put(-1.6080236f).put(4.26552f).put(-1.6635385f).put(-1.6080236f).put(4.4324822f).put(-1.7041669f).put(-1.2155358f).put(4.5407367f).put(-2.1043348f).put(-1.2155358f).put(4.3696966f);
//		buf.put(-1.6635385f).put(-1.6080236f).put(4.4324822f).put(-1.2595168f).put(-1.6080236f).put(4.563757f).put(-1.290278f).put(-1.2155358f).put(4.675217f).put(-1.7041669f).put(-1.2155358f).put(4.5407367f);
//		buf.put(-1.2595168f).put(-1.6080236f).put(4.563757f).put(-0.84535444f).put(-1.6080236f).put(4.6582866f).put(-0.8660004f).put(-1.2155358f).put(4.7720556f).put(-1.290278f).put(-1.2155358f).put(4.675217f);
//		buf.put(-0.84535444f).put(-1.6080236f).put(4.6582866f).put(-0.42438567f).put(-1.6080236f).put(4.715311f).put(-0.4347504f).put(-1.2155358f).put(4.8304725f).put(-0.8660004f).put(-1.2155358f).put(4.7720556f);
//		buf.put(-0.42438567f).put(-1.6080236f).put(4.715311f).put(9.8463054E-17f).put(-1.6080236f).put(4.73437f).put(7.44301E-17f).put(-1.2155358f).put(4.849997f).put(-0.4347504f).put(-1.2155358f).put(4.8304725f);
//		buf.put(9.8463054E-17f).put(-1.6080236f).put(4.73437f).put(0.42438567f).put(-1.6080236f).put(4.715311f).put(0.4347504f).put(-1.2155358f).put(4.8304725f).put(7.44301E-17f).put(-1.2155358f).put(4.849997f);
//		buf.put(0.42438567f).put(-1.6080236f).put(4.715311f).put(0.84535444f).put(-1.6080236f).put(4.6582866f).put(0.8660004f).put(-1.2155358f).put(4.7720556f).put(0.4347504f).put(-1.2155358f).put(4.8304725f);
//		buf.put(0.84535444f).put(-1.6080236f).put(4.6582866f).put(1.2595168f).put(-1.6080236f).put(4.563757f).put(1.290278f).put(-1.2155358f).put(4.675217f).put(0.8660004f).put(-1.2155358f).put(4.7720556f);
//		buf.put(1.2595168f).put(-1.6080236f).put(4.563757f).put(1.6635385f).put(-1.6080236f).put(4.4324822f).put(1.7041669f).put(-1.2155358f).put(4.5407367f).put(1.290278f).put(-1.2155358f).put(4.675217f);
//		buf.put(1.6635385f).put(-1.6080236f).put(4.4324822f).put(2.054166f).put(-1.6080236f).put(4.26552f).put(2.1043348f).put(-1.2155358f).put(4.3696966f).put(1.7041669f).put(-1.2155358f).put(4.5407367f);
//		buf.put(2.054166f).put(-1.6080236f).put(4.26552f).put(2.428255f).put(-1.6080236f).put(4.064214f).put(2.48756f).put(-1.2155358f).put(4.163474f).put(2.1043348f).put(-1.2155358f).put(4.3696966f);
//		buf.put(2.428255f).put(-1.6080236f).put(4.064214f).put(2.7827928f).put(-1.6080236f).put(3.830186f).put(2.850757f).put(-1.2155358f).put(3.9237301f).put(2.48756f).put(-1.2155358f).put(4.163474f);
//		buf.put(2.7827928f).put(-1.6080236f).put(3.830186f).put(3.1149254f).put(-1.6080236f).put(3.565319f).put(3.191001f).put(-1.2155358f).put(3.6523945f).put(2.850757f).put(-1.2155358f).put(3.9237301f);
//		buf.put(3.1149254f).put(-1.6080236f).put(3.565319f).put(3.4219785f).put(-1.6080236f).put(3.2717464f).put(3.505553f).put(-1.2155358f).put(3.351652f).put(3.191001f).put(-1.2155358f).put(3.6523945f);
//		buf.put(3.4219785f).put(-1.6080236f).put(3.2717464f).put(3.7014797f).put(-1.6080236f).put(2.9518313f).put(3.7918806f).put(-1.2155358f).put(3.0239239f).put(3.505553f).put(-1.2155358f).put(3.351652f);
//		buf.put(3.7014797f).put(-1.6080236f).put(2.9518313f).put(3.9511786f).put(-1.6080236f).put(2.6081502f).put(4.047678f).put(-1.2155358f).put(2.6718488f).put(3.7918806f).put(-1.2155358f).put(3.0239239f);
//		buf.put(3.9511786f).put(-1.6080236f).put(2.6081502f).put(4.169065f).put(-1.6080236f).put(2.2434697f).put(4.270886f).put(-1.2155358f).put(2.2982616f).put(4.047678f).put(-1.2155358f).put(2.6718488f);
//		buf.put(4.169065f).put(-1.6080236f).put(2.2434697f).put(4.353385f).put(-1.6080236f).put(1.860726f).put(4.4597073f).put(-1.2155358f).put(1.9061702f).put(4.270886f).put(-1.2155358f).put(2.2982616f);
//		buf.put(4.353385f).put(-1.6080236f).put(1.860726f).put(4.5026536f).put(-1.6080236f).put(1.4630008f).put(4.6126213f).put(-1.2155358f).put(1.4987316f).put(4.4597073f).put(-1.2155358f).put(1.9061702f);
//		buf.put(4.5026536f).put(-1.6080236f).put(1.4630008f).put(4.6156697f).put(-1.6080236f).put(1.0534965f).put(4.728398f).put(-1.2155358f).put(1.0792259f).put(4.6126213f).put(-1.2155358f).put(1.4987316f);
//		buf.put(4.6156697f).put(-1.6080236f).put(1.0534965f).put(4.691523f).put(-1.6080236f).put(0.63550997f).put(4.8061037f).put(-1.2155358f).put(0.65103096f).put(4.728398f).put(-1.2155358f).put(1.0792259f);
//		buf.put(4.691523f).put(-1.6080236f).put(0.63550997f).put(4.729603f).put(-1.6080236f).put(0.21240671f).put(4.8451138f).put(-1.2155358f).put(0.2175943f).put(4.8061037f).put(-1.2155358f).put(0.65103096f);
//		buf.put(4.729603f).put(-1.6080236f).put(0.21240671f).put(4.729603f).put(-1.6080236f).put(-0.21240671f).put(4.8451138f).put(-1.2155358f).put(-0.2175943f).put(4.8451138f).put(-1.2155358f).put(0.2175943f);
//		buf.put(-4.8451138f).put(-1.2155358f).put(-0.2175943f).put(-4.8451138f).put(-1.2155358f).put(0.2175943f).put(-4.9281783f).put(-0.81490785f).put(0.22132474f).put(-4.9281783f).put(-0.81490785f).put(-0.22132474f);
//		buf.put(-4.8451138f).put(-1.2155358f).put(0.2175943f).put(-4.8061037f).put(-1.2155358f).put(0.65103096f).put(-4.8884993f).put(-0.81490785f).put(0.6621922f).put(-4.9281783f).put(-0.81490785f).put(0.22132474f);
//		buf.put(-4.8061037f).put(-1.2155358f).put(0.65103096f).put(-4.728398f).put(-1.2155358f).put(1.0792259f).put(-4.809461f).put(-0.81490785f).put(1.0977281f).put(-4.8884993f).put(-0.81490785f).put(0.6621922f);
//		buf.put(-4.728398f).put(-1.2155358f).put(1.0792259f).put(-4.6126213f).put(-1.2155358f).put(1.4987316f).put(-4.6917005f).put(-0.81490785f).put(1.5244259f).put(-4.809461f).put(-0.81490785f).put(1.0977281f);
//		buf.put(-4.6126213f).put(-1.2155358f).put(1.4987316f).put(-4.4597073f).put(-1.2155358f).put(1.9061702f).put(-4.5361643f).put(-0.81490785f).put(1.9388497f).put(-4.6917005f).put(-0.81490785f).put(1.5244259f);
//		buf.put(-4.4597073f).put(-1.2155358f).put(1.9061702f).put(-4.270886f).put(-1.2155358f).put(2.2982616f).put(-4.3441057f).put(-0.81490785f).put(2.3376632f).put(-4.5361643f).put(-0.81490785f).put(1.9388497f);
//		buf.put(-4.270886f).put(-1.2155358f).put(2.2982616f).put(-4.047678f).put(-1.2155358f).put(2.6718488f).put(-4.117071f).put(-0.81490785f).put(2.717655f).put(-4.3441057f).put(-0.81490785f).put(2.3376632f);
//		buf.put(-4.047678f).put(-1.2155358f).put(2.6718488f).put(-3.7918806f).put(-1.2155358f).put(3.0239239f).put(-3.8568885f).put(-0.81490785f).put(3.0757658f).put(-4.117071f).put(-0.81490785f).put(2.717655f);
//		buf.put(-3.7918806f).put(-1.2155358f).put(3.0239239f).put(-3.505553f).put(-1.2155358f).put(3.351652f).put(-3.5656524f).put(-0.81490785f).put(3.4091127f).put(-3.8568885f).put(-0.81490785f).put(3.0757658f);
//		buf.put(-3.505553f).put(-1.2155358f).put(3.351652f).put(-3.191001f).put(-1.2155358f).put(3.6523945f).put(-3.2457075f).put(-0.81490785f).put(3.7150111f).put(-3.5656524f).put(-0.81490785f).put(3.4091127f);
//		buf.put(-3.191001f).put(-1.2155358f).put(3.6523945f).put(-2.850757f).put(-1.2155358f).put(3.9237301f).put(-2.8996303f).put(-0.81490785f).put(3.9909985f).put(-3.2457075f).put(-0.81490785f).put(3.7150111f);
//		buf.put(-2.850757f).put(-1.2155358f).put(3.9237301f).put(-2.48756f).put(-1.2155358f).put(4.163474f).put(-2.5302067f).put(-0.81490785f).put(4.234853f).put(-2.8996303f).put(-0.81490785f).put(3.9909985f);
//		buf.put(-2.48756f).put(-1.2155358f).put(4.163474f).put(-2.1043348f).put(-1.2155358f).put(4.3696966f).put(-2.1404116f).put(-0.81490785f).put(4.4446106f).put(-2.5302067f).put(-0.81490785f).put(4.234853f);
//		buf.put(-2.1043348f).put(-1.2155358f).put(4.3696966f).put(-1.7041669f).put(-1.2155358f).put(4.5407367f).put(-1.7333832f).put(-0.81490785f).put(4.6185827f).put(-2.1404116f).put(-0.81490785f).put(4.4446106f);
//		buf.put(-1.7041669f).put(-1.2155358f).put(4.5407367f).put(-1.290278f).put(-1.2155358f).put(4.675217f).put(-1.3123984f).put(-0.81490785f).put(4.755369f).put(-1.7333832f).put(-0.81490785f).put(4.6185827f);
//		buf.put(-1.290278f).put(-1.2155358f).put(4.675217f).put(-0.8660004f).put(-1.2155358f).put(4.7720556f).put(-0.88084716f).put(-0.81490785f).put(4.853868f).put(-1.3123984f).put(-0.81490785f).put(4.755369f);
//		buf.put(-0.8660004f).put(-1.2155358f).put(4.7720556f).put(-0.4347504f).put(-1.2155358f).put(4.8304725f).put(-0.44220376f).put(-0.81490785f).put(4.913286f).put(-0.88084716f).put(-0.81490785f).put(4.853868f);
//		buf.put(-0.4347504f).put(-1.2155358f).put(4.8304725f).put(7.44301E-17f).put(-1.2155358f).put(4.849997f).put(4.9898714E-17f).put(-0.81490785f).put(4.9331455f).put(-0.44220376f).put(-0.81490785f).put(4.913286f);
//		buf.put(7.44301E-17f).put(-1.2155358f).put(4.849997f).put(0.4347504f).put(-1.2155358f).put(4.8304725f).put(0.44220376f).put(-0.81490785f).put(4.913286f).put(4.9898714E-17f).put(-0.81490785f).put(4.9331455f);
//		buf.put(0.4347504f).put(-1.2155358f).put(4.8304725f).put(0.8660004f).put(-1.2155358f).put(4.7720556f).put(0.88084716f).put(-0.81490785f).put(4.853868f).put(0.44220376f).put(-0.81490785f).put(4.913286f);
//		buf.put(0.8660004f).put(-1.2155358f).put(4.7720556f).put(1.290278f).put(-1.2155358f).put(4.675217f).put(1.3123984f).put(-0.81490785f).put(4.755369f).put(0.88084716f).put(-0.81490785f).put(4.853868f);
//		buf.put(1.290278f).put(-1.2155358f).put(4.675217f).put(1.7041669f).put(-1.2155358f).put(4.5407367f).put(1.7333832f).put(-0.81490785f).put(4.6185827f).put(1.3123984f).put(-0.81490785f).put(4.755369f);
//		buf.put(1.7041669f).put(-1.2155358f).put(4.5407367f).put(2.1043348f).put(-1.2155358f).put(4.3696966f).put(2.1404116f).put(-0.81490785f).put(4.4446106f).put(1.7333832f).put(-0.81490785f).put(4.6185827f);
//		buf.put(2.1043348f).put(-1.2155358f).put(4.3696966f).put(2.48756f).put(-1.2155358f).put(4.163474f).put(2.5302067f).put(-0.81490785f).put(4.234853f).put(2.1404116f).put(-0.81490785f).put(4.4446106f);
//		buf.put(2.48756f).put(-1.2155358f).put(4.163474f).put(2.850757f).put(-1.2155358f).put(3.9237301f).put(2.8996303f).put(-0.81490785f).put(3.9909985f).put(2.5302067f).put(-0.81490785f).put(4.234853f);
//		buf.put(2.850757f).put(-1.2155358f).put(3.9237301f).put(3.191001f).put(-1.2155358f).put(3.6523945f).put(3.2457075f).put(-0.81490785f).put(3.7150111f).put(2.8996303f).put(-0.81490785f).put(3.9909985f);
//		buf.put(3.191001f).put(-1.2155358f).put(3.6523945f).put(3.505553f).put(-1.2155358f).put(3.351652f).put(3.5656524f).put(-0.81490785f).put(3.4091127f).put(3.2457075f).put(-0.81490785f).put(3.7150111f);
//		buf.put(3.505553f).put(-1.2155358f).put(3.351652f).put(3.7918806f).put(-1.2155358f).put(3.0239239f).put(3.8568885f).put(-0.81490785f).put(3.0757658f).put(3.5656524f).put(-0.81490785f).put(3.4091127f);
//		buf.put(3.7918806f).put(-1.2155358f).put(3.0239239f).put(4.047678f).put(-1.2155358f).put(2.6718488f).put(4.117071f).put(-0.81490785f).put(2.717655f).put(3.8568885f).put(-0.81490785f).put(3.0757658f);
//		buf.put(4.047678f).put(-1.2155358f).put(2.6718488f).put(4.270886f).put(-1.2155358f).put(2.2982616f).put(4.3441057f).put(-0.81490785f).put(2.3376632f).put(4.117071f).put(-0.81490785f).put(2.717655f);
//		buf.put(4.270886f).put(-1.2155358f).put(2.2982616f).put(4.4597073f).put(-1.2155358f).put(1.9061702f).put(4.5361643f).put(-0.81490785f).put(1.9388497f).put(4.3441057f).put(-0.81490785f).put(2.3376632f);
//		buf.put(4.4597073f).put(-1.2155358f).put(1.9061702f).put(4.6126213f).put(-1.2155358f).put(1.4987316f).put(4.6917005f).put(-0.81490785f).put(1.5244259f).put(4.5361643f).put(-0.81490785f).put(1.9388497f);
//		buf.put(4.6126213f).put(-1.2155358f).put(1.4987316f).put(4.728398f).put(-1.2155358f).put(1.0792259f).put(4.809461f).put(-0.81490785f).put(1.0977281f).put(4.6917005f).put(-0.81490785f).put(1.5244259f);
//		buf.put(4.728398f).put(-1.2155358f).put(1.0792259f).put(4.8061037f).put(-1.2155358f).put(0.65103096f).put(4.8884993f).put(-0.81490785f).put(0.6621922f).put(4.809461f).put(-0.81490785f).put(1.0977281f);
//		buf.put(4.8061037f).put(-1.2155358f).put(0.65103096f).put(4.8451138f).put(-1.2155358f).put(0.2175943f).put(4.9281783f).put(-0.81490785f).put(0.22132474f).put(4.8884993f).put(-0.81490785f).put(0.6621922f);
//		buf.put(4.8451138f).put(-1.2155358f).put(0.2175943f).put(4.8451138f).put(-1.2155358f).put(-0.2175943f).put(4.9281783f).put(-0.81490785f).put(-0.22132474f).put(4.9281783f).put(-0.81490785f).put(0.22132474f);
//		buf.put(-4.9281783f).put(-0.81490785f).put(-0.22132474f).put(-4.9281783f).put(-0.81490785f).put(0.22132474f).put(-4.9782405f).put(-0.4088228f).put(0.22357304f).put(-4.9782405f).put(-0.4088228f).put(-0.22357304f);
//		buf.put(-4.9281783f).put(-0.81490785f).put(0.22132474f).put(-4.8884993f).put(-0.81490785f).put(0.6621922f).put(-4.9381585f).put(-0.4088228f).put(0.668919f).put(-4.9782405f).put(-0.4088228f).put(0.22357304f);
//		buf.put(-4.8884993f).put(-0.81490785f).put(0.6621922f).put(-4.809461f).put(-0.81490785f).put(1.0977281f).put(-4.858318f).put(-0.4088228f).put(1.1088793f).put(-4.9381585f).put(-0.4088228f).put(0.668919f);
//		buf.put(-4.809461f).put(-0.81490785f).put(1.0977281f).put(-4.6917005f).put(-0.81490785f).put(1.5244259f).put(-4.7393603f).put(-0.4088228f).put(1.5399115f).put(-4.858318f).put(-0.4088228f).put(1.1088793f);
//		buf.put(-4.6917005f).put(-0.81490785f).put(1.5244259f).put(-4.5361643f).put(-0.81490785f).put(1.9388497f).put(-4.5822444f).put(-0.4088228f).put(1.9585453f).put(-4.7393603f).put(-0.4088228f).put(1.5399115f);
//		buf.put(-4.5361643f).put(-0.81490785f).put(1.9388497f).put(-4.3441057f).put(-0.81490785f).put(2.3376632f).put(-4.388235f).put(-0.4088228f).put(2.36141f).put(-4.5822444f).put(-0.4088228f).put(1.9585453f);
//		buf.put(-4.3441057f).put(-0.81490785f).put(2.3376632f).put(-4.117071f).put(-0.81490785f).put(2.717655f).put(-4.158894f).put(-0.4088228f).put(2.745262f).put(-4.388235f).put(-0.4088228f).put(2.36141f);
//		buf.put(-4.117071f).put(-0.81490785f).put(2.717655f).put(-3.8568885f).put(-0.81490785f).put(3.0757658f).put(-3.8960683f).put(-0.4088228f).put(3.1070108f).put(-4.158894f).put(-0.4088228f).put(2.745262f);
//		buf.put(-3.8568885f).put(-0.81490785f).put(3.0757658f).put(-3.5656524f).put(-0.81490785f).put(3.4091127f).put(-3.6018736f).put(-0.4088228f).put(3.4437437f).put(-3.8960683f).put(-0.4088228f).put(3.1070108f);
//		buf.put(-3.5656524f).put(-0.81490785f).put(3.4091127f).put(-3.2457075f).put(-0.81490785f).put(3.7150111f).put(-3.2786787f).put(-0.4088228f).put(3.7527497f).put(-3.6018736f).put(-0.4088228f).put(3.4437437f);
//		buf.put(-3.2457075f).put(-0.81490785f).put(3.7150111f).put(-2.8996303f).put(-0.81490785f).put(3.9909985f).put(-2.9290857f).put(-0.4088228f).put(4.031541f).put(-3.2786787f).put(-0.4088228f).put(3.7527497f);
//		buf.put(-2.8996303f).put(-0.81490785f).put(3.9909985f).put(-2.5302067f).put(-0.81490785f).put(4.234853f).put(-2.5559096f).put(-0.4088228f).put(4.277872f).put(-2.9290857f).put(-0.4088228f).put(4.031541f);
//		buf.put(-2.5302067f).put(-0.81490785f).put(4.234853f).put(-2.1404116f).put(-0.81490785f).put(4.4446106f).put(-2.1621547f).put(-0.4088228f).put(4.489761f).put(-2.5559096f).put(-0.4088228f).put(4.277872f);
//		buf.put(-2.1404116f).put(-0.81490785f).put(4.4446106f).put(-1.7333832f).put(-0.81490785f).put(4.6185827f).put(-1.7509916f).put(-0.4088228f).put(4.6655f).put(-2.1621547f).put(-0.4088228f).put(4.489761f);
//		buf.put(-1.7333832f).put(-0.81490785f).put(4.6185827f).put(-1.3123984f).put(-0.81490785f).put(4.755369f).put(-1.3257303f).put(-0.4088228f).put(4.803676f).put(-1.7509916f).put(-0.4088228f).put(4.6655f);
//		buf.put(-1.3123984f).put(-0.81490785f).put(4.755369f).put(-0.88084716f).put(-0.81490785f).put(4.853868f).put(-0.8897951f).put(-0.4088228f).put(4.9031754f).put(-1.3257303f).put(-0.4088228f).put(4.803676f);
//		buf.put(-0.88084716f).put(-0.81490785f).put(4.853868f).put(-0.44220376f).put(-0.81490785f).put(4.913286f).put(-0.44669583f).put(-0.4088228f).put(4.963197f).put(-0.8897951f).put(-0.4088228f).put(4.9031754f);
//		buf.put(-0.44220376f).put(-0.81490785f).put(4.913286f).put(4.9898714E-17f).put(-0.81490785f).put(4.9331455f).put(2.5033177E-17f).put(-0.4088228f).put(4.9832582f).put(-0.44669583f).put(-0.4088228f).put(4.963197f);
//		buf.put(4.9898714E-17f).put(-0.81490785f).put(4.9331455f).put(0.44220376f).put(-0.81490785f).put(4.913286f).put(0.44669583f).put(-0.4088228f).put(4.963197f).put(2.5033177E-17f).put(-0.4088228f).put(4.9832582f);
//		buf.put(0.44220376f).put(-0.81490785f).put(4.913286f).put(0.88084716f).put(-0.81490785f).put(4.853868f).put(0.8897951f).put(-0.4088228f).put(4.9031754f).put(0.44669583f).put(-0.4088228f).put(4.963197f);
//		buf.put(0.88084716f).put(-0.81490785f).put(4.853868f).put(1.3123984f).put(-0.81490785f).put(4.755369f).put(1.3257303f).put(-0.4088228f).put(4.803676f).put(0.8897951f).put(-0.4088228f).put(4.9031754f);
//		buf.put(1.3123984f).put(-0.81490785f).put(4.755369f).put(1.7333832f).put(-0.81490785f).put(4.6185827f).put(1.7509916f).put(-0.4088228f).put(4.6655f).put(1.3257303f).put(-0.4088228f).put(4.803676f);
//		buf.put(1.7333832f).put(-0.81490785f).put(4.6185827f).put(2.1404116f).put(-0.81490785f).put(4.4446106f).put(2.1621547f).put(-0.4088228f).put(4.489761f).put(1.7509916f).put(-0.4088228f).put(4.6655f);
//		buf.put(2.1404116f).put(-0.81490785f).put(4.4446106f).put(2.5302067f).put(-0.81490785f).put(4.234853f).put(2.5559096f).put(-0.4088228f).put(4.277872f).put(2.1621547f).put(-0.4088228f).put(4.489761f);
//		buf.put(2.5302067f).put(-0.81490785f).put(4.234853f).put(2.8996303f).put(-0.81490785f).put(3.9909985f).put(2.9290857f).put(-0.4088228f).put(4.031541f).put(2.5559096f).put(-0.4088228f).put(4.277872f);
//		buf.put(2.8996303f).put(-0.81490785f).put(3.9909985f).put(3.2457075f).put(-0.81490785f).put(3.7150111f).put(3.2786787f).put(-0.4088228f).put(3.7527497f).put(2.9290857f).put(-0.4088228f).put(4.031541f);
//		buf.put(3.2457075f).put(-0.81490785f).put(3.7150111f).put(3.5656524f).put(-0.81490785f).put(3.4091127f).put(3.6018736f).put(-0.4088228f).put(3.4437437f).put(3.2786787f).put(-0.4088228f).put(3.7527497f);
//		buf.put(3.5656524f).put(-0.81490785f).put(3.4091127f).put(3.8568885f).put(-0.81490785f).put(3.0757658f).put(3.8960683f).put(-0.4088228f).put(3.1070108f).put(3.6018736f).put(-0.4088228f).put(3.4437437f);
//		buf.put(3.8568885f).put(-0.81490785f).put(3.0757658f).put(4.117071f).put(-0.81490785f).put(2.717655f).put(4.158894f).put(-0.4088228f).put(2.745262f).put(3.8960683f).put(-0.4088228f).put(3.1070108f);
//		buf.put(4.117071f).put(-0.81490785f).put(2.717655f).put(4.3441057f).put(-0.81490785f).put(2.3376632f).put(4.388235f).put(-0.4088228f).put(2.36141f).put(4.158894f).put(-0.4088228f).put(2.745262f);
//		buf.put(4.3441057f).put(-0.81490785f).put(2.3376632f).put(4.5361643f).put(-0.81490785f).put(1.9388497f).put(4.5822444f).put(-0.4088228f).put(1.9585453f).put(4.388235f).put(-0.4088228f).put(2.36141f);
//		buf.put(4.5361643f).put(-0.81490785f).put(1.9388497f).put(4.6917005f).put(-0.81490785f).put(1.5244259f).put(4.7393603f).put(-0.4088228f).put(1.5399115f).put(4.5822444f).put(-0.4088228f).put(1.9585453f);
//		buf.put(4.6917005f).put(-0.81490785f).put(1.5244259f).put(4.809461f).put(-0.81490785f).put(1.0977281f).put(4.858318f).put(-0.4088228f).put(1.1088793f).put(4.7393603f).put(-0.4088228f).put(1.5399115f);
//		buf.put(4.809461f).put(-0.81490785f).put(1.0977281f).put(4.8884993f).put(-0.81490785f).put(0.6621922f).put(4.9381585f).put(-0.4088228f).put(0.668919f).put(4.858318f).put(-0.4088228f).put(1.1088793f);
//		buf.put(4.8884993f).put(-0.81490785f).put(0.6621922f).put(4.9281783f).put(-0.81490785f).put(0.22132474f).put(4.9782405f).put(-0.4088228f).put(0.22357304f).put(4.9381585f).put(-0.4088228f).put(0.668919f);
//		buf.put(4.9281783f).put(-0.81490785f).put(0.22132474f).put(4.9281783f).put(-0.81490785f).put(-0.22132474f).put(4.9782405f).put(-0.4088228f).put(-0.22357304f).put(4.9782405f).put(-0.4088228f).put(0.22357304f);
//		buf.put(-4.9782405f).put(-0.4088228f).put(-0.22357304f).put(-4.9782405f).put(-0.4088228f).put(0.22357304f).put(-4.9949656f).put(6.117068E-16f).put(0.22432415f).put(-4.9949656f).put(6.117068E-16f).put(-0.22432415f);
//		buf.put(-4.9782405f).put(-0.4088228f).put(0.22357304f).put(-4.9381585f).put(-0.4088228f).put(0.668919f).put(-4.9547486f).put(6.0678174E-16f).put(0.6711663f).put(-4.9949656f).put(6.117068E-16f).put(0.22432415f);
//		buf.put(-4.9381585f).put(-0.4088228f).put(0.668919f).put(-4.858318f).put(-0.4088228f).put(1.1088793f).put(-4.8746395f).put(5.969712E-16f).put(1.1126046f).put(-4.9547486f).put(6.0678174E-16f).put(0.6711663f);
//		buf.put(-4.858318f).put(-0.4088228f).put(1.1088793f).put(-4.7393603f).put(-0.4088228f).put(1.5399115f).put(-4.7552824f).put(5.8235416E-16f).put(1.545085f).put(-4.8746395f).put(5.969712E-16f).put(1.1126046f);
//		buf.put(-4.7393603f).put(-0.4088228f).put(1.5399115f).put(-4.5822444f).put(-0.4088228f).put(1.9585453f).put(-4.597639f).put(5.6304837E-16f).put(1.9651252f).put(-4.7552824f).put(5.8235416E-16f).put(1.545085f);
//		buf.put(-4.5822444f).put(-0.4088228f).put(1.9585453f).put(-4.388235f).put(-0.4088228f).put(2.36141f).put(-4.4029775f).put(5.3920926E-16f).put(2.3693433f).put(-4.597639f).put(5.6304837E-16f).put(1.9651252f);
//		buf.put(-4.388235f).put(-0.4088228f).put(2.36141f).put(-4.158894f).put(-0.4088228f).put(2.745262f).put(-4.1728663f).put(5.1102874E-16f).put(2.754485f).put(-4.4029775f).put(5.3920926E-16f).put(2.3693433f);
//		buf.put(-4.158894f).put(-0.4088228f).put(2.745262f).put(-3.8960683f).put(-0.4088228f).put(3.1070108f).put(-3.9091575f).put(4.787337E-16f).put(3.117449f).put(-4.1728663f).put(5.1102874E-16f).put(2.754485f);
//		buf.put(-3.8960683f).put(-0.4088228f).put(3.1070108f).put(-3.6018736f).put(-0.4088228f).put(3.4437437f).put(-3.6139743f).put(4.4258422E-16f).put(3.4553132f).put(-3.9091575f).put(4.787337E-16f).put(3.117449f);
//		buf.put(-3.6018736f).put(-0.4088228f).put(3.4437437f).put(-3.2786787f).put(-0.4088228f).put(3.7527497f).put(-3.2896936f).put(4.028713E-16f).put(3.7653573f).put(-3.6139743f).put(4.4258422E-16f).put(3.4553132f);
//		buf.put(-3.2786787f).put(-0.4088228f).put(3.7527497f).put(-2.9290857f).put(-0.4088228f).put(4.031541f).put(-2.9389262f).put(3.5991468E-16f).put(4.045085f).put(-3.2896936f).put(4.028713E-16f).put(3.7653573f);
//		buf.put(-2.9290857f).put(-0.4088228f).put(4.031541f).put(-2.5559096f).put(-0.4088228f).put(4.277872f).put(-2.5644963f).put(3.1406023E-16f).put(4.292244f).put(-2.9389262f).put(3.5991468E-16f).put(4.045085f);
//		buf.put(-2.5559096f).put(-0.4088228f).put(4.277872f).put(-2.1621547f).put(-0.4088228f).put(4.489761f).put(-2.1694188f).put(2.6567718E-16f).put(4.504844f).put(-2.5644963f).put(3.1406023E-16f).put(4.292244f);
//		buf.put(-2.1621547f).put(-0.4088228f).put(4.489761f).put(-1.7509916f).put(-0.4088228f).put(4.6655f).put(-1.7568741f).put(2.1515502E-16f).put(4.6811743f).put(-2.1694188f).put(2.6567718E-16f).put(4.504844f);
//		buf.put(-1.7509916f).put(-0.4088228f).put(4.6655f).put(-1.3257303f).put(-0.4088228f).put(4.803676f).put(-1.3301842f).put(1.6290058E-16f).put(4.819814f).put(-1.7568741f).put(2.1515502E-16f).put(4.6811743f);
//		buf.put(-1.3257303f).put(-0.4088228f).put(4.803676f).put(-0.8897951f).put(-0.4088228f).put(4.9031754f).put(-0.8927845f).put(1.0933456E-16f).put(4.919648f).put(-1.3301842f).put(1.6290058E-16f).put(4.819814f);
//		buf.put(-0.8897951f).put(-0.4088228f).put(4.9031754f).put(-0.44669583f).put(-0.4088228f).put(4.963197f).put(-0.44819653f).put(5.4888248E-17f).put(4.9798713f).put(-0.8927845f).put(1.0933456E-16f).put(4.919648f);
//		buf.put(-0.44669583f).put(-0.4088228f).put(4.963197f).put(2.5033177E-17f).put(-0.4088228f).put(4.9832582f).put(0.0f).put(0.0f).put(5.0f).put(-0.44819653f).put(5.4888248E-17f).put(4.9798713f);
//		buf.put(2.5033177E-17f).put(-0.4088228f).put(4.9832582f).put(0.44669583f).put(-0.4088228f).put(4.963197f).put(0.44819653f).put(0.0f).put(4.9798713f).put(0.0f).put(0.0f).put(5.0f);
//		buf.put(0.44669583f).put(-0.4088228f).put(4.963197f).put(0.8897951f).put(-0.4088228f).put(4.9031754f).put(0.8927845f).put(0.0f).put(4.919648f).put(0.44819653f).put(0.0f).put(4.9798713f);
//		buf.put(0.8897951f).put(-0.4088228f).put(4.9031754f).put(1.3257303f).put(-0.4088228f).put(4.803676f).put(1.3301842f).put(0.0f).put(4.819814f).put(0.8927845f).put(0.0f).put(4.919648f);
//		buf.put(1.3257303f).put(-0.4088228f).put(4.803676f).put(1.7509916f).put(-0.4088228f).put(4.6655f).put(1.7568741f).put(0.0f).put(4.6811743f).put(1.3301842f).put(0.0f).put(4.819814f);
//		buf.put(1.7509916f).put(-0.4088228f).put(4.6655f).put(2.1621547f).put(-0.4088228f).put(4.489761f).put(2.1694188f).put(0.0f).put(4.504844f).put(1.7568741f).put(0.0f).put(4.6811743f);
//		buf.put(2.1621547f).put(-0.4088228f).put(4.489761f).put(2.5559096f).put(-0.4088228f).put(4.277872f).put(2.5644963f).put(0.0f).put(4.292244f).put(2.1694188f).put(0.0f).put(4.504844f);
//		buf.put(2.5559096f).put(-0.4088228f).put(4.277872f).put(2.9290857f).put(-0.4088228f).put(4.031541f).put(2.9389262f).put(0.0f).put(4.045085f).put(2.5644963f).put(0.0f).put(4.292244f);
//		buf.put(2.9290857f).put(-0.4088228f).put(4.031541f).put(3.2786787f).put(-0.4088228f).put(3.7527497f).put(3.2896936f).put(0.0f).put(3.7653573f).put(2.9389262f).put(0.0f).put(4.045085f);
//		buf.put(3.2786787f).put(-0.4088228f).put(3.7527497f).put(3.6018736f).put(-0.4088228f).put(3.4437437f).put(3.6139743f).put(0.0f).put(3.4553132f).put(3.2896936f).put(0.0f).put(3.7653573f);
//		buf.put(3.6018736f).put(-0.4088228f).put(3.4437437f).put(3.8960683f).put(-0.4088228f).put(3.1070108f).put(3.9091575f).put(0.0f).put(3.117449f).put(3.6139743f).put(0.0f).put(3.4553132f);
//		buf.put(3.8960683f).put(-0.4088228f).put(3.1070108f).put(4.158894f).put(-0.4088228f).put(2.745262f).put(4.1728663f).put(0.0f).put(2.754485f).put(3.9091575f).put(0.0f).put(3.117449f);
//		buf.put(4.158894f).put(-0.4088228f).put(2.745262f).put(4.388235f).put(-0.4088228f).put(2.36141f).put(4.4029775f).put(0.0f).put(2.3693433f).put(4.1728663f).put(0.0f).put(2.754485f);
//		buf.put(4.388235f).put(-0.4088228f).put(2.36141f).put(4.5822444f).put(-0.4088228f).put(1.9585453f).put(4.597639f).put(0.0f).put(1.9651252f).put(4.4029775f).put(0.0f).put(2.3693433f);
//		buf.put(4.5822444f).put(-0.4088228f).put(1.9585453f).put(4.7393603f).put(-0.4088228f).put(1.5399115f).put(4.7552824f).put(0.0f).put(1.545085f).put(4.597639f).put(0.0f).put(1.9651252f);
//		buf.put(4.7393603f).put(-0.4088228f).put(1.5399115f).put(4.858318f).put(-0.4088228f).put(1.1088793f).put(4.8746395f).put(0.0f).put(1.1126046f).put(4.7552824f).put(0.0f).put(1.545085f);
//		buf.put(4.858318f).put(-0.4088228f).put(1.1088793f).put(4.9381585f).put(-0.4088228f).put(0.668919f).put(4.9547486f).put(0.0f).put(0.6711663f).put(4.8746395f).put(0.0f).put(1.1126046f);
//		buf.put(4.9381585f).put(-0.4088228f).put(0.668919f).put(4.9782405f).put(-0.4088228f).put(0.22357304f).put(4.9949656f).put(0.0f).put(0.22432415f).put(4.9547486f).put(0.0f).put(0.6711663f);
//		buf.put(4.9782405f).put(-0.4088228f).put(0.22357304f).put(4.9782405f).put(-0.4088228f).put(-0.22357304f).put(4.9949656f).put(0.0f).put(-0.22432415f).put(4.9949656f).put(0.0f).put(0.22432415f);
//		buf.put(-4.9949656f).put(6.117068E-16f).put(-0.22432415f).put(-4.9949656f).put(6.117068E-16f).put(0.22432415f).put(-4.9782405f).put(0.4088228f).put(0.22357304f).put(-4.9782405f).put(0.4088228f).put(-0.22357304f);
//		buf.put(-4.9949656f).put(6.117068E-16f).put(0.22432415f).put(-4.9547486f).put(6.0678174E-16f).put(0.6711663f).put(-4.9381585f).put(0.4088228f).put(0.668919f).put(-4.9782405f).put(0.4088228f).put(0.22357304f);
//		buf.put(-4.9547486f).put(6.0678174E-16f).put(0.6711663f).put(-4.8746395f).put(5.969712E-16f).put(1.1126046f).put(-4.858318f).put(0.4088228f).put(1.1088793f).put(-4.9381585f).put(0.4088228f).put(0.668919f);
//		buf.put(-4.8746395f).put(5.969712E-16f).put(1.1126046f).put(-4.7552824f).put(5.8235416E-16f).put(1.545085f).put(-4.7393603f).put(0.4088228f).put(1.5399115f).put(-4.858318f).put(0.4088228f).put(1.1088793f);
//		buf.put(-4.7552824f).put(5.8235416E-16f).put(1.545085f).put(-4.597639f).put(5.6304837E-16f).put(1.9651252f).put(-4.5822444f).put(0.4088228f).put(1.9585453f).put(-4.7393603f).put(0.4088228f).put(1.5399115f);
//		buf.put(-4.597639f).put(5.6304837E-16f).put(1.9651252f).put(-4.4029775f).put(5.3920926E-16f).put(2.3693433f).put(-4.388235f).put(0.4088228f).put(2.36141f).put(-4.5822444f).put(0.4088228f).put(1.9585453f);
//		buf.put(-4.4029775f).put(5.3920926E-16f).put(2.3693433f).put(-4.1728663f).put(5.1102874E-16f).put(2.754485f).put(-4.158894f).put(0.4088228f).put(2.745262f).put(-4.388235f).put(0.4088228f).put(2.36141f);
//		buf.put(-4.1728663f).put(5.1102874E-16f).put(2.754485f).put(-3.9091575f).put(4.787337E-16f).put(3.117449f).put(-3.8960683f).put(0.4088228f).put(3.1070108f).put(-4.158894f).put(0.4088228f).put(2.745262f);
//		buf.put(-3.9091575f).put(4.787337E-16f).put(3.117449f).put(-3.6139743f).put(4.4258422E-16f).put(3.4553132f).put(-3.6018736f).put(0.4088228f).put(3.4437437f).put(-3.8960683f).put(0.4088228f).put(3.1070108f);
//		buf.put(-3.6139743f).put(4.4258422E-16f).put(3.4553132f).put(-3.2896936f).put(4.028713E-16f).put(3.7653573f).put(-3.2786787f).put(0.4088228f).put(3.7527497f).put(-3.6018736f).put(0.4088228f).put(3.4437437f);
//		buf.put(-3.2896936f).put(4.028713E-16f).put(3.7653573f).put(-2.9389262f).put(3.5991468E-16f).put(4.045085f).put(-2.9290857f).put(0.4088228f).put(4.031541f).put(-3.2786787f).put(0.4088228f).put(3.7527497f);
//		buf.put(-2.9389262f).put(3.5991468E-16f).put(4.045085f).put(-2.5644963f).put(3.1406023E-16f).put(4.292244f).put(-2.5559096f).put(0.4088228f).put(4.277872f).put(-2.9290857f).put(0.4088228f).put(4.031541f);
//		buf.put(-2.5644963f).put(3.1406023E-16f).put(4.292244f).put(-2.1694188f).put(2.6567718E-16f).put(4.504844f).put(-2.1621547f).put(0.4088228f).put(4.489761f).put(-2.5559096f).put(0.4088228f).put(4.277872f);
//		buf.put(-2.1694188f).put(2.6567718E-16f).put(4.504844f).put(-1.7568741f).put(2.1515502E-16f).put(4.6811743f).put(-1.7509916f).put(0.4088228f).put(4.6655f).put(-2.1621547f).put(0.4088228f).put(4.489761f);
//		buf.put(-1.7568741f).put(2.1515502E-16f).put(4.6811743f).put(-1.3301842f).put(1.6290058E-16f).put(4.819814f).put(-1.3257303f).put(0.4088228f).put(4.803676f).put(-1.7509916f).put(0.4088228f).put(4.6655f);
//		buf.put(-1.3301842f).put(1.6290058E-16f).put(4.819814f).put(-0.8927845f).put(1.0933456E-16f).put(4.919648f).put(-0.8897951f).put(0.4088228f).put(4.9031754f).put(-1.3257303f).put(0.4088228f).put(4.803676f);
//		buf.put(-0.8927845f).put(1.0933456E-16f).put(4.919648f).put(-0.44819653f).put(5.4888248E-17f).put(4.9798713f).put(-0.44669583f).put(0.4088228f).put(4.963197f).put(-0.8897951f).put(0.4088228f).put(4.9031754f);
//		buf.put(-0.44819653f).put(5.4888248E-17f).put(4.9798713f).put(0.0f).put(0.0f).put(5.0f).put(2.5033177E-17f).put(0.4088228f).put(4.9832582f).put(-0.44669583f).put(0.4088228f).put(4.963197f);
//		buf.put(0.0f).put(0.0f).put(5.0f).put(0.44819653f).put(0.0f).put(4.9798713f).put(0.44669583f).put(0.4088228f).put(4.963197f).put(2.5033177E-17f).put(0.4088228f).put(4.9832582f);
//		buf.put(0.44819653f).put(0.0f).put(4.9798713f).put(0.8927845f).put(0.0f).put(4.919648f).put(0.8897951f).put(0.4088228f).put(4.9031754f).put(0.44669583f).put(0.4088228f).put(4.963197f);
//		buf.put(0.8927845f).put(0.0f).put(4.919648f).put(1.3301842f).put(0.0f).put(4.819814f).put(1.3257303f).put(0.4088228f).put(4.803676f).put(0.8897951f).put(0.4088228f).put(4.9031754f);
//		buf.put(1.3301842f).put(0.0f).put(4.819814f).put(1.7568741f).put(0.0f).put(4.6811743f).put(1.7509916f).put(0.4088228f).put(4.6655f).put(1.3257303f).put(0.4088228f).put(4.803676f);
//		buf.put(1.7568741f).put(0.0f).put(4.6811743f).put(2.1694188f).put(0.0f).put(4.504844f).put(2.1621547f).put(0.4088228f).put(4.489761f).put(1.7509916f).put(0.4088228f).put(4.6655f);
//		buf.put(2.1694188f).put(0.0f).put(4.504844f).put(2.5644963f).put(0.0f).put(4.292244f).put(2.5559096f).put(0.4088228f).put(4.277872f).put(2.1621547f).put(0.4088228f).put(4.489761f);
//		buf.put(2.5644963f).put(0.0f).put(4.292244f).put(2.9389262f).put(0.0f).put(4.045085f).put(2.9290857f).put(0.4088228f).put(4.031541f).put(2.5559096f).put(0.4088228f).put(4.277872f);
//		buf.put(2.9389262f).put(0.0f).put(4.045085f).put(3.2896936f).put(0.0f).put(3.7653573f).put(3.2786787f).put(0.4088228f).put(3.7527497f).put(2.9290857f).put(0.4088228f).put(4.031541f);
//		buf.put(3.2896936f).put(0.0f).put(3.7653573f).put(3.6139743f).put(0.0f).put(3.4553132f).put(3.6018736f).put(0.4088228f).put(3.4437437f).put(3.2786787f).put(0.4088228f).put(3.7527497f);
//		buf.put(3.6139743f).put(0.0f).put(3.4553132f).put(3.9091575f).put(0.0f).put(3.117449f).put(3.8960683f).put(0.4088228f).put(3.1070108f).put(3.6018736f).put(0.4088228f).put(3.4437437f);
//		buf.put(3.9091575f).put(0.0f).put(3.117449f).put(4.1728663f).put(0.0f).put(2.754485f).put(4.158894f).put(0.4088228f).put(2.745262f).put(3.8960683f).put(0.4088228f).put(3.1070108f);
//		buf.put(4.1728663f).put(0.0f).put(2.754485f).put(4.4029775f).put(0.0f).put(2.3693433f).put(4.388235f).put(0.4088228f).put(2.36141f).put(4.158894f).put(0.4088228f).put(2.745262f);
//		buf.put(4.4029775f).put(0.0f).put(2.3693433f).put(4.597639f).put(0.0f).put(1.9651252f).put(4.5822444f).put(0.4088228f).put(1.9585453f).put(4.388235f).put(0.4088228f).put(2.36141f);
//		buf.put(4.597639f).put(0.0f).put(1.9651252f).put(4.7552824f).put(0.0f).put(1.545085f).put(4.7393603f).put(0.4088228f).put(1.5399115f).put(4.5822444f).put(0.4088228f).put(1.9585453f);
//		buf.put(4.7552824f).put(0.0f).put(1.545085f).put(4.8746395f).put(0.0f).put(1.1126046f).put(4.858318f).put(0.4088228f).put(1.1088793f).put(4.7393603f).put(0.4088228f).put(1.5399115f);
//		buf.put(4.8746395f).put(0.0f).put(1.1126046f).put(4.9547486f).put(0.0f).put(0.6711663f).put(4.9381585f).put(0.4088228f).put(0.668919f).put(4.858318f).put(0.4088228f).put(1.1088793f);
//		buf.put(4.9547486f).put(0.0f).put(0.6711663f).put(4.9949656f).put(0.0f).put(0.22432415f).put(4.9782405f).put(0.4088228f).put(0.22357304f).put(4.9381585f).put(0.4088228f).put(0.668919f);
//		buf.put(4.9949656f).put(0.0f).put(0.22432415f).put(4.9949656f).put(0.0f).put(-0.22432415f).put(4.9782405f).put(0.4088228f).put(-0.22357304f).put(4.9782405f).put(0.4088228f).put(0.22357304f);
//		buf.put(-4.9782405f).put(0.4088228f).put(-0.22357304f).put(-4.9782405f).put(0.4088228f).put(0.22357304f).put(-4.9281783f).put(0.81490785f).put(0.22132474f).put(-4.9281783f).put(0.81490785f).put(-0.22132474f);
//		buf.put(-4.9782405f).put(0.4088228f).put(0.22357304f).put(-4.9381585f).put(0.4088228f).put(0.668919f).put(-4.8884993f).put(0.81490785f).put(0.6621922f).put(-4.9281783f).put(0.81490785f).put(0.22132474f);
//		buf.put(-4.9381585f).put(0.4088228f).put(0.668919f).put(-4.858318f).put(0.4088228f).put(1.1088793f).put(-4.809461f).put(0.81490785f).put(1.0977281f).put(-4.8884993f).put(0.81490785f).put(0.6621922f);
//		buf.put(-4.858318f).put(0.4088228f).put(1.1088793f).put(-4.7393603f).put(0.4088228f).put(1.5399115f).put(-4.6917005f).put(0.81490785f).put(1.5244259f).put(-4.809461f).put(0.81490785f).put(1.0977281f);
//		buf.put(-4.7393603f).put(0.4088228f).put(1.5399115f).put(-4.5822444f).put(0.4088228f).put(1.9585453f).put(-4.5361643f).put(0.81490785f).put(1.9388497f).put(-4.6917005f).put(0.81490785f).put(1.5244259f);
//		buf.put(-4.5822444f).put(0.4088228f).put(1.9585453f).put(-4.388235f).put(0.4088228f).put(2.36141f).put(-4.3441057f).put(0.81490785f).put(2.3376632f).put(-4.5361643f).put(0.81490785f).put(1.9388497f);
//		buf.put(-4.388235f).put(0.4088228f).put(2.36141f).put(-4.158894f).put(0.4088228f).put(2.745262f).put(-4.117071f).put(0.81490785f).put(2.717655f).put(-4.3441057f).put(0.81490785f).put(2.3376632f);
//		buf.put(-4.158894f).put(0.4088228f).put(2.745262f).put(-3.8960683f).put(0.4088228f).put(3.1070108f).put(-3.8568885f).put(0.81490785f).put(3.0757658f).put(-4.117071f).put(0.81490785f).put(2.717655f);
//		buf.put(-3.8960683f).put(0.4088228f).put(3.1070108f).put(-3.6018736f).put(0.4088228f).put(3.4437437f).put(-3.5656524f).put(0.81490785f).put(3.4091127f).put(-3.8568885f).put(0.81490785f).put(3.0757658f);
//		buf.put(-3.6018736f).put(0.4088228f).put(3.4437437f).put(-3.2786787f).put(0.4088228f).put(3.7527497f).put(-3.2457075f).put(0.81490785f).put(3.7150111f).put(-3.5656524f).put(0.81490785f).put(3.4091127f);
//		buf.put(-3.2786787f).put(0.4088228f).put(3.7527497f).put(-2.9290857f).put(0.4088228f).put(4.031541f).put(-2.8996303f).put(0.81490785f).put(3.9909985f).put(-3.2457075f).put(0.81490785f).put(3.7150111f);
//		buf.put(-2.9290857f).put(0.4088228f).put(4.031541f).put(-2.5559096f).put(0.4088228f).put(4.277872f).put(-2.5302067f).put(0.81490785f).put(4.234853f).put(-2.8996303f).put(0.81490785f).put(3.9909985f);
//		buf.put(-2.5559096f).put(0.4088228f).put(4.277872f).put(-2.1621547f).put(0.4088228f).put(4.489761f).put(-2.1404116f).put(0.81490785f).put(4.4446106f).put(-2.5302067f).put(0.81490785f).put(4.234853f);
//		buf.put(-2.1621547f).put(0.4088228f).put(4.489761f).put(-1.7509916f).put(0.4088228f).put(4.6655f).put(-1.7333832f).put(0.81490785f).put(4.6185827f).put(-2.1404116f).put(0.81490785f).put(4.4446106f);
//		buf.put(-1.7509916f).put(0.4088228f).put(4.6655f).put(-1.3257303f).put(0.4088228f).put(4.803676f).put(-1.3123984f).put(0.81490785f).put(4.755369f).put(-1.7333832f).put(0.81490785f).put(4.6185827f);
//		buf.put(-1.3257303f).put(0.4088228f).put(4.803676f).put(-0.8897951f).put(0.4088228f).put(4.9031754f).put(-0.88084716f).put(0.81490785f).put(4.853868f).put(-1.3123984f).put(0.81490785f).put(4.755369f);
//		buf.put(-0.8897951f).put(0.4088228f).put(4.9031754f).put(-0.44669583f).put(0.4088228f).put(4.963197f).put(-0.44220376f).put(0.81490785f).put(4.913286f).put(-0.88084716f).put(0.81490785f).put(4.853868f);
//		buf.put(-0.44669583f).put(0.4088228f).put(4.963197f).put(2.5033177E-17f).put(0.4088228f).put(4.9832582f).put(4.9898714E-17f).put(0.81490785f).put(4.9331455f).put(-0.44220376f).put(0.81490785f).put(4.913286f);
//		buf.put(2.5033177E-17f).put(0.4088228f).put(4.9832582f).put(0.44669583f).put(0.4088228f).put(4.963197f).put(0.44220376f).put(0.81490785f).put(4.913286f).put(4.9898714E-17f).put(0.81490785f).put(4.9331455f);
//		buf.put(0.44669583f).put(0.4088228f).put(4.963197f).put(0.8897951f).put(0.4088228f).put(4.9031754f).put(0.88084716f).put(0.81490785f).put(4.853868f).put(0.44220376f).put(0.81490785f).put(4.913286f);
//		buf.put(0.8897951f).put(0.4088228f).put(4.9031754f).put(1.3257303f).put(0.4088228f).put(4.803676f).put(1.3123984f).put(0.81490785f).put(4.755369f).put(0.88084716f).put(0.81490785f).put(4.853868f);
//		buf.put(1.3257303f).put(0.4088228f).put(4.803676f).put(1.7509916f).put(0.4088228f).put(4.6655f).put(1.7333832f).put(0.81490785f).put(4.6185827f).put(1.3123984f).put(0.81490785f).put(4.755369f);
//		buf.put(1.7509916f).put(0.4088228f).put(4.6655f).put(2.1621547f).put(0.4088228f).put(4.489761f).put(2.1404116f).put(0.81490785f).put(4.4446106f).put(1.7333832f).put(0.81490785f).put(4.6185827f);
//		buf.put(2.1621547f).put(0.4088228f).put(4.489761f).put(2.5559096f).put(0.4088228f).put(4.277872f).put(2.5302067f).put(0.81490785f).put(4.234853f).put(2.1404116f).put(0.81490785f).put(4.4446106f);
//		buf.put(2.5559096f).put(0.4088228f).put(4.277872f).put(2.9290857f).put(0.4088228f).put(4.031541f).put(2.8996303f).put(0.81490785f).put(3.9909985f).put(2.5302067f).put(0.81490785f).put(4.234853f);
//		buf.put(2.9290857f).put(0.4088228f).put(4.031541f).put(3.2786787f).put(0.4088228f).put(3.7527497f).put(3.2457075f).put(0.81490785f).put(3.7150111f).put(2.8996303f).put(0.81490785f).put(3.9909985f);
//		buf.put(3.2786787f).put(0.4088228f).put(3.7527497f).put(3.6018736f).put(0.4088228f).put(3.4437437f).put(3.5656524f).put(0.81490785f).put(3.4091127f).put(3.2457075f).put(0.81490785f).put(3.7150111f);
//		buf.put(3.6018736f).put(0.4088228f).put(3.4437437f).put(3.8960683f).put(0.4088228f).put(3.1070108f).put(3.8568885f).put(0.81490785f).put(3.0757658f).put(3.5656524f).put(0.81490785f).put(3.4091127f);
//		buf.put(3.8960683f).put(0.4088228f).put(3.1070108f).put(4.158894f).put(0.4088228f).put(2.745262f).put(4.117071f).put(0.81490785f).put(2.717655f).put(3.8568885f).put(0.81490785f).put(3.0757658f);
//		buf.put(4.158894f).put(0.4088228f).put(2.745262f).put(4.388235f).put(0.4088228f).put(2.36141f).put(4.3441057f).put(0.81490785f).put(2.3376632f).put(4.117071f).put(0.81490785f).put(2.717655f);
//		buf.put(4.388235f).put(0.4088228f).put(2.36141f).put(4.5822444f).put(0.4088228f).put(1.9585453f).put(4.5361643f).put(0.81490785f).put(1.9388497f).put(4.3441057f).put(0.81490785f).put(2.3376632f);
//		buf.put(4.5822444f).put(0.4088228f).put(1.9585453f).put(4.7393603f).put(0.4088228f).put(1.5399115f).put(4.6917005f).put(0.81490785f).put(1.5244259f).put(4.5361643f).put(0.81490785f).put(1.9388497f);
//		buf.put(4.7393603f).put(0.4088228f).put(1.5399115f).put(4.858318f).put(0.4088228f).put(1.1088793f).put(4.809461f).put(0.81490785f).put(1.0977281f).put(4.6917005f).put(0.81490785f).put(1.5244259f);
//		buf.put(4.858318f).put(0.4088228f).put(1.1088793f).put(4.9381585f).put(0.4088228f).put(0.668919f).put(4.8884993f).put(0.81490785f).put(0.6621922f).put(4.809461f).put(0.81490785f).put(1.0977281f);
//		buf.put(4.9381585f).put(0.4088228f).put(0.668919f).put(4.9782405f).put(0.4088228f).put(0.22357304f).put(4.9281783f).put(0.81490785f).put(0.22132474f).put(4.8884993f).put(0.81490785f).put(0.6621922f);
//		buf.put(4.9782405f).put(0.4088228f).put(0.22357304f).put(4.9782405f).put(0.4088228f).put(-0.22357304f).put(4.9281783f).put(0.81490785f).put(-0.22132474f).put(4.9281783f).put(0.81490785f).put(0.22132474f);
//		buf.put(-4.9281783f).put(0.81490785f).put(-0.22132474f).put(-4.9281783f).put(0.81490785f).put(0.22132474f).put(-4.8451138f).put(1.2155358f).put(0.2175943f).put(-4.8451138f).put(1.2155358f).put(-0.2175943f);
//		buf.put(-4.9281783f).put(0.81490785f).put(0.22132474f).put(-4.8884993f).put(0.81490785f).put(0.6621922f).put(-4.8061037f).put(1.2155358f).put(0.65103096f).put(-4.8451138f).put(1.2155358f).put(0.2175943f);
//		buf.put(-4.8884993f).put(0.81490785f).put(0.6621922f).put(-4.809461f).put(0.81490785f).put(1.0977281f).put(-4.728398f).put(1.2155358f).put(1.0792259f).put(-4.8061037f).put(1.2155358f).put(0.65103096f);
//		buf.put(-4.809461f).put(0.81490785f).put(1.0977281f).put(-4.6917005f).put(0.81490785f).put(1.5244259f).put(-4.6126213f).put(1.2155358f).put(1.4987316f).put(-4.728398f).put(1.2155358f).put(1.0792259f);
//		buf.put(-4.6917005f).put(0.81490785f).put(1.5244259f).put(-4.5361643f).put(0.81490785f).put(1.9388497f).put(-4.4597073f).put(1.2155358f).put(1.9061702f).put(-4.6126213f).put(1.2155358f).put(1.4987316f);
//		buf.put(-4.5361643f).put(0.81490785f).put(1.9388497f).put(-4.3441057f).put(0.81490785f).put(2.3376632f).put(-4.270886f).put(1.2155358f).put(2.2982616f).put(-4.4597073f).put(1.2155358f).put(1.9061702f);
//		buf.put(-4.3441057f).put(0.81490785f).put(2.3376632f).put(-4.117071f).put(0.81490785f).put(2.717655f).put(-4.047678f).put(1.2155358f).put(2.6718488f).put(-4.270886f).put(1.2155358f).put(2.2982616f);
//		buf.put(-4.117071f).put(0.81490785f).put(2.717655f).put(-3.8568885f).put(0.81490785f).put(3.0757658f).put(-3.7918806f).put(1.2155358f).put(3.0239239f).put(-4.047678f).put(1.2155358f).put(2.6718488f);
//		buf.put(-3.8568885f).put(0.81490785f).put(3.0757658f).put(-3.5656524f).put(0.81490785f).put(3.4091127f).put(-3.505553f).put(1.2155358f).put(3.351652f).put(-3.7918806f).put(1.2155358f).put(3.0239239f);
//		buf.put(-3.5656524f).put(0.81490785f).put(3.4091127f).put(-3.2457075f).put(0.81490785f).put(3.7150111f).put(-3.191001f).put(1.2155358f).put(3.6523945f).put(-3.505553f).put(1.2155358f).put(3.351652f);
//		buf.put(-3.2457075f).put(0.81490785f).put(3.7150111f).put(-2.8996303f).put(0.81490785f).put(3.9909985f).put(-2.850757f).put(1.2155358f).put(3.9237301f).put(-3.191001f).put(1.2155358f).put(3.6523945f);
//		buf.put(-2.8996303f).put(0.81490785f).put(3.9909985f).put(-2.5302067f).put(0.81490785f).put(4.234853f).put(-2.48756f).put(1.2155358f).put(4.163474f).put(-2.850757f).put(1.2155358f).put(3.9237301f);
//		buf.put(-2.5302067f).put(0.81490785f).put(4.234853f).put(-2.1404116f).put(0.81490785f).put(4.4446106f).put(-2.1043348f).put(1.2155358f).put(4.3696966f).put(-2.48756f).put(1.2155358f).put(4.163474f);
//		buf.put(-2.1404116f).put(0.81490785f).put(4.4446106f).put(-1.7333832f).put(0.81490785f).put(4.6185827f).put(-1.7041669f).put(1.2155358f).put(4.5407367f).put(-2.1043348f).put(1.2155358f).put(4.3696966f);
//		buf.put(-1.7333832f).put(0.81490785f).put(4.6185827f).put(-1.3123984f).put(0.81490785f).put(4.755369f).put(-1.290278f).put(1.2155358f).put(4.675217f).put(-1.7041669f).put(1.2155358f).put(4.5407367f);
//		buf.put(-1.3123984f).put(0.81490785f).put(4.755369f).put(-0.88084716f).put(0.81490785f).put(4.853868f).put(-0.8660004f).put(1.2155358f).put(4.7720556f).put(-1.290278f).put(1.2155358f).put(4.675217f);
//		buf.put(-0.88084716f).put(0.81490785f).put(4.853868f).put(-0.44220376f).put(0.81490785f).put(4.913286f).put(-0.4347504f).put(1.2155358f).put(4.8304725f).put(-0.8660004f).put(1.2155358f).put(4.7720556f);
//		buf.put(-0.44220376f).put(0.81490785f).put(4.913286f).put(4.9898714E-17f).put(0.81490785f).put(4.9331455f).put(7.44301E-17f).put(1.2155358f).put(4.849997f).put(-0.4347504f).put(1.2155358f).put(4.8304725f);
//		buf.put(4.9898714E-17f).put(0.81490785f).put(4.9331455f).put(0.44220376f).put(0.81490785f).put(4.913286f).put(0.4347504f).put(1.2155358f).put(4.8304725f).put(7.44301E-17f).put(1.2155358f).put(4.849997f);
//		buf.put(0.44220376f).put(0.81490785f).put(4.913286f).put(0.88084716f).put(0.81490785f).put(4.853868f).put(0.8660004f).put(1.2155358f).put(4.7720556f).put(0.4347504f).put(1.2155358f).put(4.8304725f);
//		buf.put(0.88084716f).put(0.81490785f).put(4.853868f).put(1.3123984f).put(0.81490785f).put(4.755369f).put(1.290278f).put(1.2155358f).put(4.675217f).put(0.8660004f).put(1.2155358f).put(4.7720556f);
//		buf.put(1.3123984f).put(0.81490785f).put(4.755369f).put(1.7333832f).put(0.81490785f).put(4.6185827f).put(1.7041669f).put(1.2155358f).put(4.5407367f).put(1.290278f).put(1.2155358f).put(4.675217f);
//		buf.put(1.7333832f).put(0.81490785f).put(4.6185827f).put(2.1404116f).put(0.81490785f).put(4.4446106f).put(2.1043348f).put(1.2155358f).put(4.3696966f).put(1.7041669f).put(1.2155358f).put(4.5407367f);
//		buf.put(2.1404116f).put(0.81490785f).put(4.4446106f).put(2.5302067f).put(0.81490785f).put(4.234853f).put(2.48756f).put(1.2155358f).put(4.163474f).put(2.1043348f).put(1.2155358f).put(4.3696966f);
//		buf.put(2.5302067f).put(0.81490785f).put(4.234853f).put(2.8996303f).put(0.81490785f).put(3.9909985f).put(2.850757f).put(1.2155358f).put(3.9237301f).put(2.48756f).put(1.2155358f).put(4.163474f);
//		buf.put(2.8996303f).put(0.81490785f).put(3.9909985f).put(3.2457075f).put(0.81490785f).put(3.7150111f).put(3.191001f).put(1.2155358f).put(3.6523945f).put(2.850757f).put(1.2155358f).put(3.9237301f);
//		buf.put(3.2457075f).put(0.81490785f).put(3.7150111f).put(3.5656524f).put(0.81490785f).put(3.4091127f).put(3.505553f).put(1.2155358f).put(3.351652f).put(3.191001f).put(1.2155358f).put(3.6523945f);
//		buf.put(3.5656524f).put(0.81490785f).put(3.4091127f).put(3.8568885f).put(0.81490785f).put(3.0757658f).put(3.7918806f).put(1.2155358f).put(3.0239239f).put(3.505553f).put(1.2155358f).put(3.351652f);
//		buf.put(3.8568885f).put(0.81490785f).put(3.0757658f).put(4.117071f).put(0.81490785f).put(2.717655f).put(4.047678f).put(1.2155358f).put(2.6718488f).put(3.7918806f).put(1.2155358f).put(3.0239239f);
//		buf.put(4.117071f).put(0.81490785f).put(2.717655f).put(4.3441057f).put(0.81490785f).put(2.3376632f).put(4.270886f).put(1.2155358f).put(2.2982616f).put(4.047678f).put(1.2155358f).put(2.6718488f);
//		buf.put(4.3441057f).put(0.81490785f).put(2.3376632f).put(4.5361643f).put(0.81490785f).put(1.9388497f).put(4.4597073f).put(1.2155358f).put(1.9061702f).put(4.270886f).put(1.2155358f).put(2.2982616f);
//		buf.put(4.5361643f).put(0.81490785f).put(1.9388497f).put(4.6917005f).put(0.81490785f).put(1.5244259f).put(4.6126213f).put(1.2155358f).put(1.4987316f).put(4.4597073f).put(1.2155358f).put(1.9061702f);
//		buf.put(4.6917005f).put(0.81490785f).put(1.5244259f).put(4.809461f).put(0.81490785f).put(1.0977281f).put(4.728398f).put(1.2155358f).put(1.0792259f).put(4.6126213f).put(1.2155358f).put(1.4987316f);
//		buf.put(4.809461f).put(0.81490785f).put(1.0977281f).put(4.8884993f).put(0.81490785f).put(0.6621922f).put(4.8061037f).put(1.2155358f).put(0.65103096f).put(4.728398f).put(1.2155358f).put(1.0792259f);
//		buf.put(4.8884993f).put(0.81490785f).put(0.6621922f).put(4.9281783f).put(0.81490785f).put(0.22132474f).put(4.8451138f).put(1.2155358f).put(0.2175943f).put(4.8061037f).put(1.2155358f).put(0.65103096f);
//		buf.put(4.9281783f).put(0.81490785f).put(0.22132474f).put(4.9281783f).put(0.81490785f).put(-0.22132474f).put(4.8451138f).put(1.2155358f).put(-0.2175943f).put(4.8451138f).put(1.2155358f).put(0.2175943f);
//		buf.put(-4.8451138f).put(1.2155358f).put(-0.2175943f).put(-4.8451138f).put(1.2155358f).put(0.2175943f).put(-4.729603f).put(1.6080236f).put(0.21240671f).put(-4.729603f).put(1.6080236f).put(-0.21240671f);
//		buf.put(-4.8451138f).put(1.2155358f).put(0.2175943f).put(-4.8061037f).put(1.2155358f).put(0.65103096f).put(-4.691523f).put(1.6080236f).put(0.63550997f).put(-4.729603f).put(1.6080236f).put(0.21240671f);
//		buf.put(-4.8061037f).put(1.2155358f).put(0.65103096f).put(-4.728398f).put(1.2155358f).put(1.0792259f).put(-4.6156697f).put(1.6080236f).put(1.0534965f).put(-4.691523f).put(1.6080236f).put(0.63550997f);
//		buf.put(-4.728398f).put(1.2155358f).put(1.0792259f).put(-4.6126213f).put(1.2155358f).put(1.4987316f).put(-4.5026536f).put(1.6080236f).put(1.4630008f).put(-4.6156697f).put(1.6080236f).put(1.0534965f);
//		buf.put(-4.6126213f).put(1.2155358f).put(1.4987316f).put(-4.4597073f).put(1.2155358f).put(1.9061702f).put(-4.353385f).put(1.6080236f).put(1.860726f).put(-4.5026536f).put(1.6080236f).put(1.4630008f);
//		buf.put(-4.4597073f).put(1.2155358f).put(1.9061702f).put(-4.270886f).put(1.2155358f).put(2.2982616f).put(-4.169065f).put(1.6080236f).put(2.2434697f).put(-4.353385f).put(1.6080236f).put(1.860726f);
//		buf.put(-4.270886f).put(1.2155358f).put(2.2982616f).put(-4.047678f).put(1.2155358f).put(2.6718488f).put(-3.9511786f).put(1.6080236f).put(2.6081502f).put(-4.169065f).put(1.6080236f).put(2.2434697f);
//		buf.put(-4.047678f).put(1.2155358f).put(2.6718488f).put(-3.7918806f).put(1.2155358f).put(3.0239239f).put(-3.7014797f).put(1.6080236f).put(2.9518313f).put(-3.9511786f).put(1.6080236f).put(2.6081502f);
//		buf.put(-3.7918806f).put(1.2155358f).put(3.0239239f).put(-3.505553f).put(1.2155358f).put(3.351652f).put(-3.4219785f).put(1.6080236f).put(3.2717464f).put(-3.7014797f).put(1.6080236f).put(2.9518313f);
//		buf.put(-3.505553f).put(1.2155358f).put(3.351652f).put(-3.191001f).put(1.2155358f).put(3.6523945f).put(-3.1149254f).put(1.6080236f).put(3.565319f).put(-3.4219785f).put(1.6080236f).put(3.2717464f);
//		buf.put(-3.191001f).put(1.2155358f).put(3.6523945f).put(-2.850757f).put(1.2155358f).put(3.9237301f).put(-2.7827928f).put(1.6080236f).put(3.830186f).put(-3.1149254f).put(1.6080236f).put(3.565319f);
//		buf.put(-2.850757f).put(1.2155358f).put(3.9237301f).put(-2.48756f).put(1.2155358f).put(4.163474f).put(-2.428255f).put(1.6080236f).put(4.064214f).put(-2.7827928f).put(1.6080236f).put(3.830186f);
//		buf.put(-2.48756f).put(1.2155358f).put(4.163474f).put(-2.1043348f).put(1.2155358f).put(4.3696966f).put(-2.054166f).put(1.6080236f).put(4.26552f).put(-2.428255f).put(1.6080236f).put(4.064214f);
//		buf.put(-2.1043348f).put(1.2155358f).put(4.3696966f).put(-1.7041669f).put(1.2155358f).put(4.5407367f).put(-1.6635385f).put(1.6080236f).put(4.4324822f).put(-2.054166f).put(1.6080236f).put(4.26552f);
//		buf.put(-1.7041669f).put(1.2155358f).put(4.5407367f).put(-1.290278f).put(1.2155358f).put(4.675217f).put(-1.2595168f).put(1.6080236f).put(4.563757f).put(-1.6635385f).put(1.6080236f).put(4.4324822f);
//		buf.put(-1.290278f).put(1.2155358f).put(4.675217f).put(-0.8660004f).put(1.2155358f).put(4.7720556f).put(-0.84535444f).put(1.6080236f).put(4.6582866f).put(-1.2595168f).put(1.6080236f).put(4.563757f);
//		buf.put(-0.8660004f).put(1.2155358f).put(4.7720556f).put(-0.4347504f).put(1.2155358f).put(4.8304725f).put(-0.42438567f).put(1.6080236f).put(4.715311f).put(-0.84535444f).put(1.6080236f).put(4.6582866f);
//		buf.put(-0.4347504f).put(1.2155358f).put(4.8304725f).put(7.44301E-17f).put(1.2155358f).put(4.849997f).put(9.8463054E-17f).put(1.6080236f).put(4.73437f).put(-0.42438567f).put(1.6080236f).put(4.715311f);
//		buf.put(7.44301E-17f).put(1.2155358f).put(4.849997f).put(0.4347504f).put(1.2155358f).put(4.8304725f).put(0.42438567f).put(1.6080236f).put(4.715311f).put(9.8463054E-17f).put(1.6080236f).put(4.73437f);
//		buf.put(0.4347504f).put(1.2155358f).put(4.8304725f).put(0.8660004f).put(1.2155358f).put(4.7720556f).put(0.84535444f).put(1.6080236f).put(4.6582866f).put(0.42438567f).put(1.6080236f).put(4.715311f);
//		buf.put(0.8660004f).put(1.2155358f).put(4.7720556f).put(1.290278f).put(1.2155358f).put(4.675217f).put(1.2595168f).put(1.6080236f).put(4.563757f).put(0.84535444f).put(1.6080236f).put(4.6582866f);
//		buf.put(1.290278f).put(1.2155358f).put(4.675217f).put(1.7041669f).put(1.2155358f).put(4.5407367f).put(1.6635385f).put(1.6080236f).put(4.4324822f).put(1.2595168f).put(1.6080236f).put(4.563757f);
//		buf.put(1.7041669f).put(1.2155358f).put(4.5407367f).put(2.1043348f).put(1.2155358f).put(4.3696966f).put(2.054166f).put(1.6080236f).put(4.26552f).put(1.6635385f).put(1.6080236f).put(4.4324822f);
//		buf.put(2.1043348f).put(1.2155358f).put(4.3696966f).put(2.48756f).put(1.2155358f).put(4.163474f).put(2.428255f).put(1.6080236f).put(4.064214f).put(2.054166f).put(1.6080236f).put(4.26552f);
//		buf.put(2.48756f).put(1.2155358f).put(4.163474f).put(2.850757f).put(1.2155358f).put(3.9237301f).put(2.7827928f).put(1.6080236f).put(3.830186f).put(2.428255f).put(1.6080236f).put(4.064214f);
//		buf.put(2.850757f).put(1.2155358f).put(3.9237301f).put(3.191001f).put(1.2155358f).put(3.6523945f).put(3.1149254f).put(1.6080236f).put(3.565319f).put(2.7827928f).put(1.6080236f).put(3.830186f);
//		buf.put(3.191001f).put(1.2155358f).put(3.6523945f).put(3.505553f).put(1.2155358f).put(3.351652f).put(3.4219785f).put(1.6080236f).put(3.2717464f).put(3.1149254f).put(1.6080236f).put(3.565319f);
//		buf.put(3.505553f).put(1.2155358f).put(3.351652f).put(3.7918806f).put(1.2155358f).put(3.0239239f).put(3.7014797f).put(1.6080236f).put(2.9518313f).put(3.4219785f).put(1.6080236f).put(3.2717464f);
//		buf.put(3.7918806f).put(1.2155358f).put(3.0239239f).put(4.047678f).put(1.2155358f).put(2.6718488f).put(3.9511786f).put(1.6080236f).put(2.6081502f).put(3.7014797f).put(1.6080236f).put(2.9518313f);
//		buf.put(4.047678f).put(1.2155358f).put(2.6718488f).put(4.270886f).put(1.2155358f).put(2.2982616f).put(4.169065f).put(1.6080236f).put(2.2434697f).put(3.9511786f).put(1.6080236f).put(2.6081502f);
//		buf.put(4.270886f).put(1.2155358f).put(2.2982616f).put(4.4597073f).put(1.2155358f).put(1.9061702f).put(4.353385f).put(1.6080236f).put(1.860726f).put(4.169065f).put(1.6080236f).put(2.2434697f);
//		buf.put(4.4597073f).put(1.2155358f).put(1.9061702f).put(4.6126213f).put(1.2155358f).put(1.4987316f).put(4.5026536f).put(1.6080236f).put(1.4630008f).put(4.353385f).put(1.6080236f).put(1.860726f);
//		buf.put(4.6126213f).put(1.2155358f).put(1.4987316f).put(4.728398f).put(1.2155358f).put(1.0792259f).put(4.6156697f).put(1.6080236f).put(1.0534965f).put(4.5026536f).put(1.6080236f).put(1.4630008f);
//		buf.put(4.728398f).put(1.2155358f).put(1.0792259f).put(4.8061037f).put(1.2155358f).put(0.65103096f).put(4.691523f).put(1.6080236f).put(0.63550997f).put(4.6156697f).put(1.6080236f).put(1.0534965f);
//		buf.put(4.8061037f).put(1.2155358f).put(0.65103096f).put(4.8451138f).put(1.2155358f).put(0.2175943f).put(4.729603f).put(1.6080236f).put(0.21240671f).put(4.691523f).put(1.6080236f).put(0.63550997f);
//		buf.put(4.8451138f).put(1.2155358f).put(0.2175943f).put(4.8451138f).put(1.2155358f).put(-0.2175943f).put(4.729603f).put(1.6080236f).put(-0.21240671f).put(4.729603f).put(1.6080236f).put(0.21240671f);
//		buf.put(-4.729603f).put(1.6080236f).put(-0.21240671f).put(-4.729603f).put(1.6080236f).put(0.21240671f).put(-4.58242f).put(1.9897431f).put(0.2057967f).put(-4.58242f).put(1.9897431f).put(-0.2057967f);
//		buf.put(-4.729603f).put(1.6080236f).put(0.21240671f).put(-4.691523f).put(1.6080236f).put(0.63550997f).put(-4.5455246f).put(1.9897431f).put(0.61573315f).put(-4.58242f).put(1.9897431f).put(0.2057967f);
//		buf.put(-4.691523f).put(1.6080236f).put(0.63550997f).put(-4.6156697f).put(1.6080236f).put(1.0534965f).put(-4.472032f).put(1.9897431f).put(1.0207121f).put(-4.5455246f).put(1.9897431f).put(0.61573315f);
//		buf.put(-4.6156697f).put(1.6080236f).put(1.0534965f).put(-4.5026536f).put(1.6080236f).put(1.4630008f).put(-4.3625326f).put(1.9897431f).put(1.4174728f).put(-4.472032f).put(1.9897431f).put(1.0207121f);
//		buf.put(-4.5026536f).put(1.6080236f).put(1.4630008f).put(-4.353385f).put(1.6080236f).put(1.860726f).put(-4.2179093f).put(1.9897431f).put(1.8028209f).put(-4.3625326f).put(1.9897431f).put(1.4174728f);
//		buf.put(-4.353385f).put(1.6080236f).put(1.860726f).put(-4.169065f).put(1.6080236f).put(2.2434697f).put(-4.0393257f).put(1.9897431f).put(2.1736538f).put(-4.2179093f).put(1.9897431f).put(1.8028209f);
//		buf.put(-4.169065f).put(1.6080236f).put(2.2434697f).put(-3.9511786f).put(1.6080236f).put(2.6081502f).put(-3.8282197f).put(1.9897431f).put(2.5269856f).put(-4.0393257f).put(1.9897431f).put(2.1736538f);
//		buf.put(-3.9511786f).put(1.6080236f).put(2.6081502f).put(-3.7014797f).put(1.6080236f).put(2.9518313f).put(-3.586291f).put(1.9897431f).put(2.8599718f).put(-3.8282197f).put(1.9897431f).put(2.5269856f);
//		buf.put(-3.7014797f).put(1.6080236f).put(2.9518313f).put(-3.4219785f).put(1.6080236f).put(3.2717464f).put(-3.3154879f).put(1.9897431f).put(3.169931f).put(-3.586291f).put(1.9897431f).put(2.8599718f);
//		buf.put(-3.4219785f).put(1.6080236f).put(3.2717464f).put(-3.1149254f).put(1.6080236f).put(3.565319f).put(-3.0179904f).put(1.9897431f).put(3.4543679f).put(-3.3154879f).put(1.9897431f).put(3.169931f);
//		buf.put(-3.1149254f).put(1.6080236f).put(3.565319f).put(-2.7827928f).put(1.6080236f).put(3.830186f).put(-2.6961937f).put(1.9897431f).put(3.710992f).put(-3.0179904f).put(1.9897431f).put(3.4543679f);
//		buf.put(-2.7827928f).put(1.6080236f).put(3.830186f).put(-2.428255f).put(1.6080236f).put(4.064214f).put(-2.3526888f).put(1.9897431f).put(3.9377377f).put(-2.6961937f).put(1.9897431f).put(3.710992f);
//		buf.put(-2.428255f).put(1.6080236f).put(4.064214f).put(-2.054166f).put(1.6080236f).put(4.26552f).put(-1.9902414f).put(1.9897431f).put(4.1327786f).put(-2.3526888f).put(1.9897431f).put(3.9377377f);
//		buf.put(-2.054166f).put(1.6080236f).put(4.26552f).put(-1.6635385f).put(1.6080236f).put(4.4324822f).put(-1.6117698f).put(1.9897431f).put(4.294545f).put(-1.9902414f).put(1.9897431f).put(4.1327786f);
//		buf.put(-1.6635385f).put(1.6080236f).put(4.4324822f).put(-1.2595168f).put(1.6080236f).put(4.563757f).put(-1.2203213f).put(1.9897431f).put(4.421735f).put(-1.6117698f).put(1.9897431f).put(4.294545f);
//		buf.put(-1.2595168f).put(1.6080236f).put(4.563757f).put(-0.84535444f).put(1.6080236f).put(4.6582866f).put(-0.81904733f).put(1.9897431f).put(4.513323f).put(-1.2203213f).put(1.9897431f).put(4.421735f);
//		buf.put(-0.84535444f).put(1.6080236f).put(4.6582866f).put(-0.42438567f).put(1.6080236f).put(4.715311f).put(-0.41117895f).put(1.9897431f).put(4.5685725f).put(-0.81904733f).put(1.9897431f).put(4.513323f);
//		buf.put(-0.42438567f).put(1.6080236f).put(4.715311f).put(9.8463054E-17f).put(1.6080236f).put(4.73437f).put(1.2183662E-16f).put(1.9897431f).put(4.5870385f).put(-0.41117895f).put(1.9897431f).put(4.5685725f);
//		buf.put(9.8463054E-17f).put(1.6080236f).put(4.73437f).put(0.42438567f).put(1.6080236f).put(4.715311f).put(0.41117895f).put(1.9897431f).put(4.5685725f).put(1.2183662E-16f).put(1.9897431f).put(4.5870385f);
//		buf.put(0.42438567f).put(1.6080236f).put(4.715311f).put(0.84535444f).put(1.6080236f).put(4.6582866f).put(0.81904733f).put(1.9897431f).put(4.513323f).put(0.41117895f).put(1.9897431f).put(4.5685725f);
//		buf.put(0.84535444f).put(1.6080236f).put(4.6582866f).put(1.2595168f).put(1.6080236f).put(4.563757f).put(1.2203213f).put(1.9897431f).put(4.421735f).put(0.81904733f).put(1.9897431f).put(4.513323f);
//		buf.put(1.2595168f).put(1.6080236f).put(4.563757f).put(1.6635385f).put(1.6080236f).put(4.4324822f).put(1.6117698f).put(1.9897431f).put(4.294545f).put(1.2203213f).put(1.9897431f).put(4.421735f);
//		buf.put(1.6635385f).put(1.6080236f).put(4.4324822f).put(2.054166f).put(1.6080236f).put(4.26552f).put(1.9902414f).put(1.9897431f).put(4.1327786f).put(1.6117698f).put(1.9897431f).put(4.294545f);
//		buf.put(2.054166f).put(1.6080236f).put(4.26552f).put(2.428255f).put(1.6080236f).put(4.064214f).put(2.3526888f).put(1.9897431f).put(3.9377377f).put(1.9902414f).put(1.9897431f).put(4.1327786f);
//		buf.put(2.428255f).put(1.6080236f).put(4.064214f).put(2.7827928f).put(1.6080236f).put(3.830186f).put(2.6961937f).put(1.9897431f).put(3.710992f).put(2.3526888f).put(1.9897431f).put(3.9377377f);
//		buf.put(2.7827928f).put(1.6080236f).put(3.830186f).put(3.1149254f).put(1.6080236f).put(3.565319f).put(3.0179904f).put(1.9897431f).put(3.4543679f).put(2.6961937f).put(1.9897431f).put(3.710992f);
//		buf.put(3.1149254f).put(1.6080236f).put(3.565319f).put(3.4219785f).put(1.6080236f).put(3.2717464f).put(3.3154879f).put(1.9897431f).put(3.169931f).put(3.0179904f).put(1.9897431f).put(3.4543679f);
//		buf.put(3.4219785f).put(1.6080236f).put(3.2717464f).put(3.7014797f).put(1.6080236f).put(2.9518313f).put(3.586291f).put(1.9897431f).put(2.8599718f).put(3.3154879f).put(1.9897431f).put(3.169931f);
//		buf.put(3.7014797f).put(1.6080236f).put(2.9518313f).put(3.9511786f).put(1.6080236f).put(2.6081502f).put(3.8282197f).put(1.9897431f).put(2.5269856f).put(3.586291f).put(1.9897431f).put(2.8599718f);
//		buf.put(3.9511786f).put(1.6080236f).put(2.6081502f).put(4.169065f).put(1.6080236f).put(2.2434697f).put(4.0393257f).put(1.9897431f).put(2.1736538f).put(3.8282197f).put(1.9897431f).put(2.5269856f);
//		buf.put(4.169065f).put(1.6080236f).put(2.2434697f).put(4.353385f).put(1.6080236f).put(1.860726f).put(4.2179093f).put(1.9897431f).put(1.8028209f).put(4.0393257f).put(1.9897431f).put(2.1736538f);
//		buf.put(4.353385f).put(1.6080236f).put(1.860726f).put(4.5026536f).put(1.6080236f).put(1.4630008f).put(4.3625326f).put(1.9897431f).put(1.4174728f).put(4.2179093f).put(1.9897431f).put(1.8028209f);
//		buf.put(4.5026536f).put(1.6080236f).put(1.4630008f).put(4.6156697f).put(1.6080236f).put(1.0534965f).put(4.472032f).put(1.9897431f).put(1.0207121f).put(4.3625326f).put(1.9897431f).put(1.4174728f);
//		buf.put(4.6156697f).put(1.6080236f).put(1.0534965f).put(4.691523f).put(1.6080236f).put(0.63550997f).put(4.5455246f).put(1.9897431f).put(0.61573315f).put(4.472032f).put(1.9897431f).put(1.0207121f);
//		buf.put(4.691523f).put(1.6080236f).put(0.63550997f).put(4.729603f).put(1.6080236f).put(0.21240671f).put(4.58242f).put(1.9897431f).put(0.2057967f).put(4.5455246f).put(1.9897431f).put(0.61573315f);
//		buf.put(4.729603f).put(1.6080236f).put(0.21240671f).put(4.729603f).put(1.6080236f).put(-0.21240671f).put(4.58242f).put(1.9897431f).put(-0.2057967f).put(4.58242f).put(1.9897431f).put(0.2057967f);
//		
//		buf.limit(4320);
//		sunRegion.getMeshData().updateVertexCount();
//		sunRegion.updateModelBound();
//		sunRegion.updateWorldBound(true);
//		
//		final Ray3 pickRay = new Ray3(new Vector3(0, 0, 10), new Vector3(0, 0, -1));
//		
//		 final FloatBuffer buf1 = line.getMeshData().getVertexBuffer();
//		 buf1.rewind();
//		 buf1.put(pickRay.getOrigin().getXf()).put(pickRay.getOrigin().getYf()).put(pickRay.getOrigin().getZf());
//		 final Vector3 pp = pickRay.getOrigin().add(pickRay.getDirection().multiply(20, null), null);
//		 buf1.put(pp.getXf()).put(pp.getYf()).put(pp.getZf());
//		
//		pickResults.clear();
//		PickingUtil.findPick(sunRegion, pickRay, pickResults);
//		if (pickResults.getNumber() > 0) {
//			final IntersectionRecord intersectionRecord = pickResults.getPickData(0).getIntersectionRecord();
//			System.out.println("Number of intersections (should be 1): " + intersectionRecord.getNumberOfIntersections());
//			for (int i=0; i<intersectionRecord.getNumberOfIntersections(); i++)
//				System.out.println(intersectionRecord.getIntersectionPoint(i));
//		}
		
		
		final FloatBuffer buf = sunRegion.getMeshData().getVertexBuffer();
		
		buf.limit(buf.capacity());
		buf.rewind();
		final double declinationStep = 2.0 * tiltAngle / 10.0;
		final double hourStep = 2.0 * Math.PI / 70.0;
		int limit = 0;
		for (double declinationAngle = -tiltAngle; declinationAngle < tiltAngle - declinationStep / 2.0; declinationAngle += declinationStep) {
			for (double hourAngle = -Math.PI; hourAngle < Math.PI - hourStep / 2.0; hourAngle += hourStep) {
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
					System.out.println("buf.put(" + v1.getXf() + "f).put(" + v1.getYf() + "f).put(" + v1.getZf() + "f).put(" + v2.getXf() + "f).put(" + v2.getYf() + "f).put(" + v2.getZf() + "f).put(" + v3.getXf() + "f).put(" + v3.getYf() + "f).put(" + v3.getZf() + "f).put(" + v4.getXf() + "f).put(" + v4.getYf() + "f).put(" + v4.getZf() + "f);");
					limit += 12;
				}
			}
		}
		buf.limit(limit);
		sunRegion.getMeshData().updateVertexCount();
		sunRegion.updateModelBound();
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
