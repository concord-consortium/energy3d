package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.Scene;

public class ChangeThicknessForAllWallsCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final List<HousePart> walls;

	public ChangeThicknessForAllWallsCommand(final Wall w) {
		walls = Scene.getInstance().getAllPartsOfSameType(w);
		final int n = walls.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = ((Wall) walls.get(i)).getThickness();
		}
	}

	public List<HousePart> getWalls() {
		return walls;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = walls.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			final Wall w = (Wall) walls.get(i);
			newValues[i] = w.getThickness();
			w.setThickness(oldValues[i]);
			w.draw();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = walls.size();
		for (int i = 0; i < n; i++) {
			final Wall w = (Wall) walls.get(i);
			w.setThickness(newValues[i]);
			w.draw();
		}
	}

	@Override
	public String getPresentationName() {
		return "Change Thickness for All Walls";
	}

}
