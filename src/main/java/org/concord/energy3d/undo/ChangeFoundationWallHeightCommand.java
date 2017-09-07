package org.concord.energy3d.undo;

import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.Scene;

public class ChangeFoundationWallHeightCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final Foundation foundation;
	private final List<Wall> walls;

	public ChangeFoundationWallHeightCommand(final Foundation foundation) {
		this.foundation = foundation;
		walls = new ArrayList<Wall>();
		for (final HousePart p : foundation.getChildren()) {
			if (p instanceof Wall) {
				walls.add((Wall) p);
			}
		}
		final int n = walls.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = walls.get(i).getHeight();
		}
	}

	public Foundation getFoundation() {
		return foundation;
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
			newValues[i] = walls.get(i).getHeight();
			walls.get(i).setHeight(oldValues[i], true);
		}
		Scene.getInstance().redrawAll();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = walls.size();
		for (int i = 0; i < n; i++) {
			walls.get(i).setHeight(newValues[i], true);
		}
		Scene.getInstance().redrawAll();
	}

	@Override
	public String getPresentationName() {
		return "Change Height for All Walls on Selected Foundation";
	}

}
