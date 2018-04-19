package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Floor;
import org.concord.energy3d.scene.SceneManager;

public class ChangeFloorTextureCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;
	private final Floor floor;

	public ChangeFloorTextureCommand(final Floor floor) {
		this.floor = floor;
		oldValue = floor.getTextureType();
	}

	public int getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = floor.getTextureType();
		floor.setTextureType(oldValue);
		floor.draw();
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		floor.setTextureType(newValue);
		floor.draw();
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Floor Texture Change";
	}

}
