package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.ParabolicDish;

public class SetParabolicDishRibsCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int oldValue;
	private int newValue;
	private final ParabolicDish dish;

	public SetParabolicDishRibsCommand(final ParabolicDish dish) {
		this.dish = dish;
		oldValue = dish.getNumberOfRibs();
	}

	public ParabolicDish getParabolicDish() {
		return dish;
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
		newValue = dish.getNumberOfRibs();
		dish.setNumberOfRibs(oldValue);
		dish.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		dish.setNumberOfRibs(newValue);
		dish.draw();
	}

	@Override
	public String getPresentationName() {
		return "Set Ribs for Selected Parabolic Dish";
	}

}
