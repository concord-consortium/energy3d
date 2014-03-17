package org.concord.energy3d.model;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.concord.energy3d.util.SelectUtil;

import com.ardor3d.extension.model.collada.jdom.ColladaAnimUtils;
import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.extension.model.collada.jdom.ColladaMaterialUtils;
import com.ardor3d.extension.model.collada.jdom.data.ColladaStorage;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.ResourceSource;

public class Tree extends HousePart {
	private static final long serialVersionUID = 1L;
	private static Spatial treeModel;
	private final Spatial model;
	
	public static void loadModel() {
		new Thread() {
			@Override
			public void run() {
				System.out.println("Loading tree.");
				Thread.yield();
				final ResourceSource source = ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_MODEL, "tree.dae");
				final ColladaImporter colladaImporter = new ColladaImporter();
				Logger.getLogger(ColladaAnimUtils.class.getName()).setLevel(Level.SEVERE);
				Logger.getLogger(ColladaMaterialUtils.class.getName()).setLevel(Level.SEVERE);
				ColladaStorage storage;
				try {
					storage = colladaImporter.load(source);
					treeModel = storage.getScene();
				} catch (final IOException e) {
					e.printStackTrace();
				}
				System.out.println("Finished loading tree model.");
			}
		}.start();
	}
	
	public Tree() {
		super(1, 1, 1);
		model = treeModel.makeCopy(true);
		root.attachChild(model);
	}
	
	@Override
	public void setPreviewPoint(final int x, final int y) {
		final int index = 0;
		final PickedHousePart pick = SelectUtil.pickPart(x, y, (Spatial) null);
		Vector3 p = points.get(index);
		if (pick != null) {
			p = pick.getPoint();
			snapToGrid(p, getAbsPoint(index), getGridSize());
			points.get(index).set(p);
		}
		draw();
		setEditPointsVisible(true);				
	}
	
	@Override
	protected boolean mustHaveContainer() {
		return false;
	}	
	
	@Override
	public boolean isDrawable() {
		return true;
	}
	
	@Override
	protected void drawMesh() {
		model.setTranslation(getAbsPoint(0));
	}

	@Override
	protected String getTextureFileName() {
		return null;
	}

	@Override
	public void updateTextureAndColor() {
	}
	
}
