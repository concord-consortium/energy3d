package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.scene.Scene;

public class RemoveHousePartCommand extends AbstractUndoableEdit {
	private final HousePart housePart;
	
	public RemoveHousePartCommand(final HousePart housePart) {
		this.housePart = housePart;
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
		return "remove " + housePart.getClass().getSimpleName();
	}
}
