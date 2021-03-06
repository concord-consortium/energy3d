package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.FresnelReflector;

public class ChangeFresnelReflectorAbsorberCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final Foundation oldValue;
	private Foundation newValue;
	private final FresnelReflector reflector;

	public ChangeFresnelReflectorAbsorberCommand(final FresnelReflector reflector) {
		this.reflector = reflector;
		oldValue = reflector.getReceiver();
	}

	public FresnelReflector getFresnelReflector() {
		return reflector;
	}

	public Foundation getOldValue() {
		return oldValue;
	}

	public Foundation getNewValue() {
		newValue = reflector.getReceiver();
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = reflector.getReceiver();
		reflector.setReceiver(oldValue);
		reflector.draw();
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
		reflector.setReceiver(newValue);
		reflector.draw();
		if (oldValue != null) {
			oldValue.drawSolarReceiver();
		}
		if (newValue != null) {
			newValue.drawSolarReceiver();
		}
	}

	@Override
	public String getPresentationName() {
		return "Change Absorber";
	}

}
