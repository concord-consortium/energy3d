package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.scene.Scene;

public class SetRibsForAllParabolicDishesCommand extends MyAbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final int[] oldValues;
	private int[] newValues;
	private final List<ParabolicDish> dishes;

	public SetRibsForAllParabolicDishesCommand() {
		dishes = Scene.getInstance().getAllParabolicDishes();
		final int n = dishes.size();
		oldValues = new int[n];
		for (int i = 0; i < n; i++) {
			oldValues[i] = dishes.get(i).getNumberOfRibs();
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = dishes.size();
		newValues = new int[n];
		for (int i = 0; i < n; i++) {
			final ParabolicDish d = dishes.get(i);
			newValues[i] = d.getNumberOfRibs();
			d.setNumberOfRibs(oldValues[i]);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		final int n = dishes.size();
		for (int i = 0; i < n; i++) {
			dishes.get(i).setNumberOfRibs(newValues[i]);
		}
	}

	@Override
	public String getPresentationName() {
		return "Set Ribs for All Parabolic Dishes";
	}

}