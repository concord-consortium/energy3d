package org.concord.energy3d.shapes;

import java.nio.FloatBuffer;

import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.util.Util;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.extension.effect.bloom.BloomRenderPass;
import com.ardor3d.light.DirectionalLight;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Transform;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.pass.BasicPassManager;
import com.ardor3d.renderer.state.ClipState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
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
	private double tiltAngle = 50.0 / 180 * Math.PI;
	private double offset = 0; //-Math.PI / 2;
	private double baseAngle = 0;
	private double sunAngle = 90;
	private double hourAngle;	
	private double declinationAngle;
	private double observerLatitude;
	private final Line sunPath;
	
	public Heliodon(final Node scene, final DirectionalLight light, final BasicPassManager passManager) {
		this.light = light;
		this.bloomRenderPass = new BloomRenderPass(SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera(), 4);
		passManager.add(bloomRenderPass);
//		bloomRenderPass.add(sun);
		
		sunPath = new Line("Sun Path", BufferUtils.createVector3Buffer(100), null, null, null);
		sunPath.getMeshData().setIndexMode(IndexMode.LineStrip);
		
		
		root.getSceneHints().setCullHint(CullHint.Always);
		final Cylinder cyl = new Cylinder("Curve", 10, 50, 5, 0.3);
		final Transform trans = new Transform();
		trans.setMatrix(new Matrix3().fromAngleAxis(Math.PI / 2, Vector3.UNIT_X));
		cyl.setDefaultColor(ColorRGBA.YELLOW);
		cyl.setTransform(trans);
		sunRing.attachChild(cyl);
//		sunRing.setTranslation(0, offset, 0);
//		sunRing.setRotation(new Matrix3().fromAngleAxis(-tiltAngle, Vector3.UNIT_X));


		final Cylinder baseCyl = new Cylinder("Base", 10, 50, 5, 0.2);
		baseCyl.setTranslation(0, 0, 0.1);
		root.attachChild(baseCyl);

		sun.setTranslation(0, 0, 5);
//		sunRot.attachChild(sun);
		sunRing.attachChild(sunRot);
		root.attachChild(sunRing);
		root.attachChild(sun);
		drawSunPath();
		root.attachChild(sunPath);
		scene.attachChild(root);

//		reverseNormals(sun.getMeshData().getNormalBuffer());

		final MaterialState material = new MaterialState();
		material.setEmissive(ColorRGBA.WHITE);
		sun.setRenderState(material);		
		
		final ClipState cs = new ClipState();
		cs.setEnableClipPlane(0, true);
//		cs.setClipPlaneEquation(0, 0, 0, 1, -0.19);
		cs.setClipPlaneEquation(0, 0, 0, 1, 0);
		cyl.setRenderState(cs);
		sunPath.setRenderState(cs);
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

//	public void setOffset(double offsetAngle) {
//		final double min = (50 - 23.5) / 180 * Math.PI;
//		if (offsetAngle < min)
//			offsetAngle = min;
//		final double max = (50 + 23.5) / 180 * Math.PI;
//		if (offsetAngle > max)
//			offsetAngle = max;
//		this.offset = offsetAngle;		
////		this.offset = offset;
////		final int intOffset = (int)offset;
////		final double t;
////		if (intOffset % 2 == 0)
////			t = offset - intOffset;
////		else
////			t = 1-(offset - intOffset);
////		sunRing.setTranslation(0, -2+4*t, 0);
//		
//		System.out.println("Offset Angle =" + offsetAngle);
//		final double tiltAngle = Math.PI / 2 - this.tiltAngle;
//		final double angle = (Math.PI - tiltAngle - offsetAngle) / 2;
//		final double radius = 5;
//		final double d = radius * Math.sin(tiltAngle - offsetAngle) / Math.sin(offsetAngle + angle);
//		final double x = d * Math.sin(Math.PI / 2 - angle);
//		final double y = -d * Math.cos(Math.PI / 2 - angle);
//		sunRing.setTranslation(0, x, y);
//		
//		System.out.println("(x,y) = (" + x + "," + y + ")");
//		
//		updateLightDirection();
//		
//	}
	
//	public void setOffset(double offsetAngle) {
////		final double min = (50 - 23.5) / 180 * Math.PI;
////		if (offsetAngle < min)
////			offsetAngle = min;
////		final double max = (50 + 23.5) / 180 * Math.PI;
////		if (offsetAngle > max)
////			offsetAngle = max;
//		this.offset = offsetAngle;		
////		this.offset = offset;
////		final int intOffset = (int)offset;
////		final double t;
////		if (intOffset % 2 == 0)
////			t = offset - intOffset;
////		else
////			t = 1-(offset - intOffset);
////		sunRing.setTranslation(0, -2+4*t, 0);
//		
////		offsetAngle = Math.PI / 2;
//		
//		System.out.println("Offset Angle =" + offsetAngle / Math.PI * 180);
//		final double tiltAngle = Math.PI / 2 - this.tiltAngle;
//		
//		final double radius = 5;
//		final double newRadius = radius; // * Math.sin(offsetAngle + Math.PI / 2 - tiltAngle);
//		final double d = Math.sqrt(Math.pow(radius, 2) - Math.pow(newRadius, 2));		
//		final double angle = Math.asin(2 * newRadius * Math.sin(tiltAngle - offsetAngle) / radius) - offsetAngle;					
//		final double x = d * Math.sin(Math.PI / 2 - angle);
//		final double y = -d * Math.cos(Math.PI / 2 - angle);
//		sunRing.setTranslation(0, x, y);
////		sunRing.setScale(newRadius / radius);
//		
//		System.out.println("new radius = " + newRadius);
//		System.out.println("(x,y) = (" + x + "," + y + ")");
//		
//		updateLightDirection();
//		
//	}	
	
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
		final double maxDeclination = 23.45 / 180 * Math.PI;
		this.declinationAngle = toPlusMinusPIRange(declinationAngle, -maxDeclination, maxDeclination);	
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
	
	
//	public void computeSunLocation(final double hourAngle_, final double declinationAngle, final double observerLatitude) {
//		this.hourAngle += hourAngle_ / 100.0;
//		
////		final double altitudeAngle = Math.asin(Math.sin(hourAngle) * Math.cos(declinationAngle) * Math.cos(observerLatitude) + Math.sin(declinationAngle) * Math.sin(observerLatitude));
////		double azimuthAngle = Math.asin(-Math.sin(hourAngle) * Math.cos(declinationAngle) / Math.cos(altitudeAngle));
//		
//		final double altitudeAngle = Math.asin(Math.sin(declinationAngle) * Math.sin(observerLatitude) + Math.cos(declinationAngle) * Math.cos(hourAngle) * Math.cos(observerLatitude));
//		final double azimuthAngle = Math.asin(Math.sin(hourAngle) * Math.cos(declinationAngle) / Math.cos(altitudeAngle));
//
//		
////		if (Double.isNaN(azimuthAngle))
////			azimuthAngle = 0;
//		final double r = 5;
//		final double x = r * Math.cos(azimuthAngle) * Math.sin(Math.PI / 2 - altitudeAngle);
//		final double y = r * Math.sin(azimuthAngle) * Math.sin(Math.PI / 2 - altitudeAngle);
//		final double z = r * Math.cos(Math.PI / 2 - altitudeAngle);
//		
//		System.out.println("houseAngle = " + toDegree(hourAngle) + ", altitudeAngle = " + toDegree(altitudeAngle) + ", azimuthAngle = " + toDegree(azimuthAngle) + " (" + x + ", " + y + ", " + z + ")");
//		if (!Double.isNaN(azimuthAngle))
//			sun.setTranslation(x, y, z);
//	}
	
	private Vector3 computeSunLocation(final double hourAngle, final double declinationAngle, final double observerLatitude) {
//		final double add = (declinationAngle < 0 ? Math.PI : 0) + (observerLatitude < 0 ? Math.PI : 0);
//		final double mul = (declinationAngle < 0 ? -1 : 1) * (observerLatitude < 0 ? -1 : 1);
//		final double add = (declinationAngle < 0 || observerLatitude < 0? Math.PI : 0);
		final double add = (declinationAngle < 0) ? Math.PI : 0;
//		final double mul = (declinationAngle < 0 || observerLatitude < 0? -1 : 1);		
		final double altitudeAngle = Math.asin(Math.sin(declinationAngle) * Math.sin(observerLatitude) + Math.cos(declinationAngle) * Math.cos(hourAngle) * Math.cos(observerLatitude));
//		final double altitudeAngle = add + mul * Math.asin(Math.sin(declinationAngle) * Math.sin(observerLatitude) + Math.cos(declinationAngle) * Math.cos(hourAngle) * Math.cos(observerLatitude));
		
//		final double azimuthAsin = Math.sin(hourAngle) * Math.cos(declinationAngle) / Math.cos(altitudeAngle);		
//		final double azimuthAngle;
//		if (azimuthAsin < -1)
//			azimuthAngle = (-Math.PI / 2);
//		else if (azimuthAsin > 1)
//			azimuthAngle = Math.PI / 2;
//		else
//			azimuthAngle = Math.asin(azimuthAsin);
		
		final double x_azm = Math.sin(hourAngle) * Math.cos(declinationAngle);
		final double y_azm = (-(Math.cos(hourAngle))*Math.cos(declinationAngle)*Math.sin(observerLatitude))+(Math.cos(observerLatitude)* Math.sin(declinationAngle));
		final double azimuthAngle = Math.atan2(y_azm, x_azm);		
		
		final double r = 5;
		final double x = r * Math.cos(azimuthAngle) * Math.sin(Math.PI / 2 - altitudeAngle);
		final double y = r * Math.sin(azimuthAngle) * Math.sin(Math.PI / 2 - altitudeAngle);
		final double z = r * Math.cos(Math.PI / 2 - altitudeAngle);
		
		System.out.println("houseAngle = " + toDegree(hourAngle) + ", declinationAngle = " + toDegree(declinationAngle) + ", observerLatitude = " + toDegree(observerLatitude) + " --> altitudeAngle = " + toDegree(altitudeAngle) + ", azimuthAngle = " + toDegree(azimuthAngle) + " (" + x + ", " + y + ", " + z + ")");
		
		return new Vector3(x, y, z);
	}
	
	private double toPlusMinusPIRange(final double radian, double min, double max) {
		final double twoPI = Math.PI * 2.0;
		double result = radian -(int)(radian / twoPI) * twoPI;
		if (Math.abs(result) > Math.PI)
			result = -Math.signum(result) * (twoPI - Math.abs(result));
		if (result < min)
			result = min;
		else if (result > max)
			result = max;
		return result;
	}

	private int toDegree(final double radian) {
		return (int)(radian / Math.PI * 180);
	}
	
	private void drawSunPath() {
		final FloatBuffer vertexBuffer = sunPath.getMeshData().getVertexBuffer();
		vertexBuffer.rewind();		
		final double from = -Math.PI;
		final double to = Math.PI;
		final double step = (to - from) / (vertexBuffer.capacity() / 3.0 - 1);
		for (double hourAngle = from; hourAngle < to; hourAngle += step) {
			final Vector3 v = computeSunLocation(hourAngle, declinationAngle, observerLatitude);
			vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		}
		while (vertexBuffer.hasRemaining()) {
			final Vector3 v = computeSunLocation(to, declinationAngle, observerLatitude);
			vertexBuffer.put(v.getXf()).put(v.getYf()).put(v.getZf());
		}
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
		drawSunPath();
		drawSun();
	}
}
