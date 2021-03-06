package org.concord.energy3d.util;

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
import com.ardor3d.intersection.Pickable;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Mesh;

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
            PickingUtil.findPick(housePart.getCollisionSpatial(), pickRay, pickResults, false);
            PickingUtil.findPick(housePart.getEditPointsRoot(), pickRay, pickResults, false);
            if (housePart instanceof Foundation) {
                final Foundation foundation = (Foundation) housePart;
                if (foundation.getPolygon().isVisible()) {
                    PickingUtil.findPick(foundation.getPolygon().getEditPointsRoot(), pickRay, pickResults, false);
                }
            }
        }
        return getPickResult(pickRay);
    }

    public static int getPickResultsNumber() {
        return pickResults.getNumber();
    }

    public static PickedHousePart pickPart(final int x, final int y, final Mesh mesh) {
        pickResults.clear();
        final Ray3 pickRay = SceneManager.getInstance().getCamera().getPickRay(new Vector2(x, y), false, null);
        PickingUtil.findPick(mesh, pickRay, pickResults, false);
        final PickedHousePart picked = getPickResultForImportedMesh();
        if (picked != null) {
            return picked;
        }
        return getPickResult(pickRay);
    }

    public static PickedHousePart pickPart(final int x, final int y, final HousePart housePart) {
        pickResults.clear();
        final Ray3 pickRay = SceneManager.getInstance().getCamera().getPickRay(new Vector2(x, y), false, null);
        if (housePart == null) {
            PickingUtil.findPick(SceneManager.getInstance().getLand(), pickRay, pickResults, false);
        } else {
            PickingUtil.findPick(housePart.getCollisionSpatial(), pickRay, pickResults, false);
        }
        final PickedHousePart picked = getPickResultForImportedMesh();
        if (picked != null) {
            return picked;
        }
        return getPickResult(pickRay);
    }

    public static PickedHousePart pickPart(final int x, final int y, final Class<?>[] typesOfHousePart, boolean allowPickOnLockedPart) {
        pickResults.clear();
        final Ray3 pickRay = SceneManager.getInstance().getCamera().getPickRay(new Vector2(x, y), false, null);
        for (final Class<?> typeOfHousePart : typesOfHousePart) {
            if (typeOfHousePart == null) {
                PickingUtil.findPick(SceneManager.getInstance().getLand(), pickRay, pickResults, false);
            } else {
                if (allowPickOnLockedPart) {
                    for (final HousePart part : Scene.getInstance().getParts()) {
                        if (typeOfHousePart.isInstance(part)) {
                            PickingUtil.findPick(part.getCollisionSpatial(), pickRay, pickResults, false);
                        }
                    }
                } else {
                    for (final HousePart part : Scene.getInstance().getParts()) {
                        if (!part.getLockEdit() && typeOfHousePart.isInstance(part)) {
                            PickingUtil.findPick(part.getCollisionSpatial(), pickRay, pickResults, false);
                        }
                    }
                }
            }
        }
        final PickedHousePart picked = getPickResultForImportedMesh();
        if (picked != null) {
            return picked;
        }
        return getPickResult(pickRay);
    }

    // if this is an imported mesh, do it here. getPickResult method below returns incorrect result.
    private static PickedHousePart getPickResultForImportedMesh() {
        if (pickResults.getNumber() > 0) {
            final PickData pick = pickResults.getPickData(0);
            final Pickable pickable = pick.getTarget();
            if (pickable instanceof Mesh) {
                final Mesh m = (Mesh) pickable;
                final UserData u = (UserData) m.getUserData();
                // the user data of land can be null
                if (u != null && u.isImported()) {
                    return new PickedHousePart(u, pick.getIntersectionRecord().getIntersectionPoint(0), u.getRotatedNormal() == null ? u.getNormal() : u.getRotatedNormal());
                }
            }
        }
        return null;
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
            if (obj instanceof UserData) { // FIXME: Note that userData can be null if the pick is the land
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
                polyDist_i -= 0.2; // give more priority to window (especially skylight)
            }
            double pointDist_i = Double.MAX_VALUE;
            if (userData != null && polyDist_i - polyDist < 0.1) {
                for (int j = 0; j < userData.getHousePart().getPoints().size(); j++) {
                    final Vector3 p = userData.getHousePart().getAbsPoint(j);
                    pointDist_i = p.distance(intersectionPoint);
                    double adjust = 0;
                    if (userData.getHousePart().isFirstPointInserted()) { // to avoid IndexOutOfBoundsException: Index: 2, Size: 2
                        if (userData.getHousePart().getNormal() != null) {
                            adjust -= Math.abs(userData.getHousePart().getNormal().negate(null).dot(pickRay.getDirection()) / 10.0);
                        }
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
                    if (pointDist_i < pointDist && (userData.getEditPointIndex() != -1 || pickedHousePart == null || pickedHousePart.getUserData() == null || pickedHousePart.getUserData().getEditPointIndex() == -1)) {
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
        if (data == null || !data.isEditPoint() || currentEditPointMesh != data.getHousePart().getEditPointShape(data.getEditPointIndex())) {
            if (currentEditPointMesh != null) {
                currentEditPointMesh.setDefaultColor(currentEditPointOriginalColor);
                currentEditPointMesh = null;
            }
        }
        if (data != null && data.isEditPoint() && currentEditPointMesh != data.getHousePart().getEditPointShape(data.getEditPointIndex())) {
            final Foundation foundation = data.getHousePart() instanceof Foundation ? (Foundation) data.getHousePart() : data.getHousePart().getTopContainer();
            if (foundation != null && !foundation.getLockEdit()) {
                currentEditPointMesh = data.getHousePart().getEditPointShape(data.getEditPointIndex());
                currentEditPointOriginalColor.set(currentEditPointMesh.getDefaultColor());
                currentEditPointMesh.setDefaultColor(ColorRGBA.RED);
            }
        }

        if (data == null) {
            Blinker.getInstance().setTarget(null);
        } else if (edit && data.isEditPoint()) {
            int pointIndex = data.getEditPointIndex();
            if (SceneManager.getInstance().isTopView() && data.getHousePart() instanceof Wall) {
                pointIndex -= 1;
            }
            data.getHousePart().setEditPoint(pointIndex);
        } else {
            if (data.getHousePart().getOriginal() == null) {
                Blinker.getInstance().setTarget(null);
            } else if (data.getHousePart() instanceof Roof) {
                Blinker.getInstance().setTarget(((Roof) data.getHousePart().getOriginal()).getRoofPartsRoot().getChild(data.getEditPointIndex()));
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