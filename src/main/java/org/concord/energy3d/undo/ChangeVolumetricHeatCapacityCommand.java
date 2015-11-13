package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Thermalizable;

public class ChangeVolumetricHeatCapacityCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double orgVolumetricHeatCapacity, newVolumetricHeatCapacity;
	private HousePart selectedPart;

	public ChangeVolumetricHeatCapacityCommand(HousePart selectedPart) {
		this.selectedPart = selectedPart;
		if (selectedPart instanceof Thermalizable)
			orgVolumetricHeatCapacity = ((Thermalizable) selectedPart).getVolumetricHeatCapacity();
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (selectedPart instanceof Thermalizable) {
			newVolumetricHeatCapacity = ((Thermalizable) selectedPart).getVolumetricHeatCapacity();
			((Thermalizable) selectedPart).setVolumetricHeatCapacity(orgVolumetricHeatCapacity);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (selectedPart instanceof Thermalizable)
			((Thermalizable) selectedPart).setVolumetricHeatCapacity(newVolumetricHeatCapacity);
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
