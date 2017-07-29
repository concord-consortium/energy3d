package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.ParabolicDish;

public class SetParabolicDishCurvatureParameterCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldCurvatureParameter;
	private double newCurvatureParameter;
	private final ParabolicDish dish;

	public SetParabolicDishCurvatureParameterCommand(final ParabolicDish dish) {
		this.dish = dish;
		oldCurvatureParameter = dish.getCurvatureParameter();
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
		newCurvatureParameter = dish.getCurvatureParameter();
		dish.setCurvatureParameter(oldCurvatureParameter);
		dish.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		dish.setCurvatureParameter(newCurvatureParameter);
		dish.draw();
	}

	@Override
	public String getPresentationName() {
		return "Set Curvature Parameter for Selected Parabolic Dish";
	}

}
