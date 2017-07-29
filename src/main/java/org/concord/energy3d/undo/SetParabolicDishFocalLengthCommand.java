package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.ParabolicDish;

public class SetParabolicDishFocalLengthCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldCurvatureParameter;
	private double newCurvatureParameter;
	private final ParabolicDish dish;

	public SetParabolicDishFocalLengthCommand(final ParabolicDish dish) {
		this.dish = dish;
		oldCurvatureParameter = dish.getFocalLength();
	}

	public ParabolicDish getParabolicDish() {
		return dish;
	}

	public double getOldValue() {
		return oldCurvatureParameter;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newCurvatureParameter = dish.getFocalLength();
		dish.setFocalLength(oldCurvatureParameter);
		dish.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		dish.setFocalLength(newCurvatureParameter);
		dish.draw();
	}

	@Override
	public String getPresentationName() {
		return "Set Focal Length for Selected Parabolic Dish";
	}

}
