package org.concord.energy3d.undo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Human;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Sensor;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class RemovePartCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final HousePart part;
	private List<Map<Integer, List<Wall>>> gableInfo; // there may be multiple roofs on a foundation, which is why we need to have a list of maps

	public RemovePartCommand(final HousePart housePart) {
		this.part = housePart;
	}

	public void setGableInfo(final List<Map<Integer, List<Wall>>> x) {
		gableInfo = new ArrayList<Map<Integer, List<Wall>>>();
		for (final Map<Integer, List<Wall>> m : x) {
			final Map<Integer, List<Wall>> a = new HashMap<Integer, List<Wall>>();
			for (final Map.Entry<Integer, List<Wall>> e : m.entrySet()) {
				a.put(e.getKey(), new ArrayList<Wall>(e.getValue()));
			}
			gableInfo.add(a);
		}
	}

	public HousePart getPart() {
		return part;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		Scene.getInstance().add(part, true);
		if (part instanceof Wall) {
			final Roof roof = ((Wall) part).getRoof();
			if (roof != null && gableInfo.size() == 1) {
				roof.setGableEditPointToWallMap(gableInfo.get(0));
			}
		} else if (part instanceof Foundation) {
			final List<Roof> roofs = ((Foundation) part).getRoofs();
			if (!roofs.isEmpty() && !gableInfo.isEmpty()) {
				for (int i = 0; i < roofs.size(); i++) {
					roofs.get(i).setGableEditPointToWallMap(gableInfo.get(i));
				}
			}
		}
		SceneManager.getInstance().setSelectedPart(part);
		if (SceneManager.getInstance().getSolarHeatMap()) {
			EnergyPanel.getInstance().updateRadiationHeatMap();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().remove(part, true);
		if (SceneManager.getInstance().getSolarHeatMap()) {
			EnergyPanel.getInstance().updateRadiationHeatMap();
		}
	}

	@Override
	public char getOneLetterCode() {
		if (part instanceof Floor) {
			return 'F';
		}
		if (part instanceof Human) {
			return 'H';
		}
		if (part instanceof Foundation) {
			return 'N';
		}
		if (part instanceof Sensor) {
			return 'S';
		}
		return 'P';
	}

	@Override
	public String getPresentationName() {
		if (part instanceof Foundation && !part.getChildren().isEmpty()) {
			return "Remove Building";
		}
		return "Remove " + part.getClass().getSimpleName();
	}

}
