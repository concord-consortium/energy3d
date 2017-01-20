package org.concord.energy3d.undo;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.Scene;

public class ChangeAtmosphericDustLossCommand extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;
	private final double[] oldValue = new double[12];
	private final double[] newValue = new double[12];

	public ChangeAtmosphericDustLossCommand() {
		for (int i = 0; i < 12; i++) {
			oldValue[i] = Scene.getInstance().getAtmosphere().getDustLoss(i);
		}
	}

	public double[] getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		for (int i = 0; i < 12; i++) {
			newValue[i] = Scene.getInstance().getAtmosphere().getDustLoss(i);
			Scene.getInstance().getAtmosphere().setDustLoss(oldValue[i], i);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		for (int i = 0; i < 12; i++) {
			Scene.getInstance().getAtmosphere().setDustLoss(newValue[i], i);
		}
	}

	@Override
	public String getPresentationName() {
		return "Change Atmospheric Dust Loss";
	}

}
