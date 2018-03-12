package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;

public class LockEditPointsForAllCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean[] oldValues;
	private boolean[] newValues;

	public LockEditPointsForAllCommand() {
		final int n = Scene.getInstance().getParts().size();
		oldValues = new boolean[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = Scene.getInstance().getParts().get(i).getLockEdit();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = Scene.getInstance().getParts().size();
		newValues = new boolean[n];
		for (int i = 0; i < n; i++) {
			final HousePart p = Scene.getInstance().getParts().get(i);
			newValues[i] = p.getLockEdit();
			p.setLockEdit(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = Scene.getInstance().getParts().size();
		for (int i = 0; i < n; i++) {
			Scene.getInstance().getParts().get(i).setLockEdit(newValues[i]);
		}
	}

	@Override
	public String getPresentationName() {
		return "Lock All Edit Points";
	}

}
