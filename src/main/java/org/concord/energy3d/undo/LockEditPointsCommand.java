package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;

public class LockEditPointsCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final boolean oldValue;
	private boolean newValue;
	private final HousePart part;

	public LockEditPointsCommand(final HousePart part) {
		this.part = part;
		oldValue = part.getLockEdit();
	}

	public HousePart getPart() {
		return part;
	}

	public boolean getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = part.getLockEdit();
		part.setLockEdit(oldValue);
		part.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		part.setLockEdit(newValue);
		part.draw();
	}

	@Override
	public String getPresentationName() {
		return "Lock Edit Points";
	}

}
