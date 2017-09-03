package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.Scene;

public class ChangeThemeCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;

	public ChangeThemeCommand() {
		oldValue = Scene.getInstance().getTheme();
	}

	public int getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = Scene.getInstance().getTheme();
		Scene.getInstance().setTheme(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().setTheme(newValue);
	}

	@Override
	public String getPresentationName() {
		return "Theme Change";
	}

}
