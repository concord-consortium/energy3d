package org.concord.energy3d.model;

import java.io.IOException;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.ui.text.BMFont;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.ResourceSource;

public abstract class Annotation extends Node {
	protected static final BMFont font;
	protected final BMText label = new BMText("textSpatial1", "0.0", font, BMText.Align.Center, BMText.Justify.Center);
	protected final Mesh mesh = getMesh(); // = new Line("Size annotation lines", BufferUtils.createVector3Buffer(12), null, null, null);
	protected final HousePart housePart;
	
	static {
		final String file = "fonts/f6.fnt";
		final ResourceSource url = ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE, file);
		BMFont f = null;
		try {
			f = new BMFont(url, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		font = f;
	}
	
	public Annotation(HousePart housePart) {
		super();
		this.housePart = housePart;
		this.attachChild(mesh);
		this.attachChild(label);
		mesh.setDefaultColor(ColorRGBA.BLACK);
		label.setTextColor(ColorRGBA.BLACK);
		label.setFontScale(0.5);
	}
	
	protected abstract Mesh getMesh();
}