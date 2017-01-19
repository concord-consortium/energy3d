package org.concord.energy3d.util;

import java.util.List;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.PickedHousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.UserData;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.model.Window;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

import com.ardor3d.intersection.PickData;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;

public class SelectUtil {
	private static final PickResults pickResults = new PrimitivePickResults();
	private static int pickLayer = -1;
	private static ColorRGBA currentEditPointOriginalColor = new ColorRGBA();
	private static Mesh currentEditPointMesh;

	static {
		pickResults.setCheckDistance(true);
	}

	public static PickedHousePart pickPart(final int x, final int y) {
		pickResults.clear();
		final Ray3 pickRay = SceneManager.getInstance().getCamera().getPickRay(new Vector2(x, y), false, null);
		for (final HousePart housePart : Scene.getInstance().getParts()) {
			if (!housePart.isFrozen()) {
				PickingUtil.findPick(housePart.getCollisionSpatial(), pickRay, pickResults, false);
				PickingUtil.findPick(housePart.getEditPointsRoot(), pickRay, pickResults, false);
				if (housePart instanceof Foundation) {
					final Foundation foundation = (Foundation) housePart;
					PickingUtil.findPick(foundation.getPolygon().getEditPointsRoot(), pickRay, pickResults, false);
					final List<Node> importedNodes = foundation.getImportedNodes();
					if (importedNodes != null) {
						for (final Node node : importedNodes) {
							for (final Spatial s : node.getChildren()) {
								PickingUtil.findPick(s, pickRay, pickResults, false);
							}
						}
					}
				}
			}
		}
		return getPickResult(pickRay);
	}

	public static PickedHousePart pickPart(final int x, final int y, final Mesh mesh) {
		pickResults.clear();
		final Ray3 pickRay = SceneManager.getInstance().getCamera().getPickRay(new Vector2(x, y), false, null);
		PickingUtil.findPick(mesh, pickRay, pickResults, false);
		return getPickResult(pickRay);
	}

	public static PickedHousePart pickPart(final int x, final int y, final HousePart housePart) {
		pickResults.clear();
		final Ray3 pickRay = SceneManager.getInstance().getCamera().getPickRay(new Vector2(x, y), false, null);
		if (housePart == null) {
			PickingUtil.findPick(SceneManager.getInstance().getLand(), pickRay, pickResults, false);
		} else {
			PickingUtil.findPick(housePart.getCollisionSpatial(), pickRay, pickResults, false);
			PickingUtil.findPick(housePart.getEditPointsRoot(), pickRay, pickResults, false);
		}
		return getPickResult(pickRay);
	}

	public static PickedHousePart pickPart(final int x, final int y, final Class<?>[] typesOfHousePart) {
		pickResults.clear();
		final Ray3 pickRay = SceneManager.getInstance().getCamera().getPickRay(new Vector2(x, y), false, null);
		for (final Class<?> typeOfHousePart : typesOfHousePart) {
			if (typeOfHousePart == null) {
				PickingUtil.findPick(SceneManager.getInstance().getLand(), pickRay, pickResults, false);
			} else {
				for (final HousePart housePart : Scene.getInstance().getParts()) {
					if (!housePart.isFrozen() && typeOfHousePart.isInstance(housePart)) {
						PickingUtil.findPick(housePart.getCollisionSpatial(), pickRay, pickResults, false);
						PickingUtil.findPick(housePart.getEditPointsRoot(), pickRay, pickResults, false);
					}
				}
			}
		}
		return getPickResult(pickRay);
	}

	private static PickedHousePart getPickResult(final Ray3 pickRay) {
		PickedHousePart pickedHousePart = null;
		double polyDist = Double.MAX_VALUE;
		double pointDist = Double.MAX_VALUE;
		int objCounter = 0;
		HousePart prevHousePart = null;
		final long pickLayer = SelectUtil.pickLayer == -1 ? -1 : SelectUtil.pickLayer % Math.max(1, pickResults.getNumber());
		for (int i = 0; i < pickResults.getNumber(); i++) {
			final PickData pick = pickResults.getPickData(i);
			if (pick.getIntersectionRecord().getNumberOfIntersections() == 0) {
				continue;
			}
			final Object obj = ((Mesh) pick.getTarget()).getUserData();
			UserData userData = null;
			if (obj instanceof UserData) {
				userData = (UserData) obj;
				if (userData.getHousePart() != prevHousePart) {
					objCounter++;
					prevHousePart = userData.getHousePart();
				}
			} else if (pickLayer != -1) {
				continue;
			}
			if (pickLayer != -1 && objCounter - 1 != pickLayer) {
				continue;
			}
			final Vector3 intersectionPoint = pick.getIntersectionRecord().getIntersectionPoint(0);
			final PickedHousePart picked_i = new PickedHousePart(userData, intersectionPoint, pick.getIntersectionRecord().getIntersectionNormal(0));
			double polyDist_i = pick.getIntersectionRecord().getClosestDistance();
			if (userData != null && userData.getHousePart() instanceof Window) {
				polyDist_i -= 0.2; // give more priority to window (specially skylight)
			}
			double pointDist_i = Double.MAX_VALUE;
			if (userData != null && polyDist_i - polyDist < 0.1) {
				for (int j = 0; j < userData.getHousePart().getPoints().size(); j++) {
					final Vector3 p = userData.getHousePart().getAbsPoint(j);
					pointDist_i = p.distance(intersectionPoint);
					double adjust = 0;
					if (userData.getHousePart().isFirstPointInserted()) { // to avoid IndexOutOfBoundsException: Index: 2, Size: 2
						adjust -= Math.abs(userData.getHousePart().getNormal().negate(null).dot(pickRay.getDirection()) / 10.0);
					}
					if (userData.getHousePart() == SceneManager.getInstance().getSelectedPart()) {
						adjust -= 0.1; // give more priority because the object is selected
					}
					if (userData.isEditPoint()) {
						adjust -= 0.1; // give more priority because this is an edit point
					}
					if (userData.isEditPoint() && userData.getHousePart() instanceof Foundation && ((Foundation) userData.getHousePart()).isResizeHouseMode()) {
						adjust -= 0.1;
					}
					pointDist_i += adjust;
					if (pointDist_i < pointDist && (userData.getIndex() != -1 || pickedHousePart == null || pickedHousePart.getUserData() == null || pickedHousePart.getUserData().getIndex() == -1)) {
						pickedHousePart = picked_i;
						polyDist = polyDist_i;
						pointDist = pointDist_i;
					}
				}
			}
			if (pickedHousePart == null || polyDist_i < polyDist) {
				pickedHousePart = picked_i;
				polyDist = polyDist_i;
				pointDist = pointDist_i;
			}
		}
		return pickedHousePart;
	}

	public static PickedHousePart selectHousePart(final int x, final int y, final boolean edit) {
		final PickedHousePart pickedHousePart = pickPart(x, y);
		UserData data = null;
		if (pickedHousePart != null) {
			data = pickedHousePart.getUserData();
		}

		// set the color of edit point that the mouse currently hovers on to red
		if (data == null || !data.isEditPoint() || currentEditPointMesh != data.getHousePart().getEditPointShape(data.getIndex())) {
			if (currentEditPointMesh != null) {
				currentEditPointMesh.setDefaultColor(currentEditPointOriginalColor);
				currentEditPointMesh = null;
			}
		}
		if (data != null && data.isEditPoint() && currentEditPointMesh != data.getHousePart().getEditPointShape(data.getIndex())) {
			currentEditPointMesh = data.getHousePart().getEditPointShape(data.getIndex());
			currentEditPointOriginalColor.set(currentEditPointMesh.getDefaultColor());
			currentEditPointMesh.setDefaultColor(ColorRGBA.RED);
		}

		if (data == null) {
			Blinker.getInstance().setTarget(null);
		} else if (edit && data.isEditPoint()) {
			int pointIndex = data.getIndex();
			if (SceneManager.getInstance().isTopView() && data.getHousePart() instanceof Wall) {
				pointIndex -= 1;
			}
			data.getHousePart().setEditPoint(pointIndex);
		} else {
			if (data.getHousePart().getOriginal() == null) {
				Blinker.getInstance().setTarget(null);
			} else if (data.getHousePart() instanceof Roof) {
				Blinker.getInstance().setTarget(((Roof) data.getHousePart().getOriginal()).getRoofPartsRoot().getChild(data.getIndex() - 0));
			} else {
				Blinker.getInstance().setTarget(data.getHousePart().getOriginal().getRoot());
			}
		}
		return pickedHousePart;
	}

	public static void nextPickLayer() {
		if (pickLayer != -1) {
			pickLayer++;
		}
		System.out.println("\tpickLayer = " + pickLayer);
	}

	public static void setPickLayer(final int i) {
		pickLayer = i;
	}

	public static Mesh getCurrentEditPointMesh() {
		return currentEditPointMesh;
	}

	public static void setCurrentEditPointMesh(final Mesh currentEditPointMesh) {
		SelectUtil.currentEditPointMesh = currentEditPointMesh;
	}
}