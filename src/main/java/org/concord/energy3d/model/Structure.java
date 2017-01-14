package org.concord.energy3d.model;

import java.util.List;

import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;

/**
 * This class defines objects that contain structures imported from other CAD formats such as COLLADA.
 * 
 * @author Charles Xie
 *
 */
public class Structure extends HousePart {

	private static final long serialVersionUID = 1L;

	public Structure() {
		super(8, 8, 0);
	}

	@Override
	protected void init() {
		super.init();
	}

	@Override
	protected String getTextureFileName() {
		return null;
	}

	@Override
	public void setPreviewPoint(final int x, final int y) {
	}

	@Override
	protected void drawMesh() {
	}

	@Override
	public void updateTextureAndColor() {
	}

	@Override
	protected void computeArea() {
	}

	@Override
	public boolean isCopyable() {
		return false;
	}

	public static void getMeshes(final Node node, final List<Mesh> meshes) {
		final List<Spatial> children = node.getChildren();
		if (!children.isEmpty()) {
			for (final Spatial c : children) {
				if (c instanceof Node) {
					getMeshes((Node) c, meshes);
				} else {
					if (c instanceof Mesh) {
						meshes.add((Mesh) c);
					}
				}
			}
		}
	}

}
