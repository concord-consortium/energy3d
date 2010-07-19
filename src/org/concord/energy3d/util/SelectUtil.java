package org.concord.energy3d.util;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.PickedHousePart;
import org.concord.energy3d.model.UserData;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

import com.ardor3d.intersection.PickData;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.intersection.PrimitivePickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;

public class SelectUtil {
	private static final PickResults pickResults = new PrimitivePickResults();
//	private static Blinker blinker = null;
	private static Mesh floor;
	private static Node housePartsNode;	
//	protected static HousePart lastHoveredObject;	
	private static int pickLayer = -1;
	
	static {
		pickResults.setCheckDistance(true);		
	}
	
	private static void pick(int x, int y, Spatial target) {
		final Vector2 pos = new Vector2(x, y);
		final Ray3 pickRay = SceneManager.getInstance().getCanvas().getCanvasRenderer().getCamera().getPickRay(pos, false, null);
		PickingUtil.findPick(target, pickRay, pickResults);
	}

//	public PickedHousePart findMousePoint(int x, int y) {
//		return findMousePoint(x, y, floor);
//	}

	public static PickedHousePart pickPart(int x, int y, Spatial target) {
		if (target == null)
			target = floor;
		pickResults.clear();
		pick(x, y, target);

		return getPickResult();
	}

	public static PickedHousePart pickPart(int x, int y, Class<?> typeOfHousePart) {
		pickResults.clear();
		if (typeOfHousePart == null)
			pick(x, y, floor);
		else
			for (HousePart housePart : Scene.getInstance().getParts())
				if (typeOfHousePart.isInstance(housePart)) // && housePart != except)
					pick(x, y, housePart.getRoot());

		return getPickResult();
	}

	private static PickedHousePart getPickResult() {
		PickedHousePart pickedHousePart = null;
		double polyDist = Double.MAX_VALUE;
		double pointDist = Double.MAX_VALUE;
		int objCounter = 0;
		HousePart prevHousePart = null;
		for (int i = 0; i < pickResults.getNumber(); i++) {
			final PickData pick = pickResults.getPickData(i);
			if (pick.getIntersectionRecord().getNumberOfIntersections() == 0)
				continue;
//			Object obj = pick.getTargetMesh().getUserData();
			Object obj = ((Mesh)pick.getTarget()).getUserData();
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
			if (pickLayer != -1 && objCounter != pickLayer)
				continue;
			Vector3 intersectionPoint = pick.getIntersectionRecord().getIntersectionPoint(0);
			PickedHousePart picked_i = new PickedHousePart(userData, intersectionPoint);
//			double polyDist_i = pick.getClosestDistance();
			double polyDist_i = pick.getIntersectionRecord().getClosestDistance();
			double pointDist_i = Double.MAX_VALUE;
			if (userData != null && polyDist_i - polyDist < 0.1) {
				for (Vector3 p : userData.getHousePart().getPoints()) {
					pointDist_i = p.distance(intersectionPoint);
					if (userData.getHousePart() == SceneManager.getInstance().getSelectedPart())
						pointDist_i -= 0.1;
					if (pointDist_i < pointDist && 
							(userData.getPointIndex() != -1 || pickedHousePart == null || 
									pickedHousePart.getUserData().getPointIndex() == -1)) {
						pickedHousePart = picked_i;
						polyDist = polyDist_i;
						pointDist = pointDist_i;
					}
				}
			}
			if (pickedHousePart == null) {
				pickedHousePart = picked_i;
				polyDist = polyDist_i;
				pointDist = pointDist_i;
			}
		}
		return pickedHousePart;
	}

	public static HousePart selectHousePart(int x, int y, boolean edit) {
		HousePart drawn = null;
		HousePart lastHoveredObject = SceneManager.getInstance().getSelectedPart();
		PickedHousePart selectedMesh = pickPart(x, y, housePartsNode);
		UserData data = null;
		if (selectedMesh != null)
			data = selectedMesh.getUserData();

		if (data == null) {
			if (lastHoveredObject != null) {
				lastHoveredObject.hidePoints();
				lastHoveredObject = null;
//				if (blinker != null) {
//					blinker.finish();
//					blinker = null;
//				}
				Blinker.getInstance().setTarget(null);
			}
		} else if (edit && data.getPointIndex() != -1) {
			drawn = data.getHousePart();
//			SceneManager.getInstance().setSelectedPart(data.getHousePart());
			int pointIndex = data.getPointIndex();
			if (SceneManager.getInstance().isTopView() && drawn instanceof Wall)
				pointIndex -= 1;
//			SceneManager.getInstance().getSelectedPart().editPoint(pointIndex);
			data.getHousePart().editPoint(pointIndex);
		} else {
			HousePart housePart = data.getHousePart();
			drawn = housePart;
//			SceneManager.getInstance().setSelectedPart(housePart);
			if (lastHoveredObject != null && lastHoveredObject != housePart) {
				lastHoveredObject.hidePoints();
				lastHoveredObject = null;
			}

			if (lastHoveredObject != housePart) {
//				if (blinker != null) {
//					blinker.finish();
//					blinker = null;
//				}
				Blinker.getInstance().setTarget(null);
				if (data.getHousePart().getOriginal() != null)
//					blinker = new Blinker(drawn.getOriginal().getRoot());
					Blinker.getInstance().setTarget(drawn.getOriginal().getRoot());
			}
			housePart.showPoints();
			lastHoveredObject = housePart;
		}
		return drawn;
	}

	public static void init(Mesh floor, Node housePartsNode) {
		SelectUtil.floor = floor;
		SelectUtil.housePartsNode = housePartsNode;
	}

	public static void nextPickLayer() {
		if (pickLayer != -1)
			pickLayer = (pickLayer + 1) % Math.max(1, pickResults.getNumber() / 2);		
	}

	public static void setPickLayer(int i) {
		pickLayer = i;
	}
}

//class Blinker extends Thread {
//	Node target;
//
//	public Blinker(Node target) {
//		this.target = target;
//		this.start();
//	}
//
//	public void run() {
//		while (target != null) {
//			// if (target != null) {
//			CullHint cullHint = target.getSceneHints().getCullHint();
//			target.getSceneHints().setCullHint(cullHint == CullHint.Always ? cullHint = CullHint.Inherit : CullHint.Always);
//			// }
//			try {
//				sleep(300);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
//	public void finish() {
//		target.getSceneHints().setCullHint(CullHint.Inherit);
//		this.target = null;
//	}
//}

