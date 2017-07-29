package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.scene.Scene;

public class SetApertureRadiusForAllParabolicDishesCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final List<ParabolicDish> dishes;

	public SetApertureRadiusForAllParabolicDishesCommand() {
		dishes = Scene.getInstance().getAllParabolicDishes();
		final int n = dishes.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = dishes.get(i).getApertureRadius();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = dishes.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			final ParabolicDish d = dishes.get(i);
			newValues[i] = d.getApertureRadius();
			d.setApertureRadius(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = dishes.size();
		for (int i = 0; i < n; i++) {
			dishes.get(i).setApertureRadius(newValues[i]);
		}
	}

	@Override
	public String getPresentationName() {
		return "Set Aperture Radius for All Parabolic Dishes";
	}

}