package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.scene.SceneManager;

public class SetFocalLengthForParabolicDishesOnFoundationCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldCurvatureParameters;
	private double[] newCurvatureParameters;
	private final Foundation foundation;
	private final List<ParabolicDish> dishes;

	public SetFocalLengthForParabolicDishesOnFoundationCommand(final Foundation foundation) {
		this.foundation = foundation;
		dishes = foundation.getParabolicDishes();
		final int n = dishes.size();
		oldCurvatureParameters = new double[n];
		for (int i = 0; i < n; i++) {
			final ParabolicDish d = dishes.get(i);
			oldCurvatureParameters[i] = d.getFocalLength();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = dishes.size();
		newCurvatureParameters = new double[n];
		for (int i = 0; i < n; i++) {
			final ParabolicDish d = dishes.get(i);
			newCurvatureParameters[i] = d.getFocalLength();
			d.setFocalLength(oldCurvatureParameters[i]);
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
			d.setFocalLength(newCurvatureParameters[i]);
			d.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Set Focal Length for All Parabolic Dishes on Selected Foundation";
	}

}
