package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Mirror;

public class ChangeMirrorTargetCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private Foundation oldValue, newValue;
	private Mirror mirror;

	public ChangeMirrorTargetCommand(Mirror mirror) {
		this.mirror = mirror;
		oldValue = mirror.getTarget();
	}

	public Mirror getMirror() {
		return mirror;
	}

	public Foundation getOldValue() {
		return oldValue;
	}

	public Foundation getNewValue() {
		newValue = mirror.getTarget();
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = mirror.getTarget();
		mirror.setTarget(oldValue);
		mirror.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		mirror.setTarget(newValue);
		mirror.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Target";
	}

}
