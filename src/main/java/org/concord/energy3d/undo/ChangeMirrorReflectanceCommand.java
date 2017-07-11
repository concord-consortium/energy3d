package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Mirror;

public class ChangeMirrorReflectanceCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldValue;
	private double newValue;
	private final Mirror mirror;

	public ChangeMirrorReflectanceCommand(final Mirror mirror) {
		this.mirror = mirror;
		oldValue = mirror.getReflectance();
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
		newValue = mirror.getReflectance();
		mirror.setReflectance(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		mirror.setReflectance(newValue);
	}

	@Override
	public String getPresentationName() {
		return "Reflectance Change for Selected Mirror";
	}

}
