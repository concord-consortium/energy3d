package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.util.Config;

public class UndoManager extends javax.swing.undo.UndoManager {
	
	private static final long serialVersionUID = 1L;

	private boolean undoFlag = false;
	private boolean redoFlag = false;
	private boolean saveFlag = false;
	private boolean changeGraphTabFlag = false;
	private boolean changeThermostatFlag = false;

	@Override
	public synchronized boolean addEdit(final UndoableEdit anEdit) {
		final boolean result = super.addEdit(anEdit);
		saveFlag = anEdit instanceof SaveCommand;
		Scene.getInstance().setEdited(!saveFlag);
		refreshUndoRedoGui();
		changeGraphTabFlag = anEdit instanceof ChangeGraphTabCommand;
		changeThermostatFlag = anEdit instanceof ChangeThermostatCommand;
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
		undoFlag = true;
		redoFlag = false;
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
		undoFlag = false;
		redoFlag = true;
	}

	@Override
	public void die() {
		super.die();
		refreshUndoRedoGui();
		undoFlag = false;
		redoFlag = false;
		saveFlag = false;
		changeGraphTabFlag = false;
		changeThermostatFlag = false;
	}

	private void refreshUndoRedoGui() {
		if (!Config.isApplet())
			MainFrame.getInstance().refreshUndoRedo();
	}

	public void setUndoFlag(boolean undoFlag) {
		this.undoFlag = undoFlag;
	}

	public boolean getUndoFlag() {
		return undoFlag;
	}

	public void setRedoFlag(boolean redoFlag) {
		this.redoFlag = redoFlag;
	}

	public boolean getRedoFlag() {
		return redoFlag;
	}

	public void setSaveFlag(boolean saveFlag) {
		this.saveFlag = saveFlag;
	}

	public boolean getSaveFlag() {
		return saveFlag;
	}

	public void setChangeGraphTabFlag(boolean changeGraphTabFlag) {
		this.changeGraphTabFlag = changeGraphTabFlag;
	}

	public boolean getChangeGraphTabFlag() {
		return changeGraphTabFlag;
	}

	public void setChangeThermostatFlag(boolean changeThermostatFlag) {
		this.changeThermostatFlag = changeThermostatFlag;
	}

	public boolean getChangeThermostatFlag() {
		return changeThermostatFlag;
	}

	@Override
	public UndoableEdit lastEdit() {
		return super.lastEdit();
	}

}
