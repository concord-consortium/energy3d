package org.concord.energy3d.undo;

import java.util.List;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.Foundation;
import org.concord.energy3d.model.ParabolicDish;
import org.concord.energy3d.scene.SceneManager;

public class SetApertureRadiusForParabolicDishesOnFoundationCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldRadii;
	private double[] newRadii;
	private final Foundation foundation;
	private final List<ParabolicDish> dishes;

	public SetApertureRadiusForParabolicDishesOnFoundationCommand(final Foundation foundation) {
		this.foundation = foundation;
		dishes = foundation.getParabolicDishes();
		final int n = dishes.size();
		oldRadii = new double[n];
		for (int i = 0; i < n; i++) {
			final ParabolicDish d = dishes.get(i);
			oldRadii[i] = d.getApertureRadius();
		}
	}

	public Foundation getFoundation() {
		return foundation;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		final int n = dishes.size();
		newRadii = new double[n];
		for (int i = 0; i < n; i++) {
			final ParabolicDish d = dishes.get(i);
			newRadii[i] = d.getApertureRadius();
			d.setApertureRadius(oldRadii[i]);
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
			d.setApertureRadius(newRadii[i]);
			d.draw();
		}
		SceneManager.getInstance().refresh();
	}

	@Override
	public String getPresentationName() {
		return "Set Aperture Radius for All Parabolic Dishes on Selected Foundation";
	}

}
