package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Mirror;

public class ChangeMirrorReflectivityCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double oldValue, newValue;
	private Mirror mirror;

	public ChangeMirrorReflectivityCommand(Mirror mirror) {
		this.mirror = mirror;
		oldValue = mirror.getReflectivity();
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
		newValue = mirror.getReflectivity();
		mirror.setReflectivity(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		mirror.setReflectivity(newValue);
	}

	@Override
	public String getPresentationName() {
		return "Reflectivity Change for Selected Mirror";
	}

}
