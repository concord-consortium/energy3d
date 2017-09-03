package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Wall;

public class ChangeWallTypeCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;
	private final Wall wall;

	public ChangeWallTypeCommand(final Wall wall) {
		this.wall = wall;
		oldValue = wall.getType();
	}

	public Wall getWall() {
		return wall;
	}

	public int getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = wall.getType();
		wall.setType(oldValue);
		wall.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		wall.setType(newValue);
		wall.draw();
	}

	@Override
	public String getPresentationName() {
		return "Type Change of Wall";
	}

}
