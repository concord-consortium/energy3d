package org.concord.energy3d.shapes;

import org.concord.energy3d.util.FontManager;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.AutoScale;

public abstract class Annotation extends Node {
	private static double fontSize = 0.1; 
	protected final Mesh mesh;
	protected final BMText label = makeNewLabel();
	
	public static void setFontSize(double fontSize) {
		Annotation.fontSize = fontSize;
	}
	
	public static BMText makeNewLabel() {
		final BMText label = new BMText("Annotation Label", "", FontManager.getInstance().getAnnotationFont(), BMText.Align.Center, BMText.Justify.Center);
		label.setTextColor(ColorRGBA.BLACK);
		label.setAutoScale(AutoScale.Off);
		label.setFontScale(fontSize);
		label.setAutoRotate(false);
		label.setModelBound(new BoundingBox());
		label.updateWorldTransform(true);
		return label;
	}
	
	public Annotation(final Mesh mesh) {
		super();		
		this.mesh = mesh;
		mesh.setModelBound(new BoundingBox());
		mesh.setDefaultColor(ColorRGBA.BLACK);
		this.attachChild(mesh);
	}

	
//	protected void updateTextSize() {
//		final BoundingBox bounds = (BoundingBox) Scene.getInstance().getOriginalHouseRoot().getWorldBound();
//		if (bounds != null) {
//			final double size = Math.max(bounds.getXExtent(), Math.max(bounds.getYExtent(), bounds.getZExtent()));
//			label.setFontScale(size / 20.0);
//		}
//	}
}