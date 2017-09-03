package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.model.ParabolicDish;

public class SetParabolicDishLabelCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final boolean oldLabelId;
	private final boolean oldLabelCustom;
	private final boolean oldLabelEnergyOutput;
	private boolean newLabelId;
	private boolean newLabelCustom;
	private boolean newLabelEnergyOutput;
	private final ParabolicDish dish;

	public SetParabolicDishLabelCommand(final ParabolicDish dish) {
		this.dish = dish;
		oldLabelId = dish.getLabelId();
		oldLabelCustom = dish.getLabelCustom();
		oldLabelEnergyOutput = dish.getLabelEnergyOutput();
	}

	public ParabolicDish getParabolicDish() {
		return dish;
	}

	public boolean getOldLabelId() {
		return oldLabelId;
	}

	public boolean getNewLabelId() {
		return newLabelId;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newLabelId = dish.getLabelId();
		newLabelCustom = dish.getLabelCustom();
		newLabelEnergyOutput = dish.getLabelEnergyOutput();
		dish.setLabelId(oldLabelId);
		dish.setLabelCustom(oldLabelCustom);
		dish.setLabelEnergyOutput(oldLabelEnergyOutput);
		dish.draw();
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		dish.setLabelId(newLabelId);
		dish.setLabelCustom(newLabelCustom);
		dish.setLabelEnergyOutput(newLabelEnergyOutput);
		dish.draw();
	}

	@Override
	public String getPresentationName() {
		return "Change Label of Parabolic Dish";
	}

}
