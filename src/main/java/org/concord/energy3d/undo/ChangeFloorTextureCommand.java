package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Floor;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class ChangeFloorTextureCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;

	public ChangeFloorTextureCommand() {
		oldValue = Scene.getInstance().getFloorTextureType();
	}

	public int getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = Scene.getInstance().getFloorTextureType();
		Scene.getInstance().setFloorTextureType(oldValue);
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Floor) {
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setFloorTextureType(newValue);
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Floor) {
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Floor Texture Change";
	}

}
