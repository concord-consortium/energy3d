package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.Scene;

public class ChangeAtmosphericDustLossCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double oldValue;
	private double newValue;

	public ChangeAtmosphericDustLossCommand() {
		oldValue = Scene.getInstance().getAtmosphere().getDustLoss();
	}

	public double getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		newValue = Scene.getInstance().getAtmosphere().getDustLoss();
		Scene.getInstance().getAtmosphere().setDustLoss(oldValue);
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		Scene.getInstance().getAtmosphere().setDustLoss(newValue);
	}

	@Override
	public String getPresentationName() {
		return "Change Atmospheric Dust Loss";
	}

}
