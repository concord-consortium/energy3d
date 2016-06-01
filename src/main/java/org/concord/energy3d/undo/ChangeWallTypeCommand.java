package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.Scene;

public class ChangeWallTypeCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private int oldValue, newValue;
	private Wall wall;

	public ChangeWallTypeCommand(Wall wall) {
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
		Scene.getInstance().redrawAll();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		wall.setType(newValue);
		Scene.getInstance().redrawAll();
	}

	@Override
	public String getPresentationName() {
		return "Type Change of Wall";
	}

}
