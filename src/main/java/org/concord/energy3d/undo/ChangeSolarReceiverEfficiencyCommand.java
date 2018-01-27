package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;

public class ChangeSolarReceiverEfficiencyCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldValue;
	private double newValue;
	private final Foundation receiver;

	public ChangeSolarReceiverEfficiencyCommand(final Foundation receiver) {
		this.receiver = receiver;
		oldValue = receiver.getSolarReceiverEfficiency();
	}

	public Foundation getSolarReceiver() {
		return receiver;
	}

	public double getOldValue() {
		return oldValue;
	}

	public double getNewValue() {
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = receiver.getSolarReceiverEfficiency();
		receiver.setSolarReceiverEfficiency(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		receiver.setSolarReceiverEfficiency(newValue);
	}

	@Override
	public String getPresentationName() {
		return "Solar Receiver Efficiency Change for Selected " + receiver.getClass().getSimpleName();
	}

}
