package org.concord.energy3d.shapes;

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
import com.ardor3d.renderer.pass.BasicPassManager;
import com.ardor3d.renderer.state.ClipState;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.hint.CullHint;
import com.ardor3d.scenegraph.shape.Cylinder;
import com.ardor3d.scenegraph.shape.Sphere;

public class Heliodon {
	private final Node root = new Node("Heliodon Root");
	private final Node sunRing = new Node("Sun Ring");
	private final Node sunRot = new Node("Sun Rot");
	private final Spatial sun = new Sphere("Sun", 20, 20, 0.3);
	private final DirectionalLight light;
	private final BloomRenderPass bloomRenderPass;
	private double tiltAngle = 23.4;
	private double offset = 1;
	private double baseAngle = 0;
	private double sunAngle = 90;
	
	public Heliodon(final Node scene, final DirectionalLight light, final BasicPassManager passManager) {
		this.light = light;
		this.bloomRenderPass = new BloomRenderPass(SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera(), 4);
		passManager.add(bloomRenderPass);
		bloomRenderPass.add(sun);
		
		root.getSceneHints().setCullHint(CullHint.Always);
		final Cylinder cyl = new Cylinder("Curve", 10, 50, 5, 0.3);
		final Transform trans = new Transform();
		trans.setMatrix(new Matrix3().fromAngleAxis(Math.PI / 2, Vector3.UNIT_X));
		cyl.setDefaultColor(ColorRGBA.YELLOW);
		cyl.setTransform(trans);
		sunRing.attachChild(cyl);
		sunRing.setTranslation(0, offset, 0);
		sunRing.setRotation(new Matrix3().fromAngleAxis(-tiltAngle * Math.PI / 180, Vector3.UNIT_X));

		final ClipState cs = new ClipState();
		cs.setEnableClipPlane(0, true);
		cs.setClipPlaneEquation(0, 0, 0, 1, -0.19);
		cyl.setRenderState(cs);

		final Cylinder baseCyl = new Cylinder("Base", 10, 50, 5, 0.2);
		baseCyl.setTranslation(0, 0, 0.1);
		root.attachChild(baseCyl);

		sun.setTranslation(0, 0, 5);
		sunRot.attachChild(sun);
		sunRing.attachChild(sunRot);
		root.attachChild(sunRing);
		scene.attachChild(root);

//		reverseNormals(sun.getMeshData().getNormalBuffer());

		final MaterialState material = new MaterialState();
		material.setEmissive(ColorRGBA.WHITE);
		sun.setRenderState(material);		
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

	public void setOffset(final double offset) {
		this.offset = offset;
		final int intOffset = (int)offset;
		final double t;
		if (intOffset % 2 == 0)
			t = offset - intOffset;
		else
			t = 1-(offset - intOffset);
		sunRing.setTranslation(0, -2+4*t, 0);
		updateLightDirection();
	}

	public double getBaseAngle() {
		return baseAngle;
	}

	public void setBaseAngle(final double baseAngle) {
		this.baseAngle = baseAngle % 360;
		root.setRotation(new Matrix3().fromAngleAxis(baseAngle * Math.PI / 180, Vector3.UNIT_Z));
		updateLightDirection();
		
	}

	public double getSunAngle() {
		return sunAngle;
	}

	public void setSunAngle(final double sunAngle) {
		this.sunAngle = sunAngle;
		sunRot.setRotation(new Matrix3().fromAngleAxis((-90 + sunAngle) * Math.PI / 180, Vector3.UNIT_Y));
		updateLightDirection();
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
	
	private void updateLightDirection() {
//		sunHeliodon.updateWorldTransform(true);
		light.setDirection(sun.getWorldTranslation().negate(null));		
	}
}
