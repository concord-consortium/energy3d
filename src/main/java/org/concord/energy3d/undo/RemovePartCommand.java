package org.concord.energy3d.undo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class RemovePartCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final HousePart housePart;
	private List<Map<Integer, List<Wall>>> gableInfo; // there may be multiple roofs on a foundation, which is why we need to have a list of maps

	public RemovePartCommand(final HousePart housePart) {
		this.housePart = housePart;
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
		return housePart;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		Scene.getInstance().add(housePart, true);
		if (housePart instanceof Wall) {
			final Roof roof = ((Wall) housePart).getRoof();
			if (roof != null && gableInfo.size() == 1) {
				roof.setGableEditPointToWallMap(gableInfo.get(0));
			}
		} else if (housePart instanceof Foundation) {
			final List<Roof> roofs = ((Foundation) housePart).getRoofs();
			if (!roofs.isEmpty() && !gableInfo.isEmpty()) {
				for (int i = 0; i < roofs.size(); i++) {
					roofs.get(i).setGableEditPointToWallMap(gableInfo.get(i));
				}
			}
		}
		SceneManager.getInstance().setSelectedPart(housePart);
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().remove(housePart, true);
		EnergyPanel.getInstance().clearRadiationHeatMap();
	}

	@Override
	public String getPresentationName() {
		if (housePart instanceof Foundation && !housePart.getChildren().isEmpty()) {
			return "Remove Building";
		}
		return "Remove " + housePart.getClass().getSimpleName();
	}

}
