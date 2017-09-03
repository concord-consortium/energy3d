package org.concord.energy3d.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.concord.energy3d.scene.Scene;

public class ChangeSnowReflectionFactorCommand extends AbstractUndoableEditWithTimestamp {

	private static final long serialVersionUID = 1L;
	private final double[] oldValue = new double[12];
	private final double[] newValue = new double[12];

	public ChangeSnowReflectionFactorCommand() {
		for (int i = 0; i < 12; i++) {
			oldValue[i] = Scene.getInstance().getGround().getSnowReflectionFactor(i);
		}
	}

	public double[] getOldValue() {
		return oldValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		for (int i = 0; i < 12; i++) {
			newValue[i] = Scene.getInstance().getGround().getSnowReflectionFactor(i);
			Scene.getInstance().getGround().setSnowReflectionFactor(oldValue[i], i);
		}
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		for (int i = 0; i < 12; i++) {
			Scene.getInstance().getGround().setSnowReflectionFactor(newValue[i], i);
		}
	}

	@Override
	public String getPresentationName() {
		return "Change Snow Reflection Factor";
	}

}
