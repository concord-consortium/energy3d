package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Roof;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class ChangeRoofTextureCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;

	public ChangeRoofTextureCommand() {
		oldValue = Scene.getInstance().getRoofTextureType();
	}

	public int getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = Scene.getInstance().getRoofTextureType();
		Scene.getInstance().setRoofTextureType(oldValue);
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Roof) {
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setRoofTextureType(newValue);
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Roof) {
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Roof Texture Change";
	}

}
