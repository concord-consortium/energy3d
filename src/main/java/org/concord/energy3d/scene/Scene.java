package org.concord.energy3d.scene;

import java.awt.EventQueue;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.gui.EnergyPanel.UpdateRadiation;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.gui.MainPanel;
import org.concord.energy3d.model.Door;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.Snap;
import org.concord.energy3d.model.Tree;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.shapes.Heliodon;
import org.concord.energy3d.simulation.SolarIrradiation;
import org.concord.energy3d.undo.SaveCommand;
import org.concord.energy3d.util.Config;
import org.concord.energy3d.util.Specifications;

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
	private static final Node notReceivingShadowRoot = new Node("Trees Root");
	private static final int currentVersion = 1;
	private static Scene instance;
	private static URL url = null;
	private static boolean redrawAll = false;
	private static boolean drawThickness = false;
	private static boolean drawAnnotationsInside = false;
	private static Unit unit = Unit.Meter;
	private transient boolean edited = false;
	private final List<HousePart> parts = Collections.synchronizedList(new ArrayList<HousePart>());
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
	private boolean hideAxes;
	private boolean showBuildingLabels;
	private double solarStep = 2.0;
	private int timeStep = 15; // in minutes
	private int budget = 100000; // in US dollars
	private double minimumArea = 100;
	private double maximumArea = 150;
	private double minimumHeight = 8;
	private double maximumHeight = 10;
	private boolean budgetEnabled;
	private boolean areaEnabled;
	private boolean heightEnabled;
	private boolean cleanup = false;
	private String wallUFactor;
	private String doorUFactor;
	private String windowUFactor;
	private String roofUFactor;
	private double solarPanelEfficiency;
	private double windowSolarHeatGainCoefficient; // range: 0.25-0.80 (we choose 0.5 by default) - http://www.energystar.gov/index.cfm?c=windows_doors.pr_ind_tested
	private double backgroundAlbedo = 0.3;
	private double heatVectorLength = 10000;

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
		EnergyPanel.getInstance().updatePartEnergy();
		EnergyPanel.getInstance().updateCost();
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
		openNow(file);
		SceneManager.getTaskManager().update(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				initSceneNow();
				instance.redrawAllNow(); // needed in case Heliodon is on and needs to be drawn with correct size
				initEnergy();
				EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
				return null;
			}
		});
	}

	public static void openNow(final URL file) throws Exception {

		if (PrintController.getInstance().isPrintPreview()) {
			MainPanel.getInstance().getPreviewButton().setSelected(false);
			while (!PrintController.getInstance().isFinished())
				Thread.yield();
		}

		synchronized (SceneManager.getInstance()) {

			Scene.url = file;

			MainPanel.getInstance().getHeliodonButton().setSelected(false);
			MainPanel.getInstance().getSunAnimButton().setSelected(false);
			// MainPanel.getInstance().getSolarButton().setSelected(false);
			SceneManager.getInstance().setSolarColorMapWithoutUpdate(false);
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

				instance.upgradeSceneToNewVersion();
				instance.cleanup();
				loadCameraLocation();
			}
			SceneManager.getInstance().hideAllEditPoints();
			final CameraControl cameraControl = SceneManager.getInstance().getCameraControl();
			if (cameraControl != null)
				cameraControl.reset();
		}

		if (!Config.isApplet()) {
			if (instance.textureMode == TextureMode.None)
				MainFrame.getInstance().getNoTextureMenuItem().setSelected(true);
			else if (instance.textureMode == TextureMode.Simple)
				MainFrame.getInstance().getSimpleTextureMenuItem().setSelected(true);
			else
				MainFrame.getInstance().getFullTextureMenuItem().setSelected(true);
		}
		MainPanel.getInstance().getAnnotationToggleButton().setSelected(instance.isAnnotationsVisible);
		EnergyPanel.getInstance().updatePartEnergy();
		EnergyPanel.getInstance().updateCost();

	}

	public static void initSceneNow() {
		root.detachAllChildren();
		originalHouseRoot.detachAllChildren();
		notReceivingShadowRoot.detachAllChildren();
		root.attachChild(originalHouseRoot);
		root.attachChild(notReceivingShadowRoot);

		if (url != null) {
			synchronized (instance.parts) {
				for (final HousePart housePart : instance.getParts()) {
					boolean b = housePart instanceof Tree || housePart instanceof Human;
					(b ? notReceivingShadowRoot : originalHouseRoot).attachChild(housePart.getRoot());
				}
			}
			System.out.println("done");
			/* must redraw now so that heliodon can be initialized to right size if it is to be visible */
			// instance.redrawAllNow();
		}

		root.updateWorldBound(true);
		SceneManager.getInstance().updateHeliodonAndAnnotationSize();
		SceneManager.getInstance().showAxes(!instance.hideAxes);
		SceneManager.getInstance().showBuildingLabels(instance.showBuildingLabels);
		MainPanel.getInstance().getNoteTextArea().setText(instance.note == null ? "" : instance.note);
		MainPanel.getInstance().getEnergyViewButton().setSelected(false); // moved from OpenNow to here to avoid triggering EnergyComputer -> RedrawAllNow before open is completed
		SceneManager.getInstance().getUndoManager().die();
		Scene.getInstance().setEdited(false);
	}

	public static void initEnergy() {
		final EnergyPanel energyPanel = EnergyPanel.getInstance();
		if (instance.calendar != null) {
			energyPanel.getDateSpinner().setValue(instance.calendar.getTime());
			energyPanel.getTimeSpinner().setValue(instance.calendar.getTime());
			Heliodon.getInstance().setDate(instance.calendar.getTime());
			energyPanel.setLatitude(instance.latitude);
			if ("Boston".equals(instance.city))
				instance.city = "Boston, MA";
			energyPanel.setCity(instance.city);
			MainPanel.getInstance().getHeliodonButton().setSelected(instance.isHeliodonVisible);
		}
		Specifications.getInstance().setMaximumBudget(instance.budget == 0 ? 100000 : instance.budget);
		Specifications.getInstance().setMinimumArea(instance.minimumArea == 0 ? 100 : instance.minimumArea);
		Specifications.getInstance().setMaximumArea(instance.maximumArea == 0 ? 150 : instance.maximumArea);
		Specifications.getInstance().setMinimumHeight(instance.minimumHeight == 0 ? 8 : instance.minimumHeight);
		Specifications.getInstance().setMaximumHeight(instance.maximumHeight == 0 ? 10 : instance.maximumHeight);
		Specifications.getInstance().setBudgetEnabled(instance.budgetEnabled);
		Specifications.getInstance().setAreaEnabled(instance.areaEnabled);
		Specifications.getInstance().setHeightEnabled(instance.heightEnabled);
		energyPanel.getColorMapSlider().setValue(instance.solarContrast == 0 ? 50 : instance.solarContrast);
		if (instance.windowUFactor != null)
			energyPanel.getWindowsComboBox().setSelectedItem(instance.windowUFactor);
		if (instance.wallUFactor != null)
			energyPanel.getWallsComboBox().setSelectedItem(instance.wallUFactor);
		if (instance.doorUFactor != null)
			energyPanel.getDoorsComboBox().setSelectedItem(instance.doorUFactor);
		if (instance.roofUFactor != null)
			energyPanel.getRoofsComboBox().setSelectedItem(instance.roofUFactor);
		if (instance.backgroundAlbedo < 0.000001)
			instance.backgroundAlbedo = 0.3;
		if (instance.solarPanelEfficiency < 0.000001)
			instance.solarPanelEfficiency = 10;
		if (instance.windowSolarHeatGainCoefficient < 0.000001) // not set
			instance.windowSolarHeatGainCoefficient = 50;
		else if (instance.windowSolarHeatGainCoefficient < 1)
			instance.windowSolarHeatGainCoefficient *= 100; // backward compatibility (when SHGC < 1)
		if (instance.heatVectorLength < 0.00001)
			instance.heatVectorLength = 5000;
		energyPanel.getSolarPanelEfficiencyComboBox().setSelectedItem(Double.toString(instance.solarPanelEfficiency));
		energyPanel.getWindowSHGCComboBox().setSelectedItem(Double.toString(instance.windowSolarHeatGainCoefficient));
		SolarIrradiation.getInstance().setSolarStep(instance.solarStep < 0.000001 ? 2 : instance.solarStep);
		SolarIrradiation.getInstance().setTimeStep(instance.timeStep == 0 ? 15 : instance.timeStep);
		Scene.getInstance().setEdited(false);
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
				synchronized (instance.getParts()) {
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
			overhangLength = 0.2;
		}

		if (version < 1) {
			for (final HousePart part : parts)
				if (part instanceof Foundation)
					((Foundation) part).scaleHouse(10);
			cameraLocation = cameraLocation.multiply(10, null);
			setOverhangLength(getOverhangLength() * 10);
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

				instance.budget = Specifications.getInstance().getMaximumBudget();
				instance.maximumArea = Specifications.getInstance().getMaximumArea();
				instance.minimumArea = Specifications.getInstance().getMinimumArea();
				instance.maximumHeight = Specifications.getInstance().getMaximumHeight();
				instance.minimumHeight = Specifications.getInstance().getMinimumHeight();
				instance.budgetEnabled = Specifications.getInstance().isBudgetEnabled();
				instance.areaEnabled = Specifications.getInstance().isAreaEnabled();
				instance.heightEnabled = Specifications.getInstance().isHeightEnabled();
				instance.hideAxes = !SceneManager.getInstance().areAxesShown();
				instance.showBuildingLabels = SceneManager.getInstance().areBuildingLabelsShown();
				instance.calendar = Heliodon.getInstance().getCalender();
				instance.latitude = EnergyPanel.getInstance().getLatitude();
				instance.city = (String) EnergyPanel.getInstance().getCityComboBox().getSelectedItem();
				instance.isHeliodonVisible = Heliodon.getInstance().isVisible();
				instance.note = MainPanel.getInstance().getNoteTextArea().getText().trim();
				instance.solarContrast = EnergyPanel.getInstance().getColorMapSlider().getValue();
				instance.solarStep = SolarIrradiation.getInstance().getSolarStep();
				instance.timeStep = SolarIrradiation.getInstance().getTimeStep();
				instance.wallUFactor = (String) EnergyPanel.getInstance().getWallsComboBox().getSelectedItem();
				instance.windowUFactor = (String) EnergyPanel.getInstance().getWindowsComboBox().getSelectedItem();
				instance.doorUFactor = (String) EnergyPanel.getInstance().getDoorsComboBox().getSelectedItem();
				instance.roofUFactor = (String) EnergyPanel.getInstance().getRoofsComboBox().getSelectedItem();
				instance.solarPanelEfficiency = Double.parseDouble((String) EnergyPanel.getInstance().getSolarPanelEfficiencyComboBox().getSelectedItem());

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
		final HousePart container = housePart.getContainer();
		if (container != null)
			container.getChildren().add(housePart);
		add(housePart);
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
		final HousePart container = housePart.getContainer();
		if (container != null)
			container.getChildren().remove(housePart);
		removeChildren(housePart);
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
		synchronized (instance.getParts()) {
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
		// no need for redrawing printparts because they will be regenerated from original parts anyways
		redrawAll = false;
	}

	public void updateAllTextures() {
		System.out.println("updateAllTextures()");
		for (final HousePart part : parts)
			part.updateTextureAndColor();
		SceneManager.getInstance().refresh();
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
			remove(part, false);
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
		// if (edited && recomputeEnergy)
		// EnergyPanel.getInstance().compute(UpdateRadiation.ONLY_IF_SLECTED_IN_GUI);
	}

	public void updateEditShapes() {
		for (final HousePart part : parts)
			part.updateEditShapes();
	}

	public void lockAll(final boolean freeze) {
		for (final HousePart part : parts)
			part.setFreeze(freeze);
		if (freeze)
			SceneManager.getInstance().hideAllEditPoints();
		redrawAll();
	}

	public void lockSelection(final boolean freeze) {
		final HousePart selectedPart = SceneManager.getInstance().getSelectedPart();
		if (selectedPart == null)
			return;
		selectedPart.setFreeze(freeze);
		final HousePart foundation = selectedPart.getTopContainer();
		if (foundation != null) {
			foundation.setFreeze(freeze);
			for (final HousePart p : parts) {
				if (p.getTopContainer() == foundation)
					p.setFreeze(freeze);
			}
		}
		if (freeze)
			SceneManager.getInstance().hideAllEditPoints();
		redrawAll();
	}

	public long nextID() {
		return ++idCounter;
	}

	public boolean hasSensor() {
		synchronized (parts) {
			for (final HousePart housePart : parts)
				if (housePart instanceof Sensor)
					return true;
		}
		return false;
	}

	/** @return the solar panel efficiency (not in percentage) */
	public double getSolarPanelEfficiencyNotPercentage() {
		return solarPanelEfficiency * 0.01;
	}

	public void setSolarPanelEfficiency(final double solarPanelEfficiency) {
		this.solarPanelEfficiency = solarPanelEfficiency;
	}

	/** @return the window SHGC (not in percentage) */
	public double getWindowSolarHeatGainCoefficientNotPercentage() {
		return windowSolarHeatGainCoefficient * 0.01;
	}

	public void setWindowSolarHeatGainCoefficient(final double windowSolarHeatGainCoefficient) {
		this.windowSolarHeatGainCoefficient = windowSolarHeatGainCoefficient;
	}

	public void setBackgroundAlbedo(final double backgroundAlbedo) {
		this.backgroundAlbedo = backgroundAlbedo;
	}

	public double getBackgroundAlbedo() {
		return backgroundAlbedo;
	}

	public void setHeatVectorLength(double heatVectorLength) {
		this.heatVectorLength = heatVectorLength;
	}

	public double getHeatVectorLength() {
		return heatVectorLength;
	}

}
