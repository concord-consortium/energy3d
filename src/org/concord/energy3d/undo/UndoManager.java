package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.concord.energy3d.scene.Scene;


public class UndoManager extends javax.swing.undo.UndoManager {
	private static final long serialVersionUID = 1L;

	@Override
	public synchronized boolean addEdit(final UndoableEdit anEdit) {
		Scene.getInstance().setEdited(true);
		return super.addEdit(anEdit);
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
	}
}
