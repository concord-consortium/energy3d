package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Mirror;

public class ChangeMirrorTargetCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final Foundation oldValue;
	private Foundation newValue;
	private final Mirror mirror;

	public ChangeMirrorTargetCommand(final Mirror mirror) {
		this.mirror = mirror;
		oldValue = mirror.getHeliostatTarget();
	}

	public Mirror getMirror() {
		return mirror;
	}

	public Foundation getOldValue() {
		return oldValue;
	}

	public Foundation getNewValue() {
		newValue = mirror.getHeliostatTarget();
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = mirror.getHeliostatTarget();
		mirror.setHeliostatTarget(oldValue);
		mirror.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		mirror.setHeliostatTarget(newValue);
		mirror.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Target";
	}

}
