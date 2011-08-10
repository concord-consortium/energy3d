package org.concord.energy3d.shapes;

import org.concord.energy3d.util.FontManager;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.AutoScale;

public abstract class Annotation extends Node {	
	protected final Mesh mesh;
	
	public static BMText makeNewLabel() {
		final BMText label = new BMText("Annotation Label", "0.0", FontManager.getInstance().getAnnotationFont(), BMText.Align.Center, BMText.Justify.Center);
		label.setTextColor(ColorRGBA.BLACK);
//		label.setAutoScale(AutoScale.Off);
//		label.setFontScale(0.12);
		label.setFontScale(0.5);
		label.setAutoRotate(false);
		return label;
	}
	
	public Annotation(final Mesh mesh) {
		super();
		this.mesh = mesh;
		this.attachChild(mesh);
		mesh.setDefaultColor(ColorRGBA.BLACK);
	}
}