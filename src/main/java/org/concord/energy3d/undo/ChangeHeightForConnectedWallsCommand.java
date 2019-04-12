package org.concord.energy3d.undo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.gui.EnergyPanel;
import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Mirror;
import org.concord.energy3d.model.Snap;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;
import org.concord.energy3d.model.WallVisitor;

public class ChangeHeightForConnectedWallsCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final List<Wall> walls;

	public ChangeHeightForConnectedWallsCommand(final Wall w) {
		walls = new ArrayList<Wall>();
		w.visitNeighbors(new WallVisitor() {
			@Override
			public void visit(final Wall currentWall, final Snap prev, final Snap next) {
				walls.add(currentWall);
			}
		});
		final int n = walls.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = walls.get(i).getHeight();
		}
	}

	public List<Wall> getWalls() {
		return walls;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = walls.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			final Wall w = walls.get(i);
			newValues[i] = w.getHeight();
			w.setHeight(oldValues[i], true);
		}
		Scene.getInstance().redrawAllWallsNow();
		updateLinkedObjects();
		if (SceneManager.getInstance().getSolarHeatMap()) {
			EnergyPanel.getInstance().updateRadiationHeatMap();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = walls.size();
		for (int i = 0; i < n; i++) {
			walls.get(i).setHeight(newValues[i], true);
		}
		Scene.getInstance().redrawAllWallsNow();
		updateLinkedObjects();
		if (SceneManager.getInstance().getSolarHeatMap()) {
			EnergyPanel.getInstance().updateRadiationHeatMap();
		}
	}

	private void updateLinkedObjects() {
		if (walls.isEmpty()) {
			return;
		}
		final Foundation foundation = walls.get(0).getTopContainer();
		if (foundation.hasSolarReceiver()) {
			foundation.drawSolarReceiver();
			for (final HousePart x : Scene.getInstance().getParts()) {
				if (x instanceof FresnelReflector) {
					final FresnelReflector reflector = (FresnelReflector) x;
					if (foundation == reflector.getReceiver() && reflector.isSunBeamVisible()) {
						reflector.drawSunBeam();
					}
				} else if (x instanceof Mirror) {
					final Mirror heliostat = (Mirror) x;
					if (foundation == heliostat.getReceiver() && heliostat.isSunBeamVisible()) {
						heliostat.drawSunBeam();
					}
				}
			}
		}
	}

	@Override
	public String getPresentationName() {
		return "Change Height for Connected Walls";
	}

}
