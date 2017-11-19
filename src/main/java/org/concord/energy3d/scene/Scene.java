package org.concord.energy3d.scene;

import java.awt.EventQueue;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.concord.energy3d.Designer;
import org.concord.energy3d.MainApplication;
import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.logger.SnapshotLogger;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.GeoLocation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Labelable;
import org.concord.energy3d.model.MeshLocator;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.NodeState;
import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.model.ParabolicTrough;
import org.concord.energy3d.model.Rack;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.Snap;
import org.concord.energy3d.model.SolarCollector;
import org.concord.energy3d.model.SolarPanel;
import org.concord.energy3d.model.Thermal;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.UserData;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.SceneManager.ViewMode;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.Atmosphere;
import org.concord.energy3d.simulation.CspCustomPrice;
import org.concord.energy3d.simulation.CspDesignSpecs;
import org.concord.energy3d.simulation.DesignSpecs;
import org.concord.energy3d.simulation.Ground;
import org.concord.energy3d.simulation.LocationData;
import org.concord.energy3d.simulation.PvCustomPrice;
import org.concord.energy3d.simulation.PvDesignSpecs;
import org.concord.energy3d.simulation.PvModuleSpecs;
import org.concord.energy3d.simulation.PvModulesData;
import org.concord.energy3d.simulation.UtilityBill;
import org.concord.energy3d.undo.AddMultiplePartsCommand;
import org.concord.energy3d.undo.AddNodeCommand;
import org.concord.energy3d.undo.LockAllCommand;
import org.concord.energy3d.undo.PastePartCommand;
import org.concord.energy3d.undo.RemoveMultiplePartsCommand;
import org.concord.energy3d.undo.RemoveMultipleShuttersCommand;
import org.concord.energy3d.undo.SaveCommand;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.Pair;
import org.concord.energy3d.util.Util;
import org.concord.energy3d.util.WallVisitor;

import com.ardor3d.extension.model.obj.ObjExporter;
import com.ardor3d.image.Texture.MinificationFilter;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.util.awt.AWTImageLoader;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Quaternion;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.TextureKey;

public class Scene implements Serializable {

	public static final int BLUE_SKY_THEME = 0;
	public static final int DESERT_THEME = 1;
	public static final int GRASSLAND_THEME = 2;
	public static final int FOREST_THEME = 3;

	private static final long serialVersionUID = 1L;
	private static final Node root = new Node("Model Root");
	private static final Node originalHouseRoot = new Node("Original Model Root");
	private static final Node notReceivingShadowRoot = new Node("No-Shadow Root");
	private static final int currentVersion = 1;
	private static boolean first = true;
	private static Scene instance;
	private static URL url;
	private static boolean redrawAll;
	private static boolean drawThickness;
	private static boolean drawAnnotationsInside;
	private static boolean isSaving;
	private transient boolean edited;
	private transient BufferedImage groundImage;
	private transient boolean avoidSavingGroundImage;
	private final List<HousePart> parts = new ArrayList<HousePart>();
	private final Calendar calendar = Calendar.getInstance();
	private TextureMode textureMode = TextureMode.None;
	private ReadOnlyVector3 cameraLocation;
	private ReadOnlyVector3 cameraDirection;
	private ReadOnlyColorRGBA landColor = new ColorRGBA(0, 1.0f, 0.75f, 0.5f);
	private ReadOnlyColorRGBA foundationColor;
	private ReadOnlyColorRGBA wallColor;
	private ReadOnlyColorRGBA doorColor;
	private ReadOnlyColorRGBA floorColor;
	private ReadOnlyColorRGBA roofColor;
	private Unit unit = Unit.InternationalSystemOfUnits;
	private Ground ground = new Ground();
	private Atmosphere atmosphere = new Atmosphere();
	private PvCustomPrice pvCustomPrice = new PvCustomPrice();
	private CspCustomPrice cspCustomPrice = new CspCustomPrice();
	private DesignSpecs designSpecs = new DesignSpecs();
	private PvDesignSpecs pvDesignSpecs = new PvDesignSpecs();
	private CspDesignSpecs cspDesignSpecs = new CspDesignSpecs();
	private transient HousePart copyBuffer, originalCopy;
	private transient Node copyNode;
	private transient NodeState copyNodeState;
	private UtilityBill utilityBill;
	private String projectName;
	private int projectType = Foundation.TYPE_BUILDING;
	private Designer designer;
	private String city;
	private String note;
	private long idCounter;
	private int version = currentVersion;
	private int latitude; // Legacy: Do NOT use this in the calculation -- use geoLocation.getLatitude() instead
	private int solarContrast;
	private int theme;
	private double annotationScale = 0.2;
	private double heatVectorLength = 2000;
	private double heatFluxGridSize = 2;
	private boolean isAnnotationsVisible;
	private boolean studentMode;
	private boolean isHeliodonVisible;
	private boolean hideAxes;
	private boolean hideLightBeams;
	private boolean showSunAngles;
	private boolean noSnapToGrids;
	private boolean cleanup;
	private boolean alwaysComputeHeatFluxVectors;
	private boolean disableShadowInAction;
	private boolean fullEnergyInSolarMap = true;
	private boolean onlyReflectedEnergyInMirrorSolarMap;
	private boolean onlySolarComponentsInSolarMap;
	private boolean solarMapForLand;
	private boolean disallowFoundationOverlap;
	private boolean dashedlineOnRoofs = true;
	private boolean onlySolarAnalysis;
	private double groundImageScale = 1;
	private boolean hideGroundImage;
	private boolean groundImageIsEarthView;
	private boolean groundImageLightColored; // a parameter that user can choose to improve the contrast of edit points etc. (i.e., no white on white)
	private transient List<List<Foundation>> foundationGroups;
	private String[] instructionSheetText;
	private String[] instructionSheetTextType;

	/* the following parameters specify the resolution of discretization for a simulation */

	// increment of time in minutes
	private int timeStep = 15;

	// number of points in x and y directions when a solar panel is discretized into a grid (to meet the need of texture, these numbers must be power of 2)
	// used in heat map visualization for solar panels (for radiation calculation, solar panels use the underlying solar cell layout, e.g., 6x10, as the discretization)
	private int solarPanelNx = 4, solarPanelNy = 4;

	// number of points in x and y directions when a solar rack is discretized into a grid (to meet the need of texture, these numbers must be power of 2)
	// used in heat map visualization for solar panel racks (for radiation calculation, specify the size of the unit cell as follows)
	private int rackNx = 8, rackNy = 8;

	private double rackCellSize = 1; // by default, the cell is 1 x 1 meter

	private boolean rackModelExact = false; // by default, use the approximate model for rack for speed

	// number of points in x and y directions when a heliostat mirror is discretized into a grid (to meet the need of texture, these numbers must be power of 2)
	// used in both radiation calculation and heat map visualization for reflecting mirrors (which are closer to square, except parabolic troughs)
	private int mirrorNx = 4, mirrorNy = 4;

	private int parabolicDishN = 4;

	// the step length of the discretized grid on any part that is not a plate
	private double solarStep = 2.0;

	private GeoLocation geoLocation;

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
			first = false;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				instance.add(new Human(Human.JACK, 1));
				final Foundation f = new Foundation(xLength, yLength);
				f.setColor(ColorRGBA.GRAY);
				instance.add(f, true);
				return null;
			}
		});
	}

	public static void open(final URL file) throws Exception {
		openNow(file);
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				final EnergyPanel e = EnergyPanel.getInstance();
				e.update();
				e.clearAllGraphs();
				e.selectInstructionSheet(0);
				if (MainFrame.getInstance().getTopViewCheckBoxMenuItem().isSelected()) { // make sure we exist the 2D top view
					MainFrame.getInstance().getTopViewCheckBoxMenuItem().setSelected(false);
					SceneManager.getInstance().resetCamera(ViewMode.NORMAL);
					loadCameraLocation();
				}
				final HousePart p = SceneManager.getInstance().getSelectedPart();
				if (p instanceof Foundation) {
					final Foundation f = (Foundation) p;
					switch (f.getProjectType()) {
					case Foundation.TYPE_BUILDING:
						e.getBuildingCostGraph().addGraph(f);
						e.getBuildingDailyEnergyGraph().clearData();
						e.getBuildingDailyEnergyGraph().addGraph(f);
						e.validate();
						break;
					case Foundation.TYPE_PV_PROJECT:
						e.getPvProjectCostGraph().addGraph(f);
						e.getPvProjectDailyEnergyGraph().clearData();
						e.getPvProjectDailyEnergyGraph().addGraph(f);
						e.validate();
						break;
					case Foundation.TYPE_CSP_PROJECT:
						e.getCspProjectCostGraph().addGraph(f);
						e.getCspProjectDailyEnergyGraph().clearData();
						e.getCspProjectDailyEnergyGraph().addGraph(f);
						e.validate();
						break;
					}
				}
				MainFrame.getInstance().toFront();
			}
		});
	}

	public static void openNow(final URL file) throws Exception {
		SceneManager.getInstance().clearMouseState();
		SceneManager.getInstance().cursorWait(true);
		if (PrintController.getInstance().isPrintPreview()) {
			MainPanel.getInstance().getPreviewButton().setSelected(false);
			while (!PrintController.getInstance().isFinished()) {
				Thread.yield();
			}
		}

		Scene.url = file;

		if (!first) {
			SceneManager.getInstance().setHeliodonVisible(false);
			SceneManager.getInstance().setSunAnimation(false);
			Util.selectSilently(MainPanel.getInstance().getHeliodonButton(), false);
			Util.selectSilently(MainPanel.getInstance().getSunAnimationButton(), false);
		}
		SceneManager.getInstance().setSolarHeatMapWithoutUpdate(false);
		Wall.resetDefaultWallHeight();

		if (instance != null) {
			instance.deleteAll();
		}

		if (url == null) {
			instance = new Scene();
			System.out.println("done");
		} else {
			System.out.println("Opening..." + file + "...");
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
		if (Util.isZero(instance.groundImageScale)) {
			instance.groundImageScale = 1;
		}
		SceneManager.getInstance().hideAllEditPoints();
		final CameraControl cameraControl = SceneManager.getInstance().getCameraControl();
		if (cameraControl != null) {
			cameraControl.reset();
		}

		instance.init();
		instance.applyGroundImage();

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
				SceneManager.getInstance().cursorWait(false);
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
		}

		root.updateWorldBound(true);
		SceneManager.getInstance().updateHeliodonAndAnnotationSize();
		SceneManager.getInstance().setAxesVisible(!hideAxes);
		SceneManager.getInstance().getSolarLand().setVisible(solarMapForLand);

		setTheme(theme);
		SceneManager.getInstance().getLand().setDefaultColor(landColor != null ? landColor : new ColorRGBA(0, 1, 0, 0.5f));
		PvModulesData.getInstance();

		final EnergyPanel energyPanel = EnergyPanel.getInstance();
		if (calendar != null) {
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			if (url == null && Heliodon.getInstance().isNightTime()) {
				calendar.set(Calendar.HOUR_OF_DAY, 12);
				calendar.set(Calendar.MINUTE, 0);
			}
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
			final LocationData ld = LocationData.getInstance();
			if (ld.getLatitudes().get(city) != null) {
				energyPanel.getCityComboBox().setToolTipText("<html>(" + ld.getLatitudes().get(city) + "&deg;, " + ld.getLongitudes().get(city) + "&deg;), elevation " + ld.getAltitudes().get(city).intValue() + "m<br>Use Edit>Set Region... to select country and region.</html>");
			} else {
				JOptionPane.showMessageDialog(MainFrame.getInstance(), city + " not supported. Please upgrade your Energy3D to the latest.", "Missing City", JOptionPane.ERROR_MESSAGE);
			}
			Scene.getInstance().updateTreeLeaves();
			SceneManager.getInstance().changeSkyTexture();
			if (!first) {
				SceneManager.getInstance().setHeliodonVisible(isHeliodonVisible);
				Util.selectSilently(MainPanel.getInstance().getHeliodonButton(), isHeliodonVisible);
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						MainPanel.getInstance().getSunAnimationButton().setEnabled(isHeliodonVisible);
					}
				});
			}
			Heliodon.getInstance().drawSun();
			SceneManager.getInstance().setShading(Heliodon.getInstance().isNightTime());
		}

		// previous versions do not have the following classes
		if (designSpecs == null) {
			designSpecs = new DesignSpecs();
		} else {
			designSpecs.setDefaultValues();
		}
		if (pvDesignSpecs == null) {
			pvDesignSpecs = new PvDesignSpecs();
		} else {
			pvDesignSpecs.setDefaultValues();
		}
		if (cspDesignSpecs == null) {
			cspDesignSpecs = new CspDesignSpecs();
		} else {
			cspDesignSpecs.setDefaultValues();
		}
		if (pvCustomPrice == null) {
			pvCustomPrice = new PvCustomPrice();
		} else {
			pvCustomPrice.setDefaultValues();
		}
		if (cspCustomPrice == null) {
			cspCustomPrice = new CspCustomPrice();
		} else {
			cspCustomPrice.setDefaultValues();
		}
		if (ground == null) {
			ground = new Ground();
		}
		if (atmosphere == null) {
			atmosphere = new Atmosphere();
		}
		if (unit == null) {
			unit = Unit.InternationalSystemOfUnits;
		}

		// restore the default values
		if (Util.isZero(heatVectorLength)) {
			heatVectorLength = 5000;
		}
		if (Util.isZero(heatFluxGridSize)) {
			heatFluxGridSize = 2;
		}
		if (Util.isZero(solarStep)) {
			solarStep = 2;
		}
		if (Util.isZero(timeStep)) {
			timeStep = 15;
		}
		if (Util.isZero(solarPanelNx)) {
			solarPanelNx = 4;
		}
		if (Util.isZero(solarPanelNy)) {
			solarPanelNy = 4;
		}
		if (Util.isZero(rackNx)) {
			rackNx = 8;
		}
		if (Util.isZero(rackNy)) {
			rackNy = 8;
		}
		if (Util.isZero(mirrorNx)) {
			mirrorNx = 4;
		}
		if (Util.isZero(mirrorNy)) {
			mirrorNy = 4;
		}
		if (Util.isZero(parabolicDishN)) {
			parabolicDishN = 4;
		}
		if (Util.isZero(rackCellSize)) {
			rackCellSize = 1;
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
				SceneManager.getInstance().getUndoManager().discardAllEdits();
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

	public void importFile(final URL url) throws Exception {
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
				redrawAll = true;
				SceneManager.getInstance().getUndoManager().addEdit(cmd);
			}

			root.updateWorldBound(true);
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					MainPanel.getInstance().getEnergyViewButton().setSelected(false);
				}
			});
			setEdited(true);
		} else {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "URL doesn't exist.", "Error", JOptionPane.ERROR_MESSAGE);
		}

	}

	public void importCollada(final File file) throws Exception {
		boolean success = true;
		Vector3 position = SceneManager.getInstance().getPickedLocationOnLand();
		if (position == null) {
			position = new Vector3();
		}
		final Foundation foundation = new Foundation(100, 100);
		for (final Vector3 p : foundation.getPoints()) {
			p.addLocal(position);
		}
		Scene.getInstance().add(foundation, false);
		try {
			final Node n = foundation.importCollada(file.toURI().toURL(), position);
			if (n != null) {
				// TODO: automatically center the model at the center
			}
		} catch (final Throwable t) {
			Util.reportError(t);
			success = false;
		}
		if (success) {
			SceneManager.getInstance().getUndoManager().addEdit(new AddNodeCommand(foundation));
		}
		setEdited(true);
	}

	public void exportObj(final File file) throws Exception {
		try {
			final List<Mesh> objs = new ArrayList<Mesh>();
			for (final HousePart part : parts) {
				part.addPrintMeshes(objs);
			}
			final Quaternion rotate = new Quaternion(-1, 0, 0, 1);
			for (final Mesh mesh : objs) {
				mesh.getMeshData().rotatePoints(rotate);
				if (mesh.getMeshData().getNormalBuffer() != null) {
					mesh.getMeshData().rotateNormals(rotate);
				}
				mesh.updateModelBound();
			}
			final String s = file.toString();
			new ObjExporter().save(objs, new File(s), new File(s.substring(0, s.lastIndexOf(".")) + ".mtl"), null);
		} catch (final Throwable t) {
			Util.reportError(t);
		}
	}

	/** This can be used by the user to fix problems that are caused by bugs based on our observations. This is different than cleanup() as the latter cannot be used to remove undrawables. */
	public void fixProblems(final boolean redraw) {

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
		if (redraw) {
			redrawAll(true);
		}

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
			if (!p.isValid()) { // remove invalid parts
				toBeRemoved.add(p);
			} else if (p.getContainer() == null) { // remove orphan parts without a container
				if (p instanceof Wall || p instanceof Roof || p instanceof Window || p instanceof Door || p instanceof SolarCollector || p instanceof Floor) {
					toBeRemoved.add(p);
				}
			} else if (!parts.contains(p.getContainer())) { // remove parts whose container doesn't exist in the scene
				toBeRemoved.add(p);
			}
		}
		for (final HousePart p : toBeRemoved) {
			remove(p, false);
		}

		// remove walls that are at the same position
		toBeRemoved.clear();
		for (final HousePart p : parts) {
			if (p instanceof Wall) { // remove walls that are at the same position
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

	public static void save(final URL url, final boolean setAsCurrentFile) {
		save(url, setAsCurrentFile, true, false);
	}

	public static void save(final URL url, final boolean setAsCurrentFile, final boolean notifyUndoManager, final boolean logger) {
		isSaving = true;
		if (MainApplication.isSceneManagerThreadAlive()) {
			SceneManager.getTaskManager().update(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					try {
						realSave(url, setAsCurrentFile, notifyUndoManager, logger);
					} catch (final Throwable e) {
						Util.reportError(e);
					} finally {
						isSaving = false;
					}
					return null;
				}
			});
		} else {
			try {
				realSave(url, setAsCurrentFile, notifyUndoManager, logger);
			} catch (final Throwable e) {
				Util.reportError(e);
			} finally {
				isSaving = false;
			}
		}
	}

	// when saving before exit or saving in the log, don't use a callback through the task manager -- use whatever is the current thread to do the job
	public static void saveOutsideTaskManager(final URL url) {
		isSaving = true;
		try {
			realSave(url, false, false, false);
		} catch (final Throwable e) {
			e.printStackTrace();
		} finally {
			isSaving = false;
		}
	}

	private static void realSave(final URL url, final boolean setAsCurrentFile, final boolean notifyUndoManager, final boolean logger) throws Exception {
		if (logger) {
			instance.storeGroundImageData();
		}
		if (notifyUndoManager) {
			instance.cleanup();
		}
		// save camera to file
		if (SceneManager.getInstance().getViewMode() == ViewMode.NORMAL) {
			saveCameraLocation();
		}

		instance.hideAxes = !SceneManager.getInstance().areAxesVisible();
		// instance.showFloatingLabels = SceneManager.getInstance().areFloatingLabelsVisible();
		instance.calendar.setTime(Heliodon.getInstance().getCalendar().getTime());
		instance.latitude = (int) Math.toDegrees(Heliodon.getInstance().getLatitude());
		instance.city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
		instance.isHeliodonVisible = Heliodon.getInstance().isVisible();
		instance.note = MainPanel.getInstance().getNoteTextArea().getText().trim();
		instance.solarContrast = EnergyPanel.getInstance().getColorMapSlider().getValue();

		if (setAsCurrentFile || (!logger && Scene.url == null)) {
			Scene.url = url;
		}
		System.out.print("Saving " + url + "...");
		final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(url.toURI().getPath()));
		out.writeObject(instance);
		out.close();

		if (notifyUndoManager) {
			SceneManager.getInstance().getUndoManager().addEdit(new SaveCommand());
		}
		if (logger) {
			instance.restoreGroundImageData();
		}

		if (!logger) {
			try {
				System.out.print("Opening..." + url + "...");
				final ObjectInputStream in = new ObjectInputStream(url.openStream());
				in.readObject();
				in.close();
			} catch (final Throwable e) {
				instance.setEdited(true);
				Util.reportError(e, "Save Verification Error: " + url + " : ");
			}
		}
		System.out.println("done");
	}

	public static void saveCameraLocation() {
		final Camera camera = SceneManager.getInstance().getCamera();
		if (camera != null) {
			instance.setCameraLocation(camera.getLocation().clone());
			instance.setCameraDirection(SceneManager.getInstance().getCamera().getDirection().clone());
		}
	}

	public static Node getRoot() {
		return root;
	}

	private Scene() {
	}

	public void add(final HousePart part, final boolean redraw) {
		final HousePart container = part.getContainer();
		if (container != null) {
			container.getChildren().add(part);
		}
		add(part);
		if (redraw) {
			redrawAll();
		}
	}

	private void add(final HousePart part) {
		System.out.println("Adding: " + part);
		if (part instanceof Tree || part instanceof Human) {
			notReceivingShadowRoot.attachChild(part.getRoot());
		} else {
			originalHouseRoot.attachChild(part.getRoot());
		}
		parts.add(part);
		for (final HousePart child : part.getChildren()) {
			add(child);
		}
	}

	public void remove(final HousePart part, final boolean redraw) {
		if (part == null) {
			return;
		}
		part.setGridsVisible(false);
		final HousePart container = part.getContainer();
		if (container != null) {
			container.getChildren().remove(part);
		}
		removeChildren(part);
		if (redraw) {
			redrawAll();
		}
	}

	private void removeChildren(final HousePart part) {
		System.out.println("Removing: " + part);
		parts.remove(part); // this must happen before call to wall.delete()
		for (final HousePart child : part.getChildren()) {
			removeChildren(child);
		}
		// originalHouseRoot.detachChild(housePart.getRoot());
		part.getRoot().removeFromParent();
		part.delete();
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

	public void setCopyNode(final Node n, final NodeState ns) {
		if (n != null) {
			copyNode = n.makeCopy(false);
			copyNodeState = ns.clone();
		} else {
			copyNode = null;
			copyNodeState = null;
		}
		copyBuffer = null;
	}

	public Node getCopyNode() {
		return copyNode;
	}

	public NodeState getCopyNodeState() {
		return copyNodeState;
	}

	public void setCopyBuffer(final HousePart p) {
		EnergyPanel.getInstance().updateRadiationHeatMap();
		if (p instanceof Roof || p instanceof Floor || p instanceof Sensor) { // exclude these types
			return;
		}
		copyBuffer = p;
		originalCopy = p;
		copyNode = null;
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
		EnergyPanel.getInstance().updateRadiationHeatMap();
		final HousePart c = copyBuffer.copy(true);
		if (c == null) {
			return;
		}
		add(c, true);
		copyBuffer = c;
		SceneManager.getInstance().getUndoManager().addEdit(new PastePartCommand(c));
		EnergyPanel.getInstance().updateRadiationHeatMap();
		EnergyPanel.getInstance().update();
	}

	public void pasteToPickedLocationOnLand() {
		EnergyPanel.getInstance().updateRadiationHeatMap();
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
					if (oldMap == null || oldMap.isEmpty()) {
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
		EnergyPanel.getInstance().updateRadiationHeatMap();
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
		if (wall != c.getContainer()) { // windows and solar panels can be pasted to a different wall
			if (c instanceof Window) {
				((Window) c).moveTo(wall);
			} else if (c instanceof SolarPanel) {
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
		EnergyPanel.getInstance().updateRadiationHeatMap();
		if (copyBuffer == null) {
			return;
		}
		if (copyBuffer instanceof Foundation) {
			return;
		}
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (!(selectedPart instanceof Roof)) {
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
		if (selectedPart != c.getContainer()) { // solar panels and racks can be pasted to a different parent
			if (c instanceof SolarPanel) {
				((SolarPanel) c).moveTo(selectedPart);
			} else if (c instanceof Rack) {
				((Rack) c).moveTo(selectedPart);
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
		if (c instanceof Rack) {
			((Rack) c).moveSolarPanels(position);
			setIdOfChildren(c);
		}
		add(c, true);
		copyBuffer = c;
		SceneManager.getInstance().getUndoManager().addEdit(new PastePartCommand(c));
	}

	public void pasteToPickedLocationOnRack() {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (!(selectedPart instanceof Rack)) {
			return;
		}
		if (!(copyBuffer instanceof SolarPanel)) {
			return;
		}
		EnergyPanel.getInstance().updateRadiationHeatMap();
		final HousePart c = copyBuffer.copy(false);
		if (c == null) {
			return;
		}
		Vector3 position = SceneManager.getInstance().getPickedLocationOnRack();
		if (position == null) {
			return;
		}
		if (selectedPart != c.getContainer()) {
			((SolarPanel) c).moveTo(selectedPart);
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
		EnergyPanel.getInstance().updateRadiationHeatMap();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (!(selectedPart instanceof Foundation)) {
			return;
		}
		final Foundation foundation = (Foundation) selectedPart;
		if (copyNode != null) {
			final Vector3 position = SceneManager.getInstance().getPickedLocationOnFoundation();
			if (position == null) {
				return;
			}
			copyNodeState.setAbsolutePosition(position.clone());
			Node newNode = null;
			try {
				newNode = foundation.importCollada(copyNodeState.getSourceURL(), position);
			} catch (final Throwable t) {
				t.printStackTrace();
			}
			if (newNode != null) { // copy the attributes that aren't copied by import
				final NodeState s = foundation.getNodeState(newNode);
				s.setDefaultColor(copyNodeState.getDefaultColor());
				s.setName(copyNodeState.getName());
				if (copyNodeState.getMeshesWithReversedNormal() != null) {
					for (final Integer i : copyNodeState.getMeshesWithReversedNormal()) {
						s.reverseNormalOfMesh(i);
						Util.reverseFace(Util.getMesh(newNode, i));
					}
				}
				if (copyNodeState.getDeletedMeshes() != null) {
					for (final Integer i : copyNodeState.getDeletedMeshes()) {
						foundation.deleteMesh(Util.getMesh(newNode, i));
					}
				}
				final HashMap<Integer, ReadOnlyColorRGBA> meshColors = copyNodeState.getMeshColors();
				if (meshColors != null) {
					for (final Integer i : meshColors.keySet()) {
						s.setMeshColor(i, meshColors.get(i));
						Util.getMesh(newNode, i).setDefaultColor(s.getMeshColor(i));
					}
				}
			}
		} else {
			if (copyBuffer != null) {
				final HousePart c = copyBuffer.copy(false);
				if (c == null) {
					return;
				}
				Vector3 position = SceneManager.getInstance().getPickedLocationOnFoundation();
				if (position == null) {
					return;
				}
				c.setContainer(foundation); // move to this foundation
				position = c.toRelative(position.subtractLocal(c.getContainer().getAbsPoint(0)));
				final Vector3 center = c.toRelative(c.getAbsCenter().subtractLocal(c.getContainer().getAbsPoint(0)));
				position = position.subtractLocal(center);
				final int n = c.getPoints().size();
				for (int i = 0; i < n; i++) {
					final Vector3 v = c.getPoints().get(i);
					v.addLocal(position);
				}
				if (c instanceof Rack) {
					((Rack) c).moveSolarPanels(position);
					setIdOfChildren(c);
				}
				add(c, true);
				copyBuffer = c;
				SceneManager.getInstance().setSelectedPart(c);
				SceneManager.getInstance().getUndoManager().addEdit(new PastePartCommand(c));
			}
		}
	}

	public void pasteToPickedLocationOnMesh(final Mesh mesh) {
		EnergyPanel.getInstance().updateRadiationHeatMap();
		if (copyBuffer == null) {
			return;
		}
		if (copyBuffer instanceof Foundation) {
			return;
		}
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (!(selectedPart instanceof Foundation)) {
			return;
		}
		final HousePart c = copyBuffer.copy(false);
		if (c == null) {
			return;
		}
		Vector3 position = SceneManager.getInstance().getPickedLocationOnMesh(mesh);
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
		if (copyBuffer instanceof Rack) {
			final Rack rack = (Rack) c;
			rack.moveSolarPanels(position);
			setIdOfChildren(c);
			final MeshLocator originalMeshLocator = ((Rack) copyBuffer).getMeshLocator();
			if (originalMeshLocator != null) {
				final UserData ud = (UserData) mesh.getUserData();
				rack.setMeshLocator(new MeshLocator((Foundation) ud.getHousePart(), ud.getNodeIndex(), ud.getMeshIndex()));
			}
		} else if (copyBuffer instanceof SolarPanel) {
			final SolarPanel panel = (SolarPanel) c;
			final MeshLocator originalMeshLocator = ((SolarPanel) copyBuffer).getMeshLocator();
			if (originalMeshLocator != null) {
				final UserData ud = (UserData) mesh.getUserData();
				panel.setMeshLocator(new MeshLocator((Foundation) ud.getHousePart(), ud.getNodeIndex(), ud.getMeshIndex()));
			}
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

	public static boolean isInternalFile() {
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
		for (final HousePart part : instance.getParts()) {
			part.drawAnnotations();
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
			}
		}

		if (Heliodon.getInstance().isVisible()) {
			Heliodon.getInstance().updateSize();
		}

		System.out.println("Time = " + (System.nanoTime() - t) / 1000000000.0);
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

	public void removeAllSolarPanels(List<SolarPanel> panels) {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (panels == null) {
			panels = new ArrayList<SolarPanel>();
			if (selectedPart != null) {
				if (selectedPart instanceof Rack) {
					for (final HousePart part : selectedPart.getChildren()) {
						if (part instanceof SolarPanel) {
							panels.add((SolarPanel) part);
						}
					}
				} else {
					final Foundation foundation = selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer();
					for (final HousePart part : parts) {
						if (part instanceof SolarPanel && part.getTopContainer() == foundation) {
							panels.add((SolarPanel) part);
						}
					}
				}
			} else {
				for (final HousePart part : parts) {
					if (part instanceof SolarPanel) {
						panels.add((SolarPanel) part);
					}
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
		final RemoveMultiplePartsCommand c = new RemoveMultiplePartsCommand(new ArrayList<HousePart>(panels));
		for (final HousePart part : panels) {
			remove(part, false);
		}
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(c);
		edited = true;
	}

	public void removeAllRacks() {
		final ArrayList<HousePart> racks = new ArrayList<HousePart>();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart != null) {
			final Foundation foundation = selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer();
			for (final HousePart part : parts) {
				if (part instanceof Rack && part.getTopContainer() == foundation) {
					racks.add(part);
				}
			}
		} else {
			for (final HousePart part : parts) {
				if (part instanceof Rack) {
					racks.add(part);
				}
			}
		}
		if (racks.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no rack to remove.", "No Rack", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + racks.size() + " solar panel racks" + (selectedPart != null ? " on the selected foundation" : "") + "?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
			return;
		}
		final RemoveMultiplePartsCommand c = new RemoveMultiplePartsCommand(racks);
		for (final HousePart part : racks) {
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
				if (part instanceof Mirror && part.getTopContainer() == foundation) {
					mirrors.add(part);
				}
			}
		} else {
			for (final HousePart part : parts) {
				if (part instanceof Mirror) {
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

	public void removeAllParabolicTroughs() {
		final ArrayList<HousePart> troughs = new ArrayList<HousePart>();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart != null) {
			final Foundation foundation = selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer();
			for (final HousePart part : parts) {
				if (part instanceof ParabolicTrough && part.getTopContainer() == foundation) {
					troughs.add(part);
				}
			}
		} else {
			for (final HousePart part : parts) {
				if (part instanceof ParabolicTrough) {
					troughs.add(part);
				}
			}
		}
		if (troughs.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no parabolic trough to remove.", "No Mirror", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + troughs.size() + " parabolic troughs" + (selectedPart != null ? " on the selected foundation" : "") + "?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
			return;
		}
		final RemoveMultiplePartsCommand c = new RemoveMultiplePartsCommand(troughs);
		for (final HousePart part : troughs) {
			remove(part, false);
		}
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(c);
		edited = true;
	}

	public void removeAllParabolicDishes() {
		final ArrayList<HousePart> dishes = new ArrayList<HousePart>();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart != null) {
			final Foundation foundation = selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer();
			for (final HousePart part : parts) {
				if (part instanceof ParabolicDish && part.getTopContainer() == foundation) {
					dishes.add(part);
				}
			}
		} else {
			for (final HousePart part : parts) {
				if (part instanceof ParabolicDish) {
					dishes.add(part);
				}
			}
		}
		if (dishes.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no parabolic dish to remove.", "No Mirror", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + dishes.size() + " parabolic dishes" + (selectedPart != null ? " on the selected foundation" : "") + "?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
			return;
		}
		final RemoveMultiplePartsCommand c = new RemoveMultiplePartsCommand(dishes);
		for (final HousePart part : dishes) {
			remove(part, false);
		}
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(c);
		edited = true;
	}

	public void removeAllFresnelReflectors() {
		final ArrayList<HousePart> reflectors = new ArrayList<HousePart>();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart != null) {
			final Foundation foundation = selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer();
			for (final HousePart part : parts) {
				if (part instanceof FresnelReflector && part.getTopContainer() == foundation) {
					reflectors.add(part);
				}
			}
		} else {
			for (final HousePart part : parts) {
				if (part instanceof FresnelReflector) {
					reflectors.add(part);
				}
			}
		}
		if (reflectors.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no Fresnel reflector to remove.", "No Mirror", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + reflectors.size() + " Fresnel reflectors" + (selectedPart != null ? " on the selected foundation" : "") + "?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
			return;
		}
		final RemoveMultiplePartsCommand c = new RemoveMultiplePartsCommand(reflectors);
		for (final HousePart part : reflectors) {
			remove(part, false);
		}
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(c);
		edited = true;
	}

	public void removeAllSensors() {
		final ArrayList<HousePart> sensors = new ArrayList<HousePart>();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart != null) {
			final Foundation foundation = selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer();
			for (final HousePart part : parts) {
				if (part instanceof Sensor && part.getTopContainer() == foundation) {
					sensors.add(part);
				}
			}
		} else {
			for (final HousePart part : parts) {
				if (part instanceof Sensor) {
					sensors.add(part);
				}
			}
		}
		if (sensors.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no sensor to remove.", "No Sensor", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + sensors.size() + " sensors" + (selectedPart != null ? " on the selected foundation" : "") + "?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
			return;
		}
		final RemoveMultiplePartsCommand c = new RemoveMultiplePartsCommand(sensors);
		for (final HousePart part : sensors) {
			remove(part, false);
		}
		redrawAll();
		SceneManager.getInstance().getUndoManager().addEdit(c);
		edited = true;
	}

	public void removeAllWalls() {
		final ArrayList<HousePart> walls = new ArrayList<HousePart>();
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart != null) {
			final Foundation foundation = selectedPart instanceof Foundation ? (Foundation) selectedPart : selectedPart.getTopContainer();
			for (final HousePart part : parts) {
				if (part instanceof Wall && !part.isFrozen() && part.getTopContainer() == foundation) {
					walls.add(part);
				}
			}
		} else {
			for (final HousePart part : parts) {
				if (part instanceof Wall && !part.isFrozen()) {
					walls.add(part);
				}
			}
		}
		if (walls.isEmpty()) {
			JOptionPane.showMessageDialog(MainFrame.getInstance(), "There is no wall to remove.", "No Wall", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (JOptionPane.showConfirmDialog(MainFrame.getInstance(), "Do you really want to remove all " + walls.size() + " walls" + (selectedPart != null ? " of the selected building" : "") + "?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
			return;
		}
		final RemoveMultiplePartsCommand c = new RemoveMultiplePartsCommand(walls);
		for (final HousePart part : walls) {
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
		parent.draw();
		SceneManager.getInstance().getUndoManager().addEdit(c);
		edited = true;
		SceneManager.getInstance().refresh();
	}

	public void deleteAllConnectedWalls(final Wall w) {
		final List<HousePart> copy = new ArrayList<HousePart>();
		w.visitNeighbors(new WallVisitor() {
			@Override
			public void visit(final Wall currentWall, final Snap prev, final Snap next) {
				copy.add(currentWall);
			}
		});
		final RemoveMultiplePartsCommand c = new RemoveMultiplePartsCommand(copy);
		for (final HousePart p : copy) {
			remove(p, false);
		}
		SceneManager.getInstance().getUndoManager().addEdit(c);
		edited = true;
		SceneManager.getInstance().refresh();
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
			return ColorRGBA.WHITE;
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
			return ColorRGBA.LIGHT_GRAY;
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
			return ColorRGBA.WHITE;
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
			return ColorRGBA.WHITE;
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
			return ColorRGBA.WHITE;
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

	public void setWindowSizeInContainer(final HousePart container, final double width, final double height) {
		for (final HousePart p : parts) {
			if (p instanceof Window && p.getContainer() == container) {
				final Window w = (Window) p;
				w.setWindowWidth(width);
				w.setWindowHeight(height);
				w.draw();
				w.getContainer().draw();
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
			part.draw();
		} else {
			for (final HousePart p : parts) {
				if (p.getTopContainer() == part.getTopContainer() && p.getClass().equals(part.getClass())) {
					p.setColor(color);
					p.draw();
				}
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setColorOfAllPartsOfSameType(final HousePart part, final ReadOnlyColorRGBA color) {
		for (final HousePart p : parts) {
			if (p.getClass().equals(part.getClass())) {
				p.setColor(color);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
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
		if (x instanceof Thermal) {
			if (x instanceof Foundation) {
				((Foundation) x).setUValue(uValue);
			} else {
				for (final HousePart p : parts) {
					if (p.getClass().equals(x.getClass()) && p.getTopContainer() == x.getTopContainer()) {
						((Thermal) p).setUValue(uValue);
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

	// solar panels

	public List<SolarPanel> getAllSolarPanels() {
		final List<SolarPanel> list = new ArrayList<SolarPanel>();
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel) {
				list.add((SolarPanel) p);
			}
		}
		return list;
	}

	public void setCellNumbersForAllSolarPanels(final int nx, final int ny) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel) {
				final SolarPanel s = (SolarPanel) p;
				s.setNumberOfCellsInX(nx);
				s.setNumberOfCellsInY(ny);
			} else if (p instanceof Rack) {
				final SolarPanel s = ((Rack) p).getSolarPanel();
				s.setNumberOfCellsInX(nx);
				s.setNumberOfCellsInY(ny);
			}
			p.draw();
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

	public boolean checkContainerIntersectionForAllSolarPanels() {
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof SolarPanel) {
				if (((SolarPanel) p).checkContainerIntersection()) {
					return true;
				}
			}
		}
		return false;
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

	public void setBaseHeightForAllSolarPanels(final double baseHeight) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel) {
				((SolarPanel) p).setBaseHeight(baseHeight);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setTrackerForAllSolarPanels(final int tracker) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel && !(p.getContainer() instanceof Rack)) { // no tracker for solar panels on racks as they use rack trackers
				((SolarPanel) p).setTracker(tracker);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setModelForAllSolarPanels(final PvModuleSpecs specs) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel) {
				((SolarPanel) p).setPvModuleSpecs(specs);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setCellTypeForAllSolarPanels(final int cellType) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel) {
				((SolarPanel) p).setCellType(cellType);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setColorForAllSolarPanels(final int colorOption) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel) {
				((SolarPanel) p).setColorOption(colorOption);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setShadeToleranceForAllSolarPanels(final int cellWiring) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel) {
				((SolarPanel) p).setShadeTolerance(cellWiring);
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

	public void setTemperatureCoefficientPmaxForAll(final double tcPmax) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel) {
				((SolarPanel) p).setTemperatureCoefficientPmax(tcPmax);
			}
		}
	}

	public void setNominalOperatingCellTemperatureForAll(final double noct) {
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel) {
				((SolarPanel) p).setNominalOperatingCellTemperature(noct);
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

	// solar panel racks

	public List<Rack> getAllRacks() {
		final List<Rack> list = new ArrayList<Rack>();
		for (final HousePart p : parts) {
			if (p instanceof Rack) {
				list.add((Rack) p);
			}
		}
		return list;
	}

	public void setTiltAngleForAllRacks(final double angle) {
		for (final HousePart p : parts) {
			if (p instanceof Rack) {
				((Rack) p).setTiltAngle(angle);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public boolean checkContainerIntersectionForAllRacks() {
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Rack) {
				if (((Rack) p).checkContainerIntersection()) {
					return true;
				}
			}
		}
		return false;
	}

	public void setAzimuthForAllRacks(final double angle) {
		for (final HousePart p : parts) {
			if (p instanceof Rack) {
				((Rack) p).setRelativeAzimuth(angle);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setBaseHeightForAllRacks(final double baseHeight) {
		for (final HousePart p : parts) {
			if (p instanceof Rack) {
				((Rack) p).setBaseHeight(baseHeight);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setSizeForAllRacks(final double width, final double height) {
		for (final HousePart p : parts) {
			if (p instanceof Rack) {
				final Rack rack = (Rack) p;
				rack.setRackWidth(width);
				rack.setRackHeight(height);
				rack.ensureFullSolarPanels(false);
				rack.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setSolarPanelModelForAllRacks(final PvModuleSpecs specs) {
		for (final HousePart p : parts) {
			if (p instanceof Rack) {
				final Rack r = (Rack) p;
				final SolarPanel s = r.getSolarPanel();
				s.setPvModuleSpecs(specs);
				r.ensureFullSolarPanels(false);
				r.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setSolarPanelSizeForAllRacks(final double width, final double height, final int numberOfCellsInX, final int numberOfCellsInY) {
		for (final HousePart p : parts) {
			if (p instanceof Rack) {
				final Rack rack = (Rack) p;
				final SolarPanel s = rack.getSolarPanel();
				s.setPanelWidth(width);
				s.setPanelHeight(height);
				s.setNumberOfCellsInX(numberOfCellsInX);
				s.setNumberOfCellsInY(numberOfCellsInY);
				rack.ensureFullSolarPanels(false);
				rack.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setSolarPanelColorForAllRacks(final int colorOption) {
		for (final HousePart p : parts) {
			if (p instanceof Rack) {
				final Rack rack = (Rack) p;
				rack.getSolarPanel().setColorOption(colorOption);
				rack.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setSolarPanelCellTypeForAllRacks(final int cellType) {
		for (final HousePart p : parts) {
			if (p instanceof Rack) {
				final Rack rack = (Rack) p;
				rack.getSolarPanel().setCellType(cellType);
				rack.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setSolarPanelShadeToleranceForAllRacks(final int tolerance) {
		for (final HousePart p : parts) {
			if (p instanceof Rack) {
				((Rack) p).getSolarPanel().setShadeTolerance(tolerance);
			}
		}
	}

	public void setSolarCellEfficiencyForAllRacks(final double eff) {
		for (final HousePart p : parts) {
			if (p instanceof Rack) {
				((Rack) p).getSolarPanel().setCellEfficiency(eff);
			}
		}
	}

	public void setInverterEfficiencyForAllRacks(final double eff) {
		for (final HousePart p : parts) {
			if (p instanceof Rack) {
				((Rack) p).getSolarPanel().setInverterEfficiency(eff);
			}
		}
	}

	public void setTemperatureCoefficientPmaxForAllRacks(final double pmax) {
		for (final HousePart p : parts) {
			if (p instanceof Rack) {
				((Rack) p).getSolarPanel().setTemperatureCoefficientPmax(pmax);
			}
		}
	}

	public void setNominalOperatingCellTemperatureForAllRacks(final double noct) {
		for (final HousePart p : parts) {
			if (p instanceof Rack) {
				((Rack) p).getSolarPanel().setNominalOperatingCellTemperature(noct);
			}
		}
	}

	public void rotateSolarPanelsOnAllRacks(final boolean rotated) {
		for (final HousePart p : parts) {
			if (p instanceof Rack) {
				final Rack r = (Rack) p;
				r.getSolarPanel().setRotated(rotated);
				r.ensureFullSolarPanels(false);
				r.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setPoleSpacingForAllRacks(final double dx, final double dy, final boolean visible) {
		for (final HousePart p : parts) {
			if (p instanceof Rack) {
				final Rack r = (Rack) p;
				r.setPoleDistanceX(dx);
				r.setPoleDistanceY(dy);
				r.setPoleVisible(visible);
				r.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setTrackerForAllRacks(final int tracker) {
		for (final HousePart p : parts) {
			if (p instanceof Rack) {
				((Rack) p).setTracker(tracker);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	// mirrors

	public List<Mirror> getAllMirrors() {
		final List<Mirror> list = new ArrayList<Mirror>();
		for (final HousePart p : parts) {
			if (p instanceof Mirror) {
				list.add((Mirror) p);
			}
		}
		return list;
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

	public void setAzimuthForAllMirrors(final double angle) {
		for (final HousePart p : parts) {
			if (p instanceof Mirror) {
				((Mirror) p).setRelativeAzimuth(angle);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setReflectanceForAllMirrors(final double reflectance) {
		for (final HousePart p : parts) {
			if (p instanceof Mirror) {
				((Mirror) p).setReflectance(reflectance);
			}
		}
	}

	public void setTargetForAllMirrors(final Foundation target) {
		final List<Foundation> oldTargets = new ArrayList<Foundation>();
		for (final HousePart p : parts) {
			if (p instanceof Mirror) {
				final Mirror m = (Mirror) p;
				final Foundation t = m.getHeliostatTarget();
				if (t != null && !oldTargets.contains(t)) {
					oldTargets.add(t);
				}
				m.setHeliostatTarget(target);
				p.draw();
			}
		}
		if (target != null) {
			target.drawSolarReceiver();
		}
		if (!oldTargets.isEmpty()) {
			for (final Foundation t : oldTargets) {
				t.drawSolarReceiver();
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

	public void setSizeForAllMirrors(final double width, final double height) {
		for (final HousePart p : parts) {
			if (p instanceof Mirror) {
				((Mirror) p).setMirrorWidth(width);
				((Mirror) p).setMirrorHeight(height);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	// parabolic troughs

	public List<ParabolicTrough> getAllParabolicTroughs() {
		final List<ParabolicTrough> list = new ArrayList<ParabolicTrough>();
		for (final HousePart p : parts) {
			if (p instanceof ParabolicTrough) {
				list.add((ParabolicTrough) p);
			}
		}
		return list;
	}

	public void setAzimuthForAllParabolicTroughs(final double angle) {
		for (final HousePart p : parts) {
			if (p instanceof ParabolicTrough) {
				((ParabolicTrough) p).setRelativeAzimuth(angle);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setReflectanceForAllParabolicTroughs(final double reflectance) {
		for (final HousePart p : parts) {
			if (p instanceof ParabolicTrough) {
				((ParabolicTrough) p).setReflectance(reflectance);
			}
		}
	}

	public void setAbsorptanceForAllParabolicTroughs(final double absorptance) {
		for (final HousePart p : parts) {
			if (p instanceof ParabolicTrough) {
				((ParabolicTrough) p).setAbsorptance(absorptance);
			}
		}
	}

	public void setOpticalEfficiencyForAllParabolicTroughs(final double efficiency) {
		for (final HousePart p : parts) {
			if (p instanceof ParabolicTrough) {
				((ParabolicTrough) p).setOpticalEfficiency(efficiency);
			}
		}
	}

	public void setThermalEfficiencyForAllParabolicTroughs(final double efficiency) {
		for (final HousePart p : parts) {
			if (p instanceof ParabolicTrough) {
				((ParabolicTrough) p).setThermalEfficiency(efficiency);
			}
		}
	}

	public void setBaseHeightForAllParabolicTroughs(final double baseHeight) {
		for (final HousePart p : parts) {
			if (p instanceof ParabolicTrough) {
				((ParabolicTrough) p).setBaseHeight(baseHeight);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setSizeForAllParabolicTroughs(final double length, final double width, final double moduleLength) {
		for (final HousePart p : parts) {
			if (p instanceof ParabolicTrough) {
				final ParabolicTrough t = (ParabolicTrough) p;
				t.setTroughLength(length);
				t.setApertureWidth(width);
				t.setModuleLength(moduleLength);
				t.ensureFullModules(false);
				t.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setSectionsForAllParabolicTroughs(final int nParabola, final int nAxis) {
		for (final HousePart p : parts) {
			if (p instanceof ParabolicTrough) {
				final ParabolicTrough t = (ParabolicTrough) p;
				t.setNSectionParabola(nParabola);
				t.setNSectionAxis(nAxis);
				t.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setSemilatusRectumForAllParabolicTroughs(final double semilatusRectum) {
		for (final HousePart p : parts) {
			if (p instanceof ParabolicTrough) {
				final ParabolicTrough t = (ParabolicTrough) p;
				t.setSemilatusRectum(semilatusRectum);
				t.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	// parabolic dishes

	public List<ParabolicDish> getAllParabolicDishes() {
		final List<ParabolicDish> list = new ArrayList<ParabolicDish>();
		for (final HousePart p : parts) {
			if (p instanceof ParabolicDish) {
				list.add((ParabolicDish) p);
			}
		}
		return list;
	}

	public void setStructureTypeForAllParabolicDishes(final int structureType) {
		for (final HousePart p : parts) {
			if (p instanceof ParabolicDish) {
				((ParabolicDish) p).setStructureType(structureType);
			}
		}
	}

	public void setReflectanceForAllParabolicDishes(final double reflectance) {
		for (final HousePart p : parts) {
			if (p instanceof ParabolicDish) {
				((ParabolicDish) p).setReflectance(reflectance);
			}
		}
	}

	public void setAbsorptanceForAllParabolicDishes(final double absorptance) {
		for (final HousePart p : parts) {
			if (p instanceof ParabolicDish) {
				((ParabolicDish) p).setAbsorptance(absorptance);
			}
		}
	}

	public void setOpticalEfficiencyForAllParabolicDishes(final double efficiency) {
		for (final HousePart p : parts) {
			if (p instanceof ParabolicDish) {
				((ParabolicDish) p).setOpticalEfficiency(efficiency);
			}
		}
	}

	public void setThermalEfficiencyForAllParabolicDishes(final double efficiency) {
		for (final HousePart p : parts) {
			if (p instanceof ParabolicDish) {
				((ParabolicDish) p).setThermalEfficiency(efficiency);
			}
		}
	}

	public void setBaseHeightForAllParabolicDishes(final double baseHeight) {
		for (final HousePart p : parts) {
			if (p instanceof ParabolicDish) {
				((ParabolicDish) p).setBaseHeight(baseHeight);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setRimRadiusForAllParabolicDishes(final double apertureRadius) {
		for (final HousePart p : parts) {
			if (p instanceof ParabolicDish) {
				((ParabolicDish) p).setRimRadius(apertureRadius);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setFocalLengthForAllParabolicDishes(final double curvatureParameter) {
		for (final HousePart p : parts) {
			if (p instanceof ParabolicDish) {
				((ParabolicDish) p).setFocalLength(curvatureParameter);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setSectionsForAllParabolicDishes(final int nParabola, final int nAxis) {
		for (final HousePart p : parts) {
			if (p instanceof ParabolicDish) {
				final ParabolicDish d = (ParabolicDish) p;
				d.setNRadialSections(nParabola);
				d.setNAxialSections(nAxis);
				d.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setNumberOfRibsForAllParabolicDishes(final int nrib) {
		for (final HousePart p : parts) {
			if (p instanceof ParabolicDish) {
				final ParabolicDish d = (ParabolicDish) p;
				d.setNumberOfRibs(nrib);
				d.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}
	// Fresnel reflectors

	public List<FresnelReflector> getAllFresnelReflectors() {
		final List<FresnelReflector> list = new ArrayList<FresnelReflector>();
		for (final HousePart p : parts) {
			if (p instanceof FresnelReflector) {
				list.add((FresnelReflector) p);
			}
		}
		return list;
	}

	public void setReflectanceForAllFresnelReflectors(final double reflectance) {
		for (final HousePart p : parts) {
			if (p instanceof FresnelReflector) {
				((FresnelReflector) p).setReflectance(reflectance);
			}
		}
	}

	public void setAbsorptanceForAllFresnelReflectors(final double absorptance) {
		for (final HousePart p : parts) {
			if (p instanceof FresnelReflector) {
				((FresnelReflector) p).setAbsorptance(absorptance);
			}
		}
	}

	public void setOpticalEfficiencyForAllFresnelReflectors(final double efficiency) {
		for (final HousePart p : parts) {
			if (p instanceof FresnelReflector) {
				((FresnelReflector) p).setOpticalEfficiency(efficiency);
			}
		}
	}

	public void setAbsorberForAllFresnelReflectors(final Foundation target) {
		final List<Foundation> oldTargets = new ArrayList<Foundation>();
		for (final HousePart p : parts) {
			if (p instanceof FresnelReflector) {
				final FresnelReflector r = (FresnelReflector) p;
				final Foundation t = r.getAbsorber();
				if (t != null && !oldTargets.contains(t)) {
					oldTargets.add(t);
				}
				r.setAbsorber(target);
				r.draw();
			}
		}
		if (target != null) {
			target.drawSolarReceiver();
		}
		if (!oldTargets.isEmpty()) {
			for (final Foundation t : oldTargets) {
				t.drawSolarReceiver();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setBaseHeightForAllFresnelReflectors(final double baseHeight) {
		for (final HousePart p : parts) {
			if (p instanceof FresnelReflector) {
				((FresnelReflector) p).setBaseHeight(baseHeight);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setSizeForAllFresnelReflectors(final double length, final double width, final double moduleLength) {
		for (final HousePart p : parts) {
			if (p instanceof FresnelReflector) {
				final FresnelReflector r = (FresnelReflector) p;
				r.setLength(length);
				r.setModuleWidth(width);
				r.setModuleLength(moduleLength);
				r.ensureFullModules(false);
				r.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setSectionsForAllFresnelReflectors(final int nLength, final int nWidth) {
		for (final HousePart p : parts) {
			if (p instanceof FresnelReflector) {
				final FresnelReflector r = (FresnelReflector) p;
				r.setNSectionLength(nLength);
				r.setNSectionWidth(nWidth);
				r.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	// walls

	public void setThicknessForAllWalls(final double thickness) {
		for (final HousePart p : parts) {
			if (p instanceof Wall) {
				((Wall) p).setThickness(thickness);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setHeightOfConnectedWalls(final Wall w, final double height) {
		w.visitNeighbors(new WallVisitor() {
			@Override
			public void visit(final Wall currentWall, final Snap prev, final Snap next) {
				currentWall.setHeight(height, true);
				currentWall.draw();
			}
		});
		SceneManager.getInstance().refresh();
	}

	public void showOutlineOfConnectedWalls(final Wall w, final boolean b) {
		w.visitNeighbors(new WallVisitor() {
			@Override
			public void visit(final Wall currentWall, final Snap prev, final Snap next) {
				currentWall.showOutline(b);
				currentWall.draw();
			}
		});
		SceneManager.getInstance().refresh();
	}

	public void setHeightForAllWalls(final double height) {
		for (final HousePart p : parts) {
			if (p instanceof Wall) {
				((Wall) p).setHeight(height, true);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void showOutlineForAllWalls(final boolean b) {
		for (final HousePart p : parts) {
			if (p instanceof Wall) {
				((Wall) p).showOutline(b);
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setColorOfConnectedWalls(final Wall w, final ColorRGBA color) {
		w.visitNeighbors(new WallVisitor() {
			@Override
			public void visit(final Wall currentWall, final Snap prev, final Snap next) {
				currentWall.setColor(color);
				currentWall.draw();
			}
		});
		SceneManager.getInstance().refresh();
	}

	public boolean isEdited() {
		return edited;
	}

	public void setEdited(final boolean edited) {
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

	public void setProjectType(final int projectType) {
		this.projectType = projectType;
		for (final HousePart p : parts) {
			if (p instanceof Foundation) {
				((Foundation) p).setProjectType(projectType);
			}
		}
	}

	public int getProjectType() {
		return projectType;
	}

	public void setProjectName(final String projectName) {
		this.projectName = projectName;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setDesigner(final Designer designer) {
		this.designer = designer;
	}

	public Designer getDesigner() {
		return designer;
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

	public void setHeatFluxGridSize(final double heatFluxGridSize) {
		this.heatFluxGridSize = heatFluxGridSize;
	}

	public double getHeatVectorGridSize() {
		return heatFluxGridSize;
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

	public boolean getOnlySolarComponentsInSolarMap() {
		return onlySolarComponentsInSolarMap;
	}

	public void setOnlySolarComponentsInSolarMap(final boolean onlySolarComponentsInSolarMap) {
		this.onlySolarComponentsInSolarMap = onlySolarComponentsInSolarMap;
		if (onlySolarComponentsInSolarMap) {
			setSolarMapForLand(false);
			SceneManager.getInstance().getSolarLand().setVisible(false);
		}
	}

	public boolean getOnlyAbsorptionInSolarMap() {
		return !fullEnergyInSolarMap;
	}

	public void setOnlyAbsorptionInSolarMap(final boolean onlyAbsorptionInSolarMap) {
		fullEnergyInSolarMap = !onlyAbsorptionInSolarMap;
	}

	public boolean getOnlyReflectedEnergyInMirrorSolarMap() {
		return onlyReflectedEnergyInMirrorSolarMap;
	}

	public void setOnlyReflectedEnergyInMirrorSolarMap(final boolean onlyReflectedEnergyInMirrorSolarMap) {
		this.onlyReflectedEnergyInMirrorSolarMap = onlyReflectedEnergyInMirrorSolarMap;
	}

	public void setSolarMapForLand(final boolean solarMapForLand) {
		this.solarMapForLand = solarMapForLand;
	}

	public boolean getSolarMapForLand() {
		return solarMapForLand;
	}

	public void setDisallowFoundationOverlap(final boolean disallowFoundationOverlap) {
		this.disallowFoundationOverlap = disallowFoundationOverlap;
	}

	public boolean getDisallowFoundationOverlap() {
		return disallowFoundationOverlap;
	}

	public void setSolarHeatMapColorContrast(final int solarContrast) {
		this.solarContrast = solarContrast;
	}

	public int getSolarHeatMapColorContrast() {
		return solarContrast;
	}

	public int countParts(final Class<?> c) {
		int count = 0;
		for (final HousePart p : parts) {
			if (c.isInstance(p)) {
				count++;
			}
		}
		return count;
	}

	public int countParts(final Class<?>[] clazz) {
		int count = 0;
		for (final HousePart p : parts) {
			for (final Class<?> c : clazz) {
				if (c.isInstance(p)) {
					count++;
				}
			}
		}
		return count;
	}

	// special treatment of counting solar panels that include individual solar panels and solar panels on racks
	public int countSolarPanels() {
		int count = 0;
		for (final HousePart p : parts) {
			if (p instanceof SolarPanel) {
				count++;
			} else if (p instanceof Rack) {
				count += ((Rack) p).getNumberOfSolarPanels();
			}
		}
		return count;
	}

	public int countNodes() {
		int count = 0;
		final List<Foundation> foundations = getAllFoundations();
		for (final Foundation f : foundations) {
			if (f.getImportedNodes() != null) {
				count += f.getImportedNodes().size();
			}
		}
		return count;
	}

	public int countMeshes() {
		int count = 0;
		final List<Foundation> foundations = getAllFoundations();
		for (final Foundation f : foundations) {
			if (f.getImportedNodes() != null) {
				for (final Node n : f.getImportedNodes()) {
					count += n.getNumberOfChildren();
				}
			}
		}
		return count;
	}

	// XIE: This needs to be called for trees to change texture when the month changes
	public void updateTreeLeaves() {
		if (SceneManager.isTaskManagerThread()) {
			updateTreeLeavesImmediately();
		} else {
			SceneManager.getTaskManager().update(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					updateTreeLeavesImmediately();
					return null;
				}
			});
		}
	}

	private void updateTreeLeavesImmediately() {
		for (final HousePart p : parts) {
			if (p instanceof Tree) {
				p.updateTextureAndColor();
			}
		}
	}

	public void updateLabels() {
		if (SceneManager.isTaskManagerThread()) {
			updateLablesImmediately();
		} else {
			SceneManager.getTaskManager().update(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					updateLablesImmediately();
					return null;
				}
			});
		}
	}

	private void updateLablesImmediately() {
		for (final HousePart p : parts) { // update the parts that support floating labels
			if (p instanceof Labelable) {
				((Labelable) p).updateLabel();
			}
		}
	}

	public void updateTrackables() {
		if (SceneManager.isTaskManagerThread()) {
			updateTrackablesImmediately();
		} else {
			SceneManager.getTaskManager().update(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					updateTrackablesImmediately();
					return null;
				}
			});
		}
	}

	private void updateTrackablesImmediately() {
		final boolean night = Heliodon.getInstance().isNightTime();
		for (final HousePart part : parts) {
			if (part instanceof Mirror) {
				final Mirror mirror = (Mirror) part;
				if (night) {
					mirror.drawSunBeam(); // call this so that the light beams can be set invisible
				} else {
					mirror.draw();
				}
			} else if (part instanceof ParabolicTrough) {
				final ParabolicTrough trough = (ParabolicTrough) part;
				if (night) {
					trough.drawSunBeam(); // call this so that the light beams can be set invisible
				} else {
					trough.draw();
				}
			} else if (part instanceof ParabolicDish) {
				final ParabolicDish dish = (ParabolicDish) part;
				if (night) {
					dish.drawSunBeam(); // call this so that the light beams can be set invisible
				} else {
					dish.draw();
				}
			} else if (part instanceof FresnelReflector) {
				final FresnelReflector fresnel = (FresnelReflector) part;
				if (night) {
					fresnel.drawSunBeam(); // call this so that the light beams can be set invisible
				} else {
					fresnel.draw();
				}
			} else if (part instanceof SolarPanel) {
				final SolarPanel panel = (SolarPanel) part;
				if (night) {
					panel.drawSunBeam(); // call this so that the sun beam can be set invisible
				} else {
					panel.draw();
				}
			} else if (part instanceof Rack) {
				final Rack rack = (Rack) part;
				if (night) {
					rack.drawSunBeam(); // call this so that the sun beam can be set invisible
				} else {
					rack.draw();
				}
			}
		}
	}

	public Ground getGround() {
		return ground;
	}

	public Atmosphere getAtmosphere() {
		return atmosphere;
	}

	public DesignSpecs getDesignSpecs() {
		return designSpecs;
	}

	public PvDesignSpecs getPvDesignSpecs() {
		return pvDesignSpecs;
	}

	public CspDesignSpecs getCspDesignSpecs() {
		return cspDesignSpecs;
	}

	public PvCustomPrice getPvCustomPrice() {
		return pvCustomPrice;
	}

	public CspCustomPrice getCspCustomPrice() {
		return cspCustomPrice;
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
		case DESERT_THEME:
			c = new ColorRGBA(1, 1, 1, 0.5f);
			break;
		case GRASSLAND_THEME:
			c = new ColorRGBA(0, 1, 0, 0.5f);
			break;
		case FOREST_THEME:
			c = new ColorRGBA(0, 1, 0.2f, 0.5f);
			break;
		default:
			c = landColor == null ? new ColorRGBA(0, 1, 0.75f, 0.5f) : landColor;
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

	public void setSolarPanelNx(final int solarPanelNx) {
		this.solarPanelNx = solarPanelNx;
	}

	public int getSolarPanelNx() {
		return solarPanelNx;
	}

	public void setSolarPanelNy(final int solarPanelNy) {
		this.solarPanelNy = solarPanelNy;
	}

	public int getSolarPanelNy() {
		return solarPanelNy;
	}

	public void setRackNx(final int rackNx) {
		this.rackNx = rackNx;
	}

	public int getRackNx() {
		return rackNx;
	}

	public void setRackNy(final int rackNy) {
		this.rackNy = rackNy;
	}

	public int getRackNy() {
		return rackNy;
	}

	public void setRackCellSize(final double rackCellSize) {
		this.rackCellSize = rackCellSize;
	}

	public double getRackCellSize() {
		return rackCellSize;
	}

	public void setRackModelExact(final boolean rackModelExact) {
		this.rackModelExact = rackModelExact;
	}

	public boolean isRackModelExact() {
		return rackModelExact;
	}

	public void setMirrorNx(final int mirrorNx) {
		this.mirrorNx = mirrorNx;
	}

	public int getMirrorNx() {
		return mirrorNx;
	}

	public void setMirrorNy(final int mirrorNy) {
		this.mirrorNy = mirrorNy;
	}

	public int getMirrorNy() {
		return mirrorNy;
	}

	public void setParabolicDishN(final int parabolicDishN) {
		this.parabolicDishN = parabolicDishN;
	}

	public int getParabolicDishN() {
		return parabolicDishN;
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

	public void setGroundImage(final BufferedImage groundImage, final double groundImageScale) {
		this.groundImage = groundImage;
		this.groundImageScale = groundImageScale;
		applyGroundImage();
	}

	public void setGroundImageScale(final double groundImageScale) {
		if (isGroundImageEnabled()) {
			this.groundImageScale = groundImageScale;
			applyGroundImage();
		}
	}

	public double getGroundImageScale() {
		return groundImageScale;
	}

	public boolean isGroundImageEnabled() {
		return groundImage != null;
	}

	private void applyGroundImage() {
		final Mesh mesh = SceneManager.getInstance().getGroundImageLand();
		if (groundImage == null) {
			mesh.setRenderState(new TextureState()); // set a dummy texture in case the mesh holds the original buffered image and causes memory leak
			mesh.setVisible(false);
			setFoundationsVisible(true);
		} else {
			SceneManager.getInstance().resizeGroundImageLand(groundImageScale);
			final Texture2D texture = new Texture2D();
			texture.setTextureKey(TextureKey.getRTTKey(MinificationFilter.NearestNeighborNoMipMaps));
			texture.setImage(AWTImageLoader.makeArdor3dImage(groundImage, true));
			final TextureState textureState = new TextureState();
			textureState.setTexture(texture);
			mesh.setRenderState(textureState);
			mesh.setVisible(!hideGroundImage);
			setFoundationsVisible(false);
		}
	}

	public void setGroundImageEarthView(final boolean groundImageIsEarthView) {
		this.groundImageIsEarthView = groundImageIsEarthView;
	}

	public boolean isGroundImageEarthView() {
		return groundImageIsEarthView;
	}

	private void setFoundationsVisible(final boolean visible) {
		for (final HousePart part : Scene.getInstance().getParts()) {
			if (part instanceof Foundation) {
				part.getMesh().setVisible(visible);
			}
		}
		SceneManager.getInstance().refresh();
	}

	public void setShowGroundImage(final boolean showGroundImage) {
		hideGroundImage = !showGroundImage;
	}

	public boolean getShowGroundImage() {
		return !hideGroundImage;
	}

	public void setGroundImageLightColored(final boolean groundImageLightColored) {
		this.groundImageLightColored = groundImageLightColored;
	}

	public boolean isGroundImageLightColored() {
		return groundImageLightColored;
	}

	/** used by SnapshotLogger */
	private void storeGroundImageData() {
		avoidSavingGroundImage = true;
	}

	/** used by SnapshotLogger */
	private void restoreGroundImageData() {
		avoidSavingGroundImage = false;
	}

	private void writeObject(final ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		if (groundImage != null && !avoidSavingGroundImage) {
			ImageIO.write(groundImage, "jpg", out);
		}
	}

	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		groundImage = ImageIO.read(in);
	}

	public static boolean isSaving() {
		return isSaving;
	}

	public List<Foundation> getAllFoundations() {
		final List<Foundation> list = new ArrayList<Foundation>();
		for (final HousePart p : parts) {
			if (p instanceof Foundation) {
				list.add((Foundation) p);
			}
		}
		return list;
	}

	public Rectangle2D getFoundationBounds(final boolean excludeSelectedPart) {
		Rectangle2D bounds = new Rectangle2D.Double(-0.1, -0.1, 0.2, 0.2);
		if (excludeSelectedPart) {
			for (final HousePart p : parts) {
				if (p instanceof Foundation && p != SceneManager.getInstance().getSelectedPart()) {
					final Foundation f = (Foundation) p;
					final Vector3 v0 = f.getAbsPoint(0);
					final Vector3 v1 = f.getAbsPoint(1);
					final Vector3 v2 = f.getAbsPoint(2);
					final Vector3 v3 = f.getAbsPoint(3);
					final double cx = 0.25 * (v0.getX() + v1.getX() + v2.getX() + v3.getX());
					final double cy = 0.25 * (v0.getY() + v1.getY() + v2.getY() + v3.getY());
					final double lx = v0.distance(v2);
					final double ly = v0.distance(v1);
					bounds = bounds.createUnion(new Rectangle2D.Double(cx - lx * 0.5, cy - ly * 0.5, lx, ly));
				}
			}
		} else {
			for (final HousePart p : parts) {
				if (p instanceof Foundation) {
					final Foundation f = (Foundation) p;
					final Vector3 v0 = f.getAbsPoint(0);
					final Vector3 v1 = f.getAbsPoint(1);
					final Vector3 v2 = f.getAbsPoint(2);
					final Vector3 v3 = f.getAbsPoint(3);
					final double cx = 0.25 * (v0.getX() + v1.getX() + v2.getX() + v3.getX());
					final double cy = 0.25 * (v0.getY() + v1.getY() + v2.getY() + v3.getY());
					final double lx = v0.distance(v2);
					final double ly = v0.distance(v1);
					bounds = bounds.createUnion(new Rectangle2D.Double(cx - lx * 0.5, cy - ly * 0.5, lx, ly));
				}
			}
		}
		return bounds;
	}

	public List<Foundation> getFoundationGroup(final Foundation master) {
		groupFoundations();
		for (final List<Foundation> g : foundationGroups) {
			if (g.contains(master)) {
				return g;
			}
		}
		return null;
	}

	/* put overlapping foundations into separate groups */
	private void groupFoundations() {
		if (foundationGroups == null) {
			foundationGroups = new ArrayList<List<Foundation>>();
		} else {
			foundationGroups.clear();
		}
		final List<Foundation> foundations = new ArrayList<Foundation>();
		for (final HousePart p : parts) {
			if (p instanceof Foundation) {
				foundations.add((Foundation) p);
			}
		}
		final int n = foundations.size();
		if (n > 1) {
			int count = 0;
			final List<Pair> pairs = new ArrayList<Pair>();
			for (int i = 0; i < n - 1; i++) {
				final Foundation fi = foundations.get(i);
				for (int j = i + 1; j < n; j++) {
					final Foundation fj = foundations.get(j);
					if (fi.overlap(fj)) {
						final Pair p = new Pair(i, j);
						if (!pairs.contains(p)) {
							pairs.add(p);
						}
					}
				}
			}

			int m = pairs.size();
			while (m > 0) {
				Pair p = pairs.get(0);
				final List<Integer> list = new ArrayList<Integer>();
				list.add(p.i());
				list.add(p.j());
				pairs.remove(p);
				m = pairs.size();
				if (m > 0) {
					final List<Pair> toRemove = new ArrayList<Pair>();
					for (int x = 0; x < m; x++) {
						p = pairs.get(x);
						final int i = p.i();
						final int j = p.j();
						if (list.contains(i) && list.contains(j)) {
							if (!toRemove.contains(p)) {
								toRemove.add(p);
							}
						} else if (!list.contains(i) && list.contains(j)) {
							list.add(i);
							if (!toRemove.contains(p)) {
								toRemove.add(p);
							}
						} else if (!list.contains(j) && list.contains(i)) {
							list.add(j);
							if (!toRemove.contains(p)) {
								toRemove.add(p);
							}
						}
					}
					if (!toRemove.isEmpty()) {
						pairs.removeAll(toRemove);
					}
				}

				final List<Foundation> group = new ArrayList<Foundation>();
				for (final Integer a : list) {
					group.add(foundations.get(a));
				}
				foundationGroups.add(group);
				count += group.size();

				m = pairs.size();

			}

			if (count < foundations.size()) {
				for (final Foundation f : foundations) {
					boolean linked = false;
					for (final List<Foundation> g : foundationGroups) {
						if (g.contains(f)) {
							linked = true;
							break;
						}
					}
					if (!linked) {
						final List<Foundation> g = new ArrayList<Foundation>();
						g.add(f);
						foundationGroups.add(g);
					}
				}
			}
			// System.out.println("###" + foundationGroups.size() + "," + (foundations.size() - count));
		} else {
			foundationGroups.add(foundations);
		}

	}

	public void setInstructionSheetText(final int i, final String text) {
		if (instructionSheetText == null) {
			instructionSheetText = new String[3];
		}
		if (i < instructionSheetText.length) {
			instructionSheetText[i] = text;
		}
	}

	public String getInstructionSheetText(final int i) {
		if (instructionSheetText == null) {
			return null;
		}
		if (i >= instructionSheetText.length) {
			return null;
		}
		return instructionSheetText[i];
	}

	public void setInstructionSheetTextType(final int i, final String type) {
		if (instructionSheetTextType == null) {
			instructionSheetTextType = new String[3];
		}
		if (i < instructionSheetTextType.length) {
			instructionSheetTextType[i] = type;
		}
	}

	public String getInstructionSheetTextType(final int i) {
		if (instructionSheetTextType == null) {
			return null;
		}
		if (i >= instructionSheetTextType.length) {
			return null;
		}
		return instructionSheetTextType[i];
	}

	public void setDisableShadowInAction(final boolean b) {
		disableShadowInAction = b;
	}

	public boolean getDisableShadowInAction() {
		return disableShadowInAction;
	}

	public void setGeoLocation(final double latitude, final double longitude, final int zoom, final String address) {
		if (geoLocation == null) {
			geoLocation = new GeoLocation(latitude, longitude);
		}
		geoLocation.setLatitude(latitude);
		geoLocation.setLongitude(longitude);
		geoLocation.setZoom(zoom);
		geoLocation.setAddress(address);
	}

	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	public boolean isSnapToGrids() {
		return !noSnapToGrids;
	}

	public void setSnapToGrids(final boolean snapToGrid) {
		this.noSnapToGrids = !snapToGrid;
	}

}
