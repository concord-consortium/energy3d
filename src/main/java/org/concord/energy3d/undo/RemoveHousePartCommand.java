package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;

@SuppressWarnings("serial")
public class RemoveHousePartCommand extends AbstractUndoableEdit {
	private final HousePart housePart;
	private final boolean isSignificant;

	public RemoveHousePartCommand(final HousePart housePart) {
		this(housePart, true);
	}

	public RemoveHousePartCommand(final HousePart housePart, final boolean isSignificant) {
		this.housePart = housePart;
		this.isSignificant  = isSignificant;
	}

	@Override
	public boolean isSignificant() {
		return isSignificant;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		Scene.getInstance().add(housePart);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().remove(housePart);
	}

	@Override
	public String getPresentationName() {
		return "Remove " + housePart.getClass().getSimpleName();
	}
}