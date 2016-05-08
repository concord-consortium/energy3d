package org.concord.energy3d.scene;

import java.awt.EventQueue;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.JOptionPane;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.EnergyPanel.UpdateRadiation;
import org.concord.energy3d.logger.SnapshotLogger;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
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
import org.concord.energy3d.simulation.SolarRadiation;
import org.concord.energy3d.simulation.Thermostat;
import org.concord.energy3d.undo.LockAllCommand;
import org.concord.energy3d.undo.PastePartCommand;
import org.concord.energy3d.undo.RemoveMultiplePartsOfSameTypeCommand;
import org.concord.energy3d.undo.SaveCommand;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.Util;

import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.scenegraph.Node;

public class Scene implements Serializable {

	public static enum Unit {
		InternationalSystemOfUnits, USCustomaryUnits
	};

	public static enum TextureMode {
		None, Simple, Full
	};

	public static final ReadOnlyColorRGBA WHITE = ColorRGBA.WHITE;
	public static final ReadOnlyColorRGBA GRAY = ColorRGBA.LIGHT_GRAY;

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
	private boolean showBuildingLabels;
	private double solarStep = 2.0;
	private int timeStep = 15; // in minutes
	private boolean cleanup = false;
	private double heatVectorLength = 2000;
	private boolean alwaysComputeHeatFluxVectors = false;
	private boolean fullEnergyInSolarMap = true;
	private Ground ground = new Ground();
	private DesignSpecs designSpecs = new DesignSpecs();
	private HousePart copyBuffer, originalCopy;

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
		final Foundation foundation = new Foundation(xLength, yLength);
		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				instance.add(foundation, true);
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
			EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					EnergyPanel.getInstance().update();
					EnergyPanel.getInstance().clearAllGraphs();
					if (MainFrame.getInstance().getTopViewCheckBoxMenuItem().isSelected()) { // make sure we exist the 2D top view
						MainFrame.getInstance().getTopViewCheckBoxMenuItem().setSelected(false);
						SceneManager.getInstance().resetCamera(ViewMode.NORMAL);
						SceneManager.getInstance().resetCamera();
					}
					final HousePart p = SceneManager.getInstance().getSelectedPart();
					if (p instanceof Foundation) {
						EnergyPanel.getInstance().getConstructionCostGraph().addGraph((Foundation) p);
						EnergyPanel.getInstance().getDailyEnergyGraph().clearData();
						EnergyPanel.getInstance().getDailyEnergyGraph().addGraph((Foundation) p);
						EnergyPanel.getInstance().validate();
					}
				}
			});
		}
	}

	public static void openNow(final URL file) throws Exception {
		if (PrintController.getInstance().isPrintPreview()) {
			MainPanel.getInstance().getPreviewButton().setSelected(false);
			while (!PrintController.getInstance().isFinished())
				Thread.yield();
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

			if (url == null) {
				instance = new Scene();
				System.out.println("done");
			} else {
				System.out.print("Opening..." + file + "...");
				final ObjectInputStream in = new ObjectInputStream(file.openStream());
				instance = (Scene) in.readObject();
				in.close();

				for (final HousePart part : instance.parts) {
					if (part instanceof Window) {
						final Window w = (Window) part;
						if (w.getColor() == null)
							w.setColor(new ColorRGBA(0.3f, 0.3f, 0.5f, 0.5f));
					}
					part.getRoot();
				}

				instance.upgradeSceneToNewVersion();
				instance.cleanup();
				loadCameraLocation();
			}
			SceneManager.getInstance().hideAllEditPoints();
			final CameraControl cameraControl = SceneManager.getInstance().getCameraControl();
			if (cameraControl != null)
				cameraControl.reset();

			for (final HousePart p : Scene.getInstance().getParts()) {
				if (p instanceof Foundation && !p.isFrozen()) {
					SceneManager.getInstance().setSelectedPart(p);
					break;
				}
			}

			initSceneNow();
			initEnergy();
			instance.redrawAllNow(); // needed in case Heliodon is on and needs to be drawn with correct size

		}

		EventQueue.invokeLater(new Runnable() { // update GUI must be called in Event Queue to prevent possible deadlocks

			@Override
			public void run() {
				if (!Config.isApplet()) {
					if (instance.textureMode == TextureMode.None)
						MainFrame.getInstance().getNoTextureMenuItem().setSelected(true);
					else if (instance.textureMode == TextureMode.Simple)
						MainFrame.getInstance().getSimpleTextureMenuItem().setSelected(true);
					else
						MainFrame.getInstance().getFullTextureMenuItem().setSelected(true);
				}
				MainPanel.getInstance().getAnnotationToggleButton().setSelected(instance.isAnnotationsVisible);
				MainFrame.getInstance().updateTitleBar();
			}

		});

	}

	public static void initSceneNow() {
		root.detachAllChildren();
		originalHouseRoot.detachAllChildren();
		notReceivingShadowRoot.detachAllChildren();
		root.attachChild(originalHouseRoot);
		root.attachChild(notReceivingShadowRoot);

		if (url != null) {
			for (final HousePart housePart : instance.getParts()) {
				final boolean b = housePart instanceof Tree || housePart instanceof Human;
				(b ? notReceivingShadowRoot : originalHouseRoot).attachChild(housePart.getRoot());
			}
			System.out.println("initSceneNow done");
			/* must redraw now so that heliodon can be initialized to right size if it is to be visible */
			// instance.redrawAllNow();
		}

		root.updateWorldBound(true);
		SceneManager.getInstance().updateHeliodonAndAnnotationSize();
		SceneManager.getInstance().setAxesVisible(!instance.hideAxes);
		SceneManager.getInstance().setBuildingLabelsVisible(instance.showBuildingLabels);
		Util.setSilently(MainPanel.getInstance().getNoteTextArea(), instance.note == null ? "" : instance.note); // need to do this to avoid logging
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				MainPanel.getInstance().setNoteVisible(MainPanel.getInstance().isNoteVisible()); // necessary for the scroll bars to show up appropriately
				MainPanel.getInstance().getEnergyViewButton().setSelected(false); // moved from OpenNow to here to avoid triggering EnergyComputer -> RedrawAllNow before open is completed
				SceneManager.getInstance().getUndoManager().die();
			}
		});
		instance.setEdited(false);
		instance.setCopyBuffer(null);
	}

	public static void initEnergy() {
		final EnergyPanel energyPanel = EnergyPanel.getInstance();
		if (instance.calendar != null) {
			Heliodon.getInstance().setDate(instance.calendar.getTime());
			Heliodon.getInstance().setTime(instance.calendar.getTime());
			Util.setSilently(energyPanel.getDateSpinner(), instance.calendar.getTime());
			Util.setSilently(energyPanel.getTimeSpinner(), instance.calendar.getTime());
			if ("Boston".equals(instance.city) || instance.city == null || "".equals(instance.city)) {
				instance.city = "Boston, MA";
				instance.latitude = 42;
			}
			energyPanel.setLatitude(instance.latitude); // already silent
			Util.selectSilently(energyPanel.getCityComboBox(), instance.city);
			Scene.getInstance().setTreeLeaves();
			MainPanel.getInstance().getHeliodonButton().setSelected(instance.isHeliodonVisible);
			SceneManager.getInstance().changeSkyTexture();
			// SceneManager.getInstance().setShading(Heliodon.getInstance().isNightTime());
		}

		if (Util.isZero(instance.solarContrast)) // if the solar map color contrast has not been set, set it to 50
			instance.solarContrast = 50;
		Util.setSilently(energyPanel.getColorMapSlider(), instance.solarContrast);

		// previous versions do not have the following classes

		if (instance.designSpecs == null)
			instance.designSpecs = new DesignSpecs();
		else
			instance.designSpecs.setDefaultValues();
		if (instance.ground == null)
			instance.ground = new Ground();

		if (instance.unit == null)
			instance.unit = Unit.InternationalSystemOfUnits;

		// set default properties of parts (object serialization initializes every number field to zero, forcing us to do this ugly thing)

		for (final HousePart p : instance.parts) {
			if (p instanceof Roof) {
				final Roof r = (Roof) p;
				if (Util.isZero(r.getUValue()))
					r.setUValue(0.15);
				if (Util.isZero(r.getOverhangLength()))
					r.setOverhangLength(2);
				if (Util.isZero(r.getVolumetricHeatCapacity()))
					r.setVolumetricHeatCapacity(0.5);
			} else if (p instanceof Foundation) {
				final Foundation f = (Foundation) p;
				if (Util.isZero(f.getUValue()))
					f.setUValue(0.19);
				if (Util.isZero(f.getVolumetricHeatCapacity()))
					f.setVolumetricHeatCapacity(0.5);
				if (f.getThermostat() == null)
					f.setThermostat(new Thermostat());

			} else if (p instanceof Wall) {
				final Wall w = (Wall) p;
				if (Util.isZero(w.getUValue()))
					w.setUValue(0.28);
				if (Util.isZero(w.getVolumetricHeatCapacity()))
					w.setVolumetricHeatCapacity(0.5);
			} else if (p instanceof Door) {
				final Door d = (Door) p;
				if (Util.isZero(d.getUValue()))
					d.setUValue(2);
				if (Util.isZero(d.getVolumetricHeatCapacity()))
					d.setVolumetricHeatCapacity(0.5);
			} else if (p instanceof Window) {
				final Window w = (Window) p;
				if (Util.isZero(w.getUValue()))
					w.setUValue(2);
				if (Util.isZero(w.getSolarHeatGainCoefficient()))
					w.setSolarHeatGainCoefficient(0.5);
				else if (w.getSolarHeatGainCoefficient() > 1) // backward compatibility, SHGC used to range from 0 to 100
					w.setSolarHeatGainCoefficient(w.getSolarHeatGainCoefficient() * 0.01);
				if (Util.isZero(w.getVolumetricHeatCapacity()))
					w.setVolumetricHeatCapacity(0.5);
			} else if (p instanceof SolarPanel) {
				final SolarPanel sp = (SolarPanel) p;
				if (Util.isZero(sp.getEfficiency()))
					sp.setEfficiency(0.1);
				else if (sp.getEfficiency() > 1) // backward compatibility, efficiency used to range from 0 to 100
					sp.setEfficiency(sp.getEfficiency() * 0.01);
			}

		}

		if (Util.isZero(instance.heatVectorLength))
			instance.heatVectorLength = 5000;
		SolarRadiation.getInstance().setSolarStep(Util.isZero(instance.solarStep) ? 2 : instance.solarStep);
		SolarRadiation.getInstance().setTimeStep(Util.isZero(instance.timeStep) ? 15 : instance.timeStep);
		instance.setEdited(false);

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				EnergyPanel.getInstance().updateThermostat();
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
			while (!PrintController.getInstance().isFinished())
				Thread.yield();
		}

		if (url != null) {

			long max = -1;
			for (final HousePart x : Scene.getInstance().parts) {
				if (x.getId() > max)
					max = x.getId();
			}
			if (max < 0)
				max = 0;

			System.out.print("Opening..." + url + "...");
			final ObjectInputStream in = new ObjectInputStream(url.openStream());
			final Scene instance = (Scene) in.readObject();
			in.close();

			// instance.cleanup();
			instance.upgradeSceneToNewVersion();

			if (url != null) {
				synchronized (SceneManager.getInstance()) {
					for (final HousePart housePart : instance.getParts()) {
						housePart.setId(max + housePart.getId());
						Scene.getInstance().parts.add(housePart);
						originalHouseRoot.attachChild(housePart.getRoot());
					}
				}
				redrawAll = true;
				System.out.println("done");
			}

			root.updateWorldBound(true);
			SceneManager.getInstance().updateHeliodonAndAnnotationSize();
			SceneManager.getInstance().getUndoManager().die();
			if (!Config.isApplet())
				MainFrame.getInstance().refreshUndoRedo();
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					MainPanel.getInstance().getEnergyViewButton().setSelected(false);
				}
			});
		}
	}

	private void cleanup() {
		final ArrayList<HousePart> toBeRemoved = new ArrayList<HousePart>();
		for (final HousePart part : parts) {
			if (!part.isValid() || ((part instanceof Roof || part instanceof Window || part instanceof Door) && part.getContainer() == null))
				toBeRemoved.add(part);
			else {
				removeDeadChildren(part, toBeRemoved);
				if (!part.isDrawCompleted())
					part.complete();
			}
		}

		for (final HousePart part : toBeRemoved)
			remove(part, false);
	}

	private void removeDeadChildren(final HousePart parent, final ArrayList<HousePart> toBeRemoved) {
		for (final HousePart part : parent.getChildren())
			if (!parts.contains(part))
				toBeRemoved.add(part);
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
			for (final HousePart part : parts)
				if (part instanceof Foundation)
					((Foundation) part).scaleHouse(10);
			cameraLocation = cameraLocation.multiply(10, null);
			// setOverhangLength(getOverhangLength() * 10);
			setAnnotationScale(1.0);
		}

		version = currentVersion;
	}

	public void connectWalls() {
		for (final HousePart part : parts)
			if (part instanceof Wall)
				part.reset();

		for (final HousePart part : parts)
			if (part instanceof Wall)
				((Wall) part).connectedWalls();

		for (final HousePart part : parts)
			if (part instanceof Wall)
				((Wall) part).computeInsideDirectionOfAttachedWalls(false);
	}

	public static void save(final URL url, final boolean setAsCurrentFile) throws Exception {
		save(url, setAsCurrentFile, true);
	}

	public static void save(final URL url, final boolean setAsCurrentFile, final boolean notifyUndoManager) throws Exception {
		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				if (notifyUndoManager)
					instance.cleanup();
				// save camera to file
				saveCameraLocation();

				instance.hideAxes = !SceneManager.getInstance().areAxesVisible();
				instance.showBuildingLabels = SceneManager.getInstance().areBuildingLabelsVisible();
				instance.calendar.setTime(Heliodon.getInstance().getCalender().getTime());
				instance.latitude = (int) Math.toDegrees(Heliodon.getInstance().getLatitude());
				instance.city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
				instance.isHeliodonVisible = Heliodon.getInstance().isVisible();
				instance.note = MainPanel.getInstance().getNoteTextArea().getText().trim();
				instance.solarContrast = EnergyPanel.getInstance().getColorMapSlider().getValue();
				instance.solarStep = SolarRadiation.getInstance().getSolarStep();
				instance.timeStep = SolarRadiation.getInstance().getTimeStep();

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
		SceneManager.getTaskManager().update(new Callable<Object>() {
			public Object call() {
				final HousePart container = housePart.getContainer();
				if (container != null)
					container.getChildren().add(housePart);
				add(housePart);
				return null;
			}
		});
		if (redraw)
			redrawAll();
	}

	private void add(final HousePart housePart) {
		System.out.println("Adding: " + housePart);
		if (housePart instanceof Tree || housePart instanceof Human) {
			notReceivingShadowRoot.attachChild(housePart.getRoot());
		} else {
			originalHouseRoot.attachChild(housePart.getRoot());
		}
		parts.add(housePart);
		for (final HousePart child : housePart.getChildren())
			add(child);
	}

	public void remove(final HousePart housePart, final boolean redraw) {
		if (housePart == null)
			return;
		housePart.setGridsVisible(false);
		SceneManager.getTaskManager().update(new Callable<Object>() {
			public Object call() {
				final HousePart container = housePart.getContainer();
				if (container != null)
					container.getChildren().remove(housePart);
				removeChildren(housePart);
				return null;
			}
		});
		if (redraw)
			redrawAll();
	}

	private void removeChildren(final HousePart housePart) {
		System.out.println("Removing: " + housePart);
		parts.remove(housePart); // this must happen before call to wall.delete()
		for (final HousePart child : housePart.getChildren())
			removeChildren(child);
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
		if (copyBuffer == null)
			return;
		if (copyBuffer instanceof Foundation) // copying a foundation copies the entire building above it, which requires a different treatment elsewhere
			return;
		EnergyPanel.getInstance().clearRadiationHeatMap();
		final HousePart c = copyBuffer.copy(true);
		if (c == null) // the copy method returns null if something is wrong (like, out of range, overlap, etc.)
			return;
		add(c, true);
		copyBuffer = c;
		SceneManager.getInstance().getUndoManager().addEdit(new PastePartCommand(c));
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	public void pasteToPickedLocationOnLand() {
		EnergyPanel.getInstance().clearRadiationHeatMap();
		if (copyBuffer == null)
			return;
		final HousePart c = copyBuffer.copy(false);
		if (c == null) // the copy method returns null if something is wrong (like, out of range, overlap, etc.)
			return;
		final Vector3 position = SceneManager.getInstance().getPickedLocationOnLand();
		if (position == null)
			return;
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
			copyBuffer = c;
			setIdOfChildren(c);
			SceneManager.getInstance().getUndoManager().addEdit(new PastePartCommand(c));
		}
	}

	public void pasteToPickedLocationOnWall() {
		EnergyPanel.getInstance().clearRadiationHeatMap();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (!(selectedPart instanceof Wall))
			return;
		if (copyBuffer == null)
			return;
		if (copyBuffer instanceof Foundation) // cannot paste a foundation to a wall
			return;
		final HousePart c = copyBuffer.copy(false);
		if (c == null) // the copy method returns null if something is wrong (like, out of range, overlap, etc.)
			return;
		Vector3 position = SceneManager.getInstance().getPickedLocationOnWall();
		if (position == null)
			return;
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
			if (!Util.insidePolygon(new Vector3(v.getX(), v.getZ(), v.getY()), relativePolygon)) // reject it if out of range
				return;
		}
		add(c, true);
		copyBuffer = c;
		SceneManager.getInstance().getUndoManager().addEdit(new PastePartCommand(c));
	}

	public void pasteToPickedLocationOnRoof() {
		EnergyPanel.getInstance().clearRadiationHeatMap();
		if (copyBuffer == null)
			return;
		if (copyBuffer instanceof Foundation) // cannot paste a foundation to a roof
			return;
		final HousePart c = copyBuffer.copy(false);
		if (c == null) // the copy method returns null if something is wrong (like, out of range, overlap, etc.)
			return;
		Vector3 position = SceneManager.getInstance().getPickedLocationOnRoof();
		if (position == null)
			return;
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

	public List<HousePart> getParts() {
		return parts;
	}

	public void drawResizeBounds() {
		for (final HousePart part : parts) {
			if (part instanceof Foundation)
				part.draw();
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
			for (final HousePart part : instance.getParts())
				part.drawAnnotations();
		}
		if (PrintController.getInstance().getPrintParts() != null)
			for (final HousePart part : PrintController.getInstance().getPrintParts())
				part.drawAnnotations();
	}

	public void redrawAll() {
		redrawAll(false);
	}

	public void redrawAll(final boolean cleanup) {
		this.cleanup = cleanup;
		if (PrintController.getInstance().isPrintPreview())
			PrintController.getInstance().restartAnimation();
		else
			redrawAll = true;
	}

	public void redrawAllNow() {
		System.out.println("redrawAllNow()");
		synchronized (SceneManager.getInstance()) {
			if (cleanup) {
				cleanup();
				cleanup = false;
			}
			connectWalls();
			Snap.clearAnnotationDrawn();
			for (final HousePart part : parts)
				if (part instanceof Roof)
					part.draw();
			for (final HousePart part : parts)
				if (!(part instanceof Roof))
					part.draw();
			// need to draw roof again because roof holes depend on drawn windows
			for (final HousePart part : parts)
				if (part instanceof Roof)
					part.draw();
		}
		// no need for redrawing print parts because they will be regenerated from original parts anyways
		redrawAll = false;
	}

	public void updateAllTextures() {
		System.out.println("updateAllTextures()");
		for (final HousePart part : parts)
			part.updateTextureAndColor();
		SceneManager.getInstance().refresh();
	}

	public void setUnit(final Unit unit) {
		this.unit = unit;
		redrawAll = true;
	}

	public Unit getUnit() {
		if (unit == null)
			unit = Unit.InternationalSystemOfUnits;
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

	public void updateRoofDashLinesColor() {
		for (final HousePart part : parts)
			if (part instanceof Roof)
				((Roof) part).updateDashLinesColor();
		if (PrintController.getInstance().getPrintParts() != null)
			for (final HousePart part : PrintController.getInstance().getPrintParts())
				if (part instanceof Roof)
					((Roof) part).updateDashLinesColor();
	}

	public void removeAllTrees() {
		final ArrayList<HousePart> trees = new ArrayList<HousePart>();
		for (final HousePart part : parts)
			if (part instanceof Tree && !part.isFrozen())
				trees.add(part);
		if (trees.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no tree to remove.", "No Tree", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + trees.size() + " trees?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
			return;
		for (final HousePart part : trees)
			remove(part, false);
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(new RemoveMultiplePartsOfSameTypeCommand(trees));
		edited = true;
	}

	public void removeAllRoofs() {
		final ArrayList<HousePart> roofs = new ArrayList<HousePart>();
		for (final HousePart part : parts)
			if (part instanceof Roof && !part.isFrozen())
				roofs.add(part);
		if (roofs.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no roof to remove.", "No Roof", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + roofs.size() + " roofs?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
			return;
		for (final HousePart part : roofs)
			remove(part, false);
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(new RemoveMultiplePartsOfSameTypeCommand(roofs));
		edited = true;
	}

	public void removeAllFloors() {
		final ArrayList<HousePart> floors = new ArrayList<HousePart>();
		for (final HousePart part : parts)
			if (part instanceof Floor && !part.isFrozen())
				floors.add(part);
		if (floors.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no floor to remove.", "No Floor", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + floors.size() + " floors?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
			return;
		for (final HousePart part : floors)
			remove(part, false);
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(new RemoveMultiplePartsOfSameTypeCommand(floors));
		edited = true;
	}

	public void removeAllSolarPanels() {
		final ArrayList<HousePart> panels = new ArrayList<HousePart>();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart != null) {
			final Foundation foundation = selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer();
			for (final HousePart part : parts) {
				if (part instanceof SolarPanel && !part.isFrozen() && part.getTopContainer() == foundation)
					panels.add(part);
			}
		} else {
			for (final HousePart part : parts) {
				if (part instanceof SolarPanel && !part.isFrozen())
					panels.add(part);
			}
		}
		if (panels.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no solar panel to remove.", "No Solar Panel", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + panels.size() + " solar panels" + (selectedPart != null ? " of the selected building" : "") + "?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
			return;
		for (final HousePart part : panels) {
			remove(part, false);
		}
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(new RemoveMultiplePartsOfSameTypeCommand(panels));
		edited = true;
	}

	public void removeAllWindows() {
		final ArrayList<HousePart> windows = new ArrayList<HousePart>();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart != null) {
			final Foundation foundation = selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer();
			for (final HousePart part : parts) {
				if (part instanceof Window && !part.isFrozen() && part.getTopContainer() == foundation)
					windows.add(part);
			}
		} else {
			for (final HousePart part : parts) {
				if (part instanceof Window && !part.isFrozen())
					windows.add(part);
			}
		}
		if (windows.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no window to remove.", "No Window", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + windows.size() + " windows" + (selectedPart != null ? " of the selected building" : "") + "?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
			return;
		for (final HousePart part : windows) {
			remove(part, false);
		}
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(new RemoveMultiplePartsOfSameTypeCommand(windows));
		edited = true;
	}

	public void lockAll(final boolean freeze) {
		if (parts.isEmpty())
			return;
		int lockCount = 0;
		for (final HousePart part : parts) {
			if (part.isFrozen())
				lockCount++;
		}
		if (!freeze) {
			if (lockCount > 0) {
				if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "<html>A lock prevents a component from being edited.<br>Do you really want to remove all the existing " + lockCount + " locks?</html>", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
					return;
			} else {
				JOptionPane.showMessageDialog(MainFrame.getInstance(), "<html>A lock prevents a component from being edited.<br>There is no lock to remove.</html>");
				return;
			}
		}
		SceneManager.getInstance().getUndoManager().addEdit(new LockAllCommand());
		for (final HousePart part : parts)
			part.setFreeze(freeze);
		if (freeze)
			SceneManager.getInstance().hideAllEditPoints();
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
		for (final HousePart part : parts)
			if (part instanceof Roof)
				((Roof) part).removeAllGables();
	}

	/** get the default color for foundations */
	public ReadOnlyColorRGBA getFoundationColor() {
		if (foundationColor == null)
			return WHITE;
		return foundationColor;
	}

	/** set the default color for foundations */
	public void setFoundationColor(final ReadOnlyColorRGBA foundationColor) {
		this.foundationColor = foundationColor;
	}

	/** get the default color for walls */
	public ReadOnlyColorRGBA getWallColor() {
		if (wallColor == null)
			return GRAY;
		return wallColor;
	}

	/** set the default color for walls */
	public void setWallColor(final ReadOnlyColorRGBA wallColor) {
		this.wallColor = wallColor;
	}

	/** get the default color for doors */
	public ReadOnlyColorRGBA getDoorColor() {
		if (doorColor == null)
			return WHITE;
		return doorColor;
	}

	/** set the default color for doors */
	public void setDoorColor(final ReadOnlyColorRGBA doorColor) {
		this.doorColor = doorColor;
	}

	/** get the default color for floors */
	public ReadOnlyColorRGBA getFloorColor() {
		if (floorColor == null)
			return WHITE;
		return floorColor;
	}

	/** set the default color for floors */
	public void setFloorColor(final ReadOnlyColorRGBA floorColor) {
		this.floorColor = floorColor;
	}

	/** get the default color for roofs */
	public ReadOnlyColorRGBA getRoofColor() {
		if (roofColor == null)
			return WHITE;
		return roofColor;
	}

	/** set the default color for roofs */
	public void setRoofColor(final ReadOnlyColorRGBA roofColor) {
		this.roofColor = roofColor;
	}

	public void setWindowColorInContainer(final HousePart container, final ColorRGBA c) {
		for (final HousePart p : parts) {
			if (p instanceof Window && p.getContainer() == container)
				((Window) p).setColor(c);
		}
	}

	public void setPartColorOfBuilding(final HousePart part, final ReadOnlyColorRGBA color) {
		if (part instanceof Foundation) {
			part.setColor(color);
		} else {
			for (final HousePart p : parts) {
				if (p.getTopContainer() == part.getTopContainer() && p.getClass().equals(part.getClass()))
					p.setColor(color);
			}
		}
	}

	public List<HousePart> getHousePartsOfSameTypeInBuilding(final HousePart x) {
		final List<HousePart> list = new ArrayList<HousePart>();
		if (x instanceof Foundation) {
			list.add(x);
		} else {
			for (final HousePart p : parts) {
				if (p.getClass().equals(x.getClass()) && p.getTopContainer() == x.getTopContainer())
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
					if (p.getClass().equals(x.getClass()) && p.getTopContainer() == x.getTopContainer())
						((Thermalizable) p).setUValue(uValue);
				}
			}
		}
	}

	public List<Window> getWindowsOnContainer(final HousePart container) {
		final List<Window> list = new ArrayList<Window>();
		for (final HousePart p : parts) {
			if (p instanceof Window && p.getContainer() == container)
				list.add((Window) p);
		}
		return list;
	}

	public void setWindowShgcInContainer(final HousePart container, final double shgc) {
		for (final HousePart p : parts) {
			if (p instanceof Window && p.getContainer() == container)
				((Window) p).setSolarHeatGainCoefficient(shgc);
		}
	}

	public List<Window> getWindowsOfBuilding(final Foundation foundation) {
		final List<Window> list = new ArrayList<Window>();
		for (final HousePart p : parts) {
			if (p instanceof Window && p.getTopContainer() == foundation)
				list.add((Window) p);
		}
		return list;
	}

	public void setWindowShgcOfBuilding(final Foundation foundation, final double shgc) {
		for (final HousePart p : parts) {
			if (p instanceof Window && p.getTopContainer() == foundation)
				((Window) p).setSolarHeatGainCoefficient(shgc);
		}
	}

	public List<SolarPanel> getSolarPanelsOfBuilding(final Foundation foundation) {
		final List<SolarPanel> list = new ArrayList<SolarPanel>();
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel && p.getTopContainer() == foundation)
				list.add((SolarPanel) p);
		}
		return list;
	}

	public void setSolarPanelEfficiencyOfBuilding(final Foundation foundation, final double eff) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel && p.getTopContainer() == foundation)
				((SolarPanel) p).setEfficiency(eff);
		}
	}

	public boolean isEdited() {
		return edited;
	}

	public void setEdited(final boolean edited) {
		setEdited(edited, true);
	}

	public void setEdited(final boolean edited, final boolean recomputeEnergy) {
		if (edited)
			SnapshotLogger.getInstance().setSceneEdited(true);
		this.edited = edited;
		if (!Config.isApplet())
			MainFrame.getInstance().updateTitleBar();
	}

	public void updateEditShapes() {
		for (final HousePart part : parts)
			part.updateEditShapes();
	}

	public long nextID() {
		return ++idCounter;
	}

	public boolean hasSensor() {
		for (final HousePart housePart : parts)
			if (housePart instanceof Sensor)
				return true;
		return false;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setStudentMode(boolean b) {
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
		if (calendar != null)
			calendar.setTime(date);
	}

	public Date getDate() {
		if (calendar != null)
			return calendar.getTime();
		return Heliodon.getInstance().getCalender().getTime();
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
		for (final HousePart part : Scene.getInstance().getParts())
			part.updateHeatFluxVisibility();
	}

	public boolean getOnlyAbsorptionInSolarMap() {
		return !fullEnergyInSolarMap;
	}

	public void setOnlyAbsorptionInSolarMap(final boolean onlyAbsorptionInSolarMap) {
		fullEnergyInSolarMap = !onlyAbsorptionInSolarMap;
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
			if (p instanceof SolarPanel)
				count++;
		}
		return count;
	}

	// XIE: This needs to be called for trees to change texture when the month changes
	public void setTreeLeaves() {
		for (final HousePart p : Scene.getInstance().getParts())
			if (p instanceof Tree)
				p.updateTextureAndColor();
	}

	public Ground getGround() {
		return ground;
	}

	public DesignSpecs getDesignSpecs() {
		return designSpecs;
	}

}
