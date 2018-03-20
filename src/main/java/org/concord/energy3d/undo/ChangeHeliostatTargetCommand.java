package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.Mirror;

public class ChangeHeliostatTargetCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final Foundation oldValue;
	private Foundation newValue;
	private final Mirror mirror;

	public ChangeHeliostatTargetCommand(final Mirror mirror) {
		this.mirror = mirror;
		oldValue = mirror.getReceiver();
	}

	public Mirror getMirror() {
		return mirror;
	}

	public Foundation getOldValue() {
		return oldValue;
	}

	public Foundation getNewValue() {
		newValue = mirror.getReceiver();
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = mirror.getReceiver();
		mirror.setReceiver(oldValue);
		mirror.draw();
		if (oldValue != null) {
			oldValue.drawSolarReceiver();
		}
		if (newValue != null) {
			newValue.drawSolarReceiver();
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		mirror.setReceiver(newValue);
		mirror.draw();
		if (oldValue != null) {
			oldValue.drawSolarReceiver();
		}
		if (newValue != null) {
			newValue.drawSolarReceiver();
		}
	}

	@Override
	public String getPresentationName() {
		return "Change Target";
	}

}
