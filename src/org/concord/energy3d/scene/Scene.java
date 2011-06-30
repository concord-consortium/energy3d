package org.concord.energy3d.scene;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;

import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Snap;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.util.Config;

import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;

public class Scene implements Serializable {
	public static enum Unit {
		Meter("m"), Centimeter("cm"), Inches("\"");
		private final String notation;

		private Unit(String notation) {
			this.notation = notation;
		}

		public String getNotation() {
			return notation;
		}
	};

	private static final long serialVersionUID = 1L;
	private static final Node root = new Node("House Root");
	private static final Node originalHouseRoot = new Node("Original House Root");
	private static Scene instance;
	private static URL url = null;
	private ArrayList<HousePart> parts = new ArrayList<HousePart>();
	private transient boolean redrawAll = false;
	private Unit unit = Unit.Meter;
	private double annotationScale = 1;
	static private boolean isTextureEnabled = true;
	static private boolean drawThickness = false;

	public static Scene getInstance() {
		if (instance == null) {
			instance = new Scene();
			try {
				if (!Config.isApplet())
					instance.open(new File("Energy3D Projects" + File.separator + "Default.ser").toURI().toURL());
				else if (Config.getApplet().getParameter("file") != null) {
					final URL url = new URL(Config.getApplet().getCodeBase(), Config.getApplet().getParameter("file"));
					instance.open(new URI(url.getProtocol(), url.getHost(), url.getPath(), null).toURL());
				} else {
					final URL url = new URL(Config.getApplet().getCodeBase(), "Energy3D Projects/Default.ser");
					instance.open((new URI(url.getProtocol(), url.getHost(), url.getPath(), null).toURL()));
				}
			} catch (Throwable e) {
				e.printStackTrace();
				instance = new Scene();
			}
			root.attachChild(originalHouseRoot);
		}
		return instance;
	}

	public static Node getRoot() {
		return root;
	}

	private Scene() {
	}

	public void add(HousePart housePart) {
		System.out.print("Adding new house part...");
		originalHouseRoot.attachChild(housePart.getRoot());
		parts.add(housePart);
		System.out.println("done");
	}

	public void remove(HousePart housePart) {
		final HousePart container = housePart.getContainer();
		if (container != null)
			container.getChildren().remove(housePart);
		removeTree(housePart);
		if (container != null)
			container.draw();
	}

	private void removeTree(HousePart housePart) {
		System.out.println("Removing: " + housePart);
		originalHouseRoot.detachChild(housePart.getRoot());
		parts.remove(housePart);
		housePart.delete();
		for (final HousePart child : housePart.getChildren())
			removeTree(child);
		housePart.getChildren().clear();
	}

	public ArrayList<HousePart> getParts() {
		return parts;
	}

	public void save(final URL url) throws FileNotFoundException, IOException {
		// remove dead objects
		final Iterator<HousePart> itr = parts.iterator();
		while (itr.hasNext()) {
			HousePart part = itr.next();
			if (part instanceof Roof || part instanceof Window || part instanceof Door)
				if (part.getContainer() == null)
					itr.remove();
		}

		Scene.url = url;
		System.out.print("Saving " + Scene.url + "...");
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(new FileOutputStream(Scene.url.toURI().getPath()));
			out.writeObject(this);
			out.close();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		System.out.println("done");
	}

	public void newFile() {
		final PrintController printController = PrintController.getInstance();
		if (printController.isPrintPreview()) {
			// printController.setPrintPreview(false);
			// MainFrame.getInstance().getPreviewMenuItem().setSelected(false);
			MainPanel.getInstance().getPreviewButton().setSelected(false);
			while (!printController.isFinished())
				Thread.yield();
		}

		SceneManager.taskManager.update(new Callable<Object>() {
			public Object call() throws Exception {
				originalHouseRoot.detachAllChildren();
				for (Spatial child : root.getChildren())
					if (child != originalHouseRoot)
						root.detachChild(child);
				root.updateWorldBound(true);
				SceneManager.getInstance().updateHeliodonSize();
				return null;
			}
		});
		parts.clear();
		url = null;
	}

	public void open(final URL file) {
		instance.newFile();
		Scene.url = file;
		SceneManager.taskManager.update(new Callable<Object>() {
			public Object call() throws Exception {
				System.out.print("Opening..." + file + "...");
				ObjectInputStream in = new ObjectInputStream(file.openStream());
				instance = (Scene) in.readObject();
				in.close();
				for (HousePart housePart : instance.getParts())
					originalHouseRoot.attachChild(housePart.getRoot());
				for (HousePart housePart : instance.getParts())
					housePart.draw();
				System.out.println("done");
				SceneManager.getInstance().updateHeliodonSize();
				return null;
			}
		});
	}

	public void drawResizeBounds() {
		for (HousePart part : parts) {
			if (part instanceof Foundation)
				part.draw();
		}
	}

	public Node getOriginalHouseRoot() {
		return originalHouseRoot;
	}

	public static URL getURL() {
		return url;
	}

	public void setAnnotationsVisible(boolean visible) {
		for (HousePart part : parts)
			part.setAnnotationsVisible(visible);
		for (HousePart part : PrintController.getInstance().getPrintParts())
			part.setAnnotationsVisible(visible);

		if (visible)
			redrawAll = true;
	}

	public void setTextureEnabled(final boolean enabled) {
		isTextureEnabled = enabled;
		for (HousePart part : parts)
			part.updateTextureAndColor(enabled);
		for (HousePart part : PrintController.getInstance().getPrintParts())
			part.updateTextureAndColor(enabled);

		if (enabled)
			redrawAll = true;
	}

	public void setDrawThickness(final boolean draw) {
		drawThickness = draw;
		redrawAll = true;
	}

	public boolean isDrawThickness() {
		return drawThickness;
	}

	public void redrawAll() {
		redrawAll = true;
	}

	public void update() {
		if (redrawAll) {
			Snap.clearAnnotationDrawn();
			for (HousePart part : parts)
				part.draw();
			if (PrintController.getInstance().getPrintParts() != null)
				for (HousePart part : PrintController.getInstance().getPrintParts())
					part.draw();
		}
		redrawAll = false;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
		redrawAll = true;
	}

	public Unit getUnit() {
		if (unit == null)
			unit = Unit.Meter;
		return unit;
	}

	public void setAnnotationScale(double scale) {
		this.annotationScale = scale;
		this.redrawAll = true;
	}

	public double getAnnotationScale() {
		if (annotationScale == 0)
			annotationScale = 1;
		return annotationScale;
	}

	public boolean isTextureEnabled() {
		return isTextureEnabled;
	}
}
