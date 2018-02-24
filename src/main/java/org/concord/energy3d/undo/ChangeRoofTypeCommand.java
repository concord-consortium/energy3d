package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Roof;

public class ChangeRoofTypeCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;
	private final Roof roof;

	public ChangeRoofTypeCommand(final Roof roof) {
		this.roof = roof;
		oldValue = roof.getType();
	}

	public Roof getRoof() {
		return roof;
	}

	public int getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = roof.getType();
		roof.setType(oldValue);
		roof.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		roof.setType(newValue);
		roof.draw();
	}

	@Override
	public String getPresentationName() {
		return "Type Change of Roof";
	}

}
