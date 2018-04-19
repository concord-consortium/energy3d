package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Door;
import org.concord.energy3d.scene.SceneManager;

public class ChangeDoorTextureCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;
	private final Door door;

	public ChangeDoorTextureCommand(final Door door) {
		this.door = door;
		oldValue = door.getTextureType();
	}

	public int getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = door.getTextureType();
		door.setTextureType(oldValue);
		door.draw();
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		door.setTextureType(newValue);
		door.draw();
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Door Texture Change";
	}

}
