package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.Config;

public class UndoManager extends javax.swing.undo.UndoManager {
	private static final long serialVersionUID = 1L;

	@Override
	public synchronized boolean addEdit(final UndoableEdit anEdit) {
		final boolean result = super.addEdit(anEdit);
		Scene.getInstance().setEdited(true);
		refreshUndoRedoGui();
		return result;
	}

	@Override
	public synchronized void undo() throws CannotUndoException {
		super.undo();
		SaveCommand.setGloabalSignificant(true);
		if (editToBeUndone() instanceof SaveCommand)
			Scene.getInstance().setEdited(false);
		else
			Scene.getInstance().setEdited(true);
		SaveCommand.setGloabalSignificant(false);
		refreshUndoRedoGui();
	}

	@Override
	public synchronized void redo() throws CannotRedoException {
		super.redo();
		SaveCommand.setGloabalSignificant(true);
		if (editToBeUndone() instanceof SaveCommand)
			Scene.getInstance().setEdited(false);
		else
			Scene.getInstance().setEdited(true);
		SaveCommand.setGloabalSignificant(false);
		refreshUndoRedoGui();
	}

	@Override
	public void die() {
		super.die();
		refreshUndoRedoGui();
	}

	private void refreshUndoRedoGui() {
		if (!Config.isApplet())
			MainFrame.getInstance().refreshUndoRedo();
	}
}
