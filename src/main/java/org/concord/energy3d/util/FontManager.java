package org.concord.energy3d.util;

import com.ardor3d.ui.text.BMFont;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.ResourceSource;

public class FontManager {
	private static final FontManager instance = new FontManager();
	private final BMFont partNumberFont;
	private final BMFont annotationFont;

	public static FontManager getInstance() {
		return instance;
	}

	private FontManager() {
		System.out.print("Loading fonts...");
		partNumberFont = loadFont("DejaVuSansCondensed-20-bold-regular.fnt");
		annotationFont = loadFont("SimpleFont.fnt");
		System.out.println("done");
	}

	private BMFont loadFont(final String file) {
		final ResourceSource url = ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE, file);
		try {
			return new BMFont(url, true);
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public BMFont getPartNumberFont() {
		return partNumberFont;
	}

	public BMFont getAnnotationFont() {
		return annotationFont;
	}
}
