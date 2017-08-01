package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.ParabolicDish;

public class SetParabolicDishStructureTypeCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;
	private final ParabolicDish dish;

	public SetParabolicDishStructureTypeCommand(final ParabolicDish dish) {
		this.dish = dish;
		oldValue = dish.getStructureType();
	}

	public ParabolicDish getParabolicDish() {
		return dish;
	}

	public double getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = dish.getStructureType();
		dish.setStructureType(oldValue);
		dish.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		dish.setStructureType(newValue);
		dish.draw();
	}

	@Override
	public String getPresentationName() {
		return "Set Structure Type for Selected Parabolic Dish";
	}

}
