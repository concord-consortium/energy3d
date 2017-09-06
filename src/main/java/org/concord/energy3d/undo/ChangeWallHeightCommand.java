package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Wall;

public class ChangeWallHeightCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldValue;
	private double newValue;
	private final Wall wall;

	public ChangeWallHeightCommand(final Wall wall) {
		this.wall = wall;
		oldValue = wall.getHeight();
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
		newValue = wall.getHeight();
		wall.setHeight(oldValue, true);
		wall.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		wall.setHeight(newValue, true);
		wall.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Height for Selected Wall";
	}

}
