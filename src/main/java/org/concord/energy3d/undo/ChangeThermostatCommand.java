package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class ChangeThermostatCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;

	public ChangeThermostatCommand() {
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
	}

	@Override
	public String getPresentationName() {
		return "Adjust Thermostat";
	}

	@Override
	public boolean isSignificant() {
		return false;
	}

}
