package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Mirror;

public class ChangeHeliostatCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private int oldValue, newValue;
	private Mirror mirror;

	public ChangeHeliostatCommand(Mirror mirror) {
		this.mirror = mirror;
		oldValue = mirror.getHeliostatType();
	}

	public Mirror getMirror() {
		return mirror;
	}

	public double getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = mirror.getHeliostatType();
		mirror.setHeliostatType(oldValue);
		mirror.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		mirror.setHeliostatType(newValue);
		mirror.draw();
	}

	@Override
	public String getPresentationName() {
		return "Set Heliostat";
	}

}
