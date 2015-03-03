package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;

public class ChangePartUFactorCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double orgUFactor, newUFactor;
	private HousePart selectedPart;

	public ChangePartUFactorCommand(HousePart selectedPart) {
		this.selectedPart = selectedPart;
		orgUFactor = selectedPart.getUFactor();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newUFactor = selectedPart.getUFactor();
		selectedPart.setUFactor(orgUFactor);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		selectedPart.setUFactor(newUFactor);
	}

	// for action logging
	public HousePart getHousePart() {
		return selectedPart;
	}

	@Override
	public String getPresentationName() {
		return "U-Factor Change for Selected Part";
	}

}
