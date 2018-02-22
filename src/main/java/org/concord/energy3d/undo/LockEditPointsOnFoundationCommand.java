package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.HousePart;

public class LockEditPointsOnFoundationCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean[] oldValues;
	private boolean[] newValues;
	private final Foundation foundation;
	private final List<HousePart> parts;

	public LockEditPointsOnFoundationCommand(final Foundation foundation, final Class<?> c) {
		this.foundation = foundation;
		parts = foundation.getChildrenOfClass(c);
		final int n = parts.size();
		oldValues = new boolean[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = parts.get(i).getLockEdit();
		}
	}

	public Foundation getFoundation() {
		return foundation;
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
		return "Lock Edit Points of Class on Selected Foundation";
	}

}
