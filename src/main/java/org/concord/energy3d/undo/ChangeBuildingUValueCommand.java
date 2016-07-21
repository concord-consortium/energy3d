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
	private double[] oldValues, newValues;
	private HousePart part;
	private List<HousePart> parts;

	public ChangeBuildingUValueCommand(HousePart part) {
		this.part = part;
		if (!(part instanceof Thermalizable))
			throw new IllegalArgumentException(part + "is not thermalizable!");
		parts = Scene.getInstance().getPartsOfSameTypeInBuilding(part);
		int n = parts.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = ((Thermalizable) parts.get(i)).getUValue();
		}
	}

	public HousePart getPart() {
		return part;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		int n = parts.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			newValues[i] = ((Thermalizable) parts.get(i)).getUValue();
			((Thermalizable) parts.get(i)).setUValue(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		int n = parts.size();
		for (int i = 0; i < n; i++) {
			((Thermalizable) parts.get(i)).setUValue(newValues[i]);
		}
	}

	@Override
	public String getPresentationName() {
		return "U-Factor Change for Whole Building";
	}

}
