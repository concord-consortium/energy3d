package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.HousePart;
import org.concord.energy3d.model.Thermal;
import org.concord.energy3d.scene.Scene;

public class ChangeBuildingUValueCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final HousePart part;
	private final List<HousePart> parts;

	public ChangeBuildingUValueCommand(final HousePart part) {
		this.part = part;
		if (!(part instanceof Thermal)) {
			throw new IllegalArgumentException(part + "is not thermalizable!");
		}
		parts = Scene.getInstance().getPartsOfSameTypeInBuilding(part);
		final int n = parts.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = ((Thermal) parts.get(i)).getUValue();
		}
	}

	public HousePart getPart() {
		return part;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = parts.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			newValues[i] = ((Thermal) parts.get(i)).getUValue();
			((Thermal) parts.get(i)).setUValue(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = parts.size();
		for (int i = 0; i < n; i++) {
			((Thermal) parts.get(i)).setUValue(newValues[i]);
		}
	}

	@Override
	public char getOneLetterCode() {
		return 'U';
	}

	@Override
	public String getPresentationName() {
		return "U-Factor Change for Whole Building";
	}

}
