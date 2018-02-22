package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;

public class LockEditPointsForClassCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean[] oldValues;
	private boolean[] newValues;
	private final List<HousePart> parts;

	public LockEditPointsForClassCommand(final HousePart x) {
		parts = Scene.getInstance().getAllPartsOfSameType(x);
		final int n = parts.size();
		oldValues = new boolean[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = parts.get(i).getLockEdit();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = parts.size();
		newValues = new boolean[n];
		for (int i = 0; i < n; i++) {
			final HousePart p = parts.get(i);
			newValues[i] = p.getLockEdit();
			p.setLockEdit(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = parts.size();
		for (int i = 0; i < n; i++) {
			parts.get(i).setLockEdit(newValues[i]);
		}
	}

	@Override
	public String getPresentationName() {
		return "Lock Edit Points for Class";
	}

}
