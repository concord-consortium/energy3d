package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class ChangeFoundationTextureCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;

	public ChangeFoundationTextureCommand() {
		oldValue = Scene.getInstance().getFoundationTextureType();
	}

	public int getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = Scene.getInstance().getFoundationTextureType();
		Scene.getInstance().setFoundationTextureType(oldValue);
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Foundation) {
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setFoundationTextureType(newValue);
		for (final HousePart p : Scene.getInstance().getParts()) {
			if (p instanceof Foundation) {
				p.draw();
			}
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Foundation Texture Change";
	}

}
