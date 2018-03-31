package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Wall;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class ChangeWallTextureCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;

	public ChangeWallTextureCommand() {
		oldValue = Scene.getInstance().getWallTextureType();
	}

	public int getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = Scene.getInstance().getWallTextureType();
		Scene.getInstance().setWallTextureType(oldValue);
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Wall) {
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setWallTextureType(newValue);
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Wall) {
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Wall Texture Change";
	}

}
