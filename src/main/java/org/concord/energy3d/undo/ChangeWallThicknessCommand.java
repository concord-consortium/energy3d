package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Wall;

public class ChangeWallThicknessCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double oldValue;
	private double newValue;
	private final Wall wall;

	public ChangeWallThicknessCommand(final Wall wall) {
		this.wall = wall;
		oldValue = wall.getThickness();
	}

	public Wall getWall() {
		return wall;
	}

	public double getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = wall.getThickness();
		wall.setThickness(oldValue);
		wall.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		wall.setThickness(newValue);
		wall.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Thickness for Selected Wall";
	}

}
