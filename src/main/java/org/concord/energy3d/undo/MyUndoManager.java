package org.concord.energy3d.undo;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import org.concord.energy3d.agents.Agent;
import org.concord.energy3d.agents.SimpleReflexAgent;
import org.concord.energy3d.gui.MainFrame;
import org.concord.energy3d.logger.TimeSeriesLogger;
import org.concord.energy3d.scene.Scene;

public class MyUndoManager extends UndoManager {

	private static final long serialVersionUID = 1L;
	private final List<Agent> agents; // Multiple agents: https://en.wikipedia.org/wiki/Multi-agent_system

	public MyUndoManager() {
		agents = new ArrayList<Agent>();
		agents.add(new SimpleReflexAgent());
	}

	@Override
	public boolean addEdit(final UndoableEdit edit) {
		final boolean result = super.addEdit(edit);
		final boolean saveFlag = edit instanceof SaveCommand;
		Scene.getInstance().setEdited(!saveFlag);
		refreshUndoRedoGui();
		if (edit instanceof MyAbstractUndoableEdit) {
			for (final Agent a : agents) {
				a.sense((MyAbstractUndoableEdit) edit);
				a.actuate();
			}
		}
		if (saveFlag) {
			TimeSeriesLogger.getInstance().logSave();
		} else {
			TimeSeriesLogger.getInstance().logAction();
		}
		return result;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		SaveCommand.setGloabalSignificant(true);
		Scene.getInstance().setEdited(!(editToBeUndone() instanceof SaveCommand));
		SaveCommand.setGloabalSignificant(false);
		refreshUndoRedoGui();
		TimeSeriesLogger.getInstance().logUndo();
	}

	@Override
	public void redo() throws CannotRedoException {
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

	public Vector<UndoableEdit> getEdits() {
		return edits;
	}

	// override to make it public
	@Override
	public UndoableEdit lastEdit() {
		return super.lastEdit();
	}

	// override to make it public
	@Override
	public UndoableEdit editToBeUndone() {
		return super.editToBeUndone();
	}

	// override to make it public
	@Override
	public UndoableEdit editToBeRedone() {
		return super.editToBeRedone();
	}

	private void refreshUndoRedoGui() {
		MainFrame.getInstance().refreshUndoRedo();
	}

}
