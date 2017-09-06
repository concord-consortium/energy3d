package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.scene.Scene;
import org.concord.energy3d.scene.SceneManager;

public class ChangeBaseHeightForAllParabolicDishesCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldValues;
	private double[] newValues;
	private final List<ParabolicDish> dishes;

	public ChangeBaseHeightForAllParabolicDishesCommand() {
		dishes = Scene.getInstance().getAllParabolicDishes();
		final int n = dishes.size();
		oldValues = new double[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = dishes.get(i).getBaseHeight();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = dishes.size();
		newValues = new double[n];
		for (int i = 0; i < n; i++) {
			final ParabolicDish d = dishes.get(i);
			newValues[i] = d.getBaseHeight();
			d.setBaseHeight(oldValues[i]);
			d.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = dishes.size();
		for (int i = 0; i < n; i++) {
			final ParabolicDish d = dishes.get(i);
			d.setBaseHeight(newValues[i]);
			d.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Change Base Height for All Parabolic Dishes";
	}

}
