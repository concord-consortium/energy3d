package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Thermalizable;
import org.concord.energy3d.scene.Scene;

public class ChangeBuildingUValueCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private double[] orgUValues, newUValues;
	private HousePart selectedPart;
	private List<HousePart> parts;

	public ChangeBuildingUValueCommand(HousePart selectedPart) {
		this.selectedPart = selectedPart;
		if (!(selectedPart instanceof Thermalizable))
			throw new IllegalArgumentException(selectedPart + "is not thermalizable!");
		parts = Scene.getInstance().getHousePartsOfSameTypeInBuilding(selectedPart);
		int n = parts.size();
		orgUValues = new double[n];
		for (int i = 0; i < n; i++) {
			orgUValues[i] = ((Thermalizable) parts.get(i)).getUValue();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		int n = parts.size();
		newUValues = new double[n];
		for (int i = 0; i < n; i++) {
			newUValues[i] = ((Thermalizable) parts.get(i)).getUValue();
			((Thermalizable) parts.get(i)).setUValue(orgUValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		int n = parts.size();
		for (int i = 0; i < n; i++) {
			((Thermalizable) parts.get(i)).setUValue(newUValues[i]);
		}
	}

	// for action logging
	public HousePart getHousePart() {
		return selectedPart;
	}

	@Override
	public String getPresentationName() {
		return "U-Factor Change for Whole Building";
	}

}
