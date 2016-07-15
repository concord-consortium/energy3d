package org.concord.energy3d.shapes;

import org.concord.energy3d.util.FontManager;
import org.concord.energy3d.util.Util;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.scenegraph.Line;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.AutoFade;
import com.ardor3d.ui.text.BMText.AutoScale;

public abstract class Annotation extends Node {

	protected final Line mesh;
	protected final BMText label = makeNewLabel(1);

	public void setFontSize(final double fontSize) {
		label.setFontScale(fontSize);
	}

	public static BMText makeNewLabel(double fontSize) {
		final BMText label = new BMText("Annotation Label", "", FontManager.getInstance().getAnnotationFont(), BMText.Align.Center, BMText.Justify.Center);
		label.setTextColor(ColorRGBA.BLACK);
		label.setAutoScale(AutoScale.Off);
		label.setFontScale(fontSize);
		label.setAutoRotate(false);
		label.setAutoFade(AutoFade.Off);
		label.setModelBound(null);
		label.updateWorldTransform(true);
		label.getSceneHints().setRenderBucketType(RenderBucketType.PostBucket);
		return label;
	}

	public Annotation(final Line mesh) {
		super();
		mesh.setDefaultColor(ColorRGBA.BLACK);
		mesh.setModelBound(null);
		this.mesh = mesh;
		Util.disablePickShadowLight(mesh);
		attachChild(mesh);
	}

	public void setColor(final ReadOnlyColorRGBA color) {
		mesh.setDefaultColor(color);
		label.setTextColor(color);
	}

	public void setLineWidth(final float lineWidth) {
		mesh.setLineWidth(lineWidth);
	}

	public void setStipplePattern(short stipplePattern) {
		mesh.setStipplePattern(stipplePattern);
	}

	public abstract void draw();
}