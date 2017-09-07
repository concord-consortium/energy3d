package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Thermal;

public class ChangeVolumetricHeatCapacityCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double oldValue, newValue;
	private final HousePart part;

	public ChangeVolumetricHeatCapacityCommand(final HousePart part) {
		this.part = part;
		if (part instanceof Thermal) {
			oldValue = ((Thermal) part).getVolumetricHeatCapacity();
		}
	}

	public double getOldValue() {
		return oldValue;
	}

	public HousePart getPart() {
		return part;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		if (part instanceof Thermal) {
			newValue = ((Thermal) part).getVolumetricHeatCapacity();
			((Thermal) part).setVolumetricHeatCapacity(oldValue);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		if (part instanceof Thermal) {
			((Thermal) part).setVolumetricHeatCapacity(newValue);
		}
	}

	@Override
	public String getPresentationName() {
		return "Volumetric Heat Capacity Change for Selected Part";
	}

}
