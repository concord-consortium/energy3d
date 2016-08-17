package org.concord.energy3d.scene;

import java.awt.EventQueue;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.logger.SnapshotLogger;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.Snap;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Thermalizable;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.SceneManager.ViewMode;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.DesignSpecs;
import org.concord.energy3d.simulation.Ground;
import org.concord.energy3d.simulation.UtilityBill;
import org.concord.energy3d.undo.AddMultiplePartsCommand;
import org.concord.energy3d.undo.LockAllCommand;
import org.concord.energy3d.undo.PastePartCommand;
import org.concord.energy3d.undo.RemoveMultiplePartsCommand;
import org.concord.energy3d.undo.RemoveMultipleShuttersCommand;
import org.concord.energy3d.undo.SaveCommand;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.Util;

import com.ardor3d.image.Image;
import com.ardor3d.image.Texture.MinificationFilter;
import com.ardor3d.image.Texture2D;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.TextureKey;

public class Scene implements Serializable {
	public static final ReadOnlyColorRGBA WHITE = ColorRGBA.WHITE;
	public static final ReadOnlyColorRGBA GRAY = ColorRGBA.LIGHT_GRAY;
	public static final int CLOUDY_SKY = 0;
	public static final int DESERT = 1;
	public static final int GRASSLAND = 2;
	public static final int FOREST = 3;

	private static final long serialVersionUID = 1L;
	private static final Node root = new Node("House Root");
	private static final Node originalHouseRoot = new Node("Original House Root");
	private static final Node notReceivingShadowRoot = new Node("Trees Root");
	private static final int currentVersion = 1;
	private static Scene instance;
	private static URL url = null;
	private static boolean redrawAll = false;
	private static boolean drawThickness = false;
	private static boolean drawAnnotationsInside = false;
	private Unit unit = Unit.InternationalSystemOfUnits;
	private transient boolean edited = false;
	private final List<HousePart> parts = new ArrayList<HousePart>();
	private final Calendar calendar = Calendar.getInstance();
	private TextureMode textureMode = TextureMode.Full;
	private ReadOnlyVector3 cameraLocation;
	private ReadOnlyVector3 cameraDirection;
	private ReadOnlyColorRGBA landColor = new ColorRGBA(0, 1, 0, 0.5f);
	private ReadOnlyColorRGBA foundationColor;
	private ReadOnlyColorRGBA wallColor;
	private ReadOnlyColorRGBA doorColor;
	private ReadOnlyColorRGBA floorColor;
	private ReadOnlyColorRGBA roofColor;
	private double annotationScale = 0.2;
	private int version = currentVersion;
	private boolean isAnnotationsVisible = true;
	private long idCounter;
	private boolean studentMode;
	private String projectName;
	private String city;
	private int latitude;
	private boolean isHeliodonVisible;
	private String note;
	private int solarContrast;
	private boolean hideAxes;
	private boolean hideLightBeams;
	private boolean showSunAngles;
	private boolean showBuildingLabels;
	private double solarStep = 2.0;
	private int timeStep = 15; // in minutes
	private int plateNx = 2; // number of grid cells in x direction for a plate
	private int plateNy = 2; // number of grid cells in y direction for a plate
	private boolean cleanup = false;
	private double heatVectorLength = 2000;
	private boolean alwaysComputeHeatFluxVectors = false;
	private boolean fullEnergyInSolarMap = true;
	private boolean allowFoundationOverlap = false;
	private Ground ground = new Ground();
	private DesignSpecs designSpecs = new DesignSpecs();
	private HousePart copyBuffer, originalCopy;
	private boolean dashedlineOnRoofs = true;
	private boolean onlySolarAnalysis;
	private UtilityBill utilityBill;
	private int theme;
	private Image mapImage;
	private double mapScale;
	private byte[] mapImageBytes;
	private transient Image storedMapImage;
	private transient double storedMapScale;
	private transient byte[] storedMapImageBytes;

	public static enum Unit {
		InternationalSystemOfUnits, USCustomaryUnits
	};

	public static enum TextureMode {
		None, Simple, Full
	};

	public static Scene getInstance() {
		if (instance == null) {
			try {
				open(null);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	public static void newFile() {
		newFile(80, 60); // by default, the foundation is 16 meters x 12 meters (192 square meters seem right for a house)
	}

	private static void newFile(final double xLength, final double yLength) {
		try {
			open(null);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				instance.add(new Human(Human.JACK, 1));
				instance.add(new Foundation(xLength, yLength), true);
				return null;
			}
		});
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				EnergyPanel.getInstance().update();
				EnergyPanel.getInstance().clearAllGraphs();
			}
		});
	}

	public static void open(final URL file) throws Exception {
		openNow(file);
		synchronized (SceneManager.getInstance()) {
			EnergyPanel.getInstance().clearRadiationHeatMap();
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					final EnergyPanel e = EnergyPanel.getInstance();
					e.update();
					e.clearAllGraphs();
					if (MainFrame.getInstance().getTopViewCheckBoxMenuItem().isSelected()) { // make sure we exist the 2D top view
						MainFrame.getInstance().getTopViewCheckBoxMenuItem().setSelected(false);
						SceneManager.getInstance().resetCamera(ViewMode.NORMAL);
						SceneManager.getInstance().resetCamera();
					}
					final HousePart p = SceneManager.getInstance().getSelectedPart();
					if (p instanceof Foundation) {
						final Foundation f = (Foundation) p;
						switch (f.getSupportingType()) {
						case Foundation.BUILDING:
							e.getConstructionCostGraph().addGraph(f);
							e.getBuildingDailyEnergyGraph().clearData();
							e.getBuildingDailyEnergyGraph().addGraph(f);
							e.validate();
							break;
						case Foundation.PV_STATION:
							e.getPvStationDailyEnergyGraph().clearData();
							e.getPvStationDailyEnergyGraph().addGraph(f);
							e.validate();
							break;
						case Foundation.CSP_STATION:
							e.getCspStationDailyEnergyGraph().clearData();
							e.getCspStationDailyEnergyGraph().addGraph(f);
							e.validate();
							break;
						}
					}
				}
			});
		}
	}

	public static void openNow(final URL file) throws Exception {
		if (PrintController.getInstance().isPrintPreview()) {
			MainPanel.getInstance().getPreviewButton().setSelected(false);
			while (!PrintController.getInstance().isFinished()) {
				Thread.yield();
			}
		}

		synchronized (SceneManager.getInstance()) {
			Scene.url = file;

			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					MainPanel.getInstance().getHeliodonButton().setSelected(false);
					MainPanel.getInstance().getSunAnimationButton().setSelected(false);
				}
			});
			SceneManager.getInstance().setSolarHeatMapWithoutUpdate(false);
			Wall.resetDefaultWallHeight();

			if (instance != null) {
				instance.deleteAll();
			}
			if (url == null) {
				instance = new Scene();
				System.out.println("done");
			} else {
				System.out.print("Opening..." + file + "...");
				final ObjectInputStream in = new ObjectInputStream(file.openStream());
				instance = (Scene) in.readObject();
				in.close();
				for (final HousePart part : instance.parts) {
					part.getRoot();
				}
				instance.upgradeSceneToNewVersion();
				instance.cleanup();
				loadCameraLocation();
			}
			instance.applyMap(true);
			SceneManager.getInstance().hideAllEditPoints();
			final CameraControl cameraControl = SceneManager.getInstance().getCameraControl();
			if (cameraControl != null) {
				cameraControl.reset();
			}

			int count = 0;
			Foundation first = null;
			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p instanceof Foundation && !p.isFrozen()) {
					if (count == 0) {
						first = (Foundation) p;
					}
					count++;
				}
			}
			if (count == 1) {
				SceneManager.getInstance().setSelectedPart(first);
			}

			instance.init();
			instance.redrawAllNow(); // needed in case Heliodon is on and needs to be drawn with correct size
			SceneManager.getInstance().updateHeliodonAndAnnotationSize();

		}

		EventQueue.invokeLater(new Runnable() { // update GUI must be called in Event Queue to prevent possible deadlocks

			@Override
			public void run() {
				if (instance.textureMode == TextureMode.None) {
					MainFrame.getInstance().getNoTextureMenuItem().setSelected(true);
				} else if (instance.textureMode == TextureMode.Simple) {
					MainFrame.getInstance().getSimpleTextureMenuItem().setSelected(true);
				} else {
					MainFrame.getInstance().getFullTextureMenuItem().setSelected(true);
				}
				MainPanel.getInstance().getAnnotationButton().setSelected(instance.isAnnotationsVisible);
				MainFrame.getInstance().updateTitleBar();
			}

		});

	}

	private void init() {

		root.detachAllChildren();
		originalHouseRoot.detachAllChildren();
		notReceivingShadowRoot.detachAllChildren();
		root.attachChild(originalHouseRoot);
		root.attachChild(notReceivingShadowRoot);

		if (url != null) {
			for (final HousePart p : parts) {
				final boolean b = p instanceof Tree || p instanceof Human;
				(b ? notReceivingShadowRoot : originalHouseRoot).attachChild(p.getRoot());
			}
			System.out.println("initSceneNow done");
			/* must redraw now so that heliodon can be initialized to right size if it is to be visible */
			// redrawAllNow();
		}

		root.updateWorldBound(true);
		SceneManager.getInstance().updateHeliodonAndAnnotationSize();
		SceneManager.getInstance().setAxesVisible(!hideAxes);
		SceneManager.getInstance().setBuildingLabelsVisible(showBuildingLabels);

		setTheme(theme);
		SceneManager.getInstance().getLand().setDefaultColor(landColor != null ? landColor : new ColorRGBA(0, 1, 0, 0.5f));

		final EnergyPanel energyPanel = EnergyPanel.getInstance();
		if (calendar != null) {
			final Date time = calendar.getTime();
			Heliodon.getInstance().setDate(time);
			Heliodon.getInstance().setTime(time);
			Util.setSilently(energyPanel.getDateSpinner(), time);
			Util.setSilently(energyPanel.getTimeSpinner(), time);
			if ("Boston".equals(city) || city == null || "".equals(city)) {
				city = "Boston, MA";
				latitude = 42;
			}
			energyPanel.setLatitude(latitude); // already silent
			Util.selectSilently(energyPanel.getCityComboBox(), city);
			Scene.getInstance().setTreeLeaves();
			MainPanel.getInstance().getHeliodonButton().setSelected(isHeliodonVisible);
			Heliodon.getInstance().drawSun();
			SceneManager.getInstance().changeSkyTexture();
			SceneManager.getInstance().setShading(Heliodon.getInstance().isNightTime());
		}

		// previous versions do not have the following classes
		if (designSpecs == null) {
			designSpecs = new DesignSpecs();
		} else {
			designSpecs.setDefaultValues();
		}
		if (ground == null) {
			ground = new Ground();
		}
		if (unit == null) {
			unit = Unit.InternationalSystemOfUnits;
		}

		// restore the default values
		if (Util.isZero(heatVectorLength)) {
			heatVectorLength = 5000;
		}
		if (Util.isZero(solarStep)) {
			solarStep = 2;
		}
		if (Util.isZero(timeStep)) {
			timeStep = 15;
		}
		if (Util.isZero(plateNx)) {
			plateNx = 2;
		}
		if (Util.isZero(plateNy)) {
			plateNy = 2;
		}
		if (Util.isZero(solarContrast)) {
			solarContrast = 50;
		}

		setEdited(false);
		setCopyBuffer(null);

		Util.setSilently(energyPanel.getColorMapSlider(), solarContrast);
		Util.setSilently(MainPanel.getInstance().getNoteTextArea(), note == null ? "" : note); // need to do this to avoid logging

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				energyPanel.updateThermostat();
				MainPanel.getInstance().setNoteVisible(MainPanel.getInstance().isNoteVisible()); // necessary for the scroll bars to show up appropriately
				MainPanel.getInstance().getEnergyViewButton().setSelected(false); // moved from OpenNow to here to avoid triggering EnergyComputer -> RedrawAllNow before open is completed
				SceneManager.getInstance().getUndoManager().die();
			}
		});

	}

	public static double parsePropertyString(final String s) {
		final int indexOfSpace = s.indexOf(' ');
		return Double.parseDouble(s.substring(0, indexOfSpace != -1 ? indexOfSpace : s.length()));
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
			while (!PrintController.getInstance().isFinished()) {
				Thread.yield();
			}
		}

		if (url != null) {

			long max = -1;
			for (final HousePart x : Scene.getInstance().parts) {
				if (x.getId() > max) {
					max = x.getId();
				}
			}
			if (max < 0) {
				max = 0;
			}

			System.out.print("Opening..." + url + "...");
			final ObjectInputStream in = new ObjectInputStream(url.openStream());
			final Scene instance = (Scene) in.readObject();
			in.close();

			// instance.cleanup();
			instance.upgradeSceneToNewVersion();

			if (url != null) {
				final AddMultiplePartsCommand cmd = new AddMultiplePartsCommand(new ArrayList<HousePart>(instance.getParts()), url);
				synchronized (SceneManager.getInstance()) {
					double cx = 0;
					double cy = 0;
					int count = 0;
					for (final HousePart p : instance.getParts()) {
						p.setId(max + p.getId());
						Scene.getInstance().parts.add(p);
						originalHouseRoot.attachChild(p.getRoot());
						if (p instanceof Foundation || p instanceof Tree || p instanceof Human) {
							final Vector3 c = p.getAbsCenter();
							cx += c.getX();
							cy += c.getY();
							count++;
						}
					}
					final Vector3 position = SceneManager.getInstance().getPickedLocationOnLand();
					if (position != null) {
						final Vector3 shift = position.subtractLocal(count == 0 ? new Vector3(0, 0, 0) : new Vector3(cx / count, cy / count, 0));
						for (final HousePart p : instance.getParts()) {
							if (p instanceof Foundation || p instanceof Tree || p instanceof Human) {
								for (int i = 0; i < p.getPoints().size(); i++) {
									p.getPoints().get(i).addLocal(shift);
								}
							}
						}
					}
				}
				redrawAll = true;
				SceneManager.getInstance().getUndoManager().addEdit(cmd);
			}

			root.updateWorldBound(true);
			SceneManager.getInstance().updateHeliodonAndAnnotationSize();
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					// SceneManager.getInstance().getUndoManager().die();
					// MainFrame.getInstance().refreshUndoRedo();
					MainPanel.getInstance().getEnergyViewButton().setSelected(false);
				}
			});

		} else {

			JOptionPane.showMessageDialog(MainFrame.getInstance(), "URL doesn't exist.", "Error", JOptionPane.ERROR_MESSAGE);

		}

	}

	/** This can be used by the user to fix problems that are caused by bugs based on our observations. This is different than cleanup() as the latter cannot be used to remove undrawables. */
	public void fixProblems() {

		// remove all undrawables
		final ArrayList<HousePart> a = new ArrayList<HousePart>();
		for (final HousePart p : parts) {
			if (!p.isDrawable()) {
				a.add(p);
			}
		}
		for (final HousePart p : a) {
			remove(p, false);
		}
		a.clear();

		cleanup();
		redrawAll(true);

	}

	private void deleteAll() {
		for (final HousePart p : parts) {
			p.delete();
		}
	}

	private void cleanup() {

		// fix if roof and wall are not linked from each other
		for (final HousePart p : parts) {
			if (p instanceof Roof) {
				final Roof r = (Roof) p;
				final HousePart c = r.getContainer();
				if (c != null && !c.getChildren().contains(r)) {
					c.getChildren().add(r);
				}
			}
		}

		final ArrayList<HousePart> toBeRemoved = new ArrayList<HousePart>();

		for (final HousePart p : parts) {
			// remove all invalid parts or orphan parts without a top container
			if (!p.isValid() || ((p instanceof Roof || p instanceof Window || p instanceof Door || p instanceof SolarPanel || p instanceof Floor) && p.getContainer() == null)) {
				toBeRemoved.add(p);
			}
			// remove walls that are at the same position
			if (p instanceof Wall) {
				if (((Wall) p).isAtSamePlaceAsAnotherPart()) {
					toBeRemoved.add(p);
				}
			}
		}
		for (final HousePart p : toBeRemoved) {
			remove(p, false);
		}

		// remove children with multiple parents
		toBeRemoved.clear();
		for (final HousePart p : parts) {
			for (final HousePart child : p.getChildren()) {
				if (child.getContainer() != p && !toBeRemoved.contains(child)) {
					toBeRemoved.add(child);
				}
			}
		}
		for (final HousePart p : toBeRemoved) {
			remove(p, false);
		}
		// remove from remaining parents
		for (final HousePart p : parts) {
			for (final HousePart r : toBeRemoved) {
				p.getChildren().remove(r);
			}
		}

		// remove all the children that are not in parts
		toBeRemoved.clear();
		for (final HousePart p : parts) {
			for (final HousePart child : p.getChildren()) {
				if (!parts.contains(child) && !toBeRemoved.contains(child)) {
					toBeRemoved.add(child);
				}
			}
		}
		for (final HousePart p : toBeRemoved) {
			remove(p, false);
		}

		// complete all non-completed parts
		for (final HousePart p : parts) {
			if (!p.isDrawCompleted()) {
				p.complete();
			}
		}

	}

	private void upgradeSceneToNewVersion() {
		if (textureMode == null) {
			textureMode = TextureMode.Full;
			for (final HousePart p : parts) {
				if (p instanceof Roof) {
					((Roof) p).setOverhangLength(0.2);
				}
			}
		}

		if (version < 1) {
			for (final HousePart part : parts) {
				if (part instanceof Foundation) {
					((Foundation) part).scaleHouseForNewVersion(10);
				}
			}
			cameraLocation = cameraLocation.multiply(10, null);
			setAnnotationScale(1.0);
		}

		version = currentVersion;
	}

	public void connectWalls() {
		for (final HousePart part : parts) {
			if (part instanceof Wall) {
				part.reset();
			}
		}

		for (final HousePart part : parts) {
			if (part instanceof Wall) {
				((Wall) part).connectedWalls();
			}
		}

		for (final HousePart part : parts) {
			if (part instanceof Wall) {
				((Wall) part).computeInsideDirectionOfAttachedWalls(false);
			}
		}
	}

	public static void save(final URL url, final boolean setAsCurrentFile) throws Exception {
		save(url, setAsCurrentFile, true, false);
	}

	public static void save(final URL url, final boolean setAsCurrentFile, final boolean notifyUndoManager, final boolean logger) throws Exception {
		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				instance.storeMapImageData();
				if (notifyUndoManager) {
					instance.cleanup();
				}
				// save camera to file
				saveCameraLocation();

				instance.hideAxes = !SceneManager.getInstance().areAxesVisible();
				instance.showBuildingLabels = SceneManager.getInstance().areBuildingLabelsVisible();
				instance.calendar.setTime(Heliodon.getInstance().getCalendar().getTime());
				instance.latitude = (int) Math.toDegrees(Heliodon.getInstance().getLatitude());
				instance.city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
				instance.isHeliodonVisible = Heliodon.getInstance().isVisible();
				instance.note = MainPanel.getInstance().getNoteTextArea().getText().trim();
				instance.solarContrast = EnergyPanel.getInstance().getColorMapSlider().getValue();

				if (setAsCurrentFile) {
					Scene.url = url;
				}
				if (instance.mapImage != null) {
					instance.mapImage.setData((ByteBuffer) null);
				}
				System.out.print("Saving " + url + "...");
				ObjectOutputStream out;
				out = new ObjectOutputStream(new FileOutputStream(url.toURI().getPath()));
				out.writeObject(instance);
				out.close();
				if (notifyUndoManager) {
					SceneManager.getInstance().getUndoManager().addEdit(new SaveCommand());
				}
				instance.restoreMapImageData();
				System.out.println("done");
				return null;
			}
		});
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

	public void add(final HousePart housePart, final boolean redraw) {
		final HousePart container = housePart.getContainer();
		if (container != null) {
			container.getChildren().add(housePart);
		}
		add(housePart);
		if (redraw) {
			redrawAll();
		}
	}

	private void add(final HousePart housePart) {
		System.out.println("Adding: " + housePart);
		if (housePart instanceof Tree || housePart instanceof Human) {
			notReceivingShadowRoot.attachChild(housePart.getRoot());
		} else {
			originalHouseRoot.attachChild(housePart.getRoot());
		}
		parts.add(housePart);
		for (final HousePart child : housePart.getChildren()) {
			add(child);
		}
	}

	public void remove(final HousePart housePart, final boolean redraw) {
		if (housePart == null) {
			return;
		}
		housePart.setGridsVisible(false);
		final HousePart container = housePart.getContainer();
		if (container != null) {
			container.getChildren().remove(housePart);
		}
		removeChildren(housePart);
		if (redraw) {
			redrawAll();
		}
	}

	private void removeChildren(final HousePart housePart) {
		System.out.println("Removing: " + housePart);
		parts.remove(housePart); // this must happen before call to wall.delete()
		for (final HousePart child : housePart.getChildren()) {
			removeChildren(child);
		}
		// originalHouseRoot.detachChild(housePart.getRoot());
		housePart.getRoot().removeFromParent();
		housePart.delete();
	}

	private static void setIdOfChildren(final HousePart p) {
		final ArrayList<HousePart> children = p.getChildren();
		for (final HousePart c : children) {
			c.setId(Scene.getInstance().nextID());
			if (!c.getChildren().isEmpty()) {
				setIdOfChildren(c);
			}
		}
	}

	public void setCopyBuffer(final HousePart p) {
		EnergyPanel.getInstance().clearRadiationHeatMap();
		// exclude the following types of house parts
		if (p instanceof Roof || p instanceof Floor || p instanceof Sensor) {
			return;
		}
		copyBuffer = p;
		originalCopy = p;
	}

	public HousePart getCopyBuffer() {
		return copyBuffer;
	}

	public HousePart getOriginalCopy() {
		return originalCopy;
	}

	public void paste() {
		if (copyBuffer == null) {
			return;
		}
		if (copyBuffer instanceof Foundation) {
			return;
		}
		EnergyPanel.getInstance().clearRadiationHeatMap();
		final HousePart c = copyBuffer.copy(true);
		if (c == null) {
			return;
		}
		add(c, true);
		copyBuffer = c;
		SceneManager.getInstance().getUndoManager().addEdit(new PastePartCommand(c));
		EnergyPanel.getInstance().clearRadiationHeatMap();
		EnergyPanel.getInstance().update();
	}

	public void pasteToPickedLocationOnLand() {
		EnergyPanel.getInstance().clearRadiationHeatMap();
		if (copyBuffer == null) {
			return;
		}
		final HousePart c = copyBuffer.copy(false);
		if (c == null) {
			return;
		}
		final Vector3 position = SceneManager.getInstance().getPickedLocationOnLand();
		if (position == null) {
			return;
		}
		if (c instanceof Tree || c instanceof Human) {
			c.getPoints().set(0, position);
			add(c, true);
			copyBuffer = c;
			SceneManager.getInstance().getUndoManager().addEdit(new PastePartCommand(c));
		} else if (c instanceof Foundation) { // pasting a foundation also clones the building above it
			final Vector3 shift = position.subtractLocal(c.getAbsCenter()).multiplyLocal(1, 1, 0);
			final int n = c.getPoints().size();
			for (int i = 0; i < n; i++) {
				c.getPoints().get(i).addLocal(shift);
			}
			add(c, true);
			// copy gable info, too
			final Foundation oldFoundation = (Foundation) copyBuffer;
			final Foundation newFoundation = (Foundation) c;
			final List<Roof> oldRoofs = oldFoundation.getRoofs();
			final List<Roof> newRoofs = newFoundation.getRoofs();
			if (!oldRoofs.isEmpty() && !newRoofs.isEmpty()) {
				for (int i = 0; i < newRoofs.size(); i++) {
					final Map<Integer, List<Wall>> oldMap = oldRoofs.get(i).getGableEditPointToWallMap();
					if (oldMap.isEmpty()) {
						continue;
					}
					final Map<Integer, List<Wall>> newMap = new HashMap<Integer, List<Wall>>();
					for (final Integer key : oldMap.keySet()) {
						final List<Wall> oldWalls = oldMap.get(key);
						final List<Wall> newWalls = new ArrayList<Wall>();
						for (final Wall w : oldWalls) {
							newWalls.add(getCopiedWall(w, oldFoundation, newFoundation));
						}
						newMap.put(key, newWalls);
					}
					newRoofs.get(i).setGableEditPointToWallMap(newMap);
				}
			}
			copyBuffer = c;
			setIdOfChildren(c);
			SceneManager.getInstance().getUndoManager().addEdit(new PastePartCommand(c));
		}
	}

	private Wall getCopiedWall(final Wall oldWall, final Foundation oldFoundation, final Foundation newFoundation) {
		final ArrayList<HousePart> oldWalls = oldFoundation.getChildren();
		final ArrayList<HousePart> newWalls = newFoundation.getChildren();
		final int index = oldWalls.indexOf(oldWall);
		if (index < 0) {
			return null;
		}
		return (Wall) newWalls.get(index);
	}

	public void pasteToPickedLocationOnWall() {
		EnergyPanel.getInstance().clearRadiationHeatMap();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (!(selectedPart instanceof Wall)) {
			return;
		}
		if (copyBuffer == null) {
			return;
		}
		if (copyBuffer instanceof Foundation) {
			return;
		}
		final HousePart c = copyBuffer.copy(false);
		if (c == null) {
			return;
		}
		Vector3 position = SceneManager.getInstance().getPickedLocationOnWall();
		if (position == null) {
			return;
		}
		final Wall wall = (Wall) selectedPart;
		if (c instanceof Window) { // windows can be pasted to a different wall
			if (wall != c.getContainer()) {
				((Window) c).moveTo(wall);
			}
		} else if (c instanceof SolarPanel) { // solar panels can be pasted to a different parent
			if (wall != c.getContainer()) {
				((SolarPanel) c).moveTo(wall);
			}
		}
		position = c.toRelative(position.subtractLocal(c.getContainer().getAbsPoint(0)));
		final Vector3 center = c.toRelative(c.getAbsCenter().subtractLocal(c.getContainer().getAbsPoint(0)));
		position = position.subtractLocal(center);
		final int n = c.getPoints().size();
		for (int i = 0; i < n; i++) {
			final Vector3 v = c.getPoints().get(i);
			v.addLocal(position);
		}
		// out of boundary check
		final List<Vector3> polygon = wall.getWallPolygonPoints();
		final List<Vector3> relativePolygon = new ArrayList<Vector3>();
		for (final Vector3 p : polygon) {
			relativePolygon.add(c.toRelative(p));
		}
		for (final Vector3 p : relativePolygon) {
			final double y = p.getY();
			p.setY(p.getZ());
			p.setZ(y);
		}
		for (int i = 0; i < n; i++) {
			final Vector3 v = c.getPoints().get(i);
			if (!Util.insidePolygon(new Vector3(v.getX(), v.getZ(), v.getY()), relativePolygon)) {
				return;
			}
		}
		add(c, true);
		copyBuffer = c;
		SceneManager.getInstance().getUndoManager().addEdit(new PastePartCommand(c));
	}

	public void pasteToPickedLocationOnRoof() {
		EnergyPanel.getInstance().clearRadiationHeatMap();
		if (copyBuffer == null) {
			return;
		}
		if (copyBuffer instanceof Foundation) {
			return;
		}
		final HousePart c = copyBuffer.copy(false);
		if (c == null) {
			return;
		}
		Vector3 position = SceneManager.getInstance().getPickedLocationOnRoof();
		if (position == null) {
			return;
		}
		if (c instanceof SolarPanel) { // solar panels can be pasted to a different parent
			final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
			if (selectedPart instanceof Roof && selectedPart != c.getContainer()) {
				((SolarPanel) c).moveTo(selectedPart);
			}
		}
		position = c.toRelative(position.subtractLocal(c.getContainer().getAbsPoint(0)));
		final Vector3 center = c.toRelative(c.getAbsCenter().subtractLocal(c.getContainer().getAbsPoint(0)));
		position = position.subtractLocal(center);
		final int n = c.getPoints().size();
		for (int i = 0; i < n; i++) {
			final Vector3 v = c.getPoints().get(i);
			v.addLocal(position);
		}
		add(c, true);
		copyBuffer = c;
		SceneManager.getInstance().getUndoManager().addEdit(new PastePartCommand(c));
	}

	public void pasteToPickedLocationOnFoundation() {
		EnergyPanel.getInstance().clearRadiationHeatMap();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (!(selectedPart instanceof Foundation)) {
			return;
		}
		if (copyBuffer == null) {
			return;
		}
		final HousePart c = copyBuffer.copy(false);
		if (c == null) {
			return;
		}
		Vector3 position = SceneManager.getInstance().getPickedLocationOnFoundation();
		if (position == null) {
			return;
		}
		position = c.toRelative(position.subtractLocal(c.getContainer().getAbsPoint(0)));
		final Vector3 center = c.toRelative(c.getAbsCenter().subtractLocal(c.getContainer().getAbsPoint(0)));
		position = position.subtractLocal(center);
		final int n = c.getPoints().size();
		for (int i = 0; i < n; i++) {
			final Vector3 v = c.getPoints().get(i);
			v.addLocal(position);
		}
		add(c, true);
		copyBuffer = c;
		SceneManager.getInstance().getUndoManager().addEdit(new PastePartCommand(c));
	}

	public List<HousePart> getParts() {
		return parts;
	}

	public HousePart getPart(final long id) {
		for (final HousePart p : parts) {
			if (id == p.getId()) {
				return p;
			}
		}
		return null;
	}

	public void drawResizeBounds() {
		for (final HousePart part : parts) {
			if (part instanceof Foundation) {
				part.draw();
			}
		}
	}

	public static Node getOriginalHouseRoot() {
		return originalHouseRoot;
	}

	public static Node getNotReceivingShadowRoot() {
		return notReceivingShadowRoot;
	}

	public static URL getURL() {
		return url;
	}

	public static boolean isTemplate() {
		if (Config.isEclipse()) {
			return url != null && url.toString().indexOf("/energy3d/target/classes") != -1;
		}
		return url != null && url.toString().indexOf(".jar!") != -1;
	}

	public void setAnnotationsVisible(final boolean visible) {
		isAnnotationsVisible = visible;
		for (final HousePart part : parts) {
			part.setAnnotationsVisible(visible);
		}
		if (PrintController.getInstance().isPrintPreview()) {
			for (final HousePart part : PrintController.getInstance().getPrintParts()) {
				part.setAnnotationsVisible(visible);
			}
		}
		if (PrintController.getInstance().isPrintPreview()) {
			PrintController.getInstance().restartAnimation();
		} else {
			SceneManager.getInstance().refresh();
		}

	}

	public void setTextureMode(final TextureMode textureMode) {
		this.textureMode = textureMode;
		redrawAll();
		Scene.getInstance().updateRoofDashLinesColor();
	}

	public TextureMode getTextureMode() {
		return textureMode;
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
		synchronized (SceneManager.getInstance()) {
			for (final HousePart part : instance.getParts()) {
				part.drawAnnotations();
			}
		}
		if (PrintController.getInstance().getPrintParts() != null) {
			for (final HousePart part : PrintController.getInstance().getPrintParts()) {
				part.drawAnnotations();
			}
		}
	}

	public void redrawAll() {
		redrawAll(false);
	}

	public void redrawAll(final boolean cleanup) {
		this.cleanup = cleanup;
		if (PrintController.getInstance().isPrintPreview()) {
			PrintController.getInstance().restartAnimation();
		} else {
			redrawAll = true;
		}
	}

	public void redrawAllNow() {
		System.out.println("redrawAllNow()");
		synchronized (SceneManager.getInstance()) {
			final long t = System.nanoTime();
			if (cleanup) {
				cleanup();
				cleanup = false;
			}
			connectWalls();
			Snap.clearAnnotationDrawn();
			for (final HousePart part : parts) {
				if (part instanceof Roof) {
					part.draw();
				}
			}
			for (final HousePart part : parts) {
				if (!(part instanceof Roof)) {
					part.draw();
				}
			}
			// need to draw roof again because roof holes depend on drawn windows
			for (final HousePart part : parts) {
				if (part instanceof Roof) {
					part.draw();
					// System.out.println(((Roof) part).getIntersectionCache().size());
				}
			}
			System.out.println("Time = " + (System.nanoTime() - t) / 1000000000.0);
		}
		// no need for redrawing print parts because they will be regenerated from original parts anyways
		redrawAll = false;
	}

	public void updateAllTextures() {
		System.out.println("updateAllTextures()");
		for (final HousePart part : parts) {
			part.updateTextureAndColor();
		}
		SceneManager.getInstance().refresh();
	}

	public void setUnit(final Unit unit) {
		this.unit = unit;
		redrawAll = true;
	}

	public Unit getUnit() {
		if (unit == null) {
			unit = Unit.InternationalSystemOfUnits;
		}
		return unit;
	}

	public void setAnnotationScale(final double scale) {
		annotationScale = scale;
	}

	public double getAnnotationScale() {
		if (annotationScale == 0) {
			annotationScale = 10;
		}
		return annotationScale;
	}

	public void updateRoofDashLinesColor() {
		for (final HousePart part : parts) {
			if (part instanceof Roof) {
				((Roof) part).updateDashLinesColor();
			}
		}
		if (PrintController.getInstance().getPrintParts() != null) {
			for (final HousePart part : PrintController.getInstance().getPrintParts()) {
				if (part instanceof Roof) {
					((Roof) part).updateDashLinesColor();
				}
			}
		}
	}

	public void removeAllTrees() {
		final ArrayList<HousePart> trees = new ArrayList<HousePart>();
		for (final HousePart part : parts) {
			if (part instanceof Tree && !part.isFrozen()) {
				trees.add(part);
			}
		}
		if (trees.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no tree to remove.", "No Tree", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + trees.size() + " trees?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
			return;
		}
		final RemoveMultiplePartsCommand c = new RemoveMultiplePartsCommand(trees);
		for (final HousePart part : trees) {
			remove(part, false);
		}
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(c);
		edited = true;
	}

	public void removeAllHumans() {
		final ArrayList<HousePart> humans = new ArrayList<HousePart>();
		for (final HousePart part : parts) {
			if (part instanceof Human) {
				humans.add(part);
			}
		}
		if (humans.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no human to remove.", "No Human", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + humans.size() + " humans?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
			return;
		}
		final RemoveMultiplePartsCommand c = new RemoveMultiplePartsCommand(humans);
		for (final HousePart part : humans) {
			remove(part, false);
		}
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(c);
		edited = true;
	}

	public void removeAllRoofs() {
		final ArrayList<HousePart> roofs = new ArrayList<HousePart>();
		for (final HousePart part : parts) {
			if (part instanceof Roof && !part.isFrozen()) {
				roofs.add(part);
			}
		}
		if (roofs.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no roof to remove.", "No Roof", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + roofs.size() + " roofs?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
			return;
		}
		final RemoveMultiplePartsCommand c = new RemoveMultiplePartsCommand(roofs);
		for (final HousePart part : roofs) {
			remove(part, false);
		}
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(c);
		edited = true;
	}

	public void removeAllFloors() {
		final ArrayList<HousePart> floors = new ArrayList<HousePart>();
		for (final HousePart part : parts) {
			if (part instanceof Floor && !part.isFrozen()) {
				floors.add(part);
			}
		}
		if (floors.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no floor to remove.", "No Floor", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + floors.size() + " floors?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
			return;
		}
		final RemoveMultiplePartsCommand c = new RemoveMultiplePartsCommand(floors);
		for (final HousePart part : floors) {
			remove(part, false);
		}
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(c);
		edited = true;
	}

	public void removeAllSolarPanels() {
		final ArrayList<HousePart> panels = new ArrayList<HousePart>();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart != null) {
			final Foundation foundation = selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer();
			for (final HousePart part : parts) {
				if (part instanceof SolarPanel && !part.isFrozen() && part.getTopContainer() == foundation) {
					panels.add(part);
				}
			}
		} else {
			for (final HousePart part : parts) {
				if (part instanceof SolarPanel && !part.isFrozen()) {
					panels.add(part);
				}
			}
		}
		if (panels.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no solar panel to remove.", "No Solar Panel", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + panels.size() + " solar panels" + (selectedPart != null ? " of the selected building" : "") + "?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
			return;
		}
		final RemoveMultiplePartsCommand c = new RemoveMultiplePartsCommand(panels);
		for (final HousePart part : panels) {
			remove(part, false);
		}
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(c);
		edited = true;
	}

	public void removeAllMirrors() {
		final ArrayList<HousePart> mirrors = new ArrayList<HousePart>();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart != null) {
			final Foundation foundation = selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer();
			for (final HousePart part : parts) {
				if (part instanceof Mirror && !part.isFrozen() && part.getTopContainer() == foundation) {
					mirrors.add(part);
				}
			}
		} else {
			for (final HousePart part : parts) {
				if (part instanceof Mirror && !part.isFrozen()) {
					mirrors.add(part);
				}
			}
		}
		if (mirrors.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no mirror to remove.", "No Mirror", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + mirrors.size() + " mirrors" + (selectedPart != null ? " on the selected foundation" : "") + "?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
			return;
		}
		final RemoveMultiplePartsCommand c = new RemoveMultiplePartsCommand(mirrors);
		for (final HousePart part : mirrors) {
			remove(part, false);
		}
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(c);
		edited = true;
	}

	public void removeAllWindows() {
		final ArrayList<HousePart> windows = new ArrayList<HousePart>();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart != null) {
			final Foundation foundation = selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer();
			for (final HousePart part : parts) {
				if (part instanceof Window && !part.isFrozen() && part.getTopContainer() == foundation) {
					windows.add(part);
				}
			}
		} else {
			for (final HousePart part : parts) {
				if (part instanceof Window && !part.isFrozen()) {
					windows.add(part);
				}
			}
		}
		if (windows.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no window to remove.", "No Window", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + windows.size() + " windows" + (selectedPart != null ? " of the selected building" : "") + "?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
			return;
		}
		final RemoveMultiplePartsCommand c = new RemoveMultiplePartsCommand(windows);
		for (final HousePart part : windows) {
			remove(part, false);
		}
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(c);
		edited = true;
	}

	public void removeAllWindowShutters() {
		final ArrayList<Window> windows = new ArrayList<Window>();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart != null) {
			final Foundation foundation = selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer();
			for (final HousePart part : parts) {
				if (part instanceof Window && !part.isFrozen() && part.getTopContainer() == foundation) {
					final Window w = (Window) part;
					if (w.getLeftShutter() || w.getRightShutter()) {
						windows.add(w);
					}
				}
			}
		} else {
			for (final HousePart part : parts) {
				if (part instanceof Window && !part.isFrozen()) {
					final Window w = (Window) part;
					if (w.getLeftShutter() || w.getRightShutter()) {
						windows.add(w);
					}
				}
			}
		}
		if (windows.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no window shutter to remove.", "No Shutter", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + windows.size() + " window shutters" + (selectedPart != null ? " of the selected building" : "") + "?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
			return;
		}
		final RemoveMultipleShuttersCommand c = new RemoveMultipleShuttersCommand(windows);
		for (final HousePart part : windows) {
			final Window w = (Window) part;
			w.setLeftShutter(false);
			w.setRightShutter(false);
		}
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(c);
		edited = true;
	}

	public void removeAllFoundations() {
		final ArrayList<HousePart> foundations = new ArrayList<HousePart>();
		for (final HousePart part : parts) {
			if (part instanceof Foundation && !part.isFrozen()) {
				foundations.add(part);
			}
		}
		if (foundations.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no activated foundation to remove.", "No Foundation", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + foundations.size() + " foundations?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
			return;
		}
		final RemoveMultiplePartsCommand c = new RemoveMultiplePartsCommand(foundations);
		for (final HousePart part : foundations) {
			remove(part, false);
		}
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(c);
		edited = true;
	}

	public void removeAllChildren(final HousePart parent) {
		final List<HousePart> children = parent.getChildren();
		final String s = parent.getClass().getSimpleName();
		final List<HousePart> copy = new ArrayList<HousePart>(); // make a copy to avoid ConcurrentModificationException
		for (final HousePart p : children) {
			if (p instanceof Roof) {
				continue; // make an exception of roof (it is a child of a wall)
			}
			copy.add(p);
		}
		if (copy.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no element to remove from " + s + ".", "No Element", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + copy.size() + " elements of " + s + "?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.NO_OPTION) {
			return;
		}
		final RemoveMultiplePartsCommand c = new RemoveMultiplePartsCommand(copy);
		for (final HousePart p : copy) {
			remove(p, false);
		}
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(c);
		edited = true;
	}

	public void lockAll(final boolean freeze) {
		if (parts.isEmpty()) {
			return;
		}
		int lockCount = 0;
		for (final HousePart part : parts) {
			if (part.isFrozen()) {
				lockCount++;
			}
		}
		if (!freeze) {
			if (lockCount > 0) {
				if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "<html>A lock prevents a component from being edited.<br>Do you really want to remove all the existing " + lockCount + " locks?</html>", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
					return;
				}
			} else {
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>A lock prevents a component from being edited.<br>There is no lock to remove.</html>");
				return;
			}
		}
		SceneManager.getInstance().getUndoManager().addEdit(new LockAllCommand());
		for (final HousePart part : parts) {
			part.setFreeze(freeze);
		}
		if (freeze) {
			SceneManager.getInstance().hideAllEditPoints();
		}
		redrawAll();
		edited = true;
	}

	public static boolean isRedrawAll() {
		return redrawAll;
	}

	public boolean areAnnotationsVisible() {
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
		for (final HousePart part : parts) {
			if (part instanceof Roof) {
				((Roof) part).removeAllGables();
			}
		}
	}

	public ReadOnlyColorRGBA getLandColor() {
		return landColor;
	}

	public void setLandColor(final ReadOnlyColorRGBA c) {
		landColor = c;
		SceneManager.getInstance().getLand().setDefaultColor(landColor);
	}

	/** get the default color for foundations */
	public ReadOnlyColorRGBA getFoundationColor() {
		if (foundationColor == null) {
			return WHITE;
		}
		return foundationColor;
	}

	/** set the default color for foundations */
	public void setFoundationColor(final ReadOnlyColorRGBA foundationColor) {
		this.foundationColor = foundationColor;
	}

	/** get the default color for walls */
	public ReadOnlyColorRGBA getWallColor() {
		if (wallColor == null) {
			return GRAY;
		}
		return wallColor;
	}

	/** set the default color for walls */
	public void setWallColor(final ReadOnlyColorRGBA wallColor) {
		this.wallColor = wallColor;
	}

	/** get the default color for doors */
	public ReadOnlyColorRGBA getDoorColor() {
		if (doorColor == null) {
			return WHITE;
		}
		return doorColor;
	}

	/** set the default color for doors */
	public void setDoorColor(final ReadOnlyColorRGBA doorColor) {
		this.doorColor = doorColor;
	}

	/** get the default color for floors */
	public ReadOnlyColorRGBA getFloorColor() {
		if (floorColor == null) {
			return WHITE;
		}
		return floorColor;
	}

	/** set the default color for floors */
	public void setFloorColor(final ReadOnlyColorRGBA floorColor) {
		this.floorColor = floorColor;
	}

	/** get the default color for roofs */
	public ReadOnlyColorRGBA getRoofColor() {
		if (roofColor == null) {
			return WHITE;
		}
		return roofColor;
	}

	/** set the default color for roofs */
	public void setRoofColor(final ReadOnlyColorRGBA roofColor) {
		this.roofColor = roofColor;
	}

	public void setWindowColorInContainer(final HousePart container, final ColorRGBA c, final boolean shutter) {
		for (final HousePart p : parts) {
			if (p instanceof Window && p.getContainer() == container) {
				final Window w = (Window) p;
				if (shutter) {
					w.setShutterColor(c);
				} else {
					w.setColor(c);
				}
				w.draw();
			}
		}
	}

	public void setShutterLengthInContainer(final HousePart container, final double length) {
		for (final HousePart p : parts) {
			if (p instanceof Window && p.getContainer() == container) {
				final Window w = (Window) p;
				w.setShutterLength(length);
				w.draw();
			}
		}
	}

	public void setShutterColorOfBuilding(final HousePart part, final ReadOnlyColorRGBA color) {
		if (part instanceof Foundation) {
			return;
		}
		for (final HousePart p : parts) {
			if (p instanceof Window && p.getTopContainer() == part.getTopContainer()) {
				final Window w = (Window) p;
				w.setShutterColor(color);
				w.draw();
			}
		}
	}

	public void setShutterLengthOfBuilding(final HousePart part, final double length) {
		if (part instanceof Foundation) {
			return;
		}
		for (final HousePart p : parts) {
			if (p instanceof Window && p.getTopContainer() == part.getTopContainer()) {
				final Window w = (Window) p;
				w.setShutterLength(length);
				w.draw();
			}
		}
	}

	public void setPartColorOfBuilding(final HousePart part, final ReadOnlyColorRGBA color) {
		if (part instanceof Foundation) {
			part.setColor(color);
		} else {
			for (final HousePart p : parts) {
				if (p.getTopContainer() == part.getTopContainer() && p.getClass().equals(part.getClass())) {
					p.setColor(color);
				}
			}
		}
	}

	public void setColorOfAllPartsOfSameType(final HousePart part, final ReadOnlyColorRGBA color) {
		for (final HousePart p : parts) {
			if (p.getClass().equals(part.getClass())) {
				p.setColor(color);
			}
		}
	}

	public List<HousePart> getPartsOfSameTypeInBuilding(final HousePart x) {
		final List<HousePart> list = new ArrayList<HousePart>();
		if (x instanceof Foundation) {
			list.add(x);
		} else {
			for (final HousePart p : parts) {
				if (p.getClass().equals(x.getClass()) && p.getTopContainer() == x.getTopContainer()) {
					list.add(p);
				}
			}
		}
		return list;
	}

	public List<HousePart> getAllPartsOfSameType(final HousePart x) {
		final List<HousePart> list = new ArrayList<HousePart>();
		for (final HousePart p : parts) {
			if (p.getClass().equals(x.getClass())) {
				list.add(p);
			}
		}
		return list;
	}

	public void setUValuesOfSameTypeInBuilding(final HousePart x, final double uValue) {
		if (x instanceof Thermalizable) {
			if (x instanceof Foundation) {
				((Foundation) x).setUValue(uValue);
			} else {
				for (final HousePart p : parts) {
					if (p.getClass().equals(x.getClass()) && p.getTopContainer() == x.getTopContainer()) {
						((Thermalizable) p).setUValue(uValue);
					}
				}
			}
		}
	}

	public List<Window> getWindowsOnContainer(final HousePart container) {
		final List<Window> list = new ArrayList<Window>();
		for (final HousePart p : parts) {
			if (p instanceof Window && p.getContainer() == container) {
				list.add((Window) p);
			}
		}
		return list;
	}

	public void setWindowShgcInContainer(final HousePart container, final double shgc) {
		for (final HousePart p : parts) {
			if (p instanceof Window && p.getContainer() == container) {
				((Window) p).setSolarHeatGainCoefficient(shgc);
			}
		}
	}

	public List<Window> getWindowsOfBuilding(final Foundation foundation) {
		final List<Window> list = new ArrayList<Window>();
		for (final HousePart p : parts) {
			if (p instanceof Window && p.getTopContainer() == foundation) {
				list.add((Window) p);
			}
		}
		return list;
	}

	public void setWindowShgcOfBuilding(final Foundation foundation, final double shgc) {
		for (final HousePart p : parts) {
			if (p instanceof Window && p.getTopContainer() == foundation) {
				((Window) p).setSolarHeatGainCoefficient(shgc);
			}
		}
	}

	public List<SolarPanel> getAllSolarPanels() {
		final List<SolarPanel> list = new ArrayList<SolarPanel>();
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel) {
				list.add((SolarPanel) p);
			}
		}
		return list;
	}

	public void setTiltAngleForSolarPanelsOnFoundation(final Foundation foundation, final double angle) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel && p.getTopContainer() == foundation) {
				((SolarPanel) p).setTiltAngle(angle);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setTiltAngleForAllSolarPanels(final double angle) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel) {
				((SolarPanel) p).setTiltAngle(angle);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setAzimuthForSolarPanelsOnFoundation(final Foundation foundation, final double angle) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel && p.getTopContainer() == foundation) {
				((SolarPanel) p).setRelativeAzimuth(angle);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setAzimuthForAllSolarPanels(final double angle) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel) {
				((SolarPanel) p).setRelativeAzimuth(angle);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setBaseHeightForSolarPanelsOnFoundation(final Foundation foundation, final double baseHeight) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel && p.getTopContainer() == foundation) {
				((SolarPanel) p).setBaseHeight(baseHeight);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setBaseHeightForAllSolarPanels(final double baseHeight) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel) {
				((SolarPanel) p).setBaseHeight(baseHeight);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setTrackerForSolarPanelsOnFoundation(final Foundation foundation, final int tracker) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel && p.getTopContainer() == foundation) {
				((SolarPanel) p).setTracker(tracker);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setTrackerForAllSolarPanels(final int tracker) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel) {
				((SolarPanel) p).setTracker(tracker);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setShadeToleranceForSolarPanelsOnFoundation(final Foundation foundation, final int cellWiring) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel && p.getTopContainer() == foundation) {
				((SolarPanel) p).setShadeTolerance(cellWiring);
			}
		}
	}

	public void setShadeToleranceForAllSolarPanels(final int cellWiring) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel) {
				((SolarPanel) p).setShadeTolerance(cellWiring);
			}
		}
	}

	public void setSolarCellEfficiencyOnFoundation(final Foundation foundation, final double eff) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel && p.getTopContainer() == foundation) {
				((SolarPanel) p).setCellEfficiency(eff);
			}
		}
	}

	public void setSolarCellEfficiencyForAll(final double eff) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel) {
				((SolarPanel) p).setCellEfficiency(eff);
			}
		}
	}

	public void setSolarPanelInverterEfficiencyOnFoundation(final Foundation foundation, final double eff) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel && p.getTopContainer() == foundation) {
				((SolarPanel) p).setInverterEfficiency(eff);
			}
		}
	}

	public void setSolarPanelInverterEfficiencyForAll(final double eff) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel) {
				((SolarPanel) p).setInverterEfficiency(eff);
			}
		}
	}

	public List<Mirror> getAllMirrors() {
		final List<Mirror> list = new ArrayList<Mirror>();
		for (final HousePart p : parts) {
			if (p instanceof Mirror) {
				list.add((Mirror) p);
			}
		}
		return list;
	}

	public void setZenithAngleForMirrorsOfFoundation(final Foundation foundation, final double angle) {
		for (final HousePart p : parts) {
			if (p instanceof Mirror && p.getTopContainer() == foundation) {
				((Mirror) p).setTiltAngle(angle);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setTiltAngleForAllMirrors(final double angle) {
		for (final HousePart p : parts) {
			if (p instanceof Mirror) {
				((Mirror) p).setTiltAngle(angle);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setAzimuthForMirrorsOnFoundation(final Foundation foundation, final double angle) {
		for (final HousePart p : parts) {
			if (p instanceof Mirror && p.getTopContainer() == foundation) {
				((Mirror) p).setRelativeAzimuth(angle);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setAzimuthForAllMirrors(final double angle) {
		for (final HousePart p : parts) {
			if (p instanceof Mirror) {
				((Mirror) p).setRelativeAzimuth(angle);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setReflectivityForMirrorsOnFoundation(final Foundation foundation, final double reflectivity) {
		for (final HousePart p : parts) {
			if (p instanceof Mirror && p.getTopContainer() == foundation) {
				((Mirror) p).setReflectivity(reflectivity);
			}
		}
	}

	public void setReflectivityForAllMirrors(final double reflectivity) {
		for (final HousePart p : parts) {
			if (p instanceof Mirror) {
				((Mirror) p).setReflectivity(reflectivity);
			}
		}
	}

	public void setTargetForMirrorsOnFoundation(final Foundation foundation, final Foundation target) {
		for (final HousePart p : parts) {
			if (p instanceof Mirror && p.getTopContainer() == foundation) {
				((Mirror) p).setHeliostatTarget(target);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setTargetForAllMirrors(final Foundation target) {
		for (final HousePart p : parts) {
			if (p instanceof Mirror) {
				((Mirror) p).setHeliostatTarget(target);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setBaseHeightForMirrorsOnFoundation(final Foundation foundation, final double baseHeight) {
		for (final HousePart p : parts) {
			if (p instanceof Mirror && p.getTopContainer() == foundation) {
				((Mirror) p).setBaseHeight(baseHeight);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setBaseHeightForAllMirrors(final double baseHeight) {
		for (final HousePart p : parts) {
			if (p instanceof Mirror) {
				((Mirror) p).setBaseHeight(baseHeight);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public boolean isEdited() {
		return edited;
	}

	public void setEdited(final boolean edited) {
		setEdited(edited, true);
	}

	public void setEdited(final boolean edited, final boolean recomputeEnergy) {
		if (edited) {
			SnapshotLogger.getInstance().setSceneEdited(true);
		}
		this.edited = edited;
		MainFrame.getInstance().updateTitleBar();
	}

	public void updateEditShapes() {
		for (final HousePart part : parts) {
			part.updateEditShapes();
		}
	}

	public long nextID() {
		return ++idCounter;
	}

	public boolean hasSensor() {
		for (final HousePart housePart : parts) {
			if (housePart instanceof Sensor) {
				return true;
			}
		}
		return false;
	}

	public void setProjectName(final String projectName) {
		this.projectName = projectName;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setStudentMode(final boolean b) {
		studentMode = b;
	}

	public boolean isStudentMode() {
		return studentMode;
	}

	public void setCity(final String city) {
		this.city = city;
	}

	public String getCity() {
		return city;
	}

	public void setDate(final Date date) {
		if (calendar != null) {
			calendar.setTime(date);
		}
	}

	public Date getDate() {
		if (calendar != null) {
			return calendar.getTime();
		}
		return Heliodon.getInstance().getCalendar().getTime();
	}

	public void setHeatVectorLength(final double heatVectorLength) {
		this.heatVectorLength = heatVectorLength;
	}

	public double getHeatVectorLength() {
		return heatVectorLength;
	}

	public boolean getAlwaysComputeHeatFluxVectors() {
		return alwaysComputeHeatFluxVectors;
	}

	public void setAlwaysComputeHeatFluxVectors(final boolean alwaysComputeHeatFluxVectors) {
		this.alwaysComputeHeatFluxVectors = alwaysComputeHeatFluxVectors;
		for (final HousePart part : Scene.getInstance().getParts()) {
			part.updateHeatFluxVisibility();
		}
	}

	public boolean getOnlyAbsorptionInSolarMap() {
		return !fullEnergyInSolarMap;
	}

	public void setOnlyAbsorptionInSolarMap(final boolean onlyAbsorptionInSolarMap) {
		fullEnergyInSolarMap = !onlyAbsorptionInSolarMap;
	}

	public void setAllowFoundationOverlap(final boolean allowFoundationOverlap) {
		this.allowFoundationOverlap = allowFoundationOverlap;
	}

	public boolean getAllowFoundationOverlap() {
		return allowFoundationOverlap;
	}

	public void setSolarHeatMapColorContrast(final int solarContrast) {
		this.solarContrast = solarContrast;
	}

	public int getSolarHeatMapColorContrast() {
		return solarContrast;
	}

	public int getNumberOfSolarPanels() {
		int count = 0;
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel) {
				count++;
			}
		}
		return count;
	}

	public int getNumberOfMirrors() {
		int count = 0;
		for (final HousePart p : parts) {
			if (p instanceof Mirror) {
				count++;
			}
		}
		return count;
	}

	// XIE: This needs to be called for trees to change texture when the month changes
	public void setTreeLeaves() {
		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				for (final HousePart p : parts) {
					if (p instanceof Tree) {
						p.updateTextureAndColor();
					}
				}
				return null;
			}
		});
	}

	public void updateMirrors() {
		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				final boolean night = Heliodon.getInstance().isNightTime();
				for (final HousePart part : parts) {
					if (part instanceof Mirror) {
						final Mirror m = (Mirror) part;
						if (night) {
							m.drawLightBeams(); // call this so that the light beams can be set invisible
						} else {
							m.draw();
						}
					}
				}
				return null;
			}
		});
	}

	public void updateSolarPanels() {
		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				final boolean night = Heliodon.getInstance().isNightTime();
				for (final HousePart part : parts) {
					if (part instanceof SolarPanel) {
						final SolarPanel sp = (SolarPanel) part;
						if (night) {
							sp.drawSunBeam();
						} else {
							sp.draw();
						}
					}
				}
				return null;
			}
		});
	}

	public Ground getGround() {
		return ground;
	}

	public DesignSpecs getDesignSpecs() {
		return designSpecs;
	}

	public void setDashedLinesOnRoofShown(final boolean dashedLineOnRoofs) {
		this.dashedlineOnRoofs = dashedLineOnRoofs;
	}

	public boolean areDashedLinesOnRoofShown() {
		return dashedlineOnRoofs;
	}

	public void setOnlySolarAnalysis(final boolean onlySolarAnalysis) {
		this.onlySolarAnalysis = onlySolarAnalysis;
	}

	public boolean getOnlySolarAnalysis() {
		return onlySolarAnalysis;
	}

	public void setUtilityBill(final UtilityBill utilityBill) {
		this.utilityBill = utilityBill;
	}

	public UtilityBill getUtilityBill() {
		return utilityBill;
	}

	public void setTheme(final int theme) {
		this.theme = theme;
		ReadOnlyColorRGBA c;
		switch (theme) {
		case DESERT:
			c = new ColorRGBA(1, 1, 1, 0.5f);
			break;
		case GRASSLAND:
			c = new ColorRGBA(0, 1, 0, 0.5f);
			break;
		case FOREST:
			c = new ColorRGBA(0, 1, 0.2f, 0.5f);
			break;
		default:
			c = new ColorRGBA(0, 1, 0, 0.5f);
		}
		setLandColor(c);
		SceneManager.getInstance().changeSkyTexture();
	}

	public int getTheme() {
		return theme;
	}

	public void setLightBeamsVisible(final boolean showLightBeams) {
		hideLightBeams = !showLightBeams;
	}

	public boolean areLightBeamsVisible() {
		return !hideLightBeams;
	}

	public void setSunAnglesVisible(final boolean showSunAngles) {
		this.showSunAngles = showSunAngles;
	}

	public boolean areSunAnglesVisible() {
		return showSunAngles;
	}

	public void setPlateNx(final int plateNx) {
		this.plateNx = plateNx;
	}

	public int getPlateNx() {
		return plateNx;
	}

	public void setPlateNy(final int plateNy) {
		this.plateNy = plateNy;
	}

	public int getPlateNy() {
		return plateNy;
	}

	public void setSolarStep(final double solarStep) {
		this.solarStep = solarStep;
	}

	public double getSolarStep() {
		return solarStep;
	}

	public void setTimeStep(final int timeStep) {
		this.timeStep = timeStep;
	}

	public int getTimeStep() {
		return timeStep;
	}

	public void setMap(final Image mapImage, final double mapScale) {
		this.mapImage = mapImage;
		if (mapImage != null) {
			this.mapScale = mapScale;
			final ByteBuffer byteBuffer = mapImage.getData().get(0);
			mapImageBytes = new byte[byteBuffer.limit()];
			byteBuffer.get(mapImageBytes);
		} else {
			mapImageBytes = null;
		}
		applyMap(false);
	}

	private void applyMap(final boolean init) {
		if (mapImage == null) {
			SceneManager.getInstance().getMapLand().setVisible(false);
			setFoundationsVisible(true);
		} else {
			if (init) {
				mapImage.setData(ByteBuffer.wrap(mapImageBytes));
			}
			SceneManager.getInstance().resizeMapLand(mapScale);
			final Texture2D texture = new Texture2D();
			texture.setTextureKey(TextureKey.getRTTKey(MinificationFilter.NearestNeighborNoMipMaps));
			texture.setImage(mapImage);
			final TextureState textureState = new TextureState();
			textureState.setTexture(texture);
			final Mesh mesh = SceneManager.getInstance().getMapLand();
			mesh.setRenderState(textureState);
			mesh.setVisible(true);
			setFoundationsVisible(false);
		}
	}

	private void setFoundationsVisible(final boolean visible) {
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Foundation) {
				part.getMesh().setVisible(visible);
			}
		}
		SceneManager.getInstance().refresh();
	}

	/** used by SnapshotLogger */
	private void storeMapImageData() {
		if (mapImageBytes != null) {
			storedMapImage = mapImage;
			storedMapScale = mapScale;
			final int n = mapImageBytes.length;
			if (storedMapImageBytes == null || storedMapImageBytes.length != n) {
				storedMapImageBytes = new byte[n];
			}
			for (int i = 0; i < n; i++) {
				storedMapImageBytes[i] = mapImageBytes[i];
			}
			setMap(null, 1);
		}
	}

	/** used by SnapshotLogger */
	private void restoreMapImageData() {
		if (storedMapImageBytes != null) {
			final int n = storedMapImageBytes.length;
			mapImageBytes = new byte[n];
			for (int i = 0; i < n; i++) {
				mapImageBytes[i] = storedMapImageBytes[i];
			}
			setMap(storedMapImage, storedMapScale);
		}
	}

}
