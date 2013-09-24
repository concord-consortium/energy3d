package org.concord.energy3d.scene;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Callable;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Snap;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.undo.SaveCommand;
import org.concord.energy3d.util.Config;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.scenegraph.Node;

public class Scene implements Serializable {
	public static enum Unit {
		Meter("m"), Centimeter("cm"), Inches("\"");
		private final String notation;

		private Unit(final String notation) {
			this.notation = notation;
		}

		public String getNotation() {
			return notation;
		}
	};

	public static enum TextureMode {
		None, Simple, Full
	};

	public static final ReadOnlyColorRGBA WHITE = ColorRGBA.WHITE;
	public static final ReadOnlyColorRGBA GRAY = ColorRGBA.LIGHT_GRAY;

	private static final long serialVersionUID = 1L;
	private static final Node root = new Node("House Root");
	private static final Node originalHouseRoot = new Node("Original House Root");
	private static final int currentVersion = 1;
	private static Scene instance;
	private static URL url = null;
	private static boolean redrawAll = false;
	private static boolean drawThickness = false;
	private static boolean drawAnnotationsInside = false;
	private static Unit unit = Unit.Meter;
	private transient boolean edited = false;
	private final ArrayList<HousePart> parts = new ArrayList<HousePart>();
	private TextureMode textureMode = TextureMode.Full;
	private ReadOnlyVector3 cameraLocation;
	private ReadOnlyVector3 cameraDirection;
	private ReadOnlyColorRGBA foundationColor;
	private ReadOnlyColorRGBA wallColor;
	private ReadOnlyColorRGBA doorColor;
	private ReadOnlyColorRGBA floorColor;
	private ReadOnlyColorRGBA roofColor;
	private double overhangLength = 2.0;
	private double annotationScale = 0.2;
	private int version = currentVersion;
	private boolean isAnnotationsVisible = true;
	private long idCounter;
	private Calendar calendar;
	private String city;
	private int latitude;
	private boolean isHeliodonVisible;
	private String note;
	private int solarContrast;
	private boolean cleanup = false;

	private static final ArrayList<PropertyChangeListener> propertyChangeListeners = new ArrayList<PropertyChangeListener>();

	public static Scene getInstance() {
		if (instance == null) {
			try {
				if (Config.isApplet() && Config.getApplet().getParameter("file") != null) {
					final URL url = new URL(Config.getApplet().getCodeBase(), Config.getApplet().getParameter("file"));
					open(new URI(url.getProtocol(), url.getHost(), url.getPath(), null).toURL());
				} else
					newFile();
			} catch (final Throwable e) {
				e.printStackTrace();
				newFile();
			}
		}
		return instance;
	}

	public static void newFile() {
		newFile(40, 30);
	}

	private static void newFile(final double xLength, final double yLength) {
		try {
			open(null);
		} catch (final Exception e) {
			e.printStackTrace();
		}

		final Foundation foundation = new Foundation(xLength, yLength);

		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				instance.add(foundation);
				redrawAll = true;
				return null;
			}
		});
	}

	public void addPropertyChangeListener(final PropertyChangeListener pcl) {
		propertyChangeListeners.add(pcl);
	}

	public void removePropertyChangeListener(final PropertyChangeListener pcl) {
		propertyChangeListeners.remove(pcl);
	}

	private void notifyPropertyChangeListeners(final PropertyChangeEvent evt) {
		if (!propertyChangeListeners.isEmpty()) {
			for (final PropertyChangeListener x : propertyChangeListeners) {
				x.propertyChange(evt);
			}
		}
	}

	public static void open(final URL file) throws Exception {
		Scene.url = file;

		if (PrintController.getInstance().isPrintPreview()) {
			MainPanel.getInstance().getPreviewButton().setSelected(false);
			while (!PrintController.getInstance().isFinished())
				Thread.yield();
		}

		MainPanel.getInstance().getHeliodonButton().setSelected(false);
		MainPanel.getInstance().getSunAnimButton().setSelected(false);
		Wall.resetDefaultWallHeight();

		if (url == null) {
			instance = new Scene();
			System.out.println("done");
		} else {
			System.out.print("Opening..." + file + "...");
			final ObjectInputStream in = new ObjectInputStream(file.openStream());
			instance = (Scene) in.readObject();
			in.close();

			for (final HousePart part : instance.parts)
				part.getRoot();

			if (instance.version >= 1)
				instance.cleanup();
			instance.upgradeSceneToNewVersion();
			loadCameraLocation();
		}

		if (!Config.isApplet()) {
			if (instance.textureMode == TextureMode.None)
				MainFrame.getInstance().getNoTextureRadioButtonMenuItem().setSelected(true);
			else if (instance.textureMode == TextureMode.Simple)
				MainFrame.getInstance().getSimpleTextureRadioButtonMenuItem().setSelected(true);
			else
				MainFrame.getInstance().getFullTextureRadioButtonMenuItem().setSelected(true);
		}
		MainPanel.getInstance().getAnnotationToggleButton().setSelected(instance.isAnnotationsVisible);

		final CameraControl cameraControl = SceneManager.getInstance().getCameraControl();
		if (cameraControl != null)
			cameraControl.reset();

		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				System.out.print("Open file...");
				originalHouseRoot.detachAllChildren();
				root.detachAllChildren();
				root.attachChild(originalHouseRoot);

				if (url != null) {
					for (final HousePart housePart : instance.getParts())
						originalHouseRoot.attachChild(housePart.getRoot());
					/* must redraw now so that heliodon can be initialized to right size if it is to be visible */
					instance.redrawAllNow();
					System.out.println("done");
				}

				root.updateWorldBound(true);
				SceneManager.getInstance().updateHeliodonAndAnnotationSize();
				final EnergyPanel energyPanel = EnergyPanel.getInstance();
				if (instance.calendar != null) {
					energyPanel.getDateSpinner().setValue(instance.calendar.getTime());
					energyPanel.getTimeSpinner().setValue(instance.calendar.getTime());
					energyPanel.setLatitude(instance.latitude);
					energyPanel.setCity(instance.city);
					MainPanel.getInstance().getHeliodonButton().setSelected(instance.isHeliodonVisible);
				}
				energyPanel.getColorMapSlider().setValue(instance.solarContrast == 0 ? 50 : instance.solarContrast);
				MainPanel.getInstance().getNoteTextArea().setText(instance.note == null ? "" : instance.note);
				SceneManager.getInstance().getUndoManager().die();
				Scene.getInstance().setEdited(false);
				energyPanel.compute(true);
				return null;
			}
		});
	}

	public static void loadCameraLocation() {
		final Camera camera = SceneManager.getInstance().getCamera();
		if (instance.getCameraLocation() != null && instance.getCameraDirection() != null) {
			camera.setLocation(instance.getCameraLocation());
			camera.lookAt(instance.getCameraLocation().add(instance.getCameraDirection(), null), Vector3.UNIT_Z);
		}
		SceneManager.getInstance().getCameraNode().updateFromCamera();
		Scene.getInstance().updateEditShapes();
	}

	public static void importFile(final URL url) throws Exception {
		if (PrintController.getInstance().isPrintPreview()) {
			MainPanel.getInstance().getPreviewButton().setSelected(false);
			while (!PrintController.getInstance().isFinished())
				Thread.yield();
		}

		if (url != null) {
			System.out.print("Opening..." + url + "...");
			final ObjectInputStream in = new ObjectInputStream(url.openStream());
			final Scene instance = (Scene) in.readObject();
			in.close();

			instance.cleanup();
			instance.upgradeSceneToNewVersion();

			if (url != null) {
				for (final HousePart housePart : instance.getParts()) {
					Scene.getInstance().parts.add(housePart);
					originalHouseRoot.attachChild(housePart.getRoot());
				}
				redrawAll = true;
				System.out.println("done");
			}

			root.updateWorldBound(true);
			SceneManager.getInstance().updateHeliodonAndAnnotationSize();
			SceneManager.getInstance().getUndoManager().die();
			if (!Config.isApplet())
				MainFrame.getInstance().refreshUndoRedo();
		}
	}

	private void cleanup() {
		final ArrayList<HousePart> toBeRemoved = new ArrayList<HousePart>();
		for (final HousePart housePart : getParts()) {
			if (!housePart.isDrawCompleted())
				housePart.complete();
			if (!housePart.isValid() || ((housePart instanceof Roof || housePart instanceof Window || housePart instanceof Door) && housePart.getContainer() == null))
				toBeRemoved.add(housePart);
			removeDeadChildren(housePart, toBeRemoved);
		}

		for (final HousePart housePart : toBeRemoved)
			remove(housePart);

		connectWalls();
	}

	private void removeDeadChildren(final HousePart parent, final ArrayList<HousePart> toBeRemoved) {
		for (final HousePart part : parent.getChildren())
			if (!getParts().contains(part))
				toBeRemoved.add(part);
	}

	private void upgradeSceneToNewVersion() {
		if (textureMode == null) {
			textureMode = TextureMode.Full;
			overhangLength = 0.2;
		}

		if (version < 1) {
			for (final HousePart part : parts) {
				if (part instanceof Foundation)
					((Foundation) part).scaleHouse(10);
			}
			cameraLocation = cameraLocation.multiply(10, null);
			setOverhangLength(getOverhangLength() * 10);
			setAnnotationScale(1.0);
		}

		version = currentVersion;
	}

	private void connectWalls() {
		for (final HousePart part : parts)
			if (part instanceof Wall)
				part.reset();

		for (final HousePart part : parts) {
			if (part instanceof Wall) {
				final Wall wall = (Wall) part;
				wall.fixDisconnectedWalls();
			}
		}
		
		for (final HousePart part : parts) {
			if (part instanceof Wall) {
				final Wall wall = (Wall) part;
				wall.computeInsideDirectionOfAttachedWalls(false);
			}
		}
	}

	public static void save(final URL url, final boolean setAsCurrentFile) throws Exception {
		save(url, setAsCurrentFile, true);
	}

	public static void save(final URL url, final boolean setAsCurrentFile, final boolean notifyUndoManager) throws Exception {
		if (notifyUndoManager)
			instance.cleanup();
		// save camera to file
		saveCameraLocation();

		instance.calendar = Heliodon.getInstance().getCalander();
		instance.latitude = EnergyPanel.getInstance().getLatitude();
		instance.city = EnergyPanel.getInstance().getCity();
		instance.isHeliodonVisible = Heliodon.getInstance().isVisible();
		instance.note = MainPanel.getInstance().getNoteTextArea().getText().trim();
		instance.solarContrast = EnergyPanel.getInstance().getColorMapSlider().getValue();

		if (setAsCurrentFile)
			Scene.url = url;
		System.out.print("Saving " + url + "...");
		ObjectOutputStream out;
		out = new ObjectOutputStream(new FileOutputStream(url.toURI().getPath()));
		out.writeObject(instance);
		out.close();
		if (notifyUndoManager)
			SceneManager.getInstance().getUndoManager().addEdit(new SaveCommand());
		System.out.println("done");
	}

	public static void saveCameraLocation() {
		final Camera camera = SceneManager.getInstance().getCamera();
		instance.setCameraLocation(camera.getLocation().clone());
		instance.setCameraDirection(SceneManager.getInstance().getCamera().getDirection().clone());
	}

	public static Node getRoot() {
		return root;
	}

	private Scene() {
	}

	public void add(final HousePart housePart) {
		final HousePart container = housePart.getContainer();
		if (container != null)
			container.getChildren().add(housePart);
		addTree(housePart);
		if (container != null)
			container.draw();
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
		housePart.setGridsVisible(false);
		final HousePart container = housePart.getContainer();
		if (container != null)
			container.getChildren().remove(housePart);
		removeTree(housePart);
		if (container != null)
			container.draw();
	}

	private void removeTree(final HousePart housePart) {
		System.out.println("Removing: " + housePart);
		parts.remove(housePart); // this must happen before call to wall.delete()
		for (final HousePart child : housePart.getChildren())
			removeTree(child);
		originalHouseRoot.detachChild(housePart.getRoot());
		housePart.delete();
	}

	public ArrayList<HousePart> getParts() {
		return parts;
	}

	public void drawResizeBounds() {
		for (final HousePart part : parts) {
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

	public void setAnnotationsVisible(final boolean visible) {
		isAnnotationsVisible = visible;
		for (final HousePart part : parts)
			part.setAnnotationsVisible(visible);
		if (PrintController.getInstance().isPrintPreview())
			for (final HousePart part : PrintController.getInstance().getPrintParts())
				part.setAnnotationsVisible(visible);
		if (PrintController.getInstance().isPrintPreview()) {
			PrintController.getInstance().restartAnimation();
		} else
			SceneManager.getInstance().refresh();

	}

	public void setTextureMode(final TextureMode textureMode) {
		this.textureMode = textureMode;
		redrawAll();
		Scene.getInstance().updateRoofDashLinesColor();
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

	public static void setDrawAnnotationsInside(final boolean drawAnnotationsInside) {
		Scene.drawAnnotationsInside = drawAnnotationsInside;
		for (final HousePart part : getInstance().getParts())
			part.drawAnnotations();
		if (PrintController.getInstance().getPrintParts() != null)
			for (final HousePart part : PrintController.getInstance().getPrintParts())
				part.drawAnnotations();
	}

	public void redrawAll() {
		redrawAll(false);
	}
	
	public void redrawAll(final boolean cleanup) {
		this.cleanup  = cleanup;
		if (PrintController.getInstance().isPrintPreview())
			PrintController.getInstance().restartAnimation();
		else
			redrawAll = true;
	}

	public void redrawAllNow() {
		System.out.println("redrawAllNow()");
		if (cleanup) {
			cleanup();
			cleanup = false;
		}
		Snap.clearAnnotationDrawn();
		for (final HousePart part : parts)
			if (part instanceof Roof)
				part.draw();
		for (final HousePart part : parts)
			if (!(part instanceof Roof))
				part.draw();
		// no need for redrawing printparts because they will be regenerated from original parts anyways
		redrawAll = false;
	}

	public void setUnit(final Unit unit) {
		Scene.unit = unit;
		redrawAll = true;
	}

	public Unit getUnit() {
		if (unit == null)
			unit = Unit.Meter;
		return unit;
	}

	public void setAnnotationScale(final double scale) {
		annotationScale = scale;
	}

	public double getAnnotationScale() {
		if (annotationScale == 0)
			annotationScale = 10;
		return annotationScale;
	}

	public TextureMode getTextureMode() {
		return textureMode;
	}

	public void updateRoofDashLinesColor() {
		for (final HousePart part : parts)
			if (part instanceof Roof)
				((Roof) part).updateDashLinesColor();
		if (PrintController.getInstance().getPrintParts() != null)
			for (final HousePart part : PrintController.getInstance().getPrintParts())
				if (part instanceof Roof)
					((Roof) part).updateDashLinesColor();
	}

	public void removeAllRoofs() {
		final ArrayList<HousePart> roofs = new ArrayList<HousePart>();
		for (final HousePart part : parts)
			if (part instanceof Roof && !part.isFrozen())
				roofs.add(part);

		for (final HousePart part : roofs)
			remove(part);
	}

	public static boolean isRedrawAll() {
		return redrawAll;
	}

	public boolean isAnnotationsVisible() {
		return isAnnotationsVisible;
	}

	public ReadOnlyVector3 getCameraLocation() {
		return cameraLocation;
	}

	public void setCameraLocation(final ReadOnlyVector3 cameraLocation) {
		this.cameraLocation = cameraLocation;
	}

	public ReadOnlyVector3 getCameraDirection() {
		return cameraDirection;
	}

	public void setCameraDirection(final ReadOnlyVector3 cameraDirection) {
		this.cameraDirection = cameraDirection;
	}

	public void removeAllGables() {
		for (final HousePart part : parts)
			if (part instanceof Roof)
				((Roof) part).removeAllGables();
	}

	public double getOverhangLength() {
		if (overhangLength < 0.01)
			return 0.01;
		else
			return overhangLength;
	}

	public void setOverhangLength(final double overhangLength) {
		this.overhangLength = overhangLength;
	}

	public ReadOnlyColorRGBA getFoundationColor() {
		if (foundationColor == null)
			return WHITE;
		else
			return foundationColor;
	}

	public void setFoundationColor(final ReadOnlyColorRGBA foundationColor) {
		this.foundationColor = foundationColor;
	}

	public ReadOnlyColorRGBA getWallColor() {
		if (wallColor == null)
			return WHITE;
		else
			return wallColor;
	}

	public void setWallColor(final ReadOnlyColorRGBA wallColor) {
		this.wallColor = wallColor;
	}

	public ReadOnlyColorRGBA getDoorColor() {
		if (doorColor == null)
			return WHITE;
		else
			return doorColor;
	}

	public void setDoorColor(final ReadOnlyColorRGBA doorColor) {
		this.doorColor = doorColor;
	}

	public ReadOnlyColorRGBA getFloorColor() {
		if (floorColor == null)
			return WHITE;
		else
			return floorColor;
	}

	public void setFloorColor(final ReadOnlyColorRGBA floorColor) {
		this.floorColor = floorColor;
	}

	public ReadOnlyColorRGBA getRoofColor() {
		if (roofColor == null)
			return WHITE;
		else
			return roofColor;
	}

	public void setRoofColor(final ReadOnlyColorRGBA roofColor) {
		this.roofColor = roofColor;
	}

	public boolean isEdited() {
		return edited;
	}

	public void setEdited(final boolean edited) {
		setEdited(edited, true);
	}

	public void setEdited(final boolean edited, final boolean recomputeEnergy) {
		if (edited)
			notifyPropertyChangeListeners(new PropertyChangeEvent(this, "Edit", this.edited, edited));
		this.edited = edited;
		if (!Config.isApplet())
			MainFrame.getInstance().updateTitleBar();
		if (edited && recomputeEnergy)
			EnergyPanel.getInstance().compute(true);
	}

	public void updateEditShapes() {
		for (final HousePart part : parts)
			part.updateEditShapes();
	}

	public void setFreeze(final boolean freeze) {
		for (final HousePart part : parts)
			part.setFreeze(freeze);
		if (freeze)
			SceneManager.getInstance().hideAllEditPoints();
		redrawAll();
	}

	public long nextID() {
		return ++idCounter;
	}
}
