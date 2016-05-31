package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.scene.Scene;

public class UndoManager extends javax.swing.undo.UndoManager {

	private static final long serialVersionUID = 1L;

	@Override
	public synchronized boolean addEdit(final UndoableEdit anEdit) {
		final boolean result = super.addEdit(anEdit);
		boolean saveFlag = anEdit instanceof SaveCommand;
		Scene.getInstance().setEdited(!saveFlag);
		refreshUndoRedoGui();
		if (saveFlag) {
			TimeSeriesLogger.getInstance().logSave();
		} else {
			TimeSeriesLogger.getInstance().logAction();
		}
		return result;
	}

	@Override
	public synchronized void undo() throws CannotUndoException {
		super.undo();
		SaveCommand.setGloabalSignificant(true);
		Scene.getInstance().setEdited(!(editToBeUndone() instanceof SaveCommand));
		SaveCommand.setGloabalSignificant(false);
		refreshUndoRedoGui();
		TimeSeriesLogger.getInstance().logUndo();
	}

	@Override
	public synchronized void redo() throws CannotRedoException {
		super.redo();
		SaveCommand.setGloabalSignificant(true);
		Scene.getInstance().setEdited(!(editToBeUndone() instanceof SaveCommand));
		SaveCommand.setGloabalSignificant(false);
		refreshUndoRedoGui();
		TimeSeriesLogger.getInstance().logRedo();
	}

	@Override
	public void die() {
		super.die();
		refreshUndoRedoGui();
	}

	// override to make it public
	@Override
	public UndoableEdit lastEdit() {
		return super.lastEdit();
	}

	private void refreshUndoRedoGui() {
		MainFrame.getInstance().refreshUndoRedo();
	}

}
