package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Thermalizable;

public class ChangePartUValueCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double orgUValue, newUValue;
	private HousePart selectedPart;

	public ChangePartUValueCommand(HousePart selectedPart) {
		this.selectedPart = selectedPart;
		if (selectedPart instanceof Thermalizable)
			orgUValue = ((Thermalizable) selectedPart).getUValue();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (selectedPart instanceof Thermalizable) {
			newUValue = ((Thermalizable) selectedPart).getUValue();
			((Thermalizable) selectedPart).setUValue(orgUValue);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (selectedPart instanceof Thermalizable)
			((Thermalizable) selectedPart).setUValue(newUValue);
	}

	// for action logging
	public HousePart getHousePart() {
		return selectedPart;
	}

	@Override
	public String getPresentationName() {
		return "U-Value Change for Selected Part";
	}

}
