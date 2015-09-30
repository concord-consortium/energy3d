package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;

public class ChangePartVolumetricHeatCapacityCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double orgVolumetricHeatCapacity, newVolumetricHeatCapacity;
	private HousePart selectedPart;

	public ChangePartVolumetricHeatCapacityCommand(HousePart selectedPart) {
		this.selectedPart = selectedPart;
		orgVolumetricHeatCapacity = selectedPart.getVolumetricHeatCapacity();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newVolumetricHeatCapacity = selectedPart.getVolumetricHeatCapacity();
		selectedPart.setVolumetricHeatCapacity(orgVolumetricHeatCapacity);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		selectedPart.setVolumetricHeatCapacity(newVolumetricHeatCapacity);
	}

	// for action logging
	public HousePart getHousePart() {
		return selectedPart;
	}

	@Override
	public String getPresentationName() {
		return "Volumetric Heat Capacity Change for Selected Part";
	}

}
