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

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Snap;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.shapes.Annotation;
import org.concord.energy3d.util.Config;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.ui.text.BMText.AutoScale;

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
	private static boolean redrawAll = false;
	private static boolean isTextureEnabled = true;
	private static boolean drawThickness = false;
	private static boolean drawAnnotationsInside = false;
	private static Unit unit = Unit.Centimeter;
	private static double annotationScale = 10;
	private ArrayList<HousePart> parts = new ArrayList<HousePart>();

	// public static Scene getInstance() {
	// if (instance == null) {
	// instance = new Scene();
	// try {
	// if (!Config.isApplet() && !Config.isWebStart())
	// instance.open(new File("Energy3D Projects" + File.separator + "Default.ser").toURI().toURL());
	// else if (Config.isWebStart()) {
	// // do nothing
	// } else if (Config.getApplet().getParameter("file") != null) {
	// final URL url = new URL(Config.getApplet().getCodeBase(), Config.getApplet().getParameter("file"));
	// instance.open(new URI(url.getProtocol(), url.getHost(), url.getPath(), null).toURL());
	// } else {
	// final URL url = new URL(Config.getApplet().getCodeBase(), "Energy3D Projects/Default.ser");
	// instance.open((new URI(url.getProtocol(), url.getHost(), url.getPath(), null).toURL()));
	// }
	// } catch (Throwable e) {
	// e.printStackTrace();
	// instance = new Scene();
	// }
	// root.attachChild(originalHouseRoot);
	// }
	// return instance;
	// }

	public static Scene getInstance() {
		if (instance == null) {
			// instance = new Scene();
			try {
				if (!Config.isApplet() && !Config.isWebStart())
					open(new File("Energy3D Projects" + File.separator + "Default.ser").toURI().toURL());
				else if (Config.isWebStart()) {
					open(null);
				} else if (Config.getApplet().getParameter("file") != null) {
					final URL url = new URL(Config.getApplet().getCodeBase(), Config.getApplet().getParameter("file"));
					open(new URI(url.getProtocol(), url.getHost(), url.getPath(), null).toURL());
				} else {
					final URL url = new URL(Config.getApplet().getCodeBase(), "Energy3D Projects/Default.ser");
					open((new URI(url.getProtocol(), url.getHost(), url.getPath(), null).toURL()));
				}
			} catch (Throwable e) {
				e.printStackTrace();
				newFile(40, 30);
			}
			// root.attachChild(originalHouseRoot);
		}
		return instance;
	}

	// public static void newFile() {
	// }

	public static void newFile(final double xLength, final double yLength) {
		// newFile();
		try {
			open(null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		final Foundation foundation = new Foundation(xLength / annotationScale, yLength / annotationScale);

		SceneManager.getTaskManager().update(new Callable<Object>() {
			public Object call() throws Exception {
				instance.add(foundation);
				redrawAll = true;
				return null;
			}
		});
	}

	public static void open(final URL file) throws Exception {
		Scene.url = file;

		if (PrintController.getInstance().isPrintPreview()) {
			MainPanel.getInstance().getPreviewButton().setSelected(false);
			while (!PrintController.getInstance().isFinished())
				Thread.yield();
		}

		if (url == null) {
			instance = new Scene();
			System.out.println("done");
		} else {
			System.out.print("Opening..." + file + "...");
			ObjectInputStream in = new ObjectInputStream(file.openStream());
			instance = (Scene) in.readObject();
			in.close();
		}

		SceneManager.getTaskManager().update(new Callable<Object>() {
			public Object call() throws Exception {
				System.out.print("Open file...");
				originalHouseRoot.detachAllChildren();
				root.detachAllChildren();
				root.attachChild(originalHouseRoot);

				if (url != null) {
					for (HousePart housePart : instance.getParts())
						originalHouseRoot.attachChild(housePart.getRoot());
					redrawAll = true;
					System.out.println("done");
				}

				root.updateWorldBound(true);
				SceneManager.getInstance().updateHeliodonAndAnnotationSize();
				SceneManager.getInstance().getUndoManager().die();
				if (!Config.isApplet())
					MainFrame.getInstance().refreshUndoRedo();
				return null;
			}
		});
	}

	public static void save(final URL url) throws Exception {
		// remove dead objects
		final Iterator<HousePart> itr = instance.parts.iterator();
		while (itr.hasNext()) {
			HousePart part = itr.next();
			if (part instanceof Roof || part instanceof Window || part instanceof Door)
				if (part.getContainer() == null)
					itr.remove();
		}

		Scene.url = url;
		System.out.print("Saving " + Scene.url + "...");
		ObjectOutputStream out;
		out = new ObjectOutputStream(new FileOutputStream(Scene.url.toURI().getPath()));
		out.writeObject(instance);
		out.close();
		System.out.println("done");
	}

	public static Node getRoot() {
		return root;
	}

	private Scene() {
	}

	public void add(HousePart housePart) {
		System.out.print("Adding new house part...");
		final HousePart container = housePart.getContainer();
		if (container != null)
			container.getChildren().add(housePart);
		addTree(housePart);
		if (container != null)
			container.draw();
		System.out.println("done");
	}

	private void addTree(final HousePart housePart) {
		System.out.println("Adding: " + housePart);
		originalHouseRoot.attachChild(housePart.getRoot());
		parts.add(housePart);
		for (final HousePart child : housePart.getChildren())
			addTree(child);
	}

	public void remove(final HousePart housePart) {
		if (housePart == null)
			return;
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
	}

	public ArrayList<HousePart> getParts() {
		return parts;
	}

	// public void save(final URL url) throws FileNotFoundException, IOException {
	// // remove dead objects
	// final Iterator<HousePart> itr = parts.iterator();
	// while (itr.hasNext()) {
	// HousePart part = itr.next();
	// if (part instanceof Roof || part instanceof Window || part instanceof Door)
	// if (part.getContainer() == null)
	// itr.remove();
	// }
	//
	// Scene.url = url;
	// System.out.print("Saving " + Scene.url + "...");
	// ObjectOutputStream out;
	// try {
	// out = new ObjectOutputStream(new FileOutputStream(Scene.url.toURI().getPath()));
	// out.writeObject(this);
	// out.close();
	// } catch (URISyntaxException e) {
	// e.printStackTrace();
	// }
	// System.out.println("done");
	// }
	//
	// public void newFile() {
	// final PrintController printController = PrintController.getInstance();
	// if (printController.isPrintPreview()) {
	// MainPanel.getInstance().getPreviewButton().setSelected(false);
	// while (!printController.isFinished())
	// Thread.yield();
	// }
	//
	// parts.clear();
	// url = null;
	//
	// SceneManager.taskManager.update(new Callable<Object>() {
	// public Object call() throws Exception {
	// originalHouseRoot.detachAllChildren();
	// for (Spatial child : root.getChildren())
	// if (child != originalHouseRoot)
	// root.detachChild(child);
	// root.updateWorldBound(true);
	// SceneManager.getInstance().updateHeliodonAndAnnotationSize();
	// SceneManager.getInstance().getUndoManager().die();
	// MainFrame.getInstance().refreshUndoRedo();
	// return null;
	// }
	// });
	// }
	//
	// public void open(final URL file) {
	// instance.newFile();
	// Scene.url = file;
	// SceneManager.taskManager.update(new Callable<Object>() {
	// public Object call() throws Exception {
	// System.out.print("Opening..." + file + "...");
	// ObjectInputStream in = new ObjectInputStream(file.openStream());
	// instance = (Scene) in.readObject();
	// in.close();
	// for (HousePart housePart : instance.getParts())
	// originalHouseRoot.attachChild(housePart.getRoot());
	// redrawAll = true;
	// System.out.println("done");
	// SceneManager.getInstance().updateHeliodonAndAnnotationSize();
	// return null;
	// }
	// });
	// }

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
		if (PrintController.getInstance().isPrintPreview())
			for (HousePart part : PrintController.getInstance().getPrintParts())
				part.setAnnotationsVisible(visible);
		SceneManager.getInstance().update();
	}

	public void setTextureEnabled(final boolean enabled) {
		isTextureEnabled = enabled;
		for (HousePart part : parts)
			part.updateTextureAndColor(enabled);
		if (PrintController.getInstance().getPrintParts() != null)
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

	public static boolean isDrawAnnotationsInside() {
		return drawAnnotationsInside;
	}

	public static void setDrawAnnotationsInside(boolean drawAnnotationsInside) {
		Scene.drawAnnotationsInside = drawAnnotationsInside;
		for (final HousePart part : getInstance().getParts())
			part.drawAnnotations();
		if (PrintController.getInstance().getPrintParts() != null)
			for (final HousePart part : PrintController.getInstance().getPrintParts())
				part.drawAnnotations();

		// redrawAll = true;
	}

	public void redrawAll() {
		redrawAll = true;
	}

	public void update() {
		if (redrawAll) {
			Snap.clearAnnotationDrawn();
			for (final HousePart part : parts)
				if (part instanceof Roof)
					part.draw();
			for (final HousePart part : parts)
				if (!(part instanceof Roof))
					part.draw();
			if (PrintController.getInstance().getPrintParts() != null)
				for (HousePart part : PrintController.getInstance().getPrintParts())
					part.draw();
			// updateTextSizes();
			redrawAll = false;
		}
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
		redrawAll = true;
	}

	public double getAnnotationScale() {
		if (annotationScale == 0)
			annotationScale = 1;
		return annotationScale;
	}

	public boolean isTextureEnabled() {
		return isTextureEnabled;
	}

	public void updateTextSizes() {
		getOriginalHouseRoot().updateWorldBound(true);
		final BoundingBox bounds = (BoundingBox) getOriginalHouseRoot().getWorldBound();
		if (bounds != null) {
			final double size = Math.max(bounds.getXExtent(), Math.max(bounds.getYExtent(), bounds.getZExtent()));
			final double fontSize = size / 20.0;
			updateTextSizes(fontSize);
		}
	}

	public void updateTextSizes(final double fontSize) {
		Annotation.setFontSize(fontSize);
		updateTextSizes(root, fontSize);
	}

	private void updateTextSizes(final Spatial spatial, final double fontSize) {
		if (spatial instanceof BMText) {
			final BMText label = (BMText) spatial;
			if (label.getAutoScale() == AutoScale.Off) {
				label.setFontScale(fontSize);
				label.updateGeometricState(0);
			}
		} else if (spatial instanceof Node) {
			for (final Spatial child : ((Node) spatial).getChildren())
				updateTextSizes(child, fontSize);
			// now that text font is updated redraw the annotation
			if (spatial instanceof Annotation)
				((Annotation) spatial).draw();
		}
	}

	public void removeAllRoofs() {
		final ArrayList<HousePart> roofs = new ArrayList<HousePart>();
		for (final HousePart part : parts)
			if (part instanceof Roof)
				roofs.add(part);

		for (final HousePart part : roofs)
			remove(part);
	}

	public static boolean isRedrawAll() {
		return redrawAll;
	}
}
